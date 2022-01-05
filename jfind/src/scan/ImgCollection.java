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

import arenbee.other.Logger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.nio.charset.MalformedInputException;
import java.util.Scanner;
import arenbee.other.Gen;

//
//  'ImgCollection' is used to hold the various items in the
// image set. 
public class ImgCollection
{
 
    
    public ImgCollection(boolean ut)
    {
        useThreads = ut;
     
    }
    
    // These four items define the ImageCollection. 
    private String top;
    private boolean useThreads = true;
    

    private final ConcurrentHashMap<Long, ImgCollectionDirItem> dirs = new ConcurrentHashMap<>();

    public ConcurrentHashMap<Long, ImgCollectionDirItem> getDirs() {
        return dirs;
    }

    public ConcurrentLinkedQueue<ImgCollectionFileItem> getFiles() {
        return files;
    }

    public ConcurrentHashMap<Long, ImgCollectionImageItem> getImages() {
        return images;
    }
    private final ConcurrentLinkedQueue<ImgCollectionFileItem> files = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<Long, ImgCollectionImageItem> images = new ConcurrentHashMap<>();

    // top & dirs are the directory store. Directories paths are stored as strings rather than 
    // paths so we can standardize across operating systems. They are held in
    // a unix '/' style.
    // They are stored relative to a top level directory with a leading '/'
    // This means we can move the image directory around without needing
    // to rebuild the imageset
    // 'files' are the file store. 
    // 'images' are the image store, one image for each image, which may belong to 
    // more than one file
    // ** The lists/hashes need to be protected when building the collection as they 
    // may be changed by more than one thread so we use the synchronized thread 
    // safe versions of List & HashMap **
    // SetTop. sets the top or root of the directories
    public void SetTop(Path topDir)
    {
        top = Gen.StandardizePath(topDir);
    }

    public String GetTop()
    {
        return top;
    }

    // AddDir. Add a directory to the collection. First make it relative to the top
    public ImgCollectionDirItem AddDir(Path dpath)
    {
        // standardize & remove root from dir path
        String dstr = Gen.StandardizePath(dpath);
        dstr = makeRelativeToTop(dstr);

        ImgCollectionDirItem sid = new ImgCollectionDirItem();
        sid.setDir(dstr);
        sid.setHash(Gen.Hash(dstr.getBytes()));

        dirs.put(sid.getHash(), sid);

        return sid;
    }

    // GetDir. Get the diritem for a particular path.
    public ImgCollectionDirItem GetDir(Path dpath)
    {
        // standardize & remove root from dir path
        String dstr = Gen.StandardizePath(dpath);
        dstr = makeRelativeToTop(dstr);

        // get the hash to access the dirs list
        long hash = Gen.Hash(dstr.getBytes());
        return GetDirByHash(hash);
    }

    public ImgCollectionDirItem GetDirByHash(long dhash)
    {
        if (dirs.containsKey(dhash))
        {
            return dirs.get(dhash);
        }
        return null;
    }

    // makeRelativeToTop. takes a path and makes it relative to the 'top' 
    public String makeRelativeToTop(String p)
    {
        if (p.startsWith(top))
        {
            // remove the top bit
            p = p.substring(top.length());
            // ensure that it starts with a '/' ( this is so that 
            // every path including the 'top' has something in it
            // for the hashing of the path to work
            if (p.length() == 0)
            {
                p = "/";
            }
        }
        return p;
    }

    // AddFile. add a file to the 'files' list. We also need the diritem to get the
    // files directories hash
    public ImgCollectionFileItem AddFile(ImgCollectionDirItem currentDir, Path file)
    {
        ImgCollectionFileItem sif = new ImgCollectionFileItem();
        sif.setDhash(currentDir.getHash());
        sif.setFile(file.getFileName().toString());
        files.add(sif);
        return sif;
    }

    // AddImage. Adds an image to the 'images' hashmap
    public boolean AddImage(ImgCollectionImageItem sii)
    {
        if (!images.containsKey(sii.getIHash()))
        {
            images.put(sii.getIHash(), sii);
            return true;
        }
        return false;
    }
    


    // Find. Searches the imgcollection to find a matching image. We calculate
    // a 'closeness' value for each image and keep the results in a treeset 
    // so that we can return them in order
    public List<SearchResult> Find(Path spath, int maxSize)
    {
        BufferedWriter dbg = null;
       
        
        //try
        //{
      
            // Create list to hold search results. U
            //List<SearchResult> pq = new LinkedList<>();
            PriorityQueue<SearchResult> pq = new PriorityQueue<>(new SearchResultComparator());
            ImgCollectionImageItem sfor = null;//TBD getFileThumbAndCrc(spath);
            if ( sfor == null )
            {
                Logger.Severe("Can't find search file " + spath);
                return null;
            }
            //Path pdir =  Paths.get("debug.j.csv");
            //dbg = Files.newBufferedWriter(pdir, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            // calculate the closeness. Only add to the search results if it is
            // below a threshold to limit its size as we will only be interested
            // in the closest results
            double cv = 0;
            for (ImgCollectionImageItem v : images.values())
            {
                if ( v.getIHash() == sfor.getIHash() )
                    cv = 0.0;
                else
                {

                   // StringBuilder ss = new StringBuilder();

                  // ss.append("img,");
                  // ss.append(v.getIHash());

                   cv = sfor.getCVal(v);//, ss);
                   
                   //dbg.write(ss.toString());
                  // dbg.newLine();
                }
                
                SearchResult sr = new SearchResult();
                sr.setImage(v);
                sr.setCloseness(cv);
                pq.add(sr);
            }
            
            // copy only the required number of results from the sorted queue to the list
            // to be returned
            
            /*
            Collections.sort(pq, new Comparator<SearchResult>()
            {
            @Override
            public int compare(SearchResult s1, SearchResult s2) 
            {
            if (s1.getCloseness() < s2.getCloseness())
            return -1;
            else if (s1.getCloseness() > s2.getCloseness() )
            return 1;
            return 0;
            }
            });
            Timer.stagestop("Find");
            return pq.subList(0, min(maxSize,pq.size()));*/
            List<SearchResult> res = new LinkedList<>();
            for(SearchResult i : pq)
            {
                res.add(i);
                if ( res.size() == maxSize )
                    break;
            }    
            return res;
        /*} 
        catch (IOException ex)
        {
            System.exit(-1);
        }*/
        //return null;
    }

    // Save. saves the imgcollection to disk. each of the collections is written to its own
    // file and 'top' is added as the first line of the directory file. 
    // ** these files need to be interchangable with the other imagecollection implementations
    //    ( c, c++, C# & python ) so we can't use a traditional serialize method **
   /* public void Save(Path s)
    {
        Timer.stagestart();


        try
        {
            Path pdir = s.resolve("dirs.txt");
            Path pfile = s.resolve("files.txt");
            Path puniq = s.resolve("images.bin");

            BufferedWriter fdirs = Files.newBufferedWriter(pdir, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            BufferedWriter ffiles = Files.newBufferedWriter(pfile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            FileOutputStream fout = new FileOutputStream(puniq.toString());
            FileChannel funiq = fout.getChannel();

            fdirs.write(top);
            fdirs.newLine();

            for (ImgCollectionDirItem d : dirs.values())
            {
                String st = String.format("%d,%s", d.getHash(), d.getDir());
                fdirs.write(st);
                fdirs.newLine();
            }

            for (ImgCollectionFileItem f : files)
            {
                String st = String.format("%d,%d,%s", f.getDhash(), f.getIHash(), f.getFile());
                ffiles.write(st);
                ffiles.newLine();
            }

            ByteBuffer ibuf = ByteBuffer.allocateDirect(8 + 3 * 16 * 16); // crc + 3 byte rgb * image sixe ( 16 * 16 )
            ibuf.order(ByteOrder.LITTLE_ENDIAN);
            for (ImgCollectionImageItem i : images.values())
            {
                if (i.getThumb() == null)
                {
                    Logger.Debug("ERR Saving img " + i + " thumb is null");
                    continue;
                }
                if (i.getThumb().length != 16 * 16 * 3)
                {
                    Logger.Debug("ERR Saving img " + i + " length not rigth: " + i.getThumb().length);
                    continue;
                }
                ibuf.rewind();
                ibuf.putLong(i.getIHash());

                int by = 0;
                for (int b = 0; b < 16 * 16 * 3; b++)
                {
                    ibuf.put(i.getThumb()[by]);
                    by++;
                }
                ibuf.rewind();
                funiq.write(ibuf);

            }

            fdirs.close();
            ffiles.close();
            funiq.close();
            fout.close();
        } catch (IOException ioe)
        {
            Logger.Severe(String.format("Caught IOE : %s in save", ioe.toString()));
            ioe.printStackTrace();

        }
        Timer.stagestop("Save");

    }*/

    // Load. loads the imgcollection from disk. each of the collections is read from its own
    // file. 
    // ** these may have been created my one of the other imagecollection implementations so
    //    we cant use serialization **
    public void Load(Path s)
    {
        int lc=0;
        int f=0;
        
        try
        {
            
            
            Path pdir = s.resolve("dirs.txt");
            Path pfile = s.resolve("files.txt");
            Path puniq = s.resolve("images.bin");

            Scanner fdirs;
            Scanner ffiles;
            FileInputStream fin;
            FileChannel funiq;

            fin = new FileInputStream(puniq.toString());
            funiq = fin.getChannel();
            fdirs = new Scanner(pdir);
            ffiles = new Scanner(pfile);

            String line;
            line = fdirs.nextLine();
            top = line;

            while (fdirs.hasNext())
            {
                line = fdirs.nextLine();
                f=1;
                lc++;
                try
                {
                    int cpos1 = line.indexOf((int) ',');

                    String sdhash = line.substring(0, cpos1);
                    String spath = line.substring(cpos1 + 1);

                    ImgCollectionDirItem sid = new ImgCollectionDirItem();
                    sid.setDir(spath);
                    sid.setHash(Long.parseLong(sdhash));

                    dirs.put(sid.getHash(), sid);
                } catch (NumberFormatException nfe)
                {
                    Logger.Fatal("Number ex reading dirs" + nfe.getMessage() + line);
                }
            }

            f=2;
            lc=0;
            long fpos=0;
            try
            {
                while( ffiles.hasNext() )
                {
                    line=ffiles.nextLine();
                    f=2;
                    lc++;
                    fpos += line.length()+1;
                    if ( lc == 80209 )
                    {
                        Logger.Info("here, fpos="+ fpos);
                    }
                    String sfhash="";
                    String scrc="";
                    String sname="";           
                    int cpos1 = line.indexOf((int) ',');
                    int cpos2 = line.indexOf((int) ',', cpos1 + 1);
                    try
                    {
                     sfhash = line.substring(0, cpos1);
                     scrc = line.substring(cpos1 + 1, cpos2);
                     sname = line.substring(cpos2 + 1);
                    }
                    catch (Exception nfe)
                    {
                        Logger.Fatal("NUmber ex reading files" + nfe.getMessage() + line);
                    }

                    try
                    {
                        long dhash = Long.parseLong(sfhash);
                        long crc = Long.parseLong(scrc);
                        ImgCollectionFileItem sif = new ImgCollectionFileItem();
                        sif.setIHash(crc);
                        sif.setDhash(dhash);
                        sif.setFile(sname);
                        files.add(sif);
                    } catch (NumberFormatException nfe)
                    {
                        Logger.Fatal("NUmber ex reading files" + nfe.getMessage() + line);
                    }
                    catch (Exception nfe)
                    {
                        Logger.Fatal("NUmber ex reading files" + nfe.getMessage() + line);
                    }

                    //line = ffiles.readLine();

                }
            }
            catch(Exception e) // sometimes filename have unprintable characters - ignore them
            {
                Logger.Fatal("Bad readline, malformed");
                return;
            }     

            lc=0;
            f=3;
            ByteBuffer ibuf = ByteBuffer.allocateDirect(8 + 3 * 16 * 16);
            ibuf.order(ByteOrder.LITTLE_ENDIAN);

            try
            {
                while (fin.available() > 0)
                {
                    ibuf.rewind();
                    funiq.read(ibuf);
                    ibuf.rewind();
                    long crc = ibuf.getLong();
                    byte[] buf = new byte[16 * 16 * 3];
                    ibuf.get(buf);
                    ImgCollectionImageItem sii = new ImgCollectionImageItem();
                    sii.setCrc(crc);
                    sii.setThumb(buf);
                    images.put(crc, sii);
                }
            } catch (IOException e)
            {

            }
            fdirs.close();
            ffiles.close();
            funiq.close();
            fin.close();

        } catch (IOException ioe)
        {
            ioe.printStackTrace();
            Logger.Severe(String.format("Caught IOE : %s in save lc=%d, f=%d", ioe.toString(), lc, f));
        }
     
    }
    
    String getNextLine(BufferedReader f)
    {
        while(true)
        {
            try
            {
                String line = f.readLine();
                return line;
            }
            catch(MalformedInputException e) // sometimes filename have unprintable characters - ignore them
            {
                continue;
            }     
            catch(IOException e) // other exceptions just end
            {
                Logger.Severe("Error reading next line");
                return null;
            }  
            
        }
    }
    // filesMatchingImage. Find all files with this image
    public List<ImgCollectionFileItem> filesMatchingImage(ImgCollectionImageItem img)
    {
        List<ImgCollectionFileItem> list = new LinkedList<>();
        for ( ImgCollectionFileItem item : files )
        {
            if ( item.getIHash() == img.getIHash() )
                list.add(item);
        }
        return list;
    }
    
    public ImgCollectionImageItem getImageItemForFileItem(ImgCollectionFileItem f)
    {
        if ( images.containsKey(f.getIHash()))
            return images.get(f.getIHash());
        return null;
    }

    // getDirPath. return the path associated with this diritem taking into account
    // the top value
    public Path getDirPath(ImgCollectionDirItem d) {
        if ( d != null) 
            return Paths.get(top,d.getDir());
        return null;
    }

    // getFilePath. return the Path of the file
    public Path getFilePath(ImgCollectionFileItem file) {
        // find the diritem associated with the file
        ImgCollectionDirItem d = GetDirByHash(file.getDhash());
        if ( d != null )
        {
            Path dp = getDirPath(d);
            // return path formed from the diritem path and the filename
            dp = dp.resolve(file.getFile());
            return dp;
        }
        return null;
    }

    public Integer getNumberOfImages() {
        return images.size();
    }

}
