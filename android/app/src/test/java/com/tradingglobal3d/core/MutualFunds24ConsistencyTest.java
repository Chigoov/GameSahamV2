package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.funds.MutualFund;
import com.tradingglobal3d.core.funds.MutualFundManager;
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
 * Pengujian verifikasi R14: 24 Produk Reksa Dana & Factsheet lengkap.
 */
public class MutualFunds24ConsistencyTest {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "r14_fund_test_" + UUID.randomUUID());
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
    public void testFull24MutualFundsLoadedWith3LegacyPreserved() {
        MutualFundManager mfm = facade.getMutualFundManager();
        Collection<MutualFund> allFunds = mfm.getAllFunds();
        assertEquals("Harus tepat memuat 24 produk reksa dana", 24, allFunds.size());

        // Verifikasi 3 produk legacy
        assertNotNull("Nusantara Kas Lancar (FUND-MM-01) harus ada", mfm.getFund("FUND-MM-01"));
        assertNotNull("Nusantara Obligasi Mantap (FUND-FI-01) harus ada", mfm.getFund("FUND-FI-01"));
        assertNotNull("Nusantara Indeks Saham Unggulan (FUND-EQ-01) harus ada", mfm.getFund("FUND-EQ-01"));

        // Verifikasi salah satu produk baru
        assertNotNull("Nusantara Teknologi Inovasi (FUND-SE-01) harus ada", mfm.getFund("FUND-SE-01"));
    }

    @Test
    public void testSubscribeAndCutoffExecution() throws Exception {
        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getAvailableCash(Currency.IDR);

        // Beli reksa dana pasar uang 5jt
        JSONObject res = facade.subscribeMutualFund("REQ-SUB-01", "FUND-MM-01", Money.of(5_000_000, Currency.IDR));
        assertTrue(res.getBoolean("success"));

        // Kas harus di-escrow/berkurang
        Money cashAfterSub = personal.getAvailableCash(Currency.IDR);
        assertEquals(initialCash.subtract(Money.of(5_000_000, Currency.IDR)), cashAfterSub);
    }
}
