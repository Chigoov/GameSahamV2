package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.npc.MarketEngine;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.TradeFill;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Uji Regresi Mandiri Plan 15 (B03):
 * Memverifikasi integritas penyimpanan, pemulihan dari jeda waktu besar,
 * deserialisasi order book dengan fee null, dan pemulihan IPO stock tanpa anomali.
 */
public class Plan15SaveAndRecoveryRegressionTest {

    private File tempDir;
    private SaveManager saveManager;
    private String catalogJson;

    @Before
    public void setUp() throws Exception {
        tempDir = Files.createTempDirectory("plan15-test-" + UUID.randomUUID()).toFile();
        saveManager = SaveManager.forDirectory(tempDir);

        File catFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("src/main/assets/data/market_universe.json");
        catalogJson = Files.readString(catFile.toPath());
    }

    @After
    public void tearDown() {
        if (tempDir != null && tempDir.exists()) {
            for (File f : tempDir.listFiles()) f.delete();
            tempDir.delete();
        }
    }

    @Test
    public void testSaveAndReloadPreservesAllAccountsAndLedger() throws Exception {
        TradingFacade game = new TradingFacade(saveManager);
        game.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("75000000"));

        Account player = game.getPersonalAccount();
        assertEquals(Money.of(75000000, Currency.IDR), player.getTotalCash(Currency.IDR));

        game.save();
        assertTrue("Save file harus tercipta", saveManager.getSaveFile().exists());

        TradingFacade reloaded = new TradingFacade(saveManager);
        reloaded.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
        assertTrue("Reload save harus sukses", reloaded.load());
        assertEquals(Money.of(75000000, Currency.IDR), reloaded.getPersonalAccount().getTotalCash(Currency.IDR));
        assertFalse("Tidak boleh membutuhkan recovery", reloaded.hasPendingBackupRecoveryOffer());
    }

    @Test
    public void testMarketEngineFillsWithNullFeesRestoreCleanly() throws Exception {
        MarketEngine engine = new MarketEngine();
        InstrumentCatalog cat = InstrumentCatalog.fromJsonString(catalogJson);
        Instrument bbca = cat.getById("IDX:BBCA");

        // Simulasikan state JSON dengan fee null
        JSONObject fillJson = new JSONObject();
        fillJson.put("id", "FILL-MOCK-001");
        fillJson.put("buyOrder", "ORD-B-1");
        fillJson.put("sellOrder", "ORD-S-1");
        fillJson.put("buyer", "ACC-BUYER");
        fillJson.put("seller", "ACC-SELLER");
        fillJson.put("price", "10000");
        fillJson.put("quantity", 100);
        fillJson.put("timestamp", System.currentTimeMillis());
        fillJson.put("sellerRealizedNet", JSONObject.NULL);
        fillJson.put("buyerFee", JSONObject.NULL);
        fillJson.put("sellerFee", JSONObject.NULL);

        JSONArray fillsArray = new JSONArray();
        fillsArray.put(fillJson);

        JSONObject bookItem = new JSONObject();
        bookItem.put("instrumentId", "IDX:BBCA");
        bookItem.put("halt", "NORMAL");
        bookItem.put("bids", new JSONArray());
        bookItem.put("asks", new JSONArray());
        bookItem.put("fills", fillsArray);
        bookItem.put("last", "10000");

        JSONArray booksArray = new JSONArray();
        booksArray.put(bookItem);

        // Deserialisasi harus berhasil tanpa melempar IllegalArgumentException
        engine.restoreState(booksArray, cat, new com.tradingglobal3d.core.trading.OrderManager());
        assertNotNull("Buku order BBCA harus pulih", engine.getBook("IDX:BBCA"));
        assertEquals(1, engine.getBook("IDX:BBCA").getTradeHistory().size());
        assertNull("Buyer fee harus null tanpa error", engine.getBook("IDX:BBCA").getTradeHistory().get(0).getBuyerFee());
    }

    @Test
    public void testIpoListedInstrumentRestoreShareSupplyWithoutEvents() throws Exception {
        InstrumentCatalog catalog = InstrumentCatalog.fromJsonString(catalogJson);
        JSONObject catalogState = catalog.exportState();
        JSONArray instruments = catalogState.getJSONArray("instruments");

        JSONObject supplyState = new JSONObject();
        supplyState.put("version", 1);
        supplyState.put("issuedShares", 123_275_050_000L);
        supplyState.put("treasuryShares", 0L);
        supplyState.put("events", new JSONObject());
        supplyState.put("eventOrder", new JSONArray());

        for (int i = 0; i < instruments.length(); i++) {
            JSONObject inst = instruments.getJSONObject(i);
            if ("IDX:BBCA".equals(inst.getString("id"))) {
                inst.put("shareSupply", supplyState);
                break;
            }
        }

        catalog.restoreState(catalogState);
        assertNotNull(catalog.getById("IDX:BBCA"));
        assertEquals(123_275_050_000L, catalog.getById("IDX:BBCA").getIssuedShares());
    }

    @Test
    public void testOfflineCatchupLargeGapDoesNotBlockOrCorruptState() throws Exception {
        TradingFacade game = new TradingFacade(saveManager);
        game.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));

        long fiveDaysRealMs = 5L * 24L * 3600L * 1000L; // 5 hari nyata = 120 hari game
        long t0 = System.currentTimeMillis();
        JSONObject report = game.reconcileOfflineElapsed(fiveDaysRealMs);
        long elapsed = System.currentTimeMillis() - t0;

        assertNotNull("Laporan catch-up harus ada", report);
        assertTrue("Waktu pemrosesan 5 hari nyata harus di bawah 15 detik", elapsed < 15000);
        game.save();

        TradingFacade reloaded = new TradingFacade(saveManager);
        reloaded.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
        assertTrue("Load harus sukses setelah offline catchup", reloaded.load());
        assertFalse("Tidak boleh membutuhkan recovery offer", reloaded.hasPendingBackupRecoveryOffer());
    }
}
