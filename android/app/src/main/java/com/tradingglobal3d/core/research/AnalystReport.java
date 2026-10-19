package com.tradingglobal3d.core.research;

import com.tradingglobal3d.core.model.Money;

import java.util.Objects;

/**
 * Laporan Analisis Emiten oleh Analis Pasar NPC.
 * Memuat rekomendasi, target price, rekam akurasi historis, dan tesis analisis.
 */
public final class AnalystReport {
    public enum Recommendation {
        BUY("Beli"),
        HOLD("Tahan"),
        SELL("Jual");

        private final String label;
        Recommendation(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final String id;
    private final String analystName;
    private final String firmName;
    private final String ticker;
    private final Recommendation recommendation;
    private final Money targetPrice;
    private final int accuracyHistoryPercent; // e.g. 68%
    private final String thesisSummary;
    private final long publishedAt;

    public AnalystReport(String id, String analystName, String firmName, String ticker,
                         Recommendation recommendation, Money targetPrice,
                         int accuracyHistoryPercent, String thesisSummary, long publishedAt) {
        this.id = Objects.requireNonNull(id);
        this.analystName = Objects.requireNonNull(analystName);
        this.firmName = Objects.requireNonNull(firmName);
        this.ticker = Objects.requireNonNull(ticker);
        this.recommendation = Objects.requireNonNull(recommendation);
        this.targetPrice = Objects.requireNonNull(targetPrice);
        this.accuracyHistoryPercent = accuracyHistoryPercent;
        this.thesisSummary = Objects.requireNonNull(thesisSummary);
        this.publishedAt = publishedAt;
    }

    public String getId() { return id; }
    public String getAnalystName() { return analystName; }
    public String getFirmName() { return firmName; }
    public String getTicker() { return ticker; }
    public Recommendation getRecommendation() { return recommendation; }
    public Money getTargetPrice() { return targetPrice; }
    public int getAccuracyHistoryPercent() { return accuracyHistoryPercent; }
    public String getThesisSummary() { return thesisSummary; }
    public long getPublishedAt() { return publishedAt; }

    public boolean isPublished(long currentTimestamp) {
        return currentTimestamp >= publishedAt;
    }
}
