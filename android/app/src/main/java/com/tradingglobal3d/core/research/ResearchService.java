package com.tradingglobal3d.core.research;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

import java.time.LocalDate;
import java.util.*;

/**
 * Layanan Riset Finansial, Berita, Kalender, dan Langganan Terminal Riset.
 * Menerapkan kontrol akses berjenjang (entitlement check) dan anti-future leak.
 */
public final class ResearchService {
    public static final long THIRTY_DAYS_MS = 30L * 24L * 60L * 60L * 1000L;

    private final List<NewsArticle> newsList = new ArrayList<>();
    private final List<AnalystReport> analystReports = new ArrayList<>();
    private final List<CalendarEvent> calendarEvents = new ArrayList<>();

    private TerminalTier currentTier = TerminalTier.BASIC;
    private long subscriptionExpiresAt = Long.MAX_VALUE; // Basic gratis selamanya

    public ResearchService() {
        initializeSampleData();
    }

    private void initializeSampleData() {
        long baseTime = System.currentTimeMillis();

        // 1. Berita yang sudah terbit
        newsList.add(new NewsArticle(
                "NEWS-01", "Bank Indonesia Tahan Suku Bunga Acuan di 6.00%",
                "Rapat Dewan Gubernur memutuskan mempertahankan suku bunga guna menjaga stabilitas nilai tukar rupiah dan inflasi.",
                NewsArticle.Category.MACRO, null, 0.40, baseTime - 3600_000L
        ));
        newsList.add(new NewsArticle(
                "NEWS-02", "Laba Bersih BBCA Tumbuh 12.8% Kuartal Ini",
                "Pertumbuhan kredit korporasi dan konsumer menopang kinerja solid bank swasta terbesar nasional.",
                NewsArticle.Category.COMPANY, "BBCA", 0.85, baseTime - 1800_000L
        ));

        // Berita masa depan (belum terbit - Anti Future Leak Test)
        newsList.add(new NewsArticle(
                "NEWS-FUTURE-01", "Bocoran Akuisisi Rahasia Sektor Tambang",
                "Rencana aksi korporasi besar yang baru akan diumumkan besok lusa.",
                NewsArticle.Category.SECTOR, "ANTM", 0.90, baseTime + 86400_000L
        ));

        // 2. Laporan Analis NPC
        analystReports.add(new AnalystReport(
                "ANA-01", "Budi Santoso, CFA", "Mandiri Sekuritas", "BBCA",
                AnalystReport.Recommendation.BUY, Money.of(11500, Currency.IDR),
                74, "Kualitas aset terjaga dengan NPL rendah dan CASA tinggi di atas 80%.",
                baseTime - 7200_000L
        ));
        analystReports.add(new AnalystReport(
                "ANA-02", "Sarah Jenkins", "Morgan Stanley", "AAPL",
                AnalystReport.Recommendation.BUY, Money.of(260.0, Currency.USD),
                68, "Siklus pembaruan perangkat AI mendorong margin layanan ekosistem Apple.",
                baseTime - 5000_000L
        ));

        // 3. Kalender Finansial
        calendarEvents.add(new CalendarEvent(
                "CAL-01", LocalDate.now(), "Rilis Laporan Keuangan BBCA Q3",
                CalendarEvent.EventCategory.EARNINGS_RELEASE, "BBCA", "Publikasi laporan keuangan kuartalan di keterbukaan informasi BEI"
        ));
        calendarEvents.add(new CalendarEvent(
                "CAL-02", LocalDate.now().plusDays(2), "Rapat Kebijakan Moneter The Fed (FOMC)",
                CalendarEvent.EventCategory.MACRO_POLICY, null, "Pengumuman suku bunga acuan Fed Funds Rate"
        ));
    }

    public org.json.JSONObject exportState() throws org.json.JSONException {
        return new org.json.JSONObject().put("tier", currentTier.name()).put("expiresAt", subscriptionExpiresAt);
    }

    public void restoreState(org.json.JSONObject state) throws org.json.JSONException {
        currentTier = TerminalTier.valueOf(state.getString("tier"));
        subscriptionExpiresAt = state.getLong("expiresAt");
    }

    public TerminalTier getCurrentTier() { return currentTier; }
    public long getSubscriptionExpiresAt() { return subscriptionExpiresAt; }

    public synchronized void addNews(NewsArticle article) {
        newsList.add(article);
    }

    public synchronized void addAnalystReport(AnalystReport report) {
        analystReports.add(report);
    }

    public synchronized void addCalendarEvent(CalendarEvent event) {
        calendarEvents.add(event);
    }

    /**
     * Mengambil berita yang SUDAH TERBIT pada waktu saat ini (Anti-Future Leak).
     */
    public synchronized List<NewsArticle> getPublishedNews(long currentTimestamp) {
        List<NewsArticle> list = new ArrayList<>();
        for (NewsArticle a : newsList) {
            if (a.isPublished(currentTimestamp)) {
                list.add(a);
            }
        }
        return list;
    }

    public synchronized List<AnalystReport> getPublishedReports(long currentTimestamp) {
        List<AnalystReport> list = new ArrayList<>();
        for (AnalystReport r : analystReports) {
            if (r.isPublished(currentTimestamp)) {
                list.add(r);
            }
        }
        return list;
    }

    public synchronized List<CalendarEvent> getCalendarEvents() {
        return Collections.unmodifiableList(calendarEvents);
    }

    /**
     * Entitlement Check: Memeriksa apakah fitur terminal tertentu diizinkan.
     */
    public synchronized boolean hasAccess(TerminalTier requiredTier, long currentTimestamp) {
        if (requiredTier == TerminalTier.BASIC) return true;
        if (currentTimestamp > subscriptionExpiresAt) {
            this.currentTier = TerminalTier.BASIC; // Otomatis turun ke Basic jika expired
            return false;
        }
        return currentTier.includes(requiredTier);
    }

    /**
     * Berlangganan paket terminal menggunakan kas USD game.
     */
    public synchronized boolean subscribeTerminal(
            Account personalAccount,
            TerminalTier targetTier,
            EconomyLedger ledger,
            long currentTimestamp) {

        if (targetTier == TerminalTier.BASIC) {
            this.currentTier = TerminalTier.BASIC;
            return true;
        }

        Money fee = targetTier.getMonthlyFeeUsd();
        Money availUsd = personalAccount.getAvailableCash(Currency.USD);
        if (availUsd.isLessThan(fee)) {
            return false; // Saldo USD tidak cukup
        }

        // Debit biaya langganan dari kas USD
        ledger.recordMutation(
                "SUB-FEE-" + targetTier.name() + "-" + currentTimestamp,
                personalAccount,
                LedgerEntryType.TRANSACTION_FEE,
                fee,
                false,
                currentTimestamp,
                "Biaya Langganan " + targetTier.getTitle() + " (30 Hari)"
        );

        this.currentTier = targetTier;
        this.subscriptionExpiresAt = currentTimestamp + THIRTY_DAYS_MS;
        return true;
    }
}
