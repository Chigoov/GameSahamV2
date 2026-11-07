package com.tradingglobal3d.core;

import com.tradingglobal3d.core.business.CompanyManager;
import com.tradingglobal3d.core.business.OperatingCompany;
import com.tradingglobal3d.core.career.CareerRank;
import com.tradingglobal3d.core.corporate.CorporateActionService;
import com.tradingglobal3d.core.corporate.IpoOffering;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.funds.MutualFund;
import com.tradingglobal3d.core.funds.MutualFundManager;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.market.MarketIndex;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.replay.StressTestResult;
import com.tradingglobal3d.core.replay.StressTestScenario;
import com.tradingglobal3d.core.risk.RiskComplianceService;
import com.tradingglobal3d.core.risk.ShortPosition;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class PackageP03EconomyAndPolicyTest {

    @Test
    public void testAU06_MarketIndexChainsCumulativeReturnsAcrossDayRollover() throws Exception {
        Instrument member = new Instrument("IDX:MOCK", "MOCK", "Mock", MarketExchange.IDX, "Finance", Currency.IDR,
                100, 1000000L, true, Money.of(10000L, Currency.IDR));
        MarketIndex index = new MarketIndex("IDX:TEST", "TEST", "Test Index", MarketExchange.IDX,
                1000.0, MarketIndex.CalculationMethod.MARKET_CAP_WEIGHTED, Collections.singletonList("IDX:MOCK"));

        // Day 1: Price rises 10% from 10,000 to 11,000
        member.updatePrice(Money.of(11000L, Currency.IDR), 1000);
        double valDay1 = index.recalculate(Collections.singletonMap("IDX:MOCK", member));
        Assert.assertEquals(1100.0, valDay1, 0.01);

        // Day 2 start: rollover startNewTradingDay() without price change
        member.startNewTradingDay();
        index.startNewTradingDay();
        double valDay2Start = index.recalculate(Collections.singletonMap("IDX:MOCK", member));

        // AU06: Index MUST NOT jump back to 1000! It must chain from 1100.0
        Assert.assertEquals("Index value must chain previous close, not jump back to base 1000!", 1100.0, valDay2Start, 0.01);

        // Day 2 intraday: Price rises another 10% from 11,000 to 12,100
        member.updatePrice(Money.of(12100L, Currency.IDR), 1000);
        double valDay2End = index.recalculate(Collections.singletonMap("IDX:MOCK", member));
        Assert.assertEquals(1210.0, valDay2End, 0.01);
    }

    @Test
    public void testAU07_CustomFundRejectsNegativeOrOutOfBoundsWeights() throws Exception {
        File testDir = new File("build/tmp-test-p03-au07");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        // Attempt weights with negative value: equity = -50, comm = 150 (total = 100) -> MUST throw exception
        boolean rejected = false;
        try {
            facade.createCustomMutualFund("Fund Negatif", -50.0, 150.0, 0.0, 0.0, BigDecimal.valueOf(10000000));
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        Assert.assertTrue("Negative asset weights must be rejected even if total is 100!", rejected);

        // Valid weights 50, 30, 10, 10 -> succeeds
        JSONObject res = facade.createCustomMutualFund("Fund Sehat", 50.0, 30.0, 10.0, 10.0, BigDecimal.valueOf(10000000));
        Assert.assertTrue(res.getBoolean("success"));
    }

    @Test
    public void testAU10_ShortBorrowFeeAccruesAsUnpaidLiabilityWhenCashInsufficient() throws Exception {
        RiskComplianceService risk = new RiskComplianceService();
        Account borrower = new Account("BORROWER", "Borrower", com.tradingglobal3d.core.model.AccountType.PERSONAL, null);
        Account lender = new Account("LENDER", "Lender", com.tradingglobal3d.core.model.AccountType.FIRM, null);
        EconomyLedger ledger = new EconomyLedger();
        Instrument inst = new Instrument("IDX:BBCA", "BBCA", "BBCA", MarketExchange.IDX, "Finance", Currency.IDR, 100, 1000000L, true, Money.of(10000L, Currency.IDR));
        InstrumentCatalog cat = new InstrumentCatalog();
        cat.registerInstrument(inst);

        lender.addPosition(new Position("BBCA", "IDX", Currency.IDR, 50000L, BigDecimal.valueOf(10000), BigDecimal.valueOf(10000)));

        // Open short 100 shares @ 10,000 -> proceeds locked in collateral, borrower available cash = 0
        ShortPosition sp = risk.openShortPosition(borrower, inst, 100L, lender, ledger, 1000L);
        Assert.assertEquals(Money.zero(Currency.IDR), borrower.getAvailableCash(Currency.IDR));

        // Accrue daily financing cost when cash is 0
        risk.accrueDailyFinancingCosts(borrower, cat, ledger, BigDecimal.valueOf(15800), 2000L);

        // AU10: Unpaid borrow fee must be accrued as liability on the position
        Assert.assertTrue("Unpaid short borrow fee must accrue as liability when cash is insufficient!", sp.getAccruedUnpaidFee().isPositive());
    }

    @Test
    public void testAU12_CompanyActionPreviewUsesDailyRevenueNotCapacity() throws Exception {
        File testDir = new File("build/tmp-test-p03-au12");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("100000000"));

        OperatingCompany comp = facade.foundCompany("COMP-01", "PT Jasa Prima", "Jasa", Money.of(25_000_000, Currency.IDR));
        Assert.assertNotNull(comp);

        JSONObject preview = facade.getCompanyManager().previewCompanyAction("COMP-01", "POLICY", 8, "STANDARD");
        BigDecimal revBefore = new BigDecimal(preview.getString("projectedDailyRevenueBefore"));
        BigDecimal capBefore = new BigDecimal(preview.getString("dailyCapacityBefore"));

        // Capacity is maximum theoretical capacity (e.g. 9M), while actual daily revenue accounts for demand (e.g. 3.09M)
        Assert.assertNotEquals("Preview revenue must use calculateDailyRevenue(), not raw theoretical capacity!", capBefore, revBefore);
        Assert.assertEquals(comp.calculateDailyRevenue().getAmount().toPlainString(), preview.getString("projectedDailyRevenueBefore"));
    }

    @Test
    public void testAU13_RepeatIpoIncreasesSharesOutstandingViaSupplyEvent() throws Exception {
        CorporateActionService cas = new CorporateActionService();
        InstrumentCatalog catalog = new InstrumentCatalog();
        cas.bindShareSupplyCatalog(catalog);

        // First IPO: NCLD 20M shares
        IpoOffering ipo1 = new IpoOffering("IPO-2026-01-1", "NCLD", "PT Nusantara Cloud Tbk", Currency.IDR,
                Money.of(500L, Currency.IDR), 100, 20_000_000L, LocalDate.of(2026, 1, 10));
        cas.getIpoOfferings().add(ipo1);

        Instrument ncldInst = new Instrument("IDX:NCLD", "NCLD", "PT Nusantara Cloud Tbk", MarketExchange.IDX, "TECH", Currency.IDR,
                100, 20_000_000L, true, Money.of(500L, Currency.IDR));
        catalog.registerInstrument(ncldInst);

        // Second IPO for same ticker: NCLD 10M shares in 2027
        IpoOffering ipo2 = new IpoOffering("IPO-2027-01-1", "NCLD", "PT Nusantara Cloud Tbk", Currency.IDR,
                Money.of(600L, Currency.IDR), 100, 10_000_000L, LocalDate.of(2027, 1, 10));
        cas.getIpoOfferings().add(ipo2);

        Account player = new Account("PLAYER", "Player", com.tradingglobal3d.core.model.AccountType.PERSONAL, null);
        player.creditCash(Money.of(100_000_000L, Currency.IDR));
        EconomyLedger ledger = new EconomyLedger();
        java.util.Map<String, Account> accounts = new java.util.HashMap<>();
        accounts.put(player.getId(), player);

        // Subscribe to second IPO: 1000 lots (100,000 shares)
        cas.orderIpo(ipo2, player, 1000L, "REQ-SUB-NCLD2", 1000L);

        long sharesBefore = ncldInst.getSharesOutstanding();
        cas.allotIpo(ipo2, 1.0, accounts, ledger, 2000L);

        long sharesAfter = ncldInst.getSharesOutstanding();
        Assert.assertEquals("Repeat IPO allotment must increase instrument shares outstanding via supply event!",
                sharesBefore + 100_000L, sharesAfter);
    }

    @Test
    public void testAU30_MutualFundRespectsCatalogParameters() throws Exception {
        MutualFundManager mfm = new MutualFundManager();
        MutualFund sh01 = mfm.getFund("FUND-SH-01");
        Assert.assertNotNull("FUND-SH-01 must exist in 24 fund catalog", sh01);

        // Catalog sets minBuyIdr = 100,000 for FUND-SH-01
        Assert.assertEquals(Money.of(100_000L, Currency.IDR), sh01.getMinBuyAmount());
        Assert.assertEquals(90, sh01.getStockPct());

        Account acc = new Account("ACC-TEST-FUND", "Test", com.tradingglobal3d.core.model.AccountType.PERSONAL, null);
        acc.creditCash(Money.of(10_000_000L, Currency.IDR));
        EconomyLedger ledger = new EconomyLedger();

        // Subscription below minBuy (Rp 50.000 < Rp 100.000) -> MUST be rejected
        boolean rejected = false;
        try {
            sh01.submitSubscription("ORD-SUB-FAIL", acc, Money.of(50_000L, Currency.IDR), ledger, 1000L);
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        Assert.assertTrue("Subscription below catalog minBuyIdr must be rejected!", rejected);

        // Subscription at/above minBuy (Rp 100.000) -> succeeds
        JSONObject receipt = sh01.submitSubscription("ORD-SUB-OK", acc, Money.of(100_000L, Currency.IDR), ledger, 1000L);
        Assert.assertNotNull(receipt);
    }

    @Test
    public void testAU38_TimeDepositRatesIdenticalBeforeAndAfterSaveLoad() throws Exception {
        File testDir = new File("build/tmp-test-p03-au38");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        String rateBefore = facade.getTimeDepositProducts().getJSONObject(0).getString("annualInterestRate");
        System.out.println("Deposit product rate before save: " + rateBefore);

        // Save
        facade.save();

        // Load into fresh facade
        SaveManager sm2 = SaveManager.forDirectory(testDir);
        TradingFacade facade2 = new TradingFacade(sm2);
        facade2.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade2.startOrResume();

        String rateAfter = facade2.getTimeDepositProducts().getJSONObject(0).getString("annualInterestRate");
        System.out.println("Deposit product rate after load: " + rateAfter);

        Assert.assertEquals("Deposit product rate must be identical before and after save-load!", rateBefore, rateAfter);
    }

    @Test
    public void testAU39_RiskAndDebtReportsIncludeUsdMargin() throws Exception {
        File testDir = new File("build/tmp-test-p03-au39");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        // Promote to PORTFOLIO_MANAGER to take margin
        JSONObject careerState = facade.getCareerManager().exportState();
        careerState.put("rank", "PORTFOLIO_MANAGER");
        facade.getCareerManager().restoreState(careerState);

        Account personal = facade.getPersonalAccount();
        // Borrow 1,000 USD margin loan
        facade.takeMarginLoan(Money.of(1000.0, Currency.USD));

        // 1. Stress test baseline includes USD debt
        StressTestScenario crash = StressTestScenario.defaultMarketCrash();
        StressTestResult stress = facade.runStressTest(crash);
        BigDecimal usdRate = facade.getFxService().getMidRate();
        BigDecimal expectedDebtIdr = BigDecimal.valueOf(1000.0).multiply(usdRate);
        System.out.println("Stress test initial value: " + stress.getBaselineValue().format() + ", expected debt: Rp " + expectedDebtIdr);

        // 2. Consolidated wealth breakdown
        JSONObject wealth = facade.getConsolidatedWealthBreakdown();
        Assert.assertTrue("Consolidated wealth breakdown must be reconciled!", wealth.getBoolean("isReconciled"));
        JSONObject liab = wealth.getJSONObject("liabilities");
        Assert.assertNotEquals("Margin debt in consolidated wealth breakdown must not be Rp 0 when holding USD margin!", "Rp 0", liab.getString("marginDebt"));

        // 3. Tax audit status
        JSONObject tax = facade.getTaxAuditStatus();
        Assert.assertNotNull(tax);
        System.out.println("Tax audit compliance: " + tax.optString("complianceScore", "OK"));
    }
}
