/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roz.settools;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.*;
import static roz.rlib.Utils.Fatal;
import static roz.rlib.Utils.IsRootPath;
import static roz.rlib.Utils.isImageFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import roz.rlib.*;

public class iscan extends SimpleFileVisitor<Path>
{
    private final static SLog l = SLog.GetL();

    private int gfiles = 0;
    private int dfiles = 0;
    private int dmfiles = 0;
    private int dnfiles = 0;
    private int rmod = 0;
    private int rnew = 0;
    private int runchf = 0;
    private int runchd = 0;
    private int nfiles = 0;
    private int efiles = 0;
    private long infree=0;
    int foundFiles = 0;
    int newFiles = 0;

    List<INode> dirsToRescan = new LinkedList<INode>();

    public int nThreads = 0;

    private boolean countMode = false;
    private boolean rescanMode = false;

    private long d2p = 0;
    private int f2p = 0;


    private ISet    set;
    private Runtime rt;
    private long intime;


    public FileEncoder encoder;



    public FileVisitResult preVisitDirectory(Path dir,  BasicFileAttributes attrs)
    {

        if ( countMode )
        {
            d2p++;
            return CONTINUE;
        }

        INode npath = null;
        try
        {
            npath = INode.Create(dir.toString(), true, INode.CreatePoint.Absolute);
        }
        catch (ImException e)
        {
            l.severe(e.toString());
            return CONTINUE;
        }
        l.info("SCAN: INDIR: %s " , npath.toString() );

        long currentWalkDirHash = npath.getDirHash();
        dfiles++;
        ISet.IDir jd = set.GetDir(currentWalkDirHash);
        long utime = Utils.utime(attrs.lastModifiedTime());

        if ( rescanMode )
        {
            if ( jd == null )// new
            {
                rnew++;
                dirsToRescan.add(npath);
            }
            else if ( utime > jd.mtime )
            {
                dirsToRescan.add(npath); // modified
                jd.mtime = utime;
                rmod++;
            }
            else // unchanged - need to mark contained filesInDir & images as inUse
            {
                runchd++;
                for(ISet.IFile f : jd.filesInDir)
                {
                    f.inUse = true;
                    runchf++;
                    ISet.IImage i = set.GetImage(f.crc);
                    if ( i != null )
                    {
                        i.inUse = true;
                    }
                }
            }
        }

        if ( jd == null )
        {
            // new directory, just add it.
            dnfiles++;
            l.info("SCAN: ADDDIR: %d ( %s ) " , npath.getDirHash(), npath);
            jd = set.AddDir(npath, utime);
            jd.isNew = true;
        }
        jd.inUse = true;

        return CONTINUE;
    }

    public FileVisitResult visitFile(Path _file, BasicFileAttributes attrs)
    {
        nfiles++;

        if ( rescanMode )
            return CONTINUE;

        if ( countMode )
        {
            f2p++;
            return CONTINUE;
        }

        INode file = null;
        try
        {
            file = INode.Create(_file.toString(), false, INode.CreatePoint.Absolute);
        }
        catch (ImException e)
        {
            l.fatal(e.toString());
            return CONTINUE;
        }
        String name = file.getFileName();

        // if not an image file, not interested
        if ( !roz.rlib.Utils.isImageFile(name) )
            return CONTINUE;

        long currentWalkDirHash = file.getDirHash();
        gfiles++;
        if ( gfiles % 1000 == 0 )
        {
            System.gc();
            pstat("update-");
        }

        if ( rescanMode )
        {
            ISet.IFile f = set.GetFile(currentWalkDirHash, file.getFileName());

            if ( f != null )
            {
                foundFiles++;
                f.inUse = true;
                ISet.IImage i = set.GetImage(f.crc);
                if ( i != null )
                {
                    i.inUse = true;
                    return CONTINUE;
                }
                else
                    l.info("Missing image!");
            }
            newFiles++;
            l.info("NEW File %s", file.toString());

        }
        processFile(file);

        return CONTINUE;
    }

    public FileVisitResult visitFileFailed(Path file, IOException exc)
    {
        efiles++;
        return CONTINUE;
    }


    private void pstat(String s)
    {
        try {
            if (rescanMode) {
                l.warn("Found %d, New %d", foundFiles, newFiles);
                return;
            }
            long endtime = System.currentTimeMillis();
            long endfree = rt.freeMemory();
            long ttime = endtime-intime;
            long faverage = 0;
            long f2go = f2p-gfiles;
            long d2go = d2p-dfiles;
            SimpleDateFormat format1 = new SimpleDateFormat("MM-dd HH:mm");

            if ( gfiles > 0 )
                faverage = ttime/gfiles;
            float rtime = (float)f2go * (float)faverage / 1000f;

            Calendar  now = Calendar.getInstance();
            now.add(Calendar.SECOND, (int)rtime);
            String eta = format1.format(now.getTime());

            l.warn(s);
            l.raw(String.format("Dirs checked   : %d, new %d, mod %d, %d remain", dfiles, dnfiles, dmfiles, d2go));
            l.raw(String.format("Files processed: %d (%d remain)", gfiles, f2go));
            l.raw(String.format("Thumbs created : %d (%d faila) (%d failb) (%d all)", ISet.ri, ISet.raf, ISet.rbf, ISet.rf));
            l.raw(String.format("Elapsed time   : %dms (%.1fs remain eta %s)", ttime, rtime, eta) );
            l.raw(String.format("File average   : %dms", faverage));
            l.raw(String.format("Free mem       : %dMB%n", endfree/1024/1024));
        }
        catch(Exception e)
        {
            l.severe("Error printing stats : " + e.getMessage());
            e.printStackTrace();
        }


    }

    private void processFile(INode file)
    {
        l.info("FILE process %s", file.getFileName() );
        encoder.process(file);
    }

    public void processDir(INode startingDir)
    {
        try
        {
            Files.walkFileTree(startingDir.getPath(), this);
        } catch (IOException ex)
        {
            l.severe("Error:" + ex);
        }
    }

    public void rescan(INode from) throws ImException
    {

        rt = Runtime.getRuntime();

        infree = rt.freeMemory();
        intime = System.currentTimeMillis();

        encoder = new FileEncoder(set);


        rescanMode = true;
        countMode = false;
        l.warn("Start rescan of " + from.getPath() + " counting dirs." );
        processDir(from);
        l.warn("There are %d new dirs, %d modified & %d unchanged. %d filesInDir marked as inuse.",
                rnew, rmod, runchd, runchf  );

        for(INode n : dirsToRescan)
        {
            l.warn("(Re)scan of %s", n.toString() );
            rescanDir(n);
        }

    }


    private void rescanDir(INode n)
    {
        try
        {
            ISet.IDir d = set.GetDir(n.getDirHash());
            if (d == null)
                l.fatal("Expected dir disnt exist");
            l.warn("Process %s", d.toString());
            for (File f : n.getFile().listFiles())
            {
                if ( f.isDirectory() || f.isHidden() )
                    continue;
                if ( !isImageFile(f.getName()) )
                    continue;

                INode fn = INode.Create(f.getPath(), false, INode.CreatePoint.Absolute);
                ISet.IFile jf = set.GetFile(d.dhash, f.getName());
                if (jf == null)
                {
                    l.warn("New file " + f.getName());
                    processFile(fn);
                }
                else
                {
                    l.warn("Existing file " + jf.toString());
                    jf.inUse = true;
                    ISet.IImage ji = set.GetImage(jf.crc);
                    if ( ji == null )
                        l.severe("Extected image not there");
                    else
                        ji.inUse = true;
                }
            }
        }
        catch (roz.rlib.ImException ie)
        {
            l.severe("Ex " + ie.toString() );
        }
    }


    List<String> findTopDirs()
    {
        List<String> allDirs = new LinkedList<String>();
        List<String> keep = new LinkedList<String>();
        for(ISet.IDir d : set.getDirs().values() )
        {
            allDirs.add(d.path);
        }

        for (String d : allDirs)
        {
            Path pp = Paths.get(d).getParent();
            if ( pp == null )
            {
                keep.add(d);
                continue;
            }
            String np = ISet.NormalizePath(pp);
            //l.warn("Dir " + d + " parent " + np);
            if (allDirs.contains(np))
                continue;
            else
                keep.add(d);
        }

        l.warn("Top levels found:-");
        for (String d : keep)
        {
            l.warn(d);
        }

        return keep;
    }


    public void scan(INode dir, Path sset) throws ImException
    {

        set.test12();

        rt = Runtime.getRuntime();
        rt.gc();

        infree = rt.freeMemory();
        intime = System.currentTimeMillis();

        encoder = new FileEncoder(set);

        try
        {
            // Check img directory exists
            if ( !Files.exists(dir.getPath()) )
            {
                throw new ImException(" - SCAN:" + dir + " does not exist.");
            }

            // Set dir - if mod or upd it must exist
            if (!Files.exists(sset))
            {
                l.warn(String.format("SCAN: Creating set %s", sset));
                Files.createDirectory(sset);
            }
            else
                l.warn(String.format("SCAN: Set %s exists.", sset));

            l.warn("Counting new files to be added..");
            countMode = true;
            processDir(dir);
            System.out.printf("Dirs: %s Files: %d%n", d2p, f2p);
            countMode = false;
            processDir(dir);
        }
        catch (Exception e)
        {
            l.severe("scan - caught: " + e );
            e.printStackTrace();

        }

        Utils.Sleep(250);
        l.info("Waiting for threads to finish");
        encoder.stop();
        l.info("All threads finished, Saving set...");

        long bcendfree = rt.freeMemory();

        rt.gc();

        pstat("Final:-");

        set.SaveFC(sset);

    }



    // forth time lucky...

    public ScanSet sset = null;

    public void create4(String scanDir, String setpath, boolean purge, boolean append )
    {
        try
        {
            Path p = Paths.get(scanDir);
            if ( !p.isAbsolute() )
                Fatal("Path " + scanDir + " is not absolute");

            String rootDir = "";
            if ( Utils.isWindows() )
                rootDir = p.getRoot().toString();
            else
                rootDir = "/media/veracrypt1";

            DirHelper.SetRoot(rootDir);

            /*
            l.warn("Root is " + rootDir);

            String tests[] = { "C:\\fred\\bob", "C:/fred/bob", "c:/fred/bob", "/fred/bob" };
            for ( String s :  tests)
            {
                DirHelper dh = new DirHelper(s);

                System.out.printf("Str %s = dh(%d) ip(%s) rp(%s)%n", dh.GetOriginalPath(), dh.GetHash(), dh.GetLocalPath(), dh.GetDiskPath() );
            }
            DirHelper dh = new DirHelper("c:/fred/bob\\img.txt", true);
            System.out.printf("Str %s = dh(%d) ip(%s) rp(%s)%n", dh.GetOriginalPath(), dh.GetHash(), dh.GetLocalPath(), dh.GetDiskPath() );
*/

            Path setp = Paths.get(setpath);
            Path scanp = Paths.get(scanDir);

            sset = new ScanSet(setp);
            sset.Load5();
            sset.Scan(scanp, purge, append);

            sset.Save5();

            System.out.println(sset.GetStats());

        }

        catch (ImException e)
        {
            l.fatal("Error making set:" + e.what);
        }

    }

    public void hotscan5(String scanDir, String setpath)
    {
        try
        {
            Path p = Paths.get(scanDir);
            if ( !p.isAbsolute() )
                Fatal("Path " + scanDir + " is not absolute");

            Path setp = Paths.get(setpath);
            sset = new ScanSet(setp);
            sset.Load5();
            Path scanp = Paths.get(scanDir);
            sset.HotScan(scanp);

            sset.Save();

            System.out.println(sset.GetStats());

        }

        catch (ImException e)
        {
            l.fatal("Error making set:" + e.what);
        }

    }
}
