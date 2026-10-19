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
        this.date = Objects.requireNonNull(date);
        this.open = Objects.requireNonNull(open);
        this.high = Objects.requireNonNull(high);
        this.low = Objects.requireNonNull(low);
        this.close = Objects.requireNonNull(close);
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
