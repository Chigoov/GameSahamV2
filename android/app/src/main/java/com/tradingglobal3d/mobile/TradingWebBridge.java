package com.tradingglobal3d.mobile;

import android.webkit.JavascriptInterface;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.MarketIndex;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.trading.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Jembatan Komunikasi Native Android WebView (@JavascriptInterface) ke TradingFacade.
 * Mengekspos API bertipe JSON ke frontend 2D portrait.
 */
public final class TradingWebBridge {
    private final TradingFacade facade;
    private MainActivity activity;
    private volatile String clockSyncResult = "{\"success\":true,\"status\":\"IDLE\"}";
    private volatile boolean clockSyncPending;
    private volatile boolean clockRequestedForeground = true;
    private long lastClockSaveNanos;

    public boolean isClockSyncPending() { return clockSyncPending; }
    private final java.util.concurrent.ExecutorService clockWorker = java.util.concurrent.Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "world-clock-reconciliation");
        thread.setDaemon(true);
        return thread;
    });

    @JavascriptInterface
    public synchronized String beginClockSync(boolean foreground) {
        clockRequestedForeground = foreground;
        if (clockSyncPending) return clockSyncResult;
        clockSyncPending = true;
        clockSyncResult = "{\"success\":true,\"status\":\"PENDING\"}";
        clockWorker.execute(() -> {
            String result;
            try {

                if (facade.getClock() == null) {
                    if (!facade.startOrResume()) throw new IllegalStateException("Progres membutuhkan pemulihan");
                }
                boolean lifecycleChanged = facade.getClock().isForeground() != clockRequestedForeground;
                facade.onAppLifecycleChanged(clockRequestedForeground);
                facade.syncClock();
                facade.onAppLifecycleChanged(clockRequestedForeground);
                long nanos = System.nanoTime();
                if (lifecycleChanged || !clockRequestedForeground || lastClockSaveNanos == 0
                        || nanos - lastClockSaveNanos >= 30_000_000_000L) {
                    facade.save();
                    lastClockSaveNanos = nanos;
                }
                result = new JSONObject().put("success", true).put("status", "COMPLETED")
                        .put("snapshot", new JSONObject(facade.getSnapshotJson())).toString();
            } catch (Exception error) {
                try {
                    result = new JSONObject().put("success", false).put("status", "FAILED")
                            .put("message", error.getMessage() == null ? error.toString() : error.getMessage()).toString();
                } catch (JSONException ignored) {
                    result = "{\"success\":false,\"status\":\"FAILED\"}";
                }
            }
            synchronized (TradingWebBridge.this) {
                clockSyncResult = result;
                clockSyncPending = false;
            }
        });
        return "{\"success\":true,\"status\":\"PENDING\"}";
    }

    @JavascriptInterface
    public String getClockSyncStatus() { return clockSyncResult; }

    public TradingWebBridge(TradingFacade facade) {
        this(facade, null);
    }

    public TradingWebBridge(TradingFacade facade, MainActivity activity) {
        this.facade = Objects.requireNonNull(facade);
        this.activity = activity;
    }

    public void attachActivity(MainActivity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public String notifyIpoAlert(String title, String message) {
        if (activity != null) {
            activity.runOnUiThread(() -> activity.triggerIpoNotification(title, message));
            return "{\"success\":true,\"method\":\"NATIVE_NOTIFICATION\"}";
        }
        return "{\"success\":true,\"method\":\"FALLBACK_INBOX_BANNER\"}";
    }

    @JavascriptInterface
    public String getPlatformInfo() {
        return "{\"platform\":\"Android\",\"nativeHost\":true,\"version\":\"1.0.0-2d-portrait\"}";
    }

    @JavascriptInterface
    public String getSnapshot() {
        try {
            return facade.getSnapshotJson();
        } catch (Exception e) {
            try { return new JSONObject().put("success", false).put("error", e.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getSaveMetadata() {
        try {
            return facade.getSaveMetadata().toString();
        } catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String getInstruments(String query, String exchange, String sector, boolean activeOnly) {
        try {
            List<Instrument> list = facade.getCatalog().search(
                    query != null && !query.isEmpty() ? query : null,
                    exchange != null && !exchange.isEmpty() ? com.tradingglobal3d.core.market.MarketExchange.valueOf(exchange) : null,
                    sector != null && !sector.isEmpty() ? sector : null,
                    activeOnly ? Boolean.TRUE : null
            );

            JSONArray array = new JSONArray();
            for (Instrument inst : list) {
                if (facade.getGameMode() == com.tradingglobal3d.core.model.GameMode.HISTORICAL_REPLAY
                        && !facade.getReplayEngine().supportsInstrument(inst.getId())) continue;
                JSONObject obj = new JSONObject();
                obj.put("id", inst.getId());
                obj.put("ticker", inst.getTicker());
                obj.put("name", inst.getName());
                obj.put("exchange", inst.getExchange().name());
                obj.put("sector", inst.getSector());
                obj.put("currency", inst.getCurrency().name());
                obj.put("lotSize", inst.getLotSize());
                obj.put("currentPrice", inst.getCurrentPrice().format());
                obj.put("currentPriceRaw", inst.getCurrentPrice().getAmount().toPlainString());
                obj.put("dailyChangePercent", String.format(java.util.Locale.US, "%.2f", inst.getDailyChangePercent()));
                obj.put("isActiveTrading", inst.isActiveTrading());
                obj.put("isWatchlist", facade.getCatalog().isInWatchlist(inst.getId()));
                int reqLevel = com.tradingglobal3d.core.facade.TradingFacade.getRequiredLevelForSector(inst.getSector(), inst.getExchange());
                obj.put("requiredLevel", reqLevel);
                obj.put("isSectorLocked", !facade.isSectorUnlocked(inst.getSector(), inst.getExchange()));
                array.put(obj);
            }
            return array.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    @JavascriptInterface
    public String getIndices() {
        try {
            if (facade.getGameMode() == com.tradingglobal3d.core.model.GameMode.HISTORICAL_REPLAY) return "[]";
            facade.getCatalog().recalculateAllIndices();
            JSONArray array = new JSONArray();

            for (MarketIndex idx : facade.getCatalog().getAllIndices()) {
                JSONObject obj = new JSONObject();
                obj.put("id", idx.getId());
                obj.put("ticker", idx.getTicker());
                obj.put("name", idx.getName());
                obj.put("exchange", idx.getExchange().name());
                obj.put("value", String.format(java.util.Locale.US, "%.2f", idx.getCurrentValue()));
                obj.put("baseValue", idx.getBaseValue());
                double changePct = idx.getBaseValue() > 0 ? ((idx.getCurrentValue() - idx.getBaseValue()) / idx.getBaseValue()) * 100.0 : 0.0;
                obj.put("changePercent", String.format(java.util.Locale.US, "%+.2f%%", changePct));
                obj.put("up", changePct >= 0);
                obj.put("source", "Model simulasi game (" + idx.getMemberIds().size() + " anggota)");
                obj.put("available", true);
                array.put(obj);
            }
            return array.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    @JavascriptInterface
    public String getIndexDetail(String indexId) {
        try {
            if (facade.getGameMode() == com.tradingglobal3d.core.model.GameMode.HISTORICAL_REPLAY)
                return new JSONObject().put("success", false).put("message", "Dataset tidak mencakup indeks historis").toString();
            MarketIndex index = facade.getCatalog().getIndex(indexId);
            if (index == null) {
                if ("N225".equalsIgnoreCase(indexId) || "HSI".equalsIgnoreCase(indexId)
                        || "FTSE".equalsIgnoreCase(indexId) || "DAX".equalsIgnoreCase(indexId)
                        || "JP:N225".equalsIgnoreCase(indexId) || "HK:HSI".equalsIgnoreCase(indexId)
                        || "UK:FTSE".equalsIgnoreCase(indexId) || "DE:DAX".equalsIgnoreCase(indexId)) {
                    return new JSONObject().put("success", false).put("unavailable", true)
                            .put("id", indexId)
                            .put("message", "Kawasan sudah dipetakan, data emiten belum disediakan. Game belum mendukung aturan transaksi dan mata uang bursa regional ini.")
                            .put("members", new JSONArray()).toString();
                }
                return new JSONObject().put("success", false)
                        .put("message", "Indeks tidak ditemukan").toString();
            }
            facade.getCatalog().recalculateAllIndices();
            JSONArray members = new JSONArray();
            for (String instrumentId : index.getMemberIds()) {
                Instrument instrument = facade.getCatalog().getById(instrumentId);
                if (instrument == null) throw new IllegalStateException("Anggota indeks tidak ditemukan");
                members.put(new JSONObject().put("instrumentId", instrument.getId())
                        .put("ticker", instrument.getTicker()).put("name", instrument.getName())
                        .put("exchange", instrument.getExchange().name())
                        .put("currency", instrument.getCurrency().name())
                        .put("price", instrument.getCurrentPrice().format())
                        .put("isActiveTrading", instrument.isActiveTrading()));
            }
            return new JSONObject().put("success", true).put("id", index.getId())
                    .put("ticker", index.getTicker()).put("name", index.getName())
                    .put("exchange", index.getExchange().name()).put("method", index.getMethod().name())
                    .put("value", String.format(java.util.Locale.US, "%.2f", index.getCurrentValue()))
                    .put("unit", "poin").put("baseValue", index.getBaseValue())
                    .put("sourceLabel", "Indeks simulasi game dari anggota katalog, bukan nilai indeks resmi bursa")
                    .put("gameDate", facade.getClock().getCurrentDate().toString())
                    .put("gameTime", facade.getClock().getFormattedTime())
                    .put("validFrom", JSONObject.NULL).put("historyAvailable", false)
                    .put("limitations", "Bobot efektif, periode berlaku dan histori indeks belum tersedia. Metode memakai harga sekarang dibanding previous close dan kapitalisasi anggota katalog; bukan metodologi indeks resmi.")
                    .put("members", members).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getOrderBook(String instrumentId) {
        try {
            Instrument inst = facade.getCatalog().getById(instrumentId);
            if (inst == null) return "{}";

            OrderBook book = facade.getMarketEngine().getOrCreateBook(inst);
            JSONObject root = new JSONObject();
            root.put("instrumentId", instrumentId);
            root.put("haltState", book.getHaltState().name());
            root.put("lastPrice", book.getLastTradedPrice() != null ? book.getLastTradedPrice().format() : "-");

            JSONArray bids = new JSONArray();
            for (OrderBook.BookLevel lvl : book.getBidLevels(10)) {
                JSONObject b = new JSONObject();
                b.put("price", lvl.price.toPlainString());
                b.put("quantity", lvl.totalQuantity);
                b.put("count", lvl.orderCount);
                bids.put(b);
            }
            root.put("bids", bids);

            JSONArray asks = new JSONArray();
            for (OrderBook.BookLevel lvl : book.getAskLevels(10)) {
                JSONObject a = new JSONObject();
                a.put("price", lvl.price.toPlainString());
                a.put("quantity", lvl.totalQuantity);
                a.put("count", lvl.orderCount);
                asks.put(a);
            }
            root.put("asks", asks);

            JSONArray tape = new JSONArray();
            List<TradeFill> trades = book.getTradeHistory();
            int start = Math.max(0, trades.size() - 20);
            for (int i = trades.size() - 1; i >= start; i--) {
                TradeFill f = trades.get(i);
                JSONObject t = new JSONObject();
                t.put("fillId", f.getFillId());
                t.put("price", f.getPrice().format());
                t.put("quantity", f.getQuantity());
                t.put("timestamp", f.getTimestamp());
                tape.put(t);
            }
            root.put("recentTrades", tape);

            return root.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    @JavascriptInterface
    public String getMarketEvents() {
        try {
            return facade.getMarketEventsJson();
        } catch (Exception e) {
            return "[]";
        }
    }

    @JavascriptInterface
    public String getMarketBosses() {
        try {
            return facade.getMarketBossesJson().toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    @JavascriptInterface
    public String startBossDuel(String bossId) {
        try {
            return facade.startBossDuel(bossId).toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String getTaxAuditStatus() {
        try {
            return facade.getTaxAuditStatus().toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String equipCosmetic(String type, String cosmeticId) {
        try {
            return facade.equipCosmetic(type, cosmeticId).toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String getResearchBoosters() {
        try {
            return facade.getResearchBoostersJson().toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    @JavascriptInterface
    public String activateResearchBooster(String boosterId) {
        try {
            return facade.activateResearchBooster(boosterId).toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String purchasePortfolioInsurance(int durationSessions) {
        try {
            return facade.purchasePortfolioInsurance(durationSessions).toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String getPortfolioInsuranceStatus() {
        try {
            return facade.getPortfolioInsuranceStatus().toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String previewTurnaroundBailout() {
        try {
            return facade.previewTurnaroundBailout().toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String executeTurnaroundBailout(String requestId) {
        try {
            return facade.executeTurnaroundBailout(requestId).toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String createCustomMutualFund(String jsonReq) {
        try {
            JSONObject req = new JSONObject(jsonReq);
            String name = req.getString("fundName");
            double eq = req.getDouble("equityWeightPct");
            double comm = req.getDouble("commodityWeightPct");
            double crypto = req.getDouble("cryptoWeightPct");
            double cash = req.getDouble("cashWeightPct");
            BigDecimal aum = req.has("aum") ? new BigDecimal(req.getString("aum")) : null;
            return facade.createCustomMutualFund(name, eq, comm, crypto, cash, aum).toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String getCustomMutualFunds() {
        return facade.getCustomMutualFundsJson().toString();
    }

    @JavascriptInterface
    public String getHoldingSubsidiaries() {
        try {
            return facade.getHoldingSubsidiariesJson().toString();
        } catch (Exception e) {
            return bridgeError(e);
        }
    }

    @JavascriptInterface
    public String getHoldingCompanies() {
        try { return facade.getHoldingCompaniesJson().toString(); }
        catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String createHoldingCompany(String jsonRequest) {
        try {
            JSONObject request = new JSONObject(jsonRequest);
            return facade.createHoldingCompany(request.getString("holdingId"), request.getString("holdingName"),
                    request.getJSONArray("subsidiaries")).toString();
        } catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String transferHoldingCapital(String jsonRequest) {
        try {
            JSONObject request = new JSONObject(jsonRequest);
            Money amount = new Money(new java.math.BigDecimal(request.getString("amount")), Currency.IDR);
            return facade.transferHoldingCapital(request.getString("requestId"), request.getString("holdingId"),
                    request.getString("fromCompanyId"), request.getString("toCompanyId"), amount,
                    request.getString("transferType")).toString();
        } catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String getTechTree() {
        try {
            return facade.getTechTreeJson().toString();
        } catch (Exception e) {
            return bridgeError(e);
        }
    }

    @JavascriptInterface
    public String getAlgorithmicMarketSignal(String instrumentId) {
        try { return facade.getAlgorithmicMarketSignal(instrumentId).toString(); }
        catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String upgradeTechNode(String nodeId) {
        try {
            return facade.upgradeTechNode(nodeId).toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String getHeadhuntingCandidates() {
        try {
            return facade.getHeadhuntingCandidatesJson().toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    @JavascriptInterface
    public String poachStaffCandidate(String candidateId) {
        try {
            return facade.poachStaffCandidate(candidateId).toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String getDailyStreak() {
        try {
            return facade.getDailyStreakJson().toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    @JavascriptInterface
    public String getSectorDominance() {
        try {
            return facade.getSectorDominanceJson().toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    @JavascriptInterface
    public String triggerMarketShockEvent(String eventType) {
        try {
            return facade.triggerMarketShockEvent(eventType).toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String getChartData(String instrumentId, String timeframe) {
        try {
            return facade.getChartData(instrumentId, timeframe).toString();
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    @JavascriptInterface
    public String getTimeDepositProducts() {
        try {
            return facade.getTimeDepositProducts().toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    @JavascriptInterface
    public String getActiveTimeDeposits() {
        try {
            return facade.getActiveTimeDeposits().toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    @JavascriptInterface
    public String openTimeDeposit(String jsonReq) {
        try {
            JSONObject req = new JSONObject(jsonReq);
            String productId = req.getString("productId");
            BigDecimal amount = new BigDecimal(req.getString("amount"));
            return facade.openTimeDeposit(productId, amount).toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String withdrawTimeDeposit(String jsonReq) {
        try {
            JSONObject req = new JSONObject(jsonReq);
            String depositId = req.getString("depositId");
            return facade.withdrawTimeDeposit(depositId).toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String getNewsFeed() {
        try {
            return facade.getNewsFeedJson();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String getIssuerOwnership(String instrumentId) {
        try { return facade.getIssuerOwnership(instrumentId).toString(); }
        catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String getIssuerFundamentals(String instrumentId) {
        try {
            return facade.getIssuerFundamentals(instrumentId).toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String getCompanyProfile(String instrumentId) {
        try {
            return facade.getCompanyProfile(instrumentId).toString();
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    @JavascriptInterface
    public String getPlayerProgress() {
        try {
            return facade.getProgressManager().toSnapshotJson().toString();
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    @JavascriptInterface
    public String recordResearchRead(String articleId) {
        try {
            boolean unlocked = facade.recordResearchRead(articleId);
            return "{\"success\":true,\"newAchievementUnlocked\":" + unlocked + "}";
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    @JavascriptInterface
    public String toggleWatchlist(String instrumentId) {
        if (facade.getCatalog().getById(instrumentId) == null)
            return "{\"success\":false,\"inWatchlist\":false,\"message\":\"Instrumen tidak ditemukan\"}";
        synchronized (facade.getCatalog()) {
            boolean in = facade.getCatalog().isInWatchlist(instrumentId);
            if (in) facade.getCatalog().removeFromWatchlist(instrumentId);
            else facade.getCatalog().addToWatchlist(instrumentId);
            return "{\"success\":true,\"inWatchlist\":" + facade.getCatalog().isInWatchlist(instrumentId) + "}";
        }
    }

    private Order parseOrder(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        String accountId = obj.optString("accountId", facade.getPersonalAccount().getId());
        requirePlayerAccount(accountId);
        Instrument instrument = facade.getCatalog().getById(obj.getString("instrumentId"));
        if (instrument == null) throw new IllegalArgumentException("Instrumen tidak ditemukan");
        OrderType type = OrderType.valueOf(obj.getString("type"));
        Money limit = type == OrderType.LIMIT ? new Money(new BigDecimal(obj.getString("limitPrice")), instrument.getCurrency()) : null;
        Money trigger = type == OrderType.STOP_LOSS || type == OrderType.TAKE_PROFIT
                ? new Money(new BigDecimal(obj.getString("triggerPrice")), instrument.getCurrency()) : null;
        return new Order("ORD-" + java.util.UUID.randomUUID(), obj.getString("requestId"), accountId, instrument.getId(),
                OrderSide.valueOf(obj.getString("side")), type, limit, trigger, obj.getLong("quantity"), facade.getClock().getGameTimestamp(), null);
    }

    private Account requirePlayerAccount(String id) {
        Account account = facade.getAccount(id);
        if (account == null || (account.getType() != com.tradingglobal3d.core.model.AccountType.PERSONAL
                && account.getType() != com.tradingglobal3d.core.model.AccountType.CLIENT)) throw new IllegalArgumentException("Akun internal tidak dapat dioperasikan pemain");
        if (account.getType() == com.tradingglobal3d.core.model.AccountType.CLIENT)
            facade.getCareerManager().requireRank(com.tradingglobal3d.core.career.CareerRank.PORTFOLIO_MANAGER, "Akun klien");
        return account;
    }

    @JavascriptInterface
    public String previewOrder(String json) {
        try { return facade.previewOrder(parseOrder(json)).toString(); }
        catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getOrderCapacity(String accountId, String instrumentId, String side, String type, String price) {
        try {
            requirePlayerAccount(accountId);
            return facade.getOrderCapacity(accountId, instrumentId, side, type, price).toString();
        } catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String submitOrder(String jsonOrderStr) {
        try {
            Order order = parseOrder(jsonOrderStr);

            MatchingEngine.OrderResult res = facade.submitOrder(order);
            JSONObject result = new JSONObject();
            result.put("success", res.isSuccess);
            result.put("message", res.message);
            result.put("orderId", res.order.getId());
            result.put("status", res.order.getStatus().name());
            result.put("filledQuantity", res.order.getFilledQuantity());
            result.put("remainingQuantity", res.order.getRemainingQuantity());
            return result.toString();
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    private String fxCommand(String json, boolean confirm) {
        try {
            JSONObject request = new JSONObject(json);
            Currency from = Currency.valueOf(request.getString("from"));
            Currency to = Currency.valueOf(request.getString("to"));
            Money amount = new Money(new BigDecimal(request.getString("amount")), from);
            String accountId = request.getString("accountId");
            requirePlayerAccount(accountId);
            return (confirm ? facade.confirmFx(request.getString("requestId"), request.getString("quoteId"), accountId, from, to, amount)
                    : facade.previewFx(accountId, from, to, amount)).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String previewFx(String json) { return fxCommand(json, false); }

    @JavascriptInterface
    public String confirmFx(String json) { return fxCommand(json, true); }

    @JavascriptInterface
    public String claimMission(String missionId) {
        try {
            Money reward = facade.claimMission(missionId);
            return new JSONObject().put("success", true).put("missionId", missionId)
                    .put("reward", reward.format()).put("status", "CLAIMED").toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getFirmSnapshot() {
        try { return facade.getFirmSnapshot().toString(); }
        catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String acceptClientMandate(String mandateId) {
        try {
            Account clientAcc = facade.acceptClientMandate(mandateId);
            JSONObject res = new JSONObject();
            res.put("success", true);
            res.put("mandateId", mandateId);
            res.put("accountId", clientAcc.getId());
            res.put("accountName", clientAcc.getName());
            res.put("message", "Mandat berhasil diterima. Akun kelolaan telah dibuka.");
            return res.toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getRiskSnapshot() {
        try {
            Account personal = facade.getPersonalAccount();
            if (personal == null) return "{\"success\":false,\"message\":\"Akun belum tersedia\"}";
            com.tradingglobal3d.core.risk.RiskComplianceService.MarginHealthReport health = facade.evaluateMarginHealth();
            Money debtIdr = facade.getRiskService().getMarginDebt(personal.getId(), Currency.IDR);
            Money debtUsd = facade.getRiskService().getMarginDebt(personal.getId(), Currency.USD);
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("accountId", personal.getId());
            result.put("accountName", personal.getName());
            result.put("equity", health.getEquity().format());
            result.put("equityRaw", health.getEquity().getAmount().toPlainString());
            result.put("marginDebtTotal", health.getMarginDebt().format());
            result.put("marginDebtIdr", debtIdr.format());
            result.put("marginDebtUsd", debtUsd.format());
            result.put("totalExposure", health.getTotalExposure().format());
            result.put("marginRatio", health.getMarginRatio().toPlainString());
            result.put("marginRatioPercent", health.getMarginRatio().multiply(BigDecimal.valueOf(100)).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + "%");
            result.put("status", health.getStatus().name());
            String statusLabel;
            switch (health.getStatus()) {
                case HEALTHY: statusLabel = "Sehat (Aman)"; break;
                case MARGIN_CALL: statusLabel = "Margin Call (Jaminan Kurang)"; break;
                case LIQUIDATION_REQUIRED: statusLabel = "Perlu Likuidasi"; break;
                default: statusLabel = health.getStatus().name();
            }
            result.put("statusLabel", statusLabel);
            result.put("valuationRateUsdIdr", facade.getFxService().getMidRate().toPlainString());
            result.put("availableCashIdr", personal.getAvailableCash(Currency.IDR).format());
            result.put("availableCashUsd", personal.getAvailableCash(Currency.USD).format());

            JSONArray shorts = new JSONArray();
            JSONArray savedShorts = facade.getRiskService().exportState().getJSONArray("shorts");
            for (int i = 0; i < savedShorts.length(); i++) {
                JSONObject saved = savedShorts.getJSONObject(i);
                if (!personal.getId().equals(saved.getString("accountId"))) continue;
                com.tradingglobal3d.core.risk.ShortPosition position = facade.getRiskService().getShortPosition(personal.getId(), saved.getString("ticker"));
                Instrument instrument = facade.getCatalog().getById(position.getExchange() + ":" + position.getTicker());
                shorts.put(new JSONObject().put("ticker", position.getTicker()).put("exchange", position.getExchange())
                        .put("shares", position.getBorrowedShares()).put("collateral", position.getCollateralLocked().format())
                        .put("liability", position.calculateLiability(instrument.getCurrentPrice().getAmount()).format())
                        .put("unrealizedPnl", position.calculateUnrealizedPnL(instrument.getCurrentPrice().getAmount()).format())
                        .put("dailyBorrowFee", position.calculateDailyBorrowFee(instrument.getCurrentPrice().getAmount()).format()));
            }
            result.put("shortPositions", shorts);

            JSONArray logs = new JSONArray();
            List<com.tradingglobal3d.core.risk.ComplianceAuditLog> allLogs = facade.getRiskService().getAuditLogs();
            for (int i = allLogs.size() - 1; i >= 0 && logs.length() < 20; i--) {
                com.tradingglobal3d.core.risk.ComplianceAuditLog l = allLogs.get(i);
                if (l.getAccountId().equals(personal.getId())) {
                    logs.put(new JSONObject()
                            .put("id", l.getId())
                            .put("category", l.getCategory().name())
                            .put("categoryLabel", l.getCategory().getLabel())
                            .put("description", l.getDescription())
                            .put("mitigation", l.getMitigationAction())
                            .put("timestamp", l.getTimestamp()));
                }
            }
            result.put("auditLogs", logs);
            return result.toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String takeMarginLoan(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            Currency cur = Currency.valueOf(req.getString("currency"));
            BigDecimal amount = new BigDecimal(req.getString("amount"));
            Money loan = new Money(amount, cur);
            facade.takeMarginLoan(loan);
            Money debt = facade.getRiskService().getMarginDebt(facade.getPersonalAccount().getId(), cur);
            return new JSONObject().put("success", true)
                    .put("currency", cur.name())
                    .put("amount", loan.format())
                    .put("currentDebt", debt.format())
                    .put("message", "Pencairan pinjaman margin berhasil: " + loan.format()).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String repayMarginLoan(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            Currency cur = Currency.valueOf(req.getString("currency"));
            BigDecimal amount = new BigDecimal(req.getString("amount"));
            Money repay = new Money(amount, cur);
            facade.repayMarginLoan(repay);
            Money debt = facade.getRiskService().getMarginDebt(facade.getPersonalAccount().getId(), cur);
            return new JSONObject().put("success", true)
                    .put("currency", cur.name())
                    .put("amount", repay.format())
                    .put("currentDebt", debt.format())
                    .put("message", "Pelunasan utang margin berhasil: " + repay.format()).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String restructureDebt(String currencyStr) {
        try {
            Currency cur = Currency.valueOf(currencyStr);
            return facade.restructureDebt(cur).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getCompanyStaff(String companyId) {
        try { return facade.getCompanyStaff(companyId).toString(); }
        catch (Exception error) { return staffError(error); }
    }

    @JavascriptInterface
    public String previewCompanyStaffAction(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            return facade.previewCompanyStaffAction(req.getString("companyId"), req.getString("action"),
                    req.getString("employeeId"), req.optString("role", null)).toString();
        } catch (Exception error) { return staffError(error); }
    }

    @JavascriptInterface
    public String manageCompanyStaff(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            return facade.manageCompanyStaff(req.getString("requestId"), req.getString("companyId"), req.getString("action"),
                    req.getString("employeeId"), req.optString("role", null), req.getLong("expectedVersion")).toString();
        } catch (Exception error) { return staffError(error); }
    }

    private String staffError(Exception error) {
        try { return new JSONObject().put("success", false).put("code", "COMPANY_STAFF_REJECTED").put("message", error.getMessage()).toString(); }
        catch (JSONException impossible) { return "{\"success\":false}"; }
    }

    private String bridgeError(Exception error) {
        try { return new JSONObject().put("success", false).put("code", "OPERATION_FAILED").put("message", error.getMessage()).toString(); }
        catch (JSONException impossible) { return "{\"success\":false}"; }
    }

    @JavascriptInterface
    public String getCompanies() {
        try {
            return facade.getCompaniesJson().toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String foundCompany(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            String id = req.getString("id");
            String name = req.getString("name");
            String sector = req.getString("sector");
            Money capital = new Money(new BigDecimal(req.getString("capital")), Currency.IDR);
            var comp = facade.foundCompany(id, name, sector, capital);
            return new JSONObject().put("success", true).put("company", comp.toJsonObject()).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String transferCompanyCapital(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            String compId = req.getString("companyId");
            boolean toCompany = req.getBoolean("toCompany");
            Money amount = new Money(new BigDecimal(req.getString("amount")), Currency.IDR);
            facade.transferCompanyCapital(compId, toCompany, amount);
            return new JSONObject().put("success", true).put("message", "Transfer modal berhasil").toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String setCompanyPolicy(String jsonRequest) {
        return "{\"success\":false,\"message\":\"Tinjau perubahan kebijakan sebelum konfirmasi\"}";
    }

    @JavascriptInterface
    public String previewCompanyAction(String jsonRequest) {
        try {
            JSONObject r = new JSONObject(jsonRequest);
            return facade.previewCompanyAction(r.getString("companyId"), r.getString("action"), r.optInt("workHours", 8), r.optString("benefitTier", "STANDARD")).toString();
        } catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String executeCompanyAction(String jsonRequest) {
        try {
            JSONObject r = new JSONObject(jsonRequest);
            return facade.executeCompanyAction(r.getString("requestId"), r.getString("companyId"), r.getString("action"), r.optInt("workHours", 8), r.optString("benefitTier", "STANDARD"), r.getLong("expectedVersion")).toString();
        } catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String companyCapitalAction(String jsonRequest) {
        try {
            JSONObject r = new JSONObject(jsonRequest);
            return facade.companyCapitalAction(r.optString("requestId", null), r.getString("companyId"), r.getBoolean("toCompany"),
                    new Money(new BigDecimal(r.getString("amount")), Currency.IDR), r.optString("expectedVersion", null)).toString();
        } catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String multiAssetOrder(String jsonRequest) {
        try {
            JSONObject r = new JSONObject(jsonRequest);
            return facade.multiAssetOrder(r.optString("requestId", null), r.getString("instrumentId"), r.getBoolean("buy"), r.getLong("quantity"), r.optString("quoteVersion", null)).toString();
        } catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String getPerformanceReport() {
        try { return facade.getPerformanceReport().toString(); }
        catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String getHistoricalDatasets() {
        try {
            JSONArray datasets = new JSONArray();
            for (String id : com.tradingglobal3d.core.market.HistoricalDatasetLoader.availableDatasetIds()) {
                var bars = com.tradingglobal3d.core.market.HistoricalDatasetLoader.loadDataset(id);
                datasets.put(new JSONObject().put("id", id).put("resolution", "1D").put("source", "Nasdaq WIKI — public domain")
                        .put("start", bars.get(0).getDate().toString()).put("end", bars.get(bars.size()-1).getDate().toString()).put("bars", bars.size()));
            }
            return new JSONObject().put("success", true).put("datasets", datasets).toString();
        } catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String resumeSavedMode(String mode) {
        try { return facade.resumeSavedMode(mode).toString(); }
        catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String getCollections() {
        try {
            JSONArray arr = facade.getCollectionManager().exportState();
            return new JSONObject().put("success", true).put("collections", arr).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String transactCollection(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            java.math.BigDecimal expectedPrice = null;
            if (req.has("expectedPrice") && !req.isNull("expectedPrice")) {
                expectedPrice = new java.math.BigDecimal(req.get("expectedPrice").toString());
            } else if (req.has("price") && !req.isNull("price")) {
                expectedPrice = new java.math.BigDecimal(req.get("price").toString());
            }
            return facade.transactCollection(req.getString("requestId"), req.getString("itemId"), req.getBoolean("buy"), expectedPrice).toString();
        } catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String buyCollection(String itemId) {
        try {
            var item = facade.buyCollection(itemId);
            return new JSONObject().put("success", true).put("item", item.toJsonObject()).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String sellCollection(String itemId) {
        try {
            var item = facade.sellCollection(itemId);
            return new JSONObject().put("success", true).put("item", item.toJsonObject()).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getLeaderboard() {
        try {
            return facade.getLeaderboard().toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String confirmPrestige(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            return facade.confirmPrestige(req.getString("requestId"), req.getString("runId")).toString();
        } catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String getPrestigeStatus() {
        try {
            return facade.getPrestigeStatus().toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    // =========================================================================
    // REKSA DANA BERBASIS UNIT & NAB (REF:funds, D06)
    // =========================================================================

    @JavascriptInterface
    public String cancelFundOrder(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            return facade.cancelFundOrder(req.getString("fundId"), req.getString("orderId")).toString();
        } catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String getMutualFunds() {
        try {
            return facade.getMutualFundsJson().toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String subscribeMutualFund(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            String fundId = req.getString("fundId");
            Money amt = new Money(new BigDecimal(req.getString("amount")), Currency.IDR);
            return facade.subscribeMutualFund(req.getString("requestId"), fundId, amt).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String redeemMutualFund(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            String fundId = req.getString("fundId");
            BigDecimal units = new BigDecimal(req.getString("units"));
            return facade.redeemMutualFund(req.getString("requestId"), fundId, units).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    // =========================================================================
    // PINJAMAN TENOR DI LUAR MARGIN (REF:loans, D05)
    // =========================================================================

    @JavascriptInterface
    public String getLoanProducts() {
        try {
            return facade.getLoanProductsJson().toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String disburseLoan(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            String productId = req.getString("productId");
            boolean isPersonal = req.optBoolean("isPersonal", true);
            String compId = req.optString("companyId", null);
            Money principal = new Money(new BigDecimal(req.getString("principal")), Currency.IDR);
            int tenor = req.getInt("tenorDays");
            return facade.disburseLoan(productId, isPersonal, compId, principal, tenor).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getLoanQuote(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            String productId = req.getString("productId");
            boolean isPersonal = req.optBoolean("isPersonal", true);
            String compId = req.optString("companyId", null);
            Money principal = new Money(new BigDecimal(req.getString("principal")), Currency.IDR);
            int tenor = req.getInt("tenorDays");
            String collateralId = req.optString("collateralItemId", null);
            return facade.getLoanQuote(productId, isPersonal, compId, principal, tenor, collateralId).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String disburseReviewedLoan(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            String requestId = req.getString("requestId");
            String productId = req.getString("productId");
            boolean isPersonal = req.optBoolean("isPersonal", true);
            String compId = req.optString("companyId", null);
            Money principal = new Money(new BigDecimal(req.getString("principal")), Currency.IDR);
            int tenor = req.getInt("tenorDays");
            String collateralId = req.optString("collateralItemId", null);
            String quoteVersion = req.has("expectedQuoteVersion") ? req.getString("expectedQuoteVersion") : req.getString("quoteVersion");
            return facade.disburseReviewedLoan(requestId, productId, isPersonal, compId, principal, tenor, collateralId, quoteVersion).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String reviewPortfolioHealth() {
        try { return facade.reviewPortfolioHealth().toString(); }
        catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String getLoans() {
        try {
            return facade.getLoansJson().toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String payLoan(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            String loanId = req.getString("loanId");
            Money amt = new Money(new BigDecimal(req.getString("amount")), Currency.IDR);
            return facade.payLoan(req.getString("requestId"), loanId, amt).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String prepayLoanRequest(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            return facade.prepayLoan(req.getString("requestId"), req.getString("loanId")).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String prepayLoan(String loanId) {
        try {
            return facade.prepayLoan(loanId).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String restructureLoan(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            String loanId = req.getString("loanId");
            int days = req.getInt("additionalDays");
            return facade.restructureLoan(loanId, days).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    // =========================================================================
    // MISI PERIODIK HARIAN & MINGGUAN (REF:missions, D07)
    // =========================================================================

    @JavascriptInterface
    public String getPeriodicMissions() {
        try {
            return facade.getPeriodicMissionsJson().toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String claimPeriodicMission(String missionId) {
        try {
            return facade.claimPeriodicMission(missionId).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    // =========================================================================
    // LIGA, RIVALITAS NPC, & LEADERBOARD (REF:ranks, rivals, leaderboard, D09, D13)
    // =========================================================================

    @JavascriptInterface
    public String getLeagueLeaderboard(String mode) {
        try {
            return facade.getLeagueLeaderboard(mode).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getRivalStatus() {
        try {
            return facade.getRivalStatus().toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    // =========================================================================
    // GAYA INVESTOR (REF:archetype, D15) & PROFIL PEMAIN (REF:profile, D16)
    // =========================================================================

    @JavascriptInterface
    public String getInvestorArchetype() {
        try {
            return facade.getInvestorArchetype().toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getPlayerProfile() {
        try {
            return facade.getPlayerProfileJson().toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String updatePlayerProfile(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            String name = req.optString("playerName", null);
            JSONObject prefs = req.optJSONObject("preferences");
            String textSize = prefs != null ? prefs.optString("textSize", "100%") : "100%";
            boolean hc = prefs != null && prefs.optBoolean("highContrast", false);
            boolean rm = prefs != null && prefs.optBoolean("reducedMotion", false);
            return facade.updatePlayerProfile(name, textSize, hc, rm).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getDailySponsorStatus() {
        try {
            return facade.getDailySponsorStatus().toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    @JavascriptInterface
    public String claimDailySponsor(String jsonRequest) {
        try {
            JSONObject req = jsonRequest != null && !jsonRequest.isEmpty() ? new JSONObject(jsonRequest) : new JSONObject();
            String reqId = req.optString("requestId", "REQ-SPONSOR-" + System.currentTimeMillis());
            return facade.claimDailySponsor(reqId).toString();
        } catch (Exception error) {
            return bridgeError(error);
        }
    }

    // =========================================================================
    // KEKAYAAN KONSOLIDASI (REF:wealth, D02)
    // =========================================================================

    @JavascriptInterface
    public String getConsolidatedWealth() {
        try {
            return facade.getConsolidatedWealthBreakdown().toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String openShortPosition(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            String ticker = req.getString("ticker");
            long shares = req.getLong("shares");
            com.tradingglobal3d.core.risk.ShortPosition sp = facade.openShortPosition(ticker, shares);
            return new JSONObject().put("success", true)
                    .put("ticker", sp.getTicker())
                    .put("shares", sp.getBorrowedShares())
                    .put("collateral", sp.getCollateralLocked().format())
                    .put("message", "Posisi short berhasil dibuka: " + shares + " lembar " + ticker).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String closeShortPosition(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            String ticker = req.getString("ticker");
            long shares = req.getLong("shares");
            facade.closeShortPosition(ticker, shares);
            return new JSONObject().put("success", true)
                    .put("ticker", ticker)
                    .put("shares", shares)
                    .put("message", "Posisi short berhasil ditutup (Buy to cover): " + shares + " lembar " + ticker).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getIpoOfferings() {
        try {
            if (facade.getGameMode() == com.tradingglobal3d.core.model.GameMode.HISTORICAL_REPLAY) return "[]";
            if (facade.getClock() != null) {
                facade.getCorporateService().ensureMonthlyIpoSchedule(facade.getClock().getCurrentDate());
            }
            JSONArray result = new JSONArray();
            for (com.tradingglobal3d.core.corporate.IpoOffering ipo : facade.getCorporateService().getIpoOfferings()) {
                result.put(new JSONObject()
                        .put("id", ipo.getId())
                        .put("ticker", ipo.getTicker())
                        .put("companyName", ipo.getCompanyName())
                        .put("currency", ipo.getCurrency().name())
                        .put("offeringPrice", ipo.getOfferingPrice().format())
                        .put("offeringPriceRaw", ipo.getOfferingPrice().getAmount().toPlainString())
                        .put("lotSize", ipo.getLotSize())
                        .put("sharesOffered", ipo.getSharesOffered())
                        .put("listingDate", ipo.getListingDate().toString())
                        .put("isAllotted", ipo.isAllotted()));
            }
            return result.toString();
        } catch (Exception error) { return "[]"; }
    }

    @JavascriptInterface
    public String orderIpo(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            String ipoId = req.getString("ipoId");
            long lots = req.getLong("lots");
            if (lots <= 0) return new JSONObject().put("success", false).put("message", "Jumlah lot harus positif").toString();
            com.tradingglobal3d.core.corporate.IpoSubscription sub = facade.orderIpo(ipoId, lots, req.getString("requestId"));
            return new JSONObject().put("success", true)
                    .put("subscriptionId", sub.getId())
                    .put("ipoId", sub.getIpoId())
                    .put("orderedLots", sub.getOrderedLots())
                    .put("reservedMoney", sub.getReservedMoney().format())
                    .put("status", sub.getStatus().name())
                    .put("message", "Pemesanan IPO berhasil diajukan (" + lots + " lot)").toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getShareholderAgendas() {
        try {
            if (facade.getGameMode() == com.tradingglobal3d.core.model.GameMode.HISTORICAL_REPLAY) return "[]";
            Account personal = facade.getPersonalAccount();
            String accountId = personal != null ? personal.getId() : "";
            JSONArray result = new JSONArray();
            for (com.tradingglobal3d.core.corporate.ShareholderMeetingAgenda a : facade.getCorporateService().getAgendas()) {
                result.put(new JSONObject()
                        .put("id", a.getId())
                        .put("ticker", a.getTicker())
                        .put("title", a.getTitle())
                        .put("resolutionText", a.getResolutionText())
                        .put("recordingDate", a.getRecordingDate().toString())
                        .put("deadline", a.getDeadline().toString()).put("isClosed", a.isClosed())
                        .put("entitledShares", a.getEntitledShares(accountId)).put("result", a.getResult()).put("consequence", a.getConsequence())
                        .put("canVote", a.isRecorded() && !a.isClosed() && !a.hasVoted(accountId) && a.getEntitledShares(accountId) > 0
                                && !facade.getClock().getCurrentDate().isAfter(a.getDeadline()))
                        .put("votesFor", a.getTotalVotesFor())
                        .put("votesAgainst", a.getTotalVotesAgainst())
                        .put("votesAbstain", a.getTotalVotesAbstain())
                        .put("hasVoted", a.hasVoted(accountId)));
            }
            return result.toString();
        } catch (Exception error) { return "[]"; }
    }

    @JavascriptInterface
    public String castShareholderVote(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            String agendaId = req.getString("agendaId");
            com.tradingglobal3d.core.corporate.ShareholderMeetingAgenda.VoteChoice choice =
                    com.tradingglobal3d.core.corporate.ShareholderMeetingAgenda.VoteChoice.valueOf(req.getString("choice"));
            facade.castShareholderVote(agendaId, choice);
            return new JSONObject().put("success", true)
                    .put("agendaId", agendaId)
                    .put("choice", choice.name())
                    .put("choiceLabel", choice.getLabel())
                    .put("message", "Hak suara berhasil disampaikan: " + choice.getLabel()).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getCorporateActions() {
        try {
            if (facade.getGameMode() == com.tradingglobal3d.core.model.GameMode.HISTORICAL_REPLAY) return "[]";
            JSONArray result = new JSONArray();
            for (com.tradingglobal3d.core.corporate.CorporateAction ca : facade.getCorporateService().getCorporateActions()) {
                result.put(new JSONObject()
                        .put("id", ca.getId())
                        .put("ticker", ca.getTicker())
                        .put("type", ca.getType().name())
                        .put("valueOrRatio", ca.getValueOrRatio().toPlainString())
                        .put("currency", ca.getCurrency() != null ? ca.getCurrency().name() : JSONObject.NULL)
                        .put("recordingDate", ca.getRecordingDate().toString())
                        .put("executionDate", ca.getExecutionDate().toString())
                        .put("approvalStatus", facade.getCorporateService().getApprovalStatus(ca)).put("isCancelled", ca.isCancelled())
                        .put("isExecuted", ca.isExecuted()));
            }
            return result.toString();
        } catch (Exception error) { return "[]"; }
    }

    @JavascriptInterface
    public String getMultiAssets() {
        try {
            if (facade.getGameMode() == com.tradingglobal3d.core.model.GameMode.HISTORICAL_REPLAY) return "{\"bonds\":[],\"commodities\":[]}";
            JSONObject result = new JSONObject();
            JSONArray bonds = new JSONArray();
            for (com.tradingglobal3d.core.multiasset.BondInstrument b : facade.getFundBuilder().getAllBonds()) {
                Position holding = facade.getPersonalAccount().getPosition("BOND:" + b.getTicker());
                bonds.put(new JSONObject()
                        .put("id", b.getId())
                        .put("ticker", b.getTicker())
                        .put("name", b.getName())
                        .put("currency", b.getCurrency().name())
                        .put("parValue", b.getParValue().format())
                        .put("couponRateAnnual", b.getCouponAnnualRate().toPlainString())
                        .put("maturityDate", b.getMaturityDate().toString())
                        .put("cleanPrice", b.getCurrentCleanPrice().format())
                        .put("unitPriceRaw", b.getCurrentCleanPrice().getAmount().toPlainString())
                        .put("quantity", holding == null ? 0 : holding.getQuantity()).put("positionId", "BOND:" + b.getTicker())
                        .put("marketValue", holding == null ? Money.zero(b.getCurrency()).format() : holding.getMarketValue().format())
                        .put("status", "Tersedia")
                        .put("note", "Fee 0,1%; harga bersih, kupon sesuai waktu kepemilikan; risiko harga dan penerbit"));
            }
            JSONArray comms = new JSONArray();
            for (com.tradingglobal3d.core.multiasset.CommodityInstrument c : facade.getFundBuilder().getAllCommodities()) {
                Position holding = facade.getPersonalAccount().getPosition("COMM:" + c.getCode());
                comms.put(new JSONObject()
                        .put("id", c.getId())
                        .put("ticker", c.getCode())
                        .put("name", c.getName())
                        .put("currency", c.getCurrency().name())
                        .put("unit", c.getUnitOfMeasure())
                        .put("spotPrice", c.getSpotPricePerUnit().format())
                        .put("unitPriceRaw", c.getSpotPricePerUnit().getAmount().toPlainString())
                        .put("quantity", holding == null ? 0 : holding.getQuantity()).put("positionId", "COMM:" + c.getCode())
                        .put("marketValue", holding == null ? Money.zero(c.getCurrency()).format() : holding.getMarketValue().format())
                        .put("status", "Tersedia")
                        .put("note", "Unit emas game; fee 0,1%, risiko harga; tanpa leverage"));
            }
            result.put("bonds", bonds);
            result.put("commodities", comms);
            return result.toString();
        } catch (Exception error) { return "{\"bonds\":[],\"commodities\":[]}"; }
    }

    @JavascriptInterface
    public String getFundPlans() {
        try {
            JSONArray result = new JSONArray();
            for (com.tradingglobal3d.core.multiasset.FundAllocationPlan p : facade.getFundBuilder().getAllPlans()) {
                JSONObject planObj = new JSONObject()
                        .put("id", p.getId())
                        .put("accountId", p.getAccountId())
                        .put("name", p.getName())
                        .put("status", p.getStatus().name())
                        .put("cashWeight", p.getTargetCashWeight())
                        .put("weights", new JSONObject(p.getTargetWeights()));
                result.put(planObj);
            }
            return result.toString();
        } catch (Exception error) { return "[]"; }
    }

    @JavascriptInterface
    public String saveFundPlan(String jsonRequest) {
        try {
            JSONObject req = new JSONObject(jsonRequest);
            String planId = req.getString("planId");
            String name = req.getString("name");
            double cashWeight = req.getDouble("cashWeight");
            String accountId = req.optString("accountId", facade.getPersonalAccount().getId());

            JSONObject weightsJson = req.getJSONObject("weights");
            Map<String, Double> weights = new LinkedHashMap<>();
            double totalWeight = cashWeight;
            Iterator<String> keys = weightsJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                double w = weightsJson.getDouble(key);
                weights.put(key, w);
                totalWeight += w;
            }

            if (Math.abs(totalWeight - 1.0) > 0.001) {
                return new JSONObject().put("success", false)
                        .put("message", "Total bobot harus tepat 100% (saat ini " + Math.round(totalWeight * 100) + "%)").toString();
            }

            com.tradingglobal3d.core.multiasset.FundAllocationPlan plan = facade.saveFundPlan(planId, accountId, name, weights, cashWeight);
            return new JSONObject().put("success", true)
                    .put("planId", plan.getId())
                    .put("status", plan.getStatus().name())
                    .put("message", "Rencana alokasi dana berhasil disimpan: " + plan.getName()).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String executeFundRebalance(String planId) {
        try {
            List<com.tradingglobal3d.core.trading.MatchingEngine.OrderResult> results = facade.executeFundRebalance(planId);
            com.tradingglobal3d.core.multiasset.FundAllocationPlan plan = facade.getFundBuilder().getPlan(planId);
            JSONArray orders = new JSONArray();
            for (com.tradingglobal3d.core.trading.MatchingEngine.OrderResult res : results) {
                orders.put(new JSONObject()
                        .put("orderId", res.order.getId())
                        .put("ticker", res.order.getInstrumentId())
                        .put("side", res.order.getSide().name())
                        .put("status", res.order.getStatus().name())
                        .put("filledQuantity", res.order.getFilledQuantity())
                        .put("remainingQuantity", res.order.getRemainingQuantity()));
            }
            return new JSONObject().put("success", true)
                    .put("planId", planId)
                    .put("planStatus", plan != null ? plan.getStatus().name() : "UNKNOWN")
                    .put("results", orders)
                    .put("message", "Rebalancing rencana dieksekusi (" + results.size() + " order dibuat)").toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getClientReports() {
        try {
            JSONObject snapshot = facade.getFirmSnapshot();
            return snapshot.optJSONArray("mandates") != null ? snapshot.getJSONArray("mandates").toString() : "[]";
        } catch (Exception error) { return "[]"; }
    }

    @JavascriptInterface
    public String getResearchFeed() {
        try {
            com.tradingglobal3d.core.research.ResearchService rs = facade.getResearchService();
            com.tradingglobal3d.core.research.TerminalTier tier = rs.getCurrentTier();
            JSONObject result = new JSONObject();
            result.put("tier", tier.name());
            result.put("tierTitle", tier.getTitle());
            result.put("expiresAt", rs.getSubscriptionExpiresAt());

            JSONArray news = new JSONArray(facade.getNewsFeedJson());
            result.put("news", news);

            JSONArray analysts = new JSONArray();
            for (com.tradingglobal3d.core.research.AnalystReport r : facade.getAnalystReports()) {
                analysts.put(new JSONObject()
                        .put("id", r.getId())
                        .put("analystName", r.getAnalystName())
                        .put("firmName", r.getFirmName())
                        .put("ticker", r.getTicker())
                        .put("recommendation", r.getRecommendation().name())
                        .put("recommendationLabel", r.getRecommendation().getLabel())
                        .put("targetPrice", r.getTargetPrice().format())
                        .put("accuracyHistoryPercent", r.getAccuracyHistoryPercent())
                        .put("thesisSummary", r.getThesisSummary()));
            }
            result.put("analysts", analysts);

            JSONArray calendar = new JSONArray();
            for (com.tradingglobal3d.core.research.CalendarEvent e : facade.getCalendar()) {
                calendar.put(new JSONObject()
                        .put("id", e.getId())
                        .put("date", e.getDate().toString())
                        .put("title", e.getTitle())
                        .put("category", e.getCategory().name())
                        .put("categoryLabel", e.getCategory().getLabel())
                        .put("relatedTicker", e.getRelatedTicker() != null ? e.getRelatedTicker() : JSONObject.NULL)
                        .put("details", e.getDetails()));
            }
            result.put("calendar", calendar);
            return result.toString();
        } catch (Exception error) {
            return "{\"tier\":\"BASIC\",\"tierTitle\":\"Basic Terminal\",\"news\":[],\"analysts\":[],\"calendar\":[]}";
        }
    }

    @JavascriptInterface
    public String getResearchTiers() {
        try {
            com.tradingglobal3d.core.research.ResearchService rs = facade.getResearchService();
            com.tradingglobal3d.core.research.TerminalTier current = rs.getCurrentTier();
            JSONArray tiers = new JSONArray();
            for (com.tradingglobal3d.core.research.TerminalTier t : com.tradingglobal3d.core.research.TerminalTier.values()) {
                tiers.put(new JSONObject()
                        .put("name", t.name())
                        .put("title", t.getTitle())
                        .put("level", t.getLevel())
                        .put("feeUsd", t.getMonthlyFeeUsd().format())
                        .put("feeUsdRaw", t.getMonthlyFeeUsd().getAmount().toPlainString())
                        .put("features", t.getFeatures())
                        .put("isCurrent", t == current));
            }
            return tiers.toString();
        } catch (Exception error) { return "[]"; }
    }

    @JavascriptInterface
    public String subscribeResearchTier(String tierName) {
        try {
            com.tradingglobal3d.core.research.TerminalTier tier =
                    com.tradingglobal3d.core.research.TerminalTier.valueOf(tierName);
            boolean ok = facade.subscribeTerminal(tier);
            if (!ok) {
                return new JSONObject().put("success", false)
                        .put("message", "Gagal berlangganan " + tier.getTitle() + ": saldo kas USD tidak mencukupi (" + tier.getMonthlyFeeUsd().format() + ")").toString();
            }
            return new JSONObject().put("success", true)
                    .put("tier", tier.name())
                    .put("tierTitle", tier.getTitle())
                    .put("message", "Berhasil berlangganan paket " + tier.getTitle()).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getSessionReports() {
        try {
            JSONArray result = new JSONArray();
            for (com.tradingglobal3d.core.career.DailySessionReport report : facade.getCareerManager().getSessionReports())
                result.put(new JSONObject().put("date", report.getDate().toString())
                        .put("accountId", facade.getPersonalAccount().getId())
                        .put("startingNetWorth", report.getStartingNetWorth().format())
                        .put("endingNetWorth", report.getEndingNetWorth().format())
                        .put("investmentPnL", report.getDailyPnL().format())
                        .put("nonInvestmentFlows", report.getNonInvestmentFlows().format())
                        .put("fees", report.getFeesPaid().format()).put("salary", report.getSalaryReceived().format())
                        .put("fills", report.getTotalFillsCount()).put("note", report.getSummaryNote()));
            return result.toString();
        } catch (Exception error) { return "[]"; }
    }

    @JavascriptInterface
    public String closeDay(String requestId) {
        return "{\"success\":false,\"message\":\"Pergantian hari berjalan otomatis\"}";
    }

    @JavascriptInterface
    public String getPortfolio(String accountId) {
        try {
            Account account = requirePlayerAccount(accountId);
            if (account == null) return new JSONObject().put("success", false).put("message", "Akun tidak ditemukan").toString();
            JSONObject result = new JSONObject().put("success", true).put("accountId", account.getId())
                    .put("accountName", account.getName()).put("accountType", account.getType().name());
            BigDecimal rate = facade.getFxService().getMidRate();
            Money assets = account.getTotalPortfolioValue(Currency.IDR, rate);
            Money equity = facade.getRiskService().evaluateMarginHealth(account, facade.getCatalog(), rate).getEquity();
            Money cashValue = account.getTotalCash(Currency.IDR).add(
                    new Money(account.getTotalCash(Currency.USD).getAmount().multiply(rate), Currency.IDR));
            result.put("netWorth", equity.format()).put("grossAssets", assets.format())
                    .put("liabilities", assets.subtract(equity).format()).put("cashValueIdr", cashValue.format())
                    .put("positionsValueIdr", assets.subtract(cashValue).format());
            result.put("positionsSharePercent", assets.isZero() ? "0" : assets.subtract(cashValue).getAmount()
                                    .multiply(BigDecimal.valueOf(100)).divide(assets.getAmount(), 2, java.math.RoundingMode.HALF_UP).toPlainString());
                    Money totalUnrealized = Money.zero(Currency.IDR);
                    Money totalRealized = Money.zero(Currency.IDR);
                    for (Position p : account.getPositions().values()) {
                        BigDecimal fx = p.getCurrency() == Currency.USD ? rate : BigDecimal.ONE;
                        totalUnrealized = totalUnrealized.add(new Money(p.getUnrealizedPnL().getAmount().multiply(fx), Currency.IDR));
                        if (p.getRealizedPnL() != null) {
                            totalRealized = totalRealized.add(new Money(p.getRealizedPnL().getAmount().multiply(fx), Currency.IDR));
                        }
                    }
                    result.put("totalUnrealizedPnL", totalUnrealized.format());
                    result.put("totalRealizedPnL", totalRealized.format());
                    JSONObject cash = new JSONObject();
            for (Currency c : Currency.values()) cash.put(c.name(), new JSONObject()
                    .put("total", account.getTotalCash(c).format()).put("available", account.getAvailableCash(c).format())
                    .put("reserved", account.getReservedCash(c).format()));
            result.put("cash", cash);
            JSONArray positions = new JSONArray();
            for (Position p : account.getPositions().values()) {
                Instrument inst = facade.getCatalog().getById(p.getExchange() + ":" + p.getTicker());
                long lotSize = inst != null ? inst.getLotSize() : ("US".equalsIgnoreCase(p.getExchange()) ? 1 : 100);
                String name = inst != null ? inst.getName() : p.getTicker();
                BigDecimal cost = p.getAverageCost();
                BigDecimal current = p.getCurrentPrice();
                BigDecimal pnlRaw = p.getUnrealizedPnL().getAmount();
                double pnlPct = cost.compareTo(BigDecimal.ZERO) > 0
                        ? current.subtract(cost).divide(cost, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                        : 0.0;
                String instrumentId = p.getExchange() + ":" + p.getTicker();
                long committedShares = facade.getCommittedShares(account.getId(), instrumentId);
                long reservedShares = Math.min(Math.max(0, p.getQuantity()), committedShares);
                positions.put(new JSONObject()
                        .put("instrumentId", p.getExchange() + ":" + p.getTicker())
                        .put("ticker", p.getTicker())
                        .put("name", name)
                        .put("exchange", p.getExchange())
                        .put("quantity", p.getQuantity())
                        .put("lotSize", lotSize)
                        .put("lots", lotSize > 0 ? p.getQuantity() / lotSize : 0)
                        .put("reservedShares", reservedShares)
                        .put("availableShares", Math.max(0, p.getQuantity() - reservedShares))
                        .put("currency", p.getCurrency().name())
                        .put("averageCost", p.getAverageCost().toPlainString())
                        .put("currentPrice", p.getCurrentPrice().toPlainString())
                        .put("marketValue", p.getMarketValue().format())
                        .put("unrealizedPnL", p.getUnrealizedPnL().format())
                        .put("unrealizedPnLRaw", pnlRaw.toPlainString())
                        .put("unrealizedPnLPercent", String.format(java.util.Locale.US, "%.2f", pnlPct))
                        .put("realizedPnLGross", p.getRealizedPnL() == null ? JSONObject.NULL : p.getRealizedPnL().format()));
            }
            return result.put("positions", positions).put("valuationRateUsdIdr", facade.getFxService().getMidRate().toPlainString())
                    .put("timestamp", System.currentTimeMillis()).toString();
        } catch (Exception error) { return "{\"success\":false}"; }
    }

    @JavascriptInterface
    public String getLedger(String accountId) {
        try {
            requirePlayerAccount(accountId);
            JSONArray result = new JSONArray();
            for (com.tradingglobal3d.core.economy.LedgerEntry entry : facade.getLedger().getEntriesForAccount(accountId)) {
                result.put(new JSONObject().put("accountId", entry.getAccountId()).put("id", entry.getId())
                        .put("requestId", entry.getRequestId()).put("timestamp", entry.getTimestamp())
                        .put("type", entry.getType().name()).put("amount", entry.getAmount().format())
                        .put("credit", entry.isCredit()).put("balanceAfter", entry.getBalanceAfter().format())
                        .put("description", entry.getDescription()));
            }
            return result.toString();
        } catch (Exception error) { return "[]"; }
    }

    @JavascriptInterface
    public String getOrders(String accountId, boolean activeOnly) {
        try {
            requirePlayerAccount(accountId);
            JSONArray result = new JSONArray();
            List<Order> orders = activeOnly ? facade.getOrderManager().getActiveOrdersForAccount(accountId)
                    : facade.getOrderManager().getOrdersForAccount(accountId);
            for (Order order : orders) result.put(new JSONObject(getOrderStatus(order.getRequestId())));
            return result.toString();
        } catch (Exception error) { return "[]"; }
    }

    @JavascriptInterface
    public String getOrderStatus(String requestId) {
        try {
            Order order = facade.getOrderManager().getOrderByRequest(requestId);
            if (order == null) return new JSONObject().put("success", false).put("message", "Order tidak ditemukan").toString();
            requirePlayerAccount(order.getAccountId());
            Instrument instrument = facade.getCatalog().getById(order.getInstrumentId());
            Currency currency = instrument.getCurrency();
            BigDecimal filledValue = BigDecimal.ZERO;
            BigDecimal actualFee = BigDecimal.ZERO;
            long filledQuantity = 0;
            boolean feeDataAvailable = true;
            com.tradingglobal3d.core.npc.MarketEngine market = facade.getMarketEngine();
            com.tradingglobal3d.core.trading.OrderBook book = market.getBook(order.getInstrumentId());
            if (book != null) for (TradeFill fill : book.getTradeHistory()) {
                boolean belongsToOrder = order.isBuy()
                        ? order.getId().equals(fill.getBuyOrderId())
                        : order.getId().equals(fill.getSellOrderId());
                if (!belongsToOrder) continue;
                filledQuantity = Math.addExact(filledQuantity, fill.getQuantity());
                filledValue = filledValue.add(fill.getPrice().getAmount().multiply(BigDecimal.valueOf(fill.getQuantity())));
                Money fee = order.isBuy() ? fill.getBuyerFee() : fill.getSellerFee();
                if (fee == null) feeDataAvailable = false;
                else actualFee = actualFee.add(fee.getAmount());
            }
            feeDataAvailable = feeDataAvailable && filledQuantity == order.getFilledQuantity() && filledQuantity > 0;
            JSONObject result = new JSONObject().put("success", true).put("orderId", order.getId()).put("requestId", order.getRequestId())
                    .put("accountId", order.getAccountId()).put("instrumentId", order.getInstrumentId())
                    .put("status", order.getStatus().name()).put("filledQuantity", order.getFilledQuantity())
                    .put("remainingQuantity", order.getRemainingQuantity())
                    .put("cancelledQuantity", order.getStatus() == OrderStatus.CANCELLED ? order.getRemainingQuantity() : 0)
                    .put("canCancel", !order.getStatus().isFinal())
                    .put("side", order.getSide().name()).put("type", order.getType().name())
                    .put("quantity", order.getTotalQuantity()).put("createdAt", order.getCreatedAt())
                    .put("currency", currency.name()).put("feeDataAvailable", feeDataAvailable);
            if (filledQuantity > 0) {
                result.put("filledValueRaw", filledValue.toPlainString())
                        .put("filledValue", new Money(filledValue, currency).format())
                        .put("averageFillPriceRaw", filledValue.divide(BigDecimal.valueOf(filledQuantity), 6, java.math.RoundingMode.HALF_UP).toPlainString())
                        .put("actualFeeRaw", feeDataAvailable ? actualFee.toPlainString() : JSONObject.NULL)
                        .put("cashImpactRaw", feeDataAvailable
                                ? (order.isBuy() ? filledValue.add(actualFee) : filledValue.subtract(actualFee)).toPlainString()
                                : JSONObject.NULL)
                        .put("cashImpactDirection", order.isBuy() ? "OUTFLOW" : "INFLOW");
            } else {
                result.put("filledValueRaw", JSONObject.NULL).put("filledValue", JSONObject.NULL)
                        .put("averageFillPriceRaw", JSONObject.NULL).put("actualFeeRaw", JSONObject.NULL)
                        .put("cashImpactRaw", JSONObject.NULL)
                        .put("cashImpactDirection", order.isBuy() ? "OUTFLOW" : "INFLOW");
            }
            return result.toString();
        } catch (Exception error) { return "{\"success\":false}"; }
    }

    @JavascriptInterface
    public String cancelOrder(String requestId) {
        try {
            Order order = facade.getOrderManager().getOrderByRequest(requestId);
            if (order != null) { requirePlayerAccount(order.getAccountId()); facade.cancelOrder(order); facade.save(); }
            return getOrderStatus(requestId);
        } catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String setClockState(boolean playing, int speed) {
        try { return facade.setClockState(playing, speed); }
        catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String onAppLifecycleChanged(boolean isForeground) {
        try {
            JSONObject res = facade.onAppLifecycleChanged(isForeground);
            return res != null ? res.toString() : "{\"success\":true}";
        } catch (Exception e) {
            try { return new JSONObject().put("success", false).put("error", e.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String getOfflineCatchupReport() {
        JSONObject r = facade.getLastOfflineCatchupReport();
        return r != null ? r.toString() : "null";
    }

    @JavascriptInterface
    public String syncClock() {
        try { facade.syncClock(); return getSnapshot(); }
        catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String tickClock(int seconds) {
        return "{\"success\":false,\"message\":\"Waktu berjalan otomatis; gunakan sinkronisasi jam\"}";
    }

    @JavascriptInterface
    public String skipDay(String requestId) {
        return "{\"success\":false,\"message\":\"Melewati waktu tidak tersedia; dunia berjalan otomatis\"}";
    }

    @JavascriptInterface
    public String saveGame() {
        try {
            facade.save();
            return "{\"success\":true,\"message\":\"Game berhasil disimpan secara atomik\"}";
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    @JavascriptInterface
    public String loadGame() {
        try {
            boolean exists = facade.getSaveManager().getSaveFile().exists() || facade.getSaveManager().getBackupFile().exists();
            boolean ok = exists && facade.startOrResume();
            return "{\"success\":" + ok + "}";
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    @JavascriptInterface
    public String getStressTestScenarios() {
        try {
            JSONArray result = new JSONArray();
            com.tradingglobal3d.core.replay.StressTestScenario crash =
                    com.tradingglobal3d.core.replay.StressTestScenario.defaultMarketCrash();
            result.put(new JSONObject()
                    .put("id", crash.getId())
                    .put("name", crash.getName())
                    .put("type", crash.getType().name())
                    .put("assetDropMultiplier", crash.getAssetDropMultiplier().toPlainString())
                    .put("fxRateMultiplier", crash.getFxRateMultiplier().toPlainString())
                    .put("description", crash.getDescription()));

            com.tradingglobal3d.core.replay.StressTestScenario fx =
                    com.tradingglobal3d.core.replay.StressTestScenario.defaultFxShock();
            result.put(new JSONObject()
                    .put("id", fx.getId())
                    .put("name", fx.getName())
                    .put("type", fx.getType().name())
                    .put("assetDropMultiplier", fx.getAssetDropMultiplier().toPlainString())
                    .put("fxRateMultiplier", fx.getFxRateMultiplier().toPlainString())
                    .put("description", fx.getDescription()));

            return result.toString();
        } catch (Exception error) { return "[]"; }
    }

    @JavascriptInterface
    public String runStressTest(String scenarioId) {
        try {
            com.tradingglobal3d.core.replay.StressTestScenario scenario =
                    "STRESS-FX-DEPRECIATION".equalsIgnoreCase(scenarioId)
                            ? com.tradingglobal3d.core.replay.StressTestScenario.defaultFxShock()
                            : com.tradingglobal3d.core.replay.StressTestScenario.defaultMarketCrash();

            com.tradingglobal3d.core.replay.StressTestResult res = facade.runStressTest(scenario);
            return new JSONObject().put("success", true)
                    .put("scenarioId", res.getScenarioId())
                    .put("baselineValue", res.getBaselineValue().format())
                    .put("stressedValue", res.getStressedValue().format())
                    .put("potentialLoss", res.getPotentialLoss().format())
                    .put("percentageDrop", (res.getPercentageDrop().multiply(BigDecimal.valueOf(100)).setScale(1, java.math.RoundingMode.HALF_UP)) + "%")
                    .put("wouldTriggerMarginCall", res.wouldTriggerMarginCall())
                    .put("riskAssessment", res.getRiskAssessment())
                    .put("message", "Uji stres " + scenario.getName() + " selesai.").toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }

    @JavascriptInterface
    public String researchQuery(String jsonRequest) {
        try { return facade.researchQuery(new JSONObject(jsonRequest)).toString(); }
        catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String corporateOfferAction(String jsonRequest) {
        try { return facade.corporateOfferAction(new JSONObject(jsonRequest)).toString(); }
        catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String strategyLab(String jsonRequest) {
        try { return facade.strategyLab(new JSONObject(jsonRequest)).toString(); }
        catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String createStrategyLab(String sandboxId) {
        try {
            String sid = sandboxId == null || sandboxId.trim().isEmpty() ? "LAB-" + java.util.UUID.randomUUID() : sandboxId;
            JSONObject result = facade.strategyLab(new JSONObject().put("action", "create").put("sandboxId", sid));
            JSONObject state = result.getJSONObject("state");
            { java.util.Iterator<String> _sk = state.keys(); while (_sk.hasNext()) { String key = _sk.next(); result.put(key, state.get(key)); } }
            return result.put("message", "Lab terisolasi berhasil disimpan").toString();
        } catch (Exception error) { return bridgeError(error); }
    }

    @JavascriptInterface
    public String simulateLabTrade(String jsonRequest) {
        try {
            JSONObject request = new JSONObject(jsonRequest).put("action", "trade");
            JSONObject result = facade.strategyLab(request);
            JSONObject state = result.getJSONObject("state");
            { java.util.Iterator<String> _sk2 = state.keys(); while (_sk2.hasNext()) { String key = _sk2.next(); result.put(key, state.get(key)); } }
            return result.put("message", result.getBoolean("success") ? "Keputusan lab tersimpan" : "Kas/posisi lab tidak cukup").toString();
        } catch (Exception error) { return bridgeError(error); }
    }
    @JavascriptInterface
    public String startNewGame(String jsonConfig) {
        try {
            JSONObject cfg = jsonConfig != null && !jsonConfig.isEmpty() ? new JSONObject(jsonConfig) : new JSONObject();
            String modeStr = cfg.optString("mode", "INTERNAL_MARKET");
            com.tradingglobal3d.core.model.GameMode mode = com.tradingglobal3d.core.model.GameMode.valueOf(modeStr);
            long capital = cfg.optLong("initialCapital", 50_000_000L);
            if (capital <= 0) capital = 50_000_000L;

            if (mode == com.tradingglobal3d.core.model.GameMode.HISTORICAL_REPLAY) {
                String datasetId = cfg.optString("datasetId", "US:AAPL");
                JSONObject replayResult = facade.startHistoricalReplay(datasetId, BigDecimal.valueOf(capital));
                facade.save();
                return replayResult.toString();
            }

            facade.startInternalGame(BigDecimal.valueOf(capital));
            facade.save();
            return new JSONObject().put("success", true)
                    .put("mode", mode.name())
                    .put("initialCapital", capital)
                    .put("message", "Permainan baru berhasil dimulai dengan modal awal " + Money.of(capital, Currency.IDR).format()).toString();
        } catch (Exception error) {
            try { return new JSONObject().put("success", false).put("message", error.getMessage()).toString(); }
            catch (JSONException impossible) { return "{\"success\":false}"; }
        }
    }
}
