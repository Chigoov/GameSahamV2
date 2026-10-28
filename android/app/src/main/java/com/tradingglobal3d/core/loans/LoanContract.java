package com.tradingglobal3d.core.loans;

import com.tradingglobal3d.core.collection.CollectibleItem;
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
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
    private long lastAccrualDay;
    private int scheduleVersion = 1;
    private String collateralId;
    private CollectibleItem boundCollateral;
    private JSONObject disbursementReceipt;
    private String disbursementPayload;
    private boolean legacyScheduleMigration;
    private boolean legacyAnchorUnresolved;
    private Money totalPenaltyAssessed = Money.zero(Currency.IDR);
    private final List<Installment> schedule = new ArrayList<>();
    private final List<JSONObject> restructureHistory = new ArrayList<>();
    private static final ZoneOffset JAKARTA = ZoneOffset.ofHours(7);
    private static final class Installment {
        long dueDay;
        Money principalDue, principalPaid = Money.zero(Currency.IDR);
        Money interestDue = Money.zero(Currency.IDR), interestPaid = Money.zero(Currency.IDR);
        Money penaltyDue = Money.zero(Currency.IDR), penaltyPaid = Money.zero(Currency.IDR);
        Installment(long dueDay, Money principal) { this.dueDay = dueDay; principalDue = principal; }
        JSONObject json() throws JSONException {
            return new JSONObject().put("dueDate", LocalDate.ofEpochDay(dueDay).toString()).put("dueDay", dueDay)
                    .put("principalDue", principalDue.getAmount().toPlainString()).put("principalPaid", principalPaid.getAmount().toPlainString())
                    .put("interestDue", interestDue.getAmount().toPlainString()).put("interestPaid", interestPaid.getAmount().toPlainString())
                    .put("penaltyDue", penaltyDue.getAmount().toPlainString()).put("penaltyPaid", penaltyPaid.getAmount().toPlainString())
                    .put("remaining", principalDue.subtract(principalPaid).add(interestDue.subtract(interestPaid)).add(penaltyDue.subtract(penaltyPaid)).getAmount().toPlainString());
        }
    }
    private final Map<String, JSONObject> paymentReceipts = new LinkedHashMap<>();

    public static final BigDecimal DAILY_RATE = new BigDecimal("0.0005");
    public static final BigDecimal DISBURSEMENT_FEE_RATE = new BigDecimal("0.005"); // 0.5%
    public static final BigDecimal PENALTY_RATE = new BigDecimal("0.0005");
    public static final BigDecimal MAX_PENALTY_RATIO = new BigDecimal("0.05");
    public static final BigDecimal SIMPLE_24H_REAL_PENALTY_RATE = new BigDecimal("0.05"); // 5% sederhana per 24 jam nyata (A16)

    public LoanContract(String id, String productId, String accountId, Money principal, int tenorDays, long createdTimestamp) {
        this.id = Objects.requireNonNull(id);
        this.productId = Objects.requireNonNull(productId);
        this.accountId = Objects.requireNonNull(accountId);
        this.originalPrincipal = Objects.requireNonNull(principal);
        this.remainingPrincipal = principal;
        if (principal.getCurrency() != Currency.IDR || !principal.isPositive() || (tenorDays != 7 && tenorDays != 14 && tenorDays != 30)
                || createdTimestamp < 0) throw new IllegalArgumentException("Pokok, tenor atau timestamp pinjaman tidak sah");
        this.tenorDays = tenorDays;
        this.remainingDays = this.tenorDays;
        this.dailyInterestRate = DAILY_RATE;
        this.accruedInterest = Money.zero(Currency.IDR);
        this.accruedPenalty = Money.zero(Currency.IDR);
        this.status = LoanStatus.ACTIVE;
        this.createdTimestamp = createdTimestamp;
        this.lastAccrualDay = day(createdTimestamp);
        buildSchedule(principal, tenorDays, lastAccrualDay);
    }

    void resolveLegacyAnchor(long savedGameTimestamp) {
        if (!legacyAnchorUnresolved) return;
        long restoredDay = day(savedGameTimestamp);
        if (restoredDay < lastAccrualDay) throw new IllegalArgumentException("Clock save mendahului jadwal legacy");
        long shift = restoredDay - lastAccrualDay;
        for (Installment installment : schedule) installment.dueDay += shift;
        lastAccrualDay = restoredDay; legacyAnchorUnresolved = false; refreshStatus();
    }
    public String getCollateralId() { return collateralId; }
    void attachCollateral(CollectibleItem item) { collateralId = item.getId(); boundCollateral = item; }
    void bindCollateral(CollectibleItem item) { boundCollateral = item; }
    public void detachCollateral() { this.collateralId = null; this.boundCollateral = null; }

    private long lastRealPenaltyTimestamp;
    public long getLastRealPenaltyTimestamp() { return lastRealPenaltyTimestamp; }
    public void setLastRealPenaltyTimestamp(long ts) { this.lastRealPenaltyTimestamp = ts; }

    public synchronized void accrueSimplePenalty24hReal(long realTimeElapsedMs) {
        if (isClosed() || realTimeElapsedMs < 86400000L) return;
        long periods = realTimeElapsedMs / 86400000L;
        Money overdue = getOverduePrincipal();
        if (!overdue.isPositive() || periods <= 0) return;
        BigDecimal penaltyRate = SIMPLE_24H_REAL_PENALTY_RATE.multiply(BigDecimal.valueOf(periods));
        Money penalty = overdue.multiply(penaltyRate);
        if (penalty.isPositive()) {
            for (Installment installment : schedule) {
                if (installment.dueDay <= lastAccrualDay && installment.principalDue.subtract(installment.principalPaid).isPositive()) {
                    installment.penaltyDue = installment.penaltyDue.add(penalty);
                    break;
                }
            }
            accruedPenalty = accruedPenalty.add(penalty);
            totalPenaltyAssessed = totalPenaltyAssessed.add(penalty);
            refreshStatus();
        }
    }

    public synchronized void processRealTimeAccrual(long currentRealTimeMs) {
        if (isClosed()) return;
        if (lastRealPenaltyTimestamp <= 0) {
            lastRealPenaltyTimestamp = currentRealTimeMs;
            return;
        }
        if (currentRealTimeMs < lastRealPenaltyTimestamp) {
            lastRealPenaltyTimestamp = currentRealTimeMs;
            return;
        }
        Money overdue = getOverduePrincipal();
        if (!overdue.isPositive()) {
            lastRealPenaltyTimestamp = currentRealTimeMs;
            return;
        }
        long elapsed = currentRealTimeMs - lastRealPenaltyTimestamp;
        if (elapsed >= 86_400_000L) {
            long periods = elapsed / 86_400_000L;
            if (periods > 0) {
                accrueSimplePenalty24hReal(periods * 86_400_000L);
                lastRealPenaltyTimestamp += periods * 86_400_000L;
            }
        }
    }
    JSONObject getDisbursementReceipt() throws JSONException { return disbursementReceipt == null ? null : new JSONObject(disbursementReceipt.toString()); }
    String getDisbursementPayload() { return disbursementPayload; }
    void recordDisbursement(String payload, JSONObject receipt) throws JSONException {
        disbursementPayload = payload; disbursementReceipt = new JSONObject(receipt.toString());
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
    public boolean isClosed() { return status == LoanStatus.PAID_OFF; }

    public Money getTotalPayoffAmount() {
        return remainingPrincipal.add(accruedInterest).add(accruedPenalty);
    }

    /**
     * Hitung estimasi angsuran harian (pokok + bunga harian).
     */
    private static long day(long timestamp) { return Instant.ofEpochMilli(timestamp).atOffset(JAKARTA).toLocalDate().toEpochDay(); }
    private void buildSchedule(Money principal, int days, long anchor) {
        BigDecimal part = principal.getAmount().divide(BigDecimal.valueOf(days), 0, RoundingMode.DOWN);
        for (int index = 1; index <= days; index++) {
            BigDecimal amount = index == days ? principal.getAmount().subtract(part.multiply(BigDecimal.valueOf(days - 1))) : part;
            schedule.add(new Installment(anchor + index, new Money(amount, Currency.IDR)));
        }
    }
    public JSONArray getSchedule() throws JSONException {
        JSONArray rows = new JSONArray();
        for (Installment installment : schedule) rows.put(installment.json());
        return rows;
    }
    public Money getOverduePrincipal() { return overduePrincipal(lastAccrualDay); }
    private Money overduePrincipal(long currentDay) {
        Money overdue = Money.zero(Currency.IDR);
        for (Installment installment : schedule) if (installment.dueDay < currentDay)
            overdue = overdue.add(installment.principalDue.subtract(installment.principalPaid));
        return overdue;
    }
    public Money getDailyInstallmentEstimate() {
        if (isClosed()) return Money.zero(Currency.IDR);
        for (Installment installment : schedule) {
            Money principal = installment.principalDue.subtract(installment.principalPaid);
            if (principal.isPositive()) return principal.add(remainingPrincipal.multiply(dailyInterestRate));
        }
        return accruedInterest.add(accruedPenalty);
    }
    private void refreshStatus() {
        remainingDays = (int) Math.max(0, schedule.get(schedule.size() - 1).dueDay - lastAccrualDay);
        status = getTotalPayoffAmount().isZero() ? LoanStatus.PAID_OFF
                : getOverduePrincipal().isPositive() ? LoanStatus.OVERDUE : LoanStatus.ACTIVE;
    }
    /** Compatibility callback: one calendar day after the persisted game-day anchor. */
    public synchronized void processDailyAccrual() { accrueThrough(lastAccrualDay + 1); }
    public synchronized void processDailyAccrual(long gameTimestamp) {
        if (gameTimestamp < 0) throw new IllegalArgumentException("Timestamp akrual tidak sah");
        accrueThrough(day(gameTimestamp));
    }
    private void accrueThrough(long targetDay) {
        while (!isClosed() && lastAccrualDay < targetDay) {
            long currentDay = lastAccrualDay + 1;
            Installment interestRow = schedule.get(schedule.size() - 1);
            for (Installment installment : schedule) if (installment.dueDay >= currentDay) { interestRow = installment; break; }
            Money interest = remainingPrincipal.multiply(dailyInterestRate);
            interestRow.interestDue = interestRow.interestDue.add(interest);
            accruedInterest = accruedInterest.add(interest);
            Money allowance = originalPrincipal.multiply(MAX_PENALTY_RATIO).subtract(totalPenaltyAssessed);
            Money dailyPenalty = overduePrincipal(currentDay).multiply(PENALTY_RATE);
            if (dailyPenalty.compareTo(allowance) > 0) dailyPenalty = allowance;
            if (dailyPenalty.isPositive()) {
                // One penalty allocation on the oldest overdue installment; the base excludes all future principal.
                for (Installment installment : schedule) if (installment.dueDay < currentDay
                        && installment.principalDue.subtract(installment.principalPaid).isPositive()) {
                    installment.penaltyDue = installment.penaltyDue.add(dailyPenalty); break;
                }
                accruedPenalty = accruedPenalty.add(dailyPenalty);
                totalPenaltyAssessed = totalPenaltyAssessed.add(dailyPenalty);
            }
            lastAccrualDay = currentDay;
            refreshStatus();
        }
    }
    private void allocatePayment(Money amount, int component) {
        Money remaining = amount;
        for (Installment installment : schedule) {
            Money due = component == 0 ? installment.penaltyDue.subtract(installment.penaltyPaid)
                    : component == 1 ? installment.interestDue.subtract(installment.interestPaid)
                    : installment.principalDue.subtract(installment.principalPaid);
            Money payment = remaining.compareTo(due) < 0 ? remaining : due;
            if (component == 0) installment.penaltyPaid = installment.penaltyPaid.add(payment);
            else if (component == 1) installment.interestPaid = installment.interestPaid.add(payment);
            else installment.principalPaid = installment.principalPaid.add(payment);
            remaining = remaining.subtract(payment);
            if (remaining.isZero()) return;
        }
        if (!remaining.isZero()) throw new IllegalStateException("Alokasi jadwal tidak cocok dengan utang");
    }

    /**
     * Membayar sebagian atau seluruh angsuran pinjaman.
     */
    public synchronized JSONObject makePayment(Account account, Money amount, EconomyLedger ledger, long now) throws JSONException {
        return makePayment(UUID.randomUUID().toString(), account, amount, ledger, now);
    }

    public synchronized JSONObject makePayment(String requestId, Account account, Money amount, EconomyLedger ledger, long now) throws JSONException {
        if (requestId == null || requestId.trim().isEmpty() || requestId.length() > 160)
            throw new IllegalArgumentException("ID pembayaran tidak valid");
        if (account == null || !accountId.equals(account.getId()))
            throw new IllegalArgumentException("Akun pembayaran bukan pemilik pinjaman");
        if (amount == null || amount.getCurrency() != Currency.IDR || !amount.isPositive())
            throw new IllegalArgumentException("Pembayaran harus positif dalam IDR");
        Objects.requireNonNull(ledger, "Ledger wajib tersedia");
        JSONObject existing = paymentReceipts.get(requestId);
        if (existing != null) {
            if (new BigDecimal(existing.getString("requestedAmount")).compareTo(amount.getAmount()) != 0)
                throw new IllegalArgumentException("ID pembayaran sudah digunakan untuk nominal berbeda");
            return new JSONObject(existing.toString());
        }
        if (isClosed()) throw new IllegalStateException("Pinjaman sudah lunas atau ditutup");

        if (now < createdTimestamp) throw new IllegalArgumentException("Pembayaran mendahului kontrak");
        LoanContract projected = restoreFromState(exportState());
        projected.processDailyAccrual(now);
        Money totalDue = projected.getTotalPayoffAmount();
        Money paymentAmount = amount.compareTo(totalDue) > 0 ? totalDue : amount;
        if (account.getAvailableCash(Currency.IDR).isLessThan(paymentAmount))
            throw new IllegalStateException("Saldo kas IDR tidak mencukupi untuk pembayaran");
        processDailyAccrual(now);

        // ponytail: the ledger owns cash; one validated payment needs no second cash mutation.
        // Alokasi pembayaran: Penalti -> Bunga -> Pokok
        Money rem = paymentAmount;
        if (!accruedPenalty.isZero()) {
            Money payPen = rem.compareTo(accruedPenalty) > 0 ? accruedPenalty : rem;
            ledger.recordMutation("PAY-PEN-" + id + "-" + requestId, account, LedgerEntryType.TRANSACTION_FEE, payPen, false, now, "Bayar penalti pinjaman " + id);
            allocatePayment(payPen, 0);
            accruedPenalty = accruedPenalty.subtract(payPen);
            rem = rem.subtract(payPen);
        }

        if (!rem.isZero() && !accruedInterest.isZero()) {
            Money payInt = rem.compareTo(accruedInterest) > 0 ? accruedInterest : rem;
            ledger.recordMutation("PAY-INT-" + id + "-" + requestId, account, LedgerEntryType.TRANSACTION_FEE, payInt, false, now, "Bayar bunga pinjaman " + id);
            allocatePayment(payInt, 1);
            accruedInterest = accruedInterest.subtract(payInt);
            rem = rem.subtract(payInt);
        }

        if (!rem.isZero()) {
            Money payPrin = rem.compareTo(remainingPrincipal) > 0 ? remainingPrincipal : rem;
            ledger.recordMutation("PAY-PRIN-" + id + "-" + requestId, account, LedgerEntryType.LOAN_REPAYMENT, payPrin, false, now, "Bayar pokok pinjaman " + id);
            allocatePayment(payPrin, 2);
            remainingPrincipal = remainingPrincipal.subtract(payPrin);
        }

        refreshStatus();
        if (isClosed() && boundCollateral != null && boundCollateral.isPledged())
            boundCollateral.releaseLoanPledge(id, getTotalPayoffAmount());

        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("loanId", id);
        res.put("requestId", requestId);
        res.put("requestedAmount", amount.getAmount().toPlainString());
        res.put("paymentTimestamp", now);
        res.put("effectiveGameDay", lastAccrualDay);
        res.put("paymentAmount", paymentAmount.format());
        res.put("remainingPrincipal", remainingPrincipal.format());
        res.put("totalRemainingDue", getTotalPayoffAmount().format());
        res.put("status", status.name());
        res.put("message", status == LoanStatus.PAID_OFF ? "Pinjaman berhasil DILUNASI!" : "Pembayaran pinjaman berhasil dicatat.");
        paymentReceipts.put(requestId, new JSONObject(res.toString()));
        return res;
    }

    /**
     * Pelunasan awal penuh (Prepay).
     */
    public synchronized JSONObject prepayFull(Account account, EconomyLedger ledger, long now) throws JSONException {
        return prepayFull(UUID.randomUUID().toString(), account, ledger, now);
    }

    public synchronized JSONObject prepayFull(String requestId, Account account, EconomyLedger ledger, long now) throws JSONException {
        if (requestId == null || requestId.trim().isEmpty() || requestId.length() > 160
                || account == null || !accountId.equals(account.getId()) || now < createdTimestamp || ledger == null)
            throw new IllegalArgumentException("Input pelunasan tidak sah");
        JSONObject existing = paymentReceipts.get(requestId);
        Money requested;
        if (existing == null) {
            LoanContract projected = restoreFromState(exportState()); projected.processDailyAccrual(now);
            requested = projected.getTotalPayoffAmount();
        } else requested = new Money(new BigDecimal(existing.getString("requestedAmount")), Currency.IDR);
        return makePayment(requestId, account, requested, ledger, now);
    }

    /**
     * Restrukturisasi pinjaman (perpanjang tenor).
     */
    public synchronized JSONObject restructure(int additionalDays, long now) throws JSONException {
        if (isClosed()) throw new IllegalStateException("Pinjaman sudah lunas atau ditutup");
        if (additionalDays <= 0) throw new IllegalArgumentException("Perpanjangan tenor harus positif");
        if (now < createdTimestamp) throw new IllegalArgumentException("Restrukturisasi mendahului kontrak");
        if (additionalDays > 3650) throw new IllegalArgumentException("Perpanjangan maksimal 3650 hari");
        processDailyAccrual(now);
        int newDays = Math.addExact(remainingDays, additionalDays);
        if (newDays > 3650) throw new IllegalArgumentException("Tenor hasil restrukturisasi maksimal 3650 hari game");
        long effectiveDay = Math.max(lastAccrualDay, day(now));
        JSONObject audit = new JSONObject().put("timestamp", now).put("effectiveDay", effectiveDay)
                .put("previousVersion", scheduleVersion).put("previousSchedule", getSchedule())
                .put("remainingPrincipal", remainingPrincipal.getAmount().toPlainString())
                .put("accruedInterest", accruedInterest.getAmount().toPlainString()).put("accruedPenalty", accruedPenalty.getAmount().toPlainString());
        List<Installment> replacement = new ArrayList<>();
        Installment carry = new Installment(effectiveDay, Money.zero(Currency.IDR));
        carry.interestDue = accruedInterest; carry.penaltyDue = accruedPenalty;
        replacement.add(carry);
        schedule.clear(); schedule.addAll(replacement);
        buildSchedule(remainingPrincipal, newDays, effectiveDay);
        restructureHistory.add(audit); scheduleVersion++;
        this.lastAccrualDay = effectiveDay;
        refreshStatus();

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
        obj.put("collateralId", collateralId == null ? JSONObject.NULL : collateralId);
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
        obj.put("scheduleVersion", scheduleVersion);
        obj.put("overduePrincipal", getOverduePrincipal().getAmount().toPlainString());
        obj.put("schedule", getSchedule());
        obj.put("disbursementFeePaid", originalPrincipal.multiply(DISBURSEMENT_FEE_RATE).getAmount().toPlainString());
        obj.put("restructureHistory", new JSONArray(new JSONArray(restructureHistory).toString()));
        return obj;
    }

    public JSONObject exportState() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("productId", productId);
        obj.put("collateralId", collateralId == null ? JSONObject.NULL : collateralId);
        obj.put("accountId", accountId);
        obj.put("originalPrincipal", originalPrincipal.getAmount().toPlainString());
        obj.put("remainingPrincipal", remainingPrincipal.getAmount().toPlainString());
        obj.put("tenorDays", tenorDays);
        obj.put("remainingDays", remainingDays);
        obj.put("accruedInterest", accruedInterest.getAmount().toPlainString());
        obj.put("accruedPenalty", accruedPenalty.getAmount().toPlainString());
        obj.put("status", status.name());
        obj.put("createdTimestamp", createdTimestamp);
        obj.put("paymentReceipts", new JSONObject(new JSONObject(paymentReceipts).toString()));
        obj.put("stateSchemaVersion", 2);
        obj.put("lastRealPenaltyTimestamp", lastRealPenaltyTimestamp);
        obj.put("disbursementPayload", disbursementPayload == null ? JSONObject.NULL : disbursementPayload);
        obj.put("disbursementReceipt", disbursementReceipt == null ? JSONObject.NULL : new JSONObject(disbursementReceipt.toString()));
        obj.put("scheduleVersion", scheduleVersion);
        obj.put("lastAccrualDay", lastAccrualDay);
        obj.put("legacyScheduleMigration", legacyScheduleMigration);
        obj.put("totalPenaltyAssessed", totalPenaltyAssessed.getAmount().toPlainString());
        obj.put("schedule", getSchedule());
        obj.put("restructureHistory", new JSONArray(new JSONArray(restructureHistory).toString()));
        return obj;
    }

    private static Money amount(JSONObject obj, String key) throws JSONException {
        BigDecimal value = new BigDecimal(obj.getString(key)).setScale(0, RoundingMode.UNNECESSARY);
        if (value.signum() < 0) throw new IllegalArgumentException("Nominal jadwal negatif");
        return new Money(value, Currency.IDR);
    }

    public static LoanContract restoreFromState(JSONObject obj) throws JSONException {
        if (obj == null) throw new IllegalArgumentException("State pinjaman kosong");
        String id = obj.optString("id");
        String pId = obj.optString("productId");
        String aId = obj.optString("accountId");
        Money origP = new Money(new BigDecimal(obj.optString("originalPrincipal", "1000000")), Currency.IDR);
        int tenor = obj.optInt("tenorDays", 14);
        long ts = obj.getLong("createdTimestamp");

        LoanContract contract = new LoanContract(id, pId, aId, origP, tenor, ts);
        contract.remainingPrincipal = new Money(new BigDecimal(obj.optString("remainingPrincipal", origP.getAmount().toPlainString())), Currency.IDR);
        contract.remainingDays = obj.optInt("remainingDays", tenor);
        contract.accruedInterest = new Money(new BigDecimal(obj.optString("accruedInterest", "0")), Currency.IDR);
        contract.accruedPenalty = new Money(new BigDecimal(obj.optString("accruedPenalty", "0")), Currency.IDR);
        contract.lastRealPenaltyTimestamp = obj.optLong("lastRealPenaltyTimestamp", 0);
        contract.status = LoanStatus.valueOf(obj.optString("status", "ACTIVE"));
        // ponytail: legacy restructuring never settles debt; retain it as an active contract.
        if (contract.status == LoanStatus.RESTRUCTURED) contract.status = LoanStatus.ACTIVE;
        if (id.trim().isEmpty() || pId.trim().isEmpty() || aId.trim().isEmpty()
                || (!pId.equals("LOAN-PERS-01") && !pId.equals("LOAN-PERS-02") && !pId.equals("LOAN-CORP-01"))
                || !origP.isPositive() || (tenor != 7 && tenor != 14 && tenor != 30)
                || contract.remainingPrincipal.isNegative() || contract.remainingPrincipal.compareTo(origP) > 0
                || contract.remainingDays < 0 || contract.accruedInterest.isNegative() || contract.accruedPenalty.isNegative()
                || (contract.status == LoanStatus.PAID_OFF && !contract.getTotalPayoffAmount().isZero()))
            throw new IllegalArgumentException("State pinjaman tidak valid");
        JSONObject receipts = obj.optJSONObject("paymentReceipts");
        if (receipts != null) {
            Iterator<String> keys = receipts.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject receipt = receipts.getJSONObject(key);
                if (key.trim().isEmpty() || key.length() > 160 || !key.equals(receipt.getString("requestId"))
                        || !id.equals(receipt.getString("loanId")) || !receipt.getBoolean("success")
                        || new BigDecimal(receipt.getString("requestedAmount")).signum() <= 0)
                    throw new IllegalArgumentException("Receipt pembayaran tidak valid");
                contract.paymentReceipts.put(key, new JSONObject(receipt.toString()));
            }
        }
        if (obj.has("stateSchemaVersion")) {
            if (new BigDecimal(obj.get("stateSchemaVersion").toString()).intValueExact() != 2) throw new IllegalArgumentException("Versi jadwal pinjaman tidak didukung");
            contract.lastAccrualDay = obj.getLong("lastAccrualDay");
            contract.scheduleVersion = obj.getInt("scheduleVersion");
            contract.legacyScheduleMigration = obj.getBoolean("legacyScheduleMigration");
            contract.totalPenaltyAssessed = amount(obj, "totalPenaltyAssessed");
            if (contract.lastAccrualDay < day(ts) || contract.scheduleVersion < 1
                    || contract.totalPenaltyAssessed.compareTo(contract.accruedPenalty) < 0)
                throw new IllegalArgumentException("Anchor/cap jadwal pinjaman tidak sah");
            contract.schedule.clear();
            JSONArray rows = obj.getJSONArray("schedule");
            long previousDay = Long.MIN_VALUE;
            Money principalRemaining = Money.zero(Currency.IDR), interestRemaining = principalRemaining, penaltyRemaining = principalRemaining;
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                long dueDay = row.getLong("dueDay");
                if (dueDay <= previousDay || !LocalDate.ofEpochDay(dueDay).toString().equals(row.getString("dueDate")))
                    throw new IllegalArgumentException("Tanggal jadwal tidak sah");
                previousDay = dueDay;
                Installment installment = new Installment(dueDay, amount(row, "principalDue"));
                installment.principalPaid = amount(row, "principalPaid");
                installment.interestDue = amount(row, "interestDue"); installment.interestPaid = amount(row, "interestPaid");
                installment.penaltyDue = amount(row, "penaltyDue"); installment.penaltyPaid = amount(row, "penaltyPaid");
                if (installment.principalPaid.compareTo(installment.principalDue) > 0
                        || installment.interestPaid.compareTo(installment.interestDue) > 0 || installment.penaltyPaid.compareTo(installment.penaltyDue) > 0)
                    throw new IllegalArgumentException("Pembayaran jadwal melampaui tagihan");
                principalRemaining = principalRemaining.add(installment.principalDue.subtract(installment.principalPaid));
                interestRemaining = interestRemaining.add(installment.interestDue.subtract(installment.interestPaid));
                penaltyRemaining = penaltyRemaining.add(installment.penaltyDue.subtract(installment.penaltyPaid));
                contract.schedule.add(installment);
            }
            if (rows.length() == 0 || !principalRemaining.equals(contract.remainingPrincipal)
                    || !interestRemaining.equals(contract.accruedInterest) || !penaltyRemaining.equals(contract.accruedPenalty)
                    || contract.remainingDays != Math.max(0, previousDay - contract.lastAccrualDay))
                throw new IllegalArgumentException("Jadwal tidak cocok dengan kewajiban kontrak");
            JSONArray history = obj.getJSONArray("restructureHistory");
            if (history.length() != contract.scheduleVersion - 1) throw new IllegalArgumentException("Versi riwayat restrukturisasi tidak cocok");
            for (int i = 0; i < history.length(); i++) contract.restructureHistory.add(new JSONObject(history.getJSONObject(i).toString()));
            LoanStatus savedStatus = contract.status; contract.refreshStatus();
            if (savedStatus != contract.status) throw new IllegalArgumentException("Status tidak cocok dengan jadwal pinjaman");
        } else {
            if (obj.has("schedule") || obj.has("lastAccrualDay") || obj.has("restructureHistory"))
                throw new IllegalArgumentException("Jadwal modern tanpa versi");
            // Legacy counter did not persist days elapsed after maturity. Preserve all saved balances exactly;
            // future accrual resumes at the minimum known date, rather than inventing historic charges.
            contract.legacyScheduleMigration = true;
            contract.legacyAnchorUnresolved = true;
            contract.lastAccrualDay = day(ts) + Math.max(0, tenor - contract.remainingDays);
            contract.totalPenaltyAssessed = contract.accruedPenalty;
            contract.schedule.clear();
            int daysLeft = Math.max(1, contract.remainingDays);
            long anchor = contract.lastAccrualDay;
            Installment carry = new Installment(anchor, Money.zero(Currency.IDR));
            carry.interestDue = contract.accruedInterest; carry.penaltyDue = contract.accruedPenalty;
            contract.schedule.add(carry);
            if (contract.remainingDays == 0) {
                carry.principalDue = contract.remainingPrincipal;
            } else contract.buildSchedule(contract.remainingPrincipal, daysLeft, anchor);
            contract.remainingDays = Math.max(0, contract.remainingDays);
            contract.refreshStatus();
        }
        if (!obj.isNull("collateralId")) {
            contract.collateralId = obj.getString("collateralId");
            if (contract.collateralId.trim().isEmpty() || !pId.equals("LOAN-PERS-01")) throw new IllegalArgumentException("Agunan kontrak tidak sah");
        }
        if (!obj.isNull("disbursementReceipt")) {
            JSONObject receipt = obj.getJSONObject("disbursementReceipt");
            String payload = obj.getString("disbursementPayload");
            if (!receipt.getBoolean("success") || !id.equals(receipt.getString("loanId"))
                    || !aId.equals(receipt.getString("accountId")) || receipt.getString("requestId").trim().isEmpty()
                    || payload.trim().isEmpty())
                throw new IllegalArgumentException("Receipt pencairan tidak sah");
            contract.recordDisbursement(payload, receipt);
        } else if (!obj.isNull("disbursementPayload")) throw new IllegalArgumentException("Payload pencairan tanpa receipt");
        return contract;
    }
}
