/*
 * Copyright (C) 2018 russell brown
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arenbee.jutils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;


// Logger. a really simple logger to write to both screen and a log file. both 
// outputs can be set to a threshold
public class Logger 
{
    private String tag;
    private static Logger instance = null;
    public enum Level { Off, Debug, Low, Info, Warning, Severe, Fatal };
    private Level clevel = Level.Info;
    private Level flevel = Level.Info;
    private FileWriter w = null;
  
    private static void make()
    {
        if ( instance != null )
        {
            Severe("Attempt to re-open logging.");
            return;
        }
        instance = new Logger();
    }

    //
    // check() 
    //
    // Is called in every write function to ensure logging is initialized.
    // If it isnt, we just detup some defaults and open file unspecified.log
    //
    private static void check()
    {
        if ( instance != null )
            return;
        make();
        instance.open("unspecified", "unspecified.log", Level.Info, Level.Info);
        Severe("Logging was uninitialized, using default settings");
    }
        
    public static void Create(String _tag, Level fileLevel, Level consoleLevel) 
    {
        make();
        instance.open( _tag,  _tag + ".log", fileLevel,  consoleLevel);
    }
    
    public static void Create(String _tag, String logFile, Level fileLevel, Level consoleLevel) 
    {
        make();
        instance.open( _tag,  logFile, fileLevel,  consoleLevel);
    }
   
     
    private void open(String _tag, String lf, Level fileLevel, Level consoleLevel) 
    {
        flevel = fileLevel;
        clevel = consoleLevel;
        tag = _tag;

        if ( fileLevel == Level.Off )
            return;

        try 
        {
            w = new FileWriter(lf, false);
            if ( w == null )
            {
                System.out.println("Can't open log file " + lf);
                System.exit(0);
            }

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    private static String pfxStr(Level l)
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
        String ds = Gen.DateStr(d);
        return ds + " " + ls + " ";
    }
    
    private void lwrite(String s, Level l)
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
    
    public static void Info(String s) 
    {
        check();
        instance.lwrite(pfxStr(Level.Info) + s, Level.Info);
    }
    public void info(String fmt, Object... args)
    {
        String s = String.format(fmt, args);
        info(s);
    }
    
    public static void Fatal(String s)
    {
        check();
        instance.lwrite(pfxStr(Level.Fatal) + s, Level.Fatal);
        System.exit(-1);
    }
    public static void Fatal(String fmt, Object... args)
    {
        String s = String.format(fmt, args);
        Fatal(s);
    }
    public static void Warn(String s)
    {
        instance.lwrite(pfxStr(Level.Warning)  + s, Level.Warning);
    }
    public static void Warn(String fmt, Object... args)
    {
        String s = String.format(fmt, args);
        Warn(s);
    }

    public static void Severe(String s)
    {
        check();
        instance.lwrite(pfxStr(Level.Severe) + s, Level.Severe);
    }
    public static void Severe(String fmt, Object... args)
    {
        String s = String.format(fmt, args);
        Severe(s);
    }
 
    
    public static void Low(String s)
    {
        check();
        instance.lwrite(pfxStr(Level.Low) + s, Level.Low);
    }
    public static void Low(String fmt, Object... args)
    {
        String s = String.format(fmt, args);
        Low(s);
    }
    
    public static void Debug(String s)
    {
        check();
        instance.lwrite(pfxStr(Level.Debug) + s, Level.Debug);
    }
    public static void Debug(String fmt, Object... args)
    {
        String s = String.format(fmt, args);
        Debug(s);
    }
    public static void Raw(String s)
    {
        check();
        instance.lwrite(s, Level.Severe);
    }
    
    private static Level getLogLevelFromString(String logFileLevel)
    {
            if ( logFileLevel.compareToIgnoreCase("warning") == 0 )
                return Level.Warning;
            if ( logFileLevel.compareToIgnoreCase("debug") == 0 )
                return Level.Debug;
            else if ( logFileLevel.compareToIgnoreCase("severe") == 0 )
                return Level.Severe;
            else if ( logFileLevel.compareToIgnoreCase("info") == 0 )
                return Level.Info;
            else if ( logFileLevel.compareToIgnoreCase("low") == 0 )
                return Level.Low;
            else
            {
                System.out.println("unknown logging level " + logFileLevel );
                return Level.Info;
            }
               
    }    
}
