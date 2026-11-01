package com.tradingglobal3d.core.synthesis;

import com.tradingglobal3d.core.synthesis.SynthesisMarketSimulationEngine.IntrinsicGravityModel;
import com.tradingglobal3d.core.synthesis.SynthesisMarketSimulationEngine.IntrinsicGravityModel.AnalystSignal;
import com.tradingglobal3d.core.synthesis.SynthesisMarketSimulationEngine.IntrinsicGravityModel.TakerBias;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.Assert.*;

public class IntrinsicValueGravityAndRadarTest {

    @Test
    public void testIntrinsicValueCalculationAndClamping() {
        BigDecimal currentPrice = new BigDecimal("10000.00");

        // 1. Kasus Normal: EPS 800, PER Sektor 15x -> 800 * 15 = 12.000 (di dalam rentang 0.60x..1.95x = 6.000..19.500)
        BigDecimal fair1 = IntrinsicGravityModel.calculateIntrinsicValue(new BigDecimal("800.00"), 15.0, currentPrice);
        assertEquals(new BigDecimal("12000.00"), fair1);

        // 2. Kasus Clamping Atas (Max Bound): EPS 2000, PER 15x -> 30.000 (> 19.500)
        // Harus dikunci pada 1.95x harga pasar = 19.500
        BigDecimal fairHigh = IntrinsicGravityModel.calculateIntrinsicValue(new BigDecimal("2000.00"), 15.0, currentPrice);
        assertEquals(new BigDecimal("19500.00"), fairHigh);

        // 3. Kasus Clamping Bawah (Min Bound): EPS 100, PER 15x -> 1.500 (< 6.000)
        // Harus dikunci pada 0.60x harga pasar = 6.000
        BigDecimal fairLow = IntrinsicGravityModel.calculateIntrinsicValue(new BigDecimal("100.00"), 15.0, currentPrice);
        assertEquals(new BigDecimal("6000.00"), fairLow);

        // 4. Kasus Redaman Harga Sangat Tinggi (> 300.000 -> x0.80)
        BigDecimal megaPrice = new BigDecimal("250000.00");
        BigDecimal fairMega = IntrinsicGravityModel.calculateIntrinsicValue(new BigDecimal("25000.00"), 16.0, megaPrice);
        // Raw = 400.000 (> 300.000), dikalikan 0.80 = 320.000
        assertEquals(new BigDecimal("320000.00"), fairMega);
    }

    @Test
    public void testThreeRegimesOfTakerBias() {
        BigDecimal fairValue = new BigDecimal("10000.00");

        // 1. Rezim OVERVALUED: Harga Pasar (11.000) >= Harga Wajar (10.000)
        // Probabilitas beli bot harus ditekan ke 40% (peluang turun 60%), lot 1.0x
        TakerBias overvalued = IntrinsicGravityModel.determineTakerBias(new BigDecimal("11000.00"), fairValue);
        assertEquals("OVERVALUED", overvalued.regime);
        assertEquals(0.40, overvalued.buyProbability, 0.001);
        assertEquals(1.0, overvalued.lotMultiplier, 0.001);

        // 2. Rezim MODERATELY_UNDERVALUED: Harga Pasar (8.000) < Fair (10.000), tapi 8.000 * 1.5 = 12.000 >= 10.000
        // Probabilitas beli bot naik ke 63%, lot 1.0x
        TakerBias modUndervalued = IntrinsicGravityModel.determineTakerBias(new BigDecimal("8000.00"), fairValue);
        assertEquals("MODERATELY_UNDERVALUED", modUndervalued.regime);
        assertEquals(0.63, modUndervalued.buyProbability, 0.001);
        assertEquals(1.0, modUndervalued.lotMultiplier, 0.001);

        // 3. Rezim DEEPLY_UNDERVALUED: Harga Pasar (6.000) * 1.5 = 9.000 < Fair (10.000)
        // Probabilitas beli bot melonjak ke 68%, dan ukuran lot digandakan (2.0x)!
        TakerBias deepUndervalued = IntrinsicGravityModel.determineTakerBias(new BigDecimal("6000.00"), fairValue);
        assertEquals("DEEPLY_UNDERVALUED", deepUndervalued.regime);
        assertEquals(0.68, deepUndervalued.buyProbability, 0.001);
        assertEquals(2.0, deepUndervalued.lotMultiplier, 0.001);
    }

    @Test
    public void testGapRateAndAnalystSignals() {
        BigDecimal fairValue = new BigDecimal("12000.00");

        // Diskon 50% (Harga 8.000 vs Fair 12.000 -> GapRate = +50%) -> STRONG_BUY
        double gapStrong = IntrinsicGravityModel.calculateGapRate(new BigDecimal("8000.00"), fairValue);
        assertEquals(50.0, gapStrong, 0.01);
        assertEquals(AnalystSignal.STRONG_BUY, IntrinsicGravityModel.evaluateSignal(gapStrong));

        // Diskon 9.09% (Harga 11.000 vs Fair 12.000 -> GapRate = +9.09%) -> BUY
        double gapBuy = IntrinsicGravityModel.calculateGapRate(new BigDecimal("11000.00"), fairValue);
        assertTrue(gapBuy > 5.0 && gapBuy < 15.0);
        assertEquals(AnalystSignal.BUY, IntrinsicGravityModel.evaluateSignal(gapBuy));

        // Wajar (Harga 12.200 vs Fair 12.000 -> GapRate = -1.64%) -> HOLD
        double gapHold = IntrinsicGravityModel.calculateGapRate(new BigDecimal("12200.00"), fairValue);
        assertTrue(gapHold >= -5.0 && gapHold <= 5.0);
        assertEquals(AnalystSignal.HOLD, IntrinsicGravityModel.evaluateSignal(gapHold));

        // Kemahalan (Harga 14.000 vs Fair 12.000 -> GapRate = -14.29%) -> SELL
        double gapSell = IntrinsicGravityModel.calculateGapRate(new BigDecimal("14000.00"), fairValue);
        assertTrue(gapSell < -5.0);
        assertEquals(AnalystSignal.SELL, IntrinsicGravityModel.evaluateSignal(gapSell));
    }

    @Test
    public void testOrderBookPriceConvergenceTowardsFairValue() {
        // Simulasi 200 detak: Harga awal Rp 7.500 (sangat murah dibanding nilai wajar Rp 11.500).
        // Membuktikan bahwa TakerBias secara matematis menarik harga rata-rata naik mendekati nilai wajar.
        BigDecimal fairValue = new BigDecimal("11500.00");
        BigDecimal price = new BigDecimal("7500.00");
        BigDecimal tick = new BigDecimal("25.00");
        Random rng = new Random(42);

        int buyCount = 0;
        int sellCount = 0;

        for (int i = 0; i < 200; i++) {
            TakerBias bias = IntrinsicGravityModel.determineTakerBias(price, fairValue);
            boolean isBuy = rng.nextDouble() < bias.buyProbability;
            if (isBuy) {
                buyCount++;
                price = price.add(tick.multiply(BigDecimal.valueOf(bias.lotMultiplier)));
            } else {
                sellCount++;
                price = price.subtract(tick);
            }
        }

        // Karena awal dimulainya dari harga murah (7.500), dominasi order buy (68%) harus lebih besar dari sell
        assertTrue("Order beli harus mendominasi saat saham undervalued", buyCount > sellCount);
        assertTrue("Harga akhir (" + price + ") harus naik signifikan dari harga awal (7.500)", price.compareTo(new BigDecimal("8500.00")) >= 0);

        // Uji Konvergensi Arah Kebalikan (Overvalued): Mulai dari harga kemahalan Rp 15.000 vs Fair Rp 11.500
        BigDecimal overPrice = new BigDecimal("15000.00");
        int overBuyCount = 0;
        int overSellCount = 0;
        for (int i = 0; i < 200; i++) {
            TakerBias bias = IntrinsicGravityModel.determineTakerBias(overPrice, fairValue);
            boolean isBuy = rng.nextDouble() < bias.buyProbability;
            if (isBuy) {
                overBuyCount++;
                overPrice = overPrice.add(tick.multiply(BigDecimal.valueOf(bias.lotMultiplier)));
            } else {
                overSellCount++;
                overPrice = overPrice.subtract(tick);
            }
        }
        assertTrue("Order jual harus mendominasi saat saham overvalued (peluang turun 60%)", overSellCount > overBuyCount);
        assertTrue("Harga akhir (" + overPrice + ") harus turun menuju nilai wajar (11.500)", overPrice.compareTo(new BigDecimal("15000.00")) < 0);
    }
}
