package hr.algebra.azul.view.components;

import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;

public class GameDialog extends Dialog<ButtonType> {
    protected VBox content;

    public GameDialog(String title, String headerText) {
        setTitle(title);
        setHeaderText(headerText);

        content = new VBox(15);
        content.setStyle("-fx-padding: 20;");

        DialogPane dialogPane = getDialogPane();
        dialogPane.setContent(content);
        styleDialog(dialogPane);
    }

    protected void styleDialog(DialogPane dialogPane) {
        dialogPane.setStyle("""
            -fx-background-color: #1F2937;
            -fx-padding: 20;
            """);

        dialogPane.lookup(".header-panel").setStyle("""
            -fx-background-color: #111827;
            -fx-padding: 20;
            """);

        dialogPane.lookup(".header-panel .label").setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            """);

        styleButtons(dialogPane);
    }

    protected void styleButtons(DialogPane dialogPane) {
        dialogPane.getButtonTypes().forEach(buttonType -> {
            var button = (javafx.scene.control.Button) dialogPane.lookupButton(buttonType);
            button.setStyle("""
                -fx-background-color: #3B82F6;
                -fx-text-fill: white;
                -fx-padding: 8 15;
                -fx-background-radius: 5;
                """);

            button.setOnMouseEntered(e -> button.setStyle(button.getStyle()
                    .replace("#3B82F6", "#2563EB")));
            button.setOnMouseExited(e -> button.setStyle(button.getStyle()
                    .replace("#2563EB", "#3B82F6")));
        });
    }
}