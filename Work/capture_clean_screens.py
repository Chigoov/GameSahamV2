from playwright.sync_api import sync_playwright
import os

out_dir = r"C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM\Artifacts\Audit-Executed-Screens"
os.makedirs(out_dir, exist_ok=True)

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(viewport={'width': 412, 'height': 880})
    page = context.new_page()

    page.goto('http://127.0.0.1:18917/')
    page.evaluate("localStorage.setItem('game_welcome_shown', 'true')")
    page.reload()
    page.wait_for_timeout(1500)

    # 1. Clean Beranda
    page.evaluate("document.querySelectorAll('.game-toast').forEach(t => t.remove())")
    page.screenshot(path=os.path.join(out_dir, "01-clean-beranda-live.png"))
    print("01-clean-beranda-live.png captured")

    # 2. Clean Pasar (Scroll down to stock list & filter chips)
    page.click('[data-tab="pasar"]')
    page.wait_for_timeout(800)
    page.locator('.market-filter-card').scroll_into_view_if_needed()
    page.wait_for_timeout(500)
    page.evaluate("document.querySelectorAll('.game-toast').forEach(t => t.remove())")
    page.screenshot(path=os.path.join(out_dir, "02-clean-pasar-live.png"))
    print("02-clean-pasar-live.png captured")

    # 3. Clean Portofolio
    page.evaluate("fetch('/qa-action', {method:'POST', body:JSON.stringify({action:'injectPosition', instrumentId:'IDX:BBCA'})})")
    page.wait_for_timeout(500)
    page.click('[data-tab="aset"]')
    page.wait_for_timeout(1000)
    page.locator('#portfolio-positions-list').scroll_into_view_if_needed()
    page.wait_for_timeout(500)
    page.evaluate("document.querySelectorAll('.game-toast').forEach(t => t.remove())")
    page.screenshot(path=os.path.join(out_dir, "03-clean-portofolio-live.png"))
    print("03-clean-portofolio-live.png captured")

    # 4. Clean Detail BBCA
    page.click('[data-tab="pasar"]')
    page.wait_for_timeout(500)
    page.locator('#stock-list-container .stock-list-item:has-text("BBCA")').first.click()
    page.wait_for_timeout(1000)
    page.evaluate("document.querySelectorAll('.game-toast').forEach(t => t.remove())")
    page.screenshot(path=os.path.join(out_dir, "04-clean-detail-bbca-live.png"))
    print("04-clean-detail-bbca-live.png captured")

    browser.close()

print("All executed clean screens captured successfully!")
