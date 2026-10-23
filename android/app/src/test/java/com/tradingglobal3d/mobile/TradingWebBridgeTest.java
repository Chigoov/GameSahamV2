package com.tradingglobal3d.mobile;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.Instrument;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class TradingWebBridgeTest {
    private File testDir;
    private TradingFacade facade;
    private TradingWebBridge bridge;

    @Before
    public void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "bridge_test_" + System.currentTimeMillis());
        testDir.mkdirs();

        File dataDir = new File("src/main/assets/data");
        File universeFile = new File(dataDir, "market_universe.json");
        String jsonUniverse = new String(Files.readAllBytes(universeFile.toPath()), StandardCharsets.UTF_8);
        InstrumentCatalog catalog = InstrumentCatalog.fromJsonString(jsonUniverse);

        facade = new TradingFacade(SaveManager.forDirectory(testDir));
        facade.setCatalog(catalog);
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        com.tradingglobal3d.core.GameTestSupport.unlockManager(facade);

        bridge = new TradingWebBridge(facade);
    }

    @Test
    public void portfolioSummarySeparatesBorrowedCashFromEquityAndClientAssets() throws Exception {
        Account personal = facade.getPersonalAccount();
        JSONObject initial = new JSONObject(bridge.getPortfolio(personal.getId()));
        assertEquals("Rp 50.000.000", initial.getString("netWorth"));
        assertEquals(0, new BigDecimal(initial.getString("positionsSharePercent")).signum());
        facade.takeMarginLoan(Money.of(1_000_000, Currency.IDR));
        JSONObject borrowed = new JSONObject(bridge.getPortfolio(personal.getId()));
        assertEquals("Rp 51.000.000", borrowed.getString("grossAssets"));
        assertEquals(initial.getString("netWorth"), borrowed.getString("netWorth"));
        assertEquals("Rp 1.000.000", borrowed.getString("liabilities"));
        assertEquals("Rp 51.000.000", borrowed.getString("cashValueIdr"));
        assertEquals("Rp 0", borrowed.getString("positionsValueIdr"));
        Account client = facade.acceptClientMandate("MANDATE-01");
        JSONObject clientSummary = new JSONObject(bridge.getPortfolio(client.getId()));
        assertEquals("CLIENT", clientSummary.getString("accountType"));
        assertNotEquals(borrowed.getString("netWorth"), clientSummary.getString("netWorth"));
        assertEquals(borrowed.getString("netWorth"), new JSONObject(bridge.getPortfolio(personal.getId())).getString("netWorth"));
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
    public void testPlatformInfo() throws Exception {
        String infoRaw = bridge.getPlatformInfo();
        JSONObject info = new JSONObject(infoRaw);
        assertEquals("Android", info.getString("platform"));
        assertTrue(info.getBoolean("nativeHost"));
        assertEquals("1.0.0-2d-portrait", info.getString("version"));
    }

    @Test
    public void testGetSnapshotAndBalances() throws Exception {
        String snapRaw = bridge.getSnapshot();
        JSONObject snap = new JSONObject(snapRaw);

        assertEquals("INTERNAL_MARKET", snap.getString("gameMode"));
        assertTrue(snap.has("clock"));

        JSONObject clock = snap.getJSONObject("clock");
        assertTrue(clock.has("time"));
        assertTrue(clock.has("session"));

        JSONObject personal = snap.getJSONObject("personalAccount");
        assertEquals("Portofolio Pribadi", personal.getString("name"));

        JSONObject idr = personal.getJSONObject("cash").getJSONObject("IDR");
        assertEquals("Rp 50.000.000", idr.getString("total"));
        assertEquals("Rp 50.000.000", idr.getString("available"));
        assertEquals("Rp 0", idr.getString("reserved"));

        assertTrue(personal.has("netWorth"));
        assertEquals("Rp 50.000.000", personal.getString("netWorth"));
        assertEquals("50000000", personal.getString("netWorthRaw"));
    }

    @Test
    public void testGetInstrumentsAndIndices() throws Exception {
        String instRaw = bridge.getInstruments("", "", "", true);
        JSONArray instruments = new JSONArray(instRaw);
        assertEquals(100, instruments.length()); // 50 IDX + 50 US, all active in the game.
        JSONArray all = new JSONArray(bridge.getInstruments("", "", "", false));
        assertEquals(100, all.length());
        for (int i = 0; i < all.length(); i++) assertTrue(all.getJSONObject(i).getBoolean("isActiveTrading"));
        JSONArray category = new JSONArray(bridge.getInstruments("", "US", "Teknologi", true));
        assertTrue(category.length() > 0);
        for (int i = 0; i < category.length(); i++) assertEquals("Teknologi", category.getJSONObject(i).getString("sector"));

        String idxRaw = bridge.getIndices();
        JSONArray indices = new JSONArray(idxRaw);
        assertEquals(3, indices.length()); // IHSG, LQ45, SP500
        JSONObject ihsg = indices.getJSONObject(0);
        assertTrue(ihsg.has("changePercent"));
        assertTrue(ihsg.has("source"));
        assertTrue(ihsg.getBoolean("available"));

        // Verifikasi detail indeks jujur kawasan belum tersedia
        JSONObject nikkeiDetail = new JSONObject(bridge.getIndexDetail("N225"));
        assertFalse(nikkeiDetail.getBoolean("success"));
        assertTrue(nikkeiDetail.getBoolean("unavailable"));
        assertTrue(nikkeiDetail.getString("message").contains("data emiten belum disediakan"));
    }

    @Test
    public void testCorporateActionsIpoAndVotingViaBridge() throws Exception {
        java.lang.reflect.Method getIpo = TradingWebBridge.class.getMethod("getIpoOfferings");
        JSONArray ipos = new JSONArray((String) getIpo.invoke(bridge));
        assertTrue(ipos.length() >= 1);
        com.tradingglobal3d.core.corporate.IpoOffering offering = facade.getCorporateService().getIpoOfferings().stream()
                .filter(item -> !item.isAllotted() && !item.getListingDate().isBefore(facade.getClock().getCurrentDate()))
                .findFirst().orElseThrow();
        JSONObject ipo = null;
        for (int i = 0; i < ipos.length(); i++) if (ipos.getJSONObject(i).getString("id").equals(offering.getId())) ipo = ipos.getJSONObject(i);
        assertNotNull("Open offering must be exposed by bridge", ipo);
        assertEquals(offering.getTicker(), ipo.getString("ticker"));
        assertEquals("IDR", ipo.getString("currency"));

        java.lang.reflect.Method orderIpo = TradingWebBridge.class.getMethod("orderIpo", String.class);
        Money initialAvail = facade.getPersonalAccount().getAvailableCash(Currency.IDR);

        Money reservation = offering.getOfferingPrice().multiply(BigDecimal.valueOf(10L * offering.getLotSize()));
        JSONObject orderReq = new JSONObject().put("ipoId", offering.getId()).put("lots", 10).put("requestId", "IPO-BRIDGE-TEST-01");
        JSONObject orderRes = new JSONObject((String) orderIpo.invoke(bridge, orderReq.toString()));
        assertTrue(orderRes.getBoolean("success"));
        assertEquals("PENDING", orderRes.getString("status"));
        assertEquals(reservation, facade.getPersonalAccount().getReservedCash(Currency.IDR));
        assertEquals(initialAvail.subtract(reservation), facade.getPersonalAccount().getAvailableCash(Currency.IDR));

        // Pesan dengan lot 0 atau IPO tidak dikenal harus ditolak
        JSONObject invalidLots = new JSONObject().put("ipoId", offering.getId()).put("lots", 0);
        assertFalse(new JSONObject((String) orderIpo.invoke(bridge, invalidLots.toString())).getBoolean("success"));
        JSONObject invalidIpo = new JSONObject().put("ipoId", "IPO-UNKNOWN").put("lots", 5);
        assertFalse(new JSONObject((String) orderIpo.invoke(bridge, invalidIpo.toString())).getBoolean("success"));

        // Voting RUPS
        java.lang.reflect.Method getAgendas = TradingWebBridge.class.getMethod("getShareholderAgendas");
        JSONArray agendas = new JSONArray((String) getAgendas.invoke(bridge));
        assertTrue(agendas.length() >= 1);
        JSONObject agenda = agendas.getJSONObject(0);
        assertEquals("RUPS-BBCA-01", agenda.getString("id"));
        assertFalse(agenda.getBoolean("hasVoted"));

        // Masuk ke jendela voting (hari recording date: gameBase + 1 hari)
        facade.getClock().advanceGameSeconds(86400);

        // Tanpa kepemilikan saham, voting harus ditolak (0 hak suara)
        java.lang.reflect.Method castVote = TradingWebBridge.class.getMethod("castShareholderVote", String.class);
        JSONObject voteReq = new JSONObject().put("agendaId", "RUPS-BBCA-01").put("choice", "FOR");
        JSONObject noSharesVote = new JSONObject((String) castVote.invoke(bridge, voteReq.toString()));
        assertFalse("Tanpa saham voting harus ditolak", noSharesVote.getBoolean("success"));
        assertTrue(noSharesVote.getString("message").contains("hak suara"));

        // Berikan kepemilikan saham BBCA
        facade.getPersonalAccount().addPosition(new Position("BBCA", "IDX", Currency.IDR, 200, BigDecimal.valueOf(10000), BigDecimal.valueOf(10250)));

        // Berikan suara sah
        JSONObject voteRes = new JSONObject((String) castVote.invoke(bridge, voteReq.toString()));
        assertTrue(voteRes.getBoolean("success"));

        // Verifikasi status hasVoted terupdate
        JSONArray updatedAgendas = new JSONArray((String) getAgendas.invoke(bridge));
        assertTrue(updatedAgendas.getJSONObject(0).getBoolean("hasVoted"));

        // Suara kedua harus ditolak (idempoten)
        JSONObject repeatVote = new JSONObject((String) castVote.invoke(bridge, voteReq.toString()));
        assertFalse("Suara ganda harus ditolak", repeatVote.getBoolean("success"));

        // Corporate actions list
        java.lang.reflect.Method getCorpActions = TradingWebBridge.class.getMethod("getCorporateActions");
        JSONArray actions = new JSONArray((String) getCorpActions.invoke(bridge));
        assertNotNull(actions);
    }

    @Test
    public void testMultiAssetFundBuilderAndFirmReportsViaBridge() throws Exception {
        // 1. Multi-Aset Katalog
        java.lang.reflect.Method getMulti = TradingWebBridge.class.getMethod("getMultiAssets");
        JSONObject multi = new JSONObject((String) getMulti.invoke(bridge));
        assertTrue(multi.has("bonds"));
        assertTrue(multi.has("commodities"));
        JSONArray bonds = multi.getJSONArray("bonds");
        assertTrue("Minimal 1 obligasi negara", bonds.length() > 0);
        assertEquals("FR0091", bonds.getJSONObject(0).getString("ticker"));

        JSONArray comms = multi.getJSONArray("commodities");
        assertTrue("Minimal 1 komoditas emas", comms.length() > 0);
        assertEquals("XAU", comms.getJSONObject(0).getString("ticker"));

        // 2. Fund Plans & Validation
        java.lang.reflect.Method getPlans = TradingWebBridge.class.getMethod("getFundPlans");
        JSONArray initialPlans = new JSONArray((String) getPlans.invoke(bridge));
        assertNotNull(initialPlans);

        java.lang.reflect.Method savePlan = TradingWebBridge.class.getMethod("saveFundPlan", String.class);
        // Validasi: bobot > 1.0 harus ditolak
        JSONObject invalidWeightsReq = new JSONObject()
                .put("planId", "PLAN-TEST-01")
                .put("name", "Alokasi Invalid")
                .put("cashWeight", 0.5)
                .put("weights", new JSONObject().put("IDX:BBCA", 0.7)); // total 1.2
        JSONObject invRes = new JSONObject((String) savePlan.invoke(bridge, invalidWeightsReq.toString()));
        assertFalse("Total bobot > 1.0 harus ditolak", invRes.getBoolean("success"));

        // Simpan rencana valid: 50% BBCA, 50% Kas
        JSONObject validPlanReq = new JSONObject()
                .put("planId", "PLAN-TEST-01")
                .put("name", "Alokasi Seimbang 50/50")
                .put("cashWeight", 0.5)
                .put("weights", new JSONObject().put("IDX:BBCA", 0.5));
        JSONObject validRes = new JSONObject((String) savePlan.invoke(bridge, validPlanReq.toString()));
        assertTrue("Rencana valid harus disimpan", validRes.getBoolean("success"));
        assertEquals("PLAN-TEST-01", validRes.getString("planId"));

        // Verifikasi rencana tersimpan
        JSONArray updatedPlans = new JSONArray((String) getPlans.invoke(bridge));
        assertTrue(updatedPlans.length() > 0);

        // 3. Eksekusi Rebalancing
        java.lang.reflect.Method execRebalance = TradingWebBridge.class.getMethod("executeFundRebalance", String.class);
        JSONObject rebalRes = new JSONObject((String) execRebalance.invoke(bridge, "PLAN-TEST-01"));
        assertTrue(rebalRes.getBoolean("success"));
        assertEquals("PLAN-TEST-01", rebalRes.getString("planId"));
        assertTrue(rebalRes.has("results"));

        // 4. Laporan Klien & Biaya Firma (S22)
        java.lang.reflect.Method getClientReports = TradingWebBridge.class.getMethod("getClientReports");
        JSONArray clientReports = new JSONArray((String) getClientReports.invoke(bridge));
        assertNotNull(clientReports);
    }

    @Test
    public void testResearchFeedAndTierSubscriptionViaBridge() throws Exception {
        // 1. Research feed & tiers
        java.lang.reflect.Method getResearch = TradingWebBridge.class.getMethod("getResearchFeed");
        JSONObject feed = new JSONObject((String) getResearch.invoke(bridge));
        assertTrue(feed.has("tier"));
        assertEquals("BASIC", feed.getString("tier"));
        assertTrue(feed.has("news"));
        assertTrue(feed.has("analysts"));
        assertTrue(feed.has("calendar"));

        JSONArray news = feed.getJSONArray("news");
        assertTrue("Berita publik tersedia", news.length() > 0);

        JSONArray analysts = feed.getJSONArray("analysts");
        assertTrue("Laporan analis tersedia", analysts.length() > 0);

        JSONArray calendar = feed.getJSONArray("calendar");
        assertTrue("Kalender bursa tersedia", calendar.length() > 0);

        // 2. Daftar Paket Riset
        java.lang.reflect.Method getTiers = TradingWebBridge.class.getMethod("getResearchTiers");
        JSONArray tiers = new JSONArray((String) getTiers.invoke(bridge));
        assertEquals(3, tiers.length());

        // 3. Berlangganan Paket Pro (gagal jika saldo USD tidak cukup)
        java.lang.reflect.Method subTier = TradingWebBridge.class.getMethod("subscribeResearchTier", String.class);
        // Default akun hanya punya kas IDR, kas USD = 0 -> gagal bayar $500
        JSONObject failSub = new JSONObject((String) subTier.invoke(bridge, "PRO"));
        assertFalse("Tanpa saldo USD langganan harus ditolak", failSub.getBoolean("success"));

        // Berikan saldo USD ke akun personal
        facade.getPersonalAccount().creditCash(new Money(BigDecimal.valueOf(1000), Currency.USD));

        // Berlangganan sukses
        JSONObject okSub = new JSONObject((String) subTier.invoke(bridge, "PRO"));
        assertTrue("Dengan kas USD cukup langganan harus berhasil", okSub.getBoolean("success"));

        // Feed terupdate menjadi PRO
        JSONObject updatedFeed = new JSONObject((String) getResearch.invoke(bridge));
        assertEquals("PRO", updatedFeed.getString("tier"));
    }

    @Test
    public void testStartNewGameAndResetViaBridge() throws Exception {
        java.lang.reflect.Method startNew = TradingWebBridge.class.getMethod("startNewGame", String.class);
        JSONObject cfg = new JSONObject().put("mode", "INTERNAL_MARKET").put("initialCapital", 25_000_000L);
        JSONObject res = new JSONObject((String) startNew.invoke(bridge, cfg.toString()));
        assertTrue("Permainan baru berhasil dimulai", res.getBoolean("success"));
        assertEquals("INTERNAL_MARKET", res.getString("mode"));

        // Verifikasi akun personal memiliki modal awal 25 juta
        assertEquals(BigDecimal.valueOf(25_000_000), facade.getPersonalAccount().getTotalCash(Currency.IDR).getAmount());
    }

    @Test
    public void testStressTestingAndStrategyLabViaBridge() throws Exception {
        // 1. Stress Testing Scenarios
        java.lang.reflect.Method getScenarios = TradingWebBridge.class.getMethod("getStressTestScenarios");
        JSONArray scenarios = new JSONArray((String) getScenarios.invoke(bridge));
        assertTrue("Minimal 2 skenario stres makro", scenarios.length() >= 2);

        // Berikan kepemilikan saham BBCA pada akun personal
        facade.getPersonalAccount().addPosition(new Position("BBCA", "IDX", Currency.IDR, 4000, BigDecimal.valueOf(10000), BigDecimal.valueOf(10000)));

        // Jalankan Skenario Crash
        java.lang.reflect.Method runStress = TradingWebBridge.class.getMethod("runStressTest", String.class);
        JSONObject stressRes = new JSONObject((String) runStress.invoke(bridge, "STRESS-CRASH-30"));
        assertTrue(stressRes.getBoolean("success"));
        assertEquals("STRESS-CRASH-30", stressRes.getString("scenarioId"));
        assertTrue(stressRes.has("potentialLoss"));
        assertTrue(stressRes.has("percentageDrop"));
        assertTrue(stressRes.has("wouldTriggerMarginCall"));

        // 2. Strategy Lab Sandbox Isolation
        java.lang.reflect.Method createLab = TradingWebBridge.class.getMethod("createStrategyLab", String.class);
        JSONObject labRes = new JSONObject((String) createLab.invoke(bridge, "LAB-TEST-01"));
        assertTrue(labRes.getBoolean("success"));
        assertEquals("LAB-TEST-01", labRes.getString("sandboxId"));

        // Simulasikan transaksi beli di lab
        java.lang.reflect.Method simTrade = TradingWebBridge.class.getMethod("simulateLabTrade", String.class);
        JSONObject tradeReq = new JSONObject()
                .put("sandboxId", "LAB-TEST-01")
                .put("instrumentId", "IDX:BBCA")
                .put("side", "BUY")
                .put("shares", 1000)
                .put("price", 10000);
        JSONObject tradeRes = new JSONObject((String) simTrade.invoke(bridge, tradeReq.toString()));
        assertTrue(tradeRes.getBoolean("success"));
        assertEquals(1, tradeRes.getInt("simulatedTradesCount"));

        // Verifikasi akun asli TIDAK BERUBAH (Isolasi Terjamin)
        assertEquals(4000, facade.getPersonalAccount().getPosition("BBCA").getQuantity());
    }

    @Test
    public void testRiskSnapshotMarginLoanAndRepayViaBridge() throws Exception {
        java.lang.reflect.Method getRisk = TradingWebBridge.class.getMethod("getRiskSnapshot");
        JSONObject snapshot = new JSONObject((String) getRisk.invoke(bridge));
        assertTrue(snapshot.getBoolean("success"));
        assertEquals("Rp 50.000.000", snapshot.getString("equity"));
        assertEquals("Rp 0", snapshot.getString("marginDebtTotal"));
        assertEquals("HEALTHY", snapshot.getString("status"));
        assertEquals("Rp 0", snapshot.getString("marginDebtIdr"));
        assertEquals("$0.00", snapshot.getString("marginDebtUsd"));

        java.lang.reflect.Method takeLoan = TradingWebBridge.class.getMethod("takeMarginLoan", String.class);
        java.lang.reflect.Method repayLoan = TradingWebBridge.class.getMethod("repayMarginLoan", String.class);

        // Pinjam 5.000.000 IDR
        JSONObject loanReqIdr = new JSONObject().put("currency", "IDR").put("amount", "5000000");
        JSONObject loanResIdr = new JSONObject((String) takeLoan.invoke(bridge, loanReqIdr.toString()));
        assertTrue(loanResIdr.getBoolean("success"));
        assertEquals("Rp 5.000.000", loanResIdr.getString("currentDebt"));
        assertEquals(Money.of(55_000_000, Currency.IDR), facade.getPersonalAccount().getTotalCash(Currency.IDR));

        // Pinjam 200 USD
        JSONObject loanReqUsd = new JSONObject().put("currency", "USD").put("amount", "200");
        JSONObject loanResUsd = new JSONObject((String) takeLoan.invoke(bridge, loanReqUsd.toString()));
        assertTrue(loanResUsd.getBoolean("success"));
        assertEquals("$200.00", loanResUsd.getString("currentDebt"));
        assertEquals(Money.of(200, Currency.USD), facade.getPersonalAccount().getTotalCash(Currency.USD));

        // Cek snapshot risiko terupdate
        JSONObject afterLoans = new JSONObject((String) getRisk.invoke(bridge));
        assertEquals("Rp 5.000.000", afterLoans.getString("marginDebtIdr"));
        assertEquals("$200.00", afterLoans.getString("marginDebtUsd"));
        assertTrue(afterLoans.getJSONArray("auditLogs").length() >= 2);

        // Pelunasan negatif harus ditolak
        JSONObject negRepay = new JSONObject().put("currency", "IDR").put("amount", "-1000");
        JSONObject negRes = new JSONObject((String) repayLoan.invoke(bridge, negRepay.toString()));
        assertFalse("Pelunasan negatif harus ditolak", negRes.getBoolean("success"));

        // Pelunasan 2.000.000 IDR
        JSONObject repayReqIdr = new JSONObject().put("currency", "IDR").put("amount", "2000000");
        JSONObject repayResIdr = new JSONObject((String) repayLoan.invoke(bridge, repayReqIdr.toString()));
        assertTrue(repayResIdr.getBoolean("success"));
        assertEquals("Rp 3.000.000", repayResIdr.getString("currentDebt"));
        assertEquals(Money.of(53_000_000, Currency.IDR), facade.getPersonalAccount().getTotalCash(Currency.IDR));

        // Pelunasan 100 USD
        JSONObject repayReqUsd = new JSONObject().put("currency", "USD").put("amount", "100");
        JSONObject repayResUsd = new JSONObject((String) repayLoan.invoke(bridge, repayReqUsd.toString()));
        assertTrue(repayResUsd.getBoolean("success"));
        assertEquals("$100.00", repayResUsd.getString("currentDebt"));
        assertEquals(Money.of(100, Currency.USD), facade.getPersonalAccount().getTotalCash(Currency.USD));

        // Save & reload mempertahankan utang
        facade.save();
        TradingFacade loaded = new TradingFacade(SaveManager.forDirectory(testDir));
        loaded.setCatalog(facade.getCatalog());
        assertTrue(loaded.load());
        TradingWebBridge loadedBridge = new TradingWebBridge(loaded);
        JSONObject loadedRisk = new JSONObject((String) getRisk.invoke(loadedBridge));
        assertEquals("Rp 3.000.000", loadedRisk.getString("marginDebtIdr"));
        assertEquals("$100.00", loadedRisk.getString("marginDebtUsd"));
        assertTrue(loaded.reconcileAll());
    }

    @Test
    public void testIndexDetailUsesCurrentDomainPricesAndLabelsMissingHistory() throws Exception {
        double before = new JSONArray(bridge.getIndices()).getJSONObject(0).getDouble("value");
        com.tradingglobal3d.core.market.Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        bbca.updatePrice(bbca.getCurrentPrice().multiply(new BigDecimal("1.10")), 100);
        double after = new JSONArray(bridge.getIndices()).getJSONObject(0).getDouble("value");
        assertTrue("Index snapshot must reflect changed constituent prices", after > before);
        java.lang.reflect.Method method = TradingWebBridge.class.getMethod("getIndexDetail", String.class);
        int entries = facade.getLedger().getEntries().size();
        JSONObject detail = new JSONObject((String) method.invoke(bridge, "IDX:COMPOSITE"));
        assertTrue(detail.getBoolean("success"));
        assertEquals("MARKET_CAP_WEIGHTED", detail.getString("method"));
        assertEquals("poin", detail.getString("unit"));
        assertEquals(after, detail.getDouble("value"), 0.01);
        assertTrue(detail.getString("sourceLabel").contains("simulasi"));
        assertTrue(detail.isNull("validFrom"));
        assertFalse(detail.getBoolean("historyAvailable"));
        JSONArray members = detail.getJSONArray("members");
        assertTrue(members.length() > 0);
        for (int i = 0; i < members.length(); i++) {
            JSONObject member = members.getJSONObject(i);
            assertNotNull(facade.getCatalog().getById(member.getString("instrumentId")));
        }
        assertEquals(entries, facade.getLedger().getEntries().size());
        assertFalse(new JSONObject((String) method.invoke(bridge, "IDX:UNKNOWN")).getBoolean("success"));
    }

    @Test
    public void testWatchlistRejectsUnknownIdAndRestoresOnFreshCatalog() throws Exception {
        JSONObject invalid = new JSONObject(bridge.toggleWatchlist("IDX:UNKNOWN"));
        assertFalse("Unknown instrument must not be reported added", invalid.optBoolean("inWatchlist", false));
        assertFalse(invalid.getBoolean("success"));
        assertEquals(0, facade.getCatalog().getWatchlist().size());
        assertFalse(facade.getCatalog().isInWatchlist("US:AAPL"));
        assertFalse(facade.getCatalog().isInWatchlist("IDX:UNKNOWN"));
        JSONObject added = new JSONObject(bridge.toggleWatchlist("US:AAPL"));
        assertTrue(added.getBoolean("success"));
        assertTrue(added.getBoolean("inWatchlist"));
        facade.save();
        TradingFacade restored = new TradingFacade(SaveManager.forDirectory(testDir));
        restored.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(new File("src/main/assets/data/market_universe.json").toPath()), StandardCharsets.UTF_8)));
        assertTrue(restored.load());
        TradingWebBridge restoredBridge = new TradingWebBridge(restored);
        JSONArray instruments = new JSONArray(restoredBridge.getInstruments("AAPL", "US", "", true));
        assertEquals(1, instruments.length());
        assertTrue(instruments.getJSONObject(0).getBoolean("isWatchlist"));
        JSONObject removed = new JSONObject(restoredBridge.toggleWatchlist("US:AAPL"));
        assertTrue(removed.getBoolean("success"));
        assertFalse(removed.getBoolean("inWatchlist"));
        assertFalse(restored.getCatalog().isInWatchlist("US:AAPL"));
    }

    @Test
    public void testSubmitOrderAndOrderBookViaBridge() throws Exception {
        // Buat order beli BBCA 1 lot limit 10250 lewat Bridge JSON
        JSONObject orderReq = new JSONObject();
        orderReq.put("instrumentId", "IDX:BBCA");
        orderReq.put("side", "BUY");
        orderReq.put("type", "LIMIT");
        orderReq.put("quantity", 100);
        orderReq.put("limitPrice", "10250");
        orderReq.put("requestId", "REQ-BRIDGE-01");

        String resRaw = bridge.submitOrder(orderReq.toString());
        JSONObject res = new JSONObject(resRaw);

        assertTrue("Order lewat bridge harus sukses", res.getBoolean("success"));
        assertTrue(res.has("orderId"));
        assertTrue(res.has("status"));

        // Periksa Order Book via Bridge
        String bookRaw = bridge.getOrderBook("IDX:BBCA");
        JSONObject book = new JSONObject(bookRaw);
        assertEquals("IDX:BBCA", book.getString("instrumentId"));
        assertTrue(book.has("bids"));
        assertTrue(book.has("asks"));
    }

    @Test
    public void testClockAndCareerSnapshotCanDriveBriefing() throws Exception {
        facade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        JSONObject snapshot = new JSONObject(bridge.getSnapshot());
        assertEquals("NOVICE_TRADER", snapshot.getJSONObject("career").getString("rank"));
        assertEquals(3, snapshot.getJSONObject("career").getJSONArray("missions").length());
        java.lang.reflect.Method controls = TradingWebBridge.class.getMethod("setClockState", boolean.class, int.class);
        JSONObject running = new JSONObject((String) controls.invoke(bridge, true, 2));
        assertFalse(running.getJSONObject("clock").getBoolean("isPaused"));
        assertEquals(2, running.getJSONObject("clock").getInt("speed"));
        JSONObject paused = new JSONObject((String) controls.invoke(bridge, false, 1));
        assertTrue(paused.getJSONObject("clock").getBoolean("isPaused"));
    }

    @Test
    public void testDayCloseRetryDoesNotAdvanceTwice() throws Exception {
        java.lang.reflect.Method close = TradingWebBridge.class.getMethod("closeDay", String.class);
        facade.getClock().play();
        facade.getClock().tick(27060); // 08:30 -> 16:01, current provisional closing boundary
        facade.getClock().pause();
        JSONObject first = new JSONObject((String) close.invoke(bridge, "DAY-CLOSE-ONE"));
        java.time.LocalDate date = facade.getClock().getCurrentDate();
        int days = facade.getCareerManager().getDaysTraded();
        JSONObject retry = new JSONObject((String) close.invoke(bridge, "DAY-CLOSE-ONE"));
        assertTrue(first.getBoolean("success"));
        assertEquals(first.getString("date"), retry.getString("date"));
        assertEquals(date, facade.getClock().getCurrentDate());
        assertEquals(days, facade.getCareerManager().getDaysTraded());
        facade.save();
        TradingFacade loaded = new TradingFacade(SaveManager.forDirectory(testDir));
        loaded.setCatalog(facade.getCatalog());
        assertTrue(loaded.load());
        JSONObject afterLoad = new JSONObject(new TradingWebBridge(loaded).closeDay("DAY-CLOSE-ONE"));
        assertEquals(first.getString("date"), afterLoad.getString("date"));
        assertEquals(date, loaded.getClock().getCurrentDate());
    }

    @Test
    public void testPortfolioAndLedgerSnapshotIsAccountScoped() throws Exception {
        String personalId = facade.getPersonalAccount().getId();
        facade.createClientAccount("Klien", "M", Money.of(1000000, Currency.IDR));
        java.lang.reflect.Method portfolioMethod = TradingWebBridge.class.getMethod("getPortfolio", String.class);
        JSONObject portfolio = new JSONObject((String) portfolioMethod.invoke(bridge, personalId));
        assertEquals(personalId, portfolio.getString("accountId"));
        assertEquals("Rp 50.000.000", portfolio.getJSONObject("cash").getJSONObject("IDR").getString("total"));
        assertEquals(0, portfolio.getJSONArray("positions").length());
        java.lang.reflect.Method ledgerMethod = TradingWebBridge.class.getMethod("getLedger", String.class);
        JSONArray ledger = new JSONArray((String) ledgerMethod.invoke(bridge, personalId));
        assertTrue(ledger.length() > 0);
        for (int i = 0; i < ledger.length(); i++) assertEquals(personalId, ledger.getJSONObject(i).getString("accountId"));
    }

    @Test
    public void testOrderHistoryIsScopedAndRetainsCanonicalStatusAfterReload() throws Exception {
        String accountId = facade.getPersonalAccount().getId();
        JSONObject request = new JSONObject().put("accountId", accountId).put("instrumentId", "IDX:BBCA")
                .put("side", "BUY").put("type", "LIMIT").put("quantity", 100)
                .put("limitPrice", "8000").put("requestId", "HISTORY-1");
        JSONObject first = new JSONObject(bridge.submitOrder(request.toString()));
        java.lang.reflect.Method history = TradingWebBridge.class.getMethod("getOrders", String.class, boolean.class);
        JSONArray active = new JSONArray((String) history.invoke(bridge, accountId, true));
        assertEquals(1, active.length());
        assertEquals(first.getString("orderId"), active.getJSONObject(0).getString("orderId"));
        assertEquals("HISTORY-1", active.getJSONObject(0).getString("requestId"));
        assertEquals("IDR", active.getJSONObject(0).getString("currency"));
        assertEquals(0, new JSONArray((String) history.invoke(bridge, "OTHER", false)).length());
        bridge.cancelOrder("HISTORY-1");
        facade.save();
        assertTrue(facade.load());
        assertEquals(0, new JSONArray((String) history.invoke(bridge, accountId, true)).length());
        JSONArray all = new JSONArray((String) history.invoke(bridge, accountId, false));
        assertEquals(1, all.length());
        assertEquals("CANCELLED", all.getJSONObject(0).getString("status"));
        assertEquals(accountId, all.getJSONObject(0).getString("accountId"));
    }

    @Test
    public void testOrderStatusAndCancellationUseCanonicalRequest() throws Exception {
        JSONObject request = new JSONObject().put("accountId", facade.getPersonalAccount().getId())
                .put("instrumentId", "IDX:BBCA").put("side", "BUY").put("type", "LIMIT")
                .put("quantity", 100).put("limitPrice", "8000").put("requestId", "STATUS-1");
        JSONObject submitted = new JSONObject(bridge.submitOrder(request.toString()));
        java.lang.reflect.Method status = TradingWebBridge.class.getMethod("getOrderStatus", String.class);
        JSONObject receipt = new JSONObject((String) status.invoke(bridge, "STATUS-1"));
        assertEquals(submitted.getString("orderId"), receipt.getString("orderId"));
        assertEquals("OPEN", receipt.getString("status"));
        java.lang.reflect.Method cancel = TradingWebBridge.class.getMethod("cancelOrder", String.class);
        JSONObject cancelled = new JSONObject((String) cancel.invoke(bridge, "STATUS-1"));
        assertEquals("CANCELLED", cancelled.getString("status"));
        assertEquals(Money.zero(Currency.IDR), facade.getPersonalAccount().getReservedCash(Currency.IDR));
    }

    @Test
    public void testOrderPreviewComesFromDomainWithoutMutation() throws Exception {
        JSONObject request = new JSONObject().put("accountId", facade.getPersonalAccount().getId())
                .put("instrumentId", "IDX:BBCA").put("side", "BUY").put("type", "LIMIT")
                .put("quantity", 100).put("limitPrice", "10250").put("requestId", "PREVIEW-1");
        int entries = facade.getLedger().getEntries().size();
        Money before = facade.getPersonalAccount().getReservedCash(Currency.IDR);
        java.lang.reflect.Method method = TradingWebBridge.class.getMethod("previewOrder", String.class);
        JSONObject preview = new JSONObject((String) method.invoke(bridge, request.toString()));
        assertTrue(preview.getBoolean("success"));
        assertEquals("IDR", preview.getString("currency"));
        assertEquals("ACC-PERSONAL-001", preview.getString("accountId"));
        assertTrue(preview.has("estimatedFee"));
        assertTrue(preview.has("estimatedFillQuantity"));
        assertTrue(preview.has("timestamp"));
        assertEquals(entries, facade.getLedger().getEntries().size());
        assertEquals(before, facade.getPersonalAccount().getReservedCash(Currency.IDR));
    }

    @Test
    public void testRetryReturnsCanonicalOrderWithoutSecondReservation() throws Exception {
        JSONObject request = new JSONObject();
        request.put("accountId", facade.getPersonalAccount().getId());
        request.put("instrumentId", "IDX:BBCA");
        request.put("side", "BUY");
        request.put("type", "LIMIT");
        request.put("quantity", 100);
        request.put("limitPrice", "8000");
        request.put("requestId", "RETRY-BRIDGE-ORDER");
        JSONObject first = new JSONObject(bridge.submitOrder(request.toString()));
        Money reserved = facade.getPersonalAccount().getReservedCash(Currency.IDR);
        JSONObject retry = new JSONObject(bridge.submitOrder(request.toString()));
        assertTrue(first.getBoolean("success"));
        assertEquals("Retry must not reserve cash twice", reserved,
                facade.getPersonalAccount().getReservedCash(Currency.IDR));
        assertEquals(first.getString("orderId"), retry.getString("orderId"));
        assertEquals(first.getString("status"), retry.getString("status"));
        assertEquals(1, facade.getOrderManager().getOrdersForAccount(facade.getPersonalAccount().getId()).size());
    }

    @Test
    public void testConflictingOrderRetryRejectsWithoutChangingCanonicalOrder() throws Exception {
        JSONObject request = new JSONObject().put("accountId", facade.getPersonalAccount().getId())
                .put("instrumentId", "IDX:BBCA").put("side", "BUY").put("type", "LIMIT")
                .put("quantity", 100).put("limitPrice", "8000").put("requestId", "CONFLICT-1");
        JSONObject first = new JSONObject(bridge.submitOrder(request.toString()));
        Money reserved = facade.getPersonalAccount().getReservedCash(Currency.IDR);
        int ledgerSize = facade.getLedger().getEntries().size();
        request.put("quantity", 200);
        JSONObject conflict = new JSONObject(bridge.submitOrder(request.toString()));
        assertFalse("Same ID with different payload is not a successful retry", conflict.getBoolean("success"));
        assertEquals(first.getString("orderId"), conflict.getString("orderId"));
        assertEquals(reserved, facade.getPersonalAccount().getReservedCash(Currency.IDR));
        assertEquals(ledgerSize, facade.getLedger().getEntries().size());
        assertEquals(100, facade.getOrderManager().getOrderByRequest("CONFLICT-1").getTotalQuantity());
        assertEquals(1, facade.getOrderManager().getOrdersForAccount(facade.getPersonalAccount().getId()).size());
    }

    @Test
    public void testFxReviewBindsQuoteAndRetryKeepsActualReceipt() throws Exception {
        java.lang.reflect.Method previewMethod = TradingWebBridge.class.getMethod("previewFx", String.class);
        java.lang.reflect.Method confirmMethod = TradingWebBridge.class.getMethod("confirmFx", String.class);
        JSONObject request = new JSONObject().put("accountId", facade.getPersonalAccount().getId())
                .put("from", "IDR").put("to", "USD").put("amount", "1583950").put("requestId", "FX-UI-1");
        int entries = facade.getLedger().getEntries().size();
        JSONObject preview = new JSONObject((String) previewMethod.invoke(bridge, request.toString()));
        assertTrue(preview.getBoolean("success"));
        assertTrue(preview.has("bid"));
        assertTrue(preview.has("ask"));
        assertTrue(preview.has("fee"));
        assertEquals(entries, facade.getLedger().getEntries().size());
        request.put("quoteId", preview.getString("quoteId"));
        facade.getFxService().setMidRate(new BigDecimal("17000"));
        JSONObject stale = new JSONObject((String) confirmMethod.invoke(bridge, request.toString()));
        assertFalse("Changed quote must require another review", stale.getBoolean("success"));
        assertEquals(entries, facade.getLedger().getEntries().size());
        preview = new JSONObject((String) previewMethod.invoke(bridge, request.toString()));
        request.put("quoteId", preview.getString("quoteId"));
        JSONObject filled = new JSONObject((String) confirmMethod.invoke(bridge, request.toString()));
        assertTrue(filled.getBoolean("success"));
        assertEquals(preview.getString("net"), filled.getString("net"));
        facade.getFxService().setMidRate(new BigDecimal("18000"));
        JSONObject retry = new JSONObject((String) confirmMethod.invoke(bridge, request.toString()));
        assertTrue(retry.getBoolean("success"));
        assertEquals(filled.getString("net"), retry.getString("net"));
        assertEquals(filled.getString("quoteId"), retry.getString("quoteId"));
        assertEquals(entries + 2, facade.getLedger().getEntries().size());
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testFirstTradeMissionRequiresFillAndClaimsOnceThroughBridge() throws Exception {
        assertFalse(facade.getCareerManager().getMission("MIS-01").isCompleted());
        bridge.getSnapshot();
        assertFalse(facade.getCareerManager().getMission("MIS-01").isCompleted());
        JSONObject request = new JSONObject().put("accountId", facade.getPersonalAccount().getId())
                .put("instrumentId", "IDX:BBCA").put("side", "BUY").put("type", "LIMIT")
                .put("quantity", 100).put("limitPrice", "8000").put("requestId", "MISSION-OPEN");
        assertTrue(new JSONObject(bridge.submitOrder(request.toString())).getBoolean("success"));
        assertFalse("Unfilled order is not a trade", facade.getCareerManager().getMission("MIS-01").isCompleted());
        bridge.cancelOrder("MISSION-OPEN");
        facade.generateNpcLiquidity();
        request.put("requestId", "MISSION-FILL").put("limitPrice", "11000");
        assertEquals("FILLED", new JSONObject(bridge.submitOrder(request.toString())).getString("status"));
        assertTrue(facade.getCareerManager().getMission("MIS-01").isCompleted());
        java.lang.reflect.Method claim = TradingWebBridge.class.getMethod("claimMission", String.class);
        Money before = facade.getPersonalAccount().getTotalCash(Currency.IDR);
        JSONObject result = new JSONObject((String) claim.invoke(bridge, "MIS-01"));
        assertTrue(result.getBoolean("success"));
        Money after = facade.getPersonalAccount().getTotalCash(Currency.IDR);
        assertEquals(before.add(Money.of(2_500_000, Currency.IDR)), after);
        assertFalse(new JSONObject((String) claim.invoke(bridge, "MIS-01")).getBoolean("success"));
        assertEquals(after, facade.getPersonalAccount().getTotalCash(Currency.IDR));
        facade.save();
        assertTrue(facade.load());
        assertTrue(facade.getCareerManager().getMission("MIS-01").isClaimed());
        assertFalse(new JSONObject((String) claim.invoke(bridge, "MIS-01")).getBoolean("success"));
        assertEquals(after, facade.getPersonalAccount().getTotalCash(Currency.IDR));
    }

    @Test
    public void testReportListingDoesNotRepeatDaySettlement() throws Exception {
        java.lang.reflect.Method reports = TradingWebBridge.class.getMethod("getSessionReports");
        assertEquals(0, new JSONArray((String) reports.invoke(bridge)).length());
        facade.deposit("REPORT-EXTERNAL", facade.getPersonalAccount().getId(), Money.of(1000000, Currency.IDR), "test inflow");
        facade.getClock().play(); facade.getClock().tick(27060); facade.getClock().pause();
        assertTrue(new JSONObject(bridge.closeDay("REPORT-LIST")).getBoolean("success"));
        Money cash = facade.getPersonalAccount().getTotalCash(Currency.IDR);
        java.time.LocalDate date = facade.getClock().getCurrentDate();
        JSONArray first = new JSONArray((String) reports.invoke(bridge));
        assertEquals(1, first.length());
        assertEquals("2026-10-19", first.getJSONObject(0).getString("date"));
        assertEquals("Rp 1.000.000", first.getJSONObject(0).getString("nonInvestmentFlows"));
        facade.save(); assertTrue(facade.load());
        JSONObject reread = new JSONArray((String) reports.invoke(bridge)).getJSONObject(0);
        assertEquals("Rp 1.000.000", reread.getString("nonInvestmentFlows"));
        JSONArray second = new JSONArray((String) reports.invoke(bridge));
        assertEquals(first.toString(), second.toString());
        assertEquals(cash, facade.getPersonalAccount().getTotalCash(Currency.IDR));
        assertEquals(date, facade.getClock().getCurrentDate());
    }

    @Test
    public void testFirmSnapshotSeparatesAumFromPersonalCashWithoutMutations() throws Exception {
        java.lang.reflect.Method method = TradingWebBridge.class.getMethod("getFirmSnapshot");
        int entries = facade.getLedger().getEntries().size();
        JSONObject empty = new JSONObject((String) method.invoke(bridge));
        assertTrue(empty.getBoolean("success"));
        assertEquals("Rp 0", empty.getString("aumIdr"));
        assertEquals(3, empty.getJSONArray("mandates").length());
        assertEquals(entries, facade.getLedger().getEntries().size());
        facade.acceptClientMandate("MANDATE-01");
        facade.acceptClientMandate("MANDATE-02");
        JSONObject active = new JSONObject((String) method.invoke(bridge));
        assertEquals("Rp 350.000.000", active.getString("aumIdr"));
        assertEquals("Rp 50.000.000", active.getString("personalCashIdr"));
        assertEquals(2, active.getInt("activeClientCount"));
        assertTrue(active.getJSONArray("mandates").getJSONObject(0).has("managementFeeAnnualRate"));
        assertTrue(active.getJSONArray("mandates").getJSONObject(0).has("maxConcentration"));
        assertTrue(active.getJSONArray("mandates").getJSONObject(0).has("minCashReserve"));
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testPendingTriggerCanBeCancelledBeforeBookExistsAndAfterReload() throws Exception {
        JSONObject request = new JSONObject().put("accountId", facade.getPersonalAccount().getId())
                .put("instrumentId", "IDX:BBCA").put("side", "BUY").put("type", "STOP_LOSS")
                .put("quantity", 100).put("triggerPrice", "11000").put("requestId", "CANCEL-TRIGGER");
        assertEquals("PENDING_TRIGGER", new JSONObject(bridge.submitOrder(request.toString())).getString("status"));
        assertNull(facade.getMarketEngine().getBook("IDX:BBCA"));
        assertEquals("CANCELLED", new JSONObject(bridge.cancelOrder("CANCEL-TRIGGER")).getString("status"));
        facade.save(); assertTrue(facade.load());
        assertEquals("CANCELLED", new JSONObject(bridge.getOrderStatus("CANCEL-TRIGGER")).getString("status"));
        facade.generateNpcLiquidity(); facade.evaluateTriggers();
        assertEquals("CANCELLED", new JSONObject(bridge.getOrderStatus("CANCEL-TRIGGER")).getString("status"));
        assertEquals(Money.zero(Currency.IDR), facade.getPersonalAccount().getReservedCash(Currency.IDR));
    }

    @Test
    public void testSaveAndLoadViaBridge() throws Exception {
        String saveRes = bridge.saveGame();
        JSONObject saveJson = new JSONObject(saveRes);
        assertTrue(saveJson.getBoolean("success"));

        String loadRes = bridge.loadGame();
        JSONObject loadJson = new JSONObject(loadRes);
        assertTrue(loadJson.getBoolean("success"));
    }

    @Test
    public void testAcceptClientMandateViaBridge() throws Exception {
        String resStr = bridge.acceptClientMandate("MANDATE-01");
        JSONObject res = new JSONObject(resStr);
        assertTrue(res.getBoolean("success"));
        assertEquals("MANDATE-01", res.getString("mandateId"));
        assertNotNull(res.getString("accountId"));

        // Coba terima lagi mandat yang sama -> idempoten, mengembalikan akun yang sama
        String resStr2 = bridge.acceptClientMandate("MANDATE-01");
        JSONObject res2 = new JSONObject(resStr2);
        assertTrue(res2.getBoolean("success"));
        assertEquals(res.getString("accountId"), res2.getString("accountId"));

        // Periksa snapshot firma mencatat 1 klien aktif dan saldo AUM
        JSONObject firmSnap = new JSONObject(bridge.getFirmSnapshot());
        assertEquals(1, firmSnap.getInt("activeClientCount"));
        assertEquals("Rp 100.000.000", firmSnap.getString("aumIdr"));
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testClientAccountOrderPreviewRejectsMandateViolation() throws Exception {
        Account clientAcc = facade.acceptClientMandate("MANDATE-01"); // Max konsentrasi 15%, Min kas 20%
        Instrument bbca = facade.getCatalog().getById("IDX:BBCA");
        assertNotNull(bbca);

        // Beli melebihi batas konsentrasi 15% (3.000 lembar * 10.250 = ~Rp 30.750.000 > 15jt)
        JSONObject req = new JSONObject();
        req.put("accountId", clientAcc.getId());
        req.put("instrumentId", "IDX:BBCA");
        req.put("side", "BUY");
        req.put("type", "LIMIT");
        req.put("quantity", 3000);
        req.put("limitPrice", "10250");
        req.put("requestId", "REQ-CLIENT-PREVIEW-VIOLATE");

        String previewStr = bridge.previewOrder(req.toString());
        JSONObject previewJson = new JSONObject(previewStr);
        assertFalse("Preview harus menolak pelanggaran batas mandat", previewJson.getBoolean("success"));
        assertTrue(previewJson.getString("message").contains("Ditolak Mandat Klien"));
    }

    @Test
    public void testShortSellingAndCoverViaBridge() throws Exception {
        Account mm = facade.getAccount("ACC-NPC-MM-01");
        assertNotNull(mm);
        Position mmPos = mm.getPosition("IDX:BBCA");
        assertNotNull(mmPos);
        long initialLenderQty = mmPos.getQuantity();

        // 1. Buka posisi short 500 lembar via bridge
        JSONObject shortReq = new JSONObject().put("ticker", "BBCA").put("shares", 500);
        String shortResStr = bridge.openShortPosition(shortReq.toString());
        JSONObject shortRes = new JSONObject(shortResStr);
        assertTrue(shortRes.getBoolean("success"));
        assertEquals("BBCA", shortRes.getString("ticker"));
        assertEquals(500, shortRes.getLong("shares"));
        JSONObject visibleShort = new JSONObject(bridge.getRiskSnapshot()).getJSONArray("shortPositions").getJSONObject(0);
        assertEquals("BBCA", visibleShort.getString("ticker"));
        assertEquals(500, visibleShort.getLong("shares"));
        assertEquals(facade.getRiskService().getShortPosition(facade.getPersonalAccount().getId(), "BBCA").getCollateralLocked().format(), visibleShort.getString("collateral"));
        assertTrue(visibleShort.has("liability") && visibleShort.has("dailyBorrowFee") && visibleShort.has("unrealizedPnl"));
        assertEquals("Inventori lender harus berkurang 500 lembar", initialLenderQty - 500, mmPos.getQuantity());

        // 2. Buy to Cover via bridge
        JSONObject coverReq = new JSONObject().put("ticker", "BBCA").put("shares", 500);
        String coverResStr = bridge.closeShortPosition(coverReq.toString());
        JSONObject coverRes = new JSONObject(coverResStr);
        assertTrue(coverRes.getBoolean("success"));
        assertEquals(0, new JSONObject(bridge.getRiskSnapshot()).getJSONArray("shortPositions").length());
        assertEquals("Inventori lender harus pulih kembali setelah cover", initialLenderQty, mmPos.getQuantity());

        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testChartDataPipelineAndOrderBookTape() throws Exception {
        // 1. Sebelum trade: bar referensi tanpa volume palsu
        String chartEmptyRaw = bridge.getChartData("IDX:BBCA", "M5");
        JSONObject chartEmpty = new JSONObject(chartEmptyRaw);
        assertTrue(chartEmpty.getBoolean("success"));
        assertEquals("IDX:BBCA", chartEmpty.getString("instrumentId"));
        assertEquals("M5", chartEmpty.getString("timeframe"));
        assertEquals(0, chartEmpty.getInt("totalTrades"));
        JSONArray emptyBars = chartEmpty.getJSONArray("bars");
        assertEquals(1, emptyBars.length());
        assertTrue(emptyBars.getJSONObject(0).getBoolean("isReference"));
        assertEquals(0, emptyBars.getJSONObject(0).getLong("volume"));

        // 2. Siapkan likuiditas market maker dan eksekusi transaksi beli 1 lot BBCA (@ Rp 10.250)
        facade.generateNpcLiquidity();
        JSONObject buyReq = new JSONObject()
                .put("requestId", "REQ-CHART-TEST-1")
                .put("instrumentId", "IDX:BBCA")
                .put("side", "BUY")
                .put("type", "LIMIT")
                .put("quantity", 100)
                .put("limitPrice", "10500");
        String buyResStr = bridge.submitOrder(buyReq.toString());
        JSONObject buyRes = new JSONObject(buyResStr);
        assertTrue(buyRes.getBoolean("success"));

        // 3. Setelah trade: bar memuat data matched trade nyata
        String chartFilledRaw = bridge.getChartData("IDX:BBCA", "M5");
        JSONObject chartFilled = new JSONObject(chartFilledRaw);
        assertTrue(chartFilled.getBoolean("success"));
        assertTrue(chartFilled.getInt("totalTrades") >= 1);
        JSONArray bars = chartFilled.getJSONArray("bars");
        assertTrue(bars.length() >= 1);
        JSONObject firstBar = bars.getJSONObject(0);
        assertFalse(firstBar.getBoolean("isReference"));
        assertTrue(firstBar.getLong("volume") >= 100);

        // 4. Periksa Order Book dan Tape
        String bookRaw = bridge.getOrderBook("IDX:BBCA");
        JSONObject book = new JSONObject(bookRaw);
        assertTrue(book.has("bids"));
        assertTrue(book.has("asks"));
        assertTrue(book.has("recentTrades"));
        JSONArray tape = book.getJSONArray("recentTrades");
        assertTrue(tape.length() >= 1);
        JSONObject latestTrade = tape.getJSONObject(0);
        assertEquals(100, latestTrade.getLong("quantity"));
    }

    @Test
    public void testPlayerProgressXpAchievementsAndPersistence() throws Exception {
        // 1. Awal permainan: Level 1, 0 XP, 18 achievements (D08)
        String progressRaw = bridge.getPlayerProgress();
        JSONObject progress = new JSONObject(progressRaw);
        assertEquals(1, progress.getInt("level"));
        assertEquals(0, progress.getInt("currentXp"));
        assertEquals(18, progress.getInt("totalAchievements"));
        assertEquals(0, progress.getInt("achievementsUnlocked"));

        // 2. Membaca artikel riset: idempoten, 5 artikel unik membuka 'reader' (150 XP)
        for (int i = 1; i <= 5; i++) {
            facade.getResearchService().addNews(new com.tradingglobal3d.core.research.NewsArticle(
                    "ART-00" + i, "Published article " + i, "Research content",
                    com.tradingglobal3d.core.research.NewsArticle.Category.MACRO, null, 0,
                    facade.getClock().getGameTimestamp() - 1000));
            bridge.recordResearchRead("ART-00" + i);
        }
        // Membaca ulang artikel yang sama tidak boleh memberi reward ganda
        bridge.recordResearchRead("ART-001");

        JSONObject progAfterReader = new JSONObject(bridge.getPlayerProgress());
        assertEquals(150, progAfterReader.getInt("currentXp"));
        assertEquals(2, progAfterReader.getInt("level")); // 150 XP mencapai Level 2 (ambang 100)
        assertEquals(1, progAfterReader.getInt("achievementsUnlocked"));

        // 3. Menerima mandat klien resmi: membuka 'mandate' (200 XP)
        JSONObject mandateRes = new JSONObject(bridge.acceptClientMandate("MANDATE-01"));
        assertTrue(mandateRes.getBoolean("success"));
        JSONObject progAfterMandate = new JSONObject(bridge.getPlayerProgress());
        assertEquals(350, progAfterMandate.getInt("currentXp"));
        assertEquals(2, progAfterMandate.getInt("achievementsUnlocked"));

        // 4. Transaksi pertama: membuka 'first' (100 XP)
        facade.generateNpcLiquidity();
        JSONObject buyReq = new JSONObject()
                .put("requestId", "REQ-PROG-TEST-1")
                .put("instrumentId", "IDX:BBCA")
                .put("side", "BUY")
                .put("type", "LIMIT")
                .put("quantity", 100)
                .put("limitPrice", "10500");
        bridge.submitOrder(buyReq.toString());

        JSONObject progAfterTrade = new JSONObject(bridge.getPlayerProgress());
        assertEquals(450, progAfterTrade.getInt("currentXp"));
        assertEquals(4, progAfterTrade.getInt("level")); // 450 XP mencapai Level 4 (ambang 450)
        assertEquals(3, progAfterTrade.getInt("achievementsUnlocked"));

        // 5. Verifikasi Save dan Restore State
        String saveRes = bridge.saveGame();
        assertTrue(new JSONObject(saveRes).getBoolean("success"));

        // Buat facade baru dan muat save
        TradingFacade reloadedFacade = new TradingFacade(SaveManager.forDirectory(testDir));
        reloadedFacade.setCatalog(facade.getCatalog());
        assertTrue(reloadedFacade.load());

        TradingWebBridge reloadedBridge = new TradingWebBridge(reloadedFacade);
        JSONObject reloadedProg = new JSONObject(reloadedBridge.getPlayerProgress());
        assertEquals(450, reloadedProg.getInt("currentXp"));
        assertEquals(4, reloadedProg.getInt("level"));
        assertEquals(3, reloadedProg.getInt("achievementsUnlocked"));

        // 6. New Game: mereset progres
        reloadedFacade.initializeNewGame(GameMode.INTERNAL_MARKET, BigDecimal.valueOf(50_000_000));
        JSONObject resetProg = new JSONObject(reloadedBridge.getPlayerProgress());
        assertEquals(0, resetProg.getInt("currentXp"));
        assertEquals(1, resetProg.getInt("level"));
        assertEquals(0, resetProg.getInt("achievementsUnlocked"));
    }

    @Test
    public void testMutualFundsSubscriptionRedemptionAndValuation() throws Exception {
        // 1. Get mutual funds catalog
        JSONObject fundsRes = new JSONObject(bridge.getMutualFunds());
        assertTrue(fundsRes.getBoolean("success"));
        JSONArray funds = fundsRes.getJSONArray("funds");
        assertEquals(facade.getMutualFundManager().getAllFunds().size(), funds.length());

        // 2. Subscribe to Money Market Fund Rp 10.000.000
        JSONObject subReq = new JSONObject()
                .put("fundId", "FUND-MM-01")
                .put("requestId", "FUND-BRIDGE-SUB-ONE")
                .put("amount", "10000000");
        JSONObject subRes = new JSONObject(bridge.subscribeMutualFund(subReq.toString()));
        assertTrue(subRes.getBoolean("success"));
        assertEquals("FUND-MM-01", subRes.getString("fundId"));
        assertEquals("ACCEPTED", subRes.getString("status"));
        assertEquals(Money.of(10_000_000, Currency.IDR), facade.getPersonalAccount().getReservedCash(Currency.IDR));
        assertEquals(BigDecimal.ZERO.setScale(6), facade.getMutualFundManager().getFund("FUND-MM-01").getUnits(facade.getPersonalAccount().getId()));
        facade.getClock().play();
        facade.advanceRealMs(93750, true); // 08:30 -> 16:00: 27,000s game / 288.
        facade.getClock().pause();
        assertEquals("SETTLED", facade.getMutualFundManager().getFund("FUND-MM-01").getOrder("FUND-BRIDGE-SUB-ONE").getString("status"));
        assertTrue(facade.reconcileAll());

        // Verifikasi kepemilikan unit dan valuasi kekayaan bersih
        Money netWorth = facade.getPersonalNetWorth();
        assertTrue(netWorth.isPositive());

        // 3. Redeem sebagian unit
        JSONObject redReq = new JSONObject()
                .put("fundId", "FUND-MM-01")
                .put("requestId", "FUND-BRIDGE-RED-ONE")
                .put("units", "500.000000");
        JSONObject redRes = new JSONObject(bridge.redeemMutualFund(redReq.toString()));
        assertTrue(redRes.toString(), redRes.getBoolean("success"));
        assertEquals("ACCEPTED", redRes.getString("status"));
        facade.skipDay("FUND-NEXT-CUTOFF");
        assertEquals("SETTLED", facade.getMutualFundManager().getFund("FUND-MM-01").getOrder("FUND-BRIDGE-RED-ONE").getString("status"));
        assertTrue(facade.reconcileAll());
    }

    @Test
    public void testInstallmentLoansDisbursementPaymentAndPrepay() throws Exception {
        // 1. Get loan products
        JSONObject prodsRes = new JSONObject(bridge.getLoanProducts());
        assertTrue(prodsRes.getBoolean("success"));
        assertEquals(2, prodsRes.getJSONArray("products").length());

        // 2. Alur pinjaman wajib: getLoanQuote → disburseReviewedLoan
        // Beli koleksi sebagai agunan (personal loan wajib agunan)
        facade.buyCollection("COL-CAR-00"); // Vespa Klasik sebagai agunan

        // 2a. Get quote
        JSONObject quoteReq = new JSONObject()
                .put("productId", "LOAN-PERS-01")
                .put("isPersonal", true)
                .put("principal", "10000000")
                .put("tenorDays", 14)
                .put("collateralItemId", "COL-CAR-00");
        JSONObject quoteRes = new JSONObject(bridge.getLoanQuote(quoteReq.toString()));
        assertTrue("Quote harus berhasil: " + quoteRes.optString("message",""), quoteRes.getBoolean("success"));
        String quoteVersion = quoteRes.getString("quoteVersion");

        // 2b. Disburse setelah review (confirmed via quoteVersion)
        JSONObject disburseReq = new JSONObject()
                .put("requestId", "BRIDGE-LOAN-DISBURSE-01")
                .put("productId", "LOAN-PERS-01")
                .put("isPersonal", true)
                .put("principal", "10000000")
                .put("tenorDays", 14)
                .put("collateralItemId", "COL-CAR-00")
                .put("quoteVersion", quoteVersion);
        JSONObject disburseRes = new JSONObject(bridge.disburseReviewedLoan(disburseReq.toString()));
        assertTrue(disburseRes.getBoolean("success"));
        String loanId = disburseRes.getString("loanId");

        // 3. List loans
        JSONObject loansList = new JSONObject(bridge.getLoans());
        assertTrue(loansList.getBoolean("success"));
        assertTrue(loansList.getJSONArray("loans").length() >= 1);

        // 4. Pay partial installment
        JSONObject payReq = new JSONObject()
                .put("loanId", loanId)
                .put("requestId", "BRIDGE-PAY-ONCE")
                .put("amount", "1000000");
        JSONObject payRes = new JSONObject(bridge.payLoan(payReq.toString()));
        assertTrue(payRes.getBoolean("success"));

        // 5. Prepay full loan
        JSONObject prepayRes = new JSONObject(bridge.prepayLoan(loanId));
        assertTrue(prepayRes.getBoolean("success"));
        assertEquals("PAID_OFF", prepayRes.getString("status"));
    }

    @Test
    public void testPeriodicMissionsAndLeague() throws Exception {
        // 1. Periodic missions
        JSONObject missionsRes = new JSONObject(bridge.getPeriodicMissions());
        assertTrue(missionsRes.getBoolean("success"));
        JSONArray activeMissions = missionsRes.getJSONArray("activeMissions");
        assertTrue(activeMissions.length() >= 3);

        // 2. League leaderboard
        JSONObject lbRes = new JSONObject(bridge.getLeagueLeaderboard("NET_WORTH"));
        assertTrue(lbRes.getBoolean("success"));
        assertEquals(21, lbRes.getInt("totalCompetitors")); // 20 NPC + 1 player

        // 3. Rival status
        JSONObject rivalRes = new JSONObject(bridge.getRivalStatus());
        assertTrue(rivalRes.getBoolean("success"));
        assertNotNull(rivalRes.getString("rivalName"));

        // 4. Investor archetype & player profile
        JSONObject archRes = new JSONObject(bridge.getInvestorArchetype());
        assertNotNull(archRes.getString("archetype"));

        JSONObject profRes = new JSONObject(bridge.getPlayerProfile());
        assertNotNull(profRes.getString("playerName"));

        // 5. Consolidated wealth breakdown (D02)
        JSONObject wealthRes = new JSONObject(bridge.getConsolidatedWealth());
        assertTrue(wealthRes.getBoolean("success"));
        assertTrue(wealthRes.getBoolean("isReconciled"));
        assertNotNull(wealthRes.getJSONObject("components"));
        assertNotNull(wealthRes.getJSONObject("liabilities"));
    }
}
