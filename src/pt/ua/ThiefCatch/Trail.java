package pt.ua.ThiefCatch;


import java.util.Vector;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Paulo
 */
public class Trail
{
    private Vector<Position> path;
    private Position startPosition;

    private boolean concluded; /* This variable tells if this Trail has an available valid path. */
    private boolean thereIsNoWay = false; /* Indicates if it is an impossible trail. A path was not found. */

    Trail(Position start)
    {
        startPosition = start;
        concluded = false;
        path = new Vector<Position>();
    }

    Trail(Trail copy)
    {
        path = (Vector<Position>) copy.getPath().clone();
        concluded = false;
        startPosition = new Position(copy.startPosition.line(), copy.startPosition.column());
    }

    public void add(Position pos)
    {
        path.add(pos);
    }

    public boolean isFromPath(Position pos)
    {
        return path.contains(pos);
    }

    public Vector<Position> getPath()
    {
        return path;
    }

    public boolean isConcluded()
    {
        return concluded;
    }

    public void setConcluded()
    {
        concluded = true;
    }

    public void set(Trail iPath)
    {
        path = (Vector<Position>) iPath.getPath().clone();
    }

    /**
     * @return true if it's still possible to find a way
     */
    public boolean isThereStillAWay() {
        return !thereIsNoWay;
    }

    /**
     * 
     */
    public void thereIsNoWay() {
        this.thereIsNoWay = true;
    }
}
