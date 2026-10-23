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

    # 4. Inisialisasi Jadwal Dividen Saham Unggulan (Fitur 3 Ekspansi)
    dividends = [
        ('DIV-BBCA-2026', 'IDX:BBCA', 225.0000, 5, 6, 8, 0),
        ('DIV-BMRI-2026', 'IDX:BMRI', 330.0000, 10, 11, 14, 0),
        ('DIV-TLKM-2026', 'IDX:TLKM', 165.0000, 15, 16, 18, 0),
        ('DIV-ASII-2026', 'IDX:ASII', 420.0000, 20, 21, 25, 0),
        ('DIV-AAPL-2026', 'US:AAPL', 0.9500, 12, 13, 15, 0)
    ]
    cursor.executemany("""
        INSERT INTO StockDividends (DividendId, Ticker, DividendPerShare, CumDateSession, ExDateSession, PaymentDateSession, IsDistributed)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """, dividends)

    # 5. Inisialisasi 4 Bos Taipan Pasar (Fitur 1 Ekspansi)
    bosses = [
        ('BOSS-01', 'Taipan Lokal Pemula', 'Easy', 'kolay_patron.jpg', 5.0, 5, 0.0, 0.0, 1, 0, 10000000.0000),
        ('BOSS-02', 'Manajer Hedge Fund Elit', 'Medium', 'orta_patron.jpg', 12.0, 7, 0.0, 0.0, 0, 0, 25000000.0000),
        ('BOSS-03', 'Konglomerat Multi-Industri', 'Hard', 'zor_patron.png', 25.0, 10, 0.0, 0.0, 0, 0, 75000000.0000),
        ('BOSS-04', 'Sang Penguasa Pasar Modal', 'Final', 'final_boss.png', 45.0, 12, 0.0, 0.0, 0, 0, 250000000.0000)
    ]
    cursor.executemany("""
        INSERT INTO MarketBossShowdown (BossId, BossName, DifficultyTier, AvatarImage, TargetReturnPercentage, DurationSessions, BossCurrentReturn, PlayerCurrentReturn, IsActive, IsDefeated, RewardCash)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, bosses)

    # 6. Inisialisasi Buku Medali Prestasi (Fitur 5 Ekspansi)
    achievements = [
        ('ACH-01', 'Langkah Perdana', 'Selesaikan transaksi beli saham pasar perdana pertamamu.', 'Trading', 1.0, 0.0, 0, 0, 1000000.0, 'NONE'),
        ('ACH-02', 'Panen Dividen', 'Kantongi dividen tunai akumulasi mencapai Rp 50.000.000.', 'Wealth', 50000000.0, 0.0, 0, 0, 15000000.0, 'DIVIDEND_TAX_DISCOUNT_5'),
        ('ACH-03', 'Raja Pembalikan Arah', 'Raih profit harian di atas 10% dalam 1 sesi perdagangan.', 'Trading', 10.0, 0.0, 0, 0, 25000000.0, 'BROKER_FEE_DISCOUNT_1'),
        ('ACH-04', 'Penakluk Taipan', 'Kalahkan Bos Taipan Pasar tingkat Sulit dalam duel 1-on-1.', 'Career', 1.0, 0.0, 0, 0, 50000000.0, 'PRESTIGE_BADGE_BOSS'),
        ('ACH-05', 'Kolektor Sultan', 'Miliki minimal 10 barang koleksi mewah di galeri pribadimu.', 'Collector', 10.0, 0.0, 0, 0, 35000000.0, 'COLLATERAL_BONUS_10')
    ]
    cursor.executemany("""
        INSERT INTO PlayerAchievements (AchievementId, Title, Description, Category, TargetThreshold, CurrentProgress, IsUnlocked, IsRewardClaimed, RewardCash, PerkBonus)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, achievements)

    # 7. Inisialisasi Avatar & Bingkai Profil (Fitur 6 Ekspansi)
    cosmetics = [
        ('AVATAR-01', 'AVATAR', 'Trader Muda Ambisius', 'avatar_trader_muda.png', 'UMUM', 'Tersedia awal', 1, 1),
        ('AVATAR-02', 'AVATAR', 'Analis Senior Wall St', 'avatar_analis_senior.png', 'TIDAK_UMUM', 'Capai Level 4', 0, 0),
        ('AVATAR-03', 'AVATAR', 'Hedge Fund Maestro', 'avatar_fund_maestro.png', 'LANGKA', 'Capai Level 7', 0, 0),
        ('AVATAR-04', 'AVATAR', 'Taipan Sultan Nusantara', 'avatar_taipan_sultan.png', 'LEGENDARIS', 'Kalahkan Final Boss', 0, 0),
        ('FRAME-01', 'FRAME', 'Baja Klasik Halus', 'frame_steel.png', 'UMUM', 'Tersedia awal', 1, 1),
        ('FRAME-02', 'FRAME', 'Perunggu Juara Liga', 'frame_bronze_champ.png', 'TIDAK_UMUM', 'Promosi Liga Perak', 0, 0),
        ('FRAME-03', 'FRAME', 'Emas Manajer Konglomerat', 'frame_gold_tycoon.png', 'LANGKA', 'Promosi Liga Emas', 0, 0),
        ('FRAME-04', 'FRAME', 'Naga Emas Kaisar', 'frame_emperor_dragon.png', 'LEGENDARIS', 'Raih Kekayaan 1 Miliar', 0, 0)
    ]
    cursor.executemany("""
        INSERT INTO ProfileCosmetics (CosmeticId, CosmeticType, Name, ImagePath, Rarity, UnlockCondition, IsUnlocked, IsEquipped)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """, cosmetics)

    conn.commit()
    conn.close()
    print("[+] Database initialization and initial seeding complete!")

if __name__ == "__main__":
    init_database()
