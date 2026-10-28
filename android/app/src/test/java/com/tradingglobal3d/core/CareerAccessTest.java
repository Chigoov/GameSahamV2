package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.career.CareerRank;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import com.tradingglobal3d.core.research.TerminalTier;
import org.json.JSONObject;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.math.BigDecimal;
import java.nio.file.*;
import static org.junit.Assert.*;

public class CareerAccessTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private TradingFacade game;
    @Before public void start() throws Exception {
        game = new TradingFacade(SaveManager.forDirectory(temporary.newFolder()));
        game.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")), java.nio.charset.StandardCharsets.UTF_8)));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
    }
    @Test public void lockedCommandsCannotMutateMoneyAccountsOrXp() throws Exception {
        int entries = game.getLedger().getEntries().size();
        java.util.Set<String> accountIds = new java.util.HashSet<>();
        for (Account account : game.getAllAccounts()) accountIds.add(account.getId());
        Money idr = game.getPersonalAccount().getTotalCash(Currency.IDR);
        Money usd = game.getPersonalAccount().getTotalCash(Currency.USD);
        Runnable[] commands = {
                () -> game.acceptClientMandate("MANDATE-01"),
                () -> game.createClientAccount("Bypass", null, Money.of(1000, Currency.IDR)),
                () -> game.takeMarginLoan(Money.of(1000, Currency.IDR)),
                () -> game.openShortPosition("BBCA", 100),
                () -> game.subscribeTerminal(TerminalTier.PRO),
                () -> game.saveFundPlan("BYPASS", game.getPersonalAccount().getId(), "Plan", java.util.Collections.emptyMap(), 1)
        };
        for (Runnable command : commands) {
            try { command.run(); fail("Expected career restriction"); }
            catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("jabatan")); }
        }
        assertEquals(entries, game.getLedger().getEntries().size());
        java.util.Set<String> afterIds = new java.util.HashSet<>();
        for (Account account : game.getAllAccounts()) afterIds.add(account.getId());
        assertEquals(accountIds, afterIds);
        assertEquals(idr, game.getPersonalAccount().getTotalCash(Currency.IDR));
        assertEquals(usd, game.getPersonalAccount().getTotalCash(Currency.USD));
        assertEquals(0, game.getProgressManager().getCurrentXp());
        assertTrue(game.getCalendar().isEmpty());
        assertTrue(game.subscribeTerminal(TerminalTier.BASIC));
    }
    @Test public void realTradingRewardsAndSalaryReachClientManagementWithoutInjectedState() throws Exception {
        long untilOpen = java.time.Duration.between(game.getClock().getCurrentTime(), java.time.LocalTime.of(9,0)).getSeconds();
        long rate = com.tradingglobal3d.core.clock.GameClock.FOREGROUND_GAME_SECONDS_PER_REAL_SECOND;
        game.setClockState(true, 1); game.tick((int)((untilOpen + rate - 1)/rate)); game.setClockState(false, 1);
        assertTrue(game.getClock().isTradingOpenFor(com.tradingglobal3d.core.market.MarketExchange.IDX));
        String account = game.getPersonalAccount().getId();
        for (String instrument : new String[]{"IDX:BBCA", "IDX:UNVR"}) {
            com.tradingglobal3d.core.market.Instrument stock = game.getCatalog().getById(instrument);
            assertTrue("Career access fixture must use stocks open at the starting rank",
                    game.isSectorUnlocked(stock.getSector(), stock.getExchange()));
            OrderBook book = game.getMarketEngine().getOrCreateBook(stock);
            BigDecimal tick = stock.getExchange().getTickSize(stock.getCurrentPrice().getAmount());
            BigDecimal ask = stock.getCurrentPrice().getAmount().add(tick);
            if (!book.getBids().isEmpty()) ask = ask.max(book.getBids().firstKey().add(tick));
            // ponytail: fund the fixture through an existing dealer's real shares, not a fabricated fill.
            MatchingEngine.OrderResult quote = game.submitOrder(Order.createLimit("CAREER-MM-"+instrument,
                    instrument, "ACC-NPC-MM-01", OrderSide.SELL, new Money(ask, stock.getCurrency()), 100, game.getGameTimestamp()));
            assertTrue(quote.message, quote.isSuccess);
            assertEquals(100, quote.order.getRemainingQuantity());
            Order buy = new Order("CAREER-"+instrument, "REQ-CAREER-"+instrument, account, instrument,
                    OrderSide.BUY, OrderType.MARKET, null, null, 100, game.getGameTimestamp(), null);
            MatchingEngine.OrderResult result = game.submitOrder(buy);
            assertTrue(result.message, result.isSuccess);
            assertEquals(result.message, OrderStatus.FILLED, result.order.getStatus());
            assertEquals(100, result.order.getFilledQuantity());
            assertEquals(100, game.getPersonalAccount().getPosition(instrument).getQuantity());
        }
        assertTrue(game.getCareerManager().getMission("MIS-01").isCompleted());
        assertTrue(game.getCareerManager().getMission("MIS-02").isCompleted());
        game.claimMission("MIS-01"); game.claimMission("MIS-02");
        for (int day=0; day<45 && game.getCareerManager().getCurrentRank()!=CareerRank.PORTFOLIO_MANAGER; day++)
            game.skipDay("CAREER-DAY-"+day);
        assertEquals(CareerRank.PORTFOLIO_MANAGER, game.getCareerManager().getCurrentRank());
        assertTrue(new JSONObject(game.getSnapshotJson()).getJSONObject("featureAccess").getJSONObject("mandates").getBoolean("unlocked"));
        assertNotNull(game.acceptClientMandate("MANDATE-01"));
        game.save(); assertTrue(game.load());
        assertEquals(CareerRank.PORTFOLIO_MANAGER, game.getCareerManager().getCurrentRank());
        assertEquals(1, game.getFirmManager().getActiveMandates().size());
        assertTrue(game.reconcileAll());
        game.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        assertFalse(new JSONObject(game.getSnapshotJson()).getJSONObject("featureAccess").getJSONObject("mandates").getBoolean("unlocked"));
    }
    @Test public void repaymentRemainsAvailableAfterRankLoss() throws Exception {
        GameTestSupport.unlockManager(game);
        game.takeMarginLoan(Money.of(1000, Currency.IDR));
        JSONObject state = game.getCareerManager().exportState(); state.put("rank", "NOVICE_TRADER");
        game.getCareerManager().restoreState(state);
        game.repayMarginLoan(Money.of(1000, Currency.IDR));
        assertEquals(Money.zero(Currency.IDR), game.getRiskService().getMarginDebt(game.getPersonalAccount().getId(), Currency.IDR));
    }

    @Test public void borrowedCashCannotUnlockWealthMissionOrPromotion() throws Exception {
        JSONObject state = game.getCareerManager().exportState();
        state.put("rank", "PORTFOLIO_MANAGER").put("days", 7).put("reputation", 80);
        game.getCareerManager().restoreState(state);
        game.takeMarginLoan(Money.of(200_000_000, Currency.IDR));
        game.evaluateTriggers();
        assertFalse(game.getCareerManager().getMission("MIS-03").isCompleted());
        assertEquals(CareerRank.PORTFOLIO_MANAGER, game.evaluateCareerPromotion());
        assertEquals("50000000", new JSONObject(game.getSnapshotJson()).getJSONObject("personalAccount").getString("netWorthRaw"));
    }
}
