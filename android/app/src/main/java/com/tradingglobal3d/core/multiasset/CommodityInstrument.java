package com.tradingglobal3d.core.multiasset;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;

import java.util.Objects;

/**
 * Instrumen Komoditas Nyata (contoh: Emas Spot Fisik Antam).
 */
public final class CommodityInstrument {
    private final String id;
    private final String code;
    private final String name;
    private final String unitOfMeasure; // e.g. "Gram"
    private final Currency currency;
    private Money spotPricePerUnit;     // e.g. Rp 1.450.000 / gram

    public CommodityInstrument(String id, String code, String name, String unitOfMeasure,
                               Currency currency, Money initialSpotPrice) {
        this.id = Objects.requireNonNull(id);
        this.code = Objects.requireNonNull(code);
        this.name = Objects.requireNonNull(name);
        this.unitOfMeasure = Objects.requireNonNull(unitOfMeasure);
        this.currency = Objects.requireNonNull(currency);
        this.spotPricePerUnit = Objects.requireNonNull(initialSpotPrice);
    }

    public String getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public Currency getCurrency() { return currency; }
    public Money getSpotPricePerUnit() { return spotPricePerUnit; }

    public void updateSpotPrice(Money newPrice) {
        this.spotPricePerUnit = Objects.requireNonNull(newPrice);
    }

    public Money calculateValue(long units) {
        return spotPricePerUnit.multiply((double) units);
    }
}
