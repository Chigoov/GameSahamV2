package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.mobile.TradingWebBridge;
import org.json.JSONObject;
import org.junit.Test;
import java.nio.file.*;
import java.time.*;
import static org.junit.Assert.*;

public class ClockAsyncReconciliationTest {
 @Test public void workerAcknowledgesBeforeCatchupAndDuplicateStartNeverRepeatsElapsed() throws Exception {
  TradingFacade game=new TradingFacade(SaveManager.forDirectory(Files.createTempDirectory(Path.of(System.getProperty("java.io.tmpdir")),"async-qa-").toFile()));
  game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(Path.of("src/main/assets/data/market_universe.json"))));
  long start=1792756800000L;game.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(start),ZoneOffset.UTC));game.startOrResume();
  long initial=game.getGameTimestamp();game.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(start+86400000L),ZoneOffset.UTC));
  TradingWebBridge bridge=new TradingWebBridge(game);
  var begin=TradingWebBridge.class.getMethod("beginClockSync",boolean.class);
  var status=TradingWebBridge.class.getMethod("getClockSyncStatus");
  long timer=System.nanoTime();JSONObject ack=new JSONObject((String)begin.invoke(bridge,true));
  assertTrue((System.nanoTime()-timer)/1000000<=300);assertEquals("PENDING",ack.getString("status"));
  begin.invoke(bridge,true);
  JSONObject result;long limit=System.nanoTime()+60_000_000_000L;
  do { result=new JSONObject((String)status.invoke(bridge)); if(!"PENDING".equals(result.getString("status")))break;Thread.sleep(10); }while(System.nanoTime()<limit);
  assertEquals(result.toString(),"COMPLETED",result.getString("status"));
  assertEquals(initial+86400000L*24,game.getGameTimestamp());assertTrue(game.reconcileAll());
  assertEquals(initial+86400000L*24, new JSONObject(bridge.getSnapshot()).getJSONObject("clock").getLong("timestamp"));
 }
 @Test public void failedSaveAndLifecycleDuringWorkerCanRetryWithoutRepeatingTime() throws Exception {
  Path dir=Files.createTempDirectory(Path.of(System.getProperty("java.io.tmpdir")),"async-fault-");
  TradingFacade game=new TradingFacade(SaveManager.forDirectory(dir.toFile()));
  game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(Path.of("src/main/assets/data/market_universe.json"))));
  long start=1792756800000L;game.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(start),ZoneOffset.UTC));game.startOrResume();
  byte[] durable=Files.readAllBytes(game.getSaveManager().getSaveFile().toPath());
  Path blocked=game.getSaveManager().getTempFile().toPath();Files.createDirectory(blocked);Files.write(blocked.resolve("block"),new byte[]{1});
  long initial=game.getGameTimestamp();game.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(start+7200000L),ZoneOffset.UTC));
  TradingWebBridge bridge=new TradingWebBridge(game);bridge.beginClockSync(true);bridge.beginClockSync(false);
  JSONObject failed=await(bridge);assertEquals("FAILED",failed.getString("status"));assertFalse(failed.getBoolean("success"));
  assertArrayEquals(durable,Files.readAllBytes(game.getSaveManager().getSaveFile().toPath()));
  Files.delete(blocked.resolve("block"));Files.delete(blocked);
  bridge.beginClockSync(true);assertEquals("COMPLETED",await(bridge).getString("status"));
  assertTrue(game.getClock().isForeground());assertEquals(initial+7200000L*24,game.getGameTimestamp());assertTrue(game.reconcileAll());
 }
 @Test public void rapidLifecycleChangesSettleToLastRequestedState() throws Exception {
  TradingFacade game=new TradingFacade(SaveManager.forDirectory(Files.createTempDirectory(Path.of(System.getProperty("java.io.tmpdir")),"async-race-").toFile()));
  game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(Path.of("src/main/assets/data/market_universe.json"))));
  long start=1792756800000L;game.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(start),ZoneOffset.UTC));game.startOrResume();
  TradingWebBridge bridge=new TradingWebBridge(game);long initial=game.getGameTimestamp();
  for(int i=0;i<100;i++) {
   game.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(start+(i+1)*1000L),ZoneOffset.UTC));
   for(int j=0;j<20;j++) bridge.beginClockSync(j%2==0);
   bridge.beginClockSync(false);assertEquals("COMPLETED",await(bridge).getString("status"));
   assertFalse("latest lifecycle at iteration"+i,game.getClock().isForeground());
  }
  assertEquals(initial+2400000L,game.getGameTimestamp());assertTrue(game.reconcileAll());
 }
 private JSONObject await(TradingWebBridge bridge) throws Exception {
  JSONObject result;long limit=System.nanoTime()+60_000_000_000L;
  do {result=new JSONObject(bridge.getClockSyncStatus());if(!"PENDING".equals(result.getString("status")))return result;Thread.sleep(10);}while(System.nanoTime()<limit);
  throw new AssertionError("worker timeout");
 }
}
