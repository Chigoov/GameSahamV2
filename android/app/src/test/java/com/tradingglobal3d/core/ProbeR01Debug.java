
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

public class ProbeR01Debug {
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
        Order makerBid = new Order("ORD-MAKER-BID", "REQ-MAKER-BID", facade.getPersonalAccount().getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT, Money.of(10000L, Currency.IDR), null, 100L,
                facade.getClock().getGameTimestamp(), Money.of(1000000L, Currency.IDR));
        facade.submitOrder(makerBid);

        Account npcSeller = facade.getAccountsMap().get("ACC-NPC-RETAIL-01");
        npcSeller.addPosition(new Position(bbca.getTicker(), bbca.getExchange().name(), bbca.getCurrency(),
                1000L, BigDecimal.valueOf(10000), BigDecimal.valueOf(10000)));

        Order npcSask = new Order("ORD-NPC-ASK", "REQ-NPC-ASK", npcSeller.getId(), bbca.getId(),
                OrderSide.SELL, OrderType.LIMIT, Money.of(10000L, Currency.IDR), null, 100L,
                facade.getClock().getGameTimestamp(), Money.zero(Currency.IDR));

        System.out.println("--- Submitting NPC Ask ---");
        var resNpc = facade.submitOrder(npcSask);
        System.out.println("NPC Ask fills count: " + resNpc.fills.size());
        if (!resNpc.fills.isEmpty()) {
            TradeFill fill = resNpc.fills.get(0);
            System.out.println("BuyerOrder ID: " + fill.getBuyOrderId());
            System.out.println("SellerOrder ID: " + fill.getSellOrderId());
            System.out.println("BuyerFee: " + fill.getBuyerFee());
            System.out.println("SellerFee: " + fill.getSellerFee());
        }
    }
}
