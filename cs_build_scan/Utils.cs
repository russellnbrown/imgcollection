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

        public static void PrintThumb(string txt, Byte[] t)
        {
            StringBuilder sb = new StringBuilder();
            Console.Write("{0} : Pixels:-\n", txt);
            int ix = 0;
            for (int rx = 0; rx < 16; rx++)
            {
                Console.Write("R %d: ", rx);
                for (int cx = 0; cx < 16; cx++)
                {
                    Console.Write("{0:X}{1:X}{2:X} ", t[ix], t[ix + 1], t[ix + 2]);
                    ix += 3;
                }
                Console.Write("\n");
            }
            Console.Write("\n");
        }
    }
}
