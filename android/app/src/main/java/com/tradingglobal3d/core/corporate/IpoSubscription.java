package com.tradingglobal3d.core.corporate;

import com.tradingglobal3d.core.model.Money;

import java.util.Objects;

/**
 * Pemesanan Saham IPO oleh Pemain atau Akun Klien.
 */
public final class IpoSubscription {
    public enum SubscriptionStatus {
        PENDING("Menunggu Penjatahan"),
        ALLOTTED("Mendapat Jatah Saham"),
        REFUNDED("Dana Dikembalikan");

        private final String label;
        SubscriptionStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final String id;
    private final String ipoId;
    private final String accountId;
    private final long orderedLots;
    private final Money reservedMoney;

    private long allottedLots;
    private Money refundMoney;
    private SubscriptionStatus status;

    public IpoSubscription(String id, String ipoId, String accountId, long orderedLots, Money reservedMoney) {
        this.id = Objects.requireNonNull(id);
        this.ipoId = Objects.requireNonNull(ipoId);
        this.accountId = Objects.requireNonNull(accountId);
        this.orderedLots = orderedLots;
        this.reservedMoney = Objects.requireNonNull(reservedMoney);
        this.allottedLots = 0;
        this.status = SubscriptionStatus.PENDING;
    }

    public String getId() { return id; }
    public String getIpoId() { return ipoId; }
    public String getAccountId() { return accountId; }
    public long getOrderedLots() { return orderedLots; }
    public Money getReservedMoney() { return reservedMoney; }
    public long getAllottedLots() { return allottedLots; }
    public Money getRefundMoney() { return refundMoney; }
    public SubscriptionStatus getStatus() { return status; }

    public void applyAllotment(long allottedLots, Money refundMoney) {
        this.allottedLots = allottedLots;
        this.refundMoney = refundMoney;
        this.status = SubscriptionStatus.ALLOTTED;
    }
}
