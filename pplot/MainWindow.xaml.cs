using Microsoft.Maps.MapControl.WPF;
using System;
using System.Collections.ObjectModel;
using System.Globalization;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;

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


        private Airport ap;

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
                planes = new ObservableCollection<Plane>();
                InitializeComponent();
                planelist.ItemsSource = planes;


                Closing += MainWindow_Closing;



                ml[0] = new MapLayer();
                ml[1] = new MapLayer();
                mainmap.Children.Add(ml[0]);
                mainmap.Children.Add(ml[1]);

                drawAirport(ap);


                
                d1090 = new Dump1090Client(ap);



                System.Windows.Threading.DispatcherTimer dispatcherTimer = new System.Windows.Threading.DispatcherTimer();
                dispatcherTimer.Tick += updateMainList;
                dispatcherTimer.Interval = new TimeSpan(0, 0, 0, 0, 250);
                dispatcherTimer.Start();
            }
            catch(Exception e)
            {
                MessageBox.Show(e.Message);
                Environment.Exit(0);
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

                foreach(var c in rw.config)
                {
                    poly = new MapPolyline();
                    poly.Locations = c.approach;
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
            foreach (Plane p in planes)
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
            ml[inactiveLayer()].Children.Clear();

            bool updateList = false;

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
                            planes.Add(lp);
                            updateList = true;

                        }
                        lp.UpdateFrom(p);
                    }
                }
            }

            bool removed = true;
            while (removed)
            {
                removed = false;
                foreach (Plane p in planes)
                {
                    if (p.Age > removeAge)
                    {
                        updateList = true;
                        removed = true;
                        planes.Remove(p);
                        break;
                    }
                }
            }

            if (updateList)
                planelist.ItemsSource = planes;
            planelist.Items.Refresh();

            drawPlanes();

            ml[activeLayer()].Visibility = Visibility.Hidden;
            ml[inactiveLayer()].Visibility = Visibility.Visible;
            toggleLayer();

        }

 

  

        bool isInsidePoly(Plane p, LocationCollection poly)
        {
            Location pl = new Location(p.Latitude, p.Longitude);
            return GEO.isInside(poly,  pl);
        }


        void drawPlanes()
        {
            foreach (Plane p in planes)
            {
                drawPlane4(p);
            }
        }

      
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
        Typeface boldText = new Typeface(new FontFamily("Century"), FontStyles.Normal, FontWeights.Bold,  FontStretches.Normal);
        StreamGeometry director = null;



        StreamGeometry MakeDirector(Rect r)
        {
            Point point1 = new Point(r.Left, r.Bottom);
            Point point2 = new Point((r.Right - r.Left)/2, 0);
            Point point3 = new Point(r.Right, r.Bottom);
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
            string line2 = alt + " " + hdg + " " + inside.ToString();


            Rect fbound = new Rect(0, 0, w, h);
            Rect rbound = new Rect(cw - dw - pd, 0, cw + dw + pd, h);
            Rect pbound = new Rect(cw-ps/2,ch-ps/2,ps,ps);

            using (var r = visual.RenderOpen())
            {
                r.DrawRectangle(fbkg, grayPen, fbound);
                r.DrawRectangle(rbkg, grayPen, rbound);

                r.DrawRectangle(planeBrush, planePen, pbound);              
                //r.DrawText(new FormattedText(line1, CultureInfo.CurrentCulture, FlowDirection.LeftToRight, boldText, 12.0, Brushes.Black), new Point(cw+dw+pd, 0));
               // r.DrawText(new FormattedText(line2, CultureInfo.CurrentCulture, FlowDirection.LeftToRight, normalText, 12.0, Brushes.Black), new Point(cw+dw+pd, 15));
                
                /*RotateTransform rt = new RotateTransform(p.Track, dw / 2, dh / 2);
                TranslateTransform tt = new TranslateTransform(cw+pd,5);
                TransformGroup myTransformGroup = new TransformGroup();
                myTransformGroup.Children.Add(rt);
                myTransformGroup.Children.Add(tt);
                director.Transform = myTransformGroup;
                r.DrawGeometry(Brushes.LightGray, new Pen(Brushes.DarkBlue, 1) , director);*/
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



    }
}
