package hr.algebra.azul.events.payloads;

import hr.algebra.azul.models.Tile;

import java.util.List;

public record TileSelectionPayload(
        int factoryIndex,
        List<Tile> selectedTiles,
        List<Tile> remainingTiles
) {}
