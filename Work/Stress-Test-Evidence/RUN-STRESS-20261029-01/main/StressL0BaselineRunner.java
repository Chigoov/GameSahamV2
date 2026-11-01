package com.tradingglobal3d.stress;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Runner Baseline Level L0: 10 seed x 100 aksi (E02 Rencana Stress Test).
 * Mengukur throughput, latensi p50/p95, dan memvalidasi seluruh invarians ekonomi.
 */
public class StressL0BaselineRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== MEMULAI RUNNER BASELINE LEVEL L0 (10 SEED x 100 AKSI) ===");
        long startTotal = System.currentTimeMillis();

        Path runDir = Path.of("Work/Stress-Test-Evidence/RUN-STRESS-20261029-01/main");
        Files.createDirectories(runDir);

        File catFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("src/main/assets/data/market_universe.json");
        String catalogJson = Files.readString(catFile.toPath());

        List<Long> actionLatenciesMicros = new ArrayList<>();
        int totalValidActions = 0;
        int totalBoundaryActions = 0;
        int totalInvalidRejectedActions = 0;
        int totalInvarianceChecksPassed = 0;

        for (int seed = 1001; seed <= 1010; seed++) {
            Random rng = new Random(seed);
            File seedSaveDir = runDir.resolve("qa-save-seed-" + seed).toFile();
            seedSaveDir.mkdirs();

            SaveManager saves = SaveManager.forDirectory(seedSaveDir);
            TradingFacade facade = new TradingFacade(saves);
            facade.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
            facade.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));
            facade.setDynamicDailyNewsEnabled(true);

            Account player = facade.getPersonalAccount();
            Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
            Instrument bmri = facade.getCatalog().getById("IDX:BMRI");

            for (int actionIdx = 1; actionIdx <= 100; actionIdx++) {
                long aStart = System.nanoTime();
                int actionType = rng.nextInt(10); // 0-6: valid (70%), 7-8: boundary (20%), 9: invalid (10%)

                if (actionType < 7) {
                    // Valid Actions
                    if (actionType == 0) {
                        // Limit Buy BBCA 1 lot if cash available, otherwise sell or topup
                        Money cost = bbca.getCurrentPrice().multiply(bbca.getLotSize());
                        if (player.getAvailableCash(Currency.IDR).isGreaterThanOrEqual(cost.multiply(1.01))) {
                            Order o = Order.createLimit("ORD-S" + seed + "-A" + actionIdx, bbca.getId(), player.getId(),
                                    OrderSide.BUY, bbca.getCurrentPrice(), bbca.getLotSize(), facade.getClock().getGameTimestamp());
                            MatchingEngine.OrderResult res = facade.submitOrder(o);
                            assertTrue("Submit BBCA: " + res.message, res.isSuccess);
                        } else {
                            facade.deposit("DEP-AUTO-" + seed + "-" + actionIdx, player.getId(), Money.of(10_000_000, Currency.IDR), "Suntikan Likuiditas");
                        }
                    } else if (actionType == 1) {
                        // Advance clock 15 minutes
                        facade.tick(900);
                    } else if (actionType == 2) {
                        // Limit Buy BMRI 2 lot if cash available, otherwise sell
                        Money cost = bmri.getCurrentPrice().multiply(bmri.getLotSize() * 2L);
                        if (player.getAvailableCash(Currency.IDR).isGreaterThanOrEqual(cost.multiply(1.01))) {
                            Order o = Order.createLimit("ORD-S" + seed + "-A" + actionIdx, bmri.getId(), player.getId(),
                                    OrderSide.BUY, bmri.getCurrentPrice(), bmri.getLotSize() * 2L, facade.getClock().getGameTimestamp());
                            MatchingEngine.OrderResult res = facade.submitOrder(o);
                            assertTrue("Submit BMRI: " + res.message, res.isSuccess);
                        } else {
                            // Jual posisi yang dimiliki jika ada
                            Position pos = player.getPosition(bmri.getId());
                            if (pos != null && pos.getQuantity() >= bmri.getLotSize()) {
                                Order o = Order.createLimit("ORD-SELL-S" + seed + "-A" + actionIdx, bmri.getId(), player.getId(),
                                        OrderSide.SELL, bmri.getCurrentPrice(), bmri.getLotSize(), facade.getClock().getGameTimestamp());
                                facade.submitOrder(o);
                            }
                        }
                    } else if (actionType == 3) {
                        // Deposit additional IDR
                        facade.deposit("DEP-" + seed + "-" + actionIdx, player.getId(), Money.of(2_000_000, Currency.IDR), "Topup");
                    } else if (actionType == 4) {
                        // Cancel pending orders if any
                        for (Order ord : facade.getOrderManager().getOrdersForAccount(player.getId())) {
                            if (!ord.getStatus().isFinal()) {
                                facade.cancelOrder(ord);
                                break;
                            }
                        }
                    } else if (actionType == 5) {
                        // Generate NPC market activity
                        facade.generateNpcLiquidity();
                    } else {
                        // Save game state
                        facade.save();
                    }
                    totalValidActions++;
                } else if (actionType < 9) {
                    // Boundary Actions (20%)
                    if (actionType == 7) {
                        // Order exactly at 25% ceiling boundary
                        Money ceiling = bbca.getDailyCeilingPrice();
                        Money cost = ceiling.multiply(bbca.getLotSize());
                        if (player.getAvailableCash(Currency.IDR).isLessThan(cost.multiply(1.01))) {
                            facade.deposit("DEP-CEIL-" + seed + "-" + actionIdx, player.getId(), Money.of(5_000_000, Currency.IDR), "Deposit Ceiling");
                        }
                        Order o = Order.createLimit("ORD-BOUND-S" + seed + "-A" + actionIdx, bbca.getId(), player.getId(),
                                OrderSide.BUY, ceiling, bbca.getLotSize(), facade.getClock().getGameTimestamp());
                        MatchingEngine.OrderResult res = facade.submitOrder(o);
                        assertTrue("Order tepat di batas ceiling harus diterima: " + res.message, res.isSuccess);
                    } else {
                        // Order exactly at 25% floor boundary
                        Money floor = bbca.getDailyFloorPrice();
                        Money cost = floor.multiply(bbca.getLotSize());
                        if (player.getAvailableCash(Currency.IDR).isLessThan(cost.multiply(1.01))) {
                            facade.deposit("DEP-FLOOR-" + seed + "-" + actionIdx, player.getId(), Money.of(5_000_000, Currency.IDR), "Deposit Floor");
                        }
                        Order o = Order.createLimit("ORD-BOUND-S" + seed + "-A" + actionIdx, bbca.getId(), player.getId(),
                                OrderSide.BUY, floor, bbca.getLotSize(), facade.getClock().getGameTimestamp());
                        MatchingEngine.OrderResult res = facade.submitOrder(o);
                        assertTrue("Order tepat di batas floor harus diterima: " + res.message, res.isSuccess);
                    }
                    totalBoundaryActions++;
                } else {
                    // Invalid Actions (10%) - Must be rejected gracefully without corrupted state
                    Order invalidOrder = Order.createLimit("ORD-INV-S" + seed + "-A" + actionIdx, bbca.getId(), player.getId(),
                            OrderSide.BUY, Money.of(-1000, Currency.IDR), 100, facade.getClock().getGameTimestamp());
                    MatchingEngine.OrderResult res = facade.submitOrder(invalidOrder);
                    assertFalse("Order dengan harga negatif harus ditolak", res.isSuccess);
                    totalInvalidRejectedActions++;
                }

                long aElapsed = (System.nanoTime() - aStart) / 1000L; // micros
                actionLatenciesMicros.add(aElapsed);

                // Verifikasi Invarians Keuangan Setelah Setiap Aksi
                assertTrue("Saldo kas IDR tidak boleh negatif", player.getAvailableCash(Currency.IDR).getAmount().signum() >= 0);
                assertTrue("Saldo tertahan tidak boleh negatif", player.getReservedCash(Currency.IDR).getAmount().signum() >= 0);
                totalInvarianceChecksPassed++;
            }

            // Verifikasi konsistensi buku besar
            assertTrue("Buku besar akun harus 100% seimbang", facade.reconcileAll());
        }

        long totalTimeMs = System.currentTimeMillis() - startTotal;
        Collections.sort(actionLatenciesMicros);

        long minMicros = actionLatenciesMicros.get(0);
        long maxMicros = actionLatenciesMicros.get(actionLatenciesMicros.size() - 1);
        long p50Micros = actionLatenciesMicros.get((int) (actionLatenciesMicros.size() * 0.50));
        long p95Micros = actionLatenciesMicros.get((int) (actionLatenciesMicros.size() * 0.95));
        long p99Micros = actionLatenciesMicros.get((int) (actionLatenciesMicros.size() * 0.99));
        double meanMicros = actionLatenciesMicros.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double throughput = (1000.0 / (totalTimeMs / 1000.0));

        JSONObject report = new JSONObject();
        report.put("level", "L0");
        report.put("totalSeeds", 10);
        report.put("actionsPerSeed", 100);
        report.put("totalActionsExecuted", 1000);
        report.put("validActions", totalValidActions);
        report.put("boundaryActions", totalBoundaryActions);
        report.put("invalidRejectedActions", totalInvalidRejectedActions);
        report.put("invarianceChecksPassed", totalInvarianceChecksPassed);
        report.put("totalTimeMs", totalTimeMs);
        report.put("throughputActionsPerSec", Math.round(throughput * 10.0) / 10.0);

        JSONObject latency = new JSONObject();
        latency.put("min_ms", minMicros / 1000.0);
        latency.put("mean_ms", Math.round((meanMicros / 1000.0) * 1000.0) / 1000.0);
        latency.put("p50_ms", p50Micros / 1000.0);
        latency.put("p95_ms", p95Micros / 1000.0);
        latency.put("p99_ms", p99Micros / 1000.0);
        latency.put("max_ms", maxMicros / 1000.0);
        report.put("latency", latency);

        File reportFile = runDir.resolve("L0-baseline-report.json").toFile();
        try (FileWriter fw = new FileWriter(reportFile)) {
            fw.write(report.toString(2));
        }

        System.out.println("=== BASELINE L0 SUKSES ===");
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
}
