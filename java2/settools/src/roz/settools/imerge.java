/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roz.settools;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import roz.rlib.ISet;
import roz.rlib.ISet.*;
import roz.rlib.ImException;
import roz.rlib.SLog;

public class imerge
{
    private final static SLog l = SLog.GetL();

    public imerge()
    {
    }




    public void merge(String args[]) throws ImException
    {
        try
        {
            Path newset = Paths.get(args[1]);
            List<Path> merged = new LinkedList<Path>();

            if ( Files.exists(newset) )
                l.fatal("Output set exixts!");
            else
                Files.createDirectory(newset);

            Path sdir = newset.resolve("directory3.txt");
            Path sfile = newset.resolve("files3.txt");
            Path suniq = newset.resolve("uniq3.bin");

            BufferedWriter fdirs;
            BufferedWriter ffiles;
            FileOutputStream  fout;
            FileChannel funiq;

            fdirs = Files.newBufferedWriter(sdir,  StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            ffiles = Files.newBufferedWriter(sfile, StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            fout = new FileOutputStream(suniq.toString()) ;
            funiq = fout.getChannel();


            String relDir = "";

            for(int c=2; c<args.length; c++)
            {
                Path p = Paths.get(args[c]);
                if ( !Files.exists(p) )
                {
                    l.severe("Set " + p + " does not exist");
                    return;
                }

                Path pdir = p.resolve("directory3.txt");
                Path pfile = p.resolve("files3.txt");
                Path puniq = p.resolve("uniq3.bin");

                BufferedReader fidirs;
                BufferedReader fifiles;
                FileInputStream  fin;
                FileChannel fiuniq;

                fin = new FileInputStream(puniq.toString());
                fiuniq =  fin.getChannel();
                fidirs = Files.newBufferedReader(pdir);
                fifiles = Files.newBufferedReader(pfile);

                String line;
                line = fidirs.readLine();
                String rootStr = line;

                if ( relDir == "" )
                {
                    relDir = rootStr;
                    fdirs.write(relDir);
                    fdirs.newLine();
                }
                else
                {
                    if ( !relDir.equals(rootStr))
                        l.fatal("Different roots %s %s in %s", relDir, rootStr, p.toString() );
                }

                while ((line = fidirs.readLine()) != null)
                {
                    fdirs.write(line);
                    fdirs.newLine();
                }
                while ((line = fifiles.readLine()) != null)
                {
                    ffiles.write(line);
                    ffiles.newLine();
                }

                ByteBuffer ibuf = ByteBuffer.allocateDirect(8+4*16*16);
                try
                {
                    while(fin.available()>0)
                    {
                        ibuf.rewind();
                        fiuniq.read(ibuf);
                        ibuf.rewind();
                        funiq.write(ibuf);
                    }
                }
                catch(IOException e)
                {

                }

                fiuniq.close();
                fin.close();
                fidirs.close();
                fifiles.close();

            }

            funiq.close();
            fout.close();
            fdirs.close();
            ffiles.close();


        }
        catch (IOException ex)
        {
            l.severe("IOEX in merge:" + ex.toString());
            ex.getStackTrace();
        }
    }

}
