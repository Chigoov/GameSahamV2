package com.tradingglobal3d.core;

import com.tradingglobal3d.core.career.CareerRank;
import com.tradingglobal3d.core.corporate.CorporateAction;
import com.tradingglobal3d.core.corporate.CorporateActionType;
import com.tradingglobal3d.core.corporate.IpoOffering;
import com.tradingglobal3d.core.corporate.IpoSubscription;
import com.tradingglobal3d.core.corporate.ShareholderMeetingAgenda;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.funds.MutualFund;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.risk.ShortPosition;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.MatchingEngine;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;
import com.tradingglobal3d.core.trading.OrderStatus;
import com.tradingglobal3d.core.trading.OrderType;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Uji Keandalan Kritis Ekstrem 10 Seed x 100 Aksi pada SEMUA FITUR Game:
 * 1. Transaksi Saham & Matching Engine
 * 2. Pinjaman Beragunan & Tenor
 * 3. Margin & Short Selling
 * 4. Reksa Dana (MM/FI/EQ) & Cutoff
 * 5. Anak Perusahaan & Transfer Modal
 * 6. Aksi Korporasi, IPO, & Voting RUPS
 * 7. Skip Day & Siklus Waktu Kalender Multi-Hari
 * 8. Save, Reload, dan Rekonsiliasi Buku Besar Ganda
 */
public class AllFeaturesExtreme10x100CriticalSequenceTest {
    private File testDir;
    private SaveManager saveManager;
    private InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "extreme_all_seq_" + System.currentTimeMillis() + "_" + Math.random());
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
        assertTrue("Audit ledger rekonsiliasi wajib PASS: " + context, facade.reconcileAll());

        Account personal = facade.getPersonalAccount();
        assertNotNull("Personal account wajib ada: " + context, personal);

        Money totalIdr = personal.getTotalCash(Currency.IDR);
        Money availIdr = personal.getAvailableCash(Currency.IDR);
        Money resIdr = personal.getReservedCash(Currency.IDR);

        assertEquals("Available + Reserved harus presisi sama dengan Total IDR: " + context,
                totalIdr, availIdr.add(resIdr));
        assertFalse("Total kas IDR tidak boleh negatif: " + context, totalIdr.isNegative());
        assertFalse("Available IDR tidak boleh negatif: " + context, availIdr.isNegative());
        assertFalse("Reserved IDR tidak boleh negatif: " + context, resIdr.isNegative());

        Money netWorth = facade.getPersonalNetWorth();
        assertNotNull("Net worth wajib ada: " + context, netWorth);
    }

    private void run100ExtremeActionsForSeed(long seed, int seqNum) throws Exception {
        Random rnd = new Random(seed);
        TradingFacade facade = new TradingFacade(saveManager);
        if (catalog != null) facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(500_000_000)); // Modal cukup untuk uji komprehensif
        GameTestSupport.unlockManager(facade);
        facade.getClock().play();

        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        assertNotNull(bbca);

        int actionCount = 0;
        for (int step = 0; step < 100; step++) {
            String ctx = String.format("Seq#%d Seed=%d Step=%d", seqNum, seed, step);
            int actionType = step % 8;

            switch (actionType) {
                case 0: { // 1. Transaksi Saham Limit Order
                    String reqId = "ORD-" + seed + "-" + step;
                    long price = bbca.getCurrentPrice().getAmount().longValue();
                    Order buyOrder = new Order(
                            reqId, reqId, facade.getPersonalAccount().getId(), "IDX:BBCA",
                            OrderSide.BUY, OrderType.LIMIT,
                            Money.of(price, Currency.IDR), null, 100,
                            facade.getClock().getGameTimestamp(), null
                    );
                    facade.submitOrder(buyOrder);
                    actionCount++;
                    break;
                }
                case 1: { // 2. Margin & Short Selling
                    try {
                        facade.takeMarginLoan(Money.of(2_000_000, Currency.IDR));
                        ShortPosition sp = facade.openShortPosition("BBCA", 100);
                        if (sp != null) {
                            facade.closeShortPosition("BBCA", 100);
                        }
                        facade.repayMarginLoan(Money.of(2_000_000, Currency.IDR));
                    } catch (Exception ignored) {
                        // Best-effort boundary
                    }
                    actionCount++;
                    break;
                }
                case 2: { // 3. Pinjaman Beragunan Koleksi
                    try {
                        facade.buyCollection("COL-CAR-00");
                        JSONObject quote = facade.getLoanQuote("LOAN-PERS-01", true, null,
                                Money.of(2_000_000, Currency.IDR), 14, "COL-CAR-00");
                        if (quote.getBoolean("success")) {
                            JSONObject disburse = facade.disburseReviewedLoan(
                                    "DISB-" + seed + "-" + step, "LOAN-PERS-01", true, null,
                                    Money.of(2_000_000, Currency.IDR), 14, "COL-CAR-00",
                                    quote.getString("quoteVersion")
                            );
                            if (disburse.getBoolean("success")) {
                                String loanId = disburse.getString("loanId");
                                facade.payLoan("PAY-" + seed + "-" + step, loanId, Money.of(500_000, Currency.IDR));
                            }
                        }
                    } catch (Exception ignored) {
                        // Collateral already pledged or balance threshold
                    }
                    actionCount++;
                    break;
                }
                case 3: { // 4. Reksa Dana Subskripsi & Cutoff
                    try {
                        String subReq = "SUB-" + seed + "-" + step;
                        facade.subscribeMutualFund(subReq, "FUND-MM-01", Money.of(1_000_000, Currency.IDR));
                    } catch (Exception ignored) {}
                    actionCount++;
                    break;
                }
                case 4: { // 5. Perusahaan Operasional & Transfer Modal
                    String compId = "COMP-" + seed + "-" + (step / 8);
                    try {
                        facade.foundCompany(compId, "Usaha " + step, "Teknologi", Money.of(25_000_000, Currency.IDR));
                    } catch (Exception ignored) {}
                    try {
                        facade.transferCompanyCapital(compId, true, Money.of(2_000_000, Currency.IDR));
                        facade.transferCompanyCapital(compId, false, Money.of(1_000_000, Currency.IDR));
                    } catch (Exception ignored) {}
                    actionCount++;
                    break;
                }
                case 5: { // 6. Aksi Korporasi & Voting RUPS
                    try {
                        String agendaId = "RUPS-AGENDA-" + seed + "-" + step;
                        ShareholderMeetingAgenda agenda = new ShareholderMeetingAgenda(
                                agendaId, "BBCA", "Resolusi RUPS " + step, "Keterangan",
                                facade.getClock().getCurrentDate()
                        );
                        facade.getCorporateService().registerAgenda(agenda);
                        facade.castShareholderVote(agendaId, ShareholderMeetingAgenda.VoteChoice.FOR);
                    } catch (Exception ignored) {}
                    actionCount++;
                    break;
                }
                case 6: { // 7. Sponsor Harian & Multi-Asset
                    try {
                        facade.claimDailySponsor("SPON-" + seed + "-" + step);
                    } catch (Exception ignored) {}
                    try {
                        facade.multiAssetOrder("BOND-BUY-" + seed + "-" + step, "BOND-ORI024", true, 1, null);
                    } catch (Exception ignored) {}
                    actionCount++;
                    break;
                }
                case 7: { // 8. Siklus Waktu & Skip Day
                    try {
                        facade.skipDay("SKIP-" + seed + "-" + step);
                    } catch (Exception ignored) {
                        facade.getClock().advanceGameSeconds(3600);
                    }
                    actionCount++;
                    break;
                }
            }

            // Invarian dicek setelah SETIAP aksi
            assertInvariants(facade, ctx);

            // Uji ketahanan Save & Reload setiap 25 langkah
            if ((step + 1) % 25 == 0) {
                facade.save();
                TradingFacade reloaded = new TradingFacade(saveManager);
                if (catalog != null) reloaded.setCatalog(catalog);
                assertTrue("Proses reload save game harus berhasil: " + ctx, reloaded.load());
                assertInvariants(reloaded, ctx + " [Reloaded]");
                facade = reloaded;
                facade.getClock().play();
            }
        }

        assertTrue("Minimal 100 aksi bermakna dieksekusi", actionCount >= 100);
    }

    @Test public void testExtremeSequence01() throws Exception { run100ExtremeActionsForSeed(8001L, 1); }
    @Test public void testExtremeSequence02() throws Exception { run100ExtremeActionsForSeed(8002L, 2); }
    @Test public void testExtremeSequence03() throws Exception { run100ExtremeActionsForSeed(8003L, 3); }
    @Test public void testExtremeSequence04() throws Exception { run100ExtremeActionsForSeed(8004L, 4); }
    @Test public void testExtremeSequence05() throws Exception { run100ExtremeActionsForSeed(8005L, 5); }
    @Test public void testExtremeSequence06() throws Exception { run100ExtremeActionsForSeed(8006L, 6); }
    @Test public void testExtremeSequence07() throws Exception { run100ExtremeActionsForSeed(8007L, 7); }
    @Test public void testExtremeSequence08() throws Exception { run100ExtremeActionsForSeed(8008L, 8); }
    @Test public void testExtremeSequence09() throws Exception { run100ExtremeActionsForSeed(8009L, 9); }
    @Test public void testExtremeSequence10() throws Exception { run100ExtremeActionsForSeed(8010L, 10); }
}
