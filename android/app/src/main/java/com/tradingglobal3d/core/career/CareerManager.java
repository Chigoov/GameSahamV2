package com.tradingglobal3d.core.career;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Manajer Perjalanan Karier dan Siklus Hari (Day Lifecycle).
 * Mengelola kenaikan jenjang trader -> fund manager, imbalan misi idempoten,
 * evaluasi laporan akhir hari, dan fasilitas pemulihan modal.
 */
public final class CareerManager {
    private CareerRank currentRank = CareerRank.NOVICE_TRADER;
    private int daysTraded = 1;
    private int reputationScore = 20; // 0 - 100
    private boolean bailoutUsed = false;

    private final Map<String, CareerMission> missions = new LinkedHashMap<>();
    private final List<DailySessionReport> sessionReports = new ArrayList<>();
    private Money startOfDayNetWorth;
    private int startOfDayLedgerIndex;

    public void beginDay(Account account, BigDecimal rate, EconomyLedger ledger) {
        startOfDayNetWorth = account.getTotalPortfolioValue(Currency.IDR, rate);
        startOfDayLedgerIndex = ledger.getEntries().size();
    }

    public CareerManager() {
        initializeMissions();
    }

    public void reset() {
        currentRank = CareerRank.NOVICE_TRADER;
        daysTraded = 1;
        reputationScore = 20;
        bailoutUsed = false;
        startOfDayNetWorth = null;
        startOfDayLedgerIndex = 0;
        missions.clear();
        sessionReports.clear();
        initializeMissions();
    }

    private void initializeMissions() {
        missions.put("MIS-01", new CareerMission(
                "MIS-01", "Langkah Pertama di Bursa",
                "Lakukan transaksi saham pertama di pasar internal",
                Money.of(2_500_000, Currency.IDR)
        ));
        missions.put("MIS-02", new CareerMission(
                "MIS-02", "Diversifikasi Portofolio",
                "Miliki posisi saham pada sedikitnya dua sektor berbeda",
                Money.of(5_000_000, Currency.IDR)
        ));
        missions.put("MIS-03", new CareerMission(
                "MIS-03", "Pertumbuhan Modal Konsisten",
                "Capai total kekayaan portofolio Rp 75.000.000",
                Money.of(10_000_000, Currency.IDR)
        ));
    }

    public org.json.JSONObject exportState() throws org.json.JSONException {
        org.json.JSONObject state = new org.json.JSONObject().put("rank", currentRank.name())
                .put("days", daysTraded).put("reputation", reputationScore).put("bailout", bailoutUsed)
                .put("ledgerIndex", startOfDayLedgerIndex);
        state.put("startNav", startOfDayNetWorth == null ? org.json.JSONObject.NULL : startOfDayNetWorth.getAmount().toPlainString());
        org.json.JSONArray savedMissions = new org.json.JSONArray();
        for (CareerMission m : missions.values()) savedMissions.put(new org.json.JSONObject()
                .put("id", m.getId()).put("completed", m.isCompleted()).put("claimed", m.isClaimed()));
        org.json.JSONArray reports = new org.json.JSONArray();
        for (DailySessionReport report : sessionReports) reports.put(new org.json.JSONObject()
                .put("date", report.getDate().toString())
                .put("start", report.getStartingNetWorth().getAmount().toPlainString())
                .put("end", report.getEndingNetWorth().getAmount().toPlainString())
                .put("pnl", report.getDailyPnL().getAmount().toPlainString())
                .put("fees", report.getFeesPaid().getAmount().toPlainString())
                .put("salary", report.getSalaryReceived().getAmount().toPlainString())
                .put("fills", report.getTotalFillsCount()).put("note", report.getSummaryNote()));
        return state.put("missions", savedMissions).put("reports", reports);
    }

    public void restoreState(org.json.JSONObject state) throws org.json.JSONException {
        currentRank = CareerRank.valueOf(state.getString("rank"));
        daysTraded = state.getInt("days"); reputationScore = state.getInt("reputation"); bailoutUsed = state.getBoolean("bailout");
        startOfDayLedgerIndex = state.optInt("ledgerIndex", 0);
        startOfDayNetWorth = state.isNull("startNav") ? null : new Money(new BigDecimal(state.getString("startNav")), Currency.IDR);
        missions.clear(); initializeMissions(); sessionReports.clear();
        org.json.JSONArray saved = state.getJSONArray("missions");
        for (int i = 0; i < saved.length(); i++) {
            org.json.JSONObject p = saved.getJSONObject(i);
            CareerMission m = missions.get(p.getString("id"));
            if (m == null) throw new IllegalArgumentException("Misi save tidak dikenal");
            if (p.getBoolean("completed")) m.markCompleted();
            if (p.getBoolean("claimed")) m.claim();
        }
        org.json.JSONArray reports = state.optJSONArray("reports");
        if (state.has("reports") && reports == null) throw new org.json.JSONException("Daftar laporan tidak valid");
        if (reports != null) for (int i = 0; i < reports.length(); i++) {
            org.json.JSONObject report = reports.getJSONObject(i);
            int fills = report.getInt("fills");
            if (fills < 0) throw new IllegalArgumentException("Jumlah fill laporan tidak valid");
            sessionReports.add(new DailySessionReport(LocalDate.parse(report.getString("date")),
                    new Money(new BigDecimal(report.getString("start")), Currency.IDR),
                    new Money(new BigDecimal(report.getString("end")), Currency.IDR),
                    new Money(new BigDecimal(report.getString("pnl")), Currency.IDR),
                    new Money(new BigDecimal(report.getString("fees")), Currency.IDR),
                    new Money(new BigDecimal(report.getString("salary")), Currency.IDR), fills, report.getString("note")));
        }
    }

    public CareerRank getCurrentRank() { return currentRank; }
    public boolean hasRank(CareerRank required) { return currentRank.getLevel() >= required.getLevel(); }
    public void requireRank(CareerRank required, String feature) {
        if (!hasRank(required)) throw new IllegalStateException(feature + " terbuka pada jabatan " + required.getTitle());
    }
    public int getDaysTraded() { return daysTraded; }
    public int getReputationScore() { return reputationScore; }

    public synchronized void penalizeReputation(int points) {
        if (points < 0) throw new IllegalArgumentException("Poin penalti tidak boleh negatif");
        this.reputationScore = Math.max(5, this.reputationScore - points);
    }

    public synchronized void boostReputation(int points) {
        if (points < 0) throw new IllegalArgumentException("Poin bonus tidak boleh negatif");
        this.reputationScore = Math.min(100, this.reputationScore + points);
    }
    public boolean isBailoutUsed() { return bailoutUsed; }
    public List<CareerMission> getAllMissions() { return new ArrayList<>(missions.values()); }
    public List<DailySessionReport> getSessionReports() { return Collections.unmodifiableList(sessionReports); }

    public void setStartOfDayNetWorth(Money netWorth) {
        this.startOfDayNetWorth = netWorth;
    }

    public CareerMission getMission(String id) {
        return missions.get(id);
    }

    public synchronized void evaluateRecordedTrades(Account account, EconomyLedger ledger) {
        for (com.tradingglobal3d.core.economy.LedgerEntry entry : ledger.getEntriesForAccount(account.getId())) {
            if ((entry.getType() == LedgerEntryType.TRADE_BUY || entry.getType() == LedgerEntryType.TRADE_SELL)
                    && entry.getAmount().isPositive()) {
                completeMission("MIS-01");
                return;
            }
        }
    }

    public void completeMission(String id) {
        CareerMission m = missions.get(id);
        if (m != null) m.markCompleted();
    }

    /**
     * Klaim reward misi secara idempoten dan tercatat di ledger.
     */
    public synchronized Money claimMissionReward(
            String missionId,
            Account account,
            EconomyLedger ledger,
            long timestamp) {

        CareerMission mission = missions.get(missionId);
        if (mission == null) throw new IllegalArgumentException("Misi tidak ditemukan: " + missionId);

        Money reward = mission.claim(); // Melempar exception jika sudah pernah diklaim!
        ledger.recordMutation(
                "REWARD-" + missionId,
                account,
                LedgerEntryType.MISSION_REWARD,
                reward,
                true,
                timestamp,
                "Imbalan penyelesaian misi: " + mission.getTitle()
        );
        this.reputationScore = Math.min(100, this.reputationScore + 10);
        return reward;
    }

    /**
     * Evaluasi promosi kenaikan pangkat karier.
     */
    public synchronized CareerRank evaluatePromotion(Account personalAccount, BigDecimal fxRate) {
        return evaluatePromotion(personalAccount.getTotalPortfolioValue(Currency.IDR, fxRate));
    }

    public synchronized CareerRank evaluatePromotion(Money netWorth) {
        if (netWorth.getCurrency() != Currency.IDR) throw new IllegalArgumentException("Kekayaan karier harus dinilai dalam IDR");
        CareerRank next = currentRank.getNextRank();
        if (next == null) return currentRank;

        boolean eligible = daysTraded >= next.getRequiredDays() && reputationScore >= next.getRequiredReputation()
                && netWorth.getAmount().compareTo(BigDecimal.valueOf(next.getRequiredNetWorth())) >= 0;

        if (eligible) {
            this.currentRank = next;
            this.reputationScore = Math.min(100, this.reputationScore + 15);
        }
        return this.currentRank;
    }

    /**
     * Fasilitas Pemulihan Modal (Bailout) sekali pakai jika bangkrut.
     */
    public synchronized Money requestBailout(Account personalAccount, EconomyLedger ledger, long timestamp) {
        if (bailoutUsed) {
            throw new IllegalStateException("Fasilitas dana pemulihan modal (Bailout) hanya dapat digunakan satu kali seumur karier");
        }
        Money avail = personalAccount.getAvailableCash(Currency.IDR);
        if (avail.getAmount().compareTo(BigDecimal.valueOf(500_000)) > 0 || !personalAccount.getPositions().isEmpty()) {
            throw new IllegalStateException("Fasilitas pemulihan hanya dapat diajukan jika modal kas di bawah Rp 500.000 dan tidak memiliki saham");
        }

        Money bailoutMoney = Money.of(10_000_000, Currency.IDR);
        ledger.recordMutation(
                "BAILOUT-" + personalAccount.getId(),
                personalAccount,
                LedgerEntryType.BAILOUT,
                bailoutMoney,
                true,
                timestamp,
                "Dana Hibah Magang Darurat (Pemulihan Modal)"
        );

        this.bailoutUsed = true;
        this.reputationScore = Math.max(5, this.reputationScore - 15); // Konsekuensi penalti reputasi
        return bailoutMoney;
    }

    /**
     * Menutup sesi hari bursa saat ini, membayarkan gaji, mencatat laporan harian,
     * dan mereset statistik pasar untuk hari bursa berikutnya.
     */
    public synchronized DailySessionReport closeDayAndAdvance(
            GameClock clock,
            Account personalAccount,
            InstrumentCatalog catalog,
            EconomyLedger ledger,
            BigDecimal fxRate,
            long gameTimestamp) {
        return closeDayAndAdvance(clock, personalAccount, catalog, ledger, fxRate, gameTimestamp, null);
    }

    public synchronized DailySessionReport closeDayAndAdvance(
            GameClock clock,
            Account personalAccount,
            InstrumentCatalog catalog,
            EconomyLedger ledger,
            BigDecimal fxRate,
            long gameTimestamp, Money consolidatedNetWorth) {

        // Existing clock includes post-closing through 16:00; never settle a future day on demand.
        if (!clock.getCurrentTime().isAfter(java.time.LocalTime.of(16, 0, 59)))
            throw new IllegalStateException("Penutupan hari belum tersedia sebelum sesi selesai (16:01 WIB pada aturan sementara)");
        LocalDate today = clock.getCurrentDate();
        Money endNetWorth = consolidatedNetWorth != null ? consolidatedNetWorth : personalAccount.getTotalPortfolioValue(Currency.IDR, fxRate);
        Money startNetWorth = (startOfDayNetWorth != null) ? startOfDayNetWorth : endNetWorth;
        Money externalFlows = Money.zero(Currency.IDR);
        Money fees = Money.zero(Currency.IDR);
        int buyEntries = 0;
        java.util.List<com.tradingglobal3d.core.economy.LedgerEntry> entries = ledger.getEntries();
        for (int i = Math.min(startOfDayLedgerIndex, entries.size()); i < entries.size(); i++) {
            com.tradingglobal3d.core.economy.LedgerEntry entry = entries.get(i);
            if (!entry.getAccountId().equals(personalAccount.getId())) continue;
            Money value = entry.getCurrency() == Currency.IDR ? entry.getAmount()
                    : new Money(entry.getAmount().getAmount().multiply(fxRate), Currency.IDR);
            switch (entry.getType()) {
                case DEPOSIT: case WITHDRAWAL: case MISSION_REWARD: case SALARY: case BAILOUT:
                case MANAGEMENT_FEE: case PERFORMANCE_FEE:
                    externalFlows = entry.isCredit() ? externalFlows.add(value) : externalFlows.subtract(value);
                    break;
                case LOAN_DISBURSEMENT: case LOAN_REPAYMENT:
                    if (consolidatedNetWorth == null)
                        externalFlows = entry.isCredit() ? externalFlows.add(value) : externalFlows.subtract(value);
                    break;
                case TRANSACTION_FEE:
                    if (!entry.isCredit()) fees = fees.add(value);
                    break;
                case TRADE_BUY: case TRADE_SELL:
                    buyEntries++;
                    break;
                default: break;
            }
        }
        Money pnl = endNetWorth.subtract(startNetWorth).subtract(externalFlows);

        // Bayar gaji harian jika ada (Senior Analyst ke atas)
        Money salary = currentRank.getDailySalary();
        if (salary.isPositive()) {
            ledger.recordMutation(
                    "SALARY-DAY-" + daysTraded + "-" + today,
                    personalAccount,
                    LedgerEntryType.SALARY,
                    salary,
                    true,
                    gameTimestamp,
                    "Gaji Harian Jabatan " + currentRank.getTitle()
            );
        }

        DailySessionReport report = new DailySessionReport(
                today,
                startNetWorth,
                endNetWorth,
                pnl,
                fees,
                salary,
                buyEntries,
                "Sesi hari bursa " + today + " ditutup. Status: " + currentRank.getTitle()
        );
        sessionReports.add(report);

        // Majukan siklus hari bursa
        this.daysTraded++;
        beginDay(personalAccount, fxRate, ledger);

        // Reset batas harian instrumen (open, high, low, volume)
        for (Instrument inst : catalog.getAllInstruments()) {
            inst.startNewTradingDay();
        }

        return report;
    }
}
