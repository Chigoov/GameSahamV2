package com.tradingglobal3d.core.market;

import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Layanan Status Dominasi & Pengaruh Sektor Bursa (Fase 3 Fitur 9).
 * Memberikan titel Penguasa Sektor dan diskon komisi transaksi 25%
 * bila kepemilikan saham pemain melebihi 25% total kapitalisasi sektor.
 */
public final class SectorDominanceService {

    public static final class SectorStatus {
        private final String sectorName;
        private final double playerOwnershipPct;
        private final boolean isDominant;
        private final boolean vetoPowerActive;
        private final double feeDiscountPct;

        public SectorStatus(String sectorName, double playerOwnershipPct, boolean isDominant,
                            boolean vetoPowerActive, double feeDiscountPct) {
            this.sectorName = Objects.requireNonNull(sectorName);
            this.playerOwnershipPct = playerOwnershipPct;
            this.isDominant = isDominant;
            this.vetoPowerActive = vetoPowerActive;
            this.feeDiscountPct = feeDiscountPct;
        }

        public String getSectorName() { return sectorName; }
        public double getPlayerOwnershipPct() { return playerOwnershipPct; }
        public boolean isDominant() { return isDominant; }
        public boolean isVetoPowerActive() { return vetoPowerActive; }
        public double getFeeDiscountPct() { return feeDiscountPct; }

        public JSONObject toJsonObject() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("sectorName", sectorName);
            obj.put("playerOwnershipPct", String.format(Locale.ROOT, "%.2f%%", playerOwnershipPct));
            obj.put("isDominant", isDominant);
            obj.put("vetoPowerActive", vetoPowerActive);
            obj.put("feeDiscountPct", String.format(Locale.ROOT, "%.0f%%", feeDiscountPct));
            obj.put("title", isDominant ? "PENGUASA SEKTOR (" + sectorName + ")" : "INVESTOR REGULER");
            return obj;
        }
    }

    public synchronized Map<String, SectorStatus> evaluateDominance(Account personal, InstrumentCatalog catalog, BigDecimal usdRate) {
        Map<String, SectorStatus> results = new LinkedHashMap<>();
        if (personal == null || catalog == null) return results;

        // Hitung total valuasi saham per sektor
        Map<String, BigDecimal> sectorTotalCap = new HashMap<>();
        Map<String, BigDecimal> playerSectorVal = new HashMap<>();

        for (Instrument inst : catalog.getActiveUniverse()) {
            String sec = inst.getSector() != null ? inst.getSector() : "Umum";
            BigDecimal fx = inst.getCurrency() == Currency.USD && usdRate != null ? usdRate : BigDecimal.ONE;
            BigDecimal cap = inst.getCurrentPrice().getAmount().multiply(BigDecimal.valueOf(inst.getSharesOutstanding())).multiply(fx);
            sectorTotalCap.merge(sec, cap, BigDecimal::add);

            Position pos = personal.getPosition(inst.getId());
            if (pos == null) pos = personal.getPosition(inst.getTicker());
            if (pos != null && pos.getQuantity() > 0) {
                BigDecimal val = inst.getCurrentPrice().getAmount().multiply(BigDecimal.valueOf(pos.getQuantity())).multiply(fx);
                playerSectorVal.merge(sec, val, BigDecimal::add);
            }
        }

        for (Map.Entry<String, BigDecimal> entry : sectorTotalCap.entrySet()) {
            String sec = entry.getKey();
            BigDecimal totalCap = entry.getValue();
            BigDecimal playerVal = playerSectorVal.getOrDefault(sec, BigDecimal.ZERO);

            double pct = totalCap.signum() > 0
                    ? playerVal.multiply(BigDecimal.valueOf(100)).divide(totalCap, 4, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            boolean isDominant = pct >= 25.0; // Ambang penguasa sektor 25%
            results.put(sec, new SectorStatus(sec, pct, isDominant, isDominant, isDominant ? 25.0 : 0.0));
        }

        return results;
    }

    public synchronized JSONArray exportDominance(Account personal, InstrumentCatalog catalog, BigDecimal usdRate) throws JSONException {
        JSONArray arr = new JSONArray();
        for (SectorStatus s : evaluateDominance(personal, catalog, usdRate).values()) {
            arr.put(s.toJsonObject());
        }
        return arr;
    }
}
