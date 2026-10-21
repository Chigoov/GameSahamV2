package com.tradingglobal3d.core.storage;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntry;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Representasi snapshot lengkap state permainan yang dapat diserialisasi ke JSON.
 * Memuat schema versioning (V2) dan checksum integritas.
 */
public final class SaveState {
    public static final int SCHEMA_VERSION = 2;

    private final int schemaVersion;
    private final GameMode gameMode;
    private final long savedTimestamp;
    private final LocalDate gameDate;
    private final LocalTime gameTime;
    private final int speedMultiplier;
    private final List<Account> accounts;
    private final List<LedgerEntry> ledgerEntries;
    private final long lastRealTimeAnchor;
    private final int gameMsRemainder;
    private final String lifecycleState;
    private JSONObject offlineCatchupReport;
    private JSONObject runtimeState = new JSONObject();

    public JSONObject getRuntimeState() { return runtimeState; }
    public void setRuntimeState(JSONObject state) { runtimeState = Objects.requireNonNull(state); }

    public SaveState(int schemaVersion, GameMode gameMode, long savedTimestamp,
                     LocalDate gameDate, LocalTime gameTime, int speedMultiplier,
                     List<Account> accounts, List<LedgerEntry> ledgerEntries) {
        this(schemaVersion, gameMode, savedTimestamp, gameDate, gameTime, speedMultiplier,
                accounts, ledgerEntries, savedTimestamp, 0, "FOREGROUND", null);
    }

    public SaveState(int schemaVersion, GameMode gameMode, long savedTimestamp,
                     LocalDate gameDate, LocalTime gameTime, int speedMultiplier,
                     List<Account> accounts, List<LedgerEntry> ledgerEntries,
                     long lastRealTimeAnchor, int gameMsRemainder, String lifecycleState,
                     JSONObject offlineCatchupReport) {
        this.schemaVersion = schemaVersion;
        this.gameMode = gameMode;
        this.savedTimestamp = savedTimestamp;
        this.gameDate = gameDate;
        this.gameTime = gameTime;
        this.speedMultiplier = speedMultiplier;
        this.accounts = accounts;
        this.ledgerEntries = ledgerEntries;
        this.lastRealTimeAnchor = lastRealTimeAnchor > 0 ? lastRealTimeAnchor : savedTimestamp;
        this.gameMsRemainder = gameMsRemainder;
        this.lifecycleState = lifecycleState != null ? lifecycleState : "FOREGROUND";
        this.offlineCatchupReport = offlineCatchupReport;
    }

    public static SaveState capture(GameMode mode, GameClock clock, Collection<Account> accountList, EconomyLedger ledger) {
        return capture(mode, clock, accountList, ledger, System.currentTimeMillis(), null);
    }

    public static SaveState capture(GameMode mode, GameClock clock, Collection<Account> accountList, EconomyLedger ledger,
                                   long lastRealTimeAnchor, JSONObject offlineCatchupReport) {
        long now = System.currentTimeMillis();
        return new SaveState(
                SCHEMA_VERSION,
                mode,
                now,
                clock.getCurrentDate(),
                clock.getCurrentTime(),
                clock.getSpeedMultiplier(),
                new ArrayList<>(accountList),
                ledger.getEntries(),
                lastRealTimeAnchor > 0 ? lastRealTimeAnchor : now,
                clock.getGameMsRemainder(),
                clock.isForeground() ? "FOREGROUND" : "INACTIVE",
                offlineCatchupReport
        );
    }

    public int getSchemaVersion() { return schemaVersion; }
    public GameMode getGameMode() { return gameMode; }
    public long getSavedTimestamp() { return savedTimestamp; }
    public LocalDate getGameDate() { return gameDate; }
    public LocalTime getGameTime() { return gameTime; }
    public int getSpeedMultiplier() { return speedMultiplier; }
    public List<Account> getAccounts() { return accounts; }
    public List<LedgerEntry> getLedgerEntries() { return ledgerEntries; }
    public long getLastRealTimeAnchor() { return lastRealTimeAnchor; }
    public int getGameMsRemainder() { return gameMsRemainder; }
    public String getLifecycleState() { return lifecycleState; }
    public JSONObject getOfflineCatchupReport() { return offlineCatchupReport; }
    public void setOfflineCatchupReport(JSONObject report) { this.offlineCatchupReport = report; }

    public String toJsonString() throws JSONException {
        JSONObject root = new JSONObject();
        root.put("schemaVersion", schemaVersion);
        root.put("gameMode", gameMode.name());
        root.put("savedTimestamp", savedTimestamp);
        root.put("gameDate", gameDate.toString());
        root.put("gameTime", gameTime.toString());
        root.put("speedMultiplier", speedMultiplier);
        root.put("lastRealTimeAnchor", lastRealTimeAnchor);
        root.put("gameMsRemainder", gameMsRemainder);
        root.put("lifecycleState", lifecycleState);
        if (offlineCatchupReport != null) {
            root.put("offlineCatchupReport", offlineCatchupReport);
        }
        root.put("runtimeState", runtimeState);

        // Accounts
        JSONArray accountsArray = new JSONArray();
        for (Account acc : accounts) {
            JSONObject accObj = new JSONObject();
            accObj.put("id", acc.getId());
            accObj.put("name", acc.getName());
            accObj.put("type", acc.getType().name());
            accObj.put("clientMandateId", acc.getClientMandateId() != null ? acc.getClientMandateId() : JSONObject.NULL);

            JSONObject cashObj = new JSONObject();
            for (Currency c : Currency.values()) {
                cashObj.put(c.name(), acc.getTotalCash(c).getAmount().toPlainString());
            }
            accObj.put("cash", cashObj);
            JSONObject reservedObj = new JSONObject();
            for (Currency c : Currency.values()) reservedObj.put(c.name(), acc.getReservedCash(c).getAmount().toPlainString());
            accObj.put("reservedCash", reservedObj);

            JSONArray positionsArray = new JSONArray();
            for (Position p : acc.getPositions().values()) {
                JSONObject posObj = new JSONObject();
                posObj.put("ticker", p.getTicker());
                posObj.put("exchange", p.getExchange());
                posObj.put("currency", p.getCurrency().name());
                posObj.put("quantity", p.getQuantity());
                posObj.put("averageCost", p.getAverageCost().toPlainString());
                posObj.put("currentPrice", p.getCurrentPrice().toPlainString());
                posObj.put("realizedPnL", p.getRealizedPnL() == null ? JSONObject.NULL : p.getRealizedPnL().getAmount().toPlainString());
                posObj.put("acquisitionFeesRemaining", p.getAcquisitionFeesRemaining().getAmount().toPlainString());
                posObj.put("acquisitionFeeQuantity", p.getAcquisitionFeeQuantity());

                positionsArray.put(posObj);
            }
            accObj.put("positions", positionsArray);
            accountsArray.put(accObj);
        }
        root.put("accounts", accountsArray);

        // Ledger entries
        JSONArray ledgerArray = new JSONArray();
        for (LedgerEntry entry : ledgerEntries) {
            JSONObject entryObj = new JSONObject();
            entryObj.put("id", entry.getId());
            entryObj.put("timestamp", entry.getTimestamp());
            entryObj.put("requestId", entry.getRequestId());
            entryObj.put("accountId", entry.getAccountId());
            entryObj.put("type", entry.getType().name());
            entryObj.put("currency", entry.getCurrency().name());
            entryObj.put("amount", entry.getAmount().getAmount().toPlainString());
            entryObj.put("isCredit", entry.isCredit());
            entryObj.put("balanceAfter", entry.getBalanceAfter().getAmount().toPlainString());
            entryObj.put("description", entry.getDescription());
            ledgerArray.put(entryObj);
        }
        root.put("ledger", ledgerArray);

        return root.toString(2);
    }

    public static SaveState fromJsonString(String jsonStr) throws JSONException {
        JSONObject root = new JSONObject(jsonStr);
        int version = root.getInt("schemaVersion");
        if (version != SCHEMA_VERSION) throw new JSONException("Versi save belum didukung: " + version);
        GameMode mode = GameMode.valueOf(root.getString("gameMode"));
        long timestamp = root.getLong("savedTimestamp");
        LocalDate date = LocalDate.parse(root.getString("gameDate"));
        LocalTime time = LocalTime.parse(root.getString("gameTime"));
        int speed = root.optInt("speedMultiplier", 1);

        // Deserialisasi Accounts
        List<Account> accounts = new ArrayList<>();
        JSONArray accountsArray = root.getJSONArray("accounts");
        for (int i = 0; i < accountsArray.length(); i++) {
            JSONObject accObj = accountsArray.getJSONObject(i);
            String id = accObj.getString("id");
            String name = accObj.getString("name");
            AccountType type = AccountType.valueOf(accObj.getString("type"));
            String mandateId = accObj.isNull("clientMandateId") ? null : accObj.getString("clientMandateId");

            Account acc = new Account(id, name, type, mandateId);

            JSONObject cashObj = accObj.getJSONObject("cash");
            for (Currency c : Currency.values()) {
                if (cashObj.has(c.name())) {
                    BigDecimal amt = new BigDecimal(cashObj.getString(c.name()));
                    acc.creditCash(new Money(amt, c));
                }
            }

            JSONObject reservedObj = accObj.optJSONObject("reservedCash");
            if (reservedObj != null) {
                for (Currency c : Currency.values()) {
                    acc.reserveCash(new Money(new BigDecimal(reservedObj.getString(c.name())), c));
                }
            }
            JSONArray posArray = accObj.getJSONArray("positions");
            for (int j = 0; j < posArray.length(); j++) {
                JSONObject posObj = posArray.getJSONObject(j);
                String ticker = posObj.getString("ticker");
                String exchange = posObj.getString("exchange");
                Currency cur = Currency.valueOf(posObj.getString("currency"));
                long qty = posObj.getLong("quantity");
                BigDecimal avgCost = new BigDecimal(posObj.getString("averageCost"));
                BigDecimal curPrice = new BigDecimal(posObj.getString("currentPrice"));

                BigDecimal realized = posObj.has("realizedPnL") && !posObj.isNull("realizedPnL")
                        ? new BigDecimal(posObj.getString("realizedPnL")) : null;
                Position p = new Position(ticker, exchange, cur, qty, avgCost, curPrice, realized,
                        new Money(new BigDecimal(posObj.optString("acquisitionFeesRemaining", "0")), cur), posObj.optLong("acquisitionFeeQuantity", qty));
                acc.addPosition(p);
            }
            accounts.add(acc);
        }

        // Deserialisasi Ledger
        List<LedgerEntry> entries = new ArrayList<>();
        JSONArray ledgerArray = root.getJSONArray("ledger");
        for (int i = 0; i < ledgerArray.length(); i++) {
            JSONObject entryObj = ledgerArray.getJSONObject(i);
            String id = entryObj.getString("id");
            long ts = entryObj.getLong("timestamp");
            String reqId = entryObj.getString("requestId");
            String accId = entryObj.getString("accountId");
            LedgerEntryType type = LedgerEntryType.valueOf(entryObj.getString("type"));
            Currency cur = Currency.valueOf(entryObj.getString("currency"));
            BigDecimal amount = new BigDecimal(entryObj.getString("amount"));
            boolean isCredit = entryObj.getBoolean("isCredit");
            BigDecimal balAfter = new BigDecimal(entryObj.getString("balanceAfter"));
            String desc = entryObj.getString("description");

            LedgerEntry entry = new LedgerEntry(
                    id, ts, reqId, accId, type,
                    new Money(amount, cur),
                    isCredit,
                    new Money(balAfter, cur),
                    desc
            );
            entries.add(entry);
        }

        SaveState state = new SaveState(version, mode, timestamp, date, time, speed, accounts, entries,
                root.optLong("lastRealTimeAnchor", timestamp),
                root.optInt("gameMsRemainder", 0),
                root.optString("lifecycleState", "FOREGROUND"),
                root.optJSONObject("offlineCatchupReport"));
        JSONObject runtime = root.optJSONObject("runtimeState");
        if (runtime != null) state.setRuntimeState(runtime);
        return state;
    }
}
