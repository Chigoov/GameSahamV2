"""Compare production and the proposed market retry against the live isolated Java QA host."""
import hashlib
import json
import sys
from pathlib import Path
from runpy import run_path
from playwright.sync_api import sync_playwright

TASK=Path(__file__).resolve().parent
ROOT=TASK.parents[1]
THEME=ROOT/'Work/Preview-Tema-Stockbit-2026-11-03'
URL=json.loads((THEME/'runtime.json').read_text())['url']
OUT=TASK/'logs/lp10-market-retry'
OUT.mkdir(exist_ok=True)
PRODUCTION=ROOT/'android/app/src/main/assets/www/app.js'
PROPOSED=TASK/'logs/lp10-market-retry-proposed.js'
BEFORE=TASK/'logs/lp10-market-retry-before.js'
SCAN=run_path(str(THEME/'verify_preview.py'))['SCAN']

def main():
    results=[];inputs={};contrast=[]
    with sync_playwright() as p:
        browser=p.chromium.launch(headless=True)
        for label,source in [('before',BEFORE if BEFORE.exists() else PRODUCTION),('proposed',PRODUCTION if '--production' in sys.argv else PROPOSED)]:
            inputs[label]={'path':str(source.relative_to(ROOT)),'sha256':hashlib.sha256(source.read_bytes()).hexdigest()}
            page=browser.new_page(viewport={'width':412,'height':915})
            errors=[];page.on('pageerror',lambda e:errors.append(str(e)))
            page.route('**/app.js*',lambda route:route.fulfill(path=str(source),content_type='application/javascript'))
            page.goto(URL,wait_until='networkidle')
            page.evaluate('() => {for(let i=1;i<10000;i++)clearInterval(i);window.qaOriginalQuotes=TradingNativeBridge.getInstruments;}')
            page.locator('[data-tab="pasar_porto"]').click();page.locator('#subtab-btn-pasar').click()
            assert page.locator('#stock-list-container [data-instrument-id]').count()>0
            for failure in ['throw','bad-json','error-object','null']:
                errors.clear()
                page.evaluate('''kind => {TradingNativeBridge.getInstruments=(...args)=>{
                    if(kind==='throw')throw Error('QA temporary quote read failure');
                    if(kind==='bad-json')return '{';
                    if(kind==='null')return 'null';
                    return JSON.stringify({success:false,message:'QA temporary quote read failure'});
                };}''',failure)
                page.locator('#chip-filter-us').click()
                page.wait_for_timeout(80)
                status=page.locator('[data-market-load-error]')
                observation={'variant':label,'failure':failure,'pageErrors':errors.copy(),
                    'errorVisible':status.count()==1,'oldRows':page.locator('#stock-list-container [data-instrument-id]').count()}
                results.append(observation)
                if label=='proposed':
                    assert status.count()==1 and observation['oldRows']==0 and not errors
                    if failure=='throw':
                        status.scroll_into_view_if_needed();page.screenshot(path=str(OUT/'proposed-error-mobile.png'))
                    # Repeated failure stays one error state, not duplicated or an empty success.
                    status.locator('button').click();assert page.locator('[data-market-load-error]').count()==1
                page.evaluate('() => {TradingNativeBridge.getInstruments=qaOriginalQuotes;}')
                if label=='proposed': page.locator('[data-market-load-error] button').click()
                else: page.locator('#subtab-btn-pasar').click()
                assert not page.locator('[data-market-load-error]').count()
                rows=page.locator('#stock-list-container [data-instrument-id]')
                assert rows.count()>0
                assert all(name.startswith('US:') for name in rows.evaluate_all('(es)=>es.map(e=>e.dataset.instrumentId)'))
                observation['retryRestoredFilteredRows']=rows.count()
            if label=='proposed':
                # Search debounce uses the same boundary as exchange/list/sector/filter callers.
                page.evaluate('() => {TradingNativeBridge.getInstruments=()=>{throw Error("QA debounce failure")};}')
                page.locator('#search-stock-input').fill('AAPL');page.wait_for_timeout(350)
                assert page.locator('[data-market-load-error]').count()==1 and not errors
                page.evaluate('() => {TradingNativeBridge.getInstruments=qaOriginalQuotes;}')
                page.locator('[data-market-load-error] button').click()
                assert page.locator('#stock-list-container [data-instrument-id="US:AAPL"]').count()==1
                assert page.locator('#stock-list-container [data-instrument-id]').count()==1
                # Initial navigation also shares the error state when the index read fails first.
                page.evaluate('() => {window.qaOriginalIndices=TradingNativeBridge.getIndices;TradingNativeBridge.getIndices=()=>"null";}')
                page.locator('#subtab-btn-pasar').click()
                assert page.locator('[data-market-load-error]').count()==1 and not errors
                page.evaluate('() => {TradingNativeBridge.getIndices=qaOriginalIndices;}')
                page.locator('[data-market-load-error] button').click()
                assert page.locator('#stock-list-container [data-instrument-id="US:AAPL"]').count()==1
                # Preview both new themes at small screens and doubled text, including keyboard retry.
                page.evaluate('() => {TradingNativeBridge.getInstruments=()=>{throw Error("QA layout failure")};}')
                page.locator('#chip-filter-all').click()
                for theme in ['stockbit-dark','stockbit-light']:
                    page.select_option('#preview-theme',theme);page.wait_for_timeout(200)
                    for width in [320,360,412,768]:
                        for scale in [1,2]:
                            page.set_viewport_size({'width':width,'height':915})
                            page.evaluate('(scale)=>document.documentElement.style.fontSize=16*scale+"px"',scale)
                            status=page.locator('[data-market-load-error]');status.scroll_into_view_if_needed()
                            assert status.evaluate('(e)=>{const r=e.getBoundingClientRect();return r.left>=0&&r.right<=innerWidth+1&&e.scrollWidth<=e.clientWidth+1}')
                    page.evaluate('document.documentElement.style.fontSize=""')
                    page.set_viewport_size({'width':412,'height':915})
                    page.locator('[data-market-load-error]').scroll_into_view_if_needed()
                    scan=page.evaluate(SCAN);assert not scan['contrast'],scan['contrast']
                    contrast.append({'theme':theme,'textPairsChecked':scan['checked'],'issues':scan['contrast']})
                    page.screenshot(path=str(OUT/f'{theme}-error.png'))
                page.evaluate('() => {TradingNativeBridge.getInstruments=qaOriginalQuotes;}')
                retry=page.locator('[data-market-load-error] button');retry.focus();page.keyboard.press('Enter')
                assert not page.locator('[data-market-load-error]').count()
                page.screenshot(path=str(OUT/'recovered-filter.png'))
            page.close()
        browser.close()
    assert any(row['pageErrors'] for row in results if row['variant']=='before'), 'Baseline did not reproduce'
    result={'scope':'Production Java QA and controlled failed reads; see mode and inputs for the exact source, no Android installation',
        'mode':'production' if '--production' in sys.argv else 'proposal','inputs':inputs,
        'sourceHashes':{str(s.relative_to(ROOT)):hashlib.sha256(s.read_bytes()).hexdigest() for s in [PRODUCTION,PROPOSED]},'cases':results,
        'layoutCases':32,'keyboardRetry':True,'searchAndExchangePreserved':True,'contrast':contrast}
    (OUT/'results.json').write_text(json.dumps(result,indent=2),encoding='utf8')
    print('PASS: baseline failure reproduced; proposed retry recovers four failures, debounce, filters, keyboard and 32 layouts')

if __name__=='__main__':
    main()
