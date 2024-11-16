package hr.algebra.azul.view.components;

import hr.algebra.azul.models.TileColor;
import hr.algebra.azul.models.Wall;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class HorizontalPlayerBoardComponent extends HBox {
    // Style constants
    private static final String CARD_BG = "#1F2937";
    private static final String ACCENT_COLOR = "#4F46E5";

    // Core components
    private final VBox scoreSection;
    private final HBox gameSection;
    private final HBox playerHand;
    private final PatternLines patternLines;
    private final FloorLineComponent floorLine;
    private Label scoreLabel;
    private final Label nameLabel;
    private boolean isActive;

    //contsructor
    public HorizontalPlayerBoardComponent(String playerName, boolean isActive) {
        this.isActive = isActive;
        this.nameLabel = new Label(playerName);
        this.scoreSection = new VBox(10);
        this.gameSection = new HBox(15);
        this.playerHand = createPlayerHand();
        this.patternLines = new PatternLines();
        this.floorLine = new FloorLineComponent();

        setupLayout();
        initializeComponents();
    }


    private void setupLayout() {
        setSpacing(20);
        setPadding(new Insets(15));
        setPrefHeight(200);
        setMaxHeight(200);
        setStyle(createBoardStyle());

        // Enable/disable hover effects based on active state
        setOnMouseEntered(e -> {
            if (!isActive) {
                setStyle(createBoardHoverStyle());
            }
        });
        setOnMouseExited(e -> {
            if (!isActive) {
                setStyle(createBoardStyle());
            }
        });
    }

    private void initializeComponents() {
        // Setup score section
        setupScoreSection();

        // Setup game section
        setupGameSection();

        // Add all sections to the board
        getChildren().addAll(scoreSection, gameSection);
    }

    private void setupScoreSection() {
        scoreSection.setAlignment(Pos.CENTER);
        scoreSection.setPrefWidth(120);

        // Style name label
        nameLabel.setStyle("""
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            """);

        // Create and style score label
        scoreLabel = new Label("0");
        scoreLabel.setStyle("""
            -fx-font-size: 24px;
            -fx-text-fill: #60A5FA;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            -fx-background-color: #374151;
            -fx-background-radius: 5;
            """);

        scoreSection.getChildren().addAll(nameLabel, scoreLabel);
    }

    private void setupGameSection() {
        gameSection.setSpacing(15);
        gameSection.setAlignment(Pos.CENTER_LEFT);

        // Create pattern section
        VBox patternSection = new VBox(10);
        patternSection.getChildren().addAll(
                new Label("Pattern Lines") {{
                    setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
                }},
                patternLines
        );

        // Create wall section
        VBox wallSection = new VBox(10);
        wallSection.getChildren().addAll(
                new Label("Wall") {{
                    setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
                }},
                createWallGrid()
        );

        // Create floor line section
        VBox floorLineSection = new VBox(10);
        floorLineSection.getChildren().addAll(
                new Label("Floor Line") {{
                    setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
                }},
                floorLine
        );

        // Add player hand section
        VBox handSection = new VBox(5);
        handSection.getChildren().addAll(
                new Label("Selected Tiles") {{
                    setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
                }},
                playerHand
        );

        gameSection.getChildren().addAll(patternSection, wallSection, handSection, floorLineSection);
    }

    private HBox createPlayerHand() {
        HBox hand = new HBox(8);
        hand.setAlignment(Pos.CENTER_LEFT);
        hand.setPadding(new Insets(10));
        hand.setMinHeight(40);
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

        return hand;
    }

    private GridPane createWallGrid() {
        GridPane wall = new GridPane();
        wall.setHgap(4);
        wall.setVgap(4);
        wall.setPadding(new Insets(5));

        TileColor[][] wallPattern = new Wall().initializeWallPattern();

        for (int row = 0; row < Wall.WALL_SIZE; row++) {
            for (int col = 0; col < Wall.WALL_SIZE; col++) {
                StackPane tileSpace = createWallTileSpace(wallPattern[row][col]);
                wall.add(tileSpace, col, row);
            }
        }

        return wall;
    }

    private StackPane createWallTileSpace(TileColor color) {
        StackPane space = new StackPane();
        space.setPrefSize(30, 30); // Smaller size for horizontal layout
        space.setStyle("""
            -fx-background-color: #1F2937;
            -fx-background-radius: 5;
            -fx-border-color: #374151;
            -fx-border-radius: 5;
            -fx-border-width: 1;
            """);

        // Create pattern indicator
        Region patternIndicator = new Region();
        patternIndicator.setPrefSize(5, 5);
        patternIndicator.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-opacity: 0.3;
            -fx-background-radius: 2.5;
            """, color.getHexCode()));

        space.getChildren().add(patternIndicator);
        return space;
    }

    private String createBoardStyle() {
        return String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 10;
            %s
            """,
                CARD_BG,
                isActive ? "-fx-border-color: " + ACCENT_COLOR + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;" : ""
        );
    }

    private String createBoardHoverStyle() {
        return """
            -fx-background-color: #262f3d;
            -fx-background-radius: 10;
            """;
    }

    // Public methods for state management
    public void setActive(boolean active) {
        this.isActive = active;
        setStyle(createBoardStyle());
    }

    public void updateScore(int score) {
        scoreLabel.setText(String.valueOf(score));
    }

    public PatternLines getPatternLines() {
        return patternLines;
    }

    public FloorLineComponent getFloorLine() {
        return floorLine;
    }

    public HBox getPlayerHand() {
        return playerHand;
    }
}