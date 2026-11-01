package com.tradingglobal3d.core;

import com.tradingglobal3d.core.corporate.CorporateActionService;
import com.tradingglobal3d.core.corporate.IpoOffering;
import com.tradingglobal3d.core.corporate.IpoSubscription;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.league.LeagueCompetitor;
import com.tradingglobal3d.core.league.LeagueManager;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.research.NewsArticle;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Uji Komprehensif Perbaikan Review APK-11 & Penguatan Game Saham (LT-01 s/d LT-32):
 * 1. LT-05 - LT-08: Eliminasi partial fill pada kombinasi multi-lawan (ProbeAon).
 * 2. LT-09 - LT-12: Sinkronisasi identitas & status order Market saat pasar tutup (ProbeClosedMarket).
 * 3. LT-17 - LT-20: Peringkat akurat & data finansial/ledger aktual profil NPC.
 * 4. LT-21 - LT-24: Batas fase penawaran & pembatalan IPO terverifikasi.
 * 5. LT-29: Audit pencegahan kebocoran informasi berita sebelum publikasi.
 */
public class Apk11IndependentReviewFixesTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void playerLimitBuyDoesNotPartiallySettleWhenOnlyOneThirdIsAvailable() {
        Money price = Money.of(1000, Currency.IDR);
        Instrument inst = new Instrument("IDX:TEST", "TEST", "Test", MarketExchange.IDX, "Test", Currency.IDR, 100, 1000000, true, price);
        Account npc = new Account("npc", "NPC", AccountType.FIRM, null);
        Account player = new Account("player", "Player", AccountType.PERSONAL, null);
        npc.addPosition(new Position("TEST", "IDX", Currency.IDR, 100, new BigDecimal("1000"), new BigDecimal("1000")));
        player.creditCash(Money.of(10_000_000, Currency.IDR));
        Map<String, Account> accounts = new HashMap<>();
        accounts.put(npc.getId(), npc);
        accounts.put(player.getId(), player);
        OrderBook book = new OrderBook(inst.getId(), price);
        MatchingEngine engine = new MatchingEngine();
        EconomyLedger ledger = new EconomyLedger();
        Order maker = Order.createLimit("maker", inst.getId(), npc.getId(), OrderSide.SELL, price, 100, 1);
        assertTrue(engine.submitOrder(maker, npc, inst, book, ledger, accounts, 1).isSuccess);
        Money before = player.getTotalCash(Currency.IDR);
        Order buy = Order.createLimit("buyer", inst.getId(), player.getId(), OrderSide.BUY, price, 300, 2);
        MatchingEngine.OrderResult result = engine.submitOrder(buy, player, inst, book, ledger, accounts, 2);
        assertTrue(result.isSuccess);
        assertEquals(OrderStatus.OPEN, buy.getStatus());
        assertEquals(0, buy.getFilledQuantity());
        assertTrue(result.fills.isEmpty());
        assertEquals(before, player.getTotalCash(Currency.IDR));
        assertEquals(100, npc.getPosition(inst.getId()).getQuantity());
        assertTrue(ledger.getEntries().isEmpty());
    }

    @Test
    public void playerMarketBuyDoesNotPartiallySettleWhenLiquidityIsInsufficient() {
        Money price = Money.of(1000, Currency.IDR);
        Instrument inst = new Instrument("IDX:TEST", "TEST", "Test", MarketExchange.IDX, "Test", Currency.IDR, 100, 1000000, true, price);
        Account npc = new Account("npc", "NPC", AccountType.FIRM, null);
        Account player = new Account("player", "Player", AccountType.PERSONAL, null);
        npc.addPosition(new Position("TEST", "IDX", Currency.IDR, 100, new BigDecimal("1000"), new BigDecimal("1000")));
        player.creditCash(Money.of(10_000_000, Currency.IDR));
        Map<String, Account> accounts = new HashMap<>();
        accounts.put(npc.getId(), npc);
        accounts.put(player.getId(), player);
        OrderBook book = new OrderBook(inst.getId(), price);
        MatchingEngine engine = new MatchingEngine();
        EconomyLedger ledger = new EconomyLedger();
        Order maker = Order.createLimit("maker", inst.getId(), npc.getId(), OrderSide.SELL, price, 100, 1);
        assertTrue(engine.submitOrder(maker, npc, inst, book, ledger, accounts, 1).isSuccess);
        Money before = player.getTotalCash(Currency.IDR);
        Order buy = new Order("market-buy", "market-buy", player.getId(), inst.getId(), OrderSide.BUY,
                OrderType.MARKET, null, null, 300, 2, null);
        MatchingEngine.OrderResult result = engine.submitOrder(buy, player, inst, book, ledger, accounts, 2);
        assertEquals(0, buy.getFilledQuantity());
        assertTrue(result.fills.isEmpty());
        assertEquals(before, player.getTotalCash(Currency.IDR));
        assertEquals(Money.zero(Currency.IDR), player.getReservedCash(Currency.IDR));
        assertEquals(100, npc.getPosition(inst.getId()).getQuantity());
        assertTrue(ledger.getEntries().isEmpty());
    }

    @Test
    public void playerMarketSellDoesNotPartiallySettleWhenLiquidityIsInsufficient() {
        Money price = Money.of(1000, Currency.IDR);
        Instrument inst = new Instrument("IDX:TEST", "TEST", "Test", MarketExchange.IDX, "Test", Currency.IDR, 100, 1000000, true, price);
        Account npc = new Account("npc", "NPC", AccountType.FIRM, null);
        Account player = new Account("player", "Player", AccountType.PERSONAL, null);
        npc.creditCash(Money.of(1_000_000, Currency.IDR));
        player.addPosition(new Position("TEST", "IDX", Currency.IDR, 300, new BigDecimal("1000"), new BigDecimal("1000")));
        Map<String, Account> accounts = new HashMap<>();
        accounts.put(npc.getId(), npc);
        accounts.put(player.getId(), player);
        OrderBook book = new OrderBook(inst.getId(), price);
        MatchingEngine engine = new MatchingEngine();
        EconomyLedger ledger = new EconomyLedger();
        Order maker = Order.createLimit("maker-buy", inst.getId(), npc.getId(), OrderSide.BUY, price, 100, 1);
        assertTrue(engine.submitOrder(maker, npc, inst, book, ledger, accounts, 1).isSuccess);
        Money npcTotal = npc.getTotalCash(Currency.IDR);
        Money npcReserved = npc.getReservedCash(Currency.IDR);
        Money playerTotal = player.getTotalCash(Currency.IDR);
        Order sell = new Order("market-sell", "market-sell", player.getId(), inst.getId(), OrderSide.SELL,
                OrderType.MARKET, null, null, 300, 2, null);
        MatchingEngine.OrderResult result = engine.submitOrder(sell, player, inst, book, ledger, accounts, 2);
        assertEquals(0, sell.getFilledQuantity());
        assertTrue(result.fills.isEmpty());
        assertEquals(playerTotal, player.getTotalCash(Currency.IDR));
        assertEquals(300, player.getPosition(inst.getId()).getQuantity());
        assertEquals(npcTotal, npc.getTotalCash(Currency.IDR));
        assertEquals(npcReserved, npc.getReservedCash(Currency.IDR));
        assertEquals(0, maker.getFilledQuantity());
        assertTrue(ledger.getEntries().isEmpty());
    }

    @Test
    public void playerMarketBuySettlesCompletelyWhenLiquidityIsSufficient() {
        Money price = Money.of(1000, Currency.IDR);
        Instrument inst = new Instrument("IDX:TEST", "TEST", "Test", MarketExchange.IDX, "Test", Currency.IDR, 100, 1000000, true, price);
        Account npc = new Account("npc", "NPC", AccountType.FIRM, null);
        Account player = new Account("player", "Player", AccountType.PERSONAL, null);
        npc.addPosition(new Position("TEST", "IDX", Currency.IDR, 300, new BigDecimal("1000"), new BigDecimal("1000")));
        player.creditCash(Money.of(10_000_000, Currency.IDR));
        Map<String, Account> accounts = new HashMap<>();
        accounts.put(npc.getId(), npc);
        accounts.put(player.getId(), player);
        OrderBook book = new OrderBook(inst.getId(), price);
        MatchingEngine engine = new MatchingEngine();
        EconomyLedger ledger = new EconomyLedger();
        Order maker = Order.createLimit("maker", inst.getId(), npc.getId(), OrderSide.SELL, price, 300, 1);
        assertTrue(engine.submitOrder(maker, npc, inst, book, ledger, accounts, 1).isSuccess);
        Order buy = new Order("market-buy-full", "market-buy-full", player.getId(), inst.getId(), OrderSide.BUY,
                OrderType.MARKET, null, null, 300, 2, null);
        MatchingEngine.OrderResult result = engine.submitOrder(buy, player, inst, book, ledger, accounts, 2);
        assertTrue(result.isSuccess);
        assertEquals(OrderStatus.FILLED, buy.getStatus());
        assertEquals(300, buy.getFilledQuantity());
        assertEquals(300, player.getPosition(inst.getId()).getQuantity());
        assertEquals(0, npc.getPosition(inst.getId()).getQuantity());
        assertEquals(Money.of(9_699_550, Currency.IDR), player.getTotalCash(Currency.IDR));
        assertEquals(Money.of(299_250, Currency.IDR), npc.getTotalCash(Currency.IDR));
        assertEquals(Money.zero(Currency.IDR), player.getReservedCash(Currency.IDR));
        assertEquals(4, ledger.getEntries().size());
        assertEquals(1, result.fills.size());
    }

    @Test
    public void multiCounterpartyPlayerBuyNeverSettlesOnlyPartOfOrder() {
        Money price = Money.of(1000, Currency.IDR);
        Instrument inst = new Instrument("IDX:TEST", "TEST", "Test", MarketExchange.IDX, "Test", Currency.IDR, 100, 1000000, true, price);
        Account npc = new Account("npc", "NPC", AccountType.FIRM, null);
        Account client = new Account("client", "Client", AccountType.CLIENT, null);
        Account player = new Account("player", "Player", AccountType.PERSONAL, null);
        npc.addPosition(new Position("TEST", "IDX", Currency.IDR, 100, new BigDecimal("1000"), new BigDecimal("1000")));
        client.addPosition(new Position("TEST", "IDX", Currency.IDR, 300, new BigDecimal("1000"), new BigDecimal("1000")));
        player.creditCash(Money.of(10_000_000, Currency.IDR));
        Map<String, Account> accounts = new HashMap<>();
        accounts.put(npc.getId(), npc);
        accounts.put(client.getId(), client);
        accounts.put(player.getId(), player);
        OrderBook book = new OrderBook(inst.getId(), price);
        MatchingEngine engine = new MatchingEngine();
        EconomyLedger ledger = new EconomyLedger();
        assertTrue(engine.submitOrder(Order.createLimit("n", inst.getId(), npc.getId(), OrderSide.SELL, price, 100, 1), npc, inst, book, ledger, accounts, 1).isSuccess);
        assertTrue(engine.submitOrder(Order.createLimit("c", inst.getId(), client.getId(), OrderSide.SELL, price, 300, 2), client, inst, book, ledger, accounts, 2).isSuccess);
        Money before = player.getTotalCash(Currency.IDR);
        Order buy = Order.createLimit("p", inst.getId(), player.getId(), OrderSide.BUY, price, 300, 3);
        MatchingEngine.OrderResult result = engine.submitOrder(buy, player, inst, book, ledger, accounts, 3);
        assertTrue(result.isSuccess);
        assertTrue("Pemain hanya boleh terisi nol atau penuh", buy.getFilledQuantity() == 0 || buy.getFilledQuantity() == 300);
        if (buy.getFilledQuantity() == 0) {
            assertTrue(result.fills.isEmpty());
            assertEquals(before, player.getTotalCash(Currency.IDR));
            assertTrue(ledger.getEntries().isEmpty());
        }
    }

    @Test
    public void testProbeAonMultiCounterpartyAllOrNoneExecution() {
        Money price = Money.of(1000, Currency.IDR);
        Instrument inst = new Instrument("IDX:TEST", "TEST", "Test", MarketExchange.IDX, "Test", Currency.IDR, 100, 1000000, true, price);
        Account npc = new Account("npc", "NPC", AccountType.FIRM, null);
        Account client = new Account("client", "Client", AccountType.CLIENT, null);
        Account player = new Account("player", "Player", AccountType.PERSONAL, null);

        npc.addPosition(new Position("TEST", "IDX", Currency.IDR, 100, new BigDecimal("1000"), new BigDecimal("1000")));
        client.addPosition(new Position("TEST", "IDX", Currency.IDR, 300, new BigDecimal("1000"), new BigDecimal("1000")));
        player.creditCash(Money.of(10_000_000, Currency.IDR));

        Map<String, Account> accounts = new HashMap<>();
        accounts.put(npc.getId(), npc);
        accounts.put(client.getId(), client);
        accounts.put(player.getId(), player);

        OrderBook book = new OrderBook(inst.getId(), price);
        MatchingEngine engine = new MatchingEngine();
        EconomyLedger ledger = new EconomyLedger();

        // NPC menjual 100
        Order n = Order.createLimit("n", inst.getId(), npc.getId(), OrderSide.SELL, price, 100, 1);
        engine.submitOrder(n, npc, inst, book, ledger, accounts, 1);

        // Client menjual 300
        Order c = Order.createLimit("c", inst.getId(), client.getId(), OrderSide.SELL, price, 300, 2);
        engine.submitOrder(c, client, inst, book, ledger, accounts, 2);

        // Pemain mengajukan BUY 300
        Order p = Order.createLimit("p", inst.getId(), player.getId(), OrderSide.BUY, price, 300, 3);
        MatchingEngine.OrderResult result = engine.submitOrder(p, player, inst, book, ledger, accounts, 3);

        // Verifikasi Plan 12 A03: Order pemain melewati maker NPC 100 dan dipenuhi 300 dari client 300
        assertEquals("Order pemain harus terisi penuh 300 dari penjual yang mencukupi", OrderStatus.FILLED, p.getStatus());
        assertEquals("Kuantitas terisi harus 300", 300, p.getFilledQuantity());
        assertEquals("Kuantitas sisa harus 0", 0, p.getRemainingQuantity());
        assertEquals("Order client 300 harus terisi penuh", OrderStatus.FILLED, c.getStatus());
        assertEquals("Order NPC 100 harus tetap antre di buku order", OrderStatus.OPEN, n.getStatus());
        assertEquals(0, n.getFilledQuantity());
        assertEquals(100, n.getRemainingQuantity());
        assertEquals(300, player.getPosition(inst.getId()).getQuantity());
        assertEquals(0, client.getPosition(inst.getId()).getQuantity());
        assertEquals(Money.of(9_699_550, Currency.IDR), player.getTotalCash(Currency.IDR));
        assertEquals(Money.of(299_250, Currency.IDR), client.getTotalCash(Currency.IDR));
        assertEquals(Money.zero(Currency.IDR), player.getReservedCash(Currency.IDR));
        assertEquals(4, ledger.getEntries().size());
        assertEquals(1, result.fills.size());
    }

    @Test
    public void testProbeClosedMarketSynchronizedStatusAndPortfolio() throws Exception {
        SaveManager saves = SaveManager.forDirectory(temp.newFolder());
        TradingFacade game = new TradingFacade(saves);
        File catFile = new File("src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("android/app/src/main/assets/data/market_universe.json");
        game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(catFile.toPath())));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));

        Account player = game.getPersonalAccount();
        Account npc = game.getAccount("ACC-NPC-MM-01");
        Instrument inst = game.getCatalog().getById("IDX:BBCA");
        Money price = inst.getCurrentPrice();

        // 1. Order MARKET dibuat saat pasar tutup (08:30 WIB)
        Order buy = new Order("CLOSED-MARKET-01", "CLOSED-MARKET-01", player.getId(), inst.getId(), OrderSide.BUY, OrderType.MARKET, null, null, 100, game.getGameTimestamp(), Money.zero(Currency.IDR));
        MatchingEngine.OrderResult queued = game.submitOrder(buy);
        assertTrue(queued.isSuccess);
        assertEquals(OrderStatus.OPEN, buy.getStatus());
        assertTrue("Dana kas harus tertahan saat order dibuat", player.getReservedCash(Currency.IDR).isPositive());

        // Jalur waktu domain harus memproses antrean pembukaan, bukan hanya mengganti jam.
        game.advanceRealMs(37500, true);

        // 3. NPC menjual 100 lembar pada harga pasar
        Order sell = Order.createLimit("NPC-CLOSED-COUNTER-01", inst.getId(), npc.getId(), OrderSide.SELL, price, 100, game.getGameTimestamp());
        MatchingEngine.OrderResult result = game.submitOrder(sell);
        assertTrue(result.isSuccess);

        // 4. Verifikasi status order pemain HARUS sinkron 100% dengan portofolio
        assertEquals("Status order pemain harus FILLED", OrderStatus.FILLED, buy.getStatus());
        assertEquals("Kuantitas terisi harus 100", 100, buy.getFilledQuantity());
        assertEquals("Kuantitas sisa harus 0", 0, buy.getRemainingQuantity());
        assertNotNull("Posisi saham pemain harus tercatat di portofolio", player.getPosition(inst.getId()));
        assertEquals(100, player.getPosition(inst.getId()).getQuantity());
        assertEquals("Dana tertahan harus telah dilepas penuh setelah match", Money.zero(Currency.IDR), player.getReservedCash(Currency.IDR));
    }

    @Test
    public void testNpcProfileExactRankCalculationAndLedgerHistory() throws Exception {
        SaveManager saves = SaveManager.forDirectory(temp.newFolder());
        TradingFacade game = new TradingFacade(saves);
        File catFile = new File("src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("android/app/src/main/assets/data/market_universe.json");
        game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(catFile.toPath())));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));

        // Ambil profil NPC peringkat #2 (Banyu Karang)
        JSONObject p2 = game.getNpcProfile("NPC-02");
        assertTrue(p2.getBoolean("success"));
        assertEquals("Peringkat kekayaan NPC #2 harus 2", 2, p2.getInt("netWorthRank"));
        assertTrue("Harus memuat peringkat return mingguan", p2.has("weeklyReturnRank"));
        assertEquals("NPC-02", p2.getString("id"));
        assertNotNull(p2.getString("cash"));
        assertTrue("Banyu Karang harus memiliki portofolio saham riil (bukan 0)", p2.getJSONArray("positions").length() > 0);
        assertNotEquals("Valuasi saham Banyu Karang tidak boleh Rp 0", "Rp 0", p2.getString("stockValuation"));
        assertNotNull(p2.getJSONArray("recentTransactions"));
    }

    @Test
    public void testAntiFutureLeakOnUnpublishedNewsArticle() throws Exception {
        long gameNow = 1792373400000L;
        long futurePublishedAt = gameNow + 3600_000L; // Terbit 1 jam game ke depan

        NewsArticle futureNews = new NewsArticle(
                "NEWS-SECRET-01", "BBCA Akusisi Bank Digital", "Detail akuisisi...",
                NewsArticle.Category.COMPANY, "BBCA", 0.50, futurePublishedAt
        );

        // Verifikasi kondisi sebelum terbit (General Public Feed)
        JSONObject unpubObj = futureNews.toJsonObject(gameNow);
        assertFalse(unpubObj.getBoolean("isPublished"));
        assertTrue("Headline belum terbit harus menyamarkan isi spesifik", unpubObj.getString("headline").startsWith("[Agenda Rilis]"));
        assertEquals("Ticker emiten tidak boleh bocor sebelum publikasi", JSONObject.NULL, unpubObj.get("relatedTicker"));
        assertEquals("Arah dampak belum terbit harus netral/terjadwal", "Terjadwal", unpubObj.getString("direction"));
        assertEquals("Rentang dampak harus kosong sebelum terbit", "", unpubObj.getString("estimatedRange"));
        assertEquals(0.0, unpubObj.getDouble("sentimentImpact"), 0.001);

        // Verifikasi kondisi setelah terbit
        JSONObject pubObj = futureNews.toJsonObject(futurePublishedAt + 1000L);
        assertTrue(pubObj.getBoolean("isPublished"));
        assertEquals("BBCA", pubObj.getString("relatedTicker"));
        assertEquals("BBCA Akusisi Bank Digital", pubObj.getString("headline"));
        assertTrue(pubObj.getDouble("sentimentImpact") > 0);
    }

    @Test
    public void testPlayerSellOrderAllOrNoneMultiCounterparty() {
        Money price = Money.of(1000, Currency.IDR);
        Instrument inst = new Instrument("IDX:TEST2", "TEST2", "Test 2", MarketExchange.IDX, "Test", Currency.IDR, 100, 1000000, true, price);
        Account npc = new Account("npc2", "NPC 2", AccountType.FIRM, null);
        Account client = new Account("client2", "Client 2", AccountType.CLIENT, null);
        Account player = new Account("player2", "Player 2", AccountType.PERSONAL, null);

        npc.creditCash(Money.of(10_000_000, Currency.IDR));
        client.creditCash(Money.of(10_000_000, Currency.IDR));
        player.addPosition(new Position("TEST2", "IDX", Currency.IDR, 300, new BigDecimal("1000"), new BigDecimal("1000")));

        Map<String, Account> accounts = new HashMap<>();
        accounts.put(npc.getId(), npc);
        accounts.put(client.getId(), client);
        accounts.put(player.getId(), player);

        OrderBook book = new OrderBook(inst.getId(), price);
        MatchingEngine engine = new MatchingEngine();
        EconomyLedger ledger = new EconomyLedger();

        // 1. NPC memasang BUY 100 di harga 1000
        Order n = Order.createLimit("n2", inst.getId(), npc.getId(), OrderSide.BUY, price, 100, 1);
        engine.submitOrder(n, npc, inst, book, ledger, accounts, 1);

        // 2. Client memasang BUY 300 di harga 1000
        Order c = Order.createLimit("c2", inst.getId(), client.getId(), OrderSide.BUY, price, 300, 2);
        engine.submitOrder(c, client, inst, book, ledger, accounts, 2);

        // 3. Pemain memasang SELL 300 di harga 1000
        Order p = Order.createLimit("p2", inst.getId(), player.getId(), OrderSide.SELL, price, 300, 3);
        MatchingEngine.OrderResult result = engine.submitOrder(p, player, inst, book, ledger, accounts, 3);

        // Verifikasi Plan 12 A03 sisi jual: Order jual pemain melewati bid NPC 100 dan dipenuhi 300 dari client 300
        assertEquals("Order jual pemain harus terisi penuh 300", OrderStatus.FILLED, p.getStatus());
        assertEquals("Kuantitas terisi harus 300", 300, p.getFilledQuantity());
        assertEquals("Kuantitas sisa harus 0", 0, p.getRemainingQuantity());
        assertEquals("Order buy client 300 harus terisi penuh", OrderStatus.FILLED, c.getStatus());
        assertEquals("Order buy NPC 100 harus tetap antre di buku order", OrderStatus.OPEN, n.getStatus());
        assertEquals(0, n.getFilledQuantity());
        assertEquals(100, n.getRemainingQuantity());
    }

    @Test
    public void testIpoOfferingPhasesAndStatePersistence() throws Exception {
        CorporateActionService service = new CorporateActionService();
        service.reset(LocalDate.of(2026, 10, 28));

        LocalDate offeringStart = LocalDate.of(2026, 10, 20);
        LocalDate offeringEnd = LocalDate.of(2026, 10, 27);
        LocalDate allotment = LocalDate.of(2026, 10, 28);
        LocalDate listing = LocalDate.of(2026, 10, 29);

        IpoOffering ipo = new IpoOffering(
                "IPO-PHASE", "PHSE", "PT Fase Lengkap Tbk",
                Currency.IDR, Money.of(1000, Currency.IDR), 100, 50_000L,
                offeringStart, offeringEnd, allotment, listing
        );
        service.registerIpoOffering(ipo);

        // Verifikasi boundary masa penawaran
        assertTrue("Hari ini (2026-10-25) harus dalam masa penawaran", ipo.isOfferingPeriod(LocalDate.of(2026, 10, 25)));
        assertFalse("Sebelum masa penawaran harus false", ipo.isOfferingPeriod(offeringStart.minusDays(1)));
        assertFalse("Setelah penutupan penawaran harus false", ipo.isOfferingPeriod(offeringEnd.plusDays(1)));

        // Verifikasi persistensi ekspor & restore
        JSONObject exported = service.exportState();
        CorporateActionService restored = new CorporateActionService();
        restored.restoreState(exported);

        IpoOffering restoredIpo = restored.getIpo("IPO-PHASE");
        assertNotNull(restoredIpo);
        assertEquals(offeringStart, restoredIpo.getOfferingStartDate());
        assertEquals(offeringEnd, restoredIpo.getOfferingEndDate());
        assertEquals(allotment, restoredIpo.getAllotmentDate());
        assertEquals(listing, restoredIpo.getListingDate());
    }

    @Test
    public void testRevA01IpoOrderRejectedOutsideOfferingWindow() {
        CorporateActionService service = new CorporateActionService();
        service.reset(LocalDate.of(2026, 10, 28));

        // Penawaran 3 hari game: 2 Nov s/d 4 Nov 2026, Listing 5 Nov 2026
        IpoOffering ipo = new IpoOffering(
                "IPO-WINDOW", "WNDW", "PT Jendela Waktu Tbk",
                Currency.IDR, Money.of(1000, Currency.IDR), 100, 100_000L,
                LocalDate.of(2026, 11, 5)
        );
        service.registerIpoOffering(ipo);

        Account account = new Account("ACC-USER-WNDW", "Investor", AccountType.PERSONAL, null);
        account.creditCash(Money.of(10_000_000L, Currency.IDR));

        long tBefore = LocalDate.of(2026, 11, 1).atStartOfDay(java.time.ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli();
        long tDuring = LocalDate.of(2026, 11, 3).atStartOfDay(java.time.ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli();
        long tAfter = LocalDate.of(2026, 11, 5).atStartOfDay(java.time.ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli();

        // 1. Pesan sebelum penawaran dibuka -> ditolak
        try {
            service.orderIpo(ipo, account, 1, "REQ-BEFORE", tBefore);
            fail("Pemesanan sebelum masa penawaran harus ditolak");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("belum dibuka"));
        }
        assertEquals(Money.zero(Currency.IDR), account.getReservedCash(Currency.IDR));

        // 2. Pesan saat masa penawaran aktif -> sukses & kas tertahan
        IpoSubscription sub = service.orderIpo(ipo, account, 1, "REQ-DURING", tDuring);
        assertNotNull(sub);
        assertEquals(IpoSubscription.SubscriptionStatus.PENDING, sub.getStatus());
        assertEquals(Money.of(100_000L, Currency.IDR), account.getReservedCash(Currency.IDR));

        // 3. Pesan setelah masa penawaran tutup (pada tanggal listing) -> ditolak
        Account account2 = new Account("ACC-USER-LATE", "Late Investor", AccountType.PERSONAL, null);
        account2.creditCash(Money.of(10_000_000L, Currency.IDR));
        try {
            service.orderIpo(ipo, account2, 1, "REQ-AFTER", tAfter);
            fail("Pemesanan setelah masa penawaran tutup harus ditolak");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("sudah ditutup"));
        }
        assertEquals(Money.zero(Currency.IDR), account2.getReservedCash(Currency.IDR));
    }
}
