package com.tradingglobal3d.core;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.storage.SaveState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Pengujian verifikasi F01: Proteksi rotasi backup dari primary file rusak / corrupt >10 byte.
 */
public class SaveBackupCorruptionTest {
    private File testDir;
    private SaveManager saveManager;

    @Before
    public void setUp() {
        testDir = new File(System.getProperty("java.io.tmpdir"), "f01_save_test_" + UUID.randomUUID());
        testDir.mkdirs();
        saveManager = SaveManager.forDirectory(testDir);
    }

    @After
    public void tearDown() {
        if (testDir != null && testDir.exists()) {
            File[] files = testDir.listFiles();
            if (files != null) for (File f : files) f.delete();
            testDir.delete();
        }
    }

    private SaveState createSampleState(long cashAmount) {
        GameClock clock = new GameClock(LocalDate.of(2026, 10, 22), LocalTime.of(10, 0, 0));
        Account acc = new Account("ACC-PERSONAL-001", "Trader Utama", AccountType.PERSONAL, null);
        acc.creditCash(Money.of(cashAmount, Currency.IDR));
        EconomyLedger ledger = new EconomyLedger();
        return SaveState.capture(GameMode.INTERNAL_MARKET, clock, Collections.singletonList(acc), ledger);
    }

    @Test
    public void testCorruptPrimaryLongerThan10BytesDoesNotOverwriteValidBackup() throws Exception {
        // 1. Buat save state valid pertama
        SaveState state1 = createSampleState(50_000_000L);
        saveManager.save(state1);

        File primary = saveManager.getSaveFile();
        File backup = saveManager.getBackupFile();
        assertTrue("Primary file harus ada", primary.exists());

        // 2. Buat save state valid kedua -> menghasilkan backupFile (.bak) valid
        SaveState state2 = createSampleState(60_000_000L);
        saveManager.save(state2);

        assertTrue("Backup file harus tercipta", backup.exists());
        String validBackupContent = new String(Files.readAllBytes(backup.toPath()), StandardCharsets.UTF_8);
        assertNotNull("Backup harus bisa diparse", SaveState.fromJsonString(validBackupContent));

        // 3. Simulasikan primary file rusak dengan teks > 10 byte (bukan sekadar 6 byte "BROKEN")
        String corruptContent = "{\"corrupted_json_garbage_data_exceeding_10_bytes_completely_invalid\": true";
        assertTrue("Panjang teks corrupt harus > 10 bytes", corruptContent.length() > 10);
        try (FileWriter fw = new FileWriter(primary, false)) {
            fw.write(corruptContent);
        }

        // 4. Lakukan penyimpanan baru state3
        SaveState state3 = createSampleState(65_000_000L);
        saveManager.save(state3);

        // Backup file HARUS TETAP valid (tidak boleh tertimpa oleh teks corrupt tadi!)
        assertTrue("Backup file harus tetap ada", backup.exists());
        String currentBackupContent = new String(Files.readAllBytes(backup.toPath()), StandardCharsets.UTF_8);
        SaveState backupState = SaveState.fromJsonString(currentBackupContent);
        assertNotNull("Backup tidak boleh rusak", backupState);

        // 5. Uji pemulihan jika primary rusak lagi
        try (FileWriter fw = new FileWriter(primary, false)) {
            fw.write("{truncated_unparsable_json_data_again_long_string_here");
        }
        SaveManager.LoadResult loadRes = saveManager.load();
        assertTrue("Load harus berhasil via recovery backup", loadRes.isSuccess());
        assertEquals(SaveManager.LoadSource.BACKUP_RECOVERED, loadRes.getSource());
    }

    @Test
    public void testCorruptPrimaryTruncatedDoesNotDestroyBackup() throws Exception {
        SaveState state1 = createSampleState(50_000_000L);
        saveManager.save(state1);

        SaveState state2 = createSampleState(55_000_000L);
        saveManager.save(state2);

        File primary = saveManager.getSaveFile();
        File backup = saveManager.getBackupFile();

        // Rusak primary dengan JSON terpotong (truncated)
        try (FileWriter fw = new FileWriter(primary, false)) {
            fw.write("{\"schemaVersion\":2,\"gameMode\":\"INTERNAL_MARKET\","); // truncated
        }

        SaveState state3 = createSampleState(60_000_000L);
        saveManager.save(state3);

        // Backup harus tetap valid
        String backupContent = new String(Files.readAllBytes(backup.toPath()), StandardCharsets.UTF_8);
        assertNotNull(SaveState.fromJsonString(backupContent));
    }

    /**
     * Regresi K-022:
     * Primary dengan struktur semantik rusak (misal accounts: 42, ledger: false, atau versi schema tidak didukung)
     * tidak boleh dianggap valid hanya karena memiliki key accounts/ledger.
     * Backup yang valid harus dipertahankan dan pemulihan (BACKUP_RECOVERED) harus berhasil.
     */
    @Test
    public void testCorruptStructureAndSemanticsDoesNotOverwriteValidBackup() throws Exception {
        SaveState state1 = createSampleState(50_000_000L);
        saveManager.save(state1);

        SaveState state2 = createSampleState(55_000_000L);
        saveManager.save(state2);

        File primary = saveManager.getSaveFile();
        File backup = saveManager.getBackupFile();
        String goodBackupContent = new String(Files.readAllBytes(backup.toPath()), StandardCharsets.UTF_8);
        assertNotNull(SaveState.fromJsonString(goodBackupContent));

        // 1. Kasus K-022: schemaVersion 2 tetapi accounts dan ledger bukan array
        String invalidStructure = "{\"schemaVersion\":2,\"accounts\":42,\"ledger\":false}";
        try (FileWriter fw = new FileWriter(primary, false)) {
            fw.write(invalidStructure);
        }

        SaveState liveState = createSampleState(60_000_000L);
        saveManager.save(liveState);

        // Backup HARUS TETAP merupakan backup baik sebelumnya (tidak tertimpa invalidStructure)
        String backupAfterInvalidSave = new String(Files.readAllBytes(backup.toPath()), StandardCharsets.UTF_8);
        assertEquals("Backup valid lama harus dipertahankan", goodBackupContent, backupAfterInvalidSave);
        assertNotNull("Backup harus bisa diparse oleh real parser", SaveState.fromJsonString(backupAfterInvalidSave));

        // Jika primary rusak, recovery dari backup harus sukses
        try (FileWriter fw = new FileWriter(primary, false)) {
            fw.write("{broken_unparsable");
        }
        SaveManager.LoadResult loadRes = saveManager.load();
        assertTrue("Load harus berhasil memulihkan backup", loadRes.isSuccess());
        assertEquals(SaveManager.LoadSource.BACKUP_RECOVERED, loadRes.getSource());

        // 2. Kasus schemaVersion tidak didukung (misal versi 999 atau 1 lama tanpa migrasi)
        try (FileWriter fw = new FileWriter(primary, false)) {
            fw.write("{\"schemaVersion\":999,\"gameMode\":\"INTERNAL_MARKET\",\"accounts\":[],\"ledger\":[]}");
        }
        SaveState liveState2 = createSampleState(65_000_000L);
        saveManager.save(liveState2);

        String backupAfterSchemaMismatch = new String(Files.readAllBytes(backup.toPath()), StandardCharsets.UTF_8);
        assertNotNull("Backup harus tetap loadable", SaveState.fromJsonString(backupAfterSchemaMismatch));

        // 3. Kasus tipe data akun ditolak parser (AccountType tidak valid)
        String invalidAccountType = "{\"schemaVersion\":2,\"gameMode\":\"INTERNAL_MARKET\",\"savedTimestamp\":1000,"
                + "\"gameDate\":\"2026-10-22\",\"gameTime\":\"10:00:00\","
                + "\"accounts\":[{\"id\":\"ACC-1\",\"name\":\"X\",\"type\":\"TIDAK_VALID\"}],\"ledger\":[]}";
        try (FileWriter fw = new FileWriter(primary, false)) {
            fw.write(invalidAccountType);
        }
        SaveState liveState3 = createSampleState(70_000_000L);
        saveManager.save(liveState3);

        String backupAfterParserReject = new String(Files.readAllBytes(backup.toPath()), StandardCharsets.UTF_8);
        assertNotNull("Backup harus tetap loadable", SaveState.fromJsonString(backupAfterParserReject));
    }
}
