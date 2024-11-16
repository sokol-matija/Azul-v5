    // Part 1: Imports and class setup through handleFactoryClickEvent()

    package hr.algebra.azul.controllers;

    import hr.algebra.azul.events.EventBus;
    import hr.algebra.azul.events.GameEvent;
    import hr.algebra.azul.events.GameEventType;
    import hr.algebra.azul.events.payloads.FactoryClickPayload;
    import hr.algebra.azul.events.payloads.TileSelectionPayload;
    import hr.algebra.azul.handlers.FactoryInteractionHandler;
    import hr.algebra.azul.helper.PatternLineInteractionHandler;
    import hr.algebra.azul.helper.TileAnimationManager;
    import hr.algebra.azul.helper.TurnManager;
    import hr.algebra.azul.helper.WallTilingManager;
    import hr.algebra.azul.models.*;
    import hr.algebra.azul.view.ModernTwoPlayerGameView;
    import javafx.animation.*;
    import javafx.application.Platform;
    import javafx.beans.property.SimpleIntegerProperty;
    import javafx.geometry.Insets;
    import javafx.geometry.Pos;
    import javafx.scene.Node;
    import javafx.scene.control.*;
    import javafx.scene.effect.DropShadow;
    import javafx.scene.effect.InnerShadow;
    import javafx.scene.layout.*;
    import javafx.scene.paint.Color;
    import javafx.scene.shape.Circle;
    import javafx.scene.shape.Rectangle;
    import javafx.stage.Stage;
    import javafx.util.Duration;
    import java.util.*;

    public class ModernTwoPlayerGameController {
        // Constants
        private static final Duration ANIMATION_DURATION = Duration.millis(500);
        private static final String DIALOG_BG = "#1F2937";
        private static final String DIALOG_HEADER_BG = "#111827";
        private static final String BUTTON_BG = "#3B82F6";
        private static final String BUTTON_HOVER_BG = "#2563EB";
        private static final String TEXT_PRIMARY = "white";
        private static final String TEXT_SECONDARY = "#9CA3AF";
        private static final String CARD_BG = "#1F2937";
        private static final String ACCENT_COLOR = "#4F46E5";

        // Core components
        private final ModernTwoPlayerGameView view;
        private final GameModel gameModel;
        private final Stage primaryStage;
        private final EventBus eventBus;

        // Helper managers
        private final PatternLineInteractionHandler patternLineInteractionHandler;
        private final WallTilingManager wallTilingManager;
        private final TileAnimationManager animationManager;
        private final TurnManager turnManager;
        private final FactoryInteractionHandler factoryHandler;

        // State tracking
        private boolean isGamePaused;
        private TileColor selectedColor;

        public ModernTwoPlayerGameController(ModernTwoPlayerGameView view, Stage primaryStage) {
            this.view = view;
            this.primaryStage = primaryStage;
            this.gameModel = new GameModel(2);
            this.eventBus = EventBus.getInstance();

            // Initialize managers
            this.wallTilingManager = new WallTilingManager(view, gameModel);
            this.turnManager = new TurnManager(gameModel, view, view.getTimerLabel());
            this.patternLineInteractionHandler = new PatternLineInteractionHandler(view, gameModel, turnManager);
            this.animationManager = new TileAnimationManager(view.getAnimationLayer());
            this.factoryHandler = new FactoryInteractionHandler(gameModel, view, animationManager);

            initializeController();
            subscribeToEvents();
            updateEntireView();
        }
        // Part 2: Core initialization and event subscription

        private void initializeController() {
            System.out.println("Initializing controller...");
            setupButtonHandlers();
            setupFactoryClickHandlers();
            setupPatternLineHandlers();
            setupCenterPoolClickHandlers();
            setupWindowHandlers();
            System.out.println("Game initialized with " + gameModel.getFactories().size() + " factories");
        }

        private void subscribeToEvents() {
            eventBus.subscribe(GameEventType.FACTORY_CLICKED, this::handleFactoryClickEvent);
            eventBus.subscribe(GameEventType.TILES_SELECTED, this::handleTilesSelectedEvent);
            eventBus.subscribe(GameEventType.TILES_MOVED_TO_HAND, this::handleTilesMovedToHandEvent);
            eventBus.subscribe(GameEventType.TILES_MOVED_TO_CENTER, this::handleTilesMovedToCenterEvent);
            eventBus.subscribe(GameEventType.PATTERN_LINE_CLICKED, this::handlePatternLineClickEvent);
            eventBus.subscribe(GameEventType.TILES_PLACED, this::handleTilesPlacedEvent);
            eventBus.subscribe(GameEventType.TURN_ENDED, this::handleTurnEndedEvent);
            eventBus.subscribe(GameEventType.ROUND_ENDED, this::handleRoundEndedEvent);
            eventBus.subscribe(GameEventType.PLAYER_TURN_CHANGED, this::handlePlayerTurnChangedEvent);
            eventBus.subscribe(GameEventType.SCORE_UPDATED, this::handleScoreUpdatedEvent);
        }

        private void setupFactoryClickHandlers() {
            GridPane factoriesGrid = view.getFactoriesContainer();
            for (int i = 0; i < factoriesGrid.getChildren().size(); i++) {
                final int factoryIndex = i;
                VBox factory = (VBox) factoriesGrid.getChildren().get(i);
                GridPane tileGrid = (GridPane) factory.getChildren().get(0);

                // Clear existing tiles
                tileGrid.getChildren().clear();

                // Get tiles from model
                Factory gameFactory = gameModel.getFactories().get(factoryIndex);
                List<Tile> tiles = gameFactory.getTiles();

                // Create new tiles with click handlers
                int tileIndex = 0;
                for (int row = 0; row < 2; row++) {
                    for (int col = 0; col < 2; col++) {
                        Circle tileCircle = new Circle(15);
                        if (tileIndex < tiles.size()) {
                            Tile tile = tiles.get(tileIndex);
                            TileColor tileColor = tile.getColor();
                            tileCircle.setFill(Color.web(tileColor.getHexCode()));

                            // Add click handler
                            final Color tileFillColor = (Color) tileCircle.getFill();
                            tileCircle.setOnMouseClicked(e -> {
                                if (!isGamePaused && getCurrentPlayerHand().getChildren().isEmpty()) {
                                    // Create and publish the factory click event
                                    FactoryClickPayload payload = new FactoryClickPayload(
                                            factoryIndex,
                                            tileFillColor,
                                            tileCircle,
                                            factory
                                    );
                                    GameEvent event = new GameEvent(GameEventType.FACTORY_CLICKED, payload);
                                    eventBus.publish(event);

                                    // Add debug logging
                                    System.out.println("Tile clicked at factory " + factoryIndex +
                                            " with color " + tileFillColor);
                                }
                                e.consume();
                            });

                            // Add hover effects
                            setupTileHoverEffects(tileCircle, factory);
                        } else {
                            tileCircle.setFill(Color.web("#374151"));
                        }

                        tileCircle.setStroke(Color.web("#4B5563"));
                        tileCircle.setStrokeWidth(1);
                        tileCircle.setEffect(new InnerShadow(5, Color.web("#000000", 0.2)));

                        // Make sure the tile can receive mouse events
                        tileCircle.setMouseTransparent(false);
                        tileGrid.add(tileCircle, col, row);
                        tileIndex++;
                    }
                }

                // Make sure containers aren't blocking clicks
                tileGrid.setMouseTransparent(false);
                factory.setMouseTransparent(false);
            }
        }

        // Part 3: Event handlers and tile interaction

        private void handleTileClick(int factoryIndex, TileColor tileColor, VBox factory) {
            if (isGamePaused || getCurrentPlayerHand().getChildren().size() > 0) {
                System.out.println("Click blocked - Game paused or hand not empty");
                return;
            }

            System.out.println("Processing tile click for factory " + factoryIndex + " color " + tileColor);

            Factory targetFactory = gameModel.getFactories().get(factoryIndex);
            if (targetFactory.getTiles().stream().noneMatch(t -> t.getColor() == tileColor)) {
                System.out.println("No matching tiles found in factory");
                return;
            }

            List<Tile> selectedTiles = targetFactory.selectTilesByColor(tileColor);
            List<Tile> remainingTiles = targetFactory.removeRemainingTiles();

            System.out.println("Selected " + selectedTiles.size() + " tiles");
            System.out.println("Remaining " + remainingTiles.size() + " tiles");

            // Debug the event publishing
            GameEvent event = new GameEvent(
                    GameEventType.TILES_SELECTED,
                    new TileSelectionPayload(factoryIndex, selectedTiles, remainingTiles)
            );
            System.out.println("Publishing event: " + event.getType());
            eventBus.publish(event);
        }

        private void handleTilesSelectedEvent(GameEvent event) {
            TileSelectionPayload payload = (TileSelectionPayload) event.getPayload();
            HBox playerHand = getCurrentPlayerHand();
            int factoryIndex = payload.factoryIndex();
            VBox factory = (VBox) view.getFactoriesContainer().getChildren().get(factoryIndex);
            Factory gameFactory = gameModel.getFactories().get(factoryIndex);

            animationManager.animateFactorySelection(
                    factory,
                    payload.selectedTiles(),
                    payload.remainingTiles(),
                    playerHand,
                    view.getCenterPool(),
                    () -> {
                        updatePlayerHand(payload.selectedTiles());
                        gameModel.addTilesToCenter(payload.remainingTiles());
                        updateFactoryDisplay(factory, gameFactory);  // Now passing both required arguments
                        updateCenterPool();
                        patternLineInteractionHandler.setupPatternLineInteractions();

                        if (gameModel.isRoundComplete()) {
                            eventBus.publish(new GameEvent(GameEventType.ROUND_ENDED, null));
                        }
                    }
            );
        }

        private void updateFactoryDisplay(VBox factoryBox) {
            // Get the factory index from the factory box's position in the grid
            GridPane factoriesGrid = view.getFactoriesContainer();
            int factoryIndex = factoriesGrid.getChildren().indexOf(factoryBox);

            if (factoryIndex >= 0 && factoryIndex < gameModel.getFactories().size()) {
                Factory factory = gameModel.getFactories().get(factoryIndex);
                updateFactoryDisplay(factoryBox, factory);
            } else {
                // Create empty factory display if no corresponding Factory model is found
                GridPane tileGrid = (GridPane) factoryBox.getChildren().get(0);
                tileGrid.getChildren().clear();

                for (int row = 0; row < 2; row++) {
                    for (int col = 0; col < 2; col++) {
                        Circle tileCircle = new Circle(15);
                        tileCircle.setFill(Color.web("#374151"));
                        tileCircle.setStroke(Color.web("#4B5563"));
                        tileCircle.setStrokeWidth(1);
                        tileCircle.setEffect(new InnerShadow(5, Color.web("#000000", 0.2)));
                        tileGrid.add(tileCircle, col, row);
                    }
                }
            }
        }

        private void handlePatternLineClickEvent(GameEvent event) {
            if (isGamePaused) return;

            int lineIndex = (int) event.getPayload();
            VBox playerBoard = gameModel.getCurrentPlayer() == gameModel.getPlayers().get(0) ?
                    view.getPlayer1Board() : view.getPlayer2Board();

            patternLineInteractionHandler.handlePatternLineClick(lineIndex, playerBoard);

            if (gameModel.isRoundComplete()) {
                eventBus.publish(new GameEvent(GameEventType.ROUND_ENDED, null));
            }
        }

        // Part 4: UI Update Methods

        private void updateEntireView() {
            Platform.runLater(() -> {
                updateFactories();
                updateCenterPool();
                updatePlayerBoards();
                updateScores();
                updateCurrentPlayer();
            });
        }

        private void updateFactories() {
            List<Factory> factories = gameModel.getFactories();
            GridPane factoriesGrid = view.getFactoriesContainer();

            for (int i = 0; i < factories.size(); i++) {
                Factory factory = factories.get(i);
                VBox factoryBox = (VBox) factoriesGrid.getChildren().get(i);
                updateFactoryDisplay(factoryBox, factory);
            }
        }

        private void updateFactoryDisplay(VBox factoryBox, Factory factory) {
            GridPane tileGrid = (GridPane) factoryBox.getChildren().get(0);
            tileGrid.getChildren().clear();

            List<Tile> tiles = factory.getTiles();
            int index = 0;
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    Circle tileCircle = new Circle(15);
                    if (index < tiles.size()) {
                        Tile tile = tiles.get(index);
                        tileCircle.setFill(Color.web(tile.getColor().getHexCode()));
                        setupTileHoverEffects(tileCircle, factoryBox);
                    } else {
                        tileCircle.setFill(Color.web("#374151"));
                    }

                    tileCircle.setStroke(Color.web("#4B5563"));
                    tileCircle.setStrokeWidth(1);
                    tileCircle.setEffect(new InnerShadow(5, Color.web("#000000", 0.2)));

                    tileGrid.add(tileCircle, col, row);
                    index++;
                }
            }
        }

        private void updatePlayerBoard(VBox playerBoard, Player player) {
            updatePatternLines(playerBoard, player);
            updateWall(playerBoard, player);
            updateFloorLine(playerBoard, player);
        }

        private void updatePlayerBoards() {
            List<Player> players = gameModel.getPlayers();
            updatePlayerBoard(view.getPlayer1Board(), players.get(0));
            updatePlayerBoard(view.getPlayer2Board(), players.get(1));
        }

        private void updatePatternLines(VBox playerBoard, Player player) {
            VBox patternLinesContainer = patternLineInteractionHandler.findPatternLinesContainer(playerBoard);
            if (patternLinesContainer == null) return;

            List<PatternLine> playerPatternLines = player.getPatternLines();
            for (int i = 1; i <= playerPatternLines.size(); i++) {
                HBox lineContainer = (HBox) patternLinesContainer.getChildren().get(i);
                PatternLine patternLine = playerPatternLines.get(i - 1);
                updateSinglePatternLine(lineContainer, patternLine);
            }
        }

        private void updateSinglePatternLine(HBox lineContainer, PatternLine patternLine) {
            List<Tile> tiles = patternLine.getTiles();
            for (int i = 0; i < lineContainer.getChildren().size(); i++) {
                Circle space = (Circle) lineContainer.getChildren().get(i);
                if (i < tiles.size()) {
                    space.setFill(Color.web(tiles.get(i).getColor().getHexCode()));
                } else {
                    space.setFill(Color.web("#374151"));
                }
            }
        }

        private void updateWall(VBox playerBoard, Player player) {
            GridPane wall = (GridPane) playerBoard.getChildren().stream()
                    .filter(node -> node instanceof GridPane)
                    .findFirst()
                    .orElse(null);

            if (wall != null) {
                boolean[][] tiles = player.getWall().getTiles();
                TileColor[][] pattern = player.getWall().getWallPattern();

                for (int row = 0; row < Wall.WALL_SIZE; row++) {
                    for (int col = 0; col < Wall.WALL_SIZE; col++) {
                        StackPane space = (StackPane) wall.getChildren().get(row * 5 + col);
                        Circle colorCircle = (Circle) space.getChildren().get(2);

                        if (tiles[row][col]) {
                            colorCircle.setFill(Color.web(pattern[row][col].getHexCode()));
                            colorCircle.setOpacity(1.0);
                            addTilePlacementAnimation(colorCircle);
                        }
                    }
                }
            }
        }

        private void updateCurrentPlayer() {
            Player current = gameModel.getCurrentPlayer();
            view.getCurrentPlayerLabel().setText(current.getName() + "'s Turn");

            boolean isFirstPlayer = current == gameModel.getPlayers().get(0);
            view.getPlayer1Board().setStyle(getBoardStyle(isFirstPlayer));
            view.getPlayer2Board().setStyle(getBoardStyle(!isFirstPlayer));

            animatePlayerTurnChange(isFirstPlayer);
        }

        // Part 5: Animation and Interaction Methods

        private void setupTileHoverEffects(Circle tileCircle, VBox factory) {
            Color tileColor = (Color) tileCircle.getFill();
            if (tileColor.equals(Color.web("#374151"))) return;

            DropShadow glow = new DropShadow();
            glow.setColor(tileColor);
            glow.setRadius(10);
            glow.setSpread(0.3);

            tileCircle.setOnMouseEntered(e -> {
                factory.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-background-radius: 10;
                    -fx-border-color: #60A5FA;
                    -fx-border-width: 2;
                    -fx-border-radius: 10;
                    """, CARD_BG));

                ScaleTransition scale = new ScaleTransition(Duration.millis(100), tileCircle);
                scale.setToX(1.1);
                scale.setToY(1.1);
                scale.play();

                tileCircle.setEffect(glow);
            });

            tileCircle.setOnMouseExited(e -> {
                factory.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-background-radius: 10;
                    """, CARD_BG));

                ScaleTransition scale = new ScaleTransition(Duration.millis(100), tileCircle);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();

                tileCircle.setEffect(new InnerShadow(5, Color.web("#000000", 0.2)));
            });
        }

        private void addTilePlacementAnimation(Circle tile) {
            ScaleTransition scale = new ScaleTransition(Duration.millis(300), tile);
            scale.setFromX(0.5);
            scale.setFromY(0.5);
            scale.setToX(1.0);
            scale.setToY(1.0);

            FadeTransition fade = new FadeTransition(Duration.millis(300), tile);
            fade.setFromValue(0.5);
            fade.setToValue(1.0);

            ParallelTransition animation = new ParallelTransition(scale, fade);
            animation.play();
        }

        private void animatePlayerTurnChange(boolean isFirstPlayer) {
            double sceneWidth = view.getStage().getWidth();
            double sceneHeight = view.getStage().getHeight();

            Rectangle indicator = new Rectangle(sceneWidth, 4);
            indicator.setFill(Color.web("#3B82F6"));
            indicator.setTranslateY(isFirstPlayer ? -2 : sceneHeight - 2);

            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#3B82F6"));
            glow.setRadius(10);
            indicator.setEffect(glow);

            view.getAnimationLayer().getChildren().add(indicator);

            TranslateTransition slide = new TranslateTransition(Duration.millis(600), indicator);
            slide.setFromX(-sceneWidth);
            slide.setToX(sceneWidth);
            slide.setOnFinished(e -> view.getAnimationLayer().getChildren().remove(indicator));
            slide.play();
        }

        private void animateScoreUpdate(VBox playerBoard, Player player) {
            HBox header = (HBox) playerBoard.getChildren().get(0);
            Label scoreLabel = (Label) header.getChildren().get(2);

            int currentScore = Integer.parseInt(scoreLabel.getText());
            int newScore = player.getScore();

            if (currentScore != newScore) {
                SimpleIntegerProperty scoreProperty = new SimpleIntegerProperty(currentScore);
                scoreProperty.addListener((obs, oldValue, newValue) ->
                        scoreLabel.setText(String.valueOf(newValue.intValue())));

                Timeline scoreTicker = new Timeline(
                        new KeyFrame(Duration.millis(500),
                                new KeyValue(scoreProperty, newScore, Interpolator.EASE_BOTH)
                        )
                );

                scoreLabel.setStyle("-fx-text-fill: " + (newScore > currentScore ? "#22C55E" : "#EF4444"));
                scoreTicker.setOnFinished(e -> scoreLabel.setStyle("-fx-text-fill: #60A5FA"));
                scoreTicker.play();
            }
        }

        // Part 6: Game State Management and Event Handlers

        private void handleFactoryClickEvent(GameEvent event) {
            System.out.println("Factory click event received");

            if (isGamePaused || getCurrentPlayerHand().getChildren().size() > 0) {
                System.out.println("Click blocked - Game paused or hand not empty");
                return;
            }

            FactoryClickPayload payload = (FactoryClickPayload) event.getPayload();
            Factory factory = gameModel.getFactories().get(payload.factoryIndex());
            TileColor selectedColor = getTileColorFromFill(payload.tileColor());

            System.out.println("Processing click for factory " + payload.factoryIndex() +
                    " color " + selectedColor);

            if (factory.getTiles().stream().noneMatch(t -> t.getColor() == selectedColor)) {
                System.out.println("No matching tiles found in factory");
                return;
            }

            List<Tile> selectedTiles = factory.selectTilesByColor(selectedColor);
            List<Tile> remainingTiles = factory.removeRemainingTiles();

            System.out.println("Selected " + selectedTiles.size() + " tiles");
            System.out.println("Remaining " + remainingTiles.size() + " tiles");

            eventBus.publish(new GameEvent(
                    GameEventType.TILES_SELECTED,
                    new TileSelectionPayload(payload.factoryIndex(), selectedTiles, remainingTiles)
            ));
        }

        private void handleTilesMovedToHandEvent(GameEvent event) {
            updatePlayerHand((List<Tile>) event.getPayload());
        }

        private void handleTilesMovedToCenterEvent(GameEvent event) {
            List<Tile> tiles = (List<Tile>) event.getPayload();
            gameModel.addTilesToCenter(tiles);
            updateCenterPool();
        }

        private void handleTilesPlacedEvent(GameEvent event) {
            updateScores();
            updatePlayerBoards();
        }

        private void handleTurnEndedEvent(GameEvent event) {
            turnManager.handleEndTurn();
        }

        private void handleRoundEndedEvent(GameEvent event) {
            if (gameModel.processRoundEnd()) {
                wallTilingManager.processWallTiling();
                clearPlayerHands();
                updateEntireView();
                turnManager.resetTimer();
                Platform.runLater(this::showRoundEndDialog);
            }
        }

        private void handlePlayerTurnChangedEvent(GameEvent event) {
            updateCurrentPlayer();
            turnManager.resetTimer();
        }

        private void handleScoreUpdatedEvent(GameEvent event) {
            updateScores();
        }

        private void setupButtonHandlers() {
            view.getUndoButton().setOnAction(e -> handleUndoClick());
            view.getSaveButton().setOnAction(e -> handleSaveClick());
            view.getExitButton().setOnAction(e -> handleExitClick());
            view.getSettingsButton().setOnAction(e -> handleSettingsClick());
            view.getEndTurnButton().setOnAction(e -> eventBus.publish(new GameEvent(GameEventType.TURN_ENDED, null)));
            view.getEndRoundButton().setOnAction(e -> eventBus.publish(new GameEvent(GameEventType.ROUND_ENDED, null)));
        }

        private void setupPatternLineHandlers() {
            setupPlayerPatternLines(view.getPlayer1Board());
            setupPlayerPatternLines(view.getPlayer2Board());
        }

        // Part 7: Pattern Line and Center Pool Handling

        private void setupPlayerPatternLines(VBox playerBoard) {
            VBox patternLinesContainer = patternLineInteractionHandler.findPatternLinesContainer(playerBoard);
            if (patternLinesContainer == null) return;

            for (int i = 1; i < patternLinesContainer.getChildren().size(); i++) {
                if (patternLinesContainer.getChildren().get(i) instanceof HBox patternLine) {
                    final int lineIndex = i - 1;
                    patternLine.setOnMouseClicked(e ->
                            eventBus.publish(new GameEvent(GameEventType.PATTERN_LINE_CLICKED, lineIndex))
                    );
                }
            }
        }

        private void setupCenterPoolClickHandlers() {
            VBox centerPool = view.getCenterPool();
            FlowPane tilesContainer = (FlowPane) centerPool.getChildren().get(1);

            tilesContainer.getChildren().addListener((javafx.collections.ListChangeListener<Node>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        for (Node node : change.getAddedSubList()) {
                            if (node instanceof Circle tile) {
                                setupCenterTileInteraction(tile);
                            }
                        }
                    }
                }
            });
        }

        private void setupCenterTileInteraction(Circle tile) {
            if (tile.getFill() == null) return;

            tile.setOnMouseClicked(e -> handleCenterTileClick(tile));
            setupCenterTileHoverEffects(tile);
        }

        private void setupCenterTileHoverEffects(Circle tile) {
            Color tileColor = (Color) tile.getFill();
            DropShadow glow = new DropShadow();
            glow.setColor(tileColor);
            glow.setRadius(10);
            glow.setSpread(0.3);

            tile.setOnMouseEntered(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), tile);
                scale.setToX(1.1);
                scale.setToY(1.1);
                scale.play();
                tile.setEffect(glow);
            });

            tile.setOnMouseExited(e -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), tile);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
                tile.setEffect(new InnerShadow(5, Color.web("#000000", 0.2)));
            });
        }

        private void updatePlayerHand(List<Tile> tiles) {
            HBox hand = getCurrentPlayerHand();
            hand.getChildren().clear();

            // Update model
            gameModel.getCurrentPlayer().getHand().clear();
            gameModel.getCurrentPlayer().getHand().addAll(tiles);

            // Update UI
            for (Tile tile : tiles) {
                Circle tileCircle = createTileCircle(tile.getColor());
                hand.getChildren().add(tileCircle);
            }
        }

        private void clearPlayerHands() {
            view.getPlayer1Hand().getChildren().clear();
            view.getPlayer2Hand().getChildren().clear();
        }

        // Part 8: Dialogs, Settings, and Utility Methods

        private void showRoundEndDialog() {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Round Complete");
            dialog.setHeaderText("Round Summary");

            VBox content = createRoundSummaryContent();
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            styleDialog(dialog);
            dialog.showAndWait();
        }

        private VBox createRoundSummaryContent() {
            VBox content = new VBox(15);
            content.setAlignment(Pos.CENTER);

            for (Player player : gameModel.getPlayers()) {
                VBox playerSummary = new VBox(5);
                Label nameLabel = new Label(player.getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
                Label scoreLabel = new Label("Score: " + player.getScore());
                scoreLabel.setStyle("-fx-text-fill: #9CA3AF;");
                playerSummary.getChildren().addAll(nameLabel, scoreLabel);
                content.getChildren().add(playerSummary);
            }

            return content;
        }

        private void showGameEndDialog() {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Game Over");
            dialog.setHeaderText("Final Scores");

            VBox content = createGameEndContent();
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

            styleDialog(dialog);

            dialog.showAndWait().ifPresent(response -> {
                turnManager.pauseTimer();
                view.getStage().close();
            });
        }

        private void handleExitClick() {
            Alert alert = createConfirmationDialog(
                    "Exit Game",
                    "Are you sure you want to exit?",
                    "Any unsaved progress will be lost."
            );

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    turnManager.pauseTimer();
                    view.getStage().close();
                }
            });
        }

        private void handleSettingsClick() {
            isGamePaused = true;
            turnManager.pauseTimer();
            showSettingsDialog();
        }

        private void styleDialog(Dialog<?> dialog) {
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setStyle("-fx-background-color: " + DIALOG_BG);

            // Style header
            dialogPane.lookup(".header-panel").setStyle("-fx-background-color: " + DIALOG_HEADER_BG);
            Label headerText = (Label) dialogPane.lookup(".header-panel > .label");
            if (headerText != null) {
                headerText.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
            }

            // Style content
            Label contentText = (Label) dialogPane.lookup(".content.label");
            if (contentText != null) {
                contentText.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 14px;");
            }

            // Style buttons
            dialog.getDialogPane().getButtonTypes().forEach(buttonType -> {
                Button button = (Button) dialogPane.lookupButton(buttonType);
                button.setStyle(getButtonStyle(BUTTON_BG));
                button.setOnMouseEntered(e -> button.setStyle(getButtonStyle(BUTTON_HOVER_BG)));
                button.setOnMouseExited(e -> button.setStyle(getButtonStyle(BUTTON_BG)));
            });
        }

        // Utility methods
        private String getButtonStyle(String backgroundColor) {
            return String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 8 15;
                -fx-background-radius: 5;
                """, backgroundColor);
        }

        private HBox getCurrentPlayerHand() {
            return gameModel.getCurrentPlayer() == gameModel.getPlayers().get(0) ?
                    view.getPlayer1Hand() : view.getPlayer2Hand();
        }

        private String getBoardStyle(boolean isActive) {
            return String.format("""
                -fx-background-color: %s;
                -fx-padding: 20;
                %s
                """,
                    CARD_BG,
                    isActive ? "-fx-border-color: " + ACCENT_COLOR + "; -fx-border-width: 2; -fx-border-radius: 10;" : ""
            );
        }

        public void show() {
            view.getStage().show();
        }

        private void setupWindowHandlers() {
            view.getStage().setOnCloseRequest(e -> {
                e.consume();
                handleExitClick();
            });
        }

        private void updateCenterPool() {
            VBox centerPool = view.getCenterPool();
            FlowPane tilesContainer = (FlowPane) centerPool.getChildren().get(1);
            tilesContainer.getChildren().clear();

            for (Tile tile : gameModel.getCenterPool()) {
                if (tile.getColor() != null) {
                    Circle tileCircle = createTileCircle(tile.getColor());
                    setupCenterTileInteraction(tileCircle);
                    tilesContainer.getChildren().add(tileCircle);
                }
            }
        }

        private void updateScores() {
            Platform.runLater(() -> {
                List<Player> players = gameModel.getPlayers();
                animateScoreUpdate(view.getPlayer1Board(), players.get(0));
                animateScoreUpdate(view.getPlayer2Board(), players.get(1));
            });
        }

        private void updateFloorLine(VBox playerBoard, Player player) {
            HBox floorLine = (HBox) playerBoard.getChildren().stream()
                    .filter(node -> node instanceof HBox)
                    .reduce((first, second) -> second)
                    .orElse(null);

            if (floorLine != null) {
                floorLine.getChildren().clear();
                for (int i = 0; i < FloorLine.MAX_TILES; i++) {
                    Circle space = createFloorLineSpace(i, player.getFloorLine().getTileAt(i));
                    floorLine.getChildren().add(space);
                }
            }
        }

        private TileColor getTileColorFromFill(Color fillColor) {
            String hexColor = String.format("#%02X%02X%02X",
                    (int)(fillColor.getRed() * 255),
                    (int)(fillColor.getGreen() * 255),
                    (int)(fillColor.getBlue() * 255));

            return Arrays.stream(TileColor.values())
                    .filter(color -> color.getHexCode().equalsIgnoreCase(hexColor))
                    .findFirst()
                    .orElse(null);
        }

        private void handleUndoClick() {
            showMessage("Cannot Undo", "Undo functionality is not yet implemented.", Alert.AlertType.INFORMATION);
        }

        private void handleSaveClick() {
            showMessage("Game Saved", "Your game has been saved successfully.", Alert.AlertType.INFORMATION);
        }

        private void handleCenterTileClick(Circle tile) {
            if (isGamePaused || getCurrentPlayerHand().getChildren().size() > 0) return;

            Color tileColor = (Color) tile.getFill();
            TileColor selectedColor = getTileColorFromFill(tileColor);
            if (selectedColor == null) return;

            List<Tile> selectedTiles = collectTilesFromCenter(selectedColor);
            boolean hasFirstPlayerToken = handleFirstPlayerToken();

            eventBus.publish(new GameEvent(
                    GameEventType.TILES_SELECTED,
                    new TileSelectionPayload(-1, selectedTiles, List.of())
            ));
        }

        private Circle createTileCircle(TileColor color) {
            Circle circle = new Circle(15);
            circle.setFill(Color.web(color.getHexCode()));
            circle.setStroke(Color.web("#4B5563"));
            circle.setStrokeWidth(1);

            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.web("#000000", 0.3));
            shadow.setRadius(5);
            circle.setEffect(shadow);

            return circle;
        }

        private VBox createGameEndContent() {
            VBox content = new VBox(20);
            content.setAlignment(Pos.CENTER);

            Optional<Player> winner = gameModel.getPlayers().stream()
                    .max(Comparator.comparingInt(Player::getScore));

            winner.ifPresent(player -> {
                Label winnerLabel = new Label(player.getName() + " Wins!");
                winnerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #22C55E;");
                content.getChildren().add(winnerLabel);
            });

            for (Player player : gameModel.getPlayers()) {
                content.getChildren().add(createPlayerScoreSummary(player));
            }

            return content;
        }

        private void showSettingsDialog() {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Game Settings");
            dialog.setHeaderText("Game Settings");

            VBox content = createSettingsContent();
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            styleDialog(dialog);

            dialog.showAndWait().ifPresent(response -> {
                isGamePaused = false;
                turnManager.resumeTimer();
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

        private Circle createFloorLineSpace(int index, Tile tile) {
            Circle space = new Circle(15);
            space.setStroke(Color.web("#4B5563"));
            space.setStrokeWidth(1);

            if (tile != null && tile.getColor() != null) {
                space.setFill(Color.web(tile.getColor().getHexCode()));
                setupPenaltyTooltip(space, index);
            } else {
                space.setFill(Color.web("#374151"));
            }

            return space;
        }

        private List<Tile> collectTilesFromCenter(TileColor color) {
            List<Tile> selectedTiles = new ArrayList<>();
            Iterator<Tile> iterator = gameModel.getCenterPool().iterator();

            while (iterator.hasNext()) {
                Tile tile = iterator.next();
                if (tile.getColor() == color) {
                    selectedTiles.add(tile);
                    iterator.remove();
                }
            }

            return selectedTiles;
        }

        private boolean handleFirstPlayerToken() {
            if (!gameModel.isFirstPlayerTokenTaken()) {
                Iterator<Tile> iterator = gameModel.getCenterPool().iterator();
                while (iterator.hasNext()) {
                    Tile tile = iterator.next();
                    if (tile.getColor() == null) {  // First player token has null color
                        iterator.remove();
                        gameModel.getCurrentPlayer().getFloorLine().addTile(tile);
                        return true;
                    }
                }
            }
            return false;
        }

        private HBox createPlayerScoreSummary(Player player) {
            HBox playerScore = new HBox(10);
            playerScore.setAlignment(Pos.CENTER);

            Label nameLabel = new Label(player.getName());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

            Label scoreLabel = new Label("Score: " + player.getScore());
            scoreLabel.setStyle("-fx-text-fill: #9CA3AF;");

            playerScore.getChildren().addAll(nameLabel, scoreLabel);
            return playerScore;
        }

        private Alert createConfirmationDialog(String title, String header, String content) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            styleDialog(alert);
            return alert;
        }

        private VBox createSettingsContent() {
            VBox content = new VBox(15);
            content.setPadding(new Insets(20));
            content.setStyle("-fx-spacing: 15;");

            // Game Settings
            CheckBox muteAudioBox = new CheckBox("Mute Audio");
            muteAudioBox.setStyle("-fx-text-fill: white;");

            // Timer Settings
            VBox timerSettings = createTimerSettings();

            // Visual Settings
            TitledPane visualSettings = createVisualSettingsPane();

            content.getChildren().addAll(muteAudioBox, timerSettings, visualSettings);
            return content;
        }

        private void setupPenaltyTooltip(Circle space, int index) {
            Tooltip penalty = new Tooltip("Penalty: -" + FloorLine.PENALTY_POINTS[index]);
            penalty.setStyle("""
            -fx-background-color: #1F2937;
            -fx-text-fill: #EF4444;
            -fx-font-size: 12px;
            """);
            Tooltip.install(space, penalty);
        }

        private VBox createTimerSettings() {
            VBox timerContainer = new VBox(5);
            Label timerLabel = new Label("Turn Timer (seconds):");
            timerLabel.setStyle("-fx-text-fill: white;");

            Slider timerSlider = new Slider(60, 180, 150);
            timerSlider.setShowTickLabels(true);
            timerSlider.setShowTickMarks(true);
            timerSlider.setMajorTickUnit(30);
            timerSlider.setBlockIncrement(10);
            timerSlider.setStyle("-fx-control-inner-background: #374151;");

            timerContainer.getChildren().addAll(timerLabel, timerSlider);
            return timerContainer;
        }

        private TitledPane createVisualSettingsPane() {
            TitledPane visualSettings = new TitledPane();
            visualSettings.setText("Visual Settings");
            visualSettings.setStyle("-fx-text-fill: white;");

            VBox content = new VBox(10);
            content.setPadding(new Insets(10));

            HBox animationSpeedBox = new HBox(10);
            Label speedLabel = new Label("Animation Speed:");
            speedLabel.setStyle("-fx-text-fill: white;");
            ComboBox<String> speedCombo = new ComboBox<>();
            speedCombo.getItems().addAll("Slow", "Normal", "Fast");
            speedCombo.setValue("Normal");

            CheckBox highlightMovesBox = new CheckBox("Highlight Valid Moves");
            highlightMovesBox.setStyle("-fx-text-fill: white;");

            content.getChildren().addAll(animationSpeedBox, highlightMovesBox);
            visualSettings.setContent(content);

            return visualSettings;
        }
    }