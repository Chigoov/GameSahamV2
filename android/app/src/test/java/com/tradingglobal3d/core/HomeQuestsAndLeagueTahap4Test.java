package com.tradingglobal3d.core;

import com.tradingglobal3d.core.career.PeriodicMission;
import com.tradingglobal3d.core.career.PeriodicMissionManager;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.league.LeagueManager;
import com.tradingglobal3d.core.market.InstrumentCatalog;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Pengujian Tahap 4: Agenda, Misi Harian, Profil DNA Investor, dan Sistem Liga Rival.
 */
public class HomeQuestsAndLeagueTahap4Test {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "tahap4_test_" + UUID.randomUUID());
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
    public void testDailyMissionsGeneratedAndTracked() {
        PeriodicMissionManager pmm = facade.getPeriodicMissionManager();
        assertNotNull(pmm);

        LocalDate today = facade.getClock().getCurrentDate();
        java.util.Collection<PeriodicMission> missions = pmm.getActiveMissions();
        assertTrue("Harus ada minimal 2 misi aktif", missions.size() >= 2);

        PeriodicMission first = missions.iterator().next();
        assertNotNull(first.getId());
        assertNotNull(first.getTitle());
        assertTrue(first.getTargetCount() > 0);
    }

    @Test
    public void testInvestorArchetypeDiagnosis() throws Exception {
        JSONObject profile = facade.getInvestorArchetype();
        assertNotNull(profile);
        assertTrue(profile.has("archetype"));
        assertTrue(profile.has("label"));
        assertTrue(profile.has("description"));
        // Day 0: Profil harus menampilkan gaya sedang terbentuk dengan transparan
        assertEquals("Gaya Investasimu Sedang Terbentuk", profile.getString("label"));
    }

    @Test
    public void testLeagueAndRivalSystem() throws Exception {
        LeagueManager lm = facade.getLeagueManager();
        assertNotNull(lm);

        assertEquals(LeagueManager.LeagueTier.BRONZE, lm.getCurrentTier());
        JSONObject rival = lm.getRivalStatus(facade.getPersonalAccount().getTotalPortfolioValue(com.tradingglobal3d.core.model.Currency.IDR, BigDecimal.ONE), 0.0);
        assertNotNull("Rival NPC harus ada", rival);
        assertTrue(rival.has("rivalName"));
        assertTrue(rival.has("rivalTitle"));
    }
}

