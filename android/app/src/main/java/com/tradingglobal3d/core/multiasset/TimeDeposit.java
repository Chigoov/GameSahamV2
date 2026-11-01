package com.tradingglobal3d.core.multiasset;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Model Penempatan Deposito Berjangka Perbankan (R10).
 */
public final class TimeDeposit {
    private final String id;
    private final String productId;
    private final String bankName;
    private final Money principal;
    private final double annualInterestRate;
    private final double bonusInterestPercent;
    private final int tenorDays;
    private final long placedTimestamp;
    private final long maturityTimestamp;

    private String status; // "ACTIVE", "MATURED", "WITHDRAWN_EARLY"
    private Money interestPaid;
    private Money penaltyPaid;

    public TimeDeposit(String id, String productId, String bankName, Money principal,
                       double annualInterestRate, int tenorDays, long placedTimestamp) {
        this(id, productId, bankName, principal, annualInterestRate, 0.0, tenorDays, placedTimestamp);
    }

    public TimeDeposit(String id, String productId, String bankName, Money principal,
                       double annualInterestRate, double bonusInterestPercent, int tenorDays, long placedTimestamp) {
        this.id = Objects.requireNonNull(id);
        this.productId = Objects.requireNonNull(productId);
        this.bankName = Objects.requireNonNull(bankName);
        this.principal = Objects.requireNonNull(principal);
        this.annualInterestRate = annualInterestRate;
        if (!Double.isFinite(bonusInterestPercent) || bonusInterestPercent < 0.0)
            throw new IllegalArgumentException("Bonus bunga deposito tidak valid");
        this.bonusInterestPercent = bonusInterestPercent;
        this.tenorDays = Math.max(1, tenorDays);
        this.placedTimestamp = placedTimestamp;
        // 1 hari game = 86400 detik = 86400_000 ms
        this.maturityTimestamp = placedTimestamp + ((long) tenorDays * 86_400_000L);
        this.status = "ACTIVE";
        this.interestPaid = Money.zero(principal.getCurrency());
        this.penaltyPaid = Money.zero(principal.getCurrency());
    }

    public String getId() { return id; }
    public String getProductId() { return productId; }
    public String getBankName() { return bankName; }
    public Money getPrincipal() { return principal; }
    public double getAnnualInterestRate() { return annualInterestRate; }
    public int getTenorDays() { return tenorDays; }
    public long getPlacedTimestamp() { return placedTimestamp; }
    public long getMaturityTimestamp() { return maturityTimestamp; }
    public String getStatus() { return status; }
    public Money getInterestPaid() { return interestPaid; }
    public Money getPenaltyPaid() { return penaltyPaid; }

    public boolean isMatured(long currentTimestamp) {
        return currentTimestamp >= maturityTimestamp;
    }

    public Money calculateAccruedInterest(long currentTimestamp) {
        long elapsedMs = Math.max(0L, Math.min(maturityTimestamp, currentTimestamp) - placedTimestamp);
        double elapsedDays = (double) elapsedMs / 86_400_000.0;
        BigDecimal effectiveAnnualRate = BigDecimal.valueOf(annualInterestRate)
                .multiply(BigDecimal.ONE.add(BigDecimal.valueOf(bonusInterestPercent).movePointLeft(2)));
        BigDecimal annualReturn = principal.getAmount().multiply(effectiveAnnualRate);
        BigDecimal accrued = annualReturn.multiply(BigDecimal.valueOf(elapsedDays / 365.0)).setScale(0, RoundingMode.HALF_UP);
        return new Money(accrued, principal.getCurrency());
    }

    public void markMatured(Money interest) {
        this.status = "MATURED";
        this.interestPaid = interest;
    }

    public void markEarlyWithdrawn(Money interest, Money penalty) {
        this.status = "WITHDRAWN_EARLY";
        this.interestPaid = interest;
        this.penaltyPaid = penalty;
    }

    public JSONObject toJsonObject(long currentTimestamp) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("productId", productId);
        obj.put("bankName", bankName);
        obj.put("principal", principal.format());
        obj.put("principalRaw", principal.getAmount().toPlainString());
        obj.put("annualInterestRate", String.format(java.util.Locale.ROOT, "%.2f%%", annualInterestRate * 100));
        obj.put("bonusInterestPercent", bonusInterestPercent);
        obj.put("effectiveAnnualInterestRate", String.format(java.util.Locale.ROOT, "%.2f%%",
                annualInterestRate * (1.0 + bonusInterestPercent / 100.0) * 100.0));
        obj.put("tenorDays", tenorDays);
        obj.put("placedTimestamp", placedTimestamp);
        obj.put("maturityTimestamp", maturityTimestamp);
        obj.put("status", status);
        obj.put("isMatured", isMatured(currentTimestamp));
        obj.put("accruedInterest", calculateAccruedInterest(currentTimestamp).format());
        obj.put("interestPaid", interestPaid.format());
        obj.put("penaltyPaid", penaltyPaid.format());
        return obj;
    }

    public JSONObject exportState() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id).put("productId", productId).put("bankName", bankName)
                .put("principal", principal.getAmount().toPlainString()).put("currency", principal.getCurrency().name())
                .put("annualInterestRate", annualInterestRate).put("bonusInterestPercent", bonusInterestPercent)
                .put("tenorDays", tenorDays).put("placedTimestamp", placedTimestamp)
                .put("status", status).put("interestPaid", interestPaid.getAmount().toPlainString())
                .put("penaltyPaid", penaltyPaid.getAmount().toPlainString());
        return obj;
    }

    public static TimeDeposit fromState(JSONObject obj) throws JSONException {
        Currency currency = Currency.valueOf(obj.getString("currency"));
        TimeDeposit deposit = new TimeDeposit(obj.getString("id"), obj.getString("productId"), obj.getString("bankName"),
                new Money(new BigDecimal(obj.getString("principal")), currency), obj.getDouble("annualInterestRate"),
                obj.optDouble("bonusInterestPercent", 0.0), obj.getInt("tenorDays"), obj.getLong("placedTimestamp"));
        String status = obj.getString("status");
        if ("ACTIVE".equals(status)) {
            deposit.status = status;
        } else if ("MATURED".equals(status)) {
            deposit.markMatured(new Money(new BigDecimal(obj.getString("interestPaid")), currency));
        } else if ("WITHDRAWN_EARLY".equals(status)) {
            deposit.markEarlyWithdrawn(new Money(new BigDecimal(obj.getString("interestPaid")), currency),
                    new Money(new BigDecimal(obj.getString("penaltyPaid")), currency));
        } else {
            throw new IllegalArgumentException("Status deposito save tidak dikenal");
        }
        return deposit;
    }
}
