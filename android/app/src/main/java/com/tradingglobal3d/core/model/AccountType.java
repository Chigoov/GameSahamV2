package com.tradingglobal3d.core.model;

public enum AccountType {
    PERSONAL("Pribadi"),
    CLIENT("Klien"),
    FIRM("Firma"),
    COMPANY("Perusahaan");

    private final String label;

    AccountType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
