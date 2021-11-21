/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roz.settools;

import roz.rlib.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;

public class SSServer
{
    private final static SLog l = SLog.GetL();
    private ServerSocket servSocket;
    private Socket fromClientSocket;
    private int cTosPortNumber = 1777;

    public SSServer()
    {
    }

    private ScanSet set=null;

    public void serve(String setpath )
    {
        try
        {

            Path setPath = Paths.get(setpath);
            if ( !ScanSet.Valid5(setPath) )
            {
                return;
            }

            try
            {
                set = new ScanSet(setPath);
                set.Load5();
            }
            catch (ImException e)
            {
                e.printStackTrace();
                return;
            }

            l.warn("Loaded %s, Start server" , setpath);

            servSocket = new ServerSocket(cTosPortNumber);
            l.warn("Waiting for a connection on " + cTosPortNumber);

            while(true)
            {

                fromClientSocket = servSocket.accept();
                l.warn("Accepted a connection");

                try
                {
                    ObjectOutputStream oos = new ObjectOutputStream(fromClientSocket.getOutputStream());
                    ObjectInputStream ois = new ObjectInputStream(fromClientSocket.getInputStream());

                    Object comp = null;
                    while ((comp = (Object) ois.readObject()) != null)
                    {
                        if (comp instanceof TxQuery)
                        {
                            TxQueryReply rep = handleQuery((TxQuery) comp);
                            l.warn("Send REPLY %s", rep);
                            oos.writeObject(rep);
                        } else
                        {
                            l.warn("Got: Unknown Object");
                        }
                    }
                    oos.close();
                    fromClientSocket.close();
                }
                catch(SocketException se)
                {
                    l.warn("Closed");
                }

            }


        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    private TxQueryReply handleQuery(TxQuery qry)
    {

        if ( qry.searchType > 0 )
            return handleComplexQuery(qry);

        ScanSet.SSCRC crc = ScanSet.GetCRCAndBytes(qry.fileName);
        List<ScanSet.SSFile> files = set.GetSSFilesForCrc(crc.crc);
        LinkedList<String> fset = new LinkedList<String>();
        for( ScanSet.SSFile f : files )
        {
            fset.add(f.GetDiskPath());
        }
        return new TxQueryReply(1, files.size(), "Search OK", fset);
    }

    private TxQueryReply handleComplexQuery(TxQuery qry)
    {
        l.warn("HANDLE QUERY " + qry);
        ScanSet.SSSearch srch = set.CreateSearch();
        ScanSet.SSSearch.SSSearchImageTask tt = srch.createImageSearch();
        tt.setImageSearch(ScanSet.SSScanType.Luma, qry.fileName);
        tt.run(); // run in fgd

        TxQueryReply rv = new TxQueryReply(1, tt.results.size(), "Search OK", null);
        rv.crcs =  new LinkedList<Long>();
        rv.thumbs =  new LinkedList<int[]>();

        for( ScanSet.SSSearch.SSSearchResult f : tt.results )
        {
            ScanSet.SSSearch.SSImageSearchResult i = ( ScanSet.SSSearch.SSImageSearchResult )f;
            rv.crcs.add((Long)i.image.crc);
            rv.thumbs.add(i.image.thumb);
        }

        return rv;

    }

/*
    public boolean convert45(String set4, String set5)
    {
        Path setPath = Paths.get(set4);
        if ( !ScanSet.Valid4(setPath) ) {
            return false;
        }


        try
        {
            set = new ScanSet(setPath);
            set.Load4();
            set.SetName(set5);
            set.Save5();
            return true;
        }
        catch (ImException e)
        {
            e.printStackTrace();
            return false;
        }
    }*/
}
