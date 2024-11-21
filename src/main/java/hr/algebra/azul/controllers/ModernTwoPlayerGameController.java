package hr.algebra.azul.controllers;

import hr.algebra.azul.models.*;
import hr.algebra.azul.networking.GameClient;
import hr.algebra.azul.networking.GameSyncProtocol;
import hr.algebra.azul.view.ModernTwoPlayerGameView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.List;

public class ModernTwoPlayerGameController extends GameController {
    private final GameClient gameClient;
    private boolean isMyTurn;
    private final String playerId;

    public ModernTwoPlayerGameController(ModernTwoPlayerGameView view, Stage primaryStage, GameClient gameClient) {
        super(view, primaryStage);
        this.gameClient = gameClient;
        this.playerId = gameClient.getPlayer().getId();
        this.isMyTurn = false;

        initializeMultiplayerGame();
        setupNetworkHandlers();
    }

    private void initializeMultiplayerGame() {
        gameModel = new GameModel(2);
        isMyTurn = gameClient.getPlayer().getId().equals(gameModel.getCurrentPlayer().getId());
        updateCurrentPlayerDisplay();
    }

    private void setupNetworkHandlers() {
        if (gameClient != null) {
            gameClient.setGameMessageCallback(this::handleNetworkMessage);
        }
    }

    @Override
    protected void handleFactoryClick(Circle clickedTile, VBox factory, int factoryIndex) {
        if (!isMyTurn || isGamePaused) return;

        // Get the color of the clicked tile
        Color tileColor = (Color) clickedTile.getFill();
        TileColor selectedColor = getTileColorFromFill(tileColor);

        if (selectedColor != null) {
            Factory factoryModel = gameModel.getFactories().get(factoryIndex);
            List<Tile> selectedTiles = factoryModel.selectTilesByColor(selectedColor);

            if (!selectedTiles.isEmpty()) {
                // Send move to other player
                gameClient.sendGameMove(factoryIndex, selectedColor, factoryModel.getIndex());

                // Update local game state
                updateAfterMove(factory, selectedTiles);
            }
        }
    }

    private void handleNetworkMessage(String message) {
        if (message.startsWith(GameSyncProtocol.MOVE_MADE)) {
            GameSyncProtocol.GameMove move = new GameSyncProtocol.GameMove(message);
            handleOpponentMove(move);
        } else if (message.startsWith(GameSyncProtocol.TURN_END)) {
            handleOpponentTurnEnd();
        } else if (message.startsWith(GameSyncProtocol.GAME_STATE)) {
            handleGameStateUpdate(message);
        }
    }

    private void handleOpponentMove(GameSyncProtocol.GameMove move) {
        Platform.runLater(() -> {
            Factory factory = gameModel.getFactories().get(move.factoryIndex);
            List<Tile> selectedTiles = factory.selectTilesByColor(move.color);

            if (!selectedTiles.isEmpty()) {
                VBox factoryNode = getFactoryNode(move.factoryIndex);
                updateAfterMove(factoryNode, selectedTiles);
            }
        });
    }

    private void handleOpponentTurnEnd() {
        Platform.runLater(() -> {
            isMyTurn = true;
            turnManager.handleEndTurn();
            updateCurrentPlayerDisplay();
        });
    }

    private void handleGameStateUpdate(String message) {
        String[] parts = message.split(":");
        if (parts.length >= 3) {
            Platform.runLater(() -> {
                // Update current player
                String currentPlayerName = parts[1];
                isMyTurn = currentPlayerName.equals(gameClient.getPlayer().getDisplayName());
                updateCurrentPlayerDisplay();

                // Update factories state
                String[] factoriesState = parts[2].split(";");
                updateFactoriesFromState(factoriesState);

                updateEntireView();
            });
        }
    }

    private void updateFactoriesFromState(String[] factoriesState) {
        for (int i = 0; i < factoriesState.length; i++) {
            String factoryState = factoriesState[i];
            if (!factoryState.isEmpty()) {
                String[] colors = factoryState.split(",");
                Factory factory = gameModel.getFactories().get(i);
                factory.clear();
                for (String color : colors) {
                    factory.addTile(new Tile(TileColor.valueOf(color)));
                }
            }
        }
    }

    private void updateAfterMove(VBox factoryNode, List<Tile> selectedTiles) {
        // Update UI
        updateFactoryDisplay(factoryNode);
        updatePlayerHand(gameModel.getCurrentPlayer().getId(), selectedTiles);
        updatePatternLines();

        // Check for round end
        if (isRoundComplete()) {
            handleRoundEnd();
        }
    }

    @Override
    protected void handleEndTurn() {
        if (!isMyTurn) return;

        super.handleEndTurn();
        gameClient.sendTurnEnd();
        isMyTurn = false;
        updateCurrentPlayerDisplay();
    }

    private void updateCurrentPlayerDisplay() {
        Platform.runLater(() -> {
            String currentPlayerText = isMyTurn ?
                    "Your Turn" :
                    "Opponent's Turn";
            view.getCurrentPlayerLabel().setText(currentPlayerText);

            // Update UI elements based on turn
            view.getEndTurnButton().setDisable(!isMyTurn);
            updatePlayerBoardStyles();
        });
    }

    private void updatePlayerBoardStyles() {
        String activeStyle = """
            -fx-background-color: #1F2937;
            -fx-border-color: #4F46E5;
            -fx-border-width: 2;
            -fx-border-radius: 10;
            """;
        String inactiveStyle = """
            -fx-background-color: #1F2937;
            """;

        if (isMyTurn) {
            view.getPlayer1Board().setStyle(activeStyle);
            view.getPlayer2Board().setStyle(inactiveStyle);
        } else {
            view.getPlayer1Board().setStyle(inactiveStyle);
            view.getPlayer2Board().setStyle(activeStyle);
        }
    }

    @Override
    protected void handleGameEnd() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText("Game Finished!");

            Player winner = determineWinner();
            boolean isWinner = winner.getId().equals(playerId);

            alert.setContentText(isWinner ?
                    "Congratulations! You won!" :
                    "Game Over - Better luck next time!");

            styleDialog(alert);
            alert.showAndWait().ifPresent(response -> {
                gameClient.disconnect();
                view.getStage().close();
            });
        });
    }

    private Player determineWinner() {
        List<Player> players = gameModel.getPlayers();
        return players.get(0).getScore() > players.get(1).getScore() ?
                players.get(0) : players.get(1);
    }

    @Override
    public void show() {
        super.show();
        // Request initial game state
        gameClient.requestGameState();
    }

    // Helper methods...
    private VBox getFactoryNode(int factoryIndex) {
        return (VBox) view.getFactoriesContainer().getChildren().get(factoryIndex);
    }
}