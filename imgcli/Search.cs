using Scanner;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static Scanner.Set;

namespace imgcli
{
    class Search
    {
        public  Search(string []args)
        {
            if (args.Length != 3)
                throw new Exception("Not enough args for Sarch ( set path )");

            Set s = new Set(true);
            s.Load(args[1]);
            List<FoundItem> found = s.Search(args[2]);
            foreach(var f in found)
            {
                Console.WriteLine(String.Format("cls {0} fil {1}", f.closeness, f.file));
            }
        }
    }
}
