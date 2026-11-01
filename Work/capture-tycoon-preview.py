import os
from pathlib import Path
from playwright.sync_api import sync_playwright

ROOT = Path("C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM").resolve()
OUT_DIR = ROOT / "Artifacts/Longtask-APK11-2026-10-28/screenshots"
OUT_DIR.mkdir(parents=True, exist_ok=True)

URL = "http://127.0.0.1:18917/Artifacts/Longtask-APK11-2026-10-28/preview-tycoon-sinematik-lengkap.html"

print(f"[*] Capturing Tycoon Sinematik preview screens from {URL} to {OUT_DIR}...")

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    # Gunakan viewport landscape/portrait modern
    context = browser.new_context(viewport={'width': 820, 'height': 900}, device_scale_factor=1.5)
    page = context.new_page()

    page.goto(URL)
    page.wait_for_load_state('networkidle')
    page.wait_for_timeout(500)

    # 1. Beranda
    print("[1/6] Capturing Beranda...")
    page.screenshot(path=str(OUT_DIR / "01-beranda-tycoon.png"))

    # 2. Pasar
    print("[2/6] Capturing Pasar...")
    page.click('button:has-text("2. Pasar")')
    page.wait_for_timeout(300)
    page.screenshot(path=str(OUT_DIR / "02-pasar-tycoon.png"))

    # 3. Portofolio
    print("[3/6] Capturing Portofolio...")
    page.click('button:has-text("3. Portofolio")')
    page.wait_for_timeout(300)
    page.screenshot(path=str(OUT_DIR / "03-portofolio-tycoon.png"))

    # 4. Bisnis
    print("[4/6] Capturing Bisnis...")
    page.click('button:has-text("4. Bisnis")')
    page.wait_for_timeout(300)
    page.screenshot(path=str(OUT_DIR / "04-bisnis-tycoon.png"))

    # 5. Lainnya (Terminal Satu Pintu)
    print("[5/6] Capturing Lainnya (Terminal Satu Pintu)...")
    page.click('button:has-text("5. Lainnya (Satu Pintu)")')
    page.wait_for_timeout(300)
    page.screenshot(path=str(OUT_DIR / "05-terminal-satu-pintu.png"))

    # 6. Galeri 3 Opsi
    print("[6/6] Capturing 3 Alternatif Desain Galeri...")
    page.click('button:has-text("💎 3 Opsi Galeri")')
    page.wait_for_timeout(300)
    page.screenshot(path=str(OUT_DIR / "06-galeri-3-opsi.png"))

    browser.close()

print("[✓] All 6 preview screenshots captured successfully!")
