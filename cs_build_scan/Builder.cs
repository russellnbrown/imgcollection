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
using System.Diagnostics;
using System.IO;

namespace cs_build_scan
{
    class Builder
    {
        Set set = null;
        string dirPath;



        public Builder(string scanTop, string setPath, string dirPath)
        {
            Stopwatch stopwatch = new Stopwatch();

            setPath = Path.GetFullPath(setPath);
            dirPath = Path.GetFullPath(dirPath);
            this.dirPath = Path.GetFullPath(dirPath);

            // check files directory exists
            if (!Directory.Exists(dirPath))
                l.Fatal("Directory " + dirPath + " dosn't exist");

            set = new Set(false);

            if ( scanTop == null ) // Append
            {
                if (!Directory.Exists(setPath))
                    l.Fatal("Set dosnt exist " + setPath);
                if (!set.Load(setPath))
                    l.Fatal("Set does not exist: ", setPath);
                scanTop = set.GetTop();
            }
            else
            {
                if (!Directory.Exists(scanTop))
                    l.Fatal("Scan Top " + scanTop + " dosn't exist");
                scanTop = Path.GetFullPath(scanTop);
                if (Directory.Exists(setPath))
                    l.Fatal("Set already exists " + setPath);
                if (!set.Initialize(scanTop, setPath))
                    l.Fatal("Could not create " + setPath);
            }




            stopwatch.Start();
            build();
            stopwatch.Stop();
            long buildms = stopwatch.ElapsedMilliseconds;

            stopwatch.Restart();
            set.Save();
            stopwatch.Stop();
            long savems = stopwatch.ElapsedMilliseconds;

            l.Info("Timings:-");
            l.Info("\tbuild - " + buildms);
            l.Info("\tsave - " + savems);


        }



        private void build()
        {
            DirectoryInfo dtop = new DirectoryInfo(dirPath);
            walk(dtop);
            set.StopAnyProcessingThreads();
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
