package com.tradingglobal3d.core;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.multiasset.BondInstrument;
import com.tradingglobal3d.core.multiasset.MultiAssetTradingService;
import com.tradingglobal3d.core.risk.RiskComplianceService;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.storage.SaveState;
import com.tradingglobal3d.mobile.TradingWebBridge;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Collections;

public class PackageP01SafetyAndDomainTest {

    @Test
    public void testAU04_SaveErrorEscapedProperlyAsJson() throws Exception {
        File testDir = new File("build/tmp-test-p01-au04");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        TradingWebBridge bridge = new TradingWebBridge(facade);

        // Intentionally create a blocker directory so facade.save() throws exception containing backslashes
        File saveBlocker = sm.getSaveFile();
        saveBlocker.mkdirs(); // making it a directory causes write failure with Windows path in exception

        String response = bridge.saveGame();
        Assert.assertNotNull(response);
        // Must parse as valid JSON without throwing JSONException
        JSONObject json = new JSONObject(response);
        Assert.assertFalse(json.getBoolean("success"));
        Assert.assertNotNull(json.getString("message"));
    }

    @Test
    public void testAU05_BondCouponDealerLiquidityDeficitDoesNotCrashOrPauseClock() throws Exception {
        File testDir = new File("build/tmp-test-p01-au05");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        Account personal = facade.getPersonalAccount();
        Account dealer = facade.getAccount("ACC-NPC-MM-01");
        MultiAssetTradingService mats = facade.getMultiAssetTrading();
        BondInstrument bond = mats.getProducts().getAllBonds().get(0);

        // Give player 10 units of bond
        personal.addPosition(new Position(bond.getTicker(), "BOND", Currency.IDR, 10, bond.getParValue().getAmount(), bond.getParValue().getAmount()));

        // Drain dealer cash to 0
        Money dealerCash = dealer.getAvailableCash(Currency.IDR);
        dealer.debitCash(dealerCash);
        Assert.assertEquals(Money.zero(Currency.IDR), dealer.getAvailableCash(Currency.IDR));

        long now = facade.getGameTimestamp() + 86400000L; // 1 day elapsed
        // settleCoupon should NOT throw IllegalStateException or pause clock
        mats.settleCoupon(personal, dealer, facade.getLedger(), now);

        // Advance facade time: clock must NOT be paused
        facade.advanceRealMs(1000, true);
        Assert.assertFalse("World clock must not be paused by coupon settlement deficit!", facade.getClock().isPaused());
    }

    @Test
    public void testAU09_ShortSellingRejectsWhenCommittedSharesExceedFree() throws Exception {
        RiskComplianceService risk = new RiskComplianceService();
        Account borrower = new Account("BORROWER", "Borrower", com.tradingglobal3d.core.model.AccountType.PERSONAL, null);
        Account lender = new Account("LENDER", "Lender", com.tradingglobal3d.core.model.AccountType.FIRM, null);
        EconomyLedger ledger = new EconomyLedger();
        Instrument inst = new Instrument("IDX:TEST", "TEST", "Test", com.tradingglobal3d.core.market.MarketExchange.IDX, "Finance", Currency.IDR, 100, 1000000L, true, Money.of(10000L, Currency.IDR));

        // Lender has 20,000 shares, but 15,000 are committed to sell limit orders
        lender.addPosition(new Position("TEST", "IDX", Currency.IDR, 20000L, BigDecimal.valueOf(10000), BigDecimal.valueOf(10000)));
        long lenderCommitted = 15000L;

        // Player wants to short 10,000 shares: only 5,000 free -> MUST be rejected
        boolean rejected = false;
        try {
            risk.openShortPosition(borrower, inst, 10000L, lender, ledger, 1000L, lenderCommitted);
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("tidak mencukupi")) {
                rejected = true;
            }
        }
        Assert.assertTrue("Short selling must be rejected when requested shares exceed free uncommitted inventory!", rejected);

        // Player shorting 5,000 shares (free inventory) -> succeeds
        risk.openShortPosition(borrower, inst, 5000L, lender, ledger, 1000L, lenderCommitted);
        Assert.assertEquals(15000L, lender.getPosition("IDX:TEST").getQuantity());
    }

    @Test
    public void testAU23_BondHolderPortfolioEndpointSucceeds() throws Exception {
        File testDir = new File("build/tmp-test-p01-au23");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        Account personal = facade.getPersonalAccount();
        TradingWebBridge bridge = new TradingWebBridge(facade);

        // Add bond position to personal account
        personal.addPosition(new Position("FR0091", "BOND", Currency.IDR, 5, BigDecimal.valueOf(1000000), BigDecimal.valueOf(1000000)));

        String res1 = bridge.getPortfolio(personal.getId());
        JSONObject json1 = new JSONObject(res1);
        Assert.assertTrue("getPortfolio must succeed for account holding bond positions!", json1.optBoolean("success", true));

        // Sell all bonds (quantity = 0)
        personal.getPosition("BOND:FR0091").addSell(5, BigDecimal.valueOf(1000000));
        Assert.assertEquals(0L, personal.getPosition("BOND:FR0091").getQuantity());

        String res2 = bridge.getPortfolio(personal.getId());
        JSONObject json2 = new JSONObject(res2);
        Assert.assertTrue("getPortfolio must succeed after bond positions sold out (qty 0)!", json2.optBoolean("success", true));
    }

    @Test
    public void testAU27_SaveWithInconsistentLedgerFailsValidationAndProtectsBackup() throws Exception {
        File testDir = new File("build/tmp-test-p01-au27");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("50000000"));

        // Save once so primary and backup exist
        facade.save();
        facade.save(); // rotates to .bak

        File primFile = sm.getSaveFile();
        String jsonStr = Files.readString(primFile.toPath());
        JSONObject saveJson = new JSONObject(jsonStr);

        JSONArray accounts = saveJson.getJSONArray("accounts");
        for (int i = 0; i < accounts.length(); i++) {
            JSONObject a = accounts.getJSONObject(i);
            if ("PERSONAL".equals(a.getString("type"))) {
                a.getJSONObject("cash").put("IDR", "50000001");
                break;
            }
        }
        Files.writeString(primFile.toPath(), saveJson.toString());

        // Facade loading primary with semantic inconsistency must detect it and offer backup
        SaveManager smNew = SaveManager.forDirectory(testDir);
        TradingFacade facadeNew = new TradingFacade(smNew);
        facadeNew.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        boolean resumed = facadeNew.startOrResume();
        Assert.assertFalse("Loading semantically inconsistent save must not resume as valid primary!", resumed);
        Assert.assertTrue("Backup recovery must be offered when primary is inconsistent!", facadeNew.hasPendingBackupRecoveryOffer());
    }

    @Test
    public void testAU37_NewGameWithInvalidNameRejectsBeforeResettingProgress() throws Exception {
        File testDir = new File("build/tmp-test-p01-au37");
        testDir.mkdirs();
        SaveManager sm = SaveManager.forDirectory(testDir);
        TradingFacade facade = new TradingFacade(sm);
        String catJson = Files.readString(new File("android/app/src/main/assets/data/market_universe.json").toPath());
        facade.setCatalog(InstrumentCatalog.fromJsonString(catJson));
        facade.startInternalGame(new BigDecimal("88000000")); // current run with 88M IDR
        facade.getPlayerProfileManager().setPlayerName("Kapten Saham Asli");
        facade.save();

        TradingWebBridge bridge = new TradingWebBridge(facade);

        // Attempt new game with invalid name (>48 characters)
        String invalidName = "A".repeat(50);
        JSONObject req = new JSONObject()
                .put("mode", "INTERNAL_MARKET")
                .put("initialCapital", 10000000L)
                .put("playerName", invalidName);

        String res = bridge.startNewGame(req.toString());
        JSONObject json = new JSONObject(res);
        Assert.assertFalse("startNewGame must fail with invalid player name!", json.getBoolean("success"));

        // Progress MUST NOT have been reset
        Assert.assertEquals(Money.of(88000000L, Currency.IDR), facade.getPersonalAccount().getAvailableCash(Currency.IDR));
        Assert.assertEquals("Kapten Saham Asli", facade.getPlayerProfileManager().getPlayerName());
    }
}
