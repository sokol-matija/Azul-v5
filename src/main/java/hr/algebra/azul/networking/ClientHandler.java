package hr.algebra.azul.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameServer server;
    private final BufferedReader in;
    private final PrintWriter out;
    private String playerId;
    private String playerName;
    private String currentLobby;

    public ClientHandler(Socket socket, GameServer server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            // Handle initial player connection
            String initMessage = in.readLine();
            String[] playerInfo = initMessage.split(":");
            playerId = playerInfo[0];
            playerName = playerInfo[1];

            System.out.println("Player connected: " + playerName + " (ID: " + playerId + ")");
            server.registerClient(playerId, playerName, this);

            // Main message loop
            String message;
            while ((message = in.readLine()) != null) {
                handleMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Player disconnected: " + playerName);
            handleDisconnect();
        }
    }

    private void handleMessage(String message) {
        System.out.println("Received from " + playerName + ": " + message);
        String[] parts = message.split(":");

        switch (parts[0]) {
            case "CREATE_LOBBY" -> {
                String lobbyName = parts[1];
                server.createLobby(lobbyName, playerId, playerName);
                currentLobby = lobbyName;
            }
            case "JOIN_LOBBY" -> {
                String lobbyName = parts[1];
                server.joinLobby(lobbyName, playerId, playerName);
                currentLobby = lobbyName;
            }
            case "LEAVE_LOBBY" -> {
                if (currentLobby != null) {
                    server.leaveLobby(currentLobby, playerId);
                    currentLobby = null;
                }
            }
        }
    }

    private void handleDisconnect() {
        if (currentLobby != null) {
            server.leaveLobby(currentLobby, playerId);
        }
        server.removeClient(playerId);

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
        System.out.println("Sent to " + playerName + ": " + message);
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getCurrentLobby() {
        return currentLobby;
    }
}