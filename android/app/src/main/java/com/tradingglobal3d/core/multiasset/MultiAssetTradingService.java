package com.tradingglobal3d.core.multiasset;

import com.tradingglobal3d.core.economy.*;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.market.*;
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
    private final Map<String, BigDecimal> referenceStocks = new LinkedHashMap<>();
    private BigDecimal referenceFx;
    private long valuationAnchor = -1, valuationAt = -1;
    public MultiAssetTradingService(FundBuilderService products) { this.products = products; }
    public void reset() { receipts.clear(); accrualAt.clear(); accrualRemainders.clear(); referenceStocks.clear(); referenceFx = null; valuationAnchor = -1; valuationAt = -1; }
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
        if (now < 0 || owner.getId().equals(dealer.getId())) throw new IllegalArgumentException("Konteks transaksi tidak sah");
        if (quantity <= 0 || quantity > 1_000_000) throw new IllegalArgumentException("Jumlah harus 1–1.000.000 unit");
        BondInstrument bond = products.getBond(id);
        if (bond != null && !Instant.ofEpochMilli(now).atZone(ZoneId.of("Asia/Jakarta")).toLocalDate().isBefore(bond.getMaturityDate()))
            throw new IllegalStateException("Obligasi telah jatuh tempo");
        Money unit = price(id), gross = unit.multiply(BigDecimal.valueOf(quantity));
        if (!unit.isPositive() || unit.getCurrency() != Currency.IDR) throw new IllegalStateException("Harga produk tidak sah");
        Money fee = gross.multiply(new BigDecimal("0.001"));
        Position held = (buy ? dealer : owner).getPosition(positionId(id));
        if (held == null || held.getQuantity() < quantity) throw new IllegalStateException("Unit tersedia tidak mencukupi");
        if ((buy ? owner : dealer).getAvailableCash(Currency.IDR).isLessThan(buy ? gross.add(fee) : gross))
            throw new IllegalStateException("Kas pembeli/dealer tidak mencukupi");
        String version = id + ":" + owner.getId() + ":" + buy + ":" + quantity + ":" + unit.getAmount().toPlainString() + ":" + now;
        return new JSONObject().put("success", true).put("instrumentId", id).put("accountId", owner.getId()).put("buy", buy)
                .put("quantity", quantity).put("unitPrice", unit.getAmount().toPlainString()).put("gross", gross.getAmount().toPlainString())
                .put("fee", fee.getAmount().toPlainString()).put("cashChange", (buy ? gross.add(fee).multiply(-1) : gross.subtract(fee)).getAmount().toPlainString())
                .put("quoteVersion", version).put("currency", "IDR").put("asOf", now).put("expiresAt", Math.addExact(now, 7_200_000L));
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
        int lastColon = version == null ? -1 : version.lastIndexOf(':');
        long issued;
        try { issued = Long.parseLong(version.substring(lastColon + 1)); }
        catch (RuntimeException invalid) { throw new IllegalArgumentException("Versi quote tidak sah"); }
        String current = q.getString("quoteVersion");
        if (lastColon < 0 || !current.substring(0, current.lastIndexOf(':')).equals(version.substring(0, lastColon))
                || issued > now || now - issued > 7_200_000L) throw new IllegalStateException("Quote berubah/kedaluwarsa; tinjau ulang transaksi");
        q.put("quoteVersion", version).put("asOf", issued).put("expiresAt", issued + 7_200_000L).put("settledAt", now);
        Account buyer = buy ? owner : dealer, seller = buy ? dealer : owner;
        Money gross = new Money(new BigDecimal(q.getString("gross")), Currency.IDR), fee = new Money(new BigDecimal(q.getString("fee")), Currency.IDR);
        List<CouponSettlement> coupons = couponPlan(owner, dealer, now);
        Money issuerNeed = buy ? Money.zero(Currency.IDR) : gross;
        for (CouponSettlement settlement : coupons) issuerNeed = issuerNeed.add(settlement.due).add(settlement.principal);
        if (dealer.getAvailableCash(Currency.IDR).isLessThan(issuerNeed)) throw new IllegalStateException("Transaksi ditunda: likuiditas penerbit kurang");
        Position existingBuyer = buyer.getPosition(positionId(id));
        if (existingBuyer != null) Math.addExact(existingBuyer.getQuantity(), quantity);
        Position sellerPosition = seller.getPosition(positionId(id));
        Money realizedNet = buy ? null : gross.subtract(new Money(sellerPosition.getAverageCost().multiply(BigDecimal.valueOf(quantity)), Currency.IDR))
                .subtract(sellerPosition.previewAcquisitionFeeAllocation(quantity)).subtract(fee);
        applyCoupons(owner, dealer, ledger, coupons, now);
        ledger.recordMutation("MULTI-" + requestId + "-BUY", buyer, LedgerEntryType.TRADE_BUY, gross, false, now, "Pembelian " + id);
        ledger.recordMutation("MULTI-" + requestId + "-SELL", seller, LedgerEntryType.TRADE_SELL, gross, true, now, "Penjualan " + id);
        ledger.recordMutation("MULTI-" + requestId + "-FEE", owner, LedgerEntryType.TRANSACTION_FEE, fee, false, now, "Biaya multi-aset " + id);
        Position buyerPosition = buyer.getPosition(positionId(id));
        if (buyerPosition == null) { buyerPosition = new Position(ticker(id), exchange(id), Currency.IDR, 0, BigDecimal.ZERO, price(id).getAmount()); buyer.addPosition(buyerPosition); }
        buyerPosition.addBuy(quantity, price(id).getAmount(), buy ? fee : Money.zero(Currency.IDR));
        sellerPosition.addSell(quantity, price(id).getAmount());
        for (BondInstrument bond : products.getAllBonds()) accrualAt.put(accrualKey(owner, bond), now);
        q.put("requestId", requestId).put("status", "SETTLED").put("input", input)
                .put("realizedNetPnL", realizedNet == null ? JSONObject.NULL : realizedNet.getAmount().toPlainString());
        receipts.put(requestId, new JSONObject(q.toString()));
        return q;
    }
    private static String accrualKey(Account owner, BondInstrument bond) { return owner.getId() + "|" + bond.getId(); }
    private static final class CouponSettlement {
        final BondInstrument bond; final long previous; final Money due, principal; final BigDecimal remainder;
        CouponSettlement(BondInstrument bond, long previous, Money due, Money principal, BigDecimal remainder) {
            this.bond = bond; this.previous = previous; this.due = due; this.principal = principal; this.remainder = remainder;
        }
    }
    private List<CouponSettlement> couponPlan(Account owner, Account dealer, long now) {
        if (now < 0 || owner.getId().equals(dealer.getId())) throw new IllegalArgumentException("Konteks kupon tidak sah");
        List<CouponSettlement> plan = new ArrayList<>();
        for (BondInstrument bond : products.getAllBonds()) {
            String anchorKey = accrualKey(owner, bond);
            long previous = accrualAt.getOrDefault(anchorKey, accrualAt.getOrDefault(owner.getId(), now));
            if (now < previous) throw new IllegalArgumentException("Waktu kupon tidak boleh mundur");
            Position position = owner.getPosition(positionId(bond.getId()));
            long quantity = position == null ? 0 : position.getQuantity();
            if (quantity < 0) throw new IllegalStateException("Posisi obligasi negatif");
            long maturity = bond.getMaturityDate().atStartOfDay(ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli();
            long elapsed = Math.max(0, Math.min(now, maturity) - previous);
            BigDecimal exact = bond.getParValue().getAmount().multiply(BigDecimal.valueOf(quantity))
                    .multiply(bond.getCouponAnnualRate()).multiply(BigDecimal.valueOf(elapsed))
                    .divide(BigDecimal.valueOf(365L * 86_400_000L), 16, RoundingMode.HALF_UP)
                    .add(accrualRemainders.getOrDefault(anchorKey, BigDecimal.ZERO));
            Money due = new Money(exact.setScale(0, RoundingMode.DOWN), Currency.IDR);
            Money principal = now >= maturity ? bond.getParValue().multiply(BigDecimal.valueOf(quantity)) : Money.zero(Currency.IDR);
            plan.add(new CouponSettlement(bond, previous, due, principal, exact.subtract(due.getAmount())));
        }
        return plan;
    }
    public void settleCoupon(Account owner, Account dealer, EconomyLedger ledger, long now) {
        List<CouponSettlement> plan = couponPlan(owner, dealer, now);
        Money total = Money.zero(Currency.IDR);
        for (CouponSettlement settlement : plan) total = total.add(settlement.due).add(settlement.principal);
        if (dealer.getAvailableCash(Currency.IDR).isLessThan(total)) throw new IllegalStateException("Kupon/pelunasan tertunda: likuiditas penerbit kurang");
        applyCoupons(owner, dealer, ledger, plan, now);
    }
    private void applyCoupons(Account owner, Account dealer, EconomyLedger ledger, List<CouponSettlement> plan, long now) {
        for (CouponSettlement settlement : plan) {
            BondInstrument bond = settlement.bond; Money due = settlement.due;
            String anchorKey = accrualKey(owner, bond);
            if (due.isPositive()) {
                String key = "BOND-COUPON:" + anchorKey + ":" + settlement.previous + ":" + now;
                ledger.recordMutation(key + ":ISSUER", dealer, LedgerEntryType.DIVIDEND, due, false, now, "Kewajiban kupon " + bond.getTicker());
                ledger.recordMutation(key + ":HOLDER", owner, LedgerEntryType.DIVIDEND, due, true, now, "Kupon " + bond.getTicker());
            }
            accrualRemainders.put(anchorKey, settlement.remainder);
            if (settlement.principal.isPositive()) {
                Money principal = settlement.principal;
                String key = "BOND-MATURITY:" + owner.getId() + ":" + bond.getId();
                ledger.recordMutation(key + ":ISSUER", dealer, LedgerEntryType.WITHDRAWAL, principal, false, now, "Pelunasan obligasi");
                ledger.recordMutation(key + ":HOLDER", owner, LedgerEntryType.TRADE_SELL, principal, true, now, "Pelunasan pokok obligasi");
                Position position = owner.getPosition(positionId(bond.getId()));
                position.addSell(position.getQuantity(), bond.getParValue().getAmount());
            }
            accrualAt.put(anchorKey, now);
        }
    }
    /** Game valuation rule, not a real-market price feed: gold follows FX and a countercyclical stock basket;
     * bond yield responds to that same basket/FX and clean premium converges to par at maturity. */
    public void updateValuations(InstrumentCatalog catalog, FxExchangeService fx, Map<String, Account> accounts, long now) {
        if (now < 0 || now < valuationAt) throw new IllegalArgumentException("Waktu valuasi tidak sah");
        BigDecimal rate = fx.getMidRate();
        if (rate.signum() <= 0) throw new IllegalArgumentException("Kurs valuasi tidak sah");
        if (referenceFx == null) { referenceFx = rate; valuationAnchor = now; }
        BigDecimal basket = BigDecimal.ZERO; int count = 0;
        for (Instrument stock : catalog.getActiveUniverse()) if (stock.getCurrency() == Currency.IDR && stock.getCurrentPrice().isPositive()) {
            referenceStocks.putIfAbsent(stock.getId(), stock.getCurrentPrice().getAmount());
            basket = basket.add(stock.getCurrentPrice().getAmount().divide(referenceStocks.get(stock.getId()), 16, RoundingMode.HALF_UP)); count++;
        }
        basket = count == 0 ? BigDecimal.ONE : basket.divide(BigDecimal.valueOf(count), 16, RoundingMode.HALF_UP);
        BigDecimal fxRatio = rate.divide(referenceFx, 16, RoundingMode.HALF_UP);
        BigDecimal risk = basket.subtract(BigDecimal.ONE);
        for (CommodityInstrument gold : products.getAllCommodities()) {
            if (!"COMM:GOLD".equals(gold.getId())) continue;
            BigDecimal value = new BigDecimal("1450000").multiply(fxRatio)
                    .multiply(BigDecimal.ONE.subtract(risk.multiply(new BigDecimal("0.25"))).max(new BigDecimal("0.2")));
            gold.updateSpotPrice(new Money(value.max(BigDecimal.ONE), Currency.IDR));
        }
        for (BondInstrument bond : products.getAllBonds()) {
            long maturity = bond.getMaturityDate().atStartOfDay(ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli();
            BigDecimal remaining = BigDecimal.valueOf(Math.max(0, maturity - now));
            BigDecimal original = BigDecimal.valueOf(Math.max(1, maturity - valuationAnchor));
            BigDecimal duration = remaining.divide(BigDecimal.valueOf(365L * 86_400_000L), 16, RoundingMode.HALF_UP).min(BigDecimal.valueOf(8));
            BigDecimal shock = risk.multiply(new BigDecimal("0.02")).add(fxRatio.subtract(BigDecimal.ONE).multiply(new BigDecimal("0.01")));
            BigDecimal premium = bond.getParValue().getAmount().multiply(new BigDecimal("0.02")).multiply(remaining.divide(original, 16, RoundingMode.HALF_UP));
            BigDecimal value = bond.getParValue().getAmount().add(premium)
                    .subtract(bond.getParValue().getAmount().multiply(shock).multiply(duration));
            bond.updatePrice(new Money(value.max(BigDecimal.ONE), Currency.IDR));
        }
        for (Account account : accounts.values()) {
            for (BondInstrument bond : products.getAllBonds()) {
                Position held = account.getPosition(positionId(bond.getId()));
                if (held != null) held.updateMarketPrice(bond.getCurrentCleanPrice().getAmount());
            }
            for (CommodityInstrument gold : products.getAllCommodities()) {
                Position held = account.getPosition(positionId(gold.getId()));
                if (held != null) held.updateMarketPrice(gold.getSpotPricePerUnit().getAmount());
            }
        }
        valuationAt = now;
    }
    public JSONObject exportState() throws JSONException {
        JSONObject prices = new JSONObject();
        for (BondInstrument bond : products.getAllBonds()) prices.put(bond.getId(), bond.getCurrentCleanPrice().getAmount().toPlainString());
        for (CommodityInstrument gold : products.getAllCommodities()) prices.put(gold.getId(), gold.getSpotPricePerUnit().getAmount().toPlainString());
        return new JSONObject().put("receipts", new JSONObject(receipts)).put("accrualAt", new JSONObject(accrualAt)).put("remainders", new JSONObject(accrualRemainders))
                .put("prices", prices).put("referenceStocks", new JSONObject(referenceStocks)).put("referenceFx", referenceFx == null ? JSONObject.NULL : referenceFx.toPlainString())
                .put("valuationAnchor", valuationAnchor).put("valuationAt", valuationAt);
    }
    public void restore(JSONObject saved) throws JSONException {
        Map<String, JSONObject> stagedReceipts = new LinkedHashMap<>(); Map<String, Long> stagedAt = new LinkedHashMap<>(); Map<String, BigDecimal> stagedRemainders = new LinkedHashMap<>();
        JSONObject r = saved.optJSONObject("receipts"), a = saved.optJSONObject("accrualAt"), f = saved.optJSONObject("remainders");
        if (r != null) { java.util.Iterator<String> _rk = r.keys(); while (_rk.hasNext()) { String k = _rk.next(); {
            JSONObject receipt = new JSONObject(r.getJSONObject(k).toString());
            String id = receipt.getString("instrumentId"), owner = receipt.getString("accountId");
            long quantity = receipt.getLong("quantity"), issued = receipt.getLong("asOf");
            boolean buy = receipt.getBoolean("buy");
            BigDecimal unit = new BigDecimal(receipt.getString("unitPrice"));
            Money gross = new Money(unit.multiply(BigDecimal.valueOf(quantity)),Currency.IDR), fee = gross.multiply(new BigDecimal("0.001"));
            String version = receipt.getString("quoteVersion");
            JSONArray input = new JSONArray(receipt.getString("input"));
            if (!k.matches("[A-Za-z0-9_-]{1,160}") || !k.equals(receipt.getString("requestId"))
                    || !receipt.getBoolean("success") || !"SETTLED".equals(receipt.getString("status"))
                    || (products.getBond(id) == null && products.getCommodity(id) == null) || owner.trim().isEmpty()
                    || quantity < 1 || quantity > 1_000_000 || unit.signum() <= 0 || issued < 0
                    || new BigDecimal(receipt.getString("gross")).compareTo(gross.getAmount()) != 0
                    || new BigDecimal(receipt.getString("fee")).compareTo(fee.getAmount()) != 0
                    || new BigDecimal(receipt.getString("cashChange")).compareTo((buy ? gross.add(fee).multiply(-1) : gross.subtract(fee)).getAmount()) != 0
                    || input.length()!=5 || !id.equals(input.getString(0)) || buy!=input.getBoolean(1)
                    || quantity!=input.getLong(2) || !owner.equals(input.getString(3)) || !version.equals(input.getString(4)))
                throw new IllegalArgumentException("Receipt multi-aset tidak valid");
            stagedReceipts.put(k, receipt);
        } } }
        if (a != null) { java.util.Iterator<String> _ak = a.keys(); while (_ak.hasNext()) { String k = _ak.next(); long v = a.getLong(k); if (v < 0 || k.trim().isEmpty()) throw new IllegalArgumentException("Anchor kupon tidak valid"); stagedAt.put(k, v); } }
        if (f != null) { java.util.Iterator<String> _fk = f.keys(); while (_fk.hasNext()) { String k = _fk.next(); BigDecimal v = new BigDecimal(f.get(k).toString()); if (v.signum() < 0 || v.compareTo(BigDecimal.ONE) >= 0) throw new IllegalArgumentException("Sisa kupon tidak valid"); stagedRemainders.put(k, v); } }
        Map<String, BigDecimal> prices = new LinkedHashMap<>(), stocks = new LinkedHashMap<>();
        JSONObject p = saved.optJSONObject("prices"), refs = saved.optJSONObject("referenceStocks");
        if (p != null) { java.util.Iterator<String> _pk = p.keys(); while (_pk.hasNext()) { String id = _pk.next();
            if (products.getBond(id) == null && products.getCommodity(id) == null) throw new IllegalArgumentException("Produk save tidak dikenal");
            BigDecimal value = new BigDecimal(p.getString(id)); if (value.signum() <= 0) throw new IllegalArgumentException("Harga save tidak sah"); prices.put(id, value);
        } }
        if (refs != null) { java.util.Iterator<String> _refk = refs.keys(); while (_refk.hasNext()) { String id = _refk.next(); BigDecimal value = new BigDecimal(refs.get(id).toString()); if (value.signum() <= 0) throw new IllegalArgumentException("Harga referensi tidak sah"); stocks.put(id, value); } }
        BigDecimal rate = saved.isNull("referenceFx") || !saved.has("referenceFx") ? null : new BigDecimal(saved.getString("referenceFx"));
        long anchor = saved.optLong("valuationAnchor", -1), at = saved.optLong("valuationAt", -1);
        if ((rate != null && (rate.signum() <= 0 || anchor < 0)) || anchor < -1 || at < anchor || (rate == null && (anchor != -1 || at != -1))) throw new IllegalArgumentException("Anchor valuasi tidak sah");
        reset(); receipts.putAll(stagedReceipts); accrualAt.putAll(stagedAt); accrualRemainders.putAll(stagedRemainders);
        referenceStocks.putAll(stocks); referenceFx = rate; valuationAnchor = anchor; valuationAt = at;
        for (Map.Entry<String, BigDecimal> entry : prices.entrySet()) {
            BondInstrument bond = products.getBond(entry.getKey());
            if (bond != null) bond.updatePrice(new Money(entry.getValue(), Currency.IDR));
            else products.getCommodity(entry.getKey()).updateSpotPrice(new Money(entry.getValue(), Currency.IDR));
        }
    }
}
