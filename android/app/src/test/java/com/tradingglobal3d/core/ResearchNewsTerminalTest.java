package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.research.*;
import com.tradingglobal3d.core.storage.SaveManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class ResearchNewsTerminalTest {
    private File testDir;
    private TradingFacade facade;
    private InstrumentCatalog catalog;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "research_test_" + System.currentTimeMillis());
        testDir.mkdirs();

        File dataDir = new File("src/main/assets/data");
        File universeFile = new File(dataDir, "market_universe.json");
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        catalog = InstrumentCatalog.fromJsonString(jsonUniverse);

        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
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
    public void testAntiFutureLeakOnNewsFeed() {
        ResearchService service = facade.getResearchService();
        long now = System.currentTimeMillis();

        // Ambil berita pada waktu sekarang
        List<NewsArticle> published = service.getPublishedNews(now);
        assertFalse(published.isEmpty());

        for (NewsArticle article : published) {
            assertTrue("Berita masa depan tidak boleh bocor ke waktu sekarang (FR-09.08)", article.getPublishedAt() <= now);
            assertFalse("Berita rahasia masa depan tidak boleh muncul", article.getId().equals("NEWS-FUTURE-01"));
        }

        // Jika waktu dimajukan 2 hari ke masa depan, berita tersebut baru boleh muncul
        long futureTime = now + 90000_000L;
        List<NewsArticle> futureFeed = service.getPublishedNews(futureTime);
        boolean foundFutureNews = false;
        for (NewsArticle a : futureFeed) {
            if (a.getId().equals("NEWS-FUTURE-01")) {
                foundFutureNews = true;
                break;
            }
        }
        assertTrue("Berita harus terbit setelah tanggal rilisnya tiba", foundFutureNews);
    }

    @Test
    public void testAnalystReportAndConsensusData() {
        List<AnalystReport> reports = facade.getAnalystReports();
        assertFalse(reports.isEmpty());

        AnalystReport rep = reports.get(0);
        assertNotNull(rep.getAnalystName());
        assertNotNull(rep.getFirmName());
        assertNotNull(rep.getTicker());
        assertNotNull(rep.getRecommendation());
        assertTrue(rep.getTargetPrice().isPositive());
        assertTrue(rep.getAccuracyHistoryPercent() > 0 && rep.getAccuracyHistoryPercent() <= 100);
        assertNotNull(rep.getThesisSummary());
    }

    @Test
    public void testCalendarEventsAvailable() {
        List<CalendarEvent> events = facade.getCalendar();
        assertFalse(events.isEmpty());

        CalendarEvent e1 = events.get(0);
        assertNotNull(e1.getTitle());
        assertNotNull(e1.getCategory());
        assertNotNull(e1.getDate());
    }

    @Test
    public void testTerminalSubscriptionEntitlementAndBilling() {
        ResearchService service = facade.getResearchService();
        Account personal = facade.getPersonalAccount();
        long now = System.currentTimeMillis();

        // 1. Awalnya pemain berada di tier BASIC
        assertEquals(TerminalTier.BASIC, service.getCurrentTier());
        assertTrue(service.hasAccess(TerminalTier.BASIC, now));
        assertFalse("Tier PRO harus terkunci sebelum berlangganan", service.hasAccess(TerminalTier.PRO, now));
        assertFalse("Tier ENTERPRISE harus terkunci sebelum berlangganan", service.hasAccess(TerminalTier.ENTERPRISE, now));

        // 2. Coba langganan PRO tanpa kas USD (saldo $0) -> harus ditolak
        boolean subFail = facade.subscribeTerminal(TerminalTier.PRO);
        assertFalse("Langganan harus ditolak jika saldo USD tidak mencukupi", subFail);
        assertEquals(TerminalTier.BASIC, service.getCurrentTier());

        // 3. Berikan modal USD $1,000 ke akun pemain melalui deposit ledger resmi
        facade.deposit("REQ-DEP-USD-TEST", personal.getId(), Money.of(1000.0, Currency.USD), "Deposit modal USD");
        assertEquals(Money.of(1000.0, Currency.USD), personal.getTotalCash(Currency.USD));

        // Langganan PRO seharga $500 USD
        boolean subOk = facade.subscribeTerminal(TerminalTier.PRO);
        assertTrue("Langganan PRO harus sukses setelah ada kas USD", subOk);
        assertEquals(TerminalTier.PRO, service.getCurrentTier());
        assertTrue("Tier PRO harus terbuka", service.hasAccess(TerminalTier.PRO, now));
        assertFalse("Tier ENTERPRISE tetap terkunci", service.hasAccess(TerminalTier.ENTERPRISE, now));

        // Kas USD terpotong $500 menjadi $500
        assertEquals(Money.of(500.0, Currency.USD), personal.getTotalCash(Currency.USD));

        // 4. Uji Kedaluwarsa Langganan (setelah 31 hari ke masa depan)
        long after31Days = now + ResearchService.THIRTY_DAYS_MS + 86400_000L;
        assertFalse("Akses PRO harus terkunci saat masa langganan kedaluwarsa", service.hasAccess(TerminalTier.PRO, after31Days));
        assertEquals(TerminalTier.BASIC, service.getCurrentTier());

        assertTrue(facade.reconcileAll());
    }
}
