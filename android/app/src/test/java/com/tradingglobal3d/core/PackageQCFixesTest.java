package com.tradingglobal3d.core;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.clock.MarketSession;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.funds.MutualFund;
import com.tradingglobal3d.core.funds.MutualFundManager;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.research.MarketEvent;
import com.tradingglobal3d.core.research.NewsArticle;
import com.tradingglobal3d.core.research.NewsPriceTarget;
import com.tradingglobal3d.core.research.ResearchBooster;
import com.tradingglobal3d.core.research.ResearchService;
import com.tradingglobal3d.core.research.TerminalTier;
import com.tradingglobal3d.core.risk.RiskComplianceService;
import com.tradingglobal3d.core.risk.ShortPosition;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.MatchingEngine;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderBook;
import com.tradingglobal3d.core.trading.OrderSide;
import com.tradingglobal3d.core.trading.OrderStatus;
import com.tradingglobal3d.core.trading.OrderType;
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

public class PackageQCFixesTest {

    @Test(timeout = 3000)
    public void testQC01_BoosterDepletedRestoresInstantlyWithoutInfiniteLoop() throws Exception {
        ResearchService rs = new ResearchService();
        rs.resetForGame(1704067200000L);

        // Find Fee Discount booster
        ResearchBooster feeBooster = null;
        for (ResearchBooster b : rs.getBoosters()) {
            if (b.getType() == ResearchBooster.BoosterType.FEE_DISCOUNT) {
                feeBooster = b;
                break;
            }
        }
        Assert.assertNotNull(feeBooster);
        feeBooster.activate();

        // Deplete all 15 uses
        for (int i = 0; i < 15; i++) {
            feeBooster.consumeUse();
        }
        Assert.assertEquals(0, feeBooster.getRemainingUses());
        Assert.assertFalse(feeBooster.isActive());

        // Export state (booster is inactive with remainingUses = 0)
        JSONObject state = rs.exportState();

        // Restore into new service
        ResearchService rs2 = new ResearchService();
        // QC01: This restoreState MUST complete without entering an infinite loop!
        rs2.restoreState(state);

        ResearchBooster restoredBooster = null;
        for (ResearchBooster b : rs2.getBoosters()) {
            if (b.getType() == ResearchBooster.BoosterType.FEE_DISCOUNT) {
                restoredBooster = b;
                break;
            }
        }
        Assert.assertNotNull(restoredBooster);
        Assert.assertEquals("Restored booster must maintain remainingUses = 0", 0, restoredBooster.getRemainingUses());
        Assert.assertFalse("Depleted booster must not become active", restoredBooster.isActive());
    }

    @Test
    public void testQC02_NewsCatalystRespectsExchangeSessionAndHalt() throws Exception {
        File testDir = new File("build/tmp-test-qc02");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));

        // Start game at 02:00 WIB (night: IDX is closed, US is open)
        GameClock nightClock = new GameClock(LocalDate.of(2026, 11, 2), LocalTime.of(2, 0, 0));
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"), nightClock);

        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        Assert.assertNotNull(bbca);
        Money bbcaPrice0 = bbca.getCurrentPrice();

        // Register active target for IDX stock (BBCA +5%)
        NewsPriceTarget targetIdx = new NewsPriceTarget("TGT-BBCA", "NEWS-BBCA", "BBCA", TerminalTier.ENTERPRISE,
                "BBCA Catalyst", false, 0.05, bbcaPrice0, bbca, nightClock.getCurrentDate());
        facade.getResearchService().registerNewsTarget(targetIdx);

        // Tick at 02:00 WIB
        facade.getMarketEngine().tick(nightClock, nightClock.getGameTimestamp(), facade.getCatalog(),
                facade.getLedger(), facade.getAccountsMap(), facade.getResearchService());

        // QC02: BBCA must NOT execute catalyst fill at 02:00 WIB because IDX is closed!
        Assert.assertEquals("IDX catalyst must NOT execute when IDX is closed at 02:00 WIB",
                bbcaPrice0, bbca.getCurrentPrice());

        // Fast forward to US close at 04:00 WIB (2 hours = 7200s)
        facade.advanceWorldSecondsForTest(7200);

        // QC02: At 04:00 WIB (US market close), only US instruments can be processed; IDX must remain unchanged!
        Assert.assertEquals("IDX catalyst must NOT execute during US market close at 04:00 WIB",
                bbcaPrice0, bbca.getCurrentPrice());
    }

    @Test
    public void testQC03_ShortUnpaidBorrowFeePreservedAndTrackedUntilPaid() throws Exception {
        RiskComplianceService risk = new RiskComplianceService();
        Account borrower = new Account("BORROWER", "Borrower", AccountType.PERSONAL, null);
        Account lender = new Account("LENDER", "Lender", AccountType.FIRM, null);
        EconomyLedger ledger = new EconomyLedger();
        Instrument inst = new Instrument("IDX:BBCA", "BBCA", "BBCA", MarketExchange.IDX, "Finance", Currency.IDR, 100, 1000000L, true, Money.of(10000L, Currency.IDR));
        InstrumentCatalog cat = new InstrumentCatalog();
        cat.registerInstrument(inst);

        lender.addPosition(new Position("BBCA", "IDX", Currency.IDR, 50000L, BigDecimal.valueOf(10000), BigDecimal.valueOf(10000)));

        // Open short 100 shares @ 10,000 -> proceeds locked, borrower cash = 0
        ShortPosition sp = risk.openShortPosition(borrower, inst, 100L, lender, ledger, 1000L);

        // Day 1: Accrue financing cost when cash is 0 -> unpaid fee accumulates
        risk.accrueDailyFinancingCosts(borrower, cat, ledger, BigDecimal.valueOf(15800), 2000L);
        Money unpaid1 = sp.getAccruedUnpaidFee();
        Assert.assertTrue("Unpaid borrow fee must accumulate", unpaid1.isPositive());

        // Calculate liability: MUST include unpaid fee
        Money liabWithUnpaid = sp.calculateLiability(BigDecimal.valueOf(10000));
        Assert.assertTrue("Liability must include unpaid borrow fee", liabWithUnpaid.compareTo(Money.of(1000000L, Currency.IDR)) > 0);

        // Shortfall to cover 100 shares @ 10,000 is 1,001,500 total cost - 997,500 released collateral = 4,000 IDR.
        // Credit exactly 4,000 IDR so shares can be covered, while leaving the ~69 IDR unpaid borrow fee.
        borrower.creditCash(Money.of(4000L, Currency.IDR));
        risk.closeShortPosition(borrower, inst, 100L, lender, ledger, 3000L);

        // QC03: When shares reach 0 but unpaid fee > 0, short position MUST NOT be deleted!
        ShortPosition spRemaining = risk.getShortPosition("BORROWER", "BBCA");
        Assert.assertNotNull("Short position with unpaid fee must NOT be deleted upon covering shares", spRemaining);
        Assert.assertEquals("Borrowed shares should be 0", 0L, spRemaining.getBorrowedShares());
        Assert.assertEquals("Unpaid fee must remain intact", unpaid1, spRemaining.getAccruedUnpaidFee());

        // Now borrower gets cash and next day accrual runs -> pays down arrears!
        borrower.creditCash(Money.of(100000L, Currency.IDR));
        risk.accrueDailyFinancingCosts(borrower, cat, ledger, BigDecimal.valueOf(15800), 4000L);

        // Arrears should be paid down
        Assert.assertEquals("Arrears must be settled when cash is available", Money.zero(Currency.IDR), spRemaining.getAccruedUnpaidFee());
    }

    @Test
    public void testQC04_FeeBoosterEliminatesBrokerCommissionOnFillOnly() throws Exception {
        File testDir = new File("build/tmp-test-qc04");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        ResearchService rs = facade.getResearchService();
        ResearchBooster booster = null;
        for (ResearchBooster b : rs.getBoosters()) {
            if (b.getType() == ResearchBooster.BoosterType.FEE_DISCOUNT) {
                booster = b;
                break;
            }
        }
        Assert.assertNotNull(booster);
        booster.activate();
        int initialUses = booster.getRemainingUses();

        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");

        // 1. Submit a limit order deep below market price (e.g. 5000 vs 10000) -> enters book as OPEN (0 fills)
        Order queuedOrder = new Order("ORD-QUEUED-1", "REQ-Q1", facade.getPersonalAccount().getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT, Money.of(5000L, Currency.IDR), null, 100L,
                facade.getClock().getGameTimestamp(), Money.of(500000L, Currency.IDR));
        facade.submitOrder(queuedOrder);

        // QC04: Queued order with 0 fills must NOT consume booster uses!
        Assert.assertEquals("Queued order with 0 fills must NOT consume booster uses!",
                initialUses, booster.getRemainingUses());

        // Cancel order: still must not consume uses
        facade.cancelOrder(queuedOrder);
        Assert.assertEquals(initialUses, booster.getRemainingUses());

        // 2. Advance to market hours (09:05) and execute an order that fills immediately
        facade.advanceWorldSecondsForTest(35 * 60);

        // Generate market liquidity
        facade.generateNpcLiquidity();
        OrderBook book = facade.getMarketEngine().getOrCreateBook(bbca);
        Assert.assertFalse("Book must have asks after liquidity generation", book.getAsks().isEmpty());
        BigDecimal lowestAsk = book.getAsks().keySet().iterator().next();

        Order fillOrder = new Order("ORD-FILL-1", "REQ-F1", facade.getPersonalAccount().getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT, new Money(lowestAsk, Currency.IDR), null, 100L,
                facade.getClock().getGameTimestamp(), Money.of(2000000L, Currency.IDR));
        MatchingEngine.OrderResult res = facade.submitOrder(fillOrder);
        Assert.assertTrue(res.isSuccess);
        Assert.assertFalse(res.fills.isEmpty());

        // QC04: When filled, exactly 1 use is consumed
        Assert.assertEquals("Filled order must consume exactly 1 booster use", initialUses - 1, booster.getRemainingUses());

        // Check fee: On BUY with 100% broker discount, fee must be 0!
        Money buyFee = res.fills.get(0).getBuyerFee();
        Assert.assertEquals("Buy fee with fee discount booster must be 0!", Money.zero(Currency.IDR), buyFee);
    }

    @Test
    public void testQC05_WhaleNewsRequiresRealQualifyingEconomicEvent() {
        ResearchService rs = new ResearchService();
        rs.resetForGame(1704067200000L);
        rs.setDynamicDailyNewsEnabled(true);
        InstrumentCatalog cat = new InstrumentCatalog();
        Instrument inst = new Instrument("IDX:BBCA", "BBCA", "Bank BCA", MarketExchange.IDX, "Finance", Currency.IDR, 100, 1000000L, true, Money.of(10000L, Currency.IDR));
        cat.registerInstrument(inst);

        LocalDate date = LocalDate.of(2026, 11, 2);
        long timestamp = 1793616000000L;

        // Process day 1 schedule (consumes initial default event EVT-02)
        rs.ensureDailyNewsSchedule(date, cat, timestamp);

        // Day 2 schedule without any new events
        LocalDate day2 = date.plusDays(1);
        int newsCountBefore = rs.getNewsList().size();
        rs.ensureDailyNewsSchedule(day2, cat, timestamp + 86400000L);

        // QC05: Without any new qualifying MarketEvent, NO new Whale news must be generated on day 2!
        boolean hasNewWhaleOnDay2 = false;
        for (NewsArticle a : rs.getNewsList()) {
            if (a.getId().startsWith("NEWS-WHALE-") && a.getPublishedAt() == (timestamp + 86400000L)) {
                hasNewWhaleOnDay2 = true;
            }
        }
        Assert.assertFalse("Without a new qualifying MarketEvent, no new Whale news must be generated!", hasNewWhaleOnDay2);

        // Now register a real economic event
        MarketEvent realEvent = new MarketEvent(
                "EVT-REAL-01", "Mega Merger Finansial Nusantara",
                "Konsorsium perbankan menyelesaikan akuisisi mayoritas",
                "Finance", MarketEvent.EventImpact.VERY_POSITIVE, timestamp + 2 * 86400000L, true
        );
        rs.addMarketEvent(realEvent);

        // Run schedule for day 3
        LocalDate day3 = date.plusDays(2);
        rs.ensureDailyNewsSchedule(day3, cat, timestamp + 2 * 86400000L);

        NewsArticle whaleNews = null;
        for (NewsArticle a : rs.getNewsList()) {
            if (a.getId().startsWith("NEWS-WHALE-")) whaleNews = a;
        }
        Assert.assertNotNull("Whale news must be generated when a real qualifying event exists!", whaleNews);
        Assert.assertEquals("Whale news must link to the real event ID", "EVT-REAL-01", whaleNews.getEconomicEventId());
    }

    @Test
    public void testQC06_MutualFundAccruesManagementFeeUsingCanonicalCatalogRate() throws Exception {
        MutualFundManager mfm = new MutualFundManager();
        MutualFund sh01 = mfm.getFund("FUND-SH-01");
        Assert.assertNotNull(sh01);
        Assert.assertEquals(new BigDecimal("0.015000"), sh01.getManagementFeeRate());

        MutualFund mm02 = mfm.getFund("FUND-MM-02");
        Assert.assertNotNull(mm02);
        Assert.assertEquals(new BigDecimal("0.003500"), mm02.getManagementFeeRate());

        java.util.Map<String, Account> accounts = new java.util.HashMap<>();
        EconomyLedger ledger = new EconomyLedger();
        long t1 = LocalDate.of(2026, 11, 2).atTime(16, 0).toInstant(java.time.ZoneOffset.ofHours(7)).toEpochMilli();
        mfm.registerPoolAccounts(accounts, ledger, t1);
        mfm.bindAccounts(accounts, new InstrumentCatalog());

        // Fund pools have cash
        Account poolSh01 = accounts.get(sh01.getPoolAccountId());
        Assert.assertNotNull(poolSh01);
        poolSh01.creditCash(Money.of(100_000_000L, Currency.IDR));

        Account poolMm02 = accounts.get(mm02.getPoolAccountId());
        Assert.assertNotNull(poolMm02);
        poolMm02.creditCash(Money.of(100_000_000L, Currency.IDR));

        // Cutoff day 1 (anchor day)
        mfm.processCutoff(accounts, ledger, t1);

        // Cutoff day 2 (1 day later)
        long t2 = LocalDate.of(2026, 11, 3).atTime(16, 0).toInstant(java.time.ZoneOffset.ofHours(7)).toEpochMilli();
        mfm.processCutoff(accounts, ledger, t2);

        // QC06: Fee paid on SH-01 (1.50% p.a.) must be substantially higher than MM-02 (0.35% p.a.)
        Money feeSh01 = sh01.getManagementFeesPaid();
        Money feeMm02 = mm02.getManagementFeesPaid();
        System.out.println("QC06 fee SH01 (150bps): " + feeSh01 + ", MM02 (35bps): " + feeMm02);
        Assert.assertTrue(feeSh01.compareTo(feeMm02) > 0);
        Assert.assertTrue("Fee for 150bps fund must be > 3500 IDR", feeSh01.getAmount().compareTo(new BigDecimal("3500")) > 0);
        Assert.assertTrue("Fee for 35bps fund must be < 1500 IDR", feeMm02.getAmount().compareTo(new BigDecimal("1500")) < 0);
    }
}
