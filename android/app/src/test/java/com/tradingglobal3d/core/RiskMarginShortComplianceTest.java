package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.risk.ComplianceAuditLog;
import com.tradingglobal3d.core.risk.MarginCallStatus;
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
import java.util.List;

import static org.junit.Assert.*;

public class RiskMarginShortComplianceTest {
    private File testDir;
    private TradingFacade facade;
    private InstrumentCatalog catalog;
    private Instrument bbca;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "risk_test_" + System.currentTimeMillis());
        testDir.mkdirs();

        File dataDir = new File("src/main/assets/data");
        File universeFile = new File(dataDir, "market_universe.json");
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        catalog = InstrumentCatalog.fromJsonString(jsonUniverse);

        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));

        bbca = catalog.getById("IDX:BBCA");
        assertNotNull(bbca);
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
    public void testMarginLoanAndRepaymentCycle() {
        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getTotalCash(Currency.IDR);

        // 1. Tarik pinjaman margin Rp 20.000.000
        Money loan = Money.of(20_000_000, Currency.IDR);
        facade.takeMarginLoan(loan);

        assertEquals(initialCash.add(loan), personal.getTotalCash(Currency.IDR));
        assertEquals(loan, facade.getRiskService().getMarginDebt(personal.getId(), Currency.IDR));

        // 2. Lunasi pinjaman margin sebagian (Rp 10.000.000)
        Money partialRepay = Money.of(10_000_000, Currency.IDR);
        facade.repayMarginLoan(partialRepay);

        assertEquals(initialCash.add(Money.of(10_000_000, Currency.IDR)), personal.getTotalCash(Currency.IDR));
        assertEquals(Money.of(10_000_000, Currency.IDR), facade.getRiskService().getMarginDebt(personal.getId(), Currency.IDR));

        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testMultiCurrencyDebtAndInvalidRepaymentPreserveObligations() throws Exception {
        Account account = facade.getPersonalAccount();
        facade.takeMarginLoan(Money.of(1000000, Currency.IDR));
        facade.takeMarginLoan(Money.of(100, Currency.USD));
        assertEquals(Money.of(1000000, Currency.IDR), facade.getRiskService().getMarginDebt(account.getId(), Currency.IDR));
        assertEquals(Money.of(100, Currency.USD), facade.getRiskService().getMarginDebt(account.getId(), Currency.USD));
        try {
            facade.repayMarginLoan(Money.of(-1000, Currency.IDR));
            fail("Negative repayment must reject before debt changes");
        } catch (IllegalArgumentException expected) { }
        assertEquals(Money.of(1000000, Currency.IDR), facade.getRiskService().getMarginDebt(account.getId(), Currency.IDR));
        assertEquals(Money.of(50_000_000, Currency.IDR), facade.evaluateMarginHealth().getEquity());
        facade.save();
        TradingFacade loaded = new TradingFacade(SaveManager.forDirectory(testDir)); loaded.setCatalog(catalog);
        assertTrue(loaded.load());
        loaded.repayMarginLoan(Money.of(50, Currency.USD));
        assertEquals(Money.of(1000000, Currency.IDR), loaded.getRiskService().getMarginDebt(account.getId(), Currency.IDR));
        assertEquals(Money.of(50, Currency.USD), loaded.getRiskService().getMarginDebt(account.getId(), Currency.USD));
        assertEquals(Money.of(50_000_000, Currency.IDR), loaded.evaluateMarginHealth().getEquity());
        assertTrue(loaded.reconcileAll());
    }

    @Test
    public void testShortSellingJourneyProceedsLockedAndBuyToCover() {
        Account personal = facade.getPersonalAccount();
        Money initialAvailable = personal.getAvailableCash(Currency.IDR);

        // Buka short 1.000 lembar BBCA @ Rp 10.250 = Rp 10.250.000
        ShortPosition shortPos = facade.openShortPosition("BBCA", 1000);
        assertNotNull(shortPos);
        assertEquals(1000, shortPos.getBorrowedShares());

        // HASIL PENJUALAN SHORT DIKUNCI SEBAGAI JAMINAN (Collateral Locked) (FR-15.05)
        // Kas tersedia pemain TIDAK bertambah menjadi keuntungan bebas
        assertTrue(personal.getReservedCash(Currency.IDR).isPositive());
        assertEquals(shortPos.getCollateralLocked(), personal.getReservedCash(Currency.IDR));

        // Asumsikan harga BBCA turun ke Rp 9.000 (Pemain profit short)
        bbca.updatePrice(Money.of(9000, Currency.IDR), 0);

        // Lakukan Buy to Cover (Tutup posisi short 1.000 lembar)
        facade.closeShortPosition("BBCA", 1000);

        // Jaminan dilepas dan posisi short ditutup
        assertNull(facade.getRiskService().getShortPosition(personal.getId(), "BBCA"));
        assertEquals(Money.zero(Currency.IDR), personal.getReservedCash(Currency.IDR));

        // Saldo kas tersedia bertambah karena profit realized
        assertTrue(personal.getAvailableCash(Currency.IDR).compareTo(initialAvailable) > 0);

        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testSecondShortCannotOverwriteExistingObligation() {
        Account personal = facade.getPersonalAccount();
        ShortPosition first = facade.openShortPosition("BBCA", 1000);
        Money cash = personal.getTotalCash(Currency.IDR);
        Money collateral = personal.getReservedCash(Currency.IDR);
        try {
            facade.openShortPosition("BBCA", 100);
            fail("An existing loan cannot be silently replaced");
        } catch (IllegalStateException expected) {
            assertSame(first, facade.getRiskService().getShortPosition(personal.getId(), "BBCA"));
            assertEquals(cash, personal.getTotalCash(Currency.IDR));
            assertEquals(collateral, personal.getReservedCash(Currency.IDR));
        }
    }

    @Test
    public void testFailedShortCoverPreservesCollateralAndDebt() {
        Account personal = facade.getPersonalAccount();
        ShortPosition position = facade.openShortPosition("BBCA", 1000);
        Money cashBefore = personal.getTotalCash(Currency.IDR);
        Money collateralBefore = personal.getReservedCash(Currency.IDR);
        int entriesBefore = facade.getLedger().getEntries().size();
        bbca.updatePrice(Money.of(100000, Currency.IDR), 0);
        try {
            facade.closeShortPosition("BBCA", 1000);
            fail("Unaffordable cover must fail without partial settlement");
        } catch (IllegalStateException expected) {
            assertEquals(cashBefore, personal.getTotalCash(Currency.IDR));
            assertEquals(collateralBefore, personal.getReservedCash(Currency.IDR));
            assertEquals(collateralBefore, position.getCollateralLocked());
            assertEquals(1000, position.getBorrowedShares());
            assertEquals(entriesBefore, facade.getLedger().getEntries().size());
        }
    }

    @Test
    public void testMarginCallThresholdEvaluation() {
        Account personal = facade.getPersonalAccount();

        // Pemain meminjam margin Rp 40.000.000
        facade.takeMarginLoan(Money.of(40_000_000, Currency.IDR));

        // Beli saham dengan eksposur besar (8.000 lembar BBCA @ Rp 10.000 = Rp 80.000.000)
        // Gunakan kas untuk pembelian sehingga saldo kas tersisa Rp 10.000.000
        facade.getLedger().recordMutation(
                "BUY-TEST-EXP",
                personal,
                com.tradingglobal3d.core.economy.LedgerEntryType.TRADE_BUY,
                Money.of(80_000_000, Currency.IDR),
                false,
                System.currentTimeMillis(),
                "Pembelian saham eksposur margin"
        );
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 8000, BigDecimal.valueOf(10000), BigDecimal.valueOf(10000)));

        // Kondisi awal masih aman
        RiskComplianceService.MarginHealthReport reportHealthy = facade.evaluateMarginHealth();
        assertNotNull(reportHealthy);

        // Sekarang harga saham BBCA jatuh drastis dari 10.000 ke 4.500 (kerugian belum terealisasi masif)
        personal.getPosition("BBCA").updateMarketPrice(BigDecimal.valueOf(4500));

        RiskComplianceService.MarginHealthReport reportDistress = facade.evaluateMarginHealth();
        // Margin ratio turun drastis di bawah 30% -> Status MARGIN_CALL atau LIQUIDATION_REQUIRED (FR-15.06)
        assertTrue(reportDistress.getStatus() == MarginCallStatus.MARGIN_CALL ||
                reportDistress.getStatus() == MarginCallStatus.LIQUIDATION_REQUIRED);
    }

    @Test
    public void testForcedLiquidationEnforcesHaltRestrictions() {
        Account personal = facade.getPersonalAccount();

        // Pemain memiliki posisi BBCA 1.000 lembar
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 1000, BigDecimal.valueOf(10000), BigDecimal.valueOf(10250)));

        // 1. Simulasikan bursa sedang HALT / Suspensi Sirkuit (FR-16.07)
        facade.getMarketEngine().haltTrading("IDX:BBCA");
        assertTrue(facade.getMarketEngine().isHalted("IDX:BBCA"));

        // Percobaan likuidasi paksa saat HALT: DITAHAN (tidak dapat menembus batasan halt - FR-16.07)
        boolean executedDuringHalt = facade.executeForcedLiquidation("BBCA");
        assertFalse(executedDuringHalt);
        // Saham pemain tetap utuh karena tidak bisa dijual saat pasar suspend
        assertEquals(1000, personal.getPosition("BBCA").getQuantity());

        // Verifikasi audit log mencatat kepatuhan suspensi bursa
        List<ComplianceAuditLog> logs = facade.getRiskService().getAuditLogs();
        boolean haltAuditLogged = logs.stream().anyMatch(l ->
                l.getCategory() == ComplianceAuditLog.AuditCategory.HALT_ENFORCEMENT);
        assertTrue(haltAuditLogged);

        // 2. Sekarang bursa dibuka kembali (resume normal)
        facade.getMarketEngine().resumeTrading("IDX:BBCA");
        assertFalse(facade.getMarketEngine().isHalted("IDX:BBCA"));

        // Likuidasi paksa setelah bursa normal: Berhasil dieksekusi (FR-15.07)
        boolean executedNormal = facade.executeForcedLiquidation("BBCA");
        assertTrue(executedNormal);
        assertEquals(0, personal.getPosition("BBCA").getQuantity());

        assertTrue(facade.reconcileAll());
    }
}
