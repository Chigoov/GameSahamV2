package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.corporate.CorporateActionService;
import com.tradingglobal3d.core.corporate.IpoOffering;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.storage.SaveState;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class CatalogExpansionRegressionTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    private String definition() throws Exception {
        File file = new File("src/main/assets/data/market_universe.json");
        if (!file.exists()) file = new File("android/app/src/main/assets/data/market_universe.json");
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    private TradingFacade current(SaveManager saves) throws Exception {
        TradingFacade game = new TradingFacade(saves);
        game.setCatalog(InstrumentCatalog.fromJsonString(definition()));
        return game;
    }

    private String legacyDefinition() throws Exception {
        JSONObject source = new JSONObject(definition());
        JSONArray legacy = new JSONArray();
        // The production JSON preserves the original 26 IDs before appending new companies.
        for (int i = 0; i < 26; i++) {
            JSONObject item = new JSONObject(source.getJSONArray("instruments").getJSONObject(i).toString());
            item.put("isActive", i < 20);
            legacy.put(item);
        }
        source.put("instruments", legacy);
        return source.toString();
    }

    private SaveManager legacySave() throws Exception {
        SaveManager saves = SaveManager.forDirectory(temporary.newFolder());
        TradingFacade game = new TradingFacade(saves);
        game.setCatalog(InstrumentCatalog.fromJsonString(legacyDefinition()));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        game.getCatalog().getById("IDX:BBCA").updatePrice(Money.of(11250, Currency.IDR), 42);
        game.getCatalog().addToWatchlist("IDX:BBNI");
        game.getPersonalAccount().addPosition(new Position("BBCA", "IDX", Currency.IDR, 100,
                BigDecimal.valueOf(10250), BigDecimal.valueOf(11250)));
        game.save();
        SaveState state = saves.load().getState();
        state.getRuntimeState().getJSONObject("catalog").remove("catalogVersion");
        saves.save(state);
        return saves;
    }

    @Test public void allottedLegacyIpoIsPreservedWithoutGrantingDealerInventory() throws Exception {
        SaveManager saves = legacySave();
        SaveState state = saves.load().getState();
        JSONObject runtime = state.getRuntimeState();
        CorporateActionService service = new CorporateActionService();
        service.restoreState(runtime.getJSONObject("corporate"));
        IpoOffering ipo = new IpoOffering("IPO-MIGRATION", "NCLD", "Migration IPO", Currency.IDR,
                Money.of(100, Currency.IDR), 100, 100000, state.getGameDate());
        ipo.markAllotted(); service.registerIpoOffering(ipo);
        runtime.put("corporate", service.exportState());
        runtime.getJSONObject("catalog").getJSONArray("instruments").put(new JSONObject()
                .put("id", "IDX:NCLD").put("price", "125").put("previous", "100")
                .put("high", "125").put("low", "100").put("volume", 100));
        for (var account : state.getAccounts()) if (account.getType() == com.tradingglobal3d.core.model.AccountType.PERSONAL)
            account.addPosition(new Position("NCLD", "IDX", Currency.IDR, 100, BigDecimal.valueOf(100), BigDecimal.valueOf(125)));
        saves.save(state);
        TradingFacade game = current(saves);
        assertTrue(game.load());
        assertEquals(101, game.getCatalog().getAllInstruments().size());
        assertEquals(Money.of(125, Currency.IDR), game.getCatalog().getById("IDX:NCLD").getCurrentPrice());
        assertEquals(100, game.getPersonalAccount().getPosition("IDX:NCLD").getQuantity());
        assertNull(game.getAccount("ACC-NPC-MM-01").getPosition("IDX:NCLD"));
        game.save();
        TradingFacade restored = current(saves);
        assertTrue(restored.load());
        assertEquals(101, restored.getCatalog().getAllInstruments().size());
        assertNull(restored.getAccount("ACC-NPC-MM-01").getPosition("IDX:NCLD"));
    }

    @Test public void legacyReplayAndInternalSlotsMigrateThroughModeSwitchWithoutLosingIdentity() throws Exception {
        SaveManager primary = legacySave();
        TradingFacade legacy = new TradingFacade(primary);
        legacy.setCatalog(InstrumentCatalog.fromJsonString(legacyDefinition()));
        assertTrue(legacy.load());
        String internalRun = new JSONObject(legacy.getSnapshotJson()).getString("runId");
        legacy.startHistoricalReplay("US:AAPL", BigDecimal.valueOf(30_000_000));
        legacy.save();
        String replayRun = new JSONObject(legacy.getSnapshotJson()).getString("runId");
        String replayState = new JSONObject(legacy.getSnapshotJson()).getJSONObject("replay").toString();
        for (SaveManager slot : new SaveManager[]{primary, legacy.getSaveManager()}) {
            SaveState state = slot.load().getState();
            state.getRuntimeState().getJSONObject("catalog").remove("catalogVersion");
            slot.save(state);
        }
        TradingFacade game = current(primary);
        assertTrue(game.load());
        JSONObject replay = game.resumeSavedMode("HISTORICAL_REPLAY");
        assertEquals(replayRun, replay.getString("runId"));
        assertEquals(replayState, replay.getJSONObject("replay").toString());
        assertEquals(100, game.getCatalog().getAllInstruments().size());
        game.save();
        JSONObject internal = game.resumeSavedMode("INTERNAL_MARKET");
        assertEquals(internalRun, internal.getString("runId"));
        assertEquals(Money.of(50_000_000, Currency.IDR), game.getPersonalAccount().getTotalCash(Currency.IDR));
        assertEquals(replayRun, game.resumeSavedMode("HISTORICAL_REPLAY").getString("runId"));
        assertEquals(100, game.getCatalog().getAllInstruments().size());
    }

    @Test public void oneMissingNewCompanyInCurrentVersionIsRejected() throws Exception {
        SaveManager saves = legacySave();
        TradingFacade upgraded = current(saves);
        assertTrue(upgraded.load()); upgraded.save();
        SaveState state = saves.load().getState();
        JSONArray entries = state.getRuntimeState().getJSONObject("catalog").getJSONArray("instruments");
        for (int i = 0; i < entries.length(); i++) if ("US:NEE".equals(entries.getJSONObject(i).getString("id"))) { entries.remove(i); break; }
        assertEquals(99, entries.length()); saves.save(state);
        assertFalse(current(saves).load());
    }

    @Test public void old26CompanySaveExpandsWithoutChangingPlayerStateOrReplenishingInventory() throws Exception {
        SaveManager saves = legacySave();
        byte[] original = Files.readAllBytes(saves.getSaveFile().toPath());
        SaveState before = saves.load().getState();
        TradingFacade game = current(saves);
        assertTrue(game.load());
        assertEquals(100, game.getCatalog().getActiveUniverse().size());
        assertEquals(Money.of(50_000_000, Currency.IDR), game.getPersonalAccount().getTotalCash(Currency.IDR));
        assertEquals(100, game.getPersonalAccount().getPosition("IDX:BBCA").getQuantity());
        assertEquals(Money.of(11250, Currency.IDR), game.getCatalog().getById("IDX:BBCA").getCurrentPrice());
        assertEquals(42, game.getCatalog().getById("IDX:BBCA").getDayVolume());
        assertEquals(before.getGameDate(), game.getClock().getCurrentDate());
        assertEquals(before.getRuntimeState().getString("runId"), new JSONObject(game.getSnapshotJson()).getString("runId"));
        assertTrue(game.getCatalog().isInWatchlist("IDX:BBNI"));
        assertArrayEquals("Load migration must not overwrite the original file", original, Files.readAllBytes(saves.getSaveFile().toPath()));
        assertEquals(20000, game.getAccount("ACC-NPC-MM-01").getPosition("IDX:BBNI").getQuantity());
        Position newStock = game.getAccount("ACC-NPC-MM-01").getPosition("US:NEE");
        assertNotNull(newStock);
        assertEquals(200, newStock.getQuantity());
        newStock.addSell(200, BigDecimal.valueOf(100));
        game.save();
        assertArrayEquals("First upgraded save retains old bytes in existing atomic backup", original, Files.readAllBytes(saves.getBackupFile().toPath()));
        assertEquals(2, saves.load().getState().getRuntimeState().getJSONObject("catalog").getInt("catalogVersion"));
        TradingFacade reloaded = current(saves);
        assertTrue(reloaded.load());
        assertEquals(0, reloaded.getAccount("ACC-NPC-MM-01").getPosition("US:NEE").getQuantity());
        assertEquals(100, reloaded.getCatalog().getAllInstruments().size());
        assertTrue(reloaded.reconcileAll());
    }

    @Test public void incompleteLegacyAndCurrentSavesStayRejectedWithoutChangingLiveGame() throws Exception {
        for (boolean legacy : new boolean[]{true, false}) {
            SaveManager saves = legacySave();
            SaveState state = saves.load().getState();
            JSONObject saved = state.getRuntimeState().getJSONObject("catalog");
            if (legacy) saved.getJSONArray("instruments").remove(0);
            else saved.put("catalogVersion", 2);
            saves.save(state);
            byte[] bytes = Files.readAllBytes(saves.getSaveFile().toPath());
            TradingFacade game = current(saves);
            game.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(75_000_000));
            assertFalse(game.load());
            assertEquals(Money.of(75_000_000, Currency.IDR), game.getPersonalAccount().getTotalCash(Currency.IDR));
            assertEquals(100, game.getCatalog().getAllInstruments().size());
            assertArrayEquals(bytes, Files.readAllBytes(saves.getSaveFile().toPath()));
        }
    }

    @Test public void malformedVersionDuplicateAndUnknownIdsCannotTriggerMigration() throws Exception {
        for (Object version : new Object[]{JSONObject.NULL, "bad", "1", 1.5, 0, 3}) {
            SaveManager saves = legacySave();
            SaveState state = saves.load().getState();
            state.getRuntimeState().getJSONObject("catalog").put("catalogVersion", version);
            saves.save(state);
            assertFalse("Invalid catalogVersion: " + version, current(saves).load());
        }
        for (String id : new String[]{"IDX:BBCA", "IDX:UNKNOWN"}) {
            SaveManager saves = legacySave();
            SaveState state = saves.load().getState();
            JSONArray entries = state.getRuntimeState().getJSONObject("catalog").getJSONArray("instruments");
            entries.put(new JSONObject(entries.getJSONObject(0).toString()).put("id", id));
            saves.save(state);
            assertFalse(current(saves).load());
        }
    }
}
