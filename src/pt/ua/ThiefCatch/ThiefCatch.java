
package pt.ua.ThiefCatch;

import static java.lang.System.*;
import pt.ua.TextBoard.*;

/**
 * This is the main class of the simulation.
 * Here, the cityplan is created, and the threads are launched.
 *
 * @author Paulo Gaspar
 *
 */
public class ThiefCatch
{
    /**
     * Message boards to record output messages.
     */
    public static MessageBoard policeStationMessageBoard;
    public static MessageBoard HideOutMessageBoard;

    /**
     * @param args File name of the map file
     */
    public static void main(String[] args) 
    {
        /* If there are no arguments to the program, exit with an error message. */
        if (args.length == 0) { err.println("Use: ThiefCatch <mapFile>"); exit(1);  }

        /* Create message boards. */
        policeStationMessageBoard = new MessageBoard("Police Station Messages");
        HideOutMessageBoard =  new MessageBoard("Hide Out Messages");

        /* Create city map plan. */
        CityPlan plan = null;
        try {  plan = new CityPlan(args[0]);  }
        catch (Exception ex) { System.out.println(ex.getMessage());  exit(1); }

        /* Create and fill viewer. */
        TextBoardViewer viewer = createAndFillViewer(plan);
        plan.setViewer(viewer);

        /* Create entities (police, banks, burglers, station, hideOut)*/
        plan.createEntitiesFromMap();

        /* Start entities. */
        plan.launchThreads();

        System.out.println("Simulation over.");
    }

    private static TextBoardViewer createAndFillViewer(CityPlan plan)
    {
        TextBoardViewer viewer = ViewerFactory.create("ThiefCatch!", plan.numberOfLines(), plan.numberOfColumns());
        for(int l = 1; l <= plan.numberOfLines(); l++)
          for(int c = 1; c <= plan.numberOfColumns(); c++)
            viewer.setCell(l, c, plan.getViewerImageRepresentation(plan.get(l, c)));

        return viewer;
    }
}
