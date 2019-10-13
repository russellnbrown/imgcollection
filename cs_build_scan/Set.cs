using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace cs_build_scan
{
    public class Set
    {
        string top = "";
        string location = "";
        List<DirEntry> dirs = new List<DirEntry>();
        List<FileEntry> files = new List<FileEntry>();
        SortedDictionary<UInt32, ImgEntry> images = new SortedDictionary<UInt32, ImgEntry>();

        public class DirEntry
        {
            public UInt32 dhash;
            public string path;
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

        internal SortedDictionary<UInt32, ImgEntry> GetImages()
        {
            return images;
        }

        public class FileEntry
        {
            public FileEntry(UInt32 _dhash, UInt32 _crc, string _name)
            {
                dhash = _dhash;
                crc = _crc;
                name = _name;
            }
            public UInt32 dhash;
            public UInt32 crc;
            public string name;
            public override string ToString()
            {
                return String.Format("File[ dhash:{0}, name:{1}, crc:{2}]", dhash, name, crc);
            }
        }

        public class ImgEntry
        {
            public ImgEntry(UInt32 _crc, byte[] _thumb)
            {
                crc = _crc;
                thumb = _thumb.ToArray();
            }
            public UInt32 crc;
            public byte[] thumb;
            public override string ToString()
            {
                return String.Format("Img[ crc:{0}]", crc);
            }

        };



        public bool Create(string path)
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

        internal string RelativeToTop(string path)
        {
            string spath = Utils.StandardizePath(path);
            spath = spath.Substring(top.Length);
            if (!spath.StartsWith("/") )
                spath = "/" + spath;
            return spath;
        }

        internal void AddFile(FileInfo f)
        {
            using (ImgFileInfo ifi = new ImgFileInfo(this, f))
            {
                FileEntry fe = new FileEntry(ifi.dhash, ifi.crc, ifi.name);
                ifi.MakeThumb();
                ImgEntry ie = new ImgEntry(ifi.crc, ifi.tmb);

                files.Add(fe);
                if (!images.ContainsKey(ie.crc))
                    images.Add(ie.crc, ie);
                else
                    l.Info("Duplicate image: " + ie);
            }
        }

        internal void AddDir(DirectoryInfo d)
        {
            string rpath = RelativeToTop(d.FullName);
            UInt32 dhash = Utils.GetHash(rpath);
            DirEntry de = new DirEntry(rpath, dhash);
            dirs.Add(de);
        }

        internal string GetTop()
        {
            return top;
        }



        internal void SetTop(string dirPath)
        {
            // get abs path
            string apath = Path.GetFullPath(dirPath);
            top = Utils.StandardizePath(apath);
        }


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

        public void Save()
        {
            string filePath = Path.Combine(location, "files.txt");
            string imgPath = Path.Combine(location, "images.bin");

            using (StreamWriter sw = new StreamWriter(Path.Combine(location, "dirs.txt")))
            {
                sw.WriteLine(top);
                foreach (DirEntry de in dirs)
                    sw.WriteLine(String.Format("{0},{1}", de.dhash, de.path));
            }
            using (StreamWriter sw = new StreamWriter(Path.Combine(location, "files.txt")))
            {
                foreach (FileEntry fe in files)
                    sw.WriteLine(String.Format("{0},{1},{2}", fe.dhash, fe.crc, fe.name));
            }
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


        internal bool Load(string setName)
        {
            string filePath = Path.Combine(setName, "files.txt");
            string dirPath = Path.Combine(setName, "dirs.txt");
            string imgPath = Path.Combine(setName, "images.bin");
            Char[] seps = { ',' };
            string line;

            try
            {

                using (StreamReader sw = new StreamReader(dirPath))
                {
                    top = sw.ReadLine();
                    while ((line = sw.ReadLine()) != null)
                    {
                        string[] parts = line.Split(seps);
                        if (parts.Length == 2)
                        {
                            UInt32 dhash = UInt32.Parse(parts[0]);
                            DirEntry fe = new DirEntry(parts[1], dhash);
                            dirs.Add(fe);
                        }
                    }
                }

                using (StreamReader sw = new StreamReader(filePath))
                {
                    while ((line = sw.ReadLine()) != null)
                    {
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


                using (BinaryReader reader = new BinaryReader(File.Open(imgPath, FileMode.Open)))
                {
                    while (reader.BaseStream.Position != reader.BaseStream.Length)
                    {
                        Int64 crc = reader.ReadInt64();
                        byte[] ba = reader.ReadBytes(Settings.TNMEM);
                        //Console.WriteLine(String.Format("CRC: {0} RGB: {1:X} {2:X} {3:X} \n", crc, ba[0] & 0xFF, ba[1] & 0xFF, ba[2] & 0xFF));
                        ImgEntry ie = new ImgEntry((UInt32)crc, ba);
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
