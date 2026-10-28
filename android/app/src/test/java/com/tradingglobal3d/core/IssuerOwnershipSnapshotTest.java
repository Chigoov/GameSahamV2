package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.nio.file.*;
import java.math.BigDecimal;
import static org.junit.Assert.*;

public class IssuerOwnershipSnapshotTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private TradingFacade game() throws Exception {
        TradingFacade g=new TradingFacade(SaveManager.forDirectory(temp.newFolder()));
        g.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")),java.nio.charset.StandardCharsets.UTF_8)));
        g.setRealTimeClock(java.time.Clock.fixed(java.time.Instant.ofEpochMilli(1792756800000L),java.time.ZoneOffset.UTC));
        assertTrue(g.startOrResume());return g;
    }
    @Test public void zeroPlayerSharesStillShowsRecordedWorldOwnersWithoutInventingPublic() throws Exception {
        TradingFacade g=game();
        java.lang.reflect.Method method=TradingFacade.class.getMethod("getIssuerOwnership",String.class);
        JSONObject snapshot=(JSONObject)method.invoke(g,"IDX:BBCA");
        assertEquals("PARTIAL",snapshot.getString("coverage"));
        assertTrue(snapshot.getJSONArray("holders").length()>0);
        long sum=0;
        for(int i=0;i<snapshot.getJSONArray("holders").length();i++){
            JSONObject h=snapshot.getJSONArray("holders").getJSONObject(i);
            assertFalse(h.getBoolean("isPersonalPlayer"));
            assertNotEquals("Publik",h.getString("name"));
            Account account=g.getAccount(h.getString("holderId"));assertNotNull(account);
            assertEquals(account.getPosition("IDX:BBCA").getQuantity(),h.getLong("quantity"));sum+=h.getLong("quantity");
        }
        assertEquals(sum,snapshot.getLong("mappedShares"));assertTrue(sum<snapshot.getLong("sharesOutstanding"));
        assertEquals(snapshot.getLong("sharesOutstanding")-sum,snapshot.getLong("unmappedShares"));
    }
    @Test public void reservationsDoNotCreateASecondOwnerAndShortDoesNotAddPositiveSupply() throws Exception {
        TradingFacade g=game();
        g.getPersonalAccount().addPosition(new Position("BBCA","IDX",Currency.IDR,500,new BigDecimal("9000"),new BigDecimal("9000")));
        java.lang.reflect.Method method=TradingFacade.class.getMethod("getIssuerOwnership",String.class);
        JSONObject snapshot=(JSONObject)method.invoke(g,"IDX:BBCA");
        int player=0;for(int i=0;i<snapshot.getJSONArray("holders").length();i++){
            JSONObject h=snapshot.getJSONArray("holders").getJSONObject(i);
            if(h.getBoolean("isPersonalPlayer")){player++;assertEquals(500,h.getLong("quantity"));assertTrue(h.getDouble("percent")>0);}
        }assertEquals(1,player);
    }

    @Test public void shortObligationIsReportedWithoutCreatingAnAdditionalHolder() throws Exception {
        TradingFacade g = game();
        GameTestSupport.unlockManager(g);
        JSONObject before = g.getIssuerOwnership("IDX:BBCA");
        g.openShortPosition("BBCA", 100);
        JSONObject during = g.getIssuerOwnership("IDX:BBCA");
        assertEquals(100, during.getLong("recordedShortShares"));
        assertEquals(before.getLong("mappedShares") - 100, during.getLong("mappedShares"));
        assertEquals(before.getJSONArray("holders").length(), during.getJSONArray("holders").length());
        assertEquals("PARTIAL", during.getString("coverage"));
        g.closeShortPosition("BBCA", 100);
        assertEquals(before.getLong("mappedShares"), g.getIssuerOwnership("IDX:BBCA").getLong("mappedShares"));
        assertTrue(g.reconcileAll());
    }

    @Test public void invalidDenominatorReturnsUnknownWithoutInventedPercent() throws Exception {
        TradingFacade g = game();
        for (long supply : new long[]{0, -1}) {
            com.tradingglobal3d.core.market.Instrument original = g.getCatalog().getById("IDX:BBCA");
            g.getCatalog().registerInstrument(new com.tradingglobal3d.core.market.Instrument(original.getId(), original.getTicker(), original.getName(),
                    original.getExchange(), original.getSector(), original.getCurrency(), original.getLotSize(), supply, original.isActiveTrading(), original.getCurrentPrice()));
            JSONObject snapshot = g.getIssuerOwnership("IDX:BBCA");
            assertEquals("UNKNOWN", snapshot.getString("coverage"));
            assertEquals("INVALID", snapshot.getString("denominatorStatus"));
            assertTrue(snapshot.isNull("unmappedShares"));
            JSONArray holders = snapshot.getJSONArray("holders");
            assertTrue(holders.length() > 0);
            for (int i = 0; i < holders.length(); i++) assertTrue(holders.getJSONObject(i).isNull("percent"));
        }
    }

    @Test public void multipleHoldersConservationAndClientSeparation() throws Exception {
        TradingFacade g = game();
        Account personal = g.getPersonalAccount();
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 300, new BigDecimal("9000"), new BigDecimal("9000")));

        // Tambah akun klien terpisah
        Account client = new Account("ACC-CLIENT-001", "Klien Prioritas", AccountType.CLIENT, "MANDATE-01");
        client.addPosition(new Position("BBCA", "IDX", Currency.IDR, 200, new BigDecimal("9100"), new BigDecimal("9100")));
        g.registerAccount(client);

        JSONObject snapshot = g.getIssuerOwnership("IDX:BBCA");
        JSONArray holders = snapshot.getJSONArray("holders");
        long totalSum = 0;
        boolean foundPersonal = false;
        boolean foundClient = false;

        for (int i = 0; i < holders.length(); i++) {
            JSONObject h = holders.getJSONObject(i);
            totalSum += h.getLong("quantity");
            if (h.getBoolean("isPersonalPlayer")) {
                assertEquals(300, h.getLong("quantity"));
                assertFalse(h.getBoolean("isClientAccount"));
                foundPersonal = true;
            } else if (h.getBoolean("isClientAccount")) {
                assertEquals(200, h.getLong("quantity"));
                assertFalse(h.getBoolean("isPersonalPlayer"));
                foundClient = true;
            }
        }
        assertTrue("Akun personal harus ditemukan terpisah", foundPersonal);
        assertTrue("Akun klien harus ditemukan terpisah", foundClient);
        assertEquals(totalSum, snapshot.getLong("mappedShares"));
    }

    @Test public void tradeFillTransfersSharesConservingTotalMappedShares() throws Exception {
        TradingFacade g = game();
        Account buyer = g.getPersonalAccount();
        Account seller = g.getAccount("ACC-NPC-MM-01");
        assertNotNull(seller);

        JSONObject before = g.getIssuerOwnership("IDX:BBCA");
        long mappedBefore = before.getLong("mappedShares");

        g.generateNpcLiquidity();
        com.tradingglobal3d.core.market.Instrument instrument = g.getCatalog().getById("IDX:BBCA");
        BigDecimal ask = g.getMarketEngine().getBook(instrument.getId()).getAskLevels(1).get(0).price;
        long transferQty = 200;
        com.tradingglobal3d.core.trading.Order buy = com.tradingglobal3d.core.trading.Order.createLimit("OWN-FILL", instrument.getId(), buyer.getId(),
                com.tradingglobal3d.core.trading.OrderSide.BUY, new Money(ask, Currency.IDR), transferQty, g.getClock().getGameTimestamp());
        assertTrue(g.submitOrder(buy).isSuccess);
        assertEquals(com.tradingglobal3d.core.trading.OrderStatus.FILLED, buy.getStatus());
        assertEquals(transferQty, buyer.getPosition(instrument.getId()).getQuantity());
        assertTrue(g.reconcileAll());

        JSONObject after = g.getIssuerOwnership("IDX:BBCA");
        long mappedAfter = after.getLong("mappedShares");
        assertEquals("Total lembar saham terpetakan harus terkonservasi", mappedBefore, mappedAfter);
    }

    @Test public void zeroOrClosedPositionNotAddedToHolders() throws Exception {
        TradingFacade g = game();
        Account closedAcc = new Account("ACC-CLOSED-001", "Closed Trader", AccountType.PERSONAL, null);
        Position pos = new Position("BBCA", "IDX", Currency.IDR, 200, new BigDecimal("9000"), new BigDecimal("9000"));
        pos.addSell(200, new BigDecimal("9200"));
        assertEquals(0, pos.getQuantity());
        closedAcc.addPosition(pos);
        g.registerAccount(closedAcc);

        JSONObject snapshot = g.getIssuerOwnership("IDX:BBCA");
        for (int i = 0; i < snapshot.getJSONArray("holders").length(); i++) {
            assertNotEquals("ACC-CLOSED-001", snapshot.getJSONArray("holders").getJSONObject(i).getString("holderId"));
        }
    }

    @Test public void pendingSellAndCancellationDoNotTransferOwnership() throws Exception {
        TradingFacade g = game();
        Account personal = g.getPersonalAccount();
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 200, new BigDecimal("9000"), new BigDecimal("10000")));
        JSONObject before = g.getIssuerOwnership("IDX:BBCA");
        com.tradingglobal3d.core.trading.Order sell = com.tradingglobal3d.core.trading.Order.createLimit("OWN-RESERVE", "IDX:BBCA", personal.getId(),
                com.tradingglobal3d.core.trading.OrderSide.SELL, Money.of(50000, Currency.IDR), 100, g.getClock().getGameTimestamp());
        assertTrue(g.submitOrder(sell).isSuccess);
        assertEquals(100, g.getCommittedShares(personal.getId(), "IDX:BBCA"));
        assertEquals(before.toString(), g.getIssuerOwnership("IDX:BBCA").toString());
        assertTrue(g.cancelOrder(sell));
        assertEquals(0, g.getCommittedShares(personal.getId(), "IDX:BBCA"));
        assertEquals(before.toString(), g.getIssuerOwnership("IDX:BBCA").toString());
    }

    @Test public void overmappedSupplyIsNotClampedAndHolderIdsRemainUnique() throws Exception {
        TradingFacade g = game();
        com.tradingglobal3d.core.market.Instrument original = g.getCatalog().getById("IDX:BBCA");
        g.getCatalog().registerInstrument(new com.tradingglobal3d.core.market.Instrument(original.getId(), original.getTicker(), original.getName(),
                original.getExchange(), original.getSector(), original.getCurrency(), original.getLotSize(), 3, true, original.getCurrentPrice()));
        JSONObject snapshot = g.getIssuerOwnership("IDX:BBCA");
        assertEquals("INCONSISTENT", snapshot.getString("coverage"));
        java.util.Set<String> ids = new java.util.HashSet<>();
        boolean over100 = false;
        for (int i = 0; i < snapshot.getJSONArray("holders").length(); i++) {
            JSONObject holder = snapshot.getJSONArray("holders").getJSONObject(i);
            assertTrue(ids.add(holder.getString("holderId")));
            BigDecimal expected = BigDecimal.valueOf(holder.getLong("quantity")).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(3), 8, java.math.RoundingMode.HALF_UP);
            assertEquals(0, expected.compareTo(new BigDecimal(holder.get("percent").toString())));
            over100 |= holder.getDouble("percent") > 100;
        }
        assertTrue(over100);
        assertFalse(snapshot.getBoolean("registryComplete"));
    }

    @Test public void splitUpdatesDynamicSupplyAndPreservesMarketCapitalizationAfterRestore() throws Exception {
        TradingFacade g = game();
        JSONObject state = g.getCorporateService().exportState();
        java.time.LocalDate date = g.getClock().getCurrentDate();
        state.getJSONArray("corporateActions").put(new JSONObject().put("id", "QA-SPLIT").put("ticker", "BBCA")
                .put("type", "STOCK_SPLIT").put("valueOrRatio", "2").put("currency", "IDR")
                .put("recordingDate", date.toString()).put("executionDate", date.toString()).put("isExecuted", false));
        g.getCorporateService().restoreState(state);
        com.tradingglobal3d.core.corporate.CorporateAction split = g.getCorporateService().getCorporateActions().stream().filter(action -> action.getId().equals("QA-SPLIT")).findFirst().get();
        JSONObject before = g.getIssuerOwnership("IDX:BBCA");
        Money capBefore = g.getCatalog().getById("IDX:BBCA").getMarketCap();
        double indexBefore = g.getCatalog().getIndex("IDX:COMPOSITE").getCurrentValue();
        g.executeStockSplit(split);
        g.getCatalog().recalculateAllIndices();
        JSONObject after = g.getIssuerOwnership("IDX:BBCA");
        assertEquals(before.getLong("mappedShares") * 2, after.getLong("mappedShares"));
        assertEquals(before.getLong("sharesOutstanding") * 2, after.getLong("sharesOutstanding"));
        assertEquals("DYNAMIC", after.getString("denominatorStatus"));
        assertEquals("PARTIAL", after.getString("coverage"));
        assertEquals(capBefore, g.getCatalog().getById("IDX:BBCA").getMarketCap());
        assertEquals(indexBefore, g.getCatalog().getIndex("IDX:COMPOSITE").getCurrentValue(), 0.01);
        for (int i = 0; i < after.getJSONArray("holders").length(); i++)
            assertEquals(before.getJSONArray("holders").getJSONObject(i).get("percent"), after.getJSONArray("holders").getJSONObject(i).get("percent"));
        g.executeStockSplit(split);
        assertEquals(after.toString(), g.getIssuerOwnership("IDX:BBCA").toString());
        g.save(); assertTrue(g.load());
        assertEquals(after.toString(), g.getIssuerOwnership("IDX:BBCA").toString());
    }

    @Test public void rightsAndBuybackFollowSettledPositionsAndTreasuryWithoutDoubleCounting() throws Exception {
        TradingFacade g = game();
        Account personal = g.getPersonalAccount();
        personal.addPosition(new Position("BBCA", "IDX", Currency.IDR, 1000, new BigDecimal("9000"), new BigDecimal("10000")));
        personal.addPosition(new Position("BMRI", "IDX", Currency.IDR, 1000, new BigDecimal("7000"), new BigDecimal("7400")));
        java.util.Map<String, Account> accounts = new java.util.LinkedHashMap<>();
        for (Account account : g.getAllAccounts()) accounts.put(account.getId(), account);
        java.time.LocalDate base = g.getClock().getCurrentDate();
        g.getCorporateService().processDate(base.plusDays(3), accounts, g.getLedger(), g.getClock().getGameTimestamp());
        long bbcaBefore = g.getIssuerOwnership("IDX:BBCA").getLong("mappedShares");
        long bmriBefore = g.getIssuerOwnership("IDX:BMRI").getLong("mappedShares");
        long bbcaSupply = g.getCatalog().getById("IDX:BBCA").getSharesOutstanding();
        long bmriSupply = g.getCatalog().getById("IDX:BMRI").getSharesOutstanding();
        g.getCorporateService().participateOffer("RIGHTS-BBCA", personal, 100, "QA-RIGHTS", base.plusDays(3), g.getLedger(), g.getClock().getGameTimestamp());
        g.getCorporateService().participateOffer("BUYBACK-BMRI", personal, 100, "QA-BUYBACK", base.plusDays(3), g.getLedger(), g.getClock().getGameTimestamp());
        assertEquals(bmriBefore, g.getIssuerOwnership("IDX:BMRI").getLong("mappedShares"));
        g.getCorporateService().processDate(base.plusDays(7), accounts, g.getLedger(), g.getClock().getGameTimestamp());
        for (Account account : accounts.values()) if (g.getAccount(account.getId()) == null) g.registerAccount(account);
        assertEquals(bbcaBefore + 100, g.getIssuerOwnership("IDX:BBCA").getLong("mappedShares"));
        assertEquals(bbcaSupply + 100, g.getCatalog().getById("IDX:BBCA").getSharesOutstanding());
        assertEquals(bmriSupply - 100, g.getCatalog().getById("IDX:BMRI").getSharesOutstanding());
        assertEquals(bmriSupply, g.getCatalog().getById("IDX:BMRI").getIssuedShares());
        assertEquals("PARTIAL", g.getIssuerOwnership("IDX:BBCA").getString("coverage"));
        assertEquals("DYNAMIC", g.getIssuerOwnership("IDX:BBCA").getString("denominatorStatus"));
        assertEquals(bmriBefore - 100, g.getIssuerOwnership("IDX:BMRI").getLong("mappedShares"));
        assertEquals(100, g.getIssuerOwnership("IDX:BMRI").getLong("treasuryShares"));
        assertEquals(900, personal.getPosition("IDX:BMRI").getQuantity());
        assertEquals(100, g.getAccount("ACC-CORP-BUYBACK-BMRI").getPosition("IDX:BMRI").getQuantity());
        assertTrue(g.reconcileAll());
        JSONObject beforeRestore = g.getIssuerOwnership("IDX:BMRI");
        g.save(); assertTrue(g.load());
        assertEquals(beforeRestore.toString(), g.getIssuerOwnership("IDX:BMRI").toString());
    }

    @Test public void fundPoolAndManyAccountsAreRecordedOnceWithoutAttributingUnitsAsShares() throws Exception {
        TradingFacade g = game();
        com.tradingglobal3d.core.funds.MutualFund fund = g.getMutualFundManager().getFund("FUND-EQ-01");
        assertNotNull(fund);
        Account pool = g.getAccount(fund.getPoolAccountId());
        assertNotNull(pool);
        pool.addPosition(new Position("BBCA", "IDX", Currency.IDR, 500, new BigDecimal("9000"), new BigDecimal("10000")));
        fund.submitSubscription("QA-POOL-UNIT", g.getPersonalAccount(), Money.of(1000000, Currency.IDR), g.getLedger(), g.getClock().getGameTimestamp());
        java.util.Map<String, Account> accountMap = new java.util.LinkedHashMap<>();
        for (Account account : g.getAllAccounts()) accountMap.put(account.getId(), account);
        g.getMutualFundManager().processCutoff(accountMap, g.getLedger(), g.getClock().getCurrentDate().plusDays(1).atTime(16, 0).toInstant(java.time.ZoneOffset.ofHours(7)).toEpochMilli());
        assertTrue(fund.getUnits(g.getPersonalAccount().getId()).signum() > 0);
        for (int i = 0; i < 40; i++) {
            Account holder = new Account("QA-HOLDER-" + i, "Pemegang QA " + i, AccountType.FIRM, null);
            holder.addPosition(new Position("BBCA", "IDX", Currency.IDR, i + 1, new BigDecimal("9000"), new BigDecimal("10000")));
            g.registerAccount(holder);
        }
        JSONObject snapshot = g.getIssuerOwnership("IDX:BBCA");
        java.util.Set<String> ids = new java.util.HashSet<>();
        int pools = 0;
        long total = 0;
        for (int i = 0; i < snapshot.getJSONArray("holders").length(); i++) {
            JSONObject holder = snapshot.getJSONArray("holders").getJSONObject(i);
            assertTrue(ids.add(holder.getString("holderId")));
            assertFalse("Unit holders do not become direct equity holders", holder.getBoolean("isPersonalPlayer"));
            if (pool.getId().equals(holder.getString("holderId"))) { pools++; assertEquals(500, holder.getLong("quantity")); }
            total = Math.addExact(total, holder.getLong("quantity"));
        }
        assertEquals(1, pools);
        assertEquals(total, snapshot.getLong("mappedShares"));
        assertTrue(g.reconcileAll());
    }

    @Test public void saveAndRestorePreservesOwnershipSnapshot() throws Exception {
        java.io.File dir = temp.newFolder();
        TradingFacade g1 = new TradingFacade(SaveManager.forDirectory(dir));
        g1.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")), java.nio.charset.StandardCharsets.UTF_8)));
        g1.setRealTimeClock(java.time.Clock.fixed(java.time.Instant.ofEpochMilli(1792756800000L), java.time.ZoneOffset.UTC));
        assertTrue(g1.startOrResume());
        g1.getPersonalAccount().addPosition(new Position("BBCA", "IDX", Currency.IDR, 700, new BigDecimal("9000"), new BigDecimal("9000")));

        JSONObject before = g1.getIssuerOwnership("IDX:BBCA");
        g1.save();

        TradingFacade g2 = new TradingFacade(SaveManager.forDirectory(dir));
        g2.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")), java.nio.charset.StandardCharsets.UTF_8)));
        g2.setRealTimeClock(java.time.Clock.fixed(java.time.Instant.ofEpochMilli(1792756800000L), java.time.ZoneOffset.UTC));
        assertTrue(g2.startOrResume());

        JSONObject after = g2.getIssuerOwnership("IDX:BBCA");
        assertEquals(before.getLong("mappedShares"), after.getLong("mappedShares"));
        assertEquals(before.getLong("sharesOutstanding"), after.getLong("sharesOutstanding"));
        assertEquals(before.getJSONArray("holders").length(), after.getJSONArray("holders").length());
    }
}
