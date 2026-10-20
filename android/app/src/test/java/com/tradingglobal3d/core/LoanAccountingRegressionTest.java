package com.tradingglobal3d.core;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.loans.InstallmentLoanManager;
import com.tradingglobal3d.core.loans.LoanContract;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import static org.junit.Assert.*;

public class LoanAccountingRegressionTest {
    private Account account(EconomyLedger ledger, long cash) {
        Account account = new Account("PERSONAL", "Player", AccountType.PERSONAL, null);
        ledger.recordMutation("INITIAL", account, LedgerEntryType.INITIAL_CAPITAL, Money.of(cash, Currency.IDR), true, 1, "Test capital");
        return account;
    }

    private LoanContract borrow(InstallmentLoanManager manager, Account account, EconomyLedger ledger, long principal) throws Exception {
        String id = manager.disburseLoan("LOAN-PERS-01", account, Money.of(principal, Currency.IDR), 7, ledger, 1).getString("loanId");
        return manager.getLoan(id);
    }

    @Test public void cashMutatesOnceAndPaymentReceiptsSurviveRestore() throws Exception {
        EconomyLedger ledger = new EconomyLedger();
        Account account = account(ledger, 50_000_000);
        InstallmentLoanManager manager = new InstallmentLoanManager();
        LoanContract loan = borrow(manager, account, ledger, 1_000_000);
        assertEquals(Money.of(50_995_000, Currency.IDR), account.getTotalCash(Currency.IDR));
        JSONObject receipt = loan.makePayment("PAY-1", account, Money.of(100_000, Currency.IDR), ledger, 10);
        String receiptSnapshot = receipt.toString();
        receipt.put("paymentAmount", "tampered");
        assertEquals(receiptSnapshot, loan.makePayment("PAY-1", account, Money.of(100_000, Currency.IDR), ledger, 10).toString());
        receipt = new JSONObject(receiptSnapshot);
        assertEquals(Money.of(50_895_000, Currency.IDR), account.getTotalCash(Currency.IDR));
        loan.makePayment("PAY-2", account, Money.of(100_000, Currency.IDR), ledger, 10);
        assertEquals(Money.of(800_000, Currency.IDR), loan.getRemainingPrincipal());
        manager.restoreState(new JSONArray(manager.exportState().toString()));
        loan = manager.getLoan(loan.getId());
        assertEquals(receipt.toString(), loan.makePayment("PAY-1", account, Money.of(100_000, Currency.IDR), ledger, 10).toString());
        try { loan.makePayment("PAY-1", account, Money.of(1, Currency.IDR), ledger, 10); fail(); }
        catch (IllegalArgumentException expected) { }
        loan.makePayment(account, Money.of(100_000, Currency.IDR), ledger, 10);
        loan.makePayment(account, Money.of(100_000, Currency.IDR), ledger, 10);
        assertEquals(Money.of(600_000, Currency.IDR), loan.getRemainingPrincipal());
        assertEquals(Money.of(50_595_000, Currency.IDR), account.getTotalCash(Currency.IDR));
        loan.prepayFull("FINAL", account, ledger, 10);
        int entries = ledger.getEntries().size();
        loan.prepayFull("FINAL", account, ledger, 10);
        assertEquals(entries, ledger.getEntries().size());
        assertEquals(LoanContract.LoanStatus.PAID_OFF, loan.getStatus());
        assertTrue(ledger.reconcile(account, Currency.IDR));
    }

    @Test public void invalidPaymentsCannotChangeCashOrDebtAndRestructureRetainsAccruals() throws Exception {
        EconomyLedger ledger = new EconomyLedger();
        Account account = account(ledger, 50_000_000);
        InstallmentLoanManager manager = new InstallmentLoanManager();
        LoanContract loan = borrow(manager, account, ledger, 1_000_000);
        String before = loan.exportState().toString();
        Money cash = account.getTotalCash(Currency.IDR);
        Account stranger = new Account("OTHER", "Other", AccountType.PERSONAL, null);
        for (Money invalid : new Money[]{Money.zero(Currency.IDR), Money.of(-1, Currency.IDR), Money.of(1, Currency.USD)}) {
            try { loan.makePayment("INVALID", account, invalid, ledger, 1); fail(); }
            catch (IllegalArgumentException expected) { }
        }
        try { loan.makePayment("OTHER", stranger, Money.of(1, Currency.IDR), ledger, 1); fail(); }
        catch (IllegalArgumentException expected) { }
        assertEquals(before, loan.exportState().toString());
        assertEquals(cash, account.getTotalCash(Currency.IDR));
        for (int day = 0; day < 9; day++) manager.processDailyAccrual();
        Money due = loan.getTotalPayoffAmount();
        assertTrue(loan.getAccruedPenalty().isPositive());
        for (int days : new int[]{0, -1}) {
            try { loan.restructure(days, 2); fail(); }
            catch (IllegalArgumentException expected) { }
        }
        loan.restructure(7, 2);
        assertEquals(due, loan.getTotalPayoffAmount());
        assertEquals(due, manager.getTotalOutstandingLiability(account.getId()));
        JSONArray corrupt = new JSONArray(manager.exportState().toString());
        corrupt.getJSONObject(0).put("remainingPrincipal", "bad");
        try { manager.restoreState(corrupt); fail(); }
        catch (IllegalArgumentException expected) { }
        assertSame(loan, manager.getLoan(loan.getId()));
        assertTrue(ledger.reconcile(account, Currency.IDR));
    }

    @Test public void tenSeedsOfOneHundredMeaningfulActionsReconcileAtEveryStep() throws Exception {
        for (int seed = 0; seed < 10; seed++) {
            Random random = new Random(seed);
            EconomyLedger ledger = new EconomyLedger();
            Account account = account(ledger, 50_000_000);
            InstallmentLoanManager manager = new InstallmentLoanManager();
            LoanContract loan = borrow(manager, account, ledger, 10_000_000);
            BigDecimal principal = new BigDecimal("10000000");
            BigDecimal interest = BigDecimal.ZERO;
            BigDecimal penalty = BigDecimal.ZERO;
            BigDecimal cash = new BigDecimal("59950000");
            int days = 7, elapsedDays = 0;
            BigDecimal assessedPenalty = BigDecimal.ZERO;
            BigDecimal[] installments = new BigDecimal[7];
            java.util.Arrays.fill(installments, new BigDecimal("1428571"));
            installments[6] = new BigDecimal("1428574");
            for (int action = 0; action < 100; action++) {
                if (random.nextInt(4) == 0) {
                    manager.processDailyAccrual();
                    BigDecimal daily = principal.multiply(new BigDecimal("0.0005")).setScale(0, RoundingMode.HALF_UP);
                    interest = interest.add(daily);
                    elapsedDays++;
                    days = Math.max(0, days - 1);
                    BigDecimal overdue = BigDecimal.ZERO;
                    for (int row = 0; row < installments.length; row++)
                        if (row + 1 < elapsedDays) overdue = overdue.add(installments[row]);
                    BigDecimal charged = overdue.multiply(new BigDecimal("0.0005")).setScale(0, RoundingMode.HALF_UP)
                            .min(new BigDecimal("500000").subtract(assessedPenalty));
                    assessedPenalty = assessedPenalty.add(charged);
                    penalty = penalty.add(charged);
                } else {
                    BigDecimal payment = BigDecimal.valueOf(1000 + random.nextInt(50000));
                    Money amount = new Money(payment, Currency.IDR);
                    String request = seed + "-" + action;
                    JSONObject receipt = loan.makePayment(request, account, amount, ledger, 2);
                    BigDecimal paidPenalty = payment.min(penalty);
                    penalty = penalty.subtract(paidPenalty);
                    BigDecimal remaining = payment.subtract(paidPenalty);
                    BigDecimal paidInterest = remaining.min(interest);
                    interest = interest.subtract(paidInterest);
                    BigDecimal principalPayment = remaining.subtract(paidInterest);
                    principal = principal.subtract(principalPayment);
                    for (int row = 0; row < installments.length && principalPayment.signum() > 0; row++) {
                        BigDecimal allocated = principalPayment.min(installments[row]);
                        installments[row] = installments[row].subtract(allocated);
                        principalPayment = principalPayment.subtract(allocated);
                    }
                    cash = cash.subtract(payment);
                    assertEquals(receipt.toString(), loan.makePayment(request, account, amount, ledger, 2).toString());
                }
                if (action % 7 == 0) {
                    manager.restoreState(new JSONArray(manager.exportState().toString()));
                    loan = manager.getLoan(loan.getId());
                }
                assertEquals(new Money(cash, Currency.IDR), account.getTotalCash(Currency.IDR));
                assertEquals(new Money(principal, Currency.IDR), loan.getRemainingPrincipal());
                assertEquals(new Money(principal.add(interest).add(penalty), Currency.IDR), manager.getTotalOutstandingLiability(account.getId()));
                assertEquals(days, loan.getRemainingDays());
                assertTrue("seed=" + seed + " action=" + action, ledger.reconcile(account, Currency.IDR));
            }
        }
    }

    @Test public void legacyRestructuringRetainsDebtAndMalformedClosedContractsAreRejected() throws Exception {
        EconomyLedger ledger = new EconomyLedger();
        Account account = account(ledger, 50_000_000);
        InstallmentLoanManager manager = new InstallmentLoanManager();
        LoanContract loan = borrow(manager, account, ledger, 1_000_000);
        loan.processDailyAccrual();
        Money debt = loan.getTotalPayoffAmount();
        JSONArray legacy = new JSONArray(manager.exportState().toString());
        legacy.getJSONObject(0).put("status", "RESTRUCTURED");
        manager.restoreState(legacy);
        loan = manager.getLoan(loan.getId());
        assertEquals(LoanContract.LoanStatus.ACTIVE, loan.getStatus());
        assertFalse(loan.isClosed());
        assertEquals(debt, manager.getTotalOutstandingLiability(account.getId()));
        for (String component : new String[]{"remainingPrincipal", "accruedInterest", "accruedPenalty"}) {
            JSONObject malformed = loan.exportState();
            malformed.put("status", "PAID_OFF").put("remainingPrincipal", "0")
                    .put("accruedInterest", "0").put("accruedPenalty", "0").put(component, "1");
            try { manager.restoreState(new JSONArray().put(malformed)); fail("Closed loan with " + component + " accepted"); }
            catch (IllegalArgumentException expected) { }
            assertSame(loan, manager.getLoan(loan.getId()));
            assertEquals(debt, manager.getTotalOutstandingLiability(account.getId()));
        }
        loan.makePayment("AFTER-MIGRATION", account, Money.of(10_000, Currency.IDR), ledger, 2);
        assertTrue(ledger.reconcile(account, Currency.IDR));
    }
}
