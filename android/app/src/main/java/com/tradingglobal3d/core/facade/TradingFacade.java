package com.tradingglobal3d.core.facade;

import com.tradingglobal3d.core.career.CareerManager;
import com.tradingglobal3d.core.career.CareerMission;
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
import com.tradingglobal3d.core.market.HistoricalBar;
import com.tradingglobal3d.core.market.HistoricalDatasetLoader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
    private static final String RULES_VERSION = "2026.10.24.24x";
    private boolean recoveryRequired;

    public synchronized boolean startOrResume() {
        if (saveManager.getSaveFile().exists() || saveManager.getBackupFile().exists()) {
            boolean loaded = load();
            recoveryRequired = !loaded;
            if (loaded) {
                resumeWorld();
            }
            return loaded;
        }
        initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        persistFinancialMutation();
        return true;
    }

    private void resumeWorld() {
        long now = realTimeClock.millis();
        if (lastRealTimeAnchor > 0 && now > lastRealTimeAnchor) reconcileOfflineElapsed(now - lastRealTimeAnchor);
        this.lastRealTimeAnchor = Math.max(lastRealTimeAnchor, now);
        installmentLoanManager.processRealTimeAccrual(now);
        clock.setForeground(true);
        persistFinancialMutation();
    }

    private void saveBeforeModeSwitch() throws IOException, JSONException {
        boolean foreground = clock.isForeground();
        try { onAppLifecycleChanged(false); save(); }
        finally { clock.setForeground(foreground); }
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
    // ponytail: only the shipped 26-company baseline is migratable; future expansions need their own versioned baseline.
    private static final Set<String> LEGACY_CATALOG_IDS = new HashSet<>(Arrays.asList(
            "IDX:BBCA", "IDX:BMRI", "IDX:BBRI", "IDX:TLKM", "IDX:ASII", "IDX:UNVR", "IDX:ICBP", "IDX:GOTO", "IDX:ANTM", "IDX:ADRO",
            "US:AAPL", "US:MSFT", "US:GOOGL", "US:AMZN", "US:NVDA", "US:TSLA", "US:META", "US:JNJ", "US:JPM", "US:BRK.B",
            "IDX:BBNI", "IDX:KLBF", "IDX:PGAS", "US:DIS", "US:NFLX", "US:WMT"));
    private static final Set<String> LEGACY_PASSIVE_IDS = new HashSet<>(Arrays.asList(
            "IDX:BBNI", "IDX:KLBF", "IDX:PGAS", "US:DIS", "US:NFLX", "US:WMT"));
    private final MarketEngine marketEngine = new MarketEngine();
    private final FxExchangeService fxService = new FxExchangeService();
    private final OrderManager orderManager = new OrderManager();
    private final CareerManager careerManager = new CareerManager();
    private final FirmManager firmManager = new FirmManager();
    private final ResearchService researchService = new ResearchService();
    private final FundBuilderService fundBuilder = new FundBuilderService();
    private final MultiAssetTradingService multiAssetTrading = new MultiAssetTradingService(fundBuilder);
    private final CorporateActionService corporateService = new CorporateActionService();
    private final RiskComplianceService riskService = new RiskComplianceService();
    private final HistoricalReplayEngine replayEngine = new HistoricalReplayEngine();
    private final PlayerProgressManager progressManager = new PlayerProgressManager();
    private final com.tradingglobal3d.core.business.CompanyManager companyManager = new com.tradingglobal3d.core.business.CompanyManager();
    private final com.tradingglobal3d.core.collection.CollectionManager collectionManager = new com.tradingglobal3d.core.collection.CollectionManager();
    private final MutualFundManager mutualFundManager = new MutualFundManager();
    private final InstallmentLoanManager installmentLoanManager = new InstallmentLoanManager();
    private final com.tradingglobal3d.core.multiasset.TimeDepositManager timeDepositManager = new com.tradingglobal3d.core.multiasset.TimeDepositManager();
    private final PeriodicMissionManager periodicMissionManager = new PeriodicMissionManager();
    private final LeagueManager leagueManager = new LeagueManager();
    private final PlayerProfileManager playerProfileManager = new PlayerProfileManager();
    private final com.tradingglobal3d.core.boss.MarketBossService marketBossService = new com.tradingglobal3d.core.boss.MarketBossService();
    private final com.tradingglobal3d.core.risk.TaxAuditService taxAuditService = new com.tradingglobal3d.core.risk.TaxAuditService();
    private final com.tradingglobal3d.core.risk.PortfolioInsuranceService portfolioInsuranceService = new com.tradingglobal3d.core.risk.PortfolioInsuranceService();
    private final com.tradingglobal3d.core.research.ResearchTechTreeService researchTechTreeService = new com.tradingglobal3d.core.research.ResearchTechTreeService();
    private final com.tradingglobal3d.core.firm.HeadhuntingService headhuntingService = new com.tradingglobal3d.core.firm.HeadhuntingService();
    private final com.tradingglobal3d.core.player.DailyStreakService dailyStreakService = new com.tradingglobal3d.core.player.DailyStreakService();
    private final com.tradingglobal3d.core.market.SectorDominanceService sectorDominanceService = new com.tradingglobal3d.core.market.SectorDominanceService();
    private final List<JSONObject> customMutualFunds = new ArrayList<>();
    private final Map<String, JSONObject> dayCloseReceipts = new LinkedHashMap<>();
    private final Map<String, JSONObject> skipDayReceipts = new LinkedHashMap<>();
    private final Map<String, StrategyLabSandbox> strategyLabs = new LinkedHashMap<>();
    public static final int GAME_SECONDS_PER_REAL_SECOND = (int) GameClock.FOREGROUND_GAME_SECONDS_PER_REAL_SECOND;

    // A12: Waktu Nyata Asia/Jakarta (00.00 WIB) untuk Hibah Harian
    private java.time.Clock realTimeClock = java.time.Clock.system(java.time.ZoneId.of("Asia/Jakarta"));
    private String dailySponsorLastClaimedRealDate = null;
    private long dailySponsorLastClaimedRealTimestamp = 0L;

    public synchronized void setRealTimeClock(java.time.Clock clock) {
        this.realTimeClock = Objects.requireNonNull(clock);
    }

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
            report.put("timestamp", realTimeClock.millis());
        } catch (JSONException ignored) {}
        this.lastOfflineCatchupReport = report;
        this.lastRealTimeAnchor = Math.max(lastRealTimeAnchor, realTimeClock.millis());
        return report;
    }

    public synchronized JSONObject onAppLifecycleChanged(boolean foreground) {
        if (clock == null || foreground == clock.isForeground()) return null;
        long now = realTimeClock.millis();
        JSONObject result = null;
        if (!foreground) {
            if (now > lastRealTimeAnchor) advanceRealMs(now - lastRealTimeAnchor, true);
            this.lastRealTimeAnchor = Math.max(lastRealTimeAnchor, now);
            clock.setForeground(false);
            try {
                save();
            } catch (Exception ignored) {}
        } else {
            if (!clock.isForeground() && lastRealTimeAnchor > 0 && now > lastRealTimeAnchor) {
                result = reconcileOfflineElapsed(now - lastRealTimeAnchor);
            }
            this.lastRealTimeAnchor = Math.max(lastRealTimeAnchor, now);
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

    public synchronized JSONObject createHoldingCompany(String holdingId, String holdingName, JSONArray subsidiaries)
            throws JSONException {
        JSONObject holding = companyManager.createHoldingCompany(holdingId, holdingName, subsidiaries);
        persistFinancialMutation();
        return holding;
    }

    public synchronized JSONObject transferHoldingCapital(String holdingId, String fromCompanyId, String toCompanyId,
            Money amount, String transferType) throws JSONException {
        return transferHoldingCapital(UUID.randomUUID().toString(), holdingId, fromCompanyId, toCompanyId, amount, transferType);
    }

    public synchronized JSONObject transferHoldingCapital(String requestId, String holdingId, String fromCompanyId,
            String toCompanyId, Money amount, String transferType) throws JSONException {
        JSONObject receipt = companyManager.transferHoldingCapital(holdingId, fromCompanyId, toCompanyId, amount,
                transferType, accounts, ledger, clock.getGameTimestamp(), requestId);
        persistFinancialMutation();
        return receipt;
    }

    public synchronized JSONArray getHoldingCompaniesJson() throws JSONException {
        return companyManager.getHoldingCompaniesJson();
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

    public synchronized JSONObject previewCompanyAction(String companyId, String action, int hours, String benefitTier) throws JSONException {
        careerManager.requireRank(CareerRank.NOVICE_TRADER, "Pengelolaan perusahaan");
        return companyManager.previewCompanyAction(companyId, action, hours, benefitTier);
    }

    public synchronized JSONObject executeCompanyAction(String requestId, String companyId, String action, int hours, String benefitTier, long version) throws JSONException {
        careerManager.requireRank(CareerRank.NOVICE_TRADER, "Pengelolaan perusahaan");
        var company = companyManager.getCompany(companyId);
        if (company == null) throw new IllegalArgumentException("Perusahaan tidak ditemukan");
        JSONObject result = companyManager.executeCompanyAction(requestId, companyId, action, hours, benefitTier, version,
                accounts.get(company.getAccountId()), ledger, clock.getGameTimestamp());
        persistFinancialMutation();
        return result;
    }

    public synchronized JSONObject companyCapitalAction(String requestId, String companyId, boolean toCompany, Money amount, String version) throws JSONException {
        var company = companyManager.getCompany(companyId);
        if (company == null || getPersonalAccount() == null) throw new IllegalArgumentException("Perusahaan tidak ditemukan");
        Account companyAccount = accounts.get(company.getAccountId());
        if (requestId == null) return companyManager.previewCapitalTransfer(companyId, toCompany, getPersonalAccount(), companyAccount, amount);
        JSONObject result = companyManager.executeCapitalTransfer(requestId, companyId, toCompany, getPersonalAccount(), companyAccount, amount, version, ledger, clock.getGameTimestamp());
        persistFinancialMutation();
        return result;
    }

    public synchronized JSONObject multiAssetOrder(String requestId, String id, boolean buy, long quantity, String version) throws JSONException {
        if (gameMode == GameMode.HISTORICAL_REPLAY) throw new IllegalStateException("Replay hanya mendukung instrumen dataset");
        careerManager.requireRank(CareerRank.SENIOR_ANALYST, "Perdagangan multi-aset");
        Account personal = getPersonalAccount(), dealer = accounts.get("ACC-NPC-MM-01");
        multiAssetTrading.updateValuations(catalog, fxService, accounts, clock.getGameTimestamp());
        if (requestId == null) return multiAssetTrading.quote(id, buy, quantity, personal, dealer, clock.getGameTimestamp());
        JSONObject result = multiAssetTrading.transact(requestId, id, buy, quantity, version, personal, dealer, ledger, clock.getGameTimestamp());
        String fillId = "MULTI-" + requestId;
        if (!playerProfileManager.hasRecordedFill(fillId)) {
            Money realized = result.isNull("realizedNetPnL") ? null : new Money(new BigDecimal(result.getString("realizedNetPnL")), Currency.IDR);
            playerProfileManager.recordFillResult(fillId, realized);
            periodicMissionManager.recordActivity("TRADE:" + fillId, "TRADE", 1, clock.getCurrentDate());
        }
        persistFinancialMutation();
        return result;
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
        return transactCollection(requestId, itemId, buy, null);
    }

    public synchronized JSONObject transactCollection(String requestId, String itemId, boolean buy, BigDecimal expectedPrice) throws JSONException {
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Permainan belum siap");
        JSONObject receipt = buy ? collectionManager.buyItem(requestId, itemId, personal, ledger, careerManager, clock.getGameTimestamp(), expectedPrice)
                : collectionManager.sellItem(requestId, itemId, personal, ledger, careerManager, clock.getGameTimestamp(), expectedPrice);
        if (receipt.optBoolean("success", false)) {
            persistFinancialMutation();
        }
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

    public synchronized JSONObject researchQuery(JSONObject request) throws JSONException {
        if (gameMode == GameMode.HISTORICAL_REPLAY) throw new IllegalStateException("Dataset replay tidak mencakup riset historis");
        long now = clock.getGameTimestamp();
        switch (request.getString("kind")) {
            case "inbox": return researchService.getAnalystInbox(now);
            case "consensus": return researchService.getConsensus(requireInstrument(request.getString("instrumentId")), now);
            case "alternative": return researchService.getAlternativeData(requireInstrument(request.getString("instrumentId")), now);
            case "comparison":
                List<Instrument> selected = new ArrayList<>();
                JSONArray ids = request.getJSONArray("instrumentIds");
                for (int i = 0; i < ids.length(); i++) selected.add(requireInstrument(ids.getString(i)));
                return researchService.compareInstruments(selected, now);
            default: throw new IllegalArgumentException("Jenis riset tidak dikenal");
        }
    }

    private Instrument requireInstrument(String id) {
        Instrument instrument = catalog.getById(id);
        if (instrument == null) throw new IllegalArgumentException("Instrumen tidak ditemukan: " + id);
        return instrument;
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
        JSONObject capacity = getFirmAumCapacityJson();
        return new JSONObject().put("success", true).put("aumIdr", aum.format())
                .put("personalCashIdr", personal.getTotalCash(Currency.IDR).format()).put("activeClientCount", activeClients)
                .put("aumCapacityIdr", capacity.getString("capacityIdr"))
                .put("aumCapacityUtilizationPct", capacity.getDouble("utilizationPct"))
                .put("aumScaleBonusPct", capacity.getDouble("scaleBonusPercent"))
                .put("valuationRateUsdIdr", fxService.getMidRate().toPlainString()).put("timestamp", System.currentTimeMillis())
                .put("parametersProvisional", true).put("mandates", mandates);
    }

    /**
     * ponytail: baseline capacity is the sum of the current mandate contracts' initial deposits;
     * the SCALE tech node increases that existing, contract-derived baseline by 50%.
     */
    public synchronized JSONObject getFirmAumCapacityJson() throws JSONException {
        BigDecimal rate = fxService.getMidRate();
        BigDecimal base = BigDecimal.ZERO;
        for (ClientMandate mandate : firmManager.getAllMandates()) {
            if (mandate.getStatus() == ClientMandate.MandateStatus.TERMINATED) continue;
            Money deposit = mandate.getInitialDeposit();
            base = base.add(deposit.getCurrency() == Currency.IDR ? deposit.getAmount() : deposit.getAmount().multiply(rate));
        }
        double scaleBonus = 0.0;
        for (com.tradingglobal3d.core.research.ResearchTechTreeService.TechNode node : researchTechTreeService.getAllNodes())
            if (node.isUnlocked() && "AUM_CAPACITY_BOOST".equals(node.getStatBonusType())) scaleBonus += node.getStatBonusValue();
        BigDecimal capacity = base.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(scaleBonus).movePointLeft(2)));
        Money currentAum = getCurrentClientAumIdr();
        double utilization = capacity.signum() > 0
                ? currentAum.getAmount().multiply(BigDecimal.valueOf(100)).divide(capacity, 4, java.math.RoundingMode.HALF_UP).doubleValue()
                : 0.0;
        return new JSONObject().put("baseCapacityIdr", new Money(base, Currency.IDR).format())
                .put("baseCapacityIdrRaw", base.toPlainString()).put("scaleBonusPercent", scaleBonus)
                .put("capacityIdr", new Money(capacity, Currency.IDR).format()).put("capacityIdrRaw", capacity.toPlainString())
                .put("currentAumIdr", currentAum.format()).put("currentAumIdrRaw", currentAum.getAmount().toPlainString())
                .put("remainingCapacityIdr", new Money(capacity.subtract(currentAum.getAmount()).max(BigDecimal.ZERO), Currency.IDR).format())
                .put("utilizationPct", utilization);
    }

    private Money getCurrentClientAumIdr() {
        Money aum = Money.zero(Currency.IDR);
        for (ClientMandate mandate : firmManager.getActiveMandates()) {
            Account account = accounts.get(mandate.getClientAccountId());
            if (account != null) aum = aum.add(account.getTotalPortfolioValue(Currency.IDR, fxService.getMidRate()));
        }
        return aum;
    }

    private void ensureAumCapacity(Money incoming) {
        if (incoming == null || !incoming.isPositive()) throw new IllegalArgumentException("Setoran AUM harus positif");
        BigDecimal amountIdr = incoming.getCurrency() == Currency.IDR
                ? incoming.getAmount() : incoming.getAmount().multiply(fxService.getMidRate());
        try {
            JSONObject capacity = getFirmAumCapacityJson();
            BigDecimal used = new BigDecimal(capacity.getString("currentAumIdrRaw"));
            BigDecimal maximum = new BigDecimal(capacity.getString("capacityIdrRaw"));
            if (used.add(amountIdr).compareTo(maximum) > 0)
                throw new IllegalStateException("Setoran melampaui kapasitas AUM firma; naikkan kapasitas melalui Pohon Riset Skala.");
        } catch (JSONException invalidCapacity) {
            throw new IllegalStateException("Kapasitas AUM firma tidak dapat divalidasi", invalidCapacity);
        }
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
        corporateService.bindShareSupplyCatalog(catalog);
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
        initializeNewGame(mode, initialIdrCapital, GameClock.createDefault());
    }

    private void initializeNewGame(GameMode mode, BigDecimal initialIdrCapital, GameClock startClock) {
        if (initialIdrCapital == null || initialIdrCapital.signum() <= 0)
            throw new IllegalArgumentException("Modal awal harus positif");
        this.gameMode = Objects.requireNonNull(mode);
        this.clock = startClock;
        this.accounts.clear();
        this.ledger.clear();
        this.dayCloseReceipts.clear();
        this.skipDayReceipts.clear();
        this.strategyLabs.clear();
        this.runId = java.util.UUID.randomUUID().toString();
        this.firmManager.reset();
        this.corporateService.reset(clock.getCurrentDate());
        this.riskService.reset();
        this.fundBuilder.reset();
        this.multiAssetTrading.reset();
        this.orderManager.reset();
        this.marketEngine.reset();
        this.progressManager.reset();
        this.careerManager.reset();
        this.fxService.reset();
        this.replayEngine.reset();
        this.companyManager.reset();
        this.collectionManager.reset();
        this.lastOfflineCatchupReport = null;
        this.lastRealTimeAnchor = realTimeClock.millis();
        this.mutualFundManager.reset();
        this.installmentLoanManager.reset();
        this.timeDepositManager.reset();
        this.portfolioInsuranceService.reset();
        this.researchTechTreeService.reset();
        this.headhuntingService.reset();
        this.dailyStreakService.reset();
        this.customMutualFunds.clear();
        this.periodicMissionManager.reset();
        this.leagueManager.reset();
        this.playerProfileManager.reset();
        if (this.clock != null) {
            this.periodicMissionManager.updateCalendar(this.clock.getCurrentDate(), careerManager.getCurrentRank());
        }
        this.researchService.resetForGame(clock.getGameTimestamp(), realTimeClock.millis());
        if (initialCatalogState != null) {
            try {
                if (catalog != null) {
                    catalog.resetToInitialState(initialCatalogState);
                } else {
                    catalog = catalogForSave(new JSONObject().put("catalog", initialCatalogState));
                    catalog.restoreState(initialCatalogState);
                }
            }
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

        // 3b. Buat dan Danai Agen NPC Ritel / Active Taker (Pembeli dan Penjual Aktif)
        String retailAccountId = "ACC-NPC-RETAIL-01";
        Account retailAccount = new Account(retailAccountId, "Investor Ritel Mandiri", AccountType.FIRM, null);
        accounts.put(retailAccountId, retailAccount);
        Money retailCashIdr = Money.of(2_000_000_000L, Currency.IDR);
        Money retailCashUsd = Money.of(200_000.0, Currency.USD);
        ledger.recordMutation("INIT-RETAIL-IDR", retailAccount, LedgerEntryType.INITIAL_CAPITAL, retailCashIdr, true, clock.getGameTimestamp(), "Modal kas IDR Investor Ritel");
        ledger.recordMutation("INIT-RETAIL-USD", retailAccount, LedgerEntryType.INITIAL_CAPITAL, retailCashUsd, true, clock.getGameTimestamp(), "Modal kas USD Investor Ritel");
        for (Instrument inst : catalog.getActiveUniverse()) {
            long initialShares = inst.getLotSize() * 100L;
            Position p = new Position(inst.getTicker(), inst.getExchange().name(), inst.getCurrency(),
                    initialShares, inst.getCurrentPrice().getAmount(), inst.getCurrentPrice().getAmount());
            retailAccount.addPosition(p);
        }
        NpcAgent retailAgent = new NpcAgent("NPC-RETAIL-01", "Investor Ritel Mandiri", NpcStrategyType.RETAIL_NOISE, retailAccount);
        marketEngine.registerNpcAgent(retailAgent);

        // Bind collection manager ke loan manager untuk agunan
        installmentLoanManager.bindCollectionManager(collectionManager);

        // Registrasi pool accounts reksa dana schema3
        mutualFundManager.registerPoolAccounts(accounts, ledger, clock.getGameTimestamp());
        mutualFundManager.bindAccounts(accounts, catalog);
        leagueManager.bindMarket(accounts, ledger, catalog, clock.getGameTimestamp());
        multiAssetTrading.initializeDealer(accounts.get("ACC-NPC-MM-01"));
        collectionManager.updateValuations(clock.getGameTimestamp());

        careerManager.beginDay(personal, fxService.getMidRate(), ledger);
        weeklyBaselineNetWorth = getPersonalNetWorth();
        weeklyPerformance = new com.tradingglobal3d.core.career.PerformancePeriod(clock.getGameTimestamp(), weeklyBaselineNetWorth,
                ledger.getEntriesForAccount(personalAccountId).size());
        runPerformance = weeklyPerformance;
        performanceHistory = new JSONArray();
        recoveryRequired = false;
    }

    public synchronized JSONObject startHistoricalReplay(String datasetId, BigDecimal initialCapital) {
        if (initialCapital != null && initialCapital.signum() <= 0) throw new IllegalArgumentException("Modal awal harus positif");
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

        try { if (clock != null && getPersonalAccount() != null) saveBeforeModeSwitch(); }
        catch (IOException | JSONException error) { throw new IllegalStateException("Simpan progres aktif sebelum membuka replay", error); }
        saveManager = new SaveManager(primarySaveManager.getSaveFile().getParentFile(), "save_replay_v2.json");
        initializeNewGame(GameMode.HISTORICAL_REPLAY, initialCapital != null ? initialCapital : BigDecimal.valueOf(50_000_000), new GameClock(bars.get(0).getDate(), LocalTime.of(9, 0)));
        replayEngine.startReplay(ds, bars);

        com.tradingglobal3d.core.market.HistoricalBar firstBar = bars.get(0);
        this.clock = new GameClock(firstBar.getDate(), LocalTime.of(9, 0, 0));

        Instrument inst = catalog.getById(ds);
        if (inst != null) {
            inst.resetReferencePrice(new Money(firstBar.getOpen(), inst.getCurrency()));
            Account personalAcc = getPersonalAccount();
            if (inst.getCurrency() == Currency.USD && personalAcc != null && personalAcc.getTotalCash(Currency.USD).isZero()) {
                BigDecimal usdRate = fxService != null ? fxService.getMidRate() : BigDecimal.valueOf(15800);
                BigDecimal usdAmount = (initialCapital != null ? initialCapital : BigDecimal.valueOf(50_000_000))
                        .divide(usdRate, 2, java.math.RoundingMode.HALF_UP);
                Money usdMoney = new Money(usdAmount, Currency.USD);
                personalAcc.creditCash(usdMoney);
                ledger.recordMutation("INIT-REPLAY-USD-" + personalAccountId, personalAcc,
                        LedgerEntryType.INITIAL_CAPITAL, usdMoney, true, clock.getGameTimestamp(), "Modal kas USD Replay " + ds);
            }
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
        TradingFacade staged = new TradingFacade(target);
        InstrumentCatalog detached = new InstrumentCatalog();
        Set<String> baseIds = new HashSet<>();
        if (initialCatalogState != null) for (int i = 0; i < initialCatalogState.getJSONArray("instruments").length(); i++)
            baseIds.add(initialCatalogState.getJSONArray("instruments").getJSONObject(i).getString("id"));
        for (Instrument source : catalog.getAllInstruments()) if (baseIds.isEmpty() || baseIds.contains(source.getId())) detached.registerInstrument(new Instrument(source.getId(), source.getTicker(),
                source.getName(), source.getExchange(), source.getSector(), source.getCurrency(), source.getLotSize(),
                source.getCatalogIssuedShares(), source.isActiveTrading(), source.getCurrentPrice()));
        staged.setCatalog(detached);
        if (!staged.load() || staged.getGameMode() != selected)
            throw new IllegalStateException("Progres mode tidak dapat divalidasi; permainan aktif dipertahankan");
        saveBeforeModeSwitch();
        SaveManager previous = saveManager;
        saveManager = target;
        if (!load()) { saveManager = previous; throw new IllegalStateException("Progres mode tidak dapat divalidasi"); }
        resumeWorld();
        return new JSONObject(getSnapshotJson()).put("success", true);
    }

    public synchronized void startInternalGame(BigDecimal capital) {
        if (capital == null || capital.signum() <= 0) throw new IllegalArgumentException("Modal awal harus positif");
        if (clock != null) persistFinancialMutation();
        saveManager = primarySaveManager;
        initializeNewGame(GameMode.INTERNAL_MARKET, capital);
    }

    public synchronized String setClockState(boolean playing, int speed) throws JSONException {
        if (clock == null || recoveryRequired) throw new IllegalStateException("Permainan belum dapat dijalankan");
        clock.setSpeedMultiplier(1);
        // Legacy controls cannot change the automatic world's tempo.
        clock.play();
        return getSnapshotJson();
    }

    public synchronized GameMode getGameMode() { return gameMode; }
    public synchronized boolean isRecoveryRequired() { return recoveryRequired; }
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
                    clock.getGameTimestamp(),
                    "Setoran modal awal mandat klien " + name
            );
        }
        return clientAcc;
    }

    public synchronized void registerAccount(Account account) {
        if (account != null) accounts.put(account.getId(), account);
    }

    public synchronized LedgerEntry deposit(String requestId, String accountId, Money amount, String description) {
        Account acc = accounts.get(accountId);
        if (acc == null) throw new IllegalArgumentException("Akun tidak ditemukan: " + accountId);
        return ledger.recordMutation(requestId, acc, LedgerEntryType.DEPOSIT, amount, true, clock.getGameTimestamp(), description);
    }

    public synchronized LedgerEntry withdraw(String requestId, String accountId, Money amount, String description) {
        Account acc = accounts.get(accountId);
        if (acc == null) throw new IllegalArgumentException("Akun tidak ditemukan: " + accountId);
        return ledger.recordMutation(requestId, acc, LedgerEntryType.WITHDRAWAL, amount, false, clock.getGameTimestamp(), description);
    }

    public synchronized long getCommittedShares(String accountId, String instrumentId) {
        if (!accounts.containsKey(accountId)) throw new IllegalArgumentException("Akun tidak ditemukan: " + accountId);
        requireInstrument(instrumentId);
        long committed = corporateService.reservedCorporateShares(accountId, instrumentId);
        for (Order order : orderManager.getOrdersForAccount(accountId))
            if (order.isSell() && order.getStatus() == OrderStatus.PENDING_TRIGGER
                    && instrumentId.equals(order.getInstrumentId()))
                committed = Math.addExact(committed, order.getRemainingQuantity());
        OrderBook book = marketEngine.getBook(instrumentId);
        if (book != null) for (Queue<Order> level : book.getAsks().values()) for (Order order : level)
            if (order.isSell() && !order.getStatus().isFinal() && accountId.equals(order.getAccountId()))
                committed = Math.addExact(committed, order.getRemainingQuantity());
        return committed;
    }

    public synchronized JSONObject getOrderCapacity(String accountId, String instrumentId, String sideName,
                                                     String typeName, String priceText) throws JSONException {
        Account account = accounts.get(accountId);
        Instrument instrument = requireInstrument(instrumentId);
        if (account == null || (account.getType() != AccountType.PERSONAL && account.getType() != AccountType.CLIENT))
            throw new IllegalArgumentException("Akun transaksi tidak tersedia");
        OrderSide side = OrderSide.valueOf(sideName);
        OrderType type = OrderType.valueOf(typeName);
        long lot = instrument.getLotSize();
        BigDecimal price = type == OrderType.LIMIT ? new BigDecimal(priceText) : instrument.getCurrentPrice().getAmount();
        if (price.signum() <= 0 || lot <= 0) throw new IllegalArgumentException("Harga atau ukuran lot tidak tersedia");
        Order sample = new Order("CAPACITY", "CAPACITY", accountId, instrumentId, side, type,
                type == OrderType.LIMIT ? new Money(price, instrument.getCurrency()) : null,
                type == OrderType.STOP_LOSS || type == OrderType.TAKE_PROFIT ? new Money(new BigDecimal(priceText), instrument.getCurrency()) : null,
                lot, clock.getGameTimestamp(), null);
        applySectorFeeDiscount(sample, account, instrument);
        Money cash = account.getAvailableCash(instrument.getCurrency());
        Position position = account.getPosition(instrumentId);
        long held = position == null ? 0 : Math.max(0, position.getQuantity());
        long committed = getCommittedShares(accountId, instrumentId);
        OrderBook book = marketEngine.getOrCreateBook(instrument);
        long availableShares = Math.max(0, held - committed), maxLots;
        if (side == OrderSide.SELL) maxLots = availableShares / lot;
        else {
            BigDecimal unit = price.multiply(BigDecimal.valueOf(lot));
            BigDecimal fee = unit.multiply(sample.getEffectiveFeeRate(MatchingEngine.BUY_FEE_RATE))
                    .setScale(instrument.getCurrency().getDecimalPlaces(), java.math.RoundingMode.CEILING);
            maxLots = cash.getAmount().divide(unit.add(fee), 0, java.math.RoundingMode.DOWN).min(BigDecimal.valueOf(Long.MAX_VALUE / lot)).longValueExact();
            if (type == OrderType.MARKET) {
                long liquidity = 0;
                for (Queue<Order> level : book.getAsks().values()) for (Order order : level)
                    if (!order.getStatus().isFinal() && !accountId.equals(order.getAccountId())) liquidity = Math.addExact(liquidity, order.getRemainingQuantity());
                long low = 0, high = Math.min(maxLots, liquidity / lot);
                while (low < high) {
                    long middle = low + (high - low + 1) / 2;
                    Order candidate = new Order("CAPACITY", "CAPACITY", accountId, instrumentId, side, type, null, null,
                            middle * lot, clock.getGameTimestamp(), null);
                    candidate.setFeeDiscountPercent(sample.getFeeDiscountPercent());
                    JSONObject quote = marketEngine.getMatchingEngine().previewOrder(candidate, account, instrument, book, clock.getGameTimestamp());
                    if (quote.optBoolean("success") && quote.optLong("estimatedFillQuantity") == middle * lot) low = middle;
                    else high = middle - 1;
                }
                maxLots = low;
            }
        }
        return new JSONObject().put("success", true).put("accountId", accountId).put("instrumentId", instrumentId)
                .put("lotSize", lot).put("currency", instrument.getCurrency().name()).put("maxLots", maxLots)
                .put("availableShares", availableShares).put("availableCashRaw", cash.getAmount().toPlainString())
                .put("asOf", clock.getGameTimestamp());
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
        applySectorFeeDiscount(order, account, instrument);
        JSONObject preview = marketEngine.getMatchingEngine().previewOrder(order, account, instrument,
                marketEngine.getOrCreateBook(instrument), clock.getGameTimestamp());
        return preview.put("mode", gameMode.getDisplayName());
    }

    public static int getRequiredLevelForSector(String sector, com.tradingglobal3d.core.market.MarketExchange exchange) {
        if (exchange == com.tradingglobal3d.core.market.MarketExchange.US) {
            return 7; // Saham Global Wall Street terbuka di Level 7+
        }
        if (sector == null) return 1;
        String s = sector.toLowerCase(java.util.Locale.ROOT);
        // Level 1-3 (Investor Pemula): Keuangan & Konsumer
        if (s.contains("keuangan") || s.contains("finance") || s.contains("bank")
                || s.contains("konsumer") || s.contains("consumer") || s.contains("retail")) {
            return 1;
        }
        // Level 4-6 (Analis Madya): Energi, Pertambangan Mineral, Properti, Infrastruktur
        if (s.contains("energi") || s.contains("tambang") || s.contains("mineral")
                || s.contains("properti") || s.contains("infrastruktur") || s.contains("industri")) {
            return 4;
        }
        // Level 7-10 (Fund Manager Elit): Teknologi, Global
        if (s.contains("teknologi") || s.contains("tech") || s.contains("digital")) {
            return 7;
        }
        return 1;
    }

    public synchronized boolean isSectorUnlocked(String sector, com.tradingglobal3d.core.market.MarketExchange exchange) {
        int playerLevel = progressManager != null ? progressManager.getCurrentLevel() : 1;
        return playerLevel >= getRequiredLevelForSector(sector, exchange);
    }

    private void applySectorFeeDiscount(Order order, Account account, Instrument instrument) {
        if (account.getType() != AccountType.PERSONAL) {
            order.setFeeDiscountPercent(0.0);
            return;
        }
        com.tradingglobal3d.core.market.SectorDominanceService.SectorStatus status = sectorDominanceService
                .evaluateDominance(account, catalog, fxService.getMidRate()).get(instrument.getSector());
        double discount = status != null && status.isDominant() ? status.getFeeDiscountPct() : 0.0;
        for (com.tradingglobal3d.core.research.ResearchTechTreeService.TechNode node : researchTechTreeService.getAllNodes())
            if (node.isUnlocked() && "BROKER_FEE_REDUCTION".equals(node.getStatBonusType())) discount += node.getStatBonusValue();
        order.setFeeDiscountPercent(Math.min(100.0, discount));
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
        if (gameMode == GameMode.HISTORICAL_REPLAY) {
            var replayBar = replayEngine.getCurrentBar();
            if (!replayEngine.supportsInstrument(inst.getId()) || replayBar == null || !replayBar.getDate().equals(clock.getCurrentDate())
                    || clock.getCurrentTime().isBefore(LocalTime.of(9, 0)) || !clock.getCurrentTime().isBefore(LocalTime.of(16, 0)))
                throw new IllegalStateException("Instrumen atau sesi replay tidak tersedia pada waktu ini");
        }
        // Validasi gembok sektor berdasarkan Level Pangkat Investor (Tahap E1 / Fitur 4)
        if (order.isBuy() && acc.getType() == AccountType.PERSONAL && gameMode != GameMode.HISTORICAL_REPLAY) {
            int playerLevel = progressManager != null ? progressManager.getCurrentLevel() : 1;
            int reqLevel = getRequiredLevelForSector(inst.getSector(), inst.getExchange());
            if (playerLevel < reqLevel) {
                order.setStatus(OrderStatus.REJECTED);
                orderManager.registerOrder(order);
                return new MatchingEngine.OrderResult(order, false,
                        "Sektor " + inst.getSector() + " terkunci. Capai Level " + reqLevel + " untuk membuka transaksi di sektor ini.", Collections.emptyList());
            }
        }
        if (order.isSell()) {
            Position position = acc.getPosition(inst.getId());
            long committed = getCommittedShares(acc.getId(), inst.getId());
            if (position != null && position.getQuantity() - committed < order.getTotalQuantity()) {
                order.setStatus(OrderStatus.REJECTED);
                orderManager.registerOrder(order);
                return new MatchingEngine.OrderResult(order, false,
                        "Saham telah direservasi oleh aksi korporasi atau order jual aktif", Collections.emptyList());
            }
        }

        applySectorFeeDiscount(order, acc, inst);
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

        if (gameMode == GameMode.HISTORICAL_REPLAY) {
            if (!replayEngine.supportsInstrument(inst.getId())) throw new IllegalArgumentException("Instrumen tidak termasuk dataset replay aktif");
            var replayBar = replayEngine.getCurrentBar();
            if (replayBar != null && replayEngine.canFillOrderOnHistoricalBar(order, replayBar)) {
                Account dealer = accounts.get("ACC-NPC-MM-01");
                if (dealer != null) {
                    Money matchPrice = order.getLimitPrice() != null ? order.getLimitPrice() : new Money(replayBar.getOpen(), inst.getCurrency());
                    OrderSide counterSide = order.getSide() == OrderSide.BUY ? OrderSide.SELL : OrderSide.BUY;
                    String reqId = "REPLAY-FILL-" + order.getId();
                    Order counterOrder = new Order(reqId, reqId, dealer.getId(), inst.getId(), counterSide, OrderType.LIMIT,
                            matchPrice, null, order.getTotalQuantity(), clock.getGameTimestamp(), Money.zero(inst.getCurrency()));
                    orderManager.registerOrder(counterOrder);
                    marketEngine.submitOrder(counterOrder, dealer, inst, ledger, accounts, clock.getGameTimestamp());
                }
            } else {
                refreshReplayLiquidity();
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
        ensureAumCapacity(mandate.getInitialDeposit());
        Account clientAcc = createClientAccount(mandate.getClientName(), mandate.getId(), mandate.getInitialDeposit());
        mandate.setClientAccountId(clientAcc.getId());
        progressManager.onMandateAccepted(clock.getGameTimestamp());
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
        ensureAumCapacity(amount);
        Account clientAcc = accounts.get(mandate.getClientAccountId());
        firmManager.processClientDeposit(mandate, clientAcc, amount, ledger, clock.getGameTimestamp());
        persistFinancialMutation();
    }

    public synchronized boolean clientWithdraw(String mandateId, Money amount) {
        ClientMandate mandate = firmManager.getMandate(mandateId);
        if (mandate == null || mandate.getClientAccountId() == null) {
            throw new IllegalArgumentException("Mandat aktif tidak ditemukan: " + mandateId);
        }
        Account clientAcc = accounts.get(mandate.getClientAccountId());
        return firmManager.processClientWithdrawal(mandate, clientAcc, amount, ledger, clock.getGameTimestamp());
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
        long realNow = System.currentTimeMillis();
        long gameTimestamp = clock != null ? clock.getGameTimestamp() : realNow;
        FxExchangeService.ConversionResult result = fxService.executeReviewedConversion(
                requestId, quoteId, account, from, to, amount, realNow, gameTimestamp, ledger);
        if (!result.isSuccess) return new JSONObject().put("success", false).put("message", result.message).put("requestId", requestId);
        persistFinancialMutation();
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
        long realNow = System.currentTimeMillis();
        long gameTimestamp = clock != null ? clock.getGameTimestamp() : realNow;
        FxQuote quote = fxService.getOrGenerateQuote(realNow);
        FxExchangeService.ConversionResult result = fxService.executeConversion(
                requestId, acc, quote, from, to, amount, realNow, gameTimestamp, ledger);
        if (result.isSuccess) persistFinancialMutation();
        return result;
    }

    public synchronized boolean cancelOrder(Order order) {
        Account acc = accounts.get(order.getAccountId());
        if (acc == null) return false;
        if (orderManager.cancelPendingTrigger(order.getRequestId())) return true;
        return marketEngine.cancelOrder(order, acc);
    }

    public synchronized int expireDayOrders() {
        int count = 0;
        for (Order order : orderManager.getAllOrders()) {
            if (!order.getStatus().isFinal()) {
                Account account = accounts.get(order.getAccountId());
                if (account != null) {
                    if (order.getStatus() == OrderStatus.PENDING_TRIGGER) {
                        orderManager.cancelPendingTrigger(order.getRequestId());
                        order.setStatus(OrderStatus.EXPIRED);
                        count++;
                    } else if (marketEngine.expireOrder(order, account)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private int lastEvaluatedPersonalLedgerSize = 0;

    private void evaluateRecordedCareerProgress() {
        Account personal = getPersonalAccount();
        if (personal != null) {
            CareerMission m1 = careerManager.getMission("MIS-01");
            if (m1 != null && !m1.isCompleted()) {
                careerManager.evaluateRecordedTrades(personal, ledger);
            }
            CareerMission m2 = careerManager.getMission("MIS-02");
            if (m2 != null && !m2.isCompleted()) {
                Set<String> sectors = new HashSet<>();
                for (Position position : personal.getPositions().values()) {
                    Instrument instrument = catalog.getById(position.getExchange() + ":" + position.getTicker());
                    if (position.getQuantity() > 0 && instrument != null) sectors.add(instrument.getSector());
                }
                if (sectors.size() >= 2) careerManager.completeMission("MIS-02");
            }
            CareerMission m3 = careerManager.getMission("MIS-03");
            if (m3 != null && !m3.isCompleted()) {
                if (personalNetWorth().getAmount().compareTo(BigDecimal.valueOf(75_000_000)) >= 0)
                    careerManager.completeMission("MIS-03");
            }
        }
        periodicMissionManager.updateCalendar(clock.getCurrentDate(), careerManager.getCurrentRank());
        List<LedgerEntry> entries = ledger.getEntriesForAccount(personalAccountId);
        if (entries.size() > lastEvaluatedPersonalLedgerSize) {
            for (int i = lastEvaluatedPersonalLedgerSize; i < entries.size(); i++) {
                LedgerEntry entry = entries.get(i);
                if (entry.getType() != LedgerEntryType.TRADE_BUY && entry.getType() != LedgerEntryType.TRADE_SELL) continue;
                if (entry.getRequestId().startsWith("SUB-FUND-") || entry.getRequestId().startsWith("RED-FUND-")
                        || (entry.getRequestId().startsWith("FUND-") && (entry.getRequestId().contains(":SUB:") || entry.getRequestId().contains(":RED:")))) continue;
                LocalDate date = java.time.Instant.ofEpochMilli(entry.getTimestamp()).atOffset(java.time.ZoneOffset.ofHours(7)).toLocalDate();
                periodicMissionManager.recordActivity("TRADE:" + entry.getId(), "TRADE", 1, date);
            }
            lastEvaluatedPersonalLedgerSize = entries.size();
        }
        // All execution paths (resting orders, NPC matches and triggers) converge here.
        for (Instrument instrument : catalog.getActiveUniverse()) {
            OrderBook book = marketEngine.getBook(instrument.getId());
            if (book == null) continue;
            for (TradeFill fill : book.getTradeHistory()) {
                boolean sold = personalAccountId.equals(fill.getSellAccountId());
                if (!sold && !personalAccountId.equals(fill.getBuyAccountId())) continue;
                if (playerProfileManager.hasRecordedFill(fill.getFillId())) continue;
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

    private void refreshReplayLiquidity() {
        if (!replayEngine.isReplayActive()) return;
        Instrument instrument = catalog.getById(replayEngine.getActiveDatasetId());
        Account dealer = accounts.get("ACC-NPC-MM-01");
        if (instrument == null || dealer == null) return;
        var bar = replayEngine.getCurrentBar();
        BigDecimal tick = bar == null ? BigDecimal.ZERO : instrument.getExchange().getTickSize(bar.getOpen());
        for (Order order : orderManager.getOrdersForAccount(dealer.getId()))
            if (order.getId().startsWith("REPLAY-MM-") && !order.getStatus().isFinal()
                    && (bar == null || order.getLimitPrice().getAmount().compareTo(bar.getOpen().add(order.isBuy() ? tick.negate() : tick)) != 0
                        || java.time.Instant.ofEpochMilli(order.getCreatedAt()).atOffset(java.time.ZoneOffset.ofHours(7)).toLocalDate().isBefore(clock.getCurrentDate())
                        || !clock.getCurrentTime().isBefore(LocalTime.of(16, 0)))) marketEngine.cancelOrder(order, dealer);
        if (bar == null || !bar.getDate().equals(clock.getCurrentDate()) || clock.getCurrentTime().isBefore(LocalTime.of(9, 0))
                || !clock.getCurrentTime().isBefore(LocalTime.of(16, 0))) return;
        // ponytail: finite opening-reference quotes, not reconstructed intraday liquidity from daily volume.
        for (OrderSide side : OrderSide.values()) {
            if (orderManager.getOrdersForAccount(dealer.getId()).stream().anyMatch(o -> o.getId().startsWith("REPLAY-MM-") && !o.getStatus().isFinal() && o.getSide() == side)) continue;
            String request = "REPLAY-MM-" + clock.getGameTimestamp() + "-" + side;
            if (orderManager.getOrderByRequest(request) != null) continue;
            Position held = dealer.getPosition(instrument.getId());
            long quantity = side == OrderSide.SELL ? (held == null ? 0 : Math.min(1000, held.getQuantity())) : 1000;
            quantity = quantity / instrument.getLotSize() * instrument.getLotSize();
            if (quantity <= 0) continue;
            Money quotePrice = new Money(bar.getOpen().add(side == OrderSide.BUY ? tick.negate() : tick), instrument.getCurrency());
            Order quote = new Order(request, request, dealer.getId(), instrument.getId(), side, OrderType.LIMIT,
                    quotePrice, null, quantity, clock.getGameTimestamp(), Money.zero(instrument.getCurrency()));
            orderManager.registerOrder(quote);
            marketEngine.submitOrder(quote, dealer, instrument, ledger, accounts, clock.getGameTimestamp());
        }
        evaluateRecordedCareerProgress();
    }

    public synchronized void generateNpcLiquidity() {
        if (gameMode == GameMode.HISTORICAL_REPLAY) { refreshReplayLiquidity(); return; }
        marketEngine.tick(clock, clock.getGameTimestamp(), catalog, ledger, accounts, researchService);
        evaluateRecordedCareerProgress();
    }

    public synchronized long getGameTimestamp() {
        return clock != null ? clock.getGameTimestamp() : System.currentTimeMillis();
    }

    public synchronized String getMarketEventsJson() {
        try {
            return researchService.getMarketEventsJson().toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    public synchronized String getNewsFeedJson() {
        try {
            return researchService.getNewsFeed(clock.getGameTimestamp(), Math.max(lastRealTimeAnchor, realTimeClock.millis())).toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    public synchronized List<NewsArticle> getNewsFeed() {
        return gameMode == GameMode.HISTORICAL_REPLAY ? Collections.emptyList() : researchService.getPublishedNews(clock.getGameTimestamp());
    }

    public synchronized List<AnalystReport> getAnalystReports() {
        return gameMode == GameMode.HISTORICAL_REPLAY ? Collections.emptyList() : researchService.getPublishedReports(clock.getGameTimestamp());
    }

    public synchronized List<CalendarEvent> getCalendar() {
        return gameMode != GameMode.HISTORICAL_REPLAY && careerManager.hasRank(CareerRank.SENIOR_ANALYST) ? researchService.getCalendarEvents() : Collections.emptyList();
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

        if (acc.getType() != AccountType.PERSONAL && acc.getType() != AccountType.CLIENT)
            throw new IllegalArgumentException("Fund Builder hanya untuk akun pemain atau klien");
        return fundBuilder.saveAllocationPlan(planId, accountId, name, targetWeights, cashWeight, mandate);
    }

    public synchronized List<MatchingEngine.OrderResult> executeFundRebalance(String planId) {
        careerManager.requireRank(CareerRank.PORTFOLIO_MANAGER, "Fund Builder");
        FundAllocationPlan plan = fundBuilder.getPlan(planId);
        if (plan == null) throw new IllegalArgumentException("Plan tidak ditemukan: " + planId);

        Account acc = accounts.get(plan.getAccountId());
        if (acc == null || (acc.getType() != AccountType.PERSONAL && acc.getType() != AccountType.CLIENT))
            throw new IllegalArgumentException("Akun rencana tidak dapat dioperasikan pemain");
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
        Instrument inst = corporateInstrument(action);
        return corporateService.executeCashDividend(action, inst, accounts.values(), ledger, clock.getGameTimestamp());
    }

    public synchronized void executeStockSplit(CorporateAction action) {
        Instrument inst = corporateInstrument(action);
        if (inst == null) throw new IllegalArgumentException("Instrumen split tidak ditemukan");
        if (hasActiveShort(inst)) throw new IllegalStateException("Split ditunda selama pinjaman saham masih terbuka");
        cancelInstrumentOrders(inst.getId(), null);
        corporateService.executeStockSplit(action, inst, accounts.values(), ledger, clock.getGameTimestamp());
    }

    private Instrument corporateInstrument(CorporateAction action) {
        return requireInstrument(action.getTicker().contains(":") ? action.getTicker()
                : (action.getCurrency() == Currency.USD ? "US:" : "IDX:") + action.getTicker());
    }

    private boolean hasActiveShort(Instrument instrument) {
        for (Account account : accounts.values()) {
            ShortPosition shortPosition = riskService.getShortPosition(account.getId(), instrument.getTicker());
            if (shortPosition != null && shortPosition.getBorrowedShares() > 0 && shortPosition.getExchange().equals(instrument.getExchange().name())) return true;
        }
        return false;
    }

    private void cancelInstrumentOrders(String instrumentId, String accountId) {
        for (Account account : accounts.values()) {
            if (accountId != null && !accountId.equals(account.getId())) continue;
            for (Order order : orderManager.getOrdersForAccount(account.getId()))
                if (instrumentId.equals(order.getInstrumentId()) && !order.getStatus().isFinal()) cancelOrder(order);
        }
    }

    public synchronized JSONObject corporateOfferAction(JSONObject request) throws JSONException {
        if (gameMode == GameMode.HISTORICAL_REPLAY) throw new IllegalStateException("Aksi korporasi sintetis tidak tersedia dalam replay historis");
        Account owner = getPersonalAccount();
        String action = request.getString("action");
        JSONArray offers = corporateService.getOffers(owner, clock.getCurrentDate());
        for (int i = 0; i < offers.length(); i++) offers.getJSONObject(i).put("requests", offers.getJSONObject(i).getJSONArray("participations"));
        if ("list".equals(action)) return new JSONObject().put("success", true).put("offers", offers);
        if ("cancel".equals(action)) {
            JSONObject result = corporateService.cancelParticipation(request.getString("requestId"), owner, clock.getCurrentDate());
            persistFinancialMutation(); return result;
        }
        JSONObject offer = null;
        String id = request.getString("offerId");
        for (int i = 0; i < offers.length(); i++) if (id.equals(offers.getJSONObject(i).getString("id"))) offer = offers.getJSONObject(i);
        if (offer == null) throw new IllegalArgumentException("Penawaran tidak ditemukan");
        long shares = request.getLong("shares");
        if ("participate".equals(action)) {
            JSONArray previous = offer.getJSONArray("participations");
            for (int i = 0; i < previous.length(); i++) if (request.getString("requestId").equals(previous.getJSONObject(i).getString("requestId"))) {
                JSONObject result = corporateService.participateOffer(id, owner, shares, request.getString("requestId"), clock.getCurrentDate(), ledger, clock.getGameTimestamp());
                persistFinancialMutation(); return result;
            }
        }
        if (shares <= 0 || !offer.getBoolean("open") || shares > offer.getLong("availableShares"))
            throw new IllegalArgumentException("Jumlah melebihi hak atau penawaran belum aktif");
        Money nominal = new Money(new BigDecimal(offer.getString("cashPrice")).multiply(BigDecimal.valueOf(shares)), Currency.IDR);
        if ("RIGHTS_ISSUE".equals(offer.getString("kind"))) {
            if (owner.getAvailableCash(Currency.IDR).isLessThan(nominal)) throw new IllegalStateException("Kas tersedia tidak cukup untuk rights");
        } else {
            Position holding = owner.getPosition(offer.getString("instrumentId"));
            if (holding == null || shares > holding.getQuantity() - corporateService.reservedCorporateShares(owner.getId(), offer.getString("instrumentId")))
                throw new IllegalStateException("Saham tersedia tidak cukup untuk tender");
        }
        if ("MERGER_TENDER".equals(offer.getString("kind")) && BigDecimal.valueOf(shares).multiply(new BigDecimal(offer.getString("exchangeRatio"))).stripTrailingZeros().scale() > 0)
            throw new IllegalArgumentException("Jumlah saham tender harus menghasilkan saham tujuan bulat");
        if ("preview".equals(action)) return new JSONObject(offer.toString()).put("success", true).put("offerId", id).put("shares", shares)
                .put("amount", new BigDecimal(offer.getString("cashPrice")).multiply(BigDecimal.valueOf(shares)).toPlainString());
        if (!"participate".equals(action)) throw new IllegalArgumentException("Aksi penawaran tidak dikenal");
        // Existing resting and trigger sells must release their claims before tender reservation.
        if (!"RIGHTS_ISSUE".equals(offer.getString("kind"))) cancelInstrumentOrders(offer.getString("instrumentId"), owner.getId());
        JSONObject result = corporateService.participateOffer(id, owner, shares, request.getString("requestId"), clock.getCurrentDate(), ledger, clock.getGameTimestamp());
        persistFinancialMutation(); return result;
    }

    public synchronized IpoSubscription orderIpo(String ipoId, long lots) {
        return orderIpo(ipoId, lots, "IPO-" + UUID.randomUUID());
    }

    public synchronized IpoSubscription orderIpo(String ipoId, long lots, String requestId) {
        if (gameMode == GameMode.HISTORICAL_REPLAY) throw new IllegalStateException("IPO game tidak tersedia dalam replay historis");
        IpoOffering ipo = corporateService.getIpo(ipoId);
        if (ipo == null) throw new IllegalArgumentException("IPO tidak ditemukan: " + ipoId);
        Account personal = getPersonalAccount();
        IpoSubscription receipt = corporateService.orderIpo(ipo, personal, lots, requestId, clock.getGameTimestamp());
        persistFinancialMutation();
        return receipt;
    }

    public synchronized void allotIpo(String ipoId, double ratio) {
        IpoOffering ipo = corporateService.getIpo(ipoId);
        if (ipo == null) throw new IllegalArgumentException("IPO tidak ditemukan: " + ipoId);
        corporateService.allotIpo(ipo, ratio, accounts, ledger, clock.getGameTimestamp());
        String exchange = ipo.getCurrency() == Currency.USD ? "US" : "IDX";
        if (catalog.getById(exchange + ":" + ipo.getTicker()) == null)
            catalog.registerInstrument(new Instrument(exchange + ":" + ipo.getTicker(), ipo.getTicker(), ipo.getCompanyName(),
                    com.tradingglobal3d.core.market.MarketExchange.valueOf(exchange), "IPO", ipo.getCurrency(), ipo.getLotSize(),
                    ipo.getSharesOffered(), true, ipo.getOfferingPrice()));
    }

    public synchronized void castShareholderVote(String agendaId, ShareholderMeetingAgenda.VoteChoice choice) {
        if (gameMode == GameMode.HISTORICAL_REPLAY) throw new IllegalStateException("Voting game tidak tersedia dalam replay historis");
        ShareholderMeetingAgenda agenda = corporateService.getAgenda(agendaId);
        if (agenda == null) throw new IllegalArgumentException("Agenda RUPS tidak ditemukan: " + agendaId);
        Account personal = getPersonalAccount();
        corporateService.castVote(agenda, personal, choice, clock.getGameTimestamp());
        persistFinancialMutation();
    }

    public synchronized void takeMarginLoan(Money amount) {
        careerManager.requireRank(CareerRank.PORTFOLIO_MANAGER, "Pinjaman margin");
        Account personal = getPersonalAccount();
        riskService.takeMarginLoan(personal, amount, ledger, clock.getGameTimestamp());
        persistFinancialMutation();
    }

    public synchronized void repayMarginLoan(Money amount) {
        Account personal = getPersonalAccount();
        riskService.repayMarginLoan(personal, amount, ledger, clock.getGameTimestamp());
        persistFinancialMutation();
    }

    public synchronized ShortPosition openShortPosition(String ticker, long shares) {
        careerManager.requireRank(CareerRank.PORTFOLIO_MANAGER, "Short selling");
        Account personal = getPersonalAccount();
        Instrument inst = catalog.getById("IDX:" + ticker);
        if (inst == null) inst = catalog.getById("US:" + ticker);
        if (inst == null) throw new IllegalArgumentException("Saham tidak ditemukan: " + ticker);
        Account mm = accounts.get("ACC-NPC-MM-01");
        ShortPosition position = riskService.openShortPosition(personal, inst, shares, mm, ledger, clock.getGameTimestamp());
        persistFinancialMutation();
        return position;
    }

    public synchronized void closeShortPosition(String ticker, long shares) {
        Account personal = getPersonalAccount();
        Instrument inst = catalog.getById("IDX:" + ticker);
        if (inst == null) inst = catalog.getById("US:" + ticker);
        if (inst == null) throw new IllegalArgumentException("Saham tidak ditemukan: " + ticker);
        Account mm = accounts.get("ACC-NPC-MM-01");
        riskService.closeShortPosition(personal, inst, shares, mm, ledger, clock.getGameTimestamp());
        persistFinancialMutation();
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
        return riskService.executeForcedLiquidation(personal, inst, marketEngine.isHalted(inst.getId()), ledger, clock.getGameTimestamp());
    }

    public synchronized StrategyLabSandbox createStrategySandbox(String sandboxId) {
        if (sandboxId == null || !sandboxId.matches("[A-Za-z0-9_-]{1,80}")) throw new IllegalArgumentException("Identitas lab tidak valid");
        if (strategyLabs.containsKey(sandboxId)) return strategyLabs.get(sandboxId);
        if (strategyLabs.size() >= 20) throw new IllegalStateException("Maksimal 20 lab tersimpan");
        Account personal = getPersonalAccount();
        StrategyLabSandbox lab = new StrategyLabSandbox(sandboxId, personal, clock.getGameTimestamp(), fxService.getMidRate());
        strategyLabs.put(sandboxId, lab);
        persistFinancialMutation();
        return lab;
    }

    public synchronized JSONObject strategyLab(JSONObject request) throws JSONException {
        String action = request.getString("action");
        if ("list".equals(action)) {
            JSONArray labs = new JSONArray();
            for (StrategyLabSandbox lab : strategyLabs.values()) labs.put(labView(lab));
            return new JSONObject().put("success", true).put("sandboxes", labs);
        }
        String sid = request.getString("sandboxId");
        StrategyLabSandbox lab = "create".equals(action) ? createStrategySandbox(sid) : strategyLabs.get(sid);
        if (lab == null) throw new IllegalArgumentException("Lab belum dibuat");
        JSONObject result = new JSONObject().put("success", true);
        switch (action) {
            case "create": case "history": break;
            case "reset": lab.resetDecisions(); persistFinancialMutation(); break;
            case "replay": result = lab.replayDecisions(); persistFinancialMutation(); break;
            case "trade":
                result = lab.executeDecision(request.getString("side"), requireInstrument(request.getString("instrumentId")),
                        request.getLong("shares"), new BigDecimal(request.get("price").toString()), clock.getGameTimestamp());
                persistFinancialMutation(); break;
            default: throw new IllegalArgumentException("Aksi lab tidak dikenal");
        }
        return result.put("sandboxId", sid).put("state", labView(lab)).put("decisions", lab.getDecisionHistory());
    }

    private JSONObject labView(StrategyLabSandbox lab) throws JSONException {
        return new JSONObject().put("sandboxId", lab.getSandboxId()).put("accountName", lab.getClonedAccount().getName())
                .put("cashIdr", lab.getClonedAccount().getTotalCash(Currency.IDR).format())
                .put("cashUsd", lab.getClonedAccount().getTotalCash(Currency.USD).format())
                .put("positionsCount", lab.getClonedAccount().getPositions().size()).put("simulatedTradesCount", lab.getSimulatedTradesCount())
                .put("simulatedRealizedPnL", lab.getSimulatedRealizedPnL().toPlainString());
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

        // Batalkan/kedaluwarsa sisa antrean order harian yang belum match
        expireDayOrders();

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
        collectionManager.updateValuations(now);
        collectionManager.processDailyMaintenance(personal, ledger, now);
        multiAssetTrading.settleCoupon(personal, accounts.get("ACC-NPC-MM-01"), ledger, now);

        // Akrual hasil reksa dana harian
        mutualFundManager.advanceDay(0.0, ledger, now);

        // Akrual bunga dan penalti harian pinjaman tenor
        installmentLoanManager.processRealTimeAccrual(realTimeClock.millis());
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

        if (!advanceToMorning) {
            // Pada midnight rollover: reset instrumen IDX (sesi IDX sudah selesai di 16:00)
            // Sesi US sedang berjalan di 00:00 WIB sehingga tidak direset di tengah malam.
            for (Instrument inst : catalog.getAllInstruments()) {
                if (inst.getExchange() == com.tradingglobal3d.core.market.MarketExchange.IDX) {
                    inst.startNewTradingDay();
                }
            }
            return report;
        }
        LocalDate nextDate = clock.getCurrentDate().plusDays(1);
        this.clock = new GameClock(nextDate, java.time.LocalTime.of(8, 30, 0));
        for (Instrument inst : catalog.getAllInstruments()) {
            inst.startNewTradingDay();
        }
        processCalendarActions(nextDate);
        return report;
    }

    private void processCalendarActions(LocalDate nextDate) {
        if (gameMode == GameMode.INTERNAL_MARKET) {
            try {
                corporateService.ensureMonthlyIpoSchedule(nextDate);
                corporateService.processDate(nextDate, accounts, ledger, clock.getGameTimestamp());
                timeDepositManager.processDailyMaturities(getPersonalAccount(), clock.getGameTimestamp(), ledger);
            } catch (JSONException error) { throw new IllegalStateException("Penyelesaian aksi korporasi gagal", error); }
        }
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
        if (gameMode == GameMode.HISTORICAL_REPLAY) return;
        for (com.tradingglobal3d.core.corporate.CorporateAction ca : corporateService.getCorporateActions()) {
            if (!ca.isExecuted() && !ca.isCancelled() && !ca.getExecutionDate().isAfter(nextDate)) {
                String approval = corporateService.getApprovalStatus(ca);
                if ("PENDING".equals(approval)) continue;
                if ("REJECTED".equals(approval)) { ca.cancel(); continue; }
                if (ca.getType() == com.tradingglobal3d.core.corporate.CorporateActionType.CASH_DIVIDEND) {
                    executeCashDividend(ca);
                } else if (ca.getType() == com.tradingglobal3d.core.corporate.CorporateActionType.STOCK_SPLIT || ca.getType() == com.tradingglobal3d.core.corporate.CorporateActionType.REVERSE_SPLIT) {
                    Instrument splitInstrument = catalog.getById(ca.getTicker().contains(":") ? ca.getTicker() : (ca.getCurrency() == Currency.USD ? "US:" : "IDX:") + ca.getTicker());
                    if (splitInstrument != null && hasActiveShort(splitInstrument)) continue;
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
            if (gameMode == GameMode.HISTORICAL_REPLAY && (!replayEngine.supportsInstrument(inst.getId())
                    || replayEngine.getCurrentBar() == null || !replayEngine.getCurrentBar().getDate().equals(clock.getCurrentDate())
                    || clock.getCurrentTime().isBefore(LocalTime.of(9, 0)) || !clock.getCurrentTime().isBefore(LocalTime.of(16, 0)))) continue;
            orderManager.checkAndActivateTriggers(inst, marketEngine, ledger, accounts, now);
        }
        evaluateRecordedCareerProgress();
    }

    public synchronized boolean tick(int realSeconds) {
        if (realSeconds < 0) throw new IllegalArgumentException("Waktu tidak boleh negatif");
        if (clock == null || !clock.isForeground() || clock.isPaused() || realSeconds == 0) return false;
        try {
            long gameSeconds = clock.advanceRealMs(realSeconds * 1000L, true);
            this.lastRealTimeAnchor = Math.max(lastRealTimeAnchor, realTimeClock.millis());
            if (gameSeconds <= 0) return false;
            return advanceWorldSeconds(gameSeconds);
        } catch (RuntimeException error) {
            clock.pause();
            throw error;
        }
    }

    public synchronized boolean syncClock() {
        long now = realTimeClock.millis();
        if (clock == null || now <= lastRealTimeAnchor) return false;
        return advanceRealMs(now - lastRealTimeAnchor, clock.isForeground());
    }

    public synchronized boolean advanceRealMs(long realMs, boolean isForeground) {
        if (realMs < 0) throw new IllegalArgumentException("Waktu tidak boleh negatif");
        if (clock == null || realMs == 0) return false;
        if (isForeground && clock.isPaused()) return false;
        try {
            long gameSeconds = clock.advanceRealMs(realMs, isForeground);
            this.lastRealTimeAnchor = Math.max(lastRealTimeAnchor, realTimeClock.millis());
            if (gameSeconds <= 0) return false;
            return advanceWorldSeconds(gameSeconds);
        } catch (RuntimeException error) {
            clock.pause();
            throw error;
        }
    }

    private boolean advanceWorldSeconds(long remaining) {
        // Lifecycle cannot change event boundaries or trigger outcomes.
        LocalDate initialDate = clock.getCurrentDate();
        long realEnd = Math.max(lastRealTimeAnchor, realTimeClock.millis());

        while (remaining > 0) {
            if (gameMode == GameMode.HISTORICAL_REPLAY && replayEngine.isReplayCompleted(clock.getCurrentDate(), clock.getCurrentTime())) {
                clock.pause(); break;
            }
            long midnight = 86400 - clock.getCurrentTime().toSecondOfDay();
            long step;
            int eventInterval = 60;
            long secondsToNextEvent = eventInterval - clock.getCurrentTime().toSecondOfDay() % eventInterval;
            step = Math.min(remaining, Math.min(secondsToNextEvent, midnight));

            if (step == midnight) {
                clock.advanceGameSeconds(step - 1);
                settleDay(false);
                clock.advanceGameSeconds(1);
                processCalendarActions(clock.getCurrentDate());
                if (gameMode == GameMode.HISTORICAL_REPLAY && replayEngine.isReplayActive()) {
                    replayEngine.synchronizeToDate(clock.getCurrentDate());
                }
            } else {
                clock.advanceGameSeconds(step);
            }
            remaining -= step;
            long eventReal = realEnd - Math.floorDiv(Math.addExact(Math.multiplyExact(remaining, 1000L), clock.getGameMsRemainder()), 24L);
            researchService.observeWorldTime(clock.getGameTimestamp(), Math.max(0, eventReal), gameMode == GameMode.HISTORICAL_REPLAY);
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
                if (gameMode == GameMode.HISTORICAL_REPLAY) {
                    refreshReplayLiquidity();
                    Instrument replayInstrument = catalog.getById(replayEngine.getActiveDatasetId());
                    BigDecimal observable = replayEngine.getObservablePrice(clock.getCurrentDate(), clock.getCurrentTime());
                    if (replayInstrument != null && observable != null) replayInstrument.updatePrice(new Money(observable, replayInstrument.getCurrency()), 0);
                } else if (clock.isAnyMarketOpen()) {
                    if (clock.getCurrentTime().getMinute() % 15 == 0) generateNpcLiquidity();
                    if (clock.getCurrentTime().getMinute() % 30 == 0 && clock.isTradingOpenFor(com.tradingglobal3d.core.market.MarketExchange.IDX))
                        for (Order npcOrder : leagueManager.getNpcOrders(clock.getGameTimestamp())) submitOrder(npcOrder);
                }
                evaluateTriggers();
            }
            if (clock.getCurrentTime().getMinute() % 15 == 0 || remaining == 0) {
                multiAssetTrading.updateValuations(catalog, fxService, accounts, clock.getGameTimestamp());
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
        JSONObject receipt = new JSONObject().put("success", false).put("requestId", requestId)
                .put("from", clock.getCurrentDate()+"T"+clock.getCurrentTime()).put("status", "PROCESSING");
        skipDayReceipts.put(requestId, receipt);
        try {
            long before = clock.getGameTimestamp();
            advanceWorldSeconds(86400);
            this.lastRealTimeAnchor = realTimeClock.millis();
            long elapsed = (clock.getGameTimestamp() - before) / 1000;
            receipt.put("success", elapsed == 86400).put("status", elapsed == 86400 ? "COMPLETED" : "DATASET_COMPLETE")
                    .put("to", clock.getCurrentDate()+"T"+clock.getCurrentTime()).put("gameSeconds", elapsed);
            if (elapsed != 86400) receipt.put("message", "Replay berakhir pada batas dataset; tidak ada waktu atau harga buatan setelahnya");
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
        runtime.put("worldClockVersion", 1);
        runtime.put("catalog", catalog.exportState());
        runtime.put("risk", riskService.exportState());
        runtime.put("orders", savedOrders.exportState());
        runtime.put("books", marketEngine.exportState());
        runtime.put("career", careerManager.exportState());
        runtime.put("mandates", firmManager.exportState());
        runtime.put("subscription", researchService.exportState());
        runtime.put("fx", fxService.exportState());
        runtime.put("fundPlans", fundBuilder.exportState());
        runtime.put("multiAssetTrading", multiAssetTrading.exportState());
        runtime.put("corporate", corporateService.exportState());
        JSONArray labs = new JSONArray();
        for (StrategyLabSandbox lab : strategyLabs.values()) labs.put(lab.exportState());
        runtime.put("strategyLabs", labs);
        runtime.put("companies", companyManager.exportState());
        runtime.put("holdingCompanies", companyManager.exportHoldingState());
        runtime.put("collections", collectionManager.exportState());
        runtime.put("playerProgress", progressManager.exportState());
        runtime.put("replayEngine", replayEngine.exportState());
        runtime.put("mutualFunds", mutualFundManager.exportState());
        runtime.put("installmentLoans", installmentLoanManager.exportState());
        runtime.put("periodicMissions", periodicMissionManager.exportState());
        runtime.put("league", leagueManager.exportState());
        runtime.put("playerProfile", playerProfileManager.exportState());
        runtime.put("strategicTimeDeposits", timeDepositManager.exportState());
        runtime.put("strategicPortfolioInsurance", portfolioInsuranceService.exportState());
        runtime.put("strategicResearchTechTree", researchTechTreeService.exportState());
        runtime.put("strategicHeadhunting", headhuntingService.exportState());
        runtime.put("strategicDailyStreak", dailyStreakService.exportState());
        runtime.put("strategicSectorDominance", sectorDominanceService.exportState(getPersonalAccount(), catalog, fxService.getMidRate()));
        JSONArray customFundsState = new JSONArray();
        for (JSONObject fund : customMutualFunds) customFundsState.put(new JSONObject(fund.toString()));
        runtime.put("strategicCustomMutualFunds", customFundsState);
        JSONObject closeReceipts = new JSONObject();
        for (Map.Entry<String, JSONObject> receipt : dayCloseReceipts.entrySet()) closeReceipts.put(receipt.getKey(), receipt.getValue());
        runtime.put("dayCloseReceipts", closeReceipts);
        runtime.put("skipDayReceipts", new JSONObject(skipDayReceipts));
        if (dailySponsorLastClaimedRealDate != null) runtime.put("dailySponsorLastClaimedRealDate", dailySponsorLastClaimedRealDate);
        runtime.put("dailySponsorLastClaimedRealTimestamp", dailySponsorLastClaimedRealTimestamp);
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
        Map<String, StrategyLabSandbox> restoredLabs = new LinkedHashMap<>();
        InstrumentCatalog restoredCatalog;
        try {
            new GameClock(state.getGameDate(), state.getGameTime()).setSpeedMultiplier(state.getSpeedMultiplier());
            JSONObject runtime = state.getRuntimeState();
            JSONArray labs = runtime.optJSONArray("strategyLabs");
            if (labs != null) {
                if (labs.length() > 20) throw new IllegalArgumentException("Terlalu banyak lab pada save");
                for (int i = 0; i < labs.length(); i++) {
                    StrategyLabSandbox lab = StrategyLabSandbox.fromState(labs.getJSONObject(i));
                    if (restoredLabs.put(lab.getSandboxId(), lab) != null) throw new IllegalArgumentException("Identitas lab duplikat");
                }
            }
            int savedCatalogVersion = runtime.has("catalog") ? runtime.getJSONObject("catalog").optInt("catalogVersion", 1) : InstrumentCatalog.STATE_VERSION;
            restoredCatalog = catalogForSave(runtime);
            if (savedCatalogVersion == 1 && runtime.has("catalog")
                    && runtime.getJSONObject("catalog").optInt("catalogVersion", 1) == InstrumentCatalog.STATE_VERSION) {
                Account dealer = null;
                for (Account account : state.getAccounts()) if ("ACC-NPC-MM-01".equals(account.getId())) dealer = account;
                if (dealer == null) throw new IllegalArgumentException("Akun likuiditas migrasi tidak tersedia");
                Set<String> baselineIds = new HashSet<>();
                JSONArray baseline = initialCatalogState.getJSONArray("instruments");
                for (int i = 0; i < baseline.length(); i++) baselineIds.add(baseline.getJSONObject(i).getString("id"));
                // Seed only newly tradable inventory once; normal reload must never replenish sold positions.
                for (Instrument inst : restoredCatalog.getActiveUniverse()) {
                    if (!baselineIds.contains(inst.getId())) continue;
                    if (LEGACY_CATALOG_IDS.contains(inst.getId()) && !LEGACY_PASSIVE_IDS.contains(inst.getId())) continue;
                    if (dealer.getPosition(inst.getId()) == null && dealer.getPosition(inst.getTicker()) == null)
                        dealer.addPosition(new Position(inst.getTicker(), inst.getExchange().name(), inst.getCurrency(),
                                inst.getLotSize() * 200L, inst.getCurrentPrice().getAmount(), inst.getCurrentPrice().getAmount()));
                }
            }
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
            if (runtime.has("catalog")) restoredCatalog.validateState(runtime.getJSONObject("catalog"));
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
            stagedOrders.restoreState(runtime.optJSONArray("orders") != null ? runtime.getJSONArray("orders") : new JSONArray(), restoredCatalog, stagedMarket);
            if (runtime.has("books")) stagedMarket.restoreState(runtime.getJSONArray("books"), restoredCatalog, stagedOrders);
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
                if (runtime.has("holdingCompanies")) stagedCompanies.restoreHoldingState(runtime.getJSONArray("holdingCompanies"));
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
                for (Account account : state.getAccounts()) savedAccounts.put(account.getId(), account.copy());
                stagedMutualFunds.validateAccounts(savedAccounts);
                EconomyLedger stagedLedger = new EconomyLedger();
                stagedMutualFunds.registerPoolAccounts(savedAccounts, stagedLedger, new GameClock(state.getGameDate(), state.getGameTime()).getGameTimestamp());
                stagedMutualFunds.bindAccounts(savedAccounts, restoredCatalog);
            }
            if (runtime.has("installmentLoans")) {
                InstallmentLoanManager stagedLoans = new InstallmentLoanManager();
                stagedLoans.restoreState(runtime.getJSONArray("installmentLoans"), new GameClock(state.getGameDate(), state.getGameTime()).getGameTimestamp());
                var stagedCollateral = new com.tradingglobal3d.core.collection.CollectionManager();
                if (runtime.has("collections")) stagedCollateral.restoreState(runtime.getJSONArray("collections"));
                stagedLoans.validateCollateral(stagedCollateral);
                for (LoanContract loan : stagedLoans.getAllLoans()) {
                    Account borrower = null;
                    for (Account account : state.getAccounts()) if (account.getId().equals(loan.getAccountId())) borrower = account;
                    if (borrower == null || (borrower.getType() != AccountType.PERSONAL && borrower.getType() != AccountType.COMPANY))
                        throw new IllegalArgumentException("Akun peminjam save tidak valid");
                    if ((("LOAN-PERS-01".equals(loan.getProductId()) || "LOAN-PERS-02".equals(loan.getProductId())) && borrower.getType() != AccountType.PERSONAL)
                            || ("LOAN-CORP-01".equals(loan.getProductId()) && borrower.getType() != AccountType.COMPANY))
                        throw new IllegalArgumentException("Produk pinjaman tidak cocok dengan peminjam");
                }
            }
            if (runtime.has("periodicMissions")) new PeriodicMissionManager().restoreState(runtime.getJSONObject("periodicMissions"));
            if (runtime.has("league")) new LeagueManager().restoreState(runtime.getJSONObject("league"));
            if (runtime.has("playerProfile")) new PlayerProfileManager().restoreState(runtime.getJSONObject("playerProfile"));
            if (runtime.has("strategicTimeDeposits")) new com.tradingglobal3d.core.multiasset.TimeDepositManager()
                    .restoreState(runtime.getJSONObject("strategicTimeDeposits"));
            if (runtime.has("strategicPortfolioInsurance")) new com.tradingglobal3d.core.risk.PortfolioInsuranceService()
                    .restoreState(runtime.getJSONObject("strategicPortfolioInsurance"));
            if (runtime.has("strategicResearchTechTree")) new com.tradingglobal3d.core.research.ResearchTechTreeService()
                    .restoreState(runtime.getJSONArray("strategicResearchTechTree"));
            if (runtime.has("strategicHeadhunting")) new com.tradingglobal3d.core.firm.HeadhuntingService()
                    .restoreState(runtime.getJSONArray("strategicHeadhunting"));
            if (runtime.has("strategicDailyStreak")) new com.tradingglobal3d.core.player.DailyStreakService()
                    .restoreState(runtime.getJSONObject("strategicDailyStreak"));
            if (runtime.has("strategicSectorDominance")) new com.tradingglobal3d.core.market.SectorDominanceService()
                    .validateState(runtime.getJSONArray("strategicSectorDominance"));
            new MultiAssetTradingService(new FundBuilderService()).restore(runtime.optJSONObject("multiAssetTrading") != null ? runtime.getJSONObject("multiAssetTrading") : new JSONObject());
            if (runtime.has("weeklyPerformance")) com.tradingglobal3d.core.career.PerformancePeriod.restore(runtime.getJSONObject("weeklyPerformance"));
            if (runtime.has("runPerformance")) com.tradingglobal3d.core.career.PerformancePeriod.restore(runtime.getJSONObject("runPerformance"));
        } catch (Exception invalidState) {
            return false;
        }
        this.runId = state.getRuntimeState().optString("runId", "LEGACY-" + java.util.UUID.nameUUIDFromBytes((state.getGameDate() + ":" + state.getLedgerEntries().size()).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        this.catalog = restoredCatalog;
        corporateService.bindShareSupplyCatalog(restoredCatalog);
        this.strategyLabs.clear(); this.strategyLabs.putAll(restoredLabs);
        String savedBaseline = state.getRuntimeState().optString("weeklyBaselineNetWorth", null);
        this.weeklyBaselineNetWorth = savedBaseline != null ? new Money(new BigDecimal(savedBaseline), Currency.IDR) : getPersonalNetWorth();
        this.gameMode = state.getGameMode();
        this.clock = new GameClock(state.getGameDate(), state.getGameTime());
        this.clock.setSpeedMultiplier(1);
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
            // Legacy articles have no historical real deadline. Start one transition once, then persist it.
            long newsAnchor = runtime.has("subscription") && runtime.getJSONObject("subscription").has("newsEffects")
                    ? lastRealTimeAnchor : Math.max(lastRealTimeAnchor, realTimeClock.millis());
            researchService.observeWorldTime(clock.getGameTimestamp(), newsAnchor, gameMode == GameMode.HISTORICAL_REPLAY);
            fxService.restoreState(runtime.has("fx") ? runtime.getJSONObject("fx") : new FxExchangeService().exportState());
            fundBuilder.restoreState(runtime.has("fundPlans") ? runtime.getJSONArray("fundPlans") : new JSONArray());
            multiAssetTrading.restore(runtime.optJSONObject("multiAssetTrading") != null ? runtime.getJSONObject("multiAssetTrading") : new JSONObject());
            if (runtime.has("corporate")) corporateService.restoreState(runtime.getJSONObject("corporate"));
            companyManager.reset(); collectionManager.reset(); mutualFundManager.reset();
            installmentLoanManager.reset(); periodicMissionManager.reset(); leagueManager.reset();
            playerProfileManager.reset(); replayEngine.reset();
            if (runtime.has("companies")) companyManager.restoreState(runtime.getJSONArray("companies"));
            if (runtime.has("holdingCompanies")) companyManager.restoreHoldingState(runtime.getJSONArray("holdingCompanies"));
            if (runtime.has("collections")) collectionManager.restoreState(runtime.getJSONArray("collections"));
            if (runtime.has("playerProgress")) progressManager.restoreState(runtime.optJSONObject("playerProgress"));
            if (runtime.has("replayEngine")) replayEngine.restoreState(runtime.optJSONObject("replayEngine"));
            if (runtime.has("mutualFunds")) mutualFundManager.restoreState(runtime.getJSONArray("mutualFunds"));
            if (runtime.has("installmentLoans")) installmentLoanManager.restoreState(runtime.getJSONArray("installmentLoans"), clock.getGameTimestamp());
            if (runtime.has("periodicMissions")) periodicMissionManager.restoreState(runtime.getJSONObject("periodicMissions"));
            if (runtime.has("league")) leagueManager.restoreState(runtime.getJSONObject("league"));
            if (runtime.has("playerProfile")) playerProfileManager.restoreState(runtime.getJSONObject("playerProfile"));
            if (runtime.has("strategicTimeDeposits")) timeDepositManager.restoreState(runtime.getJSONObject("strategicTimeDeposits"));
            if (runtime.has("strategicPortfolioInsurance")) portfolioInsuranceService.restoreState(runtime.getJSONObject("strategicPortfolioInsurance"));
            if (runtime.has("strategicResearchTechTree")) researchTechTreeService.restoreState(runtime.getJSONArray("strategicResearchTechTree"));
            if (runtime.has("strategicHeadhunting")) headhuntingService.restoreState(runtime.getJSONArray("strategicHeadhunting"));
            if (runtime.has("strategicDailyStreak")) dailyStreakService.restoreState(runtime.getJSONObject("strategicDailyStreak"));
            if (runtime.has("strategicCustomMutualFunds")) {
                customMutualFunds.clear();
                JSONArray savedCustomFunds = runtime.getJSONArray("strategicCustomMutualFunds");
                for (int i = 0; i < savedCustomFunds.length(); i++) customMutualFunds.add(new JSONObject(savedCustomFunds.getJSONObject(i).toString()));
            }
            if (runtime.has("dailySponsorLastClaimedRealDate")) {
                dailySponsorLastClaimedRealDate = runtime.getString("dailySponsorLastClaimedRealDate");
            }
            dailySponsorLastClaimedRealTimestamp = runtime.optLong("dailySponsorLastClaimedRealTimestamp", 0L);
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
            Account retailAccount = accounts.get("ACC-NPC-RETAIL-01");
            if (retailAccount == null) {
                retailAccount = new Account("ACC-NPC-RETAIL-01", "Investor Ritel Mandiri", AccountType.FIRM, null);
                accounts.put("ACC-NPC-RETAIL-01", retailAccount);
                Money retailCashIdr = Money.of(2_000_000_000L, Currency.IDR);
                Money retailCashUsd = Money.of(200_000.0, Currency.USD);
                ledger.recordMutation("INIT-RETAIL-IDR", retailAccount, LedgerEntryType.INITIAL_CAPITAL, retailCashIdr, true, clock.getGameTimestamp(), "Modal kas IDR Investor Ritel");
                ledger.recordMutation("INIT-RETAIL-USD", retailAccount, LedgerEntryType.INITIAL_CAPITAL, retailCashUsd, true, clock.getGameTimestamp(), "Modal kas USD Investor Ritel");
                for (Instrument inst : catalog.getActiveUniverse()) {
                    long initialShares = inst.getLotSize() * 100L;
                    Position p = new Position(inst.getTicker(), inst.getExchange().name(), inst.getCurrency(),
                            initialShares, inst.getCurrentPrice().getAmount(), inst.getCurrentPrice().getAmount());
                    retailAccount.addPosition(p);
                }
            }
            marketEngine.registerNpcAgent(new NpcAgent("NPC-RETAIL-01", "Investor Ritel Mandiri", NpcStrategyType.RETAIL_NOISE, retailAccount));

            // Bind collection manager ke loan manager setelah keduanya di-restore dan lepaskan agunan migrasi (A16)
            installmentLoanManager.bindCollectionManager(collectionManager);
            installmentLoanManager.releaseAllCollateralsPreservingDebt();

            // Bind fund pool accounts setelah restore akun dan katalog
            mutualFundManager.registerPoolAccounts(accounts, ledger, clock.getGameTimestamp());
            mutualFundManager.bindAccounts(accounts, catalog);
            leagueManager.bindMarket(accounts, ledger, catalog, clock.getGameTimestamp());
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

    /** Restore only catalog additions proven by persisted IPO allotment; never accept unknown save symbols. */
    private InstrumentCatalog catalogForSave(JSONObject runtime) throws JSONException {
        InstrumentCatalog result = new InstrumentCatalog();
        JSONObject catalogState = runtime.optJSONObject("catalog");
        Object versionValue = catalogState == null ? InstrumentCatalog.STATE_VERSION
                : catalogState.has("catalogVersion") ? catalogState.get("catalogVersion") : 1;
        if (!(versionValue instanceof Integer) || (Integer) versionValue < 1 || (Integer) versionValue > InstrumentCatalog.STATE_VERSION)
            throw new IllegalArgumentException("Versi katalog save tidak didukung");
        int version = (Integer) versionValue;
        CorporateActionService savedCorporate = new CorporateActionService();
        if (runtime.has("corporate")) savedCorporate.restoreState(runtime.getJSONObject("corporate"));
        Map<String, IpoOffering> listed = new HashMap<>();
        for (IpoOffering ipo : savedCorporate.getIpoOfferings()) if (ipo.isAllotted())
            listed.put((ipo.getCurrency() == Currency.USD ? "US:" : "IDX:") + ipo.getTicker(), ipo);
        JSONArray saved = runtime.has("catalog") ? runtime.getJSONObject("catalog").getJSONArray("instruments") : catalog.exportState().getJSONArray("instruments");
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < saved.length(); i++) {
            String id = saved.getJSONObject(i).getString("id");
            if (!ids.add(id)) throw new IllegalArgumentException("ID katalog save duplikat");
            Instrument source = catalog.getById(id);
            IpoOffering ipo = listed.get(id);
            if (source == null && ipo == null) throw new IllegalArgumentException("Instrumen save tidak dikenal: " + id);
            result.registerInstrument(source != null ? new Instrument(source.getId(), source.getTicker(), source.getName(), source.getExchange(),
                    source.getSector(), source.getCurrency(), source.getLotSize(), source.getCatalogIssuedShares(), source.isActiveTrading(), source.getCurrentPrice())
                    : new Instrument(id, ipo.getTicker(), ipo.getCompanyName(), com.tradingglobal3d.core.market.MarketExchange.valueOf(ipo.getCurrency() == Currency.USD ? "US" : "IDX"),
                    "IPO", ipo.getCurrency(), ipo.getLotSize(), ipo.getSharesOffered(), true, ipo.getOfferingPrice()));
        }
        if (initialCatalogState != null) {
            JSONArray required = initialCatalogState.getJSONArray("instruments");
            Set<String> requiredIds = new HashSet<>();
            for (int i = 0; i < required.length(); i++) requiredIds.add(required.getJSONObject(i).getString("id"));
            Set<String> savedBaseIds = new HashSet<>(ids);
            savedBaseIds.retainAll(requiredIds);
            boolean upgrade = version == 1 && catalogState != null && requiredIds.size() >= 100
                    && savedBaseIds.equals(LEGACY_CATALOG_IDS) && requiredIds.containsAll(LEGACY_CATALOG_IDS);
            if (upgrade) {
                for (int i = 0; i < required.length(); i++) {
                    JSONObject initial = required.getJSONObject(i);
                    String id = initial.getString("id");
                    if (ids.add(id)) {
                        Instrument source = catalog.getById(id);
                        result.registerInstrument(new Instrument(source.getId(), source.getTicker(), source.getName(), source.getExchange(),
                                source.getSector(), source.getCurrency(), source.getLotSize(), source.getCatalogIssuedShares(),
                                source.isActiveTrading(), new Money(new BigDecimal(initial.getString("price")), source.getCurrency())));
                        saved.put(new JSONObject(initial.toString()));
                    }
                }
                // Only the detached SaveState changes here; disk stays intact until normal atomic save + .bak rotation.
                catalogState.put("catalogVersion", InstrumentCatalog.STATE_VERSION);
            }
            for (int i = 0; i < required.length(); i++) if (!ids.contains(required.getJSONObject(i).getString("id")))
                throw new IllegalArgumentException("Instrumen dasar save hilang");
        }
        for (MarketIndex index : catalog.getAllIndices()) result.registerIndex(new MarketIndex(index.getId(), index.getTicker(), index.getName(),
                index.getExchange(), index.getBaseValue(), index.getMethod(), index.getMemberIds()));
        if (catalogState != null) {
            result.restoreState(catalogState);
            savedCorporate.bindShareSupplyCatalog(result);
            savedCorporate.reconcileShareSupply();
            runtime.put("catalog", result.exportState());
        }
        return result;
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
        if (gameMode == GameMode.HISTORICAL_REPLAY && clock != null) root.put("replay", new JSONObject()
                .put("datasetId", replayEngine.getActiveDatasetId()).put("barIndex", replayEngine.getCurrentBarIndex())
                .put("totalBars", replayEngine.getTotalBars()).put("completed", replayEngine.isReplayCompleted(clock.getCurrentDate(), clock.getCurrentTime()))
                .put("executionModel", "Daily opening-reference quotes with one tick spread; finite game dealer inventory, not historical order book"));

        if (clock != null) {
            JSONObject clockObj = new JSONObject();
            clockObj.put("date", clock.getFormattedDate());
            clockObj.put("time", clock.getFormattedTime());
            clockObj.put("timestamp", clock.getGameTimestamp());
            clockObj.put("monotonicAnchor", System.currentTimeMillis());
            clockObj.put("rate", GameClock.FOREGROUND_GAME_SECONDS_PER_REAL_SECOND);
            clockObj.put("isAutomatic", true);
            clockObj.put("session", clock.getCurrentMarketSession().name());
            clockObj.put("sessionLabel", clock.getCurrentMarketSession().getLabel());
            clockObj.put("isTradingOpen", clock.getCurrentMarketSession().isOpenForTrading());
            clockObj.put("isAnyTradingOpen", clock.isAnyMarketOpen());
            clockObj.put("isIdxTradingOpen", clock.isTradingOpenFor(com.tradingglobal3d.core.market.MarketExchange.IDX));
            clockObj.put("isUsTradingOpen", clock.isTradingOpenFor(com.tradingglobal3d.core.market.MarketExchange.US));
            clockObj.put("speed", clock.getSpeedMultiplier());
            clockObj.put("isPaused", clock.isPaused());
            root.put("clock", clockObj);
        }
        root.put("dailySponsor", getDailySponsorStatus());

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
        if (saveManager != null) {
            root.put("saveMetadata", saveManager.getSaveMetadata());
            java.io.File pFile = saveManager.getSaveFile();
            if (pFile != null && pFile.exists()) {
                root.put("savedTimestamp", pFile.lastModified());
            }
        }
        if (lastOfflineCatchupReport != null) {
            root.put("offlineCatchupReport", lastOfflineCatchupReport);
        }
        return root.toString();
    }

    public synchronized JSONObject getSaveMetadata() throws JSONException {
        return saveManager != null ? saveManager.getSaveMetadata() : new JSONObject().put("format", "JSON Standar (Skema V2, UTF-8)").put("primaryExists", false).put("backupFileExists", false);
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

    public synchronized JSONObject getIssuerOwnership(String instrumentId) throws JSONException {
        Instrument instrument = requireInstrument(instrumentId);
        long outstanding = instrument.getSharesOutstanding(), mapped = 0, shortShares = 0, treasuryMapped = 0;
        String denominatorStatus = outstanding <= 0 ? "INVALID" : "DYNAMIC";
        boolean validDenominator = outstanding > 0;
        JSONArray holders = new JSONArray();
        List<Account> ordered = new ArrayList<>(accounts.values());
        ordered.sort(Comparator.comparing(Account::getId));
        for (Account account : ordered) {
            ShortPosition shortPosition = riskService.getShortPosition(account.getId(), instrument.getTicker());
            if (shortPosition != null && instrument.getExchange().name().equals(shortPosition.getExchange()))
                shortShares = Math.addExact(shortShares, shortPosition.getBorrowedShares());
            Position position = account.getPosition(instrument.getId());
            if (position == null) continue;
            long quantity = position.getQuantity();
            if (quantity == 0) continue;
            boolean treasury = corporateService.isTreasuryHolder(account.getId(), instrumentId);
            if (treasury) treasuryMapped = Math.addExact(treasuryMapped, quantity);
            else mapped = Math.addExact(mapped, quantity);
            Object percent = validDenominator && !treasury ? BigDecimal.valueOf(quantity).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(outstanding), 8, java.math.RoundingMode.HALF_UP) : JSONObject.NULL;
            holders.put(new JSONObject().put("holderId", account.getId()).put("name", account.getName())
                    .put("accountType", account.getType().name()).put("quantity", quantity).put("percent", percent).put("isTreasury", treasury)
                    .put("isPersonalPlayer", account.getId().equals(personalAccountId))
                    .put("isClientAccount", account.getType() == AccountType.CLIENT));
        }
        long knownClaims = Math.addExact(mapped, shortShares);
        boolean reconciled = validDenominator && knownClaims <= outstanding && treasuryMapped == instrument.getTreasuryShares();
        boolean complete = reconciled && knownClaims == outstanding && shortShares == 0;
        return new JSONObject().put("success", true).put("instrumentId", instrument.getId())
                .put("asOf", clock.getGameTimestamp()).put("sharesOutstanding", outstanding)
                .put("issuedShares", instrument.getIssuedShares()).put("treasuryShares", instrument.getTreasuryShares())
                .put("recordedTreasuryShares", treasuryMapped).put("supplyReconciled", reconciled)
                .put("unknownLegacyShares", validDenominator ? Math.max(0, outstanding - knownClaims) : JSONObject.NULL)
                .put("denominatorStatus", denominatorStatus).put("registryComplete", complete)
                .put("mappedShares", mapped).put("unmappedShares", validDenominator ? Math.max(0, outstanding - mapped) : JSONObject.NULL)
                .put("recordedShortShares", shortShares).put("holders", holders)
                .put("coverage", !validDenominator ? "UNKNOWN" : !reconciled ? "INCONSISTENT" : complete ? "COMPLETE" : "PARTIAL")
                .put("holderBasis", "RECORDED_ACCOUNT_POSITIONS")
                .put("coverageNote", "Daftar akun pemegang yang tercatat. Registri seluruh saham beredar belum lengkap; saham yang belum terpetakan tidak diatribusikan ke investor rekaan. Posisi short tidak dihitung sebagai kepemilikan positif.");
    }

    public synchronized JSONObject getChartData(String instrumentId, String timeframe) throws JSONException {
        Instrument inst = catalog.getById(instrumentId);
        if (inst == null) {
            return new JSONObject().put("success", false).put("message", "Instrumen tidak ditemukan: " + instrumentId);
        }

        if (timeframe != null && Arrays.asList("R6H", "R1D", "R7D", "R30D").contains(timeframe))
            return getChartRange(inst, timeframe);

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
            if (replayEngine.getCurrentBar() != null
                    && !replayEngine.getCurrentBar().getDate().isAfter(clock.getCurrentDate())
                    && !barsArr.toString().contains(replayEngine.getCurrentBar().getDate().toString())) {
                BigDecimal openPrice = replayEngine.getObservablePrice(clock.getCurrentDate(), clock.getCurrentTime());
                if (openPrice == null) openPrice = replayEngine.getCurrentBar().getOpen();
                JSONObject bObj = new JSONObject();
                bObj.put("time", replayEngine.getCurrentBar().getDate().toString());
                bObj.put("open", openPrice.doubleValue());
                bObj.put("high", openPrice.doubleValue());
                bObj.put("low", openPrice.doubleValue());
                bObj.put("close", openPrice.doubleValue());
                bObj.put("volume", 0);
                bObj.put("isReference", true);
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
        String sourceLabel = "Matched trades domain engine internal";

        if ("D1".equals(tf)) {
            if ("US:AAPL".equalsIgnoreCase(inst.getId()) || "AAPL".equalsIgnoreCase(inst.getTicker())) {
                try {
                    List<HistoricalBar> histBars = HistoricalDatasetLoader.loadDataset("US:AAPL");
                    if (histBars != null && !histBars.isEmpty()) {
                        int count = Math.min(histBars.size(), 30);
                        List<HistoricalBar> recent = histBars.subList(histBars.size() - count, histBars.size());
                        for (HistoricalBar hb : recent) {
                            JSONObject bObj = new JSONObject();
                            bObj.put("time", hb.getDate().toString());
                            bObj.put("open", hb.getOpen().doubleValue());
                            bObj.put("high", hb.getHigh().doubleValue());
                            bObj.put("low", hb.getLow().doubleValue());
                            bObj.put("close", hb.getClose().doubleValue());
                            bObj.put("volume", hb.getVolume());
                            bObj.put("isReference", false);
                            bars.put(bObj);
                        }
                        sourceLabel = "Data historis harian bursa resmi (US:AAPL)";
                    }
                } catch (Exception ignored) {}
            } else if ("IDX:BBCA".equalsIgnoreCase(inst.getId()) || "BBCA".equalsIgnoreCase(inst.getTicker())) {
                try {
                    InputStream is = HistoricalDatasetLoader.openAssetStream("data/historical/idx_bbca_daily.csv");
                    if (is != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buf = new byte[8192];
                        int r;
                        while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
                        List<HistoricalBar> bbcaBars = HistoricalDatasetLoader.loadFromCsvString(new String(baos.toByteArray(), StandardCharsets.UTF_8));
                        if (bbcaBars != null && !bbcaBars.isEmpty()) {
                            int count = Math.min(bbcaBars.size(), 30);
                            List<HistoricalBar> recent = bbcaBars.subList(bbcaBars.size() - count, bbcaBars.size());
                            for (HistoricalBar hb : recent) {
                                JSONObject bObj = new JSONObject();
                                bObj.put("time", hb.getDate().toString());
                                bObj.put("open", hb.getOpen().doubleValue());
                                bObj.put("high", hb.getHigh().doubleValue());
                                bObj.put("low", hb.getLow().doubleValue());
                                bObj.put("close", hb.getClose().doubleValue());
                                bObj.put("volume", hb.getVolume());
                                bObj.put("isReference", false);
                                bars.put(bObj);
                            }
                            sourceLabel = "Data historis harian bursa resmi (IDX:BBCA)";
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        if (bars.length() == 0 && fills.isEmpty()) {
            BigDecimal refPrice = inst.getCurrentPrice().getAmount();
            double base = refPrice.doubleValue();
            if ("D1".equals(tf)) {
                LocalDate curDate = clock.getCurrentDate();
                for (int d = 4; d >= 0; d--) {
                    LocalDate dDate = curDate.minusDays(d);
                    JSONObject bar = new JSONObject();
                    bar.put("time", dDate.toString());
                    bar.put("open", base);
                    bar.put("high", base);
                    bar.put("low", base);
                    bar.put("close", base);
                    bar.put("volume", 0);
                    bar.put("isReference", true);
                    bars.put(bar);
                }
                sourceLabel = "Harga acuan harian simulasi game";
            } else {
                JSONObject bar1 = new JSONObject();
                bar1.put("time", "Ref 09:00");
                bar1.put("open", base);
                bar1.put("high", base);
                bar1.put("low", base);
                bar1.put("close", base);
                bar1.put("volume", 0);
                bar1.put("isReference", true);
                bars.put(bar1);
                sourceLabel = "Harga acuan sesi aktif";
            }
        } else if (bars.length() == 0) {
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
        result.put("source", sourceLabel);
        return result;
    }

    private JSONObject getChartRange(Instrument instrument, String range) throws JSONException {
        long duration = "R6H".equals(range) ? 21600000L : "R1D".equals(range) ? 86400000L
                : "R7D".equals(range) ? 604800000L : 2592000000L;
        long interval = "R6H".equals(range) ? 300000L : "R1D".equals(range) ? 900000L
                : "R7D".equals(range) ? 14400000L : 86400000L;
        long end = clock.getGameTimestamp(), start = end - duration;
        JSONArray bars = new JSONArray();
        int trades = 0;
        String source = "MATCHED_TRADES";
        if (gameMode == GameMode.HISTORICAL_REPLAY) {
            source = "HISTORICAL_DAILY";
            interval = Math.max(interval, 86400000L);
            if (instrument.getId().equals(replayEngine.getActiveDatasetId())) {
                for (HistoricalBar bar : replayEngine.getVisibleBars(clock.getCurrentDate(), clock.getCurrentTime())) {
                    long timestamp = bar.getDate().atStartOfDay(java.time.ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli();
                    if (timestamp < start || timestamp > end) continue;
                    bars.put(new JSONObject().put("timestamp", timestamp).put("time", bar.getDate().toString())
                            .put("open", bar.getOpen()).put("high", bar.getHigh()).put("low", bar.getLow())
                            .put("close", bar.getClose()).put("volume", bar.getVolume()).put("isReference", false)
                            .put("partial", false));
                }
            }
        } else {
            OrderBook book = marketEngine.getBook(instrument.getId());
            Map<String, TradeFill> unique = new LinkedHashMap<>();
            if (book != null) for (TradeFill fill : book.getTradeHistory()) {
                if (fill.getTimestamp() >= start && fill.getTimestamp() <= end) unique.putIfAbsent(fill.getFillId(), fill);
            }
            List<TradeFill> fills = new ArrayList<>(unique.values());
            fills.sort(Comparator.comparingLong(TradeFill::getTimestamp).thenComparing(TradeFill::getFillId));
            Map<Long, List<TradeFill>> buckets = new TreeMap<>();
            for (TradeFill fill : fills) buckets.computeIfAbsent(Math.floorDiv(fill.getTimestamp(), interval) * interval,
                    key -> new ArrayList<>()).add(fill);
            for (Map.Entry<Long, List<TradeFill>> entry : buckets.entrySet()) {
                List<TradeFill> items = entry.getValue();
                BigDecimal open = items.get(0).getPrice().getAmount(), high = open, low = open;
                long volume = 0;
                for (TradeFill fill : items) {
                    high = high.max(fill.getPrice().getAmount()); low = low.min(fill.getPrice().getAmount());
                    volume = Math.addExact(volume, fill.getQuantity());
                }
                long timestamp = entry.getKey();
                bars.put(new JSONObject().put("timestamp", timestamp)
                        .put("time", java.time.Instant.ofEpochMilli(timestamp).atZone(java.time.ZoneId.of("Asia/Jakarta"))
                                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM HH:mm", java.util.Locale.forLanguageTag("id-ID"))))
                        .put("open", open).put("high", high).put("low", low)
                        .put("close", items.get(items.size() - 1).getPrice().getAmount()).put("volume", volume)
                        .put("isReference", false).put("partial", timestamp < start || timestamp + interval > end));
            }
            trades = fills.size();
        }
        return new JSONObject().put("success", true).put("instrumentId", instrument.getId()).put("ticker", instrument.getTicker())
                .put("currency", instrument.getCurrency().name()).put("lotSize", instrument.getLotSize())
                .put("range", range).put("rangeStart", start).put("rangeEnd", end).put("intervalMs", interval)
                .put("totalTrades", trades).put("bars", bars).put("source", source)
                .put("coverage", bars.length() == 0 ? "EMPTY" : "AVAILABLE_HISTORY")
                .put("historyComplete", false);
    }

    public synchronized com.tradingglobal3d.core.boss.MarketBossService getMarketBossService() {
        return marketBossService;
    }

    public synchronized com.tradingglobal3d.core.risk.TaxAuditService getTaxAuditService() {
        return taxAuditService;
    }

    public synchronized JSONArray getMarketBossesJson() throws JSONException {
        return marketBossService.exportBosses();
    }

    public synchronized JSONObject startBossDuel(String bossId) throws JSONException {
        com.tradingglobal3d.core.boss.MarketBoss b = marketBossService.startDuel(bossId);
        return b.toJsonObject();
    }

    public synchronized JSONObject getTaxAuditStatus() throws JSONException {
        Account personal = getPersonalAccount();
        Money netWorth = getPersonalNetWorth();
        Money liabilities = personal != null ? riskService.getMarginDebt(personal.getId(), Currency.IDR) : Money.zero(Currency.IDR);
        Money assets = netWorth.add(liabilities);
        return taxAuditService.evaluateCompliance(personal, assets, liabilities).toJsonObject();
    }

    public synchronized JSONArray getResearchBoostersJson() throws JSONException {
        return researchService.getBoostersJson();
    }

    public synchronized JSONObject activateResearchBooster(String boosterId) throws JSONException {
        JSONObject res = researchService.activateBooster(boosterId);
        persistFinancialMutation();
        return res;
    }

    public synchronized com.tradingglobal3d.core.risk.PortfolioInsuranceService getPortfolioInsuranceService() {
        return portfolioInsuranceService;
    }

    public synchronized JSONObject purchasePortfolioInsurance(int durationSessions) throws JSONException {
        Account personal = getPersonalAccount();
        if (personal == null) return new JSONObject().put("success", false).put("message", "Akun pribadi tidak ditemukan");
        Money assets = getPersonalNetWorth();
        int curSession = playerProfileManager.getRunDaysPlayed();
        com.tradingglobal3d.core.risk.PortfolioInsuranceService.Policy pol = portfolioInsuranceService.purchasePolicy(
                personal, assets, durationSessions, ledger, clock.getGameTimestamp(), curSession
        );
        persistFinancialMutation();
        return new JSONObject().put("success", true).put("policy", pol.toJsonObject(curSession));
    }

    public synchronized JSONObject getPortfolioInsuranceStatus() throws JSONException {
        int curSession = playerProfileManager.getRunDaysPlayed();
        return portfolioInsuranceService.toJsonObject(curSession);
    }

    public synchronized JSONObject previewTurnaroundBailout() throws JSONException {
        Account personal = getPersonalAccount();
        if (personal == null) return new JSONObject().put("eligible", false).put("reason", "Akun pribadi belum siap");
        BigDecimal usdRate = fxService.getMidRate();
        Money totalDebt = riskService.getMarginDebt(personal.getId(), Currency.IDR)
                .add(new Money(riskService.getMarginDebt(personal.getId(), Currency.USD).getAmount().multiply(usdRate), Currency.IDR))
                .add(installmentLoanManager.getTotalOutstandingLiability(personal.getId()));
        Money netWorth = getPersonalNetWorth();
        Money cash = personal.getAvailableCash(Currency.IDR);
        boolean eligible = totalDebt.isPositive() && cash.getAmount().compareTo(BigDecimal.valueOf(500_000)) < 0
                && !netWorth.isPositive();
        JSONArray auctionItems = new JSONArray();
        Money estimatedProceeds = Money.zero(Currency.IDR);
        for (com.tradingglobal3d.core.collection.CollectibleItem item : collectionManager.getAllItems()) {
            if (!item.isOwned() || !personal.getId().equals(item.getOwnerAccountId()) || item.isPledged()) continue;
            auctionItems.put(new JSONObject().put("id", item.getId()).put("name", item.getName())
                    .put("estimatedValue", item.getCurrentValuation().format()));
            estimatedProceeds = estimatedProceeds.add(item.getCurrentValuation());
        }
        return new JSONObject().put("eligible", eligible)
                .put("reason", eligible ? "Akun memenuhi syarat pemulihan insolvensi" : "Perlu utang aktif, kas IDR di bawah Rp 500.000, dan kekayaan bersih tidak positif")
                .put("cash", cash.format()).put("netWorth", netWorth.format()).put("totalDebt", totalDebt.format())
                .put("auctionItems", auctionItems).put("estimatedAuctionProceeds", estimatedProceeds.format())
                .put("warning", "Koleksi tidak diagunkan yang tercantum akan dijual; perkiraan hasil bukan jaminan harga");
    }

    public synchronized JSONObject executeTurnaroundBailout(String requestId) throws JSONException {
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Akun pribadi belum siap");
        if (requestId == null || requestId.trim().isEmpty() || requestId.length() > 120)
            throw new IllegalArgumentException("ID permintaan turnaround tidak valid");
        String markerId = "TURNAROUND-" + requestId;
        if (ledger.isRequestProcessed(markerId))
            return new JSONObject().put("success", true).put("alreadyProcessed", true)
                    .put("message", "Permintaan turnaround ini sudah diproses.");

        BigDecimal usdRate = fxService.getMidRate();
        Money marginIdr = riskService.getMarginDebt(personal.getId(), Currency.IDR);
        Money marginUsd = new Money(riskService.getMarginDebt(personal.getId(), Currency.USD).getAmount()
                .multiply(usdRate), Currency.IDR);
        Money installmentDebt = installmentLoanManager.getTotalOutstandingLiability(personal.getId());
        Money totalDebt = marginIdr.add(marginUsd).add(installmentDebt);
        if (!totalDebt.isPositive() || personal.getAvailableCash(Currency.IDR).getAmount().compareTo(BigDecimal.valueOf(500_000)) >= 0
                || getPersonalNetWorth().isPositive())
            throw new IllegalStateException("Fasilitas turnaround hanya tersedia saat akun insolven: utang aktif, kas IDR di bawah Rp 500.000, dan kekayaan bersih tidak positif.");

        int auctionedCollections = 0;
        Money auctionProceeds = Money.zero(Currency.IDR);
        for (com.tradingglobal3d.core.collection.CollectibleItem item : collectionManager.getAllItems()) {
            if (!item.isOwned() || !personal.getId().equals(item.getOwnerAccountId()) || item.isPledged()) continue;
            JSONObject receipt = collectionManager.sellItem(requestId + "-AUCTION-" + item.getId(), item.getId(),
                    personal, ledger, careerManager, clock.getGameTimestamp());
            if (receipt.getBoolean("success")) {
                auctionedCollections++;
                auctionProceeds = auctionProceeds.add(new Money(new BigDecimal(receipt.getString("amount")), Currency.IDR));
            }
        }

        Money marginRepaidIdr = Money.zero(Currency.IDR);
        Money availableIdr = personal.getAvailableCash(Currency.IDR);
        if (availableIdr.isPositive() && marginIdr.isPositive()) {
            marginRepaidIdr = marginIdr.isLessThan(availableIdr) ? marginIdr : availableIdr;
            riskService.repayMarginLoan(personal, marginRepaidIdr, ledger, clock.getGameTimestamp());
        }
        Money marginRepaidUsd = Money.zero(Currency.USD);
        Money availableUsd = personal.getAvailableCash(Currency.USD);
        Money outstandingUsd = riskService.getMarginDebt(personal.getId(), Currency.USD);
        if (availableUsd.isPositive() && outstandingUsd.isPositive()) {
            marginRepaidUsd = outstandingUsd.isLessThan(availableUsd) ? outstandingUsd : availableUsd;
            riskService.repayMarginLoan(personal, marginRepaidUsd, ledger, clock.getGameTimestamp());
        }

        JSONArray restructuredLoans = new JSONArray();
        for (LoanContract loan : installmentLoanManager.getAllLoans()) {
            if (!personal.getId().equals(loan.getAccountId()) || loan.getTotalPayoffAmount().isZero()) continue;
            JSONObject receipt = loan.restructure(30, clock.getGameTimestamp());
            restructuredLoans.put(receipt);
        }
        JSONObject marginRestructure = new JSONObject();
        for (Currency currency : Currency.values()) {
            Money debt = riskService.getMarginDebt(personal.getId(), currency);
            if (debt.isPositive()) {
                Money relief = riskService.restructureDebt(personal, currency, clock.getGameTimestamp());
                marginRestructure.put(currency.name(), new JSONObject().put("debtRelief", relief.format())
                        .put("remainingDebt", riskService.getMarginDebt(personal.getId(), currency).format()));
            }
        }
        careerManager.penalizeReputation(10);
        ledger.recordMutation(markerId, personal, LedgerEntryType.BAILOUT, Money.zero(Currency.IDR), true,
                clock.getGameTimestamp(), "Receipt idempoten: turnaround, lelang koleksi, dan restrukturisasi utang");
        persistFinancialMutation();
        return new JSONObject().put("success", true).put("alreadyProcessed", false)
                .put("auctionedCollections", auctionedCollections).put("auctionProceeds", auctionProceeds.format())
                .put("marginDebtRepaidIdr", marginRepaidIdr.format()).put("marginDebtRepaidUsd", marginRepaidUsd.format())
                .put("debtRestructure", marginRestructure).put("restructuredLoans", restructuredLoans)
                .put("cashGrant", Money.zero(Currency.IDR).format()).put("reputationPenalty", 10)
                .put("message", "Turnaround selesai: koleksi dilelang untuk membayar utang, lalu sisa kewajiban dijadwalkan ulang dengan penalti reputasi.");
    }

    public synchronized JSONObject createCustomMutualFund(String fundName, double eqPct, double commPct, double cryptoPct, double cashPct, BigDecimal aum) throws JSONException {
        if (fundName == null || fundName.trim().isEmpty()) throw new IllegalArgumentException("Nama reksa dana wajib diisi");
        double totalPct = eqPct + commPct + cryptoPct + cashPct;
        if (Math.abs(totalPct - 100.0) > 0.01) throw new IllegalArgumentException("Total alokasi aset harus 100%");

        String fundId = "FUND-CUST-" + (customMutualFunds.size() + 1);
        JSONObject obj = new JSONObject();
        obj.put("fundId", fundId);
        obj.put("fundName", fundName);
        obj.put("equityWeightPct", eqPct);
        obj.put("commodityWeightPct", commPct);
        obj.put("cryptoWeightPct", cryptoPct);
        obj.put("cashWeightPct", cashPct);
        obj.put("totalAum", Money.of(aum != null ? aum.doubleValue() : 25_000_000L, Currency.IDR).format());
        obj.put("managementFeePct", "1.5%");
        obj.put("performanceFeePct", "15.0%");
        customMutualFunds.add(obj);
        persistFinancialMutation();
        return obj;
    }

    public synchronized JSONArray getCustomMutualFundsJson() {
        JSONArray arr = new JSONArray();
        for (JSONObject f : customMutualFunds) arr.put(f);
        return arr;
    }

    public synchronized JSONArray getHoldingSubsidiariesJson() throws JSONException {
        JSONArray arr = new JSONArray();
        for (com.tradingglobal3d.core.business.OperatingCompany c : companyManager.getAllCompanies()) {
            JSONObject obj = new JSONObject();
            obj.put("companyId", c.getId());
            obj.put("companyName", c.getName());
            obj.put("sector", c.getSector());
            Account compAcc = accounts.get(c.getAccountId());
            Money cash = compAcc != null ? compAcc.getTotalCash(Currency.IDR) : Money.zero(Currency.IDR);
            obj.put("currentCash", cash.format());
            obj.put("morale", c.getMorale());
            arr.put(obj);
        }
        return arr;
    }

    public synchronized com.tradingglobal3d.core.research.ResearchTechTreeService getResearchTechTreeService() {
        return researchTechTreeService;
    }

    public synchronized com.tradingglobal3d.core.firm.HeadhuntingService getHeadhuntingService() {
        return headhuntingService;
    }

    public synchronized JSONArray getTechTreeJson() throws JSONException {
        return researchTechTreeService.exportTree();
    }

    public synchronized JSONObject getAlgorithmicMarketSignal(String instrumentId) throws JSONException {
        Instrument instrument = requireInstrument(instrumentId);
        double bonus = researchTechTreeService.getUnlockedBonus("SIGNAL_ACCURACY");
        if (bonus <= 0.0) return new JSONObject().put("available", false).put("reason", "Buka node Algoritma sebelum meminta sinyal.");
        double threshold = Math.max(0.10, 1.0 - bonus / 100.0);
        double change = instrument.getDailyChangePercent();
        String direction = change > threshold ? "BUY" : change < -threshold ? "SELL" : "HOLD";
        return new JSONObject().put("available", true).put("instrumentId", instrument.getId())
                .put("direction", direction).put("observedChangePercent", change)
                .put("signalBonusPercent", bonus).put("triggerThresholdPercent", threshold)
                .put("basis", "current-day-price-change").put("usesFutureData", false)
                .put("disclaimer", "Sinyal simulasi dari perubahan harga sesi berjalan; bukan prediksi atau rekomendasi.");
    }

    public synchronized JSONObject upgradeTechNode(String nodeId) throws JSONException {
        Account personal = getPersonalAccount();
        if (personal == null) return new JSONObject().put("success", false).put("message", "Akun tidak ditemukan");
        com.tradingglobal3d.core.research.ResearchTechTreeService.TechNode n = researchTechTreeService.upgradeNode(nodeId, personal, ledger, clock.getGameTimestamp());
        persistFinancialMutation();
        return n.toJsonObject().put("success", true);
    }

    public synchronized JSONArray getHeadhuntingCandidatesJson() throws JSONException {
        return headhuntingService.exportCandidates();
    }

    public synchronized JSONObject poachStaffCandidate(String candidateId) throws JSONException {
        Account personal = getPersonalAccount();
        if (personal == null) return new JSONObject().put("success", false).put("message", "Akun tidak ditemukan");
        com.tradingglobal3d.core.firm.HeadhuntingService.Candidate c = headhuntingService.poachCandidate(candidateId, personal, ledger, clock.getGameTimestamp());
        persistFinancialMutation();
        return c.toJsonObject().put("success", true);
    }

    public synchronized com.tradingglobal3d.core.player.DailyStreakService getDailyStreakService() {
        return dailyStreakService;
    }

    public synchronized com.tradingglobal3d.core.market.SectorDominanceService getSectorDominanceService() {
        return sectorDominanceService;
    }

    public synchronized JSONObject getDailyStreakJson() throws JSONException {
        dailyStreakService.recordSessionLogin(playerProfileManager.getRunDaysPlayed());
        return dailyStreakService.toJsonObject();
    }

    public synchronized JSONArray getSectorDominanceJson() throws JSONException {
        Account personal = getPersonalAccount();
        BigDecimal usdRate = fxService != null ? fxService.getMidRate() : BigDecimal.valueOf(15800);
        return sectorDominanceService.exportDominance(personal, catalog, usdRate);
    }

    public synchronized JSONObject triggerMarketShockEvent(String eventType) throws JSONException {
        // Black Swan Shock (15% drop) or Supply Chain Crisis
        if (!"BLACK_SWAN_SHOCK".equalsIgnoreCase(eventType) && !"SUPPLY_CHAIN_CRISIS".equalsIgnoreCase(eventType))
            throw new IllegalArgumentException("Jenis kejutan pasar tidak dikenal");
        boolean supplyChainCrisis = "SUPPLY_CHAIN_CRISIS".equalsIgnoreCase(eventType);
        double dropRate = supplyChainCrisis ? 0.08 : 0.15;
        Account personal = getPersonalAccount();
        Money startingNetWorth = personal != null ? getPersonalNetWorth() : Money.zero(Currency.IDR);
        Money cashBufferIdr = Money.zero(Currency.IDR);
        if (personal != null) cashBufferIdr = personal.getAvailableCash(Currency.IDR)
                .add(new Money(personal.getAvailableCash(Currency.USD).getAmount().multiply(fxService.getMidRate()), Currency.IDR));
        int affectedCompanies = supplyChainCrisis ? companyManager.applySupplyChainCrisis() : 0;
        int affectedCount = 0;
        Money totalLoss = Money.zero(Currency.IDR);
        Money grossEquityLoss = Money.zero(Currency.IDR);
        Money safeHavenGain = Money.zero(Currency.IDR);
        BigDecimal usdRate = fxService != null ? fxService.getMidRate() : BigDecimal.valueOf(15800);
        for (Instrument inst : catalog.getActiveUniverse()) {
            boolean isEnergy = inst.getSector() != null && inst.getSector().toLowerCase().contains("energi");
            boolean isManufacturing = inst.getSector() != null && (inst.getSector().toLowerCase().contains("manufaktur") || inst.getSector().toLowerCase().contains("industri") || inst.getSector().toLowerCase().contains("konsumer"));

            BigDecimal current = inst.getCurrentPrice().getAmount();
            BigDecimal newPrice;
            if ("SUPPLY_CHAIN_CRISIS".equalsIgnoreCase(eventType)) {
                if (isEnergy) {
                    newPrice = current.multiply(BigDecimal.valueOf(1.10)).setScale(0, java.math.RoundingMode.HALF_UP); // Energi naik +10%
                } else if (isManufacturing) {
                    newPrice = current.multiply(BigDecimal.valueOf(0.88)).setScale(0, java.math.RoundingMode.HALF_UP); // Manufaktur anjlok -12%
                } else {
                    newPrice = current;
                }
            } else {
                newPrice = current.multiply(BigDecimal.valueOf(1.0 - dropRate)).setScale(0, java.math.RoundingMode.HALF_UP);
            }
            if (newPrice.compareTo(BigDecimal.ONE) >= 0 && !newPrice.equals(current)) {
                Position playerPosition = personal != null ? personal.getPosition(inst.getId()) : null;
                if (playerPosition == null && personal != null) playerPosition = personal.getPosition(inst.getTicker());
                if (playerPosition != null && playerPosition.getQuantity() > 0 && newPrice.compareTo(current) < 0) {
                    BigDecimal rawLoss = current.subtract(newPrice).multiply(BigDecimal.valueOf(playerPosition.getQuantity()));
                    BigDecimal idrLoss = inst.getCurrency() == Currency.USD ? rawLoss.multiply(usdRate) : rawLoss;
                    totalLoss = totalLoss.add(new Money(idrLoss, Currency.IDR));
                    grossEquityLoss = grossEquityLoss.add(new Money(idrLoss, Currency.IDR));
                }
                inst.updatePrice(new Money(newPrice, inst.getCurrency()), 0);
                affectedCount++;
            }
        }
        double safeHavenMovePercent = 0.0;
        if ("BLACK_SWAN_SHOCK".equalsIgnoreCase(eventType)) {
            com.tradingglobal3d.core.multiasset.CommodityInstrument gold = fundBuilder.getCommodity("COMM:GOLD");
            if (gold != null) {
                BigDecimal goldBefore = gold.getSpotPricePerUnit().getAmount();
                BigDecimal goldAfter = goldBefore.multiply(new BigDecimal("1.05")).setScale(0, java.math.RoundingMode.HALF_UP);
                gold.updateSpotPrice(new Money(goldAfter, gold.getCurrency()));
                safeHavenMovePercent = 5.0;
                if (personal != null) {
                    Position goldPosition = personal.getPosition("COMM:XAU");
                    if (goldPosition != null && goldPosition.getQuantity() > 0) {
                        safeHavenGain = new Money(goldAfter.subtract(goldBefore)
                                .multiply(BigDecimal.valueOf(goldPosition.getQuantity())), Currency.IDR);
                        totalLoss = totalLoss.subtract(safeHavenGain);
                        if (totalLoss.isNegative()) totalLoss = Money.zero(Currency.IDR);
                    }
                }
            }
        }
        Money reimbursement = Money.zero(Currency.IDR);
        boolean insuranceClaimSettled = false;
        int currentSession = playerProfileManager.getRunDaysPlayed();
        if (personal != null && totalLoss.isPositive() && portfolioInsuranceService.getActivePolicy() != null) {
            reimbursement = portfolioInsuranceService.evaluateMarketCrashClaim(personal, dropRate * 100.0,
                    totalLoss, ledger, clock.getGameTimestamp(), currentSession);
            insuranceClaimSettled = reimbursement.isPositive();
        }
        double netLossPctOfStartingWealth = startingNetWorth.isPositive()
                ? totalLoss.getAmount().multiply(BigDecimal.valueOf(100)).divide(startingNetWorth.getAmount(), 4, java.math.RoundingMode.HALF_UP).doubleValue()
                : 0.0;
        persistFinancialMutation();
        return new JSONObject().put("success", true)
                .put("eventType", eventType)
                .put("affectedInstruments", affectedCount)
                .put("dropRatePercent", dropRate * 100)
                .put("startingNetWorth", startingNetWorth.format())
                .put("cashBufferIdr", cashBufferIdr.format())
                .put("netLossPercentOfStartingWealth", netLossPctOfStartingWealth)
                .put("affectedCompanies", affectedCompanies)
                .put("safeHavenGoldMovePercent", safeHavenMovePercent)
                .put("safeHavenGain", safeHavenGain.getAmount().toPlainString())
                .put("grossPlayerLoss", grossEquityLoss.getAmount().toPlainString())
                .put("netPlayerLoss", totalLoss.getAmount().toPlainString())
                .put("playerPortfolioLoss", totalLoss.format())
                .put("insuranceClaimSettled", insuranceClaimSettled)
                .put("insuranceReimbursement", reimbursement.format());
    }

    private String readStreamUtf8(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toString(StandardCharsets.UTF_8.name());
    }

    private JSONObject companyProfilesCache;

    public synchronized com.tradingglobal3d.core.multiasset.TimeDepositManager getTimeDepositManager() {
        return timeDepositManager;
    }

    public synchronized JSONArray getTimeDepositProducts() throws JSONException {
        return timeDepositManager.exportProducts();
    }

    public synchronized JSONArray getActiveTimeDeposits() throws JSONException {
        return timeDepositManager.exportActiveDeposits(clock.getGameTimestamp());
    }

    public synchronized JSONObject openTimeDeposit(String productId, BigDecimal amount) throws JSONException {
        Account personal = getPersonalAccount();
        if (personal == null) return new JSONObject().put("success", false).put("message", "Akun pribadi belum tersedia");
        Money principal = new Money(amount, Currency.IDR);
        com.tradingglobal3d.core.multiasset.TimeDeposit dep = timeDepositManager.openDeposit(personal, productId, principal, clock.getGameTimestamp(), ledger);
        persistFinancialMutation();
        return new JSONObject().put("success", true).put("deposit", dep.toJsonObject(clock.getGameTimestamp()));
    }

    public synchronized JSONObject withdrawTimeDeposit(String depositId) throws JSONException {
        Account personal = getPersonalAccount();
        if (personal == null) return new JSONObject().put("success", false).put("message", "Akun pribadi belum tersedia");
        com.tradingglobal3d.core.multiasset.TimeDeposit dep = timeDepositManager.withdrawDeposit(depositId, personal, clock.getGameTimestamp(), ledger);
        persistFinancialMutation();
        return new JSONObject().put("success", true).put("deposit", dep.toJsonObject(clock.getGameTimestamp()));
    }

    public synchronized JSONObject getIssuerFundamentals(String instrumentId) throws JSONException {
        Instrument inst = catalog.getById(instrumentId);
        if (inst == null) return new JSONObject().put("success", false).put("message", "Emiten tidak ditemukan: " + instrumentId);
        com.tradingglobal3d.core.market.IssuerFundamentals f = new com.tradingglobal3d.core.market.IssuerFundamentals(
                inst.getId(), inst.getTicker(), inst.getName(), inst.getExchange(),
                inst.getSector(), inst.getCurrency(), inst.getSharesOutstanding(), inst.getCurrentPrice().getAmount()
        );
        return f.toJsonObject(inst.getCurrentPrice());
    }

    public synchronized JSONObject getCompanyProfile(String instrumentId) {
        if (companyProfilesCache == null) {
            try (InputStream is = HistoricalDatasetLoader.openAssetStream("data/company_profiles.json")) {
                if (is != null) {
                    String json = readStreamUtf8(is);
                    companyProfilesCache = new JSONObject(json);
                } else {
                    companyProfilesCache = new JSONObject();
                }
            } catch (Exception e) {
                companyProfilesCache = new JSONObject();
            }
        }
        JSONObject profile = companyProfilesCache != null ? companyProfilesCache.optJSONObject(instrumentId) : null;
        if (profile != null) {
            try {
                return new JSONObject(profile.toString()).put("success", true);
            } catch (JSONException ignored) {}
        }
        Instrument inst = catalog.getById(instrumentId);
        if (inst != null) {
            try {
                JSONObject fallback = new JSONObject();
                fallback.put("success", true);
                fallback.put("id", inst.getId());
                fallback.put("ticker", inst.getTicker());
                fallback.put("name", inst.getName());
                fallback.put("exchange", inst.getExchange().name());
                fallback.put("country", inst.getExchange() == com.tradingglobal3d.core.market.MarketExchange.IDX ? "Indonesia" : "Amerika Serikat");
                fallback.put("sector", inst.getSector());
                fallback.put("currency", inst.getCurrency().name());
                fallback.put("description", inst.getName() + " adalah emiten terdaftar pada bursa " + inst.getExchange() + " di sektor " + inst.getSector() + ".");
                fallback.put("website", inst.getExchange() == com.tradingglobal3d.core.market.MarketExchange.IDX ? "https://www.idx.co.id" : "https://www.nasdaq.com");
                fallback.put("source", "Katalog Saham Resmi");
                fallback.put("verifiedDate", "2026-10-22");
                return fallback;
            } catch (JSONException ignored) {}
        }
        try {
            return new JSONObject().put("success", false).put("message", "Profil tidak ditemukan untuk " + instrumentId);
        } catch (JSONException e) {
            return new JSONObject();
        }
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

    private void persistFinancialMutation() {
        try { save(); }
        catch (IOException error) { throw new IllegalStateException("Penyimpanan gagal; ulangi permintaan dengan ID yang sama", error); }
        catch (org.json.JSONException error) { throw new IllegalStateException("Serialisasi save gagal", error); }
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

    public synchronized JSONObject equipCosmetic(String type, String cosmeticId) throws JSONException {
        if ("AVATAR".equalsIgnoreCase(type)) {
            playerProfileManager.setEquippedAvatar(cosmeticId);
        } else if ("FRAME".equalsIgnoreCase(type)) {
            playerProfileManager.setEquippedFrame(cosmeticId);
        }
        persistFinancialMutation();
        return new JSONObject().put("success", true)
                .put("equippedAvatar", playerProfileManager.getEquippedAvatar())
                .put("equippedFrame", playerProfileManager.getEquippedFrame());
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
        Account personal = getPersonalAccount();
        BigDecimal usdRate = fxService != null ? fxService.getMidRate() : BigDecimal.valueOf(15800);

        res.put("careerRank", careerManager != null ? careerManager.getCurrentRank().getTitle() : "-");
        res.put("careerRankId", careerManager != null ? careerManager.getCurrentRank().name() : "INTERN");
        res.put("reputation", careerManager != null ? careerManager.getReputationScore() : 0);
        res.put("currentLevel", progressManager.getCurrentLevel());
        res.put("currentXp", progressManager.getCurrentXp());
        res.put("netWorth", getPersonalNetWorth().format());
        res.put("netWorthRaw", getPersonalNetWorth().getAmount().toPlainString());

        // 1. Ringkasan Keuangan Lengkap (A10 Seksi 2)
        JSONObject fin = new JSONObject();
        fin.put("personalNetWorth", getPersonalNetWorth().format());
        fin.put("personalCashIdr", personal != null ? personal.getTotalCash(Currency.IDR).format() : "Rp 0");
        fin.put("personalCashUsd", personal != null ? personal.getTotalCash(Currency.USD).format() : "$0.00");
        Money secVal = Money.zero(Currency.IDR);
        if (personal != null) {
            for (Position p : personal.getPositions().values()) {
                Money val = p.getMarketValue();
                secVal = secVal.add(val.getCurrency() == Currency.IDR ? val : new Money(val.getAmount().multiply(usdRate), Currency.IDR));
            }
        }
        fin.put("securitiesValueIdr", secVal.format());
        fin.put("collectionsValueIdr", collectionManager.calculateTotalValuation().format());
        fin.put("mutualFundsValueIdr", mutualFundManager.calculateTotalHoldingsValue(personal != null ? personal.getId() : "").format());
        Money marginDebt = personal != null ? riskService.getMarginDebt(personal.getId(), Currency.IDR) : Money.zero(Currency.IDR);
        fin.put("liabilitiesIdr", marginDebt.format());
        Money clientAum = Money.zero(Currency.IDR);
        for (ClientMandate mandate : firmManager.getAllMandates()) {
            Account account = accounts.get(mandate.getClientAccountId());
            if (mandate.getStatus() == ClientMandate.MandateStatus.ACTIVE && account != null) {
                clientAum = clientAum.add(account.getTotalPortfolioValue(Currency.IDR, usdRate));
            }
        }
        fin.put("clientAumIdr", clientAum.format());
        res.put("financialSummary", fin);

        // 2. Statistik Trading Eksplisit (A10 Seksi 3)
        int totalTrades = playerProfileManager.getTotalTradesExecuted();
        int profitable = playerProfileManager.getProfitableTradesCount();
        int closed = res.optInt("closedTrades", 0);
        int loss = Math.max(0, closed - profitable);
        res.put("totalTrades", totalTrades);
        res.put("profitableTrades", profitable);
        res.put("lossTrades", loss);
        res.put("closedTrades", closed);
        if (closed > 0) {
            double winRateVal = (profitable * 100.0) / closed;
            res.put("winRate", String.format(java.util.Locale.US, "%.1f%% (%d menang / %d selesai)", winRateVal, profitable, closed));
            res.put("winRatePercent", winRateVal);
        } else {
            res.put("winRate", "Belum Ada Data");
            res.put("winRatePercent", 0.0);
        }
        res.put("bestTradePnL", playerProfileManager.getBestTradePnL().format());
        res.put("worstTradePnL", playerProfileManager.getWorstTradePnL().format());

        // 3. Peringkat Liga & Performa (A10 Seksi 4 & 5)
        String pName = playerProfileManager.getPlayerName();
        res.put("playerLeagueRank", getPersonalNetWorth() != null ? String.valueOf(leagueManager.getLeaderboardByNetWorth(getPersonalNetWorth(), pName, careerManager.getCurrentRank().getTitle()).optInt("playerRank", 1)) : "1");
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

    public synchronized JSONObject getDailySponsorStatus() throws JSONException {
        dailyStreakService.recordSessionLogin(playerProfileManager.getRunDaysPlayed());
        java.time.ZoneId wib = java.time.ZoneId.of("Asia/Jakarta");
        LocalDate todayReal = LocalDate.now(realTimeClock.withZone(wib));
        long nowReal = realTimeClock.millis();

        boolean eligible = (dailySponsorLastClaimedRealDate == null || !dailySponsorLastClaimedRealDate.equals(todayReal.toString()));
        // Perlindungan manipulasi jam mundur (offline time rewind)
        if (dailySponsorLastClaimedRealTimestamp > nowReal + 60_000L && dailySponsorLastClaimedRealDate != null && dailySponsorLastClaimedRealDate.compareTo(todayReal.toString()) >= 0) {
            eligible = false;
        }

        java.time.LocalDateTime nextMidnight = todayReal.plusDays(1).atStartOfDay();
        long nextClaimAtMs = nextMidnight.atZone(wib).toInstant().toEpochMilli();

        JSONObject status = new JSONObject();
        status.put("eligible", eligible);
        status.put("alreadyClaimed", !eligible);
        Money dailyGrant = currentDailySponsorGrant();
        status.put("grantAmount", dailyGrant.getAmount().longValueExact());
        status.put("grantAmountFormatted", dailyGrant.format());
        status.put("nextClaimAt", nextClaimAtMs);
        status.put("nextClaimAtLabel", "00.00 WIB");
        status.put("todayRealDate", todayReal.toString());
        status.put("lastClaimedDate", dailySponsorLastClaimedRealDate != null ? dailySponsorLastClaimedRealDate : JSONObject.NULL);
        return status;
    }

    public synchronized JSONObject claimDailySponsor(String requestId) throws JSONException {
        if (requestId == null || requestId.trim().isEmpty()) throw new IllegalArgumentException("Request ID wajib");
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Akun personal belum siap");

        java.time.ZoneId wib = java.time.ZoneId.of("Asia/Jakarta");
        LocalDate todayReal = LocalDate.now(realTimeClock.withZone(wib));
        long nowReal = realTimeClock.millis();

        java.time.LocalDateTime nextMidnight = todayReal.plusDays(1).atStartOfDay();
        long nextClaimAtMs = nextMidnight.atZone(wib).toInstant().toEpochMilli();

        String todayKey = "SPONSOR-REAL-" + todayReal;

        if (ledger.isRequestProcessed(requestId)) {
            // Idempotent retry: jika request sama sudah diproses
            com.tradingglobal3d.core.economy.LedgerEntry receipt = ledger.getProcessedEntry(todayKey);
            Money credited = receipt != null && receipt.isCredit() ? receipt.getAmount() : currentDailySponsorGrant();
            return new JSONObject().put("success", true)
                    .put("amount", credited.format())
                    .put("message", "Permintaan klaim hibah harian sudah diproses.")
                    .put("alreadyClaimed", true)
                    .put("nextClaimAt", nextClaimAtMs);
        }

        // Cek eligibility tanggal dunia nyata Asia/Jakarta
        if (dailySponsorLastClaimedRealDate != null && dailySponsorLastClaimedRealDate.equals(todayReal.toString())) {
            return new JSONObject().put("success", false)
                    .put("alreadyClaimed", true)
                    .put("message", "Dana sponsor harian sudah diklaim hari ini. Tersedia lagi pukul 00.00 WIB")
                    .put("nextClaimAt", nextClaimAtMs);
        }
        if (ledger.isRequestProcessed(todayKey)) {
            dailySponsorLastClaimedRealDate = todayReal.toString();
            return new JSONObject().put("success", false)
                    .put("alreadyClaimed", true)
                    .put("message", "Dana sponsor harian sudah diklaim hari ini. Tersedia lagi pukul 00.00 WIB")
                    .put("nextClaimAt", nextClaimAtMs);
        }

        // Perlindungan manipulasi jam mundur
        if (dailySponsorLastClaimedRealTimestamp > nowReal + 60_000L && dailySponsorLastClaimedRealDate != null && dailySponsorLastClaimedRealDate.compareTo(todayReal.toString()) >= 0) {
            return new JSONObject().put("success", false)
                    .put("alreadyClaimed", true)
                    .put("message", "Waktu perangkat tidak valid. Tersedia lagi pukul 00.00 WIB")
                    .put("nextClaimAt", nextClaimAtMs);
        }

        Money grantAmount = currentDailySponsorGrant();
        long nowSim = clock != null ? clock.getGameTimestamp() : System.currentTimeMillis();

        dailySponsorLastClaimedRealDate = todayReal.toString();
        dailySponsorLastClaimedRealTimestamp = nowReal;

        ledger.recordMutation(todayKey, personal, LedgerEntryType.BAILOUT, grantAmount, true, nowSim, "Dana Sponsor Harian WIB " + todayReal);
        if (!todayKey.equals(requestId)) {
            ledger.recordMutation(requestId, personal, LedgerEntryType.BAILOUT, Money.zero(Currency.IDR), true, nowSim, "Tanda terima requestId klaim hibah: " + requestId);
        }

        persistFinancialMutation();
        return new JSONObject().put("success", true)
                .put("amount", grantAmount.format())
                .put("message", "Sponsor harian " + grantAmount.format() + " berhasil masuk ke kas pribadi!")
                .put("nextClaimAt", nextClaimAtMs)
                .put("alreadyClaimed", true);
    }

    private Money currentDailySponsorGrant() {
        BigDecimal base = BigDecimal.valueOf(25_000_000L);
        BigDecimal multiplier = BigDecimal.valueOf(dailyStreakService.getRewardMultiplier());
        return new Money(base.multiply(multiplier).setScale(0, java.math.RoundingMode.HALF_UP), Currency.IDR);
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
        Money netWorth = getPersonalNetWorth();
        totalLiabilities = grossAssets.subtract(netWorth);

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

        if (!careerManager.hasRank(CareerRank.PORTFOLIO_MANAGER)) {
            blockers.put("Perlu pangkat Manajer Portofolio");
        }
        if (playerProfileManager.getRunDaysPlayed() < 21) {
            blockers.put("Perlu minimal 21 hari perjalanan pada era ini (" + playerProfileManager.getRunDaysPlayed() + "/21)");
        }
        if (!firmManager.getActiveMandates().isEmpty()) {
            blockers.put("Selesaikan kewajiban mandat klien aktif (" + firmManager.getActiveMandates().size() + " mandat)");
        }

        // Pinjaman cicilan pribadi
        boolean hasLoanDebt = false;
        for (LoanContract loan : installmentLoanManager.getAllLoans()) {
            if (!loan.getTotalPayoffAmount().isZero()) {
                hasLoanDebt = true;
                break;
            }
        }
        if (hasLoanDebt) blockers.put("Lunasi seluruh pinjaman tenor pribadi");

        // Hanya periksa akun milik pemain (Personal)
        Account personal = getPersonalAccount();
        if (personal != null) {
            boolean hasMarginDebt = false;
            boolean hasReservedCash = false;
            for (Currency currency : Currency.values()) {
                if (riskService.getMarginDebt(personal.getId(), currency).isPositive()) hasMarginDebt = true;
                if (personal.getReservedCash(currency).isPositive()) hasReservedCash = true;
            }
            if (hasMarginDebt) blockers.put("Lunasi utang fasilitas margin pribadi");
            if (hasReservedCash) blockers.put("Masih ada dana tertahan pada order aktif");

            // Antrean order milik pemain (bukan order NPC / Market Maker)
            long playerRestingOrders = 0;
            for (Order o : marketEngine.getRestingOrders()) {
                if (personal.getId().equals(o.getAccountId()) && !o.getStatus().isFinal()) {
                    playerRestingOrders++;
                }
            }
            if (playerRestingOrders > 0) {
                blockers.put("Batalkan seluruh antrean order aktif milikmu (" + playerRestingOrders + " order)");
            }
        }

        for (var company : companyManager.getAllCompanies()) {
            if (company.getPayrollArrears().isPositive()) {
                blockers.put("Bayar tunggakan gaji pegawai usaha (" + company.getName() + ")");
                break;
            }
        }
        if (collectionManager.getTotalMaintenanceLiability().isPositive()) {
            blockers.put("Bayar tunggakan biaya pemeliharaan koleksi");
        }

        for (MutualFund fund : mutualFundManager.getAllFunds()) {
            JSONArray orders = fund.toJsonObject(personalAccountId).getJSONArray("orders");
            for (int i = 0; i < orders.length(); i++) {
                String status = orders.getJSONObject(i).getString("status");
                if (status.equals("ACCEPTED") || status.startsWith("PENDING_")) {
                    blockers.put("Selesaikan antrean transaksi reksa dana aktif");
                    break;
                }
            }
        }

        return new JSONObject().put("success", true).put("eligible", blockers.length() == 0).put("runId", runId)
                .put("currentRank", careerManager.getCurrentRank().getTitle())
                .put("requiredRank", CareerRank.PORTFOLIO_MANAGER.getTitle())
                .put("runDays", playerProfileManager.getRunDaysPlayed())
                .put("minimumRunDays", 21)
                .put("blockers", blockers)
                .put("description", "Pilihan memulai perjalanan baru. Identitas, preferensi, statistik lifetime dan riwayat era dipertahankan.")
                .put("prestigeBonus", "Badge kosmetik Era Baru")
                .put("reset", "Kas kembali Rp50 juta; posisi, order, bisnis, pegawai, koleksi, XP dan karier perjalanan direset. Seluruh kewajiban harus diselesaikan dahulu.")
                .put("retained", "Identitas, preferensi, statistik lifetime, riwayat prestige dan backup perjalanan lama");
    }

    public synchronized JSONObject confirmPrestige(String requestId, String expectedRunId) throws JSONException {
        if (gameMode == GameMode.HISTORICAL_REPLAY) throw new IllegalStateException("Prestige tidak tersedia dalam replay historis");
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
