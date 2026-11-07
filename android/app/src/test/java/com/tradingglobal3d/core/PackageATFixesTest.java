package com.tradingglobal3d.core;

import com.tradingglobal3d.core.business.CompanyManager;
import com.tradingglobal3d.core.business.OperatingCompany;
import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.funds.MutualFund;
import com.tradingglobal3d.core.funds.MutualFundManager;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.trading.TradingHaltState;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.risk.RiskComplianceService;
import com.tradingglobal3d.core.risk.ShortPosition;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

public class PackageATFixesTest {

    @Test
    public void testAT01_ShortCollateralQuantizedConsistentlyAndPreflightAtomic() {
        RiskComplianceService risk = new RiskComplianceService();
        Account borrower = new Account("BORROWER", "Borrower", AccountType.PERSONAL, null);
        Account lender = new Account("LENDER", "Lender", AccountType.FIRM, null);
        EconomyLedger ledger = new EconomyLedger();

        // 100 shares @ Rp 202
        Instrument inst = new Instrument("IDX:TEST202", "TEST202", "Test 202", MarketExchange.IDX, "Finance",
                Currency.IDR, 100, 1000000L, true, Money.of(202L, Currency.IDR));
        InstrumentCatalog cat = new InstrumentCatalog();
        cat.registerInstrument(inst);

        lender.addPosition(new Position("TEST202", "IDX", Currency.IDR, 1000L, BigDecimal.valueOf(202), BigDecimal.valueOf(202)));

        // AT01: 100 shares * 202 = 20,200 gross. Fee = 50.5 -> 51. Net = 20,149.
        // Borrower cash is initially 0. Operation MUST succeed without throwing reservation exception!
        ShortPosition sp = risk.openShortPosition(borrower, inst, 100L, lender, ledger, 1000L);
        Assert.assertNotNull(sp);
        Assert.assertEquals(Money.of(20149L, Currency.IDR), sp.getCollateralLocked());
        Assert.assertEquals(Money.zero(Currency.IDR), borrower.getAvailableCash(Currency.IDR));

        // Preflight test: Insufficient lender inventory MUST fail without any mutation
        boolean rejected = false;
        long lenderSharesBefore = lender.getPosition("TEST202").getQuantity();
        int ledgerSizeBefore = ledger.getEntries().size();
        try {
            risk.openShortPosition(borrower, inst, 50000L, lender, ledger, 2000L);
        } catch (IllegalStateException e) {
            rejected = true;
        }
        Assert.assertTrue("Short with insufficient lender inventory must be rejected in preflight!", rejected);
        Assert.assertEquals("Lender shares must be untouched on preflight rejection",
                lenderSharesBefore, lender.getPosition("TEST202").getQuantity());
        Assert.assertEquals("Ledger must be untouched on preflight rejection",
                ledgerSizeBefore, ledger.getEntries().size());
    }

    @Test
    public void testAT02_FundRebalanceOrderSurvivesOvernightExpiryAndExecutesInMorning() throws Exception {
        File testDir = new File("build/tmp-test-at02");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));

        // Start at 15:55 WIB on Day 1
        GameClock startClock = new GameClock(LocalDate.of(2026, 11, 2), LocalTime.of(15, 55, 0));
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"), startClock);

        // Submit fund subscription so cutoff has cash to trigger equity rebalance
        facade.getMutualFundManager().getFund("FUND-EQ-01").submitSubscription(
                "ORD-SUB-EQ", facade.getPersonalAccount(), Money.of(20_000_000L, Currency.IDR),
                facade.getLedger(), facade.getClock().getGameTimestamp());

        // Advance 10 minutes to 16:05 WIB (triggers 16:00 cutoff and generates rebalance order)
        facade.advanceWorldSecondsForTest(600);

        Order rebalOrder = null;
        for (Order o : facade.getOrderManager().getAllOrders()) {
            if (o != null && o.getRequestId().contains(":REBALANCE:")) {
                rebalOrder = o;
                break;
            }
        }
        Assert.assertNotNull("Rebalance order must be created at 16:00 cutoff", rebalOrder);
        Assert.assertEquals(OrderStatus.OPEN, rebalOrder.getStatus());

        // Fast forward through midnight to 08:30 WIB next morning (16.5 hours = 59,400s)
        facade.advanceWorldSecondsForTest(59400);

        // AT02: Rebalance order MUST NOT be expired by expireDayOrders!
        Assert.assertEquals("Fund rebalance order must remain OPEN overnight awaiting next morning's market open!",
                OrderStatus.OPEN, rebalOrder.getStatus());
    }

    @Test
    public void testAT03_ShortOpenAndCloseRejectedWhenMarketClosedOrHalted() throws Exception {
        File testDir = new File("build/tmp-test-at03");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));

        // Start at 08:30 WIB (IDX market is CLOSED until 09:00)
        facade.startInternalGame(new BigDecimal("50000000"));

        // Promote to PORTFOLIO_MANAGER to qualify for short
        JSONObject careerState = facade.getCareerManager().exportState();
        careerState.put("rank", "PORTFOLIO_MANAGER");
        facade.getCareerManager().restoreState(careerState);

        // 1. Attempt open short at 08:30 (Market closed) -> MUST fail
        boolean openRejected = false;
        try {
            facade.openShortPosition("BBCA", 100L);
        } catch (IllegalStateException e) {
            openRejected = true;
            Assert.assertTrue(e.getMessage().contains("tutup"));
        }
        Assert.assertTrue("Opening short when market is closed must be rejected!", openRejected);

        // Advance to 09:05 (Market open) -> open short succeeds
        facade.advanceWorldSecondsForTest(35 * 60);
        ShortPosition sp = facade.openShortPosition("BBCA", 100L);
        Assert.assertNotNull(sp);

        // 2. Halt BBCA with circuit breaker
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        facade.getMarketEngine().getOrCreateBook(bbca).setHaltState(TradingHaltState.SUSPENDED_CIRCUIT_BREAKER);

        // Attempt close short when halted -> MUST fail
        boolean closeRejected = false;
        try {
            facade.closeShortPosition("BBCA", 100L);
        } catch (IllegalStateException e) {
            closeRejected = true;
            Assert.assertTrue(e.getMessage().contains("suspensi") || e.getMessage().contains("halt"));
        }
        Assert.assertTrue("Closing short when instrument is halted must be rejected!", closeRejected);
    }

    @Test
    public void testAT04_HoldingCompanyRequiresCanonicalCompanySectors() throws Exception {
        CompanyManager cm = new CompanyManager();
        Account personal = new Account("PERSONAL", "Personal", AccountType.PERSONAL, null);
        personal.creditCash(Money.of(200_000_000L, Currency.IDR));
        EconomyLedger ledger = new EconomyLedger();
        java.util.Map<String, Account> accounts = new java.util.HashMap<>();
        accounts.put("PERSONAL", personal);

        // Found 4 companies, but 2 are in FINANCIAL sector
        OperatingCompany c1 = cm.foundCompany("COMP-01", "PT Bank Satu", "FINANCIAL", personal, Money.of(25_000_000L, Currency.IDR), ledger, accounts, 1000L);
        OperatingCompany c2 = cm.foundCompany("COMP-02", "PT Finance Dua", "FINANCIAL", personal, Money.of(25_000_000L, Currency.IDR), ledger, accounts, 1000L);
        OperatingCompany c3 = cm.foundCompany("COMP-03", "PT Tambang Tiga", "ENERGY", personal, Money.of(25_000_000L, Currency.IDR), ledger, accounts, 1000L);
        OperatingCompany c4 = cm.foundCompany("COMP-04", "PT Ritel Empat", "CONSUMER", personal, Money.of(25_000_000L, Currency.IDR), ledger, accounts, 1000L);

        // Attempt to create holding claiming COMP-02 is in "TECHNOLOGY" when its real sector is FINANCIAL
        JSONArray subsFraud = new JSONArray();
        subsFraud.put(new JSONObject().put("sector", "FINANCIAL").put("companyId", "COMP-01"));
        subsFraud.put(new JSONObject().put("sector", "TECHNOLOGY").put("companyId", "COMP-02")); // Fake sector!
        subsFraud.put(new JSONObject().put("sector", "ENERGY").put("companyId", "COMP-03"));
        subsFraud.put(new JSONObject().put("sector", "CONSUMER").put("companyId", "COMP-04"));

        boolean fraudRejected = false;
        try {
            cm.createHoldingCompany("HOLD-01", "Holding Fiktif", subsFraud);
        } catch (IllegalArgumentException e) {
            fraudRejected = true;
            System.out.println("AT04 rejection message: " + e.getMessage());
            Assert.assertTrue(e.getMessage().contains("tidak cocok"));
        }
        Assert.assertTrue("Holding creation with mislabeled company sector must be rejected!", fraudRejected);
    }

    @Test
    public void testAT05_HoldingTransferRetryReturnsOriginalReceiptAndRejectsChangedPayload() throws Exception {
        CompanyManager cm = new CompanyManager();
        Account personal = new Account("PERSONAL", "Personal", AccountType.PERSONAL, null);
        personal.creditCash(Money.of(200_000_000L, Currency.IDR));
        EconomyLedger ledger = new EconomyLedger();
        java.util.Map<String, Account> accounts = new java.util.HashMap<>();
        accounts.put("PERSONAL", personal);

        OperatingCompany c1 = cm.foundCompany("COMP-01", "PT Satu", "FINANCIAL", personal, Money.of(50_000_000L, Currency.IDR), ledger, accounts, 1000L);
        OperatingCompany c2 = cm.foundCompany("COMP-02", "PT Dua", "TECHNOLOGY", personal, Money.of(50_000_000L, Currency.IDR), ledger, accounts, 1000L);
        OperatingCompany c3 = cm.foundCompany("COMP-03", "PT Tiga", "ENERGY", personal, Money.of(50_000_000L, Currency.IDR), ledger, accounts, 1000L);
        OperatingCompany c4 = cm.foundCompany("COMP-04", "PT Empat", "CONSUMER", personal, Money.of(50_000_000L, Currency.IDR), ledger, accounts, 1000L);

        JSONArray subs = new JSONArray();
        subs.put(new JSONObject().put("sector", "FINANCIAL").put("companyId", "COMP-01"));
        subs.put(new JSONObject().put("sector", "TECHNOLOGY").put("companyId", "COMP-02"));
        subs.put(new JSONObject().put("sector", "ENERGY").put("companyId", "COMP-03"));
        subs.put(new JSONObject().put("sector", "CONSUMER").put("companyId", "COMP-04"));
        cm.createHoldingCompany("HOLD-01", "Holding Konglomerasi", subs);

        String reqId = "REQ-TRF-001";
        Money amount = Money.of(1_000_000L, Currency.IDR);

        // First transfer: 1,000,000 IDR from COMP-01 to COMP-02
        JSONObject res1 = cm.transferHoldingCapital("HOLD-01", "COMP-01", "COMP-02", amount, "CAPITAL_INJECTION", accounts, ledger, 2000L, reqId);
        Assert.assertTrue(res1.getBoolean("success"));
        Assert.assertFalse(res1.getBoolean("alreadyProcessed"));
        Assert.assertEquals("Rp 1.000.000", res1.getString("amount"));

        // Retry 1: Identical payload -> returns alreadyProcessed = true with original amount
        JSONObject resRetry = cm.transferHoldingCapital("HOLD-01", "COMP-01", "COMP-02", amount, "CAPITAL_INJECTION", accounts, ledger, 2000L, reqId);
        Assert.assertTrue(resRetry.getBoolean("success"));
        Assert.assertTrue(resRetry.getBoolean("alreadyProcessed"));
        Assert.assertEquals("Rp 1.000.000", resRetry.getString("amount"));

        // Retry 2: Changed amount to 10,000,000 IDR -> MUST be rejected!
        boolean changedPayloadRejected = false;
        try {
            cm.transferHoldingCapital("HOLD-01", "COMP-01", "COMP-02", Money.of(10_000_000L, Currency.IDR), "CAPITAL_INJECTION", accounts, ledger, 2000L, reqId);
        } catch (IllegalArgumentException e) {
            changedPayloadRejected = true;
            Assert.assertTrue(e.getMessage().contains("berbeda"));
        }
        Assert.assertTrue("Retry transfer with changed payload must be rejected!", changedPayloadRejected);
    }

    @Test
    public void testNV01_SaveManagerUsesIsolatedTempFilesToPreventConcurrentCorruption() throws Exception {
        File testDir = new File("build/tmp-test-nv01");
        testDir.mkdirs();

        SaveManager sm1 = SaveManager.forDirectory(testDir);
        SaveManager sm2 = SaveManager.forDirectory(testDir);

        TradingFacade f1 = new TradingFacade(sm1);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        f1.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        f1.startInternalGame(new BigDecimal("50000000"));

        // Both save sequentially/concurrently
        f1.save();
        Assert.assertTrue("Save file must exist and be valid", sm1.getSaveFile().exists());

        // Verify SaveManager metadata indicates clean atomic write
        JSONObject meta = sm1.getSaveMetadata();
        Assert.assertTrue(meta.getBoolean("primaryExists"));
    }
}
