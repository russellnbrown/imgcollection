using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace csview
{
    public partial class CSViewForm : Form
    {
        private  ImageConverter ic = new ImageConverter();
        private string icPath;

        public CSViewForm()
        {
            InitializeComponent();
        }

        private void cslist_SelectedIndexChanged(object sender, EventArgs e)
        {
  

        }

        private void CSViewForm_Load(object sender, EventArgs e)
        {
            Size bsize = new Size(16, 16);

            cslist.LargeImageList = imglist;

            ListViewItem lvi = new ListViewItem();
            lvi.ImageIndex = 0;

            cslist.View = View.LargeIcon;
  

            if (Environment.GetCommandLineArgs().Length != 2 )
            {
                MessageBox.Show("Specify path to img collection folder when starting");
                return;
            }
            string fileName = Environment.GetCommandLineArgs()[1];
            if (!Directory.Exists(fileName))
            {
                MessageBox.Show(fileName + " dosn't exist");
                return;
            }
            fileName += "/images.bin";
            using (BinaryReader reader = new BinaryReader(File.Open(fileName, FileMode.Open)))
            {
                while(reader.BaseStream.Position != reader.BaseStream.Length)
                {
                    Int64 crc = reader.ReadInt64();
                    byte []ba = reader.ReadBytes(3 * 16 * 16);
                    Bitmap bimg = new Bitmap(16, 16, PixelFormat.Format24bppRgb);
                    Rectangle r = new Rectangle(0, 0, 16, 16);
                    BitmapData bmpData = bimg.LockBits(r, ImageLockMode.WriteOnly, bimg.PixelFormat);
                    int pad = bmpData.Stride - 3 * 16;
                    unsafe
                    {
                        byte* ptr = (byte*)bmpData.Scan0;
                        int wp = 0;
                        int bx = 0;
                        for(int y=0; y<16; y++)
                        {
                            for (int x = 0; x < 16; x++)
                            {
                                ptr[2] = ba[wp++];
                                ptr[1] = ba[wp++];
                                ptr[0] = ba[wp++];
                                ptr += 3;
                                bx++;
                            }
                            ptr += pad;
                        }
                        bimg.UnlockBits(bmpData);
                    }
  
                    imglist.Images.Add((Image)bimg);
                    lvi = new ListViewItem();
                    lvi.ImageIndex = imglist.Images.Count-1;
                    cslist.Items.Add(lvi);
                    if (imglist.Images.Count > 1500)
                        break;

                }
            }
        }

        public  Bitmap GetImageFromByteArray(byte[] byteArray)
        {
            Bitmap bm = (Bitmap)ic.ConvertFrom(byteArray);

            if (bm != null && (bm.HorizontalResolution != (int)bm.HorizontalResolution ||
                               bm.VerticalResolution != (int)bm.VerticalResolution))
            {
                // Correct a strange glitch that has been observed in the test program when converting 
                //  from a PNG file image created by CopyImageToByteArray() - the dpi value "drifts" 
                //  slightly away from the nominal integer value
                bm.SetResolution((int)(bm.HorizontalResolution + 0.5f),
                                 (int)(bm.VerticalResolution + 0.5f));
            }

            return bm;
        }

    }
}
