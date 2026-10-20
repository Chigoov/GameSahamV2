package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.storage.SaveManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Pengujian Kritis Standar A3 (PHASE-QUALITY-STANDARDS.md):
 * Minimal 10 variasi urutan yang dapat direproduksi, masing-masing minimal 100 aksi bermakna,
 * dengan invariant diperiksa setelah SETIAP aksi.
 * Cakupan: akun, kas, reserved cash, ledger, clock speed/tick, background/foreground, skip day, save/load, boundary/error recovery.
 */
public class DomainAccountSaveCriticalSequenceTest {
    private File testDir;
    private SaveManager saveManager;
    private com.tradingglobal3d.core.market.InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "critical_seq_test_" + System.currentTimeMillis());
        testDir.mkdirs();
        saveManager = SaveManager.forDirectory(testDir);

        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) {
            universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        }
        if (universeFile.exists()) {
            String jsonUniverse = new String(java.nio.file.Files.readAllBytes(universeFile.toPath()), java.nio.charset.StandardCharsets.UTF_8);
            catalog = com.tradingglobal3d.core.market.InstrumentCatalog.fromJsonString(jsonUniverse);
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
        Account personal = facade.getPersonalAccount();
        assertNotNull("Personal account wajib ada: " + context, personal);

        // 1. Double-entry ledger audit
        assertTrue("Ledger audit gagal pada: " + context, facade.reconcileAll());

        // 2. Saldo IDR: Available + Reserved == Total
        Money totalIdr = personal.getTotalCash(Currency.IDR);
        Money availIdr = personal.getAvailableCash(Currency.IDR);
        Money resIdr = personal.getReservedCash(Currency.IDR);
        assertEquals("Available + Reserved harus sama dengan Total IDR: " + context,
                totalIdr, availIdr.add(resIdr));

        // 3. Saldo USD: Available + Reserved == Total
        Money totalUsd = personal.getTotalCash(Currency.USD);
        Money availUsd = personal.getAvailableCash(Currency.USD);
        Money resUsd = personal.getReservedCash(Currency.USD);
        assertEquals("Available + Reserved harus sama dengan Total USD: " + context,
                totalUsd, availUsd.add(resUsd));

        // 4. Saldo tidak boleh negatif ilegal
        assertFalse("Total IDR tidak boleh negatif: " + context, totalIdr.isNegative());
        assertFalse("Available IDR tidak boleh negatif: " + context, availIdr.isNegative());
        assertFalse("Reserved IDR tidak boleh negatif: " + context, resIdr.isNegative());

        // 5. Waktu tidak boleh null
        assertNotNull("Clock wajib ada: " + context, facade.getClock());
        assertTrue("Remainder ms harus 0..999: " + context,
                facade.getClock().getGameMsRemainder() >= 0 && facade.getClock().getGameMsRemainder() < 1000);
    }

    private void run100ActionsForSeed(long seed, int sequenceNumber) throws Exception {
        TradingFacade facade = new TradingFacade(saveManager);
        if (catalog != null) facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        GameTestSupport.unlockManager(facade);

        Random rnd = new Random(seed);
        String pId = facade.getPersonalAccount().getId();
        facade.getClock().play();

        int actionsCount = 0;
        int step = 0;
        while (actionsCount < 100) {
            step++;
            int op = rnd.nextInt(12);
            String ctx = String.format("Seq %d (Seed %d) Step %d Op %d", sequenceNumber, seed, step, op);

            switch (op) {
                case 0: { // Deposit kas IDR
                    long amt = 100_000 + rnd.nextInt(5_000_000);
                    facade.deposit("DEP-" + seed + "-" + step, pId, Money.of(amt, Currency.IDR), "Deposit valid");
                    actionsCount++;
                    break;
                }
                case 1: { // Penarikan kas IDR (valid atau boundary)
                    long avail = facade.getPersonalAccount().getAvailableCash(Currency.IDR).getAmount().longValue();
                    if (avail > 500_000) {
                        long amt = 100_000 + rnd.nextInt((int) Math.min(avail, 2_000_000));
                        facade.withdraw("WIT-" + seed + "-" + step, pId, Money.of(amt, Currency.IDR), "Withdraw valid");
                        actionsCount++;
                    } else { // Uji batas over-withdrawal harus ditolak
                        try {
                            facade.withdraw("WIT-FAIL-" + seed + "-" + step, pId, Money.of(avail + 10_000_000, Currency.IDR), "Overdraw");
                            fail("Harus menolak withdraw melebihi saldo");
                        } catch (IllegalStateException expected) {
                            // Expected boundary rejection
                            actionsCount++;
                        }
                    }
                    break;
                }
                case 2: { // Reserve cash (simulasi order placing)
                    long avail = facade.getPersonalAccount().getAvailableCash(Currency.IDR).getAmount().longValue();
                    if (avail > 200_000) {
                        long amt = 100_000 + rnd.nextInt((int) Math.min(avail, 1_000_000));
                        facade.getPersonalAccount().reserveCash(Money.of(amt, Currency.IDR));
                        actionsCount++;
                    }
                    break;
                }
                case 3: { // Release reserved cash (simulasi order cancel/fill)
                    long res = facade.getPersonalAccount().getReservedCash(Currency.IDR).getAmount().longValue();
                    if (res > 0) {
                        long amt = 1 + (Math.abs(rnd.nextLong()) % res);
                        facade.getPersonalAccount().releaseReservedCash(Money.of(amt, Currency.IDR));
                        actionsCount++;
                    }
                    break;
                }
                case 4: { // Advance time (real seconds foreground)
                    int sec = 1 + rnd.nextInt(10);
                    facade.tick(sec);
                    actionsCount++;
                    break;
                }
                case 5: { // Change speed multiplier
                    int[] speeds = {1, 2, 5};
                    int s = speeds[rnd.nextInt(speeds.length)];
                    facade.getClock().setSpeedMultiplier(s);
                    actionsCount++;
                    break;
                }
                case 6: { // Pause / Play clock toggle
                    if (facade.getClock().isPaused()) facade.getClock().play();
                    else facade.getClock().pause();
                    actionsCount++;
                    break;
                }
                case 7: { // Transisi background dan offline reconciliation
                    facade.onAppLifecycleChanged(false); // Background
                    long offlineMs = 10_000 + rnd.nextInt(300_000); // 10s s/d 5 menit nyata
                    facade.reconcileOfflineElapsed(offlineMs);
                    facade.onAppLifecycleChanged(true); // Foreground
                    actionsCount++;
                    break;
                }
                case 8: { // Skip day dengan request ID
                    String reqId = "SKIP-CRIT-" + seed + "-" + step;
                    facade.skipDay(reqId);
                    // Idempotent retry
                    facade.skipDay(reqId);
                    actionsCount++;
                    break;
                }
                case 9: { // Save state
                    facade.save();
                    actionsCount++;
                    break;
                }
                case 10: { // Save dan Reload roundtrip
                    facade.save();
                    TradingFacade reloaded = new TradingFacade(saveManager);
                    if (catalog != null) reloaded.setCatalog(catalog);
                    assertTrue("Load harus sukses pada " + ctx, reloaded.load());
                    assertInvariants(reloaded, ctx + " [Reloaded]");
                    facade = reloaded;
                    actionsCount++;
                    break;
                }
                case 11: { // Uji batas deposit negatif ditolak
                    try {
                        facade.deposit("DEP-NEG-" + seed + "-" + step, pId, Money.of(-500_000, Currency.IDR), "Illegal");
                        fail("Deposit negatif harus ditolak: " + ctx);
                    } catch (IllegalArgumentException expected) {
                        actionsCount++;
                    }
                    break;
                }
            }

            // Invariant wajib diperiksa setelah SETIAP aksi
            assertInvariants(facade, ctx);
        }

        assertTrue("Minimal 100 aksi bermakna harus tercapai", actionsCount >= 100);
    }

    @Test
    public void testCriticalSequence01() throws Exception { run100ActionsForSeed(101101L, 1); }

    @Test
    public void testCriticalSequence02() throws Exception { run100ActionsForSeed(202202L, 2); }

    @Test
    public void testCriticalSequence03() throws Exception { run100ActionsForSeed(303303L, 3); }

    @Test
    public void testCriticalSequence04() throws Exception { run100ActionsForSeed(404404L, 4); }

    @Test
    public void testCriticalSequence05() throws Exception { run100ActionsForSeed(505505L, 5); }

    @Test
    public void testCriticalSequence06() throws Exception { run100ActionsForSeed(606606L, 6); }

    @Test
    public void testCriticalSequence07() throws Exception { run100ActionsForSeed(707707L, 7); }

    @Test
    public void testCriticalSequence08() throws Exception { run100ActionsForSeed(808808L, 8); }

    @Test
    public void testCriticalSequence09() throws Exception { run100ActionsForSeed(909909L, 9); }

    @Test
    public void testCriticalSequence10() throws Exception { run100ActionsForSeed(1010101L, 10); }
}
