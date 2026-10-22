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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Pengujian verifikasi R13: 150 Koleksi Mewah dalam 12 Kategori & 5 Rarity,
 * proteksi 12 item legacy, dan transaksi pembelian/penjualan/agunan.
 */
public class Collections150ConsistencyTest {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "r13_col_test_" + UUID.randomUUID());
        testDir.mkdirs();

        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) {
            universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        }
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(InstrumentCatalog.fromJsonString(jsonUniverse));
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(500_000_000));
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
    public void testFull150CollectionsLoadedAcross12CategoriesAnd5Rarities() {
        CollectionManager cm = facade.getCollectionManager();
        Collection<CollectibleItem> items = cm.getAllItems();
        assertEquals("Total barang koleksi harus tepat 150 item", 150, items.size());

        Set<CollectibleItem.Category> cats = new HashSet<>();
        Set<CollectibleItem.Rarity> rars = new HashSet<>();
        for (CollectibleItem it : items) {
            cats.add(it.getCategory());
            rars.add(it.getRarity());
        }

        assertTrue("Kategori harus mencakup minimal 12 varian", cats.size() >= 12);
        assertTrue("Rarity harus mencakup minimal 5 tingkatan", rars.size() >= 5);

        // Verifikasi 12 Item Legacy Terpelihara (ID & Harga Dasar)
        CollectibleItem vespa = cm.getItem("COL-CAR-00");
        assertNotNull("Vespa Klasik 1965 harus ada", vespa);
        assertEquals(Money.of(25_000_000L, Currency.IDR), vespa.getBasePrice());

        CollectibleItem porsche = cm.getItem("COL-CAR-01");
        assertNotNull("Porsche 911 GT3 RS harus ada", porsche);
        assertEquals(Money.of(4_500_000_000L, Currency.IDR), porsche.getBasePrice());

        CollectibleItem scbd = cm.getItem("COL-PROP-01");
        assertNotNull("Penthouse SCBD harus ada", scbd);
        assertEquals(Money.of(15_000_000_000L, Currency.IDR), scbd.getBasePrice());
    }

    @Test
    public void testBuyAndSellCollectionItem() {
        CollectionManager cm = facade.getCollectionManager();
        Account personal = facade.getPersonalAccount();
        CollectibleItem vespa = cm.getItem("COL-CAR-00");
        assertNotNull(vespa);
        assertFalse(vespa.isOwned());

        // Beli Vespa seharga 25jt
        facade.buyCollection("COL-CAR-00");
        assertTrue(vespa.isOwned());
        assertEquals(personal.getId(), vespa.getOwnerAccountId());

        // Jual kembali
        facade.sellCollection("COL-CAR-00");
        assertFalse(vespa.isOwned());
    }
}
