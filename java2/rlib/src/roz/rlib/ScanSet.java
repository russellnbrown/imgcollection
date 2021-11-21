package roz.rlib;

import com.twelvemonkeys.image.ResampleOp;
import javafx.scene.image.*;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static java.lang.Math.abs;
import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * Created by russellb on 3/08/2016.
 */
public class ScanSet extends SimpleFileVisitor<Path> implements java.io.Serializable
{
    private SSFileEncoder encoder = null;
    private int ri = 0;
    private int raf = 0;
    private int rf=0;
    private int rbf=0;
    private long intime=0;
    private Runtime rt;
    private Path homeDir;
    private Map<Long, SSDir> dirs = new HashMap<Long, SSDir>();
    private List<SSFile> files = new LinkedList<SSFile>();
    private Map<Long, SSInstances> instances = new HashMap<Long, SSInstances>();
    private Map<Long, SSImage> images = new HashMap<Long, SSImage>();
    private roz.rlib.RTimer loadTimer = new roz.rlib.RTimer();
    private roz.rlib.RTimer saveTimer = new roz.rlib.RTimer();
    private roz.rlib.RTimer scanTimer = new roz.rlib.RTimer();
    private roz.rlib.RTimer intervalTimer = new roz.rlib.RTimer();


    private final static SLog l = SLog.GetL();
    public String GetName()
    {
        return homeDir.toString();
    }
    public void SetName(String newname)
    {
        homeDir = Paths.get(newname);
        try
        {
            // If it dosn't exist, create it.
            if (!Files.exists(homeDir))
            {
                l.warn(String.format("SCAN: Creating set %s", homeDir));
                Files.createDirectory(homeDir);
            }
            else
                l.warn(String.format("SCAN: Set %s exists.",homeDir));
        }
        catch (Exception e)
        {
            l.severe("scan - caught: " + e );
            e.printStackTrace();
            l.fatal("Stopping");
        }
        markAllUsed();
    }

    public String GetStats()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%nScanSet %s%n" , homeDir.toString()));
        sb.append(String.format(" Sizes: Dirs %d, Files %d, Imgs %d, Instances %d%n", dirs.size(), files.size(), images.size(), instances.size()));
        sb.append(String.format(" Times: Load %s, Save %s, Scan %s%n", loadTimer , saveTimer , scanTimer  ));
        sb.append(String.format("%n"));
        sb.append(String.format("%n"));

        return sb.toString();
    }

    public void Rate(SSImage ifi, int lvl)
    {
        ifi.SetRating(lvl);
        SaveImg5(ifi);
    }

    public void Rate(SSFile ifi, int lvl)
    {
        SSImage i = GetImage(ifi.crc);
        i.SetRating(lvl);
        SaveImg5(i);
    }

    public void Rate(File ifi, int lvl)
    {
        ScanSet.SSCRC crc = GetCRCAndBytes(ifi.getPath());
        SSImage i = GetImage(crc.crc);
        if ( i == null )
        {
            Utils.whoops("Not in Set", "Cant' find this in set", ifi.getName() );
            return;
        }
        i.SetRating(lvl);
        SaveImg5(i);

    }



    public class SSDir
    {
        public DirHelper dh;
        public long mtime;
        public boolean inUse = false;
        public List<SSFile> filesInDir = null;

        public String GetDiskPath()
        {
            return dh.GetDiskPath();
        }
        public String GetLocalPath()
        {
            return dh.GetLocalPath();
        }

        public SSDir(String path, long mtime)
        {
            this.dh = new DirHelper(path);
            this.mtime = mtime;
            filesInDir = new LinkedList<SSFile>();
        }

        @Override
        public String toString()
        {
            return "SSDir{" +
                    "GetHash=" + dh.GetHash() +
                    ", GetLocalPath='" + dh.GetLocalPath() + '\'' +
                    ", mtime=" + mtime +
                    (inUse?",InUse":",NotInUse") +
                    '}';
        }
    }

    public class SSFile
    {
        private long     dhash;
        private long     crc;
        private String   name;
        public boolean  inUse = false;


        public javafx.scene.image.Image GetThumbnail()
        {
            SSImage ji = GetImage(crc);
            return ji.GetThumbnail();
        }

        public String GetLocalPath()
        {
            SSDir d = GetDir(dhash);
            return d.dh.GetLocalPath() + "/" + name;
        }

        public String GetUrlPath() throws ImException
        {
            File fu = new File(GetDiskPath());
            try
            {
                return fu.toURI().toURL().toString();
            }
            catch (MalformedURLException e)
            {
                throw new ImException("Cant get url for " + GetDiskPath() );
            }
        }

        public String GetDiskPath()
        {
            SSDir d = GetDir(dhash);
            String dpath = d.dh.GetDiskPath();
            String path = dpath + "/" + name;
            return path;
        }

        public String GetName() { return name; }
        public long GetDirHash()
        {
            return dhash;
        }

        public SSFile(long dhash, String name, long crc)
        {
            this.dhash = dhash;
            this.name = name;
            this.crc = crc;
        }

        @Override
        public String toString()
        {
            return "SSFile{" +
                    "fhash=" + dhash +
                    ", crc=" + crc +
                    ", name='" + name + '\'' +
                    (inUse?",InUse":",NotInUse") +
                    '}';
        }
    }


    public class SSImage //implements Serializable
    {
        public SSImage(long crc, int thumb[], int rate, int user1, long pos)
        {
            this.crc = crc;
            this.rating = rate;
            this.user1 = user1;
            this.pos = pos;
            this.thumb = thumb.clone();
        }

        public javafx.scene.image.Image GetThumbnail()
        {

            WritableImage wImage = new WritableImage(16,16);
            PixelWriter pixelWriter = wImage.getPixelWriter();
            pixelWriter.setPixels(0,0,16,16, PixelFormat.getIntArgbInstance(),thumb,0, 16 );

            return wImage;
        }

       // private static final long serialVersionUID = 1L;
        public long     pos;
        public long     crc;
        public int      thumb[];
        public boolean  inUse = false;
        public boolean  isNew = false;
        private int     rating = 0;
        private int     user1 = 0;

        public void SetRating(int l) { rating = l;}
        public int GetRating() { return rating; }
        public void SetUser1(int l) { user1 = l;}
        public int GetUser1() { return user1; }

        @Override
        public String toString()
        {
            return "SSImage{" +
                    "crc=" + crc +
                    ", thumb=" + Arrays.toString(thumb) +
                    (isNew?",New":",NotNew") +
                    (inUse?",InUse":",NotInUse") +
                    '}';
        }
    }



    public class SSInstances
    {
        public long crc;
        public List<SSFile> instances;

        public SSInstances(long crc) {
            this.crc = crc;
            instances = new LinkedList<SSFile>();
        }
    }

    public List<SSFile> GetSSFilesForCrc(long crc)
    {
        SSInstances inst = instances.get(crc);
        List<SSFile> files = new LinkedList<SSFile>();
        return inst.instances;
    }

    public static boolean Valid4(Path s)
    {
        Path proot = s.resolve("root4.txt");
        Path pdir = s.resolve("dirs.txt");
        Path pfile = s.resolve("files4.txt");
        Path puniq = s.resolve("uniq4.bin");

        if ( !proot.toFile().exists() )
            return false;
        if ( !pdir.toFile().exists() )
            return false;
        if ( !pfile.toFile().exists() )
            return false;
        if ( !puniq.toFile().exists() )
            return false;

        return true;
    }
 
    public static boolean Valid6(Path s)
    {
        Path pdir = s.resolve("dirs.txt");
        Path pfile = s.resolve("files.txt");
        Path puniq = s.resolve("images.bin");

        if ( !pdir.toFile().exists() )
            return false;
        if ( !pfile.toFile().exists() )
            return false;
        if ( !puniq.toFile().exists() )
            return false;

        return true;
    }
        
    public static boolean Valid5(Path s)
    {
        Path proot = s.resolve("root5.txt");
        Path pdir = s.resolve("directory5.txt");
        Path pfile = s.resolve("files5.txt");
        Path puniq = s.resolve("uniq5.bin");

        if ( !proot.toFile().exists() )
            return false;
        if ( !pdir.toFile().exists() )
            return false;
        if ( !pfile.toFile().exists() )
            return false;
        if ( !puniq.toFile().exists() )
            return false;

        return true;
    }
    // Create scanset.
    public ScanSet(Path p) throws ImException
    {
        homeDir = p;
        try
        {
            // If it dosn't exist, create it.
            if (!Files.exists(p))
            {
                l.warn(String.format("SCAN: Creating set %s", p));
                Files.createDirectory(p);
            }
            else
                l.warn(String.format("SCAN: Set %s exists.", p));
        }
        catch (Exception e)
        {
            l.severe("scan - caught: " + e );
            e.printStackTrace();
            l.fatal("Stopping");
        }


    }

    int ssdmarked=0;
    int ssfmarked=0;
    int ssimarked=0;

    // Mark everything in scope as unused. Things not in scope ( outside given GetLocalPath )
    // are assumed to be inUse.
    private void markAllNotUsed(Path scanp)
    {
        DirHelper db = new DirHelper(scanp.toString());

        // Mark all files & images as not used
        synchronized (files)
        {
            for (SSFile f : files)
            {
                f.inUse = false;
                ssfmarked++;
            }
        }

        synchronized (images)
        {
            for (SSImage i : images.values())
            {
                i.inUse = false;
                ssimarked++;
            }
        }

        // With directories, see if they are in scope. If not, mark it used along with all its files
        // and images used by those files.
        synchronized (dirs)
        {
            for (SSDir d : dirs.values())
            {
                // if not in scan GetLocalPath its outside scope, assume used along with everything in dir
                if (!d.dh.GetLocalPath().startsWith(db.GetLocalPath()))
                {
                    l.warn("SCAN: MARK: dir %s outside scope %s, mark USED", d.dh.GetLocalPath(), db.GetLocalPath() );
                    d.inUse = true;
                    for (SSFile f : d.filesInDir)
                    {
                        f.inUse = true;
                        GetImage(f.crc).inUse = true;
                    }
                }
                else
                {
                    // Inscope, mark unused
                    d.inUse = false;
                    l.warn("SCAN: MARK: dir %s inside scope %s, leave UNUSED", d.dh.GetLocalPath(), db.GetLocalPath());
                }
            }
        }
    }


    private void pstat()
    {
        try {

            long endtime = System.currentTimeMillis();
            long endfree = rt.freeMemory();
            long ttime = endtime-intime;
            long faverage = 0;
            long f2go = f2p-nfiles;
            long d2go = d2p-dfiles;
            SimpleDateFormat format1 = new SimpleDateFormat("MM-dd HH:mm");

            if ( nfiles > 0 )
                faverage = ttime/nfiles;
            float rtime = (float)f2go * (float)faverage / 1000f;

            Calendar  now = Calendar.getInstance();
            now.add(Calendar.SECOND, (int)rtime);
            String eta = format1.format(now.getTime());


            l.raw(String.format("Dirs checked   : %d (%d remain)", dfiles, d2go));
            l.raw(String.format("Files processed: %d (%d remain) existing:%d", nfiles, f2go, foundFiles));
            if ( rescanMode )
                l.raw(String.format("New Files: %d New Dirs: %d)", newFiles, ndirs));
            l.raw(String.format("Elapsed time   : %dms (%.1fs remain eta %s)", ttime, rtime, eta) );
        }
        catch(Exception e)
        {
            l.severe("Error printing stats : " + e.getMessage());
            e.printStackTrace();
        }


    }

    // Mark everything in scope as unused. Things not in scope ( outside given GetLocalPath )
    // are assumed to be inUse.
    private void markAllUsed()
    {

        // Mark all files & images as not used
        synchronized (files)
        {
            for (SSFile f : files)
            {
                f.inUse = true;
                ssfmarked++;
            }
        }

        synchronized (images)
        {
            for (SSImage i : images.values())
            {
                i.inUse = true;
                ssimarked++;
            }
        }

        synchronized (dirs)
        {
            for (SSDir d : dirs.values())
            {
                    d.inUse = true;
                    ssdmarked++;
            }
        }
    }

    // Class to hold crc & data of a file.
    public static class SSCRC
    {
        public SSCRC()
        {
        }
        public long crc;
        public byte bytes[];
    }

    // Get bytes of an img & calc crc
    public static SSCRC GetCRCAndBytes(String xfile)
    {
        try
        {
            SSCRC cr = new SSCRC();

            File file = new File(xfile);
            long filelength = file.length();
            cr.bytes = new byte[(int)filelength];
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(cr.bytes);
            dis.close();

            cr.crc = Utils.BHash(cr.bytes);
            return cr;
        }

        catch (Exception ex)
        {
            l.severe("GetCRCAndBytes : error processing " + xfile.toString() + " : " + ex.getMessage());
        }

        return null;
    }

    // Use file bytes to make a TN
    public int[] GetTnFromFileBytes(byte []fileBytes)
    {

        l.info("GetTnFromFileBytes processing "  );

        BufferedImage image;
        try
        {
            InputStream in = new ByteArrayInputStream(fileBytes);
            image  = ImageIO.read(in);
            if ( image == null )
            {
                l.info("GetTN No image returned ");
                return null;
            }

            BufferedImage thumbi=Resize(image, new Dimension(16,16));
            if ( thumbi == null )
            {
                l.info("TNI is NULL");
                return null;
            }

            int rgb[] = thumbi.getRGB(0, 0, 16, 16, null, 0, 16);
            if ( rgb.length == 256 )
                return rgb;
            //thumbi.getRGB(0, 0, 16, 16, rgb, 0, 16);

            return null;
        }
        catch (IOException ex)
        {
            l.info("GetTnFromFileBytes2 IO error " + ex.toString());
        }
        catch ( Exception e)
        {
            l.info("GetTnFromFileBytes2 other error " + e.toString());
        }
        catch ( OutOfMemoryError e)
        {
            rt.gc();
            l.info("GetTnFromFileBytes2 OutOfMemoryError error " + e.toString());
        }

        return null;
    }



    public  BufferedImage Resize(BufferedImage image, Dimension d)
    {

        com.twelvemonkeys.image.ResampleOp resampleOp = new com.twelvemonkeys.image.ResampleOp(16, 16, ResampleOp.FILTER_CUBIC);
        BufferedImage thumbi=null;
        //resampleOp = new ResampleOp (d.width, d.height);

        ri++;

        try {
            thumbi = resampleOp.filter(image, null);
        }
        catch(Exception e)
        {
            raf++;
            return ResizeInt(image, d);
        }

        if ( thumbi == null )
        {
            rf++;
            return null;
        }
        return thumbi;
    }

    public BufferedImage ResizeInt(BufferedImage image, Dimension d)
    {
        BufferedImage thumbi=null;

        try
        {
            thumbi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = thumbi.createGraphics();
            g.drawImage(image, 0, 0, 16, 16, 0, 0, image.getWidth(), image.getHeight(), null);
            g.dispose();
            if ( thumbi == null )
                rbf++;
            return thumbi;
        }
        catch(Exception e)
        {
            rbf++;
            l.severe("Backup failed, image=" + image.toString() );
            return null;
        }
    }

    private String rootDirStr="";
    private String topDirStr="";

    public String GetRoot()
    {
        return rootDirStr;
    }

    public String GetTopDir()
    {
        return topDirStr;
    }

    public void Load4() throws ImException
    {
        try
        {
            loadTimer.reset();
            roz.rlib.RTimer tt = new roz.rlib.RTimer();
            tt.reset();

            Path rdir = homeDir.resolve("root4.txt");
            Path pdir = homeDir.resolve("directory4.txt");
            Path pfile = homeDir.resolve("files4.txt");
            Path puniq = homeDir.resolve("uniq4.bin");

            rootDirStr = rdir.toString();

            if ( !Files.exists(rdir) )
            {
                l.warn("LOAD: NONEXISTANT: Set " + homeDir.toString() + " does not exist");
                return;
            }
            l.warn("LOAD: LOADING: Set " + homeDir.toString() );

            BufferedReader froot;
            BufferedReader fdirs;
            BufferedReader ffiles;
            FileInputStream  fin;
            FileChannel funiq;

            fin = new FileInputStream(puniq.toString());
            funiq =  fin.getChannel();

            froot = Files.newBufferedReader(rdir);
            fdirs = Files.newBufferedReader(pdir);
            ffiles = Files.newBufferedReader(pfile);

            String line;
            line = froot.readLine();
            l.warn("LOAD: LOADING: ROOT: " + line );
            if ( DirHelper.GetRoot().isEmpty() )
                DirHelper.SetRoot(line);

            if ( !DirHelper.GetRoot().equals(line) )
                throw new ImException("Roots dont match: " + DirHelper.GetRoot() + " - " + line );

            while ((line = fdirs.readLine()) != null)
            {
                int cpos1 = line.indexOf((int)',');
                int cpos2 = line.indexOf((int)',', cpos1+1);

                String sdhash = line.substring(0, cpos1);
                String stime = line.substring(cpos1+1,cpos2);
                String spath = "";
                if ( line.length() > cpos2+1 )
                    spath = line.substring(cpos2+1);

                try
                {
                    Long t = Long.parseLong(stime);
                    Long d = Long.parseLong(sdhash);
                    SSDir jd = new SSDir(spath, t);
                   // if ( withQFile )
                   //     jd.filesInDir = new LinkedList<IFile>();

                    l.debug("LOAD: LOADING: DIR: hash=%d calchash=%d GetLocalPath=%s", d, jd.dh.GetHash(), spath );

                    if ( jd.dh.GetHash() != d ) {
                        //    throw new ImException("DHASH Mismatch on " + spath);
                        l.severe("DHASH Mismatch on " + spath);
                        jd.dh.SetHash(d);
                    }

                    PutDir(jd.dh.GetHash(), jd);
                }
                catch(NumberFormatException nfe)
                {
                    l.fatal("Number ex reading dirs" + nfe.getMessage() + line );
                }
            }

            tt.stop();
            l.debug("Dirs  : " + dirs.size() + " in: " + tt );
            tt.reset();
            int lc=0;
            while ((line = ffiles.readLine()) != null) {
                try {
                    int cpos1 = line.indexOf((int) ',');
                    int cpos2 = line.indexOf((int) ',', cpos1 + 1);
                    String sfhash = line.substring(0, cpos1);
                    String scrc = line.substring(cpos1 + 1, cpos2);
                    String sname = line.substring(cpos2 + 1);

                    long dhash = Long.parseLong(sfhash);
                    long crc = Long.parseLong(scrc);
                    SSFile jf = new SSFile(dhash, sname, crc);
                    l.debug("LOAD: LOADING: FILE: hash=%d name=%s cec=%d", dhash, sname, crc);

                    lc++;
                    files.add(jf);

                    SSDir id = GetDir(dhash);
                    if (id != null) {
                        synchronized (id.filesInDir) {
                            id.filesInDir.add(jf);
                        }
                    } else
                        l.severe("File for no dir!");
                } catch (NumberFormatException nfe) {
                    l.fatal("NUmber ex reading files" + nfe.getMessage() + line);
                } catch (StringIndexOutOfBoundsException e) {
                    l.severe(e.getMessage()+ "line " + lc);
                }
            }


            tt.stop();
            l.debug("Files : " + files.size() + " in: " + tt  );
            tt.reset();


            ByteBuffer ibuf = ByteBuffer.allocateDirect(8+4+4*16*16);
            try
            {
                while(fin.available()>0)
                {
                    ibuf.rewind();
                    long pos = funiq.position();
                    funiq.read(ibuf);
                    ibuf.rewind();
                    long crc = ibuf.getLong();
                    int rate = ibuf.getInt();
                    int []buf = new int[16*16];
                    for(int px = 0; px < 16*16; px++)
                    {
                        buf[px] = ibuf.getInt();
                    }
                    SSImage ji = new SSImage(crc,buf,rate,0, pos);
                    if ( ji.rating > 0 )
                        l.warn("SAVE: IMAGE: CRC %d RATING %d", ji.crc, ji.rating);

                    images.put(crc, ji);
                    l.debug("LOAD: LOADING: IMG: crc=%d",   crc );

                }
            }
            catch(IOException e)
            {

            }
            tt.stop();
            l.warn("Images: " + images.size() + " in: " + tt );
            tt.reset();

            fdirs.close();
            ffiles.close();
            funiq.close();
            fin.close();

            makeInstances();

            loadTimer.stop();
            tt.stop();

            tt.stop();
            l.debug("Instances created in: " + tt );
            l.warn("Total load time     : " + loadTimer  );
        }

        catch(IOException ioe)
        {
            l.severe(String.format("Caught IOE : %s in save" , ioe.toString()) );
            throw new ImException("Can't Open Set");
        }
    }

    public void Load5() throws ImException
    {
        try
        {
            loadTimer.reset();
            roz.rlib.RTimer tt = new roz.rlib.RTimer();
            tt.reset();

            Path rdir = homeDir.resolve("root5.txt");
            Path pdir = homeDir.resolve("directory5.txt");
            Path pfile = homeDir.resolve("files5.txt");
            Path puniq = homeDir.resolve("uniq5.bin");

            rootDirStr = rdir.toString();

            if ( !Files.exists(rdir) )
            {
                l.warn("LOAD: NONEXISTANT: Set " + homeDir.toString() + " does not exist");
                return;
            }
            l.warn("LOAD: LOADING: Set " + homeDir.toString() );

            BufferedReader froot;
            BufferedReader fdirs;
            BufferedReader ffiles;
            FileInputStream  fin;
            FileChannel funiq;

            fin = new FileInputStream(puniq.toString());
            funiq =  fin.getChannel();

            froot = Files.newBufferedReader(rdir);
            fdirs = Files.newBufferedReader(pdir);
            ffiles = Files.newBufferedReader(pfile);

            String line;
            line = froot.readLine();
            l.warn("LOAD: LOADING: ROOT: " + line );
            if ( DirHelper.GetRoot().isEmpty() )
                DirHelper.SetRoot(line);

            if ( !DirHelper.GetRoot().equals(line) )
                throw new ImException("Roots dont match: " + DirHelper.GetRoot() + " - " + line );

            while ((line = fdirs.readLine()) != null)
            {
                int cpos1 = line.indexOf((int)',');
                int cpos2 = line.indexOf((int)',', cpos1+1);

                String sdhash = line.substring(0, cpos1);
                String stime = line.substring(cpos1+1,cpos2);
                String spath = "";
                if ( line.length() > cpos2+1 )
                    spath = line.substring(cpos2+1);

                try
                {
                    Long t = Long.parseLong(stime);
                    Long d = Long.parseLong(sdhash);
                    SSDir jd = new SSDir(spath, t);
                    // if ( withQFile )
                    //     jd.filesInDir = new LinkedList<IFile>();

                    //l.debug("LOAD: LOADING: DIR: hash=%d calchash=%d GetLocalPath=%s", d, jd.dh.GetHash(), spath );

                    if ( jd.dh.GetHash() != d ) {
                        //    throw new ImException("DHASH Mismatch on " + spath);
                        l.severe("DHASH Mismatch on " + spath);
                        jd.dh.SetHash(d);
                    }

                    PutDir(jd.dh.GetHash(), jd);
                }
                catch(NumberFormatException nfe)
                {
                    l.fatal("Number ex reading dirs" + nfe.getMessage() + line );
                }
            }

            tt.stop();
            l.debug("Dirs  : " + dirs.size() + " in: " + tt );
            tt.reset();
            int lc=0;
            while ((line = ffiles.readLine()) != null) {
                try {
                    int cpos1 = line.indexOf((int) ',');
                    int cpos2 = line.indexOf((int) ',', cpos1 + 1);
                    String sfhash = line.substring(0, cpos1);
                    String scrc = line.substring(cpos1 + 1, cpos2);
                    String sname = line.substring(cpos2 + 1);

                    long dhash = Long.parseLong(sfhash);
                    long crc = Long.parseLong(scrc);
                    SSFile jf = new SSFile(dhash, sname, crc);
                    l.debug("LOAD: LOADING: FILE: hash=%d name=%s cec=%d", dhash, sname, crc);

                    lc++;
                    files.add(jf);

                    SSDir id = GetDir(dhash);
                    if (id != null) {
                        synchronized (id.filesInDir) {
                            id.filesInDir.add(jf);
                        }
                    } else
                        l.severe("File for no dir!");
                } catch (NumberFormatException nfe) {
                    l.fatal("NUmber ex reading files" + nfe.getMessage() + line);
                } catch (StringIndexOutOfBoundsException e) {
                    l.severe(e.getMessage()+ "line " + lc);
                }
            }


            tt.stop();
            l.debug("Files : " + files.size() + " in: " + tt  );
            tt.reset();


            ByteBuffer ibuf = ByteBuffer.allocateDirect(8+4+4+(4*16*16));
            try
            {
                while(fin.available()>0)
                {
                    ibuf.rewind();
                    long pos = funiq.position();
                    funiq.read(ibuf);
                    ibuf.rewind();
                    long crc = ibuf.getLong();
                    int rate = ibuf.getInt();
                    int user1 = ibuf.getInt();
                   // System.out.printf("CRC %d %s %s = %s %s%n", crc, Long.toHexString(crc), Integer.toHexString(rate), Integer.toHexString(user1) );
                    int []buf = new int[16*16];
                    for(int px = 0; px < 16*16; px++)
                    {
                        buf[px] = ibuf.getInt();
                    }
                    SSImage ji = new SSImage(crc,buf,rate, user1, pos);
                    if ( ji.rating > 0 )
                        l.warn("LOAD: IMAGE: CRC %d RATING %d", ji.crc, ji.rating);
                    if ( ji.user1 > 0 )
                        l.warn("LOAD: IMAGE: CRC %d USER1 %d", ji.crc, ji.rating);

                    images.put(crc, ji);
                    l.debug("LOAD: LOADING: IMG: crc=%d",   crc );

                }
            }
            catch(IOException e)
            {

            }
            tt.stop();
            l.warn("Images: " + images.size() + " in: " + tt );
            tt.reset();

            fdirs.close();
            ffiles.close();
            funiq.close();
            fin.close();

            makeInstances();

            loadTimer.stop();
            tt.stop();

            tt.stop();
            l.debug("Instances created in: " + tt );
            l.warn("Total load time     : " + loadTimer  );
        }

        catch(IOException ioe)
        {
            l.severe(String.format("Caught IOE : %s in save" , ioe.toString()) );
            throw new ImException("Can't Open Set");
        }
    }

    
    public void Load6() throws ImException
    {
        try
        {
            loadTimer.reset();
            roz.rlib.RTimer tt = new roz.rlib.RTimer();
            tt.reset();

            Path pdir = homeDir.resolve("dirs.txt");
            Path pfile = homeDir.resolve("files.txt");
            Path puniq = homeDir.resolve("images.bin");



            BufferedReader fdirs;
            BufferedReader ffiles;
            FileInputStream  fin;
            FileChannel funiq;

            fin = new FileInputStream(puniq.toString());
            funiq =  fin.getChannel();

            fdirs = Files.newBufferedReader(pdir);
            ffiles = Files.newBufferedReader(pfile);

 
            rootDirStr = fdirs.readLine();
            Path rdir = Paths.get(rootDirStr);

            if ( !Files.exists(rdir) )
            {
                l.warn("LOAD: NONEXISTANT: root dor " + rdir.toString() + " does not exist");
                return;
            }
            l.warn("LOAD: LOADING: Set " + pdir.toString() );   
            if ( !Files.exists(rdir) )
            {
                l.warn("LOAD: NONEXISTANT: Set " + rdir.toString() + " does not exist");
                return;
            }
            l.warn("LOAD: LOADING: Set " + pdir.toString() );            
            
            
            l.warn("LOAD: LOADING: ROOT: " + rootDirStr );
            if ( DirHelper.GetRoot().isEmpty() )
                DirHelper.SetRoot(rootDirStr);

 
            String line;
            while ((line = fdirs.readLine()) != null)
            {
                int cpos1 = line.indexOf((int)',');

                String sdhash = line.substring(0, cpos1);
                String spath = line.substring(cpos1 + 1);

                try
                {
                    Long t = 0L;//Long.parseLong(0L);
                    Long d = Long.parseLong(sdhash);
                    SSDir jd = new SSDir(spath, t);
                    // if ( withQFile )
                    //     jd.filesInDir = new LinkedList<IFile>();

                    //l.debug("LOAD: LOADING: DIR: hash=%d calchash=%d GetLocalPath=%s", d, jd.dh.GetHash(), spath );

                    if ( jd.dh.GetHash() != d ) {
                        //    throw new ImException("DHASH Mismatch on " + spath);
                        l.severe("DHASH Mismatch on " + spath);
                        jd.dh.SetHash(d);
                    }

                    PutDir(jd.dh.GetHash(), jd);
                }
                catch(NumberFormatException nfe)
                {
                    l.fatal("Number ex reading dirs" + nfe.getMessage() + line );
                }
            }

            tt.stop();
            l.debug("Dirs  : " + dirs.size() + " in: " + tt );
            tt.reset();
            int lc=0;
            while ((line = ffiles.readLine()) != null) {
                try {
                    int cpos1 = line.indexOf((int) ',');
                    int cpos2 = line.indexOf((int) ',', cpos1 + 1);
                    String sfhash = line.substring(0, cpos1);
                    String scrc = line.substring(cpos1 + 1, cpos2);
                    String sname = line.substring(cpos2 + 1);

                    long dhash = Long.parseLong(sfhash);
                    long crc = Long.parseLong(scrc);
                    SSFile jf = new SSFile(dhash, sname, crc);
                    l.debug("LOAD: LOADING: FILE: hash=%d name=%s cec=%d", dhash, sname, crc);

                    lc++;
                    files.add(jf);

                    SSDir id = GetDir(dhash);
                    if (id != null) {
                        synchronized (id.filesInDir) {
                            id.filesInDir.add(jf);
                        }
                    } else
                        l.severe("File for no dir!");
                } catch (NumberFormatException nfe) {
                    l.fatal("NUmber ex reading files" + nfe.getMessage() + line);
                } catch (StringIndexOutOfBoundsException e) {
                    l.severe(e.getMessage()+ "line " + lc);
                }
            }


            tt.stop();
            l.debug("Files : " + files.size() + " in: " + tt  );
            tt.reset();


            ByteBuffer ibuf = ByteBuffer.allocateDirect(8+4+4+(4*16*16));
            try
            {
                while(fin.available()>0)
                {
                    ibuf.rewind();
                    long pos = funiq.position();
                    funiq.read(ibuf);
                    ibuf.rewind();
                    long crc = ibuf.getLong();
                    int rate = ibuf.getInt();
                    int user1 = ibuf.getInt();
                   // System.out.printf("CRC %d %s %s = %s %s%n", crc, Long.toHexString(crc), Integer.toHexString(rate), Integer.toHexString(user1) );
                    int []buf = new int[16*16];
                    for(int px = 0; px < 16*16; px++)
                    {
                        buf[px] = ibuf.getInt();
                    }
                    SSImage ji = new SSImage(crc,buf,rate, user1, pos);
                    if ( ji.rating > 0 )
                        l.warn("LOAD: IMAGE: CRC %d RATING %d", ji.crc, ji.rating);
                    if ( ji.user1 > 0 )
                        l.warn("LOAD: IMAGE: CRC %d USER1 %d", ji.crc, ji.rating);

                    images.put(crc, ji);
                    l.debug("LOAD: LOADING: IMG: crc=%d",   crc );

                }
            }
            catch(IOException e)
            {

            }
            tt.stop();
            l.warn("Images: " + images.size() + " in: " + tt );
            tt.reset();

            fdirs.close();
            ffiles.close();
            funiq.close();
            fin.close();

            makeInstances();

            loadTimer.stop();
            tt.stop();

            tt.stop();
            l.debug("Instances created in: " + tt );
            l.warn("Total load time     : " + loadTimer  );
        }

        catch(IOException ioe)
        {
            l.severe(String.format("Caught IOE : %s in save" , ioe.toString()) );
            throw new ImException("Can't Open Set");
        }
    }

    // Output, In1, In2...
    public static void Merge(String args[]) throws ImException
    {
        try
        {
            Path newset = Paths.get(args[1]);
            List<Path> merged = new LinkedList<Path>();
            String line;
            String relDir = "";

            if ( Files.exists(newset) )
                l.fatal("Output set exixts!");
            else
                Files.createDirectory(newset);

            Path sroot = newset.resolve("root5.txt");
            Path sdir = newset.resolve("directory5.txt");
            Path sfile = newset.resolve("files5.txt");
            Path suniq = newset.resolve("uniq5.bin");

            BufferedWriter froot = Files.newBufferedWriter(sroot,  StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            BufferedWriter fdirs = Files.newBufferedWriter(sdir,  StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            BufferedWriter ffiles= Files.newBufferedWriter(sfile, StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            FileOutputStream  fout = new FileOutputStream(suniq.toString()) ;
            FileChannel funiq= fout.getChannel();


            for(int c=2; c<args.length; c++)
            {

                Path p = Paths.get(args[c]);
                if ( !ScanSet.Valid5(p))
                    l.fatal("Set " + p + " Not found");
            }

            Path p = Paths.get(args[2]);

            Path proot = p.resolve("root5.txt");
            BufferedReader froots  = Files.newBufferedReader(proot);
            line = froots.readLine();
            l.warn("MERGE: LOADING: ROOT: " + line );
            froot.write(line);
            froot.newLine();
            froot.close();


            for(int c=2; c<args.length; c++)
            {
                p = Paths.get(args[c]);


                l.warn("MERGE: LOADING: " + p.toString() );
                Path pdir = p.resolve("directory5.txt");
                Path pfile = p.resolve("files5.txt");
                Path puniq = p.resolve("uniq5.bin");

                BufferedReader fidirs = Files.newBufferedReader(pdir);
                BufferedReader fifiles = Files.newBufferedReader(pfile);
                FileInputStream  fin = new FileInputStream(puniq.toString());
                FileChannel fiuniq =  fin.getChannel();


                while ((line = fidirs.readLine()) != null)
                {
                    fdirs.write(line);
                }
                fdirs.newLine();
                while ((line = fifiles.readLine()) != null)
                {
                    ffiles.write(line);
                    ffiles.newLine();
                }

                ByteBuffer ibuf = ByteBuffer.allocateDirect(8+4+4*16*16);
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

    private void makeInstances()
    {
        for(SSFile f : files  )
        {
            SSInstances ii = instances.get(f.crc);
            if ( ii == null )
            {
                ii = new SSInstances(f.crc);
                instances.put(ii.crc, ii);
            }
            ii.instances.add(f);
        }
    }

    private void PutFile(SSFile f)
    {
        synchronized(files)
        {
            files.add(f);
        }
    }

    private void PutDir(long dhash, SSDir jd)
    {
        synchronized(dirs)
        {
            dirs.put(jd.dh.GetHash(), jd);
        }
    }

    public SSImage PutImage(long crc, int[] tn, int rate, int user1)
    {
        SSImage i = new SSImage(crc, tn, rate, user1, 0);
        synchronized(images)
        {
            images.put(crc, i);
        }
        return i;
    }

    public void SaveImg5(SSImage i)
    {
        try
        {
            Path puniq = homeDir.resolve("uniq5.bin");
            RandomAccessFile  fout;
            FileChannel funiq;
            fout = new RandomAccessFile (puniq.toString(),"rw"); //FileOutputStream(puniq.toString()) ;//
            funiq = fout.getChannel();

            ByteBuffer ibuf = ByteBuffer.allocateDirect(8+4+4+4*16*16);

            l.low("Saving img " + i);
            ibuf.rewind();
            ibuf.putLong(i.crc);
            ibuf.putInt(i.rating);
            ibuf.putInt(i.user1);

            int by = 0;
            for (int b = 0; b < 16 * 16; b++)
            {
                ibuf.putInt(i.thumb[by]);
                by++;
            }
            ibuf.rewind();
            funiq.position(i.pos);
            funiq.write(ibuf);
            funiq.close();
            fout.close();
        }

        catch(IOException ioe)
        {
            l.severe(String.format("Caught IOE : %s in save" , ioe.toString()) );
            ioe.printStackTrace();

        }
    }

    public void Save()
    {
        try
        {
            saveTimer.reset();

            Path rdir = homeDir.resolve("root4.txt");
            Path pdir = homeDir.resolve("directory4.txt");
            Path pfile = homeDir.resolve("files4.txt");
            Path puniq = homeDir.resolve("uniq4.bin");

            BufferedWriter froot;
            BufferedWriter fdirs;
            BufferedWriter ffiles;
            FileOutputStream  fout;
            FileChannel funiq;

            froot = Files.newBufferedWriter(rdir,  StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            fdirs = Files.newBufferedWriter(pdir,  StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            ffiles = Files.newBufferedWriter(pfile, StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            fout = new FileOutputStream(puniq.toString()) ;// RandomAccessFile (puniq.toString(),"rw");
            funiq = fout.getChannel();

            froot.write(DirHelper.GetRoot());
            froot.newLine();

            synchronized(dirs)
            {
                for (SSDir d : dirs.values())
                {
                    if (d.inUse)
                    {
                        String rel = d.dh.GetLocalPath();
                        String st = String.format("%d,%d,%s", d.dh.GetHash(), d.mtime, rel);
                        fdirs.write(st);
                        fdirs.newLine();
                    }
                }
            }

            synchronized(files)
            {
                for (SSFile f : files)
                {
                    if (f.inUse)
                    {
                        String st = String.format("%d,%d,%s", f.dhash, f.crc, f.name);
                        ffiles.write(st);
                        ffiles.newLine();
                    }
                }
            }


            ByteBuffer ibuf = ByteBuffer.allocateDirect(8+4+4*16*16);

            synchronized(images)
            {
                for (SSImage i : images.values())
                {
                    if (!i.inUse)
                        continue;

                    if (i.thumb == null)
                    {
                        l.severe("ERR Saving img " + i + " thumb is null");
                        continue;
                    }
                    if (i.thumb.length != 16 * 16)
                    {
                        l.severe("ERR Saving img " + i + " length not rigth: " + i.thumb.length);
                        continue;
                    }
                    l.low("Saving img " + i);
                    ibuf.rewind();
                    ibuf.putLong(i.crc);
                    ibuf.putInt(i.rating);

                    int by = 0;
                    for (int b = 0; b < 16 * 16; b++)
                    {
                        ibuf.putInt(i.thumb[by]);
                        by++;
                    }
                    ibuf.rewind();
                    funiq.write(ibuf);
                    if ( i.rating > 0 )
                        l.warn("SAVE: IMAGE: CRC %d RATING %d", i.crc, i.rating);
                }
            }

            saveTimer.stop();
            ffiles.close();
            funiq.close();
            fout.close();
            froot.close();
            fdirs.close();
        }

        catch(IOException ioe)
        {
            l.severe(String.format("Caught IOE : %s in save" , ioe.toString()) );
            ioe.printStackTrace();

        }
    }
    public void Save5()
    {
        try
        {
            saveTimer.reset();

            Path rdir = homeDir.resolve("root5.txt");
            Path pdir = homeDir.resolve("directory5.txt");
            Path pfile = homeDir.resolve("files5.txt");
            Path puniq = homeDir.resolve("uniq5.bin");

            BufferedWriter froot;
            BufferedWriter fdirs;
            BufferedWriter ffiles;
            FileOutputStream  fout;
            FileChannel funiq;

            froot = Files.newBufferedWriter(rdir,  StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            fdirs = Files.newBufferedWriter(pdir,  StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            ffiles = Files.newBufferedWriter(pfile, StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            fout = new FileOutputStream(puniq.toString()) ;// RandomAccessFile (puniq.toString(),"rw");
            funiq = fout.getChannel();

            froot.write(DirHelper.GetRoot());
            froot.newLine();

            synchronized(dirs)
            {
                for (SSDir d : dirs.values())
                {
                    if (d.inUse)
                    {
                        String rel = d.dh.GetLocalPath();
                        String st = String.format("%d,%d,%s", d.dh.GetHash(), d.mtime, rel);
                        fdirs.write(st);
                        fdirs.newLine();
                    }
                }
            }

            synchronized(files)
            {
                for (SSFile f : files)
                {
                    if (f.inUse)
                    {
                        String st = String.format("%d,%d,%s", f.dhash, f.crc, f.name);
                        ffiles.write(st);
                        ffiles.newLine();
                    }
                }
            }


            ByteBuffer ibuf = ByteBuffer.allocateDirect(8+4+4+4*16*16);

            synchronized(images)
            {
                for (SSImage i : images.values())
                {
                    if (!i.inUse)
                        continue;

                    if (i.thumb == null)
                    {
                        l.severe("ERR Saving img " + i + " thumb is null");
                        continue;
                    }
                    if (i.thumb.length != 16 * 16)
                    {
                        l.severe("ERR Saving img " + i + " length not rigth: " + i.thumb.length);
                        continue;
                    }
                    l.low("Saving img " + i);
                    ibuf.rewind();
                    ibuf.putLong(i.crc);
                    ibuf.putInt(i.rating);
                    ibuf.putInt(i.user1);

                    int by = 0;
                    for (int b = 0; b < 16 * 16; b++)
                    {
                        ibuf.putInt(i.thumb[by]);
                        by++;
                    }
                    ibuf.rewind();
                    funiq.write(ibuf);
                    if ( i.rating > 0 )
                        l.warn("SAVE: IMAGE: CRC %d RATING %d", i.crc, i.rating);
                }
            }

            saveTimer.stop();
            ffiles.close();
            funiq.close();
            fout.close();
            froot.close();
            fdirs.close();
        }

        catch(IOException ioe)
        {
            l.severe(String.format("Caught IOE : %s in save" , ioe.toString()) );
            ioe.printStackTrace();

        }
    }
    private boolean countMode = false;
    private boolean purgeMode = false;
    private int d2p=0;
    private int f2p=0;
    private int dfiles=0;
    private int nfiles=0;
    private int efiles=0;
    private int ndirs=0;

    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
    {

        if ( countMode )
        {
            d2p++;
            l.info("SCAN: INDIR: COUNT: %s " , dir.toString() );
            return CONTINUE;
        }

        long utime = Utils.utime(attrs.lastModifiedTime());
        SSDir newdir = new SSDir(dir.toString(), utime);

        l.info("SCAN: INDIR: VISIT RAW: %s GetLocalPath=%s hash=%d", dir.toString(), newdir.dh.GetLocalPath(), newdir.dh.GetHash()  );

        SSDir existing = GetDir(newdir.dh.GetHash());
        if ( existing == null )
        {
            l.warn("SCAN: INDIR: PROCESS: NEW %s ", newdir.dh.GetLocalPath());
            PutDir(newdir.dh.GetHash(), newdir);
            existing = GetDir(newdir.dh.GetHash());
            ndir++;
        }
        else
        {
            l.debug("SCAN: INDIR: PROCESS: EXISTING %s ", existing.dh.GetLocalPath());
        }
        existing.inUse = true;

        dfiles++;



        return CONTINUE;
    }

    public SSDir GetDir(long dhash)
    {
        synchronized (dirs)
        {
            return dirs.getOrDefault(dhash, null);
        }
    }


    public SSFile GetFile(long dhash, String name)
    {
        synchronized (files)
        {
            SSDir id = null;
            id = GetDir(dhash);
            if (id != null && id.filesInDir != null)
            {
                // we have a quick file list created. use that
                for (SSFile f : id.filesInDir)
                {
                    if (name.equals(f.name))
                        return f;
                }
                return null;
            }

            for (SSFile f : files)
            {
                if (f.dhash == dhash && name.equals(f.name))
                    return f;
            }
            return null;
        }
    }

    int foundFiles = 0;
    int newFiles = 0;

    public SSImage GetImage(long crc)
    {
        synchronized (images)
        {
            return images.get(crc);
        }
    }

    public void PutImage(SSImage img)
    {
        synchronized (images)
        {
            images.put(img.crc, img);
        }
    }

    private void processFile(SSFile file)
    {
        l.info("FILE process %s", file.toString() );
        encoder.Add(file);
    }

    public FileVisitResult visitFile(Path _file, BasicFileAttributes attrs)
    {
        if ( countMode )
        {
            f2p++;

            if ( intervalTimer.elapsed() > 1000 )
            {
                intervalTimer.reset();
                l.warn("Counting... dirs %d files %d", d2p, f2p);
            }
            return CONTINUE;
        }

        nfiles++;
        DirHelper dh = new DirHelper(_file.toString(), true);
        String name = _file.getFileName().toString();

        // if not an image file, not interested
        if ( !roz.rlib.Utils.isImageFile(name) )
            return CONTINUE;

        SSFile f = GetFile(dh.GetHash(), name);

        if ( f != null )
        {
            foundFiles++;
            f.inUse = true;
            SSImage i = GetImage(f.crc);
            if ( i != null )
            {
                i.inUse = true;
                return CONTINUE;
            }
            else
                l.info("Missing image!");
        }
        newFiles++;
        l.info("NEW File %s", name);

        f = new SSFile(dh.GetHash(), name, 0);
        f.inUse = true;
        PutFile(f);
        processFile(f);

        if ( intervalTimer.elapsed() > 10000 )
        {
            intervalTimer.reset();
            pstat();
        }

        return CONTINUE;
    }

    public FileVisitResult visitFileFailed(Path file, IOException exc)
    {
        efiles++;
        return CONTINUE;
    }


    public void Scan(Path scanp, boolean purge, boolean append)throws ImException
    {
        // Create an encoder.
        if ( encoder == null )
            encoder = new SSFileEncoder(this);

        scanTimer.reset();
        intervalTimer.reset();

        rt = Runtime.getRuntime();
        rt.gc();


       // encoder = new FileEncoder(set);

        try
        {
            // Check img directory exists
            if ( !Files.exists(scanp) )
            {
                throw new ImException(" - SCAN:" + scanp.toString() + " does not exist.");
            }

            l.warn("SCAN - Counting new files to be added..");
            countMode = true;
            purgeMode = purge;


            if ( purgeMode )
            {
                l.warn("SCAN - Starting scan, marking all as unused ( purge mode )");
                markAllNotUsed(scanp);
            }
            else
            {
                markAllUsed();
                l.warn("SCAN - Starting scan, marking existing as in use ( add/create mode) d=%d f=%d i=%d", ssdmarked, ssfmarked, ssimarked);
            }

            Files.walkFileTree(scanp, this);
            l.warn("SCAN: To Process: Dirs: %s Files: %d%n, begin processing... ", d2p, f2p);
            intervalTimer.reset();
            countMode = false;
            intime = System.currentTimeMillis();

            Files.walkFileTree(scanp, this);
        }
        catch (Exception e)
        {
            l.severe("scan - caught: " + e );
            e.printStackTrace();

        }

        makeInstances();
        scanTimer.stop();

        l.warn("SCANSET: Waiting for threads to finish");
        encoder.Stop();
        l.warn("SCANSET: All finished finish");


    }


    private int ndir=0;
    private int nfile=0;

    private boolean rescanMode = false;

    public void HotScan(Path scanp)throws ImException
    {
        // Create an encoder.
        if ( encoder == null )
            encoder = new SSFileEncoder(this);

        scanTimer.reset();
        intervalTimer.reset();

        rt = Runtime.getRuntime();
        rt.gc();


        // encoder = new FileEncoder(set);

        try
        {
            // Check img directory exists
            if ( !Files.exists(scanp) )
            {
                throw new ImException(" - SCAN:" + scanp.toString() + " does not exist.");
            }

            l.warn("Counting new files to be added..");
            countMode = true;

            markAllUsed();
            rescanMode = true;
            newFiles=0;
            ndirs=0;

            Files.walkFileTree(scanp, this);
            l.warn("Existing dirs under hot dir: %s files: %d%n", d2p, f2p);
            intervalTimer.reset();
            countMode = false;
            intime = System.currentTimeMillis();

            Files.walkFileTree(scanp, this);
        }
        catch (Exception e)
        {
            l.severe("scan - caught: " + e );
            e.printStackTrace();

        }

        makeInstances();
        scanTimer.stop();

        l.warn("SCANSET: Waiting for threads to finish");
        encoder.Stop();
        l.warn("SCANSET: All finished finish");
        l.warn("HotScan complete, new files %s, dirs %d", newFiles, ndirs);


    }

    public class SSFileEncoder
    {
        private SSFileEncoderTrd encoders[];
        private int nThreads = 0;
        private ScanSet set;
        private int finishedThreads = 0;
        private boolean running = true;
        private int pfiles = 0;

        private List<ScanSet.SSFile> finished = new LinkedList<ScanSet.SSFile>();


        public SSFileEncoder(ScanSet s)
        {
            set = s;
            Runtime rt = Runtime.getRuntime();
            nThreads = Math.max(2, rt.availableProcessors() - 1);

            encoders = new SSFileEncoderTrd[nThreads];
            for (int t = 0; t < nThreads; t++)
            {
                encoders[t] = new SSFileEncoderTrd(t);
                new Thread(encoders[t]).start();
            }
            l.warn("FileEncoder created, using %d threads.", nThreads);
        }


        public ScanSet.SSFile Get()
        {
            synchronized (finished)
            {
                if (finished.size() > 0)
                {
                    ScanSet.SSFile f = finished.remove(0);
                    return f;
                }
            }
            return null;
        }

        public void Stop()
        {
            l.warn("ENCODER: STOP: Wait for %d threads to shutdown", nThreads);
            finishedThreads = nThreads;
            Utils.Sleep(100);
            running = false;

            int i=10;
            while (i-->0 && finishedThreads > 0)
                Utils.Sleep(1000);


            if ( finishedThreads > 0 )
                l.severe("ENCODER: STOP: some remaining active:d" , finishedThreads);
            else
                l.warn("ENCODER: STOP: all finished");
        }



        public void Add(ScanSet.SSFile file)
        {
            int thisFile = pfiles++;
            int retry = 0;


            while (true)
            {
                for (int t = 0; t < nThreads; t++)
                {
                    if (encoders[t].f == null)
                    {
                        l.debug("ENCODER: ADD: File %s sent to encoder tread %d after %d attempts", file.name, t, retry);
                        encoders[t].f = file;
                        return;
                    }
                }
                retry++;
                Utils.Sleep(1);
            }

        }


        private class SSFileEncoderTrd implements Runnable
        {
            public ScanSet.SSFile f = null;
            public int instance;

            private void tprocessFile(ScanSet.SSFile f)
            {
                try
                {
                    ScanSet.SSCRC crcs = GetCRCAndBytes(f.GetDiskPath());

                    if (crcs != null)
                    {
                        ScanSet.SSImage i = null;

                        i = set.GetImage(crcs.crc);

                        if (f != null && i != null)
                        {
                            f.crc = crcs.crc;
                            f.inUse = true;
                            i.inUse = true;
                            l.debug("ENCODERTHRD(%d) NOWTTODO %s", instance, f.name);
                            return; // noithing to do
                        }

                        if (i == null)
                        {
                            int[] tn = GetTnFromFileBytes(crcs.bytes);
                            if (tn == null)
                            {
                                l.warn("ENCODERTHRD(%d) ERROR %s", instance, f.name);
                                return;
                            }
                            i = set.PutImage(crcs.crc, tn, 0,0);
                            f.crc = crcs.crc;
                            i.isNew = true;
                            l.debug("ENCODERTHRD(%d) FINISHED %s", instance, f.name);
                        }
                        i.inUse = true;
                    }
                }
                catch (Exception ex)
                {
                    l.severe("tprocessFile : error processing " + f.toString() + " : " + ex.toString());
                }

            }


            public SSFileEncoderTrd(int inst)
            {
                instance = inst;
            }

            @Override
            public void run()
            {
                l.info("Thread " + instance + " running");

                while (running)
                {
                    // Wait for a file to arrive
                    if (f == null)
                    {
                        Utils.Sleep(1);
                        continue;
                    }
                    // process it
                    l.debug("ENCODERTHRD(%d) PICKINGUP %s", instance, f.name);
                    tprocessFile(f);
                    // l.info("FENC Thread " + instance + " finished processing " );
                    // indicate we are ready for next file
                    f = null;
                }
                l.info("ENCODERTHRD(%d) STOPPING. ", instance);
                finishedThreads--;

            }

        }
    }

    public enum SSState  { NotStarted, Running, Finished, Abort, Retired };
    public enum SSScanType { Simple, Luma, Mono };
    public static int staskseed = 0;

    public SSSearch CreateSearch()
    {
        return new SSSearch();
    }

    public class SSSearch
    {

        private SSSearchTask currentTask = null;

        public SSSearchTextTask createTextSearch()
        {
            return new SSSearchTextTask();
        }
        public SSSearchImageTask createImageSearch()
        {
            return new SSSearchImageTask();
        }
        public SSSearchRatingTask createRatingSearch()
        {
            return new SSSearchRatingTask();
        }

        public SSSearch()
        {
        }

        public void Stop()
        {
            if ( currentTask != null )
                currentTask.aborted();
        }


        public boolean startSearch(SSSearchTask task)
        {
            if ( currentTask != null )
                return false;
            currentTask = task;
            l.warn("SE - Start start task");
            currentTask.start();
            l.warn("SE - Start return");
            return true;
        }

        public abstract class SSSearchTask extends Thread
        {
            public void retire()
            {
                state = SSState.Retired;
            }

            public String errorTxt = "";
            public int taskNum = ++staskseed;


            public void aborted()
            {
                state =SSState.Abort;
            }
            public void running()
            {
                currentTask = this;
                state = SSState.Running;
                start();
            }

            public void finished()
            {
                currentTask = null;
                state = SSState.Finished;
            }

            public abstract void run();
            public String statusStr = "";
            public SSState state = SSState.NotStarted;
            public List<SSSearchResult> results = new LinkedList<SSSearchResult>();

            public void addResult(SSSearchResult r)
            {
                synchronized (results)
                {
                    results.add(r);
                }
            }

        }

        public class RatingComparator implements Comparator<SSSearchResult> {
            @Override
            public int compare(SSSearchResult o1, SSSearchResult o2) {
                SSRatingSearchResult r1 = ( SSRatingSearchResult)o1;
                SSRatingSearchResult r2 = ( SSRatingSearchResult)o2;
                return Integer.compare(r1.i.GetRating(),r2.i.GetRating());
            }
        }

        public class SSSearchRatingTask extends SSSearchTask
        {

            @Override
            public void run()
            {
                l.warn(String.format("SearchRatingTask - run rate search"));
                int s=0, f=0;

                for( SSImage i : images.values() )
                {
                    s++;
                    if ( state == SSState.Abort )
                        break;
                    if ( i.rating > 0  )
                    {
                        f++;
                        addResult(new SSRatingSearchResult(i));
                        statusStr = String.format("Searching %d images, found %d", s, f);
                    }
                }

                statusStr = String.format("Searched %d images, found %d", s, f);
                l.warn(String.format("SearchRatingTask - finished thread search "));

                Collections.sort(results, new RatingComparator());
                finished();
            }
        }

        public class SSSearchTextTask extends SSSearchTask
        {

            public void setSearchFiles(boolean searchFiles) {  this.textSearchFileState = searchFiles; }
            private boolean textSearchDirState;
            public void setSearchDirs(boolean searchDirs)
            {
                this.textSearchDirState = searchDirs;
            }
            private boolean textSearchFileState;
            public void setTextSearchString(String textSearchString)
            {
                this.textSearchString = textSearchString;
            }
            private String textSearchString="";


            @Override
            public void run()
            {
                int fd=0;
                int sd=0;
                int sf=0;
                int ff=0;

                l.warn(String.format("SearchTextTask - run thread search %s", textSearchString));

                if ( textSearchString.length() == 0 )
                {
                    statusStr = String.format("Empty search string");
                    finished();
                    return;
                }

                if ( textSearchDirState )
                {
                    for( SSDir d : dirs.values() )
                    {
                        sd++;
                        if ( state == SSState.Abort )
                            break;
                        if ( StringUtils.containsIgnoreCase(d.GetDiskPath(),textSearchString) )
                        {
                            fd++;
                            addResult(new SSTextDirSearchResult(d));
                        }
                        statusStr = String.format("Searched %d dirs found %d, %d files found %d", sd, fd, sf, ff);
                    }
                }

                if ( textSearchFileState )
                {

                    for( SSFile d : files )
                    {
                        sf++;
                        if ( state == SSState.Abort )
                            break;
                        if ( StringUtils.containsIgnoreCase(d.name,textSearchString) )
                        {
                            ff++;
                            addResult(new SSTextFileSearchResult(d));
                        }
                        statusStr = String.format("Searched %d dirs found %d, %d files found %d", sd, fd, sf, ff);
                    }
                }

                statusStr = String.format("Finished search, %d dirs found, files found %d", fd,  ff);
                l.warn(String.format("SearchTextTask - finished thread search %s", textSearchString));

                finished();
            }
        }

        public class SSSearchResult
        {

        }

        public class SSImageSearchResult extends SSSearchResult implements Comparable<SSImageSearchResult>
        {
            public SSImage  image=null;
            public double       closeness=-1;

            public SSImageSearchResult(SSImage image, double closeness)
            {
                this.closeness = closeness;
                this.image = image;
            }

            @Override
            public int compareTo(SSImageSearchResult o)
            {
                return Double.compare(closeness, o.closeness);
            }
        }

        public class SSTextFileSearchResult extends SSSearchResult
        {
            public SSFile   file=null;

            public SSTextFileSearchResult(SSFile f)
            {
                this.file = f;
            }

        }

        public class SSTextDirSearchResult extends SSSearchResult
        {
            public SSDir    dir=null;

            public SSTextDirSearchResult(SSDir d)
            {
                this.dir = d;
            }

        }

        public class SSRatingSearchResult extends SSSearchResult
        {
            public SSImage    i=null;

            public SSRatingSearchResult(SSImage i)
            {
                this.i = i;
            }

        }

        public class SSSearchImageTask extends SSSearchTask
        {
            public void setImageSearch(SSScanType scanType, String searchImagePath)
            {
                this.scanType = scanType;
                this.searchImagePath = searchImagePath;
            }
            public String searchImagePath;
            private javafx.scene.image.Image searchImage;

            SSCRC crcs =  null;
            int tnbytes[];
            List<SSImageSearchResult> iresults = new LinkedList<SSImageSearchResult>();
            SSScanType scanType;

            public void getCVal(SSImage ji)
            {
                if (ji.crc == crcs.crc)
                {
                    //l.info("SEIMG(" + taskNum + ") ADD exact with " + ji.crc);
                    iresults.add(new SSImageSearchResult(ji, -1));
                }
                else
                {
                    double td = 0;
                    double top=5;
                    switch(scanType)
                    {
                        case Luma: top=5.0; break;
                        case Mono: top=10.0; break;
                        case Simple: top=10.0; break;
                    }

                    for (int tix = 0; tix < 16 * 16; tix++)
                    {
                        int sbx = (tnbytes[tix] & 0x000000ff);
                        int sgx = (tnbytes[tix] & 0x0000ff00) >> 8;
                        int srx = (tnbytes[tix] & 0x00ff0000) >> 16;
                        int cbx = (ji.thumb[tix] & 0x000000ff);
                        int cgx = (ji.thumb[tix] & 0x0000ff00) >> 8;
                        int crx = (ji.thumb[tix] & 0x00ff0000) >> 16;
                        switch(scanType)
                        {
                            case Mono:
                            {
                                double lums = ((double) srx * 0.21) + ((double) sgx * 0.72) + ((double) sbx * 0.07);
                                double lumc = ((double) crx * 0.21) + ((double) cgx * 0.72) + ((double) cbx * 0.07);
                                td += abs(lums - lumc) / 255.0;
                            }
                            break;
                            case Simple:
                            {
                                int tx = abs(sbx - cbx) + abs(sgx - cgx) + abs(srx - crx);
                                td += (double) tx / 756.0;
                            }
                            break;
                            case Luma:
                            {
                                double dxr = abs(srx - crx) * 0.21;
                                double dxg = abs(sgx - cgx) * 0.72;
                                double dxb = abs(sbx - cbx) * 0.07;
                                double tx = (dxr + dxg + dxb) / 765.0;
                                td += tx;
                            }
                        }
                    }
                    //l.info("Img " + ji.crc + " diff=" + td);
                    if ( td < top )
                    {
                        iresults.add(new SSImageSearchResult(ji, td));
                    }


                }
            }

            @Override
            public void run()
            {
                int sf=0;
                int ff=0;

                l.warn("SEIMG(" + taskNum + ") starting search for similar to " + searchImagePath );

                crcs =  GetCRCAndBytes(Paths.get(searchImagePath).toString());
                tnbytes = GetTnFromFileBytes(crcs.bytes);

                if ( tnbytes == null )
                {
                    l.info("SEIMG(" + taskNum + ") failed to get a thumb, crc=" + crcs.crc + " bytes=" + crcs.bytes.length );
                    finished();
                    return;
                }

                l.info("SEIMG(" + taskNum + ") got image data, crc=" + crcs.crc + " bytes=" + crcs.bytes.length + ", tnbytes=" + tnbytes.length );
                roz.rlib.RTimer et = new  roz.rlib.RTimer();

                for( SSImage ji : images.values() )
                {
                    sf++;
                    if ( state == SSState.Abort )
                        break;

                    getCVal(ji);


                }
                l.warn("SEIMG(" + taskNum + ") sorting results");

                Collections.sort(iresults);
                int ix=0;
                for(SSSearchResult r : iresults)
                {
                    if ( ix++ > 20 )
                        break;
                    addResult(r);
                }
                statusStr = String.format("Searched %d of %d found %d", sf, images.size(), ff);
                et.stop();
                l.warn("SEIMG(" + taskNum + ") finished in " + et  );
                finished();
            }
        }

    }
}
