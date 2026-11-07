package com.tradingglobal3d.core;

import com.tradingglobal3d.core.boss.MarketBoss;
import com.tradingglobal3d.core.boss.MarketBossService;
import com.tradingglobal3d.core.collection.CollectionManager;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.league.LeagueManager;
import com.tradingglobal3d.core.market.HistoricalDatasetLoader;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.player.PlayerProfileManager;
import com.tradingglobal3d.core.player.PlayerProgressManager;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Collections;

public class PackageP05CatalogAndProfileTest {

    @Test
    public void testAU11_CollectionManagerLoads150ItemsViaAssetSource() throws Exception {
        // Setup AssetSource that reads the actual asset file
        HistoricalDatasetLoader.setAssetSource(path -> new FileInputStream(new File("android/app/src/main/assets/" + path)));

        CollectionManager cm = new CollectionManager();
        Assert.assertEquals("Collection catalog must load all 150 items via AssetSource!", 150, cm.getAllItems().size());
    }

    @Test
    public void testAU15_MarketBossDuelAdvancesOnWorldDaySettlement() throws Exception {
        File testDir = new File("build/tmp-test-p05-au15");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        facade.startBossDuel("BOSS-01");
        MarketBoss boss = facade.getMarketBossService().getBoss("BOSS-01");
        Assert.assertNotNull(boss);
        Assert.assertTrue(boss.isActive());
        Assert.assertEquals(0, boss.getElapsedSessions());

        // Fast forward 1 full trading day (86,400 seconds) crossing 16:01 settlement
        facade.advanceWorldSecondsForTest(86400);

        Assert.assertEquals("Boss duel must advance elapsed sessions on day settlement!", 1, boss.getElapsedSessions());
    }

    @Test
    public void testAU16_MarketBossStatePersistsAcrossSaveLoadAndResetsOnNewGame() throws Exception {
        File testDir = new File("build/tmp-test-p05-au16");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        facade.startBossDuel("BOSS-01");
        MarketBoss boss = facade.getMarketBossService().getBoss("BOSS-01");
        Assert.assertNotNull(boss);
        Assert.assertTrue(boss.isActive());

        // Advance 1 day
        facade.advanceWorldSecondsForTest(86400);
        Assert.assertEquals(1, boss.getElapsedSessions());

        // Save
        facade.save();

        // Load into new facade
        SaveManager sm2 = SaveManager.forDirectory(testDir);
        TradingFacade facade2 = new TradingFacade(sm2);
        facade2.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade2.startOrResume();

        MarketBoss bossRestored = facade2.getMarketBossService().getBoss("BOSS-01");
        Assert.assertNotNull(bossRestored);
        Assert.assertTrue("Boss must remain active after load!", bossRestored.isActive());
        Assert.assertEquals("Boss elapsed sessions must persist after load!", 1, bossRestored.getElapsedSessions());

        // New game on same facade resets boss state
        facade2.startInternalGame(new BigDecimal("50000000"));
        MarketBoss bossReset = facade2.getMarketBossService().getBoss("BOSS-01");
        Assert.assertFalse("Boss duel must be reset on new game!", bossReset.isActive());
        Assert.assertEquals(0, bossReset.getElapsedSessions());
    }

    @Test
    public void testAU17_ResearchEliteAndLeagueCompetitorAchievementsUnlockViaProductionEvents() {
        PlayerProgressManager ppm = new PlayerProgressManager();

        PlayerProgressManager.Achievement elite = ppm.getAchievement("research_elite");
        Assert.assertNotNull(elite);
        Assert.assertFalse(elite.isUnlocked());

        // Read 10 unique research articles
        for (int i = 1; i <= 10; i++) {
            ppm.onResearchRead("ARTICLE-" + i, 1000L * i);
        }
        Assert.assertTrue("Research elite achievement must unlock after reading 10 unique articles!", elite.isUnlocked());

        // League competitor achievement
        PlayerProgressManager.Achievement leagueComp = ppm.getAchievement("league_competitor");
        Assert.assertNotNull(leagueComp);
        Assert.assertFalse(leagueComp.isUnlocked());

        // Trigger weekly league participation event
        ppm.onLeagueParticipated(20000L);
        Assert.assertTrue("League competitor achievement must unlock via onLeagueParticipated!", leagueComp.isUnlocked());
    }

    @Test
    public void testAU18_LeagueLeaderboardArchivePreservesPlayerRowWhenRankedOutsideTop20() throws Exception {
        LeagueManager lm = new LeagueManager();

        // Simulate 30 NPCs with +5% return, player with +1% return -> Player ranked 31st (outside top 20!)
        for (int i = 1; i <= 30; i++) {
            var npc = lm.getNpcCompetitors().get("ACC-NPC-LEAGUE-" + String.format(java.util.Locale.ROOT, "%02d", i));
            if (npc != null) {
                npc.setWeeklyReturnPercent(5.0);
            }
        }

        // Process rollover with player weekly return = 1.0%
        lm.processWeeklyRollover(1.0, Money.of(50000000L, com.tradingglobal3d.core.model.Currency.IDR));

        java.util.List<JSONObject> history = lm.getPeriodHistory();
        Assert.assertTrue("History must have at least 1 archived period", history.size() > 0);
        JSONObject lastPeriod = history.get(history.size() - 1);
        JSONArray trimmedBoard = lastPeriod.getJSONArray("leaderboard");

        // AU18: The trimmed leaderboard must contain the player!
        boolean playerFound = false;
        for (int i = 0; i < trimmedBoard.length(); i++) {
            JSONObject row = trimmedBoard.getJSONObject(i);
            if (row.optBoolean("isPlayer", false)) {
                playerFound = true;
                break;
            }
        }
        Assert.assertTrue("Trimmed leaderboard archive must contain player row even if ranked outside top 20!", playerFound);
    }

    @Test
    public void testAU19_EquippedAvatarAndFramePersistAcrossSaveLoad() throws Exception {
        PlayerProfileManager ppm = new PlayerProfileManager();
        ppm.setEquippedAvatar("AVATAR-CUSTOM-007");
        ppm.setEquippedFrame("FRAME-DIAMOND-999");

        JSONObject exported = ppm.exportState();
        Assert.assertEquals("AVATAR-CUSTOM-007", exported.getString("equippedAvatar"));
        Assert.assertEquals("FRAME-DIAMOND-999", exported.getString("equippedFrame"));

        PlayerProfileManager ppm2 = new PlayerProfileManager();
        ppm2.restoreState(exported);

        Assert.assertEquals("Equipped avatar must persist after restoreState!", "AVATAR-CUSTOM-007", ppm2.getEquippedAvatar());
        Assert.assertEquals("Equipped frame must persist after restoreState!", "FRAME-DIAMOND-999", ppm2.getEquippedFrame());
    }
}
