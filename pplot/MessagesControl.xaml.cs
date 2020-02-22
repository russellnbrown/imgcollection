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
    /// Interaction logic for MessagesControl.xaml
    /// </summary>
    public partial class MessagesControl : UserControl
    {
        public MessagesControl()
        {
            InitializeComponent();
            msgList.Background = new SolidColorBrush(Colors.AntiqueWhite);
            System.Windows.Threading.DispatcherTimer dispatcherTimer = new System.Windows.Threading.DispatcherTimer();
            dispatcherTimer.Tick += DispatcherTimer_Tick;
            dispatcherTimer.Interval = new TimeSpan(0, 0, 0, 0, 250);
            dispatcherTimer.Start();
        }

        private void DispatcherTimer_Tick(object sender, EventArgs e)
        {
            int mc = ApWin.Get().GetMessageCount();
            while(msgList.Items.Count < mc)
            {
                ApWin.Message m = ApWin.Get().GetMessage(msgList.Items.Count);
                ListViewItem lvi = new ListViewItem();
                lvi.Content = m.msg;
                lvi.Foreground = new SolidColorBrush(m.colour);
                lvi.Background = new SolidColorBrush(Colors.OldLace);
                if (m.alert)
                {
                    lvi.FontWeight = FontWeights.Bold;
                }

                msgList.Items.Insert(0, lvi);
            }
        }
    }
}
