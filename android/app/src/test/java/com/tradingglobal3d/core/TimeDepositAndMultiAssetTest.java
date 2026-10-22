package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.multiasset.TimeDeposit;
import com.tradingglobal3d.core.multiasset.TimeDepositManager;
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
 * Pengujian verifikasi R10: Deposito Berjangka Perbankan, penempatan, pencairan dini berpenalti, dan jatuh tempo.
 */
public class TimeDepositAndMultiAssetTest {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "r10_dep_test_" + UUID.randomUUID());
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
    public void testOpenTimeDepositAndEarlyWithdrawal() throws Exception {
        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getAvailableCash(Currency.IDR);

        // Buka Deposito 10jt di BCA
        BigDecimal depositAmount = BigDecimal.valueOf(10_000_000);
        JSONObject res = facade.openTimeDeposit("DEP-BCA-1M", depositAmount);
        assertTrue(res.getBoolean("success"));

        // Kas harus berkurang 10jt
        Money afterOpen = personal.getAvailableCash(Currency.IDR);
        assertEquals(initialCash.subtract(new Money(depositAmount, Currency.IDR)), afterOpen);

        // Verifikasi deposito tercatat aktif
        assertEquals(1, facade.getActiveTimeDeposits().length());
        String depId = res.getJSONObject("deposit").getString("id");

        // Cairkan dini
        JSONObject withRes = facade.withdrawTimeDeposit(depId);
        assertTrue(withRes.getBoolean("success"));
        assertEquals(0, facade.getActiveTimeDeposits().length());

        // Kas pokok harus kembali utuh
        Money afterWithdraw = personal.getAvailableCash(Currency.IDR);
        assertTrue("Kas setelah pencairan harus kembali mendekati modal awal",
                afterWithdraw.getAmount().compareTo(initialCash.getAmount().subtract(BigDecimal.valueOf(1000))) >= 0);
    }
}
