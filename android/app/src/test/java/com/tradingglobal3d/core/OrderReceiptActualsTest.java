package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.trading.*;
import com.tradingglobal3d.mobile.TradingWebBridge;
import org.json.JSONObject;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import static org.junit.Assert.*;

public class OrderReceiptActualsTest {
 @Rule public TemporaryFolder temp=new TemporaryFolder();
 @Test public void matchedReceiptUsesWeightedFillsAndActualFeesAfterReload() throws Exception {
  SaveManager saves=SaveManager.forDirectory(temp.newFolder());
  TradingFacade g=new TradingFacade(saves);
  g.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")),java.nio.charset.StandardCharsets.UTF_8)));
  g.initializeNewGame(GameMode.INTERNAL_MARKET,new BigDecimal("50000000"));
  g.getClock().advanceGameSeconds(1800);
  Account buyer=g.getPersonalAccount(), seller=g.getAccount("ACC-NPC-MM-01");
  String instrument="IDX:BBCA";
  g.submitOrder(Order.createLimit("MAKER-A",instrument,seller.getId(),OrderSide.SELL,Money.of(9000,Currency.IDR),100,g.getGameTimestamp()));
  g.submitOrder(Order.createLimit("MAKER-B",instrument,seller.getId(),OrderSide.SELL,Money.of(9100,Currency.IDR),200,g.getGameTimestamp()));
  Order order=new Order("RECEIPT-ORDER","RECEIPT-REQUEST",buyer.getId(),instrument,OrderSide.BUY,OrderType.LIMIT,Money.of(9200,Currency.IDR),null,300,g.getGameTimestamp(),null);
  assertTrue(g.submitOrder(order).isSuccess);
  JSONObject receipt=new JSONObject(new TradingWebBridge(g).getOrderStatus("RECEIPT-REQUEST"));
  assertEquals("FILLED",receipt.getString("status"));
  assertEquals(0,new BigDecimal("2720000").compareTo(new BigDecimal(receipt.getString("filledValueRaw"))));
  assertEquals(0,new BigDecimal("4080").compareTo(new BigDecimal(receipt.getString("actualFeeRaw"))));
  assertEquals(0,new BigDecimal("2724080").compareTo(new BigDecimal(receipt.getString("cashImpactRaw"))));
  assertEquals(9066.666667,receipt.getDouble("averageFillPriceRaw"),.000001);
  java.util.List<TradeFill> fills=g.getMarketEngine().getBook(instrument).getTradeHistory();
  assertEquals(2,fills.size());
  assertEquals(0,new BigDecimal("1350").compareTo(fills.get(0).getBuyerFee().getAmount()));
  assertEquals(0,new BigDecimal("2250").compareTo(fills.get(0).getSellerFee().getAmount()));
  assertEquals(0,new BigDecimal("2730").compareTo(fills.get(1).getBuyerFee().getAmount()));
  assertEquals(0,new BigDecimal("4550").compareTo(fills.get(1).getSellerFee().getAmount()));
  assertTrue(g.reconcileAll());g.save();assertTrue(g.load());
  JSONObject restored=new JSONObject(new TradingWebBridge(g).getOrderStatus("RECEIPT-REQUEST"));
  assertEquals(receipt.getString("actualFeeRaw"),restored.getString("actualFeeRaw"));
  assertEquals(receipt.getString("cashImpactRaw"),restored.getString("cashImpactRaw"));
 }

 @Test public void partialFillReceiptSurvivesCancellationAndReload() throws Exception {
  SaveManager saves=SaveManager.forDirectory(temp.newFolder());
  TradingFacade g=new TradingFacade(saves);
  g.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")),java.nio.charset.StandardCharsets.UTF_8)));
  g.initializeNewGame(GameMode.INTERNAL_MARKET,new BigDecimal("50000000"));
  g.getClock().advanceGameSeconds(1800);
  Account buyer=g.getPersonalAccount(), seller=g.getAccount("ACC-NPC-MM-01");
  String instrument="IDX:BBCA";
  g.submitOrder(Order.createLimit("PARTIAL-MAKER",instrument,seller.getId(),OrderSide.SELL,Money.of(9000,Currency.IDR),100,g.getGameTimestamp()));
  Money cashBeforeOrder=buyer.getTotalCash(Currency.IDR);
  Order order=new Order("PARTIAL-ORDER","PARTIAL-REQUEST",buyer.getId(),instrument,OrderSide.BUY,OrderType.LIMIT,Money.of(9200,Currency.IDR),null,300,g.getGameTimestamp(),null);
  assertTrue(g.submitOrder(order).isSuccess);
  TradingWebBridge bridge=new TradingWebBridge(g);
  JSONObject beforeCancel=new JSONObject(bridge.getOrderStatus("PARTIAL-REQUEST"));
  assertEquals("PARTIALLY_FILLED",beforeCancel.getString("status"));
  assertEquals(100,beforeCancel.getLong("filledQuantity"));
  assertEquals(200,beforeCancel.getLong("remainingQuantity"));
  assertEquals(900000,beforeCancel.getLong("filledValueRaw"));
  assertEquals(1350,beforeCancel.getLong("actualFeeRaw"));
  assertEquals(901350,beforeCancel.getLong("cashImpactRaw"));
  Money reservedBeforeCancel=buyer.getReservedCash(Currency.IDR);
  JSONObject cancelled=new JSONObject(bridge.cancelOrder("PARTIAL-REQUEST"));
  assertEquals("CANCELLED",cancelled.getString("status"));
  assertEquals(100,cancelled.getLong("filledQuantity"));
  assertEquals(200,cancelled.getLong("remainingQuantity"));
  assertEquals(200,cancelled.getLong("cancelledQuantity"));
  assertEquals("900000",cancelled.getString("filledValueRaw"));
  assertEquals("1350",cancelled.getString("actualFeeRaw"));
  assertEquals("901350",cancelled.getString("cashImpactRaw"));
  assertTrue(buyer.getReservedCash(Currency.IDR).isLessThan(reservedBeforeCancel));
  assertEquals(Money.zero(Currency.IDR),buyer.getReservedCash(Currency.IDR));
  assertEquals(cashBeforeOrder.subtract(Money.of(901350,Currency.IDR)),buyer.getTotalCash(Currency.IDR));
  JSONObject duplicateCancel=new JSONObject(bridge.cancelOrder("PARTIAL-REQUEST"));
  assertEquals("CANCELLED",duplicateCancel.getString("status"));
  assertEquals(buyer.getTotalCash(Currency.IDR),cashBeforeOrder.subtract(Money.of(901350,Currency.IDR)));
  assertEquals(Money.zero(Currency.IDR),buyer.getReservedCash(Currency.IDR));
  assertTrue(g.reconcileAll());g.save();assertTrue(g.load());
  JSONObject restored=new JSONObject(bridge.getOrderStatus("PARTIAL-REQUEST"));
  assertEquals("CANCELLED",restored.getString("status"));
  assertEquals(cancelled.getString("filledValueRaw"),restored.getString("filledValueRaw"));
  assertEquals(cancelled.getString("actualFeeRaw"),restored.getString("actualFeeRaw"));
  assertEquals(cancelled.getString("cashImpactRaw"),restored.getString("cashImpactRaw"));
  assertTrue(g.reconcileAll());
 }

 @Test public void legacyFillWithoutFeeFieldsNeverInventsCurrentFee() throws Exception {
  SaveManager saves=SaveManager.forDirectory(temp.newFolder());
  TradingFacade g=new TradingFacade(saves);
  g.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")),java.nio.charset.StandardCharsets.UTF_8)));
  g.initializeNewGame(GameMode.INTERNAL_MARKET,new BigDecimal("50000000"));
  g.getClock().advanceGameSeconds(1800);
  Account buyer=g.getPersonalAccount(), seller=g.getAccount("ACC-NPC-MM-01");
  String instrument="IDX:BBCA";
  g.submitOrder(Order.createLimit("LEGACY-MAKER",instrument,seller.getId(),OrderSide.SELL,Money.of(9000,Currency.IDR),100,g.getGameTimestamp()));
  Order order=new Order("LEGACY-ORDER","LEGACY-REQUEST",buyer.getId(),instrument,OrderSide.BUY,OrderType.LIMIT,Money.of(9200,Currency.IDR),null,100,g.getGameTimestamp(),null);
  assertTrue(g.submitOrder(order).isSuccess);
  JSONObject state=g.getMarketEngine().exportState().getJSONObject(0);
  for (int i=0;i<state.getJSONArray("fills").length();i++) {
   JSONObject fill=state.getJSONArray("fills").getJSONObject(i);
   fill.remove("buyerFee"); fill.remove("sellerFee");
  }
  g.getMarketEngine().restoreState(new org.json.JSONArray().put(state),g.getCatalog(),g.getOrderManager());
  JSONObject receipt=new JSONObject(new TradingWebBridge(g).getOrderStatus("LEGACY-REQUEST"));
  assertEquals("FILLED",receipt.getString("status"));
  assertEquals("900000",receipt.getString("filledValueRaw"));
  assertFalse(receipt.getBoolean("feeDataAvailable"));
  assertTrue(receipt.isNull("actualFeeRaw"));
  assertTrue(receipt.isNull("cashImpactRaw"));
 }

 @Test public void openOrderHasNoSyntheticFillAmounts() throws Exception {
  SaveManager saves=SaveManager.forDirectory(temp.newFolder());
  TradingFacade g=new TradingFacade(saves);
  g.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")),java.nio.charset.StandardCharsets.UTF_8)));
  g.initializeNewGame(GameMode.INTERNAL_MARKET,new BigDecimal("50000000"));
  g.getClock().advanceGameSeconds(1800);
  JSONObject submitted=new JSONObject(new TradingWebBridge(g).submitOrder(new JSONObject()
          .put("accountId",g.getPersonalAccount().getId()).put("instrumentId","IDX:BBCA")
          .put("side","BUY").put("type","LIMIT").put("quantity",100)
          .put("limitPrice","8000").put("requestId","OPEN-RECEIPT").toString()));
  assertTrue(submitted.getBoolean("success"));
  JSONObject receipt=new JSONObject(new TradingWebBridge(g).getOrderStatus("OPEN-RECEIPT"));
  assertEquals("OPEN",receipt.getString("status"));
  assertEquals(0,receipt.getLong("filledQuantity"));
  assertFalse(receipt.getBoolean("feeDataAvailable"));
  assertTrue(receipt.isNull("filledValueRaw"));
  assertTrue(receipt.isNull("averageFillPriceRaw"));
  assertTrue(receipt.isNull("actualFeeRaw"));
  assertTrue(receipt.isNull("cashImpactRaw"));
 }

 @Test public void unknownOrderDoesNotReturnSuccessfulReceiptOrAmounts() throws Exception {
  TradingFacade g=new TradingFacade(SaveManager.forDirectory(temp.newFolder()));
  JSONObject result=new JSONObject(new TradingWebBridge(g).getOrderStatus("UNKNOWN-REQUEST"));
  assertFalse(result.getBoolean("success"));
  assertFalse(result.has("status"));
  assertFalse(result.has("filledValueRaw"));
  assertFalse(result.has("actualFeeRaw"));
  assertFalse(result.has("cashImpactRaw"));
 }

 @Test public void rejectedOrderReceiptHasNoFillAmounts() throws Exception {
  TradingFacade g=new TradingFacade(SaveManager.forDirectory(temp.newFolder()));
  g.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")),java.nio.charset.StandardCharsets.UTF_8)));
  g.initializeNewGame(GameMode.INTERNAL_MARKET,new BigDecimal("1000000"));
  g.getClock().advanceGameSeconds(1800);
  JSONObject submitted=new JSONObject(new TradingWebBridge(g).submitOrder(new JSONObject()
          .put("accountId",g.getPersonalAccount().getId()).put("instrumentId","IDX:BBCA")
          .put("side","BUY").put("type","LIMIT").put("quantity",100)
          .put("limitPrice","20000").put("requestId","REJECT-RECEIPT").toString()));
  assertEquals("REJECTED",submitted.getString("status"));
  JSONObject receipt=new JSONObject(new TradingWebBridge(g).getOrderStatus("REJECT-RECEIPT"));
  assertEquals("REJECTED",receipt.getString("status"));
  assertEquals(0,receipt.getLong("filledQuantity"));
  assertTrue(receipt.isNull("filledValueRaw"));
  assertTrue(receipt.isNull("actualFeeRaw"));
  assertTrue(receipt.isNull("cashImpactRaw"));
 }

 @Test public void expiredTerminalOrderReceiptHasNoFillAmounts() throws Exception {
  TradingFacade g=new TradingFacade(SaveManager.forDirectory(temp.newFolder()));
  g.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")),java.nio.charset.StandardCharsets.UTF_8)));
  g.initializeNewGame(GameMode.INTERNAL_MARKET,new BigDecimal("50000000"));
  g.getClock().advanceGameSeconds(1800);
  JSONObject submitted=new JSONObject(new TradingWebBridge(g).submitOrder(new JSONObject()
          .put("accountId",g.getPersonalAccount().getId()).put("instrumentId","IDX:BBCA")
          .put("side","BUY").put("type","LIMIT").put("quantity",100)
          .put("limitPrice","8000").put("requestId","EXPIRED-RECEIPT").toString()));
  assertEquals("OPEN",submitted.getString("status"));
  g.getOrderManager().getOrderByRequest("EXPIRED-RECEIPT").setStatus(OrderStatus.EXPIRED);
  JSONObject receipt=new JSONObject(new TradingWebBridge(g).getOrderStatus("EXPIRED-RECEIPT"));
  assertEquals("EXPIRED",receipt.getString("status"));
  assertEquals(0,receipt.getLong("filledQuantity"));
  assertFalse(receipt.getBoolean("canCancel"));
  assertTrue(receipt.isNull("filledValueRaw"));
  assertTrue(receipt.isNull("actualFeeRaw"));
  assertTrue(receipt.isNull("cashImpactRaw"));
 }

 @Test public void productionExpiryLifecycleAtDayCloseReleasesCashReservation() throws Exception {
  SaveManager saves = SaveManager.forDirectory(temp.newFolder());
  TradingFacade g = new TradingFacade(saves);
  g.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")), java.nio.charset.StandardCharsets.UTF_8)));
  g.initializeNewGame(GameMode.INTERNAL_MARKET, new BigDecimal("50000000"));
  g.getClock().advanceGameSeconds(1800);
  Account buyer = g.getPersonalAccount();
  Money initialCash = buyer.getTotalCash(Currency.IDR);

  JSONObject submitted = new JSONObject(new TradingWebBridge(g).submitOrder(new JSONObject()
          .put("accountId", buyer.getId()).put("instrumentId", "IDX:BBCA")
          .put("side", "BUY").put("type", "LIMIT").put("quantity", 100)
          .put("limitPrice", "5000").put("requestId", "LIFECYCLE-EXPIRY-REQ").toString()));
  assertTrue(submitted.getBoolean("success"));
  assertEquals("OPEN", submitted.getString("status"));
  assertTrue(buyer.getReservedCash(Currency.IDR).isPositive());

  g.getClock().advanceGameSeconds(7 * 3600 + 10 * 60);
  g.closeDayAndAdvance();

  TradingWebBridge bridge = new TradingWebBridge(g);
  JSONObject receipt = new JSONObject(bridge.getOrderStatus("LIFECYCLE-EXPIRY-REQ"));
  assertEquals("EXPIRED", receipt.getString("status"));
  assertEquals(0, receipt.getLong("filledQuantity"));
  assertFalse(receipt.getBoolean("canCancel"));
  assertTrue(receipt.isNull("filledValueRaw"));
  assertTrue(receipt.isNull("actualFeeRaw"));

  assertEquals(Money.zero(Currency.IDR), buyer.getReservedCash(Currency.IDR));
  assertEquals(initialCash, buyer.getTotalCash(Currency.IDR));

  g.save();
  assertTrue(g.load());
  JSONObject restored = new JSONObject(new TradingWebBridge(g).getOrderStatus("LIFECYCLE-EXPIRY-REQ"));
  assertEquals("EXPIRED", restored.getString("status"));
  assertFalse(restored.getBoolean("canCancel"));
 }
}
