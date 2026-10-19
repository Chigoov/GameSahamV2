package com.tradingglobal3d.core.economy;

public enum LedgerEntryType {
    INITIAL_CAPITAL("Modal Awal"),
    DEPOSIT("Setoran Kas"),
    WITHDRAWAL("Penarikan Kas"),
    TRADE_BUY("Pembelian Saham"),
    TRADE_SELL("Penjualan Saham"),
    TRANSACTION_FEE("Biaya Transaksi / Pajak"),
    FX_CONVERSION_DEBIT("Konversi FX (Debit)"),
    FX_CONVERSION_CREDIT("Konversi FX (Kredit)"),
    MANAGEMENT_FEE("Management Fee Manajer"),
    PERFORMANCE_FEE("Performance Fee Manajer"),
    DIVIDEND("Dividen Emiten"),
    SALARY("Gaji / Tunjangan Analis"),
    MISSION_REWARD("Imbalan Misi"),
    BAILOUT("Dana Hibah / Pemulihan Modal"),
    LOAN_DISBURSEMENT("Pencairan Pinjaman Margin"),
    LOAN_REPAYMENT("Pelunasan Pinjaman Margin");

    private final String description;

    LedgerEntryType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
