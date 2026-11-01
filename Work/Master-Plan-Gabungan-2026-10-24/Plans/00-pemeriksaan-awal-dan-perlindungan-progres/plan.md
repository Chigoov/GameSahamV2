# PLAN-00 — Pemeriksaan Awal dan Perlindungan Progres

**Status: SELESAI PENUH & TERVERIFIKASI. BERHENTI MENUNGGU INSTRUKSI PEMILIK.**

- **Paket:** P00 — Pemeriksaan awal dan perlindungan progres
- **Tanggal:** 28 Oktober 2026, Asia/Jakarta
- **Lokasi Plan:** `Work/Master-Plan-Gabungan-2026-10-24/Plans/00-pemeriksaan-awal-dan-perlindungan-progres/plan.md`
- **Root Proyek:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`
- **Tujuan P00:** Memastikan identitas source, inventarisasi artefak/review/APK, memetakan keputusan master plan secara jujur, mengaudit kode domain kritis, menyiapkan save QA terisolasi tanpa memutasi save pemain asli, dan menyusun peta dependensi paket berikutnya sebelum melanjutkan.

---

## 1. Identitas Source dan Perlindungan Baseline

1. **Root Direktori Aktif:**
   - Path absolut: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`
   - Verifikasi isolasi: Terpisah sepenuhnya dari direktori 3D lama (`C:/Users/HYPE AMD/Downloads/VIBE CODING/Game Trading Saham`). Tidak ada pembacaan atau impor otomatis dari repo lama.
2. **Status Git & Branch:**
   - Branch aktif: `codex/classic-ui-new-assets`
   - Tracking upstream: `origin/codex/classic-ui-new-assets`
   - Commit HEAD: `2b6ee5c Add game plans, references and work artifacts`
3. **Modifikasi Baseline (Uncommitted Changes Dipertahankan):**
   - 1 berkas termodifikasi: `Work/Master-Plan-Gabungan-2026-10-24/MASTER-PLAN-EKSEKUSI-MASUKAN-PEMAIN.md` (penyesuaian teks keputusan A06 hapus replay, R8 hapus penanda simulasi, dan scope M4).
   - 4 berkas untracked:
     - `MASTER-PLAN-OFFLINE-DAN-REALISME-2026-10-28.md`
     - `Work/Master-Plan-Gabungan-2026-10-24/DRAFT-PLAN-PEMEGANG-SAHAM-IPO-PEMAIN.md`
     - `hs_err_pid24396.log`
     - `replay_pid24396.log`
   - Tindakan: Seluruh perubahan lokal dipertahankan 100% tanpa git reset, clean, stash, atau checkout paksa.

---

## 2. Inventarisasi Build, APK, Laporan dan Hasil Review

1. **Artefak APK Terkini:**
   - **APK-08:** `Artifacts/Eksekusi-Masukan-Pemain-2026-10-28/apk/GameSaham-APK08-revisi-final.apk`
     - Ukuran: 14.299.879 bytes (13,64 MB)
     - SHA-256: `87ae6d3f60b466cd451314da1e850e54dd4c474f786aa2f12270086ce6808681`
     - Tanda tangan: Android Debug (V2 Signature valid).
   - **APK-09:** `Artifacts/Persiapan-Aplikasi-2026-10-28/apk/GameSaham-APK09-rilis-awal.apk`
     - Ukuran: 14.745.457 bytes (14,06 MB)
     - SHA-256: `2c0d3aa2f8069e7e030266ab22f0354b7f9954712c17304107d33f09db736aca`
     - Tanda tangan: Android Debug (V2 Signature valid).
   - *Catatan Kritis:* Keduanya berstatus build Debug. Belum ada release signing produksi dan belum siap rilis toko aplikasi.
2. **Temuan Kritis Review Independen APK-09 (`Artifacts/Review-Kandidat-APK09-2026-10-28/LAPORAN-REVIEW-INDEPENDEN.md`):**
   - **Temuan Tinggi 1 (Denda Ganda):** Denda lama 0,05% per hari game di `LoanContract.java:239` masih dieksekusi bersamaan dengan denda baru 5% per 24 jam nyata di `TradingFacade.java:1699`. Kebijakan 5% belum berdiri sendiri.
   - **Temuan Tinggi 2 (Reset Game Baru Belum Lengkap):** Pengujian reset pada `NewGameAndResetRegressionTest` hanya memvalidasi saldo, posisi, pinjaman sintetis, dan koleksi; belum membentuk atau memvalidasi domain order, bisnis, misi, level karier, dan histori. Rotasi backup `SaveManager` berisiko memulihkan data lama jika terjadi kegagalan save.
   - **Temuan Tinggi 3 (Approval Desain Galeri Belum Ada):** Tiga alternatif desain galeri belum disetujui secara eksplisit oleh pemilik sebelum kode galeri diubah di `app.js`.
   - **Temuan Sedang 4 (Benchmark Tidak Mengukur Operasi Riil):** Skrip `benchmark-performance-real.cjs` hanya menunggu DOMContentLoaded dan container UI, bukan bukti save termuat atau render chart selesai; tidak mengirim order transaksi riil.
   - **Temuan Sedang 5 (Ketidaksinkronan Data Laporan):** Angka ringkasan performa pada laporan naratif berbeda dengan nilai mentah JSON.
   - **Temuan Sedang 6 (Produk Beragunan Masih Tersedia):** `InstallmentLoanManager.java` masih memuat produk pinjaman beragunan `LOAN-PERS-01`.
3. **Status Save Pemain Asli (HASH_ONLY):**
   - Berkas: `Artifacts/LocalPreview/preview-save/save_game_v2.json`
   - SHA-256: `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`
   - Kondisi: Utuh, terverifikasi byte-for-byte, tidak dimutasi atau disentuh.
4. **Status Port Preview & Runner:**
   - Port 18917, 18924, 18925, 8794, 8000 diperiksa: bersih dari listener liar / runner zombie.

---

## 3. Pemetaan Keputusan Master Plan

Keputusan dari `MASTER-PLAN-OFFLINE-DAN-REALISME-2026-10-28.md` dan `DRAFT-PLAN-PEMEGANG-SAHAM-IPO-PEMAIN.md` dipetakan ke dalam 3 kategori ketat:

### Kategori A — Sudah Dipilih Pemilik (Keputusan Terkunci / Arah Resmi)
1. **K01:** Game ditujukan untuk Android dan dapat dimainkan offline.
2. **K02 / A06:** Hapus fitur replay historis karena tidak memengaruhi pasar utama; penanganan save replay lama menunggu D06.
3. **K03:** Tolak usulan tutorial wajib pembelian saham pertama; panduan sukarela tetap dipertahankan.
4. **K04 / R8:** Hapus penanda "harga simulasi game" pada pasar utama.
5. **K06:** Waktu otomatis 24× (1 hari game = 1 jam nyata) konsisten pada aktif, background, dan aplikasi ditutup; tanpa tombol pause, speed, atau skip.
6. **K07:** Efek berita 60/120 menit nyata dengan deadline persisten.
7. **K08:** Tarif, hadiah, formula ekonomi, desain visual navy gelap dan navigasi Beranda/Pasar/Portofolio/Bisnis/Lainnya tidak diubah diam-diam.
8. **Transaksi Pasar Pemain:** Hanya terisi penuh (full fill) atau tidak terisi (unfilled). Tidak ada order terisi sebagian (partial fill) untuk pemain.
9. **Ketersediaan Saldo:** Jika harga bergerak dan saldo tidak cukup saat order diproses, order gagal dan dana tertahan dikembalikan penuh. Tidak ada fitur saran jumlah lebih kecil.
10. **Lawan Transaksi:** Tidak menambah karakter lawan transaksi khusus.
11. **Order Saat Pasar Tutup:** Order Limit & Market dapat dibuat saat bursa tutup; dana beli langsung ditahan saat order dibuat; order disimpan dan baru diteruskan saat bursa buka. Pembatalan sebelum buka melepas dana/saham tertahan. Jika saham disuspensi saat buka, order ditolak dan dana/saham dilepas.
12. **Masa Berlaku Order:** Order yang belum terisi tetap aktif sampai dibatalkan pemain (GTC).
13. **Kalender Game:** Bursa buka setiap hari game.
14. **SH01 / Q01:** Tampilkan pemegang saham kepemilikan 1% atau lebih dan kepemilikan publik, urut terbesar ke terkecil (Q03), dengan nama, lembar/lot, dan persentase (Q04). Diperbarui saat halaman dibuka/di-refresh (Q05).
15. **SH02 / Q02:** NPC memiliki beragam saham sesuai kekayaannya.
16. **SH03 / Q06:** Hak suara, usulan agenda, dan kendali bertahap berdasarkan porsi saham berhak (Q09); hak langsung menyesuaikan saat kepemilikan turun (Q08). Saham akun klien terpisah dari akun pribadi (Q10).
17. **IPO01 / Q13:** IPO emiten lain menampilkan Profil, Keuangan, Risiko, dan Rencana IPO bertab (Contoh B). Pemesanan dapat dibatalkan selama masa penawaran sebelum penjatahan (Q14).
18. **IPO02 / Q12 / Q15:** Sisa dana IPO yang tidak terpakai dikembalikan otomatis; saham IPO tercatat dan dapat diperdagangkan di pasar sekunder game.
19. **IPO03 / Q11:** Ambang pesanan besar minimal 20% dari total penawaran; penjatahan kelebihan pesanan dibagi awal 60% kelompok besar dan 40% pemesan lain; sisa lot dialihkan antarkelompok proporsional. Sisa pecahan lot terbesar mendapat lot utuh; bila pecahan sama (seri), diundi oleh game. SEOJK proporsional disederhanakan tanpa jatah pasti 1 lot per pemesan.
20. **IPO04 / Q16 / Q17:** Perusahaan pemain bisa IPO setelah memenuhi syarat kesiapan, usia, dan keuangan. Dana hasil penerbitan saham baru masuk ke kas perusahaan (Q19). Pemain memilih sendiri jumlah saham baru yang ditawarkan tanpa saran game (Q18).
21. **IPO05 / Q20:** Pemain tetap memegang kendali penuh atas perusahaan miliknya setelah IPO.
22. **NPC01:** Tepat 1.000 NPC unik aktif di pasar dan mengisi leaderboard bersama; pemain adalah peserta terpisah.
23. **NPC02:** Peringkat mengikuti hasil kekayaan/portofolio nyata; strategi NPC beragam dan aktivitas berbeda (tidak semua bertransaksi setiap saat).
24. **NPC03:** Profil ringkas NPC dibagi dalam tab (Opsi B).
25. **NPC04:** Arah avatar menggunakan 100 contoh avatar anime sebagai dasar (5 sheet @ 20 avatar); 1 avatar boleh dipakai ulang untuk NPC berbeda dengan nama campuran Indonesia dan internasional.
26. **KP01:** Misi awal bebas dipilih, tidak memaksa beli saham.
27. **KP02:** Tampilkan semua syarat dan progres karier (Contoh A).
28. **KP03:** Klaim hadiah misi otomatis langsung saat misi selesai (termasuk saat rekonsiliasi offline).
29. **KP04:** Dua kelompok kegiatan pasar tutup (riset/pantauan dan bisnis/aset) dapat diakses bersama pembuatan order antre.
30. **KP05:** Pertahankan nominal sponsor harian saat ini.
31. **KP06:** Tampilkan perbandingan pemasukan, biaya, dan proyeksi laba sebelum/sesudah keputusan bisnis.
32. **KP07:** Sediakan pencapaian minat dan ide kegiatan opsional (keduanya).
33. **KP08 / D05:** Daftar seluruh kejadian yang tercatat saat kembali bermain (Contoh B).
34. **KP09 / D07:** Notifikasi ponsel mencakup 5 jenis: order, berita, IPO, bisnis, tagihan; aktif secara awal dan pemain dapat mengatur toggle per jenis.
35. **KP10:** Sasaran pencapaian besar pertama pemain adalah 1–3 hari nyata melalui 3 gaya bermain (aktif trading, bisnis, santai/offline).
36. **Kebijakan Pembiayaan:** Denda 5% per 24 jam nyata atas cicilan belum dibayar; denda lama 0,05% per hari game dihapus; agunan dilepas dan utang lama tetap ada.

### Kategori B — Masih Menunggu Jawaban atau Preview (Pending Approval)
1. **D01 (Jadwal Spesifik Bursa):** Jam buka dan jam tutup bursa per hari game (contoh 09.00–16.00 belum dipilih; pemilik meminta bursa lebih sering buka, alternatif jam spesifik perlu disajikan).
2. **D03 (Besar Respons Berita):** Adaptasi bertingkat kecil/sedang/besar vs pendekatan lain.
3. **D04 (Formula Bisnis):** Nilai kuantitatif pengaruh keputusan operasional terhadap laba, pendapatan, dan valuasi perusahaan.
4. **D06 (Save Replay Lama):** Kebijakan pemulihan/migrasi save replay lama milik pemain.
5. **O04 (Rincian Market Order):** Formula perkiraan dana maksimum yang disarankan game, buffer fee, durasi saat likuiditas kurang, dan revisi penamaan antarmuka.
6. **Definisi Kecocokan Harga:** Kriteria pasti harga cocok (matching price criteria) dan toleransi harga tanpa batasan fraksi BEI formal.
7. **Rincian Hak Voting & Kendali Bertahap:** Ambang persentase kepemilikan saham untuk hak usul agenda dan veto.
8. **Syarat Numerik IPO Perusahaan Sendiri:** Batas minimal usia usaha, kas/laba minimal, dan batas maksimal persentase saham publik.
9. **Dasar Komponen Cicilan Denda 5%:** Kepastian apakah denda 5% dikenakan atas pokok overdue saja atau pokok + bunga cicilan.
10. **Desain UI yang Wajib Preview & Approval Klik Pemilik:**
    - Preview UI penghapusan menu Replay Historis (P01).
    - Preview UI penghapusan penanda "Harga simulasi game" pada pasar utama.
    - 3 alternatif desain galeri koleksi mewah 150 aset (A15 / G01).
    - Preview dialog order tengah layar dengan tahapan order (A03 / O04).
    - Preview daftar seluruh kejadian saat kembali bermain (KP08 / D05).
    - Preview tab informasi perusahaan IPO (I01 / Q13).
    - Preview tab profil ringkas NPC (NPC03).
    - Preview toggle 5 pengaturan notifikasi ponsel (KP09 / D07).
    - Preview tampilan pemegang saham 1%+ dan publik (SH01).

### Kategori C — Perlu Dibuktikan pada Source atau Pengujian
1. **Verifikasi Denda Ganda:** Pembuktian kode `LoanContract.java:239` (0,05%) dan `TradingFacade.java:1699` (5% 24h) pada alur akrual produksi.
2. **Eliminasi Partial Fill Pemain:** Membuktikan transisi status di `MatchingEngine.java` dan `OrderManager.java` bahwa order pemain tidak pernah menerima status `PARTIALLY_FILLED`.
3. **Isolasi Replay Engine:** Membuktikan bahwa pemotongan `HistoricalReplayEngine.java` tidak merusak `HistoricalDatasetLoader.java`, chart OHLCV, atau instrumen catalog pasar utama.
4. **Verifikasi Idempotensi 24× Offline:** Membuktikan rekonsiliasi waktu offline di `GameClock.java` tidak menghasilkan perhitungan ganda pada denda, dividen, bunga deposito, atau klaim misi otomatis.
5. **Kapasitas 1.000 NPC:** Mengukur performa CPU, alokasi memori, waktu serialisasi/deserialisasi JSON save, dan query SQLite saat `LeagueManager.java` menampung 1.000 NPC unik.
6. **Status Aset Avatar Anime:** Membuktikan kondisi aset avatar saat ini (masih berupa 5 spritesheet PNG mentah berukuran ~14 MB) dan perlunya cropping menjadi 100 WebP terpisah.
7. **Penyelidikan Bug `saveFailed`:** Membuktikan mengapa `saveFailed` di `MainActivity.java:137` tidak pernah diubah nilainya menjadi true saat save gagal.
8. **Izin & Background Notifikasi Android:** Membuktikan kesiapan `POST_NOTIFICATIONS` di Android 13+ dan perlunya AlarmManager/WorkManager untuk notifikasi saat game ditutup.
9. **Benchmark Transaksi Riil:** Menjalankan benchmark kinerja riil (bukan mock selector) pada tiket order, submit order, render chart, dan pergantian tab.

---

## 4. Hasil Audit Domain Kode Kritis

| Domain | Berkas Kunci | Temuan Sumber Kode Aktual |
|---|---|---|
| **Waktu 24×** | `GameClock.java`, `MarketSession.java` | Laju tunggal 24× (1 jam nyata = 1 hari game) telah diterapkan di core Java. Sesi pasar IDX saat ini masih mengasumsikan jam statis (09.00–16.00) dan AS dalam WIB. Menunggu keputusan jam spesifik D01. |
| **Pasar & Order** | `Order.java`, `MatchingEngine.java`, `OrderManager.java` | `OrderStatus.PARTIALLY_FILLED` masih ada di enum dan logika engine. Keputusan pemilik menghendaki hanya terisi penuh atau tidak terisi untuk pemain; sisa ditolak/dibatalkan. |
| **Berita** | `NewsArticle.java`, `ResearchService.java` | Deadline persisten 60/120 menit nyata terhubung ke `MarketEngine`. Menunggu keputusan D03 untuk skala intensitas respons pasar. |
| **Bisnis** | `OperatingCompany.java`, `CompanyManager.java` | Model operasional bisnis, staf, dan biaya berjalan. Menunggu keputusan D04 mengenai formula dampak keputusan terhadap laba & valuasi. |
| **Pinjaman & Denda** | `LoanContract.java`, `InstallmentLoanManager.java` | **Terverifikasi temuan APK-09:** `PENALTY_RATE = 0.0005` (0,05%) masih dieksekusi di `accrueDailyInterest()` baris 239 bersamaan dengan 5% di `TradingFacade.java:1699`. Produk beragunan `LOAN-PERS-01` masih ada di registry. |
| **Save & Sync** | `SaveManager.java`, `MainActivity.java` | `saveFailed` di `MainActivity.java:137` tidak pernah diset bernilai `true`. Kegagalan native tersembunyi. Pengujian reset belum mencakup seluruh entitas domain. |
| **Replay Historis** | `HistoricalReplayEngine.java`, `TradingFacade.java` | Replay mode memiliki cabang di setidaknya 15 titik di `TradingFacade.java` dan `TradingWebBridge.java`. Pemotongan harus rapi tanpa merusak chart pasar utama. |
| **IPO & Saham** | `CorporateActionService.java`, `IpoOffering.java` | Logika penawaran IPO emiten lain ada. Logika IPO perusahaan sendiri (IPO04/Q16) dan kendali pasca IPO (IPO05/Q20) belum terimplementasi di domain. |
| **Liga & NPC** | `LeagueManager.java`, `LeagueCompetitor.java` | **Terverifikasi:** Roster saat ini di-hardcode tepat 20 nama (`DEFAULT_NAMES.length == 20`). Penambahan 1.000 NPC membutuhkan migrasi arsitektur roster yang aman agar save restore tidak melempar exception. |
| **Avatar Anime** | `Artifacts/Preview-Saran-2026-10-28/` | Terverifikasi: 100 avatar anime saat ini hanya berupa 5 file PNG sheet mentah (`avatar-anime-01-20.png` s/d `avatar-anime-81-100.png`), belum di-slice menjadi WebP individual dan belum terpasang di assets aplikasi. |
| **Penanda Simulasi**| `android/app/src/main/assets/www/app.js` | Tiga lokasi penanda ditemukan: baris 3969 (indeks), 4006 (harga saham), dan 4349 (fundamental). Siap dihapus setelah preview UI. |

---

## 5. Lingkungan Pengujian dan Isolasi Save QA

1. **Proteksi Save Pemain:**
   - Berkas: `Artifacts/LocalPreview/preview-save/save_game_v2.json`
   - Hash SHA-256 Terverifikasi: `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed`
   - Kebijakan: HASH_ONLY. Tidak ada penulisan, pemutasian, atau penghapusan save pemain asli dalam P00 maupun paket selanjutnya.
2. **Direktori Save QA Terisolasi:**
   - Path absolut: `Artifacts/Master-Plan-Gabungan-2026-10-24/Plans/00-pemeriksaan-awal-dan-perlindungan-progres/qa-save/`
   - Seluruh pengujian masa depan menggunakan folder sandbox ini.

---

## 6. Inventarisasi Dependensi Paket Lanjutan (P01–P09) dan Analisis Risiko

```
[P00: Baseline & Proteksi] (SELESAI)
           │
           ▼
[P01: Hapus Replay Secara Aman] ──► Menunggu D06 (Save Replay Lama)
           │
           ▼
[P02: Keamanan Save & Reset] ──► Perbaikan saveFailed & Reset Lengkap
           │
           ├──────────────────────────────┐
           ▼                              ▼
[P03: Jadwal Pasar & Order Tutup]   [P05: Hubungan Bisnis & Laba]
(Menunggu D01 Jam Bursa)            (Menunggu D04 Formula Bisnis)
           │                              │
           ▼                              │
[P04: Berita & Respons Pasar]            │
(Menunggu D03 Respons Berita)             │
           │                              │
           ├──────────────────────────────┘
           ▼
[P06: Konsistensi Ekonomi, Denda 5% & Offline 24×]
(Perbaikan Denda Ganda APK-09 & Eliminasi Partial Fill Pemain)
           │
           ▼
[P07: Kemudahan Bermain & Keseimbangan (Playtest 1-3 Hari)]
(Uji 3 Gaya Bermain & Progres Minat)
           │
           ▼
[P08: Notifikasi 5 Jenis & Uji Android Offline]
(Izin POST_NOTIFICATIONS & Background Lifecycle)
           │
           ▼
[P09: Kandidat Rilis & Keputusan Publikasi]
(Build Release Signing, Benchmark Riil, Rekonsiliasi Final)
```

### Analisis Risiko & Hambatan Kritis:
1. **Risiko Regresi Denda Pinjaman (P06):** Menghapus denda harian 0,05% di `LoanContract.java` harus memastikan perhitungan denda 5% per 24 jam nyata mencatat riwayat cicilan secara akurat, tidak memicu denda berulang saat offline catch-up, dan tidak merusak utang lama.
2. **Risiko Penghapusan Replay (P01):** Replay Engine memiliki keterikatan erat dengan TradingFacade. Pemotongan harus dipastikan tidak merusak dataset lilin historis nyata BBCA/AAPL yang dipakai untuk referensi riset/chart.
3. **Risiko Skalabilitas 1.000 NPC & Deserialisasi JSON:** Memperluas 20 peserta liga menjadi 1.000 NPC aktif berpotensi meningkatkan ukuran file save JSON secara signifikan dan memperlambat startup/load game di Android WebView. Perlu strategi paging atau lazy-loading di domain Java.
4. **Kepatuhan Larangan ADB & Perangkat (`AGENTS.md`):** Seluruh pengujian native/Android fisik tidak boleh dilakukan secara sepihak tanpa instruksi eksplisit pemilik.

---

## 7. Rekomendasi Urutan Paket Berikutnya

1. **Rekomendasi Paket Segera:** **P01 — Hapus replay historis dengan aman**, diikuti langsung oleh **P02 — Perbaiki keamanan penyimpanan dan sinkronisasi** dan penutupan temuan denda ganda APK-09 pada **P06**.
2. **Tindakan Saat Ini:** P00 telah selesai penuh dan terverifikasi. Sesuai mandat: **BERHENTI DAN MENUNGGU INSTRUKSI PEMILIK**. Tidak ada eksekusi paket berikutnya secara otomatis.
