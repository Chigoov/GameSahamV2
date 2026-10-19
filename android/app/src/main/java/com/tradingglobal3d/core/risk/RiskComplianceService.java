package com.tradingglobal3d.core.risk;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Layanan Manajemen Risiko Margin, Pinjaman Short Selling, dan Audit Kepatuhan.
 */
public final class RiskComplianceService {
    public static final BigDecimal MAINTENANCE_MARGIN_RATIO = new BigDecimal("0.30"); // 30%
    public static final BigDecimal LIQUIDATION_MARGIN_RATIO = new BigDecimal("0.20");  // 20%

    public static final class MarginHealthReport {
        private final String accountId;
        private final Money equity;
        private final Money marginDebt;
        private final Money totalExposure;
        private final BigDecimal marginRatio;
        private final MarginCallStatus status;

        public MarginHealthReport(String accountId, Money equity, Money marginDebt,
                                  Money totalExposure, BigDecimal marginRatio, MarginCallStatus status) {
            this.accountId = accountId;
            this.equity = equity;
            this.marginDebt = marginDebt;
            this.totalExposure = totalExposure;
            this.marginRatio = marginRatio;
            this.status = status;
        }

        public String getAccountId() { return accountId; }
        public Money getEquity() { return equity; }
        public Money getMarginDebt() { return marginDebt; }
        public Money getTotalExposure() { return totalExposure; }
        public BigDecimal getMarginRatio() { return marginRatio; }
        public MarginCallStatus getStatus() { return status; }
    }

    private final Map<String, Map<Currency, Money>> marginDebts = new HashMap<>();

    private void setMarginDebt(String accountId, Money debt) {
        marginDebts.computeIfAbsent(accountId, key -> new EnumMap<>(Currency.class)).put(debt.getCurrency(), debt);
    }
    private final Map<String, Map<String, ShortPosition>> shortPositions = new HashMap<>();
    private final List<ComplianceAuditLog> auditLogs = new ArrayList<>();

    public synchronized org.json.JSONObject exportState() throws org.json.JSONException {
        org.json.JSONObject state = new org.json.JSONObject();
        org.json.JSONArray debts = new org.json.JSONArray();
        for (Map.Entry<String, Map<Currency, Money>> entry : marginDebts.entrySet()) {
            for (Money debt : entry.getValue().values()) {
                debts.put(new org.json.JSONObject().put("accountId", entry.getKey())
                        .put("currency", debt.getCurrency().name())
                        .put("amount", debt.getAmount().toPlainString()));
            }
        }
        state.put("debts", debts);
        org.json.JSONArray shorts = new org.json.JSONArray();
        for (Map.Entry<String, Map<String, ShortPosition>> entry : shortPositions.entrySet()) {
            for (ShortPosition p : entry.getValue().values()) {
                shorts.put(new org.json.JSONObject().put("accountId", entry.getKey()).put("ticker", p.getTicker())
                        .put("exchange", p.getExchange()).put("currency", p.getCurrency().name())
                        .put("shares", p.getBorrowedShares()).put("price", p.getAverageShortPrice().toPlainString())
                        .put("collateral", p.getCollateralLocked().getAmount().toPlainString())
                        .put("rate", p.getAnnualBorrowFeeRate().toPlainString()));
            }
        }
        state.put("shorts", shorts);
        return state;
    }

    public synchronized void restoreState(org.json.JSONObject state) throws org.json.JSONException {
        marginDebts.clear(); shortPositions.clear(); auditLogs.clear();
        org.json.JSONArray debts = state.optJSONArray("debts");
        if (debts != null) for (int i = 0; i < debts.length(); i++) {
            org.json.JSONObject d = debts.getJSONObject(i);
            Money debt = new Money(new BigDecimal(d.getString("amount")), Currency.valueOf(d.getString("currency")));
            if (debt.isNegative()) throw new IllegalArgumentException("Utang save tidak boleh negatif");
            Map<Currency, Money> currencies = marginDebts.get(d.getString("accountId"));
            if (currencies != null && currencies.containsKey(debt.getCurrency()))
                throw new IllegalArgumentException("Utang save duplikat");
            setMarginDebt(d.getString("accountId"), debt);
        }
        org.json.JSONArray shorts = state.optJSONArray("shorts");
        if (shorts != null) for (int i = 0; i < shorts.length(); i++) {
            org.json.JSONObject p = shorts.getJSONObject(i);
            Currency currency = Currency.valueOf(p.getString("currency"));
            ShortPosition position = new ShortPosition(p.getString("ticker"), p.getString("exchange"), currency,
                    p.getLong("shares"), new BigDecimal(p.getString("price")),
                    new Money(new BigDecimal(p.getString("collateral")), currency), new BigDecimal(p.getString("rate")));
            shortPositions.computeIfAbsent(p.getString("accountId"), k -> new HashMap<>()).put(position.getTicker(), position);
        }
    }

    public List<ComplianceAuditLog> getAuditLogs() {
        return Collections.unmodifiableList(auditLogs);
    }

    public synchronized Money getMarginDebt(String accountId, Currency currency) {
        Map<Currency, Money> debts = marginDebts.get(accountId);
        return debts == null ? Money.zero(currency) : debts.getOrDefault(currency, Money.zero(currency));
    }

    public synchronized ShortPosition getShortPosition(String accountId, String ticker) {
        Map<String, ShortPosition> userShorts = shortPositions.get(accountId);
        if (userShorts == null) return null;
        return userShorts.get(ticker);
    }

    /**
     * Meminjam dana fasilitas margin.
     */
    public synchronized void takeMarginLoan(Account account, Money loanAmount, EconomyLedger ledger, long timestamp) {
        if (loanAmount.isNegative() || loanAmount.isZero()) {
            throw new IllegalArgumentException("Nominal pinjaman harus positif");
        }
        Money currentDebt = getMarginDebt(account.getId(), loanAmount.getCurrency());
        // Kreditkan kas pinjaman ke akun
        ledger.recordMutation(
                "MARGIN-LOAN-" + System.nanoTime(),
                account,
                LedgerEntryType.DEPOSIT,
                loanAmount,
                true,
                timestamp,
                "Pencairan fasilitas pinjaman margin " + loanAmount
        );
        setMarginDebt(account.getId(), currentDebt.add(loanAmount));


        auditLogs.add(new ComplianceAuditLog(
                "LOG-" + System.nanoTime(), timestamp, account.getId(),
                ComplianceAuditLog.AuditCategory.MARGIN_LOAN,
                "Penambahan pinjaman margin " + loanAmount,
                "Pencairan kas ke akun"
        ));
    }

    /**
     * Melunasi utang fasilitas margin.
     */
    public synchronized void repayMarginLoan(Account account, Money repayAmount, EconomyLedger ledger, long timestamp) {
        if (!repayAmount.isPositive()) throw new IllegalArgumentException("Nominal pelunasan harus positif");
        Money currentDebt = getMarginDebt(account.getId(), repayAmount.getCurrency());

        if (currentDebt.isZero()) return;

        Money actualRepay = currentDebt.isLessThan(repayAmount) ? currentDebt : repayAmount;
        if (account.getAvailableCash(repayAmount.getCurrency()).isLessThan(actualRepay)) {
            throw new IllegalStateException("Kas tersedia tidak mencukupi untuk melunasi utang margin");
        }

        ledger.recordMutation(
                "MARGIN-REPAY-" + System.nanoTime(),
                account,
                LedgerEntryType.WITHDRAWAL,
                actualRepay,
                false,
                timestamp,
                "Pelunasan utang margin " + actualRepay
        );
        setMarginDebt(account.getId(), currentDebt.subtract(actualRepay));

        auditLogs.add(new ComplianceAuditLog(
                "LOG-" + System.nanoTime(), timestamp, account.getId(),
                ComplianceAuditLog.AuditCategory.MARGIN_LOAN,
                "Pelunasan utang margin " + actualRepay,
                "Debet kas dari akun"
        ));
    }

    /**
     * Membuka posisi Short Selling dengan meminjam saham.
     * Hasil penjualan short ditahan sebagai jaminan (Collateral Locked) (FR-15.05).
     */
    public synchronized ShortPosition openShortPosition(
            Account account,
            Instrument instrument,
            long sharesToShort,
            EconomyLedger ledger,
            long timestamp) {

        if (sharesToShort <= 0) throw new IllegalArgumentException("Jumlah lembar short harus positif");
        if (getShortPosition(account.getId(), instrument.getTicker()) != null) {
            throw new IllegalStateException("Posisi short masih aktif; tutup kewajiban sebelum membuka pinjaman baru");
        }

        BigDecimal currentPrice = instrument.getCurrentPrice().getAmount();
        BigDecimal grossProceeds = currentPrice.multiply(BigDecimal.valueOf(sharesToShort));
        BigDecimal fee = grossProceeds.multiply(new BigDecimal("0.0025")); // 0.25% fee jual
        BigDecimal netProceeds = grossProceeds.subtract(fee);

        Money proceedsMoney = new Money(netProceeds, instrument.getCurrency());
        Money feeMoney = new Money(fee, instrument.getCurrency());

        // 1. Catat kas masuk hasil penjualan short via ledger
        ledger.recordMutation(
                "SHORT-PROCEEDS-" + System.nanoTime(),
                account,
                LedgerEntryType.TRADE_SELL,
                new Money(grossProceeds, instrument.getCurrency()),
                true,
                timestamp,
                "Hasil penjualan short " + instrument.getTicker() + " " + sharesToShort + " lembar"
        );

        // 2. Potong fee transaksi
        ledger.recordMutation(
                "SHORT-FEE-" + System.nanoTime(),
                account,
                LedgerEntryType.TRANSACTION_FEE,
                feeMoney,
                false,
                timestamp,
                "Fee bursa penjualan short " + instrument.getTicker()
        );

        // 3. TAHAN SELURUH HASIL PENJUALAN SEBAGAI JAMINAN (Collateral Locked) - FR-15.05
        account.reserveCash(proceedsMoney);

        ShortPosition shortPos = new ShortPosition(
                instrument.getTicker(), instrument.getExchange().name(), instrument.getCurrency(),
                sharesToShort, currentPrice, proceedsMoney, new BigDecimal("0.025")
        );

        shortPositions.computeIfAbsent(account.getId(), k -> new HashMap<>()).put(instrument.getTicker(), shortPos);

        auditLogs.add(new ComplianceAuditLog(
                "LOG-" + System.nanoTime(), timestamp, account.getId(),
                ComplianceAuditLog.AuditCategory.SHORT_BORROW,
                "Pembukaan posisi short " + instrument.getTicker() + " " + sharesToShort + " lembar @" + currentPrice,
                "Hasil penjualan Rp " + netProceeds + " dikunci sebagai jaminan kas tertahan"
        ));

        return shortPos;
    }

    /**
     * Menutup posisi short (Buy to Cover).
     */
    public synchronized void closeShortPosition(
            Account account,
            Instrument instrument,
            long sharesToCover,
            EconomyLedger ledger,
            long timestamp) {

        Map<String, ShortPosition> userShorts = shortPositions.get(account.getId());
        if (userShorts == null || !userShorts.containsKey(instrument.getTicker())) {
            throw new IllegalStateException("Tidak ada posisi short untuk: " + instrument.getTicker());
        }

        ShortPosition shortPos = userShorts.get(instrument.getTicker());
        if (sharesToCover <= 0 || sharesToCover > shortPos.getBorrowedShares()) {
            throw new IllegalArgumentException("Jumlah cover melebihi saham yang dipinjam");
        }

        BigDecimal coverPrice = instrument.getCurrentPrice().getAmount();
        BigDecimal grossCost = coverPrice.multiply(BigDecimal.valueOf(sharesToCover));
        BigDecimal fee = grossCost.multiply(new BigDecimal("0.0015")); // 0.15% fee beli
        Money totalCostMoney = new Money(grossCost, instrument.getCurrency())
                .add(new Money(fee, instrument.getCurrency()));

        // Lepas jaminan kas tertahan proporsional
        double coverRatio = (double) sharesToCover / shortPos.getBorrowedShares();
        BigDecimal releasedAmt = shortPos.getCollateralLocked().getAmount().multiply(BigDecimal.valueOf(coverRatio));
        Money releasedMoney = new Money(releasedAmt, instrument.getCurrency());
        if (account.getAvailableCash(instrument.getCurrency()).add(releasedMoney).isLessThan(totalCostMoney)) {
            throw new IllegalStateException("Kas dan jaminan tidak mencukupi untuk menutup short; kewajiban tetap tercatat");
        }
        account.releaseReservedCash(releasedMoney);

        // Bayar pembelian kembali saham ke pasar
        ledger.recordMutation(
                "SHORT-COVER-BUY-" + System.nanoTime(),
                account,
                LedgerEntryType.TRADE_BUY,
                new Money(grossCost, instrument.getCurrency()),
                false,
                timestamp,
                "Buy to cover saham pinjaman " + instrument.getTicker() + " " + sharesToCover + " lembar"
        );
        ledger.recordMutation(
                "SHORT-COVER-FEE-" + System.nanoTime(),
                account,
                LedgerEntryType.TRANSACTION_FEE,
                new Money(fee, instrument.getCurrency()),
                false,
                timestamp,
                "Fee pembelian penutupan short " + instrument.getTicker()
        );

        shortPos.coverShares(sharesToCover, coverPrice);
        if (shortPos.getBorrowedShares() == 0) {
            userShorts.remove(instrument.getTicker());
        }

        auditLogs.add(new ComplianceAuditLog(
                "LOG-" + System.nanoTime(), timestamp, account.getId(),
                ComplianceAuditLog.AuditCategory.SHORT_BORROW,
                "Penutupan posisi short " + instrument.getTicker() + " " + sharesToCover + " lembar",
                "Jaminan dilepaskan dan saham pinjaman dikembalikan ke broker"
        ));
    }

    /**
     * Evaluasi kesehatan rasio margin dan penerbitan Margin Call.
     */
    public synchronized MarginHealthReport evaluateMarginHealth(
            Account account,
            InstrumentCatalog catalog,
            BigDecimal usdRate) {

        Currency baseCur = Currency.IDR;
        if (usdRate == null || usdRate.signum() <= 0) throw new IllegalArgumentException("Kurs valuasi harus positif");
        BigDecimal totalCashIdr = account.getTotalCash(baseCur).getAmount()
                .add(account.getTotalCash(Currency.USD).getAmount().multiply(usdRate));
        Money marginDebt = getMarginDebt(account.getId(), baseCur).add(new Money(
                getMarginDebt(account.getId(), Currency.USD).getAmount().multiply(usdRate), baseCur));


        // 1. Hitung nilai pasar posisi Long
        BigDecimal longValIdr = BigDecimal.ZERO;
        for (Position p : account.getPositions().values()) {
            if (p.getQuantity() > 0) {
                BigDecimal val = p.getCurrentPrice().multiply(BigDecimal.valueOf(p.getQuantity()));
                if (p.getCurrency() == Currency.USD) val = val.multiply(usdRate);
                longValIdr = longValIdr.add(val);
            }
        }

        // 2. Hitung kewajiban posisi Short
        BigDecimal shortLiabIdr = BigDecimal.ZERO;
        Map<String, ShortPosition> userShorts = shortPositions.get(account.getId());
        if (userShorts != null) {
            for (ShortPosition sp : userShorts.values()) {
                Instrument inst = catalog.getById("IDX:" + sp.getTicker());
                if (inst == null) inst = catalog.getById("US:" + sp.getTicker());
                BigDecimal curPx = (inst != null) ? inst.getCurrentPrice().getAmount() : sp.getAverageShortPrice();
                BigDecimal liab = curPx.multiply(BigDecimal.valueOf(sp.getBorrowedShares()));
                if (sp.getCurrency() == Currency.USD) liab = liab.multiply(usdRate);
                shortLiabIdr = shortLiabIdr.add(liab);
            }
        }

        // Equity = Total Cash + Long - Short Liability - Margin Debt
        BigDecimal equityAmt = totalCashIdr.add(longValIdr).subtract(shortLiabIdr).subtract(marginDebt.getAmount());
        Money equity = new Money(equityAmt, baseCur);

        // Exposure = Long + Short Liability
        BigDecimal exposureAmt = longValIdr.add(shortLiabIdr);
        Money totalExposure = new Money(exposureAmt, baseCur);

        BigDecimal marginRatio;
        if (exposureAmt.compareTo(BigDecimal.ZERO) == 0) {
            marginRatio = BigDecimal.ONE;
        } else {
            marginRatio = equityAmt.divide(exposureAmt, 4, RoundingMode.HALF_UP);
        }

        MarginCallStatus status = MarginCallStatus.HEALTHY;
        if (marginRatio.compareTo(LIQUIDATION_MARGIN_RATIO) < 0) {
            status = MarginCallStatus.LIQUIDATION_REQUIRED;
        } else if (marginRatio.compareTo(MAINTENANCE_MARGIN_RATIO) < 0) {
            status = MarginCallStatus.MARGIN_CALL;
        }

        return new MarginHealthReport(account.getId(), equity, marginDebt, totalExposure, marginRatio, status);
    }

    /**
     * Eksekusi Likuidasi Paksa dengan kepatuhan penuh terhadap status suspensi/halt bursa (FR-15.07, FR-16.07).
     */
    public synchronized boolean executeForcedLiquidation(
            Account account,
            Instrument instrument,
            boolean isMarketHalted,
            EconomyLedger ledger,
            long timestamp) {

        // Proteksi Hard: Likuidasi TIDAK BISA menembus status halt bursa (FR-16.07)
        if (isMarketHalted) {
            auditLogs.add(new ComplianceAuditLog(
                    "LOG-HALT-" + System.nanoTime(), timestamp, account.getId(),
                    ComplianceAuditLog.AuditCategory.HALT_ENFORCEMENT,
                    "Permintaan likuidasi paksa untuk " + instrument.getTicker() + " tertahan: Bursa sedang disuspensi (HALT)",
                    "Posisi ditangguhkan dalam status HALTED_PENDING_LIQUIDATION hingga pasar dibuka kembali"
            ));
            return false;
        }

        // Likuidasi posisi long untuk menutup kewajiban
        Position p = account.getPosition(instrument.getId());
        if (p != null && p.getQuantity() > 0) {
            long sellShares = p.getQuantity();
            BigDecimal price = instrument.getCurrentPrice().getAmount();
            BigDecimal gross = price.multiply(BigDecimal.valueOf(sellShares));
            BigDecimal fee = gross.multiply(new BigDecimal("0.0025"));
            BigDecimal net = gross.subtract(fee);

            ledger.recordMutation(
                    "FORCED-LIQ-" + System.nanoTime(),
                    account,
                    LedgerEntryType.TRADE_SELL,
                    new Money(net, instrument.getCurrency()),
                    true,
                    timestamp,
                    "Likuidasi paksa posisi long " + instrument.getTicker() + " " + sellShares + " lembar"
            );

            p.addSell(sellShares, price);

            auditLogs.add(new ComplianceAuditLog(
                    "LOG-LIQ-" + System.nanoTime(), timestamp, account.getId(),
                    ComplianceAuditLog.AuditCategory.FORCED_LIQUIDATION,
                    "Eksekusi likuidasi paksa berhasil pada " + instrument.getTicker(),
                    "Kas hasil likuidasi bersih " + net + " dikreditkan ke saldo akun"
            ));
            return true;
        }

        return false;
    }
}
