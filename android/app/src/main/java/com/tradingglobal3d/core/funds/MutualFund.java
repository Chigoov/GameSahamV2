package com.tradingglobal3d.core.funds;

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
 * Representasi produk Reksa Dana berbasis Unit dan Nilai Aktiva Bersih (NAB).
 * Mematuhi spesifikasi D06:
 * - 3 strategi: Pasar Uang, Pendapatan Tetap (Obligasi), Indeks Saham.
 * - NAB awal Rp 10.000 / unit. Presisi unit: 6 desimal.
 * - Biaya beli (entry fee): 0.1%, biaya jual (exit fee): 0.1%.
 * - Biaya manajemen: 0.75% per tahun diakru harian.
 * - Cutoff dealing 16:00 WIB.
 */
public final class MutualFund {

    public enum FundType {
        MONEY_MARKET("Pasar Uang", "Rendah", 0.05),
        FIXED_INCOME("Pendapatan Tetap", "Moderat", 0.08),
        EQUITY_INDEX("Indeks Saham", "Agresif", 0.12);

        private final String label;
        private final String riskLevel;
        private final double targetAnnualYield;

        FundType(String label, String riskLevel, double targetAnnualYield) {
            this.label = label;
            this.riskLevel = riskLevel;
            this.targetAnnualYield = targetAnnualYield;
        }

        public String getLabel() { return label; }
        public String getRiskLevel() { return riskLevel; }
        public double getTargetAnnualYield() { return targetAnnualYield; }
    }

    public static final class PendingFundOrder {
        final String orderId;
        final String accountId;
        final boolean isSubscribe;
        final Money cashAmount; // untuk subscribe
        final BigDecimal units; // untuk redeem
        final long requestTimestamp;

        PendingFundOrder(String orderId, String accountId, boolean isSubscribe, Money cashAmount, BigDecimal units, long requestTimestamp) {
            this.orderId = orderId;
            this.accountId = accountId;
            this.isSubscribe = isSubscribe;
            this.cashAmount = cashAmount;
            this.units = units != null ? units.setScale(6, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            this.requestTimestamp = requestTimestamp;
        }
    }

    private final String id;
    private final String name;
    private final FundType type;
    private final String managerName;
    private BigDecimal navPerUnit; // Rupiah per unit
    private BigDecimal totalUnits; // Total unit beredar
    private Money poolCash; // Kas dalam portofolio reksa dana
    private final Map<String, BigDecimal> holdings = new HashMap<>(); // accountId -> units
    private final List<PendingFundOrder> pendingOrders = new ArrayList<>();
    private final List<JSONObject> historicalNavs = new ArrayList<>();

    public static final BigDecimal INITIAL_NAV = new BigDecimal("10000.00");
    public static final BigDecimal ENTRY_FEE_RATE = new BigDecimal("0.001"); // 0.1%
    public static final BigDecimal EXIT_FEE_RATE = new BigDecimal("0.001");  // 0.1%
    public static final BigDecimal MANAGEMENT_FEE_RATE = new BigDecimal("0.0075"); // 0.75% / year

    public MutualFund(String id, String name, FundType type, String managerName) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.managerName = Objects.requireNonNull(managerName);
        this.navPerUnit = INITIAL_NAV;
        this.totalUnits = new BigDecimal("100000.000000"); // 100.000 unit seed awal sponsor
        this.poolCash = Money.of(1_000_000_000L, Currency.IDR); // Rp 1 Miliar seed aset awal
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public FundType getType() { return type; }
    public String getManagerName() { return managerName; }
    public BigDecimal getNavPerUnit() { return navPerUnit; }
    public BigDecimal getTotalUnits() { return totalUnits; }
    public Money getPoolCash() { return poolCash; }

    public BigDecimal getUnits(String accountId) {
        return holdings.getOrDefault(accountId, BigDecimal.ZERO).setScale(6, RoundingMode.HALF_UP);
    }

    public Money getHoldingsValue(String accountId) {
        BigDecimal u = getUnits(accountId);
        if (u.signum() <= 0) return Money.zero(Currency.IDR);
        BigDecimal val = u.multiply(navPerUnit).setScale(0, RoundingMode.HALF_UP);
        return new Money(val, Currency.IDR);
    }

    /**
     * Memajukan waktu harian reksa dana (akru yield aset dan potongan fee pengelolaan).
     */
    public synchronized void advanceDay(double marketFactorChange) {
        // Yield harian
        double dailyYield = (type.getTargetAnnualYield() / 365.0) + (marketFactorChange * (type == FundType.EQUITY_INDEX ? 0.02 : 0.005));
        BigDecimal factor = BigDecimal.ONE.add(BigDecimal.valueOf(dailyYield)).subtract(MANAGEMENT_FEE_RATE.divide(BigDecimal.valueOf(365), 8, RoundingMode.HALF_UP));
        
        this.navPerUnit = this.navPerUnit.multiply(factor).setScale(4, RoundingMode.HALF_UP);
        if (this.navPerUnit.compareTo(BigDecimal.ONE) < 0) {
            this.navPerUnit = BigDecimal.ONE;
        }

        // Catat histori NAB
        JSONObject navRecord = new JSONObject();
        try {
            navRecord.put("time", System.currentTimeMillis());
            navRecord.put("nav", navPerUnit.doubleValue());
        } catch (JSONException ignored) {}
        historicalNavs.add(navRecord);
        if (historicalNavs.size() > 60) historicalNavs.remove(0);
    }

    /**
     * Mengajukan pembelian unit reksa dana (Subscription).
     */
    public synchronized JSONObject submitSubscription(String orderId, Account account, Money grossCash, EconomyLedger ledger, long now) throws JSONException {
        if (grossCash.getCurrency() != Currency.IDR) throw new IllegalArgumentException("Mata uang subscription harus IDR");
        if (grossCash.getAmount().compareTo(BigDecimal.valueOf(10_000)) < 0) {
            throw new IllegalArgumentException("Minimal pembelian reksa dana adalah Rp 10.000");
        }
        if (account.getAvailableCash(Currency.IDR).isLessThan(grossCash)) {
            throw new IllegalStateException("Saldo kas IDR tidak mencukupi");
        }

        // Hitung fee dan reservasi kas
        Money fee = new Money(grossCash.getAmount().multiply(ENTRY_FEE_RATE).setScale(0, RoundingMode.HALF_UP), Currency.IDR);
        Money netCash = grossCash.subtract(fee);

        account.debitCash(grossCash);
        ledger.recordMutation("FEE-FUND-IN-" + orderId, account, LedgerEntryType.TRANSACTION_FEE, fee, false, now, "Biaya beli unit " + name);
        ledger.recordMutation("SUB-FUND-" + orderId, account, LedgerEntryType.TRADE_BUY, netCash, false, now, "Pembelian unit reksa dana " + name);

        // Langsung terbitkan unit pada NAB terkini
        BigDecimal issuedUnits = netCash.getAmount().divide(navPerUnit, 6, RoundingMode.HALF_UP);
        BigDecimal current = holdings.getOrDefault(account.getId(), BigDecimal.ZERO);
        holdings.put(account.getId(), current.add(issuedUnits));
        this.totalUnits = this.totalUnits.add(issuedUnits);
        this.poolCash = this.poolCash.add(netCash);

        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("fundId", id);
        res.put("orderId", orderId);
        res.put("grossAmount", grossCash.format());
        res.put("entryFee", fee.format());
        res.put("netAmount", netCash.format());
        res.put("navPerUnit", navPerUnit.toPlainString());
        res.put("unitsIssued", issuedUnits.toPlainString());
        res.put("totalUnitsHeld", getUnits(account.getId()).toPlainString());
        res.put("message", "Pembelian unit reksa dana " + name + " berhasil!");
        return res;
    }

    /**
     * Mengajukan penjualan unit reksa dana (Redemption).
     */
    public synchronized JSONObject submitRedemption(String orderId, Account account, BigDecimal unitsToRedeem, EconomyLedger ledger, long now) throws JSONException {
        if (unitsToRedeem == null || unitsToRedeem.signum() <= 0) {
            throw new IllegalArgumentException("Jumlah unit redemption harus positif");
        }
        unitsToRedeem = unitsToRedeem.setScale(6, RoundingMode.HALF_UP);
        BigDecimal current = getUnits(account.getId());
        if (current.compareTo(unitsToRedeem) < 0) {
            throw new IllegalStateException("Jumlah unit yang dimiliki tidak mencukupi (Dimiliki: " + current + ", diminta: " + unitsToRedeem + ")");
        }

        BigDecimal grossAmt = unitsToRedeem.multiply(navPerUnit).setScale(0, RoundingMode.HALF_UP);
        Money gross = new Money(grossAmt, Currency.IDR);
        Money fee = new Money(grossAmt.multiply(EXIT_FEE_RATE).setScale(0, RoundingMode.HALF_UP), Currency.IDR);
        Money netProceeds = gross.subtract(fee);

        // Kurangi unit
        holdings.put(account.getId(), current.subtract(unitsToRedeem));
        this.totalUnits = this.totalUnits.subtract(unitsToRedeem);
        this.poolCash = this.poolCash.subtract(gross);

        // Kreditkan kas ke akun pemain
        account.creditCash(netProceeds);
        ledger.recordMutation("RED-FUND-" + orderId, account, LedgerEntryType.TRADE_SELL, gross, true, now, "Pencairan unit reksa dana " + name);
        ledger.recordMutation("FEE-FUND-OUT-" + orderId, account, LedgerEntryType.TRANSACTION_FEE, fee, false, now, "Biaya pencairan unit " + name);

        JSONObject res = new JSONObject();
        res.put("success", true);
        res.put("fundId", id);
        res.put("orderId", orderId);
        res.put("unitsRedeemed", unitsToRedeem.toPlainString());
        res.put("navPerUnit", navPerUnit.toPlainString());
        res.put("grossProceeds", gross.format());
        res.put("exitFee", fee.format());
        res.put("netProceeds", netProceeds.format());
        res.put("remainingUnits", getUnits(account.getId()).toPlainString());
        res.put("message", "Pencairan unit reksa dana " + name + " berhasil!");
        return res;
    }

    public JSONObject toJsonObject(String accountId) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("name", name);
        obj.put("type", type.name());
        obj.put("typeLabel", type.getLabel());
        obj.put("riskLevel", type.getRiskLevel());
        obj.put("managerName", managerName);
        obj.put("navPerUnit", navPerUnit.setScale(2, RoundingMode.HALF_UP).toPlainString());
        obj.put("totalAum", poolCash.format());
        obj.put("totalUnits", totalUnits.setScale(2, RoundingMode.HALF_UP).toPlainString());
        BigDecimal u = accountId != null ? getUnits(accountId) : BigDecimal.ZERO;
        obj.put("myUnits", u.toPlainString());
        obj.put("myHoldingsValue", (accountId != null ? getHoldingsValue(accountId) : Money.zero(Currency.IDR)).format());
        obj.put("entryFee", "0.1%");
        obj.put("exitFee", "0.1%");
        obj.put("managementFee", "0.75% / tahun");
        obj.put("cutoffTime", "16:00 WIB");
        return obj;
    }

    public JSONObject exportState() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("name", name);
        obj.put("type", type.name());
        obj.put("managerName", managerName);
        obj.put("navPerUnit", navPerUnit.toPlainString());
        obj.put("totalUnits", totalUnits.toPlainString());
        obj.put("poolCash", poolCash.getAmount().toPlainString());

        JSONObject hMap = new JSONObject();
        for (Map.Entry<String, BigDecimal> e : holdings.entrySet()) {
            hMap.put(e.getKey(), e.getValue().toPlainString());
        }
        obj.put("holdings", hMap);
        return obj;
    }

    public void restoreState(JSONObject obj) throws JSONException {
        if (obj == null) return;
        this.navPerUnit = new BigDecimal(obj.optString("navPerUnit", INITIAL_NAV.toPlainString()));
        this.totalUnits = new BigDecimal(obj.optString("totalUnits", "100000.000000"));
        this.poolCash = new Money(new BigDecimal(obj.optString("poolCash", "1000000000")), Currency.IDR);
        this.holdings.clear();
        JSONObject hMap = obj.optJSONObject("holdings");
        if (hMap != null) {
            Iterator<String> keys = hMap.keys();
            while (keys.hasNext()) {
                String k = keys.next();
                this.holdings.put(k, new BigDecimal(hMap.getString(k)));
            }
        }
    }
}
