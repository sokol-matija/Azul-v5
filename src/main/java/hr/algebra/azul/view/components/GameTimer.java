package hr.algebra.azul.view.components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class GameTimer {
    private final Timeline timeline;
    private final IntegerProperty timeRemaining = new SimpleIntegerProperty(150);
    private final Label timerLabel;

    public GameTimer(Label timerLabel) {
        this.timerLabel = timerLabel;
        this.timeline = createTimeline();
    }

    private Timeline createTimeline() {
        Timeline timer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateTimer())
        );
        timer.setCycleCount(Timeline.INDEFINITE);
        return timer;
    }

    private void updateTimer() {
        if (timeRemaining.get() > 0) {
            timeRemaining.set(timeRemaining.get() - 1);
            updateDisplay();
        } else {
            timeline.stop();
        }
    }

    private void updateDisplay() {
        int minutes = timeRemaining.get() / 60;
        int seconds = timeRemaining.get() % 60;
        timerLabel.setText(String.format("⏱ %02d:%02d", minutes, seconds));

        if (timeRemaining.get() <= 30) {
            timerLabel.setStyle("-fx-text-fill: #EF4444;"); // Warning color
        }
    }

    public void start() {
        timeline.play();
    }

    public void pause() {
        timeline.pause();
    }

    public void reset() {
        timeRemaining.set(150);
        timerLabel.setStyle("-fx-text-fill: #9CA3AF;");
        updateDisplay();
    }

    public IntegerProperty timeRemainingProperty() {
        return timeRemaining;
    }
}