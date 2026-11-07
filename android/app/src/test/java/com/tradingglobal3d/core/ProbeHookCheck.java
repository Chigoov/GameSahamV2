
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

public class ProbeHookCheck {
    public static void main(String[] args) throws Exception {
        File testDir = new File("build/tmp-probe-r01");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);

        Instrument inst = new Instrument("IDX:BBCA", "BBCA", "Bank BCA", MarketExchange.IDX, "Finance", Currency.IDR, 100, 1000000L, true, Money.of(10000L, Currency.IDR));
        Account personal = new Account("ACC-PERSONAL-001", "Player", AccountType.PERSONAL, null);
        Order order = new Order("ORD-1", "REQ-1", personal.getId(), inst.getId(), OrderSide.BUY, OrderType.LIMIT, Money.of(10000L, Currency.IDR), null, 100L, 1000L, null);

        BigDecimal rate = facade.getMarketEngine().getMatchingEngine().resolveFeeRate(order, personal, inst, MatchingEngine.BUY_FEE_RATE, false);
        System.out.println("Resolved fee rate without booster: " + rate);

        facade.getResearchService().getBoosters().get(1).activate();
        BigDecimal rateWithBooster = facade.getMarketEngine().getMatchingEngine().resolveFeeRate(order, personal, inst, MatchingEngine.BUY_FEE_RATE, false);
        System.out.println("Resolved fee rate with booster: " + rateWithBooster);
    }
}
