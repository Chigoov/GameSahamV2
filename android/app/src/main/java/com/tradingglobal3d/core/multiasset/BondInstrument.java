package com.tradingglobal3d.core.multiasset;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Instrumen Obligasi Negara (Fixed Income Bond).
 * Memiliki kupon bunga tetap tahunan, nilai par, dan tanggal jatuh tempo.
 */
public final class BondInstrument {
    private final String id;
    private final String ticker;
    private final String name;
    private final Currency currency;
    private final Money parValue;             // e.g. Rp 1.000.000 per unit obligasi
    private final BigDecimal couponAnnualRate;// e.g. 0.06375 (6.375% p.a.)
    private final LocalDate maturityDate;
    private Money currentCleanPrice;          // Harga pasar bersih per unit

    public BondInstrument(String id, String ticker, String name, Currency currency,
                          Money parValue, BigDecimal couponAnnualRate,
                          LocalDate maturityDate, Money initialCleanPrice) {
        this.id = Objects.requireNonNull(id);
        this.ticker = Objects.requireNonNull(ticker);
        this.name = Objects.requireNonNull(name);
        this.currency = Objects.requireNonNull(currency);
        this.parValue = Objects.requireNonNull(parValue);
        this.couponAnnualRate = Objects.requireNonNull(couponAnnualRate);
        this.maturityDate = Objects.requireNonNull(maturityDate);
        this.currentCleanPrice = Objects.requireNonNull(initialCleanPrice);
    }

    public String getId() { return id; }
    public String getTicker() { return ticker; }
    public String getName() { return name; }
    public Currency getCurrency() { return currency; }
    public Money getParValue() { return parValue; }
    public BigDecimal getCouponAnnualRate() { return couponAnnualRate; }
    public LocalDate getMaturityDate() { return maturityDate; }
    public Money getCurrentCleanPrice() { return currentCleanPrice; }

    public void updatePrice(Money newPrice) {
        this.currentCleanPrice = newPrice;
    }

    /**
     * Menghitung kupon bunga yang terakru berdasarkan jumlah hari kepemilikan.
     */
    public Money calculateAccruedCoupon(int daysHeld, long unitsHeld) {
        if (daysHeld <= 0 || unitsHeld <= 0) return Money.zero(currency);
        BigDecimal totalNominal = parValue.getAmount().multiply(BigDecimal.valueOf(unitsHeld));
        BigDecimal dailyRate = couponAnnualRate.divide(BigDecimal.valueOf(365), 8, RoundingMode.HALF_UP);
        BigDecimal accrued = totalNominal.multiply(dailyRate).multiply(BigDecimal.valueOf(daysHeld))
                .setScale(currency.getDecimalPlaces(), RoundingMode.HALF_UP);
        return new Money(accrued, currency);
    }
}
