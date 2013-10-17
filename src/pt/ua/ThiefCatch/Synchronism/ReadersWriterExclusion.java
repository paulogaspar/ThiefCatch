package pt.ua.ThiefCatch.Synchronism;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Paulo
 */
public class ReadersWriterExclusion
{
    private int nReadersWaiting, nReadersActive, nWritersWaiting;
    private boolean writerActive;

    public ReadersWriterExclusion()
    {
        writerActive = false;
        nReadersWaiting = nReadersActive = nWritersWaiting = 0;
    }

    public synchronized void lockReader()
    {
        nReadersWaiting++;
        while (writerActive || (nWritersWaiting > 0))
            try {
            wait();
        } catch (InterruptedException ex) {
            System.exit(4);
        }
        nReadersWaiting--;

        nReadersActive++;
    }

    public synchronized void unlockReader()
    {
        assert lockIsMine();
        
        nReadersActive--;
        if(nReadersActive == 0)
            notifyAll();
    }

    public synchronized void lockWriter()
    {
        nWritersWaiting++;

        while((nReadersActive > 0) || (writerActive))
            try {
            wait();
        } catch (InterruptedException ex) {
            System.exit(4);
        }
        nWritersWaiting--;

        writerActive = true;
    }
    
    public synchronized void unlockWriter()
    {
        assert lockIsMine();

        writerActive = false;
        notifyAll();
    }

    private boolean lockIsMine()
    {
        return true;
    }

}
