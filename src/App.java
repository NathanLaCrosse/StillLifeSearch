import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Searches for still lifes in Conway's Game of Life with some new techniques I discovered. This program using matrices to optimize
 * our search compared to last time.
 */
public class App {
    public static int currentIndex, permutations;
    public static boolean complete = false;

    public static void main(String[] args) {
        ProgressThread pt = new ProgressThread();
        pt.start();

        ArrayList<int[]> stillLifes = findAllPossibleStillLifes(3);

        writeArrayListDataToFile(stillLifes);

        complete = true;
        System.out.println("Process Complete!");

    }

    /**
     * Runs through all possible Conway's Game of Life games within a gridDim by gridDim grid and records all still lifes discovered
     * in this process.
     */
    public static ArrayList<int[]> findAllPossibleStillLifes(int gridDim) {
        ArrayList<int[]> foundStillLifes = new ArrayList<>();

        permutations = (int)(Math.pow(2, gridDim * gridDim));

        String[][] map = buildMapMatrix(gridDim);
        int[][] d = buildDMatrix(gridDim);
        int[][] l = buildLMatrix(gridDim);
        int[] empty = new int[gridDim];

        //starts at 1 so we dont get all 0s as a still life
        for(currentIndex = 1; currentIndex < permutations; currentIndex++) {
            int[] state = BinaryHelper.convertNumberToBinaryArray(currentIndex);
            state = BinaryHelper.fitBitArrayToSize(state, gridDim * gridDim);

            int[] nextState = findNextState(state, gridDim, map, d, l, empty);

            if(equivalentArray(state, nextState)) {
                foundStillLifes.add(state);
            }
        }

        //begin the sanitization process, to figure out if our still lifes are still lifes on the open map
        String[][] modMap = buildMapMatrix(gridDim + 2);
        int[][] modD = buildDMatrix(gridDim + 2);
        int[][] modL = buildLMatrix(gridDim + 2);
        int[] modEmpty = new int[gridDim + 2];

        for(int i = 0; i < foundStillLifes.size(); i++) {
            if(!sanitizeStillLife(foundStillLifes.get(i), gridDim, modMap, modD, modL, modEmpty)) {
                foundStillLifes.remove(i);
                i--;
            }
        }

        return foundStillLifes;
    }

    /**
     * Sanitizes the still life by checking what would happen if it was on a n+2 by n+2 grid, instead of the n by n one we evaluated it on.
     * @param state - the state that we are checking to see if it is a proper still life
     * @param originalGridDim - the dimensions of the original grid
     * @param modMap - the map for the new n+2 grid
     * @param modD - the "Diagonal" matrix for the new n+2 grid
     * @param modL - the "Line" matrix for the new n+2 grid
     * @param modEmpty - the "Void" matrix for the new n+2 grid
     * @return - true if the inputted state is a true still life, false if it is only a still life on the closed grid
     */
    private static boolean sanitizeStillLife(int[] state, int originalGridDim, String[][] modMap, int[][] modD, int[][] modL, int[] modEmpty) {
        int newDim = originalGridDim + 2;

        //create a modified state for the new grid size using the old state
        int[] modifiedState = new int[newDim * newDim];
        int stateDex = 0;
        for(int row = 1; row < 1 + originalGridDim; row++) {
            for(int col = 1; col < 1 + originalGridDim; col++) {
                modifiedState[row * newDim + col] = state[stateDex];
                stateDex++;
            }
        }

        int[] modifiedNextState = findNextState(modifiedState, newDim, modMap, modD, modL, modEmpty);
        return equivalentArray(modifiedState, modifiedNextState);
    }

    private static boolean equivalentArray(int[] a1, int[] a2) {
        for(int i = 0; i < a1.length; i++) {
            if(a1[i] != a2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds the next state in using the conditions of Conway's Game of Life. Determines the number of neighbors of each tile and uses that
     * to determine whether the tile stays alive or not.
     * @param startingState - the state we are finding neighbors for
     * @param gridDim - the dimension of the grid we are working with
     * @param map - the "Map" that tells us which matrices to use in specific spots
     * @param d - the "Diagonal" matrix component of the neighbor matrix
     * @param l - the "Line" matrix component of the neighbor matrix
     * @param empty - the "Void" matrix componenet of the neighbor matrix, which is most conviently represented as a blank array
     * @return - the next state of the Conway's Game of Life simulation
     */
    private static int[] findNextState(int[] startingState, int gridDim, String[][] map, int[][] d, int[][] l, int[] empty) {
        int[] neighbors = findNeighborState(startingState, gridDim, map, d, l, empty);
        int[] newState = new int[startingState.length];

        for(int i = 0; i < startingState.length; i++) {
            if(neighbors[i] == 3 || (startingState[i] == 1 && neighbors[i] == 2)) {
                newState[i] = 1;
            }
        }
        return newState;
    }
    /**
     * Finds the state that corresponds to how many neighbors each tile has on the grid. This overload allows for the reuse of
     * the map, "diagonal", "line" and "void" matrix 
     * @param startingState - the state we are finding neighbors for
     * @param gridDim - the dimension of the grid we are working with
     * @param map - the "Map" that tells us which matrices to use in specific spots
     * @param d - the "Diagonal" matrix component of the neighbor matrix
     * @param l - the "Line" matrix component of the neighbor matrix
     * @param empty - the "Void" matrix componenet of the neighbor matrix, which is most conviently represented as a blank array
     * @return - a state the records the number of neighbors for each alive matrix
     */
    private static int[] findNeighborState(int[] startingState, int gridDim, String[][] map, int[][] d, int[][] l, int[] empty) {
        int[] neighborState = new int[startingState.length];

        for(int i = 0; i < startingState.length; i++) {
            //these rows point us to which matrices we are using and where
            int macroRow = i / gridDim;
            int miniRow = i % gridDim;

            //build the row of the neighbor matrix we need
            int[] neighborRow = new int[startingState.length];
            for(int macroCol = 0; macroCol < gridDim; macroCol++) {
                String read = map[macroRow][macroCol];
                int[] readrow;
                if(read.equals("V")) {
                    readrow = empty;
                }else if(read.equals("D")) {
                    readrow = d[miniRow];
                }else {
                    readrow = l[miniRow];
                }

                for(int microCol = 0; microCol < gridDim; microCol++) {
                    neighborRow[macroCol * gridDim + microCol] = readrow[microCol];
                }
            }

            //now with the neighborRow, perform a dot product on it and the starting state to get our neighbor state
            int total = 0;
            for(int k = 0; k < startingState.length; k++) {
                total += startingState[k] * neighborRow[k];
            }
            neighborState[i] = total;
        }

        return neighborState;
    }
    /**
     * Finds the state that corresponds to how many neighbors each tile has on the grid. Overload that requires less inputs, but works slower.
     * @param startingState - the state we are finding the neighbors for
     * @param gridDim - the dimension of the square grid we are using
     * @return - an array containing the number of neighbors for each point on the grid
     */
    private static int[] findNeighborState(int[] startingState, int gridDim) {
        String[][] map = buildMapMatrix(gridDim);
        int[][] d = buildDMatrix(gridDim);
        int[][] l = buildLMatrix(gridDim);
        int[] empty = new int[gridDim];

        return findNeighborState(startingState, gridDim, map, d, l, empty);
    }
    /**
     * Builds a "Map" matrix that represents a simplified version of the whole neighbor matrix. Useful to refer to when figuring out
     * how to build a specific row of the neighbor matrix.
     * @param gridDim - the dimension of the square grid we are making this for
     * @return - the "Map" of the neighbor matrix
     */
    private static String[][] buildMapMatrix(int gridDim) {
        String[][] m = new String[gridDim][gridDim];
        for(int i = 0; i < m.length; i++) {
            for(int k = 0; k < m.length; k++) {
                String result = "V";
                if(i == k) {
                    result = "D";
                }else if(i == k - 1 || i == k + 1) {
                    result = "L";
                }
                m[i][k] = result;
            }
        }
        return m;
    }
    /**
     * Builds the mini "Diagonal" matrix which appears several times inside of the neighbor matrix. This matrix has all 0s except that to
     * the left and right of the diagonal are 1s.
     * @param gridDim - the dimension of the square grid we are making this for
     * @return - the "Diagonal" matrix for the given gridDim
     */
    private static int[][] buildDMatrix(int gridDim) {
        int[][] m = new int[gridDim][gridDim];
        for(int i = 0; i < gridDim; i++) {
            if(i - 1 > -1) {
                m[i][i-1] = 1;
            }
            if(i + 1 < gridDim) {
                m[i][i+1] = 1;
            }
        }
        return m;
    }
    /**
     * Builds the mini "Line" matrix which appears inside the neighbor matrix. This matrix is similar to the "Diagonal" matrix except
     * it has 1s going down the diagonal aswell.
     * @param gridDim
     * @return
     */
    private static int[][] buildLMatrix(int gridDim) {
        int[][] m = new int[gridDim][gridDim];
        for(int i = 0; i < gridDim; i++) {
            if(i - 1 > -1) {
                m[i][i-1] = 1;
            }
            if(i + 1 < gridDim) {
                m[i][i+1] = 1;
            }
            m[i][i] = 1;
        }
        return m;
    }
    /**
     * Prints a given matrix
     * @param m - the matrix to be printed
     */
    private static void printMatrix(int[][] m) {
        for(int i = 0; i < m.length; i++) {
            for(int k = 0; k < m[0].length; k++) {
                System.out.print(m[i][k] + " ");
            }
            System.out.println();
        }
    }
    /**
     * Prints out an array
     * @param ar - the array to be printed
     */
    private static void printArray(int[] ar) {
        for(int i = 0; i < ar.length; i++) {
            System.out.print(ar[i] + " ");
        }
        System.out.println();
    }

    /**
     * Writes an arraylist of int[] to the file data.txt, used to store the results of the still life finding process
     */
    private static void writeArrayListDataToFile(ArrayList<int[]> data) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(new File("data.txt"));
        }catch(FileNotFoundException e) {
            System.out.println("Write failed!");
            return;
        }
        for(int i = 0; i < data.size(); i++) {
            int[] read = data.get(i);
            for(int k = 0; k < read.length; k++) {
                writer.print(read[k] + " ");
            }
            writer.println();
        }
        writer.close();
    }
}
