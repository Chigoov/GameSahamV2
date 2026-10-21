package com.tradingglobal3d.core.replay;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** Isolated what-if account and deterministic replay of its own decisions, never the main ledger. */
public final class StrategyLabSandbox {
    private final String sandboxId;
    private final Account initialAccount;
    private Account clonedAccount;
    private final EconomyLedger sandboxLedger = new EconomyLedger();
    private final JSONArray decisions = new JSONArray();
    private final long initialTimestamp;
    private long lastTimestamp;
    private final BigDecimal usdIdr;
    private int simulatedTradesCount;
    private BigDecimal simulatedRealizedPnL = BigDecimal.ZERO;

    public StrategyLabSandbox(String sandboxId, Account sourceAccount) {
        // ponytail: legacy callers use a deterministic baseline; production passes its canonical game clock/FX.
        this(sandboxId, sourceAccount, 0, BigDecimal.valueOf(15800));
    }

    public StrategyLabSandbox(String sandboxId, Account sourceAccount, long gameTimestamp, BigDecimal usdIdr) {
        if (sandboxId == null || sandboxId.trim().isEmpty() || sandboxId.length() > 160 || gameTimestamp < 0
                || usdIdr == null || usdIdr.signum() <= 0) throw new IllegalArgumentException("Identitas, waktu, dan kurs lab wajib valid");
        this.sandboxId = sandboxId;
        this.initialTimestamp = this.lastTimestamp = gameTimestamp;
        this.usdIdr = usdIdr;
        this.initialAccount = copyAccount(Objects.requireNonNull(sourceAccount));
        this.clonedAccount = copyAccount(initialAccount);
    }

    private Account copyAccount(Account source) {
        Account clone = new Account("SANDBOX-" + sandboxId, "Lab", AccountType.PERSONAL, null);
        for (Currency currency : Currency.values()) {
            clone.creditCash(source.getTotalCash(currency));
            clone.reserveCash(source.getReservedCash(currency));
        }
        for (Position position : source.getPositions().values()) clone.addPosition(new Position(position.getTicker(), position.getExchange(),
                position.getCurrency(), position.getQuantity(), position.getAverageCost(), position.getCurrentPrice(),
                position.getRealizedPnL() == null ? null : position.getRealizedPnL().getAmount(), position.getAcquisitionFeesRemaining(), position.getAcquisitionFeeQuantity()));
        return clone;
    }

    public String getSandboxId() { return sandboxId; }
    public synchronized Account getClonedAccount() { return clonedAccount; }
    public synchronized int getSimulatedTradesCount() { return simulatedTradesCount; }
    /** All P/L is IDR at this sandbox's fixed opening FX; FX itself is held constant for replay. */
    public synchronized BigDecimal getSimulatedRealizedPnL() { return simulatedRealizedPnL; }

    public synchronized boolean simulateBuy(Instrument instrument, long shares, BigDecimal price) {
        return simulate("BUY", instrument, shares, price, Math.addExact(lastTimestamp, 1));
    }

    public synchronized boolean simulateSell(Instrument instrument, long shares, BigDecimal price) {
        return simulate("SELL", instrument, shares, price, Math.addExact(lastTimestamp, 1));
    }

    private boolean simulate(String action, Instrument instrument, long shares, BigDecimal price, long timestamp) {
        if (!("BUY".equals(action) || "SELL".equals(action)) || instrument == null || shares <= 0 || price == null
                || price.signum() <= 0 || timestamp < lastTimestamp
                || new Money(price, instrument.getCurrency()).getAmount().compareTo(price) != 0)
            throw new IllegalArgumentException("Aksi lab memerlukan jumlah/harga positif dan waktu game berurutan");
        boolean buy = action.equals("BUY");
        BigDecimal gross = price.multiply(BigDecimal.valueOf(shares));
        Money fee = new Money(gross.multiply(new BigDecimal(buy ? "0.0015" : "0.0025")), instrument.getCurrency());
        Money cash = new Money(gross, instrument.getCurrency());
        Position position = clonedAccount.getPosition(instrument.getId());
        if (buy) {
            Money required = cash.add(fee);
            if (clonedAccount.getAvailableCash(instrument.getCurrency()).isLessThan(required)) return false;
            if (position != null) Math.addExact(position.getQuantity(), shares);
            sandboxLedger.recordMutation("LAB-" + sandboxId + "-" + decisions.length(), clonedAccount,
                    LedgerEntryType.TRADE_BUY, required, false, timestamp, "Lab beli " + instrument.getId());
            if (position == null) {
                position = new Position(instrument.getTicker(), instrument.getExchange().name(), instrument.getCurrency(), 0, BigDecimal.ZERO, price);
                clonedAccount.addPosition(position);
            }
            position.addBuy(shares, price, fee);
            clonedAccount.getPosition(instrument.getId()).updateMarketPrice(price);
        } else {
            if (position == null || position.getQuantity() < shares) return false;
            Money netProceeds = cash.subtract(fee);
            Money allocatedBuyFee = position.previewAcquisitionFeeAllocation(shares);
            Money grossRealized = position.addSell(shares, price);
            sandboxLedger.recordMutation("LAB-" + sandboxId + "-" + decisions.length(), clonedAccount,
                    LedgerEntryType.TRADE_SELL, netProceeds, true, timestamp, "Lab jual " + instrument.getId());
            BigDecimal realized = grossRealized.subtract(fee).subtract(allocatedBuyFee).getAmount();
            simulatedRealizedPnL = simulatedRealizedPnL.add(realized.multiply(instrument.getCurrency() == Currency.USD ? usdIdr : BigDecimal.ONE));
        }
        try {
            decisions.put(new JSONObject().put("action", action).put("instrumentId", instrument.getId()).put("ticker", instrument.getTicker())
                    .put("exchange", instrument.getExchange().name()).put("currency", instrument.getCurrency().name())
                    .put("shares", shares).put("price", price.toPlainString()).put("timestamp", timestamp).put("fee", fee.getAmount().toPlainString()));
        } catch (JSONException impossible) { throw new IllegalStateException("Gagal mencatat keputusan lab", impossible); }
        simulatedTradesCount++; lastTimestamp = timestamp;
        return true;
    }

    public synchronized JSONObject executeDecision(String action, Instrument instrument, long shares, BigDecimal price, long timestamp) throws JSONException {
        boolean success = simulate(action, instrument, shares, price, timestamp);
        return new JSONObject().put("success", success).put("sandboxId", sandboxId).put("status", success ? "EXECUTED" : "REJECTED")
                .put("reason", success ? "" : "Kas/posisi lab tidak mencukupi")
                .put("trades", simulatedTradesCount).put("realizedNetPnLIdr", simulatedRealizedPnL.toPlainString());
    }

    public synchronized JSONArray getDecisionHistory() throws JSONException { return new JSONArray(decisions.toString()); }

    public synchronized JSONObject exportState() throws JSONException {
        JSONObject cash = new JSONObject(), reserved = new JSONObject();
        for (Currency currency : Currency.values()) {
            cash.put(currency.name(), initialAccount.getTotalCash(currency).getAmount().toPlainString());
            reserved.put(currency.name(), initialAccount.getReservedCash(currency).getAmount().toPlainString());
        }
        JSONArray positions = new JSONArray();
        for (Position position : initialAccount.getPositions().values()) positions.put(new JSONObject().put("ticker", position.getTicker())
                .put("exchange", position.getExchange()).put("currency", position.getCurrency().name()).put("quantity", position.getQuantity())
                .put("averageCost", position.getAverageCost().toPlainString()).put("currentPrice", position.getCurrentPrice().toPlainString())
                .put("realizedPnL", position.getRealizedPnL() == null ? JSONObject.NULL : position.getRealizedPnL().getAmount().toPlainString())
                .put("acquisitionFeesRemaining", position.getAcquisitionFeesRemaining().getAmount().toPlainString())
                .put("acquisitionFeeQuantity", position.getAcquisitionFeeQuantity()));
        return new JSONObject().put("sandboxId", sandboxId).put("initialTimestamp", initialTimestamp).put("fxUsdIdr", usdIdr.toPlainString())
                .put("initialCash", cash).put("initialReservedCash", reserved).put("initialPositions", positions).put("decisions", getDecisionHistory());
    }

    public static StrategyLabSandbox fromState(JSONObject state) throws JSONException {
        Account account = new Account("LAB-INITIAL", "Lab initial", AccountType.PERSONAL, null);
        for (Currency currency : Currency.values()) {
            account.creditCash(new Money(new BigDecimal(state.getJSONObject("initialCash").getString(currency.name())), currency));
            account.reserveCash(new Money(new BigDecimal(state.getJSONObject("initialReservedCash").getString(currency.name())), currency));
        }
        JSONArray positions = state.getJSONArray("initialPositions");
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < positions.length(); i++) {
            JSONObject p = positions.getJSONObject(i);
            if (!ids.add(p.getString("exchange") + ":" + p.getString("ticker")) || p.getLong("quantity") < 0
                    || new BigDecimal(p.getString("averageCost")).signum() < 0 || new BigDecimal(p.getString("currentPrice")).signum() <= 0)
                throw new IllegalArgumentException("Posisi awal lab tidak valid");
            account.addPosition(new Position(p.getString("ticker"), p.getString("exchange"), Currency.valueOf(p.getString("currency")),
                    p.getLong("quantity"), new BigDecimal(p.getString("averageCost")), new BigDecimal(p.getString("currentPrice")),
                    p.isNull("realizedPnL") ? null : new BigDecimal(p.getString("realizedPnL")),
                    new Money(new BigDecimal(p.optString("acquisitionFeesRemaining", "0")), Currency.valueOf(p.getString("currency"))),
                    p.optLong("acquisitionFeeQuantity", p.getLong("quantity"))));
        }
        StrategyLabSandbox restored = new StrategyLabSandbox(state.getString("sandboxId"), account, state.getLong("initialTimestamp"), new BigDecimal(state.getString("fxUsdIdr")));
        JSONArray decisions = state.getJSONArray("decisions");
        for (int i = 0; i < decisions.length(); i++) restored.decisions.put(new JSONObject(decisions.getJSONObject(i).toString()));
        restored.replayDecisions();
        return restored;
    }

    public synchronized void resetDecisions() {
        clonedAccount = copyAccount(initialAccount); sandboxLedger.clear();
        while (decisions.length() > 0) decisions.remove(decisions.length() - 1);
        simulatedTradesCount = 0; simulatedRealizedPnL = BigDecimal.ZERO; lastTimestamp = initialTimestamp;
    }

    public synchronized JSONObject replayDecisions() throws JSONException {
        JSONArray recorded = getDecisionHistory();
        resetDecisions();
        for (int i = 0; i < recorded.length(); i++) {
            JSONObject decision = recorded.getJSONObject(i);
            Currency currency = Currency.valueOf(decision.getString("currency"));
            BigDecimal price = new BigDecimal(decision.getString("price"));
            Instrument instrument = new Instrument(decision.getString("instrumentId"), decision.getString("ticker"), "Lab replay",
                    MarketExchange.valueOf(decision.getString("exchange")), "Lab", currency, 1, 0, true, new Money(price, currency));
            if (!simulate(decision.getString("action"), instrument, decision.getLong("shares"), price, decision.getLong("timestamp")))
                throw new IllegalStateException("Keputusan lab tidak dapat direproduksi");
        }
        return new JSONObject().put("success", true).put("sandboxId", sandboxId).put("replayed", decisions.length())
                .put("realizedNetPnLIdr", simulatedRealizedPnL.toPlainString()).put("fxUsdIdr", usdIdr.toPlainString())
                .put("scope", "ISOLATED_LAB_DECISIONS");
    }

    public synchronized Money calculateSandboxTotalEquity(Map<String, BigDecimal> marketPrices, BigDecimal usdRate) {
        if (marketPrices == null || usdRate == null || usdRate.signum() <= 0) throw new IllegalArgumentException("Harga dan kurs lab tidak valid");
        BigDecimal total = clonedAccount.getTotalCash(Currency.IDR).getAmount().add(clonedAccount.getTotalCash(Currency.USD).getAmount().multiply(usdRate));
        for (Position position : clonedAccount.getPositions().values()) if (position.getQuantity() > 0) {
            String id = position.getExchange() + ":" + position.getTicker();
            BigDecimal price = marketPrices.get(id);
            // Legacy ticker lookup is accepted only when that ticker is unique among owned positions.
            if (price == null && marketPrices.containsKey(position.getTicker())) {
                if (clonedAccount.getPositions().values().stream().filter(p -> p.getQuantity() > 0 && p.getTicker().equals(position.getTicker())).count() != 1)
                    throw new IllegalArgumentException("Ticker lintas bursa ambigu; gunakan exchange:ticker");
                price = marketPrices.get(position.getTicker());
            }
            if (price == null) price = position.getCurrentPrice();
            if (price.signum() <= 0) throw new IllegalArgumentException("Harga valuasi lab harus positif");
            BigDecimal value = price.multiply(BigDecimal.valueOf(position.getQuantity()));
            total = total.add(value.multiply(position.getCurrency() == Currency.USD ? usdRate : BigDecimal.ONE));
        }
        return new Money(total, Currency.IDR);
    }
}
