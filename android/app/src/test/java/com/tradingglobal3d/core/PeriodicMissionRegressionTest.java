package com.tradingglobal3d.core;

import com.tradingglobal3d.core.career.*;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.player.PlayerProgressManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.time.LocalDate;
import java.util.*;
import static org.junit.Assert.*;

public class PeriodicMissionRegressionTest {
    @Test public void mondayIsoWeekAndYearAreIndependentOfLocale() throws Exception {
        Locale original = Locale.getDefault();
        try {
            String expected = null;
            for (Locale locale : new Locale[]{Locale.US, Locale.FRANCE, Locale.forLanguageTag("id-ID")}) {
                Locale.setDefault(locale);
                PeriodicMissionManager manager = new PeriodicMissionManager();
                manager.updateCalendar(LocalDate.of(2020, 12, 31));
                assertEquals("2020-W53", manager.exportState().getString("currentWeekPeriodId"));
                manager.updateCalendar(LocalDate.of(2021, 1, 3));
                assertEquals("2020-W53", manager.exportState().getString("currentWeekPeriodId"));
                manager.updateCalendar(LocalDate.of(2021, 1, 4));
                assertEquals("2021-W01", manager.exportState().getString("currentWeekPeriodId"));
                String state = manager.exportState().toString();
                if (expected == null) expected = state; else assertEquals(expected, state);
                manager.updateCalendar(LocalDate.of(2020, 1, 1));
                assertEquals(state, manager.exportState().toString());
            }
        } finally { Locale.setDefault(original); }
    }

    @Test public void sixTemplatesRotateWithEligibilityAndIncomeIsNeverDaySettlement() throws Exception {
        Set<String> daily = new HashSet<>(), weekly = new HashSet<>();
        PeriodicMissionManager manager = new PeriodicMissionManager();
        for (int day = 0; day < 100; day++) {
            manager.updateCalendar(LocalDate.of(2026, 1, 1).plusDays(day), CareerRank.PORTFOLIO_MANAGER);
            int dailyCount = 0, weeklyCount = 0;
            for (PeriodicMission mission : manager.getActiveMissions()) {
                if (mission.getPeriod() == PeriodicMission.MissionPeriod.DAILY) { daily.add(mission.getActivityType()); dailyCount++; }
                else { weekly.add(mission.getActivityType()); weeklyCount++; }
            }
            assertEquals(3, dailyCount); assertEquals(3, weeklyCount);
        }
        assertEquals(6, daily.size()); assertEquals(6, weekly.size());
        PeriodicMissionManager novice = new PeriodicMissionManager();
        for (int day = 0; day < 30; day++) {
            novice.updateCalendar(LocalDate.of(2026, 1, 1).plusDays(day), CareerRank.NOVICE_TRADER);
            for (PeriodicMission mission : novice.getActiveMissions()) {
                assertNotEquals("FUND_SETTLED", mission.getActivityType());
                assertNotEquals("COMPANY_OPERATION", mission.getActivityType());
            }
        }
        String legacyWeek = novice.exportState().getString("currentWeekPeriodId");
        PeriodicMission income = new PeriodicMission("WM-DIV-" + legacyWeek, "Pendapatan", "Pendapatan nyata",
                PeriodicMission.MissionPeriod.WEEKLY, 1, 250, Money.of(3_000_000, Currency.IDR), legacyWeek);
        JSONObject legacy = income.exportState(); legacy.remove("activityType");
        assertEquals("INCOME_REALIZED", PeriodicMission.restoreFromState(legacy).getActivityType());
        JSONObject state = novice.exportState();
        legacy.put("periodId", state.getString("currentWeekPeriodId"));
        state.getJSONArray("active").put(legacy);
        novice.restoreState(state);
        novice.recordActivity("DAY_SETTLED", 1);
        assertFalse(novice.getMission(income.getId()).isCompleted());
        novice.recordActivity("INCOME_REALIZED", 1);
        assertTrue(novice.getMission(income.getId()).isCompleted());
    }

    @Test public void eventReplayArchiveClaimsAndInvalidRestorePreserveMoney() throws Exception {
        PeriodicMissionManager manager = new PeriodicMissionManager();
        LocalDate date = LocalDate.of(2026, 10, 20);
        manager.updateCalendar(date);
        PeriodicMission mission = manager.getActiveMissions().stream().filter(m -> m.getPeriod() == PeriodicMission.MissionPeriod.DAILY).findFirst().get();
        assertTrue(manager.recordActivity("EVENT", mission.getActivityType(), 1, date));
        String completed = manager.exportState().toString();
        manager.restoreState(new JSONObject(completed));
        assertFalse(manager.recordActivity("EVENT", mission.getActivityType(), 1, date));
        try { manager.recordActivity("EVENT", mission.getActivityType(), 2, date); fail(); }
        catch (IllegalArgumentException expected) { }
        assertEquals(completed, manager.exportState().toString());
        manager.updateCalendar(date.plusDays(1));
        String rolled = manager.exportState().toString();
        assertFalse(manager.recordActivity("LATE", mission.getActivityType(), 1, date));
        assertEquals(rolled, manager.exportState().toString());
        Account account = new Account("PLAYER", "QA", AccountType.PERSONAL, null);
        EconomyLedger ledger = new EconomyLedger();
        try { manager.claimMission(mission.getId(), account, null, 1); fail(); }
        catch (NullPointerException expected) { }
        assertFalse(manager.getMission(mission.getId()).isClaimed());
        JSONObject receipt = manager.claimMission(mission.getId(), account, ledger, 1);
        String expectedReceipt = receipt.toString();
        receipt.put("xpReward", -1);
        assertEquals(mission.getCashReward(), account.getTotalCash(Currency.IDR));
        manager.restoreState(new JSONObject(manager.exportState().toString()));
        assertEquals(expectedReceipt, manager.claimMission(mission.getId(), account, ledger, 2).toString());
        assertEquals(mission.getCashReward(), account.getTotalCash(Currency.IDR));
        assertEquals(1, ledger.getEntries().size()); assertTrue(ledger.reconcile(account, Currency.IDR));
        String before = manager.exportState().toString();
        JSONObject corrupt = new JSONObject(before);
        corrupt.getJSONArray("active").getJSONObject(0).put("currentCount", -1);
        try { manager.restoreState(corrupt); fail(); }
        catch (IllegalArgumentException expected) { }
        assertEquals(before, manager.exportState().toString());
    }

    @Test public void progressRejectsNegativeAndCannotOverflow() {
        PeriodicMission mission = new PeriodicMission("TEST", "QA", "QA", PeriodicMission.MissionPeriod.DAILY,
                Integer.MAX_VALUE, 0, Money.zero(Currency.IDR), "2026-10-20", "TRADE");
        mission.recordProgress(1); mission.recordProgress(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, mission.getCurrentCount()); assertTrue(mission.isCompleted());
        try { mission.recordProgress(-1); fail(); } catch (IllegalArgumentException expected) { }
    }

    @Test public void missionXpIsActualIdempotentAndPersistedWithBadgeProgress() throws Exception {
        PlayerProgressManager progress = new PlayerProgressManager();
        assertTrue(progress.awardMissionXp("DM-READ-2026-10-20", 50, 1));
        int earned = progress.getCurrentXp();
        assertTrue(earned >= 50);
        assertFalse(progress.awardMissionXp("DM-READ-2026-10-20", 50, 2));
        assertEquals(earned, progress.getCurrentXp());
        PlayerProgressManager restored = new PlayerProgressManager();
        restored.restoreState(new JSONObject(progress.exportState().toString()));
        assertFalse(restored.awardMissionXp("DM-READ-2026-10-20", 50, 3));
        assertEquals(earned, restored.getCurrentXp());
        try { restored.awardMissionXp("DM-READ-2026-10-20", 200, 3); fail(); }
        catch (IllegalArgumentException expected) { }
        assertTrue(restored.awardMissionXp("DM-TRADE-2026-10-21", 50, 4));
        assertEquals(earned + 50, restored.getCurrentXp());
        restored.restoreState(new JSONObject(restored.exportState().toString()));
        assertEquals(earned + 50, restored.getCurrentXp());
    }

    @Test public void restoredTemplatesAndIsoCalendarCannotChangeRewards() throws Exception {
        PeriodicMissionManager manager = new PeriodicMissionManager();
        manager.updateCalendar(LocalDate.of(2026, 10, 20));
        String before = manager.exportState().toString();
        for (String field : new String[]{"cashReward", "xpReward", "targetCount", "activityType", "id"}) {
            JSONObject corrupt = new JSONObject(before);
            JSONObject mission = corrupt.getJSONArray("active").getJSONObject(0);
            if (field.equals("activityType") || field.equals("id")) mission.put(field, "UNKNOWN");
            else mission.put(field, 999999);
            try { manager.restoreState(corrupt); fail("Accepted corrupt " + field); }
            catch (IllegalArgumentException expected) { }
            assertEquals(before, manager.exportState().toString());
        }
        for (String week : new String[]{"2026-W54", "2025-W43", "bad"}) {
            JSONObject corrupt = new JSONObject(before).put("currentWeekPeriodId", week);
            try { manager.restoreState(corrupt); fail("Accepted corrupt week"); }
            catch (IllegalArgumentException expected) { }
            assertEquals(before, manager.exportState().toString());
        }
    }

    @Test public void tenHundredActionSequencesKeepPeriodClaimAndLedgerInvariants() throws Exception {
        for (int seed = 0; seed < 10; seed++) {
            PeriodicMissionManager manager = new PeriodicMissionManager();
            CareerRank rank = CareerRank.values()[seed % 4];
            LocalDate date = LocalDate.of(2026, 12, 20).plusDays(seed);
            manager.updateCalendar(date, rank);
            Account account = new Account("PLAYER", "QA", AccountType.PERSONAL, null);
            EconomyLedger ledger = new EconomyLedger(); Money expected = Money.zero(Currency.IDR);
            PlayerProgressManager progress = new PlayerProgressManager();
            Random random = new Random(seed);
            for (int action = 0; action < 100; action++) {
                List<PeriodicMission> unfinished = new ArrayList<>();
                for (PeriodicMission mission : manager.getActiveMissions())
                    if (mission.getPeriod() == PeriodicMission.MissionPeriod.DAILY && !mission.isCompleted()) unfinished.add(mission);
                if (unfinished.isEmpty()) {
                    date = date.plusDays(1); manager.updateCalendar(date, rank);
                    for (PeriodicMission mission : manager.getActiveMissions())
                        if (mission.getPeriod() == PeriodicMission.MissionPeriod.DAILY) unfinished.add(mission);
                }
                PeriodicMission chosen = unfinished.get(random.nextInt(unfinished.size()));
                String event = seed + ":" + action;
                assertTrue(manager.recordActivity(event, chosen.getActivityType(), 1, date));
                assertTrue(chosen.isCompleted());
                assertFalse(manager.recordActivity(event, chosen.getActivityType(), 1, date));
                for (PeriodicMission mission : manager.getActiveMissions()) {
                    if (mission.isCompleted() && !mission.isClaimed()) {
                        manager.claimMission(mission.getId(), account, ledger, action);
                        int previousXp = progress.getCurrentXp();
                        boolean badgeUnlocked = progress.getAchievement("periodic_master").isUnlocked();
                        assertTrue(progress.awardMissionXp(mission.getId(), mission.getXpReward(), action));
                        int badgeReward = !badgeUnlocked && progress.getAchievement("periodic_master").isUnlocked() ? 200 : 0;
                        assertEquals(previousXp + mission.getXpReward() + badgeReward, progress.getCurrentXp());
                        assertFalse(progress.awardMissionXp(mission.getId(), mission.getXpReward(), action));
                        expected = expected.add(mission.getCashReward());
                    }
                    assertTrue(mission.getCurrentCount() >= 0 && mission.getCurrentCount() <= mission.getTargetCount());
                    assertEquals(mission.getCurrentCount() == mission.getTargetCount(), mission.isCompleted());
                }
                if (action % 7 == 0) {
                    manager.restoreState(new JSONObject(manager.exportState().toString()));
                    progress.restoreState(new JSONObject(progress.exportState().toString()));
                }
                assertEquals(expected, account.getTotalCash(Currency.IDR));
                assertTrue("seed=" + seed + " action=" + action, ledger.reconcile(account, Currency.IDR));
            }
        }
    }
}
