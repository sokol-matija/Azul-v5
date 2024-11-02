package hr.algebra.azul.view.components;

import hr.algebra.azul.models.PatternLine;
import hr.algebra.azul.models.Tile;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatternLineRow extends HBox {
    private final List<Circle> spaces;
    private final int size;
    private static final double TILE_RADIUS = 15;
    private static final String EMPTY_SPACE_COLOR = "#374151";
    private static final String BORDER_COLOR = "#4B5563";

    public PatternLineRow(int size) {
        this.size = size;
        this.spaces = new ArrayList<>();

        setSpacing(5);
        createSpaces();
    }

    private void createSpaces() {
        for (int i = 0; i < size; i++) {
            Circle space = createSpace();
            spaces.add(space);
            getChildren().add(space);
        }
    }

    private Circle createSpace() {
        Circle circle = new Circle(TILE_RADIUS);
        circle.setFill(Color.web(EMPTY_SPACE_COLOR));
        circle.setStroke(Color.web(BORDER_COLOR));
        circle.setStrokeWidth(1);

        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.3));
        shadow.setRadius(5);
        circle.setEffect(shadow);

        return circle;
    }

    public void update(PatternLine patternLine) {
        if (patternLine == null) {
            clearSpaces();
            return;
        }

        List<Tile> tiles = patternLine.getTiles();
        for (int i = 0; i < spaces.size(); i++) {
            Circle space = spaces.get(i);
            if (i < tiles.size()) {
                Tile tile = tiles.get(i);
                updateSpace(space, tile);
            } else {
                resetSpace(space);
            }
        }
    }

    private void updateSpace(Circle space, Tile tile) {
        if (tile != null && tile.getColor() != null) {
            space.setFill(Color.web(tile.getColor().getHexCode()));
            space.setOpacity(1.0);
        } else {
            resetSpace(space);
        }
    }

    private void resetSpace(Circle space) {
        space.setFill(Color.web(EMPTY_SPACE_COLOR));
        space.setOpacity(0.7);
    }

    private void clearSpaces() {
        spaces.forEach(this::resetSpace);
    }

    public void setHighlight(boolean highlight) {
        setStyle(highlight ?
                "-fx-border-color: #22C55E; -fx-border-width: 2; -fx-border-radius: 5; -fx-padding: 2;" :
                "-fx-border-color: transparent; -fx-padding: 4;");
    }

    public List<Circle> getSpaces() {
        return Collections.unmodifiableList(spaces);
    }

    public int getSize() {
        return size;
    }
}
