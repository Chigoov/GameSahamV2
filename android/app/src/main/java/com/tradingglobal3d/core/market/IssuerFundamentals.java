package com.tradingglobal3d.core.market;

import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Model data fundamental lengkap untuk seluruh emiten (R08).
 * Menjamin persamaan akuntansi Neraca (Aset = Liabilitas + Ekuitas)
 * dan Arus Kas (Arus Kas Bersih = CFO + CFI + CFF) konsisten 100%.
 */
public final class IssuerFundamentals {
    private final String instrumentId;
    private final String ticker;
    private final String name;
    private final MarketExchange exchange;
    private final String sector;
    private final Currency currency;

    // Saham & Struktur Modal
    private final long sharesIssued;
    private final long sharesOutstanding;
    private final long freeFloatShares;
    private final double freeFloatPercent;
    private final int totalShareholdersCount;
    private final String majorShareholders;

    // Neraca (Balance Sheet): Aset = Liabilitas + Ekuitas
    private final BigDecimal totalAssets;
    private final BigDecimal totalLiabilities;
    private final BigDecimal totalEquity;

    // Laba Rugi (Income Statement)
    private final BigDecimal totalRevenue;
    private final BigDecimal operatingProfit;
    private final BigDecimal netProfit;

    // Arus Kas (Cash Flow): Net Cashflow = CFO + CFI + CFF
    private final BigDecimal cfo;
    private final BigDecimal cfi;
    private final BigDecimal cff;
    private final BigDecimal netCashflow;

    // Rasio Keuangan
    private final BigDecimal eps;
    private final BigDecimal dividendPerShare;
    private final double dividendPayoutRatio;

    // Metadata & Provenance
    private final String periodType;
    private final String identitySource;
    private final String financialSource;

    public IssuerFundamentals(String instrumentId, String ticker, String name,
                              MarketExchange exchange, String sector, Currency currency,
                              long sharesOutstanding, BigDecimal currentPrice) {
        this.instrumentId = Objects.requireNonNull(instrumentId);
        this.ticker = Objects.requireNonNull(ticker);
        this.name = Objects.requireNonNull(name);
        this.exchange = Objects.requireNonNull(exchange);
        this.sector = Objects.requireNonNull(sector);
        this.currency = Objects.requireNonNull(currency);

        long shares = Math.max(1_000_000L, sharesOutstanding);
        this.sharesOutstanding = shares;
        this.sharesIssued = Math.multiplyExact(shares / 100L, 105L); // 105% (5% treasury)
        this.freeFloatShares = Math.multiplyExact(shares / 100L, 40L); // 40% free float
        this.freeFloatPercent = 40.0;

        // Seed deterministik berbasis instrumentId hash
        int seed = Math.abs(instrumentId.hashCode());
        this.totalShareholdersCount = 25_000 + (seed % 150_000);
        this.majorShareholders = "Pendiri & Pengendali (55.0%), Lembaga Investasi & Reksa Dana (25.0%), Publik (20.0%)";

        BigDecimal price = currentPrice.max(BigDecimal.ONE);
        BigDecimal marketCap = price.multiply(BigDecimal.valueOf(shares));

        // Konstruksi Finansial Konsisten
        // Revenue berkisar 25% - 40% dari market cap
        double revFactor = 0.25 + ((seed % 15) / 100.0);
        this.totalRevenue = marketCap.multiply(BigDecimal.valueOf(revFactor)).setScale(0, RoundingMode.HALF_UP);

        // Operating Profit margin 20%
        this.operatingProfit = this.totalRevenue.multiply(new BigDecimal("0.20")).setScale(0, RoundingMode.HALF_UP);
        // Net Profit margin 15% dari revenue
        this.netProfit = this.totalRevenue.multiply(new BigDecimal("0.15")).setScale(0, RoundingMode.HALF_UP);

        // Ekuitas berkisar 40% - 60% dari market cap
        this.totalEquity = marketCap.multiply(new BigDecimal("0.50")).setScale(0, RoundingMode.HALF_UP);
        // Liabilitas = Ekuitas * DER (0.65 - 0.85)
        double derFactor = 0.65 + ((seed % 20) / 100.0);
        this.totalLiabilities = this.totalEquity.multiply(BigDecimal.valueOf(derFactor)).setScale(0, RoundingMode.HALF_UP);
        // ASET = LIABILITAS + EKUITAS (Neraca Seimbang 100%!)
        this.totalAssets = this.totalLiabilities.add(this.totalEquity);

        // Arus Kas Konsisten:
        // CFO (Operasi) positif ~ 120% net profit
        this.cfo = this.netProfit.multiply(new BigDecimal("1.20")).setScale(0, RoundingMode.HALF_UP);
        // CFI (Investasi Capex) negatif ~ 50% CFO
        this.cfi = this.cfo.multiply(new BigDecimal("-0.50")).setScale(0, RoundingMode.HALF_UP);
        // CFF (Pendanaan/Dividen/Hutang) negatif ~ 40% CFO
        this.cff = this.cfo.multiply(new BigDecimal("-0.40")).setScale(0, RoundingMode.HALF_UP);
        // NET CASHFLOW = CFO + CFI + CFF (Konsisten!)
        this.netCashflow = this.cfo.add(this.cfi).add(this.cff);

        // EPS = Net Profit / Shares
        this.eps = this.netProfit.divide(BigDecimal.valueOf(shares), 2, RoundingMode.HALF_UP);
        // Dividen Payout 35%
        this.dividendPayoutRatio = 35.0;
        this.dividendPerShare = this.eps.multiply(new BigDecimal("0.35")).setScale(2, RoundingMode.HALF_UP);

        this.periodType = "Tahunan (FY2026 Model Game)";
        this.identitySource = exchange == MarketExchange.IDX ? "Bursa Efek Indonesia (IDX)" : "NYSE/NASDAQ (US)";
        this.financialSource = "GAME_ECONOMY_MODEL";
    }

    public JSONObject toJsonObject(Money currentPrice) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("instrumentId", instrumentId);
        obj.put("ticker", ticker);
        obj.put("name", name);
        obj.put("exchange", exchange.name());
        obj.put("sector", sector);
        obj.put("currency", currency.name());

        BigDecimal price = currentPrice != null ? currentPrice.getAmount() : BigDecimal.ONE;
        BigDecimal mCap = price.multiply(BigDecimal.valueOf(sharesOutstanding));

        // 1. Saham & Struktur Kepemilikan
        JSONObject sharesObj = new JSONObject();
        sharesObj.put("sharesIssued", sharesIssued);
        sharesObj.put("sharesOutstanding", sharesOutstanding);
        sharesObj.put("freeFloatShares", freeFloatShares);
        sharesObj.put("freeFloatPercent", freeFloatPercent);
        sharesObj.put("totalShareholdersCount", totalShareholdersCount);
        sharesObj.put("majorShareholders", majorShareholders);
        obj.put("shareStructure", sharesObj);

        // 2. Neraca (Aset = Liabilitas + Ekuitas)
        JSONObject balanceSheet = new JSONObject();
        balanceSheet.put("totalAssets", totalAssets.toPlainString());
        balanceSheet.put("totalLiabilities", totalLiabilities.toPlainString());
        balanceSheet.put("totalEquity", totalEquity.toPlainString());
        balanceSheet.put("isBalanced", totalAssets.equals(totalLiabilities.add(totalEquity)));
        obj.put("balanceSheet", balanceSheet);

        // 3. Laba Rugi
        JSONObject incomeStatement = new JSONObject();
        incomeStatement.put("totalRevenue", totalRevenue.toPlainString());
        incomeStatement.put("operatingProfit", operatingProfit.toPlainString());
        incomeStatement.put("netProfit", netProfit.toPlainString());
        obj.put("incomeStatement", incomeStatement);

        // 4. Arus Kas
        JSONObject cashFlow = new JSONObject();
        cashFlow.put("cfo", cfo.toPlainString());
        cashFlow.put("cfi", cfi.toPlainString());
        cashFlow.put("cff", cff.toPlainString());
        cashFlow.put("netCashflow", netCashflow.toPlainString());
        obj.put("cashFlow", cashFlow);

        // 5. Valuasi & Rasio Keuangan
        JSONObject valuation = new JSONObject();
        valuation.put("marketCap", mCap.toPlainString());
        valuation.put("eps", eps.toPlainString());
        BigDecimal per = eps.signum() > 0 ? price.divide(eps, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        valuation.put("per", per.signum() > 0 ? per.toPlainString() + "x" : "N/A (Laba Negatif)");

        BigDecimal bps = totalEquity.divide(BigDecimal.valueOf(sharesOutstanding), 2, RoundingMode.HALF_UP);
        BigDecimal pbv = bps.signum() > 0 ? price.divide(bps, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        valuation.put("pbv", pbv.toPlainString() + "x");

        BigDecimal roe = totalEquity.signum() > 0 ? netProfit.multiply(BigDecimal.valueOf(100)).divide(totalEquity, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        valuation.put("roe", roe.toPlainString() + "%");

        BigDecimal roa = totalAssets.signum() > 0 ? netProfit.multiply(BigDecimal.valueOf(100)).divide(totalAssets, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        valuation.put("roa", roa.toPlainString() + "%");

        BigDecimal der = totalEquity.signum() > 0 ? totalLiabilities.divide(totalEquity, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        valuation.put("der", der.toPlainString() + "x");

        valuation.put("dividendPerShare", dividendPerShare.toPlainString());
        BigDecimal divYield = price.signum() > 0 ? dividendPerShare.multiply(BigDecimal.valueOf(100)).divide(price, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        valuation.put("dividendYield", divYield.toPlainString() + "%");
        valuation.put("dividendPayoutRatio", dividendPayoutRatio + "%");
        obj.put("valuationRatios", valuation);

        // 6. Metadata & Provenance
        obj.put("periodType", periodType);
        obj.put("identitySource", identitySource);
        obj.put("financialSource", financialSource);
        obj.put("success", true);
        return obj;
    }
}
