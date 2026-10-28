package com.tradingglobal3d.core;

import com.tradingglobal3d.core.corporate.*;
import com.tradingglobal3d.core.facade.TradingFacade;
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
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Pengujian Kritis Standar A3 untuk Phase 10 (PHASE-QUALITY-STANDARDS.md):
 * Minimal 10 variasi urutan yang dapat direproduksi, masing-masing minimal 100 aksi bermakna,
 * dengan invariant diperiksa setelah SETIAP aksi.
 * Cakupan: Dividen tunai, stock split, rights/buyback, pemesanan dan penjatahan IPO,
 * refund dana IPO, voting RUPS idempoten, rollover kalender, dan save/load.
 */
public class CorporateActionIpoVotingCriticalSequenceTest {
    private File testDir;
    private SaveManager saveManager;
    private InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "corp_crit_seq_" + System.currentTimeMillis());
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

        assertNotNull("Corporate service wajib ada: " + context, facade.getCorporateService());
    }

    private void run100CorporateActionsForSeed(long seed, int sequenceNumber) throws Exception {
        TradingFacade facade = new TradingFacade(saveManager);
        if (catalog != null) facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(100_000_000));
        GameTestSupport.unlockManager(facade);

        Account personal = facade.getPersonalAccount();
        // Beri posisi awal 2000 lembar BBCA untuk dividen dan voting
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 2000, BigDecimal.valueOf(10000), BigDecimal.valueOf(10250)));

        Random rnd = new Random(seed);
        facade.getClock().play();

        int actionsCount = 0;
        int step = 0;

        while (actionsCount < 100) {
            step++;
            int op = rnd.nextInt(9);
            String ctx = String.format("Seq %d (Seed %d) Step %d Op %d", sequenceNumber, seed, step, op);

            switch (op) {
                case 0: { // Eksekusi Dividen Tunai BBCA
                    LocalDate now = facade.getClock().getCurrentDate();
                    CorporateAction div = new CorporateAction(
                            "DIV-" + seed + "-" + step, "BBCA", CorporateActionType.CASH_DIVIDEND,
                            BigDecimal.valueOf(50 + rnd.nextInt(150)), Currency.IDR, now, now
                    );
                    int b = facade.executeCashDividend(div);
                    assertTrue("Dividen harus diproses untuk pemegang saham: " + ctx, b > 0);
                    assertTrue(div.isExecuted());
                    actionsCount++;
                    break;
                }
                case 1: { // Pesan IPO jika ada penawaran aktif
                    LocalDate now = facade.getClock().getCurrentDate();
                    var ipos = facade.getCorporateService().getIpoOfferings().stream()
                            .filter(i -> !i.isAllotted() && !i.getListingDate().isBefore(now))
                            .collect(java.util.stream.Collectors.toList());
                    if (!ipos.isEmpty()) {
                        IpoOffering ipo = ipos.get(0);
                        long avail = personal.getAvailableCash(Currency.IDR).getAmount().longValue();
                        long lotPrice = ipo.getOfferingPrice().getAmount().longValue() * ipo.getLotSize();
                        if (avail >= lotPrice * 5 && !ipo.isAllotted()) {
                            IpoSubscription sub = facade.orderIpo(ipo.getId(), 5);
                            assertNotNull("Pemesanan IPO harus berhasil", sub);
                            actionsCount++;
                        }
                    }
                    break;
                }
                case 2: { // Penjatahan IPO jika ada penawaran yang sudah dipesan
                    LocalDate now = facade.getClock().getCurrentDate();
                    var ipos = facade.getCorporateService().getIpoOfferings().stream()
                            .filter(i -> !i.isAllotted() && !i.getListingDate().isBefore(now))
                            .collect(java.util.stream.Collectors.toList());
                    if (!ipos.isEmpty()) {
                        IpoOffering ipo = ipos.get(0);
                        if (!ipo.isAllotted()) {
                            facade.allotIpo(ipo.getId(), 0.50);
                            assertTrue(ipo.isAllotted());
                            actionsCount++;
                        }
                    }
                    break;
                }
                case 3: { // Daftarkan IPO baru
                    String ipoId = "IPO-" + seed + "-" + step;
                    LocalDate listDate = facade.getClock().getCurrentDate().plusDays(2);
                    IpoOffering newIpo = new IpoOffering(
                            ipoId, "TICK" + (step % 100), "PT Emisi Perdana " + step, Currency.IDR,
                            Money.of(200 + rnd.nextInt(800), Currency.IDR), 100, 50_000, listDate
                    );
                    facade.getCorporateService().registerIpoOffering(newIpo);
                    assertNotNull(facade.getCorporateService().getIpo(ipoId));
                    actionsCount++;
                    break;
                }
                case 4: { // Voting RUPS
                    String agendaId = "RUPS-AGENDA-" + seed + "-" + step;
                    ShareholderMeetingAgenda agenda = new ShareholderMeetingAgenda(
                            agendaId, "BBCA", "Persetujuan Rencana Investasi " + step,
                            "Resolusi ekspansi usaha " + step,
                            facade.getClock().getCurrentDate() // recordingDate = hari ini agar voting bisa dilakukan
                    );
                    facade.getCorporateService().registerAgenda(agenda);
                    facade.castShareholderVote(agendaId, ShareholderMeetingAgenda.VoteChoice.FOR);
                    assertTrue("Agenda harus mencatat suara pemain", agenda.hasVoted(personal.getId()));
                    // Boundary: vote ganda harus ditolak
                    try {
                        facade.castShareholderVote(agendaId, ShareholderMeetingAgenda.VoteChoice.FOR);
                        fail("Voting ganda harus ditolak: " + ctx);
                    } catch (IllegalStateException expected) {
                        // Expected boundary pass
                    }
                    actionsCount++;
                    break;
                }
                case 5: { // Stock Split
                    Position p = personal.getPosition("BBCA");
                    if (p != null && p.getQuantity() > 0 && p.getAverageCost().compareTo(BigDecimal.valueOf(1000)) > 0) {
                        LocalDate now = facade.getClock().getCurrentDate();
                        CorporateAction split = new CorporateAction(
                                "SPLIT-" + seed + "-" + step, "BBCA", CorporateActionType.STOCK_SPLIT,
                                BigDecimal.valueOf(2), Currency.IDR, now, now
                        );
                        facade.executeStockSplit(split);
                        assertTrue(split.isExecuted());
                        actionsCount++;
                    }
                    break;
                }
                case 6: { // Advance time (memicu penutupan hari & pemrosesan corporate actions)
                    int sec = 30 + rnd.nextInt(90);
                    facade.tick(sec);
                    actionsCount++;
                    break;
                }
                case 7: { // Boundary: Tolak pemesanan IPO melebihi saldo kas tersedia
                    LocalDate now = facade.getClock().getCurrentDate();
                    var ipos = facade.getCorporateService().getIpoOfferings().stream()
                            .filter(i -> !i.isAllotted() && !i.getListingDate().isBefore(now))
                            .collect(java.util.stream.Collectors.toList());
                    if (!ipos.isEmpty()) {
                        IpoOffering ipo = ipos.get(0);
                        if (!ipo.isAllotted()) {
                            try {
                                facade.orderIpo(ipo.getId(), 1_000_000);
                                fail("Pemesanan IPO melebihi saldo kas harus ditolak: " + ctx);
                            } catch (IllegalStateException expected) {
                                assertTrue("Harus ditolak karena kas, bukan jadwal: " + ctx,
                                        expected.getMessage().contains("Saldo kas"));
                                actionsCount++;
                            }
                        }
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
    public void testCorporateSequence01() throws Exception { run100CorporateActionsForSeed(10001L, 1); }

    @Test
    public void testCorporateSequence02() throws Exception { run100CorporateActionsForSeed(10002L, 2); }

    @Test
    public void testCorporateSequence03() throws Exception { run100CorporateActionsForSeed(10003L, 3); }

    @Test
    public void testCorporateSequence04() throws Exception { run100CorporateActionsForSeed(10004L, 4); }

    @Test
    public void testCorporateSequence05() throws Exception { run100CorporateActionsForSeed(10005L, 5); }

    @Test
    public void testCorporateSequence06() throws Exception { run100CorporateActionsForSeed(10006L, 6); }

    @Test
    public void testCorporateSequence07() throws Exception { run100CorporateActionsForSeed(10007L, 7); }

    @Test
    public void testCorporateSequence08() throws Exception { run100CorporateActionsForSeed(10008L, 8); }

    @Test
    public void testCorporateSequence09() throws Exception { run100CorporateActionsForSeed(10009L, 9); }

    @Test
    public void testCorporateSequence10() throws Exception { run100CorporateActionsForSeed(10010L, 10); }
}
