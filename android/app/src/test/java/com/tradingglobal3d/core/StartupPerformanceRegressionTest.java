package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.npc.MarketEngine;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import org.junit.Test;
import java.math.BigDecimal;
import java.nio.file.*;
import static org.junit.Assert.*;

public class StartupPerformanceRegressionTest {
    @Test public void requestLookupSurvivesRestoreCancellationAndReset() throws Exception {
        InstrumentCatalog catalog = InstrumentCatalog.fromJsonString(Files.readString(Path.of("src/main/assets/data/market_universe.json")));
        OrderManager orders = new OrderManager();
        Order trigger = new Order("trigger", "request", "owner", "IDX:BBCA", OrderSide.SELL,
                OrderType.STOP_LOSS, null, Money.of(9000, Currency.IDR), 100, 1, null);
        orders.registerOrder(trigger);
        assertSame(trigger, orders.getOrderByRequest("request"));
        orders.restoreState(orders.exportState(), catalog, new MarketEngine());
        assertEquals(1, orders.getAllOrders().size());
        assertTrue(orders.cancelPendingTrigger("request"));
        assertEquals(OrderStatus.CANCELLED, orders.getOrderByRequest("request").getStatus());
        assertFalse(orders.cancelPendingTrigger("request"));
        orders.restoreState(new org.json.JSONArray(), catalog, new MarketEngine());
        assertNull(orders.getOrderByRequest("request"));
        orders.registerOrder(trigger);
        orders.reset();
        assertNull(orders.getOrderByRequest("request"));
    }

    @Test public void personalFillsCountOnceAcrossAppendReloadAndNewGame() throws Exception {
        Path dir = Files.createTempDirectory(Path.of(System.getProperty("java.io.tmpdir")), "startup-regression-");
        TradingFacade game = new TradingFacade(SaveManager.forDirectory(dir.toFile()));
        game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(Path.of("src/main/assets/data/market_universe.json"))));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        game.getClock().advanceGameSeconds(1800);
        trade(game, "one");
        assertEquals(1, game.getPlayerProfileManager().exportState().getInt("totalTradesExecuted"));
        game.evaluateTriggers();
        assertEquals(1, game.getPlayerProfileManager().exportState().getInt("totalTradesExecuted"));
        game.save();
        trade(game, "two");
        assertEquals(2, game.getPlayerProfileManager().exportState().getInt("totalTradesExecuted"));
        assertTrue(game.load());
        game.evaluateTriggers();
        assertEquals(1, game.getPlayerProfileManager().exportState().getInt("totalTradesExecuted"));
        trade(game, "two");
        assertEquals(2, game.getPlayerProfileManager().exportState().getInt("totalTradesExecuted"));
        assertTrue(game.reconcileAll());
        game.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        game.getClock().advanceGameSeconds(1800);
        trade(game, "one");
        assertEquals(1, game.getPlayerProfileManager().exportState().getInt("totalTradesExecuted"));
        assertTrue(game.reconcileAll());
    }

    private void trade(TradingFacade game, String id) {
        Money price = Money.of(9000, Currency.IDR);
        Order sell = Order.createLimit("sell-" + id, "IDX:BBCA", "ACC-NPC-MM-01", OrderSide.SELL, price, 100, game.getGameTimestamp());
        Order buy = Order.createLimit("buy-" + id, "IDX:BBCA", game.getPersonalAccount().getId(), OrderSide.BUY, price, 100, game.getGameTimestamp());
        assertTrue(game.submitOrder(sell).isSuccess);
        assertTrue(game.submitOrder(buy).isSuccess);
        assertEquals(OrderStatus.FILLED, buy.getStatus());
        assertTrue(game.submitOrder(buy).isSuccess);
    }
}
