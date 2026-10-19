package com.tradingglobal3d.core.market;

import com.tradingglobal3d.core.model.Currency;

import java.math.BigDecimal;

public enum MarketExchange {
    IDX("Bursa Efek Indonesia", Currency.IDR, 100, "Asia/Jakarta"),
    US("US Markets (NYSE/NASDAQ)", Currency.USD, 1, "America/New_York");

    private final String fullName;
    private final Currency baseCurrency;
    private final int defaultLotSize;
    private final String timeZoneId;

    MarketExchange(String fullName, Currency baseCurrency, int defaultLotSize, String timeZoneId) {
        this.fullName = fullName;
        this.baseCurrency = baseCurrency;
        this.defaultLotSize = defaultLotSize;
        this.timeZoneId = timeZoneId;
    }

    public String getFullName() { return fullName; }
    public Currency getBaseCurrency() { return baseCurrency; }
    public int getDefaultLotSize() { return defaultLotSize; }
    public String getTimeZoneId() { return timeZoneId; }

    /**
     * Fraksi harga resmi (Tick Size).
     */
    public BigDecimal getTickSize(BigDecimal price) {
        if (this == US) {
            return new BigDecimal("0.01");
        }
        // Aturan fraksi harga BEI (IDX)
        double p = price.doubleValue();
        if (p < 200.0) return BigDecimal.valueOf(1);
        if (p < 500.0) return BigDecimal.valueOf(2);
        if (p < 2000.0) return BigDecimal.valueOf(5);
        if (p < 5000.0) return BigDecimal.valueOf(10);
        return BigDecimal.valueOf(25);
    }
}
