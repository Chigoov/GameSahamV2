package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.risk.RiskComplianceService;
import com.tradingglobal3d.core.risk.ShortPosition;
import com.tradingglobal3d.core.storage.SaveManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Pengujian Kritis Standar A3 untuk Phase 11 (PHASE-QUALITY-STANDARDS.md):
 * Minimal 10 variasi urutan yang dapat direproduksi, masing-masing minimal 100 aksi bermakna,
 * dengan invariant diperiksa setelah SETIAP aksi.
 * Cakupan: Pinjaman margin multi-currency (IDR & USD), short selling & collateral locked,
 * buy-to-cover, evaluasi margin health, forced liquidation, rollover bunga & borrow fee,
 * boundary pelunasan negatif / short ganda, dan save/load roundtrip.
 */
public class RiskMarginShortComplianceCriticalSequenceTest {
    private File testDir;
    private SaveManager saveManager;
    private InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "risk_crit_seq_" + System.currentTimeMillis());
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

        Money debtIdr = facade.getRiskService().getMarginDebt(personal.getId(), Currency.IDR);
        Money debtUsd = facade.getRiskService().getMarginDebt(personal.getId(), Currency.USD);
        assertFalse("Debt IDR negatif: " + context, debtIdr.isNegative());
        assertFalse("Debt USD negatif: " + context, debtUsd.isNegative());

        ShortPosition sp = facade.getRiskService().getShortPosition(personal.getId(), "BBCA");
        if (sp != null) {
            assertTrue("Collateral locked harus positif pada short position: " + context,
                    sp.getCollateralLocked().isPositive());
            assertTrue("Reserved cash minimal sebesar collateral locked: " + context,
                    personal.getReservedCash(Currency.IDR).isGreaterThanOrEqual(sp.getCollateralLocked()));
        }

        assertNotNull("Risk compliance service wajib ada: " + context, facade.getRiskService());
    }

    private void run100RiskActionsForSeed(long seed, int sequenceNumber) throws Exception {
        TradingFacade facade = new TradingFacade(saveManager);
        if (catalog != null) facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(100_000_000));
        GameTestSupport.unlockManager(facade);

        Account personal = facade.getPersonalAccount();
        Random rnd = new Random(seed);
        facade.getClock().play();

        int actionsCount = 0;
        int step = 0;

        while (actionsCount < 100) {
            step++;
            int op = rnd.nextInt(9);
            String ctx = String.format("Seq %d (Seed %d) Step %d Op %d", sequenceNumber, seed, step, op);

            switch (op) {
                case 0: { // Ambil pinjaman margin IDR (Rp 1.000.000 s.d. Rp 5.000.000)
                    Money loan = Money.of(1_000_000 + rnd.nextInt(5_000_000), Currency.IDR);
                    facade.takeMarginLoan(loan);
                    assertTrue(facade.getRiskService().getMarginDebt(personal.getId(), Currency.IDR).isPositive());
                    actionsCount++;
                    break;
                }
                case 1: { // Bayar pinjaman margin IDR
                    Money debt = facade.getRiskService().getMarginDebt(personal.getId(), Currency.IDR);
                    if (debt.isPositive()) {
                        Money avail = personal.getAvailableCash(Currency.IDR);
                        if (avail.isPositive()) {
                            long repayAmt = Math.min(debt.getAmount().longValue(), avail.getAmount().longValue() / 2);
                            if (repayAmt > 0) {
                                facade.repayMarginLoan(Money.of(repayAmt, Currency.IDR));
                                actionsCount++;
                            }
                        }
                    }
                    break;
                }
                case 2: { // Ambil pinjaman margin USD ($100 s.d. $500)
                    Money loanUsd = Money.of(100 + rnd.nextInt(400), Currency.USD);
                    facade.takeMarginLoan(loanUsd);
                    assertTrue(facade.getRiskService().getMarginDebt(personal.getId(), Currency.USD).isPositive());
                    actionsCount++;
                    break;
                }
                case 3: { // Buka short position BBCA jika belum ada
                    ShortPosition existing = facade.getRiskService().getShortPosition(personal.getId(), "BBCA");
                    if (existing == null) {
                        long avail = personal.getAvailableCash(Currency.IDR).getAmount().longValue();
                        if (avail >= 15_000_000) {
                            ShortPosition sp = facade.openShortPosition("BBCA", 500);
                            assertNotNull("Posisi short harus terbentuk", sp);
                            actionsCount++;
                        }
                    }
                    break;
                }
                case 4: { // Tutup short position BBCA jika ada
                    ShortPosition existing = facade.getRiskService().getShortPosition(personal.getId(), "BBCA");
                    if (existing != null) {
                        facade.closeShortPosition("BBCA", existing.getBorrowedShares());
                        assertNull("Posisi short harus tertutup", facade.getRiskService().getShortPosition(personal.getId(), "BBCA"));
                        actionsCount++;
                    }
                    break;
                }
                case 5: { // Evaluasi margin health
                    RiskComplianceService.MarginHealthReport report = facade.evaluateMarginHealth();
                    assertNotNull("Laporan margin health harus ada", report);
                    actionsCount++;
                    break;
                }
                case 6: { // Boundary: Tolak pelunasan margin bernilai negatif
                    try {
                        facade.repayMarginLoan(Money.of(-5000, Currency.IDR));
                        fail("Pelunasan margin dengan nilai negatif harus ditolak: " + ctx);
                    } catch (IllegalArgumentException expected) {
                        actionsCount++;
                    }
                    break;
                }
                case 7: { // Boundary: Tolak pembukaan short bertumpuk pada emiten yang sama
                    ShortPosition existing = facade.getRiskService().getShortPosition(personal.getId(), "BBCA");
                    if (existing != null) {
                        try {
                            facade.openShortPosition("BBCA", 100);
                            fail("Pembukaan short ganda tanpa cover harus ditolak: " + ctx);
                        } catch (IllegalStateException expected) {
                            actionsCount++;
                        }
                    } else {
                        // Advance time simulasi rollover bunga margin
                        facade.tick(60);
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
    public void testRiskSequence01() throws Exception { run100RiskActionsForSeed(11001L, 1); }

    @Test
    public void testRiskSequence02() throws Exception { run100RiskActionsForSeed(11002L, 2); }

    @Test
    public void testRiskSequence03() throws Exception { run100RiskActionsForSeed(11003L, 3); }

    @Test
    public void testRiskSequence04() throws Exception { run100RiskActionsForSeed(11004L, 4); }

    @Test
    public void testRiskSequence05() throws Exception { run100RiskActionsForSeed(11005L, 5); }

    @Test
    public void testRiskSequence06() throws Exception { run100RiskActionsForSeed(11006L, 6); }

    @Test
    public void testRiskSequence07() throws Exception { run100RiskActionsForSeed(11007L, 7); }

    @Test
    public void testRiskSequence08() throws Exception { run100RiskActionsForSeed(11008L, 8); }

    @Test
    public void testRiskSequence09() throws Exception { run100RiskActionsForSeed(11009L, 9); }

    @Test
    public void testRiskSequence10() throws Exception { run100RiskActionsForSeed(11010L, 10); }
}
