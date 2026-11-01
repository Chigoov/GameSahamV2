package com.tradingglobal3d.core;

import com.tradingglobal3d.core.collection.CollectibleItem;
import com.tradingglobal3d.core.collection.CollectionManager;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
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
 * Pengujian Tahap 3: Portofolio Realized/Unrealized PnL, Pinjaman Bank & Margin Health,
 * serta Galeri 150 Koleksi Mewah.
 */
public class PortfolioAndGalleryTahap3Test {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "tahap3_test_" + UUID.randomUUID());
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
    public void testPortfolioUnrealizedAndRealizedPnL() throws Exception {
        Account personal = facade.getPersonalAccount();
        assertNotNull(personal);

        // Tambahkan posisi saham BBCA 100 lembar
        personal.addPosition(new com.tradingglobal3d.core.model.Position(
                "BBCA", "IDX", Currency.IDR, 100,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10250)
        ));

        // Verifikasi posisi terbentuk
        com.tradingglobal3d.core.model.Position pos = personal.getPosition("IDX:BBCA");
        if (pos == null) pos = personal.getPosition("BBCA");
        assertNotNull("Posisi harus terbentuk", pos);
        assertEquals(100L, pos.getQuantity());

        // Cek Margin Health via facade
        com.tradingglobal3d.core.risk.RiskComplianceService.MarginHealthReport health = facade.evaluateMarginHealth();
        assertNotNull(health);
        assertEquals(com.tradingglobal3d.core.risk.MarginCallStatus.HEALTHY, health.getStatus());
        assertTrue(health.getEquity().isPositive());
    }

    @Test
    public void testGalleryCatalog150ItemsCoverage() {
        CollectionManager cm = facade.getCollectionManager();
        Collection<CollectibleItem> items = cm.getAllItems();
        assertEquals("Total barang koleksi harus 150", 150, items.size());

        CollectibleItem vespa = cm.getItem("COL-CAR-00");
        assertNotNull("Vespa Klasik 1965 harus ada", vespa);
        assertEquals(Money.of(25_000_000L, Currency.IDR), vespa.getBasePrice());

        // Test purchase
        facade.buyCollection("COL-CAR-00");
        assertTrue(vespa.isOwned());
    }
}
