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
    private final long catalogIssuedShares;
    private long issuedShares;
    private long treasuryShares;
    private final java.util.Map<String, String> shareSupplyEvents = new java.util.LinkedHashMap<>();
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
        this.catalogIssuedShares = sharesOutstanding;
        this.issuedShares = sharesOutstanding;
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
    public synchronized long getSharesOutstanding() { return Math.subtractExact(issuedShares, treasuryShares); }
    public long getCatalogIssuedShares() { return catalogIssuedShares; }
    public synchronized long getIssuedShares() { return issuedShares; }
    public synchronized long getTreasuryShares() { return treasuryShares; }
    public synchronized boolean hasShareSupplyEvent(String eventId) { return shareSupplyEvents.containsKey(eventId); }

    private long[] nextShareSupply(String eventId, BigDecimal ratio, long issuedDelta, long treasuryDelta) {
        if (eventId == null || eventId.trim().isEmpty() || ratio == null || ratio.signum() <= 0)
            throw new IllegalArgumentException("Perubahan supply saham tidak valid");
        String payload = ratio.stripTrailingZeros().toPlainString() + ":" + issuedDelta + ":" + treasuryDelta;
        if (shareSupplyEvents.containsKey(eventId)) {
            if (!payload.equals(shareSupplyEvents.get(eventId))) throw new IllegalArgumentException("ID supply sudah dipakai dengan perubahan berbeda");
            return null;
        }
        long issued = Math.addExact(BigDecimal.valueOf(issuedShares).multiply(ratio).setScale(0, RoundingMode.DOWN).longValueExact(), issuedDelta);
        long treasury = Math.addExact(BigDecimal.valueOf(treasuryShares).multiply(ratio).setScale(0, RoundingMode.DOWN).longValueExact(), treasuryDelta);
        if (issued < 0 || treasury < 0 || treasury > issued) throw new IllegalArgumentException("Supply saham atau treasury tidak sah");
        return new long[]{issued, treasury};
    }

    public synchronized void validateShareSupplyEvent(String eventId, BigDecimal ratio, long issuedDelta, long treasuryDelta) {
        nextShareSupply(eventId, ratio, issuedDelta, treasuryDelta);
    }

    public synchronized void applyShareSupplyEvent(String eventId, BigDecimal ratio, long issuedDelta, long treasuryDelta) {
        long[] next = nextShareSupply(eventId, ratio, issuedDelta, treasuryDelta);
        if (next == null) return;
        issuedShares = next[0]; treasuryShares = next[1];
        shareSupplyEvents.put(eventId, ratio.stripTrailingZeros().toPlainString() + ":" + issuedDelta + ":" + treasuryDelta);
    }

    synchronized org.json.JSONObject exportShareSupply() throws org.json.JSONException {
        return new org.json.JSONObject().put("version", 1).put("issuedShares", issuedShares).put("treasuryShares", treasuryShares)
                .put("events", new org.json.JSONObject(shareSupplyEvents))
                .put("eventOrder", new org.json.JSONArray(shareSupplyEvents.keySet()));
    }

    synchronized void restoreShareSupply(org.json.JSONObject state) throws org.json.JSONException {
        if (state == null) {
            issuedShares = catalogIssuedShares; treasuryShares = 0; shareSupplyEvents.clear(); return;
        }
        if (state.getInt("version") != 1) throw new IllegalArgumentException("Versi supply saham tidak didukung");
        long issued = state.getLong("issuedShares"), treasury = state.getLong("treasuryShares");
        if (issued < 0 || treasury < 0 || treasury > issued) throw new IllegalArgumentException("Supply save tidak valid");
        java.util.Map<String, String> events = new java.util.LinkedHashMap<>();
        org.json.JSONObject savedEvents = state.getJSONObject("events");
        org.json.JSONArray order = state.getJSONArray("eventOrder");
        if (order.length() != savedEvents.length()) throw new IllegalArgumentException("Urutan supply save tidak lengkap");
        Instrument replay = new Instrument(id, ticker, name, exchange, sector, currency, lotSize, catalogIssuedShares, isActiveTrading, currentPrice);
        for (int i = 0; i < order.length(); i++) {
            String key = order.getString(i);
            if (key.trim().isEmpty() || events.containsKey(key)) throw new IllegalArgumentException("ID supply save kosong atau duplikat");
            String payload = savedEvents.getString(key);
            String[] parts = payload.split(":", -1);
            if (parts.length != 3) throw new IllegalArgumentException("Payload supply save tidak valid");
            replay.applyShareSupplyEvent(key, new BigDecimal(parts[0]), Long.parseLong(parts[1]), Long.parseLong(parts[2]));
            events.put(key, payload);
        }
        if (issued != replay.getIssuedShares() || treasury != replay.getTreasuryShares())
            throw new IllegalArgumentException("Supply save tidak cocok dengan baseline dan riwayat aksi");
        issuedShares = issued; treasuryShares = treasury;
        shareSupplyEvents.clear(); shareSupplyEvents.putAll(events);
    }
    public boolean isActiveTrading() { return isActiveTrading; }

    public Money getCurrentPrice() { return currentPrice; }
    public Money getPreviousClosePrice() { return previousClosePrice; }
    public Money getDayHigh() { return dayHigh; }
    public Money getDayLow() { return dayLow; }
    public long getDayVolume() { return dayVolume; }
    public void resetReferencePrice(Money price) {
        if (price == null || price.getCurrency() != currency || !price.isPositive()) throw new IllegalArgumentException("Harga referensi tidak valid");
        currentPrice = price; previousClosePrice = price; dayHigh = price; dayLow = price; dayVolume = 0;
    }

    public Money getMarketCap() {
        BigDecimal cap = currentPrice.getAmount().multiply(BigDecimal.valueOf(getSharesOutstanding()));
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

    public synchronized void adjustSplitReferencePrices(BigDecimal ratio) {
        if (ratio == null || ratio.signum() <= 0) throw new IllegalArgumentException("Rasio split tidak valid");
        previousClosePrice = new Money(previousClosePrice.getAmount().divide(ratio, 8, RoundingMode.HALF_UP), currency);
        dayHigh = new Money(dayHigh.getAmount().divide(ratio, 8, RoundingMode.HALF_UP), currency);
        dayLow = new Money(dayLow.getAmount().divide(ratio, 8, RoundingMode.HALF_UP), currency);
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
