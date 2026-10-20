package com.tradingglobal3d.core.player;

import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Layanan Klasifikasi Gaya Investor Berdasarkan Perilaku Trading Riil (D15).
 * - Menilai eksposur portofolio, frekuensi transaksi, dan penggunaan leverage.
 * - Mengharuskan minimal 5 fill dan 7 hari game sebelum memberikan label definitif.
 */
public final class InvestorArchetypeService {

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
        JSONObject res = new JSONObject();
        res.put("totalTrades", totalTradeFills);
        res.put("daysElapsed", gameDaysElapsed);

        if (totalTradeFills < 5 || gameDaysElapsed < 7) {
            res.put("archetype", Archetype.INSUFFICIENT_DATA.name());
            res.put("label", Archetype.INSUFFICIENT_DATA.getLabel());
            res.put("description", Archetype.INSUFFICIENT_DATA.getDescription());
            res.put("confidence", "Rendah (Tahap Pembelajaran)");
            return res;
        }

        BigDecimal cash = personalAccount != null ? personalAccount.getTotalCash(Currency.IDR).getAmount() : BigDecimal.ZERO;
        BigDecimal netWorth = totalNetWorth != null ? totalNetWorth.getAmount() : BigDecimal.ONE;
        if (netWorth.signum() <= 0) netWorth = BigDecimal.ONE;

        double cashRatio = cash.divide(netWorth, 4, RoundingMode.HALF_UP).doubleValue();
        boolean usesMargin = marginDebt != null && marginDebt.isPositive();

        Archetype result;
        String reasoning;

        if (usesMargin || totalTradeFills >= 20 || cashRatio < 0.15) {
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
        res.put("confidence", "Tinggi (Berdasarkan Data Transaksi Riil)");
        return res;
    }
}
