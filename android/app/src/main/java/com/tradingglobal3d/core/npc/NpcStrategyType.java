package com.tradingglobal3d.core.npc;

public enum NpcStrategyType {
    MARKET_MAKER("Penyedia Likuiditas (Market Maker)"),
    VALUE_INVESTOR("Investor Fundamental (Value)"),
    MOMENTUM_TRADER("Trader Tren (Momentum)"),
    RETAIL_NOISE("Investor Ritel (Noise)");

    private final String label;

    NpcStrategyType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
