package com.tradingglobal3d.core.risk;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Layanan Asuransi Portofolio Lindung Nilai / Hedging (Fase 3 Fitur 2).
 * Melindungi modal pemain saat terjadi kejatuhan pasar ekstrem (crash > 5%)
 * dengan penggantian otomatis 80% dari nilai kerugian portofolio.
 */
public final class PortfolioInsuranceService {

    public static final class Policy {
        private final String policyId;
        private final int purchasedSession;
        private final int expirySession;
        private final double coveragePercentage; // default 80%
        private final Money premiumPaid;
        private boolean isClaimed;

        public Policy(String policyId, int purchasedSession, int expirySession,
                      double coveragePercentage, Money premiumPaid) {
            this.policyId = Objects.requireNonNull(policyId);
            this.purchasedSession = purchasedSession;
            this.expirySession = expirySession;
            this.coveragePercentage = coveragePercentage;
            this.premiumPaid = Objects.requireNonNull(premiumPaid);
            this.isClaimed = false;
        }

        public String getPolicyId() { return policyId; }
        public int getPurchasedSession() { return purchasedSession; }
        public int getExpirySession() { return expirySession; }
        public double getCoveragePercentage() { return coveragePercentage; }
        public Money getPremiumPaid() { return premiumPaid; }
        public boolean isClaimed() { return isClaimed; }

        public boolean isValidAtSession(int currentSession) {
            return !isClaimed && currentSession >= purchasedSession && currentSession <= expirySession;
        }

        public void markClaimed() {
            this.isClaimed = true;
        }

        public JSONObject toJsonObject(int currentSession) throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("policyId", policyId);
            obj.put("purchasedSession", purchasedSession);
            obj.put("expirySession", expirySession);
            obj.put("coveragePercentage", String.format(java.util.Locale.ROOT, "%.0f%%", coveragePercentage));
            obj.put("premiumPaid", premiumPaid.format());
            obj.put("isClaimed", isClaimed);
            obj.put("isValid", isValidAtSession(currentSession));
            obj.put("remainingSessions", Math.max(0, expirySession - currentSession));
            return obj;
        }
    }

    private Policy activePolicy = null;

    public synchronized Policy getActivePolicy() {
        return activePolicy;
    }

    public synchronized Policy purchasePolicy(Account account, Money portfolioValue, int durationSessions,
                                              EconomyLedger ledger, long now, int currentSession) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(portfolioValue);
        int duration = Math.max(1, durationSessions);

        // Premi: 1.0% dari total nilai portofolio (minimal Rp 500.000)
        BigDecimal premAmt = portfolioValue.getAmount().multiply(new BigDecimal("0.0100")).setScale(0, RoundingMode.HALF_UP);
        if (premAmt.compareTo(BigDecimal.valueOf(500_000)) < 0) {
            premAmt = BigDecimal.valueOf(500_000);
        }
        Money premium = new Money(premAmt, Currency.IDR);

        Money available = account.getAvailableCash(Currency.IDR);
        if (available.isLessThan(premium)) {
            throw new IllegalStateException("Saldo kas IDR tidak mencukupi untuk membayar premi asuransi: " + premium.format());
        }

        String polId = "POL-" + System.currentTimeMillis();
        ledger.recordMutation(
                "INS-PREM-" + polId,
                account,
                LedgerEntryType.TRANSACTION_FEE,
                premium,
                false, // debit
                now,
                "Pembelian Polis Asuransi Portofolio (Hedging Lindung Nilai " + duration + " Sesi)"
        );

        this.activePolicy = new Policy(polId, currentSession, currentSession + duration, 80.0, premium);
        return activePolicy;
    }

    public synchronized Money evaluateMarketCrashClaim(Account account, double marketDropPercent, Money lossAmount,
                                                       EconomyLedger ledger, long now, int currentSession) {
        if (activePolicy == null || !activePolicy.isValidAtSession(currentSession)) {
            return Money.zero(Currency.IDR);
        }

        // Syarat klaim: kejatuhan pasar ekstrem > 5.0% dan nilai kerugian positif
        if (marketDropPercent >= 5.0 && lossAmount != null && lossAmount.isPositive()) {
            BigDecimal reimbVal = lossAmount.getAmount().multiply(new BigDecimal("0.80")).setScale(0, RoundingMode.HALF_UP);
            Money reimbursement = new Money(reimbVal, Currency.IDR);

            ledger.recordMutation(
                    "INS-CLAIM-" + activePolicy.getPolicyId(),
                    account,
                    LedgerEntryType.BAILOUT,
                    reimbursement,
                    true, // kredit
                    now,
                    "Klaim Asuransi Portofolio (Penggantian 80% Kerugian Crash " + String.format(java.util.Locale.ROOT, "%.2f%%", marketDropPercent) + ")"
            );

            activePolicy.markClaimed();
            return reimbursement;
        }

        return Money.zero(Currency.IDR);
    }

    public synchronized JSONObject toJsonObject(int currentSession) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("hasActivePolicy", activePolicy != null && activePolicy.isValidAtSession(currentSession));
        obj.put("policy", activePolicy != null ? activePolicy.toJsonObject(currentSession) : JSONObject.NULL);
        return obj;
    }
}
