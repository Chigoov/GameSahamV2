package com.tradingglobal3d.core.corporate;

public enum CorporateActionType {
    CASH_DIVIDEND("Dividen Tunai"),
    STOCK_SPLIT("Pemecahan Saham (Stock Split)"),
    REVERSE_SPLIT("Penggabungan Saham (Reverse Split)"),
    BUYBACK_TENDER("Pembelian Kembali Saham (Buyback)");

    private final String label;
    CorporateActionType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
