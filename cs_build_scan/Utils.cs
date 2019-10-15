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

using DamienG.Security.Cryptography;
using System;
using System.Text;

namespace cs_build_scan
{
    // Utils - general utility methods

    public class Utils
    {
        // StandardizePath - makes math conform to a
        // standard unix style as this is what we save to
        // the database

        public static string StandardizePath(string p)
        {
            p = p.Replace("\\", "/");
            if (p.Length > 1 && p.EndsWith("/"))
                p = p.Substring(0, p.Length - 1);
            if (p.Length == 0)
                p = "/";
            return p;
        }

        // GetHash - Change string to bytes and return CRC32 from
        // thirdparty method
        public static UInt32 GetHash(string s)
        {
            byte[] bytes = Encoding.UTF8.GetBytes(s);
            return Crc32.Compute(bytes);
        }

        // GetHash - return CRC32 from thirdparty method
        public static UInt32 GetHash(byte [] bytes)
        {
            return Crc32.Compute(bytes);
        }

        // PrintThumb - debug method to print out a thumbnail
        // as a set of RGB values
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
