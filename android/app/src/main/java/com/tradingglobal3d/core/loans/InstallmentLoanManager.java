package com.tradingglobal3d.core.loans;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Manajer Pengajuan dan Portofolio Pinjaman Tenor (D05).
 * Mendukung 2 lini produk pinjaman:
 * 1. LOAN-PERS-01: Pinjaman Pribadi Multiguna (Akun Personal)
 * 2. LOAN-CORP-01: Kredit Modal Kerja Operasional (Akun Perusahaan)
 */
public final class InstallmentLoanManager {

    private final Map<String, LoanContract> loans = new LinkedHashMap<>();

    public synchronized void reset() {
        loans.clear();
    }

    public synchronized LoanContract getLoan(String loanId) {
        return loans.get(loanId);
    }

    public synchronized Collection<LoanContract> getAllLoans() {
        return Collections.unmodifiableCollection(loans.values());
    }

    public synchronized List<LoanContract> getLoansForAccount(String accountId) {
        List<LoanContract> list = new ArrayList<>();
        for (LoanContract l : loans.values()) {
            if (l.getAccountId().equals(accountId)) {
                list.add(l);
            }
        }
        return list;
    }

    /**
     * Hitung total kewajiban pokok pinjaman yang belum lunas untuk akun tertentu.
     * Digunakan sebagai liabilitas pengurang pada perhitungan kekayaan bersih (D02).
     */
    public synchronized Money getTotalOutstandingPrincipal(String accountId) {
        if (accountId == null) return Money.zero(Currency.IDR);
        Money total = Money.zero(Currency.IDR);
        for (LoanContract l : loans.values()) {
            if (l.getAccountId().equals(accountId) && !l.isClosed()) {
                total = total.add(l.getRemainingPrincipal());
            }
        }
        return total;
    }

    /**
     * Pencairan pinjaman baru (Disbursement).
     * Contoh: Pinjaman Rp 1.000.000 tenor 14 hari, fee pencairan 0.5% (Rp 5.000),
     * kas cair Rp 995.000, pokok utang dicatat Rp 1.000.000.
     */
    public synchronized JSONObject disburseLoan(String productId, Account account, Money principal, int tenorDays, EconomyLedger ledger, long now) throws JSONException {
        if (principal.getCurrency() != Currency.IDR) throw new IllegalArgumentException("Pinjaman hanya tersedia dalam IDR");
        if (principal.getAmount().compareTo(BigDecimal.valueOf(1_000_000)) < 0) {
            throw new IllegalArgumentException("Plafon pinjaman minimal Rp 1.000.000");
        }
        if (tenorDays != 7 && tenorDays != 14 && tenorDays != 30) {
            throw new IllegalArgumentException("Pilihan tenor pinjaman yang sah: 7, 14, atau 30 hari");
        }

        // Biaya pencairan 0.5%
        Money fee = new Money(principal.getAmount().multiply(LoanContract.DISBURSEMENT_FEE_RATE).setScale(0, RoundingMode.HALF_UP), Currency.IDR);
        Money netDisbursed = principal.subtract(fee);

        String loanId = "LN-" + System.currentTimeMillis() + "-" + (loans.size() + 1);
        LoanContract contract = new LoanContract(loanId, productId, account.getId(), principal, tenorDays, now);
        loans.put(loanId, contract);

        // Kreditkan kas bersih ke akun
        account.creditCash(netDisbursed);
        ledger.recordMutation("LOAN-DISBURSE-" + loanId, account, LedgerEntryType.LOAN_DISBURSEMENT, principal, true, now, "Pencairan pokok pinjaman " + loanId);
        ledger.recordMutation("FEE-LOAN-" + loanId, account, LedgerEntryType.TRANSACTION_FEE, fee, false, now, "Biaya provisi pencairan pinjaman " + loanId);

        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("loanId", loanId);
        res.put("productId", productId);
        res.put("principal", principal.format());
        res.put("disbursementFee", fee.format());
        res.put("netCashDisbursed", netDisbursed.format());
        res.put("tenorDays", tenorDays);
        res.put("dailyRate", "0.05% / hari");
        res.put("dailyEstimate", contract.getDailyInstallmentEstimate().format());
        res.put("message", "Pencairan pinjaman " + loanId + " berhasil! Kas bersih " + netDisbursed.format() + " telah masuk ke akun.");
        return res;
    }

    /**
     * Memajukan waktu harian untuk seluruh pinjaman aktif (hitung bunga harian dan penalti jika jatuh tempo).
     */
    public synchronized void processDailyAccrual() {
        for (LoanContract l : loans.values()) {
            l.processDailyAccrual();
        }
    }

    public synchronized JSONArray exportState() throws JSONException {
        JSONArray arr = new JSONArray();
        for (LoanContract l : loans.values()) {
            arr.put(l.exportState());
        }
        return arr;
    }

    public synchronized void restoreState(JSONArray arr) throws JSONException {
        loans.clear();
        if (arr == null) return;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            LoanContract c = LoanContract.restoreFromState(obj);
            if (c != null) {
                loans.put(c.getId(), c);
            }
        }
    }
}
