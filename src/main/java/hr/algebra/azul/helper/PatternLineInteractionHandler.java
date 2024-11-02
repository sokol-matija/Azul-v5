package hr.algebra.azul.helper;

import hr.algebra.azul.models.*;
import hr.algebra.azul.view.ModernTwoPlayerGameView;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PatternLineInteractionHandler {
    private final ModernTwoPlayerGameView view;
    private final GameModel gameModel;
    private static final String VALID_PLACEMENT = """
            -fx-border-color: #22C55E;
            -fx-border-width: 2;
            -fx-border-radius: 5;
            -fx-padding: 2;
            """;
    private static final String INVALID_PLACEMENT = """
            -fx-border-color: #EF4444;
            -fx-border-width: 2;
            -fx-border-radius: 5;
            -fx-padding: 2;
            """;
    private static final String NO_HIGHLIGHT = "";
    private static final double HIGHLIGHT_SCALE = 1.1;
    private static final double NORMAL_SCALE = 1.0;
    private static final int ANIMATION_DURATION_MS = 100;

    public PatternLineInteractionHandler(ModernTwoPlayerGameView view, GameModel gameModel) {
        this.view = view;
        this.gameModel = gameModel;
    }

    public void setupPatternLineInteractions() {
        if (!hasActiveTiles()) return;

        // Find pattern lines container in current player's board
        VBox patternLinesContainer = findPatternLinesContainer(getCurrentPlayerBoard());
        if (patternLinesContainer == null) return;

        // Get color of tiles in hand
        TileColor selectedColor = getSelectedTileColor();
        if (selectedColor == null) return;

        // Setup interactions for each pattern line
        setupPatternLineHandlers(patternLinesContainer, selectedColor);

        // Disable factory interactions while tiles are in hand
        disableFactoryInteractions();
    }

    private boolean hasActiveTiles() {
        HBox playerHand = getCurrentPlayerHand();
        return playerHand != null && !playerHand.getChildren().isEmpty();
    }

    private VBox getCurrentPlayerBoard() {
        return gameModel.getCurrentPlayer() == gameModel.getPlayers().get(0)
                ? view.getPlayer1Board()
                : view.getPlayer2Board();
    }

    private HBox getCurrentPlayerHand() {
        return gameModel.getCurrentPlayer() == gameModel.getPlayers().get(0)
                ? view.getPlayer1Hand()
                : view.getPlayer2Hand();
    }

    public VBox findPatternLinesContainer(VBox playerBoard) {
        for (Node node : playerBoard.getChildren()) {
            if (node instanceof VBox container) {
                // Look for the VBox that contains the "Pattern Lines" label
                boolean isPatternLinesContainer = container.getChildren().stream()
                        .anyMatch(child -> child instanceof Label &&
                                ((Label) child).getText().equals("Pattern Lines"));
                if (isPatternLinesContainer) {
                    return container;
                }
            }
        }
        return null;
    }

    private void setupPatternLineHandlers(VBox patternLinesContainer, TileColor selectedColor) {
        // Skip the label at index 0
        for (int i = 1; i <= 5; i++) {
            final int lineIndex = i - 1;
            if (patternLinesContainer.getChildren().get(i) instanceof HBox patternLine) {
                setupSinglePatternLineHandler(patternLine, lineIndex, selectedColor);
            }
        }
    }

    private void setupSinglePatternLineHandler(HBox patternLine, int lineIndex, TileColor selectedColor) {
        boolean isValidPlacement = validatePlacement(lineIndex, selectedColor);

        // Clear existing handlers
        removePatternLineInteractions(patternLine);

        if (isValidPlacement) {
            setupValidPatternLineInteraction(patternLine, lineIndex);
        } else {
            setupInvalidPatternLineInteraction(patternLine);
        }
    }

    private boolean validatePlacement(int lineIndex, TileColor selectedColor) {
        Player currentPlayer = gameModel.getCurrentPlayer();
        PatternLine patternLine = currentPlayer.getPatternLines().get(lineIndex);

        // Check if pattern line is full
        if (patternLine.isFull()) {
            return false;
        }

        // Check if pattern line already has different color
        if (!patternLine.isEmpty() && patternLine.getColor() != selectedColor) {
            return false;
        }

        // Check if wall already has this color in the corresponding row
        return !currentPlayer.getWall().hasColor(lineIndex, selectedColor);
    }

    private void setupValidPatternLineInteraction(HBox patternLine, int lineIndex) {
        patternLine.setOnMouseEntered(e -> {
            patternLine.setStyle(VALID_PLACEMENT);
            animatePatternLine(patternLine, HIGHLIGHT_SCALE);
        });

        patternLine.setOnMouseExited(e -> {
            patternLine.setStyle(NO_HIGHLIGHT);
            animatePatternLine(patternLine, NORMAL_SCALE);
        });

        patternLine.setOnMouseClicked(e -> handlePatternLineClick(lineIndex));
    }

    private void setupInvalidPatternLineInteraction(HBox patternLine) {
        patternLine.setOnMouseEntered(e -> patternLine.setStyle(INVALID_PLACEMENT));
        patternLine.setOnMouseExited(e -> patternLine.setStyle(NO_HIGHLIGHT));
    }

    private void animatePatternLine(HBox patternLine, double scale) {
        patternLine.getChildren().forEach(node -> {
            if (node instanceof Circle) {
                ScaleTransition scaleTransition = new ScaleTransition(
                        Duration.millis(ANIMATION_DURATION_MS), node);
                scaleTransition.setToX(scale);
                scaleTransition.setToY(scale);
                scaleTransition.play();
            }
        });
    }

    private void handlePatternLineClick(int lineIndex) {
        if (!hasActiveTiles()) return;

        TileColor selectedColor = getSelectedTileColor();
        if (selectedColor == null) return;

        Player currentPlayer = gameModel.getCurrentPlayer();
        PatternLine targetLine = currentPlayer.getPatternLines().get(lineIndex);

        // Create tiles from hand
        List<Tile> tilesFromHand = createTilesFromHand(selectedColor);

        // Try to place tiles
        if (targetLine.addTiles(tilesFromHand)) {
            // Handle overflow
            handleOverflow(tilesFromHand, targetLine);

            // Clear hand and update UI
            getCurrentPlayerHand().getChildren().clear();
            updatePatternLines();
            updateFloorLine();
            clearAllPatternLineInteractions();
            enableFactoryInteractions();
        }
    }

    private List<Tile> createTilesFromHand(TileColor color) {
        HBox hand = getCurrentPlayerHand();
        List<Tile> tiles = new ArrayList<>();
        hand.getChildren().forEach(node -> tiles.add(new Tile(color)));
        return tiles;
    }

    private void handleOverflow(List<Tile> tiles, PatternLine targetLine) {
        int availableSpace = targetLine.getSize() - targetLine.getTiles().size() + tiles.size();
        if (availableSpace > targetLine.getSize()) {
            List<Tile> overflow = tiles.subList(targetLine.getSize(), tiles.size());
            gameModel.getCurrentPlayer().getFloorLine().addTiles(overflow);
        }
    }

    private TileColor getSelectedTileColor() {
        HBox hand = getCurrentPlayerHand();
        if (hand.getChildren().isEmpty()) return null;

        Circle firstTile = (Circle) hand.getChildren().get(0);
        return getTileColorFromFill((Color) firstTile.getFill());
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

    private void updatePatternLines() {
        VBox patternLinesContainer = findPatternLinesContainer(getCurrentPlayerBoard());
        if (patternLinesContainer == null) return;

        List<PatternLine> patternLines = gameModel.getCurrentPlayer().getPatternLines();

        // Skip label at index 0
        for (int i = 1; i <= patternLines.size(); i++) {
            if (patternLinesContainer.getChildren().get(i) instanceof HBox lineView) {
                updateSinglePatternLine(lineView, patternLines.get(i - 1));
            }
        }
    }

    private void updateSinglePatternLine(HBox lineView, PatternLine patternLine) {
        List<Circle> circles = lineView.getChildren().stream()
                .filter(node -> node instanceof Circle)
                .map(node -> (Circle) node)
                .collect(Collectors.toList());

        List<Tile> tiles = patternLine.getTiles();
        for (int i = 0; i < circles.size(); i++) {
            Circle circle = circles.get(i);
            if (i < tiles.size()) {
                circle.setFill(Color.web(tiles.get(i).getColor().getHexCode()));
            } else {
                circle.setFill(Color.web("#374151")); // Empty space color
            }
        }
    }

    private void updateFloorLine() {
        // TODO: Implement floor line update logic
    }

    private void disableFactoryInteractions() {
        view.getFactoriesContainer().setDisable(true);
    }

    private void enableFactoryInteractions() {
        view.getFactoriesContainer().setDisable(false);
    }

    private void removePatternLineInteractions(HBox patternLine) {
        patternLine.setOnMouseEntered(null);
        patternLine.setOnMouseExited(null);
        patternLine.setOnMouseClicked(null);
        patternLine.setStyle(NO_HIGHLIGHT);
        animatePatternLine(patternLine, NORMAL_SCALE);
    }

    public void clearAllPatternLineInteractions() {
        VBox patternLinesContainer = findPatternLinesContainer(getCurrentPlayerBoard());
        if (patternLinesContainer == null) return;

        // Skip label at index 0
        for (int i = 1; i <= 5; i++) {
            if (patternLinesContainer.getChildren().get(i) instanceof HBox patternLine) {
                removePatternLineInteractions(patternLine);
            }
        }
    }
}