#!/usr/bin/env python3
"""
Inisialisasi Database SQLite Lokal & Seeder 150 Galeri Mewah.
Sesuai Dokumen Docs/MASTERPLAN_15_FITUR_2D_PORTRAIT.md (Tahap 1).
"""

import csv
import os
import sqlite3
import datetime
from pathlib import Path

ROOT = Path("C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM").resolve()
DB_PATH = ROOT / "android/app/src/main/assets/data/gamesaham.db"
SCHEMA_PATH = ROOT / "android/app/src/main/assets/data/gamesaham_schema.sql"
MANIFEST_PATH = ROOT / "Work/Collection-Art-150-2026-10-22/MANIFEST.csv"
KATALOG_PATH = ROOT / "Work/Owner-Revision-14-2026-10-22/KATALOG-KOLEKSI-150.csv"

def init_database():
    print(f"[*] Initializing SQLite database at: {DB_PATH}")
    if DB_PATH.exists():
        DB_PATH.unlink()
    DB_PATH.parent.mkdir(parents=True, exist_ok=True)

    conn = sqlite3.connect(str(DB_PATH))
    cursor = conn.cursor()

    # 1. Terapkan Skema 8 Tabel
    schema_sql = SCHEMA_PATH.read_text(encoding="utf-8")
    cursor.executescript(schema_sql)
    print("[+] Successfully executed 8-table DDL schema.")

    # 2. Baca Katalog dan Manifest 150 Item
    catalog_meta = {}
    with open(KATALOG_PATH, encoding="utf-8-sig") as f:
        for row in csv.DictReader(f):
            catalog_meta[row["item_id"]] = {
                "base_price": float(row["harga_awal_game_idr"]),
                "rep_bonus": int(row["bonus_reputasi"]),
                "kategori_id": row["kategori_id"],
                "kategori_label": row["kategori"],
                "rarity": row["rarity"]
            }

    manifest_rows = []
    with open(MANIFEST_PATH, encoding="utf-8-sig") as f:
        for row in csv.DictReader(f):
            manifest_rows.append(row)

    print(f"[*] Loaded {len(manifest_rows)} manifest rows and {len(catalog_meta)} catalog rows.")
    assert len(manifest_rows) == 150, f"Expected 150 manifest items, got {len(manifest_rows)}"

    # 3. Insert ke GalleryCatalog
    inserted_count = 0
    for row in manifest_rows:
        item_id = row["item_id"]
        item_name = row["nama"]
        meta = catalog_meta.get(item_id)
        if not meta:
            raise ValueError(f"Metadata missing for item: {item_id}")

        category = meta["kategori_label"]
        rarity = meta["rarity"]
        base_price = meta["base_price"]
        rep_bonus = meta["rep_bonus"]
        image_path = f"images/{item_id}.png"

        cursor.execute("""
            INSERT INTO GalleryCatalog (ItemId, ItemName, Category, Rarity, BasePrice, ImagePath, ReputationBonus)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """, (item_id, item_name, category, rarity, base_price, image_path, rep_bonus))
        inserted_count += 1

    print(f"[+] Successfully seeded {inserted_count} items into GalleryCatalog.")

    # 4. Inisialisasi Data Awal (PlayerProfile, InvestmentFirm, DailyQuests)
    now_iso = datetime.datetime.now(datetime.timezone.utc).isoformat()
    
    # Default Player Profile
    cursor.execute("""
        INSERT INTO PlayerProfile (PlayerId, PlayerName, DifficultyMode, CurrentLeague, PrestigeLevel, CashBalance, TotalDebt, InvestorArchetype, FirmReputation, CreatedAt)
        VALUES ('PLAYER-001', 'Trader Perdana', 'Casual', 'Junior', 0, 50000000.0000, 0.0000, 'Gaya Sedang Terbentuk', 10, ?)
    """, (now_iso,))

    # Default Investment Firm Level 1
    cursor.execute("""
        INSERT INTO InvestmentFirm (FirmLevel, FirmName, TotalAUM, TotalStaffHired, HappinessScore, DailyManagementFee)
        VALUES (1, 'Nusantara Capital Management', 0.0000, 1, 100.00, 0.0000)
    """, ())

    # 3 Daily Quests Awal
    quests = [
        ('QUEST-01', 'Beli minimal 1 lot saham emiten perbankan', 1, 0, 2500000.0000, 0),
        ('QUEST-02', 'Lakukan diversifikasi ke 2 sektor saham berbeda', 2, 0, 5000000.0000, 0),
        ('QUEST-03', 'Klaim dana hibah modal harian', 1, 0, 1000000.0000, 0)
    ]
    cursor.executemany("""
        INSERT INTO DailyQuests (QuestId, Title, TargetGoal, CurrentProgress, RewardCash, IsClaimed)
        VALUES (?, ?, ?, ?, ?, ?)
    """, quests)

    conn.commit()
    conn.close()
    print("[+] Database initialization and initial seeding complete!")

if __name__ == "__main__":
    init_database()
