/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package arenbee;

/**
 *
 * @author russ
 */
public class SortedImage
{
    public double closeness = 0.0;
    public String path = null;
    public long ihash = 0;
    
    public SortedImage(double c, long i)
    {
        closeness = c;
        ihash = i;
    }
    
}
