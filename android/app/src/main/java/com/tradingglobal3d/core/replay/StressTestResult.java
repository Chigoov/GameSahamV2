package com.tradingglobal3d.core.replay;

import com.tradingglobal3d.core.model.Money;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Hasil Proyeksi Uji Stres Portofolio (FR-17.02).
 */
public final class StressTestResult {
    private final String scenarioId;
    private final Money baselineValue;
    private final Money stressedValue;
    private final Money potentialLoss;
    private final BigDecimal percentageDrop;
    private final boolean wouldTriggerMarginCall;
    private final String riskAssessment;

    public StressTestResult(String scenarioId, Money baselineValue, Money stressedValue,
                            Money potentialLoss, BigDecimal percentageDrop,
                            boolean wouldTriggerMarginCall, String riskAssessment) {
        this.scenarioId = Objects.requireNonNull(scenarioId);
        this.baselineValue = Objects.requireNonNull(baselineValue);
        this.stressedValue = Objects.requireNonNull(stressedValue);
        this.potentialLoss = Objects.requireNonNull(potentialLoss);
        this.percentageDrop = Objects.requireNonNull(percentageDrop);
        this.wouldTriggerMarginCall = wouldTriggerMarginCall;
        this.riskAssessment = Objects.requireNonNull(riskAssessment);
    }

    public String getScenarioId() { return scenarioId; }
    public Money getBaselineValue() { return baselineValue; }
    public Money getStressedValue() { return stressedValue; }
    public Money getPotentialLoss() { return potentialLoss; }
    public BigDecimal getPercentageDrop() { return percentageDrop; }
    public boolean wouldTriggerMarginCall() { return wouldTriggerMarginCall; }
    public String getRiskAssessment() { return riskAssessment; }
}
