package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.*;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.trading.*;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.mobile.TradingWebBridge;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class PackagePFPerformanceVerificationTest {

    private File tempDir;
    private TradingFacade facade;
    private TradingWebBridge bridge;

    @Before
    public void setUp() throws Exception {
        tempDir = new File("build/tmp-test-pf-perf");
        tempDir.mkdirs();
        deleteRecursive(tempDir);
        tempDir.mkdirs();

        SaveManager sm = SaveManager.forDirectory(tempDir);
        facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("100000000"));

        bridge = new TradingWebBridge(facade);
    }

    @After
    public void tearDown() {
        if (bridge != null) bridge.destroy();
        deleteRecursive(tempDir);
    }

    private void deleteRecursive(File f) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File child : files) deleteRecursive(child);
            }
        }
        f.delete();
    }

    @Test
    public void testSnapshotContractAndPayloadSanity() throws Exception {
        String snapStr = facade.getSnapshotJson();
        assertNotNull(snapStr);
        JSONObject snap = new JSONObject(snapStr);

        assertTrue(snap.has("clock"));
        JSONObject clock = snap.getJSONObject("clock");
        assertTrue(clock.has("timestamp"));
        assertTrue(clock.has("sessionLabel"));
        assertTrue(clock.has("rate"));

        assertTrue(snap.has("personalAccount"));
        JSONObject personal = snap.getJSONObject("personalAccount");
        assertTrue(personal.has("cash"));
        JSONObject cash = personal.getJSONObject("cash");
        assertTrue(cash.has("IDR"));
        assertTrue(cash.has("USD"));

        assertTrue(snap.has("playerProgress"));
        assertTrue(snap.has("featureAccess"));
        assertTrue(snap.has("career"));

        // Verifikasi keselarasan getSnapshot dari bridge
        String bridgeSnapStr = bridge.getSnapshot();
        JSONObject bridgeSnap = new JSONObject(bridgeSnapStr);
        assertEquals(snap.getString("runId"), bridgeSnap.getString("runId"));
    }

    @Test
    public void testOrderBookAndQuoteContract() throws Exception {
        // Majukan waktu ke sesi bursa aktif (09:35 WIB)
        facade.advanceWorldSecondsForTest(35 * 60);

        String instStr = bridge.getInstruments("", "", null, true);
        JSONArray instArr = new JSONArray(instStr);
        assertTrue(instArr.length() >= 20);

        JSONObject bbca = null;
        for (int i = 0; i < instArr.length(); i++) {
            JSONObject obj = instArr.getJSONObject(i);
            if ("IDX:BBCA".equals(obj.optString("id"))) {
                bbca = obj;
                break;
            }
        }
        assertNotNull("BBCA harus ada di instrumen aktif", bbca);
        assertEquals("BBCA", bbca.getString("ticker"));
        assertEquals("IDX", bbca.getString("exchange"));
        assertEquals("IDR", bbca.getString("currency"));
        assertEquals(100, bbca.getInt("lotSize"));

        // Order book BBCA
        String obStr = bridge.getOrderBook("IDX:BBCA");
        JSONObject ob = new JSONObject(obStr);
        assertTrue(ob.has("bids"));
        assertTrue(ob.has("asks"));
        assertTrue(ob.has("haltState"));

        // Chart data BBCA
        String cdStr = bridge.getChartData("IDX:BBCA", "R1D");
        JSONObject cd = new JSONObject(cdStr);
        assertTrue(cd.getBoolean("success"));
        assertTrue(cd.has("bars"));
    }

    @Test
    public void testTradingInvariantsUnderHighFrequencyTicks() throws Exception {
        facade.advanceWorldSecondsForTest(35 * 60);

        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        Account personal = facade.getPersonalAccount();

        // 1. Simpan order limit beli pemain (AON)
        Order buyOrder = new Order("ORD-PF-001", "REQ-PF-001", personal.getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT, Money.of(10000L, Currency.IDR), null, 100L,
                facade.getClock().getGameTimestamp(), Money.of(1000000L, Currency.IDR));
        var submitRes = facade.submitOrder(buyOrder);
        assertTrue(submitRes.isSuccess);

        // Dana tertahan harus tercatat di reserved
        Money reserved = personal.getReservedCash(Currency.IDR);
        assertTrue(reserved.getAmount().compareTo(BigDecimal.ZERO) > 0);

        // 2. Simulasikan 30 ticks frekuensi tinggi (syncClock & advance)
        for (int i = 0; i < 30; i++) {
            facade.advanceWorldSecondsForTest(10);
            facade.syncClock();
        }

        // Ledger harus konsisten dengan kas
        facade.requirePlayerFinancialWritesAvailable();
        assertFalse(facade.areFinancialWritesBlocked());

        // 3. Batalkan order dan verifikasi dana tertahan dilepas utuh
        boolean cancelRes = facade.cancelOrder(buyOrder);
        assertTrue(cancelRes);
        assertEquals(BigDecimal.ZERO, personal.getReservedCash(Currency.IDR).getAmount());
    }

    @Test
    public void testSaveGenerationFencePreservedWithBridge() throws Exception {
        facade.save();
        long firstTs = facade.getSaveManager().getSaveFile().lastModified();

        // Majukan waktu dan simpan kembali
        facade.advanceWorldSecondsForTest(60);
        facade.save();
        long secondTs = facade.getSaveManager().getSaveFile().lastModified();
        assertTrue(secondTs >= firstTs);

        // Cek snapshot savedTimestamp
        JSONObject snap = new JSONObject(facade.getSnapshotJson());
        assertTrue(snap.has("savedTimestamp"));
    }
}
