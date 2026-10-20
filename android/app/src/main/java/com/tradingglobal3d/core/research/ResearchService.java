package com.tradingglobal3d.core.research;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.market.Instrument;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
    private long contentEpoch;

    public ResearchService() {
        resetForGame(System.currentTimeMillis());
    }

    public synchronized void resetForGame(long baseTime) {
        contentEpoch = baseTime;
        newsList.clear();
        analystReports.clear();
        calendarEvents.clear();
        currentTier = TerminalTier.BASIC;
        subscriptionExpiresAt = Long.MAX_VALUE;
        LocalDate baseDate = java.time.Instant.ofEpochMilli(baseTime).atZone(java.time.ZoneId.of("Asia/Jakarta")).toLocalDate();

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
        newsList.add(new NewsArticle("NEWS-GAME-04", "Simulasi: membaca likuiditas pasar",
                "Antrean tipis dapat menyebabkan partial fill. Periksa spread dan volume transaksi sebelum mengirim order.",
                NewsArticle.Category.MACRO, null, 0, baseTime + 2L * 86400_000L));
        newsList.add(new NewsArticle("NEWS-GAME-05", "Simulasi: menilai risiko lintas mata uang",
                "Nilai saham USD dalam portofolio IDR juga berubah bersama kurs valuasi. Pisahkan laba saham dan eksposur kurs.",
                NewsArticle.Category.MACRO, null, 0, baseTime + 3L * 86400_000L));

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
                "CAL-01", baseDate, "Rilis Laporan Keuangan BBCA Q3",
                CalendarEvent.EventCategory.EARNINGS_RELEASE, "BBCA", "Publikasi laporan keuangan kuartalan di keterbukaan informasi BEI"
        ));
        calendarEvents.add(new CalendarEvent(
                "CAL-02", baseDate.plusDays(2), "Rapat Kebijakan Moneter The Fed (FOMC)",
                CalendarEvent.EventCategory.MACRO_POLICY, null, "Pengumuman suku bunga acuan Fed Funds Rate"
        ));
    }

    public org.json.JSONObject exportState() throws org.json.JSONException {
        return new org.json.JSONObject().put("tier", currentTier.name()).put("expiresAt", subscriptionExpiresAt).put("contentEpoch", contentEpoch);
    }

    public void restoreState(org.json.JSONObject state) throws org.json.JSONException {
        if (state.has("contentEpoch")) resetForGame(state.getLong("contentEpoch"));
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

    private void requireAccess(TerminalTier tier, long asOf) {
        if (asOf < 0) throw new IllegalArgumentException("Waktu riset tidak valid");
        if (!hasAccess(tier, asOf)) throw new IllegalStateException("LOCKED: membutuhkan paket " + tier.getTitle());
    }

    public synchronized JSONObject getConsensus(Instrument instrument, long asOf) throws JSONException {
        requireAccess(TerminalTier.PRO, asOf);
        Objects.requireNonNull(instrument);
        Map<String, AnalystReport> latest = new LinkedHashMap<>();
        for (AnalystReport report : getPublishedReports(asOf)) {
            if (!report.getTicker().equals(instrument.getTicker()) || report.getTargetPrice().getCurrency() != instrument.getCurrency()) continue;
            String key = report.getAnalystName() + "|" + report.getFirmName();
            AnalystReport previous = latest.get(key);
            if (previous == null || report.getPublishedAt() > previous.getPublishedAt()
                    || (report.getPublishedAt() == previous.getPublishedAt() && report.getId().compareTo(previous.getId()) > 0)) latest.put(key, report);
        }
        JSONArray sources = new JSONArray();
        List<BigDecimal> targets = new ArrayList<>();
        int buy = 0, hold = 0, sell = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (AnalystReport report : latest.values()) {
            sources.put(reportJson(report));
            BigDecimal target = report.getTargetPrice().getAmount();
            targets.add(target); total = total.add(target);
            switch (report.getRecommendation()) { case BUY: buy++; break; case HOLD: hold++; break; case SELL: sell++; break; }
        }
        Collections.sort(targets);
        BigDecimal mean = targets.isEmpty() ? null : total.divide(BigDecimal.valueOf(targets.size()), instrument.getCurrency().getDecimalPlaces(), RoundingMode.HALF_UP);
        BigDecimal median = targets.isEmpty() ? null : targets.size() % 2 == 1 ? targets.get(targets.size() / 2)
                : targets.get(targets.size() / 2 - 1).add(targets.get(targets.size() / 2)).divide(BigDecimal.valueOf(2), instrument.getCurrency().getDecimalPlaces(), RoundingMode.HALF_UP);
        return new JSONObject().put("success", true).put("instrumentId", instrument.getId()).put("asOf", asOf)
                .put("source", "Laporan analis NPC yang sudah terbit dalam dunia game").put("sampleCount", targets.size())
                .put("status", targets.isEmpty() ? "EMPTY" : "AVAILABLE").put("currency", instrument.getCurrency().name())
                .put("buy", buy).put("hold", hold).put("sell", sell)
                .put("meanTarget", mean == null ? JSONObject.NULL : mean.toPlainString())
                .put("medianTarget", median == null ? JSONObject.NULL : median.toPlainString())
                .put("minTarget", targets.isEmpty() ? JSONObject.NULL : targets.get(0).toPlainString())
                .put("maxTarget", targets.isEmpty() ? JSONObject.NULL : targets.get(targets.size() - 1).toPlainString())
                .put("currentPrice", instrument.getCurrentPrice().getAmount().toPlainString())
                .put("reports", sources).put("uncertainty", targets.size() < 2
                        ? "Belum cukup opini independen untuk menilai sebaran konsensus; target bukan jaminan harga."
                        : "Target adalah opini NPC, dapat berbeda dan salah; bukan harga masa depan.");
    }

    private JSONObject reportJson(AnalystReport report) throws JSONException {
        return new JSONObject().put("id", report.getId()).put("analyst", report.getAnalystName()).put("firm", report.getFirmName())
                .put("ticker", report.getTicker()).put("recommendation", report.getRecommendation().getLabel())
                .put("targetPrice", report.getTargetPrice().getAmount().toPlainString()).put("currency", report.getTargetPrice().getCurrency().name())
                .put("publishedAt", report.getPublishedAt()).put("thesis", report.getThesisSummary())
                .put("accuracyHistoryPercent", report.getAccuracyHistoryPercent()).put("accuracyBasis", "NPC_PROFILE_PARAMETER_NOT_VERIFIED_TRACK_RECORD");
    }

    public synchronized JSONObject getAnalystInbox(long asOf) throws JSONException {
        requireAccess(TerminalTier.BASIC, asOf);
        List<AnalystReport> published = getPublishedReports(asOf);
        published.sort(Comparator.comparingLong(AnalystReport::getPublishedAt).reversed().thenComparing(AnalystReport::getId));
        JSONArray messages = new JSONArray();
        for (AnalystReport report : published) messages.put(reportJson(report).put("messageId", "ANALYST:" + report.getId()));
        return new JSONObject().put("success", true).put("asOf", asOf).put("messages", messages)
                .put("source", "Laporan NPC dunia game").put("status", messages.length() == 0 ? "EMPTY" : "AVAILABLE");
    }

    public synchronized JSONObject getAlternativeData(Instrument instrument, long asOf) throws JSONException {
        requireAccess(TerminalTier.ENTERPRISE, asOf);
        Objects.requireNonNull(instrument);
        JSONArray sources = new JSONArray();
        double sum = 0;
        for (NewsArticle article : getPublishedNews(asOf)) {
            if (asOf - article.getPublishedAt() > 7L * 86_400_000L
                    || (article.getRelatedTicker() != null && !article.getRelatedTicker().equals(instrument.getTicker()))) continue;
            sum += article.getSentimentImpact();
            sources.put(new JSONObject().put("id", article.getId()).put("publishedAt", article.getPublishedAt())
                    .put("headline", article.getHeadline()).put("sentiment", article.getSentimentImpact()));
        }
        return new JSONObject().put("success", true).put("instrumentId", instrument.getId()).put("asOf", asOf)
                .put("source", "Agregasi berita game terbit 7 hari terakhir dan transaksi pasar game saat ini")
                .put("observationWindowDays", 7).put("sampleCount", sources.length()).put("sources", sources)
                .put("newsSentiment", sources.length() == 0 ? JSONObject.NULL : sum / sources.length())
                .put("observedSessionVolume", instrument.getDayVolume()).put("priceChangePercent", instrument.getDailyChangePercent())
                .put("relevance", "Konteks sentimen berita dan aktivitas transaksi instrumen, bukan prediksi keuntungan")
                .put("uncertainty", "Sampel berita terbatas dan sentimen merupakan parameter dunia game; korelasi tidak membuktikan penyebab harga.");
    }

    public synchronized JSONObject compareInstruments(List<Instrument> instruments, long asOf) throws JSONException {
        requireAccess(TerminalTier.PRO, asOf);
        if (instruments == null || instruments.size() < 2 || instruments.size() > 4)
            throw new IllegalArgumentException("Pilih 2–4 instrumen untuk dibandingkan");
        Set<String> seen = new HashSet<>();
        JSONArray rows = new JSONArray();
        for (Instrument instrument : instruments) {
            if (instrument == null || !seen.add(instrument.getId())) throw new IllegalArgumentException("Instrumen tidak valid atau duplikat");
            JSONObject consensus = getConsensus(instrument, asOf);
            rows.put(new JSONObject().put("instrumentId", instrument.getId()).put("name", instrument.getName())
                    .put("sector", instrument.getSector()).put("currency", instrument.getCurrency().name())
                    .put("price", instrument.getCurrentPrice().getAmount().toPlainString()).put("dailyChangePercent", instrument.getDailyChangePercent())
                    .put("sessionVolume", instrument.getDayVolume()).put("marketCap", instrument.getMarketCap().getAmount().toPlainString())
                    .put("consensus", consensus));
        }
        return new JSONObject().put("success", true).put("asOf", asOf).put("instruments", rows)
                .put("priceBasis", "NATIVE_CURRENCY_GAME_MARKET").put("uncertainty", "Harga dan kapitalisasi lintas mata uang tidak dapat dibandingkan langsung tanpa konversi.");
    }

    /**
     * Entitlement Check: Memeriksa apakah fitur terminal tertentu diizinkan.
     */
    public synchronized boolean hasAccess(TerminalTier requiredTier, long currentTimestamp) {
        if (currentTimestamp >= subscriptionExpiresAt) {
            this.currentTier = TerminalTier.BASIC; // Otomatis turun ke Basic jika expired
        }
        if (requiredTier == TerminalTier.BASIC) return true;
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
            this.subscriptionExpiresAt = Long.MAX_VALUE;
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
