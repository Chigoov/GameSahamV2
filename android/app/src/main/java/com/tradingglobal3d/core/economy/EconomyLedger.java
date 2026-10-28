package com.tradingglobal3d.core.economy;

import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

import java.math.BigDecimal;
import java.util.*;

/**
 * Buku besar ekonomi game (Single Source of Truth untuk riwayat dan rekonsiliasi).
 * Melindungi idempotency mutasi keuangan dan menyediakan verifikasi integritas saldo.
 */
public final class EconomyLedger {
    private final List<LedgerEntry> entries = new ArrayList<>();
    private final Map<String, List<LedgerEntry>> entriesByAccount = new HashMap<>();
    private final Map<String, LedgerEntry> processedRequests = new HashMap<>();

    public synchronized boolean isRequestProcessed(String requestId) {
        if (requestId == null || requestId.isEmpty()) return false;
        return processedRequests.containsKey(requestId);
    }

    public synchronized LedgerEntry getProcessedEntry(String requestId) {
        return processedRequests.get(requestId);
    }

    /**
     * Mencatat mutasi kas secara idempoten.
     * Mengembalikan entry baru atau entry yang sudah pernah tercatat jika requestId duplikat.
     */
    public synchronized LedgerEntry recordMutation(
            String requestId,
            Account account,
            LedgerEntryType type,
            Money amount,
            boolean isCredit,
            long gameTimestamp,
            String description) {

        Objects.requireNonNull(account, "Account cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");

        if (requestId != null && !requestId.isEmpty() && processedRequests.containsKey(requestId)) {
            // Idempotency hit: return existing record without duplicate mutation
            return processedRequests.get(requestId);
        }

        // Jalankan mutasi fisik pada akun
        if (isCredit) {
            account.creditCash(amount);
        } else {
            account.debitCash(amount);
        }

        Money balanceAfter = account.getTotalCash(amount.getCurrency());
        String entryId = "LEDGER-" + (entries.size() + 1) + "-" + System.nanoTime();

        LedgerEntry entry = new LedgerEntry(
                entryId,
                gameTimestamp,
                requestId != null ? requestId : entryId,
                account.getId(),
                type,
                amount,
                isCredit,
                balanceAfter,
                description
        );

        entries.add(entry);
        entriesByAccount.computeIfAbsent(entry.getAccountId(), key -> new ArrayList<>()).add(entry);
        if (requestId != null && !requestId.isEmpty()) {
            processedRequests.put(requestId, entry);
        }

        return entry;
    }

    public synchronized List<LedgerEntry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public synchronized List<LedgerEntry> getEntriesForAccount(String accountId) {
        return new ArrayList<>(entriesByAccount.getOrDefault(accountId, Collections.emptyList()));
    }

    public synchronized void clear() {
        this.entries.clear();
        this.entriesByAccount.clear();
        this.processedRequests.clear();
    }

    public synchronized void restoreEntries(List<LedgerEntry> savedEntries) {
        this.entries.clear();
        this.entriesByAccount.clear();
        this.processedRequests.clear();
        if (savedEntries != null) {
            for (LedgerEntry e : savedEntries) {
                this.entries.add(e);
                this.entriesByAccount.computeIfAbsent(e.getAccountId(), key -> new ArrayList<>()).add(e);
                if (e.getRequestId() != null && !e.getRequestId().isEmpty()) {
                    this.processedRequests.put(e.getRequestId(), e);
                }
            }
        }
    }

    /**
     * Memverifikasi konsistensi saldo akun terhadap buku besar.
     * Menghitung ulang seluruh kredit dan debit dari awal mula.
     *
     * @return true jika saldo akun sama persis dengan perhitungan mutasi ledger.
     */
    public synchronized boolean reconcile(Account account, Currency currency) {
        BigDecimal calculated = BigDecimal.ZERO;
        for (LedgerEntry e : entries) {
            if (e.getAccountId().equals(account.getId()) && e.getCurrency() == currency) {
                if (e.isCredit()) {
                    calculated = calculated.add(e.getAmount().getAmount());
                } else {
                    calculated = calculated.subtract(e.getAmount().getAmount());
                }
            }
        }
        Money actualBalance = account.getTotalCash(currency);
        return actualBalance.getAmount().compareTo(calculated) == 0;
    }
}
