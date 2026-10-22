package com.tradingglobal3d.core.player;

import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.trading.TradeFill;
import java.util.*;

/**
 * Layanan Klasifikasi Gaya Investor Berdasarkan Perilaku Trading Riil (D15).
 * - Menilai eksposur portofolio, frekuensi transaksi, dan penggunaan leverage.
 * - Mengharuskan minimal 5 fill dan 7 hari game sebelum memberikan label definitif.
 */
public final class InvestorArchetypeService {

    public static JSONObject evaluateArchetype(List<TradeFill> fills, int gameDaysElapsed, Account account,
            Money netWorth, Money marginDebt, BigDecimal usdIdr, long asOf) throws JSONException {
        if (fills == null || account == null || usdIdr == null || usdIdr.signum() <= 0 || asOf < 0)
            throw new IllegalArgumentException("Riwayat dan kurs profil wajib valid");
        List<TradeFill> own = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (TradeFill fill : fills)
            if (fill.getTimestamp() <= asOf && (account.getId().equals(fill.getBuyAccountId()) || account.getId().equals(fill.getSellAccountId()))
                    && seen.add(fill.getFillId())) own.add(fill);
        own.sort(Comparator.comparingLong(TradeFill::getTimestamp).thenComparing(TradeFill::getFillId));
        JSONObject result = evaluateArchetype(own.size(), gameDaysElapsed, account, netWorth, marginDebt);
        if (result.getString("archetype").equals(Archetype.INSUFFICIENT_DATA.name())) return result;
        double equity = netWorth.getAmount().doubleValue(), exposure = 0, largest = 0;
        int holdings = 0;
        for (Position position : account.getPositions().values()) {
            double value = idr(position.getMarketValue(), usdIdr);
            if (value > 0) { exposure += value; largest = Math.max(largest, value); holdings++; }
        }
        double turnover = 0, heldQuantityDays = 0, heldQuantity = 0;
        int unmatchedSells = 0;
        Map<String, Deque<long[]>> lots = new HashMap<>();
        for (TradeFill fill : own) {
            if (fill.getQuantity() <= 0 || fill.getTimestamp() < 0) throw new IllegalArgumentException("Fill profil tidak valid");
            if (asOf - fill.getTimestamp() < 7L * 86_400_000L)
                turnover += idr(fill.getPrice(), usdIdr) * fill.getQuantity();
            Deque<long[]> instrumentLots = lots.computeIfAbsent(fill.getInstrumentId(), ignored -> new ArrayDeque<>());
            if (account.getId().equals(fill.getBuyAccountId())) instrumentLots.add(new long[]{fill.getQuantity(), fill.getTimestamp()});
            if (account.getId().equals(fill.getSellAccountId())) {
                long remaining = fill.getQuantity();
                while (remaining > 0 && !instrumentLots.isEmpty()) {
                    long[] lot = instrumentLots.peek();
                    long closed = Math.min(remaining, lot[0]);
                    heldQuantityDays += closed * ((fill.getTimestamp() - lot[1]) / 86_400_000.0);
                    heldQuantity += closed; remaining -= closed; lot[0] -= closed;
                    if (lot[0] == 0) instrumentLots.remove();
                }
                if (remaining > 0) unmatchedSells++;
            }
        }
        for (Deque<long[]> instrumentLots : lots.values()) for (long[] lot : instrumentLots) {
            heldQuantityDays += lot[0] * ((asOf - lot[1]) / 86_400_000.0); heldQuantity += lot[0];
        }
        double stockRatio = exposure / equity, concentration = exposure > 0 ? largest / exposure : 0;
        double leverage = marginDebt == null ? 0 : marginDebt.getAmount().doubleValue() / equity;
        double turnoverRatio = turnover / equity;
        double holdingDays = heldQuantity > 0 ? heldQuantityDays / heldQuantity : 0;
        // ponytail: explicit weighted rules are explainable; no ML model or second investment history.
        double score = Math.min(1, stockRatio) * .35 + Math.min(1, leverage / .5) * .25
                + concentration * .15 + (heldQuantity > 0 ? Math.max(0, 1 - holdingDays / 7) : 0) * .15
                + Math.min(1, turnoverRatio / 2) * .10;
        Archetype type = score >= .65 ? Archetype.AGGRESSIVE : score >= .35 ? Archetype.BALANCED : Archetype.CONSERVATIVE;
        result.put("archetype", type.name()).put("label", type.getLabel()).put("description", type.getDescription())
                .put("method", "WEIGHTED_EXPOSURE_LEVERAGE_DIVERSIFICATION_HOLDING_TURNOVER_V1")
                .put("riskScore", score).put("asOf", asOf).put("turnoverWindowDays", 7)
                .put("stockExposureRatio", stockRatio).put("positionCount", holdings).put("largestPositionShare", concentration)
                .put("leverageRatio", leverage).put("turnoverRatio", turnoverRatio)
                .put("averageHoldingDays", heldQuantity > 0 ? holdingDays : JSONObject.NULL)
                .put("unmatchedSellFills", unmatchedSells)
                .put("weights", new JSONObject().put("equityExposure", .35).put("leverage", .25)
                        .put("concentration", .15).put("shortHolding", .15).put("turnover", .10))
                .put("reasoning", "Berdasarkan eksposur saham, leverage, konsentrasi posisi, lama kepemilikan FIFO dan turnover 7 hari game.")
                .put("confidence", unmatchedSells > 0 ? "Terbatas: sebagian riwayat akuisisi tidak tersedia" : "Berdasarkan fill dan posisi canonical")
                .put("holdingMethod", "FIFO_QUANTITY_WEIGHTED").put("fxBasis", "AS_OF_GAME_FX");
        return result;
    }

    private static double idr(Money value, BigDecimal usdIdr) {
        return value.getAmount().multiply(value.getCurrency() == Currency.USD ? usdIdr : BigDecimal.ONE).doubleValue();
    }

    public enum Archetype {
        INSUFFICIENT_DATA("Belum Cukup Data", "Memerlukan minimal 5 transaksi pasar dan 7 hari trading untuk menganalisis profil investasi."),
        CONSERVATIVE("Konservatif (Preservasi Modal)", "Fokus pada keamanan modal dengan porsi kas dan instrumen berisiko rendah yang dominan."),
        BALANCED("Seimbang (Pertumbuhan Terukur)", "Mengombinasikan saham berfundamental solid dan diversifikasi aset terencana."),
        AGGRESSIVE("Agresif (Pencari Alpha)", "Aktif bertransaksi, memanfaatkan momentum pasar, dan terbuka terhadap instrumen berisiko tinggi.");

        private final String label;
        private final String description;

        Archetype(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() { return label; }
        public String getDescription() { return description; }
    }

    public static JSONObject evaluateArchetype(int totalTradeFills, int gameDaysElapsed, Account personalAccount, Money totalNetWorth, Money marginDebt) throws JSONException {
        if (totalTradeFills < 0 || gameDaysElapsed < 0 || (totalNetWorth != null && totalNetWorth.getCurrency() != Currency.IDR)
                || (marginDebt != null && (marginDebt.getCurrency() != Currency.IDR || marginDebt.isNegative())))
            throw new IllegalArgumentException("Data profil investasi tidak valid");
        JSONObject res = new JSONObject();
        res.put("totalTrades", totalTradeFills);
        res.put("daysElapsed", gameDaysElapsed);

        if (totalTradeFills < 5 || gameDaysElapsed < 7 || personalAccount == null || totalNetWorth == null || !totalNetWorth.isPositive()) {
            res.put("archetype", Archetype.INSUFFICIENT_DATA.name());
            res.put("label", "Gaya Investasimu Sedang Terbentuk");
            res.put("description", "Profil risiko dan gaya investasi dievaluasi secara otomatis dari perilaku transaksi pasar riil Anda tanpa kuis buatan.");
            res.put("progressText", totalTradeFills + "/5 transaksi pasar · " + gameDaysElapsed + "/7 hari trading");
            res.put("confidence", "Sedang Mengumpulkan Data (" + totalTradeFills + "/5 transaksi, " + gameDaysElapsed + "/7 hari)");
            res.put("reasoning", "Belum cukup aktivitas perdagangan untuk menyimpulkan gaya investasi secara objektif.");
            JSONArray indicators = new JSONArray();
            indicators.put("Eksposur Saham & Portofolio Riil");
            indicators.put("Penggunaan Fasilitas Leverage Margin");
            indicators.put("Tingkat Konsentrasi & Diversifikasi Aset");
            indicators.put("Durasi Rata-rata Kepemilikan Saham (FIFO)");
            indicators.put("Perputaran Portofolio 7 Hari Game (Turnover)");
            res.put("observedIndicators", indicators);
            return res;
        }

        BigDecimal cash = personalAccount != null ? personalAccount.getTotalCash(Currency.IDR).getAmount() : BigDecimal.ZERO;
        BigDecimal netWorth = totalNetWorth.getAmount();

        double cashRatio = cash.divide(netWorth, 4, RoundingMode.HALF_UP).doubleValue();
        boolean usesMargin = marginDebt != null && marginDebt.isPositive();

        Archetype result;
        String reasoning;

        double fillsPerDay = (double) totalTradeFills / gameDaysElapsed;
        if (usesMargin || fillsPerDay >= 3 || cashRatio < 0.15) {
            result = Archetype.AGGRESSIVE;
            reasoning = "Perilaku trading aktif dengan alokasi modal intensif" + (usesMargin ? " dan penggunaan fasilitas leverage margin." : ".");
        } else if (cashRatio >= 0.50) {
            result = Archetype.CONSERVATIVE;
            reasoning = "Mempertahankan likuiditas tinggi (kas >= 50% kekayaan bersih) untuk meminimalisasi risiko pasar.";
        } else {
            result = Archetype.BALANCED;
            reasoning = "Alokasi modal terdistribusi proporsional antara kas cadangan dan portofolio aset produktif.";
        }

        res.put("archetype", result.name());
        res.put("label", result.getLabel());
        res.put("description", result.getDescription());
        res.put("reasoning", reasoning);
        res.put("cashRatio", cashRatio);
        res.put("fillsPerDay", fillsPerDay);
        res.put("usesMargin", usesMargin);
        res.put("method", "CASH_LEVERAGE_FILL_FREQUENCY");
        res.put("confidence", "Terbatas (alokasi kas, leverage, dan frekuensi transaksi)");
        return res;
    }
}
