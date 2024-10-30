
package hr.algebra.azul.models;

import java.util.*;

public class TileBag {
    private final Queue<Tile> tiles;

    public TileBag() {
        tiles = new LinkedList<>();
        for (TileColor color : TileColor.values()) {
            for (int i = 0; i < 20; i++) {  // 20 tiles of each color
                tiles.add(new Tile(color));
            }
        }
        // Shuffle tiles
        List<Tile> tileList = new ArrayList<>(tiles);
        Collections.shuffle(tileList);
        tiles.clear();
        tiles.addAll(tileList);
    }

    public Tile draw() {
        return tiles.poll();
    }

    public boolean hasNext() {
        return !tiles.isEmpty();
    }
}
