"""Reproducible production-asset browser checks; QA overrides never enter the APK.

Uses installed Playwright, JDK, Android stubs and JSON jar. Starts its own loopback
Java domain with a new QA save, closes only the processes it owns, and records
individual outcomes. --help does not start any process.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import socket
import shutil
import subprocess
import sys
import time
import urllib.request

from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parents[2]
TASK = Path(__file__).resolve().parent
JDK = Path('C:/Program Files/Android/Android Studio/jbr/bin')
SDK_JAR = Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk/platforms/android-36/android.jar'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--label', default='current')
    parser.add_argument('--skip-compile', action='store_true')
    parser.add_argument('--host', action='store_true', help='run selected Java domain regressions in new QA directories')
    parser.add_argument('--benchmark', action='store_true', help='three repeated browser update measurements, 30 samples per category')
    parser.add_argument('--sequence', action='store_true', help='10 seeds x 100 controlled UI actions with per-action assertions')
    parser.add_argument('--dense', action='store_true', help='benchmark 96 canonical 15-minute bars (one full game day)')
    parser.add_argument('--layout', action='store_true', help='production screens at seven viewports, normal and 200 percent root text')
    parser.add_argument('--interaction-benchmark', action='store_true', help='three repeated scroll-flush and browser-resume measurements')
    parser.add_argument('--decision-preview', action='store_true', help='QA-only previews for the unapproved out-of-window candle fallback')
    parser.add_argument('--busy', action='store_true', help='seed five real QA holdings and 24 open LIMIT orders before measurement')
    parser.add_argument('--ownership-preview', action='store_true', help='QA-only before/proposed preview of the existing ownership coverage note')
    parser.add_argument('--stats-preview', action='store_true', help='Capture existing nine-field issuer statistics using actual Java QA fills; requires --busy')
    parser.add_argument('--asset-source', type=Path, help='archived task-local app.js for a measured baseline')
    args = parser.parse_args()
    if args.stats_preview and not args.busy:
        parser.error('--stats-preview requires --busy')
    if not args.label.replace('-', '').replace('_', '').isalnum():
        parser.error('label must be alphanumeric')
    out = TASK / 'logs' / ('verified-' + args.label)
    out.mkdir(parents=True, exist_ok=True)
    classes = TASK / 'logs' / 'qa-classes'
    classes.mkdir(exist_ok=True)
    subprocess.run([sys.executable, str(ROOT / 'Work/prepare-host-json-reader.py'), str(classes)], check=True)
    jars = list((Path.home() / '.gradle/caches/modules-2/files-2.1/org.json/json/20240303').rglob('*.jar'))
    if len(jars) != 1:
        raise RuntimeError('Expected exactly one installed JSON jar')
    cp = os.pathsep.join(map(str, [classes, jars[0], SDK_JAR]))
    sources = list((ROOT / 'android/app/src/main/java/com/tradingglobal3d/core').rglob('*.java'))
    sources += [ROOT / 'android/app/src/main/java/com/tradingglobal3d/mobile' / n for n in ('TradingWebBridge.java', 'MainActivity.java')]
    sources += [ROOT / 'Work/WireQaServer.java']
    manifest = {str(p.relative_to(ROOT)): hashlib.sha256(p.read_bytes()).hexdigest() for p in sources}
    app_js = ROOT / 'android/app/src/main/assets/www/app.js'
    if args.asset_source:
        app_js = args.asset_source.resolve()
        if not app_js.is_relative_to(TASK / 'logs') or app_js.suffix != '.js':
            parser.error('Archived asset must be a task-local JavaScript file under logs/')
    manifest[str(app_js.relative_to(ROOT))] = hashlib.sha256(app_js.read_bytes()).hexdigest()
    for p in [Path(__file__).resolve(),ROOT/'android/app/src/main/assets/www/index.html',ROOT/'android/app/src/main/assets/www/style.css']:
        manifest[str(p.relative_to(ROOT))]=hashlib.sha256(p.read_bytes()).hexdigest()
    (out / 'source-manifest.json').write_text(json.dumps(manifest, indent=2), encoding='utf8')
    (out / 'app.js').write_bytes(app_js.read_bytes())
    if not args.skip_compile:
        argfile = out / 'javac-sources.txt'
        argfile.write_text('\n'.join('"' + p.as_posix() + '"' for p in sources), encoding='utf8')
        with (out / 'compile.log').open('w', encoding='utf8') as log:
            subprocess.run([str(JDK / 'javac.exe'), '-J-Xmx256m', '-encoding', 'UTF-8', '-cp', cp,
                            '-d', str(classes), '@' + str(argfile)], cwd=ROOT, stdout=log, stderr=log, check=True, timeout=90)
    if args.host:
        dependencies = []
        for artifact, version in [('junit/junit','4.13.2'), ('org.hamcrest/hamcrest-core','1.3')]:
            matches = list((Path.home() / '.gradle/caches/modules-2/files-2.1' / artifact / version).rglob('*.jar'))
            if len(matches) != 1:
                raise RuntimeError('Expected one installed test jar: ' + artifact)
            dependencies.extend(matches)
        test_cp = cp + os.pathsep + os.pathsep.join(map(str, dependencies))
        test_sources = list((ROOT / 'android/app/src/test/java').rglob('*.java'))
        test_args = out / 'javac-tests.txt'
        test_args.write_text('\n'.join('"' + p.as_posix() + '"' for p in test_sources), encoding='utf8')
        with (out / 'test-compile.log').open('w', encoding='utf8') as log:
            subprocess.run([str(JDK/'javac.exe'), '-J-Xmx256m', '-encoding', 'UTF-8', '-cp', test_cp,
                            '-d', str(classes), '@'+str(test_args)], cwd=ROOT, stdout=log, stderr=log, check=True, timeout=90)
        names = [p.stem for p in test_sources if p.stem.startswith('Package') and p.stem.endswith('Test')]
        names += ['ChartHistoryRangeTest','OrderReceiptActualsTest','OrderMatrixCombinationsTest',
                  'OrderCapacityQuoteTest','MarketClosedOrderQueueRegressionTest',
                  'Plan15SaveAndRecoveryRegressionTest','StartupPerformanceRegressionTest','SaveWritersConcurrencyRegressionTest']
        host_results = []
        for name in sorted(set(names)):
            scratch = out / ('qa-' + name + '-' + str(time.time_ns()))
            for rel in ['src/main/assets/data','android/app/src/main/assets/data']:
                dest = scratch / rel
                dest.parent.mkdir(parents=True, exist_ok=True)
                shutil.copytree(ROOT/'android/app/src/main/assets/data', dest)
            (scratch/'tmp').mkdir()
            command = [str(JDK/'java.exe'), '-Xmx384m', '-Djava.io.tmpdir='+str(scratch/'tmp'),
                       '-cp', test_cp, 'org.junit.runner.JUnitCore', 'com.tradingglobal3d.core.'+name]
            with (out/(name+'.log')).open('w', encoding='utf8') as log:
                started = time.monotonic()
                result = subprocess.run(command, cwd=scratch, stdout=log, stderr=log, timeout=120)
            host_results.append({'suite':name,'exitCode':result.returncode,'seconds':time.monotonic()-started,'command':command})
            print(name, 'PASS' if result.returncode==0 else 'FAIL', flush=True)
        (out/'host-results.json').write_text(json.dumps(host_results,indent=2), encoding='utf8')
        raise SystemExit(1 if any(r['exitCode'] for r in host_results) else 0)
    with socket.socket() as sock:
        sock.bind(('127.0.0.1', 0))
        port = sock.getsockname()[1]
    # ponytail: unique QA directory per invocation; never resume a player's save.
    saves = ROOT / 'Artifacts/Codex-LP-Verification' / ('qa-save-' + str(time.time_ns()))
    socket_dir = ROOT / '.socket-qa'
    socket_dir.mkdir(exist_ok=True)
    server_log = (out / 'server.log').open('w', encoding='utf8')
    proc = subprocess.Popen([str(JDK / 'java.exe'), '-Xmx384m', '-Djdk.net.unixdomain.tmpdir=' + str(socket_dir), '-Dqa.initialCapital=1000000000',
                             '-Dqa.fixedEpochMs=1793664000000', '-cp', cp,
                             'com.tradingglobal3d.mobile.WireQaServer',
                             str(ROOT / 'android/app/src/main/assets'), str(saves), str(port)],
                            cwd=ROOT, stdout=server_log, stderr=server_log,
                            creationflags=getattr(subprocess, 'CREATE_NO_WINDOW', 0))
    results = []
    try:
        url = f'http://127.0.0.1:{port}'
        for _ in range(300):
            if proc.poll() is not None:
                raise RuntimeError('QA server exited; inspect server.log')
            try:
                with urllib.request.urlopen(url + '/qa-meta', timeout=1) as response:
                    json.load(response)
                break
            except OSError:
                time.sleep(.1)
        else:
            raise TimeoutError('QA server not ready')
        with sync_playwright() as pw:
            browser = pw.chromium.launch(headless=True)
            context = browser.new_context(viewport={'width': 390, 'height': 844})
            context.route('**/*', lambda route: route.continue_() if route.request.url.startswith(url + '/')
                          or route.request.url.startswith(('data:', 'blob:')) else route.abort())
            hooks = '''
window.__lpTest = {refreshSnapshot, updateInstrumentDetailLive, updateIssuerOwnership,loadStocks,applyPlayerPreferences,
  renderFlowingClock, loadKeuanganView, openInstrumentDetail,
  requestClockSync, switchTab, getSnapshot: () => latestSnapshot, getAccount:()=>currentAccount,
  queue: () => pendingSnapshot, flags:()=>({clockSyncRunning,clockSyncBlocked,isUserScrolling}),
  defer: () => {pendingSnapshot=structuredClone(window.__qaFixture?.snapshot || latestSnapshot);}, scroll: onScrollActivity};
'''
            def script_route(route):
                source = app_js.read_text(encoding='utf8')
                pos = source.rfind('})();')
                assert pos >= 0
                route.fulfill(status=200, content_type='text/javascript', body=source[:pos] + hooks + source[pos:])
            context.route('**/app.js*', script_route)
            page = context.new_page()
            page.set_default_timeout(8000)
            errors = []
            page.on('pageerror', lambda e: errors.append(str(e)))
            page.goto(url, wait_until='networkidle')
            print('Browser boot', page.evaluate('typeof window.__lpTest'), errors, flush=True)
            page.wait_for_function('window.__lpTest && __lpTest.getSnapshot()?.personalAccount', timeout=10000)
            page.wait_for_function('!document.getElementById("clock-display")?.hasAttribute("aria-busy")')
            workload=None
            if args.busy:
                workload=page.evaluate('''async () => {
                  const n=window.TradingNativeBridge;
                  // ponytail: advance the injected real clock, then reuse production 48x synchronization.
                  const shifted=await fetch('/qa-action',{method:'POST',body:JSON.stringify({action:'offsetRealClock',elapsedMs:150000})});
                  if(!(await shifted.json()).success)throw Error('QA clock setup failed');
                  await __lpTest.requestClockSync(true);
                  if(!JSON.parse(n.getSnapshot()).clock.isIdxTradingOpen)throw Error('QA IDX session did not open');
                  await fetch('/qa-action',{method:'POST',body:JSON.stringify({action:'generateNpcLiquidity'})});
                  const snap=JSON.parse(n.getSnapshot()),accountId=snap.personalAccount.id;
                  if(!accountId)throw Error('Missing QA personal account');
                  const quotes=JSON.parse(n.getInstruments('','',null,true)),filled=[],attempts=[];
                  for(const q of quotes.filter(q=>q.exchange==='IDX'&&Number(q.currentPriceRaw)>1000)) {
                    const r=JSON.parse(n.submitOrder(JSON.stringify({accountId,instrumentId:q.id,side:'BUY',type:'MARKET',quantity:100,requestId:'QA-BUSY-HOLD-'+q.id})));
                    attempts.push({id:q.id,result:r});
                    if(r.status==='FILLED')filled.push(q.id);
                    if(filled.length===5)break;
                  }
                  if(filled.length!==5)throw Error('Could not seed five fully filled QA holdings: '+JSON.stringify({filled,attempts,clock:snap.clock,quote:quotes[0]}));
                  const q=quotes.find(q=>q.id==='IDX:BBCA'),limitPrice=String(Math.floor(Number(q.currentPriceRaw)*.85/100)*100);
                  const orders=[];
                  for(let i=0;i<24;i++) {
                    const r=JSON.parse(n.submitOrder(JSON.stringify({accountId,instrumentId:q.id,side:'BUY',type:'LIMIT',quantity:100,limitPrice,requestId:'QA-BUSY-OPEN-'+i})));
                    if(!r.success||r.status!=='OPEN')throw Error('QA open order seed failed: '+JSON.stringify(r));
                    orders.push(r.orderId);
                  }
                  return {kind:'New isolated Java game, five actual market fills and 24 OPEN player LIMIT orders',holdings:filled,openOrders:orders,limitPrice,portfolio:JSON.parse(n.getPortfolio(accountId))};
                }''')
                (out/'workload.json').write_text(json.dumps(workload,indent=2),encoding='utf8')
            # Stop automatic polling only in the test context, so each data transition is explicit.
            page.evaluate('''() => {
              for(let i=1;i<10000;i++) clearInterval(i);
              document.querySelector('.flow-page .back-button')?.click();
              const native = window.TradingNativeBridge;
              window.__qaNativeMethods={...native};
              window.__qaFixture = {quotes: JSON.parse(native.getInstruments('', '', null, true)),indices:JSON.parse(native.getIndices()),
                snapshot: JSON.parse(native.getSnapshot()), charts: {}, owner: null, ownerError: false, calls: []};
              const original = {};
              for(const name of ['getInstruments','getSnapshot','getChartData','getIssuerOwnership']) original[name]=native[name];
              native.getInstruments = (query='',exchange='',sector=null) => {__qaFixture.calls.push([query,exchange,sector]);return JSON.stringify(__qaFixture.quotes.filter(q=>
                (!query || (q.ticker+' '+q.name).toLowerCase().includes(query.toLowerCase())) &&
                (!exchange || q.exchange===exchange) && (!sector || q.sector===sector)));};
              native.getSnapshot = () => JSON.stringify(__qaFixture.snapshot);
              native.getIndices = () => JSON.stringify(__qaFixture.indices);
              native.getChartData = (id,tf) => __qaFixture.charts[id] ? JSON.stringify(__qaFixture.charts[id]) : original.getChartData(id,tf);
              native.getIssuerOwnership = id => {if(__qaFixture.ownerError) throw Error('QA temporary error');
                return __qaFixture.owner ? JSON.stringify(__qaFixture.owner) : original.getIssuerOwnership(id);};
              window.__qaCounts={};
              for(const name of Object.keys(native)) {
                if(typeof native[name]!=='function') continue;
                const fn=native[name];native[name]=(...args)=>{__qaCounts[name]=(__qaCounts[name]||0)+1;return fn(...args);};
              }
            }''')

            if args.stats_preview:
                stats = page.evaluate('''() => {
                  const n=window.TradingNativeBridge;Object.assign(n,__qaNativeMethods);
                  __lpTest.refreshSnapshot(JSON.parse(n.getSnapshot()));__lpTest.switchTab('beranda');
                  const stock=JSON.parse(n.getInstruments('BBCA','IDX',null,true))[0];
                  __lpTest.openInstrumentDetail(stock);
                  const values=Object.fromEntries([...document.querySelectorAll('.market-stats-card [data-price-field]')].map(el=>[el.dataset.priceField,el.textContent]));
                  if(Object.keys(values).length!==9||Object.values(values).includes('-'))throw Error('Daily statistics missing despite actual QA fills: '+JSON.stringify(values));
                  return {stock,values,clock:JSON.parse(n.getSnapshot()).clock};
                }''')
                page.evaluate("document.getElementById('toast-container')?.replaceChildren()")
                page.locator('.market-stats-card').screenshot(path=str(out/'daily-stats.png'))
                (out/'daily-stats.json').write_text(json.dumps({'scope':'Existing production table with actual isolated Java QA fills; no additional trade table',
                    'stats':stats,'pageErrors':errors},indent=2),encoding='utf8')
                browser.close()
                if errors:raise SystemExit(1)
                return

            if args.ownership_preview:
                page.evaluate('''() => {
                  Object.assign(window.TradingNativeBridge,__qaNativeMethods);
                  __lpTest.switchTab('beranda');document.querySelector('#home-market-list .stock-list-item').click();
                  document.querySelector('[data-tab-id="pemegang"]').click();
                }''')
                page.wait_for_function("document.getElementById('issuer-ownership-panel')?.dataset.status==='success'")
                observation=page.evaluate('''() => {
                  const p=document.getElementById('issuer-ownership-panel'),s=p.querySelector('.ownership-status');
                  const payload=JSON.parse(TradingNativeBridge.getIssuerOwnership(p.dataset.instrumentId));
                  return {coverage:payload.coverage,note:payload.coverageNote,display:getComputedStyle(s).display,text:s.textContent};
                }''')
                page.locator('#issuer-ownership-panel').screenshot(path=str(out/'ownership-current.png'))
                page.evaluate('''note => {
                  const s=document.querySelector('#issuer-ownership-panel .ownership-status');s.textContent=note;s.style.display='block';s.setAttribute('role','status');
                }''',observation['note'])
                page.locator('#issuer-ownership-panel').screenshot(path=str(out/'ownership-proposed.png'))
                (out/'ownership-preview.json').write_text(json.dumps({'scope':'Actual isolated Java ownership payload; proposed note visibility changes test DOM only','before':observation,'productionChanged':False},indent=2),encoding='utf8')
                browser.close()
                return

            if args.decision_preview:
                observation=page.evaluate('''() => {
                  __lpTest.switchTab('beranda');const q=__qaFixture.quotes[0],t=__qaFixture.snapshot.clock.timestamp;
                  __qaFixture.charts[q.id]={success:true,instrumentId:q.id,bars:[0,1,2].map(i=>({timestamp:t+i*900000,time:'QA '+(9+i)+':00',open:100,high:130,low:90,close:100+i*10,volume:100+i*100,partial:i===2}))};
                  document.querySelector('#home-market-list .stock-list-item').click();
                  const slider=document.querySelector('[data-chart-control="selected-candle"]');slider.value='0';slider.dispatchEvent(new Event('input'));
                  const selected=document.querySelector('.flow-content')._selectedCandleTimestamp;
                  __qaFixture.charts[q.id].bars.shift();__lpTest.updateInstrumentDetailLive(q.id,__qaFixture.quotes);
                  return {removedSelection:selected,currentSelection:document.querySelector('.flow-content')._selectedCandleTimestamp,readout:document.querySelector('#ohlc-readout').textContent};
                }''')
                page.locator('#instrument-chart-section').screenshot(path=str(out/'current-latest.png'))
                page.evaluate('''() => {const s=document.querySelector('[data-chart-control="selected-candle"]');s.value='0';s.dispatchEvent(new Event('input'));}''')
                page.locator('#instrument-chart-section').screenshot(path=str(out/'alternative-first.png'))
                (out/'decision.json').write_text(json.dumps({'scope':'Synthetic QA candles; preview only; no production fallback changed','observation':observation,'pending':'Owner approval for fallback after selected candle leaves the range'},indent=2),encoding='utf8')
                browser.close()
                return

            if args.interaction_benchmark:
                measured=[]
                for repeat in range(3):
                    for screen in ['scroll-main','scroll-detail','browser-resume']:
                        page.evaluate('''screen => {
                          document.querySelector('.flow-page .back-button')?.click();__lpTest.switchTab('beranda');
                          if(screen==='scroll-detail') document.querySelector('#home-market-list .stock-list-item').click();
                        }''',screen)
                        samples=page.evaluate('''async screen => {
                          const samples=[];
                          for(let i=-5;i<30;i++) {
                            while(__lpTest.flags().isUserScrolling) await new Promise(r=>setTimeout(r,20));
                            __qaCounts={};const start=performance.now();
                            if(screen==='browser-resume') {
                              document.dispatchEvent(new Event('visibilitychange'));
                              while(__lpTest.flags().clockSyncRunning) await new Promise(r=>setTimeout(r,10));
                              if(__lpTest.flags().clockSyncBlocked) throw Error('Browser resume synchronization failed');
                            } else {
                              document.querySelector(screen==='scroll-main'?'.app-scroll':'.flow-page').dispatchEvent(new Event('scroll'));
                              __lpTest.defer();
                              while(__lpTest.flags().isUserScrolling) await new Promise(r=>setTimeout(r,10));
                              if(__lpTest.queue()!==null) throw Error('Pending refresh did not flush');
                            }
                            await new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)));
                            if(i>=0) samples.push({ms:performance.now()-start,calls:{...__qaCounts}});
                          }
                          return samples;
                        }''',screen)
                        measured.append({'repeat':repeat+1,'screen':screen,'samples':samples})
                        print('Interaction',repeat+1,screen,len(samples),'samples',flush=True)
                (out/'interactions.json').write_text(json.dumps({'scope':'Browser event-to-flush/resume and two animation frames; scroll includes deliberate 150ms debounce; no Android background/process-death proof',
                    'assetSha256':hashlib.sha256(app_js.read_bytes()).hexdigest(),'runs':measured,'pageErrors':errors},indent=2),encoding='utf8')
                browser.close()
                if errors: raise SystemExit(1)
                return

            if args.layout:
                layouts=[]
                for width,height in [(320,568),(360,640),(390,844),(412,915),(430,932),(768,1024),(1440,900)]:
                    page.set_viewport_size({'width':width,'height':height})
                    for scale in [100,200]:
                        page.add_style_tag(content=f'html {{font-size:{scale}% !important}}')
                        for screen in ['beranda','pasar','candle','line','ownership']:
                            page.evaluate('''screen => {
                              document.querySelector('.flow-page .back-button')?.click();__lpTest.switchTab(screen==='pasar'?'pasar':'beranda');
                              const q=__qaFixture.quotes[0],t=__qaFixture.snapshot.clock.timestamp;
                              __qaFixture.charts[q.id]={success:true,instrumentId:q.id,bars:[{timestamp:t,time:'QA ACTIVE',open:100,high:120,low:90,close:110,volume:100,partial:true}]};
                              if(['candle','line','ownership'].includes(screen)) {
                                document.querySelector('#home-market-list .stock-list-item').click();
                                if(screen==='ownership')document.querySelector('[data-tab-id="pemegang"]').click();
                                else document.querySelector('[data-chart-control="type-'+screen+'"]').click();
                              }
                            }''',screen)
                            if screen=='ownership':
                                page.wait_for_function("document.getElementById('issuer-ownership-panel')?.dataset.status==='success'")
                                page.evaluate("document.querySelector('.ownership-status')?.scrollIntoView({block:'start',behavior:'instant'})")
                            else:
                                page.evaluate("document.querySelector('.candle-layout')?.scrollIntoView({block:'center',behavior:'instant'})")
                            page.wait_for_timeout(50)
                            geometry=page.evaluate('''() => {
                              const selectors=['.app-scroll','.flow-page','.candle-layout','[data-chart-control="selected-candle"]','#issuer-ownership-panel','.ownership-status'];
                              const boxes=selectors.map(s=>{const e=document.querySelector(s);if(!e||!e.getClientRects().length)return null;
                                const r=e.getBoundingClientRect();return {selector:s,left:r.left,right:r.right,width:r.width,inside:r.left>=-1&&r.right<=innerWidth+1};}).filter(Boolean);
                              return {rootFont:getComputedStyle(document.documentElement).fontSize,boxes,pass:boxes.every(b=>b.inside)};
                            }''')
                            layouts.append({'width':width,'height':height,'scale':scale,'screen':screen,**geometry})
                            page.screenshot(path=str(out/f'{screen}-{width}-text{scale}.png'))
                (out/'layouts.json').write_text(json.dumps({'scope':'Browser root-text scaling and bounds of touched containers; not Android system-font/touch proof','cases':layouts,'pageErrors':errors},indent=2),encoding='utf8')
                browser.close()
                if errors or any(not r['pass'] for r in layouts): raise SystemExit(1)
                return

            if args.benchmark:
                measured=[]
                for repeat in range(3):
                    for screen in ['beranda','pasar','candle','line']:
                        page.evaluate('''({screen,dense}) => {
                          document.querySelector('.flow-page .back-button')?.click();
                          __lpTest.switchTab(screen==='pasar'?'pasar':'beranda');
                          const q=__qaFixture.quotes[0];
                          const t=Math.floor(__qaFixture.snapshot.clock.timestamp/900000)*900000;
                          __qaFixture.charts[q.id]={success:true,instrumentId:q.id,range:'R1D',intervalMs:900000,
                            bars:[{timestamp:t,time:'QA ACTIVE',open:100,high:120,low:90,close:100,volume:100,partial:true}]};
                          if(dense) __qaFixture.charts[q.id].bars=Array.from({length:96},(_,i)=>({timestamp:t-(95-i)*900000,
                            time:'QA '+i,open:100,high:120,low:90,close:100,volume:100+i,partial:i===95}));
                          if(screen==='candle'||screen==='line') {
                            document.querySelector('#home-market-list .stock-list-item').click();
                            document.querySelector('[data-chart-control="type-'+screen+'"]').click();
                          }
                        }''',{'screen':screen,'dense':args.dense})
                        samples=page.evaluate('''async () => {
                          const samples=[];
                          for(let i=-5;i<30;i++) {
                            const q=__qaFixture.quotes[0];q.currentPriceRaw=String(100+(i+5)%10);q.currentPrice='Rp '+q.currentPriceRaw;
                            const bars=__qaFixture.charts[q.id].bars,b=bars[bars.length-1];b.close=Number(q.currentPriceRaw);b.volume+=100;
                            __qaCounts={};
                            const start=performance.now();
                            __lpTest.refreshSnapshot(__qaFixture.snapshot);
                            await new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)));
                            if(i>=0) samples.push({ms:performance.now()-start,calls:{...__qaCounts}});
                          }
                          return samples;
                        }''')
                        measured.append({'repeat':repeat+1,'screen':screen,'samples':samples})
                        print('Benchmark',repeat+1,screen,len(samples),'samples',flush=True)
                (out/'benchmark.json').write_text(json.dumps({'scope':'Browser refresh-to-two-animation-frames; synthetic quotes/bars; not Android IPC, FPS, or physical input latency',
                    'assetSha256':hashlib.sha256(app_js.read_bytes()).hexdigest(),'bars':96 if args.dense else 1,'workload':workload,'runs':measured,'pageErrors':errors},indent=2),encoding='utf8')
                browser.close()
                return

            if args.sequence:
                sequences=[]
                for seed in range(10):
                    actions=page.evaluate('''async seed => {
                      let random=71000+seed;const actions=[];
                      const next=()=>{random=(Math.imul(random,1664525)+1013904223)>>>0;return random;};
                      const q=__qaFixture.quotes[0],id=q.id;
                      const t=Math.floor(__qaFixture.snapshot.clock.timestamp/900000)*900000;
                      __qaFixture.charts[id]={success:true,instrumentId:id,range:'R1D',intervalMs:900000,
                        bars:[{timestamp:t-900000,time:'QA HISTORY',open:100,high:120,low:90,close:100,volume:100,partial:false},
                              {timestamp:t,time:'QA ACTIVE',open:100,high:120,low:90,close:100,volume:200,partial:true}]};
                      const close=()=>document.querySelector('.flow-page .back-button')?.click();
                      const open=()=>{if(!document.querySelector('.flow-page')){__lpTest.switchTab('beranda');document.querySelector('#home-market-list .stock-list-item').click();}};
                      for(let i=0;i<100;i++) {
                        const kind=next()%8,start=performance.now();
                        if(kind===0) {q.currentPriceRaw=String(100+i);q.currentPrice='Rp '+q.currentPriceRaw;__lpTest.refreshSnapshot(__qaFixture.snapshot);}
                        if(kind===1) {close();__lpTest.switchTab(document.querySelector('#view-beranda.active')?'pasar':'beranda');}
                        if(kind===2) {open();const controls=[...document.querySelectorAll('[data-timeframe]')];controls[(seed+i)%controls.length].click();if(!document.querySelector('.candle-layout'))throw Error('Missing canonical plot');}
                        if(kind===3) {open();const type=document.getElementById('live-line-path')?'candle':'line';document.querySelector('[data-chart-control="type-'+type+'"]').click();if(!document.querySelector(type==='line'?'#live-line-path':'.candle-body'))throw Error('Wrong chart mode');}
                        if(kind===4) {open();const slider=document.querySelector('input[data-chart-control="selected-candle"]');slider.value='0';slider.dispatchEvent(new Event('input'));const ts=document.querySelector('.flow-content')._selectedCandleTimestamp;__lpTest.updateInstrumentDetailLive(id,__qaFixture.quotes);if(document.querySelector('.flow-content')._selectedCandleTimestamp!==ts)throw Error('Selection moved');}
                        if(kind===5) {open();const b=__qaFixture.charts[id].bars[1];b.close=101+(i%10);b.volume+=100;__lpTest.updateInstrumentDetailLive(id,__qaFixture.quotes);if(JSON.stringify(document.querySelector('.flow-content')._chartBars)!==JSON.stringify(__qaFixture.charts[id].bars))throw Error('Noncanonical bars');}
                        if(kind===6) {close();if(document.querySelector('.flow-page'))throw Error('Overlay leak');}
                        if(kind===7) {const latest=__lpTest.getSnapshot(),old=structuredClone(latest);old.clock.timestamp-=1000;old.personalAccount.cash.IDR.available='QA STALE';__lpTest.refreshSnapshot(old);if(__lpTest.getSnapshot()!==latest)throw Error('Stale snapshot accepted');}
                        if(document.querySelectorAll('.flow-page').length>1)throw Error('Multiple overlays');
                        await new Promise(r=>requestAnimationFrame(r));
                        actions.push({index:i+1,kind,ms:performance.now()-start});
                      }
                      close();return actions;
                    }''',seed)
                    sequences.append({'seed':seed,'actions':actions})
                    print('Sequence seed',seed,'PASS',len(actions),'actions',flush=True)
                (out/'sequences.json').write_text(json.dumps({'scope':'Controlled UI actions, not physical touch or trading stress proof',
                    'runs':sequences,'pageErrors':errors},indent=2),encoding='utf8')
                browser.close()
                if errors:
                    raise SystemExit(1)
                return

            def check(name, expression):
                try:
                    detail = page.evaluate(expression)
                    results.append({'case': name, 'status': 'PASS', 'detail': detail})
                except Exception as exc:
                    results.append({'case': name, 'status': 'FAIL', 'detail': str(exc)})
                (out/'results.json').write_text(json.dumps({'scope':'Chromium + production assets; controlled QA bridge overrides','cases':results,'pageErrors':errors},indent=2),encoding='utf8')
                print(name, results[-1]['status'], flush=True)

            # A future quote in a different bucket must not rewrite a closed canonical candle.
            page.evaluate('''() => {
              __lpTest.switchTab('beranda');
              const q = __qaFixture.quotes[0]; q.currentPriceRaw='100'; q.currentPrice='Rp 100';
              const t = Math.floor(__qaFixture.snapshot.clock.timestamp/900000)*900000-900000;
              __qaFixture.charts[q.id]={success:true,instrumentId:q.id,range:'R1D',intervalMs:900000,
                bars:[{timestamp:t,time:'QA CLOSED',open:100,high:110,low:90,close:100,volume:100,partial:false}]};
              document.querySelector('#home-market-list .stock-list-item').click();
            }''')
            page.wait_for_selector('#live-candle-body')
            check('T07_closed_bar_not_mutated', '''() => {
              const panel=document.querySelector('.flow-content');
              const q=__qaFixture.quotes[0];q.currentPriceRaw='105';q.currentPrice='Rp 105';
              __lpTest.updateInstrumentDetailLive(q.id,__qaFixture.quotes);
              if(panel._chartBars[0].close!==100) throw Error('Closed candle rewritten from quote: '+panel._chartBars[0].close);
              return {close:panel._chartBars[0].close};
            }''')
            check('T08_line_updates_from_canonical_bars', '''() => {
              document.querySelector('[data-chart-control="type-line"]').click();
              const before=document.querySelector('#live-line-path').getAttribute('d');
              __qaFixture.charts[__qaFixture.quotes[0].id].bars[0].close=108;
              __lpTest.updateInstrumentDetailLive(__qaFixture.quotes[0].id,__qaFixture.quotes);
              const after=document.querySelector('#live-line-path').getAttribute('d');
              if(before===after) throw Error('Line path did not reflect changed canonical close');
              return {before,after};
            }''')
            check('T13_same_clock_pending_snapshot', '''async () => {
              document.querySelector('.flow-page .back-button').click();
              const a=structuredClone(__qaFixture.snapshot);
              __qaFixture.snapshot=a;
              __lpTest.scroll(); await __lpTest.requestClockSync(true);
              const b=structuredClone(a); b.personalAccount.cash.IDR.available='Rp 987.654';
              __qaFixture.snapshot=b; __lpTest.refreshSnapshot(b);
              await new Promise(r=>setTimeout(r,220));
              const actual=__lpTest.getSnapshot().personalAccount.cash.IDR.available;
              if(actual!==b.personalAccount.cash.IDR.available) throw Error('Pending older same-clock cash replaced newer cash: '+actual);
              return actual;
            }''')
            check('T10_owner_cache_preserves_nodes', '''() => {
              __qaFixture.owner={instrumentId:__qaFixture.quotes[0].id,sharesOutstanding:1000,
                coverage:'PARTIAL',coverageNote:'QA registry is partial',holders:[{name:'QA holder',quantity:200,isPersonalPlayer:false}]};
              document.querySelector('#home-market-list .stock-list-item').click();
              const host=document.createElement('section');host.id='issuer-ownership-panel';host.dataset.instrumentId=__qaFixture.quotes[0].id;
              host.innerHTML='<p class="ownership-status"></p><div class="ownership-meta"></div><div class="ownership-list"></div>';
              document.querySelector('.flow-content').append(host);
              __lpTest.updateIssuerOwnership(host,__qaFixture.quotes[0].id);
              const card=host.querySelector('.shareholder-card');
              const note=host.querySelector('.ownership-status'),textNode=note.firstChild;
              __lpTest.updateIssuerOwnership(host,__qaFixture.quotes[0].id);
              if(!card || card!==host.querySelector('.shareholder-card')) throw Error('Unchanged holder payload rebuilt cards');
              if(getComputedStyle(note).display==='none'||note.textContent!==__qaFixture.owner.coverageNote||note.firstChild!==textNode)throw Error('Coverage note hidden/replaced on cache hit');
              return {sameNode:true,coverageVisible:true};
            }''')
            check('T10_owner_retry_and_nonplayer_supply_change', '''() => {
              const id=__qaFixture.quotes[0].id, host=document.getElementById('issuer-ownership-panel');
              __qaFixture.owner.instrumentId='QA-WRONG-ISSUER';__lpTest.updateIssuerOwnership(host,id);
              if(host.dataset.status!=='error')throw Error('Wrong issuer identity accepted');
              __qaFixture.owner.instrumentId=id;
              __qaFixture.ownerError=true; __lpTest.updateIssuerOwnership(host,id);
              if(host.dataset.status!=='error') throw Error('Missing error state');
              __qaFixture.ownerError=false; host.querySelector('button').click();
              if(host.dataset.status!=='success') throw Error('Retry failed');
              if(host.querySelector('.ownership-status').getAttribute('role')!=='status'||getComputedStyle(host.querySelector('.ownership-status')).display==='none')throw Error('Retry left stale alert/hidden coverage note');
              __qaFixture.owner.sharesOutstanding=2000;
              __qaFixture.owner.holders[0].quantity=400;
              __lpTest.updateInstrumentDetailLive(id,__qaFixture.quotes);
              if(host.dataset.status!=='success' || !host.textContent.includes('400')) throw Error('Nonplayer ownership change remained stale');
              return {status:host.dataset.status,text:host.textContent};
            }''')
            check('T04_T05_selection_and_node_identity', '''() => {
              document.querySelector('.flow-page .back-button').click();
              const id=__qaFixture.quotes[0].id, bars=__qaFixture.charts[id].bars;
              const first=structuredClone(bars[0]);
              bars.push({...first,timestamp:first.timestamp+900000,time:'QA SECOND',close:102,volume:200});
              document.querySelector('#home-market-list .stock-list-item').click();
              const slider=document.querySelector('[data-chart-control="selected-candle"]');
              slider.value='0';slider.dispatchEvent(new Event('input'));slider.focus();
              const before=document.getElementById('ohlc-readout').textContent;
              bars[1].close=109;bars[1].volume=300;
              __lpTest.updateInstrumentDetailLive(id,__qaFixture.quotes);
              if(slider!==document.querySelector('[data-chart-control="selected-candle"]') || slider.value!=='0') throw Error('Historical selection/node changed');
              if(before!==document.getElementById('ohlc-readout').textContent || document.activeElement!==slider) throw Error('Historical readout/focus changed');
              slider.value='1';slider.dispatchEvent(new Event('input'));
              bars.push({...first,timestamp:first.timestamp+1800000,time:'QA THIRD',volume:400});
              slider.value='1';slider.dispatchEvent(new Event('input'));
              __lpTest.updateInstrumentDetailLive(id,__qaFixture.quotes);
              slider.value='1';slider.dispatchEvent(new Event('input'));
              const selected=bars[1].timestamp;bars.shift();
              __lpTest.updateInstrumentDetailLive(id,__qaFixture.quotes);
              if(document.querySelector('.flow-content')._selectedCandleTimestamp!==selected || slider.value!=='0') throw Error('Rolling window lost candle timestamp');
              bars.shift();__lpTest.updateInstrumentDetailLive(id,__qaFixture.quotes);
              const panel=document.querySelector('.flow-content');
              if(panel._selectedCandleTimestamp!==bars[bars.length-1].timestamp || panel._isViewingLatestCandle!==true)throw Error('Approved latest-candle fallback failed');
              return {preservedTimestamp:selected,fallbackTimestamp:panel._selectedCandleTimestamp,approvedFallback:'latest'};
            }''')
            page.evaluate('''() => {
              const id=__qaFixture.quotes[0].id,t=__qaFixture.snapshot.clock.timestamp,b=__qaFixture.charts[id].bars[0];
              __qaFixture.charts[id].bars=[0,1,2].map(i=>({...b,timestamp:t+i*900000,time:'QA POINTER '+i}));
              __lpTest.updateInstrumentDetailLive(id,__qaFixture.quotes);
              window.__qaSlider=document.querySelector('[data-chart-control="selected-candle"]');
            }''')
            slider=page.locator('[data-chart-control="selected-candle"]')
            slider.focus()
            page.keyboard.press('Home')
            page.keyboard.press('ArrowRight')
            check('T04_browser_keyboard', '''() => {
              const p=document.querySelector('.flow-content');
              if(__qaSlider.value!=='1'||p._selectedCandleTimestamp!==p._chartBars[1].timestamp)throw Error('Native range keyboard selection failed');
              return {value:__qaSlider.value};
            }''')
            page.evaluate('''async () => {
              __qaSlider.scrollIntoView({block:'center',behavior:'instant'});
              await new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)));
            }''')
            box=slider.bounding_box()
            assert page.evaluate('''b => document.elementFromPoint(b.x+b.width*.15,b.y+b.height/2)===__qaSlider''',box), 'QA pointer would hit an overlay instead of the slider'
            page.mouse.move(box['x']+box['width']*.15,box['y']+box['height']/2)
            page.mouse.down()
            page.evaluate('''() => {const id=__qaFixture.quotes[0].id;__qaFixture.charts[id].bars[2].volume+=100;__lpTest.updateInstrumentDetailLive(id,__qaFixture.quotes);}''')
            page.mouse.move(box['x']+box['width']*.75,box['y']+box['height']/2)
            page.mouse.up()
            check('T04_browser_pointer_during_refresh', '''() => {
              const p=document.querySelector('.flow-content'),s=document.querySelector('[data-chart-control="selected-candle"]');
              if(s!==__qaSlider||p._selectedCandleTimestamp!==p._chartBars[Number(s.value)].timestamp||document.activeElement!==s)throw Error('Pointer range state: '+JSON.stringify({same:s===__qaSlider,value:s.value,selected:p._selectedCandleTimestamp,expected:p._chartBars[Number(s.value)].timestamp,active:document.activeElement.outerHTML.slice(0,180),rect:s.getBoundingClientRect().toJSON()}));
              return {value:s.value,sameNode:true,scope:'Chromium mouse, not physical touch'};
            }''')
            check('T06_T07_T09_canonical_buckets_and_no_rebuild', '''() => {
              const id=__qaFixture.quotes[0].id;
              for(const tf of ['R6H','R1D','R7D','R30D']) {
                document.querySelector('[data-timeframe="'+tf+'"]').click();
                const panel=document.querySelector('.flow-content');
                if(JSON.stringify(panel._chartBars)!==JSON.stringify(__qaFixture.charts[id].bars)) throw Error('Canonical bars differ for '+tf);
                const layout=panel.querySelector('.candle-layout'), slider=panel.querySelector('input[type=range]');
                __qaFixture.charts[id].rangeEnd=(__qaFixture.charts[id].rangeEnd||0)+1000;
                __lpTest.updateInstrumentDetailLive(id,__qaFixture.quotes);
                if(layout!==panel.querySelector('.candle-layout') || slider!==panel.querySelector('input[type=range]')) throw Error('Clock metadata rebuilt chart');
              }
              __qaFixture.charts[id].bars=[];
              __lpTest.updateInstrumentDetailLive(id,__qaFixture.quotes);
              if(document.querySelector('.candle-layout') || !document.querySelector('.chart-empty-box')) throw Error('Empty data retained synthetic chart');
              return {timeframes:4,empty:true};
            }''')
            check('T06_axis_volume_and_empty_recovery', '''() => {
              const id=__qaFixture.quotes[0].id,t=__qaFixture.snapshot.clock.timestamp;
              __qaFixture.charts[id].bars=[{timestamp:t,time:'QA RECOVERY',open:100,high:200,low:50,close:180,volume:900,partial:true}];
              __lpTest.updateInstrumentDetailLive(id,__qaFixture.quotes);
              document.querySelector('[data-chart-control="type-candle"]').click();
              const panel=document.querySelector('.flow-content');
              if(panel._minPrice!==50||panel._maxPrice!==200||!panel.querySelector('.candle-layout'))throw Error('Axis/empty recovery failed');
              if(!panel.querySelector('#ohlc-readout').textContent.includes('Volume 900'))throw Error('Canonical volume missing');
              const b=__qaFixture.charts[id].bars[0];b.high=400;b.low=20;b.close=350;b.volume=1800;
              __lpTest.updateInstrumentDetailLive(id,__qaFixture.quotes);
              if(panel._minPrice!==20||panel._maxPrice!==400||!panel.querySelector('#ohlc-readout').textContent.includes('Close 350'))throw Error('Extrema/readout did not rescale');
              const wick=panel.querySelector('#live-candle-wick');
              if(!wick||Number(wick.getAttribute('y1'))!==20||Number(wick.getAttribute('y2'))!==170)throw Error('SVG wick not mapped to new extrema');
              return {min:panel._minPrice,max:panel._maxPrice,volume:panel._chartBars[0].volume};
            }''')
            check('T01_T02_home_and_movers_update_in_place', '''() => {
              document.querySelector('.flow-page .back-button').click();__lpTest.switchTab('beranda');
              const row=document.querySelector('#home-market-list .stock-list-item');
              const q=__qaFixture.quotes.find(q=>q.id===row.instrument.id);
              q.currentPrice='Rp 432';q.currentPriceRaw='432';q.dailyChangePercent=77;
              __qaCounts={};
              __lpTest.refreshSnapshot(__qaFixture.snapshot);
              if(row!==document.querySelector('#home-market-list .stock-list-item') || row.querySelector('.stock-price').textContent!==q.currentPrice) throw Error('Home quote did not update in place');
              if(__qaCounts.getInstruments!==1 || __qaCounts.getIndices!==1) throw Error('Duplicate quote/index refresh');
              __lpTest.switchTab('pasar');
              const index=__qaFixture.indices.find(x=>document.querySelector('.index-card[data-ticker="'+x.ticker+'"]'));
              if(!index) throw Error('No market index available for update assertion');
              const card=document.querySelector('.index-card[data-ticker="'+index.ticker+'"]');
              index.value='QA 12.345';index.changePercent='+7.89%';index.up=true;
              __qaFixture.quotes.forEach(x=>x.dailyChangePercent=0);q.dailyChangePercent=88;
              __lpTest.refreshSnapshot(__qaFixture.snapshot);
              if(card!==document.querySelector('.index-card[data-ticker="'+index.ticker+'"]') || card.querySelector('strong').textContent!==index.value || card.querySelector('.index-change-val').textContent!==index.changePercent) throw Error('Market index did not update in place');
              const top=document.querySelector('#market-highlights .highlight-group button');
              if(!top || top.dataset.instrumentId!==q.id) throw Error('Combined-tab movers not recomputed');
              return {top:q.id,homePrice:q.currentPrice};
            }''')
            page.locator('#search-stock-input').fill('NONEXISTENT999')
            page.wait_for_function("__qaFixture.calls.some(a=>a[0]==='NONEXISTENT999') && document.querySelectorAll('#stock-list-container .stock-list-item').length===0")
            check('T03_filter_empty_then_restore', '''() => {
              if(document.querySelectorAll('#stock-list-container .stock-list-item').length) throw Error('Empty filter still has rows');
              const input=document.getElementById('search-stock-input');input.value='BBCA';input.dispatchEvent(new Event('input'));
              return {empty:true};
            }''')
            page.wait_for_function("document.querySelectorAll('#stock-list-container .stock-list-item').length===1")
            check('T03_filter_remains_after_tick', '''() => {
              __lpTest.refreshSnapshot(__qaFixture.snapshot);
              const rows=[...document.querySelectorAll('#stock-list-container .stock-list-item')];
              if(rows.length!==1 || rows[0].instrument.ticker!=='BBCA') throw Error('Filter lost on update');
              return {ticker:rows[0].instrument.ticker};
            }''')
            check('T03_exchange_sector_watchlist', '''() => {
              const input=document.getElementById('search-stock-input'),ex=document.getElementById('filter-exchange-select'),
                sector=document.getElementById('filter-sector-select'),list=document.getElementById('filter-list-select');
              input.value='';ex.value='US';ex.dispatchEvent(new Event('change'));
              let rows=[...document.querySelectorAll('#stock-list-container .stock-list-item')];
              if(!rows.length||rows.some(r=>r.instrument.exchange!=='US'))throw Error('Exchange filter failed');
              ex.value='';const q=__qaFixture.quotes[0];sector.value=q.sector;sector.dispatchEvent(new Event('change'));
              rows=[...document.querySelectorAll('#stock-list-container .stock-list-item')];
              if(!rows.length||rows.some(r=>r.instrument.sector!==q.sector))throw Error('Sector filter failed');
              sector.value='';__qaFixture.quotes.forEach(x=>x.isWatchlist=x.id===q.id);
              list.value='watchlist';list.dispatchEvent(new Event('change'));__lpTest.refreshSnapshot(__qaFixture.snapshot);
              rows=[...document.querySelectorAll('#stock-list-container .stock-list-item')];
              if(rows.length!==1||rows[0].instrument.id!==q.id)throw Error('Watchlist changed after tick');
              list.value='all';list.dispatchEvent(new Event('change'));
              return {exchange:true,sector:true,watchlist:true};
            }''')
            cdp=context.new_cdp_session(page)
            def listeners():
                counts={}
                for expression in ['window','document','document.body','document.querySelector(".app-scroll")']:
                    obj=cdp.send('Runtime.evaluate',{'expression':expression,'objectGroup':'lp-listeners'})['result']['objectId']
                    found=cdp.send('DOMDebugger.getEventListeners',{'objectId':obj})['listeners']
                    counts[expression]=sorted(x['type'] for x in found)
                cdp.send('Runtime.releaseObjectGroup',{'objectGroup':'lp-listeners'})
                return counts
            listeners_before=listeners()
            check('T11_T14_detached_panel_and_repeated_close', '''() => {
              __lpTest.switchTab('beranda');
              const counts=[];
              for(let i=0;i<100;i++) {
                document.querySelector('#home-market-list .stock-list-item').click();
                __qaCounts={};__lpTest.refreshSnapshot(__qaFixture.snapshot);
                if(__qaCounts.getInstruments!==1||__qaCounts.getChartData!==1)throw Error('Duplicate/missing quote or chart request on cycle '+i);
                counts.push({quotes:__qaCounts.getInstruments,chart:__qaCounts.getChartData});
                const host=document.createElement('section');host.dataset.instrumentId='OLD';
                document.querySelector('.flow-content').append(host);
                document.querySelector('.flow-page .back-button').click();
                __lpTest.updateIssuerOwnership(host,'OLD');
                if(document.querySelectorAll('.flow-page').length!==0) throw Error('Overlay remained after close');
              }
              return {cycles:100,remaining:0,counts,scope:'DOM/closed-panel safety and per-update request bounds, not heap proof'};
            }''')
            listeners_after=listeners()
            results.append({'case':'T14_persistent_listener_bounds','status':'PASS' if listeners_before==listeners_after else 'FAIL',
                            'detail':{'before':listeners_before,'after':listeners_after,'scope':'persistent browser event targets; no heap or physical timer proof'}})
            cdp.detach()
            check('T12_scroll_main_and_detail_flush_current', '''async () => {
              for(const detail of [false,true]) {
                if(detail) document.querySelector('#home-market-list .stock-list-item').click();
                const target=document.querySelector(detail?'.flow-page':'.app-scroll');
                target.dispatchEvent(new Event('scroll')); __lpTest.defer();
                const b=structuredClone(__qaFixture.snapshot);b.personalAccount.cash.IDR.available=detail?'Rp 333':'Rp 222';
                __qaFixture.snapshot=b;
                // Smooth scrolling can emit more events after the dispatched event.
                for(let i=0;i<50 && __lpTest.flags().isUserScrolling;i++) await new Promise(r=>setTimeout(r,50));
                if(__lpTest.getSnapshot().personalAccount.cash.IDR.available!==b.personalAccount.cash.IDR.available) throw Error(JSON.stringify({detail,flags:__lpTest.flags(),pending:__lpTest.queue(),actual:__lpTest.getSnapshot().personalAccount.cash.IDR.available,expected:b.personalAccount.cash.IDR.available}));
                if(detail) document.querySelector('.flow-page .back-button').click();
              }
              return {main:true,detail:true};
            }''')
            check('T15_market_preview_and_limit_preservation', '''() => {
              __lpTest.switchTab('beranda');document.querySelector('#home-market-list .stock-list-item').click();
              document.querySelector('.btn-detail-buy').click();
              const qty=document.querySelector('#order-qty');
              if(!qty) throw Error('Missing quantity input');
              qty.value='1';qty.dispatchEvent(new Event('input'));
              const market=document.querySelector('input[name="order-type"][value="MARKET"]');
              market.checked=true;market.dispatchEvent(new Event('change'));
              __qaCounts={};__qaFixture.quotes[0].currentPriceRaw='107';__qaFixture.quotes[0].currentPrice='Rp 107';
              __lpTest.refreshSnapshot(__qaFixture.snapshot);
              if(!__qaCounts.previewOrder) throw Error('Market preview was not revalidated');
              const limit=document.querySelector('input[name="order-type"][value="LIMIT"]');
              limit.checked=true;limit.dispatchEvent(new Event('change'));
              const input=document.getElementById('order-limit-price');input.value='9275';input.dispatchEvent(new Event('input'));
              __qaFixture.quotes[0].currentPriceRaw='108';__lpTest.refreshSnapshot(__qaFixture.snapshot);
              if(input.value!=='9275') throw Error('Player LIMIT price overwritten');
              document.getElementById('btn-close-modal').click();document.querySelector('.flow-page .back-button')?.click();
              const next=structuredClone(__qaFixture.snapshot);next.runId='QA-NEXT-RUN';next.personalAccount.cash.IDR.available='Rp 555';
              __qaCounts={};
              __lpTest.scroll();__lpTest.defer();__qaFixture.snapshot=next;__lpTest.refreshSnapshot(next);
              if(__lpTest.queue()!==null || __lpTest.getSnapshot().runId!==next.runId) throw Error('Old queue survived accepted run change');
              if(__qaCounts.getPlayerProfile!==1||__qaCounts.getMacroAndSynthesisState!==1)throw Error('Same-date new run reused profile/macro cache');
              const counts={...__qaCounts};__lpTest.refreshSnapshot(next);
              if(__qaCounts.getPlayerProfile!==counts.getPlayerProfile||__qaCounts.getMacroAndSynthesisState!==counts.getMacroAndSynthesisState)throw Error('Stable run reloaded unchanged profile/macro cache');
              return {limit:input.value,run:next.runId,profileReloads:counts.getPlayerProfile,macroReloads:counts.getMacroAndSynthesisState};
            }''')
            check('LP08_menu_badge_reuses_canonical_snapshot', '''() => {
              const previous=structuredClone(__qaFixture.snapshot);
              const n=TradingNativeBridge,profileRead=n.getPlayerProfile,progressRead=n.getPlayerProgress;
              try {
                __qaFixture.snapshot.career.title='QA Canonical Rank';__qaFixture.snapshot.playerProgress.level=7;
                __qaCounts={};__lpTest.switchTab('riset');
                const calls={profile:__qaCounts.getPlayerProfile||0,progress:__qaCounts.getPlayerProgress||0};
                const text=document.querySelector('#view-riset .m-tag').textContent;
                if(calls.profile||calls.progress)throw Error('Menu badge redundantly fetched profile/progress: '+JSON.stringify(calls));
                if(text!=='Lv.7 · QA Canonical Rank')throw Error('Menu badge is not canonical: '+text);
                const fallback={profile:0,progress:0};
                n.getPlayerProfile=()=>{fallback.profile++;return JSON.stringify({careerRankLabel:'QA Fallback Rank'});};
                n.getPlayerProgress=()=>{fallback.progress++;return JSON.stringify({level:9});};
                delete __qaFixture.snapshot.playerProgress;__lpTest.switchTab('riset');
                if(fallback.profile!==0||fallback.progress!==1||document.querySelector('#view-riset .m-tag').textContent!=='Lv.9 · QA Canonical Rank')throw Error('Partial snapshot did not fetch only the missing level');
                delete __qaFixture.snapshot.career;__lpTest.switchTab('riset');
                if(fallback.profile!==1||fallback.progress!==2||document.querySelector('#view-riset .m-tag').textContent!=='Lv.9 · QA Fallback Rank')throw Error('Missing snapshot badge did not retain the existing fallback');
                return {calls,text,fallback,scope:'Controlled missing-field QA payloads; original assertions retained'};
              } finally {
                n.getPlayerProfile=profileRead;n.getPlayerProgress=progressRead;__qaFixture.snapshot=previous;__lpTest.switchTab('beranda');
              }
            }''')
            check('T15_real_java_account_switch', '''async () => {
              document.getElementById('btn-close-modal').click();document.querySelector('.flow-page .back-button')?.click();
              const n=window.TradingNativeBridge;Object.assign(n,__qaNativeMethods);
              __lpTest.refreshSnapshot(JSON.parse(n.getSnapshot()));
              if(!JSON.parse(n.saveGame()).success)throw Error('Isolated QA baseline save failed');
              const seeded=await fetch('/qa-action',{method:'POST',body:JSON.stringify({action:'seedPortfolioManager'})});
              if(!(await seeded.json()).success)throw Error('QA career prerequisite not seeded');
              const client=JSON.parse(n.acceptClientMandate('MANDATE-01'));
              if(!client.success)throw Error('QA client mandate rejected: '+JSON.stringify(client));
              window.__qaClient=client;
              __lpTest.refreshSnapshot(JSON.parse(n.getSnapshot()));__lpTest.switchTab('beranda');
              document.querySelector('#home-market-list .stock-list-item').click();document.querySelector('.btn-detail-buy').click();
              const qty=document.getElementById('order-qty');qty.value='1';qty.dispatchEvent(new Event('input'));
              const select=document.getElementById('order-account-select');
              if(!select)throw Error('Real client account missing in ticket');
              for(const id of [client.accountId,__lpTest.getSnapshot().personalAccount.id,client.accountId]) {
                select.value=id;select.dispatchEvent(new Event('change'));
                if(__lpTest.getAccount().id!==id)throw Error('Account selection did not change');
                const port=JSON.parse(n.getPortfolio(id));
                if(document.getElementById('order-cash-avail').textContent!==port.cash.IDR.available)throw Error('Ticket cash belongs to another account');
              }
              __lpTest.refreshSnapshot(JSON.parse(n.getSnapshot()));
              if(__lpTest.getAccount().id!==client.accountId)throw Error('Valid selected client lost on refresh');
              document.getElementById('btn-close-modal').click();document.querySelector('.flow-page .back-button')?.click();
              return {clientAccountId:client.accountId,scope:'Synthetic QA career prerequisite; actual Java-created client and account cash; no order submitted'};
            }''')
            check('T15_real_java_restore_removes_selected_account', '''() => {
              const n=window.TradingNativeBridge,loaded=JSON.parse(n.loadGame());
              if(!loaded.success)throw Error('New isolated QA save did not restore');
              const snap=JSON.parse(n.getSnapshot());
              if(snap.tradingAccounts.some(a=>a.id===__qaClient.accountId))throw Error('QA baseline unexpectedly contains client');
              __lpTest.refreshSnapshot(snap);
              if(__lpTest.getAccount()?.id!==snap.personalAccount.id)throw Error('Removed client remains selected after real QA restore: '+__lpTest.getAccount()?.id);
              return {run:snap.runId,selected:__lpTest.getAccount().id};
            }''')
            check('T15_real_java_new_run_same_date', '''() => {
              const n=window.TradingNativeBridge,before=JSON.parse(n.getSnapshot());
              const created=JSON.parse(n.startNewGame(JSON.stringify({initialCapital:250000000,playerName:'QA Context'})));
              if(!created.success)throw Error('Isolated QA new run failed');
              const after=JSON.parse(n.getSnapshot());
              if(after.runId===before.runId||after.clock.date!==before.clock.date)throw Error('QA new-run/same-date precondition failed');
              __lpTest.refreshSnapshot(after);
              if(__lpTest.getAccount()?.id!==after.personalAccount.id||__lpTest.getAccount()?.cash?.IDR?.available!==after.personalAccount.cash.IDR.available)throw Error('New real Java run retained old account/cash');
              if(document.getElementById('home-avail-cash').textContent!==after.personalAccount.cash.IDR.available)throw Error('New run home cash is stale');
              return {before:before.runId,after:after.runId,date:after.clock.date,cash:after.personalAccount.cash.IDR.available};
            }''')
            check('T13_real_java_same_clock_order_reservation', '''async () => {
              const n=window.TradingNativeBridge,a=JSON.parse(n.getSnapshot());
              __lpTest.refreshSnapshot(a);__lpTest.scroll();
              const scrolling=setInterval(()=>__lpTest.scroll(),40);
              try {await __lpTest.requestClockSync(true);} finally {clearInterval(scrolling);}
              if(!__lpTest.queue())throw Error('QA scroll did not defer the actual sync snapshot');
              const order=JSON.parse(n.submitOrder(JSON.stringify({accountId:a.personalAccount.id,instrumentId:'IDX:BBCA',side:'BUY',type:'LIMIT',quantity:100,limitPrice:'9000',requestId:'QA-SAME-CLOCK-RESERVE'})));
              if(!order.success||order.status!=='OPEN')throw Error('QA financial mutation did not queue/reserve: '+JSON.stringify(order));
              const b=JSON.parse(n.getSnapshot());
              if(a.clock.timestamp!==b.clock.timestamp||a.personalAccount.cash.IDR.available===b.personalAccount.cash.IDR.available)throw Error('QA same-clock financial mutation precondition failed');
              __lpTest.refreshSnapshot(b);
              for(let i=0;i<50&&__lpTest.flags().isUserScrolling;i++)await new Promise(r=>setTimeout(r,50));
              const current=__lpTest.getSnapshot();
              if(current.personalAccount.cash.IDR.available!==b.personalAccount.cash.IDR.available||current.personalAccount.cash.IDR.reserved!==b.personalAccount.cash.IDR.reserved)throw Error('Real Java order reservation overwritten by stale same-clock snapshot');
              return {timestamp:b.clock.timestamp,availableBefore:a.personalAccount.cash.IDR.available,availableAfter:b.personalAccount.cash.IDR.available,reserved:b.personalAccount.cash.IDR.reserved,orderId:order.orderId};
            }''')
            check('LP09_clock_does_not_rewind_on_sync', '''async () => {
              const n=window.TradingNativeBridge,original={begin:n.beginClockSync,status:n.getClockSyncStatus};
              const snapshot=structuredClone(__qaFixture.snapshot),own=Object.getOwnPropertyDescriptor(performance,'now');
              let now=performance.now(),promise;
              try {
                Object.defineProperty(performance,'now',{configurable:true,value:()=>now});
                const snap=structuredClone(snapshot);snap.runId='QA-CLOCK-NO-REWIND';
                snap.clock.timestamp=Date.parse('2026-11-03T23:59:50+07:00');snap.clock.rate=48;
                __lpTest.refreshSnapshot(snap);now+=2000;__lpTest.renderFlowingClock();
                const before=document.getElementById('clock-display').textContent;
                let polls=0;
                n.beginClockSync=()=>JSON.stringify({success:true,status:'PENDING'});
                n.getClockSyncStatus=()=>JSON.stringify(++polls===1?{success:true,status:'PENDING',progress:{
                  fromGameTimestamp:snap.clock.timestamp,currentGameTimestamp:snap.clock.timestamp}}:
                  {success:true,status:'COMPLETED',snapshot:snap});
                promise=__lpTest.requestClockSync(true);
                const during=document.getElementById('clock-display').textContent;
                await promise;const after=document.getElementById('clock-display').textContent;
                if(before!==during||before!==after)throw Error('Clock rewound: '+JSON.stringify({before,during,after}));
                return {before,during,after,scope:'Controlled delayed snapshot at WIB midnight; actual sync presenter'};
              } finally {
                if(promise)await promise.catch(()=>{});
                n.beginClockSync=original.begin;n.getClockSyncStatus=original.status;
                if(own)Object.defineProperty(performance,'now',own);else delete performance.now;
                __lpTest.refreshSnapshot(snapshot);
              }
            }''')
            check('LP09_date_rollover_and_single_format', '''() => {
              const snapshot=structuredClone(__qaFixture.snapshot),own=Object.getOwnPropertyDescriptor(performance,'now');
              let now=performance.now();
              try {
                Object.defineProperty(performance,'now',{configurable:true,value:()=>now});
                const snap=structuredClone(snapshot);snap.runId='QA-DATE-ROLLOVER';snap.career.days=12;
                snap.clock.timestamp=Date.parse('2026-11-03T23:59:50+07:00');snap.clock.date='2026-11-03';snap.clock.rate=48;
                __lpTest.refreshSnapshot(snap);now+=1000;__lpTest.renderFlowingClock();
                const expected='Hari 13 · '+new Date(snap.clock.timestamp+48000).toLocaleDateString('id-ID',{
                  timeZone:'Asia/Jakarta',day:'2-digit',month:'long',year:'numeric'});
                const before=document.getElementById('home-date').textContent;
                const beforeNode=document.getElementById('home-date').firstChild;
                __lpTest.refreshSnapshot(snap);const after=document.getElementById('home-date').textContent;
                if(before!==expected||after!==expected)throw Error('Date stale or format flips: '+JSON.stringify({before,after,expected}));
                if(beforeNode!==document.getElementById('home-date').firstChild)throw Error('Snapshot replaces unchanged date/month text');
                const dateNode=document.getElementById('home-date').firstChild;__lpTest.renderFlowingClock();
                if(dateNode!==document.getElementById('home-date').firstChild)throw Error('Unchanged date replaces DOM text');
                snap.runId='QA-MONTH-ROLLOVER';snap.clock.timestamp=Date.parse('2026-10-31T23:59:50+07:00');snap.clock.date='2026-10-31';
                __lpTest.refreshSnapshot(snap);now+=1000;__lpTest.renderFlowingClock();
                const monthAfter=document.getElementById('home-date').textContent;
                __lpTest.refreshSnapshot(snap);
                if(!monthAfter.includes('01 November 2026')||document.getElementById('home-date').textContent!==monthAfter)
                  throw Error('Month rollover flickers or stays stale: '+monthAfter);
                return {before,after,monthAfter};
              } finally {
                if(own)Object.defineProperty(performance,'now',own);else delete performance.now;
                __lpTest.refreshSnapshot(snapshot);
              }
            }''')
            check('LP09_calendar_filters_by_WIB_day', '''() => {
              const n=window.TradingNativeBridge,feed=n.getResearchFeed,getSnapshot=n.getSnapshot,snapshot=structuredClone(__qaFixture.snapshot);
              try {
                const snap=structuredClone(snapshot);snap.runId='QA-CALENDAR-WIB';
                snap.clock.timestamp=Date.parse('2026-11-04T01:00:00+07:00');snap.clock.date='2026-11-04';
                __qaFixture.snapshot=snap;
                n.getSnapshot=()=>JSON.stringify(snap);
                __lpTest.refreshSnapshot(snap);
                n.getResearchFeed=()=>JSON.stringify({news:[],calendar:[
                  {date:'2026-11-04',title:'QA agenda WIB hari ini',category:'MACRO',categoryLabel:'Makro',details:'QA'},
                  {date:'2026-11-03',title:'QA agenda kemarin',category:'MACRO',categoryLabel:'Makro',details:'QA'},
                  {date:'2026-11-05',title:'QA agenda besok',category:'MACRO',categoryLabel:'Makro',details:'QA'}]});
                __lpTest.switchTab('keuangan');__lpTest.loadKeuanganView('kalender');
                const parent=document.getElementById('keuangan-subtab-content'),text=parent.textContent;
                if(parent.querySelector('.day-card.active strong')?.textContent!=='4'||!text.includes('QA agenda WIB hari ini')||text.includes('QA agenda kemarin'))
                  throw Error('Calendar label/filter disagree at 01:00 WIB: '+text);
                snap.clock.timestamp=Date.parse('2026-11-05T00:01:00+07:00');snap.clock.date='2026-11-05';
                __lpTest.refreshSnapshot(snap);
                if(parent.querySelector('.day-card.active strong')?.textContent!=='5'||!parent.textContent.includes('QA agenda besok')||parent.textContent.includes('QA agenda WIB hari ini'))
                  throw Error('Calendar did not refresh at next WIB day');
                return {day:'4',included:'QA agenda WIB hari ini',excluded:'QA agenda kemarin',rolloverDay:'5'};
              } finally {n.getResearchFeed=feed;n.getSnapshot=getSnapshot;__qaFixture.snapshot=snapshot;__lpTest.switchTab('beranda');__lpTest.refreshSnapshot(snapshot);}
            }''')
            check('LP09_detail_latest_daily_prices', '''() => {
              const stock=structuredClone(__qaFixture.quotes[0]);
              Object.assign(stock,{currentPriceRaw:'9100',currentPrice:'Rp 9.100',openPriceRaw:'9050',previousCloseRaw:'9000',highPriceRaw:'9200',lowPriceRaw:'8900',
                volumeLots:'3',dayValueRaw:'2720000',avgPriceRaw:'9066.6667',dailyCeilingRaw:'11250',dailyFloorRaw:'6750'});
              try {
                __lpTest.openInstrumentDetail(stock);
                const read=field=>document.querySelector('[data-price-field="'+field+'"]')?.textContent;
                if(read('previousCloseRaw')!=='9.000'||read('highPriceRaw')!=='9.200'||read('lowPriceRaw')!=='8.900')throw Error('Initial daily prices missing');
                if(read('openPriceRaw')!=='9.050'||read('volumeLots')!=='3'||read('dayValueRaw')!=='2.72 M'||read('avgPriceRaw')!=='9.066,6667'
                    ||read('dailyCeilingRaw')!=='11.250'||read('dailyFloorRaw')!=='6.750')throw Error('Initial nine-column statistics incomplete');
                const node=document.querySelector('[data-price-field="highPriceRaw"]');
                Object.assign(stock,{currentPriceRaw:'9300',currentPrice:'Rp 9.300',highPriceRaw:'9400',lowPriceRaw:'8800',volumeLots:'5',dayValueRaw:'4580000',avgPriceRaw:'9160'});
                __lpTest.updateInstrumentDetailLive(stock.id,[stock]);
                if(document.querySelector('.flow-content .metric-hero')?.textContent!=='Rp 9.300'||read('highPriceRaw')!=='9.400'||read('lowPriceRaw')!=='8.800')
                  throw Error('Live issuer price/statistics did not update');
                if(read('volumeLots')!=='5'||read('dayValueRaw')!=='4.58 M'||read('avgPriceRaw')!=='9.160')throw Error('Daily volume/value/average stayed stale');
                __lpTest.updateInstrumentDetailLive(stock.id,[stock]);
                if(node!==document.querySelector('[data-price-field="highPriceRaw"]'))throw Error('Stable price node replaced');
                return {last:'Rp 9.300',previous:'9.000',high:'9.400',low:'8.800',scope:'Controlled quote transition; actual issuer detail'};
              } finally {document.querySelector('.flow-page .back-button')?.click();__lpTest.switchTab('beranda');}
            }''')
            page.screenshot(path=str(out / 'browser.png'), full_page=True)
            browser.close()
        (out / 'results.json').write_text(json.dumps({'scope':'Chromium + production assets; controlled QA bridge overrides',
            'cases':results,'pageErrors':errors}, indent=2), encoding='utf8')
        if any(r['status'] != 'PASS' for r in results) or errors:
            raise SystemExit(1)
    finally:
        proc.terminate()
        try:
            proc.wait(timeout=10)
        except subprocess.TimeoutExpired:
            proc.kill()
            proc.wait(timeout=5)
        server_log.close()


if __name__ == '__main__':
    main()
