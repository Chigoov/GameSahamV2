
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

public class ProbeR01WithBooster {
    public static void main(String[] args) throws Exception {
        File testDir = new File("build/tmp-probe-r01");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        ResearchService rs = facade.getResearchService();
        ResearchBooster booster = null;
        for (ResearchBooster b : rs.getBoosters()) {
            if (b.getType() == ResearchBooster.BoosterType.FEE_DISCOUNT) {
                booster = b;
                break;
            }
        }
        booster.activate();
        System.out.println("Booster active: " + booster.isActive() + ", uses: " + booster.getRemainingUses());

        facade.advanceWorldSecondsForTest(35 * 60);

        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        Order makerBid = new Order("ORD-MAKER-BID", "REQ-MAKER-BID", facade.getPersonalAccount().getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT, Money.of(10000L, Currency.IDR), null, 100L,
                facade.getClock().getGameTimestamp(), Money.of(1000000L, Currency.IDR));
        var resMaker = facade.submitOrder(makerBid);
        System.out.println("Maker result: " + resMaker.isSuccess + ", msg=" + resMaker.message + ", fills=" + resMaker.fills.size());

        Account npcSeller = facade.getAccountsMap().get("ACC-NPC-RETAIL-01");
        npcSeller.addPosition(new Position(bbca.getTicker(), bbca.getExchange().name(), bbca.getCurrency(),
                1000L, BigDecimal.valueOf(10000), BigDecimal.valueOf(10000)));

        Order npcSask = new Order("ORD-NPC-ASK", "REQ-NPC-ASK", npcSeller.getId(), bbca.getId(),
                OrderSide.SELL, OrderType.LIMIT, Money.of(10000L, Currency.IDR), null, 100L,
                facade.getClock().getGameTimestamp(), Money.zero(Currency.IDR));

        var resNpc = facade.submitOrder(npcSask);
        System.out.println("NPC Ask result: " + resNpc.isSuccess + ", msg=" + resNpc.message + ", fills=" + resNpc.fills.size());
        if (!resNpc.fills.isEmpty()) {
            TradeFill fill = resNpc.fills.get(0);
            System.out.println("Fill: buyerFee=" + fill.getBuyerFee() + ", sellerFee=" + fill.getSellerFee());
        }
        System.out.println("Booster after fill: uses=" + booster.getRemainingUses() + ", active=" + booster.isActive());
        System.out.println("Maker order boosterApplied: " + makerBid.isBoosterApplied());
    }
}
