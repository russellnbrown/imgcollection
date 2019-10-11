using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace cs_build_scan
{
    class Builder
    {
        Set set = new Set();

        public Builder(string setPath, string dirPath)
        {
            // check files directory exists
            if ( !Directory.Exists(dirPath) )
                l.Fatal("Directory " + dirPath + " dosn't exist");
            // Open the DB set
            if (!set.Create(setPath) )
                l.Fatal("Could not create " + setPath);

            set.SetTop(dirPath);
            build();
            set.Save();

        }


        private void build()
        {
            DirectoryInfo dtop = new DirectoryInfo(set.GetTop());
            walk(dtop);
        }

        private int processDirectory(DirectoryInfo d)
        {
            l.Info("DIR: " + d.Name  );
            set.AddDir(d);
            return 0;
        }

        private void processFile(FileInfo f)
        {
            l.Info("FILE:" + f.FullName);
            set.AddFile(f);
        }

        private void walk(DirectoryInfo root)
        {
            System.IO.FileInfo[] files = null;
            System.IO.DirectoryInfo[] subDirs = null;

            int dhash = processDirectory(root);
            try
            {

                // First, process all the files directly under this folder
                files = root.GetFiles("*.*");

                if (files != null)
                {
                    foreach (System.IO.FileInfo fi in files)
                        processFile(fi);

                    subDirs = root.GetDirectories();
                    foreach (System.IO.DirectoryInfo dirInfo in subDirs)
                        walk(dirInfo);
                }
            }
            catch (UnauthorizedAccessException e)
            {
                l.Error(e.Message);
            }
            catch (System.IO.DirectoryNotFoundException e)
            {
                l.Fatal("Directory not found: " + root);
            }
        }
    }
}
