package hr.algebra.azul.view.components;

import hr.algebra.azul.models.FloorLine;
import hr.algebra.azul.models.Tile;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.ArrayList;
import java.util.List;

public class FloorLineComponent extends HBox {
    private final List<Circle> spaces;
    private static final int FLOOR_SIZE = 7;
    private static final double TILE_RADIUS = 15;

    public FloorLineComponent() {
        setSpacing(5);
        spaces = new ArrayList<>();
        createSpaces();
    }

    private void createSpaces() {
        for (int i = 0; i < FLOOR_SIZE; i++) {
            Circle space = createSpace();
            spaces.add(space);
            getChildren().add(space);
        }
    }

    private Circle createSpace() {
        Circle circle = new Circle(TILE_RADIUS);
        circle.setFill(Color.web("#374151")); // Empty space color
        circle.setStroke(Color.web("#4B5563"));
        circle.setStrokeWidth(1);
        return circle;
    }

    public void update(FloorLine floorLine) {
        for (int i = 0; i < FLOOR_SIZE; i++) {
            Circle space = spaces.get(i);
            Tile tile = floorLine.getTileAt(i);

            if (tile != null && tile.getColor() != null) {
                space.setFill(Color.web(tile.getColor().getHexCode()));
            } else {
                space.setFill(Color.web("#374151")); // Empty space color
            }
        }
    }

    public List<Circle> getSpaces() {
        return spaces;
    }
}