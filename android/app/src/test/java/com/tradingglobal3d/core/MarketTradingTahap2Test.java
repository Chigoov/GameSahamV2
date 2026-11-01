package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketCandleDto;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.research.MarketEvent;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Pengujian Tahap 2: Candlestick OHLCV DTO, MarketEvent Sentimen Makro,
 * dan Eksekusi Perdagangan Saham.
 */
public class MarketTradingTahap2Test {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "tahap2_test_" + UUID.randomUUID());
        testDir.mkdirs();

        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) {
            universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        }
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(InstrumentCatalog.fromJsonString(jsonUniverse));
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
    }

    @After
    public void tearDown() {
        if (testDir != null && testDir.exists()) {
            File[] files = testDir.listFiles();
            if (files != null) for (File f : files) f.delete();
            testDir.delete();
        }
    }

    @Test
    public void testMarketCandleDtoCreationAndSerialization() throws Exception {
        long now = System.currentTimeMillis();
        MarketCandleDto greenCandle = new MarketCandleDto("IDX:BBCA", now, 10000.0, 10250.0, 9950.0, 10200.0, 500000);
        assertTrue("Close > Open harus hijau", greenCandle.isGreen());
        assertEquals(500000L, greenCandle.getVolume());

        JSONObject json = greenCandle.toJsonObject();
        assertEquals("IDX:BBCA", json.getString("ticker"));
        assertEquals(10200.0, json.getDouble("close"), 0.001);
        assertTrue(json.getBoolean("isGreen"));

        MarketCandleDto redCandle = new MarketCandleDto("IDX:BMRI", now, 7000.0, 7050.0, 6800.0, 6850.0, 300000);
        assertFalse("Close < Open harus merah", redCandle.isGreen());
    }

    @Test
    public void testMarketEventsMacroImpact() throws Exception {
        String eventsJsonStr = facade.getMarketEventsJson();
        JSONArray arr = new JSONArray(eventsJsonStr);
        assertTrue("Harus ada minimal 3 market event makro per sesi", arr.length() >= 3);

        boolean foundFinance = false;
        boolean foundEnergy = false;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject ev = arr.getJSONObject(i);
            String sector = ev.getString("targetSector");
            double mult = ev.getDouble("multiplier");
            if ("Keuangan".equalsIgnoreCase(sector)) {
                foundFinance = true;
                assertTrue("Multiplier Keuangan harus positif (>1.0)", mult > 1.0);
            }
            if ("Energi".equalsIgnoreCase(sector)) {
                foundEnergy = true;
                assertTrue("Multiplier Energi harus sangat positif (>=1.05)", mult >= 1.05);
            }
        }
        assertTrue("Harus ada event sektor Keuangan", foundFinance);
        assertTrue("Harus ada event sektor Energi", foundEnergy);
    }

    @Test
    public void testGetChartDataOutput() throws Exception {
        JSONObject chartData = facade.getChartData("IDX:BBCA", "D1");
        assertTrue(chartData.getBoolean("success"));
        assertEquals("IDX:BBCA", chartData.getString("instrumentId"));
        assertTrue(chartData.has("bars"));
    }
}
