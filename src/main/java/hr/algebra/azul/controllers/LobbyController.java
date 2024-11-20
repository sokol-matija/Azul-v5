package hr.algebra.azul.controllers;

import hr.algebra.azul.models.GameLobby;
import hr.algebra.azul.networking.GameClient;
import hr.algebra.azul.view.LobbyDetailsView;
import hr.algebra.azul.view.ModernLobbyView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

public class LobbyController {
    private final ModernLobbyView view;
    private final GameClient client;
    private final ObservableList<GameLobby> lobbies;
    private LobbyDetailsView detailsView;

    public LobbyController(ModernLobbyView view) {
        this.view = view;
        this.lobbies = FXCollections.observableArrayList();

        try {
            this.client = new GameClient();
            initializeNetworking();
            initializeController();
        } catch (Exception e) {
            showError("Connection Error", "Failed to connect to server: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void initializeNetworking() {
        client.connect();
        client.setLobbyUpdateCallback(this::handleServerMessage);
    }

    private void initializeController() {
        view.getLobbyListView().setItems(lobbies);

        // Create lobby button handler
        view.getCreateLobbyButton().setOnAction(e -> {
            String lobbyName = "Lobby-" + client.getPlayer().getDisplayName();
            client.createLobby(lobbyName);
            showLobbyDetails(lobbyName, true);
        });

        // Join lobby button handler
        view.getJoinButton().setOnAction(e -> {
            GameLobby selected = view.getLobbyListView().getSelectionModel().getSelectedItem();
            if (selected != null) {
                client.joinLobby(selected.getName());
                showLobbyDetails(selected.getName(), false);
            }
        });

        // Refresh button handler
        view.getRefreshButton().setOnAction(e -> client.requestLobbies());

        // Search field handler
        view.getSearchField().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            filterLobbies(newValue);
        });

        // Lobby selection handler
        view.getLobbyListView().getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        view.updateSelectedLobbyInfo(newValue);
                        view.getJoinButton().setDisable(false);
                    }
                }
        );
    }

    private void filterLobbies(String searchText) {
        if (searchText.isEmpty()) {
            view.getLobbyListView().setItems(lobbies);
            return;
        }

        ObservableList<GameLobby> filteredList = lobbies.filtered(lobby ->
                lobby.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                        lobby.getHost().toLowerCase().contains(searchText.toLowerCase())
        );
        view.getLobbyListView().setItems(filteredList);
    }

    private void handleServerMessage(String message) {
        if (message == null || message.isEmpty()) return;

        String[] parts = message.split(":");
        switch (parts[0]) {
            case "LOBBIES" -> handleLobbiesUpdate(parts);
            case "LOBBY_UPDATE" -> handleLobbyUpdate(parts);
            case "CHAT" -> handleChatMessage(parts);
            case "ERROR" -> handleError(parts);
            case "DISCONNECTED" -> handleDisconnect(parts);
        }
    }

    private void handleLobbiesUpdate(String[] parts) {
        Platform.runLater(() -> {
            lobbies.clear();
            if (parts.length > 1 && !parts[1].isEmpty()) {
                for (String lobbyName : parts[1].split(",")) {
                    lobbies.add(new GameLobby(
                            lobbyName,
                            client.getPlayer().getDisplayName(),
                            1,
                            2,
                            "Waiting",
                            "All"
                    ));
                }
            }
        });
    }

    private void handleLobbyUpdate(String[] parts) {
        if (parts.length < 3) return;
        String lobbyName = parts[1];
        String playersData = parts[2];

        Platform.runLater(() -> {
            // Update lobby list
            for (GameLobby lobby : lobbies) {
                if (lobby.getName().equals(lobbyName)) {
                    int playerCount = playersData.split(",").length;
                    int index = lobbies.indexOf(lobby);
                    lobbies.set(index, new GameLobby(
                            lobbyName,
                            lobby.getHost(),
                            playerCount,
                            2,
                            playerCount == 2 ? "Full" : "Waiting",
                            "All"
                    ));
                    break;
                }
            }

            // Update details view if open
            if (detailsView != null) {
                updateLobbyDetails(lobbyName, playersData);
            }
        });
    }

    private void handleChatMessage(String[] parts) {
        if (parts.length < 4) return;
        String lobbyName = parts[1];
        String playerName = parts[2];
        String message = parts[3];

        if (detailsView != null) {
            Platform.runLater(() -> detailsView.addChatMessage(playerName, message));
        }
    }

    private void handleError(String[] parts) {
        if (parts.length < 2) return;
        Platform.runLater(() -> showError("Error", parts[1]));
    }

    private void handleDisconnect(String[] parts) {
        Platform.runLater(() -> {
            String message = parts.length > 1 ? parts[1] : "Lost connection to server";
            showError("Disconnected", message);
            if (detailsView != null) {
                detailsView.getStage().close();
            }
            view.getStage().close();
        });
    }

    private void showLobbyDetails(String lobbyName, boolean isHost) {
        Platform.runLater(() -> {
            detailsView = new LobbyDetailsView(lobbyName, isHost);

            // Set up leave button
            detailsView.getLeaveLobbyButton().setOnAction(e -> {
                client.leaveLobby(lobbyName);
                detailsView.getStage().close();
                view.getStage().show();
            });

            // Set up chat
            detailsView.getMessageField().setOnAction(e -> {
                String message = detailsView.getMessageField().getText();
                if (!message.isEmpty()) {
                    client.sendChatMessage(lobbyName, message);
                    detailsView.getMessageField().clear();
                }
            });

            // Set up start game button if host
            if (isHost) {
                detailsView.getStartGameButton().setOnAction(e ->
                        client.startGame(lobbyName)
                );
            }

            // Add initial player
            detailsView.addPlayer(client.getPlayer().getDisplayName(), false);

            view.getStage().hide();
            detailsView.getStage().show();
        });
    }

    private void updateLobbyDetails(String lobbyName, String playersData) {
        String[] players = playersData.split(",");
        for (String player : players) {
            String[] playerInfo = player.split("\\|");
            String playerName = playerInfo[0];
            boolean isReady = playerInfo.length > 1 && playerInfo[1].equals("ready");
            detailsView.addPlayer(playerName, isReady);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void show() {
        view.getStage().show();
    }
}