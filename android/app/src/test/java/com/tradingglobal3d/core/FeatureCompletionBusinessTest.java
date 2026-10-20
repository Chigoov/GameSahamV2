package com.tradingglobal3d.core;

import com.tradingglobal3d.core.business.CompanyManager;
import com.tradingglobal3d.core.business.OperatingCompany;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.trading.TradeFill;
import com.tradingglobal3d.core.collection.CollectionManager;
import com.tradingglobal3d.core.player.InvestorArchetypeService;
import com.tradingglobal3d.core.player.PlayerProfileManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import static org.junit.Assert.*;

public class FeatureCompletionBusinessTest {
    @Test public void expansionIsPaidReviewedIdempotentAndSurvivesReload() throws Exception {
        CompanyManager manager = new CompanyManager();
        EconomyLedger ledger = new EconomyLedger();
        Account personal = new Account("P", "QA", AccountType.PERSONAL, null);
        ledger.recordMutation("INITIAL", personal, LedgerEntryType.INITIAL_CAPITAL, Money.of(100_000_000, Currency.IDR), true, 1, "QA");
        Map<String, Account> accounts = new HashMap<>(); accounts.put("P", personal);
        OperatingCompany company = manager.foundCompany("C", "QA", "Jasa", personal, Money.of(80_000_000, Currency.IDR), ledger, accounts, 1);
        Account account = accounts.get(company.getAccountId());
        String before = manager.exportState().toString();
        JSONObject quote = manager.previewCompanyAction("C", "UPGRADE", 8, "STANDARD");
        assertEquals(before, manager.exportState().toString());
        assertEquals("15000000", quote.getString("cost"));
        JSONObject receipt = manager.executeCompanyAction("EXPAND", "C", "UPGRADE", 8, "STANDARD", quote.getLong("version"), account, ledger, 2);
        assertEquals(2, company.getLevel());
        assertEquals(Money.of(65_000_000, Currency.IDR), account.getTotalCash(Currency.IDR));
        manager.restoreState(new JSONArray(manager.exportState().toString()));
        assertEquals(receipt.toString(), manager.executeCompanyAction("EXPAND", "C", "UPGRADE", 8, "STANDARD", 0, account, ledger, 3).toString());
        assertEquals(2, manager.getCompany("C").getLevel());
        try { manager.executeCompanyAction("STALE", "C", "UPGRADE", 8, "STANDARD", quote.getLong("version"), account, ledger, 3); fail(); }
        catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("STALE")); }
        assertTrue(ledger.reconcile(account, Currency.IDR));
    }

    @Test public void policyHasReviewHistoryAndInvalidInputCannotMutate() throws Exception {
        CompanyManager manager = new CompanyManager(); EconomyLedger ledger = new EconomyLedger();
        Account personal = new Account("P", "QA", AccountType.PERSONAL, null);
        ledger.recordMutation("INITIAL", personal, LedgerEntryType.INITIAL_CAPITAL, Money.of(25_000_000, Currency.IDR), true, 1, "QA");
        Map<String, Account> accounts = new HashMap<>(); accounts.put("P", personal);
        OperatingCompany company = manager.foundCompany("C", "QA", "Ritel", personal, Money.of(25_000_000, Currency.IDR), ledger, accounts, 1);
        Account account = accounts.get(company.getAccountId());
        JSONObject quote = manager.previewCompanyAction("C", "POLICY", 6, "PLUS");
        assertEquals("6250000", quote.getString("dailyPayrollAfter"));
        manager.executeCompanyAction("POL", "C", "POLICY", 6, "PLUS", quote.getLong("version"), account, ledger, 2);
        assertEquals(75, company.getMorale());
        assertEquals(1, manager.exportState().getJSONObject(0).getJSONArray("policyHistory").length());
        manager.restoreState(new JSONArray(manager.exportState().toString()));
        assertEquals(1, manager.exportState().getJSONObject(0).getJSONArray("policyHistory").length());
        String before = manager.exportState().toString();
        try { manager.previewCompanyAction("C", "POLICY", 7, "STANDARD"); fail(); }
        catch (IllegalArgumentException expected) { }
        assertEquals(before, manager.exportState().toString());
        try { manager.executeCompanyAction("NO-CASH", "C", "UPGRADE", 6, "PLUS", manager.getCompany("C").getStaffVersion(), account, ledger, 3); fail(); }
        catch (IllegalStateException expected) { }
        assertEquals(before, manager.exportState().toString());
    }

    @Test public void tradeStatsUseClosedTradesAndPreserveFillIdempotency() throws Exception {
        PlayerProfileManager profile = new PlayerProfileManager();
        assertEquals("Belum tersedia", profile.toJsonObject().getString("winRate"));
        profile.recordFillResult("BUY", null);
        assertFalse(profile.toJsonObject().getBoolean("winRateAvailable"));
        profile.recordFillResult("SELL", Money.of(1000, Currency.IDR));
        assertEquals("100.0%", profile.toJsonObject().getString("winRate"));
        assertEquals(Money.of(1000, Currency.IDR), profile.getWorstTradePnL());
        PlayerProfileManager loaded = new PlayerProfileManager(); loaded.restoreState(profile.exportState());
        loaded.recordFillResult("SELL", Money.of(1000, Currency.IDR));
        assertEquals(2, loaded.getTotalTradesExecuted());
        try { loaded.recordFillResult("SELL", Money.of(-1000, Currency.IDR)); fail(); }
        catch (IllegalArgumentException expected) { }
        loaded.recordFillResult("LOSS", Money.of(-500, Currency.IDR));
        assertEquals("50.0%", loaded.toJsonObject().getString("winRate"));
        assertEquals(2, loaded.toJsonObject().getInt("closedTrades"));
    }

    @Test public void profileRejectsInvalidPreferencesAndRestoresUnicodeIdentity() throws Exception {
        PlayerProfileManager profile = new PlayerProfileManager(); profile.setPlayerName("😀 Trader");
        assertEquals("😀T", profile.getAvatarMonogram());
        profile.setPreferences("200%", true, true);
        PlayerProfileManager restored = new PlayerProfileManager(); restored.restoreState(profile.exportState());
        assertEquals("😀T", restored.getAvatarMonogram());
        assertEquals("200%", restored.toJsonObject().getJSONObject("preferences").getString("textSize"));
        String before = restored.exportState().toString();
        try { restored.setPreferences("999%", false, false); fail(); } catch (IllegalArgumentException expected) { }
        try { restored.setPlayerName("bad\nname"); fail(); } catch (IllegalArgumentException expected) { }
        assertEquals(before, restored.exportState().toString());
    }

    @Test public void archetypeNeedsPositiveEquityAndDoesNotPunishLifetimeFills() throws Exception {
        Account account = new Account("P", "QA", AccountType.PERSONAL, null);
        account.creditCash(Money.of(100_000_000, Currency.IDR));
        assertEquals("INSUFFICIENT_DATA", InvestorArchetypeService.evaluateArchetype(20, 100, account,
                Money.of(-1, Currency.IDR), Money.zero(Currency.IDR)).getString("archetype"));
        assertEquals("CONSERVATIVE", InvestorArchetypeService.evaluateArchetype(20, 100, account,
                Money.of(100_000_000, Currency.IDR), Money.zero(Currency.IDR)).getString("archetype"));
    }

    @Test public void fullArchetypeUsesRealFillsHoldingTurnoverAndDiversification() throws Exception {
        Account account = new Account("P", "QA", AccountType.PERSONAL, null);
        List<TradeFill> fills = new ArrayList<>();
        long day = 86_400_000L;
        for (int i = 0; i < 5; i++) fills.add(new TradeFill("F" + i, "B", "S", "P", "NPC", "IDX:BBCA",
                Money.of(100, Currency.IDR), 100, day));
        account.addPosition(new Position("BBCA", "IDX", Currency.IDR, 500, BigDecimal.valueOf(100), BigDecimal.valueOf(100)));
        JSONObject profile = InvestorArchetypeService.evaluateArchetype(fills, 8, account, Money.of(100_000, Currency.IDR),
                Money.zero(Currency.IDR), BigDecimal.valueOf(15800), 8 * day);
        assertEquals(7.0, profile.getDouble("averageHoldingDays"), .0001);
        assertEquals(.5, profile.getDouble("stockExposureRatio"), .0001);
        assertEquals(1, profile.getInt("positionCount"));
        assertEquals(0.0, profile.getDouble("turnoverRatio"), .0001);
        fills.add(fills.get(0));
        fills.add(new TradeFill("FUTURE", "B", "S", "P", "NPC", "IDX:BBCA", Money.of(100, Currency.IDR), 100, 9 * day));
        JSONObject again = InvestorArchetypeService.evaluateArchetype(fills, 8, account, Money.of(100_000, Currency.IDR),
                Money.zero(Currency.IDR), BigDecimal.valueOf(15800), 8 * day);
        assertEquals(5, again.getInt("totalTrades"));
        assertEquals(profile.getDouble("riskScore"), again.getDouble("riskScore"), .0001);
    }

    @Test public void capitalReviewIsIdempotentKeepsReserveAndDailyReportsPersist() throws Exception {
        CompanyManager manager = new CompanyManager(); EconomyLedger ledger = new EconomyLedger();
        Account personal = new Account("P", "QA", AccountType.PERSONAL, null);
        ledger.recordMutation("INITIAL", personal, LedgerEntryType.INITIAL_CAPITAL, Money.of(100_000_000, Currency.IDR), true, 1, "QA");
        Map<String, Account> accounts = new HashMap<>(); accounts.put("P", personal);
        OperatingCompany company = manager.foundCompany("C", "QA", "Jasa", personal, Money.of(25_000_000, Currency.IDR), ledger, accounts, 1);
        Account account = accounts.get(company.getAccountId());
        JSONObject quote = manager.previewCapitalTransfer("C", true, personal, account, Money.of(10_000_000, Currency.IDR));
        JSONObject receipt = manager.executeCapitalTransfer("CAP", "C", true, personal, account, Money.of(10_000_000, Currency.IDR), quote.getString("version"), ledger, 2);
        manager.restoreState(new JSONArray(manager.exportState().toString())); company = manager.getCompany("C");
        assertEquals(receipt.toString(), manager.executeCapitalTransfer("CAP", "C", true, personal, account, Money.of(10_000_000, Currency.IDR), "old", ledger, 3).toString());
        assertEquals(Money.of(35_000_000, Currency.IDR), account.getTotalCash(Currency.IDR));
        assertFalse(manager.previewCapitalTransfer("C", false, personal, account, Money.of(30_000_000, Currency.IDR)).getBoolean("eligible"));
        manager.processDailyOperations(account, company, ledger, 86_400_000L);
        manager.processDailyOperations(account, company, ledger, 2 * 86_400_000L);
        manager.processDailyOperations(account, company, ledger, 2 * 86_400_000L);
        manager.restoreState(new JSONArray(manager.exportState().toString()));
        assertEquals(2, manager.exportState().getJSONObject(0).getJSONArray("operationalReports").length());
        assertTrue(ledger.reconcile(personal, Currency.IDR)); assertTrue(ledger.reconcile(account, Currency.IDR));
    }

    @Test public void collectionValuationIsAnchoredPartitionInvariantAndMigrates() throws Exception {
        CollectionManager bulk = new CollectionManager(), chunks = new CollectionManager();
        long origin = 1_790_000_000_000L, day = 86_400_000L;
        bulk.updateValuations(origin); chunks.updateValuations(origin);
        Money original = bulk.getItem("COL-CAR-00").getCurrentValuation();
        bulk.updateValuations(origin + 20 * day);
        for (int i = 1; i <= 20; i++) { chunks.updateValuations(origin + i * day); chunks.restoreState(new JSONArray(chunks.exportState().toString())); }
        assertEquals(bulk.exportState().toString(), chunks.exportState().toString());
        assertTrue(bulk.getItem("COL-CAR-00").getCurrentValuation().isLessThan(original));
        assertEquals("CATEGORY_CALENDAR_V1", bulk.getItem("COL-CAR-00").toJsonObject().getString("valuationRule"));
        String before = bulk.exportState().toString();
        try { bulk.updateValuations(origin); fail(); } catch (IllegalArgumentException expected) { }
        assertEquals(before, bulk.exportState().toString());
    }
}
