package com.tradingglobal3d.core.trading;

public enum OrderSide {
    BUY("Beli"),
    SELL("Jual");

    private final String label;

    OrderSide(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
