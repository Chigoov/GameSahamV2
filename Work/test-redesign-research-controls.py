"""Click research queries and paid tiers after earning real career access."""
import argparse
import json
import os
from decimal import Decimal
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
report = {'checks': [], 'pageErrors': [], 'preparation': [], 'scope': 'Real Java QA research controls; no injected rank, balances, opinions or entitlement.'}

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page(viewport={'width':390, 'height':844}, reduced_motion='reduce')
    page.set_default_timeout(12000)
    page.on('pageerror', lambda error: report['pageErrors'].append(str(error)))
    def dialog(d):
        report.setdefault('dialogs', []).append({'type':d.type, 'message':d.message})
        if report.pop('cancelNextDialog', False):
            d.dismiss()
        else:
            d.accept()
    page.on('dialog', dialog)
    report['platform'] = page.request.get(args.url+'/qa-meta').json()['platform']
    assert report['platform']['testFixture'] and not report['platform']['nativeHost']
    page.goto(args.url); page.wait_for_load_state('networkidle')

    def call(method, *values):
        return json.loads(page.evaluate('([m,a])=>TradingNativeBridge[m](...a)', [method, list(values)]))

    def record(name, observed, response=None):
        assert not report['pageErrors'], report['pageErrors']
        page.screenshot(path=str(out/(name+'.png')))
        report['checks'].append({'name':name, 'observed':observed, 'response':response, 'screenshot':name+'.png', 'visible':page.locator('.flow-page').inner_text()})
        report['bridgeCalls'] = page.evaluate('researchCalls')
        (out/'research-controls.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')

    def btn(text):
        return page.get_by_role('button', name=text, exact=True)

    def usd_cash():
        return Decimal(call('getSnapshot')['personalAccount']['cash']['USD']['total'].replace('$', '').replace(',', ''))

    def terminal():
        if page.locator('.flow-page').count():
            page.locator('.back-button').click()
        page.locator('.nav-item[data-tab=pasar]').click()
        page.locator('#btn-market-research').click()
        btn('Terminal: analis, konsensus & perbandingan').click()
        assert page.locator('#terminal-kind').is_visible()

    def reopen_risk():
        page.locator('.back-button').click()
        btn('Fasilitas margin & risiko').click()

    def uncertain_fault(method, mode):
        # Fault only the acknowledgement after a real successful mutation; never alter game state or results.
        page.evaluate('''([method,mode])=>{window.faultOriginal=TradingNativeBridge[method];window.faultInvocations=0;TradingNativeBridge[method]=(...args)=>{faultInvocations++;const result=faultOriginal(...args);if(!JSON.parse(result).success)throw Error('Fault setup mutation rejected: '+result);if(mode==='transport')throw Error('QA acknowledgement transport lost');return JSON.stringify({success:false,message:'Penyimpanan gagal; ulangi permintaan dengan ID yang sama'});};}''', [method, mode])
        if method == 'openShortPosition':
            page.locator('#short-instrument').select_option('IDX:BBCA')
            page.locator('#short-shares').fill('100')
            control_names = ['Buka posisi short', 'Beli kembali saham short']
        else:
            page.locator('#margin-amt-input').fill('1000000')
            control_names = ['Tarik pinjaman margin', 'Lunasi utang margin']
        btn(control_names[0]).click()
        assert all(btn(name).is_disabled() for name in control_names)
        assert page.evaluate('faultInvocations') == 1
        status = page.locator('.flow-page [role=status]').last.inner_text() if method == 'openShortPosition' else page.locator('#margin-error').inner_text()
        assert 'belum terkonfirmasi' in status and 'Buka kembali layar risiko' in status
        assert 'ulangi permintaan dengan ID yang sama' not in status
        btn(control_names[0]).evaluate('el=>el.click()')
        assert page.evaluate('faultInvocations') == 1
        page.evaluate('method=>{TradingNativeBridge[method]=faultOriginal}', method)
        actual = call('getRiskSnapshot')
        if method == 'openShortPosition':
            assert actual['shortPositions'][0]['shares'] == 100
        else:
            assert actual['marginDebtIdr'] == 'Rp 1.000.000'
        record('uncertain-'+method+'-'+mode, 'Real mutation applied but acknowledgement faulted; both actions disabled, no blind retry, actual state retained for inspection', actual)
        reopen_risk()
        if method == 'openShortPosition':
            page.locator('#short-instrument').select_option('IDX:BBCA')
            page.locator('#short-shares').fill('100')
        else:
            page.locator('#margin-amt-input').fill('1000000')
        btn(control_names[1]).click()
        actual = call('getRiskSnapshot')
        assert not actual['shortPositions'] and actual['marginDebtIdr'] == 'Rp 0'

    def query(kind):
        page.locator('#terminal-kind').select_option(kind)
        if kind == 'comparison':
            page.locator('#terminal-instruments').select_option(['IDX:BBCA', 'IDX:TLKM'])
        elif kind != 'inbox':
            page.locator('#terminal-instruments').select_option('IDX:BBCA')
        previous = len(page.evaluate('researchCalls'))
        btn('Tampilkan riset').click()
        calls = page.evaluate('researchCalls')
        assert len(calls) == previous+1 and calls[-1]['method'] == 'researchQuery', calls[-1:]
        assert json.loads(calls[-1]['args'][0])['kind'] == kind
        response = calls[-1]['response']
        assert response['success'], response
        visible = page.locator('.flow-page').inner_text()
        assert 'Waktu data' in visible and 'LOCKED' not in visible
        if kind == 'inbox':
            assert response['messages'] and response['messages'][0]['thesis'] in visible
        elif kind == 'consensus':
            assert response['instrumentId'] == 'IDX:BBCA' and response['sampleCount'] > 0
            assert 'Target rata-rata' in visible and str(response['meanTarget']) in visible
        elif kind == 'comparison':
            assert [i['instrumentId'] for i in response['instruments']] == ['IDX:BBCA', 'IDX:TLKM']
            assert all(i['name'] in visible and i['instrumentId'] in visible for i in response['instruments'])
        else:
            assert response['instrumentId'] == 'IDX:BBCA' and response['observationWindowDays'] == 7
            assert 'Sentimen berita' in visible and response['uncertainty'] in visible
        record('query-'+kind, 'Selector and submit clicked; exact Java result and source text rendered', response)

    def subscribe(tier):
        tiers = call('getResearchTiers')
        offering = next(t for t in tiers if t['name'] == tier)
        before = usd_cash()
        btn('Lihat paket terminal').click()
        card = page.locator('article[data-tier="'+tier+'"]')
        assert card.get_by_role('button').is_enabled()
        card.get_by_role('button').click()
        calls = page.evaluate('researchCalls')
        receipt = next(c for c in reversed(calls) if c['method'] == 'subscribeResearchTier')
        assert receipt['args'] == [tier] and receipt['response']['success'], receipt
        active = next(t for t in call('getResearchTiers') if t['isCurrent'])
        assert active['name'] == tier
        after = usd_cash()
        assert before-after == Decimal(offering['feeUsdRaw']), (before, after, offering)
        assert 'Sedang aktif saat ini' in page.locator('article[data-tier="'+tier+'"]').inner_text()
        record('subscribe-'+tier.lower(), 'Real career-unlocked button paid exact USD game fee; active package and account verified', {'receipt':receipt, 'fee':offering['feeUsdRaw'], 'cashBefore':str(before), 'cashAfter':str(after)})
        terminal()

    try:
        assert call('startNewGame', json.dumps({'initialCapital':100000000}))['success']
        clock = call('getSnapshot')['clock']
        page.evaluate('''c=>{const d=new Date(c.timestamp+7*3600000),s=d.getUTCHours()*3600+d.getUTCMinutes()*60+d.getUTCSeconds();TradingNativeBridge.setClockState(true,1);TradingNativeBridge.tickClock(Math.ceil(((34200-s+86400)%86400)/c.rate));TradingNativeBridge.setClockState(false,1)}''', clock)
        for ticker in ['BBCA', 'TLKM']:
            result = call('submitOrder', json.dumps({'requestId':'RESEARCH-EARN-'+ticker, 'instrumentId':'IDX:'+ticker, 'side':'BUY', 'type':'MARKET', 'quantity':100}))
            assert result['status'] == 'FILLED', result
            report['preparation'].append({'action':'Actual market fill', 'result':result})
        for mission in ['MIS-01', 'MIS-02', 'MIS-03']:
            reward = call('claimMission', mission)
            assert reward['success'], reward
            report['preparation'].append({'action':'Claim earned '+mission, 'result':reward})
        for day in range(10):
            if call('getSnapshot')['career']['rank'] == 'SENIOR_ANALYST':
                break
            result = call('skipDay', 'RESEARCH-RANK-'+str(day))
            assert result['success'], result
            report['preparation'].append({'action':'Skip actual day', 'result':result})
        earned = call('getSnapshot')
        assert earned['career']['rank'] == 'SENIOR_ANALYST', earned['career']
        assert earned['featureAccess']['terminal']['unlocked']
        call('setClockState', False, 1)
        report['earnedSnapshot'] = earned
        page.reload(); page.wait_for_load_state('networkidle')
        page.evaluate('''()=>{window.researchCalls=[];for(const method of ['researchQuery','subscribeResearchTier','confirmFx','openShortPosition','closeShortPosition']){const original=TradingNativeBridge[method];TradingNativeBridge[method]=(...args)=>{const result=original(...args);researchCalls.push({method,args,response:JSON.parse(result)});return result;};}}''')
        page.locator('.nav-item[data-tab=aset]').click()
        btn('Tukar IDR / USD').click()
        page.locator('#fx-amount').fill('45000000')
        btn('Tinjau konversi').click()
        btn('Konfirmasi konversi').click()
        fx = next(c for c in page.evaluate('researchCalls') if c['method'] == 'confirmFx')
        assert fx['response']['success'], fx
        assert usd_cash() >= 2500
        record('fx-funded-by-existing-cash', 'IDR from actual run exchanged via UI review and confirmation; no deposit or cash injection', fx)
        terminal(); query('inbox')
        subscribe('PRO'); query('consensus'); query('comparison')
        subscribe('ENTERPRISE'); query('alternative')
        for day in range(45):
            if call('getSnapshot')['career']['rank'] == 'PORTFOLIO_MANAGER':
                break
            result = call('skipDay', 'RESEARCH-SHORT-RANK-'+str(day))
            assert result['success'], result
            report['preparation'].append({'action':'Earn short-access career day', 'result':result})
        assert call('getSnapshot')['career']['rank'] == 'PORTFOLIO_MANAGER'
        call('setClockState', False, 1)
        page.locator('.back-button').click()
        page.locator('.nav-item[data-tab=aset]').click()
        page.locator('#asset-tools > summary').click()
        btn('Fasilitas margin & risiko').click()
        risk_before = call('getRiskSnapshot')
        assert not risk_before['shortPositions']
        cash_before_short = call('getSnapshot')['personalAccount']['cash']['IDR']['total']
        page.locator('#short-instrument').select_option('IDX:BBCA')
        page.locator('#short-shares').fill('0')
        calls_before = len(page.evaluate('researchCalls'))
        btn('Buka posisi short').click()
        assert 'Pilih saham dan isi jumlah saham bulat yang positif.' in page.locator('.flow-page').inner_text()
        assert len(page.evaluate('researchCalls')) == calls_before
        page.locator('#short-shares').fill('500')
        report['cancelNextDialog'] = True
        btn('Buka posisi short').click()
        assert not call('getRiskSnapshot')['shortPositions']
        assert len(page.evaluate('researchCalls')) == calls_before
        record('short-validation-cancel', 'Zero shares rejected before bridge; cancelled native confirmation creates no obligation')
        btn('Buka posisi short').click()
        risk_open = call('getRiskSnapshot')
        assert len(risk_open['shortPositions']) == 1, risk_open
        position = risk_open['shortPositions'][0]
        assert position['ticker'] == 'BBCA' and position['shares'] == 500, position
        assert 'Saham wajib dikembalikan' in page.locator('.flow-page').inner_text()
        record('short-open', 'Actual PM access earned; native confirmation accepted; Java lender inventory and collateral recorded', risk_open)
        page.locator('#short-instrument').select_option('IDX:BBCA')
        page.locator('#short-shares').fill('501')
        btn('Beli kembali saham short').click()
        denied = page.evaluate('researchCalls')[-1]
        assert denied['method'] == 'closeShortPosition' and not denied['response']['success'], denied
        assert call('getRiskSnapshot')['shortPositions'] == risk_open['shortPositions']
        record('short-overcover-denied', 'Cover501 with500 borrowed rejected by actual Java; quantity/collateral unchanged', denied)
        page.locator('#short-shares').fill('200')
        btn('Beli kembali saham short').click()
        risk_partial = call('getRiskSnapshot')
        assert risk_partial['shortPositions'][0]['shares'] == 300
        assert risk_partial['shortPositions'][0]['collateral'] != position['collateral']
        record('short-partial-cover', 'Cover200 reduces real obligation500→300 and releases proportional collateral', risk_partial)
        page.set_viewport_size({'width':320, 'height':568})
        page.evaluate('document.documentElement.style.fontSize="200%"')
        short_row = page.locator('.flow-page .data-row').filter(has=page.get_by_role('heading', name='BBCA · IDX', exact=True))
        short_row.scroll_into_view_if_needed()
        geometry = page.evaluate('''()=>({viewport:innerWidth,root:document.documentElement.scrollWidth,flow:document.querySelector('.flow-content').scrollWidth})''')
        assert geometry['root'] <= geometry['viewport'] and geometry['flow'] <= geometry['viewport'], geometry
        page.screenshot(path=str(out/'short-partial-text200.png'))
        toast = page.locator('.game-toast')
        assert toast.count() == 1
        toast_box = toast.bounding_box()
        assert toast_box['height'] <= 568*.4+1 and toast_box['width'] <= 320, toast_box
        btn('Tutup pemberitahuan').click()
        assert toast.count() == 0
        report['toastText200'] = {'countBeforeDismiss':1, 'box':toast_box, 'viewport':[320,568], 'dismissedByNamedButton':True}
        short_row.evaluate('el=>el.scrollIntoView({block:"start"})')
        page.screenshot(path=str(out/'short-partial-row-start-text200.png'))
        short_row.locator('.data-row').last.evaluate('el=>el.scrollIntoView({block:"start"})')
        page.screenshot(path=str(out/'short-partial-row-end-text200.png'))
        row_geometry = short_row.evaluate('el=>[el,...el.querySelectorAll("*")].map(e=>({tag:e.tagName,width:e.clientWidth,scroll:e.scrollWidth})).filter(e=>e.scroll>e.width+1)')
        assert not row_geometry, row_geometry
        report['shortText200'] = {'viewport':[320,568], 'fontPercent':200, 'geometry':geometry, 'rowOverflow':row_geometry, 'rowCapture':'Two real viewport positions after clicking toast dismiss; immediate screenshot retains actual overlay', 'screenshots':['short-partial-text200.png','short-partial-row-start-text200.png','short-partial-row-end-text200.png']}
        page.evaluate('document.documentElement.style.fontSize="100%"')
        page.set_viewport_size({'width':390, 'height':844})
        page.locator('#short-instrument').select_option('IDX:BBCA')
        page.locator('#short-shares').fill('300')
        btn('Beli kembali saham short').click()
        risk_closed = call('getRiskSnapshot')
        assert not risk_closed['shortPositions'], risk_closed
        assert 'Tidak ada posisi short' in page.locator('.flow-page').inner_text()
        assert call('getSnapshot')['personalAccount']['cash']['IDR']['reserved'] == 'Rp 0'
        assert call('getSnapshot')['personalAccount']['cash']['IDR']['total'] != cash_before_short
        record('short-cover', 'Remaining300 shares covered through UI; actual fees charged; no remaining short or collateral', risk_closed)
        uncertain_fault('openShortPosition', 'save')
        uncertain_fault('openShortPosition', 'transport')
        uncertain_fault('takeMarginLoan', 'save')
        snapshot = call('getSnapshot')
        ledger = call('getLedger', snapshot['personalAccount']['id'])
        subscriptions = [entry for entry in ledger if entry['type'] == 'TRANSACTION_FEE' and entry['description'].startswith('Biaya Langganan ')]
        assert len(subscriptions) == 2, subscriptions
        for prefix in ['Hasil penjualan short BBCA 500', 'Fee bursa penjualan short BBCA', 'Buy to cover saham pinjaman BBCA 200', 'Buy to cover saham pinjaman BBCA 300', 'Fee pembelian penutupan short BBCA']:
            assert any(entry['description'].startswith(prefix) for entry in ledger), prefix
        report['ledger'] = ledger
        report['finalSnapshot'] = snapshot
        page.locator('.back-button').click()
        for action in ['Beli obligasi','Jual obligasi','Beli emas','Jual emas']:
            page.locator('.nav-item[data-tab=pasar]').click(); page.locator('#btn-multiasset').click()
            btn(action).first.click()
            page.locator('#multiasset-quantity').fill('1')
            btn('Tinjau transaksi').click()
            assert btn('Konfirmasi').count(), page.locator('.flow-page').inner_text()
            btn('Konfirmasi').click()
            assert 'Tindakan selesai dan tersimpan.' in page.locator('.flow-page').inner_text()
            record('multiasset-'+action.replace(' ','-'), 'Real career-unlocked trade reviewed and confirmed; no injected holdings')
            btn('Selesai').click()
        for mature in [False,True]:
            page.locator('.nav-item[data-tab=pasar]').click(); page.locator('#btn-multiasset').click()
            btn('Buka Deposito').first.click()
            deposit=call('getActiveTimeDeposits')[0]
            page.locator('.back-button').click()
            if mature:
                for day in range(deposit['tenorDays']):
                    assert call('skipDay','DEPOSIT-UI-'+str(day))['success']
            page.locator('.nav-item[data-tab=pasar]').click(); page.locator('#btn-multiasset').click()
            btn('Cairkan Pokok & Bunga' if mature else 'Tarik Dini (Penalti Bunga 50%)').first.click()
            assert not call('getActiveTimeDeposits')
            record('deposit-'+str(mature), 'Actual placement and withdrawal through UI; maturity reached through real game days', deposit)
            page.locator('.back-button').click()
        for day in range(25):
            status = call('getPrestigeStatus')
            if status['eligible']:
                break
            result = call('skipDay', 'RESEARCH-ERA-'+str(day))
            assert result['success'], result
        status = call('getPrestigeStatus')
        assert status['eligible'], status
        page.locator('.nav-item[data-tab=firma]').click()
        btn('Tinjau era baru').click()
        assert 'Yang direset' in page.locator('.flow-page').inner_text()
        record('era-review', 'Eligibility earned through real days and career; reset and retained data displayed', status)
        btn('Konfirmasi & Mulai Era Baru').click()
        assert call('getSnapshot')['runId'] != status['runId']
        assert not call('getRiskSnapshot')['shortPositions']
        assert not call('getPrestigeStatus')['eligible']
        report['checks'].append({'name':'era-confirm', 'observed':'Actual confirmation changed run ID; new career locked; previous obligation settled before reset'})
        report['bridgeCalls'] = page.evaluate('researchCalls')
        page.screenshot(path=str(out/'era-confirm.png'))
        assert not report['pageErrors'], report['pageErrors']
        report['status'] = 'PASS'
    except Exception as error:
        report['status'] = 'FAIL'; report['error'] = str(error)
        page.screenshot(path=str(out/'failure.png'))
        raise
    finally:
        (out/'research-controls.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
        browser.close()
print(json.dumps({'status':report['status'], 'checks':len(report['checks'])}))
