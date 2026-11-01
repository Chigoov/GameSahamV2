package com.tradingglobal3d.core;

import com.tradingglobal3d.core.business.CompanyManager;
import com.tradingglobal3d.core.business.OperatingCompany;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.research.ResearchBooster;
import com.tradingglobal3d.core.research.ResearchService;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Pengujian Tahap E4: Fasilitas Kantor, Manajemen Moral Staf & Kartu Booster Riset R&D.
 */
public class OfficePerksAndBoostersTahapE4Test {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "e4_boost_test_" + UUID.randomUUID());
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
    public void testResearchBoostersWorkflow() throws Exception {
        ResearchService rs = facade.getResearchService();
        List<ResearchBooster> boosters = rs.getBoosters();
        assertEquals("Harus ada 3 kartu booster riset", 3, boosters.size());

        // 1. Verifikasi booster fee discount (BOOST-02)
        ResearchBooster b2 = boosters.stream().filter(b -> b.getBoosterId().equals("BOOST-02")).findFirst().orElse(null);
        assertNotNull(b2);
        assertFalse(b2.isActive());
        assertEquals(15, b2.getRemainingUses());

        // 2. Aktivasi booster
        JSONObject actRes = facade.activateResearchBooster("BOOST-02");
        assertTrue(actRes.getBoolean("isActive"));
        assertTrue(b2.isActive());
        assertTrue(rs.hasActiveBooster(ResearchBooster.BoosterType.FEE_DISCOUNT));

        // 3. Konsumsi pemakaian
        b2.consumeUse();
        assertEquals(14, b2.getRemainingUses());
    }

    @Test
    public void testStaffMoraleAndOfficePerks() {
        CompanyManager cm = facade.getCompanyManager();
        assertNotNull(cm);

        if (!cm.getAllCompanies().isEmpty()) {
            OperatingCompany comp = cm.getAllCompanies().iterator().next();
            assertTrue("Skor moral awal harus dalam rentang 0-100", comp.getMorale() >= 0 && comp.getMorale() <= 100);
            assertNotNull("Tingkat fasilitas/tunjangan kantor harus ada", comp.getBenefitTier());
        }
    }
}
