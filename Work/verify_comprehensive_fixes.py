import subprocess
import os

repo = "C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM"
os.chdir(repo)

output_classes = "Artifacts/Synthesis-Simulation-Tests/classes"
existing_classes = "Artifacts/Test-Plan14-Full/classes"
json_jar = "C:/Users/HYPE AMD/.gradle/caches/modules-2/files-2.1/org.json/json/20240303/ebb88e8fb5122b7506d5cf1d69f1ccdb790d22a/json-20240303.jar"
junit_jar = "C:/Users/HYPE AMD/.gradle/caches/modules-2/files-2.1/junit/junit/4.13.2/8ac9e16d933b6fb43bc7f576336b8f4d7eb5ba12/junit-4.13.2.jar"
hamcrest_jar = "C:/Users/HYPE AMD/.gradle/caches/modules-2/files-2.1/org.hamcrest/hamcrest-core/1.3/42a25dc3219429f0e5d060061f71acb49bf010a0/hamcrest-core-1.3.jar"
android_jar = "C:/Users/HYPE AMD/AppData/Local/Android/Sdk/platforms/android-36/android.jar"
gradle_classes = "android/app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes"
cp = f"{output_classes};{gradle_classes};{existing_classes};{json_jar};{junit_jar};{hamcrest_jar};{android_jar}"

javac_exe = "C:/Program Files/Android/Android Studio/jbr/bin/javac.exe"
java_exe = "C:/Program Files/Android/Android Studio/jbr/bin/java.exe"

test_source = """
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
"""

with open("TestTradeAction.java", "w", encoding="utf-8") as f:
    f.write(test_source)

res = subprocess.run([javac_exe, "-encoding", "UTF-8", "-cp", cp, "TestTradeAction.java"], capture_output=True, text=True)
if res.returncode != 0:
    print("Compilation failed:", res.stderr)
else:
    print("Compilation success. Running...")
    p = subprocess.run([java_exe, "-cp", f".;{cp}", "TestTradeAction"], capture_output=True, text=True)
    print("STDOUT:", p.stdout)
    print("STDERR:", p.stderr)
    if p.returncode != 0:
        raise SystemExit(p.returncode)
