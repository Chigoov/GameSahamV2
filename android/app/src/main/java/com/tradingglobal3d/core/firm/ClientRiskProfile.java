package com.tradingglobal3d.core.firm;

public enum ClientRiskProfile {
    CONSERVATIVE("Konservatif", 0.15, 0.20, 0.10), // max konsentrasi 15%, min kas 20%, max loss 10%
    MODERATE("Moderat", 0.25, 0.10, 0.15),         // max konsentrasi 25%, min kas 10%, max loss 15%
    AGGRESSIVE("Agresif", 0.40, 0.05, 0.25);       // max konsentrasi 40%, min kas 5%, max loss 25%

    private final String label;
    private final double maxConcentrationPerStock;
    private final double minCashReserve;
    private final double maxDrawdownLimit;

    ClientRiskProfile(String label, double maxConcentrationPerStock, double minCashReserve, double maxDrawdownLimit) {
        this.label = label;
        this.maxConcentrationPerStock = maxConcentrationPerStock;
        this.minCashReserve = minCashReserve;
        this.maxDrawdownLimit = maxDrawdownLimit;
    }

    public String getLabel() { return label; }
    public double getMaxConcentrationPerStock() { return maxConcentrationPerStock; }
    public double getMinCashReserve() { return minCashReserve; }
    public double getMaxDrawdownLimit() { return maxDrawdownLimit; }
}
