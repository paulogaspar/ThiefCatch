/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.ua.ThiefCatch;

import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ua.TextBoard.Colors;
import pt.ua.TextBoard.TextBoardViewer;

/**
 * Class responsable for the movement of police cars and burglars.
 * Each police car and burglar thread has its own insntace of this class, providing them
 * a way to move in the city, finding the path between two points.
 * This class also makes the movement itself, removing the entity from its old position
 * and placing it into the new position.
 * Also, this class allows several tipes of movement:
 * Random walk: creates a random path in map, and walks through it.
 * On persuit: each step recalculates the complete path to the destiny. This is necessary when
 * chasing the burglar, since he is constantly moving.
 * normal walk: simply calculates the path from one place to another, and the walks it step by step.
 *
 * @author Paulo Gaspar
 *
 */
public class Movement
{

    public enum Direction {Up, Down, Left, Right};

    /* References to plan and viewer. */
    private TextBoardViewer viewer;
    private CityPlan plan;

    /* Wandering path. */
    private Vector<Position> currentPath;
    private int pathIndex;

    /* Type of entity using this class. Ex.: policeCar, burglar. */
    private CityPlan.mapEntities entityType;

    /* ID of entity. */
    private int ID;

    /* State of entity when printing on screen. */
    private int state;

    /* Delay time the entity takes to go from one square to another. */
    private int travelTime;

    /**
     * Constructor for this class.
     * 
     * @param iPlan Reference to the cityPlan
     * @param iEntityType type of entity that instantiates this class: might be a Burglar or Patrol
     * @param iID ID of the entity instantiating this class.
     */
    public Movement(CityPlan iPlan, CityPlan.mapEntities iEntityType, int iID)
    {
        plan = iPlan;
        viewer = plan.getViewer();
        entityType = iEntityType;
        currentPath = null;
        ID = iID;
        travelTime = 200;
    }

    private void Move(Position oldPos, Position newPos)
    {
        /* Erase last position */
        viewer.setCell(oldPos.line(), oldPos.column(), plan.getViewerImageRepresentation( plan.get(oldPos) ));

        /* Set new position. */
        oldPos.setLine(newPos.line());
        oldPos.setColumn(newPos.column());

        /* And draw entity in new position. */
        if (plan.get(newPos) == CityPlan.mapEntities.ROAD.getCharacter())
            viewer.setCell(newPos.line(), newPos.column(), entityType.getImageRepresentation(state));
        else
            viewer.setCell(newPos.line(), newPos.column(), CityPlan.mapEntities.getEntityFromChar(plan.get(newPos)).getImageRepresentation(0) );

        delay(travelTime);
    }

    /**
     * Erases an entity from the graphical interface.
     * @param currentPosition Position to be erased, and replaced with the map original object.
     */
    public void eraseMeFromBoard(Position currentPosition)
    {
        plan.setPlaceAsOccupied(currentPosition, false);
        viewer.setCell(currentPosition.line(), currentPosition.column(), plan.getViewerImageRepresentation( plan.get(currentPosition) ));
    }

    

    /**
     * If a path from origin to destiny already exists, makes a step further in that path.
     * If there still is no path, creates a path from origin to destiny.
     * If type is "randomWalk" that destiny will be a random destiny within the map.
     * If type is "normalWalk" then makes a normal step as described above.
     * If type is "onPersuit" then each step recalculates the path from origin to destiny.
     * @param origin Origin of the path to be calculated.
     * @param destiny Destiny of the path to be calculated.
     * @param type Type of desired walk.
     * @return The position after a step was taken.
     */
    public Position makeWalkToPlace(Position origin, Position destiny, String type)
    {
        /* If there is no current path to walk, create one. Avoid recalculation on persuit if thief is in the same place. */
        if ((currentPath == null) || currentPath.isEmpty() || (type.equals("onPersuit") && !currentPath.lastElement().equals(destiny)))
            createPathTo(origin, destiny, type.equals("randomWalk"));
            
        /* This is wrong! Only happens when origin=destiny.*/
        if (currentPath.isEmpty()) return origin;

        /* newPos is the position to where it goes. oldPos is the position where it is right now. */
        Position newPos = new Position(currentPath.elementAt(pathIndex++));
        Position oldPos = new Position(origin);

        //System.out.print("\nBurglar " + ID + " walked from ("+oldPos.line()+", "+oldPos.column()+") to ("+newPos.line()+", "+newPos.column()+")");

        /* Try to walk to the next position. If it is occupied, find a new Path. */
        if (!plan.setPlaceAsOccupied(newPos, true))
        {
            currentPath = null;
            return oldPos;
        }

        /* Make movement. */
        Move(origin, newPos);

        /* Set old place as NOT occupied. */
        plan.setPlaceAsOccupied(oldPos, false);

        /* Se chegou ao fim do caminho calculado, terminar. */
        if (pathIndex >= currentPath.size()) currentPath = null;

        return newPos;
    }

    private void createPathTo(Position origin, Position bankPosition, boolean isRandomPath)
    {
        pathIndex = 0;

        Position destiny;
        if (isRandomPath) destiny = generateValidPosition(origin);
        else destiny = bankPosition;

        PathSolverLauncher launcher = new PathSolverLauncher(origin, destiny, plan);
        currentPath = launcher.getChosenPath().getPath();

        /* First position is always the origin position, thereby it must be ignored. */
        if (!currentPath.isEmpty())
            currentPath.removeElementAt(0);

        //printPath();
    }

    /**
     * Erases current path.
     */
    public void clearCurrentPath()
    {
        currentPath = null;
    }

    private void printPath()
    {
        Random rand = new Random();
        Colors bra = Colors.values()[rand.nextInt(8)];
       // System.out.print("Burglar " + ID + ": ");
        for (Position pos: currentPath)
            //System.out.print("("+ currentPath.elementAt(i).line()+","+ currentPath.elementAt(i).column()+") ");
            viewer.setCell(pos.line(), pos.column(), ".", bra);
    }

    private Position generateValidPosition(Position notAcceptedPosition)
    {
        Random rand = new Random();
        int linha, coluna;
        
        while (true)
        {
            linha = 1+rand.nextInt(plan.numberOfLines()-1);
            coluna = 1+rand.nextInt(plan.numberOfColumns()-1);

            if (!plan.isObstacle(new Position(linha, coluna)) && !notAcceptedPosition.equals(linha, coluna))
                return new Position(linha, coluna);
        }
    }

    private void delay(int mili)
    {
        try {
            Thread.sleep(mili);
        } catch (InterruptedException ex) {
            Logger.getLogger(Movement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setState(int state)
    {
        this.state = state;
    }

    public void setType(CityPlan.mapEntities entityType)
    {
        this.entityType = entityType;
    }

    public void setTravelTime(int time)
    {
        travelTime = time;
    }
}
