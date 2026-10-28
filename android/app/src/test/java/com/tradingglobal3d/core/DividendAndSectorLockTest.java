package com.tradingglobal3d.core;

import com.tradingglobal3d.core.corporate.CorporateAction;
import com.tradingglobal3d.core.corporate.CorporateActionService;
import com.tradingglobal3d.core.corporate.CorporateActionType;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.market.MarketExchange;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.MatchingEngine;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;
import com.tradingglobal3d.core.trading.OrderStatus;
import com.tradingglobal3d.core.trading.OrderType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Pengujian Tahap E1: Pembagian Dividen Tunai Emiten & Gembok Sektor Saham Berdasarkan Level Pangkat.
 */
public class DividendAndSectorLockTest {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "e1_div_test_" + UUID.randomUUID());
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
    public void testSectorLevelLockRules() {
        // Level 1: Keuangan & Konsumer terbuka
        assertEquals(1, TradingFacade.getRequiredLevelForSector("Keuangan", MarketExchange.IDX));
        assertEquals(1, TradingFacade.getRequiredLevelForSector("Konsumer Primer", MarketExchange.IDX));

        // Level 4: Energi, Pertambangan, Properti
        assertEquals(4, TradingFacade.getRequiredLevelForSector("Energi", MarketExchange.IDX));
        assertEquals(4, TradingFacade.getRequiredLevelForSector("Tambang & Mineral", MarketExchange.IDX));

        // Level 7: Teknologi & Saham Global Wall Street
        assertEquals(7, TradingFacade.getRequiredLevelForSector("Teknologi", MarketExchange.IDX));
        assertEquals(7, TradingFacade.getRequiredLevelForSector("Teknologi", MarketExchange.US));
        assertEquals(7, TradingFacade.getRequiredLevelForSector("Keuangan", MarketExchange.US));
    }

    @Test
    public void testOrderRejectionOnLockedSectorAtLevel1() {
        Account personal = facade.getPersonalAccount();

        // 1. Order Beli saham sektor Energi (ADRO) pada Level 1 harus DITOLAK
        Instrument adro = facade.getCatalog().getById("IDX:ADRO");
        if (adro != null) {
            Order adroOrder = new Order(
                    "ORD-ADRO-LOCK", "REQ-ADRO-LOCK", personal.getId(), "IDX:ADRO",
                    OrderSide.BUY, OrderType.LIMIT,
                    adro.getCurrentPrice(), null, 100, System.currentTimeMillis(), Money.of(500_000, Currency.IDR)
            );
            MatchingEngine.OrderResult res = facade.submitOrder(adroOrder);
            assertFalse("Order beli sektor Energi pada level 1 harus ditolak", res.isSuccess);
            assertTrue("Pesan penolakan harus menyebut sektor terkunci",
                    res.message != null && res.message.contains("terkunci"));
        }

        // 2. Order Beli saham sektor Keuangan (BBCA) pada Level 1 tidak ditolak oleh gembok sektor
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        assertNotNull(bbca);
        assertTrue("Sektor Keuangan harus terbuka di Level 1",
                facade.isSectorUnlocked(bbca.getSector(), bbca.getExchange()));
    }

    @Test
    public void testCashDividendDistributionAndLedgerCredit() {
        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getAvailableCash(Currency.IDR);

        // Berikan kepemilikan 1.000 lembar saham BBCA ke pemain
        personal.addPosition(new Position(
                "BBCA", "IDX", Currency.IDR, 1000,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10250)
        ));

        // Buat Aksi Korporasi Dividen Tunai Rp 225 per lembar
        CorporateActionService cs = facade.getCorporateService();
        CorporateAction divAction = new CorporateAction(
                "ACT-DIV-BBCA-TEST", "BBCA", CorporateActionType.CASH_DIVIDEND,
                BigDecimal.valueOf(225), Currency.IDR,
                LocalDate.now(), LocalDate.now()
        );
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");

        // Eksekusi pembagian dividen
        int paidCount = cs.executeCashDividend(
                divAction, bbca,
                java.util.Collections.singletonList(personal),
                facade.getLedger(),
                System.currentTimeMillis()
        );

        assertTrue("Dividen harus dibayarkan ke minimal 1 akun pemegang", paidCount >= 1);
        assertTrue("Status dividen harus bertanda dieksekusi", divAction.isExecuted());

        // Saldo kas pemain harus bertambah Rp 225 x 1.000 = Rp 225.000
        Money expectedGain = Money.of(225_000, Currency.IDR);
        Money finalCash = personal.getAvailableCash(Currency.IDR);
        assertEquals("Kas harus bertambah tepat sebesar dividen tunai",
                initialCash.add(expectedGain), finalCash);
    }
}
