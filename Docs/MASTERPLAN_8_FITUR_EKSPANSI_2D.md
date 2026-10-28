# MASTERPLAN EKSPANSI: 8 FITUR LANJUTAN (2D PORTRAIT)
## Proyek: Game Saham Android (Repo: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`)

- **Tanggal Dokumen:** 23 Oktober 2026
- **Status:** Masterplan Ekspansi (Fase 2 - Pasca 15 Fitur Inti)
- **Target Repo:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`
- **Format UI:** **2D Portrait Mobile** (Layar Vertikal Ponsel Android, Kontrol Ramah Jempol)
- **Desain Visual:** Dark Theme (Navy Berlapis, Teks Putih Hangat, Aksen Emas Kusam/Amber, Target Sentuh $\ge 48\text{ dp}$)
- **Penyimpanan:** Database SQLite Lokal di Perangkat (*Offline-First*)
- **Integrasi 5 Tab Navigasi Bawah:** **Beranda | Pasar | Aset | Firma | Riset**

---

## 1. Ringkasan Eksekutif & Tujuan Ekspansi

Setelah 15 fitur inti perdagangan dan keuangan berhasil dibangun, Masterplan Ekspansi ini menambahkan **8 fitur gameplay dan ekonomi tingkat lanjut** yang digali langsung dari biner dan aset visual aplikasi referensi (`com.lydia.tycoongame`).

Tujuan utama ekspansi ini adalah:
1. **Meningkatkan Daya Saing & Tantangan:** Menghadirkan pertarungan melawan "Bos Taipan Pasar" dan sistem audit kepatuhan risiko.
2. **Memperdalam Realisme Pasar Modal:** Menambahkan pembagian dividen tunai emiten dan pembukaan gembok sektor saham secara bertahap.
3. **Memperkaya Personalisasi & Loyalitas Pemain:** Menyediakan kustomisasi avatar/bingkai profil, medali pencapaian permanen, kartu booster riset, dan pengelolaan kebahagiaan staf kantor.

---

## 2. Pemetaan 8 Fitur Lanjutan ke dalam 5 Tab Navigasi

```
┌────────────────────────────────────────────────────────────────────────┐
│               PEMETAAN 8 FITUR EKSPANSI PADA TAB NAVIGASI              │
├──────────────┬──────────────┬──────────────┬─────────────┬─────────────┤
│ 1. BERANDA   │ 2. PASAR     │ 3. ASET      │ 4. FIRMA    │ 5. RISET    │
├──────────────┼──────────────┼──────────────┼─────────────┼─────────────┤
│ • Duel Bos   │ • Dividen    │ • Audit      │ • Moral &   │ • Kartu     │
│   Taipan (F1)│   Saham (F3) │   Pajak (F2) │   Fasilitas │   Booster   │
│ • Medali     │ • Buka Kunci │              │   Kantor(F8)│   Riset (F7)│
│   Prestasi   │   Sektor (F4)│              │             │             │
│   (F5)       │              │              │             │             │
│ • Avatar &   │              │              │             │             │
│   Bingkai(F6)│              │              │             │             │
└──────────────┴──────────────┴──────────────┴─────────────┴─────────────┘
```

---

## 3. Rincian 8 Fitur Lanjutan dalam Bahasa Manusia

### 1. Duel Lawan Bos Taipan Pasar (*Market Boss Showdown*)
* **Konsep Bahasa Manusia:**
  Di setiap akhir jenjang karier atau promosi liga, pemain ditantang berduel *1-on-1* melawan Bos Taipan Pasar legendaris:
  - *Bos Mudah:* Taipan Lokal Pemula (Aset: `kolay_patron.jpg`).
  - *Bos Menengah:* Manajer Hedge Fund Elit (Aset: `orta_patron.jpg`).
  - *Bos Sulit:* Konglomerat Multi-Industri (Aset: `zor_patron.png`).
  - *Bos Puncak (Final Boss):* Sang Penguasa Pasar Modal (Aset: `final_boss.png`).
* **Mekanisme Permainan:**
  Tantangan berlangsung selama 5–10 sesi pasar. Kriteria kemenangan: persentase cuan portofolio pemain harus melampaui cuan sang Bos.
* **Hadiah Kemenangan:**
  Membuka akses ke saham privat eksklusif, uang hadiah besar, dan bingkai avatar prestisius.

### 2. Audit Pajak & Kepatuhan Risiko (*Tax Audit & Risk Compliance*)
* **Konsep Bahasa Manusia:**
  Seperti di dunia nyata, kantor otoritas pajak dan komite pengawas bursa bisa datang melakukan audit mendadak (*tax & compliance audit*).
* **Mekanisme Pemicu:**
  Pemain yang rasio utangnya terlalu tinggi (> 40% dari total aset), sering memakai margin ekstrem, atau tidak memiliki cadangan kas darurat berisiko tinggi terkena audit.
* **Konsekuensi:**
  - *Jika Lolos:* Mendapatkan predikat kepatuhan tinggi dan diskon pajak dividen 10%.
  - *Jika Gagal:* Dikenakan denda pajak tambahan dan pembatasan sementara fasilitas pinjaman bank selama 3 sesi.

### 3. Pembagian Dividen Tunai Emiten Saham (*Dividend Payouts & Yield*)
* **Konsep Bahasa Manusia:**
  Saham bukan cuma soal jual-beli cepat. Emiten berfundamental sehat secara berkala membagikan keuntungan perusahaan kepada para pemegang saham dalam bentuk **Dividen Tunai**.
* **Alur Bursa:**
  Setiap emiten memiliki jadwal: *Cum-Date* (batas akhir beli untuk dapat dividen), *Ex-Date*, dan *Payment Date*. Pada hari pembayaran, uang kas dividen otomatis masuk ke saldo rekening pemain sesuai jumlah lot yang dimiliki.
* **Keseruan:**
  Membuka strategi pasif bagi pemain: hidup makmur dari dividen saham *blue chip* tanpa perlu pusing memantau pergerakan grafik setiap menit.

### 4. Pangkat Level Investor & Buka Kunci Sektor (*Tiered Level & Sector Unlocks*)
* **Konsep Bahasa Manusia:**
  Pemain baru tidak langsung dibebani ratusan pilihan saham yang membingungkan. Terdapat sistem **10 Tingkat Pangkat Investor** (didukung aset grafis di folder `level_system/` dan `rank_system/`).
* **Hierarki Buka Kunci Sektor:**
  - *Level 1–3 (Investor Pemula):* Sektor Perbankan & Konsumer Barang Pokok (pergerakan harga stabil).
  - *Level 4–6 (Analis Madya):* Sektor Energi, Pertambangan Mineral, & Properti/Infrastruktur.
  - *Level 7–10 (Fund Manager Elit):* Sektor Teknologi Tinggi, Saham Global Wall Street, dan Komoditas Emas/Minyak Mentah.

### 5. Buku Rekor Medali Prestasi Permanen (*Trophy Achievements*)
* **Konsep Bahasa Manusia:**
  Buku rekor sejarah perjalanan pemain dari nol hingga konglomerat (didukung aset visual `achievements.png`).
* **Contoh Prestasi:**
  - *"Langkah Perdana":* Menyelesaikan transaksi beli saham pertama.
  - *"Panen Dividen":* Mengantongi dividen tunai akumulasi Rp 50.000.000.
  - *"Raja Pembalikan Arah":* Membeli saham di titik terendah dan menjual di titik tertinggi hari itu.
  - *"Penakluk Taipan":* Mengalahkan Bos Taipan Pasar tingkat Sulit.
* **Hadiah Permanen:**
  Setiap medali yang diklaim memberikan bonus status permanen (misal: diskon komisi 1%, peningkatan batas pinjaman) serta uang tunai.

### 6. Kustomisasi Avatar & Bingkai Profil Prestise (*Avatar & Frames*)
* **Konsep Bahasa Manusia:**
  Pemain bisa memilih tampilan identitas dirinya lewat 12 foto karakter investor (folder `avatars_players/`) dan 15 bingkai unik berornamen mewah (folder `frame_players/`).
* **Koleksi Bingkai:**
  - *Bingkai Standar:* Kayu Eboni, Baja Halus.
  - *Bingkai Liga:* Perunggu Juara, Perak Mahasiswa, Emas Manajer Investasi.
  - *Bingkai Legenda:* Kristal Safir, Naga Emas Kaisar, Serat Karbon Hypercar.
* **Syarat Membuka:** Bingkai diperoleh secara bertahap saat naik liga, memenangkan duel bos, atau mengoleksi barang mewah langka.

### 7. Kartu Riset R&D & Booster Efisiensi Sementara (*R&D Boosters*)
* **Konsep Bahasa Manusia:**
  Pemain bisa membeli atau memperoleh kartu riset khusus yang memberikan keuntungan taktis untuk waktu terbatas:
  - *Bocoran Riset Analis:* Akurasi sinyal berita makro meningkat 30% selama 3 hari bursa.
  - *Koneksi Khusus Broker:* Bebas komisi transaksi selama 15 order berikutnya.
  - *Suntikan Likuiditas Cepat:* Plafon pinjaman bank meningkat 50% selama 1 sesi bursa.
* **Penyimpanan:** Disimpan di dompet kartu pada tab Riset dan bisa diaktifkan kapan saja saat momen pasar sangat genting.

### 8. Manajemen Moral Staf & Fasilitas Kantor (*Staff Morale & Office Perks*)
* **Konsep Bahasa Manusia:**
  Di tab Firma, pemain dapat merenovasi dan menambahkan fasilitas di kantor investasinya (didukung aset 35 gambar ruangan di `rooms/`):
  - *Pantry Kopi Premium:* Meningkatkan kecepatan analisis riset staf.
  - *Ruang Rekreasi Karyawan:* Menjaga skor kebahagiaan (*Happiness*) staf di atas 90%.
  - *Server Komputasi Kuantum:* Meningkatkan pendapatan pasif harian firma.
* **Dampak:** Staf yang bahagia menghasilkan prediksi pasar yang lebih akurat dan menarik lebih banyak dana kelolaan nasabah (AUM).

---

## 4. Skema Database SQLite Tambahan (Ekspansi 8 Fitur)

Untuk melengkapi 8 tabel yang sudah ada dari masterplan sebelumnya, dirancang **5 tabel tambahan** di database SQLite lokal:

```sql
-- 9. Sistem Pertarungan Bos Taipan Pasar
CREATE TABLE MarketBossShowdown (
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

-- 10. Jadwal & Riwayat Dividen Emiten
CREATE TABLE StockDividends (
    DividendId TEXT PRIMARY KEY,
    Ticker TEXT NOT NULL,
    DividendPerShare DECIMAL(18, 4) NOT NULL,
    CumDateSession INTEGER NOT NULL,
    ExDateSession INTEGER NOT NULL,
    PaymentDateSession INTEGER NOT NULL,
    IsDistributed INTEGER NOT NULL DEFAULT 0
);

-- 11. Buku Medali Prestasi Pemain
CREATE TABLE PlayerAchievements (
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

-- 12. Koleksi Avatar & Bingkai Profil
CREATE TABLE ProfileCosmetics (
    CosmeticId TEXT PRIMARY KEY,
    CosmeticType TEXT NOT NULL, -- AVATAR / FRAME
    Name TEXT NOT NULL,
    ImagePath TEXT NOT NULL,
    Rarity TEXT NOT NULL,
    UnlockCondition TEXT NOT NULL,
    IsUnlocked INTEGER NOT NULL DEFAULT 0,
    IsEquipped INTEGER NOT NULL DEFAULT 0
);

-- 13. Kartu Booster Riset & Efisiensi Sementara
CREATE TABLE ResearchBoosters (
    BoosterId TEXT PRIMARY KEY,
    BoosterType TEXT NOT NULL, -- NEWS_ACCURACY, FEE_DISCOUNT, MARGIN_EXPAND
    Title TEXT NOT NULL,
    Description TEXT NOT NULL,
    RemainingSessionsOrUses INTEGER NOT NULL,
    IsActive INTEGER NOT NULL DEFAULT 0,
    MultiplierValue DECIMAL(6, 2) NOT NULL
);
```

---

## 5. Roadmap Tahapan Eksekusi 8 Fitur Lanjutan

Pengerjaan 8 fitur ekspansi ini dibagi ke dalam 4 sub-tahap modular:

```
TAHAP E1: DIVIDEN & TIERING SEKTOR (Tab PASAR)
├── Bangun jadwal dividen saham (Cum/Ex/Payment) & pembayaran kas otomatis
├── Terapkan pembatasan gembok sektor saham berdasarkan Level Pangkat
└── Uji verifikasi distribusi dividen dan validasi order sektor terkunci

TAHAP E2: DUEL BOS TAIPAN & KEPATUHAN PAJAK (Tab BERANDA & ASET)
├── Implementasikan sistem tantangan Bos Taipan (Easy s/d Final Boss)
├── Integrasikan perhitungan kepatuhan risiko & audit pajak berkala
└── Bangun antarmuka duel bos dan status audit di UI 2D Portrait

TAHAP E3: MEDALI PRESTASI, AVATAR & BINGKAI PROFIL (Tab BERANDA)
├── Pasang sistem tracking prestasi otomatis & klaim hadiah
├── Integrasikan 12 avatar dan 15 bingkai profil (folder avatars & frames)
└── Bangun modal ganti avatar/bingkai di profil pemain

TAHAP E4: FASILITAS KANTOR & KARTU BOOSTER RISET (Tab FIRMA & RISET)
├── Bangun renovasi fasilitas kantor & kalkulasi skor kebahagiaan staf
├── Bangun inventaris kartu booster riset sementara
└── Uji regresi menyeluruh, verifikasi memori, dan finalisasi rilis
```

---

## 6. Kriteria Keberhasilan (*Definition of Done*):
1. **Pemeriksaan Runnable:** Setiap fitur baru memiliki uji unit di C# Domain (penghitungan dividen, validasi kemenangan duel bos, dan konsumsi kartu booster).
2. **Kinerja Lancar di Android Portrait:** Seluruh dialog bos, halaman medali, dan kustomisasi avatar responsif, bebas lag, dan ramah sentuhan satu tangan.
3. **Data Utuh:** Seluruh status dividen, duel bos, dan medali tersimpan secara persisten di database SQLite lokal.
