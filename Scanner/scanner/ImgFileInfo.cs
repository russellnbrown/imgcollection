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
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Runtime.InteropServices;

namespace Scanner
{
    // ImgFileInfo 
    // Gets relevant information for an image file. directory & image hash, file anme, thumbnail (optional ) 
    public class ImgFileInfo : IDisposable
    {
        public string name;
        public string path;
        public UInt32 crc;
        public byte[] bytes;
        public byte[] tmb;
        public UInt32 dhash;
        public UInt32 fhash;
        public Int64 len;

        public ImgFileInfo(Set s, FileInfo f)
        {
            // If set is specified, we will calculate the directory key for the set
            // otherwise just ignore it
            if ( s != null )
            {
                string dpart = s.RelativeToTop(f.DirectoryName);
                dhash = Utils.GetHash(dpart);
                fhash = Utils.GetHash(f.Name);
            }
            else
                dhash = 0;

            path = f.FullName;
            name = f.Name;
            len = f.Length;



        }

        internal void MakeHashes()
        {
            FileInfo f = new FileInfo(path);
            var fo = f.Open(FileMode.Open, FileAccess.Read, FileShare.Read);
            
            bytes = new byte[len];
            fo.Read(bytes, 0, (int)len);
            crc = Utils.GetHash(bytes);
            tmb = null;
        }

        internal void MakeThumb()
        {
            tmb = new byte[Settings.TNMEM];
            //Byte []ttmb = new byte[Settings.TNMEM];

            using (MemoryStream mStream = new MemoryStream(bytes))
            {
               System.Drawing.Image i = Image.FromStream(mStream);
                Bitmap b = new Bitmap(i, new Size(Settings.TS, Settings.TS));
                i.Dispose();

                BitmapData bmpdata = b.LockBits(new Rectangle(0, 0, Settings.TS, Settings.TS), ImageLockMode.ReadOnly, PixelFormat.Format24bppRgb);
                IntPtr ptr = bmpdata.Scan0;
                Marshal.Copy(ptr, tmb, 0, Settings.TNMEM);
                b.UnlockBits(bmpdata);
                b.Dispose();
            }
            // Image gives us BGR, we need to swap B&G to give RGB
            for(int px=0; px<Settings.TNMEM; px+=3)
            {
                byte t = tmb[px+0];
                tmb[px+0] = tmb[px+2];
                tmb[px+2] = t;
            }
        }

        public override string ToString()
        {
            return String.Format("IMI[nam:{0} dhash{1} fhash{2} crc{3}", name, dhash, fhash, crc);
        }

        void IDisposable.Dispose()
        {
            
        }
    }
}
