package hr.algebra.azul.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player implements Serializable {
    private static final int PATTERN_LINE_COUNT = 5;

    private List<Tile> hand;
    public final String name;
    public int score;
    public final Wall wall;
    public final List<PatternLine> patternLines;
    public final FloorLine floorLine;

    public Player(String name) {
        this.hand = new ArrayList<>();
        this.name = name;
        this.score = 0;
        this.wall = new Wall();
        this.patternLines = initializePatternLines();
        this.floorLine = new FloorLine();
    }

    private List<PatternLine> initializePatternLines() {
        List<PatternLine> lines = new ArrayList<>();
        for (int i = 0; i < PATTERN_LINE_COUNT; i++) {
            lines.add(new PatternLine(i + 1)); // Creates pattern lines of sizes 1 through 5
        }
        return lines;
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

    public boolean hasCompletedRow() {
        for (int row = 0; row < Wall.WALL_SIZE; row++) {
            boolean rowComplete = true;
            for (int col = 0; col < Wall.WALL_SIZE; col++) {
                if (!wall.isTilePlaced(row, col)) {
                    rowComplete = false;
                    break;
                }
            }
            if (rowComplete) return true;
        }
        return false;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public Wall getWall() {
        return wall;
    }

    public List<PatternLine> getPatternLines() {
        return Collections.unmodifiableList(patternLines);
    }

    public PatternLine getPatternLine(int index) {
        if (index >= 0 && index < patternLines.size()) {
            return patternLines.get(index);
        }
        throw new IndexOutOfBoundsException("Invalid pattern line index: " + index);
    }

    public FloorLine getFloorLine() {
        return floorLine;
    }

    // Score management
    public void addScore(int points) {
        this.score += points;
    }

    public void setScore(int score) {
        this.score = score;
    }

    // Pattern line management
    public void clearPatternLine(int index) {
        if (index >= 0 && index < PATTERN_LINE_COUNT) {
            patternLines.get(index).clear();
        }
    }

    public boolean isPatternLineFull(int index) {
        if (index < 0 || index >= PATTERN_LINE_COUNT) {
            return false;
        }
        return patternLines.get(index).isFull();
    }

    public int calculateFinalScore() {
        int finalScore = score;

        // Bonus for completed rows (2 points each)
        for (int row = 0; row < Wall.WALL_SIZE; row++) {
            boolean rowComplete = true;
            for (int col = 0; col < Wall.WALL_SIZE; col++) {
                if (!wall.isTilePlaced(row, col)) {
                    rowComplete = false;
                    break;
                }
            }
            if (rowComplete) finalScore += 2;
        }

        // Bonus for completed columns (7 points each)
        for (int col = 0; col < Wall.WALL_SIZE; col++) {
            boolean colComplete = true;
            for (int row = 0; row < Wall.WALL_SIZE; row++) {
                if (!wall.isTilePlaced(row, col)) {
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
            for (int row = 0; row < Wall.WALL_SIZE; row++) {
                for (int col = 0; col < Wall.WALL_SIZE; col++) {
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

    public List<Tile> getHand() {
        return hand;
    }

    public void clearHand() {
        hand.clear();
    }

    public void addTilesToHand(List<Tile> tiles) {
        hand.addAll(tiles);
    }

    @Override
    public String toString() {
        return String.format("Player: %s, Score: %d", name, score);
    }
}