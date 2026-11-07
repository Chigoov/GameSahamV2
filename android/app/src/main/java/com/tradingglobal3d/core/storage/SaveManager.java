package com.tradingglobal3d.core.storage;

import org.json.JSONException;
import org.json.JSONObject;

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
    private java.util.function.BooleanSupplier writeAuthority = () -> true;
    private byte[] validatedPrimaryDigest;
    private long validatedPrimaryTimestamp;
    private boolean migrationReadbackRequired;

    public void setStateValidator(java.util.function.Consumer<SaveState> validator) {
        Objects.requireNonNull(validator);
        if (stateValidator != validator) invalidateValidatedPrimary();
        stateValidator = validator;
    }

    public void invalidateValidatedPrimary() { validatedPrimaryDigest = null; }

    public synchronized void setWriteAuthority(java.util.function.BooleanSupplier authority) {
        writeAuthority = Objects.requireNonNull(authority);
    }

    private static java.security.MessageDigest newDigest() {
        try { return java.security.MessageDigest.getInstance("SHA-256"); }
        catch (java.security.NoSuchAlgorithmException unavailable) { throw new IllegalStateException(unavailable); }
    }

    private static byte[] digest(File file) throws IOException {
        java.security.MessageDigest digest = newDigest();
        try (InputStream input = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) digest.update(buffer, 0, count);
        }
        return digest.digest();
    }

    public SaveState readValidatedState(String content) throws JSONException {
        SaveState state = SaveState.fromJsonString(content);
        validateState(state, false);
        return state;
    }

    private SaveState readValidatedFile(File file, java.security.MessageDigest digest) throws IOException, JSONException {
        SaveState state;
        try (InputStream fileInput = new FileInputStream(file);
             InputStream input = digest == null ? fileInput : new java.security.DigestInputStream(fileInput, digest);
             Reader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8.newDecoder()))) {
            state = SaveState.fromReader(reader);
        }
        validateState(state, false);
        SaveState migrated = state.compactValidatedNpcLedger();
        if (migrated != state) migrationReadbackRequired = true;
        return migrated;
    }

    private void validateState(SaveState state) throws JSONException {
        validateState(state, true);
    }

    private void validateState(SaveState state, boolean detach) throws JSONException {
        if (!SaveState.supportsVersion(state.getSchemaVersion())) throw new IllegalArgumentException("Versi save belum didukung");
        if ((state.getSchemaVersion() == SaveState.COMPACTED_SCHEMA_VERSION) != state.getRuntimeState().has("npcLedgerSummary"))
            throw new IllegalArgumentException("Versi save dan ringkasan ledger NPC tidak cocok");
        if (state.getAccounts().isEmpty()) throw new IllegalArgumentException("Save tidak memiliki akun");
        Objects.requireNonNull(state.getGameMode());
        Objects.requireNonNull(state.getGameDate());
        Objects.requireNonNull(state.getGameTime());
        if (state.getGameMsRemainder() < 0 || state.getGameMsRemainder() > 999) throw new IllegalArgumentException("Remainder harus 0..999");
        // Parsed files already belong exclusively to this validation. Live snapshots still require isolation.
        stateValidator.accept(detach ? state.detachedCopy() : state);
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
        File tempFile = File.createTempFile("save-", ".tmp", baseDir);
        File targetFile = getSaveFile();
        File backupFile = getBackupFile();
        java.security.MessageDigest writtenDigest = newDigest();

        try {
            // 1. Tulis ke file temporary dengan disk sync
            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 OutputStreamWriter osw = new OutputStreamWriter(
                         new java.security.DigestOutputStream(fos, writtenDigest), StandardCharsets.UTF_8);
                 BufferedWriter bw = new BufferedWriter(osw)) {
                state.writeJson(bw);
                bw.flush();
                fos.getFD().sync();
            }
            byte[] candidateDigest = writtenDigest.digest();
            if (migrationReadbackRequired) {
                SaveState roundTrip = readValidatedFile(tempFile, null);
                java.security.MessageDigest roundTripDigest = newDigest();
                try (Writer check = new BufferedWriter(new OutputStreamWriter(new java.security.DigestOutputStream(
                        new OutputStream() {
                            @Override public void write(int value) {}
                            @Override public void write(byte[] bytes, int offset, int length) {}
                        }, roundTripDigest), StandardCharsets.UTF_8))) {
                    roundTrip.writeJson(check);
                }
                if (!java.util.Arrays.equals(candidateDigest, roundTripDigest.digest()))
                    throw new IOException("Kandidat migrasi berubah saat validasi round-trip");
            }

            // ponytail: serialize commits across instances; use per-path locks if multiple save directories contend.
            synchronized (SaveManager.class) {
                if (!writeAuthority.getAsBoolean()) throw new IllegalStateException("Pemilik save sudah digantikan; penulisan ditolak");
                // 2. Rotasi backup dari file lama jika ada dan valid
                if (targetFile.exists() && targetFile.length() > 0) {
                    boolean isTargetValid = false;
                    long existingSavedTimestamp = -1L;
                    try {
                        // ponytail: reuse validation only for identical bytes under the same domain validator; changed files are fully revalidated.
                        existingSavedTimestamp = validatedPrimaryDigest != null
                                && java.util.Arrays.equals(validatedPrimaryDigest, digest(targetFile))
                                ? validatedPrimaryTimestamp
                                : readValidatedFile(targetFile, null).getSavedTimestamp();
                        isTargetValid = true;
                    } catch (Exception invalidTarget) {
                        // Primary rusak (>10 byte, truncated, atau JSON tidak valid):
                        // JANGAN timpa backup! Pertahankan last-good backup yang masih valid.
                        isTargetValid = false;
                    }

                    if (isTargetValid) {
                        // R04: Perlindungan Generation Fence - Cegah snapshot lama menimpa snapshot yang lebih baru di disk
                            if (existingSavedTimestamp > state.getSavedTimestamp()) {
                                throw new IllegalStateException("Penolakan penulisan save: snapshot yang akan disimpan (savedTimestamp: "
                                        + state.getSavedTimestamp() + ") lebih lama daripada save di disk (savedTimestamp: "
                                        + existingSavedTimestamp + ")");
                            }

                        Path tempBackup = Files.createTempFile(baseDir.toPath(), "bak-", ".tmp");
                        try {
                            Files.copy(targetFile.toPath(), tempBackup, StandardCopyOption.REPLACE_EXISTING);
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
                validatedPrimaryDigest = candidateDigest;
                validatedPrimaryTimestamp = state.getSavedTimestamp();
                migrationReadbackRequired = false;
            }
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
                java.security.MessageDigest primaryDigest = newDigest();
                SaveState state = readValidatedFile(targetFile, primaryDigest);
                validatedPrimaryDigest = primaryDigest.digest();
                validatedPrimaryTimestamp = state.getSavedTimestamp();
                return new LoadResult(state, LoadSource.PRIMARY, "Berhasil memuat save utama");
            } catch (Exception e) {
                // File utama korup, coba beralih ke backup
                System.err.println("Peringatan: Save utama korup (" + e.getMessage() + "), mencoba pemulihan dari backup...");
            }
        }

        // Coba load file backup
        if (backupFile.exists()) {
            try {
                SaveState state = readValidatedFile(backupFile, null);
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
        return readFileToString(file, null);
    }

    private String readFileToString(File file, java.security.MessageDigest digest) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        if (digest != null) digest.update(bytes);
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
