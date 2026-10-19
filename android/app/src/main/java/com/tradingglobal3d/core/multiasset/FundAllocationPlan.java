package com.tradingglobal3d.core.multiasset;

import java.util.*;

/**
 * Rencana Alokasi Portofolio (Fund Allocation Plan).
 * Menentukan target bobot instrumen dan cadangan kas.
 * Menyimpan rencana TIDAK mengubah posisi fisik sebelum dieksekusi.
 */
public final class FundAllocationPlan {
    public enum PlanStatus {
        DRAFT("Draf"),
        SAVED("Tersimpan"),
        EXECUTING("Sedang Dieksekusi"),
        PARTIAL("Eksekusi Belum Lengkap"),
        EXECUTED("Seluruh Order Terisi");


        private final String label;
        PlanStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final String id;
    private final String accountId;
    private final String name;
    private final Map<String, Double> targetWeights; // e.g. "IDX:BBCA" -> 0.30, "BOND:FR0091" -> 0.30
    private final double targetCashWeight;           // e.g. 0.10 (10%)
    private PlanStatus status;
    private final List<String> executionRequests = new ArrayList<>();

    public List<String> getExecutionRequests() { return Collections.unmodifiableList(executionRequests); }
    public void setExecutionRequests(Collection<String> requests) {
        executionRequests.clear(); executionRequests.addAll(requests);
    }

    public FundAllocationPlan(String id, String accountId, String name,
                              Map<String, Double> targetWeights, double targetCashWeight) {
        this.id = Objects.requireNonNull(id);
        this.accountId = Objects.requireNonNull(accountId);
        this.name = Objects.requireNonNull(name);
        this.targetWeights = Collections.unmodifiableMap(new LinkedHashMap<>(targetWeights));
        this.targetCashWeight = targetCashWeight;
        this.status = PlanStatus.DRAFT;

        validateWeights();
    }

    private void validateWeights() {
        if (!Double.isFinite(targetCashWeight) || targetCashWeight < 0 || targetCashWeight > 1)
            throw new IllegalArgumentException("Bobot kas harus terbatas antara 0 dan 1");
        double sum = targetCashWeight;
        for (Map.Entry<String, Double> entry : targetWeights.entrySet()) {
            Double w = entry.getValue();
            if (entry.getKey() == null || entry.getKey().trim().isEmpty() || w == null || !Double.isFinite(w) || w < 0 || w > 1)
                throw new IllegalArgumentException("Bobot instrumen harus terbatas antara 0 dan 1");
            sum += w;
        }
        if (Math.abs(sum - 1.0) > 0.001) {
            throw new IllegalArgumentException("Total bobot instrumen dan kas harus berjumlah tepat 100% (aktual: " + (sum * 100) + "%)");
        }
    }

    public String getId() { return id; }
    public String getAccountId() { return accountId; }
    public String getName() { return name; }
    public Map<String, Double> getTargetWeights() { return targetWeights; }
    public double getTargetCashWeight() { return targetCashWeight; }
    public PlanStatus getStatus() { return status; }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }
}
