package com.tradingglobal3d.core.clock;

public enum MarketSession {
    CLOSED("Pasar Tutup", false),
    PRE_OPENING("Pra-Pembukaan", false),
    SESSION_1("Sesi I (Pagi)", true),
    BREAK("Istirahat Siang", false),
    SESSION_2("Sesi II (Siang)", true),
    POST_CLOSING("Pra-Penutupan", false);

    private final String label;
    private final boolean isOpenForTrading;

    MarketSession(String label, boolean isOpenForTrading) {
        this.label = label;
        this.isOpenForTrading = isOpenForTrading;
    }

    public String getLabel() {
        return label;
    }

    public boolean isOpenForTrading() {
        return isOpenForTrading;
    }
}
