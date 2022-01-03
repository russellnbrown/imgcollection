package arenbee;

import arenbee.api.*;
import arenbee.other.*;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import org.apache.commons.lang3.tuple.Pair;
import spark.*;
import static spark.Spark.*;

public class JServer
{

   
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
    
    public JServer(Path p)
    {        

        set  = new ScanSet(p.toString());
        
        port(6020);
        apply();
        
        

        Gson gson = new GsonBuilder().create();//.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZ").create();

        // http://localhost:6020/txtsrch/i
        // http://localhost:6020/dirsrch/i
        
        System.out.println("Waiting for requests...");

        get("/status", (req, res) -> "Hello");
        
        get("/dirsrch/:srch", (request, response) -> 
        {
            response.type("application/json");
            String what = request.params(":srch");
            arenbee.api.GenericSearchResult res = set.matchingDirs(what, 200);
            System.out.println("DSEARCH for " + what + " returning " + res.items.size() + " items");
            return gson.toJson(res);
        });
        
        get("/txtsrch/:srch", (request, response) -> 
        {
            response.type("application/json");
            String what = request.params(":srch");
            arenbee.api.GenericSearchResult res = set.matchingFiles(what, 200);
            System.out.println("TSEARCH for " + what + " returning " + res.items.size() + " items");
            return gson.toJson(res);
        });
        
        get("/imgsrch/:srch", (request, response) -> 
        {
            response.type("application/json");
            String jwhat = request.params(":srch");
            String what = Helpers.hexToStr(jwhat);
            
            arenbee.api.GenericSearchResult res = set.matchingImages(what, 200);
            System.out.println("ISEARCH for " + what);
            return gson.toJson(res);
        });
        
        get("/test/:srch", (request, response) -> 
        {
            response.type("application/json");
            String what = request.params(":srch");
            
            arenbee.api.GenericSearchResult res = new arenbee.api.GenericSearchResult();
            res.items = new ArrayList<>();
            res.message = "Worked";
            res.items.add(new arenbee.api.GenericSearchResultItem("abc"));
            res.items.add(new arenbee.api.GenericSearchResultItem("def"));
            System.out.println("TSEARCH for " + what);
            return gson.toJson(res);
        });

    }



    public static void main(String[] args)
    {
        if ( args.length != 1 )            
        {
            System.out.println("No set specified ");
            return;
        }
        Path p = Paths.get(args[0]);

        
        if ( !Files.exists(p) )
        {
            System.out.println("Set specified dosn't exist");
            return;
        }
        
        new JServer(p);
    }

}
