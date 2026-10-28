package com.tradingglobal3d.core;

import com.tradingglobal3d.core.research.NewsArticle;
import com.tradingglobal3d.core.research.ResearchService;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class NewsRealDeadlineTest {
    private static final long GAME = 1792373400000L;
    private static final long REAL = 1792756800000L;

    @Test public void sixtyAndHundredTwentyMinuteDeadlinesUseRealTimeAndExclusiveEnd() throws Exception {
        ResearchService service = new ResearchService();
        service.resetForGame(GAME, REAL);
        service.addNews(new NewsArticle("REAL-60", "One hour", "Test", NewsArticle.Category.COMPANY, "TEST60", .6, GAME));
        service.addNews(new NewsArticle("REAL-120", "Two hours", "Test", NewsArticle.Category.COMPANY, "TEST120", .7, GAME, 120));
        service.observeWorldTime(GAME + 43200000L, REAL + 1800000L, false);
        assertEquals(.6, service.getActiveNewsImpact("TEST60", "none", GAME + 43200000L), .0001);
        JSONObject news = find(service, "REAL-60", GAME + 43200000L);
        assertEquals(1800000L, news.getLong("effectRemainingRealMs"));
        service.observeWorldTime(GAME + 86400000L, REAL + 3600000L, false);
        assertEquals(0, service.getActiveNewsImpact("TEST60", "none", GAME + 86400000L), .0001);
        assertEquals(.7, service.getActiveNewsImpact("TEST120", "none", GAME + 86400000L), .0001);
        service.observeWorldTime(GAME + 172800000L, REAL + 7200000L, false);
        assertEquals(0, service.getActiveNewsImpact("TEST120", "none", GAME + 172800000L), .0001);
    }

    @Test public void exportRestoreAndBackwardClockDoNotRestartEffect() throws Exception {
        ResearchService service = new ResearchService();
        service.resetForGame(GAME, REAL);
        service.observeWorldTime(GAME + 43200000L, REAL + 1800000L, false);
        JSONObject before = find(service, "NEWS-02", GAME + 43200000L);
        ResearchService restored = new ResearchService();
        restored.restoreState(service.exportState());
        restored.observeWorldTime(GAME + 43200000L, REAL + 1200000L, false);
        JSONObject after = find(restored, "NEWS-02", GAME + 43200000L);
        assertEquals(before.getLong("effectEndsRealAt"), after.getLong("effectEndsRealAt"));
        assertEquals(before.getLong("effectRemainingRealMs"), after.getLong("effectRemainingRealMs"));
        restored.observeWorldTime(GAME + 129600000L, REAL + 5400000L, false);
        assertEquals(0, restored.getActiveNewsImpact("BBCA", "Keuangan", GAME + 129600000L), .0001);
    }

    @Test public void replayDoesNotApplyInternalNewsSentiment() {
        ResearchService service = new ResearchService();
        service.resetForGame(GAME, REAL);
        service.observeWorldTime(GAME, REAL, true);
        assertEquals(0, service.getActiveNewsImpact("BBCA", "Keuangan", GAME), .0001);
    }

    @Test public void scheduledReleaseDuringOfflineIntervalHasTheSamePersistentDeadline() throws Exception {
        ResearchService bulk = new ResearchService(), parts = new ResearchService();
        bulk.resetForGame(GAME, REAL); parts.resetForGame(GAME, REAL);
        bulk.observeWorldTime(GAME + 129600000L, REAL + 5400000L, false);
        for (int i = 1; i <= 90; i++) parts.observeWorldTime(GAME + i * 1440000L, REAL + i * 60000L, false);
        JSONObject a = find(bulk, "NEWS-FUTURE-01", GAME + 129600000L);
        JSONObject b = find(parts, "NEWS-FUTURE-01", GAME + 129600000L);
        assertEquals(REAL + 150000L, a.getLong("effectStartsRealAt"));
        assertEquals(a.getLong("effectEndsRealAt"), b.getLong("effectEndsRealAt"));
        assertEquals("ENDED", a.getString("effectStatus"));
        assertEquals(0, bulk.getActiveNewsImpact("ADRO", "Energi", GAME + 129600000L), .0001);
    }

    @Test public void readingFeedCannotMovePublicationAnchorOrExtendDeadline() throws Exception {
        ResearchService service = new ResearchService();
        service.resetForGame(GAME, REAL);
        service.getNewsFeed(GAME, REAL + 300000L);
        service.observeWorldTime(GAME + 7200000L, REAL + 300000L, false);
        JSONObject article = find(service, "NEWS-FUTURE-01", GAME + 7200000L);
        assertEquals(REAL + 150000L, article.getLong("effectStartsRealAt"));
        assertEquals(REAL + 3750000L, article.getLong("effectEndsRealAt"));
    }

    private JSONObject find(ResearchService service, String id, long at) throws Exception {
        org.json.JSONArray feed = service.getNewsFeed(at);
        for (int i=0;i<feed.length();i++) if (id.equals(feed.getJSONObject(i).getString("id"))) return feed.getJSONObject(i);
        throw new AssertionError("Missing news " + id);
    }

    @Test public void facadeOfflineCatchupAndRestartPreserveDeadlineAcrossExpiry() throws Exception {
        java.io.File directory = java.nio.file.Files.createTempDirectory(
                java.nio.file.Paths.get(System.getProperty("java.io.tmpdir")), "news-deadline-").toFile();
        com.tradingglobal3d.core.storage.SaveManager saves = com.tradingglobal3d.core.storage.SaveManager.forDirectory(directory);
        com.tradingglobal3d.core.facade.TradingFacade game = open(saves, REAL);
        long world = game.getGameTimestamp();
        JSONObject original = find(game.getResearchService(), "NEWS-02", world);
        assertEquals(REAL + 3600000L, original.getLong("effectEndsRealAt"));
        game.onAppLifecycleChanged(false);
        game = open(saves, REAL + 2700000L);
        JSONObject active = find(game.getResearchService(), "NEWS-02", game.getGameTimestamp());
        assertTrue(active.getBoolean("effectActive"));
        assertEquals(900000L, active.getLong("effectRemainingRealMs"));
        game.onAppLifecycleChanged(false);
        game = open(saves, REAL + 5400000L);
        JSONObject expired = find(game.getResearchService(), "NEWS-02", game.getGameTimestamp());
        assertEquals("ENDED", expired.getString("effectStatus"));
        assertEquals(original.getLong("effectEndsRealAt"), expired.getLong("effectEndsRealAt"));
        assertEquals(0, game.getResearchService().getActiveNewsImpact("BBCA", "Keuangan", game.getGameTimestamp()), .0001);
        assertTrue(game.reconcileAll());
        long after = game.getGameTimestamp();
        game = open(saves, REAL + 5400000L);
        assertEquals(after, game.getGameTimestamp());
        assertEquals(original.getLong("effectEndsRealAt"), find(game.getResearchService(), "NEWS-02", after).getLong("effectEndsRealAt"));
    }

    @Test public void offlineWholeAndPartitionedAcrossPublicationAndDeadlineHaveIdenticalEconomy() throws Exception {
        java.nio.file.Path root = java.nio.file.Files.createTempDirectory(java.nio.file.Path.of(System.getProperty("java.io.tmpdir")), "news-economy-");
        var bulk = open(com.tradingglobal3d.core.storage.SaveManager.forDirectory(root.resolve("bulk").toFile()), REAL);
        var parts = open(com.tradingglobal3d.core.storage.SaveManager.forDirectory(root.resolve("parts").toFile()), REAL);
        bulk.setRealTimeClock(java.time.Clock.fixed(java.time.Instant.ofEpochMilli(REAL + 5400000L), java.time.ZoneOffset.UTC));
        bulk.syncClock();
        for (int i = 1; i <= 90; i++) {
            parts.setRealTimeClock(java.time.Clock.fixed(java.time.Instant.ofEpochMilli(REAL + i * 60000L), java.time.ZoneOffset.UTC));
            parts.syncClock();
            assertTrue(parts.reconcileAll());
        }
        assertEquals(economy(bulk), economy(parts));
        assertEquals(find(bulk.getResearchService(), "NEWS-FUTURE-01", bulk.getGameTimestamp()).getLong("effectEndsRealAt"),
                find(parts.getResearchService(), "NEWS-FUTURE-01", parts.getGameTimestamp()).getLong("effectEndsRealAt"));
        assertTrue(bulk.reconcileAll());
    }

    @Test public void legacyMigrationBackupAndFailedSaveNeverRenewDeadline() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory(java.nio.file.Path.of(System.getProperty("java.io.tmpdir")), "news-legacy-");
        var saves = com.tradingglobal3d.core.storage.SaveManager.forDirectory(dir.toFile());
        var game = open(saves, REAL);
        JSONObject legacy = new JSONObject(java.nio.file.Files.readString(saves.getSaveFile().toPath()));
        JSONObject subscription = legacy.getJSONObject("runtimeState").getJSONObject("subscription");
        subscription.remove("newsEffects"); subscription.remove("observedRealAt"); subscription.remove("observedGameAt");
        java.nio.file.Files.writeString(saves.getSaveFile().toPath(), legacy.toString());
        long migratedAt = REAL + 900000L;
        game = open(saves, migratedAt);
        long deadline = find(game.getResearchService(), "NEWS-02", game.getGameTimestamp()).getLong("effectEndsRealAt");
        assertEquals(migratedAt + 3600000L, deadline);
        game.save(); // ensure backup also contains one-time migration
        game = open(saves, migratedAt + 1000);
        assertEquals(deadline, find(game.getResearchService(), "NEWS-02", game.getGameTimestamp()).getLong("effectEndsRealAt"));
        byte[] durable = java.nio.file.Files.readAllBytes(saves.getSaveFile().toPath());
        var blocked = saves.getTempFile().toPath(); java.nio.file.Files.createDirectory(blocked);
        java.nio.file.Files.write(blocked.resolve("block"), new byte[]{1});
        try { game.save(); fail("write must fail"); } catch (java.io.IOException expected) { }
        assertArrayEquals(durable, java.nio.file.Files.readAllBytes(saves.getSaveFile().toPath()));
        java.nio.file.Files.delete(blocked.resolve("block")); java.nio.file.Files.delete(blocked);
        java.nio.file.Files.writeString(saves.getSaveFile().toPath(), "{broken");
        game = open(saves, migratedAt + 2000);
        assertEquals(deadline, find(game.getResearchService(), "NEWS-02", game.getGameTimestamp()).getLong("effectEndsRealAt"));
        assertTrue(game.reconcileAll());
        game = open(saves, deadline);
        assertEquals("ENDED", find(game.getResearchService(), "NEWS-02", game.getGameTimestamp()).getString("effectStatus"));
    }

    @Test public void marketEngineUsesSameExclusiveDeadlineForSixtyAndHundredTwentyMinutes() throws Exception {
        for (int duration : new int[]{60, 120}) {
            java.util.List<String> active = engineAt(duration, duration * 60000L - 1, false);
            java.util.List<String> exact = engineAt(duration, duration * 60000L, false);
            java.util.List<String> after = engineAt(duration, duration * 60000L + 1, false);
            java.util.List<String> replay = engineAt(duration, duration * 60000L - 1, true);
            assertEquals("engine neutral exactly at deadline", exact, after);
            assertEquals("replay excludes internal news", exact, replay);
            assertNotEquals("active sentiment must change actual NPC economy", active, exact);
        }
    }

    private java.util.List<String> engineAt(int duration, long elapsed, boolean replay) throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory(java.nio.file.Path.of(System.getProperty("java.io.tmpdir")), "news-engine-");
        var game = open(com.tradingglobal3d.core.storage.SaveManager.forDirectory(dir.toFile()), REAL);
        // Same world timestamp and seed/input in each branch; only deadline observation differs.
        game.getClock().advanceGameSeconds(1800);
        var research = game.getResearchService();
        JSONObject state = research.exportState();
        org.json.JSONArray effects = state.getJSONArray("newsEffects");
        for (int i = 0; i < effects.length(); i++) effects.getJSONObject(i).put("durationMinutes", duration);
        research.restoreState(state);
        research.observeWorldTime(game.getGameTimestamp(), REAL + elapsed, replay);
        game.generateNpcLiquidity();
        assertTrue(game.reconcileAll());
        return economy(game);
    }

    private java.util.List<String> economy(com.tradingglobal3d.core.facade.TradingFacade game) {
        java.util.List<String> view = new java.util.ArrayList<>();
        for (var account : game.getAllAccounts()) {
            for (var currency : com.tradingglobal3d.core.model.Currency.values())
                view.add("cash:" + account.getId() + ":" + currency + ":" + account.getTotalCash(currency) + ":" + account.getReservedCash(currency));
            for (var p : account.getPositions().entrySet()) view.add("position:" + account.getId() + ":" + p.getKey() + ":" + p.getValue().getQuantity() + ":" + p.getValue().getMarketValue());
        }
        for (var e : game.getLedger().getEntries()) view.add("ledger:" + e.getAccountId() + ":" + e.getType() + ":" + e.getAmount() + ":" + e.isCredit() + ":" + e.getTimestamp());
        for (var inst : game.getCatalog().getActiveUniverse()) {
            var book = game.getMarketEngine().getBook(inst.getId());
            if (book != null) for (var fill : book.getTradeHistory()) view.add("fill:" + inst.getId() + ":" + fill.getPrice() + ":" + fill.getQuantity() + ":" + fill.getTimestamp());
        }
        java.util.Collections.sort(view); return view;
    }

    private com.tradingglobal3d.core.facade.TradingFacade open(com.tradingglobal3d.core.storage.SaveManager saves, long now) throws Exception {
        com.tradingglobal3d.core.facade.TradingFacade game = new com.tradingglobal3d.core.facade.TradingFacade(saves);
        game.setCatalog(com.tradingglobal3d.core.market.InstrumentCatalog.fromJsonString(new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get("src/main/assets/data/market_universe.json")), java.nio.charset.StandardCharsets.UTF_8)));
        game.setRealTimeClock(java.time.Clock.fixed(java.time.Instant.ofEpochMilli(now), java.time.ZoneOffset.UTC));
        assertTrue(game.startOrResume());
        return game;
    }
}
