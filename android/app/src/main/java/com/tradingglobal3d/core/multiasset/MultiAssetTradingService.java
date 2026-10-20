package com.tradingglobal3d.core.multiasset;

import com.tradingglobal3d.core.economy.*;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import org.json.*;
import java.math.*;
import java.time.*;
import java.util.*;

/** Finite dealer inventory, two-sided cash settlement, time-proportional coupons and maturity. */
public final class MultiAssetTradingService {
    private final Map<String, JSONObject> receipts = new LinkedHashMap<>();
    private final Map<String, Long> accrualAt = new LinkedHashMap<>();
    private final Map<String, BigDecimal> accrualRemainders = new LinkedHashMap<>();
    private final FundBuilderService products;
    public MultiAssetTradingService(FundBuilderService products) { this.products = products; }
    public void reset() { receipts.clear(); accrualAt.clear(); accrualRemainders.clear(); }
    private Money price(String id) {
        BondInstrument bond = products.getBond(id);
        if (bond != null) return bond.getCurrentCleanPrice();
        CommodityInstrument gold = products.getCommodity(id);
        if (gold != null) return gold.getSpotPricePerUnit();
        throw new IllegalArgumentException("Produk multi-aset tidak dikenal");
    }
    private String ticker(String id) { return products.getBond(id) != null ? products.getBond(id).getTicker() : products.getCommodity(id).getCode(); }
    private String exchange(String id) { return products.getBond(id) != null ? "BOND" : "COMM"; }
    private String positionId(String id) { return exchange(id) + ":" + ticker(id); }
    public void initializeDealer(Account dealer) {
        for (BondInstrument b : products.getAllBonds()) if (dealer.getPosition(positionId(b.getId())) == null)
            dealer.addPosition(new Position(b.getTicker(), "BOND", Currency.IDR, 200_000, b.getCurrentCleanPrice().getAmount(), b.getCurrentCleanPrice().getAmount()));
        for (CommodityInstrument c : products.getAllCommodities()) if (dealer.getPosition(positionId(c.getId())) == null)
            dealer.addPosition(new Position(c.getCode(), "COMM", Currency.IDR, 1_000_000, c.getSpotPricePerUnit().getAmount(), c.getSpotPricePerUnit().getAmount()));
    }
    public JSONObject quote(String id, boolean buy, long quantity, Account owner, Account dealer, long now) throws JSONException {
        if (quantity <= 0 || quantity > 1_000_000) throw new IllegalArgumentException("Jumlah harus 1–1.000.000 unit");
        BondInstrument bond = products.getBond(id);
        if (bond != null && !Instant.ofEpochMilli(now).atZone(ZoneId.of("Asia/Jakarta")).toLocalDate().isBefore(bond.getMaturityDate()))
            throw new IllegalStateException("Obligasi telah jatuh tempo");
        Money unit = price(id), gross = unit.multiply(BigDecimal.valueOf(quantity));
        Money fee = gross.multiply(new BigDecimal("0.001"));
        Position held = (buy ? dealer : owner).getPosition(positionId(id));
        if (held == null || held.getQuantity() < quantity) throw new IllegalStateException("Unit tersedia tidak mencukupi");
        if ((buy ? owner : dealer).getAvailableCash(Currency.IDR).isLessThan(buy ? gross.add(fee) : gross))
            throw new IllegalStateException("Kas pembeli/dealer tidak mencukupi");
        String version = id + ":" + owner.getId() + ":" + buy + ":" + quantity + ":" + unit.getAmount().toPlainString();
        return new JSONObject().put("success", true).put("instrumentId", id).put("accountId", owner.getId()).put("buy", buy)
                .put("quantity", quantity).put("unitPrice", unit.getAmount().toPlainString()).put("gross", gross.getAmount().toPlainString())
                .put("fee", fee.getAmount().toPlainString()).put("cashChange", (buy ? gross.add(fee).negate() : gross.subtract(fee)).getAmount().toPlainString())
                .put("quoteVersion", version).put("currency", "IDR").put("asOf", now);
    }
    public JSONObject transact(String requestId, String id, boolean buy, long quantity, String version,
            Account owner, Account dealer, EconomyLedger ledger, long now) throws JSONException {
        if (requestId == null || !requestId.matches("[A-Za-z0-9_-]{1,160}")) throw new IllegalArgumentException("ID transaksi tidak valid");
        JSONObject prior = receipts.get(requestId);
        String input = new JSONArray().put(id).put(buy).put(quantity).put(owner.getId()).put(version).toString();
        if (prior != null) {
            if (!input.equals(prior.getString("input"))) throw new IllegalArgumentException("ID transaksi telah dipakai untuk input berbeda");
            return new JSONObject(prior.toString());
        }
        JSONObject q = quote(id, buy, quantity, owner, dealer, now);
        if (!q.getString("quoteVersion").equals(version)) throw new IllegalStateException("Quote berubah; tinjau ulang transaksi");
        settleCoupon(owner, dealer, ledger, now);
        Account buyer = buy ? owner : dealer, seller = buy ? dealer : owner;
        Money gross = new Money(new BigDecimal(q.getString("gross")), Currency.IDR), fee = new Money(new BigDecimal(q.getString("fee")), Currency.IDR);
        ledger.recordMutation("MULTI-" + requestId + "-BUY", buyer, LedgerEntryType.TRADE_BUY, gross, false, now, "Pembelian " + id);
        ledger.recordMutation("MULTI-" + requestId + "-SELL", seller, LedgerEntryType.TRADE_SELL, gross, true, now, "Penjualan " + id);
        ledger.recordMutation("MULTI-" + requestId + "-FEE", owner, LedgerEntryType.TRANSACTION_FEE, fee, false, now, "Biaya multi-aset " + id);
        Position buyerPosition = buyer.getPosition(positionId(id));
        if (buyerPosition == null) { buyerPosition = new Position(ticker(id), exchange(id), Currency.IDR, 0, BigDecimal.ZERO, price(id).getAmount()); buyer.addPosition(buyerPosition); }
        buyerPosition.addBuy(quantity, price(id).getAmount());
        seller.getPosition(positionId(id)).addSell(quantity, price(id).getAmount());
        accrualAt.put(owner.getId(), now);
        q.put("requestId", requestId).put("status", "SETTLED").put("input", input);
        receipts.put(requestId, new JSONObject(q.toString()));
        return q;
    }
    public void settleCoupon(Account owner, Account dealer, EconomyLedger ledger, long now) {
        long previous = accrualAt.getOrDefault(owner.getId(), now);
        if (now < previous) throw new IllegalArgumentException("Waktu kupon tidak boleh mundur");
        for (BondInstrument bond : products.getAllBonds()) {
            Position position = owner.getPosition(positionId(bond.getId()));
            if (position == null || position.getQuantity() == 0) continue;
            long maturity = bond.getMaturityDate().atStartOfDay(ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli();
            long elapsed = Math.max(0, Math.min(now, maturity) - previous);
            BigDecimal exact = bond.getParValue().getAmount().multiply(BigDecimal.valueOf(position.getQuantity()))
                    .multiply(bond.getCouponAnnualRate()).multiply(BigDecimal.valueOf(elapsed))
                    .divide(BigDecimal.valueOf(365L * 86_400_000L), 16, RoundingMode.HALF_UP)
                    .add(accrualRemainders.getOrDefault(owner.getId(), BigDecimal.ZERO));
            Money due = new Money(exact.setScale(0, RoundingMode.DOWN), Currency.IDR);
            if (dealer.getAvailableCash(Currency.IDR).isLessThan(due)) throw new IllegalStateException("Kupon tertunda: likuiditas penerbit kurang");
            if (due.isPositive()) {
                String key = "BOND-COUPON:" + owner.getId() + ":" + previous + ":" + now;
                ledger.recordMutation(key + ":ISSUER", dealer, LedgerEntryType.DIVIDEND, due, false, now, "Kewajiban kupon " + bond.getTicker());
                ledger.recordMutation(key + ":HOLDER", owner, LedgerEntryType.DIVIDEND, due, true, now, "Kupon " + bond.getTicker());
            }
            accrualRemainders.put(owner.getId(), exact.subtract(due.getAmount()));
            if (now >= maturity) {
                Money principal = bond.getParValue().multiply(BigDecimal.valueOf(position.getQuantity()));
                if (dealer.getAvailableCash(Currency.IDR).isLessThan(principal)) throw new IllegalStateException("Pelunasan obligasi tertunda");
                String key = "BOND-MATURITY:" + owner.getId() + ":" + bond.getId();
                ledger.recordMutation(key + ":ISSUER", dealer, LedgerEntryType.WITHDRAWAL, principal, false, now, "Pelunasan obligasi");
                ledger.recordMutation(key + ":HOLDER", owner, LedgerEntryType.TRADE_SELL, principal, true, now, "Pelunasan pokok obligasi");
                position.addSell(position.getQuantity(), bond.getParValue().getAmount());
            }
        }
        accrualAt.put(owner.getId(), now);
    }
    public JSONObject exportState() throws JSONException {
        return new JSONObject().put("receipts", new JSONObject(receipts)).put("accrualAt", new JSONObject(accrualAt)).put("remainders", new JSONObject(accrualRemainders));
    }
    public void restore(JSONObject saved) throws JSONException {
        Map<String, JSONObject> stagedReceipts = new LinkedHashMap<>(); Map<String, Long> stagedAt = new LinkedHashMap<>(); Map<String, BigDecimal> stagedRemainders = new LinkedHashMap<>();
        JSONObject r = saved.optJSONObject("receipts"), a = saved.optJSONObject("accrualAt"), f = saved.optJSONObject("remainders");
        if (r != null) for (String k : r.keySet()) stagedReceipts.put(k, r.getJSONObject(k));
        if (a != null) for (String k : a.keySet()) { long v = a.getLong(k); if (v < 0) throw new IllegalArgumentException("Anchor kupon tidak valid"); stagedAt.put(k, v); }
        if (f != null) for (String k : f.keySet()) { BigDecimal v = new BigDecimal(f.get(k).toString()); if (v.signum() < 0 || v.compareTo(BigDecimal.ONE) >= 0) throw new IllegalArgumentException("Sisa kupon tidak valid"); stagedRemainders.put(k, v); }
        reset(); receipts.putAll(stagedReceipts); accrualAt.putAll(stagedAt); accrualRemainders.putAll(stagedRemainders);
    }
}
