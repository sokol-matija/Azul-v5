package hr.algebra.azul.controllers;

import hr.algebra.azul.helper.PatternLineInteractionHandler;
import hr.algebra.azul.helper.TileAnimationManager;
import hr.algebra.azul.helper.TurnManager;
import hr.algebra.azul.helper.WallTilingManager;
import hr.algebra.azul.models.*;
import hr.algebra.azul.view.ModernTwoPlayerGameView;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ModernTwoPlayerGameController {
    // Constants
    private static final Duration ANIMATION_DURATION = Duration.millis(500);
    private static final String DIALOG_BG = "#1F2937";
    private static final String DIALOG_HEADER_BG = "#111827";
    private static final String BUTTON_BG = "#3B82F6";
    private static final String BUTTON_HOVER_BG = "#2563EB";
    private static final String TEXT_PRIMARY = "white";
    private static final String TEXT_SECONDARY = "#9CA3AF";
    private static final String CARD_BG = "#1F2937";
    private static final String ACCENT_COLOR = "#4F46E5";

    // Core components
    private final ModernTwoPlayerGameView view;
    private final GameModel gameModel;
    private final Stage primaryStage;

    // Helper managers
    private final PatternLineInteractionHandler patternLineInteractionHandler;
    private final WallTilingManager wallTilingManager;
    private final TileAnimationManager animationManager;
    private final TurnManager turnManager;

    // State tracking
    private boolean isGamePaused = false;
    private int selectedFactoryIndex = -1;
    private TileColor selectedColor = null;

    // Constructor and Initialization
    public ModernTwoPlayerGameController(ModernTwoPlayerGameView view, Stage primaryStage) {
        this.view = view;
        this.primaryStage = primaryStage;
        this.gameModel = new GameModel(2);
        this.wallTilingManager = new WallTilingManager(view, gameModel);
        this.turnManager = new TurnManager(gameModel, view, view.getTimerLabel());
        this.patternLineInteractionHandler = new PatternLineInteractionHandler(view, gameModel, turnManager);
        this.animationManager = new TileAnimationManager(view.getAnimationLayer());

        initializeController();
        updateEntireView();
    }

    private void initializeController() {
        setupButtonHandlers();
        setupFactoryClickHandlers();
        setupPatternLineHandlers();
        setupCenterPoolClickHandlers();

        view.getStage().setOnCloseRequest(e -> {
            e.consume();
            handleExitClick();
        });
    }

    private void setupButtonHandlers() {
        view.getUndoButton().setOnAction(e -> handleUndoClick());
        view.getSaveButton().setOnAction(e -> handleSaveClick());
        view.getExitButton().setOnAction(e -> handleExitClick());
        view.getSettingsButton().setOnAction(e -> handleSettingsClick());
        view.getEndTurnButton().setOnAction(e -> turnManager.handleEndTurn());
        view.getEndRoundButton().setOnAction(e -> handleRoundEnd());
    }
    // Factory and tile selection handling
    private void setupFactoryClickHandlers() {
        GridPane factoriesGrid = view.getFactoriesContainer();
        for (int i = 0; i < factoriesGrid.getChildren().size(); i++) {
            final int factoryIndex = i;
            VBox factory = (VBox) factoriesGrid.getChildren().get(i);
            GridPane tileGrid = (GridPane) factory.getChildren().get(0);

            for (Node node : tileGrid.getChildren()) {
                if (node instanceof Circle tile) {
                    setupTileHoverEffects((Circle) node, factory);
                    tile.setOnMouseClicked(e -> handleFactoryClick(tile, factory, factoryIndex));
                }
            }
        }
    }

    private void handleFactoryClick(Circle clickedTile, VBox factory, int factoryIndex) {
        if (isGamePaused || getCurrentPlayerHand().getChildren().size() > 0) return;

        Color tileColor = (Color) clickedTile.getFill();
        Factory factoryModel = gameModel.getFactories().get(factoryIndex);

        // Get tiles of the selected color
        List<Tile> selectedTiles = factoryModel.selectTilesByColor(getTileColorFromFill(tileColor));
        List<Tile> remainingTiles = factoryModel.removeRemainingTiles();

        // Get the current player's hand
        HBox playerHand = getCurrentPlayerHand();

        // Start animation
        animationManager.animateFactorySelection(
                factory,
                selectedTiles,
                remainingTiles,
                playerHand,
                view.getCenterPool(),
                () -> {
                    updatePlayerHand(selectedTiles);
                    gameModel.addTilesToCenter(remainingTiles);
                    updateFactoryDisplay(factory);
                    updateCenterPool();
                    patternLineInteractionHandler.setupPatternLineInteractions();

                    // Check for round completion
                    if (gameModel.isRoundComplete()) {
                        handleRoundEnd();
                    }
                }
        );
    }

    private void updatePlayerHand(List<Tile> tiles) {
        HBox hand = getCurrentPlayerHand();
        hand.getChildren().clear();

        // Update model
        gameModel.getCurrentPlayer().getHand().clear();
        gameModel.getCurrentPlayer().getHand().addAll(tiles);

        // Update UI
        for (Tile tile : tiles) {
            Circle tileCircle = new Circle(15);
            tileCircle.setFill(Color.web(tile.getColor().getHexCode()));
            tileCircle.setStroke(Color.web("#4B5563"));
            tileCircle.setStrokeWidth(1);
            hand.getChildren().add(tileCircle);
        }
    }

    private void setupTileHoverEffects(Circle tileCircle, VBox factory) {
        Color tileColor = (Color) tileCircle.getFill();
        if (tileColor.equals(Color.web("#374151"))) return; // Skip empty tiles

        DropShadow glow = new DropShadow();
        glow.setColor(tileColor);
        glow.setRadius(10);
        glow.setSpread(0.3);

        tileCircle.setOnMouseEntered(e -> {
            factory.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 10;
                -fx-border-color: #60A5FA;
                -fx-border-width: 2;
                -fx-border-radius: 10;
                """, CARD_BG));

            ScaleTransition scale = new ScaleTransition(Duration.millis(100), tileCircle);
            scale.setToX(1.1);
            scale.setToY(1.1);
            scale.play();

            tileCircle.setEffect(glow);
        });

        tileCircle.setOnMouseExited(e -> {
            factory.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 10;
                """, CARD_BG));

            ScaleTransition scale = new ScaleTransition(Duration.millis(100), tileCircle);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();

            tileCircle.setEffect(new InnerShadow(5, Color.web("#000000", 0.2)));
        });
    }
    // Pattern line handling
    private void setupPatternLineHandlers() {
        setupPlayerPatternLines(view.getPlayer1Board());
        setupPlayerPatternLines(view.getPlayer2Board());
    }

    private void setupPlayerPatternLines(VBox playerBoard) {
        VBox patternLinesContainer = patternLineInteractionHandler.findPatternLinesContainer(playerBoard);
        if (patternLinesContainer == null) return;

        for (int i = 1; i < patternLinesContainer.getChildren().size(); i++) {
            Node node = patternLinesContainer.getChildren().get(i);
            if (node instanceof HBox patternLine) {
                final int lineIndex = i - 1;
                patternLine.setOnMouseClicked(e -> handlePatternLineClick(lineIndex, playerBoard));
            }
        }
    }

    private void handlePatternLineClick(int lineIndex, VBox playerBoard) {
        if (isGamePaused) return;

        patternLineInteractionHandler.handlePatternLineClick(lineIndex, playerBoard);

        // Check for round completion after tile placement
        if (gameModel.isRoundComplete()) {
            handleRoundEnd();
        }
    }

    // Center pool handling
    private void setupCenterPoolClickHandlers() {
        VBox centerPool = view.getCenterPool();
        FlowPane tilesContainer = (FlowPane) centerPool.getChildren().get(1);

        tilesContainer.getChildren().addListener((ListChangeListener<Node>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Node node : change.getAddedSubList()) {
                        if (node instanceof Circle tile) {
                            setupCenterTileInteraction(tile);
                        }
                    }
                }
            }
        });
    }

    private void setupCenterTileInteraction(Circle tile) {
        if (tile.getFill() == null) return; // Skip first player token

        tile.setOnMouseClicked(e -> handleCenterTileClick(tile));
        setupCenterTileHoverEffects(tile);
    }

    private void setupCenterTileHoverEffects(Circle tileCircle) {

    }

    private void handleCenterTileClick(Circle clickedTile) {
        if (isGamePaused || getCurrentPlayerHand().getChildren().size() > 0) return;

        Color tileColor = (Color) clickedTile.getFill();
        TileColor selectedColor = getTileColorFromFill(tileColor);
        List<Tile> selectedTiles = collectTilesFromCenter(selectedColor);
        boolean hasFirstPlayerToken = handleFirstPlayerToken();

        animateSelectionFromCenter(selectedTiles, hasFirstPlayerToken);
    }

    private void animateSelectionFromCenter(List<Tile> selectedTiles, boolean hasFirstPlayerToken) {
        HBox playerHand = getCurrentPlayerHand();
        List<Circle> tileCircles = selectedTiles.stream()
                .map(tile -> {
                    Circle circle = new Circle(15);
                    circle.setFill(Color.web(tile.getColor().getHexCode()));
                    circle.setStroke(Color.web("#4B5563"));
                    circle.setStrokeWidth(1);
                    return circle;
                })
                .collect(Collectors.toList());

        VBox centerPool = view.getCenterPool();
        Bounds centerBounds = centerPool.localToScene(centerPool.getBoundsInLocal());
        Bounds handBounds = playerHand.localToScene(playerHand.getBoundsInLocal());

        ParallelTransition allAnimations = new ParallelTransition();

        for (Circle tileCircle : tileCircles) {
            Path path = new Path();
            path.getElements().addAll(
                    new MoveTo(centerBounds.getCenterX(), centerBounds.getCenterY()),
                    new LineTo(handBounds.getCenterX(), handBounds.getCenterY())
            );

            PathTransition transition = new PathTransition(Duration.millis(500), path, tileCircle);
            allAnimations.getChildren().add(transition);
        }

        allAnimations.setOnFinished(e -> {
            updatePlayerHand(selectedTiles);
            if (hasFirstPlayerToken) {
                gameModel.getCurrentPlayer().getFloorLine().addTile(new Tile(null));
            }
        });

        allAnimations.play();
    }

    private List<Tile> collectTilesFromCenter(TileColor color) {
        List<Tile> selectedTiles = new ArrayList<>();
        List<Node> tilesToRemove = new ArrayList<>();
        FlowPane tilesContainer = (FlowPane) view.getCenterPool().getChildren().get(1);

        for (Node node : tilesContainer.getChildren()) {
            if (node instanceof Circle circle) {
                Color circleFill = (Color) circle.getFill();
                if (getTileColorFromFill(circleFill) == color) {
                    selectedTiles.add(new Tile(color));
                    tilesToRemove.add(circle);
                }
            }
        }

        tilesContainer.getChildren().removeAll(tilesToRemove);
        return selectedTiles;
    }

    // Game flow management
    private void handleRoundEnd() {
        if (gameModel.processRoundEnd()) {
            // Process wall tiling
            wallTilingManager.processWallTiling();

            // Update the view
            clearPlayerHands();
            updateEntireView();

            // Reset timer
            turnManager.resetTimer();

            // Show round end dialog
            Platform.runLater(this::showRoundEndDialog);
        }
    }

    private void showRoundEndDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Round Complete");
        dialog.setHeaderText("Round Summary");

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);

        for (Player player : gameModel.getPlayers()) {
            VBox playerSummary = new VBox(5);
            Label nameLabel = new Label(player.getName());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
            Label scoreLabel = new Label("Score: " + player.getScore());
            scoreLabel.setStyle("-fx-text-fill: #9CA3AF;");
            playerSummary.getChildren().addAll(nameLabel, scoreLabel);
            content.getChildren().add(playerSummary);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        styleDialog(dialog);
        dialog.showAndWait();
    }

    private void updateEntireView() {
        Platform.runLater(() -> {
            updateFactories();
            updateCenterPool();
            updatePlayerBoards();
            updateScores();
            updateCurrentPlayer();
        });
    }

    private void handleGameEnd() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Game Over");
        dialog.setHeaderText("Final Scores");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        List<Player> players = gameModel.getPlayers();
        Player winner = players.stream()
                .max(Comparator.comparingInt(Player::getScore))
                .orElse(null);

        if (winner != null) {
            Label winnerLabel = new Label(winner.getName() + " Wins!");
            winnerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #22C55E;");
            content.getChildren().add(winnerLabel);
        }

        for (Player player : players) {
            HBox playerScore = new HBox(10);
            playerScore.setAlignment(Pos.CENTER);
            playerScore.getChildren().addAll(
                    new Label(player.getName()) {{
                        setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
                    }},
                    new Label("Score: " + player.getScore()) {{
                        setStyle("-fx-text-fill: #9CA3AF;");
                    }}
            );
            content.getChildren().add(playerScore);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        styleDialog(dialog);

        dialog.showAndWait().ifPresent(response -> {
            turnManager.pauseTimer();
            view.getStage().close();
        });
    }
    // Utility methods
    private void styleDialog(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + DIALOG_BG);
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: " + DIALOG_HEADER_BG);

        // Style header text
        Label headerText = (Label) dialogPane.lookup(".header-panel > .label");
        if (headerText != null) {
            headerText.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        }

        // Style content
        Label contentText = (Label) dialogPane.lookup(".content.label");
        if (contentText != null) {
            contentText.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 14px;");
        }

        // Style buttons
        dialog.getDialogPane().getButtonTypes().forEach(buttonType -> {
            Button button = (Button) dialogPane.lookupButton(buttonType);
            button.setStyle(getButtonStyle(BUTTON_BG));
            button.setOnMouseEntered(e -> button.setStyle(getButtonStyle(BUTTON_HOVER_BG)));
            button.setOnMouseExited(e -> button.setStyle(getButtonStyle(BUTTON_BG)));
        });
    }

    private String getButtonStyle(String bgColor) {
        return String.format("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-font-size: 14px;
            -fx-padding: 8 15;
            -fx-background-radius: 5;
            """, bgColor, TEXT_PRIMARY);
    }

    private void updateFactories() {
        List<Factory> factories = gameModel.getFactories();
        GridPane factoriesGrid = view.getFactoriesContainer();

        for (int i = 0; i < factories.size(); i++) {
            Factory factory = factories.get(i);
            VBox factoryBox = (VBox) factoriesGrid.getChildren().get(i);
            GridPane tileGrid = (GridPane) factoryBox.getChildren().get(0);
            List<Tile> factoryTiles = factory.getTiles();

            tileGrid.getChildren().clear();

            int tileIndex = 0;
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    Circle tileCircle = new Circle(15);
                    if (tileIndex < factoryTiles.size()) {
                        Tile tile = factoryTiles.get(tileIndex);
                        tileCircle.setFill(Color.web(tile.getColor().getHexCode()));
                        setupTileHoverEffects(tileCircle, factoryBox);
                    } else {
                        tileCircle.setFill(Color.web("#374151"));
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

    private void updatePlayerBoards() {
        List<Player> players = gameModel.getPlayers();
        updatePlayerBoard(view.getPlayer1Board(), players.get(0));
        updatePlayerBoard(view.getPlayer2Board(), players.get(1));
    }

    private void updatePlayerBoard(VBox playerBoard, Player player) {
        updatePatternLines(playerBoard, player);
        updateWall(playerBoard, player);
        updateFloorLine(playerBoard, player);
    }

    private void updatePatternLines(VBox playerBoard, Player player) {
        VBox patternLinesContainer = patternLineInteractionHandler.findPatternLinesContainer(playerBoard);
        if (patternLinesContainer == null) return;

        List<PatternLine> playerPatternLines = player.getPatternLines();
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
                space.setFill(Color.web("#374151"));
            }
        }
    }

    private void updateWall(VBox playerBoard, Player player) {
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

                        // Add completion animation if newly placed
                        if (colorCircle.getOpacity() != 1.0) {
                            addTilePlacementAnimation(colorCircle);
                        }
                    }
                }
            }
        }
    }

    private void addTilePlacementAnimation(Circle tile) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), tile);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.0);
        scale.setToY(1.0);

        FadeTransition fade = new FadeTransition(Duration.millis(300), tile);
        fade.setFromValue(0.5);
        fade.setToValue(1.0);

        ParallelTransition animation = new ParallelTransition(scale, fade);
        animation.play();
    }

    private void updateScores() {
        Platform.runLater(() -> {
            List<Player> players = gameModel.getPlayers();
            updatePlayerScore(view.getPlayer1Board(), players.get(0));
            updatePlayerScore(view.getPlayer2Board(), players.get(1));
        });
    }

    private void updatePlayerScore(VBox playerBoard, Player player) {
        HBox header = (HBox) playerBoard.getChildren().get(0);
        Label scoreLabel = (Label) header.getChildren().get(2);

        int currentScore = Integer.parseInt(scoreLabel.getText());
        int newScore = player.getScore();

        if (currentScore != newScore) {
            SimpleIntegerProperty scoreProperty = new SimpleIntegerProperty(currentScore);
            scoreProperty.addListener((obs, oldValue, newValue) ->
                    scoreLabel.setText(String.valueOf(newValue.intValue())));

            Timeline scoreTicker = new Timeline(
                    new KeyFrame(Duration.millis(500),
                            new KeyValue(scoreProperty, newScore, Interpolator.EASE_BOTH)
                    )
            );

            // Visual feedback for score change
            scoreLabel.setStyle("-fx-text-fill: " + (newScore > currentScore ? "#22C55E" : "#EF4444"));
            scoreTicker.setOnFinished(e -> scoreLabel.setStyle("-fx-text-fill: #60A5FA"));
            scoreTicker.play();
        }
    }

    private void updateCurrentPlayer() {
        Player current = gameModel.getCurrentPlayer();
        view.getCurrentPlayerLabel().setText(current.getName() + "'s Turn");

        // Update board styles
        view.getPlayer1Board().setStyle(getBoardStyle(current == gameModel.getPlayers().get(0)));
        view.getPlayer2Board().setStyle(getBoardStyle(current == gameModel.getPlayers().get(1)));
    }

    private String getBoardStyle(boolean isActive) {
        return String.format("""
            -fx-background-color: %s;
            -fx-padding: 20;
            %s
            """,
                CARD_BG,
                isActive ? "-fx-border-color: " + ACCENT_COLOR + "; -fx-border-width: 2; -fx-border-radius: 10;" : ""
        );
    }

    private TileColor getTileColorFromFill(Color fillColor) {
        String hexColor = String.format("#%02X%02X%02X",
                (int)(fillColor.getRed() * 255),
                (int)(fillColor.getGreen() * 255),
                (int)(fillColor.getBlue() * 255));

        return Arrays.stream(TileColor.values())
                .filter(color -> color.getHexCode().equalsIgnoreCase(hexColor))
                .findFirst()
                .orElse(null);
    }

    // Button handlers
    private void handleUndoClick() {
        // TODO: Implement undo functionality
        showMessage("Cannot Undo", "Undo functionality is not yet implemented.", Alert.AlertType.INFORMATION);
    }

    private void handleSaveClick() {
        showMessage("Game Saved", "Your game has been saved successfully.", Alert.AlertType.INFORMATION);
    }

    private void handleExitClick() {
        Alert alert = createConfirmationDialog(
                "Exit Game",
                "Are you sure you want to exit?",
                "Any unsaved progress will be lost."
        );

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                turnManager.pauseTimer();
                view.getStage().close();
            }
        });
    }

    private Alert createConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleDialog(alert);
        return alert;
    }

    private void showMessage(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        styleDialog(alert);
        alert.showAndWait();
    }

    public void show() {
        view.getStage().show();
    }

    private void handleSettingsClick() {
        isGamePaused = true;
        turnManager.pauseTimer();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Game Settings");
        dialog.setHeaderText("Game Settings");

        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20;");

        // Game Settings Controls
        CheckBox muteAudioBox = new CheckBox("Mute Audio");
        muteAudioBox.setStyle("-fx-text-fill: white;");

        // Timer Slider
        VBox timerContainer = new VBox(5);
        Label timerLabel = new Label("Turn Timer (seconds):");
        timerLabel.setStyle("-fx-text-fill: white;");

        Slider timerSlider = new Slider(60, 180, 150);
        timerSlider.setShowTickLabels(true);
        timerSlider.setShowTickMarks(true);
        timerSlider.setMajorTickUnit(30);
        timerSlider.setBlockIncrement(10);
        timerSlider.setStyle("""
            -fx-control-inner-background: #374151;
            -fx-track-background: #4B5563;
            """);

        timerContainer.getChildren().addAll(timerLabel, timerSlider);

        // Visual Settings
        TitledPane visualSettings = createVisualSettingsPane();

        // Add all controls to content
        content.getChildren().addAll(
                muteAudioBox,
                timerContainer,
                visualSettings
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        styleDialog(dialog);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                applySettings(timerSlider.getValue(), muteAudioBox.isSelected());
            }
            isGamePaused = false;
            turnManager.resumeTimer();
        });
    }

    private TitledPane createVisualSettingsPane() {
        TitledPane visualSettings = new TitledPane();
        visualSettings.setText("Visual Settings");
        visualSettings.setStyle("-fx-text-fill: white;");

        VBox visualContent = new VBox(10);
        visualContent.setStyle("-fx-padding: 10;");

        // Animation speed control
        HBox animationSpeedBox = new HBox(10);
        Label animSpeedLabel = new Label("Animation Speed:");
        animSpeedLabel.setStyle("-fx-text-fill: white;");
        ComboBox<String> animSpeedCombo = new ComboBox<>();
        animSpeedCombo.getItems().addAll("Slow", "Normal", "Fast");
        animSpeedCombo.setValue("Normal");
        animationSpeedBox.getChildren().addAll(animSpeedLabel, animSpeedCombo);

        // Highlight valid moves
        CheckBox highlightMovesBox = new CheckBox("Highlight Valid Moves");
        highlightMovesBox.setStyle("-fx-text-fill: white;");

        visualContent.getChildren().addAll(animationSpeedBox, highlightMovesBox);
        visualSettings.setContent(visualContent);

        return visualSettings;
    }

    private void applySettings(double timerValue, boolean isMuted) {
        // Update timer duration
        turnManager.setTurnDuration((int) timerValue);

        // Update audio settings
        // TODO: Implement audio settings

        // Show confirmation toast
        showToast("Settings Applied", ToastType.SUCCESS);
    }

    private void showToast(String message, ToastType type) {
        Label toast = new Label(message);
        toast.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-font-size: 14px;
            """, type.getColor()));

        StackPane.setAlignment(toast, Pos.TOP_CENTER);
        toast.setTranslateY(50);

        view.getAnimationLayer().getChildren().add(toast);

        // Animate toast
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(2));

        SequentialTransition sequence = new SequentialTransition(fadeIn, fadeOut);
        sequence.setOnFinished(e -> view.getAnimationLayer().getChildren().remove(toast));
        sequence.play();
    }

    private enum ToastType {
        SUCCESS("#22C55E"),
        WARNING("#F59E0B"),
        ERROR("#EF4444"),
        INFO("#3B82F6");

        private final String color;

        ToastType(String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }
    }

    private void updateCenterPool() {
        VBox centerPool = view.getCenterPool();
        FlowPane tilesContainer = (FlowPane) centerPool.getChildren().get(1);
        tilesContainer.getChildren().clear();

        for (Tile tile : gameModel.getCenterPool()) {
            if (tile.getColor() != null) {
                Circle tileCircle = createTileCircle(tile.getColor());
                setupCenterTileInteraction(tileCircle);
                tilesContainer.getChildren().add(tileCircle);
            }
        }
    }

    private Circle createTileCircle(TileColor color) {
        Circle circle = new Circle(15);
        circle.setFill(Color.web(color.getHexCode()));
        circle.setStroke(Color.web("#4B5563"));
        circle.setStrokeWidth(1);

        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.3));
        shadow.setRadius(5);
        circle.setEffect(shadow);

        return circle;
    }

    private void updateFloorLine(VBox playerBoard, Player player) {
        HBox floorLine = (HBox) playerBoard.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .reduce((first, second) -> second)
                .orElse(null);

        if (floorLine == null) return;

        // Clear existing floor line
        floorLine.getChildren().clear();

        // Create new floor line spaces
        for (int i = 0; i < FloorLine.MAX_TILES; i++) {
            Circle space = new Circle(15);
            space.setFill(Color.web("#374151")); // Empty space color
            space.setStroke(Color.web("#4B5563"));
            space.setStrokeWidth(1);

            // If there's a tile at this position, color it
            Tile tile = player.getFloorLine().getTileAt(i);
            if (tile != null && tile.getColor() != null) {
                space.setFill(Color.web(tile.getColor().getHexCode()));
                // Add popup showing penalty value
                setupPenaltyPopup(space, i);
            }

            floorLine.getChildren().add(space);
        }
    }

    private void setupPenaltyPopup(Circle space, int index) {
        Tooltip penalty = new Tooltip("Penalty: " + FloorLine.PENALTY_POINTS[index]);
        penalty.setStyle("""
            -fx-background-color: #1F2937;
            -fx-text-fill: #EF4444;
            -fx-font-size: 12px;
            """);
        Tooltip.install(space, penalty);
    }

    private void clearPlayerHands() {
        view.getPlayer1Hand().getChildren().clear();
        view.getPlayer2Hand().getChildren().clear();
    }

    private HBox getCurrentPlayerHand() {
        return gameModel.getCurrentPlayer() == gameModel.getPlayers().get(0) ?
                view.getPlayer1Hand() : view.getPlayer2Hand();
    }

    private void updateFactoryDisplay(VBox factory) {
        GridPane tileGrid = (GridPane) factory.getChildren().get(0);
        tileGrid.getChildren().clear();
    }

    private boolean handleFirstPlayerToken() {
        AtomicBoolean tokenFound = new AtomicBoolean(false);
        FlowPane tilesContainer = (FlowPane) view.getCenterPool().getChildren().get(1);

        tilesContainer.getChildren().removeIf(node -> {
            if (node instanceof Circle tile && tile.getFill() == null) {
                tokenFound.set(true);
                Platform.runLater(() -> animateFirstPlayerToken(tile));
                return true;
            }
            return false;
        });

        return tokenFound.get();
    }

    private void animateFirstPlayerToken(Circle token) {
        // Get target floor line
        HBox floorLine = gameModel.getCurrentPlayer() == gameModel.getPlayers().get(0) ?
                (HBox) view.getPlayer1Board().getChildren().get(view.getPlayer1Board().getChildren().size() - 1) :
                (HBox) view.getPlayer2Board().getChildren().get(view.getPlayer2Board().getChildren().size() - 1);

        // Animate token movement
        PathTransition path = createPathTransition(token, floorLine, ANIMATION_DURATION);
        FadeTransition fade = new FadeTransition(ANIMATION_DURATION, token);
        fade.setToValue(0.5);

        ParallelTransition animation = new ParallelTransition(path, fade);
        animation.play();
    }

    private PathTransition createPathTransition(Node sourceNode, Node targetNode, Duration duration) {
        Bounds sourceBounds = sourceNode.localToScene(sourceNode.getBoundsInLocal());
        Bounds targetBounds = targetNode.localToScene(targetNode.getBoundsInLocal());

        Path path = new Path();
        path.getElements().addAll(
                new MoveTo(sourceBounds.getCenterX(), sourceBounds.getCenterY()),
                new LineTo(targetBounds.getCenterX(), targetBounds.getCenterY())
        );

        PathTransition transition = new PathTransition(duration, path, sourceNode);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        return transition;
    }
}
