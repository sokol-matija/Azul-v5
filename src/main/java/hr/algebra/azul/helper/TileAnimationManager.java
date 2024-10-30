package hr.algebra.azul.helper;

import hr.algebra.azul.models.Tile;
import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
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

        // Add all animated circles to the animation layer
        animationLayer.getChildren().addAll(selectedCircles);
        animationLayer.getChildren().addAll(remainingCircles);

        // Position circles at the factory's location
        Bounds factoryBounds = factoryNode.localToScene(factoryNode.getBoundsInLocal());
        positionCirclesAtSource(selectedCircles, factoryBounds);
        positionCirclesAtSource(remainingCircles, factoryBounds);

        ParallelTransition allAnimations = new ParallelTransition();

        // Animate selected tiles to hand
        addTileAnimations(allAnimations, selectedCircles, factoryNode, targetHand);

        // Animate remaining tiles to center
        addTileAnimations(allAnimations, remainingCircles, factoryNode, centerPool);

        // Clean up after animation completes
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

    private void positionCirclesAtSource(List<Circle> circles, Bounds sourceBounds) {
        double centerX = sourceBounds.getCenterX();
        double centerY = sourceBounds.getCenterY();

        // Arrange circles in a grid pattern
        int size = (int) Math.ceil(Math.sqrt(circles.size()));
        for (int i = 0; i < circles.size(); i++) {
            Circle circle = circles.get(i);
            int row = i / size;
            int col = i % size;

            // Convert scene coordinates to animation layer coordinates
            Point2D point = animationLayer.sceneToLocal(
                    centerX + (col - size/2.0) * 35,
                    centerY + (row - size/2.0) * 35
            );

            circle.setCenterX(point.getX());
            circle.setCenterY(point.getY());
        }
    }

    private void addTileAnimations(ParallelTransition parallel, List<Circle> circles,
                                   Node source, Node target) {
        Bounds sourceBounds = source.localToScene(source.getBoundsInLocal());
        Bounds targetBounds = target.localToScene(target.getBoundsInLocal());

        for (Circle circle : circles) {
            // Create path from source to target
            Point2D start = animationLayer.sceneToLocal(
                    sourceBounds.getCenterX(),
                    sourceBounds.getCenterY()
            );
            Point2D end = animationLayer.sceneToLocal(
                    targetBounds.getCenterX(),
                    targetBounds.getCenterY()
            );

            Path path = new Path();
            path.getElements().addAll(
                    new MoveTo(start.getX(), start.getY()),
                    new LineTo(end.getX(), end.getY())
            );

            // Create and configure path transition
            PathTransition pathTransition = new PathTransition(ANIMATION_DURATION, path, circle);
            pathTransition.setInterpolator(Interpolator.EASE_BOTH);

            // Add fade and scale effects
            FadeTransition fade = new FadeTransition(ANIMATION_DURATION, circle);
            fade.setFromValue(1.0);
            fade.setToValue(0.8);

            ScaleTransition scale = new ScaleTransition(ANIMATION_DURATION, circle);
            scale.setFromX(1.0);
            scale.setFromY(1.0);
            scale.setToX(0.8);
            scale.setToY(0.8);

            // Combine all effects
            ParallelTransition tileAnimation = new ParallelTransition(
                    circle,
                    pathTransition,
                    fade,
                    scale
            );

            parallel.getChildren().add(tileAnimation);
        }
    }
}