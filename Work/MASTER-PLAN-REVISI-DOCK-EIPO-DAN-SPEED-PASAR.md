# MASTER PLAN EKSEKUSI: REVISI DOCK AKSI, PEROMBAKAN UI E-IPO & AKSELERASI PERGERAKAN HARGA PASAR

**Target Proyek:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM` (Game Saham 2D Portrait Android)  
**Tanggal Dokumen:** 31 Oktober 2026  
**Status Dokumen:** Menunggu Jawaban 7 Pertanyaan Konfirmasi Sebelum Eksekusi  
**Prinsip Batasan:** Scope Ketat (*Narrow Scope*) — Hanya menyentuh task yang diminta dan tidak mengubah kode di luar ruang lingkup yang ditentukan.

---

## I. PEMETAAN RUANG LINGKUP & TASK EKSEKUSI

### Task 1: Reposisi Dual Action Dock Detail Saham (JUAL / BELI)
- **Lokasi Kode:** `style.css:848-864`, `app.js:openInstrumentDetail`
- **Kondisi Eksisting:** `.detail-actions-dock` memiliki aturan `bottom: 0 !important;` sehingga menutupi bilah navigasi tab bawah (`.bottom-nav`).
- **Target Solusi:** 
  - Mengembalikan posisi dock berada tepat di atas tab bawah: `bottom: var(--nav-height, 80px) !important;`.
  - Memberikan padding pengaman scroll bawah pada `.flow-content` (minimal `calc(var(--nav-height) + 80px)`) agar konten grafik dan order book terbawah tidak tertutup dock.

### Task 2: Perombakan UI & Desain e-IPO Berdasarkan Referensi Visual
- **Dokumen Acuan:** `beranda_e_ipo_0af308.webp` (List e-IPO Stockbit) & `tampilan_ordert_34b2c0.webp` (Detail Pesanan & Timeline Penjatahan)
- **Subtask 2.1: Halaman List Saham e-IPO:**
  - Header Hero Banner: Menampilkan judul `e-IPO 🚀`, deskripsi *"Dengan e-IPO, kamu bisa cari dan pesan saham IPO dengan mudah."*, serta ornamen ilustrasi roket dan kosmik vektor SVG lokal.
  - Sub-header & Filter Bar: Judul seksi *"List Saham e-IPO ⓘ"* dengan 3 filter pill: `Ongoing` (aktif), `Upcoming`, dan `Past`.
  - Kartu Penawaran IPO Modern:
    - Baris Atas: Logo lingkaran emiten, Kode Ticker (bold putih), Nama Perusahaan (abu-abu sekunder), dan Rentang Harga (`Rp470 - Rp605`) atau Harga Tetap (`Rp100`).
    - Garis Pemisah Tipis (*Divider*).
    - Baris Bawah: Badge Tahapan Penawaran (warna sian untuk `Penawaran Awal`, hijau toska untuk `Distribusi Saham`) di kiri, dan Jadwal Tanggal (`09 Feb - 15 Feb`) di kanan.
- **Subtask 2.2: Lembar Rincian Pesanan & Penjatahan e-IPO:**
  - Kartu Rincian Pesanan: Order ID unik, Allocation Type (`Pool` / `Fixed`), Jumlah Lot Pemesanan vs Jumlah Lot Penjatahan (disertai persentase realisasi seperti `150 (25%)`), Harga final, dan Nilai Penjatahan riil.
  - Timeline Tracker 5-Tahap:
    1. *Penawaran Awal (Bookbuilding)* [Centang selesai / tanggal]
    2. *Penawaran Umum (Offering)* [Centang selesai / tanggal]
    3. *Penjatahan Efek (Allotment)* [Status penjatahan / Allotted w/ Scale Back]
    4. *Distribusi Saham* [Tanggal distribusi ke portofolio]
    5. *IPO Listing* [Tanggal pencatatan perdana di bursa]
  - Tombol Aksi Bawah: Tombol `Lihat Detail e-IPO` dan opsi pembatalan transparan jika masih masa bookbuilding.

### Task 3: Akselerasi Kecepatan Pergerakan Harga Pasar (*Realtime Market Velocity*)
- **Lokasi Kode:** `TradingFacade.java:2292-2296`, `NpcAgent.java:164-170`, `MarketEngine.java:231-295`
- **Akar Masalah:** NPC liquidity saat ini hanya di-generate setiap 15 menit game (~37.5 detik nyata) dengan seed `timeBucket = timestamp / 900_000L`.
- **Target Solusi:**
  - Mempercepat frekuensi pemicuan transaksi bot NPC menjadi setiap **1 menit game (~2.5 detik nyata)**.
  - Memperbarui `timeBucket` di `NpcAgent.java` menjadi `timestamp / 60_000L` agar pembeli/penjual ritel dan market maker bereaksi dinamis.
  - Menghidupkan transaksi aktif pada seluruh emiten yang sedang buka bursa sehingga pergerakan harga terlihat mengalir cepat seperti game pasar modal modern.

### Task 4: Pergerakan Harga & Simulasi Pasar Selama Periode Offline
- **Lokasi Kode:** `TradingFacade.java:advanceWorldSeconds (2230-2304)`, `reconcileOfflineElapsed`
- **Akar Masalah:** Baris 2292 membatasi perdagangan hanya jika `clock.isForeground()` bernilai true. Ketika aplikasi ditinggal offline, `clock.isForeground()` bernilai false sehingga bot NPC tidak bertransaksi dan harga saham membeku sama sekali.
- **Target Solusi:**
  - Mengizinkan siklus perdagangan bot NPC (`generateNpcLiquidity` dan drift nilai wajar) tetap diproses saat melangkah melewati hari offline bursa.
  - Mensimulasikan perubahan harga harian realistis (-5% s/d +7%) untuk seluruh 20 emiten aktif berdasarkan sentimen berita dan siklus makro selama pemain tidak membuka aplikasi.

### Task 5: Penyelarasan Modul Tambahan yang Diberi Anotasi
- **Katalog Multi-Aset (Comment 2):** Penataan informasi instrumen obligasi negara (FR0091) dengan nilai par, kupon, dan status yang lebih tegas.
- **Alokasi Sektor Portofolio (Comment 3):** Memperbaiki tampilan Donut Chart dan legenda persentase sektor industri agar rapi dan tidak timpang.
- **Fitur Kelola Dana & Fund Builder (Comment 4):** Memastikan menu pengelolaan dana, laporan mandat fee, dan headhunting staf analis tertata dengan visual dark modern.

### Task 6: Verifikasi Menyeluruh & Build APK
- Menjalankan 17 suite test sintesa simulasi pasar.
- Menguji build APK Gradle lokal Windows.
- Melakukan verifikasi visual di browser desktop port 8794 dan live headless capture.

---

## II. DAFTAR 7 PERTANYAAN KONFIRMASI KEPADA PENGGUNA

Sebelum satu baris kode pun dimodifikasi, agen wajib mengajukan 7 pertanyaan konfirmasi keputusan kepada pengguna:
1. **Posisi Dock JUAL/BELI:** Apakah dock JUAL/BELI ingin dipatok menempel di atas bilah navigasi tab bawah (`bottom: 80px`), atau apakah bilah tab bawah sebaiknya otomatis disembunyikan saat membuka layar detail saham agar area dock menempati dasar layar secara penuh?
2. **Kategori e-IPO (Ongoing, Upcoming, Past):** Apakah 36 emiten IPO katalog ingin disaring dinamis ke 3 tab filter (`Ongoing`, `Upcoming`, `Past`) berdasarkan tanggal game berjalan saat ini?
3. **Desain Ilustrasi Roket e-IPO:** Apakah ilustrasi roket dan ornamen kosmik pada banner hero e-IPO ingin memakai grafis vektor SVG lokal bergaya minimalis hijau toska/emas agar ringan dan selaras dengan tema game?
4. **Timeline Penjatahan e-IPO:** Apakah timeline 5-tahap (*Penawaran Awal $\rightarrow$ Penawaran Umum $\rightarrow$ Penjatahan Efek $\rightarrow$ Distribusi $\rightarrow$ Listing*) ingin langsung diterapkan pada seluruh kartu pesanan di modul e-IPO?
5. **Kecepatan Pergerakan Harga Pasar (Live):** Berapa kecepatan transaksi live yang diinginkan: apakah pergerakan harga berganti setiap **1 menit game (~2.5 detik nyata)**, atau setiap **tick jam (setiap detik)**?
6. **Mekanisme Pergerakan Harga Offline:** Apakah saat ditinggal offline, seluruh 20 emiten aktif ingin disimulasikan mengalami fluktuasi harga harian penuh (-5% s/d +7%) mengikuti sentimen warta dan siklus makro sehingga grafik candlestick berubah nyata saat pemain kembali?
7. **Penyelarasan 3 Modul Tambahan (Multi-Aset, Donut Sektor, Fund Builder):** Apakah ketiga modul yang ditandai pada Comment 2, 3, dan 4 ingin ikut diselaraskan tata letak kartu dan palet warnanya dengan gaya Stockbit gelap yang baru?
