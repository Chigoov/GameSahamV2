package com.tradingglobal3d.core;

import com.tradingglobal3d.core.business.CompanyManager;
import com.tradingglobal3d.core.business.OperatingCompany;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.multiasset.CommodityInstrument;
import com.tradingglobal3d.core.multiasset.FundBuilderService;
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
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Pengujian Tahap S2: Kongsi Konglomerasi (Holding & Subsidiaries),
 * Meracik Reksa Dana Kustom (Alokasi 100%), dan Aset Kripto Digital Spot 24 Jam.
 */
public class HoldingAndCustomFundsTahapS2Test {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "s2_test_" + UUID.randomUUID());
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
    public void testCryptoDigitalSpotAssetsAvailable() {
        FundBuilderService fb = facade.getFundBuilder();
        assertNotNull(fb);

        CommodityInstrument btc = fb.getCommodity("COMM:BTC");
        assertNotNull("Bitcoin digital spot harus terdaftar di multi-aset", btc);
        assertEquals("BTC", btc.getCode());
        assertTrue("Harga Bitcoin harus positif", btc.getSpotPricePerUnit().isPositive());

        CommodityInstrument eth = fb.getCommodity("COMM:ETH");
        assertNotNull("Ethereum digital spot harus terdaftar di multi-aset", eth);
        assertEquals("ETH", eth.getCode());
    }

    @Test
    public void testCustomMutualFundCreationAndAllocationValidation() throws Exception {
        // 1. Pembuatan reksa dana kustom valid (40% + 30% + 20% + 10% = 100%)
        JSONObject fund1 = facade.createCustomMutualFund(
                "Nusantara Alokasi Cerdas", 40.0, 30.0, 20.0, 10.0, BigDecimal.valueOf(50_000_000)
        );
        assertNotNull(fund1);
        assertEquals("Nusantara Alokasi Cerdas", fund1.getString("fundName"));
        assertEquals(40.0, fund1.getDouble("equityWeightPct"), 0.01);

        // 2. Pembuatan reksa dana kustom tidak valid (total != 100%) harus ditolak
        try {
            facade.createCustomMutualFund("Fund Gagal", 50.0, 50.0, 20.0, 0.0, BigDecimal.valueOf(10_000_000));
            fail("Alokasi 120% harus ditolak IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("100%"));
        }
    }

    @Test
    public void testHoldingCompanyInternalCapitalTransfer() throws Exception {
        Account personal = facade.getPersonalAccount();

        // Dirikan anak usaha holding (PT Modal Digital Nusantara) dengan modal 25jt
        OperatingCompany sub = facade.foundCompany(
                "COMP-HOLDING-01", "PT Modal Digital Nusantara", "Teknologi",
                Money.of(25_000_000, Currency.IDR)
        );
        assertNotNull(sub);
        assertEquals("COMP-HOLDING-01", sub.getId());

        // Transfer suntik modal internal holding dari personal ke anak usaha tanpa pajak
        facade.transferCompanyCapital("COMP-HOLDING-01", true, Money.of(10_000_000, Currency.IDR));

        // Verifikasi saldo kas anak usaha bertambah menjadi 35jt via getCompaniesJson
        JSONObject comps = facade.getCompaniesJson();
        JSONArray compArr = comps.getJSONArray("companies");
        boolean verifiedCash = false;
        for (int i = 0; i < compArr.length(); i++) {
            JSONObject c = compArr.getJSONObject(i);
            if (c.getString("id").equals("COMP-HOLDING-01")) {
                assertEquals("35000000", c.getString("cash"));
                verifiedCash = true;
            }
        }
        assertTrue("Kas anak usaha holding harus terverifikasi 35jt", verifiedCash);
    }
}
