"""One authorized 60-minute QA-device session. Never targets the production package."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess
import time
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parents[2]
OUT = Path(__file__).resolve().parent / 'logs/device-session-2026-11-03' / ('run-' + str(time.time_ns()))
ADB = 'C:/Users/HYPE AMD/AppData/Local/Android/Sdk/platform-tools/adb.exe'
SERIAL = 'AYW4LJZHOZI7TOZP'
PACKAGE = 'com.tradingglobal3d.mobile.qa'
COMPONENT = PACKAGE + '/com.tradingglobal3d.mobile.MainActivity'


def adb(*args, timeout=25):
    result = subprocess.run([ADB, '-s', SERIAL, *args], capture_output=True, text=True,
                            encoding='utf8', errors='replace', timeout=timeout)
    if result.returncode:
        raise RuntimeError('ADB failed: ' + ' '.join(args) + ': ' + result.stderr)
    return result.stdout


def foreground():
    state = adb('shell', 'dumpsys', 'activity', 'activities')
    resumed = [line for line in state.splitlines() if 'topResumedActivity=' in line]
    if not any(COMPONENT in line for line in resumed):
        raise RuntimeError('QA is not foreground; input/screenshot refused: ' + str(resumed))


def connect(pw):
    pid = adb('shell', 'pidof', PACKAGE).strip()
    if not pid.isdigit():
        raise RuntimeError('QA PID missing')
    port = adb('forward', 'tcp:0', 'localabstract:webview_devtools_remote_' + pid).strip()
    for _ in range(30):
        try:
            browser = pw.chromium.connect_over_cdp('http://127.0.0.1:' + port, timeout=3000)
            pages = [page for context in browser.contexts for page in context.pages
                     if page.url == 'file:///android_asset/www/index.html']
            if len(pages) == 1:
                return browser, pages[0], pid, port
        except Exception:
            time.sleep(.2)
    raise RuntimeError('QA WebView connection missing')


def native(page, method, *args):
    return page.evaluate('''({method,args}) => JSON.parse(TradingNativeBridge[method](...args))''',
                         {'method': method, 'args': list(args)})


def verify_installed_apk(expected_sha256):
    paths = adb('shell', 'pm', 'path', PACKAGE).splitlines()
    base = [line.removeprefix('package:').strip() for line in paths if line.strip().endswith('/base.apk')]
    if len(base) != 1:
        raise RuntimeError('QA base APK missing or ambiguous')
    installed = OUT / 'installed-base.apk'
    adb('pull', base[0], str(installed), timeout=60)
    actual = hashlib.sha256(installed.read_bytes()).hexdigest()
    if actual != expected_sha256:
        raise RuntimeError('Installed QA APK differs from the supplied artifact: ' + actual)
    return actual


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apk', required=True, type=Path, help='Archived QA APK to verify against the installed package')
    apk = parser.parse_args().apk.resolve()
    if not apk.is_relative_to(ROOT) or not apk.is_file():
        parser.error('Supply an existing archived QA APK inside this workspace')
    OUT.mkdir(parents=True)
    state_path = OUT / 'session.json'
    if state_path.exists():
        raise RuntimeError('Session evidence exists; inspect it before resuming or starting another session')
    started = time.monotonic()
    started_unix = time.time()
    state = {'status': 'RUNNING', 'startedUnix': started_unix, 'runnerPid': __import__('os').getpid(),
             'targetSeconds': 3600, 'scope': 'Actual QA Android app, synthetic touch via CDP; not physical finger',
             'package': PACKAGE, 'actions': [], 'lifecycle': [], 'samples': [], 'pageErrors': []}
    state['runnerSha256'] = hashlib.sha256(Path(__file__).read_bytes()).hexdigest()
    state['qaApkPath'] = str(apk)
    state['qaApkSha256'] = hashlib.sha256(apk.read_bytes()).hexdigest()

    def save():
        state['elapsedSeconds'] = time.monotonic() - started
        state_path.write_text(json.dumps(state, indent=2), encoding='utf8')

    def sample(page, label):
        foreground()
        snap = native(page, 'getSnapshot')
        metadata = native(page, 'getSaveMetadata')
        (OUT / (label + '-snapshot.json')).write_text(json.dumps(snap, indent=2), encoding='utf8')
        for name, args in {
            'meminfo': ('shell', 'dumpsys', 'meminfo', PACKAGE),
            'gfxinfo': ('shell', 'dumpsys', 'gfxinfo', PACKAGE, 'framestats'),
            'display': ('shell', 'dumpsys', 'display'),
            'power': ('shell', 'dumpsys', 'power'),
            'thermal': ('shell', 'dumpsys', 'thermalservice'),
        }.items():
            (OUT / (label + '-' + name + '.txt')).write_text(adb(*args), encoding='utf8')
        page.screenshot(path=str(OUT / (label + '.png')))
        state['samples'].append({'label': label, 'elapsed': time.monotonic() - started,
                                 'clock': snap.get('clock'), 'runId': snap.get('runId'), 'save': metadata})
        save()

    def tap(page, selector):
        foreground()
        locator = page.locator(selector).first
        locator.scroll_into_view_if_needed(timeout=6000)
        box = locator.bounding_box()
        if not box:
            raise RuntimeError('Missing target: ' + selector)
        page.evaluate('''() => {window.__qaTapResult=new Promise(resolve=>{
          document.addEventListener('click',()=>{const start=performance.now();
            requestAnimationFrame(()=>requestAnimationFrame(()=>resolve(performance.now()-start)));
          },{once:true,capture:true});});}''')
        cdp = page.context.new_cdp_session(page)
        cdp.send('Input.dispatchTouchEvent', {'type': 'touchStart', 'touchPoints': [
            {'x': box['x'] + box['width'] / 2, 'y': box['y'] + box['height'] / 2}]})
        cdp.send('Input.dispatchTouchEvent', {'type': 'touchEnd', 'touchPoints': []})
        cdp.detach()
        measurement = page.evaluate('window.__qaTapResult')
        return measurement

    save()
    forward_ports = []
    try:
        state['installedApkSha256'] = verify_installed_apk(state['qaApkSha256'])
        save()
        with sync_playwright() as pw:
            browser, page, pid, port = connect(pw)
            forward_ports.append(port)
            page.set_default_timeout(7000)
            page.on('pageerror', lambda error: state['pageErrors'].append(str(error)))
            initial = native(page, 'getSnapshot')
            if initial.get('clock', {}).get('rate') != 48:
                raise RuntimeError('Expected 48x domain clock')
            state['initialRunId'] = initial['runId']
            state['initialGameTimestamp'] = initial['clock']['timestamp']
            state['initialPid'] = pid
            sample(page, 'start')
            adb('shell', 'dumpsys', 'gfxinfo', PACKAGE, 'reset')
            lifecycle_done = set()
            sample_done = set()
            index = 0
            while time.monotonic() - started < 3600:
                if (OUT/'STOP').exists():
                    raise RuntimeError('Session stop requested; incomplete endurance evidence retained')
                elapsed = time.monotonic() - started
                foreground()
                if elapsed >= 600 and 'background' not in lifecycle_done:
                    before = native(page, 'getSnapshot')
                    adb('shell', 'input', 'keyevent', 'KEYCODE_HOME')
                    # No input/screenshot outside QA while it is deliberately backgrounded.
                    until = time.monotonic() + 30
                    while time.monotonic() < until:
                        time.sleep(min(1, until - time.monotonic()))
                    launch = adb('shell', 'am', 'start', '-W', '-n', COMPONENT)
                    page.wait_for_function('document.getElementById("clock-display")?.getAttribute("aria-busy")!=="true"')
                    after = native(page, 'getSnapshot')
                    if after['runId'] != before['runId'] or after['clock']['timestamp'] < before['clock']['timestamp']:
                        raise RuntimeError('Background/resume lost run/time')
                    state['lifecycle'].append({'kind': 'background30s', 'before': before['clock'], 'after': after['clock'], 'launch': launch})
                    lifecycle_done.add('background')
                if elapsed >= 1200 and 'forceStop' not in lifecycle_done:
                    saved = native(page, 'saveGame')
                    if not saved.get('success'):
                        raise RuntimeError('QA checkpoint save failed before force-stop')
                    before = native(page, 'getSnapshot')
                    meta = native(page, 'getSaveMetadata')
                    adb('shell', 'am', 'force-stop', PACKAGE)
                    time.sleep(8)
                    launch = adb('shell', 'am', 'start', '-W', '-n', COMPONENT)
                    browser, page, pid, port = connect(pw)
                    forward_ports.append(port)
                    page.on('pageerror', lambda error: state['pageErrors'].append(str(error)))
                    page.wait_for_function('document.getElementById("clock-display")?.getAttribute("aria-busy")!=="true"')
                    after = native(page, 'getSnapshot')
                    if after['runId'] != before['runId'] or after['clock']['timestamp'] < before['clock']['timestamp']:
                        raise RuntimeError('Force-stop restore lost run/time')
                    if after['personalAccount']['cash'] != before['personalAccount']['cash']:
                        raise RuntimeError('Force-stop changed QA cash unexpectedly')
                    state['lifecycle'].append({'kind': 'savedForceStop8s', 'before': before['clock'], 'after': after['clock'], 'save': meta, 'launch': launch, 'newPid': pid})
                    lifecycle_done.add('forceStop')
                if elapsed >= 1800 and 'middle' not in sample_done:
                    sample(page, 'middle')
                    sample_done.add('middle')
                kind = index % 8
                result = {'index': index + 1, 'elapsed': time.monotonic() - started, 'kind': kind}
                if kind == 0:
                    result['clickToTwoBrowserFramesMs'] = tap(page, '.nav-item[data-tab="beranda"]')
                    if not page.locator('#view-beranda.active').count():
                        raise RuntimeError('Home navigation failed')
                elif kind == 1:
                    result['clickToTwoBrowserFramesMs'] = tap(page, '.nav-item[data-tab="pasar_porto"]')
                    result['visibleQuotes'] = page.locator('#stock-list-container .stock-list-item').count()
                    if not result['visibleQuotes']:
                        raise RuntimeError('Market lost quotes')
                elif kind == 2:
                    tap(page, '.nav-item[data-tab="beranda"]')
                    result['clickToTwoBrowserFramesMs'] = tap(page, '#home-market-list .stock-list-item')
                    if not page.locator('.flow-page').count():
                        raise RuntimeError('Detail did not open')
                    result['bars'] = page.locator('.candle-body').count()
                elif kind == 3:
                    if not page.locator('[data-chart-control="type-line"]').count():
                        tap(page, '.nav-item[data-tab="beranda"]')
                        tap(page, '#home-market-list .stock-list-item')
                    result['clickToTwoBrowserFramesMs'] = tap(page, '[data-chart-control="type-line"]')
                    if page.locator('.candle-layout').count() and not page.locator('#live-line-path').count():
                        raise RuntimeError('Line mode missing path')
                elif kind == 4:
                    if page.locator('[data-timeframe="R7D"]').count():
                        result['clickToTwoBrowserFramesMs'] = tap(page, '[data-timeframe="R7D"]')
                    if page.locator('.flow-page .back-button').count():
                        tap(page, '.flow-page .back-button')
                elif kind in (5, 6):
                    tab = 'firma' if kind == 5 else 'riset'
                    result['clickToTwoBrowserFramesMs'] = tap(page, '.nav-item[data-tab="' + tab + '"]')
                else:
                    tap(page, '.nav-item[data-tab="beranda"]')
                    # Native touch scrolling through the QA surface, without global phone settings.
                    adb('shell', 'input', 'swipe', '640', '1900', '640', '900', '450')
                    result['scroll'] = True
                state['actions'].append(result)
                if index % 10 == 0:
                    snap = native(page, 'getSnapshot')
                    if snap.get('runId') != state['initialRunId'] or snap.get('clock', {}).get('rate') != 48:
                        raise RuntimeError('Run or automatic 48x clock changed')
                    save()
                    print('QA minute', round((time.monotonic() - started) / 60, 1), '| actions', len(state['actions']), flush=True)
                index += 1
                until = time.monotonic() + 3
                while time.monotonic() < until:
                    time.sleep(min(.5, until - time.monotonic()))
            tap(page, '.nav-item[data-tab="beranda"]')
            sample(page, 'end')
            final = native(page, 'getSnapshot')
            state['finalGameTimestamp'] = final['clock']['timestamp']
            state['finalRunId'] = final['runId']
            state['finalSave'] = native(page, 'saveGame')
            state['status'] = 'PASS' if not state['pageErrors'] else 'FAIL'
            if not {'background', 'forceStop'}.issubset(lifecycle_done):
                raise RuntimeError('Lifecycle steps incomplete')
    except Exception as error:
        state['status'] = 'STOPPED_WITH_ERROR'
        state['error'] = str(error)
        print('SESSION STOPPED:', error, flush=True)
    finally:
        save()
        for port in forward_ports:
            try:
                adb('forward', '--remove', 'tcp:' + port)
            except Exception as error:
                state.setdefault('cleanupErrors', []).append(str(error))
        save()
    print('Evidence:', state_path, '| status:', state['status'], flush=True)
    return 0 if state['status'] == 'PASS' else 1


if __name__ == '__main__':
    raise SystemExit(main())
