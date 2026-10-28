from playwright.sync_api import sync_playwright
from pathlib import Path
import time

ROOT = Path("C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM").resolve()
OUT_DIR = ROOT / "Artifacts/Live-Fase3-Preview-2026-10-23"
OUT_DIR.mkdir(parents=True, exist_ok=True)
URL = "http://127.0.0.1:18917/"

print(f"[*] Capturing live preview from {URL} to {OUT_DIR}...")

with sync_playwright() as p:
    browser = p.chromium.launch(
        headless=True,
        args=['--disable-dev-shm-usage', '--no-sandbox', '--disable-gpu']
    )
    context = browser.new_context(viewport={'width': 412, 'height': 860})
    page = context.new_page()

    page.on('dialog', lambda d: d.accept())

    page.goto(URL)
    page.wait_for_load_state('networkidle')
    page.wait_for_timeout(1000)

    # 1. Beranda Utama (dengan tombol Duel Bos, Disiplin Harian, Dominasi Sektor)
    print("[1/9] Beranda Utama...")
    page.screenshot(path=str(OUT_DIR / "01-beranda-fase3.png"))

    # 2. Modal Jalur Disiplin Harian (Streak)
    print("[2/9] Modal Disiplin Harian...")
    page.click('#btn-home-streak')
    page.wait_for_timeout(600)
    page.screenshot(path=str(OUT_DIR / "02-modal-disiplin-streak.png"))
    page.click('.back-button')
    page.wait_for_timeout(300)

    # 3. Modal Dominasi Sektor Bursa
    print("[3/9] Modal Dominasi Sektor...")
    page.click('#btn-home-dominance')
    page.wait_for_timeout(600)
    page.screenshot(path=str(OUT_DIR / "03-modal-dominasi-sektor.png"))
    page.click('.back-button')
    page.wait_for_timeout(300)

    # 4. Modal Duel Bos Taipan Pasar
    print("[4/9] Modal Duel Bos Taipan...")
    page.click('#btn-market-boss')
    page.wait_for_timeout(600)
    page.screenshot(path=str(OUT_DIR / "04-modal-duel-bos-taipan.png"))
    page.click('.back-button')
    page.wait_for_timeout(300)

    # 5. Tab Portofolio -> Modal Asuransi Portofolio (Hedging)
    print("[5/9] Modal Asuransi Portofolio...")
    page.click('button.nav-item[data-tab="aset"]')
    page.wait_for_timeout(400)
    tools1 = page.locator('#asset-tools')
    if tools1.count():
        if not tools1.evaluate('el => el.open'):
            tools1.locator('summary').click()
    page.locator('#asset-tools button:has-text("Asuransi Portofolio (Hedging)")').click()
    page.wait_for_timeout(600)
    page.screenshot(path=str(OUT_DIR / "05-modal-asuransi-hedging.png"))
    page.click('.back-button')
    page.wait_for_timeout(300)

    # 6. Modal Meracik Reksa Dana Kustom
    print("[6/9] Modal Meracik Reksa Dana Kustom...")
    tools2 = page.locator('#asset-tools')
    if tools2.count():
        if not tools2.evaluate('el => el.open'):
            tools2.locator('summary').click()
    page.locator('#asset-tools button:has-text("Racik Reksa Dana Kustom")').click()
    page.wait_for_timeout(600)
    page.screenshot(path=str(OUT_DIR / "06-modal-racik-reksadana.png"))
    page.click('.back-button')
    page.wait_for_timeout(300)

    # 7. Tab Bisnis -> Kelola Dana -> Bursa Headhunting Staf Analis
    print("[7/9] Modal Bursa Headhunting...")
    page.click('button.nav-item[data-tab="firma"]')
    page.wait_for_timeout(400)
    page.click('#firma-subtab-dana')
    page.wait_for_timeout(400)
    page.locator('button:has-text("Bursa Headhunting Staf Analis Elit")').click()
    page.wait_for_timeout(600)
    page.screenshot(path=str(OUT_DIR / "07-modal-headhunting-staf.png"))
    page.click('.back-button')
    page.wait_for_timeout(300)

    # 8. Tab Lainnya -> Pohon Riset Finansial Tech Tree
    print("[8/9] Modal Pohon Riset Tech Tree...")
    page.click('button.nav-item[data-tab="riset"]')
    page.wait_for_timeout(400)
    page.locator('button:has-text("Pohon Riset Finansial Tech Tree")').click()
    page.wait_for_timeout(600)
    page.screenshot(path=str(OUT_DIR / "08-modal-pohon-riset-techtree.png"))
    page.click('.back-button')
    page.wait_for_timeout(300)

    # 9. Detail Candlestick BBCA Data Historis Nyata
    print("[9/9] Detail Candlestick BBCA...")
    page.click('button.nav-item[data-tab="pasar"]')
    page.wait_for_timeout(400)
    page.fill('#search-stock-input', 'BBCA')
    page.wait_for_timeout(300)
    page.locator('#stock-list-container .stock-list-item:has-text("BBCA")').first.click()
    page.wait_for_timeout(600)
    page.locator('.chart-controls').scroll_into_view_if_needed()
    page.wait_for_timeout(400)
    page.screenshot(path=str(OUT_DIR / "09-chart-candlestick-bbca.png"))

    browser.close()
    print("[+] All 9 live preview captures completed successfully!")
