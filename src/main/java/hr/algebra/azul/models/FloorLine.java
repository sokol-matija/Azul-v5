package hr.algebra.azul.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FloorLine {
    private final List<Tile> tiles;
    public static final int[] PENALTY_POINTS = {-1, -1, -2, -2, -2, -3, -3};
    public static final int MAX_TILES = 7;

    public FloorLine() {
        this.tiles = new ArrayList<>();
    }

    public void addTiles(List<Tile> newTiles) {
        // Only add tiles up to the maximum capacity
        int remainingSpace = MAX_TILES - tiles.size();
        int tilesToAdd = Math.min(remainingSpace, newTiles.size());

        if (tilesToAdd > 0) {
            tiles.addAll(newTiles.subList(0, tilesToAdd));
        }
    }

    public void addTile(Tile tile) {
        if (tiles.size() < MAX_TILES) {
            tiles.add(tile);
        }
    }

    public int calculatePenalty() {
        int penalty = 0;
        for (int i = 0; i < Math.min(tiles.size(), PENALTY_POINTS.length); i++) {
            penalty += PENALTY_POINTS[i];
        }
        return penalty;
    }

    public void clear() {
        tiles.clear();
    }

    public boolean isFull() {
        return tiles.size() >= MAX_TILES;
    }

    public int getSize() {
        return tiles.size();
    }

    // Return an unmodifiable view of the tiles
    public List<Tile> getTiles() {
        return Collections.unmodifiableList(tiles);
    }

    // Method to get a specific tile at an index
    public Tile getTileAt(int index) {
        if (index >= 0 && index < tiles.size()) {
            return tiles.get(index);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FloorLine: ");
        for (Tile tile : tiles) {
            sb.append(tile.getColor() != null ? tile.getColor().toString().charAt(0) : "F").append(" ");
        }
        return sb.toString();
    }
}