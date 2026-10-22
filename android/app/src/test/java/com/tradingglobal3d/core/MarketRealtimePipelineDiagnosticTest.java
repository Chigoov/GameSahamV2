package com.tradingglobal3d.core;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.OrderBook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalTime;

import static org.junit.Assert.*;

/**
 * Uji diagnostik dan regresi untuk A4: Rantai Realtime Pasar Game (Tick -> NPC -> Matching -> Harga).
 * Digunakan pada A4-01 untuk mendeteksi kegagalan harga flat 0,00%, dan sebagai gerbang verifikasi A4-02..A4-04.
 */
public class MarketRealtimePipelineDiagnosticTest {
    private File testDir;
    private SaveManager saveManager;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "a4_diagnostic_test_" + System.currentTimeMillis());
        testDir.mkdirs();
        saveManager = SaveManager.forDirectory(testDir);
        facade = new TradingFacade(saveManager);

        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) {
            universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        }
        if (universeFile.exists()) {
            String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
            facade.setCatalog(InstrumentCatalog.fromJsonString(jsonUniverse));
        }

        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        facade.getClock().play();
    }

    @After
    public void tearDown() {
        if (testDir != null && testDir.exists()) {
            for (File f : testDir.listFiles()) f.delete();
            testDir.delete();
        }
    }

    /**
     * Membuktikan bahwa saat jam perdagangan aktif IDX, aktivitas pasar menghasilkan transaksi nyata
     * (TradeFill > 0) dan harga bergerak secara sah tanpa perlu transaksi pemain pertama.
     */
    @Test
    public void testIdxMarketProducesFillsDuringActiveSession() {
        GameClock clock = facade.getClock();
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        assertNotNull("BBCA harus ada di katalog", bbca);
        OrderBook book = facade.getMarketEngine().getOrCreateBook(bbca);

        // Majukan waktu ke 09:00:00 (Sesi 1 IDX buka)
        while (clock.getCurrentTime().isBefore(LocalTime.of(9, 0))) {
            facade.tick(10);
        }

        // Jalankan 4 interval x 15 menit game
        for (int i = 0; i < 4; i++) {
            facade.advanceRealMs(9375, true);
        }

        // Assertion yang mengevaluasi apakah ada transaksi terbentuk
        int trades = book.getTradeHistory().size();
        assertTrue("Pasar aktif harus menghasilkan setidaknya 1 transaksi pada emiten acuan selama sesi buka, aktual: " + trades,
                trades > 0);
    }

    /**
     * Membuktikan bahwa pada jam bursa Amerika (21:30 - 04:00 WIB), instrumen bursa US
     * diakui dalam sesi perdagangan aktif dan likuiditas/order matching dapat terbentuk.
     */
    @Test
    public void testUsMarketHoursRecognizedAndActive() {
        GameClock clock = facade.getClock();
        Instrument aapl = facade.getCatalog().getById("US:AAPL");
        assertNotNull("AAPL harus ada di katalog", aapl);
        OrderBook book = facade.getMarketEngine().getOrCreateBook(aapl);

        // Majukan waktu ke 21:30:00 (Pembukaan bursa US di waktu WIB)
        while (clock.getCurrentTime().isBefore(LocalTime.of(21, 30))) {
            facade.tick(30);
        }

        // Jalankan simulasi pada jam bursa US
        for (int i = 0; i < 4; i++) {
            facade.advanceRealMs(9375, true);
        }

        int trades = book.getTradeHistory().size();
        assertTrue("Bursa US harus menghasilkan transaksi selama jam perdagangan US (21:30+ WIB), aktual: " + trades,
                trades > 0);
    }
}
