package hr.algebra.azul.events.payloads;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.layout.VBox;
import hr.algebra.azul.models.Tile;
import java.util.List;

public record FactoryClickPayload(
        int factoryIndex,
        Color tileColor,
        Circle clickedTile,
        VBox factory
) {}

