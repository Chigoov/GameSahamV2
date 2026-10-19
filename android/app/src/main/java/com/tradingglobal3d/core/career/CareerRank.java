package com.tradingglobal3d.core.career;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

public enum CareerRank {
    NOVICE_TRADER(1, "Novice Trader", Money.of(0, Currency.IDR), "Trading modal pribadi, akses saham dasar", 1, 0, 0),
    SENIOR_ANALYST(2, "Senior Analyst", Money.of(500_000, Currency.IDR), "Membuka kalender bursa, terminal riset & gaji harian", 2, 30, 0),
    PORTFOLIO_MANAGER(3, "Portfolio Manager", Money.of(2_500_000, Currency.IDR), "Membuka mandat klien, fasilitas margin & short selling", 4, 50, 75_000_000),
    CHIEF_INVESTMENT_OFFICER(4, "Chief Investment Officer", Money.of(10_000_000, Currency.IDR), "Mandat institusional besar & multi-aset lanjutan", 7, 80, 200_000_000);

    private final int level;
    private final String title;
    private final Money dailySalary;
    private final String description;
    private final int requiredDays, requiredReputation;
    private final long requiredNetWorth;

    CareerRank(int level, String title, Money dailySalary, String description, int days, int reputation, long netWorth) {
        this.level = level;
        this.title = title;
        this.dailySalary = dailySalary;
        this.description = description;
        this.requiredDays = days;
        this.requiredReputation = reputation;
        this.requiredNetWorth = netWorth;
    }

    public int getLevel() { return level; }
    public String getTitle() { return title; }
    public Money getDailySalary() { return dailySalary; }
    public String getDescription() { return description; }
    public int getRequiredDays() { return requiredDays; }
    public int getRequiredReputation() { return requiredReputation; }
    public long getRequiredNetWorth() { return requiredNetWorth; }

    public CareerRank getNextRank() {
        switch (this) {
            case NOVICE_TRADER: return SENIOR_ANALYST;
            case SENIOR_ANALYST: return PORTFOLIO_MANAGER;
            case PORTFOLIO_MANAGER: return CHIEF_INVESTMENT_OFFICER;
            default: return null;
        }
    }
}
