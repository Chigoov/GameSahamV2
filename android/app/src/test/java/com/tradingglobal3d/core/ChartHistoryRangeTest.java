package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.OrderBook;
import com.tradingglobal3d.core.trading.TradeFill;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.Assert.*;

public class ChartHistoryRangeTest {
    @Test public void dailyStatisticsUseActualTurnoverSurviveLoadAndResetSafely() throws Exception {
        TradingFacade game = game();
        var bridge = new com.tradingglobal3d.mobile.TradingWebBridge(game);
        try {
            var instrument = game.getCatalog().getById("IDX:BBCA");
            Money first = instrument.getCurrentPrice().multiply(new java.math.BigDecimal("1.02"));
            Money second = instrument.getCurrentPrice().multiply(new java.math.BigDecimal("1.01"));
            instrument.updatePrice(first, 100); instrument.updatePrice(second, 200);
            java.math.BigDecimal turnover = first.getAmount().multiply(java.math.BigDecimal.valueOf(100))
                    .add(second.getAmount().multiply(java.math.BigDecimal.valueOf(200)));
            JSONObject quote = new JSONArray(bridge.getInstruments("BBCA", "IDX", null, true)).getJSONObject(0);
            assertEquals(first.getAmount().toPlainString(), quote.getString("openPriceRaw"));
            assertEquals(0, turnover.compareTo(new java.math.BigDecimal(quote.getString("dayValueRaw"))));
            assertEquals("3", new java.math.BigDecimal(quote.getString("volumeLots")).stripTrailingZeros().toPlainString());
            assertEquals(0, turnover.divide(java.math.BigDecimal.valueOf(300), 4, java.math.RoundingMode.HALF_UP)
                    .compareTo(new java.math.BigDecimal(quote.getString("avgPriceRaw"))));
            assertEquals(instrument.getDailyCeilingPrice().getAmount().toPlainString(), quote.getString("dailyCeilingRaw"));
            assertEquals(instrument.getDailyFloorPrice().getAmount().toPlainString(), quote.getString("dailyFloorRaw"));
            game.save(); instrument.updatePrice(first, 100);
            assertTrue(game.load());
            JSONObject loaded = new JSONArray(bridge.getInstruments("BBCA", "IDX", null, true)).getJSONObject(0);
            for (String field : new String[]{"openPriceRaw", "volumeLots", "dayValueRaw", "avgPriceRaw"})
                assertEquals("Daily statistic lost after save/load: " + field, quote.getString(field), loaded.getString(field));
            JSONObject invalid = game.getCatalog().exportState();
            JSONArray invalidItems = invalid.getJSONArray("instruments");
            for (int i = 0; i < invalidItems.length(); i++) if ("IDX:BBCA".equals(invalidItems.getJSONObject(i).getString("id")))
                invalidItems.getJSONObject(i).put("dayOpen", "999999999999");
            assertThrows(IllegalArgumentException.class, () -> game.getCatalog().restoreState(invalid));
            assertEquals(loaded.getString("openPriceRaw"), new JSONArray(bridge.getInstruments("BBCA", "IDX", null, true)).getJSONObject(0).getString("openPriceRaw"));
            JSONObject legacy = game.getCatalog().exportState();
            JSONArray items = legacy.getJSONArray("instruments");
            for (int i = 0; i < items.length(); i++) { items.getJSONObject(i).remove("dayOpen"); items.getJSONObject(i).remove("dayTurnover"); }
            game.getCatalog().restoreState(legacy);
            JSONObject unknown = new JSONArray(bridge.getInstruments("BBCA", "IDX", null, true)).getJSONObject(0);
            assertTrue(unknown.isNull("openPriceRaw")); assertTrue(unknown.isNull("dayValueRaw")); assertTrue(unknown.isNull("avgPriceRaw"));
            long now = game.getGameTimestamp();
            OrderBook book = game.getMarketEngine().getOrCreateBook(game.getCatalog().getById("IDX:BBCA"));
            book.recordFill(new TradeFill("QA-DAY-SECOND", "QA-B2", "QA-S2", "ACC-NPC-RETAIL-01", "ACC-NPC-MM-01", "IDX:BBCA", second, 200, now));
            game.save(); assertTrue(game.load());
            assertTrue(new JSONArray(bridge.getInstruments("BBCA", "IDX", null, true)).getJSONObject(0).isNull("dayValueRaw"));
            book = game.getMarketEngine().getOrCreateBook(game.getCatalog().getById("IDX:BBCA"));
            book.recordFill(new TradeFill("QA-DAY-FIRST", "QA-B1", "QA-S1", "ACC-NPC-RETAIL-01", "ACC-NPC-MM-01", "IDX:BBCA", first, 100, now - 1000));
            game.save(); assertTrue(game.load());
            JSONObject recovered = new JSONArray(bridge.getInstruments("BBCA", "IDX", null, true)).getJSONObject(0);
            assertEquals(quote.getString("openPriceRaw"), recovered.getString("openPriceRaw"));
            assertEquals(quote.getString("dayValueRaw"), recovered.getString("dayValueRaw"));
            instrument = game.getCatalog().getById("IDX:BBCA"); instrument.startNewTradingDay();
            instrument.updatePrice(first, 100);
            JSONObject nextDay = new JSONArray(bridge.getInstruments("BBCA", "IDX", null, true)).getJSONObject(0);
            assertEquals(first.getAmount().toPlainString(), nextDay.getString("openPriceRaw"));
            assertEquals(0, first.getAmount().multiply(java.math.BigDecimal.valueOf(100)).compareTo(new java.math.BigDecimal(nextDay.getString("dayValueRaw"))));
        } finally { bridge.destroy(); }
    }

    @Test public void quoteCarriesActualDailyPricesAndUpdatesThem() throws Exception {
        TradingFacade game = game();
        var bridge = new com.tradingglobal3d.mobile.TradingWebBridge(game);
        try {
            var instrument = game.getCatalog().getById("IDX:BBCA");
            JSONObject before = new JSONArray(bridge.getInstruments("BBCA", "IDX", null, true)).getJSONObject(0);
            instrument.updatePrice(instrument.getCurrentPrice().multiply(new java.math.BigDecimal("1.02")), 100);
            JSONObject after = new JSONArray(bridge.getInstruments("BBCA", "IDX", null, true)).getJSONObject(0);
            assertTrue(before.isNull("highPriceRaw"));
            assertNotEquals(before.opt("highPriceRaw"), after.opt("highPriceRaw"));
            assertEquals(instrument.getDayHigh().getAmount().toPlainString(), after.getString("highPriceRaw"));
            assertEquals(instrument.getDayLow().getAmount().toPlainString(), after.getString("lowPriceRaw"));
            assertEquals(instrument.getPreviousClosePrice().getAmount().toPlainString(), after.getString("previousCloseRaw"));
        } finally { bridge.destroy(); }
    }
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private TradingFacade game() throws Exception {
        TradingFacade game = new TradingFacade(SaveManager.forDirectory(temp.newFolder()));
        game.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")), java.nio.charset.StandardCharsets.UTF_8)));
        game.setRealTimeClock(java.time.Clock.fixed(java.time.Instant.ofEpochMilli(1792756800000L), java.time.ZoneOffset.UTC));
        assertTrue(game.startOrResume());
        return game;
    }
    private void fill(OrderBook book, String id, long at, long price, long quantity) {
        book.recordFill(new TradeFill(id,"BUY-"+id,"SELL-"+id,"QA-B","QA-S","IDX:BBCA",Money.of(price,Currency.IDR),quantity,at)
                .withTransactionFees(Money.zero(Currency.IDR), Money.zero(Currency.IDR)));
    }
    @Test public void fourRangesFilterActualTimestampsWithoutReferenceOrFutureBars() throws Exception {
        TradingFacade game=game();long now=game.getGameTimestamp();OrderBook book=game.getMarketEngine().getOrCreateBook(game.getCatalog().getById("IDX:BBCA"));
        fill(book,"old",now-31L*86400000,9000,100);
        fill(book,"month",now-20L*86400000,9100,200);
        fill(book,"week",now-3L*86400000,9200,300);
        fill(book,"day",now-12L*3600000,9300,400);
        fill(book,"six",now-2L*3600000,9400,500);
        fill(book,"future",now+1,9999,600);
        int[] expected={1,2,3,4};String[] ranges={"R6H","R1D","R7D","R30D"};
        for(int i=0;i<ranges.length;i++){
            JSONObject chart=game.getChartData("IDX:BBCA",ranges[i]);
            assertEquals(ranges[i],chart.getString("range"));
            assertEquals(expected[i],chart.getInt("totalTrades"));
            long volume=0;JSONArray bars=chart.getJSONArray("bars");
            for(int j=0;j<bars.length();j++){JSONObject bar=bars.getJSONObject(j);assertTrue(bar.getLong("timestamp")<=now);assertFalse(bar.getBoolean("isReference"));assertTrue(bar.getDouble("high")<9999);volume+=bar.getLong("volume");}
            long expectedVolume=500+(i>=1?400:0)+(i>=2?300:0)+(i>=3?200:0);
            assertEquals(expectedVolume,volume);
        }
        assertEquals(now,game.getGameTimestamp());
    }
    @Test public void unorderedFillsAreSortedAndDedupedWithCorrectOhlcv() throws Exception {
        TradingFacade game=game();long now=game.getGameTimestamp();OrderBook book=game.getMarketEngine().getOrCreateBook(game.getCatalog().getById("IDX:BBCA"));
        long bucket=Math.floorDiv(now,900000L)*900000L-900000L;
        fill(book,"last",bucket+5000,9100,300);fill(book,"first",bucket+1000,9000,100);fill(book,"high",bucket+3000,9300,200);fill(book,"last",bucket+5000,9100,300);
        JSONObject bar=game.getChartData("IDX:BBCA","R1D").getJSONArray("bars").getJSONObject(0);
        assertEquals(bucket,bar.getLong("timestamp"));assertEquals(9000,bar.getDouble("open"),0);assertEquals(9100,bar.getDouble("close"),0);assertEquals(9300,bar.getDouble("high"),0);assertEquals(9000,bar.getDouble("low"),0);assertEquals(600,bar.getLong("volume"));
    }
    @Test public void emptyRangeDoesNotInventCandlesOrBorrowHistoricalDataset() throws Exception {
        TradingFacade game=game();
        for(String range:new String[]{"R6H","R1D","R7D","R30D"}){
            JSONObject chart=game.getChartData("IDX:BBCA",range);assertEquals(0,chart.getJSONArray("bars").length());assertEquals(0,chart.getInt("totalTrades"));assertEquals("EMPTY",chart.getString("coverage"));
        }
    }

    @Test public void exactBucketBoundariesAndPartialCandleMarkers() throws Exception {
        TradingFacade game = game();
        long now = game.getGameTimestamp();
        OrderBook book = game.getMarketEngine().getOrCreateBook(game.getCatalog().getById("IDX:BBCA"));
        long duration = 86400000L;
        long start = now - duration;

        fill(book, "out-before", start - 1000L, 8900, 50);
        fill(book, "at-start", start, 9000, 100);
        fill(book, "mid", now - 3600000L, 9100, 200);
        fill(book, "at-end", now, 9200, 300);
        fill(book, "out-after", now + 1000L, 9300, 400);

        JSONObject chart = game.getChartData("IDX:BBCA", "R1D");
        assertEquals(3, chart.getInt("totalTrades"));
        JSONArray bars = chart.getJSONArray("bars");
        long totalVol = 0;
        for (int i = 0; i < bars.length(); i++) {
            totalVol += bars.getJSONObject(i).getLong("volume");
        }
        assertEquals(600, totalVol);
    }

    @Test public void gapsAcrossWeekendsAndSessionClosuresPreservedWithoutPhantomBars() throws Exception {
        TradingFacade game = game();
        long now = game.getGameTimestamp();
        OrderBook book = game.getMarketEngine().getOrCreateBook(game.getCatalog().getById("IDX:BBCA"));

        fill(book, "fri-1", now - 4L * 86400000L, 9000, 100);
        fill(book, "fri-2", now - 4L * 86400000L + 10000L, 9050, 150);
        fill(book, "mon-1", now - 1L * 86400000L, 9100, 200);

        JSONObject chart = game.getChartData("IDX:BBCA", "R7D");
        JSONArray bars = chart.getJSONArray("bars");
        assertEquals(2, bars.length());
        assertEquals(250, bars.getJSONObject(0).getLong("volume"));
        assertEquals(200, bars.getJSONObject(1).getLong("volume"));
    }

    @Test public void crossInstrumentAndCurrencyHandling() throws Exception {
        TradingFacade game = game();
        long now = game.getGameTimestamp();
        OrderBook bbcaBook = game.getMarketEngine().getOrCreateBook(game.getCatalog().getById("IDX:BBCA"));
        OrderBook aaplBook = game.getMarketEngine().getOrCreateBook(game.getCatalog().getById("US:AAPL"));

        bbcaBook.recordFill(new TradeFill("fill-bbca", "BUY", "SELL", "B", "S", "IDX:BBCA", Money.of(9100, Currency.IDR), 100, now - 1000L));
        aaplBook.recordFill(new TradeFill("fill-aapl", "BUY", "SELL", "B", "S", "US:AAPL", Money.of(220, Currency.USD), 10, now - 1000L));

        JSONObject bbcaChart = game.getChartData("IDX:BBCA", "R1D");
        assertEquals("IDR", bbcaChart.getString("currency"));
        assertEquals(100, bbcaChart.getInt("lotSize"));
        assertEquals(100, bbcaChart.getJSONArray("bars").getJSONObject(0).getLong("volume"));

        JSONObject aaplChart = game.getChartData("US:AAPL", "R1D");
        assertEquals("USD", aaplChart.getString("currency"));
        assertEquals(1, aaplChart.getInt("lotSize"));
        assertEquals(10, aaplChart.getJSONArray("bars").getJSONObject(0).getLong("volume"));
    }

    @Test public void saveAndRestorePreservesChartDataConsistently() throws Exception {
        java.io.File dir = temp.newFolder();
        TradingFacade game1 = new TradingFacade(SaveManager.forDirectory(dir));
        game1.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")), java.nio.charset.StandardCharsets.UTF_8)));
        game1.setRealTimeClock(java.time.Clock.fixed(java.time.Instant.ofEpochMilli(1792756800000L), java.time.ZoneOffset.UTC));
        assertTrue(game1.startOrResume());

        long now = game1.getGameTimestamp();
        OrderBook book1 = game1.getMarketEngine().getOrCreateBook(game1.getCatalog().getById("IDX:BBCA"));
        fill(book1, "save-1", now - 3600000L, 9000, 150);
        fill(book1, "save-2", now - 1800000L, 9200, 250);

        JSONObject beforeSave = game1.getChartData("IDX:BBCA", "R1D");
        assertEquals(2, beforeSave.getInt("totalTrades"));
        game1.save();

        TradingFacade game2 = new TradingFacade(SaveManager.forDirectory(dir));
        game2.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")), java.nio.charset.StandardCharsets.UTF_8)));
        game2.setRealTimeClock(java.time.Clock.fixed(java.time.Instant.ofEpochMilli(1792756800000L), java.time.ZoneOffset.UTC));
        assertTrue(game2.startOrResume());

        JSONObject afterRestore = game2.getChartData("IDX:BBCA", "R1D");
        assertEquals(beforeSave.getInt("totalTrades"), afterRestore.getInt("totalTrades"));
        assertEquals(beforeSave.getJSONArray("bars").length(), afterRestore.getJSONArray("bars").length());
        assertEquals(beforeSave.getJSONArray("bars").getJSONObject(0).getDouble("close"),
                afterRestore.getJSONArray("bars").getJSONObject(0).getDouble("close"), 0.001);
    }
}
