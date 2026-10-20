package com.tradingglobal3d.core.funds;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

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

    public synchronized void advanceDay(double marketFactor) {
        for (MutualFund fund : funds.values()) {
            fund.advanceDay(marketFactor);
        }
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

    public synchronized JSONArray exportState() throws JSONException {
        JSONArray arr = new JSONArray();
        for (MutualFund fund : funds.values()) {
            arr.put(fund.exportState());
        }
        return arr;
    }

    public synchronized void restoreState(JSONArray arr) throws JSONException {
        if (arr == null) return;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String id = obj.optString("id");
            MutualFund fund = funds.get(id);
            if (fund != null) {
                fund.restoreState(obj);
            }
        }
    }
}
