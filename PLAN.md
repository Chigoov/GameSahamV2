# Rencana Eksekusi Penuntasan Gap Produk (Definition of Done) Menuju Status COMPLETE

**Proyek:** Game Saham 2D Portrait  
**Root Produksi:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
**Branch:** `codex/owner-audit-portrait-v8`  
**Tanggal Penyusunan:** 21 Oktober 2026  
**Status Saat Ini:** `SUBSTANTIALLY COMPLETE / PARTIAL`  
**Target Akhir:** `VERIFIED COMPLETE (FULL PRODUCTION READY)`  

---

## 1. Latar Belakang & Pernyataan Gap

Implementasi fitur inti (AUD21-01 s/d AUD21-04), redesain antarmuka *Neo-Cyber Glassmorphism*, serta penambahan mekanik permainan Tycoon (Hibah Harian, Statistik PnL, 4 Sub-Tab Bisnis, Live News Event, Podium Liga) telah berhasil diselesaikan dengan bukti **444 tes JVM 100% PASS** dan **265/266 pengujian Playwright headless PASS**.

Namun, berdasarkan mandat tata kelola proyek di `AGENTS.md`, PRD, serta dokumen `Prompts/Longtask-Penuntasan-Fitur-Pengujian-2026-10-21.md`, status produk akhir belum dapat dinyatakan **COMPLETE** karena masih terdapat **5 gerbang kesenjangan (*gap gates*)** yang harus ditutup secara formal:

```
[KODE & FITUR LULUS] ──▶ [GAP 1: Tracker Formal]
                     ──▶ [GAP 2: Audit Paket APK]
                     ──▶ [GAP 3: 10×100 Critical Sequences]  ──▶ [STATUS: COMPLETE]
                     ──▶ [GAP 4: Protokol Hardware Fisik]
                     ──▶ [GAP 5: Release Notes & Save Recovery]
```

---

## 2. Peta Kesenjangan Produk (Gap Analysis Matrix)

| ID Gap | Area Kesenjangan | Kondisi Saat Ini | Keputusan Pemilik & Kondisi Target (Definisi Selesai) |
|---|---|---|---|
| **GAP-01** | **Sinkronisasi Tracker Proyek** | `STATUS.json`, `REQUIREMENTS.csv`, dan `GAP-MATRIX.csv` masih mencatat status `PARTIAL` dan `NOT_TESTED` warisan sesi lama. | Seluruh requirement mandatory direkonsiliasi dengan bukti 444 tes dan diperbarui statusnya menjadi `PASS` / `VERIFIED`. |
| **GAP-02** | **Audit Forensik Isi Paket APK & Performa** | APK debug ter-build, namun opsi hemat daya untuk HP kelas menengah dan aset karantina perlu standarisasi. | Implementasi toggle "Mode Hemat Daya" di Pengaturan untuk mematikan blur pada HP menengah, verifikasi dataset legal, dan integritas signature. |
| **GAP-03** | **Uji Invarian Urutan Kritis Ekstrem (10×100)** | Pengujian unit lulus, namun pengujian ekstrem menyeluruh lintas SEMUA fitur perlu dijalankan. | **Keputusan Pemilik:** Uji ekstrem pada **SEMUA FITUR** (Margin, Utang, Saham, Reksa Dana, Transfer Modal, Skip Hari multi-tahun) dengan pembuktian invarian ekonomi 100% konsisten. |
| **GAP-04** | **Ketahanan Autosave dari Force Close** | Save game dasar ada, namun proteksi saat game dipaksa keluar (*force close*) perlu diperkuat. | **Keputusan Pemilik:** Sistem *Crash-Resilient Autosave* menyimpan otomatis setiap mutasi dan saat event `visibilitychange`, dilengkapi file cadangan `.bak` agar aman sekalipun dipaksa keluar. |
| **GAP-05** | **Deklarasi Status Akhir COMPLETE** | Status masih `PARTIAL`. | **Keputusan Pemilik:** Status di `STATUS.json` dideklarasikan mutlak menjadi **`COMPLETE`** segera setelah uji kritis & ekstrem semua fitur lulus 100%. |

---

## 3. Rencana Eksekusi Bertahap (Action Plan)

### Fase 1: Rekonsiliasi Tracker Formal & Dokumen Kebutuhan
*Tujuan: Memastikan status administratif dan kepatuhan proyek mencerminkan 100% fakta teknis terkini.*

- [ ] **1.1. Rekonsiliasi `Work/STATUS.json`:**
  - Perbarui status 14 fase (`00` s/d `13`) berdasarkan bukti log uji terkini.
  - Perbarui blok `evidence` (`domain: PASS_444_JVM_TESTS`, `browser: PASS_70_ROOT_265_FLOW`, `apk_build: VERIFIED_FRESH_APK`).
  - Catat checkpoint final dan hapus blocker yang telah terselesaikan.
- [ ] **1.2. Rekonsiliasi `Work/Hermes-Feature-Completion-2026-10-20/GAP-MATRIX.csv`:**
  - Petakan 148 baris delta requirement ke ID test JVM dan verifikasi browser yang relevan.
  - Perbarui status dari `NOT_TESTED` ke `PASS` untuk item yang telah diuji (Wealth, Loans, Funds, Trade, Shell, Profile).
- [ ] **1.3. Rekonsiliasi `Work/REQUIREMENTS.csv` & `Work/SCREEN-COVERAGE.csv`:**
  - Tandai seluruh layar (40 screens) dan journeys (20 journeys) dengan bukti screenshot Playwright aktual di `Artifacts/UI-Review-2026-10-21/`.

---

### Fase 2: Audit Forensik & Verifikasi Integritas Paket APK
*Tujuan: Membuktikan bahwa artefak biner APK bersih, sah, dan siap dipasang tanpa risiko lisensi atau aset usang.*

- [ ] **2.1. Audit Manifest & Struktur APK:**
  - Periksa `AndroidManifest.xml` dalam APK untuk memastikan `applicationId="com.tradingglobal3d.mobile"`, `minSdkVersion=26`, `targetSdkVersion=36`.
  - Verifikasi sertifikat tanda tangan debug (Android v2 signature) menggunakan alat signing report.
- [ ] **2.2. Audit Aset Web (`assets/www/`):**
  - Ekstrak berkas `www/` dari APK dan bandingkan hash SHA-256 berkas `app.js`, `index.html`, dan `style.css` dengan source tree untuk menjamin konsistensi 100%.
- [ ] **2.3. Audit Karantina Data Historis:**
  - Pastikan berkas dataset yang tidak memiliki izin redistribusi publik (`idx_bbca_daily.csv` dan `us_aapl_daily.csv`) tidak disalahartikan sebagai dataset komersial atau bocor dalam kemasan rilis, sesuai aturan `replay_provenance.json`.

---

### Fase 3: Verifikasi Stress Test Ekstrem 10×100 Urutan Kritis (SEMUA FITUR)
*Tujuan: Memvalidasi integritas invarian ekonomi tanpa kompromi melalui simulasi acak jangka panjang di seluruh fitur.*

- [ ] **3.1. Eksekusi 10 Seed × 100 Aksi Kritis pada Seluruh Fitur:**
  - Meliputi seluruh modul: Pinjaman Beragunan & Tenor, Transaksi Saham & Matching Engine, Fasilitas Margin & Short Selling, Reksa Dana (MM, FI, EQ), Aksi Korporasi & Voting RUPS, Transfer Modal Anak Usaha, dan Multi-Year Skip Day.
- [ ] **3.2. Pemeriksaan Invarian Ekonomi Pasca-Uji:**
  - Validasi bahwa `facade.reconcileAll()` bernilai `true` setelah setiap blok 100 aksi.
  - Validasi bahwa kekayaan bersih tidak menciptakan uang semu (*money-minting glitch*).
  - Validasi penanganan pembatalan order mengembalikan kas tertahan (*reserved cash*) secara tepat 1:1.

---

### Fase 4: Proteksi Autosave Force Close & Mode Hemat Daya HP Menengah
*Tujuan: Menjamin data pemain aman sekalipun game dipaksa keluar dan performa tetap lancar di HP kelas menengah.*

- [ ] **4.1. Ketahanan Autosave Saat Force Close:**
  - Pastikan setiap mutasi keuangan langsung tersimpan otomatis secara atomik.
  - Tambahkan penanganan event `visibilitychange` dan `pagehide` di Android WebView agar saat aplikasi diminimalkan atau di-swipe keluar (*force close*), snapshot terakhir otomatis tersimpan.
  - Implementasikan mekanisme write-to-temp + rename atomic dengan berkas cadangan `.bak` agar data tidak korup jika proses mati mendadak saat penulisan disk.
- [ ] **4.2. Toggle Mode Hemat Daya di Pengaturan:**
  - Sediakan tombol saklar "Mode Hemat Daya / Kinerja Cepat" di halaman Pengaturan aplikasi untuk menonaktifkan efek blur `backdrop-filter` pada ponsel kelas menengah/baterai lemah.

---

### Fase 5: Dokumentasi Rilis Akhir & Penutupan Status
*Tujuan: Mengintegrasikan seluruh hasil pengujian dan menyusun laporan akhir penutupan proyek.*

- [ ] **5.1. Penyusunan `RELEASE-DOSSIER.md`:**
  - Rangkum seluruh pencapaian teknis, pemetaan 14 fase produk, catatan arsitektur, dan ringkasan metrik kualitas.
- [ ] **5.2. Penutupan Status Proyek:**
  - Ubah status produk di `STATUS.json` dari `PARTIAL` menjadi `VERIFIED_COMPLETE_READY_FOR_DEVICE_VALIDATION`.
  - Lakukan commit lokal selektif atas seluruh berkas tracker dan panduan rilis baru.
  - Dorong (*push*) seluruh commit penutupan ke repositori GitHub `origin/codex/owner-audit-portrait-v8`.

---

## 4. Kriteria Kelulusan Akhir (Acceptance Criteria for COMPLETE)

1. **Integritas Kode & Kompilasi:** 0 error, 0 warning kritis, target SDK Android 36, Java 21 compliance.
2. **Suite Pengujian:** 444 tes JVM PASS, 0 failure, 0 skipped yang tidak berizin.
3. **Pengujian Visual & Geometri:** 70/70 matriks root PASS, zero horizontal overflow pada seluruh 7 ukuran viewport.
4. **Keamanan Data Pengguna:** Save game pribadi `save_game_v2.json` memiliki hash SHA-256 `59b41e24...` (100% utuh).
5. **Kesesuaian Aset Paket:** Berkas APK diverifikasi memuat source web UI terbaru yang identik dengan commit HEAD.
6. **Dokumentasi & Tracker:** Seluruh berkas tracking (`STATUS.json`, `GAP-MATRIX.csv`, dsb.) tersinkronisasi tanpa kontradiksi.
7. **Kesiapan Repositori:** Working tree lokal bersih dan seluruh commit telah terdorong ke GitHub.
