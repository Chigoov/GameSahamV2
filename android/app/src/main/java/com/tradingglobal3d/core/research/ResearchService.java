package com.tradingglobal3d.core.research;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketExchange;
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
    private final List<MarketEvent> marketEvents = new ArrayList<>();
    private final List<ResearchBooster> boosters = new ArrayList<>();
    private final List<NewsPriceTarget> newsTargets = new ArrayList<>();
    private final Map<String, JSONObject> legacyTargetEffects = new HashMap<>();
    private final Set<String> scheduledDates = new HashSet<>();

    private TerminalTier currentTier = TerminalTier.BASIC;
    private TerminalTier enrolledTier = TerminalTier.BASIC;
    private long subscriptionExpiresAt = Long.MAX_VALUE; // Basic gratis selamanya
    private long contentEpoch;
    private long observedGameAt = -1, observedRealAt = -1;
    private boolean replayClock;

    public ResearchService() {
        resetForGame(System.currentTimeMillis());
    }

    public synchronized void resetForGame(long gameTime, long realTime) {
        resetForGame(gameTime);
        observeWorldTime(gameTime, realTime, false);
    }

    public synchronized void observeWorldTime(long gameTime, long realTime, boolean replay) {
        if (gameTime < 0 || realTime < 0) throw new IllegalArgumentException("Waktu berita tidak valid");
        long now = Math.max(observedRealAt, realTime);
        for (NewsArticle article : newsList) {
            if (!article.isPublished(gameTime)) continue;
            long publishedReal = now;
            if (observedGameAt >= 0 && observedRealAt >= 0 && article.getPublishedAt() > observedGameAt
                    && gameTime > observedGameAt) {
                long gameDelta = article.getPublishedAt() - observedGameAt;
                long realDelta = now - observedRealAt;
                publishedReal = observedRealAt + new BigDecimal(java.math.BigInteger.valueOf(gameDelta)
                        .multiply(java.math.BigInteger.valueOf(realDelta))
                        .divide(java.math.BigInteger.valueOf(gameTime - observedGameAt))).longValueExact();
            }
            article.startRealEffect(publishedReal);
        }
        observedGameAt = gameTime;
        observedRealAt = now;
        replayClock = replay;
        pinActiveTargetReferences();
    }

    public synchronized void resetForGame(long baseTime) {
        contentEpoch = baseTime;
        observedGameAt = -1; observedRealAt = -1; replayClock = false;
        newsList.clear();
        analystReports.clear();
        calendarEvents.clear();
        marketEvents.clear();
        boosters.clear();
        newsTargets.clear();
        legacyTargetEffects.clear();
        scheduledDates.clear();
        currentTier = TerminalTier.BASIC;
        enrolledTier = TerminalTier.BASIC;
        subscriptionExpiresAt = Long.MAX_VALUE;
        LocalDate baseDate = java.time.Instant.ofEpochMilli(baseTime).atZone(java.time.ZoneId.of("Asia/Jakarta")).toLocalDate();

        // Kartu Booster Riset R&D Awal (Fitur 7 Ekspansi)
        boosters.add(new ResearchBooster("BOOST-01", ResearchBooster.BoosterType.NEWS_ACCURACY, "Bocoran Riset Analis", "Akurasi sinyal berita makro meningkat 30% selama 3 hari bursa.", 3, 1.30));
        boosters.add(new ResearchBooster("BOOST-02", ResearchBooster.BoosterType.FEE_DISCOUNT, "Koneksi Khusus Broker", "Bebas komisi transaksi selama 15 order berikutnya.", 15, 0.00));
        boosters.add(new ResearchBooster("BOOST-03", ResearchBooster.BoosterType.MARGIN_EXPAND, "Suntikan Likuiditas Cepat", "Plafon pinjaman bank meningkat 50% selama 1 sesi bursa.", 1, 1.50));

        // 0. Sentimen Berita Makro & Peristiwa Pasar (Fitur 2)
        marketEvents.add(new MarketEvent(
                "EVT-01", "Suku Bunga Acuan BI Dipertahankan",
                "Keputusan menahan suku bunga acuan memperkuat stabilitas margin bunga perbankan.",
                "Keuangan", MarketEvent.EventImpact.POSITIVE, baseTime, true
        ));
        marketEvents.add(new MarketEvent(
                "EVT-02", "Lonjakan Permintaan Energi Global",
                "Ekspor komoditas energi dan mineral menguat seiring permintaan industri global.",
                "Energi", MarketEvent.EventImpact.VERY_POSITIVE, baseTime, true
        ));
        marketEvents.add(new MarketEvent(
                "EVT-03", "Akselerasi Belanja Infrastruktur Digital",
                "Adopsi teknologi jaringan dan digitalisasi layanan mendorong pertumbuhan sektor telekomunikasi.",
                "Teknologi", MarketEvent.EventImpact.POSITIVE, baseTime, true
        ));

        // 1. Berita yang sudah terbit & jadwal mendatang dengan clue probabilistik (R12)
        newsList.add(new NewsArticle(
                "NEWS-01", "Bank Indonesia Tahan Suku Bunga Acuan di 6.00%",
                "Rapat Dewan Gubernur memutuskan mempertahankan suku bunga guna menjaga stabilitas nilai tukar rupiah dan inflasi.",
                NewsArticle.Category.MACRO, null, "Perbankan & Moneter", "POSITIF",
                "+1.5% s/d +3.2%", 2, "TINGGI", 0.40,
                baseTime - 7200_000L, baseTime - 3600_000L
        ));
        newsList.add(new NewsArticle(
                "NEWS-02", "Laba Bersih BBCA Tumbuh 12.8% Kuartal Ini",
                "Pertumbuhan kredit korporasi dan konsumer menopang kinerja solid bank swasta terbesar nasional.",
                NewsArticle.Category.COMPANY, "BBCA", "Keuangan", "POSITIF",
                "+2.5% s/d +5.5%", 2, "TINGGI", 0.65,
                baseTime - 3600_000L, baseTime - 1800_000L
        ));

        // Berita mendatang (Scheduled Future News dengan Clue & Estimasi Rentang - Anti-Future Leak)
        newsList.add(new NewsArticle(
                "NEWS-FUTURE-01", "Rilis Kinerja Finansial Sektor Energi & Batubara",
                "Laporan resmi laba bersih dan dividen interim emiten sektor energi.",
                NewsArticle.Category.SECTOR, "ADRO", "Energi", "POSITIF",
                "+3.0% s/d +7.0%", 2, "SEDANG", 0.50,
                baseTime - 3600_000L, baseTime + 3600_000L // Terbit 1 jam game ke depan
        ));
        newsList.add(new NewsArticle(
                "NEWS-FUTURE-02", "Publikasi Data Cadangan dan Ekspor Logam Tambang",
                "Evaluasi kuota ekspor dan biaya operasional komoditas mineral.",
                NewsArticle.Category.SECTOR, "ANTM", "Tambang & Logam", "NEGATIF",
                "-2.0% s/d -4.8%", 1, "SEDANG", -0.45,
                baseTime - 1800_000L, baseTime + 7200_000L // Terbit 2 jam game ke depan
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
        JSONArray articles = new JSONArray();
        for (NewsArticle article : newsList) articles.put(article.exportState());
        JSONArray effects = new JSONArray();
        Set<String> exportedEffects = new HashSet<>();
        for (NewsArticle article : newsList) { effects.put(article.exportEffect()); exportedEffects.add(article.getId()); }
        for (Map.Entry<String, JSONObject> entry : legacyTargetEffects.entrySet())
            if (!exportedEffects.contains(entry.getKey())) effects.put(new JSONObject(entry.getValue().toString()));

        JSONArray targetArr = new JSONArray();
        for (NewsPriceTarget t : newsTargets) targetArr.put(t.toJsonObject());

        JSONArray dateArr = new JSONArray();
        for (String d : scheduledDates) dateArr.put(d);

        return new org.json.JSONObject()
                .put("tier", currentTier.name())
                .put("enrolledTier", (enrolledTier != null ? enrolledTier : currentTier).name())
                .put("autoRenewal", isAutoRenewalEnabled())
                .put("dynamicDailyNewsEnabled", dynamicDailyNewsEnabled)
                .put("expiresAt", subscriptionExpiresAt)
                .put("contentEpoch", contentEpoch)
                .put("newsEffects", effects)
                .put("newsArticles", articles)
                .put("newsTargets", targetArr)
                .put("scheduledDates", dateArr)
                .put("observedGameAt", observedGameAt)
                .put("observedRealAt", observedRealAt);
    }

    public void restoreState(org.json.JSONObject state) throws org.json.JSONException {
        if (state.has("contentEpoch")) resetForGame(state.getLong("contentEpoch"));
        JSONArray articles = state.optJSONArray("newsArticles");
        if (articles != null) {
            List<NewsArticle> restored = new ArrayList<>();
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < articles.length(); i++) {
                NewsArticle article = NewsArticle.fromState(articles.getJSONObject(i));
                if (!ids.add(article.getId())) throw new IllegalArgumentException("ID berita save duplikat");
                restored.add(article);
            }
            newsList.clear();
            newsList.addAll(restored);
        }
        currentTier = TerminalTier.valueOf(state.getString("tier"));
        if (state.has("enrolledTier")) {
            enrolledTier = TerminalTier.valueOf(state.getString("enrolledTier"));
        } else {
            // Kompatibilitas mundur save lama: bila belum ada enrolledTier,
            // gunakan currentTier jika bukan BASIC
            enrolledTier = (currentTier != TerminalTier.BASIC) ? currentTier : TerminalTier.BASIC;
        }
        subscriptionExpiresAt = state.getLong("expiresAt");
        dynamicDailyNewsEnabled = state.optBoolean("dynamicDailyNewsEnabled", false);
        observedGameAt = state.optLong("observedGameAt", -1);
        observedRealAt = state.optLong("observedRealAt", -1);
        if (observedGameAt < -1 || observedRealAt < -1) throw new IllegalArgumentException("Anchor berita tidak valid");
        legacyTargetEffects.clear();
        JSONArray effects = state.optJSONArray("newsEffects");
        if (effects != null) {
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < effects.length(); i++) {
                JSONObject effect = effects.getJSONObject(i);
                String id = effect.getString("id");
                if (!seen.add(id)) throw new IllegalArgumentException("Deadline berita duplikat");
                boolean found = false;
                for (NewsArticle article : newsList) if (id.equals(article.getId())) { article.restoreEffect(effect); found = true; }
                if (!found) {
                    long start = effect.getLong("startsRealAt");
                    int duration = effect.getInt("durationMinutes");
                    if (start < -1 || (duration != 60 && duration != 120)) throw new IllegalArgumentException("Deadline berita lama tidak valid");
                    legacyTargetEffects.put(id, new JSONObject(effect.toString()));
                }
            }
        }
        newsTargets.clear();
        JSONArray targetArr = state.optJSONArray("newsTargets");
        if (targetArr != null) {
            for (int i = 0; i < targetArr.length(); i++) {
                NewsPriceTarget restored = NewsPriceTarget.fromJsonObject(targetArr.getJSONObject(i));
                if (restored.getStatus() == NewsPriceTarget.TargetStatus.WAITING_PREVIOUS_TARGET)
                    restored.setStatus(NewsPriceTarget.TargetStatus.ACTIVE);
                newsTargets.add(restored);
            }
        }
        scheduledDates.clear();
        JSONArray dateArr = state.optJSONArray("scheduledDates");
        if (dateArr != null) {
            for (int i = 0; i < dateArr.length(); i++) {
                scheduledDates.add(dateArr.getString(i));
            }
        }
    }

    public TerminalTier getCurrentTier() { return currentTier; }
    public TerminalTier getEnrolledTier() { return enrolledTier; }
    public long getSubscriptionExpiresAt() { return subscriptionExpiresAt; }

    /**
     * Memeriksa apakah perpanjangan otomatis paket sedang aktif (Butir 18).
     */
    public synchronized boolean isAutoRenewalEnabled() {
        return enrolledTier != null && enrolledTier != TerminalTier.BASIC;
    }

    /**
     * Menghentikan perpanjangan otomatis tanpa mencabut akses aktif berbayar (Butir 18 & 54).
     * Akses Pro/Whale tetap aktif hingga subscriptionExpiresAt; saat jatuh tempo turun ke BASIC.
     */
    public synchronized boolean cancelAutoRenewal() {
        if (enrolledTier == TerminalTier.BASIC) return false;
        this.enrolledTier = TerminalTier.BASIC;
        return true;
    }

    public synchronized void addNews(NewsArticle article) {
        newsList.add(article);
        if (observedRealAt >= 0 && article.isPublished(observedGameAt)) article.startRealEffect(observedRealAt);
    }

    private final Set<String> proceduralNewsDates = new HashSet<>();

    /**
     * Generator Berita & Katalis Otomatis Procedural.
     * Memastikan berita bursa, rumor sektor, dan performa laba emiten terus terbit
     * secara mandiri setiap 1-2 hari bursa tanpa batas waktu.
     */
    public synchronized void generateDailyProceduralNews(LocalDate date, long timestamp, InstrumentCatalog catalog, int macroIndex) {
        if (date == null) return;
        String dateKey = date.toString();
        if (proceduralNewsDates.contains(dateKey)) return;
        proceduralNewsDates.add(dateKey);

        int daySeed = Math.abs(Objects.hash(date.getYear(), date.getDayOfYear()));
        int typeMod = daySeed % 4; // 0: Emiten Laba, 1: Kebijakan Makro, 2: Sektor Industri, 3: Katalis Global

        String newsId = "NEWS-AUTO-" + date.toString() + "-" + (daySeed % 100);
        NewsArticle article = null;

        if (typeMod == 0 && catalog != null) {
            List<Instrument> universe = catalog.getActiveUniverse();
            if (!universe.isEmpty()) {
                Instrument inst = universe.get(daySeed % universe.size());
                boolean isBull = (daySeed % 2 == 0);
                double impact = isBull ? 0.35 + (daySeed % 35) / 100.0 : -0.30 - (daySeed % 30) / 100.0;
                String headline = isBull
                        ? "Laba Bersih " + inst.getTicker() + " Lampaui Ekspektasi Konsensus"
                        : "Koreksi Laba Bersih Kuartalan Tekan Saham " + inst.getTicker();
                String content = isBull
                        ? "Peningkatan volume penjualan dan efisiensi margin operasional mendongkrak optimisme investor pada " + inst.getName() + "."
                        : "Kenaikan beban pokok pendapatan serta pelemahan permintaan menekan kinerja kuartalan " + inst.getName() + ".";
                article = new NewsArticle(
                        newsId, headline, content, NewsArticle.Category.COMPANY,
                        inst.getTicker(), inst.getSector(), isBull ? "POSITIF" : "NEGATIF",
                        isBull ? "+2.5% s/d +6.0%" : "-2.0% s/d -5.0%", 2, "TINGGI",
                        impact, timestamp - 1800_000L, timestamp
                );
            }
        } else if (typeMod == 1) {
            boolean isExp = macroIndex >= 5;
            double impact = isExp ? 0.40 : -0.35;
            String headline = isExp
                    ? "Bank Sentral Menilai Likuiditas Pasar Stabil di Level " + macroIndex
                    : "Tekanan Inflasi Memicu Kebijakan Moneter Ketat (Makro Lv." + macroIndex + ")";
            String content = isExp
                    ? "Pertumbuhan kredit perbankan yang solid dan iklim investasi kondusif menopang daya beli di pasar modal."
                    : "Pelaku pasar mencermati risiko kenaikan biaya dana perbankan dan penyesuaian belanja modal korporasi.";
            article = new NewsArticle(
                    newsId, headline, content, NewsArticle.Category.MACRO,
                    null, "Perbankan & Moneter", isExp ? "POSITIF" : "NEGATIF",
                    isExp ? "+1.5% s/d +3.5%" : "-1.5% s/d -4.0%", 3, "TINGGI",
                    impact, timestamp - 1800_000L, timestamp
            );
        } else if (typeMod == 2) {
            String[] sectors = {"Energi", "Manufaktur", "Konsumen Primer", "Teknologi", "Bahan Baku"};
            String sec = sectors[daySeed % sectors.length];
            boolean isBull = (daySeed % 3 != 0);
            double impact = isBull ? 0.30 : -0.28;
            String headline = isBull
                    ? "Permintaan Produk Sektor " + sec + " Alami Peningkatan Kuartal Ini"
                    : "Tantangan Rantai Pasok Global Bayangi Sektor " + sec;
            String content = isBull
                    ? "Kenaikan harga komoditas dan pesanan baru memperkuat arus kas emiten pada sektor " + sec + "."
                    : "Kenaikan tarif logistik dan fluktuasi bahan baku membebani margin laba sektor " + sec + ".";
            article = new NewsArticle(
                    newsId, headline, content, NewsArticle.Category.SECTOR,
                    null, sec, isBull ? "POSITIF" : "NEGATIF",
                    isBull ? "+2.0% s/d +5.0%" : "-1.8% s/d -4.5%", 2, "SEDANG",
                    impact, timestamp - 1800_000L, timestamp
            );
        } else {
            boolean isBull = (daySeed % 2 == 0);
            double impact = isBull ? 0.38 : -0.32;
            String headline = isBull
                    ? "Indeks Bursa Global Wall St Menguat Ditopang Sektor Teknologi"
                    : "Bursa Global Tertekan Aksi Ambil Untung (Profit Taking) Investor";
            String content = isBull
                    ? "Laporan keuangan raksasa teknologi AS yang solid mengangkat optimisme bursa global."
                    : "Investor institusi global melakukan rotasi portofolio ke aset likuid di tengah ketidakpastian pasar.";
            article = new NewsArticle(
                    newsId, headline, content, NewsArticle.Category.MACRO,
                    null, "Global & Teknologi", isBull ? "POSITIF" : "NEGATIF",
                    isBull ? "+1.8% s/d +4.2%" : "-1.5% s/d -3.8%", 2, "SEDANG",
                    impact, timestamp - 1800_000L, timestamp
            );
        }

        if (article != null) {
            addNews(article);
        }
    }

    public synchronized void addAnalystReport(AnalystReport report) {
        analystReports.add(report);
    }

    public synchronized List<MarketEvent> getMarketEvents() {
        return new ArrayList<>(marketEvents);
    }

    public synchronized JSONArray getMarketEventsJson() throws JSONException {
        JSONArray arr = new JSONArray();
        for (MarketEvent ev : marketEvents) {
            arr.put(ev.toJsonObject());
        }
        return arr;
    }

    public synchronized List<ResearchBooster> getBoosters() {
        return new ArrayList<>(boosters);
    }

    public synchronized JSONArray getBoostersJson() throws JSONException {
        JSONArray arr = new JSONArray();
        for (ResearchBooster b : boosters) {
            arr.put(b.toJsonObject());
        }
        return arr;
    }

    public synchronized JSONObject activateBooster(String boosterId) throws JSONException {
        for (ResearchBooster b : boosters) {
            if (b.getBoosterId().equalsIgnoreCase(boosterId)) {
                b.activate();
                return b.toJsonObject();
            }
        }
        throw new IllegalArgumentException("Booster tidak ditemukan: " + boosterId);
    }

    public synchronized boolean hasActiveBooster(ResearchBooster.BoosterType type) {
        for (ResearchBooster b : boosters) {
            if (b.isActive() && b.getType() == type) return true;
        }
        return false;
    }

    public synchronized void addCalendarEvent(CalendarEvent event) {
        calendarEvents.add(event);
    }

    /**
     * Mengambil feed berita lengkap: berita terbit + jadwal rilis mendatang (R12).
     * Berita mendatang mematuhi anti-future leak (hanya clue yang ditampilkan).
     */
    public synchronized JSONArray getNewsFeed(long currentTimestamp) throws JSONException {
        return getNewsFeed(currentTimestamp, Math.max(0, observedRealAt));
    }

    public synchronized JSONArray getNewsFeed(long currentTimestamp, long realNow) throws JSONException {
        JSONArray arr = new JSONArray();
        for (NewsArticle a : newsList) {
            if (a.isAnnounced(currentTimestamp) || a.isPublished(currentTimestamp)) {
                JSONObject obj = a.toJsonObject(currentTimestamp, Math.max(realNow, observedRealAt), replayClock);
                NewsPriceTarget target = getTargetForNews(a.getId());
                if (target != null) {
                    obj.put("targetInfo", target.toJsonObject());
                }
                arr.put(obj);
            }
        }
        return arr;
    }

    /**
     * Q09: Gabungkan persentase berita yang masih aktif terhadap harga acuan yang sama:
     * +5% dan -3% menjadi target bersih +2% selama keduanya aktif (penjumlahan bertanda).
     */
    public synchronized double getActiveNewsImpact(String ticker, String sector, long currentTimestamp) {
        if (replayClock) return 0.0;
        double tickerSentimentSum = 0.0;
        int tickerNewsCount = 0;
        double sectorSentimentSum = 0.0;
        int sectorNewsCount = 0;

        for (NewsArticle a : newsList) {
            if (a.isPublished(currentTimestamp) && (observedRealAt >= 0 ? a.isRealEffectActive(observedRealAt) : a.isHorizonActive(currentTimestamp))) {
                NewsPriceTarget target = getTargetForNews(a.getId());
                if (target != null && target.getStatus() == NewsPriceTarget.TargetStatus.WAITING_PREVIOUS_TARGET) {
                    continue; // Jangan terapkan sentimen jika masih mengantre menunggu target sebelumnya (Keputusan 6)
                }
                boolean matchTicker = ticker != null && ticker.equalsIgnoreCase(a.getRelatedTicker());
                boolean matchSector = sector != null && sector.equalsIgnoreCase(a.getAffectedSector());
                if (matchTicker) {
                    tickerSentimentSum += a.getSentimentImpact();
                    tickerNewsCount++;
                } else if (matchSector) {
                    sectorSentimentSum += a.getSentimentImpact();
                    sectorNewsCount++;
                }
            }
        }
        // Berita spesifik emiten diutamakan; jika ada, jumlahkan seluruh sentimen aktif emiten
        if (tickerNewsCount > 0) {
            return Math.max(-1.0, Math.min(1.0, tickerSentimentSum));
        }
        if (sectorNewsCount > 0) {
            return Math.max(-1.0, Math.min(1.0, sectorSentimentSum));
        }
        return 0.0;
    }

    public synchronized void registerNewsTarget(NewsPriceTarget target) {
        Objects.requireNonNull(target);
        for (NewsPriceTarget existing : newsTargets) if (existing.getId().equals(target.getId())) return;
        target.setStatus(NewsPriceTarget.TargetStatus.ACTIVE);
        newsTargets.add(target);
        pinActiveTargetReferences();
    }

    private boolean targetEffectActive(NewsPriceTarget target) {
        for (NewsArticle article : newsList) if (article.getId().equals(target.getNewsId()))
            return !replayClock && article.isPublished(observedGameAt) && article.isRealEffectActive(observedRealAt);
        JSONObject legacyEffect = legacyTargetEffects.get(target.getNewsId());
        if (legacyEffect != null) {
            long start = legacyEffect.optLong("startsRealAt", -1);
            return !replayClock && start >= 0 && observedRealAt >= start
                    && observedRealAt < Math.addExact(start, legacyEffect.optLong("durationMinutes") * 60000L);
        }
        // ponytail: manually registered domain targets without an article use their explicit active status.
        return target.getStatus() == NewsPriceTarget.TargetStatus.ACTIVE;
    }

    private long targetPublicationTime(NewsPriceTarget target) {
        for (NewsArticle article : newsList) if (article.getId().equals(target.getNewsId())) return article.getPublishedAt();
        return target.getPublishedDate().atStartOfDay().toInstant(java.time.ZoneOffset.ofHours(7)).toEpochMilli();
    }

    private void pinActiveTargetReferences() {
        List<NewsPriceTarget> active = new ArrayList<>();
        for (NewsPriceTarget target : newsTargets) if (targetEffectActive(target)) active.add(target);
        active.sort(Comparator.comparingLong(this::targetPublicationTime).thenComparing(NewsPriceTarget::getId));
        Map<String, Money> references = new HashMap<>();
        Map<String, Long> origins = new HashMap<>();
        for (NewsPriceTarget target : active) {
            String ticker = target.getTicker().toUpperCase(java.util.Locale.ROOT);
            long origin = target.isReferencePinned() ? target.getReferenceOriginGameAt() : targetPublicationTime(target);
            if (!origins.containsKey(ticker) || origin < origins.get(ticker)) {
                origins.put(ticker, origin);
                references.put(ticker, target.getReferencePrice());
            }
        }
        for (NewsPriceTarget target : active) {
            String ticker = target.getTicker().toUpperCase(java.util.Locale.ROOT);
            target.pinReference(references.get(ticker), origins.get(ticker));
        }
    }

    public synchronized NewsPriceTarget getTargetForNews(String newsId) {
        if (newsId == null) return null;
        for (NewsPriceTarget t : newsTargets) {
            if (newsId.equals(t.getNewsId())) return t;
        }
        return null;
    }

    public synchronized List<NewsPriceTarget> getNewsTargets() {
        return new ArrayList<>(newsTargets);
    }

    public synchronized JSONArray getNewsTargetsJson() throws JSONException {
        JSONArray arr = new JSONArray();
        for (NewsPriceTarget t : newsTargets) {
            arr.put(t.toJsonObject());
        }
        return arr;
    }

    public synchronized boolean hasActiveNewsTarget(String ticker) {
        return getActiveNewsTarget(ticker) != null;
    }

    public synchronized NewsPriceTarget getActiveNewsTarget(String ticker) {
        if (ticker == null) return null;
        pinActiveTargetReferences();
        NewsPriceTarget first = null;
        BigDecimal total = BigDecimal.ZERO;
        for (NewsPriceTarget target : newsTargets) {
            if (targetEffectActive(target) && (ticker.equalsIgnoreCase(target.getTicker()) || ticker.endsWith(":" + target.getTicker()))) {
                if (first == null || targetPublicationTime(target) < targetPublicationTime(first)) first = target;
                total = total.add(BigDecimal.valueOf(target.getTargetPercent()));
            }
        }
        if (first == null || total.signum() == 0) return null;
        return new NewsPriceTarget("NET-" + first.getId(), first.getNewsId(), first.getTicker(), first.getTier(),
                first.getHeadline(), false, total.doubleValue(), first.getReferencePrice(), null, first.getPublishedDate());
    }

    public synchronized void evaluateNewsTargets(InstrumentCatalog catalog, LocalDate date) {
        if (catalog == null || date == null) return;
        pinActiveTargetReferences();
        Map<String, NewsPriceTarget> effectiveByTicker = new HashMap<>();
        for (NewsPriceTarget target : newsTargets)
            effectiveByTicker.computeIfAbsent(target.getTicker(), this::getActiveNewsTarget);
        for (NewsPriceTarget target : newsTargets) {
            if (target.getStatus() == NewsPriceTarget.TargetStatus.ACTIVE) {
                Instrument inst = catalog.getByExchangeAndTicker(MarketExchange.IDX, target.getTicker());
                if (inst == null) inst = catalog.getById(target.getTicker());
                if (inst == null) inst = catalog.getById("IDX:" + target.getTicker());

                if (inst != null && inst.getCurrentPrice().isPositive() && target.getReferencePrice().isPositive()) {
                    double pnlPct = (inst.getCurrentPrice().getAmount().doubleValue() - target.getReferencePrice().getAmount().doubleValue())
                            / target.getReferencePrice().getAmount().doubleValue() * 100.0;
                    target.setProgressPercent(Math.round(pnlPct * 10.0) / 10.0);

                    NewsPriceTarget effective = effectiveByTicker.get(target.getTicker());
                    int comparison = effective == null ? 0 : inst.getCurrentPrice().compareTo(effective.getTargetPrice());
                    if (effective != null && (effective.getTargetPercent() < 0 ? comparison <= 0 : comparison >= 0)) {
                        target.markAchieved(date);
                    } else if (date.isAfter(target.getDeadlineDate())) {
                        target.markExpired(date);
                    }
                }
            }
        }
    }

    private boolean dynamicDailyNewsEnabled = false;

    public synchronized void setDynamicDailyNewsEnabled(boolean enabled) {
        this.dynamicDailyNewsEnabled = enabled;
    }

    public synchronized boolean isDynamicDailyNewsEnabled() {
        return dynamicDailyNewsEnabled;
    }

    public synchronized void ensureDailyNewsSchedule(LocalDate date, InstrumentCatalog catalog, long timestamp) {
        if (!dynamicDailyNewsEnabled) return;
        if (date == null || catalog == null || scheduledDates.contains(date.toString())) return;
        scheduledDates.add(date.toString());

        List<Instrument> idxUniverse = new ArrayList<>();
        for (Instrument inst : catalog.getActiveUniverse()) {
            if (inst.getExchange() == MarketExchange.IDX && inst.isTradable() && inst.getCurrentPrice().isPositive()) {
                idxUniverse.add(inst);
            }
        }
        if (idxUniverse.isEmpty()) return;

        int dayOfYear = date.getDayOfYear();
        int year = date.getYear();

        // 1. Dua berita Pro per hari bursa (2-5% masing-masing)
        int idx1 = Math.abs((year * 365 + dayOfYear) % idxUniverse.size());
        int idx2 = (dayOfYear % 3 == 0) ? idx1 : Math.abs((year * 365 + dayOfYear + 3) % idxUniverse.size());

        Instrument inst1 = idxUniverse.get(idx1);
        Instrument inst2 = idxUniverse.get(idx2);

        double pct1 = 0.02 + (Math.abs(Math.sin(dayOfYear * 7.1)) * 0.03); // 2.0% s/d 5.0%
        double pct2 = 0.02 + (Math.abs(Math.cos(dayOfYear * 5.3)) * 0.03); // 2.0% s/d 5.0%

        pct1 = Math.round(pct1 * 1000.0) / 1000.0;
        pct2 = Math.round(pct2 * 1000.0) / 1000.0;

        String id1 = "NEWS-PRO-" + date + "-1";
        String id2 = "NEWS-PRO-" + date + "-2";

        String headline1 = (idx1 == idx2)
                ? "Laba Kuartalan " + inst1.getTicker() + " Lampaui Konsensus Analis"
                : "Konsensus Analis Proyeksikan Ekspansi Margin " + inst1.getTicker();
        String headline2 = (idx1 == idx2)
                ? "Akumulasi Berkelanjutan Institusi pada Saham " + inst2.getTicker()
                : "Peluang Pertumbuhan Arus Kas Operasional " + inst2.getTicker();

        NewsArticle newsPro1 = new NewsArticle(
                id1, headline1, "Analisis riset fundamental mendeteksi dorongan beli dengan target kenaikan minimal +" + String.format(java.util.Locale.ROOT, "%.1f", pct1 * 100.0) + "%.",
                NewsArticle.Category.COMPANY, inst1.getTicker(), pct1, timestamp
        );
        NewsArticle newsPro2 = new NewsArticle(
                id2, headline2, "Laporan riset pasar mengonfirmasi optimisme konsensus dengan target kenaikan minimal +" + String.format(java.util.Locale.ROOT, "%.1f", pct2 * 100.0) + "%.",
                NewsArticle.Category.COMPANY, inst2.getTicker(), pct2, timestamp + 1800_000L
        );

        addNews(newsPro1);
        addNews(newsPro2);

        registerNewsTarget(new NewsPriceTarget(
                "TGT-" + id1, id1, inst1.getTicker(), TerminalTier.PRO, headline1, false,
                pct1, inst1.getPreviousClosePrice(), inst1, date
        ));
        registerNewsTarget(new NewsPriceTarget(
                "TGT-" + id2, id2, inst2.getTicker(), TerminalTier.PRO, headline2, false,
                pct2, inst2.getPreviousClosePrice(), inst2, date
        ));

        // 2. Warta Eksklusif Whale (1-2 per 7 hari game, kenaikan > 20%)
        int dayIn14 = Math.floorMod(date.toEpochDay(), 14);
        if (dayIn14 == 2) {
            Instrument whaleInst = idxUniverse.get((dayOfYear + 1) % idxUniverse.size());
            double whalePct = 0.25; // +25% (> 20%)
            String whaleId = "NEWS-WHALE-" + date;
            String whaleHeadline = "Katalis Mega Taipan: Akuisisi Strategis Konglomerat pada " + whaleInst.getTicker();
            NewsArticle whaleNews = new NewsArticle(
                    whaleId, whaleHeadline, "Warta intelijen eksklusif Whale mendeteksi transaksi institusi skala raksasa dengan target kenaikan minimal +" + String.format(java.util.Locale.ROOT, "%.1f", whalePct * 100.0) + "%.",
                    NewsArticle.Category.COMPANY, whaleInst.getTicker(), whalePct, timestamp
            );
            addNews(whaleNews);
            registerNewsTarget(new NewsPriceTarget(
                    "TGT-" + whaleId, whaleId, whaleInst.getTicker(), TerminalTier.ENTERPRISE, whaleHeadline, false,
                    whalePct, whaleInst.getPreviousClosePrice(), whaleInst, date
            ));
        }

        // 3. Rumor Lanjutan Whale (minimal sekali per 14 hari game, persentase minimum 2x rumor awal)
        if (dayIn14 == 9) {
            NewsArticle originNews = null;
            for (int i = newsList.size() - 1; i >= 0; i--) {
                NewsArticle cand = newsList.get(i);
                if (cand.getId().startsWith("NEWS-WHALE-") && !cand.getId().contains("FOLLOWUP")
                        && cand.getCategory() == NewsArticle.Category.COMPANY
                        && cand.getPublishedAt() < timestamp) {
                    originNews = cand;
                    break;
                }
            }
            if (originNews != null) {
                String ticker = originNews.getRelatedTicker();
                Instrument followUpInst = null;
                for (Instrument inst : idxUniverse) {
                    if (inst.getTicker().equalsIgnoreCase(ticker)) {
                        followUpInst = inst;
                        break;
                    }
                }
                if (followUpInst != null) {
                    double originPct = originNews.getSentimentImpact();
                    double followUpPct = Math.round(originPct * 2.0 * 1000.0) / 1000.0;
                    String followUpId = "NEWS-WHALE-FOLLOWUP-" + date;
                    String followUpHeadline = "Rumor Lanjutan Taipan: Finalisasi Merger Multinasional " + followUpInst.getTicker();
                    NewsArticle followUpNews = new NewsArticle(
                            followUpId, followUpHeadline,
                            "Perkembangan lanjutan katalis Whale dari warta " + originNews.getId() + " dengan dorongan beli berlipat ganda minimal +" + String.format(java.util.Locale.ROOT, "%.1f", followUpPct * 100.0) + "%.",
                            NewsArticle.Category.COMPANY, followUpInst.getTicker(), followUpPct, timestamp
                    );
                    addNews(followUpNews);
                    registerNewsTarget(new NewsPriceTarget(
                            "TGT-" + followUpId, followUpId, followUpInst.getTicker(), TerminalTier.ENTERPRISE, followUpHeadline, true,
                            followUpPct, followUpInst.getPreviousClosePrice(), followUpInst, date
                    ));
                }
            }
        }
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
        double gapRate = calculateGapRate(instrument);
        String fundSignal = getFundamentalSignal(gapRate);
        return new JSONObject().put("success", true).put("instrumentId", instrument.getId()).put("asOf", asOf)
                .put("source", "Laporan analis NPC yang sudah terbit dalam dunia game").put("sampleCount", targets.size())
                .put("status", targets.isEmpty() ? "EMPTY" : "AVAILABLE").put("currency", instrument.getCurrency().name())
                .put("buy", buy).put("hold", hold).put("sell", sell)
                .put("meanTarget", mean == null ? JSONObject.NULL : mean.toPlainString())
                .put("medianTarget", median == null ? JSONObject.NULL : median.toPlainString())
                .put("minTarget", targets.isEmpty() ? JSONObject.NULL : targets.get(0).toPlainString())
                .put("maxTarget", targets.isEmpty() ? JSONObject.NULL : targets.get(targets.size() - 1).toPlainString())
                .put("currentPrice", instrument.getCurrentPrice().getAmount().toPlainString())
                .put("intrinsicValue", instrument.getIntrinsicValue() != null ? instrument.getIntrinsicValue().toPlainString() : instrument.getCurrentPrice().getAmount().toPlainString())
                .put("gapRatePercent", String.format(java.util.Locale.US, "%.2f", gapRate))
                .put("fundamentalSignal", fundSignal)
                .put("trailingEps", instrument.getTrailingEps().toPlainString())
                .put("sectorPer", instrument.getSectorAvgPer())
                .put("reports", sources).put("uncertainty", targets.size() < 2
                        ? "Belum cukup opini independen untuk menilai sebaran konsensus; target bukan jaminan harga."
                        : "Target adalah opini NPC, dapat berbeda dan salah; bukan harga masa depan.");
    }

    public double calculateGapRate(Instrument instrument) {
        if (instrument == null) return 0.0;
        BigDecimal fair = instrument.getIntrinsicValue();
        BigDecimal current = instrument.getCurrentPrice().getAmount();
        if (fair == null || current.signum() <= 0) return 0.0;
        return fair.subtract(current).divide(current, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
    }

    public String getFundamentalSignal(double gapRate) {
        if (gapRate >= 15.0) return "STRONG_BUY";
        if (gapRate >= 5.0)  return "BUY";
        if (gapRate >= -5.0) return "HOLD";
        return "SELL";
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
            // Menghentikan perpanjangan otomatis: enrolledTier menjadi BASIC.
            // Jika masih dalam masa aktif berbayar (currentTimestamp < subscriptionExpiresAt),
            // pertahankan akses berbayar dan subscriptionExpiresAt hingga periode berakhir (Butir 18 & 54).
            this.enrolledTier = TerminalTier.BASIC;
            if (currentTimestamp >= subscriptionExpiresAt) {
                this.currentTier = TerminalTier.BASIC;
                this.subscriptionExpiresAt = Long.MAX_VALUE;
            }
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
        this.enrolledTier = targetTier;
        this.subscriptionExpiresAt = currentTimestamp + THIRTY_DAYS_MS;
        return true;
    }

    /**
     * Perpanjangan otomatis paket Pro & Whale setelah 30 hari game jika saldo USD cukup (A05).
     * Jika saldo USD kurang, status langganan otomatis turun ke BASIC secara transparan.
     */
    public synchronized void processDailyAutoRenewal(Account personalAccount, EconomyLedger ledger, long currentTimestamp) {
        TerminalTier tierToRenew = enrolledTier;
        if (tierToRenew == null || tierToRenew == TerminalTier.BASIC) return;
        if (currentTimestamp >= subscriptionExpiresAt) {
            Money fee = tierToRenew.getMonthlyFeeUsd();
            Money availUsd = personalAccount.getAvailableCash(Currency.USD);
            if (availUsd.isGreaterThanOrEqual(fee)) {
                ledger.recordMutation(
                        "AUTO-RENEW-" + tierToRenew.name() + "-" + currentTimestamp,
                        personalAccount,
                        LedgerEntryType.TRANSACTION_FEE,
                        fee,
                        false,
                        currentTimestamp,
                        "Perpanjangan Otomatis " + tierToRenew.getTitle() + " (30 Hari Game)"
                );
                this.currentTier = tierToRenew;
                this.enrolledTier = tierToRenew;
                this.subscriptionExpiresAt = currentTimestamp + THIRTY_DAYS_MS;
            } else {
                this.currentTier = TerminalTier.BASIC;
                this.enrolledTier = TerminalTier.BASIC;
                this.subscriptionExpiresAt = Long.MAX_VALUE;
            }
        }
    }
}
