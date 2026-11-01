package com.tradingglobal3d.core.synthesis;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.synthesis.SynthesisMarketSimulationEngine.SectorPieCompetitionModel;
import com.tradingglobal3d.core.synthesis.SynthesisMarketSimulationEngine.SectorPieCompetitionModel.Competitor;
import com.tradingglobal3d.core.synthesis.SynthesisMarketSimulationEngine.SupplySqueezeModel;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;

public class SectorPieAndSupplySqueezeTest {

    @Test
    public void testSectorRevenueZeroSumAllocation() {
        Money totalSectorPie = Money.of(20_000_000_000_000L, Currency.IDR); // 20 Triliun

        // 4 Emiten bursa di sektor Manufaktur
        List<Competitor> stockCompetitors = Arrays.asList(
                new Competitor("IDX:ASII", "Astra International", 85.0),
                new Competitor("IDX:AUTO", "Astra Otoparts", 65.0),
                new Competitor("IDX:SMSM", "Selamat Sempurna", 55.0),
                new Competitor("IDX:GJTL", "Gajah Tunggal", 45.0)
        );

        // Perusahaan Operasional milik pemain di tab Bisnis
        Competitor playerCompany = new Competitor("COMP-PLAYER-01", "Nusantara Manufaktur Perkasa", 50.0);

        Map<String, Money> allocation = SectorPieCompetitionModel.allocateSectorRevenue(
                totalSectorPie, stockCompetitors, playerCompany
        );

        assertEquals(5, allocation.size());

        // Verifikasi sifat Zero-Sum: Jumlah seluruh pendapatan yang dibagikan harus sama dengan total kue sektor (+- toleransi pembulatan pecahan)
        BigDecimal sumAllocated = BigDecimal.ZERO;
        for (Money m : allocation.values()) {
            sumAllocated = sumAllocated.add(m.getAmount());
        }

        BigDecimal diff = sumAllocated.subtract(totalSectorPie.getAmount()).abs();
        assertTrue("Total alokasi harus persis sama dengan kue sektor (zero-sum): diff=" + diff, diff.compareTo(new BigDecimal("10.00")) <= 0);

        // Perusahaan dengan skor daya saing tertinggi (ASII = 85.0) harus menerima bagian pendapatan terbesar
        assertTrue(allocation.get("IDX:ASII").compareTo(allocation.get("IDX:AUTO")) > 0);
        assertTrue(allocation.get("IDX:AUTO").compareTo(allocation.get("IDX:SMSM")) > 0);
        assertTrue(allocation.get("IDX:SMSM").compareTo(allocation.get("COMP-PLAYER-01")) > 0);
        assertTrue(allocation.get("COMP-PLAYER-01").compareTo(allocation.get("IDX:GJTL")) > 0);
    }

    @Test
    public void testPlayerResearchAndMoraleTakesMarketShareFromCompetitors() {
        Money totalSectorPie = Money.of(10_000_000_000_000L, Currency.IDR); // 10 Triliun

        List<Competitor> stockCompetitors = Arrays.asList(
                new Competitor("COMP-A", "Emiten Dominan A", 80.0),
                new Competitor("COMP-B", "Emiten B", 60.0),
                new Competitor("COMP-C", "Emiten C", 40.0)
        );

        // Keadaan 1: Perusahaan pemain masih awal (skor daya saing 20.0)
        Competitor playerEarly = new Competitor("PLAYER-CO", "Perusahaan Pemain", 20.0);
        Map<String, Money> alloc1 = SectorPieCompetitionModel.allocateSectorRevenue(totalSectorPie, stockCompetitors, playerEarly);

        Money playerRevBefore = alloc1.get("PLAYER-CO");
        Money compARevBefore = alloc1.get("COMP-A");

        // Keadaan 2: Pemain meningkatkan R&D, merekrut Headhunter, dan menaikkan Moral (skor melonjak ke 100.0)
        Competitor playerUpgraded = new Competitor("PLAYER-CO", "Perusahaan Pemain", 100.0);
        Map<String, Money> alloc2 = SectorPieCompetitionModel.allocateSectorRevenue(totalSectorPie, stockCompetitors, playerUpgraded);

        Money playerRevAfter = alloc2.get("PLAYER-CO");
        Money compARevAfter = alloc2.get("COMP-A");

        // Pendapatan pemain harus melonjak naik signifikan
        assertTrue("Pendapatan pemain harus naik setelah upgrade", playerRevAfter.compareTo(playerRevBefore) > 0);

        // Pendapatan kompetitor harus tertekan turun secara nyata karena kue sektornya zero-sum
        assertTrue("Pendapatan kompetitor A harus tersedot turun", compARevAfter.compareTo(compARevBefore) < 0);
    }

    @Test
    public void testSupplySqueezeMultiplierCurve() {
        // 1. Rasio float terkunci < 30%: normal (multiplier 1.0x)
        assertEquals(1.0, SupplySqueezeModel.getSqueezePriceMultiplier(0.15), 0.001);
        assertEquals(1.0, SupplySqueezeModel.getSqueezePriceMultiplier(0.29), 0.001);

        // 2. Rasio float terkunci 30%..60%: mulai langka (multiplier 1.35x)
        assertEquals(1.35, SupplySqueezeModel.getSqueezePriceMultiplier(0.45), 0.001);

        // 3. Rasio float terkunci 60%..80%: sangat langka (multiplier 1.80x)
        assertEquals(1.80, SupplySqueezeModel.getSqueezePriceMultiplier(0.70), 0.001);

        // 4. Rasio float terkunci >= 80%: Extreme Supply Squeeze (multiplier 2.50x)
        assertEquals(2.50, SupplySqueezeModel.getSqueezePriceMultiplier(0.85), 0.001);
    }

    @Test
    public void testCumulativeFloatLockingFromPlayerAndFunds() {
        long totalIssued = 100_000_000L; // 100 juta lembar
        long treasury = 20_000_000L;    // 20 juta (20%)
        long player = 15_000_000L;      // 15 juta (15%)
        long clientAum = 10_000_000L;    // 10 juta (10%)
        long mutualFund = 25_000_000L;   // 25 juta (25%)

        // Total terkunci = 20 + 15 + 10 + 25 = 70 juta lembar (70%)
        double ratio = SupplySqueezeModel.calculateLockedFloatRatio(totalIssued, treasury, player, clientAum, mutualFund);
        assertEquals(0.70, ratio, 0.001);

        // Dengan 70% float terkunci, dorongan kenaikan harga di Order Book harus mendapat pengali squeeze 1.80x
        double mult = SupplySqueezeModel.getSqueezePriceMultiplier(ratio);
        assertEquals(1.80, mult, 0.001);
    }
}
