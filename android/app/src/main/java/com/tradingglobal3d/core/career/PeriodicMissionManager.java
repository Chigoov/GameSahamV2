package com.tradingglobal3d.core.career;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

/**
 * Manajer Misi Periodik Harian dan Mingguan (D07).
 * - Rotasi deterministik berdasarkan kalender game Asia/Jakarta.
 * - Memisahkan misi aktif dengan arsip misi selesai/terlewat.
 */
public final class PeriodicMissionManager {

    private final Map<String, PeriodicMission> activeMissions = new LinkedHashMap<>();
    private final List<PeriodicMission> archivedMissions = new ArrayList<>();
    private String currentDayPeriodId = "";
    private String currentWeekPeriodId = "";

    public synchronized void reset() {
        activeMissions.clear();
        archivedMissions.clear();
        currentDayPeriodId = "";
        currentWeekPeriodId = "";
    }

    public synchronized Collection<PeriodicMission> getActiveMissions() {
        return Collections.unmodifiableCollection(activeMissions.values());
    }

    public synchronized List<PeriodicMission> getArchivedMissions() {
        return Collections.unmodifiableList(archivedMissions);
    }

    public synchronized PeriodicMission getMission(String id) {
        PeriodicMission m = activeMissions.get(id);
        if (m != null) return m;
        for (PeriodicMission a : archivedMissions) {
            if (a.getId().equals(id)) return a;
        }
        return null;
    }

    /**
     * Memperbarui siklus kalender game. Jika berganti hari atau minggu, rotasi misi baru diaktifkan.
     */
    public synchronized void updateCalendar(LocalDate currentDate) {
        if (currentDate == null) return;
        String dayId = currentDate.toString();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = currentDate.get(weekFields.weekOfYear());
        String weekId = currentDate.getYear() + "-W" + weekNumber;

        if (!dayId.equals(currentDayPeriodId)) {
            rotateDailyMissions(dayId);
            currentDayPeriodId = dayId;
        }

        if (!weekId.equals(currentWeekPeriodId)) {
            rotateWeeklyMissions(weekId);
            currentWeekPeriodId = weekId;
        }
    }

    private void rotateDailyMissions(String dayId) {
        // Arsipkan misi harian lama yang belum diklaim
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, PeriodicMission> e : activeMissions.entrySet()) {
            if (e.getValue().getPeriod() == PeriodicMission.MissionPeriod.DAILY) {
                toRemove.add(e.getKey());
                archivedMissions.add(e.getValue());
            }
        }
        for (String k : toRemove) activeMissions.remove(k);

        // Pilih 3 misi harian dari pool
        activeMissions.put("DM-READ-" + dayId, new PeriodicMission(
                "DM-READ-" + dayId, "Wawasan Pasar", "Baca catatan riset emiten terkini di Ruang Riset",
                PeriodicMission.MissionPeriod.DAILY, 1, 50, Money.of(250_000, Currency.IDR), dayId
        ));
        activeMissions.put("DM-TRADE-" + dayId, new PeriodicMission(
                "DM-TRADE-" + dayId, "Eksekusi Taktis", "Lakukan minimal 1 transaksi saham yang berhasil diproses",
                PeriodicMission.MissionPeriod.DAILY, 1, 75, Money.of(500_000, Currency.IDR), dayId
        ));
        activeMissions.put("DM-HEALTH-" + dayId, new PeriodicMission(
                "DM-HEALTH-" + dayId, "Manajemen Risiko", "Tinjau alokasi aset dan profil risiko portofolio",
                PeriodicMission.MissionPeriod.DAILY, 1, 40, Money.of(200_000, Currency.IDR), dayId
        ));
    }

    private void rotateWeeklyMissions(String weekId) {
        // Arsipkan misi mingguan lama
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, PeriodicMission> e : activeMissions.entrySet()) {
            if (e.getValue().getPeriod() == PeriodicMission.MissionPeriod.WEEKLY) {
                toRemove.add(e.getKey());
                archivedMissions.add(e.getValue());
            }
        }
        for (String k : toRemove) activeMissions.remove(k);

        // Pilih 3 misi mingguan dari pool
        activeMissions.put("WM-VOL-" + weekId, new PeriodicMission(
                "WM-VOL-" + weekId, "Volume Konsisten", "Eksekusi 5 transaksi pasar saham dalam minggu ini",
                PeriodicMission.MissionPeriod.WEEKLY, 5, 200, Money.of(2_500_000, Currency.IDR), weekId
        ));
        activeMissions.put("WM-DIV-" + weekId, new PeriodicMission(
                "WM-DIV-" + weekId, "Pertumbuhan Portofolio", "Raih pendapatan dividen, bagi hasil reksa dana, atau laba trading",
                PeriodicMission.MissionPeriod.WEEKLY, 1, 250, Money.of(3_000_000, Currency.IDR), weekId
        ));
        activeMissions.put("WM-DAYS-" + weekId, new PeriodicMission(
                "WM-DAYS-" + weekId, "Disiplin Finansial", "Tutup minimal 3 sesi hari bursa tanpa pelanggaran risiko",
                PeriodicMission.MissionPeriod.WEEKLY, 3, 300, Money.of(3_500_000, Currency.IDR), weekId
        ));
    }

    /**
     * Perekaman progres aktivitas pemain.
     */
    public synchronized void recordActivity(String activityType, int count) {
        for (PeriodicMission m : activeMissions.values()) {
            if (m.isCompleted()) continue;
            if ("TRADE".equals(activityType) && (m.getId().startsWith("DM-TRADE") || m.getId().startsWith("WM-VOL"))) {
                m.recordProgress(count);
            } else if ("READ_NEWS".equals(activityType) && m.getId().startsWith("DM-READ")) {
                m.recordProgress(count);
            } else if ("HEALTH_CHECK".equals(activityType) && m.getId().startsWith("DM-HEALTH")) {
                m.recordProgress(count);
            } else if ("DAY_SETTLED".equals(activityType) && (m.getId().startsWith("WM-DAYS") || m.getId().startsWith("WM-DIV"))) {
                m.recordProgress(count);
            }
        }
    }

    public synchronized JSONObject claimMission(String missionId, Account account, EconomyLedger ledger, long now) throws JSONException {
        PeriodicMission m = getMission(missionId);
        if (m == null) throw new IllegalArgumentException("Misi tidak ditemukan: " + missionId);
        return m.claim(account, ledger, now);
    }

    public synchronized JSONObject exportState() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("currentDayPeriodId", currentDayPeriodId);
        obj.put("currentWeekPeriodId", currentWeekPeriodId);

        JSONArray act = new JSONArray();
        for (PeriodicMission m : activeMissions.values()) {
            act.put(m.exportState());
        }
        obj.put("active", act);

        JSONArray arc = new JSONArray();
        for (PeriodicMission m : archivedMissions) {
            arc.put(m.exportState());
        }
        obj.put("archived", arc);
        return obj;
    }

    public synchronized void restoreState(JSONObject obj) throws JSONException {
        reset();
        if (obj == null) return;
        this.currentDayPeriodId = obj.optString("currentDayPeriodId", "");
        this.currentWeekPeriodId = obj.optString("currentWeekPeriodId", "");

        JSONArray act = obj.optJSONArray("active");
        if (act != null) {
            for (int i = 0; i < act.length(); i++) {
                PeriodicMission m = PeriodicMission.restoreFromState(act.getJSONObject(i));
                if (m != null) activeMissions.put(m.getId(), m);
            }
        }

        JSONArray arc = obj.optJSONArray("archived");
        if (arc != null) {
            for (int i = 0; i < arc.length(); i++) {
                PeriodicMission m = PeriodicMission.restoreFromState(arc.getJSONObject(i));
                if (m != null) archivedMissions.add(m);
            }
        }
    }
}
