package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.market.SectorDominanceService;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.player.DailyStreakService;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.MatchingEngine;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Pengujian Tahap S4: Jalur Disiplin Harian (Streak Strip),
 * Guncangan Pasar Mendadak (Black Swan & Supply Chain Crisis),
 * serta Status Dominasi & Pengaruh Sektor Bursa.
 */
public class DailyStreakAndMarketShockTahapS4Test {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "s4_test_" + UUID.randomUUID());
        testDir.mkdirs();

        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) {
            universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        }
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(InstrumentCatalog.fromJsonString(jsonUniverse));
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(500_000_000));
    }

    @After
    public void tearDown() {
        if (testDir != null && testDir.exists()) {
            File[] files = testDir.listFiles();
            if (files != null) for (File f : files) f.delete();
            testDir.delete();
        }
    }

    @Test
    public void testDailyStreakProgressionAndBrokenStreakReset() throws Exception {
        DailyStreakService ss = facade.getDailyStreakService();
        assertNotNull(ss);

        // Awal: Sesi 1 -> Streak 1 (1.0x)
        assertEquals(1, ss.getCurrentStreak());
        assertEquals(1.0, ss.getRewardMultiplier(), 0.01);
        assertFalse(ss.isStreakBroken());

        // Login berurutan: Sesi 2, Sesi 3, Sesi 4, Sesi 5
        ss.recordSessionLogin(2);
        assertEquals(2, ss.getCurrentStreak());

        ss.recordSessionLogin(3);
        assertEquals(3, ss.getCurrentStreak());
        assertEquals(1.25, ss.getRewardMultiplier(), 0.01); // 1.25x

        ss.recordSessionLogin(4);
        ss.recordSessionLogin(5);
        assertEquals(5, ss.getCurrentStreak());
        assertEquals(1.50, ss.getRewardMultiplier(), 0.01); // 1.50x

        // Login berurutan hingga Hari ke-7
        ss.recordSessionLogin(6);
        ss.recordSessionLogin(7);
        assertEquals(7, ss.getCurrentStreak());
        assertEquals(2.0, ss.getRewardMultiplier(), 0.01); // Day 7+: 2.0x

        // Absen melewatkan sesi (lompat dari sesi 7 ke sesi 10) -> Broken Streak!
        ss.recordSessionLogin(10);
        assertEquals(1, ss.getCurrentStreak());
        assertTrue("Absen harus memicu broken streak", ss.isStreakBroken());
        assertEquals(1.0, ss.getRewardMultiplier(), 0.01);
    }

    @Test
    public void dailyStreakStateSurvivesSaveAndLoad() throws Exception {
        DailyStreakService streak = facade.getDailyStreakService();
        streak.recordSessionLogin(2);
        streak.recordSessionLogin(3);
        assertEquals(3, streak.getCurrentStreak());
        facade.save();

        TradingFacade restored = new TradingFacade(SaveManager.forDirectory(testDir));
        restored.setCatalog(facade.getCatalog());
        assertTrue(restored.load());
        assertEquals(3, restored.getDailyStreakService().getCurrentStreak());
        assertEquals(1.25, restored.getDailyStreakService().getRewardMultiplier(), 0.001);
    }

    @Test
    public void dailySponsorRewardUsesSevenDayStreakMultiplier() throws Exception {
        DailyStreakService streak = facade.getDailyStreakService();
        for (int session = 2; session <= 7; session++) streak.recordSessionLogin(session);
        assertEquals(2.0, streak.getRewardMultiplier(), 0.001);
        JSONObject status = facade.getDailySponsorStatus();
        assertEquals(50_000_000L, status.getLong("grantAmount"));
        Money before = facade.getPersonalAccount().getAvailableCash(Currency.IDR);
        JSONObject receipt = facade.claimDailySponsor("S4-STREAK-SPONSOR-" + UUID.randomUUID());
        assertTrue(receipt.getBoolean("success"));
        assertEquals("Rp 50.000.000", receipt.getString("amount"));
        assertEquals(before.add(Money.of(50_000_000, Currency.IDR)),
                facade.getPersonalAccount().getAvailableCash(Currency.IDR));
    }

    @Test
    public void testSectorDominanceThresholdAndPrivileges() throws Exception {
        Account personal = facade.getPersonalAccount();
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        assertNotNull(bbca);

        // Sebelum beli saham dalam jumlah dominan
        SectorDominanceService sds = facade.getSectorDominanceService();
        Map<String, SectorDominanceService.SectorStatus> statusesBefore = sds.evaluateDominance(personal, facade.getCatalog(), BigDecimal.valueOf(15800));
        assertFalse(statusesBefore.get("Keuangan").isDominant());

        // Akumulasi kepemilikan masif di sektor Keuangan (> 25% outstanding seluruh sektor)
        for (Instrument inst : facade.getCatalog().getActiveUniverse()) {
            if ("Keuangan".equalsIgnoreCase(inst.getSector())) {
                long hugeShares = (long) (inst.getSharesOutstanding() * 0.35);
                personal.addPosition(new Position(inst.getTicker(), inst.getExchange().name(), inst.getCurrency(), hugeShares,
                        inst.getCurrentPrice().getAmount(), inst.getCurrentPrice().getAmount()));
            }
        }

        Map<String, SectorDominanceService.SectorStatus> statusesAfter = sds.evaluateDominance(personal, facade.getCatalog(), BigDecimal.valueOf(15800));
        SectorDominanceService.SectorStatus finStatus = statusesAfter.get("Keuangan");
        assertNotNull(finStatus);
        assertTrue("Kepemilikan > 25% harus dinobatkan sebagai Penguasa Sektor", finStatus.isDominant());
        assertTrue("Penguasa sektor harus memiliki hak veto aktif", finStatus.isVetoPowerActive());
        assertEquals(25.0, finStatus.getFeeDiscountPct(), 0.01);
    }

    @Test
    public void exactTwentyFivePercentMustNotUnlockSectorDominance() {
        InstrumentCatalog oneSector = new InstrumentCatalog();
        oneSector.registerInstrument(new Instrument("IDX:TST", "TST", "Test Instrument", MarketExchange.IDX,
                "Threshold", Currency.IDR, 1, 100L, true, Money.of(100, Currency.IDR)));
        Account player = new Account("ACC-THRESHOLD", "Test Player", com.tradingglobal3d.core.model.AccountType.PERSONAL, null);
        player.addPosition(new Position("TST", "IDX", Currency.IDR, 25L, BigDecimal.valueOf(100), BigDecimal.valueOf(100)));

        SectorDominanceService.SectorStatus exact = new SectorDominanceService()
                .evaluateDominance(player, oneSector, BigDecimal.valueOf(15800)).get("Threshold");
        assertEquals(25.0, exact.getPlayerOwnershipPct(), 0.0001);
        assertFalse("Persyaratan masterplan adalah >25%, bukan >=25%", exact.isDominant());

        player.addPosition(new Position("TST", "IDX", Currency.IDR, 26L, BigDecimal.valueOf(100), BigDecimal.valueOf(100)));
        SectorDominanceService.SectorStatus above = new SectorDominanceService()
                .evaluateDominance(player, oneSector, BigDecimal.valueOf(15800)).get("Threshold");
        assertTrue(above.isDominant());
    }

    @Test
    public void dominantSectorDiscountReducesPlayerBuyCommission() throws Exception {
        Account personal = facade.getPersonalAccount();
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        assertNotNull(bbca);
        for (Instrument inst : facade.getCatalog().getActiveUniverse()) {
            if ("Keuangan".equalsIgnoreCase(inst.getSector())) {
                long shares = (long) (inst.getSharesOutstanding() * 0.35);
                personal.addPosition(new Position(inst.getTicker(), inst.getExchange().name(), inst.getCurrency(), shares,
                        inst.getCurrentPrice().getAmount(), inst.getCurrentPrice().getAmount()));
            }
        }
        SectorDominanceService.SectorStatus status = facade.getSectorDominanceService()
                .evaluateDominance(personal, facade.getCatalog(), BigDecimal.valueOf(15800)).get("Keuangan");
        assertTrue(status.isDominant());

        Account dealer = facade.getAccount("ACC-NPC-MM-01");
        assertNotNull(dealer);
        Position dealerPosition = dealer.getPosition(bbca.getId());
        assertNotNull(dealerPosition);
        assertTrue(dealerPosition.getQuantity() >= bbca.getLotSize());
        com.tradingglobal3d.core.trading.OrderBook book = facade.getMarketEngine().getOrCreateBook(bbca);
        String reqId = "DOMINANT-BUY-FEE-001";
        com.tradingglobal3d.core.trading.Order ask = new com.tradingglobal3d.core.trading.Order(
                "ASK-" + reqId, "ASK-" + reqId, dealer.getId(), bbca.getId(),
                com.tradingglobal3d.core.trading.OrderSide.SELL, com.tradingglobal3d.core.trading.OrderType.LIMIT,
                bbca.getCurrentPrice(), null, bbca.getLotSize(), facade.getClock().getGameTimestamp(), Money.zero(Currency.IDR));
        book.addLimitOrder(ask);
        com.tradingglobal3d.core.trading.Order buy = new com.tradingglobal3d.core.trading.Order(
                "ORD-" + reqId, reqId, personal.getId(), bbca.getId(),
                com.tradingglobal3d.core.trading.OrderSide.BUY, com.tradingglobal3d.core.trading.OrderType.LIMIT,
                bbca.getCurrentPrice(), null, bbca.getLotSize(), facade.getClock().getGameTimestamp(), Money.zero(Currency.IDR));
        MatchingEngine.OrderResult result = facade.submitOrder(buy);
        assertTrue(result.isSuccess);
        com.tradingglobal3d.core.economy.LedgerEntry buyFee = facade.getLedger().getEntriesForAccount(personal.getId()).stream()
                .filter(entry -> entry.getType() == com.tradingglobal3d.core.economy.LedgerEntryType.TRANSACTION_FEE
                        && entry.getDescription().contains("Biaya beli BBCA"))
                .reduce((first, second) -> second).orElseThrow(AssertionError::new);
        BigDecimal ordinaryFee = bbca.getCurrentPrice().getAmount().multiply(BigDecimal.valueOf(bbca.getLotSize()))
                .multiply(MatchingEngine.BUY_FEE_RATE);
        assertTrue("Dominasi sektor >25% harus memangkas fee beli sebesar 25%", buyFee.getAmount().getAmount()
                .compareTo(ordinaryFee.multiply(new BigDecimal("0.75"))) <= 0);
    }

    @Test
    public void researchEfficiencyUpgradeReducesPlayerBuyCommission() throws Exception {
        facade.upgradeTechNode("TECH-EFF-1");
        Account personal = facade.getPersonalAccount();
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        Account dealer = facade.getAccount("ACC-NPC-MM-01");
        assertNotNull(bbca);
        assertNotNull(dealer);
        assertTrue(dealer.getPosition(bbca.getId()).getQuantity() >= bbca.getLotSize());
        com.tradingglobal3d.core.trading.OrderBook book = facade.getMarketEngine().getOrCreateBook(bbca);
        String reqId = "RESEARCH-BUY-FEE-001";
        book.addLimitOrder(new com.tradingglobal3d.core.trading.Order("ASK-" + reqId, "ASK-" + reqId, dealer.getId(),
                bbca.getId(), com.tradingglobal3d.core.trading.OrderSide.SELL, com.tradingglobal3d.core.trading.OrderType.LIMIT,
                bbca.getCurrentPrice(), null, bbca.getLotSize(), facade.getClock().getGameTimestamp(), Money.zero(Currency.IDR)));
        MatchingEngine.OrderResult result = facade.submitOrder(new com.tradingglobal3d.core.trading.Order("ORD-" + reqId,
                reqId, personal.getId(), bbca.getId(), com.tradingglobal3d.core.trading.OrderSide.BUY,
                com.tradingglobal3d.core.trading.OrderType.LIMIT, bbca.getCurrentPrice(), null, bbca.getLotSize(),
                facade.getClock().getGameTimestamp(), Money.zero(Currency.IDR)));
        assertTrue(result.isSuccess);
        com.tradingglobal3d.core.economy.LedgerEntry buyFee = facade.getLedger().getEntriesForAccount(personal.getId()).stream()
                .filter(entry -> entry.getType() == com.tradingglobal3d.core.economy.LedgerEntryType.TRANSACTION_FEE
                        && entry.getDescription().contains("Biaya beli BBCA"))
                .reduce((first, second) -> second).orElseThrow(AssertionError::new);
        BigDecimal ordinaryFee = bbca.getCurrentPrice().getAmount().multiply(BigDecimal.valueOf(bbca.getLotSize()))
                .multiply(MatchingEngine.BUY_FEE_RATE);
        BigDecimal expectedDiscountedFee = new Money(ordinaryFee.multiply(new BigDecimal("0.85")), Currency.IDR).getAmount();
        assertEquals("Tech node EFF-1 harus memberi diskon komisi 15%; fills=" + result.fills.size(),
                expectedDiscountedFee, buyFee.getAmount().getAmount());
    }

    @Test
    public void testMarketShockAndSupplyChainCrisis() throws Exception {
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        BigDecimal priceBefore = bbca.getCurrentPrice().getAmount();
        Account personal = facade.getPersonalAccount();
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 1_000, priceBefore, priceBefore));
        com.tradingglobal3d.core.multiasset.CommodityInstrument gold = facade.getFundBuilder().getCommodity("COMM:GOLD");
        BigDecimal goldBefore = gold.getSpotPricePerUnit().getAmount();
        personal.addPosition(new Position("XAU", "COMM", Currency.IDR, 10, goldBefore, goldBefore));
        Money cashReserveBefore = personal.getAvailableCash(Currency.IDR);

        // 1. Uji Black Swan Market Shock (15% drop)
        JSONObject shockRes = facade.triggerMarketShockEvent("BLACK_SWAN_SHOCK");
        assertTrue(shockRes.getBoolean("success"));
        assertTrue(shockRes.getInt("affectedInstruments") > 0);
        assertEquals(5.0, shockRes.getDouble("safeHavenGoldMovePercent"), 0.001);
        assertTrue("Emas fisik harus menguat sebagai safe-haven", gold.getSpotPricePerUnit().getAmount().compareTo(goldBefore) > 0);
        assertTrue("Eksposur emas harus menghasilkan offset positif", new BigDecimal(shockRes.getString("safeHavenGain")).signum() > 0);
        assertTrue("Keuntungan safe-haven harus mengurangi kerugian bersih", new BigDecimal(shockRes.getString("netPlayerLoss"))
                .compareTo(new BigDecimal(shockRes.getString("grossPlayerLoss"))) < 0);
        assertEquals("Kas adalah cadangan likuid dan tidak ikut terpotong oleh Black Swan",
                cashReserveBefore, personal.getAvailableCash(Currency.IDR));
        assertEquals(cashReserveBefore.format(), shockRes.getString("cashBufferIdr"));
        assertTrue(shockRes.getDouble("netLossPercentOfStartingWealth") >= 0.0);

        BigDecimal priceAfterShock = bbca.getCurrentPrice().getAmount();
        assertTrue("Harga saham harus anjlok setelah guncangan pasar", priceAfterShock.compareTo(priceBefore) < 0);

        // 2. Uji Supply Chain Crisis pada biaya perusahaan operasional
        com.tradingglobal3d.core.business.OperatingCompany manufacturer = facade.foundCompany("S4-MANU", "QA Manufaktur", "MANUFAKTUR", Money.of(25_000_000, Currency.IDR));
        com.tradingglobal3d.core.business.OperatingCompany retailer = facade.foundCompany("S4-RETAIL", "QA Retail", "RITEL", Money.of(25_000_000, Currency.IDR));
        com.tradingglobal3d.core.business.OperatingCompany energy = facade.foundCompany("S4-ENERGY", "QA Energi", "ENERGI", Money.of(25_000_000, Currency.IDR));
        Money manufacturerCostBefore = manufacturer.calculateDailyOperatingCost();
        Money retailCostBefore = retailer.calculateDailyOperatingCost();
        Money energyRevenueBefore = energy.calculateDailyRevenue();
        JSONObject crisisRes = facade.triggerMarketShockEvent("SUPPLY_CHAIN_CRISIS");
        assertTrue(crisisRes.getBoolean("success"));
        assertTrue("Krisis pasokan harus menaikkan biaya operasi manufaktur", manufacturer.calculateDailyOperatingCost().compareTo(manufacturerCostBefore) > 0);
        assertTrue("Krisis pasokan harus menaikkan biaya operasi ritel", retailer.calculateDailyOperatingCost().compareTo(retailCostBefore) > 0);
        assertTrue("Sektor energi harus mendapat upside permintaan saat krisis pasokan", energy.calculateDailyRevenue().compareTo(energyRevenueBefore) > 0);
    }
}
