package hr.algebra.azul.networking;

import hr.algebra.azul.models.Factory;
import hr.algebra.azul.models.GameModel;
import hr.algebra.azul.models.Tile;
import hr.algebra.azul.models.TileColor;

public class GameSyncProtocol {
    // Message types for game synchronization
    public static final String MOVE_MADE = "MOVE";
    public static final String TURN_END = "TURN_END";
    public static final String GAME_STATE = "GAME_STATE";
    public static final String PLAYER_READY = "READY";
    public static final String GAME_START = "START";

    public static String createMoveMessage(int factoryIndex, TileColor color, int patternLineIndex) {
        return String.format("%s:%d:%s:%d",
                MOVE_MADE,
                factoryIndex,
                color.name(),
                patternLineIndex
        );
    }

    public static String createTurnEndMessage() {
        return TURN_END;
    }

    public static String createPlayerReadyMessage(boolean isReady) {
        return PLAYER_READY + ":" + isReady;
    }

    public static String createGameStartMessage() {
        return GAME_START;
    }

    public static String createGameStateMessage(GameModel gameModel) {
        // Serialize essential game state
        StringBuilder state = new StringBuilder(GAME_STATE + ":");
        state.append(gameModel.getCurrentPlayer().getName()).append(":");

        // Add factories state
        for (Factory factory : gameModel.getFactories()) {
            state.append(serializeFactory(factory)).append(";");
        }

        return state.toString();
    }

    private static String serializeFactory(Factory factory) {
        StringBuilder factoryState = new StringBuilder();
        for (Tile tile : factory.getTiles()) {
            if (tile.getColor() != null) {
                factoryState.append(tile.getColor().name()).append(",");
            }
        }
        return factoryState.length() > 0 ?
                factoryState.substring(0, factoryState.length() - 1) : "";
    }

    public static class GameMove {
        public final int factoryIndex;
        public final TileColor color;
        public final int patternLineIndex;

        public GameMove(String message) {
            String[] parts = message.split(":");
            this.factoryIndex = Integer.parseInt(parts[1]);
            this.color = TileColor.valueOf(parts[2]);
            this.patternLineIndex = Integer.parseInt(parts[3]);
        }
    }
}