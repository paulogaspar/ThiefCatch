
package pt.ua.ThiefCatch;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import static java.lang.System.*;
import pt.ua.TextBoard.TextBoardViewer;
import pt.ua.ThiefCatch.CityPlan.mapEntities;
import pt.ua.ThiefCatch.Synchronism.ReadersWriterExclusion;

/**
 * Class responsable for the city map and entities.
 * This class loads the map into memory, and creates all the threads.
 * Also, it loads the images of each entity to show in the graphical interface.
 * 
 * MANY FUNCTIONS ON THIS CLASS WERE ADAPTED FROM PROFESSOR Miguel Oliveira e Silva (University of Aveiro) code.
 *
 * @author Paulo Gaspar
 *
 */
public class CityPlan
{

    /* Map file characters interpretation. */
    public static enum mapEntities
    {
        /* Map caracter, isRequired, mustBeUnique, priority, class, imagePaths */
        ROAD (' ', true, false, -1, null),
        WALL ('#', true, false, -1, null, "img/wall.png", "img/wall2.png", "img/wall3.png"),
        PATROL ('C', true, false, 0, Patrol.class, "img/patrol.png", "img/patrol2.png"),
        BURGLAR ('R', false, false, -1, Burglar.class, "img/burglar.png"),
        POLICESTATION('P', true, true, 3, PoliceStation.class, "img/station.png"),
        BANK('B', true, false, 1, Bank.class, "img/bank.png"),
        HIDEOUT('H', true, true, 2, HideOut.class, "img/hideout.png");
        
       private char mapCharacter;
       private boolean required; //must this entity exist in the map?
       private boolean unique; //must this entity be unique in the map?
       private int priority; //priority in witch the respective entity is instantiated (zero is the biggest priority)
       private Class classe; //class responsible for this entity
       private Vector<ImageIcon> icon; //images representing this character on visual interface

       private mapEntities(char mapCharacter, boolean required, boolean unique, int priority, Class classe, String... imagePaths)
       {
           this.mapCharacter = mapCharacter;
           this.required = required;
           this.unique = unique;
           this.classe = classe;
           this.icon = null;
           this.priority = priority;

           try {
               if ((imagePaths.length > 0) && (imagePaths != null)) {
                   this.icon = new Vector<ImageIcon>();
                   for (String s : imagePaths) {
                       icon.add(new ImageIcon(Toolkit.getDefaultToolkit().getImage(s).getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
                   }
               }
           }
           catch (Exception ex) {
               System.out.println("Error while reading/creating images.");
               exit(1);
           }
       }
       
       public char getCharacter()
       { return this.mapCharacter; }

       public boolean isUnique()
       { return this.unique; }

       public boolean isRequired()
       { return this.required; }

       public Class getEntityClass()
       { return this.classe; }

       public int getPriority()
       { return priority; }

       private static int getMaxPriority()
       {
           int max = 0;
           for (mapEntities e : mapEntities.values())
                if (e.getPriority() > max) max = e.getPriority();

           return max;
        }

       public static mapEntities getEntityFromChar(char c)
       {
           for (mapEntities e : mapEntities.values())
                if (e.getCharacter() == c) return e;
          
           return null;
       }

       public ImageIcon getImageRepresentation(int imageNumber)
       { return icon == null? new ImageIcon() : icon.elementAt(imageNumber); }

    }

    /* Map sizes. */
    private int numLines;
    private int numColumns;

    /* Map character matrix. */
    private char[][] matrix;

    /* List of all active entities on the city. */
    private Vector<Entity> EntityList;

    /* Synchronism scheme. */
    private final ReadersWriterExclusion sync = new ReadersWriterExclusion();

    /* Reference to viewer. */
    private TextBoardViewer viewer;

    /** Movement synchronization lock objects. */
    public static Boolean[][] positionOccupied;

    private Random rand;

    /**
     * Contructor of the city plan.
     * @param string filename of the city map.
     */
    public CityPlan(String string) throws Exception
    {
        assert string != null;
        assert string.length() > 0;

        rand = new Random();

        /* Read map to memory. */
        readFromFile(string);

        /* Verify if read map is a valid map. */
        verifyMapValidity();

        /* Set all positions as free (unoccupied). */
        positionOccupied = new Boolean[numLines][numColumns];
        for (int i=0; i<numLines; i++)
            for (int j=0; j<numColumns; j++)
                positionOccupied[i][j] = new Boolean(false);
    }

    private void verifyMapValidity() throws Exception
    {
        /* Count number of occurences of each entity. */
        Map<Character, Integer> numEntities = new HashMap<Character, Integer>();
        for (int i=0; i<numLines; i++)
            for (int j=0; j<numColumns; j++)
                if (!numEntities.containsKey(matrix[i][j]))
                    numEntities.put(matrix[i][j], 1);
                else
                    numEntities.put(matrix[i][j], numEntities.get(matrix[i][j])+1);

        /**********************************************************/
        /* Check for all obligatory entities and unique entities. */
        /**********************************************************/
        for (mapEntities e : mapEntities.values())
        {
            if (!numEntities.containsKey(e.getCharacter()) && e.isRequired())
                throw new Exception("Error: Invalid map: Map should at least have one of each of these entities: Bank(B) Hideout(H) Patrol(C) Police Station(P) Wall(#) Road( ).");

            if (numEntities.containsKey(e.getCharacter()))
                if ((numEntities.get(e.getCharacter()) > 1) && e.isUnique())
                    throw new Exception("Error: Invalid map: This entity can't have more than one instance: " + e.getCharacter());
        }

        /****************************/
        /* Verify if map is closed. */
        /****************************/
        for (int j=0; j<numColumns; j++)
            if ((matrix[0][j] != mapEntities.WALL.getCharacter()) || (matrix[numLines-1][j] != mapEntities.WALL.getCharacter()))
                    throw new Exception("Error: Invalid map: The city map must be surrounded with walls (#).");

        for (int i=0; i<numLines; i++)
            if ((matrix[i][0] != mapEntities.WALL.getCharacter()) || (matrix[i][numColumns-1] != mapEntities.WALL.getCharacter()))
                    throw new Exception("Error: Invalid map: The city map must be surrounded with walls (#).");

        /****************************************************************/
        /* Verify if between all entities the map is a connected graph. */
        /****************************************************************/
        Position[] policeStationPositions = getEntityPositions(mapEntities.POLICESTATION);
        char[][] tmpMatrix = new char[numLines][numColumns];
        //arraycopy(matrix, 0, tmpMatrix, 0, matrix.length);
        for (int i=0; i<numLines; i++)
            for (int j=0; j<numColumns; j++)
                tmpMatrix[i][j] = matrix[i][j];
        fillConnectedPaths(policeStationPositions[0].line()-1, policeStationPositions[0].column()-1, tmpMatrix); //fill tmpMatrix with '*' in all places connected to the police station.

        for (Object key : numEntities.keySet())
        {
            mapEntities entity = mapEntities.getEntityFromChar( (Character)key);
            if (entity.getEntityClass() == null) continue;

            Position [] positions = getEntityPositions(mapEntities.getEntityFromChar( (Character)key) );
            
            for (Position pos: positions)
                if ((tmpMatrix[pos.line()-2][pos.column()-1] != '*') &&
                   (tmpMatrix[pos.line()][pos.column()-1] != '*') &&
                   (tmpMatrix[pos.line()-1][pos.column()-2] != '*') &&
                   (tmpMatrix[pos.line()-1][pos.column()] != '*'))
                        throw new Exception("Error: Invalid map: Some entities are isolated and cannot connect to others. All two entities must have a road between them.");

        }
    }

    public void fillConnectedPaths(int l, int c, char[][] matriz)
    {
        if (validLine(l+1) && validColumn(c+1) && ((matriz[l][c] == ' ') || (matriz[l][c]== 'P')))
        {
                matriz[l][c] = '*';
                fillConnectedPaths(l+1, c, matriz);
                fillConnectedPaths(l-1, c, matriz);
                fillConnectedPaths(l, c+1, matriz);
                fillConnectedPaths(l, c-1, matriz);
        }
    }

    /**
     * Creates each entity of the simulation.
     * For each entity, checks in the map its position before creating it.
     * Entities are created with a specific order, sorted by priority.
     * Creates police cars, banks, police station, and hideout.
     */
    public void createEntitiesFromMap()
    {
        EntityList = new Vector<Entity>();
        int entityID = 0;

        int priority = 0;
        while (priority <= mapEntities.getMaxPriority())
        {
            for (mapEntities entity : mapEntities.values())
            {
                /* Instantiate only entities who have a class of their own and priority. Instantiate by priority order. */
                if ((entity.getEntityClass() == null) || (entity.getPriority() == -1) || (entity.getPriority() != priority)) continue;

                Position [] entityPositions = getEntityPositions(entity);

                Constructor C = null;
                try { C = entity.getEntityClass().getConstructor(CityPlan.class, Position.class, Integer.class); }
                catch (Exception ex) { System.out.println("Error trying to get constructor of entity: " + ex.getMessage()); exit(1); }

                for (int i=0; i<entityPositions.length; i++)
                  try { EntityList.add((Entity) C.newInstance(this, entityPositions[i], entityID++)); 
                  System.out.println("Created entity " + entity.toString() + " with priority " + priority); }

                  catch (InstantiationException ex) {
                    Logger.getLogger(CityPlan.class.getName()).log(Level.SEVERE, null, ex); exit(1);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(CityPlan.class.getName()).log(Level.SEVERE, null, ex); exit(1);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(CityPlan.class.getName()).log(Level.SEVERE, null, ex); exit(1);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(CityPlan.class.getName()).log(Level.SEVERE, null, ex); exit(1);
                }  
            }
            
            priority++;
        }

    }

    /**
     * Tries to set a place in the city map as occupied or not, so it get locked to someone, or free to step into.
     *
     * @param pos Position to set as occupied or not.
     * @param setOccupied value to set. True to try to set as occupied.
     * @return true when successfully changed to the setOccupied value. False otherwise.
     */
    public boolean setPlaceAsOccupied(Position pos, boolean setOccupied)
    {
        int line = pos.line();
        int column = pos.column();

        synchronized (positionOccupied[line][column])
        {
            if (setOccupied && positionOccupied[line][column]) return false;
            positionOccupied[line][column] = setOccupied;
            return true;
        }
    }

    /*
     * Function to read a map file.
     */
    public void readFromFile(String nome) throws Exception
    {
        try
        {
          // counting lines & columns:
          numLines = 0;
          numColumns = 0;
          File fin = new File(nome);
          Scanner scin = new Scanner(fin);
          while(scin.hasNextLine())
          {
            String line = scin.nextLine();
            if (line.length() > numColumns)
              numColumns = line.length();
            numLines++;
          }
          scin.close();

          matrix = new char[numLines][numColumns];

          // reading plan:
          scin = new Scanner(fin);
          for(int l=0; scin.hasNextLine(); l++)
          {
            int c;
            String line = scin.nextLine();
            for(c = 0;c < line.length();c++)
              matrix[l][c] = line.charAt(c);
            for(;c < numColumns;c++)
              matrix[l][c] = mapEntities.ROAD.getCharacter();
          }
          scin.close();

        }
        catch(IOException e)
        {
          throw new Exception("FATAL ERROR: unable to read city plan from file!");
        }
    }

    public boolean validLine(int line)
    {
      return line >= 1 && line <= numberOfLines();
    }

    public boolean validColumn(int column)
    {
      return column >= 1 && column <= numberOfColumns();
    }

    boolean validPosition(Position initialPosition)
    {
        return validLine(initialPosition.line()) && validColumn(initialPosition.column());
    }

    public int numberOfLines()
    {
        return numLines;
    }

    public int numberOfColumns()
    {
      return numColumns;
    }

    public char get(int line, int column)
    {
        assert validLine(line) && validColumn(column);

        return matrix[line-1][column -1];
    }

    public char get(Position pos)
    {
        return get(pos.line(), pos.column());
    }

    public void set(int line, int column, char c)
    {
        assert validLine(line) && validColumn(column);

        matrix[line-1][column -1] = c;
    }

    public void set(Position pos, char c)
    {
        set(pos.line(), pos.column(), c);
    }

    /**
     * Given a character from the map, returns the imageIcon representation of it.
     * @param c character from the map. Must be one of this: #, B, P, C, R, H.
     * @return ImageIcon representation of the character.
     */
    public ImageIcon getViewerImageRepresentation(char c)
    {
        mapEntities entity = mapEntities.getEntityFromChar(c);
        int selectedImage = 0;

        /* A little bit of personalization :P ... just to spice the application.
           This also ruins the oject oriented paradigm of this function.        */
        if (entity == mapEntities.WALL)
        {
            switch(rand.nextInt(3))
            {
                case 0: selectedImage = 0; break;
                case 1: selectedImage = 1; break;
                case 2: selectedImage = 2; break;
            }
        }

        return entity.getImageRepresentation(selectedImage);
    }
    
    /**
     * For a given entity type, return all positions of all entities of that type.
     * For example, given the type Bank, returns the positions of all Banks.
     *
     * @param entity entity type to lookfor.
     * @return positions of all entities of that type.
     */
    public Position[] getEntityPositions(mapEntities entity)
    {
        Position[] result = new Position[0];

        for(int l=0; l < numLines; l++)
          for(int c=0; c < numColumns; c++)
            if (matrix[l][c] == entity.getCharacter())
            {
              Position[] newRes = new Position[result.length + 1];
              arraycopy(result, 0, newRes, 0, result.length);
              newRes[result.length] = new Position(l+1, c+1);
              result = newRes;
            }

        return result;
    }

    public Position getEntityPositionByID(int entityID)
    {
        sync.lockReader();
        for (Entity e : EntityList)
            if (e.getID() == entityID)
            {
                //System.out.println("Entity " + e.getClass().getSimpleName() + " is at " + e.getPosition().line() + ","+e.getPosition().column());
                Position pos = e.getPosition();
                sync.unlockReader();
                return pos;
            }

        sync.unlockReader();
        return null;
    }

    public void setViewer(TextBoardViewer iViewer)
    {
        viewer = iViewer;
    }

    public TextBoardViewer getViewer()
    {
        return viewer;
    }

    public char[][] getMatrix()
    {
        return matrix;
    }

    /* Get a new position from a source and a direction. */
    public Position getPosition(Position source, Movement.Direction dir)
    {
        switch(dir)
        {
            case Up: return new Position(source.line()-1, source.column());
            case Down: return new Position(source.line()+1, source.column());
            case Left: return new Position(source.line(), source.column()-1);
            case Right: return new Position(source.line(), source.column()+1);
        }

        return null;
    }

    /**
     * Return true if in that position of the map there is an entity of that type.
     */
    public boolean isEntityInPosition(Position pos, mapEntities entity)
    {
        assert (pos != null) && validPosition(pos);

        int line = pos.line();
        int column = pos.column();

        return get(line, column) == entity.getCharacter();
    }

    /**
     * Returns true if the given position is a wall, or a police station, or a bank, or a hideout, or burglar or patrol.
     * @param pos Position to check for obstacles.
     * @return true if given position has an obstacle.
     */
    public boolean isObstacle(Position pos)
    {
        boolean isObstacle = isEntityInPosition(pos, mapEntities.WALL) ||
               isEntityInPosition(pos, mapEntities.POLICESTATION) ||
               isEntityInPosition(pos, mapEntities.BANK) ||
               isEntityInPosition(pos, mapEntities.HIDEOUT);

        if (isObstacle) return true;

        for (Entity p : getEntityList(mapEntities.PATROL))
            if (p.getPosition().equals(pos)) return true;

        for (Entity p : getEntityList(mapEntities.BURGLAR))
            if (p.getPosition().equals(pos)) return true;

        return false;
    }

    public synchronized void launchThreads()
    {
        /* Create thread group. */
        group = new ThreadGroup("ThiefCatch thread group");
        maxNumberOfActiveThreads = EntityList.size();

        Thread thread = null;

        for (Entity entity : EntityList)
        {
            thread = new Thread(group, entity);
            thread.start();
            System.out.println("Started an entity.");
        }
        
        try { thread.join(); }
        catch (InterruptedException ex)
        {
            System.out.println("Error while waiting for thread to die.");
            exit(1);
        }
    }

    public int getNumberOfEntities(mapEntities entity)
    {
        return getEntityList(entity).size();
    }

    public Entity getEntity(int ID)
    {
        sync.lockReader();
        for (Entity e : EntityList)
            if (e.getID() == ID) 
            {
                Entity ent = e;
                sync.unlockReader();
                return ent;
            }


        sync.unlockReader();
        return null;
    }

    public Vector<Entity> getEntityList(mapEntities entity)
    {
        sync.lockReader();
        Vector<Entity> listOfEntities = new Vector<Entity>();
        for (Entity e : EntityList)
            if (e.getClass() == entity.getEntityClass())
                listOfEntities.add(e);

        sync.unlockReader();
        return listOfEntities;
    }

    public int getMaxEntitiesID()
    {
        sync.lockReader();
        int ID = -1;
        for (Entity e : EntityList)
            if (e.getID() > ID) ID = e.getID();

        sync.unlockReader();
        return ID;
    }

    public void addEntityToList(Entity entity)
    {
        sync.lockWriter();
        EntityList.add(entity);
        sync.unlockWriter();
    }

    public void removeEntityFromList(Entity entity)
    {
        sync.lockWriter();
        EntityList.remove(entity);
        sync.unlockWriter();
    }

    private static int maxNumberOfActiveThreads = 1;
    private static ThreadGroup group = new ThreadGroup("ThiefCatch thread group");

    public void checkMaxNumberOfThreads()
    {
        if (group.activeCount() > maxNumberOfActiveThreads)
        {
            maxNumberOfActiveThreads = group.activeCount();
            System.out.println("Maximum active threads: " + maxNumberOfActiveThreads);
        }
    }
}
