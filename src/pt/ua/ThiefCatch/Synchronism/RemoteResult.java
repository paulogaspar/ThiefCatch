package pt.ua.ThiefCatch.Synchronism;


import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Paulo
 */

public class RemoteResult
{
    private Object result;
    private boolean terminatedMethod;

    public RemoteResult()
    {
        result = null;
        terminatedMethod = false;
    }

    public synchronized void await()
    {
        while ((result == null) && (!terminatedMethod))
        {
            try { wait(); }
            catch (InterruptedException ex) {  Logger.getLogger(RemoteResult.class.getName()).log(Level.SEVERE, null, ex);  }
        }
    }

    public Object get()
    {
        if (result == null) await();
        return result;
    }

    public synchronized void set(Object result)
    {
        this.result = result;
        this.terminatedMethod = true;
        notify();
    }
}
