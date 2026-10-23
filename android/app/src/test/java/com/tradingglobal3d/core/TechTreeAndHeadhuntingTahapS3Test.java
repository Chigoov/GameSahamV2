package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.firm.HeadhuntingService;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.research.ResearchTechTreeService;
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
 * Pengujian Tahap S3: Pohon Riset Finansial Tech Tree & Bursa Headhunting Staf Analis Elit.
 */
public class TechTreeAndHeadhuntingTahapS3Test {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "s3_test_" + UUID.randomUUID());
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
    public void testResearchTechTreeWorkflow() throws Exception {
        ResearchTechTreeService tts = facade.getResearchTechTreeService();
        assertNotNull(tts);

        List<ResearchTechTreeService.TechNode> nodes = tts.getAllNodes();
        assertTrue("Harus ada minimal 4 node riset", nodes.size() >= 4);

        // Node awal ALGO 1 terbuka
        ResearchTechTreeService.TechNode algo1 = tts.getNode("TECH-ALGO-1");
        assertNotNull(algo1);
        assertTrue(algo1.isUnlocked());

        // Node EFF 1 terkunci
        ResearchTechTreeService.TechNode eff1 = tts.getNode("TECH-EFF-1");
        assertNotNull(eff1);
        assertFalse(eff1.isUnlocked());

        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getAvailableCash(Currency.IDR);

        // Lakukan riset upgrade EFF 1 (Biaya 10jt)
        JSONObject upRes = facade.upgradeTechNode("TECH-EFF-1");
        assertTrue(upRes.getBoolean("success"));
        assertTrue(eff1.isUnlocked());

        // Kas terdebit biaya riset
        Money cashAfter = personal.getAvailableCash(Currency.IDR);
        assertEquals(initialCash.subtract(eff1.getUpgradeCost()), cashAfter);
    }

    @Test
    public void testHeadhuntingCompetitorStaffWorkflow() throws Exception {
        HeadhuntingService hs = facade.getHeadhuntingService();
        assertNotNull(hs);

        List<HeadhuntingService.Candidate> candidates = hs.getAllCandidates();
        assertTrue("Harus ada minimal 3 kandidat analis elit", candidates.size() >= 3);

        HeadhuntingService.Candidate cand1 = hs.getCandidate("CAND-01");
        assertNotNull(cand1);
        assertEquals("Budi Santoso, CFA", cand1.getName());
        assertFalse(cand1.isHired());

        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getAvailableCash(Currency.IDR);

        // Bajak staf (Biaya jasa headhunter 15jt)
        JSONObject poachRes = facade.poachStaffCandidate("CAND-01");
        assertTrue(poachRes.getBoolean("success"));
        assertTrue(cand1.isHired());

        // Kas terdebit biaya jasa headhunter
        Money cashAfter = personal.getAvailableCash(Currency.IDR);
        assertEquals(initialCash.subtract(cand1.getPoachCost()), cashAfter);
    }
}
