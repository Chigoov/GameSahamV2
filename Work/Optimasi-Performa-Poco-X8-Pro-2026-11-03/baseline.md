# BASELINE PENGUKURAN PERFORMA GAME SAHAM 2D PORTRAIT (POCO X8 PRO)

Tanggal: 3 November 2026, Asia/Jakarta  
Direktori Proyek: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`  
Dokumen Rencana: `Work/Optimasi-Performa-Poco-X8-Pro-2026-11-03/plan.md`  
Status Fase: **PF00 SELESAI (DIPERBAIKI_DAN_DIUJI_HOST)**

---

## 1. Identitas Repositori & Baseline Source Code

- **Root Direktori:** `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`
- **Git Branch:** `codex/classic-ui-new-assets` (up-to-date terhadap `origin/codex/classic-ui-new-assets`)
- **Git HEAD Commit:** `9653f7a992405b4dfaf28acddbf1caf158d03957`
- **Perubahan Lokal:** Seluruh delta lokal aktif (38 berkas dimodifikasi, berkas pengujian dan artefak untracked) dipertahankan utuh tanpa reset, clean, atau stash.
- **Integritas Save Pemain Asli:**
  - Berkas: `Artifacts/LocalPreview/preview-save/save_game_v2.json`
  - Hash SHA-256 Referensi: `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED`
  - Hash SHA-256 Terverifikasi: `59B41E24C5A0A3B6F87D127D41C18B7F3F0EBE371F4DD2D277A31CA4B5F867ED` (Bit-perfect, tidak pernah dibuka/disalin).

---

## 2. Baseline Paket APK & Toolchain Kompilasi

- **Kandidat APK Debug Baseline:**
  - Lokasi: `android/app/build/outputs/apk/debug/app-debug.apk`
  - Ukuran Berkas: `16.406.619 bytes` (15,65 MB)
  - Hash SHA-256: `91cb54c9366257e6aeb7dc89ba7c0ccceaf00feeddf72ab6ef1e2098973fc3ce`
- **Toolchain Lingkungan:**
  - Java Runtime: OpenJDK 21.0.10 (build 21.0.10+-14961533-b1163.108) di `C:/Program Files/Android/Android Studio/jbr`
  - Android SDK: platform-36 di `C:/Users/HYPE AMD/AppData/Local/Android/Sdk`
  - Build System: Gradle Wrapper 8.14.3
- **Status Gate Perangkat Sasaran (Poco X8 Pro):**
  - Sesuai batasan instruksi: **Tidak ada ADB, profiling perangkat, atau pemasangan APK otomatis tanpa izin tindakan konkret**.
  - Gate pengujian fisik pada Poco X8 Pro berstatus **OPEN / PENDING by policy**. Seluruh pengukuran baseline di bawah dijalankan secara objektif pada lingkungan host dan bridge/web runtime terisolasi.

---

## 3. Status Perbaikan Terdahulu (R01–R09) & Uji Regresi Host

Seluruh perbaikan logika bisnis, AON, booster resting order, holding company, short borrow fee, save version fence (NV01), warta Whale, fund rebalance, loader Android, dan indeks terbukti lulus pada test suite host:
- `PackageP01SafetyAndDomainTest`: 6/6 PASS
- `PackageP02OrderAndTrustTest`: 5/5 PASS
- `PackageP03EconomyAndPolicyTest`: 8/8 PASS
- `PackageP04NewsAndTerminalTest`: 8/8 PASS
- `PackageP05CatalogAndProfileTest`: 6/6 PASS
- `PackageP07ReplaySecurityTest`: 2/2 PASS
- `PackageQCFixesTest`: 6/6 PASS
- `PackageATFixesTest`: 6/6 PASS
- `PackageR01ToR06VerificationTest`: 6/6 PASS
- `PackageR08ToR09VerificationTest`: 5/5 PASS
- **Total Regresi:** **58 dari 58 pengujian lulus 100% (0 failure, 0 error)**.

---

## 4. Pemetaan Metodologi Pengukuran Baseline (PF01)

Skenario pengukuran tetap disiapkan menggunakan data QA sintetis berlabel:
- 20 instrumen aktif (10 IDX, 10 US)
- Portofolio QA dengan 5 posisi saham, 2 pesanan aktif (*resting order*), 1 pinjaman tenor, 1 anak usaha
- Riwayat transaksi QA: 50 baris transaksi

Metode profiling komponen:
1. **Domain Java & Serialization Cost:** Pengukuran durasi eksekusi `TradingFacade.getSnapshotJson()`, `getInstruments()`, `getOrderBook()`, dan evaluasi `personalNetWorth()`.
2. **Bridge IPC & JSON Parsing Overhead:** Ukuran string JSON payload dan waktu deserialisasi `JSON.parse` di JavaScript.
3. **DOM Layout & Chart Render Cost:** Durasi pembaruan DOM per detik, jumlah elemen yang diganti vs dipertahankan (*DOM mutation rate*), dan pemanggilan `renderChart` / SVG rebuild.
4. **Interval Stacking Analysis:** Frekuensi tumpukan pembaruan 1 detik (`setInterval`) terhadap callback `requestClockSync` dan redundansi panggilan ganda `refreshSnapshot()`.
