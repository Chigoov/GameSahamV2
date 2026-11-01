package com.tradingglobal3d.core;

import com.tradingglobal3d.core.corporate.CorporateActionService;
import com.tradingglobal3d.core.corporate.IpoOffering;
import com.tradingglobal3d.core.corporate.IpoSubscription;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.league.LeagueCompetitor;
import com.tradingglobal3d.core.league.LeagueManager;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Suite Pengujian Khusus Perbaikan Temuan Review APK-10 (REV-01 s/d REV-06):
 * 1. REV-01: Eliminasi eksekusi parsial saat order pemain mengantre di buku order.
 * 2. REV-02: Migrasi save 20 NPC canonical lama ke 1.000 NPC tanpa exception bindMarket.
 * 3. REV-03: Penjatahan IPO kurang peminat (undersubscribed) 100% terpenuhi.
 * 4. REV-04: Pembulatan 60:40 kuota awal IPO dengan metode pecahan terbesar (7:4 pada 11 lot).
 * 5. REV-06: Pembatalan pemesanan IPO sebelum tanggal listing melepaskan reservasi dana.
 */
public class Apk10IndependentReviewFixesTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testRev01RestingPlayerOrderIsNotPartiallyFilledBySmallerOpponent() throws Exception {
        SaveManager saves = SaveManager.forDirectory(temp.newFolder());
        TradingFacade g = new TradingFacade(saves);
        File catFile = new File("src/main/assets/data/market_universe.json");
        if (!catFile.exists()) catFile = new File("android/app/src/main/assets/data/market_universe.json");
        g.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(catFile.toPath())));
        g.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));
        g.getClock().advanceGameSeconds(1800); // 09:00 WIB (Bursa buka)
        // This case controls counterparties; remove only this QA facade's seeded market books.
        // No player save or production state is involved.
        g.getMarketEngine().reset();

        Account buyer = g.getPersonalAccount();
        // A fresh QA seller has no own bids in the seeded market book.
        Account seller = new Account("QA-SELLER", "QA Seller", AccountType.FIRM, null);
        seller.addPosition(new com.tradingglobal3d.core.model.Position("BBCA", "IDX", Currency.IDR, 400,
                new BigDecimal("9000"), new BigDecimal("9000")));
        g.registerAccount(seller);

        // 1. Pemain memasang order beli 300 saham di harga 9.000
        Order buy = Order.createLimit("AUDIT-PLAYER-BUY", "IDX:BBCA", buyer.getId(), OrderSide.BUY, Money.of(9000, Currency.IDR), 300, g.getGameTimestamp());
        assertTrue(g.submitOrder(buy).isSuccess);
        assertEquals(OrderStatus.OPEN, buy.getStatus());
        assertEquals(0, buy.getFilledQuantity());
        assertEquals(300, buy.getRemainingQuantity());

        // 2. Lawan (NPC) menjual hanya 100 saham di harga 9.000
        Order sell100 = Order.createLimit("AUDIT-NPC-SELL-100", "IDX:BBCA", seller.getId(), OrderSide.SELL, Money.of(9000, Currency.IDR), 100, g.getGameTimestamp());
        MatchingEngine.OrderResult sell100Result = g.submitOrder(sell100);
        assertTrue(sell100Result.message, sell100Result.isSuccess);

        // Verifikasi: Order pemain TIDAK BOLEH terisi sebagian!
        // Status tetap OPEN dan filledQuantity tetap 0!
        assertEquals("Order pemain yang mengantre tidak boleh terisi sebagian", OrderStatus.OPEN, buy.getStatus());
        assertEquals(0, buy.getFilledQuantity());
        assertEquals(300, buy.getRemainingQuantity());

        // 3. Lawan berikutnya menjual 300 saham penuh di harga 9.000
        Order sell300 = Order.createLimit("AUDIT-NPC-SELL-300", "IDX:BBCA", seller.getId(), OrderSide.SELL, Money.of(9000, Currency.IDR), 300, g.getGameTimestamp());
        MatchingEngine.OrderResult sell300Result = g.submitOrder(sell300);
        assertTrue(sell300Result.message, sell300Result.isSuccess);

        // Verifikasi: Order pemain sekarang terisi penuh (300 saham)
        assertEquals("Order pemain terisi penuh saat volume lawan mencukupi; sell status=" + sell300.getStatus()
                + ", filled=" + sell300.getFilledQuantity() + ", fills=" + sell300Result.fills.size(), OrderStatus.FILLED, buy.getStatus());
        assertEquals(300, buy.getFilledQuantity());
        assertEquals(0, buy.getRemainingQuantity());
    }

    @Test
    public void testRev02CanonicalNpcMigrationSeamlesslyCreatesMissingAccounts() {
        LeagueManager league = new LeagueManager();
        JSONObject state = league.exportState();
        JSONArray all = state.getJSONArray("competitors");
        JSONArray old20 = new JSONArray();
        JSONObject opening = new JSONObject();
        Map<String, Account> oldAccounts = new HashMap<>();

        for (int i = 0; i < 20; i++) {
            JSONObject n = all.getJSONObject(i);
            old20.put(n);
            String id = n.getString("id");
            opening.put(id, n.getString("netWorth"));
            String aid = LeagueManager.accountIdForNpc(id);
            oldAccounts.put(aid, new Account(aid, id, AccountType.FIRM, null));
        }

        state.put("competitors", old20).put("periodOpening", opening).put("canonicalNpcAccounts", true);
        LeagueManager restored = new LeagueManager();
        restored.restoreState(state);

        // Bind market harus berhasil tanpa exception "Akun NPC liga hilang: ACC-LEAGUE-NPC-0021"
        restored.bindMarket(oldAccounts, new EconomyLedger(), new InstrumentCatalog(), 0L);

        // Akun NPC baru otomatis dibuat dan dipetakan (total 1000 akun NPC)
        assertEquals(1000, oldAccounts.size());
        assertNotNull(oldAccounts.get("ACC-LEAGUE-NPC-0021"));
        assertNotNull(oldAccounts.get("ACC-LEAGUE-NPC-1000"));
    }

    @Test
    public void testRev03IpoUndersubscribedIsOneHundredPercentAllotted() {
        CorporateActionService service = new CorporateActionService();
        service.reset(LocalDate.of(2026, 10, 28));

        // Penawaran 1.000 lot (100.000 lembar)
        IpoOffering ipo = new IpoOffering(
                "AUDIT-50", "AUD2", "Audit 2",
                Currency.IDR, Money.of(1000, Currency.IDR), 100, 100_000L,
                LocalDate.of(2026, 11, 5)
        );
        service.registerIpoOffering(ipo);

        Map<String, Account> accounts = new HashMap<>();
        Account a = new Account("USER-1", "User", AccountType.PERSONAL, null);
        a.creditCash(Money.of(50_000_000L, Currency.IDR));
        accounts.put(a.getId(), a);

        // Pesan 10 lot (10 <= 1000, kurang peminat)
        IpoSubscription sub = service.orderIpo(ipo, a, 10, "SUB-1", 1L);

        // Eksekusi penjatahan meskipun ratio yang diteruskan 0.50
        service.allotIpo(ipo, 0.50, accounts, new EconomyLedger(), 2L);

        assertEquals("Pesanan IPO yang kurang peminat harus 100% dipenuhi", 10, sub.getAllottedLots());
        assertEquals("Refund harus 0 saat pesanan 100% dipenuhi", Money.zero(Currency.IDR), sub.getRefundMoney());
    }

    @Test
    public void testRev04IpoLargestRemainderQuota7To4On11Lots() {
        CorporateActionService service = new CorporateActionService();
        service.reset(LocalDate.of(2026, 10, 28));

        // Penawaran 11 lot (1.100 lembar)
        IpoOffering ipo = new IpoOffering(
                "AUDIT-11", "AUDIT", "Audit",
                Currency.IDR, Money.of(1000, Currency.IDR), 100, 1100L,
                LocalDate.of(2026, 11, 5)
        );
        service.registerIpoOffering(ipo);

        Map<String, Account> accounts = new HashMap<>();
        Account aL = new Account("L", "L", AccountType.PERSONAL, null); aL.creditCash(Money.of(10_000_000L, Currency.IDR)); accounts.put("L", aL);
        Account aA = new Account("A", "A", AccountType.PERSONAL, null); aA.creditCash(Money.of(10_000_000L, Currency.IDR)); accounts.put("A", aA);
        Account aB = new Account("B", "B", AccountType.PERSONAL, null); aB.creditCash(Money.of(10_000_000L, Currency.IDR)); accounts.put("B", aB);
        Account aC = new Account("C", "C", AccountType.PERSONAL, null); aC.creditCash(Money.of(10_000_000L, Currency.IDR)); accounts.put("C", aC);

        // Pemesan besar (8 lot >= 20% dari 11 = 3 lot)
        IpoSubscription large = service.orderIpo(ipo, aL, 8, "L", 1L);
        // Pemesan lain (2 lot masing-masing, total 6 lot)
        IpoSubscription a = service.orderIpo(ipo, aA, 2, "A", 1L);
        IpoSubscription b = service.orderIpo(ipo, aB, 2, "B", 1L);
        IpoSubscription c = service.orderIpo(ipo, aC, 2, "C", 1L);

        service.allotIpo(ipo, 1.0, accounts, new EconomyLedger(), 2L);

        assertEquals("Pemesan besar mendapat 7 lot (60% dari 11 dengan pecahan terbesar 6.6 -> 7)", 7, large.getAllottedLots());
        long others = a.getAllottedLots() + b.getAllottedLots() + c.getAllottedLots();
        assertEquals("Pemesan lain mendapat 4 lot (40% dari 11 dengan pecahan 4.4 -> 4)", 4, others);
        assertEquals("Total lot terjatah tepat 11 lot", 11, large.getAllottedLots() + others);
    }

    @Test
    public void testRev06IpoCancellationBoundaryBeforeAndAfterListingDate() {
        CorporateActionService service = new CorporateActionService();
        service.reset(LocalDate.of(2026, 10, 28));

        LocalDate listingDate = LocalDate.of(2026, 11, 5);
        IpoOffering ipo = new IpoOffering(
                "IPO-BOUND", "BND", "PT Batas Waktu Tbk",
                Currency.IDR, Money.of(1000, Currency.IDR), 100, 100_000L,
                listingDate
        );
        service.registerIpoOffering(ipo);

        Account account = new Account("ACC-USER", "Investor", AccountType.PERSONAL, null);
        account.creditCash(Money.of(20_000_000L, Currency.IDR));

        IpoSubscription sub = service.orderIpo(ipo, account, 10, "SUB-BOUND-1", 1000L);
        assertEquals(Money.of(1_000_000L, Currency.IDR), account.getReservedCash(Currency.IDR));

        // 1. Pembatalan setelah penutupan masa penawaran DITOLAK (LT-23 / REV-06)
        boolean rejectedAfterOffering = service.cancelSubscription(sub.getId(), account, ipo.getOfferingEndDate().plusDays(1));
        assertFalse("Pembatalan setelah masa penawaran berakhir harus ditolak", rejectedAfterOffering);
        boolean rejectedAtListing = service.cancelSubscription(sub.getId(), account, ipo.getListingDate());
        assertFalse("Pembatalan pada tanggal listing harus ditolak", rejectedAtListing);
        assertEquals(IpoSubscription.SubscriptionStatus.PENDING, sub.getStatus());
        assertEquals(Money.of(1_000_000L, Currency.IDR), account.getReservedCash(Currency.IDR));

        // 2. Pembatalan selama masa penawaran DITERIMA dan dana tertahan dilepas
        boolean validCancel = service.cancelSubscription(sub.getId(), account, ipo.getOfferingEndDate());
        assertTrue("Pembatalan selama masa penawaran harus diterima", validCancel);
        assertEquals(IpoSubscription.SubscriptionStatus.CANCELLED, sub.getStatus());
        assertEquals(Money.zero(Currency.IDR), account.getReservedCash(Currency.IDR));
    }
}
