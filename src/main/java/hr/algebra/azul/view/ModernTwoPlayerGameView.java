package hr.algebra.azul.view;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ModernTwoPlayerGameView {
    private Stage stage;
    private Scene scene;
    private BorderPane root;

    // UI Components
    private HBox topBar;
    private Label timerLabel;
    private Label currentPlayerLabel;
    private VBox player1Board;
    private VBox player2Board;
    private VBox gameCenter;
    private HBox controlBar;

    // Game Controls
    private Button undoButton;
    private Button saveButton;
    private Button exitButton;
    private Button settingsButton;

    // Factory Displays
    private GridPane factoriesContainer;
    private VBox centerPool;

    // Style constants
    private static final String DARK_BG = "#111827";
    private static final String DARKER_BG = "#0F172A";
    private static final String CARD_BG = "#1F2937";
    private static final String BUTTON_BG = "#3B82F6";
    private static final String BUTTON_HOVER = "#2563EB";
    private static final String ACCENT_COLOR = "#4F46E5";

    public ModernTwoPlayerGameView() {
        createView();
    }

    private void createView() {
        // Initialize root container
//        root = new BorderPane();
//        root.setStyle(String.format("-fx-background-color: %s;", DARK_BG));
        root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/styles/game.css").toExternalForm());

        createTopBar();
        createPlayerBoards();
        createGameCenter();
        createControlBar();

        // Create scene
        scene = new Scene(root, 1400, 800);
        scene.getStylesheets().add("/styles/game.css");

        // Set up stage
        stage = new Stage();
        stage.setTitle("Azul - Two Player Mode");
        stage.setMinWidth(1200);
        stage.setMinHeight(700);
        stage.setScene(scene);

        // Add entrance animations
        addEntranceAnimations();
    }

    private void createTopBar() {
        topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setStyle(String.format("-fx-background-color: %s;", DARKER_BG));

        // Title with gradient effect
        Label titleLabel = new Label("AZUL");
        titleLabel.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: linear-gradient(to right, #3B82F6, #8B5CF6);
            """);

        Label subtitle = new Label("Two Player Mode");
        subtitle.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #9CA3AF;
            """);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Timer and current player
        timerLabel = new Label("⏱ 02:30");
        timerLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: #9CA3AF;
            """);

        currentPlayerLabel = new Label("Player 1's Turn");
        currentPlayerLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: #9CA3AF;
            """);

        // Settings button
        settingsButton = createIconButton("⚙", "Settings");

        topBar.getChildren().addAll(
                titleLabel,
                new Label(" • "),
                subtitle,
                spacer,
                currentPlayerLabel,
                timerLabel,
                settingsButton
        );

        root.setTop(topBar);
    }

    private void createPlayerBoards() {
        // Player 1 Board (Left)
        player1Board = createPlayerBoard("Player 1", true);
        VBox.setVgrow(player1Board, Priority.ALWAYS);

        // Player 2 Board (Right)
        player2Board = createPlayerBoard("Player 2", false);
        VBox.setVgrow(player2Board, Priority.ALWAYS);

        // Add to root
        root.setLeft(player1Board);
        root.setRight(player2Board);
    }

    private VBox createPlayerBoard(String playerName, boolean isActive) {
        VBox board = new VBox(15);
        board.setPrefWidth(300);
        board.setPadding(new Insets(20));
        board.setStyle(String.format("""
            -fx-background-color: %s;
            %s
            """,
                CARD_BG,
                isActive ? "-fx-border-color: " + ACCENT_COLOR + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;" : ""
        ));

        // Player header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(playerName);
        nameLabel.setStyle("""
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            """);

        Label scoreLabel = new Label("0");
        scoreLabel.setStyle("""
            -fx-background-color: #374151;
            -fx-padding: 5 15;
            -fx-background-radius: 5;
            -fx-text-fill: #60A5FA;
            -fx-font-size: 16px;
            """);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        header.getChildren().addAll(nameLabel, headerSpacer, scoreLabel);

        // Pattern lines
        VBox patternLines = new VBox(5);
        patternLines.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < 5; i++) {
            HBox row = new HBox(5);
            for (int j = 0; j <= i; j++) {
                Circle circle = createTileSpace();
                row.getChildren().add(circle);
            }
            patternLines.getChildren().add(row);
        }

        // Wall grid
        GridPane wall = new GridPane();
        wall.setHgap(5);
        wall.setVgap(5);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                wall.add(createTileSpace(), j, i);
            }
        }

        // Floor line
        HBox floorLine = new HBox(5);
        for (int i = 0; i < 7; i++) {
            floorLine.getChildren().add(createTileSpace());
        }

        // Add all components
        board.getChildren().addAll(
                header,
                new Label("Pattern Lines") {{ setStyle("-fx-text-fill: #9CA3AF;"); }},
                patternLines,
                new Label("Wall") {{ setStyle("-fx-text-fill: #9CA3AF;"); }},
                wall,
                new Label("Floor Line") {{ setStyle("-fx-text-fill: #9CA3AF;"); }},
                floorLine
        );

        return board;
    }

    private void createGameCenter() {
        gameCenter = new VBox(30);
        gameCenter.setAlignment(Pos.CENTER);
        gameCenter.setPadding(new Insets(20));

        // Factories grid
        factoriesContainer = new GridPane();
        factoriesContainer.setHgap(15);
        factoriesContainer.setVgap(15);
        factoriesContainer.setAlignment(Pos.CENTER);

        // Create factory displays
        int factories = 5;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                if (i * 3 + j < factories) {
                    factoriesContainer.add(createFactoryDisplay(i * 3 + j), j, i);
                }
            }
        }

        // Center pool
        centerPool = createCenterPool();

        gameCenter.getChildren().addAll(factoriesContainer, centerPool);
        root.setCenter(gameCenter);
    }

    private VBox createFactoryDisplay(int index) {
        VBox factory = new VBox(10);
        factory.setAlignment(Pos.CENTER);
        factory.setPadding(new Insets(15));
        factory.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 10;
            """, CARD_BG));

        // Tiles grid
        GridPane tiles = new GridPane();
        tiles.setHgap(5);
        tiles.setVgap(5);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                tiles.add(createTileSpace(), j, i);
            }
        }

        Label indexLabel = new Label("Factory " + (index + 1));
        indexLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        factory.getChildren().addAll(tiles, indexLabel);
        return factory;
    }

    private VBox createCenterPool() {
        VBox pool = new VBox(10);
        pool.setAlignment(Pos.CENTER);
        pool.setPadding(new Insets(20));
        pool.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 10;
            """, CARD_BG));

        Label centerLabel = new Label("Center");
        centerLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px;");

        FlowPane tiles = new FlowPane(5, 5);
        tiles.setPrefWrapLength(200);
        tiles.setAlignment(Pos.CENTER);

        pool.getChildren().addAll(centerLabel, tiles);
        return pool;
    }

    private void createControlBar() {
        controlBar = new HBox(10);
        controlBar.setAlignment(Pos.CENTER);
        controlBar.setPadding(new Insets(15));
        controlBar.setStyle(String.format("-fx-background-color: %s;", DARKER_BG));

        undoButton = createIconButton("↩", "Undo");
        saveButton = createIconButton("💾", "Save");
        exitButton = createIconButton("✖", "Exit");

        controlBar.getChildren().addAll(undoButton, saveButton, exitButton);
        root.setBottom(controlBar);
    }

    private Circle createTileSpace() {
        Circle circle = new Circle(15);
        circle.setFill(Color.web("#374151"));
        circle.setStroke(Color.web("#4B5563"));
        circle.setStrokeWidth(1);
        return circle;
    }

    private Button createIconButton(String icon, String tooltip) {
        Button button = new Button(icon);
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-padding: 8 15;
            -fx-background-radius: 5;
            """, BUTTON_BG));

        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle().replace(BUTTON_BG, BUTTON_HOVER))
        );

        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle().replace(BUTTON_HOVER, BUTTON_BG))
        );

        Tooltip.install(button, new Tooltip(tooltip));
        return button;
    }

    private void addEntranceAnimations() {
        // Fade in animation for the whole scene
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Slide in animations for player boards
        TranslateTransition slideLeft = createSlideAnimation(player1Board, -50, 0);
        TranslateTransition slideRight = createSlideAnimation(player2Board, 50, 0);

        // Scale up animation for factories
        ScaleTransition scaleFactories = new ScaleTransition(Duration.seconds(0.5), factoriesContainer);
        scaleFactories.setFromX(0.8);
        scaleFactories.setFromY(0.8);
        scaleFactories.setToX(1);
        scaleFactories.setToY(1);

        // Play all animations together
        ParallelTransition parallel = new ParallelTransition(
                fadeIn, slideLeft, slideRight, scaleFactories
        );
        parallel.play();
    }

    private TranslateTransition createSlideAnimation(Region node, double fromX, double toX) {
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.5), node);
        slide.setFromX(fromX);
        slide.setToX(toX);
        return slide;
    }

    // Getters
    public Stage getStage() { return stage; }
    public Button getUndoButton() { return undoButton; }
    public Button getSaveButton() { return saveButton; }
    public Button getExitButton() { return exitButton; }
    public Button getSettingsButton() { return settingsButton; }
    public Label getTimerLabel() { return timerLabel; }
    public Label getCurrentPlayerLabel() { return currentPlayerLabel; }
    public VBox getPlayer1Board() { return player1Board; }
    public VBox getPlayer2Board() { return player2Board; }
    public GridPane getFactoriesContainer() { return factoriesContainer; }
    public VBox getCenterPool() { return centerPool; }
}