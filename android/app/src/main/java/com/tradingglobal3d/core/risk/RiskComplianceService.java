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
    public static final long MARGIN_CALL_GRACE_REAL_MS = 120L * 60_000L;

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
    private final Map<String, Long> marginCallDeadlines = new HashMap<>();
    private final Map<String, Long> financingDays = new HashMap<>();
    private long observedMarginRealTime;

    private void setMarginDebt(String accountId, Money debt) {
        marginDebts.computeIfAbsent(accountId, key -> new EnumMap<>(Currency.class)).put(debt.getCurrency(), debt);
    }
    private final Map<String, Map<String, ShortPosition>> shortPositions = new HashMap<>();
    private final List<ComplianceAuditLog> auditLogs = new ArrayList<>();

    public synchronized void reset() {
        marginDebts.clear();
        marginCallDeadlines.clear(); observedMarginRealTime = 0;
        financingDays.clear();
        shortPositions.clear();
        auditLogs.clear();
    }

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
        org.json.JSONArray calls = new org.json.JSONArray();
        for (Map.Entry<String, Long> call : marginCallDeadlines.entrySet())
            calls.put(new org.json.JSONObject().put("accountId", call.getKey()).put("deadlineRealAt", call.getValue()));
        state.put("marginCalls", calls).put("observedMarginRealTime", observedMarginRealTime);
        org.json.JSONArray chargedDays = new org.json.JSONArray();
        for (Map.Entry<String, Long> entry : financingDays.entrySet()) chargedDays.put(new org.json.JSONObject()
                .put("accountId", entry.getKey()).put("day", entry.getValue()));
        state.put("financingDays", chargedDays);
        org.json.JSONArray shorts = new org.json.JSONArray();
        for (Map.Entry<String, Map<String, ShortPosition>> entry : shortPositions.entrySet()) {
            for (ShortPosition p : entry.getValue().values()) {
                shorts.put(new org.json.JSONObject().put("accountId", entry.getKey()).put("ticker", p.getTicker())
                        .put("exchange", p.getExchange()).put("currency", p.getCurrency().name())
                        .put("shares", p.getBorrowedShares()).put("price", p.getAverageShortPrice().toPlainString())
                        .put("collateral", p.getCollateralLocked().getAmount().toPlainString())
                        .put("rate", p.getAnnualBorrowFeeRate().toPlainString())
                        .put("unpaidFee", p.getAccruedUnpaidFee().getAmount().toPlainString()));
            }
        }
        state.put("shorts", shorts);
        return state;
    }

    public synchronized void restoreState(org.json.JSONObject state) throws org.json.JSONException {
        marginDebts.clear(); shortPositions.clear(); auditLogs.clear();
        marginCallDeadlines.clear();
        financingDays.clear();
        org.json.JSONArray chargedDays = state.optJSONArray("financingDays");
        if (chargedDays != null) for (int i = 0; i < chargedDays.length(); i++) {
            org.json.JSONObject charged = chargedDays.getJSONObject(i);
            String accountId = charged.getString("accountId");
            long day = charged.getLong("day");
            java.time.LocalDate.ofEpochDay(day);
            if (accountId.trim().isEmpty() || financingDays.putIfAbsent(accountId, day) != null)
                throw new IllegalArgumentException("Tanggal pembiayaan save duplikat atau tidak valid");
        }
        observedMarginRealTime = state.optLong("observedMarginRealTime", 0);
        if (observedMarginRealTime < 0) throw new IllegalArgumentException("Waktu margin save tidak valid");
        org.json.JSONArray calls = state.optJSONArray("marginCalls");
        if (calls != null) for (int i = 0; i < calls.length(); i++) {
            org.json.JSONObject call = calls.getJSONObject(i);
            String accountId = call.getString("accountId");
            long deadline = call.getLong("deadlineRealAt");
            if (accountId.trim().isEmpty() || deadline <= 0 || deadline - observedMarginRealTime > MARGIN_CALL_GRACE_REAL_MS
                    || marginCallDeadlines.putIfAbsent(accountId, deadline) != null)
                throw new IllegalArgumentException("Deadline margin save tidak valid atau duplikat");
        }
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
            if (p.has("unpaidFee")) {
                position.addUnpaidFee(new Money(new BigDecimal(p.getString("unpaidFee")), currency));
            }
            shortPositions.computeIfAbsent(p.getString("accountId"), k -> new HashMap<>()).put(position.getTicker(), position);
        }
    }

    public List<ComplianceAuditLog> getAuditLogs() {
        return Collections.unmodifiableList(auditLogs);
    }

    public synchronized boolean observeMarginCall(Account account, InstrumentCatalog catalog, BigDecimal rate,
                                                   long realNow, long gameNow) {
        if (realNow < 0) throw new IllegalArgumentException("Waktu nyata margin tidak valid");
        observedMarginRealTime = Math.max(observedMarginRealTime, realNow);
        MarginHealthReport health = evaluateMarginHealth(account, catalog, rate);
        if (!health.getMarginDebt().isPositive() || health.getMarginRatio().compareTo(MAINTENANCE_MARGIN_RATIO) >= 0)
            return marginCallDeadlines.remove(account.getId()) != null;
        if (marginCallDeadlines.containsKey(account.getId())) return false;
        long deadline = Math.addExact(observedMarginRealTime, MARGIN_CALL_GRACE_REAL_MS);
        marginCallDeadlines.put(account.getId(), deadline);
        auditLogs.add(new ComplianceAuditLog("MARGIN-CALL-" + account.getId() + "-" + deadline, gameNow, account.getId(),
                ComplianceAuditLog.AuditCategory.MARGIN_CALL, "Jaminan margin di bawah 30%",
                "Tambah jaminan atau kurangi utang sebelum " + deadline + " (120 menit nyata)"));
        return true;
    }

    public synchronized long getMarginCallDeadline(String accountId) { return marginCallDeadlines.getOrDefault(accountId, 0L); }

    public synchronized long getMarginCallRemainingRealMs(String accountId, long realNow) {
        return Math.max(0, getMarginCallDeadline(accountId) - Math.max(observedMarginRealTime, realNow));
    }

    public synchronized boolean isMarginCallDue(String accountId, long realNow) {
        return getMarginCallDeadline(accountId) > 0 && getMarginCallRemainingRealMs(accountId, realNow) == 0;
    }

    public synchronized void recordLiquidationAttempt(Account account, Instrument instrument, boolean halted, long gameNow) {
        String id = "MARGIN-LIQ-" + account.getId() + "-" + instrument.getId() + "-" + getMarginCallDeadline(account.getId()) + "-" + halted;
        if (auditLogs.stream().anyMatch(log -> log.getId().equals(id))) return;
        auditLogs.add(new ComplianceAuditLog(id, gameNow, account.getId(), halted
                ? ComplianceAuditLog.AuditCategory.HALT_ENFORCEMENT : ComplianceAuditLog.AuditCategory.FORCED_LIQUIDATION,
                (halted ? "Likuidasi menunggu suspensi berakhir: " : "Order likuidasi setelah tenggat margin: ") + instrument.getTicker(),
                "Tetap mengikuti likuiditas dan aturan order AON"));
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
     * Restrukturisasi utang margin untuk pemulihan solvabilitas tanpa suntikan uang tunai.
     */
    public synchronized Money restructureDebt(Account account, Currency currency, long timestamp) {
        Money currentDebt = getMarginDebt(account.getId(), currency);
        if (currentDebt.isZero()) {
            throw new IllegalStateException("Tidak ada utang margin untuk direstrukturisasi pada mata uang " + currency);
        }
        // Restrukturisasi memberikan keringanan 50% dari pokok utang
        Money relief = currentDebt.multiply(new BigDecimal("0.50"));
        setMarginDebt(account.getId(), currentDebt.subtract(relief));
        auditLogs.add(new ComplianceAuditLog(
                "LOG-RESTRUCTURE-" + System.nanoTime(), timestamp, account.getId(),
                ComplianceAuditLog.AuditCategory.MARGIN_LOAN,
                "Restrukturisasi utang margin " + currency + ": keringanan " + relief,
                "Penyelesaian utang tanpa suntikan uang otomatis"
        ));
        return relief;
    }

    /**
     * Membuka posisi Short Selling dengan meminjam saham.
     * Hasil penjualan short ditahan sebagai jaminan (Collateral Locked) (FR-15.05).
     * Memeriksa ketersediaan saham pinjaman pada lender pool (Shared Liquidity).
     */
    public ShortPosition openShortPosition(
            Account account,
            Instrument instrument,
            long sharesToShort,
            Account lenderPool,
            EconomyLedger ledger,
            long timestamp) {
        return openShortPosition(account, instrument, sharesToShort, lenderPool, ledger, timestamp, 0L);
    }

    public ShortPosition openShortPosition(
            Account account,
            Instrument instrument,
            long sharesToShort,
            Account lenderPool,
            EconomyLedger ledger,
            long timestamp,
            long lenderCommittedShares) {

        if (sharesToShort <= 0) throw new IllegalArgumentException("Jumlah lembar short harus positif");
        if (getShortPosition(account.getId(), instrument.getTicker()) != null) {
            throw new IllegalStateException("Posisi short masih aktif; tutup kewajiban sebelum membuka pinjaman baru");
        }

        // 0. Hitung nominal dan fee secara konsisten terhadap pembulatan mata uang (AT01)
        BigDecimal currentPrice = instrument.getCurrentPrice().getAmount();
        BigDecimal grossProceeds = currentPrice.multiply(BigDecimal.valueOf(sharesToShort));
        Money grossMoney = new Money(grossProceeds, instrument.getCurrency());
        BigDecimal fee = grossProceeds.multiply(new BigDecimal("0.0025")); // 0.25% fee jual
        Money feeMoney = new Money(fee, instrument.getCurrency());
        Money proceedsMoney = grossMoney.subtract(feeMoney);

        if (proceedsMoney.isNegative()) {
            throw new IllegalStateException("Hasil bersih penjualan short tidak boleh negatif");
        }

        // 1. Validasi ketersediaan inventori pinjaman pada lender pool sebelum mutasi apa pun (Preflight)
        Position lenderPos = null;
        if (lenderPool != null) {
            lenderPos = lenderPool.getPosition(instrument.getId());
            if (lenderPos == null) lenderPos = lenderPool.getPosition(instrument.getTicker());
            long totalQuantity = (lenderPos != null) ? lenderPos.getQuantity() : 0L;
            long availableLendable = Math.max(0L, totalQuantity - lenderCommittedShares);
            if (sharesToShort > availableLendable) {
                throw new IllegalStateException("Stok pinjaman saham (lendable inventory) tidak mencukupi untuk short: "
                        + instrument.getTicker() + " (diminta: " + sharesToShort + ", tersedia bebas: " + availableLendable + ")");
            }
        }

        // Lakukan pemotongan saham pinjaman hanya setelah seluruh validasi preflight lulus
        if (lenderPos != null) {
            lenderPos.borrowShares(sharesToShort);
        }

        // 2. Catat kas masuk hasil penjualan short via ledger
        ledger.recordMutation(
                "SHORT-PROCEEDS-" + System.nanoTime(),
                account,
                LedgerEntryType.TRADE_SELL,
                grossMoney,
                true,
                timestamp,
                "Hasil penjualan short " + instrument.getTicker() + " " + sharesToShort + " lembar"
        );

        // 3. Potong fee transaksi
        ledger.recordMutation(
                "SHORT-FEE-" + System.nanoTime(),
                account,
                LedgerEntryType.TRANSACTION_FEE,
                feeMoney,
                false,
                timestamp,
                "Fee bursa penjualan short " + instrument.getTicker()
        );

        // 4. TAHAN SELURUH HASIL PENJUALAN SEBAGAI JAMINAN (Collateral Locked) - FR-15.05
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
                "Hasil penjualan " + proceedsMoney + " dikunci sebagai jaminan kas tertahan"
        ));

        return shortPos;
    }

    public synchronized ShortPosition openShortPosition(
            Account account,
            Instrument instrument,
            long sharesToShort,
            EconomyLedger ledger,
            long timestamp) {
        return openShortPosition(account, instrument, sharesToShort, null, ledger, timestamp);
    }

    /**
     * Menutup posisi short (Buy to Cover).
     */
    public synchronized void closeShortPosition(
            Account account,
            Instrument instrument,
            long sharesToCover,
            Account lenderPool,
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

        // Lunasi sisa tunggakan sewa short jika kas tersedia mencukupi (QC03)
        if (shortPos.getAccruedUnpaidFee().isPositive()) {
            Money unpaid = shortPos.getAccruedUnpaidFee();
            Money avail = account.getAvailableCash(instrument.getCurrency());
            if (avail.isGreaterThanOrEqual(unpaid)) {
                ledger.recordMutation(
                        "SHORT-COVER-ARREARS-" + System.nanoTime(),
                        account,
                        LedgerEntryType.TRANSACTION_FEE,
                        unpaid,
                        false,
                        timestamp,
                        "Pelunasan sisa tunggakan sewa short saat penutupan " + instrument.getTicker()
                );
                shortPos.payUnpaidFee(unpaid);
            }
        }

        // Hapus posisi hanya jika kuantitas saham 0 DAN tunggakan biaya sewa sudah lunas (QC03)
        if (shortPos.getBorrowedShares() == 0 && shortPos.getAccruedUnpaidFee().isZero()) {
            userShorts.remove(instrument.getTicker());
        }

        // Kembalikan saham ke inventori lender pool jika ada
        if (lenderPool != null) {
            Position lenderPos = lenderPool.getPosition(instrument.getId());
            if (lenderPos == null) lenderPos = lenderPool.getPosition(instrument.getTicker());
            if (lenderPos != null) {
                lenderPos.returnShares(sharesToCover);
            }
        }

        auditLogs.add(new ComplianceAuditLog(
                "LOG-" + System.nanoTime(), timestamp, account.getId(),
                ComplianceAuditLog.AuditCategory.SHORT_BORROW,
                "Penutupan posisi short " + instrument.getTicker() + " " + sharesToCover + " lembar",
                "Jaminan dilepaskan dan saham pinjaman dikembalikan ke broker/lender"
        ));
    }

    public synchronized void closeShortPosition(
            Account account,
            Instrument instrument,
            long sharesToCover,
            EconomyLedger ledger,
            long timestamp) {
        closeShortPosition(account, instrument, sharesToCover, null, ledger, timestamp);
    }

    public static final BigDecimal ANNUAL_MARGIN_LOAN_INTEREST_RATE = new BigDecimal("0.072"); // 0.02% per game day on a 360-day basis.

    /**
     * Membebankan biaya pembiayaan harian (Daily Financing Costs):
     * 1. Biaya sewa pinjaman short harian (Short Borrow Fee).
     * 2. Bunga harian pinjaman fasilitas margin (Margin Loan Interest).
     */
    public synchronized void accrueDailyFinancingCosts(
            Account account,
            InstrumentCatalog catalog,
            EconomyLedger ledger,
            BigDecimal usdRate,
            long timestamp) {

        long day = java.time.Instant.ofEpochMilli(timestamp).atZone(java.time.ZoneId.of("Asia/Jakarta")).toLocalDate().toEpochDay();
        Long previous = financingDays.get(account.getId());
        if (previous != null && day <= previous) return;

        // 1. Biaya pinjaman short harian (Borrow fee)
        Map<String, ShortPosition> userShorts = shortPositions.get(account.getId());
        if (userShorts != null) {
            List<String> clearedTickers = new ArrayList<>();
            for (ShortPosition sp : userShorts.values()) {
                Instrument inst = catalog.getById("IDX:" + sp.getTicker());
                if (inst == null) inst = catalog.getById("US:" + sp.getTicker());
                BigDecimal px = (inst != null) ? inst.getCurrentPrice().getAmount() : sp.getAverageShortPrice();
                Money dailyFee = sp.calculateDailyBorrowFee(px);
                if (dailyFee.isPositive()) {
                    Money available = account.getAvailableCash(dailyFee.getCurrency());
                    Money feePaid = available.compareTo(dailyFee) >= 0 ? dailyFee : (available.isPositive() ? available : Money.zero(dailyFee.getCurrency()));
                    Money unpaid = dailyFee.subtract(feePaid);
                    if (feePaid.isPositive()) {
                        ledger.recordMutation(
                                "SHORT-BORROW-FEE-" + sp.getTicker() + "-" + timestamp,
                                account,
                                LedgerEntryType.TRANSACTION_FEE,
                                feePaid,
                                false,
                                timestamp,
                                "Biaya sewa pinjaman short harian " + sp.getTicker()
                        );
                        auditLogs.add(new ComplianceAuditLog(
                                "LOG-BORROW-FEE-" + System.nanoTime(), timestamp, account.getId(),
                                ComplianceAuditLog.AuditCategory.SHORT_BORROW,
                                "Debet biaya sewa short " + sp.getTicker() + ": " + feePaid,
                                "Dipotong dari kas tersedia harian"
                        ));
                    }
                    if (unpaid.isPositive()) {
                        sp.addUnpaidFee(unpaid);
                        auditLogs.add(new ComplianceAuditLog(
                                "LOG-BORROW-FEE-UNPAID-" + System.nanoTime(), timestamp, account.getId(),
                                ComplianceAuditLog.AuditCategory.SHORT_BORROW,
                                "Tunggakan biaya sewa short " + sp.getTicker() + ": " + unpaid,
                                "Kas tidak mencukupi, dicatat sebagai kewajiban terutang"
                        ));
                    }
                }
                if (sp.getAccruedUnpaidFee().isPositive()) {
                    Money availForArrears = account.getAvailableCash(sp.getCurrency());
                    if (availForArrears.isPositive()) {
                        Money arrearsPaid = availForArrears.compareTo(sp.getAccruedUnpaidFee()) >= 0 ? sp.getAccruedUnpaidFee() : availForArrears;
                        ledger.recordMutation(
                                "SHORT-ARREARS-PAY-" + sp.getTicker() + "-" + timestamp,
                                account,
                                LedgerEntryType.TRANSACTION_FEE,
                                arrearsPaid,
                                false,
                                timestamp,
                                "Pelunasan tunggakan biaya sewa short " + sp.getTicker()
                        );
                        sp.payUnpaidFee(arrearsPaid);
                    }
                }
                if (sp.getBorrowedShares() == 0 && sp.getAccruedUnpaidFee().isZero()) {
                    clearedTickers.add(sp.getTicker());
                }
            }
            for (String ticker : clearedTickers) {
                userShorts.remove(ticker);
            }
        }

        // 2. Bunga harian pinjaman margin: 0,02% dari utang berjalan.
        Map<Currency, Money> debts = marginDebts.get(account.getId());
        if (debts != null) {
            for (Map.Entry<Currency, Money> entry : debts.entrySet()) {
                Currency cur = entry.getKey();
                Money debt = entry.getValue();
                if (debt.isPositive()) {
                    BigDecimal dailyRate = ANNUAL_MARGIN_LOAN_INTEREST_RATE.divide(BigDecimal.valueOf(360), 8, RoundingMode.HALF_UP);
                    BigDecimal interestAmt = debt.getAmount().multiply(dailyRate).setScale(2, RoundingMode.HALF_UP);
                    if (interestAmt.compareTo(BigDecimal.ZERO) > 0) {
                        Money interest = new Money(interestAmt, cur);
                        if (account.getAvailableCash(cur).isGreaterThanOrEqual(interest)) {
                            ledger.recordMutation(
                                    "MARGIN-INTEREST-" + cur.name() + "-" + timestamp,
                                    account,
                                    LedgerEntryType.TRANSACTION_FEE,
                                    interest,
                                    false,
                                    timestamp,
                                    "Bunga harian pinjaman margin " + cur.name()
                            );
                        } else {
                            setMarginDebt(account.getId(), debt.add(interest));
                            auditLogs.add(new ComplianceAuditLog(
                                    "LOG-MARGIN-INT-" + System.nanoTime(), timestamp, account.getId(),
                                    ComplianceAuditLog.AuditCategory.MARGIN_LOAN,
                                    "Akumulasi bunga margin " + interest + " ke pokok utang",
                                    "Kas tidak mencukupi untuk bayar bunga langsung"
                            ));
                        }
                    }
                }
            }
        }
        financingDays.put(account.getId(), day);
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
                Instrument instrument = catalog.getById(p.getExchange() + ":" + p.getTicker());
                BigDecimal mark = instrument == null ? p.getCurrentPrice() : instrument.getCurrentPrice().getAmount();
                BigDecimal val = mark.multiply(BigDecimal.valueOf(p.getQuantity()));
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
                BigDecimal liab = curPx.multiply(BigDecimal.valueOf(sp.getBorrowedShares())).add(sp.getAccruedUnpaidFee().getAmount());
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

    public synchronized Money getTotalShortLiability(String accountId, InstrumentCatalog catalog, BigDecimal usdRate) {
        Map<String, ShortPosition> userShorts = shortPositions.get(accountId);
        if (userShorts == null || userShorts.isEmpty()) return Money.zero(Currency.IDR);
        BigDecimal totalIdr = BigDecimal.ZERO;
        for (ShortPosition sp : userShorts.values()) {
            Instrument inst = catalog != null ? catalog.getById("IDX:" + sp.getTicker()) : null;
            if (inst == null && catalog != null) inst = catalog.getById("US:" + sp.getTicker());
            BigDecimal curPx = inst != null ? inst.getCurrentPrice().getAmount() : sp.getAverageShortPrice();
            BigDecimal liab = curPx.multiply(BigDecimal.valueOf(sp.getBorrowedShares())).add(sp.getAccruedUnpaidFee().getAmount());
            if (sp.getCurrency() == Currency.USD && usdRate != null) liab = liab.multiply(usdRate);
            totalIdr = totalIdr.add(liab);
        }
        return new Money(totalIdr, Currency.IDR);
    }

}
