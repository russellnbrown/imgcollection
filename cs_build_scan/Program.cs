using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace cs_build_scan
{
    class Program
    {
        private static void usage()
        {
            Console.WriteLine("usage: cs_build_scan [-c <set> <file path>|-s <set> <file path>]");
        }

        static void Main(string[] args)
        {
            l.To("cs_build_scan.log");

            if (args.Length == 3 && args[0] == "-c")
                new Builder(args[1], args[2]);
            else if (args.Length == 3 && args[0] == "-s")
                new Search(args[1], args[2]);
            else
                usage();
        }
    }
}
