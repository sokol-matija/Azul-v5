package hr.algebra.azul.view.components;

import hr.algebra.azul.models.Factory;
import hr.algebra.azul.models.Tile;
import hr.algebra.azul.models.TileColor;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class FactoryComponent extends VBox {
    private final Factory factory;
    private final GridPane tilesGrid;
    private final List<Circle> tileCircles;
    private FactorySelectionHandler selectionHandler;

    // Style constants
    private static final String FACTORY_BG = "#1F2937";
    private static final String BORDER_COLOR = "#374151";
    private static final String HOVER_BORDER = "#60A5FA";
    private static final double TILE_RADIUS = 15.0;
    private static final double FACTORY_SIZE = 120.0;

    public interface FactorySelectionHandler {
        void onTileSelected(Factory factory, TileColor color, Circle clickedCircle);
    }

    public FactoryComponent(Factory factory) {
        this.factory = factory;
        this.tilesGrid = new GridPane();
        this.tileCircles = new ArrayList<>();

        setupLayout();
        createTileCircles();
    }

    private void setupLayout() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(15));
        setPrefSize(FACTORY_SIZE, FACTORY_SIZE);
        setStyle(getDefaultStyle());

        tilesGrid.setHgap(10);
        tilesGrid.setVgap(10);
        tilesGrid.setAlignment(Pos.CENTER);

        Label label = new Label("Factory " + (factory.getIndex() + 1));
        label.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        getChildren().addAll(tilesGrid, label);

        setupHoverEffect();
    }

    private void createTileCircles() {
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                Circle circle = createTileCircle();
                tileCircles.add(circle);
                tilesGrid.add(circle, col, row);
            }
        }
    }

    private Circle createTileCircle() {
        Circle circle = new Circle(TILE_RADIUS);
        circle.setFill(Color.web("#374151")); // Empty tile color
        circle.setStroke(Color.web("#4B5563"));
        circle.setStrokeWidth(1);

        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.3));
        shadow.setRadius(5);
        circle.setEffect(shadow);

        // Add hover and click handlers
        setupTileInteractions(circle);

        return circle;
    }

    private void setupTileInteractions(Circle circle) {
        circle.setOnMouseEntered(e -> {
            if (circle.getFill() != Color.web("#374151")) { // Only if tile has color
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), circle);
                scale.setToX(1.1);
                scale.setToY(1.1);
                scale.play();

                circle.setStroke(Color.web(HOVER_BORDER));
                circle.setStrokeWidth(2);

                DropShadow glow = new DropShadow();
                glow.setColor(((Color)circle.getFill()).deriveColor(0, 1, 1, 0.5));
                glow.setRadius(10);
                circle.setEffect(glow);
            }
        });

        circle.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), circle);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();

            circle.setStroke(Color.web("#4B5563"));
            circle.setStrokeWidth(1);

            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web("#000000", 0.3));
            shadow.setRadius(5);
            circle.setEffect(shadow);
        });

        circle.setOnMouseClicked(e -> handleTileClick(circle));
    }

    private void handleTileClick(Circle circle) {
        if (circle.getFill() != Color.web("#374151") && selectionHandler != null) {
            int index = tileCircles.indexOf(circle);
            List<Tile> tiles = factory.getTiles();
            if (index >= 0 && index < tiles.size()) {
                TileColor color = tiles.get(index).getColor();
                selectionHandler.onTileSelected(factory, color, circle);
            }
        }
    }

    private void setupHoverEffect() {
        setOnMouseEntered(e -> {
            if (!factory.isEmpty()) {
                setStyle(getHoverStyle());
            }
        });

        setOnMouseExited(e -> setStyle(getDefaultStyle()));
    }

    private String getDefaultStyle() {
        return String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 10;
            -fx-border-color: %s;
            -fx-border-radius: 10;
            -fx-border-width: 1;
            """, FACTORY_BG, BORDER_COLOR);
    }

    private String getHoverStyle() {
        return String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 10;
            -fx-border-color: %s;
            -fx-border-radius: 10;
            -fx-border-width: 2;
            -fx-effect: dropshadow(gaussian, rgba(96, 165, 250, 0.4), 10, 0, 0, 0);
            """, FACTORY_BG, HOVER_BORDER);
    }

    public void update() {
        List<Tile> tiles = factory.getTiles();

        // Update all circles
        for (int i = 0; i < tileCircles.size(); i++) {
            Circle circle = tileCircles.get(i);
            if (i < tiles.size()) {
                Tile tile = tiles.get(i);
                circle.setFill(Color.web(tile.getColor().getHexCode()));
            } else {
                circle.setFill(Color.web("#374151")); // Empty tile color
            }
        }

        // Update factory state visual feedback
        setOpacity(factory.isEmpty() ? 0.7 : 1.0);
    }

    public void setOnTileSelected(FactorySelectionHandler handler) {
        this.selectionHandler = handler;
    }

    public void highlight(boolean highlight) {
        setStyle(highlight ?
                String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 10;
                -fx-border-color: #4F46E5;
                -fx-border-radius: 10;
                -fx-border-width: 2;
                """, FACTORY_BG) :
                getDefaultStyle());
    }

    public Factory getFactory() {
        return factory;
    }

    public List<Circle> getTileCircles() {
        return tileCircles;
    }
}