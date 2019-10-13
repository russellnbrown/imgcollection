using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace cs_build_scan
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

            Console.WriteLine(String.Format("I1: {0:X} {1:X} {2:X} I2: {3:X}  {4:X} {5:X} \n", 
                i1.thumb[0] & 0xFF, i1.thumb[1] & 0xFF, i1.thumb[2] & 0xFF, i2[0] & 0xFF, i2[1] & 0xFF, i2[2] & 0xFF));

            for (int tix = 0; tix < Settings.TNMEM; tix += 3)
            {
                double srx = i1.thumb[tix];
                double crx = i2[tix];
                double sgx = i1.thumb[tix + 1];
                double cgx = i2[tix + 1];
                double sbx = i1.thumb[tix + 2];
                double cbx = i2[tix + 2];

                switch (scanType)
                {
                    case Method.Mono:
                        {
                            double lums = ((double)srx * 0.21) + ((double)sgx * 0.72) + ((double)sbx * 0.07);
                            double lumc = ((double)crx * 0.21) + ((double)cgx * 0.72) + ((double)cbx * 0.07);
                            td += Math.Abs(lums - lumc) / 255.0;
                        }
                        break;
                    case Method.Simple:
                        {
                            double tx = Math.Abs(sbx - cbx) + Math.Abs(sgx - cgx) + Math.Abs(srx - crx);
                            td += (double)tx;
                        }
                        break;
                    case Method.Luma:
                        {
                            double dxr = Math.Abs(srx - crx) * 0.21;
                            double dxg = Math.Abs(sgx - cgx) * 0.72;
                            double dxb = Math.Abs(sbx - cbx) * 0.07;
                            double tx = (dxr + dxg + dxb);
                            td += tx;
                        }
                        break;
                }
            }

            close =  td;
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
