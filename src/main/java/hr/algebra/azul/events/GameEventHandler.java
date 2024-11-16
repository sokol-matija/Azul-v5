package hr.algebra.azul.events;

@FunctionalInterface
public interface GameEventHandler {
    void handle(GameEvent event);
}