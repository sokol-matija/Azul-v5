package hr.algebra.azul.models;

import java.io.Serializable;
import java.util.*;
import java.util.stream.IntStream;

public class GameModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int FACTORY_COUNT = 5;
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;

    // Game components
    private final List<Factory> factories;
    private final List<Tile> centerPool;
    private final List<Player> players;
    private final TileBag tileBag;
    private final Stack<GameAction> actionHistory;

    // Game state
    private Player currentPlayer;
    private boolean firstPlayerTokenTaken;
    private GameState gameState;
    private int currentRound;
    private boolean isProcessingRound;

    public void addTilesToCenter(List<Tile> tiles) {
        centerPool.addAll(tiles);
    }

    public enum GameState {
        SETUP,
        FACTORY_SELECTION,
        PATTERN_LINE_SELECTION,
        WALL_TILING,
        SCORING,
        GAME_END
    }

    public GameModel(int numberOfPlayers) {
        validatePlayerCount(numberOfPlayers);
        this.factories = initializeFactories();
        this.centerPool = new ArrayList<>();
        this.players = createPlayers(numberOfPlayers);
        this.tileBag = new TileBag();
        this.actionHistory = new Stack<>();
        this.currentPlayer = players.get(0);
        this.firstPlayerTokenTaken = false;
        this.gameState = GameState.SETUP;
        this.currentRound = 1;
        this.isProcessingRound = false;
        initializeGame();
    }

    private void validatePlayerCount(int numberOfPlayers) {
        if (numberOfPlayers < MIN_PLAYERS || numberOfPlayers > MAX_PLAYERS) {
            throw new IllegalArgumentException(
                    "Player count must be between " + MIN_PLAYERS + " and " + MAX_PLAYERS);
        }
    }

    private List<Factory> initializeFactories() {
        List<Factory> factoryList = new ArrayList<>();
        for (int i = 0; i < FACTORY_COUNT; i++) {
            factoryList.add(new Factory(i));
        }
        return factoryList;
    }

    private List<Player> createPlayers(int numberOfPlayers) {
        List<Player> playerList = new ArrayList<>();
        for (int i = 0; i < numberOfPlayers; i++) {
            playerList.add(new Player("Player " + (i + 1)));
        }
        return playerList;
    }

    public void initializeGame() {
        // Fill factories
        for (Factory factory : factories) {
            factory.clear();
            factory.fillFromBag(tileBag);
        }

        // Reset center pool
        centerPool.clear();
        centerPool.add(new Tile(null)); // First player token

        gameState = GameState.FACTORY_SELECTION;
    }

    public boolean processRoundEnd() {
        if (isProcessingRound || !isRoundComplete()) {
            return false;
        }

        isProcessingRound = true;
        try {
            // Process wall tiling for all players
            for (Player player : players) {
                processPlayerWallTiling(player);
            }

            // Clear pattern lines and calculate penalties
            for (Player player : players) {
                applyFloorPenalties(player);
                clearPatternLines(player);
            }

            // Check if game should end
            if (shouldEndGame()) {
                calculateFinalScores();
                gameState = GameState.GAME_END;
                return true;
            }

            // Start new round
            startNewRound();
            return true;
        } finally {
            isProcessingRound = false;
        }
    }

    private void processPlayerWallTiling(Player player) {
        List<PatternLine> patterns = player.getPatternLines();
        for (int i = 0; i < patterns.size(); i++) {
            PatternLine line = patterns.get(i);
            if (line.isFull()) {
                TileColor color = line.getColor();
                if (color != null) {
                    int points = player.getWall().addTile(i, color);
                    player.addScore(points);
                }
            }
        }
    }

    private void applyFloorPenalties(Player player) {
        int penalty = player.getFloorLine().calculatePenalty();
        if (penalty != 0) {
            player.addScore(penalty);
            player.getFloorLine().clear();
        }
    }

    private void clearPatternLines(Player player) {
        for (PatternLine line : player.getPatternLines()) {
            line.clear();
        }
        player.clearHand();
    }

    private boolean shouldEndGame() {
        //TODO: Implement end game conditions
        //return hasCompletedRow() || currentRound >= 5;
        return false;
    }

    private void calculateFinalScores() {
        for (Player player : players) {
            int rowBonus = calculateRowBonus(player);
            int columnBonus = calculateColumnBonus(player);
            int colorBonus = calculateColorBonus(player);
            player.addScore(rowBonus + columnBonus + colorBonus);
        }
    }

    private int calculateRowBonus(Player player) {
        return (int) IntStream.range(0, Wall.WALL_SIZE)
                .filter(row -> player.getWall().isRowComplete(row))
                .count() * 2;
    }

    private int calculateColumnBonus(Player player) {
        return (int) IntStream.range(0, Wall.WALL_SIZE)
                .filter(col -> player.getWall().isColumnComplete(col))
                .count() * 7;
    }

    private int calculateColorBonus(Player player) {
        return (int) Arrays.stream(TileColor.values())
                .filter(color -> player.getWall().isColorComplete(color))
                .count() * 10;
    }

    private void startNewRound() {
        currentRound++;
        firstPlayerTokenTaken = false;
        gameState = GameState.FACTORY_SELECTION;
        initializeGame();
    }

    public boolean selectTilesFromFactory(int factoryIndex, TileColor color, int patternLineIndex) {
        if (!isValidSelection(factoryIndex, color, patternLineIndex)) {
            return false;
        }

        Factory factory = factories.get(factoryIndex);
        List<Tile> selectedTiles = factory.selectTilesByColor(color);
        List<Tile> remainingTiles = factory.removeRemainingTiles();

        // Move remaining tiles to center
        centerPool.addAll(remainingTiles);

        // Record action for undo
        recordAction(factoryIndex, color, patternLineIndex, selectedTiles, remainingTiles);

        return placeTiles(selectedTiles, patternLineIndex);
    }

    public boolean selectTilesFromCenter(TileColor color, int patternLineIndex) {
        if (!isValidCenterSelection(color, patternLineIndex)) {
            return false;
        }

        List<Tile> selectedTiles = collectTilesFromCenter(color);
        handleFirstPlayerToken();

        return placeTiles(selectedTiles, patternLineIndex);
    }

    private boolean isValidSelection(int factoryIndex, TileColor color, int patternLineIndex) {
        return gameState == GameState.FACTORY_SELECTION &&
                factoryIndex >= 0 &&
                factoryIndex < factories.size() &&
                currentPlayer.canPlaceTiles(color, patternLineIndex);
    }

    private boolean isValidCenterSelection(TileColor color, int patternLineIndex) {
        return gameState == GameState.FACTORY_SELECTION &&
                !centerPool.isEmpty() &&
                currentPlayer.canPlaceTiles(color, patternLineIndex);
    }

    private List<Tile> collectTilesFromCenter(TileColor color) {
        List<Tile> selectedTiles = new ArrayList<>();
        Iterator<Tile> iterator = centerPool.iterator();

        while (iterator.hasNext()) {
            Tile tile = iterator.next();
            if (tile.getColor() == color) {
                selectedTiles.add(tile);
                iterator.remove();
            }
        }

        return selectedTiles;
    }

    private void handleFirstPlayerToken() {
        if (!firstPlayerTokenTaken) {
            for (Tile tile : centerPool) {
                if (tile.getColor() == null) {
                    firstPlayerTokenTaken = true;
                    centerPool.remove(tile);
                    currentPlayer.getFloorLine().addTile(tile);
                    break;
                }
            }
        }
    }

    private boolean placeTiles(List<Tile> tiles, int patternLineIndex) {
        if (tiles.isEmpty()) {
            return false;
        }

        PatternLine targetLine = currentPlayer.getPatternLines().get(patternLineIndex);
        List<Tile> overflow = calculateOverflow(tiles, targetLine);

        // Add tiles to pattern line
        targetLine.addTiles(tiles.subList(0, tiles.size() - overflow.size()));

        // Handle overflow
        if (!overflow.isEmpty()) {
            currentPlayer.getFloorLine().addTiles(overflow);
        }

        return true;
    }

    private List<Tile> calculateOverflow(List<Tile> tiles, PatternLine targetLine) {
        List<Tile> overflow = new ArrayList<>();
        int availableSpace = targetLine.getSize() - targetLine.getTiles().size();

        if (tiles.size() > availableSpace) {
            overflow.addAll(tiles.subList(availableSpace, tiles.size()));
        }

        return overflow;
    }

    private void recordAction(int factoryIndex, TileColor color, int patternLineIndex,
                              List<Tile> selectedTiles, List<Tile> remainingTiles) {
        actionHistory.push(new GameAction(
                GameAction.ActionType.FACTORY_SELECTION,
                Map.of(
                        "factoryIndex", factoryIndex,
                        "color", color,
                        "patternLineIndex", patternLineIndex,
                        "selectedTiles", new ArrayList<>(selectedTiles),
                        "remainingTiles", new ArrayList<>(remainingTiles)
                )
        ));
    }

    public boolean isRoundComplete() {
        return factories.stream().allMatch(Factory::isEmpty) &&
                centerPool.stream().noneMatch(tile -> tile.getColor() != null) &&
                players.stream().allMatch(p -> p.getHand().isEmpty());
    }

    public void nextTurn() {
        int currentIndex = players.indexOf(currentPlayer);
        currentPlayer = players.get((currentIndex + 1) % players.size());
    }

    // Getters
    public List<Factory> getFactories() {
        return Collections.unmodifiableList(factories);
    }

    public List<Tile> getCenterPool() {
        return Collections.unmodifiableList(centerPool);
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setPlayerTurn(Player player) {
        if (!players.contains(player)) {
            throw new IllegalArgumentException("Player must be part of the game");
        }
        this.currentPlayer = player;
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public boolean isFirstPlayerTokenTaken() {
        return firstPlayerTokenTaken;
    }

    // For testing and debugging
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Game State: ").append(gameState)
                .append("\nCurrent Round: ").append(currentRound)
                .append("\nCurrent Player: ").append(currentPlayer.getName())
                .append("\nFirst Player Token Taken: ").append(firstPlayerTokenTaken)
                .append("\n\nPlayers:\n");

        players.forEach(player -> sb.append(player.toString()).append("\n"));

        return sb.toString();
    }
}