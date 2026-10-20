package com.tradingglobal3d.core;

import com.tradingglobal3d.core.economy.*;
import com.tradingglobal3d.core.league.*;
import com.tradingglobal3d.core.market.*;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.trading.*;
import org.json.*;
import org.junit.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.Assert.*;

public class FeatureCompletionLeagueCanonicalTest {
    private static final Money WEALTH = Money.of(45_000_000, Currency.IDR);
    @Test public void unavailableReturnHasNoRankAndCannotPromoteOrClaimRivalWin() throws Exception {
        LeagueManager league = new LeagueManager();
        JSONObject board = league.getLeaderboardByWeeklyReturn((Double) null, WEALTH, "QA", "Trader");
        assertTrue(board.isNull("playerRank")); assertFalse(board.getBoolean("playerEligible"));
        JSONObject player = board.getJSONArray("leaderboard").getJSONObject(20);
        assertTrue(player.isNull("weeklyReturnRaw"));
        assertFalse(league.getRivalStatus(WEALTH, (Double) null).getBoolean("isLeading"));
        league.processWeeklyRollover((Double) null, WEALTH);
        assertEquals(LeagueManager.LeagueTier.BRONZE, league.getCurrentTier());
        assertTrue(league.getLeaderboardByWeeklyReturn(6.0, Money.zero(Currency.IDR), "QA", "Trader").isNull("playerRank"));
    }

    @Test public void canonicalNpcReturnsFollowRealFillsAndPricesNeverRandomRolloverWealth() throws Exception {
        LeagueManager league = new LeagueManager();
        Map<String, Account> accounts = new LinkedHashMap<>(); EconomyLedger ledger = new EconomyLedger();
        InstrumentCatalog catalog = new InstrumentCatalog();
        Instrument stock = new Instrument("IDX:BBCA", "BBCA", "QA", MarketExchange.IDX, "BANK", Currency.IDR,
                100, 1_000_000_000L, true, Money.of(1000, Currency.IDR));
        catalog.registerInstrument(stock); league.bindMarket(accounts, ledger, catalog, 1000);
        int seeded = ledger.getEntries().size(); league.bindMarket(accounts, ledger, catalog, 1001);
        assertEquals(20, accounts.size()); assertEquals(seeded, ledger.getEntries().size());
        Order intent = league.getNpcOrders(2000).get(0);
        assertEquals(intent.getRequestId(), league.getNpcOrders(2001).get(0).getRequestId());
        Money before = accounts.get(intent.getAccountId()).getTotalCash(Currency.IDR);
        assertEquals(before, accounts.get(intent.getAccountId()).getTotalCash(Currency.IDR));
        Account seller = new Account("SELLER", "QA seller", AccountType.FIRM, null); accounts.put(seller.getId(), seller);
        seller.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100, BigDecimal.valueOf(1000), BigDecimal.valueOf(1000)));
        MatchingEngine engine = new MatchingEngine(); OrderBook book = new OrderBook(stock.getId(), stock.getCurrentPrice());
        Order sell = new Order("QA-SELL", "QA-SELL", seller.getId(), stock.getId(), OrderSide.SELL, OrderType.LIMIT,
                stock.getCurrentPrice(), null, 100, 1500, Money.zero(Currency.IDR));
        assertTrue(engine.submitOrder(sell, seller, stock, book, ledger, accounts, 1500).isSuccess);
        assertTrue(engine.submitOrder(intent, accounts.get(intent.getAccountId()), stock, book, ledger, accounts, 2000).isSuccess);
        stock.updatePrice(Money.of(2000, Currency.IDR), 0); league.refreshNpcValuations();
        JSONObject npc = league.exportState().getJSONArray("competitors").getJSONObject(0);
        assertTrue(npc.getDouble("weeklyReturnPercent") > 0.0);
        String worth = npc.getString("netWorth");
        league.processWeeklyRollover(0.0, WEALTH);
        assertEquals(worth, league.exportState().getJSONArray("competitors").getJSONObject(0).getString("netWorth"));
        assertEquals(1, league.exportState().getJSONArray("periodHistory").length());
        LeagueManager restored = new LeagueManager(); restored.restoreState(league.exportState());
        restored.bindMarket(accounts, ledger, catalog, 3000);
        assertEquals(0.0, restored.exportState().getJSONArray("competitors").getJSONObject(0).getDouble("weeklyReturnPercent"), 1e-12);
        for (Account account : accounts.values()) assertTrue(account.getId(), ledger.reconcile(account, Currency.IDR));
    }

    @Test public void corruptRosterIsRejectedWithoutMutatingLiveState() throws Exception {
        LeagueManager league = new LeagueManager(); String before = league.exportState().toString();
        JSONObject invalid = new JSONObject(before); JSONArray roster = invalid.getJSONArray("competitors");
        roster.put(1, roster.getJSONObject(0));
        try { league.restoreState(invalid); fail("duplicate NPC accepted"); }
        catch (IllegalArgumentException expected) { }
        assertEquals(before, league.exportState().toString());
    }
}
