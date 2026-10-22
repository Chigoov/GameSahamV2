import csv, json
from pathlib import Path

csv_path = Path("Work/Owner-Revision-14-2026-10-22/KATALOG-KOLEKSI-150.csv")
out_path = Path("android/app/src/main/assets/data/katalog_koleksi_150.json")

items = []
with open(csv_path, encoding="utf-8-sig") as f:
    for row in csv.DictReader(f):
        items.append({
            "id": row["item_id"],
            "name": row["nama"],
            "category": row["kategori_id"],
            "categoryLabel": row["kategori"],
            "rarity": row["rarity"],
            "basePrice": int(row["harga_awal_game_idr"]),
            "dailyMaintenance": int(row["biaya_pemeliharaan_harian_game_idr"]),
            "reputationBonus": int(row["bonus_reputasi"]),
            "artBrief": row["art_brief"]
        })

out_path.parent.mkdir(parents=True, exist_ok=True)
with open(out_path, "w", encoding="utf-8") as f:
    json.dump(items, f, indent=2, ensure_ascii=False)

print(f"Generated {len(items)} items to {out_path}")
