package com.tradingglobal3d.core.trading;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Mesin Pencocokan Order (Matching Engine).
 * Menerapkan eksekusi Price-Time Priority, penyelesaian atomik kas/saham/ledger,
 * pemisahan fee beli (0.15%) dan jual (0.25%), serta lelang call auction saat pembukaan kembali.
 */
public final class MatchingEngine {
    public static final BigDecimal BUY_FEE_RATE = new BigDecimal("0.0015");   // 0.15%
    public static final BigDecimal SELL_FEE_RATE = new BigDecimal("0.0025");  // 0.25% (termasuk pajak game)

    public static final class OrderResult {
        public final Order order;
        public final boolean isSuccess;
        public final String message;
        public final List<TradeFill> fills;

        public OrderResult(Order order, boolean isSuccess, String message, List<TradeFill> fills) {
            this.order = order;
            this.isSuccess = isSuccess;
            this.message = message;
            this.fills = fills;
        }
    }

    public static String validateOrder(Order order, Account account, Instrument instrument) {
        if (order.getRequestId().trim().isEmpty() || order.getId().trim().isEmpty()) return "Identitas order wajib diisi";
        if (!order.getAccountId().equals(account.getId()) || !order.getInstrumentId().equals(instrument.getId()))
            return "Konteks akun atau instrumen tidak sesuai";
        if (!instrument.isTradable()) return "Instrumen tidak aktif untuk perdagangan";
        if (order.getTotalQuantity() <= 0 || order.getTotalQuantity() % instrument.getLotSize() != 0)
            return "Jumlah harus positif dan sesuai ukuran lot instrumen";
        if (order.getType() == OrderType.LIMIT && order.getLimitPrice() == null) return "Harga limit wajib diisi";
        if ((order.getType() == OrderType.STOP_LOSS || order.getType() == OrderType.TAKE_PROFIT)
                && order.getTriggerPrice() == null) return "Harga pemicu wajib diisi";
        for (Money price : new Money[]{order.getLimitPrice(), order.getTriggerPrice()}) {
            if (price == null) continue;
            if (price.getCurrency() != instrument.getCurrency() || !price.isPositive())
                return "Harga harus positif dalam mata uang instrumen";
            if (price.getAmount().remainder(instrument.getExchange().getTickSize(price.getAmount())).signum() != 0)
                return "Harga tidak sesuai fraksi instrumen";
        }
        return null;
    }

    private Money requiredLimitBuyReservation(Order order, Instrument instrument) {
        Currency currency = instrument.getCurrency();
        BigDecimal lotValue = order.getLimitPrice().getAmount().multiply(BigDecimal.valueOf(instrument.getLotSize()));
        BigDecimal feePerLot = lotValue.multiply(BUY_FEE_RATE).setScale(currency.getDecimalPlaces(), RoundingMode.CEILING);
        BigDecimal lots = BigDecimal.valueOf(order.getTotalQuantity() / instrument.getLotSize());
        return new Money(lotValue.add(feePerLot).multiply(lots), currency);
    }

    public synchronized org.json.JSONObject previewOrder(Order order, Account account, Instrument instrument,
                                                         OrderBook book, long timestamp) throws org.json.JSONException {
        String error = validateOrder(order, account, instrument);
        org.json.JSONObject result = new org.json.JSONObject().put("success", error == null);
        if (error != null) return result.put("message", error);
        Currency currency = instrument.getCurrency();
        long remaining = order.getTotalQuantity();
        BigDecimal nominal = BigDecimal.ZERO;
        Money fee = Money.zero(currency);
        BigDecimal reference = instrument.getCurrentPrice().getAmount();
        boolean waiting = order.getStatus() == OrderStatus.PENDING_TRIGGER || book.getHaltState() != TradingHaltState.NORMAL;
        if (!waiting) {
            Map<BigDecimal, Queue<Order>> levels = order.isBuy() ? book.getAsks() : book.getBids();
            for (Map.Entry<BigDecimal, Queue<Order>> level : levels.entrySet()) {
                if (order.getType() == OrderType.LIMIT && (order.isBuy()
                        ? level.getKey().compareTo(order.getLimitPrice().getAmount()) > 0
                        : level.getKey().compareTo(order.getLimitPrice().getAmount()) < 0)) break;
                for (Order maker : level.getValue()) {
                    if (maker.getStatus().isFinal() || maker.getAccountId().equals(account.getId())) continue;
                    long units = Math.min(remaining, maker.getRemainingQuantity());
                    Money value = new Money(level.getKey().multiply(BigDecimal.valueOf(units)), currency);
                    nominal = nominal.add(value.getAmount());
                    fee = fee.add(new Money(value.getAmount().multiply(order.isBuy() ? BUY_FEE_RATE : SELL_FEE_RATE), currency));
                    remaining -= units;
                    if (remaining == 0) break;
                }
                if (remaining == 0) break;
            }
        }
        long filled = order.getTotalQuantity() - remaining;
        Money cashImpact = new Money(nominal, currency);
        cashImpact = order.isBuy() ? cashImpact.add(fee) : cashImpact.subtract(fee);
        Money reserved = Money.zero(currency);
        if (order.isBuy() && order.getType() == OrderType.LIMIT) {
            reserved = requiredLimitBuyReservation(order, instrument);
        } else if (order.isBuy()) reserved = cashImpact;
        boolean affordable = !order.isBuy() || account.getAvailableCash(currency).isGreaterThanOrEqual(reserved);
        if (!affordable) result.put("success", false).put("message", "Kas tersedia tidak mencukupi");
        BigDecimal vwap = filled == 0 ? null : nominal.divide(BigDecimal.valueOf(filled), 6, RoundingMode.HALF_UP);
        result.put("accountId", account.getId()).put("accountName", account.getName())
                .put("instrumentId", instrument.getId()).put("currency", currency.name()).put("side", order.getSide().name())
                .put("type", order.getType().name()).put("quantity", order.getTotalQuantity())
                .put("estimatedFillQuantity", filled).put("remainingQuantity", remaining)
                .put("estimatedFee", fee.format()).put("estimatedCashImpact", cashImpact.format())
                .put("requiredCash", reserved.format()).put("availableCash", account.getAvailableCash(currency).format())
                .put("estimatedVwap", vwap == null ? org.json.JSONObject.NULL : vwap.toPlainString())
                .put("slippagePercent", vwap == null || reference.signum() == 0 ? org.json.JSONObject.NULL
                        : vwap.subtract(reference).divide(reference, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).toPlainString())
                .put("timestamp", timestamp).put("expiresAt", timestamp + 30000)
                .put("haltState", book.getHaltState().name()).put("waitingForTrigger", waiting)
                .put("warning", "Estimasi antrean pasar game, bukan jaminan fill. Validasi saldo, pasar dan mandat diulang saat kirim.");
        return result;
    }

    /**
     * Mengirim dan memproses order ke pasar.
     */
    public synchronized OrderResult submitOrder(
            Order order,
            Account account,
            Instrument instrument,
            OrderBook book,
            EconomyLedger ledger,
            Map<String, Account> allAccounts,
            long gameTimestamp) {

        Objects.requireNonNull(order);
        Objects.requireNonNull(account);
        Objects.requireNonNull(instrument);
        Objects.requireNonNull(book);

        String validationError = validateOrder(order, account, instrument);
        if (validationError != null) {
            order.setStatus(OrderStatus.REJECTED);
            return new OrderResult(order, false, validationError, Collections.emptyList());
        }

        if (book.getHaltState() == TradingHaltState.NORMAL) {
            String settlementError = validateCounterparties(order, account, instrument, book, allAccounts);
            if (settlementError != null) {
                order.setStatus(OrderStatus.REJECTED);
                return new OrderResult(order, false, settlementError, Collections.emptyList());
            }
        }

        // 1. Validasi Saldo & Kepemilikan + Reservasi
        Currency currency = instrument.getCurrency();
        if (order.isBuy()) {
            BigDecimal estPrice = order.getLimitPrice() != null ? order.getLimitPrice().getAmount() : instrument.getCurrentPrice().getAmount();
            BigDecimal nominal = estPrice.multiply(BigDecimal.valueOf(order.getTotalQuantity()));
            BigDecimal fee = nominal.multiply(BUY_FEE_RATE);
            Money totalRequired = order.getType() == OrderType.LIMIT
                    ? requiredLimitBuyReservation(order, instrument) : new Money(nominal.add(fee), currency);
            if (order.getType() != OrderType.LIMIT) {
                totalRequired = Money.zero(currency);
                long remaining = order.getTotalQuantity();
                for (Map.Entry<BigDecimal, Queue<Order>> level : book.getAsks().entrySet()) {
                    for (Order maker : level.getValue()) {
                        if (maker.getStatus().isFinal()) continue;
                        long units = Math.min(remaining, maker.getRemainingQuantity());
                        Money value = new Money(level.getKey().multiply(BigDecimal.valueOf(units)), currency);
                        totalRequired = totalRequired.add(value)
                                .add(new Money(value.getAmount().multiply(BUY_FEE_RATE), currency));
                        remaining -= units;
                        if (remaining == 0) break;
                    }
                    if (remaining == 0) break;
                }
            }

            if (account.getAvailableCash(currency).isLessThan(totalRequired)) {
                order.setStatus(OrderStatus.REJECTED);
                return new OrderResult(order, false, "Saldo kas " + currency + " tidak mencukupi untuk order beli", Collections.emptyList());
            }
            account.reserveCash(totalRequired);
            order.setReservedMoney(totalRequired);
        } else {
            // SELL
            Position pos = account.getPosition(instrument.getId());
            long committedShares = 0;
            for (Queue<Order> level : book.getAsks().values()) {
                for (Order queued : level) {
                    if (!queued.getStatus().isFinal() && queued.getAccountId().equals(account.getId())) {
                        committedShares = Math.addExact(committedShares, queued.getRemainingQuantity());
                    }
                }
            }
            if (pos == null || pos.getQuantity() - committedShares < order.getTotalQuantity()) {
                order.setStatus(OrderStatus.REJECTED);
                return new OrderResult(order, false, "Jumlah lembar saham tidak mencukupi untuk order jual", Collections.emptyList());
            }
        }

        // 2. Cek Halt State
        if (book.getHaltState() == TradingHaltState.SUSPENDED_CIRCUIT_BREAKER) {
            if (order.getType() == OrderType.LIMIT) {
                book.addLimitOrder(order);
                return new OrderResult(order, true, "Pasar sedang Halt/Suspensi; Order limit masuk antrean lelang", Collections.emptyList());
            } else {
                order.setStatus(OrderStatus.REJECTED);
                if (order.isBuy()) account.releaseReservedCash(order.getReservedMoney());
                return new OrderResult(order, false, "Market order ditolak saat instrumen sedang Halt/Suspensi", Collections.emptyList());
            }
        }

        // 3. Continuous Matching
        List<TradeFill> fills = new ArrayList<>();
        if (order.isBuy()) {
            matchBuyOrder(order, account, instrument, book, ledger, allAccounts, fills, gameTimestamp);
        } else {
            matchSellOrder(order, account, instrument, book, ledger, allAccounts, fills, gameTimestamp);
        }

        // 4. Post-matching handling
        if (order.getRemainingQuantity() > 0) {
            if (order.getType() == OrderType.LIMIT) {
                book.addLimitOrder(order);
            } else if (order.getType() != OrderType.LIMIT) {
                // Sisa market order dibatalkan (Immediate or Cancel)
                order.cancel();
                if (order.isBuy()) {
                    // Hitung dan rilis sisa reservasi kas
                    Money remainingReserved = order.getReservedMoney();
                    if (remainingReserved != null && remainingReserved.isPositive()) {
                        account.releaseReservedCash(remainingReserved);
                        order.setReservedMoney(Money.zero(currency));
                    }
                }
            }
        }

        return new OrderResult(order, true, "Order berhasil diproses", fills);
    }

    private String validateCounterparties(Order incoming, Account account, Instrument instrument, OrderBook book,
                                          Map<String, Account> accounts) {
        Map<BigDecimal, Queue<Order>> levels = incoming.isBuy() ? book.getAsks() : book.getBids();
        Map<String, Long> selling = new HashMap<>();
        Map<String, Money> buying = new HashMap<>();
        long remaining = incoming.getRemainingQuantity();
        try {
            for (Map.Entry<BigDecimal, Queue<Order>> level : levels.entrySet()) {
                if (incoming.getType() == OrderType.LIMIT && (incoming.isBuy()
                        ? level.getKey().compareTo(incoming.getLimitPrice().getAmount()) > 0
                        : level.getKey().compareTo(incoming.getLimitPrice().getAmount()) < 0)) break;
                for (Order maker : level.getValue()) {
                    if (maker.getStatus().isFinal()) continue;
                    if (maker.getAccountId().equals(account.getId())) return "Order bersilangan dengan order akun sendiri; batalkan order lama terlebih dahulu";
                    Account counterparty = accounts.get(maker.getAccountId());
                    if (counterparty == null || maker.getRemainingQuantity() <= 0
                            || !instrument.getId().equals(maker.getInstrumentId())) return "Keadaan pihak lawan tidak valid; order belum diterapkan";
                    long quantity = Math.min(remaining, maker.getRemainingQuantity());
                    Account seller = incoming.isBuy() ? counterparty : account;
                    Position position = seller.getPosition(instrument.getId());
                    long sold = Math.addExact(selling.getOrDefault(seller.getId(), 0L), quantity);
                    if (position == null || position.getCurrency() != instrument.getCurrency() || position.getQuantity() < sold)
                        return "Kepemilikan pihak penjual tidak cukup; order belum diterapkan";
                    selling.put(seller.getId(), sold);
                    Account buyer = incoming.isBuy() ? account : counterparty;
                    Position buyerPosition = buyer.getPosition(instrument.getId());
                    if (buyerPosition != null) {
                        if (buyerPosition.getCurrency() != instrument.getCurrency()) return "Mata uang posisi pembeli tidak sesuai";
                        Math.addExact(buyerPosition.getQuantity(), incoming.isBuy() ? incoming.getTotalQuantity() - remaining + quantity : quantity);
                    }
                    Money value = new Money(level.getKey().multiply(BigDecimal.valueOf(quantity)), instrument.getCurrency());
                    Money needed = value.add(new Money(value.getAmount().multiply(BUY_FEE_RATE), instrument.getCurrency()));
                    if (!incoming.isBuy()) {
                        if (maker.getReservedMoney() == null || maker.getReservedMoney().isLessThan(needed))
                            return "Reservasi order pihak pembeli tidak cukup; order belum diterapkan";
                        Money total = buying.getOrDefault(buyer.getId(), Money.zero(instrument.getCurrency())).add(needed);
                        if (buyer.getReservedCash(instrument.getCurrency()).isLessThan(total))
                            return "Reservasi akun pihak pembeli tidak cukup; order belum diterapkan";
                        buying.put(buyer.getId(), total);
                    }
                    remaining -= quantity;
                    if (remaining == 0) return null;
                }
            }
        } catch (ArithmeticException invalidQuantity) {
            return "Kuantitas settlement melampaui batas; order belum diterapkan";
        }
        return null;
    }

    private void matchBuyOrder(Order buyOrder, Account buyer, Instrument instrument,
                               OrderBook book, EconomyLedger ledger,
                               Map<String, Account> allAccounts,
                               List<TradeFill> fills, long timestamp) {

        Currency cur = instrument.getCurrency();
        Iterator<Map.Entry<BigDecimal, Queue<Order>>> it = book.getAsks().entrySet().iterator();

        while (it.hasNext() && buyOrder.getRemainingQuantity() > 0) {
            Map.Entry<BigDecimal, Queue<Order>> entry = it.next();
            BigDecimal askPrice = entry.getKey();

            // Jika buy limit dan ask price > limit price, hentikan pencocokan
            if (buyOrder.getType() == OrderType.LIMIT && buyOrder.getLimitPrice() != null) {
                if (askPrice.compareTo(buyOrder.getLimitPrice().getAmount()) > 0) {
                    break;
                }
            }

            Queue<Order> queue = entry.getValue();
            while (!queue.isEmpty() && buyOrder.getRemainingQuantity() > 0) {
                Order makerSell = queue.peek();
                if (makerSell.getStatus().isFinal()) {
                    queue.poll();
                    continue;
                }

                long matchQty = Math.min(buyOrder.getRemainingQuantity(), makerSell.getRemainingQuantity());
                Money execPrice = new Money(askPrice, cur);
                Money tradeValue = execPrice.multiply((double) matchQty);
                Money buyFee = new Money(tradeValue.getAmount().multiply(BUY_FEE_RATE), cur);
                Money sellFee = new Money(tradeValue.getAmount().multiply(SELL_FEE_RATE), cur);

                Account seller = allAccounts.get(makerSell.getAccountId());
                if (seller == null) {
                    queue.poll();
                    continue;
                }

                // 1. Settlement Buyer
                Money releasedFromReserve = tradeValue.add(buyFee);
                buyer.releaseReservedCash(releasedFromReserve);
                if (buyOrder.getReservedMoney() != null && buyOrder.getReservedMoney().isGreaterThanOrEqual(releasedFromReserve)) {
                    buyOrder.setReservedMoney(buyOrder.getReservedMoney().subtract(releasedFromReserve));
                }

                Position buyerPos = buyer.getPosition(instrument.getId());
                if (buyerPos == null) {
                    buyerPos = new Position(instrument.getTicker(), instrument.getExchange().name(), cur, 0, BigDecimal.ZERO, askPrice);
                    buyer.addPosition(buyerPos);
                }
                buyerPos.addBuy(matchQty, askPrice, buyFee);

                // Catat di ledger buyer (otomatis mendebit kas buyer)
                ledger.recordMutation("BUY-FILL-" + System.nanoTime(), buyer, LedgerEntryType.TRADE_BUY, tradeValue, false, timestamp, "Beli " + matchQty + " " + instrument.getTicker());
                ledger.recordMutation("BUY-FEE-" + System.nanoTime(), buyer, LedgerEntryType.TRANSACTION_FEE, buyFee, false, timestamp, "Biaya beli " + instrument.getTicker());

                // 2. Settlement Seller
                Position sellerPos = seller.getPosition(instrument.getId());
                Money realizedNet = netSaleResult(sellerPos, matchQty, askPrice, sellFee);
                sellerPos.addSell(matchQty, askPrice);

                // Catat di ledger seller (otomatis mengkredit hasil jual dan mendebit fee)
                ledger.recordMutation("SELL-FILL-" + System.nanoTime(), seller, LedgerEntryType.TRADE_SELL, tradeValue, true, timestamp, "Jual " + matchQty + " " + instrument.getTicker());
                ledger.recordMutation("SELL-FEE-" + System.nanoTime(), seller, LedgerEntryType.TRANSACTION_FEE, sellFee, false, timestamp, "Biaya jual " + instrument.getTicker());

                // Update orders & fills
                buyOrder.applyFill(matchQty);
                releaseCompletedBuyReservation(buyOrder, buyer);
                makerSell.applyFill(matchQty);

                TradeFill fill = new TradeFill(
                        "FILL-" + System.nanoTime(),
                        buyOrder.getId(), makerSell.getId(),
                        buyer.getId(), seller.getId(),
                        instrument.getId(), execPrice, matchQty, timestamp
                ).withSellerRealizedNetPnL(realizedNet);
                fills.add(fill);
                book.recordFill(fill);
                recordMarketPrice(instrument, execPrice, matchQty, allAccounts.values());

                if (makerSell.getRemainingQuantity() == 0) {
                    queue.poll();
                }
            }

            if (queue.isEmpty()) {
                it.remove();
            }
        }
    }

    private void matchSellOrder(Order sellOrder, Account seller, Instrument instrument,
                                OrderBook book, EconomyLedger ledger,
                                Map<String, Account> allAccounts,
                                List<TradeFill> fills, long timestamp) {

        Currency cur = instrument.getCurrency();
        Iterator<Map.Entry<BigDecimal, Queue<Order>>> it = book.getBids().entrySet().iterator();

        while (it.hasNext() && sellOrder.getRemainingQuantity() > 0) {
            Map.Entry<BigDecimal, Queue<Order>> entry = it.next();
            BigDecimal bidPrice = entry.getKey();

            // Jika sell limit dan bid price < limit price, hentikan pencocokan
            if (sellOrder.getType() == OrderType.LIMIT && sellOrder.getLimitPrice() != null) {
                if (bidPrice.compareTo(sellOrder.getLimitPrice().getAmount()) < 0) {
                    break;
                }
            }

            Queue<Order> queue = entry.getValue();
            while (!queue.isEmpty() && sellOrder.getRemainingQuantity() > 0) {
                Order makerBuy = queue.peek();
                if (makerBuy.getStatus().isFinal()) {
                    queue.poll();
                    continue;
                }

                long matchQty = Math.min(sellOrder.getRemainingQuantity(), makerBuy.getRemainingQuantity());
                Money execPrice = new Money(bidPrice, cur);
                Money tradeValue = execPrice.multiply((double) matchQty);
                Money buyFee = new Money(tradeValue.getAmount().multiply(BUY_FEE_RATE), cur);
                Money sellFee = new Money(tradeValue.getAmount().multiply(SELL_FEE_RATE), cur);

                Account buyer = allAccounts.get(makerBuy.getAccountId());
                if (buyer == null) {
                    queue.poll();
                    continue;
                }

                // 1. Settlement Buyer
                Money releasedFromReserve = tradeValue.add(buyFee);
                buyer.releaseReservedCash(releasedFromReserve);
                if (makerBuy.getReservedMoney() != null && makerBuy.getReservedMoney().isGreaterThanOrEqual(releasedFromReserve)) {
                    makerBuy.setReservedMoney(makerBuy.getReservedMoney().subtract(releasedFromReserve));
                }

                Position buyerPos = buyer.getPosition(instrument.getId());
                if (buyerPos == null) {
                    buyerPos = new Position(instrument.getTicker(), instrument.getExchange().name(), cur, 0, BigDecimal.ZERO, bidPrice);
                    buyer.addPosition(buyerPos);
                }
                buyerPos.addBuy(matchQty, bidPrice, buyFee);

                ledger.recordMutation("BUY-FILL-" + System.nanoTime(), buyer, LedgerEntryType.TRADE_BUY, tradeValue, false, timestamp, "Beli " + matchQty + " " + instrument.getTicker());
                ledger.recordMutation("BUY-FEE-" + System.nanoTime(), buyer, LedgerEntryType.TRANSACTION_FEE, buyFee, false, timestamp, "Biaya beli " + instrument.getTicker());

                // 2. Settlement Seller
                Position sellerPos = seller.getPosition(instrument.getId());
                Money realizedNet = netSaleResult(sellerPos, matchQty, bidPrice, sellFee);
                sellerPos.addSell(matchQty, bidPrice);

                ledger.recordMutation("SELL-FILL-" + System.nanoTime(), seller, LedgerEntryType.TRADE_SELL, tradeValue, true, timestamp, "Jual " + matchQty + " " + instrument.getTicker());
                ledger.recordMutation("SELL-FEE-" + System.nanoTime(), seller, LedgerEntryType.TRANSACTION_FEE, sellFee, false, timestamp, "Biaya jual " + instrument.getTicker());

                sellOrder.applyFill(matchQty);
                makerBuy.applyFill(matchQty);
                releaseCompletedBuyReservation(makerBuy, buyer);

                TradeFill fill = new TradeFill(
                        "FILL-" + System.nanoTime(),
                        makerBuy.getId(), sellOrder.getId(),
                        buyer.getId(), seller.getId(),
                        instrument.getId(), execPrice, matchQty, timestamp
                ).withSellerRealizedNetPnL(realizedNet);
                fills.add(fill);
                book.recordFill(fill);
                recordMarketPrice(instrument, execPrice, matchQty, allAccounts.values());

                if (makerBuy.getRemainingQuantity() == 0) {
                    queue.poll();
                }
            }

            if (queue.isEmpty()) {
                it.remove();
            }
        }
    }

    private Money netSaleResult(Position position, long quantity, BigDecimal executionPrice, Money saleFee) {
        BigDecimal cost = position.getAverageCost().multiply(BigDecimal.valueOf(quantity));
        Money allocatedBuyFee = position.previewAcquisitionFeeAllocation(quantity);
        return new Money(executionPrice.multiply(BigDecimal.valueOf(quantity)).subtract(cost), position.getCurrency())
                .subtract(saleFee).subtract(allocatedBuyFee);
    }

    private void recordMarketPrice(Instrument instrument, Money price, long quantity, Collection<Account> accounts) {
        instrument.updatePrice(price, quantity);
        for (Account account : accounts) {
            Position position = account.getPosition(instrument.getId());
            if (position != null) position.updateMarketPrice(price.getAmount());
        }
    }

    private void releaseCompletedBuyReservation(Order order, Account buyer) {
        Money reserved = order.getReservedMoney();
        if (order.getRemainingQuantity() == 0 && reserved != null && reserved.isPositive()) {
            buyer.releaseReservedCash(reserved);
            order.setReservedMoney(Money.zero(reserved.getCurrency()));
        }
    }

    /**
     * Pembatalan order yang belum terisi penuh.
     */
    public synchronized boolean cancelOrder(Order order, Account account, OrderBook book) {
        if (order.getStatus().isFinal()) return false;
        boolean removed = book.cancelOrder(order.getId());
        if (removed && order.isBuy()) {
            Money remainingReserved = order.getReservedMoney();
            if (remainingReserved != null && remainingReserved.isPositive()) {
                account.releaseReservedCash(remainingReserved);
                order.setReservedMoney(Money.zero(order.getLimitPrice() != null ? order.getLimitPrice().getCurrency() : Currency.IDR));
            }
        }
        return removed;
    }

    private boolean validateAuctionSettlement(Instrument instrument, Money price, Queue<Order> bids,
                                              Queue<Order> asks, Map<String, Account> accounts) {
        Iterator<Order> buyers = bids.iterator();
        Iterator<Order> sellers = asks.iterator();
        Order buy = null;
        Order sell = null;
        long buyRemaining = 0;
        long sellRemaining = 0;
        Map<String, Long> sold = new HashMap<>();
        Map<String, Long> bought = new HashMap<>();
        Map<String, Money> accountCosts = new HashMap<>();
        Map<Order, Money> orderCosts = new IdentityHashMap<>();
        Currency currency = instrument.getCurrency();
        try {
            while ((buyRemaining > 0 || buyers.hasNext()) && (sellRemaining > 0 || sellers.hasNext())) {
                if (buyRemaining == 0) {
                    buy = buyers.next();
                    buyRemaining = buy.getRemainingQuantity();
                }
                if (sellRemaining == 0) {
                    sell = sellers.next();
                    sellRemaining = sell.getRemainingQuantity();
                }
                Account buyer = accounts.get(buy.getAccountId());
                Account seller = accounts.get(sell.getAccountId());
                if (buyer == null || seller == null || buyer.getId().equals(seller.getId())
                        || buy.getStatus().isFinal() || sell.getStatus().isFinal()
                        || buyRemaining <= 0 || sellRemaining <= 0 || !buy.isBuy() || sell.isBuy()
                        || buy.getType() != OrderType.LIMIT || sell.getType() != OrderType.LIMIT
                        || validateOrder(buy, buyer, instrument) != null || validateOrder(sell, seller, instrument) != null
                        || buy.getLimitPrice().isLessThan(price) || price.isLessThan(sell.getLimitPrice())) return false;
                long quantity = Math.min(buyRemaining, sellRemaining);
                Position sellerPosition = seller.getPosition(instrument.getId());
                long totalSold = Math.addExact(sold.getOrDefault(seller.getId(), 0L), quantity);
                if (sellerPosition == null || sellerPosition.getCurrency() != currency || sellerPosition.getQuantity() < totalSold) return false;
                sold.put(seller.getId(), totalSold);
                Position buyerPosition = buyer.getPosition(instrument.getId());
                if (buyerPosition != null && buyerPosition.getCurrency() != currency) return false;
                long totalBought = Math.addExact(bought.getOrDefault(buyer.getId(), 0L), quantity);
                Math.addExact(buyerPosition == null ? 0 : buyerPosition.getQuantity(), totalBought);
                bought.put(buyer.getId(), totalBought);
                Money value = new Money(price.getAmount().multiply(BigDecimal.valueOf(quantity)), currency);
                Money cost = value.add(new Money(value.getAmount().multiply(BUY_FEE_RATE), currency));
                Money orderCost = orderCosts.getOrDefault(buy, Money.zero(currency)).add(cost);
                Money accountCost = accountCosts.getOrDefault(buyer.getId(), Money.zero(currency)).add(cost);
                if (buy.getReservedMoney() == null || buy.getReservedMoney().getCurrency() != currency
                        || buy.getReservedMoney().isLessThan(orderCost) || buyer.getReservedCash(currency).isLessThan(accountCost)) return false;
                orderCosts.put(buy, orderCost);
                accountCosts.put(buyer.getId(), accountCost);
                buyRemaining -= quantity;
                sellRemaining -= quantity;
            }
        } catch (ArithmeticException invalidQuantity) {
            return false;
        }
        return true;
    }

    /**
     * Menjalankan Call Auction lelang pembukaan kembali saat pemulihan dari Halt/Suspensi.
     * Menentukan harga kliring tunggal (Single Equilibrium Price) dan mengeksekusi order match.
     */
    public synchronized int runCallAuction(Instrument instrument, OrderBook book,
                                           EconomyLedger ledger, Map<String, Account> allAccounts,
                                           long gameTimestamp) {

        // Cari harga kliring tunggal yang memaksimalkan volume
        TreeMap<BigDecimal, Queue<Order>> bids = book.getBids();
        TreeMap<BigDecimal, Queue<Order>> asks = book.getAsks();

        if (bids.isEmpty() || asks.isEmpty()) {
            book.setHaltState(TradingHaltState.NORMAL);
            return 0;
        }

        BigDecimal bestBid = bids.firstKey();
        BigDecimal bestAsk = asks.firstKey();

        if (bestBid.compareTo(bestAsk) < 0) {
            // Tidak ada perjumpaan harga
            book.setHaltState(TradingHaltState.NORMAL);
            return 0;
        }

        // Equilibrium price: rata-rata best bid dan best ask yang bersilangan
        BigDecimal clearingPrice = bestBid.add(bestAsk).divide(BigDecimal.valueOf(2), instrument.getCurrency().getDecimalPlaces(), RoundingMode.HALF_UP);
        Money clearingMoney = new Money(clearingPrice, instrument.getCurrency());

        int matchesCount = 0;
        // Jalankan eksekusi pada clearing price
        Queue<Order> buyQueue = bids.get(bestBid);
        Queue<Order> sellQueue = asks.get(bestAsk);
        if (!validateAuctionSettlement(instrument, clearingMoney, buyQueue, sellQueue, allAccounts)) return 0;

        while (buyQueue != null && !buyQueue.isEmpty() && sellQueue != null && !sellQueue.isEmpty()) {
            Order buyerOrder = buyQueue.peek();
            Order sellerOrder = sellQueue.peek();

            long matchQty = Math.min(buyerOrder.getRemainingQuantity(), sellerOrder.getRemainingQuantity());
            Account buyer = allAccounts.get(buyerOrder.getAccountId());
            Account seller = allAccounts.get(sellerOrder.getAccountId());

            if (buyer != null && seller != null) {
                Money tradeValue = clearingMoney.multiply((double) matchQty);
                Money buyFee = new Money(tradeValue.getAmount().multiply(BUY_FEE_RATE), instrument.getCurrency());
                Money sellFee = new Money(tradeValue.getAmount().multiply(SELL_FEE_RATE), instrument.getCurrency());

                Money consumedReservation = tradeValue.add(buyFee);
                buyer.releaseReservedCash(consumedReservation);
                buyerOrder.setReservedMoney(buyerOrder.getReservedMoney().subtract(consumedReservation));

                Position bPos = buyer.getPosition(instrument.getId());
                if (bPos == null) {
                    bPos = new Position(instrument.getTicker(), instrument.getExchange().name(), instrument.getCurrency(), 0, BigDecimal.ZERO, clearingPrice);
                    buyer.addPosition(bPos);
                }
                bPos.addBuy(matchQty, clearingPrice, buyFee);

                ledger.recordMutation("AUCTION-BUY-" + System.nanoTime(), buyer, LedgerEntryType.TRADE_BUY, tradeValue, false, gameTimestamp, "Lelang Beli " + matchQty + " " + instrument.getTicker());
                ledger.recordMutation("AUCTION-BUYFEE-" + System.nanoTime(), buyer, LedgerEntryType.TRANSACTION_FEE, buyFee, false, gameTimestamp, "Biaya Lelang " + instrument.getTicker());

                Position sPos = seller.getPosition(instrument.getId());
                Money realizedNet = netSaleResult(sPos, matchQty, clearingPrice, sellFee);
                sPos.addSell(matchQty, clearingPrice);

                ledger.recordMutation("AUCTION-SELL-" + System.nanoTime(), seller, LedgerEntryType.TRADE_SELL, tradeValue, true, gameTimestamp, "Lelang Jual " + matchQty + " " + instrument.getTicker());
                ledger.recordMutation("AUCTION-SELLFEE-" + System.nanoTime(), seller, LedgerEntryType.TRANSACTION_FEE, sellFee, false, gameTimestamp, "Biaya Lelang " + instrument.getTicker());

                buyerOrder.applyFill(matchQty);
                releaseCompletedBuyReservation(buyerOrder, buyer);
                sellerOrder.applyFill(matchQty);

                TradeFill fill = new TradeFill(
                        "AUCTION-FILL-" + System.nanoTime(),
                        buyerOrder.getId(), sellerOrder.getId(),
                        buyer.getId(), seller.getId(),
                        instrument.getId(), clearingMoney, matchQty, gameTimestamp
                ).withSellerRealizedNetPnL(realizedNet);
                book.recordFill(fill);
                recordMarketPrice(instrument, clearingMoney, matchQty, allAccounts.values());
                matchesCount++;

                if (buyerOrder.getRemainingQuantity() == 0) buyQueue.poll();
                if (sellerOrder.getRemainingQuantity() == 0) sellQueue.poll();
            } else {
                break;
            }
        }

        book.setHaltState(TradingHaltState.NORMAL);
        return matchesCount;
    }
}
