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
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();

            Init();

        }

        private void Init()
        {
            TabControl theMainTabControl = new TabControl();
            
            Grid.SetRow(theMainTabControl, 0);
            mainGrid.Children.Add(theMainTabControl);

            TabItem explorerTabItem = new TabItem();
            explorerTabItem.Header = "Explorer";
            ExplorerTab explorerTab = new ExplorerTab();
            explorerTabItem.Content = explorerTab;
            theMainTabControl.Items.Add(explorerTabItem);
        }
    }

}

