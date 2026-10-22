package com.tradingglobal3d.core;

import com.tradingglobal3d.core.collection.CollectionManager;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.time.LocalDate;
import java.time.ZoneOffset;
import static org.junit.Assert.*;

public class CollectionAccountingRegressionTest {
    private static final String ITEM = "COL-ART-00";
    private long time(int day) { return LocalDate.of(2026, 10, 19).plusDays(day).atTime(23, 59).toInstant(ZoneOffset.ofHours(7)).toEpochMilli(); }
    private Account account(EconomyLedger ledger, long cash) {
        Account account = new Account("PLAYER", "Player", AccountType.PERSONAL, null);
        ledger.recordMutation("INITIAL", account, LedgerEntryType.INITIAL_CAPITAL, Money.of(cash, Currency.IDR), true, time(0), "Test");
        return account;
    }
    private Money wealth(CollectionManager manager, Account account) {
        return account.getTotalCash(Currency.IDR).add(manager.calculateTotalValuation()).subtract(manager.getTotalMaintenanceLiability());
    }

    @Test public void sameTimestampRoundtripsReceiptsAndReloadNeverMintCashOrAssets() throws Exception {
        for (int seed = 0; seed < 10; seed++) {
            CollectionManager manager = new CollectionManager();
            EconomyLedger ledger = new EconomyLedger();
            Account account = account(ledger, 50_000_000);
            for (int action = 0; action < 100; action++) {
                String request = seed + ":" + action;
                boolean buy = action % 2 == 0;
                JSONObject receipt = buy ? manager.buyItem(request, ITEM, account, ledger, null, time(0))
                        : manager.sellItem(request, ITEM, account, ledger, null, time(0));
                String expected = receipt.toString();
                receipt.put("amount", "tampered");
                manager.restoreState(new JSONArray(manager.exportState().toString()));
                JSONObject retry = buy ? manager.buyItem(request, ITEM, account, ledger, null, time(0))
                        : manager.sellItem(request, ITEM, account, ledger, null, time(0));
                assertEquals(expected, retry.toString());
                assertEquals(Money.of(50_000_000, Currency.IDR), wealth(manager, account));
                assertEquals(buy, manager.getItem(ITEM).isOwned());
                assertTrue("seed=" + seed + " action=" + action, ledger.reconcile(account, Currency.IDR));
            }
            assertEquals(Money.of(50_000_000, Currency.IDR), account.getTotalCash(Currency.IDR));
        }
    }

    @Test public void missedUpkeepPersistsAfterSaleAndSameDayRetryDoesNotChargeTwice() throws Exception {
        CollectionManager manager = new CollectionManager();
        EconomyLedger ledger = new EconomyLedger();
        Account account = account(ledger, 15_000_000);
        manager.buyItem("BUY", ITEM, account, ledger, null, time(0));
        manager.processDailyMaintenance(account, ledger, time(0));
        manager.processDailyMaintenance(account, ledger, time(2));
        assertEquals(Money.of(30_000, Currency.IDR), manager.getTotalMaintenanceLiability());
        manager.restoreState(new JSONArray(manager.exportState().toString()));
        manager.processDailyMaintenance(account, ledger, time(2));
        assertEquals(Money.of(30_000, Currency.IDR), manager.getTotalMaintenanceLiability());
        assertEquals("30000", manager.getItem(ITEM).toJsonObject().getString("maintenanceArrears"));
        manager.sellItem("SELL", ITEM, account, ledger, null, time(2));
        assertEquals(Money.of(30_000, Currency.IDR), manager.getTotalMaintenanceLiability());
        assertEquals(Money.of(14_970_000, Currency.IDR), wealth(manager, account));
        manager.processDailyMaintenance(account, ledger, time(2));
        assertEquals(Money.zero(Currency.IDR), manager.getTotalMaintenanceLiability());
        assertEquals(Money.of(14_970_000, Currency.IDR), account.getTotalCash(Currency.IDR));
        int entries = ledger.getEntries().size();
        manager.processDailyMaintenance(account, ledger, time(2));
        assertEquals(entries, ledger.getEntries().size());
        assertTrue(ledger.reconcile(account, Currency.IDR));
    }

    @Test public void invalidOwnershipPayloadAndValuationCannotMutateRun() throws Exception {
        CollectionManager manager = new CollectionManager();
        EconomyLedger ledger = new EconomyLedger();
        Account account = account(ledger, 50_000_000);
        manager.buyItem("BUY", ITEM, account, ledger, null, time(0));
        String before = manager.exportState().toString();
        try { manager.sellItem("BUY", ITEM, account, ledger, null, time(0)); fail(); }
        catch (IllegalArgumentException expected) { }
        Account other = new Account("OTHER", "Other", AccountType.PERSONAL, null);
        try { manager.sellItem("OTHER", ITEM, other, ledger, null, time(0)); fail(); }
        catch (IllegalStateException expected) { }
        for (Money invalid : new Money[]{null, Money.of(-1, Currency.IDR), Money.of(1, Currency.USD),
                new Money(java.math.BigDecimal.valueOf(Long.MAX_VALUE).add(java.math.BigDecimal.ONE), Currency.IDR)}) {
            try { manager.getItem(ITEM).setValuation(invalid); fail(); }
            catch (IllegalArgumentException expected) { }
        }
        JSONArray corrupted = new JSONArray(manager.exportState().toString());
        corrupted.getJSONObject(0).put("currentValuation", "-1");
        try { manager.restoreState(corrupted); fail(); }
        catch (IllegalArgumentException expected) { }
        assertEquals(before, manager.exportState().toString());
        assertEquals(150, manager.getAllItems().size());
        assertTrue(ledger.reconcile(account, Currency.IDR));
    }

    @Test public void zeroValuationPersistsAndSaleCannotMintCashOrAssets() throws Exception {
        CollectionManager manager = new CollectionManager();
        EconomyLedger ledger = new EconomyLedger();
        Account account = account(ledger, 50_000_000);
        manager.buyItem("BUY", ITEM, account, ledger, null, time(0));
        Money cash = account.getTotalCash(Currency.IDR);
        manager.getItem(ITEM).setValuation(Money.zero(Currency.IDR));
        manager.restoreState(new JSONArray(manager.exportState().toString()));
        manager.updateValuations(time(0));
        manager.updateValuations(time(20));
        manager.restoreState(new JSONArray(manager.exportState().toString()));
        assertTrue(manager.getItem(ITEM).isOwned());
        assertEquals(Money.zero(Currency.IDR), manager.calculateTotalValuation());
        assertEquals(cash, wealth(manager, account));
        JSONObject receipt = manager.sellItem("ZERO-SELL", ITEM, account, ledger, null, time(20));
        assertEquals("0", receipt.getString("amount"));
        int entries = ledger.getEntries().size();
        manager.restoreState(new JSONArray(manager.exportState().toString()));
        assertEquals(receipt.toString(), manager.sellItem("ZERO-SELL", ITEM, account, ledger, null, time(20)).toString());
        assertFalse(manager.getItem(ITEM).isOwned());
        assertEquals(entries, ledger.getEntries().size());
        assertEquals(cash, account.getTotalCash(Currency.IDR));
        assertEquals(cash, wealth(manager, account));
        assertTrue(ledger.reconcile(account, Currency.IDR));
    }
}
