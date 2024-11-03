package hr.algebra.azul.models;

import java.io.Serializable;
import java.util.*;

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

        // Initialize game components
        this.factories = initializeFactories();
        this.centerPool = new ArrayList<>();
        this.players = createPlayers(numberOfPlayers);
        this.tileBag = new TileBag();
        this.actionHistory = new Stack<>();

        // Set initial state
        this.currentPlayer = players.get(0);
        this.firstPlayerTokenTaken = false;
        this.gameState = GameState.SETUP;
        this.currentRound = 1;

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

    private boolean isValidSelection(int factoryIndex, TileColor color, int patternLineIndex) {
        if (gameState != GameState.FACTORY_SELECTION) {
            return false;
        }

        if (factoryIndex < 0 || factoryIndex >= factories.size()) {
            return false;
        }

        if (!currentPlayer.canPlaceTiles(color, patternLineIndex)) {
            return false;
        }

        return true;
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

    public boolean selectTilesFromCenter(TileColor color, int patternLineIndex) {
        if (!isValidCenterSelection(color, patternLineIndex)) {
            return false;
        }

        List<Tile> selectedTiles = collectTilesFromCenter(color);
        handleFirstPlayerToken();

        return placeTiles(selectedTiles, patternLineIndex);
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

        if (!overflow.isEmpty()) {
            currentPlayer.getFloorLine().addTiles(overflow);
        }

        return true;
    }

    private List<Tile> calculateOverflow(List<Tile> tiles, PatternLine targetLine) {
        List<Tile> overflow = new ArrayList<>();

        if (!targetLine.addTiles(tiles)) {
            overflow.addAll(tiles);
        } else {
            int availableSpace = targetLine.getSize() - targetLine.getTiles().size();
            if (tiles.size() > availableSpace) {
                overflow.addAll(tiles.subList(availableSpace, tiles.size()));
            }
        }

        return overflow;
    }


    public void endTurn() {
        if (isRoundComplete()) {
            handleRoundEnd();
        } else {
            moveToNextPlayer();
        }
    }

    public boolean isRoundComplete() {
        boolean factoriesEmpty = getFactories().stream().allMatch(Factory::isEmpty);
        boolean centerEmpty = getCenterPool().isEmpty();
        boolean noTilesInHands = true;  // Add this check

        // Check if any player has tiles in hand
        for (Player player : getPlayers()) {
            if (!player.getHand().isEmpty()) {  // Need to add hand tracking to Player class
                noTilesInHands = false;
                break;
            }
        }

        return factoriesEmpty && centerEmpty && noTilesInHands;
    }

    private void moveToNextPlayer() {
        int currentIndex = players.indexOf(currentPlayer);
        currentPlayer = players.get((currentIndex + 1) % players.size());
    }

    private void handleRoundEnd() {
        processWallTiling();

        if (isGameComplete()) {
            finishGame();
        } else {
            startNewRound();
        }
    }

    private void processWallTiling() {
        for (Player player : players) {
            // Process pattern lines
            List<PatternLine> patterns = player.getPatternLines();
            for (PatternLine line : patterns) {
                if (line.isFull()) {
                    processCompletedLine(player, line);
                }
            }

            // Apply floor penalties
            applyFloorPenalties(player);
        }
    }

    public void setPlayerTurn(Player player) {
        if (!players.contains(player)) {
            throw new IllegalArgumentException("Player must be part of the game");
        }
        this.currentPlayer = player;
    }

    public void nextTurn() {
        int currentIndex = players.indexOf(currentPlayer);
        currentPlayer = players.get((currentIndex + 1) % players.size());
    }

    public void addTilesToCenter(List<Tile> tiles) {
        centerPool.addAll(tiles);
    }

    private void processCompletedLine(Player player, PatternLine line) {
        TileColor color = line.getColor();
        int lineIndex = player.getPatternLines().indexOf(line);
        int points = player.getWall().addTile(lineIndex, color);
        player.addScore(points);
        line.clear();
    }

    private void applyFloorPenalties(Player player) {
        int penalty = ScoringSystem.calculateFloorLinePenalty(player.getFloorLine());
        player.addScore(penalty);
        player.getFloorLine().clear();
    }

    private boolean isGameComplete() {
        return hasCompletedRow() || currentRound >= 5;
    }

    private boolean hasCompletedRow() {
        return players.stream().anyMatch(Player::hasCompletedRow);
    }

    private void finishGame() {
        calculateFinalScores();
        gameState = GameState.GAME_END;
    }

    private void calculateFinalScores() {
        for (Player player : players) {
            int bonus = ScoringSystem.calculateEndGameBonus(player);
            player.addScore(bonus);
        }
    }

    private void startNewRound() {
        currentRound++;
        resetRoundState();
        initializeGame();
    }

    private void resetRoundState() {
        firstPlayerTokenTaken = false;
        gameState = GameState.FACTORY_SELECTION;
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

    public GameState getGameState() {
        return gameState;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public boolean isFirstPlayerTokenTaken() {
        return firstPlayerTokenTaken;
    }
}