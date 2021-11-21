package arenbee.jfxview;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by russellb on 10/08/2016.
 */

// DirLoadEngine. This creates a list if ImageViews of thumbnails
// for all images in a directory. An initial list is returned with
// all images set to 'initialImg'. Thereafter thread(s) are
// started to load the real thumbnails ( or an error thumbnail )
// which are returned in a seperate list 'flist'. The initiator
// is responsible for removing the results from flist

public class DirLoadEngine
{

    private final static SLog l = SLog.GetL();
    private File dir;
    private Image initialImg;
    private Image errorImg;

    private boolean finished = false;
    private long start;
    private long end;

    public List<ImageView> ilist = new LinkedList<ImageView>();
    private List<DirLoadResult> flist = new LinkedList<DirLoadResult>();
    public static int deinstpool = 0;
    public int deinst = ++deinstpool;


    public DirLoadEngine(File dir)
    {
        l.info("DLE(" + deinst + ") running for " + dir.toString() );
        this.dir = dir;
        getInitialList();
    }


    public void  getInitialList()
    {
        try
        {
            File[] files = dir.listFiles();

            Arrays.sort(files, new Comparator<File>()
                {
                    @Override
                    public int compare(File o1, File o2)
                    {
                        switch (ViewSettings.dirsort)
                        {
                            default:
                            case "Name": return o1.getName().compareTo(o2.getName());
                            case "Modification Date": return  Long.valueOf(o1.lastModified()).compareTo(o2.lastModified());
                            case "Modification Date Reverse": return  Long.valueOf(o2.lastModified()).compareTo(o1.lastModified());
                            case "Size": return  Long.valueOf(o1.length()).compareTo(o2.length());
                            case "Size Reverse": return  Long.valueOf(o2.length()).compareTo(o1.length());
                        }
                    }
                }
            );

            initialImg = new Image(getClass().getResourceAsStream("inprogress.png"));
            errorImg = new Image(getClass().getResourceAsStream("error.png"));

            // process each file
            for (File f : files)
            {
                // we only want image files
                if (f.isDirectory())
                    continue;
                if (!Utils.isImageFile(f.getName()))
                    continue;

                // create an initial imageview with the default image
                ImageView iv = new ImageView(initialImg);
                DirLoadTag tag = new DirLoadTag();
                tag.file = f;
                iv.setUserData((Object) tag); // store file against image for convenience
                iv.setFitHeight(ViewSettings.tndispsize);
                iv.setFitWidth(ViewSettings.tndispsize);
                ilist.add(iv);
            }

            // Start encoding threads
            start = System.currentTimeMillis();
            int np = Runtime.getRuntime().availableProcessors();
            if (np < 1)
                np = 1;

            l.info("DLE(" + deinst + ") creating " + np + " threads");

            for (int p = 0; p < np; p++)
            {
                new DirLoadEngineTrd(p);
            }
            l.info("DLE(" + deinst + ") returning " + ilist.size() + " initial imgs");

        }
        catch (Exception e)
        {
            l.warn("Exception2 " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void Stop()
    {
        finished = true;
    }

    // This must be called from gui thread !
    public void loadPendingResults()
    {
        // load any pending results into the imageview list ( replace initial image with real one )
        synchronized (flist)
        {
            for (DirLoadEngine.DirLoadResult r : flist )
            {
                l.info("DLE(" + deinst + ") loading to main list" + r.ix + " img is " + r.img.getWidth() + "," + r.img.getHeight()  );
                ilist.get(r.ix).setImage(r.img);
            }
            flist.clear();
        }
    }

    // Stores a result in the results list
    public class DirLoadTag
    {
        public File file = null;
    }

    // Stores a result in the results list
    public class DirLoadResult
    {
        public Image img = null; // the real image
        public int ix = -1;      // irs position in the image view list
    }

    // encoding threads use this to get their next task
    public int nextOffRank = 0;

    class DirLoadEngineTrd extends Thread
    {
        int processed = 0;
        int tid = 0;

        DirLoadEngineTrd(int tid)
        {
            this.tid = tid;
            start();
        }

        public void run()
        {
            l.info("DLETRD(" + deinst + "," + tid + ") running");
            try
            {
                // run until no more to do ( or we are stopped externally )
                while (!finished)
                {
                    DirLoadTag tag = null;
                    int myIx = -1;

                    // get next file to encode
                    synchronized (ilist)
                    {
                        if (nextOffRank < ilist.size())
                        {
                            tag = (DirLoadTag)ilist.get(nextOffRank).getUserData();
                            myIx =  nextOffRank++;
                        }
                    }

                    // if there was one to do, do it!
                    if ( tag != null )
                    {
                        l.info("DLETRD(" + deinst + "," + tid + ") making " + tag.file.getName() + " ix= " + myIx + " of " + ilist.size() );
                        // Create a result and fill it with the real image and its location
                        DirLoadResult r = new DirLoadResult();
                        try
                        {
                            r.img = new Image(tag.file.toURI().toURL().toString(),
                                                 ViewSettings.tndispsize, ViewSettings.tndispsize, true, true);

                            r.ix = myIx;
                            if ( r.img.isError() )
                            {
                                l.warn("DLETRD(" + deinst + "," + tid + ") error in make image in " + tag.file.getName() + r.img.getException().toString()+ " try ImageIO" );
                                try
                                {
                                    BufferedImage bi =ImageIO.read(tag.file);
                                    BufferedImage rz = arenbee.jfxview.Image.Resize(bi, new Dimension(ViewSettings.tndispsize, ViewSettings.tndispsize), arenbee.jfxview.Image.ThumbType.SCALR);
                                    r.img = SwingFXUtils.toFXImage(rz, null);
                                    if ( r.img.isError())
                                    {
                                        l.warn("DLETRD(" + deinst + "," + tid + ") ImageIO failed also " + tag.file.getName() + r.img.getException().toString());
                                        r.img = errorImg;
                                    }
                                    else
                                        l.warn("DLETRD(" + deinst + "," + tid + ") ImageIO " + tag.file.getName() + " Seems to work" );
                                }
                                catch(Exception e)
                                {
                                    l.warn("DLETRD(" + deinst + "," + tid + ") ImageIO failed also, give up " + tag.file.getName() );
                                    r.img = errorImg;
                                }
                            }
                            else
                                l.info("DLETRD(" + deinst + "," + tid + ") made in " + tag.file.getName() + " size " + r.img.getWidth() + "," + r.img.getHeight() );
                            processed++;
                        }
                        catch (Exception e)
                        {
                            l.warn("DLETRD(" + deinst + "," + tid + ") error in " + tag.file.getName() + " use error image");                            r.img = errorImg;
                            r.img = errorImg;
                        }

                        // bung the result on the results q
                        l.info("DLETRD(" + deinst + "," + tid + ") made " + tag.file.getName() + " add to results");
                        synchronized (flist)
                        {
                            flist.add(r);
                        }

                    }
                    else
                    {
                        // there was nothing left to do, we can stop
                        end = System.currentTimeMillis();
                        l.info("DLETRD(" + deinst + "," + tid + ") nothing left to do, stopping" );
                        return;
                    }
                }
            }
            catch (Exception e)
            {
                l.warn("Exception " + e.getMessage());
                e.printStackTrace();
            }
        }

    }
}



