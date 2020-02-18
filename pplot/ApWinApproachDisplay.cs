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

        private void drawDisplay(Airport.DisplayRunway rw)
        {
            rw.aprCanv.Children.Clear();
            rw.aprCanv.BeginInit();

            double pad = 20;
            double w = rw.aprCanv.ActualWidth;
            double mid = w / 2;
            double h = rw.aprCanv.ActualHeight;
            double rwtop = pad;
            double rwbottom = h - pad;
            double length = rwtop - rwbottom;
            double markSize = length / rw.NumMarks;
            double pixelSize = Math.Abs(length / (rw.NumMarks * rw.MarkDistance));
            double markDist = length / rw.NumMarks;

            Line centerLine = new Line();
            centerLine.Stroke = System.Windows.Media.Brushes.LightSteelBlue;
            centerLine.X1 = mid;
            centerLine.Y1 = rwtop;
            centerLine.X2 = mid;
            centerLine.Y2 = rwbottom;
            centerLine.StrokeThickness = 2;
            rw.aprCanv.Children.Add(centerLine);

            for (int mx = 1; mx <= rw.NumMarks; mx++)
            {
                double ypos = rwbottom + mx * markDist;
                Line marker = new Line();
                marker.Stroke = System.Windows.Media.Brushes.DarkRed;
                marker.X1 = mid;
                marker.Y1 = ypos;
                marker.X2 = mid + pad;
                marker.Y2 = ypos;
                marker.StrokeThickness = 1;
                rw.aprCanv.Children.Add(marker);
                string txt = (mx * rw.MarkDistance).ToString() + "M";
                TextBlock tb = new TextBlock();
                tb.Text = txt;
                tb.Foreground = new SolidColorBrush(Colors.DarkGoldenrod);
                Canvas.SetLeft(tb, mid + 3);
                Canvas.SetTop(tb, ypos);
                rw.aprCanv.Children.Add(tb);

            }

            foreach (Plane p in Planes)
            {
                if (p.Approaching == null || p.Approaching.Name != rw.Name)
                    continue;

                double dist = p.ApproachDistance;
                double pdist = dist * pixelSize;

                TextBlock tb = new TextBlock();
                tb.Text = p.Callsign;
                tb.Foreground = new SolidColorBrush(Colors.White);
                tb.Background = new SolidColorBrush(Colors.DarkBlue);

                tb.Opacity = 1;
                Canvas.SetLeft(tb, mid - 40);
                Canvas.SetTop(tb, rwbottom - pdist);
                rw.aprCanv.Children.Add(tb);
            }
            rw.aprCanv.EndInit();
        }
    }
}

    
