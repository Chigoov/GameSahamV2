package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.player.PlayerProgressManager;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Pengujian Tahap E3: Medali Prestasi (Achievements), Avatar & Bingkai Profil Prestise.
 */
public class AchievementsAndCosmeticsTahapE3Test {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "e3_ach_test_" + UUID.randomUUID());
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
    public void testAchievementsTrackingAndRewards() throws Exception {
        PlayerProgressManager ppm = facade.getProgressManager();
        Collection<PlayerProgressManager.Achievement> allAch = ppm.getAchievements();
        assertEquals("Harus ada 18 medali pencapaian", 18, allAch.size());

        PlayerProgressManager.Achievement first = ppm.getAchievement("first");
        assertNotNull(first);
        assertTrue(first.getName().toLowerCase().contains("transaksi"));
        assertFalse(first.isUnlocked());

        // Simulasi transaksi bursa pertama
        ppm.onTradeExecuted("IDX", System.currentTimeMillis());
        assertTrue("Medali transaksi perdana harus terbuka setelah order terisi", first.isUnlocked());
    }

    @Test
    public void testAvatarAndFrameEquipping() throws Exception {
        // Default avatar & frame
        JSONObject profile = facade.getPlayerProfileJson();
        assertEquals("avatar_trader_muda.png", profile.getString("equippedAvatar"));
        assertEquals("frame_steel.png", profile.getString("equippedFrame"));

        // Pasang avatar baru
        JSONObject avRes = facade.equipCosmetic("AVATAR", "avatar_fund_maestro.png");
        assertTrue(avRes.getBoolean("success"));
        assertEquals("avatar_fund_maestro.png", avRes.getString("equippedAvatar"));

        // Pasang bingkai baru
        JSONObject frRes = facade.equipCosmetic("FRAME", "frame_gold_tycoon.png");
        assertTrue(frRes.getBoolean("success"));
        assertEquals("frame_gold_tycoon.png", frRes.getString("equippedFrame"));

        // Verifikasi perubahan profil persisten
        JSONObject updatedProfile = facade.getPlayerProfileJson();
        assertEquals("avatar_fund_maestro.png", updatedProfile.getString("equippedAvatar"));
        assertEquals("frame_gold_tycoon.png", updatedProfile.getString("equippedFrame"));
    }
}
