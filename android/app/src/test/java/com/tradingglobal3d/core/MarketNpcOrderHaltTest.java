package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
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
import java.util.List;

import static org.junit.Assert.*;

public class MarketNpcOrderHaltTest {
    private File testDir;
    private TradingFacade facade;
    private InstrumentCatalog catalog;
    private Instrument bbca;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "trading_order_test_" + System.currentTimeMillis());
        testDir.mkdirs();

        File dataDir = new File("src/main/assets/data");
        File universeFile = new File(dataDir, "market_universe.json");
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        catalog = InstrumentCatalog.fromJsonString(jsonUniverse);

        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));

        bbca = catalog.getById("IDX:BBCA");
        assertNotNull(bbca);
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
    public void testOrderWithoutFillDoesNotChangeLastTradePrice() {
        Money initialPrice = bbca.getCurrentPrice();
        OrderBook book = facade.getMarketEngine().getOrCreateBook(bbca);
        assertEquals(initialPrice, book.getLastTradedPrice());

        Account personal = facade.getPersonalAccount();
        // Pasang order beli limit jauh di bawah pasar (misal Rp 8.000 vs pasar Rp 10.250)
        Money bidPrice = Money.of(8000, Currency.IDR);
        Order lowBid = new Order(
                "ORD-TEST-LOWBID", "REQ-LOWBID-1",
                personal.getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT,
                bidPrice, null, 100, System.currentTimeMillis(), null
        );

        MatchingEngine.OrderResult res = facade.submitOrder(lowBid);
        assertTrue(res.isSuccess);
        assertTrue(res.fills.isEmpty()); // Tidak match
        assertEquals(OrderStatus.OPEN, lowBid.getStatus());

        // Pastikan harga transaksi terakhir TIDAK BERUBAH (FR-04.14)
        assertEquals(initialPrice, book.getLastTradedPrice());
        assertEquals(initialPrice, bbca.getCurrentPrice());
    }

    @Test
    public void testLimitOrderMatchingAndPriceTimePriority() {
        OrderBook book = facade.getMarketEngine().getOrCreateBook(bbca);
        Account personal = facade.getPersonalAccount();

        // 1. Generate antrean likuiditas dari NPC Market Maker
        facade.generateNpcLiquidity();
        List<OrderBook.BookLevel> askLevels = book.getAskLevels(5);
        assertFalse("Harus ada antrean ask dari Market Maker", askLevels.isEmpty());

        BigDecimal lowestAskPrice = askLevels.get(0).price;
        long availableQty = askLevels.get(0).totalQuantity;
        assertTrue(availableQty > 0);

        // 2. Pemain pasang order beli limit pada harga lowestAsk
        Money buyPrice = new Money(lowestAskPrice, Currency.IDR);
        long buyQty = 100; // 1 lot
        Order buyOrder = new Order(
                "ORD-BUY-100", "REQ-BUY-100",
                personal.getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT,
                buyPrice, null, buyQty, System.currentTimeMillis(), null
        );

        MatchingEngine.OrderResult res = facade.submitOrder(buyOrder);
        assertTrue(res.isSuccess);
        assertFalse("Harus menghasilkan fill", res.fills.isEmpty());
        assertEquals(OrderStatus.FILLED, buyOrder.getStatus());
        assertEquals(buyQty, buyOrder.getFilledQuantity());

        // Pastikan harga transaksi terakhir terupdate ke harga eksekusi
        assertEquals(buyPrice, book.getLastTradedPrice());

        // Pastikan posisi saham bertambah di akun pemain
        assertNotNull(personal.getPosition("BBCA"));
        assertEquals(buyQty, personal.getPosition("BBCA").getQuantity());

        // Verifikasi audit trail ledger
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testPartialFillAndRemainingQuantityQueue() {
        OrderBook book = facade.getMarketEngine().getOrCreateBook(bbca);
        Account personal = facade.getPersonalAccount();

        // Siapkan seller penjual dengan hanya 100 lembar pada harga 10.300
        Account seller = new Account("ACC-SELLER-PARTIAL", "Penjual Parsial", AccountType.PERSONAL, null);
        seller.creditCash(Money.of(10_000_000, Currency.IDR));
        seller.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100, BigDecimal.valueOf(10000), BigDecimal.valueOf(10300)));
        facade.getAllAccounts(); // memastikan terdaftar

        Order sellOrder = new Order(
                "ORD-SELL-100", "REQ-SELL-100",
                seller.getId(), bbca.getId(),
                OrderSide.SELL, OrderType.LIMIT,
                Money.of(10300, Currency.IDR), null, 100, System.currentTimeMillis(), null
        );
        facade.getMarketEngine().submitOrder(sellOrder, seller, bbca, facade.getLedger(), java.util.Collections.singletonMap(seller.getId(), seller), System.currentTimeMillis());
        assertEquals(OrderStatus.OPEN, sellOrder.getStatus());

        // Pemain beli 300 lembar pada harga 10.300
        Order buyOrder = new Order(
                "ORD-BUY-300", "REQ-BUY-300",
                personal.getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT,
                Money.of(10300, Currency.IDR), null, 300, System.currentTimeMillis(), null
        );

        java.util.Map<String, Account> mapAcc = new java.util.HashMap<>();
        mapAcc.put(personal.getId(), personal);
        mapAcc.put(seller.getId(), seller);

        facade.getMarketEngine().submitOrder(buyOrder, personal, bbca, facade.getLedger(), mapAcc, System.currentTimeMillis());

        // Harus terisi sebagian (100 lembar), sisa 200 lembar masuk antrean bids
        assertEquals(OrderStatus.PARTIALLY_FILLED, buyOrder.getStatus());
        assertEquals(100, buyOrder.getFilledQuantity());
        assertEquals(200, buyOrder.getRemainingQuantity());
        assertEquals(OrderStatus.FILLED, sellOrder.getStatus());

        // Sisa 200 lembar harus ada di bids order book
        List<OrderBook.BookLevel> bids = book.getBidLevels(5);
        assertFalse(bids.isEmpty());
        assertEquals(BigDecimal.valueOf(10300), bids.get(0).price);
        assertEquals(200, bids.get(0).totalQuantity);
    }

    @Test
    public void testOrderCancellationAndCashRelease() {
        Account personal = facade.getPersonalAccount();
        Money initialAvail = personal.getAvailableCash(Currency.IDR);

        // Pasang order beli limit yang tidak langsung match
        Money limitPrice = Money.of(8500, Currency.IDR);
        long qty = 200;
        Order order = new Order(
                "ORD-CANCEL-TEST", "REQ-CANCEL-1",
                personal.getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT,
                limitPrice, null, qty, System.currentTimeMillis(), null
        );

        facade.submitOrder(order);
        assertEquals(OrderStatus.OPEN, order.getStatus());

        // Saldo available berkurang karena di-reserve
        assertTrue(personal.getAvailableCash(Currency.IDR).isLessThan(initialAvail));

        // Batalkan order
        boolean cancelled = facade.cancelOrder(order);
        assertTrue("Pembatalan order harus sukses", cancelled);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        // Saldo available harus kembali utuh seperti semula
        assertEquals(initialAvail, personal.getAvailableCash(Currency.IDR));
        assertEquals(Money.zero(Currency.IDR), personal.getReservedCash(Currency.IDR));
    }

    @Test
    public void testAuctionInvalidLaterSellerCannotPartiallySettleOrReopen() {
        MarketEngine engine = facade.getMarketEngine();
        engine.haltTrading(bbca.getId());
        Account buyer = facade.getPersonalAccount();
        Account first = facade.createClientAccount("First auction seller", null, Money.of(1000000, Currency.IDR));
        Account second = facade.createClientAccount("Second auction seller", null, Money.of(1000000, Currency.IDR));
        java.util.Map<String, Account> accounts = new java.util.HashMap<>();
        for (Account account : facade.getAllAccounts()) accounts.put(account.getId(), account);
        Order buy = Order.createLimit("AUC-ATOMIC-BUY", bbca.getId(), buyer.getId(), OrderSide.BUY, Money.of(10500, Currency.IDR), 200, 1L);
        assertTrue(facade.submitOrder(buy).isSuccess);
        for (Account seller : new Account[]{first, second}) {
            seller.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100, new BigDecimal("10000"), new BigDecimal("10400")));
            assertTrue(facade.submitOrder(Order.createLimit("AUC-ATOMIC-" + seller.getId(), bbca.getId(), seller.getId(),
                    OrderSide.SELL, Money.of(10400, Currency.IDR), 100, 2L)).isSuccess);
        }
        second.removePosition(bbca.getId());
        Money cash = buyer.getTotalCash(Currency.IDR);
        Money reserved = buyer.getReservedCash(Currency.IDR);
        int entries = facade.getLedger().getEntries().size();
        Money price = bbca.getCurrentPrice();
        assertEquals(0, engine.resumeFromHaltWithAuction(bbca, facade.getLedger(), accounts, 3L));
        assertEquals(0, buy.getFilledQuantity());
        assertNull(buyer.getPosition(bbca.getId()));
        assertEquals(100, first.getPosition(bbca.getId()).getQuantity());
        assertEquals(cash, buyer.getTotalCash(Currency.IDR));
        assertEquals(reserved, buyer.getReservedCash(Currency.IDR));
        assertEquals(entries, facade.getLedger().getEntries().size());
        assertEquals(price, bbca.getCurrentPrice());
        assertTrue(engine.isHalted(bbca.getId()));
        assertTrue(engine.getBook(bbca.getId()).getTradeHistory().isEmpty());
    }

    @Test
    public void testAuctionCannotTradeWithOwnAccountOrReleaseHalt() {
        MarketEngine engine = facade.getMarketEngine();
        engine.haltTrading(bbca.getId());
        Account account = facade.getPersonalAccount();
        account.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100, new BigDecimal("10000"), new BigDecimal("10400")));
        Order buy = Order.createLimit("AUC-SELF-BUY", bbca.getId(), account.getId(), OrderSide.BUY, Money.of(10500, Currency.IDR), 100, 1L);
        Order sell = Order.createLimit("AUC-SELF-SELL", bbca.getId(), account.getId(), OrderSide.SELL, Money.of(10400, Currency.IDR), 100, 2L);
        assertTrue(facade.submitOrder(buy).isSuccess);
        assertTrue(facade.submitOrder(sell).isSuccess);
        Money cash = account.getTotalCash(Currency.IDR);
        Money reserved = account.getReservedCash(Currency.IDR);
        int entries = facade.getLedger().getEntries().size();
        assertEquals(0, engine.resumeFromHaltWithAuction(bbca, facade.getLedger(), java.util.Collections.singletonMap(account.getId(), account), 3L));
        assertEquals(0, buy.getFilledQuantity());
        assertEquals(0, sell.getFilledQuantity());
        assertEquals(cash, account.getTotalCash(Currency.IDR));
        assertEquals(reserved, account.getReservedCash(Currency.IDR));
        assertEquals(entries, facade.getLedger().getEntries().size());
        assertTrue(engine.isHalted(bbca.getId()));
    }

    @Test
    public void testCircuitBreakerHaltAndCallAuctionReopening() {
        MarketEngine engine = facade.getMarketEngine();
        OrderBook book = engine.getOrCreateBook(bbca);
        Account personal = facade.getPersonalAccount();

        // 1. Picu Circuit Breaker / Halt
        engine.haltTrading(bbca.getId());
        assertEquals(TradingHaltState.SUSPENDED_CIRCUIT_BREAKER, book.getHaltState());

        // Market order harus ditolak saat Halt
        Order marketOrder = new Order(
                "ORD-MKT-HALT", "REQ-MKT-HALT",
                personal.getId(), bbca.getId(),
                OrderSide.BUY, OrderType.MARKET,
                null, null, 100, System.currentTimeMillis(), null
        );
        MatchingEngine.OrderResult mktRes = facade.submitOrder(marketOrder);
        assertFalse("Market order harus ditolak saat halt", mktRes.isSuccess);
        assertEquals(OrderStatus.REJECTED, marketOrder.getStatus());

        // 2. Limit order masuk antrean lelang
        Order limitBuy = new Order(
                "ORD-AUCTION-BUY", "REQ-AUC-BUY",
                personal.getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT,
                Money.of(10500, Currency.IDR), null, 100, System.currentTimeMillis(), null
        );
        MatchingEngine.OrderResult limRes = facade.submitOrder(limitBuy);
        assertTrue(limRes.isSuccess); // Diterima untuk lelang
        assertTrue(limRes.fills.isEmpty());

        // Buat seller yang juga memasang order jual limit saat suspensi
        Account seller = new Account("ACC-SELLER-AUC", "Penjual Lelang", AccountType.PERSONAL, null);
        seller.creditCash(Money.of(10_000_000, Currency.IDR));
        seller.addPosition(new Position("BBCA", "IDX", Currency.IDR, 100, BigDecimal.valueOf(10000), BigDecimal.valueOf(10400)));

        Order limitSell = new Order(
                "ORD-AUCTION-SELL", "REQ-AUC-SELL",
                seller.getId(), bbca.getId(),
                OrderSide.SELL, OrderType.LIMIT,
                Money.of(10400, Currency.IDR), null, 100, System.currentTimeMillis(), null
        );
        engine.submitOrder(limitSell, seller, bbca, facade.getLedger(), java.util.Collections.singletonMap(seller.getId(), seller), System.currentTimeMillis());

        // 3. Jalankan Call Auction pembukaan kembali
        java.util.Map<String, Account> map = new java.util.HashMap<>();
        map.put(personal.getId(), personal);
        map.put(seller.getId(), seller);

        int auctionMatches = engine.resumeFromHaltWithAuction(bbca, facade.getLedger(), map, System.currentTimeMillis());
        assertTrue("Harus terjadi eksekusi lelang bersilangan", auctionMatches > 0);
        assertEquals(TradingHaltState.NORMAL, book.getHaltState());
        assertEquals(OrderStatus.FILLED, limitBuy.getStatus());
        assertEquals(OrderStatus.FILLED, limitSell.getStatus());
        assertEquals(Money.of(10450, Currency.IDR), book.getLastTradedPrice()); // Rata-rata clearing price 10400 dan 10500

        assertTrue(facade.reconcileAll());
    }
}
