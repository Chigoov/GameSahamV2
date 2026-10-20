package com.tradingglobal3d.core.career;

import com.tradingglobal3d.core.economy.*;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import org.json.*;
import java.math.*;
import java.util.List;

/** Persisted period boundary; internal transfers and loan principal are not external returns. */
public final class PerformancePeriod {
    private final long start;
    private final Money opening;
    private final int firstEntry;

    public PerformancePeriod(long start, Money opening, int firstEntry) {
        if (start < 0 || firstEntry < 0 || opening.getCurrency() != Currency.IDR)
            throw new IllegalArgumentException("Batas periode tidak valid");
        this.start = start; this.opening = opening; this.firstEntry = firstEntry;
    }

    public JSONObject calculate(long end, Money closing, List<LedgerEntry> entries, BigDecimal usdIdr) throws JSONException {
        BigDecimal flow = BigDecimal.ZERO, weighted = BigDecimal.ZERO;
        long duration = Math.max(0, end - start);
        for (int i = Math.min(firstEntry, entries.size()); i < entries.size(); i++) {
            LedgerEntry entry = entries.get(i);
            switch (entry.getType()) {
                case DEPOSIT: case WITHDRAWAL: case SALARY: case MISSION_REWARD: case BAILOUT: break;
                default: continue;
            }
            BigDecimal amount = entry.getAmount().getAmount();
            if (entry.getCurrency() == Currency.USD) amount = amount.multiply(usdIdr);
            if (!entry.isCredit()) amount = amount.negate();
            flow = flow.add(amount);
            long at = Math.max(start, Math.min(end, entry.getTimestamp()));
            BigDecimal weight = duration == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(end - at)
                    .divide(BigDecimal.valueOf(duration), 16, RoundingMode.HALF_UP);
            weighted = weighted.add(amount.multiply(weight));
        }
        BigDecimal gain = closing.getAmount().subtract(opening.getAmount()).subtract(flow);
        BigDecimal denominator = opening.getAmount().add(weighted);
        boolean available = duration > 0 && denominator.signum() > 0;
        return new JSONObject().put("success", true).put("method", "MODIFIED_DIETZ")
                .put("startTimestamp", start).put("endTimestamp", end).put("currency", "IDR")
                .put("openingRaw", opening.getAmount().toPlainString()).put("closingRaw", closing.getAmount().toPlainString())
                .put("nonInvestmentFlowsRaw", flow.toPlainString()).put("investmentResultRaw", gain.toPlainString())
                .put("denominatorRaw", denominator.toPlainString()).put("returnAvailable", available)
                .put("returnPercent", available ? gain.multiply(BigDecimal.valueOf(100)).divide(denominator, 8, RoundingMode.HALF_UP) : JSONObject.NULL)
                .put("reason", available ? "" : "Periode belum berjalan atau modal tertimbang tidak positif")
                .put("fxBasis", "Kurs valuasi domain periode");
    }

    public JSONObject exportState() throws JSONException {
        return new JSONObject().put("start", start).put("opening", opening.getAmount().toPlainString()).put("firstEntry", firstEntry);
    }
    public static PerformancePeriod restore(JSONObject state) throws JSONException {
        return new PerformancePeriod(state.getLong("start"), new Money(new BigDecimal(state.getString("opening")), Currency.IDR), state.getInt("firstEntry"));
    }
}
