package com.tradingglobal3d.core.synthesis;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Engine Simulasi Sintesa 5 Masterpiece (Capitalism x Game Saham 2D Portrait).
 * Mengintegrasikan prinsip ekonomi biner com.SyGame.Capitalism ke dalam
 * fondasi pasar modal com.tradingglobal3d tanpa menimpa arsitektur Order Book/Matching Engine.
 */
public final class SynthesisMarketSimulationEngine {

    // =========================================================================
    // 1. MODUL GRAVITASI NILAI INTRINSIK (Intrinsic Value Gravity & Taker Bias)
    // =========================================================================
    public static final class IntrinsicGravityModel {

        public static BigDecimal calculateIntrinsicValue(BigDecimal eps, double sectorAvgPer, BigDecimal currentPrice) {
            if (eps == null || currentPrice == null) throw new IllegalArgumentException("Parameter harga/EPS tidak boleh null");
            if (sectorAvgPer <= 0) sectorAvgPer = 15.0; // Default P/E ratio 15x

            // Rumus VirCal: Raw Fair Value = EPS * P/E Ratio
            BigDecimal rawFair = eps.multiply(BigDecimal.valueOf(sectorAvgPer)).setScale(2, RoundingMode.HALF_UP);

            // Dibatasi pada koridor wajar [0.60x .. 1.95x] dari harga pasar saat ini
            BigDecimal minBound = currentPrice.multiply(new BigDecimal("0.60")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal maxBound = currentPrice.multiply(new BigDecimal("1.95")).setScale(2, RoundingMode.HALF_UP);

            BigDecimal clamped = rawFair.max(minBound).min(maxBound);

            // Redaman harga tinggi (High-Price Damper)
            if (clamped.compareTo(new BigDecimal("300000")) > 0) {
                clamped = clamped.multiply(new BigDecimal("0.80")).setScale(2, RoundingMode.HALF_UP);
            }
            if (clamped.compareTo(new BigDecimal("500000")) > 0) {
                clamped = clamped.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
            }

            return clamped.max(new BigDecimal("50.00")); // Batas bawah harga nominal
        }

        public static final class TakerBias {
            public final double buyProbability;
            public final double lotMultiplier;
            public final String regime;

            public TakerBias(double buyProbability, double lotMultiplier, String regime) {
                this.buyProbability = buyProbability;
                this.lotMultiplier = lotMultiplier;
                this.regime = regime;
            }
        }

        public static TakerBias determineTakerBias(BigDecimal currentPrice, BigDecimal intrinsicValue) {
            Objects.requireNonNull(currentPrice);
            Objects.requireNonNull(intrinsicValue);

            // 1. Overvalued (Harga Pasar >= Harga Wajar): Peluang naik dasar 40%, turun 60%
            if (currentPrice.compareTo(intrinsicValue) >= 0) {
                return new TakerBias(0.40, 1.0, "OVERVALUED");
            }

            // 2. Deeply Undervalued (Harga Pasar * 1.5 < Harga Wajar): Diskon > 33%
            // Peluang naik dasar melonjak ke 68%, dan volume beli bot digandakan (2.0x)!
            BigDecimal threshold = currentPrice.multiply(new BigDecimal("1.50"));
            if (threshold.compareTo(intrinsicValue) < 0) {
                return new TakerBias(0.68, 2.0, "DEEPLY_UNDERVALUED");
            }

            // 3. Moderately Undervalued (Harga Pasar < Harga Wajar <= 1.5x Harga Pasar)
            return new TakerBias(0.63, 1.0, "MODERATELY_UNDERVALUED");
        }

        public enum AnalystSignal {
            STRONG_BUY("Diskon Sangat Besar / Strong Buy", 1),
            BUY("Diskon Menarik / Buy", 2),
            HOLD("Harga Wajar / Hold", 3),
            SELL("Kemahalan / Sell", 4);

            private final String label;
            private final int priority;

            AnalystSignal(String label, int priority) {
                this.label = label;
                this.priority = priority;
            }

            public String getLabel() { return label; }
            public int getPriority() { return priority; }
        }

        public static double calculateGapRate(BigDecimal currentPrice, BigDecimal intrinsicValue) {
            if (currentPrice.signum() <= 0) return 0.0;
            BigDecimal diff = intrinsicValue.subtract(currentPrice);
            return diff.divide(currentPrice, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        }

        public static AnalystSignal evaluateSignal(double gapRatePercent) {
            if (gapRatePercent >= 15.0) return AnalystSignal.STRONG_BUY;
            if (gapRatePercent >= 5.0) return AnalystSignal.BUY;
            if (gapRatePercent >= -5.0) return AnalystSignal.HOLD;
            return AnalystSignal.SELL;
        }
    }

    // =========================================================================
    // 2. MODUL PEREBUTAN KUE PANGSA PASAR SEKTOR (Zero-Sum Sector Pie Model)
    // =========================================================================
    public static final class SectorPieCompetitionModel {

        public static final class Competitor {
            public final String id;
            public final String name;
            public final double capabilityScore; // (Marketing + R&D) / 2

            public Competitor(String id, String name, double capabilityScore) {
                this.id = id;
                this.name = name;
                this.capabilityScore = Math.max(1.0, capabilityScore);
            }
        }

        public static Map<String, Money> allocateSectorRevenue(
                Money totalSectorPie,
                List<Competitor> stockCompetitors,
                Competitor playerOperatingCompany) {

            Objects.requireNonNull(totalSectorPie);
            List<Competitor> all = new ArrayList<>(stockCompetitors);
            if (playerOperatingCompany != null) all.add(playerOperatingCompany);

            double sumScore = 0.0;
            for (Competitor c : all) sumScore += c.capabilityScore;

            Map<String, Money> result = new LinkedHashMap<>();
            BigDecimal totalPieAmount = totalSectorPie.getAmount();

            for (Competitor c : all) {
                double shareRatio = c.capabilityScore / sumScore;
                BigDecimal rev = totalPieAmount.multiply(BigDecimal.valueOf(shareRatio)).setScale(2, RoundingMode.HALF_UP);
                result.put(c.id, new Money(rev, totalSectorPie.getCurrency()));
            }

            return result;
        }
    }

    // =========================================================================
    // 3. MODUL EFEK KELANGKAAN SAHAM (Supply Squeeze Model)
    // =========================================================================
    public static final class SupplySqueezeModel {

        public static double calculateLockedFloatRatio(
                long totalIssuedShares,
                long treasuryShares,
                long playerShares,
                long clientAumShares,
                long mutualFundShares) {

            if (totalIssuedShares <= 0) return 0.0;
            long locked = treasuryShares + playerShares + clientAumShares + mutualFundShares;
            double ratio = (double) locked / (double) totalIssuedShares;
            return Math.min(0.95, Math.max(0.0, ratio));
        }

        public static double getSqueezePriceMultiplier(double lockedFloatRatio) {
            // Ketika lebih dari 40% saham dikunci di portofolio/reksa dana:
            // Dorongan kenaikan harga per detak menguat akibat penipisan float publik di antrean ask.
            if (lockedFloatRatio < 0.30) return 1.0;
            if (lockedFloatRatio < 0.60) return 1.35;
            if (lockedFloatRatio < 0.80) return 1.80;
            return 2.50; // Extreme Squeeze (> 80% float terkunci)
        }
    }

    // =========================================================================
    // 4. MODUL SIKLUS MAKROEKONOMI TERPADU (Macro Cycle Engine 1..9)
    // =========================================================================
    public static final class MacroCycleEngine {
        private int macroIndex; // 1 (Resesi Parah) s.d. 9 (Booming)

        public MacroCycleEngine(int initialIndex) {
            this.macroIndex = Math.max(1, Math.min(9, initialIndex));
        }

        public int getMacroIndex() { return macroIndex; }

        public void stepQuarterlyCycle(int lineScoreDelta) {
            // Evaluasi kuartalan (setiap 3 bulan):
            if (lineScoreDelta <= -2) {
                macroIndex = Math.max(1, macroIndex - 1);
            } else if (lineScoreDelta >= 2) {
                macroIndex = Math.min(9, macroIndex + 1);
            }
        }

        public double getSectorPieMultiplier() {
            // Level 1: 0.75x (Krisis/Pasar menyusut)
            // Level 5: 1.00x (Normal)
            // Level 9: 1.30x (Booming)
            if (macroIndex == 5) return 1.00;
            if (macroIndex < 5) {
                return 1.00 - (5 - macroIndex) * 0.0625; // 5 -> 1.00, 1 -> 0.75
            } else {
                return 1.00 + (macroIndex - 5) * 0.075;  // 5 -> 1.00, 9 -> 1.30
            }
        }

        public double getTimeDepositRateAnnualPercent() {
            // Saat Resesi (Level 1..3): Bank menaikkan bunga deposito (8.0% - 9.5%) untuk menyerap dana kas
            // Saat Booming (Level 7..9): Suku bunga acuan rendah (4.0% - 5.5%), uang membanjiri bursa saham
            return 9.5 - (macroIndex - 1) * (5.5 / 8.0);
        }

        public double getInstallmentLoanInterestAnnualPercent() {
            // Suku bunga pinjaman bank berbanding lurus dengan bunga deposito + spread 4.0%
            return getTimeDepositRateAnnualPercent() + 4.0;
        }

        public double getIpoListingGainMultiplier() {
            // Saat Booming (Level 8..9): Listing gain IPO meningkat +20% s.d. +35%
            // Saat Krisis (Level 1..2): Minat IPO lesu, rawan listing di bawah harga penawaran
            return 0.80 + (macroIndex - 1) * (0.60 / 8.0);
        }
    }
}
