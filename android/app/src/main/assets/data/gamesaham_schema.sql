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

-- 9. Jadwal & Riwayat Dividen Emiten Saham (Fitur 3 Ekspansi)
CREATE TABLE IF NOT EXISTS StockDividends (
    DividendId TEXT PRIMARY KEY,
    Ticker TEXT NOT NULL,
    DividendPerShare DECIMAL(18, 4) NOT NULL,
    CumDateSession INTEGER NOT NULL,
    ExDateSession INTEGER NOT NULL,
    PaymentDateSession INTEGER NOT NULL,
    IsDistributed INTEGER NOT NULL DEFAULT 0
);

-- 10. Sistem Pertarungan Bos Taipan Pasar (Fitur 1 Ekspansi)
CREATE TABLE IF NOT EXISTS MarketBossShowdown (
    BossId TEXT PRIMARY KEY,
    BossName TEXT NOT NULL,
    DifficultyTier TEXT NOT NULL, -- Easy, Medium, Hard, Final
    AvatarImage TEXT NOT NULL,
    TargetReturnPercentage DECIMAL(6, 2) NOT NULL,
    DurationSessions INTEGER NOT NULL,
    BossCurrentReturn DECIMAL(6, 2) NOT NULL DEFAULT 0.0,
    PlayerCurrentReturn DECIMAL(6, 2) NOT NULL DEFAULT 0.0,
    IsActive INTEGER NOT NULL DEFAULT 0,
    IsDefeated INTEGER NOT NULL DEFAULT 0,
    RewardCash DECIMAL(18, 4) NOT NULL
);

-- 11. Buku Medali Prestasi Pemain (Fitur 5 Ekspansi)
CREATE TABLE IF NOT EXISTS PlayerAchievements (
    AchievementId TEXT PRIMARY KEY,
    Title TEXT NOT NULL,
    Description TEXT NOT NULL,
    Category TEXT NOT NULL, -- Trading, Wealth, Career, Collector
    TargetThreshold DECIMAL(18, 4) NOT NULL,
    CurrentProgress DECIMAL(18, 4) NOT NULL DEFAULT 0,
    IsUnlocked INTEGER NOT NULL DEFAULT 0,
    IsRewardClaimed INTEGER NOT NULL DEFAULT 0,
    RewardCash DECIMAL(18, 4) NOT NULL,
    PerkBonus TEXT -- e.g. 'FEE_DISCOUNT_1'
);

-- 12. Koleksi Avatar & Bingkai Profil (Fitur 6 Ekspansi)
CREATE TABLE IF NOT EXISTS ProfileCosmetics (
    CosmeticId TEXT PRIMARY KEY,
    CosmeticType TEXT NOT NULL, -- AVATAR / FRAME
    Name TEXT NOT NULL,
    ImagePath TEXT NOT NULL,
    Rarity TEXT NOT NULL,
    UnlockCondition TEXT NOT NULL,
    IsUnlocked INTEGER NOT NULL DEFAULT 0,
    IsEquipped INTEGER NOT NULL DEFAULT 0
);

-- 13. Kartu Booster Riset & Efisiensi Sementara (Fitur 7 Ekspansi)
CREATE TABLE IF NOT EXISTS ResearchBoosters (
    BoosterId TEXT PRIMARY KEY,
    BoosterType TEXT NOT NULL, -- NEWS_ACCURACY, FEE_DISCOUNT, MARGIN_EXPAND
    Title TEXT NOT NULL,
    Description TEXT NOT NULL,
    RemainingSessionsOrUses INTEGER NOT NULL,
    IsActive INTEGER NOT NULL DEFAULT 0,
    MultiplierValue DECIMAL(6, 2) NOT NULL
);

-- 14. Deposito Bank Berjangka Tenor Sesi & Penalti Bunga Hangus (Fase 3 Fitur 1)
CREATE TABLE IF NOT EXISTS TimeDeposits (
    DepositId TEXT PRIMARY KEY,
    PrincipalAmount DECIMAL(18, 4) NOT NULL,
    TenorSessions INTEGER NOT NULL, -- 10, 25, 50
    DailyInterestRate DECIMAL(6, 4) NOT NULL,
    BonusMultiplier DECIMAL(4, 2) NOT NULL,
    StartSession INTEGER NOT NULL,
    MaturitySession INTEGER NOT NULL,
    IsWithdrawnEarly INTEGER NOT NULL DEFAULT 0,
    IsMatured INTEGER NOT NULL DEFAULT 0,
    AccruedInterest DECIMAL(18, 4) NOT NULL DEFAULT 0
);

-- 15. Polis Asuransi Portofolio Lindung Nilai (Fase 3 Fitur 2)
CREATE TABLE IF NOT EXISTS PortfolioInsurancePolicy (
    PolicyId TEXT PRIMARY KEY,
    PurchasedSession INTEGER NOT NULL,
    ExpirySession INTEGER NOT NULL,
    CoveragePercentage DECIMAL(5, 2) NOT NULL DEFAULT 80.0,
    PremiumPaid DECIMAL(18, 4) NOT NULL,
    IsClaimed INTEGER NOT NULL DEFAULT 0
);

-- 16. Produk Reksa Dana Kustom Pemain (Fase 3 Fitur 11)
CREATE TABLE IF NOT EXISTS CustomMutualFunds (
    FundId TEXT PRIMARY KEY,
    FundName TEXT NOT NULL,
    EquityWeightPct DECIMAL(5, 2) NOT NULL,
    CommodityWeightPct DECIMAL(5, 2) NOT NULL,
    CryptoWeightPct DECIMAL(5, 2) NOT NULL,
    CashWeightPct DECIMAL(5, 2) NOT NULL,
    TotalAUM DECIMAL(18, 4) NOT NULL DEFAULT 0,
    ManagementFeePct DECIMAL(5, 2) NOT NULL DEFAULT 1.5,
    PerformanceFeePct DECIMAL(5, 2) NOT NULL DEFAULT 15.0
);

-- 17. Anak Perusahaan Induk Konglomerasi (Fase 3 Fitur 4)
CREATE TABLE IF NOT EXISTS HoldingSubsidiaries (
    SubsidiaryId TEXT PRIMARY KEY,
    SectorName TEXT NOT NULL,
    CompanyName TEXT NOT NULL,
    CapitalInjected DECIMAL(18, 4) NOT NULL DEFAULT 0,
    CurrentCash DECIMAL(18, 4) NOT NULL DEFAULT 0,
    CompanyValuation DECIMAL(18, 4) NOT NULL DEFAULT 0,
    RetainedEarnings DECIMAL(18, 4) NOT NULL DEFAULT 0
);

-- 18. Pasar Bakat Analis & Headhunting (Fase 3 Fitur 10)
CREATE TABLE IF NOT EXISTS StaffHeadhuntingMarket (
    CandidateId TEXT PRIMARY KEY,
    CandidateName TEXT NOT NULL,
    RoleTitle TEXT NOT NULL,
    SkillRating INTEGER NOT NULL,
    PoachCost DECIMAL(18, 4) NOT NULL,
    ExpectedSalary DECIMAL(18, 4) NOT NULL,
    CurrentFirmEmployer TEXT NOT NULL,
    LoyaltyScore DECIMAL(5, 2) NOT NULL DEFAULT 100.0,
    IsHiredByPlayer INTEGER NOT NULL DEFAULT 0
);

-- 19. Pohon Riset Teknologi Finansial Tech Tree (Fase 3 Fitur 7)
CREATE TABLE IF NOT EXISTS ResearchTechTreeProgress (
    NodeId TEXT PRIMARY KEY,
    NodeName TEXT NOT NULL,
    Category TEXT NOT NULL, -- ALGO, EFFICIENCY, SCALE
    TierLevel INTEGER NOT NULL,
    UpgradeCostCash DECIMAL(18, 4) NOT NULL,
    IsUnlocked INTEGER NOT NULL DEFAULT 0,
    StatBonusType TEXT NOT NULL,
    StatBonusValue DECIMAL(6, 2) NOT NULL
);

-- 20. Pelacak Jalur Disiplin Harian Streak (Fase 3 Fitur 8)
CREATE TABLE IF NOT EXISTS DailyStreakTracker (
    PlayerId TEXT PRIMARY KEY,
    CurrentStreak INTEGER NOT NULL DEFAULT 0,
    LongestStreak INTEGER NOT NULL DEFAULT 0,
    LastLoginSession INTEGER NOT NULL DEFAULT 0,
    IsStreakBroken INTEGER NOT NULL DEFAULT 0,
    RewardMultiplier DECIMAL(4, 2) NOT NULL DEFAULT 1.0
);

-- 21. Status Dominasi & Pengaruh Sektor Bursa (Fase 3 Fitur 9)
CREATE TABLE IF NOT EXISTS SectorDominanceStatus (
    SectorName TEXT PRIMARY KEY,
    PlayerOwnershipPct DECIMAL(5, 2) NOT NULL DEFAULT 0.0,
    IsDominantPlayer INTEGER NOT NULL DEFAULT 0,
    VetoPowerActive INTEGER NOT NULL DEFAULT 0,
    FeeDiscountPct DECIMAL(5, 2) NOT NULL DEFAULT 0.0
);
