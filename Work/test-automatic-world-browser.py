"""Actual Java bridge checks for automatic play, page exit, reentry and responsive home."""
import argparse
import json
from pathlib import Path
import time
from urllib.request import urlopen
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parent.parent
parser = argparse.ArgumentParser()
parser.add_argument('--url', default='http://127.0.0.1:18917')
parser.add_argument('--output-dir', required=True, type=Path)
args = parser.parse_args()
out = args.output_dir.resolve()
assert out.is_relative_to(ROOT / 'Artifacts')
meta = json.load(urlopen(args.url + '/qa-meta'))
assert meta['platform']['testFixture'] and not meta['platform']['nativeHost']
out.mkdir(parents=True, exist_ok=True)
errors, samples = [], []
with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page(viewport={'width':462, 'height':648})
    page.on('pageerror', lambda e: errors.append(str(e)))
    page.goto(args.url); page.wait_for_load_state('networkidle')
    snapshot = lambda: page.evaluate('JSON.parse(TradingNativeBridge.getSnapshot())')
    before = snapshot()['clock']['timestamp']
    assert not snapshot()['clock']['isPaused']
    assert page.locator('#btn-briefing, #home-clock-state, .session-card').count() == 0
    page.wait_for_timeout(1300)
    assert snapshot()['clock']['timestamp'] > before
    page.evaluate('TradingNativeBridge.setClockState(false, 1)')
    assert not snapshot()['clock']['isPaused']

    before = snapshot()['clock']['timestamp']
    page.goto('about:blank')
    time.sleep(1.3)
    page.goto(args.url); page.wait_for_load_state('networkidle')
    offline_game_ms = snapshot()['clock']['timestamp'] - before
    offline = page.evaluate('JSON.parse(TradingNativeBridge.getOfflineCatchupReport())')
    assert offline['realElapsedMs'] >= 1000, offline
    assert abs(offline['gameSecondsAdvanced'] * 1000 - offline['realElapsedMs'] * 24) < 1000, offline
    assert offline_game_ms >= offline['gameSecondsAdvanced'] * 1000, offline_game_ms
    assert not snapshot()['clock']['isPaused']

    for width, height in [(320,568),(360,640),(390,844),(412,915),(430,932),(462,648),(768,1024),(1440,900)]:
        page.set_viewport_size({'width':width,'height':height})
        for scale in [100,200]:
            page.evaluate('(scale)=>document.documentElement.style.fontSize=scale+"%"', scale)
            page.wait_for_timeout(100)
            assert page.locator('.nav-item').count() == 5
            assert page.evaluate('document.documentElement.scrollWidth <= innerWidth + 1')
            if scale == 100:
                assert page.locator('.bottom-nav').bounding_box()['height'] < 130
            page.locator('#btn-reports').scroll_into_view_if_needed()
            image = f'home-{width}x{height}-text{scale}.png'
            page.screenshot(path=str(out/image))
            samples.append({'viewport':[width,height],'textScale':scale,'screenshot':image})
    browser.close()
assert not errors, errors
(out/'results.json').write_text(json.dumps({'status':'PASS','scope':'Browser and actual Java host; not Android',
    'automaticStartup':True,'legacyPauseIgnored':True,'offlineGameMsAfterPageExit':offline_game_ms,'offlineReport':offline,
    'samples':samples,'pageErrors':errors},indent=2),encoding='utf-8')
print(json.dumps({'status':'PASS','responsiveSamples':len(samples),'offlineGameMs':offline_game_ms}))
