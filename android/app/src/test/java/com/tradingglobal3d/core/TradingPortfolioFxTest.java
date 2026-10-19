package com.tradingglobal3d.core;

import com.tradingglobal3d.core.economy.FxExchangeService;
import com.tradingglobal3d.core.economy.FxQuote;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.npc.MarketEngine;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TradingPortfolioFxTest {
    private File testDir;
    private TradingFacade facade;
    private InstrumentCatalog catalog;
    private Instrument bbca;
    private Instrument aapl;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "trading_port_test_" + System.currentTimeMillis());
        testDir.mkdirs();

        File dataDir = new File("src/main/assets/data");
        File universeFile = new File(dataDir, "market_universe.json");
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        catalog = InstrumentCatalog.fromJsonString(jsonUniverse);

        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));

        bbca = catalog.getById("IDX:BBCA");
        aapl = catalog.getById("US:AAPL");
        assertNotNull(bbca);
        assertNotNull(aapl);
    }

    @After
    public void tearDown() {
        deleteDir(testDir);
    }

    private void deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) deleteDir(f);
        }
        dir.delete();
    }

    @Test
    public void testFullTradingJourneyBuyPartialCancelSell() {
        Account personal = facade.getPersonalAccount();
        Money startCash = personal.getTotalCash(Currency.IDR);

        // Siapkan counterparty penjual dengan 100 lembar BBCA di harga 10.250
        Account seller = new Account("ACC-SELLER-01", "Counterparty Penjual", AccountType.PERSONAL, null);
        seller.creditCash(Money.of(10_000_000, Currency.IDR));
        seller.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100, BigDecimal.valueOf(10000), BigDecimal.valueOf(10250)));

        Order sellOrder = new Order(
                "ORD-S-100", "REQ-S-100",
                seller.getId(), bbca.getId(),
                OrderSide.SELL, OrderType.LIMIT,
                Money.of(10250, Currency.IDR), null, 100, System.currentTimeMillis(), null
        );

        Map<String, Account> map = new HashMap<>();
        map.put(personal.getId(), personal);
        map.put(seller.getId(), seller);

        facade.getMarketEngine().submitOrder(sellOrder, seller, bbca, facade.getLedger(), map, System.currentTimeMillis());

        // 1. Pemain beli 300 lembar pada harga limit 10.250 (akan match 100, sisa 200 antre)
        Order buyOrder = new Order(
                "ORD-B-300", "REQ-B-300",
                personal.getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT,
                Money.of(10250, Currency.IDR), null, 300, System.currentTimeMillis(), null
        );

        facade.getMarketEngine().submitOrder(buyOrder, personal, bbca, facade.getLedger(), map, System.currentTimeMillis());

        assertEquals(OrderStatus.PARTIALLY_FILLED, buyOrder.getStatus());
        assertEquals(100, buyOrder.getFilledQuantity());
        assertEquals(200, buyOrder.getRemainingQuantity());

        // 2. Batalkan sisa 200 lembar
        boolean cancelSuccess = facade.getMarketEngine().cancelOrder(buyOrder, personal);
        assertTrue(cancelSuccess);
        assertEquals(OrderStatus.CANCELLED, buyOrder.getStatus());

        // 3. Jual 100 lembar saham yang dimiliki ke counterparty pembeli baru di harga 10.500 (ambil untung)
        Account buyerNew = new Account("ACC-BUYER-02", "Pembeli Baru", AccountType.PERSONAL, null);
        buyerNew.creditCash(Money.of(10_000_000, Currency.IDR));
        map.put(buyerNew.getId(), buyerNew);

        Order buyNewOrder = new Order(
                "ORD-B-NEW", "REQ-B-NEW",
                buyerNew.getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT,
                Money.of(10500, Currency.IDR), null, 100, System.currentTimeMillis(), null
        );
        facade.getMarketEngine().submitOrder(buyNewOrder, buyerNew, bbca, facade.getLedger(), map, System.currentTimeMillis());

        Order sellPlayer = new Order(
                "ORD-S-PLAYER", "REQ-S-PLAYER",
                personal.getId(), bbca.getId(),
                OrderSide.SELL, OrderType.LIMIT,
                Money.of(10500, Currency.IDR), null, 100, System.currentTimeMillis(), null
        );
        facade.getMarketEngine().submitOrder(sellPlayer, personal, bbca, facade.getLedger(), map, System.currentTimeMillis());

        assertEquals(OrderStatus.FILLED, sellPlayer.getStatus());
        assertEquals(0, personal.getPosition("BBCA").getQuantity());

        // Verifikasi audit rekonsiliasi kas & ledger
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testFillMarksEveryHolderOfOnlyTheTradedExchange() {
        Account holder = facade.createClientAccount("Holder", null, Money.of(1000000, Currency.IDR));
        holder.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100, new BigDecimal("10000"), new BigDecimal("10000")));
        holder.addPosition(new Position("BBCA", "US", Currency.USD, 1, new BigDecimal("10"), new BigDecimal("10")));
        Account seller = facade.createClientAccount("Seller", null, Money.of(1000000, Currency.IDR));
        seller.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100, new BigDecimal("10000"), new BigDecimal("10000")));
        Map<String, Account> accounts = new HashMap<>();
        for (Account account : facade.getAllAccounts()) accounts.put(account.getId(), account);
        Order ask = Order.createLimit("MARK-ASK", bbca.getId(), seller.getId(), OrderSide.SELL, Money.of(10500, Currency.IDR), 100, 1L);
        facade.getMarketEngine().submitOrder(ask, seller, bbca, facade.getLedger(), accounts, 1L);
        assertEquals(new BigDecimal("10000"), holder.getPosition("IDX:BBCA").getCurrentPrice());
        Order buy = Order.createLimit("MARK-BUY", bbca.getId(), facade.getPersonalAccount().getId(), OrderSide.BUY, Money.of(10500, Currency.IDR), 100, 2L);
        facade.getMarketEngine().submitOrder(buy, facade.getPersonalAccount(), bbca, facade.getLedger(), accounts, 2L);
        assertEquals(OrderStatus.FILLED, buy.getStatus());
        assertEquals(0, holder.getPosition("IDX:BBCA").getCurrentPrice().compareTo(new BigDecimal("10500")));
        assertEquals(new BigDecimal("10"), holder.getPosition("US:BBCA").getCurrentPrice());
        assertEquals(Money.of(50000, Currency.IDR), holder.getPosition("IDX:BBCA").getUnrealizedPnL());
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testCrossingOwnOrderRejectsBeforeCashOrPriceChanges() {
        Account account = facade.getPersonalAccount();
        account.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100, new BigDecimal("10000"), new BigDecimal("10250")));
        Order ask = Order.createLimit("SELF-ASK", bbca.getId(), account.getId(), OrderSide.SELL, Money.of(10500, Currency.IDR), 100, 1L);
        assertTrue(facade.submitOrder(ask).isSuccess);
        Money cash = account.getTotalCash(Currency.IDR);
        int entries = facade.getLedger().getEntries().size();
        Money price = bbca.getCurrentPrice();
        Order buy = Order.createLimit("SELF-BUY", bbca.getId(), account.getId(), OrderSide.BUY, Money.of(10500, Currency.IDR), 100, 2L);
        assertFalse("Own resting order must not generate a fill", facade.submitOrder(buy).isSuccess);
        assertEquals(OrderStatus.OPEN, ask.getStatus());
        assertEquals(0, ask.getFilledQuantity());
        assertEquals(100, account.getPosition("IDX:BBCA").getQuantity());
        assertEquals(Money.zero(Currency.IDR), account.getReservedCash(Currency.IDR));
        assertEquals(cash, account.getTotalCash(Currency.IDR));
        assertEquals(entries, facade.getLedger().getEntries().size());
        assertEquals(price, bbca.getCurrentPrice());
    }

    @Test
    public void testInvalidSecondMakerCannotLeaveFirstFillPartiallySettled() {
        Account first = facade.createClientAccount("First", null, Money.of(1000000, Currency.IDR));
        Account second = facade.createClientAccount("Second", null, Money.of(1000000, Currency.IDR));
        for (Account seller : new Account[]{first, second}) {
            seller.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100, new BigDecimal("10000"), new BigDecimal("10250")));
            assertTrue(facade.submitOrder(Order.createLimit("ATOMIC-" + seller.getId(), bbca.getId(), seller.getId(),
                    OrderSide.SELL, Money.of(10500, Currency.IDR), 100, 1L)).isSuccess);
        }
        second.removePosition("IDX:BBCA"); // Simulate inconsistent/restored counterparty graph.
        Account buyer = facade.getPersonalAccount();
        Money cash = buyer.getTotalCash(Currency.IDR);
        Money sellerCash = first.getTotalCash(Currency.IDR);
        int entries = facade.getLedger().getEntries().size();
        Order buy = Order.createLimit("ATOMIC-BUY", bbca.getId(), buyer.getId(), OrderSide.BUY, Money.of(10500, Currency.IDR), 200, 2L);
        MatchingEngine.OrderResult result = facade.submitOrder(buy);
        assertFalse(result.isSuccess);
        assertEquals(0, buy.getFilledQuantity());
        assertNull(buyer.getPosition("IDX:BBCA"));
        assertEquals(100, first.getPosition("IDX:BBCA").getQuantity());
        assertEquals(cash, buyer.getTotalCash(Currency.IDR));
        assertEquals(sellerCash, first.getTotalCash(Currency.IDR));
        assertEquals(Money.zero(Currency.IDR), buyer.getReservedCash(Currency.IDR));
        assertEquals(entries, facade.getLedger().getEntries().size());
        assertTrue(facade.getMarketEngine().getBook(bbca.getId()).getTradeHistory().isEmpty());
    }

    @Test
    public void testLimitBuyReserveCoversFeeRoundingAcrossMinimumLotFills() {
        Account buyer = facade.getPersonalAccount();
        for (int i = 0; i < 2; i++) {
            Account seller = facade.createClientAccount("Rounding seller " + i, null, Money.of(1000, Currency.IDR));
            seller.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100, new BigDecimal("10000"), new BigDecimal("10250")));
            assertTrue(facade.submitOrder(Order.createLimit("ROUND-ASK-" + i, bbca.getId(), seller.getId(),
                    OrderSide.SELL, Money.of(10250, Currency.IDR), 100, i)).isSuccess);
        }
        Money before = buyer.getTotalCash(Currency.IDR);
        Order buy = Order.createLimit("ROUND-BUY", bbca.getId(), buyer.getId(), OrderSide.BUY,
                Money.of(10250, Currency.IDR), 200, 3L);
        assertTrue(facade.submitOrder(buy).isSuccess);
        assertEquals(OrderStatus.FILLED, buy.getStatus());
        assertEquals(before.subtract(Money.of(2053076, Currency.IDR)), buyer.getTotalCash(Currency.IDR));
        assertEquals(Money.zero(Currency.IDR), buyer.getReservedCash(Currency.IDR));
        assertEquals(200, buyer.getPosition("IDX:BBCA").getQuantity());
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testFilledLimitBuyReleasesPriceImprovementReserve() {
        Account personal = facade.getPersonalAccount();
        facade.generateNpcLiquidity();
        Order buy = Order.createLimit("RESERVE-IMPROVEMENT", bbca.getId(), personal.getId(),
                OrderSide.BUY, Money.of(11000, Currency.IDR), 100, 1L);
        MatchingEngine.OrderResult result = facade.submitOrder(buy);
        assertTrue(result.isSuccess);
        assertEquals(OrderStatus.FILLED, buy.getStatus());
        assertEquals("Filled order must not retain unused limit-price cash",
                Money.zero(Currency.IDR), personal.getReservedCash(Currency.IDR));
        assertEquals(Money.zero(Currency.IDR), buy.getReservedMoney());
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testInvalidOrderCannotChangeCashOrBook() {
        Account personal = facade.getPersonalAccount();
        Money before = personal.getTotalCash(Currency.IDR);
        Order[] invalid = {
                Order.createLimit("BAD-ZERO", bbca.getId(), personal.getId(), OrderSide.BUY, Money.of(10250, Currency.IDR), 0, 1L),
                Order.createLimit("BAD-LOT", bbca.getId(), personal.getId(), OrderSide.BUY, Money.of(10250, Currency.IDR), 1, 1L),
                Order.createLimit("BAD-TICK", bbca.getId(), personal.getId(), OrderSide.BUY, Money.of(10251, Currency.IDR), 100, 1L),
                Order.createLimit("BAD-CURRENCY", bbca.getId(), personal.getId(), OrderSide.BUY, Money.of(100, Currency.USD), 100, 1L),
                Order.createLimit("BAD-PRICE", bbca.getId(), personal.getId(), OrderSide.BUY, Money.of(-25, Currency.IDR), 100, 1L)
        };
        for (Order order : invalid) {
            assertFalse("Invalid order " + order.getId() + " must be rejected", facade.submitOrder(order).isSuccess);
            assertEquals(OrderStatus.REJECTED, order.getStatus());
            assertEquals(before, personal.getTotalCash(Currency.IDR));
            assertEquals(Money.zero(Currency.IDR), personal.getReservedCash(Currency.IDR));
        }
        assertTrue(facade.getMarketEngine().getOrCreateBook(bbca).getBidLevels(10).isEmpty());
    }

    @Test
    public void testOpenSellOrdersCannotPromiseTheSameSharesTwice() {
        Account personal = facade.getPersonalAccount();
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10250)));
        Order first = Order.createLimit("SELL-HELD-1", bbca.getId(), personal.getId(),
                OrderSide.SELL, Money.of(12000, Currency.IDR), 100, 1L);
        Order second = Order.createLimit("SELL-HELD-2", bbca.getId(), personal.getId(),
                OrderSide.SELL, Money.of(12000, Currency.IDR), 100, 2L);
        assertTrue(facade.submitOrder(first).isSuccess);
        assertFalse("Open sell quantity must reduce available shares", facade.submitOrder(second).isSuccess);
        assertTrue(facade.cancelOrder(first));
        Order afterCancel = Order.createLimit("SELL-HELD-3", bbca.getId(), personal.getId(),
                OrderSide.SELL, Money.of(12000, Currency.IDR), 100, 3L);
        assertTrue(facade.submitOrder(afterCancel).isSuccess);
    }

    @Test
    public void testMarketBuyInsufficientCashDoesNotPartiallySettle() {
        Account personal = facade.getPersonalAccount();
        facade.generateNpcLiquidity();
        Money keep = Money.of(1_028_000, Currency.IDR);
        facade.withdraw("LOW-CASH", personal.getId(), personal.getTotalCash(Currency.IDR).subtract(keep), "test");
        int count = facade.getLedger().getEntries().size();
        Order market = new Order("LOW-MARKET", "LOW-MARKET-REQ", personal.getId(), bbca.getId(),
                OrderSide.BUY, OrderType.MARKET, null, null, 100, 1L, null);
        MatchingEngine.OrderResult result = facade.submitOrder(market);
        assertFalse(result.isSuccess);
        assertEquals(keep, personal.getTotalCash(Currency.IDR));
        assertEquals(Money.zero(Currency.IDR), personal.getReservedCash(Currency.IDR));
        assertNull(personal.getPosition("BBCA"));
        assertEquals(count, facade.getLedger().getEntries().size());
    }

    @Test
    public void testPausedTickDoesNotGenerateLiquidity() {
        facade.getClock().play();
        facade.getClock().tick(2700);
        facade.getClock().pause();
        Money reserved = facade.getAllAccounts().stream()
                .filter(a -> a.getType() == AccountType.FIRM).findFirst().get().getReservedCash(Currency.IDR);
        facade.tick(5);
        assertEquals(reserved, facade.getAllAccounts().stream()
                .filter(a -> a.getType() == AccountType.FIRM).findFirst().get().getReservedCash(Currency.IDR));
    }

    @Test
    public void testTriggeredOrdersCancelUnfilledRemainderAndWaitDuringHalt() {
        Account personal = facade.getPersonalAccount();
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100,
                BigDecimal.valueOf(10250), BigDecimal.valueOf(10250)));
        for (OrderType type : new OrderType[]{OrderType.STOP_LOSS, OrderType.TAKE_PROFIT}) {
            Order trigger = new Order("TRIGGER-" + type, "TRIGGER-REQ-" + type, personal.getId(), bbca.getId(),
                    OrderSide.SELL, type, null, Money.of(10250, Currency.IDR), 100, 1L, null);
            facade.submitOrder(trigger);
            facade.getMarketEngine().haltTrading(bbca.getId());
            facade.evaluateTriggers();
            assertEquals("Halt must retain trigger without losing the obligation", OrderStatus.PENDING_TRIGGER, trigger.getStatus());
            facade.getMarketEngine().resumeTrading(bbca.getId());
            facade.evaluateTriggers();
            assertEquals("Triggered market remainder must not remain OPEN outside the book", OrderStatus.CANCELLED, trigger.getStatus());
            assertEquals(100, personal.getPosition("BBCA").getQuantity());
        }
    }

    @Test
    public void testStopLossTriggerActivation() {
        Account personal = facade.getPersonalAccount();
        facade.getClock().play();

        // Berikan kepemilikan saham 100 lembar BBCA agar order jual sah
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100, BigDecimal.valueOf(10250), BigDecimal.valueOf(10250)));

        // Pemain pasang order jual Stop-Loss BBCA jika harga jatuh <= Rp 9.500
        Money triggerPrice = Money.of(9500, Currency.IDR);
        Order stopLossOrder = new Order(
                "ORD-SL-TEST", "REQ-SL-1",
                personal.getId(), bbca.getId(),
                OrderSide.SELL, OrderType.STOP_LOSS,
                null, triggerPrice, 100, System.currentTimeMillis(), null
        );

        facade.submitOrder(stopLossOrder);
        assertEquals(OrderStatus.PENDING_TRIGGER, stopLossOrder.getStatus());

        // Simulasikan harga pasar masih di atas pemicu (Rp 10.000)
        bbca.updatePrice(Money.of(10000, Currency.IDR), 1000);
        facade.tick(5);
        assertEquals(OrderStatus.PENDING_TRIGGER, stopLossOrder.getStatus());

        // Simulasikan harga pasar jatuh ke Rp 9.400 (menembus stop loss trigger!)
        bbca.updatePrice(Money.of(9400, Currency.IDR), 1000);
        facade.tick(5);

        // Order pemicu harus aktif (berubah dari PENDING_TRIGGER)
        assertNotEquals(OrderStatus.PENDING_TRIGGER, stopLossOrder.getStatus());
    }

    @Test
    public void testFxQuoteAndAtomicConversion() {
        Account personal = facade.getPersonalAccount();
        assertEquals(Money.zero(Currency.USD), personal.getTotalCash(Currency.USD));

        // 1. Preview penukaran Rp 15.839.500 ke USD (kurs ask ~15839.50)
        Money idrToExchange = Money.of(15_839_500, Currency.IDR);
        FxQuote.ConversionPreview preview = facade.previewFx(Currency.IDR, Currency.USD, idrToExchange);
        assertNotNull(preview);
        assertTrue("Harus menghasilkan setidaknya $990 USD", preview.netToAmount.getAmount().doubleValue() >= 990.0);

        // 2. Eksekusi konversi
        FxExchangeService.ConversionResult res = facade.convertFx(
                "REQ-FX-CONV-1", personal.getId(),
                Currency.IDR, Currency.USD, idrToExchange
        );
        assertTrue(res.isSuccess);

        // Kas IDR berkurang, kas USD bertambah
        assertEquals(Money.of(50_000_000 - 15_839_500, Currency.IDR), personal.getTotalCash(Currency.IDR));
        assertTrue(personal.getTotalCash(Currency.USD).isPositive());

        // Verifikasi audit ledger
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testFxRejectExpiredQuoteAndReservedCashProtection() {
        Account personal = facade.getPersonalAccount();

        // 1. Uji Proteksi Kas Tertahan: Tahan Rp 45.000.000 untuk order
        personal.reserveCash(Money.of(45_000_000, Currency.IDR));
        assertEquals(Money.of(5_000_000, Currency.IDR), personal.getAvailableCash(Currency.IDR));

        // Coba konversi Rp 10.000.000 ke USD (harus ditolak karena available cuma Rp 5.000.000)
        FxExchangeService.ConversionResult res = facade.convertFx(
                "REQ-FX-OVER-AVAIL", personal.getId(),
                Currency.IDR, Currency.USD, Money.of(10_000_000, Currency.IDR)
        );
        assertFalse("Harus menolak konversi dari kas tertahan", res.isSuccess);
        assertTrue(res.message.contains("tidak cukup"));

        // 2. Uji Penolakan Kuotasi Kedaluwarsa
        FxQuote expiredQuote = new FxQuote("QUOTE-EXPIRED", BigDecimal.valueOf(15800), System.currentTimeMillis() - 100_000L);
        assertTrue(expiredQuote.isExpired(System.currentTimeMillis()));

        FxExchangeService.ConversionResult expRes = facade.getFxService().executeConversion(
                "REQ-FX-EXP", personal, expiredQuote,
                Currency.IDR, Currency.USD, Money.of(1_000_000, Currency.IDR),
                System.currentTimeMillis(), facade.getLedger()
        );
        assertFalse("Harus menolak quote expired", expRes.isSuccess);
        assertTrue(expRes.message.contains("kedaluwarsa"));
    }

    @Test
    public void testFxRetryReturnsOriginalQuoteAndAmountAfterRateChangeAndReload() throws Exception {
        String accountId = facade.getPersonalAccount().getId();
        Money amount = Money.of(40_000_000, Currency.IDR);
        FxExchangeService.ConversionResult first = facade.convertFx("FX-RETRY", accountId, Currency.IDR, Currency.USD, amount);
        assertTrue(first.isSuccess);
        Money idr = facade.getPersonalAccount().getTotalCash(Currency.IDR);
        Money usd = facade.getPersonalAccount().getTotalCash(Currency.USD);
        int entries = facade.getLedger().getEntries().size();
        facade.getFxService().setMidRate(new BigDecimal("17000"));
        FxExchangeService.ConversionResult retry = facade.convertFx("FX-RETRY", accountId, Currency.IDR, Currency.USD, amount);
        assertTrue("Retry is a status lookup, not a second affordability check", retry.isSuccess);
        assertEquals(first.preview.netToAmount, retry.preview.netToAmount);
        assertEquals(first.preview.effectiveRate, retry.preview.effectiveRate);
        facade.save();
        TradingFacade loaded = new TradingFacade(SaveManager.forDirectory(testDir));
        loaded.setCatalog(catalog);
        assertTrue(loaded.load());
        FxExchangeService.ConversionResult afterLoad = loaded.convertFx("FX-RETRY", accountId, Currency.IDR, Currency.USD, amount);
        assertTrue(afterLoad.isSuccess);
        assertEquals(first.preview.netToAmount, afterLoad.preview.netToAmount);
        assertEquals(first.preview.effectiveRate, afterLoad.preview.effectiveRate);
        assertEquals(idr, loaded.getPersonalAccount().getTotalCash(Currency.IDR));
        assertEquals(usd, loaded.getPersonalAccount().getTotalCash(Currency.USD));
        assertEquals(entries, loaded.getLedger().getEntries().size());
        assertTrue(loaded.reconcileAll());
    }

    @Test
    public void testInvalidFxInputsAndLedgerCollisionCannotPartiallySettle() {
        Account account = facade.getPersonalAccount();
        FxQuote quote = facade.getFxService().getOrGenerateQuote(System.currentTimeMillis());
        Money before = account.getTotalCash(Currency.IDR);
        int entries = facade.getLedger().getEntries().size();
        for (Money amount : new Money[]{Money.zero(Currency.IDR), Money.of(-1000, Currency.IDR), Money.of(1000, Currency.USD)}) {
            try {
                quote.calculatePreview(Currency.IDR, Currency.USD, amount);
                fail("Invalid FX amount must not produce a review");
            } catch (IllegalArgumentException expected) { }
            assertEquals(before, account.getTotalCash(Currency.IDR));
            assertEquals(entries, facade.getLedger().getEntries().size());
        }
        facade.deposit("FX-COLLISION-CREDIT", account.getId(), Money.of(1000, Currency.IDR), "unrelated request");
        before = account.getTotalCash(Currency.IDR);
        entries = facade.getLedger().getEntries().size();
        FxExchangeService.ConversionResult result = facade.convertFx("FX-COLLISION", account.getId(), Currency.IDR, Currency.USD, Money.of(1000000, Currency.IDR));
        assertFalse("Pre-existing unrelated ledger leg must reject before debit", result.isSuccess);
        assertEquals(before, account.getTotalCash(Currency.IDR));
        assertEquals(Money.zero(Currency.USD), account.getTotalCash(Currency.USD));
        assertEquals(entries, facade.getLedger().getEntries().size());
    }

    @Test
    public void testAntiRoundingArbitrage() {
        // Uji bahwa konversi bolak-balik IDR -> USD -> IDR tidak pernah menciptakan uang gratis
        Account acc = new Account("ACC-ARB-TEST", "Arbitrage Test", AccountType.PERSONAL, null);
        Money initialIdr = Money.of(100_000_000, Currency.IDR);
        acc.creditCash(initialIdr);

        FxQuote quote = facade.getFxService().getOrGenerateQuote(System.currentTimeMillis());

        // IDR -> USD
        FxQuote.ConversionPreview p1 = quote.calculatePreview(Currency.IDR, Currency.USD, initialIdr);
        Money obtainedUsd = p1.netToAmount;

        // USD -> IDR
        FxQuote.ConversionPreview p2 = quote.calculatePreview(Currency.USD, Currency.IDR, obtainedUsd);
        Money finalIdr = p2.netToAmount;

        // Final IDR harus lebih kecil dari modal awal karena adanya spread dealer 0.5%
        assertTrue("Konversi bolak-balik tidak boleh menguntungkan (harus tergerus spread)", finalIdr.compareTo(initialIdr) < 0);
    }
}
