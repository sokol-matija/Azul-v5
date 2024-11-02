package hr.algebra.azul.view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

import static hr.algebra.azul.view.components.GameComponent.DARKER_BG;

public class ControlBar extends HBox {
    private final Button undoButton;
    private final Button saveButton;
    private final Button exitButton;
    private final Button settingsButton;

    public ControlBar() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(15));
        setSpacing(10);
        setStyle("-fx-background-color: " + DARKER_BG);

        undoButton = createControlButton("↩", "Undo");
        saveButton = createControlButton("💾", "Save");
        exitButton = createControlButton("✖", "Exit");
        settingsButton = createControlButton("⚙", "Settings");

        getChildren().addAll(undoButton, saveButton, settingsButton, exitButton);
    }

    private Button createControlButton(String icon, String tooltip) {
        Button button = new Button(icon);
        button.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-padding: 8 15;
            -fx-background-radius: 5;
            """);

        button.setOnMouseEntered(e -> button.setStyle(button.getStyle()
                .replace("#3B82F6", "#2563EB")));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle()
                .replace("#2563EB", "#3B82F6")));

        Tooltip.install(button, new Tooltip(tooltip));
        return button;
    }

    public Button getUndoButton() { return undoButton; }
    public Button getSaveButton() { return saveButton; }
    public Button getExitButton() { return exitButton; }
    public Button getSettingsButton() { return settingsButton; }
}
