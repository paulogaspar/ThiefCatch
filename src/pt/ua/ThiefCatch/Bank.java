package pt.ua.ThiefCatch;

import pt.ua.ThiefCatch.Synchronism.Channel;

/**
 * Class responsable for the bank threads.
 * Waits until a client appears, to wakeup and warn the police station about a robbery.
 * THen goes back to waiting again.
 *
 * @author Paulo Gaspar
 *
 */
public class Bank extends Entity
{
    private boolean beingRobbed;
    private Entity client; /* Entity being served by the bank. */

    /* Channel to receive entity references when an entity enters the bank. */
    private Channel queueAlarm;

    /**
     * Constructor for the bank.
     *
     * @param iPlan reference to the CityPlan.
     * @param iPosition Position of this bank on the city map.
     * @param iID ID that this bank will have.
     */
    public Bank(CityPlan iPlan, Position iPosition, Integer iID)
    {
        assert iPlan != null;
        assert (iPosition != null);
        assert (iPlan.validPosition(iPosition));
        assert (iID >= 0);

        currentPosition = iPosition;
        ID = iID;
        plan = iPlan;

        beingRobbed = false;

        queueAlarm = new Channel();
    }

    @Override
    public void run()
    {
        while(true)
        {
            client = (Entity) queueAlarm.take(); /* Wait for alarm to go on. */

            if (client instanceof Burglar)
            {
                synchronized (this)
                {   beingRobbed = true;  }
                callPoliceStation();
            }
        }
    }

    /**
     * Informs the bank that there is a burglar inside, and that an assault is beggining.
     * The client reference is put into a channel, in order for the Bank to receive it.
     *
     * @param client Reference to the client making the assault.
     */
    public void thisIsAnAssault(Entity thief)
    {
        assert thief != null;
        assert plan.getEntity(thief.getID()) != null;
        assert thief instanceof Burglar;
        assert queueAlarm != null;

        queueAlarm.put(thief);
    }

    /**
     * Inform bank that the assault terminated.
     * This is called by the burglar to inform the end of the assault.
     */
    public synchronized void assaultTerminated()
    {
        assert beingRobbed;

        beingRobbed = false;
    }

    private void callPoliceStation()
    {
        //plan.getPoliceStation().IamBeingRobbed(ID);
        PoliceStation.StationOfficer.invokeSyncProc(3, ID);
    }

    /**
     * Ask the bank for a description(ID) of the burglar.
     * @return the ID of the burglar.
     */
    public int  whoIsTheBurglar()
    {
        assert beingRobbed;
        assert client != null;

        return client.getID();
    }

    /**
     * Tells wether the assault is over or not.
     * @return true if there is no current assault.
     */
    public synchronized boolean isTheAssaultOver()
    {
        return !beingRobbed;
    }

}
