package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.HistoricalBar;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.replay.*;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;
import com.tradingglobal3d.core.trading.OrderType;
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

import static org.junit.Assert.*;

public class HistoricalReplayStressLabTest {
    private File testDir;
    private TradingFacade facade;
    private InstrumentCatalog catalog;
    private Instrument bbca;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "replay_test_" + System.currentTimeMillis());
        testDir.mkdirs();

        File dataDir = new File("src/main/assets/data");
        File universeFile = new File(dataDir, "market_universe.json");
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        catalog = InstrumentCatalog.fromJsonString(jsonUniverse);

        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.HISTORICAL_REPLAY, BigDecimal.valueOf(50_000_000));

        bbca = catalog.getById("IDX:BBCA");
        assertNotNull(bbca);
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
    public void testHonestHistoricalCandleFillLogic() {
        HistoricalReplayEngine engine = facade.getReplayEngine();

        // Bar harian historis: Open 10.000, High 10.500, Low 9.800, Close 10.200
        HistoricalBar bar = new HistoricalBar(
                LocalDate.of(2026, 1, 15),
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(10500),
                BigDecimal.valueOf(9800),
                BigDecimal.valueOf(10200),
                50000L
        );

        // 1. Order Beli Limit @ 10.000 (Low 9.800 <= 10.000 -> Terisi)
        Order buyFillable = Order.createLimit("ORD-B1", "IDX:BBCA", "ACC-P", OrderSide.BUY,
                Money.of(10000, Currency.IDR), 100, 1000L);
        assertTrue(engine.canFillOrderOnHistoricalBar(buyFillable, bar));

        // 2. Order Beli Limit @ 9.500 (Low 9.800 > 9.500 -> Tidak Terisi karena harga tak pernah serendah itu)
        Order buyUnfillable = Order.createLimit("ORD-B2", "IDX:BBCA", "ACC-P", OrderSide.BUY,
                Money.of(9500, Currency.IDR), 100, 1000L);
        assertFalse(engine.canFillOrderOnHistoricalBar(buyUnfillable, bar));

        // 3. Order Jual Limit @ 10.400 (High 10.500 >= 10.400 -> Terisi)
        Order sellFillable = Order.createLimit("ORD-S1", "IDX:BBCA", "ACC-P", OrderSide.SELL,
                Money.of(10400, Currency.IDR), 100, 1000L);
        assertTrue(engine.canFillOrderOnHistoricalBar(sellFillable, bar));

        // 4. Order Jual Limit @ 11.000 (High 10.500 < 11.000 -> Tidak Terisi)
        Order sellUnfillable = Order.createLimit("ORD-S2", "IDX:BBCA", "ACC-P", OrderSide.SELL,
                Money.of(11000, Currency.IDR), 100, 1000L);
        assertFalse(engine.canFillOrderOnHistoricalBar(sellUnfillable, bar));
    }

    @Test
    public void testAntiFutureLeakOnHistoricalSeries() {
        HistoricalReplayEngine engine = facade.getReplayEngine();
        List<HistoricalBar> bars = new ArrayList<>();

        // Tambahkan 3 bar berurutan
        bars.add(new HistoricalBar(LocalDate.of(2026, 1, 10), BigDecimal.valueOf(9000), BigDecimal.valueOf(9200), BigDecimal.valueOf(8900), BigDecimal.valueOf(9100), 1000L));
        bars.add(new HistoricalBar(LocalDate.of(2026, 1, 11), BigDecimal.valueOf(9100), BigDecimal.valueOf(9300), BigDecimal.valueOf(9000), BigDecimal.valueOf(9250), 1200L));
        bars.add(new HistoricalBar(LocalDate.of(2026, 1, 12), BigDecimal.valueOf(9250), BigDecimal.valueOf(9500), BigDecimal.valueOf(9200), BigDecimal.valueOf(9400), 1500L));

        // Jika tanggal simulasi saat ini = 2026-01-11, bar ke-3 (12 Jan) DILARANG KELUAR (Anti-Future Leak - FR-01.05)
        List<HistoricalBar> visible = engine.queryHistoricalBarsAntiLeak(bars, LocalDate.of(2026, 1, 11));
        assertEquals(2, visible.size());
        assertEquals(LocalDate.of(2026, 1, 10), visible.get(0).getDate());
        assertEquals(LocalDate.of(2026, 1, 11), visible.get(1).getDate());
    }

    @Test
    public void testStrategyLabSandboxIsolationDoesNotMutateRealAccount() {
        Account realPersonal = facade.getPersonalAccount();
        Money initialRealCash = realPersonal.getTotalCash(Currency.IDR);

        // Berikan kepemilikan awal BBCA pada akun asli
        realPersonal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 500, BigDecimal.valueOf(10000), BigDecimal.valueOf(10250)));

        // Buat Sandbox Terisolasi (FR-17.03, FR-17.04)
        StrategyLabSandbox sandbox = facade.createStrategySandbox("SB-EXP-01");
        assertNotNull(sandbox);

        // Lakukan simulasi transaksi agresif di lab (Beli 2.000 lembar BBCA)
        boolean bought = sandbox.simulateBuy(bbca, 2000, BigDecimal.valueOf(10250));
        assertTrue(bought);
        assertEquals(1, sandbox.getSimulatedTradesCount());

        // Verifikasi akun sandbox berubah
        assertEquals(2500, sandbox.getClonedAccount().getPosition("BBCA").getQuantity());

        // VERIFIKASI KETAT ISOLASI (FR-17.03): Akun asli TIDAK BERUBAH SAMA SEKALI!
        assertEquals(initialRealCash, realPersonal.getTotalCash(Currency.IDR));
        assertEquals(500, realPersonal.getPosition("BBCA").getQuantity());
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testMacroStressTestExecutionAndMarginCallProjection() {
        Account personal = facade.getPersonalAccount();
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 4000, BigDecimal.valueOf(10000), BigDecimal.valueOf(10000)));

        // Tarik utang margin Rp 20.000.000
        facade.takeMarginLoan(Money.of(20_000_000, Currency.IDR));

        // Jalankan Stress Test Skenario Market Crash (-30%)
        StressTestScenario crashScenario = StressTestScenario.defaultMarketCrash();
        StressTestResult crashResult = facade.runStressTest(crashScenario);

        assertNotNull(crashResult);
        assertTrue(crashResult.getPotentialLoss().isPositive());
        assertTrue(crashResult.getPercentageDrop().compareTo(BigDecimal.ZERO) > 0);

        // Jalankan Skenario FX Shock (+35% USD)
        StressTestScenario fxScenario = StressTestScenario.defaultFxShock();
        StressTestResult fxResult = facade.runStressTest(fxScenario);
        assertNotNull(fxResult);
        assertNotNull(fxResult.getRiskAssessment());
    }
}
