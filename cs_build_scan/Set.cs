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

namespace cs_build_scan
{
    // Set - methods & datastructures used to build a database ( aka set )
    public class Set
    {
        private string location = "";                           // where the set is located

        // the following constitute the 'set' 
        private string top = "";                                // top path to which all others are relative
        private List<DirEntry> dirs = new List<DirEntry>();     // directories ( relative to top )
        private List<FileEntry> files = new List<FileEntry>();  // files
        private SortedDictionary<UInt32, ImgEntry> images = 
            new SortedDictionary<UInt32, ImgEntry>();           // unique images 
        internal SortedDictionary<UInt32, ImgEntry> GetImages() { return images;  }

        // DirEntry - Holds data relecant to a directory
        public class DirEntry
        {
            public UInt32 dhash;    // CRC32 of directory path ( key used in fileent )
            public string path;     // The path ( relative to top )
            public DirEntry(string _path, UInt32 _dhash)
            {
                dhash = _dhash;
                path = _path;
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
            public UInt32 crc;      // key to find relecant image
            public string name;     // file name 
            public FileEntry(UInt32 _dhash, UInt32 _crc, string _name)
            {
                dhash = _dhash;
                crc = _crc;
                name = _name;
            }
 
            public override string ToString()
            {
                return String.Format("File[ dhash:{0}, name:{1}, crc:{2}]", dhash, name, crc);
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

        // Initialize
        // basic initialization, check set location
        // exists and store it
        public bool Initialize(string path)
        {
 
            if (!Directory.Exists(path))
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
            }
            location = path;
            return true;
        }

        // RelativeToTop 
        // Takes an absolute path and returns the part reletive to top 
        internal string RelativeToTop(string path)
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
        internal void AddFile(FileInfo f)
        {
            // ImgFileInfo will get us the files crc
            using (ImgFileInfo ifi = new ImgFileInfo(this, f))
            {
                // make a fileentry
                FileEntry fe = new FileEntry(ifi.dhash, ifi.crc, ifi.name);
                // mahe the thumb
                ifi.MakeThumb();
                // make an imgentry
                ImgEntry ie = new ImgEntry(ifi.crc, ifi.tmb);

                // now add file entry to files list
                files.Add(fe);
                // and the imgentry to the images map ( ignore if duplicate )
                if (!images.ContainsKey(ie.crc))
                    images.Add(ie.crc, ie);
                else
                    l.Info("Duplicate image: " + ie);
            }
        }

        // AddDir
        // Create a direntry for a directory & add to dir list
        internal void AddDir(DirectoryInfo d)
        {
            // Get directories path relevant to top
            string rpath = RelativeToTop(d.FullName);
            // and the CRC32 for use as key to file list
            UInt32 dhash = Utils.GetHash(rpath);
            DirEntry de = new DirEntry(rpath, dhash);
            // add to list
            dirs.Add(de);
        }

        // GetTop
        // return path of set to which all dirs are relative to
        internal string GetTop()
        {
            return top;
        }


        // SetTop
        // save the set top directory
        internal void SetTop(string dirPath)
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
            foreach (DirEntry de in dirs)
                l.Info("\tDir: " + de.ToString());
            l.Info("Files:");
            foreach (FileEntry fe in files)
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
                foreach (DirEntry de in dirs)
                    sw.WriteLine(String.Format("{0},{1}", de.dhash, de.path));
            }

            // the fileent list
            using (StreamWriter sw = new StreamWriter(Path.Combine(location, "files.txt")))
            {
                foreach (FileEntry fe in files)
                    sw.WriteLine(String.Format("{0},{1},{2}", fe.dhash, fe.crc, fe.name));
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

        // Load
        // Loads the set from disk  ( can't use serialization as format must be common to all
        // languages ) 
        internal bool Load(string setName)
        {
            string filePath = Path.Combine(setName, "files.txt");
            string dirPath = Path.Combine(setName, "dirs.txt");
            string imgPath = Path.Combine(setName, "images.bin");
            Char[] seps = { ',' };
            string line;

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
                        if (parts.Length == 2)
                        {
                            UInt32 dhash = UInt32.Parse(parts[0]);
                            DirEntry fe = new DirEntry(parts[1], dhash);
                            dirs.Add(fe);
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
                        if (parts.Length == 3)
                        {
                            UInt32 dhash = UInt32.Parse(parts[0]);
                            UInt32 crc = UInt32.Parse(parts[1]);
                            FileEntry fe = new FileEntry(dhash, crc, parts[2]);
                            files.Add(fe);
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

            foreach(FileEntry fe in files)
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


    }
}
