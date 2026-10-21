package com.tradingglobal3d.core;

import com.tradingglobal3d.core.collection.CollectibleItem;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.multiasset.BondInstrument;
import com.tradingglobal3d.core.multiasset.CommodityInstrument;
import com.tradingglobal3d.core.multiasset.FundAllocationPlan;
import com.tradingglobal3d.core.storage.SaveManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Pengujian Kritis Standar A3 untuk Phase 09 (PHASE-QUALITY-STANDARDS.md):
 * Minimal 10 variasi urutan yang dapat direproduksi, masing-masing minimal 100 aksi bermakna,
 * dengan invariant diperiksa setelah SETIAP aksi.
 * Cakupan: Multi-aset (obligasi & komoditas emas), fund builder & rebalancing,
 * transaksi barang koleksi mewah/kendaraan/properti, biaya pemeliharaan, dan save/load.
 */
public class MultiAssetCollectionFundCriticalSequenceTest {
    private File testDir;
    private SaveManager saveManager;
    private InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "multi_crit_seq_" + System.currentTimeMillis());
        testDir.mkdirs();
        saveManager = SaveManager.forDirectory(testDir);

        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (universeFile.exists()) {
            String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
            catalog = InstrumentCatalog.fromJsonString(jsonUniverse);
        }
    }

    @After
    public void tearDown() {
        deleteDir(testDir);
    }

    private void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) for (File f : files) deleteDir(f);
        }
        dir.delete();
    }

    private void assertInvariants(TradingFacade facade, String context) {
        assertTrue("Audit ledger rekonsiliasi gagal: " + context, facade.reconcileAll());

        Account personal = facade.getPersonalAccount();
        assertNotNull("Personal account wajib ada: " + context, personal);

        Money totalIdr = personal.getTotalCash(Currency.IDR);
        Money availIdr = personal.getAvailableCash(Currency.IDR);
        Money resIdr = personal.getReservedCash(Currency.IDR);
        assertEquals("Available + Reserved harus sama dengan Total IDR: " + context,
                totalIdr, availIdr.add(resIdr));
        assertFalse("Total IDR negatif: " + context, totalIdr.isNegative());
        assertFalse("Available IDR negatif: " + context, availIdr.isNegative());
        assertFalse("Reserved IDR negatif: " + context, resIdr.isNegative());

        assertNotNull("Fund builder service wajib ada: " + context, facade.getFundBuilder());
        assertNotNull("Collection manager wajib ada: " + context, facade.getCollectionManager());
    }

    private void run100MultiAssetActionsForSeed(long seed, int sequenceNumber) throws Exception {
        TradingFacade facade = new TradingFacade(saveManager);
        if (catalog != null) facade.setCatalog(catalog);
        // Modal besar Rp 50.000.000.000 untuk pengujian transaksi koleksi dan properti mewah
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000_000L));
        GameTestSupport.unlockManager(facade);

        Random rnd = new Random(seed);
        String pId = facade.getPersonalAccount().getId();
        facade.getClock().play();

        int actionsCount = 0;
        int step = 0;

        while (actionsCount < 100) {
            step++;
            int op = rnd.nextInt(9);
            String ctx = String.format("Seq %d (Seed %d) Step %d Op %d", sequenceNumber, seed, step, op);

            switch (op) {
                case 0: { // Pembayaran kupon obligasi FR91
                    BondInstrument fr91 = facade.getFundBuilder().getBond("BOND:FR0091");
                    if (fr91 != null) {
                        Money beforeCoupon = facade.getPersonalAccount().getTotalCash(Currency.IDR);
                        try {
                            facade.getFundBuilder().payBondCoupon(fr91, facade.getPersonalAccount(), 90, 10,
                                    facade.getLedger(), System.currentTimeMillis());
                            fail("Legacy arbitrary coupon must be rejected");
                        } catch (UnsupportedOperationException expected) { }
                        assertEquals(beforeCoupon, facade.getPersonalAccount().getTotalCash(Currency.IDR));
                        actionsCount++;
                    }
                    break;
                }
                case 1: { // Valuasi komoditas emas
                    CommodityInstrument gold = facade.getFundBuilder().getCommodity("COMM:GOLD");
                    if (gold != null) {
                        Money goldVal = gold.calculateValue(100);
                        assertTrue("Valuasi emas harus positif", goldVal.isPositive());
                        actionsCount++;
                    }
                    break;
                }
                case 2: { // Simpan rencana alokasi Fund Builder
                    Map<String, Double> weights = new HashMap<>();
                    weights.put("IDX:BBCA", 0.40);
                    weights.put("IDX:BMRI", 0.30);
                    String planId = "PLAN-CRIT-" + seed + "-" + step;
                    FundAllocationPlan plan = facade.saveFundPlan(planId, pId, "Rencana Alokasi " + step, weights, 0.30);
                    assertNotNull(plan);
                    assertEquals(FundAllocationPlan.PlanStatus.SAVED, plan.getStatus());
                    actionsCount++;
                    break;
                }
                case 3: { // Eksekusi rebalancing rencana fund builder
                    Map<String, Double> weights = new HashMap<>();
                    weights.put("IDX:BBCA", 0.10);
                    String planId = "PLAN-REBAL-" + seed + "-" + step;
                    facade.saveFundPlan(planId, pId, "Rencana Rebal " + step, weights, 0.90);
                    var results = facade.executeFundRebalance(planId);
                    assertFalse(results.isEmpty());
                    actionsCount++;
                    break;
                }
                case 4: { // Beli barang koleksi jika belum dimiliki
                    var items = facade.getCollectionManager().getAllItems();
                    for (CollectibleItem item : items) {
                        if (!item.isOwned()) {
                            long avail = facade.getPersonalAccount().getAvailableCash(Currency.IDR).getAmount().longValue();
                            if (avail >= item.getCurrentValuation().getAmount().longValue()) {
                                facade.buyCollection(item.getId());
                                assertTrue(item.isOwned());
                                actionsCount++;
                                break;
                            }
                        }
                    }
                    break;
                }
                case 5: { // Jual salah satu barang koleksi jika dimiliki
                    var items = facade.getCollectionManager().getAllItems();
                    for (CollectibleItem item : items) {
                        if (item.isOwned()) {
                            facade.sellCollection(item.getId());
                            assertFalse(item.isOwned());
                            actionsCount++;
                            break;
                        }
                    }
                    break;
                }
                case 6: { // Biaya pemeliharaan harian koleksi
                    facade.getCollectionManager().processDailyMaintenance(
                            facade.getPersonalAccount(), facade.getLedger(), System.currentTimeMillis());
                    actionsCount++;
                    break;
                }
                case 7: { // Boundary: Tolak rencana fund builder dengan bobot ilegal (> 100% atau negatif)
                    try {
                        Map<String, Double> badWeights = new HashMap<>();
                        badWeights.put("IDX:BBCA", 1.50);
                        facade.saveFundPlan("PLAN-BAD-" + seed + "-" + step, pId, "Bad Plan", badWeights, 0.10);
                        fail("Bobot alokasi melebihi 100% harus ditolak: " + ctx);
                    } catch (IllegalArgumentException expected) {
                        actionsCount++;
                    }
                    break;
                }
                case 8: { // Save dan Reload roundtrip
                    facade.save();
                    TradingFacade reloaded = new TradingFacade(saveManager);
                    if (catalog != null) reloaded.setCatalog(catalog);
                    assertTrue("Load harus sukses pada " + ctx, reloaded.load());
                    assertInvariants(reloaded, ctx + " [Reloaded]");
                    facade = reloaded;
                    actionsCount++;
                    break;
                }
            }

            assertInvariants(facade, ctx);
        }

        assertTrue("Minimal 100 aksi bermakna harus tercapai", actionsCount >= 100);
    }

    @Test
    public void testMultiAssetSequence01() throws Exception { run100MultiAssetActionsForSeed(9001L, 1); }

    @Test
    public void testMultiAssetSequence02() throws Exception { run100MultiAssetActionsForSeed(9002L, 2); }

    @Test
    public void testMultiAssetSequence03() throws Exception { run100MultiAssetActionsForSeed(9003L, 3); }

    @Test
    public void testMultiAssetSequence04() throws Exception { run100MultiAssetActionsForSeed(9004L, 4); }

    @Test
    public void testMultiAssetSequence05() throws Exception { run100MultiAssetActionsForSeed(9005L, 5); }

    @Test
    public void testMultiAssetSequence06() throws Exception { run100MultiAssetActionsForSeed(9006L, 6); }

    @Test
    public void testMultiAssetSequence07() throws Exception { run100MultiAssetActionsForSeed(9007L, 7); }

    @Test
    public void testMultiAssetSequence08() throws Exception { run100MultiAssetActionsForSeed(9008L, 8); }

    @Test
    public void testMultiAssetSequence09() throws Exception { run100MultiAssetActionsForSeed(9009L, 9); }

    @Test
    public void testMultiAssetSequence10() throws Exception { run100MultiAssetActionsForSeed(9010L, 10); }
}
