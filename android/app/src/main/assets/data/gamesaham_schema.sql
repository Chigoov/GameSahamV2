-- SKEMA DATABASE SQLITE LOKAL: 15 FITUR GAME SAHAM 2D PORTRAIT
-- Sesuai Dokumen Docs/MASTERPLAN_15_FITUR_2D_PORTRAIT.md

PRAGMA foreign_keys = ON;

-- 1. Profil Pemain & Karier
CREATE TABLE IF NOT EXISTS PlayerProfile (
    PlayerId TEXT PRIMARY KEY,
    PlayerName TEXT NOT NULL,
    DifficultyMode TEXT NOT NULL DEFAULT 'Casual',
    CurrentLeague TEXT NOT NULL DEFAULT 'Junior',
    PrestigeLevel INTEGER NOT NULL DEFAULT 0,
    CashBalance DECIMAL(18, 4) NOT NULL,
    TotalDebt DECIMAL(18, 4) NOT NULL DEFAULT 0,
    InvestorArchetype TEXT DEFAULT 'Unknown',
    FirmReputation INTEGER NOT NULL DEFAULT 0,
    CreatedAt DATETIME NOT NULL
);

-- 2. Buku Transaksi Saham (Immutable Ledger)
CREATE TABLE IF NOT EXISTS StockLedger (
    TransactionId TEXT PRIMARY KEY,
    Timestamp DATETIME NOT NULL,
    Ticker TEXT NOT NULL,
    OrderSide TEXT NOT NULL, -- BUY / SELL
    ExecutionPrice DECIMAL(18, 4) NOT NULL,
    LotQuantity INTEGER NOT NULL,
    BrokerFee DECIMAL(18, 4) NOT NULL,
    NetTotalAmount DECIMAL(18, 4) NOT NULL
);

-- 3. Portofolio Saham Aktif
CREATE TABLE IF NOT EXISTS ActivePortfolio (
    Ticker TEXT PRIMARY KEY,
    TotalLots INTEGER NOT NULL,
    AverageBuyPrice DECIMAL(18, 4) NOT NULL,
    LastKnownPrice DECIMAL(18, 4) NOT NULL
);

-- 4. Riwayat Lilin Harga (Candlestick OHLCV)
CREATE TABLE IF NOT EXISTS MarketCandleHistory (
    Ticker TEXT NOT NULL,
    CandleTimestamp DATETIME NOT NULL,
    OpenPrice DECIMAL(18, 4) NOT NULL,
    HighPrice DECIMAL(18, 4) NOT NULL,
    LowPrice DECIMAL(18, 4) NOT NULL,
    ClosePrice DECIMAL(18, 4) NOT NULL,
    Volume INTEGER NOT NULL,
    PRIMARY KEY (Ticker, CandleTimestamp)
);

-- 5. Pinjaman Bank & Fasilitas Margin
CREATE TABLE IF NOT EXISTS BankLoan (
    LoanId TEXT PRIMARY KEY,
    PrincipalAmount DECIMAL(18, 4) NOT NULL,
    InterestRatePerSession DECIMAL(6, 4) NOT NULL,
    AccruedInterest DECIMAL(18, 4) NOT NULL DEFAULT 0,
    BorrowedAtSession INTEGER NOT NULL,
    IsSettled INTEGER NOT NULL DEFAULT 0
);

-- 6. Firma Pengelola Dana & Karyawan
CREATE TABLE IF NOT EXISTS InvestmentFirm (
    FirmLevel INTEGER PRIMARY KEY,
    FirmName TEXT NOT NULL,
    TotalAUM DECIMAL(18, 4) NOT NULL DEFAULT 0,
    TotalStaffHired INTEGER NOT NULL DEFAULT 0,
    HappinessScore DECIMAL(5, 2) NOT NULL DEFAULT 100.0,
    DailyManagementFee DECIMAL(18, 4) NOT NULL DEFAULT 0
);

-- 7. Katalog & Kepemilikan 150 Galeri Aset Mewah
CREATE TABLE IF NOT EXISTS GalleryCatalog (
    ItemId TEXT PRIMARY KEY,
    ItemName TEXT NOT NULL,
    Category TEXT NOT NULL,
    Rarity TEXT NOT NULL, -- UMUM, TIDAK_UMUM, LANGKA, EPIK, LEGENDARIS
    BasePrice DECIMAL(18, 4) NOT NULL,
    ImagePath TEXT NOT NULL,
    ReputationBonus INTEGER NOT NULL DEFAULT 10
);

CREATE TABLE IF NOT EXISTS PlayerGalleryOwnership (
    OwnershipId TEXT PRIMARY KEY,
    ItemId TEXT NOT NULL,
    AcquiredAt DATETIME NOT NULL,
    PurchasePrice DECIMAL(18, 4) NOT NULL,
    IsEquippedOrDisplayed INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY(ItemId) REFERENCES GalleryCatalog(ItemId)
);

-- 8. Misi Harian & Checklist
CREATE TABLE IF NOT EXISTS DailyQuests (
    QuestId TEXT PRIMARY KEY,
    Title TEXT NOT NULL,
    TargetGoal INTEGER NOT NULL,
    CurrentProgress INTEGER NOT NULL DEFAULT 0,
    RewardCash DECIMAL(18, 4) NOT NULL,
    IsClaimed INTEGER NOT NULL DEFAULT 0
);
