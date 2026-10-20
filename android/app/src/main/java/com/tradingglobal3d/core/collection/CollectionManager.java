package com.tradingglobal3d.core.collection;

import com.tradingglobal3d.core.career.CareerManager;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Pengelola Portofolio Koleksi Mewah, Properti, dan Kendaraan.
 * Menegakkan transaksi pembelian, penjualan dengan valuasi terkini,
 * dan biaya pemeliharaan harian (maintenance cost) melalui ledger resmi.
 */
public final class CollectionManager {
    private final Map<String, CollectibleItem> catalog = new LinkedHashMap<>();

    public CollectionManager() {
        initDefaultCatalog();
    }

    private void initDefaultCatalog() {
        catalog.clear();
        addDefault(new CollectibleItem(
                "COL-CAR-01", "Porsche 911 GT3 RS",
                CollectibleItem.Category.VEHICLE,
                Money.of(4_500_000_000L, Currency.IDR),
                Money.of(500_000, Currency.IDR), 10
        ));
        addDefault(new CollectibleItem(
                "COL-CAR-02", "Ferrari 296 GTB",
                CollectibleItem.Category.VEHICLE,
                Money.of(8_000_000_000L, Currency.IDR),
                Money.of(1_000_000, Currency.IDR), 15
        ));
        addDefault(new CollectibleItem(
                "COL-PROP-01", "Penthouse SCBD Mega Kuningan",
                CollectibleItem.Category.REAL_ESTATE,
                Money.of(15_000_000_000L, Currency.IDR),
                Money.of(1_500_000, Currency.IDR), 25
        ));
        addDefault(new CollectibleItem(
                "COL-PROP-02", "Villa Tebing Uluwatu Bali",
                CollectibleItem.Category.REAL_ESTATE,
                Money.of(25_000_000_000L, Currency.IDR),
                Money.of(2_500_000, Currency.IDR), 35
        ));
        addDefault(new CollectibleItem(
                "COL-ART-01", "Patek Philippe Grandmaster Chime",
                CollectibleItem.Category.ART_AND_WATCHES,
                Money.of(5_000_000_000L, Currency.IDR),
                Money.of(200_000, Currency.IDR), 15
        ));
        addDefault(new CollectibleItem(
                "COL-ART-02", "Lukisan Bersejarah Raden Saleh",
                CollectibleItem.Category.ART_AND_WATCHES,
                Money.of(12_000_000_000L, Currency.IDR),
                Money.of(500_000, Currency.IDR), 20
        ));
    }

    private void addDefault(CollectibleItem item) {
        catalog.put(item.getId(), item);
    }

    public Collection<CollectibleItem> getAllItems() {
        return Collections.unmodifiableCollection(catalog.values());
    }

    public CollectibleItem getItem(String itemId) {
        return catalog.get(itemId);
    }

    public synchronized void reset() {
        initDefaultCatalog();
    }

    public synchronized CollectibleItem buyItem(
            String itemId,
            Account personalAccount,
            EconomyLedger ledger,
            CareerManager careerManager,
            long timestamp) {

        CollectibleItem item = catalog.get(itemId);
        if (item == null) throw new IllegalArgumentException("Barang koleksi tidak ditemukan: " + itemId);
        if (item.isOwned()) throw new IllegalStateException("Barang koleksi sudah dimiliki");

        Money price = item.getCurrentValuation();
        if (personalAccount.getAvailableCash(Currency.IDR).isLessThan(price)) {
            throw new IllegalStateException("Kas pribadi tidak mencukupi untuk membeli koleksi " + item.getName());
        }

        ledger.recordMutation(
                "COL-BUY-" + itemId + "-" + timestamp,
                personalAccount,
                LedgerEntryType.COLLECTION_PURCHASE,
                price,
                false,
                timestamp,
                "Pembelian koleksi " + item.getName()
        );

        item.markPurchased(price, timestamp);
        if (careerManager != null) {
            // Memberi reputasi sosial
            careerManager.boostReputation(item.getReputationBonus());
        }
        return item;
    }

    public synchronized CollectibleItem sellItem(
            String itemId,
            Account personalAccount,
            EconomyLedger ledger,
            CareerManager careerManager,
            long timestamp) {

        CollectibleItem item = catalog.get(itemId);
        if (item == null) throw new IllegalArgumentException("Barang koleksi tidak ditemukan: " + itemId);
        if (!item.isOwned()) throw new IllegalStateException("Barang koleksi belum dimiliki");

        Money valuation = item.getCurrentValuation();
        ledger.recordMutation(
                "COL-SELL-" + itemId + "-" + timestamp,
                personalAccount,
                LedgerEntryType.COLLECTION_SALE,
                valuation,
                true,
                timestamp,
                "Penjualan koleksi " + item.getName()
        );

        item.markSold();
        if (careerManager != null) {
            careerManager.penalizeReputation(item.getReputationBonus());
        }
        return item;
    }

    public synchronized void processDailyMaintenance(Account personalAccount, EconomyLedger ledger, long timestamp) {
        Money totalCost = calculateDailyMaintenance();
        if (totalCost.isPositive() && personalAccount.getAvailableCash(Currency.IDR).isGreaterThanOrEqual(totalCost)) {
            ledger.recordMutation(
                    "COL-MAINT-" + timestamp,
                    personalAccount,
                    LedgerEntryType.COLLECTION_MAINTENANCE,
                    totalCost,
                    false,
                    timestamp,
                    "Biaya pemeliharaan aset koleksi dan properti"
            );
        }
    }

    public Money calculateTotalValuation() {
        Money total = Money.zero(Currency.IDR);
        for (CollectibleItem item : catalog.values()) {
            if (item.isOwned()) {
                total = total.add(item.getCurrentValuation());
            }
        }
        return total;
    }

    public Money calculateDailyMaintenance() {
        Money total = Money.zero(Currency.IDR);
        for (CollectibleItem item : catalog.values()) {
            if (item.isOwned()) {
                total = total.add(item.getDailyMaintenanceCost());
            }
        }
        return total;
    }

    public JSONArray exportState() throws JSONException {
        JSONArray arr = new JSONArray();
        for (CollectibleItem item : catalog.values()) {
            arr.put(item.toJsonObject());
        }
        return arr;
    }

    public void restoreState(JSONArray arr) throws JSONException {
        if (arr == null) return;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String id = obj.getString("id");
            CollectibleItem existing = catalog.get(id);
            if (existing != null) {
                if (obj.optBoolean("isOwned", false)) {
                    Money p = obj.isNull("purchasePrice") ? existing.getBasePrice()
                            : new Money(new java.math.BigDecimal(obj.getString("purchasePrice")), Currency.IDR);
                    existing.markPurchased(p, obj.optLong("purchaseTimestamp", System.currentTimeMillis()));
                } else {
                    existing.markSold();
                }
            } else {
                catalog.put(id, CollectibleItem.fromJsonObject(obj));
            }
        }
    }
}
