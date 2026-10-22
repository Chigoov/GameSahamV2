package com.tradingglobal3d.core.collection;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * Model Barang Koleksi, Properti, dan Kendaraan Mewah.
 */
public final class CollectibleItem {
    public enum Category {
        VEHICLE("Kendaraan"),
        REAL_ESTATE("Properti"),
        JEWELRY("Permata"),
        ART_AND_WATCHES("Seni & Jam Tangan");

        private final String label;
        Category(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum Rarity {
        STANDARD("Standar"),
        RARE("Langka"),
        LEGENDARY("Legenda");

        private final String label;
        Rarity(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final String id;
    private final String name;
    private final Category category;
    private final Rarity rarity;
    private final Money basePrice;
    private Money currentValuation;
    private final Money dailyMaintenanceCost;
    private final int reputationBonus;

    private boolean isOwned;
    private long purchaseTimestamp;
    private Money purchasePrice;
    private String ownerAccountId;
    private String pledgedLoanId;
    private Money maintenanceArrears = Money.zero(Currency.IDR);
    private long lastMaintenanceDay = Long.MIN_VALUE;
    private long valuationAnchorDay = Long.MIN_VALUE;
    private long valuationAsOfDay = Long.MIN_VALUE;
    private Money valuationAnchorPrice;
    private final Map<String, JSONObject> transactionReceipts = new LinkedHashMap<>();

    public CollectibleItem(
            String id,
            String name,
            Category category,
            Money basePrice,
            Money dailyMaintenanceCost,
            int reputationBonus) {
        this(id, name, category, Rarity.STANDARD, basePrice, dailyMaintenanceCost, reputationBonus);
    }

    public CollectibleItem(
            String id,
            String name,
            Category category,
            Rarity rarity,
            Money basePrice,
            Money dailyMaintenanceCost,
            int reputationBonus) {

        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.category = Objects.requireNonNull(category);
        this.rarity = rarity != null ? rarity : Rarity.STANDARD;
        this.basePrice = Objects.requireNonNull(basePrice);
        this.currentValuation = basePrice;
        this.dailyMaintenanceCost = Objects.requireNonNull(dailyMaintenanceCost);
        validatePrice(basePrice);
        if (dailyMaintenanceCost.getCurrency() != Currency.IDR || dailyMaintenanceCost.isNegative())
            throw new IllegalArgumentException("Biaya pemeliharaan wajib IDR nonnegatif");
        this.reputationBonus = reputationBonus;
        this.isOwned = false;
        this.purchaseTimestamp = 0L;
        this.purchasePrice = null;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public Rarity getRarity() { return rarity; }
    public Money getBasePrice() { return basePrice; }
    public Money getCurrentValuation() { return currentValuation; }
    public Money getDailyMaintenanceCost() { return dailyMaintenanceCost; }
    public int getReputationBonus() { return reputationBonus; }
    public boolean isOwned() { return isOwned; }
    public long getPurchaseTimestamp() { return purchaseTimestamp; }
    public Money getPurchasePrice() { return purchasePrice; }
    public String getPledgedLoanId() { return pledgedLoanId; }
    public boolean isPledged() { return pledgedLoanId != null; }
    public void pledgeToLoan(String loanId, String accountId) {
        if (loanId == null || loanId.trim().isEmpty() || !isOwned || !Objects.equals(ownerAccountId, accountId))
            throw new IllegalArgumentException("Agunan wajib dimiliki peminjam");
        if (pledgedLoanId != null) throw new IllegalStateException("Koleksi sudah dijaminkan");
        pledgedLoanId = loanId;
    }
    public void releaseLoanPledge(String loanId, Money remainingLiability) {
        if (!Objects.equals(pledgedLoanId, loanId) || remainingLiability == null || !remainingLiability.isZero())
            throw new IllegalStateException("Agunan hanya dilepas setelah seluruh kewajiban lunas");
        pledgedLoanId = null;
    }
    public String getOwnerAccountId() { return ownerAccountId; }
    public Money getMaintenanceArrears() { return maintenanceArrears; }
    public long getLastMaintenanceDay() { return lastMaintenanceDay; }
    public long getValuationAsOfDay() { return valuationAsOfDay; }
    void accrueMaintenance(long day) {
        if (day > lastMaintenanceDay) {
            long previous = lastMaintenanceDay == Long.MIN_VALUE
                    ? java.time.Instant.ofEpochMilli(purchaseTimestamp).atOffset(java.time.ZoneOffset.ofHours(7)).toLocalDate().toEpochDay() - 1
                    : lastMaintenanceDay;
            maintenanceArrears = maintenanceArrears.add(dailyMaintenanceCost.multiply(BigDecimal.valueOf(day - previous)));
            lastMaintenanceDay = day;
        }
    }
    void repayMaintenance(Money payment) { maintenanceArrears = maintenanceArrears.subtract(payment); }
    JSONObject getTransactionReceipt(String requestId) { return transactionReceipts.get(requestId); }
    void recordTransactionReceipt(String requestId, JSONObject receipt) throws JSONException {
        transactionReceipts.put(requestId, new JSONObject(receipt.toString()));
    }

    private static void validatePrice(Money price) {
        if (price == null || price.getCurrency() != Currency.IDR || !price.isPositive()
                || price.getAmount().compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0)
            throw new IllegalArgumentException("Valuasi wajib IDR positif dalam batas nominal");
    }

    public void setValuation(Money valuation) {
        validatePrice(valuation);
        this.currentValuation = valuation;
    }

    void updateCalendarValuation(long day) {
        if (valuationAnchorDay == Long.MIN_VALUE) {
            valuationAnchorDay = day; valuationAnchorPrice = currentValuation;
        }
        if (day < valuationAsOfDay) throw new IllegalArgumentException("Waktu valuasi tidak boleh mundur");
        long elapsed = day - valuationAnchorDay;
        BigDecimal factor;
        if (category == Category.VEHICLE) {
            factor = BigDecimal.ONE.subtract(BigDecimal.valueOf(Math.min(elapsed, 1000), 4)).max(new BigDecimal("0.90"));
        } else {
            int period = category == Category.REAL_ESTATE ? 30 : category == Category.JEWELRY ? 45 : 60;
            int amplitudeBps = category == Category.REAL_ESTATE ? 300 : category == Category.JEWELRY ? 500 : 600;
            // ponytail: an anchored category cycle is deterministic across offline partitions without random price state.
            long phase = Math.floorMod(elapsed, period * 2L);
            long triangle = phase <= period ? phase : period * 2L - phase;
            int direction = Math.floorMod(id.hashCode(), 2) == 0 ? 1 : -1;
            factor = BigDecimal.ONE.add(BigDecimal.valueOf(direction * amplitudeBps)
                    .multiply(BigDecimal.valueOf(triangle)).divide(BigDecimal.valueOf(period * 10_000L), 12, java.math.RoundingMode.HALF_UP));
        }
        currentValuation = valuationAnchorPrice.multiply(factor);
        valuationAsOfDay = day;
    }

    public void markPurchased(Money price, long timestamp) {
        markPurchased(price, timestamp, "ACC-PERSONAL-001");
    }

    public void markPurchased(Money price, long timestamp, String ownerAccountId) {
        validatePrice(price);
        if (ownerAccountId == null || ownerAccountId.trim().isEmpty()) throw new IllegalArgumentException("Pemilik koleksi wajib diisi");
        if (isOwned || isPledged()) throw new IllegalStateException("Koleksi sudah dimiliki/dijaminkan");
        this.isOwned = true;
        this.purchasePrice = price;
        this.purchaseTimestamp = timestamp;
        this.ownerAccountId = ownerAccountId;
        // Ownership gaps carry no upkeep; an already-settled purchase day is never charged twice.
        long purchaseDay = java.time.Instant.ofEpochMilli(timestamp).atOffset(java.time.ZoneOffset.ofHours(7)).toLocalDate().toEpochDay();
        lastMaintenanceDay = Math.max(lastMaintenanceDay, purchaseDay - 1);
    }

    public void markSold() {
        if (isPledged()) throw new IllegalStateException("Agunan pinjaman aktif tidak dapat dijual");
        this.isOwned = false;
        this.purchasePrice = null;
        this.purchaseTimestamp = 0L;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("name", name);
        obj.put("category", category.name());
        obj.put("categoryLabel", category.getLabel());
        obj.put("rarity", rarity.name());
        obj.put("basePrice", basePrice.getAmount().toPlainString());
        obj.put("currentValuation", currentValuation.getAmount().toPlainString());
        obj.put("dailyMaintenanceCost", dailyMaintenanceCost.getAmount().toPlainString());
        obj.put("reputationBonus", reputationBonus);
        obj.put("isOwned", isOwned);
        obj.put("owned", isOwned);
        obj.put("isPledged", isPledged());
        obj.put("pledgedLoanId", pledgedLoanId == null ? JSONObject.NULL : pledgedLoanId);
        obj.put("purchaseTimestamp", purchaseTimestamp);
        obj.put("purchasePrice", purchasePrice != null ? purchasePrice.getAmount().toPlainString() : JSONObject.NULL);
        obj.put("ownerAccountId", ownerAccountId == null ? JSONObject.NULL : ownerAccountId);
        obj.put("maintenanceArrears", maintenanceArrears.getAmount().toPlainString());
        obj.put("lastMaintenanceDay", lastMaintenanceDay);
        obj.put("valuationRule", valuationAnchorDay == Long.MIN_VALUE ? "LEGACY_FIXED_UNTIL_CALENDAR_SYNC" : "CATEGORY_CALENDAR_V1");
        obj.put("valuationAnchorDay", valuationAnchorDay);
        obj.put("valuationAsOfDay", valuationAsOfDay);
        obj.put("valuationAnchorPrice", valuationAnchorPrice == null ? JSONObject.NULL : valuationAnchorPrice.getAmount().toPlainString());
        obj.put("buySellSpreadBps", 0);
        obj.put("transactionReceipts", new JSONObject(transactionReceipts));
        return obj;
    }

    public static CollectibleItem fromJsonObject(JSONObject obj) throws JSONException {
        String id = obj.getString("id");
        String name = obj.getString("name");
        Category category = Category.valueOf(obj.getString("category"));
        Money basePrice = new Money(new BigDecimal(obj.getString("basePrice")), Currency.IDR);
        Money dailyCost = new Money(new BigDecimal(obj.getString("dailyMaintenanceCost")), Currency.IDR);
        int rep = obj.getInt("reputationBonus");

        CollectibleItem item = new CollectibleItem(id, name, category,
                Rarity.valueOf(obj.optString("rarity", "STANDARD")), basePrice, dailyCost, rep);
        if (obj.has("currentValuation")) {
            item.setValuation(new Money(new BigDecimal(obj.getString("currentValuation")), Currency.IDR));
        }
        if (obj.optBoolean("isOwned", false)) {
            Money pPrice = obj.isNull("purchasePrice") ? basePrice
                    : new Money(new BigDecimal(obj.getString("purchasePrice")), Currency.IDR);
            long ts = obj.optLong("purchaseTimestamp", System.currentTimeMillis());
            item.markPurchased(pPrice, ts, obj.optString("ownerAccountId", "ACC-PERSONAL-001"));
        }
        if (!obj.isNull("ownerAccountId")) item.ownerAccountId = obj.getString("ownerAccountId");
        item.maintenanceArrears = new Money(new BigDecimal(obj.optString("maintenanceArrears", "0")), Currency.IDR);
        item.lastMaintenanceDay = obj.optLong("lastMaintenanceDay", Long.MIN_VALUE);
        if (item.maintenanceArrears.isNegative() || (item.maintenanceArrears.isPositive()
                && (item.ownerAccountId == null || item.ownerAccountId.trim().isEmpty())))
            throw new IllegalArgumentException("Tunggakan koleksi tidak valid");
        if (!obj.isNull("pledgedLoanId")) {
            item.pledgedLoanId = obj.getString("pledgedLoanId");
            if (item.pledgedLoanId.trim().isEmpty() || !item.isOwned) throw new IllegalArgumentException("Agunan koleksi tidak sah");
        }
        JSONObject receipts = obj.optJSONObject("transactionReceipts");
        if (receipts != null) {
            Iterator<String> keys = receipts.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject receipt = receipts.getJSONObject(key);
                if (key.trim().isEmpty() || key.length() > 160 || !key.equals(receipt.getString("requestId"))
                        || !id.equals(receipt.getString("itemId")) || !receipt.getBoolean("success")
                        || !(receipt.getString("action").equals("BUY") || receipt.getString("action").equals("SELL"))
                        || receipt.getString("accountId").trim().isEmpty())
                    throw new IllegalArgumentException("Receipt koleksi tidak valid");
                item.recordTransactionReceipt(key, receipt);
            }
        }
        item.valuationAnchorDay = obj.optLong("valuationAnchorDay", Long.MIN_VALUE);
        item.valuationAsOfDay = obj.optLong("valuationAsOfDay", Long.MIN_VALUE);
        if (item.valuationAnchorDay != Long.MIN_VALUE) {
            item.valuationAnchorPrice = new Money(new BigDecimal(obj.getString("valuationAnchorPrice")), Currency.IDR);
            validatePrice(item.valuationAnchorPrice);
            if (item.valuationAsOfDay < item.valuationAnchorDay) throw new IllegalArgumentException("Anchor valuasi tidak valid");
            Money expected = item.currentValuation;
            item.updateCalendarValuation(item.valuationAsOfDay);
            if (!expected.equals(item.currentValuation)) throw new IllegalArgumentException("Valuasi berbeda dari aturan tersimpan");
        } else if (item.valuationAsOfDay != Long.MIN_VALUE || !obj.isNull("valuationAnchorPrice"))
            throw new IllegalArgumentException("Anchor valuasi tidak lengkap");
        return item;
    }
}
