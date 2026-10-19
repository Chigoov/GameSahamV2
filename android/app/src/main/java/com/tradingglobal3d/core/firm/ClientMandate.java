package com.tradingglobal3d.core.firm;

import com.tradingglobal3d.core.model.Money;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Kontrak Mandat Pengelolaan Dana Klien.
 * Menyimpan batas konsentrasi, cadangan kas minimum, High-Water Mark (HWM),
 * tarif fee, dan skor kepercayaan (Trust Score).
 */
public final class ClientMandate {
    public enum MandateStatus {
        OFFERED("Tawaran Tersedia"),
        ACTIVE("Mandat Aktif"),
        TERMINATED("Mandat Dihentikan");

        private final String label;
        MandateStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final String id;
    private final String clientName;
    private final ClientRiskProfile riskProfile;
    private final String benchmarkIndexId;
    private final Money initialDeposit;
    private final BigDecimal managementFeeAnnualRate; // e.g. 0.015 (1.5% p.a.)
    private final BigDecimal performanceFeeRate;       // e.g. 0.20 (20%)

    private Money highWaterMark;
    private int trustScore; // 0 - 100
    private MandateStatus status;
    private String clientAccountId;

    public ClientMandate(String id, String clientName, ClientRiskProfile riskProfile,
                         String benchmarkIndexId, Money initialDeposit,
                         BigDecimal managementFeeAnnualRate, BigDecimal performanceFeeRate) {
        this.id = Objects.requireNonNull(id);
        this.clientName = Objects.requireNonNull(clientName);
        this.riskProfile = Objects.requireNonNull(riskProfile);
        this.benchmarkIndexId = Objects.requireNonNull(benchmarkIndexId);
        this.initialDeposit = Objects.requireNonNull(initialDeposit);
        this.managementFeeAnnualRate = Objects.requireNonNull(managementFeeAnnualRate);
        this.performanceFeeRate = Objects.requireNonNull(performanceFeeRate);

        this.highWaterMark = initialDeposit;
        this.trustScore = 70; // Trust awal
        this.status = MandateStatus.OFFERED;
    }

    public String getId() { return id; }
    public String getClientName() { return clientName; }
    public ClientRiskProfile getRiskProfile() { return riskProfile; }
    public String getBenchmarkIndexId() { return benchmarkIndexId; }
    public Money getInitialDeposit() { return initialDeposit; }
    public BigDecimal getManagementFeeAnnualRate() { return managementFeeAnnualRate; }
    public BigDecimal getPerformanceFeeRate() { return performanceFeeRate; }
    public Money getHighWaterMark() { return highWaterMark; }
    public int getTrustScore() { return trustScore; }
    public MandateStatus getStatus() { return status; }
    public String getClientAccountId() { return clientAccountId; }

    public void setClientAccountId(String accountId) {
        this.clientAccountId = accountId;
        this.status = MandateStatus.ACTIVE;
    }

    public void setStatus(MandateStatus status) {
        this.status = status;
    }

    public void increaseHwmForDeposit(Money deposit) {
        this.highWaterMark = this.highWaterMark.add(deposit);
    }

    public void decreaseHwmForWithdrawal(Money withdrawal) {
        this.highWaterMark = this.highWaterMark.subtract(withdrawal);
    }

    public void updateHwmAfterPerformanceFee(Money newPeakNetWorth) {
        this.highWaterMark = newPeakNetWorth;
    }

    public void adjustTrust(int delta) {
        this.trustScore = Math.max(0, Math.min(100, this.trustScore + delta));
        if (this.trustScore < 15) {
            this.status = MandateStatus.TERMINATED; // Klien mencabut mandat
        }
    }
}
