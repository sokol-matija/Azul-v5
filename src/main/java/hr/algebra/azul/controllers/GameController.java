package hr.algebra.azul.controllers;

import hr.algebra.azul.helper.PatternLineInteractionHandler;
import hr.algebra.azul.helper.TileAnimationManager;
import hr.algebra.azul.helper.TurnManager;
import hr.algebra.azul.helper.WallTilingManager;
import hr.algebra.azul.models.*;
import hr.algebra.azul.view.ModernTwoPlayerGameView;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.List;

public abstract class GameController {
    protected ModernTwoPlayerGameView view;
    protected GameModel gameModel;
    protected Stage primaryStage;
    protected TileAnimationManager animationManager;
    protected TurnManager turnManager;
    protected WallTilingManager wallTilingManager;
    protected PatternLineInteractionHandler patternLineInteractionHandler;
    protected boolean isGamePaused;

    public GameController(ModernTwoPlayerGameView view, Stage primaryStage) {
        this.view = view;
        this.primaryStage = primaryStage;
        this.gameModel = new GameModel(2);
        initializeController();
    }

    protected void initializeController() {
        setupManagers();
        setupEventHandlers();
        setupWindowHandlers();
        updateEntireView();
    }

    private void setupManagers() {
        this.turnManager = new TurnManager(gameModel, view, view.getTimerLabel());
        this.wallTilingManager = new WallTilingManager(view, gameModel);
        this.patternLineInteractionHandler = new PatternLineInteractionHandler(view, gameModel, turnManager);
        this.animationManager = new TileAnimationManager(view.getAnimationLayer());

        turnManager.setRoundEndCallback(this::handleRoundEnd);
    }

    private void setupEventHandlers() {
        setupButtonHandlers();
        setupFactoryClickHandlers();
        setupPatternLineHandlers();
        setupCenterPoolClickHandlers();
        setupKeyboardShortcuts();
    }

    private void setupButtonHandlers() {
        view.getUndoButton().setOnAction(e -> handleUndoClick());
        view.getSaveButton().setOnAction(e -> handleSaveClick());
        view.getExitButton().setOnAction(e -> handleExitClick());
        view.getSettingsButton().setOnAction(e -> handleSettingsClick());
        view.getEndTurnButton().setOnAction(e -> handleEndTurn());
        view.getEndRoundButton().setOnAction(e -> handleRoundEnd());
    }

    protected void setupFactoryClickHandlers() {
        GridPane factoriesGrid = view.getFactoriesContainer();
        for (int i = 0; i < factoriesGrid.getChildren().size(); i++) {
            final int factoryIndex = i;
            VBox factory = (VBox) factoriesGrid.getChildren().get(i);
            GridPane tileGrid = (GridPane) factory.getChildren().get(0);

            for (Node node : tileGrid.getChildren()) {
                if (node instanceof Circle tile) {
                    tile.setOnMouseClicked(e -> handleFactoryClick(tile, factory, factoryIndex));
                }
            }
        }
    }

    protected void setupPatternLineHandlers() {
        setupPlayerPatternLines(view.getPlayer1Board());
        setupPlayerPatternLines(view.getPlayer2Board());
    }

    protected void setupCenterPoolClickHandlers() {
        VBox centerPool = view.getCenterPool();
        FlowPane tilesContainer = (FlowPane) centerPool.getChildren().get(1);

        tilesContainer.getChildren().addListener((ListChangeListener<Node>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Node node : change.getAddedSubList()) {
                        if (node instanceof Circle tile) {
                            setupCenterTileInteraction(tile, centerPool);
                        }
                    }
                }
            }
        });
    }

    private void setupWindowHandlers() {
        view.getStage().setOnCloseRequest(e -> {
            e.consume();
            handleExitClick();
        });
    }

    protected void setupKeyboardShortcuts() {
        view.getScene().setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case S -> {
                    if (e.isControlDown()) handleSaveClick();
                }
                case Z -> {
                    if (e.isControlDown()) handleUndoClick();
                }
                case ESCAPE -> handlePauseGame();
                case SPACE -> handleEndTurn();
            }
        });
    }

    // Abstract methods that must be implemented by child classes
    protected abstract void handleFactoryClick(Circle clickedTile, VBox factory, int factoryIndex);
    protected abstract void handleEndTurn();
    protected abstract void handleGameEnd();

    protected void handleUndoClick() {
        if (!gameModel.canUndo()) {
            showMessage("Cannot Undo", "No moves available to undo.", Alert.AlertType.INFORMATION);
            return;
        }
        gameModel.undo();
        updateEntireView();
    }

    protected void handleSaveClick() {
        showMessage("Game Saved", "Your game has been saved successfully.", Alert.AlertType.INFORMATION);
    }

    protected void handleExitClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Game");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Any unsaved progress will be lost.");

        styleDialog(alert);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Platform.exit();
            }
        });
    }

    protected void handleSettingsClick() {
        isGamePaused = true;
        turnManager.pauseTimer();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Game Settings");
        dialog.setHeaderText("Settings");

        VBox content = new VBox(10);
        content.setStyle("-fx-spacing: 10; -fx-padding: 20;");

        // Add settings controls
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
                turnManager.timeRemainingProperty().set((int) timerSlider.getValue());
            }
            isGamePaused = false;
            turnManager.resumeTimer();
        });
    }

    protected void handlePauseGame() {
        isGamePaused = !isGamePaused;
        if (isGamePaused) {
            turnManager.pauseTimer();
            showPauseDialog();
        } else {
            turnManager.resumeTimer();
        }
    }

    protected void handleRoundEnd() {
        wallTilingManager.processWallTiling();
        gameModel.initializeGame();
        updateEntireView();
        turnManager.resetTimer();
        Platform.runLater(this::showRoundEndDialog);
    }

    protected void showPauseDialog() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Game Paused");
        alert.setHeaderText("Game Paused");
        alert.setContentText("Press ESC to resume");

        // Custom buttons
        ButtonType resumeButton = new ButtonType("Resume");
        ButtonType saveAndQuitButton = new ButtonType("Save & Quit");
        alert.getButtonTypes().addAll(resumeButton, saveAndQuitButton);

        styleDialog(alert);

        alert.showAndWait().ifPresent(response -> {
            if (response == saveAndQuitButton) {
                handleSaveClick();
                view.getStage().close();
            } else {
                handlePauseGame(); // Resume
            }
        });
    }

    protected void showRoundEndDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Round Complete");
        dialog.setHeaderText("Round Summary");

        VBox content = new VBox(15);
        content.setStyle("-fx-spacing: 15; -fx-padding: 20;");

        // Add round summary for each player
        for (Player player : gameModel.getPlayers()) {
            VBox playerSummary = new VBox(5);
            playerSummary.setStyle("-fx-spacing: 5;");

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

    protected void showMessage(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);

        styleDialog(alert);
        alert.showAndWait();
    }

    protected void styleDialog(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1F2937;");

        dialogPane.lookup(".header-panel").setStyle("""
            -fx-background-color: #111827;
            -fx-padding: 20;
            """);

        Label headerText = (Label) dialogPane.lookup(".header-panel > .label");
        if (headerText != null) {
            headerText.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                """);
        }

        Label contentText = (Label) dialogPane.lookup(".content.label");
        if (contentText != null) {
            contentText.setStyle("""
                -fx-text-fill: #9CA3AF;
                -fx-font-size: 14px;
                """);
        }

        // Style buttons
        dialogPane.getButtonTypes().forEach(buttonType -> {
            Button button = (Button) dialogPane.lookupButton(buttonType);
            button.setStyle("""
                -fx-background-color: #3B82F6;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 8 15;
                -fx-background-radius: 5;
                """);
        });
    }

    protected void updateEntireView() {
        Platform.runLater(() -> {
            updateFactories();
            updateCenterPool();
            updatePlayerBoards();
            updateCurrentPlayerDisplay();
        });
    }

    protected void updateFactories() {
        // Implementation depends on specific game type
    }

    protected void updateCenterPool() {
        VBox centerPool = view.getCenterPool();
        FlowPane tilesContainer = (FlowPane) centerPool.getChildren().get(1);
        tilesContainer.getChildren().clear();

        List<Tile> centerTiles = gameModel.getCenterPool();
        for (Tile tile : centerTiles) {
            Circle tileCircle = new Circle(15);
            tileCircle.setFill(Color.web(tile.getColor().getHexCode()));
            tileCircle.setStroke(Color.web("#4B5563"));
            tilesContainer.getChildren().add(tileCircle);
        }
    }

    protected void updatePlayerBoards() {
        updatePlayerBoard(view.getPlayer1Board(), gameModel.getPlayers().get(0));
        updatePlayerBoard(view.getPlayer2Board(), gameModel.getPlayers().get(1));
    }

    private void updatePlayerBoard(VBox board, Player player) {
        updatePatternLines(board, player);
        updateWall(board, player);
        updateFloorLine(board, player);
        updateScore(board, player);
    }

    protected void updateCurrentPlayerDisplay() {
        String currentPlayerName = gameModel.getCurrentPlayer().getName();
        view.getCurrentPlayerLabel().setText(currentPlayerName + "'s Turn");
    }

    protected boolean isRoundComplete() {
        return gameModel.getFactories().stream().allMatch(Factory::isEmpty) &&
                gameModel.getCenterPool().isEmpty();
    }

    public void show() {
        view.getStage().show();
    }

    // Utility methods for child classes
    protected TileColor getTileColorFromFill(Color fillColor) {
        String hexColor = String.format("#%02X%02X%02X",
                (int)(fillColor.getRed() * 255),
                (int)(fillColor.getGreen() * 255),
                (int)(fillColor.getBlue() * 255));

        for (TileColor color : TileColor.values()) {
            if (color.getHexCode().equalsIgnoreCase(hexColor)) {
                return color;
            }
        }
        return null;
    }
}