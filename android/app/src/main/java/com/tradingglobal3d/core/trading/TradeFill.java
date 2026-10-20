package com.tradingglobal3d.core.trading;

import com.tradingglobal3d.core.model.Money;

import java.util.Objects;

/**
 * Catatan eksekusi parsial atau penuh perjumpaan order beli dan jual.
 */
public final class TradeFill {
    private final String fillId;
    private final String buyOrderId;
    private final String sellOrderId;
    private final String buyAccountId;
    private final String sellAccountId;
    private final String instrumentId;
    private final Money price;
    private final long quantity; // Lembar saham
    private final long timestamp;
    private Money sellerRealizedNetPnL;

    public TradeFill(String fillId, String buyOrderId, String sellOrderId,
                     String buyAccountId, String sellAccountId,
                     String instrumentId, Money price, long quantity, long timestamp) {
        this.fillId = Objects.requireNonNull(fillId);
        this.buyOrderId = Objects.requireNonNull(buyOrderId);
        this.sellOrderId = Objects.requireNonNull(sellOrderId);
        this.buyAccountId = Objects.requireNonNull(buyAccountId);
        this.sellAccountId = Objects.requireNonNull(sellAccountId);
        this.instrumentId = Objects.requireNonNull(instrumentId);
        this.price = Objects.requireNonNull(price);
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public String getFillId() { return fillId; }
    public String getBuyOrderId() { return buyOrderId; }
    public String getSellOrderId() { return sellOrderId; }
    public String getBuyAccountId() { return buyAccountId; }
    public String getSellAccountId() { return sellAccountId; }
    public String getInstrumentId() { return instrumentId; }
    public Money getPrice() { return price; }
    public long getQuantity() { return quantity; }
    public long getTimestamp() { return timestamp; }
    public Money getSellerRealizedNetPnL() { return sellerRealizedNetPnL; }
    public TradeFill withSellerRealizedNetPnL(Money value) {
        sellerRealizedNetPnL = value;
        return this;
    }

    public Money getTotalAmount() {
        return price.multiply((double) quantity);
    }
}
