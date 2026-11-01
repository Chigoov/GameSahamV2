"""Check the classic shell and retained asset flows against isolated Java state."""
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
out = Path(args.output_dir).resolve()
assert out.is_relative_to(Path(__file__).resolve().parent.parent)
out.mkdir(parents=True, exist_ok=True)
report = {'status': 'IN_PROGRESS', 'checks': [], 'errors': [], 'scope': 'Classic shell and retained asset content; isolated Java QA, no Android certification.'}

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page(viewport={'width': 390, 'height': 844}, reduced_motion='reduce')
    page.set_default_timeout(10000)
    page.on('pageerror', lambda error: report['errors'].append(str(error)))
    page.goto(args.url)
    page.wait_for_load_state('networkidle')

    def call(method, *values):
        return json.loads(page.evaluate('([m,a])=>TradingNativeBridge[m](...a)', [method, list(values)]))

    def save(name, observed):
        assert not report['errors'], report['errors']
        filename = name+'.png'
        page.screenshot(path=str(out/filename))
        report['checks'].append({'name': name, 'observed': observed, 'screenshot': filename})
        (out/'classic-assets.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')

    def tab(name):
        if page.locator('.flow-page').count():
            page.locator('.back-button').click()
        page.locator('.nav-item[data-tab='+name+']').click()
        assert page.locator('#view-'+name).is_visible()
        assert page.locator('.nav-item[aria-current="page"]').get_attribute('data-tab') == name

    def asset_tool(name):
        tab('aset')
        details = page.locator('#asset-tools')
        if not details.evaluate('(el)=>el.open'):
            details.locator('summary').click()
        details.get_by_role('button', name=name, exact=True).click()

    try:
        platform = call('getPlatformInfo')
        assert platform['testFixture'] and not platform['nativeHost']
        report['platform'] = platform
        assert call('startNewGame', json.dumps({'initialCapital': 100000000}))['success']
        assert not call('setClockState', False, 1)['clock']['isPaused']
        page.reload()
        page.wait_for_load_state('networkidle')

        names = [s.strip() for s in page.locator('.nav-item').all_text_contents()]
        assert names == ['Beranda', 'Pasar', 'Portofolio', 'Bisnis', 'Lainnya'], names
        assert page.locator('.brand-mark').inner_text().replace('\n', '') == 'G↗'
        tokens = page.evaluate('''()=>Object.fromEntries(['--background','--action','--accent-purple','--positive'].map(k=>[k,getComputedStyle(document.documentElement).getPropertyValue(k).trim().toUpperCase()]))''')
        assert tokens == {'--background': '#080A12', '--action': '#FFB800', '--accent-purple': '#9945FF', '--positive': '#14F195'}, tokens
        for name in ['beranda', 'pasar', 'aset', 'firma', 'riset']:
            tab(name)
        assert page.evaluate('window.closeActiveLayer()') is False
        assert page.locator('#view-riset').is_visible()
        save('classic-five-tabs', {'navigation': names, 'palette': tokens, 'backAtRoot': 'Lainnya stays selected'})
        for label, title in [('Profil Saya', 'Profil Saya'), ('Misi Periodik', 'Pusat Misi & Target'), ('Liga & Rival', 'Liga & rival')]:
            page.locator('#view-riset').get_by_role('button', name=label, exact=True).click()
            assert page.locator('.flow-page').get_attribute('aria-label') == title
            page.locator('.back-button').click()
            assert page.locator('#view-riset').is_visible()
        save('classic-profile-missions-league', 'Profile, missions and league remain reachable through the classic Lainnya root.')

        clock = call('getSnapshot')['clock']
        page.evaluate('''c=>{const d=new Date(c.timestamp+7*3600000),s=d.getUTCHours()*3600+d.getUTCMinutes()*60+d.getUTCSeconds();TradingNativeBridge.setClockState(true,1);TradingNativeBridge.tickClock(Math.ceil(((34200-s+86400)%86400)/c.rate));TradingNativeBridge.setClockState(false,1)}''', clock)
        result = call('submitOrder', json.dumps({'requestId': 'CLASSIC-ASSET-HOLDING', 'instrumentId': 'IDX:BBCA', 'side': 'BUY', 'type': 'MARKET', 'quantity': 100}))
        assert result['success'] and result['status'] == 'FILLED', result
        call('setClockState', False, 1)
        page.reload()
        page.wait_for_load_state('networkidle')
        tab('aset')
        portfolio = call('getPortfolio', call('getSnapshot')['personalAccount']['id'])
        assert portfolio['success'] and portfolio['positions'], portfolio
        summary = page.locator('#asset-summary')
        assert summary.locator('.metric-hero').inner_text() == portfolio['netWorth']
        assert portfolio['cashValueIdr'] in summary.inner_text()
        assert portfolio['positionsValueIdr'] in summary.inner_text()
        assert summary.locator('.allocation-bar').get_attribute('aria-label') == str(portfolio['positionsSharePercent'])+'% aset berupa posisi saham'
        wallet = summary.locator('details').filter(has=page.locator('summary', has_text='Dompet IDR & USD'))
        wallet.locator('summary').click()
        for currency in ['IDR', 'USD']:
            for key, label in [('total', 'total'), ('available', 'tersedia'), ('reserved', 'tertahan')]:
                row = wallet.locator('.data-row').filter(has=page.locator('span', has_text=currency+' '+label))
                assert row.locator('strong').inner_text() == portfolio['cash'][currency][key]
        assert page.locator('.position-row[data-instrument-id="IDX:BBCA"]').count() == 1
        page.locator('.position-row[data-instrument-id="IDX:BBCA"]').click()
        assert page.locator('.flow-page').get_attribute('aria-label') == 'Detail posisi'
        assert 'BBCA' in page.locator('.flow-page').inner_text()
        save('real-portfolio-position', 'Real market BUY produced an actual BBCA position; displayed wealth, allocation and six wallet balances match getPortfolio.')
        page.locator('.back-button').click()

        assert page.locator('#asset-tools > button').count() == 12
        routes = [
            ('Rincian Kekayaan Bersih Konsolidasi', 'Kekayaan Bersih Konsolidasi'),
            ('Reksa Dana Berbasis NAB', 'Pasar Reksa Dana (Unit/NAB)'),
            ('Utang & Pinjaman Tenor', 'Utang & Pinjaman Tenor'),
            ('Barang koleksi & properti mewah', 'Galeri Koleksi Mewah'),
            ('Multi-aset', 'Katalog multi-aset'),
            ('Riwayat Transaksi Akun', 'Riwayat Transaksi'),
            ('Fasilitas margin & risiko', 'Fasilitas margin & risiko'),
            ('Aksi korporasi & voting RUPS', 'Aksi korporasi & voting RUPS'),
            ('Uji stres portofolio', 'Uji stres portofolio makro'),
            ('Lab strategi & sandbox what-if', 'Lab strategi & replay keputusan'),
            ('Replay historis', 'Replay historis data nyata'),
        ]
        for index, (label, title) in enumerate(routes):
            asset_tool(label)
            assert page.locator('.flow-page').get_attribute('aria-label') == title
            assert page.locator('.flow-page h1').inner_text() == title
            assert len(page.locator('.flow-page').inner_text()) > len(title)+30
            if label == 'Barang koleksi & properti mewah':
                page.locator('.collections-gallery-wrap .category-pills button').nth(1).click()
                page.locator('.collections-gallery-wrap .filter-chips button').nth(1).click()
                assert page.locator('.flow-page h1').inner_text() == title
                assert page.locator('.back-button').is_visible()
            if label == 'Fasilitas margin & risiko':
                assert page.locator('#short-instrument').is_visible()
                assert page.locator('#short-shares').is_visible()
                open_short = page.get_by_role('button', name=re.compile('^Buka posisi short'))
                assert open_short.count() == 1
                assert open_short.is_disabled() == (not call('getSnapshot')['featureAccess']['short']['unlocked'])
                assert page.get_by_role('button', name='Beli kembali saham short', exact=True).count() == 1
            save('asset-route-'+str(index+1), {'button': label, 'destination': title})
            page.locator('.back-button').click()
        asset_tool('Bisnis operasional & anak usaha')
        assert page.locator('#view-firma').is_visible()
        save('asset-route-12-business', 'Business tool opens classic Bisnis tab; collection tool above opened its gallery directly.')
        tab('aset')
        page.get_by_role('button', name='Tukar IDR / USD', exact=True).click()
        assert page.locator('.flow-page').get_attribute('aria-label') == 'Konversi FX'
        save('asset-fx', 'FX remains outside the twelve expandable tools and opens its actual form.')
        page.locator('.back-button').click()

        geometry = []
        for width, height in [(320, 568), (360, 800), (390, 844), (412, 915)]:
            page.set_viewport_size({'width': width, 'height': height})
            for scale in [100, 200]:
                page.evaluate('scale=>document.documentElement.style.fontSize=scale+"%"', scale)
                for name in ['beranda', 'pasar', 'aset', 'firma', 'riset']:
                    tab(name)
                    sizes = page.evaluate('''()=>({root:document.documentElement.scrollWidth,width:innerWidth,main:document.querySelector('.app-scroll').scrollWidth,mainWidth:document.querySelector('.app-scroll').clientWidth,overflow:[...document.querySelectorAll('.app-scroll *')].filter(e=>{const b=e.getBoundingClientRect();return b.width&&b.right>innerWidth+1&&getComputedStyle(e).visibility!=='hidden'}).slice(0,15).map(e=>({tag:e.tagName,id:e.id,class:e.className,text:e.innerText?.slice(0,70),right:e.getBoundingClientRect().right}))})''')
                    assert sizes['root'] <= sizes['width']+1 and sizes['main'] <= sizes['mainWidth']+1, (name, scale, width, sizes)
                    assert page.locator('.nav-item[aria-current="page"]').is_visible()
                    geometry.append({'route': name, 'width': width, 'height': height, 'textPercent': scale, **sizes})
                    save('geometry-'+name+'-'+str(width)+'-'+str(scale), sizes)
        report['geometry'] = geometry
        assert not report['errors'], report['errors']
        report['status'] = 'PASS'
    except Exception as error:
        report['status'] = 'FAIL'
        report['failure'] = str(error)
        page.screenshot(path=str(out/'failure.png'))
        raise
    finally:
        (out/'classic-assets.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
        browser.close()
print(json.dumps({'status': report['status'], 'checks': len(report['checks']), 'output': str(out)}))
