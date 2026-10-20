package com.tradingglobal3d.core.player;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Manajer Profil Pemain, Statistik Riil, dan Preferensi Antarmuka Lokal (D16).
 * Menyimpan data identitas, preferensi aksesibilitas, dan rekor performa tanpa membutuhkan login online.
 */
public final class PlayerProfileManager {

    private String playerName = "Trader Nusantara";
    private String avatarMonogram = "TN";
    private int totalTradesExecuted = 0;
    private int profitableTradesCount = 0;
    private Money bestTradePnL = Money.zero(Currency.IDR);
    private Money worstTradePnL = Money.zero(Currency.IDR);
    private Money lifetimePeakNetWorth = Money.of(50_000_000L, Currency.IDR);
    private int lifetimeDaysPlayed = 1;

    // Preferensi Antarmuka (Aksesibilitas D16)
    private String textSize = "100%";
    private boolean highContrast = false;
    private boolean reducedMotion = false;

    public synchronized void reset() {
        this.playerName = "Trader Nusantara";
        this.avatarMonogram = "TN";
        this.totalTradesExecuted = 0;
        this.profitableTradesCount = 0;
        this.bestTradePnL = Money.zero(Currency.IDR);
        this.worstTradePnL = Money.zero(Currency.IDR);
        this.lifetimePeakNetWorth = Money.of(50_000_000L, Currency.IDR);
        this.lifetimeDaysPlayed = 1;
    }

    public synchronized String getPlayerName() { return playerName; }
    public synchronized String getAvatarMonogram() { return avatarMonogram; }
    public synchronized int getTotalTradesExecuted() { return totalTradesExecuted; }
    public synchronized int getProfitableTradesCount() { return profitableTradesCount; }
    public synchronized Money getBestTradePnL() { return bestTradePnL; }
    public synchronized Money getWorstTradePnL() { return worstTradePnL; }
    public synchronized Money getLifetimePeakNetWorth() { return lifetimePeakNetWorth; }
    public synchronized int getLifetimeDaysPlayed() { return lifetimeDaysPlayed; }

    public synchronized void setPlayerName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.playerName = name.trim();
            String[] parts = this.playerName.split("\\s+");
            if (parts.length >= 2) {
                this.avatarMonogram = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            } else {
                this.avatarMonogram = ("" + this.playerName.charAt(0) + (this.playerName.length() > 1 ? this.playerName.charAt(1) : "")).toUpperCase();
            }
        }
    }

    public synchronized void updateNetWorthPeak(Money currentNetWorth) {
        if (currentNetWorth != null && currentNetWorth.compareTo(lifetimePeakNetWorth) > 0) {
            this.lifetimePeakNetWorth = currentNetWorth;
        }
    }

    public synchronized void recordTradeResult(Money realizedPnL) {
        this.totalTradesExecuted++;
        if (realizedPnL != null) {
            if (realizedPnL.isPositive()) {
                this.profitableTradesCount++;
            }
            if (realizedPnL.compareTo(bestTradePnL) > 0) {
                this.bestTradePnL = realizedPnL;
            }
            if (realizedPnL.compareTo(worstTradePnL) < 0) {
                this.worstTradePnL = realizedPnL;
            }
        }
    }

    public synchronized void incrementDaysPlayed() {
        this.lifetimeDaysPlayed++;
    }

    public synchronized void setPreferences(String textSize, boolean highContrast, boolean reducedMotion) {
        if (textSize != null) this.textSize = textSize;
        this.highContrast = highContrast;
        this.reducedMotion = reducedMotion;
    }

    public synchronized JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("playerName", playerName);
        obj.put("avatarMonogram", avatarMonogram);
        obj.put("totalTrades", totalTradesExecuted);
        obj.put("profitableTrades", profitableTradesCount);
        double winRate = totalTradesExecuted > 0 ? ((double) profitableTradesCount / totalTradesExecuted) * 100.0 : 0.0;
        obj.put("winRate", String.format(java.util.Locale.ROOT, "%.1f%%", winRate));
        obj.put("bestTradePnL", bestTradePnL.format());
        obj.put("worstTradePnL", worstTradePnL.format());
        obj.put("lifetimePeakNetWorth", lifetimePeakNetWorth.format());
        obj.put("lifetimeDaysPlayed", lifetimeDaysPlayed);

        JSONObject prefs = new JSONObject();
        prefs.put("textSize", textSize);
        prefs.put("highContrast", highContrast);
        prefs.put("reducedMotion", reducedMotion);
        obj.put("preferences", prefs);
        return obj;
    }

    public synchronized JSONObject exportState() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("playerName", playerName);
        obj.put("avatarMonogram", avatarMonogram);
        obj.put("totalTradesExecuted", totalTradesExecuted);
        obj.put("profitableTradesCount", profitableTradesCount);
        obj.put("bestTradePnL", bestTradePnL.getAmount().toPlainString());
        obj.put("worstTradePnL", worstTradePnL.getAmount().toPlainString());
        obj.put("lifetimePeakNetWorth", lifetimePeakNetWorth.getAmount().toPlainString());
        obj.put("lifetimeDaysPlayed", lifetimeDaysPlayed);
        obj.put("textSize", textSize);
        obj.put("highContrast", highContrast);
        obj.put("reducedMotion", reducedMotion);
        return obj;
    }

    public synchronized void restoreState(JSONObject obj) throws JSONException {
        if (obj == null) return;
        this.playerName = obj.optString("playerName", "Trader Nusantara");
        this.avatarMonogram = obj.optString("avatarMonogram", "TN");
        this.totalTradesExecuted = obj.optInt("totalTradesExecuted", 0);
        this.profitableTradesCount = obj.optInt("profitableTradesCount", 0);
        this.bestTradePnL = new Money(new BigDecimal(obj.optString("bestTradePnL", "0")), Currency.IDR);
        this.worstTradePnL = new Money(new BigDecimal(obj.optString("worstTradePnL", "0")), Currency.IDR);
        this.lifetimePeakNetWorth = new Money(new BigDecimal(obj.optString("lifetimePeakNetWorth", "50000000")), Currency.IDR);
        this.lifetimeDaysPlayed = obj.optInt("lifetimeDaysPlayed", 1);
        this.textSize = obj.optString("textSize", "100%");
        this.highContrast = obj.optBoolean("highContrast", false);
        this.reducedMotion = obj.optBoolean("reducedMotion", false);
    }
}
