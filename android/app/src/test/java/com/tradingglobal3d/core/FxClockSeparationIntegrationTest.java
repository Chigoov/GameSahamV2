package com.tradingglobal3d.core;

import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.FxExchangeService;
import com.tradingglobal3d.core.economy.FxQuote;
import com.tradingglobal3d.core.economy.LedgerEntry;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tes integrasi pemisahan waktu FX:
 * - Quote TTL memakai waktu nyata (wall clock).
 * - Ledger settlement memakai waktu game (game timestamp).
 * - Idempotensi requestId.
 * - Penolakan quote kedaluwarsa.
 */
public class FxClockSeparationIntegrationTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    private TradingFacade facade;

    @Before
    public void setUp() throws Exception {
        File dataDir = new File("src/main/assets/data");
        File universeFile = new File(dataDir, "market_universe.json");
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        InstrumentCatalog catalog = InstrumentCatalog.fromJsonString(jsonUniverse);

        facade = new TradingFacade(SaveManager.forDirectory(temporary.newFolder()));
        facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(100_000_000));
    }

    @Test
    public void fxSettlementUsesGameTimestampInLedgerWhileQuoteUsesRealtime() throws Exception {
        Account personal = facade.getPersonalAccount();
        long gameTimestampBefore = facade.getClock().getGameTimestamp();
        long realNow = System.currentTimeMillis();

        Money idrToExchange = Money.of(15_800_000, Currency.IDR);
        JSONObject preview = facade.previewFx(personal.getId(), Currency.IDR, Currency.USD, idrToExchange);
        assertTrue(preview.getBoolean("success"));
        String quoteId = preview.getString("quoteId");

        // Konfirmasi penukaran
        JSONObject confirm = facade.confirmFx("REQ-FX-CLOCK-1", quoteId, personal.getId(), Currency.IDR, Currency.USD, idrToExchange);
        assertTrue(confirm.getBoolean("success"));
        assertEquals("SETTLED", confirm.getString("status"));

        // Verifikasi mutasi ledger mencatat gameTimestamp, bukan realNow
        List<LedgerEntry> entries = facade.getLedger().getEntriesForAccount(personal.getId());
        boolean foundDebit = false;
        boolean foundCredit = false;
        for (LedgerEntry entry : entries) {
            if ("REQ-FX-CLOCK-1-DEBIT".equals(entry.getRequestId())) {
                assertEquals(LedgerEntryType.FX_CONVERSION_DEBIT, entry.getType());
                assertEquals(gameTimestampBefore, entry.getTimestamp());
                foundDebit = true;
            }
            if ("REQ-FX-CLOCK-1-CREDIT".equals(entry.getRequestId())) {
                assertEquals(LedgerEntryType.FX_CONVERSION_CREDIT, entry.getType());
                assertEquals(gameTimestampBefore, entry.getTimestamp());
                foundCredit = true;
            }
        }
        assertTrue("Debit mutasi harus ditemukan dengan gameTimestamp", foundDebit);
        assertTrue("Credit mutasi harus ditemukan dengan gameTimestamp", foundCredit);

        // Verifikasi saldo kas
        assertTrue(personal.getTotalCash(Currency.USD).isPositive());
        assertEquals(Money.of(100_000_000 - 15_800_000, Currency.IDR), personal.getTotalCash(Currency.IDR));
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void fxConversionIsIdempotentOnSameRequestId() throws Exception {
        Account personal = facade.getPersonalAccount();
        Money idrToExchange = Money.of(15_800_000, Currency.IDR);
        JSONObject preview = facade.previewFx(personal.getId(), Currency.IDR, Currency.USD, idrToExchange);
        String quoteId = preview.getString("quoteId");

        JSONObject first = facade.confirmFx("REQ-FX-IDEMPOTENT", quoteId, personal.getId(), Currency.IDR, Currency.USD, idrToExchange);
        assertTrue(first.getBoolean("success"));
        Money usdAfterFirst = personal.getTotalCash(Currency.USD);

        // Panggilan kedua dengan requestId yang sama harus mengembalikan hasil yang sama tanpa mutasi ganda
        JSONObject second = facade.confirmFx("REQ-FX-IDEMPOTENT", quoteId, personal.getId(), Currency.IDR, Currency.USD, idrToExchange);
        assertTrue(second.getBoolean("success"));
        assertEquals(usdAfterFirst, personal.getTotalCash(Currency.USD));
        assertEquals(Money.of(100_000_000 - 15_800_000, Currency.IDR), personal.getTotalCash(Currency.IDR));
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void expiredQuoteInRealtimeIsRejected() {
        Account personal = facade.getPersonalAccount();
        EconomyLedger ledger = facade.getLedger();
        FxExchangeService fxService = facade.getFxService();

        long realNow = System.currentTimeMillis();
        FxQuote quote = fxService.getOrGenerateQuote(realNow);

        // Lewatkan waktu nyata melebihi TTL quote (30 detik = 30_000 ms)
        long expiredTime = quote.getExpiresAt() + 1000L;
        long gameTimestamp = facade.getClock().getGameTimestamp();

        FxExchangeService.ConversionResult res = fxService.executeReviewedConversion(
                "REQ-FX-EXPIRED", quote.getQuoteId(), personal, Currency.IDR, Currency.USD,
                Money.of(1_000_000, Currency.IDR), expiredTime, gameTimestamp, ledger
        );
        assertFalse("Quote kedaluwarsa waktu nyata harus ditolak: " + res.message, res.isSuccess);
        assertTrue(res.message.contains("kedaluwarsa") || res.message.contains("berubah"));
    }
}
