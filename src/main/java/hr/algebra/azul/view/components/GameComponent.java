package hr.algebra.azul.view.components;

import javafx.scene.layout.Region;

public abstract class GameComponent extends Region {
    protected static final String DARK_BG = "#111827";
    protected static final String DARKER_BG = "#0F172A";
    protected static final String CARD_BG = "#1F2937";
    protected static final String BORDER_COLOR = "#374151";
    protected static final String ACCENT_COLOR = "#4F46E5";
    protected static final String TEXT_PRIMARY = "white";
    protected static final String TEXT_SECONDARY = "#9CA3AF";

    public abstract void update();
}