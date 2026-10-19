package com.tradingglobal3d.core.clock;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Clock tunggal (Single Source of Time) untuk siklus game.
 * Mengontrol tanggal bursa, waktu sesi (WIB / UTC+7), percepatan, dan pause.
 */
public final class GameClock {
    private LocalDate currentDate;
    private LocalTime currentTime;
    private boolean isPaused;
    private int speedMultiplier; // 1, 2, 5

    public GameClock(LocalDate startDate, LocalTime startTime) {
        this.currentDate = Objects.requireNonNull(startDate);
        this.currentTime = Objects.requireNonNull(startTime);
        this.isPaused = true;
        this.speedMultiplier = 1;
    }

    public static GameClock createDefault() {
        return new GameClock(LocalDate.of(2026, 10, 19), LocalTime.of(8, 30, 0));
    }

    public LocalDate getCurrentDate() { return currentDate; }
    public LocalTime getCurrentTime() { return currentTime; }
    public boolean isPaused() { return isPaused; }
    public int getSpeedMultiplier() { return speedMultiplier; }

    public void play() { this.isPaused = false; }
    public void pause() { this.isPaused = true; }

    public void setSpeedMultiplier(int multiplier) {
        if (multiplier != 1 && multiplier != 2 && multiplier != 5) {
            throw new IllegalArgumentException("Kecepatan waktu yang didukung hanya 1x, 2x, atau 5x");
        }
        this.speedMultiplier = multiplier;
    }

    /**
     * Memajukan waktu game sebanyak seconds * speedMultiplier jika tidak di-pause.
     * Mengembalikan true jika terjadi pergantian hari bursa.
     */
    public synchronized boolean tick(int elapsedRealSeconds) {
        if (isPaused) return false;
        int advancedSeconds = elapsedRealSeconds * speedMultiplier;
        LocalTime nextTime = currentTime.plusSeconds(advancedSeconds);

        boolean dayChanged = false;
        if (nextTime.isBefore(currentTime)) {
            // Melewati tengah malam
            currentDate = currentDate.plusDays(1);
            dayChanged = true;
        }
        currentTime = nextTime;
        return dayChanged;
    }

    public MarketSession getCurrentMarketSession() {
        int hour = currentTime.getHour();
        int minute = currentTime.getMinute();
        int totalMinutes = hour * 60 + minute;

        // 08:45 - 08:59: Pre-opening
        if (totalMinutes >= 525 && totalMinutes < 540) {
            return MarketSession.PRE_OPENING;
        }
        // 09:00 - 11:30: Sesi 1
        if (totalMinutes >= 540 && totalMinutes <= 690) {
            return MarketSession.SESSION_1;
        }
        // 11:31 - 13:29: Break
        if (totalMinutes > 690 && totalMinutes < 810) {
            return MarketSession.BREAK;
        }
        // 13:30 - 15:49: Sesi 2
        if (totalMinutes >= 810 && totalMinutes < 950) {
            return MarketSession.SESSION_2;
        }
        // 15:50 - 16:00: Post-closing
        if (totalMinutes >= 950 && totalMinutes <= 960) {
            return MarketSession.POST_CLOSING;
        }

        return MarketSession.CLOSED;
    }

    public String getFormattedDate() {
        return currentDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    public String getFormattedTime() {
        return currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " WIB";
    }

    @Override
    public String toString() {
        return getFormattedDate() + " " + getFormattedTime() + " [" + getCurrentMarketSession().getLabel() + "] (Speed: " + speedMultiplier + "x, Paused: " + isPaused + ")";
    }
}
