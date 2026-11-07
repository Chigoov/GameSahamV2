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
import org.json.JSONTokener;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.io.Reader;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;
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
    public static final int COMPACTED_SCHEMA_VERSION = 3;

    static boolean supportsVersion(int version) {
        return version == SCHEMA_VERSION || version == COMPACTED_SCHEMA_VERSION;
    }

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
                ledger.hasCompactedNpcTrades() ? COMPACTED_SCHEMA_VERSION : SCHEMA_VERSION,
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

    SaveState compactValidatedNpcLedger() throws JSONException {
        if (schemaVersion != SCHEMA_VERSION || gameMode != GameMode.INTERNAL_MARKET) return this;
        Set<String> npcAccounts = new HashSet<>();
        for (Account account : accounts) {
            String id = account.getId();
            if (account.getType() == AccountType.FIRM && (id.startsWith("ACC-LEAGUE-NPC-")
                    || id.equals("ACC-NPC-MM-01") || id.equals("ACC-NPC-RETAIL-01"))) npcAccounts.add(id);
        }
        EconomyLedger compacted = new EconomyLedger();
        compacted.restoreAndCompactValidatedEntries(ledgerEntries, npcAccounts);
        if (!compacted.hasCompactedNpcTrades()) return this;
        List<LedgerEntry> retained = compacted.getEntries();
        JSONObject career = runtimeState.optJSONObject("career");
        if (career != null) {
            int oldIndex = Math.min(career.optInt("ledgerIndex", 0), ledgerEntries.size());
            int newIndex = 0;
            for (int i = 0; i < oldIndex && newIndex < retained.size(); i++)
                if (ledgerEntries.get(i) == retained.get(newIndex)) newIndex++;
            career.put("ledgerIndex", newIndex);
        }
        runtimeState.put("npcLedgerSummary", compacted.exportNpcSummary());
        SaveState migrated = new SaveState(COMPACTED_SCHEMA_VERSION, gameMode, savedTimestamp, gameDate, gameTime,
                speedMultiplier, accounts, retained, lastRealTimeAnchor, gameMsRemainder, lifecycleState, offlineCatchupReport);
        migrated.setRuntimeState(runtimeState);
        return migrated;
    }

    SaveState detachedCopy() throws JSONException {
        List<Account> copiedAccounts = new ArrayList<>();
        for (Account account : accounts) copiedAccounts.add(account.copy());
        // LedgerEntry and Money are immutable; copying their references avoids a second full ledger JSON graph.
        SaveState copy = new SaveState(schemaVersion, gameMode, savedTimestamp, gameDate, gameTime, speedMultiplier,
                copiedAccounts, new ArrayList<>(ledgerEntries), lastRealTimeAnchor, gameMsRemainder, lifecycleState,
                offlineCatchupReport == null ? null : (JSONObject) copyJson(offlineCatchupReport));
        copy.setRuntimeState((JSONObject) copyJson(runtimeState));
        return copy;
    }

    private static Object copyJson(Object value) throws JSONException {
        if (value instanceof JSONObject source) {
            JSONObject copy = new JSONObject();
            for (Iterator<String> keys = source.keys(); keys.hasNext();) {
                String key = keys.next();
                copy.put(key, copyJson(source.get(key)));
            }
            return copy;
        }
        if (value instanceof JSONArray source) {
            JSONArray copy = new JSONArray();
            for (int i = 0; i < source.length(); i++) copy.put(copyJson(source.opt(i)));
            return copy;
        }
        if (value == null || value == JSONObject.NULL || value instanceof String
                || value instanceof Number || value instanceof Boolean) return value;
        return value.toString();
    }

    public String toJsonString() throws JSONException {
        StringWriter json = new StringWriter();
        try {
            writeJson(json);
        } catch (IOException impossible) {
            throw new AssertionError(impossible);
        }
        return json.toString();
    }

    void writeJson(Writer output) throws IOException, JSONException {
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
                posObj.put("realizedPnL", p.getRealizedPnLAmount() == null ? JSONObject.NULL : p.getRealizedPnLAmount().toPlainString());
                posObj.put("acquisitionFeesRemaining", p.getAcquisitionFeesRemaining().getAmount().toPlainString());
                posObj.put("acquisitionFeeQuantity", p.getAcquisitionFeeQuantity());

                positionsArray.put(posObj);
            }
            accObj.put("positions", positionsArray);
            accountsArray.put(accObj);
        }
        root.put("accounts", accountsArray);

        JsonWriter writer = new JsonWriter(output);
        writer.beginObject();
        for (Iterator<String> keys = root.keys(); keys.hasNext();) {
            String key = keys.next();
            writer.name(key);
            writeJsonValue(writer, root.get(key));
        }
        writer.name("ledger").beginArray();
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
            writeJsonValue(writer, entryObj);
        }
        writer.endArray().endObject();
        writer.flush();
    }

    private static void writeJsonValue(JsonWriter writer, Object value) throws IOException, JSONException {
        if (value instanceof JSONObject object) {
            writer.beginObject();
            for (Iterator<String> keys = object.keys(); keys.hasNext();) {
                String key = keys.next();
                writer.name(key);
                writeJsonValue(writer, object.get(key));
            }
            writer.endObject();
        } else if (value instanceof JSONArray array) {
            writer.beginArray();
            for (int i = 0; i < array.length(); i++) writeJsonValue(writer, array.get(i));
            writer.endArray();
        } else if (value == null || value == JSONObject.NULL) {
            writer.nullValue();
        } else if (value instanceof Number number) {
            writer.value(number);
        } else if (value instanceof Boolean bool) {
            writer.value(bool);
        } else {
            writer.value(value.toString());
        }
    }

    public static SaveState fromJsonString(String jsonStr) throws JSONException {
        List<LedgerEntry> entries = new ArrayList<>();
        JSONObject root = readRoot(new JSONTokener(jsonStr), entries);
        return fromRoot(root, entries);
    }

    static SaveState fromReader(Reader input) throws IOException, JSONException {
        JsonReader reader = new JsonReader(input);
        JSONObject root = new JSONObject();
        List<LedgerEntry> entries = new ArrayList<>();
        Map<String, String> ledgerText = new HashMap<>();
        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            if (root.has(key)) throw new JSONException("Field save duplikat");
            if ("ledger".equals(key)) {
                reader.beginArray();
                while (reader.hasNext()) entries.add(readLedgerEntry((JSONObject) readJsonValue(reader), ledgerText));
                reader.endArray();
                root.put(key, true);
            } else {
                root.put(key, readJsonValue(reader));
            }
        }
        reader.endObject();
        if (reader.peek() != JsonToken.END_DOCUMENT || !root.has("ledger"))
            throw new JSONException("Akhir objek save atau ledger tidak valid");
        return fromRoot(root, entries);
    }

    private static Object readJsonValue(JsonReader reader) throws IOException, JSONException {
        switch (reader.peek()) {
            case BEGIN_OBJECT:
                JSONObject object = new JSONObject();
                reader.beginObject();
                while (reader.hasNext()) {
                    String key = reader.nextName();
                    if (object.has(key)) throw new JSONException("Field save duplikat");
                    object.put(key, readJsonValue(reader));
                }
                reader.endObject();
                return object;
            case BEGIN_ARRAY:
                JSONArray array = new JSONArray();
                reader.beginArray();
                while (reader.hasNext()) array.put(readJsonValue(reader));
                reader.endArray();
                return array;
            case STRING: return reader.nextString();
            case NUMBER: return new JSONTokener(reader.nextString()).nextValue();
            case BOOLEAN: return reader.nextBoolean();
            case NULL:
                reader.nextNull();
                return JSONObject.NULL;
            default: throw new JSONException("Nilai save tidak valid");
        }
    }

    private static SaveState fromRoot(JSONObject root, List<LedgerEntry> entries) throws JSONException {
        int version = root.getInt("schemaVersion");
        if (!supportsVersion(version)) throw new JSONException("Versi save belum didukung: " + version);
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

        SaveState state = new SaveState(version, mode, timestamp, date, time, speed, accounts, entries,
                root.optLong("lastRealTimeAnchor", timestamp),
                root.optInt("gameMsRemainder", 0),
                root.optString("lifecycleState", "FOREGROUND"),
                root.optJSONObject("offlineCatchupReport"));
        JSONObject runtime = root.optJSONObject("runtimeState");
        if (runtime != null) state.setRuntimeState(runtime);
        if ((version == COMPACTED_SCHEMA_VERSION) != state.getRuntimeState().has("npcLedgerSummary"))
            throw new JSONException("Versi save dan ringkasan ledger NPC tidak cocok");
        return state;
    }

    private static JSONObject readRoot(JSONTokener reader, List<LedgerEntry> entries) throws JSONException {
        if (reader.nextClean() != '{') throw new JSONException("Objek save tidak valid");
        JSONObject root = new JSONObject();
        char separator;
        do {
            Object name = reader.nextValue();
            if (!(name instanceof String key) || root.has(key) || reader.nextClean() != ':')
                throw new JSONException("Field save tidak valid atau duplikat");
            if ("ledger".equals(key)) {
                readLedger(reader, entries);
                root.put(key, true);
            } else {
                root.put(key, reader.nextValue());
            }
            separator = reader.nextClean();
        } while (separator == ',');
        if (separator != '}' || reader.nextClean() != 0 || !root.has("ledger"))
            throw new JSONException("Akhir objek save atau ledger tidak valid");
        return root;
    }

    private static void readLedger(JSONTokener reader, List<LedgerEntry> entries) throws JSONException {
        if (reader.nextClean() != '[') throw new JSONException("Ledger save bukan array");
        if (reader.nextClean() == ']') return;
        reader.back();
        Map<String, String> ledgerText = new HashMap<>();
        char separator;
        do {
            if (!(reader.nextValue() instanceof JSONObject row)) throw new JSONException("Baris ledger tidak valid");
            entries.add(readLedgerEntry(row, ledgerText));
            separator = reader.nextClean();
        } while (separator == ',');
        if (separator != ']') throw new JSONException("Akhir ledger save tidak valid");
    }

    private static LedgerEntry readLedgerEntry(JSONObject row, Map<String, String> ledgerText) throws JSONException {
        Currency currency = Currency.valueOf(row.getString("currency"));
        return new LedgerEntry(row.getString("id"), row.getLong("timestamp"), row.getString("requestId"),
                sharedLedgerText(row.getString("accountId"), ledgerText), LedgerEntryType.valueOf(row.getString("type")),
                new Money(new BigDecimal(row.getString("amount")), currency), row.getBoolean("isCredit"),
                new Money(new BigDecimal(row.getString("balanceAfter")), currency),
                sharedLedgerText(row.getString("description"), ledgerText));
    }

    private static String sharedLedgerText(String value, Map<String, String> ledgerText) {
        String existing = ledgerText.get(value);
        if (existing != null) return existing;
        // ponytail: bound parse-local sharing to 4096 repeated labels, so unique descriptions cannot grow a second index.
        if (ledgerText.size() < 4096) ledgerText.put(value, value);
        return value;
    }
}
