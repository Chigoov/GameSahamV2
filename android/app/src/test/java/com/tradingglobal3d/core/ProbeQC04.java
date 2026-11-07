
package com.tradingglobal3d.core;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.*;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.trading.*;
import com.tradingglobal3d.core.storage.SaveManager;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;

public class ProbeQC04 {
    public static void main(String[] args) throws Exception {
        File testDir = new File("build/tmp-probe-qc04");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        facade.advanceWorldSecondsForTest(35 * 60);

        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        Account mm = facade.getAccountsMap().get("ACC-NPC-MM-01");
        System.out.println("MM account: " + mm);
        Position pos = mm.getPosition("IDX:BBCA");
        System.out.println("MM pos before: " + pos);

        mm.addPosition(new Position(bbca.getTicker(), bbca.getExchange().name(), bbca.getCurrency(),
                1000L, bbca.getCurrentPrice().getAmount(), bbca.getCurrentPrice().getAmount()));

        System.out.println("MM pos after: " + mm.getPosition("IDX:BBCA"));

        Order ask = new Order("ORD-ASK-MM", "REQ-ASK-MM", mm.getId(), bbca.getId(),
                OrderSide.SELL, OrderType.LIMIT, bbca.getCurrentPrice(), null, 100L,
                facade.getClock().getGameTimestamp(), Money.zero(Currency.IDR));
        var resAsk = facade.submitOrder(ask);
        System.out.println("Ask result: success=" + resAsk.isSuccess + ", msg=" + resAsk.message + ", status=" + ask.getStatus());

        Order fillOrder = new Order("ORD-FILL-1", "REQ-F1", facade.getPersonalAccount().getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT, bbca.getCurrentPrice(), null, 100L,
                facade.getClock().getGameTimestamp(), Money.of(2000000L, Currency.IDR));
        var resFill = facade.submitOrder(fillOrder);
        System.out.println("Fill result: success=" + resFill.isSuccess + ", msg=" + resFill.message + ", fills=" + resFill.fills.size() + ", status=" + fillOrder.getStatus());
    }
}
