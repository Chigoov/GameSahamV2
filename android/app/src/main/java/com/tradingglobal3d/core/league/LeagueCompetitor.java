package com.tradingglobal3d.core.league;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Representasi peserta kompetisi liga dan rivalitas (D09).
 */
public final class LeagueCompetitor {

    private final String id;
    private final String name;
    private final String title;
    private final String style; // KONSERVATIF, SEIMBANG, AGRESIF
    private Money netWorth;
    private double weeklyReturnPercent;
    private final boolean isPlayer;

    public LeagueCompetitor(String id, String name, String title, String style, Money netWorth, double weeklyReturnPercent, boolean isPlayer) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.title = Objects.requireNonNull(title);
        this.style = Objects.requireNonNull(style);
        this.netWorth = Objects.requireNonNull(netWorth);
        this.weeklyReturnPercent = weeklyReturnPercent;
        this.isPlayer = isPlayer;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getStyle() { return style; }
    public Money getNetWorth() { return netWorth; }
    public double getWeeklyReturnPercent() { return weeklyReturnPercent; }
    public boolean isPlayer() { return isPlayer; }

    public void setNetWorth(Money nw) { this.netWorth = Objects.requireNonNull(nw); }
    public void setWeeklyReturnPercent(double ret) { this.weeklyReturnPercent = ret; }

    public JSONObject toJsonObject(int rank) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("rank", rank);
        obj.put("id", id);
        obj.put("name", name);
        obj.put("title", title);
        obj.put("style", style);
        obj.put("netWorth", netWorth.format());
        obj.put("netWorthRaw", netWorth.getAmount().doubleValue());
        obj.put("weeklyReturn", String.format(java.util.Locale.ROOT, "%+.2f%%", weeklyReturnPercent));
        obj.put("weeklyReturnRaw", weeklyReturnPercent);
        obj.put("isPlayer", isPlayer);
        return obj;
    }

    public JSONObject exportState() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("name", name);
        obj.put("title", title);
        obj.put("style", style);
        obj.put("netWorth", netWorth.getAmount().toPlainString());
        obj.put("weeklyReturnPercent", weeklyReturnPercent);
        obj.put("isPlayer", isPlayer);
        return obj;
    }

    public static LeagueCompetitor restoreFromState(JSONObject obj) {
        if (obj == null) return null;
        String id = obj.optString("id");
        String name = obj.optString("name");
        String title = obj.optString("title");
        String style = obj.optString("style", "SEIMBANG");
        Money nw = new Money(new BigDecimal(obj.optString("netWorth", "50000000")), Currency.IDR);
        double ret = obj.optDouble("weeklyReturnPercent", 0.0);
        boolean isP = obj.optBoolean("isPlayer", false);
        return new LeagueCompetitor(id, name, title, style, nw, ret, isP);
    }
}
