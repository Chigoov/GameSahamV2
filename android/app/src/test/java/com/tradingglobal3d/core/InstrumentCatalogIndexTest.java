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
        assertEquals("1.0.0", manifest.getString("manifestVersion"));

        JSONObject provenance = manifest.getJSONObject("provenance");
        assertFalse("A manifest without a sourced dataset cannot claim historical readiness",
                manifest.getBoolean("historicalDataAvailable"));
        assertEquals("UNAVAILABLE", provenance.getString("datasetType"));
        assertEquals("UNVERIFIED", provenance.getString("priceBasis"));
        assertEquals(0, manifest.getJSONArray("sourceFiles").length());
        assertTrue(manifest.getString("blockingReason").contains("OQ-03"));
        assertFalse("An unbound checksum is not dataset evidence", manifest.has("sha256"));

        JSONObject universe = manifest.getJSONObject("universe");
        assertEquals(10, universe.getInt("activeCountIDX"));
        assertEquals(10, universe.getInt("activeCountUS"));
        assertEquals(6, universe.getInt("passiveCatalogCount"));
        assertEquals(3, universe.getInt("indicesCount"));
    }
}
