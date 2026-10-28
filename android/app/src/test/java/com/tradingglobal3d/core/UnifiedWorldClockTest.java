package com.tradingglobal3d.core;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.mobile.TradingWebBridge;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import static org.junit.Assert.*;

public class UnifiedWorldClockTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private static final long START = 1792756800000L;

    private void time(TradingFacade game, long now) {
        game.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(now), ZoneOffset.UTC));
    }

    private TradingFacade open(File directory, long now) throws Exception {
        TradingFacade game = new TradingFacade(SaveManager.forDirectory(directory));
        game.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(
                Paths.get("src/main/assets/data/market_universe.json")), java.nio.charset.StandardCharsets.UTF_8)));
        time(game, now);
        assertTrue(game.startOrResume());
        return game;
    }

    @Test public void activeHiddenRestartAndMixedHaveEqualElapsedForAllRequiredDurations() throws Exception {
        for (long elapsed : new long[]{300000L, 3600000L, 7200000L}) {
            for (int lifecycle = 0; lifecycle < 4; lifecycle++) {
                File directory = temporary.newFolder();
                TradingFacade game = open(directory, START);
                long initial = game.getGameTimestamp();
                if (lifecycle == 0) {
                    time(game, START + elapsed); game.syncClock();
                } else if (lifecycle == 1) {
                    game.onAppLifecycleChanged(false);
                    time(game, START + elapsed); game.onAppLifecycleChanged(true);
                } else if (lifecycle == 2) {
                    game.save(); game = open(directory, START + elapsed);
                } else {
                    time(game, START + 1900); game.syncClock();
                    time(game, START + 2000); game.onAppLifecycleChanged(false);
                    time(game, START + elapsed - 1000); game.onAppLifecycleChanged(true);
                    time(game, START + elapsed); game.syncClock();
                }
                assertEquals("duration " + elapsed + " lifecycle " + lifecycle, elapsed * 24, game.getGameTimestamp() - initial);
                assertEquals(0, game.getClock().getGameMsRemainder());
                assertTrue(game.reconcileAll());
                assertFalse(game.getClock().isPaused());
                long end = game.getGameTimestamp();
                game.syncClock(); game.onAppLifecycleChanged(true);
                assertEquals(end, game.getGameTimestamp());
            }
        }
    }

    @Test public void failedSaveRetryAndCrashRecoveryKeepElapsedExactlyOnce() throws Exception {
        File directory = temporary.newFolder();
        TradingFacade game = open(directory, START);
        long initial = game.getGameTimestamp();
        byte[] durable = Files.readAllBytes(game.getSaveManager().getSaveFile().toPath());
        File temp = game.getSaveManager().getTempFile();
        assertTrue(temp.mkdir());
        Files.write(temp.toPath().resolve("block"), new byte[]{1});
        time(game, START + 1900); game.syncClock();
        try { game.save(); fail("blocked temporary path must fail save"); }
        catch (java.io.IOException expected) { }
        assertArrayEquals(durable, Files.readAllBytes(game.getSaveManager().getSaveFile().toPath()));
        Files.delete(temp.toPath().resolve("block")); Files.delete(temp.toPath());
        game.save(); game.save();
        game = open(directory, START + 2000);
        assertEquals(initial + 48000, game.getGameTimestamp());
        assertEquals(0, game.getClock().getGameMsRemainder());
        assertTrue(game.reconcileAll());
        game = open(directory, START + 2000);
        assertEquals(initial + 48000, game.getGameTimestamp());
    }

    @Test public void legacyWithoutAnchorUsesSavedTimestampWithoutInventingHistory() throws Exception {
        File directory = temporary.newFolder();
        TradingFacade game = open(directory, START);
        long initial = game.getGameTimestamp();
        File file = game.getSaveManager().getSaveFile();
        JSONObject legacy = new JSONObject(new String(Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8));
        legacy.remove("lastRealTimeAnchor"); legacy.remove("gameMsRemainder"); legacy.remove("lifecycleState");
        legacy.put("savedTimestamp", START);
        legacy.getJSONObject("runtimeState").remove("worldClockVersion");
        Files.write(file.toPath(), legacy.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        game = open(directory, START + 1900);
        assertEquals(initial + 45000, game.getGameTimestamp());
        assertEquals(600, game.getClock().getGameMsRemainder());
        game = open(directory, START + 2000);
        assertEquals(initial + 48000, game.getGameTimestamp());
        assertTrue(game.reconcileAll());
    }

    @Test public void everyLifecycleAndLegacySpeedUsesTwentyFourTimes() {
        for (boolean foreground : new boolean[]{true, false}) {
            for (int speed : new int[]{1, 2, 5}) {
                assertEquals(7200000L, GameClock.convertRealMsToGameMs(300000, foreground, speed));
                assertEquals(86400000L, GameClock.convertRealMsToGameMs(3600000, foreground, speed));
                assertEquals(172800000L, GameClock.convertRealMsToGameMs(7200000, foreground, speed));
            }
        }
    }

    @Test public void partitionedMillisecondsPreserveExactlyTheSameRemainder() {
        GameClock whole = GameClock.createDefault(), parts = GameClock.createDefault();
        long all = whole.advanceRealMs(1234567, true), split = 0;
        for (int i = 0; i < 1234; i++) split += parts.advanceRealMs(1000, i % 2 == 0);
        split += parts.advanceRealMs(567, false);
        assertEquals(all, split);
        assertEquals(whole.getGameMsRemainder(), parts.getGameMsRemainder());
    }

    @Test public void delayedTimerHiddenResumeKeepsTwoSecondsExactly() throws Exception {
        TradingFacade game = open(temporary.newFolder(), START);
        long before = game.getClock().getGameTimestamp();
        time(game, START + 1900); game.syncClock();
        assertEquals(45000L, game.getClock().getGameTimestamp() - before);
        assertEquals(600, game.getClock().getGameMsRemainder());
        time(game, START + 2000); game.onAppLifecycleChanged(false);
        assertEquals(48000L, game.getClock().getGameTimestamp() - before);
        assertEquals(0, game.getClock().getGameMsRemainder());
        game.onAppLifecycleChanged(false);
        time(game, START + 3000); game.onAppLifecycleChanged(true);
        game.onAppLifecycleChanged(true);
        assertEquals(72000L, game.getClock().getGameTimestamp() - before);
        assertTrue(game.reconcileAll());
    }

    @Test public void backwardClockAndRestartNeverLowerTheAnchorOrRepeatElapsed() throws Exception {
        File directory = temporary.newFolder();
        TradingFacade game = open(directory, START);
        long before = game.getClock().getGameTimestamp();
        time(game, START + 2000); game.syncClock();
        time(game, START + 1000); game.onAppLifecycleChanged(false);
        assertEquals(START + 2000, game.getLastRealTimeAnchor());
        game = open(directory, START + 1500);
        assertEquals(START + 2000, game.getLastRealTimeAnchor());
        time(game, START + 2500); game.syncClock();
        assertEquals(60000L, game.getClock().getGameTimestamp() - before);
        assertTrue(game.reconcileAll());
    }

    @Test public void restartWithNoBackgroundCallbackUsesTheSameRateAndPreservesCash() throws Exception {
        File directory = temporary.newFolder();
        TradingFacade game = open(directory, START);
        long before = game.getClock().getGameTimestamp();
        Object cash = game.getPersonalAccount().getTotalCash(com.tradingglobal3d.core.model.Currency.IDR);
        game.save();
        game = open(directory, START + 300000);
        assertEquals(7200000L, game.getClock().getGameTimestamp() - before);
        assertEquals(cash, game.getPersonalAccount().getTotalCash(com.tradingglobal3d.core.model.Currency.IDR));
        game = open(directory, START + 300000);
        assertEquals(7200000L, game.getClock().getGameTimestamp() - before);
        assertTrue(game.reconcileAll());
    }

    @Test public void oldPlayerControlEndpointsCannotAdvanceOrPauseTheWorld() throws Exception {
        TradingFacade game = open(temporary.newFolder(), START);
        TradingWebBridge bridge = new TradingWebBridge(game);
        long before = game.getClock().getGameTimestamp();
        bridge.setClockState(false, 5);
        assertFalse(game.getClock().isPaused());
        assertEquals(1, game.getClock().getSpeedMultiplier());
        assertFalse(new JSONObject(bridge.skipDay("NO-PLAYER-SKIP")).getBoolean("success"));
        assertFalse(new JSONObject(bridge.tickClock(86400)).getBoolean("success"));
        assertFalse(new JSONObject(bridge.closeDay("NO-PLAYER-CLOSE")).getBoolean("success"));
        assertEquals(before, game.getClock().getGameTimestamp());
        time(game, START + 1000); game.syncClock();
        assertEquals(24000L, game.getClock().getGameTimestamp() - before);
    }

    @Test public void legacySaveMigrationKeepsWorldFractionAndBalancesAndRunsOnce() throws Exception {
        File directory = temporary.newFolder();
        TradingFacade game = open(directory, START);
        long before = game.getClock().getGameTimestamp();
        game.getClock().setGameMsRemainder(456);
        game.save();
        File file = game.getSaveManager().getSaveFile();
        JSONObject legacy = new JSONObject(new String(Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8));
        legacy.put("speedMultiplier", 5);
        legacy.getJSONObject("runtimeState").remove("worldClockVersion");
        legacy.getJSONObject("runtimeState").put("rulesVersion", "2026.10.20.2");
        Files.write(file.toPath(), legacy.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        game = open(directory, START + 1000);
        assertEquals(before + 24000L, game.getClock().getGameTimestamp());
        assertEquals(456, game.getClock().getGameMsRemainder());
        assertEquals(1, game.getClock().getSpeedMultiplier());
        JSONObject migrated = new JSONObject(new String(Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8));
        assertEquals(legacy.getJSONArray("accounts").length(), migrated.getJSONArray("accounts").length());
        for (int i = 0; i < legacy.getJSONArray("accounts").length(); i++) {
            JSONObject expected = legacy.getJSONArray("accounts").getJSONObject(i);
            JSONObject actual = null;
            for (int j = 0; j < migrated.getJSONArray("accounts").length(); j++) {
                JSONObject candidate = migrated.getJSONArray("accounts").getJSONObject(j);
                if (expected.getString("id").equals(candidate.getString("id"))) actual = candidate;
            }
            assertNotNull("Missing account " + expected.getString("id"), actual);
            assertTrue("Migration changed account " + expected + " to " + actual, expected.similar(actual));
        }
        assertTrue(legacy.getJSONArray("ledger").similar(migrated.getJSONArray("ledger")));
        assertEquals(1, migrated.getJSONObject("runtimeState").getInt("worldClockVersion"));
        game = open(directory, START + 1000);
        assertEquals(before + 24000L, game.getClock().getGameTimestamp());
        assertEquals(456, game.getClock().getGameMsRemainder());
        assertTrue(game.reconcileAll());
    }
}
