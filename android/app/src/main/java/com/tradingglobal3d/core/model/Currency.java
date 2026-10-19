package com.tradingglobal3d.core.model;

public enum Currency {
    IDR("Rp", 0),
    USD("$", 2);

    private final String symbol;
    private final int decimalPlaces;

    Currency(String symbol, int decimalPlaces) {
        this.symbol = symbol;
        this.decimalPlaces = decimalPlaces;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }
}
