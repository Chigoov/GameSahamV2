package com.tradingglobal3d.core;

import com.tradingglobal3d.core.corporate.CorporateActionService;
import com.tradingglobal3d.core.corporate.IpoOffering;
import com.tradingglobal3d.core.corporate.IpoSubscription;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.research.ResearchService;
import com.tradingglobal3d.core.research.TerminalTier;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.Assert.*;

/**
 * Uji Regresi Mandiri untuk Verifikasi Temuan & Fix Plan 13.
 */
public class Plan13FixesAndAuditsTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testResearchTerminalTierPricingAndAutoRenewalGuard() throws Exception {
        // 1. Verifikasi harga paket Pro USD 250.000 dan Whale USD 1.000.000
        assertEquals(Money.of(250_000.0, Currency.USD), TerminalTier.PRO.getMonthlyFeeUsd());
        assertEquals(Money.of(1_000_000.0, Currency.USD), TerminalTier.ENTERPRISE.getMonthlyFeeUsd());

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

        // 2. Berikan saldo kas USD $600.000 (cukup untuk 2 kali siklus langganan PRO $250k)
        game.deposit("DEP-USD-600K", personal.getId(), Money.of(600_000.0, Currency.USD), "Modal USD test");
        assertEquals(Money.of(600_000.0, Currency.USD), personal.getTotalCash(Currency.USD));
        GameTestSupport.unlockManager(game);

        // 3. Berlangganan PRO
        boolean subOk = game.subscribeTerminal(TerminalTier.PRO);
        assertTrue("Berlangganan PRO harus sukses dengan kas USD cukup", subOk);
        assertEquals(TerminalTier.PRO, service.getCurrentTier());
        assertEquals(Money.of(350_000.0, Currency.USD), personal.getTotalCash(Currency.USD));

        // 4. Simulasi waktu maju 31 hari ke masa depan (melewati subscriptionExpiresAt)
        long expiredTimestamp = now + ResearchService.THIRTY_DAYS_MS + 3600_000L;

        // Panggil hasAccess secara langsung: tier aktif turun sementara ke BASIC
        boolean accessPro = service.hasAccess(TerminalTier.PRO, expiredTimestamp);
        assertFalse("hasAccess harus false karena masa aktif lewat", accessPro);
        assertEquals(TerminalTier.BASIC, service.getCurrentTier());

        // 5. Eksekusi proses auto-renewal: harus mengenali enrolledTier sebelumnya dan memperpanjang otomatis
        service.processDailyAutoRenewal(personal, ledger, expiredTimestamp);
        assertEquals("Tier harus kembali PRO setelah auto-renewal sukses", TerminalTier.PRO, service.getCurrentTier());
        assertTrue("hasAccess harus kembali true setelah perpanjangan", service.hasAccess(TerminalTier.PRO, expiredTimestamp));
        // Kas berkurang lagi $250.000 menjadi $100.000
        assertEquals(Money.of(100_000.0, Currency.USD), personal.getTotalCash(Currency.USD));

        // 6. Simulasi waktu maju lagi 31 hari ke siklus berikutnya (kas sekarang hanya $100k, tidak cukup untuk $250k)
        long secondCycleExpired = expiredTimestamp + ResearchService.THIRTY_DAYS_MS + 3600_000L;
        service.processDailyAutoRenewal(personal, ledger, secondCycleExpired);

        // Paket harus turun bersih ke BASIC tanpa saldo negatif
        assertEquals(TerminalTier.BASIC, service.getCurrentTier());
        assertEquals(Money.of(100_000.0, Currency.USD), personal.getTotalCash(Currency.USD));
        assertFalse("Tidak boleh ada akses premium saat kas kurang", service.hasAccess(TerminalTier.PRO, secondCycleExpired));
    }

    @Test
    public void testAutoRenewalSurvivesExportAndRestoreState() throws Exception {
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

        // 1. Saldo USD $600.000, langganan PRO ($250.000) -> sisa $350.000
        game.deposit("DEP-USD-RELOAD-TEST", personal.getId(), Money.of(600_000.0, Currency.USD), "Modal USD reload");
        GameTestSupport.unlockManager(game);
        assertTrue(game.subscribeTerminal(TerminalTier.PRO));
        assertEquals(Money.of(350_000.0, Currency.USD), personal.getTotalCash(Currency.USD));
        assertEquals(TerminalTier.PRO, service.getEnrolledTier());

        // 2. Ekspor state ke JSONObject
        JSONObject exported = service.exportState();
        assertTrue("State harus memuat enrolledTier", exported.has("enrolledTier"));
        assertEquals("PRO", exported.getString("enrolledTier"));

        // 3. Pulihkan ke instance ResearchService baru
        ResearchService restoredService = new ResearchService();
        restoredService.restoreState(exported);
        assertEquals(TerminalTier.PRO, restoredService.getEnrolledTier());
        assertEquals(TerminalTier.PRO, restoredService.getCurrentTier());

        // 4. Simulasi waktu jatuh tempo diperiksa sebelum settlement (misal via hasAccess)
        long expiredTime = now + ResearchService.THIRTY_DAYS_MS + 3600_000L;
        assertFalse("Akses expired sebelum settlement", restoredService.hasAccess(TerminalTier.PRO, expiredTime));
        assertEquals(TerminalTier.BASIC, restoredService.getCurrentTier());
        assertEquals("Enrolled tier harus tetap PRO meskipun access turun sementara", TerminalTier.PRO, restoredService.getEnrolledTier());

        // 5. Jalankan auto-renewal pada instance yang dipulihkan
        restoredService.processDailyAutoRenewal(personal, ledger, expiredTime);

        // 6. Paket harus sukses kembali ke PRO dan sisa saldo menjadi $100.000
        assertEquals("Paket harus kembali PRO setelah restore dan auto-renewal", TerminalTier.PRO, restoredService.getCurrentTier());
        assertEquals(Money.of(100_000.0, Currency.USD), personal.getTotalCash(Currency.USD));
        assertTrue(restoredService.hasAccess(TerminalTier.PRO, expiredTime));
    }

    @Test
    public void testIpoWindowThreeDaysOfferingBoundary() {
        CorporateActionService cs = new CorporateActionService();
        cs.reset(LocalDate.of(2026, 11, 1));

        IpoOffering ipo = new IpoOffering(
                "IPO-PLAN13", "TEST", "PT Test Listing Tbk",
                Currency.IDR, Money.of(1000, Currency.IDR), 100, 100_000L,
                LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 4),
                LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 5)
        );
        cs.registerIpoOffering(ipo);

        Account acc = new Account("ACC-USER-IPO", "Investor", AccountType.PERSONAL, null);
        acc.creditCash(Money.of(10_000_000L, Currency.IDR));

        long tBefore = LocalDate.of(2026, 11, 1).atStartOfDay(ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli();
        long tDuring = LocalDate.of(2026, 11, 3).atStartOfDay(ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli();
        long tAfter = LocalDate.of(2026, 11, 5).atStartOfDay(ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli();

        // 1. Pesan sebelum buka -> ditolak
        try {
            cs.orderIpo(ipo, acc, 10, "REQ-BEFORE", tBefore);
            fail("Pemesanan sebelum masa penawaran harus ditolak");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("belum dibuka"));
        }

        // 2. Pesan saat masa penawaran (3 hari game) -> sukses
        IpoSubscription sub = cs.orderIpo(ipo, acc, 10, "REQ-DURING", tDuring);
        assertNotNull(sub);
        assertEquals(10, sub.getOrderedLots());
        assertEquals(Money.of(1_000_000L, Currency.IDR), acc.getReservedCash(Currency.IDR));

        // 3. Pesan setelah masa penawaran tutup -> ditolak
        try {
            cs.orderIpo(ipo, acc, 5, "REQ-AFTER", tAfter);
            fail("Pemesanan setelah masa penawaran tutup harus ditolak");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("sudah ditutup"));
        }
    }

    @Test
    public void testNpcDualRankAndRealPositions() throws Exception {
        SaveManager saves = SaveManager.forDirectory(temp.newFolder());
        TradingFacade game = new TradingFacade(saves);
        File catFile = new File("android/app/src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("src/main/assets/data/market_universe.json");
        game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(catFile.toPath())));
        game.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));

        // Periksa profil NPC #1 (Hartono W.) dan NPC #2 (Banyu Karang)
        JSONObject p1 = game.getNpcProfile("NPC-01");
        assertTrue(p1.getBoolean("success"));
        assertEquals(1, p1.getInt("netWorthRank"));
        assertTrue(p1.has("weeklyReturnRank"));
        assertTrue("Hartono harus punya posisi saham riil", p1.getJSONArray("positions").length() > 0);

        JSONObject p2 = game.getNpcProfile("NPC-02");
        assertTrue(p2.getBoolean("success"));
        assertEquals(2, p2.getInt("netWorthRank"));
        assertTrue(p2.has("weeklyReturnRank"));
        assertTrue("Banyu Karang harus punya posisi saham riil", p2.getJSONArray("positions").length() > 0);
        assertNotEquals("Valuasi saham tidak boleh Rp 0", "Rp 0", p2.getString("stockValuation"));
    }

    @Test
    public void testAonMultiOpponentSkippingNotPartiallyFilled() {
        Money price = Money.of(1000, Currency.IDR);
        com.tradingglobal3d.core.market.Instrument inst = new com.tradingglobal3d.core.market.Instrument(
                "IDX:TEST", "TEST", "Test", com.tradingglobal3d.core.market.MarketExchange.IDX,
                "Test", Currency.IDR, 100, 1000000, true, price
        );
        Account npc = new Account("npc", "NPC", AccountType.FIRM, null);
        Account client = new Account("client", "Client", AccountType.CLIENT, null);
        Account player = new Account("player", "Player", AccountType.PERSONAL, null);

        npc.addPosition(new Position("TEST", "IDX", Currency.IDR, 100, new BigDecimal("1000"), new BigDecimal("1000")));
        client.addPosition(new Position("TEST", "IDX", Currency.IDR, 300, new BigDecimal("1000"), new BigDecimal("1000")));
        player.creditCash(Money.of(10_000_000L, Currency.IDR));

        java.util.Map<String, Account> accounts = new java.util.HashMap<>();
        accounts.put(npc.getId(), npc);
        accounts.put(client.getId(), client);
        accounts.put(player.getId(), player);

        com.tradingglobal3d.core.trading.OrderBook book = new com.tradingglobal3d.core.trading.OrderBook(inst.getId(), price);
        com.tradingglobal3d.core.trading.MatchingEngine engine = new com.tradingglobal3d.core.trading.MatchingEngine();
        EconomyLedger ledger = new EconomyLedger();

        com.tradingglobal3d.core.trading.Order n = com.tradingglobal3d.core.trading.Order.createLimit("n", inst.getId(), npc.getId(), com.tradingglobal3d.core.trading.OrderSide.SELL, price, 100, 1);
        com.tradingglobal3d.core.trading.Order c = com.tradingglobal3d.core.trading.Order.createLimit("c", inst.getId(), client.getId(), com.tradingglobal3d.core.trading.OrderSide.SELL, price, 300, 2);
        com.tradingglobal3d.core.trading.Order p = com.tradingglobal3d.core.trading.Order.createLimit("p", inst.getId(), player.getId(), com.tradingglobal3d.core.trading.OrderSide.BUY, price, 300, 3);

        engine.submitOrder(n, npc, inst, book, ledger, accounts, 1);
        engine.submitOrder(c, client, inst, book, ledger, accounts, 2);

        com.tradingglobal3d.core.trading.MatchingEngine.OrderResult res = engine.submitOrder(p, player, inst, book, ledger, accounts, 3);
        assertTrue(res.isSuccess);
        assertEquals(com.tradingglobal3d.core.trading.OrderStatus.FILLED, p.getStatus());
        assertEquals(300, p.getFilledQuantity());
        assertEquals(0, p.getRemainingQuantity());
        // Pemain tidak boleh mengalami partial fill (terisi 100 lot lalu sisa 200 lot)
        assertEquals(300, player.getPosition(inst.getId()).getQuantity());
    }
}
