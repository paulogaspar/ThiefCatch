
package pt.ua.ThiefCatch;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class responsable for the thief threads.
 * When started, this thread walks to a destiny bank to rob it.
 * When it arrives the bank, sounds the alarm, warning the bank about an assault.
 * After waiting a random time in the bank, tries to get back to his hideout.
 * If he gets to the hideout without getting caught by the police, the thread finishes there,
 * making a successfull assault.
 *
 * @author Paulo Gaspar
 *
 */
public class Burglar extends Entity implements Runnable
{
    /** Time the burglar takes to walk from one city square into another. */
    public static final int delayTime = 40;
    private Position startPosition;
    private Position bankPosition;
    private int bankID; /* Victim bank ID. */
    private State currentState;
    private boolean gotArested, escaped;

    /* Movement class. */
    private Movement movement;

   /* Burglar possible states. */
    private enum State { goingToBank, onBank, Escaping };

    /**
     * Constructor for the burglar.
     *
     * @param iPlan Reference to the CityPlan.
     * @param initialPosition Position where this thief starts its journey to the bank.
     * @param myID ID of this burglar.
     * @param iBankID ID of the bank he is going to assault.
     */
    public Burglar(CityPlan iPlan, Position initialPosition, int myID, int iBankID)
    {
        assert iPlan != null;
        assert (initialPosition != null) && (iPlan.validPosition(initialPosition));
        assert (myID >= 0);
        assert (iBankID >= 0);
        assert (iPlan.getEntity(iBankID) instanceof Bank);

        currentPosition = new Position(initialPosition);
        bankPosition = iPlan.getEntityPositionByID(iBankID);
        startPosition = new Position(initialPosition);
        ID = myID;
        plan = iPlan;
        bankID = iBankID;
        gotArested = false;
        escaped = false;

        /* Thief starts by going to the bank. */
        currentState = State.goingToBank;

        /* Instantiate a new movement class. */
        movement = new Movement(plan, CityPlan.mapEntities.BURGLAR, ID);
    }

    @Override
    public void run()
    {
        Random rand = new Random();

        /* State machine. */
        while(true)
        {
            if (gotArested)
            {
                ((Bank)plan.getEntity(bankID)).assaultTerminated();
                ((HideOut)plan.getEntityList(CityPlan.mapEntities.HIDEOUT).firstElement()).gotArrested(bankID, ID);
                movement.eraseMeFromBoard(currentPosition);
                return;
            }

            switch (currentState)
            {
                /* When thief is going to the bank. */
                case goingToBank:
                    movement.setTravelTime(200);
                    movement.setState(0);
                    movement.makeWalkToPlace(currentPosition, bankPosition, "normalWalk");
                    if (currentPosition.equals(bankPosition)) currentState = State.onBank;
                    break;
                    
                /* When thief is inside the bank. */
                case onBank:
                    ((Bank)plan.getEntity(bankID)).thisIsAnAssault(this);
                    delay(2000 + rand.nextInt(1000));
                    currentState = State.Escaping;
                    break;

                /* When thief is going back to his hideout. */
                case Escaping:
                    movement.setTravelTime(120);
                    movement.setState(0);
                    movement.makeWalkToPlace(currentPosition, startPosition, "normalWalk");
                    if (currentPosition.equals(startPosition))
                    {
                        synchronized (this) { if (gotArested) continue; escaped = true; }
                        ((Bank)plan.getEntity(bankID)).assaultTerminated();
                        ((HideOut)plan.getEntityList(CityPlan.mapEntities.HIDEOUT).firstElement()).terminatedRobbery(bankID, ID);
                        movement.eraseMeFromBoard(currentPosition);
                        return;
                    }
                    break;
            }
        }
    }

    private void delay(int mili)
    {
        assert mili >= 0;

        try {
            Thread.sleep(mili);
        } catch (InterruptedException ex) {
            Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sets this burglar as arrested by the police.
     * Happens after robbing a bank, when the police cars chasing him get too close.
     */
    public synchronized boolean youAreUnderArest()
    {
        assert gotArested != true;

        if (escaped) return false;
        
        gotArested = true;
        return true;
    }

    /**
     * Returns the ID of the bank this burglar is assaulting.
     * @return the ID of the bank this burglar is assaulting.
     */
    public int getBankID() 
    {
        assert bankID >= 0;
        
        return bankID;
    }

}
