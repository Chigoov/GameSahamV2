package com.tradingglobal3d.core;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.research.NewsArticle;
import com.tradingglobal3d.core.research.ResearchService;
import com.tradingglobal3d.core.research.TerminalTier;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.MatchingEngine;
import com.tradingglobal3d.core.trading.Order;
import com.tradingglobal3d.core.trading.OrderSide;
import com.tradingglobal3d.core.trading.OrderStatus;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;

import static org.junit.Assert.*;

/**
 * Pengujian Terarah Gate 2 (G2) Plan 14:
 * Ekonomi, Rekonsiliasi 24x, Auto-Renewal, Logika Warta & Recovery QA.
 */
public class Plan14GateTwoEconomicAndRecoveryTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testGateTwoOfflineCatchupAndBypassWhenZeroEvents() throws Exception {
        SaveManager saves = SaveManager.forDirectory(temp.newFolder());
        TradingFacade game = new TradingFacade(saves);
        File catFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("src/main/assets/data/market_universe.json");
        game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(catFile.toPath())));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));

        // 1. Simpan kondisi awal
        game.save();

        // 2. Majukan waktu secara offline tanpa mutasi
        JSONObject reportZero = game.reconcileOfflineElapsed(1000); // 1 detik nyata
        assertNotNull(reportZero);
        assertEquals(0, reportZero.optInt("transactionsCount", 0));

        // 3. Rekonsiliasi offline 1 jam nyata (3.600.000 ms) = 1 hari game 24x
        JSONObject reportWithDay = game.reconcileOfflineElapsed(3600_000L);
        assertNotNull(reportWithDay);
        assertTrue(reportWithDay.optLong("gameSecondsAdvanced", 0) >= 86400);
    }

    @Test
    public void testGateTwoCorruptSaveRecoveryFromBackup() throws Exception {
        File saveDir = temp.newFolder();
        SaveManager saves = SaveManager.forDirectory(saveDir);
        TradingFacade game = new TradingFacade(saves);
        File catFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("src/main/assets/data/market_universe.json");
        game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(catFile.toPath())));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));

        // Simpan save valid pertama
        game.save();
        File saveFile = saves.getSaveFile();
        File backupFile = saves.getBackupFile();
        assertTrue("Save file utama harus ada", saveFile.exists());

        // Simpan kedua untuk memicu rotasi backup
        game.getPersonalAccount().creditCash(Money.of(1_000_000, Currency.IDR));
        game.save();
        assertTrue("Backup file harus terbentuk setelah save kedua", backupFile.exists());

        // Sengaja merusak file save utama (korupsi JSON)
        Files.writeString(saveFile.toPath(), "{ CORRUPT_JSON_CONTENT !!!");

        // 1. Muat ulang pada instance baru: mendeteksi korupsi primer dan menawarkan pemulihan backup
        TradingFacade reloaded = new TradingFacade(SaveManager.forDirectory(saveDir));
        reloaded.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(catFile.toPath())));
        boolean loadOk = reloaded.load();
        assertFalse("Cadangan tidak boleh diterapkan sebelum konfirmasi", loadOk);
        assertTrue("Harus menawarkan pemulihan cadangan setelah korupsi primer (Butir 10 & 35)", reloaded.hasPendingBackupRecoveryOffer());
        assertNull("Akun belum boleh aktif sebelum konfirmasi", reloaded.getPersonalAccount());
        assertThrows(java.io.IOException.class, () -> reloaded.save());
        assertEquals("{ CORRUPT_JSON_CONTENT !!!", Files.readString(saveFile.toPath()));

        JSONObject offer = reloaded.getPendingBackupRecoveryOffer();
        assertNotNull(offer);
        assertTrue(offer.getBoolean("primaryDamaged"));
        assertTrue(offer.has("backupLastModified"));
        assertTrue(offer.has("riskExplanation"));

        // 2. Pemain mengonfirmasi pemulihan
        reloaded.confirmBackupRecovery();
        assertFalse(reloaded.hasPendingBackupRecoveryOffer());
        assertNotNull("Akun personal harus pulih", reloaded.getPersonalAccount());
        assertEquals("Saldo kas harus sesuai backup terakhir (50 juta)", Money.of(50_000_000, Currency.IDR), reloaded.getPersonalAccount().getTotalCash(Currency.IDR));

        // 3. Skenario: Pemain menolak pemulihan cadangan
        Files.writeString(saveFile.toPath(), "{ CORRUPT_JSON_CONTENT !!!");
        TradingFacade rejectedFacade = new TradingFacade(SaveManager.forDirectory(saveDir));
        rejectedFacade.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(catFile.toPath())));
        rejectedFacade.load();
        assertTrue(rejectedFacade.hasPendingBackupRecoveryOffer());
        rejectedFacade.rejectBackupRecovery();
        assertFalse(rejectedFacade.hasPendingBackupRecoveryOffer());
        assertNull("State live harus dibersihkan saat pemulihan ditolak", rejectedFacade.getPersonalAccount());
        assertEquals("{ CORRUPT_JSON_CONTENT !!!", Files.readString(saveFile.toPath()));

        // 4. Skenario: Save utama dan backup keduanya rusak
        Files.writeString(backupFile.toPath(), "{ CORRUPT_BACKUP_CONTENT !!!");
        TradingFacade allCorruptFacade = new TradingFacade(SaveManager.forDirectory(saveDir));
        allCorruptFacade.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(catFile.toPath())));
        assertFalse("Load harus gagal jika primary dan backup keduanya rusak", allCorruptFacade.load());
        assertFalse("Tidak boleh ada penawaran jika backup rusak", allCorruptFacade.hasPendingBackupRecoveryOffer());
    }

    @Test
    public void testGateTwoAutoRenewalCancellationKeepsActiveAccess() throws Exception {
        SaveManager saves = SaveManager.forDirectory(temp.newFolder());
        TradingFacade game = new TradingFacade(saves);
        File catFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("src/main/assets/data/market_universe.json");
        game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(catFile.toPath())));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));

        Account personal = game.getPersonalAccount();
        EconomyLedger ledger = game.getLedger();
        ResearchService service = game.getResearchService();
        long now = game.getClock().getGameTimestamp();

        // 1. Berlangganan Pro $250.000 USD
        game.deposit("DEP-USD-CANCEL-TEST", personal.getId(), Money.of(500_000.0, Currency.USD), "Modal USD");
        GameTestSupport.unlockManager(game);
        assertTrue(game.subscribeTerminal(TerminalTier.PRO));
        assertEquals(TerminalTier.PRO, service.getCurrentTier());

        // 2. Pemain menghentikan perpanjangan otomatis via cancelAutoRenewal (butir 18 & 53)
        boolean cancelOk = game.cancelTerminalAutoRenewal();
        assertTrue("Penghentian auto-renewal harus sukses", cancelOk);
        assertFalse("Auto-renewal harus nonaktif", service.isAutoRenewalEnabled());
        assertEquals(TerminalTier.BASIC, service.getEnrolledTier());
        TradingFacade reloadedGame = new TradingFacade(SaveManager.forDirectory(saves.getSaveFile().getParentFile()));
        reloadedGame.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(catFile.toPath())));
        assertTrue("Pembatalan tersimpan untuk reload", reloadedGame.load());
        assertFalse("Pembatalan bertahan setelah reload save", reloadedGame.getResearchService().isAutoRenewalEnabled());
        assertEquals(TerminalTier.PRO, reloadedGame.getResearchService().getCurrentTier());

        // 3. Masa aktif belum habis (hari ke-15 & hari ke-29): akses PRO harus tetap aktif penuh (butir 54)
        long day15 = now + (15L * 86400_000L);
        assertTrue("Akses PRO harus tetap aktif pada hari ke-15 setelah pembatalan", service.hasAccess(TerminalTier.PRO, day15));
        assertEquals(TerminalTier.PRO, service.getCurrentTier());

        long day29 = now + (29L * 86400_000L);
        assertTrue("Akses PRO harus tetap aktif pada hari ke-29", service.hasAccess(TerminalTier.PRO, day29));

        // 4. Status penghentian tetap benar setelah save dan reload
        JSONObject exported = service.exportState();
        assertFalse("State ekspor harus mencatat autoRenewal nonaktif", exported.getBoolean("autoRenewal"));
        ResearchService reloadedService = new ResearchService();
        reloadedService.restoreState(exported);
        assertFalse("State restore harus mempertahankan autoRenewal nonaktif", reloadedService.isAutoRenewalEnabled());
        assertEquals(TerminalTier.BASIC, reloadedService.getEnrolledTier());
        assertTrue("State restore tetap memberi akses PRO sebelum jatuh tempo", reloadedService.hasAccess(TerminalTier.PRO, day15));

        // Settlement may run before any access check at expiration.
        Money beforeSettlement = personal.getTotalCash(Currency.USD);
        reloadedService.processDailyAutoRenewal(personal, ledger, now + ResearchService.THIRTY_DAYS_MS);
        assertEquals("Pembatalan tidak boleh ditagih saat settlement lebih dulu", beforeSettlement, personal.getTotalCash(Currency.USD));
        assertFalse("Settlement tidak boleh mengaktifkan ulang perpanjangan", reloadedService.isAutoRenewalEnabled());

        // 5. Setelah masa berlaku berakhir (hari ke-31): akses otomatis turun ke Basic tanpa debit perpanjangan
        long day31 = now + (31L * 86400_000L);
        assertFalse("Akses PRO harus terkunci setelah 30 hari berakhir", reloadedService.hasAccess(TerminalTier.PRO, day31));
        assertEquals(TerminalTier.BASIC, reloadedService.getCurrentTier());

        // Eksekusi settlement harian auto-renewal: saldo USD tidak boleh terpotong lagi
        Money cashBeforeRenewal = personal.getTotalCash(Currency.USD);
        reloadedService.processDailyAutoRenewal(personal, ledger, day31);
        assertEquals("Tidak boleh ada potongan kas USD setelah pembatalan perpanjangan", cashBeforeRenewal, personal.getTotalCash(Currency.USD));
        assertEquals(TerminalTier.BASIC, reloadedService.getCurrentTier());
    }

    @Test
    public void testGateTwoMarketClosedOrderCancellationReleasesReserve() throws Exception {
        SaveManager saves = SaveManager.forDirectory(temp.newFolder());
        TradingFacade game = new TradingFacade(saves);
        File catFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("src/main/assets/data/market_universe.json");
        game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(catFile.toPath())));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));

        Account player = game.getPersonalAccount();
        Instrument inst = game.getCatalog().getById("IDX:BBCA");
        assertNotNull(inst);

        // Pasang jam pada 20:00 WIB (pasar tutup)
        game.getClock().pause();

        // Ajukan order beli limit 100 lembar (1 lot) saat pasar tutup
        Money initialAvail = player.getAvailableCash(Currency.IDR);
        Order order = Order.createLimit("ORD-CLOSED-CANCEL", inst.getId(), player.getId(), OrderSide.BUY, inst.getCurrentPrice(), 100, game.getClock().getGameTimestamp());
        MatchingEngine.OrderResult res = game.submitOrder(order);
        assertTrue(res.isSuccess);
        assertEquals(OrderStatus.OPEN, order.getStatus());

        // Dana harus tertahan seketika
        Money reserved = player.getReservedCash(Currency.IDR);
        assertTrue("Dana harus tertahan", reserved.isPositive());
        assertEquals(initialAvail.subtract(reserved), player.getAvailableCash(Currency.IDR));

        // Batalkan order sebelum bursa buka (butir 61)
        boolean cancelOk = game.cancelOrder(order);
        assertTrue("Pembatalan order pasar tutup harus sukses", cancelOk);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        // Dana tertahan harus dilepas seketika kembali ke kas bebas (butir 61)
        assertEquals(Money.zero(Currency.IDR), player.getReservedCash(Currency.IDR));
        assertEquals(initialAvail, player.getAvailableCash(Currency.IDR));
    }

    @Test
    public void testGateTwoSignedNewsImpactAndIdxTickRoundingAraConflict() {
        ResearchService service = new ResearchService();
        long now = 1792373400000L;
        service.resetForGame(now);

        // 1. Audit Q09: Dua berita pada emiten yang sama
        NewsArticle newsA = new NewsArticle("NEWS-A", "BBCA Ekspansi Cabang Digital", "Ekspansi...",
                NewsArticle.Category.COMPANY, "QA-RECENCY", 0.04, now); // +4% pada t=now
        NewsArticle newsB = new NewsArticle("NEWS-B", "BBCA Hadapi Gugatan Pajak", "Gugatan...",
                NewsArticle.Category.COMPANY, "QA-RECENCY", -0.02, now + 3600_000L); // -2% pada t=now+1jam

        service.addNews(newsA);
        service.addNews(newsB);

        // Saat hanya newsA yang terbit (t=now)
        assertEquals(0.04, service.getActiveNewsImpact("QA-RECENCY", "Keuangan", now + 1000L), 0.0001);

        // Kedua efek aktif dijumlahkan bertanda: +4% -2% = +2%.
        assertEquals(0.02, service.getActiveNewsImpact("QA-RECENCY", "Keuangan", now + 3600_000L + 1000L), 0.0001);

        // 2. Audit Pembulatan Fraksi IDX (Butir 48): Saham BBCA Rp 10.250 (> 5.000 -> fraksi Rp 25)
        Money basePrice = Money.of(10250, Currency.IDR);
        Instrument bbca = new Instrument("IDX:BBCA", "BBCA", "Bank Central Asia",
                com.tradingglobal3d.core.market.MarketExchange.IDX, "Keuangan",
                Currency.IDR, 100, 10_000_000L, true, basePrice);

        assertEquals(BigDecimal.valueOf(25), bbca.getIdxTickSize(basePrice.getAmount()));

        // Target kenaikan 2.5%: 10.250 * 1.025 = 10.506,25 -> dibulatkan ke atas kelipatan 25 = 10.525
        BigDecimal targetPrice = basePrice.getAmount().multiply(new BigDecimal("1.025"));
        Money roundedPrice = bbca.roundUpToIdxTick(targetPrice);
        assertEquals(Money.of(10525, Currency.IDR), roundedPrice);

        // 3. Audit Batas Harian 25% vs Target Kenaikan Whale > 20% (Contoh Rumor Lanjutan 42%)
        // Saham harga Rp 10.000: Batas harian 25% adalah Rp 12.500 (T03)
        Money price10k = Money.of(10000, Currency.IDR);
        Instrument stock10k = new Instrument("IDX:TEST10K", "TEST", "Test ARA",
                com.tradingglobal3d.core.market.MarketExchange.IDX, "Keuangan",
                Currency.IDR, 100, 10_000_000L, true, price10k);

        // Target rumor lanjutan 42%: 10.000 * 1.42 = 14.200
        BigDecimal target42Pct = price10k.getAmount().multiply(new BigDecimal("1.42"));
        Money roundedTarget = stock10k.roundUpToIdxTick(target42Pct);
        assertEquals(Money.of(14200, Currency.IDR), roundedTarget);

        // Batas kenaikan harian 25% (T03): 10.000 * 1.25 = 12.500
        Money dayCeiling = stock10k.getDailyCeilingPrice();
        assertEquals(Money.of(12500, Currency.IDR), dayCeiling);
        assertTrue("Target kenaikan 42% (14.200) melebihi batas 25% harian (12.500) sehingga butuh multi-hari",
                roundedTarget.compareTo(dayCeiling) > 0);

        // Clamping updatePrice memastikan harga sesi hari ini tidak melanggar batas 25%
        stock10k.updatePrice(roundedTarget, 100);
        assertEquals("Harga sesi hari ini tetap dibatasi batas 25% harian (Rp 12.500)", dayCeiling, stock10k.getCurrentPrice());
    }
}
