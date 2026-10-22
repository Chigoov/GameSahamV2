package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.HistoricalDatasetLoader;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Pengujian verifikasi F02: Kompatibilitas stream reader profil perusahaan dengan Android API 26+
 * (bebas dari InputStream.readAllBytes() yang membutuhkan API 33).
 */
public class CompanyProfileApiCompatibilityTest {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "f02_profile_test_" + UUID.randomUUID());
        testDir.mkdirs();

        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) {
            universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        }
        if (universeFile.exists()) {
            String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
            HistoricalDatasetLoader.setAssetSource(path -> {
                File f = new File("android/app/src/main/assets", path);
                if (!f.exists()) f = new File("src/main/assets", path);
                return f.exists() ? new FileInputStream(f) : null;
            });
            facade = new TradingFacade(SaveManager.forDirectory(testDir));
            facade.setCatalog(InstrumentCatalog.fromJsonString(jsonUniverse));
            facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        } else {
            facade = new TradingFacade(SaveManager.forDirectory(testDir));
            facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        }
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
    public void testGetCompanyProfileWorksWithoutApi33Dependency() {
        JSONObject profileBbca = facade.getCompanyProfile("IDX:BBCA");
        assertNotNull("Profil BBCA tidak boleh null", profileBbca);
        assertTrue("Status profil harus success", profileBbca.optBoolean("success", false));
        assertEquals("BBCA", profileBbca.optString("ticker"));

        // Fallback untuk instrumen yang tidak ada di company_profiles.json
        JSONObject profileUnknown = facade.getCompanyProfile("NON_EXISTENT:TICKER");
        assertNotNull(profileUnknown);
    }
}
