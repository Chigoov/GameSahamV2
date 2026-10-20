package com.tradingglobal3d.core;

import com.tradingglobal3d.core.collection.CollectionManager;
import com.tradingglobal3d.core.collection.CollectibleItem;
import com.tradingglobal3d.core.loans.InstallmentLoanManager;
import com.tradingglobal3d.core.loans.LoanContract;
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

public class LoanCollateralRegressionTest {
    private static Money cash(long amount) { return Money.of(amount, Currency.IDR); }
    private static long time(int day) { return LocalDate.of(2026, 10, 20).plusDays(day).atTime(12, 0).toInstant(ZoneOffset.ofHours(7)).toEpochMilli(); }
    private static final class Setup {
        CollectionManager collections = new CollectionManager();
        InstallmentLoanManager loans = new InstallmentLoanManager();
        final EconomyLedger ledger = new EconomyLedger();
        final Account account = new Account("P", "QA", AccountType.PERSONAL, null);
        CollectibleItem item;
        Setup() throws Exception {
            loans.bindCollectionManager(collections);
            ledger.recordMutation("INIT", account, LedgerEntryType.INITIAL_CAPITAL, cash(100_000_000), true, time(0), "Isolated QA");
            item = collections.buyItem("COL-ART-00", account, ledger, null, time(0));
        }
        JSONObject quote(long principal) throws Exception { return loans.getLoanQuote("LOAN-PERS-01", account, cash(principal), 7, item, null); }
        JSONObject borrow(String request, long principal, String version) throws Exception {
            return loans.disburseReviewedLoan(request, "LOAN-PERS-01", account, cash(principal), 7, item, null, version, ledger, time(0));
        }
        void reload() throws Exception {
            CollectionManager restored = new CollectionManager(); restored.restoreState(new JSONArray(collections.exportState().toString()));
            InstallmentLoanManager loaded = new InstallmentLoanManager(); loaded.restoreState(new JSONArray(loans.exportState().toString()), time(0));
            loaded.bindCollectionManager(restored); collections = restored; loans = loaded; item = collections.getItem("COL-ART-00");
        }
    }
    @Test public void quoteIsPureAndHalfValueCapRejectsUnownedClonedAndOverLimitAssets() throws Exception {
        Setup s = new Setup(); String before = s.collections.exportState().toString();
        JSONObject quote = s.quote(7_500_000); assertTrue(quote.getBoolean("eligible")); assertEquals("7500000", quote.getString("maxPrincipalRaw"));
        assertEquals(before, s.collections.exportState().toString()); assertEquals(0, s.loans.getAllLoans().size());
        assertFalse(s.quote(7_500_001).getBoolean("eligible"));
        try { s.borrow("OVER", 7_500_001, s.quote(7_500_001).getString("quoteVersion")); fail(); }
        catch (IllegalStateException expected) { }
        CollectibleItem clone = CollectibleItem.fromJsonObject(s.item.toJsonObject());
        for (CollectibleItem invalid : new CollectibleItem[]{null, clone, s.collections.getItem("COL-CAR-00")}) {
            try { s.loans.getLoanQuote("LOAN-PERS-01", s.account, cash(1_000_000), 7, invalid, null); fail(); }
            catch (IllegalArgumentException expected) { }
        }
        assertEquals(before, s.collections.exportState().toString());
    }
    @Test public void pledgeBlocksSellingAndDuplicateLoansUntilFullPayoffAndSurvivesReload() throws Exception {
        Setup s = new Setup(); String version = s.quote(5_000_000).getString("quoteVersion");
        JSONObject receipt = s.borrow("ONE", 5_000_000, version); String id = receipt.getString("loanId");
        Money balance = s.account.getTotalCash(Currency.IDR); assertEquals(id, s.item.getPledgedLoanId());
        assertEquals(receipt.toString(), s.borrow("ONE", 5_000_000, version).toString()); assertEquals(balance, s.account.getTotalCash(Currency.IDR));
        try { s.borrow("ONE", 6_000_000, version); fail(); } catch (IllegalArgumentException expected) { }
        try { s.collections.sellItem("SELL", s.item.getId(), s.account, s.ledger, null, time(0)); fail(); }
        catch (IllegalStateException expected) { }
        try { s.borrow("TWO", 5_000_000, version); fail(); } catch (IllegalArgumentException expected) { }
        s.reload(); assertEquals(id, s.item.getPledgedLoanId());
        assertEquals(receipt.toString(), s.borrow("ONE", 5_000_000, version).toString());
        LoanContract loan = s.loans.getLoan(id); loan.makePayment("PART", s.account, cash(1_000_000), s.ledger, time(0));
        assertTrue(s.item.isPledged()); loan.prepayFull("FINAL", s.account, s.ledger, time(0));
        assertFalse(s.item.isPledged()); assertTrue(loan.getTotalPayoffAmount().isZero());
        s.collections.sellItem("SELL-FINAL", s.item.getId(), s.account, s.ledger, null, time(0));
        s.loans.validateCollateral(s.collections); assertTrue(s.ledger.reconcile(s.account, Currency.IDR));
    }
    @Test public void reviewedQuoteRejectsChangedValuationWithoutPledgeOrCashMutation() throws Exception {
        Setup s = new Setup(); String version = s.quote(5_000_000).getString("quoteVersion");
        s.item.setValuation(cash(14_000_000)); Money before = s.account.getTotalCash(Currency.IDR);
        try { s.borrow("STALE", 5_000_000, version); fail(); } catch (IllegalStateException expected) { }
        assertFalse(s.item.isPledged()); assertEquals(before, s.account.getTotalCash(Currency.IDR)); assertEquals(0, s.loans.getAllLoans().size());
    }
    @Test public void companyCashflowCeilingDeductsExistingDebtAndNeverUsesPersonalCollateral() throws Exception {
        Setup s = new Setup(); Account company = new Account("C", "Company", AccountType.COMPANY, null);
        JSONObject quote = s.loans.getLoanQuote("LOAN-CORP-01", company, cash(2_000_000), 14, null, cash(3_000_000));
        s.loans.disburseReviewedLoan("CORP", "LOAN-CORP-01", company, cash(2_000_000), 14, null, cash(3_000_000), quote.getString("quoteVersion"), s.ledger, time(0));
        JSONObject over = s.loans.getLoanQuote("LOAN-CORP-01", company, cash(2_000_000), 14, null, cash(3_000_000));
        assertFalse(over.getBoolean("eligible")); assertEquals("1000000", over.getString("maxPrincipalRaw"));
        assertFalse(s.item.isPledged()); assertEquals(cash(1_990_000), company.getTotalCash(Currency.IDR));
        assertTrue(s.ledger.reconcile(company, Currency.IDR));
    }
    @Test public void mismatchedRegistryRejectsOrphanPledgeAndMissingPledge() throws Exception {
        Setup s = new Setup(); JSONObject receipt = s.borrow("ONE", 5_000_000, s.quote(5_000_000).getString("quoteVersion"));
        JSONArray saved = new JSONArray(s.collections.exportState().toString());
        for (int i = 0; i < saved.length(); i++) if (saved.getJSONObject(i).getString("id").equals(s.item.getId()))
            saved.getJSONObject(i).put("pledgedLoanId", "ORPHAN");
        CollectionManager corrupted = new CollectionManager(); corrupted.restoreState(saved);
        try { s.loans.validateCollateral(corrupted); fail(); } catch (IllegalArgumentException expected) { }
        LoanContract loan = s.loans.getLoan(receipt.getString("loanId"));
        loan.prepayFull("FINAL", s.account, s.ledger, time(0)); assertFalse(s.item.isPledged());
        s.loans.validateCollateral(s.collections);
    }
    @Test public void rejectedFuturePaymentAndPrepayDoNotChangeAccrualOrSchedule() throws Exception {
        Setup s = new Setup(); String id = s.borrow("ONE", 5_000_000, s.quote(5_000_000).getString("quoteVersion")).getString("loanId");
        LoanContract loan = s.loans.getLoan(id); Account other = new Account("OTHER", "Other", AccountType.PERSONAL, null);
        String before = loan.exportState().toString();
        try { loan.prepayFull("BAD", other, s.ledger, time(100)); fail(); } catch (IllegalArgumentException expected) { }
        assertEquals(before, loan.exportState().toString());
        s.ledger.recordMutation("DRAIN", s.account, LedgerEntryType.WITHDRAWAL, s.account.getAvailableCash(Currency.IDR), false, time(0), "QA cash drain");
        try { loan.makePayment("NO-CASH", s.account, cash(1_000_000), s.ledger, time(100)); fail(); } catch (IllegalStateException expected) { }
        assertEquals(before, loan.exportState().toString());
        try { loan.prepayFull("NO-CASH-FULL", s.account, s.ledger, time(100)); fail(); } catch (IllegalStateException expected) { }
        assertEquals(before, loan.exportState().toString());
    }
    @Test public void oldMaturedLoanAnchorsAtSavedClockWithoutRechargingUnknownHistory() throws Exception {
        Setup s = new Setup(); String id = s.borrow("ONE", 5_000_000, s.quote(5_000_000).getString("quoteVersion")).getString("loanId");
        LoanContract loan = s.loans.getLoan(id); loan.processDailyAccrual(time(50));
        JSONObject old = loan.exportState();
        for (String field : new String[]{"stateSchemaVersion", "scheduleVersion", "lastAccrualDay", "legacyScheduleMigration", "totalPenaltyAssessed", "schedule", "restructureHistory"}) old.remove(field);
        Money debt = loan.getTotalPayoffAmount(); InstallmentLoanManager migrated = new InstallmentLoanManager();
        migrated.restoreState(new JSONArray().put(old), time(50)); LoanContract restored = migrated.getLoan(id);
        assertEquals(debt, restored.getTotalPayoffAmount()); restored.processDailyAccrual(time(50)); assertEquals(debt, restored.getTotalPayoffAmount());
        String snapshot = restored.exportState().toString(); migrated.restoreState(new JSONArray().put(restored.exportState()), time(51));
        assertEquals(snapshot, migrated.getLoan(id).exportState().toString());
    }
}
