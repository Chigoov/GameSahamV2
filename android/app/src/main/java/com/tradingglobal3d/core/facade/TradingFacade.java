package com.tradingglobal3d.core.facade;

import com.tradingglobal3d.core.career.CareerManager;
import com.tradingglobal3d.core.career.CareerRank;
import com.tradingglobal3d.core.career.DailySessionReport;
import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.clock.MarketSession;
import com.tradingglobal3d.core.corporate.*;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.FxExchangeService;
import com.tradingglobal3d.core.economy.FxQuote;
import com.tradingglobal3d.core.economy.LedgerEntry;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.firm.ClientMandate;
import com.tradingglobal3d.core.firm.FirmManager;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketIndex;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.multiasset.*;
import com.tradingglobal3d.core.npc.MarketEngine;
import com.tradingglobal3d.core.npc.NpcAgent;
import com.tradingglobal3d.core.npc.NpcStrategyType;
import com.tradingglobal3d.core.player.PlayerProgressManager;
import com.tradingglobal3d.core.replay.*;
import com.tradingglobal3d.core.research.*;
import com.tradingglobal3d.core.risk.*;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.storage.SaveState;
import com.tradingglobal3d.core.trading.*;
import com.tradingglobal3d.core.funds.MutualFund;
import com.tradingglobal3d.core.funds.MutualFundManager;
import com.tradingglobal3d.core.loans.LoanContract;
import com.tradingglobal3d.core.loans.InstallmentLoanManager;
import com.tradingglobal3d.core.career.PeriodicMission;
import com.tradingglobal3d.core.career.PeriodicMissionManager;
import com.tradingglobal3d.core.league.LeagueCompetitor;
import com.tradingglobal3d.core.league.LeagueManager;
import com.tradingglobal3d.core.player.InvestorArchetypeService;
import com.tradingglobal3d.core.player.PlayerProfileManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Fasade Utama (Single Source of Truth) untuk domain simulasi trading saham 2D.
 * Menghubungkan GameClock, Accounts, EconomyLedger, dan SaveManager.
 */
public final class TradingFacade {
    private GameMode gameMode;
    private String runId = java.util.UUID.randomUUID().toString();
    private Money weeklyBaselineNetWorth = null;
    private com.tradingglobal3d.core.career.PerformancePeriod weeklyPerformance;
    private com.tradingglobal3d.core.career.PerformancePeriod runPerformance;
    private JSONArray performanceHistory = new JSONArray();
    private static final String RULES_VERSION = "2026.10.20.2";
    private boolean recoveryRequired;

    public synchronized boolean startOrResume() {
        if (saveManager.getSaveFile().exists() || saveManager.getBackupFile().exists()) {
            boolean loaded = load();
            recoveryRequired = !loaded;
            if (loaded) {
                long now = System.currentTimeMillis();
                if (lastRealTimeAnchor > 0 && now > lastRealTimeAnchor) {
                    reconcileOfflineElapsed(now - lastRealTimeAnchor);
                }
                this.lastRealTimeAnchor = System.currentTimeMillis();
                if (clock != null) clock.setForeground(true);
            }
            return loaded;
        }
        initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        return true;
    }
    private GameClock clock;
    private long lastRealTimeAnchor = System.currentTimeMillis();
    private JSONObject lastOfflineCatchupReport;
    private final Map<String, Account> accounts = new HashMap<>();
    private String personalAccountId;
    private final EconomyLedger ledger = new EconomyLedger();
    private SaveManager saveManager;
    private final SaveManager primarySaveManager;
    private InstrumentCatalog catalog = new InstrumentCatalog();
    private JSONObject initialCatalogState;
    private final MarketEngine marketEngine = new MarketEngine();
    private final FxExchangeService fxService = new FxExchangeService();
    private final OrderManager orderManager = new OrderManager();
    private final CareerManager careerManager = new CareerManager();
    private final FirmManager firmManager = new FirmManager();
    private final ResearchService researchService = new ResearchService();
    private final FundBuilderService fundBuilder = new FundBuilderService();
    private final CorporateActionService corporateService = new CorporateActionService();
    private final RiskComplianceService riskService = new RiskComplianceService();
    private final HistoricalReplayEngine replayEngine = new HistoricalReplayEngine();
    private final PlayerProgressManager progressManager = new PlayerProgressManager();
    private final com.tradingglobal3d.core.business.CompanyManager companyManager = new com.tradingglobal3d.core.business.CompanyManager();
    private final com.tradingglobal3d.core.collection.CollectionManager collectionManager = new com.tradingglobal3d.core.collection.CollectionManager();
    private final MutualFundManager mutualFundManager = new MutualFundManager();
    private final InstallmentLoanManager installmentLoanManager = new InstallmentLoanManager();
    private final PeriodicMissionManager periodicMissionManager = new PeriodicMissionManager();
    private final LeagueManager leagueManager = new LeagueManager();
    private final PlayerProfileManager playerProfileManager = new PlayerProfileManager();
    private final Map<String, JSONObject> dayCloseReceipts = new LinkedHashMap<>();
    private final Map<String, JSONObject> skipDayReceipts = new LinkedHashMap<>();
    public static final int GAME_SECONDS_PER_REAL_SECOND = 288;

    public synchronized JSONObject closeDay(String requestId) throws JSONException {
        if (requestId == null || requestId.trim().isEmpty()) throw new IllegalArgumentException("ID penutupan wajib diisi");
        if (dayCloseReceipts.containsKey(requestId)) return dayCloseReceipts.get(requestId);
        DailySessionReport report = closeDayAndAdvance();
        JSONObject receipt = new JSONObject().put("success", true).put("date", report.getDate().toString())
                .put("startingNetWorth", report.getStartingNetWorth().format())
                .put("endingNetWorth", report.getEndingNetWorth().format()).put("investmentPnL", report.getDailyPnL().format())
                .put("fees", report.getFeesPaid().format()).put("salary", report.getSalaryReceived().format())
                .put("fills", report.getTotalFillsCount()).put("note", report.getSummaryNote());
        dayCloseReceipts.put(requestId, receipt);
        return receipt;
    }

    public TradingFacade(SaveManager saveManager) {
        this.primarySaveManager = saveManager;
        this.saveManager = Objects.requireNonNull(saveManager);
    }

    public synchronized long getLastRealTimeAnchor() { return lastRealTimeAnchor; }
    public synchronized JSONObject getLastOfflineCatchupReport() { return lastOfflineCatchupReport; }

    public synchronized JSONObject reconcileOfflineElapsed(long elapsedRealMs) {
        if (elapsedRealMs <= 0 || clock == null) return null;
        Money startNetWorth = getPersonalNetWorth();
        LocalDate startDate = clock.getCurrentDate();

        // Di luar aplikasi / ditutup: selalu rasio inactive 24x (1 jam nyata = 1 hari game)
        long gameSeconds = clock.advanceRealMs(elapsedRealMs, false);
        if (gameSeconds > 0) {
            advanceWorldSeconds(gameSeconds);
        }

        Money endNetWorth = getPersonalNetWorth();
        LocalDate endDate = clock.getCurrentDate();

        JSONObject report = new JSONObject();
        try {
            report.put("realElapsedMs", elapsedRealMs);
            report.put("gameSecondsAdvanced", gameSeconds);
            report.put("startDate", startDate.toString());
            report.put("endDate", endDate.toString());
            report.put("startingNetWorth", startNetWorth.format());
            report.put("endingNetWorth", endNetWorth.format());
            report.put("pnl", endNetWorth.subtract(startNetWorth).format());
            report.put("timestamp", System.currentTimeMillis());
        } catch (JSONException ignored) {}
        this.lastOfflineCatchupReport = report;
        this.lastRealTimeAnchor = System.currentTimeMillis();
        return report;
    }

    public synchronized JSONObject onAppLifecycleChanged(boolean foreground) {
        if (clock == null) return null;
        long now = System.currentTimeMillis();
        JSONObject result = null;
        if (!foreground) {
            this.lastRealTimeAnchor = now;
            clock.setForeground(false);
            try {
                save();
            } catch (Exception ignored) {}
        } else {
            if (!clock.isForeground() && lastRealTimeAnchor > 0 && now > lastRealTimeAnchor) {
                result = reconcileOfflineElapsed(now - lastRealTimeAnchor);
            }
            this.lastRealTimeAnchor = now;
            clock.setForeground(true);
        }
        return result;
    }

    public synchronized PlayerProgressManager getProgressManager() {
        return progressManager;
    }

    public synchronized boolean recordResearchRead(String articleId) {
        if (articleId == null) return false;
        for (com.tradingglobal3d.core.research.NewsArticle article : getNewsFeed()) {
            if (article.getId().equals(articleId)) {
                boolean firstRead = progressManager.onResearchRead(articleId, clock.getGameTimestamp());
                periodicMissionManager.updateCalendar(clock.getCurrentDate(), careerManager.getCurrentRank());
                periodicMissionManager.recordActivity("READ:" + articleId, "READ_NEWS", 1, clock.getCurrentDate());
                return firstRead;
            }
        }
        return false;
    }

    public synchronized HistoricalReplayEngine getReplayEngine() {
        return replayEngine;
    }

    public synchronized RiskComplianceService getRiskService() {
        return riskService;
    }

    public synchronized CorporateActionService getCorporateService() {
        return corporateService;
    }

    public synchronized com.tradingglobal3d.core.business.CompanyManager getCompanyManager() {
        return companyManager;
    }

    public synchronized com.tradingglobal3d.core.business.OperatingCompany foundCompany(
            String companyId, String name, String sector, Money initialCapital) {
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Akun personal belum dibuat");
        long now = clock != null ? clock.getGameTimestamp() : System.currentTimeMillis();
        var existing = companyManager.getCompany(companyId);
        if (existing != null) throw new IllegalStateException("Perusahaan sudah didirikan; buka daftar perusahaan");
        var company = companyManager.foundCompany(companyId, name, sector, personal, initialCapital, ledger, accounts, now);
        try { save(); } catch (IOException | JSONException error) { throw new IllegalStateException("Pendirian belum tersimpan; simpan ulang melalui pengaturan", error); }
        return company;
    }

    public synchronized void transferCompanyCapital(String companyId, boolean toCompany, Money amount) {
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Akun personal belum dibuat");
        com.tradingglobal3d.core.business.OperatingCompany comp = companyManager.getCompany(companyId);
        if (comp == null) throw new IllegalArgumentException("Perusahaan tidak ditemukan: " + companyId);
        Account compAccount = accounts.get(comp.getAccountId());
        if (compAccount == null) throw new IllegalStateException("Akun perusahaan belum tersedia");
        long now = clock != null ? clock.getGameTimestamp() : System.currentTimeMillis();
        companyManager.transferCapital(companyId, toCompany, personal, compAccount, amount, ledger, now);
    }

    public synchronized JSONObject getCompaniesJson() throws JSONException {
        JSONArray companies = new JSONArray();
        for (var company : companyManager.getAllCompanies()) {
            JSONObject item = company.toJsonObject();
            Account account = accounts.get(company.getAccountId());
            if (account == null) throw new IllegalStateException("Akun perusahaan tidak tersedia");
            item.put("cash", account.getTotalCash(Currency.IDR).getAmount().toPlainString());
            item.put("employeeCount", company.getEmployees().size());
            String ceoName = "Belum ada CEO";
            for (var employee : company.getEmployees()) if (employee.getRole() == com.tradingglobal3d.core.business.Employee.Role.CEO) ceoName = employee.getName();
            companies.put(item.put("ceoName", ceoName));
        }
        return new JSONObject().put("success", true).put("companies", companies);
    }

    public synchronized JSONObject getCompanyStaff(String companyId) throws JSONException {
        if (getPersonalAccount() == null) throw new IllegalStateException("Permainan belum siap");
        return companyManager.getStaff(companyId);
    }

    public synchronized JSONObject previewCompanyStaffAction(String companyId, String action, String employeeId, String role) throws JSONException {
        careerManager.requireRank(CareerRank.NOVICE_TRADER, "Pengelolaan pegawai");
        return companyManager.previewStaffAction(companyId, action, employeeId, role);
    }

    public synchronized JSONObject manageCompanyStaff(String requestId, String companyId, String action, String employeeId, String role, long expectedVersion) throws JSONException {
        careerManager.requireRank(CareerRank.NOVICE_TRADER, "Pengelolaan pegawai");
        var company = companyManager.getCompany(companyId);
        if (getPersonalAccount() == null || company == null) throw new IllegalArgumentException("Perusahaan tidak ditemukan");
        Account companyAccount = accounts.get(company.getAccountId());
        JSONObject receipt = companyManager.executeStaffAction(requestId, companyId, action, employeeId, role,
                companyAccount, ledger, clock.getGameTimestamp(), expectedVersion);
        persistFinancialMutation();
        return receipt;
    }

    public synchronized com.tradingglobal3d.core.collection.CollectionManager getCollectionManager() {
        return collectionManager;
    }

    public synchronized JSONObject transactCollection(String requestId, String itemId, boolean buy) throws JSONException {
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Permainan belum siap");
        JSONObject receipt = buy ? collectionManager.buyItem(requestId, itemId, personal, ledger, careerManager, clock.getGameTimestamp())
                : collectionManager.sellItem(requestId, itemId, personal, ledger, careerManager, clock.getGameTimestamp());
        persistFinancialMutation();
        return receipt;
    }

    public synchronized com.tradingglobal3d.core.collection.CollectibleItem buyCollection(String itemId) {
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Akun personal belum dibuat");
        long now = clock != null ? clock.getGameTimestamp() : System.currentTimeMillis();
        return collectionManager.buyItem(itemId, personal, ledger, careerManager, now);
    }

    public synchronized com.tradingglobal3d.core.collection.CollectibleItem sellCollection(String itemId) {
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Akun personal belum dibuat");
        long now = clock != null ? clock.getGameTimestamp() : System.currentTimeMillis();
        return collectionManager.sellItem(itemId, personal, ledger, careerManager, now);
    }

    public synchronized MutualFundManager getMutualFundManager() { return mutualFundManager; }
    public synchronized InstallmentLoanManager getInstallmentLoanManager() { return installmentLoanManager; }
    public synchronized PeriodicMissionManager getPeriodicMissionManager() { return periodicMissionManager; }
    public synchronized LeagueManager getLeagueManager() { return leagueManager; }
    public synchronized PlayerProfileManager getPlayerProfileManager() { return playerProfileManager; }

    public synchronized FundBuilderService getFundBuilder() {
        return fundBuilder;
    }

    public synchronized ResearchService getResearchService() {
        if (clock != null) researchService.hasAccess(TerminalTier.BASIC, clock.getGameTimestamp());
        return researchService;
    }

    public synchronized FirmManager getFirmManager() {
        return firmManager;
    }

    public synchronized JSONObject getFirmSnapshot() throws JSONException {
        Money aum = Money.zero(Currency.IDR);
        int activeClients = 0;
        JSONArray mandates = new JSONArray();
        for (ClientMandate mandate : firmManager.getAllMandates()) {
            Account account = accounts.get(mandate.getClientAccountId());
            if (mandate.getStatus() == ClientMandate.MandateStatus.ACTIVE) {
                if (account == null) throw new IllegalStateException("Akun mandat aktif tidak tersedia");
                aum = aum.add(account.getTotalPortfolioValue(Currency.IDR, fxService.getMidRate()));
                activeClients++;
            }
            mandates.put(new JSONObject().put("id", mandate.getId()).put("clientName", mandate.getClientName())
                    .put("status", mandate.getStatus().name()).put("statusLabel", mandate.getStatus().getLabel())
                    .put("accountId", mandate.getClientAccountId() == null ? JSONObject.NULL : mandate.getClientAccountId())
                    .put("riskProfile", mandate.getRiskProfile().name()).put("benchmark", mandate.getBenchmarkIndexId())
                    .put("initialDeposit", mandate.getInitialDeposit().format()).put("highWaterMark", mandate.getHighWaterMark().format())
                    .put("trust", mandate.getTrustScore()).put("managementFeeAnnualRate", mandate.getManagementFeeAnnualRate().toPlainString())
                    .put("performanceFeeRate", mandate.getPerformanceFeeRate().toPlainString())
                    .put("maxConcentration", mandate.getRiskProfile().getMaxConcentrationPerStock())
                    .put("minCashReserve", mandate.getRiskProfile().getMinCashReserve()));
        }
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Akun pribadi belum tersedia");
        return new JSONObject().put("success", true).put("aumIdr", aum.format())
                .put("personalCashIdr", personal.getTotalCash(Currency.IDR).format()).put("activeClientCount", activeClients)
                .put("valuationRateUsdIdr", fxService.getMidRate().toPlainString()).put("timestamp", System.currentTimeMillis())
                .put("parametersProvisional", true).put("mandates", mandates);
    }

    public synchronized CareerManager getCareerManager() {
        return careerManager;
    }

    public synchronized MarketEngine getMarketEngine() {
        return marketEngine;
    }

    public synchronized FxExchangeService getFxService() {
        return fxService;
    }

    public synchronized OrderManager getOrderManager() {
        return orderManager;
    }

    public synchronized InstrumentCatalog getCatalog() {
        return catalog;
    }

    public synchronized void setCatalog(InstrumentCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog);
        try { initialCatalogState = catalog.exportState(); }
        catch (JSONException error) { throw new IllegalArgumentException("Katalog tidak dapat disiapkan", error); }
        Account mm = accounts.get("ACC-NPC-MM-01");
        if (mm != null) {
            for (Instrument inst : catalog.getActiveUniverse()) {
                if (mm.getPosition(inst.getId()) == null && mm.getPosition(inst.getTicker()) == null) {
                    long initialShares = inst.getLotSize() * 200L;
                    Position p = new Position(inst.getTicker(), inst.getExchange().name(), inst.getCurrency(),
                            initialShares, inst.getCurrentPrice().getAmount(), inst.getCurrentPrice().getAmount());
                    mm.addPosition(p);
                }
            }
        }
    }

    public static TradingFacade createInMemory() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "tradingglobal3d_test_" + System.nanoTime());
        return new TradingFacade(SaveManager.forDirectory(tempDir));
    }

    public synchronized void initializeNewGame(GameMode mode, BigDecimal initialIdrCapital) {
        if (initialIdrCapital == null || initialIdrCapital.signum() <= 0)
            throw new IllegalArgumentException("Modal awal harus positif");
        this.gameMode = Objects.requireNonNull(mode);
        this.clock = GameClock.createDefault();
        this.accounts.clear();
        this.ledger.clear();
        this.dayCloseReceipts.clear();
        this.skipDayReceipts.clear();
        this.runId = java.util.UUID.randomUUID().toString();
        this.firmManager.reset();
        this.corporateService.reset();
        this.riskService.reset();
        this.fundBuilder.reset();
        this.orderManager.reset();
        this.marketEngine.reset();
        this.progressManager.reset();
        this.careerManager.reset();
        this.fxService.reset();
        this.replayEngine.reset();
        this.companyManager.reset();
        this.collectionManager.reset();
        this.lastOfflineCatchupReport = null;
        this.lastRealTimeAnchor = System.currentTimeMillis();
        this.mutualFundManager.reset();
        this.installmentLoanManager.reset();
        this.periodicMissionManager.reset();
        this.leagueManager.reset();
        this.playerProfileManager.reset();
        if (this.clock != null) {
            this.periodicMissionManager.updateCalendar(this.clock.getCurrentDate(), careerManager.getCurrentRank());
        }
        this.researchService.resetForGame(clock.getGameTimestamp());
        if (initialCatalogState != null) {
            try { catalog.restoreState(initialCatalogState); }
            catch (JSONException error) { throw new IllegalStateException("Katalog awal tidak dapat dipulihkan", error); }
        }

        // 1. Buat Akun Pribadi Pemain
        this.personalAccountId = "ACC-PERSONAL-001";
        Account personal = new Account(personalAccountId, "Portofolio Pribadi", AccountType.PERSONAL, null);
        accounts.put(personalAccountId, personal);

        // 2. Catat Modal Awal di Ledger
        Money initialMoney = new Money(initialIdrCapital != null ? initialIdrCapital : BigDecimal.valueOf(50_000_000), Currency.IDR);
        ledger.recordMutation(
                "INIT-CAPITAL-" + personalAccountId,
                personal,
                LedgerEntryType.INITIAL_CAPITAL,
                initialMoney,
                true,
                clock.getGameTimestamp(),
                "Setoran modal awal profil baru"
        );

        // 3. Buat dan Danai Agen NPC Market Maker (Likuiditas Awal)
        String mmAccountId = "ACC-NPC-MM-01";
        Account mmAccount = new Account(mmAccountId, "Market Maker Utama", AccountType.FIRM, null);
        accounts.put(mmAccountId, mmAccount);
        Money mmCashIdr = Money.of(5_000_000_000L, Currency.IDR);
        Money mmCashUsd = Money.of(500_000.0, Currency.USD);
        ledger.recordMutation("INIT-MM-IDR", mmAccount, LedgerEntryType.INITIAL_CAPITAL, mmCashIdr, true, clock.getGameTimestamp(), "Modal kas IDR Market Maker");
        ledger.recordMutation("INIT-MM-USD", mmAccount, LedgerEntryType.INITIAL_CAPITAL, mmCashUsd, true, clock.getGameTimestamp(), "Modal kas USD Market Maker");

        // Berikan kepemilikan saham awal untuk Market Maker agar bisa memasang antrean Ask
        for (Instrument inst : catalog.getActiveUniverse()) {
            long initialShares = inst.getLotSize() * 200L;
            Position p = new Position(inst.getTicker(), inst.getExchange().name(), inst.getCurrency(),
                    initialShares, inst.getCurrentPrice().getAmount(), inst.getCurrentPrice().getAmount());
            mmAccount.addPosition(p);
        }

        NpcAgent marketMaker = new NpcAgent("NPC-MM-01", "Bandar Likuiditas Prima", NpcStrategyType.MARKET_MAKER, mmAccount);
        marketEngine.registerNpcAgent(marketMaker);
        // Bind collection manager ke loan manager untuk agunan
        installmentLoanManager.bindCollectionManager(collectionManager);

        // Registrasi pool accounts reksa dana schema3
        mutualFundManager.registerPoolAccounts(accounts, ledger, clock.getGameTimestamp());

        careerManager.beginDay(personal, fxService.getMidRate(), ledger);
        weeklyBaselineNetWorth = getPersonalNetWorth();
        weeklyPerformance = new com.tradingglobal3d.core.career.PerformancePeriod(clock.getGameTimestamp(), weeklyBaselineNetWorth,
                ledger.getEntriesForAccount(personalAccountId).size());
        runPerformance = weeklyPerformance;
        performanceHistory = new JSONArray();
        recoveryRequired = false;
    }

    public synchronized JSONObject startHistoricalReplay(String datasetId, BigDecimal initialCapital) {
        String ds = com.tradingglobal3d.core.market.HistoricalDatasetLoader.canonicalDatasetId(datasetId == null ? "US:AAPL" : datasetId);
        List<com.tradingglobal3d.core.market.HistoricalBar> bars;
        try {
            bars = com.tradingglobal3d.core.market.HistoricalDatasetLoader.loadDataset(ds);
        } catch (IOException e) {
            throw new IllegalStateException("Gagal memuat dataset replay: " + ds + " (" + e.getMessage() + ")", e);
        }
        if (bars.isEmpty()) {
            throw new IllegalStateException("Dataset replay kosong: " + ds);
        }

        try { if (clock != null && getPersonalAccount() != null) save(); }
        catch (IOException | JSONException error) { throw new IllegalStateException("Simpan progres aktif sebelum membuka replay", error); }
        saveManager = new SaveManager(primarySaveManager.getSaveFile().getParentFile(), "save_replay_v2.json");
        initializeNewGame(GameMode.HISTORICAL_REPLAY, initialCapital != null ? initialCapital : BigDecimal.valueOf(50_000_000));
        replayEngine.startReplay(ds, bars);

        com.tradingglobal3d.core.market.HistoricalBar firstBar = bars.get(0);
        this.clock = new GameClock(firstBar.getDate(), LocalTime.of(9, 0, 0));

        Instrument inst = catalog.getById(ds);
        if (inst != null) {
            inst.updatePrice(new Money(firstBar.getOpen(), inst.getCurrency()), 0);
        }
        weeklyBaselineNetWorth = getPersonalNetWorth();
        weeklyPerformance = new com.tradingglobal3d.core.career.PerformancePeriod(clock.getGameTimestamp(), weeklyBaselineNetWorth,
                ledger.getEntriesForAccount(personalAccountId).size());
        runPerformance = weeklyPerformance;

        JSONObject result = new JSONObject();
        try {
            result.put("success", true);
            result.put("mode", GameMode.HISTORICAL_REPLAY.name());
            result.put("datasetId", ds);
            result.put("startDate", firstBar.getDate().toString());
            result.put("totalBars", bars.size());
            result.put("initialPrice", firstBar.getOpen().toPlainString());
            result.put("initialCapital", (initialCapital != null ? initialCapital : BigDecimal.valueOf(50_000_000)).toPlainString());
            result.put("message", "Replay historis " + ds + " berhasil dimulai dari tanggal " + firstBar.getDate());
        } catch (JSONException ignored) {}
        return result;
    }

    public synchronized JSONObject resumeSavedMode(String mode) throws JSONException, IOException {
        GameMode selected = GameMode.valueOf(mode);
        SaveManager target = selected == GameMode.HISTORICAL_REPLAY
                ? new SaveManager(primarySaveManager.getSaveFile().getParentFile(), "save_replay_v2.json") : primarySaveManager;
        if (!target.getSaveFile().isFile() && !target.getBackupFile().isFile())
            throw new IllegalStateException("Belum ada progres tersimpan untuk mode ini");
        save();
        SaveManager previous = saveManager;
        saveManager = target;
        if (!load()) { saveManager = previous; throw new IllegalStateException("Progres mode tidak dapat divalidasi"); }
        return new JSONObject(getSnapshotJson()).put("success", true);
    }

    public synchronized void startInternalGame(BigDecimal capital) {
        saveManager = primarySaveManager;
        initializeNewGame(GameMode.INTERNAL_MARKET, capital);
    }

    public synchronized String setClockState(boolean playing, int speed) throws JSONException {
        if (clock == null || recoveryRequired) throw new IllegalStateException("Permainan belum dapat dijalankan");
        clock.setSpeedMultiplier(speed);
        if (playing) clock.play(); else clock.pause();
        return getSnapshotJson();
    }

    public synchronized GameMode getGameMode() { return gameMode; }
    public synchronized GameClock getClock() { return clock; }
    public synchronized EconomyLedger getLedger() { return ledger; }
    public synchronized SaveManager getSaveManager() { return saveManager; }

    public synchronized Account getPersonalAccount() {
        return accounts.get(personalAccountId);
    }

    public synchronized Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    public synchronized List<Account> getAllAccounts() {
        return Collections.unmodifiableList(new ArrayList<>(accounts.values()));
    }

    public synchronized Account createClientAccount(String name, String clientMandateId, Money initialDeposit) {
        careerManager.requireRank(CareerRank.PORTFOLIO_MANAGER, "Mandat klien");
        String id = "ACC-CLIENT-" + (accounts.size() + 1);
        Account clientAcc = new Account(id, name, AccountType.CLIENT, clientMandateId);
        accounts.put(id, clientAcc);

        if (initialDeposit != null && initialDeposit.isPositive()) {
            ledger.recordMutation(
                    "MANDATE-DEPOSIT-" + id,
                    clientAcc,
                    LedgerEntryType.DEPOSIT,
                    initialDeposit,
                    true,
                    System.currentTimeMillis(),
                    "Setoran modal awal mandat klien " + name
            );
        }
        return clientAcc;
    }

    public synchronized LedgerEntry deposit(String requestId, String accountId, Money amount, String description) {
        Account acc = accounts.get(accountId);
        if (acc == null) throw new IllegalArgumentException("Akun tidak ditemukan: " + accountId);
        return ledger.recordMutation(requestId, acc, LedgerEntryType.DEPOSIT, amount, true, System.currentTimeMillis(), description);
    }

    public synchronized LedgerEntry withdraw(String requestId, String accountId, Money amount, String description) {
        Account acc = accounts.get(accountId);
        if (acc == null) throw new IllegalArgumentException("Akun tidak ditemukan: " + accountId);
        return ledger.recordMutation(requestId, acc, LedgerEntryType.WITHDRAWAL, amount, false, System.currentTimeMillis(), description);
    }

    public synchronized JSONObject previewOrder(Order order) throws JSONException {
        Account account = accounts.get(order.getAccountId());
        Instrument instrument = catalog.getById(order.getInstrumentId());
        if (account == null || instrument == null) return new JSONObject().put("success", false).put("message", "Akun atau instrumen tidak ditemukan");
        if (account.getType() == AccountType.CLIENT && !careerManager.hasRank(CareerRank.PORTFOLIO_MANAGER))
            return new JSONObject().put("success", false).put("message", "Trading klien terbuka pada jabatan Portfolio Manager");
        if (account.getType() == AccountType.CLIENT && account.getClientMandateId() != null) {
            ClientMandate mandate = firmManager.getMandate(account.getClientMandateId());
            if (mandate != null) {
                FirmManager.ValidationResult val = firmManager.validateOrder(mandate, account, order, instrument, fxService.getMidRate());
                if (!val.isValid) {
                    return new JSONObject().put("success", false).put("message", "Ditolak Mandat Klien: " + val.reason);
                }
            }
        }
        JSONObject preview = marketEngine.getMatchingEngine().previewOrder(order, account, instrument,
                marketEngine.getOrCreateBook(instrument), System.currentTimeMillis());
        return preview.put("mode", gameMode.getDisplayName());
    }

    public synchronized MatchingEngine.OrderResult submitOrder(Order order) {
        Order existing = orderManager.getOrderByRequest(order.getRequestId());
        if (existing != null) {
            if (!existing.getAccountId().equals(order.getAccountId())
                    || !existing.getInstrumentId().equals(order.getInstrumentId())
                    || existing.getSide() != order.getSide() || existing.getType() != order.getType()
                    || existing.getTotalQuantity() != order.getTotalQuantity()
                    || !Objects.equals(existing.getLimitPrice(), order.getLimitPrice())
                    || !Objects.equals(existing.getTriggerPrice(), order.getTriggerPrice())) {
                return new MatchingEngine.OrderResult(existing, false,
                        "ID permintaan sudah digunakan dengan rincian order berbeda; order asli tidak diubah", Collections.emptyList());
            }
            return new MatchingEngine.OrderResult(existing, existing.getStatus() != OrderStatus.REJECTED,
                    "Status permintaan yang sudah diterima", Collections.emptyList());
        }
        Account acc = accounts.get(order.getAccountId());
        if (acc == null) throw new IllegalArgumentException("Akun tidak ditemukan: " + order.getAccountId());
        if (acc.getType() == AccountType.CLIENT) careerManager.requireRank(CareerRank.PORTFOLIO_MANAGER, "Trading klien");
        Instrument inst = catalog.getById(order.getInstrumentId());
        if (inst == null) throw new IllegalArgumentException("Instrumen tidak ditemukan: " + order.getInstrumentId());

        String validationError = MatchingEngine.validateOrder(order, acc, inst);
        if (validationError != null) {
            order.setStatus(OrderStatus.REJECTED);
            orderManager.registerOrder(order);
            return new MatchingEngine.OrderResult(order, false, validationError, Collections.emptyList());
        }

        // Jika akun adalah akun klien, validasi terhadap hard limit mandat
        if (acc.getType() == AccountType.CLIENT && acc.getClientMandateId() != null) {
            ClientMandate mandate = firmManager.getMandate(acc.getClientMandateId());
            if (mandate != null) {
                FirmManager.ValidationResult val = firmManager.validateOrder(mandate, acc, order, inst, fxService.getMidRate());
                if (!val.isValid) {
                    mandate.adjustTrust(-5);
                    order.setStatus(OrderStatus.REJECTED);
                    orderManager.registerOrder(order);
                    return new MatchingEngine.OrderResult(order, false, "Ditolak Mandat Klien: " + val.reason, Collections.emptyList());
                }
            }
        }

        orderManager.registerOrder(order);
        if (order.getStatus() == OrderStatus.PENDING_TRIGGER) {
            return new MatchingEngine.OrderResult(order, true, "Order pemicu tersimpan menunggu harga menyentuh trigger", Collections.emptyList());
        }

        if (gameMode == GameMode.HISTORICAL_REPLAY && replayEngine.isReplayActive()) {
            com.tradingglobal3d.core.market.HistoricalBar bar = replayEngine.getCurrentBar();
            if (bar != null) {
                if (replayEngine.canFillOrderOnHistoricalBar(order, bar)) {
                    Money fillPrice = order.getLimitPrice() != null ? order.getLimitPrice() : new Money(bar.getClose(), inst.getCurrency());
                    long fillQty = order.getRemainingQuantity();
                    TradeFill fill = new TradeFill(
                            "FILL-HIST-" + System.nanoTime(),
                            order.getId(),
                            "HIST-MM",
                            order.getSide() == OrderSide.BUY ? acc.getId() : "ACC-NPC-MM-01",
                            order.getSide() == OrderSide.SELL ? acc.getId() : "ACC-NPC-MM-01",
                            inst.getId(),
                            fillPrice,
                            fillQty,
                            clock.getGameTimestamp()
                    );
                    Money gross = fill.getTotalAmount();
                    Money fee = new Money(gross.getAmount().multiply(new BigDecimal("0.0015")), inst.getCurrency());
                    if (order.isBuy()) {
                        Position pos = acc.getPosition(inst.getId());
                        if (pos == null) {
                            pos = new Position(inst.getTicker(), inst.getExchange().name(), inst.getCurrency(), 0, BigDecimal.ZERO, fillPrice.getAmount());
                            acc.addPosition(pos);
                        }
                        pos.addBuy(fillQty, fillPrice.getAmount());
                        ledger.recordMutation("BUY-HIST-" + order.getId(), acc, LedgerEntryType.TRADE_BUY, gross, false, clock.getGameTimestamp(), "Beli replay " + inst.getTicker());
                        ledger.recordMutation("FEE-HIST-" + order.getId(), acc, LedgerEntryType.TRANSACTION_FEE, fee, false, clock.getGameTimestamp(), "Biaya beli replay " + inst.getTicker());
                    } else {
                        Position pos = acc.getPosition(inst.getId());
                        if (pos != null) {
                            pos.addSell(fillQty, fillPrice.getAmount());
                        }
                        ledger.recordMutation("SELL-HIST-" + order.getId(), acc, LedgerEntryType.TRADE_SELL, gross, true, clock.getGameTimestamp(), "Jual replay " + inst.getTicker());
                        ledger.recordMutation("FEE-HIST-" + order.getId(), acc, LedgerEntryType.TRANSACTION_FEE, fee, false, clock.getGameTimestamp(), "Biaya jual replay " + inst.getTicker());
                    }
                    order.applyFill(fillQty);
                    orderManager.registerOrder(order);
                    evaluateRecordedCareerProgress();
                    return new MatchingEngine.OrderResult(order, true, "Order terisi pada candle historis " + bar.getDate(), List.of(fill));
                } else {
                    order.setStatus(OrderStatus.OPEN);
                    orderManager.registerOrder(order);
                    return new MatchingEngine.OrderResult(order, true, "Order tersimpan menunggu harga bar historis menyentuh limit", Collections.emptyList());
                }
            }
        }

        MatchingEngine.OrderResult result = marketEngine.submitOrder(order, acc, inst, ledger, accounts, clock.getGameTimestamp());
        evaluateRecordedCareerProgress();
        return result;
    }

    public synchronized Account acceptClientMandate(String mandateId) {
        ClientMandate mandate = firmManager.getMandate(mandateId);
        if (mandate == null) throw new IllegalArgumentException("Mandat tidak ditemukan: " + mandateId);

        if (mandate.getClientAccountId() != null) {
            Account existing = accounts.get(mandate.getClientAccountId());
            if (existing == null) throw new IllegalStateException("Akun mandat aktif tidak ditemukan; perlu pemulihan");
            return existing;
        }
        Account clientAcc = createClientAccount(mandate.getClientName(), mandate.getId(), mandate.getInitialDeposit());
        mandate.setClientAccountId(clientAcc.getId());
        progressManager.onMandateAccepted(System.currentTimeMillis());
        return clientAcc;
    }

    public synchronized FirmManager.FeeResult collectClientFees(String mandateId) {
        ClientMandate mandate = firmManager.getMandate(mandateId);
        if (mandate == null || mandate.getClientAccountId() == null) {
            throw new IllegalArgumentException("Mandat aktif tidak ditemukan: " + mandateId);
        }
        Account clientAcc = accounts.get(mandate.getClientAccountId());
        Account managerPersonal = getPersonalAccount();
        return firmManager.collectFees(mandate, clientAcc, managerPersonal, ledger, fxService.getMidRate(),
                clock.getCurrentDate().atStartOfDay(java.time.ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli());
    }

    public synchronized void clientDeposit(String mandateId, Money amount) {
        ClientMandate mandate = firmManager.getMandate(mandateId);
        if (mandate == null || mandate.getClientAccountId() == null) {
            throw new IllegalArgumentException("Mandat aktif tidak ditemukan: " + mandateId);
        }
        Account clientAcc = accounts.get(mandate.getClientAccountId());
        firmManager.processClientDeposit(mandate, clientAcc, amount, ledger, System.currentTimeMillis());
    }

    public synchronized boolean clientWithdraw(String mandateId, Money amount) {
        ClientMandate mandate = firmManager.getMandate(mandateId);
        if (mandate == null || mandate.getClientAccountId() == null) {
            throw new IllegalArgumentException("Mandat aktif tidak ditemukan: " + mandateId);
        }
        Account clientAcc = accounts.get(mandate.getClientAccountId());
        return firmManager.processClientWithdrawal(mandate, clientAcc, amount, ledger, System.currentTimeMillis());
    }

    private JSONObject fxPayload(Account account, FxQuote quote, FxQuote.ConversionPreview preview) throws JSONException {
        return new JSONObject().put("success", true).put("accountId", account.getId()).put("accountName", account.getName())
                .put("from", preview.from.name()).put("to", preview.to.name()).put("amount", preview.fromAmount.format())
                .put("quoteId", quote.getQuoteId()).put("createdAt", quote.getCreatedAt()).put("expiresAt", quote.getExpiresAt())
                .put("bid", quote.getBidRate().toPlainString()).put("ask", quote.getAskRate().toPlainString())
                .put("spread", quote.getAskRate().subtract(quote.getBidRate()).toPlainString())
                .put("effectiveRate", preview.effectiveRate.toPlainString()).put("fee", preview.feeAmount.format())
                .put("gross", preview.grossToAmount.format()).put("net", preview.netToAmount.format());
    }

    public synchronized JSONObject previewFx(String accountId, Currency from, Currency to, Money amount) throws JSONException {
        Account account = accounts.get(accountId);
        if (account == null || recoveryRequired) throw new IllegalArgumentException("Akun belum tersedia");
        FxQuote quote = fxService.getOrGenerateQuote(System.currentTimeMillis());
        FxQuote.ConversionPreview preview = quote.calculatePreview(from, to, amount);
        if (account.getAvailableCash(from).isLessThan(amount)) throw new IllegalArgumentException("Kas tersedia tidak cukup untuk konversi");
        return fxPayload(account, quote, preview).put("available", account.getAvailableCash(from).format())
                .put("sourceBalanceAfter", account.getTotalCash(from).subtract(amount).format())
                .put("targetBalanceAfter", account.getTotalCash(to).add(preview.netToAmount).format());
    }

    public synchronized JSONObject confirmFx(String requestId, String quoteId, String accountId, Currency from, Currency to, Money amount) throws JSONException {
        Account account = accounts.get(accountId);
        if (account == null || recoveryRequired) throw new IllegalArgumentException("Akun belum tersedia");
        FxExchangeService.ConversionResult result = fxService.executeReviewedConversion(requestId, quoteId, account, from, to, amount, System.currentTimeMillis(), ledger);
        if (!result.isSuccess) return new JSONObject().put("success", false).put("message", result.message).put("requestId", requestId);
        return fxPayload(account, fxService.getReceiptQuote(requestId), result.preview).put("requestId", requestId)
                .put("status", "SETTLED").put("message", result.message);
    }

    public synchronized FxQuote.ConversionPreview previewFx(Currency from, Currency to, Money amount) {
        FxQuote quote = fxService.getOrGenerateQuote(System.currentTimeMillis());
        return quote.calculatePreview(from, to, amount);
    }

    public synchronized FxExchangeService.ConversionResult convertFx(String requestId, String accountId, Currency from, Currency to, Money amount) {
        Account acc = accounts.get(accountId);
        if (acc == null) throw new IllegalArgumentException("Akun tidak ditemukan: " + accountId);
        FxQuote quote = fxService.getOrGenerateQuote(System.currentTimeMillis());
        return fxService.executeConversion(requestId, acc, quote, from, to, amount, System.currentTimeMillis(), ledger);
    }

    public synchronized boolean cancelOrder(Order order) {
        Account acc = accounts.get(order.getAccountId());
        if (acc == null) return false;
        if (orderManager.cancelPendingTrigger(order.getRequestId())) return true;
        return marketEngine.cancelOrder(order, acc);
    }

    private void evaluateRecordedCareerProgress() {
        Account personal = getPersonalAccount();
        if (personal != null) careerManager.evaluateRecordedTrades(personal, ledger);
        if (personal != null) {
            Set<String> sectors = new HashSet<>();
            for (Position position : personal.getPositions().values()) {
                Instrument instrument = catalog.getById(position.getExchange() + ":" + position.getTicker());
                if (position.getQuantity() > 0 && instrument != null) sectors.add(instrument.getSector());
            }
            if (sectors.size() >= 2) careerManager.completeMission("MIS-02");
            if (personalNetWorth().getAmount().compareTo(BigDecimal.valueOf(75_000_000)) >= 0)
                careerManager.completeMission("MIS-03");
        }
        periodicMissionManager.updateCalendar(clock.getCurrentDate(), careerManager.getCurrentRank());
        for (LedgerEntry entry : ledger.getEntriesForAccount(personalAccountId)) {
            if (entry.getType() != LedgerEntryType.TRADE_BUY && entry.getType() != LedgerEntryType.TRADE_SELL) continue;
            if (entry.getRequestId().startsWith("SUB-FUND-") || entry.getRequestId().startsWith("RED-FUND-")
                    || (entry.getRequestId().startsWith("FUND-") && (entry.getRequestId().contains(":SUB:") || entry.getRequestId().contains(":RED:")))) continue;
            LocalDate date = java.time.Instant.ofEpochMilli(entry.getTimestamp()).atOffset(java.time.ZoneOffset.ofHours(7)).toLocalDate();
            periodicMissionManager.recordActivity("TRADE:" + entry.getId(), "TRADE", 1, date);
        }
        // All execution paths (resting orders, NPC matches and triggers) converge here.
        for (Instrument instrument : catalog.getActiveUniverse()) {
            OrderBook book = marketEngine.getBook(instrument.getId());
            if (book == null) continue;
            for (TradeFill fill : book.getTradeHistory()) {
                boolean sold = personalAccountId.equals(fill.getSellAccountId());
                if (!sold && !personalAccountId.equals(fill.getBuyAccountId())) continue;
                Money net = sold ? fill.getSellerRealizedNetPnL() : null;
                if (net != null && net.getCurrency() == Currency.USD)
                    net = new Money(net.getAmount().multiply(fxService.getMidRate()), Currency.IDR);
                playerProfileManager.recordFillResult(fill.getFillId(), net);
            }
        }
        for (Order order : orderManager.getOrdersForAccount(personalAccountId)) {
            if (order.getFilledQuantity() > 0)
                progressManager.onTradeExecuted(catalog.getById(order.getInstrumentId()).getExchange().name(), clock.getGameTimestamp());
        }
    }

    public synchronized void generateNpcLiquidity() {
        marketEngine.tick(clock.getGameTimestamp(), catalog, ledger, accounts);
        evaluateRecordedCareerProgress();
    }

    public synchronized List<NewsArticle> getNewsFeed() {
        return researchService.getPublishedNews(clock.getGameTimestamp());
    }

    public synchronized List<AnalystReport> getAnalystReports() {
        return researchService.getPublishedReports(clock.getGameTimestamp());
    }

    public synchronized List<CalendarEvent> getCalendar() {
        return careerManager.hasRank(CareerRank.SENIOR_ANALYST) ? researchService.getCalendarEvents() : Collections.emptyList();
    }

    public synchronized boolean subscribeTerminal(TerminalTier tier) {
        if (tier != TerminalTier.BASIC) careerManager.requireRank(CareerRank.SENIOR_ANALYST, "Terminal premium");
        Account personal = getPersonalAccount();
        if (personal == null) return false;
        return researchService.subscribeTerminal(personal, tier, ledger, clock.getGameTimestamp());
    }

    public synchronized FundAllocationPlan saveFundPlan(String planId, String accountId, String name, Map<String, Double> targetWeights, double cashWeight) {
        careerManager.requireRank(CareerRank.PORTFOLIO_MANAGER, "Fund Builder");
        Account acc = accounts.get(accountId);
        if (acc == null) throw new IllegalArgumentException("Akun tidak ditemukan: " + accountId);

        ClientMandate mandate = (acc.getType() == AccountType.CLIENT && acc.getClientMandateId() != null)
                ? firmManager.getMandate(acc.getClientMandateId())
                : null;

        return fundBuilder.saveAllocationPlan(planId, accountId, name, targetWeights, cashWeight, mandate);
    }

    public synchronized List<MatchingEngine.OrderResult> executeFundRebalance(String planId) {
        careerManager.requireRank(CareerRank.PORTFOLIO_MANAGER, "Fund Builder");
        FundAllocationPlan plan = fundBuilder.getPlan(planId);
        if (plan == null) throw new IllegalArgumentException("Plan tidak ditemukan: " + planId);

        Account acc = accounts.get(plan.getAccountId());
        List<MatchingEngine.OrderResult> results = new ArrayList<>();
        if (!plan.getExecutionRequests().isEmpty()) {
            for (String request : plan.getExecutionRequests()) {
                Order order = orderManager.getOrderByRequest(request);
                if (order == null) throw new IllegalStateException("Order rencana tidak ditemukan; perlu pemulihan");
                results.add(new MatchingEngine.OrderResult(order, order.getStatus() != OrderStatus.REJECTED,
                        "Status order rencana yang sudah dikirim", Collections.emptyList()));
            }
        } else {
            List<Order> rebalOrders = fundBuilder.calculateRebalanceOrders(plan, acc, catalog, fxService.getMidRate());
            List<String> requests = new ArrayList<>();
            for (Order order : rebalOrders) requests.add(order.getRequestId());
            plan.setExecutionRequests(requests);
            plan.setStatus(FundAllocationPlan.PlanStatus.EXECUTING);
            for (Order order : rebalOrders) results.add(submitOrder(order));
        }
        boolean allFilled = !results.isEmpty();
        boolean active = false;
        for (MatchingEngine.OrderResult result : results) {
            allFilled &= result.order.getStatus() == OrderStatus.FILLED;
            active |= !result.order.getStatus().isFinal();
        }
        plan.setStatus(allFilled ? FundAllocationPlan.PlanStatus.EXECUTED
                : active ? FundAllocationPlan.PlanStatus.EXECUTING : FundAllocationPlan.PlanStatus.PARTIAL);
        return results;
    }

    public synchronized int executeCashDividend(CorporateAction action) {
        Instrument inst = catalog.getById("IDX:" + action.getTicker());
        if (inst == null) inst = catalog.getById("US:" + action.getTicker());
        return corporateService.executeCashDividend(action, inst, accounts.values(), ledger, System.currentTimeMillis());
    }

    public synchronized void executeStockSplit(CorporateAction action) {
        Instrument inst = catalog.getById("IDX:" + action.getTicker());
        if (inst == null) inst = catalog.getById("US:" + action.getTicker());
        corporateService.executeStockSplit(action, inst, accounts.values());
    }

    public synchronized IpoSubscription orderIpo(String ipoId, long lots) {
        IpoOffering ipo = corporateService.getIpo(ipoId);
        if (ipo == null) throw new IllegalArgumentException("IPO tidak ditemukan: " + ipoId);
        Account personal = getPersonalAccount();
        return corporateService.orderIpo(ipo, personal, lots, System.currentTimeMillis());
    }

    public synchronized void allotIpo(String ipoId, double ratio) {
        IpoOffering ipo = corporateService.getIpo(ipoId);
        if (ipo == null) throw new IllegalArgumentException("IPO tidak ditemukan: " + ipoId);
        corporateService.allotIpo(ipo, ratio, accounts, ledger, System.currentTimeMillis());
    }

    public synchronized void castShareholderVote(String agendaId, ShareholderMeetingAgenda.VoteChoice choice) {
        ShareholderMeetingAgenda agenda = corporateService.getAgenda(agendaId);
        if (agenda == null) throw new IllegalArgumentException("Agenda RUPS tidak ditemukan: " + agendaId);
        Account personal = getPersonalAccount();
        corporateService.castVote(agenda, personal, choice, System.currentTimeMillis());
    }

    public synchronized void takeMarginLoan(Money amount) {
        careerManager.requireRank(CareerRank.PORTFOLIO_MANAGER, "Pinjaman margin");
        Account personal = getPersonalAccount();
        riskService.takeMarginLoan(personal, amount, ledger, System.currentTimeMillis());
    }

    public synchronized void repayMarginLoan(Money amount) {
        Account personal = getPersonalAccount();
        riskService.repayMarginLoan(personal, amount, ledger, System.currentTimeMillis());
    }

    public synchronized ShortPosition openShortPosition(String ticker, long shares) {
        careerManager.requireRank(CareerRank.PORTFOLIO_MANAGER, "Short selling");
        Account personal = getPersonalAccount();
        Instrument inst = catalog.getById("IDX:" + ticker);
        if (inst == null) inst = catalog.getById("US:" + ticker);
        if (inst == null) throw new IllegalArgumentException("Saham tidak ditemukan: " + ticker);
        Account mm = accounts.get("ACC-NPC-MM-01");
        return riskService.openShortPosition(personal, inst, shares, mm, ledger, System.currentTimeMillis());
    }

    public synchronized void closeShortPosition(String ticker, long shares) {
        Account personal = getPersonalAccount();
        Instrument inst = catalog.getById("IDX:" + ticker);
        if (inst == null) inst = catalog.getById("US:" + ticker);
        if (inst == null) throw new IllegalArgumentException("Saham tidak ditemukan: " + ticker);
        Account mm = accounts.get("ACC-NPC-MM-01");
        riskService.closeShortPosition(personal, inst, shares, mm, ledger, System.currentTimeMillis());
    }

    public synchronized RiskComplianceService.MarginHealthReport evaluateMarginHealth() {
        Account personal = getPersonalAccount();
        return riskService.evaluateMarginHealth(personal, catalog, fxService.getMidRate());
    }

    public synchronized boolean executeForcedLiquidation(String ticker) {
        Account personal = getPersonalAccount();
        Instrument inst = catalog.getById("IDX:" + ticker);
        if (inst == null) inst = catalog.getById("US:" + ticker);
        if (inst == null) throw new IllegalArgumentException("Saham tidak ditemukan: " + ticker);
        return riskService.executeForcedLiquidation(personal, inst, marketEngine.isHalted(inst.getId()), ledger, System.currentTimeMillis());
    }

    public synchronized StrategyLabSandbox createStrategySandbox(String sandboxId) {
        Account personal = getPersonalAccount();
        return replayEngine.createSandbox(sandboxId, personal);
    }

    public synchronized StressTestResult runStressTest(StressTestScenario scenario) {
        Account personal = getPersonalAccount();
        Money marginDebt = riskService.getMarginDebt(personal.getId(), Currency.IDR);
        return replayEngine.runStressTest(personal, scenario, catalog, fxService.getMidRate(), marginDebt);
    }

    public synchronized Money claimMission(String missionId) {
        evaluateRecordedCareerProgress();
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Akun personal belum dibuat");
        Money reward = careerManager.claimMissionReward(missionId, personal, ledger, clock.getGameTimestamp());
        evaluateCareerPromotion();
        return reward;
    }

    public synchronized CareerRank evaluateCareerPromotion() {
        Account personal = getPersonalAccount();
        if (personal == null) return careerManager.getCurrentRank();
        return careerManager.evaluatePromotion(personalNetWorth());
    }

    public synchronized Money requestBailoutRecovery() {
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Akun personal belum dibuat");
        return careerManager.requestBailout(personal, ledger, System.currentTimeMillis());
    }

    public synchronized JSONObject restructureDebt(Currency currency) throws JSONException {
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Akun personal belum dibuat");
        Money currentDebt = riskService.getMarginDebt(personal.getId(), currency);
        if (currentDebt.isZero()) {
            throw new IllegalStateException("Tidak ada utang margin untuk direstrukturisasi pada mata uang " + currency);
        }
        long now = clock != null ? clock.getGameTimestamp() : System.currentTimeMillis();
        Money relief = riskService.restructureDebt(personal, currency, now);
        careerManager.penalizeReputation(10);
        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("currency", currency.name());
        res.put("previousDebt", currentDebt.format());
        res.put("debtRelief", relief.format());
        res.put("remainingDebt", riskService.getMarginDebt(personal.getId(), currency).format());
        res.put("message", "Restrukturisasi utang margin disetujui. Sisa utang berkurang tanpa suntikan kas otomatis; reputasi karier disesuaikan.");
        return res;
    }

    public synchronized DailySessionReport closeDayAndAdvance() {
        return settleDay(true);
    }

    private DailySessionReport settleDay(boolean advanceToMorning) {
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Akun personal belum dibuat");
        // Validate before financing, fees or ledger mutations can occur.
        if (!clock.getCurrentTime().isAfter(LocalTime.of(16, 0, 59)))
            throw new IllegalStateException("Penutupan hari belum tersedia sebelum sesi selesai (16:01 WIB pada aturan sementara)");
        for (DailySessionReport existing : careerManager.getSessionReports())
            if (existing.getDate().equals(clock.getCurrentDate())) return existing;
        long now = clock.getGameTimestamp();

        // 1. Bebankan biaya pembiayaan harian (short borrow fee & bunga pinjaman margin)
        riskService.accrueDailyFinancingCosts(personal, catalog, ledger, fxService.getMidRate(), now);

        // Automatic calendar advancement must retain the time of day.
        for (ClientMandate mandate : firmManager.getActiveMandates()) collectClientFees(mandate.getId());
        if (!firmManager.getActiveMandates().isEmpty() && firmManager.getActiveMandates().stream().allMatch(mandate ->
                firmManager.isWithinLimits(mandate, accounts.get(mandate.getClientAccountId()), fxService.getMidRate())))
            progressManager.onCleanSessionClosed(now);

        // Operasional harian perusahaan (payroll pegawai dan pendapatan usaha)
        for (com.tradingglobal3d.core.business.OperatingCompany comp : companyManager.getAllCompanies()) {
            Account compAccount = accounts.get(comp.getAccountId());
            if (compAccount != null) {
                companyManager.processDailyOperations(compAccount, comp, ledger, now);
                periodicMissionManager.recordActivity("COMPANY:" + comp.getId() + ":" + clock.getCurrentDate(), "COMPANY_OPERATION", 1, clock.getCurrentDate());
            }
        }

        // Biaya pemeliharaan harian barang koleksi & properti
        collectionManager.processDailyMaintenance(personal, ledger, now);

        // Akrual hasil reksa dana harian
        mutualFundManager.advanceDay(0.0, ledger, now);

        // Akrual bunga dan penalti harian pinjaman tenor
        installmentLoanManager.processDailyAccrual(now);

        DailySessionReport report = careerManager.closeDayAndAdvance(
                clock, personal, catalog, ledger, fxService.getMidRate(), now, getPersonalNetWorth()
        );
        evaluateRecordedCareerProgress();
        evaluateCareerPromotion();

        careerManager.setStartOfDayNetWorth(getPersonalNetWorth());

        // Misi periodik dan kalender game
        periodicMissionManager.recordActivity("DAY:" + clock.getCurrentDate(), "DAY_SETTLED", 1, clock.getCurrentDate());
        periodicMissionManager.updateCalendar(clock.getCurrentDate(), careerManager.getCurrentRank());

        // Statistik profil pemain
        playerProfileManager.incrementDaysPlayed();
        playerProfileManager.updateNetWorthPeak(getPersonalNetWorth());

        // Evaluasi pencapaian net worth
        progressManager.onNetWorthEvaluated(getPersonalNetWorth().getAmount().longValue(), now);

        if (!advanceToMorning) return report;
        LocalDate nextDate = clock.getCurrentDate().plusDays(1);
        this.clock = new GameClock(nextDate, java.time.LocalTime.of(8, 30, 0));
        processCalendarActions(nextDate);
        return report;
    }

    private void processCalendarActions(LocalDate nextDate) {
        // 1. Update kalender misi dan rollover mingguan liga (D07, D09)
        periodicMissionManager.updateCalendar(nextDate, careerManager.getCurrentRank());
        if (nextDate.getDayOfWeek() == java.time.DayOfWeek.MONDAY) {
            Double weeklyRet = computeWeeklyReturnPercent();
            var previousTier = leagueManager.getCurrentTier();
            try { performanceHistory.put(getPerformanceReport().getJSONObject("weekly")); }
            catch (JSONException impossible) { throw new IllegalStateException(impossible); }
            leagueManager.processWeeklyRollover(weeklyRet, getPersonalNetWorth());
            weeklyBaselineNetWorth = getPersonalNetWorth();
            weeklyPerformance = new com.tradingglobal3d.core.career.PerformancePeriod(clock.getGameTimestamp(), weeklyBaselineNetWorth,
                    ledger.getEntriesForAccount(personalAccountId).size());
            if (leagueManager.getCurrentTier().ordinal() > previousTier.ordinal())
                progressManager.onLeaguePromoted(clock.getGameTimestamp());
        }

        // 2. Eksekusi otomatis aksi korporasi yang jatuh tempo pada hari berikutnya
        for (com.tradingglobal3d.core.corporate.CorporateAction ca : corporateService.getCorporateActions()) {
            if (!ca.isExecuted() && !ca.getExecutionDate().isAfter(nextDate)) {
                if (ca.getType() == com.tradingglobal3d.core.corporate.CorporateActionType.CASH_DIVIDEND) {
                    executeCashDividend(ca);
                } else if (ca.getType() == com.tradingglobal3d.core.corporate.CorporateActionType.STOCK_SPLIT) {
                    executeStockSplit(ca);
                }
            }
        }

        // 3. Eksekusi otomatis penjatahan IPO yang jatuh tempo pada hari listing
        for (com.tradingglobal3d.core.corporate.IpoOffering ipo : corporateService.getIpoOfferings()) {
            if (!ipo.isAllotted() && !ipo.getListingDate().isAfter(nextDate)) {
                allotIpo(ipo.getId(), 0.50); // Penjatahan 50% dan refund 50%
            }
        }

    }

    public synchronized void evaluateTriggers() {
        long now = clock.getGameTimestamp();
        for (Instrument inst : catalog.getActiveUniverse()) {
            orderManager.checkAndActivateTriggers(inst, marketEngine, ledger, accounts, now);
        }
        evaluateRecordedCareerProgress();
    }

    public synchronized boolean tick(int realSeconds) {
        if (realSeconds < 0) throw new IllegalArgumentException("Waktu tidak boleh negatif");
        if (clock == null || clock.isPaused() || realSeconds == 0) return false;
        try {
            long gameSeconds = clock.advanceRealMs(realSeconds * 1000L, true);
            this.lastRealTimeAnchor = System.currentTimeMillis();
            if (gameSeconds <= 0) return false;
            return advanceWorldSeconds(gameSeconds);
        } catch (RuntimeException error) {
            clock.pause();
            throw error;
        }
    }

    public synchronized boolean advanceRealMs(long realMs, boolean isForeground) {
        if (realMs < 0) throw new IllegalArgumentException("Waktu tidak boleh negatif");
        if (clock == null || realMs == 0) return false;
        if (isForeground && clock.isPaused()) return false;
        try {
            long gameSeconds = clock.advanceRealMs(realMs, isForeground);
            this.lastRealTimeAnchor = System.currentTimeMillis();
            if (gameSeconds <= 0) return false;
            return advanceWorldSeconds(gameSeconds);
        } catch (RuntimeException error) {
            clock.pause();
            throw error;
        }
    }

    private boolean advanceWorldSeconds(long remaining) {
        // ponytail: economy runs on absolute minute boundaries; sub-minute events need an explicit event boundary here.
        LocalDate initialDate = clock.getCurrentDate();
        while (remaining > 0) {
            long midnight = 86400 - clock.getCurrentTime().toSecondOfDay();
            long secondsToMinute = 60 - clock.getCurrentTime().getSecond();
            long step = Math.min(remaining, Math.min(secondsToMinute, midnight));
            if (step == midnight) {
                clock.advanceGameSeconds(step - 1);
                settleDay(false);
                clock.advanceGameSeconds(1);
                processCalendarActions(clock.getCurrentDate());
                if (gameMode == GameMode.HISTORICAL_REPLAY && replayEngine.isReplayActive()) {
                    boolean advanced = replayEngine.advanceBar();
                    if (advanced) {
                        com.tradingglobal3d.core.market.HistoricalBar bar = replayEngine.getCurrentBar();
                        Instrument inst = catalog.getById(replayEngine.getActiveDatasetId());
                        if (inst != null && bar != null) {
                            inst.updatePrice(new Money(bar.getClose(), inst.getCurrency()), bar.getVolume());
                        }
                    }
                }
            } else {
                clock.advanceGameSeconds(step);
            }
            remaining -= step;
            if (clock.getCurrentTime().equals(LocalTime.of(16, 0))) {
                try {
                    mutualFundManager.processCutoff(accounts, ledger, clock.getGameTimestamp());
                    for (MutualFund fund : mutualFundManager.getAllFunds()) {
                        JSONArray orders = fund.toJsonObject(personalAccountId).getJSONArray("orders");
                        for (int i = 0; i < orders.length(); i++) {
                            JSONObject order = orders.getJSONObject(i);
                            if (!"SETTLED".equals(order.getString("status"))) continue;
                            LocalDate date = java.time.Instant.ofEpochMilli(order.getLong("settledTimestamp")).atOffset(java.time.ZoneOffset.ofHours(7)).toLocalDate();
                            periodicMissionManager.recordActivity("FUND:" + fund.getId() + ":" + order.getString("orderId"), "FUND_SETTLED", 1, date);
                            if ("SUBSCRIBE".equals(order.getString("side"))) progressManager.onFundSubscribed(order.getLong("settledTimestamp"));
                        }
                    }
                }
                catch (JSONException error) { throw new IllegalStateException("Cutoff reksa dana gagal", error); }

                // Equity rebalance: fund EQ index menjaga rasio BBCA/kas sesuai NAB
                try {
                    List<Order> rebalanceOrders = mutualFundManager.getEquityRebalanceOrders(catalog, clock.getGameTimestamp());
                    for (Order rebalOrder : rebalanceOrders) {
                        submitOrder(rebalOrder);
                    }
                } catch (Exception rebalErr) {
                    // Rebalance best-effort; kegagalan tidak menghentikan cutoff
                    // Simpan status kegagalan agar dapat diperiksa
                    mutualFundManager.recordRebalanceFailure(rebalErr.getMessage(), clock.getGameTimestamp());
                }
            }
            if (clock.getCurrentTime().getSecond() == 0) {
                if (clock.getCurrentMarketSession().isOpenForTrading()) generateNpcLiquidity();
                evaluateTriggers();
            }
        }
        return !initialDate.equals(clock.getCurrentDate());
    }

    public synchronized JSONObject skipDay(String requestId) throws JSONException, IOException {
        if (requestId == null || requestId.trim().isEmpty() || requestId.length() > 160)
            throw new IllegalArgumentException("ID lewati hari tidak valid");
        if (skipDayReceipts.containsKey(requestId)) {
            save();
            return skipDayReceipts.get(requestId);
        }
        if (clock == null || recoveryRequired) throw new IllegalStateException("Permainan belum siap");
        clock.pause();
        JSONObject receipt = new JSONObject().put("success", false).put("requestId", requestId)
                .put("from", clock.getCurrentDate()+"T"+clock.getCurrentTime()).put("status", "PROCESSING");
        skipDayReceipts.put(requestId, receipt);
        try {
            advanceWorldSeconds(86400);
            this.lastRealTimeAnchor = System.currentTimeMillis();
            receipt.put("success", true).put("status", "COMPLETED")
                    .put("to", clock.getCurrentDate()+"T"+clock.getCurrentTime()).put("gameSeconds", 86400);
        } catch (RuntimeException failure) {
            clock.pause();
            receipt.put("status", "NEEDS_REVIEW").put("message", "Kemajuan berhenti; periksa status sebelum mencoba lagi");
        }
        save();
        return receipt;
    }

    /**
     * Memvalidasi bahwa seluruh saldo akun cocok dengan audit ledger.
     */
    public synchronized boolean reconcileAll() {
        for (Account acc : accounts.values()) {
            for (Currency c : Currency.values()) {
                if (!ledger.reconcile(acc, c)) {
                    return false;
                }
            }
        }
        return true;
    }

    public synchronized void save() throws IOException, JSONException {
        if (recoveryRequired || clock == null || getPersonalAccount() == null)
            throw new IOException("Save belum dapat ditulis; pulihkan salinan valid terlebih dahulu");
        SaveState state = SaveState.capture(gameMode, clock, accounts.values(), ledger, lastRealTimeAnchor, lastOfflineCatchupReport);
        OrderManager savedOrders = new OrderManager();
        Set<String> savedIds = new HashSet<>();
        for (Account account : accounts.values()) {
            for (Order order : orderManager.getOrdersForAccount(account.getId())) {
                savedOrders.registerOrder(order); savedIds.add(order.getId());
            }
        }
        for (Order order : marketEngine.getRestingOrders()) {
            if (savedIds.add(order.getId())) savedOrders.registerOrder(order);
        }
        JSONObject runtime = new JSONObject();
        runtime.put("runId", runId);
        if (weeklyPerformance != null) runtime.put("weeklyPerformance", weeklyPerformance.exportState());
        if (runPerformance != null) runtime.put("runPerformance", runPerformance.exportState());
        runtime.put("performanceHistory", performanceHistory);
        if (weeklyBaselineNetWorth != null) {
            runtime.put("weeklyBaselineNetWorth", weeklyBaselineNetWorth.getAmount().toPlainString());
        }
        runtime.put("rulesVersion", RULES_VERSION);
        runtime.put("catalog", catalog.exportState());
        runtime.put("risk", riskService.exportState());
        runtime.put("orders", savedOrders.exportState());
        runtime.put("books", marketEngine.exportState());
        runtime.put("career", careerManager.exportState());
        runtime.put("mandates", firmManager.exportState());
        runtime.put("subscription", researchService.exportState());
        runtime.put("fx", fxService.exportState());
        runtime.put("fundPlans", fundBuilder.exportState());
        runtime.put("corporate", corporateService.exportState());
        runtime.put("companies", companyManager.exportState());
        runtime.put("collections", collectionManager.exportState());
        runtime.put("playerProgress", progressManager.exportState());
        runtime.put("replayEngine", replayEngine.exportState());
        runtime.put("mutualFunds", mutualFundManager.exportState());
        runtime.put("installmentLoans", installmentLoanManager.exportState());
        runtime.put("periodicMissions", periodicMissionManager.exportState());
        runtime.put("league", leagueManager.exportState());
        runtime.put("playerProfile", playerProfileManager.exportState());
        JSONObject closeReceipts = new JSONObject();
        for (Map.Entry<String, JSONObject> receipt : dayCloseReceipts.entrySet()) closeReceipts.put(receipt.getKey(), receipt.getValue());
        runtime.put("dayCloseReceipts", closeReceipts);
        runtime.put("skipDayReceipts", new JSONObject(skipDayReceipts));
        state.setRuntimeState(runtime);
        saveManager.save(state);
    }

    public synchronized boolean load() {
        SaveManager.LoadResult res = saveManager.load();
        if (!res.isSuccess()) return false;

        SaveState state = res.getState();
        // Validate the saved graph in detached managers before touching the live run.
        Map<String, JSONObject> restoredDayReceipts = new LinkedHashMap<>();
        Map<String, JSONObject> restoredSkipReceipts = new LinkedHashMap<>();
        try {
            new GameClock(state.getGameDate(), state.getGameTime()).setSpeedMultiplier(state.getSpeedMultiplier());
            JSONObject runtime = state.getRuntimeState();
            JSONObject skips = runtime.optJSONObject("skipDayReceipts");
            if (skips != null) {
                Iterator<String> keys = skips.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject receipt = skips.getJSONObject(key);
                    if (key.trim().isEmpty() || !key.equals(receipt.getString("requestId")))
                        throw new JSONException("Receipt lewati hari tidak valid");
                    receipt.getBoolean("success");
                    java.time.LocalDateTime from = java.time.LocalDateTime.parse(receipt.getString("from"));
                    if (receipt.getBoolean("success")) {
                        java.time.LocalDateTime to = java.time.LocalDateTime.parse(receipt.getString("to"));
                        if (receipt.getLong("gameSeconds") != 86400 || !from.plusDays(1).equals(to)
                                || !"COMPLETED".equals(receipt.getString("status"))) throw new JSONException("Durasi lewati hari tidak valid");
                    }
                    restoredSkipReceipts.put(key, receipt);
                }
            }
            if (runtime.has("dayCloseReceipts")) {
                JSONObject receipts = runtime.getJSONObject("dayCloseReceipts");
                Iterator<String> keys = receipts.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject receipt = receipts.getJSONObject(key);
                    if (key.trim().isEmpty() || !receipt.getBoolean("success") || receipt.getInt("fills") < 0)
                        throw new JSONException("Receipt penutupan tidak valid");
                    LocalDate.parse(receipt.getString("date"));
                    for (String field : Arrays.asList("startingNetWorth", "endingNetWorth", "investmentPnL", "fees", "salary", "note"))
                        receipt.getString(field);
                    restoredDayReceipts.put(key, receipt);
                }
            }
            if (runtime.has("catalog")) catalog.validateState(runtime.getJSONObject("catalog"));
            if (runtime.has("career")) new CareerManager().restoreState(runtime.getJSONObject("career"));
            if (runtime.has("mandates")) new FirmManager().restoreState(runtime.getJSONArray("mandates"));
            if (runtime.has("subscription")) new ResearchService().restoreState(runtime.getJSONObject("subscription"));
            if (runtime.has("playerProgress")) new PlayerProgressManager().restoreState(runtime.getJSONObject("playerProgress"));
            if (runtime.has("fx")) new FxExchangeService().restoreState(runtime.getJSONObject("fx"));
            else for (LedgerEntry entry : state.getLedgerEntries()) {
                if (entry.getType() == LedgerEntryType.FX_CONVERSION_DEBIT || entry.getType() == LedgerEntryType.FX_CONVERSION_CREDIT)
                    throw new IllegalArgumentException("Save FX lama memerlukan migrasi receipt; tidak boleh membuat hasil kurs baru");
            }
            new RiskComplianceService().restoreState(runtime.optJSONObject("risk") != null ? runtime.getJSONObject("risk") : new JSONObject());
            OrderManager stagedOrders = new OrderManager();
            MarketEngine stagedMarket = new MarketEngine();
            stagedOrders.restoreState(runtime.optJSONArray("orders") != null ? runtime.getJSONArray("orders") : new JSONArray(), catalog, stagedMarket);
            if (runtime.has("books")) stagedMarket.restoreState(runtime.getJSONArray("books"), catalog, stagedOrders);
            FundBuilderService stagedFunds = new FundBuilderService();
            stagedFunds.restoreState(runtime.has("fundPlans") ? runtime.getJSONArray("fundPlans") : new JSONArray());
            for (FundAllocationPlan plan : stagedFunds.getAllPlans()) {
                boolean accountExists = false;
                for (Account account : state.getAccounts()) accountExists |= account.getId().equals(plan.getAccountId());
                if (!accountExists) throw new IllegalArgumentException("Akun rencana save tidak ditemukan");
                for (String request : plan.getExecutionRequests()) {
                    Order order = stagedOrders.getOrderByRequest(request);
                    if (order == null || !order.getAccountId().equals(plan.getAccountId()))
                        throw new IllegalArgumentException("Order rencana save tidak cocok");
                }
            }
            if (runtime.has("corporate")) new CorporateActionService().restoreState(runtime.getJSONObject("corporate"));
            // Validate optional domains before replacing the live clock/accounts/ledger.
            if (runtime.has("companies")) {
                com.tradingglobal3d.core.business.CompanyManager stagedCompanies = new com.tradingglobal3d.core.business.CompanyManager();
                stagedCompanies.restoreState(runtime.getJSONArray("companies"));
                Set<String> companyAccounts = new HashSet<>();
                if (stagedCompanies.getAllCompanies().size() != runtime.getJSONArray("companies").length())
                    throw new IllegalArgumentException("ID perusahaan duplikat");
                for (com.tradingglobal3d.core.business.OperatingCompany company : stagedCompanies.getAllCompanies()) {
                    Account owner = null;
                    for (Account account : state.getAccounts()) if (account.getId().equals(company.getAccountId())) owner = account;
                    if (owner == null || owner.getType() != AccountType.COMPANY || !companyAccounts.add(owner.getId()))
                        throw new IllegalArgumentException("Akun perusahaan save tidak valid");
                }
            }
            if (runtime.has("collections")) {
                var stagedCollections = new com.tradingglobal3d.core.collection.CollectionManager();
                stagedCollections.restoreState(runtime.getJSONArray("collections"));
                for (var item : stagedCollections.getAllItems()) {
                    if (!item.isOwned() && item.getMaintenanceArrears().isZero()) continue;
                    boolean validOwner = false;
                    for (Account account : state.getAccounts()) if (account.getId().equals(item.getOwnerAccountId()) && account.getType() == AccountType.PERSONAL) validOwner = true;
                    if (!validOwner) throw new IllegalArgumentException("Pemilik koleksi save tidak valid");
                }
            }
            if (runtime.has("replayEngine")) new HistoricalReplayEngine().restoreState(runtime.getJSONObject("replayEngine"));
            if (runtime.has("mutualFunds")) {
                MutualFundManager stagedMutualFunds = new MutualFundManager();
                stagedMutualFunds.restoreState(runtime.getJSONArray("mutualFunds"));
                Map<String, Account> savedAccounts = new LinkedHashMap<>();
                for (Account account : state.getAccounts()) savedAccounts.put(account.getId(), account);
                stagedMutualFunds.validateAccounts(savedAccounts);
            }
            if (runtime.has("installmentLoans")) {
                InstallmentLoanManager stagedLoans = new InstallmentLoanManager();
                stagedLoans.restoreState(runtime.getJSONArray("installmentLoans"), new GameClock(state.getGameDate(), state.getGameTime()).getGameTimestamp());
                for (LoanContract loan : stagedLoans.getAllLoans()) {
                    Account borrower = null;
                    for (Account account : state.getAccounts()) if (account.getId().equals(loan.getAccountId())) borrower = account;
                    if (borrower == null || (borrower.getType() != AccountType.PERSONAL && borrower.getType() != AccountType.COMPANY))
                        throw new IllegalArgumentException("Akun peminjam save tidak valid");
                    if (("LOAN-PERS-01".equals(loan.getProductId()) && borrower.getType() != AccountType.PERSONAL)
                            || ("LOAN-CORP-01".equals(loan.getProductId()) && borrower.getType() != AccountType.COMPANY))
                        throw new IllegalArgumentException("Produk pinjaman tidak cocok dengan peminjam");
                }
            }
            if (runtime.has("periodicMissions")) new PeriodicMissionManager().restoreState(runtime.getJSONObject("periodicMissions"));
            if (runtime.has("league")) new LeagueManager().restoreState(runtime.getJSONObject("league"));
            if (runtime.has("playerProfile")) new PlayerProfileManager().restoreState(runtime.getJSONObject("playerProfile"));
            if (runtime.has("weeklyPerformance")) com.tradingglobal3d.core.career.PerformancePeriod.restore(runtime.getJSONObject("weeklyPerformance"));
            if (runtime.has("runPerformance")) com.tradingglobal3d.core.career.PerformancePeriod.restore(runtime.getJSONObject("runPerformance"));
        } catch (Exception invalidState) {
            return false;
        }
        this.runId = state.getRuntimeState().optString("runId", "LEGACY-" + java.util.UUID.nameUUIDFromBytes((state.getGameDate() + ":" + state.getLedgerEntries().size()).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        String savedBaseline = state.getRuntimeState().optString("weeklyBaselineNetWorth", null);
        this.weeklyBaselineNetWorth = savedBaseline != null ? new Money(new BigDecimal(savedBaseline), Currency.IDR) : getPersonalNetWorth();
        this.gameMode = state.getGameMode();
        this.clock = new GameClock(state.getGameDate(), state.getGameTime());
        this.clock.setSpeedMultiplier(state.getSpeedMultiplier());
        this.clock.setGameMsRemainder(state.getGameMsRemainder());
        this.clock.setForeground("FOREGROUND".equalsIgnoreCase(state.getLifecycleState()));
        this.lastRealTimeAnchor = state.getLastRealTimeAnchor();
        this.lastOfflineCatchupReport = state.getOfflineCatchupReport();

        this.accounts.clear();
        for (Account a : state.getAccounts()) {
            this.accounts.put(a.getId(), a);
            if (a.getType() == AccountType.PERSONAL) {
                this.personalAccountId = a.getId();
            }
        }
        this.ledger.restoreEntries(state.getLedgerEntries());
        try {
            JSONObject runtime = state.getRuntimeState();
            if (runtime.has("catalog")) catalog.restoreState(runtime.getJSONObject("catalog"));
            if (runtime.has("career")) careerManager.restoreState(runtime.getJSONObject("career"));
            if (runtime.has("mandates")) firmManager.restoreState(runtime.getJSONArray("mandates"));
            if (runtime.has("subscription")) researchService.restoreState(runtime.getJSONObject("subscription"));
            fxService.restoreState(runtime.has("fx") ? runtime.getJSONObject("fx") : new FxExchangeService().exportState());
            fundBuilder.restoreState(runtime.has("fundPlans") ? runtime.getJSONArray("fundPlans") : new JSONArray());
            if (runtime.has("corporate")) corporateService.restoreState(runtime.getJSONObject("corporate"));
            companyManager.reset(); collectionManager.reset(); mutualFundManager.reset();
            installmentLoanManager.reset(); periodicMissionManager.reset(); leagueManager.reset();
            playerProfileManager.reset(); replayEngine.reset();
            if (runtime.has("companies")) companyManager.restoreState(runtime.getJSONArray("companies"));
            if (runtime.has("collections")) collectionManager.restoreState(runtime.getJSONArray("collections"));
            if (runtime.has("playerProgress")) progressManager.restoreState(runtime.optJSONObject("playerProgress"));
            if (runtime.has("replayEngine")) replayEngine.restoreState(runtime.optJSONObject("replayEngine"));
            if (runtime.has("mutualFunds")) mutualFundManager.restoreState(runtime.getJSONArray("mutualFunds"));
            if (runtime.has("installmentLoans")) installmentLoanManager.restoreState(runtime.getJSONArray("installmentLoans"), clock.getGameTimestamp());
            if (runtime.has("periodicMissions")) periodicMissionManager.restoreState(runtime.getJSONObject("periodicMissions"));
            if (runtime.has("league")) leagueManager.restoreState(runtime.getJSONObject("league"));
            if (runtime.has("playerProfile")) playerProfileManager.restoreState(runtime.getJSONObject("playerProfile"));
            riskService.restoreState(runtime.optJSONObject("risk") != null ? runtime.getJSONObject("risk") : new JSONObject());
            marketEngine.clearState();
            orderManager.restoreState(runtime.optJSONArray("orders") != null ? runtime.getJSONArray("orders") : new JSONArray(), catalog, marketEngine);
            if (runtime.has("books")) marketEngine.restoreState(runtime.getJSONArray("books"), catalog, orderManager);
            dayCloseReceipts.clear();
            dayCloseReceipts.putAll(restoredDayReceipts);
            skipDayReceipts.clear();
            skipDayReceipts.putAll(restoredSkipReceipts);
            Account marketMakerAccount = accounts.get("ACC-NPC-MM-01");
            if (marketMakerAccount != null) marketEngine.registerNpcAgent(new NpcAgent("NPC-MM-01", "Bandar Likuiditas Prima", NpcStrategyType.MARKET_MAKER, marketMakerAccount));

            // Bind collection manager ke loan manager setelah keduanya di-restore
            installmentLoanManager.bindCollectionManager(collectionManager);

            // Bind fund pool accounts setelah restore akun dan katalog
            mutualFundManager.registerPoolAccounts(accounts, ledger, clock.getGameTimestamp());
            mutualFundManager.bindAccounts(accounts, catalog);
            weeklyPerformance = runtime.has("weeklyPerformance") ? com.tradingglobal3d.core.career.PerformancePeriod.restore(runtime.getJSONObject("weeklyPerformance"))
                    : new com.tradingglobal3d.core.career.PerformancePeriod(clock.getGameTimestamp(), getPersonalNetWorth(), ledger.getEntriesForAccount(personalAccountId).size());
            runPerformance = runtime.has("runPerformance") ? com.tradingglobal3d.core.career.PerformancePeriod.restore(runtime.getJSONObject("runPerformance")) : weeklyPerformance;
            performanceHistory = runtime.optJSONArray("performanceHistory") != null ? runtime.getJSONArray("performanceHistory") : new JSONArray();
        } catch (Exception invalidState) {
            return false;
        }
        recoveryRequired = false;
        return true;
    }

    /**
     * Menghasilkan JSON snapshot ringkas untuk disajikan ke UI / Android WebView.
     */
    public synchronized String getSnapshotJson() throws JSONException {
        JSONObject root = new JSONObject();
        root.put("runId", runId).put("rulesVersion", RULES_VERSION);
        root.put("recoveryRequired", recoveryRequired);
        root.put("gameMode", gameMode != null ? gameMode.name() : "NONE");
        root.put("gameModeLabel", gameMode != null ? gameMode.getDisplayName() : "-");

        if (clock != null) {
            JSONObject clockObj = new JSONObject();
            clockObj.put("date", clock.getFormattedDate());
            clockObj.put("time", clock.getFormattedTime());
            clockObj.put("session", clock.getCurrentMarketSession().name());
            clockObj.put("sessionLabel", clock.getCurrentMarketSession().getLabel());
            clockObj.put("isTradingOpen", clock.getCurrentMarketSession().isOpenForTrading());
            clockObj.put("speed", clock.getSpeedMultiplier());
            clockObj.put("isPaused", clock.isPaused());
            root.put("clock", clockObj);
        }

        Account personal = getPersonalAccount();
        if (personal != null) {
            JSONObject personalObj = new JSONObject();
            personalObj.put("id", personal.getId());
            personalObj.put("name", personal.getName());

            JSONObject cashObj = new JSONObject();
            for (Currency c : Currency.values()) {
                JSONObject cObj = new JSONObject();
                cObj.put("total", personal.getTotalCash(c).format());
                cObj.put("available", personal.getAvailableCash(c).format());
                cObj.put("reserved", personal.getReservedCash(c).format());
                cashObj.put(c.name(), cObj);
            }
            personalObj.put("cash", cashObj);

            Money netWorth = personalNetWorth();
            personalObj.put("netWorth", netWorth.format());
            personalObj.put("netWorthRaw", netWorth.getAmount().toPlainString());

            root.put("personalAccount", personalObj);
        }

        JSONArray accountsArray = new JSONArray();
        for (Account acc : accounts.values()) {
            if (acc.getType() == AccountType.PERSONAL || acc.getType() == AccountType.CLIENT) {
                JSONObject aObj = new JSONObject();
                aObj.put("id", acc.getId());
                aObj.put("name", acc.getName());
                aObj.put("type", acc.getType().name());
                aObj.put("mandateId", acc.getClientMandateId() == null ? JSONObject.NULL : acc.getClientMandateId());
                JSONObject cObj = new JSONObject();
                for (Currency c : Currency.values()) {
                    cObj.put(c.name(), acc.getAvailableCash(c).format());
                }
                aObj.put("availableCash", cObj);
                accountsArray.put(aObj);
            }
        }
        root.put("tradingAccounts", accountsArray);

        JSONObject career = new JSONObject().put("rank", careerManager.getCurrentRank().name())
                .put("title", careerManager.getCurrentRank().getTitle()).put("days", careerManager.getDaysTraded())
                .put("reputation", careerManager.getReputationScore()).put("parametersProvisional", true);
        JSONArray missions = new JSONArray();
        for (com.tradingglobal3d.core.career.CareerMission mission : careerManager.getAllMissions()) {
            missions.put(new JSONObject().put("id", mission.getId()).put("title", mission.getTitle())
                    .put("description", mission.getDescription()).put("reward", mission.getReward().format())
                    .put("completed", mission.isCompleted()).put("claimed", mission.isClaimed()));
        }
        root.put("career", career.put("missions", missions));
        CareerRank nextRank = careerManager.getCurrentRank().getNextRank();
        if (nextRank != null) career.put("nextPromotion", new JSONObject().put("title", nextRank.getTitle())
                .put("days", nextRank.getRequiredDays()).put("reputation", nextRank.getRequiredReputation())
                .put("netWorthIdr", nextRank.getRequiredNetWorth()));
        root.put("featureAccess", new JSONObject()
                .put("calendar", careerAccess(CareerRank.SENIOR_ANALYST))
                .put("terminal", careerAccess(CareerRank.SENIOR_ANALYST))
                .put("mandates", careerAccess(CareerRank.PORTFOLIO_MANAGER))
                .put("fundBuilder", careerAccess(CareerRank.PORTFOLIO_MANAGER))
                .put("margin", careerAccess(CareerRank.PORTFOLIO_MANAGER))
                .put("short", careerAccess(CareerRank.PORTFOLIO_MANAGER)));
        root.put("playerProgress", progressManager.toSnapshotJson());
        root.put("accountCount", accounts.size());
        root.put("ledgerEntryCount", ledger.getEntries().size());
        return root.toString();
    }

    private JSONObject careerAccess(CareerRank required) throws JSONException {
        return new JSONObject().put("unlocked", careerManager.hasRank(required)).put("requiredRank", required.name())
                .put("requiredTitle", required.getTitle());
    }

    public synchronized Money getPersonalNetWorth() {
        Account personal = getPersonalAccount();
        if (personal == null) return Money.zero(Currency.IDR);
        BigDecimal usdRate = fxService != null ? fxService.getMidRate() : BigDecimal.valueOf(15800);
        Money equity = riskService.evaluateMarginHealth(personal, catalog, usdRate).getEquity();

        // Sertakan ekuitas perusahaan operasional milik pemain (100% owned) dikurangi utang perusahaan (D02)
        if (companyManager != null) {
            for (com.tradingglobal3d.core.business.OperatingCompany comp : companyManager.getAllCompanies()) {
                Account compAccount = accounts.get(comp.getAccountId());
                if (compAccount != null) {
                    Money compVal = compAccount.getTotalPortfolioValue(Currency.IDR, usdRate);
                    if (installmentLoanManager != null) {
                        Money compDebt = installmentLoanManager.getTotalOutstandingLiability(compAccount.getId());
                        compVal = compVal.subtract(compDebt);
                    }
                    equity = equity.add(compVal.subtract(comp.getPayrollArrears()));
                }
            }
        }

        // Sertakan valuasi barang koleksi dan properti milik pemain
        if (collectionManager != null) {
            equity = equity.add(collectionManager.calculateTotalValuation()).subtract(collectionManager.getTotalMaintenanceLiability());
        }

        // Sertakan nilai unit reksa dana yang dimiliki (D06, D02)
        if (mutualFundManager != null) {
            equity = equity.add(mutualFundManager.calculateTotalHoldingsValue(personal.getId()));
        }

        // Kurangi liabilitas pinjaman tenor pribadi yang belum lunas (D05, D02)
        if (installmentLoanManager != null) {
            Money personalLoanDebt = installmentLoanManager.getTotalOutstandingLiability(personal.getId());
            equity = equity.subtract(personalLoanDebt);
        }

        return equity;
    }

    private Money personalNetWorth() {
        return getPersonalNetWorth();
    }

    /**
     * Menghitung return mingguan pemain berdasarkan baseline awal pekan.
     * Return = (currentNetWorth - baseline) / baseline * 100.
     * Jika baseline nol atau negatif, return 0 untuk menghindari division by zero.
     */
    private Double computeWeeklyReturnPercent() {
        try {
            JSONObject weekly = getPerformanceReport().getJSONObject("weekly");
            return weekly.getBoolean("returnAvailable") ? weekly.getDouble("returnPercent") : null;
        } catch (JSONException impossible) { throw new IllegalStateException(impossible); }
    }

    public synchronized JSONObject getPerformanceReport() throws JSONException {
        if (weeklyPerformance == null || runPerformance == null) throw new IllegalStateException("Periode statistik belum siap");
        long now = clock.getGameTimestamp();
        List<LedgerEntry> entries = ledger.getEntriesForAccount(personalAccountId);
        return new JSONObject().put("success", true)
                .put("weekly", weeklyPerformance.calculate(now, getPersonalNetWorth(), entries, fxService.getMidRate()))
                .put("run", runPerformance.calculate(now, getPersonalNetWorth(), entries, fxService.getMidRate()))
                .put("weeklyHistory", new JSONArray(performanceHistory.toString()));
    }

    public synchronized JSONObject getChartData(String instrumentId, String timeframe) throws JSONException {
        Instrument inst = catalog.getById(instrumentId);
        if (inst == null) {
            return new JSONObject().put("success", false).put("message", "Instrumen tidak ditemukan: " + instrumentId);
        }

        if (gameMode == GameMode.HISTORICAL_REPLAY && replayEngine.isReplayActive()
                && (instrumentId.equalsIgnoreCase(replayEngine.getActiveDatasetId())
                    || instrumentId.contains(replayEngine.getActiveDatasetId())
                    || (inst.getTicker() != null && replayEngine.getActiveDatasetId().contains(inst.getTicker())))) {
            JSONArray barsArr = new JSONArray();
            for (com.tradingglobal3d.core.market.HistoricalBar b : replayEngine.getVisibleBars(clock.getCurrentDate(), clock.getCurrentTime())) {
                JSONObject bObj = new JSONObject();
                bObj.put("time", b.getDate().toString());
                bObj.put("open", b.getOpen().doubleValue());
                bObj.put("high", b.getHigh().doubleValue());
                bObj.put("low", b.getLow().doubleValue());
                bObj.put("close", b.getClose().doubleValue());
                bObj.put("volume", b.getVolume());
                bObj.put("isReference", false);
                barsArr.put(bObj);
            }
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("instrumentId", inst.getId());
            result.put("ticker", inst.getTicker());
            result.put("currency", inst.getCurrency().name());
            result.put("lotSize", inst.getLotSize());
            result.put("timeframe", "1D");
            result.put("totalTrades", barsArr.length());
            result.put("bars", barsArr);
            result.put("source", "Data historis harian anti-future leak (" + replayEngine.getActiveDatasetId() + ")");
            return result;
        }

        OrderBook book = marketEngine.getOrCreateBook(inst);
        List<TradeFill> fills = book.getTradeHistory();

        long intervalMs;
        String tf = timeframe != null ? timeframe.toUpperCase() : "M5";
        switch (tf) {
            case "M1":
                intervalMs = 60_000L;
                break;
            case "M5":
                intervalMs = 300_000L;
                break;
            case "M15":
                intervalMs = 900_000L;
                break;
            case "H1":
                intervalMs = 3_600_000L;
                break;
            case "D1":
            default:
                intervalMs = 86_400_000L;
                break;
        }

        JSONArray bars = new JSONArray();
        if (fills.isEmpty()) {
            BigDecimal refPrice = inst.getCurrentPrice().getAmount();
            JSONObject bar = new JSONObject();
            bar.put("time", "Ref 09:00");
            bar.put("open", refPrice.doubleValue());
            bar.put("high", refPrice.doubleValue());
            bar.put("low", refPrice.doubleValue());
            bar.put("close", refPrice.doubleValue());
            bar.put("volume", 0);
            bar.put("isReference", true);
            bars.put(bar);
        } else {
            Map<Long, List<TradeFill>> buckets = new LinkedHashMap<>();
            for (TradeFill fill : fills) {
                long bucketKey = (fill.getTimestamp() / intervalMs) * intervalMs;
                buckets.computeIfAbsent(bucketKey, k -> new ArrayList<>()).add(fill);
            }

            int barIndex = 1;
            for (Map.Entry<Long, List<TradeFill>> entry : buckets.entrySet()) {
                List<TradeFill> bucketFills = entry.getValue();
                BigDecimal open = bucketFills.get(0).getPrice().getAmount();
                BigDecimal close = bucketFills.get(bucketFills.size() - 1).getPrice().getAmount();
                BigDecimal high = open;
                BigDecimal low = open;
                long volume = 0;

                for (TradeFill fill : bucketFills) {
                    BigDecimal p = fill.getPrice().getAmount();
                    if (p.compareTo(high) > 0) high = p;
                    if (p.compareTo(low) < 0) low = p;
                    volume += fill.getQuantity();
                }

                JSONObject bar = new JSONObject();
                if ("D1".equals(tf)) {
                    bar.put("time", "Sesi " + barIndex);
                } else if ("H1".equals(tf)) {
                    bar.put("time", "Jam " + barIndex);
                } else {
                    java.time.Instant instant = java.time.Instant.ofEpochMilli(entry.getKey());
                    java.time.ZonedDateTime zdt = instant.atZone(java.time.ZoneId.of("Asia/Jakarta"));
                    bar.put("time", String.format(java.util.Locale.US, "%02d:%02d", zdt.getHour(), zdt.getMinute()));
                }
                bar.put("open", open.doubleValue());
                bar.put("high", high.doubleValue());
                bar.put("low", low.doubleValue());
                bar.put("close", close.doubleValue());
                bar.put("volume", volume);
                bar.put("isReference", false);
                bars.put(bar);
                barIndex++;
            }
        }

        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("instrumentId", inst.getId());
        result.put("ticker", inst.getTicker());
        result.put("currency", inst.getCurrency().name());
        result.put("lotSize", inst.getLotSize());
        result.put("timeframe", tf);
        result.put("totalTrades", fills.size());
        result.put("bars", bars);
        result.put("source", "Matched trades domain engine internal");
        return result;
    }

    // =========================================================================
    // 1. REKSA DANA BERBASIS UNIT & NAB (REF:funds, D06)
    // =========================================================================

    public synchronized JSONObject getMutualFundsJson() throws JSONException {
        Account personal = getPersonalAccount();
        String pId = personal != null ? personal.getId() : null;
        JSONArray arr = new JSONArray();
        for (MutualFund f : mutualFundManager.getAllFunds()) {
            arr.put(f.toJsonObject(pId));
        }
        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("gameTimestamp", clock.getGameTimestamp());
        res.put("funds", arr);
        res.put("totalHoldingsValue", (pId != null ? mutualFundManager.calculateTotalHoldingsValue(pId) : Money.zero(Currency.IDR)).format());
        return res;
    }

    public synchronized JSONObject subscribeMutualFund(String fundId, Money amount) throws JSONException {
        return subscribeMutualFund(java.util.UUID.randomUUID().toString(), fundId, amount);
    }

    public synchronized JSONObject subscribeMutualFund(String requestId, String fundId, Money amount) throws JSONException {
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Akun personal belum dibuat");
        MutualFund fund = mutualFundManager.getFund(fundId);
        if (fund == null) throw new IllegalArgumentException("Reksa dana tidak ditemukan: " + fundId);
        long now = clock != null ? clock.getGameTimestamp() : System.currentTimeMillis();
        String orderId = requestId;
        JSONObject res = fund.submitSubscription(orderId, personal, amount, ledger, now);
        persistFinancialMutation();
        return res;
    }

    public synchronized JSONObject redeemMutualFund(String fundId, BigDecimal units) throws JSONException {
        return redeemMutualFund(java.util.UUID.randomUUID().toString(), fundId, units);
    }

    public synchronized JSONObject redeemMutualFund(String requestId, String fundId, BigDecimal units) throws JSONException {
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Akun personal belum dibuat");
        MutualFund fund = mutualFundManager.getFund(fundId);
        if (fund == null) throw new IllegalArgumentException("Reksa dana tidak ditemukan: " + fundId);
        long now = clock != null ? clock.getGameTimestamp() : System.currentTimeMillis();
        String orderId = requestId;
        JSONObject receipt = fund.submitRedemption(orderId, personal, units, ledger, now);
        persistFinancialMutation();
        return receipt;
    }

    public synchronized JSONObject cancelFundOrder(String fundId, String orderId) throws JSONException {
        MutualFund fund = mutualFundManager.getFund(fundId);
        if (fund == null || getPersonalAccount() == null) throw new IllegalArgumentException("Reksa dana tidak ditemukan");
        JSONObject receipt = fund.cancelOrder(orderId, getPersonalAccount(), clock.getGameTimestamp());
        persistFinancialMutation();
        return receipt;
    }

    // =========================================================================
    // 2. PINJAMAN TENOR DI LUAR MARGIN (REF:loans, D05)
    // =========================================================================

    public synchronized JSONObject getLoanProductsJson() throws JSONException {
        JSONArray arr = new JSONArray();
        JSONObject p1 = new JSONObject();
        p1.put("productId", "LOAN-PERS-01");
        p1.put("name", "Pinjaman Pribadi Multiguna");
        p1.put("description", "Pembiayaan kas pribadi dengan bunga harian 0.05% atas sisa pokok.");
        p1.put("minPrincipal", "Rp 1.000.000");
        p1.put("availableTenors", new JSONArray().put(7).put(14).put(30));
        p1.put("dailyInterestRate", "0.05% / hari");
        p1.put("disbursementFee", "0.5% (provisi cair)");
        arr.put(p1);

        JSONObject p2 = new JSONObject();
        p2.put("productId", "LOAN-CORP-01");
        p2.put("name", "Kredit Modal Kerja Operasional");
        p2.put("description", "Pembiayaan kas anak usaha untuk ekspansi atau modal kerja.");
        p2.put("minPrincipal", "Rp 1.000.000");
        p2.put("availableTenors", new JSONArray().put(7).put(14).put(30));
        p2.put("dailyInterestRate", "0.05% / hari");
        p2.put("disbursementFee", "0.5% (provisi cair)");
        arr.put(p2);

        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("products", arr);
        return res;
    }

    public synchronized JSONObject disburseLoan(String productId, boolean isPersonal, String companyId, Money principal, int tenorDays) throws JSONException {
        throw new IllegalStateException("Tinjau quote dan agunan terlebih dahulu sebelum mengonfirmasi pinjaman");
    }

    /**
     * Ambil quote pinjaman beragunan (personal) atau batas arus kas (perusahaan).
     * Non-mutating preview dengan quoteVersion untuk validasi ulang saat disburse.
     */
    public synchronized JSONObject getLoanQuote(String productId, boolean isPersonal, String companyId,
            Money principal, int tenorDays, String collateralItemId) throws JSONException {
        Account targetAccount;
        com.tradingglobal3d.core.collection.CollectibleItem collateral = null;
        Money cashflowLimit = null;
        if (isPersonal) {
            targetAccount = getPersonalAccount();
            if (collateralItemId != null && !collateralItemId.isEmpty()) {
                collateral = collectionManager.getItem(collateralItemId);
                if (collateral == null) throw new IllegalArgumentException("Agunan tidak ditemukan: " + collateralItemId);
            }
        } else {
            com.tradingglobal3d.core.business.OperatingCompany comp = companyManager.getCompany(companyId);
            if (comp == null) throw new IllegalArgumentException("Perusahaan tidak ditemukan: " + companyId);
            targetAccount = accounts.get(comp.getAccountId());
            cashflowLimit = comp.getLastRealizedRevenue();
        }
        if (targetAccount == null) throw new IllegalStateException("Akun tujuan pinjaman tidak ditemukan");
        return installmentLoanManager.getLoanQuote(productId, targetAccount, principal, tenorDays, collateral, cashflowLimit);
    }

    /**
     * Cairkan pinjaman setelah review quote — agunan di-pledge otomatis.
     * Validasi ulang quote version dan syarat; stale quote ditolak.
     */
    public synchronized JSONObject disburseReviewedLoan(String requestId, String productId, boolean isPersonal,
            String companyId, Money principal, int tenorDays, String collateralItemId, String expectedQuoteVersion) throws JSONException {
        Account targetAccount;
        com.tradingglobal3d.core.collection.CollectibleItem collateral = null;
        Money cashflowLimit = null;
        if (isPersonal) {
            targetAccount = getPersonalAccount();
            if (collateralItemId != null && !collateralItemId.isEmpty()) {
                collateral = collectionManager.getItem(collateralItemId);
                if (collateral == null) throw new IllegalArgumentException("Agunan tidak ditemukan: " + collateralItemId);
            }
        } else {
            com.tradingglobal3d.core.business.OperatingCompany comp = companyManager.getCompany(companyId);
            if (comp == null) throw new IllegalArgumentException("Perusahaan tidak ditemukan: " + companyId);
            targetAccount = accounts.get(comp.getAccountId());
            cashflowLimit = comp.getLastRealizedRevenue();
        }
        if (targetAccount == null) throw new IllegalStateException("Akun tujuan pinjaman tidak ditemukan");
        long now = clock != null ? clock.getGameTimestamp() : System.currentTimeMillis();
        JSONObject result = installmentLoanManager.disburseReviewedLoan(requestId, productId, targetAccount, principal,
                tenorDays, collateral, cashflowLimit, expectedQuoteVersion, ledger, now);
        persistFinancialMutation();
        return result;
    }

    public synchronized JSONObject getLoansJson() throws JSONException {
        Account personal = getPersonalAccount();
        String pId = personal != null ? personal.getId() : "";
        JSONArray arr = new JSONArray();
        for (LoanContract l : installmentLoanManager.getAllLoans()) {
            arr.put(l.toJsonObject());
        }
        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("loans", arr);
        res.put("totalPersonalDebt", installmentLoanManager.getTotalOutstandingLiability(pId).format());
        return res;
    }

    public synchronized JSONObject payLoan(String loanId, Money amount) throws JSONException {
        return payLoan(java.util.UUID.randomUUID().toString(), loanId, amount);
    }

    public synchronized JSONObject payLoan(String requestId, String loanId, Money amount) throws JSONException {
        LoanContract loan = installmentLoanManager.getLoan(loanId);
        if (loan == null) throw new IllegalArgumentException("Kontrak pinjaman tidak ditemukan: " + loanId);
        Account acc = accounts.get(loan.getAccountId());
        if (acc == null) throw new IllegalStateException("Akun peminjam tidak ditemukan");
        long now = clock != null ? clock.getGameTimestamp() : System.currentTimeMillis();
        JSONObject res = loan.makePayment(requestId, acc, amount, ledger, now);
        persistFinancialMutation();
        if (loan.isClosed()) {
            progressManager.onLoanPaidOff(now);
        }
        return res;
    }

    public synchronized JSONObject prepayLoan(String loanId) throws JSONException {
        return prepayLoan(java.util.UUID.randomUUID().toString(), loanId);
    }

    public synchronized JSONObject prepayLoan(String requestId, String loanId) throws JSONException {
        LoanContract loan = installmentLoanManager.getLoan(loanId);
        if (loan == null) throw new IllegalArgumentException("Kontrak pinjaman tidak ditemukan: " + loanId);
        Account acc = accounts.get(loan.getAccountId());
        if (acc == null) throw new IllegalStateException("Akun peminjam tidak ditemukan");
        long now = clock != null ? clock.getGameTimestamp() : System.currentTimeMillis();
        JSONObject res = loan.prepayFull(requestId, acc, ledger, now);
        persistFinancialMutation();
        if (loan.isClosed()) {
            progressManager.onLoanPaidOff(now);
        }
        return res;
    }

    public synchronized JSONObject restructureLoan(String loanId, int additionalDays) throws JSONException {
        LoanContract loan = installmentLoanManager.getLoan(loanId);
        if (loan == null) throw new IllegalArgumentException("Kontrak pinjaman tidak ditemukan: " + loanId);
        long now = clock != null ? clock.getGameTimestamp() : System.currentTimeMillis();
        JSONObject receipt = loan.restructure(additionalDays, now);
        careerManager.penalizeReputation(5);
        persistFinancialMutation();
        return receipt;
    }

    private void persistFinancialMutation() throws JSONException {
        try { save(); }
        catch (IOException error) { throw new IllegalStateException("Penyimpanan gagal; ulangi permintaan dengan ID yang sama", error); }
    }

    // =========================================================================
    // 3. MISI PERIODIK HARIAN & MINGGUAN (REF:missions, D07)
    // =========================================================================

    public synchronized JSONObject getPeriodicMissionsJson() throws JSONException {
        if (clock != null) {
            periodicMissionManager.updateCalendar(clock.getCurrentDate(), careerManager.getCurrentRank());
        }
        JSONArray activeArr = new JSONArray();
        for (PeriodicMission m : periodicMissionManager.getActiveMissions()) {
            activeArr.put(m.toJsonObject());
        }
        JSONArray archivedArr = new JSONArray();
        for (PeriodicMission m : periodicMissionManager.getArchivedMissions()) {
            archivedArr.put(m.toJsonObject());
        }
        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("activeMissions", activeArr);
        res.put("archivedMissions", archivedArr);
        return res;
    }

    public synchronized JSONObject reviewPortfolioHealth() throws JSONException {
        periodicMissionManager.updateCalendar(clock.getCurrentDate(), careerManager.getCurrentRank());
        periodicMissionManager.recordActivity("HEALTH:" + clock.getCurrentDate(), "HEALTH_CHECK", 1, clock.getCurrentDate());
        JSONObject result = getConsolidatedWealthBreakdown();
        persistFinancialMutation();
        return result;
    }

    public synchronized JSONObject claimPeriodicMission(String missionId) throws JSONException {
        Account personal = getPersonalAccount();
        long now = clock != null ? clock.getGameTimestamp() : System.currentTimeMillis();
        JSONObject res = periodicMissionManager.claimMission(missionId, personal, ledger, now);
        progressManager.awardMissionXp(missionId, res.getInt("xpReward"), now);
        persistFinancialMutation();
        return res;
    }

    // =========================================================================
    // 4. LIGA PERIODIK, RIVALITAS NPC, & LEADERBOARD (REF:ranks, rivals, leaderboard, D09, D13)
    // =========================================================================

    public synchronized JSONObject getLeaderboard() throws JSONException {
        return getLeagueLeaderboard("NET_WORTH");
    }

    public synchronized JSONObject getLeagueLeaderboard(String mode) throws JSONException {
        Money playerWorth = getPersonalNetWorth();
        Account personal = getPersonalAccount();
        String pName = playerProfileManager != null ? playerProfileManager.getPlayerName() : (personal != null ? personal.getName() : "Pemain");
        String pTitle = careerManager != null ? careerManager.getCurrentRank().getTitle() : "Trader";

        if ("WEEKLY_RETURN".equalsIgnoreCase(mode)) {
            Double weeklyRet = computeWeeklyReturnPercent();
            return leagueManager.getLeaderboardByWeeklyReturn(weeklyRet, playerWorth, pName, pTitle);
        } else {
            return leagueManager.getLeaderboardByNetWorth(playerWorth, pName, pTitle);
        }
    }

    public synchronized JSONObject getRivalStatus() throws JSONException {
        Money playerWorth = getPersonalNetWorth();
        Double playerRet = computeWeeklyReturnPercent();
        return leagueManager.getRivalStatus(playerWorth, playerRet);
    }

    // =========================================================================
    // 5. GAYA INVESTOR (REF:archetype, D15) & PROFIL PEMAIN (REF:profile, D16)
    // =========================================================================

    public synchronized JSONObject getInvestorArchetype() throws JSONException {
        Account personal = getPersonalAccount();
        List<TradeFill> fills = new ArrayList<>();
        for (Instrument instrument : catalog.getActiveUniverse()) {
            OrderBook book = marketEngine.getBook(instrument.getId());
            if (book != null) fills.addAll(book.getTradeHistory());
        }
        int days = playerProfileManager.getRunDaysPlayed();
        Money netWorth = getPersonalNetWorth();
        Money marginDebt = personal != null ? riskService.getMarginDebt(personal.getId(), Currency.IDR) : Money.zero(Currency.IDR);
        return InvestorArchetypeService.evaluateArchetype(fills, days, personal, netWorth, marginDebt, fxService.getMidRate(), clock.getGameTimestamp());
    }

    public synchronized JSONObject getPlayerProfileJson() throws JSONException {
        JSONObject res = playerProfileManager.toJsonObject();
        res.put("careerRank", careerManager != null ? careerManager.getCurrentRank().getTitle() : "-");
        res.put("reputation", careerManager != null ? careerManager.getReputationScore() : 0);
        res.put("currentLevel", progressManager.getCurrentLevel());
        res.put("currentXp", progressManager.getCurrentXp());
        res.put("netWorth", getPersonalNetWorth().format());
        res.put("performance", getPerformanceReport());
        return res;
    }

    public synchronized JSONObject updatePlayerProfile(String name, String textSize, boolean highContrast, boolean reducedMotion) throws JSONException {
        if (name != null && !name.trim().isEmpty()) {
            playerProfileManager.setPlayerName(name);
        }
        playerProfileManager.setPreferences(textSize, highContrast, reducedMotion);
        persistFinancialMutation();
        return getPlayerProfileJson();
    }

    // =========================================================================
    // 6. KEKAYAAN KONSOLIDASI (D02) & RETURN METHODOLOGY (D13)
    // =========================================================================

    public synchronized JSONObject getConsolidatedWealthBreakdown() throws JSONException {
        Account personal = getPersonalAccount();
        BigDecimal usdRate = fxService != null ? fxService.getMidRate() : BigDecimal.valueOf(15800);

        Money personalCash = personal != null ? personal.getTotalCash(Currency.IDR) : Money.zero(Currency.IDR);
        if (personal != null) {
            personalCash = personalCash.add(new Money(personal.getTotalCash(Currency.USD).getAmount().multiply(usdRate), Currency.IDR));
        }

        Money securitiesVal = Money.zero(Currency.IDR);
        if (personal != null) {
            for (Position p : personal.getPositions().values()) {
                Money val = p.getMarketValue();
                if (val.getCurrency() == Currency.IDR) {
                    securitiesVal = securitiesVal.add(val);
                } else {
                    securitiesVal = securitiesVal.add(new Money(val.getAmount().multiply(usdRate), Currency.IDR));
                }
            }
        }

        Money fundUnitsVal = mutualFundManager.calculateTotalHoldingsValue(personal != null ? personal.getId() : "");
        Money collectionVal = collectionManager.calculateTotalValuation();

        Money companyEquityVal = Money.zero(Currency.IDR);
        for (com.tradingglobal3d.core.business.OperatingCompany comp : companyManager.getAllCompanies()) {
            Account compAccount = accounts.get(comp.getAccountId());
            if (compAccount != null) {
                Money cVal = compAccount.getTotalPortfolioValue(Currency.IDR, usdRate);
                Money cDebt = installmentLoanManager.getTotalOutstandingLiability(compAccount.getId());
                companyEquityVal = companyEquityVal.add(cVal.subtract(cDebt).subtract(comp.getPayrollArrears()));
            }
        }

        Money marginDebt = personal != null ? riskService.getMarginDebt(personal.getId(), Currency.IDR) : Money.zero(Currency.IDR);
        Money installmentDebt = personal != null ? installmentLoanManager.getTotalOutstandingLiability(personal.getId()) : Money.zero(Currency.IDR);
        Money totalLiabilities = marginDebt.add(installmentDebt).add(collectionManager.getTotalMaintenanceLiability());

        Money grossAssets = personalCash.add(securitiesVal).add(fundUnitsVal).add(collectionVal).add(companyEquityVal);
        Money netWorth = grossAssets.subtract(totalLiabilities);

        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("totalNetWorth", netWorth.format());
        res.put("totalNetWorthRaw", netWorth.getAmount().toPlainString());
        res.put("grossAssets", grossAssets.format());
        res.put("grossAssetsRaw", grossAssets.getAmount().toPlainString());
        res.put("totalLiabilities", totalLiabilities.format());
        res.put("totalLiabilitiesRaw", totalLiabilities.getAmount().toPlainString());

        JSONObject compObj = new JSONObject();
        compObj.put("cash", personalCash.format());
        compObj.put("securities", securitiesVal.format());
        compObj.put("mutualFunds", fundUnitsVal.format());
        compObj.put("collections", collectionVal.format());
        compObj.put("companiesEquity", companyEquityVal.format());
        res.put("components", compObj);

        JSONObject liabObj = new JSONObject();
        liabObj.put("marginDebt", marginDebt.format());
        liabObj.put("installmentDebt", installmentDebt.format());
        liabObj.put("collectionMaintenance", collectionManager.getTotalMaintenanceLiability().format());
        res.put("liabilities", liabObj);

        res.put("isReconciled", true);
        return res;
    }

    public synchronized JSONObject getPrestigeStatus() throws JSONException {
        JSONArray blockers = new JSONArray();
        if (!careerManager.hasRank(CareerRank.PORTFOLIO_MANAGER)) blockers.put("Perlu jabatan Portfolio Manager");
        if (playerProfileManager.getRunDaysPlayed() < 21) blockers.put("Perlu minimal tiga minggu pada perjalanan ini");
        if (!firmManager.getActiveMandates().isEmpty()) blockers.put("Selesaikan kewajiban mandat klien");
        for (LoanContract loan : installmentLoanManager.getAllLoans()) if (!loan.getTotalPayoffAmount().isZero()) { blockers.put("Lunasi seluruh pinjaman"); break; }
        for (Account account : accounts.values()) {
            for (Currency currency : Currency.values()) {
                if (riskService.getMarginDebt(account.getId(), currency).isPositive()) blockers.put("Lunasi utang margin");
                if (account.getReservedCash(currency).isPositive()) blockers.put("Selesaikan seluruh reservasi transaksi");
            }
        }
        if (!marketEngine.getRestingOrders().isEmpty()) blockers.put("Batalkan seluruh antrean order");
        for (var company : companyManager.getAllCompanies()) if (company.getPayrollArrears().isPositive()) { blockers.put("Bayar tunggakan gaji"); break; }
        if (collectionManager.getTotalMaintenanceLiability().isPositive()) blockers.put("Bayar tunggakan pemeliharaan koleksi");
        for (MutualFund fund : mutualFundManager.getAllFunds()) {
            JSONArray orders = fund.toJsonObject(personalAccountId).getJSONArray("orders");
            for (int i=0; i<orders.length(); i++) {
                String status = orders.getJSONObject(i).getString("status");
                if (status.equals("ACCEPTED") || status.startsWith("PENDING_")) blockers.put("Selesaikan antrean reksa dana");
            }
        }
        return new JSONObject().put("success", true).put("eligible", blockers.length() == 0).put("runId", runId)
                .put("currentRank", careerManager.getCurrentRank().getTitle()).put("requiredRank", CareerRank.PORTFOLIO_MANAGER.getTitle())
                .put("runDays", playerProfileManager.getRunDaysPlayed()).put("minimumRunDays", 21).put("blockers", blockers)
                .put("description", "Pilihan memulai perjalanan baru. Identitas, preferensi, statistik lifetime dan riwayat era dipertahankan.")
                .put("prestigeBonus", "Badge kosmetik Era Baru")
                .put("reset", "Kas kembali Rp50 juta; posisi, order, bisnis, pegawai, koleksi, XP dan karier perjalanan direset. Seluruh kewajiban harus diselesaikan dahulu.")
                .put("retained", "Identitas, preferensi, statistik lifetime, riwayat prestige dan backup perjalanan lama");
    }

    public synchronized JSONObject confirmPrestige(String requestId, String expectedRunId) throws JSONException {
        if (requestId == null || !requestId.matches("[A-Za-z0-9_-]{1,160}")) throw new IllegalArgumentException("ID prestige tidak valid");
        JSONArray history = playerProfileManager.exportState().getJSONArray("prestigeHistory");
        for (int i=0; i<history.length(); i++) {
            JSONObject receipt = history.getJSONObject(i);
            if (!requestId.equals(receipt.getString("requestId"))) continue;
            if (!expectedRunId.equals(receipt.getString("oldRunId"))) throw new IllegalArgumentException("ID prestige digunakan untuk perjalanan berbeda");
            persistFinancialMutation();
            return new JSONObject(receipt.toString());
        }
        if (!runId.equals(expectedRunId)) throw new IllegalStateException("Perjalanan berubah; tinjau ulang prestige");
        JSONObject preview = getPrestigeStatus();
        if (!preview.getBoolean("eligible")) throw new IllegalStateException(preview.getJSONArray("blockers").join("; "));
        JSONObject retained = playerProfileManager.exportState();
        String oldRunId = runId;
        long timestamp = clock.getGameTimestamp();
        String backup = null;
        try {
            save();
            backup = saveManager.createPrestigeBackup(requestId);
            initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
            playerProfileManager.retainAcrossPrestige(retained);
            JSONObject receipt = playerProfileManager.recordPrestige(requestId, oldRunId, runId, timestamp, backup);
            save();
            return receipt;
        } catch (Exception failure) {
            if (backup != null) {
                try {
                    saveManager.restorePrestigeBackup(backup);
                    if (!load()) throw new IllegalStateException("Backup tidak dapat dimuat");
                } catch (Exception restoreFailure) {
                    recoveryRequired = true;
                    throw new IllegalStateException("Prestige gagal dan perlu pemulihan dari " + backup, restoreFailure);
                }
            }
            throw new IllegalStateException("Prestige tidak diselesaikan; perjalanan lama dipertahankan", failure);
        }
    }

    private static final class LeaderboardEntry {
        final int rank;
        final String name;
        final Money netWorth;
        final String title;
        final boolean isPlayer;

        LeaderboardEntry(int rank, String name, Money netWorth, String title, boolean isPlayer) {
            this.rank = rank;
            this.name = name;
            this.netWorth = netWorth;
            this.title = title;
            this.isPlayer = isPlayer;
        }
    }
}
