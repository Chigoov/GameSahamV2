package com.tradingglobal3d.core.clock;

import com.tradingglobal3d.core.market.MarketExchange;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Clock tunggal (Single Source of Time) untuk siklus game.
 * Mengontrol tanggal bursa, waktu sesi (WIB / UTC+7), percepatan, dan pause.
 */
public final class GameClock {
    public static final long FOREGROUND_GAME_SECONDS_PER_REAL_SECOND = 96L;
    public static final long INACTIVE_GAME_SECONDS_PER_REAL_SECOND = 24L;
    public static final long SECONDS_PER_GAME_DAY = 86400L;
    public static final long MS_PER_GAME_DAY = 86400000L;

    private LocalDate currentDate;
    private LocalTime currentTime;
    private boolean isPaused;
    private int speedMultiplier; // 1, 2, 5
    private int gameMsRemainder; // 0 - 999 ms game tersisa
    private boolean isForeground = true;

    public GameClock(LocalDate startDate, LocalTime startTime) {
        this.currentDate = Objects.requireNonNull(startDate);
        this.currentTime = Objects.requireNonNull(startTime);
        this.isPaused = true;
        this.speedMultiplier = 1;
        this.gameMsRemainder = 0;
        this.isForeground = true;
    }

    public static GameClock createDefault() {
        return new GameClock(LocalDate.of(2026, 10, 19), LocalTime.of(8, 30, 0));
    }

    public LocalDate getCurrentDate() { return currentDate; }
    public LocalTime getCurrentTime() { return currentTime; }
    public boolean isPaused() { return isPaused; }
    public int getSpeedMultiplier() { return speedMultiplier; }
    public int getGameMsRemainder() { return gameMsRemainder; }
    public boolean isForeground() { return isForeground; }

    public void setGameMsRemainder(int remainder) {
        if (remainder < 0 || remainder >= 1000) throw new IllegalArgumentException("Remainder harus 0..999");
        this.gameMsRemainder = remainder;
    }

    public void setForeground(boolean foreground) { this.isForeground = foreground; }

    public void play() { this.isPaused = false; }
    public void pause() { this.isPaused = true; }

    public void setSpeedMultiplier(int multiplier) {
        if (multiplier != 1 && multiplier != 2 && multiplier != 5) {
            throw new IllegalArgumentException("Kecepatan waktu yang didukung hanya 1x, 2x, atau 5x");
        }
        this.speedMultiplier = multiplier;
    }

    /**
     * Konversi eksak real millisecond ke game millisecond sesuai kontrak TIME-ACCEPTANCE.
     * Foreground 1x: 96 game ms / real ms (900 detik nyata = 1 hari game / 86400 detik game).
     * Foreground 2x: 192 game ms / real ms.
     * Foreground 5x: 480 game ms / real ms.
     * Inactive (background / ditutup): 24 game ms / real ms (3600 detik nyata = 1 hari game, mengabaikan speedMultiplier).
     */
    public static long convertRealMsToGameMs(long realMs, boolean foreground, int speedMultiplier) {
        if (realMs < 0) throw new IllegalArgumentException("Waktu tidak boleh negatif");
        long rate = foreground ? (FOREGROUND_GAME_SECONDS_PER_REAL_SECOND * (long) speedMultiplier)
                               : INACTIVE_GAME_SECONDS_PER_REAL_SECOND;
        return Math.multiplyExact(realMs, rate);
    }

    /**
     * Akumulasi real milliseconds dengan preservasi pecahan game ms (remainder).
     * Mengembalikan jumlah detik game penuh yang siap dimajukan.
     */
    public synchronized long advanceRealMs(long realMs, boolean foreground) {
        if (realMs < 0) throw new IllegalArgumentException("Waktu tidak boleh negatif");
        if (foreground && isPaused) return 0;
        long gameMs = convertRealMsToGameMs(realMs, foreground, speedMultiplier);
        long totalGameMs = Math.addExact(gameMs, (long) gameMsRemainder);
        long gameSeconds = totalGameMs / 1000L;
        this.gameMsRemainder = (int) (totalGameMs % 1000L);
        return gameSeconds;
    }

    /**
     * Memajukan waktu game sebanyak seconds * speedMultiplier jika tidak di-pause.
     * Mengembalikan true jika terjadi pergantian hari bursa.
     */
    public synchronized boolean tick(int elapsedGameSeconds) {
        if (elapsedGameSeconds < 0) throw new IllegalArgumentException("Waktu tidak boleh mundur");
        if (isPaused) return false;
        return advanceGameSeconds(Math.multiplyExact((long) elapsedGameSeconds, speedMultiplier));
    }

    /** Low-level simulation step; real elapsed time is converted once by TradingFacade. */
    public synchronized boolean advanceGameSeconds(long seconds) {
        if (seconds < 0) throw new IllegalArgumentException("Waktu tidak boleh mundur");
        java.time.LocalDateTime next = currentDate.atTime(currentTime).plusSeconds(seconds);
        boolean dayChanged = !next.toLocalDate().equals(currentDate);
        currentDate = next.toLocalDate();
        currentTime = next.toLocalTime();
        return dayChanged;
    }

    public long getGameTimestamp() {
        return currentDate.atTime(currentTime).toInstant(java.time.ZoneOffset.ofHours(7)).toEpochMilli();
    }

    public MarketSession getCurrentMarketSession() {
        return getMarketSession(MarketExchange.IDX);
    }

    public MarketSession getMarketSession(MarketExchange exchange) {
        int hour = currentTime.getHour();
        int minute = currentTime.getMinute();
        int totalMinutes = hour * 60 + minute;

        if (exchange == MarketExchange.US) {
            // US Markets (NYSE/NASDAQ) pada waktu simulasi game WIB
            // Sesi reguler 21:30 - 04:00 WIB (09:30 - 16:00 New York Time)
            // 21:00 - 21:29: Pre-opening
            if (totalMinutes >= 1260 && totalMinutes < 1290) {
                return MarketSession.PRE_OPENING;
            }
            // 21:30 - 23:59 dan 00:00 - 03:59: Sesi Perdagangan US Aktif
            if (totalMinutes >= 1290 || totalMinutes < 240) {
                return MarketSession.SESSION_1;
            }
            // 04:00 - 04:30: Post-closing
            if (totalMinutes >= 240 && totalMinutes <= 270) {
                return MarketSession.POST_CLOSING;
            }
            return MarketSession.CLOSED;
        }

        // Default IDX (WIB)
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

    public boolean isTradingOpenFor(MarketExchange exchange) {
        return getMarketSession(exchange).isOpenForTrading();
    }

    public boolean isAnyMarketOpen() {
        return isTradingOpenFor(MarketExchange.IDX) || isTradingOpenFor(MarketExchange.US);
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
