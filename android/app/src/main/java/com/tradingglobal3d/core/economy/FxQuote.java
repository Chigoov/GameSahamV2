package com.tradingglobal3d.core.economy;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Kuotasi Nilai Tukar Valuta Asing (FX) berumur.
 * Memuat kurs tengah, spread bid/ask 0.5%, biaya konversi, dan batas waktu kedaluwarsa.
 */
public final class FxQuote {
    public static final BigDecimal SPREAD_RATE = new BigDecimal("0.0050"); // 0.5% spread
    public static final long QUOTE_VALIDITY_MS = 60_000L; // Berlaku 60 detik

    private final String quoteId;
    private final BigDecimal midRate;      // e.g. 15800 IDR per 1 USD
    private final BigDecimal bidRate;      // Harga beli USD oleh dealer (pemain jual USD terima IDR): mid * (1 - spread/2)
    private final BigDecimal askRate;      // Harga jual USD oleh dealer (pemain beli USD bayar IDR): mid * (1 + spread/2)
    private final long createdAt;
    private final long expiresAt;

    public FxQuote(String quoteId, BigDecimal midRate, long createdAt) {
        this.quoteId = Objects.requireNonNull(quoteId);
        this.midRate = Objects.requireNonNull(midRate);
        if (midRate.signum() <= 0) throw new IllegalArgumentException("Kurs tengah harus positif");
        this.createdAt = createdAt;
        this.expiresAt = createdAt + QUOTE_VALIDITY_MS;

        BigDecimal halfSpread = SPREAD_RATE.divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);
        this.bidRate = midRate.multiply(BigDecimal.ONE.subtract(halfSpread)).setScale(2, RoundingMode.HALF_UP);
        this.askRate = midRate.multiply(BigDecimal.ONE.add(halfSpread)).setScale(2, RoundingMode.HALF_UP);
    }

    public String getQuoteId() { return quoteId; }
    public BigDecimal getMidRate() { return midRate; }
    public BigDecimal getBidRate() { return bidRate; }
    public BigDecimal getAskRate() { return askRate; }
    public long getCreatedAt() { return createdAt; }
    public long getExpiresAt() { return expiresAt; }

    public boolean isExpired(long currentTimestamp) {
        return currentTimestamp > expiresAt;
    }

    public static final class ConversionPreview {
        public final Currency from;
        public final Currency to;
        public final Money fromAmount;
        public final BigDecimal effectiveRate;
        public final Money grossToAmount;
        public final Money feeAmount;
        public final Money netToAmount;

        public ConversionPreview(Currency from, Currency to, Money fromAmount,
                                 BigDecimal effectiveRate, Money grossToAmount,
                                 Money feeAmount, Money netToAmount) {
            this.from = from;
            this.to = to;
            this.fromAmount = fromAmount;
            this.effectiveRate = effectiveRate;
            this.grossToAmount = grossToAmount;
            this.feeAmount = feeAmount;
            this.netToAmount = netToAmount;
        }
    }

    /**
     * Menghitung preview konversi FX secara matematis tanpa arbitrase.
     * IDR -> USD: bayar IDR pada kurs Ask.
     * USD -> IDR: terima IDR pada kurs Bid.
     */
    public ConversionPreview calculatePreview(Currency from, Currency to, Money amount) {
        if (from == null || to == null || from == to) {
            throw new IllegalArgumentException("Mata uang asal dan tujuan harus berbeda dan valid");
        }
        if (amount == null || amount.getCurrency() != from || !amount.isPositive())
            throw new IllegalArgumentException("Jumlah konversi harus positif dalam mata uang asal");
        if (from == Currency.IDR && amount.getAmount().divide(askRate, 2, RoundingMode.FLOOR).signum() <= 0)
            throw new IllegalArgumentException("Jumlah konversi terlalu kecil untuk menghasilkan USD");
        if (from == Currency.USD && amount.getAmount().multiply(bidRate).setScale(0, RoundingMode.FLOOR).signum() <= 0)
            throw new IllegalArgumentException("Jumlah konversi terlalu kecil untuk menghasilkan IDR");

        if (from == Currency.IDR && to == Currency.USD) {
            // Pemain beli USD menggunakan IDR (rate = askRate IDR per USD)
            BigDecimal grossUsd = amount.getAmount().divide(askRate, 4, RoundingMode.FLOOR);
            // Bulatkan ke bawah ke scale 2 USD untuk mencegah keuntungan fraksi
            BigDecimal netUsd = grossUsd.setScale(2, RoundingMode.FLOOR);
            Money netMoney = new Money(netUsd, Currency.USD);
            return new ConversionPreview(from, to, amount, askRate, netMoney, Money.zero(Currency.USD), netMoney);
        } else {
            // Pemain jual USD menerima IDR (rate = bidRate IDR per USD)
            BigDecimal grossIdr = amount.getAmount().multiply(bidRate).setScale(0, RoundingMode.FLOOR);
            Money netMoney = new Money(grossIdr, Currency.IDR);
            return new ConversionPreview(from, to, amount, bidRate, netMoney, Money.zero(Currency.IDR), netMoney);
        }
    }
}
