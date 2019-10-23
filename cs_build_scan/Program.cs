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
            // start logging
            l.To("cs_build_scan.log");

            // check args to see what to do
            if (args.Length == 3 && args[0].Substring(0, 2) == "-c")
                new Builder(args[1], args[2], args[0]=="-cn" ? false : true);
            else if (args.Length == 3 && args[0] == "-s")
                new Search(args[1], args[2], args[0] == "-cn" ? false : true);
            else
                usage();

            l.Close();
        }
    }
}
