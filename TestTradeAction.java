
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.funds.MutualFundManager;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.mobile.TradingWebBridge;
import org.json.JSONObject;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;

public class TestTradeAction {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting TestTradeAction comprehensive verification...");
        
        // 1. Check Money.zero format
        Money zeroIdr = Money.zero(Currency.IDR);
        System.out.println("Zero IDR format: '" + zeroIdr.format() + "'");
        if (!"Rp 0".equals(zeroIdr.format())) {
            throw new AssertionError("Money.zero(IDR) should format to 'Rp 0', got: " + zeroIdr.format());
        }

        // 2. Check MutualFundManager asset loading (24 funds)
        MutualFundManager mfm = new MutualFundManager();
        int fundCount = mfm.getAllFunds().size();
        System.out.println("MutualFundManager loaded funds count: " + fundCount);
        if (fundCount < 24) {
            throw new AssertionError("MutualFundManager should load at least 24 funds, got: " + fundCount);
        }

        // 3. Facade setup
        File probeDir = new File("build/tmp-probe");
        probeDir.mkdirs();
        SaveManager saveManager = SaveManager.forDirectory(probeDir);
        TradingFacade facade = new TradingFacade(saveManager);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        InstrumentCatalog catalog = InstrumentCatalog.fromJsonString(catJson);
        facade.setCatalog(catalog);
        facade.startInternalGame(new BigDecimal("50000000"));
        TradingWebBridge bridge = new TradingWebBridge(facade, null);

        Instrument bbca = catalog.getById("IDX:BBCA");
        System.out.println("BBCA OrderBook: " + bridge.getOrderBook("IDX:BBCA"));

        // 4. Test Market BUY
        String buyMarketReq = new JSONObject()
            .put("accountId", facade.getPersonalAccount().getId())
            .put("instrumentId", "IDX:BBCA")
            .put("side", "BUY")
            .put("type", "MARKET")
            .put("quantity", 100)
            .put("requestId", "TEST-BUY-MKT-01")
            .toString();

        String buyResStr = bridge.submitOrder(buyMarketReq);
        System.out.println("Submit BUY MARKET: " + buyResStr);
        JSONObject buyRes = new JSONObject(buyResStr);
        if (!buyRes.getBoolean("success") || !"FILLED".equals(buyRes.getString("status"))) {
            throw new AssertionError("Market BUY failed: " + buyResStr);
        }

        // 5. Test Market SELL
        String sellReq = new JSONObject()
            .put("accountId", facade.getPersonalAccount().getId())
            .put("instrumentId", "IDX:BBCA")
            .put("side", "SELL")
            .put("type", "MARKET")
            .put("quantity", 100)
            .put("requestId", "TEST-SELL-MKT-01")
            .toString();

        String sellResStr = bridge.submitOrder(sellReq);
        System.out.println("Submit SELL MARKET: " + sellResStr);
        JSONObject sellRes = new JSONObject(sellResStr);
        if (!sellRes.getBoolean("success") || !"FILLED".equals(sellRes.getString("status"))) {
            throw new AssertionError("Market SELL failed: " + sellResStr);
        }

        // 6. Test Live Price Feed
        String priceFeed = bridge.getLivePriceFeed();
        System.out.println("Live price feed size: " + priceFeed.length() + " chars");
        if (priceFeed.length() > 2500) {
            throw new AssertionError("Live price feed should be micro (<2.5KB), got length: " + priceFeed.length());
        }

        System.out.println("ALL COMPREHENSIVE CHECKS PASSED SUCCESSFULLY!");
    }
}
