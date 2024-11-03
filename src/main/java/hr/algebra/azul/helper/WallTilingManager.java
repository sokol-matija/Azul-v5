package hr.algebra.azul.helper;

import hr.algebra.azul.models.*;
import hr.algebra.azul.view.ModernTwoPlayerGameView;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.List;

public class WallTilingManager {
    private final ModernTwoPlayerGameView view;
    private final GameModel gameModel;

    public WallTilingManager(ModernTwoPlayerGameView view, GameModel gameModel) {
        this.view = view;
        this.gameModel = gameModel;
    }

    public void processWallTiling() {
        // Process each player's pattern lines
        for (Player player : gameModel.getPlayers()) {
            List<PatternLine> patternLines = player.getPatternLines();
            boolean wallChanged = false;

            // Process each pattern line
            for (int i = 0; i < patternLines.size(); i++) {
                PatternLine line = patternLines.get(i);

                // Only process complete lines
                if (line.isFull()) {
                    TileColor color = line.getColor();
                    if (color != null) {
                        // Add tile to wall and get points
                        int points = player.getWall().addTile(i, color);
                        player.addScore(points);
                        wallChanged = true;

                        // Clear the pattern line after moving tile to wall
                        line.clear();

                        // Show animation and update UI
                        animateWallTiling(player, i, color);
                    }
                }
            }

            // Update the wall display if changes were made
            if (wallChanged) {
                updateWallDisplay(player);
            }

            // Process floor line penalties after wall tiling
            int penalty = player.getFloorLine().calculatePenalty();
            if (penalty != 0) {
                player.addScore(penalty);
                player.getFloorLine().clear();
                updateFloorLineDisplay(player);
            }
        }

        // Check for completed rows and update display
        checkAndHighlightCompletedRows();
    }

    private void animateWallTiling(Player player, int row, TileColor color) {
        Platform.runLater(() -> {
            VBox playerBoard = (player == gameModel.getPlayers().get(0)) ?
                    view.getPlayer1Board() : view.getPlayer2Board();

            // Find the wall grid in the player board
            GridPane wall = (GridPane) playerBoard.getChildren().stream()
                    .filter(node -> node instanceof GridPane)
                    .findFirst()
                    .orElse(null);

            if (wall != null) {
                // Find the correct column based on the wall pattern
                int col = findColumnForColor(player.getWall().getWallPattern()[row], color);

                // Get the tile space at the correct position
                StackPane tileSpace = (StackPane) wall.getChildren().get(row * 5 + col);

                // Create and play the animation
                Circle colorCircle = (Circle) tileSpace.getChildren().get(2);

                // Scale and fade animation
                ScaleTransition scale = new ScaleTransition(Duration.millis(300), colorCircle);
                scale.setFromX(0.5);
                scale.setFromY(0.5);
                scale.setToX(1.0);
                scale.setToY(1.0);

                FadeTransition fade = new FadeTransition(Duration.millis(300), colorCircle);
                fade.setFromValue(0.5);
                fade.setToValue(1.0);

                ParallelTransition animation = new ParallelTransition(scale, fade);
                animation.play();

                // Update the tile appearance
                colorCircle.setFill(Color.web(color.getHexCode()));
                colorCircle.setOpacity(1.0);
            }
        });
    }

    private void updateWallDisplay(Player player) {
        VBox playerBoard = (player == gameModel.getPlayers().get(0)) ?
                view.getPlayer1Board() : view.getPlayer2Board();

        GridPane wall = (GridPane) playerBoard.getChildren().stream()
                .filter(node -> node instanceof GridPane)
                .findFirst()
                .orElse(null);

        if (wall != null) {
            boolean[][] tiles = player.getWall().getTiles();
            TileColor[][] pattern = player.getWall().getWallPattern();

            for (int row = 0; row < Wall.WALL_SIZE; row++) {
                for (int col = 0; col < Wall.WALL_SIZE; col++) {
                    StackPane space = (StackPane) wall.getChildren().get(row * 5 + col);
                    Circle colorCircle = (Circle) space.getChildren().get(2);

                    if (tiles[row][col]) {
                        colorCircle.setFill(Color.web(pattern[row][col].getHexCode()));
                        colorCircle.setOpacity(1.0);
                    }
                }
            }
        }
    }

    private void checkAndHighlightCompletedRows() {
        for (Player player : gameModel.getPlayers()) {
            Wall wall = player.getWall();

            for (int row = 0; row < Wall.WALL_SIZE; row++) {
                if (wall.isRowComplete(row)) {
                    highlightCompletedRow(player, row);
                }
            }
        }
    }

    private void highlightCompletedRow(Player player, int row) {
        VBox playerBoard = (player == gameModel.getPlayers().get(0)) ?
                view.getPlayer1Board() : view.getPlayer2Board();

        GridPane wall = (GridPane) playerBoard.getChildren().stream()
                .filter(node -> node instanceof GridPane)
                .findFirst()
                .orElse(null);

        if (wall != null) {
            for (int col = 0; col < Wall.WALL_SIZE; col++) {
                StackPane space = (StackPane) wall.getChildren().get(row * 5 + col);

                // Add glow effect to completed row
                DropShadow glow = new DropShadow();
                glow.setColor(Color.GOLD);
                glow.setRadius(10);
                space.setEffect(glow);

                // Add subtle animation
                Timeline pulse = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 10)),
                        new KeyFrame(Duration.seconds(1), new KeyValue(glow.radiusProperty(), 15))
                );
                pulse.setCycleCount(Timeline.INDEFINITE);
                pulse.setAutoReverse(true);
                pulse.play();
            }
        }
    }

    private int findColumnForColor(TileColor[] rowPattern, TileColor color) {
        for (int i = 0; i < rowPattern.length; i++) {
            if (rowPattern[i] == color) {
                return i;
            }
        }
        return -1;
    }

    private void updateFloorLineDisplay(Player player) {
        VBox playerBoard = (player == gameModel.getPlayers().get(0)) ?
                view.getPlayer1Board() : view.getPlayer2Board();

        HBox floorLine = (HBox) playerBoard.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .reduce((first, second) -> second)
                .orElse(null);

        if (floorLine != null) {
            floorLine.getChildren().clear();
            // Recreate empty floor line spaces
            for (int i = 0; i < FloorLine.MAX_TILES; i++) {
                Circle space = new Circle(15);
                space.setFill(Color.web("#374151")); // Empty space color
                space.setStroke(Color.web("#4B5563"));
                space.setStrokeWidth(1);
                floorLine.getChildren().add(space);
            }
        }
    }
}