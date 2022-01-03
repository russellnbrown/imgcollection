
package arenbee.api;


//import com.twelvemonkeys.image;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

// Image. Helpers for dealing with images
public class Image
{

    // SSCRC. Used to return all we ever need from an image
    public static class SSCRC
    {
        public SSCRC()
        {
        }
        public long crc;        // crc32 hash of file data
        public byte bytes[];    // the file data
        public byte thumb[];    // a thumb of the image
    }

    // GetTnFromFileBytes. Create a 16x16 thumbnail from the data 
    // in a file
    public static byte[] GetTnFromFileBytes(byte[] fileBytes)
    {

        Logger.Debug("GetTnFromFileBytes processing ");

        // Create a stream from the image bytes and use that to
        // create a javax image
        BufferedImage image;
        try
        {
            InputStream in = new ByteArrayInputStream(fileBytes);
            image = ImageIO.read(in);
            if (image == null)
            {
                Logger.Debug("GetTN No image returned ");
                return null;
            }

            // Create a thumbnail image from the main image
            BufferedImage thumbi = Resize(image, new Dimension(16, 16));
            if (thumbi == null)
            {
                Logger.Debug("TNI is NULL");
                return null;
            }

            // now get the RGB values from that thumbnail image
            int irgb[] = thumbi.getRGB(0, 0, 16, 16, null, 0, 16);
            if (irgb.length == 256) // ARGB
            {
                int pb = 0;
                byte rgb[] = new byte[16 * 16 * 3];
                for (int RGB : irgb)
                {
                    int red = (RGB >> 16) & 255;
                    int green = (RGB >> 8) & 255;
                    int blue = (RGB) & 255;
                    rgb[pb++] = (byte) red;
                    rgb[pb++] = (byte) green;
                    rgb[pb++] = (byte) blue;
                }

                return rgb;
            }
            else
                Logger.Info("unexpected pixel depth " + irgb.length );
            
            return null;
        } catch (IOException ex)
        {
            Logger.Debug("GetTnFromFileBytes2 IO error " + ex.toString());
        } catch (OutOfMemoryError e)
        {
            Logger.Info("GetTnFromFileBytes2 OutOfMemoryError error " + e.toString());
        } catch (Exception e)
        {
            Logger.Info("GetTnFromFileBytes2 other error " + e.toString());
        }

        return null;
    }

    // Resize. resize an image 
    public static BufferedImage Resize(BufferedImage image, Dimension d)
    {
        com.twelvemonkeys.image.ResampleOp resampleOp = new com.twelvemonkeys.image.ResampleOp(16, 16, ResampleOp.FILTER_BOX);
        BufferedImage thumbi = null;

        try
        {
            thumbi = resampleOp.filter(image, null);
            return thumbi;
        } catch (Exception e)
        {
            // if this method fails, try a slower onre from a different library
            return ResizeInt(image, d);
        }

    }

    // ResizeInt. resize an image using a different library - just in case
    public static BufferedImage ResizeInt(BufferedImage image, Dimension d)
    {
        BufferedImage thumbi = null;

        try
        {
            thumbi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = thumbi.createGraphics();
            g.drawImage(image, 0, 0, 16, 16, 0, 0, image.getWidth(), image.getHeight(), null);
            g.dispose();
            return thumbi;
        } catch (Exception e)
        {
            Logger.Severe("Backup failed, image=" + image.toString());
            return null;
        }
    }

    // GetFilesImageComponents. wrapper to get all bits 
    public static SSCRC GetFilesImageComponents(Path file)
    {
        try
        {
            SSCRC cr = new SSCRC();

            long filelength = Files.size(file);
            cr.bytes = new byte[(int) filelength];
            DataInputStream dis = new DataInputStream(new FileInputStream(file.toFile()));
            dis.readFully(cr.bytes);
            dis.close();

            cr.crc = Gen.Hash(cr.bytes);
            cr.thumb = GetTnFromFileBytes(cr.bytes);
            return cr;
        } catch (Exception ex)
        {
            Logger.Severe("GetCRCAndBytes : error processing " + file.toString() + " : " + ex.getMessage());
        }

        return null;
    }
    
    public static SSCRC GetFilesBytes(Path file)
    {
        try
        {
            SSCRC cr = new SSCRC();

            long filelength = Files.size(file);
            cr.bytes = new byte[(int) filelength];
            DataInputStream dis = new DataInputStream(new FileInputStream(file.toFile()));
            dis.readFully(cr.bytes);
            dis.close();

            cr.crc = 0;
            cr.thumb = null;
            return cr;
        } catch (Exception ex)
        {
            Logger.Severe("GetFilesBytes : error processing " + file.toString() + " : " + ex.getMessage());
        }

        return null;
    }    

}
