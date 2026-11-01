package com.tradingglobal3d.core;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntry;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.loans.InstallmentLoanManager;
import com.tradingglobal3d.core.loans.LoanContract;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.npc.NpcAgent;
import com.tradingglobal3d.core.npc.NpcStrategyType;
import com.tradingglobal3d.core.research.NewsArticle;
import com.tradingglobal3d.core.research.NewsPriceTarget;
import com.tradingglobal3d.core.research.ResearchService;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.OrderBook;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

public class BacklogReviewB01B05Test {

    @Test
    public void testB01_RetailReplenishmentRecordedViaSupplyEventAndLedger() throws Exception {
        File testDir = new File("build/tmp-test-b01-unit");
        testDir.mkdirs();
        for (File f : testDir.listFiles()) f.delete();

        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        InstrumentCatalog catalog = InstrumentCatalog.fromJsonString(catJson);
        facade.setCatalog(catalog);
        facade.startInternalGame(new BigDecimal("50000000"));

        Account retail = facade.getAccount("ACC-NPC-RETAIL-01");
        Assert.assertNotNull(retail);

        Instrument bbca = catalog.getById("IDX:BBCA");
        OrderBook book = facade.getMarketEngine().getOrCreateBook(bbca);
        EconomyLedger ledger = facade.getLedger();

        // Drain retail BBCA inventory
        Position pos = retail.getPosition(bbca.getId());
        if (pos != null) retail.removePosition(bbca.getId());

        // Drain retail cash
        Money curCash = retail.getAvailableCash(Currency.IDR);
        retail.debitCash(curCash.subtract(Money.of(1000L, Currency.IDR)));

        long startShares = bbca.getSharesOutstanding();
        int ledgerCountBefore = ledger.getEntries().size();

        NpcAgent retailAgent = new NpcAgent("NPC-RETAIL-01", "Investor Ritel Mandiri", NpcStrategyType.RETAIL_NOISE, retail);
        retailAgent.ensureLiquidity(bbca, book, ledger, facade.getGameTimestamp(), 0L, Money.zero(Currency.IDR), "UNIT-TEST-1");

        // 1. Shares replenished to 1000 lots
        Position posAfter = retail.getPosition(bbca.getId());
        Assert.assertNotNull(posAfter);
        Assert.assertEquals(1000L * bbca.getLotSize(), posAfter.getQuantity());

        // 2. Shares outstanding increased cleanly
        Assert.assertEquals(startShares + 1000L * bbca.getLotSize(), bbca.getSharesOutstanding());

        // 3. Cash replenished to 2 Billion IDR
        Assert.assertEquals(Money.of(2_000_000_000L, Currency.IDR), retail.getAvailableCash(Currency.IDR));

        // 4. Ledger records capital mutation
        Assert.assertEquals(ledgerCountBefore + 1, ledger.getEntries().size());
        LedgerEntry entry = ledger.getEntries().get(ledger.getEntries().size() - 1);
        Assert.assertEquals(LedgerEntryType.LIQUIDITY_CAPITAL, entry.getType());
        Assert.assertEquals(Money.of(1_999_999_000L, Currency.IDR), entry.getAmount());

        // 5. Repeat ensureLiquidity is idempotent
        retailAgent.ensureLiquidity(bbca, book, ledger, facade.getGameTimestamp(), 0L, Money.zero(Currency.IDR), "UNIT-TEST-2");
        Assert.assertEquals(startShares + 1000L * bbca.getLotSize(), bbca.getSharesOutstanding());
        Assert.assertEquals(ledgerCountBefore + 1, ledger.getEntries().size());
    }

    @Test
    public void testB02_LoanQuoteAndContractRateMatchExactly() throws Exception {
        InstallmentLoanManager manager = new InstallmentLoanManager();
        manager.updateMacroLoanRate(12.5); // 12.5% p.a.
        Account acc = new Account("ACC-TEST-LOAN", "Peminjam Uji", AccountType.PERSONAL, null);
        acc.creditCash(Money.of(20_000_000L, Currency.IDR));
        EconomyLedger ledger = new EconomyLedger();

        Money principal = Money.of(10_000_000L, Currency.IDR);
        long now = 1700000000000L;
        JSONObject res = manager.disburseLoan("LOAN-PERS-02", acc, principal, 14, ledger, now);

        String loanId = res.getString("loanId");
        LoanContract contract = manager.getLoan(loanId);
        Assert.assertNotNull(contract);

        BigDecimal expectedDailyRate = BigDecimal.valueOf(12.5).divide(BigDecimal.valueOf(36500), 8, RoundingMode.HALF_UP);
        Assert.assertEquals(expectedDailyRate, contract.getDailyInterestRate());

        // Accrue 1 day
        contract.processDailyAccrual(now + 86400000L);
        Money interest = contract.getAccruedInterest();
        Money expectedInterest = principal.multiply(expectedDailyRate);
        Assert.assertEquals(expectedInterest, interest);

        // State export/restore
        JSONObject exported = contract.exportState();
        LoanContract restored = LoanContract.restoreFromState(exported);
        Assert.assertEquals(expectedDailyRate, restored.getDailyInterestRate());
    }

    @Test
    public void testB03_WhaleFollowUpLinksToOriginAndDoublesTarget() throws Exception {
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        InstrumentCatalog catalog = InstrumentCatalog.fromJsonString(catJson);
        ResearchService rs = new ResearchService();
        rs.setDynamicDailyNewsEnabled(true);

        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate whaleOriginDate = null;
        for (int i = 0; i < 30; i++) {
            LocalDate d = startDate.plusDays(i);
            if (Math.floorMod(d.toEpochDay(), 14) == 2) {
                whaleOriginDate = d;
                break;
            }
        }
        Assert.assertNotNull(whaleOriginDate);

        // Generate origin
        long originTs = whaleOriginDate.toEpochDay() * 86400000L;
        rs.ensureDailyNewsSchedule(whaleOriginDate, catalog, originTs);

        NewsArticle origin = null;
        for (NewsArticle a : rs.getPublishedNews(originTs + 1000L)) {
            if (a.getId().startsWith("NEWS-WHALE-") && !a.getId().contains("FOLLOWUP")) {
                origin = a;
                break;
            }
        }
        Assert.assertNotNull(origin);

        // Advance 7 days to follow-up date (dayIn14 == 9)
        LocalDate followUpDate = whaleOriginDate.plusDays(7);
        Assert.assertEquals(9, Math.floorMod(followUpDate.toEpochDay(), 14));
        long followUpTs = followUpDate.toEpochDay() * 86400000L;
        rs.ensureDailyNewsSchedule(followUpDate, catalog, followUpTs);

        NewsArticle followUp = null;
        for (NewsArticle a : rs.getPublishedNews(followUpTs + 1000L)) {
            if (a.getId().startsWith("NEWS-WHALE-FOLLOWUP-")) {
                followUp = a;
                break;
            }
        }
        Assert.assertNotNull(followUp);

        // Ticker must match origin exactly
        Assert.assertEquals(origin.getRelatedTicker(), followUp.getRelatedTicker());

        // Target percent must be 2x origin
        double expectedPct = Math.round(origin.getSentimentImpact() * 2.0 * 1000.0) / 1000.0;
        Assert.assertEquals(expectedPct, followUp.getSentimentImpact(), 0.0001);

        // Price target is registered with isFollowUp == true
        NewsPriceTarget tgt = rs.getTargetForNews(followUp.getId());
        Assert.assertNotNull(tgt);
        Assert.assertTrue(tgt.isFollowUp());
    }
}
