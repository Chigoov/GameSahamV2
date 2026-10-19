"""Supplemental reading/forms smoke check; requires isolated Java QA fixture."""
from pathlib import Path
import json
from playwright.sync_api import sync_playwright

out = Path('Artifacts/UI-Refresh-2026-10-20')
checks, errors = [], []
with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page(viewport={'width':390,'height':844}, reduced_motion='reduce')
    page.on('pageerror', lambda error: errors.append(str(error)))
    page.goto('http://127.0.0.1:8767'); page.wait_for_load_state('networkidle')
    assert page.evaluate('JSON.parse(TradingNativeBridge.getPlatformInfo()).testFixture')
    def call(m,*a):
        return json.loads(page.evaluate('([m,a])=>TradingNativeBridge[m](...a)',[m,list(a)]))
    def tab(name): page.locator('[data-tab='+name+']').click()
    def back(): page.get_by_role('button',name='Kembali',exact=True).click()
    def opened(name, marker):
        layer=page.locator('.flow-page')
        assert marker in layer.inner_text(),name
        assert layer.evaluate('(el)=>el.scrollWidth<=el.clientWidth+1'),name
        layer.evaluate('(el)=>el.scrollTop=el.scrollHeight')
        page.screenshot(path=str(out/(name+'.png')))
        assert not errors, errors
        checks.append(name)

    assert call('startNewGame','{}')['success']
    page.reload(); page.wait_for_load_state('networkidle')
    tab('pasar')
    page.locator('#indices-row .index-card').first.click();opened('index-detail','Anggota');back()
    page.get_by_role('button',name='Detail indeks N225',exact=True).click();opened('index-unavailable','belum');back()
    page.locator('#btn-ipo-center').click();opened('ipo-center','IPO');back()
    page.locator('#btn-multiasset').click();opened('multiasset','Obligasi');back()
    page.locator('#btn-order-history').click();opened('orders-empty','Belum ada order');back()
    tab('aset');page.get_by_role('button',name='Tukar IDR / USD',exact=True).click()
    page.locator('#fx-amount').fill('1000000')
    page.get_by_role('button',name='Tinjau konversi',exact=True).click();opened('fx-review','Terima bersih')
    page.get_by_role('button',name='Konfirmasi konversi',exact=True).click();opened('fx-receipt','Debit / kredit bersih');back()
    tab('aset');page.locator('#asset-tools > summary').click()
    page.get_by_role('button',name='Fasilitas margin & risiko',exact=True).click();opened('risk-locked','Ekuitas bersih')
    assert page.get_by_role('button',name='Tarik pinjaman margin — Portfolio Manager',exact=True).is_disabled()
    back();tab('aset');page.locator('#asset-tools > summary').click()
    page.get_by_role('button',name='Aksi korporasi & voting RUPS',exact=True).click();opened('corporate-voting','Agenda voting');back()
    tab('aset');page.locator('#asset-tools > summary').click()
    page.get_by_role('button',name='Uji stres portofolio',exact=True).click()
    page.get_by_role('button',name='Jalankan simulasi dampak',exact=True).first.click();opened('stress-results','Potensi kerugian');back()
    tab('aset');page.locator('#asset-tools > summary').click()
    page.get_by_role('button',name='Lab strategi & sandbox what-if',exact=True).click()
    before = call('getSnapshot')['personalAccount']
    page.get_by_role('button',name='Eksekusi transaksi lab',exact=True).click();opened('lab-result','Total trade lab')
    assert before == call('getSnapshot')['personalAccount']
    back();tab('aset');page.locator('#asset-tools > summary').click()
    page.get_by_role('button',name='Replay historis',exact=True).click();opened('replay-unavailable','belum tersedia');back()
    tab('beranda');page.locator('#btn-briefing').click();opened('briefing','24 jam game = 5 menit nyata');back()
    page.locator('#btn-reports').click();opened('reports-empty','Belum ada hari');back()
    page.locator('#btn-entry-game').click();opened('entry-checkpoint','Portofolio Pribadi')
    page.get_by_role('button',name='Konfigurasi permainan baru',exact=True).click();opened('newgame-config','mereset');back()
    page.locator('#btn-settings').click();opened('settings','Simpan permainan')
    page.get_by_role('button',name='Panduan tutorial interaktif',exact=True).click();opened('tutorial','5. Penutupan');back()
    page.locator('#context-account-btn').click();opened('account-picker','Portofolio Pribadi');back()

    # Earn the rank using real fills, claims and daily salary, without fixture money/rank.
    call('setClockState',True,1);call('tickClock',7);call('setClockState',False,1)
    for ticker,price in [('BBCA','10300'),('TLKM','3200')]:
        payload={'requestId':'UI-FLOWS-'+ticker,'instrumentId':'IDX:'+ticker,'side':'BUY','type':'LIMIT','quantity':100,'limitPrice':price}
        assert call('submitOrder',json.dumps(payload))['success']
    assert call('claimMission','MIS-01')['success']
    assert call('claimMission','MIS-02')['success']
    for i in range(45):
        if call('getSnapshot')['career']['rank']=='PORTFOLIO_MANAGER':break
        assert call('skipDay','UI-FLOWS-DAY-'+str(i))['success']
    assert call('getSnapshot')['career']['rank']=='PORTFOLIO_MANAGER'
    tab('firma')
    page.get_by_role('button',name='Fund Builder & alokasi portofolio',exact=True).click();opened('fund-builder','Rencana tersimpan');back()
    tab('firma');page.get_by_role('button',name='Yayasan Dana Sejahtera — Tawaran Tersedia',exact=True).click();opened('mandate-offer','High-water mark')
    page.get_by_role('button',name='Terima Mandat Kelolaan Klien',exact=True).click()
    assert any(a['type']=='CLIENT' for a in call('getSnapshot')['tradingAccounts'])
    back();tab('firma');page.get_by_role('button',name='Laporan klien & fee mandat',exact=True).click();opened('client-reports','Trust');back()
    tab('riset');page.get_by_role('button',name='Lihat paket & opsi langganan',exact=True).click();opened('research-tiers','Basic');back()
    assert not errors,errors
    browser.close()
(out/'extended-flow-result.json').write_text(json.dumps({'status':'PASS','checks':checks,'errors':errors,'scope':'Browser + isolated Java; not native evidence'},indent=2),encoding='utf-8')
print(json.dumps({'status':'PASS','extendedFlows':len(checks)}))
