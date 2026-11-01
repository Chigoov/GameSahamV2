package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.npc.NpcAgent;
import com.tradingglobal3d.core.npc.NpcStrategyType;
import com.tradingglobal3d.core.trading.*;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONObject;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class AuditRemediationRegressionTest {
    private TradingFacade game(SaveManager manager) {
        TradingFacade game = new TradingFacade(manager);
        game.setRealTimeClock(java.time.Clock.fixed(java.time.Instant.parse("2026-11-01T00:00:00Z"), java.time.ZoneOffset.UTC));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));
        return game;
    }

    private String invalidDomain(String content) throws Exception {
        JSONObject root = new JSONObject(content);
        root.getJSONObject("runtimeState").put("strategyLabs", new org.json.JSONArray().put(new JSONObject()));
        return root.toString();
    }

    @Test public void domainInvalidPrimaryNeverReplacesHealthyBackup() throws Exception {
        Path directory = Files.createTempDirectory("qa-audit-save-");
        SaveManager manager = SaveManager.forDirectory(directory.toFile());
        TradingFacade game = game(manager);
        game.save();
        game.save();
        byte[] backup = Files.readAllBytes(manager.getBackupFile().toPath());
        Files.writeString(manager.getSaveFile().toPath(), invalidDomain(Files.readString(manager.getSaveFile().toPath())));
        assertEquals(SaveManager.LoadSource.BACKUP_RECOVERED, manager.load().getSource());
        assertFalse(game.load());
        assertTrue(game.hasPendingBackupRecoveryOffer());
        game.confirmBackupRecovery();
        assertArrayEquals(backup, Files.readAllBytes(manager.getBackupFile().toPath()));
        assertEquals(SaveManager.LoadSource.PRIMARY, manager.load().getSource());
    }

    @Test public void domainInvalidBackupIsNotOfferedAndValidationLeavesLiveStateAlone() throws Exception {
        SaveManager manager = SaveManager.forDirectory(Files.createTempDirectory("qa-audit-invalid-").toFile());
        TradingFacade game = game(manager);
        game.save();
        String valid = Files.readString(manager.getSaveFile().toPath());
        String broken = invalidDomain(valid);
        JSONObject before = new JSONObject(game.getSnapshotJson());
        before.getJSONObject("clock").remove("monotonicAnchor");
        try { manager.readValidatedState(broken); fail("Invalid graph accepted"); }
        catch (IllegalArgumentException expected) { }
        JSONObject after = new JSONObject(game.getSnapshotJson());
        after.getJSONObject("clock").remove("monotonicAnchor");
        assertTrue(before.similar(after));
        Files.writeString(manager.getSaveFile().toPath(), broken);
        Files.writeString(manager.getBackupFile().toPath(), broken);
        assertFalse(game.load());
        assertFalse(game.hasPendingBackupRecoveryOffer());
        assertEquals(SaveManager.LoadSource.CORRUPTED_ALL, manager.load().getSource());
    }

    @Test public void failedWritePreservesMemoryBlocksNewCommandsAndRetryUnlocks() throws Exception {
        SaveManager manager = SaveManager.forDirectory(Files.createTempDirectory("qa-audit-write-").toFile());
        TradingFacade game = game(manager);
        game.save();
        byte[] primary = Files.readAllBytes(manager.getSaveFile().toPath());
        Files.createDirectory(manager.getTempFile().toPath());
        try { game.openTimeDeposit("DEP-TENOR-10", new BigDecimal("10000000")); fail("Write should fail"); }
        catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("Penyimpanan")); }
        assertTrue(game.areFinancialWritesBlocked());
        assertEquals(Money.of(40_000_000, Currency.IDR), game.getPersonalAccount().getTotalCash(Currency.IDR));
        assertEquals(Money.of(10_000_000, Currency.IDR), game.getTimeDepositManager().calculateTotalActivePrincipal());
        int entries = game.getLedger().getEntries().size();
        try { game.openTimeDeposit("DEP-TENOR-10", new BigDecimal("10000000")); fail("New transaction allowed"); }
        catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("dikunci")); }
        try { game.convertFx("QA-FX", game.getPersonalAccount().getId(), Currency.IDR, Currency.USD, Money.of(100000, Currency.IDR)); fail("FX allowed"); }
        catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("dikunci")); }
        assertEquals(entries, game.getLedger().getEntries().size());
        long timestamp = game.getGameTimestamp();
        game.advanceRealMs(1000, true);
        assertEquals(timestamp + 48000, game.getGameTimestamp());
        assertArrayEquals(primary, Files.readAllBytes(manager.getSaveFile().toPath()));
        Files.delete(manager.getTempFile().toPath()); // Only the empty fault-injection directory owned by this test.
        game.save();
        assertFalse(game.areFinancialWritesBlocked());
        assertTrue(game.load());
        assertEquals(Money.of(40_000_000, Currency.IDR), game.getPersonalAccount().getTotalCash(Currency.IDR));
        assertEquals(Money.of(10_000_000, Currency.IDR), game.getTimeDepositManager().calculateTotalActivePrincipal());
    }

    @Test public void liquidityTopUpCountsFreeSharesAndReconcilesExternalCapital() {
        Instrument instrument = new Instrument("IDX:QA", "QA", "QA sintetis", MarketExchange.IDX,
                "BANK", Currency.IDR, 100, 10_000_000L, true, Money.of(1000, Currency.IDR));
        Account mm = new Account("ACC-NPC-MM-01", "QA MM", AccountType.FIRM, null);
        mm.addPosition(new Position("QA", "IDX", Currency.IDR, 200_000L, new BigDecimal("1000"), new BigDecimal("1000")));
        OrderBook book = new OrderBook(instrument.getId());
        book.addLimitOrder(new Order("Q-ASK", "Q-ASK", mm.getId(), instrument.getId(), OrderSide.SELL,
                OrderType.LIMIT, Money.of(1020, Currency.IDR), null, 198_000L, 1L, null));
        EconomyLedger ledger = new EconomyLedger();
        ledger.recordMutation("QA-SEED", mm, LedgerEntryType.INITIAL_CAPITAL, Money.of(4_000_000_000L, Currency.IDR), true, 1L, "QA sintetis");
        NpcAgent agent = new NpcAgent("QA-MM", "QA MM", NpcStrategyType.MARKET_MAKER, mm);
        agent.ensureLiquidity(instrument, book, ledger, 2L, 0, Money.zero(Currency.IDR), "QA-BASE");
        assertEquals(398_000L, mm.getPosition(instrument.getId()).getQuantity());
        assertEquals(10_198_000L, instrument.getIssuedShares());
        assertEquals(Money.of(10_000_000_000L, Currency.IDR), mm.getAvailableCash(Currency.IDR));
        assertTrue(ledger.reconcile(mm, Currency.IDR));
        assertEquals(2, ledger.getEntries().size());
        agent.ensureLiquidity(instrument, book, ledger, 2L, 0, Money.zero(Currency.IDR), "QA-BASE");
        assertEquals(398_000L, mm.getPosition(instrument.getId()).getQuantity());
        assertEquals(10_198_000L, instrument.getIssuedShares());
        assertEquals(2, ledger.getEntries().size());
    }

    @Test public void freeInventoryThresholdAndCancelledCommitmentsDoNotOverIssue() {
        for (long freeLots : new long[]{49, 50}) {
            Instrument instrument = new Instrument("IDX:QA", "QA", "QA sintetis", MarketExchange.IDX,
                    "BANK", Currency.IDR, 100, 10_000_000L, true, Money.of(1000, Currency.IDR));
            Account mm = new Account("ACC-NPC-MM-01", "QA MM", AccountType.FIRM, null);
            mm.addPosition(new Position("QA", "IDX", Currency.IDR, 200_000L, new BigDecimal("1000"), new BigDecimal("1000")));
            OrderBook book = new OrderBook(instrument.getId());
            Order first = Order.createLimit("QA-ASK-1", instrument.getId(), mm.getId(), OrderSide.SELL,
                    Money.of(1020, Currency.IDR), 100_000L, 1L);
            book.addLimitOrder(first);
            book.addLimitOrder(Order.createLimit("QA-ASK-2", instrument.getId(), mm.getId(), OrderSide.SELL,
                    Money.of(1020, Currency.IDR), (1000 - freeLots) * 100, 1L));
            EconomyLedger ledger = new EconomyLedger();
            NpcAgent agent = new NpcAgent("QA-MM", "QA MM", NpcStrategyType.MARKET_MAKER, mm);
            agent.ensureLiquidity(instrument, book, ledger, 2L, 0, Money.zero(Currency.IDR), "QA-THRESHOLD");
            long expectedIssued = freeLots < 50 ? (2000 - freeLots) * 100 : 0;
            assertEquals(200_000L + expectedIssued, mm.getPosition(instrument.getId()).getQuantity());
            assertEquals(10_000_000L + expectedIssued, instrument.getIssuedShares());
            first.cancel();
            agent.ensureLiquidity(instrument, book, ledger, 3L, 0, Money.zero(Currency.IDR), "QA-AFTER-CANCEL");
            assertEquals(200_000L + expectedIssued, mm.getPosition(instrument.getId()).getQuantity());
            assertEquals(10_000_000L + expectedIssued, instrument.getIssuedShares());
            assertTrue(ledger.reconcile(mm, Currency.IDR));
        }
    }

    private TradingFacade tradingGame(BigDecimal capital) throws Exception {
        TradingFacade game = game(SaveManager.forDirectory(Files.createTempDirectory("qa-audit-trade-").toFile()));
        com.tradingglobal3d.core.market.InstrumentCatalog catalog = new com.tradingglobal3d.core.market.InstrumentCatalog();
        catalog.registerInstrument(new Instrument("IDX:BBCA", "BBCA", "QA sintetis", MarketExchange.IDX,
                "BANK", Currency.IDR, 100, 100_000_000L, true, Money.of(10000, Currency.IDR)));
        catalog.registerInstrument(new Instrument("US:AAPL", "AAPL", "QA sintetis US", MarketExchange.US,
                "TECH", Currency.USD, 1, 100_000_000L, true, Money.of(100, Currency.USD)));
        game.setCatalog(catalog);
        game.initializeNewGame(GameMode.INTERNAL_MARKET, capital);
        GameTestSupport.unlockIdxLevelFourForStressFixture(game);
        game.getClock().advanceGameSeconds(Math.floorMod(36000 - game.getClock().getCurrentTime().toSecondOfDay(), 86400));
        return game;
    }

    private Order order(TradingFacade game, String id, OrderSide side, OrderType type, long quantity, long price) {
        return new Order(id, id, game.getPersonalAccount().getId(), "IDX:BBCA", side, type,
                type == OrderType.LIMIT ? Money.of(price, Currency.IDR) : null,
                null, quantity, game.getGameTimestamp(), null);
    }

    @Test public void rejectedUnaffordableOrdersNeverMintInventoryOrCash() throws Exception {
        for (OrderType type : new OrderType[]{OrderType.LIMIT, OrderType.MARKET}) {
            TradingFacade game = tradingGame(new BigDecimal("50000000"));
            Instrument inst = game.getCatalog().getById("IDX:BBCA");
            Account mm = game.getAccount("ACC-NPC-MM-01");
            long supply = inst.getIssuedShares(), shares = mm.getPosition(inst.getId()).getQuantity();
            Money cash = mm.getTotalCash(Currency.IDR);
            MatchingEngine.OrderResult result = game.submitOrder(order(game, "QA-POOR-" + type, OrderSide.BUY, type, 1_000_000, 10025));
            assertEquals(0, result.order.getFilledQuantity());
            assertEquals(supply, inst.getIssuedShares());
            assertEquals(shares, mm.getPosition(inst.getId()).getQuantity());
            assertEquals(cash, mm.getTotalCash(Currency.IDR));
            Order invalidSell = order(game, "QA-NO-SHARES-" + type, OrderSide.SELL, type, 1_000_000, 9975);
            assertFalse(game.previewOrder(invalidSell).getBoolean("success"));
            assertFalse(game.submitOrder(invalidSell).isSuccess);
            assertEquals(supply, inst.getIssuedShares());
            assertEquals(shares, mm.getPosition(inst.getId()).getQuantity());
            assertEquals(cash, mm.getTotalCash(Currency.IDR));
        }
    }

    @Test public void playerLimitDoesNotSetMarketMakerPrice() throws Exception {
        TradingFacade game = tradingGame(new BigDecimal("100000000000"));
        Instrument inst = game.getCatalog().getById("IDX:BBCA");
        long supply = inst.getIssuedShares();
        MatchingEngine.OrderResult result = game.submitOrder(order(game, "QA-CHEAP", OrderSide.BUY, OrderType.LIMIT, 1_000_000, 9500));
        assertEquals(OrderStatus.OPEN, result.order.getStatus());
        assertEquals(0, result.order.getFilledQuantity());
        assertEquals(supply, inst.getIssuedShares());
        assertTrue(game.getMarketEngine().getBook(inst.getId()).getAsks().isEmpty());
    }

    @Test public void largeBuyAndSellFillEntirelyAtMarketQuotesAndReconcile() throws Exception {
        for (String instrumentId : new String[]{"IDX:BBCA", "US:AAPL"})
        for (OrderType type : new OrderType[]{OrderType.MARKET, OrderType.LIMIT})
        for (long lots : new long[]{10000, 50000}) for (OrderSide side : OrderSide.values()) {
            TradingFacade game = tradingGame(new BigDecimal("100000000000"));
            Instrument inst = game.getCatalog().getById(instrumentId);
            if (inst.getExchange() == MarketExchange.US) {
                game.getProgressManager().awardMissionXp("QA-US-LEVEL", 1350, game.getGameTimestamp());
                game.getClock().advanceGameSeconds(11 * 3600 + 30 * 60);
                game.getLedger().recordMutation("QA-USD", game.getPersonalAccount(), LedgerEntryType.INITIAL_CAPITAL,
                        Money.of(20_000_000, Currency.USD), true, game.getGameTimestamp(), "QA sintetis US");
            }
            long quantity = lots * inst.getLotSize();
            if (side == OrderSide.SELL) game.getPersonalAccount().addPosition(new Position(inst.getTicker(), inst.getExchange().name(), inst.getCurrency(),
                    quantity, inst.getCurrentPrice().getAmount(), inst.getCurrentPrice().getAmount()));
            Money price = NpcAgent.marketMakerQuotePrice(inst, game.getMarketEngine().getOrCreateBook(inst), side == OrderSide.SELL);
            String id = "QA-LARGE-" + instrumentId + lots + side + type;
            Order request = new Order(id, id, game.getPersonalAccount().getId(), inst.getId(), side, type,
                    type == OrderType.LIMIT ? price : null, null, quantity, game.getGameTimestamp(), null);
            JSONObject preview = game.previewOrder(request);
            assertTrue(preview.toString(), preview.getBoolean("success"));
            assertEquals(quantity, preview.getLong("estimatedFillQuantity"));
            MatchingEngine.OrderResult result = game.submitOrder(request);
            assertEquals(id + ": " + result.message, OrderStatus.FILLED, result.order.getStatus());
            assertEquals(quantity, result.order.getFilledQuantity());
            assertEquals(price, result.fills.get(0).getPrice());
            Account mm = game.getAccount("ACC-NPC-MM-01");
            assertTrue(game.getLedger().reconcile(game.getPersonalAccount(), inst.getCurrency()));
            assertTrue(game.getLedger().reconcile(mm, inst.getCurrency()));
            assertEquals(Money.zero(inst.getCurrency()), game.getPersonalAccount().getReservedCash(inst.getCurrency()));
            long issued = inst.getIssuedShares();
            Money mmCash = mm.getTotalCash(inst.getCurrency());
            int entries = game.getLedger().getEntries().size();
            game.save();
            assertTrue(game.load());
            assertEquals(issued, game.getCatalog().getById(instrumentId).getIssuedShares());
            assertEquals(mmCash, game.getAccount("ACC-NPC-MM-01").getTotalCash(inst.getCurrency()));
            assertEquals(OrderStatus.FILLED, game.submitOrder(request).order.getStatus());
            assertEquals(entries, game.getLedger().getEntries().size());
        }
    }

    @Test public void shareCountsBeyondDoublePrecisionUseExactSettlement() {
        long quantity = 36_028_797_018_964_100L;
        assertNotEquals(BigDecimal.valueOf(quantity), BigDecimal.valueOf((double) quantity));
        Instrument inst = new Instrument("IDX:EXACT", "EXACT", "QA sintetis", MarketExchange.IDX,
                "BANK", Currency.IDR, 100, Long.MAX_VALUE, true, Money.of(5000, Currency.IDR));
        Account buyer = new Account("QA-BUY", "QA", AccountType.PERSONAL, null);
        Account seller = new Account("ACC-NPC-SELL", "QA", AccountType.FIRM, null);
        seller.addPosition(new Position("EXACT", "IDX", Currency.IDR, quantity, new BigDecimal("5000"), new BigDecimal("5000")));
        BigDecimal nominal = new BigDecimal("5000").multiply(BigDecimal.valueOf(quantity));
        Money fee = new Money(nominal.multiply(MatchingEngine.BUY_FEE_RATE), Currency.IDR);
        EconomyLedger ledger = new EconomyLedger();
        ledger.recordMutation("QA-EXACT-CAPITAL", buyer, LedgerEntryType.INITIAL_CAPITAL,
                new Money(nominal, Currency.IDR).add(fee), true, 1L, "QA sintetis");
        OrderBook book = new OrderBook(inst.getId());
        book.addLimitOrder(new Order("QA-MAKER", "QA-MAKER", seller.getId(), inst.getId(), OrderSide.SELL,
                OrderType.LIMIT, Money.of(5000, Currency.IDR), null, quantity, 1L, null));
        Order incoming = new Order("QA-EXACT", "QA-EXACT", buyer.getId(), inst.getId(), OrderSide.BUY,
                OrderType.MARKET, null, null, quantity, 2L, null);
        MatchingEngine.OrderResult result = new MatchingEngine().submitOrder(incoming, buyer, inst, book, ledger,
                java.util.Map.of(buyer.getId(), buyer, seller.getId(), seller), 2L);
        assertEquals(result.message, OrderStatus.FILLED, incoming.getStatus());
        assertEquals(quantity, buyer.getPosition(inst.getId()).getQuantity());
        assertEquals(Money.zero(Currency.IDR), buyer.getTotalCash(Currency.IDR));
        assertTrue(ledger.reconcile(buyer, Currency.IDR));
    }

    @Test public void closedMarketQueueSurvivesReloadAndWaitsForItsOwnOpening() throws Exception {
        TradingFacade game = tradingGame(new BigDecimal("100000000000"));
        game.getClock().advanceGameSeconds(6 * 3600);
        game.getClock().setForeground(false);
        Order request = order(game, "QA-OPENING", OrderSide.BUY, OrderType.MARKET, 10000L, 0);
        assertTrue(game.submitOrder(request).isSuccess);
        assertTrue(request.isAwaitingMarketOpen());
        assertNull(request.getLimitPrice());
        assertTrue(game.getMarketEngine().getBook("IDX:BBCA").getBids().isEmpty());
        game.advanceRealMs(5 * 3600_000L / 48 + 30 * 60_000L / 48, false);
        assertEquals(java.time.LocalTime.of(21, 30), game.getClock().getCurrentTime());
        assertEquals(0, request.getFilledQuantity());
        game.getCatalog().getById("IDX:BBCA").updatePrice(Money.of(11000, Currency.IDR), 0);
        game.save();
        assertTrue(game.load());
        Order restored = game.getOrderManager().getOrderByRequest(request.getRequestId());
        assertTrue(restored.isAwaitingMarketOpen());
        game.advanceRealMs((11 * 3600_000L + 30 * 60_000L) / 48, false);
        assertEquals(java.time.LocalTime.of(9, 0), game.getClock().getCurrentTime());
        assertEquals(OrderStatus.FILLED, restored.getStatus());
        assertEquals(OrderType.MARKET, restored.getType());
        assertNull(restored.getLimitPrice());
        assertEquals(Money.of(11025, Currency.IDR), game.getMarketEngine().getBook("IDX:BBCA").getTradeHistory().get(0).getPrice());
        assertEquals(Money.zero(Currency.IDR), game.getPersonalAccount().getReservedCash(Currency.IDR));
    }

    @Test public void cancellingOpeningQueueReleasesTheWholeReservation() throws Exception {
        TradingFacade game = tradingGame(new BigDecimal("100000000000"));
        game.getClock().advanceGameSeconds(6 * 3600);
        Order request = order(game, "QA-CANCEL-OPENING", OrderSide.BUY, OrderType.MARKET, 10000L, 0);
        game.submitOrder(request);
        assertTrue(game.getPersonalAccount().getReservedCash(Currency.IDR).isPositive());
        assertTrue(game.cancelOrder(request));
        assertEquals(OrderStatus.CANCELLED, request.getStatus());
        assertEquals(Money.zero(Currency.IDR), game.getPersonalAccount().getReservedCash(Currency.IDR));
        assertFalse(game.cancelOrder(request));
    }

    @Test public void negativeNewsTargetRequiresActualPriceDecline() {
        Instrument inst = new Instrument("IDX:NEWS", "NEWS", "QA sintetis", MarketExchange.IDX,
                "BANK", Currency.IDR, 100, 10_000_000, true, Money.of(9500, Currency.IDR));
        com.tradingglobal3d.core.market.InstrumentCatalog catalog = new com.tradingglobal3d.core.market.InstrumentCatalog();
        catalog.registerInstrument(inst);
        com.tradingglobal3d.core.research.ResearchService research = new com.tradingglobal3d.core.research.ResearchService();
        java.time.LocalDate date = java.time.LocalDate.of(2026, 11, 1);
        com.tradingglobal3d.core.research.NewsPriceTarget target = new com.tradingglobal3d.core.research.NewsPriceTarget(
                "QA-NEGATIVE-TARGET", "QA-NEGATIVE", "NEWS", com.tradingglobal3d.core.research.TerminalTier.PRO,
                "QA sintetis turun", false, -0.03, Money.of(9500, Currency.IDR), inst, date);
        research.registerNewsTarget(target);
        research.evaluateNewsTargets(catalog, date);
        assertEquals(Money.of(9200, Currency.IDR), target.getTargetPrice());
        assertEquals(com.tradingglobal3d.core.research.NewsPriceTarget.TargetStatus.ACTIVE, target.getStatus());
        Account buyer = new Account("ACC-NPC-MM-01", "QA pembeli", AccountType.FIRM, null);
        Account seller = new Account("ACC-LEAGUE-NPC-01", "QA penjual", AccountType.FIRM, null);
        seller.addPosition(new Position("NEWS", "IDX", Currency.IDR, 200, new BigDecimal("9500"), new BigDecimal("9500")));
        EconomyLedger ledger = new EconomyLedger();
        ledger.recordMutation("QA-CATALYST-CASH", buyer, LedgerEntryType.INITIAL_CAPITAL,
                Money.of(100_000_000L, Currency.IDR), true, 1L, "QA sintetis");
        var engine = new com.tradingglobal3d.core.npc.MarketEngine();
        var clock = new com.tradingglobal3d.core.clock.GameClock(date, java.time.LocalTime.of(9, 0));
        engine.tick(clock, clock.getGameTimestamp(), catalog, ledger,
                java.util.Map.of(buyer.getId(), buyer, seller.getId(), seller), research);
        assertEquals(Money.of(9200, Currency.IDR), inst.getCurrentPrice());
        assertEquals(200, buyer.getPosition(inst.getId()).getQuantity());
        assertEquals(0, seller.getPosition(inst.getId()).getQuantity());
        assertTrue(ledger.reconcile(buyer, Currency.IDR));
        assertTrue(ledger.reconcile(seller, Currency.IDR));
        research.evaluateNewsTargets(catalog, date);
        assertEquals(com.tradingglobal3d.core.research.NewsPriceTarget.TargetStatus.ACHIEVED, target.getStatus());
    }

    @Test public void newsEpisodeKeepsFirstReferenceAcrossDaysExpiryAndReload() throws Exception {
        var research = new com.tradingglobal3d.core.research.ResearchService();
        var inst = new Instrument("IDX:NEWS", "NEWS", "QA sintetis", MarketExchange.IDX,
                "BANK", Currency.IDR, 100, 10_000_000, true, Money.of(10000, Currency.IDR));
        var date = java.time.LocalDate.of(2026, 11, 1);
        long gameAt = date.atTime(9, 0).toInstant(java.time.ZoneOffset.ofHours(7)).toEpochMilli();
        long realAt = 1_000_000L;
        research.observeWorldTime(gameAt, realAt, false);
        research.addNews(new com.tradingglobal3d.core.research.NewsArticle("QA-EFFECT-A", "QA", "QA sintetis",
                com.tradingglobal3d.core.research.NewsArticle.Category.COMPANY, "NEWS", 0.05, gameAt, 60));
        research.registerNewsTarget(new com.tradingglobal3d.core.research.NewsPriceTarget("QA-TARGET-A", "QA-EFFECT-A", "NEWS",
                com.tradingglobal3d.core.research.TerminalTier.PRO, "QA", false, 0.05, Money.of(10000, Currency.IDR), inst, date));
        research.addNews(new com.tradingglobal3d.core.research.NewsArticle("QA-EFFECT-B", "QA", "QA sintetis",
                com.tradingglobal3d.core.research.NewsArticle.Category.COMPANY, "NEWS", -0.03, gameAt + 86_400_000L, 120));
        research.registerNewsTarget(new com.tradingglobal3d.core.research.NewsPriceTarget("QA-TARGET-B", "QA-EFFECT-B", "NEWS",
                com.tradingglobal3d.core.research.TerminalTier.PRO, "QA", false, -0.03, Money.of(10500, Currency.IDR), inst, date.plusDays(1)));
        research.observeWorldTime(gameAt + 86_400_000L, realAt + 1_800_000L, false);
        assertEquals(Money.of(10000, Currency.IDR), research.getActiveNewsTarget("NEWS").getReferencePrice());
        assertEquals(Money.of(10200, Currency.IDR), research.getActiveNewsTarget("NEWS").getTargetPrice());
        var restored = new com.tradingglobal3d.core.research.ResearchService();
        restored.restoreState(research.exportState());
        restored.observeWorldTime(gameAt + 61 * 60_000L * 48, realAt + 61 * 60_000L, false);
        assertEquals(Money.of(10000, Currency.IDR), restored.getActiveNewsTarget("NEWS").getReferencePrice());
        assertEquals(Money.of(9700, Currency.IDR), restored.getActiveNewsTarget("NEWS").getTargetPrice());
        restored.observeWorldTime(gameAt + 151 * 60_000L * 48, realAt + 151 * 60_000L, false);
        assertNull(restored.getActiveNewsTarget("NEWS"));
        JSONObject legacy = research.exportState();
        legacy.remove("newsArticles");
        restored.restoreState(legacy);
        restored.observeWorldTime(gameAt + 61 * 60_000L * 48, realAt + 61 * 60_000L, false);
        assertEquals(Money.of(9700, Currency.IDR), restored.getActiveNewsTarget("NEWS").getTargetPrice());
        var legacyReload = new com.tradingglobal3d.core.research.ResearchService();
        legacyReload.restoreState(restored.exportState());
        assertEquals(Money.of(9700, Currency.IDR), legacyReload.getActiveNewsTarget("NEWS").getTargetPrice());
    }

    @Test public void exactlyOffsetNewsHasNoForcedTarget() {
        var research = new com.tradingglobal3d.core.research.ResearchService();
        var inst = new Instrument("IDX:NEWS", "NEWS", "QA sintetis", MarketExchange.IDX,
                "BANK", Currency.IDR, 100, 10_000_000, true, Money.of(9500, Currency.IDR));
        var date = java.time.LocalDate.of(2026, 11, 1);
        for (double percentage : new double[]{0.03, -0.03}) research.registerNewsTarget(
                new com.tradingglobal3d.core.research.NewsPriceTarget("QA-ZERO-" + percentage, "QA-ZERO-NEWS-" + percentage,
                        "NEWS", com.tradingglobal3d.core.research.TerminalTier.PRO, "QA", false,
                        percentage, Money.of(9500, Currency.IDR), inst, date));
        assertFalse(research.hasActiveNewsTarget("NEWS"));
        assertNull(research.getActiveNewsTarget("NEWS"));
    }

    @Test public void newsReferenceFollowsPublicationOrderAndResetsOnlyAfterEpisodeEnds() throws Exception {
        var research = new com.tradingglobal3d.core.research.ResearchService();
        var inst = new Instrument("IDX:NEWS", "NEWS", "QA sintetis", MarketExchange.IDX,
                "BANK", Currency.IDR, 100, 10_000_000, true, Money.of(10000, Currency.IDR));
        var date = java.time.LocalDate.of(2026, 11, 1);
        long firstAt = date.atTime(9, 0).toInstant(java.time.ZoneOffset.ofHours(7)).toEpochMilli();
        research.observeWorldTime(firstAt + 86_400_000L, 1_000_000L, false);
        for (int i = 0; i < 2; i++) research.addNews(new com.tradingglobal3d.core.research.NewsArticle(
                "QA-ORDER-" + i, "QA", "QA sintetis", com.tradingglobal3d.core.research.NewsArticle.Category.COMPANY,
                "NEWS", i == 0 ? 0.05 : -0.03, firstAt + i * 86_400_000L, 60));
        for (int i : new int[]{1, 0}) research.registerNewsTarget(new com.tradingglobal3d.core.research.NewsPriceTarget(
                "QA-TARGET-ORDER-" + i, "QA-ORDER-" + i, "NEWS", com.tradingglobal3d.core.research.TerminalTier.PRO,
                "QA", false, i == 0 ? 0.05 : -0.03, Money.of(i == 0 ? 10000 : 10500, Currency.IDR), inst, date.plusDays(i)));
        assertEquals(Money.of(10000, Currency.IDR), research.getActiveNewsTarget("NEWS").getReferencePrice());
        assertEquals(Money.of(10200, Currency.IDR), research.getActiveNewsTarget("NEWS").getTargetPrice());
        var restored = new com.tradingglobal3d.core.research.ResearchService();
        restored.restoreState(research.exportState());
        assertEquals(Money.of(10200, Currency.IDR), restored.getActiveNewsTarget("NEWS").getTargetPrice());
        long nextAt = firstAt + 86_400_000L + 61 * 60_000L * 48;
        restored.observeWorldTime(nextAt, 1_000_000L + 61 * 60_000L, false);
        assertNull(restored.getActiveNewsTarget("NEWS"));
        restored.addNews(new com.tradingglobal3d.core.research.NewsArticle("QA-NEW-EPISODE", "QA", "QA sintetis",
                com.tradingglobal3d.core.research.NewsArticle.Category.COMPANY, "NEWS", 0.05, nextAt, 60));
        restored.registerNewsTarget(new com.tradingglobal3d.core.research.NewsPriceTarget("QA-NEW-TARGET", "QA-NEW-EPISODE", "NEWS",
                com.tradingglobal3d.core.research.TerminalTier.PRO, "QA", false, 0.05, Money.of(11000, Currency.IDR), inst, date.plusDays(3)));
        assertEquals(Money.of(11000, Currency.IDR), restored.getActiveNewsTarget("NEWS").getReferencePrice());
        assertEquals(Money.of(11550, Currency.IDR), restored.getActiveNewsTarget("NEWS").getTargetPrice());
    }

    @Test public void reconciliationProgressCanBePolledWhileDomainMonitorIsBusy() throws Exception {
        TradingFacade game = game(SaveManager.forDirectory(Files.createTempDirectory("qa-audit-progress-").toFile()));
        game.beginWorldReconciliation();
        synchronized (game) {
            String progress = java.util.concurrent.CompletableFuture.supplyAsync(game::getWorldReconciliationProgress)
                    .get(1, java.util.concurrent.TimeUnit.SECONDS);
            assertEquals("PENDING", new JSONObject(progress).getString("stage"));
        }
        try { game.openTimeDeposit("DEP-TENOR-10", new BigDecimal("10000000")); fail("Reconciliation must gate commands"); }
        catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("dikunci")); }
        game.advanceRealMs(1000, true);
        JSONObject progress = new JSONObject(game.getWorldReconciliationProgress());
        assertEquals(48, progress.getLong("processedGameSeconds"));
        assertEquals(48, progress.getLong("totalGameSeconds"));
        assertEquals(progress.getLong("targetGameTimestamp"), progress.getLong("currentGameTimestamp"));
        game.endWorldReconciliation(false);
        try { game.requirePlayerFinancialWritesAvailable(); fail("Failed reconciliation unlocked commands"); }
        catch (IllegalStateException expected) { }
        game.beginWorldReconciliation();
        game.save();
        game.endWorldReconciliation(true);
        game.requirePlayerFinancialWritesAvailable();
    }

    @Test public void preflightValidatesTheMakerActuallyUsedAfterSkippingPlayerAonOrder() throws Exception {
        for (boolean solvent : new boolean[]{false, true}) {
            Instrument inst = new Instrument("IDX:AON", "AON", "QA sintetis", MarketExchange.IDX,
                    "BANK", Currency.IDR, 100, 1000000, true, Money.of(10000, Currency.IDR));
            Account buyer = new Account("QA-TAKER", "QA", AccountType.PERSONAL, null);
            Account playerMaker = new Account("QA-PLAYER-MAKER", "QA", AccountType.PERSONAL, null);
            Account nextMaker = new Account("QA-FIRM-MAKER", "QA", AccountType.FIRM, null);
            playerMaker.addPosition(new Position("AON", "IDX", Currency.IDR, 200, new BigDecimal("10000"), new BigDecimal("10000")));
            if (solvent) nextMaker.addPosition(new Position("AON", "IDX", Currency.IDR, 100, new BigDecimal("10025"), new BigDecimal("10025")));
            EconomyLedger ledger = new EconomyLedger();
            ledger.recordMutation("QA-CAPITAL", buyer, LedgerEntryType.INITIAL_CAPITAL, Money.of(50000000, Currency.IDR), true, 1, "QA sintetis");
            OrderBook book = new OrderBook(inst.getId());
            Order skipped = new Order("QA-SKIPPED", "QA-SKIPPED", playerMaker.getId(), inst.getId(), OrderSide.SELL,
                    OrderType.LIMIT, Money.of(10000, Currency.IDR), null, 200, 1, null);
            book.addLimitOrder(skipped);
            book.addLimitOrder(new Order("QA-NEXT", "QA-NEXT", nextMaker.getId(), inst.getId(), OrderSide.SELL,
                    OrderType.LIMIT, Money.of(10025, Currency.IDR), null, 100, 1, null));
            Order incoming = new Order("QA-AON-TAKER", "QA-AON-TAKER", buyer.getId(), inst.getId(), OrderSide.BUY,
                    OrderType.MARKET, null, null, 100, 2, null);
            var accounts = java.util.Map.of(buyer.getId(), buyer, playerMaker.getId(), playerMaker, nextMaker.getId(), nextMaker);
            MatchingEngine matching = new MatchingEngine();
            JSONObject estimate = matching.previewOrder(incoming, buyer, inst, book, 2, accounts);
            assertEquals("10025.000000", estimate.getString("estimatedVwap"));
            MatchingEngine.OrderResult result = matching.submitOrder(incoming, buyer, inst, book, ledger, accounts, 2);
            assertEquals(0, skipped.getFilledQuantity());
            assertEquals(200, playerMaker.getPosition(inst.getId()).getQuantity());
            if (solvent) {
                assertEquals(result.message, OrderStatus.FILLED, incoming.getStatus());
                assertEquals(100, buyer.getPosition(inst.getId()).getQuantity());
                assertEquals(Money.of(10025, Currency.IDR), result.fills.get(0).getPrice());
            } else {
                assertFalse(result.isSuccess);
                assertEquals(Money.of(50000000, Currency.IDR), buyer.getTotalCash(Currency.IDR));
                assertEquals(Money.zero(Currency.IDR), buyer.getReservedCash(Currency.IDR));
                assertEquals(1, ledger.getEntries().size());
                assertTrue(book.getTradeHistory().isEmpty());
            }
        }
    }

    @Test public void previewMatchesTwoMakerSettlementWithDifferentFeesAndReservations() throws Exception {
        for (OrderSide side : OrderSide.values()) {
            Instrument inst = new Instrument("IDX:QA", "QA", "QA sintetis", MarketExchange.IDX,
                    "BANK", Currency.IDR, 100, 1_000_000L, true, Money.of(10000, Currency.IDR));
            Account player = new Account("QA-PLAYER", "QA", AccountType.PERSONAL, null);
            EconomyLedger ledger = new EconomyLedger();
            java.util.Map<String, Account> accounts = new java.util.HashMap<>();
            accounts.put(player.getId(), player);
            ledger.recordMutation("QA-PLAYER-CASH", player, LedgerEntryType.INITIAL_CAPITAL,
                    Money.of(100_000_000, Currency.IDR), true, 1, "QA sintetis");
            if (side == OrderSide.SELL) player.addPosition(new Position("QA", "IDX", Currency.IDR, 300,
                    new BigDecimal("10000"), new BigDecimal("10000")));
            MatchingEngine matching = new MatchingEngine();
            OrderBook book = new OrderBook(inst.getId());
            for (int i = 1; i <= 2; i++) {
                Account maker = new Account("QA-MAKER-" + i, "QA", AccountType.FIRM, null);
                accounts.put(maker.getId(), maker);
                ledger.recordMutation("QA-CASH-" + i, maker, LedgerEntryType.INITIAL_CAPITAL,
                        Money.of(100_000_000, Currency.IDR), true, 1, "QA sintetis");
                if (side == OrderSide.BUY) maker.addPosition(new Position("QA", "IDX", Currency.IDR, i * 100,
                        new BigDecimal("10000"), new BigDecimal("10000")));
                Order quote = Order.createLimit("QA-QUOTE-" + i, inst.getId(), maker.getId(),
                        side == OrderSide.BUY ? OrderSide.SELL : OrderSide.BUY, Money.of(9975 + i * 25, Currency.IDR), i * 100, i);
                quote.setFeeDiscountPercent(i * 25);
                assertTrue(matching.submitOrder(quote, maker, inst, book, ledger, accounts, i).isSuccess);
            }
            Order incoming = Order.createLimit("QA-INCOMING", inst.getId(), player.getId(), side,
                    Money.of(side == OrderSide.BUY ? 10025 : 10000, Currency.IDR), 300, 3);
            incoming.setFeeDiscountPercent(10);
            JSONObject estimate = matching.previewOrder(incoming, player, inst, book, 3, accounts);
            Money before = player.getTotalCash(Currency.IDR);
            var result = matching.submitOrder(incoming, player, inst, book, ledger, accounts, 3);
            assertEquals(result.message, OrderStatus.FILLED, incoming.getStatus());
            assertEquals(2, result.fills.size());
            Money actualImpact = side == OrderSide.BUY ? before.subtract(player.getTotalCash(Currency.IDR))
                    : player.getTotalCash(Currency.IDR).subtract(before);
            assertEquals(new Money(new BigDecimal(estimate.getString("estimatedCashImpactRaw")), Currency.IDR), actualImpact);
            for (Account account : accounts.values()) {
                assertEquals(Money.zero(Currency.IDR), account.getReservedCash(Currency.IDR));
                assertTrue(ledger.reconcile(account, Currency.IDR));
            }
        }
    }

    @Test public void previewAndMaxIncludeAdaptiveDepthWithoutIssuingAssets() throws Exception {
        TradingFacade game = tradingGame(new BigDecimal("100000000000"));
        Instrument inst = game.getCatalog().getById("IDX:BBCA");
        Account mm = game.getAccount("ACC-NPC-MM-01");
        long issued = inst.getIssuedShares(), shares = mm.getPosition(inst.getId()).getQuantity();
        Money cash = mm.getTotalCash(Currency.IDR);
        int ledgerSize = game.getLedger().getEntries().size();
        JSONObject preview = game.previewOrder(order(game, "QA-PREVIEW-LARGE", OrderSide.BUY, OrderType.MARKET, 5_000_000, 0));
        assertTrue(preview.getBoolean("success"));
        assertEquals(5_000_000, preview.getLong("estimatedFillQuantity"));
        assertEquals(0, preview.getLong("remainingQuantity"));
        assertEquals("10025.000000", preview.getString("estimatedVwap"));
        JSONObject capacity = game.getOrderCapacity(game.getPersonalAccount().getId(), inst.getId(), "BUY", "MARKET", "10000");
        assertTrue(capacity.getLong("maxLots") >= 50000);
        assertEquals(issued, inst.getIssuedShares());
        assertEquals(shares, mm.getPosition(inst.getId()).getQuantity());
        assertEquals(cash, mm.getTotalCash(Currency.IDR));
        assertEquals(ledgerSize, game.getLedger().getEntries().size());
        assertTrue(game.getMarketEngine().getBook(inst.getId()).getAsks().isEmpty());
    }

    @Test public void malformedLateRuntimeFieldsFallBackBeforeLiveStateChanges() throws Exception {
        for (String field : new String[]{"weeklyBaselineNetWorth", "strategicCustomMutualFunds", "gameMsRemainder"}) {
            SaveManager manager = SaveManager.forDirectory(Files.createTempDirectory("qa-audit-late-field-").toFile());
            TradingFacade game = game(manager);
            game.save(); game.save();
            JSONObject broken = new JSONObject(Files.readString(manager.getSaveFile().toPath()));
            if (field.equals("gameMsRemainder")) broken.put(field, 1000);
            else broken.getJSONObject("runtimeState").put(field, "invalid");
            Files.writeString(manager.getSaveFile().toPath(), broken.toString());
            assertEquals(SaveManager.LoadSource.BACKUP_RECOVERED, manager.load().getSource());
            assertEquals(Money.of(50000000, Currency.IDR), game.getPersonalAccount().getTotalCash(Currency.IDR));
        }
    }

    @Test public void asynchronousFailurePublishesFailedEvenWithoutNativeLogger() throws Exception {
        SaveManager manager = SaveManager.forDirectory(Files.createTempDirectory("qa-audit-async-").toFile());
        TradingFacade game = game(manager);
        Files.createDirectory(manager.getTempFile().toPath());
        var bridge = new com.tradingglobal3d.mobile.TradingWebBridge(game);
        try {
            bridge.beginClockSync(true);
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
            while (bridge.isClockSyncPending() && System.nanoTime() < deadline) Thread.sleep(10);
            assertFalse("Clock reconciliation stranded in PENDING", bridge.isClockSyncPending());
            JSONObject status = new JSONObject(bridge.getClockSyncStatus());
            assertFalse(status.getBoolean("success"));
            assertEquals("FAILED", status.getString("status"));
            assertTrue(game.areFinancialWritesBlocked());
        } finally { bridge.destroy(); }
    }

    @Test public void sameSizeSameTimestampCorruptionCannotBypassBackupValidationCache() throws Exception {
        SaveManager manager = SaveManager.forDirectory(Files.createTempDirectory("qa-audit-cache-").toFile());
        TradingFacade game = game(manager);
        game.save(); game.save();
        byte[] backup = Files.readAllBytes(manager.getBackupFile().toPath());
        Path primary = manager.getSaveFile().toPath();
        var modifiedAt = Files.getLastModifiedTime(primary);
        byte[] original = Files.readAllBytes(primary);
        JSONObject bad = new JSONObject(new String(original, java.nio.charset.StandardCharsets.UTF_8));
        String previous = bad.getJSONObject("runtimeState").getString("weeklyBaselineNetWorth");
        bad.getJSONObject("runtimeState").put("weeklyBaselineNetWorth", "X".repeat(previous.length()));
        byte[] corrupted = bad.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(original.length, corrupted.length);
        Files.write(primary, corrupted);
        Files.setLastModifiedTime(primary, modifiedAt);
        game.save();
        assertArrayEquals(backup, Files.readAllBytes(manager.getBackupFile().toPath()));
        assertEquals(SaveManager.LoadSource.PRIMARY, manager.load().getSource());
    }

    @Test public void invalidUtf8PrimaryFallsBackWithoutLosingBackup() throws Exception {
        SaveManager manager = SaveManager.forDirectory(Files.createTempDirectory("qa-audit-encoding-").toFile());
        TradingFacade game = game(manager);
        game.save(); game.save();
        byte[] backup = Files.readAllBytes(manager.getBackupFile().toPath());
        Files.write(manager.getSaveFile().toPath(), new byte[]{'{', '"', (byte) 0xc3, '"', ':', '1', '}'});
        assertEquals(SaveManager.LoadSource.BACKUP_RECOVERED, manager.load().getSource());
        game.save();
        assertArrayEquals(backup, Files.readAllBytes(manager.getBackupFile().toPath()));
    }

    @Test public void rotationAndPrimaryMoveFailuresKeepLastValidBytesAndRecoverOnRetry() throws Exception {
        for (boolean blockBackup : new boolean[]{true, false}) {
            Path dir = Files.createTempDirectory("qa-audit-move-failure-");
            SaveManager manager = SaveManager.forDirectory(dir.toFile());
            TradingFacade game = game(manager);
            game.save(); game.save();
            byte[] primary = Files.readAllBytes(manager.getSaveFile().toPath());
            byte[] backup = Files.readAllBytes(manager.getBackupFile().toPath());
            Path blocked = (blockBackup ? manager.getBackupFile() : manager.getSaveFile()).toPath();
            Path kept = dir.resolve("qa-kept-valid.json");
            Files.move(blocked, kept);
            Files.createDirectory(blocked);
            Path blocker = Files.write(blocked.resolve("qa-blocker"), new byte[]{1});
            try { game.save(); fail("Blocked move must not acknowledge a save"); }
            catch (java.io.IOException expected) {
                assertTrue(game.areFinancialWritesBlocked());
                if (blockBackup) assertArrayEquals(primary, Files.readAllBytes(manager.getSaveFile().toPath()));
                else assertArrayEquals(backup, Files.readAllBytes(manager.getBackupFile().toPath()));
                assertArrayEquals(blockBackup ? backup : primary, Files.readAllBytes(kept));
                manager.readValidatedState(Files.readString(kept));
            }
            Files.delete(blocker); Files.delete(blocked); Files.move(kept, blocked);
            game.save();
            assertFalse(game.areFinancialWritesBlocked());
            assertTrue(game.load());
            assertEquals(Money.of(50_000_000, Currency.IDR), game.getPersonalAccount().getTotalCash(Currency.IDR));
            manager.readValidatedState(Files.readString(manager.getBackupFile().toPath()));
        }
    }

    @Test public void longLedgerSaveReloadAndOfflineCatchUpHaveMeasuredHostResults() throws Exception {
        Path dir = Files.createTempDirectory("qa-audit-long-ledger-");
        SaveManager manager = SaveManager.forDirectory(dir.toFile());
        TradingFacade game = game(manager);
        for (int i = 0; i < 10000; i++) game.getLedger().recordMutation("QA-LONG-" + i, game.getPersonalAccount(),
                LedgerEntryType.DEPOSIT, Money.of(1, Currency.IDR), true, game.getGameTimestamp(), "QA sintetis benchmark");
        long before = game.getGameTimestamp();
        long start = System.nanoTime();
        game.advanceRealMs(86_400_000L, false);
        double catchUpMs = (System.nanoTime() - start) / 1_000_000.0;
        assertEquals(before + 48L * 86_400_000L, game.getGameTimestamp());
        assertTrue(game.reconcileAll());
        double[] saveMs = new double[30], loadMs = new double[30];
        for (int i = 0; i < 30; i++) {
            start = System.nanoTime();
            game.save();
            saveMs[i] = (System.nanoTime() - start) / 1_000_000.0;
            start = System.nanoTime();
            assertTrue(game.load());
            loadMs[i] = (System.nanoTime() - start) / 1_000_000.0;
            assertTrue(game.reconcileAll());
            assertTrue(game.getLedger().isRequestProcessed("QA-LONG-0"));
        }
        JSONObject metrics = new JSONObject().put("fixture", "QA sintetis, 10000 mutasi + 48 hari game")
                .put("catchUpSamples", 1).put("catchUpMs", catchUpMs).put("saveSamplesMs", new org.json.JSONArray(saveMs))
                .put("loadSamplesMs", new org.json.JSONArray(loadMs)).put("saveBytes", Files.size(manager.getSaveFile().toPath()))
                .put("entries", game.getLedger().getEntries().size());
        java.util.Arrays.sort(saveMs); java.util.Arrays.sort(loadMs);
        metrics.put("saveP95Ms", saveMs[28]).put("saveMaxMs", saveMs[29])
                .put("loadP95Ms", loadMs[28]).put("loadMaxMs", loadMs[29]);
        Files.writeString(dir.resolve("host-metrics.json"), metrics.toString(2));
        System.out.println("AUDIT_HOST_METRICS " + metrics);
    }

    @Test public void externalCapitalEventRemainsIdempotentBeyondFiveThousandMutationsAndReload() {
        Account mm = new Account("QA-MM", "QA sintetis", AccountType.FIRM, null);
        EconomyLedger ledger = new EconomyLedger();
        var first = ledger.recordMutation("MM-CAPITAL-FIRST", mm, LedgerEntryType.LIQUIDITY_CAPITAL,
                Money.of(1000, Currency.IDR), true, 1L, "QA modal eksternal");
        for (int i = 0; i < 5001; i++) ledger.recordMutation("QA-NEXT-" + i, mm, LedgerEntryType.TRADE_SELL,
                Money.of(1, Currency.IDR), true, 2L + i, "QA sintetis");
        Money balance = mm.getTotalCash(Currency.IDR);
        int entries = ledger.getEntries().size();
        assertTrue(ledger.isRequestProcessed("MM-CAPITAL-FIRST"));
        assertSame(first, ledger.recordMutation("MM-CAPITAL-FIRST", mm, LedgerEntryType.LIQUIDITY_CAPITAL,
                Money.of(1000, Currency.IDR), true, 9000L, "QA retry"));
        EconomyLedger restored = new EconomyLedger();
        restored.restoreEntries(ledger.getEntries());
        assertTrue(restored.isRequestProcessed("MM-CAPITAL-FIRST"));
        assertSame(first, restored.getProcessedEntry("MM-CAPITAL-FIRST"));
        restored.recordMutation("MM-CAPITAL-FIRST", mm, LedgerEntryType.LIQUIDITY_CAPITAL,
                Money.of(1000, Currency.IDR), true, 10000L, "QA retry sesudah restore");
        assertEquals(balance, mm.getTotalCash(Currency.IDR));
        assertEquals(entries, restored.getEntries().size());
        assertTrue(restored.reconcile(mm, Currency.IDR));
    }
}
