
package com.tradingglobal3d.core;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.*;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.trading.*;
import com.tradingglobal3d.core.storage.SaveManager;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;

public class ProbeQC04C {
    public static void main(String[] args) throws Exception {
        File testDir = new File("build/tmp-probe-qc04");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        facade.advanceWorldSecondsForTest(35 * 60); // 09:05 WIB
        facade.generateNpcLiquidity();

        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        OrderBook book = facade.getMarketEngine().getOrCreateBook(bbca);
        System.out.println("Book asks: " + book.getAsks().keySet());
        System.out.println("Book bids: " + book.getBids().keySet());
        if (!book.getAsks().isEmpty()) {
            BigDecimal lowestAsk = book.getAsks().keySet().iterator().next();
            System.out.println("Lowest ask price: " + lowestAsk);
            Order fillOrder = new Order("ORD-FILL-1", "REQ-F1", facade.getPersonalAccount().getId(), bbca.getId(),
                    OrderSide.BUY, OrderType.LIMIT, new Money(lowestAsk, Currency.IDR), null, 100L,
                    facade.getClock().getGameTimestamp(), Money.of(2000000L, Currency.IDR));
            var resFill = facade.submitOrder(fillOrder);
            System.out.println("Fill result: success=" + resFill.isSuccess + ", msg=" + resFill.message + ", fills=" + resFill.fills.size() + ", status=" + fillOrder.getStatus());
        }
    }
}
