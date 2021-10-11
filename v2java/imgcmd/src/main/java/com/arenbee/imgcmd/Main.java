/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arenbee.imgcmd;
import com.arenbee.imglib.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


/**
 *
 * @author User
 */
public class Main {

    private static boolean useThreads = true;

    private static void usage()
    {
        Logger.Raw("Usage: -c <set> <dir> | -s <set> <file>");
        Logger.Raw("Where");
        Logger.Raw("-c <set> <dir> - Creates a dataset in <set> from the files under directory <dir>");
        Logger.Raw("-s <set> <file> - Scans the dataset in <set> looking for images similar to <file>");
        System.exit(0);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Logger.Create("JImage", Logger.Level.Debug, Logger.Level.Info);


        // -c : Create the image collection
        if (args[0].startsWith("-c") && args.length == 3)
        {
            if ( args[0].equals("-cn") )
                useThreads = false;
            // We need to create a builder to create our collection
            ImgCollectionBuilder icbuilder = new ImgCollectionBuilder(useThreads);
            Path imageSetPath = Paths.get(args[1]);
            Path directoriesPath = Paths.get(args[2]);
            // create the collection. This will also save to disk
            icbuilder.Create(imageSetPath.toAbsolutePath(), directoriesPath.toAbsolutePath());
            Logger.Raw(Timer.stagereport("Create Timings"));

        } // -s : search for an image in tge collection
        else if (args[0].startsWith("-s") && args.length == 3)
        {
            if ( args[0].equals("-sn") )
                useThreads = false;

            Path imageSetPath = Paths.get(args[1]);
            Path fileToSearch = Paths.get(args[2]);

            // Create the collection & load from disk
            ImgCollection st = new ImgCollection(useThreads);
            st.Load(imageSetPath);

            // Get list of images similar to the search image
            List<SearchResult> results = st.Find(fileToSearch, 10);

            if (results != null)
            {
                Logger.Info("Results:");
                results.forEach(item ->
                {
                    Logger.Raw("  Img:" + item.getImage().getIHash() + ", Closeness: " + item.getCloseness() + ", Files:-" );
                    List<ImgCollectionFileItem> match = st.filesMatchingImage(item.getImage());
                    for ( ImgCollectionFileItem file : match )
                    {
                        Path p = st.getFilePath(file);
                        Logger.Raw("    File: " +  ( p != null ? p.toString() : "bad path" )  );
                    }
                    // getLogger.Info(st.);
                });
            }
            Logger.Raw(Timer.stagereport("Search Timings"));

        } else
        {
            usage();
        }
    }
    
}
