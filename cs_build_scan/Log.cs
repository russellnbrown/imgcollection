using System;
using System.Collections.Generic;
using System.Threading;
using System.Text;

namespace cs_build_scan
{
    public class l
    {

        private static System.IO.StreamWriter logs = null;
        private static Level minLevelLogged = Level.Info;

        public static Level MinLogLevel
        {
            get { return l.minLevelLogged; }
            set { l.minLevelLogged = value; }
        }

        private static Level miConsoleLevelLogged = Level.Info;

        public static Level MinConsoleLogLevel
        {
            get { return l.miConsoleLevelLogged; }
            set { l.miConsoleLevelLogged = value; }
        }

        public enum Level { Nano, Debug, Info, Warn, Error, Fatal };
        private static char[] indicators = { 'N', 'D', 'I', 'W', 'E', 'F' };

        private static string Timestamp()
        {
            return DateTime.Now.ToString("HH:mm:ss.fff");
        }

        public static void To(string file)
        {
            if (logs != null)
            {
                WriteLine(Level.Warn, "Attempt to reopen log, name=" + file);
                return;
            }

            try
            {
                logs = new System.IO.StreamWriter(file);
            }
            catch (System.Exception e)
            {
                Console.WriteLine("Error opening " + file + " " + e.Message);
            }
        }

        public static void WriteLine(Level lvl, string l)
        {
            lock (logs)
            {

                if (lvl < minLevelLogged)
                    return;

                Int32 tix = Thread.CurrentThread.ManagedThreadId;

                string lt = "U";
                switch (lvl)
                {
                    case Level.Debug: lt = "D"; break;
                    case Level.Nano: lt = "N"; break;
                    case Level.Error: lt = "E"; break;
                    case Level.Fatal: lt = "F"; break;
                    case Level.Info: lt = "I"; break;
                    case Level.Warn: lt = "W"; break;
                }
                string dt = DateTime.Now.ToShortTimeString();

                string outs = String.Format("[ -{0}- {1} ({2})] {3}", lt, dt, tix, l);

                if (logs != null)
                {
                    logs.WriteLine(outs);
                    logs.Flush();
                }

                if (lvl >= miConsoleLevelLogged)
                    Console.WriteLine(outs);


                if (lvl == Level.Fatal)
                {
                    Environment.ExitCode = -1;
                    System.Diagnostics.Process.GetCurrentProcess().Kill();
                }
            }
        }


        public static void WriteLine(Level lvl, string fmt, object o1) { String s = String.Format(fmt, o1); WriteLine(lvl, s); }
        public static void WriteLine(Level lvl, string fmt, object o1, object o2) { String s = String.Format(fmt, o1, o2); WriteLine(lvl, s); }
        public static void WriteLine(Level lvl, string fmt, object o1, object o2, object o3) { String s = String.Format(fmt, o1, o2, o3); WriteLine(lvl, s); }
        public static void WriteLine(Level lvl, string fmt, object o1, object o2, object o3, object o4) { String s = String.Format(fmt, o1, o2, o3, o4); WriteLine(lvl, s); }
        public static void WriteLine(Level lvl, string fmt, object o1, object o2, object o3, object o4, object o5) { String s = String.Format(fmt, o1, o2, o3, o4, o5); WriteLine(lvl, s); }
        public static void WriteLine(Level lvl, string fmt, object o1, object o2, object o3, object o4, object o5, object o6) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6); WriteLine(lvl, s); }
        public static void WriteLine(Level lvl, string fmt, object o1, object o2, object o3, object o4, object o5, object o6, object o7) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6, o7); WriteLine(lvl, s); }
        public static void WriteLine(Level lvl, string fmt, object o1, object o2, object o3, object o4, object o5, object o6, object o7, object o8) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6, o7, o8); WriteLine(lvl, s); }

        public static void Debug(string s) { WriteLine(Level.Debug, s); }
        public static void Debug(string fmt, object o1) { String s = String.Format(fmt, o1); WriteLine(Level.Debug, s); }
        public static void Debug(string fmt, object o1, object o2) { String s = String.Format(fmt, o1, o2); WriteLine(Level.Debug, s); }
        public static void Debug(string fmt, object o1, object o2, object o3) { String s = String.Format(fmt, o1, o2, o3); WriteLine(Level.Debug, s); }
        public static void Debug(string fmt, object o1, object o2, object o3, object o4) { String s = String.Format(fmt, o1, o2, o3, o4); WriteLine(Level.Debug, s); }
        public static void Debug(string fmt, object o1, object o2, object o3, object o4, object o5) { String s = String.Format(fmt, o1, o2, o3, o4, o5); WriteLine(Level.Debug, s); }
        public static void Debug(string fmt, object o1, object o2, object o3, object o4, object o5, object o6) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6); WriteLine(Level.Debug, s); }
        public static void Debug(string fmt, object o1, object o2, object o3, object o4, object o5, object o6, object o7) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6, o7); WriteLine(Level.Debug, s); }
        public static void Debug(string fmt, object o1, object o2, object o3, object o4, object o5, object o6, object o7, object o8) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6, o7, o8); WriteLine(Level.Debug, s); }

        public static void Info(string s) { WriteLine(Level.Info, s); }
        public static void Info(string fmt, object o1) { String s = String.Format(fmt, o1); WriteLine(Level.Info, s); }
        public static void Info(string fmt, object o1, object o2) { String s = String.Format(fmt, o1, o2); WriteLine(Level.Info, s); }
        public static void Info(string fmt, object o1, object o2, object o3) { String s = String.Format(fmt, o1, o2, o3); WriteLine(Level.Info, s); }
        public static void Info(string fmt, object o1, object o2, object o3, object o4) { String s = String.Format(fmt, o1, o2, o3, o4); WriteLine(Level.Info, s); }
        public static void Info(string fmt, object o1, object o2, object o3, object o4, object o5) { String s = String.Format(fmt, o1, o2, o3, o4, o5); WriteLine(Level.Info, s); }
        public static void Info(string fmt, object o1, object o2, object o3, object o4, object o5, object o6) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6); WriteLine(Level.Info, s); }
        public static void Info(string fmt, object o1, object o2, object o3, object o4, object o5, object o6, object o7) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6, o7); WriteLine(Level.Info, s); }
        public static void Info(string fmt, object o1, object o2, object o3, object o4, object o5, object o6, object o7, object o8) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6, o7, o8); WriteLine(Level.Info, s); }

        public static void Warn(string s) { WriteLine(Level.Warn, s); }
        public static void Warn(string fmt, object o1) { String s = String.Format(fmt, o1); WriteLine(Level.Warn, s); }
        public static void Warn(string fmt, object o1, object o2) { String s = String.Format(fmt, o1, o2); WriteLine(Level.Warn, s); }
        public static void Warn(string fmt, object o1, object o2, object o3) { String s = String.Format(fmt, o1, o2, o3); WriteLine(Level.Warn, s); }
        public static void Warn(string fmt, object o1, object o2, object o3, object o4) { String s = String.Format(fmt, o1, o2, o3, o4); WriteLine(Level.Warn, s); }
        public static void Warn(string fmt, object o1, object o2, object o3, object o4, object o5) { String s = String.Format(fmt, o1, o2, o3, o4, o5); WriteLine(Level.Warn, s); }
        public static void Warn(string fmt, object o1, object o2, object o3, object o4, object o5, object o6) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6); WriteLine(Level.Warn, s); }
        public static void Warn(string fmt, object o1, object o2, object o3, object o4, object o5, object o6, object o7) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6, o7); WriteLine(Level.Warn, s); }
        public static void Warn(string fmt, object o1, object o2, object o3, object o4, object o5, object o6, object o7, object o8) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6, o7, o8); WriteLine(Level.Warn, s); }

        public static void Error(string s) { WriteLine(Level.Error, s); }
        public static void Error(string fmt, object o1) { String s = String.Format(fmt, o1); WriteLine(Level.Error, s); }
        public static void Error(string fmt, object o1, object o2) { String s = String.Format(fmt, o1, o2); WriteLine(Level.Error, s); }
        public static void Error(string fmt, object o1, object o2, object o3) { String s = String.Format(fmt, o1, o2, o3); WriteLine(Level.Error, s); }
        public static void Error(string fmt, object o1, object o2, object o3, object o4) { String s = String.Format(fmt, o1, o2, o3, o4); WriteLine(Level.Error, s); }
        public static void Error(string fmt, object o1, object o2, object o3, object o4, object o5) { String s = String.Format(fmt, o1, o2, o3, o4, o5); WriteLine(Level.Error, s); }
        public static void Error(string fmt, object o1, object o2, object o3, object o4, object o5, object o6) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6); WriteLine(Level.Error, s); }
        public static void Error(string fmt, object o1, object o2, object o3, object o4, object o5, object o6, object o7) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6, o7); WriteLine(Level.Error, s); }
        public static void Error(string fmt, object o1, object o2, object o3, object o4, object o5, object o6, object o7, object o8) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6, o7, o8); WriteLine(Level.Error, s); }

        public static void Fatal(string s) { WriteLine(Level.Fatal, s); }
        public static void Fatal(string fmt, object o1) { String s = String.Format(fmt, o1); WriteLine(Level.Fatal, s); }
        public static void Fatal(string fmt, object o1, object o2) { String s = String.Format(fmt, o1, o2); WriteLine(Level.Fatal, s); }
        public static void Fatal(string fmt, object o1, object o2, object o3) { String s = String.Format(fmt, o1, o2, o3); WriteLine(Level.Fatal, s); }
        public static void Fatal(string fmt, object o1, object o2, object o3, object o4) { String s = String.Format(fmt, o1, o2, o3, o4); WriteLine(Level.Fatal, s); }
        public static void Fatal(string fmt, object o1, object o2, object o3, object o4, object o5) { String s = String.Format(fmt, o1, o2, o3, o4, o5); WriteLine(Level.Fatal, s); }
        public static void Fatal(string fmt, object o1, object o2, object o3, object o4, object o5, object o6) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6); WriteLine(Level.Fatal, s); }
        public static void Fatal(string fmt, object o1, object o2, object o3, object o4, object o5, object o6, object o7) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6, o7); WriteLine(Level.Fatal, s); }
        public static void Fatal(string fmt, object o1, object o2, object o3, object o4, object o5, object o6, object o7, object o8) { String s = String.Format(fmt, o1, o2, o3, o4, o5, o6, o7, o8); WriteLine(Level.Fatal, s); }


        public static void Close()
        {
            if (logs != null)
                logs.Close();
            logs = null;
        }

        public static Level Parse(string clog)
        {
            string l = clog.ToLower();
            if (l == "warn") return Level.Warn;
            if (l == "error") return Level.Error;
            if (l == "debug") return Level.Debug;
            if (l == "fatal") return Level.Fatal;
            if (l == "info") return Level.Info;
            if (l == "nano") return Level.Nano;
            return Level.Debug;
        }
    }
}


