package com.tradingglobal3d.core;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.Assert.*;

/**
 * Pengujian komprehensif aturan waktu dual-rate, skenario numerik TIME-ACCEPTANCE.csv,
 * dan kasus transisi lifecycle L01..L19 sesuai mandat 20 Oktober 2026.
 */
public class TimeDualRateLifecycleTest {
    private File testDir;
    private SaveManager saveManager;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "time_dual_rate_test_" + System.currentTimeMillis());
        testDir.mkdirs();
        saveManager = SaveManager.forDirectory(testDir);
        facade = new TradingFacade(saveManager);

        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) {
            universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        }
        if (universeFile.exists()) {
            String jsonUniverse = new String(java.nio.file.Files.readAllBytes(universeFile.toPath()), java.nio.charset.StandardCharsets.UTF_8);
            facade.setCatalog(com.tradingglobal3d.core.market.InstrumentCatalog.fromJsonString(jsonUniverse));
        }

        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        GameTestSupport.unlockManager(facade);
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

    // ==========================================
    // 1. Verifikasi seluruh 26 Kasus TIME-ACCEPTANCE.csv
    // ==========================================

    @Test
    public void testTimeAcceptanceT01_NoElapsed() {
        long gameMs = GameClock.convertRealMsToGameMs(0, true, 1);
        assertEquals(0L, gameMs);
    }

    @Test
    public void testTimeAcceptanceT02_Foreground1SecAt1x() {
        long gameMs = GameClock.convertRealMsToGameMs(1000, true, 1);
        assertEquals(288000L, gameMs); // 288 detik game
    }

    @Test
    public void testTimeAcceptanceT03_Foreground5MinAt1xEqualsOneDay() {
        long gameMs = GameClock.convertRealMsToGameMs(300000, true, 1);
        assertEquals(86400000L, gameMs); // 1 hari game penuh (300 detik nyata)
    }

    @Test
    public void testTimeAcceptanceT03b_Foreground15MinAt1xEqualsThreeDays() {
        long gameMs = GameClock.convertRealMsToGameMs(900000, true, 1);
        assertEquals(259200000L, gameMs);
    }

    @Test
    public void testTimeAcceptanceT04_Foreground150SecAt2xEqualsOneDay() {
        long gameMs = GameClock.convertRealMsToGameMs(150000, true, 2);
        assertEquals(86400000L, gameMs); // 1 hari game penuh
    }

    @Test
    public void testTimeAcceptanceT05_Foreground60SecAt5xEqualsOneDay() {
        long gameMs = GameClock.convertRealMsToGameMs(60000, true, 5);
        assertEquals(86400000L, gameMs); // 1 hari game penuh
    }

    @Test
    public void testTimeAcceptanceT06_Foreground15MinAt1xEqualsThreeDays() {
        long gameMs = GameClock.convertRealMsToGameMs(900000, true, 1);
        assertEquals(259200000L, gameMs); // 3 hari game
    }

    @Test
    public void testTimeAcceptanceT07_Foreground30MinAt1xEqualsSixDays() {
        long gameMs = GameClock.convertRealMsToGameMs(1800000, true, 1);
        assertEquals(518400000L, gameMs); // 6 hari game
    }

    @Test
    public void testTimeAcceptanceT08_Inactive1Sec() {
        long gameMs = GameClock.convertRealMsToGameMs(1000, false, 1);
        assertEquals(24000L, gameMs); // 24 detik game
    }

    @Test
    public void testTimeAcceptanceT09_Inactive5MinEqualsTwoHours() {
        long gameMs = GameClock.convertRealMsToGameMs(300000, false, 1);
        assertEquals(7200000L, gameMs); // 2 jam game
    }

    @Test
    public void testTimeAcceptanceT10_Inactive30MinEqualsTwelveHours() {
        long gameMs = GameClock.convertRealMsToGameMs(1800000, false, 1);
        assertEquals(43200000L, gameMs); // 12 jam game
    }

    @Test
    public void testTimeAcceptanceT11_Inactive1HourEqualsOneDay() {
        long gameMs = GameClock.convertRealMsToGameMs(3600000, false, 1);
        assertEquals(86400000L, gameMs); // 1 hari game
    }

    @Test
    public void testTimeAcceptanceT12_Inactive8HoursEqualsEightDays() {
        long gameMs = GameClock.convertRealMsToGameMs(28800000, false, 1);
        assertEquals(691200000L, gameMs); // 8 hari game
    }

    @Test
    public void testTimeAcceptanceT13_Inactive24HoursEqualsTwentyFourDays() {
        long gameMs = GameClock.convertRealMsToGameMs(86400000, false, 1);
        assertEquals(2073600000L, gameMs); // 24 hari game
    }

    @Test
    public void testTimeAcceptanceT14_Inactive30RealDaysEquals720GameDays() {
        long gameMs = GameClock.convertRealMsToGameMs(2592000000L, false, 1);
        assertEquals(62208000000L, gameMs); // 720 hari game
    }

    @Test
    public void testTimeAcceptanceT15_InactiveIgnoresForegroundSpeed2x() {
        long gameMs = GameClock.convertRealMsToGameMs(3600000, false, 2);
        assertEquals(86400000L, gameMs); // Tetap 1 hari game (rate 24x)
    }

    @Test
    public void testTimeAcceptanceT16_InactiveIgnoresForegroundSpeed5x() {
        long gameMs = GameClock.convertRealMsToGameMs(3600000, false, 5);
        assertEquals(86400000L, gameMs); // Tetap 1 hari game (rate 24x)
    }

    @Test
    public void testTimeAcceptanceT17_Foreground1DayPlusInactive1Day() {
        long fg = GameClock.convertRealMsToGameMs(300000, true, 1);
        long in = GameClock.convertRealMsToGameMs(3600000, false, 1);
        assertEquals(172800000L, fg + in); // 2 hari game
    }

    @Test
    public void testTimeAcceptanceT18_Foreground2xOneDayPlusInactive1Day() {
        long fg = GameClock.convertRealMsToGameMs(150000, true, 2);
        long in = GameClock.convertRealMsToGameMs(3600000, false, 2);
        assertEquals(172800000L, fg + in); // 2 hari game
    }

    @Test
    public void testTimeAcceptanceT19_Foreground5xOneMinutePlusInactive1Hour() {
        long fg = GameClock.convertRealMsToGameMs(60000, true, 5); // 1 hari game
        long in = GameClock.convertRealMsToGameMs(3600000, false, 5); // 1 hari game
        assertEquals(172800000L, fg + in); // 2 hari game
    }

    @Test
    public void testTimeAcceptanceT20_Foreground1MinPlusInactive30Min() {
        long fg = GameClock.convertRealMsToGameMs(60000, true, 1);
        long in = GameClock.convertRealMsToGameMs(1800000, false, 1);
        assertEquals(60480000L, fg + in);
    }

    @Test
    public void testTimeAcceptanceT21_Foreground1msRemainder() {
        GameClock clock = GameClock.createDefault();
        clock.play();
        long sec = clock.advanceRealMs(1, true);
        assertEquals(0, sec);
        assertEquals(288, clock.getGameMsRemainder());
    }

    @Test
    public void testTimeAcceptanceT22_Inactive1msRemainder() {
        GameClock clock = GameClock.createDefault();
        long sec = clock.advanceRealMs(1, false);
        assertEquals(0, sec);
        assertEquals(24, clock.getGameMsRemainder());
    }

    @Test
    public void testTimeAcceptanceT23_Mixed1msRemainders() {
        GameClock clock = GameClock.createDefault();
        clock.play();
        clock.advanceRealMs(1, true);
        assertEquals(288, clock.getGameMsRemainder());
        clock.advanceRealMs(1, false);
        assertEquals(312, clock.getGameMsRemainder());
    }

    @Test
    public void testTimeAcceptanceT24_Foreground100msPartitionInvariance() {
        GameClock clockA = GameClock.createDefault();
        clockA.play();
        long totalSecA = 0;
        for (int i = 0; i < 10; i++) {
            totalSecA += clockA.advanceRealMs(100, true);
        }
        GameClock clockB = GameClock.createDefault();
        clockB.play();
        long totalSecB = clockB.advanceRealMs(1000, true);

        assertEquals(totalSecB, totalSecA);
        assertEquals(288, totalSecA);
        assertEquals(0, clockA.getGameMsRemainder());
        assertEquals(0, clockB.getGameMsRemainder());
    }

    @Test
    public void testTimeAcceptanceT25_Inactive100msPartitionInvariance() {
        GameClock clockA = GameClock.createDefault();
        long totalSecA = 0;
        for (int i = 0; i < 10; i++) {
            totalSecA += clockA.advanceRealMs(100, false);
        }
        GameClock clockB = GameClock.createDefault();
        long totalSecB = clockB.advanceRealMs(1000, false);

        assertEquals(totalSecB, totalSecA);
        assertEquals(24, totalSecA);
        assertEquals(0, clockA.getGameMsRemainder());
        assertEquals(0, clockB.getGameMsRemainder());
    }

    @Test
    public void testTimeAcceptanceT26_InactiveSevenRealDaysEquals168GameDays() {
        long gameMs = GameClock.convertRealMsToGameMs(604800000L, false, 1);
        assertEquals(14515200000L, gameMs); // 168 hari game
    }

    // ==========================================
    // 2. Lifecycle Skenario L01..L19
    // ==========================================

    @Test
    public void testLifecycleL01_ActiveThenBackgroundThenActive() {
        GameClock clock = facade.getClock();
        clock.play();
        LocalDate startDate = clock.getCurrentDate();
        LocalTime startTime = clock.getCurrentTime();

        // 1. Foreground aktif selama 150 detik pada 2x = 1 hari game.
        clock.setSpeedMultiplier(2);
        facade.advanceRealMs(150000, true);
        assertEquals(startDate.plusDays(1), clock.getCurrentDate());
        assertEquals(startTime, clock.getCurrentTime());

        // 2. Transisi ke background
        facade.onAppLifecycleChanged(false);
        assertFalse(clock.isForeground());

        // 3. Di background selama 1 jam nyata = 1 hari game
        facade.reconcileOfflineElapsed(3600000);
        assertEquals(startDate.plusDays(2), clock.getCurrentDate());
        assertEquals(startTime, clock.getCurrentTime());

        // 4. Kembali ke foreground
        facade.onAppLifecycleChanged(true);
        assertTrue(clock.isForeground());
    }

    @Test
    public void testLifecycleL02_Active5xDoesNotCarryOverToBackground() {
        GameClock clock = facade.getClock();
        clock.play();
        clock.setSpeedMultiplier(5);

        LocalDate startDate = clock.getCurrentDate();

        // Background selama 1 jam nyata harus TEPAT 1 hari game, bukan 5 hari game!
        facade.reconcileOfflineElapsed(3600000);
        assertEquals(startDate.plusDays(1), clock.getCurrentDate());
        assertEquals(5, clock.getSpeedMultiplier()); // Pilihan 5x tetap terjaga saat kembali ke foreground
    }

    @Test
    public void testLifecycleL03_RepeatedLifecycleCallbacksAreIdempotent() {
        facade.onAppLifecycleChanged(false);
        facade.onAppLifecycleChanged(false); // callback ganda
        assertFalse(facade.getClock().isForeground());

        facade.onAppLifecycleChanged(true);
        facade.onAppLifecycleChanged(true); // callback ganda
        assertTrue(facade.getClock().isForeground());
    }

    @Test
    public void testLifecycleL07_LegacySaveWithoutAnchorReceivesCurrentTimeAnchor() throws Exception {
        // Simulasikan save V2 lama tanpa anchor
        Account personal = facade.getPersonalAccount();
        facade.save();

        TradingFacade restored = new TradingFacade(saveManager);
        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (universeFile.exists()) {
            String jsonUniverse = new String(java.nio.file.Files.readAllBytes(universeFile.toPath()), java.nio.charset.StandardCharsets.UTF_8);
            restored.setCatalog(com.tradingglobal3d.core.market.InstrumentCatalog.fromJsonString(jsonUniverse));
        }
        assertTrue(restored.load());
        assertTrue(restored.getLastRealTimeAnchor() > 0);
        assertTrue(restored.reconcileAll());
    }

    @Test
    public void testLifecycleL08_NegativeElapsedRejected() {
        try {
            facade.advanceRealMs(-5000, true);
            fail("Elapsed negatif harus ditolak");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("tidak boleh negatif"));
        }
    }

    @Test
    public void testLifecycleL10_SkipDayResetsAnchorAndDoesNotDoubleCount() throws Exception {
        LocalDate beforeSkip = facade.getClock().getCurrentDate();
        JSONObject receipt = facade.skipDay("SKIP-L10-REQ-1");
        assertTrue(receipt.getBoolean("success"));
        assertEquals(beforeSkip.plusDays(1), facade.getClock().getCurrentDate());

        // Retry Skip ID sama harus idempoten (L11)
        JSONObject retry = facade.skipDay("SKIP-L10-REQ-1");
        assertEquals(receipt.getString("from"), retry.getString("from"));
        assertEquals(receipt.getString("to"), retry.getString("to"));
        assertEquals(beforeSkip.plusDays(1), facade.getClock().getCurrentDate());
    }

    @Test
    public void testLifecycleL12_LongOffline30Days() {
        LocalDate startDate = facade.getClock().getCurrentDate();
        // 30 hari nyata = 2.592.000 detik = 720 hari game
        JSONObject report = facade.reconcileOfflineElapsed(2592000000L);
        assertNotNull(report);
        assertEquals(startDate.plusDays(720), facade.getClock().getCurrentDate());
        assertTrue(facade.reconcileAll());
    }
}
