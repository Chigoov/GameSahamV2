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

    public static Money marketMakerQuotePrice(Instrument instrument, OrderBook book, boolean bid) {
        Currency cur = instrument.getCurrency();
        BigDecimal currentPrice = instrument.getCurrentPrice().getAmount();
        BigDecimal tick = instrument.getExchange().getTickSize(currentPrice);
        BigDecimal highestBid = book.getBids().isEmpty() ? null : book.getBids().firstKey();
        BigDecimal lowestAsk = book.getAsks().isEmpty() ? null : book.getAsks().firstKey();
        double lockedRatio = instrument.getLockedFloatRatio();
        double squeeze = lockedRatio >= 0.80 ? 2.50 : lockedRatio >= 0.60 ? 1.80 : lockedRatio >= 0.30 ? 1.35 : 1.0;
        BigDecimal spread = tick.multiply(BigDecimal.valueOf(squeeze)).setScale(cur.getDecimalPlaces(), java.math.RoundingMode.CEILING);
        BigDecimal price = bid ? currentPrice.subtract(tick) : currentPrice.add(spread);
        if (bid && lowestAsk != null && price.compareTo(lowestAsk) >= 0) price = lowestAsk.subtract(tick);
        if (!bid && highestBid != null && price.compareTo(highestBid) <= 0) price = highestBid.add(tick);
        Money rounded = bid ? instrument.roundDownToIdxTick(price) : instrument.roundUpToIdxTick(price);
        if (instrument.getDailyFloorPrice() != null && rounded.compareTo(instrument.getDailyFloorPrice()) < 0)
            return instrument.getDailyFloorPrice();
        if (instrument.getDailyCeilingPrice() != null && rounded.compareTo(instrument.getDailyCeilingPrice()) > 0)
            return instrument.getDailyCeilingPrice();
        return rounded;
    }

    public void ensureLiquidity(Instrument instrument, OrderBook book,
                                com.tradingglobal3d.core.economy.EconomyLedger ledger,
                                long timestamp, long requiredFreeShares, Money requiredAvailableCash, String eventId) {
        if ("ACC-NPC-MM-01".equals(account.getId())) {
            Currency currency = instrument.getCurrency();
            long committed = 0;
            for (java.util.Queue<Order> level : book.getAsks().values()) for (Order order : level) {
                if (!order.getStatus().isFinal() && account.getId().equals(order.getAccountId()))
                    committed = Math.addExact(committed, order.getRemainingQuantity());
            }
            Position current = account.getPosition(instrument.getId());
            long held = current == null ? 0 : current.getQuantity();
            if (committed > held) throw new IllegalStateException("Komitmen saham MM melebihi persediaan");
            long free = held - committed;
            long target = free < Math.multiplyExact(instrument.getLotSize(), 50L)
                    ? Math.max(Math.multiplyExact(instrument.getLotSize(), 2000L), requiredFreeShares) : requiredFreeShares;
            long issued = Math.max(0L, Math.subtractExact(target, free));
            String supplyId = "MM-SUPPLY-" + eventId;
            Position next = current == null ? new Position(instrument.getTicker(), instrument.getExchange().name(), currency,
                    0, BigDecimal.ZERO, instrument.getCurrentPrice().getAmount()) : current.copy();
            if (issued > 0 && !instrument.hasShareSupplyEvent(supplyId)) {
                next.addBuy(issued, instrument.getCurrentPrice().getAmount());
                instrument.validateShareSupplyEvent(supplyId, BigDecimal.ONE, issued, 0);
            }
            Money minimum = currency == Currency.USD ? Money.of(500_000, currency) : Money.of(5_000_000_000L, currency);
            Money baseline = currency == Currency.USD ? Money.of(1_000_000, currency) : Money.of(10_000_000_000L, currency);
            Money available = account.getAvailableCash(currency);
            Money cashTarget = available.isLessThan(minimum) && baseline.isGreaterThanOrEqual(requiredAvailableCash)
                    ? baseline : requiredAvailableCash;
            if (available.isLessThan(cashTarget)) {
                ledger.recordMutation("MM-CAPITAL-" + eventId + "-" + currency, account,
                        com.tradingglobal3d.core.economy.LedgerEntryType.LIQUIDITY_CAPITAL,
                        cashTarget.subtract(available), true, timestamp, "Modal eksternal fasilitas likuiditas tanpa utang/bunga");
            }
            if (issued > 0 && !instrument.hasShareSupplyEvent(supplyId)) {
                instrument.applyShareSupplyEvent(supplyId, BigDecimal.ONE, issued, 0);
                account.addPosition(next);
            }
        } else if ("ACC-NPC-RETAIL-01".equals(account.getId())) {
            Currency currency = instrument.getCurrency();
            long committed = 0;
            for (java.util.Queue<Order> level : book.getAsks().values()) for (Order order : level) {
                if (!order.getStatus().isFinal() && account.getId().equals(order.getAccountId()))
                    committed = Math.addExact(committed, order.getRemainingQuantity());
            }
            Position current = account.getPosition(instrument.getId());
            long held = current == null ? 0 : current.getQuantity();
            long free = Math.max(0L, held - committed);
            long minInventory = Math.multiplyExact(instrument.getLotSize(), 100L);
            long targetInventory = Math.multiplyExact(instrument.getLotSize(), 1000L);
            long target = free < minInventory ? Math.max(targetInventory, requiredFreeShares) : requiredFreeShares;
            long issued = Math.max(0L, Math.subtractExact(target, free));
            String supplyId = "RETAIL-SUPPLY-" + eventId;
            Position next = current == null ? new Position(instrument.getTicker(), instrument.getExchange().name(), currency,
                    0, BigDecimal.ZERO, instrument.getCurrentPrice().getAmount()) : current.copy();
            if (issued > 0 && !instrument.hasShareSupplyEvent(supplyId)) {
                next.addBuy(issued, instrument.getCurrentPrice().getAmount());
                instrument.validateShareSupplyEvent(supplyId, BigDecimal.ONE, issued, 0);
            }
            Money minCash = currency == Currency.USD ? Money.of(200_000.0, currency) : Money.of(2_000_000_000L, currency);
            Money cashTarget = minCash;
            Money available = account.getAvailableCash(currency);
            if (available.isLessThan(cashTarget)) {
                ledger.recordMutation("RETAIL-CAPITAL-" + eventId + "-" + currency, account,
                        com.tradingglobal3d.core.economy.LedgerEntryType.LIQUIDITY_CAPITAL,
                        cashTarget.subtract(available), true, timestamp, "Modal eksternal fasilitas likuiditas ritel");
            }
            if (issued > 0 && !instrument.hasShareSupplyEvent(supplyId)) {
                instrument.applyShareSupplyEvent(supplyId, BigDecimal.ONE, issued, 0);
                account.addPosition(next);
            }
        }
    }

    /**
     * Menghasilkan order yang sah menurut strategi dan saldo/posisi NPC saat ini.
     */
    public List<Order> generateOrders(Instrument instrument, OrderBook book, long timestamp) {
        return generateOrders(instrument, book, timestamp, 0.0);
    }

    /**
     * Menghasilkan order NPC dengan mempertimbangkan sentimen berita pasar (R12).
     */
    public List<Order> generateOrders(Instrument instrument, OrderBook book, long timestamp, double newsSentimentImpact) {
        List<Order> orders = new ArrayList<>();
        Currency cur = instrument.getCurrency();
        BigDecimal currentPrice = instrument.getCurrentPrice().getAmount();
        BigDecimal tick = instrument.getExchange().getTickSize(currentPrice);

        BigDecimal ceiling = instrument.getDailyCeilingPrice() != null ? instrument.getDailyCeilingPrice().getAmount() : null;
        BigDecimal floor = instrument.getDailyFloorPrice() != null ? instrument.getDailyFloorPrice().getAmount() : null;

        if (strategy == NpcStrategyType.MARKET_MAKER) {
            Position pos = account.getPosition(instrument.getId());
            BigDecimal bid1 = marketMakerQuotePrice(instrument, book, true).getAmount();
            BigDecimal ask1 = marketMakerQuotePrice(instrument, book, false).getAmount();
            BigDecimal bid2 = instrument.roundDownToIdxTick(bid1.subtract(tick)).getAmount();
            BigDecimal ask2 = instrument.roundUpToIdxTick(ask1.add(ask1.subtract(currentPrice).max(tick))).getAmount();
            if (ceiling != null && ask2.compareTo(ceiling) > 0) ask2 = ceiling;
            if (floor != null && bid2.compareTo(floor) < 0) bid2 = floor;

            long mmLot = instrument.getLotSize() * 25L; // 25 lot per level

            // Level 1 Bid
            if (!book.getBids().containsKey(bid1) || book.getBids().get(bid1).isEmpty()) {
                Money req1 = new Money(bid1.multiply(BigDecimal.valueOf(mmLot)), cur);
                if (account.getAvailableCash(cur).isGreaterThanOrEqual(req1)) {
                    orders.add(new Order("ORD-NPC-MM-B1-" + System.nanoTime(), "REQ-MM-B1-" + System.nanoTime(),
                            account.getId(), instrument.getId(), OrderSide.BUY, OrderType.LIMIT,
                            new Money(bid1, cur), null, mmLot, timestamp, req1));
                }
            }

            // Level 2 Bid
            if (bid2.compareTo(bid1) < 0 && (!book.getBids().containsKey(bid2) || book.getBids().get(bid2).isEmpty())) {
                Money req2 = new Money(bid2.multiply(BigDecimal.valueOf(mmLot)), cur);
                if (account.getAvailableCash(cur).isGreaterThanOrEqual(req2)) {
                    orders.add(new Order("ORD-NPC-MM-B2-" + System.nanoTime(), "REQ-MM-B2-" + System.nanoTime(),
                            account.getId(), instrument.getId(), OrderSide.BUY, OrderType.LIMIT,
                            new Money(bid2, cur), null, mmLot, timestamp, req2));
                }
            }

            // Level 1 Ask
            if (pos != null && pos.getQuantity() >= mmLot && (!book.getAsks().containsKey(ask1) || book.getAsks().get(ask1).isEmpty())) {
                orders.add(new Order("ORD-NPC-MM-S1-" + System.nanoTime(), "REQ-MM-S1-" + System.nanoTime(),
                        account.getId(), instrument.getId(), OrderSide.SELL, OrderType.LIMIT,
                        new Money(ask1, cur), null, mmLot, timestamp, null));
            }

            // Level 2 Ask
            if (pos != null && pos.getQuantity() >= mmLot * 2 && ask2.compareTo(ask1) > 0 && (!book.getAsks().containsKey(ask2) || book.getAsks().get(ask2).isEmpty())) {
                orders.add(new Order("ORD-NPC-MM-S2-" + System.nanoTime(), "REQ-MM-S2-" + System.nanoTime(),
                        account.getId(), instrument.getId(), OrderSide.SELL, OrderType.LIMIT,
                        new Money(ask2, cur), null, mmLot, timestamp, null));
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
            Position pos = account.getPosition(instrument.getId());

            // Investor & Trader Ritel: mengambil likuiditas antrean (taker) untuk membentuk transaksi sah setiap detik
            long timeBucket = timestamp / 1000L; // bucket per detik (kecepatan pergerakan harga tinggi)
            int seed = Math.abs(Objects.hash(instrument.getId(), timeBucket, id));

            // SINTESA MODUL 1: 3 Rezim Naluri Bot Pembeli terhadap Nilai Intrinsik (VirCal Gravity)
            BigDecimal intrinsic = instrument.getIntrinsicValue();
            if (intrinsic == null) intrinsic = currentPrice;

            int buyProbabilityPercent;
            boolean deeplyUndervalued = false;
            if (currentPrice.compareTo(intrinsic) >= 0) {
                // 1. Overvalued (Harga Pasar >= Harga Wajar): Peluang beli 40%, jual 60%
                buyProbabilityPercent = 40;
            } else if (currentPrice.multiply(new BigDecimal("1.50")).compareTo(intrinsic) < 0) {
                // 2. Deeply Undervalued (Diskon > 33%): Peluang beli melonjak 68%, lot digandakan 2.0x!
                buyProbabilityPercent = 68;
                deeplyUndervalued = true;
            } else {
                // 3. Moderately Undervalued (Harga Pasar < Wajar <= 1.5x): Peluang beli 63%
                buyProbabilityPercent = 63;
            }

            // Pengaruh Sentimen Berita
            if (newsSentimentImpact > 0.1) {
                buyProbabilityPercent = Math.min(90, buyProbabilityPercent + 15);
            } else if (newsSentimentImpact < -0.1) {
                buyProbabilityPercent = Math.max(15, buyProbabilityPercent - 25);
            }

            // Jika ada sentimen berita aktif, antusiasme transaksi melonjak (90% vs 70%)
            int activityThreshold = Math.abs(newsSentimentImpact) > 0.1 ? 9 : 7;
            if (seed % 10 < activityThreshold) {
                boolean isBuy = (seed % 100) < buyProbabilityPercent;

                long lotMultiplier = 1 + (seed % 4); // 1 - 4 lot
                if (isBuy && deeplyUndervalued) {
                    lotMultiplier *= 2;
                }
                long qty = Math.max(instrument.getLotSize(), instrument.getLotSize() * lotMultiplier);

                if (isBuy) {
                    BigDecimal targetPrice;
                    if (!book.getAsks().isEmpty()) {
                        targetPrice = book.getAsks().firstKey();
                    } else {
                        targetPrice = currentPrice.add(tick);
                    }
                    if (ceiling != null && targetPrice.compareTo(ceiling) > 0) targetPrice = ceiling;
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
                    pos = account.getPosition(instrument.getId());
                    if (pos != null && pos.getQuantity() >= qty) {
                        BigDecimal targetPrice;
                        if (!book.getBids().isEmpty()) {
                            targetPrice = book.getBids().firstKey();
                        } else {
                            targetPrice = currentPrice.subtract(tick);
                        }
                        if (floor != null && targetPrice.compareTo(floor) < 0) targetPrice = floor;
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
