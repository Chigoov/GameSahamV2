package com.tradingglobal3d.core;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.research.NewsArticle;
import com.tradingglobal3d.core.research.NewsPriceTarget;
import com.tradingglobal3d.core.research.ResearchService;
import com.tradingglobal3d.core.research.TerminalTier;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.MatchingEngine;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;
import com.tradingglobal3d.core.trading.OrderStatus;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * Pengujian Komprehensif Paket T01–T08 Master Plan Lanjutan:
 * - Batas Kenaikan/Penurunan Harian 25% (T03)
 * - Target Berita Pro/Whale Tersimpan & Terjamin Lintas Hari (T04)
 * - Frekuensi Berita & Antrean Konflik (T05)
 */
public class Plan14MasterPlanNewsAndPriceBandTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testDailyPriceBand25PercentOnAllStocks() {
        Money basePrice = Money.of(10250, Currency.IDR);
        Instrument bbca = new Instrument("IDX:BBCA", "BBCA", "Bank Central Asia",
                MarketExchange.IDX, "Keuangan", Currency.IDR, 100, 10_000_000L, true, basePrice);

        // 1. Batas Atas 25% (Ceiling): 10.250 * 1.25 = 12.812,5 -> floor ke tick 25 = Rp 12.800
        Money ceiling = bbca.getDailyCeilingPrice();
        assertEquals(Money.of(12800, Currency.IDR), ceiling);
        assertTrue("Ceiling tidak boleh melebihi 25%", ceiling.getAmount().doubleValue() <= 10250.0 * 1.25);

        // 2. Batas Bawah 25% (Floor): 10.250 * 0.75 = 7.687,5 -> ceil ke tick 25 = Rp 7.700
        Money floor = bbca.getDailyFloorPrice();
        assertEquals(Money.of(7700, Currency.IDR), floor);
        assertTrue("Floor tidak boleh di bawah -25%", floor.getAmount().doubleValue() >= 10250.0 * 0.75);

        // 3. Verifikasi isWithinDailyPriceBand dan batas harga (T03):
        assertTrue("Harga tepat ceiling harus dalam batas", bbca.isWithinDailyPriceBand(ceiling));
        assertTrue("Harga tepat floor harus dalam batas", bbca.isWithinDailyPriceBand(floor));
        assertFalse("Harga 1 tick di atas ceiling harus di luar batas", bbca.isWithinDailyPriceBand(Money.of(12825, Currency.IDR)));
        assertFalse("Harga 1 tick di bawah floor harus di luar batas", bbca.isWithinDailyPriceBand(Money.of(7675, Currency.IDR)));

        // 4. UpdatePrice clamping: updatePrice tidak boleh menghasilkan harga di luar [floor, ceiling] (T03)
        bbca.updatePrice(Money.of(15000, Currency.IDR), 100);
        assertEquals("updatePrice harus di-clamp ke batas ceiling 25%", ceiling, bbca.getCurrentPrice());

        bbca.updatePrice(Money.of(5000, Currency.IDR), 100);
        assertEquals("updatePrice harus di-clamp ke batas floor 25%", floor, bbca.getCurrentPrice());
    }

    @Test
    public void testTargetRoundingUpAndMultiDayDeadlineCalculation() {
        Money price10k = Money.of(10000, Currency.IDR);
        Instrument stock = new Instrument("IDX:TEST", "TEST", "Test Instrument",
                MarketExchange.IDX, "Keuangan", Currency.IDR, 100, 10_000_000L, true, price10k);

        // Skenario A: Target +30% dari Rp 10.000 -> Target Rp 13.000
        // Pergerakan harian maks 25%: Hari 1 maks 12.500, Hari 2 maks 15.625 (>= 13.000) -> 2 hari
        // Sesuai Butir 3: Jika secara matematis bisa dicapai <= 3 hari, tenggat wajib maksimal 3 hari
        NewsPriceTarget target30 = new NewsPriceTarget(
                "TGT-30", "NEWS-30", "TEST", TerminalTier.ENTERPRISE, "Katalis +30%", false,
                0.30, price10k, stock, LocalDate.of(2026, 11, 1)
        );
        assertEquals(Money.of(13000, Currency.IDR), target30.getTargetPrice());
        assertEquals(2, target30.getRequiredDays());
        assertEquals(3, target30.getEffectiveDeadlineDays());
        assertEquals(LocalDate.of(2026, 11, 4), target30.getDeadlineDate());
        assertNull("Target <= 3 hari tidak membutuhkan catatan perpanjangan", target30.getDynamicNote());

        // Skenario B: Target +100% dari Rp 10.000 -> Target Rp 20.000
        // Hari 1: 12.500
        // Hari 2: 15.625
        // Hari 3: 19.531 (< 20.000)
        // Hari 4: 24.414 (>= 20.000) -> butuh 4 hari bursa!
        NewsPriceTarget target100 = new NewsPriceTarget(
                "TGT-100", "NEWS-100", "TEST", TerminalTier.ENTERPRISE, "Katalis +100%", false,
                1.00, price10k, stock, LocalDate.of(2026, 11, 1)
        );
        assertEquals(Money.of(20000, Currency.IDR), target100.getTargetPrice());
        assertEquals(4, target100.getRequiredDays());
        assertEquals(4, target100.getEffectiveDeadlineDays());
        assertEquals(LocalDate.of(2026, 11, 5), target100.getDeadlineDate());
        assertNotNull("Target > 3 hari wajib menyajikan catatan perhitungan dinamis", target100.getDynamicNote());
        assertTrue(target100.getDynamicNote().contains("membutuhkan sedikitnya 4 hari bursa"));
        assertTrue(target100.getDynamicNote().contains("100.0%"));
    }

    @Test
    public void testConflictingNewsOnSameTickerUsesSignedNetTarget() {
        ResearchService service = new ResearchService();
        service.resetForGame(1792373400000L);

        Money refPrice = Money.of(10000, Currency.IDR);
        Instrument stockA = new Instrument("IDX:BBCA", "BBCA", "Bank Central Asia",
                MarketExchange.IDX, "Keuangan", Currency.IDR, 100, 10_000_000L, true, refPrice);
        Instrument stockB = new Instrument("IDX:ADRO", "ADRO", "Adaro Energy",
                MarketExchange.IDX, "Energi", Currency.IDR, 100, 10_000_000L, true, refPrice);

        InstrumentCatalog catalog = new InstrumentCatalog();
        catalog.registerInstrument(stockA);
        catalog.registerInstrument(stockB);

        // 1. Target 1 pada BBCA (+4%)
        NewsPriceTarget target1 = new NewsPriceTarget(
                "TGT-1", "N-1", "BBCA", TerminalTier.PRO, "Berita 1 BBCA", false,
                0.04, refPrice, stockA, LocalDate.of(2026, 11, 1)
        );
        service.registerNewsTarget(target1);
        assertEquals(NewsPriceTarget.TargetStatus.ACTIVE, target1.getStatus());

        // Q09: kedua efek emiten aktif bersamaan; +4% -2% menjadi +2%.
        NewsPriceTarget target2 = new NewsPriceTarget(
                "TGT-2", "N-2", "BBCA", TerminalTier.PRO, "Berita 2 BBCA Konflik", false,
                -0.02, refPrice, stockA, LocalDate.of(2026, 11, 1)
        );
        service.registerNewsTarget(target2);
        assertEquals(NewsPriceTarget.TargetStatus.ACTIVE, target2.getStatus());
        assertEquals(Money.of(10200, Currency.IDR), service.getActiveNewsTarget("BBCA").getTargetPrice());

        // 3. Berita pada emiten BERBEDA (ADRO) -> langsung ACTIVE (independen, tidak saling menunggu)
        NewsPriceTarget target3 = new NewsPriceTarget(
                "TGT-3", "N-3", "ADRO", TerminalTier.PRO, "Berita 3 ADRO", false,
                0.03, refPrice, stockB, LocalDate.of(2026, 11, 1)
        );
        service.registerNewsTarget(target3);
        assertEquals(NewsPriceTarget.TargetStatus.ACTIVE, target3.getStatus());

        // Selesaikan target efektif +2%, bukan target individual yang sudah dikombinasikan.
        stockA.updatePrice(Money.of(10200, Currency.IDR), 100);
        service.evaluateNewsTargets(catalog, LocalDate.of(2026, 11, 2));

        assertEquals(NewsPriceTarget.TargetStatus.ACHIEVED, target1.getStatus());
        // Kedua target mengacu pada hasil gabungan yang sama.
        assertEquals(NewsPriceTarget.TargetStatus.ACHIEVED, target2.getStatus());
    }

    @Test
    public void testWhaleFollowUpRumorExceptionActivatesImmediately() {
        ResearchService service = new ResearchService();
        service.resetForGame(1792373400000L);

        Money refPrice = Money.of(10000, Currency.IDR);
        Instrument stock = new Instrument("IDX:BBCA", "BBCA", "Bank Central Asia",
                MarketExchange.IDX, "Keuangan", Currency.IDR, 100, 10_000_000L, true, refPrice);

        // 1. Rumor awal Whale (+21%)
        NewsPriceTarget initialRumor = new NewsPriceTarget(
                "TGT-WHALE-1", "N-W1", "BBCA", TerminalTier.ENTERPRISE, "Rumor Awal Whale", false,
                0.21, refPrice, stock, LocalDate.of(2026, 11, 1)
        );
        service.registerNewsTarget(initialRumor);
        assertEquals(NewsPriceTarget.TargetStatus.ACTIVE, initialRumor.getStatus());

        // 2. Rumor lanjutan Whale (+42% = 2x rumor awal)
        // Merupakan pengecualian terhadap antrean: aktif seketika (isFollowUp = true)
        Money prevCloseBeforeFollowUp = Money.of(12000, Currency.IDR);
        stock.updatePrice(prevCloseBeforeFollowUp, 0);
        stock.startNewTradingDay();

        NewsPriceTarget followUpRumor = new NewsPriceTarget(
                "TGT-WHALE-2", "N-W2", "BBCA", TerminalTier.ENTERPRISE, "Rumor Lanjutan Whale", true,
                0.42, prevCloseBeforeFollowUp, stock, LocalDate.of(2026, 11, 2)
        );
        service.registerNewsTarget(followUpRumor);

        // Kedua target tercatat dan aktif
        assertEquals(NewsPriceTarget.TargetStatus.ACTIVE, followUpRumor.getStatus());
        assertEquals(2, service.getNewsTargets().size());
        assertEquals(0.42, followUpRumor.getTargetPercent(), 0.001);
    }

    @Test
    public void testDailyNewsScheduleFrequencyAndPersistence() throws Exception {
        SaveManager saves = SaveManager.forDirectory(temp.newFolder());
        TradingFacade game = new TradingFacade(saves);
        File catFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("src/main/assets/data/market_universe.json");
        game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(catFile.toPath())));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));

        ResearchService service = game.getResearchService();
        service.setDynamicDailyNewsEnabled(true);
        LocalDate day1 = LocalDate.of(2026, 11, 2);

        // Pastikan jadwal hari 1 diproses
        service.ensureDailyNewsSchedule(day1, game.getCatalog(), game.getClock().getGameTimestamp());

        // Harus ada tepat 2 berita Pro untuk hari tersebut
        long proCount = service.getNewsTargets().stream().filter(t -> t.getTier() == TerminalTier.PRO).count();
        assertTrue("Harus ada minimal 2 warta Pro per hari bursa", proCount >= 2);

        // Simpan state
        JSONObject exported = service.exportState();
        assertTrue("State harus memuat newsTargets", exported.has("newsTargets"));
        assertTrue(exported.getJSONArray("newsTargets").length() >= 2);

        // Pulihkan ke service baru
        ResearchService reloaded = new ResearchService();
        reloaded.restoreState(exported);
        assertEquals(service.getNewsTargets().size(), reloaded.getNewsTargets().size());
        assertEquals(service.getNewsTargets().get(0).getTicker(), reloaded.getNewsTargets().get(0).getTicker());
        assertEquals(service.getNewsTargets().get(0).getTargetPrice(), reloaded.getNewsTargets().get(0).getTargetPrice());
    }
}
