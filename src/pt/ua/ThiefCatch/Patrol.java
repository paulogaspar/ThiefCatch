

package pt.ua.ThiefCatch;

import pt.ua.ThiefCatch.Synchronism.ReadersWriterExclusion;


/**
 * Class responsable for the police car threads.
 * while nothing happens in the city, they walk randomly through it.
 * When the police station demands a persuit on some burglar, they go
 * after the thief until they catch him or he escapes to the hideout.
 * When a patrol catches a burglar, he must return it to the police station.
 *
 * @author Paulo Gaspar
 *
 */
public class Patrol extends Entity
{
    /** Time the burglar takes to walk from one city square into another. */
    public static final int delayTime = 40;

   // private Position currentPosition; /* Position on board of this entity. */
    private Position policeStationPosition;
  //  private int ID; /* ID of this patrol. */
  //  private CityPlan plan; /* Reference to the city plan. */
    private State currentState; /* Current state of the police car. See below. */

    private int thiefRunningAwayID; /* ID of burglar that this patrol is on persuit of. */
    private int policeStationID;

    /* Movement class. */
    private Movement movement;

    /* Synchronism scheme. */
    private ReadersWriterExclusion sync;


    /* States that the patrol can be on. */
    private enum State { Wandering, ChasingBurglar, TakingToPoliceStation };

    /* Reference to the thief this patrol is chasing. */
    private Burglar thief;

    /**
     * Constructor for this class.
     *
     * @param iPlan Reference to the CityPlan.
     * @param initialPosition Position where this thief starts its journey to the bank.
     * @param iID ID of this patrol.
     */
    public Patrol(CityPlan iPlan, Position initialPosition, Integer iID)
    {
        assert iPlan != null;
        assert initialPosition != null;
        assert iPlan.validPosition(initialPosition);
        assert iID >= 0;

        currentPosition = initialPosition;
        ID = iID;
        plan = iPlan;
        policeStationPosition = plan.getEntityPositions(CityPlan.mapEntities.POLICESTATION)[0];

        currentState = State.Wandering;

        sync = new ReadersWriterExclusion();
        movement = new Movement(plan, CityPlan.mapEntities.PATROL, ID);

        plan.setPlaceAsOccupied(initialPosition, true);
        plan.set(initialPosition, CityPlan.mapEntities.ROAD.getCharacter());
    }

    @Override
    public void run()
    {
        /* Temporary value of current state. */
        State state;

        /* Police Station ID. */
        policeStationID = plan.getEntityList(CityPlan.mapEntities.POLICESTATION).firstElement().getID();

        /* State machine. */
        while(true)
        {

            sync.lockReader();
            state = currentState;
            sync.unlockReader();

            /* Select current state, and execute it's code. */
            switch (state)
            {
                case Wandering:
                    movement.setTravelTime(200);
                    movement.setState(0);
                    movement.makeWalkToPlace(currentPosition, null, "randomWalk");
                    break;

                case ChasingBurglar:
                    movement.setTravelTime(50);
                    movement.setState(1);

                    sync.lockReader();
                    if (nearEnough(thief))
                    {
                        /* Get thief, and inform him of arest. */
                        if (!thief.youAreUnderArest()) continue; /* If burglar escaped, wait for orders from police station. */

                        /* Warn police station. */
                        PoliceStation.StationOfficer.invokeSyncProc(2, thief, ID); //plan.getPoliceStation().arrestedThief(thiefRunningAwayID, ID);
                        sync.unlockReader();

                        /* Change to next state. */
                        changeState(State.TakingToPoliceStation);
                        movement.clearCurrentPath();
                        break;
                    }
                    else
                        sync.unlockReader();

                    /* Walk a step in the city. */
                    movement.makeWalkToPlace(currentPosition, thief.getPosition(), "onPersuit");

                    break;
                    
                case TakingToPoliceStation:
                    movement.setTravelTime(100);
                    movement.setState(1);
                    movement.makeWalkToPlace(currentPosition, policeStationPosition, "normalWalk");

                    /* If arrived police station. */
                    if (currentPosition.equals(policeStationPosition))
                    {
                        changeState(State.Wandering);

                        movement.clearCurrentPath();
                        PoliceStation.StationOfficer.invokeSyncProc(4); //plan.getPoliceStation().thiefWasDeliveredToPoliceStation();
                    }
                    break;
            }
        }
    }

    /* To check if entity is in neighbour-8. */
    private boolean nearEnough(Entity entity)
    {
        assert entity != null;

        Position entityPos = entity.getPosition();

        /* If it's a burglar, and he got to the hide out, return false. */
        if (entity instanceof Burglar)
            if (entityPos.equals(plan.getEntityPositions(CityPlan.mapEntities.HIDEOUT)[0])) return false;

        int entityLine = entityPos.line();
        int entityColumn = entityPos.column();

        int myLine = currentPosition.line();
        int myColumn = currentPosition.column();

        if ((myLine>=entityLine-1) && (myLine<=entityLine+1) &&(myColumn>=entityColumn-1) && (myColumn<=entityColumn+1))
            return true;

        return false;
    }

    private void changeState(State state)
    {
        assert state != null;

        sync.lockWriter();
        currentState = state;
        sync.unlockWriter();
    }

    /**
     * Inform this patrol to start the persuit of a burglar.
     * @param entityID Burglar to persuit.
     */
    public void dealRobbery(int thiefID)
    {
        assert (thiefID >=0 );
        assert plan.getEntity(thiefID) != null;
        assert (plan.getEntity(thiefID) instanceof Burglar);
        assert currentState != State.TakingToPoliceStation;
        assert thiefRunningAwayID != thiefID;

        sync.lockWriter();
        thiefRunningAwayID = thiefID;
        thief = ((Burglar)plan.getEntity(thiefRunningAwayID));
        currentState = State.ChasingBurglar;
        sync.unlockWriter();
    }

    /**
     * Inform this patrol that the persuit was finished due to some reason.
     */
    public void persuitFinished()
    {
        assert currentState == State.ChasingBurglar;

        sync.lockWriter();
        currentState = State.Wandering;
        sync.unlockWriter();
    }

    /**
     * Ask this patrol if he is available to start a persuit.
     * @return true if available.
     */
    public boolean isFree()
    {
        
        sync.lockReader();
        boolean value = (currentState == State.Wandering);
        sync.unlockReader();

        return value;
    }

    /**
     * Returns true if is taking a thief to the police station after arresting him.
     * @return true if is taking a thief to the police station after arresting him.
     */
    public boolean isTakingBurglarToPoliceStation()
    {
        sync.lockReader();
        boolean value = currentState == State.TakingToPoliceStation;
        sync.unlockReader();

        return value;
    }

}
