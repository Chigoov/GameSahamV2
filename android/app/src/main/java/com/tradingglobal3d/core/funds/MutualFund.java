package com.tradingglobal3d.core.funds;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.trading.*;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;

/** Unit/NAV dealing with canonical cash, equity positions, and issuer-backed debt contracts. */
public final class MutualFund {
    public enum FundType {
        MONEY_MARKET("Pasar Uang", "Rendah", 0.05),
        FIXED_INCOME("Pendapatan Tetap", "Moderat", 0.08),
        EQUITY_INDEX("Indeks Saham", "Agresif", 0.12),
        MIXED("Campuran", "Moderat-Agresif", 0.10),
        EQUITY_ACTIVE("Saham Aktif", "Agresif", 0.14);
        private final String label, riskLevel;
        private final double targetAnnualYield;
        FundType(String label, String riskLevel, double targetAnnualYield) {
            this.label = label; this.riskLevel = riskLevel; this.targetAnnualYield = targetAnnualYield;
        }
        public String getLabel() { return label; }
        public String getRiskLevel() { return riskLevel; }
        /** Historical product metadata, never an accrual or guaranteed return. */
        public double getTargetAnnualYield() { return targetAnnualYield; }

        public static FundType fromPolicy(String policy) {
            if (policy == null) return MONEY_MARKET;
            try { return FundType.valueOf(policy.toUpperCase()); }
            catch (Exception e) { return MONEY_MARKET; }
        }
    }

    public static final BigDecimal INITIAL_NAV = new BigDecimal("10000.000000000000");
    public static final BigDecimal ENTRY_FEE_RATE = new BigDecimal("0.001");
    public static final BigDecimal EXIT_FEE_RATE = new BigDecimal("0.001");
    public static final BigDecimal MANAGEMENT_FEE_RATE = new BigDecimal("0.0075");
    private static final ZoneOffset JAKARTA = ZoneOffset.ofHours(7);

    public static final class PendingFundOrder {
        final String orderId, accountId;
        final boolean isSubscribe;
        final Money cashAmount;
        final BigDecimal units;
        final long requestTimestamp, cutoffTimestamp, cancellationDeadline;
        String status = "ACCEPTED";
        JSONObject settlement;
        PendingFundOrder(String orderId, String accountId, boolean subscribe, Money cash, BigDecimal units,
                         long requested, long cutoff, long deadline) {
            this.orderId = orderId; this.accountId = accountId; isSubscribe = subscribe;
            cashAmount = cash; this.units = units; requestTimestamp = requested;
            cutoffTimestamp = cutoff; cancellationDeadline = deadline;
        }
        boolean pending() { return status.equals("ACCEPTED") || status.startsWith("PENDING_"); }
    }

    private final String id, name, managerName;
    private final FundType type;
    private Account poolAccount;
    private InstrumentCatalog valuationCatalog;
    private boolean canonicalPool;
    private Account debtIssuer;
    private BigDecimal debtPrincipal = BigDecimal.ZERO, debtAccrued = BigDecimal.ZERO;
    private BigDecimal debtPriceFactor = BigDecimal.ONE;
    private long debtAccrualDay = Long.MIN_VALUE;
    private long lastDebtActionTimestamp = Long.MIN_VALUE;
    public String getDebtIssuerAccountId() { return "ACC-FUND-ISSUER-" + id; }
    private Money markedSecurities = Money.zero(Currency.IDR);
    public String getPoolAccountId() { return "ACC-FUND-" + id; }
    synchronized void registerPoolAccount(Map<String, Account> accounts, EconomyLedger ledger, long now) {
        Account account = accounts.get(getPoolAccountId());
        if (account == null) {
            if (canonicalPool) throw new IllegalArgumentException("Akun pool canonical hilang");
            account = new Account(getPoolAccountId(), name, AccountType.FIRM, null);
            accounts.put(account.getId(), account);
            if (poolCash.isPositive()) ledger.recordMutation("FUND-LEGACY-POOL-" + id, account,
                    LedgerEntryType.INITIAL_CAPITAL, poolCash, true, now, "Migrasi saldo pool reksa dana tersimpan");
        } else if (!canonicalPool && (!account.getTotalCash(Currency.IDR).equals(poolCash) || !account.getPositions().isEmpty()))
            throw new IllegalArgumentException("Akun pool migrasi sudah terpakai");
        if (account.getType() != AccountType.FIRM) throw new IllegalArgumentException("Jenis akun pool salah");
        poolAccount = account; canonicalPool = true;
        if (type != FundType.EQUITY_INDEX) {
            debtIssuer = accounts.get(getDebtIssuerAccountId());
            if (debtIssuer == null) {
                if (debtPrincipal.signum() > 0 || debtAccrued.signum() > 0)
                    throw new IllegalArgumentException("Akun penerbit backing hilang");
                debtIssuer = new Account(getDebtIssuerAccountId(), "Penerbit kontrak " + name, AccountType.FIRM, null);
                accounts.put(debtIssuer.getId(), debtIssuer);
                // ponytail: finite NPC capital backs coupons; exhausted issuers retain unpaid liabilities, never mint returns.
                ledger.recordMutation("FUND-ISSUER-SEED-" + id, debtIssuer, LedgerEntryType.INITIAL_CAPITAL,
                        Money.of(10_000_000, Currency.IDR), true, now, "Modal awal NPC penerbit kontrak game " + name);
            }
        }
    }
    synchronized void bindAccounts(Map<String, Account> accounts, InstrumentCatalog catalog) {
        Account account = accounts.get(getPoolAccountId());
        if (!canonicalPool || account == null || account.getType() != AccountType.FIRM)
            throw new IllegalArgumentException("Pool belum diregistrasi");
        poolAccount = account; valuationCatalog = Objects.requireNonNull(catalog);
        if (type != FundType.EQUITY_INDEX) {
            debtIssuer = accounts.get(getDebtIssuerAccountId());
            if (debtIssuer == null && (debtPrincipal.signum() > 0 || debtAccrued.signum() > 0))
                throw new IllegalArgumentException("Akun penerbit backing hilang");
        }
        markAssets();
    }
    private Money markAssets() {
        if (poolAccount != null) {
            poolCash = poolAccount.getTotalCash(Currency.IDR);
            markedSecurities = Money.zero(Currency.IDR);
            for (Map.Entry<String, Position> entry : poolAccount.getPositions().entrySet()) {
                Instrument instrument = valuationCatalog == null ? null : valuationCatalog.getById(entry.getKey());
                if (instrument == null || instrument.getCurrency() != Currency.IDR || entry.getValue().getQuantity() < 0)
                    throw new IllegalStateException("Instrumen pool tidak dapat divaluasi");
                markedSecurities = markedSecurities.add(instrument.getCurrentPrice().multiply(BigDecimal.valueOf(entry.getValue().getQuantity())));
            }
            markedSecurities = markedSecurities.add(new Money(debtFairValue().setScale(0, RoundingMode.DOWN), Currency.IDR));
        }
        return poolCash.add(markedSecurities);
    }
    public synchronized Order getEquityRebalanceOrder(InstrumentCatalog catalog, long now) {
        if (type != FundType.EQUITY_INDEX || poolAccount == null) return null;
        valuationCatalog = catalog;
        Money assets = markAssets();
        Instrument stock = catalog.getById("IDX:BBCA");
        if (stock == null || !stock.isTradable() || stock.getCurrency() != Currency.IDR || !stock.getCurrentPrice().isPositive()) return null;
        Position position = poolAccount.getPosition(stock.getId());
        long owned = position == null ? 0 : position.getQuantity();
        BigDecimal redemption = BigDecimal.ZERO;
        for (PendingFundOrder order : orders.values()) if (order.pending() && !order.isSubscribe)
            redemption = redemption.add(order.units.multiply(getNavPerUnit()));
        BigDecimal price = stock.getCurrentPrice().getAmount();
        long lot = stock.getLotSize();
        long quantity; OrderSide side;
        if (redemption.signum() > 0) {
            BigDecimal shortfall = redemption.add(feeRemainder).subtract(poolAccount.getAvailableCash(Currency.IDR).getAmount());
            if (shortfall.signum() <= 0) return null;
            BigDecimal netLot = price.multiply(BigDecimal.valueOf(lot)).multiply(BigDecimal.ONE.subtract(MatchingEngine.SELL_FEE_RATE));
            quantity = Math.min(owned / lot, shortfall.divide(netLot, 0, RoundingMode.UP).longValueExact()) * lot;
            side = OrderSide.SELL;
        } else {
            BigDecimal target = assets.getAmount().multiply(new BigDecimal("0.8"));
            BigDecimal gap = target.subtract(price.multiply(BigDecimal.valueOf(owned)));
            BigDecimal budget = poolAccount.getAvailableCash(Currency.IDR).getAmount().subtract(feeRemainder).max(BigDecimal.ZERO).min(gap);
            if (budget.signum() <= 0) return null;
            BigDecimal grossLot = price.multiply(BigDecimal.valueOf(lot)).multiply(BigDecimal.ONE.add(MatchingEngine.BUY_FEE_RATE));
            quantity = budget.divide(grossLot, 0, RoundingMode.DOWN).longValueExact() * lot;
            side = OrderSide.BUY;
        }
        if (quantity <= 0) return null;
        String request = id + ":REBALANCE:" + now + ":" + side;
        return new Order(request, request, poolAccount.getId(), stock.getId(), side, OrderType.LIMIT,
                stock.getCurrentPrice(), null, quantity, now, Money.zero(Currency.IDR));
    }
    private BigDecimal totalUnits = BigDecimal.ZERO.setScale(6);
    private Money poolCash = Money.zero(Currency.IDR);
    private final Map<String, BigDecimal> holdings = new LinkedHashMap<>();
    private final Map<String, PendingFundOrder> orders = new LinkedHashMap<>();
    private final List<JSONObject> historicalNavs = new ArrayList<>();
    private long lastCutoffTimestamp = Long.MIN_VALUE, feeAccrualDay = Long.MIN_VALUE;
    private boolean legacySponsorMigration;
    private BigDecimal feeRemainder = BigDecimal.ZERO;
    private Money managementFeesPaid = Money.zero(Currency.IDR);

    public MutualFund(String id, String name, FundType type, String managerName) {
        this.id = Objects.requireNonNull(id); this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type); this.managerName = Objects.requireNonNull(managerName);
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public FundType getType() { return type; }
    public String getManagerName() { return managerName; }
    public BigDecimal getNavPerUnit() {
        return totalUnits.signum() == 0 ? INITIAL_NAV : markAssets().getAmount().subtract(feeRemainder)
                .divide(totalUnits, 12, RoundingMode.HALF_UP);
    }
    public BigDecimal getTotalUnits() { return totalUnits; }
    public Money getPoolCash() { markAssets(); return poolCash; }
    public Money getReservedSubscriptionCash(String accountId) {
        Money reserved = Money.zero(Currency.IDR);
        for (PendingFundOrder order : orders.values())
            if (order.pending() && order.isSubscribe && order.accountId.equals(accountId)) reserved = reserved.add(order.cashAmount);
        return reserved;
    }
    public synchronized void validateAccounts(Map<String, Account> accounts) {
        for (String accountId : holdings.keySet()) {
            if (legacySponsorMigration && accountId.equals("LEGACY-SPONSOR:" + id)) continue;
            Account account = accounts.get(accountId);
            if (account == null || !accountId.equals(account.getId())) throw new IllegalArgumentException("Pemegang unit tidak mempunyai akun canonical");
        }
        for (PendingFundOrder order : orders.values()) {
            Account account = accounts.get(order.accountId);
            if (account == null || !order.accountId.equals(account.getId())) throw new IllegalArgumentException("Akun order reksa dana tidak ditemukan");
        }
    }
    public BigDecimal getUnits(String accountId) { return holdings.getOrDefault(accountId, BigDecimal.ZERO).setScale(6); }
    public BigDecimal getReservedUnits(String accountId) {
        BigDecimal reserved = BigDecimal.ZERO;
        for (PendingFundOrder order : orders.values())
            if (order.pending() && !order.isSubscribe && order.accountId.equals(accountId)) reserved = reserved.add(order.units);
        return reserved.setScale(6);
    }
    public BigDecimal getAvailableUnits(String accountId) { return getUnits(accountId).subtract(getReservedUnits(accountId)); }
    public Money getHoldingsValue(String accountId) { return new Money(getUnits(accountId).multiply(getNavPerUnit()), Currency.IDR); }

    private BigDecimal debtFairValue() {
        BigDecimal fair = debtPrincipal.multiply(debtPriceFactor).add(debtAccrued);
        // Recoverability is bounded by this issuer's canonical liquid assets, including funded principal.
        return debtIssuer == null ? fair : fair.min(debtIssuer.getTotalCash(Currency.IDR).getAmount()).max(BigDecimal.ZERO);
    }

    private void transferDebtCash(EconomyLedger ledger, Account from, Account to, Money amount, String request, long now) {
        if (!amount.isPositive()) return;
        ledger.recordMutation(request + ":OUT", from, LedgerEntryType.YIELD, amount, false, now, "Kontrak backing " + id);
        ledger.recordMutation(request + ":IN", to, LedgerEntryType.YIELD, amount, true, now, "Kontrak backing " + id);
    }

    private void accrueDebt(EconomyLedger ledger, long now) {
        long day = Instant.ofEpochMilli(now).atOffset(JAKARTA).toLocalDate().toEpochDay();
        if (debtAccrualDay == Long.MIN_VALUE) debtAccrualDay = day;
        if (day < debtAccrualDay) throw new IllegalArgumentException("Waktu kontrak backing mundur");
        long elapsed = day - debtAccrualDay;
        BigDecimal coupon = new BigDecimal(type == FundType.MONEY_MARKET ? "0.05" : "0.08");
        debtAccrued = debtAccrued.add(debtPrincipal.multiply(coupon).multiply(BigDecimal.valueOf(elapsed))
                .divide(BigDecimal.valueOf(365), 16, RoundingMode.HALF_UP));
        debtAccrualDay = day;
        BigDecimal excess = debtIssuer.getAvailableCash(Currency.IDR).getAmount().subtract(debtPrincipal).max(BigDecimal.ZERO);
        Money paid = new Money(debtAccrued.min(excess).setScale(0, RoundingMode.DOWN), Currency.IDR);
        transferDebtCash(ledger, debtIssuer, poolAccount, paid, id + ":COUPON:" + now, now);
        debtAccrued = debtAccrued.subtract(paid.getAmount());
    }

    private void manageDebtLiquidity(EconomyLedger ledger, long now) {
        if (debtIssuer == null || poolAccount == null) return;
        BigDecimal needed = feeRemainder;
        BigDecimal pendingUnits = BigDecimal.ZERO;
        for (PendingFundOrder order : orders.values()) if (order.pending() && !order.isSubscribe)
            { needed = needed.add(order.units.multiply(getNavPerUnit())); pendingUnits = pendingUnits.add(order.units); }
        BigDecimal cash = poolAccount.getAvailableCash(Currency.IDR).getAmount();
        if (needed.compareTo(cash) > 0 && debtPrincipal.signum() > 0) {
            BigDecimal fairPrincipal = debtFairValue().subtract(debtAccrued).max(BigDecimal.ZERO);
            BigDecimal price = fairPrincipal.divide(debtPrincipal, 16, RoundingMode.DOWN);
            if (price.signum() > 0) {
                BigDecimal face = needed.subtract(cash).divide(price, 0, RoundingMode.UP).min(debtPrincipal);
                if (pendingUnits.compareTo(totalUnits) == 0) face = debtPrincipal;
                Money proceeds = new Money(face.multiply(price).setScale(0, RoundingMode.DOWN)
                        .min(debtIssuer.getAvailableCash(Currency.IDR).getAmount()), Currency.IDR);
                if (proceeds.isPositive()) {
                    transferDebtCash(ledger, debtIssuer, poolAccount, proceeds, id + ":DEBT-REDEEM:" + now, now);
                    debtPrincipal = debtPrincipal.subtract(face);
                }
            }
        } else if (needed.signum() == 0 && totalUnits.signum() > 0) {
            BigDecimal target = markAssets().getAmount().multiply(new BigDecimal("0.8"));
            Money invest = new Money(target.subtract(debtPrincipal).max(BigDecimal.ZERO).min(cash)
                    .setScale(0, RoundingMode.DOWN), Currency.IDR);
            if (invest.isPositive()) {
                transferDebtCash(ledger, poolAccount, debtIssuer, invest, id + ":DEBT-ISSUE:" + now, now);
                debtPrincipal = debtPrincipal.add(invest.getAmount());
            }
        }
    }

    public synchronized void advanceDay(double marketFactorChange, EconomyLedger ledger, long now) {
        if (!Double.isFinite(marketFactorChange)) throw new IllegalArgumentException("Perubahan yield tidak sah");
        if (type == FundType.EQUITY_INDEX || poolAccount == null || debtIssuer == null) return;
        if (now <= lastDebtActionTimestamp) return;
        accrueDebt(ledger, now);
        if (type == FundType.FIXED_INCOME)
            debtPriceFactor = BigDecimal.ONE.subtract(BigDecimal.valueOf(marketFactorChange))
                    .max(new BigDecimal("0.7")).min(new BigDecimal("1.1"));
        manageDebtLiquidity(ledger, now);
        lastDebtActionTimestamp = now;
        markAssets();
    }

    private static boolean dealingDay(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY;
    }
    private static long cutoff(LocalDate date) { return date.atTime(16, 0).toInstant(JAKARTA).toEpochMilli(); }
    private long nextCutoff(long now) {
        LocalDate date = Instant.ofEpochMilli(now).atOffset(JAKARTA).toLocalDate();
        if (now >= cutoff(date) || lastCutoffTimestamp >= cutoff(date)) date = date.plusDays(1);
        while (!dealingDay(date)) date = date.plusDays(1);
        return cutoff(date);
    }
    private static void validateId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty() || orderId.length() > 128)
            throw new IllegalArgumentException("ID order wajib diisi (maksimal 128 karakter)");
    }
    private PendingFundOrder duplicate(String orderId, Account account, boolean subscribe, Money cash, BigDecimal units) {
        validateId(orderId);
        PendingFundOrder prior = orders.get(orderId);
        if (prior != null && (!prior.accountId.equals(account.getId()) || prior.isSubscribe != subscribe
                || !prior.cashAmount.equals(cash) || prior.units.compareTo(units) != 0))
            throw new IllegalArgumentException("ID order sudah dipakai dengan payload berbeda");
        return prior;
    }
    private JSONObject accept(String orderId, Account account, boolean subscribe, Money cash, BigDecimal units, long now) throws JSONException {
        if (now < 0 || now < lastCutoffTimestamp) throw new IllegalArgumentException("Timestamp order mendahului cutoff terakhir");
        long due = nextCutoff(now);
        PendingFundOrder order = new PendingFundOrder(orderId, account.getId(), subscribe, cash, units, now,
                due, due - 30 * 60 * 1000L);
        orders.put(orderId, order);
        if (feeAccrualDay == Long.MIN_VALUE) feeAccrualDay = Instant.ofEpochMilli(now).atOffset(JAKARTA).toLocalDate().toEpochDay();
        return receipt(order);
    }
    public synchronized JSONObject submitSubscription(String orderId, Account account, Money grossCash,
                                                       EconomyLedger ledger, long now) throws JSONException {
        Objects.requireNonNull(account); Objects.requireNonNull(grossCash); Objects.requireNonNull(ledger);
        if (grossCash.getCurrency() != Currency.IDR || grossCash.getAmount().compareTo(BigDecimal.valueOf(10_000)) < 0)
            throw new IllegalArgumentException("Subscription minimal Rp10.000 dalam IDR");
        PendingFundOrder prior = duplicate(orderId, account, true, grossCash, BigDecimal.ZERO);
        if (prior != null) return receipt(prior);
        if (now < 0 || now < lastCutoffTimestamp) throw new IllegalArgumentException("Timestamp order mendahului cutoff terakhir");
        account.reserveCash(grossCash);
        return accept(orderId, account, true, grossCash, BigDecimal.ZERO.setScale(6), now);
    }
    public synchronized JSONObject submitRedemption(String orderId, Account account, BigDecimal units,
                                                     EconomyLedger ledger, long now) throws JSONException {
        Objects.requireNonNull(account); Objects.requireNonNull(ledger);
        if (units == null || units.signum() <= 0) throw new IllegalArgumentException("Unit redemption wajib positif");
        try { units = units.setScale(6, RoundingMode.UNNECESSARY); }
        catch (ArithmeticException invalid) { throw new IllegalArgumentException("Presisi unit maksimal 6 desimal"); }
        PendingFundOrder prior = duplicate(orderId, account, false, Money.zero(Currency.IDR), units);
        if (prior != null) return receipt(prior);
        if (getAvailableUnits(account.getId()).compareTo(units) < 0) throw new IllegalStateException("Unit tersedia tidak mencukupi");
        return accept(orderId, account, false, Money.zero(Currency.IDR), units, now);
    }
    public synchronized JSONObject cancelOrder(String orderId, Account account, long now) throws JSONException {
        PendingFundOrder order = orders.get(orderId);
        if (order == null || !order.accountId.equals(account.getId())) throw new IllegalArgumentException("Order tidak ditemukan pada akun");
        if (order.status.equals("CANCELLED")) return receipt(order);
        if (!order.pending() || now >= order.cancellationDeadline) throw new IllegalStateException("Batas pembatalan 15:30 WIB telah lewat");
        if (order.isSubscribe) account.releaseReservedCash(order.cashAmount);
        order.status = "CANCELLED";
        return receipt(order);
    }
    public synchronized JSONObject getOrder(String orderId) throws JSONException {
        PendingFundOrder order = orders.get(orderId);
        if (order == null) throw new IllegalArgumentException("Order reksa dana tidak ditemukan");
        return receipt(order);
    }
    private JSONObject receipt(PendingFundOrder order) throws JSONException {
        JSONObject result = order.settlement == null ? new JSONObject() : new JSONObject(order.settlement.toString());
        result.put("success", true).put("fundId", id).put("orderId", order.orderId).put("receiptId", id + ":" + order.orderId)
                .put("accountId", order.accountId).put("side", order.isSubscribe ? "SUBSCRIBE" : "REDEEM")
                .put("status", order.status).put("code", order.status).put("requestTimestamp", order.requestTimestamp)
                .put("expectedSettlement", order.cutoffTimestamp).put("cancellationDeadline", order.cancellationDeadline)
                .put("cashAmount", order.cashAmount.getAmount().toPlainString()).put("units", order.units.toPlainString())
                .put("reservedCash", order.pending() && order.isSubscribe ? order.cashAmount.getAmount().toPlainString() : "0")
                .put("reservedUnits", order.pending() && !order.isSubscribe ? order.units.toPlainString() : "0")
                .put("currency", "IDR").put("message", order.status.equals("SETTLED") ? "Transaksi selesai pada NAB cutoff" :
                        order.status.equals("CANCELLED") ? "Order dibatalkan" : "Order diterima; menunggu cutoff dan likuiditas");
        return result;
    }
    public synchronized void processCutoff(Map<String, Account> accounts, EconomyLedger ledger, long now) throws JSONException {
        LocalDate date = Instant.ofEpochMilli(now).atOffset(JAKARTA).toLocalDate();
        long currentCutoff = cutoff(date);
        if (!dealingDay(date) || now < currentCutoff || currentCutoff <= lastCutoffTimestamp) return;
        if (debtIssuer != null) {
            accrueDebt(ledger, currentCutoff);
            boolean redemptionPending = false;
            for (PendingFundOrder order : orders.values()) if (order.pending() && !order.isSubscribe) redemptionPending = true;
            if (redemptionPending) manageDebtLiquidity(ledger, currentCutoff);
        }
        accrueManagementFee(date.toEpochDay(), ledger, currentCutoff);
        BigDecimal nav = getNavPerUnit();
        if (nav.signum() <= 0) throw new IllegalStateException("NAB tidak positif; dealing dihentikan");
        for (PendingFundOrder order : orders.values()) {
            if (!order.pending() || order.cutoffTimestamp > currentCutoff) continue;
            Account account = accounts.get(order.accountId);
            if (account == null) { order.status = "PENDING_ACCOUNT_UNAVAILABLE"; continue; }
            if (order.isSubscribe) settleSubscription(order, account, ledger, currentCutoff, nav);
            else settleRedemption(order, account, ledger, currentCutoff, nav);
        }
        lastCutoffTimestamp = currentCutoff;
        if (debtIssuer != null) { manageDebtLiquidity(ledger, currentCutoff); lastDebtActionTimestamp = currentCutoff; }
        historicalNavs.add(new JSONObject().put("time", currentCutoff).put("nav", getNavPerUnit().toPlainString()));
        if (historicalNavs.size() > 60) historicalNavs.remove(0);
    }
    private void accrueManagementFee(long day, EconomyLedger ledger, long now) {
        if (feeAccrualDay == Long.MIN_VALUE) { feeAccrualDay = day; return; }
        long days = Math.max(0, day - feeAccrualDay);
        if (days == 0) return;
        BigDecimal fee = markAssets().getAmount().subtract(feeRemainder).multiply(MANAGEMENT_FEE_RATE)
                .multiply(BigDecimal.valueOf(days)).divide(BigDecimal.valueOf(365), 16, RoundingMode.HALF_UP).add(feeRemainder);
        // No fee can consume more than the backed pool; retained fractions remain explicit liabilities.
        fee = fee.min(markAssets().getAmount());
        BigDecimal available = poolAccount == null ? poolCash.getAmount() : poolAccount.getAvailableCash(Currency.IDR).getAmount();
        Money paid = new Money(fee.setScale(0, RoundingMode.DOWN).min(available), Currency.IDR);
        if (poolAccount != null && paid.isPositive()) ledger.recordMutation(id + ":MGMT:" + day, poolAccount,
                LedgerEntryType.MANAGEMENT_FEE, paid, false, now, "Biaya pengelolaan " + name);
        poolCash = poolCash.subtract(paid);
        managementFeesPaid = managementFeesPaid.add(paid);
        feeRemainder = fee.subtract(paid.getAmount());
        feeAccrualDay = day;
    }
    private void settleSubscription(PendingFundOrder order, Account account, EconomyLedger ledger, long now, BigDecimal nav) throws JSONException {
        if (account.getReservedCash(Currency.IDR).isLessThan(order.cashAmount)) {
            order.status = "PENDING_RESERVATION_MISMATCH"; return;
        }
        Money fee = order.cashAmount.multiply(ENTRY_FEE_RATE);
        Money net = order.cashAmount.subtract(fee);
        BigDecimal issued = net.getAmount().divide(nav, 6, RoundingMode.DOWN);
        if (issued.signum() <= 0) { order.status = "PENDING_NAV"; return; }
        account.releaseReservedCash(order.cashAmount);
        ledger.recordMutation(id + ":SUB:" + order.orderId, account, LedgerEntryType.TRADE_BUY, net, false, now, "Pembelian unit " + name);
        ledger.recordMutation(id + ":ENTRY:" + order.orderId, account, LedgerEntryType.TRANSACTION_FEE, fee, false, now, "Biaya beli unit " + name);
        holdings.put(account.getId(), getUnits(account.getId()).add(issued));
        totalUnits = totalUnits.add(issued);
        if (poolAccount != null) ledger.recordMutation(id + ":POOL-SUB:" + order.orderId, poolAccount,
                LedgerEntryType.TRADE_SELL, net, true, now, "Kas subscription pool " + name);
        poolCash = poolCash.add(net);
        order.status = "SETTLED";
        order.settlement = new JSONObject().put("navPerUnit", nav.toPlainString()).put("unitsIssued", issued.toPlainString())
                .put("entryFee", fee.format()).put("netAmount", net.format()).put("grossAmount", order.cashAmount.format())
                .put("feeRaw", fee.getAmount().toPlainString()).put("netRaw", net.getAmount().toPlainString())
                .put("settledTimestamp", now).put("totalUnitsHeld", getUnits(account.getId()).toPlainString());
    }
    private void settleRedemption(PendingFundOrder order, Account account, EconomyLedger ledger, long now, BigDecimal nav) throws JSONException {
        Money gross = order.units.compareTo(totalUnits) == 0 ? new Money(markAssets().getAmount().subtract(feeRemainder).setScale(0, RoundingMode.DOWN), Currency.IDR)
                : new Money(order.units.multiply(nav).setScale(0, RoundingMode.DOWN), Currency.IDR);
        if ((poolAccount == null ? poolCash : poolAccount.getAvailableCash(Currency.IDR)).isLessThan(gross)) { order.status = "PENDING_LIQUIDITY"; return; }
        if (getUnits(account.getId()).compareTo(order.units) < 0) { order.status = "PENDING_UNIT_MISMATCH"; return; }
        Money fee = gross.multiply(EXIT_FEE_RATE), net = gross.subtract(fee);
        if (poolAccount != null) ledger.recordMutation(id + ":POOL-RED:" + order.orderId, poolAccount,
                LedgerEntryType.TRADE_BUY, gross, false, now, "Pembayaran redemption pool " + name);
        ledger.recordMutation(id + ":RED:" + order.orderId, account, LedgerEntryType.TRADE_SELL, gross, true, now, "Pencairan unit " + name);
        ledger.recordMutation(id + ":EXIT:" + order.orderId, account, LedgerEntryType.TRANSACTION_FEE, fee, false, now, "Biaya pencairan unit " + name);
        holdings.put(account.getId(), getUnits(account.getId()).subtract(order.units));
        totalUnits = totalUnits.subtract(order.units);
        poolCash = poolCash.subtract(gross);
        if (totalUnits.signum() == 0) {
            if (poolAccount != null && poolCash.isPositive()) ledger.recordMutation(id + ":FINAL-FEE:" + order.orderId,
                    poolAccount, LedgerEntryType.MANAGEMENT_FEE, poolCash, false, now, "Penyelesaian sisa biaya pool " + name);
            managementFeesPaid = managementFeesPaid.add(poolCash);
            poolCash = Money.zero(Currency.IDR); feeRemainder = BigDecimal.ZERO;
            // The last holder bears any default write-off; a later subscriber must not inherit their old claim.
            if (debtFairValue().compareTo(BigDecimal.ONE) < 0) { debtPrincipal = BigDecimal.ZERO; debtAccrued = BigDecimal.ZERO; }
        }
        order.status = "SETTLED";
        order.settlement = new JSONObject().put("navPerUnit", nav.toPlainString()).put("unitsRedeemed", order.units.toPlainString())
                .put("grossProceeds", gross.format()).put("exitFee", fee.format()).put("netProceeds", net.format())
                .put("feeRaw", fee.getAmount().toPlainString()).put("netRaw", net.getAmount().toPlainString())
                .put("settledTimestamp", now).put("remainingUnits", getUnits(account.getId()).toPlainString());
    }
    public synchronized JSONObject toJsonObject(String accountId) throws JSONException {
        JSONObject obj = new JSONObject().put("id", id).put("name", name).put("type", type.name()).put("typeLabel", type.getLabel())
                .put("riskLevel", type.getRiskLevel()).put("managerName", managerName).put("navPerUnit", getNavPerUnit().toPlainString())
                .put("totalAum", markAssets().format()).put("totalAumRaw", markAssets().getAmount().toPlainString()).put("totalUnits", totalUnits.toPlainString())
                .put("myUnits", accountId == null ? "0" : getUnits(accountId).toPlainString())
                .put("myHoldingsValue", accountId == null ? Money.zero(Currency.IDR).format() : getHoldingsValue(accountId).format())
                .put("availableUnits", accountId == null ? "0" : getAvailableUnits(accountId).toPlainString())
                .put("reservedUnits", accountId == null ? "0" : getReservedUnits(accountId).toPlainString())
                .put("entryFee", "0.1%").put("exitFee", "0.1%").put("managementFee", "0.75% / tahun")
                .put("cutoffTime", "16:00 WIB").put("dealingCalendar", "MONDAY_FRIDAY").put("cancelDeadline", "15:30 WIB")
                .put("poolAccountId", getPoolAccountId()).put("securitiesValue", markedSecurities.getAmount().toPlainString())
                .put("backingModel", type == FundType.EQUITY_INDEX ? "CANONICAL_EQUITY_POSITIONS" :
                        type == FundType.MONEY_MARKET ? "CANONICAL_DEPOSIT_CONTRACT" : "CANONICAL_BOND_CONTRACT")
                .put("backingContract", debtContractJson());
        JSONArray mine = new JSONArray();
        for (PendingFundOrder order : orders.values()) if (order.accountId.equals(accountId)) mine.put(receipt(order));
        return obj.put("orders", mine).put("navHistory", new JSONArray(historicalNavs));
    }
    public synchronized JSONObject exportState() throws JSONException {
        JSONObject obj = new JSONObject().put("stateSchemaVersion", 4).put("canonicalPool", canonicalPool)
                .put("debtContract", debtContractJson())
                .put("markedSecurities", markAssets().getAmount().subtract(poolCash.getAmount()).toPlainString()).put("legacySponsorMigration", legacySponsorMigration)
                .put("id", id).put("name", name).put("type", type.name()).put("managerName", managerName)
                .put("navPerUnit", getNavPerUnit().toPlainString()).put("totalUnits", totalUnits.toPlainString())
                .put("poolCash", poolCash.getAmount().toPlainString()).put("feeRemainder", feeRemainder.toPlainString())
                .put("managementFeesPaid", managementFeesPaid.getAmount().toPlainString())
                .put("lastCutoffTimestamp", lastCutoffTimestamp).put("feeAccrualDay", feeAccrualDay);
        JSONObject positions = new JSONObject();
        for (Map.Entry<String, BigDecimal> entry : holdings.entrySet()) positions.put(entry.getKey(), entry.getValue().toPlainString());
        JSONArray savedOrders = new JSONArray();
        for (PendingFundOrder order : orders.values()) {
            JSONObject saved = receipt(order);
            if (order.settlement != null) saved.put("settlement", new JSONObject(order.settlement.toString()));
            savedOrders.put(saved);
        }
        return obj.put("holdings", positions).put("orders", savedOrders).put("navHistory", new JSONArray(historicalNavs));
    }
    public synchronized void restoreState(JSONObject obj) throws JSONException {
        MutualFund staged = new MutualFund(id, name, type, managerName);
        staged.restoreValidated(obj);
        debtPrincipal = staged.debtPrincipal; debtAccrued = staged.debtAccrued;
        debtPriceFactor = staged.debtPriceFactor; debtAccrualDay = staged.debtAccrualDay;
        lastDebtActionTimestamp = staged.lastDebtActionTimestamp; debtIssuer = null;
        canonicalPool = staged.canonicalPool; markedSecurities = staged.markedSecurities; poolAccount = null; valuationCatalog = null;
        totalUnits = staged.totalUnits; poolCash = staged.poolCash; feeRemainder = staged.feeRemainder;
        managementFeesPaid = staged.managementFeesPaid; lastCutoffTimestamp = staged.lastCutoffTimestamp;
        feeAccrualDay = staged.feeAccrualDay; legacySponsorMigration = staged.legacySponsorMigration;
        holdings.clear(); holdings.putAll(staged.holdings);
        orders.clear(); orders.putAll(staged.orders);
        historicalNavs.clear(); historicalNavs.addAll(staged.historicalNavs);
    }
    private static BigDecimal decimal(JSONObject obj, String key, int scale) throws JSONException {
        return new BigDecimal(obj.getString(key)).setScale(scale, RoundingMode.UNNECESSARY);
    }
    private JSONObject debtContractJson() throws JSONException {
        return new JSONObject().put("issuerAccountId", getDebtIssuerAccountId()).put("currency", "IDR")
                .put("principal", debtPrincipal.toPlainString()).put("accruedInterest", debtAccrued.toPlainString())
                .put("issuerLiability", debtPrincipal.add(debtAccrued).toPlainString())
                .put("couponAnnual", type == FundType.MONEY_MARKET ? "0.05" : "0.08")
                .put("priceFactor", debtPriceFactor.toPlainString()).put("accrualDay", debtAccrualDay)
                .put("lastActionTimestamp", lastDebtActionTimestamp)
                .put("fairValue", debtFairValue().setScale(0, RoundingMode.DOWN).toPlainString())
                .put("risk", "Risiko penerbit, likuiditas, dan perubahan yield; kupon tidak dijamin");
    }
    private void restoreValidated(JSONObject obj) throws JSONException {
        if (obj == null) throw new IllegalArgumentException("State reksa dana kosong");
        boolean legacy = !obj.has("stateSchemaVersion");
        if (!legacy && (obj.getInt("stateSchemaVersion") < 2 || obj.getInt("stateSchemaVersion") > 4))
            throw new IllegalArgumentException("Versi state reksa dana tidak didukung");
        if (!legacy && obj.getInt("stateSchemaVersion") == 4) {
            JSONObject debt = obj.getJSONObject("debtContract");
            debtPrincipal = decimal(debt, "principal", 0);
            debtAccrued = new BigDecimal(debt.getString("accruedInterest"));
            debtPriceFactor = new BigDecimal(debt.getString("priceFactor"));
            debtAccrualDay = debt.getLong("accrualDay");
            lastDebtActionTimestamp = debt.getLong("lastActionTimestamp");
            if (!getDebtIssuerAccountId().equals(debt.getString("issuerAccountId")) || debtPrincipal.signum() < 0
                    || debtAccrued.signum() < 0 || debtPriceFactor.compareTo(new BigDecimal("0.7")) < 0
                    || debtPriceFactor.compareTo(new BigDecimal("1.1")) > 0
                    || new BigDecimal(debt.getString("issuerLiability")).compareTo(debtPrincipal.add(debtAccrued)) != 0
                    || (debtAccrualDay != Long.MIN_VALUE && (debtAccrualDay < 0 || debtAccrualDay > LocalDate.MAX.toEpochDay()))
                    || (lastDebtActionTimestamp != Long.MIN_VALUE && lastDebtActionTimestamp < 0)
                    || (type == FundType.EQUITY_INDEX && (debtPrincipal.signum() != 0 || debtAccrued.signum() != 0)))
                throw new IllegalArgumentException("Kontrak backing reksa dana tidak sah");
        }
        if (!id.equals(obj.getString("id")) || !name.equals(obj.getString("name"))
                || !type.name().equals(obj.getString("type")) || !managerName.equals(obj.getString("managerName")))
            throw new IllegalArgumentException("Identitas katalog reksa dana berbeda");
        // Only the original schema can migrate unregistered seed units. Missing modern metadata is corruption.
        if (legacy && (obj.has("orders") || obj.has("feeAccrualDay") || obj.has("lastCutoffTimestamp")
                || obj.has("managementFeesPaid") || obj.has("feeRemainder") || obj.has("navHistory") || obj.has("legacySponsorMigration")))
            throw new IllegalArgumentException("Metadata state modern tanpa versi");
        canonicalPool = !legacy && obj.optBoolean("canonicalPool", false);
        markedSecurities = new Money(new BigDecimal(obj.optString("markedSecurities", "0")), Currency.IDR);
        if (markedSecurities.isNegative()) throw new IllegalArgumentException("Valuasi aset pool negatif");
        totalUnits = decimal(obj, "totalUnits", 6);
        poolCash = new Money(decimal(obj, "poolCash", 0), Currency.IDR);
        feeRemainder = legacy ? BigDecimal.ZERO : new BigDecimal(obj.getString("feeRemainder"));
        managementFeesPaid = legacy ? Money.zero(Currency.IDR) : new Money(decimal(obj, "managementFeesPaid", 0), Currency.IDR);
        if (totalUnits.signum() < 0 || poolCash.isNegative() || feeRemainder.signum() < 0
                || (!canonicalPool && feeRemainder.compareTo(BigDecimal.ONE) >= 0) || feeRemainder.compareTo(poolCash.add(markedSecurities).getAmount()) > 0
                || managementFeesPaid.isNegative() || (totalUnits.signum() > 0 && poolCash.add(markedSecurities).getAmount().compareTo(feeRemainder) <= 0)
                || (totalUnits.signum() == 0 && (!poolCash.isZero() || !markedSecurities.isZero() || feeRemainder.signum() != 0)))
            throw new IllegalArgumentException("State pool reksa dana tidak sah");
        lastCutoffTimestamp = legacy ? Long.MIN_VALUE : obj.getLong("lastCutoffTimestamp");
        feeAccrualDay = legacy ? Long.MIN_VALUE : obj.getLong("feeAccrualDay");
        if (feeAccrualDay != Long.MIN_VALUE && (feeAccrualDay < 0 || feeAccrualDay > LocalDate.MAX.toEpochDay()))
            throw new IllegalArgumentException("Anchor biaya reksa dana tidak sah");
        if (lastCutoffTimestamp != Long.MIN_VALUE) {
            LocalDate lastDate = Instant.ofEpochMilli(lastCutoffTimestamp).atOffset(JAKARTA).toLocalDate();
            if (lastCutoffTimestamp < 0 || !dealingDay(lastDate) || lastCutoffTimestamp != cutoff(lastDate)
                    || feeAccrualDay != lastDate.toEpochDay()) throw new IllegalArgumentException("Anchor cutoff reksa dana tidak sah");
        }
        JSONObject savedHoldings = obj.getJSONObject("holdings");
        BigDecimal registered = BigDecimal.ZERO;
        Iterator<String> keys = savedHoldings.keys();
        while (keys.hasNext()) {
            String key = keys.next(); validateId(key);
            BigDecimal value = decimal(savedHoldings, key, 6);
            if (value.signum() < 0) throw new IllegalArgumentException("Unit pemegang tidak sah");
            holdings.put(key, value); registered = registered.add(value);
        }
        if (registered.compareTo(totalUnits) > 0 || (!legacy && registered.compareTo(totalUnits) != 0))
            throw new IllegalArgumentException("Register unit tidak cocok dengan unit beredar");
        legacySponsorMigration = !legacy && obj.getBoolean("legacySponsorMigration");
        String sponsor = "LEGACY-SPONSOR:" + id;
        for (String accountId : holdings.keySet()) if (accountId.startsWith("LEGACY-SPONSOR:")
                && (!accountId.equals(sponsor) || !legacySponsorMigration)) throw new IllegalArgumentException("Identitas sponsor legacy tidak sah");
        if (legacy && registered.compareTo(totalUnits) < 0) {
            legacySponsorMigration = true;
            holdings.put(sponsor, totalUnits.subtract(registered));
        }
        if (!legacy && new BigDecimal(obj.getString("navPerUnit")).compareTo(getNavPerUnit()) != 0)
            throw new IllegalArgumentException("NAB tersimpan tidak sesuai aset backing");
        JSONArray savedOrders = legacy ? new JSONArray() : obj.getJSONArray("orders");
        Set<String> allowedStatuses = new HashSet<>(Arrays.asList("ACCEPTED", "PENDING_ACCOUNT_UNAVAILABLE", "PENDING_RESERVATION_MISMATCH",
                "PENDING_NAV", "PENDING_LIQUIDITY", "PENDING_UNIT_MISMATCH", "SETTLED", "CANCELLED"));
        for (int i = 0; i < savedOrders.length(); i++) {
            JSONObject saved = savedOrders.getJSONObject(i);
            String orderId = saved.getString("orderId"), accountId = saved.getString("accountId");
            validateId(orderId); validateId(accountId);
            if (orders.containsKey(orderId)) throw new IllegalArgumentException("Duplikasi order tersimpan");
            String side = saved.getString("side");
            if (!side.equals("SUBSCRIBE") && !side.equals("REDEEM")) throw new IllegalArgumentException("Sisi order tidak sah");
            boolean subscribe = side.equals("SUBSCRIBE");
            PendingFundOrder order = new PendingFundOrder(orderId, accountId, subscribe,
                    new Money(decimal(saved, "cashAmount", 0), Currency.IDR), decimal(saved, "units", 6),
                    saved.getLong("requestTimestamp"), saved.getLong("expectedSettlement"), saved.getLong("cancellationDeadline"));
            order.status = saved.getString("status");
            JSONObject savedSettlement = saved.optJSONObject("settlement");
            order.settlement = savedSettlement == null ? null : new JSONObject(savedSettlement.toString());
            if (!allowedStatuses.contains(order.status) || order.requestTimestamp < 0
                    || (subscribe ? order.cashAmount.isLessThan(Money.of(10_000, Currency.IDR)) || order.units.signum() != 0
                    : order.units.signum() <= 0 || !order.cashAmount.isZero()))
                throw new IllegalArgumentException("State order reksa dana tidak sah");
            // Temporarily ignore the latest cutoff when reconstructing the request's original dealing date.
            long originalAnchor = lastCutoffTimestamp; lastCutoffTimestamp = Long.MIN_VALUE;
            long expectedCutoff = nextCutoff(order.requestTimestamp); lastCutoffTimestamp = originalAnchor;
            if (order.cutoffTimestamp != expectedCutoff || order.cancellationDeadline != expectedCutoff - 30 * 60 * 1000L
                    || (feeAccrualDay == Long.MIN_VALUE)) throw new IllegalArgumentException("Jadwal order reksa dana tidak sah");
            if (order.status.equals("SETTLED")) {
                if (order.settlement == null || lastCutoffTimestamp == Long.MIN_VALUE)
                    throw new IllegalArgumentException("Receipt settlement tidak lengkap");
                long settled = order.settlement.getLong("settledTimestamp");
                LocalDate settledDay = Instant.ofEpochMilli(settled).atOffset(JAKARTA).toLocalDate();
                if (settled < order.cutoffTimestamp || settled > lastCutoffTimestamp || settled != cutoff(settledDay) || !dealingDay(settledDay))
                    throw new IllegalArgumentException("Timestamp settlement tidak sah");
                if (new BigDecimal(order.settlement.getString("navPerUnit")).signum() <= 0
                        || new BigDecimal(order.settlement.getString("feeRaw")).signum() < 0
                        || new BigDecimal(order.settlement.getString("netRaw")).signum() < 0)
                    throw new IllegalArgumentException("Nominal receipt settlement tidak sah");
            } else if (order.settlement != null) throw new IllegalArgumentException("Order belum settled mempunyai receipt settlement");
            orders.put(orderId, order);
        }
        if (lastCutoffTimestamp == Long.MIN_VALUE) {
            long earliestRequestDay = Long.MAX_VALUE;
            for (PendingFundOrder order : orders.values()) earliestRequestDay = Math.min(earliestRequestDay,
                    Instant.ofEpochMilli(order.requestTimestamp).atOffset(JAKARTA).toLocalDate().toEpochDay());
            long expectedAnchor = earliestRequestDay == Long.MAX_VALUE ? Long.MIN_VALUE : earliestRequestDay;
            if (feeAccrualDay != expectedAnchor) throw new IllegalArgumentException("Anchor biaya tidak sesuai riwayat permintaan");
        }
        // Every pending redemption must resolve to an existing holder, including orphan account IDs.
        for (PendingFundOrder order : orders.values()) if (order.pending() && !order.isSubscribe
                && (!holdings.containsKey(order.accountId) || getAvailableUnits(order.accountId).signum() < 0))
            throw new IllegalArgumentException("Reservasi unit melebihi kepemilikan");
        JSONArray history = legacy ? new JSONArray() : obj.getJSONArray("navHistory");
        long previous = Long.MIN_VALUE;
        for (int i = 0; i < history.length(); i++) {
            JSONObject point = history.getJSONObject(i);
            long timestamp = point.getLong("time");
            LocalDate date = Instant.ofEpochMilli(timestamp).atOffset(JAKARTA).toLocalDate();
            if (timestamp < 0 || timestamp <= previous || timestamp > lastCutoffTimestamp || !dealingDay(date)
                    || timestamp != cutoff(date) || new BigDecimal(point.getString("nav")).signum() <= 0)
                throw new IllegalArgumentException("Riwayat NAB tidak sah");
            previous = timestamp;
            if (i >= history.length() - 60) historicalNavs.add(new JSONObject(point.toString()));
        }
    }
}
