package com.tradingglobal3d.core;

import com.tradingglobal3d.core.corporate.CorporateActionService;
import com.tradingglobal3d.core.corporate.IpoOffering;
import com.tradingglobal3d.core.corporate.IpoSubscription;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.storage.SaveManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Pengujian verifikasi R06: Minimal 3 IPO per bulan game, penjatahan, refund, dan registrasi bursa.
 */
public class IpoScheduleAndAllotmentTest {
    private File testDir;
    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "r06_ipo_test_" + UUID.randomUUID());
        testDir.mkdirs();

        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) {
            universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        }
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(InstrumentCatalog.fromJsonString(jsonUniverse));
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(500_000_000));
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
    public void testAtLeast3IposPerMonthScheduledAcrossYear() {
        CorporateActionService cs = facade.getCorporateService();
        LocalDate baseDate = LocalDate.of(2026, 1, 1);

        for (int m = 0; m < 12; m++) {
            LocalDate currentMonth = baseDate.plusMonths(m);
            cs.ensureMonthlyIpoSchedule(currentMonth);

            int monthCount = 0;
            for (IpoOffering ipo : cs.getIpoOfferings()) {
                if (ipo.getListingDate().getYear() == currentMonth.getYear() &&
                    ipo.getListingDate().getMonthValue() == currentMonth.getMonthValue()) {
                    monthCount++;
                }
            }
            assertTrue("Bulan " + currentMonth + " harus memiliki minimal 3 IPO terjadwal (aktual: " + monthCount + ")",
                    monthCount >= 3);
        }
    }

    @Test
    public void testIpoSubscriptionAllotmentAndMarketListing() {
        CorporateActionService cs = facade.getCorporateService();
        LocalDate gameDate = facade.getClock().getCurrentDate();
        cs.ensureMonthlyIpoSchedule(gameDate);
        IpoOffering targetIpo = null;
        for (IpoOffering ipo : cs.getIpoOfferings()) {
            if (!ipo.getListingDate().isBefore(gameDate) && !ipo.isAllotted()) {
                targetIpo = ipo;
                break;
            }
        }
        assertNotNull("Harus ada penawaran IPO aktif untuk tanggal game saat ini", targetIpo);
        Account personal = facade.getPersonalAccount();
        Money initialCash = personal.getAvailableCash(Currency.IDR);

        // Pesan 10 lot
        int orderLots = 10;
        IpoSubscription sub = facade.orderIpo(targetIpo.getId(), orderLots, "REQ-TEST-IPO-1");
        assertNotNull(sub);
        assertEquals(orderLots, sub.getOrderedLots());

        // Kas harus berkurang karena dana di-escrow/reserve
        Money cashAfterOrder = personal.getAvailableCash(Currency.IDR);
        assertTrue("Kas harus berkurang setelah memesan IPO", initialCash.getAmount().compareTo(cashAfterOrder.getAmount()) > 0);

        // Eksekusi penjatahan 50%
        facade.allotIpo(targetIpo.getId(), 0.50);
        assertTrue("IPO harus berstatus allotted", targetIpo.isAllotted());

        // Pemain harus menerima saham penjatahan di portofolio
        long expectedShares = 5 * targetIpo.getLotSize();
        com.tradingglobal3d.core.model.Position pos = personal.getPosition(targetIpo.getTicker());
        assertNotNull("Pemain harus memiliki posisi", pos);
        assertEquals("Pemain harus memegang saham hasil penjatahan IPO", expectedShares, pos.getQuantity());

        // Saham IPO harus terdaftar di katalog pasar aktif
        String expectedInstId = "IDX:" + targetIpo.getTicker();
        assertNotNull("Emiten IPO harus terdaftar di katalog pasar", facade.getCatalog().getById(expectedInstId));
    }
}
