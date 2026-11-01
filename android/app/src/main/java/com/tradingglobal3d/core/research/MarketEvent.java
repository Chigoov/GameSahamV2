package com.tradingglobal3d.core.research;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entitas Sentimen Berita Makro & Peristiwa Pasar (Fitur 2).
 * Menyediakan bobot pengali tren per sektor saham secara logis per sesi pasar.
 */
public final class MarketEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum EventImpact {
        VERY_POSITIVE("Sangat Positif", 1.05),
        POSITIVE("Positif", 1.025),
        NEUTRAL("Netral", 1.0),
        NEGATIVE("Negatif", 0.975),
        VERY_NEGATIVE("Sangat Negatif", 0.95);

        private final String label;
        private final double sectorMultiplier;

        EventImpact(String label, double sectorMultiplier) {
            this.label = label;
            this.sectorMultiplier = sectorMultiplier;
        }

        public String getLabel() { return label; }
        public double getSectorMultiplier() { return sectorMultiplier; }
    }

    private final String eventId;
    private final String headline;
    private final String summary;
    private final String targetSector; // e.g. "Keuangan", "Energi", "Teknologi", "Konsumer", "Semua"
    private final EventImpact impact;
    private final long sessionTimestamp;
    private final boolean isPublished;

    public MarketEvent(String eventId, String headline, String summary, String targetSector,
                       EventImpact impact, long sessionTimestamp, boolean isPublished) {
        this.eventId = Objects.requireNonNull(eventId);
        this.headline = Objects.requireNonNull(headline);
        this.summary = Objects.requireNonNull(summary);
        this.targetSector = targetSector != null ? targetSector : "Semua";
        this.impact = Objects.requireNonNull(impact);
        this.sessionTimestamp = sessionTimestamp;
        this.isPublished = isPublished;
    }

    public String getEventId() { return eventId; }
    public String getHeadline() { return headline; }
    public String getSummary() { return summary; }
    public String getTargetSector() { return targetSector; }
    public EventImpact getImpact() { return impact; }
    public long getSessionTimestamp() { return sessionTimestamp; }
    public boolean isPublished() { return isPublished; }

    public double getSectorPriceMultiplier(String sector) {
        if (!isPublished) return 1.0;
        if ("Semua".equalsIgnoreCase(targetSector) || targetSector.equalsIgnoreCase(sector)) {
            return impact.getSectorMultiplier();
        }
        return 1.0;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("eventId", eventId);
        obj.put("headline", headline);
        obj.put("summary", summary);
        obj.put("targetSector", targetSector);
        obj.put("impact", impact.name());
        obj.put("impactLabel", impact.getLabel());
        obj.put("multiplier", impact.getSectorMultiplier());
        obj.put("sessionTimestamp", sessionTimestamp);
        obj.put("isPublished", isPublished);
        return obj;
    }
}
