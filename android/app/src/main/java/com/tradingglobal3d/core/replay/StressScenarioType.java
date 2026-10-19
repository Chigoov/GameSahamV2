package com.tradingglobal3d.core.replay;

public enum StressScenarioType {
    MARKET_CRASH("Kejatuhan Pasar Global (-30%)"),
    FX_SHOCK("Krisis Kurs Valas / Depresiasi Rupiah"),
    INTEREST_RATE_HIKE("Kenaikan Agresif Suku Bunga Acuan"),
    ILLIQUIDITY_SHOCK("Kekeringan Likuiditas Ekstrem");

    private final String label;
    StressScenarioType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
