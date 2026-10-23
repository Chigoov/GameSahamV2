package com.tradingglobal3d.core.player;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Layanan Pelacak Jalur Disiplin Harian (Daily Streak Strip) (Fase 3 Fitur 8).
 * Memberikan pelipatgandaan bonus kas modal harian bagi trader yang konsisten
 * dan peringatan Broken Streak jika absen.
 */
public final class DailyStreakService implements Serializable {
    private static final long serialVersionUID = 1L;

    private int currentStreak = 1;
    private int longestStreak = 1;
    private int lastLoginSession = 1;
    private boolean streakBroken = false;

    public synchronized int getCurrentStreak() {
        return currentStreak;
    }

    public synchronized int getLongestStreak() {
        return longestStreak;
    }

    public synchronized boolean isStreakBroken() {
        return streakBroken;
    }

    public synchronized double getRewardMultiplier() {
        if (currentStreak >= 7) return 2.0; // Day 7+: 2.0x
        if (currentStreak >= 5) return 1.5; // Day 5-6: 1.5x
        if (currentStreak >= 3) return 1.25;// Day 3-4: 1.25x
        return 1.0;                         // Day 1-2: 1.0x
    }

    public synchronized void recordSessionLogin(int currentSession) {
        if (currentSession <= lastLoginSession) return;

        if (currentSession == lastLoginSession + 1) {
            // Berurutan disiplin
            currentStreak++;
            if (currentStreak > longestStreak) {
                longestStreak = currentStreak;
            }
            streakBroken = false;
        } else if (currentSession > lastLoginSession + 1) {
            // Absen 1 sesi atau lebih -> Broken Streak!
            currentStreak = 1;
            streakBroken = true;
        }
        lastLoginSession = currentSession;
    }

    public synchronized JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("currentStreak", currentStreak);
        obj.put("longestStreak", longestStreak);
        obj.put("lastLoginSession", lastLoginSession);
        obj.put("isStreakBroken", streakBroken);
        obj.put("rewardMultiplier", String.format(java.util.Locale.ROOT, "%.2fx", getRewardMultiplier()));
        obj.put("statusLabel", streakBroken ? "BROKEN STREAK (RESET)" : ("STREAK HARI KE-" + currentStreak));
        return obj;
    }
}
