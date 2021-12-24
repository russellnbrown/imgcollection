/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsync;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

public class Jsync 
{

    private long dcount = 0;
    private long ncreates = 0;
    private long cfiles = 0;
    private long fcount = 0;
    private long ecount = 0;
    private String topSrc = null;
    private String topDest = null;
    private boolean checkAll = false;
    private boolean testOnly = false;
        
    Path destPath(Path p)
    {
        return Paths.get(topDest + p.toString().substring(topSrc.length()));
    }
    
    long reportAt = 0;
    int ckdir=0;
    int nckdir=0;
    
    
    private void doDir(Path dir)
    {

        boolean processFiles = false;
        boolean nonexistantDir = false;
        boolean nonexistantFile = false;
        
        dcount++;
        
        Date date = new Date();
        if ( date.getTime() > reportAt )
        {
            report();
            reportAt = date.getTime() + 10000;
        }
        
        try
        {
            //NLog.i("In <<< ", dir.toAbsolutePath() );
            Path otherPath = destPath(dir);

            if(!Files.exists(otherPath)  )
            {
                NLog.d("CREATE ", otherPath);
                ncreates++;
                if ( !testOnly )
                    Files.createDirectory(otherPath);
                processFiles = true;
                nonexistantDir = true;
            }       
            else if ( checkAll == true)
            {
                processFiles = true;
                NLog.d("FORCE ", otherPath);
            }
            else
            {
                BasicFileAttributes srcd = Files.readAttributes(dir, BasicFileAttributes.class);
                BasicFileAttributes destd = Files.readAttributes(otherPath, BasicFileAttributes.class);
                if ( srcd.lastModifiedTime().toMillis() > destd.lastModifiedTime().toMillis() )
                {
                    processFiles = true;
                    NLog.d("OLDER ", otherPath);
                }
                
            }

            if ( processFiles )
                ckdir++;
            else
                nckdir++;
            
            DirectoryStream<Path> ds = Files.newDirectoryStream(dir);
            
            for(Path f : ds)
            {
                if ( Files.isDirectory(f) )
                    doDir(f);
                
                if ( !processFiles )
                    continue;
            
                if ( Files.isRegularFile(f) )
                {
                    fcount++;
 
                    Path otherFile = destPath(f);
                    if ( !nonexistantDir )// dont check existance if dir is just been created
                    {
                        if ( !Files.exists(otherFile) )
                            nonexistantFile = true;
                    }

                    if ( nonexistantDir || nonexistantFile )
                    {
                        NLog.d("COPY ", f.toAbsolutePath().toString() , " TO ", otherPath.toAbsolutePath().toString(), nonexistantDir?" (missing dir)":" (missing file)");
                        cfiles++;
                        if ( !testOnly )
                            Files.copy(f, otherFile);                
                    }
                
                }
            }
        }
        catch(Exception ex)
        {
            ecount++;
            NLog.e(ex, "Abort processing dir ", dir);
        }
    }
    
    private void report()
    {
        NLog.i("Scan: dirs=", dcount ," (",ckdir,":",nckdir,"), files=", fcount, ", errors=", ecount, ". Actions: mkdir=", ncreates, ", copied=", cfiles);
    }
    
    private Jsync(Path src, Path dest, String op) 
    {
        topSrc = src.toString();
        topDest = dest.toString();
        Date date = new Date();
        reportAt = date.getTime() + 10000;
        
        if ( op.equalsIgnoreCase("test"))
            testOnly = true;
        else if ( op.equalsIgnoreCase("force"))
            checkAll = true;
        else if ( op.equalsIgnoreCase("sync"))
            ;
        else
            NLog.f("usage: <src> <dst> <test|sync|all>");
     
        
        if(!Files.exists(src) )
            NLog.f("Source ", topSrc, " dosn't exist");
        if(!Files.exists(dest) )
            NLog.f("Dest ", topDest, " dosn't exist");
        
        NLog.i("Syncing " + src.toString() + " to " + dest.toString() + " using " + op);
        try
        {
            doDir(src);
        }
        catch(Exception e)
        {
            
        }
        
        report();
        
    }

    public static void main(String[] args) 
    {
        if ( args.length != 3 )
            NLog.f("usage: <src> <dst> <test|sync|all>");
        
        new Jsync(Paths.get(args[0]), Paths.get(args[1]), args[2]);
        
    }
}
