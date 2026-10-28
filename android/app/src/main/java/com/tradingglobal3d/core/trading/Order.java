package com.tradingglobal3d.core.trading;

import com.tradingglobal3d.core.model.Money;

import java.util.Objects;

/**
 * Entitas pesanan perdagangan (Order).
 * Mendukung limit order, market order, stop-loss, take-profit,
 * pelacakan partial fill, serta token idempotensi.
 */
public final class Order {
    private final String id;
    private final String requestId;          // Idempotency token
    private final String accountId;
    private final String instrumentId;
    private final OrderSide side;
    private final OrderType type;
    private final Money limitPrice;          // Null untuk market order
    private final Money triggerPrice;        // Pemicu untuk stop-loss/take-profit
    private final long totalQuantity;        // Lembar saham yang dipesan
    private final long createdAt;
    private Money reservedMoney;             // Kas tertahan (untuk BUY)
    private double feeDiscountPercent;

    private long filledQuantity;
    private OrderStatus status;

    public Order(String id, String requestId, String accountId, String instrumentId,
                 OrderSide side, OrderType type, Money limitPrice, Money triggerPrice,
                 long totalQuantity, long createdAt, Money reservedMoney) {
        this.id = Objects.requireNonNull(id);
        this.requestId = Objects.requireNonNull(requestId);
        this.accountId = Objects.requireNonNull(accountId);
        this.instrumentId = Objects.requireNonNull(instrumentId);
        this.side = Objects.requireNonNull(side);
        this.type = Objects.requireNonNull(type);
        this.limitPrice = limitPrice;
        this.triggerPrice = triggerPrice;
        this.totalQuantity = totalQuantity;
        this.createdAt = createdAt;
        this.reservedMoney = reservedMoney;

        this.filledQuantity = 0;
        this.status = (type == OrderType.STOP_LOSS || type == OrderType.TAKE_PROFIT)
                ? OrderStatus.PENDING_TRIGGER
                : OrderStatus.OPEN;
    }

    public String getId() { return id; }
    public String getRequestId() { return requestId; }
    public String getAccountId() { return accountId; }
    public String getInstrumentId() { return instrumentId; }
    public OrderSide getSide() { return side; }
    public OrderType getType() { return type; }
    public Money getLimitPrice() { return limitPrice; }
    public Money getTriggerPrice() { return triggerPrice; }
    public long getTotalQuantity() { return totalQuantity; }
    public long getCreatedAt() { return createdAt; }
    public Money getReservedMoney() { return reservedMoney; }
    public long getFilledQuantity() { return filledQuantity; }
    public OrderStatus getStatus() { return status; }

    public long getRemainingQuantity() {
        return totalQuantity - filledQuantity;
    }

    public boolean isBuy() { return side == OrderSide.BUY; }
    public boolean isSell() { return side == OrderSide.SELL; }

    public void applyFill(long fillQty) {
        if (fillQty <= 0) return;
        this.filledQuantity += fillQty;
        if (this.filledQuantity >= this.totalQuantity) {
            this.status = OrderStatus.FILLED;
        } else {
            this.status = OrderStatus.PARTIALLY_FILLED;
        }
    }

    public void cancel() {
        if (!status.isFinal()) {
            this.status = OrderStatus.CANCELLED;
        }
    }

    public void setStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    public void setReservedMoney(Money reserved) {
        this.reservedMoney = reserved;
    }

    public double getFeeDiscountPercent() { return feeDiscountPercent; }

    public void setFeeDiscountPercent(double discountPercent) {
        if (!Double.isFinite(discountPercent) || discountPercent < 0.0 || discountPercent > 100.0)
            throw new IllegalArgumentException("Diskon komisi order tidak valid");
        feeDiscountPercent = discountPercent;
    }

    public java.math.BigDecimal getEffectiveFeeRate(java.math.BigDecimal baseRate) {
        return baseRate.multiply(java.math.BigDecimal.ONE.subtract(java.math.BigDecimal.valueOf(feeDiscountPercent)
                .movePointLeft(2)));
    }

    public static Order createLimit(String id, String instrumentId, String accountId, OrderSide side, Money price, long quantity, long timestamp) {
        return new Order(id, "REQ-" + id, accountId, instrumentId, side, OrderType.LIMIT, price, null, quantity, timestamp, null);
    }
}
