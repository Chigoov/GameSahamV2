package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.multiasset.TimeDeposit;
import com.tradingglobal3d.core.multiasset.TimeDepositManager;
import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.risk.PortfolioInsuranceService;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Pengujian Tahap S1: Deposito Tenor & Penalti Bunga Hangus, Asuransi Portofolio (Hedging),
 * dan Restrukturisasi Darurat Kebangkrutan.
 */
public class LiquidityInsuranceAndBailoutTahapS1Test {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "s1_test_" + UUID.randomUUID());
        testDir.mkdirs();

        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) {
            universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        }
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(InstrumentCatalog.fromJsonString(jsonUniverse));
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(100_000_000));
    }

    @After
    public void tearDown() {
        if (testDir != null && testDir.exists()) {
            File[] files = testDir.listFiles();
            if (files != null) for (File f : files) f.delete();
            testDir.delete();
        }
    }

    @Test
    public void testTimeDepositTenorAndLostInterestPenalty() throws Exception {
        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getAvailableCash(Currency.IDR);

        // Buka Deposito Tenor 10 Sesi sebesar 10jt
        JSONObject openRes = facade.openTimeDeposit("DEP-TENOR-10", BigDecimal.valueOf(10_000_000));
        assertTrue("Buka deposito harus berhasil", openRes.getBoolean("success"));

        String depId = openRes.getJSONObject("deposit").getString("id");

        // Cairkan dini sebelum jatuh tempo (Lost Interest Penalty)
        JSONObject withRes = facade.withdrawTimeDeposit(depId);
        assertTrue("Pencairan dini harus berhasil", withRes.getBoolean("success"));

        JSONObject depObj = withRes.getJSONObject("deposit");
        assertEquals("WITHDRAWN_EARLY", depObj.getString("status"));
        assertEquals("Rp 0", depObj.getString("interestPaid")); // Bunga hangus total!

        // Pokok kas kembali utuh ke saldo kas
        Money finalCash = personal.getAvailableCash(Currency.IDR);
        assertEquals(initialCash, finalCash);
    }

    @Test
    public void earlyDepositWithdrawalForfeitsAlreadyAccruedInterest() {
        Account personal = facade.getPersonalAccount();
        Money cashBefore = personal.getAvailableCash(Currency.IDR);
        TimeDepositManager manager = facade.getTimeDepositManager();
        long openedAt = 0L;
        TimeDeposit deposit = manager.openDeposit(personal, "DEP-TENOR-10",
                Money.of(10_000_000, Currency.IDR), openedAt, facade.getLedger());
        long withdrawalAt = 5L * 86_400_000L;
        Money accruedBeforeWithdrawal = deposit.calculateAccruedInterest(withdrawalAt);
        assertTrue("Fixture harus sudah mengakru bunga sebelum dicairkan", accruedBeforeWithdrawal.getAmount().signum() > 0);

        TimeDeposit withdrawn = manager.withdrawDeposit(deposit.getId(), personal, withdrawalAt, facade.getLedger());
        assertEquals("WITHDRAWN_EARLY", withdrawn.getStatus());
        assertTrue(withdrawn.getPenaltyPaid().getAmount().signum() > 0);
        assertEquals(Money.zero(Currency.IDR), withdrawn.getInterestPaid());
        assertEquals(cashBefore, personal.getAvailableCash(Currency.IDR));
    }

    @Test
    public void testDepositProductsExposeSessionTenorsAndTieredInterestBonuses() throws Exception {
        org.json.JSONArray products = facade.getTimeDepositProducts();
        assertEquals(6, products.length());
        JSONObject tenSession = null;
        JSONObject twentyFiveSession = null;
        JSONObject fiftySession = null;
        for (int i = 0; i < products.length(); i++) {
            JSONObject product = products.getJSONObject(i);
            if ("DEP-TENOR-10".equals(product.optString("id"))) tenSession = product;
            if ("DEP-TENOR-25".equals(product.optString("id"))) twentyFiveSession = product;
            if ("DEP-TENOR-50".equals(product.optString("id"))) fiftySession = product;
        }
        assertNotNull(tenSession);
        assertNotNull(twentyFiveSession);
        assertNotNull(fiftySession);
        assertEquals(10, tenSession.getInt("tenorSessions"));
        assertEquals(25, twentyFiveSession.getInt("tenorSessions"));
        assertEquals(50, fiftySession.getInt("tenorSessions"));
        assertEquals(10.0, tenSession.getDouble("bonusInterestPercent"), 0.001);
        assertEquals(25.0, twentyFiveSession.getDouble("bonusInterestPercent"), 0.001);
        assertEquals(50.0, fiftySession.getDouble("bonusInterestPercent"), 0.001);

        TimeDeposit deposit = facade.getTimeDepositManager().openDeposit(facade.getPersonalAccount(), "DEP-TENOR-10",
                Money.of(10_000_000, Currency.IDR), 0L, facade.getLedger());
        Money expectedInterest = Money.of(10_000_000, Currency.IDR).multiply(0.045 * 1.10 * 10.0 / 365.0);
        assertEquals(expectedInterest, deposit.calculateAccruedInterest(10L * 86_400_000L));

        TimeDeposit baseRateDeposit = new TimeDeposit("BASE", "BASE", "Base rate",
                Money.of(10_000_000, Currency.IDR), 0.045, 0, 0L);
        Money baseInterest = baseRateDeposit.calculateAccruedInterest(10L * 86_400_000L);
        assertTrue("Bonus 10% harus menaikkan bunga efektif dibanding tarif dasar",
                deposit.calculateAccruedInterest(10L * 86_400_000L).getAmount()
                        .compareTo(baseInterest.getAmount()) > 0);
    }

    @Test
    public void timeDepositAndInsuranceStateSurviveSaveAndLoad() throws Exception {
        facade.openTimeDeposit("DEP-TENOR-10", BigDecimal.valueOf(10_000_000));
        facade.purchasePortfolioInsurance(5);
        facade.save();

        TradingFacade restored = new TradingFacade(SaveManager.forDirectory(testDir));
        restored.setCatalog(facade.getCatalog());
        assertTrue(restored.load());
        assertEquals("Deposito aktif harus pulih setelah load", 1, restored.getActiveTimeDeposits().length());
        assertTrue("Polis aktif harus pulih setelah load", restored.getPortfolioInsuranceStatus().getBoolean("hasActivePolicy"));
    }

    @Test
    public void testPortfolioInsurancePurchaseAndCrashClaim() throws Exception {
        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getAvailableCash(Currency.IDR);

        // Beli polis asuransi untuk 5 sesi
        JSONObject polRes = facade.purchasePortfolioInsurance(5);
        assertTrue(polRes.getBoolean("success"));
        assertTrue(polRes.getJSONObject("policy").getBoolean("isValid"));

        // Kas berkurang untuk membayar premi
        Money cashAfterPrem = personal.getAvailableCash(Currency.IDR);
        assertTrue(cashAfterPrem.getAmount().compareTo(initialCash.getAmount()) < 0);

        // Simulasi Market Crash 7.5% dengan kerugian Rp 10.000.000
        PortfolioInsuranceService insService = facade.getPortfolioInsuranceService();
        Money claimReimbursed = insService.evaluateMarketCrashClaim(
                personal, 7.5, Money.of(10_000_000, Currency.IDR),
                facade.getLedger(), System.currentTimeMillis(), 1
        );

        // Klaim harus mengganti 80% dari kerugian = Rp 8.000.000
        assertEquals(Money.of(8_000_000, Currency.IDR), claimReimbursed);
        assertTrue("Polis harus ditandai telah diklaim", insService.getActivePolicy().isClaimed());
    }

    @Test
    public void insuranceClaimRequiresMarketDropStrictlyAboveFivePercent() {
        Account personal = facade.getPersonalAccount();
        PortfolioInsuranceService service = new PortfolioInsuranceService();
        service.purchasePolicy(personal, Money.of(10_000_000, Currency.IDR), 5,
                facade.getLedger(), facade.getClock().getGameTimestamp(), facade.getPlayerProfileManager().getRunDaysPlayed());
        Money loss = Money.of(1_000_000, Currency.IDR);
        Money atFive = service.evaluateMarketCrashClaim(personal, 5.0, loss, facade.getLedger(),
                facade.getClock().getGameTimestamp(), facade.getPlayerProfileManager().getRunDaysPlayed());
        assertEquals(Money.zero(Currency.IDR), atFive);
        assertFalse(service.getActivePolicy().isClaimed());
        Money aboveFive = service.evaluateMarketCrashClaim(personal, 5.01, loss, facade.getLedger(),
                facade.getClock().getGameTimestamp(), facade.getPlayerProfileManager().getRunDaysPlayed());
        assertEquals(Money.of(800_000, Currency.IDR), aboveFive);
        assertTrue(service.getActivePolicy().isClaimed());
    }

    @Test
    public void portfolioInsurancePurchaseMustBeBeforeAnyMarketOpens() throws Exception {
        GameClock marketOpen = new GameClock(java.time.LocalDate.of(2026, 10, 19), java.time.LocalTime.of(9, 15));
        assertTrue(marketOpen.isTradingOpenFor(MarketExchange.IDX));
        Money portfolioValue = Money.of(10_000_000, Currency.IDR);
        try {
            facade.getPortfolioInsuranceService().purchasePolicy(facade.getPersonalAccount(), portfolioValue, 5,
                    facade.getLedger(), marketOpen.getGameTimestamp(), facade.getPlayerProfileManager().getRunDaysPlayed());
            fail("Polis harus ditolak setelah sesi bursa dibuka");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("sebelum"));
        }
    }

    @Test
    public void blackSwanMarketEventMustAutomaticallySettleInsuranceClaim() throws Exception {
        Account personal = facade.getPersonalAccount();
        com.tradingglobal3d.core.market.Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        assertNotNull(bbca);
        personal.addPosition(new com.tradingglobal3d.core.model.Position("BBCA", "IDX", Currency.IDR, 1_000,
                bbca.getCurrentPrice().getAmount(), bbca.getCurrentPrice().getAmount()));
        facade.getPortfolioInsuranceService().purchasePolicy(personal, Money.of(10_000_000, Currency.IDR),
                5, facade.getLedger(), facade.getClock().getGameTimestamp(), facade.getPlayerProfileManager().getRunDaysPlayed());
        JSONObject result = facade.triggerMarketShockEvent("BLACK_SWAN_SHOCK");
        assertEquals(15.0, result.getDouble("dropRatePercent"), 0.001);
        assertTrue("Klaim harus tercatat otomatis setelah kejutan pasar", result.getBoolean("insuranceClaimSettled"));
        assertTrue(facade.getPortfolioInsuranceService().getActivePolicy().isClaimed());
    }

    @Test
    public void turnaroundBailoutRejectsNonInsolventPlayer() throws Exception {
        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getAvailableCash(Currency.IDR);
        JSONObject preview = facade.previewTurnaroundBailout();
        assertFalse(preview.getBoolean("eligible"));
        assertTrue(preview.getJSONArray("auctionItems").length() == 0);
        try {
            facade.executeTurnaroundBailout("BO-HEALTHY-001");
            fail("Pemain tanpa utang insolven tidak boleh menerima dana bailout");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("insolven"));
        }
        assertEquals(initialCash, personal.getAvailableCash(Currency.IDR));
    }

    @Test
    public void turnaroundExtendsRealInstallmentLoanAfterAuctioningUnpledgedCollectible() throws Exception {
        Account personal = facade.getPersonalAccount();
        facade.buyCollection("COL-CAR-00");
        JSONObject quote = facade.getLoanQuote("LOAN-PERS-01", true, null,
                Money.of(5_000_000, Currency.IDR), 14, "COL-CAR-00");
        assertTrue(quote.getBoolean("success"));
        JSONObject disbursement = facade.disburseReviewedLoan("F03-LOAN-DISBURSE", "LOAN-PERS-01", true, null,
                Money.of(5_000_000, Currency.IDR), 14, "COL-CAR-00", quote.getString("quoteVersion"));
        assertTrue(disbursement.getBoolean("success"));
        String loanId = disbursement.getString("loanId");
        int remainingDaysBefore = facade.getInstallmentLoanManager().getLoan(loanId).getRemainingDays();
        facade.buyCollection("COL-ART-00");
        facade.getRiskService().takeMarginLoan(personal, Money.of(300_000_000, Currency.IDR),
                facade.getLedger(), facade.getClock().getGameTimestamp());
        Money allAvailableCash = personal.getAvailableCash(Currency.IDR);
        facade.getLedger().recordMutation("F03-DISTRESS-USE-CASH", personal,
                com.tradingglobal3d.core.economy.LedgerEntryType.TRANSACTION_FEE, allAvailableCash, false,
                facade.getClock().getGameTimestamp(), "Fixture penggunaan kas untuk skenario insolvensi QA");
        assertTrue(facade.getPersonalNetWorth().isNegative());

        JSONObject rescue = facade.executeTurnaroundBailout("F03-INSTALLMENT-RESCUE");
        assertTrue(rescue.getBoolean("success"));
        assertEquals("Koleksi tanpa agunan harus dilelang", 1, rescue.getInt("auctionedCollections"));
        assertTrue("Koleksi yang diagunkan harus tetap dimiliki", facade.getCollectionManager().getItem("COL-CAR-00").isOwned());
        assertFalse("Koleksi tidak diagunkan harus dilelang", facade.getCollectionManager().getItem("COL-ART-00").isOwned());
        JSONObject loanRestructure = rescue.getJSONArray("restructuredLoans").getJSONObject(0);
        assertEquals(loanId, loanRestructure.getString("loanId"));
        assertEquals(remainingDaysBefore + 30, loanRestructure.getInt("newRemainingDays"));
        assertEquals("Rp 0", rescue.getString("cashGrant"));
    }

    @Test
    public void turnaroundBailoutAuctionsCollectionRepaysDebtAndIsIdempotent() throws Exception {
        Account personal = facade.getPersonalAccount();
        com.tradingglobal3d.core.collection.CollectibleItem item = facade.getCollectionManager().getItem("COL-ART-00");
        assertNotNull(item);
        facade.buyCollection(item.getId());
        facade.getRiskService().takeMarginLoan(personal, Money.of(300_000_000, Currency.IDR),
                facade.getLedger(), facade.getClock().getGameTimestamp());
        Money remainingCash = personal.getAvailableCash(Currency.IDR);
        facade.getLedger().recordMutation("TEST-INSOLVENCY-CASH", personal,
                com.tradingglobal3d.core.economy.LedgerEntryType.TRANSACTION_FEE, remainingCash, false,
                facade.getClock().getGameTimestamp(), "Fixture biaya distress untuk uji bailout terisolasi");
        assertTrue(facade.getPersonalNetWorth().isNegative());

        JSONObject result = facade.executeTurnaroundBailout("BO-VALID-001");
        assertTrue(result.getBoolean("success"));
        assertEquals(1, result.getInt("auctionedCollections"));
        assertTrue(result.getJSONObject("debtRestructure").getJSONObject("IDR")
                .getString("debtRelief").contains("Rp"));
        assertFalse("Bailout tidak boleh menjadi hibah kas gratis", personal.getAvailableCash(Currency.IDR).isPositive());
        assertFalse("Koleksi yang dilelang tidak lagi dimiliki", item.isOwned());

        Money debtAfterFirst = facade.getRiskService().getMarginDebt(personal.getId(), Currency.IDR);
        JSONObject retry = facade.executeTurnaroundBailout("BO-VALID-001");
        assertTrue(retry.getBoolean("alreadyProcessed"));
        assertEquals(debtAfterFirst, facade.getRiskService().getMarginDebt(personal.getId(), Currency.IDR));
    }
}
