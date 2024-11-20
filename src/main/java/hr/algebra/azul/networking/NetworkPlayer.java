package hr.algebra.azul.networking;

public class NetworkPlayer {
    private final String id;
    private final String displayName;

    public NetworkPlayer(String id) {
        this.id = id;
        this.displayName = generateDisplayName();
    }

    private String generateDisplayName() {
        String[] adjectives = {"Swift", "Clever", "Brave", "Bright", "Noble"};
        String[] nouns = {"Knight", "Wizard", "Dragon", "Phoenix", "Warrior"};

        int adjIndex = (int) (Math.random() * adjectives.length);
        int nounIndex = (int) (Math.random() * nouns.length);

        return adjectives[adjIndex] + nounIndex + nouns[nounIndex];
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
}