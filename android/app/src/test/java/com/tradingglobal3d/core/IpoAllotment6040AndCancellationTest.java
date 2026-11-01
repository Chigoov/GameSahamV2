package com.tradingglobal3d.core;

import com.tradingglobal3d.core.corporate.CorporateActionService;
import com.tradingglobal3d.core.corporate.IpoOffering;
import com.tradingglobal3d.core.corporate.IpoSubscription;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.AccountType;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Uji Penjatahan IPO 60:40, Ambang 20%, Lot Sisa, dan Pembatalan (IPO01 - IPO03):
 */
public class IpoAllotment6040AndCancellationTest {

    @Test
    public void testCancelSubscriptionReleasesReservedCash() {
        CorporateActionService service = new CorporateActionService();
        service.reset(LocalDate.of(2026, 10, 28));

        IpoOffering ipo = new IpoOffering(
                "IPO-TEST-01", "TEST", "PT Uji Perdana Tbk",
                Currency.IDR, Money.of(1000, Currency.IDR), 100, 100_000L,
                LocalDate.of(2026, 11, 5)
        );
        service.registerIpoOffering(ipo);

        Account account = new Account("ACC-USER-01", "Investor", AccountType.PERSONAL, null);
        account.creditCash(Money.of(50_000_000L, Currency.IDR));

        // 1. Buat pesanan IPO 50 lot (50 * 100 * 1000 = Rp 5.000.000)
        IpoSubscription sub = service.orderIpo(ipo, account, 50, "REQ-SUB-01", 1000L);
        assertNotNull(sub);
        assertEquals(IpoSubscription.SubscriptionStatus.PENDING, sub.getStatus());
        assertEquals(Money.of(5_000_000L, Currency.IDR), account.getReservedCash(Currency.IDR));

        // 2. Batalkan pemesanan sebelum penjatahan
        boolean cancelled = service.cancelSubscription(sub.getId(), account);
        assertTrue("Pemesanan IPO harus berhasil dibatalkan", cancelled);
        assertEquals(IpoSubscription.SubscriptionStatus.CANCELLED, sub.getStatus());
        assertEquals("Dana tertahan harus dilepas seketika", Money.zero(Currency.IDR), account.getReservedCash(Currency.IDR));
        assertEquals(Money.of(50_000_000L, Currency.IDR), account.getAvailableCash(Currency.IDR));
    }

    @Test
    public void testAllotmentWithLargeAndOtherOrdersQuota6040() {
        CorporateActionService service = new CorporateActionService();
        service.reset(LocalDate.of(2026, 10, 28));

        // Penawaran 20.000 lembar = 200 lot.
        // Ambang besar (>= 20%) = 40 lot.
        IpoOffering ipo = new IpoOffering(
                "IPO-TEST-02", "TEST2", "PT Mega IPO Tbk",
                Currency.IDR, Money.of(1000, Currency.IDR), 100, 20_000L,
                LocalDate.of(2026, 11, 5)
        );
        service.registerIpoOffering(ipo);

        Map<String, Account> accounts = new HashMap<>();
        EconomyLedger ledger = new EconomyLedger();

        // Akun A: Pesanan besar 150 lot (>= 40 lot, kelompok besar)
        Account accA = new Account("ACC-LARGE-01", "Institusi A", AccountType.PERSONAL, null);
        accA.creditCash(Money.of(50_000_000L, Currency.IDR));
        accounts.put(accA.getId(), accA);
        IpoSubscription subA = service.orderIpo(ipo, accA, 150, "REQ-SUB-A", 1000L);

        // Akun B: Pesanan lain 30 lot (< 40 lot, kelompok lain)
        Account accB = new Account("ACC-RETAIL-01", "Ritel B", AccountType.PERSONAL, null);
        accB.creditCash(Money.of(50_000_000L, Currency.IDR));
        accounts.put(accB.getId(), accB);
        IpoSubscription subB = service.orderIpo(ipo, accB, 30, "REQ-SUB-B", 1000L);

        // Akun C: Pesanan lain 70 lot... wait, 70 >= 40 so C would be large!
        // Akun C pesan 35 lot, Akun D pesan 35 lot (< 40 lot)
        Account accC = new Account("ACC-RETAIL-02", "Ritel C", AccountType.PERSONAL, null);
        accC.creditCash(Money.of(50_000_000L, Currency.IDR));
        accounts.put(accC.getId(), accC);
        IpoSubscription subC = service.orderIpo(ipo, accC, 35, "REQ-SUB-C", 1000L);

        Account accD = new Account("ACC-RETAIL-03", "Ritel D", AccountType.PERSONAL, null);
        accD.creditCash(Money.of(50_000_000L, Currency.IDR));
        accounts.put(accD.getId(), accD);
        IpoSubscription subD = service.orderIpo(ipo, accD, 35, "REQ-SUB-D", 1000L);

        // Total penawaran 200 lot. Total permintaan = 150 (besar) + 100 (lain) = 250 lot (oversubscribed).
        // Pool Besar (60%) = 120 lot, Pool Lain (40%) = 80 lot.
        // Sub A (satu-satunya di kelompok besar) mendapat 120 lot.
        // Kelompok lain (B=30, C=35, D=35, total 100) mendapat 80 lot.
        service.allotIpo(ipo, 1.0, accounts, ledger, 2000L);

        assertTrue(ipo.isAllotted());
        assertEquals(IpoSubscription.SubscriptionStatus.ALLOTTED, subA.getStatus());
        assertEquals("Pemesan besar mendapat alokasi dari pool 60%", 120, subA.getAllottedLots());
        long othersTotal = subB.getAllottedLots() + subC.getAllottedLots() + subD.getAllottedLots();
        assertEquals("Kelompok lain mendapat alokasi dari pool 40%", 80, othersTotal);

        // Kas tertahan harus bersih
        assertEquals(Money.zero(Currency.IDR), accA.getReservedCash(Currency.IDR));
        assertEquals(Money.zero(Currency.IDR), accB.getReservedCash(Currency.IDR));
    }

    @Test
    public void testLargestRemainderMethod11LotsSplit7To4() {
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

        // Ambang besar >= 20% dari 11 = 3 lot
        IpoSubscription large = service.orderIpo(ipo, aL, 8, "L", 1L);
        IpoSubscription a = service.orderIpo(ipo, aA, 2, "A", 1L);
        IpoSubscription b = service.orderIpo(ipo, aB, 2, "B", 1L);
        IpoSubscription c = service.orderIpo(ipo, aC, 2, "C", 1L);

        service.allotIpo(ipo, 1.0, accounts, new EconomyLedger(), 2L);

        assertEquals("Pemesan besar mendapat 7 lot (60% dari 11 dengan pecahan terbesar 6.6 -> 7)", 7, large.getAllottedLots());
        long others = a.getAllottedLots() + b.getAllottedLots() + c.getAllottedLots();
        assertEquals("Pemesan lain mendapat 4 lot (40% dari 11 dengan pecahan 4.4 -> 4)", 4, others);
        assertEquals("Total lot terjatah tepat 11 lot", 11, large.getAllottedLots() + others);
    }
}
