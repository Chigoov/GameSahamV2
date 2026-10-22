package com.tradingglobal3d.core;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.*;

import static org.junit.Assert.*;

/**
 * Uji komprehensif penerimaan A12: Dana Hibah Harian Reset 00.00 WIB (Asia/Jakarta) Dunia Nyata.
 */
public class DailySponsorRealDateWibTest {
    private File testDir;
    private SaveManager saveManager;
    private TradingFacade facade;
    private MutableTestClock testRealClock;
    private static final ZoneId WIB = ZoneId.of("Asia/Jakarta");

    public static class MutableTestClock extends Clock {
        private Instant instant;
        private final ZoneId zone;

        public MutableTestClock(Instant initialInstant, ZoneId zone) {
            this.instant = initialInstant;
            this.zone = zone;
        }

        public void setInstant(Instant next) {
            this.instant = next;
        }

        public void advance(Duration duration) {
            this.instant = this.instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableTestClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "a12_daily_sponsor_test_" + System.currentTimeMillis());
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

        // Set waktu nyata awal: 22 Oktober 2026, 23:30:00 WIB (= 16:30:00 UTC)
        ZonedDateTime initialWib = ZonedDateTime.of(2026, 10, 22, 23, 30, 0, 0, WIB);
        testRealClock = new MutableTestClock(initialWib.toInstant(), WIB);
        facade.setRealTimeClock(testRealClock);
    }

    @After
    public void tearDown() {
        if (testDir != null && testDir.exists()) {
            for (File f : testDir.listFiles()) f.delete();
            testDir.delete();
        }
    }

    @Test
    public void testFirstClaimSucceedsAndCredits25M() throws Exception {
        Money initialCash = facade.getPersonalAccount().getTotalCash(Currency.IDR);
        JSONObject statusBefore = facade.getDailySponsorStatus();
        assertTrue("Sebelum klaim pertama harus eligible", statusBefore.getBoolean("eligible"));
        assertFalse(statusBefore.getBoolean("alreadyClaimed"));

        JSONObject res = facade.claimDailySponsor("REQ-SPON-01");
        assertTrue("Klaim pertama harus sukses", res.getBoolean("success"));

        Money finalCash = facade.getPersonalAccount().getTotalCash(Currency.IDR);
        assertEquals("Kas harus bertambah tepat Rp 25.000.000",
                initialCash.add(Money.of(25_000_000, Currency.IDR)), finalCash);

        JSONObject statusAfter = facade.getDailySponsorStatus();
        assertFalse("Setelah klaim tidak boleh eligible", statusAfter.getBoolean("eligible"));
        assertTrue(statusAfter.getBoolean("alreadyClaimed"));
    }

    @Test
    public void testSecondClaimSameWibDateRejectedWithoutDuplicateCredit() throws Exception {
        facade.claimDailySponsor("REQ-SPON-01");
        Money cashAfterFirst = facade.getPersonalAccount().getTotalCash(Currency.IDR);

        // Coba klaim kedua dengan request baru pada tanggal WIB yang sama
        JSONObject res2 = facade.claimDailySponsor("REQ-SPON-02");
        assertFalse("Klaim kedua pada tanggal WIB sama harus ditolak", res2.getBoolean("success"));
        assertTrue(res2.getBoolean("alreadyClaimed"));

        Money cashAfterSecond = facade.getPersonalAccount().getTotalCash(Currency.IDR);
        assertEquals("Kas tidak boleh bertambah pada klaim kedua", cashAfterFirst, cashAfterSecond);
    }

    @Test
    public void testIdempotentRetrySameRequestId() throws Exception {
        JSONObject res1 = facade.claimDailySponsor("REQ-IDEMPOTENT-1");
        assertTrue(res1.getBoolean("success"));
        Money cash1 = facade.getPersonalAccount().getTotalCash(Currency.IDR);

        // Retry dengan requestId yang sama
        JSONObject res2 = facade.claimDailySponsor("REQ-IDEMPOTENT-1");
        assertTrue("Retry idempotent harus mengembalikan success", res2.getBoolean("success"));
        Money cash2 = facade.getPersonalAccount().getTotalCash(Currency.IDR);
        assertEquals("Retry tidak boleh menambah uang ganda", cash1, cash2);
    }

    @Test
    public void testGameTimeAdvancementAndSkipDayDoNotResetEligibility() throws Exception {
        facade.claimDailySponsor("REQ-SPON-01");
        Money cashAfterFirst = facade.getPersonalAccount().getTotalCash(Currency.IDR);

        // Majukan waktu game beberapa hari via skipDay dan tick
        facade.skipDay("SKIP-DAY-1");
        facade.skipDay("SKIP-DAY-2");
        facade.getClock().advanceGameSeconds(86400 * 5); // 5 hari game simulasi berlalu

        // Waktu simulasi sudah berpindah ke hari ke-8, TAPI waktu nyata WIB masih 22 Oktober 2026
        JSONObject status = facade.getDailySponsorStatus();
        assertFalse("Pergantian hari game simulasi TIDAK boleh membuka hak hibah pada tanggal nyata yang sama",
                status.getBoolean("eligible"));

        JSONObject res = facade.claimDailySponsor("REQ-SPON-ATTEMPT-2");
        assertFalse(res.getBoolean("success"));
        assertEquals(cashAfterFirst, facade.getPersonalAccount().getTotalCash(Currency.IDR));
    }

    @Test
    public void testCrossingMidnightWibUnlocksExactlyOneNewClaim() throws Exception {
        // Klaim pada 22 Oktober 2026 23:59:59 WIB
        ZonedDateTime justBeforeMidnight = ZonedDateTime.of(2026, 10, 22, 23, 59, 59, 0, WIB);
        testRealClock.setInstant(justBeforeMidnight.toInstant());

        JSONObject claim1 = facade.claimDailySponsor("REQ-DAY1");
        assertTrue(claim1.getBoolean("success"));
        Money cash1 = facade.getPersonalAccount().getTotalCash(Currency.IDR);

        // Majukan 1 detik: melewati tengah malam -> 23 Oktober 2026 00:00:00 WIB (= 17:00:00 UTC 22 Okt)
        testRealClock.advance(Duration.ofSeconds(1));

        JSONObject statusMidnight = facade.getDailySponsorStatus();
        assertTrue("Tepat 00.00 WIB hari berikutnya harus membuka hak klaim baru",
                statusMidnight.getBoolean("eligible"));
        assertEquals("Kas belum boleh dikredit otomatis tanpa aksi klaim",
                cash1, facade.getPersonalAccount().getTotalCash(Currency.IDR));

        // Klaim hari kedua
        JSONObject claim2 = facade.claimDailySponsor("REQ-DAY2");
        assertTrue("Klaim hari kedua pada 00.00 WIB harus sukses", claim2.getBoolean("success"));
        Money cash2 = facade.getPersonalAccount().getTotalCash(Currency.IDR);
        assertEquals(cash1.add(Money.of(25_000_000, Currency.IDR)), cash2);

        // Coba klaim ketiga di tanggal 23 Oktober
        JSONObject claim3 = facade.claimDailySponsor("REQ-DAY2-EXTRA");
        assertFalse("Klaim ketiga di tanggal 23 Oktober harus ditolak", claim3.getBoolean("success"));
    }

    @Test
    public void testTimeRewindProtection() throws Exception {
        // Klaim pada 22 Oktober 2026 23:30 WIB
        facade.claimDailySponsor("REQ-ORIGINAL");

        // Manipulasi jam perangkat dimundurkan ke 22 Oktober 2026 10:00 WIB
        ZonedDateTime rewoundTime = ZonedDateTime.of(2026, 10, 22, 10, 0, 0, 0, WIB);
        testRealClock.setInstant(rewoundTime.toInstant());

        JSONObject status = facade.getDailySponsorStatus();
        assertFalse("Jam dimundurkan tidak boleh membuka kembali hak klaim", status.getBoolean("eligible"));

        JSONObject res = facade.claimDailySponsor("REQ-REWOUND");
        assertFalse(res.getBoolean("success"));
    }

    @Test
    public void testPersistenceSaveAndLoadRoundtrip() throws Exception {
        facade.claimDailySponsor("REQ-SAVE-TEST");
        Money cashBefore = facade.getPersonalAccount().getTotalCash(Currency.IDR);
        facade.save();

        // Buat instance facade baru dan load
        TradingFacade reloaded = new TradingFacade(SaveManager.forDirectory(testDir));
        reloaded.setCatalog(facade.getCatalog());
        reloaded.setRealTimeClock(testRealClock);
        assertTrue(reloaded.load());

        JSONObject status = reloaded.getDailySponsorStatus();
        assertFalse("Setelah reload, status alreadyClaimed harus tetap bertahan", status.getBoolean("eligible"));
        assertTrue(status.getBoolean("alreadyClaimed"));

        JSONObject res = reloaded.claimDailySponsor("REQ-POST-RELOAD");
        assertFalse("Klaim setelah reload pada tanggal sama harus ditolak", res.getBoolean("success"));
        assertEquals(cashBefore, reloaded.getPersonalAccount().getTotalCash(Currency.IDR));
    }
}
