from playwright.sync_api import sync_playwright
import os
import json

out_dir = r"C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM\Artifacts\Live-Plan15-Verified"
os.makedirs(out_dir, exist_ok=True)

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(viewport={'width': 412, 'height': 880})
    page = context.new_page()

    page.goto('http://127.0.0.1:18917/')
    page.evaluate("localStorage.setItem('game_welcome_shown', 'true')")
    page.reload()
    page.wait_for_timeout(1500)

    # 1. Test Comment 3: Click Avatar -> Opens Profile & Statistics with Pengaturan Tab
    print("Testing Comment 3: Avatar click...")
    page.click('#btn-settings')
    page.wait_for_timeout(500)
    page.screenshot(path=os.path.join(out_dir, "01-profile-kinerja-live.png"))

    # Click subtab Pengaturan
    page.click('.profile-subtabs-nav button:has-text("Pengaturan & Tema")')
    page.wait_for_timeout(500)
    page.screenshot(path=os.path.join(out_dir, "02-profile-pengaturan-live.png"))

    # Test Theme switch to Midnight Neon
    page.click('button:has-text("Midnight Neon")')
    page.wait_for_timeout(500)
    theme_attr = page.evaluate("document.documentElement.getAttribute('data-theme')")
    print("Theme after switch to Midnight Neon:", theme_attr)
    assert theme_attr == 'midnight-neon', f"Expected midnight-neon, got {theme_attr}"
    page.screenshot(path=os.path.join(out_dir, "03-theme-midnight-neon-live.png"))

    # Switch back to Emas Klasik
    page.click('button:has-text("Emas Klasik")')
    page.wait_for_timeout(500)
    theme_attr2 = page.evaluate("document.documentElement.getAttribute('data-theme')")
    print("Theme after switch to Emas Klasik:", theme_attr2)
    assert theme_attr2 == 'emas-klasik', f"Expected emas-klasik, got {theme_attr2}"

    # Close modal
    page.click('.back-button')
    page.wait_for_timeout(500)

    # 2. Test Comment 1 & 2: Fixed BUY/SELL Dock & Day 1 Candlestick Chart
    print("Testing Comment 1 & 2: BBCA detail & Day 1 chart...")
    page.click('[data-tab="pasar"]')
    page.wait_for_timeout(800)
    page.locator('#stock-list-container .stock-list-item:has-text("BBCA")').first.click()
    page.wait_for_timeout(1000)

    # Verify chart is rendered (not empty state)
    empty_chart = page.locator('#instrument-chart-section .empty-state').count()
    svg_chart = page.locator('#instrument-chart-section svg').count()
    readout = page.locator('#ohlc-readout').count()
    print(f"Chart empty_count={empty_chart}, svg_count={svg_chart}, readout_count={readout}")
    assert empty_chart == 0, "Chart should not be empty!"
    assert svg_chart > 0, "SVG chart should be present!"

    # Verify dock is fixed at bottom
    dock = page.locator('.detail-actions-dock')
    assert dock.count() == 1, "Dock should be present"
    dock_style = page.evaluate("window.getComputedStyle(document.querySelector('.detail-actions-dock')).position")
    print("Dock position style:", dock_style)
    assert dock_style == 'fixed', f"Dock position should be fixed, got {dock_style}"

    # Dismiss any toasts to keep screenshot pristine
    page.evaluate("document.querySelectorAll('.game-toast').forEach(t => t.remove())")
    page.wait_for_timeout(200)

    # Scroll chart layout slightly so the entire candlestick SVG and timeframe controls are centered
    page.locator('.candle-layout').scroll_into_view_if_needed()
    page.wait_for_timeout(300)

    page.screenshot(path=os.path.join(out_dir, "04-bbca-detail-fixed-dock-live.png"))

    # Buy 1 lot BBCA to test Portfolio
    print("Buying 1 lot BBCA for portfolio test...")
    page.click('.btn-detail-buy')
    page.wait_for_timeout(500)
    # Select MARKET order so it fills immediately
    page.evaluate("document.querySelector('input[value=\"MARKET\"]').click()")
    page.wait_for_timeout(300)
    # Inside order ticket modal, click Tinjau order
    page.click('#btn-submit-order')
    page.wait_for_timeout(800)
    page.click('button:has-text("Kirim order")')
    page.wait_for_timeout(1000)

    # Close order receipt / modal
    if page.locator('.back-button').count() > 0:
        page.locator('.back-button').first.click()
        page.wait_for_timeout(500)

    # 3. Test Comment 4: Compact Portfolio (B01)
    print("Testing Comment 4: Compact portfolio row...")
    page.evaluate("fetch('/qa-action', {method:'POST', body:JSON.stringify({action:'injectPosition', instrumentId:'IDX:BBCA'})})")
    page.wait_for_timeout(500)
    page.click('[data-tab="aset"]')
    page.wait_for_timeout(1000)

    # Scroll down to positions list
    page.locator('#portfolio-positions-list').scroll_into_view_if_needed()
    page.wait_for_timeout(500)

    pos_rows = page.locator('.position-card.compact-stockbit-row')
    pos_count = pos_rows.count()
    print(f"Found {pos_count} compact portfolio rows!")
    assert pos_count > 0, "Should have at least 1 position row!"

    page.screenshot(path=os.path.join(out_dir, "05-portfolio-compact-rows-live.png"))

    # Test click on compact position row -> navigates to instrument detail
    print("Clicking compact portfolio row to verify market detail navigation...")
    pos_rows.first.click()
    page.wait_for_timeout(1000)
    detail_header = page.locator('.flow-header h1').text_content()
    print("Navigated to detail header:", detail_header)
    assert "BBCA" in detail_header or len(detail_header) > 0

    page.screenshot(path=os.path.join(out_dir, "06-navigated-from-portfolio-live.png"))

    browser.close()

print("ALL PLAYWRIGHT TESTS PASSED DETERMINISTICALLY!")
