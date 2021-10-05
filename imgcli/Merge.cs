using NLog;
using Scanner;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace imgcli
{
    class Merge
    {
        private static Logger log = LogManager.GetCurrentClassLogger();

        public Merge(string []args)
        {
            if (args.Length < 4)
                throw new Exception("No files sprcified for merge");

            int ap = 1;
           // string relativeTo = args[ap++];
            string outputSet = args[ap++];

            Set s = new Set(false);
            s.Load(args[ap++]);
            // s.Create(relativeTo, outputSet);
            for (int x = 2; x < args.Length-1; x++)
            {
                Set ms = new Set(false);
                ms.Load(args[ap++]);
                s.Append(ref ms);
            }
            s.SaveAs(outputSet);


        }

    }
}
