"""Run the production clock/bridge presenter against deterministic synthetic worker states."""
from pathlib import Path
import re
from playwright.sync_api import sync_playwright

root = Path(__file__).resolve().parents[2]
source = (root / "android/app/src/main/assets/www/app.js").read_text(encoding="utf-8")
proxy = source[source.index("    let clockSyncRunning = false;"):source.index("    // 2. DOM Elements")]
presenter = source[source.index("    function requestClockSync("):source.index("    // =========================================================================", source.index("    function renderFlowingClock()"))]
fixture = """
const clockDisplay = document.getElementById('clock-display');
let latestSnapshot={clock:{timestamp:Date.UTC(2026,10,1,2,15)},career:{days:24}};
let anchorGameTimestamp=latestSnapshot.clock.timestamp, anchorPerfNow=performance.now(), anchorRate=48;
let clockSyncPromise=null, clockChaseFrom=0, clockChaseTo=0, clockChasePerf=0, clockChaseFrame=0, lastSnapshotCacheAt=-Infinity;
let stateIndex=0, fail=false, saved=false, nativeReads=0, mutations=0, warnings=0;
const finishAt=anchorGameTimestamp+4*86400000;
window.TradingNativeBridge={
 getPositions:()=>{nativeReads++;return '{"positions":[{"ticker":"QA"}]}';},
 submitOrder:()=>{mutations++;return '{"success":true}';},
 cancelTerminalAutoRenewal:()=>{mutations++;return '{"success":true}';},
 beginClockSync:()=>{stateIndex=0;return '{"status":"PENDING"}';},
 getClockSyncStatus:()=>JSON.stringify(stateIndex++<5 ? {status:'PENDING',progress:{fromGameTimestamp:latestSnapshot.clock.timestamp,
   currentGameTimestamp:latestSnapshot.clock.timestamp+Math.min(stateIndex,4)*86400000}} : fail ? {success:false,status:'FAILED',message:'QA write denied'} :
   {success:true,status:'COMPLETED',snapshot:{clock:{timestamp:finishAt,rate:48},career:{days:28}}})
};
function refreshSnapshot(snap){latestSnapshot=snap;saved=true;renderFlowingClock();}
function showPendingBackupRecovery(){}
window.showNativeSaveFailure=()=>{warnings++;};
"""
checks = """
window.qa={sync:()=>requestClockSync(true), read:()=>bridge.getPositions(), submit:()=>bridge.submitOrder(), cancel:()=>bridge.cancelTerminalAutoRenewal(),
 fail:value=>{fail=value;}, counts:()=>({nativeReads,mutations,warnings,running:clockSyncRunning,blocked:clockSyncBlocked}),
 timestamp:()=>clockChaseTo};
"""
out = root / "Artifacts/Codex-Audit-Ulang-2026-11-01/ui-clock-production"
out.mkdir(exist_ok=True)
with sync_playwright() as p:
    browser = p.chromium.launch(channel="chrome", headless=True)
    for reduced in (False, True):
        page = browser.new_page(viewport={"width": 320, "height": 700}, reduced_motion="reduce" if reduced else "no-preference")
        page.route("http://qa.local/", lambda route: route.fulfill(body='<time id="clock-display"></time><h1 id="home-date"></h1><button id="nav">Pasar</button><main></main>', content_type="text/html"))
        page.goto("http://qa.local/")
        dated_fixture = fixture.replace("Date.UTC(2026,10,1,2,15)", "Date.UTC(2026,11,30,2,15)") if reduced else fixture
        page.add_script_tag(content=dated_fixture + proxy + presenter + checks)
        assert '"QA"' in page.evaluate("qa.read()")
        page.evaluate("qa.sync();document.getElementById('nav').onclick=()=>document.getElementById('nav').dataset.visited='yes'")
        page.locator("#nav").click()
        assert page.locator("#nav").get_attribute("data-visited") == "yes"
        assert '"QA"' in page.evaluate("qa.read()")
        assert page.evaluate("qa.counts().nativeReads") == 1
        assert page.evaluate("(()=>{try{qa.submit();return false}catch(e){return true}})()")
        assert page.evaluate("(()=>{try{qa.cancel();return false}catch(e){return true}})()")
        assert page.locator("#clock-sync-status").count() == 0
        page.wait_for_function("!qa.counts().running")
        assert not page.evaluate("qa.counts().blocked")
        assert page.evaluate("qa.counts().mutations") == 0
        assert page.locator("#clock-display").inner_text() == "09:15 WIB"
        assert ("2027" if reduced else "2026") in page.locator("#home-date").inner_text()
        assert page.locator("#clock-display").get_attribute("aria-busy") is None
        assert "game_last_snapshot" in page.evaluate("Object.keys(localStorage)")
        page.evaluate("qa.fail(true);qa.sync().catch(()=>{})")
        page.wait_for_function("!qa.counts().running")
        assert page.evaluate("qa.counts().blocked")
        assert page.evaluate("qa.counts().warnings") == 1
        assert page.evaluate("(()=>{try{qa.submit();return false}catch(e){return true}})()")
        page.evaluate("qa.fail(false);qa.sync()")
        page.wait_for_function("!qa.counts().running")
        assert not page.evaluate("qa.counts().blocked")
        page.close()
    functions = []
    for name in ["node", "showFlow", "closeFlow", "showConfirmModal", "flowText", "flowButton", "openResearchTiersModal", "showGameToast", "renderToast", "processToastQueue"]:
        match = re.search(r"^    function " + name + r"\([^\n]*\)[^\n]*\{[\s\S]*?^    }", source, re.M)
        assert match, name
        functions.append(match.group())
    sizes = ((320,568),(360,640),(390,844),(412,915),(430,932),(768,1024),(1440,900))
    for width, height, scale, failure in ((w,h,s,f) for w,h in sizes for s in (100,200)
                                        for f in ("response", "exception", "no_message")):
        page = browser.new_page(viewport={"width":width,"height":height})
        page.set_content('<button id="opener">Paket riset</button><div id="toast-container"></div>')
        page.add_style_tag(path=str(root / "android/app/src/main/assets/www/style.css"))
        if scale == 200:
            page.add_style_tag(content="html {font-size:200% !important}")
        page.add_script_tag(content="""
let flowPanel=null,flowOpener=null,activeDetailStock=null,activeReceiptRequestId=null,activeDetailPositionInstrumentId=null;
const activeToastKeys=new Set(),settledEventKeys=new Set(),toastNotificationHistory=[],toastQueue=[];
const bridge={getResearchTiers:()=>JSON.stringify([{name:'PRO',title:'Pro',feeUsd:'250000',features:'QA sintetis',
 isCurrent:true,level:2,autoRenewal:true,expiresAt:1800000000000}]),subscribeResearchTier:()=>{},
 cancelTerminalAutoRenewal:()=>{if(window.qaFailure==='exception')throw new Error('QA koneksi gagal');return window.qaFailure==='no_message' ? '{"success":false}' : '{"success":false,"message":"QA simpan gagal"}';}};
""" + "\n".join(functions) + "\nwindow.openQaTiers=openResearchTiersModal;")
        page.evaluate("document.getElementById('opener').focus();window.qaFailure=" + repr(failure) + ";openQaTiers()")
        page.get_by_role("button", name="Hentikan perpanjangan otomatis").click()
        assert page.locator('[data-tier="PRO"]').count() == 0
        page.get_by_role("button", name="Ya, Lanjutkan").click()
        assert page.locator(".flow-page").count() == 0
        toast = page.locator("#toast-container .game-toast")
        assert toast.is_visible()
        assert "Pembatalan gagal" in toast.inner_text()
        expected = {"exception": "QA koneksi gagal", "response": "QA simpan gagal",
                    "no_message": "Perpanjangan otomatis belum dihentikan. Silakan coba lagi."}[failure]
        assert expected in toast.inner_text()
        box = toast.bounding_box()
        assert box and box['x'] >= 0 and box['x'] + box['width'] <= width, (width,scale,box)
        assert page.locator("#opener").evaluate("el=>el===document.activeElement")
        page.close()
    browser.close()
(out / "check.txt").write_text("PASS: production clock/bridge functions; native read cache; navigation; no waiting panel; financial gate during pending/failure; retry unlock; snapshot cache; reduced-motion. R10 actual flow/confirm/toast functions: failed response and exception remain visible after old flow removal, opener focus restored. Synthetic bridge fixture, not Java host/device integration.\n", encoding="utf-8")
print("PASS: production clock presenter and bridge gate with synthetic worker states")
