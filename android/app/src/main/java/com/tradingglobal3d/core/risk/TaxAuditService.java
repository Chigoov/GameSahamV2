package com.tradingglobal3d.core.risk;

import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Layanan Audit Pajak & Kepatuhan Risiko (Fitur 2 Ekspansi).
 */
public final class TaxAuditService {

    public static final class TaxAuditReport {
        private final boolean isAuditTriggered;
        private final String complianceRating; // "A" (Patuh), "B" (Waspada), "C" (Pelanggaran Risiko)
        private final double debtToAssetRatio;
        private final double dividendTaxDiscount;
        private final Money penaltyAmount;
        private final String statusMessage;

        public TaxAuditReport(boolean isAuditTriggered, String complianceRating,
                              double debtToAssetRatio, double dividendTaxDiscount,
                              Money penaltyAmount, String statusMessage) {
            this.isAuditTriggered = isAuditTriggered;
            this.complianceRating = complianceRating;
            this.debtToAssetRatio = debtToAssetRatio;
            this.dividendTaxDiscount = dividendTaxDiscount;
            this.penaltyAmount = penaltyAmount;
            this.statusMessage = statusMessage;
        }

        public boolean isAuditTriggered() { return isAuditTriggered; }
        public String getComplianceRating() { return complianceRating; }
        public double getDebtToAssetRatio() { return debtToAssetRatio; }
        public double getDividendTaxDiscount() { return dividendTaxDiscount; }
        public Money getPenaltyAmount() { return penaltyAmount; }
        public String getStatusMessage() { return statusMessage; }

        public JSONObject toJsonObject() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("isAuditTriggered", isAuditTriggered);
            obj.put("complianceRating", complianceRating);
            obj.put("debtToAssetRatio", String.format(java.util.Locale.ROOT, "%.2f%%", debtToAssetRatio * 100));
            obj.put("dividendTaxDiscount", String.format(java.util.Locale.ROOT, "%.1f%%", dividendTaxDiscount * 100));
            obj.put("penaltyAmount", penaltyAmount != null ? penaltyAmount.format() : "Rp 0");
            obj.put("statusMessage", statusMessage);
            return obj;
        }
    }

    public TaxAuditReport evaluateCompliance(Account personal, Money totalAssets, Money totalDebt) {
        Objects.requireNonNull(personal);
        double assets = totalAssets != null ? totalAssets.getAmount().doubleValue() : 0.0;
        double debt = totalDebt != null ? totalDebt.getAmount().doubleValue() : 0.0;

        if (assets <= 0) {
            return new TaxAuditReport(false, "A", 0.0, 0.10, Money.zero(Currency.IDR),
                    "Portofolio bersih, kepatuhan pajak optimal.");
        }

        double ratio = debt / assets;
        if (ratio > 0.40) {
            // Pelanggaran rasio utang > 40%
            double excess = (ratio - 0.40) * assets;
            BigDecimal penaltyVal = BigDecimal.valueOf(excess * 0.05).setScale(0, RoundingMode.HALF_UP);
            Money penalty = new Money(penaltyVal, Currency.IDR);
            return new TaxAuditReport(true, "C", ratio, 0.0, penalty,
                    "Peringatan: Rasio utang melampaui 40%. Terkena penalti pajak & pembatasan fasilitas margin.");
        } else if (ratio > 0.20) {
            return new TaxAuditReport(false, "B", ratio, 0.05, Money.zero(Currency.IDR),
                    "Kepatuhan wajar. Rasio utang termonitor normal.");
        } else {
            return new TaxAuditReport(false, "A", ratio, 0.10, Money.zero(Currency.IDR),
                    "Kepatuhan Tinggi (Peringkat A). Berhak atas diskon pajak dividen 10%.");
        }
    }
}
