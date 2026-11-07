package com.tradingglobal3d.core.economy;

import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.*;

/**
 * Buku besar ekonomi game (Single Source of Truth untuk riwayat dan rekonsiliasi).
 * Melindungi idempotency mutasi keuangan dan menyediakan verifikasi integritas saldo.
 */
public final class EconomyLedger {
    private final List<LedgerEntry> entries = new ArrayList<>();
    private final Map<String, List<LedgerEntry>> entriesByAccount = new HashMap<>();
    private long sequence = 0;
    private final Map<String, LedgerEntry> processedRequests = new HashMap<>();
    private final Map<String, EnumMap<Currency, BigDecimal>> compactedCash = new HashMap<>();
    private int compactedCount;
    private static final BigDecimal LARGE_NPC_TRADE = BigDecimal.valueOf(500_000_000);

    public static boolean isLargeNpcTrade(LedgerEntry entry) {
        return (entry.getType() == LedgerEntryType.TRADE_BUY || entry.getType() == LedgerEntryType.TRADE_SELL)
                && entry.getAmount().getAmount().compareTo(LARGE_NPC_TRADE) >= 0;
    }

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
        String entryId = "LEDGER-" + (++sequence) + "-" + System.nanoTime();

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

    public synchronized int getEntryCount() {
        return Math.addExact(entries.size(), compactedCount);
    }

    public synchronized boolean hasCompactedNpcTrades() {
        return compactedCount > 0;
    }

    public synchronized void compactNpcTrades(Set<String> npcAccounts) {
        Set<LedgerEntry> recent = new HashSet<>();
        Map<String, Integer> recentCounts = new HashMap<>();
        for (int i = entries.size() - 1; i >= 0; i--) {
            LedgerEntry entry = entries.get(i);
            if (npcAccounts.contains(entry.getAccountId())
                    && recentCounts.getOrDefault(entry.getAccountId(), 0) < 10) {
                recent.add(entry);
                recentCounts.merge(entry.getAccountId(), 1, Integer::sum);
            }
        }
        java.util.function.Predicate<LedgerEntry> removable = entry -> npcAccounts.contains(entry.getAccountId())
                && !recent.contains(entry) && !isLargeNpcTrade(entry) && isInternalMatchingEntry(entry);
        entries.removeIf(entry -> {
            if (!removable.test(entry)) return false;
            BigDecimal amount = entry.getAmount().getAmount();
            compactedCash.computeIfAbsent(entry.getAccountId(), id -> new EnumMap<>(Currency.class))
                    .merge(entry.getCurrency(), entry.isCredit() ? amount : amount.negate(), BigDecimal::add);
            processedRequests.remove(entry.getRequestId(), entry);
            compactedCount++;
            return true;
        });
        for (String account : npcAccounts) {
            List<LedgerEntry> rows = entriesByAccount.get(account);
            if (rows != null) rows.removeIf(removable);
        }
    }

    private static boolean isInternalMatchingEntry(LedgerEntry entry) {
        // These IDs are generated for a new fill in MatchingEngine and are never supplied by a retry caller.
        String request = entry.getRequestId();
        return switch (entry.getType()) {
            case TRADE_BUY -> request.startsWith("BUY-FILL-") || request.startsWith("AUCTION-BUY-");
            case TRADE_SELL -> request.startsWith("SELL-FILL-") || request.startsWith("AUCTION-SELL-");
            case TRANSACTION_FEE -> request.startsWith("BUY-FEE-") || request.startsWith("SELL-FEE-")
                    || request.startsWith("AUCTION-BUYFEE-") || request.startsWith("AUCTION-SELLFEE-");
            default -> false;
        };
    }

    public synchronized JSONObject exportNpcSummary() throws JSONException {
        if (compactedCount == 0) return null;
        JSONObject balances = new JSONObject();
        for (var account : compactedCash.entrySet()) {
            JSONObject currencies = new JSONObject();
            for (var value : account.getValue().entrySet())
                currencies.put(value.getKey().name(), value.getValue().toPlainString());
            balances.put(account.getKey(), currencies);
        }
        return new JSONObject().put("count", compactedCount).put("cash", balances);
    }

    public synchronized void restoreNpcSummary(JSONObject summary, Set<String> npcAccounts) throws JSONException {
        if (summary == null) return;
        int count = summary.getInt("count");
        JSONObject balances = summary.getJSONObject("cash");
        if (count <= 0 || balances.length() == 0 || balances.length() > count)
            throw new IllegalArgumentException("Ringkasan ledger NPC tidak valid");
        Math.addExact(entries.size(), count);
        Map<String, EnumMap<Currency, BigDecimal>> restored = new HashMap<>();
        for (Iterator<String> ids = balances.keys(); ids.hasNext();) {
            String id = ids.next();
            if (!npcAccounts.contains(id)) throw new IllegalArgumentException("Akun ringkasan ledger NPC tidak valid");
            JSONObject currencies = balances.getJSONObject(id);
            if (currencies.length() == 0) throw new IllegalArgumentException("Saldo ringkasan ledger NPC kosong");
            EnumMap<Currency, BigDecimal> totals = new EnumMap<>(Currency.class);
            for (Iterator<String> names = currencies.keys(); names.hasNext();) {
                String currency = names.next();
                totals.put(Currency.valueOf(currency), new BigDecimal(currencies.getString(currency)));
            }
            restored.put(id, totals);
        }
        compactedCash.clear();
        compactedCash.putAll(restored);
        compactedCount = count;
        sequence = getEntryCount();
    }

    public synchronized List<LedgerEntry> getEntriesForAccount(String accountId) {
        return new ArrayList<>(entriesByAccount.getOrDefault(accountId, Collections.emptyList()));
    }

    public synchronized void clear() {
        this.entries.clear();
        this.entriesByAccount.clear();
        this.processedRequests.clear();
        compactedCash.clear();
        compactedCount = 0;
        sequence = 0;
    }

    public synchronized void restoreEntries(List<LedgerEntry> savedEntries) {
        clear();
        if (savedEntries != null) {
            for (LedgerEntry e : savedEntries) {
                this.entries.add(e);
                this.entriesByAccount.computeIfAbsent(e.getAccountId(), key -> new ArrayList<>()).add(e);
                if (e.getRequestId() != null && !e.getRequestId().isEmpty()) {
                    this.processedRequests.put(e.getRequestId(), e);
                }
            }
        }
        sequence = entries.size();
    }

    public synchronized void restoreAndCompactValidatedEntries(List<LedgerEntry> savedEntries, Set<String> npcAccounts) {
        clear();
        entries.addAll(savedEntries);
        compactNpcTrades(npcAccounts);
        for (LedgerEntry entry : entries) {
            entriesByAccount.computeIfAbsent(entry.getAccountId(), key -> new ArrayList<>()).add(entry);
            processedRequests.put(entry.getRequestId(), entry);
        }
        sequence = getEntryCount();
    }

    /**
     * Memverifikasi konsistensi saldo akun terhadap buku besar.
     * Menghitung ulang seluruh kredit dan debit dari awal mula.
     *
     * @return true jika saldo akun sama persis dengan perhitungan mutasi ledger.
     */
    public synchronized boolean reconcile(Account account, Currency currency) {
        Map<Currency, BigDecimal> summary = compactedCash.get(account.getId());
        BigDecimal calculated = summary == null ? BigDecimal.ZERO : summary.getOrDefault(currency, BigDecimal.ZERO);
        for (LedgerEntry e : entriesByAccount.getOrDefault(account.getId(), Collections.emptyList())) {
            if (e.getCurrency() == currency) {
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
