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
import org.json.JSONObject;
import org.junit.Test;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import static org.junit.Assert.*;

public class CompanyOperationsRegressionTest {
    private static long time(int day) {
        return LocalDate.of(2026, 10, 20).plusDays(day).atTime(16, 1)
                .toInstant(ZoneOffset.ofHours(7)).toEpochMilli();
    }

    private static Money rupiah(long amount) { return Money.of(amount, Currency.IDR); }

    private static final class Setup {
        CompanyManager manager = new CompanyManager();
        final EconomyLedger ledger = new EconomyLedger();
        final Map<String, Account> accounts = new LinkedHashMap<>();
        final Account personal = new Account("P", "Pribadi", AccountType.PERSONAL, null);
        OperatingCompany company;
        final Account account;
        Setup(String sector) {
            accounts.put(personal.getId(), personal);
            ledger.recordMutation("INITIAL", personal, LedgerEntryType.INITIAL_CAPITAL,
                    rupiah(1_000_000_000), true, time(-1), "Isolated QA");
            company = manager.foundCompany("C", "Perusahaan QA", sector, personal,
                    rupiah(25_000_000), ledger, accounts, time(-1));
            account = accounts.get(company.getAccountId());
        }
        void run(int day) { manager.processDailyOperations(account, company, ledger, time(day)); }
        void reload() throws Exception {
            CompanyManager loaded = new CompanyManager();
            loaded.restoreState(manager.exportState());
            manager = loaded;
            company = manager.getCompany("C");
        }
    }

    @Test public void sectorsHaveDistinctCostsDemandAndCapacity() {
        Setup jasa = new Setup("Jasa"), retail = new Setup("Ritel"), plant = new Setup("Manufaktur");
        assertEquals(rupiah(1_000_000), jasa.company.calculateDailyOperatingCost());
        assertEquals(rupiah(5_000_000), retail.company.calculateDailyOperatingCost());
        assertEquals(rupiah(15_000_000), plant.company.calculateDailyOperatingCost());
        for (int day = 0; day < 30; day++) {
            assertTrue(jasa.company.calculateDailyRevenue(day).isLessThan(retail.company.calculateDailyRevenue(day)));
            assertTrue(jasa.company.calculateDailyRevenue(day).compareTo(jasa.company.calculateDailyCapacity()) <= 0);
            assertTrue(plant.company.getDemandFactor(day).doubleValue() >= .70);
            assertTrue(plant.company.getDemandFactor(day).doubleValue() <= 1.40);
        }
        Money underStaffed = plant.company.calculateDailyCapacity();
        plant.company.addEmployee(new Employee("OPS", "Operasi", Employee.Role.OPERATIONS_MANAGER, 1));
        plant.company.addEmployee(new Employee("PROD", "Produksi", Employee.Role.PRODUCTION_MANAGER, 1));
        assertTrue(plant.company.calculateDailyCapacity().compareTo(underStaffed) > 0);
    }

    @Test public void dailyCashflowIncludesRealOperatingExpenses() {
        Setup state = new Setup("Jasa");
        long day = LocalDate.of(2026, 10, 20).toEpochDay();
        Money cash = state.account.getTotalCash(Currency.IDR);
        Money expected = cash.subtract(state.company.calculateDailyPayroll())
                .subtract(state.company.calculateDailyOperatingCost()).add(state.company.calculateDailyRevenue(day));
        state.run(0);
        assertEquals(expected, state.account.getTotalCash(Currency.IDR));
        assertTrue(state.ledger.reconcile(state.account, Currency.IDR));
    }

    @Test public void unpaidPayrollSurvivesDismissalReloadAndCapitalInjectionPaysItFirst() throws Exception {
        Setup state = new Setup("Jasa");
        state.manager.transferCapital("C", false, state.personal, state.account, rupiah(23_000_000), state.ledger, time(-1));
        int morale = state.company.getMorale();
        state.run(0);
        assertEquals(rupiah(3_000_000), state.company.getPayrollArrears());
        assertEquals(rupiah(0), state.account.getTotalCash(Currency.IDR));
        assertEquals(morale - 20, state.company.getMorale());
        state.company.removeEmployee("EMP-C-CEO");
        state.reload();
        assertEquals(rupiah(3_000_000), state.company.getPayrollArrears());
        assertEquals(morale - 20, state.company.getMorale());
        state.manager.transferCapital("C", true, state.personal, state.account, rupiah(4_000_000), state.ledger, time(0));
        assertEquals(rupiah(0), state.company.getPayrollArrears());
        assertEquals(rupiah(1_000_000), state.account.getTotalCash(Currency.IDR));
        assertEquals(morale - 20, state.company.getMorale());
        assertTrue(state.ledger.reconcile(state.personal, Currency.IDR));
        assertTrue(state.ledger.reconcile(state.account, Currency.IDR));
    }

    @Test public void partialOverheadFundingProducesOnlyPaidFraction() {
        Setup state = new Setup("Jasa");
        state.manager.transferCapital("C", false, state.personal, state.account, rupiah(19_500_000), state.ledger, time(-1));
        Money revenue = state.company.calculateDailyRevenue(LocalDate.of(2026, 10, 20).toEpochDay()).multiply(.5);
        state.run(0);
        assertEquals(revenue, state.account.getTotalCash(Currency.IDR));
        assertTrue(state.ledger.reconcile(state.account, Currency.IDR));
    }

    @Test public void policyTogglingAndStaffChangesCannotFarmMoraleAndLoadPreservesIt() throws Exception {
        Setup state = new Setup("Jasa");
        int initialMorale = state.company.getMorale();
        for (int i = 0; i < 100; i++) {
            state.company.setWorkPolicy(6, OperatingCompany.BenefitTier.PREMIUM);
            state.company.setWorkPolicy(10, OperatingCompany.BenefitTier.STANDARD);
        }
        assertEquals(initialMorale, state.company.getMorale());
        state.company.setWorkPolicy(6, OperatingCompany.BenefitTier.PREMIUM);
        assertEquals(rupiah(7_500_000), state.company.calculateDailyPayroll());
        state.run(0);
        assertEquals(initialMorale + 5, state.company.getMorale());
        state.reload();
        assertEquals(initialMorale + 5, state.company.getMorale());
        try { state.company.setWorkPolicy(7, OperatingCompany.BenefitTier.STANDARD); fail(); }
        catch (IllegalArgumentException expected) { }
        assertEquals(6, state.company.getWorkHours());
        try { state.company.setWorkPolicy(10, null); fail(); }
        catch (NullPointerException expected) { }
        assertEquals(6, state.company.getWorkHours());
    }

    @Test public void legacyCompanyMigrationRetainsMoraleAndDefaultsNewState() throws Exception {
        Setup state = new Setup("Teknologi Finansial");
        JSONObject old = state.company.toJsonObject();
        old.remove("payrollArrears");
        old.remove("lastOperationsDay");
        old.put("morale", 32);
        OperatingCompany restored = OperatingCompany.fromJsonObject(old);
        assertEquals(32, restored.getMorale());
        assertEquals(rupiah(0), restored.getPayrollArrears());
        assertEquals("JASA", restored.getSectorModel());
    }

    @Test public void tenSeededHundredActionSequencesKeepDailyReplayAndMoneyInvariants() throws Exception {
        for (int seed = 0; seed < 10; seed++) {
            Setup state = new Setup(new String[]{"Jasa", "Ritel", "Manufaktur"}[seed % 3]);
            Random random = new Random(seed);
            for (int step = 0; step < 100; step++) {
                state.company.setWorkPolicy(new int[]{6, 8, 10}[random.nextInt(3)],
                        OperatingCompany.BenefitTier.values()[random.nextInt(3)]);
                if (state.account.getAvailableCash(Currency.IDR).isLessThan(rupiah(25_000_000)))
                    state.manager.transferCapital("C", true, state.personal, state.account, rupiah(25_000_000), state.ledger, time(step));
                state.run(step);
                String snapshot = state.company.toJsonObject().toString();
                Money balance = state.account.getTotalCash(Currency.IDR);
                int entries = state.ledger.getEntries().size();
                state.run(step);
                state.manager.processDailyOperations(state.account, state.company, state.ledger, time(step) + 1000);
                assertEquals(snapshot, state.company.toJsonObject().toString());
                assertEquals(balance, state.account.getTotalCash(Currency.IDR));
                assertEquals(entries, state.ledger.getEntries().size());
                state.reload();
                state.run(step);
                assertEquals(snapshot, state.company.toJsonObject().toString());
                assertEquals(balance, state.account.getTotalCash(Currency.IDR));
                assertTrue("seed=" + seed + " step=" + step, state.ledger.reconcile(state.account, Currency.IDR));
                assertTrue(state.ledger.reconcile(state.personal, Currency.IDR));
                assertFalse(state.company.getPayrollArrears().isNegative());
                assertTrue(state.company.getMorale() >= 0 && state.company.getMorale() <= 100);
            }
        }
    }
}
