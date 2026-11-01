# MASTERPLAN ARSITEKTUR & EKSEKUSI 15 FITUR (2D PORTRAIT)
## Proyek: Game Saham Android (Repo: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`)

- **Tanggal Dokumen:** 23 Oktober 2026
- **Target Repo:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`
- **Format UI:** **2D Portrait Mobile** (Layar Vertikal Ponsel Android, Satu Tangan)
- **Desain Visual:** Dark Theme (Navy Berlapis, Putih Hangat, Aksen Emas Kusam/Amber, Target Sentuh $\ge 48\text{ dp}$)
- **Penyimpanan:** Database SQLite Lokal di Perangkat (*Offline-First*)
- **Navigasi Utama (5 Tab Bawah):** **Beranda | Pasar | Aset | Firma | Riset**

---

## 1. Ringkasan Eksekutif & Filosofi Desain

Masterplan ini merumuskan integrasi **15 fitur gameplay dan ekonomi** yang diadaptasi dari hasil penelusuran arsitektur referensi ke dalam game buatanmu: **Game Saham 2D Portrait**.

### Tiga Prinsip Utama Desain:
1. **Konteks Sebelum Komitmen:** Semua rincian akun, instrumen saham, harga estimasi, volume lot, dan biaya komisi harus terlihat jelas sebelum tombol beli/jual ditekan.
2. **Kedaulatan Logika Tunggal (*Single Source of Truth*):** Seluruh formula pergerakan harga pasar, matching order, pembukuan kas/utang, dan progres karier fund manager dikelola oleh C# Domain lokal.
3. **Penyimpanan Mandiri (*Offline-First SQLite*):** Seluruh data permainan (portofolio, buku jurnal, aset koleksi, misi, dan status firma) tersimpan aman di SQLite lokal ponsel tanpa ketergantungan server luar.

---

## 2. Pemetaan 15 Fitur ke dalam 5 Tab Navigasi 2D Portrait

Kelima belas fitur dipetakan secara rapi dan ergonomis ke dalam 5 pilar navigasi bawah ponsel:

```
┌────────────────────────────────────────────────────────────────────────┐
│                   NAVIGASI 2D PORTRAIT GAME SAHAM                      │
├──────────────┬──────────────┬──────────────┬─────────────┬─────────────┤
│ 1. BERANDA   │ 2. PASAR     │ 3. ASET      │ 4. FIRMA    │ 5. RISET    │
├──────────────┼──────────────┼──────────────┼─────────────┼─────────────┤
│ • Agenda Hari│ • Watchlist  │ • Portofolio │ • Klien &   │ • Kalender  │
│ • Misi Harian│ • Candlestick│ • Pinjaman   │   Mandat    │   Makro     │
│ • DNA Status │ • Order Book │   Bank       │ • Staf/Tim  │ • Berita    │
│ • Kasta Liga │ • Tiket Beli/│ • Galeri 150 │ • Dividen & │   Pasar     │
│              │   Jual       │   Aset Mewah │   Prestige  │ • Replay    │
│              │ • Fitur IPO  │ • Mode Sulit │             │   Historis  │
│              │ • Stock Split│              │             │             │
└──────────────┴──────────────┴──────────────┴─────────────┴─────────────┘
```

---

## 3. Rincian 15 Fitur dalam Bahasa Manusia

### PILAR A: SISTEM BURSA & EKSEKUSI TRADING (Tab PASAR)

#### 1. Data Candlestick OHLCV (`MarketCandleDto`)
* **Bahasa Manusia:** Grafik lilin hijau-merah standar bursa (*Open, High, Low, Close, Volume*) dengan pemilih rentang waktu (1H, 1D, 1M, 1Y).
* **Integrasi 2D Portrait:** Ditempatkan di bagian atas halaman detail saham, dapat di-zoom/scroll horizontal dengan lancar menggunakan Canvas lokal yang ringan.

#### 2. Sentimen Berita Makro & Peristiwa Pasar (`MarketEvent`)
* **Bahasa Manusia:** Setiap sesi pasar dimulai, ada berita ekonomi (misal: kenaikan suku bunga acuan, kelangkaan energi, atau rilis laporan keuangan) yang menggerakkan sektor saham tertentu secara logis.
* **Integrasi 2D Portrait:** Tampil sebagai kartu sorotan berita di bagian atas tab Pasar dan tab Riset.

#### 3. Pemisahan Buku Kas Transaksi & Portofolio (`Ledger vs Portfolio`)
* **Bahasa Manusia:** Setiap kali beli atau jual, transaksi dicatat permanen di buku jurnal (*ledger*). Di portofolio, pemain bisa melihat *Unrealized P/L* (untung/rugi mengambang) dan *Realized P/L* (keuntungan bersih yang sudah cair).
* **Integrasi 2D Portrait:** Kartu ringkasan posisi tertanam di halaman detail saham dan daftar kepemilikan di tab Aset.

#### 4. Tata Letak Layar Perdagangan Vertikal (*Portrait Trading Sheet*)
* **Bahasa Manusia:** Alur trading satu tangan yang ergonomis:
  1. *Header:* Ticker, nama emiten, harga terkini, persentase perubahan harian.
  2. *Tengah:* Grafik lilin + Buku Antrean (*Order Book Bid/Ask*) interaktif (klik harga di antrean langsung mengisi input harga).
  3. *Bawah:* Tiket order cepat dengan tab Beli/Jual, pengatur lot, tombol persentase modal instan (25%, 50%, 75%, MAX), dan estimasi total biaya transparan sebelum konfirmasi.

#### 5. Penawaran Saham Perdana (IPO - *Initial Public Offering*)
* **Bahasa Manusia:** Momen emas memesan saham perusahaan baru dengan harga penawaran awal sebelum resmi listing di bursa.
* **Keseruan:** Begitu hari listing tiba, harga bisa melonjak tinggi atau terkoreksi tergantung sentimen sektor industri saat itu.

#### 6. Aksi Korporasi: Pemecahan Nilai Saham (*Stock Split*)
* **Bahasa Manusia:** Jika ada saham unggulan yang harganya sudah terbang terlalu tinggi (misal Rp 50.000/lembar), emiten melakukan pemecahan nominal (misal 1:5). Harga per lembar turun jadi Rp 10.000, tapi jumlah lembar saham pemain otomatis bertambah 5 kali lipat sehingga kembali likuid.

---

### PILAR B: KEUANGAN, RISIKO & PROGRES PRIBADI (Tab ASET)

#### 7. Fasilitas Pinjaman Bank & Margin Trading
* **Bahasa Manusia:** Pemain bisa meminjam dana segar dari bank game saat uang kas habis tapi melihat peluang cuan besar.
* **Mekanisme Risiko:** Dibatasi maksimal 50% dari total nilai aset bersih. Ada bunga harian. Jika portofolio anjlok parah hingga melewati batas aman, bank akan mengeksekusi *Margin Call* (menjual paksa saham pemain untuk pelunasan utang).

#### 8. Galeri Koleksi 150 Aset Mewah Sultan
* **Bahasa Manusia:** Tempat pemain membelanjakan kekayaan hasil trading untuk mengoleksi barang-barang bergengsi kelas sultan.
* **Dukungan Aset Lengkap (150 PNG $1254 \times 1254$ Transparan):**
  - **12 Kategori:** Mobil Mewah (20), Properti Hunian (16), Jam Tangan (15), Perhiasan Permata (15), Motor Kolektor (12), Seni Rupa & Patung (12), Teknologi Premium (12), Properti Komersial (10), Barang Antik (10), Kapal & Yacht (10), Trofi Prestasi (10), Pesawat & Helikopter (8).
  - **5 Kasta Kelangkaan:** UMUM (25), TIDAK_UMUM (29), LANGKA (35), EPIK (29), LEGENDARIS (32).
* **Fungsi Finansial:** Aset koleksi ini menaikkan gengsi pemain di mata investor klien dan nilainya kebal terhadap krisis bursa (*safe haven*).

#### 9. Pengaturan Tingkat Realisme Pasar (*Market Difficulty*)
* **Bahasa Manusia:** Pilihan suasana pasar sesuai kemampuan pemain:
  - *Mode Belajar:* Pergerakan harga lebih jinak, tanpa biaya komisi, tanpa bunga pinjaman.
  - *Mode Realistis:* Fluktuasi tajam, ada komisi broker dan pajak bursa riil, ada inflasi harian, dan risiko suspensi emiten (*halt*).

---

### PILAR C: IDENTITAS INVESTOR, MISI & PERSAINGAN (Tab BERANDA)

#### 10. Kartu DNA Gaya Investor (*Investor Archetype*)
* **Bahasa Manusia:** Game membaca riwayat trading pemain dan memberikan diagnosis profil psikologi investasi:
  - *The Scalper:* Suka transaksi cepat dan volatilitas tinggi (Buff: Diskon komisi order instan).
  - *Value Investor:* Sabar memegang saham fundamental kuat (Buff: Imbal hasil dividen lebih tinggi).
  - *The Risk-Taker:* Gemar saham IPO dan fasilitas margin utang.

#### 11. Checklist Misi & Tugas Harian (*Daily Quests*)
* **Bahasa Manusia:** Daftar agenda harian di layar Beranda sebelum mulai trading:
  - *"Lakukan diversifikasi ke 2 sektor saham berbeda hari ini"*
  - *"Raih profit bersih Rp 5.000.000 dalam sesi ini"*
  - *"Selesaikan cicilan bunga bank sebelum penutupan pasar"*
* **Hadiah:** Memberikan reward uang modal tambahan dan poin reputasi karier.

#### 12. Sistem Rival Pedagang Saham & Promosi Kasta Liga
* **Bahasa Manusia:** Pemain dipasangkan dengan 1 karakter Rival pedagang saham NPC yang modalnya seimbang. Ada klasemen mingguan; jika pertumbuhan portofolio pemain mengungguli Rival, pemain promosi ke divisi liga yang lebih tinggi (Liga Investor Junior $\rightarrow$ Manajer Senior $\rightarrow$ Konglomerat Sultan).

---

### PILAR D: KARIER PENGELOLA DANA, KANTOR & WARISAN (Tab FIRMA)

#### 13. Kerajaan Pengelola Investasi & Staf Kantor (*Firm & Staff Management*)
* **Bahasa Manusia:** Pemain mendirikan firma pengelola investasi sendiri:
  - Meningkatkan level kantor (dari ruang sewa kecil hingga menara kantor finansial).
  - Merekrut staf Analis Riset (memperbaiki akurasi sinyal berita), Staf Pemasaran (menarik dana nasabah AUM), dan Manajer Risiko.
  - Mengelola kebahagiaan staf agar produktivitas bonus pasif tetap tinggi.

#### 14. Akun Klien Terpisah & Mandat Investasi
* **Bahasa Manusia:** Sebagai Fund Manager profesional, uang pribadi pemain dipisahkan secara tegas dari dana kelolaan nasabah (*Assets Under Management* / AUM). Pemain menerima komisi manajemen (*management fee*) dan bagi hasil keuntungan (*performance fee*).

#### 15. Sistem Prestise Warisan (*Legacy Rebirth System*)
* **Bahasa Manusia:** Mekanisme pensiun terhormat saat kekayaan pemain sudah mencapai puncak tertinggi. Pemain mereset modal dan saham ke awal, namun mendapatkan **Gelar Warisan Permanen**:
  - Diskon komisi broker seumur hidup.
  - Modal awal siklus berikutnya jauh lebih besar.
  - Akses bocoran riset orang dalam (*insider research*).

---

## 4. Skema Database SQLite Lokal (*Offline-First*)

Untuk menyimpan seluruh data 15 fitur secara mandiri dan cepat di Android (.NET MAUI SQLite), disiapkan 8 tabel utama:

```sql
-- 1. Profil Pemain & Karier
CREATE TABLE PlayerProfile (
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
CREATE TABLE StockLedger (
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
CREATE TABLE ActivePortfolio (
    Ticker TEXT PRIMARY KEY,
    TotalLots INTEGER NOT NULL,
    AverageBuyPrice DECIMAL(18, 4) NOT NULL,
    LastKnownPrice DECIMAL(18, 4) NOT NULL
);

-- 4. Riwayat Lilin Harga (Candlestick OHLCV)
CREATE TABLE MarketCandleHistory (
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
CREATE TABLE BankLoan (
    LoanId TEXT PRIMARY KEY,
    PrincipalAmount DECIMAL(18, 4) NOT NULL,
    InterestRatePerSession DECIMAL(6, 4) NOT NULL,
    AccruedInterest DECIMAL(18, 4) NOT NULL DEFAULT 0,
    BorrowedAtSession INTEGER NOT NULL,
    IsSettled INTEGER NOT NULL DEFAULT 0
);

-- 6. Firma Pengelola Dana & Karyawan
CREATE TABLE InvestmentFirm (
    FirmLevel INTEGER PRIMARY KEY,
    FirmName TEXT NOT NULL,
    TotalAUM DECIMAL(18, 4) NOT NULL DEFAULT 0,
    TotalStaffHired INTEGER NOT NULL DEFAULT 0,
    HappinessScore DECIMAL(5, 2) NOT NULL DEFAULT 100.0,
    DailyManagementFee DECIMAL(18, 4) NOT NULL DEFAULT 0
);

-- 7. Katalog & Kepemilikan 150 Galeri Aset Mewah
CREATE TABLE GalleryCatalog (
    ItemId TEXT PRIMARY KEY,
    ItemName TEXT NOT NULL,
    Category TEXT NOT NULL,
    Rarity TEXT NOT NULL, -- UMUM, TIDAK_UMUM, LANGKA, EPIK, LEGENDARIS
    BasePrice DECIMAL(18, 4) NOT NULL,
    ImagePath TEXT NOT NULL,
    ReputationBonus INTEGER NOT NULL DEFAULT 10
);

CREATE TABLE PlayerGalleryOwnership (
    OwnershipId TEXT PRIMARY KEY,
    ItemId TEXT NOT NULL,
    AcquiredAt DATETIME NOT NULL,
    PurchasePrice DECIMAL(18, 4) NOT NULL,
    IsEquippedOrDisplayed INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY(ItemId) REFERENCES GalleryCatalog(ItemId)
);

-- 8. Misi Harian & Checklist
CREATE TABLE DailyQuests (
    QuestId TEXT PRIMARY KEY,
    Title TEXT NOT NULL,
    TargetGoal INTEGER NOT NULL,
    CurrentProgress INTEGER NOT NULL DEFAULT 0,
    RewardCash DECIMAL(18, 4) NOT NULL,
    IsClaimed INTEGER NOT NULL DEFAULT 0
);
```

---

## 5. Roadmap Tahapan Eksekusi

1. **Tahap 1: Fondasi Data & Mesin SQLite Lokal**
   - Menyiapkan database SQLite dan repositori data di C# Domain `GAME SAHAM`.
   - Mengisi tabel `GalleryCatalog` dengan metadata 150 item galeri dari file `catalog.json`.
   - Uji verifikasi baca/tulis database lokal.

2. **Tahap 2: Layar Perdagangan 2D Portrait & Grafik Lilin (Tab PASAR)**
   - Mengintegrasikan render Candlestick OHLCV Canvas portrait yang responsif.
   - Mengintegrasikan Order Book antrean Bid/Ask interaktif dan tiket transaksi satu sentuh.
   - Menghubungkan berita sentimen makro dan event IPO.

3. **Tahap 3: Tab ASET (Portofolio, Pinjaman Bank & Galeri 150 Item)**
   - Menampilkan evaluasi posisi saham (Realized & Unrealized P/L).
   - Menghubungkan fasilitas pinjaman bank dan hitungan bunga sesi.
   - Membangun galeri interaktif untuk menampilkan dan membeli 150 aset mewah sultan.

4. **Tahap 4: Tab BERANDA (Agenda, Misi Harian, DNA & Rival)**
   - Membangun kartu checklist misi harian.
   - Menghitung statistik gaya bermain dan menerbitkan kartu DNA Investor.
   - Membangun sistem perbandingan portofolio dengan Rival pedagang saham.

5. **Tahap 5: Tab FIRMA & Endgame (Karier Fund Manager & Prestise)**
   - Membangun alur pengelolaan dana klien AUM dan rekrutmen staf kantor.
   - Mengimplementasikan sistem Rebirth Prestise Konglomerat.
   - Uji performa menyeluruh, verifikasi memori, dan build APK Android.
