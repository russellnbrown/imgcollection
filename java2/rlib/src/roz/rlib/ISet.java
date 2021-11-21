package roz.rlib;

import com.twelvemonkeys.image.ResampleOp;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;

import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;

import static javafx.embed.swing.SwingFXUtils.fromFXImage;

/**
 * Created by russellb on 3/08/2016.
 */
public class ISet implements java.io.Serializable
{

    public ISet()
    {
        instance = this;
    }

    private final static SLog l = SLog.GetL();

    private static ISet instance = null;
    public static ISet get() { return instance; }

    public void MarkAllInUse()
    {
        for(IDir d : dirs.values() )
        {
            d.inUse = true;
        }

        for(IFile f : files )
        {
            f.inUse = true;
        }

        for(IImage i : images.values() )
        {
            i.inUse = true;
        }
    }



    public enum ScanType { Simple, Luma, Mono };


    public static int ri=0;
    public static int rf=0;
    public static int raf=0;
    public static int rbf=0;

    public String State()
    {
        return "dirs=" + dirs.size() +
                ", files=" + files.size() +
                ", images=" + images.size();
    }


    public class IImage implements Serializable
    {
        public IImage(long crc, int thumb[])
        {
            this.crc = crc;
            this.thumb = thumb.clone();
        }


        private static final long serialVersionUID = 1L;
        public long     crc;
        public int      thumb[];
        public boolean  inUse = false;
        public boolean  isNew = false;

        @Override
        public String toString()
        {
            return "IImage{" +
                    "crc=" + crc +
                    ", thumb=" + Arrays.toString(thumb) +
                    (isNew?",New":",NotNew") +
                    (inUse?",InUse":",NotInUse") +
                    '}';
        }
    }

    public class IDir
    {
        public long dhash;
        public String path;
        public long mtime;

        public boolean isNew = false;
        public boolean inUse = false;
        public List<IFile> filesInDir = null;

        public IDir(long dhash, String path, long mtime)
        {
            this.dhash = dhash;
            this.path = path;
            this.mtime = mtime;
        }

        @Override
        public String toString()
        {
            return "IDir{" +
                    "GetHash=" + dhash +
                    ", GetLocalPath='" + path + '\'' +
                    ", mtime=" + mtime +
                    (isNew?",New":",NotNew") +
                    (inUse?",InUse":",NotInUse") +
                    '}';
        }
    }

    public class IFile
    {
        public long     dhash;
        public long     crc;
        public String   name;
        public boolean  inUse = false;
        public boolean  isNew = false;

        public String getName() { return name; }
        public long getDHash()
        {
            return dhash;
        }

        public IFile(long dhash, String name, long crc)
        {
            this.dhash = dhash;
            this.name = name;
            this.crc = crc;
        }

        @Override
        public String toString()
        {
            return "IFile{" +
                    "fhash=" + dhash +
                    ", crc=" + crc +
                    ", name='" + name + '\'' +
                    (isNew?",New":",NotNew") +
                    (inUse?",InUse":",NotInUse") +
                    '}';
        }
    }

    public class IInstances {
        public long crc;
        public List<IFile> instances;

        public IInstances(long crc) {
            this.crc = crc;
            instances = new LinkedList<IFile>();
        }
    }

    public Map<Long, IDir> getDirs()
    {
        return dirs;
    }
    public Map<Long, IInstances> getInstances()
    {
        return instances;
    }
    public List<IFile> getFiles()
    {
        return files;
    }
    public Map<Long, IImage> getImages()
    {
        return images;
    }
    private Map<Long, IDir> dirs = new HashMap<Long, IDir>();
    private Map<Long, IInstances> instances = new HashMap<Long, IInstances>();
    private List<IFile> files = new LinkedList<IFile>();
    private Map<Long, IImage> images = new HashMap<Long, IImage>();

    public String GetDirPath(long dhash)
    {
        IDir d = dirs.getOrDefault(dhash,null);
        if ( d == null )
            return null;
        return d.path;
    }
    public String GetDirPath(IFile f)
    {
        return GetDirPath(f.dhash) + "/" + f.name;
    }

    public String GetUrlPath(IFile f)
    {
        File fu = new File(GetDirPath(f));
        try
        {
            return fu.toURI().toURL().toString();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public IDir GetDir(long dhash)
    {
        return dirs.getOrDefault(dhash,null);
    }

    public IFile GetFile(long dhash, String name)
    {
        synchronized(this)
        {
            IDir id = null;
            id = dirs.get(dhash);
            if (id != null && id.filesInDir != null)
            {
                // we have a quick file list created. use that
                for (IFile f : id.filesInDir)
                {
                    if (name.equals(f.name))
                        return f;
                }
                return null;
            }

            for (IFile f : files)
            {
                if (f.dhash == dhash && name.equals(f.name))
                    return f;
            }
            return null;
        }
    }

    public IFile GetFile(INode fileName)
    {
        IDir d = GetDir(fileName.getDirHash());
        return GetFile(fileName.getDirHash(),fileName.getFileName() );
    }

    public IImage GetImage(long crc)
    {
        return images.get(crc);
    }
    public IFile AddFile(INode fileName, long crc)
    {
        synchronized (this)
        {
            IFile f = new IFile(fileName.getDirHash(), fileName.getFileName(), crc);
            files.add(f);
            return f;
        }
    }

    public IDir AddDir(INode path, long utime)
    {
        synchronized(this)
        {
            IDir d = new IDir(path.getDirHash(), path.getDir(), utime);
            dirs.put(path.getDirHash(), d);
            return d;
        }
    }

    public IImage AddImage(long crc, int thumb[])
    {
        synchronized(this)
        {
            IImage i = new IImage(crc, thumb);
            images.put(crc, i);
            return i;
        }
    }

    public static ISet Create()
    {
        ISet s = new ISet();
        return s;
    }

    public static String NormalizePath(Path pa)
    {
        pa.normalize();
        String p = pa.toString();
        p = p.replace('\\','/');
        p = p.replace("//","/");

        int l = p.length();
        if ( p.endsWith("/") )
        {
            p = p.substring(0, l - 1);
        }

        return p;
    }

    public static long DHash(String str)
    {
        long hash = 5381;
        int l = str.length();
        for(int cx=0; cx<l; cx++)
        {
            hash = ((hash << 5) + hash) + str.charAt(cx); /* hash * 33 + c */
        }

        return hash;
    }

    public static long BHash(byte[] bytes)
    {
        long hash = 5381;

        int l = bytes.length;
        for(int cx=0; cx<l; cx++)
        {
            hash = ((hash << 5) + hash) + bytes[cx]; /* hash * 33 + c */
        }

        return hash;
    }

    public static class ICRC
    {
        public ICRC()
        {
        }
        public long crc;
        public byte bytes[];
    }

    public static ICRC GetCRCAndBytes(Path xfile)
    {
        try
        {
            ICRC cr = new ICRC();

            File file = xfile.toFile();
            long filelength = file.length();
            cr.bytes = new byte[(int)filelength];
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(cr.bytes);
            dis.close();

            cr.crc = BHash(cr.bytes);
            return cr;
        }

        catch (Exception ex)
        {
            l.severe("GetCRCAndBytes : error processing " + xfile.toString() + " : " + ex.getMessage());
        }

        return null;
    }

    public static int[] GetTnFromFileBytes(byte []fileBytes)
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

        return null;
    }


    public static BufferedImage Resize(BufferedImage image, Dimension d)
    {
        //System.out.printf("Resizing image " + image.getWidth() + "," + image.getHeight());

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

    public static BufferedImage ResizeInt(BufferedImage image, Dimension d)
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


    public void SaveFC(Path s)
    {
        try
        {
            Path pdir = s.resolve("directory3.txt");
            Path pfile = s.resolve("files3.txt");
            Path puniq = s.resolve("uniq3.bin");

            BufferedWriter fdirs;
            BufferedWriter ffiles;
            FileOutputStream  fout;
            FileChannel funiq;

            fdirs = Files.newBufferedWriter(pdir,  StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            ffiles = Files.newBufferedWriter(pfile, StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            fout = new FileOutputStream(puniq.toString()) ;// RandomAccessFile (puniq.toString(),"rw");
            funiq = fout.getChannel();

            fdirs.write(INode.GetRoot());
            fdirs.newLine();

            for(IDir d : dirs.values() )
            {
                if ( d.inUse)
                {
                    String rel = INode.relativeToRoot(d.path);
                    String st = String.format("%d,%d,%s", d.dhash, d.mtime, rel);
                    fdirs.write(st);
                    fdirs.newLine();
                }
            }
            for(IFile f : files )
            {
                if ( f.inUse )
                {
                    String st = String.format("%d,%d,%s", f.dhash, f.crc, f.name);
                    ffiles.write(st);
                    ffiles.newLine();
                }
            }


            ByteBuffer ibuf = ByteBuffer.allocateDirect(8+4*16*16);

            for(IImage i : images.values() )
            {
                if ( !i.inUse )
                    continue;

                if ( i.thumb == null )
                {
                    l.severe("ERR Saving img " + i + " thumb is null");
                    continue;
                }
                if ( i.thumb.length != 16*16 )
                {
                    l.severe("ERR Saving img " + i + " length not rigth: " + i.thumb.length );
                    continue;
                }
                l.low("Saving img " + i );
                ibuf.rewind();
                ibuf.putLong(i.crc);

                int by = 0;
                for(int b=0; b<16*16; b++)
                {
                    ibuf.putInt(i.thumb[by]);
                    by++;
                }
                ibuf.rewind();
                funiq.write(ibuf);
            }

            fdirs.close();
            ffiles.close();
            funiq.close();
            fout.close();
        }

        catch(IOException ioe)
        {
            l.severe(String.format("Caught IOE : %s in save" , ioe.toString()) );
            ioe.printStackTrace();

        }
    }


    public void SaveAndArchive(String arg)
    {
        Path saveAs = Paths.get(arg + ".new");
        Path archiveAs = Paths.get(arg + ".old");
        Path setAs = Paths.get(arg);

        try
        {

            if (!Files.exists(saveAs))
            {
                l.warn(String.format("SCAN: Creating set %s", saveAs));
                Files.createDirectory(saveAs);
            }

            else
                l.warn(String.format("SCAN: Set %s exists.", saveAs));

            SaveFC(saveAs);

            if ( Files.exists(archiveAs))
            {
                Files.delete(Paths.get(archiveAs.toString() + "/directory3.txt"));
                Files.delete(Paths.get(archiveAs.toString() + "/files3.txt"));
                Files.delete(Paths.get(archiveAs.toString() + "/uniq3.bin"));
                Files.delete(archiveAs);
            }
            Files.move(setAs, archiveAs);
            Files.move(saveAs, setAs);
            l.warn("Set updated, Old copy in " + archiveAs.toString());
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }

    }


    public void test12()
    {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPEG");
        while (readers.hasNext()) {
            System.out.println("reader: " + readers.next());
        }
    }

    public boolean Valid(Path s)
    {
        Path pdir = s.resolve("directory3.txt");
        Path pfile = s.resolve("files3.txt");
        Path puniq = s.resolve("uniq3.bin");

        if ( !pdir.toFile().exists() )
            return false;
        if ( !pfile.toFile().exists() )
            return false;
        if ( !puniq.toFile().exists() )
            return false;

        return true;
    }


    public void Load(Path s, boolean withInstances) throws ImException
    {
        try
        {
            roz.rlib.RTimer et = new roz.rlib.RTimer();

            l.warn("Open JSet " + s.toString());
            Path pdir = s.resolve("directory2.txt");
            Path pfile = s.resolve("files2.txt");
            Path puniq = s.resolve("uniq2.bin");

            BufferedReader fdirs;
            BufferedReader ffiles;
            ObjectInputStream funiq;
            FileInputStream fin = new FileInputStream(puniq.toString());
            funiq =  new ObjectInputStream(fin);

            l.info("Loading set...");

            fdirs = Files.newBufferedReader(pdir);
            ffiles = Files.newBufferedReader(pfile);

            l.warn("Load start..");
            String line = null;
            while ((line = fdirs.readLine()) != null)
            {
                int cpos1 = line.indexOf((int)',');
                int cpos2 = line.indexOf((int)',', cpos1+1);

                String sdhash = line.substring(0, cpos1);
                String stime = line.substring(cpos1+1,cpos2);
                String spath = line.substring(cpos2+1);

                try
                {
                    Long t = Long.parseLong(stime);
                    Long d = Long.parseLong(sdhash);
                    IDir jd = new IDir(d, spath, t);
                    dirs.put(jd.dhash, jd);
                }
                catch(NumberFormatException nfe)
                {
                    l.fatal("NUmber ex reading dirs" + nfe.getMessage() + line );
                }
            }

            l.warn(dirs.size() + " dirs, " + et );
            while ((line = ffiles.readLine()) != null)
            {
                int cpos1 = line.indexOf((int)',');
                int cpos2 = line.indexOf((int)',', cpos1+1);
                String sfhash = line.substring(0, cpos1);
                String scrc = line.substring(cpos1+1,cpos2);
                String sname = line.substring(cpos2+1);

                try
                {
                    long dhash = Long.parseLong(sfhash);
                    long crc = Long.parseLong(scrc);
                    IFile jf = new IFile(dhash, sname, crc);
                    files.add(jf);
                }
                catch(NumberFormatException nfe)
                {
                    l.fatal("NUmber ex reading files" + nfe.getMessage() + line );
                }


            }

            l.warn(files.size() + " files, " + et );
            try
            {
                while(fin.available()>0)
                {
                    long crc = funiq.readLong();
                    int []buf = new int[16*16];
                    for(int px = 0; px < 16*16; px++)
                    {
                        buf[px] = funiq.readInt();
                    }
                    IImage ji = new IImage(crc,buf);
                    images.put(crc, ji);
                }
            }
            catch(IOException e)
            {

            }
            l.warn(images.size() + " images." + et  );


            fdirs.close();
            ffiles.close();
            funiq.close();
            fin.close();

            if ( withInstances )
            {
                for(IFile f : files  )
                {
                    IInstances ii = instances.get(f.crc);
                    if ( ii == null )
                    {
                        ii = new IInstances(f.crc);
                        instances.put(ii.crc, ii);
                    }
                    ii.instances.add(f);
                }
            }

            et.stop();
            l.warn("Load finished. " + et  );
        }

        catch(IOException ioe)
        {
            l.severe(String.format("Caught IOE : %s in save" , ioe.toString()) );
            throw new ImException("Can't Open Set");
        }
    }


    public void Load2(Path s, boolean withInstances) throws ImException
    {
        try
        {
            roz.rlib.RTimer et = new roz.rlib.RTimer();

            l.warn("Open JSet " + s.toString());
            Path pdir = s.resolve("directory2.txt");
            Path pfile = s.resolve("files2.txt");
            Path puniq = s.resolve("uniq2.bin");

            BufferedReader fdirs;
            BufferedReader ffiles;
            DataInputStream funiq;
            FileInputStream fin = new FileInputStream(puniq.toString());
            funiq =  new DataInputStream(fin);

            l.info("Loading set...");

            fdirs = Files.newBufferedReader(pdir);
            ffiles = Files.newBufferedReader(pfile);

            l.warn("Load start..");
            String line = null;
            while ((line = fdirs.readLine()) != null)
            {
                int cpos1 = line.indexOf((int)',');
                int cpos2 = line.indexOf((int)',', cpos1+1);

                String sdhash = line.substring(0, cpos1);
                String stime = line.substring(cpos1+1,cpos2);
                String spath = line.substring(cpos2+1);

                try
                {
                    Long t = Long.parseLong(stime);
                    Long d = Long.parseLong(sdhash);
                    IDir jd = new IDir(d, spath, t);
                    dirs.put(jd.dhash, jd);
                }
                catch(NumberFormatException nfe)
                {
                    l.fatal("NUmber ex reading dirs" + nfe.getMessage() + line );
                }
            }

            et.stop();
            l.warn(dirs.size() + " dirs, " + et  );
            while ((line = ffiles.readLine()) != null)
            {
                int cpos1 = line.indexOf((int)',');
                int cpos2 = line.indexOf((int)',', cpos1+1);
                String sfhash = line.substring(0, cpos1);
                String scrc = line.substring(cpos1+1,cpos2);
                String sname = line.substring(cpos2+1);

                try
                {
                    long dhash = Long.parseLong(sfhash);
                    long crc = Long.parseLong(scrc);
                    IFile jf = new IFile(dhash, sname, crc);
                    files.add(jf);
                }
                catch(NumberFormatException nfe)
                {
                    l.fatal("NUmber ex reading files" + nfe.getMessage() + line );
                }


            }

            et.stop();
            l.warn(files.size() + " files, " + et  );
            try
            {
                while(fin.available()>0)
                {
                    long crc = funiq.readLong();
                    int []buf = new int[16*16];
                    for(int px = 0; px < 16*16; px++)
                    {
                        buf[px] = funiq.readInt();
                    }
                    IImage ji = new IImage(crc,buf);
                    images.put(crc, ji);
                }
            }
            catch(IOException e)
            {

            }
            l.warn(images.size() + " images." + et  );

            fdirs.close();
            ffiles.close();
            funiq.close();
            fin.close();

            if ( withInstances )
            {
                for(IFile f : files  )
                {
                    IInstances ii = instances.get(f.crc);
                    if ( ii == null )
                    {
                        ii = new IInstances(f.crc);
                        instances.put(ii.crc, ii);
                    }
                    ii.instances.add(f);
                }
            }

            l.warn("Load finished. " + et  );
        }

        catch(IOException ioe)
        {
            l.severe(String.format("Caught IOE : %s in save" , ioe.toString()) );
            throw new ImException("Can't Open Set");
        }
    }


    private String rootStr = "";
    public String GetRoot() { return rootStr; }



    public void LoadFC(Path s, boolean withInstances, boolean withQFile) throws ImException
    {
        try
        {
            roz.rlib.RTimer tt = new roz.rlib.RTimer();
            roz.rlib.RTimer lt = new roz.rlib.RTimer();

            l.warn("Open JSet " + s.toString());

            Path pdir = s.resolve("directory3.txt");
            Path pfile = s.resolve("files3.txt");
            Path puniq = s.resolve("uniq3.bin");

            BufferedReader fdirs;
            BufferedReader ffiles;
            FileInputStream  fin;
            FileChannel funiq;

            fin = new FileInputStream(puniq.toString());
            funiq =  fin.getChannel();

            fdirs = Files.newBufferedReader(pdir);
            ffiles = Files.newBufferedReader(pfile);

            String line;
            line = fdirs.readLine();
            rootStr = line;

            while ((line = fdirs.readLine()) != null)
            {
                int cpos1 = line.indexOf((int)',');
                int cpos2 = line.indexOf((int)',', cpos1+1);

                String sdhash = line.substring(0, cpos1);
                String stime = line.substring(cpos1+1,cpos2);
                String spath = "";
                if ( line.length() > cpos2+1 )
                    spath = rootStr + "/" + line.substring(cpos2+1);
                spath = Utils.StandardizePath(spath);

                try
                {
                    Long t = Long.parseLong(stime);
                    Long d = Long.parseLong(sdhash);
                    IDir jd = new IDir(d, spath, t);
                    if ( withQFile )
                        jd.filesInDir = new LinkedList<IFile>();

                    dirs.put(jd.dhash, jd);
                }
                catch(NumberFormatException nfe)
                {
                    l.fatal("Number ex reading dirs" + nfe.getMessage() + line );
                }
            }

            l.warn("Dirs  : " + dirs.size() + " in: " + tt );
            tt.reset();

            while ((line = ffiles.readLine()) != null)
            {
                int cpos1 = line.indexOf((int)',');
                int cpos2 = line.indexOf((int)',', cpos1+1);
                String sfhash = line.substring(0, cpos1);
                String scrc = line.substring(cpos1+1,cpos2);
                String sname = line.substring(cpos2+1);

                try
                {
                    long dhash = Long.parseLong(sfhash);
                    long crc = Long.parseLong(scrc);
                    IFile jf = new IFile(dhash, sname, crc);
                    files.add(jf);

                    if ( withQFile )
                    {
                        IDir id = dirs.get(dhash);
                        if ( id != null )
                        {
                            synchronized (id.filesInDir)
                            {
                                id.filesInDir.add(jf);
                            }
                        }
                        else
                            l.severe("File for no dir!");
                    }
                }
                catch(NumberFormatException nfe)
                {
                    l.fatal("NUmber ex reading files" + nfe.getMessage() + line );
                }


            }

            l.warn("Files : " + files.size() + " in: " + tt  );
            tt.reset();
            ByteBuffer ibuf = ByteBuffer.allocateDirect(8+4*16*16);
            try
            {
                while(fin.available()>0)
                {
                    ibuf.rewind();
                    funiq.read(ibuf);
                    ibuf.rewind();
                    long crc = ibuf.getLong();
                    int []buf = new int[16*16];
                    for(int px = 0; px < 16*16; px++)
                    {
                        buf[px] = ibuf.getInt();
                    }
                    IImage ji = new IImage(crc,buf);
                    images.put(crc, ji);
                }
            }
            catch(IOException e)
            {

            }
            l.warn("Images: " + images.size() + " in: " + tt  );
            tt.reset();
            fdirs.close();
            ffiles.close();
            funiq.close();
            fin.close();

            if ( withInstances )
            {
                for(IFile f : files  )
                {
                    IInstances ii = instances.get(f.crc);
                    if ( ii == null )
                    {
                        ii = new IInstances(f.crc);
                        instances.put(ii.crc, ii);
                    }
                    ii.instances.add(f);
                }
            }

            l.warn("Instances created in: " + tt  );
            l.warn("Total load time     : " + lt  );
        }

        catch(IOException ioe)
        {
            l.severe(String.format("Caught IOE : %s in save" , ioe.toString()) );
            throw new ImException("Can't Open Set");
        }
    }


    public void Dump(boolean changesOnly)
    {
        l.info("Dirs:");
        for(IDir d : dirs.values() )
        {
            if ( !changesOnly || d.isNew || !d.inUse )
                l.info(d.toString());
        }

        l.info("Files:");
        for(IFile f : files )
        {
            if ( !changesOnly || f.isNew || !f.inUse )
                l.info(f.toString());
        }

        l.info("Images:");
        for(IImage i : images.values() )
        {
            if ( !changesOnly || i.isNew || !i.inUse )
                l.info(i.toString());
        }

    }



    public javafx.scene.image.Image GetImage(ISet.IFile jf)
    {
        String p = GetDirPath(jf);//GetDir(jf.GetDirHash()).GetLocalPath + "/" + jf.GetName();
        File f = new File(p);

        try
        {
            javafx.scene.image.Image i =  new javafx.scene.image.Image(f.toURI().toURL().toExternalForm());
            return i;
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        return null;

    }



    public javafx.scene.image.Image GetThumbnail(long ikey)
    {
        IImage ji = GetImage(ikey);

        WritableImage wImage = new WritableImage(16,16);
        PixelWriter pixelWriter = wImage.getPixelWriter();

        pixelWriter.setPixels(0,0,16,16, PixelFormat.getIntArgbInstance(),ji.thumb,0, 16 );

        return wImage;
    }



    public List<IFile> GetIFilesForCrc(long crc, boolean oneOnly)
    {
        List<IFile> files = new LinkedList<IFile>();
        for (ISet.IFile f : getFiles() )
        {
            if (f.crc == crc)
            {
                files.add(f);
                if ( oneOnly )
                    return files;
            }
        }
        return files;
    }

    public int[] GetThumbBytes(javafx.scene.image.Image i)
    {
        BufferedImage bi=fromFXImage(i,null);
        BufferedImage tnbi = Resize(bi, new Dimension(16,16));
        int rgbx[] = tnbi.getRGB(0, 0, tnbi.getWidth(), tnbi.getHeight(), null, 0, tnbi.getWidth());
        return rgbx;
    }


    public void RemoveFile(IFile f)
    {
        synchronized (files)
        {
            files.remove(f);
        }
    }

    public void convert4()
    {

        for( IDir d : getDirs().values() )
        {
            String path = d.path;
            DirHelper dh = new DirHelper(path);
            System.out.printf("In: GetLocalPath %s hash %d out %d%n", path, d.dhash, dh.GetHash() );
            d.dhash = dh.GetHash();
        }
    }

    public void SaveFC4(String s)
    {
    }



}
