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
using System.Threading;

namespace cs_build_scan
{
    // l - provide a basic console/file logging 
    public class l
    {
        // the file log and min level to log to it
        private static System.IO.StreamWriter logs = null;
        private static Level minLevelLogged = Level.Info;

        public static Level MinLogLevel
        {
            get { return l.minLevelLogged; }
            set { l.minLevelLogged = value; }
        }

        // the min level logged to console
        private static Level miConsoleLevelLogged = Level.Info;

        public static Level MinConsoleLogLevel
        {
            get { return l.miConsoleLevelLogged; }
            set { l.miConsoleLevelLogged = value; }
        }

        /// <summary>
        ///  the logging levels available
        /// </summary>
        public enum Level { Nano, Debug, Info, Warn, Error, Fatal };
        private static char[] indicators = { 'N', 'D', 'I', 'W', 'E', 'F' };

        // Timestamp - provide a string to timestamp entry
        private static string Timestamp()
        {
            return DateTime.Now.ToString("HH:mm:ss.fff");
        }

        // The file name to log to
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

        // Write line does the acrual logging to file & console
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
                    Close();
                    Environment.ExitCode = -1;
                    System.Diagnostics.Process.GetCurrentProcess().Kill();
                }
            }
        }

        // pretty sure there is a varargs way to do this....
        public static void Debug(string s) { WriteLine(Level.Debug, s); }
        public static void Debug(string fmt, object o1) { String s = String.Format(fmt, o1); WriteLine(Level.Debug, s); }
        public static void Debug(string fmt, object o1, object o2) { String s = String.Format(fmt, o1, o2); WriteLine(Level.Debug, s); }
        public static void Debug(string fmt, object o1, object o2, object o3) { String s = String.Format(fmt, o1, o2, o3); WriteLine(Level.Debug, s); }
        public static void Debug(string fmt, object o1, object o2, object o3, object o4) { String s = String.Format(fmt, o1, o2, o3, o4); WriteLine(Level.Debug, s); }
 
        public static void Info(string s) { WriteLine(Level.Info, s); }
        public static void Info(string fmt, object o1) { String s = String.Format(fmt, o1); WriteLine(Level.Info, s); }
        public static void Info(string fmt, object o1, object o2) { String s = String.Format(fmt, o1, o2); WriteLine(Level.Info, s); }
        public static void Info(string fmt, object o1, object o2, object o3) { String s = String.Format(fmt, o1, o2, o3); WriteLine(Level.Info, s); }
        public static void Info(string fmt, object o1, object o2, object o3, object o4) { String s = String.Format(fmt, o1, o2, o3, o4); WriteLine(Level.Info, s); }
 
        public static void Warn(string s) { WriteLine(Level.Warn, s); }
        public static void Warn(string fmt, object o1) { String s = String.Format(fmt, o1); WriteLine(Level.Warn, s); }
        public static void Warn(string fmt, object o1, object o2) { String s = String.Format(fmt, o1, o2); WriteLine(Level.Warn, s); }
        public static void Warn(string fmt, object o1, object o2, object o3) { String s = String.Format(fmt, o1, o2, o3); WriteLine(Level.Warn, s); }
        public static void Warn(string fmt, object o1, object o2, object o3, object o4) { String s = String.Format(fmt, o1, o2, o3, o4); WriteLine(Level.Warn, s); }
 
        public static void Error(string s) { WriteLine(Level.Error, s); }
        public static void Error(string fmt, object o1) { String s = String.Format(fmt, o1); WriteLine(Level.Error, s); }
        public static void Error(string fmt, object o1, object o2) { String s = String.Format(fmt, o1, o2); WriteLine(Level.Error, s); }
        public static void Error(string fmt, object o1, object o2, object o3) { String s = String.Format(fmt, o1, o2, o3); WriteLine(Level.Error, s); }
        public static void Error(string fmt, object o1, object o2, object o3, object o4) { String s = String.Format(fmt, o1, o2, o3, o4); WriteLine(Level.Error, s); }
 
        public static void Fatal(string s) { WriteLine(Level.Fatal, s); }
        public static void Fatal(string fmt, object o1) { String s = String.Format(fmt, o1); WriteLine(Level.Fatal, s); }
        public static void Fatal(string fmt, object o1, object o2) { String s = String.Format(fmt, o1, o2); WriteLine(Level.Fatal, s); }
        public static void Fatal(string fmt, object o1, object o2, object o3) { String s = String.Format(fmt, o1, o2, o3); WriteLine(Level.Fatal, s); }
        public static void Fatal(string fmt, object o1, object o2, object o3, object o4) { String s = String.Format(fmt, o1, o2, o3, o4); WriteLine(Level.Fatal, s); }
 

        public static void Close()
        {
            if (logs != null)
                logs.Close();
            logs = null;
        }

    }
}


