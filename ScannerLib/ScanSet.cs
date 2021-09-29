using NLog;
using System;
using System.IO;

namespace Scanner
{
    public class ScanSet
    {
        private static Logger log = LogManager.GetCurrentClassLogger();
        private Set set = null;

        public bool Load(string filePath)
        {
            if (set != null)
                throw new Exception("Load - Set is already created");
            set = new Set(false);
            set.Load(filePath);
            log.Info("Loading " + filePath);
            return false;
        }

        public bool SaveTo(string filePath)
        {
            log.Info("Save To " + filePath);
            return false;
        }

        public bool Save()
        {
            log.Info("Save ");
            return false;
        }

        public bool Append(ref ScanSet other)
        {
            if (set == null)
                throw new Exception("Append - No set to append to");
            log.Info("Append " + other);
            return false;

        }

        public bool Create(string relativeTo, string filePath)
        {
            log.Info("Create " + filePath + " with top of " + relativeTo);

            if (File.Exists(filePath + "/dirs.txt"))
                throw new Exception("Create Error:  - Output set already exists:" + filePath);

            return false;

        }

    }
}
