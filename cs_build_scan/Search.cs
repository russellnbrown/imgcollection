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
using System.Diagnostics;
using System.IO;

namespace cs_build_scan
{
    class Search
    {
        Set set = new Set();

        public Search(string v1, string v2)
        {
            string fileToSearch = v2;
            string setName = v1;
            Stopwatch stopwatch = new Stopwatch();

            if (!File.Exists(v2))
                l.Fatal("File to search does not exist: ", fileToSearch);

            stopwatch.Start();
            if ( !set.Load(setName) )
                l.Fatal("Set does not exist: ", setName);
            stopwatch.Stop();
            long loadt = stopwatch.ElapsedMilliseconds;

            ImgFileInfo ifi = new ImgFileInfo(null, new FileInfo(fileToSearch));
            ifi.MakeThumb();

            SortedList<double, Closeness> matches = new SortedList<double, Closeness>(new DuplicateKeyComparer<double>());

            stopwatch.Restart();
            foreach(Set.ImgEntry ie in set.GetImages().Values)
            {
                Closeness c;

                if (ie.crc == ifi.crc)
                    c = new Closeness(ie, 0.0);
                else
                    c = new Closeness(ie, ifi.tmb);

                if (c.Close < 20000)
                    matches.Add(c.Close, c);
            }
            stopwatch.Stop();
            long searcht = stopwatch.ElapsedMilliseconds;

            foreach (var m in matches)
            {
                l.Info("Match {0}, files:", m);
                List<Set.FileEntry> files = set.FindFilesForImage(m.Value.Ihash);
                if (files != null)
                    foreach (Set.FileEntry fe in files)
                        l.Info("\t{0}", fe);
            }
            l.Info("Timings:-");
            l.Info("\tload - " + loadt);
            l.Info("\tsearch - " + searcht);


        }
    }

    public class DuplicateKeyComparer<TKey> :  IComparer<TKey> where TKey : IComparable
    {
        public int Compare(TKey x, TKey y)
        {
            int result = x.CompareTo(y);

            if (result == 0)
                return 1;   // Handle equality as beeing greater
            else
                return result;
        }
    }

}
