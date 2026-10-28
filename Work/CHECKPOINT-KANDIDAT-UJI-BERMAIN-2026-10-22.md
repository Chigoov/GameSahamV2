# Checkpoint & Laporan Kandidat Siap Uji Bermain (22 Oktober 2026)
**Target Status**: `READY_FOR_OWNER_PLAYTEST` (Host Engine & Browser Verified; Assets Pending Codex; Physical Android Deferred)  
**Root Proyek**: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
**Git Branch**: `codex/owner-audit-portrait-v8`  
**Baseline Git**: Commit `49bbafb` + Local Verified Working Tree Changes  
**Tanggal**: 22 Oktober 2026  

---

## 1. Ringkasan Eksekutif & Pemenuhan Lima Tahap (P1–P5)

| Tahap | Deskripsi & Ruang Lingkup | Status Verifikasi | Bukti Konkret |
|---|---|---|---|
| **P1** | **Verifikasi Penutupan Terbaru & Rekonsiliasi Bukti** | **PASS** | Rekonsiliasi ukuran APK (955.886 byte) dan test suite JVM (461 tes / 55 berkas XML PASS). Hash save pribadi `59b41e24...` terverifikasi 100% utuh dan tidak termodifikasi. |
| **P2** | **Uji Satu Rangkaian Bermain Utuh (E2E Playtest)** | **PASS** | 10/10 checks PASS pada Playwright headless (`Work/test-e2e-playtest-journey.py`). Alur lengkap: Init -> Beli 2 lot IDX -> Jual sebagian (100 lembar) -> Jual habis (0 saham live di modal terbuka) -> Konversi FX IDR->USD -> Beli saham US (US:AAPL) -> Pasang limit resting order & batalkan in-place -> Bisnis & penarikan modal -> Simpan & muat ulang (state konsisten). 17 tangkapan layar tersimpan di `Artifacts/Kandidat-Layak-Uji-Bermain-2026-10-22/playtest-journey/screenshots/`. |
| **P3** | **Uji Keberlanjutan Permainan & Penyimpanan (7 Hari + 10×100 JVM)** | **PASS** | 4/4 checks PASS (`Work/test-sustainability-and-storage.py`). Rangkaian 7 pergantian hari beruntun via `skipDay` dengan laporan operasional bisnis dan entri ledger harian. Verifikasi rasio dilatasi waktu: Aktif 288x (1d=5m), Background 24x (1d=1h). Suite kritis JVM `AllFeaturesExtreme10x100CriticalSequenceTest` dan `TimeDualRateLifecycleTest` lulus 100% (durasi 247.6s). |
| **P4** | **Kesiapan UI, Aksesibilitas 7 Viewport & Benchmark Performa** | **PASS** | 5/5 checks PASS (`Work/test-p4-viewports-accessibility.py`). 7 Viewport (320x568 s/d 1440x900) × 2 Skala Font (100% & 200%) = 70 sampel матрикс tanpa scroll overflow. Target sentuh tombol >= 44px, fokus keyboard aktif, mode hemat daya aktif. Benchmark responsivitas 32 sampel per aksi (`Work/benchmark-performance-final.py`): Navigasi P95 = 90.30 ms, Aksi Biasa P95 = 95.30 ms, Pekerjaan Berat SkipDay P95 = 161.84 ms. |
| **P5** | **Review, Build APK Kandidat Baru & Panduan Uji HP** | **PASS** | APK kandidat berhasil dikompilasi dan diverifikasi: `Artifacts/Kandidat-Layak-Uji-Bermain-2026-10-22/apk/GameSaham-v1.0.0-candidate-playtest.apk` (955.886 byte, SHA-256 `1c375c62...`). Signature Scheme v2 VALID. Panduan uji HP ringkas disusun di `Work/PANDUAN-UJI-BERMAIN-ANDROID-2026-10-22.md`. |

---

## 2. Rincian Teknis & Hasil Pengujian Komprehensif

### A. Rekonsiliasi & Kebenaran Data (P1)
- **Ukuran File APK**: 955.886 bytes (dikoreksi dari pencatatan lama 956.416 bytes).
- **Jumlah Tes Unit JVM**: 461 tes lulus pada 55 berkas XML laporan (`TEST-*.xml`).
  * Catatan: 455 tes adalah baseline sebelum penambahan pengujian `CatalogExpansionRegressionTest` (6 tes baru untuk katalog 100 perusahaan aktif).
- **Integritas Aset Bundle**:
  * 9 aset total dalam direktori `assets/` APK.
  * SHA-256 `assets/www/app.js`: `3db0676ab5e93d110058b002ba816233f0bce1e42fe78747e83e632e4acb4f0f` (100% identik dengan berkas lokal).
  * Dataset karantina `idx_bbca_daily.csv` secara tegas dikecualikan dari APK.
- **Save Pribadi**: SHA-256 `59b41e24c5a0a3b6f87d127d41c18b7f3f0ebe371f4dd2d277a31ca4b5f867ed` (terlindungi murni).

### B. Perjalanan Bermain Pemain (P2)
Skrip otomatis `Work/test-e2e-playtest-journey.py` memvalidasi alur end-to-end tanpa jalan pintas fixture:
1. **Inisialisasi**: Saldo kas IDR dan kekayaan bersih (`#home-net-worth`) tampil konsisten dengan domain.
2. **Pembelian Saham IDX (ASII)**: Pencarian emiten, pembukaan tiket order, review order terisolasi, pengiriman order -> status `FILLED`.
3. **Penjualan Bertahap & Penjualan Habis**:
   - Penjualan sebagian 100 lembar: modal detail posisi langsung sinkron in-place dari 200 saham ke 100 saham.
   - Penjualan sisa 100 lembar: kuantitas menjadi `0 saham`, metric hero menampilkan `"Rp 0 (Posisi ditutup)"`, laba terealisasi kotor tetap tercatat (`Rp -40.000`), dan catatan posisi ditutup sepenuhnya.
4. **Valuta Asing & Saham US**:
   - Konversi FX Rp 5.000.000 ke USD: dompet USD di tab Portofolio langsung sinkron tanpa reload.
   - Pembelian 2 saham `US:AAPL` (lot size = 1 lembar): order terisi dan terkonfirmasi dalam USD.
   - Order Limit Resting (di bawah harga pasar): status `OPEN`, dibatalkan via bridge -> receipt langsung berubah ke `CANCELLED` dan tombol pembatalan hilang otomatis.
5. **Firma / Bisnis**:
   - Saldo kas perusahaan dan rekening pribadi terpisah secara ketat. Transfer modal Rp 2.000.000 mengurangi kas perusahaan sebesar Rp 2.000.000 dan menambah kas pribadi sebesar Rp 2.000.000 secara presisi.
6. **Save & Reload Continuity**:
   - Reload halaman web: saldo IDR, saldo USD, dan 4 posisi saham tetap persis sama, membuktikan integritas serialisasi save game.

### C. Keberlanjutan 7 Hari & Penyimpanan (P3)
- **Siklus 7 Hari Kalender**: 7 kali eksekusi `skipDay` berturut-turut memicu settlement harian bisnis, pencatatan payroll, dan biaya operasional.
- **Kontrak Waktu Dual-Rate**:
  * Aktif 1x: 1 hari game = 5 menit nyata (rasio 288x).
  * Latar Belakang / Offline: 1 hari game = 1 jam nyata (rasio 24x).
  * Pengujian percepatan waktu melalui `tickClock` dan rekonsiliasi laporan offline terbukti sinkron.
- **Uji Stres Ekstrem JVM**:
  * 10 Seed × 100 Aksi Kritis pada `AllFeaturesExtreme10x100CriticalSequenceTest` (mencakup perbankan, matching engine, margin, short, reksa dana, transfer modal, dan siklus multi-tahun 730 hari) lulus 100% tanpa kegagalan.

### D. Aksesibilitas & Responsivitas (P4)
- **Matriks Viewport**:
  * Menguji 7 ukuran layar nyata: `320×568`, `360×640`, `390×844`, `412×915`, `430×932`, `768×1024`, dan `1440×900`.
  * Diuji pada skala font normal (100%) dan zoom aksesibilitas tinggi (200%).
  * Seluruh 70 sampel kombinasi tab dan resolusi terbukti **bebas horizontal overflow** (`scrollWidth <= clientWidth + 1`).
- **Touch Target**: Tombol navigasi bawah, tombol aksi emiten, dan tombol `JUAL`/`BELI` memenuhi standar ergonomi minimal 44–48 px.
- **Benchmark Performa (320 Sampel)**:
  * Navigasi 5 Tab (160 sampel): P95 = **90.30 ms** (Ambang batas <= 1000.0 ms) -> **PASS**
  * Aksi Biasa (128 sampel): P95 = **95.30 ms** (Ambang batas <= 300.0 ms) -> **PASS**
  * Pekerjaan Berat SkipDay (32 sampel): P95 = **161.84 ms** (Ambang batas <= 300.0 ms) -> **PASS**
  * Evaluator Regresi: 5/5 skenario uji mandiri lulus 100%.

---

## 3. Metadata Paket APK Kandidat Final

- **Nama File**: `GameSaham-v1.0.0-candidate-playtest.apk`
- **Lokasi Penyimpanan**: `Artifacts/Kandidat-Layak-Uji-Bermain-2026-10-22/apk/GameSaham-v1.0.0-candidate-playtest.apk`
- **Ukuran File**: **955.886 bytes (934 KB)**
- **SHA-256 Digest**: `1c375c621244179d44fa0588103fb96f7938323c7217e93707ed891121716ac6`
- **Tanda Tangan Digital**: `APK Signature Scheme v2 VALID` (Cert SHA-256 `d4fdeb3f...`)
- **Konfigurasi Android**:
  * Application ID: `com.tradingglobal3d.mobile`
  * Min SDK: 26 (Android 8.0 Oreo)
  * Target SDK: 36 (Android 16)
  * Orientasi: Portrait (Tegak) terkunci di manifest.

---

## 4. Status Gerbang Kelayakan (Gate Assessment)

1. **Host Implementation**: **COMPLETE (100% PASS)**
2. **Domain & Storage Engine**: **COMPLETE (461/461 JVM Tests PASS)**
3. **E2E Playtest Gameplay**: **COMPLETE (10/10 PASS, 17 Screenshots)**
4. **Sustainability & Dual-Rate**: **COMPLETE (4/4 PASS)**
5. **Accessibility & Viewports**: **COMPLETE (70/70 Matrix Samples PASS)**
6. **Performance Benchmark**: **COMPLETE (PASS_WITHIN_HOST_SCOPE)**
7. **Perlindungan Save Pribadi**: **VERIFIED UTUH (`59b41e24...`)**
8. **Aset Ilustrasi Gambar**: **ASSETS_PENDING_CODEX** (Sesuai keputusan pemilik proyek, ilustrasi raster dikerjakan oleh Codex melalui imagegen; UI saat ini menggunakan fallback typography/styling yang rapi dan fungsional).
9. **Pengujian Perangkat Fisik**: **NOT_TESTED / DEFERRED** (Siap diuji langsung oleh pemilik mengikuti panduan `Work/PANDUAN-UJI-BERMAIN-ANDROID-2026-10-22.md`).

**Kesimpulan Status**: **`READY_FOR_OWNER_PLAYTEST`**
