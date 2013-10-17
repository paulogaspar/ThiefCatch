package pt.ua.ThiefCatch.Synchronism;


import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Paulo Gaspar
 */
public class Semaphore
{
    private int resourceSharingMax = -1;
    private int resourceThreadsNumber = 0;


    public Semaphore(int state)
    {
        resourceThreadsNumber = state;
    }

    public synchronized void aquire()
    {
        while(resourceThreadsNumber == 0)
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(Semaphore.class.getName()).log(Level.SEVERE, null, ex);
            }

        resourceThreadsNumber--;
    }

    public synchronized void release()
    {
        resourceThreadsNumber++;
        notify();
    }

}
