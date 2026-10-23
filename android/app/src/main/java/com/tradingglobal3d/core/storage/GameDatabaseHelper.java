package com.tradingglobal3d.core.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Helper lokal untuk 15 Fitur Game Saham 2D Portrait.
 * Menyediakan penyimpanan mandiri (offline-first) dengan 8 tabel utama
 * sesuai Docs/MASTERPLAN_15_FITUR_2D_PORTRAIT.md.
 */
public final class GameDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "gamesaham.db";
    private static final int DATABASE_VERSION = 1;
    private final Context context;

    public GameDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Profil Pemain & Karier
        db.execSQL("CREATE TABLE IF NOT EXISTS PlayerProfile ("
                + "PlayerId TEXT PRIMARY KEY, "
                + "PlayerName TEXT NOT NULL, "
                + "DifficultyMode TEXT NOT NULL DEFAULT 'Casual', "
                + "CurrentLeague TEXT NOT NULL DEFAULT 'Junior', "
                + "PrestigeLevel INTEGER NOT NULL DEFAULT 0, "
                + "CashBalance DECIMAL(18, 4) NOT NULL, "
                + "TotalDebt DECIMAL(18, 4) NOT NULL DEFAULT 0, "
                + "InvestorArchetype TEXT DEFAULT 'Unknown', "
                + "FirmReputation INTEGER NOT NULL DEFAULT 0, "
                + "CreatedAt DATETIME NOT NULL"
                + ");");

        // 2. Buku Transaksi Saham (Immutable Ledger)
        db.execSQL("CREATE TABLE IF NOT EXISTS StockLedger ("
                + "TransactionId TEXT PRIMARY KEY, "
                + "Timestamp DATETIME NOT NULL, "
                + "Ticker TEXT NOT NULL, "
                + "OrderSide TEXT NOT NULL, "
                + "ExecutionPrice DECIMAL(18, 4) NOT NULL, "
                + "LotQuantity INTEGER NOT NULL, "
                + "BrokerFee DECIMAL(18, 4) NOT NULL, "
                + "NetTotalAmount DECIMAL(18, 4) NOT NULL"
                + ");");

        // 3. Portofolio Saham Aktif
        db.execSQL("CREATE TABLE IF NOT EXISTS ActivePortfolio ("
                + "Ticker TEXT PRIMARY KEY, "
                + "TotalLots INTEGER NOT NULL, "
                + "AverageBuyPrice DECIMAL(18, 4) NOT NULL, "
                + "LastKnownPrice DECIMAL(18, 4) NOT NULL"
                + ");");

        // 4. Riwayat Lilin Harga (Candlestick OHLCV)
        db.execSQL("CREATE TABLE IF NOT EXISTS MarketCandleHistory ("
                + "Ticker TEXT NOT NULL, "
                + "CandleTimestamp DATETIME NOT NULL, "
                + "OpenPrice DECIMAL(18, 4) NOT NULL, "
                + "HighPrice DECIMAL(18, 4) NOT NULL, "
                + "LowPrice DECIMAL(18, 4) NOT NULL, "
                + "ClosePrice DECIMAL(18, 4) NOT NULL, "
                + "Volume INTEGER NOT NULL, "
                + "PRIMARY KEY (Ticker, CandleTimestamp)"
                + ");");

        // 5. Pinjaman Bank & Fasilitas Margin
        db.execSQL("CREATE TABLE IF NOT EXISTS BankLoan ("
                + "LoanId TEXT PRIMARY KEY, "
                + "PrincipalAmount DECIMAL(18, 4) NOT NULL, "
                + "InterestRatePerSession DECIMAL(6, 4) NOT NULL, "
                + "AccruedInterest DECIMAL(18, 4) NOT NULL DEFAULT 0, "
                + "BorrowedAtSession INTEGER NOT NULL, "
                + "IsSettled INTEGER NOT NULL DEFAULT 0"
                + ");");

        // 6. Firma Pengelola Dana & Karyawan
        db.execSQL("CREATE TABLE IF NOT EXISTS InvestmentFirm ("
                + "FirmLevel INTEGER PRIMARY KEY, "
                + "FirmName TEXT NOT NULL, "
                + "TotalAUM DECIMAL(18, 4) NOT NULL DEFAULT 0, "
                + "TotalStaffHired INTEGER NOT NULL DEFAULT 0, "
                + "HappinessScore DECIMAL(5, 2) NOT NULL DEFAULT 100.0, "
                + "DailyManagementFee DECIMAL(18, 4) NOT NULL DEFAULT 0"
                + ");");

        // 7. Katalog & Kepemilikan 150 Galeri Aset Mewah
        db.execSQL("CREATE TABLE IF NOT EXISTS GalleryCatalog ("
                + "ItemId TEXT PRIMARY KEY, "
                + "ItemName TEXT NOT NULL, "
                + "Category TEXT NOT NULL, "
                + "Rarity TEXT NOT NULL, "
                + "BasePrice DECIMAL(18, 4) NOT NULL, "
                + "ImagePath TEXT NOT NULL, "
                + "ReputationBonus INTEGER NOT NULL DEFAULT 10"
                + ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS PlayerGalleryOwnership ("
                + "OwnershipId TEXT PRIMARY KEY, "
                + "ItemId TEXT NOT NULL, "
                + "AcquiredAt DATETIME NOT NULL, "
                + "PurchasePrice DECIMAL(18, 4) NOT NULL, "
                + "IsEquippedOrDisplayed INTEGER NOT NULL DEFAULT 0, "
                + "FOREIGN KEY(ItemId) REFERENCES GalleryCatalog(ItemId)"
                + ");");

        // 8. Misi Harian & Checklist
        db.execSQL("CREATE TABLE IF NOT EXISTS DailyQuests ("
                + "QuestId TEXT PRIMARY KEY, "
                + "Title TEXT NOT NULL, "
                + "TargetGoal INTEGER NOT NULL, "
                + "CurrentProgress INTEGER NOT NULL DEFAULT 0, "
                + "RewardCash DECIMAL(18, 4) NOT NULL, "
                + "IsClaimed INTEGER NOT NULL DEFAULT 0"
                + ");");

        // 9. Jadwal & Riwayat Dividen Emiten
        db.execSQL("CREATE TABLE IF NOT EXISTS StockDividends ("
                + "DividendId TEXT PRIMARY KEY, "
                + "Ticker TEXT NOT NULL, "
                + "DividendPerShare DECIMAL(18, 4) NOT NULL, "
                + "CumDateSession INTEGER NOT NULL, "
                + "ExDateSession INTEGER NOT NULL, "
                + "PaymentDateSession INTEGER NOT NULL, "
                + "IsDistributed INTEGER NOT NULL DEFAULT 0"
                + ");");

        // 10. Sistem Pertarungan Bos Taipan Pasar
        db.execSQL("CREATE TABLE IF NOT EXISTS MarketBossShowdown ("
                + "BossId TEXT PRIMARY KEY, "
                + "BossName TEXT NOT NULL, "
                + "DifficultyTier TEXT NOT NULL, "
                + "AvatarImage TEXT NOT NULL, "
                + "TargetReturnPercentage DECIMAL(6, 2) NOT NULL, "
                + "DurationSessions INTEGER NOT NULL, "
                + "BossCurrentReturn DECIMAL(6, 2) NOT NULL DEFAULT 0.0, "
                + "PlayerCurrentReturn DECIMAL(6, 2) NOT NULL DEFAULT 0.0, "
                + "IsActive INTEGER NOT NULL DEFAULT 0, "
                + "IsDefeated INTEGER NOT NULL DEFAULT 0, "
                + "RewardCash DECIMAL(18, 4) NOT NULL"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Upgrade strategy: migrasi berjenjang tanpa kehilangan data pemain
    }

    /**
     * Memastikan database telah tersalin/terinisialisasi dari file aset jika belum ada.
     */
    public void ensureDatabaseInitialized() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            dbFile.getParentFile().mkdirs();
            try (InputStream is = context.getAssets().open("data/" + DATABASE_NAME);
                 OutputStream os = new FileOutputStream(dbFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            } catch (Exception e) {
                // Fallback ke pembuatan tabel runtime jika file aset tidak ada
                getWritableDatabase();
            }
        }
    }
}
