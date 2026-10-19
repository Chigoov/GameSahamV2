package com.tradingglobal3d.core.trading;

import com.tradingglobal3d.core.model.Money;

import java.math.BigDecimal;
import java.util.*;

/**
 * Buku Antrean Pesanan (Order Book) untuk satu instrumen tertentu.
 * Mengimplementasikan aturan prioritas harga dan waktu (Price-Time Priority FIFO)
 * serta mengelola status perdagangan (Normal vs Suspensi/Halt).
 */
public final class OrderBook {
    private final String instrumentId;
    private final TreeMap<BigDecimal, Queue<Order>> bids = new TreeMap<>(Comparator.reverseOrder());
    private final TreeMap<BigDecimal, Queue<Order>> asks = new TreeMap<>();
    private final List<Order> triggerOrders = new ArrayList<>();
    private final List<TradeFill> tradeHistory = new ArrayList<>();

    private TradingHaltState haltState = TradingHaltState.NORMAL;
    private Money lastTradedPrice;

    public OrderBook(String instrumentId) {
        this(instrumentId, null);
    }

    public OrderBook(String instrumentId, Money initialPrice) {
        this.instrumentId = Objects.requireNonNull(instrumentId);
        this.lastTradedPrice = initialPrice;
    }

    public String getInstrumentId() { return instrumentId; }
    public TradingHaltState getHaltState() { return haltState; }
    public Money getLastTradedPrice() { return lastTradedPrice; }
    public List<TradeFill> getTradeHistory() { return Collections.unmodifiableList(tradeHistory); }

    public void setHaltState(TradingHaltState state) {
        this.haltState = state;
    }

    public void setLastTradedPrice(Money price) {
        this.lastTradedPrice = price;
    }

    public synchronized void addLimitOrder(Order order) {
        if (order.getLimitPrice() == null) {
            throw new IllegalArgumentException("Limit order harus memiliki limit price");
        }
        BigDecimal price = order.getLimitPrice().getAmount();
        if (order.isBuy()) {
            bids.computeIfAbsent(price, k -> new LinkedList<>()).add(order);
        } else {
            asks.computeIfAbsent(price, k -> new LinkedList<>()).add(order);
        }
    }

    public synchronized void addTriggerOrder(Order order) {
        triggerOrders.add(order);
    }

    public synchronized TreeMap<BigDecimal, Queue<Order>> getBids() { return bids; }
    public synchronized TreeMap<BigDecimal, Queue<Order>> getAsks() { return asks; }
    public synchronized List<Order> getTriggerOrders() { return triggerOrders; }

    public synchronized void recordFill(TradeFill fill) {
        tradeHistory.add(fill);
        this.lastTradedPrice = fill.getPrice(); // HANYA BERUBAH saat terjadi transaksi nyata (FR-04.14)
    }

    public static final class BookLevel {
        public final BigDecimal price;
        public final long totalQuantity;
        public final int orderCount;

        public BookLevel(BigDecimal price, long totalQuantity, int orderCount) {
            this.price = price;
            this.totalQuantity = totalQuantity;
            this.orderCount = orderCount;
        }
    }

    public synchronized List<BookLevel> getBidLevels(int maxLevels) {
        return extractLevels(bids, maxLevels);
    }

    public synchronized List<BookLevel> getAskLevels(int maxLevels) {
        return extractLevels(asks, maxLevels);
    }

    private List<BookLevel> extractLevels(TreeMap<BigDecimal, Queue<Order>> map, int maxLevels) {
        List<BookLevel> levels = new ArrayList<>();
        int count = 0;
        for (Map.Entry<BigDecimal, Queue<Order>> entry : map.entrySet()) {
            if (count >= maxLevels) break;
            long totalQty = 0;
            int orderCount = 0;
            for (Order o : entry.getValue()) {
                if (!o.getStatus().isFinal()) {
                    totalQty += o.getRemainingQuantity();
                    orderCount++;
                }
            }
            if (totalQty > 0) {
                levels.add(new BookLevel(entry.getKey(), totalQty, orderCount));
                count++;
            }
        }
        return levels;
    }

    public synchronized boolean cancelOrder(String orderId) {
        // Cek di bids
        for (Queue<Order> queue : bids.values()) {
            for (Order o : queue) {
                if (o.getId().equals(orderId) && !o.getStatus().isFinal()) {
                    o.cancel();
                    return true;
                }
            }
        }
        // Cek di asks
        for (Queue<Order> queue : asks.values()) {
            for (Order o : queue) {
                if (o.getId().equals(orderId) && !o.getStatus().isFinal()) {
                    o.cancel();
                    return true;
                }
            }
        }
        // Cek di trigger orders
        for (Order o : triggerOrders) {
            if (o.getId().equals(orderId) && !o.getStatus().isFinal()) {
                o.cancel();
                return true;
            }
        }
        return false;
    }
}
