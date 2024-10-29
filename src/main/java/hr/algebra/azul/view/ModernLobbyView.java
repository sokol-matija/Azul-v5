package hr.algebra.azul.view;

import hr.algebra.azul.models.GameLobby;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ModernLobbyView {
    private Stage stage;
    private Scene scene;
    private ListView<GameLobby> lobbyListView;
    private VBox selectedLobbyInfo;
    private Button createLobbyButton;
    private Button refreshButton;
    private TextField searchField;
    private GameLobby selectedLobby;

    // Style constants
    private static final String DARK_BG = "#111827";
    private static final String CARD_BG = "#1F2937";
    private static final String BLUE_ACCENT = "#3B82F6";

    public ModernLobbyView() {
        createView();
    }

    private void createView() {
        // Initialize stage
        stage = new Stage();
        stage.setTitle("Azul - Game Lobby");

        // Create main container
        BorderPane root = new BorderPane();
        root.setStyle(String.format("-fx-background-color: %s;", DARK_BG));
        root.setPadding(new Insets(20));

        // Create header
        HBox header = createHeader();
        root.setTop(header);

        // Create content
        VBox content = new VBox(15);
        content.getChildren().addAll(
                createSearchBar(),
                createMainContent()
        );
        root.setCenter(content);

        // Create scene
        scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        header.setSpacing(20);

        Label title = new Label("GAME LOBBIES");
        title.setStyle("""
            -fx-font-size: 28px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            """);

        createLobbyButton = new Button("Create Lobby");
        createLobbyButton.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            """);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(title, spacer, createLobbyButton);
        return header;
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search lobbies...");
        searchField.setStyle("""
            -fx-background-color: #1F2937;
            -fx-text-fill: white;
            -fx-prompt-text-fill: #6B7280;
            -fx-padding: 8;
            -fx-background-radius: 5;
            """);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        refreshButton = new Button("⟳");
        refreshButton.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #9CA3AF;
            -fx-border-color: #374151;
            -fx-border-radius: 5;
            -fx-padding: 8 12;
            """);

        searchBar.getChildren().addAll(searchField, refreshButton);
        return searchBar;
    }

    private HBox createMainContent() {
        HBox mainContent = new HBox(20);

        // Create lobby list
        VBox lobbyContainer = new VBox(10);
        lobbyContainer.setPrefWidth(600);

        Label listTitle = new Label("Available Lobbies");
        listTitle.setStyle("""
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            """);

        // Initialize lobby list view
        lobbyListView = new ListView<>();
        lobbyListView.setStyle("""
    -fx-background-color: transparent;
    -fx-background: transparent;
    -fx-control-inner-background: #1F2937;
    -fx-control-inner-background-alt: #1F2937;
    -fx-background-insets: 0;
    -fx-padding: 5;
    """);
        VBox.setVgrow(lobbyListView, Priority.ALWAYS);

        // Set custom cell factory
        lobbyListView.setCellFactory(lv -> new LobbyListCell());

        lobbyContainer.getChildren().addAll(listTitle, lobbyListView);

        // Create selected lobby panel
        selectedLobbyInfo = new VBox(15);
        selectedLobbyInfo.setPrefWidth(300);
        selectedLobbyInfo.setStyle("""
            -fx-background-color: #1F2937;
            -fx-padding: 20;
            -fx-background-radius: 5;
            """);
        selectedLobbyInfo.setVisible(false);

        mainContent.getChildren().addAll(lobbyContainer, selectedLobbyInfo);
        return mainContent;
    }

    // Custom ListCell for lobbies
    private class LobbyListCell extends ListCell<GameLobby> {
        private VBox content;
        private Label nameLabel;
        private Label infoLabel;

        public LobbyListCell() {
            content = new VBox(5);
            content.setPadding(new Insets(10));
            content.setStyle("""
            -fx-background-color: #1F2937;
            -fx-border-color: #374151;
            -fx-border-width: 1;
            -fx-border-radius: 5;
            """);

            nameLabel = new Label();
            nameLabel.setStyle("""
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
            """);

            infoLabel = new Label();
            infoLabel.setStyle("""
            -fx-text-fill: #9CA3AF;
            -fx-font-size: 12px;
            """);

            content.getChildren().addAll(nameLabel, infoLabel);

            // Add hover effect
            content.setOnMouseEntered(e ->
                    content.setStyle("""
                -fx-background-color: #374151;
                -fx-border-color: #4B5563;
                -fx-border-width: 1;
                -fx-border-radius: 5;
                """)
            );

            content.setOnMouseExited(e ->
                    content.setStyle("""
                -fx-background-color: #1F2937;
                -fx-border-color: #374151;
                -fx-border-width: 1;
                -fx-border-radius: 5;
                """)
            );
        }

        @Override
        protected void updateItem(GameLobby lobby, boolean empty) {
            super.updateItem(lobby, empty);

            if (empty || lobby == null) {
                setGraphic(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                nameLabel.setText(lobby.getName());
                infoLabel.setText(String.format("👑 %s • 👥 %d/%d • %s",
                        lobby.getHost(),
                        lobby.getCurrentPlayers(),
                        lobby.getMaxPlayers(),
                        lobby.getRank()));
                setGraphic(content);
                setStyle("-fx-background-color: transparent;");
            }
        }
    }

    // Getters
    public ListView<GameLobby> getLobbyListView() {
        return lobbyListView;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public Button getCreateLobbyButton() {
        return createLobbyButton;
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public Stage getStage() {
        return stage;
    }

    public void updateSelectedLobbyInfo(GameLobby lobby) {
        if (lobby == null) {
            selectedLobbyInfo.setVisible(false);
            return;
        }

        selectedLobbyInfo.getChildren().clear();
        selectedLobbyInfo.setVisible(true);

        Label title = new Label(lobby.getName());
        title.setStyle("""
            -fx-font-size: 20px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            """);

        GridPane details = new GridPane();
        details.setHgap(10);
        details.setVgap(10);

        addDetailRow(details, 0, "Host", lobby.getHost());
        addDetailRow(details, 1, "Players",
                lobby.getCurrentPlayers() + "/" + lobby.getMaxPlayers());
        addDetailRow(details, 2, "Status", lobby.getStatus());
        addDetailRow(details, 3, "Rank", lobby.getRank());

        Button joinButton = new Button("Join Lobby");
        joinButton.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-padding: 10;
            -fx-background-radius: 5;
            -fx-min-width: 200;
            """);

        selectedLobbyInfo.getChildren().addAll(title, details, joinButton);
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        Label valueNode = new Label(value);

        labelNode.setStyle("-fx-text-fill: #9CA3AF;");
        valueNode.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }
}