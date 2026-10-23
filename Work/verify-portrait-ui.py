"""UI checks against an explicitly isolated Java QA host; never resets a native save."""
from pathlib import Path
import argparse
import json
from urllib.parse import urlparse
from playwright.sync_api import sync_playwright

parser = argparse.ArgumentParser()
parser.add_argument('--url', default='http://127.0.0.1:8767')
parser.add_argument('--output', default='Artifacts/UI-Refresh-2026-10-20')
args = parser.parse_args()
assert urlparse(args.url).hostname in ('127.0.0.1', 'localhost')
out = Path(args.output).resolve()
out.mkdir(parents=True, exist_ok=True)
checks, samples, errors = [], [], []


def passed(name):
    checks.append(name)


with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page(viewport={'width':390, 'height':844}, reduced_motion='reduce')
    page.set_default_timeout(10000)
    page.on('pageerror', lambda error: errors.append(str(error)))
    page.goto(args.url)
    page.wait_for_load_state('networkidle')
    platform = page.evaluate('JSON.parse(TradingNativeBridge.getPlatformInfo())')
    assert platform.get('testFixture') is True and 'QA HOST JAVA' in platform.get('label', '')

    def call(method, *values):
        return json.loads(page.evaluate('([m,a])=>TradingNativeBridge[m](...a)', [method,list(values)]))

    def back():
        page.get_by_role('button', name='Kembali', exact=True).click()

    def tab(name):
        page.locator('[data-tab='+('pasar' if name == 'riset' else name)+']').click()
        if name == 'riset':
            page.locator('#btn-market-research').click()

    def flow():
        return page.locator('.flow-page')

    def screenshot(name):
        page.screenshot(path=str(out/(name+'.png')))

    assert call('startNewGame','{}')['success']
    page.reload(); page.wait_for_load_state('networkidle')
    snap = call('getSnapshot')
    assert page.locator('#home-net-worth').inner_text() == snap['personalAccount']['netWorth']
    assert page.locator('#header-net-worth').inner_text() == snap['personalAccount']['cash']['IDR']['available']
    passed('Home hero uses Java net equity; header uses actual available personal cash')
    screenshot('home-final')

    # Scroll belongs to content; navigation remains at the same viewport coordinates.
    for width,height in [(320,568),(360,640),(390,844),(412,915),(430,932),(768,1024),(1440,900)]:
        page.set_viewport_size({'width':width,'height':height})
        for name in ['beranda','pasar','aset','firma','riset']:
            tab(name)
            nav = page.locator('.bottom-nav').bounding_box()
            page.locator('.app-scroll').evaluate('(el)=>el.scrollTop=el.scrollHeight')
            assert page.locator('.bottom-nav').bounding_box() == nav
            m = page.evaluate('''()=>({width:innerWidth,scrollWidth:document.documentElement.scrollWidth,
                height:innerHeight,bodyScroll:document.documentElement.scrollHeight,
                navBottom:document.querySelector('.bottom-nav').getBoundingClientRect().bottom,
                mainHeight:document.querySelector('.app-scroll').clientHeight})''')
            assert m['scrollWidth'] <= width+1 and m['bodyScroll'] <= height+1, (name,m)
            assert abs(m['navBottom']-height) <= 1 and m['mainHeight'] >= 80, (name,m)
            assert page.locator('.nav-item[aria-current=page]').get_attribute('data-tab') == ('pasar' if name == 'riset' else name)
            assert len(page.locator('#view-'+name).inner_text()) > 60
            samples.append({'tab':name,**m})
    passed('35 root/viewport samples: content scrolls, nav stays visible, no page overflow')

    page.set_viewport_size({'width':390,'height':844})
    tab('pasar')
    page.locator('#filter-exchange-select').select_option('US')
    page.locator('#search-stock-input').fill('AAPL')
    assert page.locator('#stock-list-container .stock-list-item').count() == 1
    page.locator('.app-scroll').evaluate('(el)=>el.scrollTop=200')
    scroll = page.locator('.app-scroll').evaluate('(el)=>el.scrollTop')
    tab('beranda'); tab('pasar')
    assert page.locator('#filter-exchange-select').input_value() == 'US'
    assert page.locator('#search-stock-input').input_value() == 'AAPL'
    assert abs(page.locator('.app-scroll').evaluate('(el)=>el.scrollTop')-scroll) < 2
    passed('Market search, exchange filter and per-root scroll survive navigation')
    page.locator('#search-stock-input').fill('no-company-like-this')
    assert page.locator('#stock-list-container .stock-list-item').count() == 0
    assert 'Tidak ada emiten' in page.locator('#stock-list-status').inner_text()
    page.locator('#filter-exchange-select').select_option('')
    page.locator('#search-stock-input').fill('')
    screenshot('market-final')
    passed('Search empty state is explicit; clearing filter restores domain instruments')

    page.locator('#search-stock-input').focus()
    page.evaluate('''()=>{ window.quoteRow=document.querySelector('#stock-list-container .stock-list-item');
        window.quoteReader=TradingNativeBridge.getInstruments;
        TradingNativeBridge.getInstruments=()=>{throw Error('QA quote failure')}; }''')
    page.locator('#quote-update-warning').wait_for(state='visible')
    assert page.locator('#search-stock-input').evaluate('(el)=>el===document.activeElement')
    page.evaluate('()=>{TradingNativeBridge.getInstruments=window.quoteReader}')
    page.locator('#quote-update-warning').wait_for(state='detached')
    assert page.evaluate("window.quoteRow===document.querySelector('#stock-list-container .stock-list-item')")
    assert page.locator('#search-stock-input').evaluate('(el)=>el===document.activeElement')
    passed('Quote failure preserves last rows and input focus, announces staleness and recovers')

    page.locator('#stock-list-container .stock-list-item').first.click()
    chart = call('getChartData', flow().get_attribute('data-stock-id'), 'D1')
    if not chart.get('bars'):
        assert 'Belum ada transaksi tercatat' in flow().inner_text()
        assert flow().locator('svg').count() == 0
    else:
        assert flow().locator('svg').count() > 0
    assert page.locator('.bottom-nav').evaluate('(el)=>!el.inert')
    page.get_by_role('button',name='Tambah ke watchlist',exact=True).click()
    assert any(s['isWatchlist'] for s in call('getInstruments','','',None,True))
    screenshot('instrument-no-trades')
    passed('Instrument has honest no-trade chart state and actual watchlist command')
    tab('aset')
    assert flow().count() == 0
    assert 'Belum ada posisi' in page.locator('#view-aset').inner_text()
    screenshot('assets-empty')
    passed('Root navigation closes reading details and portfolio empty state is honest')

    # Reading a menu is not reading all documents and cannot award all article XP.
    xp = call('getPlayerProgress')['currentXp']
    tab('riset')
    assert call('getPlayerProgress')['currentXp'] == xp
    page.get_by_role('button',name='Baca catatan',exact=True).first.click()
    assert 'Simulasi game' in flow().inner_text()
    first_read = call('getPlayerProgress')['currentXp']
    assert first_read >= xp
    back(); tab('riset')
    page.get_by_role('button',name='Baca catatan',exact=True).first.click()
    assert call('getPlayerProgress')['currentXp'] == first_read
    back()
    page.get_by_role('button',name='Lihat paket & opsi langganan',exact=True).click()
    assert page.locator('[data-tier=PRO] button').is_disabled()
    back()
    screenshot('research-final')
    passed('Research requires opening an article; reread is idempotent; novice premium stays locked')

    # Actual trading path through Java rather than fixture balance/rank injection.
    clock=call('getSnapshot')['clock']
    page.evaluate('''clock=>{const d=new Date(clock.timestamp+7*3600000),seconds=d.getUTCHours()*3600+d.getUTCMinutes()*60+d.getUTCSeconds();TradingNativeBridge.setClockState(true,1);TradingNativeBridge.tickClock(Math.ceil(((9*3600+30*60-seconds+86400)%86400)/clock.rate));TradingNativeBridge.setClockState(false,1);}''',clock)
    tab('pasar'); page.locator('#stock-list-container .stock-list-item').first.click()
    page.locator('.btn-detail-buy').click()
    assert page.locator('.bottom-nav').evaluate('(el)=>el.inert')
    assert page.locator('[data-side=BUY]').get_attribute('aria-pressed') == 'true'
    assert page.locator('[data-side=SELL]').get_attribute('aria-pressed') == 'false'
    page.locator('[data-side=SELL]').click()
    assert page.locator('[data-side=BUY]').get_attribute('aria-pressed') == 'false'
    assert page.locator('[data-side=SELL]').get_attribute('aria-pressed') == 'true'
    page.locator('[data-side=BUY]').click()
    page.locator('#order-type').select_option('MARKET')
    page.locator('#order-qty').fill('1')
    page.set_viewport_size({'width':390,'height':390})
    page.locator('#order-qty').focus()
    page.locator('#btn-submit-order').scroll_into_view_if_needed()
    assert page.locator('#btn-submit-order').bounding_box()['y'] >= 0
    screenshot('ticket-short-viewport')
    page.set_viewport_size({'width':390,'height':844})
    page.locator('#btn-submit-order').click()
    assert flow().evaluate('(el)=>el.closest(".flow-page").classList.contains("isolated")')
    screenshot('review-final')
    page.get_by_role('button',name='Kirim order',exact=True).click()
    assert flow().locator('[data-status="FILLED"]').count() == 1, flow().inner_text()
    assert call('getPlayerProgress')['currentXp'] >= 100
    back(); tab('aset')
    assert 'BBCA' in page.locator('#portfolio-positions-list').inner_text()
    before = call('getSnapshot')['personalAccount']
    assert call('saveGame')['success'] and call('loadGame')['success']
    assert call('getSnapshot')['personalAccount'] == before
    screenshot('assets-with-position')
    passed('Explicit buy entry, buy/sell switching, short viewport ticket, isolated review, Java fill, position and save/load')

    tab('pasar'); page.locator('#stock-list-container .stock-list-item').first.click()
    screenshot('instrument-with-history')
    page.get_by_role('button',name='Garis',exact=True).click()
    assert page.get_by_role('button',name='Garis',exact=True).get_attribute('aria-pressed') == 'true'
    page.get_by_role('button',name='Tabel OHLCV Alternatif',exact=True).click()
    assert page.locator('#chart-table tbody tr').count() > 0
    page.locator('[data-chart-control="toggle-table"]').focus()
    call('setClockState',True,1);call('tickClock',10);call('setClockState',False,1)
    page.wait_for_timeout(1300)
    assert page.locator('#chart-table').is_visible()
    assert page.locator('[data-chart-control="toggle-table"]').evaluate('(el)=>el===document.activeElement && el.getAttribute("aria-expanded")==="true"')
    page.evaluate('''()=>{window.chartReader=TradingNativeBridge.getChartData;TradingNativeBridge.getChartData=()=>{throw Error('Gangguan chart QA')}}''')
    page.locator('.chart-warning').wait_for(state='visible')
    assert page.locator('[data-chart-control="toggle-table"]').evaluate('(el)=>el===document.activeElement')
    assert page.locator('#chart-table').is_visible()
    page.evaluate('()=>{TradingNativeBridge.getChartData=window.chartReader}')
    page.locator('.chart-warning').wait_for(state='detached')
    page.locator('[data-timeframe=M1]').click()
    assert page.locator('[data-timeframe=M1]').get_attribute('aria-pressed') == 'true'
    page.wait_for_timeout(1200)
    assert page.locator('[data-timeframe=M1]').evaluate('(el)=>el===document.activeElement')
    passed('Recorded game transactions render chart, timeframe, line/candle and alternative table')
    back()
    tab('beranda'); page.locator('#btn-achievements').click()
    page.get_by_role('button',name='Terbuka',exact=True).click()
    assert page.get_by_role('button',name='Terbuka',exact=True).get_attribute('aria-pressed') == 'true'
    assert page.locator('.achievement-card.locked').count() == 0
    flow().get_by_role('button',name='Semua',exact=True).click()
    screenshot('achievements-final')
    back(); page.locator('#btn-career').click()
    assert 'Syarat Senior Analyst' in flow().inner_text()
    assert page.locator('[data-mission-id=MIS-01]').count() == 1
    screenshot('missions-final')
    back(); passed('Badge filters reflect state; earned mission and promotion requirements remain reachable')

    for width,height in [(320,568),(390,844),(412,915)]:
        page.set_viewport_size({'width':width,'height':height})
        page.evaluate("document.documentElement.style.fontSize='200%'")
        for name in ['beranda','pasar','aset','firma','riset']:
            tab(name)
            page.locator('.app-scroll').evaluate('(el)=>el.scrollTop=el.scrollHeight')
            m=page.evaluate('''()=>({width:innerWidth,scrollWidth:document.documentElement.scrollWidth,
                nav:document.querySelector('.bottom-nav').getBoundingClientRect().bottom,
                main:document.querySelector('.app-scroll').clientHeight})''')
            assert m['scrollWidth'] <= width+1 and abs(m['nav']-height) <= 1 and m['main']>70,(name,m)
        tab('beranda'); page.locator('#btn-achievements').click()
        assert flow().evaluate('(el)=>el.scrollWidth<=el.clientWidth+1')
        flow().evaluate('(el)=>el.scrollTop=el.scrollHeight')
        screenshot('text-200-'+str(width)); back()
        page.evaluate("document.documentElement.style.fontSize=''")
    passed('15 root text-200 samples and achievement detail reflow without horizontal overflow')
    assert not errors, errors
    passed('No JavaScript runtime errors')
    browser.close()

result={'status':'PASS','scope':'Isolated Java host + browser; not Android/device evidence',
        'checks':checks,'viewportSamples':samples,'pageErrors':errors}
(out/'portrait-ui-result.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps({'status':'PASS','checks':len(checks),'rootViewportSamples':len(samples)}))
