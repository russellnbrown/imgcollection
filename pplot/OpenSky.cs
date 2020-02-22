using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace pplot
{
    public sealed class OpenSky
    {
        private static OpenSky instance = new OpenSky();
       
        private OpenSky()
        {
            
        }

        public void Init()
        {
            try
            {
                using (StreamReader sr = new StreamReader("data/aircraftDatabase.csv"))
                {
                    char[] seps = { ',' };
                    string line = sr.ReadLine();
                    line = sr.ReadLine();
                    while (line != null && line.Length > 0)
                    {
                        try
                        {
                            string[] parts = line.Split(seps);
                            if (parts.Length == 27 && parts[0].Length > 0)
                            {
                                AircraftInfo ai = new AircraftInfo();
                                ai.Hex = parts[0].ToUpper().Replace("\"", "");
                                ai.Reg = parts[1].ToUpper().Replace("\"", "");
                                ai.Typ = parts[4].ToUpper().Replace("\"", "");
                                ai.Cpy = parts[10].ToUpper().Replace("\"", "");
                                if ( ai.Hex.Length > 0 && !aircraftInfo.ContainsKey(ai.Hex))
                                    aircraftInfo.Add(ai.Hex, ai);
                            }
                        }
                        catch (Exception e)
                        {
                            l.Info(e.Message);
                        }
                        line = sr.ReadLine();
                    }
                }
            }
            catch(Exception e)
            {
                l.Info(e.Message);
            }
            AircraftInfo ax = aircraftInfo["7C1466"];
            l.Info(ax.ToString());
        }

        public static OpenSky Get
        {
            get { return instance; }
        }

        public SortedDictionary<string, AircraftInfo> aircraftInfo = new SortedDictionary<string, AircraftInfo>();
        public class AircraftInfo
        {
            public string Hex = "";
            public string Reg = "";
            public string Typ = "";
            public string Cpy = "";

            public override string ToString()
            {
                return String.Format("AI({0} {1} {2} {3})", Hex, Reg, Typ, Cpy);
            }
        }
    }

}
