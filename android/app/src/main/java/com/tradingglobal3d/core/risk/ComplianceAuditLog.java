package com.tradingglobal3d.core.risk;

import java.util.Objects;

/**
 * Log Jejak Audit Kepatuhan & Risiko (Compliance Audit Trail - FR-15.10).
 */
public final class ComplianceAuditLog {
    public enum AuditCategory {
        MARGIN_LOAN("Fasilitas Margin"),
        MARGIN_CALL("Margin Call & Defisit"),
        SHORT_BORROW("Pinjaman Saham Short"),
        FORCED_LIQUIDATION("Likuidasi Paksa"),
        MANDATE_BREACH("Pelanggaran Mandat Klien"),
        HALT_ENFORCEMENT("Kepatuhan Suspensi Bursa");

        private final String label;
        AuditCategory(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final String id;
    private final long timestamp;
    private final String accountId;
    private final AuditCategory category;
    private final String description;
    private final String mitigationAction;

    public ComplianceAuditLog(String id, long timestamp, String accountId,
                              AuditCategory category, String description, String mitigationAction) {
        this.id = Objects.requireNonNull(id);
        this.timestamp = timestamp;
        this.accountId = Objects.requireNonNull(accountId);
        this.category = Objects.requireNonNull(category);
        this.description = Objects.requireNonNull(description);
        this.mitigationAction = Objects.requireNonNull(mitigationAction);
    }

    public String getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public String getAccountId() { return accountId; }
    public AuditCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public String getMitigationAction() { return mitigationAction; }
}
