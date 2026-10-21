package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.mobile.TradingWebBridge;
import org.json.*;
import org.junit.Test;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import static org.junit.Assert.*;

public class WireIntegrationDomainTest {
    private InstrumentCatalog catalog() throws Exception {
        return InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(new File("src/main/assets/data/market_universe.json").toPath()), StandardCharsets.UTF_8));
    }
    private TradingFacade game(File dir) throws Exception {
        TradingFacade facade = new TradingFacade(SaveManager.forDirectory(dir)); facade.setCatalog(catalog());
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000")); return facade;
    }
    private File dir() throws Exception { return Files.createTempDirectory("wire-integration-").toFile(); }
    private JSONObject call(String json) throws Exception { return new JSONObject(json); }

    @Test public void persistedLabDecisionHistoryReplayAndResetNeverMutatePlayer() throws Exception {
        File dir = dir(); TradingFacade game = game(dir); TradingWebBridge bridge = new TradingWebBridge(game);
        Money original = game.getPersonalAccount().getTotalCash(Currency.IDR);
        assertTrue(call(bridge.createStrategyLab("QA-LAB")).getBoolean("success"));
        assertTrue(call(bridge.simulateLabTrade("{\"sandboxId\":\"QA-LAB\",\"instrumentId\":\"IDX:BBCA\",\"side\":\"BUY\",\"shares\":100,\"price\":9000}")).getBoolean("success"));
        TradingFacade loaded = new TradingFacade(SaveManager.forDirectory(dir)); loaded.setCatalog(catalog()); assertTrue(loaded.load());
        TradingWebBridge restored = new TradingWebBridge(loaded);
        JSONObject history = call(restored.strategyLab("{\"action\":\"history\",\"sandboxId\":\"QA-LAB\"}"));
        assertEquals(1, history.getJSONArray("decisions").length());
        String cash = history.getJSONObject("state").getString("cashIdr");
        assertTrue(call(restored.strategyLab("{\"action\":\"replay\",\"sandboxId\":\"QA-LAB\"}")).getBoolean("success"));
        assertEquals(cash, call(restored.strategyLab("{\"action\":\"history\",\"sandboxId\":\"QA-LAB\"}")).getJSONObject("state").getString("cashIdr"));
        assertEquals(original, loaded.getPersonalAccount().getTotalCash(Currency.IDR));
        assertTrue(call(restored.strategyLab("{\"action\":\"reset\",\"sandboxId\":\"QA-LAB\"}")).getBoolean("success"));
        assertEquals(0, call(restored.strategyLab("{\"action\":\"history\",\"sandboxId\":\"QA-LAB\"}")).getJSONArray("decisions").length());
    }

    @Test public void researchUsesPublishedGameClockAndEnforcesTier() throws Exception {
        TradingFacade game = game(dir()); TradingWebBridge bridge = new TradingWebBridge(game);
        assertTrue(call(bridge.researchQuery("{\"kind\":\"inbox\"}")).getBoolean("success"));
        assertFalse(call(bridge.researchQuery("{\"kind\":\"consensus\",\"instrumentId\":\"IDX:BBCA\"}")).getBoolean("success"));
        game.deposit("QA-USD", game.getPersonalAccount().getId(), Money.of(5000, Currency.USD), "QA funding");
        game.getCareerManager().restoreState(game.getCareerManager().exportState().put("rank", "SENIOR_ANALYST"));
        assertTrue(game.subscribeTerminal(com.tradingglobal3d.core.research.TerminalTier.ENTERPRISE));
        assertTrue(call(bridge.researchQuery("{\"kind\":\"consensus\",\"instrumentId\":\"IDX:BBCA\"}")).getBoolean("success"));
        assertTrue(call(bridge.researchQuery("{\"kind\":\"alternative\",\"instrumentId\":\"IDX:BBCA\"}")).getBoolean("success"));
        assertEquals(2, call(bridge.researchQuery("{\"kind\":\"comparison\",\"instrumentIds\":[\"IDX:BBCA\",\"IDX:BMRI\"]}")).getJSONArray("instruments").length());
    }

    @Test public void corporateRightsReservePersistCancelAndRetryThroughBridge() throws Exception {
        File dir = dir(); TradingFacade game = game(dir); TradingWebBridge bridge = new TradingWebBridge(game);
        game.getPersonalAccount().addPosition(new Position("BBCA", "IDX", Currency.IDR, 1000, new BigDecimal("9000"), new BigDecimal("9000")));
        game.getClock().advanceGameSeconds(2 * 86400);
        java.util.Map<String, Account> accounts = new java.util.LinkedHashMap<>(); for (Account a : game.getAllAccounts()) accounts.put(a.getId(), a);
        game.getCorporateService().processDate(game.getClock().getCurrentDate(), accounts, game.getLedger(), game.getClock().getGameTimestamp());
        assertEquals(3, call(bridge.corporateOfferAction("{\"action\":\"list\"}")).getJSONArray("offers").length());
        assertEquals("420000", call(bridge.corporateOfferAction("{\"action\":\"preview\",\"offerId\":\"RIGHTS-BBCA\",\"shares\":50}")).getString("amount"));
        String input = "{\"action\":\"participate\",\"offerId\":\"RIGHTS-BBCA\",\"shares\":50,\"requestId\":\"QA-RIGHTS\"}";
        assertTrue(call(bridge.corporateOfferAction(input)).getBoolean("success"));
        assertTrue(call(bridge.corporateOfferAction(input)).getBoolean("success"));
        assertEquals(420000, game.getPersonalAccount().getReservedCash(Currency.IDR).getAmount().longValueExact());
        assertTrue(call(bridge.corporateOfferAction("{\"action\":\"cancel\",\"requestId\":\"QA-RIGHTS\"}")).getBoolean("success"));
        assertTrue(game.getPersonalAccount().getReservedCash(Currency.IDR).isZero());
    }

    @Test public void ipoRequestRetryPersistsAndListedHoldingCanReload() throws Exception {
        File dir = dir(); TradingFacade game = game(dir); TradingWebBridge bridge = new TradingWebBridge(game);
        String request = "{\"ipoId\":\"IPO-CLOUD\",\"lots\":2,\"requestId\":\"QA-IPO\"}";
        assertTrue(call(bridge.orderIpo(request)).getBoolean("success")); assertTrue(call(bridge.orderIpo(request)).getBoolean("success"));
        assertEquals(100000, game.getPersonalAccount().getReservedCash(Currency.IDR).getAmount().longValueExact());
        game.allotIpo("IPO-CLOUD", 1.0); game.save();
        assertNotNull(game.getCatalog().getById("IDX:NCLD"));
        TradingFacade loaded = new TradingFacade(SaveManager.forDirectory(dir)); loaded.setCatalog(catalog()); assertTrue(loaded.load());
        assertNotNull(loaded.getCatalog().getById("IDX:NCLD")); assertEquals(200, loaded.getPersonalAccount().getPosition("IDX:NCLD").getQuantity());
        loaded.startInternalGame(new BigDecimal("50000000")); assertNull(loaded.getCatalog().getById("IDX:NCLD"));
    }

    @Test public void bridgeRejectsInternalAccountsAndMissingReplayPreservesCurrentRun() throws Exception {
        TradingFacade game = game(dir()); TradingWebBridge bridge = new TradingWebBridge(game);
        assertFalse(call(bridge.submitOrder("{\"accountId\":\"ACC-NPC-MM-01\",\"instrumentId\":\"IDX:BBCA\",\"side\":\"BUY\",\"type\":\"MARKET\",\"quantity\":100,\"requestId\":\"STEAL\"}")).getBoolean("success"));
        String run = call(game.getSnapshotJson()).getString("runId");
        assertThrows(IllegalStateException.class, () -> game.resumeSavedMode("HISTORICAL_REPLAY"));
        assertEquals(run, call(game.getSnapshotJson()).getString("runId"));
    }

    @Test public void replayAndInternalModesKeepIndependentSavesAndLabs() throws Exception {
        File dir = dir(); TradingFacade game = game(dir); TradingWebBridge bridge = new TradingWebBridge(game);
        bridge.createStrategyLab("INTERNAL-LAB"); game.save();
        String internalRun = call(game.getSnapshotJson()).getString("runId");
        JSONObject started = game.startHistoricalReplay("US:AAPL", new BigDecimal("50000000"));
        assertTrue(started.getBoolean("success"));
        assertEquals(2016, game.getClock().getCurrentDate().getYear());
        assertEquals(0, call(bridge.strategyLab("{\"action\":\"list\"}")).getJSONArray("sandboxes").length());
        bridge.createStrategyLab("REPLAY-LAB"); game.save();
        String replayRun = call(game.getSnapshotJson()).getString("runId");
        assertEquals(1, new JSONArray(bridge.getInstruments("", "", "", false)).length());
        assertEquals(0, new JSONArray(bridge.getIndices()).length());
        assertFalse(call(bridge.researchQuery("{\"kind\":\"inbox\"}")).getBoolean("success"));
        assertTrue(game.resumeSavedMode("INTERNAL_MARKET").getBoolean("success"));
        assertEquals(internalRun, call(game.getSnapshotJson()).getString("runId"));
        assertEquals("INTERNAL-LAB", call(bridge.strategyLab("{\"action\":\"list\"}")).getJSONArray("sandboxes").getJSONObject(0).getString("sandboxId"));
        assertTrue(game.resumeSavedMode("HISTORICAL_REPLAY").getBoolean("success"));
        assertEquals(replayRun, call(game.getSnapshotJson()).getString("runId"));
        assertEquals("REPLAY-LAB", call(bridge.strategyLab("{\"action\":\"list\"}")).getJSONArray("sandboxes").getJSONObject(0).getString("sandboxId"));
    }

    @Test public void replayCompletionStopsClockAndSkipDoesNotInventFullDay() throws Exception {
        File dir = dir(); TradingFacade game = game(dir); game.startHistoricalReplay("US:AAPL", new BigDecimal("50000000"));
        java.time.LocalDate last = java.time.LocalDate.of(2016, 8, 3);
        long seconds = java.time.temporal.ChronoUnit.DAYS.between(game.getClock().getCurrentDate(), last) * 86400 + 6 * 3600 + 59 * 60;
        game.getClock().advanceGameSeconds(seconds); game.getReplayEngine().synchronizeToDate(last);
        game.getClock().play();
        game.advanceRealMs(1000, true);
        assertEquals(java.time.LocalTime.of(16, 0), game.getClock().getCurrentTime());
        assertTrue(call(game.getSnapshotJson()).getJSONObject("replay").getBoolean("completed"));
        JSONObject skipped = game.skipDay("AT-END"); assertFalse(skipped.getBoolean("success"));
        assertEquals("DATASET_COMPLETE", skipped.getString("status")); assertEquals(0, skipped.getLong("gameSeconds"));
        assertEquals(last, game.getClock().getCurrentDate());
    }
}
