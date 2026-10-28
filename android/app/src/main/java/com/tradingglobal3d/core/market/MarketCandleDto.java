package com.tradingglobal3d.core.market;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Data Transfer Object untuk Candlestick OHLCV (Open, High, Low, Close, Volume).
 * Sesuai Fitur 1 Masterplan 15 Fitur 2D Portrait.
 */
public final class MarketCandleDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String ticker;
    private final long timestamp;
    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final long volume;
    private final boolean isReference;

    public MarketCandleDto(String ticker, long timestamp, BigDecimal open, BigDecimal high,
                           BigDecimal low, BigDecimal close, long volume, boolean isReference) {
        this.ticker = Objects.requireNonNull(ticker);
        this.timestamp = timestamp;
        this.open = Objects.requireNonNull(open);
        this.high = Objects.requireNonNull(high);
        this.low = Objects.requireNonNull(low);
        this.close = Objects.requireNonNull(close);
        this.volume = Math.max(0L, volume);
        this.isReference = isReference;
    }

    public MarketCandleDto(String ticker, long timestamp, double open, double high,
                           double low, double close, long volume) {
        this(ticker, timestamp, BigDecimal.valueOf(open), BigDecimal.valueOf(high),
                BigDecimal.valueOf(low), BigDecimal.valueOf(close), volume, false);
    }

    public String getTicker() { return ticker; }
    public long getTimestamp() { return timestamp; }
    public BigDecimal getOpen() { return open; }
    public BigDecimal getHigh() { return high; }
    public BigDecimal getLow() { return low; }
    public BigDecimal getClose() { return close; }
    public long getVolume() { return volume; }
    public boolean isReference() { return isReference; }

    public boolean isGreen() {
        return close.compareTo(open) >= 0;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("ticker", ticker);
        obj.put("timestamp", timestamp);
        obj.put("time", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.ROOT)
                .format(new java.util.Date(timestamp)));
        obj.put("open", open.doubleValue());
        obj.put("high", high.doubleValue());
        obj.put("low", low.doubleValue());
        obj.put("close", close.doubleValue());
        obj.put("volume", volume);
        obj.put("isGreen", isGreen());
        obj.put("isReference", isReference);
        return obj;
    }

    public static MarketCandleDto fromJsonObject(JSONObject json) throws JSONException {
        return new MarketCandleDto(
                json.getString("ticker"),
                json.optLong("timestamp", System.currentTimeMillis()),
                BigDecimal.valueOf(json.getDouble("open")),
                BigDecimal.valueOf(json.getDouble("high")),
                BigDecimal.valueOf(json.getDouble("low")),
                BigDecimal.valueOf(json.getDouble("close")),
                json.optLong("volume", 0),
                json.optBoolean("isReference", false)
        );
    }
}
