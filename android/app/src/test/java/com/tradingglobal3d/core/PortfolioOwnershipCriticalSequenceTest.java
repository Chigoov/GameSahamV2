package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.Account;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.model.Money;
import com.tradingglobal3d.core.model.Position;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;

/** PLAN-04: ten seeded sequences, 100 state-changing actions, invariants after every action. */
@RunWith(Parameterized.class)
public class PortfolioOwnershipCriticalSequenceTest {
    @Parameterized.Parameters(name = "seed={0}")
    public static Object[] seeds() { return new Object[]{41001L,41002L,41003L,41004L,41005L,41006L,41007L,41008L,41009L,41010L}; }
    @Parameterized.Parameter public long seed;
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private static final long EPOCH = 1792756800000L;

    private TradingFacade loadGame(File dir, boolean restore) throws Exception {
        TradingFacade game = new TradingFacade(SaveManager.forDirectory(dir));
        game.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")), StandardCharsets.UTF_8)));
        game.setRealTimeClock(java.time.Clock.fixed(java.time.Instant.ofEpochMilli(EPOCH), java.time.ZoneOffset.UTC));
        assertTrue(restore ? game.load() : game.startOrResume());
        GameTestSupport.unlockManager(game);
        return game;
    }

    private void invariant(TradingFacade game, long baseline, int action) throws Exception {
        String context = "seed=" + seed + " action=" + action;
        assertTrue(context, game.reconcileAll());
        for (Account account : game.getAllAccounts()) {
            for (Currency currency : Currency.values()) {
                assertEquals(context, account.getTotalCash(currency), account.getAvailableCash(currency).add(account.getReservedCash(currency)));
                assertFalse(context, account.getAvailableCash(currency).isNegative());
                assertFalse(context, account.getReservedCash(currency).isNegative());
            }
            for (Position position : account.getPositions().values()) assertTrue(context, position.getQuantity() >= 0);
        }
        JSONObject snapshot = game.getIssuerOwnership("IDX:BBCA");
        long total = 0;
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < snapshot.getJSONArray("holders").length(); i++) {
            JSONObject holder = snapshot.getJSONArray("holders").getJSONObject(i);
            assertTrue(context, ids.add(holder.getString("holderId")));
            long quantity = game.getAccount(holder.getString("holderId")).getPosition("IDX:BBCA").getQuantity();
            assertEquals(context, quantity, holder.getLong("quantity"));
            total = Math.addExact(total, quantity);
        }
        assertEquals(context, total, snapshot.getLong("mappedShares"));
        assertEquals(context, baseline, Math.addExact(total, snapshot.getLong("recordedShortShares")));
        Account personal = game.getPersonalAccount();
        assertTrue(context, game.getCommittedShares(personal.getId(), "IDX:BBCA") <= personal.getPosition("IDX:BBCA").getQuantity());
    }

    @Test public void oneHundredPortfolioAndOwnershipActions() throws Exception {
        File dir = temp.newFolder();
        TradingFacade game = loadGame(dir, false);
        game.getPersonalAccount().addPosition(new Position("BBCA", "IDX", Currency.IDR, 2000, new BigDecimal("9000"), new BigDecimal("10000")));
        long baseline = game.getIssuerOwnership("IDX:BBCA").getLong("mappedShares");
        Random random = new Random(seed);
        int actions = 0;
        for (int round = 0; round < 5; round++) {
            List<Integer> pairs = new ArrayList<>(Arrays.asList(0,1,2,3,4,0,1,2,3,5));
            Collections.shuffle(pairs, random);
            for (int pair : pairs) {
                Account personal = game.getPersonalAccount();
                long quantity = 100L * (1 + random.nextInt(2));
                String id = "QA-P04-" + seed + "-" + round + "-" + pair + "-" + actions;
                if (pair == 0 || pair == 1) {
                    Order order = new Order(id, id, personal.getId(), "IDX:BBCA", OrderSide.SELL,
                            pair == 0 ? OrderType.LIMIT : OrderType.TAKE_PROFIT,
                            pair == 0 ? Money.of(50000, Currency.IDR) : null,
                            pair == 1 ? Money.of(50000, Currency.IDR) : null,
                            quantity, game.getClock().getGameTimestamp(), null);
                    assertTrue(game.submitOrder(order).isSuccess);
                    assertEquals(quantity, game.getCommittedShares(personal.getId(), "IDX:BBCA"));
                    invariant(game, baseline, ++actions);
                    assertTrue(game.cancelOrder(order));
                    assertEquals(0, game.getCommittedShares(personal.getId(), "IDX:BBCA"));
                    invariant(game, baseline, ++actions);
                } else if (pair == 2) {
                    Account maker = game.getAccount("ACC-NPC-MM-01");
                    BigDecimal ask = game.getCatalog().getById("IDX:BBCA").getCurrentPrice().getAmount();
                    Order makerSell = Order.createLimit(id + "-MM-S", "IDX:BBCA", maker.getId(), OrderSide.SELL, new Money(ask, Currency.IDR), quantity, game.getClock().getGameTimestamp());
                    assertTrue(game.submitOrder(makerSell).isSuccess);
                    long held = personal.getPosition("IDX:BBCA").getQuantity();
                    Order buy = Order.createLimit(id + "-B", "IDX:BBCA", personal.getId(), OrderSide.BUY, new Money(ask, Currency.IDR), quantity, game.getClock().getGameTimestamp());
                    assertTrue(game.submitOrder(buy).isSuccess);
                    assertEquals(OrderStatus.FILLED, buy.getStatus());
                    assertEquals(held + quantity, personal.getPosition("IDX:BBCA").getQuantity());
                    invariant(game, baseline, ++actions);
                    BigDecimal bid = game.getCatalog().getById("IDX:BBCA").getCurrentPrice().getAmount();
                    Order makerBuy = Order.createLimit(id + "-MM-B", "IDX:BBCA", maker.getId(), OrderSide.BUY, new Money(bid, Currency.IDR), quantity, game.getClock().getGameTimestamp());
                    assertTrue(game.submitOrder(makerBuy).isSuccess);
                    Order sell = Order.createLimit(id + "-S", "IDX:BBCA", personal.getId(), OrderSide.SELL, new Money(bid, Currency.IDR), quantity, game.getClock().getGameTimestamp());
                    assertTrue(game.submitOrder(sell).isSuccess);
                    assertEquals(OrderStatus.FILLED, sell.getStatus());
                    assertEquals(held, personal.getPosition("IDX:BBCA").getQuantity());
                    invariant(game, baseline, ++actions);
                } else if (pair == 3) {
                    game.openShortPosition("BBCA", quantity);
                    invariant(game, baseline, ++actions);
                    game.closeShortPosition("BBCA", quantity);
                    invariant(game, baseline, ++actions);
                } else if (pair == 5) {
                    org.json.JSONObject corporate = game.getCorporateService().exportState();
                    String date = game.getClock().getCurrentDate().toString();
                    corporate.getJSONArray("corporateActions").put(new JSONObject().put("id", id + "-SPLIT").put("ticker", "BBCA")
                            .put("type", "STOCK_SPLIT").put("valueOrRatio", "2").put("currency", "IDR")
                            .put("recordingDate", date).put("executionDate", date).put("isExecuted", false));
                    corporate.getJSONArray("corporateActions").put(new JSONObject().put("id", id + "-REVERSE").put("ticker", "BBCA")
                            .put("type", "REVERSE_SPLIT").put("valueOrRatio", "0.5").put("currency", "IDR")
                            .put("recordingDate", date).put("executionDate", date).put("isExecuted", false));
                    game.getCorporateService().restoreState(corporate);
                    long supply = game.getCatalog().getById("IDX:BBCA").getSharesOutstanding();
                    com.tradingglobal3d.core.corporate.CorporateAction split = game.getCorporateService().getCorporateActions().stream().filter(action -> action.getId().equals(id + "-SPLIT")).findFirst().get();
                    game.executeStockSplit(split);
                    assertEquals(supply * 2, game.getCatalog().getById("IDX:BBCA").getSharesOutstanding());
                    invariant(game, baseline * 2, ++actions);
                    com.tradingglobal3d.core.corporate.CorporateAction reverse = game.getCorporateService().getCorporateActions().stream().filter(action -> action.getId().equals(id + "-REVERSE")).findFirst().get();
                    game.executeStockSplit(reverse);
                    assertEquals(supply, game.getCatalog().getById("IDX:BBCA").getSharesOutstanding());
                    invariant(game, baseline, ++actions);
                } else {
                    game.save();
                    invariant(game, baseline, ++actions);
                    JSONObject before = game.getIssuerOwnership("IDX:BBCA");
                    game = loadGame(dir, true);
                    assertEquals(before.toString(), game.getIssuerOwnership("IDX:BBCA").toString());
                    invariant(game, baseline, ++actions);
                }
            }
        }
        assertEquals(100, actions);
        System.out.println("PLAN04 seed=" + seed + " meaningful_actions=" + actions + " invariants=PASS");
    }
}
