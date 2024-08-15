package tides;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * This class contains methods that provide information about select terrains 
 * using 2D arrays. Uses floodfill to flood given maps and uses that 
 * information to understand the potential impacts. 
 * Instance Variables:
 *  - a double array for all the heights for each cell
 *  - a GridLocation array for the sources of water on empty terrain 
 * 
 * @author Original Creator Keith Scharz (NIFTY STANFORD) 
 * @author Vian Miranda (Rutgers University)
 */
public class RisingTides {

    // Instance variables
    private double[][] terrain;     // an array for all the heights for each cell
    private GridLocation[] sources; // an array for the sources of water on empty terrain 

    /**
     * DO NOT EDIT!
     * Constructor for RisingTides.
     * @param terrain passes in the selected terrain 
     */
    public RisingTides(Terrain terrain) {
        this.terrain = terrain.heights;
        this.sources = terrain.sources;
    }

    /**
     * Find the lowest and highest point of the terrain and output it.
     * 
     * @return double[], with index 0 and index 1 being the lowest and 
     * highest points of the terrain, respectively
     */
    public double[] elevationExtrema() {
        double minElevation = Double.MAX_VALUE;
        double maxElevation = Double.MIN_VALUE;

        for (int i = 0; i < terrain.length; i++) {
            for (int j = 0; j < terrain[i].length; j++) {
                if (terrain[i][j] < minElevation) {
                    minElevation = terrain[i][j];
                }
                if (terrain[i][j] > maxElevation) {
                    maxElevation = terrain[i][j];
                }
            }
        }

        return new double[]{minElevation, maxElevation};
    }
    /**
     * Implement the floodfill algorithm using the provided terrain and sources.
     * 
     * All water originates from the source GridLocation. If the height of the 
     * water is greater than that of the neighboring terrain, flood the cells. 
     * Repeat iteratively till the neighboring terrain is higher than the water 
     * height.
     * 
     * 
     * @param height of the water
     * @return boolean[][], where flooded cells are true, otherwise false
     */
    public boolean[][] floodedRegionsIn(double height) {
        int rows = terrain.length;
        int cols = terrain[0].length;
        boolean[][] result = new boolean[rows][cols];
        Queue<GridLocation> queue = new LinkedList<>();

        for (GridLocation source : sources) {
            queue.add(source);
            result[source.row][source.col] = true;
        }

        int[] dRow = {-1, 1, 0, 0};
        int[] dCol = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            GridLocation current = queue.poll();
            int currentRow = current.row;
            int currentCol = current.col;

            for (int i = 0; i < 4; i++) {
                int newRow = currentRow + dRow[i];
                int newCol = currentCol + dCol[i];

                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                    if (!result[newRow][newCol] && terrain[newRow][newCol] <= height) {
                        result[newRow][newCol] = true;
                        queue.add(new GridLocation(newRow, newCol));
                    }
                }
            }
        }

        return result;
    }
    /**
     * Checks if a given cell is flooded at a certain water height.
     * 
     * @param height of the water
     * @param cell location 
     * @return boolean, true if cell is flooded, otherwise false
     */
    public boolean isFlooded(double height, GridLocation cell) {
        boolean[][] floodedRegions = floodedRegionsIn(height);
        return floodedRegions[cell.row][cell.col];
    }

    /**
     * Given the water height and a GridLocation find the difference between 
     * the chosen cells height and the water height.
     * 
     * If the return value is negative, the Driver will display "meters below"
     * If the return value is positive, the Driver will display "meters above"
     * The value displayed will be positive.
     * 
     * @param height of the water
     * @param cell location
     * @return double, representing how high/deep a cell is above/below water
     */
    public double heightAboveWater(double height, GridLocation cell) {
        return terrain[cell.row][cell.col] - height;
    }

    /**
     * Total land available (not underwater) given a certain water height.
     * 
     * @param height of the water
     * @return int, representing every cell above water
     */
    public int totalVisibleLand(double height) {
        boolean[][] floodedRegions = floodedRegionsIn(height);
        int visibleLandCount = 0;

        for (int i = 0; i < floodedRegions.length; i++) {
            for (int j = 0; j < floodedRegions[i].length; j++) {
                if (!floodedRegions[i][j]) {
                    visibleLandCount++;
                }
            }
        }

        return visibleLandCount;
    }


    /**
     * Given 2 heights, find the difference in land available at each height. 
     * 
     * If the return value is negative, the Driver will display "Will gain"
     * If the return value is positive, the Driver will display "Will lose"
     * The value displayed will be positive.
     * 
     * @param height of the water
     * @param newHeight the future height of the water
     * @return int, representing the amount of land lost or gained
     */
    public int landLost(double height, double newHeight) {
        int currentVisibleLand = totalVisibleLand(height);
        int futureVisibleLand = totalVisibleLand(newHeight);
        return currentVisibleLand - futureVisibleLand;
    }

    /**
     * Count the total number of islands on the flooded terrain.
     * 
     * Parts of the terrain are considered "islands" if they are completely 
     * surround by water in all 8-directions. Should there be a direction (ie. 
     * left corner) where a certain piece of land is connected to another 
     * landmass, this should be considered as one island. A better example 
     * would be if there were two landmasses connected by one cell. Although 
     * seemingly two islands, after further inspection it should be realized 
     * this is one single island. Only if this connection were to be removed 
     * (height of water increased) should these two landmasses be considered 
     * two separate islands.
     * 
     * @param height of the water
     * @return int, representing the total number of islands
     */    public int numOfIslands(double height) {
        int rows = terrain.length;
        int cols = terrain[0].length;
        WeightedQuickUnionUF uf = new WeightedQuickUnionUF(rows, cols);
        boolean[][] floodedRegions = floodedRegionsIn(height);

        int[] dRow = {-1, -1, -1, 0, 1, 1, 1, 0};
        int[] dCol = {-1, 0, 1, 1, 1, 0, -1, -1};

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (!floodedRegions[row][col]) {
                    for (int i = 0; i < 8; i++) {
                        int newRow = row + dRow[i];
                        int newCol = col + dCol[i];

                        if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols && !floodedRegions[newRow][newCol]) {
                            uf.union(new GridLocation(row, col), new GridLocation(newRow, newCol));
                        }
                    }
                }
            }
        }

        Set<GridLocation> uniqueIslands = new HashSet<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (!floodedRegions[row][col]) {
                    uniqueIslands.add(uf.find(new GridLocation(row, col)));
                }
            }
        }

        return uniqueIslands.size();
    }
}
