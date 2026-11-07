package com.tradingglobal3d.core;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.*;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.TradeFill;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class OwnerDailyMarketActivityTest {
    @Test public void dailyBatchPreservesUnmatchedPlayerOrdersAndReservations() throws Exception {
        TradingFacade game = game();
        var account = game.getPersonalAccount();
        Instrument stock = game.getCatalog().getById("IDX:BBCA");
        account.addPosition(new Position(stock.getTicker(), stock.getExchange().name(), stock.getCurrency(),
                10000, stock.getCurrentPrice().getAmount(), stock.getCurrentPrice().getAmount()));
        var buy = new com.tradingglobal3d.core.trading.Order("QA-GTC-B", "QA-GTC-B", account.getId(), stock.getId(),
                com.tradingglobal3d.core.trading.OrderSide.BUY, com.tradingglobal3d.core.trading.OrderType.LIMIT,
                stock.getDailyFloorPrice(), null, 1000, game.getGameTimestamp(), null);
        var sell = new com.tradingglobal3d.core.trading.Order("QA-GTC-S", "QA-GTC-S", account.getId(), stock.getId(),
                com.tradingglobal3d.core.trading.OrderSide.SELL, com.tradingglobal3d.core.trading.OrderType.LIMIT,
                stock.getDailyCeilingPrice(), null, 10000, game.getGameTimestamp(), null);
        assertTrue(game.submitOrder(buy).isSuccess);
        assertTrue(game.submitOrder(sell).isSuccess);
        Money reserved = account.getReservedCash(stock.getCurrency());
        assertTrue(reserved.isPositive());
        game.advanceWorldSecondsForTest(86400);
        assertEquals(com.tradingglobal3d.core.trading.OrderStatus.OPEN, buy.getStatus());
        assertEquals(com.tradingglobal3d.core.trading.OrderStatus.OPEN, sell.getStatus());
        assertEquals(0, buy.getFilledQuantity());
        assertEquals(0, sell.getFilledQuantity());
        assertEquals(reserved, account.getReservedCash(stock.getCurrency()));
        assertEquals(10000, account.getPosition(stock.getId()).getQuantity());
        assertTrue(game.reconcileAll());
        game.save(); assertTrue(game.load());
        assertEquals(reserved, game.getPersonalAccount().getReservedCash(stock.getCurrency()));
        assertEquals(com.tradingglobal3d.core.trading.OrderStatus.OPEN,
                game.getOrderManager().getOrderByRequest("QA-GTC-B").getStatus());
        assertEquals(com.tradingglobal3d.core.trading.OrderStatus.OPEN,
                game.getOrderManager().getOrderByRequest("QA-GTC-S").getStatus());
    }
    @Test public void liveFeedsAgreeWithInstrumentListOnLotUnits() throws Exception {
        TradingFacade game = game();
        Instrument stock = game.getCatalog().getById("IDX:BBCA");
        stock.updatePrice(stock.getCurrentPrice(), 123L * stock.getLotSize());
        var bridge = new com.tradingglobal3d.mobile.TradingWebBridge(game);
        var single = new org.json.JSONObject(bridge.getLivePriceFeed(stock.getId()));
        assertEquals(123, single.getInt("volumeLots"));
        var feed = new org.json.JSONArray(bridge.getLivePriceFeed());
        for (int i = 0; i < feed.length(); i++) {
            var row = feed.getJSONArray(i);
            if (stock.getTicker().equals(row.getString(0))) {
                assertEquals(123, row.getInt(3));
                return;
            }
        }
        fail("BBCA missing from live feed");
    }
    @Test public void fifteenHoursOfflineAdvanceThirtyGameDaysAndSave() throws Exception {
        TradingFacade game = game();
        long start = game.getGameTimestamp();
        game.advanceRealMs(15L * 3600000, false);
        assertEquals(30L * 86400000, game.getGameTimestamp() - start);
        assertEquals(30, game.getCareerManager().getSessionReports().size());
        assertTrue(game.reconcileAll());
        game.save();
        assertTrue(game.load());
        assertEquals(start + 30L * 86400000, game.getGameTimestamp());
        assertTrue(game.reconcileAll());
    }
    private TradingFacade game() throws Exception {
        TradingFacade game = new TradingFacade(SaveManager.forDirectory(Files.createTempDirectory("owner-daily-market-").toFile()));
        game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(Path.of(System.getProperty("annotation.root"),
                "android/app/src/main/assets/data/market_universe.json"))));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"),
                new GameClock(LocalDate.of(2026, 10, 19), LocalTime.MIDNIGHT));
        game.getResearchService().setDynamicDailyNewsEnabled(false);
        return game;
    }

    private long dayVolume(TradingFacade game, Instrument stock, long start) {
        long volume = 0;
        for (TradeFill fill : game.getMarketEngine().getOrCreateBook(stock).getTradeHistory())
            if (fill.getTimestamp() >= start && fill.getTimestamp() < start + 86400000L) volume += fill.getQuantity();
        return volume / stock.getLotSize();
    }

    @Test public void everyActiveStockHasMatchedVolumeAndANonflatDailyCloseDuringCatchup() throws Exception {
        TradingFacade game = game();
        long start = game.getGameTimestamp();
        Map<String, Money> references = new LinkedHashMap<>();
        for (Instrument stock : game.getCatalog().getActiveUniverse())
            if (stock.isTradable()) references.put(stock.getId(), stock.getPreviousClosePrice());
        assertTrue(references.size() > 10);
        game.advanceWorldSecondsForTest(86400);
        List<String> quiet = new ArrayList<>(), flat = new ArrayList<>();
        for (Map.Entry<String, Money> entry : references.entrySet()) {
            Instrument stock = game.getCatalog().getById(entry.getKey());
            long volume = dayVolume(game, stock, start);
            if (volume < 1000) quiet.add(stock.getId() + ":" + volume);
            if (stock.getPreviousClosePrice().equals(entry.getValue())) flat.add(stock.getId());
        }
        assertTrue("Flat daily close: " + flat, flat.isEmpty());
        assertTrue("Daily matched volume below 1,000 lots: " + quiet, quiet.isEmpty());
        assertTrue(game.reconcileAll());
    }

    @Test public void dailyVolumeScalesWithClimateAndUsesFundedCounterparties() throws Exception {
        TradingFacade low = game(), high = game();
        low.setMacroIndex(1); high.setMacroIndex(9);
        long start = low.getGameTimestamp();
        List<Instrument> sessionStocks = low.getCatalog().getActiveUniverse();
        low.advanceWorldSecondsForTest(86400); high.advanceWorldSecondsForTest(86400);
        for (Instrument stock : sessionStocks) {
            if (!stock.isTradable()) continue;
            long lowLots = dayVolume(low, stock, start);
            long highLots = dayVolume(high, high.getCatalog().getById(stock.getId()), start);
            assertTrue(stock.getId(), highLots > lowLots);
            assertTrue(highLots <= 99999);
        }
        for (TradingFacade game : new TradingFacade[]{low, high}) {
            for (Account account : game.getAllAccounts())
                for (com.tradingglobal3d.core.model.Currency currency : com.tradingglobal3d.core.model.Currency.values())
                    assertTrue(account.getId(), game.getLedger().reconcile(account, currency));
            assertTrue(game.reconcileAll());
        }
    }

    @Test public void multipleDaysAndReloadKeepValidTicksWhileHaltedStocksAreExcluded() throws Exception {
        TradingFacade game = game();
        for (String id : new String[]{"IDX:BBCA", "US:AAPL"}) game.getMarketEngine().haltTrading(id);
        for (int day = 0; day < 3; day++) {
            Map<String, Money> references = new HashMap<>();
            for (Instrument stock : game.getCatalog().getActiveUniverse()) references.put(stock.getId(), stock.getPreviousClosePrice());
            long start = game.getGameTimestamp();
            game.advanceWorldSecondsForTest(86400);
            int ups = 0, downs = 0;
            for (Instrument stock : game.getCatalog().getActiveUniverse()) {
                Money before = references.get(stock.getId());
                if (before == null || !stock.isTradable()) continue;
                if (game.getMarketEngine().isHalted(stock.getId())) {
                    assertEquals(before, stock.getPreviousClosePrice());
                    assertEquals(0, dayVolume(game, stock, start));
                } else {
                    int change = stock.getPreviousClosePrice().compareTo(before);
                    assertNotEquals(stock.getId(), 0, change);
                    if (change > 0) ups++; else downs++;
                    assertTrue(dayVolume(game, stock, start) >= 1000);
                    assertEquals(0, stock.getPreviousClosePrice().getAmount().remainder(
                            stock.getExchange().getTickSize(stock.getPreviousClosePrice().getAmount())).signum());
                }
            }
            assertTrue(ups > 0 && downs > 0);
            assertTrue(game.reconcileAll());
            game.save();
            TradingFacade restored = new TradingFacade(game.getSaveManager());
            restored.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(Path.of(System.getProperty("annotation.root"),
                    "android/app/src/main/assets/data/market_universe.json"))));
            assertTrue(restored.load());
            assertEquals(game.getGameTimestamp(), restored.getGameTimestamp());
            for (String id : references.keySet()) assertEquals(game.getCatalog().getById(id).getPreviousClosePrice(),
                    restored.getCatalog().getById(id).getPreviousClosePrice());
            game = restored;
        }
    }
}
