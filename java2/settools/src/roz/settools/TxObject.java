package roz.settools;

import java.io.Serializable;

// test test

public class TxObject
{
    private static transient Integer seqCnt=0;

    public Integer seqId;

    public TxObject()
    {
        seqId = seqCnt++;
    }



}




