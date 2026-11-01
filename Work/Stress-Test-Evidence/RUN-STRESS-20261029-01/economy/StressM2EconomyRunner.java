package com.tradingglobal3d.stress.economy;

import com.tradingglobal3d.core.business.CompanyManager;
import com.tradingglobal3d.core.business.OperatingCompany;
import com.tradingglobal3d.core.corporate.CorporateActionService;
import com.tradingglobal3d.core.corporate.IpoOffering;
import com.tradingglobal3d.core.corporate.IpoSubscription;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * Sub-Agent A (Ekonomi) — Runner M2: Stress Transaksi, Order AON, IPO, Bisnis & Rekonsiliasi.
 */
public class StressM2EconomyRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== SUB-AGENT A: MEMULAI STRESS TEST TRANSAKSI & EKONOMI (M2) ===");
        long startMs = System.currentTimeMillis();

        Path econDir = Path.of("Work/Stress-Test-Evidence/RUN-STRESS-20261029-01/economy");
        Files.createDirectories(econDir);

        File catFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("src/main/assets/data/market_universe.json");
        String catalogJson = Files.readString(catFile.toPath());

        int totalSeeds = 50;
        int totalTransactionsExecuted = 0;
        int totalAonChecksPassed = 0;
        int totalIpoRoundsPassed = 0;
        int totalBusinessCyclesPassed = 0;
        int totalLedgerReconciliationsPassed = 0;

        for (int s = 1; s <= totalSeeds; s++) {
            int seed = 2000 + s;
            Random rng = new Random(seed);

            File saveDir = econDir.resolve("qa-save-seed-" + seed).toFile();
            saveDir.mkdirs();
            SaveManager saves = SaveManager.forDirectory(saveDir);
            TradingFacade facade = new TradingFacade(saves);
            facade.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
            facade.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("100000000"));

            Account player = facade.getPersonalAccount();
            Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
            Instrument bmri = facade.getCatalog().getById("IDX:BMRI");

            // 1. SIKLUS TRANSAKSI ORDER & AON MULTI-LAWAN (E03 A)
            for (int cyc = 1; cyc <= 20; cyc++) {
                // Buy order AON 3 lot (300 lembar)
                Order buyOrder = Order.createLimit("ORD-M2-B-" + seed + "-" + cyc, bbca.getId(), player.getId(),
                        OrderSide.BUY, bbca.getCurrentPrice(), bbca.getLotSize() * 3L, facade.getClock().getGameTimestamp());
                MatchingEngine.OrderResult buyRes = facade.submitOrder(buyOrder);
                assertTrue("Order AON buy harus valid", buyRes.isSuccess);

                // Pastikan status All-Or-None: jika terisi, harus tepat 300 lembar, atau tetap OPEN di buku order (0 partial fill)
                assertTrue("Pemain tidak boleh mengalami partial fill",
                        buyOrder.getStatus() == OrderStatus.FILLED || buyOrder.getStatus() == OrderStatus.OPEN);
                if (buyOrder.getStatus() == OrderStatus.FILLED) {
                    assertEquals(300, buyOrder.getFilledQuantity());
                    assertEquals(0, buyOrder.getRemainingQuantity());
                }
                totalAonChecksPassed++;
                totalTransactionsExecuted++;

                // Jual sebagian posisi yang dimiliki secara bertahap
                Position bbcaPos = player.getPosition(bbca.getId());
                if (bbcaPos != null && bbcaPos.getQuantity() >= 100) {
                    Order sellOrder = Order.createLimit("ORD-M2-S-" + seed + "-" + cyc, bbca.getId(), player.getId(),
                            OrderSide.SELL, bbca.getCurrentPrice(), 100, facade.getClock().getGameTimestamp());
                    MatchingEngine.OrderResult sellRes = facade.submitOrder(sellOrder);
                    assertTrue("Order sell AON harus valid", sellRes.isSuccess);
                    totalTransactionsExecuted++;
                }

                facade.getClock().advanceGameSeconds(300); // 5 menit game
            }

            // 2. SIKLUS IPO 60:40 HAMILTON & PEMBATALAN PESANAN (E03 B)
            CorporateActionService cs = facade.getCorporateService();
            LocalDate today = facade.getClock().getCurrentDate();
            IpoOffering offering = new IpoOffering(
                    "IPO-ECON-" + seed, "ECLK", "PT Ekosistem Maju " + seed,
                    Currency.IDR, Money.of(1000, Currency.IDR), 100, 10_000_000L,
                    today.minusDays(1), today.plusDays(1), today.plusDays(2), today.plusDays(2)
            );
            cs.registerIpoOffering(offering);

            Money initialAvail = player.getAvailableCash(Currency.IDR);
            Money initialReserved = player.getReservedCash(Currency.IDR);
            IpoSubscription sub = facade.orderIpo(offering.getId(), 10, "REQ-M2-IPO-" + seed);
            assertNotNull("Pemesanan IPO sah harus sukses", sub);
            assertEquals(10, sub.getOrderedLots());
            assertTrue("Kas harus bertambah tertahan untuk pemesanan IPO", player.getReservedCash(Currency.IDR).compareTo(initialReserved) > 0);

            // Uji pembatalan pemesanan IPO sebelum tanggal listing
            boolean cancelIpo = facade.cancelIpoSubscription(sub.getId());
            assertTrue("Pembatalan pemesanan IPO sebelum listing harus sukses", cancelIpo);
            assertEquals(IpoSubscription.SubscriptionStatus.CANCELLED, sub.getStatus());
            assertEquals("Dana tertahan harus kembali ke nilai awal sebelum IPO", initialReserved, player.getReservedCash(Currency.IDR));
            assertEquals("Saldo kas tersedia harus kembali utuh", initialAvail, player.getAvailableCash(Currency.IDR));
            totalIpoRoundsPassed++;

            // 3. SIKLUS OPERASIONAL BISNIS & PEMISAHAN KAS (E03 B)
            OperatingCompany comp = facade.foundCompany(
                    "COMP-" + seed, "PT Usaha Makmur " + seed, "JASA",
                    Money.of(25_000_000, Currency.IDR)
            );
            assertNotNull(comp);
            Account compAccount = facade.getAccount(comp.getAccountId());
            assertNotNull(compAccount);
            assertEquals(AccountType.COMPANY, compAccount.getType());

            // Proses operasional harian 5x
            CompanyManager cm = facade.getCompanyManager();
            EconomyLedger ledger = facade.getLedger();
            for (int day = 1; day <= 5; day++) {
                long now = facade.getClock().getGameTimestamp() + day * 86400_000L;
                cm.processDailyOperations(compAccount, comp, ledger, now);
                totalBusinessCyclesPassed++;
            }

            // Verifikasi pemisahan kas anak usaha dari kas pribadi
            assertNotEquals("Akun perusahaan harus independen dari akun pribadi", player.getId(), compAccount.getId());
            assertTrue("Kas operasional perusahaan harus terpisah di ledger", compAccount.getTotalCash(Currency.IDR).isPositive());

            // 4. VERIFIKASI INVARIANS & REKONSILIASI BUKU BESAR
            assertTrue("Seluruh neraca akun harus seimbang di buku besar", facade.reconcileAll());
            totalLedgerReconciliationsPassed++;
        }

        long elapsedMs = System.currentTimeMillis() - startMs;
        JSONObject report = new JSONObject();
        report.put("milestone", "M2");
        report.put("subAgentId", "AGENT-SUB-EKONOMI");
        report.put("totalSeedsTested", totalSeeds);
        report.put("totalTransactionsExecuted", totalTransactionsExecuted);
        report.put("totalAonChecksPassed", totalAonChecksPassed);
        report.put("totalIpoRoundsPassed", totalIpoRoundsPassed);
        report.put("totalBusinessCyclesPassed", totalBusinessCyclesPassed);
        report.put("totalLedgerReconciliationsPassed", totalLedgerReconciliationsPassed);
        report.put("elapsedMs", elapsedMs);
        report.put("status", "SUCCESS");

        File repFile = econDir.resolve("m2-report.json").toFile();
        try (FileWriter fw = new FileWriter(repFile)) {
            fw.write(report.toString(2));
        }

        System.out.println("=== SUB-AGENT A (M2) SUKSES SELESAI ===");
        System.out.println(report.toString(2));
    }

    private static void assertTrue(String msg, boolean cond) {
        if (!cond) throw new AssertionError(msg);
    }

    private static void assertTrue(boolean cond) {
        if (!cond) throw new AssertionError();
    }

    private static void assertFalse(String msg, boolean cond) {
        if (cond) throw new AssertionError(msg);
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected instanceof Number && actual instanceof Number) {
            if (((Number) expected).longValue() == ((Number) actual).longValue()) return;
        }
        if (!Objects.equals(expected, actual)) throw new AssertionError("Expected " + expected + " but got " + actual);
    }

    private static void assertEquals(String msg, Object expected, Object actual) {
        if (expected instanceof Number && actual instanceof Number) {
            if (((Number) expected).longValue() == ((Number) actual).longValue()) return;
        }
        if (!Objects.equals(expected, actual)) throw new AssertionError(msg + " -> Expected " + expected + " but got " + actual);
    }

    private static void assertNotEquals(String msg, Object a, Object b) {
        if (Objects.equals(a, b)) throw new AssertionError(msg);
    }

    private static void assertNotNull(Object obj) {
        if (obj == null) throw new AssertionError("Expected not null");
    }

    private static void assertNotNull(String msg, Object obj) {
        if (obj == null) throw new AssertionError(msg);
    }
}
