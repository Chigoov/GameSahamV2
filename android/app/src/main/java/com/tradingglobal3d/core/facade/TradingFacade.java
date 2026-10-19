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
    private boolean recoveryRequired;

    public synchronized boolean startOrResume() {
        if (saveManager.getSaveFile().exists() || saveManager.getBackupFile().exists()) {
            boolean loaded = load();
            recoveryRequired = !loaded;
            return loaded;
        }
        initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        return true;
    }
    private GameClock clock;
    private final Map<String, Account> accounts = new HashMap<>();
    private String personalAccountId;
    private final EconomyLedger ledger = new EconomyLedger();
    private final SaveManager saveManager;
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
        this.saveManager = Objects.requireNonNull(saveManager);
    }

    public synchronized PlayerProgressManager getProgressManager() {
        return progressManager;
    }

    public synchronized boolean recordResearchRead(String articleId) {
        if (articleId == null) return false;
        for (com.tradingglobal3d.core.research.NewsArticle article : getNewsFeed()) {
            if (article.getId().equals(articleId))
                return progressManager.onResearchRead(articleId, clock.getGameTimestamp());
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
                System.currentTimeMillis(),
                "Setoran modal awal profil baru"
        );

        // 3. Buat dan Danai Agen NPC Market Maker (Likuiditas Awal)
        String mmAccountId = "ACC-NPC-MM-01";
        Account mmAccount = new Account(mmAccountId, "Market Maker Utama", AccountType.FIRM, null);
        accounts.put(mmAccountId, mmAccount);
        Money mmCashIdr = Money.of(5_000_000_000L, Currency.IDR);
        Money mmCashUsd = Money.of(500_000.0, Currency.USD);
        ledger.recordMutation("INIT-MM-IDR", mmAccount, LedgerEntryType.INITIAL_CAPITAL, mmCashIdr, true, System.currentTimeMillis(), "Modal kas IDR Market Maker");
        ledger.recordMutation("INIT-MM-USD", mmAccount, LedgerEntryType.INITIAL_CAPITAL, mmCashUsd, true, System.currentTimeMillis(), "Modal kas USD Market Maker");

        // Berikan kepemilikan saham awal untuk Market Maker agar bisa memasang antrean Ask
        for (Instrument inst : catalog.getActiveUniverse()) {
            long initialShares = inst.getLotSize() * 200L;
            Position p = new Position(inst.getTicker(), inst.getExchange().name(), inst.getCurrency(),
                    initialShares, inst.getCurrentPrice().getAmount(), inst.getCurrentPrice().getAmount());
            mmAccount.addPosition(p);
        }

        NpcAgent marketMaker = new NpcAgent("NPC-MM-01", "Bandar Likuiditas Prima", NpcStrategyType.MARKET_MAKER, mmAccount);
        marketEngine.registerNpcAgent(marketMaker);
        careerManager.beginDay(personal, fxService.getMidRate(), ledger);
        recoveryRequired = false;
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
        // All execution paths (resting orders, NPC matches and triggers) converge here.
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
        return researchService.getCalendarEvents();
    }

    public synchronized boolean subscribeTerminal(TerminalTier tier) {
        Account personal = getPersonalAccount();
        if (personal == null) return false;
        return researchService.subscribeTerminal(personal, tier, ledger, clock.getGameTimestamp());
    }

    public synchronized FundAllocationPlan saveFundPlan(String planId, String accountId, String name, Map<String, Double> targetWeights, double cashWeight) {
        Account acc = accounts.get(accountId);
        if (acc == null) throw new IllegalArgumentException("Akun tidak ditemukan: " + accountId);

        ClientMandate mandate = (acc.getType() == AccountType.CLIENT && acc.getClientMandateId() != null)
                ? firmManager.getMandate(acc.getClientMandateId())
                : null;

        return fundBuilder.saveAllocationPlan(planId, accountId, name, targetWeights, cashWeight, mandate);
    }

    public synchronized List<MatchingEngine.OrderResult> executeFundRebalance(String planId) {
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
        Account personal = getPersonalAccount();
        riskService.takeMarginLoan(personal, amount, ledger, System.currentTimeMillis());
    }

    public synchronized void repayMarginLoan(Money amount) {
        Account personal = getPersonalAccount();
        riskService.repayMarginLoan(personal, amount, ledger, System.currentTimeMillis());
    }

    public synchronized ShortPosition openShortPosition(String ticker, long shares) {
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
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Akun personal belum dibuat");
        return careerManager.claimMissionReward(missionId, personal, ledger, System.currentTimeMillis());
    }

    public synchronized CareerRank evaluateCareerPromotion() {
        Account personal = getPersonalAccount();
        if (personal == null) return careerManager.getCurrentRank();
        return careerManager.evaluatePromotion(personal, fxService.getMidRate());
    }

    public synchronized Money requestBailoutRecovery() {
        Account personal = getPersonalAccount();
        if (personal == null) throw new IllegalStateException("Akun personal belum dibuat");
        return careerManager.requestBailout(personal, ledger, System.currentTimeMillis());
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

        DailySessionReport report = careerManager.closeDayAndAdvance(
                clock, personal, catalog, ledger, fxService.getMidRate(), now
        );

        // Automatic calendar advancement must retain the time of day.
        for (ClientMandate mandate : firmManager.getActiveMandates()) collectClientFees(mandate.getId());
        if (!firmManager.getActiveMandates().isEmpty() && firmManager.getActiveMandates().stream().allMatch(mandate ->
                firmManager.isWithinLimits(mandate, accounts.get(mandate.getClientAccountId()), fxService.getMidRate())))
            progressManager.onCleanSessionClosed(now);
        if (!advanceToMorning) return report;
        LocalDate nextDate = clock.getCurrentDate().plusDays(1);
        this.clock = new GameClock(nextDate, java.time.LocalTime.of(8, 30, 0));
        processCalendarActions(nextDate);
        return report;
    }

    private void processCalendarActions(LocalDate nextDate) {
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
        if (clock == null || clock.isPaused() || realSeconds <= 0) return false;
        try {
            return advanceWorldSeconds(Math.multiplyExact((long) realSeconds,
                    GAME_SECONDS_PER_REAL_SECOND * clock.getSpeedMultiplier()));
        } catch (RuntimeException error) {
            clock.pause();
            throw error;
        }
    }

    private boolean advanceWorldSeconds(long remaining) {
        // ponytail: minute steps suit the current date-based events; use event timestamps if sub-minute events are introduced.
        LocalDate initialDate = clock.getCurrentDate();
        while (remaining > 0) {
            long midnight = 86400 - clock.getCurrentTime().toSecondOfDay();
            long step = Math.min(remaining, Math.min(60, midnight));
            if (step == midnight) {
                clock.advanceGameSeconds(step - 1);
                settleDay(false);
                clock.advanceGameSeconds(1);
                processCalendarActions(clock.getCurrentDate());
            } else clock.advanceGameSeconds(step);
            remaining -= step;
            if (clock.getCurrentMarketSession().isOpenForTrading()) generateNpcLiquidity();
            evaluateTriggers();
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
        SaveState state = SaveState.capture(gameMode, clock, accounts.values(), ledger);
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
        runtime.put("playerProgress", progressManager.exportState());
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
        } catch (Exception invalidState) {
            return false;
        }
        this.gameMode = state.getGameMode();
        this.clock = new GameClock(state.getGameDate(), state.getGameTime());
        this.clock.setSpeedMultiplier(state.getSpeedMultiplier());

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
            if (runtime.has("playerProgress")) progressManager.restoreState(runtime.optJSONObject("playerProgress"));
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

            BigDecimal usdRate = fxService != null ? fxService.getMidRate() : BigDecimal.valueOf(15500);
            Money netWorth = (riskService != null && catalog != null)
                    ? riskService.evaluateMarginHealth(personal, catalog, usdRate).getEquity()
                    : personal.getTotalPortfolioValue(Currency.IDR, usdRate);
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
        root.put("playerProgress", progressManager.toSnapshotJson());
        root.put("accountCount", accounts.size());
        root.put("ledgerEntryCount", ledger.getEntries().size());
        return root.toString();
    }

    public synchronized JSONObject getChartData(String instrumentId, String timeframe) throws JSONException {
        Instrument inst = catalog.getById(instrumentId);
        if (inst == null) {
            return new JSONObject().put("success", false).put("message", "Instrumen tidak ditemukan: " + instrumentId);
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
}
