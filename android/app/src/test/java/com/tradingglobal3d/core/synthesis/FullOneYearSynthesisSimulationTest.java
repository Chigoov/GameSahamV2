package com.tradingglobal3d.core.synthesis;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.synthesis.SynthesisMarketSimulationEngine.*;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static org.junit.Assert.*;

public class FullOneYearSynthesisSimulationTest {

    @Test
    public void testFullOneYearSynthesisSimulationRun() {
        // Modal Awal Game Saham: Rp 50.000.000
        BigDecimal cash = new BigDecimal("50000000.00");
        long sharesBbca = 0L;
        BigDecimal priceBbca = new BigDecimal("9500.00");
        BigDecimal fairBbca = new BigDecimal("11800.00");
        BigDecimal tick = new BigDecimal("25.00");

        MacroCycleEngine macro = new MacroCycleEngine(5);
        double lockedFloatRatio = 0.25;

        boolean companyFounded = false;
        int companyLevel = 1;
        double companyRndScore = 55.0;
        BigDecimal firmAum = BigDecimal.ZERO;

        BigDecimal totalIpoProfit = BigDecimal.ZERO;
        BigDecimal totalCompanyProfit = BigDecimal.ZERO;
        BigDecimal totalMgmtFee = BigDecimal.ZERO;
        BigDecimal totalDividends = BigDecimal.ZERO;
        BigDecimal totalTradingProfit = BigDecimal.ZERO;
        BigDecimal avgBuyPrice = BigDecimal.ZERO;

        Random rng = new Random(20261031);
        Money sectorPie = Money.of(15_000_000_000_000L, Currency.IDR); // 15 Triliun

        // 365 Hari Kalender (1 Tahun Penuh)
        for (int day = 1; day <= 365; day++) {
            boolean isWeekday = (day % 7) != 6 && (day % 7) != 0;

            if (isWeekday) {
                // 19 Detak per sesi perdagangan bursa (IDX)
                for (int t = 0; t < 19; t++) {
                    IntrinsicGravityModel.TakerBias bias = IntrinsicGravityModel.determineTakerBias(priceBbca, fairBbca);
                    double squeeze = SupplySqueezeModel.getSqueezePriceMultiplier(lockedFloatRatio);

                    boolean isBuy = rng.nextDouble() < bias.buyProbability;
                    if (isBuy) {
                        BigDecimal step = tick.multiply(BigDecimal.valueOf(Math.round(bias.lotMultiplier * squeeze)));
                        priceBbca = priceBbca.add(step);
                    } else {
                        priceBbca = priceBbca.subtract(tick);
                    }
                    if (priceBbca.compareTo(new BigDecimal("5000.00")) < 0) {
                        priceBbca = new BigDecimal("5000.00");
                    }
                }

                // Radar GapRate Analis: Akumulasi saat diskon > 10%, Take Profit saat wajar/overvalued
                double gapRate = IntrinsicGravityModel.calculateGapRate(priceBbca, fairBbca);
                if (gapRate > 10.0 && cash.compareTo(priceBbca.multiply(new BigDecimal("1000"))) >= 0) {
                    BigDecimal budget = cash.multiply(new BigDecimal("0.50"));
                    long lots = budget.divide(priceBbca.multiply(new BigDecimal("100")), 0, RoundingMode.DOWN).longValue();
                    long qty = lots * 100L;
                    if (qty > 0) {
                        BigDecimal cost = priceBbca.multiply(BigDecimal.valueOf(qty)).multiply(new BigDecimal("1.0015"));
                        if (sharesBbca == 0) {
                            avgBuyPrice = priceBbca;
                        } else {
                            BigDecimal prevTotal = avgBuyPrice.multiply(BigDecimal.valueOf(sharesBbca));
                            BigDecimal newTotal = priceBbca.multiply(BigDecimal.valueOf(qty));
                            avgBuyPrice = prevTotal.add(newTotal).divide(BigDecimal.valueOf(sharesBbca + qty), 2, RoundingMode.HALF_UP);
                        }
                        cash = cash.subtract(cost);
                        sharesBbca += qty;
                        lockedFloatRatio = Math.min(0.75, lockedFloatRatio + 0.03);
                    }
                } else if (gapRate <= -2.0 && sharesBbca > 0) {
                    BigDecimal proceeds = priceBbca.multiply(BigDecimal.valueOf(sharesBbca)).multiply(new BigDecimal("0.9975"));
                    BigDecimal buyCost = avgBuyPrice.multiply(BigDecimal.valueOf(sharesBbca));
                    totalTradingProfit = totalTradingProfit.add(proceeds.subtract(buyCost));
                    cash = cash.add(proceeds);
                    sharesBbca = 0L;
                    lockedFloatRatio = Math.max(0.25, lockedFloatRatio - 0.03);
                }
            }

            // Evaluasi Bulanan (Tiap 30 Hari): Alokasi Kue Sektor Zero-Sum & Revaluasi Nilai Wajar (VirCal)
            if (day % 30 == 0) {
                double epsBase = 700.0 * (1.0 + (macro.getMacroIndex() - 5) * 0.07);
                double sectorPer = 15.0 + (macro.getMacroIndex() - 5) * 0.8;
                fairBbca = IntrinsicGravityModel.calculateIntrinsicValue(BigDecimal.valueOf(epsBase), sectorPer, priceBbca);
            }

            // Evaluasi Siklus Makro Tiap Kuartal (84 Hari)
            if (day % 84 == 0) {
                macro.stepQuarterlyCycle(1); // Pergeseran makro perlahan
            }

            // Partisipasi IPO 3x Sebulan (Tgl 8, 18, 28)
            int dayOfMonth = ((day - 1) % 30) + 1;
            if ((dayOfMonth == 8 || dayOfMonth == 18 || dayOfMonth == 28) && cash.compareTo(new BigDecimal("15000000")) > 0) {
                BigDecimal alloc = cash.multiply(new BigDecimal("0.25")).min(new BigDecimal("25000000.00"));
                double gainRate = (0.02 + rng.nextDouble() * 0.16) * macro.getIpoListingGainMultiplier();
                BigDecimal gain = alloc.multiply(BigDecimal.valueOf(gainRate));
                cash = cash.add(gain);
                totalIpoProfit = totalIpoProfit.add(gain);
            }

            // Pendirian & Operasional Perusahaan Operasional di Tab Bisnis
            BigDecimal netWorth = cash.add(priceBbca.multiply(BigDecimal.valueOf(sharesBbca)));
            if (!companyFounded && netWorth.compareTo(new BigDecimal("75000000.00")) >= 0) {
                companyFounded = true;
                cash = cash.subtract(new BigDecimal("25000000.00")); // Modal pendirian
            }

            if (companyFounded) {
                // Alokasi Kue Sektor: Perusahaan bersaing dengan emiten bursa
                double marketShareFactor = (companyRndScore / 50.0) * macro.getSectorPieMultiplier();
                double dailyNet = (500_000.0 + rng.nextDouble() * 400_000.0) * companyLevel * marketShareFactor;
                BigDecimal profit = BigDecimal.valueOf(dailyNet).setScale(2, RoundingMode.HALF_UP);
                cash = cash.add(profit);
                totalCompanyProfit = totalCompanyProfit.add(profit);

                if (day % 60 == 0 && cash.compareTo(new BigDecimal("35000000")) > 0 && companyLevel < 4) {
                    cash = cash.subtract(new BigDecimal("12000000.00")); // Upgrade R&D & level
                    companyRndScore = Math.min(100.0, companyRndScore + 7.5);
                    companyLevel++;
                }
            }

            // Mandat Klien AUM Firma di Tab Firma
            if (day == 100) {
                firmAum = new BigDecimal("200000000.00");
            } else if (day == 210) {
                firmAum = new BigDecimal("700000000.00");
            }

            if (firmAum.signum() > 0) {
                BigDecimal dailyMgmtFee = firmAum.multiply(new BigDecimal("0.02")).divide(new BigDecimal("365"), 2, RoundingMode.HALF_UP);
                cash = cash.add(dailyMgmtFee);
                totalMgmtFee = totalMgmtFee.add(dailyMgmtFee);
            }

            // Portofolio Inti Dividen (Core Dividend Holding: 5.000 lembar saham Blue Chip)
            // Dividen Tunai Kuartalan (Hari 90, 180, 270, 360)
            if (day == 90 || day == 180 || day == 270 || day == 360) {
                long eligibleShares = Math.max(5000L, sharesBbca);
                BigDecimal divPerShare = priceBbca.multiply(new BigDecimal("0.025"));
                BigDecimal divTotal = divPerShare.multiply(BigDecimal.valueOf(eligibleShares));
                cash = cash.add(divTotal);
                totalDividends = totalDividends.add(divTotal);
            }
        }

        BigDecimal finalNetWorth = cash.add(priceBbca.multiply(BigDecimal.valueOf(sharesBbca)));

        // Verifikasi Integritas Finansial:
        assertTrue("Kas tidak boleh minus", cash.signum() > 0);
        assertTrue("Total profit IPO harus positif", totalIpoProfit.signum() > 0);
        assertTrue("Total profit perusahaan operasional harus positif", totalCompanyProfit.signum() > 0);
        assertTrue("Total komisi AUM firma harus positif", totalMgmtFee.signum() > 0);
        assertTrue("Total dividen harus positif", totalDividends.signum() > 0);

        // Verifikasi Pencapaian 1 Tahun:
        // Modal awal Rp 50 Juta berhasil bertumbuh melampaui Rp 800 Juta s.d. 1 Miliar
        assertTrue("Kekayaan akhir (" + finalNetWorth + ") harus melampaui target Rp 800 Juta",
                finalNetWorth.compareTo(new BigDecimal("800000000.00")) >= 0);
    }
}
