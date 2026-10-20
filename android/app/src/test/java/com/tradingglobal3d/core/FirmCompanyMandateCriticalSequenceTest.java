package com.tradingglobal3d.core;

import com.tradingglobal3d.core.business.Employee;
import com.tradingglobal3d.core.business.OperatingCompany;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.firm.ClientMandate;
import com.tradingglobal3d.core.firm.FirmManager;
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
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Pengujian Kritis Standar A3 untuk Phase 07 (PHASE-QUALITY-STANDARDS.md):
 * Minimal 10 variasi urutan yang dapat direproduksi, masing-masing minimal 100 aksi bermakna,
 * dengan invariant diperiksa setelah SETIAP aksi.
 * Cakupan: Firma investasi, akun klien terisolasi, HWM, fee manajemen/performa,
 * pendirian perusahaan operasional, payroll pegawai, pendapatan operasional,
 * kebijakan kerja, dan rekonsiliasi pembukuan (ledger) serta save/load roundtrip.
 */
public class FirmCompanyMandateCriticalSequenceTest {
    private File testDir;
    private SaveManager saveManager;
    private InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "firm_comp_crit_seq_" + System.currentTimeMillis());
        testDir.mkdirs();
        saveManager = SaveManager.forDirectory(testDir);

        File universeFile = new File("src/main/assets/data/market_universe.json");
        if (!universeFile.exists()) universeFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (universeFile.exists()) {
            String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
            catalog = InstrumentCatalog.fromJsonString(jsonUniverse);
        }
    }

    @After
    public void tearDown() {
        deleteDir(testDir);
    }

    private void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) for (File f : files) deleteDir(f);
        }
        dir.delete();
    }

    private void assertInvariants(TradingFacade facade, String context) {
        // 1. Audit ledger double-entry rekonsiliasi seluruh akun
        assertTrue("Audit ledger rekonsiliasi gagal: " + context, facade.reconcileAll());

        Account personal = facade.getPersonalAccount();
        assertNotNull("Personal account wajib ada: " + context, personal);

        // 2. Invariant seluruh akun (Personal, Client, Company)
        for (Account acc : facade.getAllAccounts()) {
            for (Currency c : Currency.values()) {
                Money tot = acc.getTotalCash(c);
                Money av = acc.getAvailableCash(c);
                Money res = acc.getReservedCash(c);
                assertEquals("Available + Reserved harus sama dengan Total Cash (" + acc.getId() + " - " + c + "): " + context,
                        tot, av.add(res));
                assertFalse("Saldo kas tidak boleh negatif (" + acc.getId() + " - " + c + "): " + context,
                        tot.isNegative());
            }
        }
    }

    private void run100FirmCompanyActionsForSeed(long seed, int sequenceNumber) throws Exception {
        TradingFacade facade = new TradingFacade(saveManager);
        if (catalog != null) facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(250_000_000));
        GameTestSupport.unlockManager(facade);

        Random rnd = new Random(seed);
        String pId = facade.getPersonalAccount().getId();
        facade.getClock().play();

        int actionsCount = 0;
        int step = 0;

        // Siapkan mandat awal
        facade.acceptClientMandate("MANDATE-01");
        facade.foundCompany("COMP-" + seed, "PT Maju Bersama " + seed, "Teknologi Finansial", Money.of(30_000_000, Currency.IDR));
        actionsCount += 2;

        while (actionsCount < 100) {
            step++;
            int op = rnd.nextInt(10);
            String ctx = String.format("Seq %d (Seed %d) Step %d Op %d", sequenceNumber, seed, step, op);

            switch (op) {
                case 0: { // Client Deposit (Capital Inflow)
                    ClientMandate m = facade.getFirmManager().getMandate("MANDATE-01");
                    if (m != null) {
                        long amt = 5_000_000 + rnd.nextInt(20_000_000);
                        facade.clientDeposit(m.getId(), Money.of(amt, Currency.IDR));
                        actionsCount++;
                    }
                    break;
                }
                case 1: { // Client Withdrawal (Capital Outflow)
                    ClientMandate m = facade.getFirmManager().getMandate("MANDATE-01");
                    if (m != null) {
                        Account acc = facade.getAllAccounts().stream()
                                .filter(a -> a.getId().equals(m.getClientAccountId())).findFirst().orElse(null);
                        if (acc != null && acc.getAvailableCash(Currency.IDR).compareTo(Money.of(10_000_000, Currency.IDR)) > 0) {
                            long amt = 1_000_000 + rnd.nextInt(5_000_000);
                            boolean ok = facade.clientWithdraw(m.getId(), Money.of(amt, Currency.IDR));
                            assertTrue("Penarikan klien yang valid harus sukses", ok);
                            actionsCount++;
                        }
                    }
                    break;
                }
                case 2: { // Pungut Fee Klien (Management & Performance fee)
                    ClientMandate m = facade.getFirmManager().getMandate("MANDATE-01");
                    if (m != null) {
                        FirmManager.FeeResult fees = facade.collectClientFees(m.getId());
                        assertNotNull(fees);
                        actionsCount++;
                    }
                    break;
                }
                case 3: { // Transfer Modal: Personal -> Perusahaan
                    long avail = facade.getPersonalAccount().getAvailableCash(Currency.IDR).getAmount().longValue();
                    if (avail > 10_000_000) {
                        long amt = 1_000_000 + rnd.nextInt(5_000_000);
                        facade.transferCompanyCapital("COMP-" + seed, true, Money.of(amt, Currency.IDR));
                        actionsCount++;
                    }
                    break;
                }
                case 4: { // Transfer Modal: Perusahaan -> Personal (Dividen Usaha)
                    var comp = facade.getCompanyManager().getCompany("COMP-" + seed);
                    if (comp != null) {
                        Account compAcc = facade.getAllAccounts().stream()
                                .filter(a -> a.getId().equals(comp.getAccountId())).findFirst().orElse(null);
                        if (compAcc != null && compAcc.getAvailableCash(Currency.IDR).compareTo(Money.of(15_000_000, Currency.IDR)) > 0) {
                            long amt = 1_000_000 + rnd.nextInt(5_000_000);
                            facade.transferCompanyCapital("COMP-" + seed, false, Money.of(amt, Currency.IDR));
                            actionsCount++;
                        }
                    }
                    break;
                }
                case 5: { // Ubah Kebijakan Kerja & Benefit Perusahaan
                    var comp = facade.getCompanyManager().getCompany("COMP-" + seed);
                    if (comp != null) {
                        int[] hours = {6, 8, 10};
                        OperatingCompany.BenefitTier[] tiers = OperatingCompany.BenefitTier.values();
                        int h = hours[rnd.nextInt(hours.length)];
                        var t = tiers[rnd.nextInt(tiers.length)];
                        comp.setWorkPolicy(h, t);
                        assertEquals(h, comp.getWorkHours());
                        assertEquals(t, comp.getBenefitTier());
                        actionsCount++;
                    }
                    break;
                }
                case 6: { // Rekrut Pegawai Baru Perusahaan
                    var comp = facade.getCompanyManager().getCompany("COMP-" + seed);
                    if (comp != null && comp.getEmployees().size() < 6) {
                        String empId = "EMP-" + seed + "-" + step;
                        Employee.Role[] roles = Employee.Role.values();
                        Employee.Role r = roles[rnd.nextInt(roles.length)];
                        comp.addEmployee(new Employee(empId, "Pegawai " + empId, r, 1));
                        assertNotNull(comp.getEmployee(empId));
                        actionsCount++;
                    }
                    break;
                }
                case 7: { // Advance time (memicu payroll harian & pendapatan operasional saat lewat tengah malam)
                    facade.tick(60);
                    actionsCount++;
                    break;
                }
                case 8: { // Boundary: Tolak penarikan klien melebihi saldo
                    ClientMandate m = facade.getFirmManager().getMandate("MANDATE-01");
                    if (m != null) {
                        boolean over = facade.clientWithdraw(m.getId(), Money.of(1_000_000_000L, Currency.IDR));
                        assertFalse("Over-withdrawal klien harus ditolak", over);
                        actionsCount++;
                    }
                    break;
                }
                case 9: { // Save dan Reload roundtrip
                    facade.save();
                    TradingFacade reloaded = new TradingFacade(saveManager);
                    if (catalog != null) reloaded.setCatalog(catalog);
                    assertTrue("Load harus berhasil pada " + ctx, reloaded.load());
                    assertInvariants(reloaded, ctx + " [Reloaded]");
                    assertNotNull("Perusahaan harus tersimpan", reloaded.getCompanyManager().getCompany("COMP-" + seed));
                    facade = reloaded;
                    actionsCount++;
                    break;
                }
            }

            // Invariant wajib diaudit setelah SETIAP aksi
            assertInvariants(facade, ctx);
        }

        assertTrue("Minimal 100 aksi bermakna harus tercapai", actionsCount >= 100);
    }

    @Test
    public void testFirmCompanySequence01() throws Exception { run100FirmCompanyActionsForSeed(7001L, 1); }

    @Test
    public void testFirmCompanySequence02() throws Exception { run100FirmCompanyActionsForSeed(7002L, 2); }

    @Test
    public void testFirmCompanySequence03() throws Exception { run100FirmCompanyActionsForSeed(7003L, 3); }

    @Test
    public void testFirmCompanySequence04() throws Exception { run100FirmCompanyActionsForSeed(7004L, 4); }

    @Test
    public void testFirmCompanySequence05() throws Exception { run100FirmCompanyActionsForSeed(7005L, 5); }

    @Test
    public void testFirmCompanySequence06() throws Exception { run100FirmCompanyActionsForSeed(7006L, 6); }

    @Test
    public void testFirmCompanySequence07() throws Exception { run100FirmCompanyActionsForSeed(7007L, 7); }

    @Test
    public void testFirmCompanySequence08() throws Exception { run100FirmCompanyActionsForSeed(7008L, 8); }

    @Test
    public void testFirmCompanySequence09() throws Exception { run100FirmCompanyActionsForSeed(7009L, 9); }

    @Test
    public void testFirmCompanySequence10() throws Exception { run100FirmCompanyActionsForSeed(7010L, 10); }
}
