package hr.algebra.azul.view.components;

import hr.algebra.azul.models.PatternLine;
import hr.algebra.azul.models.TileColor;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatternLines extends VBox {
    private final List<PatternLineRow> rows;
    private static final int PATTERN_LINES_COUNT = 5;

    public PatternLines() {
        setSpacing(5);
        setPadding(new Insets(5));
        rows = new ArrayList<>();

        initializeRows();
    }

    private void initializeRows() {
        for (int i = 0; i < PATTERN_LINES_COUNT; i++) {
            PatternLineRow row = new PatternLineRow(i + 1);
            rows.add(row);
            getChildren().add(row);
        }
    }

    public void update(List<PatternLine> patternLines) {
        if (patternLines == null) return;

        for (int i = 0; i < Math.min(rows.size(), patternLines.size()); i++) {
            rows.get(i).update(patternLines.get(i));
        }
    }

    public void highlightValidLines(TileColor color, boolean[] validLines) {
        if (validLines == null) return;

        for (int i = 0; i < Math.min(rows.size(), validLines.length); i++) {
            rows.get(i).setHighlight(validLines[i]);
        }
    }

    public void clearHighlights() {
        rows.forEach(row -> row.setHighlight(false));
    }

    public List<PatternLineRow> getRows() {
        return Collections.unmodifiableList(rows);
    }
}
