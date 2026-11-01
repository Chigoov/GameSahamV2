import os
import sys
import subprocess
import json
from pathlib import Path

ROOT = Path("C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM")
SRC_DIR = ROOT / "Artifacts/Collection-Art-150-2026-10-22/images"
DEST_DIR = ROOT / "android/app/src/main/assets/www/images/collections"
DEST_DIR.mkdir(parents=True, exist_ok=True)

catalog_file = ROOT / "android/app/src/main/assets/data/katalog_koleksi_150.json"
with open(catalog_file, 'r', encoding='utf-8') as f:
    items = json.load(f)

print(f"Memproses {len(items)} aset koleksi...")
success_count = 0

for item in items:
    item_id = item['id']
    src_img = SRC_DIR / f"{item_id}.png"
    dest_webp = DEST_DIR / f"{item_id}.webp"
    dest_png = DEST_DIR / f"{item_id}.png"
    
    if not src_img.exists():
        print(f"Peringatan: {src_img} tidak ditemukan!")
        continue
        
    # Scale to 320x320 with high quality libwebp
    if not dest_webp.exists() or dest_webp.stat().st_size == 0:
        cmd_webp = [
            "ffmpeg", "-y", "-i", str(src_img),
            "-vf", "scale=320:320",
            "-c:v", "libwebp", "-lossless", "0", "-q:v", "88",
            str(dest_webp)
        ]
        subprocess.run(cmd_webp, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        
    # Also generate compact PNG fallback
    if not dest_png.exists() or dest_png.stat().st_size == 0:
        cmd_png = [
            "ffmpeg", "-y", "-i", str(src_img),
            "-vf", "scale=320:320",
            "-c:v", "png",
            str(dest_png)
        ]
        subprocess.run(cmd_png, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

    success_count += 1

print(f"Selesai! {success_count}/{len(items)} gambar koleksi teroptimasi WebP & PNG terpasang di assets.")
total_bytes = sum(f.stat().st_size for f in DEST_DIR.glob("*.webp"))
print(f"Total ukuran WebP: {total_bytes / 1024 / 1024:.2f} MB")
