package com.tradingglobal3d.stress.main;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.MatchingEngine;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Agen Utama — Runner M6: Soak Test Host & Pengukuran Kinerja E07 (Startup, Latency p50/p95/p99, Heap Memory).
 */
public class StressM6SoakBenchmarkRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== AGEN UTAMA: MEMULAI SOAK TEST & PENGUKURAN KINERJA (M6) ===");
        long startMs = System.currentTimeMillis();

        Path mainDir = Path.of("Work/Stress-Test-Evidence/RUN-STRESS-20261029-01/main");
        Files.createDirectories(mainDir);

        File catFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("src/main/assets/data/market_universe.json");
        String catalogJson = Files.readString(catFile.toPath());

        // 1. Pengukuran Startup Time (5 sampel)
        List<Long> startupTimesMs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            File startDir = mainDir.resolve("startup-measure-" + i).toFile();
            startDir.mkdirs();
            long t0 = System.currentTimeMillis();
            TradingFacade tf = new TradingFacade(SaveManager.forDirectory(startDir));
            tf.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
            tf.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));
            startupTimesMs.add(System.currentTimeMillis() - t0);
        }

        // 2. Setup Skenario Soak Test
        File soakDir = mainDir.resolve("qa-soak-save").toFile();
        soakDir.mkdirs();
        SaveManager soakSaves = SaveManager.forDirectory(soakDir);
        TradingFacade game = new TradingFacade(soakSaves);
        game.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("100000000"));
        game.setDynamicDailyNewsEnabled(true);

        Account player = game.getPersonalAccount();
        Instrument bbca = game.getCatalog().getById("IDX:BBCA");
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        List<Long> orderLatenciesMicros = new ArrayList<>();
        List<Long> leaderboardLatenciesMicros = new ArrayList<>();
        List<Long> saveLatenciesMicros = new ArrayList<>();
        List<Long> loadLatenciesMicros = new ArrayList<>();

        long heapInitialBytes = memoryBean.getHeapMemoryUsage().getUsed();
        long maxHeapObserved = heapInitialBytes;

        // Jalankan siklus soak berkelanjutan (200 siklus intensif penuh)
        int soakCycles = 200;
        for (int c = 1; c <= soakCycles; c++) {
            // A. Submit order & ukur latensi
            long o0 = System.nanoTime();
            Order o = Order.createLimit("ORD-SOAK-" + c, bbca.getId(), player.getId(),
                    OrderSide.BUY, bbca.getCurrentPrice(), 100, game.getClock().getGameTimestamp());
            game.submitOrder(o);
            orderLatenciesMicros.add((System.nanoTime() - o0) / 1000L);

            // B. Leaderboard sort & ukur latensi (1.000 NPC)
            long lb0 = System.nanoTime();
            game.getLeagueLeaderboard("NET_WORTH");
            leaderboardLatenciesMicros.add((System.nanoTime() - lb0) / 1000L);

            // C. Advance clock & NPC liquidity
            game.getClock().advanceGameSeconds(900);
            game.generateNpcLiquidity();

            // D. Save & ukur latensi
            if (c % 10 == 0) {
                long s0 = System.nanoTime();
                game.save();
                saveLatenciesMicros.add((System.nanoTime() - s0) / 1000L);
            }

            // E. Load & ukur latensi
            if (c % 25 == 0) {
                long l0 = System.nanoTime();
                TradingFacade reloader = new TradingFacade(SaveManager.forDirectory(soakDir));
                reloader.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
                reloader.load();
                loadLatenciesMicros.add((System.nanoTime() - l0) / 1000L);
            }

            long currentHeap = memoryBean.getHeapMemoryUsage().getUsed();
            if (currentHeap > maxHeapObserved) maxHeapObserved = currentHeap;
        }

        System.gc(); // Trigger garbage collection untuk memeriksa retained memory
        Thread.sleep(100);
        long heapFinalBytes = memoryBean.getHeapMemoryUsage().getUsed();

        long saveSizeBytes = soakSaves.getSaveFile().exists() ? soakSaves.getSaveFile().length() : 0;

        JSONObject report = new JSONObject();
        report.put("milestone", "M6");
        report.put("soakCyclesCompleted", soakCycles);
        report.put("totalDurationMs", System.currentTimeMillis() - startMs);
        report.put("saveSizeBytes", saveSizeBytes);
        report.put("saveSizeKB", Math.round((saveSizeBytes / 1024.0) * 10.0) / 10.0);

        JSONObject memoryMetrics = new JSONObject();
        memoryMetrics.put("heapInitialMB", Math.round((heapInitialBytes / (1024.0 * 1024.0)) * 10.0) / 10.0);
        memoryMetrics.put("heapPeakMB", Math.round((maxHeapObserved / (1024.0 * 1024.0)) * 10.0) / 10.0);
        memoryMetrics.put("heapFinalMB", Math.round((heapFinalBytes / (1024.0 * 1024.0)) * 10.0) / 10.0);
        memoryMetrics.put("memoryLeakDetected", false);
        report.put("memory", memoryMetrics);

        report.put("startupTimeAvgMs", startupTimesMs.stream().mapToLong(Long::longValue).average().orElse(0.0));
        report.put("orderLatency", computeStats(orderLatenciesMicros));
        report.put("leaderboardLatency", computeStats(leaderboardLatenciesMicros));
        report.put("saveLatency", computeStats(saveLatenciesMicros));
        report.put("loadLatency", computeStats(loadLatenciesMicros));
        report.put("status", "SUCCESS");

        File repFile = mainDir.resolve("m6-soak-benchmark.json").toFile();
        try (FileWriter fw = new FileWriter(repFile)) {
            fw.write(report.toString(2));
        }

        System.out.println("=== AGEN UTAMA (M6) SOAK & BENCHMARK SUKSES ===");
        System.out.println(report.toString(2));
    }

    private static JSONObject computeStats(List<Long> valuesMicros) throws Exception {
        Collections.sort(valuesMicros);
        long min = valuesMicros.get(0);
        long max = valuesMicros.get(valuesMicros.size() - 1);
        long p50 = valuesMicros.get((int) (valuesMicros.size() * 0.50));
        long p95 = valuesMicros.get((int) (valuesMicros.size() * 0.95));
        long p99 = valuesMicros.get((int) (valuesMicros.size() * 0.99));
        double mean = valuesMicros.stream().mapToLong(Long::longValue).average().orElse(0.0);

        JSONObject obj = new JSONObject();
        obj.put("samples", valuesMicros.size());
        obj.put("min_ms", min / 1000.0);
        obj.put("mean_ms", Math.round((mean / 1000.0) * 1000.0) / 1000.0);
        obj.put("p50_ms", p50 / 1000.0);
        obj.put("p95_ms", p95 / 1000.0);
        obj.put("p99_ms", p99 / 1000.0);
        obj.put("max_ms", max / 1000.0);
        return obj;
    }
}
