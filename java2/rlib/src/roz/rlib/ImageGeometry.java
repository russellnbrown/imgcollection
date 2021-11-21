/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roz.rlib;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import javax.swing.JPanel;

public class ImageGeometry
{

    private static Logger l = ImLogging.GetL();

    public double imageW; // image size
    public double imageH;
    
    public double cimageW; // cropped image size
    public double cimageH;
    public Point  cimageTL; 

    public double monW; // primary monitor size
    public double monH;
    
    public double dispW; // display size
    public double dispH;

    public BufferedImage image;

    public double scale;
    public double ascale;
    public double aspect;

    public void Print(String s)
    {
        l.fine(String.format(
"GEOM - %s\n - Primary monitor size %.3f,%.3f\n - Image size %.3f,%.3f\n - Crop size tl=%d,%d w=%.3f h=%.3f\n - Aspect %.3f\n - Scale %.3f & %.3f\n - Display Size w=%.3f h=%.3f\n",
         s,
         monW, monH, 
         imageW, imageH, 
         cimageTL.x, cimageTL.y, cimageW, cimageH, 
         aspect, 
         scale, ascale,
         dispW, dispH));
 
    
    }
    public ImageGeometry(BufferedImage i)
    {
        image = i;
 
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        monW = gd.getDisplayMode().getWidth();
        monH = gd.getDisplayMode().getHeight();
        
        Reset();
        
        Print("Initial State");
 
    }
    
    public void Reset()
    {
        imageW = image.getWidth();
        imageH = image.getHeight();       
        cimageTL = new Point(0,0);
        cimageW = image.getWidth();
        cimageH = image.getHeight();
        aspect = (double) imageW / (double) imageH;
        calcFirstDisplaySize();
    }
    

    public void setCrop(Rectangle r)
    {
        
        double x = (double)r.x * ascale;
        double y = (double)r.y * ascale;
        double iw = (double)r.width * ascale;
        double ih = (double)r.height * ascale;
        
        l.fine(String.format("GEOM - Crop\n Rect tl=%d,%d w=%d h=%d\n - Using scales=%.3f & %.3f\n - Gives tl=%.3f,%.3f w=%.3f h=%.3f" , 
                r.x, r.y, r.width, r.height, scale, ascale, x, y, iw, ih) );
        
        cimageTL = new Point((int)x, (int)y);
        cimageW = iw;
        cimageH = ih;
        
        calcFirstDisplaySize();
        Dimension d = getIdealWindowDimension();
        
        Print("After crop");
    }
    
    public Point getImagePoint(Point p)
    {
        double x = p.x * scale;
        double y = p.y * scale;
        return new Point((int)x,(int)y);
    }
            
    private void calcFirstDisplaySize()
    {
        scale = 1.0;
        while ((cimageH * scale > monH) || (cimageW * scale > monW))
        {
            scale -= 0.01;
        }
        
        ascale = 1/scale;
        dispW = getScaledWidth();
        dispH = getScaledHeight();
        
    }

    public int getScaledWidth()
    {
        return (int) (cimageW * scale);
    }

    public int getScaledHeight()
    {
        return (int) (cimageH * scale);
    }

    public Dimension getIdealWindowDimension()
    {
        return new Dimension(getScaledWidth(), getScaledHeight());
    }
    
    public void drawImage(Graphics g, JPanel onto)
    {        
        l.fine(String.format(
"GEOM - Draw onto w=%d h=%d\n - Scales=%.3f & %.3f\n - Crop tl=%d,%d w=%.1f h=%.1f" , 
                onto.getWidth(), onto.getHeight(), 
                scale, ascale, 
                cimageTL.x, cimageTL.y, cimageW, cimageH) );
        
        
        g.drawImage(image, 0, 0, onto.getWidth(), onto.getHeight(), 
                    cimageTL.x, cimageTL.y, cimageTL.x+(int)cimageW, cimageTL.y+(int)cimageH, null);
    }

}
