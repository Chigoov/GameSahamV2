package com.tradingglobal3d.core.career;

import com.tradingglobal3d.core.model.Money;

import java.util.Objects;

/**
 * Misi perjalanan karier dengan pencapaian objektif dan imbalan idempoten.
 */
public final class CareerMission {
    private final String id;
    private final String title;
    private final String description;
    private final Money reward;

    private boolean isCompleted;
    private boolean isClaimed;

    public CareerMission(String id, String title, String description, Money reward) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.reward = Objects.requireNonNull(reward);
        this.isCompleted = false;
        this.isClaimed = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Money getReward() { return reward; }
    public boolean isCompleted() { return isCompleted; }
    public boolean isClaimed() { return isClaimed; }

    public void markCompleted() {
        this.isCompleted = true;
    }

    /**
     * Mengklaim reward. Hanya dapat berhasil SATU KALI.
     */
    public synchronized Money claim() {
        if (!isCompleted) {
            throw new IllegalStateException("Misi belum selesai diselesaikan");
        }
        if (isClaimed) {
            throw new IllegalStateException("Imbalan misi sudah pernah diklaim sebelumnya (Idempoten)");
        }
        this.isClaimed = true;
        return reward;
    }
}
