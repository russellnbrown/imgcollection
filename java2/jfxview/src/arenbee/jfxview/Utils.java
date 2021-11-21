/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arenbee.jfxview;

import com.sun.glass.ui.GlassRobot;
import javafx.scene.control.Alert;
import javafx.stage.Screen;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;



public class Utils {
    private static String OS = System.getProperty("os.name").toLowerCase();
    public static DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
    public static DateFormat dfs = new SimpleDateFormat("HH:mm:ss");

    public static FileTime ftime(long l) {
        return FileTime.from(l, TimeUnit.SECONDS);
    }

    public static long utime(FileTime t) {
        return t.to(TimeUnit.SECONDS);
    }

    public static void Fatal(String s) {
        JOptionPane.showMessageDialog(null, s);
        System.exit(-1);
    }

    public static String dateStr(Date d) {
        return df.format(d);
    }

    public static String shortDateStr(Date d) {
        return dfs.format(d);
    }

    public static void Sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {

        }
    }

    public static void whoops(String m0, String m1, String m2)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(m0);
        alert.setHeaderText(m1);
        alert.setContentText(m2);
        alert.show();
    }

    public static int GetScreenUnderCursor()
    {
        return 0;
        /*
        GlassRobot robot = com.sun.glass.ui.Application.GetApplication().createRobot();
        double x = robot.getMouseX();
        double y = robot.getMouseY();
        int scr = 0;
        int bscr = -1;
        for (Screen s : Screen.getScreens())
        {
            System.out.printf("Screen %d is %s%n", scr, s.getBounds());
            if (s.getBounds().contains(x, y))
                bscr=scr;
            scr++;
        }
        System.out.printf("Screen under cursor is %d%n", bscr);
        return bscr;*/
    }

    public static String PathToUri(File fi) {
        Path f = fi.toPath();
        try {
            return f.toUri().toURL().toString();
        } catch (MalformedURLException e) {
            return "bad GetLocalPath in PathToUri:" + fi.getPath();
        }
    }

    public static String PathToUri(String s) {
        Path f = Paths.get(s);
        try {
            return f.toUri().toURL().toString();
        } catch (MalformedURLException e) {
            return "bad GetLocalPath in PathToUri:" + s;
        }
    }

    public static boolean atWork() {
        try {
            String host = InetAddress.getLocalHost().getHostName();
            if (host.toLowerCase().contains("ausyd"))
                return true;
        } catch (UnknownHostException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }

    public static boolean isImageFile(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1).toLowerCase();
            if (extension.equals("jpg"))
                return true;
            if (extension.equals("png"))
                return true;
            if (extension.equals("bmp"))
                return true;
            if (extension.equals("jpeg"))
                return true;
            if (extension.equals("gif"))
                return true;
        }

        return false;
    }

    public static long SHash(String str) {
        long hash = 5381;

        int l = str.length();
        for (int cx = 0; cx < l; cx++) {
            hash = ((hash << 5) + hash) + str.charAt(cx); /* hash * 33 + c */
        }

        return hash;
    }

    public static long PHash(Path dir) {
        try {
            return Utils.SHash(dir.toRealPath().toString());
        } catch (IOException ex) {
            return Utils.SHash(dir.toAbsolutePath().toString());
        }
    }

    public static long BHash(byte[] bytes) {
        long hash = 5381;

        int l = bytes.length;
        for (int cx = 0; cx < l; cx++) {
            hash = ((hash << 5) + hash) + bytes[cx]; /* hash * 33 + c */
        }

        return hash;
    }

    public static void OpenInExplorer(String fPath) {
        try {
            Path p = Paths.get(fPath);

            if ( !isWindows() )
                p = p.getParent();

            Path np = p.normalize();
            String ps = np.toAbsolutePath().toString();
            if ( isWindows() )
                Runtime.getRuntime().exec("explorer.exe /select," + ps);
            else
                JOptionPane.showMessageDialog(null, "Not yet in Linux");
            //Desktop.getDesktop().open(new File(ps));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String StandardizePath(String p)
    {
        //String GetLocalPath = p.replaceAll("\\\\","/"); // a\\c -> a/c
        String path = p.replace("\\","/"); // a\c -> a/c
        path = path.replace("//","/");// a\c -> a/c
        if ( path.length() > 2 && path.charAt(1) == ':' )
            path = path.substring(0,1).toUpperCase() + path.substring(1);
        return path;
    }

    public static boolean IsRootPath(String p)
    {
        return p.charAt(1) == ':' || p.startsWith("/");
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String TrimEnds(String p, boolean head, boolean tail)
    {
        if ( head && p.startsWith("/") )
            p = p.substring(1);
        if ( tail && p.endsWith("/") )
            p = p.substring(0,p.length()-1);
        return p;
    }

    // dops - directory related stuff

    //public static String dopsCommonRoot = "";

    public static String StandardizePath(Path p)
    {
        return StandardizePath(p.toString());
    }

  //  public static String dopsFullPath(String p)
   // {
  //      return dopsCommonRoot + p;
  //  }

    //public static String dopsFullPath(Path p)
   // {
   //     return dopsCommonRoot + StandardizePath(p);
   // }

  //  public static void dopsSetRoot(Path p)
   // {
    //    dopsCommonRoot = StandardizePath(p);
    //}



}


