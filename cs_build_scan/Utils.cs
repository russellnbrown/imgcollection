using DamienG.Security.Cryptography;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace cs_build_scan
{
    public class Utils
    {
        public static string StandardizePath(string p)
        {
            p = p.Replace("\\", "/");
            if (p.Length > 1 && p.EndsWith("/"))
                p = p.Substring(0, p.Length - 1);
            if (p.Length == 0)
                p = "/";
            return p;
        }

        public static UInt32 GetHash(string s)
        {
            byte[] bytes = Encoding.UTF8.GetBytes(s);
            return Crc32.Compute(bytes);
        }

        public static UInt32 GetHash(byte [] bytes)
        {
            return Crc32.Compute(bytes);
        }
    }
}
