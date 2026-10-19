package com.tradingglobal3d.core.market;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Entitas instrumen saham individual.
 * Memiliki identitas unik lintas bursa (e.g. "IDX:BBCA", "US:AAPL")
 * untuk mencegah bentrok ticker pada bursa berbeda.
 */
public final class Instrument {
    private final String id;               // e.g. "IDX:BBCA"
    private final String ticker;           // e.g. "BBCA"
    private final String name;             // e.g. "Bank Central Asia Tbk"
    private final MarketExchange exchange;
    private final String sector;
    private final Currency currency;
    private final int lotSize;
    private final long sharesOutstanding;
    private final boolean isActiveTrading;

    private Money currentPrice;
    private Money previousClosePrice;
    private Money dayHigh;
    private Money dayLow;
    private long dayVolume;

    public Instrument(String id, String ticker, String name, MarketExchange exchange,
                      String sector, Currency currency, int lotSize, long sharesOutstanding,
                      boolean isActiveTrading, Money initialPrice) {
        this.id = Objects.requireNonNull(id);
        this.ticker = Objects.requireNonNull(ticker);
        this.name = Objects.requireNonNull(name);
        this.exchange = Objects.requireNonNull(exchange);
        this.sector = Objects.requireNonNull(sector);
        this.currency = Objects.requireNonNull(currency);
        this.lotSize = lotSize;
        this.sharesOutstanding = sharesOutstanding;
        this.isActiveTrading = isActiveTrading;

        this.currentPrice = Objects.requireNonNull(initialPrice);
        this.previousClosePrice = initialPrice;
        this.dayHigh = initialPrice;
        this.dayLow = initialPrice;
        this.dayVolume = 0;
    }

    public String getId() { return id; }
    public String getTicker() { return ticker; }
    public String getName() { return name; }
    public MarketExchange getExchange() { return exchange; }
    public String getSector() { return sector; }
    public Currency getCurrency() { return currency; }
    public int getLotSize() { return lotSize; }
    public long getSharesOutstanding() { return sharesOutstanding; }
    public boolean isActiveTrading() { return isActiveTrading; }

    public Money getCurrentPrice() { return currentPrice; }
    public Money getPreviousClosePrice() { return previousClosePrice; }
    public Money getDayHigh() { return dayHigh; }
    public Money getDayLow() { return dayLow; }
    public long getDayVolume() { return dayVolume; }

    public Money getMarketCap() {
        BigDecimal cap = currentPrice.getAmount().multiply(BigDecimal.valueOf(sharesOutstanding));
        return new Money(cap, currency);
    }

    public double getDailyChangePercent() {
        if (previousClosePrice.isZero()) return 0.0;
        BigDecimal diff = currentPrice.getAmount().subtract(previousClosePrice.getAmount());
        return diff.divide(previousClosePrice.getAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
    }

    public void updatePrice(Money newPrice, long volumeDelta) {
        this.currentPrice = newPrice;
        if (newPrice.compareTo(dayHigh) > 0) dayHigh = newPrice;
        if (newPrice.compareTo(dayLow) < 0) dayLow = newPrice;
        this.dayVolume += volumeDelta;
    }

    void restoreMarketState(Money price, Money previous, Money high, Money low, long volume) {
        for (Money value : new Money[]{price, previous, high, low}) {
            if (value == null || value.getCurrency() != currency || !value.isPositive())
                throw new IllegalArgumentException("Harga save tidak valid: " + id);
        }
        if (volume < 0 || high.compareTo(price) < 0 || low.compareTo(price) > 0 || low.compareTo(high) > 0)
            throw new IllegalArgumentException("Statistik pasar save tidak valid: " + id);
        currentPrice = price; previousClosePrice = previous; dayHigh = high; dayLow = low; dayVolume = volume;
    }

    public void startNewTradingDay() {
        this.previousClosePrice = this.currentPrice;
        this.dayHigh = this.currentPrice;
        this.dayLow = this.currentPrice;
        this.dayVolume = 0;
    }

    public boolean isTradable() {
        return isActiveTrading;
    }
}
