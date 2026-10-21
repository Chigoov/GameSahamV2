"""Browser evidence for wired features. Only isolated local Java QA host is permitted."""
from pathlib import Path
import argparse
import json
import re
import hashlib
from urllib.request import urlopen
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / 'Artifacts/Wire-Integration-2026-10-21'
VIEWPORTS = [(320,568),(360,640),(390,844),(412,915),(430,932),(768,1024),(1440,900)]
parser = argparse.ArgumentParser()
parser.add_argument('--inspect', action='store_true')
parser.add_argument('--matrix', action='store_true')
parser.add_argument('--journeys', action='store_true')
parser.add_argument('--skip-company', action='store_true')
parser.add_argument('--flow-matrix', action='store_true')
args = parser.parse_args()
OUT.mkdir(parents=True, exist_ok=True)
meta = json.load(urlopen('http://127.0.0.1:8784/qa-meta'))
assert meta['platform']['testFixture'] is True and meta['platform']['nativeHost'] is False

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page(viewport={'width':390,'height':844}, reduced_motion='reduce')
    page.set_default_timeout(12000)
    errors = []
    dialogs = []
    page.on('pageerror', lambda error: errors.append(str(error)))
    def accept_dialog(dialog):
        dialogs.append({'type':dialog.type,'message':dialog.message})
        dialog.accept()
    page.on('dialog', accept_dialog)
    page.goto('http://127.0.0.1:8784/')
    page.wait_for_load_state('networkidle')
    assert page.evaluate('JSON.parse(TradingNativeBridge.getPlatformInfo()).testFixture')

    def call(method, *values):
        return json.loads(page.evaluate('([method,values])=>TradingNativeBridge[method](...values)', [method,list(values)]))

    def btn(text):
        return page.get_by_role('button', name=re.compile(re.escape(text)))

    def tab(name):
        page.locator('[data-tab='+name+']').click()

    def asset_tool(text):
        tab('aset')
        page.locator('#asset-tools > summary').click()
        btn(text).click()

    journeys = []
    def state(name, marker=''):
        layer = page.locator('.flow-page')
        text = layer.inner_text()
        if marker:
            assert marker in text, (name,text)
        page.screenshot(path=str(OUT/(name+'.png')))
        (OUT/(name+'.txt')).write_text(text,encoding='utf-8')
        assert not errors, errors
        journeys.append({'name':name,'text':text,'buttons':layer.locator('button').all_inner_texts()})

    if args.journeys:
        try:
            assert call('startNewGame',json.dumps({'initialCapital':100000000}))['success']
            call('setClockState',False,1)
            page.reload();page.wait_for_load_state('networkidle')
            # Company creation and versioned review/execute paths through visible controls.
            if not args.skip_company:
                tab('firma');btn('+ Dirikan Perusahaan Baru').click()
                page.locator('#company-name').fill('QA Wiratama')
                btn('Resmikan Pendirian Perusahaan').click()
                page.wait_for_timeout(750)
                assert len(call('getCompanies')['companies'])==1
                btn('Tingkatkan perusahaan').click();state('company-expansion-review')
                btn('Konfirmasi').click();state('company-expansion-receipt')
                btn('Selesai').click()
                btn('Laporan operasional').click();state('company-operating-report')
                tab('firma');btn('Kebijakan').first.click();state('company-policy-form')
                tab('firma');btn('Transfer Modal').first.click();state('company-capital-form')
            tab('firma');page.get_by_role('button',name='Beli Koleksi (Rp 25.000.000)',exact=True).click()
            owned=[item for item in call('getCollections')['collections'] if item.get('isOwned')]
            assert len(owned)==1
            collateral_id=owned[0]['id']
            asset_tool('Utang & Pinjaman Tenor');btn('+ Ajukan Pinjaman Baru').click()
            collateral=page.locator('.flow-page select').filter(has=page.locator('option[value="'+collateral_id+'"]'))
            collateral.select_option(collateral_id)
            state('loan-owned-collateral-form')
            btn('Ajukan & Cairkan Pinjaman').click();state('loan-reviewed-quote','Review Pinjaman')
            btn('Konfirmasi & Cairkan').click();page.wait_for_timeout(750)
            state('loan-disbursed','Pinjaman Pribadi Multiguna')
            assert any(item.get('pledgedLoanId') for item in call('getCollections')['collections'] if item['id']==collateral_id)
            btn('Pelunasan Awal (Prepay)').click();state('loan-prepaid')
            assert not next(item for item in call('getCollections')['collections'] if item['id']==collateral_id).get('pledgedLoanId')
            assert call('saveGame')['success'] and call('loadGame')['success']
            assert not next(item for item in call('getCollections')['collections'] if item['id']==collateral_id).get('pledgedLoanId')
            # Terminal's four independently entitled query kinds.
            tab('riset');btn('Terminal: analis, konsensus & perbandingan').click()
            state('research-terminal-inbox')
            for kind in ['consensus','alternative','comparison']:
                page.locator('#terminal-kind').select_option(kind)
                btn('Tampilkan riset').click()
                state('research-terminal-'+kind)
            tab('riset');btn('Hasil investasi & arus kas').click();state('performance-report')
            # Corporate offers/default/preview denial are reachable with honest entitlements.
            asset_tool('Aksi korporasi & voting RUPS');state('corporate-offers-default','Rights issue')
            btn('Lihat penawaran').first.click();state('corporate-offer-detail')
            if btn('Tinjau partisipasi').is_disabled():
                state('corporate-offer-locked')
            else:
                btn('Tinjau partisipasi').click();state('corporate-offer-ineligible')
            # Local strategy decisions persist; actual account must remain unchanged.
            before=call('getSnapshot')['personalAccount']
            asset_tool('Lab strategi & sandbox what-if');state('lab-default')
            btn('Eksekusi transaksi lab').click();state('lab-trade-result','Transaksi lab tercatat')
            assert call('getSnapshot')['personalAccount']==before
            history=call('strategyLab',json.dumps({'action':'list'}))
            (OUT/'lab-list-after-trade.json').write_text(json.dumps(history,indent=2),encoding='utf-8')
            btn('Putar ulang keputusan').click();state('lab-replay-result','Keputusan diputar ulang')
            assert call('getSnapshot')['personalAccount']==before
            assert call('saveGame')['success']
            assert call('loadGame')['success']
            page.reload();page.wait_for_load_state('networkidle')
            asset_tool('Lab strategi & sandbox what-if');state('lab-after-reload')
            # Starting replay cannot overwrite the internal-mode save slot.
            asset_tool('Replay historis');state('replay-catalog','US:AAPL')
            btn('Mulai replay baru').click();state('replay-consent-required','Centang persetujuan')
            slot=OUT/'qa-save/save_game_v2.json'
            original_hash=hashlib.sha256(slot.read_bytes()).hexdigest()
            original_save=json.loads(slot.read_text(encoding='utf-8'))
            page.locator('#replay-new-confirm').check()
            btn('Mulai replay baru').click()
            assert call('getSnapshot')['gameMode']=='HISTORICAL_REPLAY'
            saved_internal=json.loads(slot.read_text(encoding='utf-8'))
            # Opening replay first saves current internal state; metadata timestamp may change.
            for key in ['accounts','ledger','gameTime','gameDate','gameMode','runtimeState']:
                assert saved_internal[key]==original_save[key], ('internal slot changed',key)
            assert (OUT/'qa-save/save_replay_v2.json').is_file()
            page.reload();page.wait_for_load_state('networkidle')
            asset_tool('Replay historis');btn('Kembali ke permainan utama').click()
            assert call('getSnapshot')['gameMode']=='INTERNAL_MARKET'
            assert call('getSnapshot')['personalAccount']==before
            journeys.append({'name':'replay-slots-isolated','internalSaveHash':original_hash})
        except Exception as error:
            page.screenshot(path=str(OUT/'journey-failure.png'),full_page=True)
            (OUT/'journey-failure-dom.html').write_text(page.content(),encoding='utf-8')
            (OUT/'journey-result.json').write_text(json.dumps({'status':'FAIL','error':str(error),'checks':journeys,'pageerrors':errors},indent=2,ensure_ascii=False),encoding='utf-8')
            raise
        (OUT/'journey-result.json').write_text(json.dumps({'status':'PASS','checks':journeys,'pageerrors':errors,'dialogs':dialogs,'scope':'Named new wiring journeys only; not whole-product acceptance'},indent=2,ensure_ascii=False),encoding='utf-8')

    inventory = []
    for root_tab in ['beranda','pasar','aset','firma','riset']:
        page.locator('[data-tab='+root_tab+']').click()
        inventory.append({'tab':root_tab,'text':page.locator('#view-'+root_tab).inner_text(),
            'buttons':page.locator('#view-'+root_tab+' button').evaluate_all('(els)=>els.map(el=>({text:el.innerText,id:el.id,disabled:el.disabled}))')})
    (OUT/'dom-inventory.json').write_text(json.dumps({'platform':meta,'roots':inventory,'errors':errors}, indent=2,ensure_ascii=False),encoding='utf-8')
    page.screenshot(path=str(OUT/'initial-lainnya.png'), full_page=True)
    if args.flow_matrix:
        def business(text):
            tab('firma');btn(text).first.click()
        def research(text):
            tab('riset');btn(text).first.click()
        def terminal(kind):
            research('Terminal: analis, konsensus & perbandingan')
            page.locator('#terminal-kind').select_option(kind);btn('Tampilkan riset').click()
        def corporate_detail():
            asset_tool('Aksi korporasi & voting RUPS');btn('Lihat penawaran').first.click()
        def loan_form():
            asset_tool('Utang & Pinjaman Tenor');btn('+ Ajukan Pinjaman Baru').click()
        def company_review():
            business('Tingkatkan perusahaan')
        routes = [
            ('company-upgrade-review',company_review),
            ('company-policy-form',lambda:business('Kebijakan & Fasilitas')),
            ('company-capital-form',lambda:business('Transfer Modal')),
            ('company-report',lambda:business('Laporan operasional')),
            ('company-staff',lambda:business('Pegawai & Organisasi')),
            ('terminal-inbox',lambda:terminal('inbox')),
            ('terminal-consensus-locked',lambda:terminal('consensus')),
            ('terminal-comparison-locked',lambda:terminal('comparison')),
            ('terminal-alternative-locked',lambda:terminal('alternative')),
            ('performance',lambda:research('Hasil investasi & arus kas')),
            ('corporate-offers',lambda:asset_tool('Aksi korporasi & voting RUPS')),
            ('corporate-offer-locked',corporate_detail),
            ('strategy-lab-history',lambda:asset_tool('Lab strategi & sandbox what-if')),
            ('replay-catalog',lambda:asset_tool('Replay historis')),
            ('loans-closed',lambda:asset_tool('Utang & Pinjaman Tenor')),
            ('loan-collateral-form',loan_form),
            ('mutual-funds',lambda:asset_tool('Reksa Dana Berbasis NAB')),
            ('profile-preferences',lambda:research('Profil & Gaya')),
            ('league-current',lambda:research('Liga & Rival')),
        ]
        samples=[]
        for width,height in VIEWPORTS:
            page.set_viewport_size({'width':width,'height':height})
            for scale in [100,200]:
                page.evaluate('(scale)=>document.documentElement.style.fontSize=scale+"%"',scale)
                for name,open_route in routes:
                    key=f'flow-{name}-{width}x{height}-text{scale}'
                    failures=[]
                    try:
                        open_route()
                        layer=page.locator('.flow-page')
                        text=layer.inner_text()
                        geometry=layer.evaluate('''el=>({width:innerWidth,height:innerHeight,
                            documentWidth:document.documentElement.scrollWidth,
                            clientWidth:el.clientWidth,scrollWidth:el.scrollWidth,
                            navBottom:document.querySelector('.bottom-nav').getBoundingClientRect().bottom,
                            rootFont:getComputedStyle(document.documentElement).fontSize})''')
                        if geometry['documentWidth']>width+1:failures.append('document horizontal overflow')
                        if geometry['scrollWidth']>geometry['clientWidth']+1:failures.append('flow horizontal overflow')
                        if re.search(r'is not defined|\bundefined\b|Laporan tidak dapat dimuat|Lab belum dapat dimuat',text):failures.append('unexpected unavailable or undefined data')
                        if abs(geometry['navBottom']-height)>1:failures.append('nav outside viewport')
                        layer.evaluate('(el)=>el.scrollTop=el.scrollHeight')
                        page.screenshot(path=str(OUT/(key+'.png')))
                        samples.append({'screen':key,'route':name,'scale':scale,**geometry,'failures':failures})
                    except Exception as error:
                        failures.append(str(error));samples.append({'screen':key,'route':name,'scale':scale,'failures':failures})
        (OUT/'flow-matrix.json').write_text(json.dumps({'samples':samples,'errors':errors,'scope':'19 named wired feature states, not all product states','method':'Actual app root font 100%/200%; geometry and screenshots'},indent=2),encoding='utf-8')
        print(json.dumps({'flowMatrixSamples':len(samples),'failedSamples':sum(bool(s['failures']) for s in samples)}))
    if args.matrix:
        samples = []
        for width,height in VIEWPORTS:
            page.set_viewport_size({'width':width,'height':height})
            for scale in [100,200]:
                page.evaluate('(scale)=>document.documentElement.style.fontSize=scale+"%"',scale)
                for root_tab in ['beranda','pasar','aset','firma','riset']:
                    page.locator('[data-tab='+root_tab+']').click()
                    page.wait_for_timeout(50)
                    geometry = page.evaluate('''()=>({width:innerWidth,height:innerHeight,
                        scrollWidth:document.documentElement.scrollWidth,scrollHeight:document.documentElement.scrollHeight,
                        rootFont:getComputedStyle(document.documentElement).fontSize,
                        navBottom:document.querySelector('.bottom-nav').getBoundingClientRect().bottom,
                        mainHeight:document.querySelector('main').clientHeight,
                        text:document.querySelector('.screen-view.active').innerText.length})''')
                    failures = []
                    if geometry['scrollWidth']>width+1: failures.append('document horizontal overflow')
                    if geometry['scrollHeight']>height+1: failures.append('document vertical overflow')
                    if abs(geometry['navBottom']-height)>1: failures.append('bottom nav outside viewport')
                    if geometry['mainHeight']<60: failures.append('content unavailable')
                    if geometry['text']<20: failures.append('root content empty')
                    nav_before = page.locator('.bottom-nav').bounding_box()
                    page.locator('main').evaluate('(el)=>el.scrollTop=el.scrollHeight')
                    if page.locator('.bottom-nav').bounding_box()!=nav_before: failures.append('bottom nav moves on scroll')
                    name=f'root-{root_tab}-{width}x{height}-text{scale}'
                    page.screenshot(path=str(OUT/(name+'.png')))
                    samples.append({'screen':name,'scale':scale,**geometry,'failures':failures})
        (OUT/'root-matrix.json').write_text(json.dumps({'samples':samples,'errors':errors,'method':'App root font-size 100%/200%, computed size recorded; screenshot itself is not enlarged'},indent=2),encoding='utf-8')
        print(json.dumps({'matrixSamples':len(samples),'failedSamples':sum(bool(s['failures']) for s in samples)}))
    assert not errors, errors
    browser.close()
print(json.dumps({'status':'INSPECTED','rootCount':len(inventory),'apiCount':len(meta['methods'])}))
