package hr.algebra.azul.helper;

import hr.algebra.azul.models.*;
import hr.algebra.azul.view.ModernTwoPlayerGameView;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class PatternLineInteractionHandler {
    private final ModernTwoPlayerGameView view;
    private final GameModel gameModel;
    private final TurnManager turnManager;

    public PatternLineInteractionHandler(ModernTwoPlayerGameView view, GameModel gameModel, TurnManager turnManager) {
        this.view = view;
        this.gameModel = gameModel;
        this.turnManager = turnManager;
    }

    public void setupPatternLineInteractions() {
        setupPlayerPatternLines(view.getPlayer1Board());
        setupPlayerPatternLines(view.getPlayer2Board());
    }

    private void setupPlayerPatternLines(VBox playerBoard) {
        VBox patternLinesContainer = findPatternLinesContainer(playerBoard);
        if (patternLinesContainer == null) return;

        // Skip the label and get the pattern lines container
        for (int i = 1; i < patternLinesContainer.getChildren().size(); i++) {
            Node node = patternLinesContainer.getChildren().get(i);
            if (node instanceof HBox patternLine) {
                final int lineIndex = i - 1; // Adjust index to account for label
                patternLine.setOnMouseClicked(e -> handlePatternLineClick(lineIndex, playerBoard));
            }
        }
    }

    public void handlePatternLineClick(int lineIndex, VBox playerBoard) {
        // Only handle clicks for the current player's board
        if ((gameModel.getCurrentPlayer() == gameModel.getPlayers().get(0) && playerBoard != view.getPlayer1Board()) ||
                (gameModel.getCurrentPlayer() == gameModel.getPlayers().get(1) && playerBoard != view.getPlayer2Board())) {
            return;
        }

        HBox playerHand = getCurrentPlayerHand();
        if (playerHand.getChildren().isEmpty()) {
            return; // No tiles selected
        }

        // Get the first tile's color from the hand (all tiles should be same color)
        TileColor selectedColor = null;
        List<Tile> selectedTiles = new ArrayList<>();

        for (Node node : playerHand.getChildren()) {
            if (node instanceof Circle circle) {
                Color fillColor = (Color) circle.getFill();
                TileColor tileColor = getTileColorFromFill(fillColor);
                if (tileColor != null) {
                    selectedColor = tileColor;
                    selectedTiles.add(new Tile(tileColor));
                }
            }
        }

        if (selectedTiles.isEmpty() || selectedColor == null) return;

        // Check if placement is valid
        PatternLine targetLine = gameModel.getCurrentPlayer().getPatternLines().get(lineIndex);
        if (!isValidPlacement(targetLine, selectedColor, lineIndex)) {
            return;
        }

        // Animate and place tiles
        animateTilesToPatternLine(playerHand, lineIndex, selectedTiles, playerBoard);
    }

    private boolean isValidPlacement(PatternLine targetLine, TileColor color, int lineIndex) {
        // Check if line already has different color
        if (!targetLine.isEmpty() && targetLine.getColor() != color) {
            return false;
        }

        // Check if the wall already has this color in the corresponding row
        Wall wall = gameModel.getCurrentPlayer().getWall();
        if (wall.hasColor(lineIndex, color)) {
            return false;
        }

        // Check if line is full
        return !targetLine.isFull();
    }

    private void animateTilesToPatternLine(HBox hand, int lineIndex, List<Tile> tiles, VBox playerBoard) {
        VBox patternLinesContainer = findPatternLinesContainer(playerBoard);
        if (patternLinesContainer == null) return;

        HBox targetLine = (HBox) patternLinesContainer.getChildren().get(lineIndex + 1);

        ParallelTransition allAnimations = new ParallelTransition();

        for (Node tileNode : hand.getChildren()) {
            PathTransition path = createPathTransition(hand, targetLine, tileNode);
            allAnimations.getChildren().add(path);
        }

        allAnimations.setOnFinished(e -> {
            // Clear the hand
            hand.getChildren().clear();

            // Add tiles to pattern line in the model
            PatternLine patternLine = gameModel.getCurrentPlayer().getPatternLines().get(lineIndex);
            patternLine.addTiles(tiles);

            // Handle overflow if any
            List<Tile> overflow = calculateOverflow(tiles, patternLine);
            if (!overflow.isEmpty()) {
                gameModel.getCurrentPlayer().getFloorLine().addTiles(overflow);
            }

            // Update the view
            updatePatternLines();

            // End turn after successful placement
            Platform.runLater(() -> {
                PauseTransition pause = new PauseTransition(Duration.millis(300));
                pause.setOnFinished(event -> turnManager.handleEndTurn());
                pause.play();
            });
        });

        allAnimations.play();
    }

    private PathTransition createPathTransition(Node source, Node target, Node movingNode) {
        Bounds sourceBounds = source.localToScene(source.getBoundsInLocal());
        Bounds targetBounds = target.localToScene(target.getBoundsInLocal());

        Path path = new Path();
        path.getElements().add(new MoveTo(
                sourceBounds.getCenterX(),
                sourceBounds.getCenterY()
        ));
        path.getElements().add(new LineTo(
                targetBounds.getCenterX(),
                targetBounds.getCenterY()
        ));

        PathTransition transition = new PathTransition(Duration.millis(500), path, movingNode);
        transition.setAutoReverse(false);

        return transition;
    }

    private List<Tile> calculateOverflow(List<Tile> tiles, PatternLine targetLine) {
        List<Tile> overflow = new ArrayList<>();
        int availableSpace = targetLine.getSize() - targetLine.getTiles().size();

        if (tiles.size() > availableSpace) {
            overflow.addAll(tiles.subList(availableSpace, tiles.size()));
        }

        return overflow;
    }

    public VBox findPatternLinesContainer(VBox playerBoard) {
        return (VBox) playerBoard.getChildren().stream()
                .filter(node -> node instanceof VBox)
                .findFirst()
                .orElse(null);
    }

    private HBox getCurrentPlayerHand() {
        return gameModel.getCurrentPlayer() == gameModel.getPlayers().get(0)
                ? view.getPlayer1Hand()
                : view.getPlayer2Hand();
    }

    private void updatePatternLines() {
        Platform.runLater(() -> {
            updatePlayerPatternLines(view.getPlayer1Board(), gameModel.getPlayers().get(0));
            updatePlayerPatternLines(view.getPlayer2Board(), gameModel.getPlayers().get(1));
        });
    }

    private void updatePlayerPatternLines(VBox playerBoard, Player player) {
        VBox patternLinesContainer = findPatternLinesContainer(playerBoard);
        if (patternLinesContainer == null) return;

        List<PatternLine> playerPatternLines = player.getPatternLines();

        // Start from index 1 to skip the label
        for (int i = 1; i <= playerPatternLines.size(); i++) {
            HBox lineContainer = (HBox) patternLinesContainer.getChildren().get(i);
            PatternLine patternLine = playerPatternLines.get(i - 1);
            updateSinglePatternLine(lineContainer, patternLine);
        }
    }

    private void updateSinglePatternLine(HBox lineContainer, PatternLine patternLine) {
        List<Tile> tiles = patternLine.getTiles();
        for (int i = 0; i < lineContainer.getChildren().size(); i++) {
            Circle space = (Circle) lineContainer.getChildren().get(i);
            if (i < tiles.size()) {
                space.setFill(Color.web(tiles.get(i).getColor().getHexCode()));
            } else {
                space.setFill(Color.web("#374151")); // Empty space color
            }
        }
    }

    private TileColor getTileColorFromFill(Color fillColor) {
        String hexColor = String.format("#%02X%02X%02X",
                (int) (fillColor.getRed() * 255),
                (int) (fillColor.getGreen() * 255),
                (int) (fillColor.getBlue() * 255));

        for (TileColor color : TileColor.values()) {
            if (color.getHexCode().equalsIgnoreCase(hexColor)) {
                return color;
            }
        }
        return null;
    }
}