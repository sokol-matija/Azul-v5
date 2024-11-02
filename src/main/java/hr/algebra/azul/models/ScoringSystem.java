package hr.algebra.azul.models;

public class ScoringSystem {
    private static final int HORIZONTAL_LINE_BONUS = 2;
    private static final int VERTICAL_LINE_BONUS = 7;
    private static final int COLOR_SET_BONUS = 10;

    public static int calculateFloorLinePenalty(FloorLine floorLine) {
        return floorLine.calculatePenalty();
    }

    public static int calculateEndGameBonus(Player player) {
        int bonus = 0;

        // Horizontal lines bonus
        bonus += calculateHorizontalBonus(player.getWall());

        // Vertical lines bonus
        bonus += calculateVerticalBonus(player.getWall());

        // Color sets bonus
        bonus += calculateColorBonus(player.getWall());

        return bonus;
    }

    private static int calculateHorizontalBonus(Wall wall) {
        int bonus = 0;
        for (int row = 0; row < Wall.WALL_SIZE; row++) {
            if (isRowComplete(wall, row)) {
                bonus += HORIZONTAL_LINE_BONUS;
            }
        }
        return bonus;
    }

    private static int calculateVerticalBonus(Wall wall) {
        int bonus = 0;
        for (int col = 0; col < Wall.WALL_SIZE; col++) {
            if (isColumnComplete(wall, col)) {
                bonus += VERTICAL_LINE_BONUS;
            }
        }
        return bonus;
    }

    private static int calculateColorBonus(Wall wall) {
        int bonus = 0;
        for (TileColor color : TileColor.values()) {
            if (isColorComplete(wall, color)) {
                bonus += COLOR_SET_BONUS;
            }
        }
        return bonus;
    }

    private static boolean isRowComplete(Wall wall, int row) {
        for (int col = 0; col < Wall.WALL_SIZE; col++) {
            if (!wall.isTilePlaced(row, col)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isColumnComplete(Wall wall, int col) {
        for (int row = 0; row < Wall.WALL_SIZE; row++) {
            if (!wall.isTilePlaced(row, col)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isColorComplete(Wall wall, TileColor color) {
        for (int row = 0; row < Wall.WALL_SIZE; row++) {
            for (int col = 0; col < Wall.WALL_SIZE; col++) {
                if (wall.getWallPattern()[row][col] == color && !wall.isTilePlaced(row, col)) {
                    return false;
                }
            }
        }
        return true;
    }
}