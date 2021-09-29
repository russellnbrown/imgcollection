/*
 * Copyright (C) 2019 russell brown
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

using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Threading;

namespace Scanner
{
    // Set - methods & datastructures used to build a database ( aka set )
    public class Set
    {
        private string location = "";                           // where the set is located
        private bool useThreads = true;                         // are we going to use multiple threads
                                                                // for image processing

        // the following constitute the 'set' 
        private string top = "";                                // top path to which all others are relative
        
        private SortedDictionary<UInt32, DirEntry> dirs = new SortedDictionary<UInt32, DirEntry>();   // directories ( relative to top ) index by dir name hash
        private SortedDictionary<UInt64, FileEntry> files = new SortedDictionary<UInt64,FileEntry>();  // files index by dir name hash + file name hash
        private SortedDictionary<UInt32, ImgEntry> images = new SortedDictionary<UInt32, ImgEntry>(); // unique images  indec by image crc

        public SortedDictionary<UInt32, ImgEntry> GetImages() { return images;  }


        // processors is a list of image processing threads
        private List<ImgProcessor> processors = new List<ImgProcessor>();
        // singleton for easy access
        private static Set instance = null;
        public static Set Get { get => instance;  }

        public int NumFiles { get => files.Count; }
        public int NumDirs { get => dirs.Count; }
        public int NumImages { get => images.Count;  }
        public int DupDirs = 0;
        public int DupFiles = 0;
        public int DupImg = 0;
        public string CurrentDir = "";


        //
        // ImgProcessor - implement file loading, crc calc & thumbnail creation
        // in in threads to maximize throughput. Set.processors is a list of these
        // threads. call AddFile to process a file. call Stop to stop
        //
        public class ImgProcessor
        {
            private int             pnum = 0;
            private int             filecount = 0; // keep tabs on how many images we processed
            public int Filecount { get => filecount; set => filecount = value; }
            private bool            running = true;
            private ImgFileInfo     ifi = null;
            private Thread          thr = null;

            // ImgProcessor
            // Stores the thread count and start thread
            public ImgProcessor(int pnum)
            {
                this.pnum = pnum; 
                l.Info("Starting thread {0}", pnum);
                thr = new Thread(new ThreadStart(Run));
                thr.Start();
            }

            // Stop
            // Stop thread. set running to false so that main thread loop exists. call
            // Join to ensure it stopped correctly
            public void Stop()
            {
                l.Info("Stopping thread {0}", pnum);
                running = false;
                if ( thr != null )
                    thr.Join();
                l.Info("Stopped thread {0}, it processes {1} files.", pnum, Filecount);
            }

            // AddFile
            // Calle by Set.AddFile to process a file. If our thread is busy processing
            // an existing file ( ifi not null ) then return false to indicate we couldnt
            // process it. otherwise return true.
            // the thread monitors ifi in its main loop to get/clear a file to process
 
            public bool AddFile(ImgFileInfo _ifi)
            {
                if (ifi != null) // we are busy, reject file
                    return false;

                // not doing anything - we can accept this file, thread will pick it up
                ifi = _ifi;
                //l.Info("File {0} accepted by thread {1}", ifi.name, pnum);
                return true;
            }

            // Run
            // The main thread body. 
            public void Run()
            {
                // loop continuously looking for work to do. We stop when
                // running is set to false ( via Stop )
                //l.Info("Thread {0} is running", pnum);
                while (running)
                {
                    // if ifi is null, then there is nothing to do. have a nap and repeat
                    if ( ifi == null ) // nowt to do
                    {
                        Thread.Sleep(1);
                        continue;
                    }
                    // ifi is set, we have a file to work with
                    //l.Info("Thread {0} is processing {1}", pnum, ifi.name);
                    // read data & make crc32
                    try
                    {
                        ifi.MakeHashes();
                        // mahe the thumb
                        ifi.MakeThumb();
                        filecount++;

                        // Call Set.LoadFile to return crc & thumb and have it entered into set
                        // 'LoadFile' is synchronized to prevent multiple threads from acessing
                        // set structures at the same time
                        Set.Get.LoadFileResult(ifi, pnum);
                    }
                    catch(Exception e)
                    {
                        l.Error("Procedding(thread)" + ifi);
                    }

                    // now set ifi to null to indicate we can accept another file
                    ifi = null;
                }
                //l.Info("Thread {0} is stopping", pnum);
            }
        }

        private UInt64 fileIx( UInt32 dh,  UInt32 fn)
        {
            UInt64 rv = ((UInt64)dh << 32) | fn;
            return rv;
        }

        // LoadFile - this is used by image processing threads, if created, to deposit results into
        // set - it needs to be synchronized to prevent files and images becoming corrupted. 
        // Alternitivly it may just be called by main thread if threading not enabled
        [MethodImpl(MethodImplOptions.Synchronized)]
        public void LoadFileResult(ImgFileInfo ifi, int pmun)
        {

           
            ImgEntry ie = new ImgEntry(ifi.crc, ifi.tmb);

            // now add file entry to files list
            UInt64 dfhash = fileIx(ifi.dhash, ifi.fhash);
            if ( !files.ContainsKey(dfhash) )
            {
                l.Warn("ADDRESULT - No file place for result " + ifi);
                return;
            }
            files[dfhash].crc = ifi.crc;

            l.Info("ADDRESULT - Adding " + ifi);
            // and the imgentry to the images map ( ignore if duplicate )
            if (!images.ContainsKey(ie.crc))
                images.Add(ie.crc, ie);
            else
            {
                DupImg++;               
            }
        }

        // DirEntry - Holds data relevant to a directory
        public class DirEntry
        {
            public UInt32 dhash;    // CRC32 of directory path ( key used in fileent )
            public string path;     // The path ( relative to top )
            public DateTime lastModded;
            public DirEntry(string _path, UInt32 _dhash, DateTime _lastModded)
            {
                dhash = _dhash;
                path = _path;
                lastModded = _lastModded;
            }
            public override string ToString()
            {
                return String.Format("Dir[ dhash:{0}, path:{1}]", dhash, path);
            }
        }

        // FileEntry - Holds data relevant to a file
        public class FileEntry
        {
            public UInt32 dhash;    // key to find relevant directory
            public UInt32 fhash;    // key to find relevant file ( with above )
            public UInt32 crc;      // key to find relecant image
            public string name;     // file name 
            public FileEntry(UInt32 _dhash, UInt32 _fhash, UInt32 _crc, string _name)
            {
                dhash = _dhash;
                fhash = _fhash;
                crc = _crc;
                name = _name;
            }
 
            public override string ToString()
            {
                return String.Format("File[ dhash:{0}, fhash{1}: name:{2}, crc:{3}]", dhash, fhash, name, crc);
            }
        }

        // ImgEntry - holds data relevant to an image
        public class ImgEntry
        {
            public UInt32 crc;      // key used to link to file
            public byte[] thumb;    // the thumbnail
            public ImgEntry(UInt32 _crc, byte[] _thumb)
            {
                crc = _crc;
                thumb = _thumb.ToArray();
            }
            public override string ToString()
            {
                return String.Format("Img[ crc:{0}]", crc);
            }

        };

        // Set constructor
        // set instance singleton to allow easy access via 'Get'
        public Set(bool useThreads)
        {
            instance = this;
            this.useThreads = useThreads;
        }

        // Initialize
        // basic initialization, check set location  exists and store it. Start 
        // any image processing threads
        public bool Initialize(string scanTop, string path)
        {
            try 
            {            
                Directory.CreateDirectory(path);
                l.Info("Create set directory " + path);
            }
            catch (Exception e)
            {
                l.Error("Could not create set directory " + path + " : " + e.Message);
                    return false;
            }
        
            location = path;
            top = scanTop;

            // nothing else to do if not using threads
            if (!useThreads)
            {
                l.Info("Not using threads");
                return true;
            }

            // otherwise we need to create the image processing threads
            // how many ? number of processors * 2 seems to work well, fewer
            // or more seems to be slower or same
            int nThreads = Environment.ProcessorCount * 2;
            if (nThreads < 1)
                nThreads = 1;
            l.Info("Using {0} image processing threads", nThreads);
            for(int p = 0; p < nThreads; p++)
                processors.Add(new ImgProcessor(p));
            return true;
        }

        // RelativeToTop 
        // Takes an absolute path and returns the part reletive to top 
        public string RelativeToTop(string path)
        {
            string spath = Utils.StandardizePath(path); // make sure in unix format
            spath = spath.Substring(top.Length); // remove the top from the path
            if (!spath.StartsWith("/") ) // make sure it starts with a '/'
                spath = "/" + spath;
            return spath;
        }

        // AddFile
        // Add a file to the set - get thumb & crc etc and create relevant 
        // file & img entries and  add to list/map
        public void AddFile(FileInfo f)
       {
            ImgFileInfo ifi = new ImgFileInfo(this, f);

            UInt64 dfhash = fileIx(ifi.dhash, ifi.fhash);

            if ( files.ContainsKey(dfhash) )
            {
                l.Warn("ADDFILE - Duplicate file, ignore " + ifi);
                DupFiles++;
                return;
            }

            FileEntry fe = new FileEntry(ifi.dhash, ifi.fhash, ifi.crc, ifi.name);
            files.Add(dfhash, fe);

            if ( !useThreads )
            {
                try
                {
                    ifi.MakeHashes();
                    // mahe the thumb
                    ifi.MakeThumb();
                    l.Warn("ADDFILE - adding " + ifi + " : " + fe);
                    LoadFileResult(ifi, 0);
                }
                catch(Exception e)
                {
                    l.Error("Processing " + ifi);
                }
                return;
            }
            while (true)
            {
                foreach (ImgProcessor ip in processors)
                {
                    if (ip.AddFile(ifi))
                        return;
                }
                System.Threading.Thread.Sleep(1);
            }
        }

        // AddDir
        // Create a direntry for a directory & add to dir list
        public UInt32 AddDir(DirectoryInfo d)
        {
            // Get directories path relevant to top
            string rpath = RelativeToTop(d.FullName);
            // and the CRC32 for use as key to file list
            UInt32 dhash = Utils.GetHash(rpath);

            l.Info("ADDDIR " + d.FullName);

            if ( dirs.ContainsKey(dhash) )
            {
                if (dirs[dhash].lastModded == d.LastWriteTime)
                {
                    l.Info("ADDDIR, already there and unmodified " + d.FullName + ", hash:" + dhash);
                    return 0; // existing and unmodified
                }
                dirs[dhash].lastModded = d.LastWriteTime;
                l.Info("ADDDIR, already there but modified " + d.FullName + ", hash:" + dhash);
                CurrentDir = rpath;
                return dhash;
            }

            DirEntry de = new DirEntry(rpath, dhash,d.LastWriteTime);
            CurrentDir = rpath;
            l.Info("ADDDIR, new dir " + d.FullName + ", hash:" + dhash);
            dirs.Add(dhash, de);
            return dhash;
        }


        // GetTop
        // return path of set to which all dirs are relative to
        public string GetTop()
        {
            return top;
        }


        // SetTop
        // save the set top directory
        public void SetTop(string dirPath)
        {
            // get abs path
            string apath = Path.GetFullPath(dirPath);
            // unixify it
            top = Utils.StandardizePath(apath);
        }

        // Dump
        // Debug to write out set entries
        public void Dump()
        {
            l.Info("Set dump, top: " + top);
            l.Info("Directories:");
            foreach (DirEntry de in dirs.Values)
                l.Info("\tDir: " + de.ToString());
            l.Info("Files:");
            foreach (FileEntry fe in files.Values)
                l.Info("\tFile: " + fe.ToString());
            l.Info("Images:");
            foreach (ImgEntry ie in images.Values)
                l.Info("\tImg: " + ie.ToString());
        }

        // Save
        // Save the database ( aka set ) to disk. ( can't use serialization as format must be common to all
        // languages ) 
        public void Save()
        {
            // set is saved over three files in a directory

            // the dirent list
            using (StreamWriter sw = new StreamWriter(Path.Combine(location, "dirs.txt")))
            {
                sw.WriteLine(top);
                foreach (DirEntry de in dirs.Values)
                    sw.WriteLine(String.Format("{0},{1},{2}", de.dhash, de.path,de.lastModded.ToString()));
            }

            // the fileent list
            using (StreamWriter sw = new StreamWriter(Path.Combine(location, "files.txt")))
            {
                foreach (FileEntry fe in files.Values)
                    sw.WriteLine(String.Format("{0},{1},{2},{3}", fe.dhash, fe.fhash, fe.crc, fe.name));
            }

            // the imgentry map ( save in binary for speed )
            using (BinaryWriter sw = new BinaryWriter(new FileStream(Path.Combine(location, "images.bin"), FileMode.Create)))
            {
                foreach (ImgEntry ie in images.Values)
                {
                    Int64 c = ie.crc;
                    sw.Write(c);
                    sw.Write(ie.thumb);
                }
            }
        }

        //
        // Add
        public bool Add(string setName)
        {
            string filePath = Path.Combine(setName, "files.txt");
            string dirPath = Path.Combine(setName, "dirs.txt");
            string imgPath = Path.Combine(setName, "images.bin");
            Char[] seps = { ',' };
            string line;

            //location = setName;

            try
            {
                // dirs.txt contaiins the directories and top
                using (StreamReader sw = new StreamReader(dirPath))
                {
                    // top is in first line
                    string mytop = sw.ReadLine();
                    if ( mytop != top )
                    {
                        l.Error("Top dosnt match");
                        return false;
                    }
                    while ((line = sw.ReadLine()) != null)
                    {
                        // all other lines are key/path for the directoreis
                        string[] parts = line.Split(seps);
                        if (parts.Length == 3)
                        {
                            UInt32 dhash = UInt32.Parse(parts[0]);
                            if (!dirs.ContainsKey(dhash))
                            {
                                DirEntry fe = new DirEntry(parts[1], dhash, DateTime.Parse(parts[2]));
                                dirs.Add(dhash, fe);
                            }
                            else
                                l.Debug("Dup dir " + line);
                        }
                    }
                }
                // files.txt containf the files, create a fileentry for each 
                using (StreamReader sw = new StreamReader(filePath))
                {
                    while ((line = sw.ReadLine()) != null)
                    {
                        // line is a csv of directory key, image key & name
                        string[] parts = line.Split(seps);
                        if (parts.Length == 4)
                        {
                            UInt32 dhash = UInt32.Parse(parts[0]);
                            UInt32 fhash = UInt32.Parse(parts[1]);
                            UInt32 crc = UInt32.Parse(parts[2]);
                            FileEntry fe = new FileEntry(dhash, fhash, crc, parts[3]);
                            l.Info("LOADTOMAP " + dhash + " " + parts[3]);
                            UInt64 dfhash = fileIx(dhash, fhash);
                            if (!files.ContainsKey(dfhash))
                                files.Add(dfhash, fe);
                            else
                                l.Debug("Dup file " + line);
                        }

                    }
                }

                // images.bin contains the image keys & thumbnails
                using (BinaryReader reader = new BinaryReader(File.Open(imgPath, FileMode.Open)))
                {
                    while (reader.BaseStream.Position != reader.BaseStream.Length)
                    {
                        // read key & thumb and create a imgentrt
                        Int64 crc = reader.ReadInt64();
                        byte[] ba = reader.ReadBytes(Settings.TNMEM);
                        //Console.WriteLine(String.Format("CRC: {0} RGB: {1:X} {2:X} {3:X} \n", crc, ba[0] & 0xFF, ba[1] & 0xFF, ba[2] & 0xFF));
                        ImgEntry ie = new ImgEntry((UInt32)crc, ba);
                        // add add to the map ( no need to check for dups at the cant be any )
                        if (!images.ContainsKey(ie.crc))
                            images.Add(ie.crc, ie);
                        else
                            l.Debug("Dup Image " + ie.crc);
                    }
                }
            }
            catch (Exception e)
            {
                l.Error("Error ", e.Message, " reading set");
                return false;
            }

            l.Info("Read set {0}, Dirs: {1}, Files: {2}, Images: {3} ", setName, dirs.Count, files.Count, images.Count);
            return true;
        }


        public bool Load(string setName)
        {
            string filePath = Path.Combine(setName, "files.txt");
            string dirPath = Path.Combine(setName, "dirs.txt");
            string imgPath = Path.Combine(setName, "images.bin");
            Char[] seps = { ',' };
            string line;

            location = setName;

            try
            {
                // dirs.txt contaiins the directories and top
                using (StreamReader sw = new StreamReader(dirPath))
                {
                    // top is in first line
                    top = sw.ReadLine();
                    while ((line = sw.ReadLine()) != null)
                    {
                        // all other lines are key/path for the directoreis
                        string[] parts = line.Split(seps);
                        if (parts.Length == 3)
                        {
                            UInt32 dhash = UInt32.Parse(parts[0]);
                            DirEntry fe = new DirEntry(parts[1], dhash, DateTime.Parse(parts[2]));
                            dirs.Add(dhash, fe);
                        }
                    }
                }
                // files.txt containf the files, create a fileentry for each 
                using (StreamReader sw = new StreamReader(filePath))
                {
                    while ((line = sw.ReadLine()) != null)
                    {
                        // line is a csv of directory key, image key & name
                        string[] parts = line.Split(seps);
                        if (parts.Length == 4)
                        {
                            UInt32 dhash = UInt32.Parse(parts[0]);
                            UInt32 fhash = UInt32.Parse(parts[1]);
                            UInt32 crc = UInt32.Parse(parts[2]);
                            FileEntry fe = new FileEntry(dhash, fhash, crc, parts[3]);
                            l.Info("LOADTOMAP " + dhash + " " + parts[3]);
                            UInt64 dfhash = fileIx( dhash, fhash);
                            files.Add(dfhash, fe);
                        }
  
                    }
                }

                // images.bin contains the image keys & thumbnails
                using (BinaryReader reader = new BinaryReader(File.Open(imgPath, FileMode.Open)))
                {
                    while (reader.BaseStream.Position != reader.BaseStream.Length)
                    {
                        // read key & thumb and create a imgentrt
                        Int64 crc = reader.ReadInt64();
                        byte[] ba = reader.ReadBytes(Settings.TNMEM);
                        //Console.WriteLine(String.Format("CRC: {0} RGB: {1:X} {2:X} {3:X} \n", crc, ba[0] & 0xFF, ba[1] & 0xFF, ba[2] & 0xFF));
                        ImgEntry ie = new ImgEntry((UInt32)crc, ba);
                        // add add to the map ( no need to check for dups at the cant be any )
                        images.Add(ie.crc, ie);
                    }
                }
            }
            catch (Exception e)
            {
                l.Error("Error ", e.Message, " reading set");
                return false;
            }

            l.Info("Read set {0}, Dirs: {1}, Files: {2}, Images: {3} ", setName, dirs.Count, files.Count, images.Count);
            return true;
        }

        // FindFilesForImage
        // Search the file list for entries containing a particular image
        // key ( there may be more than one )
        public List<FileEntry> FindFilesForImage(UInt32 ihash)
        {
            List<FileEntry> matches = null;

            foreach(FileEntry fe in files.Values)
            {
                if ( fe.crc == ihash )
                {
                    if (matches == null)
                        matches = new List<FileEntry>();
                    matches.Add(fe);
                }
            }
            return matches;
        }

        public void StopAnyProcessingThreads()
        {
            if (!useThreads)
                return;

            l.Info("Stopping all threads");
            foreach (var p in processors)
                p.Stop();
            l.Info("All threads stopped");
        }


    }
}
