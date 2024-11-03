    package hr.algebra.azul.controllers;

    import hr.algebra.azul.helper.*;
    import hr.algebra.azul.models.*;
    import hr.algebra.azul.view.ModernTwoPlayerGameView;
    import javafx.animation.*;
    import javafx.application.Platform;
    import javafx.beans.property.IntegerProperty;
    import javafx.beans.property.SimpleIntegerProperty;
    import javafx.collections.ListChangeListener;
    import javafx.geometry.Bounds;
    import javafx.geometry.Pos;
    import javafx.scene.Node;
    import javafx.scene.control.*;
    import javafx.scene.effect.DropShadow;
    import javafx.scene.effect.InnerShadow;
    import javafx.scene.layout.*;
    import javafx.scene.paint.Color;
    import javafx.scene.shape.Circle;
    import javafx.scene.shape.LineTo;
    import javafx.scene.shape.MoveTo;
    import javafx.scene.shape.Path;
    import javafx.stage.Stage;
    import javafx.util.Duration;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Optional;
    import java.util.concurrent.atomic.AtomicBoolean;
    import java.util.stream.Collectors;

    public class ModernTwoPlayerGameController {
        private PatternLineInteractionHandler patternLineInteractionHandler;
        private WallTilingManager wallTilingManager;
        private final ModernTwoPlayerGameView view;
        private final GameModel gameModel;
        private final Stage primaryStage;
        private Timeline timer;
        private final IntegerProperty timeRemaining = new SimpleIntegerProperty(150); // 2:30 in seconds
        private boolean isGamePaused = false;
        private static final Duration ANIMATION_DURATION = Duration.millis(500);
        private TileAnimationManager animationManager;
        private TurnManager turnManager;
        // Track UI state
        private int selectedFactoryIndex = -1;
        private TileColor selectedColor = null;

        // Style constants
        private static final String DIALOG_BG = "#1F2937";
        private static final String DIALOG_HEADER_BG = "#111827";
        private static final String BUTTON_BG = "#3B82F6";
        private static final String BUTTON_HOVER_BG = "#2563EB";
        private static final String TEXT_PRIMARY = "white";
        private static final String TEXT_SECONDARY = "#9CA3AF";
        private static final String CARD_BG = "#1F2937";
        private static final String ACCENT_COLOR = "#4F46E5";

        public ModernTwoPlayerGameController(ModernTwoPlayerGameView view, Stage primaryStage) {
            this.view = view;
            this.primaryStage = primaryStage;
            this.gameModel = new GameModel(2);
            this.wallTilingManager = new WallTilingManager(view, gameModel);
            turnManager = new TurnManager(gameModel, view, view.getTimerLabel());
            this.patternLineInteractionHandler = new PatternLineInteractionHandler(view, gameModel, turnManager);
            initializeController();
            updateEntireView();
        }

        private void initializeController() {
            setupButtonHandlers();
            setupFactoryClickHandlers();
            setupPatternLineHandlers();
            setupCenterPoolClickHandlers();
            initializeTimer();
            turnManager.setRoundEndCallback(this::handleRoundEnd);

            view.getStage().setOnCloseRequest(e -> {
                e.consume();
                handleExitClick();
            });

            this.animationManager = new TileAnimationManager(view.getAnimationLayer());
        }

        private void setupButtonHandlers() {
            view.getUndoButton().setOnAction(e -> handleUndoClick());
            view.getSaveButton().setOnAction(e -> handleSaveClick());
            view.getExitButton().setOnAction(e -> handleExitClick());
            view.getSettingsButton().setOnAction(e -> handleSettingsClick());
            view.getEndTurnButton().setOnAction(e -> turnManager.handleEndTurn());
            view.getEndRoundButton().setOnAction(e -> handleRoundEnd());
        }


        private void setupPatternLineHandlers() {
            setupPlayerPatternLines(view.getPlayer1Board());
            setupPlayerPatternLines(view.getPlayer2Board());
        }

        private void setupPlayerPatternLines(VBox playerBoard) {
            // Find the VBox containing pattern lines - it's after the header and hand
            VBox patternLinesContainer = patternLineInteractionHandler.findPatternLinesContainer(playerBoard);
            if (patternLinesContainer != null) {
                // Skip the label and get the pattern lines container
                for (int i = 1; i < patternLinesContainer.getChildren().size(); i++) {
                    Node node = patternLinesContainer.getChildren().get(i);
                    if (node instanceof HBox patternLine) {
                        final int lineIndex = i - 1; // Adjust index to account for label
                        patternLine.setOnMouseClicked(e -> handlePatternLineClick(lineIndex));
                    }
                }
            }
        }



        private void handleFactoryClick(Circle clickedTile, VBox factory, int factoryIndex) {
            if (isGamePaused) return;

            // Get the color of the clicked tile
            Color tileColor = (Color) clickedTile.getFill();
            Factory factoryModel = gameModel.getFactories().get(factoryIndex);

            // Get tiles of the selected color
            List<Tile> selectedTiles = factoryModel.selectTilesByColor(getTileColorFromFill(tileColor));
            List<Tile> remainingTiles = factoryModel.removeRemainingTiles();

            // Get the current player's hand
            HBox playerHand = getCurrentPlayerHand();

            // Start animation
            animationManager.animateFactorySelection(
                    factory,
                    selectedTiles,
                    remainingTiles,
                    playerHand,
                    view.getCenterPool(),
                    () -> {
                        // Update model and UI after animation completes
                        updatePlayerHand(selectedTiles);
                        gameModel.addTilesToCenter(remainingTiles);
                        updateFactoryDisplay(factory);
                        updateCenterPool();

                        // Set up pattern line interactions after updating hand
                        patternLineInteractionHandler.setupPatternLineInteractions();
                    }
            );
        }

        private void setupFactoryClickHandlers() {
            GridPane factoriesGrid = view.getFactoriesContainer();
            for (int i = 0; i < factoriesGrid.getChildren().size(); i++) {
                final int factoryIndex = i;
                VBox factory = (VBox) factoriesGrid.getChildren().get(i);
                GridPane tileGrid = (GridPane) factory.getChildren().get(0);

                for (Node node : tileGrid.getChildren()) {
                    if (node instanceof Circle tile) {
                        tile.setOnMouseClicked(e -> handleFactoryClick(tile, factory, factoryIndex));
                    }
                }
            }
        }

        private void setupCenterPoolClickHandlers() {
            VBox centerPool = view.getCenterPool();
            FlowPane tilesContainer = (FlowPane) centerPool.getChildren().get(1); // Get the tiles container

            tilesContainer.getChildren().addListener((ListChangeListener<Node>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        for (Node node : change.getAddedSubList()) {
                            if (node instanceof Circle tile) {
                                setupCenterTileInteraction(tile, centerPool);
                            }
                        }
                    }
                }
            });
        }

        private void setupCenterTileInteraction(Circle tile, VBox centerPool) {
            // Skip if it's the first player token
            if (tile.getFill() == null) return;

            // Add hover effects
            setupTileHoverEffectsForCenter(tile, (Color) tile.getFill(), centerPool);

            // Add click handler
            tile.setOnMouseClicked(e -> handleCenterTileClick(tile, centerPool));
        }

        private void setupTileHoverEffectsForCenter(Circle tileCircle, Color tileColor, VBox centerPool) {
            // Create glow effect
            DropShadow glow = new DropShadow();
            glow.setColor(tileColor);
            glow.setRadius(10);
            glow.setSpread(0.3);

            tileCircle.setOnMouseEntered(e -> {
                // Highlight the center pool
                centerPool.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 10;
                -fx-border-color: #60A5FA;
                -fx-border-width: 2;
                -fx-border-radius: 10;
                """, CARD_BG));

                // Highlight all tiles of the same color
                FlowPane tilesContainer = (FlowPane) centerPool.getChildren().get(1);
                for (Node node : tilesContainer.getChildren()) {
                    if (node instanceof Circle circle && circle.getFill().equals(tileColor)) {
                        // Scale up effect
                        ScaleTransition scale = new ScaleTransition(Duration.millis(100), circle);
                        scale.setToX(1.1);
                        scale.setToY(1.1);
                        scale.play();

                        // Add glow effect
                        circle.setEffect(glow);

                        // Add highlight stroke
                        circle.setStroke(Color.web("#60A5FA"));
                        circle.setStrokeWidth(2);
                    }
                }
            });

            tileCircle.setOnMouseExited(e -> {
                // Reset center pool style
                centerPool.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 10;
                """, CARD_BG));

                // Reset all tiles
                FlowPane tilesContainer = (FlowPane) centerPool.getChildren().get(1);
                for (Node node : tilesContainer.getChildren()) {
                    if (node instanceof Circle circle && circle.getFill().equals(tileColor)) {
                        // Scale down effect
                        ScaleTransition scale = new ScaleTransition(Duration.millis(100), circle);
                        scale.setToX(1.0);
                        scale.setToY(1.0);
                        scale.play();

                        // Reset effect
                        circle.setEffect(new InnerShadow(5, Color.web("#000000", 0.2)));

                        // Reset stroke
                        circle.setStroke(Color.web("#4B5563"));
                        circle.setStrokeWidth(1);
                    }
                }
            });
        }

        private void handleCenterTileClick(Circle clickedTile, VBox centerPool) {
            if (isGamePaused || getCurrentPlayerHand().getChildren().size() > 0) return;

            // Get the color of the clicked tile
            Color tileColor = (Color) clickedTile.getFill();
            TileColor selectedColor = getTileColorFromFill(tileColor);

            // Get all tiles of the same color from center
            List<Tile> selectedTiles = collectTilesFromCenter(selectedColor);

            // Handle first player token if present
            boolean hasFirstPlayerToken = handleFirstPlayerToken();

            // Get the current player's hand
            HBox playerHand = getCurrentPlayerHand();

            // Start animation
            animateSelectionFromCenter(centerPool, selectedTiles, playerHand, hasFirstPlayerToken);
        }

        private List<Tile> collectTilesFromCenter(TileColor color) {
            List<Tile> selectedTiles = new ArrayList<>();
            List<Circle> tilesToRemove = new ArrayList<>();

            FlowPane tilesContainer = (FlowPane) view.getCenterPool().getChildren().get(1);

            tilesContainer.getChildren().forEach(node -> {
                if (node instanceof Circle tile) {
                    Color tileColor = (Color) tile.getFill();
                    if (getTileColorFromFill(tileColor) == color) {
                        selectedTiles.add(new Tile(color));
                        tilesToRemove.add(tile);
                    }
                }
            });

            // Remove selected tiles from view
            tilesContainer.getChildren().removeAll(tilesToRemove);

            return selectedTiles;
        }

        private boolean handleFirstPlayerToken() {
            FlowPane tilesContainer = (FlowPane) view.getCenterPool().getChildren().get(1);
            AtomicBoolean tokenFound = new AtomicBoolean(false);

            tilesContainer.getChildren().removeIf(node -> {
                if (node instanceof Circle tile && tile.getFill() == null) {
                    // This is the first player token
                    tokenFound.set(true);
                    // Add token to current player's floor line
                    gameModel.getCurrentPlayer().getFloorLine().addTile(new Tile(null));
                    return true;
                }
                return false;
            });

            return tokenFound.get();
        }

        private void animateSelectionFromCenter(VBox centerPool, List<Tile> selectedTiles,
                                                HBox playerHand, boolean hasFirstPlayerToken) {
            // Create visual representations of selected tiles
            List<Circle> tileCircles = createTileCircles(selectedTiles);

            // Get initial and target positions for animation
            Bounds centerBounds = centerPool.localToScene(centerPool.getBoundsInLocal());
            Bounds handBounds = playerHand.localToScene(playerHand.getBoundsInLocal());

            // Create animation
            ParallelTransition animation = new ParallelTransition();

            // Add path transitions for each tile
            for (Circle tileCircle : tileCircles) {
                PathTransition path = createPathTransition(
                        centerBounds.getCenterX(), centerBounds.getCenterY(),
                        handBounds.getCenterX(), handBounds.getCenterY(),
                        tileCircle,
                        Duration.millis(500)
                );
                animation.getChildren().add(path);
            }

            // After animation completes
            animation.setOnFinished(e -> {
                // Update player's hand
                updatePlayerHand(selectedTiles);

                // Update game state
                if (hasFirstPlayerToken) {
                    gameModel.getCurrentPlayer().getFloorLine().addTile(new Tile(null));
                }

                // Set up pattern line interactions
                patternLineInteractionHandler.setupPatternLineInteractions();

                // Check if center and factories are empty to end round
                if (isCenterEmpty() && areFactoriesEmpty()) {
                    Platform.runLater(() -> handleRoundEnd());

                }
            });

            animation.play();
        }

        private PathTransition createPathTransition(double startX, double startY,
                                                    double endX, double endY,
                                                    Node node, Duration duration) {
            Path path = new Path();
            path.getElements().addAll(
                    new MoveTo(startX, startY),
                    new LineTo(endX, endY)
            );

            PathTransition transition = new PathTransition(duration, path, node);
            transition.setInterpolator(Interpolator.EASE_OUT);
            return transition;
        }

        private boolean isCenterEmpty() {
            FlowPane tilesContainer = (FlowPane) view.getCenterPool().getChildren().get(1);
            return tilesContainer.getChildren().isEmpty();
        }

        private boolean areFactoriesEmpty() {
            return gameModel.getFactories().stream().allMatch(Factory::isEmpty);
        }

        private void animateSelectionAndMove(VBox factoryNode, List<Tile> selectedTiles, List<Tile> remainingTiles) {
            // Create visual representations of the tiles
            List<Circle> selectedCircles = createTileCircles(selectedTiles);
            List<Circle> remainingCircles = createTileCircles(remainingTiles);

            // Get target locations
            HBox playerHand = getCurrentPlayerHand();
            VBox centerPool = view.getCenterPool();

            // Create and play animations
            ParallelTransition allAnimations = new ParallelTransition();

            // Animate selected tiles to hand
            for (Circle tileCircle : selectedCircles) {
                PathTransition pathToHand = createPathTransition(
                        factoryNode,
                        playerHand,
                        tileCircle,
                        ANIMATION_DURATION
                );
                allAnimations.getChildren().add(pathToHand);
            }

            // Animate remaining tiles to center
            for (Circle tileCircle : remainingCircles) {
                PathTransition pathToCenter = createPathTransition(
                        factoryNode,
                        centerPool,
                        tileCircle,
                        ANIMATION_DURATION
                );
                allAnimations.getChildren().add(pathToCenter);
            }

            // After animations complete
            allAnimations.setOnFinished(e -> {
                updatePlayerHand(selectedTiles);
                gameModel.addTilesToCenter(remainingTiles);
                updateFactoryDisplay(factoryNode);
                updateCenterPool();
            });

            allAnimations.play();
        }


        private List<Circle> createTileCircles(List<Tile> tiles) {
            return tiles.stream().map(tile -> {
                Circle circle = GameUIConstants.createBaseCircle();
                circle.setFill(Color.web(tile.getColor().getHexCode()));
                circle.setEffect(GameUIConstants.createTileShadow());
                return circle;
            }).collect(Collectors.toList());
        }

        private PathTransition createPathTransition(Node source, Node target, Node movingNode, Duration duration) {
            // Get source and target coordinates in scene
            Bounds sourceBounds = source.localToScene(source.getBoundsInLocal());
            Bounds targetBounds = target.localToScene(target.getBoundsInLocal());

            // Create path
            Path path = new Path();
            path.getElements().add(new MoveTo(
                    sourceBounds.getCenterX(),
                    sourceBounds.getCenterY()
            ));
            path.getElements().add(new LineTo(
                    targetBounds.getCenterX(),
                    targetBounds.getCenterY()
            ));

            // Create transition
            PathTransition transition = new PathTransition(duration, path, movingNode);
            transition.setAutoReverse(false);

            return transition;
        }

        private HBox getCurrentPlayerHand() {
            return gameModel.getCurrentPlayer() == gameModel.getPlayers().get(0)
                    ? view.getPlayer1Hand()
                    : view.getPlayer2Hand();
        }

        private void updatePlayerHand(List<Tile> tiles) {
            HBox hand = getCurrentPlayerHand();
            hand.getChildren().clear();

            // Update model
            gameModel.getCurrentPlayer().clearHand();
            gameModel.getCurrentPlayer().addTilesToHand(tiles);

            // Update UI
            for (Tile tile : tiles) {
                Circle tileCircle = new Circle(15);
                tileCircle.setFill(Color.web(tile.getColor().getHexCode()));
                tileCircle.setStroke(Color.web("#4B5563"));
                hand.getChildren().add(tileCircle);
            }
            patternLineInteractionHandler.setupPatternLineInteractions();
        }

        private void clearPlayerHand() {
            getCurrentPlayerHand().getChildren().clear();
        }

        private void updateFactoryDisplay(VBox factory) {
            GridPane tileGrid = (GridPane) factory.getChildren().get(0);
            tileGrid.getChildren().clear();
        }

        private void showTileColorSelectionDialog(List<Tile> tiles) {
            Dialog<TileColor> dialog = new Dialog<>();
            dialog.setTitle("Select Tiles");
            dialog.setHeaderText("Choose tile color to take");

            DialogPane dialogPane = dialog.getDialogPane();
            VBox colorOptions = new VBox(10);
            colorOptions.setAlignment(Pos.CENTER);

            // Count tiles of each color
            tiles.stream()
                    .map(Tile::getColor)
                    .distinct()
                    .forEach(color -> {
                        long count = tiles.stream()
                                .filter(t -> t.getColor() == color)
                                .count();

                        Button colorButton = createColorButton(color, count);
                        colorButton.setOnAction(e -> dialog.setResult(color));
                        colorOptions.getChildren().add(colorButton);
                    });

            dialogPane.setContent(colorOptions);
            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            styleDialog(dialog);

            Optional<TileColor> result = dialog.showAndWait();
            result.ifPresent(color -> {
                selectedColor = color;
                updateFactorySelection(color);
            });
        }

        private Button createColorButton(TileColor color, long count) {
            Button button = new Button(color.toString() + " (" + count + ")");
            button.setPrefWidth(200);
            button.setPrefHeight(40);
            button.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-padding: 10 20;
                -fx-background-radius: 5;
                -fx-font-size: 14px;
                """, color.getHexCode()));

            button.setOnMouseEntered(e ->
                    button.setStyle(button.getStyle() + "-fx-background-color: " + getDarkerColor(color.getHexCode()) + ";")
            );

            button.setOnMouseExited(e ->
                    button.setStyle(button.getStyle() + "-fx-background-color: " + color.getHexCode() + ";")
            );

            return button;
        }

        private String getDarkerColor(String hexColor) {
            Color color = Color.web(hexColor);
            return String.format("#%02X%02X%02X",
                    (int)(color.getRed() * 0.8 * 255),
                    (int)(color.getGreen() * 0.8 * 255),
                    (int)(color.getBlue() * 0.8 * 255));
        }

        //TODO: Duplicate? Check if needed
        private void handlePatternLineClick(int lineIndex) {
            if (isGamePaused) return;

            // Get the current player's hand
            HBox playerHand = getCurrentPlayerHand();
            if (playerHand.getChildren().isEmpty()) {
                return; // No tiles selected
            }

            // Get selected tiles from hand
            List<Tile> selectedTiles = new ArrayList<>();
            for (Node node : playerHand.getChildren()) {
                if (node instanceof Circle circle) {
                    // Convert the circle's fill color back to a TileColor
                    Color fillColor = (Color) circle.getFill();
                    TileColor tileColor = getTileColorFromFill(fillColor);
                    if (tileColor != null) {
                        selectedTiles.add(new Tile(tileColor));
                    }
                }
            }

            if (selectedTiles.isEmpty()) return;

            // Try to place tiles in the pattern line
            if (gameModel.selectTilesFromFactory(selectedFactoryIndex, selectedTiles.get(0).getColor(), lineIndex)) {
                // Animate tiles moving to pattern line
                //animateTilesToPatternLine(playerHand, lineIndex, selectedTiles);
            }
        }


        private TileColor getTileColorFromFill(Color fillColor) {
            // Convert the JavaFX color to hex format
            String hexColor = String.format("#%02X%02X%02X",
                    (int) (fillColor.getRed() * 255),
                    (int) (fillColor.getGreen() * 255),
                    (int) (fillColor.getBlue() * 255));

            // Match with TileColor enum
            for (TileColor color : TileColor.values()) {
                if (color.getHexCode().equalsIgnoreCase(hexColor)) {
                    return color;
                }
            }

            System.out.println("Warning: No matching TileColor found for " + hexColor);
            return null;
        }

        private void updateEntireView() {
            Platform.runLater(() -> {
                updateFactories();
                updateCenterPool();
                updatePlayerBoards();
                updateScores();
                updateCurrentPlayer();
                updateGameState();
            });
        }

        private void updateFactories() {
            List<Factory> factories = gameModel.getFactories();
            GridPane factoriesGrid = view.getFactoriesContainer();

            for (int i = 0; i < factories.size(); i++) {
                Factory factory = factories.get(i);
                VBox factoryBox = (VBox) factoriesGrid.getChildren().get(i);
                GridPane tileGrid = (GridPane) factoryBox.getChildren().get(0);
                List<Tile> factoryTiles = factory.getTiles();

                // Clear existing tiles
                tileGrid.getChildren().clear();

                // Create and store the tile circles for this factory
                List<Circle> factoryCircles = new ArrayList<>();

                // Add new tiles
                int tileIndex = 0;
                final int factoryIndex = i;
                for (int row = 0; row < 2; row++) {
                    for (int col = 0; col < 2; col++) {
                        Circle tileCircle = new Circle(15);

                        if (tileIndex < factoryTiles.size()) {
                            Tile tile = factoryTiles.get(tileIndex);
                            Color tileColor = Color.web(tile.getColor().getHexCode());
                            tileCircle.setFill(tileColor);

                            // Add hover effects
                            setupTileHoverEffects(tileCircle, tileColor, factoryBox, tileGrid);

                            // Add click handler for non-empty tiles
                            tileCircle.setOnMouseClicked(e -> handleFactoryClick(tileCircle, factoryBox, factoryIndex));
                        } else {
                            tileCircle.setFill(Color.web("#374151")); // Empty tile color
                        }

                        tileCircle.setStroke(Color.web("#4B5563"));
                        tileCircle.setStrokeWidth(1);
                        tileCircle.setEffect(new InnerShadow(5, Color.web("#000000", 0.2)));

                        factoryCircles.add(tileCircle);
                        tileGrid.add(tileCircle, col, row);
                        tileIndex++;
                    }
                }
            }
        }

        private void setupTileHoverEffects(Circle tileCircle, Color tileColor, VBox factoryBox, GridPane tileGrid) {
            // Create glow effect for hover
            DropShadow glow = new DropShadow();
            glow.setColor(tileColor);
            glow.setRadius(10);
            glow.setSpread(0.3);

            tileCircle.setOnMouseEntered(e -> {
                // Highlight the factory box
                factoryBox.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 10;
                -fx-border-color: #60A5FA;
                -fx-border-width: 2;
                -fx-border-radius: 10;
                """, CARD_BG));

                // Highlight all tiles of the same color
                for (Node node : tileGrid.getChildren()) {
                    if (node instanceof Circle circle) {
                        if (circle.getFill().equals(tileColor)) {
                            // Scale up effect
                            ScaleTransition scale = new ScaleTransition(Duration.millis(100), circle);
                            scale.setToX(1.1);
                            scale.setToY(1.1);
                            scale.play();

                            // Add glow effect
                            circle.setEffect(glow);

                            // Add highlight stroke
                            circle.setStroke(Color.web("#60A5FA"));
                            circle.setStrokeWidth(2);
                        }
                    }
                }
            });

            tileCircle.setOnMouseExited(e -> {
                // Reset factory box style
                factoryBox.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 10;
                """, CARD_BG));

                // Reset all tiles
                for (Node node : tileGrid.getChildren()) {
                    if (node instanceof Circle circle) {
                        if (circle.getFill().equals(tileColor)) {
                            // Scale down effect
                            ScaleTransition scale = new ScaleTransition(Duration.millis(100), circle);
                            scale.setToX(1.0);
                            scale.setToY(1.0);
                            scale.play();

                            // Reset effect
                            circle.setEffect(new InnerShadow(5, Color.web("#000000", 0.2)));

                            // Reset stroke
                            circle.setStroke(Color.web("#4B5563"));
                            circle.setStrokeWidth(1);
                        }
                    }
                }
            });
        }
        private void updateCenterPool() {
            VBox centerPool = view.getCenterPool();
            FlowPane tilesContainer = (FlowPane) centerPool.getChildren().get(1);
            updateTileDisplay(tilesContainer, gameModel.getCenterPool());
        }

        private void updateFactorySelection(TileColor selectedColor) {
            // Update visual selection state
            GridPane factoriesGrid = view.getFactoriesContainer();
            for (int i = 0; i < factoriesGrid.getChildren().size(); i++) {
                VBox factory = (VBox) factoriesGrid.getChildren().get(i);
                boolean isSelected = i == selectedFactoryIndex;

                factory.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-background-radius: 10;
                    -fx-border-color: %s;
                    -fx-border-width: %d;
                    -fx-border-radius: 8;
                    """,
                        CARD_BG,
                        isSelected ? ACCENT_COLOR : "transparent",
                        isSelected ? 2 : 0
                ));
            }
        }

        private void updateTileDisplay(Pane container, List<Tile> tiles) {
            container.getChildren().clear();
            for (Tile tile : tiles) {
                if (tile.getColor() != null) {  // Skip first player token
                    Circle tileCircle = new Circle(15);
                    tileCircle.setFill(Color.web(tile.getColor().getHexCode()));
                    tileCircle.setStroke(Color.web("#4B5563"));
                    container.getChildren().add(tileCircle);
                }
            }
        }

        private void updatePlayerBoards() {
            List<Player> players = gameModel.getPlayers();
            updatePlayerBoard(view.getPlayer1Board(), players.get(0));
            updatePlayerBoard(view.getPlayer2Board(), players.get(1));
        }

        private void updatePlayerBoard(VBox playerBoard, Player player) {
            //updateSinglePlayerPatternLines (playerBoard, player);
            updateWall(playerBoard, player);
            updateFloorLine(playerBoard, player);
        }

        private void updateSinglePlayerPatternLines(VBox playerBoard, Player player) {
            VBox patternLinesContainer = patternLineInteractionHandler.findPatternLinesContainer(playerBoard);
            if (patternLinesContainer == null) return;

            List<PatternLine> playerPatternLines = player.getPatternLines();

            // Start from index 1 to skip the label
            for (int i = 1; i <= playerPatternLines.size(); i++) {
                HBox lineContainer = (HBox) patternLinesContainer.getChildren().get(i);
                PatternLine patternLine = playerPatternLines.get(i - 1);
                updateSinglePatternLine(lineContainer, patternLine);
            }
        }

        private void updateSinglePatternLine(HBox lineContainer, PatternLine patternLine) {
            for (int j = 0; j < lineContainer.getChildren().size(); j++) {
                Circle space = (Circle) lineContainer.getChildren().get(j);
                List<Tile> tiles = patternLine.getTiles();
                if (j < tiles.size()) {
                    Tile tile = tiles.get(j);
                    space.setFill(Color.web(tile.getColor().getHexCode()));
                } else {
                    space.setFill(Color.web(CARD_BG));
                }
            }
        }

        private void updatePatternLines() {
            Platform.runLater(() -> {
                updatePlayerPatternLines(view.getPlayer1Board(), gameModel.getPlayers().get(0));
                updatePlayerPatternLines(view.getPlayer2Board(), gameModel.getPlayers().get(1));
            });
        }

        private void updatePlayerPatternLines(VBox playerBoard, Player player) {
            VBox patternLinesContainer = patternLineInteractionHandler.findPatternLinesContainer(playerBoard);
            if (patternLinesContainer == null) return;

            List<PatternLine> playerPatternLines = player.getPatternLines(); // Changed from PatternLines to PatternLine

            // Start from index 1 to skip the label
            for (int i = 1; i <= playerPatternLines.size(); i++) {
                HBox lineContainer = (HBox) patternLinesContainer.getChildren().get(i);
                PatternLine patternLine = playerPatternLines.get(i - 1); // Changed from PatternLines

                // Update each space in the pattern line
                updatePatternLine(lineContainer, patternLine);
            }
        }

        private void updateWall(VBox playerBoard, Player player) {
            GridPane wall = (GridPane) playerBoard.getChildren().stream()
                    .filter(node -> node instanceof GridPane)
                    .findFirst()
                    .orElse(null);

            if (wall != null) {
                for (int row = 0; row < 5; row++) {
                    for (int col = 0; col < 5; col++) {
                        StackPane space = (StackPane) wall.getChildren().get(row * 5 + col);
                        if (player.wall.getTiles()[row][col]) {
                            Circle tile = (Circle) space.getChildren().get(2);
                            tile.setFill(Color.web(player.wall.getWallPattern()[row][col].getHexCode()));
                            tile.setOpacity(1.0);
                        }
                    }
                }
            }
        }

        private void updateFloorLine(VBox playerBoard, Player player) {
            HBox floorLine = (HBox) playerBoard.getChildren().stream()
                    .filter(node -> node instanceof HBox)
                    .reduce((first, second) -> second) // Get the last HBox (floor line)
                    .orElse(null);

            if (floorLine != null) {
                // Clear existing floor line
                floorLine.getChildren().clear();

                // Create new floor line spaces
                for (int i = 0; i < FloorLine.MAX_TILES; i++) {
                    Circle space = new Circle(15);
                    space.setFill(Color.web(CARD_BG)); // Empty space color
                    space.setStroke(Color.web("#4B5563"));
                    space.setStrokeWidth(1);

                    // If there's a tile at this position, color it
                    Tile tile = player.getFloorLine().getTileAt(i);
                    if (tile != null && tile.getColor() != null) {
                        space.setFill(Color.web(tile.getColor().getHexCode()));
                    }

                    floorLine.getChildren().add(space);
                }
            }
        }

        private void updatePatternLine(HBox lineContainer, PatternLine patternLine) { // Changed parameter type
            List<Tile> tiles = patternLine.getTiles();
            for (int i = 0; i < lineContainer.getChildren().size(); i++) {
                Circle space = (Circle) lineContainer.getChildren().get(i);
                if (i < tiles.size()) {
                    space.setFill(Color.web(tiles.get(i).getColor().getHexCode()));
                } else {
                    space.setFill(Color.web("#374151")); // Empty space color
                }
            }
        }

        private void updateScores() {
            List<Player> players = gameModel.getPlayers();
            updatePlayerScore(view.getPlayer1Board(), players.get(0));
            updatePlayerScore(view.getPlayer2Board(), players.get(1));
        }

        private void updatePlayerScore(VBox playerBoard, Player player) { // Changed parameter
            HBox header = (HBox) playerBoard.getChildren().get(0);
            Label scoreLabel = (Label) header.getChildren().get(2);
            scoreLabel.setText(String.valueOf(player.getScore())); // Use getter instead of direct field access
        }

        private void updateCurrentPlayer() {
            Player current = gameModel.getCurrentPlayer();
            view.getCurrentPlayerLabel().setText(current.name + "'s Turn");

            view.getPlayer1Board().setStyle(getPlayerBoardStyle(current == gameModel.getPlayers().get(0)));
            view.getPlayer2Board().setStyle(getPlayerBoardStyle(current == gameModel.getPlayers().get(1)));
        }

        private void updateGameState() {
            switch (gameModel.getGameState()) {
                case GAME_END -> handleGameEnd();
                case SCORING -> handleRoundEnd();
            }
        }

        private void initializeTimer() {
            timer = new Timeline(
                    new KeyFrame(Duration.seconds(1), e -> updateTimer())
            );
            timer.setCycleCount(Timeline.INDEFINITE);
            timer.play();
            timeRemaining.set(150);
        }

        private void updateTimer() {
            if (timeRemaining.get() > 0) {
                Platform.runLater(() -> {
                    timeRemaining.set(timeRemaining.get() - 1);
                    int minutes = timeRemaining.get() / 60;
                    int seconds = timeRemaining.get() % 60;
                    view.getTimerLabel().setText(String.format("⏱ %02d:%02d", minutes, seconds));

                    if (timeRemaining.get() == 30) {
                        showTimeWarning();
                    }
                });
            } else {
                timer.stop();
                Platform.runLater(this::handleTimeOut);
            }
        }

        private void resetTimer() {
            timeRemaining.set(150);
            view.getTimerLabel().setStyle("-fx-text-fill: #9CA3AF;");
        }

        private void showTimeWarning() {
            view.getTimerLabel().setStyle("-fx-text-fill: #EF4444;");
        }

        private void handleTimeOut() {
            showMessage("Time Out", "Your turn has ended due to time out.", Alert.AlertType.WARNING);
            gameModel.endTurn();
            updateEntireView();
            resetTimer();
        }

        private void handleUndoClick() {
    //        //TODO: Implement undo functionality
    //        if (!gameModel.canUndo()) {
    //            showMessage("Cannot Undo", "No moves available to undo.", Alert.AlertType.INFORMATION);
    //            return;
    //        }
            //gameModel.undo();
            updateEntireView();
        }

        private void handleSaveClick() {
            showMessage("Game Saved", "Your game has been saved successfully.", Alert.AlertType.INFORMATION);
        }

        private void handleExitClick() {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit Game");
            alert.setHeaderText("Are you sure you want to exit?");
            alert.setContentText("Any unsaved progress will be lost.");

            styleDialog(alert);

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    timer.stop();
                    view.getStage().close();
                }
            });
        }

        private void handleSettingsClick() {
            isGamePaused = true;
            timer.pause();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Game Settings");
            dialog.setHeaderText("Settings");

            VBox content = new VBox(10);

            // Add game settings controls
            CheckBox muteAudioBox = new CheckBox("Mute Audio");
            muteAudioBox.setStyle("-fx-text-fill: white;");

            Slider timerSlider = new Slider(60, 180, 150);
            timerSlider.setShowTickLabels(true);
            timerSlider.setShowTickMarks(true);
            timerSlider.setMajorTickUnit(30);
            Label timerLabel = new Label("Turn Timer (seconds): ");
            timerLabel.setStyle("-fx-text-fill: white;");

            content.getChildren().addAll(
                    muteAudioBox,
                    new VBox(5, timerLabel, timerSlider)
            );

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            styleDialog(dialog);

            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Apply settings
                    timeRemaining.set((int) timerSlider.getValue());
                }
                isGamePaused = false;
                timer.play();
            });
        }

        private void showMessage(String title, String content, Alert.AlertType type) {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(title);
            alert.setContentText(content);

            styleDialog(alert);
            alert.showAndWait();
        }

        private void showInvalidMoveDialog() {
            showMessage("Invalid Move", "This move is not allowed.", Alert.AlertType.WARNING);
        }

        private boolean isRoundComplete() {
            return gameModel.getFactories().stream()
                    .allMatch(factory -> factory.isEmpty()) &&
                    gameModel.getCenterPool().isEmpty();
        }

        private void handleRoundEnd() {
            // Process wall tiling
            wallTilingManager.processWallTiling();

            // Reset game state for next round
            gameModel.initializeGame();
            gameModel.setPlayerTurn(gameModel.getPlayers().get(0)); // Reset to first player

            // Update the view
            updateEntireView();

            // Reset timer
            turnManager.resetTimer();

            // Show round end dialog
            Platform.runLater(() -> showRoundEndDialog());
        }

        private void showRoundEndDialog() {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Round Complete");
            dialog.setHeaderText("Round Summary");

            VBox content = new VBox(15);
            content.setAlignment(Pos.CENTER);

            // Add round summary information
            List<Player> players = gameModel.getPlayers();
            for (Player player : players) {
                VBox playerSummary = new VBox(5);
                Label nameLabel = new Label(player.getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
                Label scoreLabel = new Label("Score: " + player.getScore());
                scoreLabel.setStyle("-fx-text-fill: #9CA3AF;");
                playerSummary.getChildren().addAll(nameLabel, scoreLabel);
                content.getChildren().add(playerSummary);
            }

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            // Style the dialog
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setStyle("""
        -fx-background-color: #1F2937;
        """);
            dialogPane.lookup(".content").setStyle("-fx-background-color: #1F2937;");
            dialogPane.lookup(".header-panel").setStyle("""
        -fx-background-color: #111827;
        -fx-padding: 20px;
        """);
            dialogPane.lookup(".header-panel .label").setStyle("""
        -fx-text-fill: white;
        -fx-font-size: 18px;
        -fx-font-weight: bold;
        """);

            // Style buttons
            dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle("""
        -fx-background-color: #3B82F6;
        -fx-text-fill: white;
        -fx-padding: 8 15;
        -fx-background-radius: 5;
        """);

            dialog.showAndWait();
        }


        private void handleGameEnd() {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Game Over");
            dialog.setHeaderText("Final Scores");

            VBox content = new VBox(20);
            content.setAlignment(Pos.CENTER);

            // Display final scores and winner
            List<Player> players = gameModel.getPlayers();
            Player winner = players.stream()
                    .max((p1, p2) -> Integer.compare(p1.score, p2.score))
                    .orElse(null);

            // Winner announcement
            if (winner != null) {
                Label winnerLabel = new Label(winner.name + " Wins!");
                winnerLabel.setStyle("""
                    -fx-font-size: 24px;
                    -fx-font-weight: bold;
                    -fx-text-fill: #22C55E;
                    """);
                content.getChildren().add(winnerLabel);
            }

            // Player scores
            for (Player player : players) {
                HBox playerScore = new HBox(10);
                playerScore.setAlignment(Pos.CENTER);

                Label nameLabel = new Label(player.name);
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

                Label scoreLabel = new Label("Final Score: " + player.score);
                scoreLabel.setStyle("-fx-text-fill: #9CA3AF;");

                playerScore.getChildren().addAll(nameLabel, scoreLabel);
                content.getChildren().add(playerScore);
            }

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            styleDialog(dialog);

            dialog.showAndWait().ifPresent(response -> {
                timer.stop();
                view.getStage().close();
            });
        }

        private void startNewRound() {
            resetTimer();
            updateEntireView();
        }

        private void styleDialog(Dialog<?> dialog) {
            DialogPane dialogPane = dialog.getDialogPane();

            dialogPane.setStyle(String.format("""
                -fx-background-color: %s;
                """, DIALOG_BG));

            // Style header
            dialogPane.lookup(".header-panel").setStyle(String.format("""
                -fx-background-color: %s;
                -fx-padding: 20px;
                """, DIALOG_HEADER_BG));

            Label headerText = (Label) dialogPane.lookup(".header-panel > .label");
            if (headerText != null) {
                headerText.setStyle("""
                    -fx-text-fill: white;
                    -fx-font-size: 18px;
                    -fx-font-weight: bold;
                    """);
            }

            // Style content
            Label contentText = (Label) dialogPane.lookup(".content.label");
            if (contentText != null) {
                contentText.setStyle(String.format("""
                    -fx-text-fill: %s;
                    -fx-font-size: 14px;
                    """, TEXT_SECONDARY));
            }

            styleDialogButtons(dialogPane);
        }

        private void styleDialogButtons(DialogPane dialogPane) {
            dialogPane.getButtonTypes().forEach(buttonType -> {
                Button button = (Button) dialogPane.lookupButton(buttonType);
                button.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-text-fill: %s;
                    -fx-font-size: 14px;
                    -fx-padding: 8 15;
                    -fx-background-radius: 5;
                    """, BUTTON_BG, TEXT_PRIMARY));

                button.setOnMouseEntered(e -> button.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-text-fill: %s;
                    -fx-font-size: 14px;
                    -fx-padding: 8 15;
                    -fx-background-radius: 5;
                    """, BUTTON_HOVER_BG, TEXT_PRIMARY)));

                button.setOnMouseExited(e -> button.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-text-fill: %s;
                    -fx-font-size: 14px;
                    -fx-padding: 8 15;
                    -fx-background-radius: 5;
                    """, BUTTON_BG, TEXT_PRIMARY)));
            });
        }

        private String getPlayerBoardStyle(boolean isActive) {
            return String.format("""
                -fx-background-color: #1F2937;
                -fx-padding: 20;
                %s
                """,
                    isActive ? "-fx-border-color: #4F46E5; -fx-border-width: 2; -fx-border-radius: 10;" : ""
            );
        }

        public void show() {
            view.getStage().show();
        }
    }