
package roz.rlib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;
/**
 *
 * @author russellb
 */
public class ImLogging 
{
    
    
    private static String tag;
    
    
    public static Logger GetL()
    {
        return Logger.getLogger(tag);
    }
    
    
    public static Logger CreateL(String _tag, boolean off) 
    {
        tag = _tag;
       
        class ConsoleFormatter extends Formatter 
        {
            public String format(LogRecord record) {
                StringBuilder builder = new StringBuilder(1000);
                builder.append(formatMessage(record));
                builder.append("\n");
                return builder.toString();
            }
        }

   
        
        try 
        {
            String hf = System.getProperty("user.home");
            String lf = hf + "/logs/" + tag + ".log";
            String lk = hf + "/logs/" + tag + ".log.lck";
            
            Path ld = Paths.get(hf + "/logs");
            
            
            System.out.println("Using " + lf + " for logging");
            File logf = new File(lf);            
            logf.delete();
            logf = new File(lk);            
            logf.delete();
            
            // %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s [%3$s] (%2$s) %5$s %6$s%n

            System.setProperty("java.util.logging.SimpleFormatter.format",  "%4$-7s (%2$s) %5$s %6$s%n");

            final ConsoleHandler consoleHandler = new ConsoleHandler();
            
            consoleHandler.setFormatter(new ConsoleFormatter());
            final FileHandler fileHandler = new FileHandler(lf);
            
            fileHandler.setFormatter(new SimpleFormatter());

           if ( off )
           {
                consoleHandler.setLevel(Level.OFF);
                fileHandler.setLevel(Level.OFF);
           }
            else
           {
                consoleHandler.setLevel(Level.INFO);
                fileHandler.setLevel(Level.FINE);
           }
             
            
            final Logger app = Logger.getLogger(tag);
            LogManager.getLogManager().reset();
            app.setLevel(Level.FINEST);
 
            app.addHandler(consoleHandler);
            app.addHandler(fileHandler);

            return app;
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    static public void setup(String logTo) 
    {
         
        try
        {
            System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %1$tb %1$td, %1$tY %1$tH:%1$tM:%1$tS %5$s%6$s%n");
            // Create Logger
            Logger logger = Logger.getLogger("");
            logger.setLevel(Level.INFO);
            int limit = 1000000; // 1 Mb

            FileHandler fh = new FileHandler(logTo, limit, 1);
            Handler ch = new ConsoleHandler();

            SimpleFormatter formatterTxt = new SimpleFormatter();
            
            fh.setFormatter(formatterTxt);
            ch.setFormatter(formatterTxt);

            //logger.addHandler(ch);
            logger.addHandler(fh);

        }
        catch ( IOException e)
        {

        }

    }
}
