using NLog;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace WpfCoreTester
{
    /// <summary>
    /// Interaction logic for DirectoryTreeUC.xaml
    /// </summary>
    public partial class DirectoryTreeUC : UserControl
    {
        Logger log = LogManager.GetCurrentClassLogger();
        public DirectoryTreeUC()
        {
            InitializeComponent();
            LoadDirectories();
        }

        public void LoadDirectories()
        {
            var drives = DriveInfo.GetDrives();
            foreach (var drive in drives)
            {
                this.treeView.Items.Add(this.GetItem(drive));
                log.Info("Load top level drive " + drive.Name);
            }
        }

        private TreeViewItem GetItem(DriveInfo drive)
        {
            var item = new TreeViewItem
            {
                Header = drive.Name,
                DataContext = drive,
                Tag = drive
            };
            this.AddDummy(item);
            item.Expanded += new RoutedEventHandler(item_Expanded);
            return item;
        }

        private TreeViewItem GetItem(DirectoryInfo directory)
        {
            var item = new TreeViewItem
            {
                Header = directory.Name,
                DataContext = directory,
                Tag = directory
            };
            this.AddDummy(item);
            item.Expanded += new RoutedEventHandler(item_Expanded);
            item.Selected += Item_Selected;// new RoutedEventHandler(Item_Selected);
            return item;
        }

        private void Item_Selected(object sender, RoutedEventArgs e)
        {
            var item = (TreeViewItem)sender;
            this.ExploreFiles2(item);
        }

        private void AddDummy(TreeViewItem item)
        {
            item.Items.Add(new DummyTreeViewItem());
        }

        private bool HasDummy(TreeViewItem item)
        {
            return item.HasItems && (item.Items.OfType<TreeViewItem>().ToList().FindAll(tvi => tvi is DummyTreeViewItem).Count > 0);
        }

        private void RemoveDummy(TreeViewItem item)
        {
            var dummies = item.Items.OfType<TreeViewItem>().ToList().FindAll(tvi => tvi is DummyTreeViewItem);
            foreach (var dummy in dummies)
            {
                item.Items.Remove(dummy);
            }
        }

        private TreeViewItem GetItem(FileInfo file)
        {
            var item = new TreeViewItem
            {
                Header = file.Name,
                DataContext = file,
                Tag = file
            };
            return item;
        }

        private void ExploreDirectories(TreeViewItem item)
        {
            try
            {
                var directoryInfo = (DirectoryInfo)null;
                if (item.Tag is DriveInfo)
                {
                    directoryInfo = ((DriveInfo)item.Tag).RootDirectory;
                }
                else if (item.Tag is DirectoryInfo)
                {
                    directoryInfo = (DirectoryInfo)item.Tag;
                }
                else if (item.Tag is FileInfo)
                {
                    directoryInfo = ((FileInfo)item.Tag).Directory;
                }
                if (object.ReferenceEquals(directoryInfo, null)) return;
                foreach (var directory in directoryInfo.GetDirectories())
                {
                    var isHidden = (directory.Attributes & FileAttributes.Hidden) == FileAttributes.Hidden;
                    var isSystem = (directory.Attributes & FileAttributes.System) == FileAttributes.System;
                    if (!isHidden && !isSystem)
                    {
                        item.Items.Add(this.GetItem(directory));
                    }
                }
            }
            catch
            {

            }
        }

        private void ExploreFiles(TreeViewItem item)
        {
            var directoryInfo = (DirectoryInfo)null;
            if (item.Tag is DriveInfo)
            {
                directoryInfo = ((DriveInfo)item.Tag).RootDirectory;
            }
            else if (item.Tag is DirectoryInfo)
            {
                directoryInfo = (DirectoryInfo)item.Tag;
            }
            else if (item.Tag is FileInfo)
            {
                directoryInfo = ((FileInfo)item.Tag).Directory;
            }
            if (object.ReferenceEquals(directoryInfo, null)) return;
            foreach (var file in directoryInfo.GetFiles())
            {
                var isHidden = (file.Attributes & FileAttributes.Hidden) == FileAttributes.Hidden;
                var isSystem = (file.Attributes & FileAttributes.System) == FileAttributes.System;
                if (!isHidden && !isSystem)
                {
                    item.Items.Add(this.GetItem(file));
                    log.Info("File is " + file.Name);
                }
            }
        }

        private void ExploreFiles2(TreeViewItem item)
        {
            var directoryInfo = (DirectoryInfo)null;
            if (item.Tag is DriveInfo)
            {
                directoryInfo = ((DriveInfo)item.Tag).RootDirectory;
            }
            else if (item.Tag is DirectoryInfo)
            {
                directoryInfo = (DirectoryInfo)item.Tag;
            }
            else if (item.Tag is FileInfo)
            {
                directoryInfo = ((FileInfo)item.Tag).Directory;
            }
            if (object.ReferenceEquals(directoryInfo, null)) return;
            log.Info("DTC Selected folder " + directoryInfo.FullName);
            FileViewUC.Get.BeginUpdate();
            foreach (var file in directoryInfo.GetFiles())
            {
                var isHidden = (file.Attributes & FileAttributes.Hidden) == FileAttributes.Hidden;
                var isSystem = (file.Attributes & FileAttributes.System) == FileAttributes.System;
                var isImage = Helpers.isImage(file);
                if (!isHidden && !isSystem && isImage)
                {
                    log.Info("DTC Add" + file.Name);
                    FileViewUC.Get.AddFile(file);
                }
                else
                    log.Info("DTC Ignore" + file.Name);
            }
            FileViewUC.Get.EndUpdate();
        }


        void item_Expanded(object sender, RoutedEventArgs e)
        {
            var item = (TreeViewItem)sender;
            if (this.HasDummy(item))
            {
                this.Cursor = Cursors.Wait;
                this.RemoveDummy(item);
                this.ExploreDirectories(item);
               // this.ExploreFiles(item);
                this.Cursor = Cursors.Arrow;
            }
        }


    }


    public class DummyTreeViewItem : TreeViewItem
    {
        public DummyTreeViewItem()
            : base()
        {
            base.Header = "Dummy";
            base.Tag = "Dummy";
        }
    }
}
