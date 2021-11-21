package arenbee.jfxview;



import java.io.File;
import java.nio.file.*;

public class INode
{
    private String pathPart;
    private String filePart;
    private static String rootPart;
    private boolean isADir = false;
    private long dhash=0;
    private final static SLog l = SLog.GetL();

    @Override
    public String toString()
    {
        return "INode{" + getFullPath() + '\'' +
                "rootPart='" + rootPart + '\'' +
                "pathPart='" + pathPart + '\'' +
                ", filePart='" + filePart + '\'' +
                ", isADir=" + isADir +
                ", GetHash=" + dhash +
                '}';
    }

    public static void SetRoot(String root)
    {
        rootPart = Utils.StandardizePath(root);
        if ( rootPart.endsWith("/"))
            rootPart = rootPart.substring(0,rootPart.length()-1);
    }

    public static String GetRoot()
    {
        return rootPart;
    }

    public INode(String path, boolean isADir) throws ImException
    {
        init(path, isADir);
    }

    public String getPathPart() { return pathPart; }

    public INode(INode other)
    {
        pathPart = other.pathPart;
        filePart = other.filePart;
        dhash = other.dhash;
        isADir = other.isADir;
    }


    public static String AbsolutePath(String s)
    {
        if ( s.startsWith("/") || ( s.length()>2 && s.charAt(1) == ':') )
            return s;
        return Utils.StandardizePath(System.getProperty("user.dir")) + "/" + s;
    }

    private void init(String path, boolean isADir) throws ImException
    {
        try
        {
            this.isADir = isADir;
            pathPart = Utils.StandardizePath(path);
            pathPart = INode.AbsolutePath(pathPart);

            if (pathPart.startsWith(rootPart))
                pathPart = pathPart.substring(rootPart.length());
            else
                throw new ImException("roots dosnt match");
            if (pathPart.startsWith("/"))
                pathPart = pathPart.substring(1);
            if (isADir)
            {
                if (pathPart.endsWith("/"))
                    pathPart = pathPart.substring(0, pathPart.length() - 1);
            }
            else
            {
                int end = pathPart.lastIndexOf("/");
                filePart = pathPart.substring(end + 1);
                if ( end > -1 )
                    pathPart = pathPart.substring(0, end);
            }
            dhash = ISet.DHash(pathPart);
        }
        catch(Exception e)
        {
            l.warn("INode Create for %s - ex: %s" , (path==null ?"null":path), e.getMessage() );
            e.printStackTrace();

        }

    }

    public enum CreatePoint { Absolute, RelativeToRoot, RelativeToCurrent };

    public static INode Create(String path, boolean isDir, CreatePoint cp) throws ImException
    {
        path = Utils.StandardizePath(path);
        switch(cp)
        {
            case Absolute:
                break;
            case RelativeToCurrent:
                if ( !path.startsWith(rootPart) )
                    path = Utils.StandardizePath(System.getProperty("user.dir")) + "/" + path;
                break;
            case RelativeToRoot:
                path = rootPart + "/" + path;
                break;
        }
        INode n = new INode(path, isDir);
        return n;
    }

    public INode(File fpath, boolean isADir) throws ImException
    {
        init(fpath.getPath(), isADir);
    }

    public INode(Path ppath, boolean isADir) throws ImException
    {
        init(ppath.toString(), isADir);
    }

    public String getFS()
    {
        throw new RuntimeException();
    }

    public String getDir()
    {
        return pathPart;
    }

    public String getFileName()
    {
        return filePart;
    }

    public long getDirHash()
    {
        return dhash;
    }

    public Path getPath()
    {
        return Paths.get(getFullPath());
    }

    public File getFile()
    {
        return new File(getFullPath());
    }

    public String getRoot()
    {
        return rootPart;
    }

    public String getRelPath()
    {
        return pathPart + "/" + filePart;
    }

    public String getFullPath()
    {
        if ( filePart != null )
            return rootPart + "/" + pathPart + "/" + filePart;
        else
            return rootPart + "/" + pathPart;
    }

    public static String relativeToRoot(String path)
    {
          if ( path.startsWith(rootPart) )
              return path.substring(rootPart.length()+1);
          return path;
    }

    public static String fullPath(Path p)
    {
         return fullPath(p.toString());
    }

    public static String fullPath(String p)
    {
        return rootPart + Utils.StandardizePath(p);
    }

    public INode GetParent()
    {
        INode n = new INode(this);
        int pos = pathPart.lastIndexOf("/");
        String p = "";
        if ( pos > -1 )
        {
            n.filePart = null;
            n.pathPart = pathPart.substring(0,pos-1);
            n.dhash = Utils.SHash(n.pathPart);
            return n;
        }
        return null;
    }

}
