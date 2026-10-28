package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import com.tradingglobal3d.mobile.TradingWebBridge;
import org.json.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;

public class ClockNewsOrderCriticalSequenceTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private static final long START = 1792756800000L;
    private TradingFacade open(File dir, long now) throws Exception {
        TradingFacade g = new TradingFacade(SaveManager.forDirectory(dir));
        g.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(Paths.get("src/main/assets/data/market_universe.json"))));
        time(g, now); assertTrue(g.startOrResume()); return g;
    }
    private void time(TradingFacade g, long now) { g.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(now), ZoneOffset.UTC)); }
    private JSONObject article(TradingFacade g) throws Exception {
        JSONArray news = new JSONArray(g.getNewsFeedJson());
        for (int i=0;i<news.length();i++) if (news.getJSONObject(i).getString("id").equals("NEWS-02")) return news.getJSONObject(i);
        throw new AssertionError("missing news");
    }
    private void invariant(TradingFacade g, long initial, long elapsed, long deadline, String ctx) throws Exception {
        assertTrue(ctx, g.reconcileAll());
        assertEquals(ctx, initial + elapsed * 24 / 1000 * 1000, g.getGameTimestamp());
        assertEquals(ctx, elapsed * 24 % 1000, g.getClock().getGameMsRemainder());
        assertEquals(ctx, deadline, article(g).getLong("effectEndsRealAt"));
        Set<String> ledgerIds = new HashSet<>();
        for (var e : g.getLedger().getEntries()) assertTrue(ctx + " duplicate ledger", ledgerIds.add(e.getRequestId()));
        for (Account account : g.getAllAccounts()) {
            for (Currency c : Currency.values()) {
                assertEquals(ctx, account.getTotalCash(c), account.getAvailableCash(c).add(account.getReservedCash(c)));
                assertFalse(ctx, account.getAvailableCash(c).isNegative());
            }
            for (Position p : account.getPositions().values()) assertTrue(ctx, p.getQuantity() >= 0);
        }
        Set<String> fillIds = new HashSet<>();
        for (var instrument : g.getCatalog().getActiveUniverse()) {
            var book = g.getMarketEngine().getBook(instrument.getId());
            if (book != null) for (TradeFill f : book.getTradeHistory()) assertTrue(ctx, fillIds.add(f.getFillId()));
        }
    }
    @Test public void tenSeedsHundredMeaningfulMixedActionsWithPerActionInvariants() throws Exception {
        for (int seed=0;seed<10;seed++) {
            File dir=temp.newFolder();long now=START;
            TradingFacade g=open(dir,now);long initial=g.getGameTimestamp(),deadline=article(g).getLong("effectEndsRealAt");
            Random random=new Random(71000+seed);int count=0;
            for(int cycle=0;cycle<10;cycle++) {
                // Vary elapsed and lifecycle order per seed; all actions mutate or assert a specified rejection/retry.
                long delta=10000+random.nextInt(20001);now+=delta;time(g,now);g.syncClock();
                invariant(g,initial,now-START,deadline,"seed"+seed+" action"+(++count));
                g.onAppLifecycleChanged(false);now+=delta;time(g,now);g.onAppLifecycleChanged(true);
                invariant(g,initial,now-START,deadline,"seed"+seed+" action"+(++count));
                g.save();now+=delta;g=open(dir,now);
                invariant(g,initial,now-START,deadline,"seed"+seed+" action"+(++count));
                long untilOpen = g.getClock().getCurrentTime().isBefore(LocalTime.of(9,0)) ? 75000 : 1;
                now+=untilOpen;time(g,now);g.syncClock();
                invariant(g,initial,now-START,deadline,"seed"+seed+" action"+(++count));
                // Controlled makers use an empty existing book to guarantee partial fill, not synthetic balances.
                var book=g.getMarketEngine().getOrCreateBook(g.getCatalog().getById("IDX:BBCA"));
                for(Order o:new ArrayList<>(g.getMarketEngine().getRestingOrders())) if(o.getInstrumentId().equals("IDX:BBCA"))
                    g.getMarketEngine().cancelOrder(o,g.getAccount(o.getAccountId()));
                String req="SEQ-"+seed+"-"+cycle;
                Account maker=g.getAccount("ACC-NPC-MM-01");
                Order sell=Order.createLimit(req+"-MAKER","IDX:BBCA",maker.getId(),OrderSide.SELL,Money.of(9000,Currency.IDR),100,g.getGameTimestamp());
                assertTrue(g.submitOrder(sell).isSuccess);
                invariant(g,initial,now-START,deadline,"seed"+seed+" action"+(++count));
                Order buy=new Order(req,req,g.getPersonalAccount().getId(),"IDX:BBCA",OrderSide.BUY,OrderType.LIMIT,Money.of(9000,Currency.IDR),null,200,g.getGameTimestamp(),null);
                assertTrue(g.submitOrder(buy).isSuccess);assertEquals(100,buy.getFilledQuantity());
                invariant(g,initial,now-START,deadline,"seed"+seed+" action"+(++count));
                int entries=g.getLedger().getEntries().size();g.submitOrder(buy);assertEquals(entries,g.getLedger().getEntries().size());
                invariant(g,initial,now-START,deadline,"seed"+seed+" action"+(++count));
                assertTrue(g.cancelOrder(buy));assertEquals(OrderStatus.CANCELLED,buy.getStatus());
                invariant(g,initial,now-START,deadline,"seed"+seed+" action"+(++count));
                JSONObject invalid=new JSONObject(new TradingWebBridge(g).submitOrder(new JSONObject().put("requestId",req+"-INVALID").put("instrumentId","IDX:BBCA").put("accountId",g.getPersonalAccount().getId()).put("side","BUY").put("type","LIMIT").put("quantity",0).put("limitPrice","9000").toString()));
                assertFalse(invalid.getBoolean("success"));
                invariant(g,initial,now-START,deadline,"seed"+seed+" action"+(++count));
                // Cross a real news deadline offline, then restart again; no historical effect loss or replay.
                now+=400000;g.onAppLifecycleChanged(false);time(g,now);g.onAppLifecycleChanged(true);g.save();g=open(dir,now);
                invariant(g,initial,now-START,deadline,"seed"+seed+" action"+(++count));
            }
            assertEquals(100,count);assertEquals("ENDED",article(g).getString("effectStatus"));
            System.out.println("seed="+(71000+seed)+" meaningful_actions="+count+" deadline="+deadline+" finalWorld="+g.getGameTimestamp());
        }
    }
}
