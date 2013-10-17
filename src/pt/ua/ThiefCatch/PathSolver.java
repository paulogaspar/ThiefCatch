package pt.ua.ThiefCatch;


import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Paulo Gaspar
 */

class PathSolver extends Thread
{
    private CityPlan plan;
    private Trail chosenPath;
    private Position position, destinyPos;
    private Movement.Direction dir;
    public char[][] boardMap; /* Map of passed places. */
    private Trail foundPath; /* Final path reference to destiny. 1st choice. */

    public final static Object lock = new Object(); /* Synchronize object. */
    private ThreadGroup group;

    private pt.ua.TextBoard.Colors cor; /* The color of this thread. To debugging purposes only. */

    public PathSolver(CityPlan iPlan, Trail iChosenPath, Position iPos, Movement.Direction iDir, Position destiny, Trail iFoundPath, char[][] iBoardMap, ThreadGroup group)
    {
        super();
        
        plan = iPlan;
        chosenPath = new Trail(iChosenPath);
        destinyPos = destiny;
        foundPath = iFoundPath;
        boardMap = iBoardMap;
        this.group = group;

        position = new Position(iPos); //set my position
        dir = iDir; //chosen direction

        //give random color to each thread
        Random rand = new Random();
        cor = pt.ua.TextBoard.Colors.values()[rand.nextInt(pt.ua.TextBoard.Colors.values().length)];
    }

    public void findPath()
    {
        /* check if he found the path */
        if ((position.line() == destinyPos.line()) && (position.column() == destinyPos.column()))
                synchronized(lock)
                {
                    //if (!wasThePathFound() || (foundPath.getPath().size() > chosenPath.getPath().size()+1))
                    {
                        chosenPath.add(position);
                        foundPath.set(chosenPath);
                        foundPath.setConcluded();
                        lock.notifyAll();
                    }
                }

        if (wasThePathFound()) return;

        //plan.getViewer().setCell(position.line(), position.column(), ".", cor);
        if (!setMap(position.line(), position.column(), '*')) return; /* In case that a thread was already here, die. */
//plan.getViewer().setCell(position.line(), position.column(), ".", cor);
        
        /* verify if already passed here. If not, mark as 'passed here' */
        if (chosenPath.isFromPath(position)) return;
        chosenPath.add(position);

        /* find available paths from current position */
        Vector<Movement.Direction> directions = new Vector<Movement.Direction>(); //to save all available directions
        boolean thisThreadGoesOn = false;
        for (Movement.Direction direction : Movement.Direction.values())
        {
            Position newPos = plan.getPosition(position, direction); //obtain next position

            if ((!newPos.equals(destinyPos)) && (plan.isObstacle(newPos) || isGoingBack(direction) || (getMap(newPos.line(), newPos.column()) == '*'))) continue; //obstacle or going back ?

            /* if current direction is to be continued, activate flag */
            if (direction == dir) thisThreadGoesOn = true;
            else directions.add(direction); /* else, add direction to list of directions */
        }

        delay(1); //SEM ESTE DELAY AS THREADS SEGUEM UM CAMINHO MUITO ESTRANHO

        /* if there are no directions to follow, we have reached a dead end. */
        if (directions.isEmpty() && !thisThreadGoesOn) return;

        //if there is only one way, there is no need to launch a new thread.
       /* if ((directions.size() == 1) && !thisThreadGoesOn)
        {
            position = plan.getPosition(position, directions.firstElement());
            findPath();
            return;
        }*/
        
        /* for each available direction, launch a seeker */
        Vector<Thread> seekers = new Vector<Thread>();
        for (Movement.Direction direction : directions)
        {
            Position newPos = plan.getPosition(position, direction);
            seekers.add(launchNewSeeker(newPos, direction));
        }

        if (thisThreadGoesOn)
        {
//            if (directions.isEmpty() && (group.activeCount() == 1))
//                System.out.println("One way: " + dir.toString());
            position = plan.getPosition(position, dir);
            findPath();
        }
    }


    private Thread launchNewSeeker(Position iPos, Movement.Direction direction)
    {
        

        PathSolver solver = new PathSolver(plan, chosenPath, iPos, direction, destinyPos, foundPath, boardMap, group);

       // Thread thread = new Thread(group, solver);
        solver.start();

        return solver;
    }

    private boolean isGoingBack(Movement.Direction direction)
    {
        /* verify if the new direction will lead to going back. */
        switch (direction)
        {
            case Up:
                    if (dir == Movement.Direction.Down) return true;
                    break;
            case Down:
                    if (dir == Movement.Direction.Up) return true;
                    break;
            case Left:
                    if (dir == Movement.Direction.Right) return true;
                    break;
            case Right:
                    if (dir == Movement.Direction.Left) return true;
                    break;
        }

        return false;
    }

    private void delay(int i)
    {
        try {
                Thread.sleep(i);
            } catch (InterruptedException ex) {
                Logger.getLogger(PathSolver.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    public boolean wasThePathFound()
    {
        return (foundPath.isConcluded() || !foundPath.isThereStillAWay());
    }

    public Trail getFinalPath()
    {
        return foundPath;
    }

    public char getMap(int line, int column)
    {
      return boardMap[line-1][column -1];
    }

    public synchronized boolean setMap(int line, int column, char c)
    {
        if (boardMap[line-1][column -1] == c) return false;
        boardMap[line-1][column -1] = c;

        return true;
    }
    
    public void waitForPathToBeReady()
    {
        try {
                synchronized (lock)
                 {  
                    lock.wait(); 
                 }
            } catch (InterruptedException ex) {
                Logger.getLogger(PathSolver.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    @Override
    public  void run()
    {
        plan.checkMaxNumberOfThreads();

        findPath();

        if (!wasThePathFound() && (group.activeCount() == 1))
            foundPath.thereIsNoWay();
    }

    
}
