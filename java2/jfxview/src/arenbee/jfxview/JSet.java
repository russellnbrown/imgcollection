
package arenbee.jfxview;

/*
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class JSet
{
    private final static SLog l = SLog.GetL();
    
    private HashMap<Long, jdir> dirs = new HashMap<Long, jdir>();
    private HashMap<UUID, jfile> files = new HashMap<UUID, jfile>();
    private HashMap<Long, jimage> images = new HashMap<Long, jimage>();

    public HashMap<Long, jdir> Dirs() { return dirs; }
    public HashMap<UUID, jfile> Files() { return files; }
    public HashMap<Long, jimage> Images() { return images ; }
    
    public static final int tnSize = 16;
    
    public JSet()
    {
    }

    public String State()
    {
        return "JSET[dirs=" + dirs.size() + ", files=" + files.size() + ", images=" + images.size() + "]";
    }

    public synchronized void MarkFilesInUse(jdir jd, boolean b)
    {
        try
        {
            for(jfile f : files.values() )
            {
                if ( f.GetHash == jd.GetHash )
                {
                    f.inuse = b;
                    jimage i = images.get(Image.CrcFromGuid(f.guid));
                    if ( i == null )
                    {
                        l.severe("Cant fing image for " + f.toString() + " crc= " + Image.CrcFromGuid(f.guid) );
                        continue;
                    }
                    try
                    {
                    i.inuse = true;
                    }
                    catch(Exception e2)
                    {
                        l.severe(e2.toString() + " f is " + f.toString() );
                    }
                }
            }
        }
        
        catch(Exception e)
        {
            l.severe("Mark all used err:" + e.getMessage()  );  
        }
    }
    
    public class jimage
    {
        public long      icrc;
        public int[]     thumb;
        public boolean   inuse;
        public jimage(long _icrc, int []_thumb)
        {
            inuse = true;
            icrc = _icrc;
            thumb = _thumb;
        }
        public String toString() 
        { 
            Long ck = 0L;
            for(int i : thumb)
            {
                ck += i & 0x00ffffff; 
            }
            return "IMG[ikey=" + icrc + ", inUse=" + inuse + ", check=" + ck + "]";
        }
    }
    
    public class jdir
    {
        public jdir(long h, Path p, Long t) 
        {
            GetHash = h; GetLocalPath = p; lastModified = t; inuse = true;
        }
        public long  GetHash;
        public Path GetLocalPath;
        public Long lastModified;
        public boolean   inuse;
        public String toString() { return "DIR[hash=" + GetHash + ", inUse=" + inuse + ",GetLocalPath=" + GetLocalPath.toString() + "]"; }
    }
    
    public class jfile
    {
        public jfile(UUID g, Path fullPath) 
        { 
            guid = g;
            name = fullPath.getFileName().toString();
            
            Path dir  = fullPath.getParent();
            GetHash = Utils.PHash(dir);
            inuse = true;
            
            jdir jd = GetJDir(GetHash);
            if ( jd == null )
            {
                jd = AddDir(GetHash,dir, jd.lastModified);
            }
            GetHash = jd.GetHash;
        }
        
        public jfile(Long d, UUID g, Path fileName) 
        { 
            guid = g;
            GetHash = d;
            name = fileName.toString();
            inuse = true;
        }
         
        
        public UUID     guid;
        public long     GetHash;
        public String   name;
        public boolean   inuse;
        
        public String toString() 
        { 
            return "FIL[guid=" + guid + ", ikey=" + Image.CrcFromGuid(guid) + ", GetHash=" + GetHash + ", inUse=" + inuse + ", name=" + name + "]";
        }
    }

    public jdir GetJDir(Path p)
    {
        long GetHash = Utils.PHash(p);
        if ( dirs.containsKey(GetHash))
            return dirs.get(GetHash);
        return null;
    }
    
    public jdir GetJDir(long GetHash)
    {
        if ( dirs.containsKey(GetHash))
            return dirs.get(GetHash);
        return null;
    }  

    public  String GetFilePath(jfile f)
    {
        jdir d = GetJDir(f.GetHash);
        return d.GetLocalPath.toString() + "/" + f.name;
    }

    public jdir AddDir(long hash, Path d, Long t) 
    {
        if ( dirs.containsKey(hash) )
            return dirs.get(hash);
        jdir jsd = new jdir(hash, d, t);
        dirs.put(hash, jsd);
        return jsd;
    }
   
    public jfile AddFile(UUID guid, Path p) 
    {
        if ( files.containsKey(guid))
            return files.get(guid);
        
        jfile jf = new jfile(guid, p);
        files.put(guid, jf);
        return jf;
    }
    
    public void AddImage(long icrc, int[] tn)
    {
        if ( images.containsKey(icrc) )
            return;
        
        jimage ji = new jimage(icrc, tn);
        images.put(icrc, ji);
        
    }
 
    public jimage GetImage(long crc)
    {
        if ( images.containsKey(crc))
            return images.get(crc);
        return null;
    }
    
    public jfile GetFile(UUID guid) 
    {
        if ( files.containsKey(guid))
            return files.get(guid);
        return null;
    }
    
    public static BufferedImage GetJImageThumbnail(jimage i)
    {
        BufferedImage bi = new BufferedImage(16,16,BufferedImage.TYPE_INT_RGB);
        bi.setRGB(0,0,16,16,i.thumb,0, 16);
        return bi;
    }





    public void Dump()
    {
        l.info("Dirs:");
        for(jdir d : dirs.values() )
        {
            l.info(d.toString());
        }

        l.info("Files:");
        for(jfile f : files.values() )
        {
            l.info(f.toString());
        }
        
        l.info("Images:");
        for(jimage i : images.values() )
        {
            l.info(i.toString());
        }

    }
   
    public void MarkInUse(boolean tf)
    {
        for(jdir d : dirs.values() )
            d.inuse = tf;

        for(jfile f : files.values() )
            f.inuse = tf;

        for(jimage i : images.values() )
            i.inuse = tf;
    }

    public void Save(Path s)
    {
        try
        {
            Path pdir = s.resolve("directory.txt");
            Path pfile = s.resolve("files.txt");
            Path puniq = s.resolve("uniq.bin");

            BufferedWriter fdirs;
            BufferedWriter ffiles;
            ObjectOutputStream funiq;

            fdirs = Files.newBufferedWriter(pdir,  StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            ffiles = Files.newBufferedWriter(pfile, StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            FileOutputStream fout = new FileOutputStream(puniq.toString());
            funiq =  new ObjectOutputStream(fout);

            
            for(jdir d : dirs.values() )
            {
                if ( !d.inuse )
                    continue;
                String st = String.format("%d,%d,%s", d.GetHash, d.lastModified, d.GetLocalPath.toString() );
                fdirs.write(st);
                fdirs.newLine();
            }

            for(jfile f : files.values() )
            {
                if ( !f.inuse )
                    continue;
                String st = String.format("%d,%s,%s", f.GetHash, f.guid.toString(), f.name );
                ffiles.write(st);
                ffiles.newLine();          
            }

            for(jimage i : images.values() )
            {
                if ( !i.inuse )
                    continue;
                if ( i.thumb == null )
                {
                    l.severe("ERR Saving img " + i.icrc + " thumb is null");
                    continue;
                }
                if ( i.thumb.length != 16*16 )
                {
                    l.severe("ERR Saving img " + i.icrc + " length not rigth: " + i.thumb.length );
                    continue;
                }
                l.low("Saving img " + i.icrc );
                funiq.writeLong(i.icrc);
                int by = 0;
                for(int b=0; b<16*16; b++)
                {
                    funiq.writeInt(i.thumb[by]);
                    by++;
                }
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
    
    public void Load(Path s) throws ImException
    {
        try
        {
            l.warn("Open JSet " + s.toString());
            Path pdir = s.resolve("directory.txt");
            Path pfile = s.resolve("files.txt");
            Path puniq = s.resolve("uniq.bin");

            BufferedReader fdirs;
            BufferedReader ffiles;
            ObjectInputStream funiq;
            FileInputStream fin = new FileInputStream(puniq.toString());
            funiq =  new ObjectInputStream(fin);
            
            l.warn("Loading set...");

            fdirs = Files.newBufferedReader(pdir);
            ffiles = Files.newBufferedReader(pfile);

            l.warn("dirs...");
            String line = null;
            while ((line = fdirs.readLine()) != null) 
            {
                int cpos1 = line.indexOf((int)',');
                int cpos2 = line.indexOf((int)',', cpos1+1);

                String sdhash = line.substring(0, cpos1);
                String stime = line.substring(cpos1+1,cpos2);
                String spath = line.substring(cpos2+1);
                
                Long t = Long.parseLong(stime);
                jdir jd = new jdir(Long.parseLong(sdhash), Paths.get(spath), t);
                dirs.put(jd.GetHash, jd);
            }
            
            l.warn(dirs.size() + " dirs, files...");
            while ((line = ffiles.readLine()) != null) 
            {
                int cpos1 = line.indexOf((int)',');
                int cpos2 = line.indexOf((int)',', cpos1+1);
                String sdhash = line.substring(0, cpos1);
                String sguid = line.substring(cpos1+1,cpos2);
                String spath = line.substring(cpos2+1);
                
                Long d = Long.parseLong(sdhash);
                UUID g = UUID.fromString(sguid);
                
                Path p = Paths.get(spath);
                l.low("GT - UUID for " + p.getFileName().toString() + " is " + g + ", crc is " + Image.CrcFromGuid((g)));
                
                jfile jf = new jfile(d, g, Paths.get(spath));
                files.put(jf.guid, jf);
            }
     
            l.warn(files.size() + " dirs, images...");
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
                    jimage ji = new jimage(crc,buf);
                   // l.warning("Read 1 bin line");
                    images.put(crc, ji);
                }
            }
            catch(IOException e)
            {
               
            }
            l.warn(images.size() + " images.");
            

            
            fdirs.close();
            ffiles.close();
           // funiqt.close();
            funiq.close();
            fin.close();
        }
        
        catch(IOException ioe)
        {
            l.severe(String.format("Caught IOE : %s in save" , ioe.toString()) );
            throw new ImException("Can't Open Set");
        }
    }
    
    
    
    
}
*/