package com.tradingglobal3d.core;

import com.tradingglobal3d.core.player.PlayerProfileManager;
import com.tradingglobal3d.core.storage.*;
import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.Collections;
import static org.junit.Assert.*;

public class PrestigeRecoveryRegressionTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private SaveState state(int day) {
        return SaveState.capture(GameMode.INTERNAL_MARKET,
                new GameClock(LocalDate.of(2026, 10, 20).plusDays(day), LocalTime.of(8, 30)),
                Collections.emptyList(), new EconomyLedger());
    }
    @Test public void backupIsExactImmutableAndRecoveryUsesValidatedSave() throws Exception {
        SaveManager saves = SaveManager.forDirectory(temporary.newFolder());
        saves.save(state(0));
        byte[] original = Files.readAllBytes(saves.getSaveFile().toPath());
        String backup = saves.createPrestigeBackup("REQUEST_1");
        assertArrayEquals(original, Files.readAllBytes(Paths.get(backup)));
        assertArrayEquals(original, Files.readAllBytes(saves.getSaveFile().toPath()));
        saves.save(state(2));
        assertEquals(backup, saves.createPrestigeBackup("REQUEST_1"));
        assertArrayEquals(original, Files.readAllBytes(Paths.get(backup)));
        SaveState restored = saves.restorePrestigeBackup(backup);
        assertEquals(LocalDate.of(2026, 10, 20), restored.getGameDate());
        assertEquals(restored.toJsonString(), saves.load().getState().toJsonString());
        assertArrayEquals(original, Files.readAllBytes(Paths.get(backup)));
    }
    @Test public void invalidSourceDestinationAndCorruptRecoveryCannotReplacePrimary() throws Exception {
        File base = temporary.newFolder();
        SaveManager saves = SaveManager.forDirectory(base);
        saves.save(state(0));
        byte[] original = Files.readAllBytes(saves.getSaveFile().toPath());
        for (String request : new String[]{"../escape", "", "a/b", "a.b"}) {
            try { saves.createPrestigeBackup(request); fail(); } catch (IllegalArgumentException expected) { }
        }
        Path blocked = base.toPath().resolve("prestige-backups");
        Files.write(blocked, "blocked".getBytes(StandardCharsets.UTF_8));
        try { saves.createPrestigeBackup("REQ"); fail(); } catch (IOException expected) { }
        assertArrayEquals(original, Files.readAllBytes(saves.getSaveFile().toPath()));
        Files.delete(blocked);
        String backup = saves.createPrestigeBackup("REQ");
        Path outside = temporary.newFile("outside.json").toPath(); Files.write(outside, original);
        try { saves.restorePrestigeBackup(outside.toString()); fail(); } catch (IOException expected) { }
        Files.write(Paths.get(backup), "corrupt".getBytes(StandardCharsets.UTF_8));
        try { saves.restorePrestigeBackup(backup); fail(); } catch (Exception expected) { }
        assertArrayEquals(original, Files.readAllBytes(saves.getSaveFile().toPath()));
        Files.write(saves.getSaveFile().toPath(), "corrupt-primary".getBytes(StandardCharsets.UTF_8));
        try { saves.createPrestigeBackup("NEW"); fail(); } catch (Exception expected) { }
        assertEquals("corrupt-primary", new String(Files.readAllBytes(saves.getSaveFile().toPath()), StandardCharsets.UTF_8));
        assertFalse(Files.exists(base.toPath().resolve("prestige-backups/NEW.json")));
    }
    @Test public void profileRetainsLifetimeIdentityPreferencesAndCosmeticHistoryOnly() throws Exception {
        PlayerProfileManager profile = new PlayerProfileManager();
        profile.setPlayerName("Nama Pemain"); profile.setPreferences("200%", true, true);
        profile.recordTradeResult(Money.of(1000, Currency.IDR));
        profile.recordTradeResult(Money.of(-200, Currency.IDR));
        profile.incrementDaysPlayed(); profile.updateNetWorthPeak(Money.of(90_000_000, Currency.IDR));
        JSONObject before = profile.exportState();
        profile.reset(); profile.retainAcrossPrestige(before);
        assertEquals("Nama Pemain", profile.getPlayerName());
        assertEquals(0, profile.getTotalTradesExecuted()); assertEquals(1, profile.getRunDaysPlayed());
        assertEquals(2, profile.getLifetimeDaysPlayed());
        assertEquals(2, profile.exportState().getInt("lifetimeTradesExecuted"));
        assertEquals("1000", profile.exportState().getString("lifetimeBestTradePnL"));
        assertEquals("-200", profile.exportState().getString("lifetimeWorstTradePnL"));
        assertTrue(profile.exportState().getBoolean("highContrast"));
        JSONObject receipt = profile.recordPrestige("REQ", "RUN_A", "RUN_B", 100, "backup.json");
        assertEquals("BADGE_ERA_BARU", receipt.getString("bonus"));
        receipt.put("bonus", "tampered");
        assertEquals("BADGE_ERA_BARU", profile.recordPrestige("REQ", "RUN_A", "RUN_B", 100, "backup.json").getString("bonus"));
        String saved = profile.exportState().toString();
        PlayerProfileManager loaded = new PlayerProfileManager(); loaded.restoreState(new JSONObject(saved));
        assertEquals(saved, loaded.exportState().toString()); assertEquals(1, loaded.getPrestigeCount());
        try { loaded.recordPrestige("REQ", "RUN_A", "OTHER", 100, "backup.json"); fail(); }
        catch (IllegalArgumentException expected) { }
        try { loaded.recordPrestige("REQ2", "RUN_A", "OTHER", 100, "backup.json"); fail(); }
        catch (IllegalStateException expected) { }
        assertEquals(saved, loaded.exportState().toString());
        loaded.recordTradeResult(Money.of(500, Currency.IDR));
        loaded.incrementDaysPlayed();
        assertEquals(3, loaded.exportState().getInt("lifetimeTradesExecuted"));
        assertEquals(1, loaded.getTotalTradesExecuted());
    }
    @Test public void legacyMigrationAndBadHistoryDoNotPartiallyMutateProfile() throws Exception {
        PlayerProfileManager profile = new PlayerProfileManager();
        profile.restoreState(new JSONObject().put("totalTradesExecuted", 3).put("profitableTradesCount", 1).put("lifetimeDaysPlayed", 7));
        assertEquals(3, profile.exportState().getInt("lifetimeTradesExecuted"));
        assertEquals(7, profile.getRunDaysPlayed());
        String before = profile.exportState().toString();
        JSONObject invalid = new JSONObject(before).put("playerName", "Bad").put("prestigeCount", 2);
        try { profile.restoreState(invalid); fail(); } catch (IllegalArgumentException expected) { }
        assertEquals(before, profile.exportState().toString());
    }
}
