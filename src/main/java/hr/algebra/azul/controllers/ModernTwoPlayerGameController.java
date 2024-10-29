package hr.algebra.azul.controllers;

import hr.algebra.azul.models.GameState;
import hr.algebra.azul.view.ModernTwoPlayerGameView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ModernTwoPlayerGameController {
    private ModernTwoPlayerGameView view;
    private GameState gameState;
    private Stage primaryStage;
    private Timeline timer;
    private int timeRemaining = 150; // 2:30 in seconds
    private int currentPlayer = 1;
    private boolean isGamePaused = false;

    // Style constants
    private static final String DIALOG_BG = "#1F2937";
    private static final String DIALOG_HEADER_BG = "#111827";
    private static final String BUTTON_BG = "#3B82F6";
    private static final String BUTTON_HOVER_BG = "#2563EB";
    private static final String TEXT_PRIMARY = "white";
    private static final String TEXT_SECONDARY = "#9CA3AF";

    public ModernTwoPlayerGameController(ModernTwoPlayerGameView view, Stage primaryStage) {
        this.view = view;
        this.primaryStage = primaryStage;
        this.gameState = new GameState();
        initializeController();
    }

    private void initializeController() {
        setupButtonHandlers();
        view.getStage().setOnCloseRequest(e -> {
            e.consume();
            handleExitClick();
        });
        initializeGame();
        initializeTimer();
        setupFactoryClickHandlers();
    }

    private void setupButtonHandlers() {
        view.getUndoButton().setOnAction(e -> handleUndoClick());
        view.getSaveButton().setOnAction(e -> handleSaveClick());
        view.getExitButton().setOnAction(e -> handleExitClick());
        view.getSettingsButton().setOnAction(e -> handleSettingsClick());
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

        // Style buttons
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

    private void initializeGame() {
        updateActivePlayer(1);
    }

    private void setupFactoryClickHandlers() {
        view.getFactoriesContainer().getChildren().forEach(factory -> {
            factory.setOnMouseClicked(e -> handleFactoryClick(factory));
        });
    }

    private void handleFactoryClick(javafx.scene.Node factory) {
        if (isGamePaused) return;
        showTileSelectionDialog(factory);
    }

    private void showTileSelectionDialog(javafx.scene.Node factory) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Select Tiles");
        dialog.setHeaderText("Choose tiles to take");

        VBox content = new VBox(10);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        styleDialog(dialog);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                handleTileSelection();
            }
        });
    }

    private void handleTileSelection() {
        if (!isValidMove()) {
            showInvalidMoveDialog();
            return;
        }
        processTileMove();
        if (isRoundComplete()) {
            handleRoundEnd();
        } else {
            switchTurns();
        }
    }

    private boolean isValidMove() {
        return true; // TODO: Implement move validation
    }

    private void processTileMove() {
        // TODO: Implement tile movement logic
    }

    private boolean isRoundComplete() {
        return false; // TODO: Implement round completion check
    }

    private void handleRoundEnd() {
        calculateScores();
        showRoundSummaryDialog();
        if (isGameEnd()) {
            handleGameEnd();
        } else {
            startNewRound();
        }
    }

    private void calculateScores() {
        // TODO: Implement score calculation
    }

    private void switchTurns() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        updateActivePlayer(currentPlayer);
        resetTimer();
    }

    private void updateActivePlayer(int playerNumber) {
        view.getCurrentPlayerLabel().setText("Player " + playerNumber + "'s Turn");
        view.getPlayer1Board().setStyle(getPlayerBoardStyle(playerNumber == 1));
        view.getPlayer2Board().setStyle(getPlayerBoardStyle(playerNumber == 2));
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

    private void initializeTimer() {
        timer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateTimer())
        );
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void updateTimer() {
        if (timeRemaining > 0) {
            timeRemaining--;
            int minutes = timeRemaining / 60;
            int seconds = timeRemaining % 60;
            Platform.runLater(() ->
                    view.getTimerLabel().setText(
                            String.format("⏱ %02d:%02d", minutes, seconds)
                    )
            );

            if (timeRemaining == 30) {
                showTimeWarning();
            }
        } else {
            timer.stop();
            handleTimeOut();
        }
    }

    private void resetTimer() {
        timeRemaining = 150;
        view.getTimerLabel().setStyle("-fx-text-fill: #9CA3AF;");
    }

    private void showTimeWarning() {
        view.getTimerLabel().setStyle("-fx-text-fill: #EF4444;");
    }

    private void handleTimeOut() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Time Out");
        alert.setHeaderText("Turn Time Out");
        alert.setContentText("Your turn has ended due to time out.");

        styleDialog(alert);
        alert.showAndWait();
        switchTurns();
    }

    private void handleUndoClick() {
        if (!gameState.canUndo()) {
            showMessage("Cannot Undo", "No moves available to undo.", Alert.AlertType.INFORMATION);
            return;
        }
        gameState.undo();
        updateGameView();
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
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        styleDialog(dialog);

        dialog.showAndWait().ifPresent(response -> {
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

    private void showRoundSummaryDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Round Complete");
        dialog.setHeaderText("Round Summary");

        VBox content = new VBox(10);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        styleDialog(dialog);
        dialog.showAndWait();
    }

    private boolean isGameEnd() {
        return false; // TODO: Implement game end check
    }

    private void handleGameEnd() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Game Over");
        dialog.setHeaderText("Final Scores");

        VBox content = new VBox(10);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        styleDialog(dialog);

        dialog.showAndWait().ifPresent(response -> {
            view.getStage().close();
        });
    }

    private void startNewRound() {
        resetTimer();
        updateGameView();
    }

    private void updateGameView() {
        // TODO: Update all UI elements based on current game state
    }

    public void show() {
        view.getStage().show();
    }
}