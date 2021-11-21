/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 *
 * @author russ
 */
public class KillMon implements Runnable
{
    private int state = 0;
    private final Lock lock = new ReentrantLock();
    private String[] killInside = {"_iexplore"};
    private String[] killOutside = { "_chrom" };
    private final static SLog l = SLog.GetL();
    private String vpnStr;
    public int iter=0;

    public KillMon( )
    {

    }

    public void Stop()
    {
        state = 0; //
        while(state == 0)
            Utils.Sleep(10);
        // all done...
    }

    private void checkState()
    {
        boolean oldState = jarrivefx.connected;
        boolean newState = false;

        try
        {
           // l.warn("Get idents...");

            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets))
            {
                l.info("   ..." + netint.getDisplayName() + ", up=" + netint.isUp() );
                //if ( !netint.getInterfaceAddresses().isEmpty() )
                //    l.fine("        ip=" + netint.getInterfaceAddresses().get(0).getAddress().toString() );

                if ( netint.getDisplayName().contains(vpnStr) &&  netint.isUp() )
                {
                    // l.fine("vpn string found");
                    newState = true;
                    break;
                }
            }
            //l.warn("Got idents...");

        }

        catch (SocketException ex)
        {
            l.severe(ex.getMessage());
        }
        jarrivefx.connected = newState;
        if ( newState != oldState )
        {
            killCheck();
            l.severe("State change, new state=" + ( newState ? "Connected" : "DisConnected") );
        }


    }


    private void killCheck()
    {
        String kills[] = jarrivefx.connected ? killInside : killOutside;

        PInfo p = new PInfo();


        for( String ks : kills )
        {
            p.KillMatching(ks);
        }
    }


    @Override
    public void run()
    {
        String s = Settings.GetStr("killinside", "");
        l.info("KillInside" + s);
        killInside =  s.split(",");
        s = Settings.GetStr("killoutside", "");
        l.info("KillOutside" + s);
        killOutside = s.split(",");
        vpnStr = Settings.GetStr("vpnname","vpn");

        checkState();
        killCheck();


        state = 1;
        while( state > 0 )
        {
            iter++;
            checkState();
            if ( state > 0 )
                killCheck();

            Utils.Sleep(250);
        }
        // state zero by Stop - set to 1 to indicate we are done
        state = 1;
        l.warn("KM Finished");
    }


}
