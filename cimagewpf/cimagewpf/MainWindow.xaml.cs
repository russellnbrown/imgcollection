using System;
using System.Collections.Generic;
using System.Configuration;
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

namespace cimagewpf
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

        private void Button_Click(object sender, RoutedEventArgs e)
        {

        }

        private void Init()
        {
            TabControl tc = new TabControl();
            Grid.SetRow(tc, 0);
            grid.Children.Add(tc);

            Viewer v = new Viewer();
            TabItem vt = new TabItem();
            vt.Header = "Viewer";
            vt.Content = v;

            tc.Items.Add(vt);
        }
    }
}
