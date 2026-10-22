import csv, json
from pathlib import Path

csv_path = Path("Work/Owner-Revision-14-2026-10-22/KATALOG-REKSA-DANA-24.csv")
out_path = Path("android/app/src/main/assets/data/katalog_reksadana_24.json")

items = []
with open(csv_path, encoding="utf-8-sig") as f:
    for row in csv.DictReader(f):
        items.append({
            "id": row["fund_id"],
            "name": row["nama"],
            "family": row["keluarga"],
            "enginePolicy": row["engine_policy"],
            "status": row["status"],
            "manager": row["pengelola_game"],
            "currency": row["mata_uang_unit"],
            "riskScore": int(row["skor_risiko_game_1_7"]),
            "stockPct": int(row["saham_pct"]),
            "bondPct": int(row["surat_utang_pct"]),
            "cashPct": int(row["kas_kontrak_pct"]),
            "managementFeeBps": int(row["management_fee_annual_bps_proposed"]),
            "custodyFeeBps": int(row["custody_fee_annual_bps_proposed"]),
            "minBuyIdr": int(row["minimum_beli_game_idr_proposed"]),
            "policy": row["kebijakan"]
        })

out_path.parent.mkdir(parents=True, exist_ok=True)
with open(out_path, "w", encoding="utf-8") as f:
    json.dump(items, f, indent=2, ensure_ascii=False)

print(f"Generated {len(items)} funds to {out_path}")
