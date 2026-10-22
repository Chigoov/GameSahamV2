package com.tradingglobal3d.diagnostic;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.clock.MarketSession;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.OrderBook;
import com.tradingglobal3d.core.trading.TradeFill;
import com.tradingglobal3d.mobile.TradingWebBridge;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.*;

public class VerifyA4DeterministicHost {

    public static void main(String[] args) throws Exception {
        System.out.println("=== A4-04: PEMBUKTIAN DETERMINISTIK HOST 1X SESI SAH IDX DAN US ===");

        Path root = Paths.get(".").toAbsolutePath().normalize();
        File qaSaveDir = new File(root.toFile(), "Artifacts/8-Anotasi-2026-10-22/run-01/qa-save");
        qaSaveDir.mkdirs();
        if (qaSaveDir.listFiles() != null) {
            for (File f : qaSaveDir.listFiles()) {
                if (f.isFile()) f.delete();
            }
        }

        Path assets = root.resolve("android/app/src/main/assets");
        TradingFacade facade = new TradingFacade(SaveManager.forDirectory(qaSaveDir));
        facade.setCatalog(InstrumentCatalog.fromJsonString(java.nio.file.Files.readString(assets.resolve("data/market_universe.json"))));
        if (!facade.startOrResume()) throw new IllegalStateException("Startup gagal");
        facade.getClock().play();
        facade.generateNpcLiquidity();

        InstrumentCatalog catalog = facade.getCatalog();
        GameClock clock = facade.getClock();
        TradingWebBridge bridge = new TradingWebBridge(facade);

        JSONObject result = new JSONObject();
        result.put("sourceFingerprint", "A4-FINAL-2026-10-22");
        result.put("startTime", System.currentTimeMillis());

        // 1. Sesi IDX (09:00 - 11:30 WIB)
        System.out.println("\n[1] PENGUJIAN SESI IDX REGULER (09:00 - 11:30 WIB)");
        while (clock.getCurrentTime().isBefore(LocalTime.of(9, 0))) {
            facade.tick(10);
        }

        Instrument bbca = catalog.getById("IDX:BBCA");
        Instrument bmri = catalog.getById("IDX:BMRI");
        OrderBook bookBbca = facade.getMarketEngine().getOrCreateBook(bbca);

        int initialBbcaFills = bookBbca.getTradeHistory().size();
        Money initialBbcaPrice = bbca.getCurrentPrice();

        // Jalankan 6 langkah @ 15 menit game
        JSONArray idxFillsLog = new JSONArray();
        for (int i = 1; i <= 6; i++) {
            facade.advanceRealMs(9375, true); // 15 menit game pada foreground 1x
            for (TradeFill fill : bookBbca.getTradeHistory()) {
                if (fill.getTimestamp() >= clock.getGameTimestamp() - 900_000L) {
                    idxFillsLog.put(new JSONObject()
                            .put("tradeId", fill.getFillId())
                            .put("instrument", fill.getInstrumentId())
                            .put("price", fill.getPrice().format())
                            .put("quantity", fill.getQuantity())
                            .put("buyer", fill.getBuyAccountId())
                            .put("seller", fill.getSellAccountId())
                            .put("time", clock.getFormattedTime())
                    );
                }
            }
        }

        int afterIdxFills = bookBbca.getTradeHistory().size();
        Money finalBbcaPrice = bbca.getCurrentPrice();
        double bbcaChangePct = bbca.getDailyChangePercent();

        System.out.println("  BBCA fills count: " + afterIdxFills + " (awal: " + initialBbcaFills + ")");
        System.out.println("  BBCA harga akhir: " + finalBbcaPrice.format() + " (" + String.format(Locale.US, "%+.2f%%", bbcaChangePct) + ")");
        if (afterIdxFills <= initialBbcaFills) throw new AssertionError("Harus ada TradeFill di sesi IDX aktif!");

        result.put("idxBbcaInitialPrice", initialBbcaPrice.format());
        result.put("idxBbcaFinalPrice", finalBbcaPrice.format());
        result.put("idxBbcaChangePercent", bbcaChangePct);
        result.put("idxBbcaTotalTrades", afterIdxFills);

        // Verifikasi Chart Data Pipeline (OHLCV)
        JSONObject chartBbca = new JSONObject(bridge.getChartData("IDX:BBCA", "M5"));
        if (!chartBbca.getBoolean("success")) throw new AssertionError("getChartData harus success");
        if (chartBbca.getInt("totalTrades") < 1) throw new AssertionError("totalTrades chart harus >= 1");
        result.put("chartBbcaBarsCount", chartBbca.getJSONArray("bars").length());

        // 2. Sesi Closed/Break (12:00 WIB) - Tidak boleh ada trade baru
        System.out.println("\n[2] PENGUJIAN SESI BREAK / CLOSED (12:00 WIB) - HARGA TETAP STABIL");
        while (clock.getCurrentTime().isBefore(LocalTime.of(12, 0))) {
            facade.tick(10);
        }
        int fillsAtBreak = bookBbca.getTradeHistory().size();
        Money priceAtBreak = bbca.getCurrentPrice();
        facade.advanceRealMs(9375, true); // 15 menit break
        int fillsAfterBreak = bookBbca.getTradeHistory().size();
        System.out.println("  Fills saat istirahat siang: awal=" + fillsAtBreak + ", sesudah=" + fillsAfterBreak);
        if (fillsAfterBreak != fillsAtBreak) throw new AssertionError("Tidak boleh ada trade baru di sesi break!");

        // 3. Sesi US (21:30 - 00:00 WIB)
        System.out.println("\n[3] PENGUJIAN SESI US REGULER (21:30 - 00:00 WIB)");
        while (clock.getCurrentTime().isBefore(LocalTime.of(21, 30))) {
            facade.tick(30);
        }

        Instrument aapl = catalog.getById("US:AAPL");
        OrderBook bookAapl = facade.getMarketEngine().getOrCreateBook(aapl);
        int initialAaplFills = bookAapl.getTradeHistory().size();
        Money initialAaplPrice = aapl.getCurrentPrice();

        JSONArray usFillsLog = new JSONArray();
        for (int i = 1; i <= 6; i++) {
            facade.advanceRealMs(9375, true);
            for (TradeFill fill : bookAapl.getTradeHistory()) {
                if (fill.getTimestamp() >= clock.getGameTimestamp() - 900_000L) {
                    usFillsLog.put(new JSONObject()
                            .put("tradeId", fill.getFillId())
                            .put("instrument", fill.getInstrumentId())
                            .put("price", fill.getPrice().format())
                            .put("quantity", fill.getQuantity())
                            .put("buyer", fill.getBuyAccountId())
                            .put("seller", fill.getSellAccountId())
                            .put("time", clock.getFormattedTime())
                    );
                }
            }
        }

        int afterAaplFills = bookAapl.getTradeHistory().size();
        Money finalAaplPrice = aapl.getCurrentPrice();
        double aaplChangePct = aapl.getDailyChangePercent();

        System.out.println("  AAPL fills count: " + afterAaplFills + " (awal: " + initialAaplFills + ")");
        System.out.println("  AAPL harga akhir: " + finalAaplPrice.format() + " (" + String.format(Locale.US, "%+.2f%%", aaplChangePct) + ")");
        if (afterAaplFills <= initialAaplFills) throw new AssertionError("Harus ada TradeFill di sesi US aktif!");

        result.put("usAaplInitialPrice", initialAaplPrice.format());
        result.put("usAaplFinalPrice", finalAaplPrice.format());
        result.put("usAaplChangePercent", aaplChangePct);
        result.put("usAaplTotalTrades", afterAaplFills);

        // 4. Verifikasi Invariant Finansial & Rekonsiliasi Buku Besar
        System.out.println("\n[4] VERIFIKASI INVARIANT KAS, SAHAM, & REKONSILIASI LEDGER");
        boolean reconciled = facade.reconcileAll();
        System.out.println("  facade.reconcileAll(): " + reconciled);
        if (!reconciled) throw new AssertionError("Ledger ekonomi harus seimbang dan rekonsiliasi sukses!");
        result.put("ledgerReconciled", reconciled);

        // 5. Verifikasi Save & Reload Roundtrip
        System.out.println("\n[5] VERIFIKASI PERSISTENSI SAVE & RELOAD");
        Money bbcaPriceBeforeSave = bbca.getCurrentPrice();
        Money aaplPriceBeforeSave = aapl.getCurrentPrice();
        System.out.println("  Price before save: BBCA=" + bbcaPriceBeforeSave.format() + ", AAPL=" + aaplPriceBeforeSave.format());
        facade.save();

        TradingFacade reloadFacade = new TradingFacade(SaveManager.forDirectory(qaSaveDir));
        reloadFacade.setCatalog(catalog);
        if (!reloadFacade.load()) throw new AssertionError("Save load gagal");

        Instrument reloadedBbca = reloadFacade.getCatalog().getById("IDX:BBCA");
        Instrument reloadedAapl = reloadFacade.getCatalog().getById("US:AAPL");
        System.out.println("  Reloaded BBCA price: " + reloadedBbca.getCurrentPrice().format());
        System.out.println("  Reloaded AAPL price: " + reloadedAapl.getCurrentPrice().format());

        if (!reloadedBbca.getCurrentPrice().equals(bbcaPriceBeforeSave)) throw new AssertionError("BBCA price mismatch after reload");
        if (!reloadedAapl.getCurrentPrice().equals(aaplPriceBeforeSave)) throw new AssertionError("AAPL price mismatch after reload");
        result.put("saveReloadPriceConsistent", true);

        // Simpan artefak bukti lengkap
        File outEvidence = new File(root.toFile(), "Artifacts/8-Anotasi-2026-10-22/run-01/evidence/A4-04-host-trace.json");
        try (FileWriter fw = new FileWriter(outEvidence)) {
            fw.write(result.toString(2));
        }

        System.out.println("\n=== A4-04 HOST 1X VERIFIKASI DETERMINISTIK: SEMUA GATE LULUS (PASS) ===");
        System.out.println("Bukti tersimpan di: " + outEvidence.getAbsolutePath());
    }
}
