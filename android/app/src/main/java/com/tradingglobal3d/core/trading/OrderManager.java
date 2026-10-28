package com.tradingglobal3d.core.trading;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.npc.MarketEngine;

import java.util.*;

/**
 * Pengelola Pesanan Aktif, Riwayat, dan Pemicu Stop-Loss / Take-Profit.
 */
public final class OrderManager {
    private final Map<String, List<Order>> ordersByAccount = new HashMap<>();
    private final List<Order> activeTriggerOrders = new ArrayList<>();

    public synchronized void registerOrder(Order order) {
        ordersByAccount.computeIfAbsent(order.getAccountId(), k -> new ArrayList<>()).add(order);
        if (order.getStatus() == OrderStatus.PENDING_TRIGGER) {
            activeTriggerOrders.add(order);
        }
    }

    public synchronized void reset() {
        ordersByAccount.clear();
        activeTriggerOrders.clear();
    }

    public synchronized org.json.JSONArray exportState() throws org.json.JSONException {
        org.json.JSONArray result = new org.json.JSONArray();
        for (List<Order> list : ordersByAccount.values()) for (Order o : list) {
            result.put(new org.json.JSONObject().put("id", o.getId()).put("requestId", o.getRequestId())
                    .put("accountId", o.getAccountId()).put("instrumentId", o.getInstrumentId())
                    .put("side", o.getSide().name()).put("type", o.getType().name())
                    .put("limit", o.getLimitPrice() == null ? org.json.JSONObject.NULL : o.getLimitPrice().getAmount().toPlainString())
                    .put("trigger", o.getTriggerPrice() == null ? org.json.JSONObject.NULL : o.getTriggerPrice().getAmount().toPlainString())
                    .put("quantity", o.getTotalQuantity()).put("filled", o.getFilledQuantity()).put("createdAt", o.getCreatedAt())
                    .put("status", o.getStatus().name()).put("feeDiscountPercent", o.getFeeDiscountPercent())
                    .put("reserved", o.getReservedMoney() == null ? org.json.JSONObject.NULL : o.getReservedMoney().getAmount().toPlainString()));
        }
        return result;
    }

    public synchronized void restoreState(org.json.JSONArray saved, InstrumentCatalog catalog, MarketEngine market) throws org.json.JSONException {
        ordersByAccount.clear(); activeTriggerOrders.clear();
        for (int i = 0; i < saved.length(); i++) {
            org.json.JSONObject p = saved.getJSONObject(i);
            Instrument instrument = catalog.getById(p.getString("instrumentId"));
            if (instrument == null) throw new IllegalArgumentException("Instrumen order save tidak tersedia");
            com.tradingglobal3d.core.model.Currency c = instrument.getCurrency();
            Order order = new Order(p.getString("id"), p.getString("requestId"), p.getString("accountId"), instrument.getId(),
                    OrderSide.valueOf(p.getString("side")), OrderType.valueOf(p.getString("type")),
                    p.isNull("limit") ? null : new Money(new java.math.BigDecimal(p.getString("limit")), c),
                    p.isNull("trigger") ? null : new Money(new java.math.BigDecimal(p.getString("trigger")), c),
                    p.getLong("quantity"), p.getLong("createdAt"),
                    p.isNull("reserved") ? null : new Money(new java.math.BigDecimal(p.getString("reserved")), c));
            order.setFeeDiscountPercent(p.optDouble("feeDiscountPercent", 0.0));
            order.applyFill(p.getLong("filled"));
            order.setStatus(OrderStatus.valueOf(p.getString("status")));
            registerOrder(order);
            if (!order.getStatus().isFinal() && order.getStatus() != OrderStatus.PENDING_TRIGGER)
                market.getOrCreateBook(instrument).addLimitOrder(order);
        }
    }

    public synchronized boolean cancelPendingTrigger(String requestId) {
        Order order = getOrderByRequest(requestId);
        if (order == null || order.getStatus() != OrderStatus.PENDING_TRIGGER) return false;
        order.cancel();
        activeTriggerOrders.remove(order);
        return true;
    }

    public synchronized Order getOrderByRequest(String requestId) {
        for (List<Order> orders : ordersByAccount.values()) {
            for (Order order : orders) {
                if (order.getRequestId().equals(requestId)) return order;
            }
        }
        return null;
    }

    public synchronized List<Order> getOrdersForAccount(String accountId) {
        return Collections.unmodifiableList(ordersByAccount.getOrDefault(accountId, Collections.emptyList()));
    }

    public synchronized List<Order> getAllOrders() {
        List<Order> all = new ArrayList<>();
        for (List<Order> list : ordersByAccount.values()) all.addAll(list);
        return Collections.unmodifiableList(all);
    }

    public synchronized List<Order> getActiveOrdersForAccount(String accountId) {
        List<Order> list = new ArrayList<>();
        for (Order o : ordersByAccount.getOrDefault(accountId, Collections.emptyList())) {
            if (!o.getStatus().isFinal()) {
                list.add(o);
            }
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Memeriksa dan memicu order Stop-Loss dan Take-Profit saat harga pasar bergerak.
     */
    public synchronized List<Order> checkAndActivateTriggers(
            Instrument instrument,
            MarketEngine marketEngine,
            EconomyLedger ledger,
            Map<String, Account> allAccounts,
            long gameTimestamp) {

        List<Order> activated = new ArrayList<>();
        Iterator<Order> it = activeTriggerOrders.iterator();
        Money currentPrice = instrument.getCurrentPrice();

        while (it.hasNext()) {
            Order o = it.next();
            if (!o.getInstrumentId().equals(instrument.getId())) {
                continue;
            }
            if (o.getStatus() != OrderStatus.PENDING_TRIGGER) {
                it.remove();
                continue;
            }

            boolean triggerMet = false;
            if (o.getType() == OrderType.STOP_LOSS) {
                // Stop loss sell dipicu jika harga pasar <= triggerPrice
                if (o.isSell() && currentPrice.compareTo(o.getTriggerPrice()) <= 0) {
                    triggerMet = true;
                } else if (o.isBuy() && currentPrice.compareTo(o.getTriggerPrice()) >= 0) {
                    triggerMet = true;
                }
            } else if (o.getType() == OrderType.TAKE_PROFIT) {
                // Take profit sell dipicu jika harga pasar >= triggerPrice
                if (o.isSell() && currentPrice.compareTo(o.getTriggerPrice()) >= 0) {
                    triggerMet = true;
                } else if (o.isBuy() && currentPrice.compareTo(o.getTriggerPrice()) <= 0) {
                    triggerMet = true;
                }
            }

            if (triggerMet && !marketEngine.isHalted(instrument.getId())) {
                o.setStatus(OrderStatus.OPEN);
                it.remove();
                activated.add(o);

                Account acc = allAccounts.get(o.getAccountId());
                if (acc != null) {
                    marketEngine.submitOrder(o, acc, instrument, ledger, allAccounts, gameTimestamp);
                }
            }
        }
        return activated;
    }
}
