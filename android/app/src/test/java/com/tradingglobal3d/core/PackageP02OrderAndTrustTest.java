package com.tradingglobal3d.core;

import com.tradingglobal3d.core.career.CareerRank;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.firm.ClientMandate;
import com.tradingglobal3d.core.firm.ClientRiskProfile;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import com.tradingglobal3d.mobile.TradingWebBridge;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;

public class PackageP02OrderAndTrustTest {

    @Test
    public void testAU01_TriggersDoNotExecuteWhenExchangeClosed() throws Exception {
        File testDir = new File("build/tmp-test-p02-au01");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        Account personal = facade.getPersonalAccount();
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        Assert.assertNotNull(bbca);

        // Advance to 08:30 WIB (market is CLOSED)
        Assert.assertFalse("IDX must be closed at 08:30 WIB", facade.getClock().isTradingOpenFor(MarketExchange.IDX));

        // Setup player holding 100 shares
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100, BigDecimal.valueOf(10000), BigDecimal.valueOf(10000)));

        // Setup opponent maker BID in order book at 9500
        OrderBook book = facade.getMarketEngine().getOrCreateBook(bbca);
        Account mm = facade.getAccount("ACC-NPC-MM-01");
        Order mmBid = new Order("ORD-MM-BID", "REQ-MM-BID", mm.getId(), bbca.getId(), OrderSide.BUY, OrderType.LIMIT,
                Money.of(9500L, Currency.IDR), null, 100, facade.getGameTimestamp(), Money.of(950000L, Currency.IDR));
        book.addLimitOrder(mmBid);

        // Player submits STOP_LOSS SELL order triggered at 10000 (condition met since current price is <= 10000)
        Order stopLoss = new Order("ORD-SL-01", "REQ-SL-01", personal.getId(), bbca.getId(), OrderSide.SELL, OrderType.STOP_LOSS,
                null, Money.of(10000L, Currency.IDR), 100, facade.getGameTimestamp(), null);
        facade.submitOrder(stopLoss);

        Assert.assertEquals(OrderStatus.PENDING_TRIGGER, stopLoss.getStatus());

        // Call evaluateTriggers while market is closed
        facade.evaluateTriggers();

        // Order MUST stay PENDING_TRIGGER, not executed when market is closed!
        Assert.assertEquals("Trigger order must not execute when market is closed!", OrderStatus.PENDING_TRIGGER, stopLoss.getStatus());
        Assert.assertEquals(100L, personal.getPosition("IDX:BBCA").getQuantity());
    }

    @Test
    public void testAU14_MultipleClientBuyOrdersRespectConcentrationLimit() throws Exception {
        File testDir = new File("build/tmp-test-p02-au14");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        // Promote career to PORTFOLIO_MANAGER
        JSONObject careerState = facade.getCareerManager().exportState();
        careerState.put("rank", "PORTFOLIO_MANAGER");
        facade.getCareerManager().restoreState(careerState);
        facade.getCareerManager().requireRank(CareerRank.PORTFOLIO_MANAGER, "Mandat klien");

        // Create client account with 100M AUM (CONSERVATIVE risk: max concentration per stock = 15% -> 15M max)
        Account clientAcc = facade.createClientAccount("Klien Yayasan", "MANDATE-01", Money.of(100_000_000L, Currency.IDR));
        ClientMandate mandate = facade.getFirmManager().getMandate("MANDATE-01");
        Assert.assertNotNull(mandate);
        mandate.setClientAccountId(clientAcc.getId());

        Instrument bbca = facade.getCatalog().getById("IDX:BBCA"); // let price be e.g. 10,000 per share

        // First buy order: 1000 shares @ 9900 = 9.9M (approx 9.9% of AUM, within 15% limit)
        Order buy1 = new Order("ORD-C-B1", "REQ-C-B1", clientAcc.getId(), bbca.getId(), OrderSide.BUY, OrderType.LIMIT,
                Money.of(9900L, Currency.IDR), null, 1000L, facade.getGameTimestamp(), Money.of(10_000_000L, Currency.IDR));
        MatchingEngine.OrderResult res1 = facade.submitOrder(buy1);
        Assert.assertTrue("First order within limit must be accepted!", res1.isSuccess);
        Assert.assertEquals(OrderStatus.OPEN, buy1.getStatus());

        // Second buy order: another 1000 shares @ 9900 = 9.9M (total projected = 19.8M > 15M limit) -> MUST REJECT!
        Order buy2 = new Order("ORD-C-B2", "REQ-C-B2", clientAcc.getId(), bbca.getId(), OrderSide.BUY, OrderType.LIMIT,
                Money.of(9900L, Currency.IDR), null, 1000L, facade.getGameTimestamp(), Money.of(10_000_000L, Currency.IDR));
        MatchingEngine.OrderResult res2 = facade.submitOrder(buy2);
        Assert.assertFalse("Second order that would breach concentration limit must be rejected!", res2.isSuccess);
        Assert.assertEquals(OrderStatus.REJECTED, buy2.getStatus());
        Assert.assertTrue(res2.message.contains("konsentrasi"));
    }

    @Test
    public void testAU28_StopLossAndTakeProfitExecuteAllOrNoneWithoutPartialFill() throws Exception {
        File testDir = new File("build/tmp-test-p02-au28");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        // Advance from 08:30 to 09:18 WIB (trading open)
        facade.tick(60);
        Assert.assertTrue("IDX must be open", facade.getClock().isTradingOpenFor(MarketExchange.IDX));

        Account personal = facade.getPersonalAccount();
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 200, BigDecimal.valueOf(10000), BigDecimal.valueOf(10000)));

        // Opponent MM only offers 100 shares bid (insufficient to fill 200 shares order)
        OrderBook book = facade.getMarketEngine().getOrCreateBook(bbca);
        book.getBids().clear();
        Account mm = facade.getAccount("ACC-NPC-MM-01");
        Money curP = bbca.getCurrentPrice();
        Money buyCost = curP.multiply(100.0);
        Money fee = new Money(buyCost.getAmount().multiply(MatchingEngine.BUY_FEE_RATE), Currency.IDR);
        Money totalRes = buyCost.add(fee);
        mm.reserveCash(totalRes);
        Order mmBid = new Order("ORD-MM-BID-100", "REQ-MM-BID-100", mm.getId(), bbca.getId(), OrderSide.BUY, OrderType.LIMIT,
                curP, null, 100, facade.getGameTimestamp(), totalRes);
        book.addLimitOrder(mmBid);

        // Player submits triggered STOP_LOSS SELL order for 200 shares with triggerPrice >= currentPrice
        Order stopLoss = new Order("ORD-SL-200", "REQ-SL-200", personal.getId(), bbca.getId(), OrderSide.SELL, OrderType.STOP_LOSS,
                null, curP.add(Money.of(100L, Currency.IDR)), 200, facade.getGameTimestamp(), null);
        facade.submitOrder(stopLoss);
        Assert.assertEquals(OrderStatus.PENDING_TRIGGER, stopLoss.getStatus());

        // Evaluate triggers (which activates stopLoss and submits to matching engine)
        facade.evaluateTriggers();
        System.out.println("stopLoss status: " + stopLoss.getStatus());

        // Must be cancelled completely without partial fill when liquidity is insufficient
        Assert.assertEquals("Order must be cancelled completely without partial fill when liquidity is insufficient!", OrderStatus.CANCELLED, stopLoss.getStatus());
        Assert.assertEquals(200L, personal.getPosition("IDX:BBCA").getQuantity());
    }

    @Test
    public void testAU29_NpcRestingOrdersExpireAtDayClose() throws Exception {
        File testDir = new File("build/tmp-test-p02-au29");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        // Advance to 09:30 WIB
        facade.advanceRealMs(1000000, true);
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");

        // Generate NPC liquidity
        facade.generateNpcLiquidity();
        List<Order> restingBefore = facade.getMarketEngine().getRestingOrders();
        Assert.assertFalse("There must be resting NPC orders in the book", restingBefore.isEmpty());

        // Add player GTC limit buy order
        Account personal = facade.getPersonalAccount();
        Order playerGtc = new Order("ORD-PLAYER-GTC", "REQ-PLAYER-GTC", personal.getId(), bbca.getId(), OrderSide.BUY, OrderType.LIMIT,
                Money.of(1000L, Currency.IDR), null, 100, facade.getGameTimestamp(), Money.of(100000L, Currency.IDR));
        facade.submitOrder(playerGtc);
        Assert.assertEquals(OrderStatus.OPEN, playerGtc.getStatus());

        // Call expireDayOrders() at end of day
        int expiredCount = facade.expireDayOrders();
        Assert.assertTrue("Expired count must include resting NPC orders!", expiredCount > 0);

        // Player GTC order MUST NOT be expired
        Assert.assertEquals("Player GTC order must remain OPEN across day close!", OrderStatus.OPEN, playerGtc.getStatus());
    }

    @Test
    public void testAU50_FractionalQuantityAndLotsRejectedCleanly() throws Exception {
        File testDir = new File("build/tmp-test-p02-au50");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        TradingWebBridge bridge = new TradingWebBridge(facade);

        // 1. Fractional quantity on order submission: 100.9 shares
        JSONObject orderReq = new JSONObject()
                .put("instrumentId", "IDX:BBCA")
                .put("side", "BUY")
                .put("type", "LIMIT")
                .put("limitPrice", "9000")
                .put("quantity", 100.9)
                .put("requestId", "REQ-FRAC-1");

        String resOrder = bridge.submitOrder(orderReq.toString());
        JSONObject jsonOrder = new JSONObject(resOrder);
        Assert.assertFalse("Fractional order quantity must be rejected!", jsonOrder.getBoolean("success"));
        Assert.assertTrue(jsonOrder.getString("message").contains("pecahan") || jsonOrder.getString("message").contains("bulat"));

        // 2. Fractional lots on IPO subscription: 1.5 lots
        JSONObject ipoReq = new JSONObject()
                .put("ipoId", "IPO-TECH-01")
                .put("lots", 1.5)
                .put("requestId", "REQ-FRAC-2");

        String resIpo = bridge.orderIpo(ipoReq.toString());
        JSONObject jsonIpo = new JSONObject(resIpo);
        Assert.assertFalse("Fractional IPO lots must be rejected!", jsonIpo.getBoolean("success"));
        Assert.assertTrue(jsonIpo.getString("message").contains("pecahan") || jsonIpo.getString("message").contains("bulat"));
    }
}
