package com.tradingglobal3d.core.npc;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.trading.*;

import java.util.*;

/**
 * Orkestrator Pasar Internal Game.
 * Mengelola antrean OrderBook seluruh instrumen, interaksi NPC agen,
 * mekanisme MatchingEngine, dan pembatasan Halt/Suspensi.
 */
public final class MarketEngine {
    private final Map<String, OrderBook> books = new HashMap<>();
    private final MatchingEngine matchingEngine = new MatchingEngine();
    private final List<NpcAgent> npcAgents = new ArrayList<>();

    public synchronized void reset() {
        books.clear();
        npcAgents.clear();
    }

    public synchronized OrderBook getOrCreateBook(Instrument instrument) {
        return books.computeIfAbsent(instrument.getId(), id -> new OrderBook(id, instrument.getCurrentPrice()));
    }

    public synchronized void clearState() {
        books.clear(); npcAgents.clear();
    }

    public synchronized List<Order> getRestingOrders() {
        List<Order> result = new ArrayList<>();
        for (OrderBook book : books.values()) {
            for (Queue<Order> level : book.getBids().values())
                for (Order o : level) if (!o.getStatus().isFinal()) result.add(o);
            for (Queue<Order> level : book.getAsks().values())
                for (Order o : level) if (!o.getStatus().isFinal()) result.add(o);
        }
        return result;
    }

    public synchronized org.json.JSONArray exportState() throws org.json.JSONException {
        org.json.JSONArray saved = new org.json.JSONArray();
        for (OrderBook book : books.values()) {
            org.json.JSONObject item = new org.json.JSONObject().put("instrumentId", book.getInstrumentId())
                    .put("halt", book.getHaltState().name())
                    .put("last", book.getLastTradedPrice() == null ? org.json.JSONObject.NULL : book.getLastTradedPrice().getAmount().toPlainString());
            for (String side : Arrays.asList("bids", "asks")) {
                org.json.JSONArray queue = new org.json.JSONArray();
                for (Queue<Order> level : (side.equals("bids") ? book.getBids() : book.getAsks()).values())
                    for (Order order : level) if (!order.getStatus().isFinal()) queue.put(order.getRequestId());
                item.put(side, queue);
            }
            org.json.JSONArray fills = new org.json.JSONArray();
            for (TradeFill fill : book.getTradeHistory()) fills.put(new org.json.JSONObject()
                    .put("id", fill.getFillId()).put("buyOrder", fill.getBuyOrderId()).put("sellOrder", fill.getSellOrderId())
                    .put("buyer", fill.getBuyAccountId()).put("seller", fill.getSellAccountId())
                    .put("price", fill.getPrice().getAmount().toPlainString()).put("quantity", fill.getQuantity()).put("timestamp", fill.getTimestamp())
                    .put("sellerRealizedNet", fill.getSellerRealizedNetPnL() == null ? org.json.JSONObject.NULL : fill.getSellerRealizedNetPnL().getAmount().toPlainString()));
            saved.put(item.put("fills", fills));
        }
        return saved;
    }

    public synchronized void restoreState(org.json.JSONArray saved, InstrumentCatalog catalog, OrderManager orders) throws org.json.JSONException {
        Map<String, OrderBook> staged = new HashMap<>();
        Set<String> queued = new HashSet<>();
        for (int i = 0; i < saved.length(); i++) {
            org.json.JSONObject item = saved.getJSONObject(i);
            Instrument instrument = catalog.getById(item.getString("instrumentId"));
            if (instrument == null || staged.containsKey(instrument.getId())) throw new IllegalArgumentException("Buku save tidak dikenal atau duplikat");
            OrderBook book = new OrderBook(instrument.getId());
            book.setHaltState(TradingHaltState.valueOf(item.getString("halt")));
            if (!item.isNull("last")) {
                com.tradingglobal3d.core.model.Money price = new com.tradingglobal3d.core.model.Money(new java.math.BigDecimal(item.getString("last")), instrument.getCurrency());
                if (!price.isPositive()) throw new IllegalArgumentException("Harga buku save tidak valid");
                book.setLastTradedPrice(price);
            }
            for (String side : Arrays.asList("bids", "asks")) {
                org.json.JSONArray queue = item.getJSONArray(side);
                for (int j = 0; j < queue.length(); j++) {
                    Order order = orders.getOrderByRequest(queue.getString(j));
                    if (order == null || !queued.add(order.getId()) || !instrument.getId().equals(order.getInstrumentId())
                            || order.getStatus().isFinal() || order.getStatus() == OrderStatus.PENDING_TRIGGER
                            || order.getLimitPrice() == null || order.isBuy() != side.equals("bids"))
                        throw new IllegalArgumentException("Antrean save tidak valid");
                    book.addLimitOrder(order);
                }
            }
            Set<String> fillIds = new HashSet<>();
            org.json.JSONArray fills = item.getJSONArray("fills");
            for (int j = 0; j < fills.length(); j++) {
                org.json.JSONObject fill = fills.getJSONObject(j);
                com.tradingglobal3d.core.model.Money price = new com.tradingglobal3d.core.model.Money(new java.math.BigDecimal(fill.getString("price")), instrument.getCurrency());
                if (!price.isPositive() || fill.getLong("quantity") <= 0 || !fillIds.add(fill.getString("id")))
                    throw new IllegalArgumentException("Fill save tidak valid");
                book.recordFill(new TradeFill(fill.getString("id"), fill.getString("buyOrder"), fill.getString("sellOrder"),
                        fill.getString("buyer"), fill.getString("seller"), instrument.getId(), price, fill.getLong("quantity"), fill.getLong("timestamp"))
                        .withSellerRealizedNetPnL(fill.isNull("sellerRealizedNet") ? null : new com.tradingglobal3d.core.model.Money(
                                new java.math.BigDecimal(fill.getString("sellerRealizedNet")), instrument.getCurrency())));
            }
            if (fills.length() > 0 && (item.isNull("last") || !book.getLastTradedPrice().getAmount().toPlainString().equals(item.getString("last"))))
                throw new IllegalArgumentException("Harga terakhir dan fill save tidak konsisten");
            staged.put(instrument.getId(), book);
        }
        for (Order order : getRestingOrders()) if (!queued.contains(order.getId())) throw new IllegalArgumentException("Order aktif hilang dari antrean save");
        books.clear(); books.putAll(staged);
    }

    public synchronized OrderBook getBook(String instrumentId) {
        return books.get(instrumentId);
    }

    public synchronized void registerNpcAgent(NpcAgent agent) {
        npcAgents.add(agent);
    }

    public synchronized List<NpcAgent> getNpcAgents() {
        return Collections.unmodifiableList(npcAgents);
    }

    public synchronized MatchingEngine getMatchingEngine() {
        return matchingEngine;
    }

    /**
     * Memproses order dari siapa pun (pemain, klien, atau NPC) menggunakan matching engine bersama.
     */
    public synchronized MatchingEngine.OrderResult submitOrder(
            Order order,
            Account account,
            Instrument instrument,
            EconomyLedger ledger,
            Map<String, Account> allAccounts,
            long gameTimestamp) {

        OrderBook book = getOrCreateBook(instrument);
        return matchingEngine.submitOrder(order, account, instrument, book, ledger, allAccounts, gameTimestamp);
    }

    /**
     * Pembatalan order yang masih terbuka di buku antrean.
     */
    public synchronized boolean cancelOrder(Order order, Account account) {
        OrderBook book = books.get(order.getInstrumentId());
        if (book == null) return false;
        return matchingEngine.cancelOrder(order, account, book);
    }

    /**
     * Menetapkan status Halt/Suspensi pada instrumen tertentu.
     */
    public synchronized void haltTrading(String instrumentId) {
        OrderBook book = books.computeIfAbsent(instrumentId, k -> new OrderBook(instrumentId));
        book.setHaltState(TradingHaltState.SUSPENDED_CIRCUIT_BREAKER);
    }

    public synchronized boolean isHalted(String instrumentId) {
        OrderBook book = books.get(instrumentId);
        return book != null && book.getHaltState() != TradingHaltState.NORMAL;
    }

    public synchronized void resumeTrading(String instrumentId) {
        OrderBook book = books.computeIfAbsent(instrumentId, k -> new OrderBook(instrumentId));
        book.setHaltState(TradingHaltState.NORMAL);
    }

    /**
     * Memulihkan dari Halt dengan menjalankan lelang Call Auction.
     */
    public synchronized int resumeFromHaltWithAuction(
            Instrument instrument,
            EconomyLedger ledger,
            Map<String, Account> allAccounts,
            long gameTimestamp) {

        OrderBook book = getOrCreateBook(instrument);
        return matchingEngine.runCallAuction(instrument, book, ledger, allAccounts, gameTimestamp);
    }

    /**
     * Tick pasar berkala: agen NPC mengevaluasi pasar dan memperbarui likuiditas antrean.
     */
    public synchronized void tick(
            long gameTimestamp,
            InstrumentCatalog catalog,
            EconomyLedger ledger,
            Map<String, Account> allAccounts) {

        for (Instrument inst : catalog.getActiveUniverse()) {
            OrderBook book = getOrCreateBook(inst);
            if (book.getHaltState() != TradingHaltState.NORMAL) {
                continue;
            }

            for (NpcAgent npc : npcAgents) {
                List<Order> npcOrders = npc.generateOrders(inst, book, gameTimestamp);
                for (Order o : npcOrders) {
                    matchingEngine.submitOrder(o, npc.getAccount(), inst, book, ledger, allAccounts, gameTimestamp);
                }
            }
        }
    }
}
