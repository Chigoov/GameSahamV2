package com.tradingglobal3d.core.career;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

public enum CareerRank {
    NOVICE_TRADER(1, "Novice Trader", Money.of(0, Currency.IDR), "Trading modal pribadi, akses saham dasar"),
    SENIOR_ANALYST(2, "Senior Analyst", Money.of(500_000, Currency.IDR), "Membuka kalender bursa, terminal riset & gaji harian"),
    PORTFOLIO_MANAGER(3, "Portfolio Manager", Money.of(2_500_000, Currency.IDR), "Membuka mandat klien, fasilitas margin & short selling"),
    CHIEF_INVESTMENT_OFFICER(4, "Chief Investment Officer", Money.of(10_000_000, Currency.IDR), "Mandat institusional besar & multi-aset lanjutan");

    private final int level;
    private final String title;
    private final Money dailySalary;
    private final String description;

    CareerRank(int level, String title, Money dailySalary, String description) {
        this.level = level;
        this.title = title;
        this.dailySalary = dailySalary;
        this.description = description;
    }

    public int getLevel() { return level; }
    public String getTitle() { return title; }
    public Money getDailySalary() { return dailySalary; }
    public String getDescription() { return description; }

    public CareerRank getNextRank() {
        switch (this) {
            case NOVICE_TRADER: return SENIOR_ANALYST;
            case SENIOR_ANALYST: return PORTFOLIO_MANAGER;
            case PORTFOLIO_MANAGER: return CHIEF_INVESTMENT_OFFICER;
            default: return null;
        }
    }
}
