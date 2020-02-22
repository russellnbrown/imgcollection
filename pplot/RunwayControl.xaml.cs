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

namespace pplot
{
    /// <summary>
    /// Interaction logic for RunwayControl.xaml
    /// </summary>
    public partial class RunwayControl : UserControl
    {
        private Airport.RunwayConfiguration rw;

        public RunwayControl(Airport.RunwayConfiguration _rw)
        {
            rw = _rw;
            InitializeComponent();
            header.Text = rw.Name;
        }

 
    }
}
