package hr.algebra.azul.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class LobbyView {
    private Stage stage;
    private Scene scene;
    private ListView<String> playerListView;
    private Label statusLabel;
    private Button startGameButton;
    private Button leaveLobbyButton;
    private TextArea chatArea;
    private TextField chatInput;
    private Button sendMessageButton;

    public LobbyView() {
        createView();
    }

    private void createView() {
        // Create the main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Create the center content with player list
        VBox centerContent = new VBox(10);
        centerContent.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Multiplayer Lobby");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        playerListView = new ListView<>();
        playerListView.setPrefHeight(200);
        playerListView.setStyle("-fx-background-color: #f4f4f4;");

        statusLabel = new Label("Waiting for players...");
        statusLabel.setStyle("-fx-font-size: 14px;");

        centerContent.getChildren().addAll(titleLabel, playerListView, statusLabel);
        root.setCenter(centerContent);

        // Create the chat area on the right
        VBox chatBox = new VBox(10);
        chatBox.setPrefWidth(200);
        chatBox.setPadding(new Insets(10));

        Label chatLabel = new Label("Chat");
        chatLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(300);
        chatArea.setWrapText(true);

        HBox chatInputBox = new HBox(5);
        chatInput = new TextField();
        chatInput.setPrefWidth(150);
        sendMessageButton = new Button("Send");
        sendMessageButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        chatInputBox.getChildren().addAll(chatInput, sendMessageButton);

        chatBox.getChildren().addAll(chatLabel, chatArea, chatInputBox);
        root.setRight(chatBox);

        // Create the bottom buttons
        HBox bottomButtons = new HBox(10);
        bottomButtons.setAlignment(Pos.CENTER);
        bottomButtons.setPadding(new Insets(10));

        startGameButton = new Button("Start Game");
        startGameButton.setStyle("""
            -fx-background-color: #4CAF50;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-padding: 10 20;
            """);

        leaveLobbyButton = new Button("Leave Lobby");
        leaveLobbyButton.setStyle("""
            -fx-background-color: #f44336;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-padding: 10 20;
            """);

        bottomButtons.getChildren().addAll(startGameButton, leaveLobbyButton);
        root.setBottom(bottomButtons);

        // Create the scene and stage
        scene = new Scene(root, 800, 600);
        stage = new Stage();
        stage.setTitle("Azul - Multiplayer Lobby");
        stage.setScene(scene);
    }

    // Getters for the controller
    public Stage getStage() {
        return stage;
    }

    public ListView<String> getPlayerListView() {
        return playerListView;
    }

    public Button getStartGameButton() {
        return startGameButton;
    }

    public Button getLeaveLobbyButton() {
        return leaveLobbyButton;
    }

    public TextArea getChatArea() {
        return chatArea;
    }

    public TextField getChatInput() {
        return chatInput;
    }

    public Button getSendMessageButton() {
        return sendMessageButton;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }
}