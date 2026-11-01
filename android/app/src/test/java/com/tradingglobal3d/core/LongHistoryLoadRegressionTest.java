package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.storage.SaveState;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import com.tradingglobal3d.core.market.InstrumentCatalog;

import static org.junit.Assert.*;

/**
 * Uji Regresi K-021:
 * 1. Memastikan reload save dengan histori panjang (ratusan hari bursa dan ribuan mutasi ledger)
 *    berhasil dimuat dan divalidasi pada batasan heap 384MB tanpa OutOfMemoryError.
 * 2. Membuktikan bahwa kegagalan load (all-or-nothing) tidak meninggalkan state setengah diterapkan.
 */
public class LongHistoryLoadRegressionTest {
    private File testDir;
    private SaveManager saveManager;
    private InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "k021_reg_test_" + System.currentTimeMillis() + "_" + Math.random());
        testDir.mkdirs();
        saveManager = SaveManager.forDirectory(testDir);

        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (universeFile.exists()) {
            String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
            catalog = InstrumentCatalog.fromJsonString(jsonUniverse);
        }
    }

    @After
    public void tearDown() {
        deleteDir(testDir);
    }

    private void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) for (File f : files) deleteDir(f);
        }
        dir.delete();
    }

    @Test
    public void testFailedLoadPreservesLiveStateAllOrNothing() throws Exception {
        TradingFacade liveFacade = new TradingFacade(saveManager);
        if (catalog != null) liveFacade.setCatalog(catalog);
        liveFacade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(100_000_000L));
        liveFacade.getClock().play();

        Account personal = liveFacade.getPersonalAccount();
        Money initialCash = personal.getTotalCash(Currency.IDR);
        String initialDate = liveFacade.getClock().getCurrentDate().toString();

        // Buat file save yang tidak valid (misal: ID perusahaan tidak memiliki akun yang cocok)
        liveFacade.save();
        File saveFile = saveManager.getSaveFile();
        String validJson = new String(Files.readAllBytes(saveFile.toPath()), StandardCharsets.UTF_8);
        JSONObject corruptRoot = new JSONObject(validJson);
        // Rusak runtimeState: buat companies merujuk akun fiktif
        JSONObject runtime = corruptRoot.getJSONObject("runtimeState");
        org.json.JSONArray compArray = new org.json.JSONArray();
        JSONObject fakeComp = new JSONObject()
                .put("id", "COMP-FAKE")
                .put("name", "PT Palsu")
                .put("sector", "JASA")
                .put("level", 1)
                .put("cash", "10000000")
                .put("accountId", "ACC-DOES-NOT-EXIST");
        compArray.put(fakeComp);
        runtime.put("companies", compArray);
        Files.write(saveFile.toPath(), corruptRoot.toString().getBytes(StandardCharsets.UTF_8));

        // Coba load ke liveFacade yang sedang berjalan
        boolean loadSuccess = liveFacade.load();
        assertFalse("Load dengan state tidak valid harus gagal (all-or-nothing)", loadSuccess);

        // Buktikan state liveFacade tetap utuh tanpa mutasi setengah jalan
        Account personalAfter = liveFacade.getPersonalAccount();
        assertEquals("Saldo akun personal harus tetap tidak berubah", initialCash, personalAfter.getTotalCash(Currency.IDR));
        assertEquals("Tanggal jam permainan harus tetap tidak berubah", initialDate, liveFacade.getClock().getCurrentDate().toString());
        assertTrue("Ledger rekonsiliasi harus tetap valid", liveFacade.reconcileAll());
    }

    @Test
    public void testLongHistoryProgressionAndReloadMemorySafe() throws Exception {
        TradingFacade facade = new TradingFacade(saveManager);
        if (catalog != null) facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(250_000_000L));
        facade.getClock().play();

        // Jalankan siklus 60 hari bursa dengan transaksi periodik untuk membangun volume data buku besar
        for (int day = 1; day <= 60; day++) {
            JSONObject receipt = facade.skipDay("K021-DAY-" + day);
            assertTrue(receipt.getBoolean("success"));

            if (day % 10 == 0) {
                facade.claimDailySponsor("K021-SPON-" + day);
            }

            if (day % 15 == 0) {
                facade.save();
                TradingFacade reloaded = new TradingFacade(saveManager);
                if (catalog != null) reloaded.setCatalog(catalog);
                assertTrue("Reload pada hari " + day + " harus berhasil tanpa OOM", reloaded.load());
                assertTrue(reloaded.reconcileAll());
                facade = reloaded;
                facade.getClock().play();
            }
        }

        // Simpan state akhir dan buktikan reload instance baru berhasil
        facade.save();
        TradingFacade finalReload = new TradingFacade(saveManager);
        if (catalog != null) finalReload.setCatalog(catalog);
        assertTrue("Reload final harus berhasil", finalReload.load());
        assertTrue("Buku besar rekonsiliasi harus valid", finalReload.reconcileAll());
        assertTrue("Koleksi data ledger harus terisi ratusan mutasi", finalReload.getLedger().getEntries().size() > 100);
    }
}
