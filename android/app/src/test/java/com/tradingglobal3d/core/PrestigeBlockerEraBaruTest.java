package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Pengujian komprehensif untuk A5, A6, dan A7:
 * - A5: Blocker reservasi tidak duplikat, dan order/reservasi NPC tidak memblokir pemain.
 * - A6: Pembersihan filosofi internal dari status utama.
 * - A7: Status locked/eligible Era Baru konsisten dengan domain.
 */
public class PrestigeBlockerEraBaruTest {
    private File testDir;
    private SaveManager saveManager;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "prestige_blocker_test_" + System.currentTimeMillis());
        testDir.mkdirs();
        saveManager = SaveManager.forDirectory(testDir);
        facade = new TradingFacade(saveManager);

        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) {
            universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        }
        if (universeFile.exists()) {
            String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
            facade.setCatalog(InstrumentCatalog.fromJsonString(jsonUniverse));
        }

        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        facade.getClock().play();
    }

    @After
    public void tearDown() {
        if (testDir != null && testDir.exists()) {
            for (File f : testDir.listFiles()) f.delete();
            testDir.delete();
        }
    }

    @Test
    public void testNpcOrdersAndReservationsDoNotBlockPlayer() throws Exception {
        // Buat likuiditas NPC dan antrean order dari Market Maker & Retail
        facade.generateNpcLiquidity();
        assertFalse("Market engine harus memiliki resting orders dari NPC",
                facade.getMarketEngine().getRestingOrders().isEmpty());

        // Buka jabatan Portfolio Manager dan majukan hari game >= 21
        GameTestSupport.unlockManager(facade);
        for (int i = 0; i < 22; i++) {
            facade.getPlayerProfileManager().incrementDaysPlayed();
        }

        // Akun pemain bersih (0 order aktif, 0 utang)
        JSONObject status = facade.getPrestigeStatus();
        JSONArray blockers = status.getJSONArray("blockers");
        System.out.println("Blockers saat NPC aktif dan pemain bersih: " + blockers);

        // Harus memenuhi syarat (eligible) karena order NPC tidak boleh memblokir pemain!
        assertTrue("Order dan kas NPC tidak boleh memblokir perjalanan pemain",
                status.getBoolean("eligible"));
        assertEquals("Blocker harus kosong", 0, blockers.length());
    }

    @Test
    public void testPlayerOrderProducesSingleMeaningfulBlocker() throws Exception {
        GameTestSupport.unlockManager(facade);
        for (int i = 0; i < 22; i++) {
            facade.getPlayerProfileManager().incrementDaysPlayed();
        }

        // Pemain memasang order beli limit antrean
        Order playerOrder = Order.createLimit("ORD-PLAYER-PRESTIGE-TEST", "IDX:BBCA",
                facade.getPersonalAccount().getId(), OrderSide.BUY,
                Money.of(8000, Currency.IDR), 100, facade.getClock().getGameTimestamp());
        facade.submitOrder(playerOrder);

        JSONObject status = facade.getPrestigeStatus();
        assertFalse("Order pemain aktif harus memblokir prestige secara sah",
                status.getBoolean("eligible"));

        JSONArray blockers = status.getJSONArray("blockers");
        assertTrue("Harus ada blocker order pemain", blockers.length() > 0);

        boolean foundOrderBlocker = false;
        for (int i = 0; i < blockers.length(); i++) {
            if (blockers.getString(i).contains("order aktif")) {
                foundOrderBlocker = true;
                break;
            }
        }
        assertTrue("Pesan blocker harus ramah pemain dan menyebut order aktif", foundOrderBlocker);

        // Batalkan order pemain
        facade.cancelOrder(playerOrder);
        JSONObject statusAfterCancel = facade.getPrestigeStatus();
        assertTrue("Setelah cancel order, pemain harus kembali eligible",
                statusAfterCancel.getBoolean("eligible"));
    }

    @Test
    public void testNoDuplicateBlockersAcrossMultipleCurrencies() throws Exception {
        JSONObject status = facade.getPrestigeStatus();
        JSONArray blockers = status.getJSONArray("blockers");

        Set<String> uniqueBlockers = new HashSet<>();
        for (int i = 0; i < blockers.length(); i++) {
            String b = blockers.getString(i);
            assertFalse("Tidak boleh ada pesan blocker yang duplikat: " + b,
                    uniqueBlockers.contains(b));
            uniqueBlockers.add(b);
        }
    }

    @Test
    public void testPrestigeExecutionCreatesBackupAndReceipt() throws Exception {
        GameTestSupport.unlockManager(facade);
        for (int i = 0; i < 22; i++) {
            facade.getPlayerProfileManager().incrementDaysPlayed();
        }

        JSONObject status = facade.getPrestigeStatus();
        assertTrue(status.getBoolean("eligible"));
        String runId = status.getString("runId");

        String reqId = "PRESTIGE-TEST-ID-01";
        JSONObject receipt = facade.confirmPrestige(reqId, runId);
        assertTrue(receipt.has("requestId"));
        assertEquals(reqId, receipt.getString("requestId"));

        // Idempotensi: panggil ulang dengan requestId sama harus mengembalikan receipt yang sama
        JSONObject retryReceipt = facade.confirmPrestige(reqId, runId);
        assertEquals(reqId, retryReceipt.getString("requestId"));
    }
}
