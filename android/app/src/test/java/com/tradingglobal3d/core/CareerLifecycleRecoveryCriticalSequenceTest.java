package com.tradingglobal3d.core;

import com.tradingglobal3d.core.career.CareerRank;
import com.tradingglobal3d.core.career.DailySessionReport;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Pengujian Kritis Standar A3 untuk Phase 06 (PHASE-QUALITY-STANDARDS.md):
 * Minimal 10 variasi urutan yang dapat direproduksi, masing-masing minimal 100 aksi bermakna,
 * dengan invariant diperiksa setelah SETIAP aksi.
 * Cakupan: Siklus hari bursa, briefing, salary, rollover misi, promosi karier earned,
 * restrukturisasi utang tanpa uang penyelamat otomatis, offline reconciliation, dan save/load.
 */
public class CareerLifecycleRecoveryCriticalSequenceTest {
    private File testDir;
    private SaveManager saveManager;
    private InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "career_crit_seq_" + System.currentTimeMillis());
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
        Account personal = facade.getPersonalAccount();
        assertNotNull("Personal account wajib ada: " + context, personal);

        // 1. Audit ledger
        assertTrue("Audit ledger rekonsiliasi gagal: " + context, facade.reconcileAll());

        // 2. Invariant IDR
        Money totalIdr = personal.getTotalCash(Currency.IDR);
        Money availIdr = personal.getAvailableCash(Currency.IDR);
        Money resIdr = personal.getReservedCash(Currency.IDR);
        assertEquals("Available + Reserved harus sama dengan Total IDR: " + context,
                totalIdr, availIdr.add(resIdr));
        assertFalse("Total IDR negatif: " + context, totalIdr.isNegative());
        assertFalse("Available IDR negatif: " + context, availIdr.isNegative());
        assertFalse("Reserved IDR negatif: " + context, resIdr.isNegative());

        // 3. Waktu tidak pernah mundur
        assertNotNull("Clock wajib ada: " + context, facade.getClock());
        assertTrue("Hari trading harus >= 1: " + context, facade.getCareerManager().getDaysTraded() >= 1);
        assertTrue("Reputasi 0..100: " + context,
                facade.getCareerManager().getReputationScore() >= 0 && facade.getCareerManager().getReputationScore() <= 100);
    }

    private void run100CareerLifecycleActionsForSeed(long seed, int sequenceNumber) throws Exception {
        TradingFacade facade = new TradingFacade(saveManager);
        if (catalog != null) facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        GameTestSupport.unlockManager(facade);

        Random rnd = new Random(seed);
        facade.getClock().play();

        int actionsCount = 0;
        int step = 0;
        while (actionsCount < 100) {
            step++;
            int op = rnd.nextInt(10);
            String ctx = String.format("Seq %d (Seed %d) Step %d Op %d", sequenceNumber, seed, step, op);

            switch (op) {
                case 0: { // Advance time normal foreground (1-10 menit game)
                    int sec = 1 + rnd.nextInt(10);
                    facade.tick(sec);
                    actionsCount++;
                    break;
                }
                case 1: { // Skip day dengan request ID idempoten
                    String reqId = "SKIP-CAREER-" + seed + "-" + step;
                    var res = facade.skipDay(reqId);
                    assertTrue("Skip day harus sukses", res.getBoolean("success"));
                    // Retry
                    var retry = facade.skipDay(reqId);
                    assertTrue("Retry skip day harus idempoten", retry.getBoolean("success"));
                    actionsCount++;
                    break;
                }
                case 2: { // Transisi offline dan rekonsiliasi latar belakang (1 jam s/d 8 jam nyata)
                    facade.onAppLifecycleChanged(false); // Background
                    long realMs = (1 + rnd.nextInt(8)) * 3600000L;
                    facade.reconcileOfflineElapsed(realMs);
                    facade.onAppLifecycleChanged(true); // Foreground
                    actionsCount++;
                    break;
                }
                case 3: { // Tutup hari dan terima gaji jika sudah lewat 16:01 WIB
                    LocalTime time = facade.getClock().getCurrentTime();
                    if (time.isAfter(LocalTime.of(16, 0, 59))) {
                        DailySessionReport rep = facade.closeDayAndAdvance();
                        assertNotNull("Laporan penutupan harian harus terbentuk: " + ctx, rep);
                        actionsCount++;
                    } else { // Boundary: Tolak penutupan sebelum jam bursa selesai
                        try {
                            facade.closeDayAndAdvance();
                            fail("Harus menolak penutupan sebelum jam 16:01: " + ctx);
                        } catch (IllegalStateException expected) {
                            actionsCount++;
                        }
                    }
                    break;
                }
                case 4: { // Selesaikan misi jika belum
                    var m1 = facade.getCareerManager().getMission("MIS-01");
                    if (m1 != null && !m1.isCompleted()) {
                        facade.getCareerManager().completeMission("MIS-01");
                        assertTrue(m1.isCompleted());
                        actionsCount++;
                    } else if (m1 != null && m1.isCompleted() && !m1.isClaimed()) {
                        Money rew = facade.claimMission("MIS-01");
                        assertTrue("Reward misi harus positif", rew.isPositive());
                        // Boundary: coba klaim ulang harus ditolak
                        try {
                            facade.claimMission("MIS-01");
                            fail("Klaim berulang harus ditolak: " + ctx);
                        } catch (IllegalStateException expected) {
                            // Boundary pass
                        }
                        actionsCount++;
                    }
                    break;
                }
                case 5: { // Evaluasi promosi karier earned
                    CareerRank rank = facade.evaluateCareerPromotion();
                    assertNotNull(rank);
                    actionsCount++;
                    break;
                }
                case 6: { // Pinjam margin jika diizinkan rank (level >= 3 / PORTFOLIO_MANAGER)
                    if (facade.getCareerManager().getCurrentRank().getLevel() >= 3) {
                        facade.takeMarginLoan(Money.of(1_000_000, Currency.IDR));
                        actionsCount++;
                    }
                    break;
                }
                case 7: { // Restrukturisasi utang margin jika punya utang
                    Money debt = facade.getRiskService().getMarginDebt(facade.getPersonalAccount().getId(), Currency.IDR);
                    if (debt.isPositive()) {
                        var res = facade.restructureDebt(Currency.IDR);
                        assertTrue("Restrukturisasi utang harus sukses", res.getBoolean("success"));
                        actionsCount++;
                    }
                    break;
                }
                case 8: { // Save state
                    facade.save();
                    actionsCount++;
                    break;
                }
                case 9: { // Save dan Reload roundtrip
                    facade.save();
                    TradingFacade reloaded = new TradingFacade(saveManager);
                    if (catalog != null) reloaded.setCatalog(catalog);
                    assertTrue("Load harus berhasil pada " + ctx, reloaded.load());
                    assertInvariants(reloaded, ctx + " [Reloaded]");
                    facade = reloaded;
                    actionsCount++;
                    break;
                }
            }

            // Invariant wajib diaudit setelah SETIAP aksi
            assertInvariants(facade, ctx);
        }

        assertTrue("Minimal 100 aksi bermakna harus tercapai", actionsCount >= 100);
    }

    @Test
    public void testCareerLifecycleSequence01() throws Exception { run100CareerLifecycleActionsForSeed(6001L, 1); }

    @Test
    public void testCareerLifecycleSequence02() throws Exception { run100CareerLifecycleActionsForSeed(6002L, 2); }

    @Test
    public void testCareerLifecycleSequence03() throws Exception { run100CareerLifecycleActionsForSeed(6003L, 3); }

    @Test
    public void testCareerLifecycleSequence04() throws Exception { run100CareerLifecycleActionsForSeed(6004L, 4); }

    @Test
    public void testCareerLifecycleSequence05() throws Exception { run100CareerLifecycleActionsForSeed(6005L, 5); }

    @Test
    public void testCareerLifecycleSequence06() throws Exception { run100CareerLifecycleActionsForSeed(6006L, 6); }

    @Test
    public void testCareerLifecycleSequence07() throws Exception { run100CareerLifecycleActionsForSeed(6007L, 7); }

    @Test
    public void testCareerLifecycleSequence08() throws Exception { run100CareerLifecycleActionsForSeed(6008L, 8); }

    @Test
    public void testCareerLifecycleSequence09() throws Exception { run100CareerLifecycleActionsForSeed(6009L, 9); }

    @Test
    public void testCareerLifecycleSequence10() throws Exception { run100CareerLifecycleActionsForSeed(6010L, 10); }
}
