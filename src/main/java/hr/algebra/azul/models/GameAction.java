package hr.algebra.azul.models;

import java.util.Collections;
import java.util.Map;

public record GameAction(hr.algebra.azul.models.GameAction.ActionType type, Map<String, Object> parameters) {

    public enum ActionType {
        TILE_PLACEMENT,
        FACTORY_SELECTION,
        PATTERN_LINE_SELECTION
    }

    @Override
    public Map<String, Object> parameters() {
        return Collections.unmodifiableMap(parameters);
    }
}