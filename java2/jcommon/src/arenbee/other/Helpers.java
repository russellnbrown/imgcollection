/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package arenbee.other;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author russ
 */
public class Helpers
{
    public static String strToHex(String s)
    {

        return Hex.encodeHexString(s.getBytes());
 
    }
    
    public static String bytesToHex(byte [] s)
    {

        return Hex.encodeHexString(s);
 
    }
    
    public static String hexToStr(String hexString)
    {
        try
        {
            String back = new String(Hex.decodeHex(hexString));
            return back;
        } catch (DecoderException ex)
        {
            Logger.getLogger(Helpers.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }
    
    public static byte []hexToBytes(String hexString)
    {
        try
        {
            return Hex.decodeHex(hexString);
        } catch (DecoderException ex)
        {
            Logger.getLogger(Helpers.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
