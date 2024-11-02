package hr.algebra.azul.view.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class TutorialOverlay extends StackPane {
    private final VBox content;
    private int currentStep = 0;
    private final List<TutorialStep> steps;

    public TutorialOverlay() {
        // Semi-transparent background
        Rectangle background = new Rectangle();
        background.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.7));
        background.widthProperty().bind(widthProperty());
        background.heightProperty().bind(heightProperty());

        content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(600);
        content.setStyle("""
            -fx-background-color: #1F2937;
            -fx-padding: 30;
            -fx-background-radius: 10;
            """);

        getChildren().addAll(background, content);

        steps = createTutorialSteps();
        showCurrentStep();
    }

    private List<TutorialStep> createTutorialSteps() {
        return List.of(
                new TutorialStep(
                        "Welcome to Azul!",
                        "Learn how to play this beautiful tile-laying game.",
                        "Next"
                ),
                new TutorialStep(
                        "Factory Selection",
                        "Click on tiles in the factories to select them. " +
                                "You must take all tiles of the same color.",
                        "Continue"
                ),
                new TutorialStep(
                        "Pattern Lines",
                        "Place selected tiles in your pattern lines. " +
                                "Each line can only contain tiles of the same color.",
                        "Continue"
                ),
                // Add more steps as needed
                new TutorialStep(
                        "Ready to Play!",
                        "You're now ready to start playing Azul. Good luck!",
                        "Start Game"
                )
        );
    }

    private void showCurrentStep() {
        if (currentStep >= steps.size()) {
            setVisible(false);
            return;
        }

        TutorialStep step = steps.get(currentStep);
        content.getChildren().clear();

        Label title = new Label(step.title());
        title.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            """);

        Label description = new Label(step.description());
        description.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: #9CA3AF;
            -fx-wrap-text: true;
            """);

        Button nextButton = new Button(step.buttonText());
        nextButton.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            """);
        nextButton.setOnAction(e -> nextStep());

        content.getChildren().addAll(title, description, nextButton);
    }

    private void nextStep() {
        currentStep++;
        showCurrentStep();
    }

    private record TutorialStep(
            String title,
            String description,
            String buttonText
    ) {}
}