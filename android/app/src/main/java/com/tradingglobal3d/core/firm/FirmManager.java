package com.tradingglobal3d.core.firm;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Manajer Firma Pengelolaan Dana (Fund Management Firm).
 * Mengelola penawaran mandat klien, validasi hard limit mandat,
 * kalkulasi Management Fee & Performance Fee dengan High-Water Mark (HWM),
 * serta integritas arus modal (setoran/penarikan).
 */
public final class FirmManager {
    private final Map<String, ClientMandate> mandates = new LinkedHashMap<>();

    public FirmManager() {
        initializeOfferedMandates();
    }

    private void initializeOfferedMandates() {
        mandates.put("MANDATE-01", new ClientMandate(
                "MANDATE-01", "Yayasan Dana Sejahtera",
                ClientRiskProfile.CONSERVATIVE, "IDX:COMPOSITE",
                Money.of(100_000_000, Currency.IDR),
                new BigDecimal("0.015"), new BigDecimal("0.20")
        ));
        mandates.put("MANDATE-02", new ClientMandate(
                "MANDATE-02", "Keluarga Cendana Growth",
                ClientRiskProfile.MODERATE, "IDX:LQ45",
                Money.of(250_000_000, Currency.IDR),
                new BigDecimal("0.015"), new BigDecimal("0.20")
        ));
        mandates.put("MANDATE-03", new ClientMandate(
                "MANDATE-03", "Apex Capital Global",
                ClientRiskProfile.AGGRESSIVE, "US:SP500_GAME",
                Money.of(50_000.0, Currency.USD),
                new BigDecimal("0.020"), new BigDecimal("0.20")
        ));
    }

    public org.json.JSONArray exportState() throws org.json.JSONException {
        org.json.JSONArray result = new org.json.JSONArray();
        for (ClientMandate m : mandates.values()) result.put(new org.json.JSONObject().put("id", m.getId())
                .put("accountId", m.getClientAccountId() == null ? org.json.JSONObject.NULL : m.getClientAccountId())
                .put("status", m.getStatus().name()).put("trust", m.getTrustScore())
                .put("hwm", m.getHighWaterMark().getAmount().toPlainString()));
        return result;
    }

    public void restoreState(org.json.JSONArray saved) throws org.json.JSONException {
        mandates.clear(); initializeOfferedMandates();
        for (int i = 0; i < saved.length(); i++) {
            org.json.JSONObject p = saved.getJSONObject(i);
            ClientMandate m = mandates.get(p.getString("id"));
            if (m == null) throw new IllegalArgumentException("Mandat save tidak dikenal");
            if (!p.isNull("accountId")) m.setClientAccountId(p.getString("accountId"));
            m.adjustTrust(p.getInt("trust") - m.getTrustScore());
            m.updateHwmAfterPerformanceFee(new Money(new BigDecimal(p.getString("hwm")), m.getInitialDeposit().getCurrency()));
            m.setStatus(ClientMandate.MandateStatus.valueOf(p.getString("status")));
        }
    }

    public List<ClientMandate> getAllMandates() {
        return new ArrayList<>(mandates.values());
    }

    public List<ClientMandate> getActiveMandates() {
        List<ClientMandate> list = new ArrayList<>();
        for (ClientMandate m : mandates.values()) {
            if (m.getStatus() == ClientMandate.MandateStatus.ACTIVE) {
                list.add(m);
            }
        }
        return list;
    }

    public ClientMandate getMandate(String id) {
        return mandates.get(id);
    }

    public static final class ValidationResult {
        public final boolean isValid;
        public final String reason;

        public ValidationResult(boolean isValid, String reason) {
            this.isValid = isValid;
            this.reason = reason;
        }

        public static ValidationResult pass() {
            return new ValidationResult(true, "Lolos validasi mandat");
        }

        public static ValidationResult fail(String reason) {
            return new ValidationResult(false, reason);
        }
    }

    /**
     * Memvalidasi hard limit mandat sebelum order dieksekusi.
     * Memeriksa batas konsentrasi per saham dan cadangan kas minimum.
     */
    public ValidationResult validateOrder(
            ClientMandate mandate,
            Account clientAccount,
            Order order,
            Instrument instrument,
            BigDecimal fxRate) {

        if (order.getSide() == OrderSide.SELL) {
            return ValidationResult.pass(); // Penjualan meningkatkan kas dan mengurangi konsentrasi
        }

        Currency baseCur = mandate.getInitialDeposit().getCurrency();
        Money totalAum = clientAccount.getTotalPortfolioValue(baseCur, fxRate);

        BigDecimal estPrice = order.getLimitPrice() != null ? order.getLimitPrice().getAmount() : instrument.getCurrentPrice().getAmount();
        BigDecimal orderNominal = estPrice.multiply(BigDecimal.valueOf(order.getTotalQuantity()));
        Money orderMoney = new Money(orderNominal, instrument.getCurrency());

        // 1. Validasi Batas Cadangan Kas Minimum
        Money availableAfter = clientAccount.getAvailableCash(baseCur).subtract(orderMoney);
        BigDecimal minCashThreshold = totalAum.getAmount().multiply(BigDecimal.valueOf(mandate.getRiskProfile().getMinCashReserve()));

        if (availableAfter.getAmount().compareTo(minCashThreshold) < 0) {
            mandate.adjustTrust(-5); // Pelanggaran batas kas
            return ValidationResult.fail("Order melanggar batas cadangan kas minimum mandat (" +
                    (mandate.getRiskProfile().getMinCashReserve() * 100) + "% dari AUM)");
        }

        // 2. Validasi Batas Konsentrasi Saham
        Position currentPos = clientAccount.getPosition(instrument.getId());
        long currentQty = currentPos != null ? currentPos.getQuantity() : 0;
        long totalQtyAfter = currentQty + order.getTotalQuantity();
        BigDecimal postValue = estPrice.multiply(BigDecimal.valueOf(totalQtyAfter));
        BigDecimal maxConcentrationAllowed = totalAum.getAmount().multiply(BigDecimal.valueOf(mandate.getRiskProfile().getMaxConcentrationPerStock()));

        if (postValue.compareTo(maxConcentrationAllowed) > 0) {
            mandate.adjustTrust(-5); // Pelanggaran konsentrasi
            return ValidationResult.fail("Order melanggar batas konsentrasi per saham mandat (" +
                    (mandate.getRiskProfile().getMaxConcentrationPerStock() * 100) + "% dari AUM)");
        }

        return ValidationResult.pass();
    }

    public static final class FeeResult {
        public final Money managementFee;
        public final Money performanceFee;
        public final Money totalFees;

        public FeeResult(Money managementFee, Money performanceFee) {
            this.managementFee = managementFee;
            this.performanceFee = performanceFee;
            this.totalFees = managementFee.add(performanceFee);
        }
    }

    /**
     * Menghitung dan memungut Management Fee harian serta Performance Fee berbasis High-Water Mark.
     */
    public synchronized FeeResult collectFees(
            ClientMandate mandate,
            Account clientAccount,
            Account managerPersonalAccount,
            EconomyLedger ledger,
            BigDecimal fxRate,
            long gameTimestamp) {

        Currency cur = mandate.getInitialDeposit().getCurrency();
        Money aum = clientAccount.getTotalPortfolioValue(cur, fxRate);

        // 1. Management Fee Harian (prorata 365 hari)
        BigDecimal dailyMgmtRate = mandate.getManagementFeeAnnualRate().divide(BigDecimal.valueOf(365), 8, RoundingMode.HALF_UP);
        BigDecimal mgmtFeeAmount = aum.getAmount().multiply(dailyMgmtRate).setScale(cur.getDecimalPlaces(), RoundingMode.HALF_UP);
        String period = java.time.Instant.ofEpochMilli(gameTimestamp)
                .atZone(java.time.ZoneId.of("Asia/Jakarta")).toLocalDate().toString();
        String managementKey = "FEE-MGMT-" + mandate.getId() + "-" + period;
        Money mgmtFee = ledger.isRequestProcessed(managementKey + "-CLIENT")
                ? Money.zero(cur) : new Money(mgmtFeeAmount, cur);

        // 2. Performance Fee (HANYA jika AUM melebihi High-Water Mark)
        Money hwm = mandate.getHighWaterMark();
        Money perfFee = Money.zero(cur);

        if (aum.compareTo(hwm) > 0) {
            Money excessProfit = aum.subtract(hwm);
            BigDecimal perfFeeAmt = excessProfit.getAmount().multiply(mandate.getPerformanceFeeRate()).setScale(cur.getDecimalPlaces(), RoundingMode.HALF_UP);
            perfFee = new Money(perfFeeAmt, cur);
        }

        // 3. Mutasi atomik pemungutan fee
        Money totalFee = mgmtFee.add(perfFee);
        if (clientAccount.getAvailableCash(cur).isLessThan(totalFee)) {
            return new FeeResult(Money.zero(cur), Money.zero(cur));
        }
        if (totalFee.isPositive()) {
            if (mgmtFee.isPositive()) {
                ledger.recordMutation(managementKey + "-CLIENT", clientAccount,
                        LedgerEntryType.MANAGEMENT_FEE, mgmtFee, false, gameTimestamp,
                        "Management fee " + mandate.getClientName() + " periode " + period);
                ledger.recordMutation(managementKey + "-MANAGER", managerPersonalAccount,
                        LedgerEntryType.MANAGEMENT_FEE, mgmtFee, true, gameTimestamp,
                        "Pendapatan management fee " + mandate.getClientName() + " periode " + period);
            }
            if (perfFee.isPositive()) {
                String performanceKey = "FEE-PERF-" + mandate.getId() + "-" + period + "-" + hwm.getAmount();
                ledger.recordMutation(performanceKey + "-CLIENT", clientAccount,
                        LedgerEntryType.MANAGEMENT_FEE, perfFee, false, gameTimestamp,
                        "Performance fee " + mandate.getClientName());
                ledger.recordMutation(performanceKey + "-MANAGER", managerPersonalAccount,
                        LedgerEntryType.MANAGEMENT_FEE, perfFee, true, gameTimestamp,
                        "Pendapatan performance fee " + mandate.getClientName());
            }

            // Perbarui High-Water Mark jika ada laba baru
            if (perfFee.isPositive()) {
                mandate.updateHwmAfterPerformanceFee(aum.subtract(perfFee));
                mandate.adjustTrust(5); // Klien puas karena ada profit
            }
        }

        return new FeeResult(mgmtFee, perfFee);
    }

    /**
     * Setoran modal baru dari klien (Capital Inflow).
     * Setoran menaikkan saldo dan menyesuaikan HWM agar tidak dihitung sebagai laba investasi!
     */
    public synchronized void processClientDeposit(
            ClientMandate mandate,
            Account clientAccount,
            Money depositAmount,
            EconomyLedger ledger,
            long gameTimestamp) {

        ledger.recordMutation(
                "MANDATE-TOPUP-" + System.nanoTime(),
                clientAccount,
                LedgerEntryType.DEPOSIT,
                depositAmount,
                true,
                gameTimestamp,
                "Setoran modal tambahan klien " + mandate.getClientName()
        );
        // Sesuaikan HWM agar setoran modal tidak dianggap profit investasi (FR-08.11)
        mandate.increaseHwmForDeposit(depositAmount);
        mandate.adjustTrust(5);
    }

    /**
     * Penarikan modal oleh klien (Capital Outflow).
     * Ditolak jika kas tersedia tidak mencukupi (harus melikuidasi saham terlebih dahulu).
     */
    public synchronized boolean processClientWithdrawal(
            ClientMandate mandate,
            Account clientAccount,
            Money withdrawAmount,
            EconomyLedger ledger,
            long gameTimestamp) {

        Money available = clientAccount.getAvailableCash(withdrawAmount.getCurrency());
        if (available.isLessThan(withdrawAmount)) {
            mandate.adjustTrust(-10); // Klien kecewa likuiditas kas tidak cukup
            return false;
        }

        ledger.recordMutation(
                "MANDATE-WITHDRAW-" + System.nanoTime(),
                clientAccount,
                LedgerEntryType.WITHDRAWAL,
                withdrawAmount,
                false,
                gameTimestamp,
                "Penarikan modal oleh klien " + mandate.getClientName()
        );
        mandate.decreaseHwmForWithdrawal(withdrawAmount);
        return true;
    }
}
