using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml;

namespace pplot
{
    class Airport
    {
        public Airport(string path)
        {
            XmlDocument xmlDoc = new XmlDocument();
            xmlDoc.Load(path);
            XmlNode titleNode = xmlDoc.SelectSingleNode("//airport/runway");
            if (titleNode != null)
                Console.WriteLine(titleNode.InnerText);
        }
    }
}
