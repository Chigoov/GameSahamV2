package com.tradingglobal3d.core.trading;

public enum OrderStatus {
    PENDING_TRIGGER("Menunggu Pemicu"),
    OPEN("Terbuka / Dalam Antrean"),
    PARTIALLY_FILLED("Terisi Sebagian"),
    FILLED("Terisi Penuh"),
    CANCELLED("Dibatalkan"),
    EXPIRED("Kedaluwarsa"),
    REJECTED("Ditolak");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isFinal() {
        return this == FILLED || this == CANCELLED || this == EXPIRED || this == REJECTED;
    }
}
