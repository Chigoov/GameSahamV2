package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.SectorDominanceService;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.player.DailyStreakService;
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
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Pengujian Tahap S4: Jalur Disiplin Harian (Streak Strip),
 * Guncangan Pasar Mendadak (Black Swan & Supply Chain Crisis),
 * serta Status Dominasi & Pengaruh Sektor Bursa.
 */
public class DailyStreakAndMarketShockTahapS4Test {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "s4_test_" + UUID.randomUUID());
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
    public void testDailyStreakProgressionAndBrokenStreakReset() throws Exception {
        DailyStreakService ss = facade.getDailyStreakService();
        assertNotNull(ss);

        // Awal: Sesi 1 -> Streak 1 (1.0x)
        assertEquals(1, ss.getCurrentStreak());
        assertEquals(1.0, ss.getRewardMultiplier(), 0.01);
        assertFalse(ss.isStreakBroken());

        // Login berurutan: Sesi 2, Sesi 3, Sesi 4, Sesi 5
        ss.recordSessionLogin(2);
        assertEquals(2, ss.getCurrentStreak());

        ss.recordSessionLogin(3);
        assertEquals(3, ss.getCurrentStreak());
        assertEquals(1.25, ss.getRewardMultiplier(), 0.01); // 1.25x

        ss.recordSessionLogin(4);
        ss.recordSessionLogin(5);
        assertEquals(5, ss.getCurrentStreak());
        assertEquals(1.50, ss.getRewardMultiplier(), 0.01); // 1.50x

        // Login berurutan hingga Hari ke-7
        ss.recordSessionLogin(6);
        ss.recordSessionLogin(7);
        assertEquals(7, ss.getCurrentStreak());
        assertEquals(2.0, ss.getRewardMultiplier(), 0.01); // Day 7+: 2.0x

        // Absen melewatkan sesi (lompat dari sesi 7 ke sesi 10) -> Broken Streak!
        ss.recordSessionLogin(10);
        assertEquals(1, ss.getCurrentStreak());
        assertTrue("Absen harus memicu broken streak", ss.isStreakBroken());
        assertEquals(1.0, ss.getRewardMultiplier(), 0.01);
    }

    @Test
    public void testSectorDominanceThresholdAndPrivileges() throws Exception {
        Account personal = facade.getPersonalAccount();
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        assertNotNull(bbca);

        // Sebelum beli saham dalam jumlah dominan
        SectorDominanceService sds = facade.getSectorDominanceService();
        Map<String, SectorDominanceService.SectorStatus> statusesBefore = sds.evaluateDominance(personal, facade.getCatalog(), BigDecimal.valueOf(15800));
        assertFalse(statusesBefore.get("Keuangan").isDominant());

        // Akumulasi kepemilikan masif di sektor Keuangan (> 25% outstanding seluruh sektor)
        for (Instrument inst : facade.getCatalog().getActiveUniverse()) {
            if ("Keuangan".equalsIgnoreCase(inst.getSector())) {
                long hugeShares = (long) (inst.getSharesOutstanding() * 0.35); // 35% kepemilikan
                personal.addPosition(new Position(
                        inst.getTicker(), inst.getExchange().name(), inst.getCurrency(), hugeShares,
                        inst.getCurrentPrice().getAmount(), inst.getCurrentPrice().getAmount()
                ));
            }
        }

        Map<String, SectorDominanceService.SectorStatus> statusesAfter = sds.evaluateDominance(personal, facade.getCatalog(), BigDecimal.valueOf(15800));
        SectorDominanceService.SectorStatus finStatus = statusesAfter.get("Keuangan");
        assertNotNull(finStatus);
        assertTrue("Kepemilikan > 25% harus dinobatkan sebagai Penguasa Sektor", finStatus.isDominant());
        assertTrue("Penguasa sektor harus memiliki hak veto aktif", finStatus.isVetoPowerActive());
        assertEquals(25.0, finStatus.getFeeDiscountPct(), 0.01);
    }

    @Test
    public void testMarketShockAndSupplyChainCrisis() throws Exception {
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        BigDecimal priceBefore = bbca.getCurrentPrice().getAmount();

        // 1. Uji Black Swan Market Shock (15% drop)
        JSONObject shockRes = facade.triggerMarketShockEvent("BLACK_SWAN_SHOCK");
        assertTrue(shockRes.getBoolean("success"));
        assertTrue(shockRes.getInt("affectedInstruments") > 0);

        BigDecimal priceAfterShock = bbca.getCurrentPrice().getAmount();
        assertTrue("Harga saham harus anjlok setelah guncangan pasar", priceAfterShock.compareTo(priceBefore) < 0);

        // 2. Uji Supply Chain Crisis
        JSONObject crisisRes = facade.triggerMarketShockEvent("SUPPLY_CHAIN_CRISIS");
        assertTrue(crisisRes.getBoolean("success"));
    }
}
