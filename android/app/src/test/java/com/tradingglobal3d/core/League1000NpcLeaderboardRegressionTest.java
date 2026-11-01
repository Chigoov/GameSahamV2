package com.tradingglobal3d.core;

import com.tradingglobal3d.core.league.LeagueCompetitor;
import com.tradingglobal3d.core.league.LeagueManager;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Uji Roster 1.000 NPC Unik, Leaderboard, Avatar Anime, dan Migrasi Save (NPC01 - NPC04):
 */
public class League1000NpcLeaderboardRegressionTest {

    @Test
    public void testExactly1000UniqueNpcGeneratedWithValidAvatars() throws Exception {
        LeagueManager league = new LeagueManager();
        JSONObject lb = league.getLeaderboardByNetWorth(Money.of(100_000_000L, Currency.IDR), "Player 1", "Investor Mandiri");

        assertEquals(1001, lb.getInt("totalCompetitors")); // 1000 NPC + 1 Player
        JSONArray entries = lb.getJSONArray("leaderboard");
        assertEquals(1001, entries.length());

        Set<String> uniqueIds = new HashSet<>();
        Set<String> uniqueNames = new HashSet<>();

        for (int i = 0; i < entries.length(); i++) {
            JSONObject c = entries.getJSONObject(i);
            String id = c.getString("id");
            String name = c.getString("name");
            assertFalse("ID NPC harus unik: " + id, uniqueIds.contains(id));
            uniqueIds.add(id);

            if (!c.getBoolean("isPlayer")) {
                assertFalse("Nama NPC harus unik: " + name, uniqueNames.contains(name));
                uniqueNames.add(name);

                int avatarId = c.getInt("avatarId");
                assertTrue("Avatar ID harus antara 1 dan 100: " + avatarId, avatarId >= 1 && avatarId <= 100);
                String path = c.getString("avatarPath");
                assertTrue("Avatar path harus mengarah ke img/avatars/avatar_xxx.webp", path.startsWith("img/avatars/avatar_") && path.endsWith(".webp"));
            }
        }

        assertEquals(1001, uniqueIds.size());
        assertEquals(1000, uniqueNames.size());
    }

    @Test
    public void testLegacy20NpcSaveMigrationTo1000NpcSeamlessly() throws Exception {
        LeagueManager league = new LeagueManager();

        // Buat mock save lama yang hanya memiliki 20 peserta
        JSONObject legacyState = new JSONObject();
        legacyState.put("currentTier", "BRONZE");
        legacyState.put("seasonWeekNumber", 2);
        legacyState.put("currentRivalId", "NPC-RIV-01");
        legacyState.put("canonicalNpcAccounts", false);

        JSONArray legacyArr = new JSONArray();
        for (int i = 1; i <= 19; i++) {
            JSONObject npc = new JSONObject()
                    .put("id", String.format("NPC-%02d", i))
                    .put("name", "Taipan Lama " + i)
                    .put("title", "Konglomerat")
                    .put("style", "AGRESIF")
                    .put("netWorth", "10000000000")
                    .put("weeklyReturnPercent", 2.5)
                    .put("isPlayer", false);
            legacyArr.put(npc);
        }
        JSONObject rival = new JSONObject()
                .put("id", "NPC-RIV-01")
                .put("name", "Rival Lama")
                .put("title", "Rival")
                .put("style", "AGRESIF")
                .put("netWorth", "50000000")
                .put("weeklyReturnPercent", 1.5)
                .put("isPlayer", false);
        legacyArr.put(rival);
        legacyState.put("competitors", legacyArr);

        // Pulihkan state legacy ke LeagueManager
        league.restoreState(legacyState);

        // Verifikasi bahwa roster otomatis diperluas menjadi 1.000 NPC tanpa crash
        JSONObject lb = league.getLeaderboardByNetWorth(Money.of(50_000_000L, Currency.IDR), "Player", "Trader");
        assertEquals(1001, lb.getInt("totalCompetitors"));
    }
}
