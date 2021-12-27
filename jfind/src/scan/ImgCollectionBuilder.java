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
package scan;

import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;


// ImgCollectionBuilder. This is a helper class to build an Image Collection.
// it is derived from SimpleFileVisitor as one of its functions will be
// to scan all the image files in the 'top' directory
public class ImgCollectionBuilder extends SimpleFileVisitor<Path>
{
 /*
    public ImgCollectionBuilder(boolean ut)
    {
        useThreads = ut;
        set = new ImgCollection(ut);
    }
    // The collection we are building
    private ImgCollection set = null;

    // signal to stop threads
    public boolean running = true;
    // number of processing threads
    private int numThreads = 0;
    private boolean useThreads = true;
    // keep some stats on whats happening
    private final Stats st = new Stats();
 
    // ImgProcessingThread. This is the thread that will processs the images
    // in the scan. 
    class ImgProcessingThread implements Runnable
    {
        

        // Info about our thread
        public int id;
        public Thread thread;

        // the thread itself
        public void run()
        {
            // keep thread running, we will exit when running is set to false
            // but we cant do a while(running) as we may loose something in
            // our input q
            while (true)
            {
                // read an ImageInfo off our input queue
                ImageInfo ii = null;
                ii = threadFeeder.poll();

                // if something to process, do it...
                if (ii != null)
                {
                    processFile(ii);
                } else 
                // otherwise Q is empty, we can check running to see 
                // if we should exit the thread
                {
                    if (!running)
                    {
                        break;
                    }
                    // nothing to do, end not indicated. take a nap...
                    arenbee.jutils.Gen.Sleep(10);
                }
            }
            Logger.Info("** EXIT Thread " + id + " is exiting.");
        }
    }
    
    // used to pass info on an image file to the processing thread
    class ImageInfo
    {
        ImgCollectionFileItem sfile;
        Path name;
        Path dir;
    }

    // The main thread is driven by the file visitor mechanism. we 
    // feed an image file to one of the processing threads. 
    // we don't create a processing thread for each individual image as we 
    // would rapidly run out of resources. we need to limit this somehow. we
    // implement this by creating a processing thread for each processor 
    // on our host. These threads are added to the 'threads' list
    public List<ImgProcessingThread> threads = new LinkedList<>();
    // we feed images to the threads using the threadFeeder, put an image into the 
    // thread and led the individual threads remove one when it is ready. this
    // is thread safe
    public ConcurrentLinkedQueue<ImageInfo> threadFeeder = new ConcurrentLinkedQueue<>();

 

    private void setUpCreateThreads()
    {
        // create the processing threads. See how many processors we have
        Runtime rt = Runtime.getRuntime();
        numThreads = rt.availableProcessors() * 2;// - 1; // don't hog all threads
        // unless we only have the one processor, in which case we will have to hog it...
        if (numThreads < 1)
            numThreads = 1;
        
        Logger.Info("Running with " + numThreads + " threads");
        
        // create the threads and start them up. They will monitor the 
        // threadFeeded Q for things to do
        for (int tx = 0; tx < numThreads; tx++)
        {
            ImgProcessingThread rti = new ImgProcessingThread();
            rti.id = tx;
            rti.thread = new Thread(rti);
            rti.thread.start();
            threads.add(rti);
            st.incThreads();
            Logger.Info("Creating thread " + tx);
        }

    }
    
    // Two ways to use the builder, either Craete ( build from files ) or
    // Load ( load an existing set ) 
    // in either case the 'set' will be filled
    public void Create(Path spath, Path dpath)
    {
        
        // Make sure the dir to add exists
        if (!Files.exists(dpath))
        {
            Logger.Fatal("Directory " + dpath + " dosn't exist");
        }

        // If the set directory dosn't exist then make it
        if (!Files.exists(spath))
        {
            try
            {
                Files.createDirectories(spath);
            } catch (IOException ex)
            {
                Logger.Fatal("Set path " + spath + " dosn't exist and I couldn't create it.");
            }
        }

        // if creating, create the threads. we dont need to do this for a 'Load'
        if ( useThreads )
            setUpCreateThreads();

        try
        {
            // Set the top directory in the imageset 
            Timer.stagestart();
            set.SetTop(dpath);

            // And build....
            Files.walkFileTree(dpath, this);
            
            // All files & dis added, wait for any thumbnails to finish
            // encoder.WaitTillFinished();
            if ( useThreads )
                waitForThreads();
            
            // and save to disk
            Timer.stagestop("Create");
            set.Save(spath);

        } catch (IOException ex)
        {
            Logger.Severe("Error while scanning dir " + spath + " : " + ex.toString());
        }
        Logger.Info(st.toString());
    }    
    
    // Load. load from disk reather than scanning files
    public boolean Load(Path spath)
    {
        set.Load(spath);
        return true;
    }

    // called when the SimpleFileVisitor finds a directory
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
    {
        st.incDirs();
        
        // add the dir
        set.AddDir(dir);
        return CONTINUE;
    }

    // called when the SimpleFileVisitor finds a file
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
    {
        st.incFiles();

        Path name = file.getFileName();
        Path dir = file.getParent();
        boolean isImage = arenbee.jutils.Gen.IsImageFile(name.getFileName());
        if ( !isImage )
            return CONTINUE;

        // Find the diritem for this file
        ImgCollectionDirItem sid = set.GetDir(dir);
        if (sid == null)
        {
            Logger.Severe("No dir for a file!" + file);
            return CONTINUE;
        }

        // Create an imageinfo for this file 
        ImageInfo ii = new ImageInfo();
        // add the file to the image collection as a placeholder without the crc or  
        // thumb calculated, these will be added when the img processor thread
        // has processed the file
        ii.sfile = set.AddFile(sid, file);

        // send to a thread
        if ( useThreads )
            add(ii);
        else
            processFile(ii);
        
        // let user know we are still alive
        if (st.getFiles() % 50 == 0)
        {
            Logger.Info("At files=" + st.getFiles());
        }

        return CONTINUE;
    }


    void add(ImageInfo ii)
    {
        // sent to thread. If the threadFeeder is getting full
        // just wait until the threads catch up ( otherwise 
        // threadFeeder will just get huge
        while (threadFeeder.size() > numThreads * 2)
        {
            arenbee.jutils.Gen.Sleep(10);
        }
        threadFeeder.add(ii);
    }

    // processFile. This is where the main work is done! called from
    // an image processing thread
    void processFile(ImageInfo ii)
    {

        // create the imageitem
        ImgCollectionImageItem sii = null;
        Logger.Debug("processFile(1), name=" + ii.sfile);
        Path fpath = GetFullPathOfFile(ii.sfile);
        
        // get the thumb & crc of the data in the file
        if (Files.exists(fpath))
        {
            sii = getFileThumbAndCrc(fpath);
            if ( sii != null )
            {
                Logger.Debug("processFile, name=" + ii.sfile.getFile() + ", crc=" + Long.toHexString(sii.getIHash()));
                // call file processes to send resuts back to main thread
                FileProcessed(ii.sfile, sii);
                return;
            }
        }
        Logger.Warn("processFile, error processing name=" + ii.sfile.getFile() + ", path" + fpath.toString());
    }

    // Create the imageitem & fill the crc & thumb 
    public static ImgCollectionImageItem getFileThumbAndCrc(Path fpath)
    {
        ImgCollectionImageItem sii = new ImgCollectionImageItem();
        // call image helper to get the bits we want
        Image.SSCRC cby = Image.GetFilesImageComponents(fpath);
        if ( cby == null )
            return null;
        // sad save to the imageitem
        sii.setCrc(cby.crc);
        sii.setThumb(cby.thumb);
        sii.setSize(cby.bytes.length);
        return sii;
    }

    // GetFullPathOfFile. Gets the path of a fileitem using its dir hash to 
    // get the dir it is in and add the name
    public Path GetFullPathOfFile(ImgCollectionFileItem sf)
    {
        // full path is gotten from top + diritem realtive path + name
        ImgCollectionDirItem sid = set.GetDirByHash(sf.getDhash());
        String tpath = set.GetTop();
        String dpath = sid.getDir();
        String fpath = sf.getFile();

        String path = tpath + dpath + "/" + fpath;
        return Paths.get(path);
    }

    public FileVisitResult visitFileFailed(Path file, IOException exc)
    {
        st.incErrors();
        return CONTINUE;
    }

    // waitForThreads. 
    void waitForThreads()
    {
        // tell threads to stop
        running = false;
        
        // join them all before returning
        Logger.Info("Wait for encoder threads to finish...");
        for (ImgProcessingThread rti : threads)
        {
            try
            {
                rti.thread.join();
            } catch (InterruptedException ex)
            {
                Logger.Info(ex.getMessage());
            }
        }
        
        // stop timer
        st.stop();
    }


    // FileProcessed. This will be called by the encoder after a file has been processed. 
    void FileProcessed(ImgCollectionFileItem sif, ImgCollectionImageItem sii)
    {
        // add the image to the colelction. It may already be there from another
        // file
        boolean isNew = set.AddImage(sii);
        
        // inc stats
        st.addBytes(sii.getSize());
        if ( isNew )
            st.incImages();
        else
            st.incDuplicates();
        
        // record images crc map in fileitem
        sif.setIHash(sii.getIHash());
    }
*/
}
