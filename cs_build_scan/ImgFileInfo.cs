using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Runtime.InteropServices;

namespace cs_build_scan
{
    public class ImgFileInfo : IDisposable
    {
        public string name;
        public UInt32 crc;
        public byte[] bytes;
        public byte[] tmb;
        public UInt32 dhash;
        public Int64 len;
        public ImgFileInfo(Set s, FileInfo f)
        {
            if ( s != null )
            {
                string dpart = s.RelativeToTop(f.DirectoryName);
                dhash = Utils.GetHash(dpart);
            }
            else
                dhash = 0;
            name = f.Name;
            var fo = f.Open(FileMode.Open, FileAccess.Read, FileShare.Read);
            len = f.Length;
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
               Image i = Image.FromStream(mStream);
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

        void IDisposable.Dispose()
        {
            
        }
    }
}
