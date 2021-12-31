package arenbee;

import com.google.gson.*;
import java.util.*;
import org.apache.commons.lang3.tuple.Pair;
import spark.*;
import static spark.Spark.*;

public class JServer
{

    public List<DirSearchResult> dirSearchResult = null;
    private static final HashMap<String, String> corsHeaders = new HashMap<String, String>();

    static
    {
        corsHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
        corsHeaders.put("Access-Control-Allow-Origin", "*");
        corsHeaders.put("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,");
        corsHeaders.put("Access-Control-Allow-Credentials", "true");
    }

    public final static void apply()
    {
        Filter filter = new Filter()
        {
            @Override
            public void handle(Request request, Response response) throws Exception
            {
                corsHeaders.forEach((key, value) ->
                {
                    response.header(key, value);
                });
            }
        };
        Spark.after(filter);
    }

    public ScanSet set;
    
    public JServer()
    {        

        set  = new ScanSet("C:\\TestEnvironments\\sync\\testset");
        
        port(6020);
        apply();
        
        

        try
        {

            dirSearchResult = getDirSearchResult();



        } catch (Exception e)
        {
            e.printStackTrace();
        }

        Gson gson = new GsonBuilder().create();//.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZ").create();

        // http://localhost:6020/txtsrch/i
        // http://localhost:6020/dirsrch/i
        
        System.out.println("Waiting for requests...");

        get("/status", (req, res) -> "Hello");
        get("/dirsrch/:srch", (request, response) -> 
        {
            response.type("application/json");
            String what = request.params(":srch");
            List<DirSearchResult> res = set.matchingDirs(what, 200);
            System.out.println("DSEARCH for " + what);
            return new Gson().toJson(res);
        });
        get("/txtsrch/:srch", (request, response) -> 
        {
            response.type("application/json");
            String what = request.params(":srch");
            List<Pair<String,String>> res = set.matchingFiles(what, 200);
            System.out.println("TSEARCH for " + what);
            return new Gson().toJson(res);
        });

    }

    public List<DirSearchResult> getDirSearchResult(String s)
    {
        List<DirSearchResult> res = new LinkedList<>();
        res.add(new DirSearchResult("abc"));
        res.add(new DirSearchResult("def"));
        return res;
    }
                   
    public List<DirSearchResult> getDirSearchResult()
    {
        List<DirSearchResult> res = new LinkedList<>();
        res.add(new DirSearchResult("abc"));
        res.add(new DirSearchResult("def"));
        return res;
    }

    public static void main(String[] args)
    {
        new JServer();
    }

}
