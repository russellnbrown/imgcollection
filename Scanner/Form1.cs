using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Scanner
{
    public partial class Form1 : Form
    {
        Builder b = new Builder();

        public Form1()
        {
            InitializeComponent();

            l.To("cs_build_scan.log");

#if DEBUG
           // relativeTo.Text = "C:/TestEnvironments";
          //  setLocation.Text = "C:/TestEnvironments/sets2/set1.db";
         //   scanLocation.Text = "C:/TestEnvironments/img2";
#else
             relativeTo.Text = "/media/veracrypt1";
             setLocation.Text = "/media/veracrypt1/stuff/scans/scan1";
             scanLocation.Text = "/media/veracrypt1/stuff/scansTest";
#endif


        }

        private void OnScan(object sender, EventArgs e)
        {
            b.StartScan(scanLocation.Text);
        }

        private void openExisting_Click(object sender, EventArgs e)
        {
            FolderBrowserDialog fb = new FolderBrowserDialog();
            
            if ( fb.ShowDialog()  == DialogResult.OK )
            {
                setLocation.Text = fb.SelectedPath;
                b = new Builder();
                b.OpenExisting(setLocation.Text);
                relativeTo.Text = b.relativeTo;
                statusText.Text = b.GetCurrnetState();
                relativeTo.Enabled = false;
            }
        }

        private void OnChooseRelativeTo(object sender, EventArgs e)
        {
            FolderBrowserDialog fb = new FolderBrowserDialog();

            if (fb.ShowDialog() == DialogResult.OK)
            {
                relativeTo.Text = fb.SelectedPath;
            }
        }

        private void OnCreateNewScan(object sender, EventArgs e)
        {
            if ( relativeTo.Text == "")
            {
                MessageBox.Show("Choose relative location first");
                return;
            }
            FolderBrowserDialog fb = new FolderBrowserDialog();
            fb.ShowNewFolderButton = true;
            if (fb.ShowDialog() == DialogResult.OK)
            {
                setLocation.Text = fb.SelectedPath;
            }
            b.CreateNew(relativeTo.Text,  setLocation.Text);
            statusText.Text = b.GetCurrnetState();
        }

        private void OnChooseScanLocation(object sender, EventArgs e)
        {
            FolderBrowserDialog fb = new FolderBrowserDialog();

            if (fb.ShowDialog() == DialogResult.OK)
            {
                scanLocation.Text = fb.SelectedPath;
            }

        }
    }
}
