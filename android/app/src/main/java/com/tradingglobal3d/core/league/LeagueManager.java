package com.tradingglobal3d.core.league;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.trading.*;
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

    private static final String[] DEFAULT_NAMES = {
        "Arunika Bara",
        "Banyu Karang",
        "Cakra Nirmala",
        "Damar Kencana",
        "Embun Lazuardi",
        "Fajar Sembada",
        "Genta Pradipta",
        "Harsa Wanatirta",
        "Intan Rimbani",
        "Jati Wisesa",
        "Kirana Wanaraya",
        "Lintang Serat",
        "Mega Sandiwara",
        "Nara Bumikarsa",
        "Ombak Medika",
        "Pijar Angkasa",
        "Ranu Sumberjati",
        "Sagara Susumerta",
        "Tirta Minyaksa",
        "Wira Lentera"
    };
    // ponytail: exact ID/name pairs migrate shipped defaults; custom names stay untouched.
    private static final String[] LEGACY_NAMES = {
        "Prajogo Pangestu",
        "Low Tuck Kwong",
        "Robert Budi Hartono",
        "Michael Bambang Hartono",
        "Sri Prakash Lohia",
        "Chairul Tanjung",
        "Tahir Perkasa",
        "Lim Hariyanto",
        "Djoko Susanto",
        "Theodore Rachmat",
        "Martua Sitorus",
        "Sukanto Tanoto",
        "Eddy Kusnadi Sariaatmadja",
        "Ciputra Nusantara",
        "Mochtar Riady",
        "Hary Tanoesoedibjo",
        "Hashim Djojohadikusumo",
        "Bambang Sutantio",
        "Arifin Panigoro Junior",
        "Hendra Sukanto"
    };

    private LeagueTier currentTier = LeagueTier.BRONZE;
    private final Map<String, LeagueCompetitor> npcCompetitors = new LinkedHashMap<>();
    private String currentRivalId = "NPC-RIV-01";
    private int seasonWeekNumber = 1;
    private boolean canonicalNpcAccounts;
    private Map<String, Account> accounts;
    private InstrumentCatalog catalog;
    private final Map<String, BigDecimal> npcPeriodOpening = new LinkedHashMap<>();
    private final List<JSONObject> periodHistory = new ArrayList<>();

    public LeagueManager() {
        initializeNpcRoster();
    }

    public synchronized void reset() {
        currentTier = LeagueTier.BRONZE;
        seasonWeekNumber = 1;
        canonicalNpcAccounts = false; accounts = null; catalog = null;
        npcPeriodOpening.clear(); periodHistory.clear();
        initializeNpcRoster();
    }

    private void initializeNpcRoster() {
        npcCompetitors.clear();
        // 20 Konglomerat dan Trader NPC Fiktif (D09)
        addNpc("NPC-01", DEFAULT_NAMES[0], "Taipan Energi & Petrokimia", "AGRESIF", 550_000_000_000L, 4.25);
        addNpc("NPC-02", DEFAULT_NAMES[1], "Raja Pertambangan Batubara", "SEIMBANG", 380_000_000_000L, 3.80);
        addNpc("NPC-03", DEFAULT_NAMES[2], "Pemilik Konglomerasi Finansial", "KONSERVATIF", 350_000_000_000L, 2.10);
        addNpc("NPC-04", DEFAULT_NAMES[3], "Taipan Perbankan & Konsumer", "KONSERVATIF", 330_000_000_000L, 2.05);
        addNpc("NPC-05", DEFAULT_NAMES[4], "Pengusaha Manufaktur Global", "SEIMBANG", 125_000_000_000L, 3.15);
        addNpc("NPC-06", DEFAULT_NAMES[5], "Raja Media & Perbankan", "SEIMBANG", 80_000_000_000L, 2.90);
        addNpc("NPC-07", DEFAULT_NAMES[6], "Taipan Finansial & Properti", "SEIMBANG", 65_000_000_000L, 2.45);
        addNpc("NPC-08", DEFAULT_NAMES[7], "Pemilik Perkebunan Sawit", "KONSERVATIF", 45_000_000_000L, 1.85);
        addNpc("NPC-09", DEFAULT_NAMES[8], "Penguasa Ritel & Minimarket", "KONSERVATIF", 35_000_000_000L, 1.95);
        addNpc("NPC-10", DEFAULT_NAMES[9], "Konglomerat Manufaktur Otomotif", "SEIMBANG", 30_000_000_000L, 2.60);
        addNpc("NPC-11", DEFAULT_NAMES[10], "Pengusaha Agrikultur Raksasa", "SEIMBANG", 25_000_000_000L, 2.30);
        addNpc("NPC-12", DEFAULT_NAMES[11], "Pemilik Industri Kertas & Serat", "AGRESIF", 20_000_000_000L, 3.50);
        addNpc("NPC-13", DEFAULT_NAMES[12], "Raja Teknologi & Media Digital", "AGRESIF", 15_000_000_000L, 4.10);
        addNpc("NPC-14", DEFAULT_NAMES[13], "Pelopor Real Estate Terpadu", "KONSERVATIF", 10_000_000_000L, 1.70);
        addNpc("NPC-15", DEFAULT_NAMES[14], "Taipan Jasa Finansial & Medis", "SEIMBANG", 8_000_000_000L, 2.20);
        addNpc("NPC-16", DEFAULT_NAMES[15], "Konglomerat Media Siaran", "AGRESIF", 5_000_000_000L, 3.05);
        addNpc("NPC-17", DEFAULT_NAMES[16], "Pengusaha Investasi & Sumber Daya", "SEIMBANG", 3_000_000_000L, 2.15);
        addNpc("NPC-18", DEFAULT_NAMES[17], "Raja Produk Susu & Olahan", "KONSERVATIF", 1_000_000_000L, 1.65);
        addNpc("NPC-19", DEFAULT_NAMES[18], "Eksplorasi Migas Mandiri", "AGRESIF", 500_000_000L, 4.80);
        addNpc("NPC-RIV-01", DEFAULT_NAMES[19], "Trader Ambisius Rival", "AGRESIF", 45_000_000L, 2.75);

        this.currentRivalId = "NPC-RIV-01";
    }

    private void addNpc(String id, String name, String title, String style, long netWorthIdr, double weeklyReturn) {
        npcCompetitors.put(id, new LeagueCompetitor(id, name, title, style, Money.of(netWorthIdr, Currency.IDR), 0.0, false));
    }

    public static String accountIdForNpc(String npcId) { return "ACC-LEAGUE-" + npcId; }

    /** Attach canonical market participants; migrating legacy wealth becomes explicitly booked NPC seed capital. */
    public synchronized void bindMarket(Map<String, Account> canonicalAccounts, EconomyLedger ledger, InstrumentCatalog market, long now) {
        Objects.requireNonNull(canonicalAccounts); Objects.requireNonNull(ledger); Objects.requireNonNull(market);
        for (LeagueCompetitor npc : npcCompetitors.values()) {
            String accountId = accountIdForNpc(npc.getId());
            Account account = canonicalAccounts.get(accountId);
            if (account == null) {
                if (canonicalNpcAccounts) throw new IllegalArgumentException("Akun NPC liga hilang: " + accountId);
                account = new Account(accountId, npc.getName(), AccountType.FIRM, null);
                canonicalAccounts.put(accountId, account);
                ledger.recordMutation("LEAGUE-SEED:" + npc.getId(), account, LedgerEntryType.INITIAL_CAPITAL,
                        npc.getNetWorth(), true, now, "Modal awal NPC liga, bukan profit periode");
            }
            if (account.getType() != AccountType.FIRM) throw new IllegalArgumentException("Jenis akun NPC liga tidak sah");
            if (!npcPeriodOpening.containsKey(npc.getId())) npcPeriodOpening.put(npc.getId(), npc.getNetWorth().getAmount());
        }
        accounts = canonicalAccounts; catalog = market; canonicalNpcAccounts = true;
        refreshNpcValuations();
    }

    public synchronized void refreshNpcValuations() {
        if (accounts == null || catalog == null) return;
        for (LeagueCompetitor npc : npcCompetitors.values()) {
            Account account = accounts.get(accountIdForNpc(npc.getId()));
            if (account == null) throw new IllegalStateException("Akun canonical NPC liga hilang");
            Money value = account.getTotalCash(Currency.IDR);
            for (Map.Entry<String, Position> holding : account.getPositions().entrySet()) {
                Instrument asset = catalog.getById(holding.getKey());
                if (asset == null || asset.getCurrency() != Currency.IDR || holding.getValue().getQuantity() < 0)
                    throw new IllegalStateException("Posisi NPC liga tidak dapat divaluasi");
                value = value.add(asset.getCurrentPrice().multiply(BigDecimal.valueOf(holding.getValue().getQuantity())));
            }
            npc.setNetWorth(value);
            BigDecimal opening = npcPeriodOpening.get(npc.getId());
            // No external flows are accepted after initial seed: Modified Dietz reduces to (V1-V0)/V0.
            npc.setWeeklyReturnPercent(opening != null && opening.signum() > 0 ?
                    value.getAmount().subtract(opening).multiply(BigDecimal.valueOf(100))
                            .divide(opening, 12, java.math.RoundingMode.HALF_UP).doubleValue() : 0.0);
        }
    }

    /** Deterministic 30-minute intents, executed only by the same matching engine as player orders. */
    public synchronized List<Order> getNpcOrders(long now) {
        List<Order> result = new ArrayList<>();
        if (accounts == null || catalog == null) return result;
        refreshNpcValuations();
        List<Instrument> instruments = new ArrayList<>();
        for (Instrument instrument : catalog.getActiveUniverse()) if (instrument.getCurrency() == Currency.IDR
                && instrument.isTradable() && instrument.getCurrentPrice().isPositive()) instruments.add(instrument);
        instruments.sort(Comparator.comparing(Instrument::getId));
        if (instruments.isEmpty()) return result;
        int index = 0;
        for (LeagueCompetitor npc : npcCompetitors.values()) {
            Account account = accounts.get(accountIdForNpc(npc.getId()));
            Instrument instrument = instruments.get(index++ % instruments.size());
            BigDecimal allocation = new BigDecimal(npc.getStyle().equals("AGRESIF") ? "0.85" :
                    npc.getStyle().equals("KONSERVATIF") ? "0.4" : "0.65");
            Position position = account.getPosition(instrument.getId());
            long owned = position == null ? 0 : position.getQuantity();
            BigDecimal target = npc.getNetWorth().getAmount().multiply(allocation);
            BigDecimal gap = target.subtract(instrument.getCurrentPrice().getAmount().multiply(BigDecimal.valueOf(owned)));
            long lot = instrument.getLotSize();
            if (lot <= 0) continue;
            OrderSide side = gap.signum() >= 0 ? OrderSide.BUY : OrderSide.SELL;
            BigDecimal price = instrument.getCurrentPrice().getAmount();
            BigDecimal budget = side == OrderSide.BUY ? gap.min(account.getAvailableCash(Currency.IDR).getAmount()) : gap.abs();
            BigDecimal grossLot = price.multiply(BigDecimal.valueOf(lot)).multiply(side == OrderSide.BUY ?
                    BigDecimal.ONE.add(MatchingEngine.BUY_FEE_RATE) : BigDecimal.ONE);
            long quantity = budget.divide(grossLot, 0, java.math.RoundingMode.DOWN).longValueExact() * lot;
            if (side == OrderSide.SELL) quantity = Math.min(quantity, owned / lot * lot);
            if (quantity <= 0) continue;
            String request = "LEAGUE:" + npc.getId() + ":" + now / 1_800_000L;
            result.add(new Order(request, request, account.getId(), instrument.getId(), side, OrderType.LIMIT,
                    instrument.getCurrentPrice(), null, quantity, now, Money.zero(Currency.IDR)));
        }
        return result;
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
        refreshNpcValuations();
        List<LeagueCompetitor> all = new ArrayList<>(npcCompetitors.values());
        LeagueCompetitor playerComp = new LeagueCompetitor(
                "PLAYER", playerName != null ? playerName + " (Kamu)" : "Pemain (Kamu)",
                playerTitle != null ? playerTitle : "Trader", "DINAMIS", playerNetWorth, 0.0, true
        );
        all.add(playerComp);

        // Equal primary metrics use stable account ID, independent of save array order.
        all.sort(Comparator.comparing((LeagueCompetitor c) -> c.getNetWorth().getAmount()).reversed()
                .thenComparing(LeagueCompetitor::getId));

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
        return getLeaderboardByWeeklyReturn(Double.valueOf(playerReturnPercent), playerNetWorth, playerName, playerTitle);
    }
    public synchronized JSONObject getLeaderboardByWeeklyReturn(Double playerReturnPercent, Money playerNetWorth, String playerName, String playerTitle) throws JSONException {
        refreshNpcValuations();
        boolean eligible = validReturn(playerReturnPercent) && playerNetWorth.isPositive();
        List<LeagueCompetitor> all = new ArrayList<>(npcCompetitors.values());
        LeagueCompetitor playerComp = new LeagueCompetitor(
                "PLAYER", playerName != null ? playerName + " (Kamu)" : "Pemain (Kamu)",
                playerTitle != null ? playerTitle : "Trader", "DINAMIS", playerNetWorth, eligible ? playerReturnPercent : 0.0, true
        );
        if (eligible) all.add(playerComp);

        all.sort(Comparator.comparingDouble(LeagueCompetitor::getWeeklyReturnPercent).reversed()
                .thenComparing(LeagueCompetitor::getId));

        int playerRank = 1;
        JSONArray entries = new JSONArray();
        for (int i = 0; i < all.size(); i++) {
            LeagueCompetitor c = all.get(i);
            int rank = i + 1;
            if (c.isPlayer()) playerRank = rank;
            entries.put(c.toJsonObject(rank));
        }
        if (!eligible) entries.put(playerComp.toJsonObject(0).put("rank", JSONObject.NULL)
                .put("weeklyReturnRaw", JSONObject.NULL).put("weeklyReturn", "Belum tersedia")
                .put("eligible", false).put("eligibilityReason", "Basis return belum sah atau kekayaan tidak positif"));

        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("mode", "WEEKLY_RETURN");
        res.put("currentTier", currentTier.name());
        res.put("tierLabel", currentTier.getLabel());
        res.put("seasonWeek", seasonWeekNumber);
        res.put("playerRank", eligible ? playerRank : JSONObject.NULL);
        res.put("playerEligible", eligible);
        res.put("totalCompetitors", npcCompetitors.size() + 1);
        res.put("eligibleCompetitors", all.size());
        res.put("leaderboard", entries);
        return res;
    }

    /**
     * Memperoleh status rival persisten minggu ini.
     */
    public synchronized JSONObject getRivalStatus(Money playerNetWorth, double playerReturnPercent) throws JSONException {
        return getRivalStatus(playerNetWorth, Double.valueOf(playerReturnPercent));
    }
    public synchronized JSONObject getRivalStatus(Money playerNetWorth, Double playerReturnPercent) throws JSONException {
        refreshNpcValuations();
        LeagueCompetitor rival = npcCompetitors.get(currentRivalId);
        if (rival == null) {
            rival = npcCompetitors.values().iterator().next();
            currentRivalId = rival.getId();
        }

        boolean eligible = validReturn(playerReturnPercent) && playerNetWorth.isPositive();
        double returnDiff = eligible ? playerReturnPercent - rival.getWeeklyReturnPercent() : 0.0;
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
        obj.put("playerWeeklyReturn", eligible ? String.format(java.util.Locale.ROOT, "%+.2f%%", playerReturnPercent) : "Belum tersedia");
        obj.put("returnDifference", eligible ? String.format(java.util.Locale.ROOT, "%+.2f%%", returnDiff) : "Belum tersedia");
        obj.put("playerEligible", eligible);
        obj.put("isLeading", eligible && returnDiff >= 0);
        obj.put("summary", !eligible ? "Return belum tersedia; menunggu basis periode yang sah." :
                returnDiff >= 0 ? "Kamu sedang mengungguli rival minggu ini!" : "Rival sedang memimpin performa mingguan.");
        return obj;
    }

    /**
     * Evaluasi mingguan liga (Promosi, Degradasi, dan Pemilihan Rival Baru).
     */
    public synchronized void processWeeklyRollover(double playerWeeklyReturn, Money playerNetWorth) {
        processWeeklyRollover(Double.valueOf(playerWeeklyReturn), playerNetWorth);
    }
    private static boolean validReturn(Double value) { return value != null && Double.isFinite(value); }
    public synchronized void processWeeklyRollover(Double playerWeeklyReturn, Money playerNetWorth) {
        refreshNpcValuations();
        try {
            JSONObject archived = getLeaderboardByWeeklyReturn(playerWeeklyReturn, playerNetWorth, "Pemain", "Trader");
            periodHistory.add(new JSONObject(archived.toString()));
            if (periodHistory.size() > 52) periodHistory.remove(0);
        } catch (JSONException error) { throw new IllegalStateException("Arsip liga gagal", error); }
        seasonWeekNumber++;
        for (LeagueCompetitor npc : npcCompetitors.values()) {
            npcPeriodOpening.put(npc.getId(), npc.getNetWorth().getAmount());
            npc.setWeeklyReturnPercent(0.0);
        }

        // Tentukan promosi / degradasi pemain
        if (validReturn(playerWeeklyReturn) && playerNetWorth.isPositive() && playerWeeklyReturn > 5.0) {
            if (currentTier == LeagueTier.BRONZE) currentTier = LeagueTier.SILVER;
            else if (currentTier == LeagueTier.SILVER) currentTier = LeagueTier.GOLD;
        } else if (validReturn(playerWeeklyReturn) && playerNetWorth.isPositive() && playerWeeklyReturn < -5.0) {
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
        obj.put("canonicalNpcAccounts", canonicalNpcAccounts);
        JSONObject openings = new JSONObject();
        for (Map.Entry<String, BigDecimal> entry : npcPeriodOpening.entrySet()) openings.put(entry.getKey(), entry.getValue().toPlainString());
        obj.put("periodOpening", openings).put("periodHistory", new JSONArray(periodHistory));

        JSONArray arr = new JSONArray();
        for (LeagueCompetitor c : npcCompetitors.values()) {
            arr.put(c.exportState());
        }
        obj.put("competitors", arr);
        return obj;
    }

    public synchronized void restoreState(JSONObject obj) throws JSONException {
        if (obj == null) return;
        LeagueTier tier = LeagueTier.valueOf(obj.getString("currentTier"));
        int week = obj.getInt("seasonWeekNumber");
        String rival = obj.getString("currentRivalId");
        if (week < 1) throw new IllegalArgumentException("Minggu liga tidak sah");
        Map<String, LeagueCompetitor> restored = new LinkedHashMap<>();
        Map<String, BigDecimal> openings = new LinkedHashMap<>();
        JSONObject savedOpenings = obj.optJSONObject("periodOpening");
        JSONArray arr = obj.getJSONArray("competitors");
        if (arr.length() != DEFAULT_NAMES.length) throw new IllegalArgumentException("Roster liga tidak lengkap");
            for (int i = 0; i < arr.length(); i++) {
                LeagueCompetitor c = LeagueCompetitor.restoreFromState(arr.getJSONObject(i));
                if (c != null) {
                    if (!npcCompetitors.containsKey(c.getId()) || c.isPlayer() || restored.containsKey(c.getId())
                            || !Double.isFinite(c.getWeeklyReturnPercent()) || c.getNetWorth().isNegative()
                            || c.getName().trim().isEmpty()) throw new IllegalArgumentException("Peserta liga tidak sah");
                    for (int n = 0; n < LEGACY_NAMES.length; n++) {
                        String defaultId = n == 19 ? "NPC-RIV-01" : String.format(Locale.ROOT, "NPC-%02d", n + 1);
                        if (!c.isPlayer() && defaultId.equals(c.getId()) && LEGACY_NAMES[n].equals(c.getName())) {
                            c = new LeagueCompetitor(c.getId(), DEFAULT_NAMES[n], c.getTitle(), c.getStyle(),
                                    c.getNetWorth(), c.getWeeklyReturnPercent(), false);
                            break;
                        }
                    }
                    restored.put(c.getId(), c);
                    if (savedOpenings != null && savedOpenings.has(c.getId())) {
                        BigDecimal opening = new BigDecimal(savedOpenings.getString(c.getId()));
                        if (opening.signum() < 0) throw new IllegalArgumentException("Basis return NPC negatif");
                        openings.put(c.getId(), opening);
                    }
                }
            }
        if (!restored.containsKey(rival)) throw new IllegalArgumentException("Rival liga tidak ditemukan");
        boolean canonical = obj.optBoolean("canonicalNpcAccounts", false);
        if (canonical && openings.size() != restored.size()) throw new IllegalArgumentException("Basis return canonical NPC tidak lengkap");
        JSONArray history = obj.optJSONArray("periodHistory");
        List<JSONObject> archived = new ArrayList<>();
        if (history != null) for (int i = Math.max(0, history.length() - 52); i < history.length(); i++) {
            JSONObject period = history.getJSONObject(i);
            if (period.getInt("seasonWeek") < 1 || period.getInt("seasonWeek") >= week)
                throw new IllegalArgumentException("Arsip periode liga tidak sah");
            archived.add(new JSONObject(period.toString()));
        }
        currentTier = tier; seasonWeekNumber = week; currentRivalId = rival;
        npcCompetitors.clear(); npcCompetitors.putAll(restored);
        npcPeriodOpening.clear(); npcPeriodOpening.putAll(openings);
        periodHistory.clear(); periodHistory.addAll(archived);
        canonicalNpcAccounts = canonical; accounts = null; catalog = null;
    }
}
