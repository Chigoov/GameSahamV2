package com.tradingglobal3d.core.collection;

import com.tradingglobal3d.core.career.CareerManager;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
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
        java.io.File jsonFile = new java.io.File("src/main/assets/data/katalog_koleksi_150.json");
        if (!jsonFile.exists()) {
            jsonFile = new java.io.File("android/app/src/main/assets/data/katalog_koleksi_150.json");
        }
        if (jsonFile.exists()) {
            try {
                byte[] bytes = java.nio.file.Files.readAllBytes(jsonFile.toPath());
                String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                JSONArray arr = new JSONArray(content);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String id = obj.getString("id");
                    String name = obj.getString("name");
                    CollectibleItem.Category cat = CollectibleItem.Category.fromCode(obj.getString("category"));
                    CollectibleItem.Rarity rar = CollectibleItem.Rarity.fromCode(obj.getString("rarity"));
                    long basePrice = obj.getLong("basePrice");
                    long maintenance = obj.getLong("dailyMaintenance");
                    int rep = obj.getInt("reputationBonus");
                    addDefault(new CollectibleItem(
                            id, name, cat, rar,
                            Money.of(basePrice, Currency.IDR),
                            Money.of(maintenance, Currency.IDR),
                            rep
                    ));
                }
                if (catalog.size() >= 150) return;
            } catch (Exception ignored) {}
        }
        // 1. KENDARAAN (VEHICLE)
        addDefault(new CollectibleItem(
                "COL-CAR-00", "Vespa Klasik 1965 Restorasi",
                CollectibleItem.Category.VEHICLE, CollectibleItem.Rarity.STANDARD,
                Money.of(25_000_000L, Currency.IDR), Money.of(20_000, Currency.IDR), 5
        ));
        addDefault(new CollectibleItem(
                "COL-CAR-01", "Porsche 911 GT3 RS",
                CollectibleItem.Category.VEHICLE, CollectibleItem.Rarity.RARE,
                Money.of(4_500_000_000L, Currency.IDR), Money.of(500_000, Currency.IDR), 15
        ));
        addDefault(new CollectibleItem(
                "COL-CAR-02", "Ferrari 296 GTB",
                CollectibleItem.Category.VEHICLE, CollectibleItem.Rarity.LEGENDARY,
                Money.of(8_000_000_000L, Currency.IDR), Money.of(1_000_000, Currency.IDR), 25
        ));

        // 2. PROPERTI (REAL ESTATE)
        addDefault(new CollectibleItem(
                "COL-PROP-00", "Ruko Komersial BSD City",
                CollectibleItem.Category.REAL_ESTATE, CollectibleItem.Rarity.STANDARD,
                Money.of(45_000_000L, Currency.IDR), Money.of(30_000, Currency.IDR), 8
        ));
        addDefault(new CollectibleItem(
                "COL-PROP-01", "Penthouse SCBD Mega Kuningan",
                CollectibleItem.Category.REAL_ESTATE, CollectibleItem.Rarity.RARE,
                Money.of(15_000_000_000L, Currency.IDR), Money.of(1_500_000, Currency.IDR), 30
        ));
        addDefault(new CollectibleItem(
                "COL-PROP-02", "Villa Tebing Uluwatu Bali",
                CollectibleItem.Category.REAL_ESTATE, CollectibleItem.Rarity.LEGENDARY,
                Money.of(25_000_000_000L, Currency.IDR), Money.of(2_500_000, Currency.IDR), 50
        ));

        // 3. PERMATA (JEWELRY)
        addDefault(new CollectibleItem(
                "COL-JEW-01", "Cincin Safir Biru Ceylon",
                CollectibleItem.Category.JEWELRY, CollectibleItem.Rarity.STANDARD,
                Money.of(35_000_000L, Currency.IDR), Money.of(15_000, Currency.IDR), 6
        ));
        addDefault(new CollectibleItem(
                "COL-JEW-02", "Kalung Berlian Solitaire Tiffany",
                CollectibleItem.Category.JEWELRY, CollectibleItem.Rarity.RARE,
                Money.of(2_500_000_000L, Currency.IDR), Money.of(100_000, Currency.IDR), 18
        ));
        addDefault(new CollectibleItem(
                "COL-JEW-03", "Tiara Berlian Kerajaan Langka",
                CollectibleItem.Category.JEWELRY, CollectibleItem.Rarity.LEGENDARY,
                Money.of(18_000_000_000L, Currency.IDR), Money.of(800_000, Currency.IDR), 40
        ));

        // 4. SENI & JAM TANGAN (ART_AND_WATCHES)
        addDefault(new CollectibleItem(
                "COL-ART-00", "Rolex Submariner Vintage 1970",
                CollectibleItem.Category.ART_AND_WATCHES, CollectibleItem.Rarity.STANDARD,
                Money.of(15_000_000L, Currency.IDR), Money.of(10_000, Currency.IDR), 4
        ));
        addDefault(new CollectibleItem(
                "COL-ART-01", "Patek Philippe Grandmaster Chime",
                CollectibleItem.Category.ART_AND_WATCHES, CollectibleItem.Rarity.RARE,
                Money.of(5_000_000_000L, Currency.IDR), Money.of(200_000, Currency.IDR), 20
        ));
        addDefault(new CollectibleItem(
                "COL-ART-02", "Lukisan Bersejarah Raden Saleh",
                CollectibleItem.Category.ART_AND_WATCHES, CollectibleItem.Rarity.LEGENDARY,
                Money.of(12_000_000_000L, Currency.IDR), Money.of(500_000, Currency.IDR), 35
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

    public synchronized void updateValuations(long timestamp) {
        if (timestamp < 0) throw new IllegalArgumentException("Waktu valuasi tidak valid");
        long day = java.time.Instant.ofEpochMilli(timestamp).atOffset(java.time.ZoneOffset.ofHours(7)).toLocalDate().toEpochDay();
        // Validate the full catalogue before changing any item.
        for (CollectibleItem item : catalog.values())
            if (day < item.getValuationAsOfDay())
                throw new IllegalArgumentException("Waktu valuasi tidak boleh mundur");
        for (CollectibleItem item : catalog.values()) item.updateCalendarValuation(day);
    }

    public synchronized CollectibleItem buyItem(
            String itemId,
            Account personalAccount,
            EconomyLedger ledger,
            CareerManager careerManager,
            long timestamp) {
        try { buyItem(UUID.randomUUID().toString(), itemId, personalAccount, ledger, careerManager, timestamp); }
        catch (JSONException failure) { throw new IllegalStateException("Receipt koleksi gagal dibuat", failure); }
        return catalog.get(itemId);
    }

    public synchronized CollectibleItem sellItem(
            String itemId,
            Account personalAccount,
            EconomyLedger ledger,
            CareerManager careerManager,
            long timestamp) {
        try { sellItem(UUID.randomUUID().toString(), itemId, personalAccount, ledger, careerManager, timestamp); }
        catch (JSONException failure) { throw new IllegalStateException("Receipt koleksi gagal dibuat", failure); }
        return catalog.get(itemId);
    }

    public synchronized JSONObject buyItem(String requestId, String itemId, Account account,
            EconomyLedger ledger, CareerManager career, long timestamp) throws JSONException {
        return trade(requestId, itemId, account, ledger, career, timestamp, true);
    }

    public synchronized JSONObject sellItem(String requestId, String itemId, Account account,
            EconomyLedger ledger, CareerManager career, long timestamp) throws JSONException {
        return trade(requestId, itemId, account, ledger, career, timestamp, false);
    }

    private void requirePersonal(Account account) {
        if (account == null || account.getType() != AccountType.PERSONAL)
            throw new IllegalArgumentException("Koleksi hanya untuk akun pribadi");
    }

    private JSONObject trade(String requestId, String itemId, Account account, EconomyLedger ledger,
            CareerManager career, long timestamp, boolean buy) throws JSONException {
        requirePersonal(account);
        Objects.requireNonNull(ledger);
        if (requestId == null || requestId.trim().isEmpty() || requestId.length() > 160)
            throw new IllegalArgumentException("ID transaksi koleksi tidak valid");
        String action = buy ? "BUY" : "SELL";
        for (CollectibleItem candidate : catalog.values()) {
            JSONObject receipt = candidate.getTransactionReceipt(requestId);
            if (receipt != null) {
                if (!itemId.equals(receipt.getString("itemId")) || !action.equals(receipt.getString("action"))
                        || !account.getId().equals(receipt.getString("accountId")))
                    throw new IllegalArgumentException("ID transaksi sudah digunakan untuk input berbeda");
                return new JSONObject(receipt.toString());
            }
        }
        CollectibleItem item = catalog.get(itemId);
        if (item == null) throw new IllegalArgumentException("Barang koleksi tidak ditemukan");
        if (buy && item.isOwned()) throw new IllegalStateException("Barang koleksi sudah dimiliki");
        if (!buy && (!item.isOwned() || !account.getId().equals(item.getOwnerAccountId())))
            throw new IllegalStateException("Barang koleksi bukan milik akun");
        if (buy && item.getMaintenanceArrears().isPositive() && !account.getId().equals(item.getOwnerAccountId()))
            throw new IllegalStateException("Tunggakan pemilik sebelumnya belum diselesaikan");
        if (!buy && item.isPledged()) throw new IllegalStateException("Koleksi terikat agunan pinjaman aktif");
        Money value = item.getCurrentValuation();
        if (buy && account.getAvailableCash(Currency.IDR).isLessThan(value))
            throw new IllegalStateException("Kas pribadi tidak mencukupi untuk membeli koleksi");
        JSONObject receipt = new JSONObject().put("success", true).put("status", "SETTLED")
                .put("requestId", requestId).put("receiptId", "COLLECTION:" + requestId).put("itemId", itemId)
                .put("action", action).put("accountId", account.getId()).put("amount", value.getAmount().toPlainString())
                .put("currency", "IDR").put("timestamp", timestamp)
                .put("valuationRule", item.toJsonObject().getString("valuationRule")).put("buySellSpreadBps", 0);
        ledger.recordMutation("COLLECTION:" + requestId, account,
                buy ? LedgerEntryType.COLLECTION_PURCHASE : LedgerEntryType.COLLECTION_SALE,
                value, !buy, timestamp, (buy ? "Pembelian " : "Penjualan ") + item.getName());
        if (buy) item.markPurchased(value, timestamp, account.getId()); else item.markSold();
        if (career != null) {
            if (buy) career.boostReputation(item.getReputationBonus());
            else career.penalizeReputation(item.getReputationBonus());
        }
        item.recordTransactionReceipt(requestId, receipt);
        return receipt;
    }

    public synchronized void processDailyMaintenance(Account personalAccount, EconomyLedger ledger, long timestamp) {
        requirePersonal(personalAccount);
        Objects.requireNonNull(ledger);
        long day = java.time.Instant.ofEpochMilli(timestamp).atOffset(java.time.ZoneOffset.ofHours(7)).toLocalDate().toEpochDay();
        for (CollectibleItem item : catalog.values()) {
            if (!personalAccount.getId().equals(item.getOwnerAccountId())) continue;
            if (item.isOwned() && timestamp >= item.getPurchaseTimestamp()) item.accrueMaintenance(day);
            Money due = item.getMaintenanceArrears();
            Money available = personalAccount.getAvailableCash(Currency.IDR);
            Money payment = due.isLessThan(available) ? due : available;
            if (payment.isPositive()) {
                ledger.recordMutation("COL-MAINT:" + item.getId() + ":" + day + ":" + ledger.getEntries().size(),
                        personalAccount, LedgerEntryType.COLLECTION_MAINTENANCE, payment, false, timestamp,
                        "Biaya dan tunggakan pemeliharaan " + item.getName());
                item.repayMaintenance(payment);
            }
        }
    }

    public synchronized Money getTotalMaintenanceLiability() {
        Money total = Money.zero(Currency.IDR);
        for (CollectibleItem item : catalog.values()) total = total.add(item.getMaintenanceArrears());
        return total;
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
        CollectionManager staged = new CollectionManager();
        Set<String> seen = new HashSet<>(), requests = new HashSet<>();
        for (int i = 0; arr != null && i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String id = obj.getString("id");
            CollectibleItem canonical = staged.catalog.get(id);
            if (canonical == null || !seen.add(id)) throw new IllegalArgumentException("Katalog koleksi tidak dikenal atau duplikat");
            // ponytail: canonical catalogue metadata stays in source; legacy six-item saves add no invented items.
            JSONObject restored = canonical.toJsonObject();
            for (String field : Arrays.asList("currentValuation", "isOwned", "purchasePrice", "purchaseTimestamp",
                    "ownerAccountId", "pledgedLoanId", "maintenanceArrears", "lastMaintenanceDay", "transactionReceipts",
                    "valuationAnchorDay", "valuationAsOfDay", "valuationAnchorPrice"))
                if (obj.has(field)) restored.put(field, obj.get(field));
            CollectibleItem item = CollectibleItem.fromJsonObject(restored);
            JSONObject receipts = restored.optJSONObject("transactionReceipts");
            if (receipts != null) {
                Iterator<String> keys = receipts.keys();
                while (keys.hasNext()) if (!requests.add(keys.next())) throw new IllegalArgumentException("ID receipt koleksi duplikat");
            }
            staged.catalog.put(id, item);
        }
        catalog.clear(); catalog.putAll(staged.catalog);
    }
}
