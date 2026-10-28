package com.tradingglobal3d.core.research;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.*;

/**
 * Layanan Pohon Riset Bisnis & Teknologi Finansial Tech Tree (Fase 3 Fitur 7).
 * Mengelola 3 jalur R&D: Algoritma Pasar, Efisiensi Biaya, dan Sindikasi Skala AUM.
 */
public final class ResearchTechTreeService {

    public static final class TechNode {
        private final String nodeId;
        private final String nodeName;
        private final String category; // ALGO, EFFICIENCY, SCALE
        private final int tierLevel;
        private final Money upgradeCost;
        private boolean unlocked;
        private final String statBonusType;
        private final double statBonusValue;

        public TechNode(String nodeId, String nodeName, String category, int tierLevel,
                        Money upgradeCost, boolean unlocked, String statBonusType, double statBonusValue) {
            this.nodeId = Objects.requireNonNull(nodeId);
            this.nodeName = Objects.requireNonNull(nodeName);
            this.category = Objects.requireNonNull(category);
            this.tierLevel = tierLevel;
            this.upgradeCost = Objects.requireNonNull(upgradeCost);
            this.unlocked = unlocked;
            this.statBonusType = Objects.requireNonNull(statBonusType);
            this.statBonusValue = statBonusValue;
        }

        public String getNodeId() { return nodeId; }
        public String getNodeName() { return nodeName; }
        public String getCategory() { return category; }
        public int getTierLevel() { return tierLevel; }
        public Money getUpgradeCost() { return upgradeCost; }
        public boolean isUnlocked() { return unlocked; }
        public String getStatBonusType() { return statBonusType; }
        public double getStatBonusValue() { return statBonusValue; }

        public void unlock() {
            this.unlocked = true;
        }

        private void lock() {
            this.unlocked = false;
        }

        public JSONObject toJsonObject() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("nodeId", nodeId);
            obj.put("nodeName", nodeName);
            obj.put("category", category);
            obj.put("tierLevel", tierLevel);
            obj.put("upgradeCost", upgradeCost.format());
            obj.put("isUnlocked", unlocked);
            obj.put("statBonusType", statBonusType);
            obj.put("statBonusValue", statBonusValue);
            obj.put("statusLabel", unlocked ? "TERBUKA" : "TERKUNCI");
            return obj;
        }
    }

    private final Map<String, TechNode> nodes = new LinkedHashMap<>();

    public ResearchTechTreeService() {
        initializeTechTree();
    }

    private void initializeTechTree() {
        nodes.clear();
        nodes.put("TECH-ALGO-1", new TechNode("TECH-ALGO-1", "Sinyal Algoritma Sektor", "ALGO", 1, Money.of(10_000_000, Currency.IDR), true, "SIGNAL_ACCURACY", 10.0));
        nodes.put("TECH-ALGO-2", new TechNode("TECH-ALGO-2", "Prediksi Volatilitas AI", "ALGO", 2, Money.of(25_000_000, Currency.IDR), false, "SIGNAL_ACCURACY", 25.0));
        nodes.put("TECH-EFF-1", new TechNode("TECH-EFF-1", "Automasi Eksekusi Order", "EFFICIENCY", 1, Money.of(10_000_000, Currency.IDR), false, "BROKER_FEE_REDUCTION", 15.0));
        nodes.put("TECH-SCALE-1", new TechNode("TECH-SCALE-1", "Sindikasi Mandat AUM Elit", "SCALE", 1, Money.of(20_000_000, Currency.IDR), false, "AUM_CAPACITY_BOOST", 50.0));
    }

    public synchronized void reset() {
        initializeTechTree();
    }

    public List<TechNode> getAllNodes() {
        return new ArrayList<>(nodes.values());
    }

    public TechNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public synchronized TechNode upgradeNode(String nodeId, Account account, EconomyLedger ledger, long now) {
        TechNode node = nodes.get(nodeId);
        if (node == null) throw new IllegalArgumentException("Node riset tidak ditemukan: " + nodeId);
        if (node.isUnlocked()) return node;

        Money cost = node.getUpgradeCost();
        Money avail = account.getAvailableCash(Currency.IDR);
        if (avail.isLessThan(cost)) {
            throw new IllegalStateException("Saldo kas IDR tidak mencukupi untuk riset " + node.getNodeName() + " (" + cost.format() + ")");
        }

        ledger.recordMutation(
                "TECH-" + node.getNodeId() + "-" + System.currentTimeMillis(),
                account,
                LedgerEntryType.TRANSACTION_FEE,
                cost,
                false, // debit
                now,
                "Upgrade Pohon Riset Finansial: " + node.getNodeName()
        );

        node.unlock();
        return node;
    }

    public synchronized double getUnlockedBonus(String statBonusType) {
        double total = 0.0;
        for (TechNode node : nodes.values())
            if (node.isUnlocked() && node.getStatBonusType().equals(statBonusType)) total += node.getStatBonusValue();
        return total;
    }

    public synchronized JSONArray exportTree() throws JSONException {
        JSONArray arr = new JSONArray();
        for (TechNode n : nodes.values()) {
            arr.put(n.toJsonObject());
        }
        return arr;
    }

    public synchronized JSONArray exportState() throws JSONException {
        JSONArray state = new JSONArray();
        for (TechNode node : nodes.values()) state.put(new JSONObject()
                .put("nodeId", node.getNodeId()).put("isUnlocked", node.isUnlocked()));
        return state;
    }

    public synchronized void restoreState(JSONArray state) throws JSONException {
        Map<String, Boolean> unlockedById = new HashMap<>();
        for (int i = 0; i < state.length(); i++) {
            JSONObject item = state.getJSONObject(i);
            String id = item.getString("nodeId");
            if (!nodes.containsKey(id) || unlockedById.put(id, item.getBoolean("isUnlocked")) != null)
                throw new IllegalArgumentException("State pohon riset tidak dikenal atau duplikat");
        }
        for (Map.Entry<String, Boolean> item : unlockedById.entrySet()) {
            TechNode node = nodes.get(item.getKey());
            if (item.getValue()) node.unlock();
            else if ("TECH-ALGO-1".equals(item.getKey())) throw new IllegalArgumentException("Node riset dasar harus terbuka");
            else node.lock();
        }
    }
}
