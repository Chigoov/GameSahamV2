package com.tradingglobal3d.core.multiasset;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.firm.ClientMandate;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;
import com.tradingglobal3d.core.trading.OrderType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Layanan Multi-Aset dan Fund Builder (Penyusunan Portofolio Campuran).
 * Mengelola instrumen Obligasi Negara, Komoditas Emas, dan Rebalancing Plan.
 */
public final class FundBuilderService {
    private final Map<String, BondInstrument> bonds = new LinkedHashMap<>();
    private final Map<String, CommodityInstrument> commodities = new LinkedHashMap<>();
    private final Map<String, FundAllocationPlan> savedPlans = new LinkedHashMap<>();

    public FundBuilderService() {
        initializeMultiAssets();
    }

    private void initializeMultiAssets() {
        // Obligasi Negara
        BondInstrument fr91 = new BondInstrument(
                "BOND:FR0091", "FR0091", "Surat Utang Negara FR0091", Currency.IDR,
                Money.of(1_000_000, Currency.IDR),
                new BigDecimal("0.06375"), // 6.375% kupon p.a.
                LocalDate.of(2032, 4, 15),
                Money.of(1_020_000, Currency.IDR)
        );
        bonds.put(fr91.getId(), fr91);

        // Komoditas Emas Fisik
        CommodityInstrument gold = new CommodityInstrument(
                "COMM:GOLD", "XAU", "Emas Spot Fisik Antam", "Gram", Currency.IDR,
                Money.of(1_450_000, Currency.IDR)
        );
        commodities.put(gold.getId(), gold);
    }

    public synchronized void reset() {
        savedPlans.clear();
    }

    public List<BondInstrument> getAllBonds() {
        return new ArrayList<>(bonds.values());
    }

    public List<CommodityInstrument> getAllCommodities() {
        return new ArrayList<>(commodities.values());
    }

    public BondInstrument getBond(String id) { return bonds.get(id); }
    public CommodityInstrument getCommodity(String id) { return commodities.get(id); }

    /**
     * Membayarkan kupon bunga obligasi yang jatuh tempo ke kas pemegang via buku besar.
     */
    public synchronized Money payBondCoupon(
            BondInstrument bond,
            Account holderAccount,
            int daysHeld,
            long unitsHeld,
            EconomyLedger ledger,
            long gameTimestamp) {

        Money coupon = bond.calculateAccruedCoupon(daysHeld, unitsHeld);
        if (coupon.isPositive()) {
            ledger.recordMutation(
                    "COUPON-" + bond.getTicker() + "-" + System.nanoTime(),
                    holderAccount,
                    LedgerEntryType.DIVIDEND,
                    coupon,
                    true,
                    gameTimestamp,
                    "Pembayaran Kupon Bunga " + bond.getName() + " (" + daysHeld + " hari)"
            );
        }
        return coupon;
    }

    /**
     * Menyimpan rencana alokasi portofolio tanpa mengubah posisi fisik (FR-11.05).
     */
    public synchronized FundAllocationPlan saveAllocationPlan(
            String planId,
            String accountId,
            String planName,
            Map<String, Double> targetWeights,
            double cashWeight,
            ClientMandate clientMandate) {

        // Validasi terhadap mandat klien jika ada
        if (clientMandate != null) {
            if (cashWeight < clientMandate.getRiskProfile().getMinCashReserve()) {
                throw new IllegalArgumentException("Rencana melanggar cadangan kas minimum mandat: target " +
                        (cashWeight * 100) + "% < min " + (clientMandate.getRiskProfile().getMinCashReserve() * 100) + "%");
            }
            for (Map.Entry<String, Double> e : targetWeights.entrySet()) {
                if (e.getValue() > clientMandate.getRiskProfile().getMaxConcentrationPerStock()) {
                    throw new IllegalArgumentException("Rencana melanggar batas konsentrasi aset " + e.getKey() + ": target " +
                            (e.getValue() * 100) + "% > max " + (clientMandate.getRiskProfile().getMaxConcentrationPerStock() * 100) + "%");
                }
            }
        }

        FundAllocationPlan plan = new FundAllocationPlan(planId, accountId, planName, targetWeights, cashWeight);
        plan.setStatus(FundAllocationPlan.PlanStatus.SAVED);
        savedPlans.put(planId, plan);
        return plan;
    }

    public synchronized org.json.JSONArray exportState() throws org.json.JSONException {
        org.json.JSONArray result = new org.json.JSONArray();
        for (FundAllocationPlan plan : savedPlans.values()) result.put(new org.json.JSONObject()
                .put("id", plan.getId()).put("accountId", plan.getAccountId()).put("name", plan.getName())
                .put("weights", new org.json.JSONObject(plan.getTargetWeights())).put("cashWeight", plan.getTargetCashWeight())
                .put("status", plan.getStatus().name()).put("requests", new org.json.JSONArray(plan.getExecutionRequests())));
        return result;
    }

    public synchronized void restoreState(org.json.JSONArray saved) throws org.json.JSONException {
        Map<String, FundAllocationPlan> staged = new LinkedHashMap<>();
        for (int i = 0; i < saved.length(); i++) {
            org.json.JSONObject item = saved.getJSONObject(i);
            org.json.JSONObject weights = item.getJSONObject("weights");
            Map<String, Double> targets = new LinkedHashMap<>();
            Iterator<String> keys = weights.keys();
            while (keys.hasNext()) { String key = keys.next(); targets.put(key, weights.getDouble(key)); }
            FundAllocationPlan plan = new FundAllocationPlan(item.getString("id"), item.getString("accountId"),
                    item.getString("name"), targets, item.getDouble("cashWeight"));
            if (staged.containsKey(plan.getId())) throw new IllegalArgumentException("Rencana save duplikat");
            plan.setStatus(FundAllocationPlan.PlanStatus.valueOf(item.getString("status")));
            List<String> requests = new ArrayList<>();
            org.json.JSONArray ids = item.getJSONArray("requests");
            for (int j = 0; j < ids.length(); j++) {
                String request = ids.getString(j);
                if (request.trim().isEmpty() || requests.contains(request)) throw new IllegalArgumentException("ID order rencana tidak valid");
                requests.add(request);
            }
            plan.setExecutionRequests(requests);
            staged.put(plan.getId(), plan);
        }
        savedPlans.clear(); savedPlans.putAll(staged);
    }

    public synchronized List<FundAllocationPlan> getAllPlans() { return new ArrayList<>(savedPlans.values()); }

    public FundAllocationPlan getPlan(String planId) {
        return savedPlans.get(planId);
    }

    /**
     * Menghitung daftar order rebalancing yang diperlukan untuk menyelaraskan
     * posisi saat ini dengan target bobot dalam rencana.
     */
    public List<Order> calculateRebalanceOrders(
            FundAllocationPlan plan,
            Account account,
            InstrumentCatalog catalog,
            BigDecimal fxRate) {

        List<Order> orders = new ArrayList<>();
        Currency cur = Currency.IDR;
        Money totalAum = account.getTotalPortfolioValue(cur, fxRate);

        for (Map.Entry<String, Double> entry : plan.getTargetWeights().entrySet()) {
            String instId = entry.getKey();
            double targetWeight = entry.getValue();

            Instrument inst = catalog.getById(instId);
            if (inst == null || !inst.isActiveTrading())
                throw new IllegalArgumentException("Jalur transaksi instrumen rencana belum tersedia: " + instId);

            BigDecimal targetVal = totalAum.getAmount().multiply(BigDecimal.valueOf(targetWeight));
            if (inst.getCurrency() == Currency.USD) {
                if (fxRate == null || fxRate.signum() <= 0) throw new IllegalArgumentException("Kurs valuasi harus positif");
                targetVal = targetVal.divide(fxRate, 8, java.math.RoundingMode.HALF_UP);
            }
            Position pos = account.getPosition(inst.getId());
            BigDecimal curVal = (pos != null) ? pos.getMarketValue().getAmount() : BigDecimal.ZERO;

            BigDecimal diffVal = targetVal.subtract(curVal);
            BigDecimal price = inst.getCurrentPrice().getAmount();

            if (diffVal.compareTo(BigDecimal.ZERO) > 0) {
                // Perlu Beli
                long sharesNeeded = diffVal.divide(price, 0, java.math.RoundingMode.FLOOR).longValue();
                long lotRounded = (sharesNeeded / inst.getLotSize()) * inst.getLotSize();
                if (lotRounded > 0) {
                    orders.add(new Order(
                            "REBAL-B-" + inst.getTicker() + "-" + System.nanoTime(),
                            "REQ-REBAL-" + System.nanoTime(),
                            account.getId(), inst.getId(),
                            OrderSide.BUY, OrderType.LIMIT,
                            inst.getCurrentPrice(), null, lotRounded, System.currentTimeMillis(), null
                    ));
                }
            } else if (diffVal.compareTo(BigDecimal.ZERO) < 0) {
                // Perlu Jual
                long sharesToSell = diffVal.abs().divide(price, 0, java.math.RoundingMode.FLOOR).longValue();
                long lotRounded = (sharesToSell / inst.getLotSize()) * inst.getLotSize();
                if (pos != null && lotRounded > 0) {
                    long finalSell = Math.min(lotRounded, pos.getQuantity());
                    if (finalSell > 0) {
                        orders.add(new Order(
                                "REBAL-S-" + inst.getTicker() + "-" + System.nanoTime(),
                                "REQ-REBAL-" + System.nanoTime(),
                                account.getId(), inst.getId(),
                                OrderSide.SELL, OrderType.LIMIT,
                                inst.getCurrentPrice(), null, finalSell, System.currentTimeMillis(), null
                        ));
                    }
                }
            }
        }
        return orders;
    }
}
