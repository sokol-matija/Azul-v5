package hr.algebra.azul.controllers;

import hr.algebra.azul.models.GameState;
import hr.algebra.azul.view.ModernGameView;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ModernGameController {
    private ModernGameView view;
    private GameState gameState;
    private Stage primaryStage;
    private Timeline timer;
    private int timeRemaining = 150; // 2:30 in seconds
    private int currentPlayer = 1;
    private boolean isGamePaused = false;

    public ModernGameController(ModernGameView view, Stage primaryStage) {
        this.view = view;
        this.primaryStage = primaryStage;
        this.gameState = new GameState();
        initializeController();
    }

    private void initializeController() {
        // Set up button handlers
        view.getExitButton().setOnAction(e -> handleExitClick());
        view.getSaveButton().setOnAction(e -> handleSaveClick());
        view.getUndoButton().setOnAction(e -> handleUndoClick());

        // Set up window close request
        view.getStage().setOnCloseRequest(e -> {
            e.consume();
            handleExitClick();
        });

        // Initialize game timer
        initializeTimer();

        // Set up factory tile click handlers
        setupFactoryClickHandlers();

        // Set up keyboard shortcuts
        setupKeyboardShortcuts();
    }

    private void handleExitClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Game");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Any unsaved progress will be lost.");

        styleDialog(alert);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (timer != null) {
                    timer.stop();
                }
                view.getStage().close();
            }
        });
    }

    private void handleSaveClick() {
        // Implement save game functionality
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Save Game");
        alert.setHeaderText("Game Saved");
        alert.setContentText("Your game has been saved successfully.");

        styleDialog(alert);

        alert.showAndWait();
    }

    private void handleUndoClick() {
        // Implement undo functionality
        if (!gameState.canUndo()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Undo");
            alert.setHeaderText("Cannot Undo");
            alert.setContentText("No moves available to undo.");

            styleDialog(alert);

            alert.showAndWait();
            return;
        }

        // Perform undo operation
        //gameState.undo();
        updateGameView();
    }

    private void updateGameView() {
        // Update the game view based on current state
        // This method should be called after any state changes
        updateScores();
        updatePlayerBoards();
        updateFactories();
    }

    private void updatePlayerBoards() {
        // Implement player board update logic
    }

    private void updateFactories() {
        // Implement factories update logic
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
            Platform.runLater(() -> {
                if (view != null && view.getTimeLabel() != null) {
                    view.getTimeLabel().setText(String.format("⏱ %02d:%02d", minutes, seconds));
                }
            });

            // Warning when time is low (30 seconds)
            if (timeRemaining == 30) {
                Platform.runLater(this::showTimeWarning);
            }
        } else {
            timer.stop();
            Platform.runLater(this::handleTimeOut);
        }
    }

    private void showTimeWarning() {
        // Create a small popup or flash the timer to warn the player
        view.getTimeLabel().setStyle("""
        -fx-text-fill: #EF4444;
        -fx-font-weight: bold;
        """);

        // Create a flash animation
        Timeline flash = new Timeline(
                new KeyFrame(Duration.seconds(0.5), evt ->
                        view.getTimeLabel().setStyle("-fx-text-fill: #EF4444;")),
                new KeyFrame(Duration.seconds(1.0), evt ->
                        view.getTimeLabel().setStyle("-fx-text-fill: #9CA3AF;"))
        );
        flash.setCycleCount(3);
        flash.play();
    }

    private void handleTimeOut() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Time's Up!");
        alert.setHeaderText("Turn Time Out");
        alert.setContentText("Your turn has ended due to time out.");

        styleDialog(alert);

        alert.showAndWait().ifPresent(response -> {
            timeRemaining = 150; // Reset timer for next turn
            updateGameState(); // Move to next player
        });
    }


    private void setupFactoryClickHandlers() {
        view.getFactoriesGrid().getChildren().forEach(factory -> {
            factory.setOnMouseClicked(e -> handleFactoryClick(factory));
        });
    }

    private void handleFactoryClick(javafx.scene.Node factory) {
        if (isGamePaused) return;

        // TODO: Implement tile selection logic
        showTileSelectionDialog(factory);
    }

    private void showTileSelectionDialog(javafx.scene.Node factory) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Select Tiles");
        dialog.setHeaderText("Choose tiles to take");

        // Style the dialog
        styleDialog(dialog);

        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // TODO: Add tile selection controls to dialog

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Handle tile selection
                updateGameState();
            }
        });
    }

    private void setupKeyboardShortcuts() {
        view.getScene().setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case S -> {
                    if (e.isControlDown()) {
                        handleSaveClick();
                    }
                }
                case Z -> {
                    if (e.isControlDown()) {
                        handleUndoClick();
                    }
                }
                case ESCAPE -> handlePauseGame();
                case SPACE -> handleEndTurn();
            }
        });
    }

    private void updateGameState() {
        // Animate turn change
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), view.getPlayerBoard());
        tt.setFromX(0);
        tt.setToX(10);
        tt.setAutoReverse(true);
        tt.setCycleCount(2);
        tt.play();

        // Update current player
        currentPlayer = (currentPlayer % 4) + 1;
        view.getTurnLabel().setText("Player " + currentPlayer + "'s Turn");

        // Reset timer
        timeRemaining = 150;

        // Update score
        updateScores();
    }

    private void updateScores() {
        // TODO: Implement score calculation
        // For now, just animate score changes
//        view.getScoreBoard().getChildren().forEach(node -> {
//            if (node instanceof HBox scoreEntry) {
//                Label scoreLabel = (Label) scoreEntry.getChildren().get(2);
//                FadeTransition ft = new FadeTransition(Duration.millis(200), scoreLabel);
//                ft.setFromValue(0.5);
//                ft.setToValue(1.0);
//                ft.play();
//            }
//        });
    }

    private void handlePauseGame() {
        isGamePaused = !isGamePaused;
        if (isGamePaused) {
            timer.pause();
            showPauseDialog();
        } else {
            timer.play();
        }
    }

    private void showPauseDialog() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Game Paused");
        alert.setHeaderText("Game Paused");
        alert.setContentText("Press ESC to resume");

        styleDialog(alert);

        // Custom buttons
        ButtonType resumeButton = new ButtonType("Resume");
        ButtonType saveAndQuitButton = new ButtonType("Save & Quit");
        alert.getButtonTypes().addAll(resumeButton, saveAndQuitButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == saveAndQuitButton) {
                handleSaveClick();
                view.getStage().close();
            } else {
                handlePauseGame(); // Resume
            }
        });
    }

    private void handleEndTurn() {
        if (isGamePaused) return;

        // Validate turn
        if (!isTurnValid()) {
            showInvalidTurnDialog();
            return;
        }

        updateGameState();
    }

    private boolean isTurnValid() {
        // TODO: Implement turn validation logic
        return true;
    }

    private void showInvalidTurnDialog() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Turn");
        alert.setHeaderText("Invalid Move");
        alert.setContentText("You must place at least one tile before ending your turn.");

        styleDialog(alert);

        alert.showAndWait();
    }

    private void showTimeoutDialog() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Time Out");
        alert.setHeaderText("Turn Time Out");
        alert.setContentText("Your turn has ended due to time out.");

        styleDialog(alert);

        alert.showAndWait();
        handleEndTurn();
    }

    private void styleDialog(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/styles/dialog.css").toExternalForm()
        );
        dialogPane.getScene().getRoot().setStyle("-fx-background-color: #1F2937;");
    }


    public void show() {
        view.getStage().show();
    }
}