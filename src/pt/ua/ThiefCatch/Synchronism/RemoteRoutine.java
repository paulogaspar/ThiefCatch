package pt.ua.ThiefCatch.Synchronism;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Paulo
 */
public abstract class RemoteRoutine
{

    public RemoteRoutine() {
        id = getGlobalID();
    }

    public long id() {
        return id;
    }

    public abstract void run();

    public synchronized void setArgs(Object... args) {
        this.args = args;
    }

    public synchronized Object result() {
        return this.result;
    }

    protected synchronized static long getGlobalID() {
        return ++globalID;
    }
    
    protected Object[] args = null;
    protected Object result = null;
    protected long id = 0;
    protected static long globalID = 0;
}
