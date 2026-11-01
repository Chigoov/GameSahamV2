"""Production UI and Java bridge check; requires WireQaServer with an isolated QA save."""
import json
from pathlib import Path
from playwright.sync_api import sync_playwright

root = Path(__file__).resolve().parents[2]
out = root / "Artifacts/Codex-Audit-Ulang-2026-11-01/ui-clock-production"
out.mkdir(parents=True, exist_ok=True)
results = []
with sync_playwright() as p:
    browser = p.chromium.launch(channel="chrome", headless=True)
    for width in (390, 320):
        page = browser.new_page(viewport={"width": width, "height": 844})
        errors = []
        page.on("pageerror", lambda error: errors.append(str(error)))
        page.goto("http://127.0.0.1:8798/?no_welcome=1", wait_until="domcontentloaded")
        page.wait_for_function("document.querySelector('#clock-display').textContent.includes('WIB') && !document.querySelector('#clock-display').hasAttribute('aria-busy')")
        before = page.evaluate("JSON.parse(TradingNativeBridge.getSnapshot()).clock.timestamp")
        page.evaluate("""() => {
            window.qaStates=[];window.qaTimes=[];
            const original=TradingNativeBridge.getClockSyncStatus;
            TradingNativeBridge.getClockSyncStatus=(...args)=>{const raw=original(...args);qaStates.push(JSON.parse(raw));return raw;};
            new MutationObserver(()=>qaTimes.push(document.querySelector('#clock-display').textContent))
                .observe(document.querySelector('#clock-display'),{childList:true,subtree:true});
        }""")
        response = page.request.post("http://127.0.0.1:8798/qa-action", data={"action": "offsetRealClock", "elapsedMs": 7200000})
        assert response.ok, response.text()
        page.evaluate("document.dispatchEvent(new Event('visibilitychange'))")
        page.locator('.nav-item[data-tab="pasar_porto"]').click()
        page.locator('.nav-item[data-tab="beranda"]').click()
        page.wait_for_function("qaStates.some(s=>s.status==='COMPLETED')", timeout=60000)
        states = page.evaluate("qaStates")
        final = next(s for s in reversed(states) if s["status"] == "COMPLETED")
        assert final["success"]
        assert final["snapshot"]["clock"]["timestamp"] - before >= 4 * 86400000
        assert page.locator('#clock-display').get_attribute('aria-busy') is None
        assert page.locator('#clock-sync-status').count() == 0
        assert not errors, errors
        page.screenshot(path=str(out / f"java-host-{width}.png"))
        results.append({"width": width, "states": [s["status"] for s in states],
                        "clockChanges": page.evaluate("qaTimes"), "errors": errors,
                        "elapsedGameMs": final["snapshot"]["clock"]["timestamp"] - before})
        page.close()
    # Hold only the QA acknowledgement, so navigation is exercised before the UI unlocks.
    # The real Java worker still processes and saves the elapsed world time.
    for width, height in ((320,568),(360,640),(390,844),(412,915),(430,932),(768,1024),(1440,900)):
        for text_scale in (100, 200):
            page = browser.new_page(viewport={"width":width,"height":height}, reduced_motion="reduce")
            errors = []
            page.on("pageerror", lambda error: errors.append(str(error)))
            page.goto("http://127.0.0.1:8798/?no_welcome=1", wait_until="domcontentloaded")
            page.wait_for_function("!document.querySelector('#clock-display').hasAttribute('aria-busy')")
            if text_scale == 200:
                page.add_style_tag(content="html {font-size:200% !important}")
            page.evaluate("""() => {
                window.qaHold=true;
                const original=TradingNativeBridge.getClockSyncStatus;
                TradingNativeBridge.getClockSyncStatus=(...args)=>{
                    const s=JSON.parse(original(...args));
                    return qaHold&&s.status==='COMPLETED' ? JSON.stringify({status:'PENDING',
                        progress:{currentGameTimestamp:s.snapshot.clock.timestamp}}) : JSON.stringify(s);
                };
            }""")
            response=page.request.post("http://127.0.0.1:8798/qa-action",data={"action":"offsetRealClock","elapsedMs":7200000})
            assert response.ok, response.text()
            page.evaluate("document.dispatchEvent(new Event('visibilitychange'))")
            for tab in ("pasar_porto","firma","keuangan","riset","beranda"):
                page.locator(f'.nav-item[data-tab="{tab}"]').click()
                assert page.locator('#clock-display').get_attribute('aria-busy') == 'true'
            clock=page.locator('#clock-display').bounding_box()
            assert clock and clock['x']>=0 and clock['x']+clock['width']<=width, (width,text_scale,clock)
            assert not errors, errors
            # Capture unrelated header clipping as evidence, rather than equating scrollWidth with readability.
            header_clipped=page.locator('#header-net-worth').evaluate("""el => {
                const range=document.createRange();range.selectNodeContents(el);
                const r=range.getBoundingClientRect();return r.left<0||r.right>innerWidth;
            }""")
            page.screenshot(path=str(out/f"pending-{width}-text{text_scale}.png"))
            page.evaluate("qaHold=false")
            page.wait_for_function("!document.querySelector('#clock-display').hasAttribute('aria-busy')")
            results.append({"width":width,"height":height,"textScalePercent":text_scale,
                            "heldAcknowledgement":True,"allFiveTabsDuringPending":True,
                            "clockWithinViewport":True,"existingHeaderClipped":header_clipped,"errors":errors})
            page.close()
    browser.close()
(out / "java-host-check.json").write_text(json.dumps(results, indent=2), encoding="utf-8")
print("PASS: production UI + Java host; four game days caught up; existing clock; navigation; no added wait panel; 390/320px. Not Android/device evidence.")
