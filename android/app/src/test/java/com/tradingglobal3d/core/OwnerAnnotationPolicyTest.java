package com.tradingglobal3d.core;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.risk.RiskComplianceService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.Test;
import static org.junit.Assert.*;

public class OwnerAnnotationPolicyTest {
    @Test public void lockedStockRejectsManualOrdersWithoutBlockingSystemLiquidation() throws Exception {
        var game = gameAt(LocalTime.NOON);
        var stock = game.getCatalog().getById("US:AAPL");
        var account = game.getPersonalAccount();
        var currency = com.tradingglobal3d.core.model.Currency.USD;
        game.getLedger().recordMutation("QA-LOCKED-USD", account,
            com.tradingglobal3d.core.economy.LedgerEntryType.DEPOSIT,
            com.tradingglobal3d.core.model.Money.of(1000, currency), true, game.getGameTimestamp(), "QA funding");
        account.addPosition(new com.tradingglobal3d.core.model.Position("AAPL", "US", currency, 1,
            stock.getCurrentPrice().getAmount(), stock.getCurrentPrice().getAmount()));
        var cash = account.getAvailableCash(currency);
        for (var side : com.tradingglobal3d.core.trading.OrderSide.values()) {
            String id = "QA-LOCKED-" + side;
            var order = new com.tradingglobal3d.core.trading.Order(id, id, account.getId(), stock.getId(), side,
                com.tradingglobal3d.core.trading.OrderType.MARKET, null, null, 1, game.getGameTimestamp(), null);
            var quote = game.previewOrder(order);
            assertFalse(quote.toString(), quote.getBoolean("success"));
            assertTrue(quote.toString(), quote.getString("message").contains("terkunci"));
            var result = game.submitOrder(order);
            assertFalse(result.message, result.isSuccess);
            assertTrue(result.message, result.message.contains("terkunci"));
        }
        assertEquals(cash, account.getAvailableCash(currency));
        assertEquals(1, account.getPosition(stock.getId()).getQuantity());
        var forced = new com.tradingglobal3d.core.trading.Order("MARGIN-LIQ-QA", "MARGIN-LIQ-QA", account.getId(), stock.getId(),
            com.tradingglobal3d.core.trading.OrderSide.SELL, com.tradingglobal3d.core.trading.OrderType.LIMIT,
            stock.getDailyFloorPrice(), null, 1, game.getGameTimestamp(), null);
        var liquidation = game.submitOrder(forced);
        assertTrue(liquidation.message, liquidation.isSuccess);
        assertNotEquals(com.tradingglobal3d.core.trading.OrderStatus.REJECTED, forced.getStatus());
    }
    @Test public void newGameListsIposWhoseListingDateAlreadyPassed() throws Exception {
        var game = gameAt(LocalTime.NOON);
        int past = 0;
        for (var ipo : game.getCorporateService().getIpoOfferings()) {
            if (!ipo.getListingDate().isAfter(game.getClock().getCurrentDate())) {
                past++;
                assertTrue("Past IPO still ongoing: " + ipo.getId(), ipo.isAllotted());
                String exchange = ipo.getCurrency() == com.tradingglobal3d.core.model.Currency.USD ? "US:" : "IDX:";
                var stock = game.getCatalog().getById(exchange + ipo.getTicker());
                assertNotNull(stock);
                var price = stock.getCurrentPrice().getAmount();
                assertEquals("IPO opening price must use a valid tick", 0,
                    price.remainder(stock.getExchange().getTickSize(price)).signum());
            } else assertFalse(ipo.isAllotted());
        }
        assertTrue(past > 0);
        assertTrue(game.reconcileAll());
        game.getCorporateService().registerIpoOffering(new com.tradingglobal3d.core.corporate.IpoOffering(
            "QA-PAST-RESUME", "QAPR", "QA Past Resume", com.tradingglobal3d.core.model.Currency.IDR,
            com.tradingglobal3d.core.model.Money.of(500, com.tradingglobal3d.core.model.Currency.IDR),
            100, 100000, game.getClock().getCurrentDate().minusDays(1)));
        var before = game.getPersonalAccount().getTotalCash(com.tradingglobal3d.core.model.Currency.IDR);
        game.save(); assertTrue(game.startOrResume());
        assertTrue(game.getCorporateService().getIpo("QA-PAST-RESUME").isAllotted());
        assertNotNull(game.getCatalog().getById("IDX:QAPR"));
        assertEquals(before, game.getPersonalAccount().getTotalCash(com.tradingglobal3d.core.model.Currency.IDR));
        assertTrue(game.reconcileAll());
    }
    @Test public void ipoAllocatesDistributesAndListsAcrossSeparateDaysAndReload() throws Exception {
        var game = gameAt(LocalTime.NOON);
        var today = game.getClock().getCurrentDate();
        var currency = com.tradingglobal3d.core.model.Currency.IDR;
        var ipo = new com.tradingglobal3d.core.corporate.IpoOffering(
                "QA-STAGED", "QAST", "QA Staged", currency,
                com.tradingglobal3d.core.model.Money.of(500, currency), 100, 100000, today.plusDays(3));
        game.getCorporateService().registerIpoOffering(ipo);
        assertEquals(today.minusDays(2), ipo.getOfferingStartDate());
        assertEquals(today, ipo.getOfferingEndDate());
        var sub = game.orderIpo(ipo.getId(), 2);
        game.advanceWorldSecondsForTest(86400);
        assertTrue(ipo.isAllotted());
        assertEquals(2, sub.getAllottedLots());
        assertFalse(ipo.isDistributed());
        assertNull(game.getPersonalAccount().getPosition("IDX:QAST"));
        assertNull(game.getCatalog().getById("IDX:QAST"));

        game.save();
        assertTrue(game.startOrResume());
        ipo = game.getCorporateService().getIpo(ipo.getId());
        assertTrue(ipo.isAllotted());
        assertFalse(ipo.isDistributed());
        game.advanceWorldSecondsForTest(86400);
        assertTrue(ipo.isDistributed());
        assertEquals(200, game.getPersonalAccount().getPosition("IDX:QAST").getQuantity());
        assertNull(game.getCatalog().getById("IDX:QAST"));

        game.save();
        assertTrue(game.startOrResume());
        game.advanceWorldSecondsForTest(86400);
        assertNotNull(game.getCatalog().getById("IDX:QAST"));
        assertEquals(200, game.getPersonalAccount().getPosition("IDX:QAST").getQuantity());
        assertTrue(game.reconcileAll());
    }

    @Test public void allottedIpoRetainsValueUntilSharesAreDistributed() throws Exception {
        var game = gameAt(LocalTime.NOON);
        var currency = com.tradingglobal3d.core.model.Currency.IDR;
        var ipo = game.getCorporateService().getIpo("IPO-CLOUD");
        var account = game.getPersonalAccount();
        var sub = game.orderIpo(ipo.getId(), 2);
        var wealth = game.getPersonalNetWorth();
        var cash = account.getTotalCash(currency);
        var accounts = game.getAllAccounts().stream().collect(java.util.stream.Collectors.toMap(a -> a.getId(), a -> a));
        game.getCorporateService().allocateIpo(ipo, 1, accounts, game.getLedger(), game.getGameTimestamp());
        assertEquals("Allocation must preserve value while shares await distribution", wealth, game.getPersonalNetWorth());
        assertEquals(cash, account.getTotalCash(currency));
        assertEquals(sub.getReservedMoney(), account.getReservedCash(currency));
        assertEquals(sub.getReservedMoney().format(), game.getMyIpoSubscriptions().getJSONObject(0).getString("heldMoney"));
        assertFalse(ipo.isDistributed());
        game.save();
        assertTrue(game.startOrResume());
        game.allotIpo(ipo.getId(), 1);
        assertEquals(cash.subtract(sub.getReservedMoney()), game.getPersonalAccount().getTotalCash(currency));
        assertTrue(game.getPersonalAccount().getReservedCash(currency).isZero());
        assertEquals(com.tradingglobal3d.core.model.Money.zero(currency).format(), game.getMyIpoSubscriptions().getJSONObject(0).getString("heldMoney"));
        assertEquals(200, game.getPersonalAccount().getPosition("IDX:NCLD").getQuantity());
        game.allotIpo(ipo.getId(), 1);
        assertEquals(200, game.getPersonalAccount().getPosition("IDX:NCLD").getQuantity());
        assertEquals(cash.subtract(sub.getReservedMoney()), game.getPersonalAccount().getTotalCash(currency));
        assertTrue(game.reconcileAll());
    }

    @Test public void previouslyPaidStagedIpoDistributesWithoutAnotherDebit() throws Exception {
        var game = gameAt(LocalTime.NOON);
        var ipo = game.getCorporateService().getIpo("IPO-CLOUD");
        var sub = game.orderIpo(ipo.getId(), 2);
        var account = game.getPersonalAccount();
        var accounts = game.getAllAccounts().stream().collect(java.util.stream.Collectors.toMap(a -> a.getId(), a -> a));
        game.getCorporateService().allocateIpo(ipo, 1, accounts, game.getLedger(), game.getGameTimestamp());
        account.releaseReservedCash(sub.getReservedMoney());
        game.getLedger().recordMutation("IPO-PAY-" + sub.getId(), account,
                com.tradingglobal3d.core.economy.LedgerEntryType.TRADE_BUY,
                sub.getReservedMoney(), false, game.getGameTimestamp(), "QA earlier staged payment");
        var cash = account.getTotalCash(ipo.getCurrency());
        game.save();
        assertTrue(game.startOrResume());
        game.allotIpo(ipo.getId(), 1);
        assertEquals(cash, game.getPersonalAccount().getTotalCash(ipo.getCurrency()));
        assertEquals(200, game.getPersonalAccount().getPosition("IDX:NCLD").getQuantity());
        assertTrue(game.reconcileAll());
    }

    @Test public void shortRejectsLevelLockedStockEvenAtPortfolioManagerRank() throws Exception {
        var game = gameAt(LocalTime.NOON);
        GameTestSupport.unlockManager(game);
        var stock = game.getCatalog().getAllInstruments().stream()
                .filter(i -> !game.isSectorUnlocked(i.getSector(), i.getExchange()))
                .findFirst().orElseThrow();
        var error = assertThrows(IllegalArgumentException.class,
                () -> game.openShortPosition(stock.getTicker(), stock.getLotSize()));
        assertTrue(error.getMessage(), error.getMessage().contains("Emiten terkunci"));
    }

    @Test public void portfolioManagerPromotionOpensFirmWithoutAnotherAction() throws Exception {
        var game = gameAt(LocalTime.NOON);
        var career = game.getCareerManager();
        career.restoreState(career.exportState().put("rank", "SENIOR_ANALYST").put("days", 4).put("reputation", 50));
        assertFalse(new org.json.JSONObject(game.getSnapshotJson()).getJSONObject("featureAccess")
                .getJSONObject("mandates").getBoolean("unlocked"));
        assertThrows(IllegalStateException.class, () -> game.acceptClientMandate("MANDATE-01"));

        var currency = com.tradingglobal3d.core.model.Currency.IDR;
        game.getLedger().recordMutation("QA-FIRM-PROMOTION", game.getPersonalAccount(),
                com.tradingglobal3d.core.economy.LedgerEntryType.DEPOSIT,
                com.tradingglobal3d.core.model.Money.of(50000000, currency), true,
                game.getGameTimestamp(), "QA promotion capital");
        assertEquals("PORTFOLIO_MANAGER", game.evaluateCareerPromotion().name());
        assertTrue(new org.json.JSONObject(game.getSnapshotJson()).getJSONObject("featureAccess")
                .getJSONObject("mandates").getBoolean("unlocked"));
        var client = game.acceptClientMandate("MANDATE-01");
        assertEquals(com.tradingglobal3d.core.model.Money.of(100000000, currency), client.getTotalCash(currency));
        game.save();
        assertTrue(game.startOrResume());
        assertEquals(client.getId(), game.acceptClientMandate("MANDATE-01").getId());
        assertTrue(game.reconcileAll());
    }

    @Test public void leveragedOrderPersistsOnceThroughTheBridge() throws Exception {
        verifyLeveragedOrder(2);
        verifyLeveragedOrder(3);
    }

    private void verifyLeveragedOrder(int multiplier) throws Exception {
        var game = gameAt(LocalTime.NOON);
        GameTestSupport.unlockManager(game);
        if (multiplier == 3) game.getCareerManager().restoreState(game.getCareerManager().exportState()
                .put("rank", "CHIEF_INVESTMENT_OFFICER"));
        var stock = game.getCatalog().getById("IDX:BBCA");
        stock.updatePrice(stock.getCurrentPrice(), 5000L * stock.getLotSize());
        game.generateNpcLiquidity();
        var account = game.getPersonalAccount();
        var bridge = new com.tradingglobal3d.mobile.TradingWebBridge(game);
        var capacity = new org.json.JSONObject(bridge.getLeveragedOrderCapacity(account.getId(), stock.getId(), multiplier));
        assertTrue(capacity.toString(), capacity.getBoolean("success"));
        assertTrue(capacity.toString(), capacity.getLong("maxLots") > 0);
        var request = new org.json.JSONObject().put("requestId", "QA-LEVERAGED-BUY").put("instrumentId", stock.getId())
                .put("side", "BUY").put("type", "MARKET").put("quantity", stock.getLotSize()).put("leverageMultiplier", multiplier);
        var quote = new org.json.JSONObject(bridge.previewOrder(request.toString()));
        assertTrue(quote.toString(), quote.getBoolean("success"));
        var cashBefore = account.getAvailableCash(stock.getCurrency());
        request.put("marginPrincipalRaw", "1");
        var changed = new org.json.JSONObject(bridge.submitOrder(request.toString()));
        assertFalse(changed.toString(), changed.getBoolean("success"));
        assertTrue(changed.getString("message").contains("Nilai pinjaman berubah"));
        assertEquals(cashBefore, account.getAvailableCash(stock.getCurrency()));
        request.put("marginPrincipalRaw", quote.getString("marginPrincipalRaw"));
        game.getSaveManager().setWriteAuthority(() -> false);
        try {
            var failed = new org.json.JSONObject(bridge.submitOrder(request.toString()));
            assertFalse(failed.toString(), failed.getBoolean("success"));
        } finally { game.getSaveManager().setWriteAuthority(() -> true); }
        account = game.getPersonalAccount();
        assertEquals(cashBefore, account.getAvailableCash(stock.getCurrency()));
        assertNull(account.getPosition(stock.getId()));
        assertTrue(game.getRiskService().getMarginDebt(account.getId(), stock.getCurrency()).isZero());
        var result = new org.json.JSONObject(bridge.submitOrder(request.toString()));
        assertTrue(result.toString(), result.getBoolean("success"));
        assertEquals("FILLED", result.getString("status"));
        var debt = game.getRiskService().getMarginDebt(account.getId(), stock.getCurrency());
        assertEquals(0, debt.getAmount().compareTo(new BigDecimal(quote.getString("marginPrincipalRaw"))));
        var receipt = new org.json.JSONObject(bridge.getOrderStatus(request.getString("requestId")));
        assertEquals(multiplier, receipt.getInt("leverageMultiplier"));
        assertEquals(debt.getAmount(), new BigDecimal(receipt.getString("marginPrincipalRaw")));
        assertEquals(cashBefore.subtract(account.getAvailableCash(stock.getCurrency())).getAmount(),
            new BigDecimal(receipt.getString("ownCashRaw")));
        var retry = new org.json.JSONObject(bridge.submitOrder(request.toString()));
        assertEquals(result.getString("orderId"), retry.getString("orderId"));
        assertEquals(stock.getLotSize(), account.getPosition(stock.getId()).getQuantity());
        assertEquals(debt, game.getRiskService().getMarginDebt(account.getId(), stock.getCurrency()));
        assertTrue(game.reconcileAll());
        assertTrue(game.load());
        var afterReload = new org.json.JSONObject(bridge.submitOrder(request.toString()));
        assertEquals(result.getString("orderId"), afterReload.getString("orderId"));
        assertEquals(debt, game.getRiskService().getMarginDebt(account.getId(), stock.getCurrency()));
        var restoredReceipt = new org.json.JSONObject(bridge.getOrderStatus(request.getString("requestId")));
        assertEquals(receipt.getString("marginPrincipalRaw"), restoredReceipt.getString("marginPrincipalRaw"));
        assertEquals(receipt.getString("ownCashRaw"), restoredReceipt.getString("ownCashRaw"));
    }
    @Test public void researchRenewalCanBeResumedWithoutRebillingAndSurvivesReload() throws Exception {
        com.tradingglobal3d.core.facade.TradingFacade game = gameAt(LocalTime.NOON);
        GameTestSupport.unlockManager(game);
        com.tradingglobal3d.core.model.Account account = game.getPersonalAccount();
        game.getLedger().recordMutation("QA-RESEARCH-USD", account, com.tradingglobal3d.core.economy.LedgerEntryType.DEPOSIT,
            com.tradingglobal3d.core.model.Money.of(1000000, com.tradingglobal3d.core.model.Currency.USD), true,
            game.getGameTimestamp(), "QA funding");
        assertTrue(game.subscribeTerminal(com.tradingglobal3d.core.research.TerminalTier.PRO));
        long expiry = game.getResearchService().getSubscriptionExpiresAt();
        com.tradingglobal3d.core.model.Money paidBalance = account.getTotalCash(com.tradingglobal3d.core.model.Currency.USD);
        assertTrue(game.cancelTerminalAutoRenewal());
        assertTrue(game.getResearchService().hasAccess(com.tradingglobal3d.core.research.TerminalTier.PRO, game.getGameTimestamp()));
        org.json.JSONObject result = new org.json.JSONObject(new com.tradingglobal3d.mobile.TradingWebBridge(game).resumeTerminalAutoRenewal());
        assertTrue(result.getBoolean("success"));
        assertEquals(expiry, game.getResearchService().getSubscriptionExpiresAt());
        assertEquals(paidBalance, account.getTotalCash(com.tradingglobal3d.core.model.Currency.USD));
        com.tradingglobal3d.core.facade.TradingFacade restored = new com.tradingglobal3d.core.facade.TradingFacade(game.getSaveManager());
        restored.setCatalog(com.tradingglobal3d.core.market.InstrumentCatalog.fromJsonString(java.nio.file.Files.readString(
            java.nio.file.Path.of(System.getProperty("annotation.root"), "android/app/src/main/assets/data/market_universe.json"))));
        assertTrue(restored.startOrResume());
        assertTrue(restored.getResearchService().isAutoRenewalEnabled());
        assertEquals(expiry, restored.getResearchService().getSubscriptionExpiresAt());
        assertEquals(paidBalance, restored.getPersonalAccount().getTotalCash(com.tradingglobal3d.core.model.Currency.USD));
        assertFalse(restored.getResearchService().resumeAutoRenewal(expiry));
        assertTrue(restored.reconcileAll());
    }
    @Test public void leverageCapMeansTotalBuyingPowerAndNeverExceedsThreeTimesEquity() throws Exception {
        for (String rank : new String[]{"PORTFOLIO_MANAGER", "CHIEF_INVESTMENT_OFFICER"}) {
            com.tradingglobal3d.core.facade.TradingFacade game = gameAt(LocalTime.NOON);
            org.json.JSONObject career = game.getCareerManager().exportState();
            career.put("rank", rank); game.getCareerManager().restoreState(career);
            com.tradingglobal3d.core.model.Money equity = game.getPersonalNetWorth();
            int totalMultiplier = rank.equals("CHIEF_INVESTMENT_OFFICER") ? 3 : 2;
            com.tradingglobal3d.core.model.Money loan = equity.multiply(totalMultiplier - 1);
            game.takeMarginLoan(loan);
            com.tradingglobal3d.core.model.Account account = game.getPersonalAccount();
            assertEquals(equity.multiply(totalMultiplier), account.getTotalCash(com.tradingglobal3d.core.model.Currency.IDR));
            assertEquals(equity, game.getPersonalNetWorth());
            try { game.takeMarginLoan(com.tradingglobal3d.core.model.Money.of(1, com.tradingglobal3d.core.model.Currency.IDR)); fail("Debt above total buying-power cap must fail"); }
            catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("Plafon pinjaman")); }
            assertEquals(loan, game.getRiskService().getMarginDebt(account.getId(), com.tradingglobal3d.core.model.Currency.IDR));
            assertTrue(game.reconcileAll());
        }
        assertEquals(new BigDecimal("0.30"), RiskComplianceService.MAINTENANCE_MARGIN_RATIO);
    }
    @Test public void sparklineUsesOnlyTodaysObservedTradesAndBoundedBuckets() throws Exception {
        com.tradingglobal3d.core.facade.TradingFacade game = gameAt(LocalTime.NOON);
        com.tradingglobal3d.core.market.Instrument stock = game.getCatalog().getById("IDX:BBCA");
        long now = game.getClock().getGameTimestamp(), start = now - 12L * 3600000L;
        com.tradingglobal3d.core.trading.OrderBook book = game.getMarketEngine().getOrCreateBook(stock);
        long[] times = {start-1, start+60000, start+120000, start+3600000, now+1};
        for (int i = 0; i < times.length; i++) book.recordFill(new com.tradingglobal3d.core.trading.TradeFill(
            "SPARK-QA-"+i, "BUY-"+i, "SELL-"+i, "NPC-A", "NPC-B", stock.getId(),
            com.tradingglobal3d.core.model.Money.of(10000+i*25, stock.getCurrency()), 100, times[i]));
        org.json.JSONArray points = game.getIntradaySparkline(stock);
        assertEquals(3, points.length());
        assertEquals(start, points.getJSONObject(0).getLong("timestamp"));
        assertEquals(start+120000, points.getJSONObject(1).getLong("timestamp"));
        assertEquals(10050, points.getJSONObject(1).getInt("price"));
        for (int i=0;i<points.length();i++) {
            assertTrue(points.getJSONObject(i).getLong("timestamp") >= start);
            assertTrue(points.getJSONObject(i).getLong("timestamp") <= now);
        }
    }
    @Test public void ipoWatchPersistsAcrossSaveAndReloadAndRejectsUnknownId() throws Exception {
        com.tradingglobal3d.core.facade.TradingFacade game = gameAt(LocalTime.NOON);
        String id = game.getCorporateService().getIpoOfferings().iterator().next().getId();
        assertTrue(game.toggleIpoWatch(id));
        game.save();
        com.tradingglobal3d.core.facade.TradingFacade restored = new com.tradingglobal3d.core.facade.TradingFacade(game.getSaveManager());
        restored.setCatalog(com.tradingglobal3d.core.market.InstrumentCatalog.fromJsonString(java.nio.file.Files.readString(
            java.nio.file.Path.of(System.getProperty("annotation.root"), "android/app/src/main/assets/data/market_universe.json"))));
        assertTrue(restored.startOrResume());
        assertTrue(restored.getCorporateService().isIpoWatched(id));
        assertFalse(restored.toggleIpoWatch(id));
        try { restored.toggleIpoWatch("NONEXISTENT-IPO"); fail("Unknown IPO must fail"); }
        catch (IllegalArgumentException expected) { }
    }
    @Test public void paidCatalystIsFilteredWhenOfferedAndRead() throws Exception {
        com.tradingglobal3d.core.facade.TradingFacade game = gameAt(LocalTime.NOON);
        com.tradingglobal3d.core.research.ResearchService research = game.getResearchService();
        com.tradingglobal3d.core.market.Instrument locked = game.getCatalog().getById("US:AAPL");
        long now = game.getClock().getGameTimestamp();
        research.addNews(new com.tradingglobal3d.core.research.NewsArticle("ANNOTATION-LOCKED", "Locked QA", "QA",
            com.tradingglobal3d.core.research.NewsArticle.Category.COMPANY, locked.getTicker(), 0.03, now));
        research.registerNewsTarget(new com.tradingglobal3d.core.research.NewsPriceTarget("ANNOTATION-TARGET", "ANNOTATION-LOCKED",
            locked.getTicker(), com.tradingglobal3d.core.research.TerminalTier.PRO, "QA", false, .03,
            locked.getPreviousClosePrice(), locked, game.getClock().getCurrentDate()));
        assertFalse(game.getNewsFeedJson().contains("ANNOTATION-LOCKED"));
        assertFalse(game.getNewsFeed().stream().anyMatch(article -> article.getId().equals("ANNOTATION-LOCKED")));
        research.setDynamicDailyNewsEnabled(true);
        research.ensureDailyNewsSchedule(game.getClock().getCurrentDate(), game.getCatalog(), now);
        for (com.tradingglobal3d.core.research.NewsPriceTarget target : research.getNewsTargets()) {
            if (target.getId().equals("ANNOTATION-TARGET")) continue;
            com.tradingglobal3d.core.market.Instrument stock = game.getCatalog().getById("IDX:" + target.getTicker());
            assertNotNull(stock);
            assertTrue(game.isSectorUnlocked(stock.getSector(), stock.getExchange()));
        }
    }
    @Test public void radarCountsOnlyTradableStocksUnlockedForPlayer() throws Exception {
        com.tradingglobal3d.core.facade.TradingFacade game = gameAt(LocalTime.NOON);
        org.json.JSONObject radar = new org.json.JSONObject(
            new com.tradingglobal3d.mobile.TradingWebBridge(game).getFundamentalRadar());
        int eligible = 0, discounted = 0;
        for (com.tradingglobal3d.core.market.Instrument stock : game.getCatalog().getAllInstruments()) {
            if (!stock.isTradable() || !game.isSectorUnlocked(stock.getSector(), stock.getExchange())) continue;
            eligible++;
            if (game.getResearchService().calculateGapRate(stock) > 0) discounted++;
        }
        assertEquals(eligible, radar.getInt("totalEligible"));
        assertEquals(discounted, radar.getInt("totalUndervalued"));
        org.json.JSONArray items = radar.getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {
            com.tradingglobal3d.core.market.Instrument stock = game.getCatalog().getById(items.getJSONObject(i).getString("id"));
            assertTrue(game.isSectorUnlocked(stock.getSector(), stock.getExchange()));
            assertTrue(game.getResearchService().calculateGapRate(stock) > 0);
        }
        for (var stock : game.getCatalog().getAllInstruments()) {
            stock.restoreFundamentalState(stock.getCurrentPrice().getAmount().multiply(new BigDecimal("0.8")), null, 0);
        }
        var bridge = new com.tradingglobal3d.mobile.TradingWebBridge(game);
        var noDiscounts = new org.json.JSONObject(bridge.getFundamentalRadar());
        assertEquals(eligible, noDiscounts.getInt("totalEligible"));
        assertEquals(0, noDiscounts.getInt("totalUndervalued"));
        assertEquals("A discount radar must be empty when no stock is below fair value", 0, noDiscounts.getJSONArray("items").length());
        var cheap = game.getCatalog().getById("IDX:BBCA");
        cheap.restoreFundamentalState(cheap.getCurrentPrice().getAmount().multiply(new BigDecimal("1.2")), null, 0);
        var oneDiscount = new org.json.JSONObject(bridge.getFundamentalRadar());
        assertEquals(1, oneDiscount.getInt("totalUndervalued"));
        assertEquals(1, oneDiscount.getJSONArray("items").length());
        assertEquals(cheap.getId(), oneDiscount.getJSONArray("items").getJSONObject(0).getString("id"));
    }

    @Test public void fundCutoffStillSettlesAt1600WithoutResettingStockReference() throws Exception {
        com.tradingglobal3d.core.facade.TradingFacade game = gameAt(LocalTime.of(15, 59));
        com.tradingglobal3d.core.funds.MutualFund fund = game.getMutualFundManager().getFund("FUND-EQ-01");
        fund.submitSubscription("ANNOTATION-FUND-CUTOFF", game.getPersonalAccount(),
            com.tradingglobal3d.core.model.Money.of(1000000L, com.tradingglobal3d.core.model.Currency.IDR),
            game.getLedger(), game.getClock().getGameTimestamp());
        com.tradingglobal3d.core.market.Instrument idx = game.getCatalog().getById("IDX:BBCA");
        com.tradingglobal3d.core.model.Money previous = idx.getPreviousClosePrice();
        game.advanceWorldSecondsForTest(120);
        assertEquals(previous, idx.getPreviousClosePrice());
        assertEquals("SETTLED", fund.toJsonObject(game.getPersonalAccount().getId()).getJSONArray("orders").getJSONObject(0).getString("status"));
        assertEquals(0, game.getCareerManager().getSessionReports().size());
    }
    private com.tradingglobal3d.core.facade.TradingFacade gameAt(LocalTime time) throws Exception {
        java.nio.file.Path save = java.nio.file.Files.createTempDirectory(java.nio.file.Path.of("."), "qa-save-cutoff-");
        com.tradingglobal3d.core.facade.TradingFacade game = new com.tradingglobal3d.core.facade.TradingFacade(
            com.tradingglobal3d.core.storage.SaveManager.forDirectory(save.toFile()));
        java.nio.file.Path assets = java.nio.file.Path.of(System.getProperty("annotation.root"), "android/app/src/main/assets/data/market_universe.json");
        game.setCatalog(com.tradingglobal3d.core.market.InstrumentCatalog.fromJsonString(java.nio.file.Files.readString(assets)));
        game.initializeNewGame(com.tradingglobal3d.core.model.GameMode.INTERNAL_MARKET, new BigDecimal("50000000"),
            new GameClock(LocalDate.of(2026, 10, 19), time));
        return game;
    }

    @Test public void oldUsClosingTimeDoesNotResetDailyReference() throws Exception {
        com.tradingglobal3d.core.facade.TradingFacade game = gameAt(LocalTime.of(3, 59));
        com.tradingglobal3d.core.market.Instrument us = game.getCatalog().getById("US:AAPL");
        com.tradingglobal3d.core.model.Money previous = us.getPreviousClosePrice();
        us.updatePrice(new com.tradingglobal3d.core.model.Money(previous.getAmount().multiply(new BigDecimal("1.01")), us.getCurrency()), 0);
        game.advanceWorldSecondsForTest(120);
        assertEquals(previous, us.getPreviousClosePrice());
    }

    @Test public void midnightResetsBothExchangesAndProducesOneDailyReport() throws Exception {
        com.tradingglobal3d.core.facade.TradingFacade game = gameAt(LocalTime.of(23, 59, 59));
        com.tradingglobal3d.core.market.Instrument us = game.getCatalog().getById("US:AAPL");
        us.updatePrice(new com.tradingglobal3d.core.model.Money(us.getCurrentPrice().getAmount().multiply(new BigDecimal("1.01")), us.getCurrency()), 0);
        com.tradingglobal3d.core.model.Money closing = us.getCurrentPrice();
        game.advanceWorldSecondsForTest(1);
        assertEquals(LocalTime.MIDNIGHT, game.getClock().getCurrentTime());
        assertEquals(closing, us.getPreviousClosePrice());
        assertEquals(1, game.getCareerManager().getSessionReports().size());
        game.advanceWorldSecondsForTest(1);
        assertEquals(1, game.getCareerManager().getSessionReports().size());
    }
    @Test public void bothExchangesTradeEveryMinuteAndMidnightAdvancesCalendar() {
        GameClock clock = new GameClock(LocalDate.of(2026, 10, 19), LocalTime.MIDNIGHT);
        for (int minute = 0; minute < 1440; minute++) {
            for (MarketExchange exchange : MarketExchange.values())
                assertTrue(exchange + " closed at " + clock.getCurrentTime(), clock.isTradingOpenFor(exchange));
            clock.advanceGameSeconds(60);
        }
        assertEquals(LocalDate.of(2026, 10, 20), clock.getCurrentDate());
        assertEquals("20 Oktober 2026", clock.getFormattedDate());
        assertEquals(86400000L, GameClock.convertRealMsToGameMs(1800000L, false, 1));
    }

    @Test public void financingRateMatchesChosenDailyRate() {
        assertEquals(0, RiskComplianceService.ANNUAL_MARGIN_LOAN_INTEREST_RATE
            .divide(BigDecimal.valueOf(360)).compareTo(new BigDecimal("0.0002")));
    }

    @Test public void cashPaidInterestCannotBeChargedAgainAfterReloadOnTheSameDate() throws Exception {
        RiskComplianceService risk = new RiskComplianceService();
        var account = new com.tradingglobal3d.core.model.Account("PAID-INTEREST-QA", "QA",
            com.tradingglobal3d.core.model.AccountType.PERSONAL, null);
        var ledger = new com.tradingglobal3d.core.economy.EconomyLedger();
        var principal = com.tradingglobal3d.core.model.Money.of(40000000L, com.tradingglobal3d.core.model.Currency.IDR);
        risk.takeMarginLoan(account, principal, ledger, 1000L);
        var catalog = new com.tradingglobal3d.core.market.InstrumentCatalog();
        risk.accrueDailyFinancingCosts(account, catalog, ledger, BigDecimal.valueOf(15800), 86400000L);
        assertEquals(0, account.getTotalCash(principal.getCurrency()).getAmount().compareTo(new BigDecimal("39992000")));
        int entries = ledger.getEntries().size();
        var restored = new RiskComplianceService();
        restored.restoreState(risk.exportState());
        restored.accrueDailyFinancingCosts(account, catalog, ledger, BigDecimal.valueOf(15800), 86401000L);
        assertEquals(entries, ledger.getEntries().size());
        assertEquals(principal, restored.getMarginDebt(account.getId(), principal.getCurrency()));
        restored.accrueDailyFinancingCosts(account, catalog, ledger, BigDecimal.valueOf(15800), 2 * 86400000L);
        assertEquals(0, account.getTotalCash(principal.getCurrency()).getAmount().compareTo(new BigDecimal("39984000")));
        assertTrue(ledger.reconcile(account, principal.getCurrency()));
    }

    @Test public void unpaidInterestCapitalizesUsingIdrWholeRupiahRounding() throws Exception {
        RiskComplianceService risk = new RiskComplianceService();
        com.tradingglobal3d.core.model.Account account = new com.tradingglobal3d.core.model.Account(
            "ANNOTATION-QA", "QA", com.tradingglobal3d.core.model.AccountType.PERSONAL, null);
        com.tradingglobal3d.core.economy.EconomyLedger ledger = new com.tradingglobal3d.core.economy.EconomyLedger();
        com.tradingglobal3d.core.model.Money principal = com.tradingglobal3d.core.model.Money.of(
            40000000L, com.tradingglobal3d.core.model.Currency.IDR);
        risk.takeMarginLoan(account, principal, ledger, 1000L);
        account.debitCash(principal);
        var usdPrincipal = com.tradingglobal3d.core.model.Money.of(10000, com.tradingglobal3d.core.model.Currency.USD);
        risk.takeMarginLoan(account, usdPrincipal, ledger, 1000L);
        account.debitCash(usdPrincipal);
        for (int day = 1; day <= 3; day++) {
            risk.accrueDailyFinancingCosts(account, new com.tradingglobal3d.core.market.InstrumentCatalog(),
                ledger, BigDecimal.valueOf(15800), day * 86400000L);
            var debt = risk.getMarginDebt(account.getId(), com.tradingglobal3d.core.model.Currency.IDR);
            var usdDebt = risk.getMarginDebt(account.getId(), com.tradingglobal3d.core.model.Currency.USD);
            var restored = new RiskComplianceService();
            restored.restoreState(risk.exportState());
            restored.accrueDailyFinancingCosts(account, new com.tradingglobal3d.core.market.InstrumentCatalog(),
                ledger, BigDecimal.valueOf(15800), day * 86400000L + 1000);
            assertEquals("Interest cannot capitalize twice on the same game date after reload", debt,
                restored.getMarginDebt(account.getId(), com.tradingglobal3d.core.model.Currency.IDR));
            assertEquals(usdDebt, restored.getMarginDebt(account.getId(), com.tradingglobal3d.core.model.Currency.USD));
            risk = restored;
        }
        assertEquals(0, risk.getMarginDebt(account.getId(), com.tradingglobal3d.core.model.Currency.IDR)
            .getAmount().compareTo(new BigDecimal("40024005")));
        assertEquals(0, risk.getMarginDebt(account.getId(), com.tradingglobal3d.core.model.Currency.USD)
            .getAmount().compareTo(new BigDecimal("10006.00")));
    }
}
