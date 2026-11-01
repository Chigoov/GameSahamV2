package com.tradingglobal3d.core.research;

import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entitas Target Kenaikan Berita Terjamin (T04, T05, Keputusan 1-6 Master Plan).
 * Merekam acuan harga penutupan sebelum berita, target minimum yang wajib tercapai,
 * perhitungan tenggat hari bursa bertahap (maks 25%/hari), dan status pencapaian.
 */
public final class NewsPriceTarget {

    public enum TargetStatus {
        ACTIVE("Aktif"),
        WAITING_PREVIOUS_TARGET("Menunggu Target Sebelumnya Selesai"),
        ACHIEVED("Target Tercapai"),
        EXPIRED_UNACHIEVED("Tenggat Berakhir");

        private final String label;
        TargetStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final String id;
    private final String newsId;
    private final String ticker;
    private final TerminalTier tier;
    private final String headline;
    private final boolean isFollowUp;
    private double targetPercent;
    private boolean referencePinned;
    private long referenceOriginGameAt = Long.MAX_VALUE;
    private Money referencePrice;
    private Money targetPrice;
    private LocalDate publishedDate;
    private int requiredDays;
    private int effectiveDeadlineDays;
    private LocalDate deadlineDate;
    private TargetStatus status;
    private double progressPercent;
    private LocalDate achievedDate;

    public NewsPriceTarget(
            String id,
            String newsId,
            String ticker,
            TerminalTier tier,
            String headline,
            boolean isFollowUp,
            double targetPercent,
            Money referencePrice,
            Instrument instrument,
            LocalDate publishedDate) {

        this.id = Objects.requireNonNull(id);
        this.newsId = Objects.requireNonNull(newsId);
        this.ticker = Objects.requireNonNull(ticker);
        this.tier = Objects.requireNonNull(tier);
        this.headline = Objects.requireNonNull(headline);
        this.isFollowUp = isFollowUp;
        this.targetPercent = targetPercent;
        this.referencePrice = Objects.requireNonNull(referencePrice);
        this.publishedDate = Objects.requireNonNull(publishedDate);
        this.status = TargetStatus.ACTIVE;
        this.progressPercent = 0.0;
        this.achievedDate = null;

        computeTargetAndDeadlines(instrument);
    }

    private void computeTargetAndDeadlines(Instrument instrument) {
        // Q10: Bulatkan searah target: naik ke atas, turun ke bawah pada fraksi harga sah
        BigDecimal multiplier = BigDecimal.ONE.add(BigDecimal.valueOf(targetPercent));
        BigDecimal rawTarget = referencePrice.getAmount().multiply(multiplier);
        if (instrument != null) {
            this.targetPrice = instrument.roundTargetPrice(rawTarget, targetPercent);
        } else {
            RoundingMode mode = targetPercent > 0 ? RoundingMode.CEILING : targetPercent < 0 ? RoundingMode.FLOOR : RoundingMode.HALF_UP;
            com.tradingglobal3d.core.market.MarketExchange exchange = referencePrice.getCurrency() == Currency.IDR
                    ? com.tradingglobal3d.core.market.MarketExchange.IDX : com.tradingglobal3d.core.market.MarketExchange.US;
            BigDecimal tick = exchange.getTickSize(rawTarget);
            this.targetPrice = new Money(rawTarget.divide(tick, 0, mode).multiply(tick), referencePrice.getCurrency());
        }

        // Perhitungan jumlah hari bursa minimum yang diperlukan (maksimal 25% per hari bursa)
        this.requiredDays = calculateRequiredDays(referencePrice.getAmount(), targetPrice.getAmount());
        // Target yang secara matematis bisa dicapai dalam 3 hari wajib tercapai paling lambat 3 hari (Butir 3)
        this.effectiveDeadlineDays = Math.max(3, requiredDays);
        this.deadlineDate = publishedDate.plusDays(effectiveDeadlineDays);
    }

    /**
     * Menghitung hari bursa minimum bertahap dengan batas harian maksimal 25%.
     */
    public static int calculateRequiredDays(BigDecimal refPrice, BigDecimal targetPrice) {
        if (refPrice == null || targetPrice == null || refPrice.signum() <= 0 || targetPrice.compareTo(refPrice) <= 0) {
            return 1;
        }
        BigDecimal current = refPrice;
        int days = 0;
        while (current.compareTo(targetPrice) < 0) {
            days++;
            current = current.multiply(new BigDecimal("1.25"));
            if (days > 30) break; // batas pengaman
        }
        return Math.max(1, days);
    }

    public void activateWithNewReference(Money newReferencePrice, LocalDate currentDate, Instrument instrument) {
        this.referencePrice = Objects.requireNonNull(newReferencePrice);
        this.publishedDate = Objects.requireNonNull(currentDate);
        this.status = TargetStatus.ACTIVE;
        computeTargetAndDeadlines(instrument);
    }

    public boolean isReferencePinned() { return referencePinned; }

    public long getReferenceOriginGameAt() { return referenceOriginGameAt; }

    public void pinReference(Money reference, long originGameAt) {
        if (referencePinned && referenceOriginGameAt <= originGameAt) return;
        referencePrice = Objects.requireNonNull(reference);
        computeTargetAndDeadlines(null);
        referencePinned = true;
        referenceOriginGameAt = originGameAt;
    }

    public void markAchieved(LocalDate date) {
        this.status = TargetStatus.ACHIEVED;
        this.achievedDate = date;
    }

    public void markExpired(LocalDate date) {
        this.status = TargetStatus.EXPIRED_UNACHIEVED;
    }

    public String getDynamicNote() {
        if (requiredDays > 3) {
            String pctStr = String.format(java.util.Locale.ROOT, "%.1f", targetPercent * 100.0);
            return "Target kenaikan minimal " + pctStr + "%. Karena batas kenaikan harian 25%, target membutuhkan sedikitnya " + requiredDays + " hari bursa.";
        }
        return null;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("newsId", newsId);
        obj.put("ticker", ticker);
        obj.put("tier", tier.name());
        obj.put("headline", headline);
        obj.put("isFollowUp", isFollowUp);
        obj.put("targetPercent", targetPercent);
        obj.put("referencePrice", referencePrice.getAmount().toPlainString());
        obj.put("referencePinned", referencePinned);
        if (referencePinned) obj.put("referenceOriginGameAt", referenceOriginGameAt);
        obj.put("currency", referencePrice.getCurrency().name());
        obj.put("targetPrice", targetPrice.getAmount().toPlainString());
        obj.put("publishedDate", publishedDate.toString());
        obj.put("requiredDays", requiredDays);
        obj.put("effectiveDeadlineDays", effectiveDeadlineDays);
        obj.put("deadlineDate", deadlineDate.toString());
        obj.put("status", status.name());
        obj.put("statusLabel", status.getLabel());
        obj.put("progressPercent", progressPercent);
        if (achievedDate != null) obj.put("achievedDate", achievedDate.toString());
        String note = getDynamicNote();
        if (note != null) obj.put("note", note);
        return obj;
    }

    public static NewsPriceTarget fromJsonObject(JSONObject obj) throws JSONException {
        String id = obj.getString("id");
        String newsId = obj.getString("newsId");
        String ticker = obj.getString("ticker");
        TerminalTier tier = TerminalTier.valueOf(obj.getString("tier"));
        String headline = obj.getString("headline");
        boolean isFollowUp = obj.optBoolean("isFollowUp", false);
        double targetPercent = obj.getDouble("targetPercent");
        Currency currency = Currency.valueOf(obj.optString("currency", "IDR"));
        Money referencePrice = new Money(new BigDecimal(obj.getString("referencePrice")), currency);
        Money targetPrice = new Money(new BigDecimal(obj.getString("targetPrice")), currency);
        LocalDate publishedDate = LocalDate.parse(obj.getString("publishedDate"));
        LocalDate deadlineDate = LocalDate.parse(obj.getString("deadlineDate"));

        NewsPriceTarget target = new NewsPriceTarget(
                id, newsId, ticker, tier, headline, isFollowUp, targetPercent, referencePrice, null, publishedDate
        );
        target.targetPrice = targetPrice;
        target.referencePinned = obj.optBoolean("referencePinned", false);
        target.referenceOriginGameAt = obj.optLong("referenceOriginGameAt", target.referencePinned
                ? publishedDate.atStartOfDay().toInstant(java.time.ZoneOffset.ofHours(7)).toEpochMilli() : Long.MAX_VALUE);
        target.requiredDays = obj.optInt("requiredDays", 1);
        target.effectiveDeadlineDays = obj.optInt("effectiveDeadlineDays", 3);
        target.deadlineDate = deadlineDate;
        target.status = TargetStatus.valueOf(obj.getString("status"));
        target.progressPercent = obj.optDouble("progressPercent", 0.0);
        if (obj.has("achievedDate") && !obj.isNull("achievedDate")) {
            target.achievedDate = LocalDate.parse(obj.getString("achievedDate"));
        }
        return target;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getNewsId() { return newsId; }
    public String getTicker() { return ticker; }
    public TerminalTier getTier() { return tier; }
    public String getHeadline() { return headline; }
    public boolean isFollowUp() { return isFollowUp; }
    public double getTargetPercent() { return targetPercent; }
    public Money getReferencePrice() { return referencePrice; }
    public Money getTargetPrice() { return targetPrice; }
    public LocalDate getPublishedDate() { return publishedDate; }
    public int getRequiredDays() { return requiredDays; }
    public int getEffectiveDeadlineDays() { return effectiveDeadlineDays; }
    public LocalDate getDeadlineDate() { return deadlineDate; }
    public TargetStatus getStatus() { return status; }
    public void setStatus(TargetStatus status) { this.status = status; }
    public double getProgressPercent() { return progressPercent; }
    public void setProgressPercent(double progressPercent) { this.progressPercent = progressPercent; }
    public LocalDate getAchievedDate() { return achievedDate; }
}
