package hr.algebra.azul.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Stack;

public class GameState {

    private Stack<GameAction> actionHistory = new Stack<>();

    public boolean canUndo() {
        return !actionHistory.isEmpty();
    }

    public void undo() {
        if (canUndo()) {
            GameAction lastAction = actionHistory.pop();
            // Revert the last action
            // Implementation depends on your game logic
        }
    }
    private StringProperty currentScreen = new SimpleStringProperty("MENU");

    public String getCurrentScreen() {
        return currentScreen.get();
    }

    public StringProperty currentScreenProperty() {
        return currentScreen;
    }

    public void setCurrentScreen(String screen) {
        this.currentScreen.set(screen);
    }
}