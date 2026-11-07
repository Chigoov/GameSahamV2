package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.mobile.TradingWebBridge;
import org.json.JSONObject;
import org.junit.Test;
import java.nio.file.*;
import java.time.*;
import java.util.concurrent.atomic.AtomicInteger;
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
  assertEquals(initial+86400000L*48,game.getGameTimestamp());assertTrue(game.reconcileAll());
  assertEquals(initial+86400000L*48, new JSONObject(bridge.getSnapshot()).getJSONObject("clock").getLong("timestamp"));
 }
 @Test public void coldStartRestoresTwoDaysOfflineWithDailyNews() throws Exception {
  Path dir = Files.createTempDirectory(Path.of(System.getProperty("java.io.tmpdir")), "cold-resume-qa-");
  String universe = Files.readString(Path.of("src/main/assets/data/market_universe.json"));
  Instant start = Instant.parse("2026-10-19T02:00:00Z");
  TradingFacade original = new TradingFacade(SaveManager.forDirectory(dir.toFile()));
  original.setCatalog(InstrumentCatalog.fromJsonString(universe));
  original.setRealTimeClock(Clock.fixed(start, ZoneOffset.UTC));
  assertTrue(original.startOrResume());
  Instant returnAt = start.plus(Duration.ofDays(2));
  long expected = original.getGameTimestamp() + Duration.between(start, returnAt).toMillis() * 48;
  TradingFacade resumed = new TradingFacade(SaveManager.forDirectory(dir.toFile()));
  resumed.setCatalog(InstrumentCatalog.fromJsonString(universe));
  resumed.setDynamicDailyNewsEnabled(true);
  resumed.setRealTimeClock(Clock.fixed(returnAt, ZoneOffset.UTC));
  TradingWebBridge bridge = new TradingWebBridge(resumed);
  bridge.beginClockSync(true);
  JSONObject result = await(bridge);
  assertEquals(result.toString(), "COMPLETED", result.getString("status"));
  assertEquals(expected, resumed.getGameTimestamp());
  assertTrue(resumed.reconcileAll());

  TradingFacade reloaded = new TradingFacade(SaveManager.forDirectory(dir.toFile()));
  reloaded.setCatalog(InstrumentCatalog.fromJsonString(universe));
  reloaded.setRealTimeClock(Clock.fixed(returnAt, ZoneOffset.UTC));
  assertTrue(reloaded.startOrResume());
  assertEquals(expected, reloaded.getGameTimestamp());
 }
 @Test public void failedSaveAndLifecycleDuringWorkerCanRetryWithoutRepeatingTime() throws Exception {
  Path dir=Files.createTempDirectory(Path.of(System.getProperty("java.io.tmpdir")),"async-fault-");
  TradingFacade game=new TradingFacade(SaveManager.forDirectory(dir.toFile()));
  game.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(Path.of("src/main/assets/data/market_universe.json"))));
  long start=1792756800000L;game.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(start),ZoneOffset.UTC));game.startOrResume();
  byte[] durable=Files.readAllBytes(game.getSaveManager().getSaveFile().toPath());
  Path blocked=game.getSaveManager().getBackupFile().toPath();Files.createDirectory(blocked);Files.write(blocked.resolve("block"),new byte[]{1});
  long initial=game.getGameTimestamp();game.setRealTimeClock(Clock.fixed(Instant.ofEpochMilli(start+7200000L),ZoneOffset.UTC));
  TradingWebBridge bridge=new TradingWebBridge(game);bridge.beginClockSync(true);bridge.beginClockSync(false);
  JSONObject failed=await(bridge);assertEquals("FAILED",failed.getString("status"));assertFalse(failed.getBoolean("success"));
  assertArrayEquals(durable,Files.readAllBytes(game.getSaveManager().getSaveFile().toPath()));
  Files.delete(blocked.resolve("block"));Files.delete(blocked);
  bridge.beginClockSync(true);assertEquals("COMPLETED",await(bridge).getString("status"));
  assertTrue(game.getClock().isForeground());assertEquals(initial+7200000L*48,game.getGameTimestamp());assertTrue(game.reconcileAll());
 }
 @Test public void failedFinalCommitRetriesFromFirstCheckpointWithRemainder() throws Exception {
  Path root = Files.createTempDirectory("checkpoint-retry-");
  Path interrupted = Files.createDirectory(root.resolve("interrupted"));
  Path reference = Files.createDirectory(root.resolve("reference"));
  String universe = Files.readString(Path.of("src/main/assets/data/market_universe.json"));
  Instant start = Instant.parse("2026-10-19T02:00:00Z");
  Instant target = start.plus(Duration.ofDays(8)).plusMillis(13);
  TradingFacade seed = coldGame(interrupted, universe, start);
  assertTrue(seed.startOrResume());
  long initial = seed.getGameTimestamp();
  Files.copy(seed.getSaveManager().getSaveFile().toPath(), reference.resolve("save_game_v2.json"));

  TradingFacade expected = coldGame(reference, universe, target);
  assertTrue(expected.startOrResume());
  TradingFacade game = coldGame(interrupted, universe, target);
  TradingWebBridge bridge = new TradingWebBridge(game);
  try {
   var commits = new AtomicInteger();
   game.getSaveManager().setWriteAuthority(() -> commits.incrementAndGet() != 2);
   bridge.beginClockSync(true);
   assertEquals("FAILED", await(bridge).getString("status"));
   assertEquals(2, commits.get());
   TradingFacade durable = coldGame(interrupted, universe, target);
   assertTrue(durable.load());
   assertTrue(durable.getLastRealTimeAnchor() > start.toEpochMilli());
   assertTrue(durable.getLastRealTimeAnchor() < target.toEpochMilli());
   assertEquals(LocalTime.MIDNIGHT, durable.getClock().getCurrentTime());
   long committedGameMs = durable.getGameTimestamp() - initial + durable.getClock().getGameMsRemainder();
   assertEquals((durable.getLastRealTimeAnchor() - start.toEpochMilli()) * 48, committedGameMs);

   bridge.beginClockSync(true);
   assertEquals("COMPLETED", await(bridge).getString("status"));
   assertEquals(expected.getGameTimestamp(), game.getGameTimestamp());
   assertEquals(13 * 48, game.getClock().getGameMsRemainder());
   assertEquals(expected.getLedger().getEntryCount(), game.getLedger().getEntryCount());
   assertEquals(expected.getCareerManager().getSessionReports().size(), game.getCareerManager().getSessionReports().size());
   assertEquals(expected.getPersonalAccount().getTotalCash(com.tradingglobal3d.core.model.Currency.IDR),
           game.getPersonalAccount().getTotalCash(com.tradingglobal3d.core.model.Currency.IDR));
   assertTrue(game.reconcileAll());
  } finally {
   bridge.destroy();
  }
 }
 @Test public void lifecycleCatchupLeavesTimeAfterCapturedTargetForNextSync() throws Exception {
  Path dir = Files.createTempDirectory("checkpoint-target-");
  String universe = Files.readString(Path.of("src/main/assets/data/market_universe.json"));
  Instant start = Instant.parse("2026-10-19T02:00:00Z");
  TradingFacade game = coldGame(dir, universe, start);
  assertTrue(game.startOrResume());
  long initial = game.getGameTimestamp();
  game.onAppLifecycleChanged(false);
  long target = start.plusSeconds(2).toEpochMilli();
  game.setRealTimeClock(new Clock() {
   private boolean captured;
   @Override public ZoneId getZone() { return ZoneOffset.UTC; }
   @Override public Clock withZone(ZoneId zone) { return Clock.fixed(instant(), zone); }
   @Override public Instant instant() { return Instant.ofEpochMilli(millis()); }
   @Override public long millis() {
    if (captured) return target + 2000;
    captured = true;
    return target;
   }
  });
  game.onAppLifecycleChanged(true);
  assertEquals(target, game.getLastRealTimeAnchor());
  assertEquals(initial + 2_000 * 48, game.getGameTimestamp());
  game.syncClock();
  assertEquals(initial + 4_000 * 48, game.getGameTimestamp());
 }
 private TradingFacade coldGame(Path dir, String universe, Instant time) throws Exception {
  TradingFacade game = new TradingFacade(SaveManager.forDirectory(dir.toFile()));
  game.setCatalog(InstrumentCatalog.fromJsonString(universe));
  game.setRealTimeClock(Clock.fixed(time, ZoneOffset.UTC));
  game.setDynamicDailyNewsEnabled(true);
  return game;
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
  assertEquals(initial+4800000L,game.getGameTimestamp());assertTrue(game.reconcileAll());
 }
 private JSONObject await(TradingWebBridge bridge) throws Exception {
  JSONObject result;long limit=System.nanoTime()+60_000_000_000L;
  do {result=new JSONObject(bridge.getClockSyncStatus());if(!"PENDING".equals(result.getString("status")))return result;Thread.sleep(10);}while(System.nanoTime()<limit);
  throw new AssertionError("worker timeout");
 }
}
