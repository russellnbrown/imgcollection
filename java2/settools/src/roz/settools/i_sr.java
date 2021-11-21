/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roz.settools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import roz.rlib.*;
/*
public class i_sr
{

    private class dparts
    {
        String dh;
        String dm;
        String dp;
        public dparts(String h, String m, String p)
        {
            dh = h;
            dm = m;
            dp = p;
        }
    }
    private final static SLog l = SLog.GetL();

    List<dparts> dirs = new LinkedList<dparts>();

    public void splitroot(String iset, String sset) throws IOException
    {
        String ip = iset + "/directory2.txt";
        String sp = sset + "/directory3.txt";
        File indirs = new File(ip);
        File outdirs = new File(sp);
        File outp = new File(sset);

        if ( !indirs.exists() )
            l.fatal("Input dosnt exist");

        if ( !outp.exists() )
            outp.mkdirs();

        BufferedReader inf = Files.newBufferedReader(indirs.toPath());
        BufferedWriter outf = Files.newBufferedWriter(outdirs.toPath());

        Path commonRoot=null;
        String line=null;

        while((line = inf.readLine())!=null)
        {
            String parts[] = line.split(",");

            String path = Utils.StandardizePath(parts[2]);

            Path rd = Paths.get(path).getRoot();
            if ( commonRoot == null )
            {
                commonRoot = rd.getRoot();
                INode.SetRoot(commonRoot.toString());
            }
            else
            {
                if (!commonRoot.equals(rd))
                {
                    System.out.printf("Mismatch roots %s %s %n", commonRoot, rd);
                    exit(0);
                }
            }

            path = INode.relativeToRoot(path);
            dparts dp = new dparts(parts[0], parts[1], path);

            dirs.add(dp);

        }

        System.out.printf("Root %s%n", INode.GetRoot());

        outf.write(commonRoot.toString());
        outf.newLine();

        for(dparts dp:dirs)
        {
            System.out.printf("Rel %s  Full %s%n", dp.dp, INode.fullPath(dp.dp.toString()));
            outf.write(String.format("%s,%s,%s%n", dp.dh, dp.dm, dp.dp));
        }

        inf.close();
        outf.close();

        String ipf = iset + "/files2.txt";
        String spf = sset + "/files3.txt";
        Files.copy(Paths.get(ipf), Paths.get(spf));
        ipf = iset + "/uniq2.bin";
        spf = sset + "/uniq3.bin";
        Files.copy(Paths.get(ipf), Paths.get(spf));


    }



}
*/