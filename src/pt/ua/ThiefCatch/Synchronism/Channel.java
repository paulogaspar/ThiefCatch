package pt.ua.ThiefCatch.Synchronism;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Paulo
 */
public class Channel
{

    protected BinarySemaphore putPermit;
    protected BinarySemaphore takePermit;
    protected BinarySemaphore taken;
    protected Object item = null;

    public Channel()
    {
        putPermit = new BinarySemaphore(1);
        takePermit = new BinarySemaphore(0);
        taken = new BinarySemaphore(0);
    }

    public void put(Object obj)
    {
        putPermit.aquire();
        synchronized (this) { item = obj; }
        takePermit.release();
        // wait for take
        taken.aquire();
    }

    public Object take()
    {
        takePermit.aquire();
        Object result;
        synchronized (this)
        {
            result = item;
            item = null;
        }
        putPermit.release();
        // notify put
        taken.release();
        return result;
    }
}
