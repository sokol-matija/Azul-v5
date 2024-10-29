package com.azul.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CreateLobbyDialog {
    private Stage dialogStage;
    private TextField lobbyNameField;
    private ComboBox<Integer> maxPlayersBox;
    private ComboBox<String> rankRequirementBox;
    private Button createButton;
    private Button cancelButton;

    public CreateLobbyDialog(Stage parentStage) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.initStyle(StageStyle.UNDECORATED);

        createDialog();
    }

    private void createDialog() {
        VBox content = new VBox(15);
        content.setStyle("""
            -fx-background-color: #111827;
            -fx-padding: 20;
            -fx-border-color: #374151;
            -fx-border-radius: 5;
            """);
        content.setPadding(new Insets(20));
        content.setMinWidth(400);

        Label titleLabel = new Label("Create New Lobby");
        titleLabel.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            """);

        // Lobby Name Field
        Label nameLabel = new Label("Lobby Name");
        nameLabel.setStyle("-fx-text-fill: #9CA3AF;");

        lobbyNameField = new TextField();
        lobbyNameField.setPromptText("Enter lobby name...");
        lobbyNameField.setStyle("""
            -fx-background-color: #1F2937;
            -fx-text-fill: white;
            -fx-prompt-text-fill: #6B7280;
            -fx-padding: 10;
            -fx-background-radius: 5;
            """);

        // Max Players Dropdown
        Label playersLabel = new Label("Max Players");
        playersLabel.setStyle("-fx-text-fill: #9CA3AF;");

        maxPlayersBox = new ComboBox<>();
        maxPlayersBox.getItems().addAll(2, 3, 4);
        maxPlayersBox.setValue(4);
        maxPlayersBox.setStyle("""
            -fx-background-color: #1F2937;
            -fx-text-fill: white;
            -fx-prompt-text-fill: #6B7280;
            -fx-padding: 8;
            -fx-background-radius: 5;
            -fx-mark-color: white;
            """);
        maxPlayersBox.setMaxWidth(Double.MAX_VALUE);

        // Rank Requirement Dropdown
        Label rankLabel = new Label("Rank Requirement");
        rankLabel.setStyle("-fx-text-fill: #9CA3AF;");

        rankRequirementBox = new ComboBox<>();
        rankRequirementBox.getItems().addAll(
                "All Ranks",
                "Gold+",
                "Platinum+",
                "Diamond+"
        );
        rankRequirementBox.setValue("All Ranks");
        rankRequirementBox.setStyle("""
            -fx-background-color: #1F2937;
            -fx-text-fill: white;
            -fx-prompt-text-fill: #6B7280;
            -fx-padding: 8;
            -fx-background-radius: 5;
            -fx-mark-color: white;
            """);
        rankRequirementBox.setMaxWidth(Double.MAX_VALUE);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        cancelButton = new Button("Cancel");
        cancelButton.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #9CA3AF;
            -fx-border-color: #374151;
            -fx-border-radius: 5;
            -fx-padding: 10 20;
            """);

        createButton = new Button("Create Lobby");
        createButton.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            """);

        buttonBox.getChildren().addAll(cancelButton, createButton);

        content.getChildren().addAll(
                titleLabel,
                nameLabel,
                lobbyNameField,
                playersLabel,
                maxPlayersBox,
                rankLabel,
                rankRequirementBox,
                buttonBox
        );

        Scene dialogScene = new Scene(content);
        dialogScene.setFill(null);
        dialogStage.setScene(dialogScene);
    }

    public Stage getStage() { return dialogStage; }
    public TextField getLobbyNameField() { return lobbyNameField; }
    public ComboBox<Integer> getMaxPlayersBox() { return maxPlayersBox; }
    public ComboBox<String> getRankRequirementBox() { return rankRequirementBox; }
    public Button getCreateButton() { return createButton; }
    public Button getCancelButton() { return cancelButton; }
}