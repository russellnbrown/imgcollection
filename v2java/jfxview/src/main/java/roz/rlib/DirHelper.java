
package roz.rlib;

// test one
// test reply


public class DirHelper
{
    public static String GetRoot() { return root;  }
    public static void SetRoot(String path)
    {
        if (path.length() >= 2 && path.charAt(1) == ':')
            path = path.substring(0, 2);
        DirHelper.root = path;//StandardizePath(path);
    }


    private static String root = "";
    private String path="";
    private String origpath="";
    private long hash=0;

    public static String StandardizePath(String p)
    {

        String path = p.replace("\\","/"); // a\c -> a/c
        path = path.replace("//","/");// a\c -> a/c
        if ( path.length() > 2 && path.charAt(1) == ':' )
            path = path.substring(2);

        if ( path.endsWith("/") )
            path = path.substring(0,path.length()-1);

       // if (Utils.isWindows() )
            return path;
       // else
       //     return path.substring(root.length());


    }

    public String RemoveRoot(String p)
    {
            if ( p.startsWith(root) )
                return p.substring(root.length());
            return p;
    }

    public DirHelper(String d)
    {
        origpath = d;
        path = StandardizePath(d);
        path = RemoveRoot(path);
        hash = Utils.SHash(path);
    }

    public DirHelper(String d, boolean file)
    {
        origpath = d;
        path = StandardizePath(d);
        path = RemoveRoot(path);
        int pos = path.lastIndexOf('/');
        path = path.substring(0,pos);
        hash = Utils.SHash(path);
    }

    public long GetHash()
    {
        return hash;
    }
    public String GetLocalPath()
    {
        return path;
    }
    public String GetOriginalPath() { return origpath; }
    public String GetDiskPath()
    {
       // if ( root.startsWith("/") )
       //     return path;
       // else
            return root + path;
    }


    public void SetHash(Long d) {
        hash=d;
    }
}