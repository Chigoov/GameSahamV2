package com.tradingglobal3d.core;

import com.tradingglobal3d.core.economy.*;
import com.tradingglobal3d.core.funds.*;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import org.json.*;
import org.junit.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;

public class FeatureCompletionFundBackingTest {
    private static long time(int day, int hour) {
        return LocalDate.of(2026, 10, 19).plusDays(day).atTime(hour, 0).toInstant(ZoneOffset.ofHours(7)).toEpochMilli();
    }
    private static final class World {
        final Map<String, Account> accounts = new LinkedHashMap<>();
        final EconomyLedger ledger = new EconomyLedger();
        MutualFundManager manager = new MutualFundManager();
        final Account player = new Account("P", "QA", AccountType.PERSONAL, null);
        World() {
            accounts.put("P", player);
            ledger.recordMutation("QA-SEED", player, LedgerEntryType.INITIAL_CAPITAL,
                    Money.of(100_000_000, Currency.IDR), true, time(0, 9), "Separate QA account");
            manager.registerPoolAccounts(accounts, ledger, time(0, 9));
        }
        MutualFund subscribe(String id) throws Exception {
            MutualFund fund = manager.getFund(id);
            fund.submitSubscription("SUB-" + id, player, Money.of(10_000_000, Currency.IDR), ledger, time(0, 10));
            manager.processCutoff(accounts, ledger, time(0, 16));
            return fund;
        }
        BigDecimal cash() {
            BigDecimal result = BigDecimal.ZERO;
            for (Account account : accounts.values()) result = result.add(account.getTotalCash(Currency.IDR).getAmount());
            return result;
        }
        void reconcile() {
            for (Account account : accounts.values()) assertTrue(account.getId(), ledger.reconcile(account, Currency.IDR));
        }
    }

    @Test public void principalAndCouponsMoveBetweenCanonicalAccountsAndNeverMintCash() throws Exception {
        for (String id : Arrays.asList("FUND-MM-01", "FUND-FI-01")) {
            World w = new World(); MutualFund fund = w.subscribe(id);
            JSONObject debt = fund.toJsonObject("P").getJSONObject("backingContract");
            assertTrue(new BigDecimal(debt.getString("principal")).signum() > 0);
            BigDecimal before = w.cash();
            fund.advanceDay(0.0, w.ledger, time(1, 9));
            assertEquals(0, before.compareTo(w.cash()));
            assertTrue(fund.getNavPerUnit().compareTo(MutualFund.INITIAL_NAV) > 0);
            String once = fund.exportState().toString(); int entries = w.ledger.getEntries().size();
            fund.advanceDay(0.0, w.ledger, time(1, 9));
            assertEquals(once, fund.exportState().toString());
            assertEquals(entries, w.ledger.getEntries().size()); w.reconcile();
        }
    }

    @Test public void exhaustedIssuerCreatesLossAndUnpaidLiabilityInsteadOfNewMoney() throws Exception {
        World w = new World(); MutualFund fund = w.subscribe("FUND-MM-01");
        Account issuer = w.accounts.get(fund.getDebtIssuerAccountId());
        BigDecimal beforeNav = fund.getNavPerUnit();
        w.ledger.recordMutation("QA-DEFAULT", issuer, LedgerEntryType.TRADE_BUY,
                issuer.getAvailableCash(Currency.IDR), false, time(1, 8), "Issuer credit-loss scenario");
        BigDecimal cash = w.cash();
        fund.advanceDay(0.0, w.ledger, time(1, 9));
        JSONObject debt = fund.exportState().getJSONObject("debtContract");
        assertTrue(new BigDecimal(debt.getString("accruedInterest")).signum() > 0);
        assertEquals("0", debt.getString("fairValue"));
        assertTrue(fund.getNavPerUnit().compareTo(beforeNav) < 0);
        assertEquals(0, cash.compareTo(w.cash())); w.reconcile();
    }

    @Test public void bondPriceRiskChangesNavWithoutChangingCashAndSurvivesReload() throws Exception {
        World w = new World(); MutualFund fund = w.subscribe("FUND-FI-01");
        BigDecimal before = fund.getNavPerUnit();
        BigDecimal cash = w.cash(); fund.advanceDay(0.05, w.ledger, time(1, 9));
        assertTrue(fund.getNavPerUnit().compareTo(before) < 0);
        assertEquals(0, cash.compareTo(w.cash()));
        JSONArray state = w.manager.exportState();
        MutualFundManager reloaded = new MutualFundManager(); reloaded.restoreState(state);
        reloaded.bindAccounts(w.accounts, new InstrumentCatalog());
        assertEquals(0, reloaded.getFund("FUND-FI-01").getNavPerUnit().compareTo(fund.getNavPerUnit()));
        w.reconcile();
    }

    @Test public void redemptionLiquidatesDebtAndSettlesOnlyAvailableCash() throws Exception {
        World w = new World(); MutualFund fund = w.subscribe("FUND-MM-01");
        fund.submitRedemption("REDEEM", w.player, fund.getUnits("P"), w.ledger, time(1, 10));
        w.manager.processCutoff(w.accounts, w.ledger, time(1, 16));
        assertEquals("SETTLED", fund.getOrder("REDEEM").getString("status"));
        assertEquals(0, fund.getTotalUnits().signum());
        assertEquals("0", fund.exportState().getJSONObject("debtContract").getString("principal"));
        w.reconcile();
    }

    @Test public void tenSequencesOfOneHundredCanonicalActionsPreserveLedgerAndReload() throws Exception {
        for (int seed = 0; seed < 10; seed++) {
            World w = new World();
            String id = seed % 2 == 0 ? "FUND-MM-01" : "FUND-FI-01";
            MutualFund fund = w.subscribe(id);
            Random random = new Random(seed);
            for (int action = 0; action < 100; action++) {
                int day = action + 1;
                switch (action % 5) {
                    case 0:
                        fund.submitSubscription("SEQ-" + action, w.player, Money.of(10_000 + random.nextInt(90_000), Currency.IDR), w.ledger, time(day, 10));
                        break;
                    case 1:
                        fund.advanceDay(id.equals("FUND-FI-01") ? random.nextDouble() * 0.04 - 0.02 : 0.0, w.ledger, time(day, 9));
                        break;
                    case 2:
                        if (fund.getAvailableUnits("P").compareTo(BigDecimal.ONE) >= 0)
                            fund.submitRedemption("RED-SEQ-" + action, w.player, BigDecimal.ONE, w.ledger, time(day, 10));
                        break;
                    case 3:
                        fund.advanceDay(0.0, w.ledger, time(day, 9));
                        String once = fund.exportState().toString();
                        fund.advanceDay(0.0, w.ledger, time(day, 9));
                        assertEquals(once, fund.exportState().toString());
                        break;
                    default:
                        MutualFundManager loaded = new MutualFundManager();
                        loaded.restoreState(w.manager.exportState()); loaded.bindAccounts(w.accounts, new InstrumentCatalog());
                        w.manager = loaded; fund = loaded.getFund(id);
                }
                w.manager.processCutoff(w.accounts, w.ledger, time(day, 16));
                assertTrue(fund.getNavPerUnit().signum() > 0);
                assertTrue(fund.getAvailableUnits("P").signum() >= 0);
                JSONObject state = fund.exportState(), debt = state.getJSONObject("debtContract");
                BigDecimal liability = new BigDecimal(debt.getString("principal")).add(new BigDecimal(debt.getString("accruedInterest")));
                assertEquals(0, liability.compareTo(new BigDecimal(debt.getString("issuerLiability"))));
                w.reconcile();
            }
        }
    }

    @Test public void defaultedFinalRedemptionDoesNotGiftOldDebtToNextSubscriber() throws Exception {
        World w = new World(); MutualFund fund = w.subscribe("FUND-MM-01");
        Account issuer = w.accounts.get(fund.getDebtIssuerAccountId());
        w.ledger.recordMutation("DEFAULT", issuer, LedgerEntryType.TRADE_BUY, issuer.getAvailableCash(Currency.IDR), false, time(1, 8), "QA default");
        fund.submitRedemption("FINAL", w.player, fund.getUnits("P"), w.ledger, time(1, 10));
        w.manager.processCutoff(w.accounts, w.ledger, time(1, 16));
        assertEquals("SETTLED", fund.getOrder("FINAL").getString("status"));
        assertEquals("0", fund.exportState().getJSONObject("debtContract").getString("issuerLiability"));
        w.reconcile();
    }
}
