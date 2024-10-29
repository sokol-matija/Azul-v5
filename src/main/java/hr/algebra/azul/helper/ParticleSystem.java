package hr.algebra.azul.helper;

import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleSystem extends Group {
    private final Random random = new Random();
    private double emitterX, emitterY;
    private final List<Node> particlesToRemove = new ArrayList<>();

    public ParticleSystem() {
        this.emitterX = 0;
        this.emitterY = 10;

        // Cleanup timer for removing finished particles
        AnimationTimer cleanupTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!particlesToRemove.isEmpty()) {
                    getChildren().removeAll(particlesToRemove);
                    particlesToRemove.clear();
                }
            }
        };
        cleanupTimer.start();
    }

    public void setEmitterLocation(double x, double y) {
        this.emitterX = x;
        this.emitterY = y;
    }

    public void emit() {
        for (int i = 0; i < 3; i++) {
            Circle particle = new Circle(random.nextDouble() * 1.5 + 0.5);
            particle.setFill(Color.WHITE);
            particle.setOpacity(0.8);
            particle.setMouseTransparent(true); // Prevent mouse interaction

            // Set initial position
            particle.setCenterX(emitterX);
            particle.setCenterY(emitterY);

            // Calculate random movement
            double angle = random.nextDouble() * 2 * Math.PI;
            double speed = random.nextDouble() * 2 + 1;
            double targetX = emitterX + Math.cos(angle) * speed * 10;
            double targetY = emitterY + Math.sin(angle) * speed * 10;

            // Create and configure animation
            ParallelTransition animation = new ParallelTransition();

            // Movement
            Timeline moveTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(particle.centerXProperty(), emitterX),
                            new KeyValue(particle.centerYProperty(), emitterY)
                    ),
                    new KeyFrame(Duration.millis(500),
                            new KeyValue(particle.centerXProperty(), targetX),
                            new KeyValue(particle.centerYProperty(), targetY)
                    )
            );

            // Fade out
            FadeTransition fade = new FadeTransition(Duration.millis(500), particle);
            fade.setFromValue(0.8);
            fade.setToValue(0);

            animation.getChildren().addAll(moveTimeline, fade);

            // Clean up when done
            animation.setOnFinished(event -> particlesToRemove.add(particle));

            getChildren().add(particle);
            animation.play();
        }
    }
}
