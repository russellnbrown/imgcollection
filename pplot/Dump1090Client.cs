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
        private Airport ap;
        private StreamWriter rawData;

        internal Dump1090Client(Airport ap)
        {
            rawData = new StreamWriter("rawdata.log", true);
            this.ap = ap;
            if (ap.useSumulator)
            {
                t = new Thread(new ThreadStart(Simulate));
            }
            else
            {
                this.connectTo = ap.dump1090;
                t = new Thread(new ThreadStart(Connect));
            }
            planesd = new SortedDictionary<string, Plane>();
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
            rawData.Close();
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

            foreach(Airport.Simulation s in ap.simTracks)
            {
                p = GetPlane(s.name);
                p.Track = 160;
                p.Altitude = 1000;
                sims.Add(new SimTrack(p, s.name, s.delay, s.timespan, s.track[0], s.track[1]));
            }
 

            lock (planesd)
            {
                foreach (SimTrack s in sims)
                {
                       // planesd.Add(s.p.HexIdent, s.p);
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
                                rawData.WriteLine(rx); rawData.Flush();
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
                            //l.Info("ALT 2 becomes " + m.Altitude);
                            p.GroundSpeed = m.GroundSpeed;
                            p.Track = ifromStr(m.Track);
                            p.VerticalRat = m.VerticalRat;
                            p.Latitude = dfromStr(m.Latitude);
                            p.Longitude = dfromStr(m.Longitude);
                            p.IsOnGround = m.IsOnGround;
                            break;
                        case 3:
                            p.Altitude = ifromStr(m.Altitude);
                            //l.Info("ALT 3 becomes " + m.Altitude);
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
                            //l.Info("ALT 5 becomes " + m.Altitude);
                            p.AlertSquawkChange = m.AlertSquawkChange;
                            p.SPIIdent = m.SPIIdent;
                            p.IsOnGround = m.IsOnGround;
                            break;
                        case 6:
                            if (m.Altitude.Length > 0)
                            {
                                p.Altitude = ifromStr(m.Altitude);
                                //l.Info("ALT 6 becomes " + m.Altitude);
                            }
                            p.Squawk = m.Squawk;
                            p.AlertSquawkChange = m.AlertSquawkChange;
                            p.Emergency = m.Emergency;
                            p.SPIIdent = m.SPIIdent;
                            p.IsOnGround = m.IsOnGround;
                            break;
                        case 7:
                            p.Altitude = ifromStr(m.Altitude);
                            //l.Info("ALT 7 becomes " + m.Altitude);
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
