package com.tradingglobal3d.core.research;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Agenda Kalender Finansial (Laporan Keuangan, Libur Bursa, Aksi Korporasi).
 */
public final class CalendarEvent {
    public enum EventCategory {
        EARNINGS_RELEASE("Laporan Keuangan (Earnings)"),
        CORPORATE_ACTION("Aksi Korporasi / Dividen"),
        MACRO_POLICY("Rapat Suku Bunga Makro"),
        MARKET_HOLIDAY("Hari Libur Bursa");

        private final String label;
        EventCategory(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final String id;
    private final LocalDate date;
    private final String title;
    private final EventCategory category;
    private final String relatedTicker;
    private final String details;

    public CalendarEvent(String id, LocalDate date, String title, EventCategory category,
                         String relatedTicker, String details) {
        this.id = Objects.requireNonNull(id);
        this.date = Objects.requireNonNull(date);
        this.title = Objects.requireNonNull(title);
        this.category = Objects.requireNonNull(category);
        this.relatedTicker = relatedTicker;
        this.details = Objects.requireNonNull(details);
    }

    public String getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getTitle() { return title; }
    public EventCategory getCategory() { return category; }
    public String getRelatedTicker() { return relatedTicker; }
    public String getDetails() { return details; }
}
