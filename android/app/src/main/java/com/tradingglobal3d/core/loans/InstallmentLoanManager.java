package com.tradingglobal3d.core.loans;

import com.tradingglobal3d.core.collection.CollectibleItem;
import com.tradingglobal3d.core.collection.CollectionManager;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
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

    private CollectionManager collectionManager;
    private final Map<String, LoanContract> loans = new LinkedHashMap<>();

    public synchronized void bindCollectionManager(CollectionManager collections) {
        Objects.requireNonNull(collections, "Registry koleksi wajib tersedia");
        validateCollateral(collections);
        collectionManager = collections;
        for (LoanContract loan : loans.values()) if (loan.getCollateralId() != null)
            loan.bindCollateral(collections.getItem(loan.getCollateralId()));
    }
    public synchronized void validateCollateral(CollectionManager collections) {
        Set<String> active = new HashSet<>();
        for (LoanContract loan : loans.values()) if (loan.getCollateralId() != null) {
            CollectibleItem item = collections.getItem(loan.getCollateralId());
            if (item == null) throw new IllegalArgumentException("Agunan kontrak tidak ditemukan");
            if (!loan.isClosed() && (!item.isOwned() || !loan.getAccountId().equals(item.getOwnerAccountId())
                    || !loan.getId().equals(item.getPledgedLoanId()) || !active.add(item.getId())))
                throw new IllegalArgumentException("Registry agunan tidak sesuai pinjaman aktif");
        }
        for (CollectibleItem item : collections.getAllItems()) if (item.isPledged()) {
            LoanContract loan = loans.get(item.getPledgedLoanId());
            if (loan == null || loan.isClosed() || !item.getId().equals(loan.getCollateralId()))
                throw new IllegalArgumentException("Agunan tidak mempunyai pinjaman aktif");
        }
    }
    private void validateProduct(String productId, Account account, Money principal, int tenor) {
        Objects.requireNonNull(account); Objects.requireNonNull(principal);
        boolean personal = "LOAN-PERS-01".equals(productId) || "LOAN-PERS-02".equals(productId);
        boolean corporate = "LOAN-CORP-01".equals(productId);
        if ((!personal && !corporate) || (personal && account.getType() != AccountType.PERSONAL)
                || (corporate && account.getType() != AccountType.COMPANY) || principal.getCurrency() != Currency.IDR
                || principal.isLessThan(Money.of(1_000_000, Currency.IDR)) || (tenor != 7 && tenor != 14 && tenor != 30))
            throw new IllegalArgumentException("Produk, akun, pokok atau tenor tidak sah");
    }
    private String payload(String productId, Account account, Money principal, int tenor, CollectibleItem collateral) {
        return productId + "|" + account.getId() + "|" + principal.getAmount().toPlainString() + "|" + tenor + "|" + (collateral == null ? "" : collateral.getId());
    }
    public synchronized JSONObject getLoanQuote(String productId, Account account, Money principal, int tenor,
            CollectibleItem collateral, Money cashflowLimit) throws JSONException {
        validateProduct(productId, account, principal, tenor);
        Money maximum;
        String basis;
        if (productId.equals("LOAN-PERS-01")) {
            if (collectionManager == null || collateral == null || collectionManager.getItem(collateral.getId()) != collateral
                    || !collateral.isOwned() || !account.getId().equals(collateral.getOwnerAccountId()) || collateral.isPledged())
                throw new IllegalArgumentException("Agunan wajib koleksi canonical milik peminjam yang belum dijaminkan");
            maximum = collateral.getCurrentValuation().subtract(collateral.getMaintenanceArrears()).multiply(new BigDecimal("0.5"));
            if (maximum.isNegative()) maximum = Money.zero(Currency.IDR);
            basis = collateral.getCurrentValuation().getAmount().toPlainString() + ":" + collateral.getMaintenanceArrears().getAmount().toPlainString();
        } else if (productId.equals("LOAN-PERS-02")) {
            if (collateral != null) throw new IllegalArgumentException("Pembiayaan tanpa agunan tidak menerima agunan");
            maximum = Money.of(50_000_000L, Currency.IDR).subtract(getTotalOutstandingPrincipal(account.getId()));
            if (maximum.isNegative()) maximum = Money.zero(Currency.IDR);
            basis = "COLLATERAL_FREE:" + maximum.getAmount().toPlainString();
        } else {
            if (collateral != null || cashflowLimit == null || cashflowLimit.getCurrency() != Currency.IDR || cashflowLimit.isNegative())
                throw new IllegalArgumentException("Batas pinjaman perusahaan wajib berasal arus kas operasi aktual");
            maximum = cashflowLimit.subtract(getTotalOutstandingPrincipal(account.getId()));
            if (maximum.isNegative()) maximum = Money.zero(Currency.IDR);
            basis = cashflowLimit.getAmount().toPlainString() + ":" + getTotalOutstandingPrincipal(account.getId()).getAmount().toPlainString();
        }
        Money fee = principal.multiply(LoanContract.DISBURSEMENT_FEE_RATE);
        String version;
        try {
            byte[] hash = java.security.MessageDigest.getInstance("SHA-256").digest((payload(productId, account, principal, tenor, collateral) + "|" + basis).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(); for (byte value : hash) hex.append(String.format(java.util.Locale.ROOT, "%02x", value & 255)); version = hex.toString();
        } catch (java.security.NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
        return new JSONObject().put("success", true).put("eligible", principal.compareTo(maximum) <= 0)
                .put("quoteVersion", version).put("productId", productId).put("accountId", account.getId())
                .put("principalRaw", principal.getAmount().toPlainString()).put("maxPrincipalRaw", maximum.getAmount().toPlainString())
                .put("feeRaw", fee.getAmount().toPlainString()).put("netCashRaw", principal.subtract(fee).getAmount().toPlainString())
                .put("tenorDays", tenor).put("collateralId", collateral == null ? JSONObject.NULL : collateral.getId())
                .put("eligibilityRule", collateral == null ? "ACTUAL_COMPANY_CASHFLOW_LESS_OUTSTANDING" : "HALF_NET_COLLECTION_VALUE")
                .put("currency", "IDR");
    }
    public synchronized JSONObject disburseReviewedLoan(String requestId, String productId, Account account, Money principal,
            int tenor, CollectibleItem collateral, Money cashflowLimit, String expectedQuoteVersion, EconomyLedger ledger, long now) throws JSONException {
        if (requestId == null || requestId.trim().isEmpty() || requestId.length() > 160) throw new IllegalArgumentException("ID pencairan tidak sah");
        validateProduct(productId, account, principal, tenor);
        String input = payload(productId, account, principal, tenor, collateral);
        for (LoanContract loan : loans.values()) {
            JSONObject receipt = loan.getDisbursementReceipt();
            if (receipt != null && requestId.equals(receipt.getString("requestId"))) {
                if (!input.equals(loan.getDisbursementPayload())) throw new IllegalArgumentException("ID pencairan digunakan untuk input berbeda");
                return receipt;
            }
        }
        JSONObject quote = getLoanQuote(productId, account, principal, tenor, collateral, cashflowLimit);
        if (!quote.getString("quoteVersion").equals(expectedQuoteVersion)) throw new IllegalStateException("STALE_QUOTE: tinjau ulang syarat pinjaman");
        if (!quote.getBoolean("eligible")) throw new IllegalStateException("Pokok melampaui batas agunan/arus kas");
        JSONObject receipt = disburseLoan(productId, account, principal, tenor, ledger, now);
        LoanContract loan = loans.get(receipt.getString("loanId"));
        if (collateral != null) { collateral.pledgeToLoan(loan.getId(), account.getId()); loan.attachCollateral(collateral); }
        receipt.put("requestId", requestId).put("receiptId", "LOAN:" + requestId).put("accountId", account.getId())
                .put("status", "SETTLED").put("quoteVersion", expectedQuoteVersion).put("collateralId", collateral == null ? JSONObject.NULL : collateral.getId());
        loan.recordDisbursement(input, receipt);
        return new JSONObject(receipt.toString());
    }

    public synchronized int releaseAllCollateralsPreservingDebt() {
        int releasedCount = 0;
        if (collectionManager != null) {
            for (CollectibleItem item : collectionManager.getAllItems()) {
                if (item.isPledged()) {
                    item.releasePledgeMigration();
                    releasedCount++;
                }
            }
        }
        for (LoanContract loan : loans.values()) {
            loan.detachCollateral();
        }
        return releasedCount;
    }

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

    public synchronized Money getTotalOutstandingLiability(String accountId) {
        Money total = Money.zero(Currency.IDR);
        for (LoanContract loan : getLoansForAccount(accountId))
            if (!loan.isClosed()) total = total.add(loan.getTotalPayoffAmount());
        return total;
    }

    /**
     * Pencairan pinjaman baru (Disbursement).
     * Contoh: Pinjaman Rp 1.000.000 tenor 14 hari, fee pencairan 0.5% (Rp 5.000),
     * kas cair Rp 995.000, pokok utang dicatat Rp 1.000.000.
     */
    /** Trusted legacy fixture/import API; production entrypoints must use reviewed secured disbursement. */
    @Deprecated
    public synchronized JSONObject disburseLoan(String productId, Account account, Money principal, int tenorDays, EconomyLedger ledger, long now) throws JSONException {
        Objects.requireNonNull(account, "Akun pinjaman wajib tersedia");
        Objects.requireNonNull(principal, "Pokok pinjaman wajib tersedia");
        Objects.requireNonNull(ledger, "Ledger wajib tersedia");
        boolean personal = "LOAN-PERS-01".equals(productId) || "LOAN-PERS-02".equals(productId);
        boolean corporate = "LOAN-CORP-01".equals(productId);
        if ((!personal && !corporate) || (personal && account.getType() != AccountType.PERSONAL)
                || (corporate && account.getType() != AccountType.COMPANY)) throw new IllegalArgumentException("Produk tidak sesuai jenis akun peminjam");
        if (now < 0) throw new IllegalArgumentException("Timestamp pencairan tidak sah");
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

        String loanId = "LN-" + now + "-" + (loans.size() + 1);
        LoanContract contract = new LoanContract(loanId, productId, account.getId(), principal, tenorDays, now);
        loans.put(loanId, contract);

        // Kreditkan kas bersih ke akun
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

    public synchronized void processDailyAccrual(long gameTimestamp) {
        for (LoanContract loan : loans.values()) loan.processDailyAccrual(gameTimestamp);
    }

    public synchronized void processRealTimeAccrual(long realTimestamp) {
        for (LoanContract loan : loans.values()) loan.processRealTimeAccrual(realTimestamp);
    }

    public synchronized JSONArray exportState() throws JSONException {
        JSONArray arr = new JSONArray();
        for (LoanContract l : loans.values()) {
            arr.put(l.exportState());
        }
        return arr;
    }

    public synchronized void restoreState(JSONArray arr) throws JSONException { restoreState(arr, null); }
    public synchronized void restoreState(JSONArray arr, long savedGameTimestamp) throws JSONException { restoreState(arr, Long.valueOf(savedGameTimestamp)); }
    private void restoreState(JSONArray arr, Long savedGameTimestamp) throws JSONException {
        Map<String, LoanContract> restored = new LinkedHashMap<>();
        for (int i = 0; arr != null && i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            LoanContract c = LoanContract.restoreFromState(obj);
            if (savedGameTimestamp != null) c.resolveLegacyAnchor(savedGameTimestamp);
            if (restored.put(c.getId(), c) != null) throw new IllegalArgumentException("ID pinjaman duplikat");
        }
        Set<String> requests = new HashSet<>();
        for (LoanContract loan : restored.values()) {
            JSONObject receipt = loan.getDisbursementReceipt();
            if (receipt != null && !requests.add(receipt.getString("requestId"))) throw new IllegalArgumentException("ID receipt pencairan duplikat");
        }
        loans.clear();
        loans.putAll(restored);
        collectionManager = null; // Restored graph must be rebound only after staged collection validation.
    }
}
