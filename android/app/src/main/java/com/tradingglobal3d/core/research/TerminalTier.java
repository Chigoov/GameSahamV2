package com.tradingglobal3d.core.research;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

/**
 * Tingkatan Paket Terminal Riset Finansial (Berbasis USD Game).
 */
public enum TerminalTier {
    BASIC(1, "Basic Terminal", Money.of(0, Currency.USD), "Berita publik setelah rilis resmi & kuotasi pasar reguler"),
    PRO(2, "Pro Trader Terminal", Money.of(250_000.0, Currency.USD), "Warta awal 1 hari game (estimasi 2-5%), Order Book L2, & radar transaksi besar NPC"),
    ENTERPRISE(3, "Whale Taipan Terminal", Money.of(1_000_000.0, Currency.USD), "Sinyal data alternatif eksklusif & Berita Katalis Mega dengan jaminan kenaikan penutupan >20%");

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
