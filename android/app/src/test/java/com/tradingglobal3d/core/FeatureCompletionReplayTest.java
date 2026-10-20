package com.tradingglobal3d.core;

import com.tradingglobal3d.core.market.HistoricalBar;
import com.tradingglobal3d.core.market.HistoricalDatasetLoader;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.replay.HistoricalReplayEngine;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class FeatureCompletionReplayTest {
    @After public void resetAssetSource() { HistoricalDatasetLoader.setAssetSource(null); }

    private List<HistoricalBar> realBars() throws IOException { return HistoricalDatasetLoader.loadDataset("US:AAPL"); }

    private HistoricalReplayEngine engine() throws IOException {
        HistoricalReplayEngine engine = new HistoricalReplayEngine();
        engine.startReplay("US:AAPL", realBars());
        return engine;
    }

    private Order buy(String id, double limit) {
        return Order.createLimit(id, "US:AAPL", "QA", OrderSide.BUY, Money.of(limit, Currency.USD), 1, 0);
    }

    @Test public void verifiedDailyDatasetLoadsThroughNativeAssetOpenerAndChecksBytes() throws Exception {
        byte[] bytes = Files.readAllBytes(HistoricalDatasetLoader.findDatasetFile(HistoricalDatasetLoader.VERIFIED_PATH).toPath());
        HistoricalDatasetLoader.setAssetSource(path -> {
            assertEquals(HistoricalDatasetLoader.VERIFIED_PATH, path);
            return new ByteArrayInputStream(bytes);
        });
        List<HistoricalBar> bars = HistoricalDatasetLoader.loadDataset(" aapl ");
        assertEquals(61, bars.size());
        assertEquals(LocalDate.of(2016, 5, 9), bars.get(0).getDate());
        assertEquals(new BigDecimal("93.0"), bars.get(0).getOpen());
        assertEquals(LocalDate.of(2016, 8, 3), bars.get(60).getDate());
        assertEquals(new BigDecimal("105.79"), bars.get(60).getClose());
        HistoricalDatasetLoader.setAssetSource(path -> new ByteArrayInputStream("tampered".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        assertThrows(IOException.class, () -> HistoricalDatasetLoader.loadDataset("US:AAPL"));
        assertThrows(IllegalArgumentException.class, () -> HistoricalDatasetLoader.loadDataset("IDX:BBCA"));
    }

    @Test public void currentCandleIsUnavailableUntilCloseAndGapsDoNotConsumeRows() throws Exception {
        HistoricalReplayEngine engine = engine();
        LocalDate first = LocalDate.of(2016, 5, 9);
        assertTrue(engine.getVisibleBars().isEmpty());
        assertTrue(engine.getVisibleBars(first, LocalTime.of(9, 0)).isEmpty());
        assertTrue(engine.getVisibleBars(first, LocalTime.of(15, 59)).isEmpty());
        assertEquals(1, engine.getVisibleBars(first, LocalTime.of(16, 0)).size());
        assertNull(engine.getObservablePrice(first, LocalTime.of(8, 59)));
        assertEquals(new BigDecimal("93.0"), engine.getObservablePrice(first, LocalTime.of(9, 0)));
        assertEquals(new BigDecimal("92.79"), engine.getObservablePrice(first, LocalTime.of(16, 0)));
        engine.synchronizeToDate(LocalDate.of(2016, 5, 13));
        int friday = engine.getCurrentBarIndex();
        assertFalse(engine.synchronizeToDate(LocalDate.of(2016, 5, 14)));
        assertFalse(engine.synchronizeToDate(LocalDate.of(2016, 5, 15)));
        assertEquals(friday, engine.getCurrentBarIndex());
        assertNull(engine.executablePrice(buy("WEEKEND", 100), LocalDate.of(2016, 5, 15), LocalTime.NOON));
        engine.synchronizeToDate(LocalDate.of(2016, 5, 16));
        assertEquals(LocalDate.of(2016, 5, 16), engine.getCurrentBar().getDate());
    }

    @Test public void dailyFillUsesKnownOpeningPriceNeverFutureHighLowOrAnotherSymbol() throws Exception {
        HistoricalReplayEngine engine = engine();
        LocalDate date = engine.getCurrentBar().getDate();
        assertNull(engine.executablePrice(buy("LOW-CHEAT", 92.60), date, LocalTime.NOON));
        assertEquals(new BigDecimal("93.0"), engine.executablePrice(buy("OPEN", 94), date, LocalTime.NOON));
        Order unrelated = Order.createLimit("WRONG", "IDX:BBCA", "QA", OrderSide.BUY, Money.of(10000, Currency.IDR), 100, 0);
        assertNull(engine.executablePrice(unrelated, date, LocalTime.NOON));
        assertNull(engine.executablePrice(buy("CLOSED", 94), date, LocalTime.of(16, 0)));
        engine.synchronizeToDate(LocalDate.of(2016, 8, 3));
        assertFalse(engine.isReplayCompleted(LocalDate.of(2016, 8, 3), LocalTime.NOON));
        assertTrue(engine.isReplayCompleted(LocalDate.of(2016, 8, 3), LocalTime.of(16, 0)));
    }

    @Test public void saveRestoreChecksVersionAndBoundsWithoutSilentlyLosingExistingState() throws Exception {
        HistoricalReplayEngine engine = engine();
        engine.synchronizeToDate(LocalDate.of(2016, 6, 10));
        JSONObject saved = engine.exportState();
        HistoricalReplayEngine restored = new HistoricalReplayEngine();
        restored.restoreState(saved);
        assertEquals(engine.getCurrentBarIndex(), restored.getCurrentBarIndex());
        assertEquals(engine.getCurrentBar().getDate(), restored.getCurrentBar().getDate());
        int oldIndex = restored.getCurrentBarIndex();
        JSONObject bad = new JSONObject(saved.toString()).put("currentBarIndex", -1);
        assertThrows(IllegalArgumentException.class, () -> restored.restoreState(bad));
        assertEquals(oldIndex, restored.getCurrentBarIndex());
        assertThrows(IllegalArgumentException.class, () -> restored.restoreState(new JSONObject(saved.toString()).put("currentBarIndex", 999)));
        assertThrows(IllegalArgumentException.class, () -> restored.restoreState(new JSONObject(saved.toString()).put("datasetSha256", "old-unverified")));
        assertEquals(oldIndex, restored.getCurrentBarIndex());
    }

    @Test public void csvRejectsFractionalOrOverflowVolumeAndMissingReorderedColumns() {
        String header = "Date,Open,High,Low,Close,Volume\n2016-05-09,93,94,92,93,";
        assertThrows(ArithmeticException.class, () -> HistoricalDatasetLoader.loadFromCsvString(header + "1.5"));
        assertThrows(ArithmeticException.class, () -> HistoricalDatasetLoader.loadFromCsvString(header + "9999999999999999999999"));
        assertThrows(IOException.class, () -> HistoricalDatasetLoader.loadFromCsvString("Date,Close,Volume,Open,High,Low\n2016-05-09,93,4,93"));
    }

    @Test public void tenSeededHundredActionSequencesKeepDatesVisibilityAndRestoreConsistent() throws Exception {
        List<HistoricalBar> bars = realBars();
        for (int seed = 0; seed < 10; seed++) {
            Random random = new Random(21000 + seed);
            HistoricalReplayEngine engine = engine();
            LocalDate date = bars.get(0).getDate();
            for (int action = 0; action < 100; action++) {
                switch (random.nextInt(4)) {
                    case 0: date = date.plusDays(random.nextInt(3)); engine.synchronizeToDate(date); break;
                    case 1: HistoricalReplayEngine reloaded = new HistoricalReplayEngine(); reloaded.restoreState(engine.exportState()); engine = reloaded; break;
                    case 2: engine.executablePrice(buy("QA-" + seed + "-" + action, random.nextBoolean() ? 1 : 10000), date, LocalTime.NOON); break;
                    default: engine.getObservablePrice(date, LocalTime.NOON); break;
                }
                assertFalse(engine.getCurrentBar().getDate().isAfter(date));
                for (HistoricalBar visible : engine.getVisibleBars(date, LocalTime.NOON)) assertTrue(visible.getDate().isBefore(date));
                HistoricalBar expected = bars.get(0);
                for (HistoricalBar bar : bars) if (!bar.getDate().isAfter(date)) expected = bar;
                assertEquals(expected.getDate(), engine.getCurrentBar().getDate());
            }
        }
    }
}
