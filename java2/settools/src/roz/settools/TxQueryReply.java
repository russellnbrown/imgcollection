package roz.settools;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.LinkedList;


public class TxQueryReply extends TxObject implements java.io.Serializable
{

    public Integer queryState=-1;
    public Integer filesFound=0;
    public String message="";
    public LinkedList<String> files = null;
    public LinkedList<Long> crcs = null;
    public LinkedList<int[]> thumbs = null;

    public TxQueryReply(int queryState, int filesFound, String txt, LinkedList<String> files)
    {
        super();
        this.queryState = queryState;
        this.filesFound = filesFound;
        this.files  = files;
        message = txt;
    }


    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "TxQueryReply{" +
                "queryState=" + queryState +
                ", filesFound=" + filesFound +
                ", message='" + message + "\n");
        if ( files != null )
        {
            sb.append("Files:-\n");
            for (String s : files)
            {
                sb.append('\t' + s + '\n');
            }
        }
        if ( crcs != null )
        {
            sb.append("Images:-\n");
            for (Long s : crcs)
            {
                sb.append('\t' + s.toString() + '\n');
            }
        }
        sb.append('}');
        return sb.toString();
    }
}




