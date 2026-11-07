package com.tradingglobal3d.core;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.storage.SaveState;
import org.json.JSONObject;
import org.junit.Test;

import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.*;

public class SaveStreamingCompatibilityTest {
    @Test public void oldFileMigratesInMemoryAndKeepsCareerBoundaryAndPrimaryBytes() throws Exception {
        var directory = Files.createTempDirectory("su-stream-legacy-");
        var manager = SaveManager.forDirectory(directory.toFile());
        var npc = new Account("ACC-LEAGUE-NPC-01", "NPC", AccountType.FIRM, null);
        var player = new Account("player", "Pemain", AccountType.PERSONAL, null);
        var ledger = new EconomyLedger();
        ledger.recordMutation("initial", npc, LedgerEntryType.INITIAL_CAPITAL, Money.of(1000, Currency.IDR), true, 1, "Modal");
        for (int i = 0; i < 30; i++)
            ledger.recordMutation("SELL-FEE-" + i, npc, LedgerEntryType.TRANSACTION_FEE, Money.of(1, Currency.IDR), false, i + 2, "Biaya \"transaksi\" 🧾");
        int oldIndex = 16;
        ledger.recordMutation("salary", player, LedgerEntryType.SALARY, Money.of(10, Currency.IDR), true, 40, "Gaji");
        var original = SaveState.capture(GameMode.INTERNAL_MARKET,
                new GameClock(LocalDate.of(2026, 10, 19), LocalTime.NOON), List.of(npc, player), ledger);
        original.setRuntimeState(new JSONObject().put("career", new JSONObject().put("ledgerIndex", oldIndex)));
        byte[] before = original.toJsonString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(manager.getSaveFile().toPath(), before);
        var loaded = manager.load();
        assertTrue(loaded.getMessage(), loaded.isSuccess());
        var migrated = loaded.getState();
        assertEquals(SaveState.COMPACTED_SCHEMA_VERSION, migrated.getSchemaVersion());
        assertArrayEquals(before, Files.readAllBytes(manager.getSaveFile().toPath()));
        assertFalse(manager.getBackupFile().exists());
        // Only the initial-capital row precedes this boundary after the old internal fees are summarized.
        assertEquals(1, migrated.getRuntimeState().getJSONObject("career").getInt("ledgerIndex"));
        assertEquals("salary", migrated.getLedgerEntries().get(migrated.getLedgerEntries().size() - 1).getRequestId());
        var restored = new EconomyLedger();
        restored.restoreEntries(migrated.getLedgerEntries());
        restored.restoreNpcSummary(migrated.getRuntimeState().getJSONObject("npcLedgerSummary"), java.util.Set.of(npc.getId()));
        assertEquals(ledger.getEntryCount(), restored.getEntryCount());
        assertTrue(restored.reconcile(npc, Currency.IDR));
        assertTrue(restored.reconcile(player, Currency.IDR));
        manager.save(migrated);
        assertArrayEquals(before, Files.readAllBytes(manager.getBackupFile().toPath()));
        assertEquals(SaveState.COMPACTED_SCHEMA_VERSION, manager.load().getState().getSchemaVersion());
    }

    @Test public void malformedOrDuplicateRootCannotBecomeAnAcceptedSave() throws Exception {
        for (String contents : List.of("{\"schemaVersion\":2,\"schemaVersion\":2,\"ledger\":[]}",
                "{\"ledger\":[null]}", "{\"ledger\":{}}", "{\"ledger\":[]}{}", "{\"ledger\":[]}x")) {
            var manager = SaveManager.forDirectory(Files.createTempDirectory("su-stream-invalid-").toFile());
            Files.writeString(manager.getSaveFile().toPath(), contents);
            assertFalse(contents, manager.load().isSuccess());
            assertEquals(contents, Files.readString(manager.getSaveFile().toPath()));
        }
    }
}
