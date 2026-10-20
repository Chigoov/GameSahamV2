package com.tradingglobal3d.core;

import com.tradingglobal3d.core.business.CompanyManager;
import com.tradingglobal3d.core.business.Employee;
import com.tradingglobal3d.core.business.OperatingCompany;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class CompanyStaffRegressionTest {
    private final CompanyManager manager = new CompanyManager();
    private final EconomyLedger ledger = new EconomyLedger();
    private final Map<String, Account> accounts = new HashMap<>();

    private Account company(String sector) {
        Account personal = new Account("PERSONAL", "Player", AccountType.PERSONAL, null);
        accounts.put(personal.getId(), personal);
        ledger.recordMutation("INITIAL", personal, LedgerEntryType.INITIAL_CAPITAL, Money.of(1_000_000_000L, Currency.IDR), true, 1, "Test");
        OperatingCompany company = manager.foundCompany("CO", "Company", sector, personal, Money.of(500_000_000, Currency.IDR), ledger, accounts, 1);
        return accounts.get(company.getAccountId());
    }

    private JSONObject execute(String request, String action, String employee, String role, Account account) throws Exception {
        return manager.executeStaffAction(request, "CO", action, employee, role, account, ledger, 10);
    }

    @Test public void staffingJourneyHasNonMutatingQuotesCostsAndPersistentReceipts() throws Exception {
        Account account = company("Jasa");
        String candidate = "CAND-CO-OPERATIONS_MANAGER";
        String state = manager.exportState().toString();
        int entries = ledger.getEntries().size();
        JSONObject quote = manager.previewStaffAction("CO", "HIRE", candidate, "OPERATIONS_MANAGER");
        assertEquals(state, manager.exportState().toString());
        assertEquals(entries, ledger.getEntries().size());
        assertEquals("3000000", quote.getString("cost"));
        assertEquals("6500000", quote.getString("dailyPayrollAfter"));
        assertEquals("NEXT_DAILY_SETTLEMENT", quote.getString("effectiveRule"));
        JSONObject receipt = execute("HIRE", "HIRE", candidate, "OPERATIONS_MANAGER", account);
        String expectedReceipt = receipt.toString();
        receipt.put("cost", "tampered");
        assertEquals(expectedReceipt, execute("HIRE", "HIRE", candidate, "OPERATIONS_MANAGER", account).toString());
        assertEquals(Money.of(497_000_000, Currency.IDR), account.getTotalCash(Currency.IDR));
        manager.restoreState(new JSONArray(manager.exportState().toString()));
        assertEquals(expectedReceipt, execute("HIRE", "HIRE", candidate, "OPERATIONS_MANAGER", account).toString());
        try { execute("HIRE", "TRAIN", candidate, null, account); fail(); }
        catch (IllegalArgumentException expected) { }
        execute("TRAIN", "TRAIN", candidate, null, account);
        assertEquals(2, manager.getCompany("CO").getEmployee(candidate).getLevel());
        execute("ASSIGN", "ASSIGN", candidate, "SALES_MANAGER", account);
        assertEquals(Employee.Role.SALES_MANAGER, manager.getCompany("CO").getEmployee(candidate).getRole());
        JSONObject withArrears = manager.exportState().getJSONObject(0).put("payrollArrears", "1000");
        manager.restoreState(new JSONArray().put(withArrears));
        execute("DISMISS", "DISMISS", candidate, null, account);
        assertNull(manager.getCompany("CO").getEmployee(candidate));
        assertEquals(Money.of(1000, Currency.IDR), manager.getCompany("CO").getPayrollArrears());
        assertEquals(Money.of(479_000_000, Currency.IDR), account.getTotalCash(Currency.IDR));
        assertTrue(ledger.reconcile(account, Currency.IDR));
    }

    @Test public void invalidAndUnaffordableActionsCannotMutateStaffCashOrLedger() throws Exception {
        Account account = company("Ritel");
        String ceo = "EMP-CO-CEO";
        for (String action : new String[]{"DISMISS", "ASSIGN"}) {
            try { execute("INVALID-" + action, action, ceo, "HR_DIRECTOR", account); fail(); }
            catch (IllegalStateException expected) { }
        }
        Account other = new Account("OTHER", "Other", AccountType.COMPANY, null);
        try { execute("OTHER", "TRAIN", ceo, null, other); fail(); }
        catch (IllegalArgumentException expected) { }
        String candidate = "CAND-CO-OPERATIONS_MANAGER";
        execute("HIRE", "HIRE", candidate, "OPERATIONS_MANAGER", account);
        try { execute("DUPLICATE", "ASSIGN", candidate, "CEO", account); fail(); }
        catch (IllegalStateException expected) { }
        ledger.recordMutation("DRAIN", account, LedgerEntryType.WITHDRAWAL,
                account.getAvailableCash(Currency.IDR), false, 11, "Isolate insufficient cash");
        String state = manager.exportState().toString();
        int entries = ledger.getEntries().size();
        try { execute("BROKE", "TRAIN", candidate, null, account); fail(); }
        catch (IllegalStateException expected) { }
        assertEquals(state, manager.exportState().toString());
        assertEquals(entries, ledger.getEntries().size());
        assertEquals(Money.zero(Currency.IDR), account.getTotalCash(Currency.IDR));
        assertTrue(ledger.reconcile(account, Currency.IDR));
    }

    @Test public void candidatesAreStableSectorCompatibleAndTrainingStopsAtFive() throws Exception {
        Account account = company("Manufaktur");
        String first = manager.getStaff("CO").getJSONArray("candidates").toString();
        assertEquals(first, manager.getStaff("CO").getJSONArray("candidates").toString());
        assertEquals(5, manager.getStaff("CO").getJSONArray("candidates").length());
        String candidate = "CAND-CO-PRODUCTION_MANAGER";
        execute("HIRE", "HIRE", candidate, "PRODUCTION_MANAGER", account);
        for (int level = 2; level <= 5; level++) execute("TRAIN-" + level, "TRAIN", candidate, null, account);
        assertEquals(5, manager.getCompany("CO").getEmployee(candidate).getLevel());
        String before = manager.exportState().toString();
        try { execute("MAX", "TRAIN", candidate, null, account); fail(); }
        catch (IllegalStateException expected) { }
        assertEquals(before, manager.exportState().toString());
        assertTrue(new BigDecimal(manager.getStaff("CO").getString("dailyPayroll")).signum() > 0);
        assertTrue(ledger.reconcile(account, Currency.IDR));
    }

    @Test public void reassignedEmployeeKeepsIdentityAndVacancyGetsUnusedCandidate() throws Exception {
        Account account = company("Jasa");
        String original = "CAND-CO-OPERATIONS_MANAGER";
        execute("HIRE-ORIGINAL", "HIRE", original, "OPERATIONS_MANAGER", account);
        execute("ASSIGN-SALES", "ASSIGN", original, "SALES_MANAGER", account);
        JSONArray candidates = manager.getStaff("CO").getJSONArray("candidates");
        String replacement = null;
        for (int i = 0; i < candidates.length(); i++) {
            JSONObject candidate = candidates.getJSONObject(i);
            if (candidate.getString("role").equals("OPERATIONS_MANAGER")) {
                assertTrue(candidate.getBoolean("available"));
                replacement = candidate.getString("id");
            }
        }
        assertEquals(original + "-2", replacement);
        execute("REFILL", "HIRE", replacement, "OPERATIONS_MANAGER", account);
        assertEquals(Employee.Role.SALES_MANAGER, manager.getCompany("CO").getEmployee(original).getRole());
        assertEquals(Employee.Role.OPERATIONS_MANAGER, manager.getCompany("CO").getEmployee(replacement).getRole());
        assertEquals(3, manager.getCompany("CO").getEmployees().size());
        manager.restoreState(new JSONArray(manager.exportState().toString()));
        assertEquals(3, manager.getCompany("CO").getEmployees().size());
        assertTrue(ledger.reconcile(account, Currency.IDR));
    }

    @Test public void staleStaffOrPolicyQuoteRejectsBeforeMutationWhileReceiptRetrySucceeds() throws Exception {
        Account account = company("Jasa");
        String ceo = "EMP-CO-CEO";
        JSONObject quote = manager.previewStaffAction("CO", "TRAIN", ceo, null);
        long reviewedVersion = quote.getLong("staffVersion");
        JSONObject receipt = manager.executeStaffAction("TRAIN-ONE", "CO", "TRAIN", ceo, null,
                account, ledger, 10, reviewedVersion);
        String state = manager.exportState().toString();
        Money cash = account.getTotalCash(Currency.IDR);
        int entries = ledger.getEntries().size();
        assertEquals(receipt.toString(), manager.executeStaffAction("TRAIN-ONE", "CO", "TRAIN", ceo, null,
                account, ledger, 10, reviewedVersion).toString());
        try {
            manager.executeStaffAction("STALE", "CO", "TRAIN", ceo, null, account, ledger, 10, reviewedVersion);
            fail("Stale training quote accepted");
        } catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("STALE")); }
        assertEquals(state, manager.exportState().toString());
        assertEquals(cash, account.getTotalCash(Currency.IDR));
        assertEquals(entries, ledger.getEntries().size());
        long nextVersion = manager.previewStaffAction("CO", "TRAIN", ceo, null).getLong("staffVersion");
        manager.getCompany("CO").setWorkPolicy(6, OperatingCompany.BenefitTier.PLUS);
        long changed = manager.getCompany("CO").getStaffVersion();
        assertTrue(changed > nextVersion);
        manager.restoreState(new JSONArray(manager.exportState().toString()));
        assertEquals(changed, manager.getStaff("CO").getLong("staffVersion"));
        try {
            manager.executeStaffAction("STALE-POLICY", "CO", "TRAIN", ceo, null, account, ledger, 10, nextVersion);
            fail("Policy changed but old quote accepted");
        } catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("STALE")); }
        assertEquals(cash, account.getTotalCash(Currency.IDR));
        assertEquals(entries, ledger.getEntries().size());
        assertTrue(ledger.reconcile(account, Currency.IDR));
    }
}
