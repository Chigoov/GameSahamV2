package com.tradingglobal3d.core;

import com.tradingglobal3d.core.replay.StrategyLabSandbox;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import org.json.JSONObject;
import org.junit.Test;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class FeatureCompletionLabTest {
    private Instrument instrument(MarketExchange exchange) {
        return new Instrument(exchange.name() + ":SAME", "SAME", "QA", exchange, "QA", exchange.getBaseCurrency(), 1, 100000, true, Money.of(100, exchange.getBaseCurrency()));
    }
    private Account account() {
        Account account = new Account("PERSONAL", "QA", AccountType.PERSONAL, null);
        account.creditCash(Money.of(10_000_000, Currency.IDR)); account.creditCash(Money.of(10000, Currency.USD));
        return account;
    }

    @Test public void decisionReplayAndExportRestoreAreDeterministicAndMainAccountIsUntouched() throws Exception {
        Account main = account();
        StrategyLabSandbox lab = new StrategyLabSandbox("QA", main, 100, BigDecimal.valueOf(16000));
        lab.executeDecision("BUY", instrument(MarketExchange.US), 10, BigDecimal.valueOf(100), 101);
        lab.executeDecision("SELL", instrument(MarketExchange.US), 10, BigDecimal.valueOf(110), 200);
        // $100 gross profit - $1.50 buy fee - $2.75 sell fee, in IDR at the sandbox's opening FX.
        assertEquals(0, BigDecimal.valueOf(1_532_000).compareTo(lab.getSimulatedRealizedPnL()));
        String history = lab.getDecisionHistory().toString();
        Money cash = lab.getClonedAccount().getTotalCash(Currency.USD);
        lab.replayDecisions();
        assertEquals(history, lab.getDecisionHistory().toString()); assertEquals(cash, lab.getClonedAccount().getTotalCash(Currency.USD));
        StrategyLabSandbox restored = StrategyLabSandbox.fromState(new JSONObject(lab.exportState().toString()));
        assertEquals(cash, restored.getClonedAccount().getTotalCash(Currency.USD));
        assertEquals(lab.getSimulatedRealizedPnL(), restored.getSimulatedRealizedPnL());
        restored.resetDecisions(); assertEquals(0, restored.getSimulatedTradesCount());
        assertEquals(Money.of(10000, Currency.USD), restored.getClonedAccount().getTotalCash(Currency.USD));
        assertEquals(Money.of(10000, Currency.USD), main.getTotalCash(Currency.USD)); assertTrue(main.getPositions().isEmpty());
    }

    @Test public void invalidInputsCannotMutateLabAndReservedCashIsPreserved() throws Exception {
        Account main = account(); main.reserveCash(Money.of(10000, Currency.USD));
        StrategyLabSandbox lab = new StrategyLabSandbox("QA", main, 100, BigDecimal.valueOf(16000));
        String before = lab.exportState().toString();
        for (long shares : new long[]{0, -1}) {
            try { lab.simulateBuy(instrument(MarketExchange.IDX), shares, BigDecimal.TEN); fail(); } catch (IllegalArgumentException expected) { }
            try { lab.simulateSell(instrument(MarketExchange.IDX), shares, BigDecimal.TEN); fail(); } catch (IllegalArgumentException expected) { }
        }
        try { lab.simulateBuy(instrument(MarketExchange.IDX), 1, BigDecimal.valueOf(-1)); fail(); } catch (IllegalArgumentException expected) { }
        try { lab.executeDecision("BUY", instrument(MarketExchange.IDX), 1, BigDecimal.TEN, 99); fail(); } catch (IllegalArgumentException expected) { }
        assertFalse(lab.simulateBuy(instrument(MarketExchange.US), 1, BigDecimal.TEN));
        assertEquals(before, lab.exportState().toString());
    }

    @Test public void canonicalInstrumentPricesPreventCrossExchangeTickerCollision() {
        Account main = account();
        main.addPosition(new Position("SAME", "IDX", Currency.IDR, 100, BigDecimal.TEN, BigDecimal.TEN));
        main.addPosition(new Position("SAME", "US", Currency.USD, 1, BigDecimal.TEN, BigDecimal.TEN));
        StrategyLabSandbox lab = new StrategyLabSandbox("QA", main);
        Map<String, BigDecimal> prices = new HashMap<>(); prices.put("IDX:SAME", BigDecimal.valueOf(20)); prices.put("US:SAME", BigDecimal.valueOf(30));
        assertEquals(Money.of(170_482_000, Currency.IDR), lab.calculateSandboxTotalEquity(prices, BigDecimal.valueOf(16000)));
        prices.clear(); prices.put("SAME", BigDecimal.valueOf(20));
        try { lab.calculateSandboxTotalEquity(prices, BigDecimal.valueOf(16000)); fail(); } catch (IllegalArgumentException expected) { }
    }
}
