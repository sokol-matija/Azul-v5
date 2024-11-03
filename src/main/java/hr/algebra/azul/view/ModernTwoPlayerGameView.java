package hr.algebra.azul.view;

import hr.algebra.azul.helper.ParticleSystem;
import hr.algebra.azul.models.TileColor;
import hr.algebra.azul.models.Wall;
import javafx.animation.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ModernTwoPlayerGameView {
    // Style constants
    private static final String DARK_BG = "#111827";
    private static final String DARKER_BG = "#0F172A";
    private static final String CARD_BG = "#1F2937";
    private static final String BUTTON_BG = "#3B82F6";
    private static final String BUTTON_HOVER = "#2563EB";
    private static final String ACCENT_COLOR = "#4F46E5";
    private static final Duration ANIMATION_DURATION = Duration.millis(300);

    // Core components
    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private StackPane animationLayer;
    private final IntegerProperty timeRemaining = new SimpleIntegerProperty(150);

    // UI Components
    private HBox topBar;
    private Label timerLabel;
    private Label currentPlayerLabel;
    private VBox player1Board;
    private VBox player2Board;
    private VBox gameCenter;
    private HBox controlBar;
    private HBox player1Hand;
    private HBox player2Hand;
    private GridPane factoriesContainer;
    private VBox centerPool;
    private StackPane player1Progress;
    private StackPane player2Progress;

    // Control buttons
    private Button undoButton;
    private Button saveButton;
    private Button exitButton;
    private Button settingsButton;
    private Button endTurnButton;
    private Button endRoundButton;

    public ModernTwoPlayerGameView() {
        createView();
    }

    private void createView() {
        // Initialize root and styles
        root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/styles/game.css").toExternalForm());
        root.setStyle(String.format("-fx-background-color: %s; -fx-padding: 0;", DARK_BG));

        // Initialize animation layer
        animationLayer = new StackPane();
        animationLayer.setMouseTransparent(true);
        animationLayer.setPickOnBounds(false);

        // Create root container with animation layer
        StackPane rootContainer = new StackPane();
        rootContainer.getChildren().addAll(root, animationLayer);

        // Create scene
        scene = new Scene(rootContainer);

        // Create UI components
        createTopBar();
        createPlayerBoards();
        createGameCenter();
        createControlBar();

        // Set up stage
        stage = new Stage();
        stage.setTitle("Azul - Two Player Mode");
        stage.setScene(scene);
        stage.setMinWidth(1200);
        stage.setMinHeight(800);

        // Add entrance animations
        addEntranceAnimations();
    }

    private void createTopBar() {
        topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(15));
        topBar.setStyle(String.format("-fx-background-color: %s;", DARKER_BG));

        // Left section - Title
        HBox leftSection = createTitleSection();

        // Center section - Turn indicator and timer
        HBox centerSection = createTurnSection();

        // Right section - Controls
        HBox rightSection = createControlSection();

        topBar.getChildren().addAll(leftSection, centerSection, rightSection);
        root.setTop(topBar);
    }

    private HBox createTitleSection() {
        HBox leftSection = new HBox(10);
        Label titleLabel = new Label("AZUL");
        titleLabel.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: linear-gradient(to right, #3B82F6, #8B5CF6);
            """);

        Label separator = new Label(" • ");
        separator.setStyle("-fx-text-fill: #9CA3AF;");

        Label subtitle = new Label("Two Player Mode");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #9CA3AF; -fx-font-weight: bold;");

        leftSection.getChildren().addAll(titleLabel, separator, subtitle);
        leftSection.setPadding(new Insets(20, 0, 0, 0));
        return leftSection;
    }

    private HBox createTurnSection() {
        HBox centerSection = new HBox(15);
        centerSection.setPadding(new Insets(0, 120, 0, 0));
        centerSection.setAlignment(Pos.CENTER);
        HBox.setHgrow(centerSection, Priority.ALWAYS);

        VBox turnInfo = new VBox(8);
        turnInfo.setAlignment(Pos.CENTER);
        turnInfo.setPadding(new Insets(8, 15, 8, 15));
        turnInfo.setStyle("-fx-background-color: #1F2937; -fx-background-radius: 5;");

        // Turn indicator and timer
        HBox turnContainer = new HBox(10);
        turnContainer.setAlignment(Pos.CENTER);

        currentPlayerLabel = new Label("Player 1's Turn");
        currentPlayerLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        timerLabel = new Label("⏱ 02:30");
        timerLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #9CA3AF;");

        turnContainer.getChildren().addAll(currentPlayerLabel, timerLabel);

        // Progress bar
        StackPane timeProgress = createTimeProgressBar();
        timeProgress.setPrefWidth(200);

        turnInfo.getChildren().addAll(turnContainer, timeProgress);
        centerSection.getChildren().add(turnInfo);

        return centerSection;
    }

    private HBox createControlSection() {
        HBox rightSection = new HBox(10);
        rightSection.setAlignment(Pos.CENTER_RIGHT);

        Button helpButton = createHelpButton();
        settingsButton = createIconButton("⚙", "Settings");

        rightSection.getChildren().addAll(helpButton, settingsButton);
        return rightSection;
    }

    private void createPlayerBoards() {
        player1Board = createPlayerBoard("Player 1", true);
        player2Board = createPlayerBoard("Player 2", false);

        // Create hands
        player1Hand = createPlayerHand();
        player2Hand = createPlayerHand();

        // Add hands to boards
        ((VBox)player1Board).getChildren().add(2, createHandSection(player1Hand));
        ((VBox)player2Board).getChildren().add(2, createHandSection(player2Hand));

        VBox.setVgrow(player1Board, Priority.ALWAYS);
        VBox.setVgrow(player2Board, Priority.ALWAYS);

        root.setLeft(player1Board);
        root.setRight(player2Board);
    }

    private VBox createPlayerBoard(String playerName, boolean isActive) {
        VBox board = new VBox(10);
        board.setPrefWidth(300);
        board.setPadding(new Insets(15));
        board.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-border-radius: 10;
            %s
            """,
                CARD_BG,
                isActive ? "-fx-border-color: " + ACCENT_COLOR + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;" : ""
        ));

        // Create board components
        HBox header = createPlayerHeader(playerName);
        VBox patternLines = createPatternLines();
        GridPane wall = createWallGrid();
        HBox floorLine = createFloorLine();

        // Add components with labels
        board.getChildren().addAll(
                header,
                patternLines,
                new Label("Wall") {{ setStyle("-fx-text-fill: #9CA3AF;"); }},
                wall,
                new Label("Floor Line") {{ setStyle("-fx-text-fill: #9CA3AF;"); }},
                floorLine
        );

        return board;
    }

    private HBox createPlayerHand() {
        HBox hand = new HBox(8);
        hand.setAlignment(Pos.CENTER_LEFT);
        hand.setPadding(new Insets(10));
        hand.setMinHeight(50);
        hand.setStyle("""
            -fx-background-color: #374151;
            -fx-background-radius: 5;
            -fx-border-color: #4B5563;
            -fx-border-radius: 5;
            -fx-border-width: 1;
            """);

        // Add placeholder
        Label placeholder = new Label("Select tiles from factory or center");
        placeholder.setStyle("-fx-text-fill: #6B7280; -fx-font-style: italic;");
        hand.getChildren().add(placeholder);

        // Handle placeholder visibility
        hand.getChildren().addListener((ListChangeListener<Node>) change -> {
            while (change.next()) {
                if (change.wasAdded() && change.getList().get(0) instanceof Circle) {
                    hand.getChildren().remove(placeholder);
                }
                if (change.getList().isEmpty()) {
                    hand.getChildren().add(placeholder);
                }
            }
        });

        return hand;
    }

    private VBox createHandSection(HBox hand) {
        VBox handSection = new VBox(5);
        handSection.setPadding(new Insets(5));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label handLabel = new Label("Selected Tiles");
        handLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        header.getChildren().add(handLabel);
        handSection.getChildren().addAll(header, hand);
        return handSection;
    }

    private HBox createPlayerHeader(String playerName) {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(playerName);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label scoreLabel = new Label("0");
        scoreLabel.setStyle("""
            -fx-background-color: #374151;
            -fx-padding: 5 15;
            -fx-background-radius: 5;
            -fx-text-fill: #60A5FA;
            -fx-font-size: 16px;
            """);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(nameLabel, spacer, scoreLabel);
        return header;
    }
    private VBox createPatternLines() {
        VBox patternLinesSection = new VBox(8);
        patternLinesSection.setPadding(new Insets(10));

        // Pattern Lines label
        Label titleLabel = new Label("Pattern Lines");
        titleLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px;");
        patternLinesSection.getChildren().add(titleLabel);

        // Create lines in reverse order (5 to 1)
        for (int row = 0; row < 5; row++) {
            HBox patternLine = createPatternLine(5 - row);
            patternLine.setId("pattern-line-" + (5 - row));
            patternLinesSection.getChildren().add(patternLine);
        }

        return patternLinesSection;
    }

    private HBox createPatternLine(int spaces) {
        HBox patternLine = new HBox(5);
        patternLine.setPadding(new Insets(2));
        patternLine.setAlignment(Pos.CENTER_LEFT);
        patternLine.setStyle("-fx-background-radius: 5; -fx-padding: 5;");

        // Create tile spaces
        for (int i = 0; i < spaces; i++) {
            Circle tileSpace = createTileSpace();
            patternLine.getChildren().add(tileSpace);
        }

        // Add hover effects
        patternLine.setOnMouseEntered(e -> {
            if (isValidTarget(patternLine)) {
                patternLine.setStyle("""
                    -fx-background-color: rgba(59, 130, 246, 0.1);
                    -fx-background-radius: 5;
                    -fx-padding: 5;
                    """);
            }
        });

        patternLine.setOnMouseExited(e -> {
            patternLine.setStyle("-fx-background-radius: 5; -fx-padding: 5;");
        });

        return patternLine;
    }

    private boolean isValidTarget(HBox patternLine) {
        // This will be updated based on game logic
        return patternLine.getStyle().contains("valid-move");
    }

    private GridPane createWallGrid() {
        GridPane wall = new GridPane();
        wall.setHgap(4);
        wall.setVgap(4);
        wall.setStyle("-fx-padding: 5;");

        TileColor[][] wallPattern = new Wall().initializeWallPattern();

        for (int row = 0; row < Wall.WALL_SIZE; row++) {
            for (int col = 0; col < Wall.WALL_SIZE; col++) {
                StackPane tileSpace = createWallTileSpace(wallPattern[row][col], row, col);
                wall.add(tileSpace, col, row);
            }
        }

        return wall;
    }

    private StackPane createWallTileSpace(TileColor color, int row, int col) {
        StackPane tileSpace = new StackPane();
        tileSpace.setPrefSize(40, 40);
        tileSpace.setMinSize(40, 40);
        tileSpace.setMaxSize(40, 40);
        tileSpace.setStyle("""
            -fx-background-color: #1F2937;
            -fx-background-radius: 5;
            -fx-border-color: #374151;
            -fx-border-radius: 5;
            -fx-border-width: 1;
            """);

        // Create circles for different states
        Circle baseCircle = new Circle(15);
        baseCircle.setFill(Color.web("#374151"));
        baseCircle.setStroke(Color.web("#4B5563"));
        baseCircle.setStrokeWidth(1);

        Circle patternIndicator = new Circle(5);
        patternIndicator.setFill(Color.web(color.getHexCode()));
        patternIndicator.setOpacity(0.3);

        Circle tileCircle = new Circle(15);
        tileCircle.setFill(Color.web(color.getHexCode()));
        tileCircle.setOpacity(0);

        tileSpace.getChildren().addAll(baseCircle, patternIndicator, tileCircle);
        setupWallTileHover(tileSpace, color);
        setupWallTileTooltip(tileSpace, row, col, color);

        return tileSpace;
    }

    private void setupWallTileHover(StackPane tileSpace, TileColor color) {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(color.getHexCode()));
        glow.setRadius(10);
        glow.setSpread(0.3);

        Circle patternIndicator = (Circle) tileSpace.getChildren().get(1);
        Circle tileCircle = (Circle) tileSpace.getChildren().get(2);

        tileSpace.setOnMouseEntered(e -> {
            if (tileCircle.getOpacity() < 1.0) {
                patternIndicator.setOpacity(0.8);
                tileSpace.setEffect(glow);
                tileSpace.setStyle("""
                    -fx-background-color: #1F2937;
                    -fx-background-radius: 5;
                    -fx-border-color: #60A5FA;
                    -fx-border-radius: 5;
                    -fx-border-width: 2;
                    """);
            }
        });

        tileSpace.setOnMouseExited(e -> {
            if (tileCircle.getOpacity() < 1.0) {
                patternIndicator.setOpacity(0.3);
                tileSpace.setEffect(null);
                tileSpace.setStyle("""
                    -fx-background-color: #1F2937;
                    -fx-background-radius: 5;
                    -fx-border-color: #374151;
                    -fx-border-radius: 5;
                    -fx-border-width: 1;
                    """);
            }
        });
    }

    private void setupWallTileTooltip(StackPane tileSpace, int row, int col, TileColor color) {
        Tooltip tooltip = new Tooltip(
                String.format("Position [%d,%d]: %s tile", row + 1, col + 1, color.toString())
        );
        tooltip.setStyle("""
            -fx-background-color: #1F2937;
            -fx-text-fill: white;
            -fx-font-size: 12px;
            """);
        Tooltip.install(tileSpace, tooltip);
    }

    private void createGameCenter() {
        gameCenter = new VBox(30);
        gameCenter.setAlignment(Pos.CENTER);
        gameCenter.setPadding(new Insets(20));

        // Create factories grid
        factoriesContainer = new GridPane();
        factoriesContainer.setHgap(15);
        factoriesContainer.setVgap(15);
        factoriesContainer.setAlignment(Pos.CENTER);

        // Add factories
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                if (i * 3 + j < 5) {  // We need only 5 factories
                    factoriesContainer.add(createFactoryDisplay(i * 3 + j), j, i);
                }
            }
        }

        // Create center pool
        centerPool = createCenterPool();

        gameCenter.getChildren().addAll(factoriesContainer, centerPool);
        root.setCenter(gameCenter);
    }

    private VBox createFactoryDisplay(int index) {
        VBox factory = new VBox(10);
        factory.setAlignment(Pos.CENTER);
        factory.setPadding(new Insets(15));
        factory.setPrefSize(120, 120);
        factory.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 10;
            """, CARD_BG));

        // Create tile grid
        GridPane tiles = new GridPane();
        tiles.setHgap(10);
        tiles.setVgap(10);
        tiles.setAlignment(Pos.CENTER);

        // Add tile spaces
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                Circle tileSpace = new Circle(15);
                tileSpace.setFill(Color.web("#374151"));
                tileSpace.setStroke(Color.web("#4B5563"));
                tileSpace.setStrokeWidth(1);
                tileSpace.setEffect(new InnerShadow(5, Color.web("#000000", 0.2)));
                tiles.add(tileSpace, col, row);
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

        FlowPane tiles = new FlowPane(10, 10);
        tiles.setPrefWrapLength(250);
        tiles.setAlignment(Pos.CENTER);
        tiles.setPadding(new Insets(10));
        tiles.setStyle("-fx-background-color: rgba(55, 65, 81, 0.3); -fx-background-radius: 5;");

        pool.getChildren().addAll(centerLabel, tiles);
        setupCenterPoolHover(pool, tiles);

        return pool;
    }

    private void setupCenterPoolHover(VBox pool, FlowPane tiles) {
        pool.setOnMouseEntered(e -> {
            if (!tiles.getChildren().isEmpty()) {
                pool.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-background-radius: 10;
                    -fx-border-color: #60A5FA;
                    -fx-border-width: 2;
                    -fx-border-radius: 10;
                    """, CARD_BG));
            }
        });

        pool.setOnMouseExited(e -> {
            pool.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 10;
                """, CARD_BG));
        });
    }
    private void createControlBar() {
        controlBar = new HBox(10);
        controlBar.setAlignment(Pos.CENTER);
        controlBar.setPadding(new Insets(15));
        controlBar.setStyle(String.format("-fx-background-color: %s;", DARKER_BG));

        undoButton = createIconButton("↩", "Undo");
        saveButton = createIconButton("💾", "Save");
        exitButton = createIconButton("✖", "Exit");
        endTurnButton = createIconButton("➡", "End Turn");
        endRoundButton = createIconButton("🔄", "End Round");

        controlBar.getChildren().addAll(undoButton, saveButton, endTurnButton, endRoundButton, exitButton);
        root.setBottom(controlBar);
    }

    private StackPane createTimeProgressBar() {
        StackPane progressContainer = new StackPane();
        progressContainer.setPrefWidth(300);
        progressContainer.setMaxWidth(300);
        progressContainer.setMinHeight(4);
        progressContainer.setMaxHeight(4);
        progressContainer.setAlignment(Pos.CENTER_LEFT);

        // Background bar
        Rectangle backgroundBar = new Rectangle(300, 4);
        backgroundBar.setFill(Color.web("#374151"));
        backgroundBar.setArcWidth(6);
        backgroundBar.setArcHeight(6);

        // Progress bar
        Rectangle progressBar = new Rectangle();
        progressBar.setHeight(4);
        progressBar.setFill(Color.web("#22C55E"));
        progressBar.setArcWidth(6);
        progressBar.setArcHeight(6);
        progressBar.setWidth(300);

        // Bind progress
        timeRemaining.addListener((obs, old, newVal) -> {
            int time = newVal.intValue();
            double width = (time / 150.0) * 300;
            progressBar.setWidth(width);

            // Update color based on time
            if (time > 90) {
                progressBar.setFill(Color.web("#22C55E")); // Green
            } else if (time > 45) {
                progressBar.setFill(Color.web("#F59E0B")); // Yellow
                createSparkEffect(progressBar);
            } else {
                progressBar.setFill(Color.web("#EF4444")); // Red
                createSparkEffect(progressBar);
            }
        });

        progressContainer.getChildren().addAll(backgroundBar, progressBar);
        return progressContainer;
    }

    private void createSparkEffect(Rectangle progressBar) {
        ParticleSystem sparkSystem = new ParticleSystem();
        sparkSystem.setEmitterLocation(progressBar.getWidth(), 2);

        Timeline sparkAnimation = new Timeline(
                new KeyFrame(Duration.millis(50), e -> sparkSystem.emit())
        );
        sparkAnimation.setCycleCount(5);
        sparkAnimation.play();
    }

    private Circle createTileSpace() {
        Circle circle = new Circle(15);
        circle.setFill(Color.web("#374151"));
        circle.setStroke(Color.web("#4B5563"));
        circle.setStrokeWidth(1);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.3));
        shadow.setRadius(5);
        circle.setEffect(shadow);

        return circle;
    }

    private HBox createFloorLine() {
        HBox floorLine = new HBox(5);
        floorLine.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < 7; i++) {
            Circle space = createTileSpace();

            // Add penalty tooltip
            Tooltip penalty = new Tooltip("Penalty: -" + (i < 2 ? "1" : i < 5 ? "2" : "3"));
            penalty.setStyle("""
                -fx-background-color: #1F2937;
                -fx-text-fill: #EF4444;
                -fx-font-size: 12px;
                """);
            Tooltip.install(space, penalty);

            floorLine.getChildren().add(space);
        }
        return floorLine;
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

        // Add hover effects
        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle().replace(BUTTON_BG, BUTTON_HOVER))
        );

        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle().replace(BUTTON_HOVER, BUTTON_BG))
        );

        Tooltip.install(button, new Tooltip(tooltip));
        return button;
    }

    private Button createHelpButton() {
        Button helpButton = new Button("?");
        helpButton.setStyle("""
            -fx-background-color: #374151;
            -fx-text-fill: #9CA3AF;
            -fx-font-size: 12px;
            -fx-min-width: 24px;
            -fx-min-height: 24px;
            -fx-background-radius: 12px;
            """);

        helpButton.setOnAction(e -> showHelpDialog());
        return helpButton;
    }

    private void showHelpDialog() {
        Alert help = new Alert(Alert.AlertType.INFORMATION);
        help.setTitle("Wall Pattern Help");
        help.setHeaderText("Understanding the Wall Pattern");
        help.setContentText("""
            The wall shows where specific colored tiles can be placed:
            • Each row must contain one of each color
            • Each column must contain one of each color
            • Colors are indicated by the small circles in each space
            • Hover over any space to see which color tile can be placed there
            """);

        DialogPane dialogPane = help.getDialogPane();
        styleDialog(dialogPane);
        help.showAndWait();
    }

    private void styleDialog(DialogPane dialogPane) {
        dialogPane.setStyle("-fx-background-color: #1F2937;");
        dialogPane.lookup(".content.label").setStyle("""
            -fx-text-fill: #9CA3AF;
            -fx-font-size: 14px;
            """);
        dialogPane.lookup(".header-panel").setStyle("""
            -fx-background-color: #111827;
            """);
        dialogPane.lookup(".header-panel .label").setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            """);
    }

    private void addEntranceAnimations() {
        // Main content fade in
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Player boards slide in
        TranslateTransition leftSlide = createSlideAnimation(player1Board, -50, 0);
        TranslateTransition rightSlide = createSlideAnimation(player2Board, 50, 0);

        // Factories scale up
        ScaleTransition scaleFactories = new ScaleTransition(Duration.seconds(0.5), factoriesContainer);
        scaleFactories.setFromX(0.8);
        scaleFactories.setFromY(0.8);
        scaleFactories.setToX(1);
        scaleFactories.setToY(1);

        ParallelTransition parallel = new ParallelTransition(fadeIn, leftSlide, rightSlide, scaleFactories);
        parallel.play();
    }

    private TranslateTransition createSlideAnimation(Region node, double fromX, double toX) {
        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.5), node);
        slide.setFromX(fromX);
        slide.setToX(toX);
        return slide;
    }

    // Animation layer methods
    public void showFloatingText(String text, Node source, boolean isPositive) {
        Label floatingText = new Label(text);
        floatingText.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-padding: 5 10;
            -fx-background-radius: 3;
            -fx-font-size: 14px;
            """, isPositive ? "#22C55E" : "#EF4444"));

        Bounds bounds = source.localToScene(source.getBoundsInLocal());
        floatingText.setTranslateX(bounds.getCenterX() - 20);
        floatingText.setTranslateY(bounds.getCenterY() - 20);

        animationLayer.getChildren().add(floatingText);

        TranslateTransition moveUp = new TranslateTransition(Duration.millis(1000), floatingText);
        moveUp.setByY(-30);

        FadeTransition fade = new FadeTransition(Duration.millis(1000), floatingText);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        ParallelTransition animation = new ParallelTransition(moveUp, fade);
        animation.setOnFinished(e -> animationLayer.getChildren().remove(floatingText));
        animation.play();
    }

    public void addParticleEffect(Node source, Color color) {
        ParticleSystem particles = new ParticleSystem();
        Bounds bounds = source.localToScene(source.getBoundsInLocal());

        particles.setEmitterLocation(bounds.getCenterX(), bounds.getCenterY());

        Timeline cleanup = new Timeline(new KeyFrame(Duration.seconds(2),
                e -> animationLayer.getChildren().remove(particles)));
        cleanup.play();
    }

    // Getters
    public Stage getStage() { return stage; }
    public Button getUndoButton() { return undoButton; }
    public Button getSaveButton() { return saveButton; }
    public Button getExitButton() { return exitButton; }
    public Button getSettingsButton() { return settingsButton; }
    public Button getEndTurnButton() { return endTurnButton; }
    public Button getEndRoundButton() { return endRoundButton; }
    public Label getTimerLabel() { return timerLabel; }
    public Label getCurrentPlayerLabel() { return currentPlayerLabel; }
    public VBox getPlayer1Board() { return player1Board; }
    public VBox getPlayer2Board() { return player2Board; }
    public GridPane getFactoriesContainer() { return factoriesContainer; }
    public VBox getCenterPool() { return centerPool; }
    public HBox getPlayer1Hand() { return player1Hand; }
    public HBox getPlayer2Hand() { return player2Hand; }
    public StackPane getAnimationLayer() { return animationLayer; }
}