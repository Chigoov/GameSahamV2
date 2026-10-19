package com.tradingglobal3d.core.economy;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

import java.util.Objects;

/**
 * Catatan transaksi tunggal yang tidak dapat diubah (immutable audit entry).
 */
public final class LedgerEntry {
    private final String id;
    private final long timestamp;            // Milliseconds game time
    private final String requestId;          // Idempotency token
    private final String accountId;
    private final LedgerEntryType type;
    private final Money amount;              // Selalu positif; arah mutasi ditentukan debit/kredit
    private final boolean isCredit;          // true = masuk/tambah (+), false = keluar/kurang (-)
    private final Money balanceAfter;        // Saldo akun setelah transaksi
    private final String description;

    public LedgerEntry(String id, long timestamp, String requestId, String accountId,
                       LedgerEntryType type, Money amount, boolean isCredit,
                       Money balanceAfter, String description) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.timestamp = timestamp;
        this.requestId = Objects.requireNonNull(requestId, "Request ID cannot be null");
        this.accountId = Objects.requireNonNull(accountId, "Account ID cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        this.isCredit = isCredit;
        this.balanceAfter = Objects.requireNonNull(balanceAfter, "Balance after cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
    }

    public String getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public String getRequestId() { return requestId; }
    public String getAccountId() { return accountId; }
    public LedgerEntryType getType() { return type; }
    public Money getAmount() { return amount; }
    public boolean isCredit() { return isCredit; }
    public Money getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public Currency getCurrency() { return amount.getCurrency(); }

    @Override
    public String toString() {
        return String.format("[%s] %s %s: %s%s (Saldo: %s) - %s",
                id, accountId, type, isCredit ? "+" : "-", amount, balanceAfter, description);
    }
}
