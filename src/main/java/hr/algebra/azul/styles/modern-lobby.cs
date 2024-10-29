 public String getStylesheet() {
        return """
            /* Global Styles */
            .root {
                -fx-font-family: 'Segoe UI', sans-serif;
            }

            /* Header */
            .header-title {
                -fx-font-size: 28px;
                -fx-font-weight: bold;
                -fx-text-fill: linear-gradient(to right, #3B82F6, #8B5CF6);
            }

            /* Buttons */
            .create-button {
                -fx-background-color: #3B82F6;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 10 20;
                -fx-background-radius: 5;
            }

            .create-button:hover {
                -fx-background-color: #2563EB;
            }

            .refresh-button {
                -fx-background-color: transparent;
                -fx-border-color: #4B5563;
                -fx-border-radius: 5;
                -fx-text-fill: #9CA3AF;
            }

            .refresh-button:hover {
                -fx-border-color: #6B7280;
                -fx-text-fill: white;
            }

            /* Search Field */
            .search-field {
                -fx-background-color: #1F2937;
                -fx-text-fill: white;
                -fx-prompt-text-fill: #6B7280;
                -fx-padding: 8;
                -fx-background-radius: 5;
            }

            /* Lobby List */
            .lobby-list {
                -fx-background-color: transparent;
                -fx-padding: 5;
            }

            .lobby-cell {
                -fx-background-color: #1F2937;
                -fx-padding: 10;
                -fx-background-radius: 5;
                -fx-border-color: #374151;
                -fx-border-radius: 5;
            }

            .lobby-cell:hover {
                -fx-background-color: #374151;
            }

            .lobby-name {
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
            }

            .lobby-info {
                -fx-text-fill: #9CA3AF;
            }

            .lobby-status {
                -fx-text-fill: #3B82F6;
                -fx-font-size: 12px;
                -fx-padding: 3 8;
                -fx-background-color: rgba(59, 130, 246, 0.1);
                -fx-background-radius: 10;
            }

            /* Selected Lobby Panel */
            .selected-lobby-panel {
                -fx-background-color: #1F2937;
                -fx-padding: 20;
                -fx-background-radius: 5;
            }

            .selected-lobby-title {
                -fx-font-size: 20px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
            }

            .detail-label {
                -fx-text-fill: #9CA3AF;
            }

            .detail-value {
                -fx-text-fill: white;
                -fx-font-weight: bold;
            }

            .join-button {
                -fx-background-color: #3B82F6;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 10;
                -fx-background-radius: 5;
            }

            .join-button:hover {
                -fx-background-color: #2563EB;
            }
            """;
    }
    // Getters for the controller
        public Stage getStage() { return stage; }
        public ListView<GameLobby> getLobbyListView() { return lobbyListView; }
        public Button getCreateLobbyButton() { return createLobbyButton; }
        public Button getRefreshButton() { return refreshButton; }
        public TextField getSearchField() { return searchField; }
}