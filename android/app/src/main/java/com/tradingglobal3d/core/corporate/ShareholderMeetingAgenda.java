package com.tradingglobal3d.core.corporate;

import java.time.LocalDate;
import java.util.*;

/**
 * Agenda Rapat Umum Pemegang Saham (RUPS) untuk Voting.
 * Hak suara dihitung dari kepemilikan saham pada recording date (1 lembar = 1 suara).
 * Satu akun hanya dapat memberikan suara satu kali (Idempoten).
 */
public final class ShareholderMeetingAgenda {
    public enum VoteChoice {
        FOR("Setuju"),
        AGAINST("Tolak"),
        ABSTAIN("Abstain");

        private final String label;
        VoteChoice(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final String id;
    private final String ticker;
    private final String title;
    private final String resolutionText;
    private final LocalDate recordingDate;

    private long totalVotesFor = 0;
    private long totalVotesAgainst = 0;
    private long totalVotesAbstain = 0;
    private final Set<String> votedAccountIds = new HashSet<>();

    public ShareholderMeetingAgenda(String id, String ticker, String title,
                                    String resolutionText, LocalDate recordingDate) {
        this.id = Objects.requireNonNull(id);
        this.ticker = Objects.requireNonNull(ticker);
        this.title = Objects.requireNonNull(title);
        this.resolutionText = Objects.requireNonNull(resolutionText);
        this.recordingDate = Objects.requireNonNull(recordingDate);
    }

    public String getId() { return id; }
    public String getTicker() { return ticker; }
    public String getTitle() { return title; }
    public String getResolutionText() { return resolutionText; }
    public LocalDate getRecordingDate() { return recordingDate; }

    public long getTotalVotesFor() { return totalVotesFor; }
    public long getTotalVotesAgainst() { return totalVotesAgainst; }
    public long getTotalVotesAbstain() { return totalVotesAbstain; }

    public synchronized boolean hasVoted(String accountId) {
        return votedAccountIds.contains(accountId);
    }

    /**
     * Menyampaikan hak suara pemegang saham.
     */
    public synchronized void castVote(String accountId, VoteChoice choice, long votingShares) {
        if (votedAccountIds.contains(accountId)) {
            throw new IllegalStateException("Akun ini sudah pernah memberikan suara pada agenda ini (Idempoten)");
        }
        if (votingShares <= 0) {
            throw new IllegalArgumentException("Akun tidak memiliki hak suara (0 lembar saham pada recording date)");
        }

        switch (choice) {
            case FOR:
                totalVotesFor += votingShares;
                break;
            case AGAINST:
                totalVotesAgainst += votingShares;
                break;
            case ABSTAIN:
                totalVotesAbstain += votingShares;
                break;
        }
        votedAccountIds.add(accountId);
    }
}
