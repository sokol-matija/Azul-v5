package hr.algebra.azul.view;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ModernGameView {
    private Stage stage;
    private Scene scene;
    private BorderPane root;

    // Game components
    private GridPane factoriesGrid;
    private VBox centerArea;
    private VBox playerBoard;
    private HBox controlBar;
    private VBox scoreBoard;

    // Control buttons
    private Button exitButton;
    private Button saveButton;
    private Button undoButton;
    private Label turnLabel;
    private Label timeLabel;

    // Style constants
    private static final String DARK_BG = "#111827";
    private static final String DARKER_BG = "#0F172A";
    private static final String CARD_BG = "#1F2937";
    private static final String BUTTON_BG = "#3B82F6";
    private static final String BUTTON_HOVER = "#2563EB";
    private static final String TEXT_PRIMARY = "#FFFFFF";
    private static final String TEXT_SECONDARY = "#9CA3AF";

    public ModernGameView() {
        createView();
    }

    private void createView() {
        // Initialize root container
        root = new BorderPane();
        root.setStyle(String.format("-fx-background-color: %s;", DARK_BG));

        // Create all sections
        createTopBar();
        createFactoriesSection();
        createPlayerBoard();
        createScoreBoard();
        createControlBar();

        // Create scene with responsive layout
        scene = new Scene(root, 1200, 800);

        // Set up stage
        stage = new Stage();
        stage.setTitle("Azul - Game");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setScene(scene);

        // Add entrance animation
        addEntranceAnimations();
    }

    private void createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setStyle(String.format("-fx-background-color: %s;", DARKER_BG));

        Label titleLabel = new Label("AZUL");
        titleLabel.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            """);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        turnLabel = new Label("Player 1's Turn");
        turnLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: #9CA3AF;
            """);

        timeLabel = new Label("⏱ 02:30");
        timeLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: #9CA3AF;
            """);

        topBar.getChildren().addAll(titleLabel, spacer, turnLabel, timeLabel);
        root.setTop(topBar);
    }

    private void createFactoriesSection() {
        // Create factories grid
        factoriesGrid = new GridPane();
        factoriesGrid.setHgap(15);
        factoriesGrid.setVgap(15);
        factoriesGrid.setAlignment(Pos.CENTER);

        // Create factory displays (5x2 grid)
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                StackPane factory = createFactoryDisplay();
                factoriesGrid.add(factory, j, i);
            }
        }

        // Create center area for discarded tiles
        centerArea = new VBox(10);
        centerArea.setAlignment(Pos.CENTER);
        centerArea.setPadding(new Insets(20));
        centerArea.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 10;
            """, CARD_BG));

        Label centerLabel = new Label("Center");
        centerLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #9CA3AF;
            """);

        FlowPane centerTiles = new FlowPane();
        centerTiles.setHgap(5);
        centerTiles.setVgap(5);
        centerTiles.setPrefWrapLength(200);

        centerArea.getChildren().addAll(centerLabel, centerTiles);

        // Add both to VBox
        VBox gameArea = new VBox(30);
        gameArea.setAlignment(Pos.CENTER);
        gameArea.setPadding(new Insets(20));
        gameArea.getChildren().addAll(factoriesGrid, centerArea);

        root.setCenter(gameArea);
    }

    private StackPane createFactoryDisplay() {
        StackPane factory = new StackPane();
        factory.setMinSize(120, 120);
        factory.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 10;
            """, CARD_BG));

        // Add tiles in a circle pattern
        // This is a placeholder - actual tiles will be added dynamically
        return factory;
    }

    private void createPlayerBoard() {
        playerBoard = new VBox(15);
        playerBoard.setPadding(new Insets(20));
        playerBoard.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 10;
            """, CARD_BG));

        // Pattern lines (5 rows)
        GridPane patternLines = new GridPane();
        patternLines.setHgap(5);
        patternLines.setVgap(5);

        // Wall grid (5x5)
        GridPane wall = new GridPane();
        wall.setHgap(5);
        wall.setVgap(5);

        // Floor line
        HBox floorLine = new HBox(5);

        playerBoard.getChildren().addAll(
                new Label("Pattern Lines"),
                patternLines,
                new Label("Wall"),
                wall,
                new Label("Floor Line"),
                floorLine
        );

        root.setRight(playerBoard);
    }

    private void createScoreBoard() {
        scoreBoard = new VBox(15);
        scoreBoard.setPadding(new Insets(20));
        scoreBoard.setPrefWidth(200);
        scoreBoard.setStyle(String.format("""
            -fx-background-color: %s;
            """, DARKER_BG));

        // Add player scores
        for (int i = 1; i <= 4; i++) {
            HBox playerScore = createPlayerScoreEntry("Player " + i, i == 1);
            scoreBoard.getChildren().add(playerScore);
        }

        root.setLeft(scoreBoard);
    }

    private HBox createPlayerScoreEntry(String name, boolean isActive) {
        HBox entry = new HBox(10);
        entry.setAlignment(Pos.CENTER_LEFT);
        entry.setPadding(new Insets(10));
        entry.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 5;
            """, isActive ? CARD_BG : "transparent"));

        Label nameLabel = new Label(name);
        nameLabel.setStyle("""
            -fx-text-fill: white;
            -fx-font-weight: bold;
            """);

        Label scoreLabel = new Label("0");
        scoreLabel.setStyle("""
            -fx-text-fill: #9CA3AF;
            """);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        entry.getChildren().addAll(nameLabel, spacer, scoreLabel);
        return entry;
    }

    private void createControlBar() {
        controlBar = new HBox(15);
        controlBar.setAlignment(Pos.CENTER);
        controlBar.setPadding(new Insets(15));
        controlBar.setStyle(String.format("-fx-background-color: %s;", DARKER_BG));

        undoButton = createControlButton("↩ Undo");
        saveButton = createControlButton("💾 Save");
        exitButton = createControlButton("🚪 Exit");

        controlBar.getChildren().addAll(undoButton, saveButton, exitButton);
        root.setBottom(controlBar);
    }

    private Button createControlButton(String text) {
        Button button = new Button(text);
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-padding: 8 15;
            -fx-background-radius: 5;
            """, BUTTON_BG));

        // Add hover effect
        button.setOnMouseEntered(e ->
                button.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 8 15;
                -fx-background-radius: 5;
                """, BUTTON_HOVER))
        );

        button.setOnMouseExited(e ->
                button.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 8 15;
                -fx-background-radius: 5;
                """, BUTTON_BG))
        );

        return button;
    }

    private void addEntranceAnimations() {
        // Fade in animation for the whole scene
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Slide in animations for different sections
        TranslateTransition slideFactories = createSlideAnimation(factoriesGrid, -50, 0);
        TranslateTransition slidePlayerBoard = createSlideAnimation(playerBoard, 50, 0);
        TranslateTransition slideScoreBoard = createSlideAnimation(scoreBoard, -50, 0);

        // Play all animations together
        ParallelTransition parallel = new ParallelTransition(
                fadeIn, slideFactories, slidePlayerBoard, slideScoreBoard
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
    public Button getExitButton() { return exitButton; }
    public Button getSaveButton() { return saveButton; }
    public Button getUndoButton() { return undoButton; }
    public GridPane getFactoriesGrid() { return factoriesGrid; }
    public VBox getPlayerBoard() { return playerBoard; }
    public VBox getScoreBoard() { return scoreBoard; }

    public Scene getScene() {
        return scene;
    }

    public Label getTimeLabel() {
        return timeLabel;
    }

    public Label getTurnLabel() {
        return turnLabel;
    }
}