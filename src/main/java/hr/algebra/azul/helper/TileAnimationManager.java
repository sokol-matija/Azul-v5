package hr.algebra.azul.helper;

import hr.algebra.azul.models.Tile;
import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

public class TileAnimationManager {
    private StackPane animationLayer;
    private static final Duration ANIMATION_DURATION = Duration.millis(500);

    public TileAnimationManager(StackPane animationLayer) {
        this.animationLayer = animationLayer;
    }

    public void animateFactorySelection(VBox factoryNode, List<Tile> selectedTiles, List<Tile> remainingTiles,
                                        Node targetHand, Node centerPool, Runnable onComplete) {
        List<Circle> selectedCircles = createTileCircles(selectedTiles);
        List<Circle> remainingCircles = createTileCircles(remainingTiles);

        // Add circles to animation layer
        animationLayer.getChildren().addAll(selectedCircles);
        animationLayer.getChildren().addAll(remainingCircles);

        // Get accurate factory position using local bounds
        Bounds localBounds = factoryNode.getBoundsInLocal();
        Bounds sceneBounds = factoryNode.localToScene(localBounds);

        // Convert scene coordinates to animation layer coordinates
        Point2D factoryPos = animationLayer.sceneToLocal(
                sceneBounds.getMinX() + sceneBounds.getWidth() / 2,
                sceneBounds.getMinY() + sceneBounds.getHeight() / 2
        );

        // Position circles relative to actual factory position
        positionCirclesAtSource(selectedCircles, factoryPos.getX(), factoryPos.getY());
        positionCirclesAtSource(remainingCircles, factoryPos.getX(), factoryPos.getY());

        ParallelTransition allAnimations = new ParallelTransition();

        // Calculate target positions
        Bounds handBounds = targetHand.localToScene(targetHand.getBoundsInLocal());
        Bounds poolBounds = centerPool.localToScene(centerPool.getBoundsInLocal());

        Point2D handPos = animationLayer.sceneToLocal(
                handBounds.getMinX() + handBounds.getWidth() / 2,
                handBounds.getMinY() + handBounds.getHeight() / 2
        );

        Point2D poolPos = animationLayer.sceneToLocal(
                poolBounds.getMinX() + poolBounds.getWidth() / 2,
                poolBounds.getMinY() + poolBounds.getHeight() / 2
        );

        // Create animations for selected tiles
        for (Circle circle : selectedCircles) {
            addSingleTileAnimation(allAnimations, circle,
                    factoryPos.getX(), factoryPos.getY(),
                    handPos.getX(), handPos.getY());
        }

        // Create animations for remaining tiles
        for (Circle circle : remainingCircles) {
            addSingleTileAnimation(allAnimations, circle,
                    factoryPos.getX(), factoryPos.getY(),
                    poolPos.getX(), poolPos.getY());
        }

        // Cleanup after animation
        allAnimations.setOnFinished(e -> {
            animationLayer.getChildren().removeAll(selectedCircles);
            animationLayer.getChildren().removeAll(remainingCircles);
            onComplete.run();
        });

        allAnimations.play();
    }

    private List<Circle> createTileCircles(List<Tile> tiles) {
        return tiles.stream().map(tile -> {
            Circle circle = new Circle(15);
            circle.setFill(Color.web(tile.getColor().getHexCode()));
            circle.setStroke(Color.web("#4B5563"));
            circle.setStrokeWidth(1);
            circle.setMouseTransparent(true);
            return circle;
        }).collect(Collectors.toList());
    }

    private void positionCirclesAtSource(List<Circle> circles, double centerX, double centerY) {
        int size = (int) Math.ceil(Math.sqrt(circles.size()));
        double spacing = 35;

        for (int i = 0; i < circles.size(); i++) {
            Circle circle = circles.get(i);
            int row = i / size;
            int col = i % size;

            // Center the grid around the factory position
            double offsetX = ((size - 1) * spacing) / 2.0;
            double offsetY = ((size - 1) * spacing) / 2.0;

            double x = centerX + (col * spacing) - offsetX;
            double y = centerY + (row * spacing) - offsetY;

            circle.setCenterX(x);
            circle.setCenterY(y);
        }
    }

    private void addSingleTileAnimation(ParallelTransition parallel, Circle circle,
                                        double startX, double startY,
                                        double endX, double endY) {
        // Create path for tile movement
        Path path = new Path();
        path.getElements().addAll(
                new MoveTo(startX, startY),
                new LineTo(endX, endY)
        );

        // Path transition
        PathTransition pathTransition = new PathTransition(ANIMATION_DURATION, path, circle);
        pathTransition.setInterpolator(Interpolator.EASE_BOTH);

        // Additional effects
        FadeTransition fade = new FadeTransition(ANIMATION_DURATION, circle);
        fade.setFromValue(1.0);
        fade.setToValue(0.8);

        ScaleTransition scale = new ScaleTransition(ANIMATION_DURATION, circle);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(0.8);
        scale.setToY(0.8);

        // Combine animations
        ParallelTransition tileAnimation = new ParallelTransition(
                circle,
                pathTransition,
                fade,
                scale
        );

        parallel.getChildren().add(tileAnimation);
    }
}