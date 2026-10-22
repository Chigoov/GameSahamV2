package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.IssuerFundamentals;
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
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Pengujian verifikasi R08: Konsistensi Fundamental seluruh emiten (Neraca, Arus Kas, Rasio, Valuasi).
 */
public class IssuerFundamentalsConsistencyTest {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "r08_fund_test_" + UUID.randomUUID());
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
    public void testAll100ActiveInstrumentsHaveBalancedFundamentals() throws Exception {
        int count = 0;
        for (Instrument inst : facade.getCatalog().getActiveUniverse()) {
            count++;
            JSONObject fund = facade.getIssuerFundamentals(inst.getId());
            assertTrue("Status harus success untuk " + inst.getId(), fund.getBoolean("success"));

            // 1. Verifikasi Neraca Seimbang: Aset = Liabilitas + Ekuitas
            JSONObject bs = fund.getJSONObject("balanceSheet");
            BigDecimal assets = new BigDecimal(bs.getString("totalAssets"));
            BigDecimal liab = new BigDecimal(bs.getString("totalLiabilities"));
            BigDecimal equity = new BigDecimal(bs.getString("totalEquity"));
            assertEquals("Neraca Aset = Liabilitas + Ekuitas harus seimbang 100% pada " + inst.getId(),
                    assets, liab.add(equity));
            assertTrue(bs.getBoolean("isBalanced"));

            // 2. Verifikasi Arus Kas Konsisten: Net Cashflow = CFO + CFI + CFF
            JSONObject cf = fund.getJSONObject("cashFlow");
            BigDecimal cfo = new BigDecimal(cf.getString("cfo"));
            BigDecimal cfi = new BigDecimal(cf.getString("cfi"));
            BigDecimal cff = new BigDecimal(cf.getString("cff"));
            BigDecimal netCf = new BigDecimal(cf.getString("netCashflow"));
            assertEquals("Arus Kas Net = CFO + CFI + CFF harus konsisten pada " + inst.getId(),
                    netCf, cfo.add(cfi).add(cff));

            // 3. Verifikasi Struktur Saham & Free Float
            JSONObject sh = fund.getJSONObject("shareStructure");
            long out = sh.getLong("sharesOutstanding");
            long ff = sh.getLong("freeFloatShares");
            long issued = sh.getLong("sharesIssued");
            assertTrue("Shares issued harus >= shares outstanding", issued >= out);
            assertTrue("Free float harus <= shares outstanding", ff <= out);
            assertTrue("Shareholders count harus > 0", sh.getInt("totalShareholdersCount") > 0);

            // 4. Verifikasi Rasio Keuangan
            JSONObject val = fund.getJSONObject("valuationRatios");
            BigDecimal mcap = new BigDecimal(val.getString("marketCap"));
            BigDecimal expectedMcap = inst.getCurrentPrice().getAmount().multiply(BigDecimal.valueOf(out));
            assertEquals("Market cap harus cocok dengan harga * saham beredar", expectedMcap, mcap);
            assertNotNull(val.getString("per"));
            assertNotNull(val.getString("pbv"));
            assertNotNull(val.getString("roe"));
            assertNotNull(val.getString("roa"));
            assertNotNull(val.getString("der"));
            assertNotNull(val.getString("dividendYield"));
        }
        assertEquals("Harus menguji seluruh 100 emiten aktif", 100, count);
    }
}
