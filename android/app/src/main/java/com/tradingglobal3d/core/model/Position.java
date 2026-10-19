package com.tradingglobal3d.core.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Posisi kepemilikan instrumen (saham, obligasi, dll).
 */
public final class Position {
    private final String ticker;
    private final String exchange;
    private final Currency currency;
    private long quantity;              // Satuan lembar saham (atau unit)
    private BigDecimal averageCost;      // Rata-rata harga beli per unit
    private BigDecimal currentPrice;     // Harga pasar saat ini
    private BigDecimal realizedPnL = BigDecimal.ZERO; // Gross, transaction fees remain in ledger

    public Position(String ticker, String exchange, Currency currency, long quantity, BigDecimal averageCost, BigDecimal currentPrice) {
        this(ticker, exchange, currency, quantity, averageCost, currentPrice, BigDecimal.ZERO);
    }

    public Position(String ticker, String exchange, Currency currency, long quantity, BigDecimal averageCost,
                    BigDecimal currentPrice, BigDecimal realizedPnL) {
        this.realizedPnL = realizedPnL;
        this.ticker = Objects.requireNonNull(ticker);
        this.exchange = Objects.requireNonNull(exchange);
        this.currency = Objects.requireNonNull(currency);
        this.quantity = quantity;
        this.averageCost = averageCost;
        this.currentPrice = currentPrice;
    }

    public String getTicker() { return ticker; }
    public String getExchange() { return exchange; }
    public Currency getCurrency() { return currency; }
    public long getQuantity() { return quantity; }
    public BigDecimal getAverageCost() { return averageCost; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public Money getRealizedPnL() { return realizedPnL == null ? null : new Money(realizedPnL, currency); }

    public Money getTotalCost() {
        return new Money(averageCost.multiply(BigDecimal.valueOf(quantity)), currency);
    }

    public Money getMarketValue() {
        return new Money(currentPrice.multiply(BigDecimal.valueOf(quantity)), currency);
    }

    public Money getUnrealizedPnL() {
        return getMarketValue().subtract(getTotalCost());
    }

    public double getUnrealizedPnLPercent() {
        if (quantity == 0 || averageCost.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        BigDecimal diff = currentPrice.subtract(averageCost);
        return diff.divide(averageCost, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
    }

    public void updateMarketPrice(BigDecimal newPrice) {
        this.currentPrice = newPrice;
    }

    /**
     * Memperbarui posisi setelah eksekusi order beli.
     */
    public void addBuy(long addedQty, BigDecimal executionPrice) {
        if (addedQty <= 0) return;
        BigDecimal currentTotal = averageCost.multiply(BigDecimal.valueOf(quantity));
        BigDecimal addedTotal = executionPrice.multiply(BigDecimal.valueOf(addedQty));
        long newQty = quantity + addedQty;
        BigDecimal newAvgCost = currentTotal.add(addedTotal).divide(BigDecimal.valueOf(newQty), 4, RoundingMode.HALF_UP);

        this.quantity = newQty;
        this.averageCost = newAvgCost;
        this.currentPrice = executionPrice;
    }

    /**
     * Memperbarui posisi setelah eksekusi order jual.
     * Mengembalikan laba/rugi terealisasi (Realized PnL).
     */
    public Money addSell(long soldQty, BigDecimal executionPrice) {
        if (soldQty <= 0) return Money.zero(currency);
        if (soldQty > quantity) {
            throw new IllegalStateException("Cannot sell more quantity than held: held=" + quantity + ", sold=" + soldQty);
        }
        BigDecimal costOfSold = averageCost.multiply(BigDecimal.valueOf(soldQty));
        BigDecimal proceedOfSold = executionPrice.multiply(BigDecimal.valueOf(soldQty));
        BigDecimal realized = proceedOfSold.subtract(costOfSold);

        this.quantity -= soldQty;
        this.currentPrice = executionPrice;
        if (realizedPnL != null) realizedPnL = realizedPnL.add(realized);

        return new Money(realized, currency);
    }
}
