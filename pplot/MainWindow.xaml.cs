using Microsoft.Maps.MapControl.WPF;
using System;
using System.Collections.ObjectModel;
using System.Globalization;
using System.Windows;
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

        private static LocationCollection approach16R = makeApproach16R();
        private static LocationCollection approach16L = makeApproach16L();
        private static LocationCollection approach28R = makeApproach28R();
        private static LocationCollection approach28L = makeApproach28L();

        const int removeAge = 20;
        const int insertAge = 15;
        int cl = 0;

         MapLayer[] ml = new MapLayer[2];

        Airport ap;

        public MainWindow()
        {
            try
            {
                instance = this;
                ap = new Airport("C:/Sources/github_svn/imgcollection/trunk/pplot/sydney.xml");

                l.To("pplot.log");
                planes = new ObservableCollection<Plane>();
                InitializeComponent();
                planelist.ItemsSource = planes;


                Closing += MainWindow_Closing;



                ml[0] = new MapLayer();
                ml[1] = new MapLayer();
                mainmap.Children.Add(ml[0]);
                mainmap.Children.Add(ml[1]);

                var poly = new MapPolyline();
                poly.Locations = approach16R;
                poly.Stroke = new SolidColorBrush(Color.FromArgb(255, 255, 0, 0));
                poly.StrokeThickness = 1;
                mainmap.Children.Add(poly);

                poly = new MapPolyline();
                poly.Locations = approach16L;
                poly.Stroke = new SolidColorBrush(Color.FromArgb(255, 255, 0, 0));
                poly.StrokeThickness = 1;
                mainmap.Children.Add(poly);


                poly = new MapPolyline();
                poly.Locations = approach28L;
                poly.Stroke = new SolidColorBrush(Color.FromArgb(255, 255, 255, 0));
                poly.StrokeThickness = 1;
                mainmap.Children.Add(poly);

                poly = new MapPolyline();
                poly.Locations = approach28R;
                poly.Stroke = new SolidColorBrush(Color.FromArgb(255, 255, 255, 0));
                poly.StrokeThickness = 1;
                mainmap.Children.Add(poly);



                string connectTo = FindResource("connectTo").ToString();
                d1090 = new Dump1090Client(connectTo);



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
        Pen locPen = new Pen(Brushes.Yellow, 1.0);
        Brush locBrush = Brushes.Yellow;
        Brush rbkg = new SolidColorBrush(Colors.Gray);

        Pen grayPen = new Pen(Brushes.LightSlateGray, 1.0);
        Typeface normalText = new Typeface("Century");
        Typeface boldText = new Typeface(new FontFamily("Century"), FontStyles.Normal, FontWeights.Bold,  FontStretches.Normal);
        StreamGeometry director = null;


        private static LocationCollection  makeApproach16R()
        {
 
            Location tl = new Location(-33.872743, 151.170923);
            Location tr = new Location(-33.870177, 151.188261);
            Location br = new Location(-33.948715, 151.188784);
            Location bl = new Location(-33.948911, 151.187464);

            var locations = new LocationCollection();
            locations.Add(tl);
            locations.Add(tr);
            locations.Add(br);
            locations.Add(bl);
            locations.Add(tl);
            return locations;
        }

        private static LocationCollection makeApproach16L()
        {
            Location br = new Location(-33.927327, 151.172232);
            Location bl = new Location(-33.927701, 151.169936);

            Location tl = new Location(-33.844984, 151.138692);
            Location tr = new Location(-33.842382, 151.155086);

            var locations = new LocationCollection();
            locations.Add(tl);
            locations.Add(tr);
            locations.Add(br);
            locations.Add(bl);
            locations.Add(tl);
            return locations;
        }

        private static LocationCollection makeApproach28L()
        {

            Location br = new Location(-34.037789, 151.202623);
            Location bl = new Location(-34.039909, 151.191358);
            Location tl = new Location(-33.966965, 151.179666);
            Location tr = new Location(-33.966573, 151.183056);

            var locations = new LocationCollection();
            locations.Add(tl);
            locations.Add(tr);
            locations.Add(br);
            locations.Add(bl);
            locations.Add(tl);
            return locations;

        }

        private static LocationCollection makeApproach28R()
        {
            Location br = new Location(-34.038650, 151.217843);
            Location bl = new Location(-34.039467, 151.205687);
            Location tl = new Location(-33.972181, 151.192871);
            Location tr = new Location(-33.971398, 151.196390);

            var locations = new LocationCollection();
            locations.Add(tl);
            locations.Add(tr);
            locations.Add(br);
            locations.Add(bl);
            locations.Add(tl);
            return locations;
        }

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
        void drawPlane4(Plane p)
        {

            int w = 100;
            int h = 32;
            int lb = 20;
            int dw = 8;
            int dh = 12;

            rbkg.Opacity = 0.5;
           

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

            bool inside = isInsidePoly(p, approach16L) | isInsidePoly(p, approach16R) | isInsidePoly(p, approach28L) | isInsidePoly(p, approach28R);

            string line1 = id;
            string line2 = alt + " " + hdg + " " + inside.ToString();


            Rect rbound = new Rect(0, 0, w, h);

            using (var r = visual.RenderOpen())
            {
                r.DrawRectangle(rbkg, grayPen, rbound);
                r.DrawText(new FormattedText(line1, CultureInfo.CurrentCulture, FlowDirection.LeftToRight, boldText, 12.0, Brushes.Black), new Point(lb + 0, 0));
                r.DrawText(new FormattedText(line2, CultureInfo.CurrentCulture, FlowDirection.LeftToRight, normalText, 12.0, Brushes.Black), new Point(lb + 0, 15));
                RotateTransform rt = new RotateTransform(p.Track, dw / 2, dh / 2);
                TranslateTransform tt = new TranslateTransform(5,5);
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
            PositionOrigin position = PositionOrigin.CenterLeft;

            //Add the image to the defined map layer
            ml[inactiveLayer()].AddChild(image, location, position);



        }

        private void MainWindow_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            d1090.Stop();
        }



    }
}
