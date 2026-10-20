package com.tradingglobal3d.core;

import com.tradingglobal3d.core.loans.LoanContract;
import com.tradingglobal3d.core.loans.InstallmentLoanManager;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Random;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Test;
import static org.junit.Assert.*;

public class LoanScheduleRegressionTest {
    private static long time(int day) { return LocalDate.of(2026, 10, 20).plusDays(day).atTime(12, 0).toInstant(ZoneOffset.ofHours(7)).toEpochMilli(); }
    private static Money cash(long value) { return Money.of(value, Currency.IDR); }
    private static final class Setup {
        InstallmentLoanManager manager = new InstallmentLoanManager();
        final EconomyLedger ledger = new EconomyLedger();
        final Account account = new Account("P", "QA", AccountType.PERSONAL, null);
        LoanContract loan;
        Setup(int tenor, long principal) throws Exception {
            ledger.recordMutation("INIT", account, LedgerEntryType.INITIAL_CAPITAL, cash(100_000_000), true, time(0), "Isolated QA");
            String id = manager.disburseLoan("LOAN-PERS-01", account, cash(principal), tenor, ledger, time(0)).getString("loanId");
            loan = manager.getLoan(id);
        }
        void reload() throws Exception {
            manager.restoreState(new JSONArray(manager.exportState().toString())); loan = manager.getLoan(loan.getId());
        }
        void invariant() throws Exception {
            Money principal = cash(0), interest = cash(0), penalty = cash(0);
            JSONArray schedule = loan.getSchedule();
            for (int i = 0; i < schedule.length(); i++) {
                JSONObject row = schedule.getJSONObject(i);
                principal = principal.add(new Money(new BigDecimal(row.getString("principalDue")).subtract(new BigDecimal(row.getString("principalPaid"))), Currency.IDR));
                interest = interest.add(new Money(new BigDecimal(row.getString("interestDue")).subtract(new BigDecimal(row.getString("interestPaid"))), Currency.IDR));
                penalty = penalty.add(new Money(new BigDecimal(row.getString("penaltyDue")).subtract(new BigDecimal(row.getString("penaltyPaid"))), Currency.IDR));
            }
            assertEquals(principal, loan.getRemainingPrincipal()); assertEquals(interest, loan.getAccruedInterest()); assertEquals(penalty, loan.getAccruedPenalty());
            assertFalse(principal.isNegative()); assertFalse(interest.isNegative()); assertFalse(penalty.isNegative());
            assertTrue(ledger.reconcile(account, Currency.IDR));
        }
    }
    @Test public void allTenorsHaveDatedEqualPrincipalWithFinalRounding() throws Exception {
        for (int tenor : new int[]{7, 14, 30}) {
            Setup s = new Setup(tenor, 1_000_001);
            JSONArray rows = s.loan.getSchedule(); assertEquals(tenor, rows.length());
            long part = 1_000_001 / tenor, sum = 0;
            for (int index = 0; index < tenor; index++) {
                JSONObject row = rows.getJSONObject(index);
                long amount = Long.parseLong(row.getString("principalDue"));
                assertEquals(index == tenor - 1 ? 1_000_001 - part * (tenor - 1) : part, amount);
                assertEquals(LocalDate.of(2026, 10, 20).plusDays(index + 1).toString(), row.getString("dueDate")); sum += amount;
            }
            assertEquals(1_000_001, sum); s.invariant();
        }
    }
    @Test public void overduePenaltyExcludesFutureInstallmentsAndPaymentsAllocateOldest() throws Exception {
        Setup s = new Setup(7, 1_000_000);
        s.loan.processDailyAccrual(time(1));
        assertEquals(cash(500), s.loan.getAccruedInterest()); assertEquals(cash(0), s.loan.getAccruedPenalty());
        assertEquals(LoanContract.LoanStatus.ACTIVE, s.loan.getStatus());
        s.loan.processDailyAccrual(time(2));
        assertEquals(cash(71), s.loan.getAccruedPenalty()); assertEquals(cash(142_857), s.loan.getOverduePrincipal());
        assertEquals(LoanContract.LoanStatus.OVERDUE, s.loan.getStatus());
        s.loan.makePayment("PART", s.account, cash(72_071), s.ledger, time(2));
        assertEquals(cash(0), s.loan.getAccruedPenalty()); assertEquals(cash(0), s.loan.getAccruedInterest());
        assertEquals(cash(929_000), s.loan.getRemainingPrincipal());
        assertEquals("71000", s.loan.getSchedule().getJSONObject(0).getString("principalPaid"));
        s.invariant(); s.reload(); s.invariant();
    }
    @Test public void dailyPaymentsAmortizeAndEarlyPayoffChargesNoFutureInterest() throws Exception {
        for (int tenor : new int[]{7, 14, 30}) {
            Setup s = new Setup(tenor, 1_000_001);
            for (int day = 1; day <= tenor; day++) {
                s.loan.processDailyAccrual(time(day));
                JSONObject row = s.loan.getSchedule().getJSONObject(day - 1);
                Money due = new Money(new BigDecimal(row.getString("principalDue")), Currency.IDR).add(s.loan.getAccruedInterest());
                s.loan.makePayment("DAY-" + day, s.account, due, s.ledger, time(day));
                assertEquals(cash(0), s.loan.getAccruedPenalty()); s.invariant();
            }
            assertTrue(s.loan.isClosed()); assertEquals(cash(0), s.loan.getTotalPayoffAmount()); s.reload(); s.invariant();
        }
        Setup early = new Setup(7, 1_000_000);
        early.loan.prepayFull("EARLY", early.account, early.ledger, time(0));
        assertEquals(cash(0), early.loan.getAccruedInterest());
        early.loan.processDailyAccrual(time(100)); assertEquals(cash(0), early.loan.getTotalPayoffAmount());
        early.invariant();
    }
    @Test public void timestampAccrualIsReplaySafeAndRestructurePreservesAllLiabilities() throws Exception {
        Setup s = new Setup(7, 1_000_000);
        s.manager.processDailyAccrual(time(9)); Money before = s.loan.getTotalPayoffAmount();
        s.loan.restructure(14, time(9)); assertEquals(before, s.loan.getTotalPayoffAmount());
        assertEquals(2, s.loan.toJsonObject().getInt("scheduleVersion"));
        assertEquals(1, s.loan.toJsonObject().getJSONArray("restructureHistory").length());
        assertEquals(LoanContract.LoanStatus.ACTIVE, s.loan.getStatus()); s.invariant();
        String snapshot = s.loan.exportState().toString(); s.reload(); s.manager.processDailyAccrual(time(9));
        assertEquals(snapshot, s.loan.exportState().toString());
        assertEquals(15, s.loan.getSchedule().length());
        s.loan.makePayment("AFTER", s.account, cash(10_000), s.ledger, time(10)); s.invariant();
    }
    @Test public void lifetimePenaltyCapDoesNotResetWhenPenaltyIsPaid() throws Exception {
        Setup s = new Setup(7, 1_000_000); s.loan.processDailyAccrual(time(150));
        assertEquals(cash(50_000), s.loan.getAccruedPenalty());
        s.loan.makePayment("PENALTY", s.account, cash(50_000), s.ledger, time(150));
        s.loan.processDailyAccrual(time(200)); assertEquals(cash(0), s.loan.getAccruedPenalty()); s.invariant();
    }
    @Test public void oldSaveMigratesDebtAndPaymentReceiptsWithoutInventedCharges() throws Exception {
        Setup s = new Setup(7, 1_000_000); s.loan.processDailyAccrual(time(2));
        s.loan.makePayment("OLD", s.account, cash(10_000), s.ledger, time(2));
        JSONObject legacy = s.loan.exportState();
        for (String key : new String[]{"stateSchemaVersion", "scheduleVersion", "lastAccrualDay", "legacyScheduleMigration", "totalPenaltyAssessed", "schedule", "restructureHistory"}) legacy.remove(key);
        Money before = s.loan.getTotalPayoffAmount(); LoanContract migrated = LoanContract.restoreFromState(legacy);
        assertEquals(before, migrated.getTotalPayoffAmount()); assertTrue(migrated.exportState().getBoolean("legacyScheduleMigration"));
        int entries = s.ledger.getEntries().size(); migrated.makePayment("OLD", s.account, cash(10_000), s.ledger, time(2));
        assertEquals(entries, s.ledger.getEntries().size());
        assertEquals(before, LoanContract.restoreFromState(migrated.exportState()).getTotalPayoffAmount());
    }
    @Test public void productBoundaryRejectsUnknownAndWrongAccountAndBothProductsSupportAllTenors() throws Exception {
        for (int tenor : new int[]{7, 14, 30}) {
            Setup s = new Setup(tenor, 1_000_000);
            Account corporate = new Account("C", "Company", AccountType.COMPANY, null);
            s.manager.disburseLoan("LOAN-CORP-01", corporate, cash(1_000_000), tenor, s.ledger, time(0));
            for (String product : new String[]{"UNKNOWN", "LOAN-CORP-01"}) {
                int count = s.manager.getAllLoans().size(); Money before = s.account.getTotalCash(Currency.IDR);
                try { s.manager.disburseLoan(product, s.account, cash(1_000_000), tenor, s.ledger, time(0)); fail(); }
                catch (IllegalArgumentException expected) { }
                assertEquals(count, s.manager.getAllLoans().size()); assertEquals(before, s.account.getTotalCash(Currency.IDR));
            }
        }
    }
    @Test public void tenSeedHundredActionsPreserveScheduleLedgerAndReloadInvariants() throws Exception {
        for (int seed = 0; seed < 10; seed++) {
            Setup s = new Setup(new int[]{7, 14, 30}[seed % 3], 10_000_000); Random random = new Random(seed);
            for (int action = 0; action < 100; action++) {
                s.loan.processDailyAccrual(time(action));
                if (action % 19 == 0) s.loan.restructure(7, time(action));
                String id = seed + "-" + action;
                Money payment = cash(1000 + random.nextInt(10000));
                JSONObject receipt = s.loan.makePayment(id, s.account, payment, s.ledger, time(action));
                assertEquals(receipt.toString(), s.loan.makePayment(id, s.account, payment, s.ledger, time(action)).toString());
                s.invariant(); s.reload(); s.invariant();
                String before = s.loan.exportState().toString(); s.loan.processDailyAccrual(time(action));
                assertEquals(before, s.loan.exportState().toString());
            }
        }
    }
}
