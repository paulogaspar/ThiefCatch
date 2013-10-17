/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.ua.ThiefCatch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ua.ThiefCatch.Synchronism.Actor;
import pt.ua.ThiefCatch.Synchronism.RemoteRoutine;

/**
 * Class responsable for the police station thread.
 * This class waits until there are robberies to deal with.
 * When so, dispatches available police car to make the persuit
 * of a burglar.
 *
 * @author Paulo Gaspar
 *
 */
public class PoliceStation extends Entity implements Runnable
{
    //private CityPlan plan;
    //private Position currentPosition;

    private Queue<Integer> banksBeingRobbed; /* Keeps record of new robberies yet to be dealt with. */
    private Queue<Integer> banksStillOnRobbery; /* Keeps record of robberies that couldn't be dealt with yet (unavailable police cars). */
    private Map<Integer, Vector<Patrol>> assaultsRecord; /* For each bank, record the patrols on persuit of it's thief. */
    private Vector<Integer> arrestedThieves;

    private boolean allPatrolsOnPersuit; /* Boolean indicator of all patrols busy (on robberies). */
    private int numberOfPatrols; /* Number of police cars. */
    private int numberOfOnGoingRobberies; /* Number of robberies currently in action. */

    /** Thread to deal with RPCs. */
    public static final Actor StationOfficer = new Actor();

    public PoliceStation(CityPlan iPlan, Position policeStationPosition, Integer iID)
    {
        assert iPlan != null;
        assert policeStationPosition != null;
        assert iPlan.validPosition(policeStationPosition);
        assert iID >= 0;

        ID = iID;
        plan = iPlan;
        currentPosition = policeStationPosition;
        
        banksBeingRobbed = new LinkedList<Integer>(); /* New robberies. */
        banksStillOnRobbery = new LinkedList<Integer>(); /* Robberies that couln't be dealt. */
        assaultsRecord = new HashMap<Integer, Vector<Patrol>>(); /* List of ongoing persuits. */
        arrestedThieves  = new Vector<Integer>(); /* List of arrested thieves. */

        allPatrolsOnPersuit = true;
        numberOfPatrols = plan.getNumberOfEntities(CityPlan.mapEntities.PATROL);
        numberOfOnGoingRobberies = 0;

        /* Create an empty list for each bank, for further keeping record
           of patrols on persuit of that bank's burglar. */
        for (Entity b : plan.getEntityList(CityPlan.mapEntities.BANK))
            assaultsRecord.put(b.getID(), new Vector<Patrol>());

        /* Create remote procedure calls, and launch actor. */
        addRemoteRoutinesToActor();
        StationOfficer.start();
    }

    @Override
    public void run()
    {
        while(true)
        {
            /* Check if there are new roberies. 
               If so, send available patrols.
               If not, wait for a robbery. */
            if (banksBeingRobbed.isEmpty())
                synchronized(this)
                {
                    try {  wait();  }
                    catch (InterruptedException ex)
                    {   Logger.getLogger(PoliceStation.class.getName()).log(Level.SEVERE, null, ex);   }
                }
            else
                dispatchPatrolsToRobbery();
        }
    }

    /**
     * Warn the policestation that a bank is being robbed.
     * @param ID ID of the bank being robbed.
     */
    public void IamBeingRobbed(int ID)
    {
        assert ID >= 0;
        assert plan.getEntity(ID) != null;
        assert plan.getEntity(ID) instanceof Bank;
        assert numberOfOnGoingRobberies >= 0;
        assert !((Bank)plan.getEntity(ID)).isTheAssaultOver();
        assert banksBeingRobbed != null;
        assert plan.getEntityList(CityPlan.mapEntities.BURGLAR).size() > 0;

        numberOfOnGoingRobberies++;

        int thiefID = ((Bank)plan.getEntity(ID)).whoIsTheBurglar();
        ThiefCatch.policeStationMessageBoard.addMessage("*** Alarm triggered at bank " + ID + " by burglar "+thiefID+" ***");
        banksBeingRobbed.add(ID);
        synchronized (this) { notify(); }
    }

    /**
     * Inform police station of a patrol that successfully delivered a caught burglar to the police station.
     */
    public void thiefWasDeliveredToPoliceStation()
    {
        assert numberOfOnGoingRobberies > 0;

        /* One robbery less. */
        if (numberOfOnGoingRobberies>0) numberOfOnGoingRobberies--;
        
        /* After delivering thief to jail, check for on going undealt robberies. */
        checkForUndealtRobberies();
    }

    /**
     * Inform police station that a thief was arrested by a patrol.
     * @param thiefID ID of the arrested thief
     * @param patrolID ID of the patrol that made the arrest.
     */
    public void arrestedThief(Burglar thief, int patrolID)
    {
        assert thief != null;
        assert patrolID >= 0;
        assert plan.getEntity(patrolID) != null;
        assert plan.getEntity(patrolID) instanceof Patrol;
        assert !((Patrol)plan.getEntity(patrolID)).isFree();
        assert arrestedThieves != null;

        /* If already arrested, means another patrol caught him. */
        if (arrestedThieves.contains(thief.getID()))
            return;

        /* Add thief to jail list. */
        arrestedThieves.add(thief.getID());

        ThiefCatch.policeStationMessageBoard.addMessage("Thief " + thief.getID() + " was caught by police car "+patrolID);

        warnPatrolsOfEndedRobbery(thief.getID(), patrolID, thief.getBankID());
    }

    /**
     * Inform police station thar a burglar escaped (got to the hideout without being arrested).
     * @param thiefID ID of escaped thief.
     */
    public synchronized void burglarEscaped(int thiefID, int bankID)
    {
        assert thiefID >= 0;
        assert bankID >= 0;
        assert plan.getEntity(thiefID) == null;
        assert plan.getEntity(bankID) != null;
        assert plan.getEntity(bankID) instanceof Bank;
        assert numberOfOnGoingRobberies > 0;
        assert !arrestedThieves.contains(thiefID);

        /* Warn all patrols in persuit of that burglar. */
        warnPatrolsOfEndedRobbery(thiefID, -1, bankID);
        
        /* One robbery less. */
        if (numberOfOnGoingRobberies>0) numberOfOnGoingRobberies--;
        //System.out.println("Burglar escaped  " + numberOfOnGoingRobberies);
    }

    private synchronized void warnPatrolsOfEndedRobbery(int thiefID, int warningPatrolID, int bankID)
    {
        assert thiefID >= 0;
        assert bankID >= 0;
        assert plan.getEntity(bankID) != null;
        assert plan.getEntity(bankID) instanceof Bank;
        assert assaultsRecord != null;
        assert assaultsRecord.get(bankID) != null;
        assert numberOfOnGoingRobberies > 0;

        /* Warn patrols to stop chasing this burglar. */
        for (Patrol patrol: assaultsRecord.get(bankID))
            /* If it's the patrol that made the warning call,
               ignore it (he already knows the robbery is over). */
            if (patrol.getID() != warningPatrolID) patrol.persuitFinished();

        /* Remove all patrols on this robbery from list of ongoing persuits. 
         Decrease number of ongoing roberries. */
        assaultsRecord.get(bankID).clear();

        checkForUndealtRobberies();
    }

    /* Check for robberies that weren't dealt with yet. */
    private synchronized void checkForUndealtRobberies()
    {
        assert banksStillOnRobbery != null;
        assert banksBeingRobbed != null;

        if (!banksStillOnRobbery.isEmpty())
        {
             /* Add old roberies to list of new roberies. Wakeup police station to warn patrols.*/
             for(int i=0; i<banksStillOnRobbery.size(); i++) { banksBeingRobbed.add(banksStillOnRobbery.poll());  }
             notify();
        }
    }

    private synchronized void dispatchPatrolsToRobbery()
    {
        assert banksBeingRobbed != null;
        assert banksStillOnRobbery != null;
        assert assaultsRecord != null;
        assert assaultsRecord.size() > 0;
        assert banksBeingRobbed.size() > 0;
        assert numberOfOnGoingRobberies > 0;
        assert numberOfPatrols >= 0;

        /* Check if the assault to be dealt with has already ended. */
        int tmpBankID = banksBeingRobbed.peek();
        if (((Bank)plan.getEntity(tmpBankID)).isTheAssaultOver())
        {
            banksBeingRobbed.poll();
            return;
        }

        String dispachedPatrols = new String();

        /* Number of patrols designated for each assault. */
        int patrolsPerAssault = numberOfPatrols/numberOfOnGoingRobberies;

        /* If there are no available patrols, move all assaults to the list of banks still beeing assalted. */
        if (patrolsPerAssault <= 0)
        {
            for(int i=0; i<banksBeingRobbed.size(); i++) { banksStillOnRobbery.add(banksBeingRobbed.poll());  }
            ThiefCatch.policeStationMessageBoard.addMessage("No patrols available to deal robbery on bank " + tmpBankID + ".");
            return; /* Too many assaults for patrol cars. */
        }

        Vector<Entity> patrols = plan.getEntityList(CityPlan.mapEntities.PATROL);

        /* Find free (available to start a persuit) patrols. */
        Queue<Integer> freePatrols = new LinkedList<Integer>();
        for(Entity p : patrols)
        {
            /* Ignore patrols taking prisioners to police station. */
            if (((Patrol)p).isTakingBurglarToPoliceStation()) continue;
            if (((Patrol)p).isFree()) freePatrols.add(p.getID());
        }

        /* Get ID of bank and burglar for this assault. */
        int bankID = banksBeingRobbed.poll();
        int burglarID = ((Bank)plan.getEntity(bankID)).whoIsTheBurglar();

        /* Dispatch free patrols to deal with robbery. */
        while (!freePatrols.isEmpty())
        {
            int patrolID = freePatrols.poll();
            Patrol p = (Patrol) plan.getEntity(patrolID);
            assaultsRecord.get(bankID).add(p);
            p.dealRobbery(burglarID);
            dispachedPatrols += patrolID + " ";
        }

         /* Select busy (in persuit) patrols that can be dispatched to this new robbery. */
        for(Integer recordsBankID : assaultsRecord.keySet())
        {
            /* Ignore the new bank beeing robbed when dispatching patrols. */
            if (recordsBankID == bankID) continue;
            
            /* ...and if there are too many patrols on one bank assault,
               dispatch some of them to the new one. */
            if (assaultsRecord.get(recordsBankID).size() > patrolsPerAssault)
                while(true)
                {
                    Patrol patrol = assaultsRecord.get(recordsBankID).firstElement();

                    assaultsRecord.get(recordsBankID).remove(patrol); /* Move patrol from one thief... */
                    assaultsRecord.get(bankID).add(patrol); /* ...to another. */
                    patrol.dealRobbery(burglarID);
                    dispachedPatrols += patrol.getID() + " ";
                    if (assaultsRecord.get(recordsBankID).size() <= patrolsPerAssault) break;
                }
        }

        if (dispachedPatrols.length() > 0)
            ThiefCatch.policeStationMessageBoard.addMessage("Dispatched police cars " + dispachedPatrols + "to bank " + bankID + ".");
    }

    /* Function to add supported routines to RPC Actor. */
    private void addRemoteRoutinesToActor()
    {
        //Add routine 1 (burglarEscaped)
        StationOfficer.add(new RemoteRoutine() {
            public void run() { burglarEscaped((Integer)args[0], (Integer)args[1]); }
        });

        //Add routine 2 (arrestedThief)
        StationOfficer.add(new RemoteRoutine() {
            public void run() { arrestedThief((Burglar)args[0], (Integer)args[1]); }
        });

        //Add routine 3 (IamBeingRobbed)
        StationOfficer.add(new RemoteRoutine() {
            public void run() { IamBeingRobbed((Integer)args[0]); }
        });

        //Add routine 4 (thiefWasDeliveredToPoliceStation)
        StationOfficer.add(new RemoteRoutine() {
            public void run() { thiefWasDeliveredToPoliceStation(); }
        });
    }
}
