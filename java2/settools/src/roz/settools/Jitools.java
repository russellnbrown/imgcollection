/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roz.settools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import roz.rlib.*;
import roz.rlib.SLog.*;

public class Jitools
{

    private static String tag = "jtools";

    private final static SLog l = SLog.CreateL(tag, SLevel.Low, SLevel.Warning);

    private static void usage()
    {
        //l.severe("Usage: -s <set> | -45 <set4> <set5>  | -c <dir> <set>  |  -h <dir> <set> | -cp <dir> <set>  | -m <newset> <set1> <set2> ...");
        l.severe("Usage: -c <dir> <set> | -cp <dir> <set>  | -m <newset> <set1> <set2> ...");
        //l.severe("-hot update a set (adds any new images/folders from the hot point. no deletes)");
        l.severe("-m merge two or more sets  to new set");
        l.severe("-c create/update  gi");
        //l.severe("-h hot scan ( scan only that dir and add/update");
        l.severe("-cp create/update with perge");
        //l.severe("-sr split set to root & rel");
        //l.severe("-l list a set");
        //l.severe("-s serve a set");
        l.severe("where <root> is a common root for everything in a set, <rrel dir> is a directory, relative to root, <set> is a GetLocalPath to a set");
        System.exit(0);
    }

    public static void main(String[] args)
    {

       // if ( org.apache.commons.lang3.StringUtils.containsIgnoreCase(args[0],"abc") )
       //     System.out.print("xx");

        if (args.length < 1)
            usage();


        try
        {
            if (args[0].equals("-c")  )
            {
                if (args.length != 3)
                    usage();
                iscan js = new iscan();
                js.create4(args[1], args[2], false , true);
            }
            else if (args[0].equals("-cp")  )
            {
                if (args.length != 3)
                    usage();
                iscan js = new iscan();
                js.create4(args[1], args[2], true , false);
            }
            else if (args[0].equals("-h")  )
            {
                if (args.length != 3)
                    usage();
                iscan js = new iscan();
                js.hotscan5(args[1],args[2]);
            }
            else if (args[0].equals("-s")  )
            {
                if (args.length != 2)
                    usage();
                SSServer js = new SSServer();
                js.serve(args[1]);
            }
            //else if (args[0].equals("-45")  )
            //{
            //    if (args.length != 3)
            //        usage();
            //    iserve js = new iserve();
            //    js.convert45(args[1], args[2]);
            //}
           // else if (args[0].equals("-sr"))
           // {
           //     if (args.length != 3)
            //        usage();
            //    i_sr js = new i_sr();
            //    js.splitroot(args[1],args[2]);
            //}
            else if (args[0].equals("-t"))
            {
                itest js = new itest();
                js.test(args);
            }
            else if (args[0].equals("-m")  )
            {
                ScanSet.Merge(args);
            }

            else if (args[0].equals("-l"))
            {
                if (args.length != 2)
                    usage();
                ISet s = new ISet();
                Path p = Paths.get(args[1]);
                if (Files.exists(p))
                {
                    s.LoadFC(p,false, true);
                    s.Dump(false);
                }
                else
                    l.severe("Set " + p.toString() + " dosn't exist");
            }
            else
                usage();
        }
        catch (ImException je)
        {
            l.severe(je.toString());
            je.printStackTrace();
        } catch (Exception ex)
        {
            l.severe(ex.toString());
            ex.printStackTrace();
        }
    }

}
