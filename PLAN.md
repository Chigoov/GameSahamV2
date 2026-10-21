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

| ID Gap | Area Kesenjangan | Kondisi Saat Ini | Kondisi Target (Definisi Selesai) |
|---|---|---|---|
| **GAP-01** | **Sinkronisasi Tracker Proyek** | `STATUS.json`, `REQUIREMENTS.csv`, dan `GAP-MATRIX.csv` masih mencatat status `PARTIAL` dan `NOT_TESTED` warisan sesi lama. | Seluruh requirement mandatory direkonsiliasi dengan bukti 444 tes dan diperbarui statusnya menjadi `PASS` / `VERIFIED`. |
| **GAP-02** | **Audit Forensik Isi Paket APK** | APK debug ter-build, namun isi struktur zip internal (assets, dex, manifest, exclusion data tidak berhak) belum diaudit per berkas. | Verifikasi bahwa dataset berlisensi tidak berhak tidak bocor ke APK, file asset `wwwroot` 100% identik dengan commit terbaru, dan signature valid. |
| **GAP-03** | **Uji Invarian Urutan Kritis (10×100)** | Pengujian unit lulus, namun pengujian keandalan multi-hari (10 seed × 100 aksi uang/order/waktu/save) perlu bukti eksekusi eksplisit pada source terkini. | Terbukti bahwa 10 urutan kritis mempertahankan invarian ekonomi (tidak ada uang gratis, saldo reconcile 100%, tidak ada overflow). |
| **GAP-04** | **Standarisasi Gerbang Perangkat Fisik** | Pengujian hanya dilakukan pada Playwright Chromium headless di Windows host (`PENDING_ATTACHED_DEVICE`). | Protokol pengujian fisik (*Hardware Acceptance Checklist*) disusun lengkap untuk validasi sentuh, keyboard IME, back button, dan GPU rendering di HP Android. |
| **GAP-05** | **Prosedur Pemulihan & Panduan Rilis** | Tidak ada panduan resmi untuk penanganan save game jika terjadi crash/korupsi dan dokumen rilis komprehensif 14 fase. | Dokumen *Disaster Recovery Protocol* dan *Comprehensive Release Dossier* tersedia untuk pemilik proyek. |

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

### Fase 3: Verifikasi Stress Test & 10×100 Urutan Kritis
*Tujuan: Memvalidasi integritas invarian ekonomi tanpa kompromi melalui simulasi acak jangka panjang.*

- [ ] **3.1. Eksekusi 10 Seed × 100 Aksi Kritis:**
  - Jalankan test suite `CorporateActionIpoVotingCriticalSequenceTest`, `MarketMatchingOrderHaltCriticalSequenceTest`, dan `MultiAssetCollectionFundCriticalSequenceTest`.
  - Simulasikan 10 urutan seed acak berulang: penempatan order pasar, limit, stop-loss, penarikan kas, transfer modal perusahaan, dan skip day.
- [ ] **3.2. Pemeriksaan Invarian Ekonomi Pasca-Uji:**
  - Validasi bahwa `facade.reconcileAll()` bernilai `true` setelah setiap blok 100 aksi.
  - Validasi bahwa kekayaan bersih tidak menciptakan uang semu (*money-minting glitch*).
  - Validasi penanganan pembatalan order mengembalikan kas tertahan (*reserved cash*) secara tepat 1:1.

---

### Fase 4: Standarisasi Protokol Uji Perangkat Fisik (Hardware Gate)
*Tujuan: Menyiapkan gerbang penyerahan fisik yang terstruktur bagi pemilik proyek untuk pengujian mandiri di HP Android.*

- [ ] **4.1. Pembuatan Lembar Uji Verifikasi Perangkat (*Hardware Acceptance Checklist*):**
  - Buat berkas panduan `Docs/QA/HARDWARE-TEST-GUIDE.md` yang memuat 10 skenario uji tangan langsung di ponsel:
    1. Uji responsivitas sentuhan pada kartu dan tombol navigasi dock bawah.
    2. Uji pop-up keyboard virtual (IME) pada input nominal uang dan lot saham tanpa merusak layout (*viewport resize check*).
    3. Uji penekanan tombol fisik *Back* Android saat modal flow terbuka (modal harus tertutup tanpa keluar dari aplikasi).
    4. Uji siklus hidup Android: minimalkan aplikasi (*Home button*), buka aplikasi lain, lalu kembali (*onResume*) untuk memastikan state tidak hilang.
    5. Uji rendering efek CSS blur/glassmorphism pada layar HP 60Hz/120Hz.
    6. Uji klaim Sponsor Harian `+Rp 25.000.000` di Beranda.
    7. Uji pembelian barang koleksi dan pengajuan pinjaman beragunan.
    8. Uji eksekusi order saham di tab Pasar.
    9. Uji transfer modal dua arah di tab Bisnis.
    10. Uji tampilan Podium 3 Besar di tab Liga & Peringkat.
- [ ] **4.2. Penyediaan Paket APK Rilis Bersih:**
  - Tempatkan berkas APK bertanda tangan di direktori rilis yang jelas dan sertakan hash SHA-256 untuk verifikasi integritas oleh pengguna.

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
