package hr.algebra.azul.events;

public enum GameEventType {
    // Factory related events
    FACTORY_CLICKED,
    TILES_SELECTED,
    TILES_MOVED_TO_HAND,
    TILES_MOVED_TO_CENTER,

    // Pattern line events
    PATTERN_LINE_CLICKED,
    TILES_PLACED,

    // Game state events
    TURN_ENDED,
    ROUND_ENDED,
    GAME_ENDED,

    // Player events
    PLAYER_TURN_CHANGED,
    SCORE_UPDATED
}