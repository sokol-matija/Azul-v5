package hr.algebra.azul.models;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private static final int PATTERN_LINE_COUNT = 5; // Added constant

    public final String name;
    public int score;
    public final Wall wall;
    public final List<PatternLine> patternLines;
    public final FloorLine floorLine;

    public Player(String name) {
        this.name = name;
        this.score = 0;
        this.wall = new Wall();
        this.patternLines = new ArrayList<>();
        for (int i = 0; i < PATTERN_LINE_COUNT; i++) {
            patternLines.add(new PatternLine(i + 1)); // Creates pattern lines of sizes 1 through 5
        }
        this.floorLine = new FloorLine();
    }

    public boolean canPlaceTiles(TileColor color, int patternLineIndex) {
        if (patternLineIndex < 0 || patternLineIndex >= PATTERN_LINE_COUNT) {
            return false;
        }

        PatternLine patternLine = patternLines.get(patternLineIndex);
        // Check if pattern line already has different color
        if (!patternLine.isEmpty() && patternLine.getColor() != color) {
            return false;
        }

        // Check if corresponding wall row already has this color
        return !wall.hasColor(patternLineIndex, color);
    }

    // Getters
    public String getName() { return name; }
    public int getScore() { return score; }
    public Wall getWall() { return wall; }
    public List<PatternLine> getPatternLines() { return new ArrayList<>(patternLines); }
    public FloorLine getFloorLine() { return floorLine; }
    public static int getPatternLineCount() { return PATTERN_LINE_COUNT; }

    // Setter for score
    public void setScore(int score) { this.score = score; }

    // Additional helper methods
    public boolean isPatternLineFull(int index) {
        if (index < 0 || index >= PATTERN_LINE_COUNT) {
            return false;
        }
        return patternLines.get(index).isFull();
    }

    public boolean hasCompletedRow() {
        boolean[][] tiles = wall.getTiles();
        for (int row = 0; row < PATTERN_LINE_COUNT; row++) {
            boolean rowComplete = true;
            for (int col = 0; col < PATTERN_LINE_COUNT; col++) {
                if (!tiles[row][col]) {
                    rowComplete = false;
                    break;
                }
            }
            if (rowComplete) return true;
        }
        return false;
    }

    public void clearPatternLine(int index) {
        if (index >= 0 && index < PATTERN_LINE_COUNT) {
            patternLines.set(index, new PatternLine(index + 1));
        }
    }

    public int calculateFinalScore() {
        int finalScore = score;

        // Bonus for completed rows (2 points each)
        for (int row = 0; row < PATTERN_LINE_COUNT; row++) {
            boolean rowComplete = true;
            for (int col = 0; col < PATTERN_LINE_COUNT; col++) {
                if (!wall.getTiles()[row][col]) {
                    rowComplete = false;
                    break;
                }
            }
            if (rowComplete) finalScore += 2;
        }

        // Bonus for completed columns (7 points each)
        for (int col = 0; col < PATTERN_LINE_COUNT; col++) {
            boolean colComplete = true;
            for (int row = 0; row < PATTERN_LINE_COUNT; row++) {
                if (!wall.getTiles()[row][col]) {
                    colComplete = false;
                    break;
                }
            }
            if (colComplete) finalScore += 7;
        }

        // Bonus for completed colors (10 points each)
        for (TileColor color : TileColor.values()) {
            boolean colorComplete = true;
            boolean[][] tiles = wall.getTiles();
            TileColor[][] pattern = wall.getWallPattern();
            for (int row = 0; row < PATTERN_LINE_COUNT; row++) {
                for (int col = 0; col < PATTERN_LINE_COUNT; col++) {
                    if (pattern[row][col] == color && !tiles[row][col]) {
                        colorComplete = false;
                        break;
                    }
                }
                if (!colorComplete) break;
            }
            if (colorComplete) finalScore += 10;
        }

        return finalScore;
    }

    @Override
    public String toString() {
        return String.format("Player: %s, Score: %d", name, score);
    }
}