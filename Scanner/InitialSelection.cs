using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Scanner
{
    public partial class InitialSelection : Form
    {
        private Builder set = null;

        public InitialSelection()
        {
            InitializeComponent();
            CheckReadyForCreate();
        }

  
        private void OnCreateNew(object sender, EventArgs e)
        {
            MessageBox.Show("Create New");
        }

        private void CheckReadyForCreate()
        {
            if (relativeTo.Text.Length > 0 && setLocation.Text.Length > 0)
                createBtn.Enabled = true;
            else
                createBtn.Enabled = false;
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

            }

        }
    }
}
