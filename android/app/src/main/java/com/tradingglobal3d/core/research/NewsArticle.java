package com.tradingglobal3d.core.research;

import java.util.Objects;

/**
 * Entitas Berita Pasar Finansial berbobot sentimen.
 * Mengimplementasikan anti-future leak: berita hanya dapat diakses
 * setelah waktu rilisnya (publishedAt <= currentTimestamp).
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
    private final double sentimentImpact; // -1.0 (sangat negatif) s/d +1.0 (sangat positif)
    private final long publishedAt;       // Timestamp rilis dalam waktu game

    public NewsArticle(String id, String headline, String content, Category category,
                       String relatedTicker, double sentimentImpact, long publishedAt) {
        this.id = Objects.requireNonNull(id);
        this.headline = Objects.requireNonNull(headline);
        this.content = Objects.requireNonNull(content);
        this.category = Objects.requireNonNull(category);
        this.relatedTicker = relatedTicker;
        this.sentimentImpact = sentimentImpact;
        this.publishedAt = publishedAt;
    }

    public String getId() { return id; }
    public String getHeadline() { return headline; }
    public String getContent() { return content; }
    public Category getCategory() { return category; }
    public String getRelatedTicker() { return relatedTicker; }
    public double getSentimentImpact() { return sentimentImpact; }
    public long getPublishedAt() { return publishedAt; }

    public boolean isPublished(long currentTimestamp) {
        return currentTimestamp >= publishedAt;
    }
}
