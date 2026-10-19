package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.firm.ClientMandate;
import com.tradingglobal3d.core.firm.FirmManager;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class FirmClientMandateFeeTest {
    private File testDir;
    private TradingFacade facade;
    private InstrumentCatalog catalog;
    private Instrument bbca;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "firm_test_" + System.currentTimeMillis());
        testDir.mkdirs();

        File dataDir = new File("src/main/assets/data");
        File universeFile = new File(dataDir, "market_universe.json");
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        catalog = InstrumentCatalog.fromJsonString(jsonUniverse);

        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        com.tradingglobal3d.core.GameTestSupport.unlockManager(facade);

        bbca = catalog.getById("IDX:BBCA");
        assertNotNull(bbca);
    }

    @After
    public void tearDown() {
        deleteDir(testDir);
    }

    private void deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) deleteDir(f);
        }
        dir.delete();
    }

    @Test
    public void testAcceptMandatesAndAccountIsolation() {
        // Terima dua mandat klien terpisah
        Account client1 = facade.acceptClientMandate("MANDATE-01");
        Account client2 = facade.acceptClientMandate("MANDATE-02");

        assertNotNull(client1);
        assertNotNull(client2);
        assertNotEquals(client1.getId(), client2.getId());
        assertEquals(AccountType.CLIENT, client1.getType());
        assertEquals(AccountType.CLIENT, client2.getType());

        // Saldo terpisah
        assertEquals(Money.of(100_000_000, Currency.IDR), client1.getTotalCash(Currency.IDR));
        assertEquals(Money.of(250_000_000, Currency.IDR), client2.getTotalCash(Currency.IDR));
        assertEquals(Money.of(50_000_000, Currency.IDR), facade.getPersonalAccount().getTotalCash(Currency.IDR));

        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testAcceptingMandateAgainCannotCreateAnotherFundedAccount() {
        Account first = facade.acceptClientMandate("MANDATE-01");
        int count = facade.getAllAccounts().size();
        int entries = facade.getLedger().getEntries().size();
        Account retry = facade.acceptClientMandate("MANDATE-01");
        assertSame(first, retry);
        assertEquals(count, facade.getAllAccounts().size());
        assertEquals(entries, facade.getLedger().getEntries().size());
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testHardLimitValidationOnClientOrder() {
        Account client1 = facade.acceptClientMandate("MANDATE-01"); // Konservatif: max konsentrasi 15% (15jt), min kas 20% (20jt)

        // Kasus 1: Coba beli BBCA senilai Rp 30.000.000 (> 15% AUM) -> Harus DITOLAK
        long excessQty = 3000; // 3000 lembar * 10250 = 30.750.000
        Order excessOrder = new Order(
                "ORD-MANDATE-EXCESS", "REQ-EXCESS",
                client1.getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT,
                Money.of(10250, Currency.IDR), null, excessQty, System.currentTimeMillis(), null
        );

        MatchingEngine.OrderResult res1 = facade.submitOrder(excessOrder);
        assertFalse("Order melanggar batas konsentrasi harus ditolak", res1.isSuccess);
        assertTrue(res1.message.contains("konsentrasi"));
        assertEquals(OrderStatus.REJECTED, excessOrder.getStatus());

        // Kasus 2: Coba beli sesuai batas aman (100 lembar * 10250 = Rp 1.025.000, sekitar 1% AUM) -> Harus DITERIMA
        Order validOrder = new Order(
                "ORD-MANDATE-VALID", "REQ-VALID",
                client1.getId(), bbca.getId(),
                OrderSide.BUY, OrderType.LIMIT,
                Money.of(10250, Currency.IDR), null, 100, System.currentTimeMillis(), null
        );

        MatchingEngine.OrderResult res2 = facade.submitOrder(validOrder);
        assertTrue("Order sesuai mandat harus diterima", res2.isSuccess);

        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testRejectedMandateRetryCannotReduceTrustTwice() {
        Account client = facade.acceptClientMandate("MANDATE-01");
        ClientMandate mandate = facade.getFirmManager().getMandate("MANDATE-01");
        Order first = Order.createLimit("MANDATE-RETRY", bbca.getId(), client.getId(), OrderSide.BUY,
                Money.of(10250, Currency.IDR), 3000, 1L);
        assertFalse(facade.submitOrder(first).isSuccess);
        int trust = mandate.getTrustScore();
        Order retry = Order.createLimit("MANDATE-RETRY", bbca.getId(), client.getId(), OrderSide.BUY,
                Money.of(10250, Currency.IDR), 3000, 2L);
        MatchingEngine.OrderResult result = facade.submitOrder(retry);
        assertFalse(result.isSuccess);
        assertSame(first, result.order);
        assertEquals("A retry is not a new compliance violation", trust, mandate.getTrustScore());
        assertEquals(1, facade.getOrderManager().getOrdersForAccount(client.getId()).size());
    }

    @Test
    public void testCapitalFlowsDepositAndWithdrawal() {
        Account client1 = facade.acceptClientMandate("MANDATE-01");
        ClientMandate m1 = facade.getFirmManager().getMandate("MANDATE-01");
        Money initialHwm = m1.getHighWaterMark();

        // 1. Klien Setor Modal Tambahan Rp 50.000.000 (Capital Inflow)
        Money topup = Money.of(50_000_000, Currency.IDR);
        facade.clientDeposit(m1.getId(), topup);

        assertEquals(Money.of(150_000_000, Currency.IDR), client1.getTotalCash(Currency.IDR));
        // High-Water Mark harus naik setara setoran (agar setoran BUKAN dianggap laba investasi! - FR-08.11)
        assertEquals(initialHwm.add(topup), m1.getHighWaterMark());

        // 2. Klien Tarik Dana Rp 30.000.000 (Capital Outflow)
        Money withdrawAmt = Money.of(30_000_000, Currency.IDR);
        boolean withdrawOk = facade.clientWithdraw(m1.getId(), withdrawAmt);
        assertTrue(withdrawOk);
        assertEquals(Money.of(120_000_000, Currency.IDR), client1.getTotalCash(Currency.IDR));
        assertEquals(initialHwm.add(topup).subtract(withdrawAmt), m1.getHighWaterMark());

        // 3. Penarikan melebihi saldo kas tersedia harus ditolak (tidak ada uang siluman - FR-08.07)
        boolean overWithdraw = facade.clientWithdraw(m1.getId(), Money.of(200_000_000, Currency.IDR));
        assertFalse("Penarikan melebihi saldo kas harus ditolak", overWithdraw);

        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testManagementAndPerformanceFeeWithHWM() {
        Account client1 = facade.acceptClientMandate("MANDATE-01");
        ClientMandate m1 = facade.getFirmManager().getMandate("MANDATE-01");
        Account personal = facade.getPersonalAccount();
        Money initialManagerCash = personal.getTotalCash(Currency.IDR);

        // Kasus 1: Portofolio belum cetak profit baru (AUM <= HWM)
        // Hitung fee: performance fee harus Rp 0!
        FirmManager.FeeResult fees1 = facade.collectClientFees(m1.getId());
        assertTrue(fees1.managementFee.isPositive()); // Management fee tetap ada (prorata harian)
        assertEquals(Money.zero(Currency.IDR), fees1.performanceFee); // Performance fee NOL!

        // Kasus 2: Portofolio mencetak laba bersih di atas HWM
        // Berikan kenaikan nilai investasi saham sebesar Rp 20.000.000
        client1.addPosition(new Position("BBCA", "IDX", Currency.IDR, 2000, BigDecimal.valueOf(10000), BigDecimal.valueOf(20000)));

        FirmManager.FeeResult fees2 = facade.collectClientFees(m1.getId());
        assertTrue("Harus dipungut performance fee saat AUM melampaui HWM", fees2.performanceFee.isPositive());

        // Pastikan pendapatan fee masuk ke kas pribadi manajer
        assertTrue(personal.getTotalCash(Currency.IDR).isGreaterThanOrEqual(initialManagerCash.add(fees2.totalFees)));

        // HWM baru harus naik ke puncak laba bersih setelah fee
        assertTrue(m1.getHighWaterMark().compareTo(m1.getInitialDeposit()) > 0);

        // Kasus 3: Coba pungut fee lagi langsung tanpa ada kenaikan harga baru
        // Performance fee harus kembali Rp 0 karena HWM sudah naik!
        FirmManager.FeeResult fees3 = facade.collectClientFees(m1.getId());
        assertEquals(Money.zero(Currency.IDR), fees3.performanceFee);

        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testManagementFeeCannotBeCollectedTwiceInSameGameDay() {
        Account client = facade.acceptClientMandate("MANDATE-01");
        FirmManager.FeeResult first = facade.collectClientFees("MANDATE-01");
        assertTrue(first.managementFee.isPositive());
        Money clientCash = client.getTotalCash(Currency.IDR);
        Money managerCash = facade.getPersonalAccount().getTotalCash(Currency.IDR);
        int entries = facade.getLedger().getEntries().size();
        FirmManager.FeeResult retry = facade.collectClientFees("MANDATE-01");
        assertEquals("The same daily fee must not debit twice", clientCash, client.getTotalCash(Currency.IDR));
        assertEquals(managerCash, facade.getPersonalAccount().getTotalCash(Currency.IDR));
        assertEquals(entries, facade.getLedger().getEntries().size());
        assertEquals(Money.zero(Currency.IDR), retry.totalFees);
    }

    @Test
    public void testUnpaidFeesCannotBeReportedAsCollected() {
        Account client = facade.acceptClientMandate("MANDATE-01");
        ClientMandate mandate = facade.getFirmManager().getMandate("MANDATE-01");
        client.reserveCash(client.getAvailableCash(Currency.IDR));
        Money clientCash = client.getTotalCash(Currency.IDR);
        Money managerCash = facade.getPersonalAccount().getTotalCash(Currency.IDR);
        Money hwm = mandate.getHighWaterMark();
        int entries = facade.getLedger().getEntries().size();
        FirmManager.FeeResult result = facade.collectClientFees("MANDATE-01");
        assertEquals("No payment must report no collected fee", Money.zero(Currency.IDR), result.totalFees);
        assertEquals(clientCash, client.getTotalCash(Currency.IDR));
        assertEquals(managerCash, facade.getPersonalAccount().getTotalCash(Currency.IDR));
        assertEquals(hwm, mandate.getHighWaterMark());
        assertEquals(entries, facade.getLedger().getEntries().size());
        client.releaseReservedCash(client.getReservedCash(Currency.IDR));
        assertTrue("Unpaid assessment must not block later payment", facade.collectClientFees("MANDATE-01").managementFee.isPositive());
    }

    @Test
    public void testTrustScoreConsequences() {
        Account client1 = facade.acceptClientMandate("MANDATE-01");
        ClientMandate m1 = facade.getFirmManager().getMandate("MANDATE-01");
        int initialTrust = m1.getTrustScore();

        // Coba berulang kali penarikan gagal karena kas kosong untuk menurunkan trust
        for (int i = 0; i < 6; i++) {
            facade.clientWithdraw(m1.getId(), Money.of(500_000_000, Currency.IDR));
        }

        assertTrue(m1.getTrustScore() < initialTrust);
        // Jika trust anjlok di bawah batas toleransi, mandat dihentikan
        if (m1.getTrustScore() < 15) {
            assertEquals(ClientMandate.MandateStatus.TERMINATED, m1.getStatus());
        }
    }

    @Test
    public void testAutomaticClientFeeCollectionOnDayClose() {
        Account client1 = facade.acceptClientMandate("MANDATE-01"); // Rp 100.000.000
        Money initialManagerCash = facade.getPersonalAccount().getTotalCash(Currency.IDR);

        // Majukan jam clock ke 16:01 WIB agar siap tutup hari
        facade.getClock().play();
        facade.getClock().tick((int) java.time.Duration.between(facade.getClock().getCurrentTime(), java.time.LocalTime.of(16, 1)).getSeconds());
        facade.getClock().pause();

        com.tradingglobal3d.core.career.DailySessionReport report = facade.closeDayAndAdvance();
        assertNotNull(report);

        // Verifikasi fee manajemen klien otomatis ditarik dan dikreditkan ke manajer
        boolean hasClientMgmtFee = facade.getLedger().getEntries().stream().anyMatch(e ->
                e.getType() == com.tradingglobal3d.core.economy.LedgerEntryType.MANAGEMENT_FEE &&
                e.getAccountId().equals(facade.getPersonalAccount().getId()));
        assertTrue("Fee manajemen klien harus dikreditkan ke manajer pada penutupan hari", hasClientMgmtFee);

        assertTrue("Kas manajer harus bertambah dari perolehan fee klien",
                facade.getPersonalAccount().getTotalCash(Currency.IDR).compareTo(initialManagerCash) > 0);

        assertTrue(facade.reconcileAll());
    }
}
