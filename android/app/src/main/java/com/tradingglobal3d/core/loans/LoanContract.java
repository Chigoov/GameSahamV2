package com.tradingglobal3d.core.loans;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Kontrak Pinjaman Tenor Berjangka (D05).
 * - Suku bunga: 0.05% per hari atas sisa pokok.
 * - Biaya pencairan: 0.5% dipotong dari kas cair (pokok utang tetap utuh).
 * - Cicilan pokok dibagi rata harian dengan penyelesaian rounding di akhir.
 * - Pelunasan awal (prepay) tanpa penalti bunga masa depan.
 * - Penalti keterlambatan: 0.05% per hari, dibatasi maksimal 5% pokok awal.
 */
public final class LoanContract {

    public enum LoanStatus {
        ACTIVE,
        OVERDUE,
        PAID_OFF,
        RESTRUCTURED
    }

    private final String id;
    private final String productId;
    private final String accountId;
    private final Money originalPrincipal;
    private Money remainingPrincipal;
    private final int tenorDays;
    private int remainingDays;
    private final BigDecimal dailyInterestRate; // 0.0005 = 0.05%
    private Money accruedInterest;
    private Money accruedPenalty;
    private LoanStatus status;
    private final long createdTimestamp;

    public static final BigDecimal DAILY_RATE = new BigDecimal("0.0005");
    public static final BigDecimal DISBURSEMENT_FEE_RATE = new BigDecimal("0.005"); // 0.5%
    public static final BigDecimal PENALTY_RATE = new BigDecimal("0.0005");
    public static final BigDecimal MAX_PENALTY_RATIO = new BigDecimal("0.05");

    public LoanContract(String id, String productId, String accountId, Money principal, int tenorDays, long createdTimestamp) {
        this.id = Objects.requireNonNull(id);
        this.productId = Objects.requireNonNull(productId);
        this.accountId = Objects.requireNonNull(accountId);
        this.originalPrincipal = Objects.requireNonNull(principal);
        this.remainingPrincipal = principal;
        this.tenorDays = tenorDays > 0 ? tenorDays : 14;
        this.remainingDays = this.tenorDays;
        this.dailyInterestRate = DAILY_RATE;
        this.accruedInterest = Money.zero(Currency.IDR);
        this.accruedPenalty = Money.zero(Currency.IDR);
        this.status = LoanStatus.ACTIVE;
        this.createdTimestamp = createdTimestamp;
    }

    public String getId() { return id; }
    public String getProductId() { return productId; }
    public String getAccountId() { return accountId; }
    public Money getOriginalPrincipal() { return originalPrincipal; }
    public Money getRemainingPrincipal() { return remainingPrincipal; }
    public int getTenorDays() { return tenorDays; }
    public int getRemainingDays() { return remainingDays; }
    public Money getAccruedInterest() { return accruedInterest; }
    public Money getAccruedPenalty() { return accruedPenalty; }
    public LoanStatus getStatus() { return status; }
    public boolean isClosed() { return status == LoanStatus.PAID_OFF || status == LoanStatus.RESTRUCTURED; }

    public Money getTotalPayoffAmount() {
        return remainingPrincipal.add(accruedInterest).add(accruedPenalty);
    }

    /**
     * Hitung estimasi angsuran harian (pokok + bunga harian).
     */
    public Money getDailyInstallmentEstimate() {
        if (remainingDays <= 0 || isClosed()) return Money.zero(Currency.IDR);
        BigDecimal dailyPrincipal = remainingPrincipal.getAmount().divide(BigDecimal.valueOf(remainingDays), 0, RoundingMode.HALF_UP);
        BigDecimal dailyInt = remainingPrincipal.getAmount().multiply(dailyInterestRate).setScale(0, RoundingMode.HALF_UP);
        return new Money(dailyPrincipal.add(dailyInt), Currency.IDR);
    }

    /**
     * Dipanggil setiap pergantian hari bursa untuk menghitung bunga dan status jatuh tempo.
     */
    public synchronized void processDailyAccrual() {
        if (isClosed()) return;

        // Bunga harian atas sisa pokok
        BigDecimal dayInt = remainingPrincipal.getAmount().multiply(dailyInterestRate).setScale(0, RoundingMode.HALF_UP);
        this.accruedInterest = this.accruedInterest.add(new Money(dayInt, Currency.IDR));

        if (remainingDays > 0) {
            remainingDays--;
        } else {
            // Jatuh tempo terlewati -> Overdue / Arrears
            this.status = LoanStatus.OVERDUE;
            BigDecimal maxPenalty = originalPrincipal.getAmount().multiply(MAX_PENALTY_RATIO).setScale(0, RoundingMode.HALF_UP);
            if (accruedPenalty.getAmount().compareTo(maxPenalty) < 0) {
                BigDecimal dayPenalty = remainingPrincipal.getAmount().multiply(PENALTY_RATE).setScale(0, RoundingMode.HALF_UP);
                BigDecimal newPenalty = accruedPenalty.getAmount().add(dayPenalty);
                if (newPenalty.compareTo(maxPenalty) > 0) newPenalty = maxPenalty;
                this.accruedPenalty = new Money(newPenalty, Currency.IDR);
            }
        }
    }

    /**
     * Membayar sebagian atau seluruh angsuran pinjaman.
     */
    public synchronized JSONObject makePayment(Account account, Money amount, EconomyLedger ledger, long now) throws JSONException {
        if (isClosed()) throw new IllegalStateException("Pinjaman sudah lunas atau ditutup");
        if (amount.getCurrency() != Currency.IDR) throw new IllegalArgumentException("Pembayaran harus dalam IDR");
        if (account.getAvailableCash(Currency.IDR).isLessThan(amount)) {
            throw new IllegalStateException("Saldo kas IDR tidak mencukupi untuk pembayaran");
        }

        Money totalDue = getTotalPayoffAmount();
        Money paymentAmount = amount.compareTo(totalDue) > 0 ? totalDue : amount;

        // Deduksi kas akun
        account.debitCash(paymentAmount);

        // Alokasi pembayaran: Penalti -> Bunga -> Pokok
        Money rem = paymentAmount;
        if (!accruedPenalty.isZero()) {
            Money payPen = rem.compareTo(accruedPenalty) > 0 ? accruedPenalty : rem;
            accruedPenalty = accruedPenalty.subtract(payPen);
            rem = rem.subtract(payPen);
            ledger.recordMutation("PAY-PEN-" + id + "-" + now, account, LedgerEntryType.TRANSACTION_FEE, payPen, false, now, "Bayar penalti pinjaman " + id);
        }

        if (!rem.isZero() && !accruedInterest.isZero()) {
            Money payInt = rem.compareTo(accruedInterest) > 0 ? accruedInterest : rem;
            accruedInterest = accruedInterest.subtract(payInt);
            rem = rem.subtract(payInt);
            ledger.recordMutation("PAY-INT-" + id + "-" + now, account, LedgerEntryType.TRANSACTION_FEE, payInt, false, now, "Bayar bunga pinjaman " + id);
        }

        if (!rem.isZero()) {
            Money payPrin = rem.compareTo(remainingPrincipal) > 0 ? remainingPrincipal : rem;
            remainingPrincipal = remainingPrincipal.subtract(payPrin);
            ledger.recordMutation("PAY-PRIN-" + id + "-" + now, account, LedgerEntryType.LOAN_REPAYMENT, payPrin, false, now, "Bayar pokok pinjaman " + id);
        }

        if (remainingPrincipal.isZero() && accruedInterest.isZero() && accruedPenalty.isZero()) {
            this.status = LoanStatus.PAID_OFF;
        }

        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("loanId", id);
        res.put("paymentAmount", paymentAmount.format());
        res.put("remainingPrincipal", remainingPrincipal.format());
        res.put("totalRemainingDue", getTotalPayoffAmount().format());
        res.put("status", status.name());
        res.put("message", status == LoanStatus.PAID_OFF ? "Pinjaman berhasil DILUNASI!" : "Pembayaran pinjaman berhasil dicatat.");
        return res;
    }

    /**
     * Pelunasan awal penuh (Prepay).
     */
    public synchronized JSONObject prepayFull(Account account, EconomyLedger ledger, long now) throws JSONException {
        return makePayment(account, getTotalPayoffAmount(), ledger, now);
    }

    /**
     * Restrukturisasi pinjaman (perpanjang tenor).
     */
    public synchronized JSONObject restructure(int additionalDays, long now) throws JSONException {
        if (isClosed()) throw new IllegalStateException("Pinjaman sudah lunas atau ditutup");
        this.remainingDays += additionalDays;
        this.status = LoanStatus.ACTIVE;
        this.accruedPenalty = Money.zero(Currency.IDR); // Hapus penalti sebagai keringanan restrukturisasi

        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("loanId", id);
        res.put("newRemainingDays", remainingDays);
        res.put("status", status.name());
        res.put("message", "Pinjaman berhasil direstrukturisasi. Tenor diperpanjang " + additionalDays + " hari.");
        return res;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("productId", productId);
        obj.put("accountId", accountId);
        obj.put("originalPrincipal", originalPrincipal.format());
        obj.put("remainingPrincipal", remainingPrincipal.format());
        obj.put("tenorDays", tenorDays);
        obj.put("remainingDays", remainingDays);
        obj.put("accruedInterest", accruedInterest.format());
        obj.put("accruedPenalty", accruedPenalty.format());
        obj.put("totalPayoff", getTotalPayoffAmount().format());
        obj.put("dailyEstimate", getDailyInstallmentEstimate().format());
        obj.put("status", status.name());
        obj.put("isClosed", isClosed());
        return obj;
    }

    public JSONObject exportState() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("productId", productId);
        obj.put("accountId", accountId);
        obj.put("originalPrincipal", originalPrincipal.getAmount().toPlainString());
        obj.put("remainingPrincipal", remainingPrincipal.getAmount().toPlainString());
        obj.put("tenorDays", tenorDays);
        obj.put("remainingDays", remainingDays);
        obj.put("accruedInterest", accruedInterest.getAmount().toPlainString());
        obj.put("accruedPenalty", accruedPenalty.getAmount().toPlainString());
        obj.put("status", status.name());
        obj.put("createdTimestamp", createdTimestamp);
        return obj;
    }

    public static LoanContract restoreFromState(JSONObject obj) {
        if (obj == null) return null;
        String id = obj.optString("id");
        String pId = obj.optString("productId");
        String aId = obj.optString("accountId");
        Money origP = new Money(new BigDecimal(obj.optString("originalPrincipal", "1000000")), Currency.IDR);
        int tenor = obj.optInt("tenorDays", 14);
        long ts = obj.optLong("createdTimestamp", System.currentTimeMillis());

        LoanContract contract = new LoanContract(id, pId, aId, origP, tenor, ts);
        contract.remainingPrincipal = new Money(new BigDecimal(obj.optString("remainingPrincipal", origP.getAmount().toPlainString())), Currency.IDR);
        contract.remainingDays = obj.optInt("remainingDays", tenor);
        contract.accruedInterest = new Money(new BigDecimal(obj.optString("accruedInterest", "0")), Currency.IDR);
        contract.accruedPenalty = new Money(new BigDecimal(obj.optString("accruedPenalty", "0")), Currency.IDR);
        contract.status = LoanStatus.valueOf(obj.optString("status", "ACTIVE"));
        return contract;
    }
}
