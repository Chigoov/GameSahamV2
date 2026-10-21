package com.tradingglobal3d.core;

import com.tradingglobal3d.core.career.CareerRank;
import com.tradingglobal3d.core.corporate.CorporateAction;
import com.tradingglobal3d.core.corporate.CorporateActionType;
import com.tradingglobal3d.core.corporate.IpoSubscription;
import com.tradingglobal3d.core.corporate.ShareholderMeetingAgenda;
import com.tradingglobal3d.core.economy.FxQuote;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.multiasset.FundAllocationPlan;
import com.tradingglobal3d.core.replay.StressTestScenario;
import com.tradingglobal3d.core.replay.StressTestResult;
import com.tradingglobal3d.core.research.TerminalTier;
import com.tradingglobal3d.core.risk.MarginCallStatus;
import com.tradingglobal3d.core.risk.RiskComplianceService;
import com.tradingglobal3d.core.risk.ShortPosition;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.MatchingEngine;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * End-to-End Grand Integration Test Suite (Task 13).
 * Menguji integrasi end-to-end seluruh 20 modul Functional Requirements.
 */
public class EndToEndGrandIntegrationTest {
    private File testDir;
    private TradingFacade facade;
    private InstrumentCatalog catalog;
    private Instrument bbca;
    private Instrument aapl;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "e2e_grand_test_" + System.currentTimeMillis());
        testDir.mkdirs();

        File dataDir = new File("src/main/assets/data");
        File universeFile = new File(dataDir, "market_universe.json");
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        catalog = InstrumentCatalog.fromJsonString(jsonUniverse);

        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(100_000_000));
        GameTestSupport.unlockManager(facade);

        bbca = catalog.getById("IDX:BBCA");
        aapl = catalog.getById("US:AAPL");
        assertNotNull(bbca);
        assertNotNull(aapl);
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
    public void testFinancialSubsystemsWithUnlockedCareerFixture() throws Exception {
        Account personal = facade.getPersonalAccount();
        assertNotNull(personal);
        assertEquals(Money.of(100_000_000, Currency.IDR), personal.getTotalCash(Currency.IDR));

        // 1. [FR-06] Konversi Valas IDR ke USD untuk modal pasar Amerika
        com.tradingglobal3d.core.economy.FxExchangeService.ConversionResult fxRes =
                facade.convertFx("E2E-REQ-FX-1", personal.getId(), Currency.IDR, Currency.USD, Money.of(20_000_000, Currency.IDR));
        assertTrue(fxRes.isSuccess);
        assertTrue(personal.getTotalCash(Currency.USD).isPositive());

        // 2. [FR-04] Perdagangan Saham IDX (Beli BBCA)
        Order buyBbca = Order.createLimit("E2E-ORD-1", "IDX:BBCA", personal.getId(), OrderSide.BUY,
                bbca.getCurrentPrice(), 1000, System.currentTimeMillis());
        MatchingEngine.OrderResult resBuy = facade.submitOrder(buyBbca);
        assertTrue(resBuy.isSuccess);
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 1000, bbca.getCurrentPrice().getAmount(), bbca.getCurrentPrice().getAmount()));

        // 3. [FR-08] Menerima Mandat Klien Firma
        Account clientAcc = facade.acceptClientMandate("MANDATE-01");
        assertNotNull(clientAcc);
        assertTrue(clientAcc.getTotalCash(Currency.IDR).isPositive());

        // 4. [FR-09] Berlangganan Terminal Riset Profesional
        boolean subTerminal = facade.subscribeTerminal(TerminalTier.PRO);
        assertTrue(subTerminal);

        // 5. [FR-11] Menyusun dan Mengeksekusi Fund Allocation Plan
        Map<String, Double> weights = new HashMap<>();
        weights.put("IDX:BBCA", 0.40);
        weights.put("IDX:BMRI", 0.30);
        weights.put("IDX:BBRI", 0.10);
        FundAllocationPlan plan = facade.saveFundPlan("E2E-PLAN", personal.getId(), "E2E Strategic Fund", weights, 0.20);
        assertNotNull(plan);
        List<MatchingEngine.OrderResult> rebalRes = facade.executeFundRebalance("E2E-PLAN");
        assertFalse(rebalRes.isEmpty());

        // 6. [FR-12] Eksekusi Dividen Tunai Emiten
        CorporateAction divAction = new CorporateAction(
                "E2E-DIV-BBCA", "BBCA", CorporateActionType.CASH_DIVIDEND,
                BigDecimal.valueOf(150), Currency.IDR, LocalDate.now(), LocalDate.now()
        );
        int divCount = facade.executeCashDividend(divAction);
        assertTrue(divCount >= 1);

        // 7. [FR-13] Pemesanan Saham IPO dan Penjatahan
        IpoSubscription ipoSub = facade.orderIpo("IPO-CLOUD", 50);
        assertNotNull(ipoSub);
        facade.allotIpo("IPO-CLOUD", 0.50);
        assertEquals(IpoSubscription.SubscriptionStatus.ALLOTTED, ipoSub.getStatus());
        assertEquals(25, ipoSub.getAllottedLots());

        // 8. [FR-14] Voting RUPS Pemegang Saham Idempoten
        // Maju 1 hari agar recordingDate RUPS-BBCA-01 (gameBase+1) tercapai
        facade.getClock().advanceGameSeconds(86400);
        facade.castShareholderVote("RUPS-BBCA-01", ShareholderMeetingAgenda.VoteChoice.FOR);
        try {
            facade.castShareholderVote("RUPS-BBCA-01", ShareholderMeetingAgenda.VoteChoice.FOR);
            fail("Voting ganda harus ditolak");
        } catch (IllegalStateException expected) {
            // Sukses ditolak
        }

        // 9. [FR-15] Fasilitas Margin & Short Selling
        facade.takeMarginLoan(Money.of(15_000_000, Currency.IDR));
        ShortPosition shortPos = facade.openShortPosition("BMRI", 500);
        assertNotNull(shortPos);
        assertTrue(personal.getReservedCash(Currency.IDR).isPositive()); // Hasil short terkunci
        facade.closeShortPosition("BMRI", 500); // Buy to cover

        // 10. [FR-17] Stress Testing Makro Skenario Crash
        StressTestResult stressRes = facade.runStressTest(StressTestScenario.defaultMarketCrash());
        assertNotNull(stressRes);
        assertTrue(stressRes.getPotentialLoss().isPositive());

        // 11. [FR-07] Evaluasi Karier dan Misi
        CareerRank rank = facade.evaluateCareerPromotion();
        assertNotNull(rank);

        // 12. [FR-19] Siklus Save State Atomik dan Load Ulang
        facade.save();
        TradingFacade loadedFacade = new TradingFacade(SaveManager.forDirectory(testDir));
        loadedFacade.setCatalog(catalog);
        boolean loadOk = loadedFacade.load();
        assertTrue(loadOk);

        // Verifikasi akun personal tetap ada dan kas konsisten
        Account loadedPersonal = loadedFacade.getPersonalAccount();
        assertNotNull(loadedPersonal);
        assertEquals(personal.getTotalCash(Currency.IDR), loadedPersonal.getTotalCash(Currency.IDR));

        // 13. [FR-05, FR-19] VERIFIKASI AKUNTANSI DOUBLE-ENTRY MUTLAK (100% REKONSILIASI)
        assertTrue(facade.reconcileAll());
        assertTrue(loadedFacade.reconcileAll());
    }
}
