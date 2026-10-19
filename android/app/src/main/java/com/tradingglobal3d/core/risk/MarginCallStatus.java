package com.tradingglobal3d.core.risk;

public enum MarginCallStatus {
    HEALTHY("Kesehatan Modal Terjaga"),
    WARNING("Peringatan Risiko Margin"),
    MARGIN_CALL("Margin Call Terbit (Kekurangan Jaminan)"),
    LIQUIDATION_REQUIRED("Likuidasi Paksa Diperlukan"),
    HALTED_PENDING_LIQUIDATION("Likuidasi Tertahan Suspensi Pasar");

    private final String label;
    MarginCallStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
