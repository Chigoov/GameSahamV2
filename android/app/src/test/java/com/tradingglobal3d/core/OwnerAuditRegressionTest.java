package com.tradingglobal3d.core;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.player.PlayerProgressManager;
import com.tradingglobal3d.core.storage.SaveManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.math.BigDecimal;
import java.nio.file.Files;
import static org.junit.Assert.*;

public class OwnerAuditRegressionTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    private TradingFacade game() throws Exception {
        TradingFacade game = new TradingFacade(SaveManager.forDirectory(temporary.newFolder()));
        game.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(java.nio.file.Paths.get("src/main/assets/data/market_universe.json")), java.nio.charset.StandardCharsets.UTF_8)));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        return game;
    }

    @Test public void rejectedEarlyCloseMustNotChargeInterest() throws Exception {
        TradingFacade game = game();
        GameTestSupport.unlockManager(game);
        game.takeMarginLoan(Money.of(1_000_000, Currency.IDR));
        Money cash = game.getPersonalAccount().getTotalCash(Currency.IDR);
        int entries = game.getLedger().getEntries().size();
        try { game.closeDayAndAdvance(); fail("Expected early close rejection"); }
        catch (IllegalStateException expected) { }
        assertEquals(cash, game.getPersonalAccount().getTotalCash(Currency.IDR));
        assertEquals(entries, game.getLedger().getEntries().size());
    }

    @Test public void arbitraryResearchIdsCannotAwardXp() throws Exception {
        TradingFacade game = game();
        for (int i=0; i<5; i++) assertFalse(game.recordResearchRead("NOT-AN-ARTICLE-"+i));
        assertEquals(0, game.getProgressManager().getCurrentXp());
    }

    @Test public void levelThresholdMustAdvanceAtExactBoundary() {
        assertEquals(7, PlayerProgressManager.calculateLevel(1749));
        assertEquals(8, PlayerProgressManager.calculateLevel(1750));
        assertEquals(9, PlayerProgressManager.calculateLevel(2250));
    }

    @Test public void clockMustNotLoseExactOrMultipleDays() {
        GameClock clock = GameClock.createDefault();
        java.time.LocalDate date = clock.getCurrentDate();
        java.time.LocalTime time = clock.getCurrentTime();
        clock.play();
        assertTrue(clock.tick(172800));
        assertEquals(date.plusDays(2), clock.getCurrentDate());
        assertEquals(time, clock.getCurrentTime());
    }

    @Test public void realTimeRatioAndPauseUseOneAuthoritativeClock() throws Exception {
        for (int speed : new int[]{1,2,5}) {
            TradingFacade game = game();
            java.time.LocalDate start = game.getClock().getCurrentDate();
            java.time.LocalTime time = game.getClock().getCurrentTime();
            game.getClock().setSpeedMultiplier(speed);
            game.getClock().play();
            assertTrue(game.tick(3600)); // One real hour/day at 24x, independent of legacy speed.
            assertEquals(start.plusDays(1), game.getClock().getCurrentDate());
            assertEquals(time, game.getClock().getCurrentTime());
            assertEquals(1, game.getCareerManager().getSessionReports().size());
            game.getClock().pause();
            assertFalse(game.tick(300));
            assertEquals(start.plusDays(1), game.getClock().getCurrentDate());
            assertTrue(game.reconcileAll());
        }
    }

    @Test public void skipDayIsExactly24HoursAndSurvivesRetryAndReload() throws Exception {
        TradingFacade game = game();
        java.time.LocalDate start = game.getClock().getCurrentDate();
        java.time.LocalTime time = game.getClock().getCurrentTime();
        game.getClock().play();
        org.json.JSONObject receipt = game.skipDay("SKIP-ONE");
        assertTrue(receipt.toString(), receipt.getBoolean("success"));
        assertFalse("skipDay tidak boleh menghentikan dunia game secara otomatis", game.getClock().isPaused());
        assertEquals(start.plusDays(1), game.getClock().getCurrentDate());
        assertEquals(time, game.getClock().getCurrentTime());
        int entries = game.getLedger().getEntries().size();
        assertEquals(receipt.toString(), game.skipDay("SKIP-ONE").toString());
        assertEquals(entries, game.getLedger().getEntries().size());
        assertTrue(game.load());
        assertEquals(receipt.toString(), game.skipDay("SKIP-ONE").toString());
        assertEquals(start.plusDays(1), game.getClock().getCurrentDate());
        assertEquals(1, game.getCareerManager().getSessionReports().size());
    }

    @Test public void previewCannotPenalizeClientTrust() throws Exception {
        TradingFacade game = game();
        GameTestSupport.unlockManager(game);
        com.tradingglobal3d.core.model.Account client = game.acceptClientMandate("MANDATE-01");
        com.tradingglobal3d.core.firm.ClientMandate mandate = game.getFirmManager().getMandate("MANDATE-01");
        com.tradingglobal3d.core.trading.Order order = com.tradingglobal3d.core.trading.Order.createLimit(
                "PREVIEW-RISK", "IDX:BBCA", client.getId(), com.tradingglobal3d.core.trading.OrderSide.BUY,
                Money.of(10250, Currency.IDR), 3000, 1L);
        int trust = mandate.getTrustScore();
        for (int i=0; i<20; i++) assertFalse(game.previewOrder(order).getBoolean("success"));
        assertEquals(trust, mandate.getTrustScore());
        assertFalse(game.submitOrder(order).isSuccess);
        assertEquals(trust-5, mandate.getTrustScore());
        assertFalse(game.submitOrder(order).isSuccess);
        assertEquals(trust-5, mandate.getTrustScore());
    }

    @Test public void mandateLimitsConvertBothCashAndPositionToBaseCurrency() throws Exception {
        TradingFacade game = game();
        GameTestSupport.unlockManager(game);
        com.tradingglobal3d.core.model.Account client = game.acceptClientMandate("MANDATE-01");
        game.deposit("USD-TEST", client.getId(), Money.of(10_000, Currency.USD), "TEST ONLY");
        com.tradingglobal3d.core.market.Instrument instrument = game.getCatalog().getById("US:AAPL");
        com.tradingglobal3d.core.firm.ClientMandate mandate = game.getFirmManager().getMandate("MANDATE-01");
        com.tradingglobal3d.core.trading.Order small = com.tradingglobal3d.core.trading.Order.createLimit(
                "CROSS-CURRENCY-SMALL", instrument.getId(), client.getId(), com.tradingglobal3d.core.trading.OrderSide.BUY,
                instrument.getCurrentPrice(), 1, 1L);
        assertTrue(game.getFirmManager().validateOrder(mandate, client, small, instrument, BigDecimal.valueOf(15500)).isValid);
        com.tradingglobal3d.core.trading.Order excess = com.tradingglobal3d.core.trading.Order.createLimit(
                "CROSS-CURRENCY-EXCESS", instrument.getId(), client.getId(), com.tradingglobal3d.core.trading.OrderSide.BUY,
                instrument.getCurrentPrice(), 50, 1L);
        assertFalse(game.getFirmManager().validateOrder(mandate, client, excess, instrument, BigDecimal.valueOf(15500)).isValid);
    }

    @Test public void researchUsesGameTimeAndRetainsItsScheduleAfterReload() throws Exception {
        TradingFacade game = game();
        assertEquals(2, game.getNewsFeed().size());
        assertFalse(game.recordResearchRead("NEWS-FUTURE-01"));
        org.json.JSONArray announced = new org.json.JSONArray(game.getNewsFeedJson());
        java.util.Map<String, Long> schedule = new java.util.HashMap<>();
        for (int i=0; i<announced.length(); i++) {
            org.json.JSONObject article = announced.getJSONObject(i);
            schedule.put(article.getString("id"), article.getLong("publishedAt"));
        }
        long publication = schedule.get("NEWS-FUTURE-01");
        game.getClock().advanceGameSeconds((publication - game.getGameTimestamp()) / 1000 - 1);
        assertFalse(game.getNewsFeed().stream().anyMatch(article -> article.getId().equals("NEWS-FUTURE-01")));
        assertFalse(game.recordResearchRead("NEWS-FUTURE-01"));
        game.getClock().advanceGameSeconds(1);
        assertTrue(game.getNewsFeed().stream().anyMatch(article -> article.getId().equals("NEWS-FUTURE-01")));
        game.recordResearchRead("NEWS-FUTURE-01");
        assertEquals(1, game.getProgressManager().getAchievement("reader").getCurrentProgress());
        assertFalse(game.recordResearchRead("NEWS-FUTURE-01"));
        long secondPublication = schedule.get("NEWS-FUTURE-02");
        game.getClock().advanceGameSeconds((secondPublication - game.getGameTimestamp()) / 1000 - 1);
        assertFalse(game.getNewsFeed().stream().anyMatch(article -> article.getId().equals("NEWS-FUTURE-02")));
        assertFalse(game.recordResearchRead("NEWS-FUTURE-02"));
        game.getClock().advanceGameSeconds(1);
        assertTrue(game.getNewsFeed().stream().anyMatch(article -> article.getId().equals("NEWS-FUTURE-02")));
        game.skipDay("NEWS-NEXT-DAY");
        java.util.Set<String> expected = new java.util.HashSet<>();
        for (java.util.Map.Entry<String, Long> article : schedule.entrySet())
            if (article.getValue() <= game.getGameTimestamp()) expected.add(article.getKey());
        java.util.Set<String> published = new java.util.HashSet<>();
        for (com.tradingglobal3d.core.research.NewsArticle article : game.getNewsFeed()) published.add(article.getId());
        assertEquals(expected, published);
        assertTrue(game.load());
        java.util.Set<String> reloaded = new java.util.HashSet<>();
        for (com.tradingglobal3d.core.research.NewsArticle article : game.getNewsFeed()) {
            reloaded.add(article.getId());
            assertEquals(schedule.get(article.getId()).longValue(), article.getPublishedAt());
        }
        assertEquals(published, reloaded);
        assertFalse(game.recordResearchRead("NEWS-FUTURE-01"));
        assertEquals(1, game.getProgressManager().getAchievement("reader").getCurrentProgress());
        GameTestSupport.unlockManager(game);
        assertEquals(game.getClock().getCurrentDate().minusDays(1), game.getCalendar().get(0).getDate());
    }

    @Test public void newGameCannotInheritCareerSubscriptionWatchlistOrSandbox() throws Exception {
        TradingFacade game = game();
        game.skipDay("OLD-SESSION");
        game.getCatalog().addToWatchlist("IDX:BBCA");
        game.createStrategySandbox("OLD-LAB");
        game.deposit("SUB-USD", game.getPersonalAccount().getId(), Money.of(10_000, Currency.USD), "TEST ONLY");
        GameTestSupport.unlockManager(game);
        assertTrue(game.subscribeTerminal(com.tradingglobal3d.core.research.TerminalTier.PRO));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        assertEquals(1, game.getCareerManager().getDaysTraded());
        assertTrue(game.getCareerManager().getSessionReports().isEmpty());
        assertTrue(game.getCatalog().getWatchlist().isEmpty());
        assertNull(game.getReplayEngine().getSandbox("OLD-LAB"));
        assertEquals(com.tradingglobal3d.core.research.TerminalTier.BASIC, game.getResearchService().getCurrentTier());
        assertEquals(0, game.getProgressManager().getCurrentXp());
        assertTrue(game.reconcileAll());
    }

    @Test public void cleanRiskSessionsRequireActualMandatesWithinLimits() throws Exception {
        TradingFacade game = game();
        for (int i=0; i<5; i++) game.skipDay("NO-MANDATE-"+i);
        assertFalse(game.getProgressManager().getAchievement("risk").isUnlocked());
        GameTestSupport.unlockManager(game);
        game.acceptClientMandate("MANDATE-01");
        for (int i=0; i<4; i++) game.skipDay("CLEAN-"+i);
        assertFalse(game.getProgressManager().getAchievement("risk").isUnlocked());
        game.skipDay("CLEAN-4");
        assertTrue(game.getProgressManager().getAchievement("risk").isUnlocked());
    }

    @Test public void restingOrderFillAlsoAwardsTradeAchievement() throws Exception {
        TradingFacade game = game();
        com.tradingglobal3d.core.trading.Order buy = com.tradingglobal3d.core.trading.Order.createLimit(
                "RESTING-BUY", "IDX:BBCA", game.getPersonalAccount().getId(), com.tradingglobal3d.core.trading.OrderSide.BUY,
                Money.of(10000, Currency.IDR), 100, 1L);
        assertTrue(game.submitOrder(buy).isSuccess);
        assertEquals(0, game.getProgressManager().getCurrentXp());
        com.tradingglobal3d.core.trading.Order sell = com.tradingglobal3d.core.trading.Order.createLimit(
                "MATCH-RESTING", "IDX:BBCA", "ACC-NPC-MM-01", com.tradingglobal3d.core.trading.OrderSide.SELL,
                Money.of(10000, Currency.IDR), 100, 2L);
        assertTrue(game.submitOrder(sell).isSuccess);
        assertEquals(100, game.getProgressManager().getCurrentXp());
        game.evaluateTriggers();
        assertEquals(100, game.getProgressManager().getCurrentXp());
    }

    @Test public void invalidProgressSaveCannotReplaceLiveState() throws Exception {
        TradingFacade game = game();
        game.save();
        java.nio.file.Path file = game.getSaveManager().getSaveFile().toPath();
        org.json.JSONObject saved = new org.json.JSONObject(new String(Files.readAllBytes(file), java.nio.charset.StandardCharsets.UTF_8));
        saved.getJSONObject("runtimeState").getJSONObject("playerProgress").put("xp", -1);
        Files.write(file, saved.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        com.tradingglobal3d.core.model.Account account = game.getPersonalAccount();
        game.deposit("UNSAVED", account.getId(), Money.of(1000, Currency.IDR), "TEST ONLY");
        assertFalse(game.load());
        assertSame(account, game.getPersonalAccount());
        assertEquals(Money.of(50_001_000, Currency.IDR), account.getTotalCash(Currency.IDR));
    }

    @Test public void terminalSubscriptionExpiresAtGameTimeBoundary() throws Exception {
        TradingFacade game = game();
        game.deposit("TERMINAL-USD", game.getPersonalAccount().getId(), Money.of(1000, Currency.USD), "TEST ONLY");
        GameTestSupport.unlockManager(game);
        assertTrue(game.subscribeTerminal(com.tradingglobal3d.core.research.TerminalTier.PRO));
        game.getClock().advanceGameSeconds(30L*86400);
        assertEquals(com.tradingglobal3d.core.research.TerminalTier.BASIC, game.getResearchService().getCurrentTier());
        assertFalse(game.getResearchService().hasAccess(com.tradingglobal3d.core.research.TerminalTier.PRO, game.getClock().getGameTimestamp()));
    }

    @Test public void invalidNewGameCapitalCannotClearAnExistingRun() throws Exception {
        TradingFacade game = game();
        com.tradingglobal3d.core.model.Account account = game.getPersonalAccount();
        try { game.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.ZERO); fail("Expected invalid capital rejection"); }
        catch (IllegalArgumentException expected) { }
        assertSame(account, game.getPersonalAccount());
        assertTrue(game.reconcileAll());
    }

    @Test public void productionBridgeCanStartHistoricalReplayMode() throws Exception {
        TradingFacade game = game();
        com.tradingglobal3d.mobile.TradingWebBridge bridge = new com.tradingglobal3d.mobile.TradingWebBridge(game);
        org.json.JSONObject result = new org.json.JSONObject(bridge.startNewGame("{\"mode\":\"HISTORICAL_REPLAY\",\"datasetId\":\"US:AAPL\",\"initialCapital\":50000000}"));
        assertTrue(result.getBoolean("success"));
        assertEquals(GameMode.HISTORICAL_REPLAY, game.getGameMode());
        assertEquals("US:AAPL", result.getString("datasetId"));
        assertTrue(game.getReplayEngine().isReplayActive());

        // 1. Chart anti-leak: only 1 visible bar initially
        org.json.JSONObject chart = game.getChartData("US:AAPL", "1D");
        assertTrue(chart.getBoolean("success"));
        org.json.JSONArray bars = chart.getJSONArray("bars");
        assertEquals(1, bars.length());
        assertEquals("2016-05-09", bars.getJSONObject(0).getString("time"));

        // 2. Submit order in historical replay
        org.json.JSONObject orderReq = new org.json.JSONObject()
                .put("requestId", "HIST-ORDER-1")
                .put("instrumentId", "US:AAPL")
                .put("side", "BUY")
                .put("type", "LIMIT")
                .put("quantity", 10)
                .put("limitPrice", "93.0"); // Open is 93.0, Low is 92.59, High is 93.77
        org.json.JSONObject orderRes = new org.json.JSONObject(bridge.submitOrder(orderReq.toString()));
        assertTrue(orderRes.getBoolean("success"));
        assertEquals("FILLED", orderRes.getString("status"));
        assertEquals(10, game.getPersonalAccount().getPosition("US:AAPL").getQuantity());

        // 3. Advance to next day
        game.skipDay("HIST-SKIP-1");
        org.json.JSONObject chart2 = game.getChartData("US:AAPL", "1D");
        org.json.JSONArray bars2 = chart2.getJSONArray("bars");
        assertEquals(2, bars2.length());
        assertEquals("2016-05-10", bars2.getJSONObject(1).getString("time"));

        // 4. Save and reload
        bridge.saveGame();
        TradingFacade reloaded = new TradingFacade(game.getSaveManager());
        reloaded.setCatalog(game.getCatalog());
        assertTrue(reloaded.load());
        assertEquals(GameMode.HISTORICAL_REPLAY, reloaded.getGameMode());
        assertTrue(reloaded.getReplayEngine().isReplayActive());
        assertEquals(1, reloaded.getReplayEngine().getCurrentBarIndex());
    }

    @Test public void clockFailurePausesInsteadOfRetryingEachTick() throws Exception {
        TradingFacade game = game();
        long untilDateLimit = java.time.Duration.between(game.getClock().getCurrentDate().atTime(game.getClock().getCurrentTime()),
                java.time.LocalDate.MAX.atTime(23, 59, 59)).getSeconds();
        game.getClock().advanceGameSeconds(untilDateLimit);
        game.getClock().play();
        try { game.tick(1); fail("The game timestamp exceeds the supported range"); }
        catch (RuntimeException expected) { }
        assertTrue(game.getClock().isPaused());
        int entries = game.getLedger().getEntries().size();
        assertFalse(game.tick(300));
        assertEquals(entries, game.getLedger().getEntries().size());
    }

    @Test public void readerBadgeIsReachableWithActualGameContent() throws Exception {
        TradingFacade game = game();
        for (int i=0; i<3; i++) game.skipDay("READER-DAY-"+i);
        int target = game.getProgressManager().getAchievement("reader").getTargetProgress();
        assertEquals(5, target);
        assertTrue(game.getNewsFeed().size() >= target);
        for (int i=0; i<target; i++) {
            boolean unlocked = game.recordResearchRead(game.getNewsFeed().get(i).getId());
            assertEquals(i == target-1, unlocked);
            assertEquals(i == target-1 ? 150 : 0, game.getProgressManager().getCurrentXp());
        }
        assertTrue(game.getProgressManager().getAchievement("reader").isUnlocked());
        assertEquals(150, game.getProgressManager().getCurrentXp());
        for (com.tradingglobal3d.core.research.NewsArticle article : game.getNewsFeed()) assertFalse(game.recordResearchRead(article.getId()));
        game.save(); assertTrue(game.load());
        for (com.tradingglobal3d.core.research.NewsArticle article : game.getNewsFeed()) assertFalse(game.recordResearchRead(article.getId()));
        assertEquals(150, game.getProgressManager().getCurrentXp());
    }
}
