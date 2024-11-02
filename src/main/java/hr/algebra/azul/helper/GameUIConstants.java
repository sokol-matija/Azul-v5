package hr.algebra.azul.helper;

import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public final class GameUIConstants {
    // Tile appearance constants
    public static final double TILE_RADIUS = 15.0;
    public static final String EMPTY_SPACE_COLOR = "#374151";
    public static final String TILE_BORDER_COLOR = "#4B5563";
    public static final double TILE_STROKE_WIDTH = 1.0;

    private GameUIConstants() {} // Prevent instantiation

    // Utility method to create base circle with common properties
    public static Circle createBaseCircle() {
        Circle circle = new Circle(TILE_RADIUS);
        circle.setStroke(Color.web(TILE_BORDER_COLOR));
        circle.setStrokeWidth(TILE_STROKE_WIDTH);
        return circle;
    }

    // Utility method to add shadow effect
    public static InnerShadow createTileShadow() {
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setRadius(2);
        innerShadow.setColor(Color.rgb(0, 0, 0, 0.3));
        return innerShadow;
    }
}