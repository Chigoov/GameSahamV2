package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.multiasset.TimeDeposit;
import com.tradingglobal3d.core.multiasset.TimeDepositManager;
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
    public void testTurnaroundBailoutRelief() throws Exception {
        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getAvailableCash(Currency.IDR);

        // Ajukan restrukturisasi bailout darurat
        JSONObject bailout = facade.executeTurnaroundBailout("BO-REQ-001");
        assertTrue(bailout.getBoolean("success"));
        assertEquals("Rp 25.000.000", bailout.getString("bailoutRelief"));

        // Kas bertambah dana talangan Rp 25.000.000
        Money expectedCash = initialCash.add(Money.of(25_000_000, Currency.IDR));
        assertEquals(expectedCash, personal.getAvailableCash(Currency.IDR));
    }
}
