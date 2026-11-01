package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Pengujian Kritis Standar A3 untuk Phase 03 (PHASE-QUALITY-STANDARDS.md):
 * Minimal 10 variasi urutan yang dapat direproduksi, masing-masing minimal 100 aksi bermakna,
 * dengan invariant diperiksa setelah SETIAP aksi.
 * Cakupan: matching engine, order book, partial fill, cancellation, halt/reopen auction,
 * sesi bursa, idempotensi retry, dan konsistensi ledger/save.
 */
public class MarketMatchingOrderHaltCriticalSequenceTest {
    private File testDir;
    private SaveManager saveManager;
    private InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "matching_seq_test_" + System.currentTimeMillis());
        testDir.mkdirs();
        saveManager = SaveManager.forDirectory(testDir);

        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (universeFile.exists()) {
            String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
            catalog = InstrumentCatalog.fromJsonString(jsonUniverse);
        }
    }

    @After
    public void tearDown() {
        deleteDir(testDir);
    }

    private void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) for (File f : files) deleteDir(f);
        }
        dir.delete();
    }

    private void assertInvariants(TradingFacade facade, String context) {
        Account personal = facade.getPersonalAccount();
        assertNotNull("Akun personal wajib ada: " + context, personal);

        // 1. Audit pembukuan double-entry
        assertTrue("Audit ledger rekonsiliasi gagal: " + context, facade.reconcileAll());

        // 2. Invariant kas IDR
        Money totalIdr = personal.getTotalCash(Currency.IDR);
        Money availIdr = personal.getAvailableCash(Currency.IDR);
        Money resIdr = personal.getReservedCash(Currency.IDR);
        assertEquals("Available + Reserved harus sama dengan Total IDR: " + context,
                totalIdr, availIdr.add(resIdr));
        assertFalse("Total IDR negatif: " + context, totalIdr.isNegative());
        assertFalse("Available IDR negatif: " + context, availIdr.isNegative());
        assertFalse("Reserved IDR negatif: " + context, resIdr.isNegative());

        // 3. Invariant posisi saham tidak boleh negatif
        for (Position p : personal.getPositions().values()) {
            assertTrue("Kuantitas posisi tidak boleh negatif: " + p.getTicker() + " pada " + context,
                    p.getQuantity() >= 0);
        }
    }

    private void run100TradingActionsForSeed(long seed, int sequenceNumber) throws Exception {
        TradingFacade facade = new TradingFacade(saveManager);
        if (catalog != null) facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(100_000_000));
        GameTestSupport.unlockManager(facade);
        GameTestSupport.unlockIdxLevelFourForStressFixture(facade);

        Instrument bbca = catalog.getById("IDX:BBCA");
        Instrument tlkm = catalog.getById("IDX:TLKM");
        Instrument[] instruments = {bbca, tlkm};

        Random rnd = new Random(seed);
        String pId = facade.getPersonalAccount().getId();
        List<Order> activeOrders = new ArrayList<>();

        int actionsCount = 0;
        int step = 0;
        while (actionsCount < 100) {
            step++;
            int op = rnd.nextInt(10);
            String ctx = String.format("Seq %d (Seed %d) Step %d Op %d", sequenceNumber, seed, step, op);
            Instrument target = instruments[rnd.nextInt(instruments.length)];

            switch (op) {
                case 0: { // Generate NPC Liquidity
                    facade.generateNpcLiquidity();
                    actionsCount++;
                    break;
                }
                case 1: { // Submit Limit Buy Order
                    long avail = facade.getPersonalAccount().getAvailableCash(Currency.IDR).getAmount().longValue();
                    Money curPrice = target.getCurrentPrice();
                    if (avail > curPrice.getAmount().longValue() * 100) {
                        long tick = target.getExchange().getTickSize(curPrice.getAmount()).longValue();
                        long base = curPrice.getAmount().longValue() + (rnd.nextInt(5) - 2) * tick;
                        long bidPriceVal = Math.max(tick, (base / tick) * tick);
                        Money bidPrice = Money.of(bidPriceVal, Currency.IDR);
                        String reqId = "BUY-LIMIT-" + seed + "-" + step;
                        Order order = new Order(
                                "ORD-" + reqId, reqId,
                                pId, target.getId(),
                                OrderSide.BUY, OrderType.LIMIT,
                                bidPrice, null, 100, System.currentTimeMillis(), null
                        );
                        MatchingEngine.OrderResult res = facade.submitOrder(order);
                        assertTrue("Submit buy order harus sukses: " + res.message, res.isSuccess);
                        if (order.getStatus() == OrderStatus.OPEN || order.getStatus() == OrderStatus.PARTIALLY_FILLED) {
                            activeOrders.add(order);
                        }
                        actionsCount++;
                    }
                    break;
                }
                case 2: { // Submit Limit Sell Order jika punya posisi dan lembar bebas >= 100
                    Position pos = facade.getPersonalAccount().getPosition(target.getId());
                    if (pos != null && pos.getQuantity() >= 100) {
                        long committedShares = 0;
                        for (Order o : activeOrders) {
                            if (o.isSell() && o.getInstrumentId().equals(target.getId()) && !o.getStatus().isFinal()) {
                                committedShares += o.getRemainingQuantity();
                            }
                        }
                        if (pos.getQuantity() - committedShares >= 100) {
                            long tick = target.getExchange().getTickSize(target.getCurrentPrice().getAmount()).longValue();
                            long base = target.getCurrentPrice().getAmount().longValue() + rnd.nextInt(5) * tick;
                            long askPriceVal = Math.max(tick, (base / tick) * tick);
                            Money askPrice = Money.of(askPriceVal, Currency.IDR);
                            String reqId = "SELL-LIMIT-" + seed + "-" + step;
                            Order order = new Order(
                                    "ORD-" + reqId, reqId,
                                    pId, target.getId(),
                                    OrderSide.SELL, OrderType.LIMIT,
                                    askPrice, null, 100, System.currentTimeMillis(), null
                            );
                            MatchingEngine.OrderResult res = facade.submitOrder(order);
                            if (res.isSuccess) {
                                if (order.getStatus() == OrderStatus.OPEN || order.getStatus() == OrderStatus.PARTIALLY_FILLED) {
                                    activeOrders.add(order);
                                }
                                actionsCount++;
                            } else if (res.message != null && res.message.contains("Order bersilangan")) {
                                // Self-trading dicegah oleh domain secara sah
                                actionsCount++;
                            } else {
                                fail("Submit sell order gagal dengan error tidak terduga: " + res.message);
                            }
                        }
                    }
                    break;
                }
                case 3: { // Cancel salah satu active order jika ada
                    if (!activeOrders.isEmpty()) {
                        int idx = rnd.nextInt(activeOrders.size());
                        Order toCancel = activeOrders.remove(idx);
                        Order inFacade = facade.getOrderManager().getOrderByRequest(toCancel.getRequestId());
                        if (inFacade != null && (inFacade.getStatus() == OrderStatus.OPEN || inFacade.getStatus() == OrderStatus.PARTIALLY_FILLED)) {
                            boolean ok = facade.cancelOrder(inFacade);
                            if (ok) {
                                assertEquals(OrderStatus.CANCELLED, inFacade.getStatus());
                                actionsCount++;
                            }
                        }
                    }
                    break;
                }
                case 4: { // Advance time (memicu trigger checking & matching session)
                    int sec = 10 + rnd.nextInt(60);
                    facade.tick(sec);
                    actionsCount++;
                    break;
                }
                case 5: { // Halt dan Reopen Auction
                    boolean isHalted = facade.getMarketEngine().isHalted(target.getId());
                    if (!isHalted) {
                        facade.getMarketEngine().haltTrading(target.getId());
                        assertTrue(facade.getMarketEngine().isHalted(target.getId()));
                        actionsCount++;
                    } else {
                        Map<String, Account> map = new HashMap<>();
                        for (Account a : facade.getAllAccounts()) map.put(a.getId(), a);
                        facade.getMarketEngine().resumeFromHaltWithAuction(target, facade.getLedger(), map, System.currentTimeMillis());
                        facade.getMarketEngine().resumeTrading(target.getId());
                        assertFalse(facade.getMarketEngine().isHalted(target.getId()));
                        actionsCount++;
                    }
                    break;
                }
                case 6: { // Boundary: Tolak submit order dengan quantity 0 atau negatif
                    String reqId = "INVALID-QTY-" + seed + "-" + step;
                    Order invalid = new Order(
                            "ORD-" + reqId, reqId,
                            pId, target.getId(),
                            OrderSide.BUY, OrderType.LIMIT,
                            target.getCurrentPrice(), null, 0, System.currentTimeMillis(), null
                    );
                    MatchingEngine.OrderResult res = facade.submitOrder(invalid);
                    assertFalse("Quantity 0 harus ditolak: " + ctx, res.isSuccess);
                    assertEquals(OrderStatus.REJECTED, invalid.getStatus());
                    actionsCount++;
                    break;
                }
                case 7: { // Idempotensi: Submit ulang order yang sama tidak boleh menduplikasi eksekusi
                    long avail = facade.getPersonalAccount().getAvailableCash(Currency.IDR).getAmount().longValue();
                    if (avail > target.getCurrentPrice().getAmount().longValue() * 100) {
                        String reqId = "IDEMP-" + seed + "-" + step;
                        Order order = new Order(
                                "ORD-" + reqId, reqId,
                                pId, target.getId(),
                                OrderSide.BUY, OrderType.LIMIT,
                                target.getCurrentPrice(), null, 100, System.currentTimeMillis(), null
                        );
                        MatchingEngine.OrderResult r1 = facade.submitOrder(order);
                        int entriesBefore = facade.getLedger().getEntries().size();
                        // Retry submit order dengan ID sama
                        MatchingEngine.OrderResult r2 = facade.submitOrder(order);
                        assertEquals("Ledger entries tidak boleh bertambah saat retry order sama",
                                entriesBefore, facade.getLedger().getEntries().size());
                        actionsCount++;
                    }
                    break;
                }
                case 8: { // Save state
                    facade.save();
                    actionsCount++;
                    break;
                }
                case 9: { // Save dan Reload roundtrip
                    facade.save();
                    TradingFacade reloaded = new TradingFacade(saveManager);
                    if (catalog != null) reloaded.setCatalog(catalog);
                    assertTrue("Load harus berhasil pada " + ctx, reloaded.load());
                    assertInvariants(reloaded, ctx + " [Reloaded]");
                    facade = reloaded;
                    actionsCount++;
                    break;
                }
            }

            // Invariant wajib diaudit setelah SETIAP aksi
            assertInvariants(facade, ctx);
        }

        assertTrue("Minimal 100 aksi bermakna harus tercapai", actionsCount >= 100);
    }

    @Test
    public void testMarketSequence01() throws Exception { run100TradingActionsForSeed(1101L, 1); }

    @Test
    public void testMarketSequence02() throws Exception { run100TradingActionsForSeed(1202L, 2); }

    @Test
    public void testMarketSequence03() throws Exception { run100TradingActionsForSeed(1303L, 3); }

    @Test
    public void testMarketSequence04() throws Exception { run100TradingActionsForSeed(1404L, 4); }

    @Test
    public void testMarketSequence05() throws Exception { run100TradingActionsForSeed(1505L, 5); }

    @Test
    public void testMarketSequence06() throws Exception { run100TradingActionsForSeed(1606L, 6); }

    @Test
    public void testMarketSequence07() throws Exception { run100TradingActionsForSeed(1707L, 7); }

    @Test
    public void testMarketSequence08() throws Exception { run100TradingActionsForSeed(1808L, 8); }

    @Test
    public void testMarketSequence09() throws Exception { run100TradingActionsForSeed(1909L, 9); }

    @Test
    public void testMarketSequence10() throws Exception { run100TradingActionsForSeed(2010L, 10); }
}
