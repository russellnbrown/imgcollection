using NLog;
using Scanner;
using System;

namespace imgcli
{
    class AddScan
    {
        private static Logger log = LogManager.GetCurrentClassLogger();

        public AddScan(string []args)
        {
            if (args.Length < 2)
                throw new Exception("Not enough args for AddScan ( set path )");

            Set s = new Set(true);
            s.Load(args[1]);
            s.Scan(args[2]);
            s.StopAnyProcessingThreads();

            s.Save();
//            s.SaveAs("C:/TestEnvironments/sets2/setimgX1");
        }

    }
}
