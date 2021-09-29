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
    /// Interaction logic for FileViewUC.xaml
    /// </summary>
    public partial class FileViewUC : UserControl
    {
        private static FileViewUC instance=null;
        public static FileViewUC Get { get => instance; }
        Logger log = LogManager.GetCurrentClassLogger();

        public FileViewUC()
        {
            InitializeComponent();
            instance = this;
        }

        public void BeginUpdate()
        {
            log.Info("FVUC BeginUpdate");
            wp.Children.Clear();
        }

        public void EndUpdate()
        {
            log.Info("FVUC EndUpdate");
        }

        public void AddFile(FileInfo  f)
        {
            if (!Helpers.isImage(f))
                return;


            Image imgTemp = new Image();


            BitmapImage myBitmapImage = new BitmapImage();
            {
                myBitmapImage.BeginInit();
                myBitmapImage.UriSource = new Uri(f.FullName);
                myBitmapImage.DecodePixelWidth = 64;
                myBitmapImage.DecodePixelHeight = 64;
                myBitmapImage.EndInit();


                imgTemp.Source = myBitmapImage;
            }
//            imgTemp.Height = imgTemp.Width = 100;
            imgTemp.MouseLeftButtonDown += imgTemp_MouseLeftButtonDown;
            //Button b = new Button();
            
            wp.Children.Add(imgTemp);

         //   wp.Children.Add(new Button() { Content = f.Name });
            log.Info("FVUC Add " + f.FullName);
        }

        private void imgTemp_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            Console.WriteLine("Selected " + ((Image)sender).Source);
        }
    }
}
