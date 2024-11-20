package hr.algebra.azul.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class LobbyDetailsView {
    private Stage stage;
    private Scene scene;
    private TextArea chatArea;
    private TextField messageField;
    private VBox playerList;
    private Map<String, HBox> playerEntries = new HashMap<>();
    private Button startGameButton;
    private Button leaveLobbyButton;

    public LobbyDetailsView(String lobbyName, boolean isHost) {
        createView(lobbyName, isHost);
    }

    private void createView(String lobbyName, boolean isHost) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #111827;");

        // Header
        VBox header = createHeader(lobbyName);
        root.setTop(header);

        // Main content
        HBox content = new HBox(20);
        content.setPadding(new Insets(20));

        // Left side - Player list
        VBox leftSide = createPlayerListSection();

        // Right side - Chat
        VBox rightSide = createChatSection();

        content.getChildren().addAll(leftSide, rightSide);
        root.setCenter(content);

        // Bottom buttons
        HBox buttons = createBottomButtons(isHost);
        root.setBottom(buttons);

        scene = new Scene(root, 800, 600);
        stage = new Stage();
        stage.setTitle("Lobby: " + lobbyName);
        stage.setScene(scene);
    }

    private VBox createHeader(String lobbyName) {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #1F2937;");

        Label title = new Label(lobbyName);
        title.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            """);

        Label subtitle = new Label("Waiting for players...");
        subtitle.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px;");

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private VBox createPlayerListSection() {
        VBox container = new VBox(15);
        container.setPrefWidth(400);
        container.setStyle("""
            -fx-background-color: #1F2937;
            -fx-background-radius: 8;
            -fx-padding: 20;
            """);

        Label title = new Label("Players");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        playerList = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(playerList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("""
            -fx-background: #1F2937;
            -fx-background-color: transparent;
            """);

        container.getChildren().addAll(title, scrollPane);
        return container;
    }

    private VBox createChatSection() {
        VBox container = new VBox(15);
        container.setPrefWidth(300);
        container.setStyle("""
            -fx-background-color: #1F2937;
            -fx-background-radius: 8;
            -fx-padding: 20;
            """);

        Label title = new Label("Chat");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(400);
        chatArea.setStyle("""
            -fx-control-inner-background: #374151;
            -fx-text-fill: white;
            -fx-background-radius: 4;
            """);

        HBox messageBox = new HBox(10);
        messageField = new TextField();
        messageField.setPromptText("Type a message...");
        messageField.setStyle("""
            -fx-background-color: #374151;
            -fx-text-fill: white;
            -fx-prompt-text-fill: #9CA3AF;
            """);
        HBox.setHgrow(messageField, Priority.ALWAYS);

        Button sendButton = new Button("Send");
        sendButton.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-background-radius: 4;
            """);

        messageBox.getChildren().addAll(messageField, sendButton);

        container.getChildren().addAll(title, chatArea, messageBox);
        return container;
    }

    private HBox createBottomButtons(boolean isHost) {
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20));
        buttons.setStyle("-fx-background-color: #1F2937;");

        startGameButton = new Button("Start Game");
        startGameButton.setStyle("""
            -fx-background-color: #10B981;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-padding: 10 20;
            -fx-background-radius: 4;
            """);
        startGameButton.setVisible(isHost);

        leaveLobbyButton = new Button("Leave Lobby");
        leaveLobbyButton.setStyle("""
            -fx-background-color: #EF4444;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-padding: 10 20;
            -fx-background-radius: 4;
            """);

        buttons.getChildren().addAll(startGameButton, leaveLobbyButton);
        return buttons;
    }

    public void addPlayer(String playerName, boolean isReady) {
        HBox playerEntry = createPlayerEntry(playerName, isReady);
        playerEntries.put(playerName, playerEntry);
        playerList.getChildren().add(playerEntry);
    }

    private HBox createPlayerEntry(String playerName, boolean isReady) {
        HBox entry = new HBox(10);
        entry.setAlignment(Pos.CENTER_LEFT);
        entry.setPadding(new Insets(10));
        entry.setStyle("""
            -fx-background-color: #374151;
            -fx-background-radius: 4;
            """);

        Label nameLabel = new Label(playerName);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(isReady ? "Ready" : "Not Ready");
        statusLabel.setStyle(String.format("""
            -fx-text-fill: %s;
            -fx-background-color: %s;
            -fx-padding: 5 10;
            -fx-background-radius: 4;
            """,
                isReady ? "#10B981" : "#EF4444",
                isReady ? "#065F46" : "#991B1B"
        ));

        entry.getChildren().addAll(nameLabel, spacer, statusLabel);
        return entry;
    }

    public void addChatMessage(String playerName, String message) {
        String formattedMessage = String.format("%s: %s\n", playerName, message);
        chatArea.appendText(formattedMessage);
    }

    // Getters for controls
    public Stage getStage() { return stage; }
    public Button getStartGameButton() { return startGameButton; }
    public Button getLeaveLobbyButton() { return leaveLobbyButton; }
    public TextField getMessageField() { return messageField; }
    public TextArea getChatArea() { return chatArea; }
}