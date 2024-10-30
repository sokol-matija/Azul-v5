package hr.algebra.azul.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatternLine {
    public final int size;
    public final List<Tile> tiles;

    public PatternLine(int size) {
        this.size = size;
        this.tiles = new ArrayList<>();
    }

    public boolean isFull() {
        return tiles.size() == size;
    }

    public boolean isEmpty() {
        return tiles.isEmpty();
    }

    public TileColor getColor() {
        return isEmpty() ? null : tiles.get(0).getColor();  // Using the record's accessor
    }

    public boolean addTiles(List<Tile> newTiles) {
        if (newTiles.isEmpty()) return false;

        if (isEmpty() || (getColor() == newTiles.get(0).getColor() && !isFull())) {
            int spaceLeft = size - tiles.size();
            int tilesToAdd = Math.min(spaceLeft, newTiles.size());
            tiles.addAll(newTiles.subList(0, tilesToAdd));
            return true;
        }
        return false;
    }

    // Make tiles accessible through a getter that returns an unmodifiable view
    public List<Tile> getTiles() {
        return Collections.unmodifiableList(tiles);
    }

    public int getCurrentSize() {
        return tiles.size();
    }
}