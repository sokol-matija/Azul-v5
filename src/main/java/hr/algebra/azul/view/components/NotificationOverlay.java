package hr.algebra.azul.view.components;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class NotificationOverlay extends VBox {
    private final Timeline hideTimeline;

    public NotificationOverlay() {
        setAlignment(Pos.TOP_CENTER);
        setSpacing(10);
        setMouseTransparent(true);

        hideTimeline = new Timeline(
                new KeyFrame(Duration.seconds(3),
                        new KeyValue(opacityProperty(), 0))
        );
        hideTimeline.setOnFinished(e -> getChildren().clear());
    }

    public void showNotification(String message, NotificationType type) {
        Label notification = createNotification(message, type);

        // Add new notification
        getChildren().add(0, notification);
        notification.setOpacity(0);

        // Animate in
        FadeTransition fadeIn = new FadeTransition(
                Duration.millis(200), notification);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Schedule removal
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(
                    Duration.millis(200), notification);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> getChildren().remove(notification));
            fadeOut.play();
        });
        pause.play();
    }

    private Label createNotification(String message, NotificationType type) {
        Label label = new Label(message);
        label.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-font-size: 14px;
            """, type.getBgColor(), type.getTextColor()));
        return label;
    }

    public enum NotificationType {
        INFO("#1F2937", "white"),
        SUCCESS("#065F46", "white"),
        WARNING("#92400E", "white"),
        ERROR("#991B1B", "white");

        private final String bgColor;
        private final String textColor;

        NotificationType(String bgColor, String textColor) {
            this.bgColor = bgColor;
            this.textColor = textColor;
        }

        public String getBgColor() { return bgColor; }
        public String getTextColor() { return textColor; }
    }
}
