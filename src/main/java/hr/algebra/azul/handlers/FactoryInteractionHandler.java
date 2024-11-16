package hr.algebra.azul.handlers;

import hr.algebra.azul.events.EventBus;
import hr.algebra.azul.events.GameEvent;
import hr.algebra.azul.events.GameEventType;
import hr.algebra.azul.events.payloads.FactoryClickPayload;
import hr.algebra.azul.events.payloads.TileSelectionPayload;
import hr.algebra.azul.helper.TileAnimationManager;
import hr.algebra.azul.models.Factory;
import hr.algebra.azul.models.GameModel;
import hr.algebra.azul.models.Tile;
import hr.algebra.azul.models.TileColor;
import hr.algebra.azul.view.ModernTwoPlayerGameView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

public class FactoryInteractionHandler {
    private final EventBus eventBus;
    private final GameModel gameModel;
    private final ModernTwoPlayerGameView view;
    private final TileAnimationManager animationManager;

    public FactoryInteractionHandler(
            GameModel gameModel,
            ModernTwoPlayerGameView view,
            TileAnimationManager animationManager
    ) {
        this.eventBus = EventBus.getInstance();
        this.gameModel = gameModel;
        this.view = view;
        this.animationManager = animationManager;
        subscribeToEvents();
    }

    private void subscribeToEvents() {
        eventBus.subscribe(GameEventType.FACTORY_CLICKED, this::handleFactoryClick);
        eventBus.subscribe(GameEventType.TILES_SELECTED, this::handleTilesSelected);
    }

    private void handleFactoryClick(GameEvent event) {
        FactoryClickPayload payload = (FactoryClickPayload) event.getPayload();

        // Validate the click
        if (!isValidFactoryClick(payload)) {
            return;
        }

        // Get the factory and process tile selection
        Factory factory = gameModel.getFactories().get(payload.factoryIndex());
        TileColor selectedColor = getTileColorFromFill(payload.tileColor());

        // Select tiles from factory
        List<Tile> selectedTiles = factory.selectTilesByColor(selectedColor);
        List<Tile> remainingTiles = factory.removeRemainingTiles();

        // Publish tiles selected event
        eventBus.publish(new GameEvent(
                GameEventType.TILES_SELECTED,
                new TileSelectionPayload(payload.factoryIndex(), selectedTiles, remainingTiles)
        ));
    }

    private void handleTilesSelected(GameEvent event) {
        TileSelectionPayload payload = (TileSelectionPayload) event.getPayload();
        HBox playerHand = getCurrentPlayerHand();

        // Get the factory visual component
        VBox factory = (VBox) view.getFactoriesContainer()
                .getChildren().get(payload.factoryIndex());

        // Start animation
        animationManager.animateFactorySelection(
                factory,
                payload.selectedTiles(),
                payload.remainingTiles(),
                playerHand,
                view.getCenterPool(),
                () -> {
                    // After animation completes
                    updatePlayerHand(payload.selectedTiles());
                    gameModel.addTilesToCenter(payload.remainingTiles());
                    updateFactoryDisplay(factory);
                    updateCenterPool();
                }
        );
    }

    private boolean isValidFactoryClick(FactoryClickPayload payload) {
        // Check if it's valid player's turn
        if (isGamePaused()) return false;

        // Check if player's hand is empty
        if (!getCurrentPlayerHand().getChildren().isEmpty()) return false;

        // Check if factory is not empty
        Factory factory = gameModel.getFactories().get(payload.factoryIndex());
        return !factory.isEmpty();
    }

    private HBox getCurrentPlayerHand() {
        return gameModel.getCurrentPlayer() == gameModel.getPlayers().get(0)
                ? view.getPlayer1Hand()
                : view.getPlayer2Hand();
    }

    private void updatePlayerHand(List<Tile> tiles) {
        HBox hand = getCurrentPlayerHand();
        hand.getChildren().clear();

        for (Tile tile : tiles) {
            Circle tileCircle = createTileCircle(tile.getColor());
            hand.getChildren().add(tileCircle);
        }
    }

    private void updateFactoryDisplay(VBox factory) {
        // Clear the factory display
        ((GridPane) factory.getChildren().get(0)).getChildren().clear();
    }

    private void updateCenterPool() {
        // Update center pool display with current tiles
        FlowPane centerTiles = (FlowPane) view.getCenterPool().getChildren().get(1);
        centerTiles.getChildren().clear();

        for (Tile tile : gameModel.getCenterPool()) {
            if (tile.getColor() != null) {  // Skip first player token
                Circle tileCircle = createTileCircle(tile.getColor());
                centerTiles.getChildren().add(tileCircle);
            }
        }
    }

    private TileColor getTileColorFromFill(Color fillColor) {
        String hexColor = String.format("#%02X%02X%02X",
                (int) (fillColor.getRed() * 255),
                (int) (fillColor.getGreen() * 255),
                (int) (fillColor.getBlue() * 255));

        for (TileColor color : TileColor.values()) {
            if (color.getHexCode().equalsIgnoreCase(hexColor)) {
                return color;
            }
        }
        return null;
    }

    private Circle createTileCircle(TileColor color) {
        Circle circle = new Circle(15);
        circle.setFill(Color.web(color.getHexCode()));
        circle.setStroke(Color.web("#4B5563"));
        circle.setStrokeWidth(1);
        return circle;
    }

    private boolean isGamePaused() {
        // Implement game pause check
        return false;
    }
}