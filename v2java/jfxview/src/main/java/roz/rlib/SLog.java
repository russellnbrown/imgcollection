/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roz.rlib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import static roz.rlib.SLog.SLevel.Off;

/**
 *
 * @author russellb
 */
public class SLog 
{
    private String tag;
    private static SLog instance = null;
    public enum SLevel { Off, Debug, Low, Info, Warning, Severe, Fatal };
    private SLevel clevel = SLevel.Info;
    private SLevel flevel = SLevel.Info;
    private FileWriter w=null;

 
    public static SLog GetL()
    {
        make();
        return instance;
    }
   
    private static void make()
    {
        if ( instance != null )
            return;
        instance = new SLog();
    }
    
    public static SLog CreateL(String _tag, SLevel fileLevel, SLevel consoleLevel) 
    {
        make();
        instance.Create( _tag,  fileLevel,  consoleLevel);
        return instance;
    }
    
    public static SLog CreateL(String _tag, String logFile, SLevel fileLevel, SLevel consoleLevel) 
    {
        make();
        instance.Create( _tag,  logFile, fileLevel,  consoleLevel);
        return instance;
    }
    
    public void Create(String _tag, SLevel fileLevel, SLevel consoleLevel) 
    {
        if ( fileLevel == Off )
            return;

        String hf = System.getProperty("user.home");
        String lf = hf + "/logs/" + _tag + ".log";
        Create(_tag, lf, fileLevel, consoleLevel);
    }
    
     
    public void Create(String _tag, String lf, SLevel fileLevel, SLevel consoleLevel) 
    {
        flevel = fileLevel;
        clevel = consoleLevel;
        tag = _tag;

        if ( fileLevel == Off )
            return;

        try 
        {
            instance.info("Using " + lf + " for logging");
            File logf = new File(lf);            
            logf.delete();
            
            w = new FileWriter(lf, false);
            if ( w == null )
            {
                System.out.println("Can't open log file " + lf);
            }

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    private static String pfxStr(SLevel l)
    {
        String ls = "";
        switch(l)
        {
            case Severe: ls = "-S-"; break;
            case Warning: ls = "-W-"; break;
            case Info: ls = "-I-"; break;
            case Low: ls = "-L-"; break;
            case Debug: ls = "-D-"; break;
            case Fatal: ls = "-F-"; break;
        }
        Date d = new Date();
        String ds = Utils.dateStr(d);
        return ds + " " + ls + " ";
    }
    
    private void lwrite(String s, SLevel l)
    {
 
        
        try
        {
            if ( clevel.compareTo(l ) <= 0 )
                System.out.println(s);
            if ( flevel.compareTo(l) <= 0 && w != null )
            {
                w.write(s + "\n"); 
                w.flush();
            }
        }
        catch (IOException ex)  {
            System.out.println("Error writing to log" + ex.getMessage() );
        
        }       
    }
    
    public void info(String s) 
    {
        lwrite(pfxStr(SLevel.Info) + s, SLevel.Info);
    }
    public void info(String fmt, Object... args)
    {
        String s = String.format(fmt, args);
        info(s);
    }
    
    public void fatal(String s)
    {
        lwrite(pfxStr(SLevel.Fatal) + s, SLevel.Fatal);
        System.exit(-1);
    }
    public void fatal(String fmt, Object... args)
    {
        String s = String.format(fmt, args);
        fatal(s);
    }
    public void warn(String s)
    {
        lwrite(pfxStr(SLevel.Warning)  + s, SLevel.Warning);
    }
    public void warn(String fmt, Object... args)
    {
        String s = String.format(fmt, args);
        warn(s);
    }

    public void severe(String s)
    {
        lwrite(pfxStr(SLevel.Severe) + s, SLevel.Severe);
    }
    public void severe(String fmt, Object... args)
    {
        String s = String.format(fmt, args);
        severe(s);
    }
 
    
    public void low(String s)
    {
        lwrite(pfxStr(SLevel.Low) + s, SLevel.Low);
    }
    public void low(String fmt, Object... args)
    {
        String s = String.format(fmt, args);
        low(s);
    }
    public void debug(String s)
    {
        lwrite(pfxStr(SLevel.Debug) + s, SLevel.Debug);
    }
    public void debug(String fmt, Object... args)
    {
        String s = String.format(fmt, args);
        debug(s);
    }
    public void raw(String s)
    {
        lwrite(s, SLevel.Severe);
    }
    
    public static SLevel getLogLevelFromString(String logFileLevel)
    {
            if ( logFileLevel.compareToIgnoreCase("warning") == 0 )
                return SLevel.Warning;
            if ( logFileLevel.compareToIgnoreCase("debug") == 0 )
                return SLevel.Debug;
            else if ( logFileLevel.compareToIgnoreCase("severe") == 0 )
                return SLevel.Severe;
            else if ( logFileLevel.compareToIgnoreCase("info") == 0 )
                return SLevel.Info;
            else if ( logFileLevel.compareToIgnoreCase("low") == 0 )
                return SLevel.Low;
            else
            {
                System.out.println("unknown logging level " + logFileLevel );
                return SLevel.Info;
            }
               
    }    
}
