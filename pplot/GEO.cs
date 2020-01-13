using System;


namespace pplot
{
    public class GEO
    {



        // Define Infinite (Using INT_MAX 
        // caused overflow problems) 
        static int INF = 10000;

        public class Location
        {
            public double longitude;
            public double latitude;

            public Location(double longitude, double latitude)
            {
                this.longitude = longitude;
                this.latitude = latitude;
            }
        };

        // Given three colinear points p, q, r, 
        // the function checks if point q lies 
        // on line segment 'pr' 
        private static bool onSegment(Location p, Location q, Location r)
        {
            if (q.longitude <= Math.Max(p.longitude, r.longitude) &&
                q.longitude >= Math.Min(p.longitude, r.longitude) &&
                q.latitude <= Math.Max(p.latitude, r.latitude) &&
                q.latitude >= Math.Min(p.latitude, r.latitude))
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
            int val = (int)((q.latitude - p.latitude) * (r.longitude - q.longitude) -
                    (q.longitude - p.longitude) * (r.latitude - q.latitude));

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

        // Returns true if the point p lies 
        // inside the polygon[] with n vertices 
        public static bool isInside(Location[] polygon,  Location p)
        {
            // There must be at least 3 vertices in polygon[] 
            if (polygon.Length < 3)
            {
                return false;
            }

            // Create a point for line segment from p to infinite 
            Location extreme = new Location(INF, p.latitude);

            // Count intersections of the above line 
            // with sides of polygon 
            int count = 0, i = 0;
            do
            {
                int next = (i + 1) % polygon.Length;

                // Check if the line segment from 'p' to 
                // 'extreme' intersects with the line 
                // segment from 'polygon[i]' to 'polygon[next]' 
                if (doIntersect(polygon[i],
                                polygon[next], p, extreme))
                {
                    // If the point 'p' is colinear with line 
                    // segment 'i-next', then check if it lies 
                    // on segment. If it lies, return true, otherwise false 
                    if (orientation(polygon[i], p, polygon[next]) == 0)
                    {
                        return onSegment(polygon[i], p,
                                        polygon[next]);
                    }
                    count++;
                }
                i = next;
            } while (i != 0);

            // Return true if count is odd, false otherwise 
            return (count % 2 == 1); // Same as (count%2 == 1) 
        }

        /*
    // Driver Code 
    public static void Main(String[] args)
    {
        Location[] polygon1 = {new Location(0, 0),
                            new Location(10, 0),
                            new Location(10, 10),
                            new Location(0, 10)};
        int n = polygon1.Length;
        Location p = new Location(20, 20);
        if (isInside(polygon1, n, p))
        {
            Console.WriteLine("Yes");
        }
        else
        {
            Console.WriteLine("No");
        }
        p = new Location(5, 5);
        if (isInside(polygon1, n, p))
        {
            Console.WriteLine("Yes");
        }
        else
        {
            Console.WriteLine("No");
        }
        Location[] polygon2 = {new Location(0, 0),
                            new Location(5, 5),
                            new Location(5, 0)};
        p = new Location(3, 3);
        n = polygon2.Length;
        if (isInside(polygon2, n, p))
        {
            Console.WriteLine("Yes");
        }
        else
        {
            Console.WriteLine("No");
        }
        p = new Location(5, 1);
        if (isInside(polygon2, n, p))
        {
            Console.WriteLine("Yes");
        }
        else
        {
            Console.WriteLine("No");
        }
        p = new Location(8, 1);
        if (isInside(polygon2, n, p))
        {
            Console.WriteLine("Yes");
        }
        else
        {
            Console.WriteLine("No");
        }
        Location[] polygon3 = {new Location(0, 0),
                            new Location(10, 0),
                            new Location(10, 10),
                            new Location(0, 10)};
        p = new Location(-1, 10);
        n = polygon3.Length;
        if (isInside(polygon3, n, p))
        {
            Console.WriteLine("Yes");
        }
        else
        {
            Console.WriteLine("No");
        }
    }*/
    }
}

// This code is contributed by 29AjayKumar 
