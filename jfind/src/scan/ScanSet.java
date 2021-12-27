/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package scan;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import jutils.Logger;

/**
 *
 * @author russ
 */
public class ScanSet 
{

    public final ConcurrentHashMap<Long, ImgCollectionDirItem> dirs = new ConcurrentHashMap<>();
    public final ConcurrentLinkedQueue<ImgCollectionFileItem> files = new ConcurrentLinkedQueue<>();
    public final ConcurrentHashMap<Long, ImgCollectionImageItem> images = new ConcurrentHashMap<>();
    public String top;

    public List<String> matchingDirs(String srch, int limit)
    {
        List<String> l = new LinkedList<String>();
        
        for(ImgCollectionDirItem i : dirs.values())
        {
            if ( org.apache.commons.lang3.StringUtils.containsIgnoreCase(i.getDir(),srch))
                    l.add(i.getDir());
            if ( l.size() == limit )
                break;
        }
        return l;
    }
    
    public ScanSet(String rootPath) 
    {
        Path s = Paths.get(rootPath);
        
        if ( !Files.exists(s))
            Logger.Fatal("No Set Found");
        
        Logger.Info("Opening %s", rootPath);

        int lc=0;
        int f=0;
        
        try
        {           
            Path pdir = s.resolve("dirs.txt");
            Path pfile = s.resolve("files.txt");
            Path puniq = s.resolve("images.bin");

            Scanner fdirs;
            Scanner ffiles;
            FileInputStream fin;
            FileChannel funiq;

            fin = new FileInputStream(puniq.toString());
            funiq = fin.getChannel();
            fdirs = new Scanner(pdir);
            ffiles = new Scanner(pfile);

            String line;
            line = fdirs.nextLine();
            top = line;

            while (fdirs.hasNext())
            {
                line = fdirs.nextLine();
                f=1;
                lc++;
                try
                {
                    String []parts = line.split("\\|");

                    ImgCollectionDirItem sid = new ImgCollectionDirItem();
                    sid.setDir(parts[1]);
                    sid.setHash(Long.parseLong(parts[0]));
                    sid.setLmod(Long.parseLong(parts[2]));
                    
                    dirs.put(sid.getHash(), sid);
                } catch (NumberFormatException nfe)
                {
                    Logger.Fatal("Number ex reading dirs" + nfe.getMessage() + line);
                }
            }

            f=2;
            lc=0;
            long fpos=0;
            try
            {
                while( ffiles.hasNext() )
                {
                    line=ffiles.nextLine();
                    f=2;
                    lc++;
                    String []fparts = line.split(",");

                    
                    try
                    {
                        long dhash = Long.parseLong(fparts[0]);
                        long crc = Long.parseLong(fparts[1]);
                        ImgCollectionFileItem sif = new ImgCollectionFileItem();
                        sif.setIHash(crc);
                        sif.setDhash(dhash);
                        sif.setFile(fparts[2]);
                        files.add(sif);
                    } catch (NumberFormatException nfe)
                    {
                        Logger.Severe("NUmber ex reading files" + nfe.getMessage() + line);
                    }
                    catch (Exception nfe)
                    {
                        Logger.Severe("NUmber ex reading files" + nfe.getMessage() + line);
                    }

                    //line = ffiles.readLine();

                }
            }
            catch(Exception e) // sometimes filename have unprintable characters - ignore them
            {
                Logger.Fatal("Bad readline, malformed");
                return;
            }     

            lc=0;
            f=3;
            ByteBuffer ibuf = ByteBuffer.allocateDirect(8 + 3 * 16 * 16);
            ibuf.order(ByteOrder.LITTLE_ENDIAN);

            try
            {
                while (fin.available() > 0)
                {
                    lc++;
                    ibuf.rewind();
                    funiq.read(ibuf);
                    ibuf.rewind();
                    long crc = ibuf.getLong();
                    byte[] buf = new byte[16 * 16 * 3];
                    ibuf.get(buf);
                    ImgCollectionImageItem sii = new ImgCollectionImageItem();
                    sii.setCrc(crc);
                    sii.setThumb(buf);
                    images.put(crc, sii);
                }
            } catch (IOException e)
            {

            }
            fdirs.close();
            ffiles.close();
            funiq.close();
            fin.close();

        } catch (IOException ioe)
        {
            ioe.printStackTrace();
            Logger.Severe(String.format("Caught IOE : %s in save lc=%d, f=%d", ioe.toString(), lc, f));
        }
        Logger.Info(String.format("Set has %d dirs, %d files and %d images.", dirs.size(),files.size(),images.size() ));
    }
    
}
