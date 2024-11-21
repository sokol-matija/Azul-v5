package hr.algebra.azul.networking;

import hr.algebra.azul.models.TileColor;
import javafx.application.Platform;

import java.io.*;
import java.net.*;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final String RECONNECT_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private static final int RECONNECT_DELAY_MS = 2000;

    private final NetworkPlayer player;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> lobbyUpdateCallback;
    private Consumer<String> gameMessageCallback;
    private Consumer<String> chatMessageCallback;
    private boolean isConnected;
    private String currentLobby;
    private final ExecutorService executorService;
    private int reconnectAttempts;
    private final Object lock = new Object();

    public GameClient() throws IOException {
        this.player = new NetworkPlayer(UUID.randomUUID().toString().substring(0, 8));
        this.executorService = Executors.newSingleThreadExecutor();
        this.reconnectAttempts = 0;
        connect();
    }

    public void connect() throws IOException {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isConnected = true;
            System.out.println("Connected to server as: " + player.getDisplayName());
            sendInitialHandshake();
            startMessageListener();
        } catch (IOException e) {
            handleConnectionFailure();
        }
    }

    private void sendInitialHandshake() {
        sendMessage(player.getId() + ":" + player.getDisplayName());
    }

    private void startMessageListener() {
        executorService.submit(this::listenForMessages);
    }

    private void listenForMessages() {
        try {
            String message;
            while (isConnected && (message = in.readLine()) != null) {
                String finalMessage = message;
                Platform.runLater(() -> handleMessage(finalMessage));
            }
        } catch (IOException e) {
            handleDisconnect("Connection lost: " + e.getMessage());
        }
    }

    private void handleMessage(String message) {
        System.out.println("Received: " + message);

        if (message.startsWith("CHAT:")) {
            if (chatMessageCallback != null) {
                chatMessageCallback.accept(message);
            }
        }
        else if (isGameMessage(message)) {
            if (gameMessageCallback != null) {
                gameMessageCallback.accept(message);
            }
        }
        else if (lobbyUpdateCallback != null) {
            lobbyUpdateCallback.accept(message);
        }
    }

    private boolean isGameMessage(String message) {
        return message.startsWith(GameSyncProtocol.MOVE_MADE) ||
                message.startsWith(GameSyncProtocol.TURN_END) ||
                message.startsWith(GameSyncProtocol.GAME_STATE) ||
                message.startsWith(GameSyncProtocol.PLAYER_READY) ||
                message.startsWith(GameSyncProtocol.GAME_START);
    }

    private void handleConnectionFailure() throws IOException {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts++;
            System.out.println("Connection attempt " + reconnectAttempts + " failed. Retrying...");
            Thread.sleep(RECONNECT_DELAY_MS);
            connect();
        } else {
            throw new IOException("Failed to connect after " + MAX_RECONNECT_ATTEMPTS + " attempts");
        }
    }

    private void handleDisconnect(String reason) {
        synchronized (lock) {
            if (!isConnected) return;
            isConnected = false;
        }

        closeConnections();

        Platform.runLater(() -> {
            if (lobbyUpdateCallback != null) {
                lobbyUpdateCallback.accept("DISCONNECTED:" + reason);
            }
        });

        // Attempt to reconnect
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            attemptReconnect();
        }
    }

    private void attemptReconnect() {
        executorService.submit(() -> {
            try {
                Thread.sleep(RECONNECT_DELAY_MS);
                connect();
                if (currentLobby != null) {
                    joinLobby(currentLobby);
                }
            } catch (Exception e) {
                handleDisconnect("Reconnection failed: " + e.getMessage());
            }
        });
    }

    // Lobby Operations
    public void createLobby(String name) {
        if (isConnected) {
            sendMessage("CREATE_LOBBY:" + name + ":" + player.getDisplayName());
            currentLobby = name;
        }
    }

    public void joinLobby(String name) {
        if (isConnected) {
            sendMessage("JOIN_LOBBY:" + name + ":" + player.getDisplayName());
            currentLobby = name;
        }
    }

    public void leaveLobby(String name) {
        if (isConnected && currentLobby != null) {
            sendMessage("LEAVE_LOBBY:" + name + ":" + player.getDisplayName());
            currentLobby = null;
        }
    }

    // Game Operations
    public void sendGameMove(int factoryIndex, TileColor color, int patternLineIndex) {
        if (isConnected && currentLobby != null) {
            sendMessage(GameSyncProtocol.createMoveMessage(factoryIndex, color, patternLineIndex));
        }
    }

    public void sendTurnEnd() {
        if (isConnected && currentLobby != null) {
            sendMessage(GameSyncProtocol.createTurnEndMessage());
        }
    }

    public void sendPlayerReady(boolean isReady) {
        if (isConnected && currentLobby != null) {
            sendMessage(GameSyncProtocol.createPlayerReadyMessage(isReady));
        }
    }

    public void startGame() {
        if (isConnected && currentLobby != null) {
            sendMessage(GameSyncProtocol.createGameStartMessage());
        }
    }

    public void sendChatMessage(String message) {
        if (isConnected && currentLobby != null) {
            sendMessage("CHAT:" + currentLobby + ":" + player.getDisplayName() + ":" + message);
        }
    }

    public void requestGameState() {
        if (isConnected && currentLobby != null) {
            sendMessage("REQUEST_STATE");
        }
    }

    private void sendMessage(String message) {
        synchronized (lock) {
            if (isConnected && out != null) {
                out.println(message);
                System.out.println("Sent: " + message);
            }
        }
    }

    public void disconnect() {
        synchronized (lock) {
            if (!isConnected) return;
            isConnected = false;
        }

        if (currentLobby != null) {
            leaveLobby(currentLobby);
        }

        closeConnections();
        executorService.shutdown();
    }

    private void closeConnections() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connections: " + e.getMessage());
        }
    }

    // Getters and setters
    public NetworkPlayer getPlayer() { return player; }
    public boolean isConnected() { return isConnected; }
    public String getCurrentLobby() { return currentLobby; }
    public void setLobbyUpdateCallback(Consumer<String> callback) { this.lobbyUpdateCallback = callback; }
    public void setGameMessageCallback(Consumer<String> callback) { this.gameMessageCallback = callback; }
    public void setChatMessageCallback(Consumer<String> callback) { this.chatMessageCallback = callback; }

    @Override
    protected void finalize() throws Throwable {
        disconnect();
        super.finalize();
    }
}