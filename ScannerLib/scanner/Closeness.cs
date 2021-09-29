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

namespace Scanner
{
    public class Closeness
    {
        double close = 0;
        UInt32 ihash;
 
        public double Close { get => close; }
        public uint Ihash { get => ihash; }

        public enum Method {  Simple, Mono, Luma };
        public Closeness(Set.ImgEntry i1, byte[] i2, Method scanType = Method.Simple)
        {
 
            double td = 0.0;
            ihash = i1.crc;

            //Utils.PrintThumb("Search Img", i2);
            //Utils.PrintThumb("Candidate Img", i1.thumb);


            switch (scanType)
            {
                case Method.Simple:
                    for (int tix = 0; tix < Settings.TNMEM; tix += 3)
                    {
                        int srx = i1.thumb[tix];
                        int crx = i2[tix];
                        int sgx = i1.thumb[tix + 1];
                        int cgx = i2[tix + 1];
                        int sbx = i1.thumb[tix + 2];
                        int cbx = i2[tix + 2];

                        int tx = Math.Abs(sbx - cbx) + Math.Abs(sgx - cgx) + Math.Abs(srx - crx);
                        td += (double)tx; 
                    }
                    break;

                case Method.Mono:
                    break;

                case Method.Luma:
                    break;
 
            }

            close =  td;
            //Console.WriteLine("Closeness = {0}", close);
        }

        public Closeness(Set.ImgEntry ie, double c)
        {
            this.ihash = ie.crc;
            this.close = 0;
        }

        public override string ToString()
        {
            return String.Format("C[close={0}, ihash={1}]", Close, Ihash);
        }
    }

 
}
