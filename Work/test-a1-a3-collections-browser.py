"""Automated Playwright browser test for A1 & A3 Collections Gallery."""
from pathlib import Path
import os
import subprocess
import time
import json
from playwright.sync_api import sync_playwright

root = Path(__file__).resolve().parent.parent
run_dir = root / 'Artifacts/8-Anotasi-2026-10-22/run-01'
classes = run_dir / 'compile/classes'
qa_save = run_dir / 'qa-save'
evidence_dir = run_dir / 'evidence'
evidence_dir.mkdir(parents=True, exist_ok=True)

# Recompile WireQaServer
json_jar = next((Path.home() / '.gradle/caches/modules-2/files-2.1/org.json/json/20240303').rglob('*.jar'))
android_jar = Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk/platforms/android-36/android.jar'
cp = os.pathsep.join(map(str, [classes, json_jar, android_jar]))
jbin = Path('C:/Program Files/Android/Android Studio/jbr/bin')

print("Compiling WireQaServer.java...")
subprocess.run([
    str(jbin / 'javac.exe'), '-encoding', 'UTF-8', '-cp', cp, '-d', str(classes),
    str(root / 'Work/WireQaServer.java')
], check=True)

# Start QA server on port 8794
server_proc = subprocess.Popen([
    str(jbin / 'java.exe'),
    '-Dqa.port=8794',
    '-cp', cp,
    'com.tradingglobal3d.mobile.WireQaServer',
    str(root / 'android/app/src/main/assets'),
    str(qa_save),
    '8794'
], cwd=str(root))

print("Started WireQaServer on port 8794, PID:", server_proc.pid)
time.sleep(2)

try:
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(viewport={'width': 438, 'height': 627})
        page = context.new_page()

        print("Navigating to http://127.0.0.1:8794/...")
        page.goto('http://127.0.0.1:8794/')
        page.wait_for_selector('#btn-home-collections', timeout=10000)

        # 1. Buka Modal Galeri Koleksi
        print("Opening Collections Gallery modal...")
        page.click('#btn-home-collections')
        page.wait_for_selector('.collections-gallery-wrap', timeout=5000)

        # 2. Verifikasi A1: Dua bagian berjudul Belum Dimiliki dan Sudah Dimiliki
        unowned_sec = page.locator('.collection-section-group:has-text("Belum Dimiliki")')
        owned_sec = page.locator('.collection-section-group:has-text("Sudah Dimiliki")')
        assert unowned_sec.count() == 1, "Seksi Belum Dimiliki harus ada di tampilan Semua Status"
        assert owned_sec.count() == 1, "Seksi Sudah Dimiliki harus ada di tampilan Semua Status"

        unowned_badge = unowned_sec.locator('.badge').text_content().strip()
        owned_badge = owned_sec.locator('.badge').text_content().strip()
        print(f"Initial counts - Belum Dimiliki: {unowned_badge}, Sudah Dimiliki: {owned_badge}")
        assert unowned_badge == '12', f"Awalnya harus 12 belum dimiliki, aktual: {unowned_badge}"
        assert owned_badge == '0', f"Awalnya harus 0 sudah dimiliki, aktual: {owned_badge}"

        # 3. Verifikasi Filter Kategori
        page.click('button:has-text("🚗 Kendaraan")')
        time.sleep(0.5)
        unowned_cards = page.locator('.collection-item-card')
        assert unowned_cards.count() == 3, f"Kendaraan harus ada 3 item, aktual: {unowned_cards.count()}"

        # Kembali ke Semua Kategori
        page.click('button:has-text("Semua")')
        time.sleep(0.5)

        # 4. Beli satu koleksi (A3 verification)
        first_card = page.locator('.collection-item-card').first
        item_title = first_card.locator('h3').text_content().strip()
        buy_btn = first_card.locator('button.btn-primary:has-text("Beli Koleksi")')
        assert buy_btn.count() == 1, "Harus ada tombol Beli Koleksi"

        print(f"Buying item: {item_title}...")
        buy_btn.click()
        page.wait_for_selector('.toast-msg:has-text("Koleksi Berhasil Dibeli")', timeout=5000)
        time.sleep(1)

        # Verifikasi setelah beli: card berpindah ke Sudah Dimiliki
        unowned_sec_after = page.locator('.collection-section-group:has-text("Belum Dimiliki")')
        owned_sec_after = page.locator('.collection-section-group:has-text("Sudah Dimiliki")')
        unowned_cnt = unowned_sec_after.locator('.badge').text_content().strip()
        owned_cnt = owned_sec_after.locator('.badge').text_content().strip()
        print(f"After buy counts - Belum Dimiliki: {unowned_cnt}, Sudah Dimiliki: {owned_cnt}")
        assert unowned_cnt == '11', f"Harus tersisa 11 belum dimiliki, aktual: {unowned_cnt}"
        assert owned_cnt == '1', f"Harus ada 1 sudah dimiliki, aktual: {owned_cnt}"

        # Periksa kartu di Sudah Dimiliki memiliki badge ✓ Dimiliki dan tombol Jual Koleksi
        owned_card = owned_sec_after.locator('.collection-item-card.owned-luxury')
        assert owned_card.count() == 1, "Harus ada 1 kartu dengan class owned-luxury"
        assert owned_card.locator('.badge-owned:has-text("✓ Dimiliki")').count() == 1, "Harus memiliki badge ✓ Dimiliki"
        sell_btn = owned_card.locator('button.btn-secondary:has-text("Jual Koleksi")')
        assert sell_btn.count() == 1, "Harus ada tombol Jual Koleksi"

        # Tangkap screenshot bukti A1 & A3
        screenshot_path = evidence_dir / 'A1-A3-collections.png'
        page.screenshot(path=str(screenshot_path))
        print("Captured screenshot:", screenshot_path)

        # 5. Uji Filter Status "✓ Dimiliki"
        print("Testing filter '✓ Dimiliki'...")
        page.click('button:has-text("✓ Dimiliki")')
        time.sleep(0.5)
        filtered_owned = page.locator('.collection-item-card')
        assert filtered_owned.count() == 1, f"Filter Dimiliki harus tampil tepat 1 item, aktual: {filtered_owned.count()}"

        # 6. Uji Filter Status "○ Belum Dimiliki"
        print("Testing filter '○ Belum Dimiliki'...")
        page.click('button:has-text("○ Belum Dimiliki")')
        time.sleep(0.5)
        filtered_unowned = page.locator('.collection-item-card')
        assert filtered_unowned.count() == 11, f"Filter Belum Dimiliki harus tampil tepat 11 item, aktual: {filtered_unowned.count()}"

        # 7. Kembalikan ke Semua Status dan Jual kembali
        print("Selling item back...")
        page.click('button:has-text("Semua Status")')
        time.sleep(0.5)
        owned_card = page.locator('.collection-section-group:has-text("Sudah Dimiliki") .collection-item-card.owned-luxury')
        sell_btn = owned_card.locator('button.btn-secondary:has-text("Jual Koleksi")')
        sell_btn.click()
        page.wait_for_selector('.toast-msg:has-text("Koleksi Berhasil Terjual")', timeout=5000)
        time.sleep(1)

        # Verifikasi kembali ke 12 belum dimiliki dan 0 sudah dimiliki
        final_unowned_cnt = page.locator('.collection-section-group:has-text("Belum Dimiliki") .badge').text_content().strip()
        final_owned_cnt = page.locator('.collection-section-group:has-text("Sudah Dimiliki") .badge').text_content().strip()
        print(f"After sell counts - Belum Dimiliki: {final_unowned_cnt}, Sudah Dimiliki: {final_owned_cnt}")
        assert final_unowned_cnt == '12', "Setelah jual harus kembali 12 belum dimiliki"
        assert final_owned_cnt == '0', "Setelah jual harus kembali 0 sudah dimiliki"

        browser.close()
        print("ALL BROWSER PLAYWRIGHT CHECKS FOR A1 & A3 PASSED (PASS)!")

finally:
    server_proc.terminate()
    try:
        server_proc.wait(timeout=3)
    except:
        server_proc.kill()
    print("Cleaned up WireQaServer.")
