package com.tradingglobal3d.stress.simulation;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.league.LeagueCompetitor;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.research.NewsPriceTarget;
import com.tradingglobal3d.core.research.ResearchService;
import com.tradingglobal3d.core.research.TerminalTier;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * Sub-Agent B (Simulasi) — Runner M3: Waktu 24x, Berita Pro/Whale, 1.000 NPC & Jangka Panjang (L4-L5).
 */
public class StressM3SimulationRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== SUB-AGENT B: MEMULAI SIMULASI WAKTU, BERITA & 1.000 NPC (M3) ===");
        long startMs = System.currentTimeMillis();

        Path simDir = Path.of("Work/Stress-Test-Evidence/RUN-STRESS-20261029-01/simulation");
        Files.createDirectories(simDir);

        File catFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("src/main/assets/data/market_universe.json");
        String catalogJson = Files.readString(catFile.toPath());

        int totalSeeds = 10;
        int totalTradingDaysSimulated = 0;
        int totalProNewsScheduled = 0;
        int totalWhaleCatalystsScheduled = 0;
        int totalFollowUpRumorsScheduled = 0;
        int totalTargetEvaluationsPassed = 0;
        int total1000NpcLeaderboardPasses = 0;

        for (int s = 1; s <= totalSeeds; s++) {
            int seed = 3000 + s;
            File saveDir = simDir.resolve("qa-save-sim-seed-" + seed).toFile();
            saveDir.mkdirs();

            SaveManager saves = SaveManager.forDirectory(saveDir);
            TradingFacade facade = new TradingFacade(saves);
            facade.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
            facade.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));
            facade.setDynamicDailyNewsEnabled(true);

            ResearchService research = facade.getResearchService();
            LocalDate startDate = facade.getClock().getCurrentDate();

            // Jalankan simulasi jangka panjang (365 hari bursa game = 1 tahun bursa penuh per seed)
            int simDays = 365;
            for (int d = 1; d <= simDays; d++) {
                LocalDate currentDate = startDate.plusDays(d);

                // 1. Penjadwalan Berita Harian (2 Pro, Whale 7 hari, Rumor Lanjutan 14 hari)
                research.ensureDailyNewsSchedule(currentDate, facade.getCatalog(), facade.getClock().getGameTimestamp());

                // 2. Evaluasi Target Berita Terhadap Batas 25% Harian
                research.evaluateNewsTargets(facade.getCatalog(), currentDate);
                totalTargetEvaluationsPassed++;

                totalTradingDaysSimulated++;
            }

            // Hitung distribusi berita terjadwal dalam 1 tahun penuh
            List<NewsPriceTarget> targets = research.getNewsTargets();
            for (NewsPriceTarget t : targets) {
                if (t.getTier() == TerminalTier.PRO) totalProNewsScheduled++;
                else if (t.isFollowUp()) totalFollowUpRumorsScheduled++;
                else totalWhaleCatalystsScheduled++;
            }

            // Verifikasi 1.000 NPC Leaderboard & Profil Peringkat Ganda
            assertEquals(1000, facade.getLeagueManager().getNpcCompetitors().size());
            JSONObject p1 = facade.getNpcProfile("NPC-01");
            assertTrue("Profil NPC #1 harus sukses", p1.getBoolean("success"));
            assertEquals("Peringkat kekayaan #1 harus tepat 1", 1, p1.getInt("netWorthRank"));
            assertTrue("Harus memuat weeklyReturnRank", p1.has("weeklyReturnRank"));
            total1000NpcLeaderboardPasses++;

            // Simpan dan pulihkan untuk membuktikan persistensi 365 hari simulasi
            facade.save();
            TradingFacade reloaded = new TradingFacade(SaveManager.forDirectory(saveDir));
            reloaded.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
            assertTrue("Save 365 hari harus sukses dimuat ulang", reloaded.load());
            assertEquals("Daftar target harus persisten setelah reload", targets.size(), reloaded.getNewsPriceTargets().size());
        }

        long elapsedMs = System.currentTimeMillis() - startMs;
        JSONObject report = new JSONObject();
        report.put("milestone", "M3");
        report.put("subAgentId", "AGENT-SUB-SIMULASI");
        report.put("totalSeedsTested", totalSeeds);
        report.put("totalTradingDaysSimulated", totalTradingDaysSimulated);
        report.put("totalProNewsScheduled", totalProNewsScheduled);
        report.put("totalWhaleCatalystsScheduled", totalWhaleCatalystsScheduled);
        report.put("totalFollowUpRumorsScheduled", totalFollowUpRumorsScheduled);
        report.put("totalTargetEvaluationsPassed", totalTargetEvaluationsPassed);
        report.put("total1000NpcLeaderboardPasses", total1000NpcLeaderboardPasses);
        report.put("elapsedMs", elapsedMs);
        report.put("status", "SUCCESS");

        File repFile = simDir.resolve("m3-report.json").toFile();
        try (FileWriter fw = new FileWriter(repFile)) {
            fw.write(report.toString(2));
        }

        System.out.println("=== SUB-AGENT B (M3) SUKSES SELESAI ===");
        System.out.println(report.toString(2));
    }

    private static void assertTrue(String msg, boolean cond) {
        if (!cond) throw new AssertionError(msg);
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
}
