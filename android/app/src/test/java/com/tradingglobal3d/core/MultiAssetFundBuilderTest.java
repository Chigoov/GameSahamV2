package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.firm.ClientMandate;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.multiasset.*;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.MatchingEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MultiAssetFundBuilderTest {
    private File testDir;
    private TradingFacade facade;
    private InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "multiasset_test_" + System.currentTimeMillis());
        testDir.mkdirs();

        File dataDir = new File("src/main/assets/data");
        File universeFile = new File(dataDir, "market_universe.json");
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        catalog = InstrumentCatalog.fromJsonString(jsonUniverse);

        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(100_000_000));
        com.tradingglobal3d.core.GameTestSupport.unlockManager(facade);
    }

    @After
    public void tearDown() {
        deleteDir(testDir);
    }

    private void deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) deleteDir(f);
        }
        dir.delete();
    }

    @Test
    public void testBondInstrumentAndCouponPayment() {
        FundBuilderService fundService = facade.getFundBuilder();
        BondInstrument fr91 = fundService.getBond("BOND:FR0091");
        assertNotNull(fr91);

        assertEquals("FR0091", fr91.getTicker());
        assertEquals(Money.of(1_000_000, Currency.IDR), fr91.getParValue());
        assertEquals(new BigDecimal("0.06375"), fr91.getCouponAnnualRate());

        // Hitung kupon untuk 10 unit obligasi yang dipegang selama 90 hari (kuartalan)
        Money coupon = fr91.calculateAccruedCoupon(90, 10);
        assertTrue(coupon.isPositive());

        // Bayar kupon ke kas akun pemain
        Account personal = facade.getPersonalAccount();
        Money cashBefore = personal.getTotalCash(Currency.IDR);
        try { fundService.payBondCoupon(fr91, personal, 90, 10, facade.getLedger(), System.currentTimeMillis()); fail("Arbitrary coupon must not mint cash"); }
        catch (UnsupportedOperationException expected) { }
        assertEquals(cashBefore, personal.getTotalCash(Currency.IDR));
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testCommodityGoldInstrumentValuation() {
        FundBuilderService fundService = facade.getFundBuilder();
        CommodityInstrument gold = fundService.getCommodity("COMM:GOLD");
        assertNotNull(gold);

        assertEquals("Gram", gold.getUnitOfMeasure());
        assertEquals(Money.of(1_450_000, Currency.IDR), gold.getSpotPricePerUnit());

        // 50 gram emas
        Money goldValue = gold.calculateValue(50);
        assertEquals(Money.of(72_500_000, Currency.IDR), goldValue);
    }

    @Test
    public void testSavingFundPlanDoesNotMutatePositionsOrCash() {
        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getTotalCash(Currency.IDR);

        // Buat target bobot: 40% BBCA, 30% BMRI, 20% Lainnya, 10% Kas
        Map<String, Double> weights = new HashMap<>();
        weights.put("IDX:BBCA", 0.40);
        weights.put("IDX:BMRI", 0.30);
        weights.put("IDX:BBRI", 0.20);
        double cashReserve = 0.10;

        FundAllocationPlan plan = facade.saveFundPlan("PLAN-TEST-01", personal.getId(), "Portofolio Campuran Primer", weights, cashReserve);
        assertNotNull(plan);
        assertEquals(FundAllocationPlan.PlanStatus.SAVED, plan.getStatus());

        // Verifikasi ketat: Menyimpan rencana TIDAK mengubah kas atau posisi fisik (FR-11.05)
        assertEquals(initialCash, personal.getTotalCash(Currency.IDR));
        assertEquals(initialCash, personal.getAvailableCash(Currency.IDR));
        assertTrue(personal.getPositions().isEmpty());
    }

    @Test
    public void testMandateValidationRejectsViolatingFundPlan() {
        Account client1 = facade.acceptClientMandate("MANDATE-01"); // Konservatif: max konsentrasi 15%, min kas 20%

        // Coba buat rencana dengan konsentrasi BBCA 40% (> 15%)
        Map<String, Double> weights = new HashMap<>();
        weights.put("IDX:BBCA", 0.40);
        weights.put("IDX:BMRI", 0.30);
        double cashReserve = 0.30;

        try {
            facade.saveFundPlan("PLAN-VIOLATE", client1.getId(), "Rencana Melanggar Mandat", weights, cashReserve);
            fail("Rencana alokasi yang melanggar batas mandat klien harus ditolak");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("melanggar batas konsentrasi"));
        }
    }

    @Test
    public void testOpenRebalanceCannotClaimCompletionOrDuplicateOrdersAfterReload() throws Exception {
        Map<String, Double> weights = new java.util.LinkedHashMap<>();
        weights.put("IDX:BBCA", 0.10);
        FundAllocationPlan plan = facade.saveFundPlan("PLAN-OPEN", facade.getPersonalAccount().getId(), "Open plan", weights, 0.90);
        List<MatchingEngine.OrderResult> first = facade.executeFundRebalance(plan.getId());
        assertEquals(1, first.size());
        assertEquals(com.tradingglobal3d.core.trading.OrderStatus.OPEN, first.get(0).order.getStatus());
        assertEquals(FundAllocationPlan.PlanStatus.EXECUTING, plan.getStatus());
        Money reserved = facade.getPersonalAccount().getReservedCash(Currency.IDR);
        List<MatchingEngine.OrderResult> retry = facade.executeFundRebalance(plan.getId());
        assertSame(first.get(0).order, retry.get(0).order);
        assertEquals(reserved, facade.getPersonalAccount().getReservedCash(Currency.IDR));
        facade.save();
        TradingFacade loaded = new TradingFacade(SaveManager.forDirectory(testDir));
        loaded.setCatalog(catalog);
        assertTrue(loaded.load());
        assertNotNull(loaded.getFundBuilder().getPlan(plan.getId()));
        List<MatchingEngine.OrderResult> afterLoad = loaded.executeFundRebalance(plan.getId());
        assertEquals(first.get(0).order.getId(), afterLoad.get(0).order.getId());
        assertEquals(reserved, loaded.getPersonalAccount().getReservedCash(Currency.IDR));
        assertEquals(1, loaded.getOrderManager().getOrdersForAccount(loaded.getPersonalAccount().getId()).size());
    }

    @Test
    public void testInvalidPlanWeightsCannotBeSaved() {
        double[][] invalid = {{Double.NaN, 0.5}, {Double.POSITIVE_INFINITY, 0.5}, {1.1, -0.1}, {0.5, Double.NaN}};
        for (int i = 0; i < invalid.length; i++) {
            Map<String, Double> weights = new HashMap<>();
            weights.put("IDX:BBCA", invalid[i][0]);
            try {
                facade.saveFundPlan("INVALID-" + i, facade.getPersonalAccount().getId(), "Invalid", weights, invalid[i][1]);
                fail("Nonfinite/out-of-range allocation must reject");
            } catch (IllegalArgumentException expected) {
                assertNull(facade.getFundBuilder().getPlan("INVALID-" + i));
            }
        }
    }

    @Test
    public void testUsdRebalanceConvertsIdrTargetBeforeSizingShares() {
        Account account = facade.getPersonalAccount();
        Map<String, Double> weights = new java.util.LinkedHashMap<>();
        weights.put("US:AAPL", 0.10);
        FundAllocationPlan plan = facade.saveFundPlan("PLAN-USD", account.getId(), "USD allocation", weights, 0.90);
        List<com.tradingglobal3d.core.trading.Order> orders = facade.getFundBuilder().calculateRebalanceOrders(
                plan, account, catalog, new BigDecimal("15800"));
        assertEquals(1, orders.size());
        assertEquals("US:AAPL", orders.get(0).getInstrumentId());
        assertEquals("IDR 10 million / 15800 / USD235 rounds down to 2 shares", 2, orders.get(0).getTotalQuantity());
        account.addPosition(new com.tradingglobal3d.core.model.Position("AAPL", "US", Currency.USD, 1,
                new BigDecimal("235.00"), new BigDecimal("235.00")));
        orders = facade.getFundBuilder().calculateRebalanceOrders(plan, account, catalog, new BigDecimal("15800"));
        assertEquals(1, orders.get(0).getTotalQuantity());
        assertEquals(com.tradingglobal3d.core.trading.OrderSide.BUY, orders.get(0).getSide());
    }

    @Test
    public void testUnsupportedPlanLegRejectsBeforeAnyOrderOrReservation() {
        Map<String, Double> weights = new java.util.LinkedHashMap<>();
        weights.put("IDX:BBCA", 0.10);
        weights.put("COMM:GOLD", 0.10);
        FundAllocationPlan plan = facade.saveFundPlan("PLAN-UNSUPPORTED", facade.getPersonalAccount().getId(), "Unsupported leg", weights, 0.80);
        Money cash = facade.getPersonalAccount().getAvailableCash(Currency.IDR);
        int entries = facade.getLedger().getEntries().size();
        try {
            facade.executeFundRebalance(plan.getId());
            fail("Unsupported target must not be silently skipped");
        } catch (IllegalArgumentException expected) {
            assertEquals(cash, facade.getPersonalAccount().getAvailableCash(Currency.IDR));
            assertTrue(facade.getOrderManager().getOrdersForAccount(facade.getPersonalAccount().getId()).isEmpty());
            assertEquals(entries, facade.getLedger().getEntries().size());
            assertEquals(FundAllocationPlan.PlanStatus.SAVED, plan.getStatus());
        }
    }

    @Test
    public void testExecuteRebalanceOrdersCalculation() {
        Account personal = facade.getPersonalAccount();

        Map<String, Double> weights = new HashMap<>();
        weights.put("IDX:BBCA", 0.50);
        weights.put("IDX:BMRI", 0.30);
        double cashReserve = 0.20;

        facade.saveFundPlan("PLAN-EXEC-01", personal.getId(), "Alokasi Eksekusi", weights, cashReserve);

        // Eksekusi rencana rebalance
        List<MatchingEngine.OrderResult> results = facade.executeFundRebalance("PLAN-EXEC-01");
        assertFalse(results.isEmpty());

        for (MatchingEngine.OrderResult res : results) {
            assertTrue(res.isSuccess);
        }

        FundAllocationPlan executedPlan = facade.getFundBuilder().getPlan("PLAN-EXEC-01");
        assertEquals(FundAllocationPlan.PlanStatus.EXECUTING, executedPlan.getStatus());
        for (MatchingEngine.OrderResult result : results)
            assertEquals(com.tradingglobal3d.core.trading.OrderStatus.OPEN, result.order.getStatus());

        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testFundRebalanceCompletesToExecutedWithReconciliation() {
        Account personal = facade.getPersonalAccount();
        Instrument bbca = catalog.getById("IDX:BBCA");
        assertNotNull(bbca);

        // Pasang antrean jual (Ask) dari Market Maker pada harga pasar saat ini
        Account mm = facade.getAccount("ACC-NPC-MM-01");
        assertNotNull(mm);
        com.tradingglobal3d.core.trading.Order mmAsk = com.tradingglobal3d.core.trading.Order.createLimit(
                "MM-ASK-BBCA-REBAL", "IDX:BBCA", mm.getId(),
                com.tradingglobal3d.core.trading.OrderSide.SELL,
                bbca.getCurrentPrice(), 5000, System.currentTimeMillis()
        );
        MatchingEngine.OrderResult askRes = facade.submitOrder(mmAsk);
        assertTrue(askRes.isSuccess);

        // Buat rencana alokasi: 20% BBCA, 80% Kas
        Map<String, Double> weights = new HashMap<>();
        weights.put("IDX:BBCA", 0.20);
        facade.saveFundPlan("PLAN-FILL-RECON", personal.getId(), "Alokasi Eksekusi Tuntas", weights, 0.80);

        // Eksekusi rencana rebalance
        List<MatchingEngine.OrderResult> results = facade.executeFundRebalance("PLAN-FILL-RECON");
        assertEquals(1, results.size());
        MatchingEngine.OrderResult rebalOrderRes = results.get(0);
        assertEquals(com.tradingglobal3d.core.trading.OrderStatus.FILLED, rebalOrderRes.order.getStatus());

        // Verifikasi plan status menjadi EXECUTED
        FundAllocationPlan executedPlan = facade.getFundBuilder().getPlan("PLAN-FILL-RECON");
        assertEquals(FundAllocationPlan.PlanStatus.EXECUTED, executedPlan.getStatus());

        // Verifikasi kepemilikan saham BBCA personal account
        com.tradingglobal3d.core.model.Position bbcaPos = personal.getPosition("IDX:BBCA");
        assertNotNull(bbcaPos);
        assertEquals(1900, bbcaPos.getQuantity());

        // Verifikasi kas tertahan kembali 0
        assertEquals(Money.zero(Currency.IDR), personal.getReservedCash(Currency.IDR));

        // Verifikasi rekonsiliasi seluruh akun di buku besar
        assertTrue(facade.reconcileAll());

        // Eksekusi ulang idempoten: tidak membuat order baru dan status tetap EXECUTED
        List<MatchingEngine.OrderResult> retryResults = facade.executeFundRebalance("PLAN-FILL-RECON");
        assertEquals(1, retryResults.size());
        assertSame(rebalOrderRes.order, retryResults.get(0).order);
        assertEquals(FundAllocationPlan.PlanStatus.EXECUTED, executedPlan.getStatus());
        assertEquals(1900, personal.getPosition("IDX:BBCA").getQuantity());
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testFundRebalanceSellsExcessPositionAndReconciles() {
        Account personal = facade.getPersonalAccount();
        Instrument bbca = catalog.getById("IDX:BBCA");
        assertNotNull(bbca);

        // Berikan kepemilikan awal 4.000 lembar BBCA @ 10.000
        personal.addPosition(new com.tradingglobal3d.core.model.Position(
                "BBCA", "IDX", Currency.IDR, 4000,
                BigDecimal.valueOf(10000), bbca.getCurrentPrice().getAmount()
        ));

        // Pasang antrean beli (Bid) dari Market Maker pada harga pasar saat ini
        Account mm = facade.getAccount("ACC-NPC-MM-01");
        assertNotNull(mm);
        com.tradingglobal3d.core.trading.Order mmBid = com.tradingglobal3d.core.trading.Order.createLimit(
                "MM-BID-BBCA-REBAL", "IDX:BBCA", mm.getId(),
                com.tradingglobal3d.core.trading.OrderSide.BUY,
                bbca.getCurrentPrice(), 5000, System.currentTimeMillis()
        );
        MatchingEngine.OrderResult bidRes = facade.submitOrder(mmBid);
        assertTrue(bidRes.isSuccess);

        Money initialCash = personal.getTotalCash(Currency.IDR);

        // Buat rencana alokasi: target hanya 10% BBCA (sisanya 90% kas)
        Map<String, Double> weights = new HashMap<>();
        weights.put("IDX:BBCA", 0.10);
        facade.saveFundPlan("PLAN-SELL-RECON", personal.getId(), "Alokasi Jual Rebalance", weights, 0.90);

        // Eksekusi rebalancing
        List<MatchingEngine.OrderResult> results = facade.executeFundRebalance("PLAN-SELL-RECON");
        assertEquals(1, results.size());
        MatchingEngine.OrderResult sellOrderRes = results.get(0);
        assertEquals(com.tradingglobal3d.core.trading.OrderSide.SELL, sellOrderRes.order.getSide());
        assertEquals(com.tradingglobal3d.core.trading.OrderStatus.FILLED, sellOrderRes.order.getStatus());

        // Verifikasi plan status menjadi EXECUTED
        FundAllocationPlan executedPlan = facade.getFundBuilder().getPlan("PLAN-SELL-RECON");
        assertEquals(FundAllocationPlan.PlanStatus.EXECUTED, executedPlan.getStatus());

        // Verifikasi kepemilikan berkurang sesuai penjualan
        long soldQty = sellOrderRes.order.getTotalQuantity();
        assertTrue("Harus menjual sebagian saham", soldQty > 0);
        assertEquals(4000 - soldQty, personal.getPosition("IDX:BBCA").getQuantity());

        // Verifikasi kas bertambah setelah penjualan bersih (gross - sell fee)
        assertTrue(personal.getTotalCash(Currency.IDR).compareTo(initialCash) > 0);

        // Verifikasi rekonsiliasi seluruh akun di buku besar
        assertTrue(facade.reconcileAll());
    }
}
