package com.tradingglobal3d.core.market;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.*;

/**
 * Katalog terpusat untuk instrumen dan indeks pasar.
 * Mendukung pencarian, filter sektor/bursa, pemisahan aktif/pasif,
 * serta manajemen watchlist.
 */
public final class InstrumentCatalog {
    private final Map<String, Instrument> instrumentsById = new LinkedHashMap<>();
    private final Map<String, MarketIndex> indicesById = new LinkedHashMap<>();
    private final Set<String> watchlistIds = new LinkedHashSet<>();

    public synchronized void registerInstrument(Instrument instrument) {
        Objects.requireNonNull(instrument);
        instrumentsById.put(instrument.getId(), instrument);
    }

    public synchronized void registerIndex(MarketIndex index) {
        Objects.requireNonNull(index);
        indicesById.put(index.getId(), index);
    }

    public synchronized Instrument getById(String id) {
        return instrumentsById.get(id);
    }

    public synchronized Instrument getByExchangeAndTicker(MarketExchange exchange, String ticker) {
        String id = exchange.name() + ":" + ticker;
        return instrumentsById.get(id);
    }

    public synchronized List<Instrument> getAllInstruments() {
        return Collections.unmodifiableList(new ArrayList<>(instrumentsById.values()));
    }

    public synchronized List<Instrument> getActiveUniverse() {
        List<Instrument> list = new ArrayList<>();
        for (Instrument inst : instrumentsById.values()) {
            if (inst.isActiveTrading()) {
                list.add(inst);
            }
        }
        return Collections.unmodifiableList(list);
    }

    public synchronized List<MarketIndex> getAllIndices() {
        return Collections.unmodifiableList(new ArrayList<>(indicesById.values()));
    }

    public synchronized MarketIndex getIndex(String id) {
        return indicesById.get(id);
    }

    /**
     * Pencarian multi-kriteria: ticker, nama, bursa, sektor, status aktif.
     */
    public synchronized List<Instrument> search(String query, MarketExchange exchangeFilter, String sectorFilter, Boolean activeOnly) {
        String q = query != null ? query.trim().toUpperCase() : "";
        List<Instrument> results = new ArrayList<>();

        for (Instrument inst : instrumentsById.values()) {
            if (exchangeFilter != null && inst.getExchange() != exchangeFilter) {
                continue;
            }
            if (sectorFilter != null && !sectorFilter.isEmpty() && !inst.getSector().equalsIgnoreCase(sectorFilter)) {
                continue;
            }
            if (activeOnly != null && inst.isActiveTrading() != activeOnly) {
                continue;
            }
            if (!q.isEmpty()) {
                boolean matchTicker = inst.getTicker().toUpperCase().contains(q);
                boolean matchName = inst.getName().toUpperCase().contains(q);
                if (!matchTicker && !matchName) {
                    continue;
                }
            }
            results.add(inst);
        }
        return results;
    }

    // Watchlist Management
    public synchronized boolean addToWatchlist(String instrumentId) {
        if (instrumentsById.containsKey(instrumentId)) {
            return watchlistIds.add(instrumentId);
        }
        return false;
    }

    public synchronized boolean removeFromWatchlist(String instrumentId) {
        return watchlistIds.remove(instrumentId);
    }

    public synchronized boolean isInWatchlist(String instrumentId) {
        return watchlistIds.contains(instrumentId);
    }

    public synchronized List<Instrument> getWatchlist() {
        List<Instrument> list = new ArrayList<>();
        for (String id : watchlistIds) {
            Instrument inst = instrumentsById.get(id);
            if (inst != null) {
                list.add(inst);
            }
        }
        return Collections.unmodifiableList(list);
    }

    public synchronized JSONObject exportState() throws JSONException {
        JSONArray instruments = new JSONArray();
        for (Instrument inst : instrumentsById.values()) instruments.put(new JSONObject()
                .put("id", inst.getId()).put("price", inst.getCurrentPrice().getAmount().toPlainString())
                .put("previous", inst.getPreviousClosePrice().getAmount().toPlainString())
                .put("high", inst.getDayHigh().getAmount().toPlainString())
                .put("low", inst.getDayLow().getAmount().toPlainString()).put("volume", inst.getDayVolume()));
        return new JSONObject().put("instruments", instruments).put("watchlist", new JSONArray(watchlistIds));
    }

    private List<Instrument> parseMarketState(JSONArray saved) throws JSONException {
        if (saved.length() != instrumentsById.size()) throw new IllegalArgumentException("Cakupan katalog save berbeda");
        List<Instrument> staged = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < saved.length(); i++) {
            JSONObject item = saved.getJSONObject(i);
            Instrument source = instrumentsById.get(item.getString("id"));
            if (source == null || !ids.add(source.getId())) throw new IllegalArgumentException("Instrumen save tidak dikenal atau duplikat");
            Currency c = source.getCurrency();
            Instrument copy = new Instrument(source.getId(), source.getTicker(), source.getName(), source.getExchange(),
                    source.getSector(), c, source.getLotSize(), source.getSharesOutstanding(), source.isActiveTrading(), source.getCurrentPrice());
            copy.restoreMarketState(new Money(new BigDecimal(item.getString("price")), c),
                    new Money(new BigDecimal(item.getString("previous")), c),
                    new Money(new BigDecimal(item.getString("high")), c),
                    new Money(new BigDecimal(item.getString("low")), c), item.getLong("volume"));
            staged.add(copy);
        }
        return staged;
    }

    private Set<String> parseWatchlist(JSONArray saved) throws JSONException {
        Set<String> result = new LinkedHashSet<>();
        for (int i = 0; i < saved.length(); i++) {
            String id = saved.getString(i);
            if (!instrumentsById.containsKey(id) || !result.add(id)) throw new IllegalArgumentException("Watchlist save tidak valid");
        }
        return result;
    }

    public synchronized void validateState(JSONObject state) throws JSONException {
        parseMarketState(state.getJSONArray("instruments"));
        parseWatchlist(state.getJSONArray("watchlist"));
    }

    public synchronized void restoreState(JSONObject state) throws JSONException {
        List<Instrument> staged = parseMarketState(state.getJSONArray("instruments"));
        Set<String> watchlist = parseWatchlist(state.getJSONArray("watchlist"));
        for (Instrument saved : staged) instrumentsById.get(saved.getId()).restoreMarketState(saved.getCurrentPrice(),
                saved.getPreviousClosePrice(), saved.getDayHigh(), saved.getDayLow(), saved.getDayVolume());
        watchlistIds.clear(); watchlistIds.addAll(watchlist);
        recalculateAllIndices();
    }

    public synchronized void recalculateAllIndices() {
        for (MarketIndex idx : indicesById.values()) {
            idx.recalculate(instrumentsById);
        }
    }

    /**
     * Memuat universe dari JSON string.
     */
    public static InstrumentCatalog fromJsonString(String jsonStr) throws JSONException {
        InstrumentCatalog catalog = new InstrumentCatalog();
        JSONObject root = new JSONObject(jsonStr);

        JSONArray instArray = root.getJSONArray("instruments");
        for (int i = 0; i < instArray.length(); i++) {
            JSONObject obj = instArray.getJSONObject(i);
            String id = obj.getString("id");
            String ticker = obj.getString("ticker");
            String name = obj.getString("name");
            MarketExchange exchange = MarketExchange.valueOf(obj.getString("exchange"));
            String sector = obj.getString("sector");
            Currency currency = Currency.valueOf(obj.getString("currency"));
            int lotSize = obj.getInt("lotSize");
            long shares = obj.getLong("sharesOutstanding");
            boolean isActive = obj.getBoolean("isActive");
            BigDecimal basePrice = new BigDecimal(obj.get("basePrice").toString());

            Instrument inst = new Instrument(
                    id, ticker, name, exchange, sector, currency,
                    lotSize, shares, isActive, new Money(basePrice, currency)
            );
            catalog.registerInstrument(inst);
        }

        JSONArray idxArray = root.getJSONArray("indices");
        for (int i = 0; i < idxArray.length(); i++) {
            JSONObject obj = idxArray.getJSONObject(i);
            String id = obj.getString("id");
            String ticker = obj.getString("ticker");
            String name = obj.getString("name");
            MarketExchange exchange = MarketExchange.valueOf(obj.getString("exchange"));
            double baseValue = obj.getDouble("baseValue");

            JSONArray membersArr = obj.getJSONArray("members");
            List<String> members = new ArrayList<>();
            for (int m = 0; m < membersArr.length(); m++) {
                members.add(membersArr.getString(m));
            }

            MarketIndex idx = new MarketIndex(
                    id, ticker, name, exchange, baseValue,
                    MarketIndex.CalculationMethod.MARKET_CAP_WEIGHTED, members
            );
            catalog.registerIndex(idx);
        }

        catalog.recalculateAllIndices();
        return catalog;
    }
}
