/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roz.settools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import roz.rlib.*;

public class itest
{
    private final static SLog l = SLog.GetL();



    public itest()
    {

    }

    public void test(String args[])
    {



            while(true)
            {
                File f = new File("C:/TestEnvironments/ww/ww/DSCN0977.JPG");
                if ( f.exists() )
                {

                    TxQuery obj = new TxQuery(f.getPath(), 1);
                    TxQueryReply reply = obj.waitreply();

                    if ( reply != null )
                    {
                        System.out.printf("Got reply:" + reply);
                        break;
                    }

                    Utils.Sleep(2000);
                }
            }


    }


    public void test_(String args[]) throws ImException
    {
        Path oldset = Paths.get(args[1]);
        Path newset = Paths.get(args[2]);
        if ( !Files.exists(newset) )
        {
            try
            {
                Files.createDirectory(newset);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        ISet totest = new ISet();
        totest.Load(oldset,false);
        totest.SaveFC(newset);

    }


}
