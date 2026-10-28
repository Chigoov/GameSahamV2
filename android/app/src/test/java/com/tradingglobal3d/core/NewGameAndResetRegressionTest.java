package com.tradingglobal3d.core;

import com.tradingglobal3d.core.collection.CollectibleItem;
import com.tradingglobal3d.core.collection.CollectionManager;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntry;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.loans.LoanContract;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;
import com.tradingglobal3d.core.trading.OrderType;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Uji Komprehensif Kondisi Awal Game Baru dan Alur Reset (Milestone P1):
 * 1. Pemain baru: aplikasi dibuka tanpa save, memperoleh saldo awal Rp50.000.000, 0 saham, 0 utang, 0 koleksi, katalog valid.
 * 2. Reset permainan: permainan yang telah melakukan transaksi direset total kembali ke Rp50.000.000 tanpa membawa jejak lama.
 * 3. Reset berulang & idempotensi.
 * 4. Simpan, tutup dan restore ulang setelah reset menghasilkan kondisi awal yang bersih.
 */
public class NewGameAndResetRegressionTest {

    private Path tempDir;
    private File saveFile;
    private File backupFile;
    private SaveManager saveManager;
    private InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        tempDir = Files.createTempDirectory("qa_new_game_reset_test_");
        saveManager = new SaveManager(tempDir.toFile(), "save_game_v2.json");
        saveFile = saveManager.getSaveFile();
        backupFile = saveManager.getBackupFile();

        // Muat katalog universe pasar dari aset produksi
        File catalogFile = new File("src/main/assets/data/market_universe.json");
        if (!catalogFile.exists()) catalogFile = new File("android/app/src/main/assets/data/market_universe.json");
        String catalogJson = Files.readString(catalogFile.toPath());
        catalog = InstrumentCatalog.fromJsonString(catalogJson);
    }

    @After
    public void tearDown() throws Exception {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    public void testBrandNewPlayerInitialStateWithoutExistingSave() throws Exception {
        // Pastikan tidak ada berkas simpanan sebelumnya
        assertFalse(saveFile.exists());
        assertFalse(backupFile.exists());

        TradingFacade facade = new TradingFacade(saveManager);
        facade.setCatalog(catalog);

        // Jalankan alur pembukaan pertama kali
        boolean started = facade.startOrResume();
        assertTrue("Game baru harus berhasil dimulai", started);
        assertFalse("Tidak boleh ada permintaan recovery", facade.isRecoveryRequired());

        // 1. Verifikasi Akun Pribadi & Saldo Kas
        Account personal = facade.getPersonalAccount();
        assertNotNull("Akun pribadi harus terbuat", personal);
        assertEquals("ACC-PERSONAL-001", personal.getId());
        assertEquals("Portofolio Pribadi", personal.getName());
        assertEquals(AccountType.PERSONAL, personal.getType());

        Money idrCash = personal.getTotalCash(Currency.IDR);
        assertEquals("Modal awal pemain baru harus tepat Rp 50.000.000", Money.of(50_000_000L, Currency.IDR), idrCash);
        assertEquals(Money.of(50_000_000L, Currency.IDR), personal.getAvailableCash(Currency.IDR));
        assertEquals(Money.zero(Currency.IDR), personal.getReservedCash(Currency.IDR));
        assertEquals(Money.zero(Currency.USD), personal.getTotalCash(Currency.USD));

        // 2. Verifikasi Portofolio Saham Bersih (0 Saham)
        assertTrue("Portofolio saham pemain baru harus kosong", personal.getPositions().isEmpty());

        // 3. Verifikasi Utang & Pinjaman Bersih (0 Pinjaman)
        assertTrue("Tidak boleh ada pinjaman aktif", facade.getInstallmentLoanManager().getAllLoans().isEmpty());
        assertEquals(Money.zero(Currency.IDR), facade.getInstallmentLoanManager().getTotalOutstandingLiability(personal.getId()));

        // 4. Verifikasi Barang Koleksi Bersih (0 Koleksi Dimiliki)
        assertTrue("Koleksi pemain baru harus bernilai 0", facade.getCollectionManager().calculateTotalValuation().isZero());
        assertTrue("Koleksi pemain baru harus tidak ada yang dimiliki", facade.getCollectionManager().getAllItems().stream().noneMatch(CollectibleItem::isOwned));

        // 5. Verifikasi Buku Besar (Ledger) Hanya Berisi Mutasi Modal Awal
        EconomyLedger ledger = facade.getLedger();
        assertTrue(ledger.reconcile(personal, Currency.IDR));
        List<LedgerEntry> entries = ledger.getEntriesForAccount(personal.getId());
        assertEquals("Ledger pemain baru hanya boleh memuat 1 mutasi setoran modal", 1, entries.size());
        assertEquals(LedgerEntryType.INITIAL_CAPITAL, entries.get(0).getType());
        assertEquals(Money.of(50_000_000L, Currency.IDR), entries.get(0).getAmount());

        // 6. Verifikasi Likuiditas Pasar Awal
        assertTrue("Katalog aktif harus memiliki minimal 20 instrumen", catalog.getActiveUniverse().size() >= 20);
        Account mm = facade.getAllAccounts().stream().filter(a -> a.getId().equals("ACC-NPC-MM-01")).findFirst().orElse(null);
        assertNotNull("Market Maker harus ada untuk menyediakan likuiditas", mm);
        assertTrue("Market Maker harus memiliki saldo IDR", mm.getTotalCash(Currency.IDR).isPositive());
    }

    @Test
    public void testResetGameClearsAllProgressAndRestoresCleanInitialState() throws Exception {
        TradingFacade facade = new TradingFacade(saveManager);
        facade.setCatalog(catalog);
        facade.startOrResume();

        Account personal = facade.getPersonalAccount();

        // Lakukan transaksi pada sesi yang sedang berjalan:
        // A. Ajukan pinjaman
        JSONObject quote = facade.getInstallmentLoanManager().getLoanQuote("LOAN-PERS-02", personal, Money.of(10_000_000L, Currency.IDR), 14, null, null);
        facade.getInstallmentLoanManager().disburseReviewedLoan("REQ-LOAN-01", "LOAN-PERS-02", personal, Money.of(10_000_000L, Currency.IDR), 14, null, null, quote.getString("quoteVersion"), facade.getLedger(), facade.getClock().getGameTimestamp());

        // B. Beli barang koleksi
        facade.getCollectionManager().buyItem("REQ-COL-01", "COL-ART-00", personal, facade.getLedger(), null, facade.getClock().getGameTimestamp());

        // C. Berikan posisi saham simulasi
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 500, new BigDecimal("9500"), new BigDecimal("9500")));

        // Verifikasi bahwa data sementara telah terbentuk
        assertFalse(facade.getInstallmentLoanManager().getAllLoans().isEmpty());
        assertTrue(facade.getCollectionManager().getAllItems().stream().anyMatch(CollectibleItem::isOwned));
        assertFalse(personal.getPositions().isEmpty());

        // Simpan ke disk sebelum reset
        facade.save();
        assertTrue(saveFile.exists());

        // EKSEKUSI RESET PERMAINAN (startInternalGame dengan modal Rp50.000.000)
        facade.startInternalGame(BigDecimal.valueOf(50_000_000L));
        facade.save();

        // VERIFIKASI SETELAH RESET:
        Account resetPersonal = facade.getPersonalAccount();
        assertEquals(Money.of(50_000_000L, Currency.IDR), resetPersonal.getTotalCash(Currency.IDR));
        assertEquals(Money.of(50_000_000L, Currency.IDR), resetPersonal.getAvailableCash(Currency.IDR));
        assertEquals(Money.zero(Currency.IDR), resetPersonal.getReservedCash(Currency.IDR));

        // Seluruh posisi saham lama HARUS BERSIH
        assertTrue("Seluruh posisi saham lama harus bersih setelah reset", resetPersonal.getPositions().isEmpty());

        // Seluruh pinjaman/utang lama HARUS BERSIH
        assertTrue("Seluruh pinjaman lama harus bersih setelah reset", facade.getInstallmentLoanManager().getAllLoans().isEmpty());

        // Seluruh barang koleksi lama HARUS BERSIH
        assertTrue("Seluruh koleksi lama harus bersih setelah reset", facade.getCollectionManager().getAllItems().stream().noneMatch(CollectibleItem::isOwned));

        // Buku besar harus seimbang dan hanya berisi 1 mutasi modal awal
        assertTrue(facade.getLedger().reconcile(resetPersonal, Currency.IDR));
        assertEquals(1, facade.getLedger().getEntriesForAccount(resetPersonal.getId()).size());
    }

    @Test
    public void testRepeatedResetAndReloadConsistency() throws Exception {
        TradingFacade facade = new TradingFacade(saveManager);
        facade.setCatalog(catalog);
        facade.startOrResume();

        // Reset berulang 3 kali
        for (int i = 1; i <= 3; i++) {
            facade.startInternalGame(BigDecimal.valueOf(50_000_000L));
            facade.save();
            assertEquals(Money.of(50_000_000L, Currency.IDR), facade.getPersonalAccount().getTotalCash(Currency.IDR));
            assertTrue(facade.getPersonalAccount().getPositions().isEmpty());
            assertTrue(facade.getInstallmentLoanManager().getAllLoans().isEmpty());
        }

        // Tutup facade lama dan buka instance baru (simulasi restart aplikasi setelah reset)
        TradingFacade freshFacade = new TradingFacade(saveManager);
        freshFacade.setCatalog(catalog);
        boolean reloaded = freshFacade.startOrResume();
        assertTrue("Reload setelah reset harus berhasil", reloaded);
        assertFalse(freshFacade.isRecoveryRequired());

        Account reloadedPersonal = freshFacade.getPersonalAccount();
        assertEquals(Money.of(50_000_000L, Currency.IDR), reloadedPersonal.getTotalCash(Currency.IDR));
        assertTrue(reloadedPersonal.getPositions().isEmpty());
        assertTrue(freshFacade.getInstallmentLoanManager().getAllLoans().isEmpty());
        assertTrue(freshFacade.getCollectionManager().getAllItems().stream().noneMatch(CollectibleItem::isOwned));
        assertTrue(freshFacade.getLedger().reconcile(reloadedPersonal, Currency.IDR));
    }
}
