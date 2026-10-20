package com.tradingglobal3d.core;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.funds.MutualFund;
import com.tradingglobal3d.core.funds.MutualFundManager;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import static org.junit.Assert.*;

public class FundDealingRegressionTest {
    private static Money cash(long amount) { return Money.of(amount, Currency.IDR); }
    private static long time(int day, int hour, int minute) {
        return LocalDate.of(2026, 10, 19).plusDays(day).atTime(hour, minute).toInstant(ZoneOffset.ofHours(7)).toEpochMilli();
    }
    private static final class Setup {
        MutualFundManager manager = new MutualFundManager();
        MutualFund fund = manager.getFund("FUND-MM-01");
        final Map<String, Account> accounts = new LinkedHashMap<>();
        final Account account = new Account("P", "QA", AccountType.PERSONAL, null);
        final EconomyLedger ledger = new EconomyLedger();
        Setup() {
            accounts.put("P", account);
            ledger.recordMutation("INIT", account, LedgerEntryType.INITIAL_CAPITAL, cash(1_000_000_000), true, time(0, 9, 0), "Isolated QA");
        }
        void subscribe(String order, long amount, int day, int hour, int minute) throws Exception {
            fund.submitSubscription(order, account, cash(amount), ledger, time(day, hour, minute));
        }
        void cutoff(int day) throws Exception { manager.processCutoff(accounts, ledger, time(day, 16, 0)); }
        void reload() throws Exception {
            MutualFundManager restored = new MutualFundManager();
            restored.restoreState(manager.exportState()); manager = restored; fund = manager.getFund("FUND-MM-01");
        }
        void invariants() throws Exception {
            assertTrue(ledger.reconcile(account, Currency.IDR));
            assertEquals(account.getTotalCash(Currency.IDR), account.getAvailableCash(Currency.IDR).add(account.getReservedCash(Currency.IDR)));
            assertFalse(fund.getPoolCash().isNegative());
            assertTrue(fund.getAvailableUnits("P").signum() >= 0);
            assertEquals(0, fund.getTotalUnits().compareTo(fund.getUnits("P")));
            assertTrue(fund.getNavPerUnit().signum() > 0);
            JSONObject state = fund.exportState();
            BigDecimal backedAssets = fund.getPoolCash().getAmount().subtract(new BigDecimal(state.getString("feeRemainder")));
            BigDecimal implied = fund.getTotalUnits().multiply(fund.getNavPerUnit());
            assertTrue(backedAssets.subtract(implied).abs().compareTo(new BigDecimal("0.00001")) <= 0);
        }
    }

    @Test public void subscriptionReservesUntilCutoffAndCashMovesExactlyOnce() throws Exception {
        Setup s = new Setup();
        s.subscribe("SUB", 10_000_000, 0, 15, 0);
        assertEquals("ACCEPTED", s.fund.getOrder("SUB").getString("status"));
        assertEquals(cash(1_000_000_000), s.account.getTotalCash(Currency.IDR));
        assertEquals(cash(10_000_000), s.account.getReservedCash(Currency.IDR));
        assertEquals(0, s.fund.getUnits("P").signum());
        s.manager.processCutoff(s.accounts, s.ledger, time(0, 15, 59));
        assertEquals(0, s.fund.getUnits("P").signum());
        s.cutoff(0);
        assertEquals("SETTLED", s.fund.getOrder("SUB").getString("status"));
        assertEquals(cash(990_000_000), s.account.getTotalCash(Currency.IDR));
        assertEquals(cash(9_990_000), s.fund.getPoolCash());
        assertEquals("999.000000", s.fund.getUnits("P").toPlainString());
        assertEquals(cash(0), s.account.getReservedCash(Currency.IDR));
        s.invariants();
    }

    @Test public void cutoffWeekendAndCancellationBoundariesAreExplicit() throws Exception {
        Setup s = new Setup();
        s.subscribe("FRIDAY", 10_000, 4, 16, 0);
        assertEquals(time(7, 16, 0), s.fund.getOrder("FRIDAY").getLong("expectedSettlement"));
        s.cutoff(4); s.cutoff(5); s.cutoff(6);
        assertEquals("ACCEPTED", s.fund.getOrder("FRIDAY").getString("status"));
        s.fund.cancelOrder("FRIDAY", s.account, time(7, 15, 29));
        assertEquals(cash(0), s.account.getReservedCash(Currency.IDR));
        s.subscribe("CLOSE", 10_000, 7, 15, 0);
        try { s.fund.cancelOrder("CLOSE", s.account, time(7, 15, 30)); fail(); }
        catch (IllegalStateException expected) { }
        s.cutoff(7);
        assertEquals("SETTLED", s.fund.getOrder("CLOSE").getString("status"));
        s.invariants();
    }

    @Test public void pendingOrdersCancelAndReceiptsPersistWithoutDoubleReservation() throws Exception {
        Setup s = new Setup();
        s.subscribe("ONCE", 100_000, 0, 10, 0);
        String receipt = s.fund.getOrder("ONCE").toString();
        s.reload();
        s.subscribe("ONCE", 100_000, 0, 10, 1);
        assertEquals(receipt, s.fund.getOrder("ONCE").toString());
        assertEquals(cash(100_000), s.account.getReservedCash(Currency.IDR));
        try { s.subscribe("ONCE", 200_000, 0, 10, 2); fail(); }
        catch (IllegalArgumentException expected) { }
        s.fund.cancelOrder("ONCE", s.account, time(0, 11, 0));
        s.reload();
        s.fund.cancelOrder("ONCE", s.account, time(0, 15, 59));
        s.subscribe("ONCE", 100_000, 0, 12, 0);
        assertEquals("CANCELLED", s.fund.getOrder("ONCE").getString("status"));
        assertEquals(cash(0), s.account.getReservedCash(Currency.IDR));
        s.invariants();
    }

    @Test public void redemptionReservesUnitsAndRoundtripDoesNotDoubleCredit() throws Exception {
        Setup s = new Setup();
        s.subscribe("IN", 10_000_000, 0, 10, 0); s.cutoff(0);
        s.fund.submitRedemption("OUT", s.account, new BigDecimal("500"), s.ledger, time(1, 10, 0));
        assertEquals("499.000000", s.fund.getAvailableUnits("P").toPlainString());
        assertEquals("500.000000", s.fund.getReservedUnits("P").toPlainString());
        try { s.fund.submitRedemption("TOO-MUCH", s.account, new BigDecimal("500"), s.ledger, time(1, 10, 0)); fail(); }
        catch (IllegalStateException expected) { }
        try { s.fund.submitRedemption("DUST", s.account, new BigDecimal("0.0000001"), s.ledger, time(1, 10, 0)); fail(); }
        catch (IllegalArgumentException expected) { }
        Money before = s.account.getTotalCash(Currency.IDR);
        s.cutoff(1);
        JSONObject receipt = s.fund.getOrder("OUT");
        assertEquals(before.add(new Money(new BigDecimal(receipt.getString("netRaw")), Currency.IDR)), s.account.getTotalCash(Currency.IDR));
        assertEquals("499.000000", s.fund.getUnits("P").toPlainString());
        assertEquals(0, s.fund.getReservedUnits("P").signum());
        s.reload();
        int count = s.ledger.getEntries().size();
        s.fund.submitRedemption("OUT", s.account, new BigDecimal("500"), s.ledger, time(1, 17, 0));
        s.cutoff(1);
        assertEquals(count, s.ledger.getEntries().size());
        s.invariants();
    }

    @Test public void unavailableAccountKeepsReservedOrderPendingForNextCutoff() throws Exception {
        Setup s = new Setup(); s.subscribe("WAIT", 10_000, 0, 10, 0);
        s.accounts.clear(); s.cutoff(0);
        assertEquals("PENDING_ACCOUNT_UNAVAILABLE", s.fund.getOrder("WAIT").getString("status"));
        assertEquals(cash(10_000), s.account.getReservedCash(Currency.IDR));
        s.reload(); s.accounts.put("P", s.account); s.cutoff(1);
        assertEquals("SETTLED", s.fund.getOrder("WAIT").getString("status"));
        s.invariants();
    }

    @Test public void navIsCashBackedWithoutRandomYieldAndFeesNeverRepeatOnReplay() throws Exception {
        Setup s = new Setup(); s.subscribe("IN", 10_000_000, 0, 10, 0); s.cutoff(0);
        BigDecimal before = s.fund.getNavPerUnit();
        s.fund.advanceDay(1_000, s.ledger, 0L); assertEquals(before, s.fund.getNavPerUnit());
        s.cutoff(1);
        assertTrue(s.fund.getNavPerUnit().compareTo(before) < 0);
        String state = s.fund.exportState().toString();
        s.reload(); s.cutoff(1);
        assertEquals(state, s.fund.exportState().toString());
        s.invariants();
    }

    @Test public void tenSeedsHundredQueueCancelSettleReloadActionsMaintainInvariants() throws Exception {
        for (int seed = 0; seed < 10; seed++) {
            Setup s = new Setup(); Random random = new Random(seed);
            for (int day = 0; day < 100; day++) {
                String id = "S-" + seed + "-" + day;
                long amount = 10_000 + random.nextInt(100_000);
                s.subscribe(id, amount, day, 10, 0);
                s.subscribe(id, amount, day, 10, 1);
                if (random.nextBoolean()) s.fund.cancelOrder(id, s.account, time(day, 11, 0));
                else if (s.fund.getAvailableUnits("P").compareTo(BigDecimal.ONE) > 0) {
                    String red = "R-" + seed + "-" + day;
                    s.fund.submitRedemption(red, s.account, BigDecimal.ONE, s.ledger, time(day, 11, 0));
                    if (random.nextBoolean()) s.fund.cancelOrder(red, s.account, time(day, 12, 0));
                }
                s.invariants(); s.reload(); s.invariants();
                s.cutoff(day); s.invariants();
                String snapshot = s.fund.exportState().toString();
                s.cutoff(day); assertEquals(snapshot, s.fund.exportState().toString());
                s.reload(); assertEquals(snapshot, s.fund.exportState().toString());
                s.invariants();
            }
        }
    }
    private interface Corruption { void apply(JSONObject state) throws Exception; }
    private static void rejectsFundState(Setup setup, Corruption corrupt) throws Exception {
        String original = setup.fund.exportState().toString();
        JSONObject bad = new JSONObject(original);
        corrupt.apply(bad);
        try { setup.fund.restoreState(bad); fail("Corrupt fund state accepted: " + bad); }
        catch (IllegalArgumentException | org.json.JSONException expected) { }
        assertEquals("Rejected restore mutated original fund", original, setup.fund.exportState().toString());
    }

    @Test public void corruptModernOrderAndPoolMetadataRejectAtomically() throws Exception {
        Setup s = new Setup(); s.subscribe("QUEUE", 100_000, 0, 10, 0);
        rejectsFundState(s, state -> state.getJSONArray("orders").getJSONObject(0).put("side", "ARBITRARY"));
        rejectsFundState(s, state -> state.getJSONArray("orders").getJSONObject(0).put("status", "PENDING_ARBITRARY"));
        rejectsFundState(s, state -> state.getJSONArray("orders").getJSONObject(0).put("expectedSettlement", time(0, 16, 1)));
        rejectsFundState(s, state -> state.getJSONArray("orders").getJSONObject(0).put("cancellationDeadline", time(0, 15, 31)));
        rejectsFundState(s, state -> state.getJSONArray("orders").getJSONObject(0).put("requestTimestamp", time(0, 16, 1)));
        rejectsFundState(s, state -> state.put("managementFeesPaid", "-1"));
        rejectsFundState(s, state -> state.put("feeAccrualDay", "-1"));
        rejectsFundState(s, state -> state.put("lastCutoffTimestamp", time(0, 16, 1)));
        rejectsFundState(s, state -> state.put("feeRemainder", "-0.5"));
        rejectsFundState(s, state -> state.put("navPerUnit", "1"));
        rejectsFundState(s, state -> state.remove("stateSchemaVersion"));
        s.cutoff(0);
        rejectsFundState(s, state -> state.getJSONObject("holdings").put("P", "0"));
        s.fund.submitRedemption("REDEEM", s.account, BigDecimal.ONE, s.ledger, time(1, 10, 0));
        rejectsFundState(s, state -> state.getJSONArray("orders").getJSONObject(1).put("accountId", "ORPHAN"));
        rejectsFundState(s, state -> state.getJSONArray("orders").getJSONObject(1).put("units", "1000000"));
    }

    @Test public void onlyExplicitOriginalSchemaCanMigrateSponsorUnitGap() throws Exception {
        Setup s = new Setup();
        JSONObject legacy = new JSONObject().put("id", s.fund.getId()).put("name", s.fund.getName())
                .put("type", s.fund.getType().name()).put("managerName", s.fund.getManagerName())
                .put("navPerUnit", "12000").put("totalUnits", "100000.000000")
                .put("poolCash", "1000000000").put("holdings", new JSONObject());
        s.fund.restoreState(legacy);
        assertEquals(0, s.fund.getTotalUnits().compareTo(s.fund.getUnits("LEGACY-SPONSOR:" + s.fund.getId())));
        assertEquals(0, s.fund.getNavPerUnit().compareTo(MutualFund.INITIAL_NAV));
        s.manager.validateAccounts(s.accounts);
        s.reload();
        assertEquals(0, s.fund.getTotalUnits().compareTo(s.fund.getUnits("LEGACY-SPONSOR:" + s.fund.getId())));
        rejectsFundState(s, state -> state.getJSONObject("holdings").put("LEGACY-SPONSOR:" + s.fund.getId(), "99999.000000"));
        rejectsFundState(s, state -> state.put("legacySponsorMigration", false));
    }

    @Test public void catalogRestoreRejectsUnknownDuplicateAndMissingFundsWithoutChangingPriorState() throws Exception {
        Setup s = new Setup(); s.subscribe("QUEUE", 100_000, 0, 10, 0);
        String original = s.manager.exportState().toString();
        JSONArray duplicate = new JSONArray(original);
        duplicate.put(1, new JSONObject(duplicate.getJSONObject(0).toString()));
        try { s.manager.restoreState(duplicate); fail(); }
        catch (IllegalArgumentException expected) { }
        assertEquals(original, s.manager.exportState().toString());
        JSONArray unknown = new JSONArray(original);
        unknown.getJSONObject(1).put("id", "UNKNOWN");
        try { s.manager.restoreState(unknown); fail(); }
        catch (IllegalArgumentException expected) { }
        assertEquals(original, s.manager.exportState().toString());
        JSONArray missing = new JSONArray().put(new JSONArray(original).getJSONObject(0));
        try { s.manager.restoreState(missing); fail(); }
        catch (IllegalArgumentException expected) { }
        assertEquals(original, s.manager.exportState().toString());
    }

    @Test public void savedAccountGraphAndCrossFundCashReservationsValidateTogether() throws Exception {
        Setup s = new Setup(); s.subscribe("QUEUE", 100_000, 0, 10, 0);
        s.manager.getFund("FUND-FI-01").submitSubscription("OTHER", s.account, cash(200_000), s.ledger, time(0, 10, 0));
        assertEquals(cash(300_000), s.manager.getReservedSubscriptionCash("P"));
        s.manager.validateAccounts(s.accounts);
        s.account.releaseReservedCash(cash(1));
        try { s.manager.validateAccounts(s.accounts); fail(); }
        catch (IllegalArgumentException expected) { }
        s.account.reserveCash(cash(1));
        s.accounts.clear();
        try { s.manager.validateAccounts(s.accounts); fail(); }
        catch (IllegalArgumentException expected) { }
        s.accounts.put("P", s.account); s.cutoff(0);
        s.accounts.clear();
        try { s.manager.validateAccounts(s.accounts); fail(); }
        catch (IllegalArgumentException expected) { }
    }

}
