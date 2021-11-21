package arenbee.jfxview;




import  java.util.prefs.*;

/**
 * Created by russellb on 10/08/2016.
 */
public class ViewSettings
{
    static Preferences prefs =  Preferences.userRoot().node("roz.jfxview");
    static void init()
    {
        top = prefs.get("top", "C:/");
        searchset = prefs.get("searchset", "");
        dirsort = prefs.get("dirsort", "Name");
        System.out.println("XX ConfigLoad load=" + dirsort);

    }
    static void save()
    {
        try
        {
            prefs.put("top", top);
            prefs.put("searchset", searchset);
            prefs.put("dirsort", dirsort );
            prefs.flush();
        }
        catch (BackingStoreException e)
        {
            e.printStackTrace();
        }
    }
    static public ScanSet.SSScanType scanType = ScanSet.SSScanType.Luma;
    final static public int tndispsize = 100;
    final static public int tpad = 2;
    static public String dirsort;
    static public String top;
    static public String searchset;


}
