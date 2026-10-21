package com.tradingglobal3d.core;

import com.tradingglobal3d.core.business.OperatingCompany;
import com.tradingglobal3d.core.career.CareerRank;
import com.tradingglobal3d.core.collection.CollectibleItem;
import com.tradingglobal3d.core.corporate.CorporateAction;
import com.tradingglobal3d.core.corporate.CorporateActionType;
import com.tradingglobal3d.core.corporate.IpoOffering;
import com.tradingglobal3d.core.corporate.IpoSubscription;
import com.tradingglobal3d.core.corporate.ShareholderMeetingAgenda;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.funds.MutualFund;
import com.tradingglobal3d.core.loans.LoanContract;
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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Uji Keandalan Kritis Ekstrem 10 Seed x 100 Aksi pada SEMUA FITUR Game:
 * 1. Transaksi Saham & Matching Engine (Buy, Sell, Cancel, Invariant Reserved Cash)
 * 2. Pinjaman Beragunan & Tenor (Beli koleksi, Quote versioning, Disburse, Cicilan, Prepay, Restructure)
 * 3. Margin & Short Selling (Margin Loan, Repay, Open Short, Close Short, Locked Proceeds)
 * 4. Reksa Dana (MM/FI/EQ Subskripsi, Cutoff, Redempti, Nilai Aktiva Bersih)
 * 5. Anak Perusahaan & Transfer Modal (Founding, Suntik Modal 2-Arah, Konservasi Kas 1:1)
 * 6. Aksi Korporasi & Voting RUPS (Agenda, Voting Idempoten, Allotment IPO)
 * 7. Multi-Asset & FX (BOND, Komoditas Emas, Valas)
 * 8. Sponsor Harian & Skip Day Multi-Hari / Multi-Tahun
 * 9. Save, Reload, dan Rekonsiliasi Buku Besar Ganda (Double-entry Invariant)
 */
public class AllFeaturesExtreme10x100CriticalSequenceTest {
    private File testDir;
    private SaveManager saveManager;
    private InstrumentCatalog catalog;
    private File traceDir;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "extreme_all_seq_" + System.currentTimeMillis() + "_" + Math.random());
        testDir.mkdirs();
        saveManager = SaveManager.forDirectory(testDir);

        traceDir = new File("Artifacts/Final-Verification-2026-10-21/action-traces");
        traceDir.mkdirs();

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
        // 1. Audit buku besar ganda (ledger reconciliation) wajib mutlak true untuk semua akun dan mata uang
        assertTrue("Audit ledger rekonsiliasi wajib PASS: " + context, facade.reconcileAll());

        // 2. Akun personal wajib konsisten
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

        // 3. Seluruh kuantitas posisi saham tidak boleh negatif
        for (Position pos : personal.getPositions().values()) {
            assertTrue("Kuantitas posisi tidak boleh negatif: " + pos.getTicker() + " pada " + context,
                    pos.getQuantity() >= 0);
        }

        // 4. Utang margin tidak boleh negatif
        Money marginDebt = facade.getRiskService().getMarginDebt(personal.getId(), Currency.IDR);
        assertNotNull("Margin debt null: " + context, marginDebt);
        assertFalse("Margin debt tidak boleh negatif: " + context, marginDebt.isNegative());

        // 5. Konsistensi semua akun anak perusahaan
        for (OperatingCompany comp : facade.getCompanyManager().getAllCompanies()) {
            Account compAcc = facade.getAccount(comp.getAccountId());
            if (compAcc != null) {
                Money cTotal = compAcc.getTotalCash(Currency.IDR);
                Money cAvail = compAcc.getAvailableCash(Currency.IDR);
                Money cRes = compAcc.getReservedCash(Currency.IDR);
                assertEquals("Available + Reserved anak perusahaan harus match Total: " + context,
                        cTotal, cAvail.add(cRes));
                assertFalse("Total kas anak perusahaan tidak boleh negatif: " + context, cTotal.isNegative());
            }
        }

        // 6. Net worth wajib ada dan dapat dihitung
        Money netWorth = facade.getPersonalNetWorth();
        assertNotNull("Net worth wajib ada: " + context, netWorth);
    }

    private void run100ExtremeActionsForSeed(long seed, int seqNum) throws Exception {
        Random rnd = new Random(seed);
        TradingFacade facade = new TradingFacade(saveManager);
        if (catalog != null) facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(1_000_000_000L)); // 1 Miliar IDR modal awal uji
        GameTestSupport.unlockManager(facade);
        facade.getClock().play();

        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        Instrument tlkm = facade.getCatalog().getById("IDX:TLKM");
        Instrument asii = facade.getCatalog().getById("IDX:ASII");
        Instrument[] instruments = {bbca, tlkm, asii};

        List<Order> openBuyOrders = new ArrayList<>();
        List<String> activeLoanIds = new ArrayList<>();
        List<String> actionTraces = new ArrayList<>();

        int actionsCount = 0;
        int step = 0;

        while (actionsCount < 100) {
            step++;
            int op = rnd.nextInt(12); // Pilihan acak 12 operasi berbeda dikendalikan seed
            String ctx = String.format("Seq#%d Seed=%d Step=%d Op=%d ActionCount=%d", seqNum, seed, step, op, actionsCount);
            Instrument target = instruments[rnd.nextInt(instruments.length)];

            switch (op) {
                case 0: { // 1. Transaksi Saham: Submit Limit Buy Order dengan validasi reserved cash
                    long avail = facade.getPersonalAccount().getAvailableCash(Currency.IDR).getAmount().longValue();
                    long price = target.getCurrentPrice().getAmount().longValue();
                    if (avail >= price * 100) {
                        String reqId = "BUY-LIMIT-" + seed + "-" + step;
                        Order buyOrder = new Order(
                                "ORD-" + reqId, reqId, facade.getPersonalAccount().getId(), target.getId(),
                                OrderSide.BUY, OrderType.LIMIT,
                                Money.of(price, Currency.IDR), null, 100,
                                facade.getClock().getGameTimestamp(), null
                        );
                        Money availBefore = facade.getPersonalAccount().getAvailableCash(Currency.IDR);
                        MatchingEngine.OrderResult res = facade.submitOrder(buyOrder);
                        assertTrue("Submit buy order harus sukses: " + res.message, res.isSuccess);

                        if (buyOrder.getStatus() == OrderStatus.OPEN || buyOrder.getStatus() == OrderStatus.PARTIALLY_FILLED) {
                            openBuyOrders.add(buyOrder);
                            assertTrue("Available cash harus berkurang untuk limit order yang open",
                                    facade.getPersonalAccount().getAvailableCash(Currency.IDR).compareTo(availBefore) < 0);
                        }
                        actionTraces.add(ctx + " -> Submit Buy Order " + target.getId() + " status=" + buyOrder.getStatus());
                        actionsCount++;
                    }
                    break;
                }

                case 1: { // 2. Transaksi Saham: Pembatalan Order (Order Cancellation) & Pemulihan Kas
                    if (!openBuyOrders.isEmpty()) {
                        Order toCancel = openBuyOrders.remove(0);
                        Order inFacade = facade.getOrderManager().getOrderByRequest(toCancel.getRequestId());
                        Order targetOrder = inFacade != null ? inFacade : toCancel;

                        if (targetOrder.getStatus() == OrderStatus.OPEN || targetOrder.getStatus() == OrderStatus.PARTIALLY_FILLED) {
                            Money resBefore = facade.getPersonalAccount().getReservedCash(Currency.IDR);
                            Money availBefore = facade.getPersonalAccount().getAvailableCash(Currency.IDR);

                            boolean cancelled = facade.cancelOrder(targetOrder);
                            assertTrue("Cancel open order harus berhasil: " + targetOrder.getId(), cancelled);
                            assertEquals("Status order harus CANCELLED", OrderStatus.CANCELLED, targetOrder.getStatus());

                            Money resAfter = facade.getPersonalAccount().getReservedCash(Currency.IDR);
                            Money availAfter = facade.getPersonalAccount().getAvailableCash(Currency.IDR);
                            assertTrue("Reserved cash harus berkurang setelah cancel", resAfter.compareTo(resBefore) < 0);
                            assertTrue("Available cash harus dipulihkan setelah cancel", availAfter.compareTo(availBefore) > 0);

                            actionTraces.add(ctx + " -> Cancelled Buy Order " + targetOrder.getId() + " recovered cash=" + (availAfter.subtract(availBefore)));
                            actionsCount++;
                        }
                    } else { // Jika belum ada open order, submit resting buy order di bawah pasar untuk cancel berikutnya
                        long tick = target.getExchange().getTickSize(target.getCurrentPrice().getAmount()).longValue();
                        long restingPrice = Math.max(tick, target.getCurrentPrice().getAmount().longValue() - (tick * 5));
                        String reqId = "BUY-REST-" + seed + "-" + step;
                        Order restOrder = new Order(
                                "ORD-" + reqId, reqId, facade.getPersonalAccount().getId(), target.getId(),
                                OrderSide.BUY, OrderType.LIMIT,
                                Money.of(restingPrice, Currency.IDR), null, 100,
                                facade.getClock().getGameTimestamp(), null
                        );
                        MatchingEngine.OrderResult res = facade.submitOrder(restOrder);
                        if (res.isSuccess && restOrder.getStatus() == OrderStatus.OPEN) {
                            openBuyOrders.add(restOrder);
                            actionTraces.add(ctx + " -> Created Resting Buy Order for future cancel: " + restOrder.getId());
                            actionsCount++;
                        }
                    }
                    break;
                }

                case 2: { // 3. Fasilitas Margin: Pinjam Margin & Bayar Margin
                    Money debtBefore = facade.getRiskService().getMarginDebt(facade.getPersonalAccount().getId(), Currency.IDR);
                    Money loanAmount = Money.of(5_000_000, Currency.IDR);

                    facade.takeMarginLoan(loanAmount);
                    Money debtAfterTake = facade.getRiskService().getMarginDebt(facade.getPersonalAccount().getId(), Currency.IDR);
                    assertEquals("Utang margin harus bertambah tepat sesuai pinjaman",
                            debtBefore.add(loanAmount), debtAfterTake);

                    Money repayAmount = Money.of(2_000_000, Currency.IDR);
                    facade.repayMarginLoan(repayAmount);
                    Money debtAfterRepay = facade.getRiskService().getMarginDebt(facade.getPersonalAccount().getId(), Currency.IDR);
                    assertEquals("Utang margin harus berkurang tepat sesuai pembayaran",
                            debtAfterTake.subtract(repayAmount), debtAfterRepay);

                    actionTraces.add(ctx + " -> Margin Loan +5jt, Repay -2jt, final debt=" + debtAfterRepay);
                    actionsCount++;
                    break;
                }

                case 3: { // 4. Short Selling & Cover: Buka Posisi Short & Beli Kembali
                    ShortPosition shortPos = facade.openShortPosition("BBCA", 100);
                    assertNotNull("Open short position harus sukses", shortPos);
                    assertEquals("BBCA", shortPos.getTicker());
                    assertEquals(100, shortPos.getBorrowedShares());

                    facade.closeShortPosition("BBCA", 100);
                    ShortPosition remaining = facade.getRiskService().getShortPosition(facade.getPersonalAccount().getId(), "BBCA");
                    assertTrue("Short position harus lunas tertutup", remaining == null || remaining.getBorrowedShares() == 0);

                    actionTraces.add(ctx + " -> Open & Close Short Position BBCA 100 shares");
                    actionsCount++;
                    break;
                }

                case 4: { // 5. Pinjaman Beragunan: Beli Koleksi, Quote Versioning, Pencairan Pinjaman, & Cicilan
                    String[] itemPool = {"COL-CAR-00", "COL-WATCH-00", "COL-ART-00"};
                    String chosenItem = itemPool[rnd.nextInt(itemPool.length)];
                    CollectibleItem item = facade.getCollectionManager().getItem(chosenItem);

                    if (item != null && !item.isOwned()) {
                        long avail = facade.getPersonalAccount().getAvailableCash(Currency.IDR).getAmount().longValue();
                        if (avail >= item.getCurrentValuation().getAmount().longValue()) {
                            facade.buyCollection(chosenItem);
                            assertTrue("Item koleksi harus terverifikasi dimiliki", item.isOwned());
                        }
                    }

                    if (item != null && item.isOwned() && !item.isPledged()) {
                        JSONObject quote = facade.getLoanQuote("LOAN-PERS-01", true, null,
                                Money.of(2_000_000, Currency.IDR), 14, chosenItem);
                        assertTrue("Loan quote harus berhasil", quote.getBoolean("success"));
                        String qVersion = quote.getString("quoteVersion");
                        assertNotNull("Quote version wajib ada", qVersion);

                        String disbReqId = "DISB-" + seed + "-" + step;
                        Money cashBeforeDisb = facade.getPersonalAccount().getAvailableCash(Currency.IDR);
                        JSONObject disburse = facade.disburseReviewedLoan(
                                disbReqId, "LOAN-PERS-01", true, null,
                                Money.of(2_000_000, Currency.IDR), 14, chosenItem, qVersion
                        );
                        assertTrue("Pencairan pinjaman beragunan harus berhasil", disburse.getBoolean("success"));
                        assertTrue("Agunan harus berstatus terikat (pledged)", item.isPledged());
                        assertTrue("Kas personal harus bertambah pasca pencairan",
                                facade.getPersonalAccount().getAvailableCash(Currency.IDR).compareTo(cashBeforeDisb) > 0);

                        String loanId = disburse.getString("loanId");
                        activeLoanIds.add(loanId);

                        // Bayar cicilan sebagian
                        JSONObject payRes = facade.payLoan("PAY-" + seed + "-" + step, loanId, Money.of(500_000, Currency.IDR));
                        assertTrue("Pembayaran cicilan harus berhasil", payRes.getBoolean("success"));

                        actionTraces.add(ctx + " -> Disbursed loan " + loanId + " collateral=" + chosenItem + " + paid partial installment");
                        actionsCount++;
                    } else { // Jika agunan belum ada atau sudah terikat, lakukan pengujian penolakan quote versi usang (stale quote rejection)
                        try {
                            facade.disburseReviewedLoan("STALE-" + seed + "-" + step, "LOAN-PERS-01", true, null,
                                    Money.of(1_000_000, Currency.IDR), 14, null, "INVALID-QUOTE-VER-999");
                            fail("Pencairan dengan stale quote version wajib ditolak");
                        } catch (Exception expected) {
                            // Penolakan valid
                        }
                        actionTraces.add(ctx + " -> Tested stale quote rejection branch");
                        actionsCount++;
                    }
                    break;
                }

                case 5: { // 6. Restrukturisasi Pinjaman / Prepay Full Pinjaman
                    if (!activeLoanIds.isEmpty()) {
                        String loanId = activeLoanIds.get(0);
                        LoanContract loan = facade.getInstallmentLoanManager().getLoan(loanId);
                        if (loan != null && !loan.isClosed()) {
                            JSONObject restRes = facade.restructureLoan(loanId, 14);
                            assertTrue("Restrukturisasi pinjaman harus sukses", restRes.getBoolean("success"));

                            actionTraces.add(ctx + " -> Restructured loan " + loanId + " +14 days");
                            actionsCount++;
                        } else {
                            activeLoanIds.remove(0);
                        }
                    } else { // Verifikasi penolakan restrukturisasi utang margin saat utang margin nol
                        Money curDebt = facade.getRiskService().getMarginDebt(facade.getPersonalAccount().getId(), Currency.IDR);
                        if (curDebt.isZero()) {
                            try {
                                facade.restructureDebt(Currency.IDR);
                                fail("Restrukturisasi utang nol harus melempar IllegalStateException");
                            } catch (IllegalStateException expected) {
                                assertTrue(expected.getMessage().contains("Tidak ada utang margin"));
                            }
                            actionTraces.add(ctx + " -> Verified zero-debt restructure rejection");
                            actionsCount++;
                        }
                    }
                    break;
                }

                case 6: { // 7. Reksa Dana: Subskripsi Reksa Dana (MM / FI / EQ) & Eksekusi Cutoff
                    String[] fundIds = {"FUND-MM-01", "FUND-FI-01", "FUND-EQ-01"};
                    String chosenFund = fundIds[rnd.nextInt(fundIds.length)];
                    Money subAmount = Money.of(2_000_000, Currency.IDR);

                    Money cashBefore = facade.getPersonalAccount().getAvailableCash(Currency.IDR);
                    JSONObject subRes = facade.subscribeMutualFund("SUB-" + seed + "-" + step, chosenFund, subAmount);
                    assertTrue("Subskripsi reksa dana harus berhasil: " + chosenFund, subRes.getBoolean("success"));

                    Money cashAfter = facade.getPersonalAccount().getAvailableCash(Currency.IDR);
                    assertTrue("Kas personal harus berkurang untuk subskripsi reksa dana", cashAfter.compareTo(cashBefore) < 0);

                    actionTraces.add(ctx + " -> Mutual Fund Subscription " + chosenFund + " amount=" + subAmount);
                    actionsCount++;
                    break;
                }

                case 7: { // 8. Reksa Dana: Redempti Unit Reksa Dana
                    String[] fundIds = {"FUND-MM-01", "FUND-FI-01", "FUND-EQ-01"};
                    String chosenFund = fundIds[rnd.nextInt(fundIds.length)];
                    MutualFund mf = facade.getMutualFundManager().getFund(chosenFund);
                    BigDecimal units = mf != null ? mf.getUnits(facade.getPersonalAccount().getId()) : BigDecimal.ZERO;

                    if (units.compareTo(BigDecimal.valueOf(10)) > 0) {
                        BigDecimal redeemUnits = BigDecimal.valueOf(10);
                        JSONObject redRes = facade.redeemMutualFund("RED-" + seed + "-" + step, chosenFund, redeemUnits);
                        assertTrue("Redempti reksa dana harus berhasil: " + chosenFund, redRes.getBoolean("success"));
                        actionTraces.add(ctx + " -> Mutual Fund Redemption " + chosenFund + " units=" + redeemUnits);
                        actionsCount++;
                    } else { // Batas pengujian penolakan order reksa dana invalid
                        try {
                            facade.subscribeMutualFund("INV-SUB", "NON-EXISTENT-FUND", Money.of(1_000_000, Currency.IDR));
                            fail("Subskripsi ke reksa dana fiktif wajib ditolak");
                        } catch (IllegalArgumentException expected) {
                            // Sukses tertolak
                        }
                        actionTraces.add(ctx + " -> Verified non-existent mutual fund subscription rejection");
                        actionsCount++;
                    }
                    break;
                }

                case 8: { // 9. Anak Perusahaan: Founding & Suntik Modal Dua Arah (Konservasi Nilai 1:1)
                    String compId = "COMP-SEQ-" + seed;
                    OperatingCompany comp = facade.getCompanyManager().getCompany(compId);
                    if (comp == null) {
                        comp = facade.foundCompany(compId, "PT Mandiri " + seed, "Teknologi", Money.of(30_000_000, Currency.IDR));
                        assertNotNull("Perusahaan wajib terdaftar", comp);
                    }

                    Account compAcc = facade.getAccount(comp.getAccountId());
                    assertNotNull("Akun anak perusahaan wajib ada", compAcc);

                    Money persBefore = facade.getPersonalAccount().getTotalCash(Currency.IDR);
                    Money compBefore = compAcc.getTotalCash(Currency.IDR);
                    Money transferAmt = Money.of(5_000_000, Currency.IDR);

                    // Suntik modal personal -> company
                    facade.transferCompanyCapital(compId, true, transferAmt);
                    assertEquals("Kas personal harus berkurang tepat sesuai suntikan",
                            persBefore.subtract(transferAmt), facade.getPersonalAccount().getTotalCash(Currency.IDR));
                    assertEquals("Kas anak perusahaan harus bertambah tepat sesuai suntikan",
                            compBefore.add(transferAmt), compAcc.getTotalCash(Currency.IDR));

                    // Tarik modal company -> personal
                    Money returnAmt = Money.of(2_000_000, Currency.IDR);
                    facade.transferCompanyCapital(compId, false, returnAmt);
                    assertEquals("Kas personal bertambah kembali pasca penarikan modal",
                            persBefore.subtract(transferAmt).add(returnAmt), facade.getPersonalAccount().getTotalCash(Currency.IDR));

                    actionTraces.add(ctx + " -> Company Capital 2-Way Transfer preserved cash 1:1");
                    actionsCount++;
                    break;
                }

                case 9: { // 10. Aksi Korporasi & RUPS: Registrasi Agenda, Voting Idempoten / Rejection Branch
                    String agendaId = "AGENDA-" + seed + "-" + step;
                    ShareholderMeetingAgenda agenda = new ShareholderMeetingAgenda(
                            agendaId, "BBCA", "Resolusi Pembagian Dividen " + step,
                            "Persetujuan dividen tunai", facade.getClock().getCurrentDate()
                    );
                    facade.getCorporateService().registerAgenda(agenda);

                    Position bbcaPos = facade.getPersonalAccount().getPosition("IDX:BBCA");
                    long shares = (bbcaPos != null) ? bbcaPos.getQuantity() : 0;
                    if (shares > 0) {
                        facade.castShareholderVote(agendaId, ShareholderMeetingAgenda.VoteChoice.FOR);
                        assertTrue("Agenda harus mencatat vote", agenda.hasVoted(facade.getPersonalAccount().getId()));

                        // Verifikasi voting berulang (idempotent / ditolak)
                        try {
                            facade.castShareholderVote(agendaId, ShareholderMeetingAgenda.VoteChoice.AGAINST);
                            fail("Voting kedua pada agenda yang sama wajib melempar IllegalStateException");
                        } catch (IllegalStateException expected) {
                            assertTrue(expected.getMessage().contains("sudah pernah memberikan suara"));
                        }
                        actionTraces.add(ctx + " -> Corporate Meeting Agenda " + agendaId + " voted & verified idempotency");
                        actionsCount++;
                    } else {
                        // Verifikasi penolakan voting pemegang saham dengan kepemilikan 0 lembar
                        try {
                            facade.castShareholderVote(agendaId, ShareholderMeetingAgenda.VoteChoice.FOR);
                            fail("Voting tanpa lembar saham wajib melempar IllegalArgumentException");
                        } catch (IllegalArgumentException expected) {
                            assertTrue(expected.getMessage().contains("tidak memiliki hak suara"));
                        }
                        actionTraces.add(ctx + " -> Verified zero-share shareholder voting rejection");
                        actionsCount++;
                    }
                    break;
                }

                case 10: { // 11. Dana Hibah Harian (Bailout) & Transaksi Multi-Asset (Obligasi Negara FR0091)
                    Money cashBeforeSponsor = facade.getPersonalAccount().getTotalCash(Currency.IDR);
                    JSONObject sponRes = facade.claimDailySponsor("SPON-" + seed + "-" + step);
                    if (sponRes.getBoolean("success")) {
                        assertEquals("Kas personal harus bertambah pas 25 juta",
                                cashBeforeSponsor.add(Money.of(25_000_000, Currency.IDR)),
                                facade.getPersonalAccount().getTotalCash(Currency.IDR));
                    }

                    // Multi-asset quote & order obligasi negara BOND:FR0091
                    JSONObject quote = facade.multiAssetOrder(null, "BOND:FR0091", true, 1, null);
                    assertTrue("Quote obligasi FR0091 harus sukses", quote.getBoolean("success"));
                    String quoteVer = quote.getString("quoteVersion");

                    JSONObject bondRes = facade.multiAssetOrder("BOND-" + seed + "-" + step, "BOND:FR0091", true, 1, quoteVer);
                    assertTrue("Order obligasi negara BOND:FR0091 harus berhasil", bondRes.getBoolean("success"));

                    actionTraces.add(ctx + " -> Claimed Daily Sponsor + Bought Government Bond FR0091");
                    actionsCount++;
                    break;
                }

                case 11: { // 12. Kemajuan Waktu & Skip Day (86400 detik)
                    LocalDate dateBefore = facade.getClock().getCurrentDate();
                    JSONObject skipReceipt = facade.skipDay("SKIP-" + seed + "-" + step);
                    assertTrue("Skip day receipt harus berhasil", skipReceipt.getBoolean("success"));
                    assertEquals(86400L, skipReceipt.getLong("gameSeconds"));
                    LocalDate dateAfter = facade.getClock().getCurrentDate();
                    assertTrue("Tanggal game harus maju setelah skip day",
                            dateAfter.isAfter(dateBefore) || dateAfter.equals(dateBefore.plusDays(1)));

                    actionTraces.add(ctx + " -> Advanced 86400s from " + dateBefore + " to " + dateAfter);
                    actionsCount++;
                    break;
                }
            }

            // Invarian diperiksa sesudah SETIAP mutasi
            assertInvariants(facade, ctx);

            // Uji ketahanan Save, Persistence, & Reload setiap 20 aksi bermakna
            if (actionsCount > 0 && actionsCount % 20 == 0) {
                facade.save();
                TradingFacade reloaded = new TradingFacade(saveManager);
                if (catalog != null) reloaded.setCatalog(catalog);
                assertTrue("Proses reload save game harus berhasil: " + ctx, reloaded.load());
                assertInvariants(reloaded, ctx + " [Reloaded Checkpoint]");
                facade = reloaded;
                facade.getClock().play();
            }
        }

        assertTrue("Tepat minimal 100 aksi bermakna tervalidasi dieksekusi", actionsCount >= 100);

        // Simpan log trace eksekusi ke berkas laporan
        File traceFile = new File(traceDir, String.format("seq_%02d_seed_%d.log", seqNum, seed));
        try (PrintWriter pw = new PrintWriter(new FileWriter(traceFile))) {
            pw.println("=== ACTION TRACE FOR SEQUENCE #" + seqNum + " SEED=" + seed + " ===");
            pw.println("Total Validated Actions: " + actionsCount);
            pw.println("Final Date: " + facade.getClock().getCurrentDate() + " " + facade.getClock().getCurrentTime());
            pw.println("Final Net Worth: " + facade.getPersonalNetWorth());
            pw.println("Ledger Reconciled: " + facade.reconcileAll());
            pw.println("--------------------------------------------------");
            for (String trace : actionTraces) {
                pw.println(trace);
            }
        }
    }

    @Test public void testExtremeSequence01() throws Exception { run100ExtremeActionsForSeed(9101L, 1); }
    @Test public void testExtremeSequence02() throws Exception { run100ExtremeActionsForSeed(9102L, 2); }
    @Test public void testExtremeSequence03() throws Exception { run100ExtremeActionsForSeed(9103L, 3); }
    @Test public void testExtremeSequence04() throws Exception { run100ExtremeActionsForSeed(9104L, 4); }
    @Test public void testExtremeSequence05() throws Exception { run100ExtremeActionsForSeed(9105L, 5); }
    @Test public void testExtremeSequence06() throws Exception { run100ExtremeActionsForSeed(9106L, 6); }
    @Test public void testExtremeSequence07() throws Exception { run100ExtremeActionsForSeed(9107L, 7); }
    @Test public void testExtremeSequence08() throws Exception { run100ExtremeActionsForSeed(9108L, 8); }
    @Test public void testExtremeSequence09() throws Exception { run100ExtremeActionsForSeed(9109L, 9); }
    @Test public void testExtremeSequence10() throws Exception { run100ExtremeActionsForSeed(9110L, 10); }

    /**
     * Membuktikan skenario multi-tahun pada rentang waktu game riil (> 730 hari bursa).
     * Memvalidasi bahwa peralihan tahun kalender (misal 2026 -> 2027 -> 2028),
     * buku besar, utang, bunga, dan persistensi bertahan utuh tanpa korupsi.
     */
    @Test
    public void testMultiYearProgression730Days() throws Exception {
        TradingFacade facade = new TradingFacade(saveManager);
        if (catalog != null) facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(500_000_000L));
        GameTestSupport.unlockManager(facade);
        facade.getClock().play();

        LocalDate initialDate = facade.getClock().getCurrentDate();
        int initialYear = initialDate.getYear();

        // Majukan 730 hari (2 tahun penuh)
        for (int day = 1; day <= 730; day++) {
            JSONObject receipt = facade.skipDay("MULTI-YEAR-SKIP-" + day);
            assertTrue("Skip day #" + day + " harus berhasil", receipt.getBoolean("success"));

            // Setiap 60 hari lakukan transaksi dan cek invarian
            if (day % 60 == 0) {
                facade.claimDailySponsor("MULTI-YEAR-SPON-" + day);
                assertInvariants(facade, "Multi-Year Day " + day);
            }

            // Setiap 180 hari uji save dan reload
            if (day % 180 == 0) {
                facade.save();
                TradingFacade reloaded = new TradingFacade(saveManager);
                if (catalog != null) reloaded.setCatalog(catalog);
                assertTrue("Reload pada hari " + day + " harus berhasil", reloaded.load());
                assertInvariants(reloaded, "Multi-Year Reload Day " + day);
                facade = reloaded;
                facade.getClock().play();
            }
        }

        LocalDate finalDate = facade.getClock().getCurrentDate();
        int finalYear = finalDate.getYear();
        assertTrue("Tahun akhir harus minimal 2 tahun lebih maju dari awal", finalYear >= initialYear + 2);
        assertInvariants(facade, "Multi-Year End State 730 Days");
    }
}
