package com.tradingglobal3d.core;

import com.tradingglobal3d.core.collection.CollectibleItem;
import com.tradingglobal3d.core.collection.CollectionManager;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.loans.InstallmentLoanManager;
import com.tradingglobal3d.core.loans.LoanContract;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.Assert.*;

/**
 * Uji Kebijakan Pembiayaan Baru & Migrasi 2026 (A16):
 * 1. Pinjaman personal LOAN-PERS-01 dapat dicairkan tanpa agunan (collateral-free).
 * 2. Migrasi pinjaman lama melepaskan agunan barang koleksi, tetapi sisa kewajiban utang tetap utuh.
 * 3. Denda keterlambatan 5% sederhana per 24 jam nyata atas cicilan yang belum dibayar (tanpa bunga majemuk).
 * 4. Konsistensi saldo kas dan rekonsiliasi buku besar.
 */
public class LoanFinancingPolicy2026RegressionTest {

    private static Money cash(long amount) {
        return Money.of(amount, Currency.IDR);
    }

    private static long time(int day) {
        return LocalDate.of(2026, 10, 20).plusDays(day).atTime(12, 0).toInstant(ZoneOffset.ofHours(7)).toEpochMilli();
    }

    @Test
    public void testCollateralFreePersonalLoanDisbursementA16() throws Exception {
        InstallmentLoanManager loans = new InstallmentLoanManager();
        CollectionManager collections = new CollectionManager();
        loans.bindCollectionManager(collections);

        EconomyLedger ledger = new EconomyLedger();
        Account account = new Account("ACC-PERSONAL-001", "Trader", AccountType.PERSONAL, null);
        ledger.recordMutation("INIT", account, LedgerEntryType.INITIAL_CAPITAL, cash(10_000_000), true, time(0), "Modal awal");

        // 1. Ambil quote personal tanpa agunan (collateral == null, LOAN-PERS-02)
        JSONObject quote = loans.getLoanQuote("LOAN-PERS-02", account, cash(20_000_000), 14, null, null);
        assertTrue("Quote pinjaman tanpa agunan harus eligible", quote.getBoolean("eligible"));
        assertEquals("LOAN-PERS-02", quote.getString("productId"));
        assertTrue(quote.isNull("collateralId"));

        // 2. Cairkan pinjaman tanpa agunan
        String quoteVer = quote.getString("quoteVersion");
        JSONObject receipt = loans.disburseReviewedLoan("REQ-LOAN-FREE-01", "LOAN-PERS-02", account, cash(20_000_000), 14, null, null, quoteVer, ledger, time(0));
        assertTrue(receipt.getBoolean("success"));
        assertEquals("SETTLED", receipt.getString("status"));

        String loanId = receipt.getString("loanId");
        LoanContract loan = loans.getLoan(loanId);
        assertNotNull(loan);
        assertNull("Pinjaman baru tidak boleh memiliki agunan", loan.getCollateralId());
        assertEquals(cash(20_000_000), loan.getRemainingPrincipal());

        // 3. Verifikasi kas cair bersih (pokok 20jt - fee 0.5% = 19.9jt)
        // Kas awal 10jt + 19.9jt = 29.9jt
        assertEquals(cash(29_900_000), account.getAvailableCash(Currency.IDR));
        assertTrue(ledger.reconcile(account, Currency.IDR));
    }

    @Test
    public void testLegacyCollateralReleasePreservingDebtA16() throws Exception {
        InstallmentLoanManager loans = new InstallmentLoanManager();
        CollectionManager collections = new CollectionManager();
        loans.bindCollectionManager(collections);

        EconomyLedger ledger = new EconomyLedger();
        Account account = new Account("ACC-PERSONAL-001", "Trader", AccountType.PERSONAL, null);
        ledger.recordMutation("INIT", account, LedgerEntryType.INITIAL_CAPITAL, cash(100_000_000), true, time(0), "Modal");

        // Beli barang koleksi
        collections.buyItem("REQ-BUY-COLLATERAL", "COL-ART-00", account, ledger, null, time(0));
        CollectibleItem item = collections.getItem("COL-ART-00");
        assertTrue(item.isOwned());

        // Buat pinjaman lama beragunan
        JSONObject quote = loans.getLoanQuote("LOAN-PERS-01", account, cash(5_000_000), 7, item, null);
        String quoteVer = quote.getString("quoteVersion");
        JSONObject disburse = loans.disburseReviewedLoan("REQ-LOAN-LEGACY-01", "LOAN-PERS-01", account, cash(5_000_000), 7, item, null, quoteVer, ledger, time(0));
        String loanId = disburse.getString("loanId");
        LoanContract loan = loans.getLoan(loanId);

        // Sebelum migrasi: item berstatus agunan
        assertTrue(item.isPledged());
        assertEquals(item.getId(), loan.getCollateralId());
        assertEquals(cash(5_000_000), loan.getRemainingPrincipal());

        // Eksekusi migrasi A16: Lepas agunan, pertahankan utang
        int released = loans.releaseAllCollateralsPreservingDebt();
        assertEquals(1, released);

        // Setelah migrasi:
        assertFalse("Agunan koleksi harus dilepas", item.isPledged());
        assertNull("Pinjaman tidak boleh lagi mengikat agunan", loan.getCollateralId());
        assertEquals("Sisa pokok utang pemain HARUS tetap ada", cash(5_000_000), loan.getRemainingPrincipal());
        assertEquals("Total kewajiban utang HARUS tetap ada", cash(5_000_000), loan.getTotalPayoffAmount());

        // Pemain tetap dapat melunasi utangnya secara normal
        JSONObject payment = loan.makePayment("REQ-PAY-MIGRATED", account, cash(5_000_000), ledger, time(0));
        assertEquals("PAID_OFF", payment.getString("status"));
        assertTrue(loan.isClosed());
        assertTrue(ledger.reconcile(account, Currency.IDR));
    }

    @Test
    public void testSimplePenalty5PercentPer24HourRealA16() throws Exception {
        LoanContract loan = new LoanContract("LOAN-TEST-PENALTY", "LOAN-PERS-01", "ACC-PERSONAL-001", cash(14_000_000), 14, time(0));

        // Hari ke-0: belum ada overdue
        loan.processDailyAccrual(time(0));
        assertEquals(cash(0), loan.getOverduePrincipal());
        assertEquals(cash(0), loan.getAccruedPenalty());

        // Majukan hari kalender bursa hingga 2 hari melewati jatuh tempo cicilan pertama (anchor + 1 day)
        loan.processDailyAccrual(time(3));
        Money overdue = loan.getOverduePrincipal();
        assertTrue("Harus ada cicilan pokok yang jatuh tempo", overdue.isPositive());
        Money penaltyBeforeReal24h = loan.getAccruedPenalty();

        // Simulasikan 24 jam nyata (86.400.000 ms) keterlambatan
        // Denda 5% sederhana dari overduePrincipal
        long oneRealDayMs = 86_400_000L;
        loan.accrueSimplePenalty24hReal(oneRealDayMs);

        Money expectedPenalty24h = overdue.multiply(new BigDecimal("0.05"));
        assertTrue(expectedPenalty24h.isPositive());
        assertEquals("Denda 24 jam harus 5% sederhana dari cicilan tertunggak", penaltyBeforeReal24h.add(expectedPenalty24h), loan.getAccruedPenalty());

        // Simulasikan 48 jam nyata berikutnya (total 2 periode x 5% = 10% sederhana, non-compounding)
        loan.accrueSimplePenalty24hReal(2 * oneRealDayMs);
        Money expectedPenalty72h = penaltyBeforeReal24h.add(expectedPenalty24h).add(overdue.multiply(new BigDecimal("0.10")));
        assertEquals("Denda tidak boleh majemuk (harus sederhana atas pokok cicilan tertunggak)", expectedPenalty72h, loan.getAccruedPenalty());
    }

    @Test
    public void testProductionLoopRealTimeAccrualAndTimerReset() throws Exception {
        InstallmentLoanManager manager = new InstallmentLoanManager();
        EconomyLedger ledger = new EconomyLedger();
        Account account = new Account("ACC-PERSONAL-001", "Trader", AccountType.PERSONAL, null);
        ledger.recordMutation("INIT", account, LedgerEntryType.INITIAL_CAPITAL, cash(50_000_000), true, time(0), "Modal");

        // Cairkan pinjaman 10 juta tenor 7 hari
        JSONObject quote = manager.getLoanQuote("LOAN-PERS-02", account, cash(10_000_000), 7, null, null);
        JSONObject disburse = manager.disburseReviewedLoan("REQ-PROD-01", "LOAN-PERS-02", account, cash(10_000_000), 7, null, null, quote.getString("quoteVersion"), ledger, time(0));
        String loanId = disburse.getString("loanId");
        LoanContract loan = manager.getLoan(loanId);

        long startRealMs = 1792756800000L;
        loan.setLastRealPenaltyTimestamp(startRealMs);

        // Hari 0: Tidak ada keterlambatan, panggil processRealTimeAccrual setelah 24 jam nyata
        manager.processRealTimeAccrual(startRealMs + 86_400_000L);
        assertEquals(cash(0), loan.getAccruedPenalty());

        // Majukan hari game ke hari 3 sehingga cicilan hari 1 dan 2 menjadi overdue
        manager.processDailyAccrual(time(3));
        Money overdue = loan.getOverduePrincipal();
        assertTrue("Harus ada cicilan tertunggak", overdue.isPositive());
        Money penaltyBeforeReal = loan.getAccruedPenalty();

        // Sekarang alirkan 24 jam nyata (1 periode) melalui alur manajer produksi
        long overdueStartRealMs = startRealMs + 86_400_000L;
        manager.processRealTimeAccrual(overdueStartRealMs + 86_400_000L);
        Money expectedPenalty = overdue.multiply(new BigDecimal("0.05"));
        assertEquals("Denda 5% harus otomatis terakrual lewat loop produksi", penaltyBeforeReal.add(expectedPenalty), loan.getAccruedPenalty());

        // Alirkan 48 jam nyata berikutnya (total 2 periode tambahan = 10%)
        manager.processRealTimeAccrual(overdueStartRealMs + 3 * 86_400_000L);
        Money expectedPenalty3Periods = penaltyBeforeReal.add(expectedPenalty).add(overdue.multiply(new BigDecimal("0.10")));
        assertEquals(expectedPenalty3Periods, loan.getAccruedPenalty());
    }

    @Test
    public void testSaveAndRestoreLoanWithAccruedPenaltyExceedingLegacyCap() throws Exception {
        // Kontrak pinjaman 1.000.000
        LoanContract loan = new LoanContract("LOAN-CAP-TEST", "LOAN-PERS-02", "ACC-PERSONAL-001", cash(1_000_000), 7, time(0));
        // Buat cicilan overdue
        loan.processDailyAccrual(time(5));
        Money overdue = loan.getOverduePrincipal();
        assertTrue(overdue.isPositive());

        // Akrual denda 15% (3 hari nyata x 5% = 15% sederhana)
        // 15% dari pokok tertunggak bisa mencapai Rp 150.000 (melebihi cap lama 5% = Rp 50.000)
        loan.accrueSimplePenalty24hReal(3 * 86_400_000L);
        assertTrue("Denda harus melampaui batas lama 5%", loan.getAccruedPenalty().compareTo(cash(50_000)) > 0);

        // Export state ke JSON
        JSONObject state = loan.exportState();

        // Restore dari state JSON: HARUS BERHASIL TANPA THROW EXCEPTION
        LoanContract restored = LoanContract.restoreFromState(state);
        assertNotNull(restored);
        assertEquals(loan.getId(), restored.getId());
        assertEquals(loan.getAccruedPenalty(), restored.getAccruedPenalty());
        assertEquals(loan.getTotalPayoffAmount(), restored.getTotalPayoffAmount());
        assertEquals(loan.getLastRealPenaltyTimestamp(), restored.getLastRealPenaltyTimestamp());

        // Pembayaran pada kontrak yang di-restore
        EconomyLedger ledger = new EconomyLedger();
        Account account = new Account("ACC-PERSONAL-001", "Trader", AccountType.PERSONAL, null);
        ledger.recordMutation("INIT", account, LedgerEntryType.INITIAL_CAPITAL, cash(10_000_000), true, time(0), "Modal");
        JSONObject payRes = restored.makePayment("REQ-PAY-RESTORED", account, restored.getTotalPayoffAmount(), ledger, time(5));
        assertEquals("PAID_OFF", payRes.getString("status"));
        assertTrue(restored.isClosed());
    }
}
