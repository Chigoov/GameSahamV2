"""Click selected missing financial/company controls; isolated Java QA only."""
import argparse
import json
import os
import re
from pathlib import Path
from urllib.parse import urlparse
from playwright.sync_api import sync_playwright

parser=argparse.ArgumentParser()
parser.add_argument('--url',default='http://127.0.0.1:'+os.environ.get('QA_PORT','8897'))
parser.add_argument('--output-dir',required=True)
args=parser.parse_args()
assert urlparse(args.url).hostname in ['localhost','127.0.0.1']
out=Path(args.output_dir).resolve()
assert out.is_relative_to(Path(__file__).resolve().parent.parent)
out.mkdir(parents=True,exist_ok=True)
report={'checks':[],'pageErrors':[],'clicks':[],'scope':'Named unique-control gaps only; repeated records share helpers. No whole-product or Android certification.'}

with sync_playwright() as p:
    browser=p.chromium.launch(headless=True)
    page=browser.new_page(viewport={'width':390,'height':844},reduced_motion='reduce')
    page.set_default_timeout(10000)
    page.on('pageerror',lambda error:report['pageErrors'].append(str(error)))
    dialogs=[]
    def dialog(d):
        dialogs.append({'type':d.type,'message':d.message})
        d.accept('7' if d.type=='prompt' else None)
    page.on('dialog',dialog)
    meta=page.request.get(args.url+'/qa-meta').json()['platform']
    assert meta['testFixture'] and not meta['nativeHost']
    report['platform']=meta
    page.goto(args.url);page.wait_for_load_state('networkidle')
    def call(method,*values):
        return json.loads(page.evaluate('([m,a])=>TradingNativeBridge[m](...a)',[method,list(values)]))
    assert call('startNewGame',json.dumps({'initialCapital':100000000}))['success']
    call('setClockState',False,1)
    page.reload();page.wait_for_load_state('networkidle')
    page.evaluate('''()=>{window.controlClicks=[];document.addEventListener('click',e=>{const el=e.target.closest('button,summary');if(el)controlClicks.push({text:el.innerText,id:el.id,route:el.closest('.flow-page')?.getAttribute('aria-label')||document.querySelector('.screen-view.active')?.id});},true)}''')
    def btn(text):return page.get_by_role('button',name=text,exact=True)
    def flow():return page.locator('.flow-page')
    def tab(name):
        if page.locator('.flow-page.isolated').count():btn('Kembali').click()
        page.locator('.nav-item[data-tab='+name+']').click()
    def state(name,detail):
        assert not report['pageErrors'],report['pageErrors']
        screenshot=name+'.png';page.screenshot(path=str(out/screenshot))
        report['checks'].append({'name':name,'observed':detail,'screenshot':screenshot,'visible':flow().inner_text() if flow().count() else page.locator('.screen-view.active').inner_text()})
        report['clicks']=prior_clicks+page.evaluate('controlClicks');report['dialogs']=dialogs
        (out/'controls.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
    def company():return call('getCompanies')['companies'][0]
    def manage(subtab):
        tab('firma');page.locator('#firma-subtab-usaha').click()
        btn('Kelola usaha & operasional').first.click()
        page.locator('.company-local-nav').get_by_role('button',name=subtab,exact=True).click()
    def asset(text):
        tab('aset');page.locator('#asset-tools > summary').click();btn(text).click()
    def confirm():
        btn('Konfirmasi').click()
        assert 'Tindakan selesai dan tersimpan.' in flow().inner_text()
    prior_clicks=[]
    try:
        tab('firma');btn('+ Dirikan Perusahaan Baru').click()
        page.locator('#company-name').fill('QA Kontrol Usaha')
        page.locator('#company-capital').fill('30000000')
        btn('Resmikan Pendirian Perusahaan').click()
        page.wait_for_timeout(750)
        assert len(call('getCompanies')['companies'])==1
        manage('Fasilitas & moral');btn('Ubah kebijakan & fasilitas kerja').click()
        flow().locator('select').nth(0).select_option('6')
        flow().locator('select').nth(1).select_option('PREMIUM')
        btn('Terapkan Kebijakan').click();confirm()
        assert company()['workHours']==6 and company()['benefitTier']=='PREMIUM'
        state('company-policy','Both policy selects → review → confirmation; actual company workHours=6/PREMIUM')
        btn('Selesai').click()
        for direction,amount in [('TO_COMPANY',1000000),('TO_PERSONAL',1000000)]:
            manage('Keuangan');btn('Suntik modal / tarik dividen').click()
            flow().locator('select').select_option(direction)
            flow().locator('input').fill(str(amount))
            before=company()['cash']
            btn('Eksekusi Transfer Modal').click();confirm()
            assert float(company()['cash'])==float(before)+(amount if direction=='TO_COMPANY' else -amount)
            state('capital-'+direction,'Review and execute real transfer; company cash delta agrees with selected direction')
            btn('Selesai').click()
        manage('Tim & SDM');btn('Rekrut pegawai & eksekutif').click()
        before=call('getCompanyStaff',company()['id'])
        btn('Tinjau perekrutan').first.click();btn('Konfirmasi perubahan pegawai').click()
        assert 'Perubahan tersimpan.' in flow().inner_text()
        after=call('getCompanyStaff',company()['id'])
        assert len(after['employees'])==len(before['employees'])+1
        state('staff-hire','Actual staff count increased after versioned confirmation')
        btn('Kembali ke pegawai').click()
        page.get_by_role('button',name=re.compile('^Tempatkan:')).first.click();btn('Konfirmasi perubahan pegawai').click()
        assert 'Perubahan tersimpan.' in flow().inner_text()
        state('staff-assign','Available role selected and versioned assignment receipt recorded')
        btn('Kembali ke pegawai').click()
        btn('Latih pegawai').last.click();btn('Konfirmasi perubahan pegawai').click()
        assert 'Perubahan tersimpan.' in flow().inner_text()
        state('staff-train','Actual training review and receipt; no rank or money injection')
        btn('Kembali ke pegawai').click()
        btn('Berhentikan pegawai').first.click();btn('Konfirmasi perubahan pegawai').click()
        assert 'Perubahan tersimpan.' in flow().inner_text()
        assert len(call('getCompanyStaff',company()['id'])['employees'])==len(before['employees'])
        state('staff-dismiss','Dismiss review and receipt restored original employee count')

        tab('pasar');btn('Reksa Dana').click()
        first=call('getMutualFunds')['funds'][0]
        btn('Beli Unit (Subscribe)').first.click();flow().locator('input').fill('1000')
        btn('Konfirmasi Pembelian Unit').click();assert 'Minimal pembelian' in flow().inner_text()
        flow().locator('input').fill('1000000');btn('Konfirmasi Pembelian Unit').click()
        fund=next(f for f in call('getMutualFunds')['funds'] if f['id']==first['id'])
        assert any(o['side']=='SUBSCRIBE' for o in fund['orders'])
        state('fund-subscribe','Minimum rejected visibly; valid amount created real queued subscription')
        btn('Lihat antrean reksa dana').click();btn('Batalkan permintaan').first.click()
        fund=next(f for f in call('getMutualFunds')['funds'] if f['id']==first['id'])
        assert any(o['status']=='CANCELLED' for o in fund['orders'])
        btn('Cairkan Unit (Redeem)').first.click();flow().locator('input').fill('0');btn('Konfirmasi Pencairan Unit').click()
        assert 'Jumlah unit harus lebih dari 0' in flow().inner_text()
        state('fund-cancel-redeem-validation','Queued subscription cancelled; invalid redemption rejected visibly')

        tab('pasar');btn('IPO').click();card=flow().locator('section.card').filter(has=btn('Pesan saham IPO')).first
        card.locator('input').fill('0');card.get_by_role('button',name='Pesan saham IPO',exact=True).click()
        assert 'Isi jumlah lot positif.' in card.inner_text()
        card.locator('input').fill('1');card.get_by_role('button',name='Pesan saham IPO',exact=True).click()
        assert 'Pemesanan berhasil' in card.inner_text() or 'Pemesanan gagal:' in card.inner_text()
        state('ipo-order','Real available offering: invalid quantity rejected; valid submission result recorded explicitly')

        asset('Utang & Pinjaman Tenor');btn('+ Ajukan Pinjaman Baru').click()
        flow().locator('input[type=number]').fill('1000000');btn('Ajukan & Cairkan Pinjaman').click()
        assert 'Pilih agunan koleksi' in flow().inner_text()
        state('loan-missing-collateral','Personal loan without owned collateral rejected by visible validation')
        assert call('skipDay','CONTROL-OPERATING-DAY')['success']
        manage('Keuangan');btn('Ajukan pinjaman modal kerja').click()
        flow().locator('input[type=number]').fill('5000000');btn('Ajukan & Cairkan Pinjaman').click()
        assert 'Review Pinjaman' in flow().inner_text(),flow().inner_text()
        btn('Konfirmasi & Cairkan').click();page.wait_for_timeout(750)
        assert any(l['productId']=='LOAN-CORP-01' for l in call('getLoans')['loans'])
        state('loan-corporate-disburse','Actual company selection, amount, quote and versioned disbursement produced loan')
        btn('Restrukturisasi Tenor').first.click()
        assert call('getLoans')['loans'][0]['remainingDays']>14
        state('loan-restructure','Prompt submitted7days; remaining term increased in domain')
        principal_before=int(re.sub(r'\D','',call('getLoans')['loans'][0]['remainingPrincipal']))
        btn('Bayar Cicilan').first.click()
        flow().locator('input').fill('1000000');btn('Setor Pembayaran Cicilan').click();page.wait_for_timeout(750)
        assert int(re.sub(r'\D','',call('getLoans')['loans'][0]['remainingPrincipal']))==principal_before-1000000
        state('loan-installment','Actual active company loan installment submitted; outstanding principal reduced')

        tab('riset');btn('Terminal: analis, konsensus & perbandingan').click()
        for kind in ['inbox','consensus','comparison','alternative']:
            page.locator('#terminal-kind').select_option(kind)
            if kind=='comparison':page.locator('#terminal-instruments').select_option(['IDX:BBCA','IDX:TLKM'])
            btn('Tampilkan riset').click()
            assert len(flow().inner_text())>80
            state('terminal-'+kind,'Type selection and submit clicked; actual entitlement/data result recorded')
        btn('Lihat paket terminal').click()
        state('terminal-packages','Package destination from terminal is reachable; entitlement remains real')
        asset('Aksi korporasi & voting RUPS');btn('Lihat penawaran').first.click()
        control=btn('Tinjau partisipasi')
        if control.is_enabled():
            page.locator('#corporate-shares').fill('1');control.click()
            state('corporate-preview','Preview clicked using actual entitlement; actual quote or rejection recorded')
        else:state('corporate-locked','Disabled participation and actual domain reason recorded; confirmation NOT_TESTED')

        # A second genuine run earns access through fills, mission rewards and calendar days.
        assert call('startNewGame',json.dumps({'initialCapital':100000000}))['success']
        clock=call('getSnapshot')['clock']
        page.evaluate('''c=>{const d=new Date(c.timestamp+7*3600000),s=d.getUTCHours()*3600+d.getUTCMinutes()*60+d.getUTCSeconds();TradingNativeBridge.setClockState(true,1);TradingNativeBridge.tickClock(Math.ceil(((34200-s+86400)%86400)/c.rate));TradingNativeBridge.setClockState(false,1)}''',clock)
        for ticker in ['BBCA','TLKM']:
            result=call('submitOrder',json.dumps({'requestId':'CONTROL-EARN-'+ticker,'instrumentId':'IDX:'+ticker,'side':'BUY','type':'MARKET','quantity':100}))
            assert result['status']=='FILLED',result
        for mission in ['MIS-01','MIS-02','MIS-03']:assert call('claimMission',mission)['success']
        for day in range(45):
            if call('getSnapshot')['career']['rank']=='PORTFOLIO_MANAGER':break
            assert call('skipDay','CONTROL-RANK-'+str(day))['success']
        assert call('getSnapshot')['career']['rank']=='PORTFOLIO_MANAGER'
        prior_clicks=page.evaluate('controlClicks')
        page.reload();page.wait_for_load_state('networkidle')
        page.evaluate('''()=>{window.controlClicks=[];document.addEventListener('click',e=>{const el=e.target.closest('button,summary');if(el)controlClicks.push({text:el.innerText,id:el.id,route:el.closest('.flow-page')?.getAttribute('aria-label')||document.querySelector('.screen-view.active')?.id});},true)}''')
        tab('firma');page.locator('#firma-subtab-dana').click();btn('Fund Builder & alokasi portofolio').click()
        page.locator('#fund-plan-name').fill('QA Rencana Kontrol')
        page.locator('#fund-weight-bbca').fill('50');btn('Simpan rencana alokasi').click()
        assert 'Total bobot harus tepat 100%' in flow().inner_text()
        page.locator('#fund-weight-bbca').fill('40');btn('Simpan rencana alokasi').click()
        assert any(plan['name']=='QA Rencana Kontrol' for plan in call('getFundPlans'))
        btn('Tinjau & eksekusi rebalancing').first.click()
        assert 'Status rencana:' in flow().inner_text()
        state('fund-plan-rebalance','Invalid weights rejected; valid target saved and rebalance executed with domain status')
        asset('Fasilitas margin & risiko');page.locator('#margin-amt-input').fill('1000000');btn('Tarik pinjaman margin').click()
        assert call('getRiskSnapshot')['marginDebtIdr']!='Rp 0'
        page.locator('#margin-amt-input').fill('1000000');btn('Lunasi utang margin').click()
        assert call('getRiskSnapshot')['marginDebtIdr']=='Rp 0'
        state('margin-borrow-repay','Unlocked real margin loan and repayment; debt returned to zero')
        assert not report['pageErrors'],report['pageErrors']
        report['status']='PASS'
    except Exception as error:
        report['status']='FAIL';report['error']=str(error)
        page.screenshot(path=str(out/'failure.png'))
        (out/'failure.html').write_text(page.content(),encoding='utf-8')
        raise
    finally:
        report['clicks']=prior_clicks+page.evaluate('controlClicks');report['dialogs']=dialogs
        (out/'controls.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
        browser.close()
print(json.dumps({'status':report['status'],'checks':len(report['checks']),'clicks':len(report['clicks'])}))
