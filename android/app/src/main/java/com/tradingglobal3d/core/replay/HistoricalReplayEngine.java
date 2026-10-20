package com.tradingglobal3d.core.replay;

import com.tradingglobal3d.core.market.HistoricalBar;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Mesin Pemutaran Ulang Replay Data Historis Nyata & Stress Testing Makro.
 */
public final class HistoricalReplayEngine {
    private final Map<String, StrategyLabSandbox> sandboxes = new HashMap<>();
    private String activeDatasetId = null;
    private List<HistoricalBar> datasetBars = new ArrayList<>();
    private int currentBarIndex = 0;
    private boolean isReplayActive = false;

    public void reset() {
        sandboxes.clear();
        activeDatasetId = null;
        datasetBars.clear();
        currentBarIndex = 0;
        isReplayActive = false;
    }

    public void startReplay(String datasetId, List<HistoricalBar> bars) {
        Objects.requireNonNull(datasetId);
        Objects.requireNonNull(bars);
        if (bars.isEmpty()) throw new IllegalArgumentException("Dataset bar tidak boleh kosong");
        LocalDate previous = null;
        for (HistoricalBar bar : bars) {
            Objects.requireNonNull(bar, "Bar tidak boleh null");
            if (previous != null && !bar.getDate().isAfter(previous)) throw new IllegalArgumentException("Tanggal replay wajib unik dan naik");
            previous = bar.getDate();
        }
        this.activeDatasetId = datasetId;
        this.datasetBars = new ArrayList<>(bars);
        this.currentBarIndex = 0;
        this.isReplayActive = true;
    }

    public boolean isReplayActive() {
        return isReplayActive;
    }

    public String getActiveDatasetId() {
        return activeDatasetId;
    }

    public int getCurrentBarIndex() {
        return currentBarIndex;
    }

    public int getTotalBars() {
        return datasetBars.size();
    }

    public HistoricalBar getCurrentBar() {
        if (!isReplayActive || datasetBars.isEmpty()) return null;
        int idx = Math.min(currentBarIndex, datasetBars.size() - 1);
        return datasetBars.get(idx);
    }

    public List<HistoricalBar> getVisibleBars() {
        if (!isReplayActive || datasetBars.isEmpty()) return Collections.emptyList();
        int end = Math.min(currentBarIndex, datasetBars.size());
        return Collections.unmodifiableList(datasetBars.subList(0, end));
    }

    /** Daily OHLCV is published only at the model close, never at that day's open. */
    public List<HistoricalBar> getVisibleBars(LocalDate date, LocalTime time) {
        if (!isReplayActive) return Collections.emptyList();
        List<HistoricalBar> visible = new ArrayList<>();
        for (HistoricalBar bar : datasetBars) {
            if (bar.getDate().isBefore(date) || (bar.getDate().equals(date) && !time.isBefore(LocalTime.of(16, 0)))) visible.add(bar);
            else break;
        }
        return Collections.unmodifiableList(visible);
    }

    /** Calendar dates, including weekends/gaps, do not consume an unrelated trading row. */
    public boolean synchronizeToDate(LocalDate date) {
        if (!isReplayActive || datasetBars.isEmpty()) return false;
        int old = currentBarIndex;
        while (currentBarIndex + 1 < datasetBars.size() && !datasetBars.get(currentBarIndex + 1).getDate().isAfter(date)) currentBarIndex++;
        return currentBarIndex != old;
    }

    public boolean supportsInstrument(String instrumentId) { return isReplayActive && activeDatasetId.equals(instrumentId); }

    public BigDecimal getObservablePrice(LocalDate date, LocalTime time) {
        HistoricalBar bar = getCurrentBar();
        if (bar == null || bar.getDate().isAfter(date)) return null;
        if (bar.getDate().isBefore(date) || !time.isBefore(LocalTime.of(16, 0))) return bar.getClose();
        if (time.isBefore(LocalTime.of(9, 0))) {
            return currentBarIndex == 0 ? null : datasetBars.get(currentBarIndex - 1).getClose();
        }
        return bar.getOpen();
    }

    /**
     * ponytail: daily opening-reference execution only; add intraday execution only with licensed finer data.
     * Day high/low cannot prove the path or justify a fill before close.
     */
    public BigDecimal executablePrice(Order order, LocalDate date, LocalTime time) {
        HistoricalBar bar = getCurrentBar();
        if (!supportsInstrument(order.getInstrumentId()) || bar == null || !bar.getDate().equals(date)
                || time.isBefore(LocalTime.of(9, 0)) || !time.isBefore(LocalTime.of(16, 0))) return null;
        BigDecimal price = bar.getOpen();
        if (order.getLimitPrice() != null) {
            int cmp = order.getLimitPrice().getAmount().compareTo(price);
            if ((order.isBuy() && cmp < 0) || (order.isSell() && cmp > 0)) return null;
        }
        return price;
    }

    public boolean advanceBar() {
        if (!isReplayActive) return false;
        if (currentBarIndex + 1 < datasetBars.size()) {
            currentBarIndex++;
            return true;
        }
        return false;
    }

    public boolean isReplayCompleted() {
        return isReplayActive && currentBarIndex >= datasetBars.size() - 1;
    }

    public boolean isReplayCompleted(LocalDate date, LocalTime time) {
        if (!isReplayActive || datasetBars.isEmpty()) return false;
        LocalDate lastDate = datasetBars.get(datasetBars.size() - 1).getDate();
        return date.isAfter(lastDate) || (date.equals(lastDate) && !time.isBefore(LocalTime.of(16, 0)));
    }

    public org.json.JSONObject exportState() throws org.json.JSONException {
        org.json.JSONObject obj = new org.json.JSONObject();
        obj.put("isReplayActive", isReplayActive);
        obj.put("activeDatasetId", activeDatasetId == null ? org.json.JSONObject.NULL : activeDatasetId);
        obj.put("currentBarIndex", currentBarIndex);
        obj.put("datasetSha256", isReplayActive ? com.tradingglobal3d.core.market.HistoricalDatasetLoader.VERIFIED_SHA256 : org.json.JSONObject.NULL);
        return obj;
    }

    public void restoreState(org.json.JSONObject obj) throws org.json.JSONException {
        if (obj == null) return;
        if (!obj.optBoolean("isReplayActive", false)) { reset(); return; }
        String id = obj.optString("activeDatasetId", "");
        int index = obj.optInt("currentBarIndex", -1);
        if (!com.tradingglobal3d.core.market.HistoricalDatasetLoader.VERIFIED_SHA256.equals(obj.optString("datasetSha256", "")))
            throw new IllegalArgumentException("Versi dataset save replay tidak cocok; progres lama dipertahankan untuk pemulihan");
        List<HistoricalBar> loaded;
        try { loaded = com.tradingglobal3d.core.market.HistoricalDatasetLoader.loadDataset(id); }
        catch (java.io.IOException e) { throw new IllegalArgumentException("Dataset replay tidak dapat dipulihkan", e); }
        if (index < 0 || index >= loaded.size()) throw new IllegalArgumentException("Indeks replay save di luar dataset");
        startReplay(com.tradingglobal3d.core.market.HistoricalDatasetLoader.canonicalDatasetId(id), loaded);
        currentBarIndex = index;
    }

    public StrategyLabSandbox createSandbox(String sandboxId, Account sourceAccount) {
        StrategyLabSandbox sb = new StrategyLabSandbox(sandboxId, sourceAccount);
        sandboxes.put(sandboxId, sb);
        return sb;
    }

    public StrategyLabSandbox getSandbox(String sandboxId) {
        return sandboxes.get(sandboxId);
    }

    /**
     * Menjalankan simulasi Stress Test terhadap akun pemain (FR-17.01, FR-17.02).
     */
    public StressTestResult runStressTest(
            Account account,
            StressTestScenario scenario,
            InstrumentCatalog catalog,
            BigDecimal currentUsdRate,
            Money currentMarginDebt) {

        Currency baseCur = Currency.IDR;
        BigDecimal cashIdr = account.getTotalCash(baseCur).getAmount();
        BigDecimal cashUsd = account.getTotalCash(Currency.USD).getAmount();

        // Kurs dasar dan kurs stres
        BigDecimal stressedUsdRate = currentUsdRate.multiply(scenario.getFxRateMultiplier());

        // 1. Hitung nilai dasar portofolio saat ini
        BigDecimal baselineTotal = cashIdr.add(cashUsd.multiply(currentUsdRate));
        BigDecimal stressedTotal = cashIdr.add(cashUsd.multiply(stressedUsdRate));

        for (Position p : account.getPositions().values()) {
            if (p.getQuantity() > 0) {
                BigDecimal curPrice = p.getCurrentPrice();
                BigDecimal rate = (p.getCurrency() == Currency.USD) ? currentUsdRate : BigDecimal.ONE;
                BigDecimal posVal = curPrice.multiply(BigDecimal.valueOf(p.getQuantity())).multiply(rate);
                baselineTotal = baselineTotal.add(posVal);

                // Harga stres
                BigDecimal stressedPrice = curPrice.multiply(scenario.getAssetDropMultiplier());
                BigDecimal stressedRate = (p.getCurrency() == Currency.USD) ? stressedUsdRate : BigDecimal.ONE;
                BigDecimal stressedVal = stressedPrice.multiply(BigDecimal.valueOf(p.getQuantity())).multiply(stressedRate);
                stressedTotal = stressedTotal.add(stressedVal);
            }
        }

        // Kurangi utang margin
        BigDecimal baselineEquity = baselineTotal.subtract(currentMarginDebt.getAmount());
        BigDecimal stressedEquity = stressedTotal.subtract(currentMarginDebt.getAmount());

        BigDecimal lossAmt = baselineEquity.subtract(stressedEquity);
        if (lossAmt.compareTo(BigDecimal.ZERO) < 0) lossAmt = BigDecimal.ZERO;

        BigDecimal dropPct = BigDecimal.ZERO;
        if (baselineEquity.compareTo(BigDecimal.ZERO) > 0) {
            dropPct = lossAmt.divide(baselineEquity, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        // Apakah memicu margin call (jika ekuitas stres < 30% dari eksposur)
        boolean triggersMarginCall = false;
        if (currentMarginDebt.isPositive() && stressedTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal marginRatio = stressedEquity.divide(stressedTotal, 4, RoundingMode.HALF_UP);
            if (marginRatio.compareTo(new BigDecimal("0.30")) < 0) {
                triggersMarginCall = true;
            }
        }

        String assessment = "Penurunan terproyeksi sebesar " + dropPct.setScale(2, RoundingMode.HALF_UP) + "%";
        if (triggersMarginCall) {
            assessment += " - PERINGATAN: Skenario ini akan memicu MARGIN CALL dan potensi likuidasi paksa!";
        }

        return new StressTestResult(
                scenario.getId(),
                new Money(baselineEquity.setScale(0, RoundingMode.HALF_UP), baseCur),
                new Money(stressedEquity.setScale(0, RoundingMode.HALF_UP), baseCur),
                new Money(lossAmt.setScale(0, RoundingMode.HALF_UP), baseCur),
                dropPct,
                triggersMarginCall,
                assessment
        );
    }

    /**
     * Model fill order historis yang jujur pada candle bar (FR-01, FR-17.05).
     * Order beli limit pada harga P terisi hanya jika P >= bar.getLow().
     * Order jual limit pada harga P terisi hanya jika P <= bar.getHigh().
     */
    public boolean canFillOrderOnHistoricalBar(Order order, HistoricalBar bar) {
        if (order.getLimitPrice() == null) return true; // Market order selalu fill
        BigDecimal orderPrice = order.getLimitPrice().getAmount();
        BigDecimal low = bar.getLow();
        BigDecimal high = bar.getHigh();

        if (order.getSide() == OrderSide.BUY) {
            // Pembeli bersedia membayar hingga orderPrice. Jika Low <= orderPrice, order dapat terisi
            return orderPrice.compareTo(low) >= 0;
        } else {
            // Penjual bersedia menjual dari orderPrice. Jika High >= orderPrice, order dapat terisi
            return orderPrice.compareTo(high) <= 0;
        }
    }

    /**
     * Verifikasi Anti-Future Leak (FR-01.05):
     * Memastikan query historis hanya mengembalikan bar dengan tanggal <= currentDate.
     */
    public List<HistoricalBar> queryHistoricalBarsAntiLeak(
            List<HistoricalBar> bars,
            LocalDate currentSimDate) {

        List<HistoricalBar> result = new ArrayList<>();
        for (HistoricalBar b : bars) {
            if (!b.getDate().isAfter(currentSimDate)) {
                result.add(b);
            }
        }
        return result;
    }
}
