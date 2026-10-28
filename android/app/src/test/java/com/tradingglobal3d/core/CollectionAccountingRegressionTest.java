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

    /**
     * Regresi K-018:
     * Domain harus mengikat persetujuan harga (expectedPrice).
     * Jika valuasi berubah di antara review dan confirm, domain menolak transaksi
     * dengan status STALE_VALUATION tanpa melakukan mutasi ekonomi apa pun.
     * Transaksi baru berhasil hanya jika pengguna meninjau ulang dan menyetujui harga baru.
     * Retry dengan requestId yang sama harus idempoten dan mengembalikan receipt aktual.
     */
    @Test
    public void testCollectionPriceAgreementAndStaleValuationRejectionK018() throws Exception {
        CollectionManager manager = new CollectionManager();
        EconomyLedger ledger = new EconomyLedger();
        Account account = account(ledger, 50_000_000);

        manager.updateValuations(time(0));
        var item = manager.getItem(ITEM);
        java.math.BigDecimal initialPrice = item.getCurrentValuation().getAmount();

        // 1. BUY dengan expectedPrice yang tidak cocok harus ditolak tanpa mutasi
        JSONObject staleBuy = manager.buyItem("REQ-BUY-STALE", ITEM, account, ledger, null, time(0), initialPrice.subtract(java.math.BigDecimal.TEN));
        assertFalse("Buy dengan harga stale harus gagal", staleBuy.getBoolean("success"));
        assertEquals("STALE_VALUATION", staleBuy.getString("status"));
        assertFalse("Item tidak boleh terbeli", item.isOwned());
        assertEquals("Saldo tidak boleh termutasi", Money.of(50_000_000, Currency.IDR), account.getTotalCash(Currency.IDR));
        assertEquals("Ledger tidak boleh bertambah", 1, ledger.getEntries().size());

        // 2. BUY dengan expectedPrice yang cocok harus berhasil
        JSONObject validBuy = manager.buyItem("REQ-BUY-VALID", ITEM, account, ledger, null, time(0), initialPrice);
        assertTrue("Buy dengan harga valid harus sukses", validBuy.getBoolean("success"));
        assertTrue("Item harus terbeli", item.isOwned());
        assertEquals(initialPrice.toPlainString(), validBuy.getString("amount"));

        // 3. Retry idempoten pada REQ-BUY-VALID harus mengembalikan receipt yang sama persis
        JSONObject retryBuy = manager.buyItem("REQ-BUY-VALID", ITEM, account, ledger, null, time(0), initialPrice);
        assertTrue("Retry idempoten harus sukses", retryBuy.getBoolean("success"));
        assertEquals(validBuy.getString("receiptId"), retryBuy.getString("receiptId"));
        assertEquals(validBuy.getString("amount"), retryBuy.getString("amount"));

        // 4. Ubah valuasi (simulasi pergantian hari kalender)
        manager.updateValuations(time(2));
        java.math.BigDecimal newPrice = item.getCurrentValuation().getAmount();
        assertNotEquals("Valuasi harus berubah pada hari baru", initialPrice, newPrice);

        // 5. SELL dengan expectedPrice lama (stale) harus ditolak oleh domain
        JSONObject staleSell = manager.sellItem("REQ-SELL-STALE", ITEM, account, ledger, null, time(2), initialPrice);
        assertFalse("Sell dengan harga stale harus ditolak domain", staleSell.getBoolean("success"));
        assertEquals("STALE_VALUATION", staleSell.getString("status"));
        assertTrue("Item masih harus tetap dimiliki karena transaksi ditolak", item.isOwned());

        // 6. SELL dengan expectedPrice baru (sesudah review) harus berhasil
        JSONObject validSell = manager.sellItem("REQ-SELL-VALID", ITEM, account, ledger, null, time(2), newPrice);
        assertTrue("Sell dengan harga baru harus sukses", validSell.getBoolean("success"));
        assertFalse("Item harus terjual", item.isOwned());
        assertEquals(newPrice.toPlainString(), validSell.getString("amount"));

        // 7. Uji Pemulihan Respons Hilang (Lost Response Recovery K-018):
        // Kasus A: Beli item kedua, simulasikan commit terjadi di domain tetapi respons hilang di klien.
        // Klien melakukan retry dengan requestId yang sama ("REQ-BUY-RECOVER"):
        final String ITEM2 = "COL-PROP-00";
        var item2 = manager.getItem(ITEM2);
        java.math.BigDecimal item2Price = item2.getCurrentValuation().getAmount();

        JSONObject firstBuyAttempt = manager.buyItem("REQ-BUY-RECOVER", ITEM2, account, ledger, null, time(2), item2Price);
        assertTrue(firstBuyAttempt.getBoolean("success"));
        assertTrue(item2.isOwned());
        Money cashAfterCommit = account.getTotalCash(Currency.IDR);
        int ledgerCountAfterCommit = ledger.getEntries().size();

        // Klien mengalami error/response lost, lalu mengirim ulang permintaan dengan requestId yang sama persis:
        JSONObject retryBuyAttempt = manager.buyItem("REQ-BUY-RECOVER", ITEM2, account, ledger, null, time(2), item2Price);
        assertTrue("Retry setelah respons putus harus mengembalikan receipt sukses awal", retryBuyAttempt.getBoolean("success"));
        assertEquals("Receipt ID harus identik", firstBuyAttempt.getString("receiptId"), retryBuyAttempt.getString("receiptId"));
        assertEquals("Nominal transaksi harus identik", firstBuyAttempt.getString("amount"), retryBuyAttempt.getString("amount"));
        assertEquals("Kas tidak boleh terdebit dua kali", cashAfterCommit, account.getTotalCash(Currency.IDR));
        assertEquals("Ledger tidak boleh bertambah dua kali", ledgerCountAfterCommit, ledger.getEntries().size());

        // Kontras kasus salah: Jika klien salah membuat requestId baru untuk item yang sudah dimiliki, domain menolak
        try {
            manager.buyItem("REQ-BUY-NEW-WRONG-ID", ITEM2, account, ledger, null, time(2), item2Price);
            fail("Mengirim requestId baru pada item yang sudah dimiliki harus ditolak");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("sudah dimiliki"));
        }

        // Kasus B: Pemulihan transaksi jual yang responsnya hilang (SELL response lost recovery)
        JSONObject firstSellAttempt = manager.sellItem("REQ-SELL-RECOVER", ITEM2, account, ledger, null, time(2), item2Price);
        assertTrue(firstSellAttempt.getBoolean("success"));
        assertFalse(item2.isOwned());
        Money cashAfterSellCommit = account.getTotalCash(Currency.IDR);
        int ledgerCountAfterSellCommit = ledger.getEntries().size();

        // Klien retries dengan requestId yang sama:
        JSONObject retrySellAttempt = manager.sellItem("REQ-SELL-RECOVER", ITEM2, account, ledger, null, time(2), item2Price);
        assertTrue("Retry sell setelah respons putus harus mengembalikan receipt sukses awal", retrySellAttempt.getBoolean("success"));
        assertEquals("Receipt ID sell harus identik", firstSellAttempt.getString("receiptId"), retrySellAttempt.getString("receiptId"));
        assertEquals("Kas tidak boleh terkredit dua kali", cashAfterSellCommit, account.getTotalCash(Currency.IDR));
        assertEquals("Ledger sell tidak boleh bertambah dua kali", ledgerCountAfterSellCommit, ledger.getEntries().size());

        // Kasus C: Persistensi receipt lintas save/reload
        manager.restoreState(new JSONArray(manager.exportState().toString()));
        JSONObject reloadedRecovery = manager.buyItem("REQ-BUY-RECOVER", ITEM2, account, ledger, null, time(2), item2Price);
        assertTrue("Setelah reload, pemulihan receipt lama harus tetap berhasil", reloadedRecovery.getBoolean("success"));
        assertEquals(firstBuyAttempt.getString("receiptId"), reloadedRecovery.getString("receiptId"));

        // 8. Buktikan ledger rekonsiliasi konsisten
        assertTrue(ledger.reconcile(account, Currency.IDR));
    }
}
