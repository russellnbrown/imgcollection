

package roz.settools;

import roz.rlib.*;

public class FileEncoder
{
    private final static SLog l = SLog.GetL();

    public FileEncoderTrd encoders[];
    public int nThreads = 0;
    private ISet set;
    private int finishedThreads = 0;
    private boolean running = true;
    int pfiles = 0;


    public FileEncoder(ISet s)
    {
        set = s;
        Runtime rt = Runtime.getRuntime();
        nThreads = Math.max(2,rt.availableProcessors() - 1);

        encoders = new FileEncoderTrd[nThreads];
        for (int t = 0; t < nThreads; t++)
        {
            encoders[t] = new FileEncoderTrd(t);
            new Thread(encoders[t]).start();
        }
        l.warn("FileEncoder created, using %d threads.", nThreads);
    }


    public void process(INode file)
    {
        int thisFile = pfiles++;
        int  retry=0;

        //l.info("FENC - File %d arrived for processing  %s ", thisFile,  file.toString() );

        while (true)
        {
            for (int t = 0; t < nThreads; t++)
            {
                if (encoders[t].p == null)
                {
                   // l.info("FENC - File %s sent to %d retry %d ", thisFile, t , retry );
                    encoders[t].p = file;
                    return;
                }
            }
            retry++;
            Utils.Sleep(1);
        }

    }

    public void stop()
    {
        finishedThreads = nThreads;
        running = false;
        while(finishedThreads > 0 )
            Utils.Sleep(1);
    }

    private class FileEncoderTrd implements Runnable
    {
        public INode p  = null;
        public int instance;

        private void tprocessFile(INode p)
        {
            try
            {
                ISet.ICRC crcs = ISet.GetCRCAndBytes(p.getPath());

                if (crcs != null)
                {
                    ISet.IFile f = null;
                    ISet.IImage i = null;

                    synchronized (set)
                    {
                        f = set.GetFile(p);
                        i = set.GetImage(crcs.crc);
                    }

                    if (f != null && i != null)
                        return; // noithing to do

                    if (i == null)
                    {
                        int[] tn = ISet.GetTnFromFileBytes(crcs.bytes);
                        if (tn == null)
                        {
                            l.info("tprocessFile : error getting thumb for  " + p.toString());
                            return;
                        }
                        synchronized (set)
                        {
                            i = set.AddImage(crcs.crc, tn);
                            i.isNew = true;
                        }
                    }
                    i.inUse = true;


                    synchronized (set)
                    {
                        l.info("SCAN: ADDFIL: dh: %d %s", p.getDirHash(), p);
                        f = set.AddFile(p, crcs.crc);
                        f.isNew = true;
                    }
                    f.inUse = true;


                }


            }
            catch (Exception ex)
            {
                l.severe("tprocessFile : error processing " + p.toString() + " : " + ex.toString());
            }

        }


        public FileEncoderTrd(int inst)
        {
            instance = inst;
        }

        @Override
        public void run()
        {
            l.info("Thread " + instance + " running");

            while (running)
            {
                // Wait for a file to arrive
                if (p == null)
                {
                    Utils.Sleep(1);
                    continue;
                }
                // process it
                l.info("FENC Thread " + instance + " processing " );
                tprocessFile(p);
                l.info("FENC Thread " + instance + " finished processing " );
                // indicate we are ready for next file
                p = null;
            }
            l.info("FENC  " + instance + " exiting");
            finishedThreads--;

        }

    }


}
