/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.ua.ThiefCatch;

/**
 *
 * @author Paulo
 */
public abstract class Entity implements Runnable
{
    protected CityPlan plan; /* Reference to the city plan. */
    protected int ID; /* ID of this patrol. */
    protected Position currentPosition; /* Position on board of this entity. */

    
    /**
     * Returns the ID of this burglar.
     * @return the ID of this burglar.
     */
    public int getID()
    {
        assert ID >= 0;
        return ID;
    }


    /**
     * Returns the current position of the burglar in the city map.
     * @return the current position of the burglar in the city map.
     */
    public synchronized Position getPosition()
    {
        assert currentPosition != null;
        return currentPosition;
    }
}
