
package roz.rlib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PInfo
{
    private final static SLog l = SLog.GetL();
     
    private static Pattern px = Pattern.compile("(.*)\\s+(\\d+).*"); 
 
    public class Pid
    {
        public String name;
        public int    pid;
        public Pid(String _name, int _pid)
        {
            name = _name;
            pid = _pid;
        }
        public boolean Kill()
        {
            String cmd;
            
            if ( Utils.isWindows())
                cmd = String.format("taskkill /F /PID %d", pid);
            else
                cmd = String.format("/bin/kill -9 %d", pid);
            
            try            
            {
                Runtime.getRuntime().exec(cmd);
                l.warn("Killed " + name + ", pid is " + pid);
                return true;
            } 
            catch (IOException ex)
            {
                l.severe(ex.getMessage());
                return false;
            }
        }
    }
    private ArrayList pids = new ArrayList();
    
    public PInfo()
    {
        GetRunning();
    }
    
    public boolean KillMatching(String name)
    {
        boolean killed = false;
        l.debug("Kill "+name);
        for (Iterator it = pids.iterator(); it.hasNext();)
        {
            Pid p = (Pid)it.next();   
            if ( p.name.contains(name)  )
            {
                l.warn("Pid is "+p.pid);
                p.Kill();
                killed = true;
            }
        }
        return killed;
    }
    
    public void GetRunning() 
    {
        String line;
        Process p=null;
    
        pids.clear();

        try  
        {
            if ( Utils.isWindows() )
                p = Runtime.getRuntime().exec(System.getenv("windir") +"\\system32\\"+"tasklist.exe /v /FO \"CSV\" /NH");
            else 
                if ( Utils.isUnix() )
                    p = Runtime.getRuntime().exec("/bin/ps h -e -o fname,pid");// -o \"fname,pid\"");
            
            if ( p == null )
                return;
            
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null)
            {
                //l.finer("Line: " + line);
                String[] parts=null;
                 
                if ( Utils.isWindows() )
                {
                    line = line.replace("\"", "");
                    parts = line.split(",");
                }
                else
                {
                    Matcher m = px.matcher(line);
                    if ( m.find())
                    {
                        parts = new String[2];
                        parts[0] = m.group(1);
                        parts[1] = m.group(2);
                    }
                }
                if ( parts != null && parts.length > 1)
                {
                    String pname = parts[0];
                    String ppid = parts[1];
                    try
                    {
                        Pid pid = new Pid(pname, Integer.parseInt(ppid));
                        pids.add(pid);
                    }
                    catch ( NumberFormatException pe)
                    {
                        l.warn("Cant parse " + ppid + " as int (" + line + ")");
                    }
                }
            }
            input.close();
        }
        
        catch (Exception err) 
        {
            l.severe("Error in get running: " + err.getMessage() );
        }
       
    }
    
}
