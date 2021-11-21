package arenbee.jfxview;

/**
 * Created by russ on 19/08/2016.
 */
public class RTimer {
    private long startAt = 0;
    private long endAt = 0;

    public RTimer()
    {
        reset();
    }

    public void reset()
    {
        startAt = System.currentTimeMillis();
        endAt=startAt;
    }

    public void stop()
    {
        endAt = System.currentTimeMillis();
    }

    public long elapsed()
    {
        return System.currentTimeMillis()-startAt;
    }


    @Override
    public String toString()
    {
        return String.format("%dms", endAt - startAt);
    }
}
