package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.*;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.trading.*;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.mobile.TradingWebBridge;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.*;

public class ProbePF01Benchmark {
    public static void main(String[] args) throws Exception {
        System.out.println("=== BENCHMARK PERFORMA DOMAIN & BRIDGE (PF01) ===");

        File testDir = new File("build/tmp-probe-pf01");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("100000000"));

        TradingWebBridge bridge = new TradingWebBridge(facade);

        // Siapkan portofolio QA dengan beberapa posisi
        Account personal = facade.getPersonalAccount();
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        Instrument bbri = facade.getCatalog().getById("IDX:BBRI");
        Instrument tlkm = facade.getCatalog().getById("IDX:TLKM");
        Instrument aapl = facade.getCatalog().getById("US:AAPL");
        Instrument msft = facade.getCatalog().getById("US:MSFT");

        personal.addPosition(new Position(bbca.getTicker(), bbca.getExchange().name(), bbca.getCurrency(), 5000L, BigDecimal.valueOf(10000), BigDecimal.valueOf(10250)));
        personal.addPosition(new Position(bbri.getTicker(), bbri.getExchange().name(), bbri.getCurrency(), 10000L, BigDecimal.valueOf(5000), BigDecimal.valueOf(5200)));
        personal.addPosition(new Position(tlkm.getTicker(), tlkm.getExchange().name(), tlkm.getCurrency(), 8000L, BigDecimal.valueOf(3800), BigDecimal.valueOf(3900)));
        personal.addPosition(new Position(aapl.getTicker(), aapl.getExchange().name(), aapl.getCurrency(), 50L, BigDecimal.valueOf(180), BigDecimal.valueOf(185)));
        personal.addPosition(new Position(msft.getTicker(), msft.getExchange().name(), msft.getCurrency(), 30L, BigDecimal.valueOf(400), BigDecimal.valueOf(410)));

        // Majukan waktu game agar ada data pasar aktif & grafik
        facade.advanceWorldSecondsForTest(35 * 60);

        // 1. Warm-up
        for (int i = 0; i < 20; i++) {
            facade.getSnapshotJson();
            bridge.getInstruments("", "", null, true);
            bridge.getOrderBook("IDX:BBCA");
            bridge.getChartData("IDX:BBCA", "R1D");
        }

        // 2. Profiling SnapshotJson
        int iterations = 100;
        long[] snapTimes = new long[iterations];
        int snapBytes = 0;
        for (int i = 0; i < iterations; i++) {
            long t0 = System.nanoTime();
            String s = facade.getSnapshotJson();
            long t1 = System.nanoTime();
            snapTimes[i] = t1 - t0;
            snapBytes = s.getBytes().length;
        }
        Arrays.sort(snapTimes);
        double snapAvgMs = Arrays.stream(snapTimes).average().orElse(0) / 1_000_000.0;
        double snapP95Ms = snapTimes[(int)(iterations * 0.95)] / 1_000_000.0;
        double snapMaxMs = snapTimes[iterations - 1] / 1_000_000.0;
        double snapMinMs = snapTimes[0] / 1_000_000.0;

        System.out.printf("getSnapshotJson (x%d): Size=%d bytes | Min=%.3fms | Avg=%.3fms | P95=%.3fms | Max=%.3fms%n",
                iterations, snapBytes, snapMinMs, snapAvgMs, snapP95Ms, snapMaxMs);

        // 3. Profiling Sub-calls
        long[] netWorthTimes = new long[iterations];
        for (int i = 0; i < iterations; i++) {
            long t0 = System.nanoTime();
            facade.getPersonalNetWorth();
            long t1 = System.nanoTime();
            netWorthTimes[i] = t1 - t0;
        }
        Arrays.sort(netWorthTimes);
        double nwAvgMs = Arrays.stream(netWorthTimes).average().orElse(0) / 1_000_000.0;
        System.out.printf("getPersonalNetWorth (x%d): Avg=%.3fms | P95=%.3fms%n",
                iterations, nwAvgMs, netWorthTimes[(int)(iterations * 0.95)] / 1_000_000.0);

        // 4. Profiling Bridge methods called per tick
        long[] instrTimes = new long[iterations];
        int instrBytes = 0;
        for (int i = 0; i < iterations; i++) {
            long t0 = System.nanoTime();
            String s = bridge.getInstruments("", "", null, true);
            long t1 = System.nanoTime();
            instrTimes[i] = t1 - t0;
            instrBytes = s.getBytes().length;
        }
        Arrays.sort(instrTimes);
        System.out.printf("getInstruments (x%d): Size=%d bytes | Avg=%.3fms | P95=%.3fms%n",
                iterations, instrBytes, Arrays.stream(instrTimes).average().orElse(0) / 1_000_000.0, instrTimes[(int)(iterations * 0.95)] / 1_000_000.0);

        long[] obTimes = new long[iterations];
        int obBytes = 0;
        for (int i = 0; i < iterations; i++) {
            long t0 = System.nanoTime();
            String s = bridge.getOrderBook("IDX:BBCA");
            long t1 = System.nanoTime();
            obTimes[i] = t1 - t0;
            obBytes = s.getBytes().length;
        }
        Arrays.sort(obTimes);
        System.out.printf("getOrderBook (x%d): Size=%d bytes | Avg=%.3fms | P95=%.3fms%n",
                iterations, obBytes, Arrays.stream(obTimes).average().orElse(0) / 1_000_000.0, obTimes[(int)(iterations * 0.95)] / 1_000_000.0);

        long[] chartTimes = new long[iterations];
        int chartBytes = 0;
        for (int i = 0; i < iterations; i++) {
            long t0 = System.nanoTime();
            String s = bridge.getChartData("IDX:BBCA", "R1D");
            long t1 = System.nanoTime();
            chartTimes[i] = t1 - t0;
            chartBytes = s.getBytes().length;
        }
        Arrays.sort(chartTimes);
        System.out.printf("getChartData (x%d): Size=%d bytes | Avg=%.3fms | P95=%.3fms%n",
                iterations, chartBytes, Arrays.stream(chartTimes).average().orElse(0) / 1_000_000.0, chartTimes[(int)(iterations * 0.95)] / 1_000_000.0);

        long[] loanTimes = new long[iterations];
        int loanBytes = 0;
        for (int i = 0; i < iterations; i++) {
            long t0 = System.nanoTime();
            String s = bridge.getLoans();
            long t1 = System.nanoTime();
            loanTimes[i] = t1 - t0;
            loanBytes = s.getBytes().length;
        }
        Arrays.sort(loanTimes);
        System.out.printf("getLoans (x%d): Size=%d bytes | Avg=%.3fms | P95=%.3fms%n",
                iterations, loanBytes, Arrays.stream(loanTimes).average().orElse(0) / 1_000_000.0, loanTimes[(int)(iterations * 0.95)] / 1_000_000.0);

        long[] macroTimes = new long[iterations];
        int macroBytes = 0;
        for (int i = 0; i < iterations; i++) {
            long t0 = System.nanoTime();
            String s = bridge.getMacroAndSynthesisState();
            long t1 = System.nanoTime();
            macroTimes[i] = t1 - t0;
            macroBytes = s.getBytes().length;
        }
        Arrays.sort(macroTimes);
        System.out.printf("getMacroAndSynthesisState (x%d): Size=%d bytes | Avg=%.3fms | P95=%.3fms%n",
                iterations, macroBytes, Arrays.stream(macroTimes).average().orElse(0) / 1_000_000.0, macroTimes[(int)(iterations * 0.95)] / 1_000_000.0);

        long[] newsTimes = new long[iterations];
        int newsBytes = 0;
        for (int i = 0; i < iterations; i++) {
            long t0 = System.nanoTime();
            String s = bridge.getResearchFeed();
            long t1 = System.nanoTime();
            newsTimes[i] = t1 - t0;
            newsBytes = s.getBytes().length;
        }
        Arrays.sort(newsTimes);
        System.out.printf("getResearchFeed (x%d): Size=%d bytes | Avg=%.3fms | P95=%.3fms%n",
                iterations, newsBytes, Arrays.stream(newsTimes).average().orElse(0) / 1_000_000.0, newsTimes[(int)(iterations * 0.95)] / 1_000_000.0);

        // 5. Total Payload per Tick
        int baselinePayloadPerTick = snapBytes * 2 + instrBytes * 2 + obBytes * 2 + chartBytes * 2 + loanBytes * 2 + macroBytes * 2 + newsBytes * 2;
        int optimizedPayloadPerTick = snapBytes + instrBytes + obBytes; // Chart only on candle change/timeframe, loans/macro only when changed
        System.out.println("\n--- ESTIMASI TOTAL PAYLOAD STRING JSON PER DETIK ---");
        System.out.printf("Baseline (Panggilan Ganda 2x per detik): ~%d bytes (%.2f KB/detik)%n",
                baselinePayloadPerTick, baselinePayloadPerTick / 1024.0);
        System.out.printf("Teroptimasi (Tunggal & Targeted): ~%d bytes (%.2f KB/detik)%n",
                optimizedPayloadPerTick, optimizedPayloadPerTick / 1024.0);
        System.out.printf("Potensi Penghematan Payload: %.1f%%%n",
                100.0 * (1.0 - (double)optimizedPayloadPerTick / baselinePayloadPerTick));
    }
}
