/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsync;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


/**
 *
 * @author russell.brown
 */
 
public class Jsync {




 
public class Jsync implements FileVisitor<Path> {

    
    private long dcount = 0;
    private long fcount = 0;
    private long ecount = 0;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        if ( args.length != 3 )
            NLog.f("usage: <src> <dst> <test|sync>");
        
        new Jsync(Paths.get(args[0]), Paths.get(args[1]), args[2]);
        
    }


    
    private Jsync(Path src, Path dest, String op) 
    {
        NLog.i("Syncing " + src.toString() + " to " + dest.toString() + " using " + op);
        try
        {
            Files.walkFileTree(src, this);
        }
        catch(Exception e)
        {
            
        }
        
        NLog.i("Visited dirs=", dcount , ", files=", fcount, ", errors=", ecount);
        
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException 
    {
        //NLog.i("Pre ", dir.toAbsolutePath());
        dcount++;
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        //NLog.i("At ", file.toAbsolutePath());
        fcount++;
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        NLog.i("Err ", file.toAbsolutePath());
        ecount++;
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        //NLog.i("Post ", dir.toAbsolutePath());
        return FileVisitResult.CONTINUE;
    }
    
}
