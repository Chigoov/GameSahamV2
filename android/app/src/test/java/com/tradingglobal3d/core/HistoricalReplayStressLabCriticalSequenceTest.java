package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.HistoricalBar;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.replay.HistoricalReplayEngine;
import com.tradingglobal3d.core.replay.StrategyLabSandbox;
import com.tradingglobal3d.core.replay.StressTestResult;
import com.tradingglobal3d.core.replay.StressTestScenario;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Pengujian Kritis Standar A3 untuk Phase 12 (PHASE-QUALITY-STANDARDS.md):
 * Minimal 10 variasi urutan yang dapat direproduksi, masing-masing minimal 100 aksi bermakna,
 * dengan invariant diperiksa setelah SETIAP aksi.
 * Cakupan: Replay candle historis nyata, honest fill logic, anti-future leak,
 * isolasi total Strategy Lab Sandbox, simulasi Macro Stress Test crash/FX,
 * penutupan hari simulasi panjang (long timeline), dan save/reload deterministik.
 */
public class HistoricalReplayStressLabCriticalSequenceTest {
    private File testDir;
    private SaveManager saveManager;
    private InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "replay_crit_seq_" + System.currentTimeMillis());
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

        assertNotNull("Replay engine wajib ada: " + context, facade.getReplayEngine());
    }

    private void run100ReplayStressActionsForSeed(long seed, int sequenceNumber) throws Exception {
        TradingFacade facade = new TradingFacade(saveManager);
        if (catalog != null) facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.HISTORICAL_REPLAY, BigDecimal.valueOf(100_000_000));
        GameTestSupport.unlockManager(facade);

        Account personal = facade.getPersonalAccount();
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 1000, BigDecimal.valueOf(10000), BigDecimal.valueOf(10250)));

        Random rnd = new Random(seed);
        facade.getClock().play();

        int actionsCount = 0;
        int step = 0;

        HistoricalBar baseBar = new HistoricalBar(
                LocalDate.of(2026, 1, 15),
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(10500),
                BigDecimal.valueOf(9800),
                BigDecimal.valueOf(10200),
                50000L
        );

        while (actionsCount < 100) {
            step++;
            int op = rnd.nextInt(9);
            String ctx = String.format("Seq %d (Seed %d) Step %d Op %d", sequenceNumber, seed, step, op);

            switch (op) {
                case 0: { // Uji pencocokan jujur order limit pada candle historis
                    HistoricalReplayEngine engine = facade.getReplayEngine();
                    Order buyFillable = Order.createLimit("ORD-B1-" + step, "IDX:BBCA", personal.getId(), OrderSide.BUY,
                            Money.of(10000, Currency.IDR), 10, 1000L);
                    assertTrue("Order beli @ 10.000 harus dapat terisi (Low 9.800)",
                            engine.canFillOrderOnHistoricalBar(buyFillable, baseBar));

                    Order buyUnfillable = Order.createLimit("ORD-B2-" + step, "IDX:BBCA", personal.getId(), OrderSide.BUY,
                            Money.of(9500, Currency.IDR), 10, 1000L);
                    assertFalse("Order beli @ 9.500 dilarang terisi (Low 9.800)",
                            engine.canFillOrderOnHistoricalBar(buyUnfillable, baseBar));
                    actionsCount++;
                    break;
                }
                case 1: { // Uji proteksi anti-future leakage pada query data historis
                    HistoricalReplayEngine engine = facade.getReplayEngine();
                    List<HistoricalBar> bars = new ArrayList<>();
                    bars.add(new HistoricalBar(LocalDate.of(2026, 2, 1), BigDecimal.valueOf(9000), BigDecimal.valueOf(9200), BigDecimal.valueOf(8900), BigDecimal.valueOf(9100), 1000L));
                    bars.add(new HistoricalBar(LocalDate.of(2026, 2, 2), BigDecimal.valueOf(9100), BigDecimal.valueOf(9300), BigDecimal.valueOf(9000), BigDecimal.valueOf(9250), 1200L));
                    bars.add(new HistoricalBar(LocalDate.of(2026, 2, 3), BigDecimal.valueOf(9250), BigDecimal.valueOf(9500), BigDecimal.valueOf(9200), BigDecimal.valueOf(9400), 1500L));

                    List<HistoricalBar> visible = engine.queryHistoricalBarsAntiLeak(bars, LocalDate.of(2026, 2, 2));
                    assertEquals(2, visible.size());
                    assertEquals(LocalDate.of(2026, 2, 2), visible.get(1).getDate());
                    actionsCount++;
                    break;
                }
                case 2: { // Isolasi ketat Strategy Lab Sandbox
                    Money realCashBefore = personal.getTotalCash(Currency.IDR);
                    long realPosBefore = personal.getPosition("BBCA") != null ? personal.getPosition("BBCA").getQuantity() : 0L;

                    StrategyLabSandbox sandbox = facade.createStrategySandbox("SB-CRIT-" + seed + "-" + step);
                    assertNotNull(sandbox);

                    var bbcaInst = catalog != null ? catalog.getById("IDX:BBCA") : null;
                    if (bbcaInst != null) {
                        sandbox.simulateBuy(bbcaInst, 100, BigDecimal.valueOf(10000));
                        assertEquals(1, sandbox.getSimulatedTradesCount());
                    }

                    // Invariant Isolasi: Akun asli dilarang berubah
                    assertEquals("Kas akun riil dilarang berubah oleh sandbox", realCashBefore, personal.getTotalCash(Currency.IDR));
                    long realPosAfter = personal.getPosition("BBCA") != null ? personal.getPosition("BBCA").getQuantity() : 0L;
                    assertEquals("Posisi saham riil dilarang berubah oleh sandbox", realPosBefore, realPosAfter);
                    actionsCount++;
                    break;
                }
                case 3: { // Jalankan skenario Macro Stress Test: Market Crash (-30%)
                    StressTestScenario crashScenario = StressTestScenario.defaultMarketCrash();
                    StressTestResult res = facade.runStressTest(crashScenario);
                    assertNotNull(res);
                    assertTrue(res.getPotentialLoss().isPositive());
                    actionsCount++;
                    break;
                }
                case 4: { // Jalankan skenario Macro Stress Test: FX Shock (+35% USD)
                    StressTestScenario fxScenario = StressTestScenario.defaultFxShock();
                    StressTestResult res = facade.runStressTest(fxScenario);
                    assertNotNull(res);
                    assertNotNull(res.getRiskAssessment());
                    actionsCount++;
                    break;
                }
                case 5: { // Jalankan skenario Stress Test kustom
                    StressTestScenario customScenario = new StressTestScenario(
                            "SC-CUSTOM-" + step, "Skenario Volatilitas Tinggi",
                            com.tradingglobal3d.core.replay.StressScenarioType.MARKET_CRASH,
                            BigDecimal.valueOf(0.75), BigDecimal.valueOf(1.10),
                            "Skenario kustom stres lab"
                    );
                    StressTestResult res = facade.runStressTest(customScenario);
                    assertNotNull(res);
                    actionsCount++;
                    break;
                }
                case 6: { // Maju waktu simulasi (long timeline advance)
                    facade.tick(60 + rnd.nextInt(120));
                    actionsCount++;
                    break;
                }
                case 7: { // Penutupan hari dan penyelesaian siklus harian via skipDay
                    facade.skipDay("SKIP-CRIT-" + seed + "-" + step);
                    actionsCount++;
                    break;
                }
                case 8: { // Save dan Reload roundtrip
                    facade.save();
                    TradingFacade reloaded = new TradingFacade(saveManager);
                    if (catalog != null) reloaded.setCatalog(catalog);
                    assertTrue("Load harus sukses pada " + ctx, reloaded.load());
                    assertInvariants(reloaded, ctx + " [Reloaded]");
                    facade = reloaded;
                    personal = facade.getPersonalAccount();
                    actionsCount++;
                    break;
                }
            }

            assertInvariants(facade, ctx);
        }

        assertTrue("Minimal 100 aksi bermakna harus tercapai", actionsCount >= 100);
    }

    @Test
    public void testReplayStressSequence01() throws Exception { run100ReplayStressActionsForSeed(12001L, 1); }

    @Test
    public void testReplayStressSequence02() throws Exception { run100ReplayStressActionsForSeed(12002L, 2); }

    @Test
    public void testReplayStressSequence03() throws Exception { run100ReplayStressActionsForSeed(12003L, 3); }

    @Test
    public void testReplayStressSequence04() throws Exception { run100ReplayStressActionsForSeed(12004L, 4); }

    @Test
    public void testReplayStressSequence05() throws Exception { run100ReplayStressActionsForSeed(12005L, 5); }

    @Test
    public void testReplayStressSequence06() throws Exception { run100ReplayStressActionsForSeed(12006L, 6); }

    @Test
    public void testReplayStressSequence07() throws Exception { run100ReplayStressActionsForSeed(12007L, 7); }

    @Test
    public void testReplayStressSequence08() throws Exception { run100ReplayStressActionsForSeed(12008L, 8); }

    @Test
    public void testReplayStressSequence09() throws Exception { run100ReplayStressActionsForSeed(12009L, 9); }

    @Test
    public void testReplayStressSequence10() throws Exception { run100ReplayStressActionsForSeed(12010L, 10); }
}
