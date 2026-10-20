package com.tradingglobal3d.core.collection;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Objects;

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

    public void setValuation(Money valuation) {
        this.currentValuation = Objects.requireNonNull(valuation);
    }

    public void markPurchased(Money price, long timestamp) {
        this.isOwned = true;
        this.purchasePrice = Objects.requireNonNull(price);
        this.purchaseTimestamp = timestamp;
    }

    public void markSold() {
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
        obj.put("basePrice", basePrice.getAmount().toPlainString());
        obj.put("currentValuation", currentValuation.getAmount().toPlainString());
        obj.put("dailyMaintenanceCost", dailyMaintenanceCost.getAmount().toPlainString());
        obj.put("reputationBonus", reputationBonus);
        obj.put("isOwned", isOwned);
        obj.put("purchaseTimestamp", purchaseTimestamp);
        obj.put("purchasePrice", purchasePrice != null ? purchasePrice.getAmount().toPlainString() : JSONObject.NULL);
        return obj;
    }

    public static CollectibleItem fromJsonObject(JSONObject obj) throws JSONException {
        String id = obj.getString("id");
        String name = obj.getString("name");
        Category category = Category.valueOf(obj.getString("category"));
        Money basePrice = new Money(new BigDecimal(obj.getString("basePrice")), Currency.IDR);
        Money dailyCost = new Money(new BigDecimal(obj.getString("dailyMaintenanceCost")), Currency.IDR);
        int rep = obj.getInt("reputationBonus");

        CollectibleItem item = new CollectibleItem(id, name, category, basePrice, dailyCost, rep);
        if (obj.has("currentValuation")) {
            item.setValuation(new Money(new BigDecimal(obj.getString("currentValuation")), Currency.IDR));
        }
        if (obj.optBoolean("isOwned", false)) {
            Money pPrice = obj.isNull("purchasePrice") ? basePrice
                    : new Money(new BigDecimal(obj.getString("purchasePrice")), Currency.IDR);
            long ts = obj.optLong("purchaseTimestamp", System.currentTimeMillis());
            item.markPurchased(pPrice, ts);
        }
        return item;
    }
}
