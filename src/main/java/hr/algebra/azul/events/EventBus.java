package hr.algebra.azul.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus {
    private static EventBus instance;
    private final Map<GameEventType, List<GameEventHandler>> handlers = new HashMap<>();

    private EventBus() {}

    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public void subscribe(GameEventType type, GameEventHandler handler) {
        handlers.computeIfAbsent(type, k -> new ArrayList<>()).add(handler);
    }

    public void publish(GameEvent event) {
        if (handlers.containsKey(event.getType())) {
            handlers.get(event.getType()).forEach(handler -> handler.handle(event));
        }
    }
}