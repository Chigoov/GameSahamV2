package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.storage.SaveManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Random;
import static org.junit.Assert.*;

public class AutomaticWorldTimeTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private static final long START = 1792756800000L;

    private void time(TradingFacade game, long now) {
        game.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(now), ZoneOffset.UTC));
    }

    private TradingFacade open(File save, long now) throws Exception {
        TradingFacade game = new TradingFacade(SaveManager.forDirectory(save));
        game.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(
                Paths.get("src/main/assets/data/market_universe.json")), java.nio.charset.StandardCharsets.UTF_8)));
        time(game, now);
        assertTrue(game.startOrResume());
        assertFalse(game.getClock().isPaused());
        return game;
    }

    @Test public void freshGameStartsAndPersistsWithoutPlayButton() throws Exception {
        TradingFacade game = open(temporary.newFolder(), START);
        assertTrue(game.getSaveManager().getSaveFile().exists());
        long before = game.getClock().getGameTimestamp();
        game.tick(1);
        assertEquals(24000L, game.getClock().getGameTimestamp() - before);
        game.setClockState(false, 1);
        assertFalse(game.getClock().isPaused());
    }

    @Test public void duplicateLifecycleCallbacksDoNotLoseOrRepeatOfflineTime() throws Exception {
        TradingFacade game = open(temporary.newFolder(), START);
        long before = game.getClock().getGameTimestamp();
        game.onAppLifecycleChanged(false);
        time(game, START + 1800000);
        game.onAppLifecycleChanged(false);
        assertEquals(START, game.getLastRealTimeAnchor());
        game.tick(10);
        assertEquals(before, game.getClock().getGameTimestamp());
        time(game, START + 3600000);
        game.onAppLifecycleChanged(true);
        assertEquals(86400000L, game.getClock().getGameTimestamp() - before);
        game.onAppLifecycleChanged(true);
        assertEquals(86400000L, game.getClock().getGameTimestamp() - before);
        assertTrue(game.reconcileAll());
    }

    @Test public void shortForegroundVisitsKeepTheirFractionalTime() throws Exception {
        TradingFacade game = open(temporary.newFolder(), START);
        long before = game.getClock().getGameTimestamp();
        time(game, START + 900);
        game.onAppLifecycleChanged(false);
        assertEquals(21000L, game.getClock().getGameTimestamp() - before);
        assertEquals(600, game.getClock().getGameMsRemainder());
        time(game, START + 1900);
        game.onAppLifecycleChanged(true);
        assertEquals(45000L, game.getClock().getGameTimestamp() - before);
        assertEquals(600, game.getClock().getGameMsRemainder());
    }

    @Test public void delayedTimerThenBackgroundDoesNotDropTheTimerRemainder() throws Exception {
        TradingFacade game = open(temporary.newFolder(), START);
        long before = game.getClock().getGameTimestamp();
        time(game, START + 1900);
        game.syncClock();
        time(game, START + 2000);
        game.onAppLifecycleChanged(false);
        assertEquals(48000L, game.getClock().getGameTimestamp() - before);
        assertEquals(0, game.getClock().getGameMsRemainder());
    }

    @Test public void closedAppAdvancesMarketAndDailyActivitiesOnce() throws Exception {
        File save = temporary.newFolder();
        TradingFacade game = open(save, START);
        long before = game.getClock().getGameTimestamp();
        int initialDays = game.getCareerManager().getDaysTraded();
        game.onAppLifecycleChanged(false);
        TradingFacade reopened = open(save, START + 3600000);
        assertEquals(86400000L, reopened.getClock().getGameTimestamp() - before);
        assertEquals(initialDays + 1, reopened.getCareerManager().getDaysTraded());
        assertTrue("Offline market must execute trades", reopened.getMarketEngine()
                .getOrCreateBook(reopened.getCatalog().getById("IDX:BBCA")).getTradeHistory().size() > 0);
        assertTrue(reopened.reconcileAll());
        TradingFacade again = open(save, START + 3600000);
        assertEquals(reopened.getClock().getGameTimestamp(), again.getClock().getGameTimestamp());
        assertEquals(initialDays + 1, again.getCareerManager().getDaysTraded());
    }

    @Test public void skippingAndClosingADayKeepTheClockRunning() throws Exception {
        TradingFacade game = open(temporary.newFolder(), START);
        assertTrue(game.skipDay("AUTO-SKIP").getBoolean("success"));
        assertFalse(game.getClock().isPaused());
        game.getClock().advanceGameSeconds(8 * 3600);
        game.closeDay("AUTO-CLOSE");
        assertFalse(game.getClock().isPaused());
        long before = game.getClock().getGameTimestamp();
        game.tick(1);
        assertEquals(24000L, game.getClock().getGameTimestamp() - before);
    }

    @Test public void returningFromReplayCatchesUpTheInternalWorld() throws Exception {
        TradingFacade game = open(temporary.newFolder(), START);
        long before = game.getClock().getGameTimestamp();
        assertTrue(game.startHistoricalReplay("US:AAPL", java.math.BigDecimal.valueOf(50000000)).getBoolean("success"));
        time(game, START + 3600000);
        assertTrue(game.resumeSavedMode("INTERNAL_MARKET").getBoolean("success"));
        assertEquals(86400000L, game.getClock().getGameTimestamp() - before);
        assertFalse(game.getClock().isPaused());
        assertTrue(game.reconcileAll());
    }

    @Test public void failedModeSwitchLeavesTheVisibleWorldRunning() throws Exception {
        TradingFacade game = open(temporary.newFolder(), START);
        java.nio.file.Path file = game.getSaveManager().getSaveFile().toPath();
        Files.delete(file);
        Files.createDirectory(file);
        Files.write(file.resolve("prevent-replacement"), new byte[]{1});
        try {
            game.startHistoricalReplay("US:AAPL", java.math.BigDecimal.valueOf(50000000));
            fail("An unwritable save must prevent the mode switch");
        } catch (IllegalStateException expected) { }
        assertEquals(com.tradingglobal3d.core.model.GameMode.INTERNAL_MARKET, game.getGameMode());
        assertTrue(game.getClock().isForeground());
        assertFalse(game.getClock().isPaused());
        long before = game.getClock().getGameTimestamp();
        time(game, START + 1000);
        game.syncClock();
        assertEquals(24000L, game.getClock().getGameTimestamp() - before);
        assertTrue(game.reconcileAll());
    }

    @Test public void tenSeedsOfHundredLifecycleAndSaveActionsPreserveTimeAndMoney() throws Exception {
        for (int seed = 0; seed < 10; seed++) {
            Random random = new Random(seed);
            File save = temporary.newFolder();
            long now = START;
            TradingFacade game = open(save, now);
            long initialGameTime = game.getClock().getGameTimestamp(), expectedMs = 0, deposits = 0;
            for (int step = 0; step < 100; step++) {
                long elapsed = 1 + random.nextInt(2000);
                switch (random.nextInt(4)) {
                    case 0:
                        now += elapsed; time(game, now);
                        game.advanceRealMs(elapsed, true); expectedMs += elapsed * 24;
                        break;
                    case 1:
                        game.onAppLifecycleChanged(false);
                        now += elapsed; time(game, now);
                        game.onAppLifecycleChanged(false);
                        game.onAppLifecycleChanged(true); expectedMs += elapsed * 24;
                        break;
                    case 2:
                        game.save(); now += elapsed;
                        game = open(save, now); expectedMs += elapsed * 24;
                        break;
                    default:
                        game.deposit("AUTO-" + seed + "-" + step, game.getPersonalAccount().getId(),
                                Money.of(100, Currency.IDR), "Isolated sequence check");
                        deposits += 100;
                }
                assertEquals("seed " + seed + " step " + step, initialGameTime + expectedMs / 1000 * 1000,
                        game.getClock().getGameTimestamp());
                assertEquals(expectedMs % 1000, game.getClock().getGameMsRemainder());
                assertEquals(Money.of(50000000 + deposits, Currency.IDR), game.getPersonalAccount().getTotalCash(Currency.IDR));
                assertFalse(game.getClock().isPaused());
                assertTrue(game.reconcileAll());
            }
        }
    }
}
