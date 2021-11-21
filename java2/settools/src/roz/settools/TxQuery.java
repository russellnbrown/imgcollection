package roz.settools;

import roz.rlib.SLog;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;


public class TxQuery extends TxObject implements java.io.Serializable, Runnable
{
    public String fileName;
    public Integer searchType;

    private transient static SLog l = SLog.GetL();
    private transient int cTosPortNumber = 1777;
    private transient Socket sock;
    private transient ObjectInputStream ois = null;
    private transient ObjectOutputStream oos = null;
    private transient TxQueryReply reply=null;
    private transient boolean finished = false;
    private transient boolean connected = false;

    public TxQuery(String path, int type)
    {
        super();
        fileName = path;
        connected = false;
        searchType = type;

        try
        {
            sock = new Socket(InetAddress.getLocalHost(), cTosPortNumber);
            ois = new ObjectInputStream(sock.getInputStream());
            oos = new ObjectOutputStream(sock.getOutputStream());
            l.warn("QRYMAIN - create for " + fileName);
            connected = true;
            Thread t = new Thread(this);
            t.start();
        }
        catch (ConnectException e)
        {
            l.warn("No Server running");
            finished = true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            finished = true;
        }
    }


    public void run()
    {
        try
        {
            l.warn("QRYTRD - send for " + fileName);
            oos.writeObject(this);
            reply = (TxQueryReply) ois.readObject();
            l.warn("QRYMAIN - got reply for " + fileName);
            finished = true;
        }
        catch (IOException e)
        {
            l.warn("QRYMAIN - error for " + e.toString());
            finished = true;
        }
        catch (ClassNotFoundException e)
        {
            l.warn("QRYMAIN - error for " + e.toString());
            finished = true;
        }

    }

    public TxQueryReply waitreply()
    {
        l.warn("QRYMAIN - wait...");
        while(!finished)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
            }
        }
        l.warn("QRYMAIN - retrn reply " + ( reply==null?"NoResponse":reply.toString()) );
        return reply;
    }

    @Override
    public String toString()
    {
        return "TxQuery{" +
                "fileName='" + fileName + '\'' +
                '}';
    }
}




