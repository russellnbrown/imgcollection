/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author russ
 */
public class DirMon implements Runnable
{
    private DirMonEvent fire;
    private final static SLog l = SLog.GetL();

   private List<File> existing = new ArrayList();
   private File dir;
   private int state = 0;
   private List<File> threadfiles = new ArrayList();
   private final Lock lock = new ReentrantLock();
   
 
   
    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() 
    {
        int iter = 0;
        state = 1;
        while( state > 0 )
        {
            if ( state >= 2 )
            {
                state = 3;
                Utils.Sleep(5);
                continue;
            }
            
            iter++;
            if ( iter == 40  )
            {
                iter = 0;
                List<File> newf = Check();
                lock.lock();
                for(File f : newf )
                    threadfiles.add(f);
                lock.unlock();
            }
            Utils.Sleep(5);
        }
        // state zero by Stop - set to 1 to indicate we are done
        state = 1;
    }
   
    public List<File> GetList()
    {
        lock.lock();
        List<File> rv = threadfiles;
        threadfiles = new ArrayList();
        lock.unlock();
        return rv;
    }
    
 

    public void Stop()
    {
        state = 0; // 
        while(state == 0)
            Utils.Sleep(10);
        // all done...
    }
    
    public void Pause()
    {
        l.debug("DM pausing...");
        state = 2; // 
        while(state == 2 )
            Utils.Sleep(5);
        l.debug("DM waiting on resume...");
    }
  
    public void Resume()
    {
        state = 1; // 
        l.debug("DM resumed");
    }
          
    
    
    public interface DirMonEvent
    {
        public void dirMonEvent (File path);
    }   

    public class MyFileFilter implements FileFilter
    {
        @Override
        public boolean accept(File pathname) 
        {
            if ( Utils.isImageFile(pathname.getName()) )
                    return true;
            return false;
        }
    }

    public DirMon(String modir, DirMonEvent dme)
    {
       fire = dme;
       l.warn("DirMon - Monitoring " + modir);
       dir = new File(modir);
       Rescan();
    }
   
    public File find(File ff)
    {
        for(File s : existing)
            if ( s.getName().equals( ff.getName()) )
                return s;
        return null;
    }
    
    public void Rescan()
    {
        lock.lock();
        existing.clear();
        File fl[] = dir.listFiles( new MyFileFilter() );
        if ( fl == null )
           return;
       
        for(File f : fl)
        {
           existing.add(f);
           l.warn("Existing-" + f.getAbsolutePath());
        }  
        lock.unlock();
    }
    
    public List<File> Check()
    {
        List<File> newf = new ArrayList();
        try
        {
            l.debug("DirMon - Scanning " + dir.toString() );
            Path p = dir.toPath();

            if ( !Files.exists(p) )
            {
                l.severe("No Dir: " + dir.toString());
                return newf;
            }

           
            File latest[] = dir.listFiles( new MyFileFilter() );

            if ( latest != null )
            {
                l.debug("DirMon - scan returned " + latest.length + " filesInDir");
                for(File lf : latest)
                {
                    File ef = find(lf);
                    if ( ef == null )
                    {
                        l.debug("DirMon - New file found: " + lf.getName());
                        existing.add(lf);
                        newf.add(lf);
                    }
                }
            }
            else
                l.debug("DirMon - scan returned no filesInDir");
        }
        catch(Exception e)
        {
            l.severe("bad juju" + e.toString() );
        }
        return newf;
    }
    
    public List<File> Existing()
    {
        return existing;
    }
    
}
