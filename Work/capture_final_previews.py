from playwright.sync_api import sync_playwright
import os

preview_dir = r"C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM\Artifacts\Live-Final-Previews"
os.makedirs(preview_dir, exist_ok=True)

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(viewport={'width': 412, 'height': 880})
    page = context.new_page()

    page.goto('http://127.0.0.1:18917/')
    page.evaluate("localStorage.setItem('game_welcome_shown', 'true')")
    page.reload()
    page.wait_for_timeout(1500)

    # 1. Beranda Live
    page.evaluate("document.querySelectorAll('.game-toast').forEach(t => t.remove())")
    page.screenshot(path=os.path.join(preview_dir, "01-preview-beranda.png"))
    print("01-preview-beranda.png captured")

    # 2. Detail Pasar BBCA (Grafik Candlestick Day 1 + Fixed BUY/SELL Dock)
    page.click('[data-tab="pasar"]')
    page.wait_for_timeout(800)
    page.locator('#stock-list-container .stock-list-item:has-text("BBCA")').first.click()
    page.wait_for_timeout(1000)
    page.evaluate("document.querySelectorAll('.game-toast').forEach(t => t.remove())")
    page.screenshot(path=os.path.join(preview_dir, "02-preview-detail-bbca.png"))
    print("02-preview-detail-bbca.png captured")

    # Close detail modal
    page.locator('.back-button').first.click()
    page.wait_for_timeout(500)

    # 3. Portofolio Ringkas (Inject BBCA position so compact row is shown)
    page.evaluate("fetch('/qa-action', {method:'POST', body:JSON.stringify({action:'injectPosition', instrumentId:'IDX:BBCA'})})")
    page.wait_for_timeout(500)
    page.click('[data-tab="aset"]')
    page.wait_for_timeout(1000)
    page.locator('#portfolio-positions-list').scroll_into_view_if_needed()
    page.wait_for_timeout(500)
    page.evaluate("document.querySelectorAll('.game-toast').forEach(t => t.remove())")
    page.screenshot(path=os.path.join(preview_dir, "03-preview-portofolio-ringkas.png"))
    print("03-preview-portofolio-ringkas.png captured")

    # 4. Profil & Pengaturan Tema (Avatar click -> Subtab Pengaturan & Tema)
    page.click('[data-tab="beranda"]')
    page.wait_for_timeout(500)
    page.click('#btn-settings')
    page.wait_for_timeout(500)
    page.click('.profile-subtabs-nav button:has-text("Pengaturan & Tema")')
    page.wait_for_timeout(500)
    page.evaluate("document.querySelectorAll('.game-toast').forEach(t => t.remove())")
    page.screenshot(path=os.path.join(preview_dir, "04-preview-pengaturan-tema.png"))
    print("04-preview-pengaturan-tema.png captured")

    browser.close()

print("All final previews captured successfully!")
