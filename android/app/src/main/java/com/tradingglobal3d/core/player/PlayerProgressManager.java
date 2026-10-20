package com.tradingglobal3d.core.player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Manajer Progres Pemain (Level, XP, dan Pencapaian / Achievement).
 * Beroperasi di domain authoritative, terpisah dari jenjang karier dan reputasi profesional.
 * Seluruh pencapaian bersifat idempoten, satu kali klaim, dan tidak memberikan hadiah uang game.
 */
public final class PlayerProgressManager {

    public static final class Achievement {
        private final String id;
        private final String name;
        private final String description;
        private final int xpReward;
        private final String icon;
        private final int targetProgress;
        private int currentProgress;
        private boolean unlocked;
        private long unlockedTimestamp;

        public Achievement(String id, String name, String description, int xpReward, String icon, int targetProgress) {
            this.id = Objects.requireNonNull(id);
            this.name = Objects.requireNonNull(name);
            this.description = Objects.requireNonNull(description);
            this.xpReward = xpReward;
            this.icon = icon;
            this.targetProgress = targetProgress;
            this.currentProgress = 0;
            this.unlocked = false;
            this.unlockedTimestamp = 0;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getXpReward() { return xpReward; }
        public String getIcon() { return icon; }
        public int getTargetProgress() { return targetProgress; }
        public int getCurrentProgress() { return currentProgress; }
        public boolean isUnlocked() { return unlocked; }
        public long getUnlockedTimestamp() { return unlockedTimestamp; }

        public void setProgress(int val) {
            this.currentProgress = Math.min(val, targetProgress);
        }

        public boolean unlock(long timestamp) {
            if (!this.unlocked) {
                this.unlocked = true;
                this.currentProgress = targetProgress;
                this.unlockedTimestamp = timestamp;
                return true;
            }
            return false;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("name", name);
            obj.put("desc", description);
            obj.put("xp", xpReward + " XP");
            obj.put("xpValue", xpReward);
            obj.put("icon", icon);
            obj.put("target", targetProgress);
            obj.put("progress", currentProgress + " / " + targetProgress);
            obj.put("unlocked", unlocked);
            obj.put("unlockedTimestamp", unlockedTimestamp);
            return obj;
        }
    }

    private int currentXp = 0;
    private final Map<String, Achievement> achievements = new LinkedHashMap<>();
    private final Set<String> readArticleIds = new LinkedHashSet<>();
    private final Set<String> tradedExchanges = new LinkedHashSet<>();
    private final Map<String, Integer> missionXpAwards = new LinkedHashMap<>();
    private int cleanSessionCloses = 0;
    private int clientReportsCompleted = 0;

    public PlayerProgressManager() {
        initializeAchievements();
    }

    private void initializeAchievements() {
        achievements.clear();
        achievements.put("first", new Achievement(
                "first", "Transaksi pertama",
                "Selesaikan satu transaksi dan baca receipt-nya.",
                100, "market", 1
        ));
        achievements.put("reader", new Achievement(
                "reader", "Pembaca pasar",
                "Pelajari konteks dari lima catatan riset.",
                150, "book", 5
        ));
        achievements.put("mandate", new Achievement(
                "mandate", "Kepercayaan pertama",
                "Terima mandat setelah memenuhi syarat karier.",
                200, "users", 1
        ));
        achievements.put("risk", new Achievement(
                "risk", "Penjaga risiko",
                "Tutup lima sesi dengan seluruh batas mandat terjaga.",
                250, "shield", 5
        ));
        achievements.put("global", new Achievement(
                "global", "Lintas pasar",
                "Pahami transaksi IDR dan USD beserta risikonya.",
                200, "globe", 2
        ));
        achievements.put("report", new Achievement(
                "report", "Laporan tepercaya",
                "Tuntaskan tiga laporan klien dengan data yang dapat ditelusuri.",
                300, "file", 3
        ));
        achievements.put("company_founder", new Achievement(
                "company_founder", "Pendiri Konglomerasi",
                "Dirikan anak usaha operasional pertamamu.",
                250, "briefcase", 1
        ));
        achievements.put("chief_hr", new Achievement(
                "chief_hr", "Harmoni Manajemen",
                "Terapkan kebijakan kerja dan kelola operasional staf.",
                180, "users", 1
        ));
        achievements.put("collector_entry", new Achievement(
                "collector_entry", "Kolektor Pemula",
                "Miliki barang mewah atau properti perdana di galeri.",
                150, "award", 1
        ));
        achievements.put("collector_luxury", new Achievement(
                "collector_luxury", "Koleksi Bergengsi",
                "Capai valuasi koleksi mewah minimal Rp 50.000.000.",
                300, "gem", 1
        ));
        achievements.put("fund_investor", new Achievement(
                "fund_investor", "Investasi Reksa Dana",
                "Beli unit reksa dana pertamamu berbasis NAB.",
                150, "pie-chart", 1
        ));
        achievements.put("loan_responsible", new Achievement(
                "loan_responsible", "Debitur Tepercaya",
                "Ambil dan lunasi fasilitas pinjaman tenor tepat waktu.",
                200, "credit-card", 1
        ));
        achievements.put("league_competitor", new Achievement(
                "league_competitor", "Petarung Liga",
                "Berpartisipasi aktif dalam kompetisi liga mingguan.",
                150, "trophy", 1
        ));
        achievements.put("league_promoted", new Achievement(
                "league_promoted", "Promosi Divisi",
                "Raih promosi ke divisi liga yang lebih tinggi.",
                350, "chevrons-up", 1
        ));
        achievements.put("networth_100m", new Achievement(
                "networth_100m", "Centurion Kekayaan",
                "Capai total kekayaan bersih pribadi Rp 100.000.000.",
                300, "trending-up", 1
        ));
        achievements.put("networth_1b", new Achievement(
                "networth_1b", "Miliarder Mandiri",
                "Capai total kekayaan bersih pribadi Rp 1.000.000.000.",
                500, "star", 1
        ));
        achievements.put("periodic_master", new Achievement(
                "periodic_master", "Disiplin Misi",
                "Selesaikan dan klaim tiga misi periodik harian atau mingguan.",
                200, "check-circle", 3
        ));
        achievements.put("research_elite", new Achievement(
                "research_elite", "Analis Ulung",
                "Pelajari 10 catatan riset dan kalender emiten pasar.",
                250, "book-open", 10
        ));
    }

    public synchronized void reset() {
        currentXp = 0;
        readArticleIds.clear();
        tradedExchanges.clear();
        missionXpAwards.clear();
        cleanSessionCloses = 0;
        clientReportsCompleted = 0;
        initializeAchievements();
    }

    public synchronized int getCurrentXp() {
        return currentXp;
    }

    public synchronized int getCurrentLevel() {
        return calculateLevel(currentXp);
    }

    public static int calculateLevel(int xp) {
        if (xp < 100) return 1;
        if (xp < 250) return 2;
        if (xp < 450) return 3;
        if (xp < 700) return 4;
        if (xp < 1000) return 5;
        if (xp < 1350) return 6;
        if (xp < 1750) return 7;
        return 8 + (xp - 1750) / 500;
    }

    public static int getNextLevelThreshold(int currentLevel) {
        switch (currentLevel) {
            case 1: return 100;
            case 2: return 250;
            case 3: return 450;
            case 4: return 700;
            case 5: return 1000;
            case 6: return 1350;
            case 7: return 1750;
            default: return 1750 + (currentLevel - 7) * 500;
        }
    }

    public static int getBaseLevelThreshold(int currentLevel) {
        switch (currentLevel) {
            case 1: return 0;
            case 2: return 100;
            case 3: return 250;
            case 4: return 450;
            case 5: return 700;
            case 6: return 1000;
            case 7: return 1350;
            default: return 1750 + (currentLevel - 8) * 500;
        }
    }

    /**
     * Merekam eksekusi trade fill yang sah.
     */
    public synchronized boolean onTradeExecuted(String exchange, long timestamp) {
        boolean xpGranted = false;
        if (exchange != null && !exchange.isEmpty()) {
            tradedExchanges.add(exchange.toUpperCase(Locale.ROOT));
        }

        // Achievement 1: Transaksi Pertama
        Achievement first = achievements.get("first");
        if (first != null && !first.isUnlocked()) {
            first.unlock(timestamp);
            currentXp += first.getXpReward();
            xpGranted = true;
        }

        // Achievement 5: Lintas Pasar (IDX + US)
        Achievement global = achievements.get("global");
        if (global != null && !global.isUnlocked()) {
            global.setProgress(tradedExchanges.size());
            if (tradedExchanges.contains("IDX") && tradedExchanges.contains("US")) {
                global.unlock(timestamp);
                currentXp += global.getXpReward();
                xpGranted = true;
            }
        }
        return xpGranted;
    }

    /**
     * Merekam pembacaan artikel riset/berita secara jujur.
     */
    public synchronized boolean onResearchRead(String articleId, long timestamp) {
        if (articleId == null || articleId.trim().isEmpty()) return false;
        boolean isNew = readArticleIds.add(articleId.trim());
        if (!isNew) return false;

        Achievement reader = achievements.get("reader");
        if (reader != null && !reader.isUnlocked()) {
            reader.setProgress(readArticleIds.size());
            if (readArticleIds.size() >= reader.getTargetProgress()) {
                reader.unlock(timestamp);
                currentXp += reader.getXpReward();
                return true;
            }
        }
        return false;
    }

    /**
     * Merekam penerimaan mandat kelolaan klien resmi.
     */
    public synchronized boolean onMandateAccepted(long timestamp) {
        Achievement mandate = achievements.get("mandate");
        if (mandate != null && !mandate.isUnlocked()) {
            mandate.unlock(timestamp);
            currentXp += mandate.getXpReward();
            return true;
        }
        return false;
    }

    /**
     * Merekam penutupan sesi harian yang bersih dari pelanggaran mandat klien.
     */
    public synchronized boolean onCleanSessionClosed(long timestamp) {
        cleanSessionCloses++;
        Achievement risk = achievements.get("risk");
        if (risk != null && !risk.isUnlocked()) {
            risk.setProgress(cleanSessionCloses);
            if (cleanSessionCloses >= risk.getTargetProgress()) {
                risk.unlock(timestamp);
                currentXp += risk.getXpReward();
                return true;
            }
        }
        return false;
    }

    /**
     * Merekam penuntasan laporan evaluasi klien.
     */
    public synchronized boolean onClientReportCompleted(long timestamp) {
        clientReportsCompleted++;
        Achievement report = achievements.get("report");
        if (report != null && !report.isUnlocked()) {
            report.setProgress(clientReportsCompleted);
            if (clientReportsCompleted >= report.getTargetProgress()) {
                report.unlock(timestamp);
                currentXp += report.getXpReward();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean onCompanyFounded(long timestamp) {
        Achievement a = achievements.get("company_founder");
        if (a != null && !a.isUnlocked()) {
            a.unlock(timestamp);
            currentXp += a.getXpReward();
            return true;
        }
        return false;
    }

    public synchronized boolean onPolicySet(long timestamp) {
        Achievement a = achievements.get("chief_hr");
        if (a != null && !a.isUnlocked()) {
            a.unlock(timestamp);
            currentXp += a.getXpReward();
            return true;
        }
        return false;
    }

    public synchronized boolean onCollectionPurchased(long totalValuationIdr, long timestamp) {
        boolean granted = false;
        Achievement a1 = achievements.get("collector_entry");
        if (a1 != null && !a1.isUnlocked()) {
            a1.unlock(timestamp);
            currentXp += a1.getXpReward();
            granted = true;
        }
        Achievement a2 = achievements.get("collector_luxury");
        if (a2 != null && !a2.isUnlocked()) {
            if (totalValuationIdr >= 50_000_000L) {
                a2.unlock(timestamp);
                currentXp += a2.getXpReward();
                granted = true;
            }
        }
        return granted;
    }

    public synchronized boolean onFundSubscribed(long timestamp) {
        Achievement a = achievements.get("fund_investor");
        if (a != null && !a.isUnlocked()) {
            a.unlock(timestamp);
            currentXp += a.getXpReward();
            return true;
        }
        return false;
    }

    public synchronized boolean onLoanPaidOff(long timestamp) {
        Achievement a = achievements.get("loan_responsible");
        if (a != null && !a.isUnlocked()) {
            a.unlock(timestamp);
            currentXp += a.getXpReward();
            return true;
        }
        return false;
    }

    public synchronized boolean onLeaguePromoted(long timestamp) {
        Achievement a = achievements.get("league_promoted");
        if (a != null && !a.isUnlocked()) {
            a.unlock(timestamp);
            currentXp += a.getXpReward();
            return true;
        }
        return false;
    }

    public synchronized boolean onPeriodicMissionClaimed(long timestamp) {
        Achievement a = achievements.get("periodic_master");
        if (a != null && !a.isUnlocked()) {
            a.setProgress(a.getCurrentProgress() + 1);
            if (a.getCurrentProgress() >= a.getTargetProgress()) {
                a.unlock(timestamp);
                currentXp += a.getXpReward();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean awardMissionXp(String missionId, int xp, long now) {
        if (missionId == null || missionId.trim().isEmpty() || missionId.length() > 160 || xp < 0)
            throw new IllegalArgumentException("Hadiah XP misi tidak valid");
        Integer prior = missionXpAwards.get(missionId);
        if (prior != null) {
            if (prior != xp) throw new IllegalArgumentException("Hadiah XP misi berbeda dari receipt");
            return false;
        }
        Achievement badge = achievements.get("periodic_master");
        int badgeXp = badge != null && !badge.isUnlocked()
                && badge.getCurrentProgress() + 1 >= badge.getTargetProgress() ? badge.getXpReward() : 0;
        Math.addExact(currentXp, Math.addExact(xp, badgeXp));
        currentXp += xp;
        missionXpAwards.put(missionId, xp);
        onPeriodicMissionClaimed(now);
        return true;
    }

    public synchronized boolean onNetWorthEvaluated(long netWorthIdr, long timestamp) {
        boolean granted = false;
        Achievement a100m = achievements.get("networth_100m");
        if (a100m != null && !a100m.isUnlocked() && netWorthIdr >= 100_000_000L) {
            a100m.unlock(timestamp);
            currentXp += a100m.getXpReward();
            granted = true;
        }
        Achievement a1b = achievements.get("networth_1b");
        if (a1b != null && !a1b.isUnlocked() && netWorthIdr >= 1_000_000_000L) {
            a1b.unlock(timestamp);
            currentXp += a1b.getXpReward();
            granted = true;
        }
        return granted;
    }

    public synchronized Collection<Achievement> getAchievements() {
        return Collections.unmodifiableCollection(achievements.values());
    }

    public synchronized Achievement getAchievement(String id) {
        return achievements.get(id);
    }

    public synchronized JSONObject toSnapshotJson() throws JSONException {
        int level = getCurrentLevel();
        int baseThreshold = getBaseLevelThreshold(level);
        int nextThreshold = getNextLevelThreshold(level);
        int xpInLevel = currentXp - baseThreshold;
        int levelSpan = nextThreshold - baseThreshold;

        JSONObject obj = new JSONObject();
        obj.put("level", level);
        obj.put("currentXp", currentXp);
        obj.put("baseLevelXp", baseThreshold);
        obj.put("nextLevelXp", nextThreshold);
        obj.put("xpInCurrentLevel", Math.max(0, xpInLevel));
        obj.put("levelSpanXp", levelSpan);
        int unlockedCount = 0;
        JSONArray list = new JSONArray();
        for (Achievement a : achievements.values()) {
            if (a.isUnlocked()) unlockedCount++;
            list.put(a.toJson());
        }
        obj.put("achievementsUnlocked", unlockedCount);
        obj.put("totalAchievements", achievements.size());
        obj.put("achievements", list);
        return obj;
    }

    public synchronized JSONObject exportState() throws JSONException {
        JSONObject state = new JSONObject();
        state.put("xp", currentXp);
        state.put("missionXpAwards", new JSONObject(missionXpAwards));
        state.put("cleanSessionCloses", cleanSessionCloses);
        state.put("clientReportsCompleted", clientReportsCompleted);

        JSONArray readArr = new JSONArray();
        for (String id : readArticleIds) readArr.put(id);
        state.put("readArticles", readArr);

        JSONArray exArr = new JSONArray();
        for (String ex : tradedExchanges) exArr.put(ex);
        state.put("tradedExchanges", exArr);

        JSONArray achArr = new JSONArray();
        for (Achievement a : achievements.values()) {
            JSONObject aObj = new JSONObject();
            aObj.put("id", a.getId());
            aObj.put("progress", a.getCurrentProgress());
            aObj.put("unlocked", a.isUnlocked());
            aObj.put("unlockedTimestamp", a.getUnlockedTimestamp());
            achArr.put(aObj);
        }
        state.put("achievements", achArr);
        return state;
    }

    public synchronized void restoreState(JSONObject state) throws JSONException {
        reset();
        if (state == null) return;
        currentXp = state.optInt("xp", 0);
        cleanSessionCloses = state.optInt("cleanSessionCloses", 0);
        clientReportsCompleted = state.optInt("clientReportsCompleted", 0);
        if (currentXp < 0 || cleanSessionCloses < 0 || clientReportsCompleted < 0)
            throw new JSONException("Progres pemain negatif");

        JSONArray readArr = state.optJSONArray("readArticles");
        if (readArr != null) {
            for (int i = 0; i < readArr.length(); i++) readArticleIds.add(readArr.getString(i));
        }

        JSONArray exArr = state.optJSONArray("tradedExchanges");
        if (exArr != null) {
            for (int i = 0; i < exArr.length(); i++) tradedExchanges.add(exArr.getString(i));
        }

        JSONArray achArr = state.optJSONArray("achievements");
        Set<String> restoredIds = new HashSet<>();
        int earnedXp = 0;
        if (achArr != null) {
            for (int i = 0; i < achArr.length(); i++) {
                JSONObject aObj = achArr.getJSONObject(i);
                String id = aObj.getString("id");
                Achievement a = achievements.get(id);
                if (a == null || !restoredIds.add(id) || aObj.getInt("progress") < 0
                        || aObj.getInt("progress") > a.getTargetProgress())
                    throw new JSONException("Pencapaian save tidak valid");
                if (a != null) {
                    a.setProgress(aObj.optInt("progress", 0));
                    if (aObj.optBoolean("unlocked", false)) {
                        a.unlock(aObj.optLong("unlockedTimestamp", 0));
                        earnedXp += a.getXpReward();
                    }
                }
            }
        }
        JSONObject missionAwards = state.optJSONObject("missionXpAwards");
        if (missionAwards != null) {
            Iterator<String> keys = missionAwards.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                int xp = missionAwards.getInt(key);
                if (key.trim().isEmpty() || key.length() > 160 || xp < 0)
                    throw new JSONException("XP misi tidak valid");
                missionXpAwards.put(key, xp);
                earnedXp = Math.addExact(earnedXp, xp);
            }
        }
        if (currentXp != earnedXp) throw new JSONException("XP tidak cocok dengan pencapaian dan misi");
    }
}
