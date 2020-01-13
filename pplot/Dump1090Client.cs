using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Sockets;
using System.Threading;
using System.Windows;

namespace pplot
{
    class Dump1090Client
    {
        private Thread t;
        public static bool running = true;
        private bool connected = false;
        private TcpClient client = null;
        private SortedDictionary<String, Plane> planesd;
        private string connectTo;

        internal Dump1090Client(string connectTo)
        {
            this.connectTo = connectTo;
            planesd = new SortedDictionary<string, Plane>();
            if (connectTo.ToLower().StartsWith("sim"))
                t = new Thread(new ThreadStart(Simulate));
            else
                t = new Thread(new ThreadStart(Connect));
            t.Start();
        }

        public Plane GetPlane(string id)
        {
            if (!planesd.ContainsKey(id))
            {
                Plane p = new Plane();
                p.HexIdent = id;
                planesd.Add(id, p);
                l.Info("NEW aircraft " + id);
            }
            else
                l.Debug("UPDATE aircraft " + id);
            return planesd[id];
        }

        public SortedDictionary<string, Plane> Planesd { get => planesd; set => planesd = value; }

        public void Stop()
        {
            running = false;
            if (client != null)
                client.Close();
            t.Join();
        }

        internal void Simulate()
        {
            Plane p;
            List<SimTrack> sims;
            sims = new List<SimTrack>();

            Point R16R4min = new Point(-33.782107, 151.133291);
            Point R16Rstart = new Point(-33.929414, 151.171594);
            Point R16Rend = new Point(-33.966635, 151.181335);

            Point R16L4min = new Point(-33.782353, 151.144985);
            Point R16Lstart = new Point(-33.949122, 151.188242);
            Point R16Lend = new Point(-33.971822, 151.194035);

            // 16R BL -33.927452, 151.169001
            //     BR -33.927274, 151.173292
            //     TL -33.787301, 151.125339
            //     TR -33.778381, 151.139672

            for (int px = 0; px < 10; px++)
            {
                p = GetPlane(String.Format("SIM{0}", px+1));
                p.Track = 160;
                p.Altitude = 1000;
                sims.Add(new SimTrack(p, String.Format("16R{0}", px + 1), px*2000, 30000, R16R4min, R16Rend));
            }
 

            lock (planesd)
            {
                foreach (SimTrack s in sims)
                {
                        planesd.Add(s.p.HexIdent, s.p);
                }
            }
            
            while (running)
            {

                    System.Threading.Thread.Sleep(200);
                
            }
            foreach (SimTrack s in sims)
            {
                s.Close();
            }
        }

        internal void Connect()
        {
            // Connect thread, we will try to connect as long as main prog is running
            while (running)
            {
                try
                {
                    using (client = new TcpClient())
                    {
                        byte[] receiveBuffer = new byte[1024];
                        // attempt a connection
                        client.Connect(connectTo, 30003); //30003
                        client.Client.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.KeepAlive, true);
                        StreamReader tr = new StreamReader(client.GetStream());
                        Console.WriteLine("Connected OK");
                        connected = true;
                        // service connection
                        try
                        {
                            int bytesRead = 0;
                            while (running)
                            {
                                if (!client.Connected)
                                    break;
                                string rx =  tr.ReadLine();
                                l.Info("Raw:" + rx);
                               // if (bytesRead == 0)
                               //     break;
                               // string rx = Encoding.ASCII.GetString(receiveBuffer, 0, bytesRead);
                                ProcessBaseStationMessage(rx);
                            }
                        }
                        catch (Exception e)
                        {
                            // connecton went down
                            Console.WriteLine("Exception : {0}", e.ToString());
                        }
                        Console.WriteLine("Read loop exiting");
                        client.Close();
                        connected = false;
                    }
                }
                catch (Exception e)
                {
                    Console.WriteLine("Exception : {0}", e.ToString());
                }
                Console.WriteLine("Problem, Try again in 2");
                if ( running )
                    Thread.Sleep(2000);
            }
        }


        internal void ProcessBaseStationMessage(string rx)
        {
            char[] seps = { ',' };
            string[] parts = rx.Split(seps);

            if (parts.Count() < 10)
            {
                l.Error("Too few parts: " + rx);
                return;
            }

            using (SBSMessage m = new SBSMessage(parts))
            {
                if (m.HexIdent != null && m.HexIdent.Length > 0)
                {
                    lock (planesd)
                    {
                        Plane p = GetPlane(m.HexIdent);
                        if (m.Msgype == "MSG")
                        {
                            ProcessTransimssionMessage(m, p);
                        }
                    }
                }
            }
        }
 
        double dfromStr(string s)
        {
            try
            {
                return Double.Parse(s);
            }
            catch
            {

            }
            return 0.0;
        }

        int ifromStr(string s)
        {
            try
            {
                return Int32.Parse(s);
            }
            catch
            {

            }
            return 0;
        }
        void ProcessTransimssionMessage(SBSMessage m, Plane p)
        {
            lock (p)
            {
                p.Updated();
                try
                {
                    switch (m.TransmissionType)
                    {
                        case 1:
                            p.Callsign = m.Callsign;
                            break;
                        case 2:
                            p.Altitude = ifromStr(m.Altitude);
                            l.Info("ALT 2 becomes " + m.Altitude);
                            p.GroundSpeed = m.GroundSpeed;
                            p.Track = ifromStr(m.Track);
                            p.VerticalRat = m.VerticalRat;
                            p.Latitude = dfromStr(m.Latitude);
                            p.Longitude = dfromStr(m.Longitude);
                            p.IsOnGround = m.IsOnGround;
                            break;
                        case 3:
                            p.Altitude = ifromStr(m.Altitude);
                            l.Info("ALT 3 becomes " + m.Altitude);
                            p.Latitude = dfromStr(m.Latitude);
                            p.Longitude = dfromStr(m.Longitude);
                            p.AlertSquawkChange = m.AlertSquawkChange;
                            p.Emergency = m.Emergency;
                            p.SPIIdent = m.SPIIdent;
                            p.IsOnGround = m.IsOnGround;
                            break;
                        case 4:
                            p.GroundSpeed = m.GroundSpeed;
                            p.Track = ifromStr(m.Track);
                            p.VerticalRat = m.VerticalRat;
                            break;
                        case 5:
                            p.Altitude = ifromStr(m.Altitude);
                            l.Info("ALT 5 becomes " + m.Altitude);
                            p.AlertSquawkChange = m.AlertSquawkChange;
                            p.SPIIdent = m.SPIIdent;
                            p.IsOnGround = m.IsOnGround;
                            break;
                        case 6:
                            if (m.Altitude.Length > 0)
                            {
                                p.Altitude = ifromStr(m.Altitude);
                                l.Info("ALT 6 becomes " + m.Altitude);
                            }
                            p.Squawk = m.Squawk;
                            p.AlertSquawkChange = m.AlertSquawkChange;
                            p.Emergency = m.Emergency;
                            p.SPIIdent = m.SPIIdent;
                            p.IsOnGround = m.IsOnGround;
                            break;
                        case 7:
                            p.Altitude = ifromStr(m.Altitude);
                            l.Info("ALT 7 becomes " + m.Altitude);
                            p.IsOnGround = m.IsOnGround;
                            break;
                        case 8:
                            p.IsOnGround = m.IsOnGround;
                            break;
                    }
                    l.Info(p.ToString());

                }
                catch (Exception e)
                {
                    l.Error("Ex " + e.Message + " processing");
                }
            }
        }



    }


}
