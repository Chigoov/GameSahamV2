"""Click corporate entitlements, votes and fund settlement against real isolated Java."""
import argparse
from datetime import datetime, timedelta, timezone
from decimal import Decimal
import json
import math
import os
from pathlib import Path
from playwright.sync_api import sync_playwright

parser = argparse.ArgumentParser()
parser.add_argument('--output-dir', required=True)
args = parser.parse_args()
ROOT = Path(__file__).resolve().parent.parent
out = Path(args.output_dir).resolve()
assert out.is_relative_to(ROOT)
out.mkdir(parents=True, exist_ok=True)
url = 'http://127.0.0.1:' + os.environ['QA_PORT']
zone = timezone(timedelta(hours=7))
report = {'status': 'IN_PROGRESS', 'scope': 'Real Java QA browser controls, no Android runtime certification',
          'checks': [], 'preparation': [], 'clicks': [], 'errors': [], 'blockers': []}

def persist():
    (out/'controls.json').write_text(json.dumps(report, indent=2, ensure_ascii=False), encoding='utf-8')

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page(viewport={'width': 390, 'height': 844}, reduced_motion='reduce')
    page.set_default_timeout(10000)
    page.on('pageerror', lambda error: report['errors'].append(str(error)))
    page.goto(url)
    page.wait_for_load_state('networkidle')
    def call(method, *values):
        return json.loads(page.evaluate('([m,a])=>TradingNativeBridge[m](...a)', [method, list(values)]))

    platform = call('getPlatformInfo')
    assert platform['testFixture'] and not platform['nativeHost'], platform
    report['platform'] = platform

    def snap():
        return call('getSnapshot')

    def prepared(method, *values):
        result = call(method, *values)
        report['preparation'].append({'method': method, 'arguments': values, 'result': result})
        persist()
        assert not isinstance(result, dict) or result.get('success') is not False, result
        return result

    def advance(target):
        state = snap()['clock']
        milliseconds = int(target.timestamp()*1000)-state['timestamp']
        assert milliseconds >= 0, (target, state)
        if milliseconds:
            prepared('setClockState', True, 1)
            prepared('tickClock', math.ceil(milliseconds/(state['rate']*1000)))
            prepared('setClockState', False, 1)

    def clicked(locator, name):
        locator.click()
        report['clicks'].append({'control': name, 'timestamp': snap()['clock']['timestamp'],
                                 'heading': page.locator('.flow-header h1').all_text_contents()})
        persist()

    def image(name):
        page.screenshot(path=str(out/(name+'.png')))

    def check(name, evidence):
        report['checks'].append({'action': name, 'observed': evidence})
        persist()

    def reload():
        page.reload()
        page.wait_for_load_state('networkidle')

    def close():
        if page.locator('.flow-page').count():
            clicked(page.locator('.back-button'), 'Tutup layar')

    def corporate():
        close()
        clicked(page.locator('[data-tab="pasar"]'), 'Pasar')
        clicked(page.locator('#btn-market-corporate'), 'Aksi korporasi & voting RUPS')

    def offers():
        return call('corporateOfferAction', json.dumps({'action': 'list'}))['offers']

    def detail(offer):
        corporate()
        card = page.locator('article.card').filter(has=page.get_by_role('heading', name=offer['instrumentId'], exact=False))
        assert card.count() == 1, offer
        clicked(card.get_by_role('button', name='Lihat penawaran', exact=True), 'Lihat penawaran '+offer['id'])

    def new_eligible():
        prepared('startNewGame', '{}')
        prepared('setClockState', False, 1)
        state = snap()
        origin = datetime.fromtimestamp(state['clock']['timestamp']/1000, zone).replace(hour=9, minute=45, second=0, microsecond=0)
        advance(origin)
        account = snap()['personalAccount']['id']
        for ticker in ['BBCA', 'BMRI', 'TLKM']:
            report['preparation'].append({'orderBook': ticker, 'result': call('getOrderBook', 'IDX:'+ticker)})
            request = {'requestId': 'CONTROL-PREP-'+ticker, 'accountId': account, 'instrumentId': 'IDX:'+ticker,
                       'side': 'BUY', 'type': 'MARKET', 'quantity': 100}
            result = prepared('submitOrder', json.dumps(request))
            assert result['status'] == 'FILLED' and result['filledQuantity'] == 100, result
        advance(origin + timedelta(days=4))
        reload()
        available = offers()
        assert len(available) == 3 and all(o['status'] == 'OPEN' and o['availableShares'] > 0 for o in available), available
        return available

    try:
        available = new_eligible()
        for offer in available:
            detail(offer)
            page.locator('#corporate-shares').fill('2')
            before = call('corporateOfferAction', json.dumps({'action': 'list'}))
            clicked(page.get_by_role('button', name='Tinjau partisipasi', exact=True), 'Tinjau '+offer['id'])
            assert page.locator('.flow-header h1').inner_text() == 'Review partisipasi korporasi'
            image(offer['id']+'-review-cancel')
            close()
            assert call('corporateOfferAction', json.dumps({'action': 'list'})) == before
            check(offer['id']+' review dibatalkan', 'Tutup review tidak membuat partisipasi atau reservasi')

            detail(offer)
            page.locator('#corporate-shares').fill('2')
            clicked(page.get_by_role('button', name='Tinjau partisipasi', exact=True), 'Tinjau '+offer['id'])
            clicked(page.get_by_role('button', name='Konfirmasi', exact=True), 'Konfirmasi '+offer['id'])
            page.get_by_role('button', name='Selesai', exact=True).wait_for()
            image(offer['id']+'-reserved')
            clicked(page.get_by_role('button', name='Selesai', exact=True), 'Selesai '+offer['id'])
            updated = next(o for o in offers() if o['id'] == offer['id'])
            assert updated['participations'][-1]['status'] == 'RESERVED', updated
            assert updated['availableShares'] == offer['availableShares']-2, updated
            detail(updated)
            clicked(page.get_by_role('button', name='Batalkan partisipasi', exact=True), 'Batalkan partisipasi '+offer['id'])
            cancelled = next(o for o in offers() if o['id'] == offer['id'])
            assert cancelled['participations'][-1]['status'] == 'CANCELLED', cancelled
            assert cancelled['availableShares'] == offer['availableShares']
            image(offer['id']+'-cancelled')
            check(offer['id']+' konfirmasi dan pembatalan', cancelled)

        for choice, field in [('Setuju', 'votesFor'), ('Tolak', 'votesAgainst'), ('Abstain', 'votesAbstain')]:
            if choice != 'Setuju':
                new_eligible()
            agenda = next(a for a in call('getShareholderAgendas') if a['canVote'])
            corporate()
            card = page.locator('[data-agenda-id="'+agenda['id']+'"]')
            assert card.get_by_role('button').count() == 3
            image('vote-'+field+'-eligible')
            clicked(card.get_by_role('button', name=choice, exact=True), choice+' RUPS')
            after = next(a for a in call('getShareholderAgendas') if a['id'] == agenda['id'])
            assert after['hasVoted'] and not after['canVote'], after
            assert after[field] == agenda[field]+agenda['entitledShares'], (agenda, after)
            assert page.locator('[data-agenda-id="'+agenda['id']+'"] button').count() == 0
            image('vote-'+field+'-recorded')
            check('RUPS '+choice, after)

        close()
        clicked(page.locator('[data-tab="pasar"]'), 'Pasar')
        today = datetime.fromtimestamp(snap()['clock']['timestamp']/1000, zone).date().isoformat()
        ipo = next(i for i in call('getIpoOfferings') if not i['isAllotted'] and i['listingDate'] >= today and i['currency'] == 'IDR')
        reserved_before = snap()['personalAccount']['cash']['IDR']['reserved']
        clicked(page.locator('#btn-ipo-center'), 'IPO')
        ipo_card = page.locator('[data-ipo-id="'+ipo['id']+'"]')
        ipo_card.locator('input').fill('1')
        image('ipo-valid-before-submit')
        clicked(ipo_card.get_by_role('button', name='Pesan saham IPO', exact=True), 'Pesan saham IPO '+ipo['id'])
        assert ipo_card.get_by_role('button', name='Pesan saham IPO', exact=True).is_disabled()
        assert 'Pemesanan berhasil' in ipo_card.inner_text(), ipo_card.inner_text()
        reserved_after = snap()['personalAccount']['cash']['IDR']['reserved']
        nominal = Decimal(ipo['offeringPriceRaw'])*ipo['lotSize']
        rupiah = lambda text: Decimal(''.join(c for c in text if c.isdigit()))
        assert rupiah(reserved_after)-rupiah(reserved_before) == nominal, (reserved_before, reserved_after, nominal)
        image('ipo-valid-reserved')
        check('IPO aktif berhasil dipesan lewat UI', {'offering': ipo, 'reservedBefore': reserved_before, 'reservedAfter': reserved_after})

        prepared('startNewGame', '{}')
        prepared('setClockState', False, 1)
        reload()
        clicked(page.locator('[data-tab="pasar"]'), 'Pasar')
        clicked(page.locator('#btn-reksa-dana'), 'Reksa Dana')
        fund = call('getMutualFunds')['funds'][0]
        clicked(page.locator('.flow-page article.card').first.get_by_role('button', name='Beli Unit (Subscribe)', exact=True), 'Beli Unit (Subscribe)')
        page.locator('.flow-content input').fill('1000000')
        clicked(page.get_by_role('button', name='Konfirmasi Pembelian Unit', exact=True), 'Konfirmasi Pembelian Unit')
        clicked(page.get_by_role('button', name='Lihat antrean reksa dana', exact=True), 'Lihat antrean reksa dana')
        pending = next(f for f in call('getMutualFunds')['funds'] if f['id'] == fund['id'])
        subscription = pending['orders'][-1]
        assert subscription['status'] == 'ACCEPTED', subscription
        image('fund-subscription-accepted')
        clicked(page.locator('.flow-page article.card').first.get_by_role('button', name='Batalkan permintaan', exact=True), 'Batalkan permintaan reksa dana')
        cancelled = next(f for f in call('getMutualFunds')['funds'] if f['id'] == fund['id'])
        assert cancelled['orders'][-1]['status'] == 'CANCELLED', cancelled
        image('fund-subscription-cancelled')
        check('Pembatalan permintaan reksa dana sebelum batas', cancelled['orders'][-1])
        clicked(page.locator('.flow-page article.card').first.get_by_role('button', name='Beli Unit (Subscribe)', exact=True), 'Beli Unit ulang setelah pembatalan')
        page.locator('.flow-content input').fill('1000000')
        clicked(page.get_by_role('button', name='Konfirmasi Pembelian Unit', exact=True), 'Konfirmasi Pembelian Unit ulang')
        clicked(page.get_by_role('button', name='Lihat antrean reksa dana', exact=True), 'Lihat antrean reksa dana')
        pending = next(f for f in call('getMutualFunds')['funds'] if f['id'] == fund['id'])
        subscription = pending['orders'][-1]
        assert subscription['status'] == 'ACCEPTED', subscription
        advance(datetime.fromtimestamp(subscription['expectedSettlement']/1000, zone)+timedelta(seconds=1))
        settled = next(f for f in call('getMutualFunds')['funds'] if f['id'] == fund['id'])
        assert settled['orders'][-1]['status'] == 'SETTLED' and Decimal(str(settled['myUnits'])) > 0, settled
        close()
        clicked(page.locator('#btn-reksa-dana'), 'Reksa Dana')
        clicked(page.locator('.flow-page article.card').first.get_by_role('button', name='Cairkan Unit (Redeem)', exact=True), 'Cairkan Unit (Redeem)')
        page.locator('.flow-content input').fill('1')
        clicked(page.get_by_role('button', name='Konfirmasi Pencairan Unit', exact=True), 'Konfirmasi Pencairan Unit')
        clicked(page.get_by_role('button', name='Lihat antrean reksa dana', exact=True), 'Lihat antrean reksa dana')
        pending = next(f for f in call('getMutualFunds')['funds'] if f['id'] == fund['id'])
        redemption = pending['orders'][-1]
        assert redemption['side'] == 'REDEEM' and redemption['status'] == 'ACCEPTED', redemption
        image('fund-redemption-accepted')
        account = snap()['personalAccount']['id']
        ledger_before = call('getLedger', account)
        advance(datetime.fromtimestamp(redemption['expectedSettlement']/1000, zone)+timedelta(seconds=1))
        redeemed = next(f for f in call('getMutualFunds')['funds'] if f['id'] == fund['id'])
        assert redeemed['orders'][-1]['status'] == 'SETTLED', redeemed
        assert Decimal(str(redeemed['myUnits'])) == Decimal(str(settled['myUnits']))-1, redeemed
        close()
        clicked(page.locator('#btn-reksa-dana'), 'Reksa Dana')
        image('fund-redemption-settled')
        report['ledgerBeforeRedemptionSettlement'] = ledger_before
        report['ledgerAfterRedemptionSettlement'] = call('getLedger', account)
        check('Pembelian dan pencairan unit valid sampai settlement', {'before': fund, 'subscription': settled, 'redemption': redeemed})
        assert not report['errors'], report['errors']
        report['status'] = 'PASS'
    except Exception as error:
        report['status'] = 'FAIL'
        report['failure'] = str(error)
        image('failure')
        raise
    finally:
        persist()
        browser.close()
print(json.dumps({'status': report['status'], 'checks': len(report['checks']), 'clicks': len(report['clicks'])}))
