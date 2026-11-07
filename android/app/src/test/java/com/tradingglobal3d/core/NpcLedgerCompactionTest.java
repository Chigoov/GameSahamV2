package com.tradingglobal3d.core;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class NpcLedgerCompactionTest {
    @Test
    public void compactionKeepsVisibleRowsAndReceiptsAndReconcilesAfterRestore() throws Exception {
        var ledger = new EconomyLedger();
        var npc = new Account("ACC-LEAGUE-NPC-01", "NPC", AccountType.FIRM, null);
        var player = new Account("player", "Player", AccountType.PERSONAL, null);
        var npcIds = Set.of(npc.getId());
        var large = ledger.recordMutation("SELL-FILL-1", npc, LedgerEntryType.TRADE_SELL,
                Money.of(500_000_000, Currency.IDR), true, 1, "Visible at the threshold");
        var receipt = ledger.recordMutation("IPO-PAY-receipt", npc, LedgerEntryType.TRADE_BUY,
                Money.of(1_000, Currency.IDR), false, 2, "IPO");
        var personal = ledger.recordMutation("SELL-FILL-3", player, LedgerEntryType.TRADE_SELL,
                Money.of(10, Currency.IDR), true, 3, "Player fill");
        for (int i = 4; i <= 25; i++) {
            ledger.recordMutation("SELL-FEE-" + i, npc, LedgerEntryType.TRANSACTION_FEE,
                    Money.of(10, Currency.IDR), false, i, "NPC fee");
        }
        var before = ledger.getEntriesForAccount(npc.getId());
        var recent = List.copyOf(before.subList(before.size() - 10, before.size()));
        int logicalCount = ledger.getEntryCount();

        ledger.compactNpcTrades(npcIds);
        var retained = ledger.getEntriesForAccount(npc.getId());
        assertEquals(12, retained.size());
        assertEquals(recent, retained.subList(2, 12));
        assertSame(large, retained.get(0));
        assertSame(receipt, ledger.getProcessedEntry(receipt.getRequestId()));
        assertSame(personal, ledger.getProcessedEntry(personal.getRequestId()));
        assertEquals(logicalCount, ledger.getEntryCount());
        assertTrue(ledger.reconcile(npc, Currency.IDR));
        assertTrue(ledger.reconcile(player, Currency.IDR));
        assertNull(ledger.getProcessedEntry("SELL-FEE-4"));

        var reopened = new EconomyLedger();
        reopened.restoreEntries(ledger.getEntries());
        reopened.restoreNpcSummary(ledger.exportNpcSummary(), npcIds);
        reopened.compactNpcTrades(npcIds);
        assertEquals(logicalCount, reopened.getEntryCount());
        assertEquals(ledger.getEntries(), reopened.getEntries());
        assertTrue(reopened.reconcile(npc, Currency.IDR));
        assertTrue(reopened.reconcile(player, Currency.IDR));
    }
}
