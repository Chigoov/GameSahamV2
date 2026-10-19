package com.tradingglobal3d.core;

import com.tradingglobal3d.core.clock.GameClock;
import com.tradingglobal3d.core.clock.MarketSession;
import com.tradingglobal3d.core.economy.EconomyLedger;
import com.tradingglobal3d.core.economy.LedgerEntry;
import com.tradingglobal3d.core.economy.LedgerEntryType;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.storage.SaveState;
import com.tradingglobal3d.core.corporate.*;
import com.tradingglobal3d.core.risk.ShortPosition;
import com.tradingglobal3d.core.career.DailySessionReport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.Assert.*;

public class DomainAccountSaveTest {
    private File testDir;
    private SaveManager saveManager;
    private TradingFacade facade;

    @Before
    public void setUp() {
        testDir = new File(System.getProperty("java.io.tmpdir"), "trading_test_" + System.currentTimeMillis());
        testDir.mkdirs();
        saveManager = SaveManager.forDirectory(testDir);
        facade = new TradingFacade(saveManager);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        com.tradingglobal3d.core.GameTestSupport.unlockManager(facade);
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
    public void testMoneyPrecisionAndFormatting() {
        Money idr = Money.of(1500000.75, Currency.IDR);
        assertEquals(new BigDecimal("1500001"), idr.getAmount());
        assertEquals("Rp 1.500.001", idr.format());

        Money usd = Money.of(1234.567, Currency.USD);
        assertEquals(new BigDecimal("1234.57"), usd.getAmount());
        assertEquals("$1,234.57", usd.format());

        Money sum = idr.add(Money.of(500000, Currency.IDR));
        assertEquals(Money.of(2000001, Currency.IDR), sum);

        try {
            idr.add(usd);
            fail("Harus menolak operasi penjumlahan mata uang berbeda");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Cannot operate on different currencies"));
        }
    }

    @Test
    public void testAccountCashAndReservedBalance() {
        Account acc = facade.getPersonalAccount();
        assertEquals(Money.of(50_000_000, Currency.IDR), acc.getTotalCash(Currency.IDR));
        assertEquals(Money.of(50_000_000, Currency.IDR), acc.getAvailableCash(Currency.IDR));

        // Tahan kas untuk order Rp 10.000.000
        Money reserved = Money.of(10_000_000, Currency.IDR);
        acc.reserveCash(reserved);

        assertEquals(Money.of(50_000_000, Currency.IDR), acc.getTotalCash(Currency.IDR));
        assertEquals(Money.of(40_000_000, Currency.IDR), acc.getAvailableCash(Currency.IDR));
        assertEquals(Money.of(10_000_000, Currency.IDR), acc.getReservedCash(Currency.IDR));

        // Coba debit 45.000.000 (harus ditolak karena available cuma 40.000.000)
        try {
            acc.debitCash(Money.of(45_000_000, Currency.IDR));
            fail("Harus menolak debit melebihi saldo available");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("tidak mencukupi"));
        }

        // Release kas tertahan
        acc.releaseReservedCash(reserved);
        assertEquals(Money.of(50_000_000, Currency.IDR), acc.getAvailableCash(Currency.IDR));
    }

    @Test
    public void testAccountIsolationBetweenPersonalAndClient() {
        Account personal = facade.getPersonalAccount();
        Account clientA = facade.createClientAccount("Klien Dana Sejahtera", "MANDATE-001", Money.of(100_000_000, Currency.IDR));

        assertEquals(AccountType.PERSONAL, personal.getType());
        assertEquals(AccountType.CLIENT, clientA.getType());
        assertEquals("MANDATE-001", clientA.getClientMandateId());

        // Saldo klien dan pribadi terpisah mutlak
        assertEquals(Money.of(50_000_000, Currency.IDR), personal.getTotalCash(Currency.IDR));
        assertEquals(Money.of(100_000_000, Currency.IDR), clientA.getTotalCash(Currency.IDR));

        // Mutasi pada klien tidak memengaruhi kas pribadi
        facade.withdraw("REQ-W-01", clientA.getId(), Money.of(20_000_000, Currency.IDR), "Penarikan klien");
        assertEquals(Money.of(80_000_000, Currency.IDR), clientA.getTotalCash(Currency.IDR));
        assertEquals(Money.of(50_000_000, Currency.IDR), personal.getTotalCash(Currency.IDR));
    }

    @Test
    public void testPortfolioValuationIncludesBothCashCurrencies() {
        Account account = facade.getPersonalAccount();
        facade.deposit("VALUE-USD", account.getId(), Money.of(100, Currency.USD), "test");
        assertEquals(Money.of(51_580_000, Currency.IDR), account.getTotalPortfolioValue(Currency.IDR, new BigDecimal("15800")));
        assertEquals(new Money(new BigDecimal("3264.56"), Currency.USD), account.getTotalPortfolioValue(Currency.USD, new BigDecimal("15800")));
    }

    @Test
    public void testNegativeCashOperationsRejectWithoutMutation() {
        Account account = facade.getPersonalAccount();
        Money before = account.getTotalCash(Currency.IDR);
        Money negative = Money.of(-1000, Currency.IDR);
        Runnable[] operations = {
                () -> account.creditCash(negative), () -> account.debitCash(negative),
                () -> account.reserveCash(negative), () -> account.releaseReservedCash(negative)
        };
        for (Runnable operation : operations) {
            try {
                operation.run();
                fail("Negative cash operation must be rejected");
            } catch (IllegalArgumentException expected) {
                assertEquals(before, account.getTotalCash(Currency.IDR));
                assertEquals(Money.zero(Currency.IDR), account.getReservedCash(Currency.IDR));
            }
        }
    }

    @Test
    public void testLedgerIdempotencyAndReconciliation() {
        Account personal = facade.getPersonalAccount();
        String reqId = "UNIQUE-DEPOSIT-123";
        Money depositAmt = Money.of(5_000_000, Currency.IDR);

        // Mutasi pertama
        LedgerEntry entry1 = facade.deposit(reqId, personal.getId(), depositAmt, "Hadiah turnamen");
        assertEquals(Money.of(55_000_000, Currency.IDR), personal.getTotalCash(Currency.IDR));

        // Retry dengan reqId yang sama
        LedgerEntry entry2 = facade.deposit(reqId, personal.getId(), depositAmt, "Hadiah turnamen (retry)");
        assertSame(entry1, entry2); // Mengembalikan objek entry yang sama
        assertEquals(Money.of(55_000_000, Currency.IDR), personal.getTotalCash(Currency.IDR)); // Saldo tidak berlipat ganda!

        // Rekonsiliasi ledger
        assertTrue("Seluruh saldo harus lolos audit rekonsiliasi", facade.reconcileAll());
    }

    @Test
    public void testGameClockAndMarketSessions() {
        GameClock clock = new GameClock(LocalDate.of(2026, 10, 19), LocalTime.of(8, 40, 0));
        assertEquals(MarketSession.CLOSED, clock.getCurrentMarketSession());

        // Play dan tick 10 menit (ke 08:50:00 -> Pre-opening)
        clock.play();
        clock.tick(600);
        assertEquals(MarketSession.PRE_OPENING, clock.getCurrentMarketSession());
        assertFalse(clock.getCurrentMarketSession().isOpenForTrading());

        // Tick 15 menit (ke 09:05:00 -> Sesi 1)
        clock.tick(900);
        assertEquals(MarketSession.SESSION_1, clock.getCurrentMarketSession());
        assertTrue(clock.getCurrentMarketSession().isOpenForTrading());

        // Pause clock -> waktu tidak bertambah
        clock.pause();
        clock.tick(1000);
        assertEquals(LocalTime.of(9, 5, 0), clock.getCurrentTime());
    }

    @Test
    public void testCorruptStartupRequiresRecoveryInsteadOfNewCapital() throws Exception {
        java.nio.file.Files.write(saveManager.getSaveFile().toPath(), "BROKEN".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        TradingFacade startup = new TradingFacade(saveManager);
        java.lang.reflect.Method method = TradingFacade.class.getMethod("startOrResume");
        assertFalse((Boolean) method.invoke(startup));
        assertNull(startup.getPersonalAccount());
        assertNull(startup.getClock());
        org.json.JSONObject snapshot = new org.json.JSONObject(startup.getSnapshotJson());
        assertTrue(snapshot.getBoolean("recoveryRequired"));
        assertEquals("BROKEN", new String(java.nio.file.Files.readAllBytes(saveManager.getSaveFile().toPath()), java.nio.charset.StandardCharsets.UTF_8));
    }

    @Test
    public void testInvalidRuntimeLoadDoesNotReplaceLiveState() throws Exception {
        facade.save();
        org.json.JSONObject data = new org.json.JSONObject(new String(java.nio.file.Files.readAllBytes(saveManager.getSaveFile().toPath()), java.nio.charset.StandardCharsets.UTF_8));
        data.getJSONObject("runtimeState").getJSONObject("career").put("rank", "INVALID");
        java.nio.file.Files.write(saveManager.getSaveFile().toPath(), data.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Account original = facade.getPersonalAccount();
        facade.deposit("UNSAVED-AFTER", original.getId(), Money.of(1000, Currency.IDR), "test");
        Money cash = original.getTotalCash(Currency.IDR);
        assertFalse(facade.load());
        assertSame("Rejected restore must preserve live object graph", original, facade.getPersonalAccount());
        assertEquals(cash, facade.getPersonalAccount().getTotalCash(Currency.IDR));
    }

    @Test
    public void testInvalidDayReceiptCannotReplaceLiveState() throws Exception {
        facade.save();
        org.json.JSONObject data = new org.json.JSONObject(new String(java.nio.file.Files.readAllBytes(saveManager.getSaveFile().toPath()), java.nio.charset.StandardCharsets.UTF_8));
        data.getJSONObject("runtimeState").getJSONObject("dayCloseReceipts").put("BAD", "not a receipt");
        java.nio.file.Files.write(saveManager.getSaveFile().toPath(), data.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Account original = facade.getPersonalAccount();
        facade.deposit("UNSAVED-RECEIPT", original.getId(), Money.of(1000, Currency.IDR), "test");
        assertFalse(facade.load());
        assertSame("Receipt validation must happen before replacing live state", original, facade.getPersonalAccount());
        assertEquals(Money.of(50_001_000, Currency.IDR), original.getTotalCash(Currency.IDR));
    }

    @Test
    public void testUnknownSaveVersionIsRejectedWithoutOverwritingOriginal() throws Exception {
        facade.save();
        org.json.JSONObject data = new org.json.JSONObject(new String(java.nio.file.Files.readAllBytes(saveManager.getSaveFile().toPath()), java.nio.charset.StandardCharsets.UTF_8));
        data.put("schemaVersion", 999);
        byte[] original = data.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        java.nio.file.Files.write(saveManager.getSaveFile().toPath(), original);
        assertFalse(saveManager.load().isSuccess());
        assertArrayEquals(original, java.nio.file.Files.readAllBytes(saveManager.getSaveFile().toPath()));
    }

    @Test
    public void testWriteFailurePreservesLastAcknowledgedSave() throws Exception {
        facade.save();
        byte[] before = java.nio.file.Files.readAllBytes(saveManager.getSaveFile().toPath());
        assertTrue(saveManager.getTempFile().mkdir());
        try {
            facade.save();
            fail("A directory at the temporary path must make the write fail");
        } catch (java.io.IOException expected) {
            assertArrayEquals(before, java.nio.file.Files.readAllBytes(saveManager.getSaveFile().toPath()));
            assertTrue(saveManager.load().isSuccess());
        }
    }

    @Test
    public void testCorruptPrimaryCannotOverwriteValidBackupOnSave() throws Exception {
        facade.save();
        facade.save();
        byte[] validBackup = java.nio.file.Files.readAllBytes(saveManager.getBackupFile().toPath());
        java.nio.file.Files.write(saveManager.getSaveFile().toPath(), "BROKEN".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        facade.save();
        assertArrayEquals("A corrupt primary must never replace the last valid backup", validBackup,
                java.nio.file.Files.readAllBytes(saveManager.getBackupFile().toPath()));
    }

    @Test
    public void testSaveRetainsCareerMandateAndSubscription() throws Exception {
        facade.getCareerManager().completeMission("MIS-01");
        facade.claimMission("MIS-01");
        Account client = facade.acceptClientMandate("MANDATE-01");
        facade.collectClientFees("MANDATE-01");
        facade.deposit("SUB-USD", facade.getPersonalAccount().getId(), Money.of(1000, Currency.USD), "test");
        facade.subscribeTerminal(com.tradingglobal3d.core.research.TerminalTier.PRO);
        facade.save();
        TradingFacade loaded = new TradingFacade(saveManager);
        assertTrue(loaded.load());
        assertTrue(loaded.getCareerManager().getMission("MIS-01").isClaimed());
        assertEquals(facade.getCareerManager().getReputationScore(), loaded.getCareerManager().getReputationScore());
        assertEquals(client.getId(), loaded.getFirmManager().getMandate("MANDATE-01").getClientAccountId());
        assertEquals(com.tradingglobal3d.core.research.TerminalTier.PRO, loaded.getResearchService().getCurrentTier());
        assertEquals(facade.getResearchService().getSubscriptionExpiresAt(), loaded.getResearchService().getSubscriptionExpiresAt());
        Money before = loaded.getAccount(client.getId()).getTotalCash(Currency.IDR);
        loaded.collectClientFees("MANDATE-01");
        assertEquals(before, loaded.getAccount(client.getId()).getTotalCash(Currency.IDR));
    }

    @Test
    public void testSaveRestoresDebtCollateralAndOpenOrders() throws Exception {
        Account personal = facade.getPersonalAccount();
        com.tradingglobal3d.core.market.InstrumentCatalog catalog = com.tradingglobal3d.core.market.InstrumentCatalog.fromJsonString(
                new String(java.nio.file.Files.readAllBytes(new File("src/main/assets/data/market_universe.json").toPath()), java.nio.charset.StandardCharsets.UTF_8));
        facade.setCatalog(catalog);
        facade.takeMarginLoan(Money.of(1000000, Currency.IDR));
        facade.openShortPosition("BBCA", 100);
        com.tradingglobal3d.core.trading.Order order = com.tradingglobal3d.core.trading.Order.createLimit(
                "SAVE-OPEN", "IDX:BMRI", personal.getId(), com.tradingglobal3d.core.trading.OrderSide.BUY,
                Money.of(5000, Currency.IDR), 100, 1L);
        facade.submitOrder(order);
        Money reserved = personal.getReservedCash(Currency.IDR);
        facade.save();
        TradingFacade loaded = new TradingFacade(saveManager);
        loaded.setCatalog(catalog);
        assertTrue(loaded.load());
        assertEquals(Money.of(1000000, Currency.IDR), loaded.getRiskService().getMarginDebt(personal.getId(), Currency.IDR));
        assertNotNull(loaded.getRiskService().getShortPosition(personal.getId(), "BBCA"));
        assertEquals(reserved, loaded.getPersonalAccount().getReservedCash(Currency.IDR));
        assertEquals(1, loaded.getOrderManager().getActiveOrdersForAccount(personal.getId()).size());
        assertTrue(loaded.cancelOrder(loaded.getOrderManager().getActiveOrdersForAccount(personal.getId()).get(0)));
        assertEquals(facade.getRiskService().getShortPosition(personal.getId(), "BBCA").getCollateralLocked(),
                loaded.getPersonalAccount().getReservedCash(Currency.IDR));
        assertTrue(loaded.reconcileAll());
    }

    @Test
    public void testFreshCatalogRestoresPricesStatisticsAndWatchlist() throws Exception {
        String universe = new String(java.nio.file.Files.readAllBytes(new File("src/main/assets/data/market_universe.json").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        com.tradingglobal3d.core.market.InstrumentCatalog original = com.tradingglobal3d.core.market.InstrumentCatalog.fromJsonString(universe);
        facade.setCatalog(original);
        com.tradingglobal3d.core.market.Instrument bbca = original.getById("IDX:BBCA");
        bbca.updatePrice(Money.of(11000, Currency.IDR), 100);
        bbca.updatePrice(Money.of(10500, Currency.IDR), 200);
        original.addToWatchlist("IDX:BBCA");
        original.addToWatchlist("US:AAPL");
        facade.save();
        TradingFacade restored = new TradingFacade(saveManager);
        restored.setCatalog(com.tradingglobal3d.core.market.InstrumentCatalog.fromJsonString(universe));
        assertTrue(restored.load());
        com.tradingglobal3d.core.market.Instrument loaded = restored.getCatalog().getById("IDX:BBCA");
        assertEquals(bbca.getCurrentPrice(), loaded.getCurrentPrice());
        assertEquals(bbca.getPreviousClosePrice(), loaded.getPreviousClosePrice());
        assertEquals(bbca.getDayHigh(), loaded.getDayHigh());
        assertEquals(bbca.getDayLow(), loaded.getDayLow());
        assertEquals(300, loaded.getDayVolume());
        assertTrue(restored.getCatalog().isInWatchlist("IDX:BBCA"));
        assertTrue(restored.getCatalog().isInWatchlist("US:AAPL"));
        assertFalse(restored.getCatalog().isInWatchlist("IDX:BMRI"));
    }

    @Test
    public void testReloadPreservesHaltFillsAndQueuePriority() throws Exception {
        String universe = new String(java.nio.file.Files.readAllBytes(new File("src/main/assets/data/market_universe.json").toPath()), java.nio.charset.StandardCharsets.UTF_8);
        facade = new TradingFacade(saveManager);
        facade.setCatalog(com.tradingglobal3d.core.market.InstrumentCatalog.fromJsonString(universe));
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        facade.generateNpcLiquidity();
        com.tradingglobal3d.core.trading.Order buy = com.tradingglobal3d.core.trading.Order.createLimit("BOOK-FILL", "IDX:BBCA",
                facade.getPersonalAccount().getId(), com.tradingglobal3d.core.trading.OrderSide.BUY, Money.of(11000, Currency.IDR), 100, 1L);
        assertTrue(facade.submitOrder(buy).isSuccess);
        com.tradingglobal3d.core.trading.OrderBook book = facade.getMarketEngine().getBook("IDX:BBCA");
        assertFalse(book.getTradeHistory().isEmpty());
        facade.getMarketEngine().haltTrading("IDX:BBCA");
        java.util.List<String> priority = new java.util.ArrayList<>();
        for (java.util.Queue<com.tradingglobal3d.core.trading.Order> level : book.getBids().values())
            for (com.tradingglobal3d.core.trading.Order order : level) if (!order.getStatus().isFinal()) priority.add(order.getId());
        facade.save();
        TradingFacade loaded = new TradingFacade(saveManager);
        loaded.setCatalog(com.tradingglobal3d.core.market.InstrumentCatalog.fromJsonString(universe));
        assertTrue(loaded.load());
        assertTrue("Reload must not silently reopen a halted market", loaded.getMarketEngine().isHalted("IDX:BBCA"));
        com.tradingglobal3d.core.trading.OrderBook restored = loaded.getMarketEngine().getBook("IDX:BBCA");
        assertEquals(book.getLastTradedPrice(), restored.getLastTradedPrice());
        assertEquals(book.getTradeHistory().size(), restored.getTradeHistory().size());
        assertEquals(book.getTradeHistory().get(0).getFillId(), restored.getTradeHistory().get(0).getFillId());
        java.util.List<String> restoredPriority = new java.util.ArrayList<>();
        for (java.util.Queue<com.tradingglobal3d.core.trading.Order> level : restored.getBids().values())
            for (com.tradingglobal3d.core.trading.Order order : level) if (!order.getStatus().isFinal()) restoredPriority.add(order.getId());
        assertEquals(priority, restoredPriority);
        assertTrue(loaded.reconcileAll());
    }

    @Test
    public void testSameTickerPositionsAcrossExchangesSurviveReloadWithoutOverwrite() throws Exception {
        Account account = facade.getPersonalAccount();
        Position idx = new Position("SAME", "IDX", Currency.IDR, 100, new BigDecimal("1000"), new BigDecimal("1100"));
        Position us = new Position("SAME", "US", Currency.USD, 2, new BigDecimal("20"), new BigDecimal("21"));
        account.addPosition(idx); account.addPosition(us);
        assertEquals(2, account.getPositions().size());
        assertSame(idx, account.getPosition("IDX:SAME"));
        assertSame(us, account.getPosition("US:SAME"));
        try { account.getPosition("SAME"); fail("Ambiguous ticker must require exchange"); }
        catch (IllegalArgumentException expected) { }
        facade.save();
        TradingFacade restored = new TradingFacade(saveManager);
        assertTrue(restored.load());
        assertEquals(100, restored.getPersonalAccount().getPosition("IDX:SAME").getQuantity());
        assertEquals(2, restored.getPersonalAccount().getPosition("US:SAME").getQuantity());
        restored.getPersonalAccount().removePosition("IDX:SAME");
        assertEquals(1, restored.getPersonalAccount().getPositions().size());
        assertEquals(2, restored.getPersonalAccount().getPosition("SAME").getQuantity());
    }

    @Test
    public void testRealizedPositionPnlSurvivesFullSaleAndReload() throws Exception {
        Position position = new Position("PNL", "IDX", Currency.IDR, 100, new BigDecimal("1000"), new BigDecimal("1000"));
        facade.getPersonalAccount().addPosition(position);
        assertEquals(Money.of(10000, Currency.IDR), position.addSell(40, new BigDecimal("1250")));
        assertEquals(Money.of(-6000, Currency.IDR), position.addSell(60, new BigDecimal("900")));
        java.lang.reflect.Method realized = Position.class.getMethod("getRealizedPnL");
        assertEquals(Money.of(4000, Currency.IDR), realized.invoke(position));
        assertEquals(0, position.getQuantity());
        facade.save();
        TradingFacade restored = new TradingFacade(saveManager);
        assertTrue(restored.load());
        Position loaded = restored.getPersonalAccount().getPosition("IDX:PNL");
        assertNotNull(loaded);
        assertEquals(Money.of(4000, Currency.IDR), realized.invoke(loaded));
        loaded.addBuy(100, new BigDecimal("950"));
        loaded.addSell(100, new BigDecimal("1000"));
        assertEquals(Money.of(9000, Currency.IDR), realized.invoke(loaded));
    }

    @Test
    public void testAtomicSaveBackupAndRecovery() throws Exception {
        Account personal = facade.getPersonalAccount();
        facade.deposit("REQ-DEP-SAVETEST", personal.getId(), Money.of(10_000_000, Currency.IDR), "Deposit sebelum save");
        assertEquals(Money.of(60_000_000, Currency.IDR), personal.getTotalCash(Currency.IDR));

        // Save pertama
        facade.save();
        File saveFile = saveManager.getSaveFile();
        assertTrue("Save file .json harus tercipta", saveFile.exists());

        // Mutasi lagi lalu save kedua (untuk menghasilkan backup .bak)
        facade.deposit("REQ-DEP-SAVETEST-2", personal.getId(), Money.of(5_000_000, Currency.IDR), "Deposit kedua");
        assertEquals(Money.of(65_000_000, Currency.IDR), personal.getTotalCash(Currency.IDR));
        facade.save();

        File backupFile = saveManager.getBackupFile();
        assertTrue("Backup file .bak harus tercipta", backupFile.exists());

        // Buat facade baru dan muat save normal
        TradingFacade facade2 = new TradingFacade(saveManager);
        assertTrue(facade2.load());
        assertEquals(Money.of(65_000_000, Currency.IDR), facade2.getPersonalAccount().getTotalCash(Currency.IDR));
        assertTrue(facade2.reconcileAll());

        // Simulasi Korupsi: rusak file save utama
        try (FileWriter fw = new FileWriter(saveFile, false)) {
            fw.write("{CORRUPTED_JSON_DATA_!!!");
        }

        // Muat kembali menggunakan facade3: harus berhasil pulih dari file backup!
        TradingFacade facade3 = new TradingFacade(saveManager);
        SaveManager.LoadResult recoveryResult = saveManager.load();
        assertTrue("Harus berhasil memuat meskipun file utama korup", recoveryResult.isSuccess());
        assertEquals(SaveManager.LoadSource.BACKUP_RECOVERED, recoveryResult.getSource());

        assertTrue(facade3.load());
        // Saldo yang dipulihkan berasal dari snapshot valid sebelum save terakhir
        assertTrue(facade3.getPersonalAccount().getTotalCash(Currency.IDR).isPositive());
        assertTrue(facade3.reconcileAll());
    }

    @Test
    public void testCorporateActionsIpoAndVotingSurviveSaveAndReload() throws Exception {
        Account personal = facade.getPersonalAccount();
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 2000, BigDecimal.valueOf(10000), BigDecimal.valueOf(10250)));

        LocalDate today = facade.getClock().getCurrentDate();
        LocalDate future = today.plusDays(5);

        // 1. Jadwalkan Dividen Tunai dan tandai sudah dieksekusi
        CorporateAction divAction = new CorporateAction(
                "ACT-SAVE-DIV", "BBCA", CorporateActionType.CASH_DIVIDEND,
                BigDecimal.valueOf(300), Currency.IDR, today, future
        );
        divAction.markExecuted();
        facade.getCorporateService().scheduleCorporateAction(divAction);

        // 2. Daftarkan IPO dan pesan 10 lot (kas tertahan Rp 500.000)
        IpoOffering ipo = new IpoOffering(
                "IPO-SAVE-TEST", "TEST", "Test Company Tbk", Currency.IDR,
                Money.of(500, Currency.IDR), 100, 50_000, future
        );
        facade.getCorporateService().registerIpoOffering(ipo);
        IpoSubscription sub = facade.orderIpo("IPO-SAVE-TEST", 10);
        assertNotNull(sub);
        assertEquals(Money.of(500_000, Currency.IDR), personal.getReservedCash(Currency.IDR));

        // 3. Daftarkan agenda RUPS dan sampaikan suara via facade (2.000 suara Setuju sesuai 2.000 lembar BBCA)
        ShareholderMeetingAgenda agenda = new ShareholderMeetingAgenda(
                "RUPS-SAVE-01", "BBCA", "Agenda Tahunan Save Test",
                "Resolusi pengujian persistensi agenda", today
        );
        facade.getCorporateService().registerAgenda(agenda);
        facade.castShareholderVote("RUPS-SAVE-01", ShareholderMeetingAgenda.VoteChoice.FOR);
        assertEquals(2000, agenda.getTotalVotesFor());
        assertTrue(agenda.hasVoted(personal.getId()));

        // 4. Save ke disk
        facade.save();

        // 5. Muat pada facade baru
        TradingFacade restored = new TradingFacade(saveManager);
        assertTrue(restored.load());

        // 6. Verifikasi Aksi Korporasi status dieksekusi tetap dipertahankan
        CorporateAction restoredDiv = null;
        for (CorporateAction ca : restored.getCorporateService().getCorporateActions()) {
            if ("ACT-SAVE-DIV".equals(ca.getId())) {
                restoredDiv = ca;
                break;
            }
        }
        assertNotNull("Aksi korporasi harus ada di save", restoredDiv);
        assertTrue("Status isExecuted dividen harus tersimpan true", restoredDiv.isExecuted());

        // 7. Verifikasi IPO & Subscriptions tersimpan dan dana tertahan tetap konsisten
        IpoOffering restoredIpo = restored.getCorporateService().getIpo("IPO-SAVE-TEST");
        assertNotNull("Offering IPO harus ada di save", restoredIpo);
        assertEquals(Money.of(500_000, Currency.IDR), restored.getPersonalAccount().getReservedCash(Currency.IDR));

        // 8. Verifikasi Agenda RUPS dan rekaman hak suara tersimpan utuh
        ShareholderMeetingAgenda restoredAgenda = restored.getCorporateService().getAgenda("RUPS-SAVE-01");
        assertNotNull("Agenda RUPS harus ada di save", restoredAgenda);
        assertEquals(2000, restoredAgenda.getTotalVotesFor());
        assertEquals(0, restoredAgenda.getTotalVotesAgainst());
        assertEquals(0, restoredAgenda.getTotalVotesAbstain());
        assertTrue("Akun pemilih harus tercatat di restored agenda", restoredAgenda.hasVoted(restored.getPersonalAccount().getId()));

        // 9. Verifikasi pencegahan suara ganda (idempotensi hak suara) setelah reload
        try {
            restored.castShareholderVote("RUPS-SAVE-01", ShareholderMeetingAgenda.VoteChoice.AGAINST);
            fail("Pemberian suara berulang setelah reload harus ditolak");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("sudah pernah"));
        }
        assertEquals("Total suara tidak boleh berubah saat retry ditolak", 2000, restoredAgenda.getTotalVotesFor());

        // 10. Verifikasi rekonsiliasi seluruh akun
        assertTrue(restored.reconcileAll());
    }

    @Test
    public void testShortPositionAndLenderInventorySurviveSaveAndCover() throws Exception {
        com.tradingglobal3d.core.market.InstrumentCatalog catalog = com.tradingglobal3d.core.market.InstrumentCatalog.fromJsonString(
                new String(java.nio.file.Files.readAllBytes(new File("src/main/assets/data/market_universe.json").toPath()), java.nio.charset.StandardCharsets.UTF_8));
        facade.setCatalog(catalog);

        Account personal = facade.getPersonalAccount();
        Account mm = facade.getAccount("ACC-NPC-MM-01");
        assertNotNull(mm);

        Position mmPos = mm.getPosition("IDX:BBCA");
        assertNotNull(mmPos);
        long initialLenderQty = mmPos.getQuantity();

        // Buka posisi short 500 lembar
        ShortPosition shortPos = facade.openShortPosition("BBCA", 500);
        assertNotNull(shortPos);
        assertEquals(initialLenderQty - 500, mmPos.getQuantity());

        // Save ke disk
        facade.save();

        // Muat ke facade baru
        TradingFacade restored = new TradingFacade(saveManager);
        restored.setCatalog(catalog);
        assertTrue(restored.load());

        Account restoredPersonal = restored.getPersonalAccount();
        Account restoredMm = restored.getAccount("ACC-NPC-MM-01");
        assertNotNull(restoredMm);

        Position restoredMmPos = restoredMm.getPosition("IDX:BBCA");
        assertNotNull(restoredMmPos);
        assertEquals("Inventori lender harus tetap berkurang setelah reload", initialLenderQty - 500, restoredMmPos.getQuantity());

        ShortPosition restoredShort = restored.getRiskService().getShortPosition(restoredPersonal.getId(), "BBCA");
        assertNotNull("Posisi short harus tetap aktif setelah reload", restoredShort);
        assertEquals(500, restoredShort.getBorrowedShares());

        // Buy to Cover pada facade yang di-restore
        restored.closeShortPosition("BBCA", 500);
        assertNull("Short harus tertutup setelah cover", restored.getRiskService().getShortPosition(restoredPersonal.getId(), "BBCA"));
        assertEquals("Inventori lender harus pulih kembali setelah cover", initialLenderQty, restoredMmPos.getQuantity());

        assertTrue(restored.reconcileAll());
    }

    @Test
    public void testDayCloseAfterReloadDoesNotDuplicateSalaryFeeDividend() throws Exception {
        Account personal = facade.getPersonalAccount();

        // Majukan waktu ke 16:01 WIB
        facade.getClock().play();
        facade.getClock().tick((int) java.time.Duration.between(facade.getClock().getCurrentTime(), LocalTime.of(16, 1)).getSeconds());
        facade.getClock().pause();

        String requestId = "REQ-DAY-CLOSE-ONCE";
        org.json.JSONObject receipt1 = facade.closeDay(requestId);
        assertNotNull(receipt1);
        int ledgerCountBefore = facade.getLedger().getEntries().size();
        Money netWorthBefore = personal.getTotalCash(Currency.IDR);

        // Save ke disk
        facade.save();

        // Muat ke facade baru
        TradingFacade restored = new TradingFacade(saveManager);
        assertTrue(restored.load());

        // Panggil closeDay dengan requestId yang sama -> harus idempoten (mengembalikan receipt yang sama tanpa menambah mutasi)
        org.json.JSONObject receipt2 = restored.closeDay(requestId);
        assertNotNull(receipt2);
        assertEquals(receipt1.getString("endingNetWorth"), receipt2.getString("endingNetWorth"));
        assertEquals(receipt1.getString("salary"), receipt2.getString("salary"));

        assertEquals("Ledger tidak boleh bertambah saat request idempoten dipanggil ulang",
                ledgerCountBefore, restored.getLedger().getEntries().size());
        assertEquals("Net worth tidak boleh berubah saat request idempoten",
                netWorthBefore, restored.getPersonalAccount().getTotalCash(Currency.IDR));

        assertTrue(restored.reconcileAll());
    }
}
