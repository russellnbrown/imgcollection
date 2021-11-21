
package arenbee.jfxview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Settings
{
    private static Properties s;
    
    public static void Open(String fn) throws FileNotFoundException, IOException
    {
        s = new Properties();
        File configFile = new File(fn);
        InputStream inputStream = new FileInputStream(configFile);
        s.load(inputStream);
        inputStream.close();
            
    }

    public static String GetStr(String name, String def)
    {
        String v = s.getProperty(name, def);
        return v;
    }

    public static String GetStr(String name) throws Exception
    {
        String v = s.getProperty(name,"???");
        if ( v.equals("???") )
            throw new Exception("Can't read value " + name + " in properties");
        return v;
    }

    public static int GetInt(String wweight, Integer i) {
        String v = s.getProperty(wweight, i.toString() );
        return Integer.parseInt(v);
    }

}
