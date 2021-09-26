using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace WpfCoreTester
{
    public class Helpers
    {
        public static List<string>  imageExtensions = new List<string> { ".jpg", ".gif", ".jfif", ".png" };
        public static bool isImage(FileInfo f)
        {
            string ext = f.Extension.ToLower();
            return imageExtensions.Contains(ext);
        }
    }
}
