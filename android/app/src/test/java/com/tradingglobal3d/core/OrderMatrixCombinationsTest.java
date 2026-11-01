package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import com.tradingglobal3d.mobile.TradingWebBridge;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class OrderMatrixCombinationsTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private TradingFacade createGame() throws Exception {
        SaveManager saves = SaveManager.forDirectory(temp.newFolder());
        TradingFacade g = new TradingFacade(saves);
        g.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")), java.nio.charset.StandardCharsets.UTF_8)));
        g.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("100000000"));
        g.getClock().advanceGameSeconds(1800); // 09:00 WIB
        return g;
    }

    @Test
    public void testMatrixSidesAndOrderTypesOnIdxAndUs() throws Exception {
        TradingFacade g = createGame();
        TradingWebBridge bridge = new TradingWebBridge(g);
        String personalId = g.getPersonalAccount().getId();

        // 1. BUY x 4 Types on IDX:BBCA (lotSize 100, IDR)
        String[] types = {"MARKET", "LIMIT", "STOP_LOSS", "TAKE_PROFIT"};
        for (String type : types) {
            JSONObject req = new JSONObject()
                    .put("accountId", personalId)
                    .put("instrumentId", "IDX:BBCA")
                    .put("side", "BUY")
                    .put("type", type)
                    .put("quantity", 100)
                    .put("requestId", "REQ-BUY-IDX-" + type);

            if (type.equals("LIMIT")) req.put("limitPrice", "8000");
            else if (type.equals("STOP_LOSS") || type.equals("TAKE_PROFIT")) req.put("triggerPrice", "11000");

            JSONObject res = new JSONObject(bridge.submitOrder(req.toString()));
            System.out.println("IDX " + type + " result: " + res);
            assertTrue("Submit BUY IDX " + type + " harus sukses: " + res.optString("message"), res.getBoolean("success"));
            String status = res.getString("status");
            if (type.equals("STOP_LOSS") || type.equals("TAKE_PROFIT")) {
                assertEquals("PENDING_TRIGGER", status);
            } else {
                assertTrue(status.equals("OPEN") || status.equals("FILLED") || status.equals("PARTIALLY_FILLED") || status.equals("CANCELLED"));
            }
        }

        // 2. Kasus Tidak Didukung: Sektor US terkunci pada Level 1 (butuh Level 7)
        JSONObject reqLockedUS = new JSONObject()
                .put("accountId", personalId)
                .put("instrumentId", "US:JPM")
                .put("side", "BUY")
                .put("type", "LIMIT")
                .put("quantity", 1)
                .put("limitPrice", "200")
                .put("requestId", "REQ-LOCKED-US-LEVEL1");
        JSONObject resLocked = new JSONObject(bridge.submitOrder(reqLockedUS.toString()));
        assertFalse("Transaksi saham US pada Level 1 harus ditolak", resLocked.getBoolean("success"));
        assertTrue(resLocked.getString("message").contains("terkunci"));

        // 3. Naikkan level pemain ke Level 7+ untuk membuka bursa US
        JSONObject state = new JSONObject()
                .put("xp", 2500)
                .put("missionXpAwards", new JSONObject().put("QA-MISSION-XP", 2500));
        g.getProgressManager().restoreState(state);
        assertTrue("Level harus >= 7", g.getProgressManager().getCurrentLevel() >= 7);
        g.getPersonalAccount().creditCash(Money.of(10000, Currency.USD));

        for (String type : types) {
            JSONObject req = new JSONObject()
                    .put("accountId", personalId)
                    .put("instrumentId", "US:JPM")
                    .put("side", "BUY")
                    .put("type", type)
                    .put("quantity", 5)
                    .put("requestId", "REQ-BUY-US-" + type);

            if (type.equals("LIMIT")) req.put("limitPrice", "200");
            else if (type.equals("STOP_LOSS") || type.equals("TAKE_PROFIT")) req.put("triggerPrice", "250");

            JSONObject res = new JSONObject(bridge.submitOrder(req.toString()));
            assertTrue("Submit BUY US " + type + " pada Level 7+ harus sukses: " + res.optString("message"), res.getBoolean("success"));
        }

        // 4. SELL tanpa posisi harus ditolak
        JSONObject sellNoPos = new JSONObject()
                .put("accountId", personalId)
                .put("instrumentId", "IDX:TLKM")
                .put("side", "SELL")
                .put("type", "LIMIT")
                .put("quantity", 100)
                .put("limitPrice", "4000")
                .put("requestId", "REQ-SELL-NO-POS");
        JSONObject resNoPos = new JSONObject(bridge.submitOrder(sellNoPos.toString()));
        assertEquals("REJECTED", resNoPos.getString("status"));

        // 5. Matriks Lengkap SELL x 4 Types pada IDX (TLKM) dengan posisi
        g.getPersonalAccount().addPosition(new Position("TLKM", "IDX", Currency.IDR, 1000, new BigDecimal("3800"), new BigDecimal("3800")));
        for (String type : types) {
            JSONObject req = new JSONObject()
                    .put("accountId", personalId)
                    .put("instrumentId", "IDX:TLKM")
                    .put("side", "SELL")
                    .put("type", type)
                    .put("quantity", 100)
                    .put("requestId", "REQ-SELL-IDX-" + type);

            if (type.equals("LIMIT")) req.put("limitPrice", "4200");
            else if (type.equals("STOP_LOSS") || type.equals("TAKE_PROFIT")) req.put("triggerPrice", "3500");

            JSONObject res = new JSONObject(bridge.submitOrder(req.toString()));
            assertTrue("Submit SELL IDX " + type + " dengan posisi harus sukses: " + res.optString("message"), res.getBoolean("success"));
            String status = res.getString("status");
            if (type.equals("STOP_LOSS") || type.equals("TAKE_PROFIT")) {
                assertEquals("PENDING_TRIGGER", status);
            } else {
                assertTrue(status.equals("OPEN") || status.equals("FILLED") || status.equals("PARTIALLY_FILLED") || status.equals("CANCELLED"));
            }
        }

        // 6. Matriks Lengkap SELL x 4 Types pada US (AAPL) dengan posisi
        g.getPersonalAccount().addPosition(new Position("AAPL", "US", Currency.USD, 100, new BigDecimal("150"), new BigDecimal("150")));
        for (String type : types) {
            JSONObject req = new JSONObject()
                    .put("accountId", personalId)
                    .put("instrumentId", "US:AAPL")
                    .put("side", "SELL")
                    .put("type", type)
                    .put("quantity", 5)
                    .put("requestId", "REQ-SELL-US-" + type);

            if (type.equals("LIMIT")) req.put("limitPrice", "160");
            else if (type.equals("STOP_LOSS") || type.equals("TAKE_PROFIT")) req.put("triggerPrice", "140");

            JSONObject res = new JSONObject(bridge.submitOrder(req.toString()));
            assertTrue("Submit SELL US " + type + " dengan posisi harus sukses: " + res.optString("message"), res.getBoolean("success"));
            String status = res.getString("status");
            if (type.equals("STOP_LOSS") || type.equals("TAKE_PROFIT")) {
                assertEquals("PENDING_TRIGGER", status);
            } else {
                assertTrue(status.equals("OPEN") || status.equals("FILLED") || status.equals("PARTIALLY_FILLED") || status.equals("CANCELLED"));
            }
        }

        // 7. Idempotency: pengiriman ulang requestId yang sama menghasilkan status yang sama tanpa duplikasi
        JSONObject sellReq = new JSONObject()
                .put("accountId", personalId)
                .put("instrumentId", "IDX:TLKM")
                .put("side", "SELL")
                .put("type", "LIMIT")
                .put("quantity", 100)
                .put("limitPrice", "4200")
                .put("requestId", "REQ-SELL-IDEMP-1");
        JSONObject res1 = new JSONObject(bridge.submitOrder(sellReq.toString()));
        JSONObject retry = new JSONObject(bridge.submitOrder(sellReq.toString()));
        assertEquals(res1.getString("status"), retry.getString("status"));
        assertEquals(res1.getString("orderId"), retry.getString("orderId"));
    }

    @Test
    public void testClientAccountTradingAndCareerGating() throws Exception {
        TradingFacade g = createGame();
        TradingWebBridge bridge = new TradingWebBridge(g);
        String personalId = g.getPersonalAccount().getId();
        Money personalCashBefore = g.getPersonalAccount().getTotalCash(Currency.IDR);

        // 1. Akun klien fiktif tanpa mandat -> akses ditolak
        JSONObject reqUnauthorized = new JSONObject()
                .put("accountId", "ACC-CLIENT-FAKE")
                .put("instrumentId", "IDX:BBCA")
                .put("side", "BUY")
                .put("type", "LIMIT")
                .put("quantity", 100)
                .put("limitPrice", "8000")
                .put("requestId", "REQ-CLIENT-UNAUTH");
        JSONObject resUnauth = new JSONObject(bridge.submitOrder(reqUnauthorized.toString()));
        assertFalse(resUnauth.getBoolean("success"));

        // Buka jabatan Portfolio Manager untuk mengizinkan penerimaan mandat klien
        GameTestSupport.unlockManager(g);

        // 2. Akun klien sah terdaftar dengan mandat
        Account clientAccount = g.acceptClientMandate("MANDATE-01");
        assertNotNull("Akun klien sah harus berhasil dibuat", clientAccount);
        String clientAccountId = clientAccount.getId();
        Money clientInitialCash = clientAccount.getTotalCash(Currency.IDR);
        assertTrue("Klien harus memiliki saldo awal IDR positif", clientInitialCash.isPositive());

        // 3. Matriks Lengkap BUY x 4 Types pada IDX (IDX:BBCA) untuk Akun Klien Sah
        String[] allTypes = {"MARKET", "LIMIT", "STOP_LOSS", "TAKE_PROFIT"};
        for (String type : allTypes) {
            Money personalBeforeStep = g.getPersonalAccount().getTotalCash(Currency.IDR);
            Money clientCashBeforeStep = clientAccount.getTotalCash(Currency.IDR);

            JSONObject req = new JSONObject()
                    .put("accountId", clientAccountId)
                    .put("instrumentId", "IDX:BBCA")
                    .put("side", "BUY")
                    .put("type", type)
                    .put("quantity", 100)
                    .put("requestId", "REQ-CLI-BUY-IDX-" + type);

            if (type.equals("LIMIT")) req.put("limitPrice", "8000");
            else if (type.equals("STOP_LOSS")) req.put("triggerPrice", "11000");
            else if (type.equals("TAKE_PROFIT")) req.put("triggerPrice", "7500");

            JSONObject res = new JSONObject(bridge.submitOrder(req.toString()));
            assertTrue("Submit BUY IDX " + type + " untuk akun klien harus berhasil: " + res.optString("message"), res.getBoolean("success"));
            if (type.equals("STOP_LOSS") || type.equals("TAKE_PROFIT")) {
                assertEquals("PENDING_TRIGGER", res.getString("status"));
            } else if (type.equals("LIMIT")) {
                assertTrue("Kas klien harus ter-reserve untuk order limit", clientAccount.getReservedCash(Currency.IDR).isPositive());
            }

            // Zero leakage: Kas manajer pribadi 100% tidak terpengaruh
            assertEquals("Kas manajer tidak boleh bocor pada BUY " + type, personalBeforeStep, g.getPersonalAccount().getTotalCash(Currency.IDR));

            // Retry idempoten pada request yang sama
            JSONObject retry = new JSONObject(bridge.submitOrder(req.toString()));
            assertEquals("Retry idempoten harus menghasilkan status yang sama", res.getString("status"), retry.getString("status"));
        }

        // 4. Matriks Lengkap SELL x 4 Types pada IDX (IDX:TLKM) untuk Akun Klien Sah (dengan posisi, tanpa self-crossing)
        clientAccount.addPosition(new Position("TLKM", "IDX", Currency.IDR, 2000, new BigDecimal("3800"), new BigDecimal("3800")));
        for (String type : allTypes) {
            Money personalBeforeStep = g.getPersonalAccount().getTotalCash(Currency.IDR);

            JSONObject req = new JSONObject()
                    .put("accountId", clientAccountId)
                    .put("instrumentId", "IDX:TLKM")
                    .put("side", "SELL")
                    .put("type", type)
                    .put("quantity", 100)
                    .put("requestId", "REQ-CLI-SELL-IDX-" + type);

            if (type.equals("LIMIT")) req.put("limitPrice", "4200");
            else if (type.equals("STOP_LOSS")) req.put("triggerPrice", "3500");
            else if (type.equals("TAKE_PROFIT")) req.put("triggerPrice", "4500");

            JSONObject res = new JSONObject(bridge.submitOrder(req.toString()));
            assertTrue("Submit SELL IDX " + type + " akun klien harus berhasil: " + res.optString("message"), res.getBoolean("success"));
            if (type.equals("STOP_LOSS") || type.equals("TAKE_PROFIT")) {
                assertEquals("PENDING_TRIGGER", res.getString("status"));
            }
            assertEquals("Kas manajer tidak boleh bocor pada SELL " + type, personalBeforeStep, g.getPersonalAccount().getTotalCash(Currency.IDR));
        }

        // 5. Matriks Lengkap Akun Klien pada Bursa US (US:AAPL) setelah unlock level
        JSONObject levelState = new JSONObject().put("xp", 3000).put("missionXpAwards", new JSONObject().put("QA-XP", 3000));
        g.getProgressManager().restoreState(levelState);
        clientAccount.creditCash(Money.of(50000, Currency.USD));
        Money personalUsdBefore = g.getPersonalAccount().getTotalCash(Currency.USD);

        for (String type : allTypes) {
            JSONObject req = new JSONObject()
                    .put("accountId", clientAccountId)
                    .put("instrumentId", "US:AAPL")
                    .put("side", "BUY")
                    .put("type", type)
                    .put("quantity", 2)
                    .put("requestId", "REQ-CLI-BUY-US-" + type);

            if (type.equals("LIMIT")) req.put("limitPrice", "150");
            else if (type.equals("STOP_LOSS")) req.put("triggerPrice", "200");
            else if (type.equals("TAKE_PROFIT")) req.put("triggerPrice", "120");

            JSONObject res = new JSONObject(bridge.submitOrder(req.toString()));
            assertTrue("Submit BUY US " + type + " akun klien harus berhasil: " + res.optString("message"), res.getBoolean("success"));
            assertEquals("Kas USD pribadi manajer harus 100% terisolasi", personalUsdBefore, g.getPersonalAccount().getTotalCash(Currency.USD));
        }

        // SELL US x 4 Types pada Akun Klien (menggunakan US:JPM agar tidak self-crossing dengan BUY AAPL)
        clientAccount.addPosition(new Position("JPM", "US", Currency.USD, 100, new BigDecimal("200"), new BigDecimal("200")));
        for (String type : allTypes) {
            JSONObject req = new JSONObject()
                    .put("accountId", clientAccountId)
                    .put("instrumentId", "US:JPM")
                    .put("side", "SELL")
                    .put("type", type)
                    .put("quantity", 2)
                    .put("requestId", "REQ-CLI-SELL-US-" + type);

            if (type.equals("LIMIT")) req.put("limitPrice", "210");
            else if (type.equals("STOP_LOSS")) req.put("triggerPrice", "180");
            else if (type.equals("TAKE_PROFIT")) req.put("triggerPrice", "220");

            JSONObject res = new JSONObject(bridge.submitOrder(req.toString()));
            assertTrue("Submit SELL US " + type + " akun klien harus berhasil: " + res.optString("message"), res.getBoolean("success"));
            assertEquals("Kas USD pribadi manajer harus tetap terisolasi", personalUsdBefore, g.getPersonalAccount().getTotalCash(Currency.USD));
        }

        // 6. Penolakan yang sah pada akun klien: SELL tanpa posisi harus ditolak
        JSONObject sellNoPosClient = new JSONObject()
                    .put("accountId", clientAccountId)
                    .put("instrumentId", "IDX:ASII")
                    .put("side", "SELL")
                    .put("type", "LIMIT")
                    .put("quantity", 100)
                    .put("limitPrice", "5000")
                    .put("requestId", "REQ-CLI-SELL-NO-POS");
        JSONObject resNoPosClient = new JSONObject(bridge.submitOrder(sellNoPosClient.toString()));
        assertEquals("SELL tanpa posisi pada akun klien harus REJECTED", "REJECTED", resNoPosClient.getString("status"));

        // 7. Penolakan yang sah: BUY dengan kuantitas melampaui kas klien
        JSONObject buyOverCashClient = new JSONObject()
                    .put("accountId", clientAccountId)
                    .put("instrumentId", "IDX:BBCA")
                    .put("side", "BUY")
                    .put("type", "LIMIT")
                    .put("quantity", 1000000)
                    .put("limitPrice", "50000")
                    .put("requestId", "REQ-CLI-BUY-OVER-CASH");
        JSONObject resOverCashClient = new JSONObject(bridge.submitOrder(buyOverCashClient.toString()));
        assertEquals("BUY melampaui kas klien harus REJECTED", "REJECTED", resOverCashClient.getString("status"));

        // Verifikasi akhir: Kas manajer tetap utuh dari awal sampai akhir
        assertEquals("Kas pribadi manajer harus tetap sama persis dari awal pengujian", personalCashBefore, g.getPersonalAccount().getTotalCash(Currency.IDR));
    }

    @Test
    public void testZeroCashAndInvalidParametersRejected() throws Exception {
        TradingFacade g = createGame();
        TradingWebBridge bridge = new TradingWebBridge(g);
        String personalId = g.getPersonalAccount().getId();

        // Kuantitas nol atau negatif ditolak
        JSONObject reqZeroQty = new JSONObject()
                .put("accountId", personalId)
                .put("instrumentId", "IDX:BBCA")
                .put("side", "BUY")
                .put("type", "LIMIT")
                .put("quantity", 0)
                .put("limitPrice", "9000")
                .put("requestId", "REQ-ZERO-QTY");
        JSONObject resZero = new JSONObject(bridge.submitOrder(reqZeroQty.toString()));
        assertFalse(resZero.getBoolean("success"));

        // Harga limit negatif ditolak
        JSONObject reqNegPrice = new JSONObject()
                .put("accountId", personalId)
                .put("instrumentId", "IDX:BBCA")
                .put("side", "BUY")
                .put("type", "LIMIT")
                .put("quantity", 100)
                .put("limitPrice", "-5000")
                .put("requestId", "REQ-NEG-PRICE");
        JSONObject resNeg = new JSONObject(bridge.submitOrder(reqNegPrice.toString()));
        assertFalse(resNeg.getBoolean("success"));

        // Saldo tidak cukup untuk order besar -> REJECTED
        JSONObject reqOverCash = new JSONObject()
                .put("accountId", personalId)
                .put("instrumentId", "IDX:BBCA")
                .put("side", "BUY")
                .put("type", "LIMIT")
                .put("quantity", 1000000)
                .put("limitPrice", "100000")
                .put("requestId", "REQ-OVER-CASH");
        JSONObject resOver = new JSONObject(bridge.submitOrder(reqOverCash.toString()));
        assertEquals("REJECTED", resOver.getString("status"));
    }
}
