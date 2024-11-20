package hr.algebra.azul.networking;

import javafx.application.Platform;
import java.io.*;
import java.net.*;
import java.util.UUID;
import java.util.function.Consumer;

public class GameClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;

    private final NetworkPlayer player;
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private Consumer<String> lobbyUpdateCallback;
    private boolean isConnected = false;

    public GameClient() throws IOException {
        this.player = new NetworkPlayer(UUID.randomUUID().toString().substring(0, 8));
        this.socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Client initialized with player: " + player.getDisplayName());
    }

    public void connect() {
        // Send player info as first message
        out.println(player.getId() + ":" + player.getDisplayName());
        isConnected = true;
        System.out.println("Connected to server as: " + player.getDisplayName());

        new Thread(this::listenForMessages).start();
    }

    public void createLobby(String name) {
        if (isConnected) {
            String message = String.format("CREATE_LOBBY:%s:%s", name, player.getDisplayName());
            out.println(message);
            System.out.println("Creating lobby: " + name);
        }
    }

    public void joinLobby(String name) {
        if (isConnected) {
            String message = String.format("JOIN_LOBBY:%s:%s", name, player.getDisplayName());
            out.println(message);
            System.out.println("Joining lobby: " + name);
        }
    }

    public void leaveLobby(String name) {
        if (isConnected) {
            String message = String.format("LEAVE_LOBBY:%s:%s", name, player.getDisplayName());
            out.println(message);
            System.out.println("Leaving lobby: " + name);
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while (isConnected && (message = in.readLine()) != null) {
                String finalMessage = message;
                Platform.runLater(() -> handleMessage(finalMessage));
            }
        } catch (IOException e) {
            System.out.println("Connection lost: " + e.getMessage());
            handleDisconnect();
        }
    }

    private void handleMessage(String message) {
        System.out.println("Received message: " + message);
        if (lobbyUpdateCallback != null) {
            lobbyUpdateCallback.accept(message);
        }
    }

    private void handleDisconnect() {
        isConnected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (lobbyUpdateCallback != null) {
            Platform.runLater(() ->
                    lobbyUpdateCallback.accept("DISCONNECTED:Server connection lost")
            );
        }
    }

    public void disconnect() {
        isConnected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLobbyUpdateCallback(Consumer<String> callback) {
        this.lobbyUpdateCallback = callback;
    }

    public NetworkPlayer getPlayer() {
        return player;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void requestLobbies() {
        if (isConnected) {
            out.println("REQUEST_LOBBIES");
        }
    }

    public void sendChatMessage(String lobbyName, String message) {
        if (isConnected) {
            out.println("CHAT:" + lobbyName + ":" + message);
        }
    }

    public void startGame(String lobbyName) {
        if (isConnected) {
            out.println("START_GAME:" + lobbyName);
        }
    }
}