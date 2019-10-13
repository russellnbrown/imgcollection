using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace cs_build_scan
{
    class Search
    {
        Set set = new Set();

        public Search(string v1, string v2)
        {
            string fileToSearch = v2;
            string setName = v1;

            if (!File.Exists(v2))
                l.Fatal("File to search does not exist: ", fileToSearch);

            if ( !set.Load(setName) )
                l.Fatal("Set does not exist: ", setName);

            ImgFileInfo ifi = new ImgFileInfo(null, new FileInfo(fileToSearch));
            ifi.MakeThumb();

            SortedList<double, Closeness> matches = new SortedList<double, Closeness>(new DuplicateKeyComparer<double>());

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
            foreach (var m in matches)
            {
                l.Info("Match {0}, files:", m);
                List<Set.FileEntry> files = set.FindFilesForImage(m.Value.Ihash);
                if (files != null)
                    foreach (Set.FileEntry fe in files)
                        l.Info("\t{0}", fe);
            }


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
