package com.tradingglobal3d.core.career;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Misi Periodik (Harian / Mingguan) sesuai D07.
 * - Harian reset setiap hari pukul 00:00 WIB waktu game.
 * - Mingguan reset setiap hari Senin pukul 00:00 WIB waktu game.
 * - Reward terdiri dari XP profil dan bonus kas game terukur (earned reward).
 */
public final class PeriodicMission {

    public enum MissionPeriod {
        DAILY("Harian"),
        WEEKLY("Mingguan");

        private final String label;
        MissionPeriod(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final String id;
    private final String title;
    private final String description;
    private final MissionPeriod period;
    private final int targetCount;
    private int currentCount;
    private final int xpReward;
    private final Money cashReward;
    private boolean completed;
    private boolean claimed;
    private final String periodId; // e.g. "2026-10-19" atau "2026-W42"

    public PeriodicMission(String id, String title, String description, MissionPeriod period,
                           int targetCount, int xpReward, Money cashReward, String periodId) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.period = Objects.requireNonNull(period);
        this.targetCount = targetCount;
        this.currentCount = 0;
        this.xpReward = xpReward;
        this.cashReward = cashReward != null ? cashReward : Money.zero(Currency.IDR);
        this.completed = false;
        this.claimed = false;
        this.periodId = periodId;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public MissionPeriod getPeriod() { return period; }
    public int getTargetCount() { return targetCount; }
    public int getCurrentCount() { return currentCount; }
    public int getXpReward() { return xpReward; }
    public Money getCashReward() { return cashReward; }
    public boolean isCompleted() { return completed; }
    public boolean isClaimed() { return claimed; }
    public String getPeriodId() { return periodId; }

    public synchronized void recordProgress(int count) {
        if (completed) return;
        this.currentCount = Math.min(this.targetCount, this.currentCount + count);
        if (this.currentCount >= this.targetCount) {
            this.completed = true;
        }
    }

    public synchronized JSONObject claim(Account account, EconomyLedger ledger, long now) throws JSONException {
        if (!completed) throw new IllegalStateException("Misi belum selesai");
        if (claimed) throw new IllegalStateException("Hadiah misi sudah pernah diklaim");

        this.claimed = true;
        if (!cashReward.isZero() && account != null && ledger != null) {
            account.creditCash(cashReward);
            ledger.recordMutation("MISSION-REWARD-" + id + "-" + now, account, LedgerEntryType.MISSION_REWARD, cashReward, true, now, "Hadiah misi " + title);
        }

        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("missionId", id);
        res.put("xpReward", xpReward);
        res.put("cashReward", cashReward.format());
        res.put("message", "Hadiah misi " + title + " berhasil diklaim!");
        return res;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("title", title);
        obj.put("description", description);
        obj.put("period", period.name());
        obj.put("periodLabel", period.getLabel());
        obj.put("progress", currentCount + " / " + targetCount);
        obj.put("currentCount", currentCount);
        obj.put("targetCount", targetCount);
        obj.put("xpReward", xpReward + " XP");
        obj.put("xpValue", xpReward);
        obj.put("cashReward", cashReward.format());
        obj.put("completed", completed);
        obj.put("claimed", claimed);
        obj.put("periodId", periodId);
        return obj;
    }

    public JSONObject exportState() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("title", title);
        obj.put("description", description);
        obj.put("period", period.name());
        obj.put("targetCount", targetCount);
        obj.put("currentCount", currentCount);
        obj.put("xpReward", xpReward);
        obj.put("cashReward", cashReward.getAmount().toPlainString());
        obj.put("completed", completed);
        obj.put("claimed", claimed);
        obj.put("periodId", periodId);
        return obj;
    }

    public static PeriodicMission restoreFromState(JSONObject obj) {
        if (obj == null) return null;
        String id = obj.optString("id");
        String title = obj.optString("title");
        String desc = obj.optString("description");
        MissionPeriod period = MissionPeriod.valueOf(obj.optString("period", "DAILY"));
        int target = obj.optInt("targetCount", 1);
        int xp = obj.optInt("xpReward", 50);
        Money cash = new Money(new java.math.BigDecimal(obj.optString("cashReward", "0")), Currency.IDR);
        String pId = obj.optString("periodId", "");

        PeriodicMission m = new PeriodicMission(id, title, desc, period, target, xp, cash, pId);
        m.currentCount = obj.optInt("currentCount", 0);
        m.completed = obj.optBoolean("completed", false);
        m.claimed = obj.optBoolean("claimed", false);
        return m;
    }
}
