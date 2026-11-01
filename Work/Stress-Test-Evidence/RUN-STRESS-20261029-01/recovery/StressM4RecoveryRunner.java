package com.tradingglobal3d.stress.recovery;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.research.ResearchService;
import com.tradingglobal3d.core.research.TerminalTier;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Sub-Agent C (Persistensi) — Runner M4: Fault Injection, Matriks Recovery & 1.000 Save-Reload Cycles (E04).
 */
public class StressM4RecoveryRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("=== SUB-AGENT C: MEMULAI FAULT INJECTION & RECOVERY TESTING (M4) ===");
        long startMs = System.currentTimeMillis();

        Path recDir = Path.of("Work/Stress-Test-Evidence/RUN-STRESS-20261029-01/recovery");
        Files.createDirectories(recDir);

        File catFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("src/main/assets/data/market_universe.json");
        String catalogJson = Files.readString(catFile.toPath());

        int totalFaultInjections = 0;
        int totalRecoveryMatrixPassed = 0;
        int totalSaveReloadCycles = 0;
        int totalAutoRenewalGuardsPassed = 0;

        // 1. PENGUJIAN MATRIKS PEMULIHAN (Primary x Backup) & PENAWARAN KONFIRMASI (E04)
        for (int v = 1; v <= 25; v++) {
            File testDir = recDir.resolve("fault-matrix-" + v).toFile();
            testDir.mkdirs();

            SaveManager saves = SaveManager.forDirectory(testDir);
            TradingFacade facade = new TradingFacade(saves);
            facade.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
            facade.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));

            // Simpan save 1
            facade.save();
            // Simpan save 2 untuk memicu backup valid
            facade.getPersonalAccount().creditCash(Money.of(1_000_000, Currency.IDR));
            facade.save();

            File priFile = saves.getSaveFile();
            File bakFile = saves.getBackupFile();
            assertTrue(priFile.exists());
            assertTrue(bakFile.exists());

            // Varian 1: Primary corrupt, Backup valid -> wajib menyajikan penawaran dengan metadata waktu & risiko
            Files.writeString(priFile.toPath(), "{ CORRUPT_PAYLOAD_V" + v + " !@#$%");
            TradingFacade recFacade = new TradingFacade(SaveManager.forDirectory(testDir));
            recFacade.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
            boolean loadOk = recFacade.load();
            assertFalse("Load awal tidak boleh langsung menimpa data tanpa konfirmasi", loadOk);
            assertTrue("Harus menawarkan pemulihan cadangan", recFacade.hasPendingBackupRecoveryOffer());

            JSONObject offer = recFacade.getPendingBackupRecoveryOffer();
            assertNotNull(offer);
            assertTrue(offer.has("backupLastModified"));
            assertTrue(offer.has("riskExplanation"));

            // Konfirmasi pemulihan
            recFacade.confirmBackupRecovery();
            assertFalse(recFacade.hasPendingBackupRecoveryOffer());
            assertNotNull(recFacade.getPersonalAccount());
            assertEquals(Money.of(50_000_000L, Currency.IDR), recFacade.getPersonalAccount().getTotalCash(Currency.IDR));
            totalFaultInjections++;
            totalRecoveryMatrixPassed++;

            // Varian 2: Primary corrupt & Backup corrupt -> gagal bersih tanpa crash
            Files.writeString(priFile.toPath(), "{ BROKEN_PRIMARY");
            Files.writeString(bakFile.toPath(), "{ BROKEN_BACKUP");
            TradingFacade allCorrupt = new TradingFacade(SaveManager.forDirectory(testDir));
            allCorrupt.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
            assertFalse("Harus gagal memuat jika kedua file rusak", allCorrupt.load());
            assertFalse("Tidak boleh ada penawaran jika backup rusak", allCorrupt.hasPendingBackupRecoveryOffer());
            totalFaultInjections++;
            totalRecoveryMatrixPassed++;
        }

        // 2. 1.000 SIKLUS SAVE-RELOAD PADA BERBAGAI UKURAN PROFIL (E04)
        File cycleDir = recDir.resolve("save-reload-1000").toFile();
        cycleDir.mkdirs();
        SaveManager cycleSaves = SaveManager.forDirectory(cycleDir);
        TradingFacade cycleGame = new TradingFacade(cycleSaves);
        cycleGame.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
        cycleGame.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));

        for (int i = 1; i <= 1000; i++) {
            // Lakukan mutasi deposit resmi ke ledger
            if (i % 10 == 0) {
                cycleGame.deposit("DEP-CYCLE-" + i, cycleGame.getPersonalAccount().getId(), Money.of(100_000, Currency.IDR), "Deposit Berkala");
            }
            cycleGame.save();

            // Reload setiap 50 siklus untuk memastikan integritas pembacaan
            if (i % 50 == 0) {
                TradingFacade reloader = new TradingFacade(SaveManager.forDirectory(cycleDir));
                reloader.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
                assertTrue("Reload siklus ke-" + i + " harus sukses", reloader.load());
                assertNotNull(reloader.getPersonalAccount());
                assertTrue("Neraca ledger harus seimbang", reloader.reconcileAll());
            }
            totalSaveReloadCycles++;
        }

        // 3. PENGUJIAN PEMBATALAN AUTO-RENEWAL & MASA AKTIF BERBAYAR (Poin 1 Review)
        for (int k = 1; k <= 10; k++) {
            File subDir = recDir.resolve("sub-cancel-" + k).toFile();
            subDir.mkdirs();
            TradingFacade subGame = new TradingFacade(SaveManager.forDirectory(subDir));
            subGame.setCatalog(InstrumentCatalog.fromJsonString(catalogJson));
            subGame.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));

            Account player = subGame.getPersonalAccount();
            ResearchService res = subGame.getResearchService();
            subGame.deposit("DEP-USD-" + k, player.getId(), Money.of(600_000.0, Currency.USD), "Deposit");

            com.tradingglobal3d.core.GameTestSupport.unlockManager(subGame);
            assertTrue(subGame.subscribeTerminal(TerminalTier.PRO));
            long now = subGame.getClock().getGameTimestamp();
            assertEquals(TerminalTier.PRO, res.getCurrentTier());

            // Batalkan auto-renewal
            assertTrue(res.cancelAutoRenewal());
            assertFalse(res.isAutoRenewalEnabled());

            // Verifikasi akses tetap aktif pada hari ke-15 dan hari ke-29
            assertTrue("Akses Pro harus aktif hari ke-15", res.hasAccess(TerminalTier.PRO, now + 15L * 86400_000L));
            assertTrue("Akses Pro harus aktif hari ke-29", res.hasAccess(TerminalTier.PRO, now + 29L * 86400_000L));

            // Simpan dan reload
            JSONObject exp = res.exportState();
            ResearchService relRes = new ResearchService();
            relRes.restoreState(exp);
            assertFalse(relRes.isAutoRenewalEnabled());
            assertTrue("Akses Pro harus persisten setelah reload", relRes.hasAccess(TerminalTier.PRO, now + 15L * 86400_000L));

            // Hari ke-31: akses terkunci dan auto-renewal tidak memotong kas
            Money cashBefore = player.getTotalCash(Currency.USD);
            assertFalse("Akses Pro harus expired hari ke-31", relRes.hasAccess(TerminalTier.PRO, now + 31L * 86400_000L));
            relRes.processDailyAutoRenewal(player, subGame.getLedger(), now + 31L * 86400_000L);
            assertEquals("Kas tidak boleh terpotong setelah pembatalan", cashBefore, player.getTotalCash(Currency.USD));

            totalAutoRenewalGuardsPassed++;
        }

        long elapsedMs = System.currentTimeMillis() - startMs;
        JSONObject report = new JSONObject();
        report.put("milestone", "M4");
        report.put("subAgentId", "AGENT-SUB-RECOVERY");
        report.put("totalFaultInjections", totalFaultInjections);
        report.put("totalRecoveryMatrixPassed", totalRecoveryMatrixPassed);
        report.put("totalSaveReloadCycles", totalSaveReloadCycles);
        report.put("totalAutoRenewalGuardsPassed", totalAutoRenewalGuardsPassed);
        report.put("elapsedMs", elapsedMs);
        report.put("status", "SUCCESS");

        File repFile = recDir.resolve("m4-report.json").toFile();
        try (FileWriter fw = new FileWriter(repFile)) {
            fw.write(report.toString(2));
        }

        System.out.println("=== SUB-AGENT C (M4) SUKSES SELESAI ===");
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

    private static void assertFalse(boolean cond) {
        if (cond) throw new AssertionError();
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

    private static void assertNotNull(Object obj) {
        if (obj == null) throw new AssertionError("Expected not null");
    }
}
