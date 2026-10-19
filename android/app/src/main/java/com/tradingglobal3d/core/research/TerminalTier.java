package com.tradingglobal3d.core.research;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

/**
 * Tingkatan Paket Terminal Riset Finansial (Berbasis USD Game).
 */
public enum TerminalTier {
    BASIC(1, "Basic Terminal", Money.of(0, Currency.USD), "Berita publik dan kuotasi standar 15 menit delayed"),
    PRO(2, "Pro Trader Terminal", Money.of(500.0, Currency.USD), "Kuotasi real-time, Order Book L2, & konsensus analis"),
    ENTERPRISE(3, "Enterprise Institutional", Money.of(2000.0, Currency.USD), "Sinyal data alternatif, sentimen makro & kalender eksklusif");

    private final int level;
    private final String title;
    private final Money monthlyFeeUsd;
    private final String features;

    TerminalTier(int level, String title, Money monthlyFeeUsd, String features) {
        this.level = level;
        this.title = title;
        this.monthlyFeeUsd = monthlyFeeUsd;
        this.features = features;
    }

    public int getLevel() { return level; }
    public String getTitle() { return title; }
    public Money getMonthlyFeeUsd() { return monthlyFeeUsd; }
    public String getFeatures() { return features; }

    public boolean includes(TerminalTier other) {
        return this.level >= other.level;
    }
}
