package com.tradingglobal3d.core.league;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.*;

/**
 * Manajer Liga Periodik Mingguan, Rivalitas NPC, dan Peringkat Offline (D09, D13).
 * - 3 Tier Liga: BRONZE (Perunggu), SILVER (Perak), GOLD (Emas Konglomerat).
 * - Roster 20 NPC fiktif persisten dengan karakteristik dan modal beragam.
 * - Rival personal persisten dipilih setiap minggu dari pesaing terdekat.
 */
public final class LeagueManager {

    public enum LeagueTier {
        BRONZE("Divisi Perunggu", "Tingkat Awal"),
        SILVER("Divisi Perak", "Tingkat Profesional"),
        GOLD("Divisi Emas", "Liga Konglomerat Utama");

        private final String label;
        private final String description;

        LeagueTier(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() { return label; }
        public String getDescription() { return description; }
    }

    private LeagueTier currentTier = LeagueTier.BRONZE;
    private final Map<String, LeagueCompetitor> npcCompetitors = new LinkedHashMap<>();
    private String currentRivalId = "NPC-RIV-01";
    private int seasonWeekNumber = 1;

    public LeagueManager() {
        initializeNpcRoster();
    }

    public synchronized void reset() {
        currentTier = LeagueTier.BRONZE;
        seasonWeekNumber = 1;
        initializeNpcRoster();
    }

    private void initializeNpcRoster() {
        npcCompetitors.clear();
        // 20 Konglomerat dan Trader NPC Fiktif (D09)
        addNpc("NPC-01", "Prajogo Pangestu", "Taipan Energi & Petrokimia", "AGRESIF", 550_000_000_000L, 4.25);
        addNpc("NPC-02", "Low Tuck Kwong", "Raja Pertambangan Batubara", "SEIMBANG", 380_000_000_000L, 3.80);
        addNpc("NPC-03", "Robert Budi Hartono", "Pemilik Konglomerasi Finansial", "KONSERVATIF", 350_000_000_000L, 2.10);
        addNpc("NPC-04", "Michael Bambang Hartono", "Taipan Perbankan & Konsumer", "KONSERVATIF", 330_000_000_000L, 2.05);
        addNpc("NPC-05", "Sri Prakash Lohia", "Pengusaha Manufaktur Global", "SEIMBANG", 125_000_000_000L, 3.15);
        addNpc("NPC-06", "Chairul Tanjung", "Raja Media & Perbankan", "SEIMBANG", 80_000_000_000L, 2.90);
        addNpc("NPC-07", "Tahir Perkasa", "Taipan Finansial & Properti", "SEIMBANG", 65_000_000_000L, 2.45);
        addNpc("NPC-08", "Lim Hariyanto", "Pemilik Perkebunan Sawit", "KONSERVATIF", 45_000_000_000L, 1.85);
        addNpc("NPC-09", "Djoko Susanto", "Penguasa Ritel & Minimarket", "KONSERVATIF", 35_000_000_000L, 1.95);
        addNpc("NPC-10", "Theodore Rachmat", "Konglomerat Manufaktur Otomotif", "SEIMBANG", 30_000_000_000L, 2.60);
        addNpc("NPC-11", "Martua Sitorus", "Pengusaha Agrikultur Raksasa", "SEIMBANG", 25_000_000_000L, 2.30);
        addNpc("NPC-12", "Sukanto Tanoto", "Pemilik Industri Kertas & Serat", "AGRESIF", 20_000_000_000L, 3.50);
        addNpc("NPC-13", "Eddy Kusnadi Sariaatmadja", "Raja Teknologi & Media Digital", "AGRESIF", 15_000_000_000L, 4.10);
        addNpc("NPC-14", "Ciputra Nusantara", "Pelopor Real Estate Terpadu", "KONSERVATIF", 10_000_000_000L, 1.70);
        addNpc("NPC-15", "Mochtar Riady", "Taipan Jasa Finansial & Medis", "SEIMBANG", 8_000_000_000L, 2.20);
        addNpc("NPC-16", "Hary Tanoesoedibjo", "Konglomerat Media Siaran", "AGRESIF", 5_000_000_000L, 3.05);
        addNpc("NPC-17", "Hashim Djojohadikusumo", "Pengusaha Investasi & Sumber Daya", "SEIMBANG", 3_000_000_000L, 2.15);
        addNpc("NPC-18", "Bambang Sutantio", "Raja Produk Susu & Olahan", "KONSERVATIF", 1_000_000_000L, 1.65);
        addNpc("NPC-19", "Arifin Panigoro Junior", "Eksplorasi Migas Mandiri", "AGRESIF", 500_000_000L, 4.80);
        addNpc("NPC-RIV-01", "Hendra Sukanto", "Trader Ambisius Rival", "AGRESIF", 45_000_000L, 2.75);

        this.currentRivalId = "NPC-RIV-01";
    }

    private void addNpc(String id, String name, String title, String style, long netWorthIdr, double weeklyReturn) {
        npcCompetitors.put(id, new LeagueCompetitor(id, name, title, style, Money.of(netWorthIdr, Currency.IDR), weeklyReturn, false));
    }

    public synchronized LeagueTier getCurrentTier() {
        return currentTier;
    }

    public synchronized int getSeasonWeekNumber() {
        return seasonWeekNumber;
    }

    /**
     * Memperoleh daftar leaderboard berdasarkan Kekayaan Bersih (Total Net Worth).
     */
    public synchronized JSONObject getLeaderboardByNetWorth(Money playerNetWorth, String playerName, String playerTitle) throws JSONException {
        List<LeagueCompetitor> all = new ArrayList<>(npcCompetitors.values());
        LeagueCompetitor playerComp = new LeagueCompetitor(
                "PLAYER", playerName != null ? playerName + " (Kamu)" : "Pemain (Kamu)",
                playerTitle != null ? playerTitle : "Trader", "DINAMIS", playerNetWorth, 0.0, true
        );
        all.add(playerComp);

        all.sort((a, b) -> b.getNetWorth().getAmount().compareTo(a.getNetWorth().getAmount()));

        int playerRank = 1;
        JSONArray entries = new JSONArray();
        for (int i = 0; i < all.size(); i++) {
            LeagueCompetitor c = all.get(i);
            int rank = i + 1;
            if (c.isPlayer()) playerRank = rank;
            entries.put(c.toJsonObject(rank));
        }

        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("mode", "NET_WORTH");
        res.put("currentTier", currentTier.name());
        res.put("tierLabel", currentTier.getLabel());
        res.put("seasonWeek", seasonWeekNumber);
        res.put("playerRank", playerRank);
        res.put("totalCompetitors", all.size());
        res.put("leaderboard", entries);
        return res;
    }

    /**
     * Memperoleh daftar leaderboard berdasarkan Return Mingguan Liga (Weekly Return %).
     */
    public synchronized JSONObject getLeaderboardByWeeklyReturn(double playerReturnPercent, Money playerNetWorth, String playerName, String playerTitle) throws JSONException {
        List<LeagueCompetitor> all = new ArrayList<>(npcCompetitors.values());
        LeagueCompetitor playerComp = new LeagueCompetitor(
                "PLAYER", playerName != null ? playerName + " (Kamu)" : "Pemain (Kamu)",
                playerTitle != null ? playerTitle : "Trader", "DINAMIS", playerNetWorth, playerReturnPercent, true
        );
        all.add(playerComp);

        all.sort((a, b) -> Double.compare(b.getWeeklyReturnPercent(), a.getWeeklyReturnPercent()));

        int playerRank = 1;
        JSONArray entries = new JSONArray();
        for (int i = 0; i < all.size(); i++) {
            LeagueCompetitor c = all.get(i);
            int rank = i + 1;
            if (c.isPlayer()) playerRank = rank;
            entries.put(c.toJsonObject(rank));
        }

        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("mode", "WEEKLY_RETURN");
        res.put("currentTier", currentTier.name());
        res.put("tierLabel", currentTier.getLabel());
        res.put("seasonWeek", seasonWeekNumber);
        res.put("playerRank", playerRank);
        res.put("totalCompetitors", all.size());
        res.put("leaderboard", entries);
        return res;
    }

    /**
     * Memperoleh status rival persisten minggu ini.
     */
    public synchronized JSONObject getRivalStatus(Money playerNetWorth, double playerReturnPercent) throws JSONException {
        LeagueCompetitor rival = npcCompetitors.get(currentRivalId);
        if (rival == null) {
            rival = npcCompetitors.values().iterator().next();
            currentRivalId = rival.getId();
        }

        double returnDiff = playerReturnPercent - rival.getWeeklyReturnPercent();
        BigDecimal worthDiff = playerNetWorth.getAmount().subtract(rival.getNetWorth().getAmount());

        JSONObject obj = new JSONObject();
        obj.put("success", true);
        obj.put("rivalId", rival.getId());
        obj.put("rivalName", rival.getName());
        obj.put("rivalTitle", rival.getTitle());
        obj.put("rivalStyle", rival.getStyle());
        obj.put("rivalNetWorth", rival.getNetWorth().format());
        obj.put("rivalWeeklyReturn", String.format(java.util.Locale.ROOT, "%+.2f%%", rival.getWeeklyReturnPercent()));
        obj.put("playerNetWorth", playerNetWorth.format());
        obj.put("playerWeeklyReturn", String.format(java.util.Locale.ROOT, "%+.2f%%", playerReturnPercent));
        obj.put("returnDifference", String.format(java.util.Locale.ROOT, "%+.2f%%", returnDiff));
        obj.put("isLeading", returnDiff >= 0);
        obj.put("summary", returnDiff >= 0 ? "Kamu sedang mengungguli rival minggu ini!" : "Rival sedang memimpin performa mingguan.");
        return obj;
    }

    /**
     * Evaluasi mingguan liga (Promosi, Degradasi, dan Pemilihan Rival Baru).
     */
    public synchronized void processWeeklyRollover(double playerWeeklyReturn, Money playerNetWorth) {
        seasonWeekNumber++;

        // Variasikan performa mingguan NPC secara deterministik
        Random rng = new Random(seasonWeekNumber * 1000L);
        for (LeagueCompetitor npc : npcCompetitors.values()) {
            double delta = (rng.nextDouble() - 0.48) * 4.0;
            double newRet = Math.max(-15.0, Math.min(25.0, npc.getWeeklyReturnPercent() + delta));
            npc.setWeeklyReturnPercent(newRet);

            // Sesuaikan kekayaan berdasarkan return
            BigDecimal mult = BigDecimal.valueOf(1.0 + (newRet / 100.0));
            BigDecimal newNw = npc.getNetWorth().getAmount().multiply(mult).setScale(0, java.math.RoundingMode.HALF_UP);
            if (newNw.compareTo(BigDecimal.valueOf(1_000_000)) < 0) newNw = BigDecimal.valueOf(1_000_000);
            npc.setNetWorth(new Money(newNw, Currency.IDR));
        }

        // Tentukan promosi / degradasi pemain
        if (playerWeeklyReturn > 5.0) {
            if (currentTier == LeagueTier.BRONZE) currentTier = LeagueTier.SILVER;
            else if (currentTier == LeagueTier.SILVER) currentTier = LeagueTier.GOLD;
        } else if (playerWeeklyReturn < -5.0) {
            if (currentTier == LeagueTier.GOLD) currentTier = LeagueTier.SILVER;
            else if (currentTier == LeagueTier.SILVER) currentTier = LeagueTier.BRONZE;
        }

        // Pilih rival terdekat baru untuk minggu berikutnya
        LeagueCompetitor closest = null;
        BigDecimal minDiff = null;
        for (LeagueCompetitor npc : npcCompetitors.values()) {
            BigDecimal diff = playerNetWorth.getAmount().subtract(npc.getNetWorth().getAmount()).abs();
            if (minDiff == null || diff.compareTo(minDiff) < 0) {
                minDiff = diff;
                closest = npc;
            }
        }
        if (closest != null) {
            currentRivalId = closest.getId();
        }
    }

    public synchronized JSONObject exportState() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("currentTier", currentTier.name());
        obj.put("seasonWeekNumber", seasonWeekNumber);
        obj.put("currentRivalId", currentRivalId);

        JSONArray arr = new JSONArray();
        for (LeagueCompetitor c : npcCompetitors.values()) {
            arr.put(c.exportState());
        }
        obj.put("competitors", arr);
        return obj;
    }

    public synchronized void restoreState(JSONObject obj) throws JSONException {
        if (obj == null) return;
        this.currentTier = LeagueTier.valueOf(obj.optString("currentTier", "BRONZE"));
        this.seasonWeekNumber = obj.optInt("seasonWeekNumber", 1);
        this.currentRivalId = obj.optString("currentRivalId", "NPC-RIV-01");

        JSONArray arr = obj.optJSONArray("competitors");
        if (arr != null) {
            npcCompetitors.clear();
            for (int i = 0; i < arr.length(); i++) {
                LeagueCompetitor c = LeagueCompetitor.restoreFromState(arr.getJSONObject(i));
                if (c != null) npcCompetitors.put(c.getId(), c);
            }
        }
    }
}
