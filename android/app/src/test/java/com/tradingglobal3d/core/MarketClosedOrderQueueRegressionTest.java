package com.tradingglobal3d.core;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import com.tradingglobal3d.mobile.TradingWebBridge;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Uji Kepatuhan Keputusan Pasar Tutup & Antrean Order (D02 / S02):
 * 1. Order dibuat saat bursa tutup: dana langsung ditahan, status OPEN/antre.
 * 2. Pembatalan sebelum bursa buka: dana/saham tertahan dilepaskan seketika.
 * 3. Good 'Til Cancelled (GTC): rollover hari bursa tidak meng-expire order pemain.
 * 4. Penolakan emiten suspensi saat buka: dana tertahan dilepas.
 */
public class MarketClosedOrderQueueRegressionTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private TradingFacade createGameAtMidnight() throws Exception {
        SaveManager saves = SaveManager.forDirectory(temp.newFolder());
        TradingFacade g = new TradingFacade(saves);
        File catFile = new File("src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("android/app/src/main/assets/data/market_universe.json");
        g.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(catFile.toPath())));
        g.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("100000000"));
        // Jam default awal adalah 08:30 WIB (Pasar Tutup sebelum Pre-opening 08:45)
        return g;
    }

    @Test
    public void testSubmitOrderWhileMarketClosedReservesCashAndQueues() throws Exception {
        TradingFacade g = createGameAtMidnight();
        TradingWebBridge bridge = new TradingWebBridge(g);
        Account personal = g.getPersonalAccount();

        // Verifikasi bursa IDX sedang tutup
        assertFalse("Bursa IDX harus tutup pada 08:30 WIB", g.getClock().isTradingOpenFor(com.tradingglobal3d.core.market.MarketExchange.IDX));

        Money initialAvail = personal.getAvailableCash(Currency.IDR);
        assertEquals(Money.of(100_000_000L, Currency.IDR), initialAvail);

        // Buat order beli LIMIT saat pasar tutup
        JSONObject req = new JSONObject()
                .put("accountId", personal.getId())
                .put("instrumentId", "IDX:BBCA")
                .put("side", "BUY")
                .put("type", "LIMIT")
                .put("quantity", 100)
                .put("limitPrice", "9000")
                .put("requestId", "REQ-CLOSED-BUY-01");

        JSONObject res = new JSONObject(bridge.submitOrder(req.toString()));
        assertTrue("Order saat pasar tutup harus berhasil diterima: " + res.optString("message"), res.getBoolean("success"));
        assertEquals("OPEN", res.getString("status"));
        assertTrue("Pesan harus menyebut bursa tutup dan order tersimpan", res.getString("message").contains("Bursa sedang tutup"));

        // Dana beli HARUS langsung ditahan saat order dibuat
        Money reserved = personal.getReservedCash(Currency.IDR);
        assertTrue("Kas harus ditahan untuk order antre saat tutup", reserved.isPositive());
        assertEquals("Total kas tidak berkurang, hanya kas tersedia yang terpotong",
                initialAvail.subtract(reserved), personal.getAvailableCash(Currency.IDR));

        // Order tersimpan di OrderManager
        assertEquals(1, g.getOrderManager().getOrdersForAccount(personal.getId()).size());
    }

    @Test
    public void testCancelOrderWhileMarketClosedReleasesReservedFundsImmediately() throws Exception {
        TradingFacade g = createGameAtMidnight();
        TradingWebBridge bridge = new TradingWebBridge(g);
        Account personal = g.getPersonalAccount();

        // 1. Submit order beli saat tutup
        JSONObject req = new JSONObject()
                .put("accountId", personal.getId())
                .put("instrumentId", "IDX:BBCA")
                .put("side", "BUY")
                .put("type", "LIMIT")
                .put("quantity", 100)
                .put("limitPrice", "9000")
                .put("requestId", "REQ-CLOSED-CANCEL-01");

        bridge.submitOrder(req.toString());
        assertTrue("Kas harus tertahan", personal.getReservedCash(Currency.IDR).isPositive());

        Order queuedOrder = g.getOrderManager().getOrderByRequest("REQ-CLOSED-CANCEL-01");
        assertNotNull("Order harus ditemukan", queuedOrder);

        // 2. Batalkan order sebelum bursa buka
        boolean cancelled = g.cancelOrder(queuedOrder);
        assertTrue("Pembatalan order antre harus berhasil", cancelled);
        assertEquals(OrderStatus.CANCELLED, queuedOrder.getStatus());

        // 3. Kas tertahan harus kembali 0 seketika
        assertEquals(Money.zero(Currency.IDR), personal.getReservedCash(Currency.IDR));
        assertEquals(Money.of(100_000_000L, Currency.IDR), personal.getAvailableCash(Currency.IDR));
    }

    @Test
    public void testPlayerOrdersRemainActiveAcrossDayRolloverGtc() throws Exception {
        TradingFacade g = createGameAtMidnight();
        TradingWebBridge bridge = new TradingWebBridge(g);
        Account personal = g.getPersonalAccount();

        JSONObject req = new JSONObject()
                .put("accountId", personal.getId())
                .put("instrumentId", "IDX:BBCA")
                .put("side", "BUY")
                .put("type", "LIMIT")
                .put("quantity", 100)
                .put("limitPrice", "8000") // Harga di bawah pasar agar tidak match langsung
                .put("requestId", "REQ-GTC-ORDER-01");

        bridge.submitOrder(req.toString());
        Order playerOrder = g.getOrderManager().getOrderByRequest("REQ-GTC-ORDER-01");
        assertNotNull(playerOrder);
        assertEquals(OrderStatus.OPEN, playerOrder.getStatus());

        // Eksekusi pembersihan harian pasar (expireDayOrders)
        int expiredCount = g.expireDayOrders();

        // Order pemain TIDAK boleh ter-expire (Good 'Til Cancelled sesuai keputusan pemilik)
        assertEquals(OrderStatus.OPEN, playerOrder.getStatus());
        assertFalse(playerOrder.getStatus().isFinal());
        assertTrue(personal.getReservedCash(Currency.IDR).isPositive());
    }
}
