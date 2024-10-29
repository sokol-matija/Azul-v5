package hr.algebra.azul.models;

public enum TileColor {
    BLUE("#2563EB"),    // Dark blue
    WHITE("#F3F4F6"),   // Light gray-white
    RED("#DC2626"),     // Red
    BLACK("#1F2937"),   // Dark gray-black
    YELLOW("#F59E0B");  // Yellow/Orange

    private final String hexCode;

    TileColor(String hexCode) {
        this.hexCode = hexCode;
    }

    public String getHexCode() {
        return hexCode;
    }
}