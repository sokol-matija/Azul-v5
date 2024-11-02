package hr.algebra.azul.view.components;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ScorePopup extends VBox {
    private final Timeline fadeOutTimeline;

    public ScorePopup() {
        setAlignment(Pos.CENTER);
        setMouseTransparent(true);
        setVisible(false);

        fadeOutTimeline = new Timeline(
                new KeyFrame(Duration.seconds(2),
                        new KeyValue(opacityProperty(), 0))
        );
        fadeOutTimeline.setOnFinished(e -> setVisible(false));
    }

    public void showScore(int points, String description, boolean isPositive) {
        String prefix = isPositive ? "+" : "";
        Label pointsLabel = new Label(prefix + points);
        pointsLabel.setStyle(String.format("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: %s;
            """, isPositive ? "#22C55E" : "#EF4444"));

        Label descLabel = new Label(description);
        descLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: white;
            """);

        getChildren().setAll(pointsLabel, descLabel);

        // Reset animation state
        setVisible(true);
        setOpacity(1);

        // Start animation
        fadeOutTimeline.playFromStart();
    }
}