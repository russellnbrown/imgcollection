using NLog;
using System;

namespace imgcli
{
    class Program
    {
        private static Logger log = LogManager.GetCurrentClassLogger();

        private static void usage()
        {
            log.Fatal("usage: imgcli [-merge <outset> <inset1> <inset...>]");
            Environment.Exit(-1);
        }

        static void Main(string[] args)
        {
            if (args.Length < 1)
                usage();

            log.Info("Running");

            try
            {
                switch (args[0])
                {
                    case "-merge":
                        new Merge(args);
                        break;

                    case "-addscan":
                        new AddScan(args);
                        break;

                    case "-search":
                        new Search(args);
                        break;

                    default:
                        usage();
                        break;
                }
            }
            catch(Exception e)
            {
                log.Error("Exception raised " + e.Message);
                log.Error("Stack " + e.StackTrace);
                log.Fatal(e);
            }
        }
    }
}
