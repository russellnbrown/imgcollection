using Microsoft.Maps.MapControl.WPF;
using Microsoft.Maps.MapControl.WPF.Core;
using System;
using System.Collections.ObjectModel;
using System.Globalization;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using System.Speech.Synthesis;
using System.Speech.AudioFormat;
using System.Collections.Generic;

namespace pplot
{
    /// <summary>
    /// Interaction logic for ApWin.xaml
    /// </summary>
    public partial class ApWin : Window
    {
        private Dump1090Client d1090;
        private ObservableCollection<Plane> planes;
        public static ApWin instance;
        public static ApWin Get() { return instance; }
        


        const int removeAge = 20;
        const int insertAge = 15;
        int cl = 0;

        MapLayer[] ml = new MapLayer[2];
        Microsoft.Maps.MapControl.WPF.Map mainmap;

        private Airport ap;

        SpeechSynthesizer synth = new SpeechSynthesizer();
        Pen normalPen = new Pen(Brushes.Blue, 1.0);
        Pen emergencyPen = new Pen(Brushes.Red, 1.0);
        Pen planePen = new Pen(Brushes.OrangeRed, 1.0);
        Pen locPen = new Pen(Brushes.Yellow, 1.0);
        Brush locBrush = Brushes.Yellow;
        Brush planeBrush = new SolidColorBrush(Colors.OrangeRed);
        Brush rbkg = new SolidColorBrush(Colors.Gray);
        Brush fbkg = new SolidColorBrush(Colors.LightPink);

        Pen grayPen = new Pen(Brushes.LightSlateGray, 1.0);
        Typeface normalText = new Typeface("Century");
        Typeface boldText = new Typeface(new FontFamily("Century"), FontStyles.Normal, FontWeights.Bold, FontStretches.Normal);
        StreamGeometry director = null;

        public ObservableCollection<Plane> Planes { get => planes; set => planes = value; }

        public ApWin()
        {
            try
            {
                instance = this;
                l.To("pplot.log");

                OpenSky.Get.Init();

                string afile = "airport.dat";
                if (Environment.GetCommandLineArgs().Length == 2)
                    afile = Environment.GetCommandLineArgs()[1];

                ap = new Airport(afile);

                
                Planes = new ObservableCollection<Plane>();
                InitializeComponent();
                createMapSection(mapGrid);
                createMessageSection(msgGrid);
                createRwSection(rwGrid1, ap.runways[0].config[0]);
                createRwSection(rwGrid2, ap.runways[0].config[1]);
                planelist.ItemsSource = Planes;


                Closing += MainWindow_Closing;




                drawAirport(ap);


                d1090 = new Dump1090Client(ap);

                System.Windows.Threading.DispatcherTimer dispatcherTimer = new System.Windows.Threading.DispatcherTimer();
                dispatcherTimer.Tick += updateMainList;
                dispatcherTimer.Interval = new TimeSpan(0, 0, 0, 0, 250);
                dispatcherTimer.Start();



                // Configure the audio output.   
                synth.SetOutputToDefaultAudioDevice();



            }
            catch (Exception e)
            {
                MessageBox.Show(e.Message);
                Environment.Exit(0);
            }
        }

        ListView msgLst = null;

        private void createMessageSection(Panel m)
        {
            MessagesControl msg = new MessagesControl();
            m.Children.Add(msg);
        }

        public class Message
        {
            public Message(String m, bool a, Color c)
            {
                msg = m;
                alert = a;
                colour = c;
            }
            public String msg;
            public bool alert;
            public Color colour;
        }

        public List<Message> messages = new List<Message>();

        public int GetMessageCount()
        {
            int lm = 0;
            lock(messages)
            {
                lm = messages.Count;
            }
            return lm;
        }
        public Message GetMessage(int x)
        {
            Message m = null;
            lock(messages)
            {
                m = messages[x];
            }
            return m;
        }

        private void AddMessage(Color c, String m, bool alert)
        {
            lock (messages)
            {
                messages.Add(new Message(m, alert, c));
            }
           
        }

        private string augmentStringCarrier(string s)
        {
            s = s.Replace("JST", "jetstar");
            s = s.Replace("VOZ", "virgin");
            s = s.Replace("RXA", "rex");
            s = s.Replace("QFA", "qantas");
            s = s.Replace("QLK", "qantas link");
            return s;
        }

        private void PlayMessage(string s)
        {
            s = augmentStringCarrier(s);
            // Speak a string.  
            synth.SpeakAsync(s);
        }

        private void createMapSection(Panel m)
        {

            mainmap = new Map();
            mainmap.Center = new Location(-33.930, 151.171);
            mainmap.ZoomLevel = 13;
            mainmap.CredentialsProvider = new ApplicationIdCredentialsProvider("AlkF1YZk5OkMIqs-_P_cLGgaV0bKpNwtiZcPTrQPAABlyudnqkDSsbgiiY7qPMNn");
            mainmap.Mode = new RoadMode();

            Grid.SetRow(m, 0);
            Grid.SetColumn(m, 0);

            ml[0] = new MapLayer();
            ml[1] = new MapLayer();
            mainmap.Children.Add(ml[0]);
            mainmap.Children.Add(ml[1]);

            m.Children.Add(mainmap);

        }

        private void createRwSection(Panel d, Airport.RunwayConfiguration rw)
        {
            Grid dg = new Grid();
            dg.Background = new SolidColorBrush(Color.FromRgb(180, 180, 250));
            RunwayControl rwc = new RunwayControl(rw);
            dg.Children.Add(rwc);
            d.Children.Add(dg);

        }
        private void createRwXXSection(Panel d, Airport.RunwayConfiguration rw)
        {
            int ncols = ap.displays.Count;
            Grid dg = new Grid();

            dg.Background = new SolidColorBrush(Color.FromRgb(80, 80, 250));


            for (int c = 0; c < ncols; c++)
            {
                var cd = new ColumnDefinition();
                cd.Width = new GridLength(1, GridUnitType.Star);
                dg.ColumnDefinitions.Add(cd);
            }

            RowDefinition row0 = new RowDefinition();
            row0.Height = new GridLength(1, GridUnitType.Star);
            dg.RowDefinitions.Add(row0);


            for (int c = 0; c < ncols; c++)
            {
                ap.displays[c].aprCanv = new Canvas();


                Grid.SetRow(ap.displays[c].aprCanv, 0);
                Grid.SetColumn(ap.displays[c].aprCanv, 0);
                dg.Children.Add(ap.displays[c].aprCanv);
            }
            d.Children.Add(dg);

        }


        /*
        private void createDepSection(Panel d)
        {
            int ncols = ap.displays.Count;
            Grid dg = new Grid();

            dg.Background = new SolidColorBrush(Color.FromRgb(70, 250, 70));


            for (int c = 0; c < ncols; c++)
            {
                var cd = new ColumnDefinition();
                cd.Width = new GridLength(1, GridUnitType.Star);
                dg.ColumnDefinitions.Add(cd);
            }

            RowDefinition row0 = new RowDefinition();
            dg.RowDefinitions.Add(row0);
            dg.RowDefinitions[0].Height = new GridLength(1, GridUnitType.Star);

            for (int c = 0; c < ncols; c++)
            {
                ap.displays[c].depCanv = new Canvas();


                Grid.SetRow(ap.displays[c].depCanv, 0);
                Grid.SetColumn(ap.displays[c].depCanv, 0);

                dg.Children.Add(ap.displays[c].depCanv);
            }
            d.Children.Add(dg);

        }

        private void drawZones(Airport ap)
        {
            MapPolyline poly;
            foreach (var z in ap.zones)
            {
                poly = new MapPolyline();
                poly.Locations = z.Value.area;
                poly.Stroke = new SolidColorBrush(Color.FromRgb(0, 0, 0255));
                poly.StrokeThickness = 2;
                mainmap.Children.Add(poly);
            }
        }
        */

        private void drawTracks(Airport ap)
        {
            MapPolyline poly;
            foreach (var st in ap.simTracks)
            {
                poly = new MapPolyline();
                poly.Locations = st.track;
                poly.Stroke = new SolidColorBrush(Color.FromArgb(255, 250, 200, 200));
                poly.StrokeThickness = 1;
                mainmap.Children.Add(poly);
            }
        }

        private void drawAirport(Airport ap)
        {
            MapPolyline poly;

            foreach (var rw in ap.runways)
            {
                poly = new MapPolyline();
                poly.Locations = rw.layout;
                poly.Stroke = new SolidColorBrush(Color.FromArgb(255, 255, 0, 255));
                poly.StrokeThickness = 3;
                mainmap.Children.Add(poly);

                foreach (var c in rw.config)
                {

                    foreach (var l in c.lineups)
                    {
                        PlaceDot(l, Color.FromRgb(0, 255, 0), "Lineup for " + c.Name);
                    }
                    PlaceDot(c.takeoff, Color.FromRgb(255, 0, 0), "Takeoff point for " + c.Name);


                }




            }

            drawTracks(ap);
            drawZones(ap);

        }


        private void drawZones(Airport ap)
        {
            MapPolyline poly;

            foreach (var rw in ap.zones)
            {
                poly = new MapPolyline();
                poly.Locations = rw.Value.area;
                poly.Stroke = new SolidColorBrush(Color.FromRgb(0, 0, 255));
                poly.StrokeThickness = 2;
                mainmap.Children.Add(poly);



            }
        }

        private Plane GetLocalPlane(string id)
        {
            foreach (Plane p in Planes)
            {
                if (p.HexIdent == id)
                    return p;
            }
            return null;
        }

        int activeLayer()
        {
            return cl;
        }
        int inactiveLayer()
        {
            if (cl == 1)
                return 0;
            return 1;
        }
        void toggleLayer()
        {
            if (cl == 1)
                cl = 0;
            else
                cl = 1;
        }


        private void updateMainList(object sender, EventArgs e)
        {
            // remove all planes from inactive layer
            ml[inactiveLayer()].Children.Clear();

            // set if change in list itself
            bool updateList = false;

            // add non stale planes from the dump list and update existing
            lock (d1090.Planesd)
            {
                foreach (Plane p in d1090.Planesd.Values)
                {
                    if (p.Age < insertAge)
                    {
                        Plane lp = GetLocalPlane(p.HexIdent);
                        if (lp == null)
                        {
                            lp = new Plane();
                            Planes.Add(lp);
                            if (OpenSky.Get.aircraftInfo.ContainsKey(p.HexIdent))
                            {
                                OpenSky.AircraftInfo ax = OpenSky.Get.aircraftInfo[p.HexIdent];
                                if (ax != null)
                                {
                                    lp.Reg = ax.Reg;
                                    lp.Typ = ax.Typ;
                                    lp.Cpy = ax.Cpy;
                                }
                            }
 
                         
                            updateList = true; // if we don't know about it, indicate list needs updating

                        }
                        lp.UpdateFrom(p);
                    }
                }
            }

            // identify old planes
            foreach (Plane p in Planes)
            {
                if (p.Age > removeAge)
                {
                    p.Stale = true;
                }
            }

            checkZones();
            checkApproaches();

            // remove old planes
            bool removed = true;
            while (removed)
            {
                removed = false;
                foreach (Plane p in Planes)
                {
                    if (p.Stale)
                    {
                        updateList = true;
                        removed = true;
                        Planes.Remove(p);
                        break;
                    }
                }
            }


            // refresh screen
            if (updateList)
                planelist.ItemsSource = Planes;
            planelist.Items.Refresh();

            // draw planes on inactive layer
            drawPlanes();
            drawApproaches();

            // switch layers to revel new positions
            ml[activeLayer()].Visibility = Visibility.Hidden;
            ml[inactiveLayer()].Visibility = Visibility.Visible;
            toggleLayer();

        }

        private void checkZones()
        {
            foreach (Plane p in Planes)
            {
                bool toDo = true;
                while (toDo)
                {
                    toDo = false;
                    foreach (Airport.Zone z in ap.zones.Values)
                    {
                        if (p.Stale)
                        {
                            foreach (Airport.Zone zname in p.InZones)
                            {
                                p.removeZone(zname);
                                toDo = true;
                                break;
                            }
                            continue;
                        }
                        bool inZone = z.isInside(p);
                        if (inZone)
                        {
                            if (!p.isInZone(z))
                            {
                                p.addZone(z);
                                toDo = true;
                                break;
                            }
                        }
                        else
                        {
                            if (p.isInZone(z))
                            {
                                p.removeZone(z);
                                toDo = true;
                                break;
                            }
                        }

                    }
                }
            }
        }

        private void drawApproaches()
        {
            foreach (var ap in ap.displays)
            {
               // drawDisplay(ap);
            }
        }

        private void checkApproaches()
        {
            foreach (var p in Planes)
            {
                p.Approaching = null;
                foreach (var rw in ap.runways)
                {
                    foreach (var cf in rw.config)
                    {
                        if (cf.approach.isInside(p))
                        {
                            p.Approaching = cf;
                            p.ApproachDistance = GEO.distanceBetween(p.Location, cf.takeoff);
                        }
                    }
                }
            }
        }



        private void MainWindow_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            d1090.Stop();
        }

        private string augmentString(string s, Plane p)
        {
            string aug = s.Replace("<callsign>", p.Callsign);
            return aug;
        }

        internal void planeEntersZone(Plane p, Airport.Zone z)
        {
            string msg = augmentString(z.VoiceEnter, p);
            AddMessage(Colors.Red, msg, true);
            PlayMessage(msg);
        }

        internal void planeLeavesZone(Plane p, Airport.Zone z)
        {
            if (z.VoiceLeave.Length > 0)
            {
                string msg = augmentString(z.VoiceLeave, p);
                AddMessage(Colors.Black, msg, false);
            }
        }

    }
}
