package com.tradingglobal3d.core;

import com.tradingglobal3d.core.league.LeagueManager;
import com.tradingglobal3d.core.league.LeagueCompetitor;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class LeagueRosterRegressionTest {
    private static final Money PLAYER_WORTH = Money.of(45_000_000, Currency.IDR);

    @Test public void fictionalRosterPersistsAndResetRestoresDefaults() throws Exception {
        LeagueManager league = new LeagueManager();
        JSONObject initial = league.exportState();
        JSONArray roster = initial.getJSONArray("competitors");
        assertEquals(20, roster.length());
        Set<String> ids = new HashSet<>(), names = new HashSet<>(), styles = new HashSet<>();
        for (int i = 0; i < roster.length(); i++) {
            JSONObject npc = roster.getJSONObject(i);
            assertTrue(ids.add(npc.getString("id")));
            assertTrue(names.add(npc.getString("name")));
            styles.add(npc.getString("style"));
            assertEquals(i == 19 ? "NPC-RIV-01" : String.format(java.util.Locale.ROOT, "NPC-%02d", i + 1), npc.getString("id"));
        }
        assertEquals(3, styles.size());
        assertEquals("Arunika Bara", roster.getJSONObject(0).getString("name"));
        assertEquals("Wira Lentera", roster.getJSONObject(19).getString("name"));
        league.processWeeklyRollover(6.0, PLAYER_WORTH);
        JSONObject evolved = league.exportState();
        LeagueManager restored = new LeagueManager();
        restored.restoreState(new JSONObject(evolved.toString()));
        assertEquals(evolved.toString(), restored.exportState().toString());
        assertEquals(league.getRivalStatus(PLAYER_WORTH, 6.0).toString(), restored.getRivalStatus(PLAYER_WORTH, 6.0).toString());
        restored.reset();
        assertEquals(initial.toString(), restored.exportState().toString());
    }

    @Test public void legacyDefaultMigrationPreservesEconomyAndCustomIdentity() throws Exception {
        LeagueManager league = new LeagueManager();
        JSONObject state = league.exportState();
        state.put("seasonWeekNumber", 8).put("currentTier", "GOLD").put("currentRivalId", "NPC-01");
        JSONArray roster = state.getJSONArray("competitors");
        JSONObject legacy = roster.getJSONObject(0);
        legacy.put("name", "Prajogo Pangestu").put("netWorth", "123456789.25").put("weeklyReturnPercent", -7.125);
        roster.getJSONObject(1).put("name", "Nama Pilihan Pemain");
        // A legacy name on a different ID is custom data and must survive.
        roster.getJSONObject(2).put("name", "Prajogo Pangestu");
        league.restoreState(state);
        JSONObject migrated = league.exportState();
        JSONObject actual = migrated.getJSONArray("competitors").getJSONObject(0);
        // IDR is whole rupiah: compare against the existing Money restore contract.
        JSONObject expected = LeagueCompetitor.restoreFromState(legacy).exportState().put("name", "Arunika Bara");
        assertEquals("123456789", expected.getString("netWorth"));
        assertEquals(expected.toString(), actual.toString());
        assertEquals("Nama Pilihan Pemain", migrated.getJSONArray("competitors").getJSONObject(1).getString("name"));
        assertEquals("Prajogo Pangestu", migrated.getJSONArray("competitors").getJSONObject(2).getString("name"));
        assertEquals(8, migrated.getInt("seasonWeekNumber"));
        assertEquals("GOLD", migrated.getString("currentTier"));
        assertEquals("NPC-01", migrated.getString("currentRivalId"));
        league.restoreState(migrated);
        assertEquals(migrated.toString(), league.exportState().toString());
    }

    @Test public void equalMetricsUseStableIdsRegardlessOfSaveOrder() throws Exception {
        LeagueManager league = new LeagueManager();
        JSONObject state = league.exportState();
        JSONArray roster = state.getJSONArray("competitors"), reversed = new JSONArray();
        for (int i = roster.length() - 1; i >= 0; i--) {
            reversed.put(roster.getJSONObject(i).put("netWorth", "45000000").put("weeklyReturnPercent", 0.0));
        }
        league.restoreState(state.put("competitors", reversed));
        JSONArray wealth = league.getLeaderboardByNetWorth(PLAYER_WORTH, "Pemain", "Trader").getJSONArray("leaderboard");
        JSONArray returns = league.getLeaderboardByWeeklyReturn(0.0, PLAYER_WORTH, "Pemain", "Trader").getJSONArray("leaderboard");
        assertEquals(21, wealth.length());
        for (int i = 0; i < wealth.length(); i++) {
            String id = wealth.getJSONObject(i).getString("id");
            assertEquals(id, returns.getJSONObject(i).getString("id"));
            if (i > 0) assertTrue(wealth.getJSONObject(i - 1).getString("id").compareTo(id) < 0);
        }
    }
}
