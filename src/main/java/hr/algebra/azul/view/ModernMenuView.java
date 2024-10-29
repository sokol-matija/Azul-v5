package hr.algebra.azul.view;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ModernMenuView {
    private Stage stage;
    private Scene scene;
    private VBox menuContainer;
    private Button singlePlayerButton;
    private Button multiPlayerButton;
    private Button optionsButton;
    private Button exitButton;
    private ImageView logoView;
    private Label versionLabel;

    // Style constants
    private static final String DARK_BG = "#111827";
    private static final String BUTTON_BG = "#3B82F6";
    private static final String BUTTON_HOVER = "#2563EB";
    private static final double BUTTON_WIDTH = 250;

    public ModernMenuView() {
        createView();
    }

    private void createView() {
        // Create main container
        BorderPane root = new BorderPane();
        root.setStyle(String.format("-fx-background-color: %s;", DARK_BG));

        // Create center menu container
        menuContainer = new VBox(20);
        menuContainer.setAlignment(Pos.CENTER);
        menuContainer.setPadding(new Insets(50));
        menuContainer.setMaxWidth(BUTTON_WIDTH * 1.2);

        // Create title/logo section
        VBox titleBox = createTitleSection();

        // Create menu buttons
        VBox buttonBox = createMenuButtons();

        // Create version label
        versionLabel = new Label("Version 1.0.0");
        versionLabel.setStyle("""
            -fx-text-fill: #6B7280;
            -fx-font-size: 12px;
            """);

        // Add all elements to menu container
        menuContainer.getChildren().addAll(titleBox, buttonBox, versionLabel);

        // Center the menu container
        root.setCenter(menuContainer);

        // Create scene
        scene = new Scene(root, 1000, 700);

        // Create stage
        stage = new Stage();
        stage.setTitle("Azul");
        stage.setScene(scene);

        // Add entrance animation
        addEntranceAnimation();
    }

    private VBox createTitleSection() {
        VBox titleBox = new VBox(15);
        titleBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("AZUL");
        titleLabel.setStyle("""
            -fx-font-size: 48px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            -fx-effect: dropshadow(gaussian, #3B82F6, 10, 0, 0, 0);
            """);

        Label subtitleLabel = new Label("The Beautiful Tile Game");
        subtitleLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: #9CA3AF;
            """);

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        return titleBox;
    }

    private VBox createMenuButtons() {
        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        singlePlayerButton = createMenuButton("Single Player", "🎮");
        multiPlayerButton = createMenuButton("Multiplayer", "👥");
        optionsButton = createMenuButton("Options", "⚙");
        exitButton = createMenuButton("Exit Game", "🚪");

        buttonBox.getChildren().addAll(
                singlePlayerButton,
                multiPlayerButton,
                optionsButton,
                exitButton
        );

        return buttonBox;
    }

    private Button createMenuButton(String text, String icon) {
        Button button = new Button(icon + "  " + text);
        button.setPrefWidth(BUTTON_WIDTH);
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-size: 16px;
            -fx-padding: 15 20;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """, BUTTON_BG));

        // Add hover effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(59, 130, 246, 0.5));
        shadow.setRadius(10);

        button.setOnMouseEntered(e -> {
            button.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-size: 16px;
                -fx-padding: 15 20;
                -fx-background-radius: 8;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.5), 10, 0, 0, 0);
                """, BUTTON_HOVER));
        });

        button.setOnMouseExited(e -> {
            button.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-size: 16px;
                -fx-padding: 15 20;
                -fx-background-radius: 8;
                -fx-cursor: hand;
                """, BUTTON_BG));
        });

        return button;
    }

    private void addEntranceAnimation() {
        menuContainer.setTranslateY(50);
        menuContainer.setOpacity(0);

        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5), menuContainer);
        translateTransition.setFromY(50);
        translateTransition.setToY(0);

        menuContainer.setOpacity(1);
        translateTransition.play();
    }

    // Getters
    public Stage getStage() { return stage; }
    public Button getSinglePlayerButton() { return singlePlayerButton; }
    public Button getMultiPlayerButton() { return multiPlayerButton; }
    public Button getOptionsButton() { return optionsButton; }
    public Button getExitButton() { return exitButton; }
}