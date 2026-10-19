package com.tradingglobal3d.core.corporate;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entitas Aksi Korporasi Terjadwal.
 */
public final class CorporateAction {
    private final String id;
    private final String ticker;
    private final CorporateActionType type;
    private final BigDecimal valueOrRatio; // Dividen per lembar (Rp/$) atau Rasio Split (e.g. 5.0)
    private final Currency currency;
    private final LocalDate recordingDate;
    private final LocalDate executionDate;
    private boolean isExecuted;

    public CorporateAction(String id, String ticker, CorporateActionType type,
                           BigDecimal valueOrRatio, Currency currency,
                           LocalDate recordingDate, LocalDate executionDate) {
        this.id = Objects.requireNonNull(id);
        this.ticker = Objects.requireNonNull(ticker);
        this.type = Objects.requireNonNull(type);
        this.valueOrRatio = Objects.requireNonNull(valueOrRatio);
        this.currency = currency;
        this.recordingDate = Objects.requireNonNull(recordingDate);
        this.executionDate = Objects.requireNonNull(executionDate);
        this.isExecuted = false;
    }

    public String getId() { return id; }
    public String getTicker() { return ticker; }
    public CorporateActionType getType() { return type; }
    public BigDecimal getValueOrRatio() { return valueOrRatio; }
    public Currency getCurrency() { return currency; }
    public LocalDate getRecordingDate() { return recordingDate; }
    public LocalDate getExecutionDate() { return executionDate; }
    public boolean isExecuted() { return isExecuted; }

    public void markExecuted() {
        this.isExecuted = true;
    }
}
