package hr.algebra.azul.helper;

import hr.algebra.azul.models.Factory;
import hr.algebra.azul.models.GameModel;
import hr.algebra.azul.models.Player;
import hr.algebra.azul.view.ModernTwoPlayerGameView;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;

public class TurnManager {
    private final GameModel gameModel;
    private final ModernTwoPlayerGameView view;
    private final IntegerProperty timeRemaining;
    private Timeline timer;
    private boolean isGamePaused;
    private Label timerLabel; // Store reference to timer label

    // Style constants
    private static final String ACTIVE_BOARD_STYLE = """
        -fx-background-color: #1F2937;
        -fx-padding: 20;
        -fx-border-color: #4F46E5;
        -fx-border-width: 2;
        -fx-border-radius: 10;
        """;

    private static final String INACTIVE_BOARD_STYLE = """
        -fx-background-color: #1F2937;
        -fx-padding: 20;
        """;

    public TurnManager(GameModel gameModel, ModernTwoPlayerGameView view, Label timerLabel) {
        this.gameModel = gameModel;
        this.view = view;
        this.timerLabel = timerLabel;
        this.timeRemaining = new SimpleIntegerProperty(150); // 2:30 in seconds
        this.isGamePaused = false;
        initializeTimer();
    }

    private void initializeTimer() {
        timer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateTimer())
        );
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void updateTimer() {
        if (timeRemaining.get() > 0) {
            Platform.runLater(() -> {
                timeRemaining.set(timeRemaining.get() - 1);
                updateTimerDisplay();

                if (timeRemaining.get() == 30) {
                    showTimeWarning();
                }
            });
        } else {
            timer.stop();
            Platform.runLater(this::handleTimeOut);
        }
    }

    private void updateTimerDisplay() {
        int minutes = timeRemaining.get() / 60;
        int seconds = timeRemaining.get() % 60;
        Platform.runLater(() ->
                timerLabel.setText(String.format("⏱ %02d:%02d", minutes, seconds))
        );
    }

    public void handleEndTurn() {
        if (isGamePaused) return;

        // Check if the player has tiles in hand
        HBox currentHand = getCurrentPlayerHand();
        if (!currentHand.getChildren().isEmpty()) {
            showInvalidTurnDialog();
            return;
        }

        // Check if round is complete
        if (isRoundComplete()) {
            handleRoundEnd();
        } else {
            switchToNextPlayer();
            showTurnChangeNotification();
        }
    }

    private void switchToNextPlayer() {
        Player currentPlayer = gameModel.getCurrentPlayer();
        List<Player> players = gameModel.getPlayers();
        int currentIndex = players.indexOf(currentPlayer);
        Player nextPlayer = players.get((currentIndex + 1) % 2);

        // Update game model's current player
        gameModel.setPlayerTurn(nextPlayer); // Changed from setCurrentPlayer to setPlayerTurn

        // Reset timer
        resetTimer();

        // Update UI
        Platform.runLater(() -> {
            view.getCurrentPlayerLabel().setText(nextPlayer.getName() + "'s Turn");
            updateBoardStyles(nextPlayer == players.get(0));
            animateTurnChange(nextPlayer == players.get(0));
        });
    }

    private void updateBoardStyles(boolean isFirstPlayerActive) {
        view.getPlayer1Board().setStyle(isFirstPlayerActive ? ACTIVE_BOARD_STYLE : INACTIVE_BOARD_STYLE);
        view.getPlayer2Board().setStyle(isFirstPlayerActive ? INACTIVE_BOARD_STYLE : ACTIVE_BOARD_STYLE);
    }

    private void showTurnChangeNotification() {
        Label notification = new Label(gameModel.getCurrentPlayer().getName() + "'s Turn");
        notification.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-font-size: 16px;
            """);

        StackPane.setAlignment(notification, Pos.TOP_CENTER);
        notification.setTranslateY(100);

        view.getAnimationLayer().getChildren().add(notification);

        // Animations
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), notification);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), notification);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(1.5));

        SequentialTransition sequence = new SequentialTransition(fadeIn, fadeOut);
        sequence.setOnFinished(e -> view.getAnimationLayer().getChildren().remove(notification));
        sequence.play();
    }

    private void animateTurnChange(boolean isFirstPlayer) {
        double sceneWidth = view.getStage().getWidth();
        double sceneHeight = view.getStage().getHeight();

        Rectangle indicator = new Rectangle(sceneWidth, 4);
        indicator.setFill(Color.web("#3B82F6"));
        indicator.setTranslateY(isFirstPlayer ? -2 : sceneHeight - 2);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#3B82F6"));
        glow.setRadius(10);
        indicator.setEffect(glow);

        view.getAnimationLayer().getChildren().add(indicator);

        TranslateTransition slide = new TranslateTransition(Duration.millis(600), indicator);
        slide.setFromX(-sceneWidth);
        slide.setToX(sceneWidth);
        slide.setOnFinished(e -> view.getAnimationLayer().getChildren().remove(indicator));
        slide.play();
    }

    public void resetTimer() {
        timeRemaining.set(150);
        if (timer != null) {
            timer.stop();
        }
        timer.play();
        timerLabel.setStyle("-fx-text-fill: #9CA3AF;");
        updateTimerDisplay();
    }

    public void pauseTimer() {
        timer.pause();
        isGamePaused = true;
    }

    public void resumeTimer() {
        timer.play();
        isGamePaused = false;
    }

    private void showTimeWarning() {
        timerLabel.setStyle("-fx-text-fill: #EF4444;");
    }

    private void handleTimeOut() {
        showTimeOutDialog();
        handleEndTurn();
    }

    private HBox getCurrentPlayerHand() {
        return gameModel.getCurrentPlayer() == gameModel.getPlayers().get(0)
                ? view.getPlayer1Hand()
                : view.getPlayer2Hand();
    }

    private boolean isRoundComplete() {
        // Check if all factories are empty
        boolean factoriesEmpty = true;
        List<Factory> factories = gameModel.getFactories();
        for (Factory factory : factories) {
            if (!factory.isEmpty()) {
                factoriesEmpty = false;
                break;
            }
        }

        // Check if center is empty
        boolean centerEmpty = gameModel.getCenterPool().isEmpty();

        return factoriesEmpty && centerEmpty;
    }

    public void handleRoundEnd() {
        // This method should be implemented in the game controller
        if (roundEndCallback != null) {
            roundEndCallback.run();
        }
    }

    private void showInvalidTurnDialog() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Turn");
        alert.setHeaderText("Cannot End Turn");
        alert.setContentText("You must place all selected tiles before ending your turn.");
        styleDialog(alert);
        alert.showAndWait();
    }

    private void showTimeOutDialog() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Time Out");
        alert.setHeaderText("Turn Time Out");
        alert.setContentText("Your turn has ended due to time out.");
        styleDialog(alert);
        alert.showAndWait();
    }

    private void styleDialog(Alert alert) {
        alert.getDialogPane().setStyle("""
            -fx-background-color: #1F2937;
            """);

        // Style header
        alert.getDialogPane().lookup(".header-panel").setStyle("""
            -fx-background-color: #111827;
            """);

        Label headerText = (Label) alert.getDialogPane().lookup(".header-panel > .label");
        if (headerText != null) {
            headerText.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                """);
        }

        Label contentText = (Label) alert.getDialogPane().lookup(".content.label");
        if (contentText != null) {
            contentText.setStyle("""
                -fx-text-fill: #9CA3AF;
                -fx-font-size: 14px;
                """);
        }
    }

    // Callback for round end handling
    private Runnable roundEndCallback;
    public void setRoundEndCallback(Runnable callback) {
        this.roundEndCallback = callback;
    }

    public boolean isGamePaused() {
        return isGamePaused;
    }

    public IntegerProperty timeRemainingProperty() {
        return timeRemaining;
    }
}