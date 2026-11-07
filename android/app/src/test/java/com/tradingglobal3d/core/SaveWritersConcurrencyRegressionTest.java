package com.tradingglobal3d.core;

import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.storage.SaveManager;
import com.tradingglobal3d.core.storage.SaveState;
import com.tradingglobal3d.mobile.TradingWebBridge;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class SaveWritersConcurrencyRegressionTest {
    @Test public void closingCurrentBridgeCanFinishItsLastSave() throws Exception {
        Path dir = Files.createTempDirectory(Path.of(System.getProperty("java.io.tmpdir")), "save-closing-bridge-");
        TradingFacade game = new TradingFacade(SaveManager.forDirectory(dir.toFile()));
        game.startInternalGame(new BigDecimal("50000000"));
        game.save();
        TradingWebBridge bridge = new TradingWebBridge(game);
        bridge.destroy();
        game.getPlayerProfileManager().setPlayerName("QA Final Save");
        game.save();
        TradingFacade resumed = new TradingFacade(SaveManager.forDirectory(dir.toFile()));
        assertTrue(resumed.startOrResume());
        assertEquals("QA Final Save", resumed.getPlayerProfileManager().getPlayerName());
    }

    @Test public void supersededBridgeCannotSaveEvenWithALaterWallTimestamp() throws Exception {
        Path dir = Files.createTempDirectory(Path.of(System.getProperty("java.io.tmpdir")), "save-superseded-bridge-");
        java.time.Clock fixed = java.time.Clock.fixed(java.time.Instant.ofEpochMilli(1793664000000L), java.time.ZoneOffset.UTC);
        TradingFacade oldGame = new TradingFacade(SaveManager.forDirectory(dir.toFile()));
        oldGame.setRealTimeClock(fixed);
        oldGame.startInternalGame(new BigDecimal("50000000"));
        oldGame.getPlayerProfileManager().setPlayerName("QA Older");
        oldGame.save();
        TradingWebBridge oldBridge = new TradingWebBridge(oldGame), currentBridge = null;
        try {
            TradingFacade currentGame = new TradingFacade(SaveManager.forDirectory(dir.toFile()));
            currentGame.setRealTimeClock(fixed);
            assertTrue(currentGame.startOrResume());
            currentBridge = new TradingWebBridge(currentGame);
            currentGame.getPlayerProfileManager().setPlayerName("QA Current");
            currentGame.save();
            byte[] committed = Files.readAllBytes(currentGame.getSaveManager().getSaveFile().toPath());
            byte[] backup = Files.readAllBytes(currentGame.getSaveManager().getBackupFile().toPath());
            Thread.sleep(3);
            boolean rejected = false;
            try { oldGame.save(); } catch (IllegalStateException obsolete) {
                assertTrue(obsolete.getMessage().contains("Pemilik save sudah digantikan"));
                rejected = true;
            }
            assertArrayEquals("Revoked owner overwrote progress when its save call arrived after the current owner",
                    committed, Files.readAllBytes(currentGame.getSaveManager().getSaveFile().toPath()));
            assertTrue("Revoked bridge owner must be rejected at the storage commit boundary", rejected);
            assertArrayEquals("Revoked owner rotated the current backup", backup,
                    Files.readAllBytes(currentGame.getSaveManager().getBackupFile().toPath()));
            try (var files = Files.list(dir)) {
                assertFalse("Rejected obsolete owner leaked temp output", files.anyMatch(path -> path.getFileName().toString().endsWith(".tmp")));
            }
        } finally { oldBridge.destroy(); if (currentBridge != null) currentBridge.destroy(); }
    }

    @Test public void twoInstancesCannotCommitOldStateAfterNewState() throws Exception {
        Path dir = Files.createTempDirectory(Path.of(System.getProperty("java.io.tmpdir")), "save-two-writers-");
        SaveManager seed = SaveManager.forDirectory(dir.toFile());
        TradingFacade game = new TradingFacade(seed);
        game.startInternalGame(new BigDecimal("50000000"));
        game.save();
        String baseline = seed.readPrimaryContent();
        long timestamp = new JSONObject(baseline).getLong("savedTimestamp");
        SaveManager oldManager = SaveManager.forDirectory(dir.toFile());
        SaveManager newManager = SaveManager.forDirectory(dir.toFile());
        SaveState oldState = oldManager.readValidatedState(new JSONObject(baseline)
                .put("savedTimestamp", timestamp + 1).toString());
        SaveState newState = newManager.readValidatedState(new JSONObject(baseline)
                .put("savedTimestamp", timestamp + 2).toString());
        CountDownLatch oldReadDisk = new CountDownLatch(1), releaseOld = new CountDownLatch(1);
        CountDownLatch newerAttempted = new CountDownLatch(1), newerDone = new CountDownLatch(1);
        AtomicReference<Throwable> oldError = new AtomicReference<>(), newError = new AtomicReference<>();
        oldManager.setStateValidator(state -> {
            if (state.getSavedTimestamp() == timestamp) {
                oldReadDisk.countDown();
                try {
                    if (!releaseOld.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("QA coordination timed out");
                } catch (InterruptedException error) { Thread.currentThread().interrupt(); throw new IllegalStateException(error); }
            }
        });
        Thread older = new Thread(() -> {
            try { oldManager.save(oldState); } catch (Throwable error) { oldError.set(error); }
        }, "qa-older-save");
        Thread newer = new Thread(() -> {
            newerAttempted.countDown();
            try { newManager.save(newState); } catch (Throwable error) { newError.set(error); }
            finally { newerDone.countDown(); }
        }, "qa-newer-save");
        older.start();
        try {
            assertTrue("Older writer must reach validation of existing disk bytes", oldReadDisk.await(3, TimeUnit.SECONDS));
            newer.start();
            assertTrue(newerAttempted.await(1, TimeUnit.SECONDS));
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            // A shared commit monitor may block the second writer; otherwise let it finish before releasing the old one.
            while (newerDone.getCount() != 0 && newer.getState() != Thread.State.BLOCKED && System.nanoTime() < deadline)
                Thread.sleep(1);
            assertTrue("Second writer neither finished nor waited for the shared commit", newerDone.getCount() == 0 || newer.getState() == Thread.State.BLOCKED);
        } finally { releaseOld.countDown(); older.join(5000); newer.join(5000); }
        assertFalse("Older thread stranded", older.isAlive());
        assertFalse("Newer thread stranded", newer.isAlive());
        assertNull("New save failed", newError.get());
        if (oldError.get() != null) assertTrue(oldError.get().toString(), oldError.get() instanceof IllegalStateException);
        assertEquals("Newer committed state was overwritten by an older independent SaveManager",
                timestamp + 2, SaveManager.forDirectory(dir.toFile()).load().getState().getSavedTimestamp());
        try {
            oldManager.save(oldState);
            fail("A rejected older snapshot must not overwrite the newer commit");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("Penolakan penulisan save"));
        }
        try (var files = Files.list(dir)) {
            assertFalse("Rejected save leaked a temporary file", files.anyMatch(path -> path.getFileName().toString().endsWith(".tmp")));
        }
    }
}
