package hr.algebra.azul.models;

import javafx.beans.property.*;

public class GameLobby {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty host = new SimpleStringProperty();
    private final IntegerProperty currentPlayers = new SimpleIntegerProperty();
    private final IntegerProperty maxPlayers = new SimpleIntegerProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty rank = new SimpleStringProperty();

    public GameLobby(String name, String host, int currentPlayers, int maxPlayers, String status, String rank) {
        this.name.set(name);
        this.host.set(host);
        this.currentPlayers.set(currentPlayers);
        this.maxPlayers.set(maxPlayers);
        this.status.set(status);
        this.rank.set(rank);
    }

    // Getters and property accessors
    public String getName() { return name.get(); }
    public String getHost() { return host.get(); }
    public int getCurrentPlayers() { return currentPlayers.get(); }
    public int getMaxPlayers() { return maxPlayers.get(); }
    public String getStatus() { return status.get(); }
    public String getRank() { return rank.get(); }

    public StringProperty nameProperty() { return name; }
    public StringProperty hostProperty() { return host; }
    public IntegerProperty currentPlayersProperty() { return currentPlayers; }
    public IntegerProperty maxPlayersProperty() { return maxPlayers; }
    public StringProperty statusProperty() { return status; }
    public StringProperty rankProperty() { return rank; }
}