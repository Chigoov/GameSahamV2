package com.tradingglobal3d.core.market;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Indeks Pasar Gabungan / Sektoral dengan perhitungan bobot kapitalisasi pasar.
 */
public final class MarketIndex {
    public enum CalculationMethod {
        MARKET_CAP_WEIGHTED,
        EQUAL_WEIGHTED
    }

    private final String id;
    private final String ticker;
    private final String name;
    private final MarketExchange exchange;
    private final double baseValue;
    private final CalculationMethod method;
    private final List<String> memberIds;
    private double currentValue;
    private double previousCloseValue;

    public MarketIndex(String id, String ticker, String name, MarketExchange exchange,
                       double baseValue, CalculationMethod method, List<String> memberIds) {
        this.id = Objects.requireNonNull(id);
        this.ticker = Objects.requireNonNull(ticker);
        this.name = Objects.requireNonNull(name);
        this.exchange = Objects.requireNonNull(exchange);
        this.baseValue = baseValue;
        this.method = method;
        this.memberIds = Collections.unmodifiableList(new ArrayList<>(memberIds));
        this.currentValue = baseValue;
        this.previousCloseValue = baseValue;
    }

    public String getId() { return id; }
    public String getTicker() { return ticker; }
    public String getName() { return name; }
    public MarketExchange getExchange() { return exchange; }
    public double getBaseValue() { return baseValue; }
    public CalculationMethod getMethod() { return method; }
    public List<String> getMemberIds() { return memberIds; }
    public double getCurrentValue() { return currentValue; }
    public double getPreviousCloseValue() { return previousCloseValue; }

    public synchronized void startNewTradingDay() {
        this.previousCloseValue = this.currentValue;
    }

    public synchronized void resetToBase() {
        this.currentValue = this.baseValue;
        this.previousCloseValue = this.baseValue;
    }

    public synchronized org.json.JSONObject exportState() throws org.json.JSONException {
        return new org.json.JSONObject()
                .put("id", id)
                .put("currentValue", currentValue)
                .put("previousCloseValue", previousCloseValue);
    }

    public synchronized void restoreState(org.json.JSONObject obj) {
        if (obj == null) return;
        this.currentValue = obj.optDouble("currentValue", this.baseValue);
        this.previousCloseValue = obj.optDouble("previousCloseValue", this.currentValue);
    }

    /**
     * Menghitung nilai terkini indeks berdasarkan kapitalisasi pasar atau bobot anggota.
     */
    public synchronized double recalculate(Map<String, Instrument> instrumentMap) {
        double anchor = previousCloseValue > 0.0 ? previousCloseValue : baseValue;
        if (memberIds.isEmpty()) return anchor;

        if (method == CalculationMethod.EQUAL_WEIGHTED) {
            double totalReturn = 0.0;
            int count = 0;
            for (String mid : memberIds) {
                Instrument inst = instrumentMap.get(mid);
                if (inst != null) {
                    totalReturn += (1.0 + (inst.getDailyChangePercent() / 100.0));
                    count++;
                }
            }
            if (count > 0) {
                this.currentValue = anchor * (totalReturn / count);
            }
            return currentValue;
        }

        // MARKET_CAP_WEIGHTED (Free-Float Adjusted)
        BigDecimal totalCurrentCap = BigDecimal.ZERO;
        BigDecimal totalBaseCap = BigDecimal.ZERO;

        for (String mid : memberIds) {
            Instrument inst = instrumentMap.get(mid);
            if (inst != null && inst.isActiveTrading()) {
                long effectiveShares = Math.max(1L, (long)(inst.getSharesOutstanding() * Math.max(0.05, 1.0 - inst.getLockedFloatRatio())));
                BigDecimal curCap = inst.getCurrentPrice().getAmount().multiply(BigDecimal.valueOf(effectiveShares));
                BigDecimal baseCap = inst.getPreviousClosePrice().getAmount().multiply(BigDecimal.valueOf(effectiveShares));
                totalCurrentCap = totalCurrentCap.add(curCap);
                totalBaseCap = totalBaseCap.add(baseCap);
            }
        }

        if (totalBaseCap.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = totalCurrentCap.divide(totalBaseCap, 6, RoundingMode.HALF_UP);
            this.currentValue = anchor * ratio.doubleValue();
        } else if (this.currentValue <= 0.0) {
            this.currentValue = anchor;
        }
        return currentValue;
    }
}
