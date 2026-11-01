package com.tradingglobal3d.core;

import com.tradingglobal3d.core.boss.MarketBoss;
import com.tradingglobal3d.core.boss.MarketBossService;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.risk.TaxAuditService;
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
 * Pengujian Tahap E2: Duel Lawan Bos Taipan Pasar & Audit Kepatuhan Pajak / Risiko.
 */
public class BossAndTaxAuditTahapE2Test {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "e2_boss_test_" + UUID.randomUUID());
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
    public void testMarketBoss4TiersAndDuelWorkflow() throws Exception {
        MarketBossService bs = facade.getMarketBossService();
        List<MarketBoss> bosses = bs.getAllBosses();
        assertEquals("Harus ada 4 Bos Taipan Pasar", 4, bosses.size());

        // 1. Verifikasi Bos Easy (BOSS-01)
        MarketBoss b1 = bs.getBoss("BOSS-01");
        assertNotNull(b1);
        assertEquals(MarketBoss.DifficultyTier.EASY, b1.getTier());
        assertEquals(5.0, b1.getTargetReturn(), 0.01);
        assertEquals(5, b1.getDurationSessions());
        assertFalse(b1.isActive());

        // 2. Mulai duel
        JSONObject started = facade.startBossDuel("BOSS-01");
        assertTrue(started.getBoolean("isActive"));
        assertTrue(b1.isActive());

        // 3. Simulasi sesi dan progres return
        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getAvailableCash(Currency.IDR);

        // Berikan return pemain 8.0% per sesi selama 5 sesi (Total 40.0% > target bos 5.0%)
        for (int i = 0; i < 5; i++) {
            bs.processSessionUpdate(8.0, personal, facade.getLedger(), System.currentTimeMillis());
        }

        assertTrue("Bos harus terkalahkan", b1.isDefeated());
        assertFalse("Duel harus berakhir", b1.isActive());

        // Pemain harus menerima hadiah kemenangan tunai Rp 10.000.000
        Money expectedCash = initialCash.add(Money.of(10_000_000, Currency.IDR));
        assertEquals("Saldo pemain harus bertambah reward bos", expectedCash, personal.getAvailableCash(Currency.IDR));
    }

    @Test
    public void testTaxAuditComplianceEvaluation() throws Exception {
        TaxAuditService taxService = facade.getTaxAuditService();
        Account personal = facade.getPersonalAccount();

        // 1. Evaluasi pada kondisi modal bersih (Grade A - Patuh)
        TaxAuditService.TaxAuditReport reportA = taxService.evaluateCompliance(
                personal, Money.of(100_000_000, Currency.IDR), Money.zero(Currency.IDR)
        );
        assertEquals("A", reportA.getComplianceRating());
        assertEquals(0.10, reportA.getDividendTaxDiscount(), 0.001);
        assertFalse(reportA.isAuditTriggered());

        // 2. Evaluasi pada kondisi utang berlebih > 40% (Grade C - Pelanggaran & Penalti)
        TaxAuditService.TaxAuditReport reportC = taxService.evaluateCompliance(
                personal, Money.of(100_000_000, Currency.IDR), Money.of(60_000_000, Currency.IDR) // Utang 60%
        );
        assertEquals("C", reportC.getComplianceRating());
        assertTrue(reportC.isAuditTriggered());
        assertTrue("Penalti harus positif pada utang ekstrem", reportC.getPenaltyAmount().isPositive());
    }
}
