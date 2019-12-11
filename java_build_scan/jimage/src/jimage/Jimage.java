/*
 * Copyright (C) 2018 russell brown
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package jimage;

import arenbee.jimageutils.DBConnectionInfo;
import arenbee.jutils.Timer;
import arenbee.jimageutils.ImgCollectionBuilder;
import arenbee.jimageutils.ImgCollectionFileItem;
import arenbee.jimageutils.SearchResult;
import arenbee.jimageutils.ImgCollection;
import arenbee.jutils.Logger;
import arenbee.jutils.Logger.Level;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Jimage
{
    
    private String message;

   public void setMessage(String message){
      this.message  = message;
   }
   public void getMessage(){
      System.out.println("Your Message : " + message);
   }
    
    public static void main(String[] args)
    {
        boolean useThreads = true;
        DBConnectionInfo dbinfo = null;
        dbinfo = new DBConnectionInfo();
        dbinfo.host = "localhost";
        dbinfo.db = "imgcollection";
        dbinfo.port = 1234;
        dbinfo.pwd = "imgdb";
        dbinfo.user = "imgdb";
   
         Logger.Create("JImage", Level.Debug, Level.Info);
        
        
        // -c : Create the image collection
        if (args[0].startsWith("-c") && args.length == 3)
        {
            if ( args[0].equals("-cn") )
                useThreads = false;
            // We need to create a builder to create our collection
            ImgCollectionBuilder icbuilder = new ImgCollectionBuilder(useThreads, dbinfo);
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
            ImgCollection st = new ImgCollection(useThreads, null);
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

    private static void usage()
    {
        Logger.Raw("Usage: -c <set> <dir> | -s <set> <file>");
        Logger.Raw("Where");
        Logger.Raw("-c <set> <dir> - Creates a dataset in <set> from the files under directory <dir>");
        Logger.Raw("-s <set> <file> - Scans the dataset in <set> looking for images similar to <file>");
        System.exit(0);
    }
}
