package roz.rlib;

import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Created by russellb on 26/07/2016.
 */


/*

public class ISearch
{

    private ISearchTask currentTask = null;
    private static SLog l = SLog.GetL();
    private ISet set = null;
    public static int staskseed = 0;

    public ISearchTextTask createTextSearch()
    {
        return new ISearchTextTask();
    }
    public ISearchImageTask createImageSearch()
    {
        return new ISearchImageTask();
    }

    public ISearch(ISet set)
    {
        this.set = set;
    }

    public void Stop()
    {
        if ( currentTask != null )
            currentTask.aborted();
    }

    public enum State  { NotStarted, Running, Finished, Abort, Retired };

    public boolean startSearch(ISearchTask task)
    {
        if ( currentTask != null )
            return false;
        currentTask = task;
        l.warn("SE - Start start task");
        currentTask.start();
        l.warn("SE - Start return");
        return true;
    }

    public abstract class ISearchTask extends Thread
    {
        public void retire()
        {
            state = ISearch.State.Retired;
        }

        public String errorTxt = "";
        public int taskNum = ++staskseed;


        public void aborted()
        {
            state = ISearch.State.Abort;
        }
        public void running()
        {
            currentTask = this;
            state = ISearch.State.Running;
            start();
        }

        public void finished()
        {
            currentTask = null;
            state = ISearch.State.Finished;
        }

        public abstract void run();
        public String statusStr = "";
        public ISearch.State state = ISearch.State.NotStarted;
        public List<ISearchResult> results = new LinkedList<ISearchResult>();

        public void addResult(ISearchResult r)
        {
            synchronized (results)
            {
                results.add(r);
            }
        }

    }

    public class ISearchTextTask extends ISearchTask
    {

        public void setSearchFiles(boolean searchFiles) {  this.textSearchFileState = searchFiles; }
        private boolean textSearchDirState;
        public void setSearchDirs(boolean searchDirs)
        {
            this.textSearchDirState = searchDirs;
        }
        private boolean textSearchFileState;
        public void setTextSearchString(String textSearchString)
        {
            this.textSearchString = textSearchString;
        }
        private String textSearchString="";


        @Override
        public void run()
        {
            int fd=0;
            int sd=0;
            int sf=0;
            int ff=0;

            l.warn(String.format("SearchTextTask - run thread search %s", textSearchString));

            if ( textSearchString.length() == 0 )
            {
                statusStr = String.format("Empty search string");
                finished();
                return;
            }

            if ( textSearchDirState )
            {
                for( ISet.IDir d : set.getDirs().values() )
                {
                    sd++;
                    if ( state == ISearch.State.Abort )
                        break;
                    if ( StringUtils.containsIgnoreCase(d.path.toString(),textSearchString) )
                    {
                        fd++;
                        addResult(new ITextDirSearchResult(d));
                    }
                    statusStr = String.format("Searched %d dirs found %d, %d files found %d", sd, fd, sf, ff);
                }
            }

            if ( textSearchFileState )
            {

                for( ISet.IFile d : set.getFiles() )
                {
                    sf++;
                    if ( state == ISearch.State.Abort )
                        break;
                    if ( StringUtils.containsIgnoreCase(d.name,textSearchString) )
                    {
                        ff++;
                        addResult(new ITextFileSearchResult(d));
                    }
                    statusStr = String.format("Searched %d dirs found %d, %d files found %d", sd, fd, sf, ff);
                }
            }

            statusStr = String.format("Finished search, %d dirs found, files found %d", fd,  ff);
            l.warn(String.format("SearchTextTask - finished thread search %s", textSearchString));

            finished();
        }
    }

    public class ISearchResult
    {
    }

    public class IImageSearchResult extends ISearchResult implements Comparable<IImageSearchResult>
    {
        public ISet.IImage  image=null;
        public double       closeness=-1;

        public IImageSearchResult(ISet.IImage image, double closeness)
        {
            this.closeness = closeness;
            this.image = image;
        }

        @Override
        public int compareTo(IImageSearchResult o)
        {
            return Double.compare(closeness, o.closeness);
        }
    }

    public class ITextFileSearchResult extends ISearchResult
    {
        public ISet.IFile   file=null;

        public ITextFileSearchResult(ISet.IFile f)
        {
            this.file = f;
        }

    }

    public class ITextDirSearchResult extends ISearchResult
    {
        public ISet.IDir    dir=null;

        public ITextDirSearchResult(ISet.IDir d)
        {
            this.dir = d;
        }

    }



    public class ISearchImageTask extends ISearchTask
    {
        public void setImageSearch(ISet.ScanType scanType, String searchImagePath)
        {
            this.scanType = scanType;
            this.searchImagePath = searchImagePath;
        }
        public String searchImagePath;
        private Image searchImage;

        ISet.ICRC crcs =  null;
        int tnbytes[];
        List<IImageSearchResult> iresults = new LinkedList<IImageSearchResult>();
        ISet.ScanType scanType;

        public void getCVal(ISet.IImage ji)
        {
            if (ji.crc == crcs.crc)
            {
                //l.info("SEIMG(" + taskNum + ") ADD exact with " + ji.crc);
                iresults.add(new IImageSearchResult(ji, -1));
            }
            else
            {
                double td = 0;
                double top=30;
                switch(scanType)
                {
                    case Luma: top=15.0; break;
                    case Mono: top=40.0; break;
                    case Simple: top=45.0; break;
                }

                for (int tix = 0; tix < 16 * 16; tix++)
                {
                    int sbx = (tnbytes[tix] & 0x000000ff);
                    int sgx = (tnbytes[tix] & 0x0000ff00) >> 8;
                    int srx = (tnbytes[tix] & 0x00ff0000) >> 16;
                    int cbx = (ji.thumb[tix] & 0x000000ff);
                    int cgx = (ji.thumb[tix] & 0x0000ff00) >> 8;
                    int crx = (ji.thumb[tix] & 0x00ff0000) >> 16;
                    switch(scanType)
                    {
                        case Mono:
                        {
                            double lums = ((double) srx * 0.21) + ((double) sgx * 0.72) + ((double) sbx * 0.07);
                            double lumc = ((double) crx * 0.21) + ((double) cgx * 0.72) + ((double) cbx * 0.07);
                            td += abs(lums - lumc) / 255.0;
                        }
                        break;
                        case Simple:
                        {
                            int tx = abs(sbx - cbx) + abs(sgx - cgx) + abs(srx - crx);
                            td += (double) tx / 756.0;
                        }
                        break;
                        case Luma:
                        {
                            double dxr = abs(srx - crx) * 0.21;
                            double dxg = abs(sgx - cgx) * 0.72;
                            double dxb = abs(sbx - cbx) * 0.07;
                            double tx = (dxr + dxg + dxb) / 765.0;
                            td += tx;
                        }
                    }
                }
                //l.info("Img " + ji.crc + " diff=" + td);
                if ( td < top )
                {
                    iresults.add(new IImageSearchResult(ji, td));
                }


            }
        }

        @Override
        public void run()
        {
            int sf=0;
            int ff=0;

            l.warn("SEIMG(" + taskNum + ") starting search for similar to " + searchImagePath );

            crcs =  ISet.GetCRCAndBytes(Paths.get(searchImagePath));
            tnbytes = ISet.GetTnFromFileBytes(crcs.bytes);



            if ( tnbytes == null )
            {
                l.info("SEIMG(" + taskNum + ") failed to get a thumb, crc=" + crcs.crc + " bytes=" + crcs.bytes.length );
                finished();
                return;
            }

            l.info("SEIMG(" + taskNum + ") got image data, crc=" + crcs.crc + " bytes=" + crcs.bytes.length + ", tnbytes=" + tnbytes.length );
            roz.rlib.RTimer et = new  roz.rlib.RTimer();

            for( ISet.IImage ji : set.getImages().values() )
            {
                sf++;
                if ( state == ISearch.State.Abort )
                    break;

                getCVal(ji);


            }
            l.warn("SEIMG(" + taskNum + ") sorting results");

            Collections.sort(iresults);
            int ix=0;
            for(ISearchResult r : iresults)
            {
                if ( ix++ > 1000 )
                    break;
                addResult(r);
            }
            statusStr = String.format("Searched %d of %d found %d", sf, set.getImages().size(), ff);
            et.stop();
            l.warn("SEIMG(" + taskNum + ") finished in " + et  );
            finished();
        }
    }

}
*/
