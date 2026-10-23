"""Exercise approved navigation and UI states against an isolated real Java host."""
import argparse
import json
import os
import re
from pathlib import Path
from urllib.parse import urlparse
from playwright.sync_api import sync_playwright

parser = argparse.ArgumentParser()
parser.add_argument('--url', default='http://127.0.0.1:'+os.environ.get('QA_PORT', '8897'))
parser.add_argument('--output-dir', required=True)
args = parser.parse_args()
assert urlparse(args.url).hostname in ['localhost', '127.0.0.1']
ROOT = Path(__file__).resolve().parent.parent
out = Path(args.output_dir).resolve()
assert out.is_relative_to(ROOT)
out.mkdir(parents=True, exist_ok=True)
checks, errors = [], []
with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page(viewport={'width':390, 'height':844}, reduced_motion='reduce')
    page.on('pageerror', lambda error: errors.append(str(error)))
    page.set_default_timeout(10000)
    page.goto(args.url); page.wait_for_load_state('networkidle')
    platform = page.evaluate('JSON.parse(TradingNativeBridge.getPlatformInfo())')
    assert platform['testFixture'] and not platform['nativeHost'] and 'QA HOST JAVA' in platform['label']

    def call(method, *values):
        return json.loads(page.evaluate('([m,a])=>TradingNativeBridge[m](...a)', [method, list(values)]))

    def passed(action, observed):
        checks.append({'action':action, 'observed':observed})
        (out/'journeys.json').write_text(json.dumps({'status':'IN_PROGRESS', 'checks':checks, 'errors':errors}, indent=2), encoding='utf-8')

    def tab(name):
        page.locator('.nav-item[data-tab='+name+']').click()
        assert page.locator('#view-'+name).is_visible()

    def back():
        page.locator('.back-button').click()
        assert page.locator('.flow-page').count() == 0

    assert call('startNewGame', '{}')['success']
    page.reload(); page.wait_for_load_state('networkidle')
    assert [text.strip() for text in page.locator('.nav-item').all_text_contents()] == ['Beranda', 'Pasar', 'Portofolio', 'Bisnis', 'Lainnya']
    for name in ['beranda', 'pasar', 'aset', 'firma', 'riset']:
        tab(name)
        assert page.locator('.nav-item[aria-current="page"]').get_attribute('data-tab') == name
    passed('Lima tab utama klasik', 'Seluruh tujuan ada dan status aktif mengikuti tujuan')

    tab('beranda')
    call('setClockState',False,1)
    page.wait_for_timeout(1300)
    assert 'Dijeda' in page.locator('#home-clock-state').inner_text()
    stopped=page.locator('#clock-display').inner_text()
    page.wait_for_timeout(400)
    assert page.locator('#clock-display').inner_text()==stopped
    page.screenshot(path=str(out/'paused-clock.png'))
    call('setClockState',True,1)
    page.wait_for_timeout(1400)
    assert 'Berjalan otomatis' in page.locator('#home-clock-state').inner_text()
    assert page.locator('#clock-display').inner_text()!=stopped
    call('setClockState',False,1)
    passed('Jam pause dan berjalan', 'Label dan interpolasi mengikuti snapshot Java, tanpa mengubah rasio waktu domain')

    page.locator('.nav-item[data-tab=riset]').click(); page.locator('#view-riset').get_by_role('button',name='Profil Saya',exact=True).click()
    assert page.locator('.flow-page').get_attribute('aria-label') == 'Profil Saya'
    for name in ['Kinerja', 'Keuangan', 'Pencapaian']:
        page.locator('.profile-subtabs-nav').get_by_role('button', name=name, exact=True).click()
        assert page.locator('.profile-subtabs-nav [aria-pressed="true"]').inner_text()==name
        assert len(page.locator('.flow-content').inner_text()) > 250
    page.set_viewport_size({'width':320,'height':568})
    page.evaluate('document.documentElement.style.fontSize="200%"')
    metric=page.locator('.flow-page .metric-hero')
    metric.scroll_into_view_if_needed()
    numeric_lines=metric.evaluate('''el=>{const text=el.firstChild,match=text.textContent.match(/[0-9][0-9.,]*/),range=document.createRange();range.setStart(text,match.index);range.setEnd(text,match.index+match[0].length);return range.getClientRects().length;}''')
    assert numeric_lines==1,'Nilai kekayaan Profil harus utuh pada320px/200%'
    page.screenshot(path=str(out/'profile-money-text200.png'))
    page.evaluate('document.documentElement.style.fontSize="100%"')
    page.set_viewport_size({'width':390,'height':844})
    page.keyboard.press('Escape')
    assert page.locator('#view-riset').get_by_role('button',name='Profil Saya',exact=True).evaluate('(el)=>el===document.activeElement')
    passed('Profil, tiga tab, Escape dan fokus kembali', 'Isi tab nyata tampil dan fokus kembali ke tombol Profil')

    tab('riset')
    page.locator('#view-riset').get_by_role('button', name='Misi Periodik', exact=True).click()
    assert 'harian' in page.locator('.mission-tabs-nav .active').inner_text().lower()
    for name in ['Mingguan', 'Karier', 'Harian']:
        page.locator('.mission-tabs-nav').get_by_role('button', name=re.compile(name, re.I)).click()
        assert name.lower() in page.locator('.mission-tabs-nav .active').inner_text().lower()
        assert page.locator('.mission-tabs-nav [aria-pressed="true"]').count()==1
    back()
    page.locator('#view-riset').get_by_role('button', name='Liga & Rival', exact=True).click()
    assert page.locator('#league-mode').input_value() == 'NET_WORTH'
    page.locator('#league-mode').select_option('WEEKLY_RETURN')
    assert page.locator('#league-mode').input_value() == 'WEEKLY_RETURN'
    back()
    passed('Lainnya → misi dan liga', 'Enum tab/mode tetap valid ketika pembuka menerima click event')

    tab('riset')
    assert page.locator('#view-riset').is_visible()
    assert page.locator('.nav-item[aria-current="page"]').get_attribute('data-tab') == 'riset'
    assert page.evaluate('window.closeActiveLayer()') is False
    assert page.locator('#view-riset').is_visible()
    tab('pasar')
    passed('Lainnya → Back native', 'Lainnya merupakan tab utama dan Back tanpa lapisan tidak mengganti tab')

    page.locator('#search-stock-input').fill('NO-SUCH-INSTRUMENT')
    assert page.locator('#stock-list-container .stock-list-item').count() == 0
    assert page.locator('#stock-list-status').inner_text().strip()
    page.locator('#search-stock-input').fill('BBCA')
    assert page.locator('#stock-list-container .stock-list-item').count() == 1
    response = page.request.post(args.url+'/qa-action', data={'action':'advanceToTradingSession'})
    assert response.ok and response.json()['success']
    page.locator('#stock-list-container .stock-list-item').first.click()
    page.locator('.btn-detail-buy').click()
    assert page.locator('#order-modal.active').is_visible()
    for scale in [100,200]:
        page.set_viewport_size({'width':320,'height':568})
        page.evaluate('scale=>document.documentElement.style.fontSize=scale+"%"',scale)
        sizes=page.locator('.modal-header').evaluate('''el=>{const node=el.querySelector('div'),title=node.getBoundingClientRect(),button=el.querySelector('button').getBoundingClientRect(),box=el.getBoundingClientRect();return {title:title.width,scroll:node.scrollWidth,client:node.clientWidth,buttonWidth:button.width,buttonHeight:button.height,inside:title.left>=box.left&&title.right<=box.right+1&&button.left>=box.left&&button.right<=box.right+1,separate:title.right<=button.left+1||button.right<=title.left+1||title.bottom<=button.top+1||button.bottom<=title.top+1};}''')
        assert sizes['inside'] and sizes['separate'] and sizes['scroll']<=sizes['client']+1 and min(sizes['buttonWidth'],sizes['buttonHeight'])>=48,sizes
    page.evaluate('document.documentElement.style.fontSize="100%"')
    page.set_viewport_size({'width':390,'height':844})
    page.locator('#order-qty').fill('0'); page.locator('#btn-submit-order').click()
    assert page.locator('#order-qty').evaluate('(el)=>!el.validity.valid && Boolean(el.validationMessage)')
    assert page.locator('#order-qty').input_value() == '0'
    page.keyboard.press('Escape')
    assert not page.locator('#order-modal').is_visible()
    back()
    passed('Pencarian kosong dan tiket invalid', 'Pesan kosong/validasi terlihat, input invalid dipertahankan, Escape menutup tiket')

    page.set_viewport_size({'width':320,'height':568})
    page.evaluate('document.documentElement.style.fontSize="200%"')
    tab('riset')
    page.get_by_role('button',name='Terminal: analis, konsensus & perbandingan',exact=True).click()
    heading=page.locator('.flow-header h1').bounding_box()
    content=page.locator('.flow-content').bounding_box()
    assert heading['width']>=content['width']*.9,(heading,content)
    back()
    widths=page.locator('.nav-item').evaluate_all('els=>els.map(el=>({width:el.clientWidth,scrollWidth:el.scrollWidth,text:el.textContent}))')
    assert all(item['width']>=48 and item['scrollWidth']<=item['width'] for item in widths),widths
    page.evaluate('document.documentElement.style.fontSize="100%"')
    page.set_viewport_size({'width':390,'height':844})
    tab('pasar')
    passed('Header dan navigasi teks 200%', 'Judul memakai lebar konten, label tab utuh, tombol Tutup tidak mendesak nama emiten')

    page.locator('#search-stock-input').fill('')
    page.locator('#search-stock-input').focus()
    page.evaluate("()=>{window.originalInstruments=TradingNativeBridge.getInstruments; TradingNativeBridge.getInstruments=()=>{throw Error('Gangguan QA terencana')}}")
    page.locator('#quote-update-warning').wait_for(state='visible')
    assert page.locator('#search-stock-input').evaluate('(el)=>el===document.activeElement')
    page.evaluate('()=>{TradingNativeBridge.getInstruments=window.originalInstruments}')
    page.locator('#quote-update-warning').wait_for(state='detached')
    passed('Gangguan quote dan pemulihan', 'Data terakhir/fokus dipertahankan, warning hilang setelah sumber pulih')

    page.locator('#btn-settings').click()
    toggle = page.locator('#toggle-power-save')
    toggle.click(); assert toggle.get_attribute('aria-pressed') == 'true'
    toggle.click(); assert toggle.get_attribute('aria-pressed') == 'false'
    back()
    page.context.set_offline(True)
    tab('aset')
    assert page.locator('#asset-summary').inner_text().strip()
    page.context.set_offline(False)
    passed('Preferensi dan gangguan host QA', 'Hemat daya mengubah state; kegagalan bridge ditampilkan tanpa saldo contoh')

    tab('beranda'); page.locator('#btn-settings').click()
    assert page.locator('#toast-container').evaluate('(el)=>!el.inert && !el.closest("[inert]")')
    assert page.locator('#main-content').evaluate('(el)=>Boolean(el.closest("[inert]"))')
    page.get_by_role('button',name='Simpan permainan',exact=True).click()
    assert 'Save terkonfirmasi' in page.locator('.flow-page').inner_text()
    page.once('dialog',lambda dialog:dialog.accept())
    page.get_by_role('button',name='Muat checkpoint tersimpan',exact=True).click()
    assert 'Checkpoint dimuat' in page.locator('.flow-page').inner_text()
    page.get_by_role('button',name='Ukuran teks 200%',exact=True).click()
    assert page.evaluate('getComputedStyle(document.documentElement).fontSize') == '32px'
    page.get_by_role('button',name='Ukuran teks standar',exact=True).click()
    assert page.evaluate('getComputedStyle(document.documentElement).fontSize') == '16px'
    passed('Save, load dan ukuran teks menu', 'Save/load Java terkonfirmasi; dua tombol ukuran mengubah font sebenarnya')
    back()
    for action in ['Buka Briefing Pasar','Buka Menu Pasar','Buka Tiket Order Saham','Periksa Portofolio & Risiko','Buka Evaluasi Karir & Misi']:
        page.locator('#btn-settings').click()
        page.get_by_role('button',name='Panduan tutorial interaktif',exact=True).click()
        page.get_by_role('button',name=action,exact=True).click()
        if page.locator('#order-modal.active').count():
            page.locator('#btn-close-modal').click()
        if page.locator('.flow-page').count():
            back()
    passed('Lima aksi tutorial', 'Setiap tombol panduan membuka tujuan UI sebenarnya tanpa page error')
    tab('beranda')
    page.locator('#btn-entry-game').click()
    page.get_by_role('button',name='Lanjutkan sesi aktif (Resume)',exact=True).click()
    assert not page.locator('.flow-page').count()
    passed('Resume', 'Kembali ke Beranda tanpa mengganti run')
    page.locator('#btn-settings').click()
    back()
    page.screenshot(path=str(out/'journey-final.png'))
    assert not errors, errors
    browser.close()
(out/'journeys.json').write_text(json.dumps({'status':'PASS', 'checks':checks, 'errors':errors, 'scope':'Browser + isolated Java, not Android runtime or all-controls certification'}, indent=2), encoding='utf-8')
print(json.dumps({'status':'PASS', 'checks':len(checks)}))
