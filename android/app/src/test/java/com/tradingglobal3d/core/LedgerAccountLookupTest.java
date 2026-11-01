package com.tradingglobal3d.core;

import com.tradingglobal3d.core.economy.*;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import org.junit.Test;
import static org.junit.Assert.*;

public class LedgerAccountLookupTest {
    @Test public void accountLookupDoesNotScanUnrelatedMarketHistoryAndRestoreKeepsOrder() {
        EconomyLedger ledger = new EconomyLedger();
        Account npc = new Account("NPC", "NPC", AccountType.FIRM, null);
        Account player = new Account("PLAYER", "Player", AccountType.PERSONAL, null);
        for (int i = 0; i < 100000; i++) ledger.recordMutation("N" + i, npc, LedgerEntryType.DEPOSIT,
                Money.of(1, Currency.IDR), true, i, "Isolated test");
        ledger.recordMutation("P1", player, LedgerEntryType.DEPOSIT, Money.of(1, Currency.IDR), true, 1, "First");
        ledger.recordMutation("P2", player, LedgerEntryType.DEPOSIT, Money.of(2, Currency.IDR), true, 2, "Second");
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) assertEquals(2, ledger.getEntriesForAccount("PLAYER").size());
        long elapsedMs = (System.nanoTime() - start) / 1000000;
        System.out.println("account lookup 1000 queries / 100000 unrelated entries ms=" + elapsedMs);
        assertTrue("Account lookup must remain local under long offline history: " + elapsedMs, elapsedMs <= 300);
        java.util.List<LedgerEntry> snapshot = ledger.getEntries();
        ledger.clear(); assertTrue(ledger.getEntriesForAccount("PLAYER").isEmpty());
        ledger.restoreEntries(snapshot);
        assertEquals("P1", ledger.getEntriesForAccount("PLAYER").get(0).getRequestId());
        assertEquals("P2", ledger.getEntriesForAccount("PLAYER").get(1).getRequestId());
        ledger.getEntriesForAccount("PLAYER").clear();
        assertEquals(2, ledger.getEntriesForAccount("PLAYER").size());
        ledger.recordMutation("P2", player, LedgerEntryType.DEPOSIT, Money.of(2, Currency.IDR), true, 2, "Duplicate");
        assertEquals(2, ledger.getEntriesForAccount("PLAYER").size());
        assertTrue(ledger.reconcile(player, Currency.IDR));
    }
}
