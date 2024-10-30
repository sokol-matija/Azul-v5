package hr.algebra.azul.models;

public class Tile {
    private final TileColor color;

    public Tile(TileColor color) {
        this.color = color;
    }

    public TileColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "Tile[" + (color == null ? "First Player" : color.name()) + "]";
    }

    // Since we're replacing a record, we should implement equals and hashCode
    // to maintain the same value-based equality behavior
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile tile = (Tile) o;
        return color == tile.color;
    }

    @Override
    public int hashCode() {
        return color != null ? color.hashCode() : 0;
    }
}