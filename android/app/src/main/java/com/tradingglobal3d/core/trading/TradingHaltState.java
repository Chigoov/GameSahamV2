package com.tradingglobal3d.core.trading;

public enum TradingHaltState {
    NORMAL("Perdagangan Normal", true),
    SUSPENDED_CIRCUIT_BREAKER("Suspensi / ARA / ARB", false),
    PRE_OPENING_AUCTION("Lelang Pembukaan", false);

    private final String label;
    private final boolean canContinuousTrade;

    TradingHaltState(String label, boolean canContinuousTrade) {
        this.label = label;
        this.canContinuousTrade = canContinuousTrade;
    }

    public String getLabel() {
        return label;
    }

    public boolean canContinuousTrade() {
        return canContinuousTrade;
    }
}
