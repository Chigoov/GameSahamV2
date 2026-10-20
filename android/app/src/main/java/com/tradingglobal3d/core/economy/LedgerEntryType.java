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
    YIELD("Imbal Hasil Harian Reksa Dana"),
    PERFORMANCE_FEE("Performance Fee Manajer"),
    DIVIDEND("Dividen Emiten"),
    SALARY("Gaji / Tunjangan Analis"),
    MISSION_REWARD("Imbalan Misi"),
    BAILOUT("Dana Hibah / Pemulihan Modal"),
    DEBT_RESTRUCTURING("Restrukturisasi Utang Margin"),
    LOAN_DISBURSEMENT("Pencairan Pinjaman Margin"),
    LOAN_REPAYMENT("Pelunasan Pinjaman Margin"),
    COMPANY_CAPITAL_INJECTION("Penyertaan Modal Perusahaan"),
    COMPANY_CAPITAL_WITHDRAWAL("Penarikan Modal Perusahaan"),
    COMPANY_PAYROLL("Pembayaran Gaji Pegawai"),
    COMPANY_REVENUE("Pendapatan Operasional Perusahaan"),
    COLLECTION_PURCHASE("Pembelian Barang Koleksi"),
    COLLECTION_SALE("Penjualan Barang Koleksi"),
    COLLECTION_MAINTENANCE("Biaya Pemeliharaan Koleksi");

    private final String description;

    LedgerEntryType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
