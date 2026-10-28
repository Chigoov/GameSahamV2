package com.tradingglobal3d.core;

import com.tradingglobal3d.core.corporate.*;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.storage.SaveManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;

import static org.junit.Assert.*;

public class CorporateActionIpoVotingTest {
    private File testDir;
    private TradingFacade facade;
    private InstrumentCatalog catalog;
    private Instrument bbca;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "corp_test_" + System.currentTimeMillis());
        testDir.mkdirs();

        File dataDir = new File("src/main/assets/data");
        File universeFile = new File(dataDir, "market_universe.json");
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        catalog = InstrumentCatalog.fromJsonString(jsonUniverse);

        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        catalog = facade.getCatalog();

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
    public void testCashDividendDistribution() {
        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getTotalCash(Currency.IDR);

        // Berikan kepemilikan 1.000 lembar saham BBCA
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 1000, BigDecimal.valueOf(10000), BigDecimal.valueOf(10250)));

        // Aksi dividen tunai Rp 250 / lembar
        CorporateAction divAction = new CorporateAction(
                "ACT-DIV-BBCA-2026", "BBCA", CorporateActionType.CASH_DIVIDEND,
                BigDecimal.valueOf(250), Currency.IDR,
                LocalDate.now(), LocalDate.now()
        );

        long expectedBeneficiaries = facade.getAllAccounts().stream()
                .filter(a -> a.getPosition("IDX:BBCA") != null && a.getPosition("IDX:BBCA").getQuantity() > 0).count();
        int beneficiaries = facade.executeCashDividend(divAction);
        assertEquals(expectedBeneficiaries, beneficiaries);
        assertTrue(divAction.isExecuted());

        // Total dividen = 1000 lembar * Rp 250 = Rp 250.000
        Money expectedDividend = Money.of(250_000, Currency.IDR);
        assertEquals(initialCash.add(expectedDividend), personal.getTotalCash(Currency.IDR));

        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testStockSplitAdjustment() {
        Account personal = facade.getPersonalAccount();

        // Kepemilikan awal: 100 lembar BBCA @ Rp 10.000 (total biaya Rp 1.000.000)
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100, BigDecimal.valueOf(10000), BigDecimal.valueOf(10250)));
        Money initialTotalCost = personal.getPosition("BBCA").getTotalCost();

        // Aksi Stock Split 1:5 (Rasio 5.0)
        CorporateAction splitAction = new CorporateAction(
                "ACT-SPLIT-BBCA", "BBCA", CorporateActionType.STOCK_SPLIT,
                BigDecimal.valueOf(5), Currency.IDR,
                LocalDate.now(), LocalDate.now()
        );

        facade.executeStockSplit(splitAction);
        assertTrue(splitAction.isExecuted());

        // Posisi baru: jumlah lembar 500, average cost Rp 2.000
        Position postSplit = personal.getPosition("BBCA");
        assertEquals(500, postSplit.getQuantity());
        assertEquals(0, new BigDecimal("2000").compareTo(postSplit.getAverageCost()));

        // Total modal perolehan tetap sama persis (Rp 1.000.000)
        assertEquals(initialTotalCost, postSplit.getTotalCost());

        // Harga bursa disesuaikan turun 5x (10250 / 5 = 2050)
        assertEquals(Money.of(2050, Currency.IDR), bbca.getCurrentPrice());
    }

    @Test
    public void testIpoSubscriptionAllotmentAndRefund() {
        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getTotalCash(Currency.IDR);

        IpoOffering ipo = facade.getCorporateService().getIpoOfferings().stream()
                .filter(i -> !i.isAllotted() && !i.getListingDate().isBefore(facade.getClock().getCurrentDate()))
                .findFirst().orElseThrow(() -> new AssertionError("Katalog harus memuat IPO terbuka"));
        Money reservation = ipo.getOfferingPrice().multiply(BigDecimal.valueOf(100L * ipo.getLotSize()));
        Money allottedCost = ipo.getOfferingPrice().multiply(BigDecimal.valueOf(40L * ipo.getLotSize()));
        IpoSubscription sub = facade.orderIpo(ipo.getId(), 100);
        assertNotNull(sub);
        assertEquals(reservation, sub.getReservedMoney());

        // Reservasi mengikuti harga dan lot pada penawaran aktif.
        assertEquals(initialCash.subtract(reservation), personal.getAvailableCash(Currency.IDR));

        // Penjatahan 40% menghasilkan 40 lot; sisa reservasi menjadi refund.
        facade.allotIpo(ipo.getId(), 0.40);
        assertEquals(IpoSubscription.SubscriptionStatus.ALLOTTED, sub.getStatus());
        assertEquals(40, sub.getAllottedLots());
        assertEquals(reservation.subtract(allottedCost), sub.getRefundMoney());

        // Saham dari penawaran yang dipilih masuk ke portofolio pemain.
        Position ipoPos = personal.getPosition("IDX:" + ipo.getTicker());
        assertNotNull(ipoPos);
        assertEquals(40L * ipo.getLotSize(), ipoPos.getQuantity());

        // Hanya biaya lot terjatah yang mengurangi total kas.
        assertEquals(initialCash.subtract(allottedCost), personal.getTotalCash(Currency.IDR));
        assertEquals(Money.zero(Currency.IDR), personal.getReservedCash(Currency.IDR));

        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testShareholderVotingAndIdempotency() {
        Account personal = facade.getPersonalAccount();

        // Pemain memegang 500 lembar saham BBCA
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 500, BigDecimal.valueOf(10000), BigDecimal.valueOf(10250)));

        ShareholderMeetingAgenda agenda = facade.getCorporateService().getAgenda("RUPS-BBCA-01");
        assertNotNull(agenda);
        assertEquals(0, agenda.getTotalVotesFor());

        // Advance the game clock to the recording date, then freeze ownership before voting.
        facade.getClock().advanceGameSeconds(86400);
        assertEquals(agenda.getRecordingDate(), facade.getClock().getCurrentDate());
        agenda.captureEntitlements(java.util.Collections.singletonList(personal));

        // Pemain memberikan suara "FOR" (Setuju)
        facade.castShareholderVote("RUPS-BBCA-01", ShareholderMeetingAgenda.VoteChoice.FOR);
        assertEquals(500, agenda.getTotalVotesFor());
        assertTrue(agenda.hasVoted(personal.getId()));

        // Coba berikan suara kedua kali (harus ditolak dengan exception!)
        try {
            facade.castShareholderVote("RUPS-BBCA-01", ShareholderMeetingAgenda.VoteChoice.FOR);
            fail("Voting ganda harus ditolak");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("sudah pernah memberikan suara"));
        }

        // Total suara tetap 500, tidak berlipat ganda
        assertEquals(500, agenda.getTotalVotesFor());
    }

    @Test
    public void testAutomaticDividendAndIpoAllotmentOnDayClose() {
        Account personal = facade.getPersonalAccount();
        LocalDate today = facade.getClock().getCurrentDate();
        LocalDate tomorrow = today.plusDays(1);

        // 1. Kepemilikan awal BBCA 1.000 lembar
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 1000, BigDecimal.valueOf(10000), BigDecimal.valueOf(10250)));

        // 2. Jadwalkan Dividen Tunai BBCA Rp 200/saham yang jatuh tempo besok
        CorporateAction divAction = new CorporateAction(
                "ACT-AUTO-DIV-01", "BBCA", CorporateActionType.CASH_DIVIDEND,
                BigDecimal.valueOf(200), Currency.IDR, today, tomorrow
        );
        facade.getCorporateService().scheduleCorporateAction(divAction);

        // 3. Jadwalkan IPO NCLD dengan tanggal listing besok dan pesan 20 lot
        IpoOffering ipo = new IpoOffering(
                "IPO-AUTO-NCLD", "NCLD", "Nusantara Cloud Tech Tbk", Currency.IDR,
                Money.of(500, Currency.IDR), 100, 100_000, tomorrow
        );
        facade.getCorporateService().registerIpoOffering(ipo);
        IpoSubscription sub = facade.orderIpo("IPO-AUTO-NCLD", 20);
        assertNotNull(sub);
        assertEquals(Money.of(1_000_000, Currency.IDR), personal.getReservedCash(Currency.IDR));

        Money cashBefore = personal.getTotalCash(Currency.IDR);

        // 4. Majukan waktu dan jalankan penutupan hari
        facade.getClock().play();
        facade.getClock().tick((int) java.time.Duration.between(facade.getClock().getCurrentTime(), java.time.LocalTime.of(16, 1)).getSeconds());
        facade.getClock().pause();

        com.tradingglobal3d.core.career.DailySessionReport report = facade.closeDayAndAdvance();
        assertNotNull(report);
        assertEquals(tomorrow, facade.getClock().getCurrentDate());

        // 5. Verifikasi Dividen Tunai dieksekusi otomatis
        assertTrue(divAction.isExecuted());
        boolean divLedgerPresent = facade.getLedger().getEntries().stream().anyMatch(e ->
                e.getType() == com.tradingglobal3d.core.economy.LedgerEntryType.DIVIDEND &&
                e.getDescription().toLowerCase().contains("dividen"));
        assertTrue("Dividen harus tercatat di ledger", divLedgerPresent);

        // 6. Verifikasi IPO dialokasikan otomatis dan refund diproses
        assertTrue(ipo.isAllotted());
        assertEquals(IpoSubscription.SubscriptionStatus.ALLOTTED, sub.getStatus());
        assertEquals(10, sub.getAllottedLots()); // 50% dari 20 lot = 10 lot
        Position ncldPos = personal.getPosition("NCLD");
        assertNotNull("Saham IPO harus masuk portofolio pemain", ncldPos);
        assertEquals(1000, ncldPos.getQuantity());

        // Kas tertahan harus kembali 0
        assertEquals(Money.zero(Currency.IDR), personal.getReservedCash(Currency.IDR));

        // Rekonsiliasi seluruh akun di buku besar
        assertTrue(facade.reconcileAll());
    }
}
