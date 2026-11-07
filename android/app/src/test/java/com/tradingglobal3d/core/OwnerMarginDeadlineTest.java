package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.risk.*;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class OwnerMarginDeadlineTest {
    @Test public void portfolioUsesTheSameLiveMarkAsMarginWithoutChangingHoldings() throws Exception {
        TradingFacade game = distressed();
        var personal = game.getPersonalAccount();
        var before = personal.getPosition("IDX:BBCA").getCurrentPrice();
        var portfolio = new JSONObject(new com.tradingglobal3d.mobile.TradingWebBridge(game).getPortfolio(personal.getId()));
        assertTrue(portfolio.toString(), portfolio.getBoolean("success"));
        var row = portfolio.getJSONArray("positions").getJSONObject(0);
        assertEquals("9000", row.getString("currentPrice"));
        assertEquals(Money.of(135000000, Currency.IDR).format(), row.getString("marketValue"));
        assertEquals(Money.of(-15000000, Currency.IDR).format(), row.getString("unrealizedPnL"));
        assertEquals(Money.of(135000000, Currency.IDR).format(), portfolio.getString("grossAssets"));
        assertEquals(Money.of(100000000, Currency.IDR).format(), portfolio.getString("liabilities"));
        assertEquals(Money.of(35000000, Currency.IDR).format(), portfolio.getString("netWorth"));
        assertEquals(before, personal.getPosition("IDX:BBCA").getCurrentPrice());
        assertEquals(15000, personal.getPosition("IDX:BBCA").getQuantity());
        assertTrue(game.reconcileAll());
    }

    @Test public void homeSnapshotIncludesMarginDebtAndTracksRepayment() throws Exception {
        TradingFacade game = distressed();
        JSONObject personal = new JSONObject(game.getSnapshotJson()).getJSONObject("personalAccount");
        assertEquals("100000000", personal.getString("loanDebtRaw"));
        var cash = Money.of(20000000, Currency.IDR);
        game.getLedger().recordMutation("QA-DEBT-PAYMENT", game.getPersonalAccount(), LedgerEntryType.DEPOSIT,
                cash, true, game.getGameTimestamp(), "QA repayment funding");
        game.repayMarginLoan(Money.of(10000000, Currency.IDR));
        personal = new JSONObject(game.getSnapshotJson()).getJSONObject("personalAccount");
        assertEquals("90000000", personal.getString("loanDebtRaw"));
        assertEquals(Money.of(90000000, Currency.IDR).format(), personal.getString("loanDebt"));
        assertTrue(game.reconcileAll());
    }

    @Test public void playerCannotSubmitAnOrderUsingTheLiquidationNamespace() throws Exception {
        TradingFacade game = distressed();
        int count = game.getOrderManager().getAllOrders().size();
        String request = new JSONObject().put("requestId", "MARGIN-LIQ-FAKE").put("instrumentId", "IDX:BBCA")
                .put("side", "SELL").put("type", "LIMIT").put("limitPrice", "9000").put("quantity", 100).toString();
        JSONObject result = new JSONObject(new com.tradingglobal3d.mobile.TradingWebBridge(game).submitOrder(request));
        assertFalse(result.getBoolean("success"));
        assertTrue(result.getString("message").contains("dicadangkan"));
        assertEquals(count, game.getOrderManager().getAllOrders().size());
    }
    private final long real = System.currentTimeMillis() + 86400000L;
    private TradingFacade distressed() throws Exception {
        TradingFacade game = new TradingFacade(SaveManager.forDirectory(Files.createTempDirectory("owner-margin-").toFile()));
        game.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(real), ZoneOffset.UTC));
        game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(Path.of(System.getProperty("annotation.root"),
                "android/app/src/main/assets/data/market_universe.json"))));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"),
                new GameClock(LocalDate.of(2026, 10, 19), LocalTime.NOON));
        JSONObject career = game.getCareerManager().exportState().put("rank", "CHIEF_INVESTMENT_OFFICER");
        game.getCareerManager().restoreState(career);
        game.takeMarginLoan(Money.of(100000000, Currency.IDR));
        Account account = game.getPersonalAccount();
        // ponytail: ledger-backed distressed holding isolates deadline behavior from order-entry tests.
        game.getLedger().recordMutation("QA-HOLDING", account, LedgerEntryType.TRADE_BUY,
                Money.of(150000000, Currency.IDR), false, game.getGameTimestamp(), "QA holding");
        account.addPosition(new Position("BBCA", "IDX", Currency.IDR, 15000, new BigDecimal("10000"), new BigDecimal("10000")));
        game.getCatalog().getById("IDX:BBCA").updatePrice(Money.of(9000, Currency.IDR), 0);
        assertFalse(game.executeForcedLiquidation("BBCA"));
        assertEquals(real + RiskComplianceService.MARGIN_CALL_GRACE_REAL_MS,
                game.getRiskService().getMarginCallDeadline(account.getId()));
        JSONObject warning = new JSONObject(game.getSnapshotJson()).getJSONObject("marginCall");
        assertEquals(real + RiskComplianceService.MARGIN_CALL_GRACE_REAL_MS, warning.getLong("deadlineRealAt"));
        assertEquals(RiskComplianceService.MARGIN_CALL_GRACE_REAL_MS, warning.getLong("remainingRealMs"));
        return game;
    }

    @Test public void deadlineIsRealPersistentAndCannotRestartWhenClockMovesBack() throws Exception {
        TradingFacade game = distressed();
        Account account = game.getPersonalAccount();
        long deadline = game.getRiskService().getMarginCallDeadline(account.getId());
        game.save();
        TradingFacade restored = new TradingFacade(game.getSaveManager());
        restored.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(real), ZoneOffset.UTC));
        restored.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(Path.of(System.getProperty("annotation.root"),
                "android/app/src/main/assets/data/market_universe.json"))));
        assertTrue(restored.startOrResume());
        assertEquals(deadline, restored.getRiskService().getMarginCallDeadline(account.getId()));
        RiskComplianceService risk = restored.getRiskService();
        risk.observeMarginCall(restored.getPersonalAccount(), restored.getCatalog(), restored.getFxService().getMidRate(),
                real + 60000, restored.getGameTimestamp());
        long remaining = risk.getMarginCallRemainingRealMs(account.getId(), real + 60000);
        risk.observeMarginCall(restored.getPersonalAccount(), restored.getCatalog(), restored.getFxService().getMidRate(),
                real - 60000, restored.getGameTimestamp());
        assertEquals(remaining, risk.getMarginCallRemainingRealMs(account.getId(), real - 60000));
        assertFalse(risk.isMarginCallDue(account.getId(), deadline - 1));
        assertTrue(risk.isMarginCallDue(account.getId(), deadline));
        assertEquals(15000, restored.getPersonalAccount().getPosition("IDX:BBCA").getQuantity());
    }

    @Test public void restoringCollateralBeforeDeadlineCancelsCall() throws Exception {
        TradingFacade game = distressed();
        Account account = game.getPersonalAccount();
        game.getLedger().recordMutation("QA-COLLATERAL", account, LedgerEntryType.DEPOSIT,
                Money.of(10000000, Currency.IDR), true, game.getGameTimestamp(), "QA top-up");
        game.advanceWorldSecondsForTest(1);
        assertEquals(0, game.getRiskService().getMarginCallDeadline(account.getId()));
        assertFalse(game.executeForcedLiquidation("BBCA"));
        assertEquals(15000, account.getPosition("IDX:BBCA").getQuantity());
        assertTrue(game.reconcileAll());
    }

    @Test public void expiredCallWaitsForHaltThenSellsThroughRealCounterpartyAndRepays() throws Exception {
        TradingFacade game = distressed();
        Account account = game.getPersonalAccount();
        game.getMarketEngine().haltTrading("IDX:BBCA");
        game.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(real + RiskComplianceService.MARGIN_CALL_GRACE_REAL_MS), ZoneOffset.UTC));
        game.advanceWorldSecondsForTest(1);
        assertEquals(15000, account.getPosition("IDX:BBCA").getQuantity());
        assertEquals(Money.zero(Currency.IDR), account.getTotalCash(Currency.IDR));
        assertTrue(game.getRiskService().getAuditLogs().stream().anyMatch(log -> log.getCategory() == ComplianceAuditLog.AuditCategory.HALT_ENFORCEMENT));
        game.getMarketEngine().resumeTrading("IDX:BBCA");
        Account mm = game.getAccount("ACC-NPC-MM-01");
        Order bid = new Order("QA-BID", "QA-BID", mm.getId(), "IDX:BBCA", OrderSide.BUY, OrderType.LIMIT,
                Money.of(9000, Currency.IDR), null, 15000, game.getGameTimestamp(), Money.zero(Currency.IDR));
        assertTrue(game.submitOrder(bid).isSuccess);
        long sharesBefore = game.getAllAccounts().stream().mapToLong(a -> a.getPosition("IDX:BBCA") == null ? 0 : a.getPosition("IDX:BBCA").getQuantity()).sum();
        game.advanceWorldSecondsForTest(1);
        assertEquals(0, account.getPosition("IDX:BBCA").getQuantity());
        assertEquals(Money.zero(Currency.IDR), game.getRiskService().getMarginDebt(account.getId(), Currency.IDR));
        assertTrue(game.getMarketEngine().getOrCreateBook(game.getCatalog().getById("IDX:BBCA")).getTradeHistory().stream()
                .anyMatch(fill -> account.getId().equals(fill.getSellAccountId())));
        long sharesAfter = game.getAllAccounts().stream().mapToLong(a -> a.getPosition("IDX:BBCA") == null ? 0 : a.getPosition("IDX:BBCA").getQuantity()).sum();
        assertEquals(sharesBefore, sharesAfter);
        assertTrue(game.reconcileAll());
    }

    @Test public void thinLiquidityKeepsLiquidationWholeAndLaterFillRepaysDebt() throws Exception {
        TradingFacade game = distressed();
        Account account = game.getPersonalAccount();
        for (Order order : game.getOrderManager().getAllOrders())
            if (order.getInstrumentId().equals("IDX:BBCA") && !order.getStatus().isFinal()) game.cancelOrder(order);
        game.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(real + RiskComplianceService.MARGIN_CALL_GRACE_REAL_MS), ZoneOffset.UTC));
        game.advanceWorldSecondsForTest(1);
        Order liquidation = game.getOrderManager().getAllOrders().stream()
                .filter(order -> order.getRequestId().startsWith("MARGIN-LIQ-")).findFirst().orElseThrow();
        assertEquals(OrderStatus.OPEN, liquidation.getStatus());
        assertEquals(0, liquidation.getFilledQuantity());
        assertEquals(15000, account.getPosition("IDX:BBCA").getQuantity());
        assertFalse(game.cancelOrder(liquidation));
        Account mm = game.getAccount("ACC-NPC-MM-01");
        for (int shares : new int[]{14900, 15000}) {
            String id = "QA-BID-" + shares;
            Order bid = new Order(id, id, mm.getId(), "IDX:BBCA", OrderSide.BUY, OrderType.LIMIT,
                    Money.of(9000, Currency.IDR), null, shares, game.getGameTimestamp(), Money.zero(Currency.IDR));
            assertTrue(game.submitOrder(bid).isSuccess);
            if (shares == 14900) {
                assertEquals(0, liquidation.getFilledQuantity());
                assertEquals(15000, account.getPosition("IDX:BBCA").getQuantity());
            }
        }
        assertEquals(OrderStatus.FILLED, liquidation.getStatus());
        game.advanceWorldSecondsForTest(1);
        assertEquals(Money.zero(Currency.IDR), game.getRiskService().getMarginDebt(account.getId(), Currency.IDR));
        assertEquals(0, game.getRiskService().getMarginCallDeadline(account.getId()));
        assertTrue(game.reconcileAll());
    }

    @Test public void offlineFourGameDaysCannotShortenTheRealGracePeriod() throws Exception {
        TradingFacade game = distressed();
        game.getResearchService().setDynamicDailyNewsEnabled(false);
        // Keep the account distressed even after four legitimate CIO salary payments.
        game.getCatalog().getById("IDX:BBCA").resetReferencePrice(Money.of(5000, Currency.IDR));
        Account account = game.getPersonalAccount();
        game.getMarketEngine().haltTrading("IDX:BBCA");
        long deadline = game.getRiskService().getMarginCallDeadline(account.getId());
        game.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(deadline - 1), ZoneOffset.UTC));
        game.advanceRealMs(RiskComplianceService.MARGIN_CALL_GRACE_REAL_MS - 1, false);
        assertEquals(15000, account.getPosition("IDX:BBCA").getQuantity());
        assertEquals(deadline, game.getRiskService().getMarginCallDeadline(account.getId()));
        assertFalse(game.getRiskService().isMarginCallDue(account.getId(), deadline - 1));
        game.save();
        game.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(deadline), ZoneOffset.UTC));
        game.advanceRealMs(1, false);
        assertTrue(game.getRiskService().isMarginCallDue(account.getId(), deadline));
        assertEquals(15000, account.getPosition("IDX:BBCA").getQuantity());
        assertTrue(game.reconcileAll());
    }
}
