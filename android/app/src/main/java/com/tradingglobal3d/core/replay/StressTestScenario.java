package com.tradingglobal3d.core.replay;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Parameter Skenario Uji Stres Portofolio (Stress Testing Scenario - FR-17.01).
 */
public final class StressTestScenario {
    private final String id;
    private final String name;
    private final StressScenarioType type;
    private final BigDecimal assetDropMultiplier; // e.g. 0.70 untuk kejatuhan harga -30%
    private final BigDecimal fxRateMultiplier;    // e.g. 1.35 untuk lonjakan kurs USD +35%
    private final String description;

    public StressTestScenario(String id, String name, StressScenarioType type,
                              BigDecimal assetDropMultiplier, BigDecimal fxRateMultiplier,
                              String description) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.assetDropMultiplier = Objects.requireNonNull(assetDropMultiplier);
        this.fxRateMultiplier = Objects.requireNonNull(fxRateMultiplier);
        this.description = Objects.requireNonNull(description);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public StressScenarioType getType() { return type; }
    public BigDecimal getAssetDropMultiplier() { return assetDropMultiplier; }
    public BigDecimal getFxRateMultiplier() { return fxRateMultiplier; }
    public String getDescription() { return description; }

    public static StressTestScenario defaultMarketCrash() {
        return new StressTestScenario(
                "STRESS-CRASH-30", "Krisis Finansial 2008 / Penurunan IHSG -30%",
                StressScenarioType.MARKET_CRASH,
                new BigDecimal("0.70"), BigDecimal.ONE,
                "Simulasi kejatuhan pasar ekuitas serentak sebesar 30% dengan likuiditas terbatas"
        );
    }

    public static StressTestScenario defaultFxShock() {
        return new StressTestScenario(
                "STRESS-FX-DEPRECIATION", "Lonjakan Kurs USD/IDR +35% (Depresiasi Rupiah)",
                StressScenarioType.FX_SHOCK,
                new BigDecimal("0.90"), new BigDecimal("1.35"),
                "Depresiasi nilai tukar Rupiah terhadap Dolar AS sebesar 35% disertai tekanan inflasi"
        );
    }
}
