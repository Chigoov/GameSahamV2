
package com.tradingglobal3d.core;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.*;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.trading.*;
import com.tradingglobal3d.core.research.*;
import com.tradingglobal3d.core.storage.SaveManager;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;

public class ProbeR01Debug2 {
    public static void main(String[] args) throws Exception {
        File testDir = new File("build/tmp-probe-r01");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        ResearchBooster booster = facade.getResearchService().getBoosters().get(1);
        booster.activate();

        facade.advanceWorldSecondsForTest(35 * 60);

        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        OrderBook book = facade.getMarketEngine().getOrCreateBook(bbca);

        // Player places a SELL limit order at best ask price
        BigDecimal bestAsk = book.getAsks().isEmpty() ? bbca.getCurrentPrice().getAmount() : book.getAsks().keySet().iterator().next();
        Account personal = facade.getPersonalAccount();
        personal.addPosition(new Position(bbca.getTicker(), bbca.getExchange().name(), bbca.getCurrency(),
                1000L, bestAsk, bestAsk));

        Order playerSell = new Order("ORD-PLAYER-SELL", "REQ-P-SELL", personal.getId(), bbca.getId(),
                OrderSide.SELL, OrderType.LIMIT, new Money(bestAsk, Currency.IDR), null, 100L,
                facade.getClock().getGameTimestamp(), Money.zero(Currency.IDR));
        facade.submitOrder(playerSell);
        System.out.println("Player sell order status: " + playerSell.getStatus());

        // Now an NPC buys at that best ask price
        Account npcBuyer = facade.getAccountsMap().get("ACC-NPC-RETAIL-01");
        npcBuyer.creditCash(Money.of(10_000_000L, Currency.IDR));

        Order npcBuy = new Order("ORD-NPC-BUY", "REQ-NPC-BUY", npcBuyer.getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT, new Money(bestAsk, Currency.IDR), null, 100L,
                facade.getClock().getGameTimestamp(), Money.of(2000000L, Currency.IDR));

        var resNpc = facade.submitOrder(npcBuy);
        System.out.println("NPC Buy fills count: " + resNpc.fills.size());
        if (!resNpc.fills.isEmpty()) {
            TradeFill fill = resNpc.fills.get(0);
            System.out.println("BuyerOrder ID: " + fill.getBuyOrderId());
            System.out.println("SellerOrder ID: " + fill.getSellOrderId());
            System.out.println("BuyerFee: " + fill.getBuyerFee());
            System.out.println("SellerFee (Player): " + fill.getSellerFee());
        }
        System.out.println("Booster uses remaining: " + booster.getRemainingUses());
    }
}
