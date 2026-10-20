package com.tradingglobal3d.core.corporate;

import java.time.LocalDate;
import java.util.*;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Position;

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
    private final Map<String, Long> entitlementShares = new LinkedHashMap<>();
    private boolean recorded;
    private boolean closed;

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
    public LocalDate getDeadline() { return recordingDate.plusDays(3); }
    public boolean isClosed() { return closed; }
    public String getResult() { return !closed ? "OPEN" : totalVotesFor > totalVotesAgainst ? "APPROVED" : "REJECTED"; }
    public String getConsequence() { return !closed ? "Menunggu tenggat" : "APPROVED".equals(getResult()) ? "Resolusi disetujui; aksi terkait mengikuti jadwal eksekusi" : "Resolusi ditolak; aksi yang mensyaratkan persetujuan dibatalkan"; }
    public boolean isRecorded() { return recorded; }
    public long getEntitledShares(String accountId) { return entitlementShares.getOrDefault(accountId, 0L); }
    public void captureEntitlements(Collection<Account> accounts) {
        if (recorded) return;
        for (Account account : accounts) {
            Position position = account.getPosition(ticker.contains(":") ? ticker : "IDX:" + ticker);
            entitlementShares.put(account.getId(), position == null ? 0 : position.getQuantity());
        }
        recorded = true;
    }
    public void close() { closed = true; }
    public org.json.JSONObject exportEligibility() throws org.json.JSONException {
        return new org.json.JSONObject().put("recorded", recorded).put("closed", closed).put("shares", new org.json.JSONObject(entitlementShares));
    }
    public void restoreEligibility(org.json.JSONObject obj) throws org.json.JSONException {
        if (obj == null) return;
        recorded = obj.optBoolean("recorded", false); closed = obj.optBoolean("closed", false);
        org.json.JSONObject shares = obj.optJSONObject("shares");
        if (shares != null) for (String id : shares.keySet()) {
            long count = shares.getLong(id);
            if (count < 0) throw new IllegalArgumentException("Hak suara save negatif");
            entitlementShares.put(id, count);
        }
    }

    public long getTotalVotesFor() { return totalVotesFor; }
    public long getTotalVotesAgainst() { return totalVotesAgainst; }
    public long getTotalVotesAbstain() { return totalVotesAbstain; }

    public synchronized boolean hasVoted(String accountId) {
        return votedAccountIds.contains(accountId);
    }

    public synchronized Set<String> getVotedAccountIds() {
        return Collections.unmodifiableSet(votedAccountIds);
    }

    public synchronized void restoreVotes(long votesFor, long votesAgainst, long votesAbstain, Collection<String> votedIds) {
        this.totalVotesFor = votesFor;
        this.totalVotesAgainst = votesAgainst;
        this.totalVotesAbstain = votesAbstain;
        this.votedAccountIds.clear();
        if (votedIds != null) this.votedAccountIds.addAll(votedIds);
    }

    /**
     * Menyampaikan hak suara pemegang saham.
     */
    public synchronized void castVote(String accountId, VoteChoice choice, long votingShares) {
        Objects.requireNonNull(choice, "Pilihan suara wajib");
        if (closed) throw new IllegalStateException("Voting sudah ditutup");
        if (votedAccountIds.contains(accountId)) {
            throw new IllegalStateException("Akun ini sudah pernah memberikan suara pada agenda ini (Idempoten)");
        }
        if (votingShares <= 0) {
            throw new IllegalArgumentException("Akun tidak memiliki hak suara (0 lembar saham pada recording date)");
        }

        switch (choice) {
            case FOR:
                totalVotesFor = Math.addExact(totalVotesFor, votingShares);
                break;
            case AGAINST:
                totalVotesAgainst = Math.addExact(totalVotesAgainst, votingShares);
                break;
            case ABSTAIN:
                totalVotesAbstain = Math.addExact(totalVotesAbstain, votingShares);
                break;
        }
        votedAccountIds.add(accountId);
    }
}
