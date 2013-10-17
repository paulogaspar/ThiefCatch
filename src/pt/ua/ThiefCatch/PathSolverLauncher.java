package pt.ua.ThiefCatch;

/**
 *
 * @author Paulo
 */
public class PathSolverLauncher
{
        private Trail chosenPath;
        private static int numberOfGroups = 0;

        public PathSolverLauncher(Position origin, Position destiny, CityPlan plan)
        {
            Trail foundPath = new Trail(origin);

            /* Create thread group. */
            ThreadGroup group = new ThreadGroup("Group " + numberOfGroups++);

            /* Create path solver. */
            PathSolver solver = new PathSolver(plan, new Trail(origin), origin, Movement.Direction.Left, destiny, foundPath, createBoardMap(plan), group);

            /* Launch solver. */
            Thread thread = new Thread(group, solver);

            thread.start();
            //solver.start();

            //System.out.println("Calculating path!");
            //System.out.println();

            while(!solver.wasThePathFound()) { solver.waitForPathToBeReady(); }

            //chosenPath = null;
            //if (foundPath.isConcluded())
                chosenPath = foundPath;
        }


        public Trail getChosenPath()
        {
            return chosenPath;
        }

        public char [][] createBoardMap(CityPlan plan)
        {
            char [][] boardMap = new char[plan.numberOfLines()][plan.numberOfColumns()];
            char [][] matrix = plan.getMatrix();

            for (int i=0; i<plan.numberOfLines(); i++)
                for (int j=0; j<plan.numberOfColumns(); j++)
                    boardMap[i][j] = matrix[i][j];

            return boardMap;
        }


}
