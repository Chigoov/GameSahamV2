package com.tradingglobal3d.core.multiasset;

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
 * Pengelola Deposito Berjangka Perbankan (R10).
 * Menangani penempatan, pencairan jatuh tempo otomatis, dan penalti penarikan dini.
 */
public final class TimeDepositManager {
    public static final class DepositProduct {
        public final String id;
        public final String bankName;
        public final int tenorDays;
        public final double annualInterestRate;
        public final Money minPlacement;

        public DepositProduct(String id, String bankName, int tenorDays, double annualInterestRate, Money minPlacement) {
            this.id = id;
            this.bankName = bankName;
            this.tenorDays = tenorDays;
            this.annualInterestRate = annualInterestRate;
            this.minPlacement = minPlacement;
        }

        public JSONObject toJsonObject() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("bankName", bankName);
            obj.put("tenorDays", tenorDays);
            obj.put("annualInterestRate", String.format(Locale.ROOT, "%.2f%%", annualInterestRate * 100));
            obj.put("minPlacement", minPlacement.format());
            obj.put("minPlacementRaw", minPlacement.getAmount().toPlainString());
            return obj;
        }
    }

    private final Map<String, DepositProduct> products = new LinkedHashMap<>();
    private final Map<String, TimeDeposit> activeDeposits = new LinkedHashMap<>();
    private final List<TimeDeposit> depositHistory = new ArrayList<>();

    public TimeDepositManager() {
        initializeProducts();
    }

    private void initializeProducts() {
        products.put("DEP-BCA-1M", new DepositProduct("DEP-BCA-1M", "PT Bank Central Asia Tbk", 30, 0.035, Money.of(10_000_000, Currency.IDR)));
        products.put("DEP-BMRI-3M", new DepositProduct("DEP-BMRI-3M", "PT Bank Mandiri (Persero) Tbk", 90, 0.0425, Money.of(10_000_000, Currency.IDR)));
        products.put("DEP-BBRI-6M", new DepositProduct("DEP-BBRI-6M", "PT Bank Rakyat Indonesia Tbk", 180, 0.050, Money.of(10_000_000, Currency.IDR)));
    }

    public synchronized void reset() {
        products.clear();
        activeDeposits.clear();
        depositHistory.clear();
        initializeProducts();
    }

    public List<DepositProduct> getProducts() {
        return new ArrayList<>(products.values());
    }

    public synchronized TimeDeposit openDeposit(Account account, String productId, Money principal, long now, EconomyLedger ledger) {
        DepositProduct prod = products.get(productId);
        if (prod == null) throw new IllegalArgumentException("Produk deposito tidak ditemukan: " + productId);
        if (principal.isLessThan(prod.minPlacement)) {
            throw new IllegalArgumentException("Nominal minimal penempatan deposito adalah " + prod.minPlacement.format());
        }
        Money available = account.getAvailableCash(principal.getCurrency());
        if (available.isLessThan(principal)) {
            throw new IllegalStateException("Saldo kas " + principal.getCurrency() + " tidak mencukupi");
        }

        String depositId = "DEP-" + System.currentTimeMillis() + "-" + (activeDeposits.size() + 1);
        // Mutasi Kas: Debit penempatan deposito
        ledger.recordMutation(
                "DEPOSIT-OPEN-" + depositId,
                account,
                LedgerEntryType.TIME_DEPOSIT_PLACEMENT,
                principal,
                false, // debit kas
                now,
                "Penempatan " + prod.bankName + " (" + prod.tenorDays + " Hari)"
        );

        TimeDeposit dep = new TimeDeposit(depositId, prod.id, prod.bankName, principal, prod.annualInterestRate, prod.tenorDays, now);
        activeDeposits.put(depositId, dep);
        return dep;
    }

    public synchronized TimeDeposit withdrawDeposit(String depositId, Account account, long now, EconomyLedger ledger) {
        TimeDeposit dep = activeDeposits.get(depositId);
        if (dep == null) throw new IllegalArgumentException("Deposito aktif tidak ditemukan: " + depositId);

        boolean matured = dep.isMatured(now);
        Money principal = dep.getPrincipal();

        if (matured) {
            Money interest = dep.calculateAccruedInterest(now);
            // Kredit Pokok
            ledger.recordMutation(
                    "DEP-MATURITY-PRIN-" + depositId,
                    account,
                    LedgerEntryType.TIME_DEPOSIT_MATURITY,
                    principal,
                    true,
                    now,
                    "Pencairan Pokok Deposito " + dep.getBankName()
            );
            // Kredit Bunga
            if (interest.isPositive()) {
                ledger.recordMutation(
                        "DEP-MATURITY-INT-" + depositId,
                        account,
                        LedgerEntryType.TIME_DEPOSIT_INTEREST,
                        interest,
                        true,
                        now,
                        "Bunga Jatuh Tempo Deposito " + dep.getBankName()
                );
            }
            dep.markMatured(interest);
        } else {
            // Pencairan Dini: Penalti 50% bunga yang sudah berjalan
            Money accrued = dep.calculateAccruedInterest(now);
            BigDecimal penaltyAmt = accrued.getAmount().multiply(new BigDecimal("0.50")).setScale(0, RoundingMode.HALF_UP);
            Money netInterest = accrued.subtract(new Money(penaltyAmt, accrued.getCurrency()));
            Money penalty = new Money(penaltyAmt, accrued.getCurrency());

            ledger.recordMutation(
                    "DEP-EARLY-PRIN-" + depositId,
                    account,
                    LedgerEntryType.TIME_DEPOSIT_EARLY_WITHDRAWAL,
                    principal,
                    true,
                    now,
                    "Pencairan Dini Pokok Deposito " + dep.getBankName()
            );
            if (netInterest.isPositive()) {
                ledger.recordMutation(
                        "DEP-EARLY-INT-" + depositId,
                        account,
                        LedgerEntryType.TIME_DEPOSIT_INTEREST,
                        netInterest,
                        true,
                        now,
                        "Bunga Bersih Pencairan Dini Deposito " + dep.getBankName()
                );
            }
            dep.markEarlyWithdrawn(netInterest, penalty);
        }

        activeDeposits.remove(depositId);
        depositHistory.add(dep);
        return dep;
    }

    public synchronized void processDailyMaturities(Account account, long now, EconomyLedger ledger) {
        List<String> maturedIds = new ArrayList<>();
        for (TimeDeposit d : activeDeposits.values()) {
            if (d.isMatured(now)) {
                maturedIds.add(d.getId());
            }
        }
        for (String id : maturedIds) {
            withdrawDeposit(id, account, now, ledger);
        }
    }

    public synchronized JSONArray exportActiveDeposits(long now) throws JSONException {
        JSONArray arr = new JSONArray();
        for (TimeDeposit d : activeDeposits.values()) {
            arr.put(d.toJsonObject(now));
        }
        return arr;
    }

    public synchronized JSONArray exportProducts() throws JSONException {
        JSONArray arr = new JSONArray();
        for (DepositProduct p : products.values()) {
            arr.put(p.toJsonObject());
        }
        return arr;
    }
}
