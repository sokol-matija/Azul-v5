package hr.algebra.azul.view;

import hr.algebra.azul.helper.ParticleSystem;
import hr.algebra.azul.models.TileColor;
import hr.algebra.azul.models.Wall;
import javafx.animation.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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

import java.util.ArrayList;
import java.util.List;

public class ModernTwoPlayerGameView {
    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private StackPane animationLayer;


    // UI Components
    private final IntegerProperty timeRemaining = new SimpleIntegerProperty(150);
    private HBox topBar;
    private Label timerLabel;
    private Label currentPlayerLabel;
    private VBox player1Board;
    private VBox player2Board;
    private VBox gameCenter;
    private HBox controlBar;
    private HBox player1Hand;
    private HBox player2Hand;

    // Game Controls
    private Button undoButton;
    private Button saveButton;
    private Button exitButton;
    private Button settingsButton;
    private Button endTurnButton;
    private Button endRoundButton;

    // Factory Displays
    private GridPane factoriesContainer;
    private VBox centerPool;

    private StackPane player1Progress;
    private StackPane player2Progress;
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
        root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/styles/game.css").toExternalForm());
        root.setStyle(String.format("-fx-background-color: %s; -fx-padding: 0;", DARK_BG));

        animationLayer = new StackPane();
        animationLayer.setMouseTransparent(true);
        animationLayer.setPickOnBounds(false);
        // Create scene with just the root first

        // Initialize animation layer
        StackPane rootContainer = new StackPane();
        rootContainer.getChildren().addAll(root, animationLayer);

        // Create scene with rootContainer instead of root

        scene = new Scene(rootContainer);
        // Create all UI components
        createTopBar();
        createPlayerBoards();
        createGameCenter();
        createControlBar();

        // Set up stage
        stage = new Stage();
        stage.setTitle("Azul - Two Player Mode");
        stage.setScene(scene);

        // Add entrance animations
        addEntranceAnimations();
    }

    private void createTopBar() {
        topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(15));
        topBar.setStyle(String.format("-fx-background-color: %s;", DARKER_BG));

        // Left section - Title
        HBox leftSection = new HBox(10);
        Label titleLabel = new Label("AZUL");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setStyle("""
        -fx-font-size: 24px;
        -fx-font-weight: bold;
        -fx-text-fill: linear-gradient(to right, #3B82F6, #8B5CF6);
        """);

        Label separator = new Label(" • ");
        separator.setAlignment(Pos.CENTER);
        separator.setStyle("-fx-text-fill: #9CA3AF;");

        Label subtitle = new Label("Two Player Mode");
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setPadding(new Insets(10, 0, 0, 0));
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #9CA3AF; -fx-font-weight: bold;");

        leftSection.getChildren().addAll(titleLabel, separator, subtitle);
        leftSection.setPadding(new Insets(20, 0, 0, 0));

        // Center section - Turn indicator, timer and progress bar
        HBox centerSection = new HBox(15);
        centerSection.setPadding(new Insets(0,120,0,0));
        centerSection.setAlignment(Pos.CENTER);
        HBox.setHgrow(centerSection, Priority.ALWAYS);

        // Create a VBox to stack the turn info and progress bar
        VBox turnInfo = new VBox(8);
        turnInfo.setAlignment(Pos.CENTER);
        turnInfo.setPadding(new Insets(8, 15, 8, 15));
        turnInfo.setStyle("""
        -fx-background-color: #1F2937;
        -fx-background-radius: 5;
        """);

        // Turn indicator and timer in HBox
        HBox turnContainer = new HBox(10);
        turnContainer.setAlignment(Pos.CENTER);

        currentPlayerLabel = new Label("Player 1's Turn");
        currentPlayerLabel.setStyle("""
        -fx-font-size: 16px;
        -fx-text-fill: white;
        """);

        timerLabel = new Label("⏱ 02:30");
        timerLabel.setStyle("""
        -fx-font-size: 16px;
        -fx-text-fill: #9CA3AF;
        """);

        turnContainer.getChildren().addAll(currentPlayerLabel, timerLabel);

        // Add progress bar
        StackPane timeProgress = createTimeProgressBar();
        timeProgress.setPrefWidth(200); // Adjust width as needed

        // Add both to the turn info container
        turnInfo.getChildren().addAll(turnContainer, timeProgress);
        centerSection.getChildren().add(turnInfo);

        // Right section - Controls
        HBox rightSection = new HBox(10);
        rightSection.setAlignment(Pos.CENTER_RIGHT);

        Button helpButton = createHelpButton();
        settingsButton = createIconButton("⚙", "Settings");

        rightSection.getChildren().addAll(helpButton, settingsButton);

        // Add all sections to the top bar
        topBar.getChildren().addAll(leftSection, centerSection, rightSection);

        root.setTop(topBar);
    }

    private StackPane createTimeProgressBar() {
        StackPane progressContainer = new StackPane();
        progressContainer.setPrefWidth(300);
        progressContainer.setMaxWidth(300);
        progressContainer.setMinHeight(4);
        progressContainer.setMaxHeight(4);
        progressContainer.setAlignment(Pos.CENTER_LEFT);

        // Background bar (gray)
        Rectangle backgroundBar = new Rectangle(300, 4);
        backgroundBar.setFill(Color.web("#374151"));
        backgroundBar.setArcWidth(6);
        backgroundBar.setArcHeight(6);

        // Progress bar
        Rectangle progressBar = new Rectangle();
        progressBar.setHeight(4);
        progressBar.setFill(Color.web("#22C55E")); // Green
        progressBar.setArcWidth(6);
        progressBar.setArcHeight(6);

        // Set initial progress bar width to full
        progressBar.setWidth(300);

        // Bind progressBar width to timeRemaining
        timeRemaining.addListener((obs, old, newVal) -> {
            int time = newVal.intValue();
            // Calculate width based on remaining time (150 seconds total)
            double width = (time / 150.0) * 300;
            progressBar.setWidth(width);

            // Update color based on remaining time
            if (time > 90) { // > 60%
                progressBar.setFill(Color.web("#22C55E")); // Green
            } else if (time > 45) { // > 30%
                progressBar.setFill(Color.web("#F59E0B")); // Yellow
            } else {
                progressBar.setFill(Color.web("#EF4444")); // Red
            }
        });

        // Timeline to decrement timeRemaining every second
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (timeRemaining.get() > 0) {
                timeRemaining.set(timeRemaining.get() - 1);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        progressContainer.getChildren().addAll(backgroundBar, progressBar);
        return progressContainer;
    }



    private String getColorForProgress(double progress) {
        if (progress > 0.6) {
            return "#22C55E"; // Green
        } else if (progress > 0.3) {
            return "#F59E0B"; // Yellow/Orange
        } else {
            return "#EF4444"; // Red
        }
    }

    private void createSparkEffect(Rectangle progressBar) {
        // Create particle system for sparks
        ParticleSystem sparkSystem = new ParticleSystem();

        // Update spark position based on progress
        progressBar.widthProperty().addListener((obs, old, newWidth) -> {
            sparkSystem.setEmitterLocation(newWidth.doubleValue(), 3);
        });

        Timeline sparkAnimation = new Timeline(
                new KeyFrame(Duration.millis(50), e -> sparkSystem.emit())
        );
        sparkAnimation.setCycleCount(Timeline.INDEFINITE);
        sparkAnimation.play();
    }

    private void createPlayerBoards() {
        player1Board = createPlayerBoard("Player 1", true);
        player2Board = createPlayerBoard("Player 2", false);

        // Create hands for each player
        player1Hand = createPlayerHand();
        player2Hand = createPlayerHand();

        // Add hands to player boards
        ((VBox)player1Board).getChildren().add(2, createHandSection(player1Hand)); // Add after header
        ((VBox)player2Board).getChildren().add(2, createHandSection(player2Hand));

        VBox.setVgrow(player1Board, Priority.ALWAYS);
        VBox.setVgrow(player2Board, Priority.ALWAYS);

        root.setLeft(player1Board);
        root.setRight(player2Board);
    }

    private HBox createPlayerHand() {
        HBox hand = new HBox();
        hand.setAlignment(Pos.CENTER_LEFT);
        hand.setPadding(new Insets(10));
        hand.setMinHeight(30);
        hand.setStyle("""
            -fx-background-color: #374151;
            -fx-background-radius: 5;
            -fx-border-color: #4B5563;
            -fx-border-radius: 5;
            -fx-border-width: 1;
            """);
        return hand;
    }

    private VBox createHandSection(HBox hand) {
        VBox handSection = new VBox(5);
        handSection.setPadding(new Insets(5));

        Label handLabel = new Label("Selected Tiles");
        handLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        handSection.getChildren().addAll(handLabel, hand);
        return handSection;
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

        // Player header
        HBox header = createPlayerHeader(playerName);

        // Pattern lines
        VBox patternLines = createPatternLines();

        // Wall grid
        GridPane wall = new GridPane();
        wall.setHgap(4);
        wall.setVgap(4);
        createWallGrid(wall);

        // Floor line
        HBox floorLine = createFloorLine();

        // Color legend
        //VBox legend = createColorLegend();

        // Add all components
        board.getChildren().addAll(
                header,
                patternLines,
                new Label("Wall") {{ setStyle("-fx-text-fill: #9CA3AF;"); }},
                wall,
                //legend,
                new Label("Floor Line") {{ setStyle("-fx-text-fill: #9CA3AF;"); }},
                floorLine
        );

        return board;
    }

    private HBox createPlayerHeader(String playerName) {
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
        return header;
    }

    private VBox createPatternLines() {
        VBox patternLinesSection = new VBox(8);
        patternLinesSection.setPadding(new Insets(10));

        // Pattern Lines label
        Label titleLabel = new Label("Pattern Lines");
        titleLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px;");
        patternLinesSection.getChildren().add(titleLabel);

        // Create pattern lines (bottom to top)
        for (int row = 0; row < 5; row++) {
            HBox patternLine = new HBox(5); // 5 pixels spacing between circles
            patternLine.setPadding(new Insets(2));

            // For each row, create the correct number of spaces (5,4,3,2,1)
            int numSpaces = 5 - row;  // Start with 5 and decrease

            for (int i = 0; i < numSpaces; i++) {
                Circle tileSpace = createTileSpace();
                patternLine.getChildren().add(tileSpace);
            }

            patternLinesSection.getChildren().add(patternLine);
        }

        // Reverse the order of the pattern lines (excluding the label)
        List<Node> lines = new ArrayList<>(patternLinesSection.getChildren());
        patternLinesSection.getChildren().clear();
        patternLinesSection.getChildren().add(lines.get(0)); // Add label back first

        // Add the lines in reverse order
        for (int i = lines.size() - 1; i > 0; i--) {
            patternLinesSection.getChildren().add(lines.get(i));
        }

        return patternLinesSection;
    }


    private void createWallGrid(GridPane wall) {
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

        // Background circle (always visible)
        Circle baseCircle = new Circle(15);
        baseCircle.setFill(Color.web("#374151"));
        baseCircle.setStroke(Color.web("#4B5563"));
        baseCircle.setStrokeWidth(1);

        // Pattern indicator circle (shows what color can go here)
        Circle patternIndicator = new Circle(5);
        patternIndicator.setFill(Color.web(color.getHexCode()));
        patternIndicator.setOpacity(0.3);

        // Tile circle (initially invisible, shown when tile is placed)
        Circle tileCircle = new Circle(15);
        tileCircle.setFill(Color.web(color.getHexCode()));
        tileCircle.setOpacity(0);

        tileSpace.getChildren().addAll(baseCircle, patternIndicator, tileCircle);

        // Add hover effect
        setupWallTileHover(tileSpace, color);

        // Add tooltip
        Tooltip tooltip = new Tooltip(
                String.format("Position [%d,%d]: %s tile", row + 1, col + 1, color.toString())
        );
        tooltip.setStyle("""
        -fx-background-color: #1F2937;
        -fx-text-fill: white;
        -fx-font-size: 12px;
        """);
        Tooltip.install(tileSpace, tooltip);

        return tileSpace;
    }

    private void setupWallTileHover(StackPane tileSpace, TileColor color) {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(color.getHexCode()));
        glow.setRadius(10);
        glow.setSpread(0.3);

        tileSpace.setOnMouseEntered(e -> {
            Circle patternIndicator = (Circle) tileSpace.getChildren().get(1);
            FadeTransition fade = new FadeTransition(Duration.millis(200), patternIndicator);
            fade.setToValue(0.8);
            fade.play();
            tileSpace.setEffect(glow);
        });

        tileSpace.setOnMouseExited(e -> {
            Circle patternIndicator = (Circle) tileSpace.getChildren().get(1);
            FadeTransition fade = new FadeTransition(Duration.millis(200), patternIndicator);
            fade.setToValue(0.3);
            fade.play();
            tileSpace.setEffect(null);
        });
    }

    private void addHoverAnimation(StackPane tileSpace, Circle colorIndicator, Circle outerCircle, TileColor color) {
        // Create glow effect
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(color.getHexCode()));
        glow.setRadius(10);
        glow.setSpread(0.3);

        // Create fade transitions
        FadeTransition fadeIn = new FadeTransition(Duration.millis(100), colorIndicator);
        fadeIn.setToValue(0.8);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(100), colorIndicator);
        fadeOut.setToValue(0.5);

        tileSpace.setOnMouseEntered(e -> {
            outerCircle.setStroke(Color.web("#60A5FA"));
            colorIndicator.setEffect(glow);
            fadeIn.play();
        });

        tileSpace.setOnMouseExited(e -> {
            outerCircle.setStroke(Color.web("#4B5563"));
            colorIndicator.setEffect(null);
            fadeOut.play();
        });
    }

    private HBox createFloorLine() {
        HBox floorLine = new HBox(5);
        for (int i = 0; i < 7; i++) {
            floorLine.getChildren().add(createTileSpace());
        }
        return floorLine;
    }

//    private VBox createColorLegend() {
//        VBox legend = new VBox(5);
//        legend.setStyle("""
//            -fx-background-color: #1F2937;
//            -fx-padding: 10;
//            -fx-background-radius: 5;
//            """);
//
//        Label legendTitle = new Label("Tile Colors");
//        legendTitle.setStyle("""
//            -fx-text-fill: #9CA3AF;
//            -fx-font-size: 12px;
//            -fx-font-weight: bold;
//            """);
//
//        GridPane colorGrid = new GridPane();
//        colorGrid.setHgap(10);
//        colorGrid.setVgap(5);
//
//        int row = 0;
//        for (TileColor color : TileColor.values()) {
//            Circle colorSample = new Circle(6);
//            colorSample.setFill(Color.web(color.getHexCode()));
//            colorSample.setStroke(Color.web("#4B5563"));
//            colorSample.setStrokeWidth(1);
//
//            Label colorName = new Label(color.toString());
//            colorName.setStyle("""
//                -fx-text-fill: #9CA3AF;
//                -fx-font-size: 12px;
//                """);
//
//            colorGrid.add(colorSample, 0, row);
//            colorGrid.add(colorName, 1, row);
//            row++;
//        }
//
//        legend.getChildren().addAll(legendTitle, colorGrid);
//        return legend;
//    }

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
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                if (i * 3 + j < 5) {
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
        factory.setPrefSize(120, 120);
        factory.setStyle(String.format("""
        -fx-background-color: %s;
        -fx-background-radius: 10;
        """, CARD_BG));

        // Create grid for tiles (2x2)
        GridPane tiles = new GridPane();
        tiles.setHgap(10);
        tiles.setVgap(10);
        tiles.setAlignment(Pos.CENTER);

        // Create 4 tile spaces (2x2 grid)
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                Circle tileSpace = new Circle(15);
                tileSpace.setFill(Color.web("#374151")); // Empty tile color
                tileSpace.setStroke(Color.web("#4B5563"));
                tileSpace.setStrokeWidth(1);

                // Add some effect to make it look more like a tile space
                tileSpace.setEffect(new InnerShadow(5, Color.web("#000000", 0.2)));

                tiles.add(tileSpace, col, row);
            }
        }

        Label indexLabel = new Label("Factory " + (index + 1));
        indexLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        factory.getChildren().addAll(tiles, indexLabel);
        return factory;
    }

    // In ModernTwoPlayerGameView.java
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
        endTurnButton = createIconButton("➡", "End Turn");
        endRoundButton = createIconButton("🔄", "End Round");

        controlBar.getChildren().addAll(undoButton, saveButton, endTurnButton,endRoundButton, exitButton);
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

        // Style the dialog
        DialogPane dialogPane = help.getDialogPane();
        dialogPane.setStyle("""
            -fx-background-color: #1F2937;
            """);
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

        help.showAndWait();
    }

    private void addKeyboardSupport(StackPane tileSpace, TileColor color, int row, int col) {
        tileSpace.setFocusTraversable(true);
        tileSpace.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER, SPACE -> {
                    // Handle tile selection
                    System.out.println("Selected " + color + " position at [" + row + "," + col + "]");
                }
            }
        });
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
    public HBox getPlayer1Hand() { return player1Hand; }
    public HBox getPlayer2Hand() { return player2Hand; }
    public StackPane getAnimationLayer() { return animationLayer; }
    public Button getEndTurnButton() { return endTurnButton; }
    public Button getEndRoundButton() { return endRoundButton; }
}