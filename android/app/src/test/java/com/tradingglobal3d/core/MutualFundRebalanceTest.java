package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.funds.MutualFund;
import com.tradingglobal3d.core.funds.MutualFundManager;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import static org.junit.Assert.*;

/**
 * Tes integrasi reksa dana: backing ketiga strategi dan penanganan kegagalan rebalance.
 * Menangkap bug AUD21-04: advanceDay inert untuk MM/FI, rebalance exception ditelan.
 */
public class MutualFundRebalanceTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    private TradingFacade game() throws Exception {
        TradingFacade game = new TradingFacade(SaveManager.forDirectory(temporary.newFolder()));
        game.setCatalog(InstrumentCatalog.fromJsonString(
                new String(Files.readAllBytes(Paths.get(
                        "src/main/assets/data/market_universe.json")), StandardCharsets.UTF_8)));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        game.getClock().play();
        return game;
    }

    @Test
    public void moneyMarketNavGrowsAfterAdvanceDay() throws Exception {
        TradingFacade game = game();
        MutualFundManager mgr = game.getMutualFundManager();
        MutualFund mm = mgr.getFund("FUND-MM-01");
        assertNotNull("FUND-MM-01 harus ada", mm);

        JSONObject sub = game.subscribeMutualFund("test-mm-sub", "FUND-MM-01",
                Money.of(5_000_000, Currency.IDR));
        assertTrue("Subskripsi MM harus sukses: " + sub.optString("error", ""),
                sub.getBoolean("success"));

        for (int i = 0; i < 5; i++) game.skipDay("mm-day-" + i);
        BigDecimal navBefore = mm.getNavPerUnit();

        for (int i = 5; i < 15; i++) game.skipDay("mm-adv-" + i);
        BigDecimal navAfter = mm.getNavPerUnit();

        assertTrue("NAB MM harus >= setelah accrual (" + navBefore + " -> " + navAfter + ")",
                navAfter.compareTo(navBefore) >= 0);
    }

    @Test
    public void fixedIncomeNavGrowsAfterAdvanceDay() throws Exception {
        TradingFacade game = game();
        MutualFundManager mgr = game.getMutualFundManager();
        MutualFund fi = mgr.getFund("FUND-FI-01");
        assertNotNull("FUND-FI-01 harus ada", fi);

        JSONObject sub = game.subscribeMutualFund("test-fi-sub", "FUND-FI-01",
                Money.of(5_000_000, Currency.IDR));
        assertTrue("Subskripsi FI harus sukses: " + sub.optString("error", ""),
                sub.getBoolean("success"));

        for (int i = 0; i < 5; i++) game.skipDay("fi-day-" + i);
        BigDecimal navBefore = fi.getNavPerUnit();

        for (int i = 5; i < 15; i++) game.skipDay("fi-adv-" + i);
        BigDecimal navAfter = fi.getNavPerUnit();

        assertTrue("NAB FI harus >= setelah accrual (" + navBefore + " -> " + navAfter + ")",
                navAfter.compareTo(navBefore) >= 0);
    }

    @Test
    public void equityIndexBackingModelIsCanonical() throws Exception {
        TradingFacade game = game();
        MutualFundManager mgr = game.getMutualFundManager();
        MutualFund eq = mgr.getFund("FUND-EQ-01");
        assertNotNull("FUND-EQ-01 harus ada", eq);

        JSONObject sub = game.subscribeMutualFund("test-eq-sub", "FUND-EQ-01",
                Money.of(5_000_000, Currency.IDR));
        assertTrue("Subskripsi EQ harus sukses: " + sub.optString("error", ""),
                sub.getBoolean("success"));

        for (int i = 0; i < 5; i++) game.skipDay("eq-day-" + i);

        JSONObject status = eq.toJsonObject("PLAYER");
        assertEquals("CANONICAL_EQUITY_POSITIONS", status.getString("backingModel"));
    }

    @Test
    public void mmAndFiBackingModelIsUpdated() throws Exception {
        TradingFacade game = game();
        MutualFundManager mgr = game.getMutualFundManager();

        JSONObject mmStatus = mgr.getFund("FUND-MM-01").toJsonObject("PLAYER");
        assertEquals("CASH_ACCRUAL_DAILY_YIELD", mmStatus.getString("backingModel"));

        JSONObject fiStatus = mgr.getFund("FUND-FI-01").toJsonObject("PLAYER");
        assertEquals("CASH_ACCRUAL_DAILY_YIELD", fiStatus.getString("backingModel"));
    }

    @Test
    public void rebalanceFailureIsRecordedAndQueryable() throws Exception {
        TradingFacade game = game();
        MutualFundManager mgr = game.getMutualFundManager();

        assertNull("Sebelum kegagalan, pesan harus null", mgr.getLastRebalanceFailure());
        assertEquals(0L, mgr.getLastRebalanceFailureTimestamp());

        mgr.recordRebalanceFailure("Kegagalan rebalance simulasi", 9_999_999L);

        assertEquals("Kegagalan rebalance simulasi", mgr.getLastRebalanceFailure());
        assertEquals(9_999_999L, mgr.getLastRebalanceFailureTimestamp());
    }

    @Test
    public void fundNavSurvivesSaveAndReload() throws Exception {
        TradingFacade game = game();

        game.subscribeMutualFund("persist-sub", "FUND-MM-01",
                Money.of(5_000_000, Currency.IDR));
        for (int i = 0; i < 8; i++) game.skipDay("persist-day-" + i);

        BigDecimal navBefore = game.getMutualFundManager().getFund("FUND-MM-01").getNavPerUnit();

        game.save();
        assertTrue(game.load());

        BigDecimal navAfter = game.getMutualFundManager().getFund("FUND-MM-01").getNavPerUnit();
        assertEquals("NAB harus bertahan setelah save/reload", 0, navBefore.compareTo(navAfter));
    }
}
