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
    /// Interaction logic for ExplorerTab.xaml
    /// </summary>
    public partial class ExplorerTab : UserControl
    {
   
        public ExplorerTab()
        {
            InitializeComponent();
            browserDP.Children.Add(new DirectoryTreeUC());
        }
    }
}
