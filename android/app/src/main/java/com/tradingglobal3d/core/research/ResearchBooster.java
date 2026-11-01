package com.tradingglobal3d.core.research;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entitas Kartu Booster Riset R&D & Efisiensi Sementara (Fitur 7 Ekspansi).
 */
public final class ResearchBooster implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum BoosterType {
        NEWS_ACCURACY("Bocoran Riset Analis", "Akurasi sinyal berita meningkat 30%"),
        FEE_DISCOUNT("Koneksi Khusus Broker", "Bebas komisi transaksi"),
        MARGIN_EXPAND("Suntikan Likuiditas Cepat", "Plafon pinjaman bank meningkat 50%");

        private final String title;
        private final String benefit;

        BoosterType(String title, String benefit) {
            this.title = title;
            this.benefit = benefit;
        }

        public String getTitle() { return title; }
        public String getBenefit() { return benefit; }
    }

    private final String boosterId;
    private final BoosterType type;
    private final String title;
    private final String description;
    private int remainingUses;
    private boolean active;
    private final double multiplierValue;

    public ResearchBooster(String boosterId, BoosterType type, String title, String description,
                           int remainingUses, double multiplierValue) {
        this.boosterId = Objects.requireNonNull(boosterId);
        this.type = Objects.requireNonNull(type);
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.remainingUses = Math.max(0, remainingUses);
        this.active = false;
        this.multiplierValue = multiplierValue;
    }

    public String getBoosterId() { return boosterId; }
    public BoosterType getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getRemainingUses() { return remainingUses; }
    public boolean isActive() { return active; }
    public double getMultiplierValue() { return multiplierValue; }

    public void activate() {
        if (remainingUses > 0) {
            this.active = true;
        }
    }

    public void consumeUse() {
        if (active && remainingUses > 0) {
            remainingUses--;
            if (remainingUses <= 0) {
                active = false;
            }
        }
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("boosterId", boosterId);
        obj.put("type", type.name());
        obj.put("title", title);
        obj.put("description", description);
        obj.put("remainingUses", remainingUses);
        obj.put("isActive", active);
        obj.put("multiplierValue", multiplierValue);
        obj.put("statusLabel", active ? "AKTIF" : (remainingUses > 0 ? "SIAP DIGUNAKAN" : "HABIS"));
        return obj;
    }
}
