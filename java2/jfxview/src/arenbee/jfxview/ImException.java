/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arenbee.jfxview;

/**
 *
 * @author russ
 */
public class ImException  extends Throwable
{
    public String what;
    public ImException(String w)
    {
        what = w;
    }
    public String toString()
    {
        return "ImException:" + what;
    }
}
