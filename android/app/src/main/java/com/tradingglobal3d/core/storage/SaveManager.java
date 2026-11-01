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
    private java.util.function.Consumer<SaveState> stateValidator = state -> {};
    private byte[] validatedPrimaryDigest;

    public void setStateValidator(java.util.function.Consumer<SaveState> validator) {
        Objects.requireNonNull(validator);
        if (stateValidator != validator) invalidateValidatedPrimary();
        stateValidator = validator;
    }

    public void invalidateValidatedPrimary() { validatedPrimaryDigest = null; }

    private static byte[] digest(String content) {
        try { return java.security.MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8)); }
        catch (java.security.NoSuchAlgorithmException unavailable) { throw new IllegalStateException(unavailable); }
    }

    public SaveState readValidatedState(String content) throws JSONException {
        SaveState state = SaveState.fromJsonString(content);
        validateState(state);
        return state;
    }

    private void validateState(SaveState state) throws JSONException {
        if (state.getSchemaVersion() != SaveState.SCHEMA_VERSION) throw new IllegalArgumentException("Versi save belum didukung");
        if (state.getAccounts().isEmpty()) throw new IllegalArgumentException("Save tidak memiliki akun");
        Objects.requireNonNull(state.getGameMode());
        Objects.requireNonNull(state.getGameDate());
        Objects.requireNonNull(state.getGameTime());
        if (state.getGameMsRemainder() < 0 || state.getGameMsRemainder() > 999) throw new IllegalArgumentException("Remainder harus 0..999");
        stateValidator.accept(state.detachedCopy());
    }

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

        validateState(state);
        String jsonStr = state.toJsonString();
        File tempFile = getTempFile();
        File targetFile = getSaveFile();
        File backupFile = getBackupFile();

        // 1. Tulis ke file temporary dengan disk sync
        try (FileOutputStream fos = new FileOutputStream(tempFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {
            bw.write(jsonStr);
            bw.flush();
            fos.getFD().sync();
        }

        // 2. Rotasi backup dari file lama jika ada dan valid
        if (targetFile.exists() && targetFile.length() > 0) {
            boolean isTargetValid = false;
            String existingContent = null;
            try {
                existingContent = readFileToString(targetFile);
                // ponytail: reuse validation only for identical bytes under the same domain validator; changed files are fully revalidated.
                if (!java.util.Arrays.equals(validatedPrimaryDigest, digest(existingContent))) readValidatedState(existingContent);
                isTargetValid = true;
            } catch (Exception invalidTarget) {
                // Primary rusak (>10 byte, truncated, atau JSON tidak valid):
                // JANGAN timpa backup! Pertahankan last-good backup yang masih valid.
                isTargetValid = false;
            }

            if (isTargetValid) {
                Path tempBackup = Files.createTempFile(baseDir.toPath(), "bak-", ".tmp");
                try {
                    Files.write(tempBackup, existingContent.getBytes(StandardCharsets.UTF_8));
                    try (FileOutputStream bfos = new FileOutputStream(tempBackup.toFile(), true)) {
                        bfos.getFD().sync();
                    }
                    try {
                        Files.move(tempBackup, backupFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING,
                                StandardCopyOption.ATOMIC_MOVE);
                    } catch (java.nio.file.FileSystemException e) {
                        robustMove(tempBackup, backupFile.toPath());
                    }
                } finally {
                    Files.deleteIfExists(tempBackup);
                }
            }
        }

        // 3. Verifikasi tempFile dan Atomic replace temp ke target file
        try {
            if (!tempFile.exists() || tempFile.length() == 0) {
                throw new IOException("Temp save file kosong atau tidak ditemukan");
            }
            try {
                Files.move(tempFile.toPath(), targetFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.FileSystemException e) {
                robustMove(tempFile.toPath(), targetFile.toPath());
            }
            validatedPrimaryDigest = digest(jsonStr);
        } finally {
            Files.deleteIfExists(tempFile.toPath());
        }
    }

    private static void robustMove(Path source, Path target) throws IOException {
        IOException lastError = null;
        for (int attempt = 0; attempt < 10; attempt++) {
            try {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                return;
            } catch (IOException ex) {
                lastError = ex;
                try { Thread.sleep(20); } catch (InterruptedException ignored) {}
            }
        }
        if (lastError != null) throw lastError;
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
                SaveState state = readValidatedState(content);
                validatedPrimaryDigest = digest(content);
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
                SaveState state = readValidatedState(content);
                return new LoadResult(state, LoadSource.BACKUP_RECOVERED, "Berhasil dipulihkan dari backup (.bak)");
            } catch (Exception e) {
                return new LoadResult(null, LoadSource.CORRUPTED_ALL, "Save utama dan backup keduanya rusak: " + e.getMessage());
            }
        }

        return new LoadResult(null, LoadSource.CORRUPTED_ALL, "Gagal memuat save file");
    }

    /** Keeps an immutable, validated primary snapshot per prestige request. */
    public synchronized String createPrestigeBackup(String requestId) throws IOException, JSONException {
        if (requestId == null || !requestId.matches("[A-Za-z0-9_-]{1,160}"))
            throw new IllegalArgumentException("ID backup prestige tidak valid");
        Path directory = prestigeDirectory();
        Files.createDirectories(directory);
        if (Files.isSymbolicLink(directory)) throw new IOException("Direktori backup tidak aman");
        Path target = directory.resolve(requestId + ".json");
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            validatePrestigePath(target.toString());
            readValidatedState(readFileToString(target.toFile()));
            return target.toAbsolutePath().toString();
        }
        Path primary = getSaveFile().toPath();
        if (Files.isSymbolicLink(primary) || !Files.isRegularFile(primary)) throw new IOException("Save utama valid wajib tersedia");
        byte[] bytes = Files.readAllBytes(primary);
        readValidatedState(new String(bytes, StandardCharsets.UTF_8));
        Path temporary = Files.createTempFile(directory, "prestige-", ".tmp");
        try {
            try (FileOutputStream output = new FileOutputStream(temporary.toFile())) {
                output.write(bytes); output.getFD().sync();
            }
            if (!java.util.Arrays.equals(bytes, Files.readAllBytes(temporary))) throw new IOException("Verifikasi backup gagal");
            readValidatedState(readFileToString(temporary.toFile()));
            // ponytail: refuse unsupported atomic backup rather than acknowledge a partial recovery point.
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            return target.toAbsolutePath().toString();
        } finally { Files.deleteIfExists(temporary); }
    }

    public synchronized SaveState restorePrestigeBackup(String backupPath) throws IOException, JSONException {
        Path backup = validatePrestigePath(backupPath);
        SaveState state = readValidatedState(readFileToString(backup.toFile()));
        save(state);
        return state;
    }

    private Path prestigeDirectory() throws IOException {
        Path base = baseDir.getCanonicalFile().toPath();
        Path directory = base.resolve("prestige-backups");
        if (Files.isSymbolicLink(directory) || !directory.toFile().getCanonicalFile().toPath().getParent().equals(base))
            throw new IOException("Direktori backup tidak aman");
        return directory;
    }

    private Path validatePrestigePath(String backupPath) throws IOException {
        if (backupPath == null) throw new IllegalArgumentException("Path backup wajib diisi");
        Path path = Paths.get(backupPath).toAbsolutePath();
        Path directory = prestigeDirectory();
        if (!path.equals(path.normalize()) || !directory.equals(path.getParent())
                || !path.getFileName().toString().matches("[A-Za-z0-9_-]{1,160}\\.json")
                || Files.isSymbolicLink(path) || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                || !path.toFile().getCanonicalFile().toPath().getParent().equals(directory))
            throw new IOException("Path backup prestige tidak aman");
        return path;
    }

    public synchronized org.json.JSONObject getSaveMetadata() throws org.json.JSONException {
        File primary = getSaveFile();
        File backup = getBackupFile();
        boolean primaryExists = primary.exists() && primary.length() > 0;
        boolean backupExists = backup.exists() && backup.length() > 0;
        org.json.JSONObject meta = new org.json.JSONObject();
        meta.put("format", "JSON Standar (Skema V2, UTF-8)");
        meta.put("primaryExists", primaryExists);
        meta.put("primaryLastModified", primaryExists ? primary.lastModified() : org.json.JSONObject.NULL);
        meta.put("primarySizeBytes", primaryExists ? primary.length() : 0);
        meta.put("backupRotationSupported", true);
        meta.put("backupFileExists", backupExists);
        meta.put("backupLastModified", backupExists ? backup.lastModified() : org.json.JSONObject.NULL);
        meta.put("backupSizeBytes", backupExists ? backup.length() : 0);
        meta.put("backupStatusLabel", backupExists ? "Tersedia (" + backup.length() + " bytes)" : "Belum tersedia (dibuat pada penyimpanan berikutnya)");
        return meta;
    }

    public static boolean validateSaveIntegrity(String content) {
        if (content == null || content.trim().isEmpty()) return false;
        try {
            org.json.JSONObject root = new org.json.JSONObject(content);
            if (root.getInt("schemaVersion") != SaveState.SCHEMA_VERSION) return false;
            com.tradingglobal3d.core.model.GameMode.valueOf(root.getString("gameMode"));
            root.getLong("savedTimestamp");
            java.time.LocalDate.parse(root.getString("gameDate"));
            java.time.LocalTime.parse(root.getString("gameTime"));
            SaveState state = SaveState.fromJsonString(content);
            if (state == null || state.getAccounts() == null || state.getAccounts().isEmpty()) return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String readBackupContent() throws IOException {
        return readFileToString(getBackupFile());
    }

    public String readPrimaryContent() throws IOException {
        return readFileToString(getSaveFile());
    }

    private String readFileToString(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes)).toString();
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
