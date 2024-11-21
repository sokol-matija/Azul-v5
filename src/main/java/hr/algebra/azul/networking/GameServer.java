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
        if (lobby != null && !lobby.isFull()) {
            synchronized (lobby) {
                if (!lobby.hasPlayer(clientId) && !lobby.hasPlayerName(playerName)) {
                    if (lobby.addPlayer(clientId, playerName)) {
                        String updateData = "LOBBY_UPDATE:" + lobby.getName() + ":" +
                                String.join(",", lobby.getPlayerNames());

                        // Broadcast to all players in lobby
                        for (String playerId : lobby.getPlayerIds()) {
                            ClientHandler client = clients.get(playerId);
                            if (client != null) {
                                client.sendMessage(updateData);
                            }
                        }
                    }
                } else {
                    ClientHandler client = clients.get(clientId);
                    if (client != null) {
                        client.sendMessage("ERROR:Already in lobby or name taken");
                    }
                }
            }
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
        private final int maxPlayers = 2;
        private boolean isGameStarted = false;
        private final Set<String> readyPlayers = ConcurrentHashMap.newKeySet();

        public GameLobby(String name, String hostId, String hostName) {
            this.name = name;
            this.hostId = hostId;
            this.players.put(hostId, hostName);
        }

        public boolean addPlayer(String playerId, String playerName) {
            if (players.size() < maxPlayers && !players.containsKey(playerId) && !players.containsValue(playerName)) {
                players.put(playerId, playerName);
                return true;
            }
            return false;
        }

        public void removePlayer(String playerId) {
            players.remove(playerId);
            readyPlayers.remove(playerId);
        }

        public boolean setPlayerReady(String playerId) {
            if (players.containsKey(playerId)) {
                readyPlayers.add(playerId);
                return true;
            }
            return false;
        }

        public boolean setPlayerNotReady(String playerId) {
            return readyPlayers.remove(playerId);
        }

        public boolean isPlayerReady(String playerId) {
            return readyPlayers.contains(playerId);
        }

        public boolean areAllPlayersReady() {
            return players.size() == maxPlayers && readyPlayers.size() == players.size();
        }

        public void startGame() {
            isGameStarted = true;
        }

        public boolean isEmpty() {
            return players.isEmpty();
        }

        public boolean isFull() {
            return players.size() >= maxPlayers;
        }

        public boolean hasPlayer(String playerId) {
            return players.containsKey(playerId);
        }

        public boolean hasPlayerName(String playerName) {
            return players.values().contains(playerName);
        }

        public boolean isHost(String playerId) {
            return playerId.equals(hostId);
        }

        // Getters
        public String getName() { return name; }
        public String getHostId() { return hostId; }
        public Set<String> getPlayerIds() { return players.keySet(); }
        public Collection<String> getPlayerNames() { return players.values(); }
        public Map<String, String> getPlayers() { return Collections.unmodifiableMap(players); }
        public boolean isGameStarted() { return isGameStarted; }
        public int getMaxPlayers() { return maxPlayers; }
        public Set<String> getReadyPlayers() { return Collections.unmodifiableSet(readyPlayers); }

        @Override
        public String toString() {
            return String.format("GameLobby[name=%s, players=%d/%d, started=%b]",
                    name, players.size(), maxPlayers, isGameStarted);
        }
    }

    public static void main(String[] args) {
        try {
            new GameServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastToOtherPlayer(String senderId, String message) {
        // Find sender's lobby
        GameLobby senderLobby = null;
        for (GameLobby lobby : lobbies.values()) {
            if (lobby.hasPlayer(senderId)) {
                senderLobby = lobby;
                break;
            }
        }

        if (senderLobby != null) {
            // Send message to other player in the same lobby
            for (String playerId : senderLobby.getPlayerIds()) {
                if (!playerId.equals(senderId)) {  // Don't send back to sender
                    ClientHandler client = clients.get(playerId);
                    if (client != null) {
                        client.sendMessage(message);
                    }
                }
            }
        }
    }
}