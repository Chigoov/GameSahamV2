#!/usr/bin/env python3
"""
Pengujian Verifikasi Baca/Tulis Database SQLite Lokal (Tahap 1).
Memvalidasi seluruh 8 tabel, integritas foreign key, query 150 galeri,
dan simulasi transaksi ledger serta portofolio.
"""

import sqlite3
import datetime
from pathlib import Path

DB_PATH = Path("C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/android/app/src/main/assets/data/gamesaham.db").resolve()

def run_tests():
    print(f"[*] Connecting to SQLite database at {DB_PATH}...")
    assert DB_PATH.exists(), f"Database file missing at {DB_PATH}"

    conn = sqlite3.connect(str(DB_PATH))
    conn.execute("PRAGMA foreign_keys = ON;")
    cursor = conn.cursor()

    # 1. Verifikasi Keberadaan 8 Tabel
    cursor.execute("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';")
    tables = {row[0] for row in cursor.fetchall()}
    print(f"[+] Found {len(tables)} tables: {sorted(tables)}")
    
    expected_tables = {
        "PlayerProfile", "StockLedger", "ActivePortfolio", "MarketCandleHistory",
        "BankLoan", "InvestmentFirm", "GalleryCatalog", "PlayerGalleryOwnership", "DailyQuests"
    }
    for t in expected_tables:
        assert t in tables, f"Missing expected table: {t}"
    print("[PASS] All 8 masterplan tables exist.")

    # 2. Verifikasi 150 Item di GalleryCatalog
    cursor.execute("SELECT COUNT(*) FROM GalleryCatalog;")
    cat_count = cursor.fetchone()[0]
    print(f"[+] Total items in GalleryCatalog: {cat_count}")
    assert cat_count == 150, f"Expected 150 items in GalleryCatalog, got {cat_count}"

    # Verifikasi sebaran kategori dan kelangkaan
    cursor.execute("SELECT Category, COUNT(*) FROM GalleryCatalog GROUP BY Category;")
    categories = cursor.fetchall()
    print(f"[+] Gallery categories count: {len(categories)}")
    assert len(categories) == 12, f"Expected 12 categories, got {len(categories)}"

    cursor.execute("SELECT Rarity, COUNT(*) FROM GalleryCatalog GROUP BY Rarity;")
    rarities = cursor.fetchall()
    print(f"[+] Gallery rarities: {dict(rarities)}")
    assert len(rarities) == 5, f"Expected 5 rarities, got {len(rarities)}"
    print("[PASS] 150 gallery items, 12 categories, 5 rarities verified.")

    # 3. Uji Tulis & Baca: PlayerProfile
    cursor.execute("SELECT PlayerId, PlayerName, CashBalance, CurrentLeague FROM PlayerProfile WHERE PlayerId='PLAYER-001';")
    player = cursor.fetchone()
    assert player is not None
    assert player[1] == "Trader Perdana"
    assert player[2] == 50000000.0
    print(f"[PASS] PlayerProfile query verified: {player}")

    # 4. Uji Tulis & Baca: StockLedger & ActivePortfolio (Simulasi Beli Saham)
    now_iso = datetime.datetime.now(datetime.timezone.utc).isoformat()
    tx_id = "TX-TEST-001"
    cursor.execute("""
        INSERT INTO StockLedger (TransactionId, Timestamp, Ticker, OrderSide, ExecutionPrice, LotQuantity, BrokerFee, NetTotalAmount)
        VALUES (?, ?, 'IDX:BBCA', 'BUY', 10000.0, 10, 15000.0, 10015000.0)
    """, (tx_id, now_iso))

    cursor.execute("""
        INSERT OR REPLACE INTO ActivePortfolio (Ticker, TotalLots, AverageBuyPrice, LastKnownPrice)
        VALUES ('IDX:BBCA', 10, 10000.0, 10000.0)
    """)

    cursor.execute("SELECT TotalLots, AverageBuyPrice FROM ActivePortfolio WHERE Ticker='IDX:BBCA';")
    pos = cursor.fetchone()
    assert pos[0] == 10 and pos[1] == 10000.0
    print("[PASS] StockLedger and ActivePortfolio CRUD verified.")

    # 5. Uji Tulis & Baca: MarketCandleHistory
    cursor.execute("""
        INSERT INTO MarketCandleHistory (Ticker, CandleTimestamp, OpenPrice, HighPrice, LowPrice, ClosePrice, Volume)
        VALUES ('IDX:BBCA', ?, 9900.0, 10100.0, 9850.0, 10050.0, 1250000)
    """, (now_iso,))
    cursor.execute("SELECT OpenPrice, ClosePrice, Volume FROM MarketCandleHistory WHERE Ticker='IDX:BBCA';")
    candle = cursor.fetchone()
    assert candle[0] == 9900.0 and candle[1] == 10050.0
    print("[PASS] MarketCandleHistory OHLCV insertion and retrieval verified.")

    # 6. Uji Tulis & Baca: BankLoan
    loan_id = "LOAN-TEST-001"
    cursor.execute("""
        INSERT INTO BankLoan (LoanId, PrincipalAmount, InterestRatePerSession, AccruedInterest, BorrowedAtSession, IsSettled)
        VALUES (?, 25000000.0, 0.015, 0.0, 1, 0)
    """, (loan_id,))
    cursor.execute("SELECT PrincipalAmount, InterestRatePerSession FROM BankLoan WHERE LoanId=?;", (loan_id,))
    loan = cursor.fetchone()
    assert loan[0] == 25000000.0 and loan[1] == 0.015
    print("[PASS] BankLoan insertion and retrieval verified.")

    # 7. Uji Tulis & Baca: PlayerGalleryOwnership (dengan FK constraint)
    ownership_id = "OWN-TEST-001"
    cursor.execute("""
        INSERT INTO PlayerGalleryOwnership (OwnershipId, ItemId, AcquiredAt, PurchasePrice, IsEquippedOrDisplayed)
        VALUES (?, 'COL-CAR-00', ?, 25000000.0, 1)
    """, (ownership_id, now_iso))
    cursor.execute("""
        SELECT o.OwnershipId, g.ItemName, o.PurchasePrice, g.Rarity
        FROM PlayerGalleryOwnership o
        JOIN GalleryCatalog g ON o.ItemId = g.ItemId
        WHERE o.OwnershipId=?;
    """, (ownership_id,))
    own_row = cursor.fetchone()
    assert own_row is not None
    assert own_row[1] == "Vespa Klasik 1965 Restorasi"
    print(f"[PASS] PlayerGalleryOwnership with JOIN verified: {own_row}")

    # Uji Foreign Key Violation (harus gagal)
    try:
        cursor.execute("""
            INSERT INTO PlayerGalleryOwnership (OwnershipId, ItemId, AcquiredAt, PurchasePrice, IsEquippedOrDisplayed)
            VALUES ('OWN-FAIL', 'NON-EXISTENT-ITEM', ?, 1000.0, 0)
        """, (now_iso,))
        assert False, "Foreign key constraint should have prevented invalid ItemId!"
    except sqlite3.IntegrityError:
        print("[PASS] Foreign key constraint successfully prevented invalid ItemId insertion.")

    # 8. Uji Tulis & Baca: DailyQuests
    cursor.execute("SELECT QuestId, Title, TargetGoal, CurrentProgress, IsClaimed FROM DailyQuests;")
    quests = cursor.fetchall()
    assert len(quests) >= 3
    print(f"[PASS] DailyQuests verified with {len(quests)} quests.")

    # Rollback data pengujian sementara agar DB tetap bersih
    conn.rollback()
    conn.close()
    print("\n=== ALL TAHAP 1 SQLITE TESTS PASSED 100% ===")

if __name__ == "__main__":
    run_tests()
