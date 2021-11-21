/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arenbee.jfxview;

import com.twelvemonkeys.image.ResampleOp;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.zip.CRC32;

//import com.mortennobel.imagescaling.ResampleOp;

/**
 *
 * @author russellb
 */
public class Image
{
    private final static Logger l = Logger.getLogger(Image.class.getName()); 
    private static CRC32 crc = new CRC32();
    
    public enum HashType { CRC, QHASH, BHASH, HASHCODE, NONE };
    public enum ThumbType { INTERNAL, SCALR, IMAGESCALING, NONE };
    
    public static class CRCGet
    {

        private long filelength;
        public CRCGet()
        {
        }
        public long crc;
        public byte bytes[];
        public ImageIcon thumb;
        public Dimension size;
    }
    
    
    /*
    public static CRCGet getDummyCRCAndBytes(Path file)
    {
        try
        {            
            CRCGet cr = new CRCGet();
            byte rb=0;
            
            long blen = blen = 78592;
            cr.bytes = new byte[(int)blen];         
            for(int b=0; b<blen; b++)
            {
                cr.bytes[b] =  rb++;
                if ( rb == 120 )
                    rb = 0;
            }
            cr.crc = Utils.BHash(cr.bytes); 
            return cr;
        }
        
        catch (Exception ex) 
        {
            l.severe(ex.getMessage());
        }
        
        return null;
    }    
    */
 
    
/*
    public static CRCGet getCRCAndBytes(Path file)
    {
        try
        {
            RandomAccessFile raf = new RandomAccessFile(file.toFile(),"r");
            CRCGet cr = new CRCGet();
 
            long blen = blen = raf.length();
            cr.bytes = new byte[(int)blen];
            raf.read(cr.bytes);
            raf.close();
           
            
           // CRC32 crc = new CRC32();    
            
           cr.crc = 0;
           crc.update(cr.bytes, 0, (int)blen);
           cr.crc = crc.getValue();
           
           
            return cr;
        }
        
        catch (Exception ex) 
        {
            l.severe(ex.getMessage());
        }
        
        return null;
    }    
    */
    
    
    public static CRCGet getHashAndBytes(Path xfile, HashType htype)
    {
        try
        {          
            CRCGet cr = new CRCGet();
       
            File file = xfile.toFile();
            cr.filelength = file.length();
            cr.bytes = new byte[(int)cr.filelength];
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(cr.bytes);
            dis.close();    
            
            switch(htype)
            {
                case QHASH:  cr.crc = 5381; for(int cx=0; cx<cr.filelength; cx++)  cr.crc = ((cr.crc * 31 ) + cr.crc)  + cr.bytes[cx] ;  break;
                case BHASH: cr.crc = Utils.BHash(cr.bytes); break;
                case HASHCODE: Arrays.hashCode(cr.bytes); break;
                case CRC:  cr.crc = 0; crc.update(cr.bytes, 0, (int)cr.filelength);  cr.crc = crc.getValue(); break;
                case NONE: break;
            }
            return cr;
        }
        
        catch (Exception ex) 
        {
            l.severe(ex.getMessage());
        }
        
        return null;
    }
 
    public static boolean makeThumbFromBytes(CRCGet c, int size)
    {
        InputStream in = new ByteArrayInputStream(c.bytes);
        BufferedImage image = null;
        try
        {
            image = ImageIO.read(in);
            c.size = new Dimension(image.getWidth(),image.getHeight());
        } 
        catch (IOException ex)
        {
            l.severe("IOError:" + ex.toString());
            return false;
        }
        catch (Exception ex)
        {
            l.severe("Error:" + ex.toString());
            return false;
        }
  
        ResampleOp  resampleOp = new ResampleOp (size,size);
        BufferedImage thumbi = resampleOp.filter(image, null);
        c.thumb =  new ImageIcon(thumbi);
        return true;
    }
    
    
    /*
    public static CRCGet getCRCAndThumb(Path file )
    {
        try
        {
            RandomAccessFile raf = new RandomAccessFile(file.toFile(),"r");
            CRCGet cr = new CRCGet();
 
            long blen = blen = raf.length();
            cr.bytes = new byte[(int)blen];
            raf.read(cr.bytes);
            raf.close();
            
            CRC32 crc = new CRC32();    
            
            crc.update(cr.bytes, 0, (int)blen);
            cr.crc = crc.getValue();
          
   
            BufferedImage image;
            try 
            {
                InputStream in = new ByteArrayInputStream(cr.bytes);
                image  = ImageIO.read(in);
                ResampleOp  resampleOp = new ResampleOp (64,64);
                BufferedImage thumbi = resampleOp.filter(image, null);
                cr.size = new Dimension(image.getWidth(), image.getHeight());
                cr.thumb =  new ImageIcon(thumbi);
           
                
            } 
            catch (IOException ex) 
            {
                l.severe(ex.toString());
            }
            catch ( Exception e)
            {
                l.severe(e.toString());            
            }         

            
            return cr;
        }
        
        catch (Exception ex) 
        {
            l.severe(ex.getMessage());
        }
        
        return null;
    }*/
    
    /*
    public static long getCRC(String file)
    {
        
        long testLen = 5000;
        
        try 
        {
            File i = new File(file);
            RandomAccessFile raf = new RandomAccessFile(file,"r");
            long fl = (int)i.length();
            long start = (fl /  2) - 2500;
            long len = 5000;
            
            if ( fl < testLen )
            {
                start = 0;
                len = fl;
            }
            
            byte  buf[] = new byte[(int)len];
            raf.seek(start);
            raf.read(buf);
            raf.close();
            
            CRC32 crc = new CRC32();
           
            crc.update(buf, 0, (int)len);
       
            long v = crc.getValue();
            
           return v;
        
        } 
        catch (Exception ex) 
        {
            l.severe(ex.getMessage());
        }
     
        return 0;
        
    }*/
    
    
    public static int[] GetTn(byte []fileBytes, ThumbType ttype)
    {
        int rgb[] = new int[16*16];
        for(int ix=0; ix<16*16; ix++)
            rgb[ix]=0;
        
        if ( ttype == ThumbType.NONE )
        {
            return rgb;
        }
        
        if ( fileBytes == null )
        {
            l.severe("GetTN No bytes given");
            return rgb;
        }
        
        BufferedImage image;
        try 
        {
            InputStream in = new ByteArrayInputStream(fileBytes);
            image  = ImageIO.read(in);
            if ( image == null )
            {
                l.severe("GetTN No image returned ");
                return rgb;
            }
           

            BufferedImage thumbi=null;
            
            switch(ttype)
            {
                case INTERNAL:
                {
                    thumbi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = thumbi.createGraphics();
                    g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
                    g.dispose();
                }
                break;
                case SCALR: 
                    thumbi = Scalr.resize(image, Scalr.Method.SPEED, Scalr.Mode.FIT_EXACT , 16,16, Scalr.OP_ANTIALIAS); 
                    break;
                case IMAGESCALING: 
                {
                    ResampleOp  resampleOp = new ResampleOp (16,16);
                    thumbi = resampleOp.filter(image, null);
                }
                break;
            }

            if ( thumbi == null )
            {
                l.severe("TNI is NULL");
                return null;
            }

            //rgb = thumbi.getRGB(0, 0, 16, 16, null, 0, 16);
            thumbi.getRGB(0, 0, 16, 16, rgb, 0, 16);

           // int ix=0;


           // System.out.printf("Bytes are:-%n");
           // for(int x : rgbx )
           //     System.out.printf("X %d = %s %n", ix++, Integer.toHexString(x) );

            // 16*16 = 256 integer values  0xaarrggbb

            return rgb;
        } 
        catch (IOException ex) 
        {
            l.severe(ex.toString());
        }
        catch ( Exception e)
        {
            l.severe(e.toString());            
        } 
        
        return rgb;
    }    
    /*
    public static int[] GetDummyTn(byte []fileBytes)
    {
        try 
        {
            int rgb[] = new int[16*16*4];
            return rgb;
        } 
        
        catch ( Exception e)
        {
            l.severe(e.toString());            
        }
        return null;
    }      
    */
    
    public static UUID FileGuid(Path p, long crc)
    {
      //  String file = p.getFileName().toString();

        long lower = crc;
        long upper = Utils.PHash(p);

        UUID l = new UUID(upper,lower);

        return l;
    }
    
    public static long CrcFromGuid(UUID guid)
    {
        long lower = guid.getLeastSignificantBits();
        //long upper = guid.getMostSignificantBits();
        return lower;
    }    
   
    public static long NameHashFromGuid(UUID guid)
    {
       // long lower = guid.getLeastSignificantBits();
        long upper = guid.getMostSignificantBits();
        return upper;
    }    

    public static BufferedImage Resize(BufferedImage image, Dimension d, ThumbType method)
    {
        BufferedImage thumbi=null;

        switch(method)
        {
            case INTERNAL:
            {
                thumbi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = thumbi.createGraphics();
                g.drawImage(image, 0,0, image.getWidth(), image.getHeight(), null);
                g.dispose();
            }
            break;
            case SCALR: 
                thumbi = Scalr.resize(image, Scalr.Method.SPEED, Scalr.Mode.FIT_EXACT , d.width, d.height, Scalr.OP_ANTIALIAS); 
                break;
            case IMAGESCALING: 
            {
                ResampleOp  resampleOp = new ResampleOp (d.width, d.height);
                thumbi = resampleOp.filter(image, null);
            }
            break;
        }

        if ( thumbi == null )
        {
            l.severe("TNI is NULL");
            return null;
        }
        return thumbi;
    }
}
  