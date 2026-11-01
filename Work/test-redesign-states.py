"""Capture five controlled presentation states against an isolated Java QA host."""
import argparse
import hashlib
import json
import os
from pathlib import Path
from urllib.parse import urlparse
from playwright.sync_api import sync_playwright

parser = argparse.ArgumentParser()
parser.add_argument('--url', default='http://127.0.0.1:'+os.environ.get('QA_PORT', '8897'))
parser.add_argument('--output-dir', required=True)
args = parser.parse_args()
assert urlparse(args.url).hostname in ['localhost', '127.0.0.1']
out = Path(args.output_dir).resolve()
assert out.is_relative_to(Path(__file__).resolve().parent.parent)
out.mkdir(parents=True, exist_ok=True)
report = {'scope':'Six viewport/text combinations × five controlled presentation states; not Android offline, native-save persistence, or all-controls certification', 'samples':[], 'pageErrors':[]}

GEOMETRY = """() => {
 const visible=el=>el.getClientRects().length&&!el.closest('[inert]')&&getComputedStyle(el).visibility!=='hidden';
 const controls=[...document.querySelectorAll('button,input,select,textarea,a[href],summary,[role="button"]')].filter(visible);
 const bounds=el=>{const r=el.getBoundingClientRect();return {id:el.id,tag:el.tagName,text:(el.innerText||'').slice(0,70),width:r.width,height:r.height};};
 const canvas=document.createElement('canvas').getContext('2d');
 const selects=controls.filter(el=>el.tagName==='SELECT').map(el=>{const style=getComputedStyle(el);canvas.font=style.fontStyle+' '+style.fontWeight+' '+style.fontSize+' '+style.fontFamily;return {id:el.id,text:el.selectedOptions[0]?.textContent,font:canvas.font,textWidth:canvas.measureText(el.selectedOptions[0]?.textContent||'').width,available:el.clientWidth-parseFloat(style.paddingLeft)-parseFloat(style.paddingRight)-24};});
 return {viewport:[innerWidth,innerHeight],rootWidth:document.documentElement.scrollWidth,font:getComputedStyle(document.documentElement).fontSize,
 overflow:[...document.querySelectorAll('body *')].filter(visible).filter(el=>{const r=el.getBoundingClientRect();return r.left< -1||r.right>innerWidth+1;}).map(bounds),
 smallTargets:controls.map(el=>el.matches('input[type="checkbox"],input[type="radio"]')?(el.labels[0]||el):el).filter(el=>{const r=el.getBoundingClientRect();return r.width<47.5||r.height<47.5;}).map(bounds),
 clippedSelects:selects.filter(el=>el.textWidth>el.available+1),
 selects,
 scrollers:[...document.querySelectorAll('.app-scroll,.flow-page')].filter(visible).map(el=>({id:el.id,clientWidth:el.clientWidth,scrollWidth:el.scrollWidth}))};
}"""


def save_report():
    (out/'states.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')


def capture(page, state, width, height, scale, detail):
    sample = {'state':state,'viewport':[width,height],'textScale':scale,'detail':detail}
    report['samples'].append(sample)
    try:
        geometry = page.evaluate(GEOMETRY)
        sample['geometry'] = geometry
        path = f'{state}-{width}x{height}-text{scale}.png'
        page.screenshot(path=str(out/path))
        sample['screenshot'] = path
        assert geometry['rootWidth'] <= width+1, geometry
        assert not geometry['overflow'] and not geometry['smallTargets'] and not geometry['clippedSelects'], geometry
        assert all(s['scrollWidth'] <= s['clientWidth']+1 for s in geometry['scrollers']), geometry
        sample['status'] = 'PASS'
    except Exception as error:
        sample.update(status='FAIL', error=str(error))
        raise
    finally:
        save_report()


with sync_playwright() as p:
    request = p.request.new_context(base_url=args.url)
    response = request.get('/qa-meta')
    assert response.ok
    report['platform'] = response.json()['platform']
    assert report['platform']['testFixture'] and not report['platform']['nativeHost']
    report['sourceHashes'] = {name:hashlib.sha256(request.get('/'+name).body()).hexdigest() for name in ['app.js','style.css']}
    browser = p.chromium.launch(headless=True)
    for width,height in [(360,800),(390,844),(412,915)]:
        for scale in [100,200]:
            context = browser.new_context(viewport={'width':width,'height':height}, reduced_motion='reduce')
            page = context.new_page()
            page.on('pageerror', lambda error: report['pageErrors'].append(str(error)))
            pending = []
            page.route('**/app.js', lambda route: pending.append(route))
            page.goto(args.url, wait_until='commit')
            page.locator('#home-net-worth').wait_for(state='visible')
            page.wait_for_function('getComputedStyle(document.documentElement).backgroundColor==="rgb(8, 10, 18)"')
            page.evaluate('s=>document.documentElement.style.fontSize=s+"%"', scale)
            assert page.locator('#home-net-worth').inner_text() == 'Memuat…'
            assert page.evaluate('typeof window.showNativeSaveFailure') == 'undefined'
            capture(page,'initial-loading',width,height,scale,'app.js response held before initialization; original loading copy, no fabricated game data')
            assert len(pending) == 1
            pending.pop().continue_()
            page.unroute('**/app.js')
            page.wait_for_function('typeof window.showNativeSaveFailure==="function"')
            page.wait_for_function('!document.getElementById("home-net-worth").textContent.includes("Memuat")')
            page.evaluate('s=>document.documentElement.style.fontSize=s+"%"', scale)

            previous = page.locator('#home-net-worth').inner_text()
            page.evaluate('''()=>{window.snapshotReader=TradingNativeBridge.getSnapshot;TradingNativeBridge.getSnapshot=()=>{throw Error('Gangguan snapshot QA terencana')}}''')
            status = page.locator('#snapshot-status')
            status.wait_for(state='visible')
            assert 'Data terakhir tetap ditampilkan' in status.inner_text()
            assert page.locator('#home-net-worth').inner_text() == previous
            status.scroll_into_view_if_needed()
            capture(page,'snapshot-failure-recovery',width,height,scale,'getSnapshot throws; actual last snapshot retained; recovery asserted with the original reader')
            page.evaluate('()=>{TradingNativeBridge.getSnapshot=window.snapshotReader}')
            page.get_by_role('button',name='Coba muat progres kembali',exact=True).click()
            status.wait_for(state='detached')
            assert page.locator('#home-net-worth').inner_text().strip()
            recovered = f'snapshot-recovered-{width}x{height}-text{scale}.png'
            page.locator('#home-net-worth').scroll_into_view_if_needed()
            page.screenshot(path=str(out/recovered))
            report['samples'][-1]['recoveryScreenshot'] = recovered

            page.evaluate('()=>{window.showNativeSaveFailure();window.showNativeSaveFailure()}')
            warning = page.locator('#native-save-warning')
            assert warning.count() == 1 and warning.get_attribute('role') == 'alert'
            assert 'belum tersimpan' in warning.inner_text()
            capture(page,'native-save-warning',width,height,scale,'Native callback invoked directly for presentation; duplicate calls create one alert; no persistence-failure claim')

            page.locator('.nav-item[data-tab=riset]').click(); page.locator('#view-riset').get_by_role('button',name='Profil Saya',exact=True).click()
            page.get_by_role('button',name='Pencapaian',exact=True).click()
            page.locator('#player-text-size').select_option(str(scale)+'%')
            assert page.locator('#player-text-size').evaluate('el=>el.selectedOptions[0].textContent') == str(scale)+'%'
            page.get_by_label('Kontras tinggi',exact=True).check()
            page.get_by_label('Kurangi gerakan',exact=True).check()
            page.get_by_role('button',name='Simpan Profil & Preferensi',exact=True).click()
            page.get_by_text('Profil dan preferensi tersimpan.',exact=True).wait_for(state='visible')
            assert page.evaluate('document.documentElement.classList.contains("high-contrast")&&document.documentElement.classList.contains("reduced-motion")')
            assert page.evaluate('getComputedStyle(document.documentElement).backgroundColor') == 'rgb(3, 5, 8)'
            assert page.locator('.flow-page button').first.evaluate('el=>getComputedStyle(el).transitionDuration') == '0s'
            capture(page,'high-contrast-reduced-motion',width,height,scale,'Preferences saved via actual UI and bridge; high-contrast canvas and disabled transitions verified')
            context.close()

            missing = browser.new_context(viewport={'width':width,'height':height})
            missing.route('**/browser-bridge.js', lambda route: route.abort())
            page = missing.new_page()
            page.on('pageerror', lambda error: report['pageErrors'].append(str(error)))
            page.goto(args.url)
            page.get_by_role('heading',name='Permainan belum terhubung',exact=True).wait_for(state='visible')
            page.evaluate('s=>document.documentElement.style.fontSize=s+"%"', scale)
            assert page.evaluate('typeof window.TradingNativeBridge') == 'undefined'
            assert not page.locator('.bottom-nav').is_visible()
            page.get_by_role('button',name='Coba hubungkan kembali',exact=True).focus()
            capture(page,'missing-bridge',width,height,scale,'QA browser-bridge.js blocked in a separate context; explicit disconnected state replaces game values')
            missing.close()
    assert len(report['samples']) == 30 and not report['pageErrors'], report
    browser.close()
    request.dispose()
report['status'] = 'PASS'
save_report()
print(json.dumps({'status':'PASS','samples':len(report['samples']),'screenshots':36,'scope':report['scope']}))
