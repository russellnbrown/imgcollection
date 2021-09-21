using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;
using static Scanner.Builder;

namespace Scanner
{
    public partial class InitialSelection : Form
    {
        private Builder set = null;

        public InitialSelection()
        {
            l.To("is.log");
            InitializeComponent();

            if (Properties.Settings.Default.SetLocation != null)
                setLocation.Text = Properties.Settings.Default.SetLocation;
            if (Properties.Settings.Default.RelativeTo != null)
                relativeTo.Text = Properties.Settings.Default.RelativeTo;
            if (Properties.Settings.Default.ScanThis != null)
                scanThis.Text = Properties.Settings.Default.ScanThis;

            CheckReadyForCreate();


            monitorTimer.Interval = 100;
            monitorTimer.Tick += new EventHandler(OnMonitorTimer);
            monitorTimer.Start();

        }

  
        private void OnCreateNew(object sender, EventArgs e)
        {
            set = new Builder();
            set.CreateNew(relativeTo.Text, setLocation.Text);
            UpdateStates();
            CheckForScanThis();
        }

        private void UpdateStates()
        {
            if (set == null)
            {
                scannerStatus.Text = "-";
                directoryCountTB.Text = "-";
                fileCountTB.Text = "-";
                imageCountTB.Text = "-";
                dupDirTB.Text = "-";
                dupFilesTB.Text = "-";
                return;
            }

            if ( !set.isBusy && startBtn.Enabled == false )
            {
                CheckForStart();
            }
            scannerStatus.Text = set.status;
            directoryCountTB.Text = set.DirectoriesProcessed.ToString();
            fileCountTB.Text = set.FilesProcessed.ToString();
            imageCountTB.Text = set.ImagesProcessed.ToString();
            dupDirTB.Text = set.DirectoriesIgnored.ToString();
            dupFilesTB.Text = set.FilesIgnored.ToString();
            curDirTB.Text = set.CurrentDir;
            curFileTB.Text = set.CurrentFile;
        }

        private void CheckReadyForCreate()
        {
            if (relativeTo.Text.Length > 0 && setLocation.Text.Length > 0)
                createBtn.Enabled = true;
            else
                createBtn.Enabled = false;
        }

        private void CheckForStart()
        {
            if (scanThis.Text.Length > 0)
                startBtn.Enabled = true;
            else
                startBtn.Enabled = false;
        }

        private void OnSetSetLocation(object sender, EventArgs e)
        {
            FolderBrowserDialog fb = new FolderBrowserDialog();

            if (fb.ShowDialog() == DialogResult.OK)
            {
                string setPath = fb.SelectedPath;
                if (File.Exists(setPath + "/dirs.txt"))
                {
                    MessageBox.Show("Set is not empty");
                    return;
                }
                setLocation.Text = setPath;
                CheckReadyForCreate();
                UpdateStates();
            }
        }

        private void OnSetRelativeTo(object sender, EventArgs e)
        {
            FolderBrowserDialog fb = new FolderBrowserDialog();

            if (fb.ShowDialog() == DialogResult.OK)
            {
                relativeTo.Text = fb.SelectedPath;
                CheckReadyForCreate();
            }
        }

        private void OnSelectExisting(object sender, EventArgs e)
        {
            FolderBrowserDialog fb = new FolderBrowserDialog();

            if (fb.ShowDialog() == DialogResult.OK)
            {
                if (!File.Exists(fb.SelectedPath + "/dirs.txt"))
                {
                    MessageBox.Show("Empty or not a set");
                    return;
                }
                string existing = fb.SelectedPath;
                MessageBox.Show("Open " + existing);
                set = new Builder();
                set.OpenExisting(fb.SelectedPath);
                CheckForScanThis();
            }

        }

        private void CheckForScanThis()
        {
            if ( set != null && !set.error )
            {
                scanThis.Enabled = true;
                scanThisBtn.Enabled = true;
                createBtn.Enabled = false;
                openExistingBtn.Enabled = false;
                openExisting.Enabled = false;

                CheckForStart();

            }
        }

        private void OnScanThis(object sender, EventArgs e)
        {
            FolderBrowserDialog fb = new FolderBrowserDialog();

            if (fb.ShowDialog() == DialogResult.OK)
            {
                scanThis.Text = fb.SelectedPath;
            }

            CheckForStart();
        }

        public void Start()
        {
            new Thread(() =>
            {
                Thread.CurrentThread.IsBackground = true;
                Run();
            }).Start();
        }

        private void Run()
        {
            set.StartScan(scanThis.Text);
        }

        private void OnMonitorTimer(object sender, EventArgs args)
        {
            UpdateStates();
        }

        private void OnStart(object sender, EventArgs e)
        {
            if ( !scanThis.Text.StartsWith(relativeTo.Text) )
            {
                MessageBox.Show("Scan this not below relative location");
                return;
            }
            startBtn.Enabled = false;

            Properties.Settings.Default.SetLocation = setLocation.Text;
            Properties.Settings.Default.RelativeTo = relativeTo.Text;
            Properties.Settings.Default.ScanThis = scanThis.Text;
            Properties.Settings.Default.Save();

            Start();


        }
    }
}
