package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
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
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Pengujian Kritis Standar A3 untuk Phase 05 (PHASE-QUALITY-STANDARDS.md):
 * Minimal 10 variasi urutan yang dapat direproduksi, masing-masing minimal 100 aksi bermakna,
 * dengan invariant diperiksa setelah SETIAP aksi.
 * Cakupan: Trading lintas mata uang (IDX/US), portofolio, konversi FX multi-currency,
 * boundary insufficient funds, idempotent retry, rekonsiliasi nilai pasar, dan save/load.
 */
public class TradingPortfolioFxCriticalSequenceTest {
    private File testDir;
    private SaveManager saveManager;
    private InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "port_fx_crit_test_" + System.currentTimeMillis());
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
        assertNotNull("Personal account wajib ada: " + context, personal);

        // 1. Audit ledger double-entry
        assertTrue("Audit ledger rekonsiliasi gagal: " + context, facade.reconcileAll());

        // 2. Invariant IDR
        Money totalIdr = personal.getTotalCash(Currency.IDR);
        Money availIdr = personal.getAvailableCash(Currency.IDR);
        Money resIdr = personal.getReservedCash(Currency.IDR);
        assertEquals("Available + Reserved harus sama dengan Total IDR: " + context,
                totalIdr, availIdr.add(resIdr));
        assertFalse("Total IDR negatif: " + context, totalIdr.isNegative());
        assertFalse("Available IDR negatif: " + context, availIdr.isNegative());
        assertFalse("Reserved IDR negatif: " + context, resIdr.isNegative());

        // 3. Invariant USD
        Money totalUsd = personal.getTotalCash(Currency.USD);
        Money availUsd = personal.getAvailableCash(Currency.USD);
        Money resUsd = personal.getReservedCash(Currency.USD);
        assertEquals("Available + Reserved harus sama dengan Total USD: " + context,
                totalUsd, availUsd.add(resUsd));
        assertFalse("Total USD negatif: " + context, totalUsd.isNegative());
        assertFalse("Available USD negatif: " + context, availUsd.isNegative());
        assertFalse("Reserved USD negatif: " + context, resUsd.isNegative());

        // 4. Invariant posisi saham
        for (Position p : personal.getPositions().values()) {
            assertTrue("Kuantitas posisi tidak boleh negatif: " + p.getTicker() + " pada " + context,
                    p.getQuantity() >= 0);
        }
    }

    private void run100TradingFxActionsForSeed(long seed, int sequenceNumber) throws Exception {
        TradingFacade facade = new TradingFacade(saveManager);
        if (catalog != null) facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(150_000_000));
        GameTestSupport.unlockManager(facade);

        Instrument bbca = catalog.getById("IDX:BBCA");
        Instrument aapl = catalog.getById("US:AAPL");

        Random rnd = new Random(seed);
        String pId = facade.getPersonalAccount().getId();
        List<Order> activeOrders = new ArrayList<>();

        int actionsCount = 0;
        int step = 0;
        while (actionsCount < 100) {
            step++;
            int op = rnd.nextInt(10);
            String ctx = String.format("Seq %d (Seed %d) Step %d Op %d", sequenceNumber, seed, step, op);

            switch (op) {
                case 0: { // Konversi FX IDR -> USD
                    long availIdr = facade.getPersonalAccount().getAvailableCash(Currency.IDR).getAmount().longValue();
                    if (availIdr > 20_000_000) {
                        long toConvert = 1_000_000 + rnd.nextInt(5_000_000);
                        String reqId = "FX-IDR2USD-" + seed + "-" + step;
                        var res = facade.convertFx(reqId, pId, Currency.IDR, Currency.USD, Money.of(toConvert, Currency.IDR));
                        assertTrue("Konversi IDR ke USD harus sukses: " + res.message, res.isSuccess);
                        // Idempotent retry
                        var retry = facade.convertFx(reqId, pId, Currency.IDR, Currency.USD, Money.of(toConvert, Currency.IDR));
                        assertTrue("Retry konversi FX harus idempoten", retry.isSuccess);
                        actionsCount++;
                    }
                    break;
                }
                case 1: { // Konversi FX USD -> IDR
                    double availUsd = facade.getPersonalAccount().getAvailableCash(Currency.USD).getAmount().doubleValue();
                    if (availUsd > 50.0) {
                        double maxConvert = Math.min(availUsd - 5.0, 100.0);
                        if (maxConvert > 10.0) {
                            double toConvert = 10.0 + (rnd.nextDouble() * (maxConvert - 10.0));
                            BigDecimal amt = BigDecimal.valueOf(toConvert).setScale(2, java.math.RoundingMode.HALF_UP);
                            String reqId = "FX-USD2IDR-" + seed + "-" + step;
                            var res = facade.convertFx(reqId, pId, Currency.USD, Currency.IDR, new Money(amt, Currency.USD));
                            if (res.isSuccess) {
                                actionsCount++;
                            } else if (res.message != null && res.message.contains("tidak cukup")) {
                                actionsCount++;
                            }
                        }
                    }
                    break;
                }
                case 2: { // Trading IDX (BBCA) Buy Limit
                    long availIdr = facade.getPersonalAccount().getAvailableCash(Currency.IDR).getAmount().longValue();
                    Money curPrice = bbca.getCurrentPrice();
                    if (availIdr > curPrice.getAmount().longValue() * 100) {
                        long tick = bbca.getExchange().getTickSize(curPrice.getAmount()).longValue();
                        long base = curPrice.getAmount().longValue() + (rnd.nextInt(5) - 2) * tick;
                        long bidPriceVal = Math.max(tick, (base / tick) * tick);
                        String reqId = "BUY-IDX-" + seed + "-" + step;
                        Order order = new Order(
                                "ORD-" + reqId, reqId,
                                pId, bbca.getId(),
                                OrderSide.BUY, OrderType.LIMIT,
                                Money.of(bidPriceVal, Currency.IDR), null, 100, System.currentTimeMillis(), null
                        );
                        var res = facade.submitOrder(order);
                        if (res.isSuccess) {
                            if (order.getStatus() == OrderStatus.OPEN || order.getStatus() == OrderStatus.PARTIALLY_FILLED) {
                                activeOrders.add(order);
                            }
                            actionsCount++;
                        }
                    }
                    break;
                }
                case 3: { // Trading US (AAPL) Buy Limit
                    double availUsd = facade.getPersonalAccount().getAvailableCash(Currency.USD).getAmount().doubleValue();
                    Money curPrice = aapl.getCurrentPrice();
                    if (availUsd > curPrice.getAmount().doubleValue() * 2) {
                        double bidPriceVal = Math.max(1.0, curPrice.getAmount().doubleValue() + (rnd.nextInt(5) - 2) * 0.05);
                        String reqId = "BUY-US-" + seed + "-" + step;
                        Order order = new Order(
                                "ORD-" + reqId, reqId,
                                pId, aapl.getId(),
                                OrderSide.BUY, OrderType.LIMIT,
                                Money.of(bidPriceVal, Currency.USD), null, 1, System.currentTimeMillis(), null
                        );
                        var res = facade.submitOrder(order);
                        if (res.isSuccess) {
                            if (order.getStatus() == OrderStatus.OPEN || order.getStatus() == OrderStatus.PARTIALLY_FILLED) {
                                activeOrders.add(order);
                            }
                            actionsCount++;
                        }
                    }
                    break;
                }
                case 4: { // Cancel salah satu active order jika ada
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
                case 5: { // Boundary: Tolak konversi FX melebihi saldo kas tersedia
                    long availIdr = facade.getPersonalAccount().getAvailableCash(Currency.IDR).getAmount().longValue();
                    String reqId = "FX-OVER-" + seed + "-" + step;
                    var res = facade.convertFx(reqId, pId, Currency.IDR, Currency.USD, Money.of(availIdr + 50_000_000, Currency.IDR));
                    assertFalse("Konversi melebihi saldo kas harus ditolak: " + ctx, res.isSuccess);
                    actionsCount++;
                    break;
                }
                case 6: { // Boundary: Tolak konversi FX nominal 0
                    try {
                        facade.getFxService().getOrGenerateQuote(System.currentTimeMillis())
                                .calculatePreview(Currency.IDR, Currency.USD, Money.zero(Currency.IDR));
                        fail("Konversi FX nominal 0 harus ditolak: " + ctx);
                    } catch (IllegalArgumentException expected) {
                        actionsCount++;
                    }
                    break;
                }
                case 7: { // Advance time (market tick & liquidity generation)
                    facade.tick(5);
                    actionsCount++;
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
    public void testTradingFxSequence01() throws Exception { run100TradingFxActionsForSeed(5001L, 1); }

    @Test
    public void testTradingFxSequence02() throws Exception { run100TradingFxActionsForSeed(5002L, 2); }

    @Test
    public void testTradingFxSequence03() throws Exception { run100TradingFxActionsForSeed(5003L, 3); }

    @Test
    public void testTradingFxSequence04() throws Exception { run100TradingFxActionsForSeed(5004L, 4); }

    @Test
    public void testTradingFxSequence05() throws Exception { run100TradingFxActionsForSeed(5005L, 5); }

    @Test
    public void testTradingFxSequence06() throws Exception { run100TradingFxActionsForSeed(5006L, 6); }

    @Test
    public void testTradingFxSequence07() throws Exception { run100TradingFxActionsForSeed(5007L, 7); }

    @Test
    public void testTradingFxSequence08() throws Exception { run100TradingFxActionsForSeed(5008L, 8); }

    @Test
    public void testTradingFxSequence09() throws Exception { run100TradingFxActionsForSeed(5009L, 9); }

    @Test
    public void testTradingFxSequence10() throws Exception { run100TradingFxActionsForSeed(5010L, 10); }
}
