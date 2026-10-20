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
    private JSONObject claimReceipt;
    private final String activityType;
    private final String periodId; // e.g. "2026-10-19" atau "2026-W42"

    public PeriodicMission(String id, String title, String description, MissionPeriod period,
                           int targetCount, int xpReward, Money cashReward, String periodId) {
        this(id, title, description, period, targetCount, xpReward, cashReward, periodId, legacyActivity(id));
    }

    public PeriodicMission(String id, String title, String description, MissionPeriod period,
                           int targetCount, int xpReward, Money cashReward, String periodId, String activityType) {
        if (id == null || id.trim().isEmpty() || periodId == null || periodId.trim().isEmpty()
                || targetCount <= 0 || xpReward < 0 || activityType == null || activityType.trim().isEmpty())
            throw new IllegalArgumentException("Definisi misi tidak valid");
        this.activityType = activityType;
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.period = Objects.requireNonNull(period);
        this.targetCount = targetCount;
        this.currentCount = 0;
        this.xpReward = xpReward;
        this.cashReward = cashReward != null ? cashReward : Money.zero(Currency.IDR);
        if (this.cashReward.getCurrency() != Currency.IDR || this.cashReward.isNegative())
            throw new IllegalArgumentException("Reward wajib IDR nonnegatif");
        this.completed = false;
        this.claimed = false;
        this.periodId = periodId;
    }

    private static String legacyActivity(String id) {
        if (id == null) return "UNSUPPORTED";
        if (id.startsWith("DM-READ")) return "READ_NEWS";
        if (id.startsWith("DM-TRADE") || id.startsWith("WM-VOL")) return "TRADE";
        if (id.startsWith("DM-HEALTH")) return "HEALTH_CHECK";
        if (id.startsWith("WM-DAYS")) return "DAY_SETTLED";
        if (id.startsWith("WM-DIV")) return "INCOME_REALIZED";
        return "UNSUPPORTED";
    }
    public String getActivityType() { return activityType; }
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
        if (count <= 0) throw new IllegalArgumentException("Progres harus positif");
        if (completed) return;
        this.currentCount = (int) Math.min((long) this.targetCount, (long) this.currentCount + count);
        if (this.currentCount >= this.targetCount) {
            this.completed = true;
        }
    }

    public synchronized JSONObject claim(Account account, EconomyLedger ledger, long now) throws JSONException {
        if (!completed) throw new IllegalStateException("Misi belum selesai");
        Objects.requireNonNull(account, "Akun reward wajib tersedia");
        Objects.requireNonNull(ledger, "Ledger reward wajib tersedia");
        if (account.getType() != com.tradingglobal3d.core.model.AccountType.PERSONAL)
            throw new IllegalArgumentException("Reward misi hanya untuk akun pribadi");
        if (claimReceipt != null) {
            if (!account.getId().equals(claimReceipt.getString("accountId")))
                throw new IllegalArgumentException("Receipt hadiah milik akun berbeda");
            return new JSONObject(claimReceipt.toString());
        }
        JSONObject res = new JSONObject().put("success", true).put("missionId", id)
                .put("receiptId", "MISSION:" + id).put("accountId", account.getId())
                .put("xpReward", xpReward).put("cashReward", cashReward.format())
                .put("message", "Hadiah misi " + title + " berhasil diklaim!");
        // Old claimed saves already paid cash; attach a receipt without issuing another reward.
        if (claimed) { claimReceipt = new JSONObject(res.toString()); return res; }
        if (!cashReward.isZero()) {
            // ponytail: the ledger owns cash, with a stable mission ID across retries.
            ledger.recordMutation("MISSION-REWARD-" + id, account, LedgerEntryType.MISSION_REWARD, cashReward, true, now, "Hadiah misi " + title);
        }
        this.claimed = true;
        claimReceipt = new JSONObject(res.toString());
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
        obj.put("activityType", activityType);
        if (claimReceipt != null) obj.put("claimReceipt", new JSONObject(claimReceipt.toString()));
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
        obj.put("activityType", activityType);
        if (claimReceipt != null) obj.put("claimReceipt", new JSONObject(claimReceipt.toString()));
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

        PeriodicMission m = new PeriodicMission(id, title, desc, period, target, xp, cash, pId, obj.optString("activityType", legacyActivity(id)));
        m.currentCount = obj.optInt("currentCount", 0);
        m.completed = obj.optBoolean("completed", false);
        m.claimed = obj.optBoolean("claimed", false);
        if (m.currentCount < 0 || m.currentCount > target || m.completed != (m.currentCount == target) || (m.claimed && !m.completed))
            throw new IllegalArgumentException("Progres/klaim misi tidak valid");
        JSONObject receipt = obj.optJSONObject("claimReceipt");
        if (receipt != null) {
            if (!m.claimed || !id.equals(receipt.optString("missionId")) || !receipt.optBoolean("success")
                    || receipt.optString("accountId").trim().isEmpty() || receipt.optInt("xpReward", -1) != xp
                    || !cash.format().equals(receipt.optString("cashReward")))
                throw new IllegalArgumentException("Receipt misi tidak valid");
            try { m.claimReceipt = new JSONObject(receipt.toString()); }
            catch (JSONException invalid) { throw new IllegalArgumentException("Receipt misi tidak valid", invalid); }
        }
        return m;
    }
}
