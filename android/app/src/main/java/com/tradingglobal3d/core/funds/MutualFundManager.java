package com.tradingglobal3d.core.funds;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.trading.Order;

/**
 * Manajer Katalog dan Transaksi Reksa Dana Berbasis Unit/NAB.
 * Memuat 3 strategi reksa dana sesuai D06:
 * 1. Pasar Uang (Nusantara Kas Lancar)
 * 2. Pendapatan Tetap / Obligasi (Nusantara Obligasi Mantap)
 * 3. Indeks Saham (Nusantara Indeks Saham Unggulan)
 */
public final class MutualFundManager {

    private final Map<String, MutualFund> funds = new LinkedHashMap<>();

    public MutualFundManager() {
        initializeDefaultFunds();
    }

    private void initializeDefaultFunds() {
        funds.clear();
        funds.put("FUND-MM-01", new MutualFund(
                "FUND-MM-01",
                "Nusantara Kas Lancar",
                MutualFund.FundType.MONEY_MARKET,
                "PT Nusantara Manajemen Investasi"
        ));
        funds.put("FUND-FI-01", new MutualFund(
                "FUND-FI-01",
                "Nusantara Obligasi Mantap",
                MutualFund.FundType.FIXED_INCOME,
                "PT Mandiri Investasi Semesta"
        ));
        funds.put("FUND-EQ-01", new MutualFund(
                "FUND-EQ-01",
                "Nusantara Indeks Saham Unggulan",
                MutualFund.FundType.EQUITY_INDEX,
                "PT Bahana Kapital Prima"
        ));
    }

    public synchronized void reset() {
        initializeDefaultFunds();
    }

    public synchronized Collection<MutualFund> getAllFunds() {
        return Collections.unmodifiableCollection(funds.values());
    }

    public synchronized MutualFund getFund(String fundId) {
        return funds.get(fundId);
    }

    public synchronized void advanceDay(double marketFactor, EconomyLedger ledger, long now) {
        for (MutualFund fund : funds.values()) {
            fund.advanceDay(marketFactor, ledger, now);
        }
    }

    public synchronized void registerPoolAccounts(Map<String, Account> accounts, EconomyLedger ledger, long now) {
        for (MutualFund fund : funds.values()) fund.registerPoolAccount(accounts, ledger, now);
    }
    public synchronized void bindAccounts(Map<String, Account> accounts, InstrumentCatalog catalog) {
        for (MutualFund fund : funds.values()) fund.bindAccounts(accounts, catalog);
    }
    public synchronized List<Order> getEquityRebalanceOrders(InstrumentCatalog catalog, long now) {
        List<Order> orders = new ArrayList<>();
        for (MutualFund fund : funds.values()) {
            Order order = fund.getEquityRebalanceOrder(catalog, now);
            if (order != null) orders.add(order);
        }
        return orders;
    }

    public synchronized void processCutoff(Map<String, Account> accounts, EconomyLedger ledger, long gameTimestamp) throws JSONException {
        for (MutualFund fund : funds.values()) fund.processCutoff(accounts, ledger, gameTimestamp);
    }

    /**
     * Menghitung total nilai seluruh unit reksa dana yang dimiliki oleh akun tertentu.
     */
    public synchronized Money calculateTotalHoldingsValue(String accountId) {
        if (accountId == null) return Money.zero(Currency.IDR);
        Money total = Money.zero(Currency.IDR);
        for (MutualFund fund : funds.values()) {
            total = total.add(fund.getHoldingsValue(accountId));
        }
        return total;
    }

    private String lastRebalanceFailure;
    private long lastRebalanceFailureTimestamp;

    /**
     * Catat kegagalan rebalance agar dapat diperiksa melalui API.
     */
    public synchronized void recordRebalanceFailure(String reason, long timestamp) {
        this.lastRebalanceFailure = reason;
        this.lastRebalanceFailureTimestamp = timestamp;
    }

    public synchronized String getLastRebalanceFailure() { return lastRebalanceFailure; }
    public synchronized long getLastRebalanceFailureTimestamp() { return lastRebalanceFailureTimestamp; }

    public synchronized JSONArray exportState() throws JSONException {
        JSONArray arr = new JSONArray();
        for (MutualFund fund : funds.values()) {
            arr.put(fund.exportState());
        }
        return arr;
    }

    public synchronized Money getReservedSubscriptionCash(String accountId) {
        Money reserved = Money.zero(Currency.IDR);
        for (MutualFund fund : funds.values()) reserved = reserved.add(fund.getReservedSubscriptionCash(accountId));
        return reserved;
    }

    public synchronized void validateAccounts(Map<String, Account> accounts) {
        for (MutualFund fund : funds.values()) fund.validateAccounts(accounts);
        for (Map.Entry<String, Account> entry : accounts.entrySet()) {
            if (!entry.getKey().equals(entry.getValue().getId())) throw new IllegalArgumentException("ID akun canonical berbeda");
            if (entry.getValue().getReservedCash(Currency.IDR).isLessThan(getReservedSubscriptionCash(entry.getKey())))
                throw new IllegalArgumentException("Reservasi kas order reksa dana tidak sesuai akun");
        }
    }

    public synchronized void restoreState(JSONArray arr) throws JSONException {
        MutualFundManager staged = new MutualFundManager();
        if (arr == null || arr.length() != staged.funds.size()) throw new IllegalArgumentException("Katalog reksa dana tersimpan tidak lengkap");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String id = obj.getString("id");
            MutualFund fund = staged.funds.get(id);
            if (fund == null || !seen.add(id)) throw new IllegalArgumentException("ID reksa dana tidak dikenal/duplikat");
            fund.restoreState(obj);
        }
        funds.clear(); funds.putAll(staged.funds);
    }
}
