using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace pplot
{


    public class SBSMessage : IDisposable
    {
        public string Msgype;
        public int TransmissionType;
        public string SessionID;
        public string AircraftID;
        public string HexIdent;
        public string FlightID;
        public string DateGenerated;
        public string TimeGenerated;
        public string DateLogged;
        public string TimeLogged;
        public string Callsign;
        public string Altitude;
        public string GroundSpeed;
        public string Track;
        public string Latitude;
        public string Longitude;
        public string VerticalRat;
        public string Squawk;
        public string AlertSquawkChange;
        public string Emergency;
        public string SPIIdent;
        public string IsOnGround;



        public void Dispose()
        {
        }

        public SBSMessage(string[] parts)
        {
            int px = 0;
            Msgype = parts[px++];
            TransmissionType = Int32.Parse(parts[px++]);
            SessionID = parts[px++];
            AircraftID = parts[px++];
            HexIdent = parts[px++];
            FlightID = parts[px++];
            DateGenerated = parts[px++];
            TimeGenerated = parts[px++];
            DateLogged = parts[px++];
            TimeLogged = parts[px++];
            Callsign = parts[px++];
            Altitude = parts[px++];
            GroundSpeed = parts[px++];
            Track = parts[px++];
            Latitude = parts[px++];
            Longitude = parts[px++];
            VerticalRat = parts[px++];
            Squawk = parts[px++];
            AlertSquawkChange = parts[px++];
            Emergency = parts[px++];
            SPIIdent = parts[px++];
            IsOnGround = parts[px++];
        }
    }
}
