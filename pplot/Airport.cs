using Microsoft.Maps.MapControl.WPF;
using System;
using System.Collections.Generic;
using System.IO;
using System.Windows.Controls;

namespace pplot
{

    public class Airport
    {
        public string Name = "";
        public string dump1090 = "";
        public List<Runway> runways = new List<Runway>();
        public Boolean useSumulator = false;
        public List<DisplayRunway> displays = new List<DisplayRunway>();
        public SortedDictionary<String, Zone> zones = new SortedDictionary<string, Zone>();

        public class Simulation
        {
            public string name = "";
            public LocationCollection track = new LocationCollection();
            public int tracking = 0;
            public int delay;
            public int timespan;
        }
        public List<Simulation> simTracks = new List<Simulation>();

        public class Zone
        {
            public LocationCollection area = new LocationCollection();

            public string Name { get; internal set; }
            public string VoiceEnter { get; internal set; }
            public string VoiceLeave { get; internal set; }

            internal bool isInside(Plane p)
            {
                return GEO.isInside(area, p.Location);
            }

  
        }
        public class RunwayConfiguration
        {
            //public LocationCollection approach = new LocationCollection();
            public Zone approach;
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
            public Canvas aprCanv;
            public Canvas depCanv;
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
                    line = line.Trim();
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
                    if (parts[0] == "ZONE")  //ZONE,voice,voice,APPROACH16R,-33.930498, 151.172299, -33.930596, 151.171483,-33.844984,151.138692,   -33.842382,151.155086
                    {
                        Zone z = new Zone();
                        z.Name = parts[1];
                        z.VoiceEnter = parts[2];
                        z.VoiceLeave = parts[3];
                        for (int p = 4; p < parts.Length - 1; p += 2)
                        {
                            z.area.Add(new Location(Double.Parse(parts[p]), Double.Parse(parts[p + 1])));
                        }
                        zones.Add(z.Name,z);
                    }
                    if (parts[0] == "APPROACH" )  //APPROACH,ZONENAME
                    {
                        rwc.approach = zones[parts[1]];
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
