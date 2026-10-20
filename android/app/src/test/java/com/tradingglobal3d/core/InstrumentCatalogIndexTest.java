package com.tradingglobal3d.core;

import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.market.MarketIndex;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class InstrumentCatalogIndexTest {
    private InstrumentCatalog catalog;
    private String jsonUniverseData;
    private String jsonManifestData;

    @Before
    public void setUp() throws Exception {
        File dataDir = new File("src/main/assets/data");
        File universeFile = new File(dataDir, "market_universe.json");
        File manifestFile = new File(dataDir, "dataset_manifest.json");

        assertTrue("File market_universe.json harus ada", universeFile.exists());
        assertTrue("File dataset_manifest.json harus ada", manifestFile.exists());

        jsonUniverseData = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        jsonManifestData = new String(Files.readAllBytes(manifestFile.toPath()), StandardCharsets.UTF_8);

        catalog = InstrumentCatalog.fromJsonString(jsonUniverseData);
    }

    @Test
    public void testCrossExchangeUniqueIdentification() {
        // Cek IDX vs US
        Instrument bbca = catalog.getById("IDX:BBCA");
        assertNotNull(bbca);
        assertEquals("BBCA", bbca.getTicker());
        assertEquals(MarketExchange.IDX, bbca.getExchange());
        assertEquals(Currency.IDR, bbca.getCurrency());
        assertEquals(100, bbca.getLotSize());

        Instrument aapl = catalog.getById("US:AAPL");
        assertNotNull(aapl);
        assertEquals("AAPL", aapl.getTicker());
        assertEquals(MarketExchange.US, aapl.getExchange());
        assertEquals(Currency.USD, aapl.getCurrency());
        assertEquals(1, aapl.getLotSize());

        // Tidak boleh tertukar jika mencari lewat getByExchangeAndTicker
        assertSame(bbca, catalog.getByExchangeAndTicker(MarketExchange.IDX, "BBCA"));
        assertNull(catalog.getByExchangeAndTicker(MarketExchange.US, "BBCA"));
        assertSame(aapl, catalog.getByExchangeAndTicker(MarketExchange.US, "AAPL"));
        assertNull(catalog.getByExchangeAndTicker(MarketExchange.IDX, "AAPL"));
    }

    @Test
    public void testActiveUniverseCountAndPassiveIsolation() {
        List<Instrument> activeList = catalog.getActiveUniverse();
        // 10 IDX + 10 US = 20 instrumen aktif
        assertEquals(20, activeList.size());

        for (Instrument inst : activeList) {
            assertTrue("Seluruh instrumen di active universe harus tradable", inst.isActiveTrading());
            assertTrue(inst.isTradable());
        }

        // Cek instrumen pasif
        Instrument bbni = catalog.getById("IDX:BBNI");
        assertNotNull(bbni);
        assertFalse("BBNI adalah katalog pasif", bbni.isActiveTrading());
        assertFalse("Instrumen pasif tidak boleh tradable", bbni.isTradable());

        Instrument dis = catalog.getById("US:DIS");
        assertNotNull(dis);
        assertFalse(dis.isActiveTrading());
        assertFalse(dis.isTradable());
    }

    @Test
    public void testSearchAndFiltering() {
        // Cari "Bank" di IDX
        List<Instrument> banks = catalog.search("Bank", MarketExchange.IDX, null, true);
        assertTrue(banks.size() >= 3); // BBCA, BMRI, BBRI

        // Filter sektor "Technology" di US
        List<Instrument> usTech = catalog.search(null, MarketExchange.US, "Technology", true);
        assertFalse(usTech.isEmpty());
        for (Instrument t : usTech) {
            assertEquals(MarketExchange.US, t.getExchange());
            assertTrue(t.getSector().contains("Technology"));
        }

        // Filter pasif saja
        List<Instrument> passiveOnly = catalog.search(null, null, null, false);
        assertEquals(6, passiveOnly.size()); // 3 IDX + 3 US pasif
    }

    @Test
    public void testMarketIndexCalculation() {
        MarketIndex ihsg = catalog.getIndex("IDX:COMPOSITE");
        assertNotNull(ihsg);
        assertEquals("IHSG", ihsg.getTicker());
        assertEquals(7700.0, ihsg.getBaseValue(), 0.001);

        double initialValue = ihsg.getCurrentValue();
        assertTrue(initialValue > 7000.0);

        // Simulasikan kenaikan harga BBCA sebesar 10%
        Instrument bbca = catalog.getById("IDX:BBCA");
        Money oldPrice = bbca.getCurrentPrice();
        Money newPrice = oldPrice.multiply(1.10);
        bbca.updatePrice(newPrice, 100000);

        // Hitung ulang indeks
        catalog.recalculateAllIndices();
        double updatedValue = ihsg.getCurrentValue();

        assertTrue("IHSG harus naik saat saham kapitalisasi besar seperti BBCA naik", updatedValue > initialValue);
    }

    @Test
    public void testWatchlistOperations() {
        assertTrue(catalog.getWatchlist().isEmpty());

        assertTrue(catalog.addToWatchlist("IDX:BBCA"));
        assertTrue(catalog.addToWatchlist("US:NVDA"));
        assertFalse("Duplikat tidak boleh menambah count", catalog.addToWatchlist("IDX:BBCA"));

        List<Instrument> watchlist = catalog.getWatchlist();
        assertEquals(2, watchlist.size());
        assertTrue(catalog.isInWatchlist("IDX:BBCA"));
        assertTrue(catalog.isInWatchlist("US:NVDA"));
        assertFalse(catalog.isInWatchlist("IDX:GOTO"));

        assertTrue(catalog.removeFromWatchlist("IDX:BBCA"));
        assertEquals(1, catalog.getWatchlist().size());
        assertFalse(catalog.isInWatchlist("IDX:BBCA"));
    }

    @Test
    public void testDatasetManifestIntegrity() throws Exception {
        JSONObject manifest = new JSONObject(jsonManifestData);
        assertEquals("2.0.0", manifest.getString("manifestVersion"));
        assertTrue("Manifest harus menyatakan historicalDataAvailable saat dataset nyata terpasang",
                manifest.getBoolean("historicalDataAvailable"));
        assertEquals(2, manifest.getJSONArray("sourceFiles").length());

        JSONObject bbcaEntry = manifest.getJSONArray("sourceFiles").getJSONObject(0);
        assertEquals("IDX:BBCA", bbcaEntry.getString("id"));
        assertEquals("2c1edd8f03302af1a102c3f3633e531a22fd5e94f0bbe7716ae0794d57b9f4ff", bbcaEntry.getString("sha256"));
        assertTrue(bbcaEntry.getString("license").contains("CC BY-NC 4.0"));

        JSONObject aaplEntry = manifest.getJSONArray("sourceFiles").getJSONObject(1);
        assertEquals("US:AAPL", aaplEntry.getString("id"));
        assertEquals("13943669a9b86fdbff381678397c82c1809155453e97f0b854f1f8a57f97b465", aaplEntry.getString("sha256"));
        assertTrue(aaplEntry.getString("license").contains("Apache"));

        JSONObject universe = manifest.getJSONObject("universe");
        assertEquals(10, universe.getInt("activeCountIDX"));
        assertEquals(10, universe.getInt("activeCountUS"));
        assertEquals(6, universe.getInt("passiveCatalogCount"));
        assertEquals(3, universe.getInt("indicesCount"));
    }

    @Test
    public void testLoadRealHistoricalBars() throws Exception {
        File dataDir = new File("src/main/assets/data");
        File bbcaFile = new File(dataDir, "historical/idx_bbca_daily.csv");
        if (!bbcaFile.exists()) bbcaFile = new File("android/app/src/main/assets/data/historical/idx_bbca_daily.csv");

        List<com.tradingglobal3d.core.market.HistoricalBar> bbcaBars =
                com.tradingglobal3d.core.market.HistoricalDatasetLoader.loadFromCsvFile(bbcaFile);
        assertNotNull(bbcaBars);
        assertEquals(149, bbcaBars.size());

        // Verifikasi bar pertama dan bar terakhir
        com.tradingglobal3d.core.market.HistoricalBar first = bbcaBars.get(0);
        assertEquals(java.time.LocalDate.parse("2019-07-29"), first.getDate());
        assertTrue(first.getHigh().compareTo(first.getLow()) >= 0);

        File aaplFile = new File(dataDir, "historical/us_aapl_daily.csv");
        if (!aaplFile.exists()) aaplFile = new File("android/app/src/main/assets/data/historical/us_aapl_daily.csv");

        List<com.tradingglobal3d.core.market.HistoricalBar> aaplBars =
                com.tradingglobal3d.core.market.HistoricalDatasetLoader.loadFromCsvFile(aaplFile);
        assertNotNull(aaplBars);
        assertEquals(149, aaplBars.size());
    }

    @Test
    public void testRejectIllegalHistoricalBars() {
        // High < Low
        try {
            new com.tradingglobal3d.core.market.HistoricalBar(java.time.LocalDate.now(),
                    new java.math.BigDecimal("100"), new java.math.BigDecimal("90"),
                    new java.math.BigDecimal("110"), new java.math.BigDecimal("100"), 1000);
            fail("Harus menolak High < Low");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("High tidak boleh lebih rendah"));
        }

        // Tanggal mundur / duplikat
        String badCsv = "Date,Open,High,Low,Close,Volume\n" +
                "2023-01-02,100,105,95,102,1000\n" +
                "2023-01-01,102,106,98,103,1000\n";
        try {
            com.tradingglobal3d.core.market.HistoricalDatasetLoader.loadFromCsvString(badCsv);
            fail("Harus menolak tanggal yang mundur");
        } catch (Exception expected) {
            assertTrue(expected.getMessage().contains("tidak urut/mundur") || expected instanceof IllegalArgumentException);
        }
    }
}
