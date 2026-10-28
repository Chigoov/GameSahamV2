package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.*;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.corporate.*;
import com.tradingglobal3d.core.storage.*;
import org.json.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public class ShareSupplyPersistenceTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private TradingFacade game(File directory, boolean load) throws Exception {
        TradingFacade game = new TradingFacade(SaveManager.forDirectory(directory));
        game.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")), StandardCharsets.UTF_8)));
        game.setRealTimeClock(java.time.Clock.fixed(java.time.Instant.ofEpochMilli(1792756800000L), java.time.ZoneOffset.UTC));
        if (load) assertTrue(game.load()); else assertTrue(game.startOrResume());
        return game;
    }
    private CorporateAction split(TradingFacade game, String id, String ratio) throws Exception {
        JSONObject state = game.getCorporateService().exportState();
        String date = game.getClock().getCurrentDate().toString();
        state.getJSONArray("corporateActions").put(new JSONObject().put("id", id).put("ticker", "BBCA")
                .put("type", new BigDecimal(ratio).compareTo(BigDecimal.ONE) < 0 ? "REVERSE_SPLIT" : "STOCK_SPLIT")
                .put("valueOrRatio", ratio).put("currency", "IDR").put("recordingDate", date).put("executionDate", date).put("isExecuted", false));
        game.getCorporateService().restoreState(state);
        return game.getCorporateService().getCorporateActions().stream().filter(action -> action.getId().equals(id)).findFirst().get();
    }
    private JSONObject accountsAndLedger(SaveState state) throws Exception {
        JSONObject root = new JSONObject(state.toJsonString());
        return new JSONObject().put("accounts", root.getJSONArray("accounts")).put("ledger", root.getJSONArray("ledger"));
    }
    @Test public void legacySupplyMigrationIsIdempotentAndDoesNotAlterAccountsOrLedger() throws Exception {
        File directory = temp.newFolder();
        TradingFacade game = game(directory, false);
        long initial = game.getCatalog().getById("IDX:BBCA").getSharesOutstanding();
        game.executeStockSplit(split(game, "MIGRATE-SPLIT", "2"));
        game.save();
        SaveManager saves = SaveManager.forDirectory(directory);
        SaveState legacy = saves.load().getState();
        JSONObject accounts = accountsAndLedger(legacy);
        JSONArray catalog = legacy.getRuntimeState().getJSONObject("catalog").getJSONArray("instruments");
        for (int i = 0; i < catalog.length(); i++) catalog.getJSONObject(i).remove("shareSupply");
        saves.save(legacy);
        TradingFacade migrated = game(directory, true);
        assertEquals(initial * 2, migrated.getCatalog().getById("IDX:BBCA").getSharesOutstanding());
        migrated.save();
        assertEquals(accounts.toString(), accountsAndLedger(saves.load().getState()).toString());
        JSONObject ownership = migrated.getIssuerOwnership("IDX:BBCA");
        TradingFacade reloaded = game(directory, true);
        assertEquals(ownership.toString(), reloaded.getIssuerOwnership("IDX:BBCA").toString());
        assertTrue(reloaded.reconcileAll());
    }
    @Test public void invalidSupplyIsRejectedBeforeReplacingLiveState() throws Exception {
        File directory = temp.newFolder();
        TradingFacade game = game(directory, false);
        game.save();
        SaveManager saves = SaveManager.forDirectory(directory);
        SaveState corrupt = saves.load().getState();
        JSONArray instruments = corrupt.getRuntimeState().getJSONObject("catalog").getJSONArray("instruments");
        instruments.getJSONObject(0).getJSONObject("shareSupply").put("issuedShares", -1);
        saves.save(corrupt);
        Account personal = game.getPersonalAccount();
        Money cash = personal.getTotalCash(Currency.IDR);
        long supply = game.getCatalog().getById("IDX:BBCA").getSharesOutstanding();
        assertFalse(game.load());
        assertSame(personal, game.getPersonalAccount());
        assertEquals(cash, game.getPersonalAccount().getTotalCash(Currency.IDR));
        assertEquals(supply, game.getCatalog().getById("IDX:BBCA").getSharesOutstanding());
    }
    @Test public void inconsistentPositiveSupplyIsRejectedBeforeReplacingLiveState() throws Exception {
        File directory = temp.newFolder();
        TradingFacade game = game(directory, false);
        game.save();
        SaveManager saves = SaveManager.forDirectory(directory);
        SaveState inconsistent = saves.load().getState();
        JSONObject supply = inconsistent.getRuntimeState().getJSONObject("catalog").getJSONArray("instruments").getJSONObject(0).getJSONObject("shareSupply");
        supply.put("issuedShares", supply.getLong("issuedShares") + 1);
        saves.save(inconsistent);
        Account personal = game.getPersonalAccount();
        assertFalse(game.load());
        assertSame(personal, game.getPersonalAccount());
    }
    @Test public void reverseSplitChangesSupplyOnceAndNewGameResetsIt() throws Exception {
        TradingFacade game = game(temp.newFolder(), false);
        Instrument instrument = game.getCatalog().getById("IDX:BBCA");
        long supply = instrument.getSharesOutstanding();
        Money cap = instrument.getMarketCap();
        CorporateAction action = split(game, "REVERSE-SUPPLY", "0.5");
        game.executeStockSplit(action);
        assertEquals(supply / 2, instrument.getSharesOutstanding());
        assertEquals(cap, instrument.getMarketCap());
        game.executeStockSplit(action);
        assertEquals(supply / 2, instrument.getSharesOutstanding());
        game.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50000000));
        assertEquals(supply, game.getCatalog().getById("IDX:BBCA").getSharesOutstanding());
    }
    @Test public void splitSupplyOverflowCannotChangeAccountPositions() throws Exception {
        TradingFacade game = game(temp.newFolder(), false);
        Position before = game.getAccount("ACC-NPC-MM-01").getPosition("IDX:BBCA");
        long quantity = before.getQuantity();
        BigDecimal price = before.getCurrentPrice();
        CorporateAction action = split(game, "OVERFLOW-SPLIT", "100000000");
        assertThrows(ArithmeticException.class, () -> game.executeStockSplit(action));
        assertEquals(quantity, game.getAccount("ACC-NPC-MM-01").getPosition("IDX:BBCA").getQuantity());
        assertEquals(price, game.getAccount("ACC-NPC-MM-01").getPosition("IDX:BBCA").getCurrentPrice());
        assertFalse(action.isExecuted());
    }
    @Test public void mergerUpdatesTargetIssuedAndSourceTreasuryWithoutChangingCash() throws Exception {
        TradingFacade game = game(temp.newFolder(), false);
        Account personal = game.getPersonalAccount();
        personal.addPosition(new Position("TLKM", "IDX", Currency.IDR, 200, new BigDecimal("4000"), new BigDecimal("4000")));
        java.util.Map<String, Account> accounts = new java.util.LinkedHashMap<>();
        for (Account account : game.getAllAccounts()) accounts.put(account.getId(), account);
        java.time.LocalDate base = game.getClock().getCurrentDate();
        long sourceSupply = game.getCatalog().getById("IDX:TLKM").getSharesOutstanding();
        long targetSupply = game.getCatalog().getById("IDX:ASII").getSharesOutstanding();
        Money cash = personal.getTotalCash(Currency.IDR);
        game.getCorporateService().processDate(base.plusDays(4), accounts, game.getLedger(), game.getClock().getGameTimestamp());
        game.getCorporateService().participateOffer("MERGER-TLKM-ASII", personal, 200, "QA-MERGER", base.plusDays(4), game.getLedger(), game.getClock().getGameTimestamp());
        game.getCorporateService().processDate(base.plusDays(8), accounts, game.getLedger(), game.getClock().getGameTimestamp());
        for (Account account : accounts.values()) if (game.getAccount(account.getId()) == null) game.registerAccount(account);
        assertEquals(sourceSupply - 200, game.getCatalog().getById("IDX:TLKM").getSharesOutstanding());
        assertEquals(targetSupply + 100, game.getCatalog().getById("IDX:ASII").getSharesOutstanding());
        assertEquals(100, personal.getPosition("IDX:ASII").getQuantity());
        assertEquals(cash, personal.getTotalCash(Currency.IDR));
        JSONObject snapshot = game.getIssuerOwnership("IDX:TLKM");
        assertEquals(200, snapshot.getLong("treasuryShares"));
        assertTrue(snapshot.getBoolean("supplyReconciled"));
        game.save(); assertTrue(game.load());
        assertEquals(snapshot.toString(), game.getIssuerOwnership("IDX:TLKM").toString());
        assertTrue(game.reconcileAll());
    }
    @Test public void supplyEventRetriesRejectConflictingPayloadAndOverflowWithoutMutation() throws Exception {
        Instrument instrument = new Instrument("IDX:QA", "QA", "QA", MarketExchange.IDX, "QA", Currency.IDR, 100, 1000, true, Money.of(1000, Currency.IDR));
        instrument.applyShareSupplyEvent("RIGHTS-QA", BigDecimal.ONE, 100, 0);
        instrument.applyShareSupplyEvent("RIGHTS-QA", BigDecimal.ONE, 100, 0);
        assertEquals(1100, instrument.getSharesOutstanding());
        assertThrows(IllegalArgumentException.class, () -> instrument.applyShareSupplyEvent("RIGHTS-QA", BigDecimal.ONE, 200, 0));
        assertThrows(ArithmeticException.class, () -> instrument.applyShareSupplyEvent("OVERFLOW", BigDecimal.valueOf(Long.MAX_VALUE), 0, 0));
        assertEquals(1100, instrument.getSharesOutstanding());
    }
}
