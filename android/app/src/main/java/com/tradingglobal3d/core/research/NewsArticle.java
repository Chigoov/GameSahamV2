package com.tradingglobal3d.core.research;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Entitas Berita Pasar Finansial berbobot sentimen & estimasi probabilistik (R12).
 * Mengimplementasikan countdown rilis, waktu berlalu, clue sektor, arah, rentang, dan horizon.
 */
public final class NewsArticle {
    public enum Category {
        MACRO("Makroekonomi"),
        SECTOR("Sektor Industri"),
        COMPANY("Emiten");

        private final String label;
        Category(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final String id;
    private final String headline;
    private final String content;
    private final Category category;
    private final String relatedTicker;   // Null jika berita makro
    private final String affectedSector;  // Sektor terdampak (e.g. "Keuangan", "Energi")
    private final String direction;       // "POSITIF", "NEGATIF", "NETRAL / CAMPURAN"
    private final String estimatedRange;  // e.g. "+2.5% s/d +6.0%"
    private final int horizonDays;        // e.g. 2 hari game
    private final String confidence;      // "TINGGI", "SEDANG", "RENDAH"
    private final double sentimentImpact; // -1.0 s/d +1.0
    private final long announcedAt;       // Waktu pengumuman jadwal awal
    private final long publishedAt;       // Timestamp rilis resmi dalam waktu game

    public NewsArticle(String id, String headline, String content, Category category,
                       String relatedTicker, double sentimentImpact, long publishedAt) {
        this(id, headline, content, category, relatedTicker,
                category == Category.COMPANY ? "Perusahaan Spesifik" : "Lintas Sektor",
                sentimentImpact > 0.1 ? "POSITIF" : sentimentImpact < -0.1 ? "NEGATIF" : "NETRAL / CAMPURAN",
                sentimentImpact > 0.1 ? "+2.0% s/d +6.0%" : sentimentImpact < -0.1 ? "-2.0% s/d -5.5%" : "±1.0%",
                2, "SEDANG", sentimentImpact, publishedAt - 3600_000L, publishedAt);
    }

    public NewsArticle(String id, String headline, String content, Category category,
                       String relatedTicker, String affectedSector, String direction,
                       String estimatedRange, int horizonDays, String confidence,
                       double sentimentImpact, long announcedAt, long publishedAt) {
        this.id = Objects.requireNonNull(id);
        this.headline = Objects.requireNonNull(headline);
        this.content = Objects.requireNonNull(content);
        this.category = Objects.requireNonNull(category);
        this.relatedTicker = relatedTicker;
        this.affectedSector = affectedSector != null ? affectedSector : "Umum";
        this.direction = direction != null ? direction : "NETRAL / CAMPURAN";
        this.estimatedRange = estimatedRange != null ? estimatedRange : "±1.5%";
        this.horizonDays = Math.max(1, horizonDays);
        this.confidence = confidence != null ? confidence : "SEDANG";
        this.sentimentImpact = sentimentImpact;
        this.announcedAt = announcedAt;
        this.publishedAt = publishedAt;
    }

    public String getId() { return id; }
    public String getHeadline() { return headline; }
    public String getContent() { return content; }
    public Category getCategory() { return category; }
    public String getRelatedTicker() { return relatedTicker; }
    public String getAffectedSector() { return affectedSector; }
    public String getDirection() { return direction; }
    public String getEstimatedRange() { return estimatedRange; }
    public int getHorizonDays() { return horizonDays; }
    public String getConfidence() { return confidence; }
    public double getSentimentImpact() { return sentimentImpact; }
    public long getAnnouncedAt() { return announcedAt; }
    public long getPublishedAt() { return publishedAt; }

    public boolean isAnnounced(long currentTimestamp) {
        return currentTimestamp >= announcedAt;
    }

    public boolean isPublished(long currentTimestamp) {
        return currentTimestamp >= publishedAt;
    }

    public boolean isHorizonActive(long currentTimestamp) {
        long expiresAt = publishedAt + ((long) horizonDays * 86_400_000L);
        return currentTimestamp >= publishedAt && currentTimestamp <= expiresAt;
    }

    public String getCountdownLabel(long currentTimestamp) {
        if (currentTimestamp < publishedAt) {
            long remainingMs = publishedAt - currentTimestamp;
            long hours = remainingMs / 3600_000L;
            long mins = (remainingMs % 3600_000L) / 60_000L;
            return String.format(java.util.Locale.ROOT, "Rilis dalam %02dj %02dm (waktu game)", hours, mins);
        } else {
            long elapsedMs = currentTimestamp - publishedAt;
            long hours = elapsedMs / 3600_000L;
            long mins = (elapsedMs % 3600_000L) / 60_000L;
            if (hours > 24) {
                return (hours / 24) + " hari game lalu";
            } else if (hours > 0) {
                return hours + "j " + mins + "m game lalu";
            } else {
                return Math.max(1, mins) + " menit game lalu";
            }
        }
    }

    public JSONObject toJsonObject(long currentTimestamp) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("category", category.name());
        obj.put("categoryLabel", category.getLabel());
        obj.put("relatedTicker", relatedTicker == null ? JSONObject.NULL : relatedTicker);
        obj.put("affectedSector", affectedSector);
        obj.put("direction", direction);
        obj.put("estimatedRange", estimatedRange);
        obj.put("horizonDays", horizonDays);
        obj.put("confidence", confidence);
        obj.put("publishedAt", publishedAt);
        obj.put("isPublished", isPublished(currentTimestamp));
        obj.put("countdownLabel", getCountdownLabel(currentTimestamp));

        if (isPublished(currentTimestamp)) {
            obj.put("headline", headline);
            obj.put("content", content);
            obj.put("sentimentImpact", sentimentImpact);
        } else {
            // Anti-future leak: isi lengkap dirahasiakan sebelum terbit, hanya clue yang tampil
            obj.put("headline", "[Jadwal Rilis] " + affectedSector + ": " + direction);
            obj.put("content", "Informasi resmi sedang dalam proses persiapan publikasi keterbukaan.");
            obj.put("sentimentImpact", 0.0);
        }
        return obj;
    }
}
