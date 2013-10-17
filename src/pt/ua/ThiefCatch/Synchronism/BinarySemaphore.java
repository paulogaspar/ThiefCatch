package pt.ua.ThiefCatch.Synchronism;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Paulo
 */
public class BinarySemaphore extends Semaphore
{
    BinarySemaphore()
    {
        super(1);
    }

    BinarySemaphore(int state)
    {
        super(state);
    }
}
