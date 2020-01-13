using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Media;

namespace pplot
{
    class SimTrack
    {
        private Thread t;
        private Point pos;
        private Point inc;
        private int duration = 0;
        private int waitms = 0;
        private string id;
        private int nsteps = 1;
        private const int stepDelay = 100;
        public Plane p;

        Point sp;
        Point ep;
        public SimTrack(Plane p, string id, int waitms, int duration,  Point sp, Point ep)
        {
            this.p = p;
            p.HexIdent = id;
            p.Callsign = id;
            this.id = id;
            this.duration = duration;
            this.sp = sp;
            this.ep = ep;
            this.waitms = waitms;
            double dy = ep.Y - sp.Y;
            double dx = ep.X - sp.X;
            nsteps = duration / stepDelay;
            inc.Y = dy / nsteps;
            inc.X = dx / nsteps;
            t = new Thread(new ThreadStart(Run));
            t.Start();
        }

        public void Run()
        {
            Thread.Sleep(waitms);
            int step = 0;
            while (Dump1090Client.running)
            {
                for (pos = sp, step=0; step < nsteps && Dump1090Client.running; pos.X += inc.X, pos.Y += inc.Y, step++)
                {
                    p.Latitude = pos.X;
                    p.Longitude = pos.Y;
                    p.Updated();
                    //l.Info("At {0} {1}", pos.X, pos.Y);
                    System.Threading.Thread.Sleep(stepDelay);
                }
                l.Info("Restart");
            }
        }

        public void Close()
        {
            t.Join();
        }
    }
}
