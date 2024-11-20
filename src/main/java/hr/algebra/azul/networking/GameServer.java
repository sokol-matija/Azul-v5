package hr.algebra.azul.networking;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    private static final int PORT = 5000;
    private final ServerSocket serverSocket;
    private final ConcurrentHashMap<String, GameLobby> lobbies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public GameServer() throws IOException {
        this.serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerClient(String clientId, String playerName, ClientHandler handler) {
        clients.put(clientId, handler);
        System.out.println("Client registered: " + playerName + " (ID: " + clientId + ")");
        broadcastLobbies();
    }

    public void createLobby(String name, String hostId, String hostName) {
        GameLobby lobby = new GameLobby(name, hostId, hostName);
        lobbies.put(name, lobby);
        System.out.println("Lobby created: " + name + " by " + hostName);
        broadcastLobbies();
    }

    public void joinLobby(String lobbyName, String clientId, String playerName) {
        GameLobby lobby = lobbies.get(lobbyName);
        if (lobby != null && lobby.addPlayer(clientId, playerName)) {
            System.out.println("Player " + playerName + " joined lobby: " + lobbyName);
            broadcastLobbyUpdate(lobby);
        }
    }

    public void leaveLobby(String lobbyName, String clientId) {
        GameLobby lobby = lobbies.get(lobbyName);
        if (lobby != null) {
            lobby.removePlayer(clientId);
            System.out.println("Player left lobby: " + lobbyName);
            if (lobby.isEmpty()) {
                lobbies.remove(lobbyName);
                broadcastLobbies();
            } else {
                broadcastLobbyUpdate(lobby);
            }
        }
    }

    public void removeClient(String clientId) {
        clients.remove(clientId);
        // Remove from any lobby they're in
        for (GameLobby lobby : lobbies.values()) {
            if (lobby.hasPlayer(clientId)) {
                leaveLobby(lobby.getName(), clientId);
            }
        }
    }

    private void broadcastLobbies() {
        String lobbiesData = "LOBBIES:" + String.join(",", lobbies.keySet());
        for (ClientHandler client : clients.values()) {
            client.sendMessage(lobbiesData);
        }
    }

    private void broadcastLobbyUpdate(GameLobby lobby) {
        String updateData = "LOBBY_UPDATE:" + lobby.getName() + ":" +
                String.join(",", lobby.getPlayerNames());
        for (String playerId : lobby.getPlayerIds()) {
            ClientHandler client = clients.get(playerId);
            if (client != null) {
                client.sendMessage(updateData);
            }
        }
    }

    public static class GameLobby {
        private final String name;
        private final String hostId;
        private final Map<String, String> players = new ConcurrentHashMap<>(); // id -> name

        public GameLobby(String name, String hostId, String hostName) {
            this.name = name;
            this.hostId = hostId;
            this.players.put(hostId, hostName);
        }

        public boolean addPlayer(String playerId, String playerName) {
            if (players.size() < 2) {
                players.put(playerId, playerName);
                return true;
            }
            return false;
        }

        public void removePlayer(String playerId) {
            players.remove(playerId);
        }

        public boolean isEmpty() {
            return players.isEmpty();
        }

        public boolean hasPlayer(String playerId) {
            return players.containsKey(playerId);
        }

        public String getName() { return name; }
        public String getHostId() { return hostId; }
        public Set<String> getPlayerIds() { return players.keySet(); }
        public Collection<String> getPlayerNames() { return players.values(); }
    }

    public static void main(String[] args) {
        try {
            new GameServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}