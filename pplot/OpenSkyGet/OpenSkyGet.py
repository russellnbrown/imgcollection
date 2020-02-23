
from opensky_api import OpenSkyApi
import time

TEST_USERNAME = "russinoz"
TEST_PASSWORD = ".G1lxyAdanoids1."
TEST_SERIAL = [-1408234645]
SYDNEYCLOSEBB=(-34.069497,-33.689838, 150.987340, 151.472798 )

api =  OpenSkyApi(TEST_USERNAME, TEST_PASSWORD)
out = open("opensky.csv","w")

def receive():
    print("Begin query")
    states = api.get_states(bbox=SYDNEYCLOSEBB)
    if states != None:
        #states = api.get_my_states()
        print("Received query")
        for s in states.states:
            print("(%r,%r,%r,%r,%r,%r,%f)" % (s.icao24, s.longitude, s.latitude, s.baro_altitude, s.velocity, s.callsign,s.heading))
            out.write("{0},{1},{2},{3},{4},{5},{6}\n".format(s.icao24, s.longitude, s.latitude, s.baro_altitude, s.velocity, s.callsign,s.heading))
            out.flush()
        print("Done query")

def main():    
    
    while True:
        receive()
        time.sleep(6)

if __name__ == '__main__':
    main()
    

#s = api.get_states(bbox=( -34.069497,-33.689838, 150.987340, 151.472798  ))
#s = api.get_states(bbox=SYDNEYCLOSEBB)
#s = api.get_my_states()
