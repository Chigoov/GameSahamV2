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
        assertEquals(2, game.getAllAccounts().size());
        assertEquals(0, game.getProgressManager().getCurrentXp());
        assertTrue(game.getCalendar().isEmpty());
        assertTrue(game.subscribeTerminal(TerminalTier.BASIC));
    }
    @Test public void realTradingRewardsAndSalaryReachClientManagementWithoutInjectedState() throws Exception {
        game.setClockState(true, 1); game.tick(7); game.setClockState(false, 1);
        String account = game.getPersonalAccount().getId();
        assertTrue(game.submitOrder(Order.createLimit("CAREER-BBCA", "IDX:BBCA", account, OrderSide.BUY, Money.of(10500, Currency.IDR), 100, 1)).order.getFilledQuantity()>0);
        assertTrue(game.submitOrder(Order.createLimit("CAREER-TLKM", "IDX:TLKM", account, OrderSide.BUY, Money.of(3150, Currency.IDR), 100, 2)).order.getFilledQuantity()>0);
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
