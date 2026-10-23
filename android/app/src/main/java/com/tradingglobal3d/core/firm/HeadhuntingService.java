package com.tradingglobal3d.core.firm;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Layanan Headhunting & Pembajakan Karyawan Elit Pesaing (Fase 3 Fitur 10).
 */
public final class HeadhuntingService {

    public static final class Candidate {
        private final String candidateId;
        private final String name;
        private final String roleTitle;
        private final int skillRating;
        private final Money poachCost;
        private final Money expectedSalary;
        private final String currentEmployer;
        private double loyaltyScore;
        private boolean isHired;

        public Candidate(String candidateId, String name, String roleTitle, int skillRating,
                         Money poachCost, Money expectedSalary, String currentEmployer) {
            this.candidateId = Objects.requireNonNull(candidateId);
            this.name = Objects.requireNonNull(name);
            this.roleTitle = Objects.requireNonNull(roleTitle);
            this.skillRating = skillRating;
            this.poachCost = Objects.requireNonNull(poachCost);
            this.expectedSalary = Objects.requireNonNull(expectedSalary);
            this.currentEmployer = Objects.requireNonNull(currentEmployer);
            this.loyaltyScore = 95.0;
            this.isHired = false;
        }

        public String getCandidateId() { return candidateId; }
        public String getName() { return name; }
        public String getRoleTitle() { return roleTitle; }
        public int getSkillRating() { return skillRating; }
        public Money getPoachCost() { return poachCost; }
        public Money getExpectedSalary() { return expectedSalary; }
        public String getCurrentEmployer() { return currentEmployer; }
        public double getLoyaltyScore() { return loyaltyScore; }
        public boolean isHired() { return isHired; }

        public void hire() {
            this.isHired = true;
            this.loyaltyScore = 100.0;
        }

        public JSONObject toJsonObject() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("candidateId", candidateId);
            obj.put("name", name);
            obj.put("roleTitle", roleTitle);
            obj.put("skillRating", skillRating);
            obj.put("poachCost", poachCost.format());
            obj.put("expectedSalary", expectedSalary.format());
            obj.put("currentEmployer", currentEmployer);
            obj.put("loyaltyScore", loyaltyScore);
            obj.put("isHired", isHired);
            obj.put("statusLabel", isHired ? "DIREKRUT (STAFF AKTIF)" : "TERSEDIA DI BURSA TALENTA");
            return obj;
        }
    }

    private final Map<String, Candidate> candidates = new LinkedHashMap<>();

    public HeadhuntingService() {
        initializeCandidates();
    }

    private void initializeCandidates() {
        candidates.clear();
        candidates.put("CAND-01", new Candidate("CAND-01", "Budi Santoso, CFA", "Analis Makro Kuantitatif", 88, Money.of(15_000_000, Currency.IDR), Money.of(12_000_000, Currency.IDR), "Mandiri Investasi Utama"));
        candidates.put("CAND-02", new Candidate("CAND-02", "Sarah Jenkins, MBA", "Spesialis Portofolio Global", 92, Money.of(25_000_000, Currency.IDR), Money.of(18_000_000, Currency.IDR), "Morgan Wall St Group"));
        candidates.put("CAND-03", new Candidate("CAND-03", "Hendra Kusuma, M.Fin", "Manajer Risiko Finansial", 85, Money.of(12_000_000, Currency.IDR), Money.of(10_000_000, Currency.IDR), "Batavia Capital Trust"));
    }

    public synchronized void reset() {
        initializeCandidates();
    }

    public List<Candidate> getAllCandidates() {
        return new ArrayList<>(candidates.values());
    }

    public Candidate getCandidate(String candidateId) {
        return candidates.get(candidateId);
    }

    public synchronized Candidate poachCandidate(String candidateId, Account personal, EconomyLedger ledger, long now) {
        Candidate cand = candidates.get(candidateId);
        if (cand == null) throw new IllegalArgumentException("Kandidat tidak ditemukan: " + candidateId);
        if (cand.isHired()) return cand;

        Money cost = cand.getPoachCost();
        Money avail = personal.getAvailableCash(Currency.IDR);
        if (avail.isLessThan(cost)) {
            throw new IllegalStateException("Kas IDR tidak mencukupi untuk biaya bajak analis: " + cost.format());
        }

        ledger.recordMutation(
                "POACH-" + cand.getCandidateId() + "-" + System.currentTimeMillis(),
                personal,
                LedgerEntryType.COMPANY_PAYROLL,
                cost,
                false, // debit
                now,
                "Biaya Jasa Headhunter & Pembajakan Staf: " + cand.getName() + " dari " + cand.getCurrentEmployer()
        );

        cand.hire();
        return cand;
    }

    public synchronized JSONArray exportCandidates() throws JSONException {
        JSONArray arr = new JSONArray();
        for (Candidate c : candidates.values()) {
            arr.put(c.toJsonObject());
        }
        return arr;
    }
}
