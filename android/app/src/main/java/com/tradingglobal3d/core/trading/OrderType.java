package com.tradingglobal3d.core.trading;

public enum OrderType {
    MARKET("Pasar (Market)"),
    LIMIT("Limit"),
    STOP_LOSS("Stop-Loss"),
    TAKE_PROFIT("Take-Profit");

    private final String label;

    OrderType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
