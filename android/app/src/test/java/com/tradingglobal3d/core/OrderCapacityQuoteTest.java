package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.*;
import com.tradingglobal3d.core.model.Currency;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONObject;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.math.BigDecimal;
import java.nio.file.*;
import static org.junit.Assert.*;

public class OrderCapacityQuoteTest {
 @Rule public TemporaryFolder temp=new TemporaryFolder();
 @Test public void zeroCashHasZeroLotsAndCashQuoteUsesExactDomainFee() throws Exception {
  TradingFacade g=new TradingFacade(SaveManager.forDirectory(temp.newFolder()));
  g.setCatalog(InstrumentCatalog.fromJsonString(new String(Files.readAllBytes(Paths.get("src/main/assets/data/market_universe.json")),java.nio.charset.StandardCharsets.UTF_8)));
  g.initializeNewGame(GameMode.INTERNAL_MARKET,new BigDecimal("1000000"));
  String id=g.getPersonalAccount().getId();
  java.lang.reflect.Method m=TradingFacade.class.getMethod("getOrderCapacity",String.class,String.class,String.class,String.class,String.class);
  JSONObject quote=(JSONObject)m.invoke(g,id,"IDX:BBCA","BUY","LIMIT","9000");
  assertEquals(1,quote.getLong("maxLots"));assertEquals(100,quote.getLong("lotSize"));
  assertEquals("1000000",quote.getString("availableCashRaw"));
  g.withdraw("QA-ZERO",id,Money.of(1000000,Currency.IDR),"isolated zero-cash fixture");
  assertEquals(0,((JSONObject)m.invoke(g,id,"IDX:BBCA","BUY","LIMIT","9000")).getLong("maxLots"));
  assertEquals(0,((JSONObject)m.invoke(g,id,"IDX:BBCA","SELL","LIMIT","9000")).getLong("maxLots"));
  assertTrue(g.reconcileAll());
 }
}
