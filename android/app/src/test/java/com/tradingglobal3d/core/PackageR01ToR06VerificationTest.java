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
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.research.MarketEvent;
import com.tradingglobal3d.core.research.NewsArticle;
import com.tradingglobal3d.core.research.ResearchBooster;
import com.tradingglobal3d.core.research.ResearchService;
import com.tradingglobal3d.core.risk.RiskComplianceService;
import com.tradingglobal3d.core.risk.ShortPosition;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.storage.SaveState;
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
import java.util.List;

public class PackageR01ToR06VerificationTest {

    @Test
    public void testR01_BoosterQuotaPreservedOnRestingOrderUntilFilledByNPCAndSingleConsumptionPerOrder() throws Exception {
        File testDir = new File("build/tmp-test-r01");
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
        Assert.assertEquals(15, initialUses);

        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");

        // 1. Submit resting buy order (price 5000 is far below 10000 -> 0 fills)
        Order restingBuy = new Order("ORD-RESTING-1", "REQ-REST-1", facade.getPersonalAccount().getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT, Money.of(5000L, Currency.IDR), null, 100L,
                facade.getClock().getGameTimestamp(), Money.of(500000L, Currency.IDR));
        MatchingEngine.OrderResult resPlace = facade.submitOrder(restingBuy);
        Assert.assertTrue(resPlace.isSuccess);
        Assert.assertTrue(resPlace.fills.isEmpty());

        // Quota MUST NOT be consumed while resting in the book!
        Assert.assertEquals("Resting order in book must NOT consume booster uses!",
                initialUses, booster.getRemainingUses());

        // 2. Advance to market hours (09:05 WIB)
        facade.advanceWorldSecondsForTest(35 * 60);

        // Player places a SELL limit order at 10,250 (inside spread, lower than MM ask)
        OrderBook book = facade.getMarketEngine().getOrCreateBook(bbca);
        BigDecimal bestBid = book.getBids().keySet().iterator().next();
        BigDecimal playerPrice = bestBid.add(BigDecimal.valueOf(25));
        Account personal = facade.getPersonalAccount();
        personal.addPosition(new Position(bbca.getTicker(), bbca.getExchange().name(), bbca.getCurrency(),
                1000L, playerPrice, playerPrice));

        // Clear existing asks so player is strictly the best ask
        book.getAsks().clear();

        Order playerSell = new Order("ORD-PLAYER-SELL", "REQ-P-SELL", personal.getId(), bbca.getId(),
                OrderSide.SELL, OrderType.LIMIT, new Money(playerPrice, Currency.IDR), null, 100L,
                facade.getClock().getGameTimestamp(), Money.zero(Currency.IDR));
        facade.submitOrder(playerSell);
        Assert.assertEquals("Player maker sell order in book must not consume booster uses", initialUses, booster.getRemainingUses());

        // 3. NPC submits buy order matching player's resting maker order!
        Account npcBuyer = facade.getAccountsMap().get("ACC-NPC-RETAIL-01");
        npcBuyer.creditCash(Money.of(10_000_000L, Currency.IDR));

        Order npcBuy = new Order("ORD-NPC-BUY", "REQ-NPC-BUY", npcBuyer.getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT, new Money(playerPrice, Currency.IDR), null, 100L,
                facade.getClock().getGameTimestamp(), Money.of(2000000L, Currency.IDR));

        // Submit NPC order -> matches against player's playerSell!
        MatchingEngine.OrderResult matchRes = facade.submitOrder(npcBuy);
        Assert.assertTrue(matchRes.isSuccess);
        Assert.assertEquals(1, matchRes.fills.size());

        // R01 Verification: Player was MAKER, filled by NPC -> booster was applied and exactly 1 use consumed!
        Assert.assertEquals("When player maker order is filled by NPC, exactly 1 booster use must be consumed!",
                initialUses - 1, booster.getRemainingUses());
        Assert.assertTrue("Maker order must have boosterApplied marked true", playerSell.isBoosterApplied());
        Assert.assertEquals("Player seller fee under booster must only be the 0.10% tax (no broker commission)!",
                new Money(new BigDecimal(playerPrice.multiply(BigDecimal.valueOf(100)).multiply(new BigDecimal("0.0010")).toPlainString()), Currency.IDR),
                matchRes.fills.get(0).getSellerFee());
    }

    @Test
    public void testR02_HoldingCompanyFormedThroughProductionPathWithCanonicalSectors() throws Exception {
        File testDir = new File("build/tmp-test-r02");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("200000000")); // 200M cash

        // Found 4 companies through production facade path using canonical sectors:
        OperatingCompany cFin = facade.foundCompany("COMP-FIN", "PT Finansial Nusantara", "FINANCIAL", Money.of(25_000_000L, Currency.IDR));
        OperatingCompany cEnergy = facade.foundCompany("COMP-NRG", "PT Energi Nusantara", "ENERGY", Money.of(25_000_000L, Currency.IDR));
        OperatingCompany cCons = facade.foundCompany("COMP-CONS", "PT Konsumer Nusantara", "CONSUMER", Money.of(25_000_000L, Currency.IDR));
        OperatingCompany cTech = facade.foundCompany("COMP-TECH", "PT Teknologi Nusantara", "TECHNOLOGY", Money.of(25_000_000L, Currency.IDR));

        Assert.assertNotNull(cFin);
        Assert.assertNotNull(cEnergy);
        Assert.assertNotNull(cCons);
        Assert.assertNotNull(cTech);

        // Form holding company
        JSONArray subs = new JSONArray();
        subs.put(new JSONObject().put("sector", "FINANCIAL").put("companyId", "COMP-FIN"));
        subs.put(new JSONObject().put("sector", "ENERGY").put("companyId", "COMP-NRG"));
        subs.put(new JSONObject().put("sector", "CONSUMER").put("companyId", "COMP-CONS"));
        subs.put(new JSONObject().put("sector", "TECHNOLOGY").put("companyId", "COMP-TECH"));

        JSONObject holding = facade.createHoldingCompany("HOLD-NUSANTARA", "Nusantara Conglomerate Group", subs);
        Assert.assertNotNull(holding);
        Assert.assertEquals("HOLD-NUSANTARA", holding.getString("holdingId"));
        Assert.assertEquals(4, holding.getJSONArray("subsidiaries").length());

        // Verify save and reload
        facade.save();

        SaveManager sm2 = SaveManager.forDirectory(testDir);
        TradingFacade facade2 = new TradingFacade(sm2);
        facade2.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade2.startOrResume();

        JSONArray loadedHoldings = facade2.getHoldingCompaniesJson();
        Assert.assertEquals(1, loadedHoldings.length());
        Assert.assertEquals("HOLD-NUSANTARA", loadedHoldings.getJSONObject(0).getString("holdingId"));
    }

    @Test
    public void testR03_ShortBorrowFeeCalculatedExclusivelyOnBorrowedSharesWithoutArrearsCompounding() throws Exception {
        RiskComplianceService risk = new RiskComplianceService();
        Account borrower = new Account("BORROWER", "Borrower", AccountType.PERSONAL, null);
        Account lender = new Account("LENDER", "Lender", AccountType.FIRM, null);
        EconomyLedger ledger = new EconomyLedger();

        Instrument inst = new Instrument("IDX:BBCA", "BBCA", "Bank BCA", MarketExchange.IDX, "Finance",
                Currency.IDR, 100, 10000000L, true, Money.of(10000L, Currency.IDR));
        InstrumentCatalog cat = new InstrumentCatalog();
        cat.registerInstrument(inst);

        lender.addPosition(new Position("BBCA", "IDX", Currency.IDR, 200000L, BigDecimal.valueOf(10000), BigDecimal.valueOf(10000)));

        // Borrow 100,000 shares @ Rp 10.000 = Rp 1,000,000,000 gross. Annual borrow fee = 2.5%
        ShortPosition sp = risk.openShortPosition(borrower, inst, 100000L, lender, ledger, 1000L);
        Assert.assertEquals(100000L, sp.getBorrowedShares());

        // Daily rate = 0.025 / 360 = 0.00006944...
        // 1B * (0.025 / 360) = 69,444.44... -> Rp 69.000 with 6 decimal places scale
        BigDecimal price = BigDecimal.valueOf(10000);
        Money feeDay1 = sp.calculateDailyBorrowFee(price);
        Assert.assertEquals(69000L, feeDay1.getAmount().longValue());

        // Accrue Day 1 with 0 borrower cash -> fee becomes unpaid fee
        risk.accrueDailyFinancingCosts(borrower, cat, ledger, BigDecimal.valueOf(15800), 2000L);
        Money unpaid1 = sp.getAccruedUnpaidFee();
        Assert.assertEquals(69000L, unpaid1.getAmount().longValue());

        // Day 2 fee calculation: MUST be based solely on borrowed stock value, NOT compounding on unpaid fee!
        Money feeDay2 = sp.calculateDailyBorrowFee(price);
        Assert.assertEquals("Daily borrow fee on Day 2 must NOT compound on unpaid arrears!", feeDay1, feeDay2);

        // Now borrower covers 50,000 shares (half position)
        borrower.creditCash(Money.of(20_000_000L, Currency.IDR));
        risk.closeShortPosition(borrower, inst, 50000L, lender, ledger, 3000L);
        Assert.assertEquals(50000L, sp.getBorrowedShares());

        // Day 3 fee calculation: MUST be halved based on 50,000 remaining shares!
        Money feeDay3 = sp.calculateDailyBorrowFee(price);
        Assert.assertEquals("Daily fee after 50% cover must be cut in half", 34500L, feeDay3.getAmount().longValue());

        // Borrower covers remaining 50,000 shares without cash for remaining arrears
        borrower.creditCash(Money.of(20_000_000L, Currency.IDR));
        risk.closeShortPosition(borrower, inst, 50000L, lender, ledger, 4000L);
        Assert.assertEquals(0L, sp.getBorrowedShares());

        // Day 4 fee calculation when borrowed shares == 0: MUST BE EXACTLY ZERO!
        Money feeDay4 = sp.calculateDailyBorrowFee(price);
        Assert.assertEquals("Daily fee when borrowed shares is 0 MUST be zero even if arrears exist!",
                Money.zero(Currency.IDR), feeDay4);
    }

    @Test
    public void testR04_SaveGenerationFenceRejectsObsoleteDelayedSnapshotFromOverwritingNewerDiskSave() throws Exception {
        File testDir = new File("build/tmp-test-r04");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);

        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        // Instance 1 saves initial state (Day 1, timestamp 1000)
        facade.save();
        SaveState diskState = sm.load().getState();
        Assert.assertNotNull(diskState);
        long initialSavedTimestamp = diskState.getSavedTimestamp();

        // Simulate Instance 2 advancing game and saving newer state (timestamp = initial + 50,000)
        String content = Files.readString(sm.getSaveFile().toPath());
        JSONObject jsonNewer = new JSONObject(content);
        jsonNewer.put("savedTimestamp", initialSavedTimestamp + 50000L);
        jsonNewer.put("gameDate", "2026-11-20");
        SaveState newerState = sm.readValidatedState(jsonNewer.toString());
        sm.save(newerState);

        Assert.assertEquals(LocalDate.of(2026, 11, 20), sm.load().getState().getGameDate());

        // Now simulate delayed Writer 1 trying to save its old snapshot (timestamp = initial)
        JSONObject jsonObsolete = new JSONObject(content);
        jsonObsolete.put("savedTimestamp", initialSavedTimestamp);
        jsonObsolete.put("gameDate", "2026-11-01");
        SaveState obsoleteState = sm.readValidatedState(jsonObsolete.toString());

        boolean overwriteBlocked = false;
        try {
            sm.save(obsoleteState);
        } catch (IllegalStateException e) {
            overwriteBlocked = true;
            System.out.println("R04 Fence triggered: " + e.getMessage());
            Assert.assertTrue(e.getMessage().contains("Penolakan penulisan save"));
        }

        Assert.assertTrue("Obsolete snapshot must be blocked from overwriting newer disk save!", overwriteBlocked);
        // Verify disk still has Day 20
        Assert.assertEquals("Disk save must remain intact with the newer date",
                LocalDate.of(2026, 11, 20), sm.load().getState().getGameDate());
    }

    @Test
    public void testR05_WhaleNewsRequiresPublishedCurrentOrNearTermEventAndStrictSectorMatch() {
        ResearchService rs = new ResearchService();
        rs.resetForGame(1704067200000L);
        rs.setDynamicDailyNewsEnabled(true);
        InstrumentCatalog cat = new InstrumentCatalog();
        Instrument instFinance = new Instrument("IDX:BBCA", "BBCA", "Bank BCA", MarketExchange.IDX, "Finance", Currency.IDR, 100, 1000000L, true, Money.of(10000L, Currency.IDR));
        cat.registerInstrument(instFinance);

        LocalDate today = LocalDate.of(2026, 11, 2);
        long todayTimestamp = 1793616000000L;

        // 1. Unpublished event -> MUST NOT generate Whale news
        MarketEvent unpubEvent = new MarketEvent("EVT-UNPUB", "Bocoran Gelap", "Belum terbit resmi", "Finance",
                MarketEvent.EventImpact.VERY_POSITIVE, todayTimestamp, false);
        rs.addMarketEvent(unpubEvent);
        rs.ensureDailyNewsSchedule(today, cat, todayTimestamp);
        Assert.assertFalse("Unpublished event must NOT generate Whale news", rs.hasNewsForEconomicEvent("EVT-UNPUB"));

        // 2. Far future event (> 1 day ahead) -> MUST NOT generate Whale news today
        LocalDate day2 = today.plusDays(1);
        long t2 = todayTimestamp + 86400000L;
        MarketEvent futureEvent = new MarketEvent("EVT-FUT", "Agenda Bulan Depan", "Masih lama", "Finance",
                MarketEvent.EventImpact.VERY_POSITIVE, t2 + 10 * 86400000L, true);
        rs.addMarketEvent(futureEvent);
        rs.ensureDailyNewsSchedule(day2, cat, t2);
        Assert.assertFalse("Future event (> 1 day) must NOT generate Whale news today", rs.hasNewsForEconomicEvent("EVT-FUT"));

        // 3. Mismatched sector with no matching instruments (e.g. Agriculture) -> MUST NOT pick arbitrary stock
        LocalDate day3 = today.plusDays(2);
        long t3 = todayTimestamp + 2 * 86400000L;
        MarketEvent agEvent = new MarketEvent("EVT-AGRI", "Panen Raya Komoditas", "Sektor pertanian", "Agriculture",
                MarketEvent.EventImpact.VERY_POSITIVE, t3, true);
        rs.addMarketEvent(agEvent);
        rs.ensureDailyNewsSchedule(day3, cat, t3);
        Assert.assertFalse("Event with no matching sector stocks must NOT fallback to arbitrary stock!", rs.hasNewsForEconomicEvent("EVT-AGRI"));

        // 4. Valid current published event matching Finance sector -> Generates Whale news for BBCA!
        LocalDate day4 = today.plusDays(3);
        long t4 = todayTimestamp + 3 * 86400000L;
        MarketEvent validEvent = new MarketEvent("EVT-VALID-01", "Mega Akuisisi Finansial", "Akuisisi perbankan", "Finance",
                MarketEvent.EventImpact.VERY_POSITIVE, t4, true);
        rs.addMarketEvent(validEvent);
        rs.ensureDailyNewsSchedule(day4, cat, t4);
        Assert.assertTrue("Valid event with matching sector must generate Whale news", rs.hasNewsForEconomicEvent("EVT-VALID-01"));
    }

    @Test
    public void testR06_FundRebalanceOrderSurvivesOvernightAndExecutesFullyInMorningSession() throws Exception {
        File testDir = new File("build/tmp-test-r06");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));

        // Start at 15:55 WIB on Day 1
        GameClock startClock = new GameClock(LocalDate.of(2026, 11, 2), LocalTime.of(15, 55, 0));
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"), startClock);

        // Subscribe 20M IDR to FUND-EQ-01
        MutualFund fund = facade.getMutualFundManager().getFund("FUND-EQ-01");
        fund.submitSubscription("SUB-R06-1", facade.getPersonalAccount(), Money.of(20_000_000L, Currency.IDR),
                facade.getLedger(), facade.getClock().getGameTimestamp());

        // Advance 10 minutes to 16:05 WIB (crosses 16:00 cutoff -> creates rebalance order for BBCA)
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
        Assert.assertEquals(OrderSide.BUY, rebalOrder.getSide());
        long rebalQty = rebalOrder.getTotalQuantity();
        Assert.assertTrue("Rebalance quantity must be positive", rebalQty > 0);

        // Advance overnight through midnight to next morning 09:05 WIB (17 hours = 61,200s)
        facade.advanceWorldSecondsForTest(61200);

        // Market is now open!
        Assert.assertTrue("IDX market must be open at 09:05 WIB",
                facade.getClock().isTradingOpenFor(MarketExchange.IDX));
        Assert.assertEquals("Rebalance order must still be OPEN in the morning",
                OrderStatus.OPEN, rebalOrder.getStatus());

        // Provide ask liquidity from Market Maker
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        Account mm = facade.getAccountsMap().get("ACC-NPC-MM-01");
        if (mm.getPosition(bbca.getId()) == null || mm.getPosition(bbca.getId()).getQuantity() < rebalQty) {
            mm.addPosition(new Position(bbca.getTicker(), bbca.getExchange().name(), bbca.getCurrency(),
                    rebalQty * 2, rebalOrder.getLimitPrice().getAmount(), rebalOrder.getLimitPrice().getAmount()));
        }
        Order mmAsk = new Order("ORD-MM-ASK-" + System.currentTimeMillis(), "REQ-MM-ASK-" + System.currentTimeMillis(),
                mm.getId(), bbca.getId(), OrderSide.SELL, OrderType.LIMIT,
                rebalOrder.getLimitPrice(), null, rebalQty,
                facade.getClock().getGameTimestamp(), Money.zero(Currency.IDR));

        // Submit MM ask -> matches rebalance order!
        MatchingEngine.OrderResult fillRes = facade.submitOrder(mmAsk);
        Assert.assertTrue("Fill result must be success", fillRes.isSuccess);

        // Rebalance order MUST be FILLED!
        Assert.assertEquals("Rebalance order must be completely FILLED!", OrderStatus.FILLED, rebalOrder.getStatus());

        // Pool account MUST now own the BBCA shares!
        Account poolAcc = facade.getAccountsMap().get(fund.getPoolAccountId());
        Assert.assertNotNull(poolAcc);
        Position poolBbca = poolAcc.getPosition(bbca.getId());
        Assert.assertNotNull("Pool account must own BBCA shares", poolBbca);
        Assert.assertEquals("Pool BBCA quantity must equal rebalance quantity", rebalQty, poolBbca.getQuantity());

        // Fund NAV must be positive and consistent
        BigDecimal nav = fund.getNavPerUnit();
        Assert.assertTrue("Fund NAV must be positive", nav.signum() > 0);
        System.out.println("R06 Fund rebalance execution verified! NAV: " + nav + ", BBCA shares in pool: " + poolBbca.getQuantity());
    }
}
