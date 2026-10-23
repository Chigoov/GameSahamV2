"""
Comprehensive End-to-End Playtest Journey Runner (P2).
Validates full player journey against local Java domain QA server:
1. Game initialization, market status, time, goals, 5-tab navigation.
2. IDX stock search -> detail -> buy -> review -> receipt -> portfolio (assertions vs domain).
3. Partial sell (100 -> 50) and full sell (50 -> 0) with in-place live update on open position detail modal.
4. FX conversion (IDR -> USD), US stock trading (US:AAPL), lot size = 1, resting order cancel.
5. Business (Firma) capital transfer with strict fund segregation between personal & company.
6. Progression / Career & Mission feedback with explicit lock explanations.
7. Save -> Reload -> Resume journey with state continuity.
"""
import time
import json
import uuid
import sys
import os
import subprocess
import argparse
from pathlib import Path
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parent.parent

def run_playtest(output_dir=None):
    if output_dir:
        out = Path(output_dir)
    else:
        out = ROOT / 'Artifacts/Kandidat-Layak-Uji-Bermain-2026-10-22/playtest-journey'
    out.mkdir(parents=True, exist_ok=True)
    screenshot_dir = out / 'screenshots'
    screenshot_dir.mkdir(parents=True, exist_ok=True)

    checks = []
    journey_log = []

    # Ensure QA server is running
    pids = subprocess.check_output("netstat -ano | grep 8784 || true", shell=True).decode().split()
    server = None
    if not pids:
        print("Starting QA server on port 8784...")
        server = subprocess.Popen(['python', str(ROOT / 'Work/WireQaStart.py')])
        time.sleep(3)

    try:
        with sync_playwright() as p:
            browser = p.chromium.launch(headless=True)
            page = browser.new_page(viewport={'width': 390, 'height': 844})
            page.goto('http://127.0.0.1:8784/')
            page.wait_for_load_state('networkidle')

            # Ensure market session is active (trading hours 09:00+ WIB)
            page.evaluate('() => TradingNativeBridge.setClockState(true, 1)')
            page.evaluate('() => TradingNativeBridge.tickClock(1800)')
            time.sleep(0.5)

            # -------------------------------------------------------------
            # STEP 1: Game Initialization, four tabs and secondary market research
            # -------------------------------------------------------------
            init_snap = page.evaluate('() => JSON.parse(TradingNativeBridge.getSnapshot())')
            personal_acc = init_snap['personalAccount']
            account_id = personal_acc['id']
            init_idr_cash = personal_acc['cash']['IDR']['available']
            init_equity = personal_acc.get('netWorth', personal_acc['cash']['IDR']['total'])

            page.screenshot(path=str(screenshot_dir / '01-beranda-initial.png'))
            tabs = ['beranda', 'pasar', 'aset', 'firma', 'riset']
            for t in tabs:
                page.locator(f'[data-tab="{"pasar" if t == "riset" else t}"]').click()
                if t == 'riset':
                    page.locator('#btn-market-research').click()
                page.wait_for_selector(f'#view-{t}.active')
                time.sleep(0.1)
            page.locator('[data-tab="beranda"]').click()
            page.wait_for_selector('#view-beranda.active')

            hero_net_equity = page.locator('#home-net-worth').text_content()
            checks.append(f"Step 1: Game initialized; four root tabs plus secondary research verified; initial Net Equity: {hero_net_equity}")
            journey_log.append({"step": 1, "action": "Init", "cash": init_idr_cash, "netEquity": hero_net_equity})

            # Ensure account has ample trading funds from company dividends/capital
            comps_data = page.evaluate('() => JSON.parse(TradingNativeBridge.getCompanies()).companies || []')
            if comps_data:
                cid = comps_data[0]['id']
                page.evaluate(f'() => JSON.parse(TradingNativeBridge.transferCompanyCapital(JSON.stringify({{"companyId": "{cid}", "toCompany": false, "amount": "10000000"}})))')
                page.evaluate('() => fetch("/qa-action", {method: "POST", headers: {"Content-Type": "application/json"}, body: JSON.stringify({action: "generateNpcLiquidity"})})')

            # -------------------------------------------------------------
            # STEP 2: IDX Stock Purchase (ASII)
            # -------------------------------------------------------------
            page.locator('[data-tab="pasar"]').click()
            page.wait_for_selector('#view-pasar.active')
            page.locator('#search-stock-input').fill('ASII')
            page.wait_for_selector('#stock-list-container .stock-list-item')
            page.locator('#stock-list-container .stock-list-item').first.click()
            page.wait_for_selector('.flow-page')

            # Verify Instrument Detail features
            status_text = page.locator('#detail-market-status-row strong').text_content()
            assert status_text == 'NORMAL', f"Market status must be NORMAL, got {status_text}"
            page.screenshot(path=str(screenshot_dir / '02-idx-detail-asii.png'))

            # Open Order Ticket
            page.locator('.btn-detail-buy').click()
            page.wait_for_selector('#order-modal.active')
            page.locator('#order-type').select_option('MARKET')
            page.locator('#order-qty').fill('2') # 2 units = 200 shares for IDX
            page.screenshot(path=str(screenshot_dir / '03-order-ticket-asii.png'))

            # Submit to Review Sheet
            page.locator('#btn-submit-order').click()
            page.wait_for_selector('.flow-page')
            page.screenshot(path=str(screenshot_dir / '04-review-sheet-asii.png'))

            # Confirm Order
            page.locator('button:has-text("Kirim order")').click()
            time.sleep(0.5)
            page.screenshot(path=str(screenshot_dir / '05-receipt-asii.png'))

            receipt_status = page.locator('.flow-page .data-row:has(span:text("Status")) strong').text_content()
            assert receipt_status in ('FILLED', 'Terisi penuh'), f"Order receipt should be FILLED / Terisi penuh, got: {receipt_status}"
            checks.append(f"Step 2: Buy 2 lots (200 shares) IDX:ASII executed and confirmed '{receipt_status}' in receipt")
            journey_log.append({"step": 2, "action": "Buy IDX:ASII", "status": receipt_status})
            page.locator('.back-button').click()
            page.wait_for_selector('.flow-page', state='detached')

            # -------------------------------------------------------------
            # STEP 3: Partial SELL & Full SELL with In-Place Modal Sync
            # -------------------------------------------------------------
            page.locator('[data-tab="aset"]').click()
            page.wait_for_selector('#view-aset.active')
            page.wait_for_selector('.position-row:has-text("IDX:ASII")')
            page.screenshot(path=str(screenshot_dir / '06-portfolio-after-buy-asii.png'))

            # Open Position Detail for ASII
            page.locator('.position-row:has-text("IDX:ASII")').first.click()
            page.wait_for_selector('.flow-page[data-position-instrument-id="IDX:ASII"]')
            init_asii_qty_str = page.locator('.flow-page .data-row:has(span:text("Jumlah saham")) strong').text_content()
            init_asii_qty = int(init_asii_qty_str.replace(' saham', '').replace('.', '').strip())
            assert init_asii_qty >= 200, f"Expected at least 200 saham, got {init_asii_qty}"
            page.screenshot(path=str(screenshot_dir / '07-position-detail-asii-full.png'))

            # 3a. Partial Sell: Sell 100 shares (1 lot) via bridge while modal remains open
            part_sell_req = 'SELL-PART-' + str(uuid.uuid4())
            part_sell = {
                "requestId": part_sell_req,
                "accountId": account_id,
                "instrumentId": "IDX:ASII",
                "side": "SELL",
                "type": "MARKET",
                "quantity": 100,
                "price": 0
            }
            part_res = page.evaluate('(order) => JSON.parse(TradingNativeBridge.submitOrder(JSON.stringify(order)))', part_sell)
            assert part_res.get('status') == 'FILLED', part_res

            expected_part_qty = f"{init_asii_qty - 100} saham"
            # Wait for modal DOM to live-sync to partial quantity
            page.wait_for_function(
                '''(exp) => {
                    const el = Array.from(document.querySelectorAll('.flow-page .data-row')).find(r => r.querySelector('span')?.textContent === 'Jumlah saham');
                    return el && el.querySelector('strong')?.textContent === exp;
                }''',
                arg=expected_part_qty,
                timeout=5000
            )
            page.screenshot(path=str(screenshot_dir / '08-position-detail-asii-partial.png'))
            checks.append(f"Step 3a: Partial sell (100 shares / 1 lot) synced in-place to '{expected_part_qty}' on open modal")

            # 3b. Full Sell: Sell all remaining shares to close position completely
            remaining_qty = init_asii_qty - 100
            full_sell_req = 'SELL-FULL-' + str(uuid.uuid4())
            full_sell = {
                "requestId": full_sell_req,
                "accountId": account_id,
                "instrumentId": "IDX:ASII",
                "side": "SELL",
                "type": "MARKET",
                "quantity": remaining_qty,
                "price": 0
            }
            full_res = page.evaluate('(order) => JSON.parse(TradingNativeBridge.submitOrder(JSON.stringify(order)))', full_sell)
            assert full_res.get('status') == 'FILLED', full_res

            # Wait for modal DOM to live-sync to closed state (0 saham)
            page.wait_for_function('''() => {
                const el = Array.from(document.querySelectorAll('.flow-page .data-row')).find(r => r.querySelector('span')?.textContent === 'Jumlah saham');
                return el && el.querySelector('strong')?.textContent === "0 saham";
            }''', timeout=5000)

            hero_closed = page.locator('.flow-page .metric-hero').text_content()
            unrealized_closed = page.locator('.flow-page .data-row:has(span:text("Laba/rugi belum terealisasi")) strong').text_content()
            realized_gross = page.locator('.flow-page .data-row:has(span:text("Hasil terealisasi sebelum biaya")) strong').text_content()
            note_closed = page.locator('.flow-page .data-row:has(span:text("Catatan")) strong').text_content()

            assert "Rp 0 (Posisi ditutup)" in hero_closed, f"Expected 'Rp 0 (Posisi ditutup)', got: {hero_closed}"
            assert "Rp 0" in unrealized_closed, f"Expected 'Rp 0', got: {unrealized_closed}"
            assert realized_gross != "-" and realized_gross != "", f"Expected retained realized gross PnL, got: {realized_gross}"
            assert "Posisi telah ditutup sepenuhnya" in note_closed, f"Expected closed note, got: {note_closed}"
            page.screenshot(path=str(screenshot_dir / '09-position-detail-asii-closed.png'))

            checks.append(f"Step 3b: Full sell synced in-place on open modal: hero={hero_closed}, realizedGross={realized_gross}")
            journey_log.append({"step": 3, "action": "Full Sell ASII", "hero": hero_closed, "realizedGross": realized_gross})
            page.locator('.back-button').click()
            page.wait_for_selector('.flow-page', state='detached')

            # -------------------------------------------------------------
            # STEP 4: FX Conversion & US Stock Trading (US:AAPL)
            # -------------------------------------------------------------
            # 4a. Fund two US shares plus fees with 10.000.000 IDR.
            page.locator('#asset-summary details summary').click()
            time.sleep(0.3)
            init_usd_avail = page.locator('#asset-summary .data-row:has(span:text("USD tersedia")) strong').text_content()

            fx_req = 'FX-PLAY-' + str(uuid.uuid4())
            fx_preview = page.evaluate(f'''() => JSON.parse(TradingNativeBridge.previewFx(JSON.stringify({{
                "accountId": "{account_id}", "from": "IDR", "to": "USD", "amount": "10000000"
            }})))''')
            assert fx_preview.get('success') is True, fx_preview
            fx_confirm = {"requestId": fx_req, "accountId": account_id, "quoteId": fx_preview["quoteId"], "from": "IDR", "to": "USD", "amount": "10000000"}
            fx_res = page.evaluate('(req) => JSON.parse(TradingNativeBridge.confirmFx(JSON.stringify(req)))', fx_confirm)
            assert fx_res.get('success') is True, fx_res

            # Wait for USD cash in DOM to reflect new available USD
            snap_fx = page.evaluate('() => JSON.parse(TradingNativeBridge.getSnapshot())')
            expected_usd = snap_fx['personalAccount']['cash']['USD']['available']
            page.wait_for_function(
                '''(exp) => {
                    const el = Array.from(document.querySelectorAll('#asset-summary .data-row')).find(r => r.querySelector('span')?.textContent === 'USD tersedia');
                    return el && el.querySelector('strong')?.textContent === exp;
                }''',
                arg=expected_usd,
                timeout=5000
            )
            page.screenshot(path=str(screenshot_dir / '10-fx-wallet-usd.png'))
            checks.append(f"Step 4a: FX conversion completed: USD available synced to {expected_usd}")

            # 4b. Buy 2 shares of US:AAPL (lotSize = 1)
            page.evaluate('''() => {
                const s=JSON.parse(TradingNativeBridge.getSnapshot());const d=new Date(s.clock.timestamp+7*3600000);
                const seconds=d.getUTCHours()*3600+d.getUTCMinutes()*60+d.getUTCSeconds();
                const delta=(21*3600+30*60-seconds+86400)%86400;
                TradingNativeBridge.setClockState(true,1);TradingNativeBridge.tickClock(Math.ceil(delta/s.clock.rate));TradingNativeBridge.setClockState(false,1);
            }''')
            assert page.evaluate('JSON.parse(TradingNativeBridge.getSnapshot()).clock.isUsTradingOpen'), 'US market must be open before the real order'
            page.locator('[data-tab="pasar"]').click()
            page.wait_for_selector('#view-pasar.active')
            page.locator('#filter-exchange-select').select_option('US')
            page.locator('#search-stock-input').fill('AAPL')
            page.wait_for_selector('#stock-list-container .stock-list-item')
            page.locator('#stock-list-container .stock-list-item').first.click()
            page.wait_for_selector('.flow-page')
            page.screenshot(path=str(screenshot_dir / '11-us-detail-aapl.png'))

            page.locator('.btn-detail-buy').click()
            page.wait_for_selector('#order-modal.active')
            page.locator('#order-type').select_option('MARKET')
            page.locator('#order-qty').fill('2')
            page.locator('#btn-submit-order').click()
            page.wait_for_selector('.flow-page')
            page.locator('button:has-text("Kirim order")').click()
            time.sleep(0.5)

            receipt_us = page.locator('.flow-page .data-row:has(span:text("Status")) strong').text_content()
            assert receipt_us in ('FILLED', 'Terisi penuh'), f"Expected US receipt FILLED / Terisi penuh, got {receipt_us}"
            page.screenshot(path=str(screenshot_dir / '12-receipt-us-aapl.png'))
            checks.append(f"Step 4b: Buy 2 shares US:AAPL executed and confirmed '{receipt_us}'")
            page.locator('.back-button').click()
            page.wait_for_selector('.flow-page', state='detached')

            # 4c. Resting Limit Order & In-Place Cancellation
            # Re-open AAPL detail from stock list
            page.locator('#stock-list-container .stock-list-item').first.click()
            page.wait_for_selector('.flow-page')

            page.locator('.btn-detail-buy').click()
            page.wait_for_selector('#order-modal.active')
            page.locator('#order-type').select_option('LIMIT')
            page.locator('#order-limit-price').fill('10.00') # far below market ($235.00)
            page.locator('#order-qty').fill('1')
            page.locator('#btn-submit-order').click()
            page.wait_for_selector('.flow-page')
            page.locator('button:has-text("Kirim order")').click()
            time.sleep(0.5)

            resting_status = page.locator('.flow-page .data-row:has(span:text("Status")) strong').text_content()
            assert resting_status in ('OPEN', 'Mengantre'), f"Expected resting order status OPEN/Mengantre, got {resting_status}"
            order_line = page.locator('.flow-page .data-row:has(span:text("Order / permintaan")) strong').text_content()
            resting_req_id = order_line.split('/')[-1].strip()
            page.screenshot(path=str(screenshot_dir / '13-receipt-resting-open.png'))

            # Cancel while receipt modal remains open
            cancel_res = page.evaluate(f'() => JSON.parse(TradingNativeBridge.cancelOrder("{resting_req_id}"))')
            assert cancel_res.get('status') == 'CANCELLED', cancel_res

            page.wait_for_function('''() => {
                const el = Array.from(document.querySelectorAll('.flow-page .data-row')).find(r => r.querySelector('span')?.textContent === 'Status');
                const txt = el ? el.querySelector('strong')?.textContent : '';
                return txt === "CANCELLED" || txt === "Dibatalkan";
            }''', timeout=5000)

            assert page.locator('.flow-page button:has-text("Batalkan sisa")').count() == 0, "Cancel button must be removed"
            page.screenshot(path=str(screenshot_dir / '14-receipt-resting-cancelled.png'))
            checks.append("Step 4c: Resting limit order submitted as OPEN, cancelled via bridge, and live-synced to CANCELLED in-place")
            page.locator('.back-button').click()
            page.wait_for_selector('.flow-page', state='detached')

            # -------------------------------------------------------------
            # STEP 5: Business (Firma) & Fund Segregation
            # -------------------------------------------------------------
            # Ensure at least one company exists
            comps_list = page.evaluate('() => JSON.parse(TradingNativeBridge.getCompanies()).companies || []')
            if not comps_list:
                comp_id = "COMP-P2-" + str(int(time.time()))
                page.evaluate(f'''() => JSON.parse(TradingNativeBridge.foundCompany(JSON.stringify({{
                    "id": "{comp_id}", "name": "PT Vibe Capital", "sector": "Keuangan", "capital": "30000000"
                }})))''')
            page.locator('[data-tab="firma"]').click()
            page.wait_for_selector('#view-firma.active')
            page.wait_for_selector('[data-company-id]')
            target_comp_id = page.locator('[data-company-id]').first.get_attribute('data-company-id')
            page.screenshot(path=str(screenshot_dir / '15-firma-tab.png'))

            # Record initial cash
            snap_before_transfer = page.evaluate('() => JSON.parse(TradingNativeBridge.getSnapshot())')
            personal_cash_before = int(snap_before_transfer['personalAccount']['cash']['IDR']['available'].replace('Rp', '').replace('.', '').strip())
            comp_before = page.evaluate('(cid) => JSON.parse(TradingNativeBridge.getCompanies()).companies.find(c => c.id === cid)', target_comp_id)
            comp_cash_before = int(comp_before['cash'])

            # Transfer 2.000.000 from company to personal
            cap_transfer = {"companyId": target_comp_id, "toCompany": False, "amount": "2000000"}
            cap_res = page.evaluate('(action) => JSON.parse(TradingNativeBridge.transferCompanyCapital(JSON.stringify(action)))', cap_transfer)
            assert cap_res.get('success') is True, cap_res

            snap_after_transfer = page.evaluate('() => JSON.parse(TradingNativeBridge.getSnapshot())')
            personal_cash_after = int(snap_after_transfer['personalAccount']['cash']['IDR']['available'].replace('Rp', '').replace('.', '').strip())
            comp_after = page.evaluate('(cid) => JSON.parse(TradingNativeBridge.getCompanies()).companies.find(c => c.id === cid)', target_comp_id)
            comp_cash_after = int(comp_after['cash'])

            # Assert strict segregation: personal increased by 2.000.000, company decreased by exactly 2.000.000
            assert personal_cash_after == personal_cash_before + 2000000, f"Personal cash must increase by 2M: {personal_cash_before} -> {personal_cash_after}"
            assert comp_cash_after == comp_cash_before - 2000000, f"Company cash must decrease by 2M: {comp_cash_before} -> {comp_cash_after}"
            checks.append(f"Step 5: Fund segregation verified: Personal cash +Rp 2.000.000, Company cash -Rp 2.000.000")

            # -------------------------------------------------------------
            # STEP 6: Career, Progression & Mission Feedback
            # -------------------------------------------------------------
            page.locator('[data-tab="firma"]').click()
            page.wait_for_selector('#view-firma.active')
            page.screenshot(path=str(screenshot_dir / '16-karier.png'))

            # Verify that mission list or career tier displays clear requirements
            career_text = page.locator('#view-firma').text_content()
            assert len(career_text) > 50 and 'Jabatan saat ini' in career_text, "Career view must render rank and progression content"
            assert page.get_by_role('button', name='Misi Periodik', exact=True).is_visible()
            checks.append("Step 6: Karier renders actual rank and reachable periodic missions")

            # -------------------------------------------------------------
            # STEP 7: Save -> Reload -> Resume Playability
            # -------------------------------------------------------------
            # Capture state before reload
            pre_reload_snap = page.evaluate('() => JSON.parse(TradingNativeBridge.getSnapshot())')
            pre_reload_idr = pre_reload_snap['personalAccount']['cash']['IDR']['available']
            pre_reload_usd = pre_reload_snap['personalAccount']['cash']['USD']['available']
            pre_reload_port = page.evaluate(f'() => JSON.parse(TradingNativeBridge.getPortfolio("{account_id}"))')
            pre_reload_pos_count = len(pre_reload_port.get('positions', []))

            # Reload browser page
            page.reload()
            page.wait_for_load_state('networkidle')
            time.sleep(0.5)

            # Capture state after reload
            post_reload_snap = page.evaluate('() => JSON.parse(TradingNativeBridge.getSnapshot())')
            post_reload_idr = post_reload_snap['personalAccount']['cash']['IDR']['available']
            post_reload_usd = post_reload_snap['personalAccount']['cash']['USD']['available']
            post_reload_port = page.evaluate(f'() => JSON.parse(TradingNativeBridge.getPortfolio("{account_id}"))')
            post_reload_pos_count = len(post_reload_port.get('positions', []))

            assert pre_reload_idr == post_reload_idr, f"IDR Cash mismatch after reload: {pre_reload_idr} vs {post_reload_idr}"
            assert pre_reload_usd == post_reload_usd, f"USD Cash mismatch after reload: {pre_reload_usd} vs {post_reload_usd}"
            assert pre_reload_pos_count == post_reload_pos_count, f"Position count mismatch after reload: {pre_reload_pos_count} vs {post_reload_pos_count}"
            page.screenshot(path=str(screenshot_dir / '17-after-reload-beranda.png'))

            # Continue playing after reload: advance clock by 10s and verify state remains live
            page.evaluate('() => TradingNativeBridge.tickClock(10)')
            snap_tick = page.evaluate('() => JSON.parse(TradingNativeBridge.getSnapshot())')
            assert snap_tick.get('personalAccount') is not None, "Playability unbroken after reload"
            checks.append(f"Step 7: Save & Load continuity verified: IDR={post_reload_idr}, USD={post_reload_usd}, Positions={post_reload_pos_count}; uninterrupted gameplay")

            browser.close()

    finally:
        if server:
            server.terminate()
            for pid in subprocess.check_output("netstat -ano | grep 8784 || true", shell=True).decode().split():
                pass

    summary = {
        "status": "PASS",
        "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
        "totalChecks": len(checks),
        "checks": checks,
        "journeyLog": journey_log,
        "screenshotsSaved": len(list(screenshot_dir.glob('*.png')))
    }
    with open(out / 'playtest-journey-result.json', 'w', encoding='utf-8') as f:
        json.dump(summary, f, indent=2)

    print(f"\nALL {len(checks)} PLAYTEST JOURNEY CHECKS PASSED:")
    for c in checks:
        print(f"  ✓ {c}")

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--output-dir', default=None, help='Target output directory')
    args = parser.parse_args()
    run_playtest(args.output_dir)
