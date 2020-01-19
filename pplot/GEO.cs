using Microsoft.Maps.MapControl.WPF;
using System;
using System.Device.Location;

namespace pplot
{
    public class GEO
    {



        // Define Infinite (Using INT_MAX 
        // caused overflow problems) 
        static int INF = 10000;

        // Given three colinear points p, q, r, 
        // the function checks if point q lies 
        // on line segment 'pr' 
        private static bool onSegment(Location p, Location q, Location r)
        {
            if (q.Longitude <= Math.Max(p.Longitude, r.Longitude) &&
                q.Longitude >= Math.Min(p.Longitude, r.Longitude) &&
                q.Latitude <= Math.Max(p.Latitude, r.Latitude) &&
                q.Latitude >= Math.Min(p.Latitude, r.Latitude))
            {
                return true;
            }
            return false;
        }

        // To find orientation of ordered triplet (p, q, r). 
        // The function returns following values 
        // 0 --> p, q and r are colinear 
        // 1 --> Clockwise 
        // 2 --> Counterclockwise 
        private static int orientation(Location p, Location q, Location r)
        {
            int val = (int)((q.Latitude - p.Latitude) * (r.Longitude - q.Longitude) -
                    (q.Longitude - p.Longitude) * (r.Latitude - q.Latitude));

            if (val == 0)
            {
                return 0; // colinear 
            }
            return (val > 0) ? 1 : 2; // clock or counterclock wise 
        }

        // The function that returns true if 
        // line segment 'p1q1' and 'p2q2' intersect. 
        private static bool doIntersect(Location p1, Location q1,
                                Location p2, Location q2)
        {
            // Find the four orientations needed for 
            // general and special cases 
            int o1 = orientation(p1, q1, p2);
            int o2 = orientation(p1, q1, q2);
            int o3 = orientation(p2, q2, p1);
            int o4 = orientation(p2, q2, q1);

            // General case 
            if (o1 != o2 && o3 != o4)
            {
                return true;
            }

            // Special Cases 
            // p1, q1 and p2 are colinear and 
            // p2 lies on segment p1q1 
            if (o1 == 0 && onSegment(p1, p2, q1))
            {
                return true;
            }

            // p1, q1 and p2 are colinear and 
            // q2 lies on segment p1q1 
            if (o2 == 0 && onSegment(p1, q2, q1))
            {
                return true;
            }

            // p2, q2 and p1 are colinear and 
            // p1 lies on segment p2q2 
            if (o3 == 0 && onSegment(p2, p1, q2))
            {
                return true;
            }

            // p2, q2 and q1 are colinear and 
            // q1 lies on segment p2q2 
            if (o4 == 0 && onSegment(p2, q1, q2))
            {
                return true;
            }

            // Doesn't fall in any of the above cases 
            return false;
        }


        public static bool isInside(LocationCollection pol, Location p)
        {
            // https://wrf.ecse.rpi.edu/Research/Short_Notes/pnpoly.html
            //int pnpoly(int nvert, float* vertx, float* verty, float testx, float testy)
            //{
            //    int i, j, c = 0;
            //    for (i = 0, j = nvert - 1; i < nvert; j = i++)
            //    {
            //        if (((verty[i] > testy) != (verty[j] > testy)) &&
            //         (testx < (vertx[j] - vertx[i]) * (testy - verty[i]) / (verty[j] - verty[i]) + vertx[i]))
            //            c = !c;
            //    }
            //    return c;
            //}

            int i, j;
            bool c = false;
            for (i = 0, j = pol.Count - 1; i < pol.Count; j = i++)
            {
                if (((pol[i].Latitude > p.Latitude) != (pol[j].Latitude > p.Latitude)) &&
                 (p.Longitude < (pol[j].Longitude - pol[i].Longitude) * (p.Latitude - pol[i].Latitude) / (pol[j].Latitude - pol[i].Latitude) + pol[i].Longitude))
                    c = !c;
            }
            return c;
        }

     

        internal static int distanceBetween(Location p1, Location p2)
        {
            GeoCoordinate gp1 = new GeoCoordinate(p1.Latitude, p1.Longitude);
            GeoCoordinate gp2 = new GeoCoordinate(p2.Latitude, p2.Longitude);

            return (int)gp1.GetDistanceTo(gp2);
        }
    }
}

  
// This code is contributed by 29AjayKumar 
