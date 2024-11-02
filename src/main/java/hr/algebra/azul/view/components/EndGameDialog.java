package hr.algebra.azul.view.components;

import hr.algebra.azul.models.Player;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

public class EndGameDialog extends GameDialog {
    public EndGameDialog(List<Player> players) {
        super("Game Over", "Final Scores");

        Player winner = findWinner(players);
        setupContent(players, winner);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
    }

    private Player findWinner(List<Player> players) {
        return players.stream()
                .max((p1, p2) -> Integer.compare(p1.getScore(), p2.getScore()))
                .orElse(null);
    }

    private void setupContent(List<Player> players, Player winner) {
        // Winner announcement
        if (winner != null) {
            Label winnerLabel = new Label(winner.getName() + " Wins!");
            winnerLabel.setStyle("""
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-text-fill: #22C55E;
                """);
            content.getChildren().add(winnerLabel);
        }

        // Player scores
        players.forEach(player -> {
            VBox playerScore = createPlayerScoreBox(player, player == winner);
            content.getChildren().add(playerScore);
        });
    }

    private VBox createPlayerScoreBox(Player player, boolean isWinner) {
        VBox scoreBox = new VBox(5);
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-padding: 10;
            -fx-background-radius: 5;
            """, isWinner ? "#065F46" : "#1F2937"));

        Label nameLabel = new Label(player.getName());
        nameLabel.setStyle("""
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-text-fill: white;
            """);

        Label scoreLabel = new Label(player.getScore() + " points");
        scoreLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-text-fill: #9CA3AF;
            """);

        scoreBox.getChildren().addAll(nameLabel, scoreLabel);
        return scoreBox;
    }
}