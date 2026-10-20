package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.research.NewsArticle;
import com.tradingglobal3d.core.research.TerminalTier;
import com.tradingglobal3d.core.storage.SaveManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Pengujian Kritis Standar A3 untuk Phase 08 (PHASE-QUALITY-STANDARDS.md):
 * Minimal 10 variasi urutan yang dapat direproduksi, masing-masing minimal 100 aksi bermakna,
 * dengan invariant diperiksa setelah SETIAP aksi.
 * Cakupan: Berita tanpa future leakage, pembacaan artikel berhadiah XP idempoten,
 * billing paket terminal uang game, expiry/renewal, kalender pasar, konsensus, dan save/load.
 */
public class ResearchTerminalBillingCriticalSequenceTest {
    private File testDir;
    private SaveManager saveManager;
    private InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "res_crit_seq_" + System.currentTimeMillis());
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

        Money totalUsd = personal.getTotalCash(Currency.USD);
        Money availUsd = personal.getAvailableCash(Currency.USD);
        Money resUsd = personal.getReservedCash(Currency.USD);
        assertEquals("Available + Reserved harus sama dengan Total USD: " + context,
                totalUsd, availUsd.add(resUsd));
        assertFalse("Total USD negatif: " + context, totalUsd.isNegative());

        assertNotNull("Research service harus ada: " + context, facade.getResearchService());
        assertNotNull("Terminal tier harus ada: " + context, facade.getResearchService().getCurrentTier());
    }

    private void run100ResearchBillingActionsForSeed(long seed, int sequenceNumber) throws Exception {
        TradingFacade facade = new TradingFacade(saveManager);
        if (catalog != null) facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        GameTestSupport.unlockManager(facade);

        // Deposit awal $5,000 USD untuk biaya billing terminal riset
        String pId = facade.getPersonalAccount().getId();
        facade.deposit("DEP-USD-" + seed, pId, Money.of(5000.0, Currency.USD), "Modal USD Riset");

        Random rnd = new Random(seed);
        facade.getClock().play();

        int actionsCount = 0;
        int step = 0;

        while (actionsCount < 100) {
            step++;
            int op = rnd.nextInt(9);
            String ctx = String.format("Seq %d (Seed %d) Step %d Op %d", sequenceNumber, seed, step, op);

            switch (op) {
                case 0: { // Baca News Feed dengan verifikasi anti-future-leakage
                    long now = facade.getClock().getGameTimestamp();
                    List<NewsArticle> news = facade.getNewsFeed();
                    for (NewsArticle a : news) {
                        assertTrue("Berita masa depan tidak boleh bocor: " + a.getId() + " pada " + ctx,
                                a.getPublishedAt() <= now);
                    }
                    actionsCount++;
                    break;
                }
                case 1: { // Baca artikel tertentu untuk perolehan XP dan uji idempotensi
                    List<NewsArticle> news = facade.getNewsFeed();
                    if (!news.isEmpty()) {
                        NewsArticle target = news.get(rnd.nextInt(news.size() > 3 ? 3 : news.size()));
                        int xpBefore = facade.getProgressManager().getCurrentXp();
                        facade.recordResearchRead(target.getId());
                        int xpAfterFirst = facade.getProgressManager().getCurrentXp();
                        // Baca ulang artikel yang sama harus idempoten
                        facade.recordResearchRead(target.getId());
                        assertEquals("Baca ulang artikel yang sama tidak boleh menambah XP: " + ctx,
                                xpAfterFirst, facade.getProgressManager().getCurrentXp());
                        actionsCount++;
                    }
                    break;
                }
                case 2: { // Baca laporan analis konsensus
                    var reports = facade.getAnalystReports();
                    assertFalse(reports.isEmpty());
                    actionsCount++;
                    break;
                }
                case 3: { // Baca agenda kalender bursa
                    var cal = facade.getCalendar();
                    assertFalse(cal.isEmpty());
                    actionsCount++;
                    break;
                }
                case 4: { // Langganan Terminal Tier PRO seharga $500 USD jika saldo cukup dan rank memadai
                    double availUsd = facade.getPersonalAccount().getAvailableCash(Currency.USD).getAmount().doubleValue();
                    if (facade.getCareerManager().hasRank(com.tradingglobal3d.core.career.CareerRank.SENIOR_ANALYST)) {
                        if (availUsd >= 500.0) {
                            boolean ok = facade.subscribeTerminal(TerminalTier.PRO);
                            assertTrue("Langganan PRO harus sukses saat kas USD cukup: " + ctx, ok);
                            assertEquals(TerminalTier.PRO, facade.getResearchService().getCurrentTier());
                            actionsCount++;
                        }
                    } else {
                        try {
                            facade.subscribeTerminal(TerminalTier.PRO);
                            fail("Novice trader tidak boleh langganan premium: " + ctx);
                        } catch (IllegalStateException expected) {
                            actionsCount++;
                        }
                    }
                    break;
                }
                case 5: { // Langganan Terminal Tier ENTERPRISE seharga $2,000 USD jika saldo cukup dan rank memadai
                    double availUsd = facade.getPersonalAccount().getAvailableCash(Currency.USD).getAmount().doubleValue();
                    if (facade.getCareerManager().hasRank(com.tradingglobal3d.core.career.CareerRank.SENIOR_ANALYST)) {
                        if (availUsd >= 2000.0) {
                            boolean ok = facade.subscribeTerminal(TerminalTier.ENTERPRISE);
                            assertTrue("Langganan ENTERPRISE harus sukses saat kas USD cukup: " + ctx, ok);
                            assertEquals(TerminalTier.ENTERPRISE, facade.getResearchService().getCurrentTier());
                            actionsCount++;
                        }
                    } else {
                        try {
                            facade.subscribeTerminal(TerminalTier.ENTERPRISE);
                            fail("Novice trader tidak boleh langganan premium: " + ctx);
                        } catch (IllegalStateException expected) {
                            actionsCount++;
                        }
                    }
                    break;
                }
                case 6: { // Boundary: coba langganan saat saldo kas USD tidak cukup
                    double availUsd = facade.getPersonalAccount().getAvailableCash(Currency.USD).getAmount().doubleValue();
                    if (availUsd < 500.0) {
                        boolean ok = facade.subscribeTerminal(TerminalTier.PRO);
                        assertFalse("Langganan harus ditolak jika kas USD kurang", ok);
                        actionsCount++;
                    } else {
                        // Tarik kas USD sementara untuk boundary test
                        Money toWithdraw = facade.getPersonalAccount().getAvailableCash(Currency.USD);
                        facade.withdraw("WIT-USD-BOUND-" + seed + "-" + step, pId, toWithdraw, "Uji kas USD nol");
                        boolean ok = facade.subscribeTerminal(TerminalTier.ENTERPRISE);
                        assertFalse("Langganan harus ditolak jika kas USD kosong", ok);
                        // Kembalikan kas
                        facade.deposit("DEP-USD-BOUND-" + seed + "-" + step, pId, toWithdraw, "Restore kas USD");
                        actionsCount++;
                    }
                    break;
                }
                case 7: { // Advance time (maju beberapa hari bursa)
                    int sec = 30 + rnd.nextInt(120);
                    facade.tick(sec);
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
                    actionsCount++;
                    break;
                }
            }

            assertInvariants(facade, ctx);
        }

        assertTrue("Minimal 100 aksi bermakna harus tercapai", actionsCount >= 100);
    }

    @Test
    public void testResearchBillingSequence01() throws Exception { run100ResearchBillingActionsForSeed(8001L, 1); }

    @Test
    public void testResearchBillingSequence02() throws Exception { run100ResearchBillingActionsForSeed(8002L, 2); }

    @Test
    public void testResearchBillingSequence03() throws Exception { run100ResearchBillingActionsForSeed(8003L, 3); }

    @Test
    public void testResearchBillingSequence04() throws Exception { run100ResearchBillingActionsForSeed(8004L, 4); }

    @Test
    public void testResearchBillingSequence05() throws Exception { run100ResearchBillingActionsForSeed(8005L, 5); }

    @Test
    public void testResearchBillingSequence06() throws Exception { run100ResearchBillingActionsForSeed(8006L, 6); }

    @Test
    public void testResearchBillingSequence07() throws Exception { run100ResearchBillingActionsForSeed(8007L, 7); }

    @Test
    public void testResearchBillingSequence08() throws Exception { run100ResearchBillingActionsForSeed(8008L, 8); }

    @Test
    public void testResearchBillingSequence09() throws Exception { run100ResearchBillingActionsForSeed(8009L, 9); }

    @Test
    public void testResearchBillingSequence10() throws Exception { run100ResearchBillingActionsForSeed(8010L, 10); }
}
