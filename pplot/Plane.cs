using Microsoft.Maps.MapControl.WPF;
using System;
using System.Collections.Generic;

namespace pplot
{
    public class Plane
    {
        public Plane()
        {
            Updated();
        }

        private string aircraftID;
        private string hexIdent;
        private string flightID;
        private string callsign;
        private int altitude = 0;
        private int track = 0;
       // private double latitude;
       // private double longitude;
        private Location location = new Location();
        private string squawk;
        private string emergency;
        private string isOnGround;
        private DateTime lastUpdate;
        private int updates = 0;
        private string alertSquawkChange;
        private string sPIIdent;
        private string groundSpeed;
        private string verticalRat;
        private string aircraftType;
        private string route;
        private int age = 0;
        private Airport.RunwayConfiguration approaching = null;
        private int approachDistance = 0;

        public string AircraftID { get => aircraftID; set => aircraftID = value; }
        public string HexIdent { get => hexIdent; set => hexIdent = value; }
        public string FlightID { get => flightID; set => flightID = value; }
        public string Callsign { get => callsign; set => callsign = value; }
        public int Altitude { get => altitude; set => altitude = value; }
        public double Latitude { get => location.Latitude; set => location.Latitude = value; }
        public double Longitude { get => location.Longitude; set => location.Longitude = value; }
        public string Squawk { get => squawk; set => squawk = value; }
        public string Emergency { get => emergency; set => emergency = value; }
        public int Track { get => track; set => track = value; }
        public string IsOnGround { get => isOnGround; set => isOnGround = value; }
        public DateTime LastUpdate { get => lastUpdate; }
        public int Age { get => age; }
        public int Updates { get => updates;  }
        public string AlertSquawkChange { get => alertSquawkChange; set => alertSquawkChange = value; }
        public string SPIIdent { get => sPIIdent; set => sPIIdent = value; }
        public string GroundSpeed { get => groundSpeed; set => groundSpeed = value; }
        public string VerticalRat { get => verticalRat; set => verticalRat = value; }
        public string AircraftType { get => aircraftType; set => aircraftType = value; }
        public string Route { get => route; set => route = value; }
        internal Airport.RunwayConfiguration Approaching { get => approaching; set => approaching = value; }
        public int ApproachDistance { get => approachDistance; set => approachDistance = value; }
        public Location Location { get => location; set => location = value; }
        public bool Stale { get; internal set; }
        public List<string> InZones { get => inZones; set => inZones = value; }

        private List<String> inZones = new List<string>();

        public void Updated()
        {
            lastUpdate = DateTime.Now;
            updates++;
        }

        public override string ToString()
        {
            return String.Format("[hex:{0} id:{1} pos:{2}, {3} alt:{4} call:{5} sq:{6}]", HexIdent, AircraftID, Latitude, Longitude, Altitude, Callsign, Squawk);
        }

  
        internal void UpdateFrom(Plane p)
        {
            aircraftID = p.AircraftID;
            hexIdent = p.HexIdent;
            flightID = p.FlightID;
            callsign = p.Callsign;
            altitude = p.Altitude;
            track = p.Track;
            location = p.Location;
            squawk = p.Squawk;
            emergency = p.Emergency;
            isOnGround = p.IsOnGround;
            lastUpdate = p.LastUpdate;
            updates = p.Updates;
            alertSquawkChange = p.AlertSquawkChange;
            sPIIdent = p.SPIIdent;
            groundSpeed = p.GroundSpeed;
            verticalRat = p.VerticalRat;
            aircraftType = p.AircraftType;
            route = p.Route;
            age = (int)((DateTime.Now - p.lastUpdate).TotalSeconds);
        }

        internal void removeZone(string v)
        {
            if ( inZones.Contains(v) )
            {
                inZones.Remove(v);
                ApWin.Get().planeLeavesZone(this, v);
            }
        }

        internal void addZone(string z)
        {
            InZones.Add(z);
            ApWin.Get().planeEntersZone(this, z);
        }

        internal bool isInZone(string z)
        {
            return InZones.Contains(z);
        }

  
    }
}
