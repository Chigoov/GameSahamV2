package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;
import com.tradingglobal3d.core.trading.OrderType;
import com.tradingglobal3d.core.research.MarketEvent;
import com.tradingglobal3d.core.research.NewsArticle;
import com.tradingglobal3d.core.research.NewsPriceTarget;
import com.tradingglobal3d.core.research.ResearchBooster;
import com.tradingglobal3d.core.research.ResearchService;
import com.tradingglobal3d.core.research.TerminalTier;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Collections;

public class PackageP04NewsAndTerminalTest {

    @Test
    public void testAU24_ProceduralNewsDatesClearedOnResetForGame() {
        ResearchService rs = new ResearchService();
        rs.setDynamicDailyNewsEnabled(true);
        InstrumentCatalog cat = new InstrumentCatalog();
        Instrument inst = new Instrument("IDX:TEST", "TEST", "Test", MarketExchange.IDX, "Finance", Currency.IDR, 100, 1000000L, true, Money.of(1000L, Currency.IDR));
        cat.registerInstrument(inst);

        long t0 = 1704067200000L; // 2024-01-01
        LocalDate d1 = LocalDate.of(2024, 1, 1);
        rs.ensureDailyNewsSchedule(d1, cat, t0);
        int newsCount1 = rs.getNewsList().size();
        Assert.assertTrue(newsCount1 > 0);

        // Reset for new game run
        rs.resetForGame(t0 + 86400000L);
        rs.setDynamicDailyNewsEnabled(true);
        // After reset, schedule same date again -> should regenerate properly because cache was cleared
        rs.ensureDailyNewsSchedule(d1, cat, t0 + 86400000L);
        int newsCount2 = rs.getNewsList().size();
        Assert.assertTrue("Reset for game must clear procedural news date cache and allow generation in new game!", newsCount2 > 0);
    }

    @Test
    public void testAU25_QuarterlyMacroShiftUsesCapturedClosingReturnBeforeRollover() throws Exception {
        File testDir = new File("build/tmp-test-p04-au25");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));

        // Start on end of Q4 (Dec 31, 2026) so next morning is Jan 1, 2027 (Quarterly Macro Evaluation Day)
        facade.initializeNewGame(com.tradingglobal3d.core.model.GameMode.INTERNAL_MARKET, new BigDecimal("50000000"),
                new com.tradingglobal3d.core.clock.GameClock(LocalDate.of(2026, 12, 31), java.time.LocalTime.of(8, 30, 0)));

        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        Assert.assertNotNull(bbca);

        // Simulate high return on end of Q1 (e.g. March 31): +5% return
        bbca.updatePrice(Money.of(10500L, Currency.IDR), 1000); // 10000 -> 10500 (+5.0%)
        int macroBefore = facade.getMacroIndex();

        // Advance day to next day (crossing midnight rollover)
        facade.advanceWorldSecondsForTest(86400);

        int macroAfter = facade.getMacroIndex();
        Assert.assertEquals("Macro index must step up when previous closing return was > 1.5%!", macroBefore + 1, macroAfter);
    }

    @Test
    public void testAU26_WhaleNewsTiedToEconomicMarketEventWithPersistentId() {
        ResearchService rs = new ResearchService();
        rs.setDynamicDailyNewsEnabled(true);
        InstrumentCatalog cat = new InstrumentCatalog();
        Instrument inst = new Instrument("IDX:BBCA", "BBCA", "Bank BCA", MarketExchange.IDX, "Finance", Currency.IDR, 100, 1000000L, true, Money.of(10000L, Currency.IDR));
        cat.registerInstrument(inst);

        // Day in 14 == 2: epochDay = 2 -> 1970-01-03
        LocalDate dWhale = LocalDate.ofEpochDay(2);
        long t = dWhale.toEpochDay() * 86400000L;
        rs.resetForGame(t);
        rs.setDynamicDailyNewsEnabled(true);
        MarketEvent whaleEvt = new MarketEvent("EVT-WHALE-01", "Akuisisi Perbankan", "Konsolidasi perbankan", "Finance",
                MarketEvent.EventImpact.VERY_POSITIVE, t, true);
        rs.addMarketEvent(whaleEvt);
        rs.ensureDailyNewsSchedule(dWhale, cat, t);

        NewsArticle whaleNews = null;
        for (NewsArticle a : rs.getNewsList()) {
            if (a.getId().startsWith("NEWS-WHALE-")) {
                whaleNews = a;
                break;
            }
        }
        Assert.assertNotNull("Whale news must be generated on day 2", whaleNews);
        Assert.assertNotNull("Whale news must have a linked economicEventId!", whaleNews.getEconomicEventId());
        Assert.assertFalse("Linked event ID must not be empty", whaleNews.getEconomicEventId().isEmpty());

        // Check marketEvents
        boolean foundInMarketEvents = false;
        for (MarketEvent me : rs.getMarketEvents()) {
            if (me.getEventId().equals(whaleNews.getEconomicEventId())) {
                foundInMarketEvents = true;
                break;
            }
        }
        Assert.assertTrue("Linked event must exist in marketEvents ledger!", foundInMarketEvents);
    }

    @Test
    public void testAU33_NewsCategorySentimentDistributionCoversBothBullAndBear() {
        ResearchService rs = new ResearchService();
        rs.setDynamicDailyNewsEnabled(true);
        InstrumentCatalog cat = new InstrumentCatalog();
        Instrument inst = new Instrument("IDX:BBCA", "BBCA", "Bank BCA", MarketExchange.IDX, "Finance", Currency.IDR, 100, 1000000L, true, Money.of(10000L, Currency.IDR));
        cat.registerInstrument(inst);

        int companyBull = 0;
        int companyBear = 0;
        int globalBull = 0;
        int globalBear = 0;

        // Sample across 200 different dates
        LocalDate cur = LocalDate.of(2025, 1, 1);
        for (int i = 0; i < 200; i++) {
            rs.resetForGame(cur.toEpochDay() * 86400000L);
            rs.setDynamicDailyNewsEnabled(true);
            rs.generateDailyProceduralNews(cur, cur.toEpochDay() * 86400000L, cat, 5);
            for (NewsArticle a : rs.getNewsList()) {
                if (a.getId().startsWith("NEWS-AUTO-")) {
                    if (a.getCategory() == NewsArticle.Category.COMPANY) {
                        if (a.getSentimentImpact() > 0) companyBull++;
                        else companyBear++;
                    } else if (a.getHeadline().contains("Bursa Global") || a.getHeadline().contains("Wall St")) {
                        if (a.getSentimentImpact() > 0) globalBull++;
                        else globalBear++;
                    }
                }
            }
            cur = cur.plusDays(1);
        }

        System.out.println("AU33 Distribution - Company: bull=" + companyBull + ", bear=" + companyBear + " | Global: bull=" + globalBull + ", bear=" + globalBear);
        Assert.assertTrue("Company earnings news must produce both bullish and bearish articles!", companyBull > 0 && companyBear > 0);
        Assert.assertTrue("Global market news must produce both bullish and bearish articles!", globalBull > 0 && globalBear > 0);
    }

    @Test
    public void testAU34_BoosterPersistsAndConsumesUsesOnOrderFills() throws Exception {
        File testDir = new File("build/tmp-test-p04-au34");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        ResearchService rs = facade.getResearchService();
        ResearchBooster feeBooster = null;
        for (ResearchBooster b : rs.getBoosters()) {
            if (b.getType() == ResearchBooster.BoosterType.FEE_DISCOUNT) {
                feeBooster = b;
                break;
            }
        }
        Assert.assertNotNull(feeBooster);
        feeBooster.activate();
        int initialUses = feeBooster.getRemainingUses();
        Assert.assertTrue(initialUses > 0);

        // Advance to 09:05 so IDX market is open and generate liquidity
        facade.advanceWorldSecondsForTest(35 * 60);
        facade.generateNpcLiquidity();

        // Submit a buy order that fills immediately
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        BigDecimal lowestAsk = facade.getMarketEngine().getOrCreateBook(bbca).getAsks().keySet().iterator().next();
        Order buy = new Order("ORD-TEST-BOOST", "REQ-BOOST-1", facade.getPersonalAccount().getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT, new Money(lowestAsk, Currency.IDR), null, 100L,
                facade.getClock().getGameTimestamp(), Money.of(2000000L, Currency.IDR));
        facade.submitOrder(buy);

        Assert.assertEquals("Fee discount booster must consume one use upon order fill!", initialUses - 1, feeBooster.getRemainingUses());

        // Test persistence of booster remaining uses across save-load
        facade.save();

        SaveManager sm2 = SaveManager.forDirectory(testDir);
        TradingFacade facade2 = new TradingFacade(sm2);
        facade2.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade2.startOrResume();

        ResearchBooster restoredBooster = null;
        for (ResearchBooster b : facade2.getResearchService().getBoosters()) {
            if (b.getType() == ResearchBooster.BoosterType.FEE_DISCOUNT) {
                restoredBooster = b;
                break;
            }
        }
        Assert.assertNotNull(restoredBooster);
        Assert.assertTrue("Restored booster must remain active!", restoredBooster.isActive());
        Assert.assertEquals("Restored booster must retain decremented uses!", initialUses - 1, restoredBooster.getRemainingUses());
    }

    @Test
    public void testAU35_UnpublishedNewsAndBasicFeedDoNotLeakTargetsOrContent() throws Exception {
        ResearchService rs = new ResearchService();
        long publishedTime = 1704070800000L; // 09:00
        long announcedTime = publishedTime - 3600000L; // 08:00
        long currentTime = announcedTime + 60000L; // 08:01 (unpublished!)

        NewsArticle article = new NewsArticle("NEWS-SECRET-01", "Rahasia Akuisisi", "Konten sangat rahasia",
                NewsArticle.Category.COMPANY, "BBCA", "Perbankan", "POSITIF", "+5.0%", 2, "TINGGI", 0.5,
                announcedTime, publishedTime);
        rs.addNews(article);

        Instrument inst = new Instrument("IDX:BBCA", "BBCA", "Bank BCA", MarketExchange.IDX, "Finance", Currency.IDR, 100, 1000000L, true, Money.of(10000L, Currency.IDR));
        NewsPriceTarget target = new NewsPriceTarget("TGT-SECRET", "NEWS-SECRET-01", "BBCA", TerminalTier.PRO,
                "Rahasia Akuisisi", false, 0.05, Money.of(10000L, Currency.IDR), inst, LocalDate.of(2024, 1, 1));
        rs.registerNewsTarget(target);

        // Fetch feed as BASIC tier at 08:01
        JSONArray feed = rs.getNewsFeed(currentTime);
        Assert.assertEquals(1, feed.length());
        JSONObject item = feed.getJSONObject(0);

        // AU35: Unpublished news headline and content must be sanitized, and targetInfo must NOT be leaked
        Assert.assertEquals("[Jadwal Rilis] Perbankan", item.getString("headline"));
        Assert.assertEquals("BELUM_DIRILIS", item.getString("direction"));
        Assert.assertEquals("Rahasia", item.getString("estimatedRange"));
        Assert.assertFalse("targetInfo must NOT be leaked on unpublished news to Basic tier!", item.has("targetInfo"));
    }

    @Test
    public void testAU36_AutoRenewalExtendsThirtyDaysWithoutMidnightGap() throws Exception {
        File testDir = new File("build/tmp-test-p04-au36");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        // Deposit USD via ledger mutation to keep ledger reconciliation intact
        facade.getLedger().recordMutation(
                "GRANT-USD-TEST",
                facade.getPersonalAccount(),
                com.tradingglobal3d.core.economy.LedgerEntryType.DEPOSIT,
                Money.of(1_000_000.0, Currency.USD),
                true,
                facade.getClock().getGameTimestamp(),
                "Initial USD Capital for Terminal Test"
        );

        // Promote to SENIOR_ANALYST to unlock PRO tier subscription
        JSONObject careerState = facade.getCareerManager().exportState();
        careerState.put("rank", "SENIOR_ANALYST");
        facade.getCareerManager().restoreState(careerState);

        // Subscribe to PRO tier
        boolean subOk = facade.subscribeTerminal(TerminalTier.PRO);
        Assert.assertTrue(subOk);
        ResearchService rs = facade.getResearchService();
        long initialExpiry = rs.getSubscriptionExpiresAt();

        // Advance time past subscription expiration in the middle of the trading day
        long currentTimestamp = initialExpiry + 3600000L; // 1 hour past expiration
        rs.processDailyAutoRenewal(facade.getPersonalAccount(), facade.getLedger(), currentTimestamp);

        // AU36: Should be auto-renewed, still PRO, and expiry extended by 30 days anchored from initialExpiry
        Assert.assertEquals(TerminalTier.PRO, rs.getCurrentTier());
        Assert.assertEquals("Expiry must be extended by 30 days anchored from initial expiry!",
                initialExpiry + (30L * 86400000L), rs.getSubscriptionExpiresAt());
    }

    @Test
    public void testAU54_FastForwardExecutesNewsCatalystAtMarketClose() throws Exception {
        File testDir = new File("build/tmp-test-p04-au54");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        // Register an active news target on BBCA: target +5% from 10000 to 10500
        NewsPriceTarget target = new NewsPriceTarget("TGT-AU54", "NEWS-AU54", "BBCA", TerminalTier.ENTERPRISE,
                "Whale Catalyst", false, 0.05, bbca.getCurrentPrice(), bbca, facade.getClock().getCurrentDate());
        facade.getResearchService().registerNewsTarget(target);

        // Clock starts at morning 08:30
        Money priceBefore = bbca.getCurrentPrice();

        // Fast forward 8 hours (28,800 seconds) crossing 16:00 market close
        facade.advanceWorldSecondsForTest(8 * 3600);

        // AU54: Essential news catalyst MUST execute at market close even during fast-forward!
        Money priceAfter = bbca.getCurrentPrice();
        System.out.println("AU54 - BBCA price before: " + priceBefore + ", after fast-forward: " + priceAfter);
        Assert.assertTrue("Price must move toward or reach catalyst target at market close during fast-forward!",
                priceAfter.compareTo(priceBefore) > 0);
    }
}
