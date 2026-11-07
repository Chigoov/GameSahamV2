package com.tradingglobal3d.core;

import com.tradingglobal3d.core.collection.CollectionManager;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.HistoricalDatasetLoader;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.market.MarketIndex;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;

public class PackageR08ToR09VerificationTest {

    @Test
    public void testR08_CollectionCatalogLoadsComplete150ItemsUsingCompatibleBufferAndClosesStream() throws Exception {
        AtomicBoolean streamClosed = new AtomicBoolean(false);
        File assetFile = new File("android/app/src/main/assets/data/katalog_koleksi_150.json");
        Assert.assertTrue("Asset file must exist", assetFile.exists());

        // Test with a stream that returns data in small fragmented chunks (e.g. max 19 bytes per read)
        // to thoroughly verify that the buffer loop accumulates complete UTF-8 content across multiple reads
        HistoricalDatasetLoader.setAssetSource(path -> {
            InputStream raw = new FileInputStream(new File("android/app/src/main/assets/" + path));
            return new FilterInputStream(raw) {
                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    int smallLen = Math.min(len, 19);
                    return super.read(b, off, smallLen);
                }

                @Override
                public void close() throws IOException {
                    streamClosed.set(true);
                    super.close();
                }
            };
        });

        CollectionManager cm = new CollectionManager();
        Assert.assertEquals("CollectionManager must load all 150 items through compatible buffer reader!",
                150, cm.getAllItems().size());
        Assert.assertTrue("Underlying input stream must be closed properly!", streamClosed.get());

        // Test missing/corrupt asset stream handling: should not throw unhandled exception or crash
        HistoricalDatasetLoader.setAssetSource(path -> new ByteArrayInputStream("CORRUPT_JSON".getBytes(StandardCharsets.UTF_8)));
        CollectionManager cmCorrupt = new CollectionManager();
        // Should fall back to hardcoded default items cleanly without crashing
        Assert.assertTrue("Corrupt asset stream must handle fallback gracefully", cmCorrupt.getAllItems().size() > 0);
    }

    @Test
    public void testR09_MarketIndexChainsCumulativeReturnsAcrossDayRolloverInProductionFacade() throws Exception {
        HistoricalDatasetLoader.setAssetSource(path -> new FileInputStream(new File("android/app/src/main/assets/" + path)));
        File testDir = new File("build/tmp-test-r09-chain");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        InstrumentCatalog catalog = facade.getCatalog();
        Instrument bbca = catalog.getById("IDX:BBCA");
        Assert.assertNotNull(bbca);

        MarketIndex ihsg = catalog.getIndex("IDX:COMPOSITE");
        Assert.assertNotNull("IHSG composite index must exist", ihsg);
        double ihsgBase = ihsg.getBaseValue();
        Assert.assertEquals(7700.0, ihsgBase, 0.01);
        Assert.assertEquals(ihsgBase, ihsg.getCurrentValue(), 0.01);

        // Day 1 intraday: Price of BBCA moves up 10% (10,000 -> 11,000)
        bbca.updatePrice(Money.of(11000L, Currency.IDR), 1000);
        catalog.recalculateAllIndices();
        double ihsgDay1Close = ihsg.getCurrentValue();
        System.out.println("IHSG Day 1 close: " + ihsgDay1Close);
        Assert.assertTrue("IHSG must rise after member stock price increases", ihsgDay1Close > ihsgBase);

        // Fast forward through midnight (86,400s) to next day 08:30 WIB
        facade.advanceWorldSecondsForTest(86400);

        // R09: On Day 2 start, index previousCloseValue MUST be chained from Day 1 close!
        Assert.assertEquals("Index previousCloseValue must chain previous day's close!",
                ihsgDay1Close, ihsg.getPreviousCloseValue(), 0.01);

        // Without any price changes on Day 2 morning, index value MUST NOT jump back to base 1000!
        catalog.recalculateAllIndices();
        double ihsgDay2Open = ihsg.getCurrentValue();
        System.out.println("IHSG Day 2 open: " + ihsgDay2Open);
        Assert.assertEquals("Index value on Day 2 start must equal Day 1 close, not jump back to 1000!",
                ihsgDay1Close, ihsgDay2Open, 0.01);

        // Day 2 intraday: Price moves up another 10% (11,000 -> 12,100)
        bbca.updatePrice(Money.of(12100L, Currency.IDR), 1000);
        catalog.recalculateAllIndices();
        double ihsgDay2Close = ihsg.getCurrentValue();
        System.out.println("IHSG Day 2 close: " + ihsgDay2Close);
        Assert.assertTrue("Index must chain upward from previous close!", ihsgDay2Close > ihsgDay2Open);
    }

    @Test
    public void testR09_MarketIndexPersistsAcrossSaveLoadAndRestoresIdenticalAnchor() throws Exception {
        HistoricalDatasetLoader.setAssetSource(path -> new FileInputStream(new File("android/app/src/main/assets/" + path)));
        File testDir = new File("build/tmp-test-r09-saveload");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        InstrumentCatalog catalog = facade.getCatalog();
        Instrument bbca = catalog.getById("IDX:BBCA");
        bbca.updatePrice(Money.of(11500L, Currency.IDR), 1000);
        catalog.recalculateAllIndices();

        MarketIndex ihsg = catalog.getIndex("IDX:COMPOSITE");
        double valBefore = ihsg.getCurrentValue();
        double refBefore = ihsg.getPreviousCloseValue();

        // Save
        facade.save();

        // Load into brand new facade instance
        SaveManager sm2 = SaveManager.forDirectory(testDir);
        TradingFacade facade2 = new TradingFacade(sm2);
        facade2.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade2.startOrResume();

        MarketIndex ihsgLoaded = facade2.getCatalog().getIndex("IDX:COMPOSITE");
        Assert.assertNotNull(ihsgLoaded);

        // R09: Restored index values must be identical to pre-save values!
        Assert.assertEquals("Restored index currentValue must match pre-save value!",
                valBefore, ihsgLoaded.getCurrentValue(), 0.001);
        Assert.assertEquals("Restored index previousCloseValue must match pre-save reference!",
                refBefore, ihsgLoaded.getPreviousCloseValue(), 0.001);
    }

    @Test
    public void testR09_NewGameResetsMarketIndexAnchorToBaseValue() throws Exception {
        HistoricalDatasetLoader.setAssetSource(path -> new FileInputStream(new File("android/app/src/main/assets/" + path)));
        File testDir = new File("build/tmp-test-r09-newgame");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        // Increase price
        facade.getCatalog().getById("IDX:BBCA").updatePrice(Money.of(12000L, Currency.IDR), 1000);
        facade.getCatalog().recalculateAllIndices();
        Assert.assertTrue(facade.getCatalog().getIndex("IDX:COMPOSITE").getCurrentValue() > 1000.0);

        // Start new game on same facade
        facade.startInternalGame(new BigDecimal("50000000"));

        MarketIndex ihsgReset = facade.getCatalog().getIndex("IDX:COMPOSITE");
        Assert.assertEquals("New game must reset index currentValue to base 7700.0",
                7700.0, ihsgReset.getCurrentValue(), 0.01);
        Assert.assertEquals("New game must reset index previousCloseValue to base 7700.0",
                7700.0, ihsgReset.getPreviousCloseValue(), 0.01);
    }

    @Test
    public void testR09_LegacySaveWithoutIndicesArrayRestoresCleanlyWithoutError() throws Exception {
        HistoricalDatasetLoader.setAssetSource(path -> new FileInputStream(new File("android/app/src/main/assets/" + path)));
        File testDir = new File("build/tmp-test-r09-legacy");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        facade.save();

        // Strip the "indices" field from save_game_v2.json to simulate an older save
        File saveFile = sm.getSaveFile();
        String jsonText = Files.readString(saveFile.toPath());
        JSONObject saveObj = new JSONObject(jsonText);
        JSONObject catObj = saveObj.getJSONObject("runtimeState").getJSONObject("catalog");
        catObj.remove("indices");
        Files.writeString(saveFile.toPath(), saveObj.toString());

        // Load into fresh facade
        SaveManager sm2 = SaveManager.forDirectory(testDir);
        TradingFacade facade2 = new TradingFacade(sm2);
        facade2.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        boolean loaded = facade2.startOrResume();
        Assert.assertTrue("Legacy save without indices field must load successfully", loaded);

        MarketIndex ihsg = facade2.getCatalog().getIndex("IDX:COMPOSITE");
        Assert.assertNotNull(ihsg);
        Assert.assertEquals(7700.0, ihsg.getCurrentValue(), 0.01);
    }
}
