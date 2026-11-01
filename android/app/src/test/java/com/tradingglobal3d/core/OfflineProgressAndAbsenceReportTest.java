package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.player.PlayerProfileManager;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;

import static org.junit.Assert.*;

public class OfflineProgressAndAbsenceReportTest {

    private TradingFacade facade;
    private File tempDir;

    @Before
    public void setUp() throws Exception {
        tempDir = Files.createTempDirectory("test-offline-progress-").toFile();
        SaveManager saveManager = SaveManager.forDirectory(tempDir);
        facade = new TradingFacade(saveManager);

        File catFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("src/main/assets/data/market_universe.json");
        facade.setCatalog(com.tradingglobal3d.core.market.InstrumentCatalog.fromJsonString(Files.readString(catFile.toPath())));
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("100000000"));
    }

    @Test
    public void testOfflineProgressTogglePersistence() throws Exception {
        PlayerProfileManager profile = facade.getPlayerProfileManager();
        assertTrue("Default kemajuan offline harus aktif (true)", profile.isOfflineProgressEnabled());

        // Ubah jadi false
        facade.updatePlayerProfile("Trader Sukses", "100%", false, false, "EMAS_KLASIK", false);
        assertFalse("Kemajuan offline harus nonaktif", profile.isOfflineProgressEnabled());

        // Simpan dan pulihkan
        facade.save();
        TradingFacade restoredFacade = new TradingFacade(SaveManager.forDirectory(tempDir));
        restoredFacade.setCatalog(facade.getCatalog());
        restoredFacade.load();
        assertFalse("Status nonaktif harus bertahan setelah muat save", restoredFacade.getPlayerProfileManager().isOfflineProgressEnabled());
    }

    @Test
    public void testWorldContinuesWhenAbsenceReportPreferenceIsDisabled() {
        facade.getPlayerProfileManager().setOfflineProgressEnabled(false);
        LocalDate dateBefore = facade.getClock().getCurrentDate();
        long timeBefore = facade.getClock().getGameTimestamp();

        // Lima jam nyata pada 48x tetap menjadi 10 hari game; preferensi hanya menonaktifkan laporan.
        JSONObject report = facade.reconcileOfflineElapsed(5L * 3600L * 1000L);
        assertNotNull(report);
        assertFalse(report.optBoolean("hasReport"));
        assertTrue(report.optBoolean("silent"));
        assertEquals(dateBefore.plusDays(10), facade.getClock().getCurrentDate());
        assertEquals(timeBefore + 10L * 86_400_000L, facade.getClock().getGameTimestamp());
    }

    @Test
    public void testShortAbsenceIsSilent() {
        facade.getPlayerProfileManager().setOfflineProgressEnabled(true);

        // Simulasi absen 60 detik (<= 120 detik / ShortAbsentCalcul)
        JSONObject report = facade.reconcileOfflineElapsed(60_000L);
        assertNotNull(report);
        assertTrue("Absen singkat (<= 2 menit) harus silent", report.optBoolean("silent"));
        assertFalse("Absen singkat tidak boleh memicu popup laporan ketidakhadiran", report.optBoolean("hasReport"));
    }

    @Test
    public void testLongAbsenceGeneratesAbsenceReport() {
        facade.getPlayerProfileManager().setOfflineProgressEnabled(true);

        // Simulasi absen 2 jam nyata = 48 jam game = 2 hari bursa pada rasio 24x
        long elapsedMs = 2L * 3600L * 1000L;
        JSONObject report = facade.reconcileOfflineElapsed(elapsedMs);
        assertNotNull(report);
        assertFalse("Absen panjang (> 2 menit) tidak boleh silent", report.optBoolean("silent"));
        assertTrue("Harus memicu laporan ketidakhadiran", report.optBoolean("hasReport"));
        assertTrue("Hari bursa yang dilewati harus >= 1", report.optLong("daysAbsent") >= 1);
        assertNotNull("Harus mencantumkan PnL", report.optString("pnl"));
        assertNotNull("Harus mencantumkan kas tersedia", report.optString("cashAvailable"));

        // Verifikasi dismiss
        facade.dismissOfflineCatchupReport();
        assertNull("Setelah dismiss, laporan harus bersih", facade.getLastOfflineCatchupReport());
    }
}
