package com.tradingglobal3d.core.economy;

import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Layanan Penukaran Valuta Asing (FX Exchange Service).
 * Mencegah arbitrase pembulatan, menolak konversi dari dana tertahan,
 * dan menyelesaikan debit/kredit secara atomik.
 */
public final class FxExchangeService {
    private BigDecimal currentMidRate = new BigDecimal("15800.00");
    private FxQuote currentQuote;
    private final java.util.Map<String, Receipt> receipts = new java.util.LinkedHashMap<>();

    public synchronized void reset() {
        currentMidRate = new BigDecimal("15800.00");
        currentQuote = null;
        receipts.clear();
    }

    private static final class Receipt {
        final String accountId;
        final FxQuote.ConversionPreview preview;
        final FxQuote quote;
        Receipt(String accountId, FxQuote.ConversionPreview preview, FxQuote quote) {
            this.accountId = accountId; this.preview = preview; this.quote = quote;
        }
    }

    public synchronized org.json.JSONObject exportState() throws org.json.JSONException {
        org.json.JSONArray saved = new org.json.JSONArray();
        for (java.util.Map.Entry<String, Receipt> entry : receipts.entrySet()) {
            Receipt receipt = entry.getValue(); FxQuote.ConversionPreview p = receipt.preview;
            saved.put(new org.json.JSONObject().put("requestId", entry.getKey()).put("accountId", receipt.accountId)
                    .put("from", p.from.name()).put("to", p.to.name()).put("amount", p.fromAmount.getAmount().toPlainString())
                    .put("rate", p.effectiveRate.toPlainString()).put("gross", p.grossToAmount.getAmount().toPlainString())
                    .put("fee", p.feeAmount.getAmount().toPlainString()).put("net", p.netToAmount.getAmount().toPlainString())
                    .put("quoteId", receipt.quote.getQuoteId()).put("quoteMid", receipt.quote.getMidRate().toPlainString())
                    .put("quoteCreatedAt", receipt.quote.getCreatedAt()));
        }
        return new org.json.JSONObject().put("midRate", currentMidRate.toPlainString()).put("receipts", saved);
    }

    public synchronized void restoreState(org.json.JSONObject state) throws org.json.JSONException {
        BigDecimal rate = new BigDecimal(state.getString("midRate"));
        if (rate.signum() <= 0) throw new IllegalArgumentException("Kurs save tidak valid");
        java.util.Map<String, Receipt> staged = new java.util.LinkedHashMap<>();
        org.json.JSONArray saved = state.getJSONArray("receipts");
        for (int i = 0; i < saved.length(); i++) {
            org.json.JSONObject item = saved.getJSONObject(i);
            Currency from = Currency.valueOf(item.getString("from")), to = Currency.valueOf(item.getString("to"));
            Money amount = new Money(new BigDecimal(item.getString("amount")), from);
            BigDecimal effective = new BigDecimal(item.getString("rate"));
            Money gross = new Money(new BigDecimal(item.getString("gross")), to);
            Money fee = new Money(new BigDecimal(item.getString("fee")), to);
            Money net = new Money(new BigDecimal(item.getString("net")), to);
            String id = item.getString("requestId");
            if (id.trim().isEmpty() || staged.containsKey(id) || from == to || !amount.isPositive()
                    || effective.signum() <= 0 || !net.isPositive() || fee.getAmount().signum() < 0 || !gross.subtract(fee).equals(net))
                throw new IllegalArgumentException("Receipt FX save tidak valid");
            FxQuote quote = new FxQuote(item.getString("quoteId"), new BigDecimal(item.getString("quoteMid")), item.getLong("quoteCreatedAt"));
            staged.put(id, new Receipt(item.getString("accountId"), new FxQuote.ConversionPreview(from, to, amount, effective, gross, fee, net), quote));
        }
        currentMidRate = rate; currentQuote = null; receipts.clear(); receipts.putAll(staged);
    }

    public synchronized FxQuote getReceiptQuote(String requestId) {
        Receipt receipt = receipts.get(requestId);
        return receipt == null ? null : receipt.quote;
    }

    public synchronized ConversionResult executeReviewedConversion(String requestId, String quoteId, Account account,
            Currency from, Currency to, Money amount, long now, EconomyLedger ledger) {
        Receipt receipt = receipts.get(requestId);
        if (receipt != null) {
            if (!receipt.quote.getQuoteId().equals(quoteId)) return new ConversionResult(false, "ID permintaan terikat quote berbeda", null);
            return executeConversion(requestId, account, receipt.quote, from, to, amount, now, ledger);
        }
        if (currentQuote == null || !currentQuote.getQuoteId().equals(quoteId) || currentQuote.isExpired(now))
            return new ConversionResult(false, "Kuotasi berubah atau kedaluwarsa; tinjau ulang konversi", null);
        return executeConversion(requestId, account, currentQuote, from, to, amount, now, ledger);
    }

    public synchronized void setMidRate(BigDecimal midRate) {
        this.currentMidRate = Objects.requireNonNull(midRate);
        this.currentQuote = null; // Batalkan quote lama
    }

    public synchronized BigDecimal getMidRate() {
        return currentMidRate;
    }

    public synchronized FxQuote getOrGenerateQuote(long currentTimestamp) {
        if (currentQuote == null || currentQuote.isExpired(currentTimestamp)) {
            String qId = "QUOTE-FX-" + java.util.UUID.randomUUID();
            currentQuote = new FxQuote(qId, currentMidRate, currentTimestamp);
        }
        return currentQuote;
    }

    public static final class ConversionResult {
        public final boolean isSuccess;
        public final String message;
        public final FxQuote.ConversionPreview preview;

        public ConversionResult(boolean isSuccess, String message, FxQuote.ConversionPreview preview) {
            this.isSuccess = isSuccess;
            this.message = message;
            this.preview = preview;
        }
    }

    /**
     * Mengeksekusi konversi mata uang secara atomik.
     */
    public synchronized ConversionResult executeConversion(
            String requestId,
            Account account,
            FxQuote quote,
            Currency from,
            Currency to,
            Money fromAmount,
            long currentTimestamp,
            EconomyLedger ledger) {

        Objects.requireNonNull(account);
        Objects.requireNonNull(quote);
        Objects.requireNonNull(fromAmount);
        Objects.requireNonNull(ledger);

        if (requestId == null || requestId.trim().isEmpty())
            return new ConversionResult(false, "ID permintaan FX wajib diisi", null);
        Receipt existing = receipts.get(requestId);
        if (existing != null) {
            FxQuote.ConversionPreview p = existing.preview;
            if (!existing.accountId.equals(account.getId()) || p.from != from || p.to != to || !p.fromAmount.equals(fromAmount))
                return new ConversionResult(false, "ID permintaan FX sudah digunakan dengan rincian berbeda", null);
            return new ConversionResult(true, "Hasil konversi yang sudah diselesaikan", p);
        }

        // 1. Validasi Kedaluwarsa Kuotasi (FR-06.03)
        if (quote.isExpired(currentTimestamp)) {
            return new ConversionResult(false, "Kuotasi nilai tukar telah kedaluwarsa. Silakan perbarui kurs.", null);
        }

        // 2. Validasi Kas Tersedia (Kas tertahan tidak boleh dipakai - FR-06.04)
        Money available = account.getAvailableCash(from);
        if (available.isLessThan(fromAmount)) {
            return new ConversionResult(false, "Kas " + from + " tersedia (" + available + ") tidak cukup untuk konversi (tertahan order lain).", null);
        }

        // 3. Hitung Preview & Konversi
        FxQuote.ConversionPreview preview = quote.calculatePreview(from, to, fromAmount);

        // Reject both legs before mutation if either key belongs to another operation.
        String debitReqId = requestId + "-DEBIT";
        String creditReqId = requestId + "-CREDIT";
        if (ledger.isRequestProcessed(debitReqId) || ledger.isRequestProcessed(creditReqId))
            return new ConversionResult(false, "ID ledger FX sudah terpakai; konversi tidak diterapkan", null);

        ledger.recordMutation(
                debitReqId, account,
                LedgerEntryType.FX_CONVERSION_DEBIT,
                fromAmount, false, currentTimestamp,
                "Konversi FX jual " + fromAmount + " ke " + to + " @ " + preview.effectiveRate
        );

        ledger.recordMutation(
                creditReqId, account,
                LedgerEntryType.FX_CONVERSION_CREDIT,
                preview.netToAmount, true, currentTimestamp,
                "Konversi FX terima " + preview.netToAmount + " dari " + from
        );

        receipts.put(requestId, new Receipt(account.getId(), preview, quote));
        return new ConversionResult(true, "Konversi berhasil diselesaikan.", preview);
    }
}
