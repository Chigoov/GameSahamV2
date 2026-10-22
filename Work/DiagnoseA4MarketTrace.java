package com.tradingglobal3d.diagnostic;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.clock.MarketSession;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketIndex;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.OrderBook;
import com.tradingglobal3d.mobile.TradingWebBridge;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class DiagnoseA4MarketTrace {

    public static void main(String[] args) throws Exception {
        System.out.println("=== A4-01: INVESTIGASI DAN PEMBUKTIAN RANTAI TICK -> NPC -> MATCHING -> HARGA ===");

        Path root = Paths.get(".").toAbsolutePath().normalize();
        File qaSaveDir = new File(root.toFile(), "Artifacts/8-Anotasi-2026-10-22/run-01/qa-save");
        qaSaveDir.mkdirs();
        // Bersihkan save lama dalam folder uji terisolasi
        for (File f : qaSaveDir.listFiles()) {
            if (f.isFile()) f.delete();
        }

        Path assets = root.resolve("android/app/src/main/assets");
        TradingFacade facade = new TradingFacade(SaveManager.forDirectory(qaSaveDir));
        facade.setCatalog(InstrumentCatalog.fromJsonString(java.nio.file.Files.readString(assets.resolve("data/market_universe.json"))));
        if (!facade.startOrResume()) throw new IllegalStateException("Save startup gagal");
        facade.getClock().play();
        facade.generateNpcLiquidity();

        InstrumentCatalog catalog = facade.getCatalog();
        GameClock clock = facade.getClock();
        TradingWebBridge bridge = new TradingWebBridge(facade);

        JSONObject report = new JSONObject();
        report.put("timestamp", System.currentTimeMillis());
        report.put("initialGameDate", clock.getCurrentDate().toString());
        report.put("initialGameTime", clock.getFormattedTime());

        JSONArray activeUniverseArr = new JSONArray();
        for (Instrument inst : catalog.getActiveUniverse()) {
            activeUniverseArr.put(new JSONObject()
                    .put("id", inst.getId())
                    .put("ticker", inst.getTicker())
                    .put("exchange", inst.getExchange().name())
                    .put("price", inst.getCurrentPrice().format())
                    .put("currency", inst.getCurrency().name())
            );
        }
        report.put("activeUniverseCount", catalog.getActiveUniverse().size());
        report.put("activeUniverse", activeUniverseArr);

        // 1. Kondisi Awal
        Instrument bbca = catalog.getById("IDX:BBCA");
        Instrument bmri = catalog.getById("IDX:BMRI");
        Instrument aapl = catalog.getById("US:AAPL");

        System.out.println("Active universe count: " + catalog.getActiveUniverse().size());
        System.out.println("BBCA initial price: " + (bbca != null ? bbca.getCurrentPrice().format() : "NULL"));
        System.out.println("AAPL initial price: " + (aapl != null ? aapl.getCurrentPrice().format() : "NULL"));

        // Catat resting orders awal di OrderBook
        OrderBook bookBbca = facade.getMarketEngine().getOrCreateBook(bbca);
        System.out.println("BBCA initial bids count: " + bookBbca.getBids().size() + ", asks count: " + bookBbca.getAsks().size());

        // 2. Simulasi Waktu Sesi IDX (09:00 WIB s/d 11:30 WIB)
        System.out.println("\n--- SIMULASI SESI IDX (09:00 - 11:30 WIB) ---");
        JSONArray idxSteps = new JSONArray();
        int initialTradeFills = bookBbca.getTradeHistory().size();

        System.out.println("Initial Clock: " + clock.getFormattedDate() + " " + clock.getFormattedTime() + " isPaused=" + clock.isPaused());
        // Majukan waktu ke 09:00:00 jika belum
        while (clock.getCurrentTime().isBefore(LocalTime.of(9, 0))) {
            facade.tick(10); // 10 detik nyata = 960 detik game
        }

        System.out.println("Start IDX Session at: " + clock.getFormattedTime() + " | Session: " + clock.getCurrentMarketSession().name() + " | isOpen: " + clock.getCurrentMarketSession().isOpenForTrading());

        // Jalankan 10 interval x 15 menit game (= 150 menit game)
        for (int i = 1; i <= 10; i++) {
            // Pada foreground 1x: 1 hari game = 15 menit nyata = 900 detik.
            // 15 menit game = 900 detik game = 9.375 detik nyata.
            // Kita panggil facade.tick(10) yang memajukan waktu game beberapa detik/menit
            // Untuk memastikan kita melewati batas 15 menit game (900 detik game):
            facade.advanceRealMs(9375, true); // foreground 1x memajukan ~900 detik game (15 menit)

            LocalTime currentTime = clock.getCurrentTime();
            MarketSession session = clock.getCurrentMarketSession();
            BigDecimal bbcaPrice = bbca.getCurrentPrice().getAmount();
            double bbcaChange = bbca.getDailyChangePercent();
            int bbcaBids = bookBbca.getBids().size();
            int bbcaAsks = bookBbca.getAsks().size();
            int bbcaTrades = bookBbca.getTradeHistory().size();

            JSONObject stepObj = new JSONObject()
                    .put("step", i)
                    .put("time", clock.getFormattedTime())
                    .put("session", session.name())
                    .put("isOpenForTrading", session.isOpenForTrading())
                    .put("bbcaPrice", bbcaPrice.toPlainString())
                    .put("bbcaDailyChangePercent", bbcaChange)
                    .put("bbcaBidsCount", bbcaBids)
                    .put("bbcaAsksCount", bbcaAsks)
                    .put("bbcaTradesCount", bbcaTrades);

            if (!bookBbca.getBids().isEmpty()) {
                stepObj.put("highestBid", bookBbca.getBids().firstKey().toPlainString());
            }
            if (!bookBbca.getAsks().isEmpty()) {
                stepObj.put("lowestAsk", bookBbca.getAsks().firstKey().toPlainString());
            }

            idxSteps.put(stepObj);

            System.out.printf("Step %02d | %s | %s | Open:%s | BBCA: %s (%+.2f%%) | Bids:%d Asks:%d Trades:%d%n",
                    i, clock.getFormattedTime(), session.name(), session.isOpenForTrading(),
                    bbcaPrice.toPlainString(), bbcaChange, bbcaBids, bbcaAsks, bbcaTrades);

            if (i == 1) {
                System.out.println("--- BBCA OrderBook Details at Step 01 ---");
                for (Map.Entry<BigDecimal, Queue<Order>> entry : bookBbca.getBids().entrySet()) {
                    for (Order o : entry.getValue()) {
                        System.out.println("  BID: price=" + entry.getKey() + " qty=" + o.getRemainingQuantity() + " acc=" + o.getAccountId() + " req=" + o.getRequestId());
                    }
                }
                for (Map.Entry<BigDecimal, Queue<Order>> entry : bookBbca.getAsks().entrySet()) {
                    for (Order o : entry.getValue()) {
                        System.out.println("  ASK: price=" + entry.getKey() + " qty=" + o.getRemainingQuantity() + " acc=" + o.getAccountId() + " req=" + o.getRequestId());
                    }
                }
            }
        }
        report.put("idxSteps", idxSteps);

        // 3. Simulasi Waktu Sesi US (21:30 WIB s/d 00:00 WIB)
        System.out.println("\n--- SIMULASI SESI US (21:30 - 00:00 WIB) ---");
        JSONArray usSteps = new JSONArray();
        OrderBook bookAapl = facade.getMarketEngine().getOrCreateBook(aapl);

        // Majukan waktu ke 21:30:00
        while (clock.getCurrentTime().isBefore(LocalTime.of(21, 30))) {
            facade.tick(10);
        }

        System.out.println("Start US Session at: " + clock.getFormattedTime() + " | Session: " + clock.getCurrentMarketSession().name() + " | isOpen: " + clock.getCurrentMarketSession().isOpenForTrading());

        for (int i = 1; i <= 6; i++) {
            facade.advanceRealMs(9375, true);

            LocalTime currentTime = clock.getCurrentTime();
            MarketSession session = clock.getCurrentMarketSession();
            BigDecimal aaplPrice = aapl.getCurrentPrice().getAmount();
            double aaplChange = aapl.getDailyChangePercent();
            int aaplBids = bookAapl.getBids().size();
            int aaplAsks = bookAapl.getAsks().size();
            int aaplTrades = bookAapl.getTradeHistory().size();

            JSONObject stepObj = new JSONObject()
                    .put("step", i)
                    .put("time", clock.getFormattedTime())
                    .put("session", session.name())
                    .put("isOpenForTrading", session.isOpenForTrading())
                    .put("aaplPrice", aaplPrice.toPlainString())
                    .put("aaplDailyChangePercent", aaplChange)
                    .put("aaplBidsCount", aaplBids)
                    .put("aaplAsksCount", aaplAsks)
                    .put("aaplTradesCount", aaplTrades);

            if (!bookAapl.getBids().isEmpty()) {
                stepObj.put("highestBid", bookAapl.getBids().firstKey().toPlainString());
            }
            if (!bookAapl.getAsks().isEmpty()) {
                stepObj.put("lowestAsk", bookAapl.getAsks().firstKey().toPlainString());
            }

            usSteps.put(stepObj);

            System.out.printf("US Step %02d | %s | %s | Open:%s | AAPL: %s (%+.2f%%) | Bids:%d Asks:%d Trades:%d%n",
                    i, clock.getFormattedTime(), session.name(), session.isOpenForTrading(),
                    aaplPrice.toPlainString(), aaplChange, aaplBids, aaplAsks, aaplTrades);
        }
        report.put("usSteps", usSteps);

        // 4. Periksa Hasil Bridge dan Indeks
        System.out.println("\n--- PEMERIKSAAN HASIL BRIDGE DAN INDEKS ---");
        String bridgeInstrumentsJson = bridge.getInstruments("", "", null, true);
        JSONArray bridgeInstruments = new JSONArray(bridgeInstrumentsJson);
        report.put("bridgeActiveInstrumentsCount", bridgeInstruments.length());

        String bridgeIndicesJson = bridge.getIndices();
        JSONArray bridgeIndices = new JSONArray(bridgeIndicesJson);
        report.put("bridgeIndices", bridgeIndices);

        System.out.println("Bridge instruments count: " + bridgeInstruments.length());
        System.out.println("Bridge indices: " + bridgeIndices.toString(2));

        // Simpan output ke file bukti
        File outReportFile = new File(root.toFile(), "Artifacts/8-Anotasi-2026-10-22/run-01/evidence/A4-01-diagnostic-trace.json");
        try (FileWriter fw = new FileWriter(outReportFile)) {
            fw.write(report.toString(2));
        }
        System.out.println("\nLaporan diagnosa tersimpan di: " + outReportFile.getAbsolutePath());
    }
}
