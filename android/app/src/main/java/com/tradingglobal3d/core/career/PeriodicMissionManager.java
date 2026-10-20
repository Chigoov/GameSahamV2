package com.tradingglobal3d.core.career;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

/** Calendar missions use game dates in WIB; weeks use ISO Monday boundaries. */
public final class PeriodicMissionManager {
    private final Map<String, PeriodicMission> activeMissions = new LinkedHashMap<>();
    private final List<PeriodicMission> archivedMissions = new ArrayList<>();
    private final Map<String, String> processedEvents = new LinkedHashMap<>();
    private String currentDayPeriodId = "";
    private String currentWeekPeriodId = "";
    private CareerRank careerRank = CareerRank.NOVICE_TRADER;
    // ponytail: six data rows per period share the existing mission model, without a quest engine.
    private static final String[] KEYS = {"READ", "TRADE", "HEALTH", "DAYS", "COMPANY", "FUND"};
    private static final String[] EVENTS = {"READ_NEWS", "TRADE", "HEALTH_CHECK", "DAY_SETTLED", "COMPANY_OPERATION", "FUND_SETTLED"};
    private static final String[] TITLES = {"Wawasan Pasar", "Eksekusi Terukur", "Tinjauan Risiko", "Laporan Sesi", "Operasi Perusahaan", "Pengelolaan Unit"};
    private static final String[] DESCRIPTIONS = {"Baca riset yang sudah diterbitkan", "Selesaikan fill transaksi pasar", "Tinjau kesehatan portofolio", "Tutup sesi dan terbitkan laporan", "Selesaikan operasi harian perusahaan", "Selesaikan dealing unit reksa dana"};

    public synchronized void reset() {
        activeMissions.clear(); archivedMissions.clear(); processedEvents.clear();
        currentDayPeriodId = ""; currentWeekPeriodId = ""; careerRank = CareerRank.NOVICE_TRADER;
    }
    public synchronized Collection<PeriodicMission> getActiveMissions() { return Collections.unmodifiableCollection(activeMissions.values()); }
    public synchronized List<PeriodicMission> getArchivedMissions() { return Collections.unmodifiableList(archivedMissions); }
    public synchronized PeriodicMission getMission(String id) {
        PeriodicMission mission = activeMissions.get(id);
        if (mission != null) return mission;
        for (PeriodicMission archived : archivedMissions) if (archived.getId().equals(id)) return archived;
        return null;
    }
    public synchronized void updateCalendar(LocalDate date) { updateCalendar(date, careerRank); }
    public synchronized void updateCalendar(LocalDate date, CareerRank rank) {
        if (date == null) return;
        if (!currentDayPeriodId.isEmpty() && date.isBefore(LocalDate.parse(currentDayPeriodId))) return;
        careerRank = Objects.requireNonNull(rank);
        String day = date.toString();
        String week = String.format(Locale.ROOT, "%04d-W%02d", date.get(WeekFields.ISO.weekBasedYear()), date.get(WeekFields.ISO.weekOfWeekBasedYear()));
        if (!day.equals(currentDayPeriodId)) {
            rotate(PeriodicMission.MissionPeriod.DAILY, day);
            currentDayPeriodId = day;
            processedEvents.clear();
        }
        if (!week.equals(currentWeekPeriodId)) {
            rotate(PeriodicMission.MissionPeriod.WEEKLY, week);
            currentWeekPeriodId = week;
        }
    }
    private void rotate(PeriodicMission.MissionPeriod period, String periodId) {
        Iterator<PeriodicMission> old = activeMissions.values().iterator();
        while (old.hasNext()) {
            PeriodicMission mission = old.next();
            if (mission.getPeriod() == period) { archivedMissions.add(mission); old.remove(); }
        }
        List<Integer> eligible = new ArrayList<>();
        for (int index = 0; index < KEYS.length; index++) {
            if (index == 4 && careerRank.getLevel() < CareerRank.PORTFOLIO_MANAGER.getLevel()) continue;
            if (index == 5 && careerRank.getLevel() < CareerRank.SENIOR_ANALYST.getLevel()) continue;
            eligible.add(index);
        }
        Collections.shuffle(eligible, new Random(31L * periodId.hashCode() + period.ordinal()));
        for (int index : eligible.subList(0, 3)) {
            boolean daily = period == PeriodicMission.MissionPeriod.DAILY;
            String id = (daily ? "DM-" : "WM-") + KEYS[index] + "-" + periodId;
            int target = daily ? 1 : index == 1 ? 5 : 3;
            activeMissions.put(id, new PeriodicMission(id, TITLES[index], DESCRIPTIONS[index] + " (" + target + ")",
                    period, target, daily ? 50 : 200, Money.of(daily ? 200_000 : 1_000_000, Currency.IDR), periodId, EVENTS[index]));
        }
    }
    /** Legacy trusted synchronous caller; use the event-ID overload for replayable domain events. */
    public synchronized void recordActivity(String type, int count) {
        if (count <= 0) throw new IllegalArgumentException("Progres aktivitas harus positif");
        for (PeriodicMission mission : activeMissions.values())
            if (mission.getActivityType().equals(type)) mission.recordProgress(count);
    }
    /** Date is the actual event date, not callback delivery date; late events cannot earn in a new period. */
    public synchronized boolean recordActivity(String eventId, String type, int count, LocalDate eventDate) {
        if (eventId == null || eventId.trim().isEmpty() || eventId.length() > 200 || type == null || type.trim().isEmpty() || count <= 0)
            throw new IllegalArgumentException("Identitas/jenis/progres aktivitas tidak valid");
        if (eventDate == null || !eventDate.toString().equals(currentDayPeriodId)) return false;
        String payload = new JSONArray().put(type).put(count).put(eventDate.toString()).toString();
        String existing = processedEvents.get(eventId);
        if (existing != null) {
            if (!existing.equals(payload)) throw new IllegalArgumentException("Event dipakai untuk aktivitas berbeda");
            return false;
        }
        recordActivity(type, count);
        processedEvents.put(eventId, payload);
        return true;
    }
    public synchronized JSONObject claimMission(String id, Account account, EconomyLedger ledger, long now) throws JSONException {
        PeriodicMission mission = getMission(id);
        if (mission == null) throw new IllegalArgumentException("Misi tidak ditemukan: " + id);
        return mission.claim(account, ledger, now);
    }
    public synchronized JSONObject exportState() throws JSONException {
        JSONObject result = new JSONObject().put("currentDayPeriodId", currentDayPeriodId).put("currentWeekPeriodId", currentWeekPeriodId)
                .put("careerRank", careerRank.name()).put("processedEvents", new JSONObject(processedEvents));
        JSONArray active = new JSONArray(), archived = new JSONArray();
        for (PeriodicMission mission : activeMissions.values()) active.put(mission.exportState());
        for (PeriodicMission mission : archivedMissions) archived.put(mission.exportState());
        return result.put("active", active).put("archived", archived);
    }

    private static String isoWeek(LocalDate date) {
        return String.format(Locale.ROOT, "%04d-W%02d", date.get(WeekFields.ISO.weekBasedYear()), date.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }

    private static String validatedWeek(String period) {
        if (!period.matches("[0-9]{4}-W[0-9]{1,2}")) throw new IllegalArgumentException("Periode minggu tidak valid");
        int year = Integer.parseInt(period.substring(0, 4));
        int week = Integer.parseInt(period.substring(6));
        LocalDate monday = LocalDate.of(year, 1, 4).with(java.time.DayOfWeek.MONDAY).plusWeeks(week - 1L);
        String normalized = String.format(Locale.ROOT, "%04d-W%02d", year, week);
        if (week < 1 || !isoWeek(monday).equals(normalized)) throw new IllegalArgumentException("Minggu ISO tidak valid");
        return normalized;
    }

    private static void validateDefinition(PeriodicMission mission) {
        boolean daily = mission.getPeriod() == PeriodicMission.MissionPeriod.DAILY;
        String period = mission.getPeriodId();
        if (daily) LocalDate.parse(period); else validatedWeek(period);
        String prefix = daily ? "DM-" : "WM-";
        for (int index = 0; index < KEYS.length; index++) {
            if (!mission.getId().equals(prefix + KEYS[index] + "-" + period)) continue;
            int target = daily ? 1 : index == 1 ? 5 : 3;
            boolean canonical = mission.getActivityType().equals(EVENTS[index]) && mission.getTargetCount() == target
                    && mission.getXpReward() == (daily ? 50 : 200)
                    && mission.getCashReward().equals(Money.of(daily ? 200_000 : 1_000_000, Currency.IDR));
            if (canonical) return;
        }
        // ponytail: accept only the six exact legacy contracts; unknown old rules require explicit recovery.
        String[] legacyIds = {"DM-READ-", "DM-TRADE-", "DM-HEALTH-", "WM-VOL-", "WM-DIV-", "WM-DAYS-"};
        String[] legacyTypes = {"READ_NEWS", "TRADE", "HEALTH_CHECK", "TRADE", "INCOME_REALIZED", "DAY_SETTLED"};
        int[] legacyTargets = {1, 1, 1, 5, 1, 3}, legacyXp = {50, 75, 40, 200, 250, 300};
        long[] legacyCash = {250_000, 500_000, 200_000, 2_500_000, 3_000_000, 3_500_000};
        for (int index = 0; index < legacyIds.length; index++)
            if (mission.getId().equals(legacyIds[index] + period) && daily == (index < 3)
                    && mission.getActivityType().equals(legacyTypes[index]) && mission.getTargetCount() == legacyTargets[index]
                    && mission.getXpReward() == legacyXp[index]
                    && mission.getCashReward().equals(Money.of(legacyCash[index], Currency.IDR))) return;
        throw new IllegalArgumentException("Definisi/reward misi tidak dikenal");
    }
    public synchronized void restoreState(JSONObject state) throws JSONException {
        PeriodicMissionManager restored = new PeriodicMissionManager();
        if (state != null) {
            restored.currentDayPeriodId = state.optString("currentDayPeriodId", "");
            restored.currentWeekPeriodId = state.optString("currentWeekPeriodId", "");
            if (!restored.currentDayPeriodId.isEmpty()) LocalDate.parse(restored.currentDayPeriodId);
            if (restored.currentDayPeriodId.isEmpty() != restored.currentWeekPeriodId.isEmpty())
                throw new IllegalArgumentException("Kalender misi tidak lengkap");
            if (!restored.currentDayPeriodId.isEmpty() && !validatedWeek(restored.currentWeekPeriodId)
                    .equals(isoWeek(LocalDate.parse(restored.currentDayPeriodId))))
                throw new IllegalArgumentException("Minggu misi tidak cocok dengan tanggal game");
            restored.careerRank = CareerRank.valueOf(state.optString("careerRank", "NOVICE_TRADER"));
            Set<String> ids = new HashSet<>();
            for (String group : new String[]{"active", "archived"}) {
                JSONArray array = state.optJSONArray(group);
                for (int i = 0; array != null && i < array.length(); i++) {
                    PeriodicMission mission = PeriodicMission.restoreFromState(array.getJSONObject(i));
                    validateDefinition(mission);
                    if (!ids.add(mission.getId())) throw new IllegalArgumentException("ID misi duplikat");
                    if (group.equals("active")) {
                        String expected = mission.getPeriod() == PeriodicMission.MissionPeriod.DAILY ? restored.currentDayPeriodId : restored.currentWeekPeriodId;
                        if (!expected.equals(mission.getPeriodId())) throw new IllegalArgumentException("Periode aktif tidak sesuai misi");
                        restored.activeMissions.put(mission.getId(), mission);
                    } else restored.archivedMissions.add(mission);
                }
            }
            JSONObject events = state.optJSONObject("processedEvents");
            if (events != null) {
                Iterator<String> keys = events.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String payload = events.getString(key);
                    JSONArray parsed = new JSONArray(payload);
                    if (key.trim().isEmpty() || key.length() > 200 || parsed.length() != 3 || parsed.getString(0).trim().isEmpty()
                            || parsed.getInt(1) <= 0 || !parsed.getString(2).equals(restored.currentDayPeriodId))
                        throw new IllegalArgumentException("Event misi tidak valid");
                    restored.processedEvents.put(key, payload);
                }
            }
        }
        activeMissions.clear(); activeMissions.putAll(restored.activeMissions);
        archivedMissions.clear(); archivedMissions.addAll(restored.archivedMissions);
        processedEvents.clear(); processedEvents.putAll(restored.processedEvents);
        currentDayPeriodId = restored.currentDayPeriodId; currentWeekPeriodId = restored.currentWeekPeriodId; careerRank = restored.careerRank;
    }
}
