package com.tradingglobal3d.core.boss;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Pengelola Pertarungan Bos Taipan Pasar (Fitur 1 Ekspansi).
 */
public final class MarketBossService {
    private final Map<String, MarketBoss> bosses = new LinkedHashMap<>();

    public MarketBossService() {
        initializeBosses();
    }

    private void initializeBosses() {
        bosses.clear();
        bosses.put("BOSS-01", new MarketBoss("BOSS-01", "Taipan Lokal Pemula", MarketBoss.DifficultyTier.EASY, "kolay_patron.jpg"));
        bosses.put("BOSS-02", new MarketBoss("BOSS-02", "Manajer Hedge Fund Elit", MarketBoss.DifficultyTier.MEDIUM, "orta_patron.jpg"));
        bosses.put("BOSS-03", new MarketBoss("BOSS-03", "Konglomerat Multi-Industri", MarketBoss.DifficultyTier.HARD, "zor_patron.png"));
        bosses.put("BOSS-04", new MarketBoss("BOSS-04", "Sang Penguasa Pasar Modal", MarketBoss.DifficultyTier.FINAL, "final_boss.png"));
    }

    public synchronized void reset() {
        initializeBosses();
    }

    public List<MarketBoss> getAllBosses() {
        return new ArrayList<>(bosses.values());
    }

    public MarketBoss getBoss(String bossId) {
        return bosses.get(bossId);
    }

    public synchronized MarketBoss startDuel(String bossId) {
        MarketBoss b = bosses.get(bossId);
        if (b == null) throw new IllegalArgumentException("Bos tidak ditemukan: " + bossId);
        if (b.isDefeated()) {
            throw new IllegalStateException("Bos " + b.getName() + " sudah dikalahkan! Silakan tantang bos pada tingkat kesulitan berikutnya.");
        }
        // Matikan duel lain yang sedang aktif (Bug #301)
        for (MarketBoss other : bosses.values()) {
            if (other.isActive() && !other.getBossId().equals(bossId)) {
                other.setActive(false);
            }
        }
        b.startShowdown();
        return b;
    }

    public synchronized void processSessionUpdate(double playerSessionReturn, Account personalAccount, EconomyLedger ledger, long now) {
        for (MarketBoss b : bosses.values()) {
            if (b.isActive()) {
                boolean won = b.updateProgress(playerSessionReturn);
                if (won && personalAccount != null && ledger != null) {
                    // Kredit hadiah kemenangan duel bos
                    Money reward = new Money(b.getRewardCash(), Currency.IDR);
                    ledger.recordMutation(
                            "BOSS-WIN-" + b.getBossId() + "-" + now,
                            personalAccount,
                            LedgerEntryType.MISSION_REWARD,
                            reward,
                            true,
                            now,
                            "Hadiah Kemenangan Duel Lawan Bos: " + b.getName()
                    );
                }
            }
        }
    }

    public synchronized JSONArray exportBosses() throws JSONException {
        JSONArray arr = new JSONArray();
        for (MarketBoss b : bosses.values()) {
            arr.put(b.toJsonObject());
        }
        return arr;
    }

    public synchronized JSONArray exportState() throws JSONException {
        JSONArray arr = new JSONArray();
        for (MarketBoss b : bosses.values()) {
            arr.put(b.exportState());
        }
        return arr;
    }

    public synchronized void restoreState(JSONArray arr) throws JSONException {
        reset();
        if (arr == null) return;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String id = obj.optString("bossId");
            MarketBoss b = bosses.get(id);
            if (b != null) {
                b.restoreState(obj);
            }
        }
    }
}
