package com.tradingglobal3d.core.boss;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entitas Pertarungan Bos Taipan Pasar (Fitur 1 Ekspansi).
 */
public final class MarketBoss implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum DifficultyTier {
        EASY("Taipan Lokal", 5.0, 5, 10_000_000L),
        MEDIUM("Hedge Fund Elit", 12.0, 7, 25_000_000L),
        HARD("Konglomerat Industri", 25.0, 10, 75_000_000L),
        FINAL("Penguasa Pasar Modal", 45.0, 12, 250_000_000L);

        private final String title;
        private final double targetReturn;
        private final int durationSessions;
        private final long rewardCash;

        DifficultyTier(String title, double targetReturn, int durationSessions, long rewardCash) {
            this.title = title;
            this.targetReturn = targetReturn;
            this.durationSessions = durationSessions;
            this.rewardCash = rewardCash;
        }

        public String getTitle() { return title; }
        public double getTargetReturn() { return targetReturn; }
        public int getDurationSessions() { return durationSessions; }
        public long getRewardCash() { return rewardCash; }
    }

    private final String bossId;
    private final String name;
    private final DifficultyTier tier;
    private final String avatarImage;
    private final double targetReturn;
    private final int durationSessions;
    private int elapsedSessions;
    private double bossCurrentReturn;
    private double playerCurrentReturn;
    private boolean active;
    private boolean defeated;
    private final BigDecimal rewardCash;

    public MarketBoss(String bossId, String name, DifficultyTier tier, String avatarImage) {
        this.bossId = Objects.requireNonNull(bossId);
        this.name = Objects.requireNonNull(name);
        this.tier = Objects.requireNonNull(tier);
        this.avatarImage = Objects.requireNonNull(avatarImage);
        this.targetReturn = tier.getTargetReturn();
        this.durationSessions = tier.getDurationSessions();
        this.elapsedSessions = 0;
        this.bossCurrentReturn = 0.0;
        this.playerCurrentReturn = 0.0;
        this.active = false;
        this.defeated = false;
        this.rewardCash = BigDecimal.valueOf(tier.getRewardCash());
    }

    public String getBossId() { return bossId; }
    public String getName() { return name; }
    public DifficultyTier getTier() { return tier; }
    public String getAvatarImage() { return avatarImage; }
    public double getTargetReturn() { return targetReturn; }
    public int getDurationSessions() { return durationSessions; }
    public int getElapsedSessions() { return elapsedSessions; }
    public double getBossCurrentReturn() { return bossCurrentReturn; }
    public double getPlayerCurrentReturn() { return playerCurrentReturn; }
    public boolean isActive() { return active; }
    public boolean isDefeated() { return defeated; }
    public BigDecimal getRewardCash() { return rewardCash; }

    public void startShowdown() {
        this.active = true;
        this.elapsedSessions = 0;
        this.bossCurrentReturn = 0.0;
        this.playerCurrentReturn = 0.0;
    }

    public boolean updateProgress(double playerSessionReturn) {
        if (!active || defeated) return false;
        this.elapsedSessions++;
        this.playerCurrentReturn += playerSessionReturn;
        // Boss bertumbuh mendekati target return secara bertahap
        this.bossCurrentReturn = (targetReturn / durationSessions) * elapsedSessions;

        if (elapsedSessions >= durationSessions) {
            this.active = false;
            if (playerCurrentReturn >= bossCurrentReturn) {
                this.defeated = true;
                return true; // Victory!
            }
        }
        return false;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("bossId", bossId);
        obj.put("name", name);
        obj.put("tier", tier.name());
        obj.put("tierTitle", tier.getTitle());
        obj.put("avatarImage", avatarImage);
        obj.put("targetReturn", String.format(java.util.Locale.ROOT, "%.1f%%", targetReturn));
        obj.put("durationSessions", durationSessions);
        obj.put("elapsedSessions", elapsedSessions);
        obj.put("remainingSessions", Math.max(0, durationSessions - elapsedSessions));
        obj.put("bossCurrentReturn", String.format(java.util.Locale.ROOT, "%+.2f%%", bossCurrentReturn));
        obj.put("playerCurrentReturn", String.format(java.util.Locale.ROOT, "%+.2f%%", playerCurrentReturn));
        obj.put("isActive", active);
        obj.put("isDefeated", defeated);
        obj.put("rewardCash", "Rp " + NumberFormat(rewardCash));
        obj.put("statusLabel", defeated ? "TERKALAHKAN" : (active ? "DUEL BERLANGSUNG" : "SIAP DITANTANG"));
        return obj;
    }

    private static String NumberFormat(BigDecimal bd) {
        return bd.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
