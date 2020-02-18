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
    /// Interaction logic for ApWin.xaml
    /// </summary>
    public partial class ApWin : Window
    {


        void drawPlanes()
        {
            foreach (Plane p in Planes)
            {
                drawPlane(p);
            }
        }




        StreamGeometry MakeDirector(Rect r)
        {
            int hw = 6;
            int hh = 8;
            Point point3 = new Point(-hw, -hh);
            Point point2 = new Point(0, hh);
            Point point1 = new Point(hw, -hh);
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


        void drawPlane(Plane p)
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
                director = MakeDirector(new Rect(0, 0, dw, dh));

            Pen dc = normalPen;
            if (p.Emergency != null && p.Emergency.Length > 0)
                dc = emergencyPen;

            var target = new RenderTargetBitmap(w, h, 0, 0, PixelFormats.Pbgra32);
            var visual = new DrawingVisual();

            string id = "0x" + p.HexIdent;
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
            Rect pbound = new Rect(cw - ps / 2, ch - ps / 2, ps, ps);

            using (var r = visual.RenderOpen())
            {
                //r.DrawRectangle(fbkg, grayPen, fbound);
                r.DrawRectangle(rbkg, grayPen, rbound);

                r.DrawRectangle(planeBrush, planePen, pbound);
                r.DrawText(new FormattedText(line1, CultureInfo.CurrentCulture, FlowDirection.LeftToRight, boldText, 12.0, Brushes.Black), new Point(cw + dw + pd, 0));
                r.DrawText(new FormattedText(line2, CultureInfo.CurrentCulture, FlowDirection.LeftToRight, normalText, 12.0, Brushes.Black), new Point(cw + dw + pd, 15));

                RotateTransform rt = new RotateTransform(180 + p.Track, 0, 0);
                TranslateTransform tt = new TranslateTransform(cw, ch);
                TransformGroup myTransformGroup = new TransformGroup();
                myTransformGroup.Children.Add(rt);
                myTransformGroup.Children.Add(tt);
                director.Transform = myTransformGroup;
                r.DrawGeometry(Brushes.LightGray, new Pen(Brushes.DarkBlue, 1), director);
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
    }
}