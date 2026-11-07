package com.tradingglobal3d.core;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.storage.SaveState;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PositionSavePrecisionTest {
    @Test public void repeatedSaveReloadPreservesFractionalDollarProfitAndLoss() throws Exception {
        for (String executionPrice : List.of("100.0060", "100.0000")) {
            var uninterrupted = new Position("QA", "US", Currency.USD, 10, new BigDecimal("100.0030"), new BigDecimal(executionPrice));
            var resumed = uninterrupted.copy();
            for (int sale = 0; sale < 10; sale++) {
                uninterrupted.addSell(1, new BigDecimal(executionPrice));
                resumed.addSell(1, new BigDecimal(executionPrice));
                var account = new Account("player", "QA", AccountType.PERSONAL, null);
                account.addPosition(resumed);
                var state = SaveState.capture(GameMode.INTERNAL_MARKET, GameClock.createDefault(),
                        List.of(account), new EconomyLedger());
                resumed = SaveState.fromJsonString(state.toJsonString()).getAccounts().get(0).getPosition("US:QA");
            }
            assertEquals("PnL changed across ten checkpoints at " + executionPrice,
                    uninterrupted.getRealizedPnL(), resumed.getRealizedPnL());
        }
    }
}
