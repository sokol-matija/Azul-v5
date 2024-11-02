package hr.algebra.azul.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Factory implements Serializable {
    private final List<Tile> tiles;
    private final int index;
    private static final int FACTORY_SIZE = 4;

    public Factory(int index) {
        this.index = index;
        this.tiles = new ArrayList<>(FACTORY_SIZE);
    }

    public void addTile(Tile tile) {
        if (tiles.size() < FACTORY_SIZE) {
            tiles.add(tile);
        }
    }

    public List<Tile> getTiles() {
        return new ArrayList<>(tiles);
    }

    public List<Tile> selectTilesByColor(TileColor color) {
        List<Tile> selectedTiles = new ArrayList<>();
        List<Tile> remainingTiles = new ArrayList<>();

        for (Tile tile : tiles) {
            if (tile.getColor() == color) {
                selectedTiles.add(tile);
            } else {
                remainingTiles.add(tile);
            }
        }

        // Clear the factory and keep only non-selected tiles
        tiles.clear();
        tiles.addAll(remainingTiles);

        return selectedTiles;
    }

    public List<Tile> removeRemainingTiles() {
        List<Tile> remaining = new ArrayList<>(tiles);
        tiles.clear();
        return remaining;
    }

    public boolean isEmpty() {
        return tiles.isEmpty();
    }

    public int getIndex() {
        return index;
    }

    public void clear() {
        tiles.clear();
    }

    public void fillFromBag(TileBag bag) {
        while (tiles.size() < FACTORY_SIZE && bag.hasNext()) {
            tiles.add(bag.draw());
        }
    }
}
