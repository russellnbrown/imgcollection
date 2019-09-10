/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arenbee.jimageviewer;

import arenbee.jimageutils.ImgCollection;
import arenbee.jimageutils.ImgCollectionDirItem;
import arenbee.jimageutils.ImgCollectionFileItem;
import arenbee.jimageutils.ImgCollectionImageItem;
import arenbee.jutils.Logger;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;

/**
 *
 * @author russe
 * 
 * jfxImage provides a JafaFX2 based interface with the ImgCollection class
 * 
 */
public class jfxImg {

    public final Image initialImg = new Image(getClass().getResourceAsStream("inprogress.png"));
    public final Image errorImg = new Image(getClass().getResourceAsStream("error.png"));
    public final Image errorImg16 = new Image(getClass().getResourceAsStream("error16.png"));
    public final Image blankImage = new Image(getClass().getResourceAsStream("blank16.png"));
            
    public Integer getNumberOfImages() 
    {
        return st.getNumberOfImages();
    }

    public class DirObservableList 
    {

        @Override
        public String toString() {
            return icdi.getDir();
        }
        public DirObservableList(ImgCollectionDirItem i)
        {
            icdi = i;
        }
        public ImgCollectionDirItem icdi;
        
        Path getPath()
        {
            String p = st.GetTop()+icdi.getDir();
            return Paths.get(p);
        }
    }
    
    public class FileObservableList 
    {

        @Override
        public String toString() {
            return icfi.getFile();
        }
        public FileObservableList(ImgCollectionFileItem i, ImgCollectionDirItem d)
        {
            icfi = i;
            icdi = d;
        }
        
        public ImgCollectionFileItem icfi;
        public ImgCollectionDirItem icdi;
        
        Path getPath()
        {
            String p = st.GetTop()+icdi.getDir()+"/"+icfi.getFile();
            return Paths.get(p);
        }
    }    
    
            
    public class jfxImgEx extends Exception
    {
        public jfxImgEx(String msg) 
        {
            super(msg);
        }
    }
    
    private ImgCollection st = new ImgCollection();
    private ObservableList<DirObservableList> oldirs = null;
    
    public boolean Open(Path set)
    {
        try
        {
            st.Load(set);
            return true;
        }
        catch(Exception e)
        {
            
        }
        return false;
    }
    
    ObservableList<DirObservableList> getListOfDirectories()
    {
        if ( oldirs == null )
        {
            ObservableList<FileObservableList> oldfiles = null;
            oldirs = FXCollections.observableArrayList ();
            for(ImgCollectionDirItem f : st.getDirs().values())
            {
                DirObservableList dol = new DirObservableList(f);
                oldirs.add(dol);
            }
        }
        return oldirs;
    }
    
    ObservableList<FileObservableList> getObservableListOfFilesInDirectory(ImgCollectionDirItem d)
    {
 
        ObservableList<FileObservableList>  f = FXCollections.observableArrayList ();
        for(ImgCollectionFileItem fi : st.getFiles())
        {
            if ( fi.getDhash() == d.getHash() )
            {
                FileObservableList dol = new FileObservableList(fi,d); // need to have a list of files in each dir iterm on load ( use a hash to biuld )
                f.add(dol);
            }
        }

        return f;
    }    
    
    List<ImgCollectionFileItem> getListOfFilesInDirectory(ImgCollectionDirItem d)
    { 
        List<ImgCollectionFileItem> files = new LinkedList<>();
        for(ImgCollectionFileItem fi : st.getFiles())
        {
            if ( fi.getDhash() == d.getHash() )
            {
                files.add(fi);
            }
        }
        return files;
    }   
    
    public Path getPathOfImgFile(ImgCollectionFileItem f)
    {
        return st.getFilePath(f);
    }
    
    public ImageView getImageFrom(Path p, int x, int y)
    {
        ImageView iv = new ImageView();
        try
        {
            Image img = new Image("file:"+p.toAbsolutePath().toString(),x,y, true, true);
            iv.setImage(img);
            return iv;
        }
        catch(Exception e)
        {
            iv.setImage(errorImg);
            iv.setFitHeight(x);
            iv.setFitWidth(y);
            return iv;
        }
    }
 
    public ImageView getImageFrom(Path p)
    {
        ImageView iv = new ImageView();
        try
        {
            Image img = new Image("file:"+p.toAbsolutePath().toString());
            iv.setImage(img);
            return iv;
        }
        catch(Exception e)
        {
            iv.setImage(errorImg);
            return iv;        
        }
    }
 
    public ImageView getImageFrom(String s)
    {
        ImageView iv = new ImageView();
        if ( s.equalsIgnoreCase("blank") )
            iv.setImage(blankImage);
        else
            iv.setImage(errorImg);
        return iv;        
    }    
    
    public Image getThumbImageFromThumbBytes(byte[] bytes ) {
        
  
        WritableImage img = new WritableImage(16, 16);
        PixelWriter pw = img.getPixelWriter();
        double w = img.getWidth();
        double h = img.getHeight();
        int bp=0;
        
        PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteRgbInstance();

        pw.setPixels(0,0,16,16, pixelFormat, bytes, 0, 16 * 3);
         
        return img;
    }    

 
    
   public ImageView getThumbImageFrom(ImgCollectionFileItem f, int x, int y) 
   {
        ImgCollectionImageItem ii = st.getImageItemForFileItem(f);
        Image tx = getThumbImageFromThumbBytes(ii.getThumb());        
        ImageView ivx = new ImageView(tx);
        ivx.setFitHeight(x);
        ivx.setFitWidth(y);       
        return ivx;
    }

    
}
