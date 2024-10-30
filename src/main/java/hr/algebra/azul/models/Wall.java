package hr.algebra.azul.models;

import java.util.Arrays;

public class Wall {
    public final boolean[][] tiles;
    public final TileColor[][] wallPattern;
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

        for (int col = 0; col < WALL_SIZE; col++) {
            if (wallPattern[row][col] == color && !tiles[row][col]) {
                tiles[row][col] = true;
                return calculatePoints(row, col);
            }
        }
        return 0;
    }

    private int calculatePoints(int row, int col) {
        int points = 0;
        boolean hasHorizontalNeighbor = false;
        boolean hasVerticalNeighbor = false;

        // Check horizontal neighbors
        if ((col > 0 && tiles[row][col - 1]) || (col < WALL_SIZE - 1 && tiles[row][col + 1])) {
            hasHorizontalNeighbor = true;
            points++;
        }

        // Check vertical neighbors
        if ((row > 0 && tiles[row - 1][col]) || (row < WALL_SIZE - 1 && tiles[row + 1][col])) {
            hasVerticalNeighbor = true;
            points++;
        }

        // If this tile is isolated, score 1 point
        if (!hasHorizontalNeighbor && !hasVerticalNeighbor) {
            points = 1;
        }

        return points;
    }

    // Returns a deep copy of the tiles array
    public boolean[][] getTiles() {
        boolean[][] copy = new boolean[WALL_SIZE][WALL_SIZE];
        for (int i = 0; i < WALL_SIZE; i++) {
            copy[i] = Arrays.copyOf(tiles[i], WALL_SIZE);
        }
        return copy;
    }

    // Returns a deep copy of the wallPattern array
    public TileColor[][] getWallPattern() {
        TileColor[][] copy = new TileColor[WALL_SIZE][WALL_SIZE];
        for (int i = 0; i < WALL_SIZE; i++) {
            copy[i] = Arrays.copyOf(wallPattern[i], WALL_SIZE);
        }
        return copy;
    }

    // Additional helper methods
    public boolean isTilePlaced(int row, int col) {
        if (row < 0 || row >= WALL_SIZE || col < 0 || col >= WALL_SIZE) {
            return false;
        }
        return tiles[row][col];
    }

    public TileColor getColorAt(int row, int col) {
        if (row < 0 || row >= WALL_SIZE || col < 0 || col >= WALL_SIZE) {
            return null;
        }
        return wallPattern[row][col];
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
        for (int row = 0; row < WALL_SIZE; row++) {
            for (int col = 0; col < WALL_SIZE; col++) {
                if (wallPattern[row][col] == color && !tiles[row][col]) {
                    return false;
                }
            }
        }
        return true;
    }

    // For debugging purposes
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < WALL_SIZE; row++) {
            for (int col = 0; col < WALL_SIZE; col++) {
                if (tiles[row][col]) {
                    sb.append(wallPattern[row][col].toString().charAt(0));
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