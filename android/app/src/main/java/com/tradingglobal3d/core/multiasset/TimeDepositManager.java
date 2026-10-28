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
        public final double bonusInterestPercent;
        public final Money minPlacement;

        public DepositProduct(String id, String bankName, int tenorDays, double annualInterestRate,
                              double bonusInterestPercent, Money minPlacement) {
            this.id = id;
            this.bankName = bankName;
            this.tenorDays = tenorDays;
            this.annualInterestRate = annualInterestRate;
            this.bonusInterestPercent = bonusInterestPercent;
            this.minPlacement = minPlacement;
        }

        public JSONObject toJsonObject() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("bankName", bankName);
            obj.put("tenorDays", tenorDays);
            obj.put("tenorSessions", tenorDays);
            obj.put("annualInterestRate", String.format(Locale.ROOT, "%.2f%%", annualInterestRate * 100));
            obj.put("bonusInterestPercent", bonusInterestPercent);
            obj.put("effectiveAnnualInterestRate", String.format(Locale.ROOT, "%.2f%%",
                    annualInterestRate * (1.0 + bonusInterestPercent / 100.0) * 100.0));
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
        products.put("DEP-BCA-1M", new DepositProduct("DEP-BCA-1M", "PT Bank Central Asia Tbk", 30, 0.035, 0.0, Money.of(10_000_000, Currency.IDR)));
        products.put("DEP-BMRI-3M", new DepositProduct("DEP-BMRI-3M", "PT Bank Mandiri (Persero) Tbk", 90, 0.0425, 0.0, Money.of(10_000_000, Currency.IDR)));
        products.put("DEP-BBRI-6M", new DepositProduct("DEP-BBRI-6M", "PT Bank Rakyat Indonesia Tbk", 180, 0.050, 0.0, Money.of(10_000_000, Currency.IDR)));
        // Produk Tenor Sesi Khusus Fase 3 (Fitur 1)
        products.put("DEP-TENOR-10", new DepositProduct("DEP-TENOR-10", "Deposito Tenor 10 Sesi (+10% Bonus)", 10, 0.045, 10.0, Money.of(10_000_000, Currency.IDR)));
        products.put("DEP-TENOR-25", new DepositProduct("DEP-TENOR-25", "Deposito Tenor 25 Sesi (+25% Bonus)", 25, 0.060, 25.0, Money.of(25_000_000, Currency.IDR)));
        products.put("DEP-TENOR-50", new DepositProduct("DEP-TENOR-50", "Deposito Tenor 50 Sesi (+50% Bonus)", 50, 0.080, 50.0, Money.of(50_000_000, Currency.IDR)));
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

        TimeDeposit dep = new TimeDeposit(depositId, prod.id, prod.bankName, principal, prod.annualInterestRate,
                prod.bonusInterestPercent, prod.tenorDays, now);
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
            // Pencairan Dini: Penalti Bunga Hangus (Lost Interest Penalty - Fase 3 Fitur 1)
            Money accrued = dep.calculateAccruedInterest(now);
            Money netInterest = Money.zero(principal.getCurrency());
            Money penalty = accrued;

            ledger.recordMutation(
                    "DEP-EARLY-PRIN-" + depositId,
                    account,
                    LedgerEntryType.TIME_DEPOSIT_EARLY_WITHDRAWAL,
                    principal,
                    true,
                    now,
                    "Pencairan Dini Pokok Deposito " + dep.getBankName() + " (Bunga Akrual Hangus)"
            );
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

    public synchronized JSONObject exportState() throws JSONException {
        JSONArray active = new JSONArray();
        for (TimeDeposit deposit : activeDeposits.values()) active.put(deposit.exportState());
        JSONArray history = new JSONArray();
        for (TimeDeposit deposit : depositHistory) history.put(deposit.exportState());
        return new JSONObject().put("active", active).put("history", history);
    }

    public synchronized void restoreState(JSONObject state) throws JSONException {
        Map<String, TimeDeposit> stagedActive = new LinkedHashMap<>();
        List<TimeDeposit> stagedHistory = new ArrayList<>();
        JSONArray active = state.optJSONArray("active");
        JSONArray history = state.optJSONArray("history");
        if (active != null) for (int i = 0; i < active.length(); i++) {
            TimeDeposit deposit = TimeDeposit.fromState(active.getJSONObject(i));
            if (!"ACTIVE".equals(deposit.getStatus()) || stagedActive.put(deposit.getId(), deposit) != null)
                throw new IllegalArgumentException("Deposito aktif save duplikat atau status tidak valid");
        }
        if (history != null) for (int i = 0; i < history.length(); i++) {
            TimeDeposit deposit = TimeDeposit.fromState(history.getJSONObject(i));
            if ("ACTIVE".equals(deposit.getStatus()) || stagedActive.containsKey(deposit.getId()))
                throw new IllegalArgumentException("Riwayat deposito save tidak valid");
            stagedHistory.add(deposit);
        }
        activeDeposits.clear(); activeDeposits.putAll(stagedActive);
        depositHistory.clear(); depositHistory.addAll(stagedHistory);
    }
}
