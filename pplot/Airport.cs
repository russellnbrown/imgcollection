using Microsoft.Maps.MapControl.WPF;
using System;
using System.Collections.Generic;
using System.IO;
using System.Windows.Controls;

namespace pplot
{

    class Airport
    {
        public string Name = "";
        public string dump1090 = "";
        public List<Runway> runways = new List<Runway>();
        public Boolean useSumulator = false;
        public List<DisplayRunway> displays = new List<DisplayRunway>();

        public class Simulation
        {
            public string name = "";
            public LocationCollection track = new LocationCollection();
            public int tracking = 0;
            public int delay;
            public int timespan;
        }
        public List<Simulation> simTracks = new List<Simulation>();

        public class RunwayConfiguration
        {
            public LocationCollection approach = new LocationCollection();
            public string Name = "";
            public LocationCollection lineups = new LocationCollection();
            public Location takeoff;
        }
        public class Runway
        {
            public LocationCollection layout = new LocationCollection();
            public List<RunwayConfiguration> config = new List<RunwayConfiguration>();
        }

        public class DisplayRunway
        {
            public string Name;
            public Runway runway;
            public RunwayConfiguration config;
            public Canvas map;
            public int MarkDistance;
            public int NumMarks;
        }

        public Airport(string path)
        {
            if (!File.Exists(path))
                throw new Exception("Can't find file " + path);

            using (StreamReader sr = new StreamReader(path))
            {
                string line="";
                char[] seps = { ',' };
                Runway rw=null;
                RunwayConfiguration rwc = null;

                while ((line = sr.ReadLine())!=null)
                {
                    string[] parts = line.Split(seps);
                    if ( parts[0] == "NAME") // NAME,SYD
                        Name = parts[1];
                    if ( parts[0] == "RUNWAY" ) // RUNWAY,1,-33.930533, 151.171892,-33.963676, 151.180480
                    {
                        rw = new Runway();
                        rw.layout.Add(new Location(Double.Parse(parts[1]), Double.Parse(parts[2])));
                        rw.layout.Add(new Location(Double.Parse(parts[3]), Double.Parse(parts[4])));
                        runways.Add(rw);
                    }
                    if ( parts[0] == "CONFIG" ) // CONFIG,16R
                    {
                        rwc = new RunwayConfiguration();
                        rwc.Name = parts[1];
                        rw.config.Add(rwc);
                    }
                    if (parts[0] == "APPROACH" )  //APPROACH,-33.872743, 151.170923,-33.870177, 151.188261,-33.948715, 151.188784,-33.948911, 151.187464
                    {
                        for(int p=1; p<parts.Length-1;p+=2)
                        {
                            rwc.approach.Add(new Location(Double.Parse(parts[p]), Double.Parse(parts[p + 1])));
                        }
                        rwc.approach.Add(rwc.approach[0]);
                    }
                    if (parts[0] == "LINEUP")  // LINEUP,-33.964278, 151.179773
                    {
                        for (int p = 1; p < parts.Length - 1; p += 2)
                        { 
                            rwc.lineups.Add(new Location(Double.Parse(parts[p]), Double.Parse(parts[p + 1])));
                        }
                    }
                    if (parts[0] == "TAKEOFF") //TAKEOFF,-33.963566, 151.180449
                    {
                        for (int p = 1; p < parts.Length - 1; p += 2)
                        {
                            rwc.takeoff = new Location(Double.Parse(parts[p]), Double.Parse(parts[p + 1]));
                        }
                    }
                    if (parts[0] == "DUMP1090") //DUMP1090,192.168.20.1
                    {
                        dump1090 = parts[1];
                    }

                    if (parts[0] == "DISPLAYRUNWAY") //DISPLAYRUNWAY,16R
                    {
                        DisplayRunway dr = new DisplayRunway();
                        dr.Name = parts[1];
                        dr.MarkDistance = Int32.Parse(parts[2]);
                        dr.NumMarks = Int32.Parse(parts[3]);
                        displays.Add(dr);
                    }

                    if (parts[0] == "SIMULATOR") //SIMULATOR,ON
                    {
                        useSumulator = Boolean.Parse(parts[1]);
                    }
                    if (parts[0] == "SIM") //SIMULATOR,ON
                    {
                        Simulation s = new Simulation();
                        s.name = parts[1];
                        s.tracking = Int32.Parse(parts[2]);
                        s.delay = Int32.Parse(parts[3]);
                        s.timespan = Int32.Parse(parts[4]);
                        s.track.Add(new Location(Double.Parse(parts[5]), Double.Parse(parts[6])));
                        s.track.Add(new Location(Double.Parse(parts[7]), Double.Parse(parts[8])));
                        simTracks.Add(s);
                    }
                }
            }
            foreach(DisplayRunway dr in displays)
            {
                foreach(Runway r in runways)
                {
                    foreach (RunwayConfiguration c in r.config)
                    {
                        if (c.Name == dr.Name)
                        {
                            dr.config = c;
                            dr.runway = r;
                        }
                    }
                }
            }
 
        }
    }
}
