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
    private Money dayOpenPrice;
    private BigDecimal dayTurnover = BigDecimal.ZERO;

    // Sintesa 5 Masterpiece: Nilai Intrinsik & Fundamental
    private BigDecimal intrinsicValue;
    private BigDecimal trailingEps;
    private double sectorAvgPer = 15.0;

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

        // Inisialisasi Metrik Fundamental Sintesa
        this.sectorAvgPer = 15.0;
        this.trailingEps = initialPrice.getAmount().divide(new BigDecimal("15.0"), 4, RoundingMode.HALF_UP);
        this.intrinsicValue = initialPrice.getAmount().setScale(2, RoundingMode.HALF_UP);
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
        if (issued != replay.getIssuedShares() || treasury != replay.getTreasuryShares()) {
            throw new IllegalArgumentException("Supply save tidak cocok dengan baseline dan riwayat aksi: " + id);
        }
        issuedShares = issued; treasuryShares = treasury;
        shareSupplyEvents.clear(); shareSupplyEvents.putAll(events);
    }
    public boolean isActiveTrading() { return isActiveTrading; }

    public Money getCurrentPrice() { return currentPrice; }
    public Money getPreviousClosePrice() { return previousClosePrice; }
    public Money getDayHigh() { return dayHigh; }
    public Money getDayLow() { return dayLow; }
    public long getDayVolume() { return dayVolume; }
    public Money getDayOpenPrice() { return dayOpenPrice; }
    public BigDecimal getDayTurnover() { return dayTurnover; }
    public void restoreMissingDayStatistics(Money open, BigDecimal turnover) {
        if (dayVolume > 0 && dayTurnover == null)
            restoreMarketState(currentPrice, previousClosePrice, dayHigh, dayLow, dayVolume, open, turnover);
    }
    public BigDecimal getDayAveragePrice() {
        return dayVolume > 0 && dayTurnover != null
                ? dayTurnover.divide(BigDecimal.valueOf(dayVolume), 4, RoundingMode.HALF_UP) : null;
    }
    public void resetReferencePrice(Money price) {
        if (price == null || price.getCurrency() != currency || !price.isPositive()) throw new IllegalArgumentException("Harga referensi tidak valid");
        currentPrice = price; previousClosePrice = price; dayHigh = price; dayLow = price; dayVolume = 0;
        dayOpenPrice = null; dayTurnover = BigDecimal.ZERO;
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
        if (volumeDelta < 0) throw new IllegalArgumentException("Volume transaksi tidak boleh negatif");
        long volume = Math.addExact(dayVolume, volumeDelta);
        Money boundedPrice = newPrice;
        if (currency == Currency.IDR && exchange == MarketExchange.IDX) {
            BigDecimal minNominal = new BigDecimal("50");
            if (boundedPrice.getAmount().compareTo(minNominal) < 0) {
                boundedPrice = new Money(minNominal, currency);
            }
        }
        if (previousClosePrice != null && previousClosePrice.isPositive()) {
            Money ceiling = getDailyCeilingPrice();
            Money floor = getDailyFloorPrice();
            if (ceiling != null && boundedPrice.compareTo(ceiling) > 0) boundedPrice = ceiling;
            if (floor != null && boundedPrice.compareTo(floor) < 0) boundedPrice = floor;
        }
        this.currentPrice = boundedPrice;
        if (boundedPrice.compareTo(dayHigh) > 0) dayHigh = boundedPrice;
        if (boundedPrice.compareTo(dayLow) < 0) dayLow = boundedPrice;
        if (volumeDelta > 0) {
            if (dayVolume == 0) { dayOpenPrice = boundedPrice; dayHigh = boundedPrice; dayLow = boundedPrice; }
            if (dayTurnover != null) dayTurnover = dayTurnover.add(boundedPrice.getAmount().multiply(BigDecimal.valueOf(volumeDelta)));
        }
        this.dayVolume = volume;
    }

    void restoreMarketState(Money price, Money previous, Money high, Money low, long volume, Money open, BigDecimal turnover) {
        for (Money value : new Money[]{price, previous, high, low}) {
            if (value == null || value.getCurrency() != currency || !value.isPositive())
                throw new IllegalArgumentException("Harga save tidak valid: " + id);
        }
        if (volume < 0 || high.compareTo(price) < 0 || low.compareTo(price) > 0 || low.compareTo(high) > 0)
            throw new IllegalArgumentException("Statistik pasar save tidak valid: " + id);
        if (open != null && (open.getCurrency() != currency || !open.isPositive() || volume == 0 || open.compareTo(high) > 0 || open.compareTo(low) < 0)
                || turnover != null && (turnover.signum() < 0 || volume > 0 && turnover.signum() == 0 || volume == 0 && turnover.signum() != 0)
                || volume > 0 && ((open == null) != (turnover == null)))
            throw new IllegalArgumentException("Statistik transaksi harian save tidak valid: " + id);
        currentPrice = price; previousClosePrice = previous; dayHigh = high; dayLow = low; dayVolume = volume;
        dayOpenPrice = open;
        // ponytail: legacy saves with volume lack exact turnover; keep it unknown until the next day rather than estimate it.
        dayTurnover = volume == 0 ? BigDecimal.ZERO : turnover;
    }

    public synchronized void adjustSplitReferencePrices(BigDecimal ratio) {
        if (ratio == null || ratio.signum() <= 0) throw new IllegalArgumentException("Rasio split tidak valid");
        previousClosePrice = new Money(previousClosePrice.getAmount().divide(ratio, 8, RoundingMode.HALF_UP), currency);
        dayHigh = new Money(dayHigh.getAmount().divide(ratio, 8, RoundingMode.HALF_UP), currency);
        dayLow = new Money(dayLow.getAmount().divide(ratio, 8, RoundingMode.HALF_UP), currency);
        if (dayOpenPrice != null) dayOpenPrice = new Money(dayOpenPrice.getAmount().divide(ratio, 8, RoundingMode.HALF_UP), currency);
    }

    public void startNewTradingDay() {
        this.previousClosePrice = this.currentPrice;
        this.dayHigh = this.currentPrice;
        this.dayLow = this.currentPrice;
        this.dayVolume = 0;
        this.dayOpenPrice = null;
        this.dayTurnover = BigDecimal.ZERO;
    }

    public boolean isTradable() {
        return isActiveTrading;
    }

    /**
     * Batas atas kenaikan harian maksimal 25% dari harga penutupan sebelumnya (T03).
     * Dibulatkan ke bawah (floor) ke fraksi harga valid di dalam batas 25%.
     */
    public Money getDailyCeilingPrice() {
        Money ref = (previousClosePrice != null && previousClosePrice.isPositive()) ? previousClosePrice : currentPrice;
        if (ref == null || !ref.isPositive()) {
            return currentPrice;
        }
        BigDecimal rawCeiling = ref.getAmount().multiply(new BigDecimal("1.25"));
        BigDecimal tick = getExchange().getTickSize(rawCeiling);
        BigDecimal ceiling = rawCeiling.divide(tick, 0, RoundingMode.FLOOR).multiply(tick);
        return new Money(ceiling, currency);
    }

    /**
     * Batas bawah penurunan harian maksimal 25% dari harga penutupan sebelumnya (T03).
     * Dibulatkan ke atas (ceiling) ke fraksi harga valid di dalam batas 25%.
     */
    public Money getDailyFloorPrice() {
        Money ref = (previousClosePrice != null && previousClosePrice.isPositive()) ? previousClosePrice : currentPrice;
        if (ref == null || !ref.isPositive()) {
            return currentPrice;
        }
        BigDecimal rawFloor = ref.getAmount().multiply(new BigDecimal("0.75"));
        BigDecimal tick = getExchange().getTickSize(rawFloor);
        BigDecimal floor = rawFloor.divide(tick, 0, RoundingMode.CEILING).multiply(tick);
        return new Money(floor, currency);
    }

    /**
     * Memeriksa apakah harga berada di dalam rentang batas harian [floor, ceiling].
     */
    public boolean isWithinDailyPriceBand(Money price) {
        if (price == null || !price.isPositive()) return false;
        if (previousClosePrice == null || !previousClosePrice.isPositive()) return true;
        return price.compareTo(getDailyFloorPrice()) >= 0 && price.compareTo(getDailyCeilingPrice()) <= 0;
    }

    /**
     * Mendapatkan fraksi harga (tick size) resmi BEI / IDX (Butir 48).
     */
    public BigDecimal getIdxTickSize(BigDecimal price) {
        return getExchange().getTickSize(price);
    }

    /**
     * Pembulatan harga ke atas (Ceiling) kelipatan fraksi IDX (T04 / Butir 48).
     * Memastikan persentase minimum target berita tidak berkurang akibat pembulatan.
     */
    public Money roundUpToIdxTick(BigDecimal targetPrice) {
        if (targetPrice == null || targetPrice.signum() <= 0) return currentPrice;
        if (currency != Currency.IDR || exchange != MarketExchange.IDX) {
            return new Money(targetPrice.setScale(currency.getDecimalPlaces(), RoundingMode.CEILING), currency);
        }
        BigDecimal tick = getIdxTickSize(targetPrice);
        BigDecimal rounded = targetPrice.divide(tick, 0, RoundingMode.CEILING).multiply(tick);
        return new Money(rounded, currency);
    }

    /**
     * Q10: Pembulatan harga ke bawah (Floor) kelipatan fraksi IDX untuk target sentimen turun.
     */
    public Money roundDownToIdxTick(BigDecimal targetPrice) {
        if (targetPrice == null || targetPrice.signum() <= 0) return currentPrice;
        if (currency != Currency.IDR || exchange != MarketExchange.IDX) {
            return new Money(targetPrice.setScale(currency.getDecimalPlaces(), RoundingMode.FLOOR), currency);
        }
        BigDecimal tick = getIdxTickSize(targetPrice);
        BigDecimal rounded = targetPrice.divide(tick, 0, RoundingMode.FLOOR).multiply(tick);
        return new Money(rounded, currency);
    }

    /**
     * Q10: Bulatkan searah target: naik ke atas, turun ke bawah pada fraksi harga sah.
     */
    public Money roundTargetPrice(BigDecimal targetPrice, double targetPercent) {
        if (targetPrice == null || targetPrice.signum() <= 0) return currentPrice;
        if (targetPercent > 0) {
            return roundUpToIdxTick(targetPrice);
        } else if (targetPercent < 0) {
            return roundDownToIdxTick(targetPrice);
        } else {
            return new Money(targetPrice.setScale(currency.getDecimalPlaces(), RoundingMode.HALF_UP), currency);
        }
    }

    // =========================================================================
    // SINTESA 5 MASTERPIECE: METRIK FUNDAMENTAL & NILAI INTRINSIK
    // =========================================================================

    public synchronized BigDecimal getIntrinsicValue() {
        if (intrinsicValue == null) {
            calculateIntrinsicValue();
        }
        return intrinsicValue;
    }

    public synchronized BigDecimal getTrailingEps() {
        if (trailingEps == null) {
            this.trailingEps = currentPrice.getAmount().divide(BigDecimal.valueOf(sectorAvgPer <= 0 ? 15.0 : sectorAvgPer), 2, RoundingMode.HALF_UP);
        }
        return trailingEps;
    }

    public synchronized double getSectorAvgPer() {
        return sectorAvgPer;
    }

    public synchronized void setFundamentalMetrics(BigDecimal eps, double sectorPer) {
        if (eps != null && eps.signum() > 0) this.trailingEps = eps;
        if (sectorPer > 0) this.sectorAvgPer = sectorPer;
        calculateIntrinsicValue();
    }

    public synchronized BigDecimal calculateIntrinsicValue() {
        if (trailingEps == null || trailingEps.signum() <= 0) {
            this.intrinsicValue = currentPrice.getAmount();
            return this.intrinsicValue;
        }
        // 1. Raw Fair Value = EPS * P/E Sektor
        BigDecimal rawFair = trailingEps.multiply(BigDecimal.valueOf(sectorAvgPer <= 0 ? 15.0 : sectorAvgPer)).setScale(2, RoundingMode.HALF_UP);

        // 2. Koridor Clamping [0.60x .. 1.95x dari currentPrice]
        BigDecimal minBound = currentPrice.getAmount().multiply(new BigDecimal("0.60")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal maxBound = currentPrice.getAmount().multiply(new BigDecimal("1.95")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal clamped = rawFair.max(minBound).min(maxBound);

        // 3. Redaman Harga Tinggi (> 300.000 x0.80 | > 500.000 x0.70)
        if (clamped.compareTo(new BigDecimal("300000")) > 0) {
            clamped = clamped.multiply(new BigDecimal("0.80")).setScale(2, RoundingMode.HALF_UP);
        }
        if (clamped.compareTo(new BigDecimal("500000")) > 0) {
            clamped = clamped.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
        }

        // 4. Batas Bawah Harga Nominal Bursa (50 IDR)
        BigDecimal floor = new BigDecimal("50.00");
        this.intrinsicValue = clamped.max(floor);
        return this.intrinsicValue;
    }

    private double lockedFloatRatio = 0.0;

    public synchronized double getLockedFloatRatio() {
        return lockedFloatRatio;
    }

    public synchronized void setLockedFloatRatio(double ratio) {
        this.lockedFloatRatio = Math.max(0.0, Math.min(0.95, ratio));
    }

    public synchronized double getLockedFloatRatio(long totalLockedShares) {
        long total = getIssuedShares();
        if (total <= 0) return 0.0;
        double ratio = (double) totalLockedShares / (double) total;
        this.lockedFloatRatio = Math.min(0.95, Math.max(0.0, ratio));
        return this.lockedFloatRatio;
    }

    public synchronized void restoreFundamentalState(BigDecimal intrinsic, BigDecimal eps, double per) {
        if (eps != null && eps.signum() > 0) this.trailingEps = eps;
        if (per > 0) this.sectorAvgPer = per;
        if (intrinsic != null && intrinsic.signum() > 0) this.intrinsicValue = intrinsic;
        else calculateIntrinsicValue();
    }
}
