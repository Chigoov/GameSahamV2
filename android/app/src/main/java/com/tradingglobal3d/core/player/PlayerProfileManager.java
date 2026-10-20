package com.tradingglobal3d.core.player;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONArray;

/**
 * Manajer Profil Pemain, Statistik Riil, dan Preferensi Antarmuka Lokal (D16).
 * Menyimpan data identitas, preferensi aksesibilitas, dan rekor performa tanpa membutuhkan login online.
 */
public final class PlayerProfileManager {

    private String playerName = "Trader Nusantara";
    private String avatarMonogram = "TN";
    private int totalTradesExecuted = 0;
    private int profitableTradesCount = 0;
    private int closedTradesCount = 0;
    private int lifetimeClosedTradesCount = 0;
    private final Map<String, String> recordedFills = new LinkedHashMap<>();
    private Money bestTradePnL = Money.zero(Currency.IDR);
    private Money worstTradePnL = Money.zero(Currency.IDR);
    private Money lifetimePeakNetWorth = Money.of(50_000_000L, Currency.IDR);
    private int lifetimeDaysPlayed = 1;

    private int runDaysPlayed = 1;
    private int lifetimeTradesExecuted = 0;
    private int lifetimeProfitableTradesCount = 0;
    private Money lifetimeBestTradePnL = Money.zero(Currency.IDR);
    private Money lifetimeWorstTradePnL = Money.zero(Currency.IDR);
    private final Map<String, JSONObject> prestigeHistory = new LinkedHashMap<>();

    // Preferensi Antarmuka (Aksesibilitas D16)
    private String textSize = "100%";
    private boolean highContrast = false;
    private boolean reducedMotion = false;

    public synchronized void reset() {
        this.playerName = "Trader Nusantara";
        this.avatarMonogram = "TN";
        this.totalTradesExecuted = 0;
        this.profitableTradesCount = 0;
        this.closedTradesCount = 0;
        this.lifetimeClosedTradesCount = 0;
        recordedFills.clear();
        this.bestTradePnL = Money.zero(Currency.IDR);
        this.worstTradePnL = Money.zero(Currency.IDR);
        this.lifetimePeakNetWorth = Money.of(50_000_000L, Currency.IDR);
        this.lifetimeDaysPlayed = 1;
        this.runDaysPlayed = 1;
        this.lifetimeTradesExecuted = 0;
        this.lifetimeProfitableTradesCount = 0;
        this.lifetimeBestTradePnL = Money.zero(Currency.IDR);
        this.lifetimeWorstTradePnL = Money.zero(Currency.IDR);
        prestigeHistory.clear();
    }

    public synchronized int getPrestigeCount() { return prestigeHistory.size(); }
    public synchronized int getRunDaysPlayed() { return runDaysPlayed; }
    public synchronized void retainAcrossPrestige(JSONObject previousProfile) throws JSONException {
        restoreState(Objects.requireNonNull(previousProfile));
        totalTradesExecuted = 0; profitableTradesCount = 0; runDaysPlayed = 1;
        closedTradesCount = 0; recordedFills.clear();
        bestTradePnL = Money.zero(Currency.IDR); worstTradePnL = Money.zero(Currency.IDR);
    }
    public synchronized JSONObject recordPrestige(String requestId, String oldRunId, String newRunId,
            long gameTimestamp, String backupPath) throws JSONException {
        if (requestId == null || !requestId.matches("[A-Za-z0-9_-]{1,160}")
                || oldRunId == null || oldRunId.trim().isEmpty() || newRunId == null || newRunId.trim().isEmpty()
                || oldRunId.equals(newRunId) || backupPath == null || backupPath.trim().isEmpty())
            throw new IllegalArgumentException("Identitas prestige/backup tidak valid");
        String payload = new JSONArray().put(oldRunId).put(newRunId).put(gameTimestamp).put(backupPath).toString();
        JSONObject existing = prestigeHistory.get(requestId);
        if (existing != null) {
            if (!payload.equals(existing.getString("payload"))) throw new IllegalArgumentException("ID prestige sudah dipakai untuk input berbeda");
            return new JSONObject(existing.toString());
        }
        for (JSONObject receipt : prestigeHistory.values())
            if (oldRunId.equals(receipt.getString("oldRunId")) || newRunId.equals(receipt.getString("newRunId")))
                throw new IllegalStateException("Run sudah memiliki transaksi prestige");
        JSONObject receipt = new JSONObject().put("success", true).put("requestId", requestId)
                .put("oldRunId", oldRunId).put("newRunId", newRunId).put("gameTimestamp", gameTimestamp)
                .put("backupPath", backupPath).put("payload", payload).put("prestigeNumber", prestigeHistory.size() + 1)
                .put("bonus", "BADGE_ERA_BARU").put("status", "COMPLETED");
        prestigeHistory.put(requestId, new JSONObject(receipt.toString()));
        return receipt;
    }
    private JSONArray exportPrestigeHistory() {
        JSONArray history = new JSONArray();
        for (JSONObject receipt : prestigeHistory.values()) history.put(receipt);
        return history;
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
        if (name == null || name.trim().isEmpty() || name.codePointCount(0, name.length()) > 48
                || name.codePoints().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Nama harus 1–48 karakter tanpa karakter kontrol");
        {
            this.playerName = name.trim();
            String[] parts = this.playerName.split("\\s+");
            if (parts.length >= 2) {
                this.avatarMonogram = (initial(parts[0]) + initial(parts[1])).toUpperCase(java.util.Locale.ROOT);
            } else {
                int count = Math.min(2, this.playerName.codePointCount(0, this.playerName.length()));
                this.avatarMonogram = this.playerName.substring(0, this.playerName.offsetByCodePoints(0, count)).toUpperCase(java.util.Locale.ROOT);
            }
        }
    }

    private static String initial(String value) { return value.substring(0, value.offsetByCodePoints(0, 1)); }

    public synchronized void updateNetWorthPeak(Money currentNetWorth) {
        if (currentNetWorth != null && currentNetWorth.compareTo(lifetimePeakNetWorth) > 0) {
            this.lifetimePeakNetWorth = currentNetWorth;
        }
    }

    public synchronized void recordTradeResult(Money realizedPnL) {
        if (realizedPnL != null && realizedPnL.getCurrency() != Currency.IDR)
            throw new IllegalArgumentException("P/L statistik harus dikonversi ke IDR");
        this.totalTradesExecuted++;
        this.lifetimeTradesExecuted++;
        if (realizedPnL != null) {
            if (closedTradesCount++ == 0) bestTradePnL = worstTradePnL = realizedPnL;
            if (lifetimeClosedTradesCount++ == 0) lifetimeBestTradePnL = lifetimeWorstTradePnL = realizedPnL;
            if (realizedPnL.isPositive()) lifetimeProfitableTradesCount++;
            if (realizedPnL.compareTo(lifetimeBestTradePnL) > 0) lifetimeBestTradePnL = realizedPnL;
            if (realizedPnL.compareTo(lifetimeWorstTradePnL) < 0) lifetimeWorstTradePnL = realizedPnL;
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

    public synchronized void recordFillResult(String fillId, Money realizedNetPnL) {
        if (fillId == null || fillId.trim().isEmpty() || fillId.length() > 200)
            throw new IllegalArgumentException("ID fill tidak valid");
        String value = realizedNetPnL == null ? "OPEN" : realizedNetPnL.getCurrency().name() + ":" + realizedNetPnL.getAmount();
        if (recordedFills.containsKey(fillId)) {
            if (!value.equals(recordedFills.get(fillId))) throw new IllegalArgumentException("ID fill sudah dipakai dengan hasil berbeda");
            return;
        }
        recordTradeResult(realizedNetPnL);
        recordedFills.put(fillId, value);
    }

    public synchronized void incrementDaysPlayed() {
        this.lifetimeDaysPlayed++;
        this.runDaysPlayed++;
    }

    public synchronized void setPreferences(String textSize, boolean highContrast, boolean reducedMotion) {
        if (textSize != null && !textSize.equals("100%") && !textSize.equals("200%"))
            throw new IllegalArgumentException("Ukuran teks yang tersedia 100% atau 200%");
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
        double winRate = closedTradesCount > 0 ? ((double) profitableTradesCount / closedTradesCount) * 100.0 : 0.0;
        obj.put("closedTrades", closedTradesCount);
        obj.put("winRateAvailable", closedTradesCount > 0);
        obj.put("winRateDenominator", "CLOSED_TRADES");
        obj.put("winRate", closedTradesCount > 0 ? String.format(java.util.Locale.ROOT, "%.1f%%", winRate) : "Belum tersedia");
        obj.put("tradeExtremaAvailable", closedTradesCount > 0);
        obj.put("bestTradePnL", bestTradePnL.format());
        obj.put("worstTradePnL", worstTradePnL.format());
        obj.put("lifetimePeakNetWorth", lifetimePeakNetWorth.format());
        obj.put("lifetimeDaysPlayed", lifetimeDaysPlayed);
        obj.put("runDaysPlayed", runDaysPlayed);
        obj.put("lifetimeTradesExecuted", lifetimeTradesExecuted);
        obj.put("lifetimeProfitableTradesCount", lifetimeProfitableTradesCount);
        obj.put("lifetimeBestTradePnL", lifetimeBestTradePnL.getAmount().toPlainString());
        obj.put("lifetimeWorstTradePnL", lifetimeWorstTradePnL.getAmount().toPlainString());
        obj.put("prestigeCount", prestigeHistory.size());
        obj.put("prestigeHistory", new JSONArray(exportPrestigeHistory().toString()));

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
        obj.put("closedTradesCount", closedTradesCount);
        obj.put("lifetimeClosedTradesCount", lifetimeClosedTradesCount);
        obj.put("recordedFills", new JSONObject(recordedFills));
        obj.put("bestTradePnL", bestTradePnL.getAmount().toPlainString());
        obj.put("worstTradePnL", worstTradePnL.getAmount().toPlainString());
        obj.put("lifetimePeakNetWorth", lifetimePeakNetWorth.getAmount().toPlainString());
        obj.put("lifetimeDaysPlayed", lifetimeDaysPlayed);
        obj.put("runDaysPlayed", runDaysPlayed);
        obj.put("lifetimeTradesExecuted", lifetimeTradesExecuted);
        obj.put("lifetimeProfitableTradesCount", lifetimeProfitableTradesCount);
        obj.put("lifetimeBestTradePnL", lifetimeBestTradePnL.getAmount().toPlainString());
        obj.put("lifetimeWorstTradePnL", lifetimeWorstTradePnL.getAmount().toPlainString());
        obj.put("prestigeCount", prestigeHistory.size());
        obj.put("prestigeHistory", new JSONArray(exportPrestigeHistory().toString()));
        obj.put("textSize", textSize);
        obj.put("highContrast", highContrast);
        obj.put("reducedMotion", reducedMotion);
        return obj;
    }

    public synchronized void restoreState(JSONObject obj) throws JSONException {
        if (obj == null) return;
        PlayerProfileManager restored = new PlayerProfileManager();
        restored.setPlayerName(obj.optString("playerName", "Trader Nusantara"));
        restored.totalTradesExecuted = obj.optInt("totalTradesExecuted", 0);
        restored.profitableTradesCount = obj.optInt("profitableTradesCount", 0);
        restored.closedTradesCount = obj.optInt("closedTradesCount", restored.totalTradesExecuted);
        restored.bestTradePnL = new Money(new BigDecimal(obj.optString("bestTradePnL", "0")), Currency.IDR);
        restored.worstTradePnL = new Money(new BigDecimal(obj.optString("worstTradePnL", "0")), Currency.IDR);
        restored.lifetimePeakNetWorth = new Money(new BigDecimal(obj.optString("lifetimePeakNetWorth", "50000000")), Currency.IDR);
        restored.lifetimeDaysPlayed = obj.optInt("lifetimeDaysPlayed", 1);
        restored.setPreferences(obj.optString("textSize", "100%"), obj.optBoolean("highContrast", false), obj.optBoolean("reducedMotion", false));
        restored.runDaysPlayed = obj.optInt("runDaysPlayed", restored.lifetimeDaysPlayed);
        restored.lifetimeTradesExecuted = obj.optInt("lifetimeTradesExecuted", restored.totalTradesExecuted);
        restored.lifetimeClosedTradesCount = obj.optInt("lifetimeClosedTradesCount", restored.lifetimeTradesExecuted);
        restored.lifetimeProfitableTradesCount = obj.optInt("lifetimeProfitableTradesCount", restored.profitableTradesCount);
        restored.lifetimeBestTradePnL = new Money(new BigDecimal(obj.optString("lifetimeBestTradePnL", restored.bestTradePnL.getAmount().toPlainString())), Currency.IDR);
        restored.lifetimeWorstTradePnL = new Money(new BigDecimal(obj.optString("lifetimeWorstTradePnL", restored.worstTradePnL.getAmount().toPlainString())), Currency.IDR);
        if (restored.totalTradesExecuted < 0 || restored.profitableTradesCount < 0 || restored.profitableTradesCount > restored.totalTradesExecuted
                || restored.runDaysPlayed < 1 || restored.lifetimeDaysPlayed < restored.runDaysPlayed
                || restored.lifetimeTradesExecuted < restored.totalTradesExecuted || restored.lifetimeProfitableTradesCount < restored.profitableTradesCount
                || restored.lifetimeProfitableTradesCount > restored.lifetimeTradesExecuted
                || restored.closedTradesCount < restored.profitableTradesCount || restored.closedTradesCount > restored.totalTradesExecuted
                || restored.lifetimeClosedTradesCount < restored.lifetimeProfitableTradesCount || restored.lifetimeClosedTradesCount > restored.lifetimeTradesExecuted
                || restored.lifetimeClosedTradesCount < restored.closedTradesCount)
            throw new IllegalArgumentException("Statistik profil tidak valid");
        JSONObject fills = obj.optJSONObject("recordedFills");
        if (fills != null) {
            java.util.Iterator<String> keys = fills.keys();
            while (keys.hasNext()) {
                String id = keys.next();
                String value = fills.getString(id);
                if (id.trim().isEmpty() || id.length() > 200 || !(value.equals("OPEN") || value.matches("IDR:-?[0-9]+")))
                    throw new IllegalArgumentException("Riwayat fill profil tidak valid");
                restored.recordedFills.put(id, value);
            }
        }
        JSONArray history = obj.optJSONArray("prestigeHistory");
        for (int i = 0; history != null && i < history.length(); i++) {
            JSONObject saved = history.getJSONObject(i);
            JSONObject checked = restored.recordPrestige(saved.getString("requestId"), saved.getString("oldRunId"), saved.getString("newRunId"),
                    saved.getLong("gameTimestamp"), saved.getString("backupPath"));
            if (!saved.getBoolean("success") || !"COMPLETED".equals(saved.getString("status"))
                    || !checked.getString("payload").equals(saved.getString("payload")) || saved.getInt("prestigeNumber") != i + 1)
                throw new IllegalArgumentException("Riwayat prestige tidak valid");
        }
        if (obj.optInt("prestigeCount", restored.prestigeHistory.size()) != restored.prestigeHistory.size())
            throw new IllegalArgumentException("Jumlah prestige tidak sesuai riwayat");
        this.playerName = restored.playerName;
        this.avatarMonogram = restored.avatarMonogram;
        this.totalTradesExecuted = restored.totalTradesExecuted;
        this.profitableTradesCount = restored.profitableTradesCount;
        this.closedTradesCount = restored.closedTradesCount;
        this.lifetimeClosedTradesCount = restored.lifetimeClosedTradesCount;
        recordedFills.clear(); recordedFills.putAll(restored.recordedFills);
        this.bestTradePnL = restored.bestTradePnL;
        this.worstTradePnL = restored.worstTradePnL;
        this.lifetimePeakNetWorth = restored.lifetimePeakNetWorth;
        this.lifetimeDaysPlayed = restored.lifetimeDaysPlayed;
        this.textSize = restored.textSize;
        this.highContrast = restored.highContrast;
        this.reducedMotion = restored.reducedMotion;
        this.runDaysPlayed = restored.runDaysPlayed;
        this.lifetimeTradesExecuted = restored.lifetimeTradesExecuted;
        this.lifetimeProfitableTradesCount = restored.lifetimeProfitableTradesCount;
        this.lifetimeBestTradePnL = restored.lifetimeBestTradePnL;
        this.lifetimeWorstTradePnL = restored.lifetimeWorstTradePnL;
        prestigeHistory.clear(); prestigeHistory.putAll(restored.prestigeHistory);
    }
}
