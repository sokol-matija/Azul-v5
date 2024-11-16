package hr.algebra.azul.view.screens;

import hr.algebra.azul.view.components.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

 class MultiplayerGameView {
    // Style constants
    private static final String DARK_BG = "#111827";
    private static final String DARKER_BG = "#0F172A";
    private static final String CARD_BG = "#1F2937";

    // Core components
    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private StackPane animationLayer;

    // Game components
    private VBox playerBoards;
    private VBox gameCenter;
    private GridPane factoriesContainer;
    private VBox centerPool;
    private HBox controlBar;

    // Player boards
    private final HorizontalPlayerBoardComponent[] players;

    public MultiplayerGameView(int numPlayers) {
        this.players = new HorizontalPlayerBoardComponent[numPlayers];
        createView();
    }

    private void createView() {
        // Initialize root and animation layer
        root = new BorderPane();
        root.setStyle(String.format("-fx-background-color: %s;", DARK_BG));

        animationLayer = new StackPane();
        animationLayer.setMouseTransparent(true);
        animationLayer.setPickOnBounds(false);

        // Create layout with animation layer
        StackPane rootContainer = new StackPane(root, animationLayer);

        // Create main sections
        createTopBar();
        createPlayerSection();
        createGameCenter();
        createControlBar();

        // Setup scene and stage
        scene = new Scene(rootContainer, 1400, 900);
        stage = new Stage();
        stage.setTitle("Azul - Multiplayer");
        stage.setScene(scene);
        stage.setMinWidth(1200);
        stage.setMinHeight(800);
    }

    private void createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(15));
        topBar.setStyle(String.format("-fx-background-color: %s;", DARKER_BG));

        // Add game info and controls
        Label titleLabel = new Label("AZUL MULTIPLAYER");
        titleLabel.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: linear-gradient(to right, #3B82F6, #8B5CF6);
            """);

        topBar.getChildren().add(titleLabel);
        root.setTop(topBar);
    }

    private void createPlayerSection() {
        playerBoards = new VBox(15);
        playerBoards.setPadding(new Insets(20));

        // Create player boards
        for (int i = 0; i < players.length; i++) {
            players[i] = new HorizontalPlayerBoardComponent("Player " + (i + 1), i == 0);
            playerBoards.getChildren().add(players[i]);
        }

        root.setLeft(playerBoards);
    }

    private void createGameCenter() {
        gameCenter = new VBox(30);
        gameCenter.setAlignment(Pos.CENTER);
        gameCenter.setPadding(new Insets(20));

        // Create factories
        factoriesContainer = new GridPane();
        factoriesContainer.setHgap(15);
        factoriesContainer.setVgap(15);
        factoriesContainer.setAlignment(Pos.CENTER);

        // Create center pool
        centerPool = new VBox(10);
        centerPool.setAlignment(Pos.CENTER);
        centerPool.setPadding(new Insets(20));
        centerPool.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 10;
            """, CARD_BG));

        gameCenter.getChildren().addAll(factoriesContainer, centerPool);
        root.setCenter(gameCenter);
    }

    private void createControlBar() {
        controlBar = new HBox(10);
        controlBar.setAlignment(Pos.CENTER);
        controlBar.setPadding(new Insets(15));
        controlBar.setStyle(String.format("-fx-background-color: %s;", DARKER_BG));

        root.setBottom(controlBar);
    }

    // Getters
    public Stage getStage() { return stage; }
    public StackPane getAnimationLayer() { return animationLayer; }
    public GridPane getFactoriesContainer() { return factoriesContainer; }
    public VBox getCenterPool() { return centerPool; }
    public HBox getControlBar() { return controlBar; }
    public HorizontalPlayerBoardComponent[] getPlayers() { return players; }
}