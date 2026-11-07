"""Exercise the production UI and Java worker with a new browser-only save."""
import argparse
import hashlib
import json
import os
import socket
import subprocess
import time
from pathlib import Path
from urllib.request import urlopen

from playwright.sync_api import sync_playwright


def digest(path):
    with path.open("rb") as file:
        return hashlib.file_digest(file, "sha256").hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("compiled", type=Path)
    args = parser.parse_args()
    repo = Path(__file__).resolve().parents[2]
    compiled = args.compiled.resolve()
    if not compiled.is_relative_to(repo / "Artifacts"):
        parser.error("Compiled classes must be under this checkout's Artifacts")
    out = compiled / f"browser-resume-{time.time_ns()}"
    out.mkdir()
    assets = repo / "android/app/src/main/assets"
    java = Path("C:/Program Files/Android/Android Studio/jbr/bin")
    json_jar, = (Path.home() / ".gradle/caches/modules-2/files-2.1/org.json/json/20240303").rglob("*.jar")
    android_jar = Path(os.environ["LOCALAPPDATA"]) / "Android/Sdk/platforms/android-36/android.jar"
    cp = os.pathsep.join(map(str, [out, compiled / "classes", json_jar, android_jar]))
    wire = repo / "Work/WireQaServer.java"
    subprocess.run([str(java / "javac.exe"), "-cp", cp, "-d", str(out), str(wire)], check=True, timeout=60)
    save = out / "qa-save"
    with socket.socket() as sock:
        sock.bind(("127.0.0.1", 0))
        port = sock.getsockname()[1]
    url = f"http://127.0.0.1:{port}"
    socket_dir = repo / ".socket-qa"
    socket_dir.mkdir(exist_ok=True)
    command = [str(java / "java.exe"), "-Xmx384m", f"-Djdk.net.unixdomain.tmpdir={socket_dir}",
               "-Dqa.fixedEpochMs=1792375200000", "-cp", cp, "com.tradingglobal3d.mobile.WireQaServer",
               str(assets), str(save), str(port)]
    inputs = [wire, Path(__file__), compiled / "source-hashes.json", json_jar, android_jar]
    inputs += list(assets.rglob("*"))
    (out / "inputs.json").write_text(json.dumps({str(p): digest(p) for p in inputs if p.is_file()}, indent=2))
    (out / "command.json").write_text(json.dumps(command, indent=2))
    with (out / "server.log").open("w") as log:
        server = subprocess.Popen(command, cwd=repo, stdout=log, stderr=log,
                                  creationflags=subprocess.CREATE_NO_WINDOW)
        try:
            for _ in range(200):
                if server.poll() is not None:
                    raise RuntimeError("QA server exited")
                try:
                    with urlopen(f"{url}/qa-meta", timeout=1):
                        break
                except OSError:
                    time.sleep(0.05)
            else:
                raise RuntimeError("QA server did not start")
            with sync_playwright() as pw:
                browser = pw.chromium.launch(headless=True)
                try:
                    run(browser, url, save, out)
                finally:
                    browser.close()
        finally:
            server.terminate()
            server.wait(timeout=15)


def run(browser, url, save, out):
    page = browser.new_page(viewport={"width": 390, "height": 844})
    page.route("**/*", lambda route: route.continue_() if route.request.url.startswith(url + "/") else route.abort())

    def expose_snapshot_test(route):
        response = route.fetch()
        source = response.text()
        end = source.rindex("})();")
        hook = "window.__suTest={refreshSnapshot,current:()=>latestSnapshot,ready:()=>!clockSyncRunning&&!clockSyncBlocked,submit:order=>bridge.submitOrder(order)};"
        route.fulfill(response=response, body=source[:end] + hook + source[end:])

    page.route("**/app.js*", expose_snapshot_test)
    errors = []
    page.on("pageerror", lambda error: errors.append(str(error)))
    page.goto(url, wait_until="networkidle")

    def status(expected):
        page.wait_for_function("""expected => JSON.parse(TradingNativeBridge.getClockSyncStatus()).status === expected""",
                               arg=expected, timeout=90_000)
        return page.evaluate("JSON.parse(TradingNativeBridge.getClockSyncStatus())")

    def hide():
        page.evaluate("TradingNativeBridge.beginClockSync(false)")
        status("COMPLETED")

    def advance(elapsed):
        result = page.request.post(f"{url}/qa-action", data={"action": "offsetRealClock", "elapsedMs": elapsed})
        assert result.ok and result.json()["success"]

    initial = status("COMPLETED")["snapshot"]["clock"]["timestamp"]
    page.locator(".nav-item[data-tab='pasar_porto']").click()
    page.locator(".nav-item[data-tab='beranda']").click()
    hide()
    advance(7 * 86_400_000)
    started = time.perf_counter()
    page.evaluate("document.dispatchEvent(new Event('visibilitychange'))")
    page.locator("#clock-display[aria-busy='true']").wait_for(timeout=3000)
    shell_ms = round((time.perf_counter() - started) * 1000, 1)
    interaction = time.perf_counter()
    page.locator(".nav-item[data-tab='pasar_porto']").click(timeout=3000)
    assert page.locator(".nav-item[data-tab='pasar_porto']").get_attribute("aria-current") == "page"
    interaction_ms = round((time.perf_counter() - interaction) * 1000, 1)
    pending_after_click = page.locator("#clock-display").get_attribute("aria-busy") == "true"
    assert pending_after_click
    blocked = page.evaluate("""() => {
        try { __suTest.submit('{}'); } catch (error) { return error.message; }
        return null;
    }""")
    assert blocked and "Transaksi menunggu" in blocked
    synced = status("COMPLETED")["snapshot"]["clock"]["timestamp"]
    assert synced == initial + 7 * 86_400_000 * 48
    page.locator("#clock-display[aria-busy='true']").wait_for(state="detached", timeout=3000)
    sync_ms = round((time.perf_counter() - started) * 1000, 1)
    hide()
    advance(7_200_000)
    primary = save / "save_game_v2.json"
    backup = save / "save_game_v2.json.bak"
    held = save / "qa-held.bak"
    backup.replace(held)
    backup.mkdir()
    blocker = backup / "block"
    blocker.touch()
    before = digest(primary)
    try:
        page.evaluate("document.dispatchEvent(new Event('visibilitychange'))")
        failed = status("FAILED")
        assert not failed["success"] and digest(primary) == before
    finally:
        blocker.unlink()
        backup.rmdir()
        held.replace(backup)
    retry_started = time.perf_counter()
    page.evaluate("document.dispatchEvent(new Event('visibilitychange'))")
    retried = status("COMPLETED")["snapshot"]["clock"]["timestamp"]
    assert retried == synced + 7_200_000 * 48
    page.wait_for_function("timestamp => __suTest.current()?.clock?.timestamp === timestamp", arg=retried)
    page.wait_for_function("__suTest.ready()")
    verified_order = page.evaluate("""staleTimestamp => {
        const before = __suTest.current();
        const order = JSON.parse(__suTest.submit(JSON.stringify({accountId:before.personalAccount.id,
            instrumentId:'IDX:BBCA',side:'BUY',type:'LIMIT',quantity:100,limitPrice:'100',requestId:'SU-BROWSER-RETRY'})));
        if (!order.success || order.status !== 'OPEN') throw Error(JSON.stringify(order));
        const after = JSON.parse(TradingNativeBridge.getSnapshot());
        if (after.personalAccount.cash.IDR.reserved === before.personalAccount.cash.IDR.reserved)
            throw Error('Reservation did not change');
        __suTest.refreshSnapshot(after);
        const stale = structuredClone(before);
        stale.clock.timestamp = staleTimestamp;
        __suTest.refreshSnapshot(stale);
        const retained = __suTest.current();
        if (retained.clock.timestamp !== after.clock.timestamp
                || retained.personalAccount.cash.IDR.reserved !== after.personalAccount.cash.IDR.reserved)
            throw Error('Stale snapshot replaced committed values');
        return {status:order.status,before:before.personalAccount.cash.IDR.reserved,after:after.personalAccount.cash.IDR.reserved};
    }""", synced)
    safe_ms = round((time.perf_counter() - retry_started) * 1000, 1)
    assert not errors, errors
    (out / "result.json").write_text(json.dumps({
        "initialGameTimestamp": initial, "syncedGameTimestamp": synced, "retryGameTimestamp": retried,
        "shellPendingMs": shell_ms, "navigationDuringPendingMs": interaction_ms, "syncMs": sync_ms,
        "pendingAfterNavigation": pending_after_click, "writeFailure": failed, "preservedPrimaryOnFailure": True,
        "pendingOrderRejected": True, "retryToAcceptedOrderMs": safe_ms, "acceptedOrder": verified_order,
        "staleSnapshotRejected": True, "pageErrors": errors,
    }, indent=2))
    print(out / "result.json")


if __name__ == "__main__":
    main()
