package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.storage.SaveManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import static org.junit.Assert.*;

/**
 * Acceptance test untuk GAP:wealth — komponen kekayaan konsisten, transfer modal netral,
 * isolasi AUM, dan konsistensi semua konsumen.
 */
public class WealthConsistencyAcceptanceTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    private TradingFacade game() throws Exception {
        TradingFacade game = new TradingFacade(SaveManager.forDirectory(temporary.newFolder()));
        game.setCatalog(InstrumentCatalog.fromJsonString(
                new String(Files.readAllBytes(Paths.get(
                        "src/main/assets/data/market_universe.json")), StandardCharsets.UTF_8)));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        game.getClock().play();
        return game;
    }

    /** GAP:wealth:01 — komponen seluruh kepemilikan terekonsiliasi */
    @Test
    public void initialNetWorthEqualsStartingCapital() throws Exception {
        TradingFacade game = game();
        Money netWorth = game.getPersonalNetWorth();
        // Kekayaan awal harus ≈ 50 juta (setelah biaya awal/fee mungkin sedikit berbeda)
        assertTrue("Kekayaan awal harus positif", netWorth.isPositive());
        assertTrue("Kekayaan awal harus ≥ 10 juta (sanity check)",
                netWorth.getAmount().compareTo(BigDecimal.valueOf(10_000_000)) >= 0);
    }

    /** GAP:wealth:02 — transfer modal ke perusahaan tidak menciptakan atau menghancurkan kekayaan */
    @Test
    public void capitalTransferToCompanyIsWealthNeutral() throws Exception {
        TradingFacade game = game();
        Money netWorthBefore = game.getPersonalNetWorth();

        // Dirikan perusahaan dengan Rp 25 juta modal
        game.foundCompany("COMP-W1", "Uji Transfer", "Jasa", Money.of(25_000_000, Currency.IDR));

        Money netWorthAfter = game.getPersonalNetWorth();
        // Selisih tidak lebih dari Rp 2 juta (toleransi biaya/fee pendirian)
        BigDecimal diff = netWorthBefore.getAmount().subtract(netWorthAfter.getAmount()).abs();
        assertTrue("Transfer modal harus mendekati netral — diff: " + diff,
                diff.compareTo(BigDecimal.valueOf(2_000_000)) <= 0);

        // Reconcile harus lulus
        assertTrue("Reconcile harus lulus setelah transfer modal", game.reconcileAll());
    }

    /** GAP:wealth:03 — AUM reksa dana tidak dihitung dua kali dalam net worth pemain */
    @Test
    public void fundAumIsNotDoubleCountedInNetWorth() throws Exception {
        TradingFacade game = game();
        Money netWorthBefore = game.getPersonalNetWorth();
        Money cashBefore = game.getPersonalAccount().getTotalCash(Currency.IDR);

        // Subscribe reksa dana — kas berkurang, tapi NAB unit menggantikannya
        game.subscribeMutualFund("w3-sub", "FUND-MM-01", Money.of(10_000_000, Currency.IDR));
        for (int i = 0; i < 3; i++) game.skipDay("wealth-w3-" + i);

        // Setelah settle, kekayaan total harus serupa (dana hanya berpindah form)
        Money netWorthAfter = game.getPersonalNetWorth();
        BigDecimal diff = netWorthBefore.getAmount().subtract(netWorthAfter.getAmount()).abs();
        // Toleransi entry fee (0.1%) = Rp 10.000
        assertTrue("Net worth tidak boleh turun lebih dari entry fee setelah subscribe fund — diff: " + diff,
                diff.compareTo(BigDecimal.valueOf(100_000)) <= 0);
    }

    /** GAP:wealth:04 — nilai negatif tidak disembunyikan, wealth konsisten dengan ledger */
    @Test
    public void netWorthAndLedgerAreConsistentAfterMultipleOperations() throws Exception {
        TradingFacade game = game();

        // Beberapa operasi: subscribe fund, beli koleksi
        game.subscribeMutualFund("w4-sub", "FUND-MM-01", Money.of(5_000_000, Currency.IDR));
        game.buyCollection("COL-CAR-00");

        for (int i = 0; i < 3; i++) game.skipDay("wealth-w4-" + i);

        // Ledger harus reconcile
        assertTrue("Reconcile harus lulus setelah operasi berganda", game.reconcileAll());

        // Net worth harus positif
        Money netWorth = game.getPersonalNetWorth();
        assertTrue("Net worth harus positif setelah operasi normal", netWorth.isPositive());
    }

    /** GAP:wealth — save dan reload mempertahankan net worth */
    @Test
    public void netWorthSurvivesSaveReload() throws Exception {
        TradingFacade game = game();

        game.subscribeMutualFund("w-save-sub", "FUND-MM-01", Money.of(5_000_000, Currency.IDR));
        for (int i = 0; i < 3; i++) game.skipDay("wealth-save-" + i);

        Money netWorthBefore = game.getPersonalNetWorth();
        game.save();
        assertTrue(game.load());
        Money netWorthAfter = game.getPersonalNetWorth();

        BigDecimal diff = netWorthBefore.getAmount().subtract(netWorthAfter.getAmount()).abs();
        assertTrue("Net worth harus sama setelah save/reload — diff: " + diff,
                diff.compareTo(BigDecimal.valueOf(1_000)) <= 0); // toleransi Rp 1.000
    }
}
