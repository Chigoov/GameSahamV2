package com.tradingglobal3d.core.career;

import com.tradingglobal3d.core.model.Money;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Laporan Evaluasi Harian Sesi Perdagangan (End-of-Day Report).
 */
public final class DailySessionReport {
    private final LocalDate date;
    private final Money startingNetWorth;
    private final Money endingNetWorth;
    private final Money dailyPnL;
    private final Money feesPaid;
    private final Money salaryReceived;
    private final int totalFillsCount;
    private final String summaryNote;

    public DailySessionReport(LocalDate date, Money startingNetWorth, Money endingNetWorth,
                              Money dailyPnL, Money feesPaid, Money salaryReceived,
                              int totalFillsCount, String summaryNote) {
        this.date = Objects.requireNonNull(date);
        this.startingNetWorth = Objects.requireNonNull(startingNetWorth);
        this.endingNetWorth = Objects.requireNonNull(endingNetWorth);
        this.dailyPnL = Objects.requireNonNull(dailyPnL);
        this.feesPaid = Objects.requireNonNull(feesPaid);
        this.salaryReceived = Objects.requireNonNull(salaryReceived);
        this.totalFillsCount = totalFillsCount;
        this.summaryNote = Objects.requireNonNull(summaryNote);
    }

    public LocalDate getDate() { return date; }
    public Money getStartingNetWorth() { return startingNetWorth; }
    public Money getEndingNetWorth() { return endingNetWorth; }
    public Money getDailyPnL() { return dailyPnL; }
    public Money getNonInvestmentFlows() { return endingNetWorth.subtract(startingNetWorth).subtract(dailyPnL); }
    public Money getFeesPaid() { return feesPaid; }
    public Money getSalaryReceived() { return salaryReceived; }
    public int getTotalFillsCount() { return totalFillsCount; }
    public String getSummaryNote() { return summaryNote; }
}
