package hr.algebra.azul.controllers;

import hr.algebra.azul.models.GameLobby;
import hr.algebra.azul.view.ModernLobbyView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public class LobbyController {
    private ModernLobbyView view;
    private ObservableList<GameLobby> lobbies;
    private FilteredList<GameLobby> filteredLobbies;

    public LobbyController(ModernLobbyView view) {
        this.view = view;
        initializeController();
    }

    private void initializeController() {
        // Initialize dummy data
        lobbies = FXCollections.observableArrayList(
                new GameLobby("Pro Players Only", "ValorantMaster", 3, 4, "In Queue", "Diamond+"),
                new GameLobby("Casual Fun Games", "ChillGamer", 2, 4, "Waiting", "All Ranks"),
                new GameLobby("Tournament Practice", "CompeteGaming", 1, 4, "Waiting", "Platinum+")
        );

        // Set up filtering
        filteredLobbies = new FilteredList<>(lobbies, p -> true);
        view.getLobbyListView().setItems(filteredLobbies);

        // Set up search functionality
        view.getSearchField().textProperty().addListener((observable, oldValue, newValue) -> {
            filteredLobbies.setPredicate(lobby -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return lobby.getName().toLowerCase().contains(lowerCaseFilter) ||
                        lobby.getHost().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Set up button handlers
        view.getCreateLobbyButton().setOnAction(e -> showCreateLobbyDialog());
        view.getRefreshButton().setOnAction(e -> refreshLobbies());

        // Set up lobby selection handler
        view.getLobbyListView().getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> view.updateSelectedLobbyInfo(newValue)
        );
    }

    private void showCreateLobbyDialog() {
        // TODO: Implement create lobby dialog
        System.out.println("Create lobby clicked");
    }

    private void refreshLobbies() {
        // TODO: Implement refresh functionality
        System.out.println("Refresh clicked");
    }

    public void show() {
        view.getStage().show();
    }
}