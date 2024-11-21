package hr.algebra.azul.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.application.Platform;
import java.util.HashMap;
import java.util.Map;

public class LobbyDetailsView {
    private final Stage stage;
    private final Scene scene;
    private TextArea chatArea;
    private TextField messageField;
    private VBox playerList;
    private final Map<String, HBox> playerEntries = new HashMap<>();
    private Button startGameButton;
    private Button readyButton;
    private Button leaveLobbyButton;
    private final boolean isHost;
    private final String lobbyName;
    private Label statusLabel;
    private boolean isReady = false;

    // For window dragging
    private double xOffset = 0;
    private double yOffset = 0;

    public LobbyDetailsView(String lobbyName, boolean isHost) {
        this.lobbyName = lobbyName;
        this.isHost = isHost;
        stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);

        BorderPane root = createLayout();
        setupWindowDragging(root);

        scene = new Scene(root, 800, 600);
        stage.setScene(scene);
    }

    private void setupWindowDragging(BorderPane root) {
        root.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged((MouseEvent event) -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    private BorderPane createLayout() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #111827;");

        VBox header = createHeader();
        root.setTop(header);

        HBox content = new HBox(20);
        content.setPadding(new Insets(20));

        VBox leftSide = createPlayerListSection();
        VBox rightSide = createChatSection();

        content.getChildren().addAll(leftSide, rightSide);
        root.setCenter(content);

        HBox buttons = createBottomButtons();
        root.setBottom(buttons);

        return root;
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #1F2937;");

        HBox titleBar = new HBox(10);
        titleBar.setAlignment(Pos.CENTER_RIGHT);

        Button closeButton = new Button("×");
        closeButton.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #9CA3AF;
            -fx-font-size: 18px;
            """);
        closeButton.setOnAction(e -> stage.close());

        Label title = new Label(lobbyName);
        title.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            """);

        statusLabel = new Label(isHost ? "Waiting for players..." : "Waiting for host...");
        statusLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px;");

        titleBar.getChildren().addAll(new Region(), title, closeButton);
        HBox.setHgrow(titleBar.getChildren().get(0), Priority.ALWAYS);

        header.getChildren().addAll(titleBar, statusLabel);
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
            -fx-control-inner-background: #1F2937;
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

        messageField = new TextField();
        messageField.setPromptText("Type a message...");
        messageField.setStyle("""
            -fx-background-color: #374151;
            -fx-text-fill: white;
            -fx-prompt-text-fill: #9CA3AF;
            -fx-background-radius: 4;
            """);

        container.getChildren().addAll(title, chatArea, messageField);
        return container;
    }

    private HBox createBottomButtons() {
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20));
        buttons.setStyle("-fx-background-color: #1F2937;");

        if (!isHost) {
            readyButton = new Button("Ready");
            readyButton.setStyle(getButtonStyle(false));
            buttons.getChildren().add(readyButton);
        }

        startGameButton = new Button("Start Game");
        startGameButton.setStyle("""
            -fx-background-color: #10B981;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-padding: 10 20;
            -fx-background-radius: 4;
            """);
        startGameButton.setDisable(true);
        startGameButton.setVisible(isHost);

        leaveLobbyButton = new Button("Leave Lobby");
        leaveLobbyButton.setStyle("""
            -fx-background-color: #EF4444;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-padding: 10 20;
            -fx-background-radius: 4;
            """);

        buttons.getChildren().addAll(
                isHost ? startGameButton : new Region(),
                leaveLobbyButton
        );

        return buttons;
    }

    public void addPlayer(String playerName, boolean ready) {
        Platform.runLater(() -> {
            if (!playerEntries.containsKey(playerName)) {
                HBox playerEntry = createPlayerEntry(playerName, ready);
                playerEntries.put(playerName, playerEntry);
                playerList.getChildren().add(playerEntry);
            }
            updatePlayerStatus(playerName, ready);
        });
    }

    private HBox createPlayerEntry(String playerName, boolean ready) {
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

        Label statusLabel = createStatusLabel(ready);

        entry.getChildren().addAll(nameLabel, spacer, statusLabel);
        return entry;
    }

    private Label createStatusLabel(boolean ready) {
        Label statusLabel = new Label(ready ? "Ready" : "Not Ready");
        statusLabel.setStyle(String.format("""
            -fx-text-fill: %s;
            -fx-background-color: %s;
            -fx-padding: 5 10;
            -fx-background-radius: 4;
            """,
                ready ? "#10B981" : "#EF4444",
                ready ? "#065F46" : "#991B1B"
        ));
        return statusLabel;
    }

    public void updatePlayerStatus(String playerName, boolean ready) {
        Platform.runLater(() -> {
            HBox entry = playerEntries.get(playerName);
            if (entry != null) {
                Label statusLabel = (Label) entry.getChildren().get(2);
                updateStatusLabel(statusLabel, ready);
            }
        });
    }

    private void updateStatusLabel(Label statusLabel, boolean ready) {
        statusLabel.setText(ready ? "Ready" : "Not Ready");
        statusLabel.setStyle(String.format("""
            -fx-text-fill: %s;
            -fx-background-color: %s;
            -fx-padding: 5 10;
            -fx-background-radius: 4;
            """,
                ready ? "#10B981" : "#EF4444",
                ready ? "#065F46" : "#991B1B"
        ));
    }

    public void updateStartButton(boolean canStart) {
        Platform.runLater(() -> {
            if (startGameButton != null) {
                startGameButton.setDisable(!canStart);
                statusLabel.setText(canStart ? "Ready to start!" : "Waiting for players to be ready...");
            }
        });
    }

    public void addChatMessage(String playerName, String message) {
        Platform.runLater(() -> {
            chatArea.appendText(String.format("%s: %s\n", playerName, message));
            chatArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    private String getButtonStyle(boolean isReady) {
        String color = isReady ? "#EF4444" : "#10B981";
        return String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-padding: 10 20;
            -fx-background-radius: 4;
            """, color);
    }

    // Getters
    public Stage getStage() { return stage; }
    public Button getStartGameButton() { return startGameButton; }
    public Button getReadyButton() { return readyButton; }
    public Button getLeaveLobbyButton() { return leaveLobbyButton; }
    public TextField getMessageField() { return messageField; }
    public TextArea getChatArea() { return chatArea; }
    public boolean isHost() { return isHost; }
    public boolean isReady() { return isReady; }
    public void setReady(boolean ready) { this.isReady = ready; }
}