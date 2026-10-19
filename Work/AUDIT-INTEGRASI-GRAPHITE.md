# AUDIT INTEGRASI UI GRAPHITE TERMINAL KE GAME SAHAM

Tanggal: 19 Oktober 2026
Target Root: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`
Git Branch: `codex/rebuild-2d-portrait`
HEAD Commit: `4c501dd fix(domain): implement subsystem reset on new game, short bridge ops, safe-area UI, and rigorous financial E2E regressions`
Status Working Tree: Clean (0 uncommitted changes)

---

## 1. Baseline Pengujian dan Lingkungan Nyata

Pengujian unit JUnit dijalankan secara mandiri pada lingkungan lokal host Windows:
- Perintah: `cd android && ./gradlew testDebugUnitTest --no-daemon`
- Hasil Verifikasi XML (`android/app/build/test-results/testDebugUnitTest/`):
  - Berkas XML Uji: 13 berkas
  - Total Tes: 133 tes
  - Failures: 0
  - Errors: 0
  - Skipped: 0
  - Waktu Eksekusi: 17s (BUILD SUCCESSFUL)
- Variabel Toolchain:
  - `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr"`
  - `ANDROID_HOME="C:/Users/HYPE AMD/AppData/Local/Android/Sdk"`
- Status Gate Fisik/ADB: PENDING by Scope (sesuai mandat pemilik tanpa perangkat/ADB).

---

## 2. Penelusuran Alur Nyata: UI → Bridge → Facade → Domain → Save

1. **UI Web (`android/app/src/main/assets/www/`)**:
   - `index.html`: Shell 2D portrait, 5 tab utama (Beranda, Pasar, Aset, Firma, Riset), modal sheet dialog order, panel flow dinamis.
   - `style.css`: Token antarmuka (saat ini masih navy/gold lama, akan dimigrasi ke Graphite Terminal).
   - `app.js`: Pengendali interaksi, memanggil `window.TradingNativeBridge` jika berjalan di Android WebView atau `MockBridge` jika di browser standalone.
2. **Bridge Native (`android/app/src/main/java/com/tradingglobal3d/mobile/TradingWebBridge.java`)**:
   - Beranotasi `@JavascriptInterface`, mengekspos 48 metode publik bertipe JSON/String.
   - Menerima dan memvalidasi payload JSON dari JavaScript, memetakan ke entitas domain, dan meneruskan ke `TradingFacade`.
3. **Facade (`android/app/src/main/java/com/tradingglobal3d/core/facade/TradingFacade.java`)**:
   - Titik masuk tunggal orkestrasi game (`ITradingFacade` pattern).
   - Menjaga konsistensi antar-subsystem: `Account`, `OrderManager`, `MarketEngine`, `FirmManager`, `CareerManager`, `RiskComplianceService`, `CorporateActionService`, `FundBuilderService`, `ResearchService`, `EconomyLedger`, `FxExchangeService`, `SaveManager`.
4. **Domain Engine (`android/app/src/main/java/com/tradingglobal3d/core/`)**:
   - Matching Engine & Order Book (`com.tradingglobal3d.core.trading`): Eksekusi limit, market, stop-loss, take-profit, pencatatan `TradeFill`.
   - Pasar & Likuiditas NPC (`com.tradingglobal3d.core.npc`): Continuous trading, lelang pra-pembukaan, kalkulasi spread, order book bid/ask.
   - Akun & Multi-Mata Uang (`com.tradingglobal3d.core.model`, `com.tradingglobal3d.core.economy`): Isolasi kas pribadi vs dana kelolaan klien (AUM), reservasi dana order, utang margin multi-currency, pembukuan buku besar (*ledger*) berbasis entri ganda.
   - Risiko & Kepatuhan (`com.tradingglobal3d.core.risk`): Maintenance margin, margin call, short borrowing pool, fee sewa harian.
   - Firma & Mandat (`com.tradingglobal3d.core.firm`): Validasi hard limit mandat klien, pemungutan management/performance fee harian.
   - Waktu & Siklus (`com.tradingglobal3d.core.clock`, `com.tradingglobal3d.core.career`): Jam simulasi, sesi I/II bursa, tutup hari, laporan sesi harian.
5. **Penyimpanan (`SaveManager` & `SaveState`)**:
   - Serialisasi atomic ke JSON disk.
   - Mencakup seluruh state akun, posisi, order, ledger, riset, mandat, aksi korporasi, IPO, suara RUPS, dan inventori lender short MM.

---

## 3. Klasifikasi Kesiapan 5 Kelompok Fitur

### A. Domain Sudah Ada dan Siap Digunakan Ulang
- Multi-Account & Portfolio: Akun personal vs klien, isolasi dana pribadi dan AUM, kalkulasi equity/net worth.
- Order Execution: Validasi order, idempotensi `requestId`, order lifecycle (PENDING -> PARTIALLY_FILLED -> FILLED / CANCELLED / REJECTED).
- Market Engine & NPC: Order book bid/ask live, continuous matching, trade fills history.
- FX Service: Konversi IDR <-> USD, expiry quote 30 detik, preview dan confirm.
- Firm & Mandate: 2 klien (S20 & S21), validasi batas risiko mandat, fee collection harian.
- Research & Corporate: Feed berita, laporan analis, kalender, dividen tunai, stock split, penawaran IPO, voting RUPS.
- Save & Recovery: Backup file, atomic write, startup recovery.

### B. Kontrak Bridge yang Perlu Dilengkapi / Disempurnakan
1. **Pipeline Grafik Candlestick OHLCV**:
   - Domain `OrderBook` telah menyimpan riwayat `TradeFill`, namun bridge belum memiliki metode agregasi bar M1/M5/M15/H1/D1 untuk dikirim ke frontend visual Graphite.
   - Diperlukan metode: `getChartData(String instrumentId, String timeframe)` yang menghitung bar OHLCV dan volume dari `TradeFill` nyata.
2. **Kekayaan Bersih Pribadi**:
   - `getSnapshotJson()` perlu secara eksplisit menyertakan `personalNetWorth` (equity = total portofolio pribadi - utang margin/short) agar header UI dapat langsung membacanya tanpa perhitungan duplikat di JS.
3. **Penyelarasan Indeks Game**:
   - `getIndices()` dan `getIndexDetail()` perlu menyertakan metadata transparan: label simulasi game, metodologi pembobotan kapitalisasi pasar, daftar konstituen 10 IDX dan 10 US, dan status tidak tersedia untuk bursa di luar jangkauan (Nikkei, Hang Seng, dsb.).
4. **Pelacakan Level/XP & Achievement**:
   - Menambahkan kontrak `getPlayerProgress()` pada bridge yang mengembalikan total XP, level pemain, dan status 6 achievement.
   - Menambahkan metode pencatatan event sah (misal `recordResearchRead(articleId)`).

### C. UI Baru yang Perlu Dihubungkan (Graphite Terminal)
- **Token CSS Bersama**: Background `#101214`, Surface `#191D21`, Raised `#252B31`, Teks `#F4F6F8`, Aksi Amber `#FFBD59`, Interaksi Teal `#66E4D5`, Positif `#54DDAD`, Negatif `#FF8A98`.
- **Tipografi Lokal**: Font lokal IBM Plex Sans (`assets/IBMPlexSans.ttf`), lisensi SIL OFL 1.1 disertakan.
- **Header Terpadu**: Chip kekayaan pribadi di kanan atas (terpisah dari AUM), chip Level/XP pemain, pemilih konteks akun transaksi.
- **Terminal Pasar**: Pita 9 indeks (IHSG, USTECH, S&P 500, dsb. dengan label simulasi/proksi jujur), pencarian instrumen, filter bursa/sektor, sparkline, order book 10 tingkat (atau tingkat riil tersedia).
- **Layar Level & Achievement**: Menampilkan level 1..N, progress bar XP, dan kartu 6 achievement (detail syarat, status terbuka/terkunci).
- **Target Sentuh & Safe Area**: Tombol navigasi dan kembali minimal 48x48 dp, penanganan safe area insets 4 sisi, penanganan layar landscape compact height.

### D. Logika Baru yang Memang Diperlukan
- **Subsystem Player Progress (XP & Achievement Engine)**:
  - Kelas domain terpisah (misal `PlayerProgressManager`) yang mengelola XP, Level, dan 6 achievement awal:
    1. `first` (Transaksi pertama): 100 XP saat transaksi pertama dieksekusi.
    2. `reader` (Pembaca pasar): 150 XP saat membuka 5 catatan riset/berita unik.
    3. `mandate` (Kepercayaan pertama): 200 XP saat menerima mandat klien pertama.
    4. `risk` (Penjaga risiko): 250 XP saat menutup 5 sesi bursa tanpa pelanggaran batas risiko mandat.
    5. `global` (Lintas pasar): 200 XP saat bertransaksi di kedua bursa (IDX dan US).
    6. `report` (Laporan tepercaya): 300 XP saat menyelesaikan 3 laporan evaluasi klien.
  - Sifat: Authoritative domain, Idempoten (hanya diberikan 1 kali, tidak ada duplikasi pada reload/refresh), 0 hadiah uang tunai, tersimpan di SaveState, dan tereset saat New Game.
- **Agregasi Bar OHLCV di Domain**:
  - Logika agregasi `TradeFill` per interval waktu ke `HistoricalBar` (Open, High, Low, Close, Volume). Volume dihitung 1 kali per transaksi. Bila belum ada transaksi, bar referensi menggunakan harga dasar instrumen tanpa volume palsu.

### E. Blocker Eksternal yang Berada di Luar Scope
- **OQ-03 s/d OQ-05 (Dataset Historis Replay Nyata)**: Status tetap BLOCKED jujur mematuhi audit lisensi data. UI tetap menyajikan notice jujur tanpa memalsukan replay sebagai data historis riil.
- **Pengujian Perangkat Fisik/ADB**: Status tetap PENDING by Scope sesuai batas mandat pemilik (tanpa perangkat/ADB).

---

## 4. Pemetaan 40 Layar PRD & 26 Cakupan Legacy

| ID | Layar PRD | Status Source Game | Penyesuaian Desain Revisi 2 |
| :--- | :--- | :--- | :--- |
| S01 | Mulai / Lanjut | Terhubung via modal Entry | Mengadopsi palet Graphite, penanganan resume run |
| S02 | Permainan Baru | Terhubung modal New Game | Pemilihan modal awal, reset progres XP & run |
| S03 | Tutorial | Terhubung langkah interaktif | Panduan awal dengan token Graphite |
| S04 | Beranda | Terhubung tab Beranda | Header kekayaan pribadi kanan atas, ribbon indeks, agenda |
| S05 | Briefing | Terhubung modal Briefing | Ringkasan kondisi pasar sebelum trading |
| S06 | Pasar & Watchlist | Terhubung tab Pasar | Pencarian, filter sektor/bursa, pengurutan, sparkline |
| S07 | Indeks | Terhubung rincian indeks | 9 indeks (IHSG, USTECH, S&P 500, dsb.) berlabel model jujur |
| S08 | Detail Instrumen | Terhubung flow instrumen | Candlestick OHLCV, volume, 10 tingkat book, tape, alternatif tabel |
| S09 | Tiket Order | Terhubung bottom sheet order | Pilihan Beli/Jual sadar tanpa default tersembunyi, konteks akun |
| S10 | Review Order | Terhubung modal Review | Estimasi dampak kas & fee domain, pembatalan jika akun berubah |
| S11 | Status Order / Receipt | Terhubung modal Receipt | Rincian fill, sisa antrean, partial fill, idempotensi submit |
| S12 | Riwayat Order | Terhubung modal Order History | Daftar order aktif dan tereksekusi per akun |
| S13 | Aset / Portofolio | Terhubung tab Aset | NAV, kas tersedia, alokasi aset, posisi saham multi-currency |
| S14 | Posisi Instrumen | Terhubung di Aset | Rincian lembar, harga rata-rata, harga pasar, floating P/L |
| S15 | Buku Besar (Ledger) | Terhubung di Aset/Settings | Entri mutasi kas ganda (debit/kredit) per akun |
| S16 | FX Konversi IDR/USD | Terhubung modal FX | Quote 30s, preview selisih kurs, eksekusi idempoten |
| S17 | Laporan Sesi Harian | Terhubung modal Sesi/CloseDay | Evaluasi PnL harian, fee, gaji manajer |
| S18 | Karier & Misi | Terhubung modal Karier | Jabatan profesional (Novice -> CIO) berdampingan dengan XP |
| S19 | Firma & AUM | Terhubung tab Firma | Pemisahan tegas AUM klien vs kas pribadi, pendapatan fee |
| S20 | Klien & Penawaran | Terhubung modal Klien | Profil risiko klien konservatif, moderat, agresif |
| S21 | Mandat Klien | Terhubung modal Mandat | Penerimaan mandat resmi, syarat alokasi & batas loss |
| S22 | Laporan Klien | Terhubung modal Client Report | Tracking kinerja dana kelolaan, penagihan fee periodik |
| S23 | Fund Builder | Terhubung modal Fund Builder | Bobot aset target, validasi total 100%, eksekusi rebalancing |
| S24 | Multi-Aset | Terhubung modal MultiAsset | Katalog obligasi & komoditas (satuan unit & karakteristik) |
| S25 | Riset & Berita | Terhubung tab Riset | Feed berita terbitan game ber-timestamp, penanda dibaca |
| S26 | Dokumen / Analis | Terhubung artikel riset | Catatan riset analis, membuka artikel menambah progres baca |
| S27 | Analis Konsensus | Terhubung laporan analis | Target harga model, proyeksi sektor |
| S28 | Kalender Bursa | Terhubung kalender | Agenda RUPS, dividen, rilis data ekonomi |
| S29 | Paket Terminal | Terhubung modal Paket | Tier Basic, Pro, Institusi |
| S30 | Billing Langganan | Terhubung status billing | Masa aktif 30 hari, auto-downgrade saat kedaluwarsa |
| S31 | Aksi Korporasi | Terhubung modal Corporate | Dividen tunai otomatis, pemecahan saham (stock split) |
| S32 | IPO Center | Terhubung modal IPO | Penawaran saham perdana, penjatahan saat tutup hari |
| S33 | Voting RUPS | Terhubung modal Voting | Pemberian suara pemegang saham sesuai jumlah lembar |
| S34 | Risiko & Kepatuhan | Terhubung modal Risiko | Rasio margin, total exposure, deteksi margin call |
| S35 | Margin & Short | Terhubung fasilitas Margin | Peminjaman kas margin, short selling saham berlikuiditas |
| S36 | Suspensi / Halt | Terhubung status halt instrumen | Tampilan status pembatasan perdagangan, proteksi order reject |
| S37 | Stress Test | Terhubung modal Stress Test | Skenario krisis moneter, tech bubble, komoditas |
| S38 | Strategy Lab | Terhubung modal Strategy Lab | Sandbox pengujian simulasi tanpa memengaruhi akun utama |
| S39 | Replay Historis | Terhubung modal Replay | Penolakan jujur UNAVAILABLE_NOTICE mematuhi lisensi dataset |
| S40 | Pengaturan & Recovery | Terhubung modal Settings | Backup save, restore snapshot, opsi teks besar, reset game |
| + | Level & Achievement | Layar baru (Rute `achievements`)| 6 achievement, XP track, filter status terbuka/terkunci |
