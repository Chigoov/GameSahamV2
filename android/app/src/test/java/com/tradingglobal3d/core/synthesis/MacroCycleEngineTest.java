package com.tradingglobal3d.core.synthesis;

import com.tradingglobal3d.core.synthesis.SynthesisMarketSimulationEngine.MacroCycleEngine;
import org.junit.Test;

import static org.junit.Assert.*;

public class MacroCycleEngineTest {

    @Test
    public void testMacroIndexBoundsAndCycleAdvancement() {
        // Inisialisasi awal pada level normal (5)
        MacroCycleEngine engine = new MacroCycleEngine(5);
        assertEquals(5, engine.getMacroIndex());

        // Kuartal 1: Sentimen positif (+3) -> naik ke level 6
        engine.stepQuarterlyCycle(3);
        assertEquals(6, engine.getMacroIndex());

        // Kuartal 2: Sentimen netral (0) -> tetap level 6
        engine.stepQuarterlyCycle(0);
        assertEquals(6, engine.getMacroIndex());

        // Kuartal 3: Sentimen negatif parah (-4) -> turun ke level 5
        engine.stepQuarterlyCycle(-4);
        assertEquals(5, engine.getMacroIndex());

        // Uji batas atas (Maksimal Level 9 Booming)
        for (int i = 0; i < 10; i++) engine.stepQuarterlyCycle(5);
        assertEquals(9, engine.getMacroIndex());

        // Uji batas bawah (Minimal Level 1 Resesi Parah)
        for (int i = 0; i < 15; i++) engine.stepQuarterlyCycle(-5);
        assertEquals(1, engine.getMacroIndex());
    }

    @Test
    public void testMacroImpactOnSectorPieAndBusiness() {
        // Level 1 Resesi: Kue pasar menyusut ke 0.75x (-25%)
        MacroCycleEngine engineRecession = new MacroCycleEngine(1);
        assertEquals(0.75, engineRecession.getSectorPieMultiplier(), 0.001);

        // Level 5 Normal: Kue pasar 1.00x
        MacroCycleEngine engineNormal = new MacroCycleEngine(5);
        assertEquals(1.00, engineNormal.getSectorPieMultiplier(), 0.001);

        // Level 9 Booming: Kue pasar mengembang ke 1.30x (+30%)
        MacroCycleEngine engineBoom = new MacroCycleEngine(9);
        assertEquals(1.30, engineBoom.getSectorPieMultiplier(), 0.001);
    }

    @Test
    public void testMacroImpactOnBankDepositAndLoanRates() {
        // Level 1 Resesi: Bank menaikkan bunga deposito (9.5% p.a.) untuk menyerap dana kas menganggur
        // Suku bunga pinjaman = 9.5% + spread 4.0% = 13.5%
        MacroCycleEngine engineRecession = new MacroCycleEngine(1);
        assertEquals(9.5, engineRecession.getTimeDepositRateAnnualPercent(), 0.01);
        assertEquals(13.5, engineRecession.getInstallmentLoanInterestAnnualPercent(), 0.01);

        // Level 5 Normal: Bunga deposito 6.75% p.a.
        MacroCycleEngine engineNormal = new MacroCycleEngine(5);
        assertEquals(6.75, engineNormal.getTimeDepositRateAnnualPercent(), 0.01);
        assertEquals(10.75, engineNormal.getInstallmentLoanInterestAnnualPercent(), 0.01);

        // Level 9 Booming: Suku bunga acuan rendah (4.0% p.a.), modal bergeser ke bursa saham
        MacroCycleEngine engineBoom = new MacroCycleEngine(9);
        assertEquals(4.0, engineBoom.getTimeDepositRateAnnualPercent(), 0.01);
        assertEquals(8.0, engineBoom.getInstallmentLoanInterestAnnualPercent(), 0.01);
    }

    @Test
    public void testMacroImpactOnIpoListingGainMultiplier() {
        // Level 1: Minat IPO lesu (0.80x, potensi diskon saat listing)
        MacroCycleEngine engineRecession = new MacroCycleEngine(1);
        assertEquals(0.80, engineRecession.getIpoListingGainMultiplier(), 0.01);

        // Level 5 Normal: 1.10x
        MacroCycleEngine engineNormal = new MacroCycleEngine(5);
        assertEquals(1.10, engineNormal.getIpoListingGainMultiplier(), 0.01);

        // Level 9 Booming: Antusiasme IPO meledak (1.40x / +40% gain potensi listing)
        MacroCycleEngine engineBoom = new MacroCycleEngine(9);
        assertEquals(1.40, engineBoom.getIpoListingGainMultiplier(), 0.01);
    }
}
