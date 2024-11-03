package hr.algebra.azul.models;

import java.util.Arrays;

public class Wall {
    private final boolean[][] tiles;
    private final TileColor[][] wallPattern;
    public static final int WALL_SIZE = 5;

    public Wall() {
        this.tiles = new boolean[WALL_SIZE][WALL_SIZE];
        this.wallPattern = initializeWallPattern();
    }

    public TileColor[][] initializeWallPattern() {
        return new TileColor[][] {
                {TileColor.BLUE, TileColor.YELLOW, TileColor.RED, TileColor.BLACK, TileColor.WHITE},
                {TileColor.WHITE, TileColor.BLUE, TileColor.YELLOW, TileColor.RED, TileColor.BLACK},
                {TileColor.BLACK, TileColor.WHITE, TileColor.BLUE, TileColor.YELLOW, TileColor.RED},
                {TileColor.RED, TileColor.BLACK, TileColor.WHITE, TileColor.BLUE, TileColor.YELLOW},
                {TileColor.YELLOW, TileColor.RED, TileColor.BLACK, TileColor.WHITE, TileColor.BLUE}
        };
    }

    public boolean hasColor(int row, TileColor color) {
        if (row < 0 || row >= WALL_SIZE) {
            return false;
        }

        // Check if this color is already placed in this row
        for (int col = 0; col < WALL_SIZE; col++) {
            if (tiles[row][col] && wallPattern[row][col] == color) {
                return true;
            }
        }
        return false;
    }

    public int addTile(int row, TileColor color) {
        if (row < 0 || row >= WALL_SIZE) {
            return 0;
        }

        // Find the correct column for this color in the given row
        int col = -1;
        for (int j = 0; j < WALL_SIZE; j++) {
            if (wallPattern[row][j] == color && !tiles[row][j]) {
                col = j;
                break;
            }
        }

        if (col == -1) {
            return 0; // Color can't be placed in this row
        }

        // Place the tile
        tiles[row][col] = true;

        // Calculate points for this placement
        return calculatePoints(row, col);
    }

    private int calculatePoints(int row, int col) {
        int points = 0;
        boolean hasHorizontalNeighbor = false;
        boolean hasVerticalNeighbor = false;

        // Check horizontal line (left and right)
        if (col > 0 && tiles[row][col - 1] || col < WALL_SIZE - 1 && tiles[row][col + 1]) {
            hasHorizontalNeighbor = true;
            points++;

            // Count all connected horizontal tiles
            int leftCount = 0;
            for (int c = col - 1; c >= 0; c--) {
                if (tiles[row][c]) leftCount++;
                else break;
            }
            int rightCount = 0;
            for (int c = col + 1; c < WALL_SIZE; c++) {
                if (tiles[row][c]) rightCount++;
                else break;
            }

            // Add points for complete horizontal sets
            if (leftCount + rightCount + 1 == WALL_SIZE) {
                points += 2; // Bonus for completing a row
            }
        }

        // Check vertical line (up and down)
        if (row > 0 && tiles[row - 1][col] || row < WALL_SIZE - 1 && tiles[row + 1][col]) {
            hasVerticalNeighbor = true;
            points++;

            // Count all connected vertical tiles
            int upCount = 0;
            for (int r = row - 1; r >= 0; r--) {
                if (tiles[r][col]) upCount++;
                else break;
            }
            int downCount = 0;
            for (int r = row + 1; r < WALL_SIZE; r++) {
                if (tiles[r][col]) downCount++;
                else break;
            }

            // Add points for complete vertical sets
            if (upCount + downCount + 1 == WALL_SIZE) {
                points += 7; // Bonus for completing a column
            }
        }

        // If tile is isolated (no neighbors), score 1 point
        if (!hasHorizontalNeighbor && !hasVerticalNeighbor) {
            points = 1;
        }

        return points;
    }

    public boolean isRowComplete(int row) {
        if (row < 0 || row >= WALL_SIZE) {
            return false;
        }
        for (int col = 0; col < WALL_SIZE; col++) {
            if (!tiles[row][col]) {
                return false;
            }
        }
        return true;
    }

    public boolean isColumnComplete(int col) {
        if (col < 0 || col >= WALL_SIZE) {
            return false;
        }
        for (int row = 0; row < WALL_SIZE; row++) {
            if (!tiles[row][col]) {
                return false;
            }
        }
        return true;
    }

    public boolean isColorComplete(TileColor color) {
        int count = 0;
        for (int row = 0; row < WALL_SIZE; row++) {
            for (int col = 0; col < WALL_SIZE; col++) {
                if (tiles[row][col] && wallPattern[row][col] == color) {
                    count++;
                }
            }
        }
        return count == WALL_SIZE;
    }

    // Getters for arrays (defensive copies)
    public boolean[][] getTiles() {
        boolean[][] copy = new boolean[WALL_SIZE][WALL_SIZE];
        for (int i = 0; i < WALL_SIZE; i++) {
            copy[i] = Arrays.copyOf(tiles[i], WALL_SIZE);
        }
        return copy;
    }

    public TileColor[][] getWallPattern() {
        TileColor[][] copy = new TileColor[WALL_SIZE][WALL_SIZE];
        for (int i = 0; i < WALL_SIZE; i++) {
            copy[i] = Arrays.copyOf(wallPattern[i], WALL_SIZE);
        }
        return copy;
    }

    public boolean isTilePlaced(int row, int col) {
        return row >= 0 && row < WALL_SIZE && col >= 0 && col < WALL_SIZE && tiles[row][col];
    }

    // For debugging
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < WALL_SIZE; i++) {
            for (int j = 0; j < WALL_SIZE; j++) {
                if (tiles[i][j]) {
                    sb.append(wallPattern[i][j].toString().charAt(0));
                } else {
                    sb.append('.');
                }
                sb.append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}