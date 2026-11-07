package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.HistoricalDatasetLoader;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.MatchingEngine;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;
import com.tradingglobal3d.core.trading.OrderStatus;
import com.tradingglobal3d.core.trading.OrderType;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.file.Files;

public class PackageP07ReplaySecurityTest {

    @Test
    public void testAU31_ReplayOrderCannotFillOnUnobservedDayLowBeforeClose() throws Exception {
        HistoricalDatasetLoader.setAssetSource(path -> new FileInputStream(new File("android/app/src/main/assets/" + path)));
        File testDir = new File("build/tmp-test-p07-au31");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));

        // Start historical replay on AAPL
        JSONObject replayInit = facade.startHistoricalReplay("US:AAPL", new BigDecimal("50000000"));
        Assert.assertTrue(replayInit.getBoolean("success"));

        // Current time is 09:00:00 (market open).
        // First bar for AAPL on 2016-05-09: Open is 93.00, Low is 92.57.
        Instrument aapl = facade.getCatalog().getById("US:AAPL");
        Account player = facade.getPersonalAccount();

        // Submit BUY limit order @ $92.57 (the day's eventual low, which is below the opening price of $93.00)
        Order limitOrder = new Order(
                "ORD-REPLAY-LIMIT", "REQ-REPLAY-LIMIT",
                player.getId(), aapl.getId(),
                OrderSide.BUY, OrderType.LIMIT,
                new Money(new BigDecimal("92.57"), Currency.USD), null,
                1L, facade.getClock().getGameTimestamp(),
                new Money(new BigDecimal("92.57"), Currency.USD)
        );

        MatchingEngine.OrderResult result = facade.submitOrder(limitOrder);

        // AU31: Order MUST NOT be filled at 09:00 because day low has not occurred yet!
        Assert.assertNotEquals("Order must NOT be filled based on future daily low!", OrderStatus.FILLED, limitOrder.getStatus());
        Assert.assertEquals("Order must remain OPEN/waiting", OrderStatus.OPEN, limitOrder.getStatus());
    }

    @Test
    public void testAU32_StartHistoricalReplayCreditsUsdOnceAndKeepsLedgerReconciled() throws Exception {
        HistoricalDatasetLoader.setAssetSource(path -> new FileInputStream(new File("android/app/src/main/assets/" + path)));
        File testDir = new File("build/tmp-test-p07-au32");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));

        BigDecimal initialIdr = new BigDecimal("50000000");
        JSONObject replayInit = facade.startHistoricalReplay("US:AAPL", initialIdr);
        Assert.assertTrue(replayInit.getBoolean("success"));

        Account player = facade.getPersonalAccount();
        BigDecimal usdRate = facade.getFxService().getMidRate();
        BigDecimal expectedUsd = initialIdr.divide(usdRate, 2, java.math.RoundingMode.HALF_UP);

        Money actualUsd = player.getTotalCash(Currency.USD);
        System.out.println("AU32 - Expected USD: " + expectedUsd + ", Actual USD: " + actualUsd.getAmount());

        // AU32: USD must be credited exactly once, not twice!
        Assert.assertEquals("USD cash must be credited exactly once!", expectedUsd, actualUsd.getAmount());

        // Reconciliation check
        for (Currency cur : Currency.values()) {
            Assert.assertTrue("Account cash must reconcile with ledger for " + cur,
                    facade.getLedger().reconcile(player, cur));
        }
    }
}
