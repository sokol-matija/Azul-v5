package hr.algebra.azul.models;

import java.util.*;
import java.util.stream.Collectors;
import hr.algebra.azul.models.*;

public class GameModel {
    private static final int FACTORY_TILE_COUNT = 4;
    private static final int FACTORY_COUNT = 5;
    private static final int PATTERN_LINE_COUNT = 5;

    private final List<List<Tile>> factories;
    private final List<Tile> centerPool;
    private final List<Player> players;
    private Player currentPlayer;
    private boolean firstPlayerTokenTaken;
    private final Stack<GameAction> actionHistory;
    private GameState gameState;
    private TileBag tileBag = new TileBag();

    public GameModel(int numberOfPlayers) {
        if (numberOfPlayers < 2 || numberOfPlayers > 4) {
            throw new IllegalArgumentException("Player count must be between 2 and 4");
        }

        this.factories = new ArrayList<>();
        for (int i = 0; i < FACTORY_COUNT; i++) {
            factories.add(new ArrayList<>());
        }

        this.centerPool = new ArrayList<>();
        this.players = new ArrayList<>();
        for (int i = 0; i < numberOfPlayers; i++) {
            players.add(new Player("Player " + (i + 1)));
        }

        this.currentPlayer = players.get(0);
        this.firstPlayerTokenTaken = false;
        this.actionHistory = new Stack<>();
        this.gameState = GameState.SETUP;

        initializeGame();
    }

    public void moveTilesToCenter(List<Tile> tiles) {
        centerPool.addAll(tiles);
    }
    public enum GameState {
        SETUP, FACTORY_SELECTION, PATTERN_LINE_SELECTION, WALL_TILING, SCORING, GAME_END
    }

    public void initializeGame() {
        for (List<Tile> factory : factories) {
            factory.clear(); // Clear any existing tiles
            // Always add exactly 4 tiles to each factory
            for (int i = 0; i < FACTORY_TILE_COUNT; i++) {
                if (tileBag.hasNext()) {
                    factory.add(tileBag.draw());
                }
            }
        }

        centerPool.clear();
        centerPool.add(new Tile(null)); // First player token

        gameState = GameState.FACTORY_SELECTION;
    }

    public boolean selectTilesFromFactory(int factoryIndex, TileColor color, int patternLineIndex) {
        if (gameState != GameState.FACTORY_SELECTION) {
            return false;
        }

        List<Tile> selectedFactory = factories.get(factoryIndex);
        List<Tile> selectedTiles = selectedFactory.stream()
                .filter(tile -> tile.getColor() == color)
                .collect(Collectors.toList());

        if (selectedTiles.isEmpty()) {
            return false;
        }

        if (!currentPlayer.canPlaceTiles(color, patternLineIndex)) {
            return false;
        }

        // Move non-selected tiles to center
        List<Tile> nonSelectedTiles = selectedFactory.stream()
                .filter(tile -> tile.getColor() != color)
                .collect(Collectors.toList());
        centerPool.addAll(nonSelectedTiles);

        // Record action for undo
        actionHistory.push(new GameAction(
                GameAction.ActionType.FACTORY_SELECTION,
                Map.of(
                        "factoryIndex", factoryIndex,
                        "color", color,
                        "patternLineIndex", patternLineIndex
                )
        ));

        // Clear the factory
        selectedFactory.clear();

        return placeTiles(selectedTiles, patternLineIndex);
    }

    private boolean placeTiles(List<Tile> tiles, int patternLineIndex) {
        if (tiles.isEmpty()) {
            return false;
        }

        PatternLine targetLine = currentPlayer.patternLines.get(patternLineIndex);
        List<Tile> overflow = new ArrayList<>();

        // Try to place tiles in pattern line
        if (!targetLine.addTiles(tiles)) {
            // If placement fails, all tiles go to floor line
            overflow.addAll(tiles);
        } else {
            // Calculate overflow
            int availableSpace = targetLine.size - targetLine.tiles.size();
            if (tiles.size() > availableSpace) {
                overflow.addAll(tiles.subList(availableSpace, tiles.size()));
            }
        }

        // Add overflow to floor line
        if (!overflow.isEmpty()) {
            currentPlayer.floorLine.addTiles(overflow);
        }

        return true;
    }

    public void endTurn() {
        // Check if round is complete
        boolean roundComplete = factories.stream().allMatch(List::isEmpty) && centerPool.isEmpty();

        if (roundComplete) {
            handleRoundEnd();
        } else {
            // Switch to next player
            int currentIndex = players.indexOf(currentPlayer);
            currentPlayer = players.get((currentIndex + 1) % players.size());
        }
    }

    private void handleRoundEnd() {
        // Wall tiling phase
        for (Player player : players) {
            for (int i = 0; i < PATTERN_LINE_COUNT; i++) {
                PatternLine line = player.patternLines.get(i);
                if (line.isFull()) {
                    TileColor color = line.getColor();
                    player.score += player.wall.addTile(i, color);
                }
            }

            // Apply floor line penalties
            player.score += player.floorLine.calculatePenalty();
            player.floorLine.clear();
        }

        // Check for game end
        boolean gameComplete = checkGameEnd();
        if (gameComplete) {
            calculateFinalScores();
            gameState = GameState.GAME_END;
        } else {
            // Prepare next round
            initializeGame();
        }
    }

    private boolean checkGameEnd() {
        // Game ends if any player has completed a horizontal line
        return players.stream().anyMatch(player -> {
            for (int row = 0; row < 5; row++) {
                boolean rowComplete = true;
                for (int col = 0; col < 5; col++) {
                    if (!player.wall.tiles[row][col]) {
                        rowComplete = false;
                        break;
                    }
                }
                if (rowComplete) return true;
            }
            return false;
        });
    }

    private void calculateFinalScores() {
        for (Player player : players) {
            // Bonus for completed horizontal lines (2 points per line)
            for (int row = 0; row < 5; row++) {
                boolean rowComplete = true;
                for (int col = 0; col < 5; col++) {
                    if (!player.wall.tiles[row][col]) {
                        rowComplete = false;
                        break;
                    }
                }
                if (rowComplete) player.score += 2;
            }

            // Bonus for completed vertical lines (7 points per line)
            for (int col = 0; col < 5; col++) {
                boolean colComplete = true;
                for (int row = 0; row < 5; row++) {
                    if (!player.wall.tiles[row][col]) {
                        colComplete = false;
                        break;
                    }
                }
                if (colComplete) player.score += 7;
            }

            // Bonus for completed colors (10 points per color)
            for (TileColor color : TileColor.values()) {
                boolean colorComplete = true;
                for (int row = 0; row < 5; row++) {
                    for (int col = 0; col < 5; col++) {
                        if (player.wall.wallPattern[row][col] == color && !player.wall.tiles[row][col]) {
                            colorComplete = false;
                            break;
                        }
                    }
                }
                if (colorComplete) player.score += 10;
            }
        }
    }

    // Getters for game state
    public List<List<Tile>> getFactories() {
        return Collections.unmodifiableList(factories);
    }

    public List<Tile> getCenterPool() {
        return Collections.unmodifiableList(centerPool);
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public GameModel.GameState getGameState() {
        return gameState;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }
}