/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arenbee.imgcmd;
import com.arenbee.imglib.*;

import java.io.IOException;
import java.nio.file.Files;
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
        Logger.Raw("-c <top> <set> <dir> - Creates a dataset in <set> from the files under directory <dir> relative to top of <top>");
        Logger.Raw("-s <set> <file> - Scans the dataset in <set> looking for images similar to <file>");
        System.exit(0);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Logger.Create("JImage", Logger.Level.Debug, Logger.Level.Info);

        if ( args.length < 1)
            usage();

        // -c : Create the image collection
        if (args[0].startsWith("-c") && args.length == 4)
        {
            if ( args[0].equals("-cn") )
                useThreads = false;
            // We need to create a builder to create our collection
            ImgCollectionBuilder icbuilder = new ImgCollectionBuilder(useThreads);
            Path topPath = Paths.get(args[1]);
            Path imageSetPath = Paths.get(args[2]);
            Path directoriesPath = Paths.get(args[3]);
            // create the collection. This will also save to disk
            icbuilder.Create(topPath.toAbsolutePath(), imageSetPath.toAbsolutePath(), directoriesPath.toAbsolutePath());
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

        } else if (args[0].startsWith("-m") && args.length > 3)
        {

            Path saveSetPath = Paths.get(args[1]);
            if (!Files.exists(saveSetPath))
            {
                try
                {
                    Files.createDirectories(saveSetPath);
                } catch (IOException ex)
                {
                    Logger.Fatal("Set path " + saveSetPath + " dosn't exist and I couldn't create it.");
                }
            }

            Path mainImageSetPath = Paths.get(args[2]);

            // Create the collection & load from disk
            ImgCollection st = new ImgCollection(useThreads);
            st.Load(mainImageSetPath);

            for ( int ms = 3; ms < args.length; ms++ )
            {
                Path nextSetPath = Paths.get(args[ms]);
                Logger.Info("Merging " + nextSetPath);
                ImgCollection ns = new ImgCollection(useThreads);
                ns.Load(nextSetPath);
                st.Merge(ns);
            }

            st.Save(saveSetPath);

        } else
        {
            usage();
        }
    }
    
}
