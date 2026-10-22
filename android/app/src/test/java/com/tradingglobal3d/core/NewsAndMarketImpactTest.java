package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.research.NewsArticle;
import com.tradingglobal3d.core.research.ResearchService;
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
 * Pengujian verifikasi R12: News berjalan, anti-future leak, dan berdampak nyata ke likuiditas NPC.
 */
public class NewsAndMarketImpactTest {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "r12_news_test_" + UUID.randomUUID());
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
    public void testNewsCountdownAndAntiFutureLeak() throws Exception {
        ResearchService rs = facade.getResearchService();
        long now = facade.getGameTimestamp();

        JSONArray feed = rs.getNewsFeed(now);
        assertTrue("News feed harus memiliki minimal 3 artikel", feed.length() >= 3);

        boolean foundFuture = false;
        boolean foundPublished = false;

        for (int i = 0; i < feed.length(); i++) {
            JSONObject n = feed.getJSONObject(i);
            boolean isPub = n.getBoolean("isPublished");
            String cd = n.getString("countdownLabel");
            assertNotNull(cd);

            if (!isPub) {
                foundFuture = true;
                // Anti-future leak check: isi dirahasiakan sebelum terbit!
                assertTrue("Headline berita mendatang harus disamarkan", n.getString("headline").startsWith("[Jadwal Rilis]"));
                assertEquals("Sentimen sebelum terbit harus 0.0", 0.0, n.getDouble("sentimentImpact"), 0.001);
                assertTrue("Countdown harus format 'Rilis dalam'", cd.startsWith("Rilis dalam"));
            } else {
                foundPublished = true;
                assertTrue("Countdown berita terbit harus format 'lalu'", cd.contains("lalu"));
                assertFalse("Headline terbit tidak boleh disamarkan", n.getString("headline").startsWith("[Jadwal Rilis]"));
            }
        }

        assertTrue("Harus ada berita mendatang (future scheduled)", foundFuture);
        assertTrue("Harus ada berita yang sudah terbit", foundPublished);
    }

    @Test
    public void testActiveNewsImpactOnNpcOrders() {
        ResearchService rs = facade.getResearchService();
        long now = facade.getGameTimestamp();

        // Cek dampak berita aktif pada BBCA (ada berita positif NEWS-02)
        double bbcaImpact = rs.getActiveNewsImpact("BBCA", "Keuangan", now);
        assertTrue("Dampak berita positif BBCA harus > 0", bbcaImpact > 0.0);

        // Emiten tanpa berita harus bernilai 0
        double neutralImpact = rs.getActiveNewsImpact("UNVR", "Konsumer", now);
        assertEquals("Emiten tanpa berita aktif harus 0", 0.0, neutralImpact, 0.001);
    }
}
