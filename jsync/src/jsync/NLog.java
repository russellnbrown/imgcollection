/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jsync;

public class NLog {

    private static final String LOG_FORMAT = "%1$s\n%2$s";
    public enum Log { DEBUG, INFO, WARN, ERROR, FATAL };

    public static void d(String tag, Object... args) {
       // log(Log.DEBUG, null, tag, args);
    }

    public static void i(String tag, Object... args) {
        log(Log.INFO, null, tag, args);
    }

    public static void w(String tag, Object... args) {
        log(Log.WARN, null, tag, args);
    }
    public static void f(String tag, Object... args) {
        log(Log.FATAL, null, tag, args);
        System.exit(-1);
    }

    public static void e(Throwable ex) {
        log(Log.ERROR, ex, null);
    }

    public static void e(String tag, Object... args) {
        log(Log.ERROR, null, tag, args);
    }

    public static void e(Throwable ex, String tag, Object... args) {
        log(Log.ERROR, ex, tag, args);
    }

    private static void log(Log priority, Throwable ex, String tag, Object... args) 
    {
        String log = "";
        if (ex == null) {
            if (args != null && args.length > 0) {
                for (Object obj : args) {
                    log += String.valueOf(obj);
                }
            }
        } else {
            String logMessage = ex.getMessage();
            
            String logBody = ex.getStackTrace().toString();
            log = String.format(LOG_FORMAT, logMessage, logBody);
        }
        System.out.println(priority.toString() + " : " + tag + " " + log);
    }
}
