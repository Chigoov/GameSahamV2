import os
from pathlib import Path
from playwright.sync_api import sync_playwright

ROOT = Path("C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM").resolve()
OUT_DIR = ROOT / "Artifacts/Overall-Preview-2026-10-23"
OUT_DIR.mkdir(parents=True, exist_ok=True)

URL = "http://127.0.0.1:18917/"

print(f"[*] Capturing overall game preview from {URL} to {OUT_DIR}...")

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(viewport={'width': 412, 'height': 860}, device_scale_factor=1)
    page = context.new_page()

    page.goto(URL)
    page.wait_for_load_state('networkidle')
    page.wait_for_timeout(1000)

    # 1. Tab Beranda
    print("[1/10] Capturing Tab Beranda...")
    page.screenshot(path=str(OUT_DIR / "01-tab-beranda.png"))

    # 2. Tab Pasar
    print("[2/10] Capturing Tab Pasar...")
    page.click('button.nav-item[data-tab="pasar"]')
    page.wait_for_timeout(500)
    page.screenshot(path=str(OUT_DIR / "02-tab-pasar.png"))

    # 3. Detail Saham BBCA & Order Book
    print("[3/10] Capturing Detail Saham BBCA & Order Book...")
    page.fill('#search-stock-input', 'BBCA')
    page.wait_for_timeout(300)
    bbca_row = page.locator('#stock-list-container .stock-list-item:has-text("BBCA")')
    if bbca_row.count():
        bbca_row.first.click()
        page.wait_for_timeout(600)
        page.screenshot(path=str(OUT_DIR / "03-detail-bbca-orderbook.png"))

        # 4. Tiket Order dengan Tombol Cepat 25%, 50%, 75%, MAX
        print("[4/10] Capturing Tiket Order & Quick Percentage...")
        page.click('button.btn-detail-buy')
        page.wait_for_timeout(500)
        page.screenshot(path=str(OUT_DIR / "04-tiket-order-quick-pct.png"))
        page.click('#btn-close-modal')
        page.wait_for_timeout(300)

        # Back from detail
        page.click('.back-button')
        page.wait_for_timeout(300)

    # 5. Tab Portofolio
    print("[5/10] Capturing Tab Portofolio...")
    page.click('button.nav-item[data-tab="aset"]')
    page.wait_for_timeout(500)
    page.screenshot(path=str(OUT_DIR / "05-tab-portofolio.png"))

    # 6. Galeri 150 Koleksi Mewah
    print("[6/10] Capturing Galeri 150 Koleksi...")
    col_btn = page.locator('#asset-summary button:has-text("Barang koleksi & properti mewah")')
    if col_btn.count():
        col_btn.click()
        page.wait_for_timeout(600)
        page.screenshot(path=str(OUT_DIR / "06-galeri-150-koleksi.png"))
        page.click('.back-button')
        page.wait_for_timeout(300)

    # 7. Tab Bisnis & Kelola Dana
    print("[7/10] Capturing Tab Bisnis...")
    page.click('button.nav-item[data-tab="firma"]')
    page.wait_for_timeout(500)
    page.screenshot(path=str(OUT_DIR / "07-tab-bisnis.png"))

    # 8. Misi Periodik 3 Tab
    print("[8/10] Capturing Misi Periodik...")
    page.click('button.nav-item[data-tab="beranda"]')
    page.wait_for_timeout(400)
    page.click('#btn-missions')
    page.wait_for_timeout(600)
    page.screenshot(path=str(OUT_DIR / "08-misi-periodik-3tabs.png"))
    page.click('.back-button')
    page.wait_for_timeout(300)

    # 9. DNA Gaya Investor
    print("[9/10] Capturing DNA Gaya Investor...")
    page.click('#btn-home-archetype')
    page.wait_for_timeout(600)
    page.screenshot(path=str(OUT_DIR / "09-dna-investor.png"))
    page.click('.back-button')
    page.wait_for_timeout(300)

    # 10. Tab Lainnya & Riset
    print("[10/10] Capturing Tab Lainnya...")
    page.click('button.nav-item[data-tab="riset"]')
    page.wait_for_timeout(500)
    page.screenshot(path=str(OUT_DIR / "10-tab-lainnya.png"))

    browser.close()
    print("[+] All 10 preview screenshots captured successfully!")
