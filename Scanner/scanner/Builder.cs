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

namespace Scanner
{
    class Builder
    {
        public Set set = null;
        public string relativeTo = "";
        public string scanFrom = "";
        public string setPath = "";
        public bool isBusy = false;
        public string status = "Not Initialized";
        public bool error = false;

        public string GetCurrnetState()
        {
            return status;
        }
        public bool IsError()
        {
            return error;
        }

        public bool IsBusy()
        {
            return isBusy;
        }

        public Builder()
        {
            set = new Set(false);
        }

        private bool setError(string e)
        {
            isBusy = false;
            error = true;
            status = e;
            l.Error(e);
            return false;
        }

        private void setStatus(string e)
        {
            error = false;
            isBusy = true;
            status = e;
            l.Info(e);
        }

        private bool setFinal(string e)
        {
            error = false;
            isBusy = false;
            status = e;
            l.Info(e);
            return true;
        }


        public bool OpenExisting(string setPath)
        {
            this.setPath = Path.GetFullPath(setPath);

            setStatus("Loading " + setPath);
            if (!Directory.Exists(setPath))
                return setError("Set dosnt exist " + setPath);

            if (!set.Load(setPath))
                return setError("Load Error");

            relativeTo = set.GetTop();
            return setFinal("Loaded " + setPath);
        }

        public bool CreateNew(string relativeTo, string setPath)
        {
                setStatus("Creating " + setPath);

                this.setPath = Path.GetFullPath(setPath);
                this.relativeTo = Path.GetFullPath(relativeTo);

                if (!Directory.Exists(relativeTo))
                    return setError(relativeTo + " does not exist");

                if (File.Exists(setPath+"/dirs.txt"))
                    return setError("Set already exists " + setPath);
                if (!set.Initialize(relativeTo, setPath))
                    return setError("Could not initialize " + setPath);

                return setFinal("Created new set at " + setPath);
            }

            public bool StartScan(string scanPos)
            {
                this.scanFrom = Path.GetFullPath(scanPos);
                Stopwatch stopwatch = new Stopwatch();

                setStatus("Scanning");
                stopwatch.Start();
                build();
                stopwatch.Stop();
                long buildms = stopwatch.ElapsedMilliseconds;

                setStatus("Saving");
                stopwatch.Restart();
                set.Save();
                stopwatch.Stop();
                long savems = stopwatch.ElapsedMilliseconds;

                l.Info("Timings:-");
                l.Info("\tbuild - " + buildms);
                l.Info("\tsave - " + savems);
                return setFinal("Saved");

        }



        public void build()
        {
            setStatus("Scanning");
            DirectoryInfo dtop = new DirectoryInfo(scanFrom);
            walk(dtop);
            set.StopAnyProcessingThreads();
            setFinal("Finished");
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

            setStatus("In " + root.FullName);
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
