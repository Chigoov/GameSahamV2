package com.tradingglobal3d.core.multiasset;

public enum AssetClass {
    EQUITY("Saham"),
    BOND("Obligasi Negara"),
    COMMODITY("Komoditas"),
    CASH_FX("Kas & Valuta Asing");

    private final String label;

    AssetClass(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
