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
        // Matikan duel lain yang sedang aktif
        for (MarketBoss other : bosses.values()) {
            if (other.isActive() && !other.getBossId().equals(bossId)) {
                // reset active status
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
                            "BOSS-WIN-" + b.getBossId() + "-" + System.currentTimeMillis(),
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
}
