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
            // Market Maker pasang 2 level bid dan 2 level ask
            BigDecimal bid1 = currentPrice.subtract(tick);
            BigDecimal bid2 = currentPrice.subtract(tick.multiply(BigDecimal.valueOf(2)));
            BigDecimal ask1 = currentPrice.add(tick);
            BigDecimal ask2 = currentPrice.add(tick.multiply(BigDecimal.valueOf(2)));

            // Order Bid (Buy) jika punya kas tersedia
            long buyLot = instrument.getLotSize() * 5L;
            Money requiredCash = new Money(bid1.multiply(BigDecimal.valueOf(buyLot)), cur);
            if (account.getAvailableCash(cur).isGreaterThanOrEqual(requiredCash)) {
                orders.add(new Order(
                        "ORD-NPC-MM-B1-" + System.nanoTime(),
                        "REQ-MM-B1-" + System.nanoTime(),
                        account.getId(), instrument.getId(),
                        OrderSide.BUY, OrderType.LIMIT,
                        new Money(bid1, cur), null, buyLot, timestamp, requiredCash
                ));
            }

            // Order Ask (Sell) jika punya saham di portofolio
            Position pos = account.getPosition(instrument.getId());
            long sellLot = instrument.getLotSize() * 5L;
            if (pos != null && pos.getQuantity() >= sellLot) {
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
        }
        return orders;
    }
}
