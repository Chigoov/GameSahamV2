"""Screenshot audit against an isolated real Java host; never native/click-journey evidence."""
import argparse
import base64
from collections import Counter
import hashlib
import json
from pathlib import Path
import re
import sys
from urllib.request import urlopen

from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parent.parent
VIEWPORTS = [(320, 568), (360, 640), (360, 800), (390, 844), (412, 915), (430, 932), (768, 1024), (1440, 900)]

# ponytail: explicit route expressions preserve the real opener contract; add a route when a screen is added.
ROUTES = [(f"root-{tab}", f"q.switchTab('{tab}')") for tab in ['beranda', 'pasar', 'aset', 'firma', 'riset']]
ROUTES += [(name, f'q.{function}()') for name, function in [
    ('research-terminal', 'openResearchTerminal'), ('performance', 'openPerformanceReport'),
    ('research-tiers', 'openResearchTiersModal'), ('company-found', 'openFoundCompanyModal'),
    ('corporate-actions', 'openCorporateActionModal'), ('ipo', 'openIpoCenter'),
    ('multi-assets', 'openMultiAssets'), ('fund-builder', 'openFundBuilder'),
    ('client-reports', 'openClientReports'), ('stress-test', 'openStressTestModal'),
    ('strategy-lab', 'openStrategyLabModal'), ('historical-replay', 'openHistoricalReplayModal'),
    ('wealth', 'openConsolidatedWealthModal'), ('my-funds', 'openMyMutualFundsModal'),
    ('mutual-funds', 'openMutualFundsMarketModal'), ('loans', 'openLoansModal'),
    ('transactions', 'openTransactionHistoryModal'), ('archetype', 'openInvestorArchetypeModal'),
    ('collections', 'openCollectionsGalleryModal'), ('risk', 'openRiskModal'), ('fx', 'openFxForm'),
    ('entry', 'openEntryPicker'), ('new-game', 'openNewGameModal'), ('tutorial', 'openTutorialModal'),
    ('achievements', 'openAchievementsModal'), ('account-picker', 'openAccountPicker'),
]]
ROUTES += [(f'business-{tab}', f"q.switchTab('firma'); q.loadFirmaView('{tab}')") for tab in ['usaha', 'koleksi', 'dana']]
ROUTES += [(f'company-{tab}', f"q.openCompanyDetailModal(company(),'{tab}')") for tab in ['ringkasan', 'tim', 'fasilitas', 'keuangan']]
ROUTES += [(name, expression) for name, expression in [
    ('company-staff', 'q.openCompanyStaffModal(company())'),
    ('company-policy', 'q.openCompanyPolicyModal(company())'),
    ('company-capital', 'q.openCompanyCapitalModal(company())'),
    ('company-upgrade-review', "q.reviewCompanyAction({companyId:company().id,action:'UPGRADE',workHours:company().workHours||8,benefitTier:company().benefitTier||'STANDARD'})"),
    ('company-staff-review', "const c=company(),s=call('getCompanyStaff',c.id); const candidate=first(s.candidates,'staff candidate'); q.openCompanyStaffReview(c,'HIRE',candidate.id,candidate.role)"),
    ('index', "q.openIndexDetail(first(call('getIndices'),'index'))"),
    ('instrument-idx', "q.openInstrumentDetail(stock('IDX'))"),
    ('instrument-us', "q.openInstrumentDetail(stock('US'))"),
    ('position', "q.openPositionDetailModal(first(call('getPortfolio',snapshot().personalAccount.id).positions,'position').instrumentId)"),
    ('order-ticket-buy', "q.openOrderTicket(stock('IDX'),'BUY')"),
    ('order-ticket-sell', "q.openOrderTicket(stock('IDX'),'SELL')"),
    ('order-review', "const s=stock('IDX'),draft={requestId:'SCREEN-PREVIEW-'+Date.now(),accountId:snapshot().personalAccount.id,instrumentId:s.id,side:'BUY',type:'LIMIT',quantity:s.lotSize,limitPrice:s.currentPriceRaw};const preview=call('previewOrder',JSON.stringify(draft));if(!preview.success)throw Error('NOT_TESTED: '+preview.message);q.showOrderReview(preview,draft)"),
    ('order-status', "const order=first(call('getOrders',snapshot().personalAccount.id,false),'order');document.getElementById('btn-order-history').click();const filter=document.querySelector('.flow-page input[type=checkbox]');filter.checked=false;filter.dispatchEvent(new Event('change'));click('.flow-page button',order.requestId)"),
    ('corporate-offer', "q.openCorporateOfferDetail(first(call('corporateOfferAction',JSON.stringify({action:'list'})).offers,'corporate offer'))"),
    ('multiasset-buy', "q.openMultiAssetOrder(first(call('getMultiAssets').bonds,'bond'),true)"),
    ('multiasset-sell', "q.openMultiAssetOrder(first(call('getMultiAssets').bonds,'bond'),false)"),
    ('fund-subscribe', "q.openSubscribeFundModal(first(call('getMutualFunds').funds,'mutual fund'))"),
    ('fund-redeem', "q.openRedeemFundModal(first(call('getMutualFunds').funds,'mutual fund'))"),
    ('loan-apply-personal', "q.openDisburseLoanModal(first(call('getLoanProducts').products,'loan product').productId)"),
    ('loan-apply-company', "q.openDisburseLoanModal('LOAN-CORP-01',company().id)"),
    ('loan-pay', "q.openPayInstallmentModal(first(call('getLoans').loans,'loan'))"),
    ('fx-review', "const request={accountId:snapshot().personalAccount.id,from:'IDR',to:'USD',amount:'1000000'},preview=call('previewFx',JSON.stringify(request));if(!preview.success)throw Error('NOT_TESTED: '+preview.message);q.showFxReview(preview,request)"),
]]
ROUTES += [(f'profile-{tab.lower()}', f"q.openPlayerProfileModal('{tab}')") for tab in ['KINERJA', 'KEUANGAN', 'PENCAPAIAN']]
ROUTES += [(f'missions-{tab.lower()}', f"q.openPeriodicMissionsModal('{tab}')") for tab in ['DAILY', 'WEEKLY', 'CAREER']]
ROUTES += [(f'league-{mode.lower()}', f"q.openLeagueAndRivalModal('{mode}')") for mode in ['NET_WORTH', 'WEEKLY_RETURN']]
ROUTES += [(f'click-{name}', f"document.getElementById('{element}').click()") for name, element in [
    ('settings', 'btn-settings'), ('briefing', 'btn-briefing'), ('career', 'btn-career'),
    ('reports', 'btn-reports'), ('order-history', 'btn-order-history'),
]]
ROUTES += [(f'terminal-{kind}', f"q.openResearchTerminal();document.getElementById('terminal-kind').value='{kind}';document.getElementById('terminal-kind').dispatchEvent(new Event('change'));Array.from(document.querySelectorAll('.flow-page button')).find(b=>b.textContent.includes('Tampilkan riset')).click()") for kind in ['inbox', 'consensus', 'comparison', 'alternative']]
ROUTES += [(name, expression) for name, expression in [
    ('inline-news-home', "first(Array.from(document.querySelectorAll('#home-news-list article')),'home news card').click()"),
    ('inline-news-research', "q.switchTab('riset');click('#view-riset button','Baca catatan')"),
    ('inline-mandate', "q.switchTab('firma');q.loadFirmaView('dana');const mandate=first(call('getFirmSnapshot').mandates,'mandate');click('#firma-subtab-content button',mandate.clientName)"),
    ('inline-era-requirements', "q.switchTab('firma');const status=call('getPrestigeStatus');const selector=document.getElementById('career-overview')?'#career-overview button':'#view-riset button';if(!document.getElementById('career-overview'))q.switchTab('riset');const labels=status.eligible?['Tinjau era baru','Buka Tinjauan Era Baru']:['Lihat syarat','Lihat Syarat'];const button=first(Array.from(document.querySelectorAll(selector)).filter(b=>labels.some(label=>b.textContent.includes(label))),'prestige requirement control');button.click()"),
    ('inline-daily-report', "const report=first(call('getSessionReports'),'daily report');document.getElementById('btn-reports').click();click('.flow-page button',report.date)"),
    ('inline-mission-review', "const mission=first(snapshot().career.missions.filter(m=>m.completed&&!m.claimed),'completed unclaimed career mission');document.getElementById('btn-career').click();click('.flow-page button',mission.id)"),
    ('inline-stress-result', "q.openStressTestModal();click('.flow-page button','Jalankan simulasi dampak')"),
]]

OPEN = """expression => {
 const q=window.__screenAudit;
 const calls=[];
 const call=(method,...args)=>{const result=JSON.parse(TradingNativeBridge[method](...args));calls.push({method,args});return result;};
 const first=(list,kind)=>{if(!Array.isArray(list)||!list.length)throw Error('NOT_TESTED: no real '+kind+' record in this QA save');const record=list[0];calls.push({selectedRecord:kind,id:record.id||record.instrumentId||record.productId||record.requestId||null});return record;};
 const snapshot=()=>call('getSnapshot');
 const company=()=>first(call('getCompanies').companies,'company');
 const stock=exchange=>first(call('getInstruments','','',null,true).filter(s=>s.exchange===exchange),'instrument '+exchange);
 const click=(selector,text)=>{const button=first(Array.from(document.querySelectorAll(selector)).filter(b=>b.textContent.includes(text)),'interactive control '+text);if(button.disabled)throw Error('NOT_TESTED: control is gated: '+text);button.click();};
 try {q.bridgeCalls.length=0;q.closeOrderTicket();q.closeFlow();q.switchTab('beranda');document.activeElement?.blur();eval(expression);return {calls,bridgeCalls:q.bridgeCalls};}
 catch(error){return {calls,bridgeCalls:q.bridgeCalls,error:String(error)};}
}"""

GEOMETRY = """() => {
 const visible=el=>el.getClientRects().length && !el.closest('[inert]') && getComputedStyle(el).visibility!=='hidden';
 const layer=document.querySelector('#order-modal.active')||document.querySelector('.flow-page')||document.querySelector('.screen-view.active');
 const rootScroller=document.querySelector('.app-scroll')||document.querySelector('main');
 const short=el=>({tag:el.tagName,id:el.id,text:(el.innerText||el.getAttribute('aria-label')||'').trim().slice(0,90)});
 const bounds=el=>{const r=el.getBoundingClientRect();return {...short(el),width:r.width,height:r.height};};
 const controls=Array.from(document.querySelectorAll('button,input,select,textarea,[role="button"],a[href],summary')).filter(visible);
 const touchTarget=el=>el.matches('input[type="checkbox"],input[type="radio"]') ? (el.labels?.[0]||el):el;
 const small=controls.filter(el=>{const r=touchTarget(el).getBoundingClientRect();return r.width<47.5||r.height<47.5;}).map(el=>({...bounds(touchTarget(el)),controlId:el.id}));
 const localScrollers=Array.from((layer||document).querySelectorAll('.table-scroll,#chart-table')).filter(visible).map(el=>({...bounds(el),scrollWidth:el.scrollWidth,clientWidth:el.clientWidth,label:el.getAttribute('aria-label'),tabIndex:el.tabIndex}));
 const overflow=Array.from((layer||document).querySelectorAll('*')).filter(visible).filter(el=>{
 const local=el.closest('.table-scroll,#chart-table');
 if(local&&local!==el&&local.tabIndex>=0&&local.getAttribute('aria-label')&&getComputedStyle(local).overflowX==='auto') { const r=local.getBoundingClientRect();if(r.left>=0&&r.right<=innerWidth+1)return false; }
 const r=el.getBoundingClientRect();return r.left< -1||r.right>innerWidth+1;
 }).slice(0,35).map(bounds);
 return {rootWidth:document.documentElement.scrollWidth,viewportWidth:innerWidth,viewportHeight:innerHeight,
 rootScroller:rootScroller?{clientWidth:rootScroller.clientWidth,scrollWidth:rootScroller.scrollWidth}:null,
 rootFont:getComputedStyle(document.documentElement).fontSize,layer:layer?{...short(layer),clientWidth:layer.clientWidth,scrollWidth:layer.scrollWidth}:null,
 smallTouchTargets:small,overflowElements:overflow,localScrollers,visibleControls:controls.length,
 unnamedFields:controls.filter(el=>el.matches('input,select,textarea')&&!el.getAttribute('aria-label')&&!el.getAttribute('aria-labelledby')&&![...(el.labels||[])].some(label=>label.textContent.trim())).map(short),
 unfocusableRoles:controls.filter(el=>el.getAttribute('role')==='button'&&el.tabIndex<0&&!el.disabled).map(short),
 title:layer?.getAttribute('aria-label')||layer?.querySelector('h1,h2')?.textContent,
 text:layer?.innerText?.slice(0,18000)};
}"""


def instrument(source):
    functions = re.findall(r'function ((?:open\w+|switchTab|loadFirmaView|showOrderReview|showOrderReceipt|showFxReview|reviewCompanyAction|closeFlow|closeOrderTicket))\s*\(', source)
    marker = source.rfind('})();')
    if marker < 0:
        raise ValueError('App IIFE closing marker not found; refuse to alter response')
    exports = '\nwindow.__screenAudit={' + ','.join(dict.fromkeys(functions)) + '};\n'
    exports += "window.__screenAudit.bridgeCalls=[];for(const method of Object.keys(window.TradingNativeBridge)){const original=window.TradingNativeBridge[method];if(typeof original==='function')window.TradingNativeBridge[method]=(...args)=>{window.__screenAudit.bridgeCalls.push({method,args});return original(...args);};}\n"
    return source[:marker] + exports + source[marker:], sorted(set(functions))


def capture_full(page, path):
    # ponytail: expand only the active scroll container for a second, explicitly labelled evidence image.
    saved = page.evaluate("""() => {
      const el=document.querySelector('#order-modal.active .modal-sheet')||document.querySelector('.flow-page')||document.querySelector('.app-scroll')||document.querySelector('main');
      const nodes=[el,document.documentElement,document.body].filter(Boolean);
      const saved=nodes.map(n=>({tag:n.tagName,id:n.id,className:n.className,style:n.getAttribute('style')}));
      window.__captureNodes=nodes;
      if(el){el.style.setProperty('flex','none','important');el.style.setProperty('min-height','0','important');el.style.setProperty('height',el.scrollHeight+'px','important');el.style.setProperty('max-height','none','important');el.style.setProperty('overflow','visible','important');el.style.setProperty('position','relative','important');el.style.setProperty('inset','auto','important');}
      document.documentElement.style.setProperty('height','auto','important');document.documentElement.style.setProperty('overflow','visible','important');
      document.body.style.setProperty('height','auto','important');document.body.style.setProperty('overflow','visible','important');
      return {nodes:saved,height:el?.scrollHeight||0};
    }""")
    paths = []
    cdp = page.context.new_cdp_session(page)
    try:
        for offset in range(0, max(1, saved['height']), 12000):
            if saved['height'] > 12000:
                actual = page.evaluate("""async offset=>{
                  const el=window.__captureNodes[0];for(const [key,value] of Object.entries({height:'12000px','max-height':'12000px','min-height':'0',flex:'none',overflow:'hidden'}))el.style.setProperty(key,value,'important');
                  el.scrollTop=offset;await new Promise(requestAnimationFrame);return {offset:el.scrollTop,clientHeight:el.clientHeight,scrollHeight:el.scrollHeight};
                }""", offset)
                expected = min(offset, actual['scrollHeight'] - actual['clientHeight'])
                if abs(actual['offset'] - expected) > 1:
                    raise AssertionError(f'Expanded capture did not scroll: requested {offset}, actual {actual}')
            tile = path if saved['height'] <= 12000 else path.with_name(path.stem + f'-tile{offset//12000+1}' + path.suffix)
            clip = page.evaluate("""() => {
              const el=window.__captureNodes[0];const r=el.getBoundingClientRect();
              return {x:Math.max(0,r.x+scrollX),y:Math.max(0,r.y+scrollY),width:Math.min(innerWidth,r.width),height:Math.min(12000,r.height),scale:1};
            }""")
            data = cdp.send('Page.captureScreenshot', {'format': 'png', 'captureBeyondViewport': True, 'clip': clip})
            tile.write_bytes(base64.b64decode(data['data']))
            paths.append(tile)
        return paths
    finally:
        page.evaluate("saved=>{window.__captureNodes.forEach((n,i)=>{if(saved.nodes[i].style===null)n.removeAttribute('style');else n.setAttribute('style',saved.nodes[i].style)});delete window.__captureNodes;}", saved)
        cdp.detach()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--url', default='http://127.0.0.1:8791')
    parser.add_argument('--output', required=True, type=Path)
    parser.add_argument('--label', required=True, choices=['before', 'after'])
    parser.add_argument('--self-check', action='store_true')
    parser.add_argument('--prepare-domain', action='store_true', help='Create one company using real domain API, only in the isolated QA save')
    parser.add_argument('--only', help='Optional route-name regex; report remains explicitly partial')
    args = parser.parse_args()
    out = args.output.resolve()
    if not out.is_relative_to(ROOT / 'Artifacts'):
        parser.error('Output must be within this repository Artifacts directory')
    if args.self_check:
        injected, names = instrument('(function(){function openOne(){} function switchTab(){} })();')
        assert names == ['openOne', 'switchTab'] and injected.index('window.__screenAudit') < injected.rindex('})();')
        assert len({name for name, _ in ROUTES}) == len(ROUTES)
        assert len(VIEWPORTS) == 8 and 'openOne' in injected
        print('PASS: IIFE instrumentation, unique route names and required viewport inventory')
        return 0
    meta = json.load(urlopen(args.url.rstrip('/') + '/qa-meta', timeout=10))
    if meta['platform'].get('testFixture') is not True or meta['platform'].get('nativeHost') is not False:
        raise ValueError('Refusing any host that is not the isolated Java QA fixture')
    source = urlopen(args.url.rstrip('/') + '/app.js', timeout=10).read().decode('utf-8')
    injected, functions = instrument(source)
    source_hash = hashlib.sha256(source.encode()).hexdigest()
    out.mkdir(parents=True, exist_ok=True)
    screenshots = out / 'screenshots'
    screenshots.mkdir(exist_ok=True)
    routes = [(name, expression) for name, expression in ROUTES if not args.only or re.search(args.only, name)]
    report = {'label': args.label, 'url': args.url, 'host': meta['platform'], 'appJsSha256': source_hash,
              'runnerSha256': hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),
              'scope': 'Route rendering with real Java state; direct testing-only opener calls do not prove clickable journeys or every gameplay state.',
              'focusCheckScope': 'Up to two Tab transitions per sample (BODY can be the browser document boundary) and focusability of visible button roles; full keyboard journeys require separate checks.',
              'viewports': VIEWPORTS, 'textScales': [100, 200], 'exportedFunctions': functions,
              'routeInventory': [{'name': name, 'expression': expression} for name, expression in ROUTES],
              'uncoveredStates': ['native save-recovery failure', 'every transaction outcome/retry/receipt', 'all lifecycle/offline transitions', 'all unlocked career states'],
              'preparation': [], 'samples': []}
    handled = set(re.findall(r'q\.(\w+)\(', ';'.join(expression for _, expression in ROUTES)))
    report['openersWithoutRoute'] = sorted(name for name in functions if name.startswith('open') and name not in handled)
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(viewport={'width': 390, 'height': 844}, reduced_motion='reduce')
        context.route('**/app.js', lambda route: route.fulfill(status=200, content_type='text/javascript; charset=utf-8', body=injected))
        page = context.new_page()
        page.set_default_timeout(5000)
        errors = []
        page.on('pageerror', lambda error: errors.append(str(error)))
        page.goto(args.url, wait_until='networkidle')
        page.wait_for_function('Boolean(window.__screenAudit)')
        initial = page.evaluate('JSON.parse(TradingNativeBridge.getSnapshot())')
        report['initialSnapshot'] = initial
        if args.prepare_domain:
            preparation = page.evaluate("""() => {
              const c=JSON.parse(TradingNativeBridge.getCompanies());if(c.companies?.length)return {action:'existing company',id:c.companies[0].id};
              const request={id:'COMP-SCREEN-AUDIT',name:'Perusahaan Uji Screenshot',sector:'JASA',capital:'25000000'};
              return {action:'foundCompany',request,result:JSON.parse(TradingNativeBridge.foundCompany(JSON.stringify(request)))};
            }""")
            report['preparation'].append(preparation)
        total = len(routes) * len(VIEWPORTS) * 2
        for width, height in VIEWPORTS:
            page.set_viewport_size({'width': width, 'height': height})
            for scale in [100, 200]:
                for name, expression in routes:
                    key = f'{name}-{width}x{height}-text{scale}'
                    sample = {'key': key, 'route': name, 'viewport': [width, height], 'scale': scale, 'expression': expression}
                    start_errors = len(errors)
                    try:
                        result = page.evaluate(OPEN, expression)
                        sample.update(result)
                        if result.get('error'):
                            sample['status'] = 'NOT_TESTED' if 'NOT_TESTED:' in result['error'] else 'FAIL'
                        else:
                            page.evaluate('(scale)=>document.documentElement.style.fontSize=scale+"%"', scale)
                            page.evaluate("() => {document.querySelectorAll('.app-scroll,main,.flow-page,.modal-body').forEach(el=>el.scrollTop=0)}")
                            page.wait_for_timeout(55)
                            sample['geometry'] = page.evaluate(GEOMETRY)
                            page.screenshot(path=str(screenshots / (key + '-viewport.png')), animations='disabled', timeout=20000)
                            expanded = capture_full(page, screenshots / (key + '-expanded-scroll.png'))
                            page.keyboard.press('Tab')
                            sample['tabTransitions'] = 1
                            if page.evaluate('document.activeElement.tagName') == 'BODY':
                                page.keyboard.press('Tab')
                                sample['tabTransitions'] = 2
                            sample['focus'] = page.evaluate("""() => {const el=document.activeElement,s=getComputedStyle(el);return {tag:el.tagName,id:el.id,text:(el.innerText||el.getAttribute('aria-label')||'').slice(0,90),inert:Boolean(el.closest('[inert]')),outlineStyle:s.outlineStyle,outlineWidth:s.outlineWidth,outlineColor:s.outlineColor,boxShadow:s.boxShadow}}""")
                            failures = []
                            geometry = sample['geometry']
                            if geometry['rootWidth'] > width + 1 or (geometry['rootScroller'] and geometry['rootScroller']['scrollWidth'] > geometry['rootScroller']['clientWidth'] + 1) or (geometry['layer'] and geometry['layer']['scrollWidth'] > geometry['layer']['clientWidth'] + 1) or geometry['overflowElements']:
                                failures.append('horizontal overflow')
                            if geometry['smallTouchTargets']:
                                failures.append('touch targets below 48 CSS px')
                            if geometry['unfocusableRoles']:
                                failures.append('visible button roles are absent from the tab order')
                            if geometry['unnamedFields']:
                                failures.append('form field without an accessible name')
                            focus = sample['focus']
                            if focus['tag'] == 'BODY' or focus['inert']:
                                failures.append('keyboard focus missing or inert')
                            if focus['outlineStyle'] == 'none' and focus['boxShadow'] == 'none':
                                failures.append('focus indicator requires visual review')
                            sample['failures'] = failures
                            sample['screenshots'] = [str(path.relative_to(out)) for path in [screenshots / (key + '-viewport.png'), *expanded]]
                            sample['status'] = 'FAIL' if failures else 'PASS'
                    except Exception as error:
                        sample.update(status='FAIL', error=str(error))
                    sample['pageErrors'] = errors[start_errors:]
                    if sample['pageErrors']:
                        sample['status'] = 'FAIL'
                    report['samples'].append(sample)
                    if len(report['samples']) % 10 == 0:
                        (out / 'screen-audit.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
                        print(f'{args.label}: {len(report["samples"])}/{total}; {dict(Counter(s["status"] for s in report["samples"]))}', flush=True)
        report['finalSnapshot'] = page.evaluate('JSON.parse(TradingNativeBridge.getSnapshot())')
        report['pageErrors'] = errors
        browser.close()
    report['summary'] = dict(Counter(sample['status'] for sample in report['samples']))
    report['screenshots'] = sum(len(sample.get('screenshots', [])) for sample in report['samples'])
    report['visualReview'] = 'NOT_TESTED: reviewer must inspect screenshot files; geometry alone is not a visual gate'
    (out / 'screen-audit.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps({'summary': report['summary'], 'screenshots': report['screenshots'], 'report': str(out / 'screen-audit.json')}))
    return 1 if report['summary'].get('FAIL') or report['summary'].get('NOT_TESTED') else 0


if __name__ == '__main__':
    sys.exit(main())
