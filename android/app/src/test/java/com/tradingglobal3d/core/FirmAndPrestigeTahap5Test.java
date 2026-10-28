package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.firm.FirmManager;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.storage.SaveManager;
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
 * Pengujian Tahap 5: Firma Dana Kelolaan, Staf Kantor, Rekening Klien Terpisah (AUM),
 * serta Status Prestise Rebirth (Era Baru).
 */
public class FirmAndPrestigeTahap5Test {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "tahap5_test_" + UUID.randomUUID());
        testDir.mkdirs();

        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) {
            universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        }
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(InstrumentCatalog.fromJsonString(jsonUniverse));
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(100_000_000));
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
    public void testFirmAndClientAumSeparation() throws Exception {
        Account personal = facade.getPersonalAccount();
        assertEquals(AccountType.PERSONAL, personal.getType());

        // Snapshot firma
        JSONObject firmSnap = facade.getFirmSnapshot();
        assertNotNull(firmSnap);
        assertTrue(firmSnap.getBoolean("success"));
        assertTrue(firmSnap.has("aumIdr"));
        assertTrue(firmSnap.has("personalCashIdr"));
        assertTrue(firmSnap.has("activeClientCount"));
        assertTrue(firmSnap.has("mandates"));

        // Verifikasi AUM klien terpisah dari kas pribadi
        assertNotEquals(firmSnap.getString("personalCashIdr"), firmSnap.getString("aumIdr"));
    }

    @Test
    public void testPrestigeRebirthStatus() throws Exception {
        JSONObject prestigeStatus = facade.getPrestigeStatus();
        assertNotNull(prestigeStatus);
        assertTrue(prestigeStatus.has("eligible"));
        assertTrue(prestigeStatus.has("currentRank"));
        assertTrue(prestigeStatus.has("requiredRank"));
        assertTrue(prestigeStatus.has("retained"));

        // Pada awal permainan, pemain baru belum eligible untuk Rebirth (butuh rank Portfolio Manager)
        assertFalse(prestigeStatus.getBoolean("eligible"));
    }
}
