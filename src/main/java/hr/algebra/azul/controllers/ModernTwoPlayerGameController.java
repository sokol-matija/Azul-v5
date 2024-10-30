package hr.algebra.azul.controllers;

import hr.algebra.azul.helper.TileAnimationManager;
import hr.algebra.azul.models.*;
import hr.algebra.azul.view.ModernTwoPlayerGameView;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModernTwoPlayerGameController {
    private final ModernTwoPlayerGameView view;
    private final GameModel gameModel;
    private final Stage primaryStage;
    private Timeline timer;
    private final IntegerProperty timeRemaining = new SimpleIntegerProperty(150); // 2:30 in seconds
    private boolean isGamePaused = false;
    private static final Duration ANIMATION_DURATION = Duration.millis(500);
    private TileAnimationManager animationManager;
    // Track UI state
    private int selectedFactoryIndex = -1;
    private TileColor selectedColor = null;

    // Style constants
    private static final String DIALOG_BG = "#1F2937";
    private static final String DIALOG_HEADER_BG = "#111827";
    private static final String BUTTON_BG = "#3B82F6";
    private static final String BUTTON_HOVER_BG = "#2563EB";
    private static final String TEXT_PRIMARY = "white";
    private static final String TEXT_SECONDARY = "#9CA3AF";
    private static final String CARD_BG = "#1F2937";
    private static final String ACCENT_COLOR = "#4F46E5";

    public ModernTwoPlayerGameController(ModernTwoPlayerGameView view, Stage primaryStage) {
        this.view = view;
        this.primaryStage = primaryStage;
        this.gameModel = new GameModel(2); // Initialize 2-player game
        initializeController();
        updateEntireView();
    }

    private void initializeController() {
        setupButtonHandlers();
        setupFactoryClickHandlers();
        setupPatternLineHandlers();
        initializeTimer();

        view.getStage().setOnCloseRequest(e -> {
            e.consume();
            handleExitClick();
        });

        this.animationManager = new TileAnimationManager(view.getAnimationLayer());
    }

    private void setupButtonHandlers() {
        view.getUndoButton().setOnAction(e -> handleUndoClick());
        view.getSaveButton().setOnAction(e -> handleSaveClick());
        view.getExitButton().setOnAction(e -> handleExitClick());
        view.getSettingsButton().setOnAction(e -> handleSettingsClick());
    }

    private void setupFactoryClickHandlers() {
        GridPane factoriesGrid = view.getFactoriesContainer();
        for (int i = 0; i < factoriesGrid.getChildren().size(); i++) {
            final int factoryIndex = i;
            VBox factory = (VBox) factoriesGrid.getChildren().get(i);
            factory.setOnMouseClicked(e -> handleFactoryClick(factoryIndex, factory));
        }
    }

    private void setupPatternLineHandlers() {
        setupPlayerPatternLines(view.getPlayer1Board());
        setupPlayerPatternLines(view.getPlayer2Board());
    }

    private void setupPlayerPatternLines(VBox playerBoard) {
        // Find the VBox containing pattern lines - it's after the header and hand
        VBox patternLinesContainer = findPatternLinesContainer(playerBoard);
        if (patternLinesContainer != null) {
            // Skip the label and get the pattern lines container
            for (int i = 1; i < patternLinesContainer.getChildren().size(); i++) {
                Node node = patternLinesContainer.getChildren().get(i);
                if (node instanceof HBox patternLine) {
                    final int lineIndex = i - 1; // Adjust index to account for label
                    patternLine.setOnMouseClicked(e -> handlePatternLineClick(lineIndex));
                }
            }
        }
    }

    private VBox findPatternLinesContainer(VBox playerBoard) {
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

    private void handleFactoryClick(int factoryIndex, VBox factoryNode) {
        if (isGamePaused) return;

        List<Tile> factoryTiles = gameModel.getFactories().get(factoryIndex);
        if (factoryTiles.isEmpty()) return;

        // Get the first tile's color
        TileColor selectedColor = factoryTiles.get(0).getColor();

        // Split tiles into selected and remaining
        List<Tile> selectedTiles = factoryTiles.stream()
                .filter(tile -> tile.getColor() == selectedColor)
                .collect(Collectors.toList());

        List<Tile> remainingTiles = factoryTiles.stream()
                .filter(tile -> tile.getColor() != selectedColor)
                .collect(Collectors.toList());

        // Clear the factory immediately
        gameModel.getFactories().get(factoryIndex).clear();

        // Start animation
        animationManager.animateFactorySelection(
                factoryNode,
                selectedTiles,
                remainingTiles,
                getCurrentPlayerHand(),
                view.getCenterPool(),
                () -> {
                    // Update model and UI after animation completes
                    updatePlayerHand(selectedTiles);
                    gameModel.moveTilesToCenter(remainingTiles);
                    updateFactoryDisplay(factoryNode);
                    updateCenterPool();
                }
        );
    }

    private void animateSelectionAndMove(VBox factoryNode, List<Tile> selectedTiles, List<Tile> remainingTiles) {
        // Create visual representations of the tiles
        List<Circle> selectedCircles = createTileCircles(selectedTiles);
        List<Circle> remainingCircles = createTileCircles(remainingTiles);

        // Get target locations
        HBox playerHand = getCurrentPlayerHand();
        VBox centerPool = view.getCenterPool();

        // Create and play animations
        ParallelTransition allAnimations = new ParallelTransition();

        // Animate selected tiles to hand
        for (Circle tileCircle : selectedCircles) {
            PathTransition pathToHand = createPathTransition(
                    factoryNode,
                    playerHand,
                    tileCircle,
                    ANIMATION_DURATION
            );
            allAnimations.getChildren().add(pathToHand);
        }

        // Animate remaining tiles to center
        for (Circle tileCircle : remainingCircles) {
            PathTransition pathToCenter = createPathTransition(
                    factoryNode,
                    centerPool,
                    tileCircle,
                    ANIMATION_DURATION
            );
            allAnimations.getChildren().add(pathToCenter);
        }

        // After animations complete
        allAnimations.setOnFinished(e -> {
            updatePlayerHand(selectedTiles);
            gameModel.moveTilesToCenter(remainingTiles);
            updateFactoryDisplay(factoryNode);
            updateCenterPool();
        });

        allAnimations.play();
    }


    private List<Circle> createTileCircles(List<Tile> tiles) {
        return tiles.stream().map(tile -> {
            Circle circle = new Circle(15);
            circle.setFill(Color.web(tile.getColor().getHexCode()));
            circle.setStroke(Color.web("#4B5563"));
            circle.setStrokeWidth(1);
            return circle;
        }).collect(Collectors.toList());
    }

    private PathTransition createPathTransition(Node source, Node target, Node movingNode, Duration duration) {
        // Get source and target coordinates in scene
        Bounds sourceBounds = source.localToScene(source.getBoundsInLocal());
        Bounds targetBounds = target.localToScene(target.getBoundsInLocal());

        // Create path
        Path path = new Path();
        path.getElements().add(new MoveTo(
                sourceBounds.getCenterX(),
                sourceBounds.getCenterY()
        ));
        path.getElements().add(new LineTo(
                targetBounds.getCenterX(),
                targetBounds.getCenterY()
        ));

        // Create transition
        PathTransition transition = new PathTransition(duration, path, movingNode);
        transition.setAutoReverse(false);

        return transition;
    }

    private HBox getCurrentPlayerHand() {
        return gameModel.getCurrentPlayer() == gameModel.getPlayers().get(0)
                ? view.getPlayer1Hand()
                : view.getPlayer2Hand();
    }

    private void updatePlayerHand(List<Tile> tiles) {
        HBox hand = getCurrentPlayerHand();
        hand.getChildren().clear();

        for (Tile tile : tiles) {
            Circle tileCircle = new Circle(15);
            tileCircle.setFill(Color.web(tile.getColor().getHexCode()));
            tileCircle.setStroke(Color.web("#4B5563"));
            tileCircle.setStrokeWidth(1);
            hand.getChildren().add(tileCircle);
        }
    }

    private void clearPlayerHand() {
        getCurrentPlayerHand().getChildren().clear();
    }

    private void updateFactoryDisplay(VBox factory) {
        GridPane tileGrid = (GridPane) factory.getChildren().get(0);
        tileGrid.getChildren().clear();
    }

    private void showTileColorSelectionDialog(List<Tile> tiles) {
        Dialog<TileColor> dialog = new Dialog<>();
        dialog.setTitle("Select Tiles");
        dialog.setHeaderText("Choose tile color to take");

        DialogPane dialogPane = dialog.getDialogPane();
        VBox colorOptions = new VBox(10);
        colorOptions.setAlignment(Pos.CENTER);

        // Count tiles of each color
        tiles.stream()
                .map(Tile::getColor)
                .distinct()
                .forEach(color -> {
                    long count = tiles.stream()
                            .filter(t -> t.getColor() == color)
                            .count();

                    Button colorButton = createColorButton(color, count);
                    colorButton.setOnAction(e -> dialog.setResult(color));
                    colorOptions.getChildren().add(colorButton);
                });

        dialogPane.setContent(colorOptions);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        styleDialog(dialog);

        Optional<TileColor> result = dialog.showAndWait();
        result.ifPresent(color -> {
            selectedColor = color;
            updateFactorySelection(color);
        });
    }

    private Button createColorButton(TileColor color, long count) {
        Button button = new Button(color.toString() + " (" + count + ")");
        button.setPrefWidth(200);
        button.setPrefHeight(40);
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-font-size: 14px;
            """, color.getHexCode()));

        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle() + "-fx-background-color: " + getDarkerColor(color.getHexCode()) + ";")
        );

        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle() + "-fx-background-color: " + color.getHexCode() + ";")
        );

        return button;
    }

    private String getDarkerColor(String hexColor) {
        Color color = Color.web(hexColor);
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 0.8 * 255),
                (int)(color.getGreen() * 0.8 * 255),
                (int)(color.getBlue() * 0.8 * 255));
    }


    private void handlePatternLineClick(int lineIndex) {
        if (isGamePaused) return;

        // Get the current player's hand
        HBox playerHand = getCurrentPlayerHand();
        if (playerHand.getChildren().isEmpty()) {
            return; // No tiles selected
        }

        // Get selected tiles from hand
        List<Tile> selectedTiles = new ArrayList<>();
        for (Node node : playerHand.getChildren()) {
            if (node instanceof Circle circle) {
                // Convert the circle's fill color back to a TileColor
                Color fillColor = (Color) circle.getFill();
                TileColor tileColor = getTileColorFromFill(fillColor);
                if (tileColor != null) {
                    selectedTiles.add(new Tile(tileColor));
                }
            }
        }

        if (selectedTiles.isEmpty()) return;

        // Try to place tiles in the pattern line
        if (gameModel.selectTilesFromFactory(selectedFactoryIndex, selectedTiles.get(0).getColor(), lineIndex)) {
            // Animate tiles moving to pattern line
            animateTilesToPatternLine(playerHand, lineIndex, selectedTiles);
        }
    }

    private void animateTilesToPatternLine(HBox hand, int lineIndex, List<Tile> tiles) {
        VBox playerBoard = gameModel.getCurrentPlayer() == gameModel.getPlayers().get(0)
                ? view.getPlayer1Board()
                : view.getPlayer2Board();

        VBox patternLinesContainer = findPatternLinesContainer(playerBoard);
        if (patternLinesContainer == null) return;

        HBox targetLine = (HBox) patternLinesContainer.getChildren().get(lineIndex + 1);

        ParallelTransition allAnimations = new ParallelTransition();

        for (Node tileNode : hand.getChildren()) {
            PathTransition path = createPathTransition(
                    hand,
                    targetLine,
                    tileNode,
                    Duration.millis(500)
            );
            allAnimations.getChildren().add(path);
        }

        allAnimations.setOnFinished(e -> {
            hand.getChildren().clear();
            updatePatternLines();
            // TODO: Implement  isRoundComplete method
//            if (gameModel.isRoundComplete()) {
//                handleRoundEnd();
//            } else {
//                gameModel.endTurn();
//                updateEntireView();
//            }
        });

        allAnimations.play();
    }

    private TileColor getTileColorFromFill(Color fillColor) {
        String hexColor = String.format("#%02X%02X%02X",
                (int) (fillColor.getRed() * 255),
                (int) (fillColor.getGreen() * 255),
                (int) (fillColor.getBlue() * 255));

//        return Arrays.stream(TileColor.values())
//                .filter(color -> color.getHexCode().equalsIgnoreCase(hexColor))
//                .findFirst()
//                .orElse(null);
        // TODO: Implement getTileColorFromFill method
        return  TileColor.BLUE;
    }

    private void updateEntireView() {
        Platform.runLater(() -> {
            updateFactories();
            updateCenterPool();
            updatePlayerBoards();
            updateScores();
            updateCurrentPlayer();
            updateGameState();
        });
    }

    private void updateFactories() {
        List<List<Tile>> factories = gameModel.getFactories();
        GridPane factoriesGrid = view.getFactoriesContainer();

        for (int i = 0; i < factories.size(); i++) {
            VBox factoryBox = (VBox) factoriesGrid.getChildren().get(i);
            GridPane tileGrid = (GridPane) factoryBox.getChildren().get(0);
            List<Tile> factoryTiles = factories.get(i);

            // Clear existing tiles
            tileGrid.getChildren().clear();

            // Add new tiles
            int tileIndex = 0;
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    Circle tileCircle = new Circle(15);

                    if (tileIndex < factoryTiles.size()) {
                        Tile tile = factoryTiles.get(tileIndex);
                        tileCircle.setFill(Color.web(tile.getColor().getHexCode()));
                    } else {
                        tileCircle.setFill(Color.web("#374151")); // Empty tile color
                    }

                    tileCircle.setStroke(Color.web("#4B5563"));
                    tileCircle.setStrokeWidth(1);
                    tileCircle.setEffect(new InnerShadow(5, Color.web("#000000", 0.2)));

                    tileGrid.add(tileCircle, col, row);
                    tileIndex++;
                }
            }
        }
    }
    private void updateCenterPool() {
        VBox centerPool = view.getCenterPool();
        FlowPane tilesContainer = (FlowPane) centerPool.getChildren().get(1);
        updateTileDisplay(tilesContainer, gameModel.getCenterPool());
    }

    private void updateFactorySelection(TileColor selectedColor) {
        // Update visual selection state
        GridPane factoriesGrid = view.getFactoriesContainer();
        for (int i = 0; i < factoriesGrid.getChildren().size(); i++) {
            VBox factory = (VBox) factoriesGrid.getChildren().get(i);
            boolean isSelected = i == selectedFactoryIndex;

            factory.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 10;
                -fx-border-color: %s;
                -fx-border-width: %d;
                -fx-border-radius: 8;
                """,
                    CARD_BG,
                    isSelected ? ACCENT_COLOR : "transparent",
                    isSelected ? 2 : 0
            ));
        }
    }

    private void updateTileDisplay(Pane container, List<Tile> tiles) {
        container.getChildren().clear();
        for (Tile tile : tiles) {
            if (tile.getColor() != null) {  // Skip first player token
                Circle tileCircle = new Circle(15);
                tileCircle.setFill(Color.web(tile.getColor().getHexCode()));
                tileCircle.setStroke(Color.web("#4B5563"));
                container.getChildren().add(tileCircle);
            }
        }
    }

    private void updatePlayerBoards() {
        List<Player> players = gameModel.getPlayers();
        updatePlayerBoard(view.getPlayer1Board(), players.get(0));
        updatePlayerBoard(view.getPlayer2Board(), players.get(1));
    }

    private void updatePlayerBoard(VBox playerBoard, Player player) {
        //updateSinglePlayerPatternLines (playerBoard, player);
        updateWall(playerBoard, player);
        updateFloorLine(playerBoard, player);
    }

    private void updateSinglePlayerPatternLines(VBox playerBoard, Player player) {
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
        for (int j = 0; j < lineContainer.getChildren().size(); j++) {
            Circle space = (Circle) lineContainer.getChildren().get(j);
            if (j < patternLine.getTiles().size()) {
                Tile tile = patternLine.getTiles().get(j);
                space.setFill(Color.web(tile.getColor().getHexCode()));
            } else {
                space.setFill(Color.web(CARD_BG));
            }
        }
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

            // Update each space in the pattern line
            for (int j = 0; j < lineContainer.getChildren().size(); j++) {
                Circle space = (Circle) lineContainer.getChildren().get(j);
                if (j < patternLine.getTiles().size()) {
                    Tile tile = patternLine.getTiles().get(j);
                    space.setFill(Color.web(tile.getColor().getHexCode()));
                } else {
                    space.setFill(Color.web(CARD_BG));
                }
            }
        }
    }

    private void updateWall(VBox playerBoard, Player player) {
        GridPane wall = (GridPane) playerBoard.getChildren().stream()
                .filter(node -> node instanceof GridPane)
                .findFirst()
                .orElse(null);

        if (wall != null) {
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    StackPane space = (StackPane) wall.getChildren().get(row * 5 + col);
                    if (player.wall.tiles[row][col]) {
                        Circle tile = (Circle) space.getChildren().get(2);
                        tile.setFill(Color.web(player.wall.wallPattern[row][col].getHexCode()));
                        tile.setOpacity(1.0);
                    }
                }
            }
        }
    }

    private void updateFloorLine(VBox playerBoard, Player player) {
        HBox floorLine = (HBox) playerBoard.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .reduce((first, second) -> second) // Get the last HBox (floor line)
                .orElse(null);

        if (floorLine != null) {
            // Clear existing floor line
            floorLine.getChildren().clear();

            // Create new floor line spaces
            for (int i = 0; i < FloorLine.MAX_TILES; i++) {
                Circle space = new Circle(15);
                space.setFill(Color.web(CARD_BG)); // Empty space color
                space.setStroke(Color.web("#4B5563"));
                space.setStrokeWidth(1);

                // If there's a tile at this position, color it
                Tile tile = player.getFloorLine().getTileAt(i);
                if (tile != null && tile.getColor() != null) {
                    space.setFill(Color.web(tile.getColor().getHexCode()));
                }

                floorLine.getChildren().add(space);
            }
        }
    }

    private void updatePatternLine(HBox lineContainer, PatternLine patternLine) {
        for (int i = 0; i < lineContainer.getChildren().size(); i++) {
            Circle space = (Circle) lineContainer.getChildren().get(i);
            if (i < patternLine.getTiles().size()) {
                Tile tile = patternLine.getTiles().get(i);
                space.setFill(Color.web(tile.getColor().getHexCode()));
            } else {
                space.setFill(Color.web("#374151")); // Empty space color
            }
        }
    }

    private void updateScores() {
        List<Player> players = gameModel.getPlayers();
        updatePlayerScore(view.getPlayer1Board(), players.get(0).score);
        updatePlayerScore(view.getPlayer2Board(), players.get(1).score);
    }

    private void updatePlayerScore(VBox playerBoard, int score) {
        HBox header = (HBox) playerBoard.getChildren().get(0);
        Label scoreLabel = (Label) header.getChildren().get(2);
        scoreLabel.setText(String.valueOf(score));
    }

    private void updateCurrentPlayer() {
        Player current = gameModel.getCurrentPlayer();
        view.getCurrentPlayerLabel().setText(current.name + "'s Turn");

        view.getPlayer1Board().setStyle(getPlayerBoardStyle(current == gameModel.getPlayers().get(0)));
        view.getPlayer2Board().setStyle(getPlayerBoardStyle(current == gameModel.getPlayers().get(1)));
    }

    private void updateGameState() {
        switch (gameModel.getGameState()) {
            case GAME_END -> handleGameEnd();
            case SCORING -> handleRoundEnd();
        }
    }

    private void initializeTimer() {
        timer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateTimer())
        );
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
        timeRemaining.set(150);
    }

    private void updateTimer() {
        if (timeRemaining.get() > 0) {
            Platform.runLater(() -> {
                timeRemaining.set(timeRemaining.get() - 1);
                int minutes = timeRemaining.get() / 60;
                int seconds = timeRemaining.get() % 60;
                view.getTimerLabel().setText(String.format("⏱ %02d:%02d", minutes, seconds));

                if (timeRemaining.get() == 30) {
                    showTimeWarning();
                }
            });
        } else {
            timer.stop();
            Platform.runLater(this::handleTimeOut);
        }
    }

    private void resetTimer() {
        timeRemaining.set(150);
        view.getTimerLabel().setStyle("-fx-text-fill: #9CA3AF;");
    }

    private void showTimeWarning() {
        view.getTimerLabel().setStyle("-fx-text-fill: #EF4444;");
    }

    private void handleTimeOut() {
        showMessage("Time Out", "Your turn has ended due to time out.", Alert.AlertType.WARNING);
        gameModel.endTurn();
        updateEntireView();
        resetTimer();
    }

    private void handleUndoClick() {
//        //TODO: Implement undo functionality
//        if (!gameModel.canUndo()) {
//            showMessage("Cannot Undo", "No moves available to undo.", Alert.AlertType.INFORMATION);
//            return;
//        }
        //gameModel.undo();
        updateEntireView();
    }

    private void handleSaveClick() {
        showMessage("Game Saved", "Your game has been saved successfully.", Alert.AlertType.INFORMATION);
    }

    private void handleExitClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Game");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Any unsaved progress will be lost.");

        styleDialog(alert);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                timer.stop();
                view.getStage().close();
            }
        });
    }

    private void handleSettingsClick() {
        isGamePaused = true;
        timer.pause();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Game Settings");
        dialog.setHeaderText("Settings");

        VBox content = new VBox(10);

        // Add game settings controls
        CheckBox muteAudioBox = new CheckBox("Mute Audio");
        muteAudioBox.setStyle("-fx-text-fill: white;");

        Slider timerSlider = new Slider(60, 180, 150);
        timerSlider.setShowTickLabels(true);
        timerSlider.setShowTickMarks(true);
        timerSlider.setMajorTickUnit(30);
        Label timerLabel = new Label("Turn Timer (seconds): ");
        timerLabel.setStyle("-fx-text-fill: white;");

        content.getChildren().addAll(
                muteAudioBox,
                new VBox(5, timerLabel, timerSlider)
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        styleDialog(dialog);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Apply settings
                timeRemaining.set((int) timerSlider.getValue());
            }
            isGamePaused = false;
            timer.play();
        });
    }

    private void showMessage(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);

        styleDialog(alert);
        alert.showAndWait();
    }

    private void showInvalidMoveDialog() {
        showMessage("Invalid Move", "This move is not allowed.", Alert.AlertType.WARNING);
    }

    private boolean isRoundComplete() {
        return gameModel.getFactories().stream().allMatch(List::isEmpty) &&
                gameModel.getCenterPool().isEmpty();
    }

    private void handleRoundEnd() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Round Complete");
        dialog.setHeaderText("Round Summary");

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);

        // Add round summary information
        List<Player> players = gameModel.getPlayers();
        for (Player player : players) {
            VBox playerSummary = new VBox(5);
            Label nameLabel = new Label(player.name);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
            Label scoreLabel = new Label("Score: " + player.score);
            scoreLabel.setStyle("-fx-text-fill: #9CA3AF;");
            playerSummary.getChildren().addAll(nameLabel, scoreLabel);
            content.getChildren().add(playerSummary);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        styleDialog(dialog);
        dialog.showAndWait().ifPresent(response -> startNewRound());
    }

    private void handleGameEnd() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Game Over");
        dialog.setHeaderText("Final Scores");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        // Display final scores and winner
        List<Player> players = gameModel.getPlayers();
        Player winner = players.stream()
                .max((p1, p2) -> Integer.compare(p1.score, p2.score))
                .orElse(null);

        // Winner announcement
        if (winner != null) {
            Label winnerLabel = new Label(winner.name + " Wins!");
            winnerLabel.setStyle("""
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-text-fill: #22C55E;
                """);
            content.getChildren().add(winnerLabel);
        }

        // Player scores
        for (Player player : players) {
            HBox playerScore = new HBox(10);
            playerScore.setAlignment(Pos.CENTER);

            Label nameLabel = new Label(player.name);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

            Label scoreLabel = new Label("Final Score: " + player.score);
            scoreLabel.setStyle("-fx-text-fill: #9CA3AF;");

            playerScore.getChildren().addAll(nameLabel, scoreLabel);
            content.getChildren().add(playerScore);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        styleDialog(dialog);

        dialog.showAndWait().ifPresent(response -> {
            timer.stop();
            view.getStage().close();
        });
    }

    private void startNewRound() {
        resetTimer();
        updateEntireView();
    }

    private void styleDialog(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.setStyle(String.format("""
            -fx-background-color: %s;
            """, DIALOG_BG));

        // Style header
        dialogPane.lookup(".header-panel").setStyle(String.format("""
            -fx-background-color: %s;
            -fx-padding: 20px;
            """, DIALOG_HEADER_BG));

        Label headerText = (Label) dialogPane.lookup(".header-panel > .label");
        if (headerText != null) {
            headerText.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                """);
        }

        // Style content
        Label contentText = (Label) dialogPane.lookup(".content.label");
        if (contentText != null) {
            contentText.setStyle(String.format("""
                -fx-text-fill: %s;
                -fx-font-size: 14px;
                """, TEXT_SECONDARY));
        }

        styleDialogButtons(dialogPane);
    }

    private void styleDialogButtons(DialogPane dialogPane) {
        dialogPane.getButtonTypes().forEach(buttonType -> {
            Button button = (Button) dialogPane.lookupButton(buttonType);
            button.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-font-size: 14px;
                -fx-padding: 8 15;
                -fx-background-radius: 5;
                """, BUTTON_BG, TEXT_PRIMARY));

            button.setOnMouseEntered(e -> button.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-font-size: 14px;
                -fx-padding: 8 15;
                -fx-background-radius: 5;
                """, BUTTON_HOVER_BG, TEXT_PRIMARY)));

            button.setOnMouseExited(e -> button.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-font-size: 14px;
                -fx-padding: 8 15;
                -fx-background-radius: 5;
                """, BUTTON_BG, TEXT_PRIMARY)));
        });
    }

    private String getPlayerBoardStyle(boolean isActive) {
        return String.format("""
            -fx-background-color: #1F2937;
            -fx-padding: 20;
            %s
            """,
                isActive ? "-fx-border-color: #4F46E5; -fx-border-width: 2; -fx-border-radius: 10;" : ""
        );
    }

    public void show() {
        view.getStage().show();
    }
}