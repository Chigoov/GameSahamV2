package com.tradingglobal3d.core.market;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Loader dan validator dataset bar historis nyata (OHLCV).
 * Menegakkan integritas data:
 * - Menolak bar ilegal (Open/High/Low/Close <= 0, High < Low, High < Open/Close, Low > Open/Close, Volume < 0)
 * - Menolak tanggal duplikat dan urutan tanggal yang tidak berurutan naik (ascending)
 * - Menangani parsing format IDX (BEI) dan format US (Yahoo/Nasdaq)
 */
public final class HistoricalDatasetLoader {
    public static final String VERIFIED_DATASET_ID = "US:AAPL";
    public static final String VERIFIED_PATH = "data/historical/us_aapl_wiki_daily.csv";
    public static final String VERIFIED_SHA256 = "491f2ff9152e7792927444cc75d8ee60a9c87263d88f816fdcc0a9e79f8a3161";
    @FunctionalInterface public interface AssetSource { InputStream open(String path) throws IOException; }
    private static volatile AssetSource assetSource;

    /** Android supplies AssetManager::open; desktop checks the same bundled file. */
    public static void setAssetSource(AssetSource source) { assetSource = source; }

    public static List<String> availableDatasetIds() { return Collections.singletonList(VERIFIED_DATASET_ID); }

    public static String canonicalDatasetId(String id) {
        if (id != null && ("AAPL".equalsIgnoreCase(id.trim()) || VERIFIED_DATASET_ID.equalsIgnoreCase(id.trim()))) return VERIFIED_DATASET_ID;
        throw new IllegalArgumentException("Dataset belum tersedia dengan hak redistribusi terverifikasi: " + id);
    }

    public static List<HistoricalBar> loadFromCsvFile(File file) throws IOException {
        Objects.requireNonNull(file, "File tidak boleh null");
        if (!file.exists()) throw new IOException("File dataset tidak ditemukan: " + file.getAbsolutePath());

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return loadFromCsvString(sb.toString());
    }

    public static List<HistoricalBar> loadFromCsvString(String csvContent) throws IOException {
        Objects.requireNonNull(csvContent, "CSV content tidak boleh null");
        List<HistoricalBar> bars = new ArrayList<>();
        Set<LocalDate> seenDates = new HashSet<>();
        LocalDate previousDate = null;

        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new IOException("File CSV kosong");
            }

            String[] headers = headerLine.split(",");
            int dateIdx = -1, openIdx = -1, highIdx = -1, lowIdx = -1, closeIdx = -1, volIdx = -1;

            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].replace("\uFEFF", "").trim().toLowerCase(Locale.ROOT);
                if (h.equals("date")) dateIdx = i;
                else if (h.equals("open") || h.equals("open_price")) openIdx = i;
                else if (h.equals("high")) highIdx = i;
                else if (h.equals("low")) lowIdx = i;
                else if (h.equals("close")) closeIdx = i;
                else if (h.equals("volume")) volIdx = i;
            }

            if (dateIdx < 0 || openIdx < 0 || highIdx < 0 || lowIdx < 0 || closeIdx < 0) {
                throw new IOException("Header CSV tidak memuat kolom OHLC wajib (Date, Open, High, Low, Close)");
            }

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",", -1);
                int maxIdx = Math.max(Math.max(dateIdx, openIdx), Math.max(Math.max(highIdx, lowIdx), Math.max(closeIdx, volIdx)));
                if (parts.length <= maxIdx) {
                    throw new IOException(String.format("Baris %d tidak memiliki kolom yang cukup: %s", lineNum, line));
                }

                LocalDate date = LocalDate.parse(parts[dateIdx].trim());
                BigDecimal open = new BigDecimal(parts[openIdx].trim());
                BigDecimal high = new BigDecimal(parts[highIdx].trim());
                BigDecimal low = new BigDecimal(parts[lowIdx].trim());
                BigDecimal close = new BigDecimal(parts[closeIdx].trim());
                long volume = volIdx >= 0 ? new BigDecimal(parts[volIdx].trim()).longValueExact() : 0L;

                // Validasi urutan tanggal (harus ascending) dan deteksi duplikat
                if (seenDates.contains(date)) {
                    throw new IllegalArgumentException(String.format("Tanggal duplikat terdeteksi pada baris %d: %s", lineNum, date));
                }
                if (previousDate != null && date.isBefore(previousDate)) {
                    throw new IllegalArgumentException(String.format("Urutan tanggal tidak urut/mundur pada baris %d: %s sebelum %s", lineNum, date, previousDate));
                }

                HistoricalBar bar = new HistoricalBar(date, open, high, low, close, volume);
                bars.add(bar);
                seenDates.add(date);
                previousDate = date;
            }
        }

        return Collections.unmodifiableList(bars);
    }

    public static File findDatasetFile(String relativePath) {
        String clean = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        File[] candidates = new File[] {
            new File(clean),
            new File("android/app/src/main/assets", clean),
            new File("src/main/assets", clean),
            new File("app/src/main/assets", clean),
            new File("../android/app/src/main/assets", clean)
        };
        for (File f : candidates) {
            if (f.exists() && f.isFile()) return f;
        }
        return candidates[candidates.length - 1];
    }

    public static List<HistoricalBar> loadDataset(String datasetId) throws IOException {
        canonicalDatasetId(datasetId);
        byte[] bytes;
        AssetSource source = assetSource;
        try (InputStream input = source == null ? new java.io.FileInputStream(findDatasetFile(VERIFIED_PATH)) : source.open(VERIFIED_PATH)) {
            java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int size;
            while ((size = input.read(buffer)) != -1) output.write(buffer, 0, size);
            bytes = output.toByteArray();
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder actual = new StringBuilder();
            for (byte b : digest) actual.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            if (!VERIFIED_SHA256.equals(actual.toString())) throw new IOException("Checksum dataset historis tidak cocok");
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 tidak tersedia", e);
        }
        return loadFromCsvString(new String(bytes, StandardCharsets.UTF_8));
    }
}
