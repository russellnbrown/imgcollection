using System;
using System.Collections.Generic;
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
    /// Interaction logic for SearchTab.xaml
    /// </summary>
    public partial class SearchTab : UserControl
    {
        public SearchTab()
        {
            InitializeComponent();

            relativeTo.Text = "/media/veracrypt1";
            scanSet.Text = "/media/veracrypt1/stuff/scans/scan1";
            scanLocation.Text = "/media/veracrypt1/stuff/scansTest";

            //new Search(args[1], args[2], args[0] == "-cn" ? false : true);
        }

        private void scanBtn_Click(object sender, RoutedEventArgs e)
        {
         //   new Builder(relativeTo.Text,scanSet.Text, scanLocation.Text);
        }

        private void appendBtn_Click(object sender, RoutedEventArgs e)
        {
           // new Builder(null, args[1], args[2]);
        }
    }
}
