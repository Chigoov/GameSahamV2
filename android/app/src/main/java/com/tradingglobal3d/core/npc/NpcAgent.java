package com.tradingglobal3d.core.npc;

import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.trading.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Agen Pelaku Pasar NPC yang terdanai nyata.
 * Memiliki saldo kas dan portofolio sendiri; tidak dapat membeli tanpa uang
 * atau menjual tanpa saham yang sah.
 */
public final class NpcAgent {
    private final String id;
    private final String name;
    private final NpcStrategyType strategy;
    private final Account account;

    public NpcAgent(String id, String name, NpcStrategyType strategy, Account account) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.strategy = Objects.requireNonNull(strategy);
        this.account = Objects.requireNonNull(account);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public NpcStrategyType getStrategy() { return strategy; }
    public Account getAccount() { return account; }

    /**
     * Menghasilkan order yang sah menurut strategi dan saldo/posisi NPC saat ini.
     */
    public List<Order> generateOrders(Instrument instrument, OrderBook book, long timestamp) {
        List<Order> orders = new ArrayList<>();
        Currency cur = instrument.getCurrency();
        BigDecimal currentPrice = instrument.getCurrentPrice().getAmount();
        BigDecimal tick = instrument.getExchange().getTickSize(currentPrice);

        if (strategy == NpcStrategyType.MARKET_MAKER) {
            BigDecimal highestBid = book.getBids().isEmpty() ? null : book.getBids().firstKey();
            BigDecimal lowestAsk = book.getAsks().isEmpty() ? null : book.getAsks().firstKey();

            // Market Maker pasang 2 level bid dan 2 level ask
            BigDecimal bid1 = currentPrice.subtract(tick);
            BigDecimal ask1 = currentPrice.add(tick);

            if (lowestAsk != null && bid1.compareTo(lowestAsk) >= 0) {
                bid1 = lowestAsk.subtract(tick);
            }
            if (highestBid != null && ask1.compareTo(highestBid) <= 0) {
                ask1 = highestBid.add(tick);
            }

            boolean hasRestingBid = false;
            if (book.getBids().containsKey(bid1)) {
                for (Order o : book.getBids().get(bid1)) {
                    if (account.getId().equals(o.getAccountId()) && !o.getStatus().isFinal()) {
                        hasRestingBid = true;
                        break;
                    }
                }
            }

            boolean hasRestingAsk = false;
            if (book.getAsks().containsKey(ask1)) {
                for (Order o : book.getAsks().get(ask1)) {
                    if (account.getId().equals(o.getAccountId()) && !o.getStatus().isFinal()) {
                        hasRestingAsk = true;
                        break;
                    }
                }
            }

            // Order Bid (Buy) jika belum ada antrean dan punya kas tersedia
            long buyLot = instrument.getLotSize() * 5L;
            Money requiredCash = new Money(bid1.multiply(BigDecimal.valueOf(buyLot)), cur);
            if (!hasRestingBid && account.getAvailableCash(cur).isGreaterThanOrEqual(requiredCash)) {
                orders.add(new Order(
                        "ORD-NPC-MM-B1-" + System.nanoTime(),
                        "REQ-MM-B1-" + System.nanoTime(),
                        account.getId(), instrument.getId(),
                        OrderSide.BUY, OrderType.LIMIT,
                        new Money(bid1, cur), null, buyLot, timestamp, requiredCash
                ));
            }

            // Order Ask (Sell) jika belum ada antrean dan punya saham di portofolio
            Position pos = account.getPosition(instrument.getId());
            long sellLot = instrument.getLotSize() * 5L;
            if (!hasRestingAsk && pos != null && pos.getQuantity() >= sellLot) {
                orders.add(new Order(
                        "ORD-NPC-MM-S1-" + System.nanoTime(),
                        "REQ-MM-S1-" + System.nanoTime(),
                        account.getId(), instrument.getId(),
                        OrderSide.SELL, OrderType.LIMIT,
                        new Money(ask1, cur), null, sellLot, timestamp, null
                ));
            }
        } else if (strategy == NpcStrategyType.VALUE_INVESTOR) {
            // Value investor membeli jika harga turun hari ini
            if (instrument.getDailyChangePercent() < -1.0) {
                long qty = instrument.getLotSize() * 2L;
                BigDecimal limit = currentPrice.subtract(tick);
                Money req = new Money(limit.multiply(BigDecimal.valueOf(qty)), cur);
                if (account.getAvailableCash(cur).isGreaterThanOrEqual(req)) {
                    orders.add(new Order(
                            "ORD-NPC-VAL-B-" + System.nanoTime(),
                            "REQ-VAL-B-" + System.nanoTime(),
                            account.getId(), instrument.getId(),
                            OrderSide.BUY, OrderType.LIMIT,
                            new Money(limit, cur), null, qty, timestamp, req
                    ));
                }
            }
        } else if (strategy == NpcStrategyType.MOMENTUM_TRADER) {
            // Momentum trader membeli jika harga sedang naik
            if (instrument.getDailyChangePercent() > 1.5) {
                long qty = instrument.getLotSize() * 2L;
                BigDecimal limit = currentPrice.add(tick);
                Money req = new Money(limit.multiply(BigDecimal.valueOf(qty)), cur);
                if (account.getAvailableCash(cur).isGreaterThanOrEqual(req)) {
                    orders.add(new Order(
                            "ORD-NPC-MOM-B-" + System.nanoTime(),
                            "REQ-MOM-B-" + System.nanoTime(),
                            account.getId(), instrument.getId(),
                            OrderSide.BUY, OrderType.LIMIT,
                            new Money(limit, cur), null, qty, timestamp, req
                    ));
                }
            }
        } else if (strategy == NpcStrategyType.RETAIL_NOISE) {
            // Investor & Trader Ritel: mengambil likuiditas antrean (taker) untuk membentuk transaksi sah
            long timeBucket = timestamp / 900_000L; // bucket 15 menit
            int seed = Math.abs(Objects.hash(instrument.getId(), timeBucket, id));
            // 70% peluang ada aktivitas perdagangan di setiap bucket 15 menit per instrumen
            if (seed % 10 < 7) {
                boolean isBuy = (seed % 2 == 0);
                long lotMultiplier = 1 + (seed % 4); // 1 - 4 lot
                long qty = Math.max(instrument.getLotSize(), instrument.getLotSize() * lotMultiplier);

                if (isBuy) {
                    BigDecimal targetPrice;
                    if (!book.getAsks().isEmpty()) {
                        targetPrice = book.getAsks().firstKey();
                    } else {
                        targetPrice = currentPrice.add(tick);
                    }
                    Money req = new Money(targetPrice.multiply(BigDecimal.valueOf(qty)), cur);
                    if (account.getAvailableCash(cur).isGreaterThanOrEqual(req)) {
                        orders.add(new Order(
                                "ORD-NPC-RET-B-" + System.nanoTime(),
                                "REQ-RET-B-" + System.nanoTime(),
                                account.getId(), instrument.getId(),
                                OrderSide.BUY, OrderType.LIMIT,
                                new Money(targetPrice, cur), null, qty, timestamp, req
                        ));
                    }
                } else {
                    Position pos = account.getPosition(instrument.getId());
                    if (pos != null && pos.getQuantity() >= qty) {
                        BigDecimal targetPrice;
                        if (!book.getBids().isEmpty()) {
                            targetPrice = book.getBids().firstKey();
                        } else {
                            targetPrice = currentPrice.subtract(tick);
                        }
                        orders.add(new Order(
                                "ORD-NPC-RET-S-" + System.nanoTime(),
                                "REQ-RET-S-" + System.nanoTime(),
                                account.getId(), instrument.getId(),
                                OrderSide.SELL, OrderType.LIMIT,
                                new Money(targetPrice, cur), null, qty, timestamp, null
                        ));
                    }
                }
            }
        }
        return orders;
    }
}
