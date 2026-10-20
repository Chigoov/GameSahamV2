package com.tradingglobal3d.core.market;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Bar data historis OHLCV untuk satu interval harian nyata.
 */
public final class HistoricalBar {
    private final LocalDate date;
    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final long volume;

    public HistoricalBar(LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, long volume) {
        this.date = Objects.requireNonNull(date, "Tanggal tidak boleh null");
        this.open = Objects.requireNonNull(open, "Open price tidak boleh null");
        this.high = Objects.requireNonNull(high, "High price tidak boleh null");
        this.low = Objects.requireNonNull(low, "Low price tidak boleh null");
        this.close = Objects.requireNonNull(close, "Close price tidak boleh null");

        if (open.signum() <= 0 || high.signum() <= 0 || low.signum() <= 0 || close.signum() <= 0) {
            throw new IllegalArgumentException("Harga OHLC harus lebih besar dari 0");
        }
        if (high.compareTo(low) < 0) {
            throw new IllegalArgumentException("High tidak boleh lebih rendah dari Low");
        }
        if (high.compareTo(open) < 0 || high.compareTo(close) < 0) {
            throw new IllegalArgumentException("High tidak boleh lebih rendah dari Open atau Close");
        }
        if (low.compareTo(open) > 0 || low.compareTo(close) > 0) {
            throw new IllegalArgumentException("Low tidak boleh lebih tinggi dari Open atau Close");
        }
        if (volume < 0) {
            throw new IllegalArgumentException("Volume transaksi tidak boleh negatif");
        }

        this.volume = volume;
    }

    public LocalDate getDate() { return date; }
    public BigDecimal getOpen() { return open; }
    public BigDecimal getHigh() { return high; }
    public BigDecimal getLow() { return low; }
    public BigDecimal getClose() { return close; }
    public long getVolume() { return volume; }

    @Override
    public String toString() {
        return String.format("%s [O:%s H:%s L:%s C:%s V:%d]", date, open, high, low, close, volume);
    }
}
