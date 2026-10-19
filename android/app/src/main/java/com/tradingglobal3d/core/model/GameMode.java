package com.tradingglobal3d.core.model;

public enum GameMode {
    INTERNAL_MARKET("Pasar Internal Game"),
    HISTORICAL_REPLAY("Replay Historis");

    private final String displayName;

    GameMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
