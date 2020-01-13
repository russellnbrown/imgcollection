using Microsoft.Maps.MapControl.WPF;
using System;
using System.Collections.ObjectModel;
using System.Globalization;
using System.Windows;
using System.Windows.Media;
using System.Windows.Media.Imaging;


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


        const int removeAge = 20;
        const int insertAge = 15;
        int cl = 0;

         MapLayer[] ml = new MapLayer[2];

        public MainWindow()
        {
            instance = this;
            l.To("pplot.log");
            planes = new ObservableCollection<Plane>();
            InitializeComponent();
            planelist.ItemsSource = planes;


            Closing += MainWindow_Closing;

 

            ml[0] = new MapLayer();
            ml[1] = new MapLayer();
            mainmap.Children.Add(ml[0]);
            mainmap.Children.Add(ml[1]);

            string dump = "192.168.20.17";
            string sim = "sim";
            string local = "127.0.0.1";

            d1090 = new Dump1090Client(dump); 

            System.Windows.Threading.DispatcherTimer dispatcherTimer = new System.Windows.Threading.DispatcherTimer();
            dispatcherTimer.Tick += updateMainList;
            dispatcherTimer.Interval = new TimeSpan(0, 0, 0, 0, 250);
            dispatcherTimer.Start();
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

        static GEO.Location R16ApBL = new GEO.Location(-33.927452, 151.169001);
        static GEO.Location R16ApBR = new GEO.Location(-33.927274, 151.173292 );
        static GEO.Location R16ApTL = new GEO.Location(-33.845030, 151.138984 );
        static GEO.Location R16ApTR = new GEO.Location(-33.838541, 151.157009 );

        GEO.Location[] approach16R = { R16ApBL, R16ApBR, R16ApTL, R16ApTR };
        bool inside16LApproach(Plane p)
        {
            GEO.Location pl = new GEO.Location(p.Latitude, p.Longitude);
            return GEO.isInside(approach16R,  pl);
 
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
            bool inside = inside16LApproach(p);

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
