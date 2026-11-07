package com.tradingglobal3d.core.risk;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Model Posisi Short Selling (Pinjaman Saham & Kewajiban Pengembalian).
 * Hasil penjualan short ditahan sebagai jaminan (collateral) dan tidak dapat ditarik bebas (FR-15.05).
 */
public final class ShortPosition {
    private final String ticker;
    private final String exchange;
    private final Currency currency;
    private long borrowedShares;
    private BigDecimal averageShortPrice;
    private Money collateralLocked;
    private BigDecimal annualBorrowFeeRate; // e.g. 0.025 (2.5% p.a.)
    private Money accruedUnpaidFee;

    public ShortPosition(String ticker, String exchange, Currency currency,
                         long borrowedShares, BigDecimal shortPrice, Money initialProceeds,
                         BigDecimal annualBorrowFeeRate) {
        this.ticker = Objects.requireNonNull(ticker);
        this.exchange = Objects.requireNonNull(exchange);
        this.currency = Objects.requireNonNull(currency);
        this.borrowedShares = borrowedShares;
        this.averageShortPrice = Objects.requireNonNull(shortPrice);
        this.collateralLocked = Objects.requireNonNull(initialProceeds);
        this.annualBorrowFeeRate = Objects.requireNonNull(annualBorrowFeeRate);
        this.accruedUnpaidFee = Money.zero(currency);
    }

    public String getTicker() { return ticker; }
    public String getExchange() { return exchange; }
    public Currency getCurrency() { return currency; }
    public long getBorrowedShares() { return borrowedShares; }
    public BigDecimal getAverageShortPrice() { return averageShortPrice; }
    public Money getCollateralLocked() { return collateralLocked; }
    public BigDecimal getAnnualBorrowFeeRate() { return annualBorrowFeeRate; }
    public Money getAccruedUnpaidFee() { return accruedUnpaidFee; }
    public void addUnpaidFee(Money fee) { if (fee != null && fee.isPositive()) this.accruedUnpaidFee = this.accruedUnpaidFee.add(fee); }
    public void payUnpaidFee(Money fee) { if (fee != null && fee.isPositive()) this.accruedUnpaidFee = this.accruedUnpaidFee.subtract(fee); }

    /**
     * Nilai pasar saham yang sedang dipinjam (pokok kewajiban saham).
     */
    public Money calculateBorrowedStockValue(BigDecimal currentPrice) {
        BigDecimal liabilityAmt = currentPrice.multiply(BigDecimal.valueOf(borrowedShares));
        return new Money(liabilityAmt, currency);
    }

    /**
     * Nilai total kewajiban pengembalian saham dan tunggakan biaya sewa terutang.
     */
    public Money calculateLiability(BigDecimal currentPrice) {
        return calculateBorrowedStockValue(currentPrice).add(accruedUnpaidFee);
    }

    /**
     * Unrealized PnL = (Short Price - Current Price) * Borrowed Shares.
     * Positif jika harga pasar turun di bawah harga jual short; negatif jika harga melonjak naik.
     */
    public Money calculateUnrealizedPnL(BigDecimal currentPrice) {
        BigDecimal diff = averageShortPrice.subtract(currentPrice);
        BigDecimal pnlAmt = diff.multiply(BigDecimal.valueOf(borrowedShares));
        return new Money(pnlAmt, currency);
    }

    /**
     * Hitung biaya sewa pinjaman saham harian.
     * Biaya sewa HANYA dihitung atas nilai pasar saham yang sedang dipinjam.
     * Tidak mengenakan bunga atas tunggakan biaya (no compounding on arrears).
     */
    public Money calculateDailyBorrowFee(BigDecimal currentPrice) {
        if (borrowedShares <= 0) {
            return Money.zero(currency);
        }
        Money stockVal = calculateBorrowedStockValue(currentPrice);
        BigDecimal dailyRate = annualBorrowFeeRate.divide(BigDecimal.valueOf(360), 6, RoundingMode.HALF_UP);
        BigDecimal feeAmt = stockVal.getAmount().multiply(dailyRate);
        return new Money(feeAmt, currency);
    }

    /**
     * Mengurangi kuantitas short saat posisi ditutup sebagian (Buy to Cover).
     */
    public void coverShares(long coveredShares, BigDecimal coverPrice) {
        if (coveredShares <= 0 || coveredShares > borrowedShares) {
            throw new IllegalArgumentException("Jumlah cover tidak valid: " + coveredShares);
        }
        double ratio = (double) coveredShares / borrowedShares;
        borrowedShares -= coveredShares;

        // Lepas proporsi jaminan
        BigDecimal releasedAmt = collateralLocked.getAmount().multiply(BigDecimal.valueOf(ratio));
        collateralLocked = collateralLocked.subtract(new Money(releasedAmt, currency));
    }
}
