/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.ua.ThiefCatch;

import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class responsable for the hideout thread.
 * This class launches new burglar threads with random bank destination.
 * The chosen bank for each new thief must not have been designated yet for another thief.
 *
 * @author Paulo Gaspar
 *
 */
public class HideOut extends Entity
{
    private static final int minDelay = 1000;

    private Position[] bankList;

    /* To set burglars IDs. */
    private static int burglarID;

    /* List of banks beeing robbed. */
    private Vector<Integer> banksBeeingRobbed;

    /* Random number creator. */
    private Random rand;

    /* Number of banks in the city. */
    int numberOfBanks;

    public HideOut(CityPlan iPlan, Position iMyPosition, Integer iID)
    {
        assert iPlan != null;
        assert (iMyPosition != null);
        assert (iPlan.validPosition(iMyPosition));
        assert (iID >= 0);

        ID = iID;
        plan = iPlan;
        currentPosition = iMyPosition;

        burglarID = plan.getMaxEntitiesID() + 10;

        banksBeeingRobbed = new Vector<Integer>();
        rand = new Random();
    }

    @Override
    public void run()
    {
        int bankID;
        Position bankPos;
        Burglar thief;

        numberOfBanks = plan.getNumberOfEntities(CityPlan.mapEntities.BANK);

        while (true)
        {
            /* Wait at least two seconds before lauching another burglar. */
            delay(minDelay + rand.nextInt(1000));

            if (banksBeeingRobbed.size() < numberOfBanks)
            {
                /* Choose a victim bank. */
                bankID = chooseRandomBank();

                /* Launch a thief. */
                thief = new Burglar(plan, currentPosition, burglarID, bankID);
                plan.addEntityToList(thief);

                burglarID++;
                banksBeeingRobbed.add(bankID);

                new Thread(thief).start();
                ThiefCatch.HideOutMessageBoard.addMessage("Released burgler " + burglarID + " with destiny bank " + bankID);
            }
        }
    }

    /* TODO: implementar smart thiefs! quando procurar um novo banco, encontrar um que esteja o mais longe dos em assalto, e mais perto do hideOut. Cuidado: Evitar k ele escolha sempre o mais perto. */

    /* Choose a bank not chosen yet. */
    private int chooseRandomBank()
    {
        assert plan.getEntityList(CityPlan.mapEntities.BANK).size() > 0;
        assert banksBeeingRobbed != null;

        int IDnumber;
        Vector<Entity> entList = plan.getEntityList(CityPlan.mapEntities.BANK);

        while (true)
        {
            IDnumber = entList.elementAt(rand.nextInt(numberOfBanks)).getID();
            if (!banksBeeingRobbed.contains(IDnumber)) return IDnumber;
        }
    }

    private void delay(int mili)
    {
        assert mili >= 0;

        try {
            Thread.sleep(mili);
        } catch (InterruptedException ex) {
            Logger.getLogger(HideOut.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Informs the hideout that a roberry was terminated, meaning the burglar got to
     * the hideout after robbing a bank, without getting caught.
     * Warns the police station about the burglar escape, so they can warn the police
     * cars to stop chasing it.
     *
     * @param bankID Robbed bank ID.
     * @param thiefID ID of the thief making the robbery.
     */
    public void terminatedRobbery(int bankID, int thiefID)
    {
        assert bankID >= 0;
        assert thiefID >= 0;
        assert plan.getEntity(bankID) != null;
        assert plan.getEntity(thiefID) != null;
        assert plan.getEntity(bankID) instanceof Bank;
        assert plan.getEntity(thiefID) instanceof Burglar;
        assert ((Burglar)plan.getEntity(thiefID)).getBankID() == bankID;

        ThiefCatch.HideOutMessageBoard.addMessage("Burglar "+thiefID+" escaped!");
        banksBeeingRobbed.removeElement(bankID);
        plan.removeEntityFromList(plan.getEntity(thiefID));

        /* RPC call to warn police station. */
        PoliceStation.StationOfficer.invokeSyncProc(1, thiefID, bankID); //plan.getPoliceStation().burglarEscaped(thiefID);
    }

    /**
     * Informs the hideout that the burglar was arrested by the police.
     * @param bankID ID of Bank that was being robbed by the arrested thief.
     * @param thiefID ID of thief that was arrested.
     */
    public void gotArrested(int bankID, int thiefID)
    {
        assert banksBeeingRobbed != null;
        assert !banksBeeingRobbed.isEmpty();
        assert banksBeeingRobbed.contains(bankID);
        assert plan.getEntity(thiefID) != null;
        assert plan.getEntity(bankID) != null;
        assert plan.getEntity(thiefID) instanceof Burglar;
        assert plan.getEntity(bankID) instanceof Bank;

        banksBeeingRobbed.removeElement(bankID);
        plan.removeEntityFromList(plan.getEntity(thiefID));
    }

    /**
     * Returns the burglar reference.
     * @param thiefRunningAwayID ID of the burglar.
     * @return the reference to the burglar with that ID.
     */
    public Burglar getBurglar(int thiefRunningAwayID)
    {
        assert plan.getEntity(thiefRunningAwayID) != null;
        assert plan.getEntity(thiefRunningAwayID) instanceof Burglar;

        return (Burglar) plan.getEntity(thiefRunningAwayID);
    }
}
