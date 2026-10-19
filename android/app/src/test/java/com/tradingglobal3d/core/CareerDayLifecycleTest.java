package com.tradingglobal3d.core;

import com.tradingglobal3d.core.career.CareerMission;
import com.tradingglobal3d.core.career.CareerRank;
import com.tradingglobal3d.core.career.DailySessionReport;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
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

import static org.junit.Assert.*;

public class CareerDayLifecycleTest {
    private File testDir;
    private TradingFacade facade;
    private InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "career_test_" + System.currentTimeMillis());
        testDir.mkdirs();

        File dataDir = new File("src/main/assets/data");
        File universeFile = new File(dataDir, "market_universe.json");
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        catalog = InstrumentCatalog.fromJsonString(jsonUniverse);

        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
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

    private void advanceToClose() {
        facade.getClock().play();
        facade.getClock().tick((int) java.time.Duration.between(facade.getClock().getCurrentTime(), LocalTime.of(16, 1)).getSeconds());
        facade.getClock().pause();
    }

    @Test
    public void testEarlyDayCloseCannotAdvanceOrCreateSalary() {
        LocalDate date = facade.getClock().getCurrentDate();
        Money cash = facade.getPersonalAccount().getTotalCash(Currency.IDR);
        int entries = facade.getLedger().getEntries().size();
        try {
            facade.closeDayAndAdvance();
            fail("Cannot settle a day before the current domain closing boundary");
        } catch (IllegalStateException expected) {
            assertEquals(date, facade.getClock().getCurrentDate());
            assertEquals(cash, facade.getPersonalAccount().getTotalCash(Currency.IDR));
            assertEquals(entries, facade.getLedger().getEntries().size());
            assertEquals(1, facade.getCareerManager().getDaysTraded());
            assertTrue(facade.getCareerManager().getSessionReports().isEmpty());
        }
    }

    @Test
    public void testDayEndLoopAndAdvanceToNextDay() {
        LocalDate startDate = facade.getClock().getCurrentDate();
        Instrument bbca = catalog.getById("IDX:BBCA");
        assertNotNull(bbca);

        // Ubah harga dan volume untuk sesi hari ini
        bbca.updatePrice(Money.of(10500, Currency.IDR), 50000);
        assertTrue(bbca.getDayVolume() > 0);

        // Tutup hari bursa
        advanceToClose();
        DailySessionReport report = facade.closeDayAndAdvance();
        assertNotNull(report);
        assertEquals(startDate, report.getDate());

        // Tanggal harus maju 1 hari dan waktu kembali ke 08:30:00 (Pre-session briefing)
        assertEquals(startDate.plusDays(1), facade.getClock().getCurrentDate());
        assertEquals(LocalTime.of(8, 30, 0), facade.getClock().getCurrentTime());

        // Statistik harian saham harus di-reset untuk hari baru
        assertEquals(0, bbca.getDayVolume());
        assertEquals(Money.of(10500, Currency.IDR), bbca.getPreviousClosePrice());

        // Hari trading bertambah
        assertEquals(2, facade.getCareerManager().getDaysTraded());
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testReportDoesNotCountDepositAsInvestmentProfit() {
        facade.deposit("REPORT-CAPITAL", facade.getPersonalAccount().getId(), Money.of(1000000, Currency.IDR), "Capital inflow");
        advanceToClose();
        DailySessionReport report = facade.closeDayAndAdvance();
        assertEquals(Money.of(50_000_000, Currency.IDR), report.getStartingNetWorth());
        assertEquals(Money.of(51_000_000, Currency.IDR), report.getEndingNetWorth());
        assertEquals(Money.zero(Currency.IDR), report.getDailyPnL());
        assertEquals(0, report.getTotalFillsCount());
    }

    @Test
    public void testSessionReportsSurviveReloadWithoutRepeatingSettlement() throws Exception {
        facade.deposit("REPORT-FLOW", facade.getPersonalAccount().getId(), Money.of(1000000, Currency.IDR), "test");
        advanceToClose();
        DailySessionReport original = facade.closeDayAndAdvance();

        Money cash = facade.getPersonalAccount().getTotalCash(Currency.IDR);
        facade.save();
        TradingFacade restored = new TradingFacade(SaveManager.forDirectory(testDir));
        restored.setCatalog(catalog);
        assertTrue(restored.load());
        assertEquals(1, restored.getCareerManager().getSessionReports().size());
        DailySessionReport report = restored.getCareerManager().getSessionReports().get(0);
        assertEquals(original.getDate(), report.getDate());
        assertEquals(original.getStartingNetWorth(), report.getStartingNetWorth());
        assertEquals(original.getEndingNetWorth(), report.getEndingNetWorth());
        assertEquals(original.getDailyPnL(), report.getDailyPnL());
        assertEquals(original.getFeesPaid(), report.getFeesPaid());
        assertEquals(original.getSalaryReceived(), report.getSalaryReceived());
        assertEquals(original.getTotalFillsCount(), report.getTotalFillsCount());
        assertEquals(cash, restored.getPersonalAccount().getTotalCash(Currency.IDR));
    }

    @Test
    public void testIdempotentMissionRewardClaim() {
        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getTotalCash(Currency.IDR);

        // Tandai misi 1 selesai
        facade.getCareerManager().completeMission("MIS-01");
        CareerMission m1 = facade.getCareerManager().getMission("MIS-01");
        assertTrue(m1.isCompleted());
        assertFalse(m1.isClaimed());

        // Klaim reward pertama kali (Rp 2.500.000)
        Money reward = facade.claimMission("MIS-01");
        assertEquals(Money.of(2_500_000, Currency.IDR), reward);
        assertTrue(m1.isClaimed());
        assertEquals(initialCash.add(reward), personal.getTotalCash(Currency.IDR));

        // Coba klaim kedua kali (harus ditolak dengan exception, tidak boleh menduplikasi uang!)
        try {
            facade.claimMission("MIS-01");
            fail("Harus menolak klaim berulang untuk misi yang sama");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("sudah pernah diklaim"));
        }

        // Saldo tidak bertambah lagi
        assertEquals(initialCash.add(reward), personal.getTotalCash(Currency.IDR));
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testCareerProgressionAndSalary() {
        assertEquals(CareerRank.NOVICE_TRADER, facade.getCareerManager().getCurrentRank());

        // Selesaikan misi dan lewati hari agar memenuhi syarat Senior Analyst
        facade.getCareerManager().completeMission("MIS-01");
        facade.claimMission("MIS-01"); // reputasi naik ke 30

        advanceToClose();
        facade.closeDayAndAdvance(); // Day 2
        advanceToClose();
        facade.closeDayAndAdvance(); // Day 3

        // Evaluasi promosi ke Senior Analyst
        CareerRank promoted = facade.evaluateCareerPromotion();
        assertEquals(CareerRank.SENIOR_ANALYST, promoted);
        assertEquals(CareerRank.SENIOR_ANALYST, facade.getCareerManager().getCurrentRank());

        // Saat hari ditutup, Senior Analyst berhak atas gaji harian Rp 500.000
        Account personal = facade.getPersonalAccount();
        Money cashBeforeSalary = personal.getTotalCash(Currency.IDR);

        advanceToClose();
        facade.closeDayAndAdvance();
        try {
            facade.closeDayAndAdvance();
            fail("Repeated close must not settle the next morning");
        } catch (IllegalStateException expected) {
            assertEquals(cashBeforeSalary.add(Money.of(500_000, Currency.IDR)), personal.getTotalCash(Currency.IDR));
        }

        Money cashAfterSalary = personal.getTotalCash(Currency.IDR);
        assertEquals(cashBeforeSalary.add(Money.of(500_000, Currency.IDR)), cashAfterSalary);

        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testBailoutRecoveryWhenBroke() {
        Account personal = facade.getPersonalAccount();

        // Kondisi 1: Masih punya modal Rp 50.000.000 -> pengajuan bailout ditolak
        try {
            facade.requestBailoutRecovery();
            fail("Bailout harus ditolak saat modal masih banyak");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("hanya dapat diajukan jika modal kas di bawah"));
        }

        // Kondisi 2: Simulasikan kerugian telak / modal habis (debit kas sampai tersisa Rp 100.000)
        Money lossAmount = personal.getTotalCash(Currency.IDR).subtract(Money.of(100_000, Currency.IDR));
        facade.withdraw("REQ-SIM-LOSS", personal.getId(), lossAmount, "Kerugian trading besar");
        assertEquals(Money.of(100_000, Currency.IDR), personal.getTotalCash(Currency.IDR));

        int repBefore = facade.getCareerManager().getReputationScore();

        // Ajukan dana pemulihan modal darurat (Bailout)
        Money bailout = facade.requestBailoutRecovery();
        assertEquals(Money.of(10_000_000, Currency.IDR), bailout);

        // Saldo pulih ke Rp 10.100.000
        assertEquals(Money.of(10_100_000, Currency.IDR), personal.getTotalCash(Currency.IDR));
        assertTrue(facade.getCareerManager().isBailoutUsed());
        assertTrue(facade.getCareerManager().getReputationScore() < repBefore); // Penalti reputasi tercatat

        // Kondisi 3: Coba ajukan bailout kedua kali -> harus ditolak karena hanya berlaku sekali per karier
        try {
            facade.requestBailoutRecovery();
            fail("Bailout kedua kali harus ditolak");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("hanya dapat digunakan satu kali"));
        }

        assertTrue(facade.reconcileAll());
    }
}
