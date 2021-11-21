package arenbee.jfxview;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by russ on 6/08/2016.
 */
public class SC extends Thread
{
    // a wrapper to provide a singleton for a set without needin to put a
    // singleton in the set itself
    private ScanSet set=null;

    private Path path;
    public boolean ready = false;
    public boolean error = false;

    public SC()
    {
        instance = this;
    }

    public void close()
    {
        instance = null;
        set = null;
        Runtime.getRuntime().gc();
    }

    public boolean load(String spath)
    {

        if ( spath == null || spath.length() == 0 ) {
            error = true;
            return false;
        }

        this.path = Paths.get(spath);
        if ( !ScanSet.Valid(path) ) {
            error = true;
            return false;
        }

        try
        {
            set = new ScanSet(path);
        }
        catch (ImException e)
        {
            e.printStackTrace();
            return false;
        }

        start();
        return true;
    }

    @Override
    public void run()
    {
        try
        {
            set.Load();
        }
        catch (ImException e)
        {
            error = true;
            e.printStackTrace();
            return;
        }
        ready = true;
    }


    private static SC instance = null;
    private static SC get() { return instance; }
    public static ScanSet gets() { return instance.set; }

}
