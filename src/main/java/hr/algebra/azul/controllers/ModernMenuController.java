package hr.algebra.azul.controllers;

import hr.algebra.azul.models.GameState;
import hr.algebra.azul.view.ModernLobbyView;
import hr.algebra.azul.view.ModernMenuView;
import hr.algebra.azul.view.ModernTwoPlayerGameView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;

public class ModernMenuController {
    private ModernMenuView view;
    private GameState gameState;
    private Stage primaryStage;

    public ModernMenuController(ModernMenuView view, Stage primaryStage) {
        this.view = view;
        this.primaryStage = primaryStage;
        this.gameState = new GameState();
        initializeController();
    }

    private void initializeController() {
        // Set up button click handlers with smooth transitions
        view.getSinglePlayerButton().setOnAction(e -> handleSinglePlayerClick());
        view.getMultiPlayerButton().setOnAction(e -> {
            try {
                handleMultiplayerClick();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        view.getOptionsButton().setOnAction(e -> handleOptionsClick());
        view.getExitButton().setOnAction(e -> handleExitClick());

        // Set up window close request
        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            handleExitClick();
        });
    }

    private void handleSinglePlayerClick() {
        gameState.setCurrentScreen("SINGLE_PLAYER");
        ModernTwoPlayerGameView gameView = new ModernTwoPlayerGameView();
        ModernTwoPlayerGameController gameController =
                new ModernTwoPlayerGameController(gameView, primaryStage);
        gameController.show();
    }

    private void handleMultiplayerClick() throws IOException {
        ModernLobbyView lobbyView = new ModernLobbyView();
        LobbyController lobbyController = new LobbyController(lobbyView);

        // Fade out menu and show lobby
        view.getStage().hide();
        lobbyController.show();

        // Handle lobby window close
        lobbyView.getStage().setOnCloseRequest(e -> {
            view.getStage().show();
        });
    }

    private void handleOptionsClick() {
        gameState.setCurrentScreen("OPTIONS");
        // TODO: Add transition animation
        System.out.println("Opening options menu...");
        // TODO: Initialize modern options screen
    }

    private void handleExitClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Game");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Any unsaved progress will be lost.");

        // Style the alert dialog
        DialogStyler.style(alert);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Platform.exit();
            }
        });
    }

    public void show() {
        view.getStage().show();
    }

    // Utility class for styling dialogs
    private static class DialogStyler {
        public static void style(Alert alert) {
            alert.getDialogPane().setStyle("""
                -fx-background-color: #1F2937;
                -fx-text-fill: white;
                """);

            alert.getDialogPane().getStylesheets().add("""
                .dialog-pane {
                    -fx-background-color: #1F2937;
                }
                .dialog-pane > *.header-panel {
                    -fx-background-color: #111827;
                }
                .dialog-pane > *.header-panel *.label {
                    -fx-text-fill: white;
                }
                .dialog-pane > *.content.label {
                    -fx-text-fill: #9CA3AF;
                    -fx-font-size: 14px;
                }
                .dialog-pane > *.button-bar *.button {
                    -fx-background-color: #3B82F6;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                }
                .dialog-pane > *.button-bar *.button:hover {
                    -fx-background-color: #2563EB;
                }
                """);
        }
    }
}