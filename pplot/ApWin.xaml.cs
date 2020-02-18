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

namespace pplot
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        private Dump1090Client d1090;
        private ObservableCollection<Plane> planes;
        public static MainWindow instance;
        public static MainWindow Get() { return instance; }

      //  private static LocationCollection approach16R = approach16L.
      //  private static LocationCollection approach16L = makeApproach16L();
      //  private static LocationCollection approach28R = makeApproach28R();
      //  private static LocationCollection approach28L = makeApproach28L();

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

        public MainWindow()
        {
            try
            {
                instance = this;
                string afile = "airport.dat";
                if (Environment.GetCommandLineArgs().Length == 2)
                     afile = Environment.GetCommandLineArgs()[1];
              
                ap = new Airport(afile);

                l.To("pplot.log");
                Planes = new ObservableCollection<Plane>();
                InitializeComponent();
                createMapSection(mapGrid);
                createMessageSection(msgGrid);
                createAprSection(aprGrid);
                createDepSection(depGrid);
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

            


                //AddMessage(Color.FromRgb(255,0,0), "test") ;
            }
            catch(Exception e)
            {
                MessageBox.Show(e.Message);
                Environment.Exit(0);
            }
        }

        ListView msgLst = null;

        private void createMessageSection(Panel m)
        {
           // m.Background = new SolidColorBrush(Color.FromRgb(200, 200, 100));

            msgLst = new ListView();
            m.Children.Add(msgLst);
        }

        private void AddMessage(String m)
        {
            AddMessage(Colors.White, m, false);
        }
        private void AddMessage(Color c, String m, bool alert)
        {
            ListViewItem lvi = new ListViewItem();
            lvi.Content = m;
            lvi.Foreground = new SolidColorBrush(c);
            lvi.Background = new SolidColorBrush(Colors.OldLace);

            msgLst.Items.Insert(0,lvi);
    
        }

        private void PlayMessage(string s)
        {
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

        private void createAprSection(Panel d)
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

                foreach(var c in rw.config)
                {
                    poly = new MapPolyline();
                    poly.Locations = c.approach.area;
                    poly.Stroke = new SolidColorBrush(Color.FromArgb(255, 255, 0, 255));
                    poly.StrokeThickness = 1;
                    mainmap.Children.Add(poly);

                    foreach(var l in c.lineups)
                    {
                        PlaceDot(l, Color.FromRgb(0, 255, 0), "Lineup for " + c.Name);
                    }
                    PlaceDot(c.takeoff, Color.FromRgb(255,0,0), "Takeoff point for " + c.Name);
  

                }

                foreach (var st in  ap.simTracks)
                {
                    poly = new MapPolyline();
                    poly.Locations = st.track;
                    poly.Stroke = new SolidColorBrush(Color.FromArgb(255, 250, 200, 200));
                    poly.StrokeThickness = 1;
                    mainmap.Children.Add(poly);
                }


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
                foreach(Airport.Zone z in ap.zones.Values)
                {
                    if ( p.Stale )
                    {
                        foreach(String zname in p.InZones)
                        {
                            p.removeZone(zname);
                        }
                        continue;
                    }
                    bool inZone = z.isInside(p);
                    if ( inZone )
                    {
                        if (!p.isInZone(z.Name))
                            p.addZone(z.Name);                               
                    }
                    else 
                    {
                        if (p.isInZone(z.Name))
                            p.removeZone(z.Name);
                    }

                }
            }
        }

        private void drawApproaches()
        {
            foreach(var ap in ap.displays)
            {
                drawDisplay(ap);
            }
        }

        private void checkApproaches()
        {
            foreach(var p in Planes)
            {
                p.Approaching = null;
                foreach(var rw in ap.runways)
                {
                    foreach(var cf in rw.config )
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
  


        void drawPlanes()
        {
            foreach (Plane p in Planes)
            {
                drawPlane4(p);
            }
        }


 

        StreamGeometry MakeDirector(Rect r)
        {
            int hw = 6;
            int hh = 8;
            Point point3 = new Point(-hw, -hh);
            Point point2 = new Point(0, hh);
            Point point1 = new Point(hw,-hh);
            StreamGeometry streamGeometry = new StreamGeometry();
            using (StreamGeometryContext geometryContext = streamGeometry.Open())
            {
                geometryContext.BeginFigure(point1, true, true);
                PointCollection points = new PointCollection
                                             {
                                                 point2,
                                                 point3
                                             };
                geometryContext.PolyLineTo(points, true, true);
            }
            return streamGeometry;
        }

    
        void PlaceDot(Location location, Color color, string ttx)
        {
            Ellipse dot = new Ellipse();
            dot.Fill = new SolidColorBrush(color);
            double radius = 5.0;
            dot.Width = radius * 2;
            dot.Height = radius * 2;
            ToolTip tt = new ToolTip();
            tt.Content = ttx;
            dot.ToolTip = tt;

            Point p0 = mainmap.LocationToViewportPoint(location);
            Point p1 = new Point(p0.X - radius, p0.Y - radius);
            Location loc = mainmap.ViewportPointToLocation(p1);
            MapLayer.SetPosition(dot, loc);
            mainmap.Children.Add(dot);
        }


        void drawPlane4(Plane p)
        {

            int w = 150;
            int h = 32;
            int cw = w / 2;
            int ch = h / 2;
            int pd = 5;
            int lb = 20;
            int dw = 8;
            int dh = 12;
            int ps = 4;

            fbkg.Opacity = 0.1;
            rbkg.Opacity = 0.5;
            planeBrush.Opacity = 1;


            if (director == null)
                director = MakeDirector(new Rect(0,0,dw,dh));

            Pen dc = normalPen;
            if (p.Emergency != null && p.Emergency.Length > 0)
                dc = emergencyPen;

            var target = new RenderTargetBitmap(w, h, 0, 0, PixelFormats.Pbgra32);
            var visual = new DrawingVisual();

            string id = "0x"+p.HexIdent;
            if (p.Callsign != null && p.Callsign.Length > 0)
                id = p.Callsign;
 
            string type = "TTBD";
            if (p.AircraftType != null && p.AircraftType.Length > 0)
                type = p.AircraftType;

            string alt = p.Altitude.ToString();
            string hdg = p.Track.ToString();

            bool inside = false;// isInsidePoly(p, approach16L) | isInsidePoly(p, approach16R) | isInsidePoly(p, approach28L) | isInsidePoly(p, approach28R);

            string line1 = id;
            string line2 = p.Approaching == null ? "" : (p.Approaching.Name + " " + p.ApproachDistance.ToString());// alt + " " + hdg + " " + inside.ToString();


            Rect fbound = new Rect(0, 0, w, h);
            Rect rbound = new Rect(cw - dw - pd, 0, cw + dw + pd, h);
            Rect pbound = new Rect(cw-ps/2,ch-ps/2,ps,ps);

            using (var r = visual.RenderOpen())
            {
                //r.DrawRectangle(fbkg, grayPen, fbound);
                r.DrawRectangle(rbkg, grayPen, rbound);

                r.DrawRectangle(planeBrush, planePen, pbound);              
                r.DrawText(new FormattedText(line1, CultureInfo.CurrentCulture, FlowDirection.LeftToRight, boldText, 12.0, Brushes.Black), new Point(cw+dw+pd, 0));
                r.DrawText(new FormattedText(line2, CultureInfo.CurrentCulture, FlowDirection.LeftToRight, normalText, 12.0, Brushes.Black), new Point(cw+dw+pd, 15));
                
                RotateTransform rt = new RotateTransform(180+p.Track, 0 , 0);
                TranslateTransform tt = new TranslateTransform(cw,ch);
                TransformGroup myTransformGroup = new TransformGroup();
                myTransformGroup.Children.Add(rt);
                myTransformGroup.Children.Add(tt);
                director.Transform = myTransformGroup;
                r.DrawGeometry(Brushes.LightGray, new Pen(Brushes.DarkBlue, 1) , director);
            }

            target.Render(visual);

            System.Windows.Controls.Image image = new System.Windows.Controls.Image();
            image.BeginInit();
            image.Source = target;
            image.EndInit();
            image.Opacity = 1;
            image.Stretch = System.Windows.Media.Stretch.None;

            Location location = new Location() { Latitude = p.Latitude, Longitude = p.Longitude };
            //Center the image around the location specified
            PositionOrigin position = PositionOrigin.Center;

            //Add the image to the defined map layer
            ml[inactiveLayer()].AddChild(image, location, position);



        }

        private void MainWindow_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            d1090.Stop();
        }

        internal void planeEntersZone(Plane p, string v)
        {
            string msg = p.Callsign + " entering zone " + v;
         
            AddMessage(Colors.Orange, msg, true);
            PlayMessage(msg);
         }

        internal void planeLeavesZone(Plane p, string v)
        {
            AddMessage(Colors.Beige, p.Callsign + " leaving zone " + v, false);
        }

    }
}
