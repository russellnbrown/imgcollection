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
        public const int TS = 16;
        public ImgFileInfo(Set s, FileInfo f)
        {
            string dpart = s.RelativeToTop(f.DirectoryName);
            dhash = Utils.GetHash(dpart);
            string fpart = f.Name;

            var fo = f.Open(FileMode.Open, FileAccess.Read, FileShare.Read);
            len = f.Length;
            bytes = new byte[len];
            fo.Read(bytes, 0, (int)len);
            crc = Utils.GetHash(bytes);
            tmb = null;
        }

        internal void MakeThumb()
        {
            tmb = new byte[TS * TS * 3];

            using (MemoryStream mStream = new MemoryStream(bytes))
            {
               Image i = Image.FromStream(mStream);
                Bitmap b = new Bitmap(i, new Size(TS, TS));
                i.Dispose();

                BitmapData bmpdata = b.LockBits(new Rectangle(0, 0, TS, TS), ImageLockMode.ReadOnly, PixelFormat.Format24bppRgb);
                IntPtr ptr = bmpdata.Scan0;
                Marshal.Copy(ptr, tmb, 0, TS * TS * 3);
                b.UnlockBits(bmpdata);
                b.Dispose();
            }
        }

        void IDisposable.Dispose()
        {
            
        }
    }
}
