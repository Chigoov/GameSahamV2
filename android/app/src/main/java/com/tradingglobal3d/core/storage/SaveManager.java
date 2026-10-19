package com.tradingglobal3d.core.storage;

import org.json.JSONException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Objects;

/**
 * Manajer penyimpanan game dengan penulisan atomik (atomic move)
 * dan perlindungan korupsi data dengan rotasi backup otomatis (.bak).
 */
public final class SaveManager {
    private final File baseDir;
    private final String filename;

    public SaveManager(File baseDir, String filename) {
        this.baseDir = Objects.requireNonNull(baseDir);
        this.filename = Objects.requireNonNull(filename);
    }

    public static SaveManager forDirectory(File baseDir) {
        return new SaveManager(baseDir, "save_game_v2.json");
    }

    public File getSaveFile() {
        return new File(baseDir, filename);
    }

    public File getBackupFile() {
        return new File(baseDir, filename + ".bak");
    }

    public File getTempFile() {
        return new File(baseDir, filename + ".tmp");
    }

    /**
     * Menyimpan SaveState secara atomik.
     * Alur:
     * 1. Tulis isi JSON ke .tmp
     * 2. Jika file .json lama ada dan valid, buat salinan ke .bak
     * 3. Rename .tmp ke .json secara atomik
     */
    public synchronized void save(SaveState state) throws IOException, JSONException {
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            throw new IOException("Gagal membuat direktori save: " + baseDir.getAbsolutePath());
        }

        String jsonStr = state.toJsonString();
        File tempFile = getTempFile();
        File targetFile = getSaveFile();
        File backupFile = getBackupFile();

        // 1. Tulis ke file temporary
        try (FileOutputStream fos = new FileOutputStream(tempFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {
            bw.write(jsonStr);
            bw.flush();
            fos.getFD().sync(); // Paksa flush ke disk fisik
        }

        // 2. Rotasi backup dari file lama jika ada
        if (targetFile.exists()) {
            boolean validPrimary;
            try {
                SaveState.fromJsonString(readFileToString(targetFile));
                validPrimary = true;
            } catch (Exception invalidPrimary) {
                validPrimary = false;
            }
            if (validPrimary) {
                // Jangan merusak backup valid atau mengakui save jika rotasi gagal.
                Files.copy(targetFile.toPath(), backupFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES);
            }
        }

        // 3. Atomic replace temp ke target file
        try {
            Files.move(tempFile.toPath(), targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile.toPath(), targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Memuat SaveState dari disk.
     * Jika file target korup / tidak valid, otomatis beralih ke file backup (.bak).
     */
    public synchronized LoadResult load() {
        File targetFile = getSaveFile();
        File backupFile = getBackupFile();

        if (!targetFile.exists() && !backupFile.exists()) {
            return new LoadResult(null, LoadSource.NOT_FOUND, "Tidak ditemukan save file");
        }

        // Coba load file utama
        if (targetFile.exists()) {
            try {
                String content = readFileToString(targetFile);
                SaveState state = SaveState.fromJsonString(content);
                return new LoadResult(state, LoadSource.PRIMARY, "Berhasil memuat save utama");
            } catch (Exception e) {
                // File utama korup, coba beralih ke backup
                System.err.println("Peringatan: Save utama korup (" + e.getMessage() + "), mencoba pemulihan dari backup...");
            }
        }

        // Coba load file backup
        if (backupFile.exists()) {
            try {
                String content = readFileToString(backupFile);
                SaveState state = SaveState.fromJsonString(content);
                return new LoadResult(state, LoadSource.BACKUP_RECOVERED, "Berhasil dipulihkan dari backup (.bak)");
            } catch (Exception e) {
                return new LoadResult(null, LoadSource.CORRUPTED_ALL, "Save utama dan backup keduanya rusak: " + e.getMessage());
            }
        }

        return new LoadResult(null, LoadSource.CORRUPTED_ALL, "Gagal memuat save file");
    }

    private String readFileToString(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public enum LoadSource {
        NOT_FOUND,
        PRIMARY,
        BACKUP_RECOVERED,
        CORRUPTED_ALL
    }

    public static final class LoadResult {
        private final SaveState state;
        private final LoadSource source;
        private final String message;

        public LoadResult(SaveState state, LoadSource source, String message) {
            this.state = state;
            this.source = source;
            this.message = message;
        }

        public SaveState getState() { return state; }
        public LoadSource getSource() { return source; }
        public String getMessage() { return message; }
        public boolean isSuccess() { return state != null; }
    }
}
